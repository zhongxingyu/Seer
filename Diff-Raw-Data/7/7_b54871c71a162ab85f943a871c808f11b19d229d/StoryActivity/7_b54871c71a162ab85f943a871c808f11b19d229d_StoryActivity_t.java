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
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.UUID;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.uofa.adventure_app.R;
 import com.uofa.adventure_app.application.AdventureApplication;
 import com.uofa.adventure_app.controller.http.HttpObjectStory;
 import com.uofa.adventure_app.interfaces.AdventureActivity;
 import com.uofa.adventure_app.model.Choice;
 import com.uofa.adventure_app.model.Fragement;
 import com.uofa.adventure_app.model.Media;
 import com.uofa.adventure_app.model.Story;
 /**
  * Displays the Fragements in the story so that the user can read them.  
  * It allows the user to go to the next fragement of the users choosing
  * @author Kevin Lafond, Chris Pavlicek
  * @param <T>
  *
  */
 public class StoryActivity<T> extends AdventureActivity {
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
 	
 	// Used for Async Task
 	Fragement startFragement = null;
 	ArrayList<Fragement> fragements = null;
 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_story);
 		tileTextView = (TextView) findViewById(R.id.titleview);
 		authorTextView = (TextView) findViewById(R.id.authorview);
 		bodyTextView = (TextView) findViewById(R.id.storyview);
 		
 
 		currentStory = AdventureApplication.getStoryController().currentStory();
 		
 		
 		
 		currentFragement = AdventureApplication.getStoryController().currentFragement();
 		bodyTextView.setText(currentFragement.body());
 
 		System.out.println(currentFragement);
 		
 		bodyTextView.setText(currentFragement.body());
 		String authors = "Author: " + currentStory.users().get(0).toString();
 		if (currentStory.users().size() > 1){
 			authors += "\nEdited by: ";
 		}
 		for(int i = 1; i<currentStory.users().size(); i++){
 			authors +=  currentStory.users().get(i);
 			if (i != currentStory.users().size()-1 ){
 				authors  += ", ";
 			}
 		}
 		authorTextView.setText(authors);
 		tileTextView.setText(currentFragement.getTitle());
 
 		currentStory.setIsLocal(true);
 		
 		AdventureApplication.getStoryController().replaceStory(currentStory);
 		AdventureApplication.getActivityController().update();
 			
 		currentView = this.findViewById(android.R.id.content);
 		fillImageDisplay();
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
 
 
 			// Style our context menu
 			menu.setHeaderIcon(android.R.drawable.ic_input_get);
 			menu.setHeaderTitle("Annotate");
 			MenuInflater inflater1 = getMenuInflater();
 
 			// Open Menu
 			inflater1.inflate(R.menu.annotatemenu, menu);
 		}else{
 			
 			//menu.clearHeader();
 			menu.clearHeader();
 			menu.clear();
 			//menu.removeGroup(R.id.annotategroup);
 			// Style our context menu
 			menu.setHeaderIcon(android.R.drawable.ic_input_get);
 			menu.setHeaderTitle("Choices");
 			int counter = 0;
 			for(Choice f : currentFragement.choices()) {
 				UUID fragId  = f.getChoiceId();
 				String title = AdventureApplication.getStoryController().currentStory().fragementWithId(fragId).getTitle();
 				menu.add(0, counter, 0, title);
 				counter++;
 			}
 			if (currentFragement.getRandomflag()){
 				menu.add(1, counter, 0, "Random Choice");
 			}
 
 			MenuInflater inflater2 = getMenuInflater();
 			inflater2.inflate(R.menu.choices, menu);
 			
 		}
 		
 	}
 	/**
 	 * sets the current fragement to the new one.
 	 * @param Fragement f
 	 */
 	private void adjustCurrentFragement(Fragement f) {
 		AdventureApplication.getStoryController().addPreviousFragement(currentFragement);
 		AdventureApplication.getStoryController().setCurrentFragement(f);
 		currentFragement = f;
 		fillImageDisplay();
 	}
 	/**
 	 * Displays a Fragement 
 	 * @param Fragement f
 	 */
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
 				Intent myIntent = new Intent(this, AnnotateActivity.class);
 				this.startActivity(myIntent);
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
 			case R.id.help:
 				helpToast();
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
 	
 	/**
 	 * Displays the help toast for the user to be able to read
 	 */
 	public void helpToast() {
 		String helpText = new String();
 		helpText="Touch Annotate, to add an annotation to the fragement\n\n";
 		helpText=helpText+"Touch Edit Story, to open the screen with the list of the fragements allowing the user to edit the story\n\n";
 		helpText=helpText+"Touch Edit Fragement, to edit the fragement\n\n";
 		helpText=helpText+"Touch Publish, to publish your work\n\n";
 		helpText=helpText+"Touch Create Copy of Story, to get the duplicate of the story\n\n";
 		helpText=helpText+"Touch Quit Story, to go to the main screen\n\n";
 		helpText=helpText+"Touch Choices, to see the list of the choices available for the fragement\n\n";
 		Toast.makeText(this, helpText, Toast.LENGTH_LONG).show();
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		if(item.getGroupId() == 0 && item.getItemId() != R.id.cancel && item.getGroupId() != 1) {
 			// TODO: This needs to be refactored....
 			UUID fragId = AdventureApplication.getStoryController().currentFragement().choices().get(item.getItemId()).getChoiceId();
 			Fragement frag = AdventureApplication.getStoryController().currentStory().fragementWithId(fragId);
 			if(frag != null) {
 				openFragement(frag);
 			} else {
 				System.err.println("Could not load Fragement with uid, " + fragId);
 			}
 			
 		} else 
 			if (item.getGroupId() == 1){
 			Random rand = new Random();
 			int size = AdventureApplication.getStoryController().currentFragement().choices().size();
 			int  n = 0;
 			if (size > 1){
 			    n = rand.nextInt(size);
 			}else
 				if(size == 1){
 					n = 0;
 					UUID fragId = AdventureApplication.getStoryController().currentFragement().choices().get(n).getChoiceId();
 					Fragement f = AdventureApplication.getStoryController().currentStory().fragementWithId(fragId);
 					openFragement(f);
 				}else{
 					Toast toast = Toast.makeText(this, "This story doesn't have any fragments to choose from", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			if(size != 0){
 				UUID fragId = AdventureApplication.getStoryController().currentFragement().choices().get(n).getChoiceId();
 				Fragement f = AdventureApplication.getStoryController().currentStory().fragementWithId(fragId);
 				openFragement(f);
 			}
 		} else{
 
 
 			return super.onContextItemSelected(item);
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
 
    
 	    /**
 	     * updates the view
 	     */
 	public void updateView(){
 		   currentFragement = AdventureApplication.getStoryController().currentFragement();
 		   System.out.println(currentFragement);
 		   tileTextView.setText(currentFragement.getTitle());
 		   bodyTextView.setText(currentFragement.body());
 		   fillImageDisplay();
 	}
 
 
     
 
     /**
      * publishes the current story being read to the server.
      */
     public void publish(){
    	if(this.isNetworkAvailable()) {
     	if (currentStory.isLocal()){
     		currentStory.setIsLocal(false);
     		HttpObjectStory httpStory = new HttpObjectStory();
     		fragements = currentStory.getFragements();
     		ArrayList<Fragement> stripedFragements = new ArrayList<Fragement>();
     		
     		for(Fragement f: fragements) {
     			stripedFragements.add(f.stripFragement());
     		}
     		
     		startFragement = currentStory.startFragement();
     		currentStory.setStartFragement(startFragement.stripFragement());
     		
     		// Strip fragements to have id only for publishing
     		currentStory.setFragements(stripedFragements);
     		this.httpRequest(httpStory.publishObject(currentStory), "PUBLISH");
     		
     		System.out.println("Publishing Story: " + currentStory.id().toString());
     		for (Fragement f: fragements){
     			System.out.println("Publish Fragement ID "+ f.uid().toString());
     			this.httpRequest(httpStory.publishFragement(f), "PUBLISH_FRAGEMENT");
     		}
     		
     	}
    	} else {
    		String helpText = new String();
    		helpText="Network not avaliable.";
    		Toast.makeText(this, helpText, Toast.LENGTH_SHORT).show();
    	}
     }
 
     /**
      * creates a local mirror of a story.
      */
     public void copy(){
     	
     	Story newStory = AdventureApplication.getStoryController().currentStory().localCopy();
 
 		AdventureApplication.getStoryController().addStory(newStory);
 		AdventureApplication.getActivityController().update();
     }
     
    protected void openLastFragement() {
 	   currentFragement = AdventureApplication.getStoryController().lastFragement();
 	   AdventureApplication.getStoryController().setCurrentFragement(currentFragement);
 	   AdventureApplication.getStoryController().popPreviousFragement();
 	   tileTextView.setText(currentFragement.getTitle());
 	   bodyTextView.setText(currentFragement.body());
 	   fillImageDisplay();
     }
    
 	protected void saveTextForView(View v, String text) {
 		
 	}
 
 	/**
 	 * fills the liear layout with the images for the current fragement.
 	 */
     public void fillImageDisplay(){
     	ArrayList<Media> fragementImages = currentFragement.media();
 
     	LinearLayout listView = (LinearLayout) findViewById(R.id.imageItemView);
     	listView.removeAllViews();
     	 for (int i = 0; i < fragementImages.size(); i++) {
              Media mediaImage = fragementImages.get(i);
              String convertedString = mediaImage.getMedia();
              if (convertedString != null) {
                      ImageView image = new ImageView(StoryActivity.this);
                      Bitmap bitmap = Media.decodeBase64(convertedString);
                      image.setImageBitmap(bitmap);
                      image.setPadding(0, 15, 0, 15);
                      listView.addView(image);
              }
      }
     	 listView.setGravity(Gravity.CENTER);
 
     }
 
 	@Override
 	public void dataReturn(ArrayList<Story> result, String method) {
 		if(method.equals("PUBLISH")) {
     		// Reset Start for Local Use
     		currentStory.setStartFragement(startFragement);
     		// Set story back to fragements
     		currentStory.setFragements(fragements);
     		currentStory.setIsLocal(true);
     		this.updateView();
 		}
 		// TODO Auto-generated method stub
 		
 	}
     
     
 }// end class
