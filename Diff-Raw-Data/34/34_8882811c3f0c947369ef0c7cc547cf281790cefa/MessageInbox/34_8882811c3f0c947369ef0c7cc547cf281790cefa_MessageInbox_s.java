 package com.allplayers.android;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.allplayers.android.activities.AllplayersSherlockActivity;
 import com.allplayers.objects.MessageData;
 import com.allplayers.rest.RestApiV1;
 import com.devspark.sidenavigation.SideNavigationView;
 import com.devspark.sidenavigation.SideNavigationView.Mode;
 
 /**
  * MessageInbox.
  * User's messages inbox.
  *
  */
 public class MessageInbox extends AllplayersSherlockActivity {
 
     private final int LIMIT = 15;
     private ArrayList<MessageData> mMessageList;
     private Button mLoadMoreButton;
     private ListView mListView;
     private MessageAdapter mMessageListAdapter;
     private ProgressBar mLoadingIndicator;
     private ViewGroup mFooter;
     private boolean mEndOfData;
     private int mOffset;
 
     /**
      * onCreate().
      * Called when the activity is first created.
      *
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.inboxlist);
 
         mActionBar = getSupportActionBar();
         mActionBar.setIcon(R.drawable.menu_icon);
         mActionBar.setTitle("Messages");
         mActionBar.setSubtitle("Inbox");
 
         mSideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
         mSideNavigationView.setMenuItems(R.menu.side_navigation_menu);
         mSideNavigationView.setMenuClickCallback(this);
         mSideNavigationView.setMode(Mode.LEFT);
 
         // Load the user's inbox.
         new GetUserInboxTask().execute();
 
         mMessageList = new ArrayList<MessageData>();
         mListView = (ListView) findViewById(R.id.customListView);
         mMessageListAdapter = new MessageAdapter(MessageInbox.this, mMessageList);
         mFooter = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.load_more, null);
         mLoadMoreButton = (Button) mFooter.findViewById(R.id.load_more_button);
         mLoadingIndicator = (ProgressBar) mFooter.findViewById(R.id.loading_indicator);
 
         // Set up the "load more" button.
         mLoadMoreButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 mLoadMoreButton.setVisibility(View.GONE);
                 mLoadingIndicator.setVisibility(View.VISIBLE);
                 new GetUserInboxTask().execute();
             }
         });
 
         // Set up the list view that will show all of the data.
         mListView.addFooterView(mFooter);
         mListView.setAdapter(mMessageListAdapter);
         mListView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
 
                 Intent intent = (new Router(MessageInbox.this)).getMessageThreadIntent(mMessageList.get(position));
                 startActivity(intent);
             }
         });
     }
 
     /**
      * GetUserInboxTask.
      * Fetches the user's message inbox asynchronously.
      *
      */
     public class GetUserInboxTask extends AsyncTask<Void, Void, String> {
 
         /**
          * doInBackground().
          * Fetch the user's message inbox.
          *
          */
         @Override
         protected String doInBackground(Void... Args) {
             return RestApiV1.getUserInbox(LIMIT, mOffset);
         }
 
         /**
          * onPostExecute().
          * Take the JSON result from fetching the user's inbox and make it into useful data.
          *
          * @param jsonResult: Result of fetching the user's inbox.
          *
          */
         @Override
         protected void onPostExecute(String jsonResult) {
             MessagesMap messages = new MessagesMap(jsonResult);
 
             // Check if there was any data returned. If there wasn't, either all of the data has
             // been fetched already or there wasn't any to fetch in the beginning.
             if (messages.size() == 0) {
                 mEndOfData = true;
 
                 // Check if the list of messages is empty. If so, there was never any data to be
                 // fetched.
                 if (mMessageList.size() == 0) {
 
                     // We need to display to the user that there aren't any messanges. We do this
                     // by making a blank MessageData object and setting its last_message_sender
                     // field to a notification. We use this field because it is the most prominent.
                     MessageData blank = new MessageData();
                     blank.setLastSender("No messages to display");
                     mMessageListAdapter.notifyDataSetChanged();
                 }
             }
 
             // If we made it here we know that there was at least part of a set of data fetched.
             else {
 
                 // If the size of the returned data is less than the maximum size we specified, we
                 // know that we have reached the end of the data to be fetched.
                 if (messages.size() < LIMIT) {
                     mEndOfData = true;
                 }
 
                 mMessageList.addAll(messages.getMessageData());
                 mMessageListAdapter.notifyDataSetChanged();
                 if (!mEndOfData) {
                     mLoadMoreButton.setVisibility(View.VISIBLE);
                     mLoadingIndicator.setVisibility(View.GONE);
                     mOffset += LIMIT;
                 } else {
                     mListView.removeFooterView(mFooter);
                 }
             }
 
         }
     }
 }
