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
 import com.allplayers.objects.AlbumData;
 import com.allplayers.objects.GroupData;
import com.allplayers.objects.MessageData;
 import com.allplayers.rest.RestApiV1;
 import com.devspark.sidenavigation.SideNavigationView;
 import com.devspark.sidenavigation.SideNavigationView.Mode;
 
 public class GroupAlbumsActivity  extends AllplayersSherlockActivity {
 
     private AlbumAdapter mAlbumListAdapter;
     private ArrayList<AlbumData> mAlbumList;
     private Button mLoadMoreButton;
     private GroupData mGroup;
     private ListView mListView;
     private ProgressBar mLoadingIndicator;
     private ViewGroup mFooter;
 
     private final int LIMIT = 15;
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
         setContentView(R.layout.albums_list);
 
         mGroup = (new Router(this)).getIntentGroup();
 
         mActionBar = getSupportActionBar();
         mActionBar.setIcon(R.drawable.menu_icon);
         mActionBar.setTitle(mGroup.getTitle());
         mActionBar.setSubtitle("Photo Albums");
 
         mSideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
         mSideNavigationView.setMenuItems(R.menu.side_navigation_menu);
         mSideNavigationView.setMenuClickCallback(this);
         mSideNavigationView.setMode(Mode.LEFT);
 
         // Get the group's albums.
         new GetGroupAlbumsByGroupIdTask().execute(mGroup);
 
         mEndOfData = false;
         mOffset = 0;
 
         mAlbumList = new ArrayList<AlbumData>();
         mListView = (ListView) findViewById(R.id.customListView);
         mAlbumListAdapter = new AlbumAdapter(GroupAlbumsActivity.this, R.layout.albumlistitem, mAlbumList);
         mFooter = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.load_more, null);
         mLoadMoreButton = (Button) mFooter.findViewById(R.id.load_more_button);
         mLoadingIndicator = (ProgressBar) mFooter.findViewById(R.id.loading_indicator);
 
         // Set up the "load more" button.
         mLoadMoreButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 mLoadMoreButton.setVisibility(View.GONE);
                 mLoadingIndicator.setVisibility(View.VISIBLE);
                 new GetGroupAlbumsByGroupIdTask().execute(mGroup);
             }
         });
 
         // Set up the list view that will show all of the data.
         mListView.addFooterView(mFooter);
         mListView.setAdapter(mAlbumListAdapter);
         mListView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
 
                 Intent intent = (new Router(GroupAlbumsActivity.this)).getAlbumPhotosActivityIntent(mAlbumList.get(position));
                 startActivity(intent);
             }
         });
     }
 
     /**
      * GetGroupAlbumsByGroupIdTask.
      * Fetches the passed group's photo albums asynchronously.
      *
      */
     public class GetGroupAlbumsByGroupIdTask extends AsyncTask<GroupData, Void, String> {
 
         /**
          * doInBackground().
          * Fetch the passed in groups albums.
          *
          * @param groups: An array of groups (that only contains one group) of whose albums we need
          * to fetch. (doInBackground()'s super requires an array as the parameter)
          *
          */
         @Override
         protected String doInBackground(GroupData... groups) {
             return RestApiV1.getGroupAlbumsByGroupId(groups[0].getUUID(), LIMIT, mOffset);
         }
 
         /**
          * onPoseExecute().
          * Take the JSON result from fetching the groups albums and turn it into useful data.
          *
          * @param jsonResult: Result of fetching the group's albums.
          *
          */
         @Override
         protected void onPostExecute(String jsonResult) {
             AlbumsMap albums = new AlbumsMap(jsonResult);
 
             // Check if there was any data returned. If there wasn't, either all of the data has
             // been fetched already or there wasn't any to fetch in the beginning.
             //if(albums.size() == 0) {
             if (jsonResult.equals("error")) {
                 mEndOfData = true;
                 mListView.removeFooterView(mFooter);
 
                 // Check if the list of messages is empty. If so, there was never any data to be
                 // fetched.
                 if (mAlbumList.size() == 0) {
 
                     // We need to display to the user that there aren't any messages. We do this
                    // by making a blank MessageData object and setting its last_message_sender
                    // field to a notification. We use this field because it is the most prominent.
                    MessageData blank = new MessageData();
                    blank.setLastSender("No messages to display");
                     mAlbumListAdapter.notifyDataSetChanged();
                 }
             }
 
             // If we made it here we know that there was at least part of a set of data fetched.
             else {
 
                 // If the size of the returned data is less than the maximum size we specified, we
                 // know that we have reached the end of the data to be fetched.
                 if (albums.size() < LIMIT) {
                     mEndOfData = true;
                 }
 
                 mAlbumList.addAll(albums.getAlbumData());
                 mAlbumListAdapter.notifyDataSetChanged();
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
