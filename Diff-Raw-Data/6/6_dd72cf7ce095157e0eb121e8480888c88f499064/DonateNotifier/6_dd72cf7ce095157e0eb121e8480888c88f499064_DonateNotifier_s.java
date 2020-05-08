 package com.vilagmegvaltas.shisha.utils;
 
 import com.vilagmegvaltas.shisha.R;
 
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class DonateNotifier {
    private final static String APP_TITLE = "Shisha Manager";
    private final static String APP_PNAME = "com.vilagmegvaltas.shisha";
     private static final String PAYPAL_LINK = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TY47JH5LK3AT8&lc=HU&item_name=Shisha%20Manager%20Donation&item_number=evogochi_donate&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted";
     
    private final static int LAUNCHES_UNTIL_PROMPT = 1;
     
     public static void app_launched(Context mContext) {
         SharedPreferences prefs = mContext.getSharedPreferences("donate", Context.MODE_PRIVATE);
         if (prefs.getBoolean("dontshowagain", false)) { return ; }
         
         SharedPreferences.Editor editor = prefs.edit();
         
         // Increment launch counter
         long launch_count = prefs.getLong("launch_count", 0) + 1;
         editor.putLong("launch_count", launch_count);
 
         // Get date of first launch
         Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
         if (date_firstLaunch == 0) {
             date_firstLaunch = System.currentTimeMillis();
             editor.putLong("date_firstlaunch", date_firstLaunch);
         }
         
         // Wait at least n days before opening
         if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            
                 showRateDialog(mContext, editor);
 
         }
         
         editor.commit();
     }   
     
     public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
         final Dialog dialog = new Dialog(mContext);
         dialog.setTitle(mContext.getString(R.string.donate_title));
 
         LinearLayout ll = new LinearLayout(mContext);
         ll.setOrientation(LinearLayout.VERTICAL);
         
         TextView tv = new TextView(mContext);
         tv.setText(mContext.getString(R.string.donate_descr));
         tv.setWidth(300);
         tv.setPadding(4, 0, 4, 10);
         ll.addView(tv);
         
         Button btn_donate = new Button(mContext);
         btn_donate.setText(mContext.getString(R.string.donate_button_donate));
         btn_donate.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	editor.putLong("launch_count", 0);
             	editor.commit();
             	mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL_LINK)));
                 dialog.dismiss();
             }
         });        
         ll.addView(btn_donate);
 
         Button btn_remindMe = new Button(mContext);
         btn_remindMe.setText(mContext.getString(R.string.donate_button_remind_later));
         btn_remindMe.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	editor.putLong("launch_count", 0);
             	editor.commit();
                 dialog.dismiss();
             }
         });
         ll.addView(btn_remindMe);
 
         Button btn_noThanks = new Button(mContext);
         btn_noThanks.setText(mContext.getString(R.string.donate_button_no_thanks));
         btn_noThanks.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 if (editor != null) {
                     editor.putBoolean("dontshowagain", true);
                     editor.commit();
                 }
                 dialog.dismiss();
             }
         });
         ll.addView(btn_noThanks);
 
         dialog.setContentView(ll);        
         dialog.show();        
     }
 }
