package com.scurrilous.circe.impl;

import java.nio.ByteBuffer;

import sun.nio.ch.DirectBuffer;

/**
 * Implementation of {@link DirectByteBufferAccess} that attempts to use
 * {@code sun.nio.ch.DirectBuffer} to obtain the direct byte buffer address.
 */
@SuppressWarnings("restriction")
public final class DirectBufferAccess implements DirectByteBufferAccess {

    @Override
    public long getAddress(ByteBuffer buffer) {
        return buffer instanceof DirectBuffer ? ((DirectBuffer) buffer).address() : 0;
    }
}
