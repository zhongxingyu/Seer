 package edu.ucsb.cs290.touch.to.chat;
 
 import java.io.IOException;
 import java.security.GeneralSecurityException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import edu.ucsb.cs290.touch.to.chat.crypto.CryptoContacts;
 import edu.ucsb.cs290.touch.to.chat.https.TorProxy;
 import edu.ucsb.cs290.touch.to.chat.remote.messages.Message;
 import edu.ucsb.cs290.touch.to.chat.remote.messages.ProtectedMessage;
 import edu.ucsb.cs290.touch.to.chat.remote.messages.TokenAuthMessage;
 
 public class ConversationDetailFragment extends Fragment {
 
     public static final String ARG_ITEM_ID = "contact name";
 
     CryptoContacts.Contact mItem;
     ListView messageList;
     EditText messageText;
 
     public ConversationDetailFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //get contact data from database, or a map? TODO
         if (getArguments().containsKey(ARG_ITEM_ID)) {
             mItem = CryptoContacts.ITEM_MAP.get(getArguments().get(ARG_ITEM_ID));
         }
        getActivity().findViewById(R.id.send_message_button).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessage(v);
				
			}
		});
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_conversation_detail, container, false);
         messageList = (ListView) rootView.findViewById(R.id.messages_list);
         if (mItem != null) {
         	String[] stuff = new String[] {"Testing","Attack at dawn"};    
             messageList.setAdapter( new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2,android.R.id.text1, stuff));
         }
         return rootView;
     }
     
     private void sendMessage(View v) {
     	EditText messageToSend = (EditText) v.findViewById(R.id.edit_message_text);
     	Message m = new Message(messageToSend.getText().toString());
     	ProtectedMessage pm = null;
     	((KeyActivity) getActivity()).getInstance().addOutgoingMessage(
     			messageToSend.getText().toString(), System.currentTimeMillis(),mItem);
 
 		try {
 			pm = new ProtectedMessage(m, mItem.getEncryptingKey(), null);
 		} catch (GeneralSecurityException e) {
 			Logger.getLogger("touch-to-text").log(Level.SEVERE,
 					"Problem creating ProtectedMessage!", e);
 		} catch (IOException e) {
 			Logger.getLogger("touch-to-text").log(Level.SEVERE,
 					"Problem creating ProtectedMessage!", e);
 		}
     	TokenAuthMessage tm = new TokenAuthMessage(pm, mItem.getSigningKey(), mItem.getToken());
     	TorProxy.sendMessage(tm);
     }
     
 }
