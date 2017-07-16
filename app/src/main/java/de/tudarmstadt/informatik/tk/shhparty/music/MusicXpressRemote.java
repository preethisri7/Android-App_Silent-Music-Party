package de.tudarmstadt.informatik.tk.shhparty.music;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.member.PartyHome;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommandBean;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;

/**
 * Created by Ashwin
 */

public class MusicXpressRemote extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public static boolean isRunningAlready=false;

    private MediaPlayer player; //media player

   // private ArrayList<MusicBean> songs; // song list

  //  private int songPosn;   //current position

    public static final int FIRST_SONG = 0;

    private final IBinder musicBind = new RemoteMusicBinder();

    private long bufferStartTime;
    private long bufferEndTime;

    public void onCreate() {
        //create the service
        super.onCreate();
        isRunningAlready=true;
        //initialize position
     //   songPosn = 0;

        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        Log.v("MusicXpress Remote","onCreate()");
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

  /*  public void setList(ArrayList<MusicBean> theSongs){
        songs=theSongs;
    }
*/
    public class RemoteMusicBinder extends Binder {
        public MusicXpressRemote getService() {
            return MusicXpressRemote.this;
        }
    }


    @Nullable
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


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

        Log.d("MusicXpressRemote","Error in preparing"+i+":>"+i1);
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        bufferEndTime=System.currentTimeMillis();
        long elapsedTime=bufferEndTime-bufferStartTime;
        mediaPlayer.seekTo((int)(long)elapsedTime+300);
        mediaPlayer.start();
    }

    // TODO : No local list, just stream from host
    /*public void setSong(int songIndex){
        songPosn=songIndex;
    }*/


    public void onStop() {
        player.stop();
    }

    public void onPlay(){
        bufferStartTime=System.currentTimeMillis();

        player.reset();
        //setSong(FIRST_SONG); //
        //get song
        //MusicBean playSong = songs.get(songPosn);
        //get id
        //long currSong = playSong.getMusicID();
        //set uri
        /*Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);*/
        // TODO: 3/6/2017 Get the command Bean and set data source, prepareasync
        CommandBean receivedCommand= SharedBox.getReceivedCommand();
        String trackUrl=receivedCommand.getURL();
        String trackName=receivedCommand.getTrackName();
        PartyHome.songNameInHome.setText(trackName);

        try{
            player.setDataSource(getApplicationContext(),Uri.parse(trackUrl));

        }
        catch(Exception e){
            Log.e("MusicXpress Remote", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void onPause(){
        player.pause();
    }
    public void onResume(int continuePosition){
        player.seekTo(continuePosition+300);
        player.start();

    }
}