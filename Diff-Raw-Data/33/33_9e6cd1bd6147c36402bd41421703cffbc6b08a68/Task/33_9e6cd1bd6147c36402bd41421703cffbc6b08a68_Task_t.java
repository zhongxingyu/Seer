 package ca.ualberta.cs.completemytask;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Set;
import java.util.TreeSet;
 
 /**
  * Stores a information about a task.
  * 
  * @author Michael Feist
  * 
  */
 
 @SuppressWarnings("serial")
 public class Task extends UserData {
 
 	private static final String TAG = "Task";
 
 	private boolean shared;
 	private boolean complete;
 	private boolean local;
 
 	private String name;
 	private String description;
 	
 	private boolean needsComment;
 	private boolean needsPhoto;
 	private boolean needsAudio;
 	
 	private List<Comment> comments;
 	private List<MyPhoto> photos;
 	private List<MyAudio> audios;
	
	private Set<String> ids;
 
 	public Task() {
 		this("New Task", "No Description");
 	}
 
 	public Task(String name, String description) {
 		super();
 		this.shared = false;
 		this.complete = false;
 		this.local = true;
 
 		this.name = name;
 		this.description = description;
 		
 		this.needsComment = false;
 		this.needsPhoto = false;
 		this.needsAudio = false;
 		
 		this.comments = new ArrayList<Comment>();
 		this.photos = new ArrayList<MyPhoto>();
 		this.audios = new ArrayList<MyAudio>();
		
		this.ids = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);  
 
 	}
 	
 	/**
 	 * Get the number of comments contained in this task.
 	 * @return Number of comments
 	 */
 	public int getNumberOfComments() {
 		return this.comments.size();
 	}
 	
 	/**
 	 * Add a comment to this task.
 	 * @param A comment
 	 */
 	public void addComment(Comment comment) {
		String id = comment.getId();
		
		if (this.ids.contains(id)) {
			return;
		}
		
		this.ids.add(id);
 		this.comments.add(comment);
 	}
 	
 	/**
 	 * Get the comment at the given position.
 	 * @param The position in the comments array.
 	 * @return The comment at that position.
 	 */
 	public Comment getCommentAt(int position) {
 		return this.comments.get(position);
 	}
 	
 	/**
 	 * Get the number of photos contained in this task.
 	 * @return Number of photos
 	 */
 	public int getNumberOfPhotos() {
 		return this.photos.size();
 	}
 	
 	/**
 	 * Add a photo to this task.
 	 * @param A photo
 	 */
 	public void addPhoto(MyPhoto photo) {
		String id = photo.getId();
		
		if (this.ids.contains(id)) {
			return;
		}
		
		this.ids.add(id);
		
 		this.photos.add(photo);
 	}
 	
 	/**
 	 * Get the photo at the given position.
 	 * @param The position in the photos array.
 	 * @return The photo at that position.
 	 */
 	public MyPhoto getPhotoAt(int position) {
 		return this.photos.get(position);
 	}
 	
 	/**
 	 * Get the number of audio files contained in this task.
 	 * @return Number of audio files
 	 */
 	public int getNumberOfAudios() {
 		return this.audios.size();
 	}
 	
 	/**
 	 * Add an audio file to this task.
 	 * @param An audio file
 	 */
 	public void addAudio(MyAudio audio) {
		String id = audio.getId();
		
		if (this.ids.contains(id)) {
			return;
		}
		
		this.ids.add(id);
 		this.audios.add(audio);
 	}
 	
 	/**
 	 * Get the audio file at the given position.
 	 * @param The position in the audios array.
 	 * @return The audio at that position.
 	 */
 	public MyAudio getAudioAt(int position) {
 		return this.audios.get(position);
 	}
 	
 	/**
 	 * Set whether or not this task should be saved locally.
 	 * @param is local
 	 */
 	public void setLocal(boolean local) {
 		this.local = local;
 	}
 	
 	/**
 	 * Should the task be saved to the local storage. 
 	 * @return true if it should be saved locally
 	 */
 	public boolean isLocal() {
 		return this.local;
 	}
 	
 	/**
 	 * Set whether or not this task should shared with
 	 * the public.
 	 * @param true if it should shared with public
 	 */
 	public void setPublic(boolean shared) {
 		this.shared = shared;
 	}
 	
 	/**
 	 * Should this be shared with the public.
 	 * @return true if it should shared with public
 	 */
 	public boolean isPublic() {
 		return shared;
 	}
 	
 	/**
 	 * Set if this task is completed or not.
 	 * @param true if complete
 	 */
 	public void setComplete(boolean complete) {
 		this.complete = complete;
 		this.sync = true;
 	}
 	
 	/**
 	 * Check if this task was completed.
 	 * @return true if completed
 	 */
 	public boolean isComplete() {
 		return complete;
 	}
 
 	/**
 	 * Set the name of the task.
 	 * @param name of task
 	 */
 	public void setName(String name) {
 		this.name = name;
 		this.sync = true;
 	}
 	
 	/**
 	 * Gets the given name of the task.
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the description of the task.
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 		this.sync = true;
 	}
 	/**
 	 * Gets the given description of the task.
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 	
 	/**
 	 * Set what this task requires.
 	 * @param comment
 	 * @param photo
 	 * @param audio
 	 */
 	public void setRequirements(boolean comment, boolean photo, boolean audio) {
 		this.needsComment = comment;
 		this.needsPhoto = photo;
 		this.needsAudio = audio;
 	}
 	
 	/**
 	 * Checks if the task needs a comment.
 	 * @return true if task needs a comment
 	 */
 	public boolean needsComment() {
 		return this.needsComment;
 	}
 
 	/**
 	 * Checks if the task needs a photo.
 	 * @return true if task needs a photo
 	 */
 	public boolean needsPhoto() {
 		return this.needsPhoto;
 	}
 	
 	/**
 	 * Checks if the task needs a audio stream.
 	 * @return true if task needs a audio stream
 	 */
 	public boolean needsAudio() {
 		return this.needsAudio;
 	}
 	
 	/**
 	 * Check if the task needs to be synchronized with the database.
 	 * @return true if task needs to be synchronized with the database
 	 */
 	public boolean needsSync() {
 		return this.sync && this.shared;
 	}
 	
 	/**
 	 * Returns the task as a string.
 	 * @return A string
 	 */
 	public String toString() {
 		
 		String yes = "YES";
 		String no = "NO";
 		
 		String publicTask = no;
 		
 		String comments = no;
 		String photos = no;
 		String audios = no;
 		
 		String userName = "Unknown";
 		
 		if (needsComment) {
 			comments = yes;
 		}
 		
 		if (needsPhoto) {
 			photos = yes;
 		}
 		
 		if (needsAudio) {
 			audios = yes;
 		}
 		
 		if (shared) {
 			publicTask = yes;
 		}
 		
 		if (hasUser()) {
 			userName = getUser().getUserName();
 		}
 
 		String task = String.format(
 				"Task: \n\t " +
 				"Name: %s\n\t " +
 				"Description: %s\n\t " +
 				"Created By: %s\n\t " +
 				"Id: %s \n\t " +
 				"Public: %s \n\n\t " +
 				"Requirements: \n\t\t " +
 				"Needs Comments: %s \n\t\t Needs Photos: %s \n\t\t Needs Audio: %s \n\t",
 				this.name, this.description,
 				userName,
 				this.id, publicTask,
 				comments, photos, audios);
 
 		return task;
 	}
 	
 	/**
 	 * Gets the task as a string in JSON form.
 	 * @return String in JSON form
 	 */
 	public String toJSON() {
 		
 		JSONObject json = new JSONObject();
 		
 		String userName = "Unknown";
 		
 		if (hasUser()) {
 			userName = getUser().getUserName();
 		}
 		
         try {
 			json.put( "type", "Task");
 			json.put( "name", this.name);
 			json.put( "description", this.description);
 			json.put( "user", userName);
 			json.put( "needsComment", this.needsComment);
 			json.put( "needsPhoto", this.needsPhoto);
 			json.put( "needsAudio", this.needsAudio);
 			json.put( "isComplete", this.complete);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
         Log.v(TAG, "JSON: " + json.toString());
         
 		return json.toString();
 	}
 }
