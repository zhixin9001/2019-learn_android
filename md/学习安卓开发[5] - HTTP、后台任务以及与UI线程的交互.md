学习安卓开发[5] - HTTP、后台任务以及与UI线程的交互

在上一篇*学习安卓开发[4] - 使用隐式Intent启动短信、联系人、相机应用*中了解了在调用其它应用的功能时隐式Intent的使用，本次基于一个图片浏览APP的开发，记录使用AsyncTask在后台执行HTTP任务以获取图片URL，然后使用HandlerThread动态下载和显示图片

- HTTP
    - 请求数据
    - 解析Json数据
- AsyncTask
    - 主线程与后台线程
    - 后台线程的启动与结果返回
- HandlerThread
    - AsyncTask不适用于批量下载图片
    - ThreadHandler的启动和注销
    - 创建并发送消息
    - 处理消息并返回结果

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
#### HandlerThread
##### AsyncTask不适用于批量下载图片
前面通过AsyncTask创建的后台线程获取到了所有图片的URL信息，接下来需要下载这些图片并显示到RecyclerView。但如果要在doInBackGround中直接下载这些图片则是不合理的，这是因为：
- 图片下载比较耗时，如果要下载的图片较多，需要等这些图片都下载成功后才去更新UI，体验很差。
- 下载的图片还涉及到保存的问题，数量较大的图片不宜直接存放在内存，而且如果要实现无限滚动来显示图片，内存很快就会耗尽
所以对于类似这种重复且数量较大、耗时较长的任务来说，AsyncView便不再适合了。
换一种实现方式，既然用RecyclerView显示图片，在加载每个Holder时，单独下载对应的图片，这样便不会存在前面的问题了，于是该是HandlerThread登场的时候了，HandlerThread使用消息队列工作，这种使用消息队列的线程也叫消息循环，消息队列由线程和looper组成，looper对象管理着线程的消息队列，会循环检查队列上是否有新消息。
创建继承了HandlerThread的ThumbnailDownloader：
```
public class ThumbnailDownloader<T> extends HandlerThread
```
这里T设置为之后ThumbnailDownloader的使用者，即PhotoHolder。

##### ThreadHandler的启动和注销
在Fragment创建时启动线程：
```
@Override
public void onCreate(@Nullable Bundle savedInstanceState) {
    ...
    mThumbnailDownloader.start();
    mThumbnailDownloader.getLooper();
    ...
}
```
在Fragment销毁时终止线程：
```

@Override
public void onDestroy() {
    super.onDestroy();
    mThumbnailDownloader.quit();
}
```
这一步是必要的，否则即使Fragment已被销毁，线程也会一直运行下去。

##### 创建并发送消息
先了解一下Message和Handler
###### Message
给消息队列发送的就是Message类的实例，Message类用户需要定义这几个变量：
- what, 用户自定义的int型消息标识代码
- obj，随消息发送的对象
- target, 处理消息的handler
target是一个handler类实例，创建的message会自动与一个Handler关联，message待处理时，handler对象负责触发消息事件
###### Handler
handler是处理message的target，也是创建和发布message的接口。而looper拥有message对象的收件箱，所以handler总是引用着looper，在looper上发布或处理消息。handler与looper为多对一关系；looper拥有整个message队列，为一对多关系；多个message可引用同一个handler，为多对一关系。
###### 使用Handler
调用Handler.obtainMessage方法创建消息，而不是手动创建，obtainMessage会从公共回收池中获取消息，这样做可以避免反复创建新的message对象，更加高效。获取到message，随后调用sendToTarget()将其发送给它的handler，handler会将这个message放置在looper消息队列的尾部。这些操作在queueThumbnail中完成：
```
public void queueThumbnail(T target, String url) {
    Log.i(TAG, "Got a URL: " + url);
    if (url == null) {
        mRequestMap.remove(target);
    } else {
        mRequestMap.put(target, url);
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                .sendToTarget();
    }
}
```
然后在RecyclerView的Adapter绑定holder的时候，调用queueThumbnail，将图片url发送给后台线程。
```
public class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
    ...
    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        ...
        mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
    }
```
但后台线程的消息队列存放的不是url，而是对应的Holder，url存放在ConcurrentMap型的mRequestMap中，ConcurrentMap是一种线程安全的Map结构。存放了holder对对应url的map关系，这样在消息队列中处理某个holder时，可以从mRequestMap拿到它的url。
```
private ConcurrentMap<T, String> mRequestMap
```
##### 处理消息并返回结果
###### 消息的处理
具体处理消息的动作通过重写Handler.handleMessage方法实现。onLooperPrepared在Looper首次检查消息队列之前调用，所以在此可以实例化handler并重写handleMessage。下载图片的实现在handleRequest方法中，将请求API拿到的byte[]数据转换成bitmap。
```
public class ThumbnailDownloader<T> extends HandlerThread {
    ...
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Get a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }


    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url||mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownload(target,bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
```
###### 结果的返回
下载得到的Bitmap需要返回给UI线程的holder以显示到屏幕。如何做呢？UI线程也是一个拥有handler和looper的消息循环。所以要返回结果给UI线程，就可以反过来，从后台线程使用主线程的handler。
那么，后台线程首先需要持有UI线程的handler:
```
public class PhotoGalleryFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ...
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        ...
    }
```
ThumbnailDownloader的构造函数中接收UI线程的handler。图片下载完成后就要向UI线程发布message了，可以通过Handler.post(Runnable)进行，重写Runable.run()方法，不让halder处理消息，而是在这里触发ThumbnailDownloadListener。
```
public class ThumbnailDownloader<T> extends HandlerThread {
    ...
    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownload(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener=listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler=responseHandler;
    }
    private void handleRequest(final T target) {
        ...
        mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target)!=url||mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownload(target,bitmap);
                }
            });
        ...
    }
}
```
mThumbnailDownloadListener被触发后，UI线程的注册方法就会将后台返回的图片绑定到其Holder。
```
public class PhotoGalleryFragment extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        ...
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownload(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                }
        );
        ...
    }
```

如此，后台任务的执行与返回就完成了。
