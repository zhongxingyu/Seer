 package nl.tudelft.tribler.ppspdroid;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.media.ThumbnailUtils;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Video.Thumbnails;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class PPSPdroidIntroActivity extends Activity {
     private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 100;
     private static final boolean DEBUG_MODE = true;
     private static final String Log_Tag = "PPSP";
     private static final int SELECT_VIDEO_FILE_REQUEST_CODE = 200;
 
     private Button mShareBtn;
 
     private TextView mVideoPath, mVideoDetailHeading;
 
     private ImageView mVideothumbnail;
     private EditText mVideoTitle, mVideoTags, mVideoDescription;
     private Uri mVideoUri;
 
     /** Returns mVideoUri */
     public Uri getVideoURI() {
         return mVideoUri;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         this.mVideoDetailHeading = (TextView) findViewById(R.id.videoDetailHeading);
         this.mVideoPath = (TextView) findViewById(R.id.fullPath);
         this.mVideoTitle = (EditText) findViewById(R.id.videoTitle);
         this.mVideoTags = (EditText) findViewById(R.id.videoTags);
         this.mVideoDescription = (EditText) findViewById(R.id.videoDescription);
         this.mShareBtn = (Button) findViewById(R.id.shareButton);
         this.mVideothumbnail = (ImageView) findViewById(R.id.videothumbnail);
     }
 
     /** Open phone's gallery when user clicks the button 'Select a video' */
     public void selectVideo(View view) {
         Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
         intent.setType("video/*"); // Only show videos
         startActivityForResult(intent, SELECT_VIDEO_FILE_REQUEST_CODE);
         setTextFields();
     }
 
     /** Sets mVideoUri */
     public void setVideoURI(Uri vUri) {
         mVideoUri = vUri;
     }
 
     /** Open phone's gallery when user clicks the button 'Select a video' */
     public void shareVideo(View view) {
         Uri vUri = getVideoURI();
         if (DEBUG_MODE) {
             Log.d(Log_Tag, "ShareVideoURI=" + vUri);
         }
         Intent intent = new Intent(Intent.ACTION_SEND);
         intent.setType("video/*"); // Specify intent for video data
         intent.putExtra(Intent.EXTRA_STREAM, vUri);
         // TODO: Pass values of selectVideoTitle, selectVideoTags,
         // selectVideoDescription
         // Show applications that could handle video
         startActivity(Intent.createChooser(intent, "Share the video using"));
     }
 
     /** Start phone's camera when user clicks the button 'Record a video' */
     public void startCamera(View view) {
         Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
         startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
         setTextFields();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (DEBUG_MODE) {
             Log.d(Log_Tag, "onActivityResult RequestCode=" + requestCode + " " + "ResultCode="
                            + resultCode);
         }
         if (DEBUG_MODE) {
             Log.d(Log_Tag, "DataUri=" + data.getData());
         }
         // TODO: Manage situation when data=null
 
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
         case CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE:
             if (resultCode == RESULT_OK && data.getDataString() != null) {
                 Toast.makeText(this, "Video saved to:\n" + data.getData(), Toast.LENGTH_LONG)
                      .show();
                 showTextFields(0);
                 setTextFields(data.getDataString(), data.getData().getLastPathSegment());
                 Uri vUri = data.getData();
                 setVideoURI(vUri);
                 String vPath = getRealPathFromURI(vUri);
                 setVideoThumbnail(vPath);
             } else if (resultCode == RESULT_OK && data.getDataString() == null) {
                 if (DEBUG_MODE) {
                     Log.i(Log_Tag, "Problem in saving the video");
                 }
                 Toast.makeText(this, "Problem in saving the video", Toast.LENGTH_LONG).show();
             } else if (resultCode == RESULT_CANCELED) {
                 // User cancelled the video capture
             } else {
                 // Video capture failed, advise user
             }
             break;
 
         case SELECT_VIDEO_FILE_REQUEST_CODE:
             if (resultCode == RESULT_OK) {
                 showTextFields(0);
                 setTextFields(data.getDataString(), data.getData().getLastPathSegment());
                 Uri vUri = data.getData();
                 setVideoURI(vUri);
                 String vPath = getRealPathFromURI(vUri);
                 setVideoThumbnail(vPath);
             } else if (resultCode == RESULT_CANCELED) {
                 // User cancelled the video selection
                 if (DEBUG_MODE) {
                     Log.i(Log_Tag, "User cancelled the video selection");
                 }
                 Toast.makeText(this, "User cancelled the video selection", Toast.LENGTH_LONG)
                      .show();
             } else {
                 // Some other error, advise user
                 if (DEBUG_MODE) {
                     Log.i(Log_Tag, "Problem in selecting the video");
                 }
                 Toast.makeText(this, "Problem in selecting the video", Toast.LENGTH_LONG)
                      .show();
             }
             break;
         }
     }
 
     /** Extracts video's fullpath from the Uri */
     private String getRealPathFromURI(Uri contentUri) {
         String[] proj = { MediaStore.Images.Media.DATA };
         Cursor cursor = managedQuery(contentUri, proj, null, null, null);
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         return cursor.getString(column_index);
     }
 
     /** Clears the text fields and set the default values */
     private void setTextFields() {
        mVideoTitle.setText("Video Title");
        mVideoTags.setText("Add Tags");
        mVideoDescription.setText("Add Description");
     }
 
     /** Displays the filepath and video title */
     private void setTextFields(String filepath, String videoTitle) {
         mVideoPath.setText("File-path:" + filepath);
         mVideoTitle.setText("PPSP_VID" + "_" + videoTitle);
     }
 
     /** Displays the video thumbnail on screen */
     private void setVideoThumbnail(String videoFilePath) {
         Bitmap bmThumbnail;
         bmThumbnail = ThumbnailUtils.createVideoThumbnail(videoFilePath, Thumbnails.MICRO_KIND);
         mVideothumbnail.setImageBitmap(bmThumbnail);
     }
 
     /** Change the visibility of text fields and buttons */
     private void showTextFields(int visibility) {
         // Visibility=0 i.e. visible, Visibility=4 i.e invisible
         mVideoDetailHeading.setVisibility(visibility);
         mVideoTitle.setVisibility(visibility);
         mVideoTags.setVisibility(visibility);
         mVideoDescription.setVisibility(visibility);
         mShareBtn.setVisibility(visibility);
     }
 
 }
