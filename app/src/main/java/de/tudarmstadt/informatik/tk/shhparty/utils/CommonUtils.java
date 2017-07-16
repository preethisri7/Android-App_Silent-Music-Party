package de.tudarmstadt.informatik.tk.shhparty.utils;

import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

/**
 * Created by Ashwin on 12/2/2016.
 */

public class CommonUtils {

    public static void saveInt(Context cxt, String key, int value) {
        SharedPreferences.Editor prefsEditor = cxt.getSharedPreferences("shhhost", Context.MODE_PRIVATE).edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
    }

    public static int getInt(Context cxt, String key) {
        SharedPreferences prefs = cxt.getSharedPreferences("shhhost", Context.MODE_PRIVATE);
        int val = prefs.getInt(key, -1);
        return val;
    }

    public static String getWiFiIPAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = getDottedDecimalIP(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    public static String getDottedDecimalIP(int ipAddr) {

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddr = Integer.reverseBytes(ipAddr);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddr).toByteArray();

        //convert to dotted decimal notation:
        String ipAddrStr = getDottedDecimalIP(ipByteArray);
        return ipAddrStr;
    }

    public static String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    public static void copyFile(File src, File dst) throws IOException
    {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
        {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static boolean isMarshMallow(){
        return (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M);
    }

    //Preneesh's part begins

    private static DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");


    public static String getCurrentTime() {

        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate() {

        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

    //Preneesh's part ends

    public static ArrayList<MusicBean> derivePlaylist(){
        ArrayList<MusicBean> allSongs = new ArrayList<MusicBean>();
        ArrayList<MusicBean> playlist = new ArrayList<MusicBean>();
        allSongs=SharedBox.getThePlaylist();
        for(int i=0;i<allSongs.size();i++){
            if(allSongs.get(i).isInPlayist())
                playlist.add(allSongs.get(i));
        }
        Collections.sort(playlist, new Comparator<MusicBean>() {
            @Override
            public int compare(MusicBean lhs, MusicBean rhs) {
                return rhs.getVotes()-lhs.getVotes();
            }
        });
        SharedBox.setDerivedPlaylist(playlist);
        Log.d("CommonUtils","Derived playlist-first item follows");
        Log.d("CommonUtils",playlist.get(0).getMusicTitle()+playlist.get(0).getVotes());

        return playlist;
    }

    public static ArrayList<MusicBean> deriveRemainingSongs(){
        ArrayList<MusicBean> allSongs = new ArrayList<MusicBean>();
        ArrayList<MusicBean> remainingSongs = new ArrayList<MusicBean>();

        allSongs=SharedBox.getThePlaylist();
        for(int i=0;i<allSongs.size();i++){
            if(!(allSongs.get(i).isInPlayist()))
                remainingSongs.add(allSongs.get(i));
        }
        return remainingSongs;
    }

}
