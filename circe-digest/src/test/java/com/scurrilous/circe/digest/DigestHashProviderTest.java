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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

import com.scurrilous.circe.HashProviders;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.params.SimpleHashParameters;

@SuppressWarnings("javadoc")
public class DigestHashProviderTest {

    private static final DigestHashProvider PROVIDER = new DigestHashProvider();
    private static final SimpleHashParameters MD5 = new SimpleHashParameters("MD5");

    @Test
    public void testQuerySupportKnown() {
        assertEquals(EnumSet.of(HashSupport.STATEFUL), PROVIDER.querySupport(MD5));
    }

    @Test
    public void testQuerySupportUnknown() {
        assertTrue(PROVIDER.querySupport(new SimpleHashParameters("NONSENSE!")).isEmpty());
    }

    @Test
    public void testCreateStateful() {
        assertEquals(MD5.algorithm(), PROVIDER.createStateful(MD5).algorithm());
    }

    @Test
    public void testDiscovery() {
        assertThat(HashProviders.search(MD5).values(), hasItem(isA(DigestHashProvider.class)));
    }
}
