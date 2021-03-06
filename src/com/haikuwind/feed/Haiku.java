package com.haikuwind.feed;

import java.io.Serializable;
import java.util.Date;

import com.haikuwind.feed.UserInfo.Rank;

public class Haiku implements Cloneable, Serializable {
    private static final long serialVersionUID = 1L;

    public final static String TAG = Haiku.class.getSimpleName();
    
    public final static int MIN_POINTS = -5;
    public final static int HALL_OF_FAME_POINTS = 25;


    private String text;
    private int points;
    private String user;
    private int userRank;
    private boolean favoritedByMe;
    private int timesVotedByMe;
    private Date time;
    private String id;
    
    public Haiku() {
        
    }
    
    public Haiku(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Rank getUserRank() {
        return Rank.getFromNum(userRank);
    }

    public void setUserRank(int userRank) {
        this.userRank = userRank;
    }

    public boolean isFavoritedByMe() {
        return favoritedByMe;
    }

    public void setFavoritedByMe(boolean favoritedByMe) {
        this.favoritedByMe = favoritedByMe;
    }

    public int getTimesVotedByMe() {
        return timesVotedByMe;
    }

    public void setTimesVotedByMe(int timesVotedByMe) {
        this.timesVotedByMe = timesVotedByMe;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Haiku [text=" + text + ", points=" + points + ", user=" + user
                + ", userRank=" + userRank + ", favoritedByMe=" + favoritedByMe
                + ", timesVotedByMe=" + timesVotedByMe + ", time=" + time
                + ", id=" + id + "]";
    }

}
