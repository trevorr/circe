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
package com.scurrilous.circe.impl;

import static com.scurrilous.circe.HashSupport.INT_SIZED;
import static com.scurrilous.circe.HashSupport.NATIVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.scurrilous.circe.Hash;
import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.HashProvider;
import com.scurrilous.circe.IncrementalIntHash;
import com.scurrilous.circe.crc.StandardCrcProvider;
import com.scurrilous.circe.params.CrcParameters;

@SuppressWarnings("javadoc")
public class GuavaHashCacheTest {

    private static class TestParams implements HashParameters {

        @Override
        public String algorithm() {
            return null;
        }
    }

    private static class TestHash implements Hash {

        @Override
        public String algorithm() {
            return null;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean supportsUnsafe() {
            return false;
        }
    }

    private class HashLoader implements Callable<Hash> {

        private final Hash loadValue;

        protected HashLoader(Hash loadValue) {
            this.loadValue = loadValue;
        }

        @Override
        public Hash call() throws Exception {
            return loadValue;
        }
    }

    @Test
    public void testCache() throws Exception {
        final GuavaHashCache c = new GuavaHashCache();
        final HashParameters p1 = new TestParams();
        final Hash h1 = new TestHash();
        assertEquals(h1, c.get(p1, EnumSet.of(INT_SIZED), new HashLoader(h1)));
        assertEquals(h1, c.get(p1, EnumSet.of(INT_SIZED), new HashLoader(new TestHash())));
        final Hash h1n = new TestHash();
        assertEquals(h1n, c.get(p1, EnumSet.of(INT_SIZED, NATIVE), new HashLoader(h1n)));
        assertEquals(h1n, c.get(p1, EnumSet.of(NATIVE), new HashLoader(new TestHash())));
        final HashParameters p2 = new TestParams();
        final Hash h2 = new TestHash();
        assertEquals(h2, c.get(p2, EnumSet.of(INT_SIZED), new HashLoader(h2)));
        assertEquals(h2, c.get(p2, EnumSet.of(INT_SIZED), new HashLoader(new TestHash())));
    }

    @Test
    public void testProvider() throws Exception {
        final HashProvider provider = new StandardCrcProvider();
        final IncrementalIntHash i1 = provider.getIncrementalInt(CrcParameters.CRC16);
        final IncrementalIntHash i2 = provider.getIncrementalInt(CrcParameters.CRC16);
        assertTrue(i1 == i2);
    }
}
