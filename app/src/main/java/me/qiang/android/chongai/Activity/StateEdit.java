package me.qiang.android.chongai.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.Constants;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.StateExploreManager;
import me.qiang.android.chongai.Model.StateItem;
import me.qiang.android.chongai.Model.User;
import me.qiang.android.chongai.R;
import me.qiang.android.chongai.util.ActivityTransition;
import me.qiang.android.chongai.util.CompressUploadTask;
import me.qiang.android.chongai.util.RequestServer;


public class StateEdit extends BaseToolbarActivity implements View.OnClickListener{

    private String photoUrl;

    private ImageView imageView;

    private Button sendState;

    private EditText stateText;
    private String stateTextContent;

    private LinearLayout changePet;
    private TextView statePetName;
    private TextView statePetType;

    private View changePhoto;

    private Context context;

    private User currentUser;

    private ArrayList<Pet> petList;
    private Pet currentPet;

    private int petSelectedPosition = 0;

    private PetListAdapter petListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_edit);

        setToolbarTile("编辑状态");

        showBackButton();
        enableBackButton();

        context = this;

        currentUser = GlobalApplication.getUserSessionManager().getCurrentUser();
        petList = GlobalApplication.getUserSessionManager().getPetList();
        currentPet = petList.get(0);
        petListAdapter = new PetListAdapter();

        stateText = (EditText) findViewById(R.id.state_text);

        statePetName = (TextView) findViewById(R.id.state_pet_name);
        statePetName.setText(currentPet.getPetName());

        statePetType = (TextView) findViewById(R.id.state_pet_type);
        statePetType.setText(currentPet.getPetType());

        if(currentPet.getPetGender() == Pet.Gender.FEMALE) {
            ((GradientDrawable)statePetName.getBackground()).setColor(0xFFFF939A);
            ((GradientDrawable)statePetType.getBackground()).setColor(0xFFFF939A);
        }
        else {
            ((GradientDrawable)statePetName.getBackground()).setColor(0xFFA8C9F5);
            ((GradientDrawable)statePetType.getBackground()).setColor(0xFFA8C9F5);
        }

        imageView = (ImageView) findViewById(R.id.state_photo);

        sendState = (Button) findViewById(R.id.send_state);
        sendState.setOnClickListener(this);

        changePet = (LinearLayout) findViewById(R.id.change_pet);
        changePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPetListDialog();
            }
        });

        photoUrl = getIntent().getStringExtra(Constants.Image.IMAGE_RESULT);

        Picasso.with(context)
                .load("file://" + photoUrl)
                .fit()
                .centerCrop()
                .into(imageView);

        changePhoto = findViewById(R.id.change_photo);
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityTransition.pickImageFromAlbum(StateEdit.this);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.send_state:
                attemptSendState();
                break;
            default:
                break;
        }
    }

    private void attemptSendState() {

        stateTextContent = stateText.getText().toString();

        if(TextUtils.isEmpty(stateTextContent)) {
            Toast.makeText(this, "还是说点什么吧～", Toast.LENGTH_LONG).show();
            return;
        }
        showProgressDialog("正在处理...");
        new CompressUploadTask(){
            @Override
            protected void onPostExecute(InputStream inputStream) {
                RequestServer.sendState(stateTextContent, photoUrl, currentPet.getPetId(),
                        0.0, 0.0, inputStream, newSendStateCallback());
            }
        }.execute(photoUrl);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.Pet.PICK_PET) {
            if (resultCode == Activity.RESULT_OK) {
                int petId = data.getExtras().getInt(Constants.Pet.PET_ID);
                Pet pet = GlobalApplication.getUserSessionManager().getPet(petId);
                currentPet = pet;
                statePetName.setText(currentPet.getPetName());
                statePetType.setText(currentPet.getPetType());

                if(currentPet.getPetGender() == Pet.Gender.FEMALE) {
                    ((GradientDrawable)statePetName.getBackground()).setColor(0xFFFF939A);
                    ((GradientDrawable)statePetType.getBackground()).setColor(0xFFFF939A);
                }
                else {
                    ((GradientDrawable)statePetName.getBackground()).setColor(0xFFA8C9F5);
                    ((GradientDrawable)statePetType.getBackground()).setColor(0xFFA8C9F5);
                }
            }
        }
        else if(requestCode ==Constants.Image.PICK_IMAGE) {
            if(resultCode == RESULT_OK) {
                photoUrl = data.getExtras().getString(Constants.Image.IMAGE_RESULT);
                Log.i("PHOTO_URL", photoUrl);
                Picasso.with(context)
                        .load("file://" + photoUrl)
                        .fit()
                        .centerCrop()
                        .into(imageView);
            }
        }
        else if(requestCode == Constants.Pet.ADD_PET) {
            if(resultCode == Activity.RESULT_OK) {
                petList = GlobalApplication.getUserSessionManager().getPetList();
                petListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showPetListDialog(){
        final AlertDialog.Builder alertDialog = new AlertDialog
                .Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.basic_listview, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("选择宠物");
        final AlertDialog petListAlertDialog = alertDialog.create();
        ListView lv = (ListView) convertView.findViewById(R.id.basic_list);
        lv.setAdapter(petListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < petList.size()) {
                    petSelectedPosition = position;
                    petListAdapter.notifyDataSetChanged();
                    Pet pet = petList.get(position);
                    currentPet = pet;
                    statePetName.setText(currentPet.getPetName());
                    statePetType.setText(currentPet.getPetType());

                    if (currentPet.getPetGender() == Pet.Gender.FEMALE) {
                        ((GradientDrawable) statePetName.getBackground()).setColor(0xFFFF939A);
                        ((GradientDrawable) statePetType.getBackground()).setColor(0xFFFF939A);
                    } else {
                        ((GradientDrawable) statePetName.getBackground()).setColor(0xFFA8C9F5);
                        ((GradientDrawable) statePetType.getBackground()).setColor(0xFFA8C9F5);
                    }
                    petListAlertDialog.dismiss();
                }
                else {
                    ActivityTransition.startAddPetActivity(StateEdit.this);
                }
            }
        });
        petListAlertDialog.show();
    }

    private JsonHttpResponseHandler newSendStateCallback() {
        return new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.i("sendState", response.toString());
                try {
                    int stateId = response.getJSONObject("body").getInt("post_id");
                    StateItem newStateItem = new StateItem(stateId,
                            "file://" + photoUrl, stateTextContent, currentUser, currentPet);
                    StateExploreManager.getStateExploreManager().push(newStateItem);
                    ActivityTransition.startMainActivity(StateEdit.this);
                    StateEdit.this.finish();
                } catch (JSONException ex) {
                    Log.i("JSON", ex.toString());
                }
                hideProgressDialog();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.i("sendState", "JSON FAIL");
                hideProgressDialog();
            }
        };
    }

    public class PetListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        PetListAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return petList.size() + 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if(position == petList.size())
                return 1;
            else
                return 0;
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
            int type = getItemViewType(position);
            Log.i("GetView", position + " "+ petList.size());
            if (view == null) {
                Log.i("GetView", position + " " + petSelectedPosition);
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

            if(type == 0) {
                Pet pet = petList.get(position);
                Picasso.with(context)
                        .load(pet.getPetPhoto())
                        .fit()
                        .centerCrop()
                        .into(holder.petPhoto);

                holder.petName.setText(pet.getPetName());

                if (position == petSelectedPosition)
                    holder.petSelected.setImageResource(R.drawable.dir_choose);
                else
                    holder.petSelected.setImageResource(android.R.color.transparent);
            }
            else {
                holder.petPhoto.setImageDrawable(getResources().getDrawable(R.drawable.add));
                holder.petName.setText("添加新宠物");
                holder.petSelected.setImageResource(android.R.color.transparent);
            }

            return view;
        }
    }

    static class ViewHolder {
        CircleImageView petPhoto;
        TextView petName;
        ImageView petSelected;
    }
}
