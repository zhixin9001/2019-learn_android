学习安卓开发[5] - HTTP、后台任务以及与UI线程的交互

在上一篇*学习安卓开发[4] - 使用隐式Intent启动短信、联系人、相机应用*中了解了在调用其它应用的功能时隐式Intent的使用，本次基于一个图片浏览APP的开发，记录使用AsyncTask在后台执行HTTP任务以获取图片URL，然后使用HandlerThread动态下载图片和显示图片的方式

- 隐式Intent
- 短信
    - 判断是否存在相关APP
- 相机
    - FileProvider
    - Bitmap
    - 功能声明

#### 隐式Intent
Intent对象用来向操作系统说明需要处理的任务。使用显式Intent时，要指定操作系统需要启动的activity，但使用隐式intent，只需告知操作系统想要进行的操作，系统就会启动能完成该操作的activity，如果有多个符合条件的activity，会提供用户一个应用列表供选择
Android是如何通过隐式intent找到并启动合适应用的呢？原因在于配置文件中的itent过滤器设置，比如我们也想开发一款短信应用，那么可以在AndroidMainfest的activity声明中这样设置：
```
<activity android:name=".CrimeListActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

隐式Intent的组成部分有
1)要执行的操作，通常以Intent类中的常量来表示，比如访问URL可以使用Intent.ACTION_VIEW，发送邮件使用Intent.ACTION_SEND
2)待访问数据的位置，这可能是设备以外的资源，如某个网页的URL，某个文件的URI
3)操作涉及的数据类型，如text/html, audio/mpeg3等
4)可选类别，用来描述对activity的使用方式

#### 短信
那么要启动短信的隐式intent的方法为：
```
mReportButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
        i.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.crime_report_suspect));
        i = Intent.createChooser(i, getString(R.string.send_report));
        startActivity(i);
    }
});
```
首先指定发送消息的操作名为ACTION_SEND,然后消息内容为文本，所以设置数据类型为text/plain，要发送的文本通过Extra的形式提供

##### 判断是否存在相关APP
使用隐式intent时，如果系统没有安装对应的软件，应用就会奔溃，所以有必要在使用隐式intent时，检查一下能够找到对应的软件，如果没找到，就避免再去发生相关的隐式intent

```
final Intent pickContact = new Intent(Intent.ACTION_SEND);
PackageManager packageManager = getActivity().getPackageManager();
    if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
        mReportButton.setEnabled(false);
}
```
通过PackageManager可以搜索需要的activity的信息，flag标志MATCH_DEFAULT_ONLY限定只搜索带CATEGORY_DEFAULT的activity，如果没有找到，就禁用发短信按钮。

#### 相机
如果所开发的APP有拍照功能，就可以使用系统相机了。拍摄的照片要保存在设备文件系统，但这就涉及到私有存储空间的问题。出于安全考虑，无法使用公共外部存储转存，那么如果想共享文件给其他应用，或者接收其他应用的文件（如相机拍摄的照片），可以使用ContentProvider把要共享的文件临时暴露出来。对于接受相机拍摄的照片这样的场景，系统提供的现成的FileProvider类。

##### FileProvider
要使用FileProvider类，需要在AndroidMainfest中添加声明。
首先添加files.xml文件
```
<paths>
    <files-path
        name="crime_photos"
        path="."/>
</paths>
```
这个描述性文件把私有存储空间的根路径映射为crime_photos，这个名字仅供FileProvider自己使用。
然后添加FileProvider声明：
```
<provider
    android:name="android.support.v4.content.FileProvider"
    android:authorities="com.example.zhixin.crimeintent.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/files" />
</provider>
```
通过这段声明，提供了一个文件保存地，相机拍摄的照片就可以放在这里了。exported="false"表示除了应用自己和给予授权的应用，其它的不允许使用这个FileProvider，grantUriPermissions="true"表示允许其他应用向指定文职的URI写入文件。

接下来就可以实现拍照功能了
```
mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
boolean canTakePhoto = mPhotoFile != null &&
        captureImage.resolveActivity(packageManager) != null;
mPhotoButton.setEnabled(canTakePhoto);

mPhotoButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Uri uri = FileProvider.getUriForFile(getActivity(),
                "com.example.zhixin.crimeintent.fileprovider", mPhotoFile);
        captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage,
                PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo activity : cameraActivities) {
            getActivity().grantUriPermission(activity.activityInfo.packageName,
                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        startActivityForResult(captureImage,REQUEST_PHOTO);
    }
});

mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
```
通过给所有目标activity授予Intent.FLAG_GRANT_WRITE_URI_PERMISSION权限，允许它们在URI指定的位置写入文件。mPhotoFile表示拍摄生成照片的名称。

在相机拍摄完成后的回调方法中，取消之前的Intent.FLAG_GRANT_WRITE_URI_PERMISSION授权，并加载显示照片。
```
Uri uri=FileProvider.getUriForFile(getActivity(),
        "com.example.zhixin.crimeintent.fileprovider",
        mPhotoFile);
getActivity().revokeUriPermission(uri,Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
updatePhotoView();
```

##### Bitmap
在显示照片时还有一些工作要做。显示照片要用到Bitmap，而Bitmap只存储实际像素数据，即使是已经压缩过的照片，存入Bitmap后，文件并不会同样压缩，比如一张1600万像素24位的相机照片存为JPG格式约为5MB，但载入Bitmap后就会达到48MB左右。
要解决这个问题，需要手动缩放位图照片。首先确认文件大小，然后根据要显示照片的区域大小合理缩放文件，最后重新读取缩放后的文件，再创建Bitmap对象。
```
public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // Figure out how much to scale down by
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale :
                    widthScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }
}
```
还有一个问题是在Fragment.OnCreateView里面加载照片的时候，无法知道要显示照片的尺寸，只有onCreate, onStart, onResume方法执行过后，才会有首个实例化布局出现。对于这种情况，可以根据Fragment所在的Activity尺寸确定屏幕的尺寸，按照屏幕尺寸缩放图像。所以再添加一个getScaledBitmap的重载：
```
public static Bitmap getScaledBitmap(String path, Activity activity) {
    Point size = new Point();
    activity.getWindowManager().getDefaultDisplay()
            .getSize(size);
    return getScaledBitmap(path, size.x, size.y);
}
```
最后在OnCreateView和相机的回调方法更新照片。

##### 功能声明
既然APP需要用到拍照功能，但像拍照、NFC、红外等并不是每个设备都有，所以进行功能声明，从而可以在应用前让用户知道，如果设备缺少某项必须功能，应用商店会拒绝安装应用。
在AndroidMainfest中添加：
```
<uses-feature
    android:name="android.hardware.camera"
    android:required="false">
</uses-feature>
```
android:required="false"表示不强制拍照功能，因为如果设备没有相机，会禁掉拍照按钮。






