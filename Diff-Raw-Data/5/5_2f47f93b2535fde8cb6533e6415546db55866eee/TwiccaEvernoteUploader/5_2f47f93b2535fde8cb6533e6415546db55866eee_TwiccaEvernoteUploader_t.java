 /*
  * Copyright 2011 Takuo Kitame.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jp.takuo.android.twicca.plugin.evernote;
 
 /* from EDAM sample */
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /* Android */
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class TwiccaEvernoteUploader extends Activity {
     public static final String SEED = "encrypt";
     private static final String LOG_TAG = "TwiccaEvernote";
     private static final int REQUEST_CODE = 210;
 
     /* hashtag related */
     private static final String SEARCH_URL = "https://twitter.com/search?q=%23";
     private static final String LATIN_ACCENTS_CHARS = "\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff\\u015f";
     private static final String HASHTAG_ALPHA_CHARS = "a-z" + LATIN_ACCENTS_CHARS +
             "\\u0400-\\u04ff\\u0500-\\u0527" +  // Cyrillic
            "\\u2de0-\\u2dff\\ua640-\\ua69f" +  // Cyrillic Extended A/B
             "\\u1100-\\u11ff\\u3130-\\u3185\\uA960-\\uA97F\\uAC00-\\uD7AF\\uD7B0-\\uD7FF" + // Hangul (Korean)
             "\\p{InHiragana}\\p{InKatakana}" +  // Japanese Hiragana and Katakana
             "\\p{InCJKUnifiedIdeographs}" +     // Japanese Kanji / Chinese Han
             "\\u3005\\u303b" +                  // Kanji/Han iteration marks
             "\\uff21-\\uff3a\\uff41-\\uff5a" +  // full width Alphabet
             "\\uff66-\\uff9f" +                 // half width Katakana
             "\\uffa1-\\uffdc";                  // half width Hangul (Korean)
     private static final String HASHTAG_ALPHA_NUMERIC_CHARS = "0-9\\uff10-\\uff19_" + HASHTAG_ALPHA_CHARS;
     private static final String HASHTAG_ALPHA = "[" + HASHTAG_ALPHA_CHARS +"]";
     private static final String HASHTAG_ALPHA_NUMERIC = "[" + HASHTAG_ALPHA_NUMERIC_CHARS +"]";
     private static final Pattern HASHTAG_PATTERN = Pattern.compile(
             "(^|[^&/" + HASHTAG_ALPHA_NUMERIC_CHARS + "])(#|\uFF03)(" +
              HASHTAG_ALPHA_NUMERIC + "*" +
              HASHTAG_ALPHA +
              HASHTAG_ALPHA_NUMERIC + "*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern HASHTAG_END = Pattern.compile("^(?:[#\uFF03]|://)");
     /* end hashtag related */
 
     private static final Pattern URL_PATTERN = Pattern.compile(
             "(https?://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", Pattern.CASE_INSENSITIVE);
     private static final String NOTE_PREFIX =
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">" +
         "<en-note>";
     private static final String NOTE_SUFFIX = "</en-note>";
 
     // Preferences keys
     public static final String PREF_EVERNOTE_USERNAME = "pref_evernote_username";
     public static final String PREF_EVERNOTE_PASSWORD = "pref_evernote_password";
     public static final String PREF_EVERNOTE_NOTEBOOK = "pref_evernote_notebook";
     public static final String PREF_EVERNOTE_TAGS     = "pref_evernote_tags";
     public static final String PREF_EVERNOTE_CRYPTED  = "pref_evernote_crypted";
     public static final String PREF_CONFIRM_DIALOG    = "pref_confirm_dialog";
     public static final String PREF_HASHTAG_CLIPTAG   = "pref_hashtag_cliptag";
     public static final String PREF_NAME_CLIPTAG      = "pref_name_cliptag";
 
     private Context mContext;
 
     // data from Twicca
     private String mScreenName;
     private String mUsername;
     private String mBodyText;
     private String mProfileImageUrl;
     private String mTweetId;
     private String mCreatedAt;
     private String mSource;
     
     // Evernote settings
     private SharedPreferences mPrefs;
     private String mEvernoteUsername;
     private String mEvernotePassword;
     private String mEvernoteNotebook;
     private String mEvernoteTags;
 
     // UI
     private EditText mEditNotebook;
     private Spinner mSpinner;
     private MultiAutoCompleteTextView mEditTags;
 
     // Caching
     private ECacheManager cacheManager;
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == REQUEST_CODE) run();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         mContext = getApplicationContext();
         cacheManager = new ECacheManager(mContext);
         Intent intent = getIntent();
         mBodyText = intent.getStringExtra(Intent.EXTRA_TEXT);
         mTweetId = intent.getStringExtra("id");
         mScreenName = intent.getStringExtra("user_screen_name");
         mUsername = intent.getStringExtra("user_name");
         mProfileImageUrl = intent.getStringExtra("user_profile_image_url_normal");
         mCreatedAt = intent.getStringExtra("created_at");
         mSource = intent.getStringExtra("source");
         run();
     }
 
     private void requestUpload ()
     {
         Time time = new Time();
         time.set(Long.parseLong(mCreatedAt));
         Matcher matcher = URL_PATTERN.matcher(mBodyText);
         mBodyText = matcher.replaceAll("<a target=\"_blank\" href=\"$0\">$0</a>").replace("\n", "<br />");
         mBodyText = autoLinkHashtags(mBodyText);
         String content =
             NOTE_PREFIX +
             "<table style='border-radius: 10px; background-color: #eeeeee'>" +
             "<tr><td valign='top' style='padding: 10px'>" +
             "<img src=\"" + mProfileImageUrl + "\"/>" +
             " </td>" +
             "<td style='padding: 10px'>" +
             "<b>" + mUsername + "(<a href=\"http://twitter.com/" + mScreenName + "\">" + "@" + mScreenName + "</a>)</b>" +
             "<p>" + mBodyText + "</p>" +
             "<a style='color: #888888' href=\"http://twitter.com/"+ mScreenName + "/statuses/"+ mTweetId + "\">" + time.format("%m/%d %H:%M:%S") + "</a>" +
             " <span style='color: #888888'>from " + mSource + "</span>" +
             "</td></tr>" +
             "</table>" +
             NOTE_SUFFIX;
 
         if (mPrefs.getBoolean(PREF_NAME_CLIPTAG, false)) {
             if (mEvernoteTags.length() > 0) {
                 mEvernoteTags += "," + mScreenName;
             } else {
                 mEvernoteTags = mScreenName;
             } // mEvernoteTags
         } // PREF_NAME_CLIPTAG
 
         Intent intent = new Intent(this, ClippingService.class);
         intent.putExtra("notebook", mEvernoteNotebook);
         intent.putExtra("tags", mEvernoteTags);
         intent.putExtra("title", "Tweet by " + mUsername +" (@" + mScreenName + ")");
         intent.putExtra("username", mEvernoteUsername);
         intent.putExtra("password", mEvernotePassword);
         intent.putExtra(Intent.EXTRA_TEXT, content);
         intent.putExtra("url", "http://twitter.com/"+ mScreenName + "/statuses/"+ mTweetId);
         Toast.makeText(mContext, getString(R.string.message_do_background), Toast.LENGTH_SHORT).show();
         startService(intent);
     }
 
     private void run () {
         mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
         mEvernoteUsername = mPrefs.getString(PREF_EVERNOTE_USERNAME, "");
         mEvernotePassword = mPrefs.getString(PREF_EVERNOTE_PASSWORD, "");
         mEvernoteNotebook = mPrefs.getString(PREF_EVERNOTE_NOTEBOOK, "");
         mEvernoteTags = mPrefs.getString(PREF_EVERNOTE_TAGS, "");
 
         String crypt = mPrefs.getString(PREF_EVERNOTE_CRYPTED, "");
         if (mEvernotePassword.length() > 0) {
             try {
             crypt = SimpleCrypt.encrypt(SEED, mEvernotePassword);
             mPrefs.edit().putString(PREF_EVERNOTE_CRYPTED, crypt).commit();
             mPrefs.edit().remove(PREF_EVERNOTE_PASSWORD).commit();
             Log.d(LOG_TAG, "plain text password has been migrate to crypted");
             } catch (Exception e) {
                 Log.d(LOG_TAG, "Failed to encrypt plain password: " + mEvernotePassword);
             }
         }
         if (crypt.length() > 0) {
             try {
                 mEvernotePassword = SimpleCrypt.decrypt(SEED, crypt);
             } catch (Exception e) {
                 Log.d(LOG_TAG, "Failed to decrypt password: " + crypt);
             }
         }
         if (mEvernoteUsername.length() == 0 || mEvernotePassword.length() == 0) {
             AlertDialog.Builder builder = new AlertDialog.Builder(TwiccaEvernoteUploader.this);
             builder.setTitle(R.string.settings_name);
             builder.setMessage(getString(R.string.account_warning));
             builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     Intent settings = new Intent(TwiccaEvernoteUploader.this, TwiccaPluginSettings.class);
                     TwiccaEvernoteUploader.this.startActivityForResult(settings, REQUEST_CODE);
                 }
             }
            );
 
             builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     TwiccaEvernoteUploader.this.finish();
                 }
             }
             );
             builder.create().show();
             return;
         }
 
         if (mPrefs.getBoolean(PREF_CONFIRM_DIALOG, true)) {
             ArrayList<String> notebooks = cacheManager.getNotebookNames();
             final int lastItem = notebooks.size();
             String[] tags = cacheManager.getTagNames();
             LayoutInflater inflater = getLayoutInflater();
             View layout = inflater.inflate(R.layout.alert_dialog,
                                            (ViewGroup) findViewById(R.id.layout_root));
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setTitle(R.string.confirm_clip);
 
             mEditNotebook = (EditText)layout.findViewById(R.id.edit_notebook);
             mEditNotebook.setHint(getString(R.string.hint_empty));
 
             mSpinner = (Spinner)layout.findViewById(R.id.notebook_list);
             ArrayAdapter<String> items = new ArrayAdapter<String>(this,
                     R.layout.dropdown_list_item, notebooks);
             int i = 0;
             if (mEvernoteNotebook.length() > 0) {
                 ListIterator<String> itr = notebooks.listIterator();
                 while (itr.hasNext()) {
                     String name = itr.next();
                     if (mEvernoteNotebook.equalsIgnoreCase(name))
                         break;
                     i++;
                 }
             }
             items.add(getString(R.string.new_or_default));
             mSpinner.setAdapter(items);
             mSpinner.setSelection(i);
             if (i == lastItem) {
                 mEditNotebook.setEnabled(true);
                 if (mEvernoteNotebook.length() > 0) {
                     mEditNotebook.setText(mEvernoteNotebook);
                 }
             } else {
                 mEditNotebook.setEnabled(false);
             }
             mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                 @Override
                 public void onItemSelected(AdapterView<?> arg0, View arg1,
                         int arg2, long arg3) {
                     // TODO Auto-generated method stub
                     if (arg2 == lastItem) {
                         mEditNotebook.setEnabled(true);
                         mEditNotebook.requestFocus();
                     } else {
                         mEditNotebook.setEnabled(false);
                     }
                 }
 
                 @Override
                 public void onNothingSelected(AdapterView<?> arg0) {
                     // TODO Auto-generated method stub
                     mEditNotebook.setEnabled(true);
                     mEditNotebook.requestFocus();
                 }
             });
 
             mEditTags = (MultiAutoCompleteTextView)layout.
                 findViewById(R.id.edit_tags);
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                     R.layout.list_item, tags);
             mEditTags.setAdapter(adapter);
             mEditTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
             mEditTags.setText(mEvernoteTags);
             builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     long index = mSpinner.getSelectedItemId();
                     if (index != (long)lastItem) {
                         mEvernoteNotebook = (String) mSpinner.getSelectedItem();
                         Log.d(LOG_TAG, "Selected: " + mEvernoteNotebook);
                     } else {
                         mEvernoteNotebook = mEditNotebook.getText().toString();
                     }
                     mEvernoteTags = mEditTags.getText().toString();
                     dialog.dismiss();
                     requestUpload();
                     finish();
                 }
             }
             );
             builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialog, int which) {
                     TwiccaEvernoteUploader.this.finish();
                 }
             }
             );
             builder.setView(layout);
             builder.create().show();
         } else {
             requestUpload();
             finish();
         }
     }
 
     /* utility */
     public static String join(AbstractCollection<String> s, String delimiter) {
         if (s.isEmpty()) return "";
         Iterator<String> iter = s.iterator();
         StringBuffer buffer = new StringBuffer(iter.next());
         while (iter.hasNext()) buffer.append(delimiter).append(iter.next());
         return buffer.toString();
     }
 
     public String autoLinkHashtags(String text) {
         StringBuffer sb = new StringBuffer();
         ArrayList<String> tags = new ArrayList<String>();
         if (mEvernoteTags.length() > 0) tags.add(mEvernoteTags);
         Matcher matcher = HASHTAG_PATTERN.matcher(text);
         while (matcher.find()) {
             String after = text.substring(matcher.end());
             if (!HASHTAG_END.matcher(after).find()) {
                 StringBuilder replacement = new StringBuilder(text.length() * 2);
                 replacement.append(matcher.group(1))
                     .append("<a href=\"").append(SEARCH_URL)
                     .append(matcher.group(3)).append("\"")
                     .append(" target=\"_blank\" title=\"#").append(matcher.group(3))
                     .append("\"");
                 replacement.append(">").append(matcher.group(2))
                     .append(matcher.group(3)).append("</a>");
                 matcher.appendReplacement(sb, replacement.toString());
                 if (!tags.contains(matcher.group(3))) {
                     tags.add(matcher.group(3));
                 } // if
             } else {
                 // not a valid hashtag
                 matcher.appendReplacement(sb, "$0");
             } // if valid?
         } // while
         matcher.appendTail(sb);
         if (mPrefs.getBoolean(PREF_HASHTAG_CLIPTAG, false)) {
             mEvernoteTags = join(tags, ",");
         } // if
         return sb.toString();
     } // func()
 }
