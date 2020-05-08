 package com.tools.tvguide.activities;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.UpdateManager;
 import com.tools.tvguide.managers.UrlManager;
 import com.tools.tvguide.components.PackageInstaller;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.view.View;
import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MoreActivity extends Activity implements Callback 
 {
     private Dialog mCheckingDialog;
     private Dialog mInfoDialog;
     private Dialog mDownloaDialog;
     private TextView mCheckTextView;
    private ImageView mUpdateNewIcon;
     private Handler mUiHandler;
     private enum SelfMessage {Msg_Need_Update, Msg_No_Need_Update};
     private enum TextStatus {State_Check, State_Upgrade};
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_more);
         createDialogs();
         
         mCheckTextView = (TextView)findViewById(R.id.more_version_check);
         String versionFormat = mCheckTextView.getText().toString();
         String finalVersion = String.format(versionFormat, AppEngine.getInstance().getUpdateManager().getCurrentVersionName());
         mCheckTextView.setText(finalVersion);
         mCheckTextView.setTag(TextStatus.State_Check);
         
        mUpdateNewIcon = (ImageView) findViewById(R.id.more_update_new_icon);
         mUiHandler = new Handler(this);
     }
     
     @Override
     protected void onResume()
     {
         super.onResume();
         AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
         {
             @Override
             public void OnIOComplete(CheckResult result) 
             {
                 if (result == CheckResult.Need_Update)
                     mUiHandler.obtainMessage(SelfMessage.Msg_Need_Update.ordinal()).sendToTarget();
                 else
                     mUiHandler.obtainMessage(SelfMessage.Msg_No_Need_Update.ordinal()).sendToTarget();
             }
         });
     }
     
     private void createDialogs()
     {
         mCheckingDialog = new AlertDialog.Builder(MoreActivity.this)
             .setTitle(getResources().getString(R.string.check_update))
             .setMessage("正在进行，请稍等...")
             .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
             {
                 @Override
                 public void onClick(DialogInterface dialog, int which) 
                 {
                     
                 }
             })
             .create();
         
         mInfoDialog = new AlertDialog.Builder(MoreActivity.this)
             .setTitle("提示")
             .setMessage("已经是最新版本，无需更新")
             .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
             {
                 @Override
                 public void onClick(DialogInterface dialog, int which) 
                 {
                 }
             })
             .create();
     }
 
     public void onClick(View view)
     {
         switch (view.getId())
         {
             case R.id.more_alarms:
                 Intent alarmIntent = new Intent(MoreActivity.this, AlarmSettingActivity.class);
                 startActivity(alarmIntent);
                 break;
             case R.id.more_check_new_ver:
                 TextStatus state = (TextStatus) mCheckTextView.getTag();
                 if (state == TextStatus.State_Check)
                     checkNewVersion();
                 else if (state == TextStatus.State_Upgrade)
                     upgrade();
                 break;
             case R.id.more_feedback:
                 Intent feedbackIntent = new Intent(MoreActivity.this, FeedbackActivity.class);
                 startActivity(feedbackIntent);
                 break;
             case R.id.more_about:
                 showAbout();
                 break;
             case R.id.more_clear_cache:
                 clearCache();
                 break;
             case R.id.more_quit:
                 quit();
                 break;
         }
     }
     
     private void checkNewVersion()
     {
         mCheckingDialog.show();
         AppEngine.getInstance().getUpdateManager().checkUpdate(new UpdateManager.IOCompleteCallback() 
         {
             public void OnIOComplete(CheckResult result) 
             {
                 if (result == CheckResult.Need_Update)
                     mUiHandler.obtainMessage(SelfMessage.Msg_Need_Update.ordinal()).sendToTarget();
                 else
                     mUiHandler.obtainMessage(SelfMessage.Msg_No_Need_Update.ordinal()).sendToTarget();
             }
         });
     }
     
     private void showAbout()
     {
         AlertDialog dialog = new AlertDialog.Builder(MoreActivity.this)
             .setIcon(R.drawable.application)
             .setTitle(getResources().getString(R.string.app_name))
             .setMessage("作者：大眼牛工作室\n邮箱：bigeyecow@qq.com\n版权：©2013-2014")
             .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() 
             {
                 @Override
                 public void onClick(DialogInterface dialog, int which) 
                 {
                     dialog.dismiss();
                 }
             })
             .create();
         dialog.show();
     }
     
     private void upgrade()
     {
         String newUrl = UrlManager.tryToReplaceHostNameWithIP(AppEngine.getInstance().getUpdateManager().getUrl());
         new PackageInstaller(MoreActivity.this).installRemotePackage(newUrl);
         Toast.makeText(MoreActivity.this, "正在下载...", Toast.LENGTH_SHORT).show();
     }
     
     private void clearCache()
     {
         AppEngine.getInstance().getCacheManager().clear();
         Toast.makeText(MoreActivity.this, "缓存已清除", Toast.LENGTH_SHORT).show();
     }
     
     private void quit()
     {
         AppEngine.getInstance().prepareBeforeExit();
         finish();
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         SelfMessage selfMsg = SelfMessage.values()[msg.what];
         switch (selfMsg)
         {
             case Msg_Need_Update:
                 if (mCheckingDialog.isShowing())
                 {
                     mCheckingDialog.dismiss();
                     
                     String downloadMsg;
                     if (AppEngine.getInstance().getUpdateManager().getLatestVersionName() != null)
                         downloadMsg = "发现最新版本(" + AppEngine.getInstance().getUpdateManager().getLatestVersionName() + "), 是否需要更新？";
                     else
                         downloadMsg = "发现最新版本，是否需要更新？";
                     
                     mDownloaDialog = new AlertDialog.Builder(MoreActivity.this)
                         .setTitle("下载")
                         .setMessage(downloadMsg)
                         .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
                         {
                             @Override
                             public void onClick(DialogInterface dialog, int which) 
                             {
                             }
                         })
                         .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener()
                         {
                             @Override
                             public void onClick(DialogInterface dialog, int which) 
                             {
                                 upgrade();
                             }
                         })
                         .create();
                     mDownloaDialog.show();
                 }
                 mUpdateNewIcon.setVisibility(View.VISIBLE);
                 String upgradeFormat = getResources().getString(R.string.version_upgrade);
                 String upgradeInfo = String.format(upgradeFormat, AppEngine.getInstance().getUpdateManager().getCurrentVersionName(), 
                                                     AppEngine.getInstance().getUpdateManager().getLatestVersionName());
                 mCheckTextView.setText(upgradeInfo);
                 mCheckTextView.setTag(TextStatus.State_Upgrade);
                 break;
             case Msg_No_Need_Update:
                 if (mCheckingDialog.isShowing())
                 {
                     mCheckingDialog.dismiss();
                     mInfoDialog.show();
                 }
                 mUpdateNewIcon.setVisibility(View.INVISIBLE);
                 break;
         }
         return false;
     }
 }
