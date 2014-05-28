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

import java.util.EnumSet;

import com.google.common.hash.Hashing;
import com.scurrilous.circe.Hash;
import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.impl.AbstractHashProvider;
import com.scurrilous.circe.params.MurmurHash3Parameters;
import com.scurrilous.circe.params.SipHash24Parameters;

/**
 * Provides hash function instances implemented in Guava.
 */
public final class GuavaHashProvider extends AbstractHashProvider<HashParameters> {

    /**
     * Constructs a new {@link GuavaHashProvider}.
     */
    public GuavaHashProvider() {
        super(HashParameters.class);
    }

    @Override
    protected EnumSet<HashSupport> querySupportTyped(HashParameters params) {
        if (params instanceof SipHash24Parameters)
            return EnumSet.of(HashSupport.STATEFUL, HashSupport.LONG_SIZED);
        if (params instanceof MurmurHash3Parameters) {
            switch (((MurmurHash3Parameters) params).variant()) {
            case X86_32:
                return EnumSet.of(HashSupport.STATEFUL, HashSupport.INT_SIZED);
            case X64_128:
                return EnumSet.of(HashSupport.STATEFUL);
            default:
                return EnumSet.noneOf(HashSupport.class);
            }
        }
        return EnumSet.noneOf(HashSupport.class);
    }

    @Override
    protected Hash get(HashParameters params, EnumSet<HashSupport> required) {
        if (params instanceof SipHash24Parameters) {
            final SipHash24Parameters sipParams = (SipHash24Parameters) params;
            return new HasherLongHash(Hashing.sipHash24(sipParams.seedLow(), sipParams.seedHigh()),
                    params.algorithm());
        }
        if (params instanceof MurmurHash3Parameters) {
            final MurmurHash3Parameters murmurParams = (MurmurHash3Parameters) params;
            final int seed = murmurParams.seed();
            switch (murmurParams.variant()) {
            case X86_32:
                return new HasherIntHash(Hashing.murmur3_32(seed), params.algorithm());
            case X64_128:
                return new HasherHash(Hashing.murmur3_128(seed), params.algorithm());
            default:
                throw new UnsupportedOperationException();
            }
        }
        throw new UnsupportedOperationException();
    }
}
