 package com.example.maptracker;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import dataWrappers.DBMarker;
 
 public class MarkerDetails extends Activity {
 
 	//ImageView ivPicture; // -----debug purpose
 	MainActivity ma = new MainActivity();
 	DBMarker theMarker;
 	TextView markerTitleText;
 	TextView markerDateText;
 	Button deletePhoto;
 	Button deleteVideo;
 	Button deleteAudio;
 	ImageButton trashMarker;
 	ImageButton closeMarker;
 	ImageButton pictureButton;
 	ImageButton videoButton;
 	ImageButton audioButton;
 
 	private void initializeMarkerDetailsButtons() {
 		// Video Button
 		deleteVideo = (Button) findViewById(R.id.deleteVideo);
 
 		// Photo Button
 		//ivPicture = (ImageView) findViewById(R.id.ivPicture); // ------debug purpose
 		deletePhoto = (Button) findViewById(R.id.deletePicture);
 
 		// Audio Button
 		deleteAudio = (Button) findViewById(R.id.deleteAudio);
 
 		// Trash Button
 		trashMarker = (ImageButton) findViewById(R.id.trashButton);
 
 		// Close Button
 		closeMarker = (ImageButton) findViewById(R.id.closeButton);
 
 		// Camera Button
 		pictureButton = (ImageButton) findViewById(R.id.pictureButton);
 
 		// Video Button
 		videoButton = (ImageButton) findViewById(R.id.videoButton);
 
 		// Audio Button
 		audioButton = (ImageButton) findViewById(R.id.audioButton);
 	}
 
 	public void passMarker(DBMarker marker){
 		theMarker = marker;
 		initializeMarkerDetailsButtons();
 		initializeMarkerDetailsText();
 		setListeners();
 		setVisible(true);
 	}
 
 	private void initializeMarkerDetailsText() {
 		markerTitleText = (TextView) findViewById(R.id.markerTitle);
 		markerDateText = (TextView) findViewById(R.id.markerDate);
		//TODO 
		markerTitleText.setText("Java Title");
		markerDateText.setText("Java Text");
 	}
 
 	private void setListeners() {
 		setTrashListener();
 		setCloseListener();
 		setPictureListener();
 		//setVideoListener();
 		setAudioListener();
 		setDeleteVideoListener();
 		setDeletePhotoListener();
 		setDeleteAudioListener();
 	}
 
 
 	private void setCloseListener() {
 		// Action for Close button
 		closeMarker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				setVisible(false);
 			}
 		});
 	}
 
 	private void setTrashListener() {
 		// Action for Trash button
 		trashMarker.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				
 			}
 		});
 	}
 
 	//	private void setTrashListener() {
 	//		// Action for Trash button
 	//		trashMarker.setOnClickListener(new OnClickListener() {
 	//
 	//
 	//			@Override
 	//			public void onClick(View v) {
 	//				Intent takePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 	//
 	//				// After photo is taken, pass it to the result method
 	//				startActivityForResult(takePhoto, 0);
 	//			}
 	//		});
 	//	}
 
 	private void setDeleteAudioListener() {
 		// Action for Media button when no data
 		deleteAudio.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				theMarker.audioLink = null;
 			}
 		});
 	}
 
 	private void setDeletePhotoListener() {
 		// Action for Media button when no data
 		deletePhoto.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				theMarker.pictureLink = null;
 			}
 		});
 	}
 
 	private void setDeleteVideoListener() {
 		// Action for Media button when no data
 		deleteVideo.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				theMarker.videoLink = null;
 			}
 		});
 	}
 
 	private void setAudioListener() {
 		// Action for Media button when no data
 		audioButton.setOnClickListener(new OnClickListener() {
 
 
 			@Override
 			public void onClick(View v) {
 				Intent takePhoto = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
 				// After photo is taken, pass it to the result method
 				startActivityForResult(takePhoto, 0);
 			}
 		});
 
 	}
 
 	private void setVideoListener() {
 		// Action for Media button when no data
 		videoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				ma.videoButtonClicked();
 			}
 		});
 	}
 
 	private void setPictureListener() {
 		// Action for Media button when no data
 		videoButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View view) {
 				ma.cameraButtonClicked();
 			}
 		});
 	}
 
 	/**
 	 * Handles media after device application is ran.
 	 * 
 	 * @param requestCode
 	 * @param resultCode
 	 * @param data
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		if (requestCode == 0){
 			Bitmap theImage = (Bitmap) data.getExtras().get("data");
 			System.out.println("Photo was taken");
 			//TODO this is where we pass the information to the db
 			//ivPicture.setImageBitmap(theImage); // ----debug purpose
 
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.marker_details, menu);
 		return true;
 	}
 
 }
