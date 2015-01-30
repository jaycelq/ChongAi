package me.qiang.android.chongai.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;

public class ChoosePetActivity extends BaseToolbarActivity {

    private Context context;

    private ListView petList;
    private ImageView petIcon;
    private View addPet;

    private int petSelectedPosition = 0;
    private PetListAdapter petListAdapter;

    private ArrayList<Pet> myPetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_pet);

        context = this;
        myPetList = GlobalApplication.getUserSessionManager().getPetList();

        petList = (ListView) findViewById(R.id.pet_list);
        petIcon = (ImageView) findViewById(R.id.pet_icon);
        addPet = findViewById(R.id.add_pet);
        addPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.startAddPetActivity(ChoosePetActivity.this);
            }
        });

        petListAdapter = new PetListAdapter();
        petList.setAdapter(petListAdapter);

        petList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                petSelectedPosition = position;
                petListAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                intent.putExtra(Constants.Pet.PET_ID, myPetList.get(position).getPetId());
                ChoosePetActivity.this.setResult(Activity.RESULT_OK, intent);
                ChoosePetActivity.this.finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.Pet.ADD_PET) {
            if (resultCode == Activity.RESULT_OK) {
                myPetList = GlobalApplication.getUserSessionManager().getPetList();
                petListAdapter.notifyDataSetChanged();
            }
        }
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
            return myPetList.size();
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
            Pet pet = myPetList.get(position);
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

            Picasso.with(context)
                    .load(pet.getPetPhoto())
                    .fit()
                    .centerCrop()
                    .into(holder.petPhoto);

            holder.petName.setText(pet.getPetName());

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
