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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;

import org.junit.Test;

import com.scurrilous.circe.HashProviders;
import com.scurrilous.circe.HashSupport;
import com.scurrilous.circe.params.MurmurHash3Parameters;
import com.scurrilous.circe.params.MurmurHash3Variant;
import com.scurrilous.circe.params.SipHash24Parameters;

@SuppressWarnings("javadoc")
public class GuavaHashProviderTest {

    private static final GuavaHashProvider PROVIDER = new GuavaHashProvider();
    private static final MurmurHash3Parameters MURMUR3_32 = new MurmurHash3Parameters(
            MurmurHash3Variant.X86_32);
    private static final MurmurHash3Parameters MURMUR3_128 = new MurmurHash3Parameters(
            MurmurHash3Variant.X64_128);
    private static final SipHash24Parameters SIP = new SipHash24Parameters();

    @Test
    public void testQuerySupportKnown() {
        assertEquals(EnumSet.of(HashSupport.STATEFUL, HashSupport.INT_SIZED),
                PROVIDER.querySupport(MURMUR3_32));
        assertEquals(EnumSet.of(HashSupport.STATEFUL), PROVIDER.querySupport(MURMUR3_128));
        assertEquals(EnumSet.of(HashSupport.STATEFUL, HashSupport.LONG_SIZED),
                PROVIDER.querySupport(SIP));
    }

    @Test
    public void testQuerySupportUnknown() {
        assertTrue(PROVIDER.querySupport(new MurmurHash3Parameters(MurmurHash3Variant.X86_128))
                .isEmpty());
    }

    @Test
    public void testCreateStateful() {
        assertEquals(MURMUR3_32.algorithm(), PROVIDER.createStateful(MURMUR3_32).algorithm());
        assertEquals(MURMUR3_128.algorithm(), PROVIDER.createStateful(MURMUR3_128).algorithm());
        assertEquals(SIP.algorithm(), PROVIDER.createStateful(SIP).algorithm());
    }

    @Test
    public void testDiscovery() {
        assertThat(HashProviders.search(SIP).values(), hasItem(isA(GuavaHashProvider.class)));
    }
}
