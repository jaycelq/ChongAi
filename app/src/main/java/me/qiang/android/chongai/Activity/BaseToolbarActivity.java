package me.qiang.android.chongai.Activity;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.qiang.android.chongai.R;

public class BaseToolbarActivity extends ActionBarActivity {

    protected Toolbar mToolbar;
    protected ImageView mToolbarBackButton;
    protected TextView mToolbarTitle;
    protected ProgressDialog barProgressDialog;

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();

        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        mToolbarBackButton = (ImageView) findViewById(R.id.toolbar_back_button);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        barProgressDialog = new ProgressDialog(this);
        barProgressDialog.setProgressStyle(barProgressDialog.STYLE_SPINNER);

        if(mToolbar != null) {
            setSupportActionBar(mToolbar);

            // Use no logo customized title instead ot the default actionbar title
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    public void setToolbarTile(String title) {
        mToolbarTitle.setText(title);
    }

    public void enableBackButton() {
        mToolbarBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void disableBackButton() {
        mToolbarBackButton.setClickable(false);
    }

    public void hideBackButton() {
        mToolbarBackButton.setVisibility(View.GONE);
    }

    public void showBackButton() {
        mToolbarBackButton.setVisibility(View.VISIBLE);
    }

    public void showProgressDialog(String message) {
        barProgressDialog.setMessage(message);
        barProgressDialog.show();
    }

    public void hideProgressDialog() {
        barProgressDialog.dismiss();
    }
 }
