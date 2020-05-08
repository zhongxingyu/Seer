 package com.jessitron.tronsmit;
 
 import static com.jessitron.tronsmit.database.Button.ButtonConfig;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.ApplicationInfo;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.gesture.Gesture;
 import android.gesture.GestureLibraries;
 import android.gesture.GestureLibrary;
 import android.gesture.GestureOverlayView;
 import android.gesture.Prediction;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class TronsmitActivity extends Activity {
     private static final int REQUEST_CODE_PICK_CONTACT = 1;
     private static final int REQUEST_CODE_CHOOSE_INTENT = 2;
     private static final int REQUEST_CODE_TAKE_PICTURE = 3;
     private static final int REQUEST_CODE_PICK_IMAGE = 4;
     public static final String LOG_PREFIX = "TronsmitActivity";
     public static final int PACKAGE_MANAGER_GET_INFO_FLAGS = PackageManager.GET_ACTIVITIES
             | PackageManager.GET_INTENT_FILTERS
             | PackageManager.GET_CONFIGURATIONS
             | PackageManager.GET_META_DATA;
 
     private PictureManager pictureManager;
 
     private static final View.OnLongClickListener BUTTON_DELETING_LISTENER = new View.OnLongClickListener() {
         @Override
         public boolean onLongClick(View view) {
             ((ViewGroup) view.getParent()).removeView(view);
             return true;
         }
     };
     private Destination destination;
     private SendIntentCreator sendIntentCreator;
     private com.jessitron.tronsmit.database.Button.Helper buttonHelper;
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         say("onCreate");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         loadPreferences();
         loadGestures();
 
         pictureManager = new PictureManager((ImageView) findViewById(R.id.pictureView), getApplicationContext());
         pictureManager.reset();
         if (!pictureManager.hasPicture()) {
             disableAllButtons();
             toast("This app is useless without pictures");
         }
 
         sendIntentCreator = new SendIntentCreator(getString(R.string.attribution), pictureManager);
         buttonHelper = new com.jessitron.tronsmit.database.Button.Helper((TronsmitApplication) getApplicationContext());
 
         createButtons();
     }
 
     private void createButtons() {
         for (ButtonConfig buttonConfig : buttonHelper.getButtons()) {
             addButtonFor(createIntentFrom(buttonConfig.component), buttonConfig.destination);
         }
     }
 
     private Intent createIntentFrom(ComponentName component) {
         final Intent intent = new Intent();
         intent.setComponent(component);
         return intent;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         say("onResume called");
 
     }
 
     private boolean isServiceSupported(Intent intent) {
         List<ResolveInfo> result = getPackageManager().queryIntentServices(intent, 0);
         return (result != null && !result.isEmpty());
     }
 
     private boolean isActivitySupported(Intent intent) {
         List<ResolveInfo> result = getPackageManager().queryIntentActivities(intent,
                 PackageManager.MATCH_DEFAULT_ONLY
                         | PackageManager.GET_RESOLVED_FILTER
                         | PackageManager.GET_INTENT_FILTERS);
         return (result != null && !result.isEmpty());
     }
 
     private Intent createEditImageIntent() {
         Intent editIntent = new Intent(Intent.ACTION_EDIT, pictureManager.getImageLocation());
         editIntent.setType(pictureManager.getImageType());
         return editIntent;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         setMenuItemEnablement(menu, R.id.flashy, isServiceSupported(createFlashlightIntent()));
         setMenuItemEnablement(menu, R.id.editpic, isActivitySupported(createEditImageIntent()));
         return super.onPrepareOptionsMenu(menu);
     }
 
 
     private Intent createSendIntent(Destination destination) {
 
         return sendIntentCreator.createSendIntent(destination);
     }
 
     private static class SendIntentCreator {
 
         private final PictureKnowerAbouter picInfo;
         private final CharSequence attributionString;
 
         private SendIntentCreator(CharSequence attributionString, PictureKnowerAbouter picInfo) {
             this.picInfo = picInfo;
             this.attributionString = attributionString;
         }
 
         private Intent createSendIntent(Destination destination) {
             Intent shareIntent = new Intent(Intent.ACTION_SEND);
             shareIntent.setType(picInfo.getImageType());
             shareIntent.putExtra(Intent.EXTRA_STREAM, picInfo.getImageLocation());
 
             if (destination.getPhoneNumber() != null) {
                 shareIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, destination.getPhoneNumber());
                 shareIntent.putExtra("address", destination.getPhoneNumber());
             }
             shareIntent.putExtra("sms_body", attributionString);
             shareIntent.putExtra(Intent.EXTRA_TEXT, attributionString);
 
             if (destination.getEmail() != null) {
                 shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{destination.getEmail()});
             }
             shareIntent.putExtra(Intent.EXTRA_SUBJECT, attributionString);
 
             return shareIntent;
         }
     }
 
     public void tronsmit(View v) {
         Intent shareIntent = createSendIntent(destination);
 
         startActivity(shareIntent);
     }
 
     public void pickContact(View v) {
 
         final Intent pickContactsIntent =
                 new Intent(
                         Intent.ACTION_PICK,
                         ContactsContract.Contacts.CONTENT_URI);
         startActivityForResult(
                 pickContactsIntent,
                 REQUEST_CODE_PICK_CONTACT);
 
     }
 
     public void chooseAction(View v) {
 
         final Intent pickActivityIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
         pickActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         pickActivityIntent.putExtra(Intent.EXTRA_INTENT, createSendIntent(destination));
         pickActivityIntent.putExtra(Intent.EXTRA_TITLE, "what should this button do?");
 
         startActivityForResult(pickActivityIntent, REQUEST_CODE_CHOOSE_INTENT);
     }
 
     @Override
     protected void onActivityResult(int requestCode,
                                     int resultCode,
                                     final Intent data) {
 
         if (requestCode == REQUEST_CODE_PICK_CONTACT
                 && resultCode == RESULT_OK) {
             gotAContact(data.getData());
             savePreferences(data.getData());
 
         } else if (requestCode == REQUEST_CODE_CHOOSE_INTENT && resultCode == RESULT_OK) {
             gotAnAction(data, destination);
         } else if (requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == RESULT_OK) {
             pictureManager.reset(); // find the new picture
         } else if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
             gotAnImage(data.getData());
         }
     }
 
     private void gotAnImage(Uri data) {
         pictureManager.useThisOne(data);
     }
 
     private void gotAnAction(final Intent data, Destination destination) {
         // note: this might not be a default action. could be trouble.
 
         addButtonFor(data, destination);
 
         addToSavedButtonConfiguration(data.getComponent());
     }
 
     private void addButtonFor(Intent data, Destination destination) {
         final Button newButton = new Button(this);
         newButton.setOnClickListener(new StartActivityLike(this, sendIntentCreator, data.getComponent(), destination));
         newButton.setText("Send to " + destination.getName() + " by " + getLabel(data));
         newButton.setOnLongClickListener(BUTTON_DELETING_LISTENER);
 
         findButtonContainer().addView(newButton);
     }
 
     private CharSequence getLabel(Intent data) {
         ActivityInfo info = data.resolveActivityInfo(getPackageManager(), 0);
         return info.loadLabel(getPackageManager());
     }
 
     private static class StartActivityLike implements View.OnClickListener {
         private final Context c;
         private final SendIntentCreator sendIntentCreator;
         private final ComponentName component;
         private final Destination destination;
 
         private StartActivityLike(Context c, SendIntentCreator sendIntentCreator, ComponentName component, Destination destination) {
             this.c = c;
             this.sendIntentCreator = sendIntentCreator;
             this.component = component;
             this.destination = destination;
         }
 
         @Override
         public void onClick(View view) {
             startActivityLike(destination, component);
         }
 
         private void startActivityLike(final Destination destination, ComponentName dataComponent) {
             Intent send = sendIntentCreator.createSendIntent(destination);
             send.setComponent(dataComponent);
             c.startActivity(send);
         }
     }
 
     private void addToSavedButtonConfiguration(ComponentName component) {
         buttonHelper.store(component, destination);
     }
 
 
     private void editPicture() {
         startActivity(createEditImageIntent());
     }
 
     private void pickArbitraryImage() {
         Intent pickIntent = new Intent(Intent.ACTION_PICK);
         pickIntent.setType("image/*");
         final ResolveInfo resolveInfo = getPackageManager().resolveActivity(pickIntent, PackageManager.MATCH_DEFAULT_ONLY);
         startActivityForResult(pickIntent, REQUEST_CODE_PICK_IMAGE);
     }
 
     private void takePicture() {
         Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         startActivityForResult(pictureIntent, REQUEST_CODE_TAKE_PICTURE);
     }
 
     private void flashSomeLights() {
         Intent intent = createFlashlightIntent();
         startService(intent);
     }
 
     private Intent createFlashlightIntent() {
         Intent intent = new Intent("com.teslacoilsw.intent.FLASHLIGHT");
         intent.putExtra("strobe", 10);
         intent.putExtra("timeout", 5);
         return intent;
     }
 
     private void dial() {
         Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + destination.getPhoneNumber()));
         dialIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         startActivity(dialIntent);
     }
 
     private void gotAContact(android.net.Uri uri) {
 
         destination = new Destination(getContentResolver(), uri);
 
         updateContactDescription();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.printstuff:
                 printStuff();
                 return true;
             case R.id.camera:
                 takePicture();
                 return true;
             case R.id.editpic:
                 editPicture();
                 return true;
             case R.id.delete:
                 deletePicture();
                 return true;
             case R.id.reset:
                 resetButtons();
                 return true;
             case R.id.choosepic:
                 pickArbitraryImage();
                 return true;
             case R.id.flashy:
                 flashSomeLights();
                 return true;
             case R.id.dial:
                 dial();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void deletePicture() {
         showDialog(0);
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         return createConfirmationDialog("Delete this picture?", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 pictureManager.delete();
             }
         });
     }
 
     private AlertDialog createConfirmationDialog(String message, DialogInterface.OnClickListener yesAction) {
         return new AlertDialog.Builder(this).setMessage(message)
                 .setPositiveButton("Yes", yesAction)
                 .setNegativeButton("No", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.cancel();
                     }
                 }).create();
     }
 
     private void resetButtons() {
         findButtonContainer().removeAllViews();
         buttonHelper.deleteAll();
         pictureManager.reset();
     }
 
 
     private LinearLayout findButtonContainer() {
         return (LinearLayout) findViewById(R.id.buttonContainer);
     }
 
     private void say(String s) {
         Log.d(LOG_PREFIX, "JessiTRON! " + s);
     }
 
     private void toast(String text) {
         Toast.makeText(this, text, Toast.LENGTH_LONG).show();
     }
 
     private void printStuff() {
         List<ResolveInfo> result = getPackageManager().queryIntentActivityOptions(null, null, createSendIntent(destination),
                 PackageManager.MATCH_DEFAULT_ONLY | PACKAGE_MANAGER_GET_INFO_FLAGS | PackageManager.GET_RESOLVED_FILTER);
 
         say("Activities available for send: -----------------------  ");
         for (ResolveInfo resolveInfo : result) {
             say("Activity option: " + resolveInfo.toString());
             say("  " + resolveInfo.activityInfo.toString());
             say("  " + resolveInfo.activityInfo.packageName);
             say("  " + resolveInfo.activityInfo.name);
             say("  " + resolveInfo.filter);
             say("  " + resolveInfo.loadLabel(getPackageManager()));
         }
 
         printInfoAboutAllApplications();
         printInfoAboutAllPackages();
 
     }
 
 
     private void printInfoAboutAllApplications() {
         say("========================");
 
         for (ApplicationInfo applicationInfo : getPackageManager().getInstalledApplications(PACKAGE_MANAGER_GET_INFO_FLAGS)) {
             say("Application info: " + applicationInfo);
             say(applicationInfo.packageName);
         }
     }
 
     private void printInfoAboutAllPackages() { // This is more useful than the applications. PackageInfo has an ApplicationInfo, and it has a list of the activities.
         say("========================");
 
         final List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(
                 PackageManager.GET_ACTIVITIES
                         | PackageManager.GET_INTENT_FILTERS
                         | PackageManager.GET_CONFIGURATIONS
                         | PackageManager.GET_META_DATA);
 
         for (PackageInfo packageInfo : installedPackages) {
             say("Package info: " + packageInfo);
             say(packageInfo.packageName);
             if (packageInfo.activities != null) {
                 for (ActivityInfo activityInfo : packageInfo.activities) {
                     say("  " + activityInfo.name);
                     say("  " + activityInfo.loadLabel(getPackageManager()));
                 }
             }
         }
     }
 
 
     @Override
     protected void onStop() {
         say("onStop");
         super.onStop();
     }
 
     @Override
     protected void onDestroy() {
         pictureManager.shutDown();
         say("onDestroy");
         super.onDestroy();
     }
 
     @Override
     protected void onPause() {
         say("onPause");
         super.onPause();
     }
 
     @Override
     protected void onUserLeaveHint() {
         say("onUserLeaveHint");
         super.onUserLeaveHint();
     }
 
     @Override
     protected void onPostResume() {
         say("onPostResume");
         super.onPostResume();
     }
 
     @Override
     protected void onRestart() {
         say("onRestart");
         super.onRestart();
     }
 
     @Override
     protected void onStart() {
         say("onStart");
         super.onStart();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         say("onConfigChanged");
         super.onConfigurationChanged(newConfig);
     }
 
     @Override
     public void onBackPressed() {
         say("onBackPressed");
         super.onBackPressed();
     }
 
     private void disableAllButtons() {
         LinearLayout container = findButtonContainer();
         for (int i = 0; i < container.getChildCount(); i++) {
             if (container.getChildAt(i) instanceof Button) {
                 container.getChildAt(i).setEnabled(false);
             }
         }
     }
 
 
     private void setMenuItemEnablement(Menu menu, int menuItemId, boolean enabled) {
         for (int i = 0; i < menu.size(); i++) {
             if (menu.getItem(i).getItemId() == menuItemId) {
                 menu.getItem(i).setEnabled(enabled);
             }
         }
     }
 
     private void savePreferences(Uri contactUri) {
         getPreferences(MODE_PRIVATE).edit().putString("contactUri", contactUri.toString()).apply();
     }
 
     private void loadPreferences() {
         String uriString = getPreferences(MODE_PRIVATE).getString("contactUri", "");
         if ("" != uriString) {
             gotAContact(Uri.parse(uriString));
         }
        updateContactDescription();
     }
 
     private void updateContactDescription() {
         TextView contactDescription = (TextView) findViewById(R.id.contactName);
         contactDescription.setText(destination.getName());
        contactDescription.invalidate();
     }
 
 
     private void loadGestures() {
         final GestureLibrary gestureLibrary = GestureLibraries.fromRawResource(getApplicationContext(), getResources().getIdentifier("raw/gestures", null, getPackageName()));
         if (!gestureLibrary.load()) {
             Toast.makeText(this, "Warning: unable to load gestures", Toast.LENGTH_LONG).show();
             return;
         }
 
         GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
         gestures.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
             @Override
             public void onGesturePerformed(GestureOverlayView gestureOverlayView, Gesture gesture) {
                 ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
                 if (predictions.size() > 0) {
                     Prediction prediction = predictions.get(0);
                     if (prediction.score > 1.0) {
                         if ("older".equals(prediction.name)) {
                             pictureManager.older();
                         } else if ("newer".equals(prediction.name)) {
                             pictureManager.newer();
                         } else if ("tronsmit".equals(prediction.name)) {
                             tronsmit(null);
                         }
                     }
                 }
             }
         });
     }
 
 }
