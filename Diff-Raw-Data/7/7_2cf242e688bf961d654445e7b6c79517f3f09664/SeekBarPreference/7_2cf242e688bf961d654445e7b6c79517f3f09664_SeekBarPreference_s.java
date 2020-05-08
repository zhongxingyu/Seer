 /* The following code was written by Matthew Wiggins 
  * and is released under the APACHE 2.0 license 
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package com.slushpupie.widget;
 
 import android.content.Context;
 import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
   private static final String androidns = "http://schemas.android.com/apk/res/android";
   //When an app imports a library, the namespace becomes that of the app. Since we cant know it ahead of
   //time, we are going to rely on the string resources to provide the name. Ugly hack. 
  //private static final String appns = "http://schemas.android.com/apk/res/com.slushpupie.livewallpaper";
   
   private SeekBar mSeekBar;
   private TextView mSplashText, mValueText;
   private Context mContext;
 
   private String mDialogMessage, mSuffix;
   private int mDefault, mMin, mMax, mValue = 0;
 
   public SeekBarPreference(Context context, AttributeSet attrs) {
     super(context, attrs);
    String appns = context.getString(R.string.package_ns);
     mContext = context;
 
     int res = attrs.getAttributeResourceValue(androidns, "dialogMessage", -1);
     if (res == -1) {
       mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
     } else {
       mDialogMessage = context.getText(res).toString();
     }
     res = attrs.getAttributeResourceValue(androidns, "text", -1);
     if (res == -1) {
       mSuffix = attrs.getAttributeValue(androidns, "text");
     } else {
       mSuffix = context.getText(res).toString();
     }
 
     
     mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
     mMax = attrs.getAttributeIntValue(appns, "max", 100);
     mMin = attrs.getAttributeIntValue(appns, "min", 0);
 
   }
 
   @Override
   protected View onCreateDialogView() {
     LinearLayout.LayoutParams params;
     LinearLayout layout = new LinearLayout(mContext);
     layout.setOrientation(LinearLayout.VERTICAL);
     layout.setPadding(6, 6, 6, 6);
 
     mSplashText = new TextView(mContext);
     if (mDialogMessage != null)
       mSplashText.setText(mDialogMessage);
     layout.addView(mSplashText);
 
     mValueText = new TextView(mContext);
     mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
     mValueText.setTextSize(32);
     params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
         LinearLayout.LayoutParams.WRAP_CONTENT);
     layout.addView(mValueText, params);
 
     mSeekBar = new SeekBar(mContext);
     mSeekBar.setOnSeekBarChangeListener(this);
     layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
         LinearLayout.LayoutParams.WRAP_CONTENT));
 
     if (shouldPersist())
       mValue = getPersistedInt(mDefault);
 
     mSeekBar.setMax(mMax-mMin);
     mSeekBar.setProgress(mValue-mMin);
     return layout;
   }
 
   @Override
   protected void onBindDialogView(View v) {
     super.onBindDialogView(v);
     mSeekBar.setMax(mMax-mMin);
     mSeekBar.setProgress(mValue-mMin);
   }
 
   @Override
   protected void onSetInitialValue(boolean restore, Object defaultValue) {
     super.onSetInitialValue(restore, defaultValue);
     if (restore)
       mValue = shouldPersist() ? getPersistedInt(mDefault) : 0;
     else
       mValue = (Integer) defaultValue;
   }
 
   public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
 	  
     String t = String.valueOf(value+mMin);
     mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
     if (shouldPersist())
       persistInt(value+mMin);
     callChangeListener(new Integer(value+mMin));
   }
 
   public void onStartTrackingTouch(SeekBar seek) {}
 
   public void onStopTrackingTouch(SeekBar seek) {}
 
   public void setMax(int max) {
     mMax = max;
   }
 
   public int getMax() {
     return mMax;
   }
 
   public void setProgress(int progress) {
     mValue = progress;
     if (mSeekBar != null)
       mSeekBar.setProgress(progress);
   }
 
   public int getProgress() {
     return mValue;
   }
 
 }
