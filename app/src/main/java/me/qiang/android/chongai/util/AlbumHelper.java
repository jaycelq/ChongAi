package me.qiang.android.chongai.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.R;

public class AlbumHelper {
    private static String ALLIMAGE = Constants.ALLIMAGE;
    HashMap<String, List<String>> albumMap = new HashMap<String, List<String>>();
    List<AlbumItem> albumItemList = new ArrayList<>();
    ContentResolver mContentResolver;
    Context context;
    private static AlbumHelper instance;
    private AlbumHelper() {
        List<String> imageList = new ArrayList<String>();
        albumMap.put(ALLIMAGE, imageList);
    }

    public static AlbumHelper getHelper() {
        if (instance == null) {
            instance = new AlbumHelper();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public List<String> getImageList(String folder) {
        if (albumMap.size() == 1)
            GetImages();
        return albumMap.get(folder);
    }

    public void reloadImages() {
        Iterator iter = albumMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            List<String> val = (List<String>) entry.getValue();
            val.clear();
        }
        albumMap.clear();
        albumItemList.clear();
        List<String> imageList = new ArrayList<String>();
        albumMap.put(ALLIMAGE, imageList);
        GetImages();
    }

    public List<AlbumItem> getAlbumItemList() {
        if (albumMap.size() == 1)
            GetImages();
        return albumItemList;
    }

    public void GetImages() {
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        mContentResolver = context.getContentResolver();
        //只查询jpeg和png的图片
        Cursor mCursor = mContentResolver.query(mImageUri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[] { "image/jpeg", "image/png" }, MediaStore.Images.Media.DATE_MODIFIED);

        if(mCursor == null){
            return;
        }

        while (mCursor.moveToNext()) {
            //获取图片的路径
            String path = mCursor.getString(mCursor
                    .getColumnIndex(MediaStore.Images.Media.DATA));
            path = "file://" + path;

            String parentPath = new File(path).getParentFile().getName();
            //根据父路径名将图片放入到mGruopMap中
            if (!albumMap.containsKey(parentPath)) {
                List<String> imageList = new ArrayList<String>();
                imageList.add(path);
                albumMap.put(parentPath, imageList);
            } else {
                albumMap.get(parentPath).add(path);
            }
            albumMap.get(ALLIMAGE).add(path);
            Log.i("ALLIMAGE",path);
        }
        mCursor.close();
        Log.i("ALLIMAGE",""+ albumMap.get(ALLIMAGE).size());
        albumItemList = subGroupOfImage(albumMap);
    }

    private List<AlbumItem> subGroupOfImage(HashMap<String, List<String>> mGruopMap){
        if(mGruopMap.size() == 0){
            return null;
        }
        List<AlbumItem> list = new ArrayList<AlbumItem>();

        Iterator<Map.Entry<String, List<String>>> it = mGruopMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            String key = entry.getKey();
            List<String> value = entry.getValue();
            Collections.reverse(value);
            AlbumItem mAlbumItem = new AlbumItem();
            mAlbumItem.setFolderName(key);
            mAlbumItem.setImageCounts(value.size());
            if(value.size() > 0)
                mAlbumItem.setTopImagePath(value.get(0));//获取该组的第一张图片
            else
                mAlbumItem.setTopImagePath("drawable://" + R.drawable.drawer_background);
            if (key.equals(ALLIMAGE))
                list.add(0, mAlbumItem);
            else
                list.add(mAlbumItem);
        }
        Collections.sort(list);
        return list;
    }
}
