package me.qiang.android.chongai.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;

import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.Fragment.BottomNavigationFragment;
import me.qiang.android.chongai.Fragment.BottomPopupFragment;
import me.qiang.android.chongai.Fragment.CreateNewStateFragment;
import me.qiang.android.chongai.Fragment.DrawerFragment;
import me.qiang.android.chongai.Fragment.StateFragment;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.baidumap.BDMapFragment;

public class MainActivity extends BaseToolbarActivity implements
        DrawerFragment.OnFragmentInteractionListener,
        BottomPopupFragment.OnFragmentInteractionListener,
        BottomNavigationFragment.OnFragmentInteractionListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        setToolbarTile("我的爱宠");
        hideBackButton();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        drawerLayout.setStatusBarBackgroundColor(getResources()
                .getColor(R.color.myPrimaryDarkColor));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(mActionBarDrawerToggle);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_screen_container, new StateFragment(),
                            Constants.FragmentTag.STATE_FRAGMENT)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.frame_drawer, new DrawerFragment(),
                            Constants.FragmentTag.DRAWER_FRAGMENT)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.bottom_container, new BottomNavigationFragment(),
                            Constants.FragmentTag.BOTTOM_NAVIGATION_FRAGMENT)
                    .commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        mActionBarDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO: implement the call back with drawer item click
    }

    // deal with create new state click in BottomNavigation item click

    @Override
    public void onFragmentInteraction(int id) {
        // TODO: deal with fragment transaction on bottom icon click
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (id) {
            case R.id.add_state:
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                CreateNewStateFragment createNewStateFragment = new CreateNewStateFragment();
                ft.add(R.id.top_screen_container, createNewStateFragment,
                        Constants.FragmentTag.CREATE_NEW_STATE_FRAGMENT);

                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
                BottomPopupFragment bottomPopupFragment = new BottomPopupFragment();
                ft.add(R.id.bottom_container, bottomPopupFragment,
                        Constants.FragmentTag.BOTTOM_POPUP_FRAGMENT);

                ft.addToBackStack(null);
                ft.commit();
                break;
            case R.id.map:
                BDMapFragment bdMapFragment = (BDMapFragment) getSupportFragmentManager().findFragmentByTag(
                        Constants.FragmentTag.BDMAP_FRAGMENT);
                if(bdMapFragment == null)
                    bdMapFragment = new BDMapFragment();
                ft.replace(R.id.main_screen_container, bdMapFragment,
                        Constants.FragmentTag.BDMAP_FRAGMENT);
                ft.commit();
                break;
            case R.id.album:
                StateFragment stateFragment = (StateFragment) getSupportFragmentManager().findFragmentByTag(
                        Constants.FragmentTag.STATE_FRAGMENT);
                if(stateFragment == null)
                    stateFragment = new StateFragment();
                ft.replace(R.id.main_screen_container, stateFragment,
                        Constants.FragmentTag.BDMAP_FRAGMENT);
                ft.commit();
                break;
        }
    }

    @Override
    public void onCloseButtonClicked() {
        BottomPopupFragment bottomPopupFragment =
                (BottomPopupFragment) getSupportFragmentManager()
                        .findFragmentByTag(Constants.FragmentTag.BOTTOM_POPUP_FRAGMENT);
        bottomPopupFragment.startCloseAnimation();
    }


    @Override
    public void onBackPressed() {
        BottomPopupFragment bottomPopupFragment = (BottomPopupFragment) getSupportFragmentManager().findFragmentByTag("bottomFragment");
        if(bottomPopupFragment != null && bottomPopupFragment.isVisible())
            onCloseButtonClicked();
        else
            super.onBackPressed();
    }
}
