package me.qiang.android.chongai.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.makeramen.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.GridViewWithHeaderAndFooter;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.RequestServer;
import me.qiang.android.chongai.widget.PetProfileView;

public class PetActivity extends BaseToolbarActivity {

    private Context context;

    private UserSessionManager userSessionManager;

    // UI widget
    GridViewWithHeaderAndFooter petImageGridView;
    View headerView;
    PetProfileView petHeader;
    private ImageAdapter mAdapter;

    private int petId;
    private Pet currentPet;

    List<String> imageUrls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);

        showBackButton();
        enableBackButton();
        setToolbarTile("宠物详情");

        context = this;
        userSessionManager = GlobalApplication.getUserSessionManager();

        petId = getIntent().getIntExtra(Constants.Pet.PET_ID, 0);

        petImageGridView = (GridViewWithHeaderAndFooter) findViewById(R.id.pet_image_grid);
        headerView = getLayoutInflater().inflate(R.layout.pet_profile_item, petImageGridView, false);
        petHeader = (PetProfileView) headerView.findViewById(R.id.pet_header);
        petImageGridView.addHeaderView(headerView);

        mAdapter = new ImageAdapter();
        petImageGridView.setAdapter(mAdapter);

        RequestServer.getPetInfo(petId, newGetPetInfoCallback());
    }

    private JsonHttpResponseHandler newGetPetInfoCallback(){
        return new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("JSON", response.toString());
                Gson gson = new Gson();
                try {
                    if(response.getInt("status") == 0) {
                        JSONObject petJsonObject = response.getJSONObject("body").getJSONObject("pet");
                        currentPet = gson.fromJson(petJsonObject.toString(), Pet.class);
                        petHeader.updatePetProfileView(currentPet);
                        JSONArray petImageJsonArray = response.getJSONObject("body").getJSONArray("photo_list");
                        Type PetImageListType = new TypeToken<ArrayList<String>>(){}.getType();
                        imageUrls = gson.fromJson(petImageJsonArray.toString(), PetImageListType);
                        mAdapter.notifyDataSetChanged();
                        invalidateOptionsMenu();
                    }

                } catch (JSONException ex)
                {}
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(currentPet != null && currentPet.getPetUserId() == userSessionManager.getCurrentUser().getUserId())
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
            ActivityTransition.startAddPetActivity(this, petId);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.Pet.ADD_PET) {
            if (resultCode == Activity.RESULT_OK) {
                int petId = data.getExtras().getInt(Constants.Pet.PET_UPDATE_RESULT);
                Pet pet = userSessionManager.getPet(petId);
                pet.petUser = userSessionManager.getCurrentUser();
                petHeader.updatePetProfileView(pet);
            }
        }
    }

    public class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        ImageAdapter() {
            inflater = LayoutInflater.from(PetActivity.this);
        }

        @Override
        public int getCount() {
            return imageUrls.size();
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
            ImageViewHolder imageViewHolder;
            View view = convertView;
            Log.i("getadapter", position+"");
            if (view == null) {
                    view = inflater.inflate(R.layout.item_grid_image, parent, false);
                    imageViewHolder = new ImageViewHolder();
                    assert view != null;
                    imageViewHolder.imageView = (ImageView) view.findViewById(R.id.image);
                    view.setTag(imageViewHolder);
            }

            imageViewHolder = (ImageViewHolder) view.getTag();

            final ImageView imageView = imageViewHolder.imageView;

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.TRANSPARENT)
                    .borderWidthDp(3)
                    .cornerRadiusDp(10)
                    .oval(false)
                    .build();

            Picasso.with(context)
                    .load(imageUrls.get(position))
                    .fit()
                    .centerCrop()
                    .transform(transformation)
                    .into(imageViewHolder.imageView);
            imageView.setColorFilter(null);
            imageView.setTag(position);

            return view;

        }
    }

    static class ImageViewHolder {
        ImageView imageView;
//		ImageView item_select;
    }
}
