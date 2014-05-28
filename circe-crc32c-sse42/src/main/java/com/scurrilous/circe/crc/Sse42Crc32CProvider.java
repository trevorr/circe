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

import java.util.EnumSet;

import com.scurrilous.circe.Hash;
import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.StatelessHash;
import com.scurrilous.circe.impl.AbstractHashProvider;
import com.scurrilous.circe.params.CrcParameters;

/**
 * Provides new instances of the SSE 4.2 CRC-32C hash function, when supported
 * by hardware.
 */
public final class Sse42Crc32CProvider extends AbstractHashProvider<HashParameters> {

    // process in chunks of 32 KB, then 4 KB, then 512 bytes
    private static final int[] DEFAULT_CHUNK_WORDS = new int[] { 4096, 512, 64 };

    /**
     * Constructs a new {@link Sse42Crc32CProvider}.
     */
    public Sse42Crc32CProvider() {
        super(HashParameters.class);
    }

    @Override
    protected EnumSet<HashSupport> querySupportTyped(HashParameters params) {
        if (isCrc32C(params) && Sse42Crc32C.isSupported())
            return EnumSet.allOf(HashSupport.class);
        return EnumSet.noneOf(HashSupport.class);
    }

    @Override
    protected Hash get(HashParameters params, EnumSet<HashSupport> required) {
        if (isCrc32C(params) && Sse42Crc32C.isSupported())
            return getCacheable(params, required);
        throw new UnsupportedOperationException();
    }

    private static boolean isCrc32C(HashParameters params) {
        if (params instanceof ChunkedCrcParameters)
            return ((ChunkedCrcParameters) params).crcParameters().equals(CrcParameters.CRC32C);
        return params.equals(CrcParameters.CRC32C);
    }

    @Override
    protected StatelessHash createCacheable(HashParameters params, EnumSet<HashSupport> required) {
        final int[] chunkWords;
        if (params instanceof ChunkedCrcParameters)
            chunkWords = ((ChunkedCrcParameters) params).chunkWords();
        else
            chunkWords = DEFAULT_CHUNK_WORDS;
        return new Sse42Crc32C(chunkWords);
    }
}
