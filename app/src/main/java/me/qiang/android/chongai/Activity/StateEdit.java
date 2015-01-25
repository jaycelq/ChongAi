package me.qiang.android.chongai.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.HttpClient;


public class StateEdit extends ActionBarActivity implements View.OnClickListener{

    private Toolbar mToolbar;

    private String photoUrl;

    private ImageView imageView;

    private Button sendState;

    private EditText stateText;
    private String stateTextContent;

    private LinearLayout changePet;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_edit);

        context = this;

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

        stateText = (EditText) findViewById(R.id.state_text);

        imageView = (ImageView) findViewById(R.id.state_photo);

        sendState = (Button) findViewById(R.id.send_state);
        sendState.setOnClickListener(this);

        changePet = (LinearLayout) findViewById(R.id.change_pet);
        changePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChoosePetActivity();
            }
        });

        photoUrl = getIntent().getStringExtra(Constants.Image.IMAGE_RESULT);

        Picasso.with(context)
                .load("file://" + photoUrl)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.send_state:
                attemptSendState();
                break;
            default:
                break;
        }
    }

    private void attemptSendState() {

        stateTextContent = stateText.getText().toString();

        if(TextUtils.isEmpty(stateTextContent)) {
            Toast.makeText(this, "还是说点什么吧～", Toast.LENGTH_LONG).show();
            return;
        }

        new CompressUploadTask().execute(photoUrl);
        StateItem newStateItem = new StateItem();
        newStateItem.setStateImage("file://" + photoUrl);
        StateExploreManager.getStateExploreManager().push(newStateItem);
        startMainActivity();
    }

    private void startMainActivity() {
        Intent registerIntent = new Intent(this, MainActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(registerIntent);
        this.finish();
    }

    private void startChoosePetActivity() {
        Intent choosePetIntent = new Intent(this, ChoosePetActivity.class);
        startActivity(choosePetIntent);
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
        private JSONObject stateInfo = new JSONObject();

        SendStateHttpClient(InputStream inputStream) {
            photoFileStream = inputStream;

            try {
                stateInfo.put("content", stateTextContent);
                stateInfo.put("pet_id", 1);
                JSONObject location = new JSONObject();
                location.put("location_x", 0.0);
                location.put("location_y", 0.0);
                stateInfo.put("location", location);

            } catch (JSONException ex) {
                // 键为null或使用json不支持的数字格式(NaN, infinities)
                throw new RuntimeException(ex);
            }
        }

        public void sendState(){
            RequestParams params = new RequestParams();
            File photo = new File(photoUrl);
            params.put("photo", photoFileStream, photo.getName());
            params.put("content", stateInfo);
            HttpClient.post("post/index", params, new JsonHttpResponseHandler() {
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
