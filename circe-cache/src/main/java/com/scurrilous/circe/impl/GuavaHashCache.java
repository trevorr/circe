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

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.scurrilous.circe.Hash;
import com.scurrilous.circe.HashParameters;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.impl.HashCache;

/**
 * Implementation of {@link HashCache} using a Guava {@link Cache}.
 * <p>
 * A {@linkplain CacheBuilderSpec cache configuration specification} can be
 * supplied using the system property
 * {@code com.scurrilous.circe.GuavaHashCache.cacheSpec}. If no specification is
 * provided, the default configuration is an initial capacity of 4, a
 * concurrency level of 1, and soft values.
 */
public class GuavaHashCache implements HashCache {

    private static class Key {

        HashParameters params;
        EnumSet<HashSupport> support;

        Key(HashParameters params, EnumSet<HashSupport> support) {
            this.params = params;
            this.support = support;
        }

        @Override
        public int hashCode() {
            return params.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Key))
                return false;
            if (obj.getClass() != Key.class)
                return obj.equals(this);
            final Key other = (Key) obj;
            return params.equals(other.params) && support.equals(other.support);
        }
    }

    private static class SearchKey extends Key {

        SearchKey(HashParameters params, EnumSet<HashSupport> support) {
            super(params, support);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof Key))
                return false;
            final Key other = (Key) obj;
            return params.equals(other.params) && other.support.containsAll(support);
        }
    }

    private final Cache<Key, Hash> cache;

    /**
     * Constructs a new {@link GuavaHashCache}.
     */
    public GuavaHashCache() {
        String cacheSpec;
        try {
            cacheSpec = System.getProperty(GuavaHashCache.class.getName() + ".cacheSpec");
        } catch (final SecurityException e) {
            cacheSpec = null;
        }

        final CacheBuilder<Object, Object> cacheBuilder;
        if (cacheSpec != null)
            cacheBuilder = CacheBuilder.from(cacheSpec);
        else
            cacheBuilder = CacheBuilder.newBuilder().initialCapacity(4).concurrencyLevel(1)
                    .softValues();
        cache = cacheBuilder.build();
    }

    @Override
    public Hash get(HashParameters params, EnumSet<HashSupport> required, Callable<Hash> loader)
            throws ExecutionException {
        final Hash hash = cache.getIfPresent(new SearchKey(params, required));
        return hash != null ? hash : cache.get(new Key(params, required), loader);
    }
}
