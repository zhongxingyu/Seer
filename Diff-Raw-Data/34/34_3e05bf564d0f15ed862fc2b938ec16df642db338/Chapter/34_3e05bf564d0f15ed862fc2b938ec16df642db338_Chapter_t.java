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
 
 package ca.ualberta.cs.c301f13t13.backend;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.UUID;
 
 import ca.ualberta.cs.c301f13t13.backend.DBContract.ChapterTable;
 
 /**
  * Role: A container to hold chapter information. This includes id, story id,
  * text, choices illustrations and photos
  * 
  * @author Ashley Brown
  * @author Stephanie Gil
  * 
  */
 public class Chapter implements Serializable {
 	private UUID id;
 	private UUID storyId;
 	private String text;
 	private ArrayList<Choice> choices;
 	private ArrayList<Media> illustrations;
 	private ArrayList<Media> photos;
 
 	/**
 	 * Initializes a new chapter object with no id.
 	 * 
 	 * @param storyId
 	 *            The id of the story the chapter is from
 	 * @param text
 	 *            The chapter text that the user will be reading
 	 */
 	public Chapter(UUID storyId, String text) {
 		this.text = text;
 		this.storyId = storyId;
 		this.id = UUID.randomUUID();
 		choices = new ArrayList<Choice>();
 		illustrations = new ArrayList<Media>();
 		photos = new ArrayList<Media>();
 	}
 
 	/**
 	 * Initialize a new chapter with an id. Can also be used to make a chapter
 	 * from the chapter information retrieved from the database. Can also be
 	 * used to create a choice that will serve to hold search criteria.
 	 * 
 	 * @param id
 	 *            The unique id of the chapter
 	 * @param storyId
 	 *            The id of the story the chapter is from
 	 * @param text
 	 *            The chapter text that the user will be reading
 	 */
 	public Chapter(UUID id, UUID storyId, String text) {
 		this.id = id;
 		this.text = text;
 		this.storyId = storyId;
 		illustrations = new ArrayList<Media>();
 		photos = new ArrayList<Media>();
 	}
 
 	// Getters
 
 	/**
 	 * Returns the Id of the chapter.
 	 * 
	 * @return id
 	 */
 	public UUID getId() {
 		return this.id;
 	}
 
 	/**
 	 * Returns the story Id of the chapter.
 	 * 
	 * @return story id
 	 */
 	public UUID getStoryId() {
 		return this.storyId;
 	}
 
 	/**
 	 * Returns the text of the chapter.
 	 * 
	 * @return text
 	 */
 	public String getText() {
 
 		return this.text;
 	}
 
 	/**
 	 * Returns the choices of the chapter.
 	 * 
	 * @return choices
 	 */
 	public ArrayList<Choice> getChoices() {
 		return this.choices;
 	}
 
 	/**
 	 * Returns the photos of the chapter.
 	 * 
	 * @return photos
 	 */
 	public ArrayList<Media> getPhotos() {
 		return this.photos;
 	}
 
 	/**
 	 * Returns the illustrations of the chapter.
 	 * 
	 * @return illustrations
 	 */
 	public ArrayList<Media> getIllustrations() {
 		return this.illustrations;
 	}
 
 	// SETTERS
 
 	/**
 	 * Sets the Id of the chapter.
 	 * 
 	 * @param id
 	 *            The unique Id of the chapter
 	 */
 	public void setId(UUID id) {
 		this.id = id;
 	}
 
 	/**
 	 * Sets the story Id of the chapter.
 	 * 
	 * @param id
 	 *            The id of the story the chapter is in
 	 */
 	public void setStoryId(UUID id) {
 		this.storyId = id;
 	}
 
 	/**
 	 * Sets the text of the chapter.
 	 * 
 	 * @param text
 	 *            The text of the chapter that the user will read
 	 */
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	/**
 	 * Sets the choices of the chapter.
 	 * 
 	 * @param choices
 	 *            The choices that link this chapter to another
 	 */
 	public void setChoices(ArrayList<Choice> choices) {
 		this.choices = choices;
 	}
 
 	/**
 	 * Sets the photos of the chapter.
 	 * 
 	 * @param photos
 	 */
 	public void setPhotos(ArrayList<Media> photos) {
 		this.photos = photos;
 	}
 
 	/**
 	 * Sets the illustrations of the chapter.
 	 * 
 	 * @param illustrations
 	 */
 	public void setIllustrations(ArrayList<Media> illustrations) {
 		this.illustrations = illustrations;
 	}
 
 	// OTHER METHODS
 
 	/**
 	 * Adds a choice to the chapter.
 	 * 
 	 * @param choice
 	 */
 	public void addChoice(Choice choice) {
 		choices.add(choice);
 	}
 
 	/**
 	 * Adds a photo to the chapter
 	 * 
 	 * @param photo
 	 */
 	public void addPhoto(Media photo) {
 		photos.add(photo);
 	}
 
 	/**
 	 * Adds an illustration to the chapter.
 	 * 
 	 * @param i
 	 */
 	public void addIllustration(Media i) {
 		illustrations.add(i);
 	}
 
 	/**
 	 * Converts a chapter object to a string.
 	 */
 	@Override
 	public String toString() {
 		return "Chapter [id=" + id + ", storyId=" + storyId + ", text=" + text
 				+ "]";
 
 	}
 
 	/**
 	 * Returns the information of the chapter (id, storyId) that could be used
 	 * in searching for a chapter in the database. This information is returned
 	 * in a HashMap where the keys are the corresponding Chapter Table column
 	 * names.
 	 * 
 	 * @return HashMap
 	 */
 	public HashMap<String, String> getSearchCriteria() {
 		HashMap<String, String> info = new HashMap<String, String>();
 
 		if (id != null) {
 			info.put(ChapterTable.COLUMN_NAME_CHAPTER_ID, id.toString());
 		}
 
 		if (storyId != null) {
 			info.put(ChapterTable.COLUMN_NAME_STORY_ID, storyId.toString());
 		}
 
 		return info;
 	}
 }
