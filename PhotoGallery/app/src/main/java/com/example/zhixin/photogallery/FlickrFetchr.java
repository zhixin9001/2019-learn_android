package com.example.zhixin.photogallery;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhiXin on 2019/1/26.
 */

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";

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

    public List<GalleryItem> downloadGalleryItems() {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String url = "http://image.baidu.com/channel/listjson?pn=0&rn=25&tag1=明星&ie=utf8";
            String jsonString = getUrlString(url);
            Log.i(TAG, "received1=" + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch URL: ", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON: ", je);
        }
        return items;
    }

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
}
