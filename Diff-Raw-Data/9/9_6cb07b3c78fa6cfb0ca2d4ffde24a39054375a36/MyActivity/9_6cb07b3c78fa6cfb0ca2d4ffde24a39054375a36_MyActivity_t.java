 package no.radiomotor.android;
 
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.support.v4.app.NotificationCompat;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.FacebookRequestError;
 import com.facebook.Response;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.ItemClick;
 import com.googlecode.androidannotations.annotations.OnActivityResult;
 import com.googlecode.androidannotations.annotations.OptionsItem;
 import com.googlecode.androidannotations.annotations.OptionsMenu;
 import com.googlecode.androidannotations.annotations.UiThread;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.menor.easyfacebookconnect.EasyActionListener;
 import com.menor.easyfacebookconnect.EasyLoginListener;
 import com.menor.easyfacebookconnect.ui.EasyFacebookFragmentActivity;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 
 import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
 import static android.widget.Toast.LENGTH_LONG;
 import static no.radiomotor.android.RadioService.ACTION_PLAY;
 import static no.radiomotor.android.RadioService.ACTION_STARTED;
 import static no.radiomotor.android.RadioService.ACTION_STOP;
 import static no.radiomotor.android.RadioService.ACTION_STOPPED;
 import static no.radiomotor.android.RadioService.ACTION_STOPPED_ERROR;
 import static no.radiomotor.android.RadiomotorXmlParser.Item;
 
 @EActivity(R.layout.main)
 @OptionsMenu(R.menu.main)
 public class MyActivity extends EasyFacebookFragmentActivity {
     public static final String IS_RADIO_PLAYING_KEY = "isRadioPlaying";
     private static final String PAGE_ID = "158209464386457";
     private static final int NOTIFICATION_ID = 24;
     private static final int GALLERY_REQUEST_CODE = 0;
     private final int PICTURE_REQUEST_CODE = 1;
     private final String IMAGE_PATH = Environment.getExternalStorageDirectory()+File.separator + "radiomotor.jpg";
 
     @ViewById ListView newsFeedList;
     @ViewById TextView noNewsTextView;
 
     private CacheHelper cacheHelper;
     boolean isRadioPlaying;
     MenuItem refresh;
     MenuItem radioControl;
     NotificationManager mNotificationManager;
     private NotificationCompat.Builder notificationBuilder;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         isRadioPlaying = SharedPreferencesHelper.get(this).getBoolean(IS_RADIO_PLAYING_KEY, false);
         mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
     @AfterViews
     void afterViewsSetup() {
         cacheHelper = new CacheHelper(getApplicationContext());
         downloadNewsfeed();
 
         LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
         IntentFilter intentFilter = new IntentFilter();
         intentFilter.addAction(ACTION_STARTED);
         intentFilter.addAction(ACTION_STOPPED);
         intentFilter.addAction(ACTION_STOPPED_ERROR);
         bManager.registerReceiver(broadcastReceiver, intentFilter);
     }
 
     @OptionsItem(R.id.action_picture)
     public void cameraSelected() {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                 MyActivity.this);
         alertDialogBuilder.setTitle(getString(R.string.choose_picture_operation));
         alertDialogBuilder
                 .setMessage(getString(R.string.picture_information))
                 .setCancelable(true)
                 .setPositiveButton(getString(R.string.take_new_photo),new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,int id) {
                         File file = new File(IMAGE_PATH);
                         Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                         cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                         startActivityForResult(cameraIntent, PICTURE_REQUEST_CODE);
                     }
                 })
                 .setNegativeButton(getString(R.string.upload_from_gallery),new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog,int id) {
                         Intent galleryIntent = new Intent(
                                 Intent.ACTION_PICK,
                                 android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                         startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
                     }
                 });
         AlertDialog alertDialog = alertDialogBuilder.create();
         alertDialog.show();
     }
 
     @OptionsItem(R.id.action_play)
     public void radioPlayerSelected() {
         Intent i = new Intent(getApplicationContext(), RadioService.class);
         i.setAction(isRadioPlaying ? ACTION_STOP : ACTION_PLAY);
         if (!isRadioPlaying) {
             radioControl.setActionView(R.layout.actionbar_indeterminate_progress);
         }
         setVolumeControlStream(AudioManager.STREAM_MUSIC);
         startService(i);
     }
 
     @OptionsItem(R.id.action_refresh)
     public void newsRefreshSelected() {
         refresh.setActionView(R.layout.actionbar_indeterminate_progress);
         downloadNewsfeed();
     }
 
     @Override
     public boolean onCreateOptionsMenu(final Menu menu) {
         refresh = menu.findItem(R.id.action_refresh);
         radioControl = menu.findItem(R.id.action_play);
         changeRadioStatus(isRadioPlaying);
         return true;
     }
 
     private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction().equals(RadioService.ACTION_STARTED)) {
                 changeRadioStatus(true);
             } else if (intent.getAction().equals(ACTION_STOPPED)) {
                 changeRadioStatus(false);
             } else if (intent.getAction().equals(ACTION_STOPPED_ERROR)) {
                 Toast.makeText(getApplicationContext(), getString(R.string.playback_error), LENGTH_LONG).show();
                 changeRadioStatus(false);
             }
         }
     };
 
     private void changeRadioStatus(boolean playing) {
         radioControl.setActionView(null);
         radioControl.setIcon(playing ? R.drawable.ic_action_av_stop : R.drawable.ic_action_av_play);
         radioControl.setTitle(playing ? R.string.action_stop : R.string.action_play);
         SharedPreferencesHelper.get(this).putBoolean(IS_RADIO_PLAYING_KEY, playing);
         isRadioPlaying = playing;
     }
 
     @OnActivityResult(GALLERY_REQUEST_CODE)
     void onGalleryResult(int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             Uri selectedImage = data.getData();
             String[] filePaths = { MediaStore.Images.Media.DATA };
             Cursor cursor = getContentResolver().query(selectedImage,
                     filePaths, null, null, null);
             cursor.moveToFirst();
 
             int columnIndex = cursor.getColumnIndex(filePaths[0]);
             String path = cursor.getString(columnIndex);
             cursor.close();
 
             uploadPicture(path);
         }
     }
 
     @OnActivityResult(PICTURE_REQUEST_CODE)
     void onCameraResult(int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             if (!isConnected()) {
                 connect(new EasyLoginListener() {
                     @Override
                     public void onSuccess(Response response) {
                         uploadPicture(IMAGE_PATH);
                     }
 
                     @Override
                     public void onError(FacebookRequestError error) {
                         Toast.makeText(MyActivity.this, getString(R.string.facebook_login_failed_message), Toast.LENGTH_LONG).show();
                     }
                 });
             } else {
                 uploadPicture(IMAGE_PATH);
             }
         }
     }
 
     void uploadPicture(String path) {
         Toast.makeText(MyActivity.this, getString(R.string.upload_picture_confirmation), Toast.LENGTH_LONG).show();
         notificationBuilder = new NotificationCompat.Builder(MyActivity.this);
         notificationBuilder.setContentTitle(getString(R.string.upload_notification_title));
         notificationBuilder.setContentText(getString(R.string.upload_notification_in_progress_text));
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_radiomotor);
         notificationBuilder.setProgress(0, 0, true);
         mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
         final Bitmap bitmap = BitmapFactory.decodeFile(path);
         shareImageToPage(PAGE_ID + "/photos", bitmap, new EasyActionListener() {
             @Override
             public void onSuccess(Response response) {
                 notificationBuilder.setContentText(getString(R.string.upload_notification_done_text))
                         .setProgress(0, 0, false);
                 mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
             }
 
             @Override
             public void onError(FacebookRequestError error, Exception exception) {
                 notificationBuilder.setContentText(getString(R.string.upload_notification_error_text))
                         .setProgress(0, 0, false);
                 mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
             }
         });
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Background
     void downloadNewsfeed() {
         String urlString = "http://www.radiomotor.no/feed/";
         InputStream stream = null;
         RadiomotorXmlParser parser = new RadiomotorXmlParser();
         ArrayList<Item> entries;
         try {
             stream = downloadUrl(urlString);
             entries = parser.parse(stream);
             cacheHelper.writeNewsItems(entries);
         } catch (XmlPullParserException e) {
             errorMessage(R.string.parse_error);
         } catch (IOException e) {
             errorMessage(R.string.connection_error);
         } finally {
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {}
             }
         }
         updateListview();
     }
 
     @UiThread
     void updateListview() {
         if (refresh != null) {
             refresh.setActionView(null);
         }
         ArrayList<Item> items = new ArrayList<Item>();
         try {
             items = cacheHelper.readNewsItems();
         } catch (IOException ignored) { }
 
         if (items.size() > 0) {
             noNewsTextView.setVisibility(View.GONE);
             newsFeedList.setAdapter(new NewsFeedAdapter(getApplicationContext(), R.layout.row_newsitem, items));
         } else {
             newsFeedList.setVisibility(View.GONE);
             noNewsTextView.setVisibility(View.VISIBLE);
         }
     }
 
     @UiThread
     void errorMessage(int resourceId) {
         Toast.makeText(getApplicationContext(), resourceId, LENGTH_LONG).show();
     }
 
     @ItemClick
     void newsFeedListItemClicked(Item item) {
         NewsItemActivity_.intent(getApplicationContext()).flags(FLAG_ACTIVITY_NEW_TASK).newsItem(item).start();
     }
 
     private InputStream downloadUrl(String urlString) throws IOException {
         URL url = new URL(urlString);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setReadTimeout(10000 /* milliseconds */);
         conn.setConnectTimeout(15000 /* milliseconds */);
         conn.setRequestMethod("GET");
         conn.setDoInput(true);
         conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
         conn.connect();
         return conn.getInputStream();
     }
 }
