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

import com.google.common.hash.HashFunction;
import com.scurrilous.circe.StatefulIntHash;
import com.scurrilous.circe.StatelessIntHash;
import com.scurrilous.circe.impl.AbstractStatelessIntHash;

/**
 * Int-sized {@link HasherHash}.
 */
public final class HasherIntHash extends HasherHash implements StatefulIntHash {

    /**
     * Constructs a {@link HasherIntHash} using the given underlying
     * {@link HashFunction}.
     * 
     * @param function the hash function from which to obtain hashers
     * @param algorithm the name of the algorithm the function implements
     */
    public HasherIntHash(HashFunction function, String algorithm) {
        super(function, algorithm);
    }

    @Override
    public StatelessIntHash asStateless() {
        return new AbstractStatelessIntHash() {
            @Override
            public String algorithm() {
                return algorithm;
            }

            @Override
            public int length() {
                return Long.SIZE;
            }

            @Override
            public StatefulIntHash createStateful() {
                return new HasherIntHash(function, algorithm);
            }

            @Override
            protected int calculateUnchecked(byte[] input, int index, int length) {
                return function.hashBytes(input, index, length).asInt();
            }
        };
    }
}
