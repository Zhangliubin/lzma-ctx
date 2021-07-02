package edu.sysu.pmglab.suranyi.lzma;

import edu.sysu.pmglab.suranyi.container.VolumeByteInputStream;
import org.tukaani.xz.LZMAInputStream;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @Data        :2021/06/30
 * @Author      :suranyi
 * @Contact     :suranyi.sysu@gamil.com
 * @Description :转换接口 LZMA 压缩上下文
 */

public class LzmaDecompressStreamCtx {
    final VolumeByteInputStream wrapper;

    /**
     * 构造器方法，LZMA 仅支持压缩级别在 0-9 之间
     */
    public LzmaDecompressStreamCtx() {
        this.wrapper = new VolumeByteInputStream();
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
    public int decompress(byte[] src, int srcOffset, int srcLength, byte[] dst, int dstOffset) throws IOException {
        // 将 dst 包装为 outputStream 对象
        this.wrapper.wrap(src, srcOffset);
        try (LZMAInputStream outputStream = new LZMAInputStream(this.wrapper)) {
            return outputStream.read(dst, dstOffset, dst.length);
        }
    }

    public static void main(String[] args) throws IOException {
        // 创建测试数据
        BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream("./src/edu/sysu/pmglab/suranyi/lzma/LzmaDecompressStreamCtx.java"));
        byte[] inputs = new byte[fileInputStream.available()];
        byte[] outputs = new byte[fileInputStream.available()];
        fileInputStream.read(inputs);
        fileInputStream.close();

        // 压缩
        LzmaCompressStreamCtx compressStreamCtx = new LzmaCompressStreamCtx(3);
        int inputsCompressedLength = compressStreamCtx.compress(inputs, 0, inputs.length, outputs, 0);

        // 解压
        LzmaDecompressStreamCtx decompressStreamCtx = new LzmaDecompressStreamCtx();
        int inputsUncompressedLength = decompressStreamCtx.decompress(outputs, 0, inputsCompressedLength, inputs, 0);
        System.out.println(new String(Arrays.copyOfRange(inputs, 0, inputsUncompressedLength)));
    }
}
