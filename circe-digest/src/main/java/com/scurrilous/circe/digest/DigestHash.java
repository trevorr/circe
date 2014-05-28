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

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import com.scurrilous.circe.StatefulHash;

/**
 * Provides a {@link StatefulHash} based on an underlying {@link MessageDigest},
 * which does not support unsafe memory access or incremental hashing.
 */
public final class DigestHash implements StatefulHash {

    private final MessageDigest digest;
    private byte[] savedDigest;

    /**
     * Constructs a {@link DigestHash} using the given underlying
     * {@link MessageDigest}.
     * 
     * @param digest the digest algorithm used to perform the hashing
     */
    public DigestHash(MessageDigest digest) {
        this.digest = digest;
    }

    @Override
    public String algorithm() {
        return digest.getAlgorithm();
    }

    @Override
    public int length() {
        return digest.getDigestLength();
    }

    @Override
    public boolean supportsUnsafe() {
        return false;
    }

    @Override
    public StatefulHash createNew() {
        try {
            return new DigestHash(MessageDigest.getInstance(digest.getAlgorithm(),
                    digest.getProvider()));
        } catch (final NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public boolean supportsIncremental() {
        return false;
    }

    @Override
    public void reset() {
        digest.reset();
        savedDigest = null;
    }

    private void checkUpdating() {
        if (savedDigest != null)
            throw new IllegalStateException();
    }

    @Override
    public void update(byte[] input) {
        checkUpdating();
        digest.update(input);
    }

    @Override
    public void update(byte[] input, int index, int length) {
        checkUpdating();
        digest.update(input, index, length);
    }

    @Override
    public void update(ByteBuffer input) {
        checkUpdating();
        digest.update(input);
    }

    @Override
    public void update(long address, long length) {
        throw new UnsupportedOperationException();
    }

    private byte[] getSavedDigest() {
        if (savedDigest == null)
            savedDigest = digest.digest();
        return savedDigest;
    }

    @Override
    public byte[] getBytes() {
        getSavedDigest();
        return Arrays.copyOf(savedDigest, savedDigest.length);
    }

    @Override
    public int getBytes(byte[] output, int index, int maxLength) {
        if (maxLength < 0)
            throw new IllegalArgumentException();
        if (index < 0 || index + maxLength > output.length)
            throw new IndexOutOfBoundsException();
        getSavedDigest();
        final int length = Math.min(savedDigest.length, maxLength);
        System.arraycopy(savedDigest, 0, output, index, length);
        return length;
    }

    @Override
    public byte getByte() {
        return getSavedDigest()[0];
    }

    @Override
    public short getShort() {
        getSavedDigest();
        return (short) (savedDigest[0] | (savedDigest.length > 1 ? savedDigest[1] << 8 : 0));
    }

    @Override
    public int getInt() {
        int result = getSavedDigest()[0] & 0xff;
        switch (savedDigest.length) {
        default:
            result |= (savedDigest[3] & 0xff) << 24;
        case 3:
            result |= (savedDigest[2] & 0xff) << 16;
        case 2:
            result |= (savedDigest[1] & 0xff) << 8;
        case 1:
        }
        return result;
    }

    @Override
    public long getLong() {
        long result = getSavedDigest()[0] & 0xffL;
        switch (savedDigest.length) {
        default:
            result |= (savedDigest[7] & 0xffL) << 56;
        case 7:
            result |= (savedDigest[6] & 0xffL) << 48;
        case 6:
            result |= (savedDigest[5] & 0xffL) << 40;
        case 5:
            result |= (savedDigest[4] & 0xffL) << 32;
        case 4:
            result |= (savedDigest[3] & 0xffL) << 24;
        case 3:
            result |= (savedDigest[2] & 0xffL) << 16;
        case 2:
            result |= (savedDigest[1] & 0xffL) << 8;
        case 1:
        }
        return result;
    }
}
