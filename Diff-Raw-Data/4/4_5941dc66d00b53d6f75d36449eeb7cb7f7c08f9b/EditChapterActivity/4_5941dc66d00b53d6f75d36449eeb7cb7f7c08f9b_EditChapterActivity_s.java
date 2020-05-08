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
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import ca.ualberta.cmput301f13t13.storyhoard.R;
 import ca.ualberta.cs.c301f13t13.backend.Chapter;
 import ca.ualberta.cs.c301f13t13.backend.Choice;
 import ca.ualberta.cs.c301f13t13.backend.ObjectType;
 import ca.ualberta.cs.c301f13t13.backend.SHController;
 import ca.ualberta.cs.c301f13t13.backend.Story;
 
 //import android.view.Menu; *Not sure if needed
 
 /**
  * Add Chapter Activity
  * 
  * Purpose: - To add a chapter to an existing story. - The author can: - Add
  * images through the use of the image button - Set the text of the chapter
  * through the Edit text space - View all chapters in the story upon pressing
  * the 'View All Chapters' button - Add a choice by pressing the 'Add Choice'
  * button. - This activity will also display the choices that exist or have been
  * added.
  * 
  * author: Alexander Wong
  */
 
 public class EditChapterActivity extends Activity {
 
 	private Chapter chapt;
 	private Story story;
 	private ArrayList<Choice> choices = new ArrayList<Choice>();
 	private Button saveButton;
 	private Button addIllust;
 	private Button addChoice;
 	private ListView viewChoices;
 	private EditText chapterContent;
 	private boolean isEditing;
 	private SHController gc;
 	private AdapterChoices choiceAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_edit_chapter);
 
 		chapterContent = (EditText) findViewById(R.id.chapterEditText);
		saveButton = (Button) findViewById(R.id.chapterSave);
 		addChoice = (Button) findViewById(R.id.addNewChoice);
 		viewChoices = (ListView) findViewById(R.id.chapterEditChoices);
 		gc = SHController.getInstance(getBaseContext());
 
 		// Get the story that chapter is being added to
 		Bundle bundle = this.getIntent().getExtras();
 		isEditing = bundle.getBoolean("isEditing");
 		if (isEditing) {
 			story = (Story) bundle.get("Story");
 			chapt = (Chapter) bundle.get("Chapter");
 		} else {
 			story = (Story) bundle.get("New Story");
 			chapt = new Chapter(story.getId(), "");
 		}
 
 		// Setup the adapter
 		choiceAdapter = new AdapterChoices(this, R.layout.browse_choice_item, choices);
 		viewChoices.setAdapter(choiceAdapter);
 		
 		// Save the chapter to the database, or update if editing
 		saveButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				chapt.setText(chapterContent.getText().toString());
 				if (isEditing) {
 					gc.updateObject(chapt, ObjectType.CHAPTER);
 				} else {
 					story.addChapter(chapt);
 					gc.addObject(story, ObjectType.CREATED_STORY);
 					gc.addObject(chapt, ObjectType.CHAPTER);
 				}
 				finish();
 			}
 		});
 		
 		addIllust.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				chapt.addIllustration(null);
 			}
 		});
 
 		// Add a choice to this chapter
 		addChoice.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getBaseContext(),
 						EditChoiceActivity.class);
 				intent.putExtra("chapter", chapt);
 				intent.putExtra("story", story);
 				startActivity(intent);
 			}
 		});
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		// Set the chapter text, if new Chapter will simply be blank
 		chapterContent.setText(chapt.getText());
 		choices.clear();
 		choices.addAll(gc.getAllChoices(chapt.getId()));
 		choiceAdapter.notifyDataSetChanged();
 	}
 
 	// private static int BROWSE_GALLERY_ACTIVITY_REQUEST_CODE = 1;
 	// private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	// Uri imageFileUri;
 	//
 	// @Override
 	// public void onCreate(Bundle savedInstanceState) {
 	// super.onCreate(savedInstanceState);
 	// setContentView(R.layout.activity_main);
 	//
 	// ImageButton button = (ImageButton) findViewById(R.id.TakeAPhoto);
 	// OnClickListener listener = new OnClickListener() {
 	// public void onClick(View v){
 	// takeAPhoto();
 	// }
 	// };
 	// button.setOnClickListener(listener);
 	// }
 	//
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu) {
 	// getMenuInflater().inflate(R.menu.activity_main, menu);
 	// return true;
 	// }
 	//
 	//
 	//
 	// public void takeAPhoto() {
 	// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 	//
 	// String folder =
 	// Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 	// File folderF = new File(folder);
 	// if (!folderF.exists()) {
 	// folderF.mkdir();
 	// }
 	//
 	// String imageFilePath = folder + "/" +
 	// String.valueOf(System.currentTimeMillis()) + "jpg";
 	// File imageFile = new File(imageFilePath);
 	// imageFileUri = Uri.fromFile(imageFile);
 	//
 	// intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 	// startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	// }
 	//
 	// protected void onActivityResult(int requestCode, int resultCode, Intent
 	// data) {
 	// if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 	// TextView tv = (TextView) findViewById(R.id.status);
 	// if (resultCode == RESULT_OK) {
 	// tv.setText("Photo OK!");
 	// ImageButton button = (ImageButton) findViewById(R.id.TakeAPhoto);
 	// button.setImageDrawable(Drawable.createFromPath(imageFileUri.getPath()));
 	// } else if (resultCode == RESULT_CANCELED) {
 	// tv.setText("Photo canceled");
 	// } else {
 	// tv.setText("Not sure what happened!" + resultCode);
 	// }
 	// } else if (requestCode == BROWSE_GALLERY_ACTIVITY_REQUEST_CODE) {
 	// TextView tv = (TextView) findViewById(R.id.status);
 	// if (resultCode == RESULT_OK) {
 	// tv.setText("Photo OK!");
 	// ImageButton button = (ImageButton) findViewById(R.id.TakeAPhoto);
 	// button.setImageDrawable(Drawable.createFromPath(imageFileUri.getPath()));
 	// } else if (resultCode == RESULT_CANCELED) {
 	// tv.setText("Photo canceled");
 	// } else {
 	// tv.setText("Not sure what happened!" + resultCode);
 	// }
 	// }
 	// }
 	//
 	// public void browseGallery() {
 	// Intent intent = new Intent(Intent.ACTION_PICK,
 	// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 	//
 	// String folder =
 	// Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmp";
 	// File folderF = new File(folder);
 	// if (!folderF.exists()) {
 	// folderF.mkdir();
 	// }
 	//
 	// String imageFilePath = folder + "/" +
 	// String.valueOf(System.currentTimeMillis()) + "jpg";
 	// File imageFile = new File(imageFilePath);
 	// imageFileUri = Uri.fromFile(imageFile);
 	//
 	// intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 	//
 	// startActivityForResult(intent, BROWSE_GALLERY_ACTIVITY_REQUEST_CODE);
 	//
 	// }
 }
