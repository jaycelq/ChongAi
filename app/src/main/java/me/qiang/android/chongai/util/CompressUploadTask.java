package me.qiang.android.chongai.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by LiQiang on 24/1/15.
 */
public class CompressUploadTask extends AsyncTask<String, Void, InputStream > {
    @Override
    protected InputStream doInBackground(String... photoUrls) {
        Log.i("Async Task", photoUrls[0]);
        Bitmap bmp = BitmapFactory.decodeFile(photoUrls[0]);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
        InputStream in = new ByteArrayInputStream(bos.toByteArray());
        return in;
    }
};
