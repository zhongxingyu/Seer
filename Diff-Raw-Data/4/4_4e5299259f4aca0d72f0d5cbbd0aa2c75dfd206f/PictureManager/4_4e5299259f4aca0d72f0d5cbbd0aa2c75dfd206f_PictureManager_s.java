 package com.cs301w01.meatload.controllers;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 import com.cs301w01.meatload.model.Picture;
 import com.cs301w01.meatload.model.Tag;
 import com.cs301w01.meatload.model.querygenerators.PictureQueryGenerator;
 import com.cs301w01.meatload.model.querygenerators.TagQueryGenerator;
 
 /**
  * Implements Controller logic for Picture objects.
  * 
  * @author Isaac Matichuk
  */
 public class PictureManager implements FController {
 
 	private Context context;
 	private int picID;
 	private String albumName;
 	//private Bitmap imgOnDisplay;
 	private ArrayList<Tag> tempTags;
 
 	public PictureManager(Context context, String albumName) {
 		this.context = context;
 		this.albumName = albumName;
 	}
 
 	public PictureManager(Picture picture) {
 		this.picID = picture.getPictureID();
 		this.tempTags = picture.getTags();
 	}
 
 	public PictureManager(Context context, Picture picture) {
 		this.context = context;
 		this.picID = picture.getPictureID();
 		this.tempTags = picture.getTags();
 	}
 
 	public PictureManager(int picID) {
 		this.picID = picID;
 	}
 
 	public PictureManager(Context context) {
 		this.context = context;
 	}
 
 	// public PictureManager(int pid) {
 	// photoID = pid;
 	// }
 
 	// public PictureManager(String albumName) {
 	// this.albumName = albumName;
 	// }
 
 	/**
 	 * Sets the context for use with the model
 	 */
 	public void setContext(Context context) {
 		this.context = context;
 	}
 
 	/**
 	 * Takes Picture, save it to file, pass Picture object to DBManager
 	 * 
 	 * @see <a
 	 *      href=http://stackoverflow.com/questions/649154/android-bitmap-save
 	 *      -to-location>
 	 *      http://stackoverflow.com/questions/649154/android-bitmap
 	 *      -save-to-location</a>
 	 * @param path
 	 *            File directory where the Picture is to be saved
 	 */
 	public Picture takePicture(File path, Bitmap imgOnDisplay) {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm-sss");
 		String timestamp = sdf.format(cal.getTime());
 		String fname = "img-" + timestamp + ".PNG";
 		String fpath = path.toString() + "/";
 
 		try {
 			OutputStream outStream = null;
 			File file = new File(path, fname);
 
 			outStream = new FileOutputStream(file);
 			imgOnDisplay.compress(Bitmap.CompressFormat.PNG, 100, outStream);
 			outStream.flush();
 			outStream.close();
 
 			// adds the new picture to the db and returns a picture object
 			return createPicture(fpath + fname, cal.getTime(), fname);
 
 		} catch (IOException e) {
 			Log.d("ERROR", "Unable to write " + fpath + fname);
 			return null;
 		}
 
 		// TODO: Move to EditPictureActivity after takePicture finishes.
 	}
 
 	private Picture createPicture(String fpath, Date date, String fname) {
 
 		Picture newPic = new Picture(albumName + ":" + date.toString(), fpath,
 				albumName, date, new ArrayList<Tag>());
 
 		newPic.setID((int) new PictureQueryGenerator(context)
 				.insertPicture(newPic));
 		Log.d("SAVE", "Saving " + fpath);
 
 		return newPic;
 	}
 	
	public void savePicture() {
		new PictureQueryGenerator(context).updatePictureByID(getPicture(), PictureQueryGenerator.TABLE_NAME, picID);
 		saveTags();
 	}
 
 	/**
 	 * Get this picture
 	 * 
 	 * @return Picture associated with this PictureManager
 	 */
 	public Picture getPicture() {
 		Picture newpic = new PictureQueryGenerator(context).selectPictureByID(picID);
 		return newpic;
 	}
 	
 	public void populateTempTags() {
 		tempTags = getPicture().getTags();
 	}
 	
 	public ArrayList<Tag> getTempTags() {
 		return tempTags;
 	}
 
 	/**
 	 * Delete the Picture associated with this PictureManager object in the
 	 * database.
 	 * 
 	 * @see PictureQueryGenerator
 	 */
 	public void deletePicture() {
 		PictureQueryGenerator pQ = new PictureQueryGenerator(context);
 		pQ.deletePictureByID(picID);
 	}
 
 	/**
 	 * Adds a new tag to a picture, using a String instead of a tag object.
 	 * 
 	 * @param tagName
 	 *            String object representing the tag to be added to this picture
 	 */
 	public void addTag(String tagName) {
 		tempTags.add(new Tag(tagName, new TagQueryGenerator(context).getTagPictureCount(tagName)));
 	}
 
 	/**
 	 * Sets the tags of the picture associated with this object to the passed
 	 * Collection of tags
 	 * 
 	 * @param tags
 	 *            Collection of tags to set the picture's tags to
 	 */
 	private void saveTags() {
 		new TagQueryGenerator(context).updateTags(picID, tempTags);
 	}
 
 	/**
 	 * Remove a tag from the picture associated with this PictureManager object.
 	 * 
 	 * @param tag
 	 *            Tag to be deleted.
 	 */
 	public void deleteTag(Tag tag) {
 		for (Tag tempTag : tempTags) {
 			if (tempTag.getName().equals(tag.getName())) {
 				tempTags.remove(tempTag);
 			}
 		}
 	}
 }
