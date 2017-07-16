package de.tudarmstadt.informatik.tk.shhparty.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import de.tudarmstadt.informatik.tk.shhparty.chat.ChatMessage;
import de.tudarmstadt.informatik.tk.shhparty.host.PartyHostServer;
import de.tudarmstadt.informatik.tk.shhparty.member.MemberBean;
import de.tudarmstadt.informatik.tk.shhparty.member.PartyMemberClient;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

/**
 * Created by Ashwin on 1/25/2017.
 */

public class SharedBox {


    public static PartyMemberClient client;

    public static PartyHostServer getServer() {
        return server;
    }

    public static void setServer(PartyHostServer server) {
        SharedBox.server = server;
    }

    public static PartyHostServer server;

    public static ArrayList<MusicBean> getThePlaylist() {
        return thePlaylist;
    }

    public static void setThePlaylist(ArrayList<MusicBean> thePlaylist) {
        SharedBox.thePlaylist = thePlaylist;
    }

    public static ArrayList<MusicBean> thePlaylist=new ArrayList<MusicBean>();

    public static void setClient(PartyMemberClient client) {
        SharedBox.client = client;
    }

    public static PartyMemberClient getClient() {
        return client;
    }

    public static ChatMessage message;
    public static ChatMessage getMessage() {
        return message;
    }

    public static void setMessage(ChatMessage message) {
        SharedBox.message = message;
    }

    public static ArrayList<MemberBean> getListOfMembers() {
        return listOfMembers;
    }

    public static void setListOfMembers(ArrayList<MemberBean> listOfMembers) {
        SharedBox.listOfMembers = listOfMembers;
    }

    public static ArrayList<MemberBean> listOfMembers=new ArrayList<MemberBean>();

    public static MemberBean getMyProfileBean() {
        return myProfileBean;
    }

    public static void setMyProfileBean(MemberBean myProfileBean) {
        SharedBox.myProfileBean = myProfileBean;
    }

    public static MemberBean myProfileBean;

    public static File getWwwroot() {
        return wwwroot;
    }

    public static void setWwwroot(File wwwroot) {
        SharedBox.wwwroot = wwwroot;
    }

    public static String getHttpHostIP() {
        return httpHostIP;
    }

    public static void setHttpHostIP(String httpHostIP) {
        SharedBox.httpHostIP = httpHostIP;
    }

    public static File wwwroot;
    public static String httpHostIP;

    public static CommandBean getReceivedCommand() {
        return receivedCommand;
    }

    public static void setReceivedCommand(CommandBean receivedCommand) {
        SharedBox.receivedCommand = receivedCommand;
    }

    public static CommandBean receivedCommand;

    public static HashMap<String, Socket> getNameSocketMapping() {
        return nameSocketMapping;
    }

    public static void setNameSocketMapping(HashMap<String, Socket> nameSocketMapping) {
        SharedBox.nameSocketMapping = nameSocketMapping;
    }

    public static HashMap<String,Socket> nameSocketMapping=new HashMap<String,Socket>();

    public static ArrayList<MusicBean> getDerivedPlaylist() {
        return derivedPlaylist;
    }

    public static void setDerivedPlaylist(ArrayList<MusicBean> derivedPlaylist) {
        SharedBox.derivedPlaylist = derivedPlaylist;
    }

    public static ArrayList<MusicBean >derivedPlaylist=new ArrayList<MusicBean>();

    public static MemberBean getMemberToRemove() {
        return memberToRemove;
    }

    public static void setMemberToRemove(MemberBean memberToRemove) {
        SharedBox.memberToRemove = memberToRemove;
    }

    public static MemberBean memberToRemove;





}
