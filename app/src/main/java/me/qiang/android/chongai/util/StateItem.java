package me.qiang.android.chongai.util;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 1/11/2015.
 */
public class StateItem {
    @SerializedName("post_id")
    public int stateId;

    @SerializedName("sender")
    public User stateOwner;

    @SerializedName("pet")
    public Pet statePet;

    @SerializedName("picture")
    public String stateImage;

    @SerializedName("content")
    public String stateContent;

    @SerializedName("location")
    public StateLocation stateLocation;

    @SerializedName("add_time")
    public long stateAddTime;

    @SerializedName("like_num")
    public int statePraisedNum;

    @SerializedName("likes")
    public List<User> statePraised;

    @SerializedName("comment_num")
    public int stateCommentsNum;

    // TODO: remove the default test constructor
    public StateItem() {
        stateOwner = new User();
        statePet = new Pet();
        stateImage = "drawable://" + R.drawable.pet_dog;
        statePraised = new LinkedList<>();
        statePraisedNum = 0;
        stateCommentsNum = 0;
    }

    public String getStateOwnerPhoto() {
        return stateOwner.photo;
    }

    public String getStateImage() {
        return stateImage;
    }

    public int getStatePraisedNum() {
        return statePraisedNum;
    }

    public String getPraiseUserPhoto(int i) {
        return statePraised.get(i).photo;
    }

    public void setStateImage(String stateImage) {
        this.stateImage = stateImage;
    }

    public boolean isFollowedStateOwner() {return stateOwner.isFollowed();}

    public int getStateOwnerId() {return stateOwner.getUserId();}

    public class StateLocation {
        @SerializedName("location_x")
        public double x;

        @SerializedName("location_y")
        public double y;
    }
}
