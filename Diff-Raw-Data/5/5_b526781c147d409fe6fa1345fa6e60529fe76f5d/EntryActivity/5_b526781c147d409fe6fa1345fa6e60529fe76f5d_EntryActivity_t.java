 package net.analogyc.wordiary;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.DialogFragment;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import net.analogyc.wordiary.EditEntryDialogFragment.EditEntryDialogListener;
 
 public class EntryActivity extends BaseActivity implements EditEntryDialogListener  {
 	private final int MOOD_RESULT_CODE = 101;
 	private int entryId;
 	private int dayId;
 	private TextView messageText, dateText;
     private ImageView photoButton, moodImage;
 	private Button setNewMoodButton, editEntryButton, deleteEntryButton, shareEntryButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_entry);
 		
 		Intent intent = getIntent();
 		//normally entryId can't be -1
 		entryId = intent.getIntExtra("entryId", entryId);
 
 		messageText = (TextView) findViewById(R.id.messageText);
 		dateText = (TextView) findViewById(R.id.dateText);
         photoButton = (ImageView) findViewById(R.id.photoButton);
         moodImage = (ImageView) findViewById(R.id.moodImage);
 
 		setNewMoodButton = (Button) findViewById(R.id.setNewMoodButton);
 		editEntryButton = (Button) findViewById(R.id.editEntryButton);
 		deleteEntryButton = (Button) findViewById(R.id.deleteEntryButton);
 		shareEntryButton = (Button) findViewById(R.id.shareEntryButton);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == MOOD_RESULT_CODE) {
 			if (resultCode == RESULT_OK) {
 				String moodId = data.getStringExtra("moodId");
 				dataBase.updateMood(entryId, moodId);
 
 			} else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the image capture
 			} else {
 				// Image capture failed, advise user
 			}
 		} else {
 			super.onActivityResult(requestCode, resultCode, data);
 		}
 	}
 	
 	private void setView(){
 		Typeface fontawsm = Typeface.createFromAsset(getAssets(), "fonts/fontawesome-webfont.ttf");
 		setNewMoodButton.setTypeface(fontawsm);
 		editEntryButton.setTypeface(fontawsm);
 		deleteEntryButton.setTypeface(fontawsm);
 		shareEntryButton.setTypeface(fontawsm);
 
 		// we keep this in onResume because the user might have changed the font in Preferences and come back to Entry
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		int typefaceInt = Integer.parseInt(preferences.getString("typeface", "1"));
 		switch (typefaceInt) {
 			case 2:
 				Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/animeace2.ttf");
 				messageText.setTypeface(typeface);
 				break;
 			case 3:
 				Typeface typeface3 = Typeface.createFromAsset(getAssets(), "fonts/stanhand.ttf");
 				messageText.setTypeface(typeface3);
 				break;
 			default:
 				messageText.setTypeface(Typeface.SANS_SERIF);
 		}
 		int fontSize = Integer.parseInt(preferences.getString("font_size", "2"));
 		switch (fontSize) {
 			case 1:
 				messageText.setTextSize(14);
 				break;
 			case 3:
				messageText.setTextSize(24);
 				break;
 			default:
				messageText.setTextSize(18);
 		}
 
   		Cursor c_entry = dataBase.getEntryById(entryId);
   		if (! c_entry.moveToFirst()) {
 			 throw new RuntimeException("Wrong entry id");
   			//error! wrong ID, but it won't happen
   		}
   		String message = c_entry.getString(2);
   		messageText.setText(message);
   		String mood = c_entry.getString(3);
   		if( mood != null){
   			int identifier = getResources().getIdentifier(mood, "drawable", R.class.getPackage().getName());
 			moodImage.setImageResource(identifier);
   		}
   		
   		
   		String d_tmp = c_entry.getString(4);
   		SimpleDateFormat format_in = new SimpleDateFormat("yyyyMMddHHmmss",Locale.ITALY);
   		SimpleDateFormat format_out = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy",Locale.ITALY);
 		try {
 			Date date = format_in.parse(d_tmp);
 			dateText.setText(format_out.format(date)); //probably a better method to do this exists
 		} catch (ParseException e) {
 			//won't happen if we use only dataBaseHelper.addEntry(...)
 		}  
 		int dayId = c_entry.getInt(1);
 		this.dayId = dayId;
         Bitmap image;
         InputStream image_stream;
         try {
             if (dayId != -1) {
 				Cursor c_photo = dataBase.getDayById(dayId);
 				c_photo.moveToFirst();
 				if (!c_photo.getString(1).equals("")) {
 					try {
 
 					bitmapWorker.createTask(photoButton, c_photo.getString(1))
 						.setDefaultBitmap(BitmapFactory.decodeStream(getAssets().open("default-avatar.jpg")))
 						.setTargetHeight(photoButton.getWidth())
 						.setTargetWidth(photoButton.getWidth())
 						.setHighQuality(true)
 						.setCenterCrop(true)
 						.execute();
 						c_photo.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else {
 					image_stream = getAssets().open("default-avatar.jpg");
 					image = BitmapFactory.decodeStream(image_stream);
 					photoButton.setImageBitmap(image);
 				}
 			}
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 		c_entry.close();
 
 
 		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
 		DisplayMetrics dm = new DisplayMetrics();
 		display.getMetrics(dm);
 		photoButton.setMaxWidth(dm.widthPixels);
 		photoButton.setMaxHeight(dm.widthPixels);
 
 	}
 
 	public void onPhotoButtonClicked(View view) {
 		Intent intent = new Intent(this, ImageActivity.class);
 		intent.putExtra("dayId", dayId);
 		startActivity(intent);
 	}
 
 	public void onMoodButtonClicked(View view){
 		Intent intent = new Intent(this, MoodsActivity.class);
 		intent.putExtra("entryId", entryId);
 		startActivityForResult(intent, MOOD_RESULT_CODE);
 	}
 	
 	public void onShareButtonClicked(View view){
 		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 		sharingIntent.setType("text/plain");
 		String shareBody = (String) messageText.getText();
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Wordiary");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
 		startActivity(Intent.createChooser(sharingIntent, "Share via"));
 	}
 	
 	public void onDeleteButtonClicked(View view){
 		dataBase.deleteEntry(entryId);
 		finish();
 	}
 	
 	public void onEditButtonClicked(View view){
 		EditEntryDialogFragment newFragment = new EditEntryDialogFragment();
 		newFragment.show(getSupportFragmentManager(), "modifyEntry");
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState){
 		super.onSaveInstanceState(outState);
 		outState.putInt("entryId", entryId);
 	}
 	
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState){
 		super.onRestoreInstanceState(savedInstanceState);
 		if(savedInstanceState.containsKey("entryId")){
 			entryId = savedInstanceState.getInt("entryId");
 		}
 	}
 	
 	@Override
 	protected void onResume(){
 		super.onResume();
 		setView();
 	}
 	
 	public void onDialogModifyClick(DialogFragment dialog) {
 		Context context = getApplicationContext();
 		CharSequence text;
 		int duration = Toast.LENGTH_SHORT;
 
 		EditText edit=(EditText)dialog.getDialog().findViewById(R.id.newMessage);
 		String message = edit.getText().toString();
 
 		if(message != ""){
 			text = "Message modified";
 			dataBase.updateMessage(entryId, message);
 			setView();
 		} else {
 			text = "Message not modified";
 		}
 
 		Toast toast1 = Toast.makeText(context, text, duration);
 		toast1.show();
 	}
 
 }
