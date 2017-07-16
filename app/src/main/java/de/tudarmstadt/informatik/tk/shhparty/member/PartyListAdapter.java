package de.tudarmstadt.informatik.tk.shhparty.member;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Ashwin on 12/3/2016.
 */

public class PartyListAdapter extends ArrayAdapter<PartyServicesBean> {

    private List<PartyServicesBean> items;

    public PartyListAdapter(Context context, int resource,
                              int textViewResourceId, List<PartyServicesBean> items) {
        super(context, resource, textViewResourceId, items);
        this.items = items;
    }


}
