package de.tudarmstadt.informatik.tk.shhparty.host;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

import de.tudarmstadt.informatik.tk.shhparty.chat.ChatMessage;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommandBean;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;
import de.tudarmstadt.informatik.tk.shhparty.music.MusicBean;

/**
 * Created by Ashwin on 12/11/2016.
 */

public class PartyHostServer extends Thread implements Handler.Callback{

    private static final String AUDIO_FILE_LASTPATH = "sample.wav";

    private static final String LOG_TAG="SHH_servermusic";
    ServerSocket serverSocket = null;
    public static final int NUM_CONNECTIONS = 10;

    public static final int SERVER_PORT = 9001;

    public static final int SERVER_CALLBACK = 103;
    public static final int SERVER_SENTPLAYLIST=104;
    public static final int SERVER_CHATBROADCASTED=120;

    // use a 10 second time out to receive an ack message
    public static final int ACK_TIMEOUT = 10000;

    // a hashmap of all client socket connections and their corresponding output
    // streams for writing
    private HashSet<Socket> connections;
    private ArrayList<ObjectOutputStream> clientObjOutputStreams=new ArrayList<>();

    private boolean needReset = true;
    private Handler handler;

    // for splitting command messages, it should be a character that cannot be
    // used in a file name, all commands have to end with a delimiter
    public static final String CMD_DELIMITER = ";";

    private static final int BUFFER_SIZE = 256;

    // commands the server can send out to the clients
    public static final String PLAY_CMD = "PLAY";
    public static final String STOP_CMD = "STOP";
    public static final String SYNC_CMD = "SYNC";

    private final Handler childHandler = new Handler(this);



