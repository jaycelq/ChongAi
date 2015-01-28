package me.qiang.android.chongai.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import me.qiang.android.chongai.GlobalApplication;
import me.qiang.android.chongai.Model.Pet;
import me.qiang.android.chongai.Model.UserSessionManager;
import me.qiang.android.chongai.R;

/**
 * Created by LiQiang on 26/1/15.
 */
public class PetProfileView extends RelativeLayout {
    private Context context;

    private UserSessionManager userSessionManager;

    public CircleImageView petPhoto;
    public TextView petMetaName;
    public TextView petMetaType;
    public CircleImageView petOwnerPhoto;
    public TextView petDeviceStatus;
    public TextView petHobby;
    public TextView petAge;
    public TextView petSkill;
    public TextView petName;

    public PetProfileView(Context context) {
        this(context, null);
        this.context = context;
    }

    public PetProfileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.context = context;
    }

    public PetProfileView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

        userSessionManager = GlobalApplication.getUserSessionManager();

        LayoutInflater.from(context).inflate(R.layout.pet_profile, this, true);

        petPhoto = (CircleImageView) findViewById(R.id.pet_photo);
        petMetaName = (TextView) findViewById(R.id.pet_meta_name);
        petMetaType = (TextView) findViewById(R.id.pet_meta_type);
        petOwnerPhoto = (CircleImageView) findViewById(R.id.pet_owner_photo);
        petDeviceStatus = (TextView) findViewById(R.id.pet_device_status);
        petHobby = (TextView) findViewById(R.id.pet_hobby);
        petAge = (TextView) findViewById(R.id.pet_age);
        petSkill = (TextView) findViewById(R.id.pet_skill);
        petName = (TextView) findViewById(R.id.pet_name);
    }

    public void updatePetProfileView(final Pet pet) {
        Picasso.with(context)
                .load(pet.getPetPhoto())
                .fit()
                .centerCrop()
                .into(petPhoto);

        petMetaName.setText(pet.getPetName());

        petMetaType.setText(pet.getPetType());

        if (pet.getPetUserId() != userSessionManager.getCurrentUser().getUserId()) {
            petDeviceStatus.setVisibility(View.GONE);
            petOwnerPhoto.setVisibility(View.VISIBLE);
            Picasso.with(context)
                    .load(pet.getPetOwner().getUserPhoto())
                    .fit()
                    .centerCrop()
                    .into(petOwnerPhoto);
            petOwnerPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        } else {
            petDeviceStatus.setVisibility(VISIBLE);
            petOwnerPhoto.setVisibility(View.GONE);
            if (pet.getImei() == null) {
                petDeviceStatus.setText("点击绑定设备");
            } else {
                petDeviceStatus.setText("点击更改绑定");
            }
            petDeviceStatus.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        petHobby.setText(pet.getPetHobby());
        petAge.setText(pet.getPetAge() + "岁");
        petSkill.setText(pet.getPetSkill());
    }

}
