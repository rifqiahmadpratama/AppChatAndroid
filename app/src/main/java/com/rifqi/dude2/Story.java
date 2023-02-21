package com.rifqi.dude2;

public class Story {

    private String imageurl;
    private String videourl;
    private long timestart;
    private long timeend;
    private String storyid;
    private String userid;

    public Story(String imageurl, String videourl, long timestart, long timeend, String storyid, String userid) {
        this.imageurl = imageurl;
        this.videourl = videourl;
        this.timestart = timestart;
        this.timeend = timeend;
        this.storyid = storyid;
        this.userid = userid;
    }

    public Story() {
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public long getTimestart() {
        return timestart;
    }

    public void setTimestart(long timestart) {
        this.timestart = timestart;
    }

    public long getTimeend() {
        return timeend;
    }

    public void setTimeend(long timeend) {
        this.timeend = timeend;
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
