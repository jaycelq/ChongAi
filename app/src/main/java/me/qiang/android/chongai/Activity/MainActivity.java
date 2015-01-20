package me.qiang.android.chongai.Activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import me.qiang.android.chongai.Fragment.BottomNavigationFragment;
import me.qiang.android.chongai.Fragment.BottomPopupFragment;
import me.qiang.android.chongai.Fragment.CreateNewStateFragment;
import me.qiang.android.chongai.Fragment.DrawerFragment;
import me.qiang.android.chongai.Fragment.MainFragment;
import me.qiang.android.chongai.Fragment.StateFragment;
import me.qiang.android.chongai.PopUpWindow.CreateNewStatePopupWindow;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.CameraUtil;

public class MainActivity extends ActionBarActivity implements DrawerFragment.OnFragmentInteractionListener,
        StateFragment.OnFragmentInteractionListener, BottomNavigationFragment.OnFragmentInteractionListener,
        BottomPopupFragment.OnFragmentInteractionListener, CreateNewStateFragment.OnFragmentInteractionListener {

    private Toolbar mToolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private CreateNewStatePopupWindow createNewStatePopupWindow;

    private final String MAINFRAGEMENT = "MAINFRAGEMENT";

    static final int REQUEST_TAKE_PHOTO = 1;
    private File photoFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
//        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.myPrimaryDarkColor));
//
//        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
//        drawerLayout.setDrawerListener(mActionBarDrawerToggle);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.full_screen_container, new MainFragment(), MAINFRAGEMENT)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_drawer, new DrawerFragment())
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bottom_container, new BottomNavigationFragment())
                    .commit();
        }

        View container = getLayoutInflater()
                .inflate(R.layout.create_new_popup, null);
        createNewStatePopupWindow = new CreateNewStatePopupWindow(container,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //Log.i("Test", id);
    }

    @Override
    public void onFragmentInteraction(String uri) {
        //Log.i("Test", id);
    }

    @Override
    public void onFragmentInteraction(int id) {
        //Log.i("Test", id);
        //startActivity(new Intent(this, StateEdit.class));
//        createNewStatePopupWindow
//                .setAnimationStyle(R.style.anim_popup_dir);
//        createNewStatePopupWindow.showAsDropDown(findViewById(R.id.toolbar_actionbar), 0, 0);
        FragmentTransaction ft =  getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

        CreateNewStateFragment newFragment = new CreateNewStateFragment();
        ft.add(R.id.full_screen_container, newFragment, "newFragment");

        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        BottomPopupFragment bottomPopupFragment = new BottomPopupFragment();
        ft.add(R.id.bottom_container, bottomPopupFragment, "bottomFragment");
        ft.addToBackStack(null);

        // Start the animated transition.
        ft.commit();
    }

    @Override
    public void onCloseButtonClicked() {
        BottomPopupFragment bottomPopupFragment = (BottomPopupFragment) getSupportFragmentManager().findFragmentByTag("bottomFragment");
        bottomPopupFragment.startCloseAnimation();
    }

    @Override
    public void onCreateNewFragmentClick(int id) {
        switch (id) {
            case R.id.pop_up_bg:
                onCloseButtonClicked();
                break;
            case R.id.pick_photo:
                startAlbumActivity();
                break;
            case R.id.take_photo:
                takePhoto();
                break;
            default:
                break;
        }
    }

    private void startAlbumActivity() {
        Intent intent = new Intent(MainActivity.this, CustomAlbum.class);
        startActivity(intent);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            try {
                photoFile = CameraUtil.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("TAKE_PHOTO", ex.getMessage());

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                startStateEditActivity(photoFile.getCanonicalPath());
            }
            catch (IOException ex) {
                // Error occurred while creating the File
                Log.i("TAKE_PHOTO", ex.getMessage());

            }
        }
    }

    private void startStateEditActivity(String imageFile) {
        Intent intent = new Intent(this, StateEdit.class);
        intent.putExtra("STATE_PHOTO", imageFile);
        Log.i("CAMERA_CAPTURE", imageFile);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // if there is a fragment and the back stack of this fragment is not empty,
        // then emulate 'onBackPressed' behaviour, because in default, it is not working
        BottomPopupFragment bottomPopupFragment = (BottomPopupFragment) getSupportFragmentManager().findFragmentByTag("bottomFragment");
        if(bottomPopupFragment != null && bottomPopupFragment.isVisible())
            onCloseButtonClicked();
        else
            super.onBackPressed();
    }
}
