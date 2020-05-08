 /*
  * Copyright (C) <2013>  <Justin Hoy>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package c301.AdventureBook.Controllers;
 
 import java.io.EOFException;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 
 import android.content.Context;
 import android.util.Log;
 import c301.AdventureBook.Models.Option;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 
 /**
  * This is LibraryManager that is responsible to load the library from disk
  * 
  * @author Justin Hoy - Main Creator
  * @author Minhal Syed - minor editor
  * 
  *
  */
 public class LibraryManager {
 	Context activityContext;
 	ArrayList<Story> mLibrary;
 
 	private static LibraryManager instance = new LibraryManager( );
 
 	public static LibraryManager getInstance() {
 		if (instance == null) {
 			instance = new LibraryManager();
 		}
 		return instance;
 	}
 
 	
 	/**
 	 * @param activityContext the activityContext to set
 	 */
 	public void initContext(Context activityContext) {
 		this.activityContext = activityContext;
 	}
 
 	
 	/**
 	 * @return the activityContext
 	 */
 	public Context getActivityContext() {
 		return activityContext;
 	}
 	
 	
 	/**
 	 * Load all stories from internal storage and set it as the currentLibrary.
 	 * 
 	 * @return a list of all Story objects
 	 */
 	public ArrayList<Story> getCurrentLibrary() {
 		updateCurrentLibrary();
 		return this.mLibrary;
 	}
 
 	
 	/**
 	 * @param currentLibrary the currentLibrary to set
 	 */
 	public void setCurrentLibrary(ArrayList<Story> currentLibrary) {
 		this.mLibrary = currentLibrary;
 	}
 	
 	/**
 	 * Delete a story from the library.
 	 * 
 	 * @param story the story to be deleted
 	 */
 	public void deleteStory(Story story){
		//String FILENAME = story.getTitle().toLowerCase() + "-" + story.getAuthor().toLowerCase() + ".sav";
		String FILENAME = story.getFilename();
 		activityContext.deleteFile(FILENAME);
 		updateCurrentLibrary();
 		this.mLibrary = null;
 	}
 	
 	public void updateCurrentLibrary() {
 		loadStoryFileWithKeyword("");
 	}
 	
 	
 	/**
 	 * Load all stories from internal storage filtered using a given keyword
 	 * 
 	 * @return a list of all Story objects with the keyword
 	 */
 	public void loadStoryFileWithKeyword(String KeyWord){
 		ArrayList<Story> tempLibrary = new ArrayList<Story>();
 		String[] files = activityContext.getApplicationContext().fileList();
 
 		for (int i = 0; i < files.length; i++) {
 			// do something with the file
 			if (files[i].toLowerCase().contains(KeyWord) && files[i].toLowerCase().contains(".sav")) {
 				try {
 					FileInputStream fis = activityContext.openFileInput(files[i]);
 					ObjectInputStream ois = new ObjectInputStream(fis);
 					while (true) {
 						Story someStory = (Story) ois.readObject();
 						tempLibrary.add(someStory);
 						Log.d("Success Load", someStory.getTitle());
 					}
 
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ClassNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		this.mLibrary = tempLibrary;
 	}
 	
 }
