package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.widget.PetProfileView;

public class PetActivity extends ActionBarActivity {

    // UI widget
    GridViewWithHeaderAndFooter petPhotoWithHeader;
    PetProfileView headerView;
    private View footerView;
    private View loading;
    private TextView noMoreStates;

    List<String> imageUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);

        petPhotoWithHeader = (GridViewWithHeaderAndFooter) findViewById(R.id.pet_photo_with_header);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        headerView = (PetProfileView)layoutInflater.inflate(R.layout.pet_profile_item, null);
        footerView = layoutInflater.inflate(R.layout.loading, null);
        loading = footerView.findViewById(R.id.loading_layout);
        noMoreStates = (TextView) footerView.findViewById(R.id.loading_nomore);
        petPhotoWithHeader.addHeaderView(headerView);
        petPhotoWithHeader.addFooterView(footerView);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pet, menu);
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
