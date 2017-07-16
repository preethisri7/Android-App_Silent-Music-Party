package de.tudarmstadt.informatik.tk.shhparty;

/**
 * Created by Admin on 1/3/2017.
 */

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class ViewHolder extends RecyclerView.ViewHolder {

  TextView songTitle,artist,vote;
  ImageView img;

  public ViewHolder(View itemView) {
    super(itemView);

    songTitle= (TextView) itemView.findViewById(R.id.songTitle);
    artist=(TextView) itemView.findViewById(R.id.songArtist);
    vote=(TextView) itemView.findViewById(R.id.songVote);
    img= (ImageView) itemView.findViewById(R.id.imageView);
  }

}
