package de.tudarmstadt.informatik.tk.shhparty.chat;

/**
 * Created by Preneesh on 05-01-2017.
 */
import java.util.ArrayList;
import java.util.Random;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import de.tudarmstadt.informatik.tk.shhparty.R;
import de.tudarmstadt.informatik.tk.shhparty.utils.CommonUtils;
import de.tudarmstadt.informatik.tk.shhparty.utils.SharedBox;


public class ChatFragment extends Fragment implements OnClickListener {

    private EditText msg_edittext;
    private String user1 = "khushi", user2 = "khushi1";
    private Random random;
    public static ArrayList<ChatMessage> chatlist;
    public static ChatAdapter chatAdapter;
    public static ListView msgListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_layout, container, false);
        random = new Random();

        msg_edittext = (EditText) view.findViewById(R.id.messageEditText);
        msgListView = (ListView) view.findViewById(R.id.msgListView);
        ImageButton sendButton = (ImageButton) view
                .findViewById(R.id.sendMessageButton);
        sendButton.setOnClickListener(this);

        // ----Set autoscroll of listview when a new message arrives----//
        msgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgListView.setStackFromBottom(true);

        chatlist = new ArrayList<ChatMessage>();
        chatAdapter = new ChatAdapter(getActivity(), chatlist);
        msgListView.setAdapter(chatAdapter);
        ChatMessage latestMessage=SharedBox.getMessage();
        if(latestMessage!=null){
            chatAdapter.add(latestMessage);
            chatAdapter.notifyDataSetChanged();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    }

    public void sendTextMessage(View v) {
        String message = msg_edittext.getEditableText().toString();
        if (!message.equalsIgnoreCase("")) {
            final ChatMessage chatMessage = new ChatMessage(SharedBox.getMyProfileBean().getName(), "",
                    message, "" + random.nextInt(1000), true);
            chatMessage.setMsgID();
            chatMessage.body = message;
            chatMessage.Date = CommonUtils.getCurrentDate();
            chatMessage.Time = CommonUtils.getCurrentTime();
            msg_edittext.setText("");
            if(getActivity().getClass().getName().equals("de.tudarmstadt.informatik.tk.shhparty.member.PartyHome")){
                //Making network call as Asynctask
                new SendChatMessage().execute(chatMessage);
            }
            else if(getActivity().getClass().getName().equals("de.tudarmstadt.informatik.tk.shhparty.host.PartyConsole")){
                SharedBox.setMessage(chatMessage);
                //Making network call as Asynctask
                new BroadcastChatMessage().execute(chatMessage);

            }
            
          /*  chatAdapter.add(chatMessage);
            chatAdapter.notifyDataSetChanged();*/
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendMessageButton:
                sendTextMessage(v);

        }
    }

    private class SendChatMessage extends AsyncTask<ChatMessage,Integer,String>{

        @Override
        protected String doInBackground(ChatMessage... message) {
            SharedBox.getClient().sendChatMessage(message[0]);
            return "Sent the chat message";

        }
    }

    private class BroadcastChatMessage extends AsyncTask<ChatMessage,Integer,String>{
        @Override
        protected String doInBackground(ChatMessage... message) {
            SharedBox.getServer().broadcastChatMessage(message[0]);
            return "Broadcasted the chat message";
        }
    }

}