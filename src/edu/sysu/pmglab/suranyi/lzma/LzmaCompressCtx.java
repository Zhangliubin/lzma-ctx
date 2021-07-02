package edu.sysu.pmglab.suranyi.lzma;

import edu.sysu.pmglab.suranyi.container.VolumeByteOutputStream;
import org.tukaani.xz.ArrayCache;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.UnsupportedOptionsException;
import org.tukaani.xz.lz.LZEncoder;
import org.tukaani.xz.lzma.LZMAEncoder;
import org.tukaani.xz.rangecoder.RangeEncoderToStream;

import java.io.IOException;

/**
 * @Data        :2021/06/30
 * @Author      :suranyi
 * @Contact     :suranyi.sysu@gamil.com
 * @Description :LZMA 压缩上下文，复用数据区
 */

public class LzmaCompressCtx {
    final VolumeByteOutputStream wrapper;
    final RangeEncoderToStream rc;
    final int props;
    final byte[] dictSizeByteArray;
    final LZEncoder lz;
    final LZMAEncoder lzma;

    /**
     * 构造器方法，LZMA 仅支持压缩级别在 0-9 之间
     * @param compressionLevel 压缩级别
     */
    public LzmaCompressCtx(int compressionLevel) throws UnsupportedOptionsException {
        LZMA2Options options = new LZMA2Options(compressionLevel);
        this.wrapper = new VolumeByteOutputStream();
        this.rc = new RangeEncoderToStream(this.wrapper);
        this.props = (options.getPb() * 5 + options.getLp()) * 9 + options.getLc();
        this.dictSizeByteArray = new byte[4];
        int dictSize = options.getDictSize();

        for (int i = 0; i < 4; ++i) {
            this.dictSizeByteArray[i] = (byte) (dictSize & 0xFF);
            dictSize >>>= 8;
        }

        this.lzma = LZMAEncoder.getInstance(rc, options, ArrayCache.getDefaultCache());
        this.lz = this.lzma.getLZEncoder();
    }

    /**
     * 压缩方法
     * @param src 原数据
     * @param srcOffset 源数据偏移量
     * @param srcLength 源数据有效长度
     * @param dst 目标数据容器
     * @param dstOffset 目标数据容器偏移量
     * @return 实际写入长度
     */
    public int compress(byte[] src, int srcOffset, int srcLength, byte[] dst, int dstOffset) throws IOException {
        // 将 dst 包装为 outputStream 对象
        this.wrapper.wrap(dst, dstOffset);
        this.wrapper.write(this.props);
        this.wrapper.write(this.dictSizeByteArray);

        // 64 bit
        this.wrapper.write(srcLength & 0xFF);
        this.wrapper.write((srcLength >> 8) & 0xFF);
        this.wrapper.write((srcLength >> 16) & 0xFF);
        this.wrapper.write((srcLength >> 24) & 0xFF);
        this.wrapper.write(0);
        this.wrapper.write(0);
        this.wrapper.write(0);
        this.wrapper.write(0);

        // 压缩数据
        while (srcLength > 0) {
            int used = this.lz.fillWindow(src, srcOffset, srcLength);
            srcOffset += used;
            srcLength -= used;
            lzma.encodeForLZMA1();
        }

        // 关闭流
        this.lz.setFinishing();
        this.lzma.encodeForLZMA1();
        this.rc.finish();
        this.rc.reset();
        this.lzma.reset();

        return this.wrapper.size() - dstOffset;
    }
}
