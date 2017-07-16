package de.tudarmstadt.informatik.tk.shhparty.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import de.tudarmstadt.informatik.tk.shhparty.chat.ChatMessage;
import de.tudarmstadt.informatik.tk.shhparty.host.ConnectionManager;
import de.tudarmstadt.informatik.tk.shhparty.member.MemberBean;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

/**
 * Created by Ashwin on 1/27/2017.
 */

public class HostUtils {

    public synchronized static void updateTheVotes(MusicBean musicBean){
        ArrayList<MusicBean> originalPlaylist=SharedBox.getThePlaylist();

        for(int i=0;i<originalPlaylist.size();i++){
            if(originalPlaylist.get(i).getMusicID()==musicBean.getMusicID()){
                int currVotes=originalPlaylist.get(i).getVotes();
               currVotes++;
                originalPlaylist.get(i).setVotes(currVotes);
                SharedBox.setThePlaylist(originalPlaylist);
            }
        }
    }

    public synchronized static void  updateThePlaylist(ArrayList<MusicBean> songsToBeAdded) {
        ArrayList<MusicBean> originalPlaylist = SharedBox.getThePlaylist();
        for (int i=0;i<originalPlaylist.size();i++) {
            for (int j = 0; j < songsToBeAdded.size(); j++) {
                if ((originalPlaylist.get(i).getMusicID()) == songsToBeAdded.get(j).getMusicID()){
                    originalPlaylist.get(i).setInPlayist(true);
                    SharedBox.setThePlaylist(originalPlaylist);
                }
            }
        }
        for(int i=0;i<originalPlaylist.size();i++){
            if(originalPlaylist.get(i).isInPlayist())
            Log.d("HostUtils",originalPlaylist.get(i).getMusicTitle()+" "+originalPlaylist.get(i).getMusicID());
        }



    }

    public synchronized static void setReceivedMessage(ChatMessage message){
        SharedBox.setMessage(message);
    }

    public synchronized  static void addNewMemberToParty(MemberBean member){
        SharedBox.listOfMembers.add(member);
    }

    public synchronized  static void addToNameSocketMap(MemberBean member,Socket clientSocket){
        HashMap<String,Socket> nameSocketMapping=SharedBox.getNameSocketMapping();
        nameSocketMapping.put(member.getName(),clientSocket);
        SharedBox.setNameSocketMapping(nameSocketMapping);

    }

    public void buildAndSendPlay(String realTrackPath,String trackName){

       Log.d("HostUtils","Calling Asynctask to copy track and send command");
        new copyTrackAndSendCommand().execute(realTrackPath,trackName);
    }

    public void buildAndSendPause(){

        Log.d("HostUtils","Calling Asynctask to send pause command");
        new SendPauseCommand().execute();
    }

    public void buildAndSendResume(int startPos){
        Log.d("HostUtils","Calling Asynctask to send resume command");
        new SendResumeCommand().execute(startPos);
    }

    public void informAboutVoteReset(){
        Log.d("HostUtils","Calling Asynctask to inform vote reset");
        new InformAboutVoteReset().execute();
    }
    public void buildAndSendStop(){
        Log.d("HostUtils","Calling Asynctask to send stop command");
        new SendStopCommand().execute();
    }
    private class copyTrackAndSendCommand extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {

            File wwwroot=SharedBox.getWwwroot();
            File localMusic= new File(params[0].toString());
            File hostedFile = new File(wwwroot,localMusic.getName());
            Log.d("HostUtils","file to copy:"+localMusic.getAbsolutePath());
            try {
                CommonUtils.copyFile(localMusic, hostedFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Uri webMusicURI = Uri.parse("http://" + SharedBox.getHttpHostIP() + ":"
                    + String.valueOf(ConnectionManager.HTTP_PORT) + "/" + hostedFile.getName());
            Log.d("HostUtils","WEB MUSIC URI:"+webMusicURI.toString());
            CommandBean commBean=new CommandBean();
            commBean.setCommand("PLAY");
            commBean.setURL(webMusicURI.toString());
            commBean.setStartPosition(0);
            commBean.setTrackLength(0);
            commBean.setTrackName(params[1]);
            SharedBox.getServer().broadcastCommandMessage(commBean);
            return "Copied and sent";
        }
    }

    private class SendPauseCommand extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... params) {
            CommandBean commBean=new CommandBean();
            commBean.setCommand("PAUSE");
            SharedBox.getServer().broadcastCommandMessage(commBean);
            return "Sent pause command";
        }
    }

    private class SendResumeCommand extends AsyncTask<Integer,Integer,String>{

        @Override
        protected String doInBackground(Integer... params) {

            CommandBean commBean=new CommandBean();
            commBean.setCommand("RESUME");
            commBean.setStartPosition(params[0]);
            SharedBox.getServer().broadcastCommandMessage(commBean);
            return "Sent Resume command";
        }
    }

    private class InformAboutVoteReset extends AsyncTask<Integer,Integer,String>{

        @Override
        protected String doInBackground(Integer... params) {
            SharedBox.getServer().broadcastMusicInfo(SharedBox.getThePlaylist());
            Log.d("HostUtils","Votereset broadcast");
            return "Informed about vote reset";
        }
    }
    private class SendStopCommand extends AsyncTask<Integer,Integer,String>{

        @Override
        protected String doInBackground(Integer... params) {
            CommandBean commBean=new CommandBean();
            commBean.setCommand("STOP");
            SharedBox.getServer().broadcastCommandMessage(commBean);
            return "Sent Stop Command";
        }
    }
}
