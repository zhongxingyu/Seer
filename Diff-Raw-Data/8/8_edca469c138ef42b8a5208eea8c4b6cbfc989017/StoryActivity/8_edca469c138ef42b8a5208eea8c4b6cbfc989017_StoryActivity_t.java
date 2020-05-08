 /*
 Adventure App - Allows you to create an Adventure Book, or Download
 	books from other authors.
 Copyright (C) Fall 2013 Team 5 CMPUT 301 University of Alberta
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.uofa.adventure_app.activity;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import java.util.Random;
 
 import java.util.SortedMap;
 import java.util.UUID;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.application.AdventureApplication;
 import com.uofa.adventure_app.controller.LocalStorageController;
 import com.uofa.adventure_app.controller.http.HttpObjectStory;
 import com.uofa.adventure_app.interfaces.AdventureActivity;
 import com.uofa.adventure_app.model.Choice;
 import com.uofa.adventure_app.model.Fragement;
 import com.uofa.adventure_app.model.Media;
 import com.uofa.adventure_app.model.Story;
 
 public class StoryActivity extends AdventureActivity {
 	TextView tileTextView;
 	TextView authorTextView;
 	TextView bodyTextView;
 	ImageView imageView; // for displaying an image, duh!
 	String choice;
 	View currentView;
 	Uri imageFileUri;
 	Uri chosenImageUri;
 	Story currentStory;
 	Fragement currentFragement = null;
 	LocalStorageController localStorageController = null;
 	private static final int PICK_IMAGE = 1111; 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		UUID storyID = null;
 		setContentView(R.layout.activity_story);
 		tileTextView = (TextView) findViewById(R.id.titleview);
 		authorTextView = (TextView) findViewById(R.id.authorview);
 		bodyTextView = (TextView) findViewById(R.id.storyview);
 		imageView = (ImageView) findViewById(R.id.annotation); // YOU ARE HERE -- JOEL
 
 		currentStory = AdventureApplication.getStoryController().currentStory();
 		
 		currentFragement = AdventureApplication.getStoryController().currentFragement();
 		bodyTextView.setText(currentFragement.body());
 		ArrayList<Media> media = currentFragement.media();
 		//imageView.setImageDrawable(Drawable.createFromPath(media.get(media.firstKey()).path()));
 
 		bodyTextView.setText(currentFragement.body());
 		authorTextView.setText("");
 		tileTextView.setText(currentFragement.getTitle());
 
 		currentStory.setIsLocal(true);
 		
 		AdventureApplication.getStoryController().replaceStory(currentStory);
 		AdventureApplication.getActivityController().update();
 			
 		currentView = this.findViewById(android.R.id.content);	
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.story, menu);
 		return true;
 	}
 
 	// We want to create a context Menu when the user long clicks on an item
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		//
 		if (!choice.equals("CHOICE")){
 			AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;	
 
 			// Style our context menu
 			menu.setHeaderIcon(android.R.drawable.ic_input_get);
 			menu.setHeaderTitle("Annotate");
 			MenuInflater inflater1 = getMenuInflater();
 
 			// Open Menu
 			inflater1.inflate(R.menu.annotatemenu, menu);
 		}else{
 			
 			AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;
 			//menu.clearHeader();
 			menu.clearHeader();
 			menu.clear();
 			//menu.removeGroup(R.id.annotategroup);
 			// Style our context menu
 			menu.setHeaderIcon(android.R.drawable.ic_input_get);
 			menu.setHeaderTitle("Choices");
 			int counter = 0;
 			for(Choice f : currentFragement.choices()) {
 				menu.add(0, counter, 0, f.getChoice().getTitle());
 				counter++;
 			}
 			if (currentFragement.getRandomflag()){
				menu.add(1, counter, 0, "Random Choice");
 			}
 
 			MenuInflater inflater2 = getMenuInflater();
 			inflater2.inflate(R.menu.choices, menu);
 			
 		}
 		
 	}
 	
 	private void adjustCurrentFragement(Fragement f) {
 		AdventureApplication.getStoryController().addPreviousFragement(currentFragement);
 		AdventureApplication.getStoryController().setCurrentFragement(f);
 		currentFragement = f;
 	}
 	
 	public void openFragement(Fragement f) {
 		adjustCurrentFragement(f);
 		tileTextView.setText(currentFragement.getTitle());
 		bodyTextView.setText(currentFragement.body());
 	}
 	
 	/**
 	 * Called to open the context view that allows the user to open
 	 * the camera or browse their own pictures.
 	 * 
 	 * @param View v
 	 */
 	public void openAnnotateContext(View v) {
 		choice = "Annotate";
 		registerForContextMenu( v );
         openContextMenu( v );  
 	}
 	/**
 	 *  Opens a context view with a list of choices for the user to 
 	 *  move on in the story.
 	 * @param View v
 	 */
 	public void openChoices(View v) {
 		choice = "CHOICE";
 		registerForContextMenu( v );
         openContextMenu( v );  
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle presses on the action bar items
 		
 		
 		switch (item.getItemId()) {
 			case R.id.editstory:
 				editStory();
 				break;
 			case R.id.annotatem:	
 				openAnnotateContext(currentView);
 				//takeAPhoto();
 				break;
 			case R.id.editfragment:
 				editFragment();
 				break;
 			case R.id.copy:
 					copy();
 				break;
 			case R.id.quit:
 				browseView();
 				break;
 			case R.id.publish:
 				publish();
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
		if(item.getGroupId() == 0 && item.getItemId() != R.id.cancel && item.getGroupId() != 1) {
 			// TODO: This needs to be refactored....
 			Fragement frag = AdventureApplication.getStoryController().currentFragement().choices().get(item.getItemId()).getChoice();
 			openFragement(frag);
 			
 		} else 
			if (item.getGroupId() == 1){
 			Random rand = new Random();
 			int size = AdventureApplication.getStoryController().currentFragement().choices().size();
 			int  n = rand.nextInt(size - 1);
 			openFragement(AdventureApplication.getStoryController().currentFragement().choices().get(n).getChoice());
 			
 		} else{
 		
 		switch (item.getItemId()) {
 		case R.id.takepic:
 			takeAPhoto();
 			break;
 		case R.id.choosemedia:
 			chooseImage();
 			break;
 		default:
 
 			return super.onContextItemSelected(item);
 	}
 		}
 	return super.onContextItemSelected(item);
 		
 	}
 	/**
 	 * changes the screen to the Edit Story Screen
 	 */
 	public void editStory() {
 		Intent myIntent = new Intent(this, EditStoryActivity.class);
 		this.startActivity(myIntent);
 	}
 	/**
 	 * Changes the screen to the Edit Fragment Screen.
 	 */
 	public void editFragment(){
 		Intent myIntent = new Intent(this, EditFragementActivity.class);
 		this.startActivity(myIntent);
 	}
 	/**
 	 * CHanges the screen to the Browse View
 	 */
 	public void browseView(){
 		Intent myIntent = new Intent(this, BrowserActivity.class);
 		this.startActivity(myIntent);
 	}
 
 	 private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	    /**
 	     * Opens the camera and allows the user to take a picture if they so choose
 	     */
 	 public void takeAPhoto() {
 		 Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 	        
 	     String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adventure_App/Images";
 	     File folderF = new File(folder);
 	     
 	     if (!folderF.exists()) {
 	            folderF.mkdir();
 	     }
 	        
 	     String imageFilePath = folder + "/" + "Adventure_App" + String.valueOf(System.currentTimeMillis()) + "jpg";
 	     File imageFile = new File(imageFilePath);
 	     imageFileUri = Uri.fromFile(imageFile);
 	        
 	     intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);
 	     startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	 }
 	 
 	    /**
 	     * Method for choosing media, invoked via context menu Annotate. 
 	     * Author - Joel Malina
 	     */
 	    public void chooseImage()
 	    {
 	    	Intent pickImage = new Intent();
 	    	pickImage.setType("image/*");
 	    	pickImage.setAction(Intent.ACTION_GET_CONTENT);
 	    	startActivityForResult(Intent.createChooser(pickImage, "Select Picture"), PICK_IMAGE);
 	    }
 	    // handles takeAPhoto return
 	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 	        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 	           //TextView tv = (TextView) findViewById(R.id.status);
 	            if (resultCode == RESULT_OK) {
 	                // System.out.println("Photo OK!");
 	            	// putting photo into imageview
 	                ImageView annotation = (ImageView) findViewById(R.id.annotation);
 	                annotation.setImageDrawable(Drawable.createFromPath(imageFileUri.getPath()));
 	                
 	                // adding path & fragment the image belongs to into local Database
 	                //LocalStorageController lts = new LocalStorageController(this);
 	                String imageId = UUID.randomUUID().toString();
 	                
 	                // make sure things are safe incrementally (can take out later)
 	                assert(currentStory != null);
 	                assert(currentStory.startFragement() != null);
 	                assert(currentStory.startFragement().uid() != null);
 	                assert(currentFragement.uid().toString() != null);
 	                
 	                // currentStory.startFragement().uid().toString() // another option that should technically get the same thing in this one case
 	                localStorageController.insertImage( imageId, imageFileUri.getPath().toString(), 0, currentFragement.uid().toString());
 	                
 	               // localStorageController.getImage(currentFragement.uid().toString());
 	                
 	            } else if (resultCode == RESULT_CANCELED) {
 	                System.out.println("Photo canceled");
 	            } else {
 	                System.out.println("Not sure what happened!" + resultCode);
 	            }
 	        }
 	        
 	        // handles selecting an image from app of users choice (usually gallery).
 	        if ((requestCode == PICK_IMAGE) && (resultCode == RESULT_OK) && (data != null))
 	        {
 	            chosenImageUri = data.getData();
 	           // chosenImageUri
 	            ImageView annotation = (ImageView) findViewById(R.id.annotation);
                 annotation.setImageURI(chosenImageUri);
 	          
 	            String path = chosenImageUri.getPath();
 	            System.out.println("SO I THINK I MADE CHOOSING MEDIA WORK BUT TO MAKE SURE I NEED TO SEE WHERE IT'S AT PATH: " + path + "/</,?<?,?,?<?,/,/,/,/,?<?<");
 	          //  Toast.MakeText(this, path, ToastLength.Long);
 	            
 	            String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Adventure_App/Images";
 	            File folderF = new File(folder);
 	   	     
 	            if (!folderF.exists()) {
 	   	            folderF.mkdir();
 	            }
 	   	        
 	   	     	String imageFilePath = folder + "/" + "Adventure_App" + String.valueOf(System.currentTimeMillis()) + "jpg";
 	   	        // File imageFile = new File(imageFilePath);
 	   	        // imageFileUri = Uri.fromFile(imageFile);
 	   	        // imageFileUri = chosenImageUri;
 	   	     	//File imageView = new File(imageFilePath);
 	   	     	//chosenImageUri = Uri.fromFile(imageView);
 	   	     	
 	   	     
 	            try {
 	            // copyfile from gallery location to our app!
 					copyFile( getRealPathFromURI(this, chosenImageUri), imageFilePath);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	            
 	            String imageId = UUID.randomUUID().toString();
 	            localStorageController.insertImage( imageId, imageFilePath, 0, currentFragement.uid().toString());
                 
 	        }
 	    }
 	    
 
 	    
 	    /**
 	     * updates the view
 	     */
 	public void updateView(){
 		//AdventureApplication.getStoryController();
 	}
 
 	@Override
 	public void dataReturn(ArrayList<Story> result, String method) {
 			// TODO Auto-generated method stub
 	}
 
 	/**
 	 * 
 	 * @param selectedImagePath - Path of image to be copied
 	 * @param string - Path of where the image is to be coppied to
 	 * @throws IOException
 	 */
     public void copyFile(String selectedImagePath, String string) throws IOException {
         InputStream in = new FileInputStream(selectedImagePath);
         OutputStream out = new FileOutputStream(string);
         
         System.out.println("I'VE GOT WHAT YOU NEED, WELL AT LEAST I HOPE I DO HERE IS THE IMAGE FILE PATHS I GOT: FROM:" + selectedImagePath + " and here is where it's going to: " + string + " yup that's it");
         
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         in.close();
         out.close();
       }
     
 /**
  * from http://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore
  * @param context
  * @param contentUri
  * @return
  */
     public String getRealPathFromURI(Context context, Uri contentUri) {
     	Cursor cursor = null;
     	try { 
     		String[] proj = { MediaStore.Images.Media.DATA };
     		cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
     		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
     		cursor.moveToFirst();
     		return cursor.getString(column_index);
     	} finally {
     		if (cursor != null) {
     			cursor.close();
     		}
     	}
     }
     
     public void publish(){
     	if (currentStory.isLocal()){
     		currentStory.setIsLocal(false);
     		HttpObjectStory httpStory = new HttpObjectStory();
     		this.httpRequest(httpStory.publishObject(currentStory), "PUBLISH");
     		currentStory.setIsLocal(true);
     	}
     }
 
     
     public void copy(){
 		Story newStory = new Story(UUID.randomUUID());
 		newStory.setIsLocal(true);
 		//LocalStorageController localStoryController = new LocalStorageController(this);
 		newStory.setTitle(currentStory.title());
 		for(int i = 0; i < currentStory.users().size(); i++)
 			newStory.addUser(currentStory.users().get(i));
 		for(int j = 0; j < currentStory.getFragements().size(); j++){
 			newStory.addFragement(currentStory.getFragements().get(j));
 			for(int w = 0; w<currentStory.getFragements().get(j).choices().size(); w++)
 				newStory.getFragements().get(j).addChoice(currentStory.getFragements().get(j).choices().get(w));
 			// TODO: get annotations for fragments & add them to newStory
 		}
 		AdventureApplication.getStoryController().addStory(newStory);
 		AdventureApplication.getActivityController().update();
     }
     
    protected void openLastFragement() {
 	   currentFragement = AdventureApplication.getStoryController().lastFragement();
 	   AdventureApplication.getStoryController().setCurrentFragement(currentFragement);
 	   AdventureApplication.getStoryController().popPreviousFragement();
 	   tileTextView.setText(currentFragement.getTitle());
 	   bodyTextView.setText(currentFragement.body());
     }
    
 	protected void saveTextForView(View v, String text) {
 		
 	}
     
     
 }// end class
