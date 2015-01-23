package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import at.markushi.ui.CircleButton;
import me.qiang.android.chongai.R;

public class AddProfileActivity extends BaseToolbarActivity {

    private RadioGroup genderRadioGroup;
    private EditText profileName;
    private EditText profileLocation;
    private EditText profileSignature;
    private CircleButton profilePhoto;
    private Button saveProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        setToolbarTile("我的资料");
        genderRadioGroup = (RadioGroup) findViewById(R.id.profile_gender);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
