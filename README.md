## 背景

目前流行的 [XZ Utils](https://tukaani.org/xz/) 是基于流式设计，它能十分友好地处理文件流数据的压缩（即未知文件大小的情形，通过不断的 put/write 方法输入数据进行压缩）。但某些时候，由于各种原因，我们需要对数据进行分块（例如，不同的 chromosome、不同的基因区段矩阵、不同组织表达矩阵等），面对的数据经常是一个数据大小已知、频繁调用压缩的场景。字典类压缩算法使用时需要创建压缩字典表（LZMA 最大可达到 4 GB）及编码器，这会对内存造成巨大负担，也制约了普通用户使用这些工具的场景。因此，设计一种能够复用上下文结构的 LZMA 算法对节省内存开销具有重要意义。此处提供两种实现方式：基于 LZMA-SDK 转换接口实现（仅方便使用），复用上下文结构 LZMA。

## 流式转换接口实现

首先，实现两个数据包装类：VolumeByteInputStream、VolumeByteOutputStream。它们的设计思想与 ByteArrayInputStream 和 ByteArrayOutputStream 类似，区别在于由于我们包装的数据长度是已知的，可以减少不必要的容量判断。此外，我们也提供 cache、seek 对外访问的方法，避免如同 ByteArrayOutputStream 在提取数据时需要使用 .toByteArray() 拷贝数据：

```java
public synchronized byte toByteArray()[] {
    return Arrays.copyOf(buf, count);
}
```

测试：

```java
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
```

转换接口使得我们不必再书写大段重复的 new、close、write 代码，而可以直接调用 ctx.compress 和 ctx.decompress 进行压缩、解压工作。类似的转换也可以应用在 GZIP 等其他流式压缩实现上。

## 复用上下文结构 LZMA

复用上下文结构指每次压缩后（或压缩前）刷新数据标记，而不销毁数据容器，从而实现容器复用。在超大规模数据处理（如数千万人基因组阵列）中具有明显性能优势（内存与时间）。

对于 LzmaCompressCtx，主要将 VolumeByteOutputStream 的构造器拆分在 LzmaCompressCtx 的构造器中，而编码器则在每一次使用后进行 reset。

对于 LzmaDecompressCtx，则主要判断是否发生了必要的“解码器重建”（即不同的压缩字典规模、不同的参数等），解码器器在每一次使用后进行 reset。此处注意到压缩字典大小是个频繁变化的参数（该值取决于原文件大小、压缩时的 compressionLevel 设置），因此，我们修改了 LZDecoder 的 reset 方法，仅当必要时发生缓冲区扩容。一旦遇到一个最大的字典大小，则之后任何的数据解压都建立在该字典数据区上，实现一次 new 多次使用。

```java
public void reset(int newBufferSize) {
    start = 0;
    pos = 0;
    full = 0;
    limit = 0;
    pendingLen = 0;
    pendingDist = 0;
    if (newBufferSize > buf.length) {
        buf = arrayCache.getByteArray(newBufferSize, false);
    }

    bufSize = newBufferSize;
    buf[bufSize - 1] = 0x00;
}
```

测试：

```java
public static void main(String[] args) throws IOException {
    // 创建测试数据
    BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream("./src/edu/sysu/pmglab/suranyi/lzma/LzmaDecompressStreamCtx.java"));
    byte[] inputs = new byte[fileInputStream.available()];
    byte[] outputs = new byte[fileInputStream.available()];
    fileInputStream.read(inputs);
    fileInputStream.close();

    // 压缩
    LzmaCompressCtx compressCtx = new LzmaCompressCtx(3);
    int inputsCompressedLength = compressCtx.compress(inputs, 0, inputs.length, outputs, 0);

    // 解压
    LzmaDecompressCtx decompressCtx = new LzmaDecompressCtx();
    int inputsUncompressedLength = decompressCtx.decompress(outputs, 0, inputsCompressedLength, inputs, 0);
    System.out.println(new String(Arrays.copyOfRange(inputs, 0, inputsUncompressedLength)));
}
```

### 

