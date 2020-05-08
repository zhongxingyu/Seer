 
 package com.allplayers.android;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.allplayers.android.activities.AllplayersSherlockActivity;
 import com.allplayers.objects.GroupData;
 import com.allplayers.objects.GroupMemberData;
 import com.allplayers.rest.RestApiV1;
 import com.devspark.sidenavigation.SideNavigationView;
 import com.devspark.sidenavigation.SideNavigationView.Mode;
 
 public class GroupPageActivity extends AllplayersSherlockActivity {
 
     private GroupData group;
     private ArrayList<GroupMemberData> membersList;
     private boolean isMember = false, isLoggedIn = false;
     private ProgressBar loading;
 
     /**
      * Called when the activity is first created, this creates the Action Bar
      * and sets up the Side Navigation Menu as well as assigning some variables.
      *
      * @param savedInstanceState: Saved data from the last instance of the
      *            activity.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         group = (new Router(this)).getIntentGroup();
 
         setContentView(R.layout.grouppage);
         loading = (ProgressBar) findViewById(R.id.progress_indicator);
 
         actionbar = getSupportActionBar();
         actionbar.setIcon(R.drawable.menu_icon);
         actionbar.setDisplayShowTitleEnabled(false);
 
         TextView title = new TextView(this);
         title.setText(group.getTitle());
         title.setTextSize(20);
         title.setTypeface(Typeface.DEFAULT_BOLD);
         title.setTextColor(Color.WHITE);
         title.setLines(1);
         title.setEllipsize(TextUtils.TruncateAt.END);
         title.setPadding(0, 15, 0, 0);
         ActionBar.LayoutParams params = new ActionBar.LayoutParams(Gravity.CENTER);
         actionbar.setCustomView(title, params);
         actionbar.setDisplayShowCustomEnabled(true);
 
         sideNavigationView = (SideNavigationView) findViewById(R.id.side_navigation_view);
         sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
         sideNavigationView.setMenuClickCallback(this);
         sideNavigationView.setMode(Mode.LEFT);
         new GetGroupMembersByGroupIdTask().execute(group.getUUID());
         new GetGroupLocationTask().execute(group.getUUID());
     }
 
     /**
      * Gets a remote image using a REST call.
      */
     public class GetRemoteImageTask extends AsyncTask<String, Void, Bitmap> {
 
         protected Bitmap doInBackground(String... logoURL) {
 
             return RestApiV1.getRemoteImage(logoURL[0]);
         }
 
         protected void onPostExecute(Bitmap logo) {
             loading.setVisibility(View.GONE);
             ImageView imView = (ImageView) findViewById(R.id.groupLogo);
            if(logo == null) {
                imView.setImageResource(R.drawable.group_default_logo);
            } else {
                imView.setImageBitmap(logo);
            }
 
             final ImageButton groupMembersButton = (ImageButton) findViewById(R.id.groupMembersButton);
             if (isMember && isLoggedIn) groupMembersButton.setVisibility(View.VISIBLE);
             groupMembersButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this))
                                     .getGroupMembersActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             final ImageButton groupEventsButton = (ImageButton) findViewById(R.id.groupEventsButton);
             if (isMember && isLoggedIn) groupEventsButton.setVisibility(View.VISIBLE);
             groupEventsButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this))
                                     .getGroupEventsActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             final ImageButton groupPhotosButton = (ImageButton) findViewById(R.id.groupPhotosButton);
             if (isMember && isLoggedIn) groupPhotosButton.setVisibility(View.VISIBLE);
             groupPhotosButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this))
                                     .getGroupAlbumsActivityIntent(group);
                     startActivity(intent);
                 }
             });
 
             final ImageButton groupLocationButton = (ImageButton) findViewById(R.id.groupLocationButton);
             if (isMember && isLoggedIn) groupLocationButton.setVisibility(View.VISIBLE);
             groupLocationButton.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = (new Router(GroupPageActivity.this))
                                     .getGroupLocationActivityIntent(group);
                     startActivity(intent);
                 }
             });
         }
     }
 
     public class GetGroupLocationTask extends AsyncTask<String, Void, String> {
         protected String doInBackground(String... group_uuid) {
             return RestApiV1.getGroupInformationByGroupId(group_uuid[0]);
         }
 
         protected void onPostExecute(String jsonResult) {
             try {
                 JSONObject groupInfo = new JSONObject(jsonResult);
                 group.setZip(groupInfo.getJSONObject("location").getString("zip"));
                 group.setLatLon(groupInfo.getJSONObject("location").getString("latitude"), groupInfo.getJSONObject("location").getString("longitude"));
             } catch (JSONException e) {
                 e.printStackTrace();
             }
 
         }
     }
 
     /*
      * Checks if the user is a group member. If the user is a group member the
      * group page interface is set up.
      */
     public class GetGroupMembersByGroupIdTask extends AsyncTask<String, Void, String> {
         protected String doInBackground(String... group_uuid) {
             // @TODO: Move to asynchronous loading.
             return RestApiV1.getGroupMembersByGroupId(group_uuid[0], 0);
         }
 
         protected void onPostExecute(String jsonResult) {
             isLoggedIn = RestApiV1.isLoggedIn();
             GroupMembersMap groupMembers = new GroupMembersMap(jsonResult);
             membersList = groupMembers.getGroupMemberData();
             String currentUUID = RestApiV1.getCurrentUserUUID();
             for (int i = 0; i < membersList.size(); i++) {
                 if (membersList.get(i).getUUID().equals(currentUUID)) {
                     isMember = true;
                     break;
                 }
             }
             new GetRemoteImageTask().execute(group.getLogo());
         }
     }
 }
