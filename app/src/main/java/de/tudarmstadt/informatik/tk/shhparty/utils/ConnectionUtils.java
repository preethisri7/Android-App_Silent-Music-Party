package de.tudarmstadt.informatik.tk.shhparty.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by Ashwin on 12/2/2016.
 */

public class ConnectionUtils {

    private static final String LOG_TAG="SHH_ConnectionUtils";

    public static int getPort(Context context) {
        int localPort = CommonUtils.getInt(context, "localport");
        if (localPort < 0) {
            localPort = getNextFreePort();
            CommonUtils.saveInt(context, "localport", localPort);
        }
        return localPort;
    }

    public static int getNextFreePort() {
        int localPort = -1;
        try {
            ServerSocket s = new ServerSocket(0);
            localPort = s.getLocalPort();

            //closing the port
            if (s != null && !s.isClosed()) {
                s.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.v(LOG_TAG, Build.MANUFACTURER + ": free port requested: " + localPort);

        return localPort;
    }

}
