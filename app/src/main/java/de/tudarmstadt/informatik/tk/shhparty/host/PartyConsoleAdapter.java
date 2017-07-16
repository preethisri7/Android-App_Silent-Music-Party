package de.tudarmstadt.informatik.tk.shhparty.host;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import de.tudarmstadt.informatik.tk.shhparty.chat.ChatFragment;

/**
 * Created by Ashwin on 1/31/2017.
 */

public class PartyConsoleAdapter extends FragmentPagerAdapter {

    int numOfTabs;

    public PartyConsoleAdapter(FragmentManager fm, int numofTabs) {
        super(fm);
        this.numOfTabs=numofTabs;

    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                HostPlaylistFragment hostplfrag = new HostPlaylistFragment();
                return hostplfrag;
            case 1:
                MembersListFragment memlistfrag = new MembersListFragment();
                return memlistfrag;
            case 2:
                ChatFragment chatfrag = new ChatFragment();
                return chatfrag;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
