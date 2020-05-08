 package mobisocial.rectacular;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import mobisocial.rectacular.model.FeedManager;
 import mobisocial.rectacular.model.FollowingManager;
 import mobisocial.rectacular.model.MEntry.EntryType;
 import mobisocial.rectacular.model.MFeed;
 import mobisocial.rectacular.model.MFollowing;
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.DbIdentity;
 import mobisocial.socialkit.musubi.Musubi;
 import mobisocial.socialkit.obj.MemObj;
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.os.Bundle;
 
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MainActivity extends FragmentActivity implements
         ActionBar.OnNavigationListener {
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * current dropdown position.
      */
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 
     private static final String ACTION_CREATE_FEED = "musubi.intent.action.CREATE_FEED";
     private static final String ACTION_EDIT_FEED = "musubi.intent.action.EDIT_FEED";
     
     private static final int REQUEST_CREATE_FEED = 1;
     private static final int REQUEST_EDIT_FEED = 2;
     
     private static final String ADD_TITLE = "member_header";
     private static final String ADD_HEADER = "Following";
 
     private static final String TAG = "MainActivity";
     
     private Musubi mMusubi;
     
     private FeedManager mFeedManager;
     private FollowingManager mFollowingManager;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         // Set up the action bar to show a dropdown list.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         // Set up the dropdown list navigation in the action bar.
         actionBar.setListNavigationCallbacks(
         // Specify a SpinnerAdapter to populate the dropdown list.
                 new ArrayAdapter<String>(actionBar.getThemedContext(),
                         android.R.layout.simple_list_item_1,
                         android.R.id.text1, new String[] {
                                 getString(R.string.title_section1),
                                 getString(R.string.title_section2),
                                 getString(R.string.title_section3), }), this);
         
         if (Musubi.isMusubiInstalled(this)) {
             mMusubi = Musubi.getInstance(this);
         }
         
         SQLiteOpenHelper databaseSource = App.getDatabaseSource(this);
         mFeedManager = new FeedManager(databaseSource);
         mFollowingManager = new FollowingManager(databaseSource);
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
         // Restore the previously serialized current dropdown position.
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
             getActionBar().setSelectedNavigationItem(
                     savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         // Serialize the current dropdown position.
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                 .getSelectedNavigationIndex());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         Log.d(TAG, "clicked an item with id " + item.getItemId());
         Log.d(TAG, "want " + R.id.menu_follow);
         switch(item.getItemId()) {
         case R.id.menu_follow:
             if (mMusubi == null) {
                 // get people to the market to install Musubi
                 Log.d(TAG, "Musubi not installed");
                 new InstallMusubiDialogFragment().show(getSupportFragmentManager(), null);
                 return super.onOptionsItemSelected(item);
             }
             Log.d(TAG, "trying to add followers ");
             String action = ACTION_CREATE_FEED;
             int request = REQUEST_CREATE_FEED;
             Uri feedUri = null;
             MFeed feedEntry = mFeedManager.getFeed(EntryType.App); // TODO: make this generic
             if (feedEntry != null) {
                 feedUri = feedEntry.feedUri;
             }
             if (feedUri != null) {
                 DbFeed feed = mMusubi.getFeed(feedUri);
                 if (feed != null) {
                     action = ACTION_EDIT_FEED;
                     request = REQUEST_EDIT_FEED;
                 } else {
                     // Delete broken feed entries
                     mFeedManager.deleteFeed(EntryType.App); // TODO: make this generic
                 }
             }
             Intent intent = new Intent(action);
             if (feedUri != null) {
                 intent.setData(feedUri);
                 intent.putExtra(ADD_TITLE, ADD_HEADER);
             }
             startActivityForResult(intent, request);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onNavigationItemSelected(int position, long id) {
         // When the given dropdown item is selected, show its contents in the
         // container view.
         Fragment fragment = new DummySectionFragment();
         Bundle args = new Bundle();
         args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
         fragment.setArguments(args);
         getSupportFragmentManager().beginTransaction()
                 .replace(R.id.container, fragment).commit();
         return true;
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CREATE_FEED && resultCode == RESULT_OK) {
             if (data == null || data.getData() == null) {
                 return;
             }
             
             Uri feedUri = data.getData();
             Log.d(TAG, "feedUri: " + feedUri);
             
             // save the feed uri
             MFeed feedEntry = new MFeed();
             feedEntry.feedUri = feedUri;
             feedEntry.type = EntryType.App; // TODO: make this generic
             mFeedManager.insertFeed(feedEntry);
             
             DbFeed feed = mMusubi.getFeed(feedUri);
             Log.d(TAG, "me: " + feed.getLocalUser().getId() + ", " + feed.getLocalUser().getName());
             
             JSONObject json = new JSONObject();
             try {
                 json.put("working", true);
             } catch (JSONException e) {
                 Log.e(TAG, "json issue", e);
                 return;
             }
             
             feed.postObj(new MemObj("rectacular", json));
             
             // Save members (these are the people I follow)
             List<DbIdentity> members = feed.getMembers();
             for (DbIdentity member : members) {
                 if (!member.isOwned()) {
                     Log.d(TAG, "member: " + member.getId() + ", " + member.getName());
                     MFollowing following = new MFollowing();
                     following.feedId = feedEntry.id;
                     following.userId = member.getId();
                     mFollowingManager.insertFollowing(following);
                     // TODO: update each member
                 }
             }
         } else if (requestCode == REQUEST_EDIT_FEED && resultCode == RESULT_OK) {
             if (data == null || data.getData() == null) {
                 return;
             }
             Uri feedUri = data.getData();
             Log.d(TAG, "feedUri: " + feedUri);
             MFeed feedEntry = mFeedManager.getFeed(EntryType.App); // TODO: make this generic
             Set<MFollowing> followingSet = mFollowingManager.getFollowing(feedEntry.id);
             Set<String> userIds = new HashSet<String>();
             for (MFollowing following : followingSet) {
                 userIds.add(following.userId);
             }
             DbFeed feed = mMusubi.getFeed(feedUri);
             List<DbIdentity> members = feed.getMembers();
             for (DbIdentity member : members) {
                 if (!member.isOwned()) {
                     Log.d(TAG, "member: " + member.getId() + ", " + member.getName());
                     if (!userIds.contains(member.getId())) {
                         MFollowing following = new MFollowing();
                         following.feedId = feedEntry.id;
                         following.userId = member.getId();
                         mFollowingManager.insertFollowing(following);
                         // TODO: update the new members
                     }
                 }
             }
         }
     }
     
     private class InstallMusubiDialogFragment extends DialogFragment {
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the Builder class for convenient dialog construction
             AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
             builder.setMessage(R.string.install_musubi)
                    .setTitle(R.string.no_musubi)
                    .setIcon(R.drawable.musubi_icon)
                    .setPositiveButton(R.string.google_play, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent market = Musubi.getMarketIntent();
                            getActivity().startActivity(market);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
             // Create the AlertDialog object and return it
             return builder.create();
         }
     }
 
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     public static class DummySectionFragment extends Fragment {
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public DummySectionFragment() {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             // Create a new TextView and set its text to the fragment's section
             // number argument value.
             TextView textView = new TextView(getActivity());
             textView.setGravity(Gravity.CENTER);
             textView.setText(Integer.toString(getArguments().getInt(
                     ARG_SECTION_NUMBER)));
             return textView;
         }
     }
 
 }
