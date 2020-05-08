 
 package ualberta.g12.adventurecreator.views;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 
 import ualberta.g12.adventurecreator.R;
 import ualberta.g12.adventurecreator.controllers.FragmentController;
 import ualberta.g12.adventurecreator.controllers.StoryController;
 import ualberta.g12.adventurecreator.controllers.StoryListController;
 import ualberta.g12.adventurecreator.data.AdventureCreator;
 import ualberta.g12.adventurecreator.data.Choice;
 import ualberta.g12.adventurecreator.data.Fragment;
 import ualberta.g12.adventurecreator.data.Story;
 import ualberta.g12.adventurecreator.data.StoryList;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
import java.util.Locale;
 
 // Right now we are making Fragments here and also choice
 // What i was thinking was maybe just make the general fragment here
 // and have another screen to add choices then we could have list a listview type thing so can have a lot of choices
 // and long clicking each one would let you edit/delete them 
 // TODO discuss with the team
 //
 
 /**
  * Activity for editing a fragment. Will allow the author to add text,
  * illustration s, or choices for the current fragment. Upon button clicks, the
  * activity will save the current fragment information and take the user back to
  * the fragment list of the current story.
  */
 public class FragmentEditActivity extends Activity implements FView<Fragment> {
 
     // UI Objects
     private EditText editTitleText;
     private ListView fragmentPartListView;
     private FragmentPartAdapter adapter;
     
     // Fragment stuff
     private Fragment fragment;
     private int position;
     public int picturePosition;
     private String pictureMode;
 
     private StoryList storyList;
     private Story story;
     private int storyPos, fragPos;
     
     // Controllers
     private StoryListController storyListController;
     private StoryController storyController;
     private FragmentController fragmentController;
     
     // Constants
     public static final int EDIT = 0;
     public static final int ADD = 1;
 
     // Logging info
     private static final String TAG = "FragmentEditActivity";
     private static final boolean DEBUG = true;
     
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_fragment_editor);
 
         storyListController = AdventureCreator.getStoryListController();
         storyController = AdventureCreator.getStoryController();
         fragmentController = AdventureCreator.getFragmentController();
         
         // obtain the intent
         Intent editActIntent = getIntent();
         storyPos = (Integer) editActIntent.getSerializableExtra("StoryPos");
         // Not reliable when in view mode
         fragPos = (Integer) editActIntent.getSerializableExtra("FragmentPos");
         // get widget references
         fragmentPartListView = (ListView) findViewById(R.id.FragmentPartList);
         editTitleText = (EditText) findViewById(R.id.fragmentTitle);
         
         // load save file
         storyList = AdventureCreator.getStoryList();
         story = storyList.getAllStories().get(storyPos);
         fragment = story.getFragments().get(fragPos);
 
         // Load title
         editTitleText.setText(fragment.getTitle());
         
         // Make user we have at least one part
         if(fragment.getDisplayOrder().size() == 0){
             fragmentController.addEmptyPart(fragment);
         }
         
         adapter = new FragmentPartAdapter(this, R.layout.activity_fragment_editor, fragment);
         fragmentPartListView.setAdapter(adapter);
         
         // Need some context menu for the list parts
         registerForContextMenu(fragmentPartListView);
         
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         fragment.addView(this);
     }
     
     @Override
     protected void onDestroy(){
         super.onDestroy();
         fragment.deleteView(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.fragment_editor, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // TODO Auto-generated method stub
         switch (item.getItemId()) {
           //  case R.id.add_option:
            //     return true;
 
             case R.id.save_fragment:
                 // Save values
                 saveFragment();
 
                 // Leave activity
                 finish();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void update(Fragment model) {
         editTitleText.setText(model.getTitle());
         
         fragment = model;
         adapter.notifyDataSetChanged();
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.fragment_part_menu, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                 .getMenuInfo();
         position = (int) info.id;
         CharSequence itemTitle = item.getTitle();
         if (itemTitle.equals("Insert Text")) {
             fragmentController.addTextSegment(fragment, "New text", position);
 
         } else if (itemTitle.equals("Insert Illustration")) {
             Log.d(TAG,"insert ill start");
             saveFragment();
             pictureMode = "Add";
             picturePosition = position;
             AddImage();
            
             
         /*} else if (itemTitle.equals("Insert Illustration (Small)")) {
             Log.d(TAG,"insert ill start");
             saveFragment();
             pictureMode = "Add";
             picturePosition = position;
             //AddImage2();???
              */
 
             
         } else if (itemTitle.equals("Edit")) {
             String type = fragmentController.getDisplayTypeAtPos(fragment, position);
             if (type.equals("t") || type.equals("e")) {
                 RelativeLayout curLayout = new RelativeLayout(this);
 
                 LayoutInflater inflater = (LayoutInflater) this
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 final PopupWindow editTextWindow = new PopupWindow(inflater.inflate(
                         R.layout.edit_text_seg_popup, null, true), fragmentPartListView.getWidth(),
                         400, true);
 
                 final EditText editTextSegView = (EditText) editTextWindow.getContentView()
                         .findViewById(R.id.editTextSeg);
                 if (type.equals("t")) {
                     int occurrence = 0;
                     for (int i = 0; i < position; i++) {
                         if (fragmentController.getDisplayTypeAtPos(fragment, i).equals("t"))
                             occurrence++;
                     }
                     editTextSegView.setText(fragmentController.getTextSegments(fragment).get(
                             occurrence));
                 }
 
                 Button editTextSave = (Button) editTextWindow.getContentView().findViewById(
                         R.id.editTextSave);
                 Button editTextCancel = (Button) editTextWindow.getContentView().findViewById(
                         R.id.editTextCancel);
 
                 editTextSave.setOnClickListener(new EditTextSegOnClickListener(position) {
                     @Override
                     public void onClick(View v) {
                         String newText = editTextSegView.getText().toString();
                         fragmentController.deleteFragmentPart(fragment, this.getPosition());
                         fragmentController.addTextSegment(fragment, newText, this.getPosition());
                         fragmentPartListView.invalidateViews();
                         editTextWindow.dismiss();
                     }
                 });
 
                 editTextCancel.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         editTextWindow.dismiss();
                     }
                 });
 
                 editTextWindow.showAtLocation(curLayout, Gravity.CENTER, 0, 0);
                 editTextWindow.update(0, 0, fragmentPartListView.getWidth(), 400);
 
             } else if (type.equals("i")) {
                 saveFragment();
                 pictureMode = "Edit";
                 picturePosition = position;
                 AddImage();
             } else if (type.equals("c")) {
 
                 // get the occurrence number of the choice
                 int choicePos = 0;
                 for (int i = 0; i < position; i++) {
                     if (fragment.getDisplayOrder().get(i).equals("c"))
                         choicePos++;
                 }
 
                 // go to edit choice activity
                 Intent intent = new Intent(this, ChoiceEditActivity.class);
                 intent.putExtra("StoryPos", storyPos);
                 intent.putExtra("FragmentPos", fragPos);
                 intent.putExtra("ChoicePos", choicePos);
                 startActivity(intent);
             }
 
         } else if (itemTitle.equals("Add Choice")) {
             // create, add and save new choice
             int choicePos = fragment.getChoices().size();
             Choice choice = new Choice();
             fragmentController.addChoice(fragment, choice);
             saveFragment();
 
             // go to edit choice activity
             Intent intent = new Intent(this, ChoiceEditActivity.class);
             intent.putExtra("StoryPos", storyPos);
             intent.putExtra("FragmentPos", fragPos);
             intent.putExtra("ChoicePos", choicePos);
             startActivity(intent);
 
         } else if (itemTitle.equals("Delete")) {
             fragmentController.deleteFragmentPart(fragment, position);
             
          // Make sure the fragment isn't completely empty
             if (fragment.getDisplayOrder().size() == 0)
                 fragmentController.addEmptyPart(fragment);
         }
 
         // reset listview to display any changes
         // adapter.notifyDataSetChanged();
         fragmentPartListView.invalidateViews();
         return true;
     }
 
     /**
      * Allows the user to import an image from either the camera or gallery
      * through selection prompts. The image is put into a listview that is
      * displayed for the current fragment. The function branches to
      * "onActivityResult" which handles the image processing.
      */
     public void AddImage() {
         final CharSequence[] options = {
                 "Take Photo", "Choose from Gallery", "Cancel"
         };
 
         AlertDialog.Builder builder = new AlertDialog.Builder(FragmentEditActivity.this);
         builder.setTitle("Add Photo!");
         builder.setItems(options, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int item) {
                 if (options[item].equals("Take Photo"))
                 {
                     Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                     File f = new File(android.os.Environment.getExternalStorageDirectory(),
                             "temp.jpg");
                     intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                     startActivityForResult(intent, 1);
                 }
                 else if (options[item].equals("Choose from Gallery"))
                 {
                     Intent intent = new Intent(Intent.ACTION_PICK,
                             android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                     startActivityForResult(intent, 2);
 
                 }
                 else if (options[item].equals("Cancel")) {
                     dialog.dismiss();
                 }
             }
         });
         builder.show();
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (resultCode == RESULT_OK) {
             Bitmap bitmap = null;
             String picturePath = null;
             if (requestCode == 1) {
 
                 File f = new File(Environment.getExternalStorageDirectory().toString());
                 for (File temp : f.listFiles()) {
                     if (temp.getName().equals("temp.jpg")) {
                         f = temp;
                         break;
                     }
                 }
                 try {
                     BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
 
                     bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                             bitmapOptions);
                     Log.d(TAG, "path of image from camera" + f.getAbsolutePath() + "");
                     f.delete();
                     
                     
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } else if (requestCode == 2) {
 
                 Uri selectedImage = data.getData();
                 String[] filePath = {
                         MediaStore.Images.Media.DATA
                 };
                 Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                 c.moveToFirst();
                 int columnIndex = c.getColumnIndex(filePath[0]);
                 picturePath = c.getString(columnIndex);
                 c.close();
                 bitmap = (BitmapFactory.decodeFile(picturePath));
                 Log.d(TAG, "path of image from gallery" + picturePath + "");
             }
             
             //following line modified from https://groups.google.com/forum/#!topic/android-developers/YjGcve7s5CQ
             //by Derek
             CharSequence appName = this.getResources().getText(this.getResources().getIdentifier("app_name", "string", this.getPackageName()));          
             
             File folder = new File(Environment.getExternalStorageDirectory().toString(), appName.toString());
             Log.d(TAG, "path of folder " + folder.getAbsolutePath() + "");
             boolean folderExists = true; //assume true
 
             Log.d(TAG, "path of exist? " + folder.exists() + "");
             if (!folder.exists()) {
                 folderExists = folder.mkdirs();
                 Log.d(TAG, "path of exists " + folderExists + "");
             }
             if (folderExists) {
                 //once folder exists finish creating picturePath
                 long picTime = System.currentTimeMillis();
                String newPicName = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.CANADA).format(picTime);
                 //TODO incorporate unique story ID in picture name
                 picturePath = folder.getAbsolutePath() + "/" + newPicName + ".jpg";
                 File file = new File(picturePath);
                 Log.d(TAG, "path of image preend " + picturePath + "");
                 
                 //then write the picture to picturePath
                 try {
                     OutputStream outFile = new FileOutputStream(file);
                     //TODO decide on quality size and figure out how 
                     //to decrease picture dimensions?
                     bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outFile);
                     outFile.flush();
                     outFile.close();
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 } catch (IOException e) {
                     e.printStackTrace();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 
                 //finish by updating the fragment part
                 if (pictureMode.equals("Add")) {
                     System.out.println("add ill start");
                     fragmentController.addIllustration(fragment, picturePath, position);
                 } else if (pictureMode.equals("Edit")) {
                     fragmentController.deleteFragmentPart(fragment, position);
                     fragmentController.addIllustration(fragment, picturePath, position);
                 }
                 saveFragment();
                 // fragmentPartListView.invalidateViews();
             } else {
                 //unable to create folder
             }
             
             Log.d(TAG, "path of image END" + picturePath + "");
         }
     }
 
     private void setTitleAndPageId() {
 
         String title = editTitleText.getText().toString();
         int idPage = -9;
         // try{
         // idPage = Integer.parseInt(idPageNum);
         // }catch(NumberFormatException e){
         // Log.d("Msg","There was a number format exception!");
         // }
 
         // fragment.setTitle(title);
         // fragment.setId(idPage);
 
         fragmentController.setTitle(fragment, title);
         fragmentController.setId(fragment, idPage);
     }
 
     private void saveFragment() {
         Log.d(TAG, "removing empty");
         
         // make sure fragment does not have any empty parts
         fragmentController.removeEmptyPart(fragment);
         
         Log.d(TAG, "removed empty");
 
         setTitleAndPageId();
         storyController.setFragmentAtLocation(story, fragPos, fragment);
         storyListController.saveOfflineStories(storyList);
     }
 }
