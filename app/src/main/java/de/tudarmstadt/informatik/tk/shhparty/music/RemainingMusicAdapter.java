package de.tudarmstadt.informatik.tk.shhparty.music;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.R;

/**
 * Created by Ashwin on 11/23/2016.
 */

public class RemainingMusicAdapter extends RecyclerView.Adapter<RemainingMusicAdapter.ViewHolder> {


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView musicTrack;
        public TextView musicArtist;
        public ViewHolder(View itemView) {
            super(itemView);
            musicTrack=(TextView) itemView.findViewById(R.id.toAdd_musictrack);
            //musicArtist=(TextView) itemView.findViewById(R.id.toAdd_musicartist);
        }
    }

    private ArrayList<MusicBean> listOfMusic=new ArrayList<MusicBean>();
    private Context con;
    private LayoutInflater musicInfo;

    public RemainingMusicAdapter(ArrayList<MusicBean> listOfMusic, Context con) {
        this.listOfMusic = listOfMusic;
        this.con = con;
    }

    public Context getCon() {
        return con;
    }

    @Override
    public RemainingMusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        View addToplistview= LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_addtoplaylist,parent,false);
        ViewHolder resultHolder=new ViewHolder(addToplistview);
        return resultHolder;
    }

    @Override
    public void onBindViewHolder(RemainingMusicAdapter.ViewHolder holder, int position) {

        MusicBean musicitem=listOfMusic.get(position);
        TextView trackTextView=holder.musicTrack;
       // TextView artistTextView=holder.musicArtist;
        trackTextView.setText(musicitem.getMusicTitle());
       // artistTextView.setText(musicitem.getArtist());


    }

    public void updatePlaylistData(ArrayList<MusicBean> refreshedPlaylist){
        // this.songs=refreshedPlaylist;
        listOfMusic.clear();
        listOfMusic.addAll(refreshedPlaylist);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount()
    {
        return listOfMusic.size();
    }


    public MusicBean getItem(int position){
        return listOfMusic.get(position);
    }
}
