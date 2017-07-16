package de.tudarmstadt.informatik.tk.shhparty.member;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;
import de.tudarmstadt.informatik.tk.shhparty.music.RemainingMusicAdapter;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.ItemClickSupport;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;


public class MusicLibrary extends Fragment {

    private ArrayList<MusicBean> remainingSongs=new ArrayList<>();
    private ArrayList<String> selectedMusicIDs = new ArrayList<>();

    private ArrayList<MusicBean> selectedSongs=new ArrayList<MusicBean>();

    private static final String LOG_TAG="SHH_MusicFrag";
    public static RecyclerView rv;
    public MusicLibrary() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_music_library, container, false);
        remainingSongs= CommonUtils.deriveRemainingSongs();
        rv = (RecyclerView) rootView.findViewById(R.id.remainMusRecycler);

        RemainingMusicAdapter rmusicAdapter=new RemainingMusicAdapter(remainingSongs,this.getActivity());
        rv.setAdapter(rmusicAdapter);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            //retrieves the specific musicitem, sets inplaylist true and adds to parcel list
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                // do it
                Log.d(LOG_TAG,"Clicked!");
                MusicBean selectedMusic=remainingSongs.get(position);
                if(!v.isSelected()){
                    v.setSelected(true);

                //MusicBean selectedMusic=musicAdapter.getItem(position);

               // String selectedMusicId=Long.toString(selectedMusic.getMusicID());
               // selectedMusicIDs.add(selectedMusicId);
                selectedSongs.add(selectedMusic);
                }else if(v.isSelected()){
                    v.setSelected(false);
                    selectedSongs.remove(selectedMusic);
                }
                //commenting to fix the duplicate entry problem in playlist
                // musicInfoToShare.add(selectedMusic);
            }
        });


        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        Button requestAdditionButton=(Button)rootView.findViewById(R.id.requestAddToPlaylist);
        requestAdditionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestToAddSongs(v);
            }
        });
        return rootView;
    }

/*    private ArrayList<MusicBean> getRemainingSongs()
    {
        ArrayList<MusicBean> allSongs = new ArrayList<MusicBean>();
        ArrayList<MusicBean> remainingSongs = new ArrayList<MusicBean>();

        allSongs=SharedBox.getThePlaylist();
        for(int i=0;i<allSongs.size();i++){
            if(!(allSongs.get(i).isInPlayist()))
                remainingSongs.add(allSongs.get(i));
        }
        return remainingSongs;
    }*/

    public void requestToAddSongs(View view){
        new RequestToAddSongs().execute(selectedSongs);
        //client.sendRequestToAddSongs(selectedMusicIDs);
        Log.d(LOG_TAG,"Fired AsyncTask to request to add songs");
    }

    private class RequestToAddSongs extends AsyncTask<ArrayList<MusicBean>,Integer,String>{
        @Override
        protected String doInBackground(ArrayList<MusicBean>... requestedSongs) {
            SharedBox.getClient().sendRequestToAddSongs(requestedSongs[0]);
            return "Requested addition of tracks";
        }
    }
   }
