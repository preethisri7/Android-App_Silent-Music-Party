package de.tudarmstadt.informatik.tk.shhparty.host;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicXpress;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicXpress.MusicBinder;
import de.tudarmstadt.informatik.tk.shhparty.sensing.ActivityRecognizedService;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;

/**
 * Created by Ashwin on 1/31/2017.
 */

public class PartyConsole extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;

    private MusicXpress musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    Button playButton;
    Button pauseButton;
    TextView songName;
    boolean resuming=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("PartyConsole","onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_console);

        Toolbar toolbar = (Toolbar) findViewById(R.id.consoletoolbar);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.consoletab_layout);

        tabLayout.addTab(tabLayout.newTab().setText("Playlist"));
        tabLayout.addTab(tabLayout.newTab().setText("Members"));
        tabLayout.addTab(tabLayout.newTab().setText("Party Talk"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.consolepager);
        final PartyConsoleAdapter adapter = new PartyConsoleAdapter(
                getFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Activity recognition code
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        playButton = (Button) findViewById(R.id.playbutton);
        pauseButton = (Button) findViewById(R.id.pausebutton);
        songName = (TextView) findViewById(R.id.songname);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v("PartyConsole","Connected to mAPICLIENT");
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 6000, pendingIntent ); // (GoogleApiClient client, long detectionIntervalMillis, PendingIntent callbackIntent)
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.v("PartyConsole","onServiceConnected()");
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(CommonUtils.derivePlaylist());
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicXpress.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }



    // Songs can be played onClick with this method
    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        musicSrv.onPlay();
    }

    @Override
      public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.options_host,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_endpartyitem:
                SharedBox.getServer().disconnectServer();
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory( Intent.CATEGORY_HOME );
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(musicConnection);
        musicSrv=null;
        super.onDestroy();
    }

    public void onStop(View view) {
        Log.v("PartyConsole","OnStop() clicked");
        //// TODO: 3/6/2017 Fire network call to stop
        musicSrv.onStop(view);
        pauseButton.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.VISIBLE);
    }

    public void onPlay(View view){
        Log.v("PartyConsole","OnPlay() clicked");
        playButton.setVisibility(View.INVISIBLE);
        pauseButton.setVisibility(View.VISIBLE);

        if(!resuming){
            songName.setText(CommonUtils.derivePlaylist().get(MusicXpress.FIRST_SONG).getMusicTitle());
        }
        // TODO: 3/5/2017 Fire network call to play 
        musicSrv.onPlay();
        resuming=false;

    }

    public void onPause(View view){
        Log.v("PartyConsole","OnPause() clicked");
        pauseButton.setVisibility(View.INVISIBLE);
        playButton.setVisibility(View.VISIBLE);
        // TODO: 3/5/2017 Fire network call to pause 
        musicSrv.onPause(view);
        resuming=true;

    }

}