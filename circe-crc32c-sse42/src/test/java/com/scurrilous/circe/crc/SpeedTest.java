/*******************************************************************************
 * Copyright 2014 Trevor Robinson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.scurrilous.circe.crc;

import static com.scurrilous.circe.params.CrcParameters.CRC32;
import static com.scurrilous.circe.params.CrcParameters.CRC32C;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;

import org.junit.Test;

import sun.nio.ch.DirectBuffer;

import com.scurrilous.circe.HashProvider;
import com.scurrilous.circe.Hashes;
import com.scurrilous.circe.StatefulHash;

@SuppressWarnings({ "javadoc", "restriction" })
public class SpeedTest {

    private static final int MB = 1024 * 1024;

    private static class Config {
        int minLength;
        int maxLength;
        float factor;
        int repetitions;

        public Config(int minLength, int maxLength, float factor, int repetitions) {
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.factor = factor;
            this.repetitions = repetitions;
        }
    }

    private interface Emitter {
        void start(String label);

        void emit(int len, float rate);
    }

    private enum StdoutEmitter implements Emitter {
        INSTANCE;

        @Override
        public void start(String label) {
            System.out.format("%s:%n", label);
        }

        @Override
        public void emit(int len, float rate) {
            System.out.format("  %d: rate = %g MB/s%n", len, rate);
        }
    }

    private static class GnuplotEmitter implements Emitter, Closeable {

        final PrintWriter ctrl;
        final PrintWriter data;
        int plots;

        public GnuplotEmitter(String ctrlFilename, String dataFilename, String outputFilename)
                throws IOException {
            ctrl = new PrintWriter(ctrlFilename);
            ctrl.println("set term png size 1600, 1200");
            ctrl.format("set output '%s'%n", outputFilename);
            ctrl.println("set logscale x");
            ctrl.println("set xlabel 'bytes'");
            ctrl.println("set ylabel 'MB/s'");
            ctrl.println("set key right bottom");
            ctrl.format("plot '%s'", dataFilename);
            data = new PrintWriter(dataFilename);
        }

        @Override
        public void start(String label) {
            StdoutEmitter.INSTANCE.start(label);
            if (plots > 0) {
                ctrl.print(", ''");
                data.println();
                data.println();
            }
            ctrl.format(" index %d with lines title '%s'", plots, label);
            ++plots;
        }

        @Override
        public void emit(int len, float rate) {
            StdoutEmitter.INSTANCE.emit(len, rate);
            data.format("%d %f%n", len, rate);
        }

        @Override
        public void close() throws IOException {
            ctrl.close();
            data.close();
        }
    }

    private static abstract class HashTester {

        final StatefulHash hash;
        final Config config;
        final Emitter emitter;

        public HashTester(StatefulHash hash, Config config, Emitter emitter) {
            this.hash = hash;
            this.config = config;
            this.emitter = emitter;
        }

        public void test() {
            init();
            for (int len = config.minLength; len <= config.maxLength; len = (int) Math.ceil(len *
                    config.factor)) {
                if (len > 256)
                    len = (len + 7) & ~7;
                long bestNanos = 0;
                int ofs = 0;
                for (int rep = 0; rep < config.repetitions; ++rep) {
                    ofs = 0;
                    final long start = System.nanoTime();
                    for (; ofs + len <= config.maxLength; ofs += len) {
                        testRange(ofs, len);
                    }
                    final long end = System.nanoTime();
                    final long nanos = end - start;
                    if (rep == 0 || nanos < bestNanos)
                        bestNanos = nanos;
                }
                final float rate = (float) ofs / MB / (bestNanos / 1e9f);
                emitter.emit(len, rate);
            }
        }

        abstract void init();

        abstract void testRange(int ofs, int len);
    }

    private static class ArrayHashTester extends HashTester {

        byte[] array;

        public ArrayHashTester(StatefulHash hash, Config config, Emitter emitter) {
            super(hash, config, emitter);
        }

        @Override
        void init() {
            array = new byte[config.maxLength];
            for (int i = 0; i < config.maxLength; ++i)
                array[i] = (byte) i;
        }

        @Override
        void testRange(int ofs, int len) {
            hash.update(array, ofs, len);
        }
    }

    private static class BufferHashTester extends HashTester {

        ByteBuffer buffer;

        public BufferHashTester(StatefulHash hash, Config config, Emitter emitter) {
            super(hash, config, emitter);
        }

        @Override
        void init() {
            buffer = ByteBuffer.allocateDirect(config.maxLength);
            for (int i = 0; i < config.maxLength; ++i)
                buffer.put((byte) i);
        }

        @Override
        void testRange(int ofs, int len) {
            buffer.position(ofs);
            buffer.limit(ofs + len);
            hash.update(buffer);
        }
    }

    private static class UnsafeHashTester extends BufferHashTester {

        private long address;

        public UnsafeHashTester(StatefulHash hash, Config config, Emitter emitter) {
            super(hash, config, emitter);
        }

        @Override
        void init() {
            super.init();
            address = ((DirectBuffer) buffer).address();
        }

        @Override
        void testRange(int ofs, int len) {
            hash.update(address + ofs, len);
        }
    }

    private static void test(Config config, Emitter emitter) {
        final HashProvider std = new StandardCrcProvider();
        final StatefulHash stdCrc32c = std.createStateful(CRC32C);
        final StatefulHash javaCrc32 = std.createStateful(CRC32);
        final StatefulHash crc32c = Hashes.createStateful(CRC32C);
        emitter.start("Best CRC32C (array)");
        new ArrayHashTester(crc32c, config, emitter).test();
        emitter.start("Best CRC32C (direct buffer)");
        new BufferHashTester(crc32c, config, emitter).test();
        if (crc32c.supportsUnsafe()) {
            emitter.start("Best CRC32C (unsafe)");
            new UnsafeHashTester(crc32c, config, emitter).test();
        }
        emitter.start("Pure Java CRC32C");
        new ArrayHashTester(stdCrc32c, config, emitter).test();
        emitter.start("Java (zlib) CRC32");
        new ArrayHashTester(javaCrc32, config, emitter).test();
    }

    @Test
    public void test() throws Exception {
        if ("full".equals(System.getProperty("SpeedTest.mode"))) {
            final Config config = new Config(1, 128 * MB, (float) Math.sqrt(2), 2);
            final GnuplotEmitter emitter = new GnuplotEmitter("crc32c.gpi", "crc32c.dat",
                    "crc32c.png");
            test(config, emitter);
            emitter.close();
        } else {
            final Config config = new Config(MB, 128 * MB, 2.0f, 1);
            test(config, StdoutEmitter.INSTANCE);
        }
    }
}
