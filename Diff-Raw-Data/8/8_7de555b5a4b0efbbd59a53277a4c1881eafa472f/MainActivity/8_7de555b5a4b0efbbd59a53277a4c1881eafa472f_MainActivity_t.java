 package com.ideaboardz.android.activities;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.*;
 import com.crittercism.app.Crittercism;
 import com.ideaboardz.android.R;
 import com.ideaboardz.android.constants.Constants;
 import com.ideaboardz.android.utilities.BoardListAdapter;
 import com.ideaboardz.android.utilities.Font;
 import com.ideaboardz.android.utilities.JSONParser;
 import com.ideaboardz.android.utilities.SharedData;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
import java.net.URLEncoder;
 
 public class MainActivity extends Activity {
 
     private MainActivity context = this;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Crittercism.init(getApplicationContext(), "50ed62ad8cb83141b400000d");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         Button buttonForDemo = (Button) findViewById(R.id.viewTestBoard);
         Button buttonForFaq = (Button) findViewById(R.id.faq);
         Button buttonForFeedback = (Button) findViewById(R.id.feedback);
         Button buttonForViewing = (Button) findViewById(R.id.viewBoard);
         Button buttonForRecentBoards = (Button) findViewById(R.id.recent);
         buttonForDemo.setTypeface(Font.setFont(this));
         buttonForViewing.setTypeface(Font.setFont(this));
         buttonForFaq.setTypeface(Font.setFont(this));
         buttonForFeedback.setTypeface(Font.setFont(this));
         buttonForRecentBoards.setTypeface(Font.setFont(this));
 
     }
 
     @Override
     public void onStart() {
         super.onStart();
 //        EasyTracker.getInstance().activityStart(this);
     }
 
     @Override
     public void onStop() {
         super.onStop();
         // EasyTracker.getInstance().activityStop(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public void viewDemo(View view) {
         Intent intent = new Intent(this, ViewSectionActivity.class);
         startViewSectionActivity("test", "2", intent);
     }
 
     public void viewBoard(View view) {
         final Intent intent = new Intent(this, ViewSectionActivity.class);
         alertForDetails(intent);
     }
 
     public void faq(View view) {
         Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.ideaboardz.com/page/faq#android"));
         startActivity(browserIntent);
     }
 
     public void recent(View view) {
         alertForRecentBoards();
     }
 
     private void alertForRecentBoards() {
 
         SharedPreferences sharedPreferences = getSharedPreferences(SharedData.PREFS_NAME, 0);
         String data = sharedPreferences.getString("boards", null);
         final String[] recentBoardNames = JSONParser.parseStringToRecentBoardsName(data);
         if (recentBoardNames == null || recentBoardNames.length == 0) {
             generateToastForNoRecentBoards();
             return;
         }
         buildAlert(recentBoardNames, JSONParser.parseStringToRecentBoardsId(data));
     }
 
     private void buildAlert(final String[] recentBoardNames, final String[] recentBoardId) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         LayoutInflater inflater = getLayoutInflater();
         View view = inflater.inflate(R.layout.alert_box_heading, null);
         TextView textView = (TextView) view.findViewById(R.id.heading);
         textView.setText("Recent boardz..!!!");
         builder.setCustomTitle(view);
         DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 Intent intent = new Intent(context, ViewSectionActivity.class);
                 startViewSectionActivity(recentBoardNames[item], recentBoardId[item], intent);
             }
         };
         ListAdapter adapter = new BoardListAdapter(this, decodeList(recentBoardNames));
         builder.setAdapter(adapter, listener);
         AlertDialog alert = builder.create();
         alert.show();
     }
 
     private String[] decodeList(String[] recentBoardNames) {
         String decodedList[] = new String[recentBoardNames.length];
         int index = 0;
         for (String recentBoardName : recentBoardNames) {
             try {
                 decodedList[index] = URLDecoder.decode(recentBoardName, "UTF-8");
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             }
         }
         return decodedList;
     }
 
     private void generateToastForNoRecentBoards() {
         LayoutInflater inflater = getLayoutInflater();
         View layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout_root));
 
         TextView text = (TextView) layout.findViewById(R.id.text);
         text.setText("No Recent Boards.");
         Toast toast = new Toast(getApplicationContext());
         toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         toast.setDuration(Toast.LENGTH_SHORT);
         toast.setView(layout);
         toast.show();
     }
 
     public void feedback(View view) {
         Intent intent = new Intent(this, ViewSectionActivity.class);
         startViewSectionActivity("Feedback", "6733", intent);
     }
 
     private void alertForDetails(final Intent intent) {
         final AlertDialog.Builder alert = new AlertDialog.Builder(this);
         LayoutInflater li = LayoutInflater.from(this);
         final View promptsView = li.inflate(R.layout.select_board, null);
         alert.setView(promptsView);
         alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
                 EditText boardKeyText = (EditText) promptsView.findViewById(R.id.boardKey);
                 EditText boardIdText = (EditText) promptsView.findViewById(R.id.boardId);
                 String boardKey = boardKeyText.getText().toString();
                 String boardId = boardIdText.getText().toString();
                 EditText urlEditText = (EditText) promptsView.findViewById(R.id.url);
                 String url = urlEditText.getText().toString();
                 if (boardId.length() != 0 && boardKey.length() != 0) {
                    try {
                        startViewSectionActivity(URLEncoder.encode(boardKey, "UTF-8").replace("+", "%20"), boardId, intent);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                 } else if (url.length() != 0) {
                     startViewSectionActivity(getBoardKeyFromUrl(url), getBoardIdFromUrl(url), intent);
                 }
             }
         });
 
         alert.show();
     }
 
 
     private String getBoardKeyFromUrl(String url) {
         String smallUrl = url.substring(0, url.lastIndexOf('/'));
         return url.substring(smallUrl.lastIndexOf('/') + 1, smallUrl.length());
     }
 
     private String getBoardIdFromUrl(String url) {
         return url.substring(url.lastIndexOf('/') + 1, url.length());
     }
 
     private void startViewSectionActivity(String boardKey, String boardId, Intent intent) {
         intent.putExtra(Constants.BOARD_KEY, boardKey);
         intent.putExtra(Constants.BOARD_ID, boardId);
         startActivity(intent);
     }
 
     @Override
     public void onBackPressed() {
         moveTaskToBack(true);
         super.onBackPressed();
     }
 }
