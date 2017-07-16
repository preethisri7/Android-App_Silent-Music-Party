package de.tudarmstadt.informatik.tk.shhparty;

/**
 * Created by Admin on 1/3/2017.
 */

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

public class SongRecyclerAdapter extends RecyclerView.Adapter<ViewHolder> {

  Context c;
  ArrayList<MusicBean> songs;
  private LayoutInflater mLayoutInflater;
  private SortedList <MusicBean> mSongs;
  Map<String, String> map = new HashMap<String, String>();
  private String LOG_TAG="Shh_SortSongsAdapter";

  public SongRecyclerAdapter(Context c, ArrayList<MusicBean> songs) {
    this.c = c;
   this.songs = songs;
    mLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mSongs = new SortedList<>(MusicBean.class, new SongListCallback());
    mSongs.addAll(songs);
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
    ViewHolder holder = new ViewHolder(v);
    return holder;


  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, final int position) {

    final MusicBean songs = mSongs.get(position);
    holder.songTitle.setText(songs.getMusicTitle());
    holder.vote.setText(String.valueOf(songs.getVotes()));
    holder.artist.setText(songs.getArtist());
    holder.img.setImageResource(R.drawable.like);

    final String title = songs.getMusicTitle();
    final int votes = songs.getVotes();
    final String artist = songs.getArtist();
    final long musicId = songs.getMusicID();
    final boolean playlist = songs.isInPlayist();

   }

  //Ashwin's change - to refresh playlist on update call from server
  public void updatePlaylistData(ArrayList<MusicBean> refreshedPlaylist){
      songs.clear();
      songs.addAll(refreshedPlaylist);
      mSongs.clear();
      mSongs.addAll(refreshedPlaylist);
      notifyDataSetChanged();

  }

  @Override
  public int getItemCount() {
    return songs.size();
  }

  private class SongListCallback extends SortedList.Callback<MusicBean> {

    @Override
    public int compare(MusicBean s1, MusicBean s2) {
      int val1=Integer.valueOf(s1.getVotes());
      int val2=Integer.valueOf(s2.getVotes());
      return val2 > val1 ? 1 : (val2 < val1 ? -1 : 0);


      //  return Integer.valueOf(s2.getVote().compareTo(s1.getVote()));
    }

    @Override
    public void onInserted(int position, int count) {
      notifyItemInserted(position);
    }

    @Override
    public void onRemoved(int position, int count) {
      notifyItemRemoved(position);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
    }

    @Override
    public void onChanged(int position, int count) {
    }

    @Override
    public boolean areContentsTheSame(MusicBean oldItem, MusicBean newItem) {
      return false;
    }

    @Override
    public boolean areItemsTheSame(MusicBean item1, MusicBean item2) {
      return false;
    }

  }

}

