package de.tudarmstadt.informatik.tk.shhparty.music;

import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.HostUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;

/**
 * Created by rohit on 03-03-2017.
 */

public class MusicXpress extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {


    private MediaPlayer player; //media player

    private ArrayList<MusicBean> songs; // song list

    private int songPosn;   //current position

    public static final int FIRST_SONG = 0;

    private final IBinder musicBind = new MusicBinder();

    private boolean isPlayerPaused=false;
    private int pausedPosition;


    HostUtils hostUtilHandle=new HostUtils();

    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        songPosn = 0;

        //create player
        player = new MediaPlayer();
        initMusicPlayer();
        Log.v("MusicXpress Service","onCreate()");
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<MusicBean> theSongs){
        songs=theSongs;
    }

    public class MusicBinder extends Binder {
        public MusicXpress getService() {
            return MusicXpress.this;
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
        setList(CommonUtils.derivePlaylist());
        onPlay();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //start playback
        SharedBox.getDerivedPlaylist().get(songPosn).setVotes(0);
        for(int i=0;i<SharedBox.getThePlaylist().size();i++){
            if(SharedBox.getThePlaylist().get(i).equals(SharedBox.getDerivedPlaylist().get(songPosn))){
                SharedBox.thePlaylist.get(i).setVotes(0);
            }
        }
        Log.d("MusicXpress","In playlist"+SharedBox.getThePlaylist().get(songPosn).getMusicTitle()+":>"+SharedBox.getThePlaylist().get(songPosn).getVotes());
        hostUtilHandle.informAboutVoteReset();
        mediaPlayer.start();
    }

    // TODO : Always select the 1st song in the playlist to play
    public void setSong(int songIndex){
        songPosn=songIndex;
    }


    public void onStop(View view) {
        hostUtilHandle.buildAndSendStop();
        player.stop();
        setList(CommonUtils.derivePlaylist());
    }

    public void onPlay(){
        if(isPlayerPaused){
            player.seekTo(pausedPosition);
            player.start();
            hostUtilHandle.buildAndSendResume(pausedPosition);
            isPlayerPaused=false;
        }
        else if(!isPlayerPaused) {

            player.reset();
            setSong(FIRST_SONG); // Always play the 1st song in the playlist
            //get song
            MusicBean playSong = songs.get(songPosn);
            //get id
            long currSong = playSong.getMusicID();
            //playSong.ge
            //set uri
            Uri trackUri = ContentUris.withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong);
            String realPath = getRealPathFromURI(this, trackUri);
            Log.d("MusicXpress", "Real path is" + realPath);
            // TODO: 3/6/2017 Copy first song into webserver as asynctask
            // TODO: 3/6/2017 Send commandbean to clients
            hostUtilHandle.buildAndSendPlay(realPath,playSong.getMusicTitle());
            try {
                player.setDataSource(getApplicationContext(), trackUri);
            } catch (Exception e) {
                Log.e("MusicXpress Service", "Error setting data source", e);
            }

            player.prepareAsync();
        }
    }

    public void onPause(View view){
        hostUtilHandle.buildAndSendPause();
        isPlayerPaused=true;
        player.pause();
        pausedPosition=player.getCurrentPosition();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}