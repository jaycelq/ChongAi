package me.qiang.android.chongai.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.CompressUploadTask;
import me.qiang.android.chongai.util.RequestServer;


public class StateEdit extends BaseToolbarActivity implements View.OnClickListener{

    private String photoUrl;

    private ImageView imageView;

    private Button sendState;

    private EditText stateText;
    private String stateTextContent;

    private LinearLayout changePet;

    private Context context;

    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_edit);

        context = this;

        currentUser = GlobalApplication.getUserSessionManager().getCurrentUser();

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
        showProgressDialog("正在处理...");
        new CompressUploadTask(){
            @Override
            protected void onPostExecute(InputStream inputStream) {
                RequestServer.sendState(stateTextContent, photoUrl, 1,
                        0.0, 0.0, inputStream, newSendStateCallback());
            }
        }.execute(photoUrl);
    }

    private void startChoosePetActivity() {
        Intent choosePetIntent = new Intent(this, ChoosePetActivity.class);
        startActivity(choosePetIntent);
    }

    private JsonHttpResponseHandler newSendStateCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("sendState", response.toString());
                try {
                    int stateId = response.getJSONObject("body").getInt("post_id");
                    StateItem newStateItem = new StateItem(stateId,
                            "file://" + photoUrl, stateTextContent, currentUser, new Pet());
                    StateExploreManager.getStateExploreManager().push(newStateItem);
                    ActivityTransition.startMainActivity(StateEdit.this);
                    StateEdit.this.finish();
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
                hideProgressDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("sendState", "JSON FAIL");
                hideProgressDialog();
            }
        };
    }
}
