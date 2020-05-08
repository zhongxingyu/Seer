 package com.cghio.easyjobs;
 
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.Toast;
 
 import com.loopj.android.http.AsyncHttpClient;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.loopj.android.http.RequestParams;
 
 import org.apache.http.Header;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Jobs extends EasyJobsBase {
 
     private static String PREF_FILE = "auth_info";
     private static String PREF_VERSION = "VERSION";
     private static String PREF_URL = "URL";
     private static String PREF_CONTENT = "CONTENT";
 
     private static String API_HELP_URL = "";
     private static String API_TOKEN = "";
 
     private static String JOBS_INDEX_VERB = "";
     private static String JOBS_INDEX_URL = "";
 
     private static String JOBS_SHOW_URL = "";
     private static String JOBS_RUN_URL = "";
     private static String JOBS_PARAMETERS_INDEX_URL = "";
     private static String TOKEN_LOGIN_URL = "";
     private static String REVOKE_TOKEN_URL = "";
 
     private static boolean launched = false;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_jobs);
 
         startEasyJobs();
 
         if (!launched) { // first time launch from link
             onNewIntent(getIntent());
         }
         launched = true;
     }
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         String fromURI = null;
         if (intent != null) {
             Uri data = intent.getData();
             if (data != null) {
                 fromURI = data.getSchemeSpecificPart();
             }
         }
 
         if (fromURI != null) {
             decode(fromURI.substring(2));
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.reload_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.reload:
                 removeEtagContent(JOBS_INDEX_URL);
                 startEasyJobs();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void startEasyJobs() {
         if (readPrefs()) {
             getHelp();
         } else {
             showScanButton();
         }
     }
 
     private boolean readPrefs() {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         int VERSION = sharedPrefs.getInt(PREF_VERSION, 0);
         String URL = sharedPrefs.getString(PREF_URL, "");
         String CONTENT = sharedPrefs.getString(PREF_CONTENT, "");
 
         // validate version number
         if (VERSION <= 0) return false;
         if (VERSION > MAX_API_VERSION) return false;
 
         // validate help URL
         try {
             java.net.URL url = new URL(URL);
             url.toURI();
         } catch (Exception e) {
             return false;
         }
 
         if (CONTENT.length() == 0) return false;
 
         API_HELP_URL = URL;
         API_TOKEN = CONTENT;
 
         return true;
     }
 
     private void getHelp() {
         if (API_TOKEN.length() == 0) return;
         RequestParams params = new RequestParams();
         params.put("token", API_TOKEN);
         AsyncHttpClient client = new AsyncHttpClient();
         client.setTimeout(TIMEOUT);
         showLoading();
         client.get(API_HELP_URL, params, new AsyncHttpResponseHandler() {
             @Override
             public void onFailure(Throwable e, String response) {
                 hideLoading();
                 String error;
                 if (e != null && e.getCause() != null) {
                     error = e.getCause().getMessage();
                 } else if (e != null && e.getCause() == null) {
                     error = e.getMessage();
                 } else {
                     error = getString(R.string.error_connection_problem);
                 }
                 if (error.matches(".*[Uu]nauthorized.*")) {
                     error += "\n\n" + getString(R.string.error_need_refresh);
                 }
                 showSimpleErrorDialog(error);
                 showReloadAndScanButton();
             }
             @Override
             public void onSuccess(String response) {
                 updateTitle();
                 hideLoading();
                 try {
                     JSONObject helpObj = new JSONObject(response);
                     JSONObject jobsObj = helpObj.getJSONObject("jobs");
                     JSONObject jobsIndexObj = jobsObj.getJSONObject("index");
                     JOBS_INDEX_VERB = jobsIndexObj.getString("verb");
                     JOBS_INDEX_URL = jobsIndexObj.getString("url");
 
                     JSONObject jobsShowObj = jobsObj.getJSONObject("show");
                     JOBS_SHOW_URL = jobsShowObj.getString("url");
 
                     JSONObject jobsRunObj = jobsObj.getJSONObject("run");
                     JOBS_RUN_URL = jobsRunObj.getString("url");
 
                     JSONObject jobsParamsObj = helpObj.getJSONObject("job_parameters");
                     JSONObject jobsParamsIndexObj = jobsParamsObj.getJSONObject("index");
                     JOBS_PARAMETERS_INDEX_URL = jobsParamsIndexObj.getString("url");
 
                     JSONObject tokensObj = helpObj.getJSONObject("tokens");
                     JSONObject tokensRevokeObj = tokensObj.getJSONObject("revoke");
                     REVOKE_TOKEN_URL = tokensRevokeObj.getString("url");
 
                     JSONObject tokensLoginObj = tokensObj.getJSONObject("login");
                     TOKEN_LOGIN_URL = tokensLoginObj.getString("url");
 
                     getJobs();
                 } catch (JSONException e) {
                     showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
                     showReloadAndScanButton();
                 }
             }
         });
     }
 
     private void getJobs() {
         if (JOBS_INDEX_VERB.length() == 0 || JOBS_INDEX_URL.length() == 0) return;
 
         String cachedContent = getEtagContent(JOBS_INDEX_URL);
         if (cachedContent.length() > 0) {
             parseContent(cachedContent);
         }
 
         AsyncHttpClient client = new AsyncHttpClient();
         RequestParams params = new RequestParams();
         params.put("token", API_TOKEN);
         client.setTimeout(TIMEOUT);
         showLoading();
 
         client.addHeader(IF_NONE_MATCH, getEtag(JOBS_INDEX_URL));
 
         client.get(JOBS_INDEX_URL, params, new AsyncHttpResponseHandler() {
             @Override
             public void onFailure(Throwable e, String response) {
                 hideLoading();
                 if (isNotModified(e)) return;
                 if (e != null && e.getCause() != null) {
                     showSimpleErrorDialog(e.getCause().getMessage());
                 } else if (e != null && e.getCause() == null) {
                     showSimpleErrorDialog(e.getMessage());
                 } else {
                     showSimpleErrorDialog(getString(R.string.error_connection_problem));
                 }
                 showReloadAndScanButton();
             }
             @Override
             public void onSuccess(int statusCode, Header[] headers, String content) {
                 hideLoading();
                 String etag = getHeader(headers, ETAG);
                 saveETagAndContent(JOBS_INDEX_URL, etag, content);
                 parseContent(content);
             }
         });
     }
 
     private void parseContent(String content) {
         try {
             List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
 
             JSONArray jobs = new JSONArray(content);
             for (int i = 0; i < jobs.length(); i++) {
                 JSONObject object = jobs.getJSONObject(i);
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("ID", object.getInt("id"));
                 map.put("KEY", object.getString("name"));
                 String server = object.getString("server_name");
                 if (server.equals("null")) server = getString(R.string.no_server);
                 map.put("VALUE", server);
                 int type_id = 0;
                 if (!object.isNull("type_id")) type_id = object.getInt("type_id");
                 map.put("TYPE_ID", type_id);
                 String type_name = object.getString("type_name");
                 if (type_name.equals("null")) type_name = getString(R.string.orphans);
                 map.put("TYPE_NAME", type_name);
                 data.add(map);
             }
 
             Collections.sort(data, new Comparator<Map<String, Object>>() {
                 @Override
                 public int compare(Map<String, Object> obj1, Map<String, Object> obj2) {
                     return Integer.parseInt(obj1.get("TYPE_ID").toString()) -
                             Integer.parseInt(obj2.get("TYPE_ID").toString());
                 }
             });
 
             String last_type_name = null;
             for (int i = 0; i < data.size(); i++) {
                 Map<String, Object> object = data.get(i);
                 String type_name = object.get("TYPE_NAME").toString();
                 if (type_name.equals(last_type_name)) continue;
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", type_name);
                 data.add(i, map);
                 last_type_name = type_name;
             }
 
             {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", getString(R.string.actions));
                 data.add(map);
             }
 
             int[] other_buttons_text = {R.string.browse_web_page, R.string.revoke_access};
             int[] other_buttons_desc = {R.string.browse_web_page_desc, R.string.revoke_access_desc};
 
             for (int i = 0; i < other_buttons_text.length; i++) {
                 Map<String, Object> map = new HashMap<String, Object>();
                 map.put("KEY", getString(other_buttons_text[i]));
                 map.put("VALUE", getString(other_buttons_desc[i]));
                 data.add(map);
             }
 
             EasyJobsAdapter adapter = new EasyJobsAdapter(Jobs.this, R.layout.listview_jobs_items,
                     data);
             ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
             listview_jobs.setAdapter(adapter);
             listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 @Override
                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                     switch (i - adapterView.getCount()) {
                         case -1:
                             toRevokeAccess();
                             break;
                         case -2:
                             toBrowseWebPage();
                             break;
                         default:
                             Object item = adapterView.getAdapter().getItem(i);
                             if (item instanceof Map) {
                                 if (!((Map) item).containsKey("ID")) break;
                                 int ID = Integer.parseInt(((Map) item).get("ID").toString());
                                 Intent intent = new Intent(Jobs.this, JobsDetails.class);
                                 intent.putExtra("API_TOKEN", API_TOKEN);
                                 intent.putExtra("JOB_ID", ID);
                                 intent.putExtra("JOBS_SHOW_URL", JOBS_SHOW_URL);
                                 intent.putExtra("JOBS_RUN_URL", JOBS_RUN_URL);
                                 intent.putExtra("JOBS_PARAMETERS_INDEX_URL",
                                         JOBS_PARAMETERS_INDEX_URL);
                                 Jobs.this.startActivity(intent);
                             }
                     }
                 }
             });
             listview_jobs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                 @Override
                 public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                     Object item = adapterView.getAdapter().getItem(i);
                     if (item instanceof Map) {
                        if (((Map) item).containsKey("NAME")) {
                            copyText(((Map) item).get("NAME").toString());
                             Toast.makeText(Jobs.this, R.string.string_copied, Toast.LENGTH_SHORT).show();
                             return true;
                         }
                     }
                     return false;
                 }
             });
         } catch (JSONException e) {
             showSimpleErrorDialog(getString(R.string.error_should_update_easyjobs));
             showReloadAndScanButton();
         }
     }
 
     private void showScanButton() {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("K", getString(R.string.scan));
         map.put("V", getString(R.string.scan_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.about));
         map.put("V", getString(R.string.about_desc));
         data.add(map);
         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                 R.layout.listview_jobs_items, new String[]{"K", "V"},
                 new int[]{R.id.text_key, R.id.text_value});
         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
         listview_jobs.setAdapter(adapter);
         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 switch (i) {
                     case 0:
                         openScanner();
                         break;
                     case 1:
                         showAboutInfo();
                         break;
                 }
             }
         });
     }
 
     private void showReloadAndScanButton() {
         List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("K", getString(R.string.retry));
         map.put("V", getString(R.string.retry_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.scan));
         map.put("V", getString(R.string.scan_desc));
         data.add(map);
         map = new HashMap<String, Object>();
         map.put("K", getString(R.string.revoke_access));
         map.put("V", getString(R.string.revoke_access_desc));
         data.add(map);
         SimpleAdapter adapter = new SimpleAdapter(Jobs.this, data,
                 R.layout.listview_jobs_items, new String[]{"K", "V"},
                 new int[]{R.id.text_key, R.id.text_value});
         ListView listview_jobs = (ListView) findViewById(R.id.listView_jobs);
         listview_jobs.setAdapter(adapter);
         listview_jobs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 switch (i) {
                     case 0:
                         startEasyJobs();
                         break;
                     case 1:
                         openScanner();
                         break;
                     case 2:
                         toRevokeAccess();
                         break;
                 }
             }
         });
     }
 
     private void openScanner() {
         try {
             Intent intent = new Intent("com.google.zxing.client.android.SCAN");
             intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
             startActivityForResult(intent, 1);
         } catch (ActivityNotFoundException e) {
             AlertDialog alertDialog = new AlertDialog.Builder(Jobs.this).create();
             alertDialog.setTitle(R.string.error);
             alertDialog.setMessage(getString(R.string.error_barcode_scanner_not_installed));
             alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                         }
                     });
             alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                     new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialogInterface, int i) {
                             Intent intent = new Intent(Intent.ACTION_VIEW);
                             intent.setData(Uri.parse(
                                     "market://details?id=com.google.zxing.client.android"));
                             startActivity(intent);
                         }
                     });
             alertDialog.show();
         }
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == 1 && resultCode == RESULT_OK) {
             String content = intent.getStringExtra("SCAN_RESULT");
             if (content == null) return;
             decode(content);
         }
     }
 
     private void decode(String content) {
         String decoded_content = null;
         try {
             byte[] decoded = Base64.decode(content, Base64.NO_WRAP);
             decoded_content = new String(decoded);
         } catch (IllegalArgumentException e) {
             showSimpleErrorDialog(getString(R.string.error_invalid_qrcode));
         }
         if (decoded_content != null) {
             try {
                 JSONObject object = new JSONObject(decoded_content);
                 int VERSION = object.getInt("v");
                 String URL = object.getString("u");
                 String CONTENT = object.getString("c");
 
                 // validate version number
 
                 if (VERSION <= 0) throw new JSONException(null);
                 if (VERSION > MAX_API_VERSION)
                     throw new Exception(getString(R.string.error_please_update_app));
 
                 // validate help URL
                 URL url = new URL(URL);
                 url.toURI(); // stop never used warning
 
                 if (CONTENT.length() == 0)
                     throw new Exception(getString(R.string.error_invalid_qrcode));
 
                 SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
                 SharedPreferences.Editor editor = sharedPrefs.edit();
                 editor.putInt(PREF_VERSION, VERSION);
                 editor.putString(PREF_URL, URL);
                 editor.putString(PREF_CONTENT, CONTENT);
                 editor.commit();
 
             } catch (MalformedURLException e) {
                 showSimpleErrorDialog(getString(R.string.error_invalid_url));
             } catch (JSONException e) {
                 showSimpleErrorDialog(getString(R.string.error_invalid_qrcode));
             } catch (Exception e) {
                 showSimpleErrorDialog(e.getMessage());
             }
             if (readPrefs()) {
                 startEasyJobs();
             }
         }
     }
 
     private void toBrowseWebPage() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.confirm_browse_webpage).setPositiveButton(R.string.yes,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         Intent intent = new Intent(Intent.ACTION_VIEW,
                                 Uri.parse(TOKEN_LOGIN_URL.replace(":token", API_TOKEN)));
                         startActivity(intent);
                     }
                 }).setNegativeButton(R.string.no, null).show();
     }
 
     private void toRevokeAccess() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.confirm_revoke_access).setPositiveButton(R.string.yes,
                 new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         revokeAccess();
                     }
                 }).setNegativeButton(R.string.no, null).show();
     }
 
     private void revokeAccessOnly() {
         SharedPreferences sharedPrefs = getSharedPreferences(PREF_FILE, 0);
         SharedPreferences.Editor editor = sharedPrefs.edit();
         editor.clear();
         editor.commit();
 
         clearEtags();
 
         AsyncHttpClient client = new AsyncHttpClient();
         client.delete(REVOKE_TOKEN_URL + "?token=" + API_TOKEN, new AsyncHttpResponseHandler() {
             @Override
             public void onSuccess(String response) {
             }
         });
     }
 
     private void revokeAccess() {
         revokeAccessOnly();
         setAllVariablesToEmpty();
         updateTitle();
         startEasyJobs();
     }
 
     private void setAllVariablesToEmpty() {
         API_HELP_URL = "";
         API_TOKEN = "";
         JOBS_INDEX_VERB = "";
         JOBS_INDEX_URL = "";
         JOBS_SHOW_URL = "";
         JOBS_RUN_URL = "";
         JOBS_PARAMETERS_INDEX_URL = "";
         REVOKE_TOKEN_URL = "";
         TOKEN_LOGIN_URL = "";
     }
 
     private void updateTitle() {
         if (API_HELP_URL.length() > 0) {
             Uri uri = Uri.parse(API_HELP_URL);
             String host = uri.getHost();
             if (uri.getPort() > 0 && uri.getPort() != 80) {
                 host += ":" + uri.getPort();
             }
             setTitle(getString(R.string.app_name) + " - " + host);
         } else {
             setTitle(getString(R.string.app_name));
         }
     }
 
 }
