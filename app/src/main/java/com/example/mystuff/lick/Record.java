package com.example.mystuff.lick;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Record extends AppCompatActivity implements MediaPlayerControl {
    Button play, stop, record;
    private MediaRecorder myAudioRecorder;
    private String outputFile = null;
    private ArrayList<Riffs> riffList;
    private ListView riffView;
    private final ThreadLocal<MusicService> musicSrv = new ThreadLocal<>();
    private Intent playIntent;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean paused=false, playbackPaused=false;
//    Drawable shuffle = getDrawable(R.drawable.rand);
//    Drawable end = getDrawable(R.drawable.end);

    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    private void setController(){
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.riff_list));
        controller.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    public void riffPicked(View view){
        musicSrv.get().setRiff(Integer.parseInt(view.getTag().toString()));
        musicSrv.get().playRiff();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicSrv.get().setShuffle();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv.set(null);
                System.exit(0);
                break;
            case R.id.followButton:
                startActivity(new Intent(this, SelectUsersActivity.class));
                return true;
            case R.id.logoutButton:
			/*
			 * Log current user out using ParseUser.logOut()
			 */
                ParseUser.logOut();
                Intent intent = new Intent(this, LoginOrSignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
//        if (musicConnection != null) {
//            unbindService(musicConnection);
//        }
        stopService(playIntent);
        musicSrv.set(null);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        setController();

        riffView = (ListView) findViewById(R.id.riff_list);
        riffList = new ArrayList<Riffs>();
        getRiffList();
        Collections.sort(riffList, new Comparator<Riffs>() {
            public int compare(Riffs a, Riffs b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        RiffAdapter riffAdt = new RiffAdapter(this, riffList);
        riffView.setAdapter(riffAdt);

        play = (Button) findViewById(R.id.button3);
        stop = (Button) findViewById(R.id.button2);
        record = (Button) findViewById(R.id.button);

        stop.setEnabled(false);
        play.setEnabled(false);
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    myAudioRecorder.prepare();
                    myAudioRecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                record.setEnabled(false);
                stop.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;

                stop.setEnabled(false);
                play.setEnabled(true);

                Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws IllegalArgumentException, SecurityException, IllegalStateException {
                MediaPlayer m = new MediaPlayer();
                try {
                    m.setDataSource(outputFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    m.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m.start();
                Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
            }
        });
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv.set(binder.getService());
            //pass list
            musicSrv.get().setList(riffList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void getRiffList() {
        //retrieve riff info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                riffList.add(new Riffs(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }

    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicSrv.get().pausePlayer();
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.get().seek(pos);
    }

    @Override
    public void start() {
        musicSrv.get().go();
    }

    @Override
    public int getDuration() {
        if(musicSrv!=null && musicBound && musicSrv.get().isPng())
        return musicSrv.get().getDur();
        else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.get().isPng())
        return musicSrv.get().getPosn();
        else return 0;
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
        return musicSrv.get().isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    private void playNext(){
        musicSrv.get().playNext();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }

    private void playPrev(){
        musicSrv.get().playPrev();
        if(playbackPaused){
            setController();
            playbackPaused=false;
        }
        controller.show(0);
    }
}