学习安卓开发[5] - HTTP、后台任务以及与UI线程的交互

在上一篇*学习安卓开发[4] - 使用隐式Intent启动短信、联系人、相机应用*中了解了在调用其它应用的功能时隐式Intent的使用，本次基于一个图片浏览APP的开发，记录使用AsyncTask在后台执行HTTP任务以获取图片URL，然后使用HandlerThread动态下载图片和显示图片

- HTTP
    - 请求数据
    - 解析Json数据
- AsyncTask
    - 主线程与后台线程
    - 后台线程的启动与结果返回
- HandlerThread
    - s 

#### HTTP
##### 请求数据
这里使用java.net.HttpURLConnection来执行HTTP请求，GET请求的基本用法如下，默认执行的就是GET，所以可以省略connection.setRequestMethod("GET")，connection.getInputStream()取得InputStream后，再循环执行read()方法将数据从流中取出、写入ByteArrayOutputStream中，然后通过ByteArrayOutputStream.toByteArray返回为Byte数组格式，最后转换为String。网上还有一种方法是使用BufferedReader.readLine()来逐行读取输入缓冲区的数据并写入StringBuilder。对于POST方法，可以使用getOutputStream()来写入参数。

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
url为百度的图片接口，返回json格式数据，所以将API返回的json字符串转换为JSONObject，然后遍历json数组，将其转换为指定的对象。

```
    ...
    String url = "http://image.baidu.com/channel/listjson?pn=0&rn=25&tag1=明星&ie=utf8";
    String jsonString = getUrlString(url);
    JSONObject jsonBody = new JSONObject(jsonString);
    parseItems(items, jsonBody);
    ...


private void parseItems(List<GalleryItem> items, JSONObject jsonObject) throws IOException, JSONException {
    JSONArray photoJsonArray = jsonObject.getJSONArray("data");
    for (int i = 0; i < photoJsonArray.length() - 1; i++) {
        JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
        if (!photoJsonObject.has("id")) {
            continue;
        }
        GalleryItem item = new GalleryItem();
        item.setId(photoJsonObject.getString("id"));
        item.setCaption(photoJsonObject.getString("desc"));
        item.setUrl(photoJsonObject.getString("image_url"));

        items.add(item);
    }
}
```
#### AsyncTask
##### 主线程与后台线程
HTTP相关的代码准备好了，但无法在Fragment类中被直接调用。因为网络操作通常比较耗时，如果在主线程(UI线程)中直接操作，会导致界面无响应的现象发生。所以Android系统禁止任何主线程的网络连接行为，否则会报NewworkOnMainThreadException。
主线程不同于普通的线程，后者在完成预定的任务后便会终止，但主线程则处于无限循环的状态，以等待用户或系统的触发事件。

##### 后台线程的启动与结果返回
至于网络操作，正确的做法是创建一个后台线程，在这个线程中进行。AsyncTask提供了使用后台线程的简便方法。代码如下：
```
private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
    @Override
    protected List<GalleryItem> doInBackground(Void... voids) {
        List<GalleryItem> items = new FlickrFetchr().fetchItems();
        return items;
    }

    @Override
    protected void onPostExecute(List<GalleryItem> galleryItems) {
        mItems = galleryItems;
        setupAdapter();
    }
}
```
重写了AsyncTask的doInBackground方法和onPostExecute方法，另外还有两个方法可重写，它们的作用分别是：
- onPreExecute(), 在后台操作开始前被UI线程调用。可以在该方法中做一些准备工作，如在界面上显示一个进度条，或者一些控件的实例化，这个方法可以不用实现。
- doInBackground(Params...), 将在onPreExecute 方法执行后马上执行，该方法运行在后台线程中。这里将主要负责执行那些很耗时的后台处理工作。可以调用 publishProgress方法来更新实时的任务进度。该方法是抽象方法，子类必须实现。
- onProgressUpdate(Progress...),在publishProgress方法被调用后，UI 线程将调用这个方法从而在界面上展示任务的进展情况，例如通过一个进度条进行展示。
- onPostExecute(Result), 在doInBackground 执行完成后，onPostExecute 方法将被UI 线程调用，后台的计算结果将通过该方法传递到UI 线程，并且在界面上展示给用户
- onCancelled(),在用户取消线程操作的时候调用。在主线程中调用onCancelled()的时候调用

AsyncTask的三个泛型参数就是对应doInBackground(Params...)、onProgressUpdate(Progress...)、onPostExecute(Result)的，这里设置为
```
AsyncTask<Void, Void, List<GalleryItem>>
```
所以线程完成后返回的结果类型为List<GalleryItem>。
后台线程的启动可以在Fragment创建的时候执行：
```
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    ...
    new FetchItemsTask().execute();
}
```























