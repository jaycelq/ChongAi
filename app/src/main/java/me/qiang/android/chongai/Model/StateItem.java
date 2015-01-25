package me.qiang.android.chongai.Model;

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

    @SerializedName("if_like")
    public boolean stateLike;

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
        stateLike = false;
    }

    public int getStateId() { return stateId;}

    public String getStateOwnerPhoto() {
        return stateOwner.photo;
    }

    public String getStateImage() {
        return stateImage;
    }

    public int getStatePraisedNum() {
        return statePraisedNum;
    }

    public int getStateCommentsNum() { return stateCommentsNum;}

    public String getPraiseUserPhoto(int i) {
        return statePraised.get(i).photo;
    }

    public int getPraiseUserId(int i) {
        return statePraised.get(i).getUserId();
    }

    public void setStateImage(String stateImage) {
        this.stateImage = stateImage;
    }

    public boolean isFollowedStateOwner() {return stateOwner.isFollowed();}

    public int getStateOwnerId() {return stateOwner.getUserId();}

    public String getStateOwnerName() {return stateOwner.getUserName();}

    public String getStateOwnerLocation() {return stateOwner.getUserLocation();}

    public String getStatePetName() {return statePet.getPetName();}

    public Pet.Gender getStatePetGender() {return statePet.getPetGender();}

    public String getStatePetType() {return statePet.getPetType();}

    public String getStateContent() {return stateContent;}

    public boolean getLikeState() {return stateLike;}

    public void setLikeState(boolean likeState) {
        stateLike = likeState;
    }

    public void addPraiseUser(User user) {
        statePraised.add(0, user);
        statePraisedNum += 1;
    }

    public void decreasePraiseUser(int userId) {
        statePraisedNum -= 1;

        for(int i = 0; i < statePraised.size(); i++) {
            if(statePraised.get(i).getUserId() == userId)
                statePraised.remove(i);
        }
    }

    public class StateLocation {
        @SerializedName("location_x")
        public double x;

        @SerializedName("location_y")
        public double y;
    }
}
