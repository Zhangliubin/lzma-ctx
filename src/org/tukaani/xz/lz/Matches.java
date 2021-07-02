/*
 * Matches
 *
 * Authors: Lasse Collin <lasse.collin@tukaani.org>
 *          Igor Pavlov <http://7-zip.org/>
 *
 * This file has been put into the public domain.
 * You can do whatever you want with this file.
 */

package org.tukaani.xz.lz;

public final class Matches {
    public final int[] len;
    public final int[] dist;
    public int count = 0;

    Matches(int countMax) {
        len = new int[countMax];
        dist = new int[countMax];
    }

    void reset(){
        count = 0;
        for (int i = 0 ; i < this.len.length; i++){
            len[i] = 0;
            dist[i] = 0;
        }
    }
}
