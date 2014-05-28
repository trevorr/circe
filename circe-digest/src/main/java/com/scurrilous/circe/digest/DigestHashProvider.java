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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;

import com.scurrilous.circe.Hash;
import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.impl.AbstractHashProvider;

/**
 * Provides {@link DigestHash} instances based on an algorithm name.
 */
public final class DigestHashProvider extends AbstractHashProvider<HashParameters> {

    /**
     * Constructs a new {@link DigestHashProvider}.
     */
    public DigestHashProvider() {
        super(HashParameters.class);
    }

    @Override
    protected EnumSet<HashSupport> querySupportTyped(HashParameters params) {
        try {
            MessageDigest.getInstance(params.algorithm());
            return EnumSet.of(HashSupport.STATEFUL);
        } catch (NoSuchAlgorithmException e) {
            return EnumSet.noneOf(HashSupport.class);
        }
    }

    @Override
    protected Hash get(HashParameters params, EnumSet<HashSupport> required) {
        try {
            return new DigestHash(MessageDigest.getInstance(params.algorithm()));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
