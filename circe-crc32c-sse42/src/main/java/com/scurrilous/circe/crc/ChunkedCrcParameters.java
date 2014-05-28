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

import java.util.Arrays;

import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.params.CrcParameters;

/**
 * Describes parameters for a CRC algorithm that can process input in chunks for
 * improved performance. Chunk sizes are specified in units of 64-bit words.
 */
public class ChunkedCrcParameters implements HashParameters {

    /**
     * The minimum number of words in a chunk.
     */
    public static final int MIN_CHUNK_WORDS = 4;

    private final CrcParameters crcParameters;
    private final int[] chunkWords;

    /**
     * Constructs a {@link ChunkedCrcParameters} with the given generic CRC
     * parameters and chunk size(s). Chunk sizes are specified in units of
     * 64-bit words, must be at least {@link #MIN_CHUNK_WORDS}, and must be in
     * strictly decreasing order.
     * 
     * @param crcParameters the generic CRC parameters
     * @param chunkWords an array of chunk sizes in 64-bit words
     * @throws IllegalArgumentException if any of the chunk sizes are less than
     *             {@link #MIN_CHUNK_WORDS} or if they are not in strictly
     *             decreasing order
     */
    public ChunkedCrcParameters(CrcParameters crcParameters, int[] chunkWords) {
        for (int i = 0; i < chunkWords.length; ++i) {
            if (chunkWords[i] < MIN_CHUNK_WORDS)
                throw new IllegalArgumentException("chunk words cannot be less than 4");
            if (i > 0 && chunkWords[i] >= chunkWords[i - 1])
                throw new IllegalArgumentException("chunk words must be strictly decreasing");
        }
        this.crcParameters = crcParameters;
        this.chunkWords = Arrays.copyOf(chunkWords, chunkWords.length);
    }

    @Override
    public String algorithm() {
        return crcParameters.algorithm();
    }

    /**
     * Returns the generic CRC parameters specified by this object.
     * 
     * @return the generic CRC parameters
     */
    public CrcParameters crcParameters() {
        return crcParameters;
    }

    /**
     * Returns an array of chunk sizes specified by this object. The returned
     * array is a copy that may be freely modified by the caller.
     * 
     * @return an array of chunk sizes in 64-bit words
     */
    public int[] chunkWords() {
        return Arrays.copyOf(chunkWords, chunkWords.length);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        final ChunkedCrcParameters other = (ChunkedCrcParameters) obj;
        return crcParameters.equals(other.crcParameters) &&
                Arrays.equals(chunkWords, other.chunkWords);
    }

    @Override
    public int hashCode() {
        return crcParameters.hashCode() ^ Arrays.hashCode(chunkWords);
    }
}
