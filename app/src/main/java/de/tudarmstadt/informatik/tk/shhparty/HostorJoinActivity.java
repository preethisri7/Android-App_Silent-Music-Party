package de.tudarmstadt.informatik.tk.shhparty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import de.tudarmstadt.informatik.tk.shhparty.host.SelectSongsActivity;
import de.tudarmstadt.informatik.tk.shhparty.member.PartyHome;
import de.tudarmstadt.informatik.tk.shhparty.member.SearchForParties;
import de.tudarmstadt.informatik.tk.shhparty.user.CreateProfile;

public class HostorJoinActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostor_join);
    }

    public void goToCreateUserProfile(View view){
        Intent goToUserProfileIntent=new Intent(this, CreateProfile.class);
        if(view.getTag()!=null){
            if (view.getTag().toString().equals("search")) {
                goToUserProfileIntent.putExtra("role", "member");
            }
        }

        startActivity(goToUserProfileIntent);
    }
    public void goToSelectSongs(View view){
        //// TODO: 11/15/2016 Transition to select songs activity
        Log.d("HostOrJoin:","Button click registered");
        Intent selectMusic=new Intent(this,SelectSongsActivity.class);
        selectMusic.putExtra("test","Ashwin");
        startActivity(selectMusic);

    }
    public void goToSelectParty(View view){
        //// TODO: 11/15/2016 Transition to select part event to join activity
        Log.d("HostorJoin","calling method to search for parties..");
        Intent toSelectParty=new Intent(this,SearchForParties.class);
        startActivity(toSelectParty);
    }

    public void goToPartyHome(View view){

        Log.d("HostorJoin","calling method to search for parties..");
        Intent toPartyHome=new Intent(this,PartyHome.class);
        startActivity(toPartyHome);
    }
}
