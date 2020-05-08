 package pj.rozkladWKD.pj.rozkladWKD.ui;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.drawable.AnimationDrawable;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import pj.rozkladWKD.*;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: paweljaneczek
  * Date: 31.05.2012
  * Time: 12:29
  * To change this template use File | Settings | File Templates.
  */
 public class MessagesFragment extends SherlockFragment {
 
     private boolean error = false;
 
     private LinkedList<UserMessage> userMessagesList;
     private MessageAdapter messageAdapter;
 
     private ImageView messageLoader;
     private AnimationDrawable messageLoaderAnimation;
 
     private static SimpleDateFormat formatterDate;
 
 
     private AsyncTask currentTask = null;
 
 
     private ListView list;
 
     private String mType;
 
     private TextView mEmpty;
 
    public MessagesFragment() {}

     public void resetMessages() {
         if(getActivity() != null && mType != null)
             Prefs.resetNotificationMessageNumber(getActivity(), mType);
     }
 
 
     public interface IMessagingListener {
         public void messageSent();
     }
 
 
     public MessagesFragment(String type) {
         mType = type;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view =  inflater.inflate(R.layout.fragment_messages, container, false);
         list = (ListView) view.findViewById(android.R.id.list);
 
 
         mEmpty = (TextView) view.findViewById(android.R.id.empty);
 
 
         messageLoader = (ImageView) view.findViewById(R.id.user_messages_loader);
         messageLoader.setBackgroundResource(R.layout.loader);
         messageLoaderAnimation = (AnimationDrawable) messageLoader.getBackground();
 
         return  view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         currentTask = new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);
 
 
     }
 
 
     private void setListAdapter() {
         if(RozkladWKD.DEBUG_LOG) {
             Log.i("MessagesFragment", "set list adapter");
         }
         if(error) {
             Toast.makeText(getActivity(), R.string.there_is_no_network_connection, 1000).show();
         }else if(userMessagesList == null) {
             list.setVisibility(View.GONE);
             mEmpty.setVisibility(View.VISIBLE);
             mEmpty.setText(R.string.no_messages);
         } else {
             list.setVisibility(View.VISIBLE);
             mEmpty.setVisibility(View.GONE);
 
             messageAdapter = new MessageAdapter(getActivity(), R.layout.rozklad_wkd_user_messages_row, userMessagesList, (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
 
             list.setAdapter(messageAdapter);
         }
     }
 
     public void sendMessage(String username, String message) {
 
         currentTask = new DownloadMessages().execute(DownloadMessages.SEND_MESSAGE, username, message);
     }
 
     private void startDownloadingAnimation() {
         messageLoader.setVisibility(View.VISIBLE);
         messageLoaderAnimation.start();
     }
     private void stopDownloadingAnimation() {
         messageLoader.setVisibility(View.INVISIBLE);
         messageLoaderAnimation.stop();
     }
 
     private LinkedList<UserMessage> getUserMessages() throws JSONException, ParseException, ClientProtocolException, Resources.NotFoundException, IOException {
         LinkedList<UserMessage> messagesList = new LinkedList<UserMessage>();
         formatterDate = new SimpleDateFormat("HH:mm dd-MM-yyyy");
 
         Calendar cal = Calendar.getInstance();
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 
 
 
         nameValuePairs.add(new BasicNameValuePair("requestType", "GET_MESSAGES"));
         nameValuePairs.add(new BasicNameValuePair("messageType", mType));
 
         if(RozkladWKD.DEBUG_LOG) {
             Log.i("MessagesFragment", "getting messages from server");
 
         }
 
 
 
         // Send the HttpPostRequest and receive a JSONObject in return
         JSONObject jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
 
         if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
             return null;
         }
 
 
         JSONArray arr = jsonObjRecv.getJSONArray("db");
         JSONObject obj;
 
 
 
         UserMessage message;
 
 
         for(int i = 0; i < arr.length(); i++) {
             obj = arr.getJSONObject(i);
             message= new UserMessage();
 
 
 
 
             cal.setTime((Date)formatterDate.parse(obj.getString("time_char")));
             message.time = (Calendar) cal.clone();
 
             message.message = obj.getString("message");
             message.fromWho = obj.getString("USER");
             message.premium = (obj.getString("premium").equals("Y"));
 
             messagesList.add(message);
         }
 
         return messagesList;
 
 
 
 
     }
 
     private Boolean sendNewUserMessage(String username, String mess) throws ClientProtocolException, JSONException, IOException {
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
 
         nameValuePairs.add(new BasicNameValuePair("requestType", "SEND_MESSAGE"));
         nameValuePairs.add(new BasicNameValuePair("username", username));
         nameValuePairs.add(new BasicNameValuePair("message", mess));
         nameValuePairs.add(new BasicNameValuePair("premium", getResources().getBoolean(R.bool.premium) ? "Y" : "N"));
         nameValuePairs.add(new BasicNameValuePair("messageType", mType));
 
         if(RozkladWKD.DEBUG_LOG) {
             Log.i("MessagesFragment", "send message from server");
         }
 
 
 
         // Send the HttpPostRequest and receive a JSONObject in return
         JSONObject jsonObjRecv = pj.rozkladWKD.HttpClient.SendHttpPost(nameValuePairs);
 
         if(jsonObjRecv == null || jsonObjRecv.getString("result") == "ERROR") {
             return false;
         } else {
             return true;
         }
     }
 
     public void refreshMessages() {
         if(currentTask != null)
             return;
         currentTask = new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);
     }
 
     private class DownloadMessages extends AsyncTask<Object, Object, Object>{
         public static final int GET_MESSAGES = 1;
         public static final int SEND_MESSAGE = 2;
 
         Exception e = null;
 
         int what;
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             startDownloadingAnimation();
             list.setVisibility(View.GONE);
             mEmpty.setVisibility(View.VISIBLE);
             mEmpty.setText(getString(R.string.loading_messages));
         }
 
 
 
         @Override
         protected Object doInBackground(Object... params) {
 
             what = ((Integer)params[0]).intValue();
 
             switch(what) {
                 case GET_MESSAGES:
                     try {
                         userMessagesList = getUserMessages();
                     } catch (Exception e) {
                         if(RozkladWKD.DEBUG_LOG) {
                             Log.w("MessageFragment", e);
                         }
                         this.e = e;
                     }
                     break;
                 case SEND_MESSAGE:
                     String username = (String) params[1];
                     String mess = (String) params[2];
 
                     try{
                         sendNewUserMessage(username, mess);
                     } catch (Exception e) {
                         if(RozkladWKD.DEBUG_LOG) {
                             Log.w("MessageFragment", e);
                         }
                         this.e = e;
                     }
 
                     break;
             }
 
 
             return null;
         }
 
 
 
         @Override
         protected void onPostExecute(Object result) {
 
             switch(what) {
                 case GET_MESSAGES:
                     setListAdapter();
                     stopDownloadingAnimation();
                     break;
                 case SEND_MESSAGE:
                     stopDownloadingAnimation();
                     ((IMessagingListener)getActivity()).messageSent();
 
 
                     if(error) {
                         Toast.makeText(getActivity(), R.string.uploading_message_error, Toast.LENGTH_SHORT).show();
                     } else {
                         error = false;
                         Toast.makeText(getActivity(), R.string.uploading_message_success, Toast.LENGTH_SHORT).show();
 
                         currentTask = new DownloadMessages().execute(DownloadMessages.GET_MESSAGES);
                     }
                     break;
 
             }
 
 //				if(e != null) {
 //					Error.handle(MessageActivity.this, "WKD", e);
 //				}
 
             currentTask = null;
         }
 
         @Override
         protected void onCancelled() {
             super.onCancelled();
 
             stopDownloadingAnimation();
         }
 
     }
 }