    public PartyHostServer(Handler handler) throws IOException
    {
        this.handler = handler;
        connections = new HashSet<Socket>();
        setupSocket();
    }
    @Override
    public void run()
    {
        // thread will terminate when someone disconnects the server
        while (serverSocket != null)
        {
            try
            {
                // always check if the server socket is still ok to function
                setupSocket();

                // A blocking operation to accept incoming client connections
                Socket clientSocket = serverSocket.accept();
                Log.d(LOG_TAG, "Accepted a client");

                // TODO: convert this to an asynchronous method call later
               // syncClientTime(clientSocket);

                connections.add(clientSocket);
                clientObjOutputStreams.add(new ObjectOutputStream(clientSocket.getOutputStream()));
                // Spawning child threads from here to listen to individual sockets
                if(clientSocket!=null) {
                    Log.d(LOG_TAG,"Code to receive votes begins");
                    PartyHostListener singleListener = new PartyHostListener(childHandler, clientSocket);
                    singleListener.start();
                    handler.obtainMessage(SERVER_CALLBACK, this).sendToTarget();
                }


            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, "Could not communicate to client socket.");
                try
                {
                    if (serverSocket != null && !serverSocket.isClosed())
                    {
                        needReset = true;

                        // close all client socket connections
                        for (Socket s : connections)
                        {
                            s.close();
                        }

                        // empty the stored connection list
                        connections = new HashSet<Socket>();
                    }
                }
                catch (IOException e1)
                {
                    Log.e(LOG_TAG, "Could not close all client sockets.");
                    disconnectServer();
                }
                break;
            }
        }
    }

    public void setupSocket()
    {
        // only setup a new server socket if something went wrong
        if (!needReset)
        {
            return;
        }

        try
        {
            if (serverSocket != null && !serverSocket.isClosed())
            {
                serverSocket.close();
                serverSocket = null;
            }

            serverSocket = new ServerSocket(SERVER_PORT);
            Log.d(LOG_TAG, "Socket Started");

            needReset = false;
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Could not start server socket.");
        }
    }

    private void syncClientTime(Socket clientSocket) throws IOException
    {
        Log.d(LOG_TAG, "Started syncing time.");

        // initialize the network latency trackers
        long prevLatency = 0;
        long currLatency = 0;
        long sendTime = System.currentTimeMillis();

        // this is the minimum latency we are willing to accept, we have to
        // relax this requirement if the network is poor
        long ACCEPTABLE_LATENCY = 50;

        InputStream iStream = clientSocket.getInputStream();
        OutputStream oStream = clientSocket.getOutputStream();

        boolean success = false;
        boolean ackReceived = false;

        clientSocket.setSoTimeout(ACK_TIMEOUT);

        while (!success)
        {
            // see if we can reach time synchronization within 7 attempts
            for (int i = 0; i < 7; i++)
            {
                // preparing command to send, this should be as fast as possible
                // ***********Warning: time sensitive code!***********
                String command = SYNC_CMD + CMD_DELIMITER;

                // use this to measure the latency
                sendTime = System.currentTimeMillis();

                // compensating time sync with network latencies:
                // assume our time sync message reaches our client in
                // approximately half of the send and receive time
                command += String.valueOf(currLatency / 2
                        + System.currentTimeMillis())
                        + CMD_DELIMITER;
                oStream.write(command.getBytes());
                // ***********End of time sensitive code************

                while (!ackReceived)
                {
                    // clear the buffer before reading
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytes;

                    // Read from the InputStream and determine the network
                    // latency, this will not block forever as the read timeout
                    // has been set
                    bytes = iStream.read(buffer);
                    if (bytes != -1)
                    {
                        // check for the correct acknowledge message, we don't
                        // want
                        // to respond to any other messages other than the SYNC
                        // ack
                        // from the client
                        String recMsg = new String(buffer);

                        String[] cmdString = recMsg.split(CMD_DELIMITER);

                        if (cmdString[0]
                                .equals(PartyHostServer.SYNC_CMD)
                                && cmdString.length > 1)
                        {
                            ackReceived = true;

                            // let's hope that the current communication
                            // latencies is within our acceptable latency when
                            // compared to
                            // the previous communication latency
                            prevLatency = currLatency;

                            // just to make the method call similar to client's,
                            // to improve the accuracy of the client receive
                            // time is half of the round trip delay
                            Long.parseLong(cmdString[1]);

                            currLatency = System.currentTimeMillis() - sendTime;

                            // can this wrap around? producing a negative
                            // number?
                            if (currLatency < 0)
                            {
                                currLatency *= -1;
                            }

                            // comparing latency jitters:
                            // if this round of latency is acceptable, then the
                            // previously sent time should be reasonable enough
                            // to be used to sync the time for our clients
                            if (Math.abs(currLatency - prevLatency) < ACCEPTABLE_LATENCY)
                            {
                                success = true;
                                Log.d(LOG_TAG, "Accepted latency: "
                                        + ACCEPTABLE_LATENCY);
                                break;
                            }
                        }
                    }
                    // socket read timed out, so treat it as an ack has been
                    // received and exit this while loop and send another
                    // message
                    else
                    {
                        ackReceived = true;

                        Log.d(LOG_TAG, "Socket read timed out.");
                    }
                }

                Log.d(LOG_TAG, "Command Sent: " + command
                        + ", and retrieved network latency of " + currLatency
                        + " ms.");

                if (success)
                {
                    break;
                }
            }

            // still can't get a satisfactory result, let's relax our
            // requirement by 2 folds
            ACCEPTABLE_LATENCY *= 2;

            // we have to call it quits some time
            if (ACCEPTABLE_LATENCY > 10000)
            {
                success = true;
            }
        }
    }

    public void sendPlay(String fileName, long playTime, int playPosition)
    {
        if (fileName == null)
        {
            return;
        }

        String command = PLAY_CMD + CMD_DELIMITER + fileName + CMD_DELIMITER
                + String.valueOf(playTime) + CMD_DELIMITER
                + String.valueOf(playPosition) + CMD_DELIMITER;

        Log.d(LOG_TAG, "Sending command: " + command);

        for (Socket s : connections)
        {
            sendCommand(s, command);
        }
    }

    public void sendStop()
    {
        String command = STOP_CMD + CMD_DELIMITER;

        Log.d(LOG_TAG, "Sending command: " + command);

        for (Socket s : connections)
        {
            sendCommand(s, command);
        }
    }

    private void sendCommand(Socket clientSocket, String command)
    {
        if (clientSocket == null)
        {
            return;
        }

        // automatically update the client connections, making sure the client
        // sockets are always "fresh"
        if (clientSocket.isClosed())
        {
            connections.remove(clientSocket);
            clientSocket = null;
            return;
        }

        try
        {
            // get the corresponding output stream from the socket
            OutputStream oStream = clientSocket.getOutputStream();

            oStream.write(command.getBytes());
            Log.d(LOG_TAG, "Command Sent: " + command);
        }
        catch (IOException e)
        {
            try
            {
                // this client socket is no longer valid, remove it from the
                // list
                clientSocket.close();
                connections.remove(clientSocket);
                clientSocket = null;
            }
            catch (IOException e1)
            {
                Log.e(LOG_TAG, "Cannot remove invalid client socket.");
            }

            Log.e(LOG_TAG, "Cannot send command over to client: " + command);
        }
    }

    public void broadcastMusicInfo(ArrayList<MusicBean> musicInfoToShare)
    {
        if (musicInfoToShare.isEmpty())
        {
            return;
        }

        Log.d(LOG_TAG, "Sending arraylist of music: " + musicInfoToShare.toString());

        for (ObjectOutputStream obj : clientObjOutputStreams)
        {
            sendMusicAndList(obj, musicInfoToShare);
        }
        handler.obtainMessage(SERVER_SENTPLAYLIST,this).sendToTarget();
    }

    private void sendMusicAndList(ObjectOutputStream toClientStream, ArrayList<MusicBean> musicInfo){

        if (toClientStream == null)
        {
            clientObjOutputStreams.remove(toClientStream);
            return;
        }

        try
        {
            // get the corresponding output stream from the socket
          //  ObjectOutputStream objStream = new ObjectOutputStream(clientSocket.getOutputStream());
           toClientStream.reset();
            toClientStream.writeUnshared(musicInfo);
            //toClientStream.writeObject(musicInfo);
            toClientStream.flush();
            Log.d(LOG_TAG, "Object Sent: " + musicInfo.toString());
        }
        catch (IOException e)
        {
            try
            {
                // this client socket is no longer valid, remove it from the
                // list
                toClientStream.close();
                clientObjOutputStreams.remove(toClientStream);
                toClientStream = null;
            }
            catch (IOException e1)
            {
                Log.e(LOG_TAG, "Cannot remove invalid client output obj stream.");
            }

          //  Log.e(LOG_TAG, "Cannot send object over to client: " + musicInfo.toString());
        }
    }

    public void broadcastChatMessage(ChatMessage messageToShare)
    {
        if (messageToShare==null)
        {
            return;
        }

        Log.d(LOG_TAG, "Broadcasting chat message: " + messageToShare.toString());

        for (ObjectOutputStream obj : clientObjOutputStreams)
        {
            sendChatMessage(obj, messageToShare);
        }
        handler.obtainMessage(SERVER_CHATBROADCASTED,this).sendToTarget();
    }
    private void sendChatMessage(ObjectOutputStream toClientStream, ChatMessage messageToSend){

        if (toClientStream == null)
        {
            clientObjOutputStreams.remove(toClientStream);
            return;
        }

        try
        {
            // get the corresponding output stream from the socket
            //  ObjectOutputStream objStream = new ObjectOutputStream(clientSocket.getOutputStream());
            if(messageToSend!=null) {
                toClientStream.reset();
                toClientStream.writeUnshared(messageToSend);
                //toClientStream.writeObject(musicInfo);
                toClientStream.flush();
                Log.d(LOG_TAG, "Chat message Sent: " + messageToSend.toString());
            }
        }
        catch (IOException e)
        {
            try
            {
                // this client socket is no longer valid, remove it from the
                // list
                toClientStream.close();
                clientObjOutputStreams.remove(toClientStream);
                toClientStream = null;
            }
            catch (IOException e1)
            {
                Log.e(LOG_TAG, "Cannot remove invalid client output obj stream.");
            }

            //  Log.e(LOG_TAG, "Cannot send object over to client: " + musicInfo.toString());
        }
    }
    public void broadcastCommandMessage(CommandBean commandToShare)
    {
        if (commandToShare==null)
        {
            return;
        }

        Log.d(LOG_TAG, "Broadcasting command message: " + commandToShare.toString());

        for (ObjectOutputStream obj : clientObjOutputStreams)
        {
            sendCommandBean(obj, commandToShare);
        }
       // handler.obtainMessage(SERVER_CHATBROADCASTED,this).sendToTarget();
    }

    private void sendCommandBean(ObjectOutputStream toClientStream, CommandBean commandToSend){

        if (toClientStream == null)
        {
            clientObjOutputStreams.remove(toClientStream);
            return;
        }

        try
        {
            // get the corresponding output stream from the socket
            //  ObjectOutputStream objStream = new ObjectOutputStream(clientSocket.getOutputStream());
            if(commandToSend!=null) {
                toClientStream.reset();
                toClientStream.writeUnshared(commandToSend);
                //toClientStream.writeObject(musicInfo);
                toClientStream.flush();
                Log.d(LOG_TAG, "Command message Sent: " + commandToSend.toString());
            }
        }
        catch (IOException e)
        {
            try
            {
                // this client socket is no longer valid, remove it from the
                // list
                toClientStream.close();
                clientObjOutputStreams.remove(toClientStream);
                toClientStream = null;
            }
            catch (IOException e1)
            {
                Log.e(LOG_TAG, "Cannot remove invalid client output obj stream.");
            }

            //  Log.e(LOG_TAG, "Cannot send object over to client: " + musicInfo.toString());
        }
    }


    public void disconnectClients()
    {
        // close all client socket connections
        for (Socket s : connections)
        {
            try
            {
                if (s != null && !s.isClosed())
                {
                    s.close();
                    s = null;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        for(ObjectOutputStream obj:clientObjOutputStreams)
            try {
                obj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // empty the stored connection list
        connections = new HashSet<Socket>();
        clientObjOutputStreams=new ArrayList<ObjectOutputStream>();
    }

    public void kickoutOneMember(){

    }

    public void sendMusicAndPlaylist(){

    }

    public void disconnectServer()
    {
        needReset = true;

        // must disconnect all clients first
        disconnectClients();

        // empty the client connection list
        connections = null;
        connections = new HashSet<Socket>();

        try
        {
            if (serverSocket != null && !serverSocket.isClosed())
            {
                serverSocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        serverSocket = null;
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch(msg.what){
            case PartyHostListener.PLAYLIST_UPDATED:
                //broadcast the playlist
                broadcastMusicInfo(SharedBox.getThePlaylist());
                break;
            case PartyHostListener.CHATBOX_UPDATED:
                broadcastChatMessage(SharedBox.getMessage());
                break;
            case PartyHostListener.MEMBERDATA_RECEIVED:
                handler.obtainMessage(PartyHostListener.MEMBERDATA_RECEIVED).sendToTarget();
                Log.d(LOG_TAG,"Asked ConnectionManager to take care of member list updation");
                break;
            case PartyHostListener.SOMECLIENT_LEFT:
                handler.obtainMessage(PartyHostListener.SOMECLIENT_LEFT).sendToTarget();
                break;
            default:
                Log.d(LOG_TAG, "In default block: "
                        + msg.what);
                break;
        }
        return true;
    }
}
