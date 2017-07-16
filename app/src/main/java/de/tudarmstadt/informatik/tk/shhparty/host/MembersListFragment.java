
package de.tudarmstadt.informatik.tk.shhparty.host;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.member.MemberBean;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicAdapter;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;
import de.tudarmstadt.informatik.tk.shhparty.sensing.ActivityRecognizedService;
import de.tudarmstadt.informatik.tk.shhparty.utils.HostUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.ItemClickSupport;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;


public class MembersListFragment extends Fragment {

    public static ArrayList<MemberBean> listOfMembers=new ArrayList<MemberBean>();
    private static final String LOG_TAG="Shh_MemFragment";

    public static MemberListAdapter memListAdapter;
    public static RecyclerView rv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_members_list, container, false);
        // Inflate the layout for this fragment
        MemberBean dummyMem1=new MemberBean();
        MemberBean dummyMem2=new MemberBean();
        dummyMem1.setName("Ashwin");
        dummyMem2.setName("Preneesh");
        listOfMembers.add(dummyMem1);
        listOfMembers.add(dummyMem2);
        rv = (RecyclerView) rootView.findViewById(R.id.membersRecycler);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        memListAdapter=new MemberListAdapter(getActivity(),listOfMembers);
        rv.setAdapter(memListAdapter);

        ItemClickSupport.addTo(rv).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            //retrieves the specific musicitem, sets inplaylist true and adds to parcel list
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                // do it
                Log.d(LOG_TAG,"Clicked");

                showMessageOKCancel("Do you want to kick this person out?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    SharedBox.nameSocketMapping.get(listOfMembers.get(position).getName()).close();
                                    listOfMembers.remove(position);
                                    SharedBox.nameSocketMapping.remove(listOfMembers.get(position).getName());
                                    memListAdapter.notifyDataSetChanged();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });

        return rootView;

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("Yes", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


}

