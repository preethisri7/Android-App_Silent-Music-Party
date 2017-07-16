package de.tudarmstadt.informatik.tk.shhparty.host;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.tudarmstadt.informatik.tk.shhparty.R;

/**
 * Created by Ashwin on 3/3/2017.
 */

public class MemberHolder extends RecyclerView.ViewHolder {

    ImageView profilePicView;
    TextView profileNameView;

    public MemberHolder(View itemView) {
        super(itemView);

        profileNameView= (TextView) itemView.findViewById(R.id.profileNameView);
        profilePicView= (ImageView) itemView.findViewById(R.id.profilePicView);
    }
}
