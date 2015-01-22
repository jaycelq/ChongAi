package me.qiang.android.chongai.util;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LiQiang on 22/1/15.
 */
public class Comment {

    @SerializedName("comment_id")
    public int commentId;

    @SerializedName("post_id")
    public int stateId;

    @SerializedName("comment_user")
    public User commentUser;

    @SerializedName("to_user")
    public User toUser;

    @SerializedName("content")
    public String content;

    public Comment(int commentId, int stateId, User commentUser, User toUser, String content) {
        this.commentId = commentId;
        this.stateId = stateId;
        this.commentUser = commentUser;
        this.toUser = toUser;
        this.content = content;
    }

    public int getCommentId() {
        return commentId;
    }

    public int getStateId() {
        return stateId;
    }

    public String getContent() {
        return content;
    }

    public String getCommentUserPhoto() {
        return commentUser.getUserPhoto();
    }

    public String getCommentUserName() {
        return commentUser.getUserName();
    }

    public User.Gender getCommentUserGender() {
        return commentUser.getUserGender();
    }

    public User getToUser() {
        return toUser;
    }

    public User getCommentUser() {
        return commentUser;
    }
}
