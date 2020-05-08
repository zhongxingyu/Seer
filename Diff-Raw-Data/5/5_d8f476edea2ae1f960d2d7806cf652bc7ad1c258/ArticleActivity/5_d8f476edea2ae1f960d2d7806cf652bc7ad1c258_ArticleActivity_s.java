 package com.novel.reader;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.adwhirl.AdWhirlLayout;
 import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
 import com.adwhirl.AdWhirlManager;
 import com.adwhirl.AdWhirlTargeting;
 import com.google.ads.AdView;
 import com.kosbrother.mail.GMailSender;
 import com.kosbrother.tool.DetectScrollView;
 import com.kosbrother.tool.DetectScrollView.DetectScrollViewListener;
 import com.novel.reader.api.NovelAPI;
 import com.novel.reader.api.Setting;
 import com.novel.reader.entity.Article;
 import com.novel.reader.entity.Bookmark;
 
 public class ArticleActivity extends SherlockFragmentActivity implements DetectScrollViewListener, AdWhirlInterface {
 
     private static final int    ID_SETTING  = 0;
     private static final int    ID_RESPONSE = 1;
     private static final int    ID_ABOUT_US = 2;
     private static final int    ID_GRADE    = 3;
     private static final int    ID_Bookmark = 4;
     private static final int    ID_Report   = 5;
 
     private int                 textSize;
     private int                 textLanguage;                                    // 0 for 繁體, 1 for 簡體
     private int                 readingDirection;                                // 0 for 直向, 1 for 橫向
     private int                 clickToNextPage;                                 // 0 for yes, 1 for no
     private int                 stopSleeping;                                    // 0 for yes, 1 for no
     private int                 textColor;
     private int                 textBackground;
 
     private TextView            articleTextView;
     private DetectScrollView    articleScrollView;
     private Button              articleButtonUp;
     private Button              articleButtonDown;
     private TextView            articlePercent;
     private Article             myAricle;                                        // uset to get article text
     private Article             theGottenArticle;
     private Boolean             downloadBoolean;
     private AlertDialog.Builder addBookMarkDialog;
     private Bundle              mBundle;
     private String              novelName;
     private String              articleTitle;
     private int                 articleId;
     private String              novelPic;
     private int                 novelId;
     private int                 yRate;
     private int                 ariticlePosition;
     private ArrayList<Integer>  articleIDs;
     // private ProgressDialog progressDialog= null;
     private AlertDialog.Builder aboutUsDialog;
     private final String        adWhirlKey  = "215f895eb71748e7ba4cb3a5f20b061e";
     private ActionBar           ab;
     private LinearLayout        layoutProgress;
     private int                 currentY    = 0;
     private int                 appTheme;
     private Boolean             booleanSend;
     private String              reportContent;
     private int                 articleNum;
     private ArrayList<Integer>  articleNums;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Setting.setApplicationActionBarTheme(this);
         setContentView(R.layout.layout_article);
         restorePreValues();
         setViews();
 
         ab = getSupportActionBar();
         mBundle = this.getIntent().getExtras();
         novelName = mBundle.getString("NovelName");
         articleTitle = mBundle.getString("ArticleTitle");
         articleId = mBundle.getInt("ArticleId");
         downloadBoolean = mBundle.getBoolean("ArticleDownloadBoolean", false);
         novelPic = mBundle.getString("NovelPic");
         novelId = mBundle.getInt("NovelId");
         yRate = mBundle.getInt("ReadingRate", 0);
         articleIDs = mBundle.getIntegerArrayList("ArticleIDs");
         ariticlePosition = mBundle.getInt("ArticlePosition");
         articleNum = mBundle.getInt("ArticleNum");
         articleNums = mBundle.getIntegerArrayList("ArticleNums");
 
         ab.setDisplayShowCustomEnabled(true);
         ab.setDisplayShowTitleEnabled(false);
 
         setActionBarTitle(articleTitle);
 
         // ab.setTitle(novelName);
         ab.setDisplayHomeAsUpEnabled(true);
 
         if (articleIDs != null) {
             if (downloadBoolean) {
                 myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
             } else {
                 myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
             }
         } else {
             if (downloadBoolean) {
                 myAricle = new Article(articleId, novelId, "", articleTitle, "", true, articleNum);
             } else {
                 myAricle = new Article(articleId, novelId, "", articleTitle, "", false, articleNum);
             }
         }
 
         new DownloadArticleTask().execute();
 
         setAboutUsDialog();
 
         try {
             Display display = getWindowManager().getDefaultDisplay();
             int width = display.getWidth(); // deprecated
             int height = display.getHeight(); // deprecated
 
             if (width > 320) {
                 setAdAdwhirl();
             }
         } catch (Exception e) {
 
         }
 
     }
 
     private void setActionBarTitle(String articleTitle) {
         LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View v = inflator.inflate(R.layout.item_title, null);
         TextView titleText = ((TextView) v.findViewById(R.id.title));
         titleText.setText(novelName + ":" + articleTitle);
         titleText.setSelected(true);
         int theme = Setting.getSetting(Setting.keyAppTheme, this);
         if (theme != Setting.initialAppTheme) {
             titleText.setTextColor(getResources().getColor(R.color.white));
             RelativeLayout bottom_buttons = (RelativeLayout) findViewById(R.id.bottom_buttons);
             bottom_buttons.setBackgroundColor(getResources().getColor(R.color.light_black));
             articlePercent.setTextColor(getResources().getColor(R.color.white));
             LinearLayout layout = (LinearLayout) findViewById(R.id.adonView);
             layout.setBackgroundColor(getResources().getColor(R.color.black));
         }
         ab.setCustomView(v);
     }
 
     private void restorePreValues() {
         textSize = Setting.getSetting(Setting.keyTextSize, ArticleActivity.this);
         textColor = Setting.getSetting(Setting.keyTextColor, ArticleActivity.this);
         textBackground = Setting.getSetting(Setting.keyTextBackground, ArticleActivity.this);
         textLanguage = Setting.getSetting(Setting.keyTextLanguage, ArticleActivity.this);
         readingDirection = Setting.getSetting(Setting.keyReadingDirection, ArticleActivity.this);
         clickToNextPage = Setting.getSetting(Setting.keyClickToNextPage, ArticleActivity.this);
         stopSleeping = Setting.getSetting(Setting.keyStopSleeping, ArticleActivity.this);
 
         if (readingDirection == 0) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         } else if (readingDirection == 1) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
 
         if (stopSleeping == 0) {
             ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
         }
     }
 
     private void setViews() {
 
         layoutProgress = (LinearLayout) findViewById(R.id.layout_progress);
         articleTextView = (TextView) findViewById(R.id.article_text);
         articleScrollView = (DetectScrollView) findViewById(R.id.article_scrollview);
         articleButtonUp = (Button) findViewById(R.id.article_button_up);
         articleButtonDown = (Button) findViewById(R.id.article_button_down);
         articlePercent = (TextView) findViewById(R.id.article_percent);
 
         articleScrollView.setScrollViewListener(ArticleActivity.this);
 
         articleTextView.setTextSize(textSize);
         articleTextView.setTextColor(textColor);
         articleScrollView.setBackgroundColor(textBackground);
 
         articleButtonUp.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
 
                 if (articleIDs != null) {
                     if (ariticlePosition != 0) {
                         ariticlePosition = ariticlePosition - 1;
                         if (downloadBoolean) {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                         } else {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                         }
                         new UpdateArticleTask().execute();
                     } else {
                         Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
                     }
                 } else {
                     new GetPreviousArticleTask().execute();
                 }
             }
         });
 
         articleButtonDown.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 if (articleIDs != null) {
                     if (ariticlePosition < articleIDs.size() - 1) {
                         ariticlePosition = ariticlePosition + 1;
                         if (downloadBoolean) {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", true, articleNums.get(ariticlePosition));
                         } else {
                             myAricle = new Article(articleIDs.get(ariticlePosition), novelId, "", articleTitle, "", false, articleNums.get(ariticlePosition));
                         }
                         new UpdateArticleTask().execute();
                     } else {
                         Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
                     }
                 } else {
                     new GetNextArticleTask().execute();
                 }
 
             }
         });
 
         if (clickToNextPage == 0) {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // new GetNextArticleTask().execute();
                     int kk = articleScrollView.getHeight();
                     int tt = articleTextView.getHeight();
                     articleScrollView.scrollTo(0, currentY + kk - 8);
                 }
             });
         }
 
         addBookMarkDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.add_my_bookmark_title))
                 .setMessage(getResources().getString(R.string.add_my_bookmark_content)).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         NovelAPI.insertBookmark(
                                 new Bookmark(0, myAricle.getNovelId(), myAricle.getId(), yRate, novelName, myAricle.getTitle(), novelPic, false),
                                 ArticleActivity.this);
                     }
                 }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         dialog.cancel();
                     }
                 });
 
     }
 
     @Override
     public void onScrollChanged(DetectScrollView scrollView, int x, int y, int oldx, int oldy) {
         int kk = articleScrollView.getHeight();
         int tt = articleTextView.getHeight();
 
         currentY = y;
         yRate = (int) (((double) (y) / (double) (tt)) * 100);
         int xx = (int) (((double) (y + kk) / (double) (tt)) * 100);
         if (xx > 100)
             xx = 100;
         String yPositon = Integer.toString(xx);
         articlePercent.setText(yPositon + "%");
     }
 
     public static int dip2px(Context context, float dpValue) {
         final float scale = context.getResources().getDisplayMetrics().density;
         return (int) (dpValue * scale + 0.5f);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         // getMenuInflater().inflate(R.menu.activity_main, menu);
 
         menu.add(0, ID_SETTING, 0, getResources().getString(R.string.menu_settings)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_RESPONSE, 1, getResources().getString(R.string.menu_respond)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_ABOUT_US, 2, getResources().getString(R.string.menu_aboutus)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_GRADE, 3, getResources().getString(R.string.menu_recommend)).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(0, ID_Bookmark, 5, getResources().getString(R.string.menu_add_bookmark)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(0, ID_Report, 4, getResources().getString(R.string.menu_report)).setIcon(R.drawable.icon_report)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
         int itemId = item.getItemId();
         switch (itemId) {
         case android.R.id.home:
             finish();
             // Toast.makeText(this, "home pressed", Toast.LENGTH_LONG).show();
             break;
         case ID_SETTING: // setting
             Intent intent = new Intent(ArticleActivity.this, SettingActivity.class);
             startActivity(intent);
             break;
         case ID_RESPONSE: // response
             final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
             emailIntent.setType("plain/text");
             emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { getResources().getString(R.string.respond_mail_address) });
             emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.respond_mail_title));
             emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
             startActivity(Intent.createChooser(emailIntent, "Send mail..."));
             break;
         case ID_ABOUT_US:
             aboutUsDialog.show();
             break;
         case ID_GRADE:
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.recommend_url)));
             startActivity(browserIntent);
             break;
         case ID_Bookmark:
             addBookMarkDialog.show();
             break;
         case ID_Report:
             showReportDialog();
             break;
         }
         return true;
     }
 
     private void showReportDialog() {
         AlertDialog.Builder editDialog = new AlertDialog.Builder(ArticleActivity.this);
         editDialog.setTitle(getResources().getString(R.string.report_title));
 
         // final EditText editText = new EditText(ArticleActivity.this);
         // editDialog.setView(editText);
 
         LayoutInflater inflater = LayoutInflater.from(ArticleActivity.this);
         View edit_view = inflater.inflate(R.layout.layout_mail_dialog, null);
         TextView text_chapter = (TextView) edit_view.findViewById(R.id.text_report_chapter);
         final EditText text_content = (EditText) edit_view.findViewById(R.id.text_report_content);
 
        text_chapter.setText(getResources().getString(R.string.report_chapter) + articleTitle);
 
         editDialog.setView(edit_view);
 
         editDialog.setPositiveButton(getResources().getString(R.string.report_confirm), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
                 if (!text_content.getText().toString().equals("")) {
                     reportContent = text_content.getText().toString();
                     new SendMailTask().execute();
                 } else {
                     Toast.makeText(ArticleActivity.this, getResources().getString(R.string.report_remind), Toast.LENGTH_SHORT).show();
                 }
             }
         });
         editDialog.setNegativeButton(getResources().getString(R.string.report_cancel), new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface arg0, int arg1) {
             }
         });
         editDialog.show();
     }
 
     private class SendMailTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             Toast.makeText(ArticleActivity.this, getResources().getString(R.string.report_reporting), Toast.LENGTH_SHORT).show();
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             try {
                 GMailSender sender = new GMailSender("sandjstudio@gmail.com", "wonderful2013");
                booleanSend = sender.sendMail(novelName + "---" + getResources().getString(R.string.report_chapter) + articleTitle, reportContent,
                         "sandjstudio@gmail.com", "brotherkos@gmail.com");
             } catch (Exception e) {
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             if (booleanSend) {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.report_success), Toast.LENGTH_SHORT).show();
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.report_fail), Toast.LENGTH_SHORT).show();
             }
         }
     }
 
     private class DownloadArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
 
             layoutProgress.setVisibility(View.GONE);
             if (textLanguage == 1) {
                 String text = "";
                 try {
                     text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getText());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 articleTextView.setText(text);
             } else {
                 // String text2 = "";
                 // try {
                 // text2 = taobe.tec.jcc.JChineseConvertor.getInstance().s2t(myAricle.getText());
                 // } catch (IOException e) {
                 // e.printStackTrace();
                 // }
                 // articleTextView.setText(text2);
                 articleTextView.setText(myAricle.getText());
             }
 
             myAricle.setNovelId(novelId);
 
             new GetLastPositionTask().execute();
 
         }
     }
 
     private class UpdateArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
                 if (textLanguage == 1) {
                     String text = "";
                     try {
                         text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getText());
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     articleTextView.setText(text);
                 } else {
                     articleTextView.setText(myAricle.getText());
                 }
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.fullScroll(ScrollView.FOCUS_UP);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
 
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_data), Toast.LENGTH_SHORT).show();
             }
 
             new GetLastPositionTask().execute();
 
         }
     }
 
     private class GetPreviousArticleTask extends AsyncTask {
 
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getPreviousArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
                 if (textLanguage == 1) {
                     String text = "";
                     try {
                         text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getText());
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     articleTextView.setText(text);
                 } else {
                     articleTextView.setText(myAricle.getText());
                 }
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.fullScroll(ScrollView.FOCUS_UP);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
 
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_up), Toast.LENGTH_SHORT).show();
             }
 
             new GetLastPositionTask().execute();
 
         }
     }
 
     private class GetNextArticleTask extends AsyncTask {
         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             layoutProgress.setVisibility(View.VISIBLE);
         }
 
         @Override
         protected Object doInBackground(Object... params) {
             if (myAricle != null) {
                 theGottenArticle = NovelAPI.getNextArticle(myAricle, ArticleActivity.this);
                 if (theGottenArticle != null) {
                     myAricle = theGottenArticle;
                 }
             }
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
             super.onPostExecute(result);
             layoutProgress.setVisibility(View.GONE);
             if (theGottenArticle != null) {
                 if (textLanguage == 1) {
                     String text = "";
                     try {
                         text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getText());
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     articleTextView.setText(text);
                 } else {
                     articleTextView.setText(myAricle.getText());
                 }
 
                 myAricle.setNovelId(novelId);
                 articleScrollView.fullScroll(ScrollView.FOCUS_UP);
                 setActionBarTitle(myAricle.getTitle());
                 articlePercent.setText("0%");
             } else {
                 Toast.makeText(ArticleActivity.this, getResources().getString(R.string.article_no_down), Toast.LENGTH_SHORT).show();
             }
 
             new GetLastPositionTask().execute();
 
         }
     }
 
     private class GetLastPositionTask extends AsyncTask {
 
         @Override
         protected Object doInBackground(Object... params) {
 
             return null;
         }
 
         @Override
         protected void onPostExecute(Object result) {
 
             super.onPostExecute(result);
 
             int kk = articleScrollView.getHeight();
             int tt = articleTextView.getHeight();
 
             if (yRate == 0) {
                 int xx = (int) (((double) (kk) / (double) (tt)) * 100);
                 if (xx > 100)
                     xx = 100;
                 String yPositon = Integer.toString(xx);
                 articlePercent.setText(yPositon + "%");
                 articleScrollView.fullScroll(ScrollView.FOCUS_UP);
             } else {
                 String yPositon = Integer.toString(yRate);
                 articlePercent.setText(yPositon + "%");
                 currentY = yRate * tt / 100;
                 articleScrollView.scrollTo(0, currentY);
             }
 
         }
     }
 
     private void setAboutUsDialog() {
 
         aboutUsDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.about_us_string)).setIcon(R.drawable.play_store_icon)
                 .setMessage(getResources().getString(R.string.about_us))
                 .setPositiveButton(getResources().getString(R.string.yes_string), new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                     }
                 });
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         int originTextLan = textLanguage;
 
         textSize = Setting.getSetting(Setting.keyTextSize, ArticleActivity.this);
         textColor = Setting.getSetting(Setting.keyTextColor, ArticleActivity.this);
         textBackground = Setting.getSetting(Setting.keyTextBackground, ArticleActivity.this);
         textLanguage = Setting.getSetting(Setting.keyTextLanguage, ArticleActivity.this);
         readingDirection = Setting.getSetting(Setting.keyReadingDirection, ArticleActivity.this);
         clickToNextPage = Setting.getSetting(Setting.keyClickToNextPage, ArticleActivity.this);
         stopSleeping = Setting.getSetting(Setting.keyStopSleeping, ArticleActivity.this);
         // appTheme = Setting.getSetting(Setting.keyAppTheme, ArticleActivity.this);
 
         articleTextView.setTextSize(textSize);
         articleTextView.setTextColor(textColor);
         articleScrollView.setBackgroundColor(textBackground);
 
         if (originTextLan != textLanguage) {
             if (textLanguage == 1) {
                 String text = "";
                 try {
                     text = taobe.tec.jcc.JChineseConvertor.getInstance().t2s(myAricle.getTitle() + "\n\n" + myAricle.getText());
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 articleTextView.setText(text);
             } else {
                 articleTextView.setText(articleTitle + "\n\n" + myAricle.getText());
             }
         }
 
         if (readingDirection == 0) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
         } else if (readingDirection == 1) {
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         }
 
         if (clickToNextPage == 0) {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // new GetNextArticleTask().execute();
                     int kk = articleScrollView.getHeight();
                     int tt = articleTextView.getHeight();
                     articleScrollView.scrollTo(0, currentY + kk - 8);
                 }
             });
         } else {
             articleTextView.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View arg0) {
                     // do nothing
                 }
             });
         }
 
         if (stopSleeping == 0) {
             ArticleActivity.this.findViewById(android.R.id.content).setKeepScreenOn(true);
         }
 
     }
 
     @Override
     protected void onPause() {
         NovelAPI.createRecentBookmark(new Bookmark(0, myAricle.getNovelId(), myAricle.getId(), yRate, novelName, myAricle.getTitle(), novelPic, true),
                 ArticleActivity.this);
         super.onPause();
     }
 
     private void setAdAdwhirl() {
 
         AdWhirlManager.setConfigExpireTimeout(1000 * 60);
         AdWhirlTargeting.setAge(23);
         AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
         AdWhirlTargeting.setKeywords("online games gaming");
         AdWhirlTargeting.setPostalCode("94123");
         AdWhirlTargeting.setTestMode(false);
 
         AdWhirlLayout adwhirlLayout = new AdWhirlLayout(this, adWhirlKey);
 
         LinearLayout mainLayout = (LinearLayout) findViewById(R.id.adonView);
 
         adwhirlLayout.setAdWhirlInterface(this);
 
         mainLayout.addView(adwhirlLayout);
 
         mainLayout.invalidate();
     }
 
     @Override
     public void adWhirlGeneric() {
 
     }
 
     public void rotationHoriztion(int beganDegree, int endDegree, AdView view) {
         final float centerX = 320 / 2.0f;
         final float centerY = 48 / 2.0f;
         final float zDepth = -0.50f * view.getHeight();
 
         Rotate3dAnimation rotation = new Rotate3dAnimation(beganDegree, endDegree, centerX, centerY, zDepth, true);
         rotation.setDuration(1000);
         rotation.setInterpolator(new AccelerateInterpolator());
         rotation.setAnimationListener(new Animation.AnimationListener() {
             public void onAnimationStart(Animation animation) {
             }
 
             public void onAnimationEnd(Animation animation) {
             }
 
             public void onAnimationRepeat(Animation animation) {
             }
         });
         view.startAnimation(rotation);
     }
 
 }
