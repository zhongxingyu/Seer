 
 package com.toycode.pwmemo;
 
 import android.app.Application;
 import android.content.Context;
 import android.content.SharedPreferences.Editor;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public final class App extends Application {
     
     /**
      * Debug flag
      */
     static final public boolean DEBUG_FLAG = true;  
     
     /**
     * Debug ログを記録
      * 
     * @param obj 対象オブジェクト
     * @param msg メッセージ
      */
    static public final void debugLog(Object obj, String msg) {
         if (DEBUG_FLAG) {
            Log.d(obj.getClass().getName(), msg);
         }
     }
 
     /**
      * メッセージをトーストにして表示
      * 
      * @param context
      * @param message_id 表示するメッセージID
      */
     static public final void toastMessage(Context context, int message_id) {
         toastMessage(context, context.getString(message_id));
     }
 
     /**
      * メッセージをトーストにして表示
      * 
      * @param context
      * @param message 表示するメッセージ
      */
     static public final void toastMessage(Context context, String message) {
         Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
         toast.show();
     }
 
     /**
      * トースト表示
      * @param context
      * @param message_id
      * @param args
      */
     static public final void toastMessage(Context context, int message_id, Object... args) {
         String messageString = String.format(context.getString(message_id), args);
         toastMessage(context, messageString);
     }
 
     /**
      * TextView が空かどうか調べる
      * view それ自体が空でも、text が空でも "" がセットされていても true を返す
      */
     static public boolean isEmptyTextView(TextView tv) {
         if (tv == null) {
             return true;
         }
         CharSequence text = tv.getText();
         return (text == null) || (text.length() == 0);
     }
     
     /**
      * 文字列が空かどうか調べる
      * オブジェクト自体がnull であっても true を返す
      * @param cs
      * @return
      */
     static public boolean isEmptyCharSequence(CharSequence cs) {
         return (cs == null) || (cs.length() == 0);
     }
        
     /**
      * Get one application local String preference.
      */
     static public String getLocalStringPreference(Context context, String key, String defalutValue) {
         return PreferenceManager.getDefaultSharedPreferences(context).getString(key, defalutValue);
     }
 
     /**
      * Get one application local boolean preference.
      */
     static public boolean getLocalBooleanPreference(Context context, String key,
             boolean defalutValue) {
         return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defalutValue);
     }
 
     /**
      * Get one application local long preference.
      */
     static public long geLocaLongPreference(Context context, String key, long defalutValue) {
         return PreferenceManager.getDefaultSharedPreferences(context).getLong(key, defalutValue);
     }
 
     /**
      * Set one application local String preference.
      */
     static public void setLocalPreference(Context context, String key, String value) {
         Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                 .edit();
         editor.putString(key, value);
         editor.commit();
     }
 
     /**
      * Set one application local long preference.
      */
     static public void setLocalPreference(Context context, String key, long value) {
         Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                 .edit();
         editor.putLong(key, value);
         editor.commit();
     }
 
     /**
      * Set one application local boolean preference.
      */
     static public void setLocalPreference(Context context, String key, boolean value) {
         Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
                 .edit();
         editor.putBoolean(key, value);
         editor.commit();
     }
 
    /**
     * copy text to clipboard
     * @param context
     * @param text
     */
     static public void copyTextToClipboard(Context context, CharSequence text) {
         android.text.ClipboardManager cm = 
             (android.text.ClipboardManager) context
                 .getSystemService(Context.CLIPBOARD_SERVICE);
         cm.setText(text);
     }
 
 }
