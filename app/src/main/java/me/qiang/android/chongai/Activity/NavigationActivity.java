package me.qiang.android.chongai.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.qiang.android.chongai.R;

public class NavigationActivity extends ActionBarActivity implements View.OnClickListener{

    private Button login, visitor, otherLogin;
    private TextView register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        login = (Button)findViewById(R.id.login);
        login.setOnClickListener(this);
        visitor = (Button)findViewById(R.id.visitor);
        visitor.setOnClickListener(this);
        otherLogin = (Button)findViewById(R.id.otherlogin);
        otherLogin.setOnClickListener(this);

        register = (TextView)findViewById(R.id.register);
        register.setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.login:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.visitor:
                startActivity(new Intent(this, StateEdit.class));
                break;
            case R.id.otherlogin:
                break;
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
        }
    }
}
