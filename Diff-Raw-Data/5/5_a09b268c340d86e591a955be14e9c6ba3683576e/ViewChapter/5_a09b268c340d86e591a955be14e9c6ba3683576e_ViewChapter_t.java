 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ca.ualberta.cs.c301f13t13.gui;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 import ca.ualberta.cs.c301f13t13.backend.Chapter;
 import ca.ualberta.cs.c301f13t13.backend.Choice;
 import ca.ualberta.cs.c301f13t13.backend.HolderApplication;
 import ca.ualberta.cs.c301f13t13.backend.Media;
 import ca.ualberta.cs.c301f13t13.backend.ObjectType;
 import ca.ualberta.cs.c301f13t13.backend.SHController;
 
 /**
  * Views the chapter provided through the intent. Does not allow going backwards
  * through the activity stack.
  * 
  * @author Alexander Wong
  * 
  */
 public class ViewChapter extends Activity {
 	HolderApplication app;
 	private SHController gc;
 	private GUIMediaUtilities util;
 	private Chapter chapter;
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 	private ArrayList<Media> photoList;
 	private ArrayList<Media> illList;
 	private AdapterChoices choiceAdapter;
 	private AlertDialog photoDialog;
 	private LinearLayout illustrations;
 	private LinearLayout photos;
 
 	private TextView chapterContent;
 	private ListView chapterChoices;
 	private Button addPhotoButton;
 
 	private Uri imageFileUri;
 	public static final int BROWSE_GALLERY_ACTIVITY_REQUEST_CODE = 1;
 	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (HolderApplication) this.getApplication();
 		setContentView(R.layout.activity_view_chapter);
 		setUpFields();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		setNextChapterListener();
 		setAddPhotoListener();
 		updateData();
 	}
 
 	/**
 	 * Initializes the private fields needed.
 	 */
 	public void setUpFields() {
 		gc = SHController.getInstance(this);
 		util = new GUIMediaUtilities();
 
 		// Setup the activity fields
 		chapterContent = (TextView) findViewById(R.id.chapterContent);
 		chapterChoices = (ListView) findViewById(R.id.chapterChoices);
 		addPhotoButton = (Button) findViewById(R.id.addPhotoButton);
 		illustrations = (LinearLayout) findViewById(R.id.horizontalIllustraions);
 		photos = (LinearLayout) findViewById(R.id.horizontalPhotos);
 
 		// Setup the choices and choice adapters
 		choiceAdapter = new AdapterChoices(this, R.layout.browse_choice_item,
 				choices);
 		chapterChoices.setAdapter(choiceAdapter);
 	}
 
 	/**
 	 * Gets the new chapter and updates the view's components.
 	 */
 	public void updateData() {
 		chapter = app.getChapter();
 		choices.clear();
 		// Check for no chapter text
 		if (chapter.getText().equals("")) {
 			chapterContent.setText("<No Chapter Content>");
 		} else {
 			chapterContent.setText(chapter.getText());
 		}
 		// Check for no choices
 		if (chapter.getChoices().isEmpty()) {
 			chapterContent.setText(chapterContent.getText()
 					+ "\n\n<No Choices>");
 		} else {
 			choices.addAll(chapter.getChoices());
 		}
 		choiceAdapter.notifyDataSetChanged();
 
 		photoList = chapter.getPhotos();
 		illList = chapter.getIllustrations();
 
 		photos.removeAllViews();
 		illustrations.removeAllViews();
 		// Insert Photos
 		for (Media photo : photoList) {
 			photos.addView(util.insertImage(photo, this));
 		}
 		// Insert Illustrations
 		for (Media ill : illList) {
 			illustrations.addView(util.insertImage(ill, this));
 		}
 	}
 
 	/**
 	 * Sets up the onClick listener for the button to add a new photo.
 	 */
 	public void setAddPhotoListener() {
 		addPhotoButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				AlertDialog.Builder alert = new AlertDialog.Builder(
 						ViewChapter.this);
 				// Set dialog title
 				alert.setTitle("Choose method:");
 				// Options that user may choose to add photo
 				final String[] methods = { "Take Photo", "Choose from Gallery" };
 				alert.setSingleChoiceItems(methods, -1,
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog, int item) {
 								switch (item) {
 								case 0:
 									takePhoto();
 									break;
 								case 1:
 									browseGallery();
 									break;
 								}
 								photoDialog.dismiss();
 							}
 						});
 				photoDialog = alert.create();
 				photoDialog.show();
 			}
 		});
 	}
 
 	/**
 	 * Sets up the onClick listener for the button to flip to the next chapter
 	 * (selecting a choice).
 	 */
 	public void setNextChapterListener() {
 		chapterChoices.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// Go to the chapter in question
 				Intent intent = new Intent(getBaseContext(), ViewChapter.class);
 				app.setChapter(gc.getCompleteChapter(choices.get(arg2).getNextChapter()));
 				startActivity(intent);
 				photos.removeAllViews();
 				illustrations.removeAllViews();
 				finish();
 			}
 		});
 	}
 
 	/**
 	 * Code for browsing gallery
 	 * 
 	 * CODE REUSE URL:
 	 * http://stackoverflow.com/questions/6016000/how-to-open-phones
 	 * -gallery-through-code
 	 */
 	public void browseGallery() {
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_GET_CONTENT);
 		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
 				BROWSE_GALLERY_ACTIVITY_REQUEST_CODE);
 	}
 
 	public void takePhoto() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		imageFileUri = util.getUri();
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 		startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	/**
 	 * Adds an image into the gallery
 	 */
 	public void insertIntoGallery(Media image) {
 		Intent mediaScanIntent = new Intent(
 				Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 		File f = new File(image.getPath());
 		Uri contentUri = Uri.fromFile(f);
 		mediaScanIntent.setData(contentUri);
 		this.sendBroadcast(mediaScanIntent);
 	}
 
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent intent) {
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				Media photo = new Media(chapter.getId(),
 						imageFileUri.getPath(), Media.PHOTO);
 				gc.addObject(photo, ObjectType.MEDIA);
 				insertIntoGallery(photo);
 			} else if (resultCode == RESULT_CANCELED) {
 				System.out.println("cancelled taking a photo");
 			} else {
 				System.err.println("Error in taking a photo" + resultCode);
 			}
 
 		} else if (requestCode == BROWSE_GALLERY_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 				imageFileUri = intent.getData();
 				String path = util.getRealPathFromURI(imageFileUri, this);
 				Media photo = new Media(chapter.getId(), path, Media.PHOTO);
 				gc.addObject(photo, ObjectType.MEDIA);
 			} else if (resultCode == RESULT_CANCELED) {
 				System.out.println("cancelled taking a photo");
 			} else {
 				System.err.println("Error in taking a photo" + resultCode);
 			}
 		}
 	}
 }
