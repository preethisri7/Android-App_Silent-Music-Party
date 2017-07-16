package de.tudarmstadt.informatik.tk.shhparty.music;

import java.io.Serializable;

/**
 * Created by Ashwin on 11/23/2016.
 */

public class MusicBean implements Serializable {


    private long musicID;
    private String musicTitle;
    private String artist;
    private boolean inPlayist;
    private int votes;

    public MusicBean(long musicID,String musicTitle,String artist,boolean inPlayist,int votes) {
        this.musicID = musicID;
        this.musicTitle=musicTitle;
        this.artist=artist;
        this.inPlayist=inPlayist;
        this.votes=votes;
    }

    public long getMusicID() {
        return musicID;
    }

    public void setMusicID(long musicID) {
        this.musicID = musicID;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }


    public boolean isInPlayist() {
        return inPlayist;
    }

    public void setInPlayist(boolean inPlayist) {
        this.inPlayist = inPlayist;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
