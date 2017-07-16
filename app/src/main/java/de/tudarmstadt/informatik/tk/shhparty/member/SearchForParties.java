package de.tudarmstadt.informatik.tk.shhparty.member;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.informatik.tk.shhparty.AppTimer;
import de.tudarmstadt.informatik.tk.shhparty.GoodbyeActivity;
import de.tudarmstadt.informatik.tk.shhparty.SongRecyclerAdapter;
import de.tudarmstadt.informatik.tk.shhparty.chat.ChatAdapter;
import de.tudarmstadt.informatik.tk.shhparty.chat.ChatFragment;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicXpress;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicXpressRemote;
import de.tudarmstadt.informatik.tk.shhparty.music.RemainingMusicAdapter;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommandBean;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;
import de.tudarmstadt.informatik.tk.shhparty.host.PartyHostServer;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;
import de.tudarmstadt.informatik.tk.shhparty.wifip2p.ConnectionTemplate;
import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.wifip2p.WiFiDirectPulseChecker;

import static android.content.ContentValues.TAG;

public class SearchForParties extends ConnectionTemplate implements WifiP2pManager.ChannelListener,WifiP2pManager.ConnectionInfoListener,Handler.Callback {

    private final IntentFilter wifiStatesIntentFilter=new IntentFilter();
    private WifiP2pManager.Channel channel;
    private WifiP2pManager p2pManager;
    private boolean channelRetry=false;
    private boolean isWifiP2PEnabled=false;
    public static final String SERVICE_INSTANCE="_shhpartymusic";
    private List<PartyServicesBean> partiesList =new ArrayList<PartyServicesBean>();
    private PartyListAdapter partyListAdapter;
    private static final String LOG_TAG="SHH_SearchParties";

    private PartyMemberClient clientThread;

    private WiFiDirectPulseChecker p2ppulsechecker;

    private WifiP2pDnsSdServiceRequest serviceRequest;

    private ListView listOfPartiesView;
    private Handler handler=new Handler(this);
    private AppTimer timer;

    PartyServicesBean partyInfo;

    private boolean alreadyRendered=false;

