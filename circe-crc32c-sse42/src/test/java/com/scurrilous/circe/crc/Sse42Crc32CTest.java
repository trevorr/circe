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

import static com.scurrilous.circe.params.CrcParameters.CRC32C;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.scurrilous.circe.IncrementalIntHash;

@SuppressWarnings("javadoc")
public class Sse42Crc32CTest {

    private static final Charset ASCII = Charset.forName("ASCII");
    private static final Sse42Crc32CProvider PROVIDER = new Sse42Crc32CProvider();

    private IncrementalIntHash NATIVE_CRC32C;

    @Before
    public void checkSupported() {
        assumeTrue(Sse42Crc32C.isSupported());
        NATIVE_CRC32C = PROVIDER.getIncrementalInt(CRC32C);
    }

    @Test
    public void testCrc32c() {
        testASCII(0xe3069283, "123456789");
        testASCII(0x22620404, "The quick brown fox jumps over the lazy dog");
        byte[] bytes = new byte[32];
        testBytes(0x8a9136aa, bytes);
        Arrays.fill(bytes, (byte) 0xff);
        testBytes(0x62a8ab43, bytes);
        bytes = new byte[1024];
        testBytes(0xeeaede7c, bytes);
    }

    private void testASCII(int expected, String s) {
        testBytes(expected, s.getBytes(ASCII));
    }

    private void testBytes(int expected, byte[] bytes) {
        assertEquals(expected, NATIVE_CRC32C.calculate(bytes));
    }

    @Test
    public void testByteBuffer() {
        final ByteBuffer buf = ByteBuffer.allocate(1024);
        assertEquals(0xeeaede7c, NATIVE_CRC32C.calculate(buf));
    }

    @Test
    public void testDirectByteBuffer() {
        final ByteBuffer buf = ByteBuffer.allocateDirect(1024);
        assertEquals(0xeeaede7c, NATIVE_CRC32C.calculate(buf));
    }

    @Test
    public void testReadOnlyByteBuffer() {
        final ByteBuffer buf = ByteBuffer.allocate(1024).asReadOnlyBuffer();
        assertEquals(0xeeaede7c, NATIVE_CRC32C.calculate(buf));
    }

    @Test
    public void testUnaligned() {
        final byte[] bytes = "123456789".getBytes(ASCII);
        assertEquals(0xbfe92a83, NATIVE_CRC32C.calculate(bytes, 1, 8));
    }

    @Test
    public void testChunking() {
        final int minWords = ChunkedCrcParameters.MIN_CHUNK_WORDS;
        final int maxWords = minWords * 2;
        final int len = maxWords * 8;
        final byte[] bytes = new byte[len];
        final int expected = new StandardCrcProvider().getIncrementalInt(CRC32C).calculate(bytes);
        for (int i = minWords; i <= maxWords; ++i) {
            final ChunkedCrcParameters params = new ChunkedCrcParameters(CRC32C, new int[] { i });
            final IncrementalIntHash impl = PROVIDER.getIncrementalInt(params);
            assertEquals(expected, impl.calculate(bytes));
        }
    }

    @Test
    public void testCRC32CIncremental() {
        final String data = "data";
        final String combined = data + data;

        final int dataChecksum = NATIVE_CRC32C.calculate(data.getBytes(ASCII));
        final int combinedChecksum = NATIVE_CRC32C.calculate(combined.getBytes(ASCII));
        final int incrementalChecksum = NATIVE_CRC32C.resume(dataChecksum, data.getBytes(ASCII));
        assertEquals(combinedChecksum, incrementalChecksum);
    }
}
