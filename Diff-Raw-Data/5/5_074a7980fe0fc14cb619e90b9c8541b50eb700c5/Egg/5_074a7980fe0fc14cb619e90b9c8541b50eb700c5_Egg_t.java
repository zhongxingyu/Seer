 package org.fourdnest.androidclient;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.net.Uri;
 import android.webkit.MimeTypeMap;
 /**
  * Represents one Egg, which is the unit of content in the system.
  * The Egg can be a local stored Egg or a temporary copy of an Egg from the
  * Nest.
  * 
  * For storing the Eggs, @see EggDatabase
  */
 public class Egg {
     
     public enum fileType {
         IMAGE, AUDIO, VIDEO, TEXT, ROUTE, NOT_SUPPORTED;
     }
 	//private static final String TAG = Egg.class.getSimpleName();
 
 
 	/** Egg id */
 	private Integer id;
 	
 	/** Id of the Nest that this Egg was or will be sent to */
 	private int nestId;
 	
 	/** Author name (for list views etc.) */
 	private String author;
 	
 	/** URI to local media file. Is null for status updates (text only Eggs). */
 	private Uri localFileURI;
 	
 	/** URI to remote media file (uploaded to a nest). Is null for text only Eggs
 	 * and Eggs that have not been successfully uploaded yet*/
 	private Uri remoteFileURI;
 	
 	/**URI to remote media file thumbnail. Is null for text only Eggs and audio Eggs
 	 */
 	private Uri remoteThumbnailUri;
 		
 	/** Caption text. */
 	private String caption;
 	
 	/** Tags attached to this Egg. */
 	private List<Tag> tags;
 	
 	/** When Egg was last uploaded to associated Nest */
 	private long lastUpload;
 	
 	/** ID used by the server for this particular egg*/
 	private String externalId;
 	
 	/**Time the egg was created in the server */
 	private Date creationDate;
 	
 	// FIXME: automatic metadata.
 	
 	
 	/**
 	 * Creates new empty Egg
 	 */
 	public Egg() {
 	}
 	
 	/**
 	 * Creates new Egg with given properties
 	 * @param id Egg id
 	 * @param nestId Nest id
 	 * @param localFileURI Local file URI
 	 * @param remoteFileURI Remote file URI
 	 * @param caption Caption text
 	 * @param tags Tag list
 	 */
 	public Egg(Integer id, int nestId, String author, Uri localFileURI, Uri remoteFileURI, String caption, List<Tag> tags, long lastUpload, Date date) {
 		this.id = id;
 		this.nestId = nestId;
 		this.author = author;
 		this.localFileURI = localFileURI;
 		this.remoteFileURI = remoteFileURI;
 		this.caption = caption;
 		this.tags = tags;
 		this.lastUpload = lastUpload;
 		this.setCreationDate(date);
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof Egg)) {
 			return false;
 		}
 		
 		Egg other = (Egg)o;
 		
 		boolean equal = (
 				Util.objectsEqual(this.id, other.id) &&
 				this.nestId == other.nestId &&
 				Util.objectsEqual(this.author, other.author) && 
 				Util.objectsEqual(this.localFileURI, other.localFileURI) &&
 				Util.objectsEqual(this.remoteFileURI, other.remoteFileURI) &&
 				Util.objectsEqual(this.caption, other.caption) &&
 				Util.objectsEqual(this.tags, other.tags) &&
 				Util.objectsEqual(this.lastUpload, other.lastUpload)
 				);
 		
 		return equal;
 	}
 	
 	@Override
 	public int hashCode() {
 		long hash = (this.id == null ? 0 : this.id.hashCode());
         hash = hash + this.nestId;
         hash = hash + (this.author == null ? 0 : this.author.hashCode());
         hash = hash + (this.localFileURI == null ? 0 : this.localFileURI.hashCode());
         hash = hash + (this.remoteFileURI == null ? 0 : this.remoteFileURI.hashCode());
         hash = hash + (this.caption == null ? 0 : this.caption.hashCode());
         hash = hash + (this.tags == null ? 0 : this.tags.hashCode());
         hash = hash + this.lastUpload;
         
         int intHash = (int) (hash % Integer.MAX_VALUE);
         
 		return intHash;
 	}
 	
 	/**
 	 * Sets Egg id
 	 * @param id New Egg id
 	 */
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	/**
 	 * Returns egg id
 	 * @return Egg id
 	 */
 	public Integer getId() {
 		return this.id;
 	}
 	
 	/**
 	 * Sets id of associated Nest
 	 * @param nestId Id of Nest that this Egg was or will be sent to.
 	 */
 	public void setNestId(int nestId) {
 		this.nestId = nestId;
 	}
 	/**
 	 * Returns id of associated Nest
 	 * @return Id of Nest that this Egg was or will be sent to.
 	 */
 	public int getNestId() {
 		return this.nestId;
 	}
 
 	/**
 	 * @return the author
 	 */
 	public String getAuthor() {
 		return this.author;
 	}
 
 	/**
 	 * @param author the author to set
 	 */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	/**
 	 * Sets local file URI
 	 * @param mediaFileURI The localFileURI to set
 	 */
 	public void setLocalFileURI(Uri mediaFileURI) {
 		this.localFileURI = mediaFileURI;
 	}
 	/**
 	 * Returns local file URI
 	 * @return The localFileURI
 	 */
 	public Uri getLocalFileURI() {
 		return this.localFileURI;
 	}
 	
 	/**
 	 * Sets remote file URI
 	 * @param remoteFileURI The remoteFileURI to set
 	 */
 	public void setRemoteFileURI(Uri remoteFileURI) {
 		this.remoteFileURI = remoteFileURI;
 	}
 	
 	/**
 	 * Returns egg remote file URI
 	 * @return Remote file URI
 	 */
 	public Uri getRemoteFileURI() {
 		return this.remoteFileURI;
 	}	
 
 	/**
 	 * Sets Egg caption text
 	 * @param caption The caption to set
 	 */
 	public void setCaption(String caption) {
 		this.caption = caption;
 	}
 	/**
 	 * Returns Egg caption text
 	 * @return Caption text
 	 */
 	public String getCaption() {
 		return this.caption;
 	}
 
 	/**
 	 * Set Egg tag list
 	 * @param tags The tag list to replace the current tags with
 	 */
 	public void setTags(List<Tag> tags) {
 		this.tags = new ArrayList<Tag>(tags);
 	}
 	/**
 	 * Returns a copy of the tag list
 	 * @return Copy of tag list.
 	 */
 	public List<Tag> getTags() {
 		return new ArrayList<Tag>(tags);
 	}
 	
 	/**
 	 * Set last upload date
 	 * @param lastUpload Last upload date
 	 */
 	public void setLastUpload(long lastUpload) {
 		this.lastUpload = lastUpload;
 	}
 	
 	/**
 	 * Returns last upload date
 	 * @return Last upload date
 	 */
 	public long getLastUpload() {
 		return this.lastUpload;
 	}
 	/**
 	 * Set the externalId
 	 * @param externalId the External id
 	 */
 	public void setExternalId(String externalId) {
 		this.externalId = externalId;
 	}
 	
 	/**
 	 * Returns the external id
 	 * @return external id
 	 */
 	public String getExternalId() {
 		return this.externalId;
 	}
 
     /**
      * Returns MIME type for Egg's local file URI
      * 
      * @return
      */
     public fileType getMimeType() {
         String mime = "";
         if (this.localFileURI != null || this.remoteFileURI != null) {
             if (this.getLocalFileURI() != null) {
                 mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                         MimeTypeMap.getFileExtensionFromUrl(this
                                 .getLocalFileURI().toString()));
             } else {
                 mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                         MimeTypeMap.getFileExtensionFromUrl(this
                                 .getRemoteFileURI().toString()));
             }
             if (mime != null) {
                 String[] fileTArray = mime.split("/");
                 String fileT = fileTArray[0];
                 if (fileT.equals("image")) {
                     return fileType.IMAGE;
                 } else if (fileT.equals("audio")) {
                     return fileType.AUDIO;
                 } else if (fileT.equals("video")) {
                     return fileType.VIDEO;
                 } else if (fileT.equals("text")) {
                     return fileType.TEXT;
                } else if (fileTArray[1].equals("json")) { // string is
                                                            // application/json
                     return fileType.ROUTE;
                 } else {
                     return fileType.NOT_SUPPORTED;
                 }
             }
         }
        return fileType.TEXT;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 	
 }
