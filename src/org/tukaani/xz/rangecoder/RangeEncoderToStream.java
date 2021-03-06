/*
 * RangeEncoderToStream
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.rangecoder;

import java.io.IOException;
import java.io.OutputStream;

public final class RangeEncoderToStream extends RangeEncoder {
    private final OutputStream out;

    public RangeEncoderToStream(OutputStream out) {
        this.out = out;
        reset();
    }

    @Override
    public void writeByte(int b) throws IOException {
        out.write(b);
    }
}
