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
import me.qiang.android.chongai.Fragment.UserFragment;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.baidumap.BDMapFragment;

public class MainActivity extends BaseToolbarActivity implements
        DrawerFragment.OnFragmentInteractionListener,
        BottomPopupFragment.OnFragmentInteractionListener,
        BottomNavigationFragment.OnFragmentInteractionListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    private FragmentTransaction ft;
    private BDMapFragment bdMapFragment;
    private StateFragment stateFragment;

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
        ft = getSupportFragmentManager().beginTransaction();
        //采用transaction.add(), transaction.show(), transaction.hide()方式载入Fragment，替代transaction.replace()方式
        hideAllContentFragment(ft);
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
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

                bdMapFragment = (BDMapFragment) getSupportFragmentManager().findFragmentByTag(
                        Constants.FragmentTag.BDMAP_FRAGMENT);
                if(bdMapFragment == null){
                    bdMapFragment = new BDMapFragment();
                    ft.add(R.id.main_screen_container, bdMapFragment, Constants.FragmentTag.BDMAP_FRAGMENT);
                }else{
                    ft.show(bdMapFragment);
                }
//                ft.replace(R.id.main_screen_container, bdMapFragment, Constants.FragmentTag.BDMAP_FRAGMENT);
                ft.commit();
                break;
            case R.id.album:
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);

                stateFragment = (StateFragment) getSupportFragmentManager().findFragmentByTag(
                        Constants.FragmentTag.STATE_FRAGMENT);
                if(stateFragment == null){
                    stateFragment = new StateFragment();
                    ft.add(R.id.main_screen_container, stateFragment, Constants.FragmentTag.STATE_FRAGMENT);
                }else{
                    ft.show(stateFragment);
                }
//                ft.replace(R.id.main_screen_container, stateFragment, Constants.FragmentTag.STATE_FRAGMENT);
                ft.commit();
                break;
            case R.id.about_me:
                ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                UserFragment userFragment = (UserFragment) getSupportFragmentManager().findFragmentByTag(
                        Constants.FragmentTag.USER_FRAGMENT);
                if(userFragment == null)
                    userFragment = new UserFragment();
                ft.replace(R.id.main_screen_container, userFragment,
                        Constants.FragmentTag.USER_FRAGMENT);
                ft.commit();
                break;
        }
    }

    //采用transaction.add(), transaction.show(), transaction.hide()方式载入Fragment，替代transaction.replace()方式
    private void hideAllContentFragment(FragmentTransaction transaction) {
        if(stateFragment != null){
            transaction.hide(stateFragment);
        }
        if(bdMapFragment != null){
            transaction.hide(bdMapFragment);
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
