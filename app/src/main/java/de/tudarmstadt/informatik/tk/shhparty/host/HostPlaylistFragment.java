package de.tudarmstadt.informatik.tk.shhparty.host;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.SongRecyclerAdapter;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;
import de.tudarmstadt.informatik.tk.shhparty.sensing.ActivityRecognizedService;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.HostUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.ItemClickSupport;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;

/**
 * Created by Ashwin on 1/31/2017.
 */

public class HostPlaylistFragment extends Fragment {

    private static final String LOG_TAG="Shh_hostplfragment";
    private ArrayList<MusicBean> allSongs=new ArrayList<MusicBean>();
    private ArrayList<String> votedSongs=new ArrayList<String>();
    public static RecyclerView rv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.fragment_playlist,null);

        allSongs= CommonUtils.derivePlaylist();
        rv = (RecyclerView) rootView.findViewById(R.id.mRecyclerCrime);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        SongRecyclerAdapter songAdapter=new SongRecyclerAdapter(this.getActivity(),allSongs);
        rv.setAdapter(songAdapter);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            //retrieves the specific musicitem, sets inplaylist true and adds to parcel list
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                // do it
                    if(ActivityRecognizedService.dancing) {
                        Log.d(LOG_TAG, "Clicked!");
                        MusicBean selectedMusic = allSongs.get(position);
                        votedSongs.add(Long.toString(selectedMusic.getMusicID()));

                        int voteCount = selectedMusic.getVotes();
                        //voteCount++;
                        selectedMusic.setVotes(voteCount);
                        HostUtils.updateTheVotes(selectedMusic);
                        //Asynctask to broadcast music
                        new BroadCastMusicInfo().execute(selectedMusic);
                    }else{
                        Toast.makeText(getActivity(),"You need to DANCE to be able to vote!",Toast.LENGTH_LONG).show();
                    }


            }
        });

        return rootView;
    }

/*
    private ArrayList<MusicBean> getPlaylistSongs()
    {
        ArrayList<MusicBean> allSongs = new ArrayList<MusicBean>();
        ArrayList<MusicBean> playlist = new ArrayList<MusicBean>();
        allSongs=SharedBox.getThePlaylist();
        for(int i=0;i<allSongs.size();i++){
            if(allSongs.get(i).isInPlayist())
                playlist.add(allSongs.get(i));
        }
        return playlist;
    }*/

    private class BroadCastMusicInfo extends AsyncTask<MusicBean,Integer,String>{

        @Override
        protected String doInBackground(MusicBean... params) {
            SharedBox.getServer().broadcastMusicInfo(SharedBox.getThePlaylist());
            return "Broadcasted music info";
        }
    }
}
