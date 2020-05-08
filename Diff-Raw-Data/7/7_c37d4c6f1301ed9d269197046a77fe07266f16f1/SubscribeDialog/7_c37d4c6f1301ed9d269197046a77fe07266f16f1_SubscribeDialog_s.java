 package org.gnuton.newshub;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.support.v4.app.DialogFragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 import org.gnuton.newshub.adapters.FeedListAdapter;
 import org.gnuton.newshub.adapters.LanguageSpinnerAdapter;
 import org.gnuton.newshub.db.DbHelper;
 import org.gnuton.newshub.db.RSSFeedDataSource;
 import org.gnuton.newshub.tasks.DownloadWebTask;
 import org.gnuton.newshub.types.RSSFeed;
 import org.gnuton.newshub.utils.FontsProvider;
 import org.gnuton.newshub.utils.MyApp;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Created by gnuton on 5/28/13.
  */
 public class SubscribeDialog extends DialogFragment implements ListView.OnItemClickListener, DownloadWebTask.OnRequestCompletedListener {
     private final String TAG = SubscribeDialog.class.getName();
     private CountDownTimer mSearchTiimer;
     private View mDlgLayout;
 
     private final RSSFeedDataSource mFeedDataSource;
     private List<RSSFeed> mFeeds;
 
     private MainActivity mMainActivity;
     private ArrayAdapter<RSSFeed> adapter;
     //private final String mFindFeedsUrl = "https://ajax.googleapis.com/ajax/services/feed/find?v=1.0&q=";
     private final String mFindFeedsUrl = "http://rssfinder-gnuton.rhcloud.com/get";
 
     private ListView mListView;
     private Spinner mLanguageSpinner;
 
     public SubscribeDialog(){
         super();
         this.mMainActivity = MyApp.getInstance().mMainActivity;
         if (mMainActivity == null){
             this.mFeedDataSource = null;
             return;
         }
         this.mFeedDataSource = new RSSFeedDataSource(mMainActivity);
     }
 
     // ListView.OnClickListener
     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         //TextView v = (TextView) view;
         RSSFeed f = (RSSFeed) adapterView.getItemAtPosition(i);
         Log.d(TAG, "Added feed:" + f.title);
 
        if (f == null)
            return;
        mFeedDataSource.create(f);
         this.dismiss();
     }
 
     private void setBusyIndicatorStatus(Boolean busy){
         if (mDlgLayout == null)
             return;
 
         View spinner = mDlgLayout.findViewById(R.id.spinningImage);
         spinner.setVisibility(busy ? View.VISIBLE : View.GONE);
 
         if (busy){
             Animation animation = AnimationUtils.loadAnimation(MyApp.getContext(), R.animator.fadeout);
             spinner.startAnimation(animation);
         } else {
             spinner.clearAnimation();
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         final SubscribeDialog t = this;
 
         String[] providers = new String[] {};
 
         //Create dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
         mDlgLayout = inflater.inflate(R.layout.subscribe_dialog, null);
         builder.setView(mDlgLayout);
         builder.setTitle(R.string.subscribe_dialog_title);
 
         //Search button display magnifier icon
         final Button searchButton = (Button) mDlgLayout.findViewById(R.id.button43);
         searchButton.setTypeface(FontsProvider.getInstace().getTypeface("fontawesome-webfont"));
 
         /*
         builder.setItems(providers, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 Log.d(TAG, "CLICKED" + which);
                 RSSFeed feed = new RSSFeed(0, "TEST TITLE", "TEST URL");
                 mListener.onFeedSelected(feed);
                 // The 'which' argument contains the index position
                 // of the selected item
             }
         });*/
 
         // Set spinner
         String[] langs = LanguageSpinnerAdapter.getFlagNamesArray();
         mLanguageSpinner = (Spinner) mDlgLayout.findViewById(R.id.language_spinner);
 
         LanguageSpinnerAdapter langAdapter = new LanguageSpinnerAdapter(MyApp.getContext(), R.layout.language_spinner_item, langs);
 
         // Specify the layout to use when the list of choices appears
         //langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         // Apply the adapter to the spinner
         mLanguageSpinner.setAdapter(langAdapter);
         String localeLang = Locale.getDefault().getLanguage() + ".png";
         for (int i = 0; i < langs.length; ++i){
             if (localeLang.equals(langs[i])){
                 mLanguageSpinner.setSelection(i);
             }
         }
 /*
         mLanguageSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 Log.d(TAG, "ITEM SELECTED");
 
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                 Log.d(TAG, "NOTHING SELECTED");
             }
         });*/
 
         // Set click listeners for category buttons
         final ViewGroup categoriesLayout = (ViewGroup) mDlgLayout.findViewById(R.id.categories_layout);
         for (int i=0; i < categoriesLayout.getChildCount(); ++i){
             ViewGroup rowLayout = (ViewGroup) categoriesLayout.getChildAt(i);
             for (int j=0; j < rowLayout.getChildCount(); ++j){
                 Button b = null;
                 try {
                     b = (Button) rowLayout.getChildAt(j);
                 } catch (ClassCastException e){
                     continue;
                 }
                 b.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         Button b = (Button) view;
                         String text = (String) b.getText();
                         if (! text.equals(getResources().getString(R.string.category_search))){
                             final Spinner languageSpinner = (Spinner) mDlgLayout.findViewById(R.id.language_spinner);
                             searchFeeds(languageSpinner.getSelectedItem().toString(), text);
                         } else {
                             // layout in the dialog that holds spinner and textedit for searching feeds
                             View searchFeedLayout = mDlgLayout.findViewById(R.id.search_feed_layout);
                             searchFeedLayout.setVisibility(View.VISIBLE);
                             View categoriesLayout = mDlgLayout.findViewById(R.id.categories_layout);
                             categoriesLayout.setVisibility(View.GONE);
                             mFeeds.clear();
                         }
                     }
                 });
             }
         }
         // Binds SQLite to list
         Context ctx = this.getActivity();
 
 
         // Initlialize list
         mFeeds = new ArrayList<RSSFeed>();
         adapter = new FeedListAdapter(ctx, R.layout.feedlist_item, mFeeds);
         mListView = (ListView) mDlgLayout.findViewById(R.id.subscribe_listView);
         mListView. setEmptyView(mDlgLayout.findViewById(R.id.subscribe_emptyView));
         mListView.setAdapter(adapter);
         mListView.setOnItemClickListener(this);
         setBusyIndicatorStatus(false);
 
         // Add listeners to editText
         EditText et = (EditText) mDlgLayout.findViewById(R.id.subscribe_editText);
         et.addTextChangedListener( new TextWatcher() {
             class mCountDownTimer extends  CountDownTimer {
 
                 public mCountDownTimer(long millisInFuture, long countDownInterval) {
                     super(millisInFuture, countDownInterval);
                 }
 
                 @Override
                 public void onTick(long l) {}
 
                 @Override
                 public void onFinish() {
                     Log.d(TAG," Start searching");
 
                     final Spinner languageSpinner = (Spinner) mDlgLayout.findViewById(R.id.language_spinner);
                     final EditText queryEditText = (EditText) mDlgLayout.findViewById(R.id.subscribe_editText);
 
                     // CLOSE SOFT KEYBOARD
                     InputMethodManager imm = (InputMethodManager) MyApp.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(queryEditText.getWindowToken(), 0);
 
                     searchFeeds(languageSpinner.getSelectedItem().toString(), queryEditText.getText().toString());
                 }
             }
 
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
 
             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {}
 
             @Override
             public void afterTextChanged(Editable editable) {
                 int delayBeforeSearching = 3000;
 
                 if (mSearchTiimer != null)
                     mSearchTiimer.cancel();
 
                 mSearchTiimer = new mCountDownTimer(delayBeforeSearching, delayBeforeSearching).start();
             }
         });
 
 
         return builder.create();
     }
 
     @Override
     public void onDismiss(DialogInterface dialog) {
         super.onDismiss(dialog);
         Log.d(TAG, "Closing dialog");
         if (mMainActivity != null)
             mMainActivity.updateDrawerList();
     }
 
     @TargetApi(Build.VERSION_CODES.GINGERBREAD)
     private void searchFeeds(String language, final String query){
         setBusyIndicatorStatus(true);
         mFeeds.clear();
 
         StringBuilder sb = new StringBuilder(mFindFeedsUrl);
         if (language.isEmpty())
             language = "en";
         else
             language = language.replace(".png","");
         sb.append("?l=");
         sb.append(language);
         sb.append("?q=");
         sb.append(URLEncoder.encode(query));
         String url = sb.toString();
         new DownloadWebTask(this).execute(url);
     }
 
     @Override
     public void onRequestCompleted(String buffer) {
         mFeeds.clear();
         setBusyIndicatorStatus(false);
 
         if (buffer == null){
             Log.d(TAG, "Got empty buffer, no providers found");
             return;
         }
 
         Log.d(TAG, "Got new providers");
 
         try {
             // Scan google responses
             //jArray = new JSONObject(buffer).getJSONObject("responseData").getJSONArray("entries");
             JSONArray jArray = new JSONArray(buffer);
             for (int i=0; i< jArray.length(); ++i) {
                 JSONObject j = null;
                 j = jArray.getJSONObject(i);
 
                 //FIXME title contains unencoded chars
                 String title = j.getString(DbHelper.FEEDS_TITLE).replaceAll("</*b>","");
                 String url = j.getString(DbHelper.FEEDS_URL);
                 Log.d(TAG, title + " URL=" + url);
                 RSSFeed f = new RSSFeed(title, url);
                 mFeeds.add(f);
             }
             adapter.notifyDataSetChanged();
         } catch (JSONException e) {
             e.printStackTrace();
         }
     }
 }
