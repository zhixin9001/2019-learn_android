学习安卓开发[5] - HTTP、后台任务以及与UI线程的交互

在上一篇*学习安卓开发[4] - 使用隐式Intent启动短信、联系人、相机应用*中了解了在调用其它应用的功能时隐式Intent的使用，本次基于一个图片浏览APP的开发，记录使用AsyncTask在后台执行HTTP任务以获取图片URL，然后使用HandlerThread动态下载图片和显示图片

- HTTP
    - 请求数据
    - 解析Json数据
- AsyncTask
    - 开始后台线程
    - 将结果返回给UI线程
- HandlerThread
    - AsyncTask不适用于批量下载图片

#### HTTP
##### 请求数据
这里使用java.net.HttpURLConnection来执行HTTP请求，GET请求的基本用法如下，默认执行的就是GET，所以可以省略connection.setRequestMethod("GET")，connection.getInputStream()取得InputStream后，再循环执行read()方法将数据从流中取出、写入ByteArrayOutputStream中，然后通过ByteArrayOutputStream.toByteArray返回为Byte数组格式，最后转换为String。网上还有一种方法是使用BufferedReader.readLine()来逐行读取输入缓冲区的数据并写入StringBuilder，

```
public byte[] getUrlBytes(String urlSpec) throws IOException {
    URL url = new URL(urlSpec);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = connection.getInputStream();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(connection.getResponseMessage() +
                    "with" + urlSpec);
        }

        int bytesRead = 0;
        byte[] buffer = new byte[1024];
        while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
        }
        out.close();
        return out.toByteArray();
    } finally {
        connection.disconnect();
    }
}

public String getUrlString(String urlSpec) throws IOException {
    return new String(getUrlBytes(urlSpec));
}
```

##### 解析Json数据



#### HandlerThread
##### AsyncTask不适用于批量下载图片
前面通过AsyncTask创建的后台线程获取到了所有图片的URL信息，接下来需要下载这些图片并显示到RecyclerView。但如果要在doInBackGround中直接下载这些图片则是不合理的，这是因为：
- 图片下载比较耗时，如果要下载的图片较多，需要等这些图片都下载成功后才去更新UI，体验很差。
- 下载的图片还涉及到保存的问题，数量较大的图片不宜直接存放在内存，而且如果要实现无限滚动来显示图片，内存很快就会耗尽
所以对于类似这种重复且数量较大、耗时较长的任务来说，AsyncView便不再适合了。






