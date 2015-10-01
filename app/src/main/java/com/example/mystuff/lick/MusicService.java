package com.example.mystuff.lick;


import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by MyStuff on 9/22/15.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Riffs> riffs;
    //current position
    private int riffPosn;
    private final IBinder musicBind = new MusicBinder();
    private String songTitle="";
    private static final int NOTIFY_ID=1;
    private boolean shuffle=false;
    private Random rand;
    private String riffTitle;

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playRiff(){
        //play a song
        player.reset();
        //get song
        Riffs playRiff = riffs.get(riffPosn);
        riffTitle=playRiff.getTitle();
        //get id
        long currRiff = playRiff.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currRiff);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        riffPosn=0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        rand=new Random();
    }

    public void setShuffle(){
        shuffle = !shuffle;
    }
    public void playNext(){
        if(shuffle){
            int newSong = riffPosn;
            while(newSong==riffPosn){
                newSong=rand.nextInt(riffs.size());
            }
            riffPosn=newSong;
        }
        else{
            riffPosn++;
            if(riffPosn>=riffs.size()) riffPosn=0;
        }
        playRiff();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Riffs> theRiffs){
        riffs=theRiffs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        Intent notIntent = new Intent(this, Record.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.play)
                .setTicker(riffTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
        .setContentText(riffTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);
    }
    public void setRiff(int riffIndex){
        riffPosn=riffIndex;
    }

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
    public void playPrev(){
        riffPosn--;
        if(riffPosn<=0) riffPosn=riffs.size()-1;
        playRiff();
    }
}