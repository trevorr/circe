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
package com.scurrilous.circe;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.nio.charset.Charset;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class CommonHashesTest {

    private static final Charset ASCII = Charset.forName("ASCII");
    private static final byte[] DIGITS = "123456789".getBytes(ASCII);

    private static byte[] hashBytes(StatefulHash h) {
        h.reset();
        h.update(DIGITS);
        return h.getBytes();
    }

    private static void assertHexEquals(String expected, byte[] actual) {
        assertEquals(expected, new BigInteger(1, actual).toString(16));
    }

    @Test
    public void testCrc32() {
        assertEquals(0xcbf43926, CommonHashes.crc32().calculate(DIGITS));
    }

    @Test
    public void testCrc32c() {
        assertEquals(0xe3069283, CommonHashes.crc32c().calculate(DIGITS));
    }

    @Test
    public void testCrc64() {
        assertEquals(0x6c40df5f0b497347L, CommonHashes.crc64().calculate(DIGITS));
    }

    @Test
    public void testMurmur3_32() {
        assertEquals(0xb4fef382, CommonHashes.murmur3_32().calculate(DIGITS));
    }

    @Test
    public void testMurmur3_32Int() {
        assertEquals(0xa3191598, CommonHashes.murmur3_32(0x1234).calculate(DIGITS));
    }

    @Test
    public void testMurmur3_128() {
        assertHexEquals("a4cc66db5e64843c05a11e3ac7faf899", hashBytes(CommonHashes.murmur3_128()));
    }

    @Test
    public void testMurmur3_128Int() {
        assertHexEquals("310ce6099d668915d9b61e70c6a3c503",
                hashBytes(CommonHashes.murmur3_128(0x1234)));
    }

    @Test
    public void testSipHash24() {
        assertEquals(0xca60fc96020efefdL, CommonHashes.sipHash24().calculate(DIGITS));
    }

    @Test
    public void testSipHash24LongLong() {
        assertEquals(0xb0bcde4064d8a799L,
                CommonHashes.sipHash24(0x0123456780ABCDEFL, 0xFEDCBA9876543210L).calculate(DIGITS));
    }

    @Test
    public void testMd5() {
        assertHexEquals("25f9e794323b453885f5181f1b624d0b", hashBytes(CommonHashes.md5()));
    }

    @Test
    public void testSha1() {
        assertHexEquals("f7c3bc1d808e04732adf679965ccc34ca7ae3441", hashBytes(CommonHashes.sha1()));
    }

    @Test
    public void testSha256() {
        assertHexEquals("15e2b0d3c33891ebb0f1ef609ec419420c20e320ce94c65fbc8c3312448eb225",
                hashBytes(CommonHashes.sha256()));
    }

    @Test
    public void testSha384() {
        assertHexEquals(
                "eb455d56d2c1a69de64e832011f3393d45f3fa31d6842f21af92d2fe469c499da5e3179847334a18479c8d1dedea1be3",
                hashBytes(CommonHashes.sha384()));
    }

    @Test
    public void testSha512() {
        assertHexEquals(
                "d9e6762dd1c8eaf6d61b3c6192fc408d4d6d5f1176d0c29169bc24e71c3f274ad27fcd5811b313d681f7e55ec02d73d499c95455b6b5bb503acf574fba8ffe85",
                hashBytes(CommonHashes.sha512()));
    }
}
