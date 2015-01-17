package me.qiang.android.chongai.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;
import me.qiang.android.chongai.util.StateExploreManager;
import me.qiang.android.chongai.util.StateItem;


public class StateEdit extends ActionBarActivity implements View.OnClickListener{

    private Toolbar mToolbar;

    private DisplayImageOptions options;

    private String photoUrl;

    private ImageView imageView;

    private Button sendState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_edit);

        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        imageView = (ImageView) findViewById(R.id.state_photo);
        Button sendState = (Button) findViewById(R.id.send_state);
        sendState.setOnClickListener(this);

        photoUrl = getIntent().getStringExtra("STATE_PHOTO");

        ImageLoader.getInstance().displayImage("file://" + photoUrl, imageView, options);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.send_state:
                new CompressUploadTask().execute(photoUrl);
                StateItem newStateItem = new StateItem();
                newStateItem.setStateImage("file://" + photoUrl);
                StateExploreManager.getStateExploreManager().push(newStateItem);
                startMainActivity();
                break;
            default:
                break;
        }
    }

    private void startMainActivity() {
        Intent registerIntent = new Intent(this, MainActivity.class);
        startActivity(registerIntent);
        this.finish();
    }

    private class CompressUploadTask extends AsyncTask<String, Void, InputStream > {
        @Override
        protected InputStream doInBackground(String... photoUrls) {
            Log.i("Async Task", photoUrls[0]);
            Bitmap bmp = BitmapFactory.decodeFile(photoUrls[0]);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            InputStream in = new ByteArrayInputStream(bos.toByteArray());
            return in;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            SendStateHttpClient sendStateHttpClient = new SendStateHttpClient(inputStream);
            sendStateHttpClient.sendState();
        }
    };

    public class SendStateHttpClient {
        InputStream photoFileStream;

        SendStateHttpClient(InputStream inputStream) {
            photoFileStream = inputStream;
        }

        public void sendState(){
            RequestParams params = new RequestParams();
            File photo = new File(photoUrl);
            params.put("photoFile", photoFileStream, photo.getName());
            HttpClient.post("login", params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    // If the response is JSONObject instead of expected JSONArray
                    Log.i("sendState", response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.i("sendState", "JSON FAIL");
                }
            });
        }
    }
}
