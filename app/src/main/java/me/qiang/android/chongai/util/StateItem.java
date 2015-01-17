package me.qiang.android.chongai.util;

import java.util.LinkedList;
import java.util.List;

import me.qiang.android.chongai.R;

/**
 * Created by qiang on 1/11/2015.
 */
public class StateItem {
    public User stateOwner;
    public Pet statePet;
    public String stateImage;
    public List<User> statePraised;
    public int statePraisedNum;
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
}
