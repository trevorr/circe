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
package com.scurrilous.circe.digest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Test;

import com.scurrilous.circe.StatefulHash;

@SuppressWarnings("javadoc")
public class DigestHashTest {

    private static final byte[] DATA = "hello world".getBytes();

    private final StatefulHash hash;

    public DigestHashTest() throws NoSuchAlgorithmException {
        hash = new DigestHash(MessageDigest.getInstance("MD5"));
    }

    @Test
    public void testAlgorithm() {
        assertEquals("MD5", hash.algorithm());
    }

    @Test
    public void testLength() {
        assertEquals(16, hash.length());
    }

    @Test
    public void testCreateNew() {
        hash.update(DATA);
        assertEquals(0xd0ee1ee0bb3bb65eL, hash.getLong());
        final StatefulHash other = hash.createNew();
        assertEquals(0x04b2008fd98c1dd4L, other.getLong());
        assertEquals(0xd0ee1ee0bb3bb65eL, hash.getLong());
    }

    @Test
    public void testReset() {
        hash.update(DATA);
        hash.reset();
        assertEquals(0x04b2008fd98c1dd4L, hash.getLong());
    }

    @Test
    public void testUpdateByteArray() {
        hash.update(DATA);
        assertEquals(0xd0ee1ee0bb3bb65eL, hash.getLong());
    }

    @Test
    public void testUpdateByteArrayIntInt() {
        hash.update(DATA, 6, 5);
        assertEquals(0x860176a03730797dL, hash.getLong());
    }

    @Test
    public void testUpdateByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.wrap(DATA);
        buffer.position(6);
        hash.update(buffer);
        assertEquals(0x860176a03730797dL, hash.getLong());
    }

    @Test
    public void testGetBytes() {
        hash.update(DATA);
        assertArrayEquals(new BigInteger("5eb63bbbe01eeed093cb22bb8f5acdc3", 16).toByteArray(),
                hash.getBytes());
    }

    @Test
    public void testGetBytesByteArrayIntInt() {
        hash.update(DATA);
        final byte[] array = new byte[16];
        array[0] = 0x11;
        array[15] = 0x22;
        hash.getBytes(array, 1, 14);
        assertArrayEquals(new BigInteger("115eb63bbbe01eeed093cb22bb8f5a22", 16).toByteArray(),
                array);
    }

    @Test
    public void testGetByte() {
        hash.update(DATA);
        assertEquals(0x5e, hash.getByte());
    }

    @Test
    public void testGetShort() {
        hash.update(DATA);
        assertEquals((short) 0xb65e, hash.getShort());
    }

    @Test
    public void testGetInt() {
        hash.update(DATA);
        assertEquals(0xbb3bb65e, hash.getInt());
    }

    @Test
    public void testGetLong() {
        hash.update(DATA);
        assertEquals(0xd0ee1ee0bb3bb65eL, hash.getLong());
    }
}
