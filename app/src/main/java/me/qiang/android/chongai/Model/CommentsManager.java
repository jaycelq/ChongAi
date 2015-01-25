package me.qiang.android.chongai.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiQiang on 25/1/15.
 */
public class CommentsManager {
    private int stateId;
    private List<Comment> commentList;

    public CommentsManager(int stateId) {
        this.stateId = stateId;
        commentList = new ArrayList<>();
    }

    public int commentsCount() {
        return commentList.size();
    }

    public Comment getComment(int i) {
        return commentList.get(i);
    }

    public User getCommentUser(int i) {
        return commentList.get(i).getCommentUser();
    }

    public void pushComment(Comment comment) {
        commentList.add(0, comment);
    }

    public void updateCommentsList(List<Comment> newCommentsList) {
        commentList = newCommentsList;
    }

    public void addComments(List<Comment> newCommentsList) {
        commentList.addAll(newCommentsList);
    }
}