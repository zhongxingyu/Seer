 
 package org.clc.android.app.redbox;
 
 import android.content.Context;

 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.android.actionbarcompat.ActionBarActivity;
 
 import org.clc.android.app.redbox.ad.AdvertisementManager;
 import org.clc.android.app.redbox.data.ActionHistoryManager.OnHistoryChangeListener;
 import org.clc.android.app.redbox.data.ActionRecord;
 import org.clc.android.app.redbox.data.BlockSetting;
 import org.clc.android.app.redbox.data.DataManager;
 import org.clc.android.app.redbox.data.PatternSetting;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 
 public class RedBoxHistoryActivity extends ActionBarActivity implements
         OnHistoryChangeListener {
     private static final String TAG = "RedBox_history";
 
     private ListView mRecordsListView;
     private View mAdView;
     private RecordsListAdapter mAdapter;
 
     private LayoutInflater mLayoutInflater;
 
     private View.OnClickListener mReactionClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             final View parent = (View) v.getParent();
             int position = (Integer) parent.getTag();
             final ActionRecord record = DataManager.getInstance().getHistory(
                     position);
 
             final BlockSetting matchedRule = record.mMatchedRule;
             final int id = DataManager.getInstance().getId(matchedRule);
             if (id == -1) {
                 Toast.makeText(RedBoxHistoryActivity.this, R.string.can_not_find_this_rule,
                         Toast.LENGTH_SHORT).show();
                 return;
             }
 
             Intent settingIntent = new Intent();
 
             BlockSetting setting = DataManager.getInstance().get(id);
             if (setting instanceof PatternSetting) {
                 settingIntent.setClass(RedBoxHistoryActivity.this,
                         RedBoxPatternSettingActivity.class);
             } else {
                 settingIntent.setClass(RedBoxHistoryActivity.this,
                         RedBoxBlockSettingActivity.class);
             }
             settingIntent.putExtra(RedBoxActivity.ID, id);
 
             RedBoxHistoryActivity.this.startActivity(settingIntent);
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.history_activity_layout);
 
         mRecordsListView = (ListView) findViewById(R.id.recordsList);
 
         mAdapter = new RecordsListAdapter();
         mRecordsListView.setAdapter(mAdapter);
 
         DataManager.getInstance().setOnHistoryChangeListener(this);
 
         mAdView = AdvertisementManager.getAdvertisementView(this);
         LinearLayout adLayout = (LinearLayout) findViewById(R.id.advertiseLayout);
         adLayout.addView(mAdView);
     }
 
     @Override
     public void onDestroy() {
         AdvertisementManager.destroyAd(mAdView);
         super.onDestroy();
     }
 
     @Override
     public void onHistoryChanged() {
         mAdapter.notifyDataSetChanged();
     }
 
     private class RecordsListAdapter extends BaseAdapter {
         RecordsListAdapter() {
             super();
             mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         }
 
         @Override
         public int getCount() {
             return DataManager.getInstance().getHistorySize();
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View recordList;
             if (convertView == null) {
                 recordList = mLayoutInflater.inflate(R.layout.record_list,
                         parent, false);
             } else {
                 recordList = convertView;
             }
 
             final ActionRecord record = DataManager.getInstance().getHistory(
                     position);
             final BlockSetting rule = record.mMatchedRule;
 
             final TextView from = (TextView) recordList
                     .findViewById(R.id.record_from_textView);
             final TextView when = (TextView) recordList
                     .findViewById(R.id.record_when_textView);
             final TextView matchedBy = (TextView) recordList
                     .findViewById(R.id.record_matched_by_textView);
             final TextView rejectedCall = (TextView) recordList
                     .findViewById(R.id.record_rejected_call_textView);
             final TextView deletedCallLog = (TextView) recordList
                     .findViewById(R.id.record_deleted_call_log_textView);
             final TextView sentAutoSMS = (TextView) recordList
                     .findViewById(R.id.record_sent_auto_sms_textView);
             final TextView autoSMS = (TextView) recordList
                     .findViewById(R.id.record_auto_sms_textView);
 
             from.setText(getString(R.string.record_from, rule.mNumber));
 
             SimpleDateFormat format = new SimpleDateFormat(
                     "yyyy.MM.dd hh:mm:ss");
             Timestamp timeStamp = new Timestamp(record.mTimeStamp);
             when.setText(format.format(timeStamp));
 
             String ruleName = rule.mAlias;
             if (ruleName == null || "".equals(ruleName)) {
                 ruleName = rule.mNumber;
             }
             matchedBy
                     .setText(getString(R.string.record_matched_rule, ruleName));
 
             if (!rule.mRejectCall) {
                 rejectedCall.setVisibility(View.GONE);
             } else {
                 rejectedCall.setVisibility(View.VISIBLE);
             }
 
             if (!rule.mDeleteCallLog) {
                 deletedCallLog.setVisibility(View.GONE);
             } else {
                 deletedCallLog.setVisibility(View.VISIBLE);
             }
             if (!rule.mSendAutoSMS) {
                 sentAutoSMS.setVisibility(View.GONE);
                 autoSMS.setVisibility(View.GONE);
             } else {
                 sentAutoSMS.setVisibility(View.VISIBLE);
                 autoSMS.setVisibility(View.VISIBLE);
                 autoSMS.setText(rule.mAutoSMS);
             }
 
             recordList.setTag((Integer) position);
 
             View reaction = recordList.findViewById(R.id.reaction_for_record);
             reaction.setOnClickListener(mReactionClickListener);
 
             return recordList;
         }
 
         @Override
         public Object getItem(int position) {
             return DataManager.getInstance().getHistory(position);
         }
 
         @Override
         public long getItemId(int position) {
             return position;
         }
     }
 }