    //MusicXpress Remote Part
    private MusicXpressRemote musicSrv;
    private boolean musicBound=false;
    private boolean alreadyInitialized=false;
    Intent playIntent=null;
    ServiceConnection musicConnection=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_for_parties);
       /* findViewById(R.id.listOfParties).setVisibility(View.INVISIBLE);
        findViewById(R.id.toolbar).setVisibility(View.INVISIBLE);
        findViewById(R.id.tab_layout).setVisibility(View.INVISIBLE);
        findViewById(R.id.pager).setVisibility(View.INVISIBLE);*/

        wifiStatesIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        wifiStatesIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        wifiStatesIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        wifiStatesIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        p2pManager= (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel=p2pManager.initialize(this,getMainLooper(),null);

      /*  listOfPartiesView= (ListView)findViewById(R.id.listOfParties);
        partyListAdapter=new PartyListAdapter(this,R.id.listOfParties,R.id.list_item_partyservice,partiesList);
        listOfPartiesView.setAdapter(partyListAdapter);
*/
        // TODO: 1/24/2017 Make this an Asynchronous call 
        lookForParties();



     /*   listOfPartiesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PartyServicesBean connectingPartyInfo=(PartyServicesBean) listOfPartiesView.getItemAtPosition(position);
                joinParty(connectingPartyInfo);
            }
        });*/

    }


    @Override
    public void onChannelDisconnected() {
        if(p2pManager!=null&&!channelRetry){
            Toast.makeText(this,"Channel lost. Trying again",Toast.LENGTH_LONG).show();
            channelRetry=true;
            channel=p2pManager.initialize(this,getMainLooper(),null);
        }
        else{
            Toast.makeText(this,"Something seems very wrong. Try disabling and re-enabling Wifi P2p",Toast.LENGTH_LONG).show();
        }
    }

    public void setIsWifiP2pEnabled(boolean wifip2pflag){
        this.isWifiP2PEnabled=wifip2pflag;
    }

    @Override
    protected void onResume(){
        super.onResume();
        p2ppulsechecker=new WiFiDirectPulseChecker(channel,p2pManager,this);
        registerReceiver(p2ppulsechecker,wifiStatesIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(p2ppulsechecker);
    }

    public void lookForParties(){

        p2pManager= (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel=p2pManager.initialize(this,getMainLooper(),null);

        p2pManager.setDnsSdResponseListeners(channel,
                new WifiP2pManager.DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                                                        String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?
                        Log.d(LOG_TAG,"Found a service, Is it ours?");
                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                                //Future code for multiple party events
                                //List the party events in the UI
                                //ListView partyListToUpdate=listOfPartiesView;
                                //PartyListAdapter adapter= (PartyListAdapter) listOfPartiesView.getAdapter();
                                partyInfo = new PartyServicesBean();
                                partyInfo.wifiP2PDevice = srcDevice;
                                partyInfo.partyName=instanceName;
                                partyInfo.serviceRegistrationType = registrationType;
                                //adapter.add(partyInfo);
                                //adapter.notifyDataSetChanged();*/
                                Log.d(TAG, "onBonjourServiceAvailable "
                                        + instanceName+ "Now connecting to"+srcDevice);
                                joinParty(partyInfo);
                            }
                        }


                }, new WifiP2pManager.DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG,
                                device.deviceName + " is ");
                    }
                });

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        // After attaching listeners, create a service request and initiate
        // discovery.

        p2pManager.clearServiceRequests(channel,new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                p2pManager.addServiceRequest(channel, serviceRequest,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                p2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {

                                        Log.d(LOG_TAG,"Discovery initiated successfully..");
                                    }

                                    @Override
                                    public void onFailure(int arg0) {
                                        Log.d(LOG_TAG,"Discovery initiation failed-reason code is:"+arg0);

                                    }
                                });


                            }

                            @Override
                            public void onFailure(int arg0) {
                                //nothing done as of now
                            }
                        });
            }

            @Override
            public void onFailure(int reason) {

            }
        });


    }

    public void joinParty(final PartyServicesBean partyInfo){
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = partyInfo.wifiP2PDevice.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent=0;
        if (serviceRequest != null)
            p2pManager.removeServiceRequest(channel, serviceRequest,
                    new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });

        p2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                //
                Log.d(LOG_TAG,"Connect call succcess");
            }

            @Override
            public void onFailure(int errorCode) {
                Log.d(LOG_TAG,"Connect call failed..Should retry"+errorCode);

                joinParty(partyInfo);
                //
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        if(info.groupFormed&&info.isGroupOwner){
            Log.d(LOG_TAG,"Connected but client side became group owner..Shit!");
        }
        else if(info.groupFormed){
            if (this.clientThread == null)
            {
                Thread client = new PartyMemberClient(this.handler,
                        info.groupOwnerAddress,
                        timer);
                client.start();
            }

        }

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what)
        {
           /* case PartyMemberClient.EVENT_RECEIVE_MSG:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf);

                // interpret the command
                String[] cmdString = readMessage
                        .split(PartyHostServer.CMD_DELIMITER);

                if (cmdString[0].equals(PartyHostServer.PLAY_CMD)
                        && cmdString.length > 3)
                {
                    try
                    {
                        Log.d(LOG_TAG,"Supposed to call play from here");
                      *//*  playMusic(cmdString[1], Long.parseLong(cmdString[2]),
                                Integer.parseInt(cmdString[3]));*//*
                    }
                    catch (NumberFormatException e)
                    {
                        Log.e(TAG,
                                "Could not convert to a proper time for these two strings: "
                                        + cmdString[2] + " and " + cmdString[3],
                                e);
                    }
                }
                else if (cmdString[0].equals(PartyHostServer.STOP_CMD)
                        && cmdString.length > 0)
                {
                   // ((SpeakerFragmentListener) getActivity()).stopMusic();
                }

                Log.d(TAG, readMessage);

                // Toast.makeText(mContentView.getContext(),
                // "Received message: " + readMessage, Toast.LENGTH_SHORT)
                // .show();
                break;*/

            case PartyMemberClient.CLIENT_CALLBACK:
                clientThread = (PartyMemberClient) msg.obj;
                Log.d(TAG, "Retrieved client thread..Putting it in sharedbox");
                SharedBox.setClient(clientThread);
                break;

            case PartyMemberClient.PLAYLIST_RECEIVE:
                Log.d(LOG_TAG,"Callback received, shud call activity");
                ArrayList<MusicBean> musicInfo=(ArrayList<MusicBean>) msg.obj;
                SharedBox.setThePlaylist(musicInfo);
                    if(!alreadyRendered) {
                        Intent topartyHome = new Intent(this, PartyHome.class);
                        startActivity(topartyHome);
                        alreadyRendered = true;
                    }else{
                        SongRecyclerAdapter plAdapter=(SongRecyclerAdapter) PlaylistFragment.rv.getAdapter();
                        plAdapter.updatePlaylistData(CommonUtils.derivePlaylist());
                        RemainingMusicAdapter rmAdapter= (RemainingMusicAdapter) MusicLibrary.rv.getAdapter();
                        rmAdapter.updatePlaylistData(CommonUtils.deriveRemainingSongs());

                    }

                break;
            case PartyMemberClient.CHATMESSAGE_RECEIVE:
                Log.d(LOG_TAG,"Callback received, received chat message");
               // Intent newMessage=new Intent(this, PartyHome.class);
               // Fragment chatFrag=getFragmentManager().findFragmentById(R.id.chatFragment);
               // getFragmentManager().beginTransaction().detach(chatFrag).attach(chatFrag).commit();
                ChatFragment.chatlist.add(SharedBox.getMessage());
                ChatFragment.chatAdapter.notifyDataSetChanged();

                break;
              //  startActivity(newMessage);
            case PartyMemberClient.COMMAND_RECEIVED:
                Log.d(LOG_TAG,"Callback received, got a command");

                // TODO: 3/6/2017 Bind to musicremote service and start onplay also

                if(!MusicXpressRemote.isRunningAlready) {
                     musicConnection = new ServiceConnection() {

                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            Log.v("PartyConsole", "onServiceConnected()");
                            MusicXpressRemote.RemoteMusicBinder remoteBinder = (MusicXpressRemote.RemoteMusicBinder) service;
                            //get service
                            musicSrv = remoteBinder.getService();
                            musicBound = true;
                            alreadyInitialized = true;
                            CommandBean receivedCommand = SharedBox.getReceivedCommand();
                            if (receivedCommand.getCommand().equals("PLAY")) {
                                Log.v(LOG_TAG, "Calling onPlay of MusicXpressRemote");

                                musicSrv.onPlay();

                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            musicBound = false;
                        }
                    };
                }
                if(MusicXpressRemote.isRunningAlready) {
                    CommandBean receivedCommand = SharedBox.getReceivedCommand();
                    if (receivedCommand.getCommand().equals("PLAY")) {
                        Log.v(LOG_TAG, "Already running-Calling onPlay of MusicXpressRemote");

                        musicSrv.onPlay();

                    }
                    else if(receivedCommand.getCommand().equals("PAUSE")){
                        Log.v(LOG_TAG,"Already running-Calling onPause of MusicXpressRemote");
                        musicSrv.onPause();
                    }
                    else if(receivedCommand.getCommand().equals("RESUME")){
                        Log.v(LOG_TAG,"Already running- Calling onResume of MusicXpressRemote");
                        musicSrv.onResume((int)(long)receivedCommand.getStartPosition());
                    }
                    else if(receivedCommand.getCommand().equals("STOP")){
                        Log.v(LOG_TAG,"Already running- Calling onResume of MusicXpressRemote");
                        musicSrv.onStop();
                    }
                }
                    if(playIntent==null){
                        playIntent = new Intent(this, MusicXpressRemote.class);
                        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                        startService(playIntent);
                    }

                break;
            case PartyMemberClient.KICKED_OUT:
                Intent sayGoodbye=new Intent(this, GoodbyeActivity.class);
                startActivity(sayGoodbye);
                break;

            default:
                Log.d(TAG, "I thought we heard something? Message type: "
                        + msg.what);
                break;
        }
        return true;
    }

 /*  private MediaPlayer mp=new MediaPlayer();
    private AppTimer musicTimer = null;


    public void playMusic(String url, long startTime, int startPos)
    {
        // This part of the code is time sensitive, it should be done as fast as
        // possible to avoid the delay in the music
        try
        {
            //mp.reset();
            mp.setDataSource(url);

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);

            mp.prepare();
            // TODO: make sure we have buffered REALLY
            // buffered the music, currently this is a big
            // HACK and takes a lot of time. We can do
            // better!
            mp.start();
            Log.d(LOG_TAG,"Mediaplayer setup with Data source-called start()");

          //  musicTimer = mActivity.retrieveTimer();

            // let the music timer determine when to play the future playback
          //  musicTimer.playFutureMusic(mp, startTime, startPos);




        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "IllegalArgumentException");
        }
        catch (IllegalStateException e)
        {
            Log.e(TAG, "illegalStateException");
        }
        catch (IOException e)
        {
            Log.e(TAG, "IOexception");
        }
    }*/

  /*  public void setupPartyHome(){

            findViewById(R.id.listOfParties).setVisibility(View.GONE);
            findViewById(R.id.partysearchtext).setVisibility(View.GONE);
            findViewById(R.id.partysearchprog).setVisibility(View.GONE);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);


            TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
            tabLayout.addTab(tabLayout.newTab().setText("Playlist"));
            tabLayout.addTab(tabLayout.newTab().setText("Music Library"));
            tabLayout.addTab(tabLayout.newTab().setText("Party Talk"));
            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

            final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            final PartyHomeAdapter adapter = new PartyHomeAdapter(
                    getFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(adapter);
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


        alreadyRendered=true;
    }*/


}
