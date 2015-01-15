package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;

import me.qiang.android.chongai.R;

public class LocationPikcerActivity extends ActionBarActivity {

    private String[] mStrings = { "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
            "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler", "Abbaye de Belloc", "Abbaye du Mont des Cats", "Abertam", "Abondance", "Ackawi",
            "Acorn", "Adelost", "Affidelice au Chablis", "Afuega'l Pitu", "Airag", "Airedale", "Aisy Cendre",
            "Allgauer Emmentaler" };

    NumberPicker numberPicker1;
    NumberPicker numberPicker;

    Button click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading);
        ProgressBar v = (ProgressBar) findViewById(R.id.loading);
        v.getIndeterminateDrawable().setColorFilter(0xff7FB446,
                android.graphics.PorterDuff.Mode.SRC_ATOP);
//        numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
//        numberPicker1 = (NumberPicker) findViewById(R.id.numberPicker1);
//        click = (Button) findViewById(R.id.click);
//        click.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(LocationPikcerActivity.this);
//                AlertDialog dialog = builder.create();
//                View rootView = getLayoutInflater().inflate(R.layout.activity_location_pikcer,null);
//                NumberPicker numberPicker1;
//                numberPicker1 = (NumberPicker) rootView.findViewById(R.id.numberPicker);
//                numberPicker1.setMaxValue(mStrings.length-1);
//                numberPicker1.setMinValue(0);
//                numberPicker1.setDisplayedValues(mStrings);
//                numberPicker1.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//                dialog.setView(rootView);
//                dialog.show();
//            }
//        });
//        numberPicker.setMaxValue(mStrings.length-1);
//        numberPicker.setMinValue(0);
//        numberPicker.setDisplayedValues(mStrings);
//        numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
//        numberPicker1.setDisplayedValues(mStrings);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_location_pikcer, menu);
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
