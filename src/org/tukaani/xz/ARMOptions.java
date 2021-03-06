/*
 * ARMOptions
 *
 * Author: Lasse Collin <lasse.collin@tukaani.org>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz;

import org.tukaani.xz.simple.ARM;

import java.io.InputStream;

/**
 * BCJ filter for little endian ARM instructions.
 */
public class ARMOptions extends BCJOptions {
    private static final int ALIGNMENT = 4;

    public ARMOptions() {
        super(ALIGNMENT);
    }

    @Override
    public FinishableOutputStream getOutputStream(FinishableOutputStream out,
                                                  ArrayCache arrayCache) {
        return new SimpleOutputStream(out, new ARM(true, startOffset));
    }

    @Override
    public InputStream getInputStream(InputStream in, ArrayCache arrayCache) {
        return new SimpleInputStream(in, new ARM(false, startOffset));
    }

    @Override
    FilterEncoder getFilterEncoder() {
        return new BCJEncoder(this, BCJCoder.ARM_FILTER_ID);
    }
}
