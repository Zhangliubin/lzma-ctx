package edu.sysu.pmglab.suranyi.container;

import java.io.IOException;
import java.io.InputStream;

/**
 * @Data        :2020/05/08
 * @Author      :suranyi
 * @Contact     :suranyi.sysu@gamil.com
 * @Description :包装字节数组为 OutputStream，方便操作数据缓冲区，并且减少内存使用量
 */

public class VolumeByteInputStream extends InputStream {
    private byte[] cache;
    private int seek;

    /**
     * 构造器方法
     */
    public VolumeByteInputStream() {
    }

    public VolumeByteInputStream(byte[] cache) {
        wrap(cache, 0);
    }

    public VolumeByteInputStream(byte[] cache, int seek) {
        wrap(cache, seek);
    }

    @Override
    public int read() throws IOException {
        return this.cache[this.seek++] & 0xFF;
    }

    public byte readByte() {
        return this.cache[this.seek++];
    }

    public int readUnsignedByte() {
        return this.cache[this.seek++] & 0xFF;
    }

    /**
     * 获取缓冲区段数据
     * @return 获取本定容容器缓冲区数据
     */
    public byte[] getCache() {
        return this.cache;
    }

    /**
     * 以另一个数组的数据替换本类数据
     * @param src 源数据
     */
    public void wrap(byte[] src) {
        this.cache = src;
        this.seek = (src == null) ? -1 : src.length;
    }

    /**
     * 以另一个数组的数据替换本类数据
     * @param src 源数据
     * @param length 该源数据有效数据长度
     */
    public void wrap(byte[] src, int length) {
        assert length >= 0;
        this.cache = src;
        this.seek = Math.min(length, src.length);
    }

    /**
     * 获取定容容器的总容量, -1 代表当前容器不可写入
     * @return 当前容器的容量
     */
    public int getCapital() {
        if (null == this.cache) {
            return -1;
        }
        return this.cache.length;
    }

    /**
     * 当前容器有效数据长度
     * @return 有效数据长度，可以通过 reset 修改
     */
    public int size() {
        return this.seek;
    }

    /**
     * 当前容器剩余容量
     * @return 剩余容量
     */
    public int remaining() {
        return this.cache.length - this.seek;
    }

    /**
     * 重设容器内部指针为 0
     */
    @Override
    public void reset() {
        this.seek = 0;
    }

    /**
     * 重设文件指针
     * @param position 新指针
     */
    public void reset(int position) {
        this.seek = position;
    }

    /**
     * 关闭文件
     */
    @Override
    public void close() {
        this.cache = null;
        this.seek = -1;
    }
}
