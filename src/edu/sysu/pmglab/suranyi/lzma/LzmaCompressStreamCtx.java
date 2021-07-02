package edu.sysu.pmglab.suranyi.lzma;

import edu.sysu.pmglab.suranyi.container.VolumeByteOutputStream;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.LZMAOutputStream;
import org.tukaani.xz.UnsupportedOptionsException;

import java.io.IOException;

/**
 * @Data        :2021/06/30
 * @Author      :suranyi
 * @Contact     :suranyi.sysu@gamil.com
 * @Description :转换接口 LZMA 解压缩上下文
 */

public class LzmaCompressStreamCtx {
    final VolumeByteOutputStream wrapper;
    final LZMA2Options options;

    /**
     * 构造器方法，LZMA 仅支持压缩级别在 0-9 之间
     * @param compressionLevel 压缩级别
     */
    public LzmaCompressStreamCtx(int compressionLevel) throws UnsupportedOptionsException {
        options = new LZMA2Options(compressionLevel);
        this.wrapper = new VolumeByteOutputStream();
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
        LZMAOutputStream outputStream = new LZMAOutputStream(this.wrapper, this.options, srcLength);
        outputStream.write(src, srcOffset, srcLength);
        outputStream.finish();
        try {
            return this.wrapper.size() - dstOffset;
        } finally {
            outputStream.close();
        }
    }
}
