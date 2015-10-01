package com.example.mystuff.lick;

/**
 * Created by MyStuff on 9/22/15.
 */
public class Riffs {

    private long id;
    private String title;
    private String artist;

    public Riffs(long riffID, String riffTitle, String riffArtist) {
        id=riffID;
        title=riffTitle;
        artist=riffArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}


}
