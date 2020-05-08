 package com.cs301w01.meatload.activities;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.view.View;
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.controllers.PictureManager;
 import com.cs301w01.meatload.model.Picture;
 
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Takes a picture and displays it in exploded view along with important metadata including
  * tags, date, etc.
  * <p>
  * Gives the user an exploded view of the picture being edited.
  * <p>
  * Allows user to change certain metadata such as tags and album.
  * @author Blake Bouchard
  */
 public class EditPictureActivity extends Skindactivity {
 
 	private PictureManager pictureManager;
 	private Picture picture;
 
 	@Override
 	public void update(Object model) {
 		
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.edit_picture);
 		
 		// Get picture object from Intent's extras bundle
 		Intent intent = getIntent();
 		Bundle extras = intent.getExtras();
 		picture = (Picture) extras.getSerializable("picture");
         
 		// Set up a new PictureManager using the Picture object passed via the intent 
 		pictureManager = new PictureManager(this, picture);
 
 		populateTextFields(picture.getAlbumName(),
                 picture.getDate().toString(),
                 picture.getPath());
 
 		createListeners();
 	}
 	
 	@Override
     protected void onResume() {
     	super.onResume();
     	pictureManager.setContext(this);
     }
 	
 	/**
 	 * Fills the text and image fields on the screen with a current picture.
 	 */
 	protected void populateTextFields(String albumName, String date, String path) {
 		// Set pictureView to path provided by Picture object
 		ImageView pictureView = (ImageView) findViewById(R.id.pictureView);
 		pictureView.setImageDrawable(Drawable.createFromPath(path));
 		
 		// Set dateView to toString representation of Date in Picture object
 		TextView dateView = (TextView) findViewById(R.id.dateView);
 		dateView.setText(date);
 		
 		// Set albumView to string representation of Album in Picture object
 		TextView albumView = (TextView) findViewById(R.id.albumView);
 		albumView.setText(albumName);
 	}
 	
 	protected void createListeners() {
 		Button changeAlbumButton = (Button) findViewById(R.id.changeAlbumButton);
 		// TODO: Add Change Album functionality to EditPicture
 		changeAlbumButton.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View view) {
				openChangeAlbumDialog();
 			}
 		});
 		
 		// TODO: Add Edit Tags functionality to EditPicture
         Button sendEmailButton = (Button) findViewById(R.id.sendEmailButton);
         
         sendEmailButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 openSendEmailActivity();
             }
         });
 	}
 	
 	/**
 	 * Opens a dialog that will allow the user to select an album from a list of album names,
 	 * which will move the selected picture to that album.
 	 */
 	private void openChangeAlbumDialog() {
 		Dialog dialog = new Dialog(this);
 		dialog.setContentView(R.layout.change_album);
 	}
     
     private void openSendEmailActivity() {
         
         Intent sendEmail = new Intent();
         sendEmail.setClassName("com.cs301w01.meatload", "com.cs301w01.meatload.activities.SendEmailActivity");
         //sendEmail.putExtra("picture", pictureManager.getPicture());
         
         startActivity(sendEmail);
 
     }
     
 }
