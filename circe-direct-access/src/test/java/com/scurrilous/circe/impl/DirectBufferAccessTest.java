package com.scurrilous.circe.impl;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class DirectBufferAccessTest {

    @Test
    public void testGetAddress() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16);
        final long address = new DirectBufferAccess().getAddress(buffer);
        assertTrue(address != 0);
    }

    @Test
    public void testLoader() {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(16);
        final long address = DirectByteBufferAccessLoader.getAddress(buffer);
        assertTrue(address != 0);
    }
}
