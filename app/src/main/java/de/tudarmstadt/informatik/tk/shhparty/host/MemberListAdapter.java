package de.tudarmstadt.informatik.tk.shhparty.host;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.p2p.WifiP2pInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.member.MemberBean;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

/**
 * Created by Ashwin on 1/17/2017.
 */

public class MemberListAdapter extends RecyclerView.Adapter<MemberHolder> {

    Context con;
    private LayoutInflater inflater;
    private ArrayList<MemberBean> listOfMembers=new ArrayList<MemberBean>();

    public MemberListAdapter(Activity activity, ArrayList<MemberBean> listOfMembers) {
        this.con=activity;
        this.listOfMembers = listOfMembers;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.memberitem,null);
        MemberHolder holder = new MemberHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int position) {
        holder.profileNameView.setText(listOfMembers.get(position).getName());
        if(listOfMembers.get(position).getBitmapdata()!=null) {
            byte[] bitmapdata=listOfMembers.get(position).getBitmapdata();
            Bitmap retrievedImage= BitmapFactory.decodeByteArray(bitmapdata,0,bitmapdata.length);

            holder.profilePicView.setImageBitmap(retrievedImage);
        }
        else{
            holder.profilePicView.setImageResource(R.drawable.profilepic_dummy);
        }

    }

    @Override
    public int getItemCount() {
        return listOfMembers.size();
    }

    public void updateMemberList(ArrayList<MemberBean> updatedMemList){
        // this.songs=refreshedPlaylist;
        listOfMembers.clear();
        listOfMembers.addAll(updatedMemList);
        notifyDataSetChanged();

    }
}
