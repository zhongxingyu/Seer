 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package ca.ualberta.cs.c301f13t13.backend;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.UUID;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 
 import ca.ualberta.cs.c301f13t13.backend.DBContract.MediaTable;
 
 /**
  * Role: A container to hold media information. The media can be a photo, an
  * illustration, audio, or video.
  * 
  * @author Stephanie Gil
  * @author Ashley Brown
  * 
  */
 public class Media implements Serializable{
 	private UUID id;
 	private UUID chapterId;
 	private String uri;
 	private String type;
 	private String bitmapString;
 
 	// CONSTANTS
 	public static final String PHOTO = "photo";
 	public static final String ILLUSTRATION = "illustration";
 	public static final String SOUND = "sound";
 	public static final String VIDEO = "video";
 
 	/**
 	 * Initializes a new Media object without an id.
 	 * 
 	 * @param chapterId
 	 * @param uri
 	 * @param type
 	 */
 	public Media(UUID chapterId, Uri uri, String type) {
 		this.id = UUID.randomUUID();
 		this.chapterId = chapterId;
 		this.type = type;
 		if (uri != null) {
 			this.uri = uri.toString();
 			Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
 			bitmapString = Utilities.getStringFromBitmap(bitmap);
 		} else {
 			this.uri = new String();
 			bitmapString = new String();
 		}
 	}
 
 	/**
 	 * Initializes a new Media object with an id, used for making a search
 	 * criteria media object or for making a media object from the database
 	 * data.
 	 * 
 	 * @param id
 	 * @param chapterId
 	 * @param uri
 	 * @param type
 	 */
 	public Media(UUID id, UUID chapterId, Uri uri, String type) {
 		this.id = id;
 		this.chapterId = chapterId;
 		this.type = type;
 		if (uri != null) {
 			this.uri = uri.toString();
 			Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
 			bitmapString = Utilities.getStringFromBitmap(bitmap);
 		} else {
 			this.uri = new String();
 			bitmapString = new String();
 		}
 	}
 
 	// GETTERS
 
 	/**
 	 * Returns the id of the media.
 	 * 
	 * @return id
 	 */
 	public UUID getId() {
 		return id;
 	}
 
 	/**
 	 * Returns the chapter id the media belongs to.
 	 * 
 	 * @return chapterId
 	 */
 	public UUID getChapterId() {
 		return this.chapterId;
 	}
 
 	/**
 	 * Returns the uri of the media.
 	 * 
 	 * @return uri
 	 */
 	public Uri getUri() {
 		return Uri.parse(uri);
 	}
 
 	/**
 	 * Returns the media's type (PHOTO, ILLUSTRATION, AUDIO, or VIDEO).
 	 * 
 	 * @return type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * Returns the media's bitmap.
 	 * 
 	 * @return bitmap
 	 */
 	public Bitmap getBitmap() {
 		return Utilities.getBitmapFromString(bitmapString);
 	}
 	
 	/**
 	 * Returns the media's bitmap as an encoded String
 	 * 
 	 * @return bitmap string
 	 */
 	public String getBitmapString() {
 		return bitmapString;
 	}	
 
 	// SETTERS
 
 	/**
 	 * Sets the media's id.
 	 * 
 	 * @param id
 	 */
 	public void setId(UUID id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets the chapter id the media belongs to.
 	 * 
 	 * @param chapterId
 	 */
 	public void setChapterId(UUID chapterId) {
 		this.chapterId = chapterId;
 	}
 
 	/**
 	 * Sets the uri of the media. Also updates the bitmap.
 	 * 
 	 * @param uri
 	 */
 	public void setUri(Uri uri) {
 		this.uri = uri.toString();
 		if (uri != null) {
 			Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
 			bitmapString = Utilities.getStringFromBitmap(bitmap);
 		}
 	}
 
 	/**
 	 * Sets the media's bitmap.
 	 * 
 	 * @param bitmap
 	 */
 	public void setBitmap(Bitmap bitmap) {
 		bitmapString = Utilities.getStringFromBitmap(bitmap);
 	}
 
 	/**
 	 * Sets the media's bitmapString.
 	 * 
 	 * @param bitmap
 	 */
 	public void setBitmap(String bitmapString) {
 		this.bitmapString = bitmapString;
 	}
 	
 	/**
 	 * Sets the media's type.
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * Returns the information of the media (id, chapterId, type) that could be
 	 * used in searching for a media in the database. This information is
 	 * returned in a HashMap where the keys are the corresponding Media Table
 	 * column names.
 	 * 
 	 * @return HashMap
 	 */
 	public HashMap<String, String> getSearchCriteria() {
 		HashMap<String, String> info = new HashMap<String, String>();
 
 		if (id != null) {
 			info.put(MediaTable.COLUMN_NAME_MEDIA_ID, id.toString());
 		}
 
 		if (chapterId != null) {
 			info.put(MediaTable.COLUMN_NAME_CHAPTER_ID, chapterId.toString());
 		}
 
 		if (type != null) {
 			info.put(MediaTable.COLUMN_NAME_TYPE, type);
 		}
 
 		return info;
 	}
 
 	/**
 	 * Converts a chapter object to a string.
 	 */
 	@Override
 	public String toString() {
 		return "Media [id=" + id + ", chapter_id=" + chapterId + ", uri="
 				+ uri + ", type=" + type + "]";
 	}
 }
