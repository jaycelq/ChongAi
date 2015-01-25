package me.qiang.android.chongai.Activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.R;

public class ChoosePetActivity extends BaseToolbarActivity {

    private ListView petList;
    private ImageView petIcon;
    private int petSelectedPosition = 0;
    private PetListAdapter petListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_pet);

        petList = (ListView) findViewById(R.id.pet_list);
        petIcon = (ImageView) findViewById(R.id.pet_icon);

        petListAdapter = new PetListAdapter();
        petList.setAdapter(petListAdapter);

        petList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                petSelectedPosition = position;
                petListAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_pet, menu);
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

    public class PetListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        PetListAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.pet_choose_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.petPhoto = (CircleImageView) view.findViewById(R.id.pet_photo);
                holder.petName = (TextView) view.findViewById(R.id.pet_name);
                holder.petSelected = (ImageView) view.findViewById(R.id.pet_selected);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if(position == petSelectedPosition)
                holder.petSelected.setImageResource(R.drawable.pet_selected);
            else
                holder.petSelected.setImageResource(R.drawable.pet_unselected);

            return view;
        }
    }

    static class ViewHolder {
        CircleImageView petPhoto;
        TextView petName;
        ImageView petSelected;
    }
}
