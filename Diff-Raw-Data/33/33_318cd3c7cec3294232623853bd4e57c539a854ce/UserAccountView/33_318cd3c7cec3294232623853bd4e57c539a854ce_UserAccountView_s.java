 package bu.edu.cs673.edukid.settings.useraccount;
 
import java.io.FileNotFoundException;
import java.io.InputStream;
 import java.util.List;

 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.Toast;
 import bu.edu.cs673.edukid.R;
 import bu.edu.cs673.edukid.db.Database;
 import bu.edu.cs673.edukid.db.ImageUtils;
 import bu.edu.cs673.edukid.db.model.UserAccount;
 import bu.edu.cs673.edukid.voicerecord.RecordUtility;
 
 /**
  * The view which contains the user account information. The user account can be
  * seen and edited here.
  * 
  * @author Peter Trevino
  * 
  * @see UserAccount
  * 
  */
 public class UserAccountView extends Activity implements OnClickListener {
 
 	private static final int TAKE_PICTURE = 1888;
 	
 	private static final int SELECT_PHOTO = 100;
 
 	private static final long DATABASE_ERROR = -1;
 
 	private EditText userName;
 
 	private ImageView userImage;
 
 	private ImageView micImage;
 
 	private ImageView playImage;
 	
 	private ImageView selectPhotoImage;
 
 	private Database database = Database.getInstance(this);
 
 	private boolean recording = false;
 
 	private String savedFilePath;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.user_account);
 		userImage = (ImageView) findViewById(R.id.createUserImage);
 		micImage = (ImageView) findViewById(R.id.accountCreationRecorderButton);
 		playImage = (ImageView) findViewById(R.id.playAudioButton);
 		selectPhotoImage = (ImageView) findViewById(R.id.AddPhotoButton);
 		userName = (EditText) findViewById(R.id.createEditChildName);
 		
 
 		// Populate user account info from database (if any)
 		List<UserAccount> userAccounts = database.getUserAccounts();
 
 		if (userAccounts.size() == 1) {
 			UserAccount userAccount = userAccounts.get(0);
 
 			// Set user name
 			
 			userName.setText(userAccount.getUserName());
 
 			// Set user image
 			userImage.setImageDrawable(ImageUtils
 					.byteArrayToDrawable(userAccount.getUserImage()));
 
 			userImage.setMaxHeight(400);
 			userImage.setMaxWidth(400);
 			userImage.setMinimumHeight(400);
 			userImage.setMinimumWidth(400);
 			userImage.setAdjustViewBounds(false);
 			//set user sound
 			savedFilePath=userAccount.getUserSound();
 
 		}
 		if(savedFilePath==null||savedFilePath.isEmpty()){
 			playImage.setBackgroundResource(R.drawable.greyplaybutton);
 			playImage.setEnabled(false);
 		}
 		// Add listeners
 		Button createSaveButton = (Button) findViewById(R.id.createSaveButton);
 		createSaveButton.setOnClickListener(this);
 		ImageButton createUploadPhotoButton = (ImageButton) findViewById(R.id.createUploadPhotoButton);
 		createUploadPhotoButton.setOnClickListener(this);
 		micImage.setOnClickListener(this);
 		playImage.setOnClickListener(this);
 		selectPhotoImage.setOnClickListener(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onClick(View view) {
 		switch (view.getId()) {
 		case R.id.createSaveButton:
 			if(!userName.getText().toString().isEmpty())
 				saveUserAccount();
 			else
 				Toast.makeText(this, "Please enter a Child's Name to save!",
 						Toast.LENGTH_LONG).show();
 			break;
 		case R.id.createUploadPhotoButton:
 			// TODO: we should have other options other than the camera like
 			// picking from the camera roll
 			startCamera();
 			break;
 		case R.id.accountCreationRecorderButton:
 			if (recording) {
 				RecordUtility.stopRecording(micImage);
 				playImage.setBackgroundResource(R.drawable.playbutton);
 				playImage.setEnabled(true);
 			} else {
 				savedFilePath = RecordUtility.startRecording("UserAccount",
 						micImage);
 			}
 
 			recording = !recording;
 			break;
 		case R.id.playAudioButton:
 			if(savedFilePath==null)
 				Toast.makeText(this, "No Audio Recorded for user, please use the record button to record the childs name",
 						Toast.LENGTH_LONG).show();
 			else{
 				RecordUtility.playbackRecording(savedFilePath);
 			}
 
 		case R.id.AddPhotoButton:
 			selectPhoto();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {
 			Bitmap photo = (Bitmap) data.getExtras().get("data");
 
 			if (photo != null) {
 				userImage.setImageBitmap(photo);
 			}
 		}
 		else if(requestCode == SELECT_PHOTO&&resultCode == RESULT_OK){
 			Uri selectedImage = data.getData();
            InputStream imageStream = null;
			try {
				imageStream = getContentResolver().openInputStream(selectedImage);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
            Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
             userImage.setImageBitmap(yourSelectedImage);
 		}
 	}
 
 	/**
 	 * Saves the user account in the database.
 	 */
 	private void saveUserAccount() {
 		String stUserName= userName.getText().toString();
 		List<UserAccount> userAccounts = database.getUserAccounts();
 		long result = DATABASE_ERROR;
 
 		if (userAccounts.size() == 0) {
 			result = database.addUserAccount(stUserName, savedFilePath,
 					userImage.getDrawable());
 		} else if (userAccounts.size() == 1) {
 			UserAccount userAccount = userAccounts.get(0);
 			userAccount.setUserName(stUserName);
 			userAccount.setUserImage(ImageUtils.drawableToByteArray(userImage
 					.getDrawable()));
 			userAccount.setUserSound(savedFilePath);
 			result = database.editUserAccount(userAccount);
 		} else {
 			// TODO: implement more than 1 user. Should not get here now.
 		}
 
 		if (result != DATABASE_ERROR) {
 			Toast.makeText(this, "User account saved successfully!",
 					Toast.LENGTH_LONG).show();
 		} else {
 			// TODO: inform user of error
 		}
 	}
 
 	/**
 	 * Starts the front facing camera to take a picture.
 	 */
 	private void startCamera() {
 		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 		startActivityForResult(intent, TAKE_PICTURE);
 	}
 	private void selectPhoto(){
 		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 		photoPickerIntent.setType("image/*");
 		startActivityForResult(photoPickerIntent, SELECT_PHOTO);    
 	}
 }
