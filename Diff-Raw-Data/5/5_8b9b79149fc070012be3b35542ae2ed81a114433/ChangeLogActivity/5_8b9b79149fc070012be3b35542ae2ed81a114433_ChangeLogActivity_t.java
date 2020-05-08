 
 package se.puzzlingbytes.changeloghelper;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.webkit.WebView;
 
 import se.puzzlingbytes.changeloghelper.data.ChangeLog;
 import se.puzzlingbytes.changeloghelper.parser.ChangeLogParser;
 
 public class ChangeLogActivity extends Activity {
 
     /**
      * Changelog XML resource id.</br> 
      * Constant value: {@value #EXTRA_CHANGELOG_XML_RESID}
      */
     public static final String EXTRA_CHANGELOG_XML_RESID = "extra_changelog_xml_resid";
 
     /**
      * Boolean to indicate if the date should be shown.</br> 
      * Constant value: {@value #EXTRA_CHANGELOG_SHOW_DATE}
      */
     public static final String EXTRA_CHANGELOG_SHOW_DATE = "extra_changelog_show_date";
 
     /**
      * Boolean to indicate if the current version mark should be shown.</br>
      * Constant value: {@value #EXTRA_CHANGELOG_SHOW_CURRENT}
      */
     public static final String EXTRA_CHANGELOG_SHOW_CURRENT = "extra_changelog_show_current";
 
     /**
      * Custom CSS style string.</br> 
      * Constant value: {@value #EXTRA_CHANGELOG_CSS_STR}
      */
     public static final String EXTRA_CHANGELOG_CSS_STR = "extra_changelog_css_str";
 
     /**
      * String resource id to a custom CSS style string.</br> 
      * Constant value: {@value #EXTRA_CHANGELOG_CSS_RESID}
      */
     public static final String EXTRA_CHANGELOG_CSS_RESID = "extra_changelog_css_resid";
 
     private static final String MIME_TYPE_TEXT_HTML = "text/html";
 
     private static final String ENCODING_UTF8 = "utf-8";
 
     private int mChangeLogResID = -1;
 
     private boolean mShowDate;
 
     private boolean mShowCurrent;
 
     private String mStyle;
 
     private WebView mWebView;
 
     public static Intent getChangeLogIntent(Context context,
             int changeLogXMLResID, boolean showDate, boolean showCurrent) {
         return getChangeLogIntent(context, changeLogXMLResID, showDate,
                 showCurrent, null, -1);
     }
 
     public static Intent getChangeLogIntent(Context context,
             int changeLogXMLResID, boolean showDate, boolean showCurrent,
             int cssStyleResID) {
         return getChangeLogIntent(context, changeLogXMLResID, showDate,
                 showCurrent, null, cssStyleResID);
     }
 
     public static Intent getChangeLogIntent(Context context,
             int changeLogXMLResID, boolean showDate, boolean showCurrent,
             String cssStyle) {
         return getChangeLogIntent(context, changeLogXMLResID, showDate,
                 showCurrent, cssStyle, -1);
     }
 
     public static Intent getChangeLogIntent(Context context,
             int changeLogXMLResID, boolean showDate, boolean showCurrent,
             String cssStyle, int cssStyleResID) {
         Intent changeLogintent = new Intent(context, ChangeLogActivity.class);
         changeLogintent.putExtra(EXTRA_CHANGELOG_XML_RESID, changeLogXMLResID);
         changeLogintent.putExtra(EXTRA_CHANGELOG_SHOW_DATE, showDate);
         changeLogintent.putExtra(EXTRA_CHANGELOG_SHOW_CURRENT, showCurrent);
         if (!TextUtils.isEmpty(cssStyle)) {
             changeLogintent.putExtra(EXTRA_CHANGELOG_CSS_STR, cssStyle);
         } else if (cssStyleResID > 0) {
            changeLogintent.putExtra(EXTRA_CHANGELOG_CSS_RESID, cssStyleResID);
         }
         return changeLogintent;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.act_changelog);
         Intent intent = getIntent();
         if (intent.getExtras() != null) {
             mChangeLogResID = intent.getIntExtra(EXTRA_CHANGELOG_XML_RESID, -1);
             mShowDate = intent
                     .getBooleanExtra(EXTRA_CHANGELOG_SHOW_DATE, false);
             mShowCurrent = intent.getBooleanExtra(EXTRA_CHANGELOG_SHOW_CURRENT,
                     false);
             mStyle = intent.getStringExtra(EXTRA_CHANGELOG_CSS_STR);
            int styleResID = intent.getIntExtra(EXTRA_CHANGELOG_CSS_RESID, -1);
             if (TextUtils.isEmpty(mStyle) && styleResID > 0) {
                 mStyle = getString(styleResID);
             }
         }
         mWebView = (WebView) findViewById(R.id.changelog_webview);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         String webViewData = null;
         if (mChangeLogResID > -1) {
             ChangeLogParser changeLogParser = new ChangeLogParser(this);
             ChangeLog changeLog = changeLogParser.parse(mChangeLogResID);
             webViewData = changeLog.generateChangeLogHTML(mShowDate,
                     mShowCurrent, mStyle);
         } else {
             webViewData = getString(R.string.changelog_missing);
         }
         mWebView.loadData(webViewData, MIME_TYPE_TEXT_HTML, ENCODING_UTF8);
     }
 }
