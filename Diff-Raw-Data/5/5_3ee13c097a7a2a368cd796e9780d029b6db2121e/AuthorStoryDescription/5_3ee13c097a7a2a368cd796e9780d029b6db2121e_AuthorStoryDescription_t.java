 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.ICurrentStoryListener;
 import ca.cmput301f13t03.adventure_datetime.model.Story;
 import ca.cmput301f13t03.adventure_datetime.serviceLocator.Locator;
 
 import java.io.File;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * View containing description of story create by author.
  * <p/>
  * User can swipe between the fragments to view other stories
  * in list.
  * <p/>
  * TODO: Link with model, controller
  *
  * @author James Finlay
  * @author Andrew Fontaine
  */
 public class AuthorStoryDescription extends Activity implements ICurrentStoryListener {
     private static final String TAG = "AuthorStoryDescription";
     private static final int PICTURE_REQUEST = 1;
     private Story _story;
     private EditText _title, _content;
     private Uri _newThumbnail;
 
     @Override
     public void OnCurrentStoryChange(Story story) {
         _story = story;
         setUpView();
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.author_descript);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.authordesc, menu);
         return true;
     }
 
     private void setUpView() {
         if(_story == null) return;
 
         /** Action bar **/
         getActionBar().setTitle(_story.getTitle());
 
         /** Layout items **/
         ImageView thumbnail = (ImageView) findViewById(R.id.thumbnail);
         _title = (EditText) findViewById(R.id.title);
         TextView author = (TextView) findViewById(R.id.author);
         _content = (EditText) findViewById(R.id.content);
         TextView datetime = (TextView) findViewById(R.id.datetime);
         TextView fragments = (TextView) findViewById(R.id.fragments);
         RelativeLayout header = (RelativeLayout) findViewById(R.id.header);
 
 		/* Text */
         thumbnail.setImageBitmap(_story.decodeThumbnail());
         _title.setText(_story.getTitle());
         author.setText("Creator: " + _story.getAuthor());
         datetime.setText("Last Modified: " + _story.getFormattedTimestamp());
         fragments.setText("Fragments: " + _story.getFragmentIds().size());
         _content.setText(_story.getSynopsis());
 
 
 		/*	switch (_story.) {
         case STATUS_UNPUBLISHED:
 			// Light blue
 			header.setBackgroundColor(Color.parseColor("#d4eef8"));
 			menu.findItem(R.id.action_upload).setIcon(R.drawable.ic_action_upload);
 			break;
 		case STATUS_UNSYNC:
 			// Light orange
 			header.setBackgroundColor(Color.parseColor("#f8e7d4"));
 			menu.findItem(R.id.action_upload).setIcon(R.drawable.ic_action_refresh);
 			break;
 		case STATUS_SYNCED:
 			// Light green
 			header.setBackgroundColor(Color.parseColor("#d4f8e1"));
 			menu.findItem(R.id.action_upload).setEnabled(false).setVisible(false);
 			break;
 		default:
 			Log.e(TAG, "Status unknown.");
 		}
 		 */
 
 			Locator.getAuthorController().selectStory(_story.getId());
 			Locator.getAuthorController().saveStory();
 
     }
 
     @Override
     public void onResume() {
         Locator.getPresenter().Subscribe(this);
         super.onResume();
     }
 
     @Override
     public void onPause() {
         Locator.getPresenter().Unsubscribe(this);
         super.onPause();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch(item.getItemId()) {
             case R.id.action_editfragments:
                 Locator.getAuthorController().selectFragment(_story.getHeadFragmentId());
                 Intent intent = new Intent(this, AuthorEdit.class);
                 startActivity(intent);
                 break;
             case R.id.action_upload:
                 Locator.getAuthorController().upload();
                 Toast.makeText(getApplicationContext(), "Uploaded!", Toast.LENGTH_LONG).show();
                 break;
             case R.id.action_discard:
             /* Ensure user is not retarded and actually wants to do this */
                 new AlertDialog.Builder(this)
                         .setTitle("Delete Story")
                         .setMessage("This will kill the story.\nAction cannot be undone.")
                         .setCancelable(true)
                         .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 Locator.getAuthorController().deleteStory(_story.getId());
                                 finish();
                             }
                         })
                         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.cancel();
                             }
                         })
                         .create().show();
                 break;
             case R.id.action_save:
                 _story.setTitle(_title.getText().toString());
                 _story.setSynopsis(_content.getText().toString());
                 _story.updateTimestamp();
                Locator.getAuthorController().saveStory();
                 Toast.makeText(getApplicationContext(), "Story saved!", Toast.LENGTH_SHORT).show();
                 break;
             default:
                 Log.e(TAG, "onOptionsItemSelected -> Unknown MenuItem");
                 break;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     public void getImage(View v) {
         File picDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                 "adventure.datetime");
         if(!picDir.exists()) picDir.mkdirs();
         File pic = new File(picDir.getPath(), File.separator + _story.getId().toString());
         Uri location = Uri.fromFile(pic);
 
         final List<Intent> cameraIntents = new ArrayList<Intent>();
         final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         final PackageManager packageManager = getPackageManager();
         final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
         for(ResolveInfo res : listCam) {
             final String packageName = res.activityInfo.packageName;
             final Intent intent = new Intent(captureIntent);
             intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
             intent.setPackage(packageName);
             intent.putExtra(MediaStore.EXTRA_OUTPUT, location);
             _newThumbnail = location;
             cameraIntents.add(intent);
         }
 
         // Filesystem.
         final Intent galleryIntent = new Intent();
         galleryIntent.setType("image/*");
         galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
 
         // Chooser of filesystem options.
         final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
 
         // Add the camera options.
         chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
 
         startActivityForResult(chooserIntent, PICTURE_REQUEST);
 
         //_fragment.addMedia(location);
 
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(resultCode == RESULT_OK) {
             if(requestCode == PICTURE_REQUEST) {
                 final boolean isCamera;
                 if(data == null) {
                     isCamera = true;
                 }
                 else {
                     final String action = data.getAction();
                     if(action == null) {
                         isCamera = false;
                     }
                     else {
                         isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                     }
                 }
 
                 Uri selectedImageUri;
                 if(isCamera) {
                     selectedImageUri = _newThumbnail;
                 }
                 else {
                     selectedImageUri = data == null ? null : data.getData();
                 }
                 InputStream is;
                 try {
                     is = getContentResolver().openInputStream(selectedImageUri);
                     Bitmap bit = BitmapFactory.decodeStream(is);
                     is.close();
                     _story.setThumbnail(bit);
                    Locator.getAuthorController().saveStory();
 
                 }
                 catch(Exception e) {
                     Log.e(TAG, "Error setting bitmap", e);
                 }
             }
         }
     }
 }
