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
package com.scurrilous.circe.guava;

import java.nio.ByteBuffer;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.scurrilous.circe.StatefulHash;

/**
 * Provides a {@link StatefulHash} based on {@link Hasher} objects obtained from
 * an underlying {@link HashFunction}. {@link Hasher} does not support unsafe
 * memory access or incremental hashing.
 */
public class HasherHash implements StatefulHash {

    final HashFunction function;
    final String algorithm;
    private Hasher hasher;
    private HashCode hashCode;

    /**
     * Constructs a {@link HasherHash} using the given underlying
     * {@link HashFunction}.
     * 
     * @param function the hash function from which to obtain hashers
     * @param algorithm the name of the algorithm the function implements
     */
    public HasherHash(HashFunction function, String algorithm) {
        this.function = function;
        this.algorithm = algorithm;
    }

    @Override
    public String algorithm() {
        return algorithm;
    }

    @Override
    public int length() {
        return function.bits() / 8;
    }

    @Override
    public boolean supportsUnsafe() {
        return false;
    }

    @Override
    public StatefulHash createNew() {
        return new HasherHash(function, algorithm);
    }

    @Override
    public boolean supportsIncremental() {
        return false;
    }

    @Override
    public void reset() {
        hasher = null;
        hashCode = null;
    }

    private Hasher getHasher(int expectedInputSize) {
        if (hasher == null) {
            if (hashCode != null)
                throw new IllegalStateException();
            hasher = function.newHasher(expectedInputSize);
        }
        return hasher;
    }

    @Override
    public void update(byte[] input) {
        getHasher(input.length).putBytes(input);
    }

    @Override
    public void update(byte[] input, int index, int length) {
        if (length < 0)
            throw new IllegalArgumentException();
        getHasher(length).putBytes(input, index, length);
    }

    @Override
    public void update(ByteBuffer input) {
        final int remaining = input.remaining();
        getHasher(remaining);
        if (input.hasArray()) {
            hasher.putBytes(input.array(), input.arrayOffset() + input.position(), remaining);
            input.position(input.limit());
        } else {
            byte[] array = new byte[remaining];
            input.get(array);
            hasher.putBytes(array);
        }
    }

    @Override
    public void update(long address, long length) {
        throw new UnsupportedOperationException();
    }

    private HashCode getHashCode() {
        if (hashCode == null) {
            hashCode = getHasher(0).hash();
            hasher = null;
        }
        return hashCode;
    }

    @Override
    public byte[] getBytes() {
        return getHashCode().asBytes();
    }

    @Override
    public int getBytes(byte[] output, int index, int maxLength) {
        if (maxLength < 0)
            throw new IllegalArgumentException();
        if (index < 0 || index + maxLength > output.length)
            throw new IndexOutOfBoundsException();
        return getHashCode().writeBytesTo(output, index, maxLength);
    }

    @Override
    public byte getByte() {
        return (byte) getHashCode().padToLong();
    }

    @Override
    public short getShort() {
        return (short) getHashCode().padToLong();
    }

    @Override
    public int getInt() {
        return (int) getHashCode().padToLong();
    }

    @Override
    public long getLong() {
        return getHashCode().padToLong();
    }
}
