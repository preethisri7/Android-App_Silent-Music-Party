package de.tudarmstadt.informatik.tk.shhparty.wifip2p;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Ashwin on 12/3/2016.
 */

public abstract class ConnectionTemplate extends AppCompatActivity {

    private boolean isWifiP2PEnabled=false;
    public void setIsWifiP2pEnabled(boolean wifip2pflag){
        this.isWifiP2PEnabled=wifip2pflag;
    }
}
