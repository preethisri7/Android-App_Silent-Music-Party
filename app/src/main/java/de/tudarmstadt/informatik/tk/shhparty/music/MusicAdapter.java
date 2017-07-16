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

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {


        public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView musicTrack;
        public TextView musicArtist;
        public ViewHolder(View itemView) {
            super(itemView);
            musicTrack=(TextView) itemView.findViewById(R.id.musicitem_track);
            //musicArtist=(TextView) itemView.findViewById(R.id.musicitem_artist);
        }
    }

    private ArrayList<MusicBean> listOfMusic=new ArrayList<MusicBean>();
    private Context con;
    private LayoutInflater musicInfo;

    public MusicAdapter(ArrayList<MusicBean> listOfMusic, Context con) {
        this.listOfMusic = listOfMusic;
        this.con = con;
    }

    public Context getCon() {
        return con;
    }

    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater=LayoutInflater.from(context);
        View musicView=inflater.inflate(R.layout.recycler_musiclist,parent,false);

        ViewHolder resultHolder=new ViewHolder(musicView);
        return resultHolder;
    }

    @Override
    public void onBindViewHolder(MusicAdapter.ViewHolder holder, int position) {

        MusicBean musicitem=listOfMusic.get(position);
        TextView trackTextView=holder.musicTrack;
        trackTextView.setText(musicitem.getMusicTitle());


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
