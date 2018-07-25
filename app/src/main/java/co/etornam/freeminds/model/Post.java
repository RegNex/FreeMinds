package co.etornam.freeminds.model;

import java.util.Date;

public class Post {
    private String postText;
    private String current_userId;
    private String imageUrl;
    private String imgDesc;
    private String timeStamp;
    private String mText;
    private Date datePosted;
    private String thumbnail;

    public Post() {
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getImgDesc() {
        return imgDesc;
    }

    public void setImgDesc(String imgDesc) {
        this.imgDesc = imgDesc;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(Date datePosted) {
        this.datePosted = datePosted;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getCurrent_userId() {
        return current_userId;
    }

    public void setCurrent_userId(String current_userId) {
        this.current_userId = current_userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getmText() {
        return mText;
    }

    public void setmText(String mText) {
        this.mText = mText;
    }
}
