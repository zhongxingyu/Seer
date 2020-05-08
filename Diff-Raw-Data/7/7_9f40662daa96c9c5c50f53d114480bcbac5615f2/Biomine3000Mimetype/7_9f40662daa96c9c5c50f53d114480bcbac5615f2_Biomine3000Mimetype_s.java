 package biomine3000.objects;
 
 import java.util.*;
 
 import util.StringUtils;
 
 /**
  * A list of leronen-appreciable Biomine TV (mime) types.
  * <p>
  * Note that the actual names are accessed via method {@link #toString()}, not via method {@link #name()}, which 
  * is final in java's enum class, and returns the name of the java language enum constant object,
  * which naturally cannot be same as the actual mime type string, being an UPPERCASE_STRING_WITH_NO_WHITESPACE
  * by convention.  
  * <p>
  * For (semi)official truth on universally boring mime types, see {@link http://en.wikipedia.org/wiki/Internet_media_type} and 
  *  {@link http://en.wikipedia.org/wiki/MIME}. As for biominoes mime types, the official truth remains to be defined.
  */
 public enum Biomine3000Mimetype {
 	
     /** Arbitrary text not be announced (todo: elaborate on the difference between plaintext and announcement */
 	PLAINTEXT("text/plain", PlainTextObject.class),
 	
 	/** Arbitrary text to be announced */
 	BIOMINE_ANNOUNCEMENT("message/announcement", PlainTextObject.class),
 	
 	/** Peaceful proposal for a zombi session, containing name of proposer and a time interval */
 	ZOMBI_PROPOSAL("message/zombiproposal"),
 	
 	/** A request to commence some kind chaos in the receivers Biomine TV, in order to boost zombi alertness. */
 	ZOMBI_ALERT("message/zombialert"),
 	
 	/**
 	 * Announcement of a plötkä match soon to be initiated. Meta-data format to be decided, probably represented as JSON.
 	 * Relevant information includes, but is not limited to: match (or tournament?) participants, time, location, mode of play
 	 * (matsi 40:een, 5 min, etc)
 	 */  
 	PLATKA_ANNOUNCEMENT("message/platkaannouncement"),
 	
 	/** format to be decided, probably represented as JSON */
 	ZOMBI_PROBABILITY_ANNOUNCEMENT("message/zombiprobabilityannouncement"),
 	
 	/** format to be decided, probably represented as JSON */
 	BIOMINE3000_SOFTWARE_AVAILABILITY_ANNOUNCEMENT("message/biomine3000_software_availability_announcement"),
 	
 	/** Png image data. Header should preferably contain some image name, perhaps, rather unexpectedly, in field "name"? */
 	PNGIMAGE("image/png", ImageObject.class, "png"),
 	
 	/** Jpg image data. Header should preferably contain some image name, perhaps, rather unexpectedly, in field "name"? */
 	JPGIMAGE("image/jpg", ImageObject.class, "jpg"),
 
 	/** Gif image data. Header should preferably contain some image name, perhaps, rather unexpectedly, in field "name"? */
     GIFIMAGE("image/gif", ImageObject.class, "gif"),
 	
 	/** URL to an already existing image in the familiar INTERNET */
 	IMAGEURL("text/url", PlainTextObject.class),
 
 	MP3("audio/mp3"),
 	
 	/** Mielivaltaista kontenttia */
 	ARBITRARY("application/arbitrary"),
 		
 	/**
 	 * Announcement of an BIOMINE COMPETITION. This rather complex concept remains yet to be defined exactly,
 	 * but all veterans of ttnr competition must know what is meant by this.
 	 */	 
 	COMPETITION("application/biomine_competition"),
 	
 	/** An entry participating in an COMPETITION described above. */
 	COMPETITION_ENTRY("application/biomine_competition_entry");
 	
 	static private Map<String, Biomine3000Mimetype> typeByName;
 	static private Map<String, Biomine3000Mimetype> typeByExtension;
 	private String typeString;
 	private String fileExtension;
 	private Class<? extends BusinessObject> implementationClass;
 	
 	static {
 	    typeByName = new HashMap<String, Biomine3000Mimetype>();
 	    typeByExtension = new HashMap<String, Biomine3000Mimetype>();
 	    for (Biomine3000Mimetype type: values()) {
 	        typeByName.put(type.typeString, type);
 	        typeByExtension.put(type.fileExtension, type);
 	    }
 	}
 	
 	public static Biomine3000Mimetype getByName(String name) {
	    return typeByName.get(name);
 	}		
 	
 	/** Return null, if no suitable type found */
 	public static Biomine3000Mimetype getImageTypeByFileName(String fileName) {
 	    String extension = StringUtils.getExtension(fileName);
 	    if (extension == null) {
 	        return null;
 	    }
 	    extension = extension.toLowerCase();
 	    Biomine3000Mimetype candidate = typeByExtension.get(extension);
 	    if (candidate != null && candidate.implementationClass == ImageObject.class) {
 	        return candidate;
 	    }
 	    else {
 	        return null;
 	    }
 	    
 	}
 	
 	/**
 	 * This might not be hygienic. Currently implemented only for .jpg, .gif, .png 
 	 * @param extension must not include the '.'.
 	 * @return null for unmappable extensions.
 	 */ 
 	public static Biomine3000Mimetype getByExtension(String extension) {
 	    if (extension.equals("gif")) {
 	        return GIFIMAGE; 
 	    }
 	    else if (extension.equals("jpg")) {
 	        return JPGIMAGE;
 	    }
 	    else if (extension.equals("png")) {
 	        return PNGIMAGE;
 	    }
 	    else {
 	        return null;
 	    }
 	}
 	
 	/** Construct a type implemented with the default {@link BusinessObjectMetadata} */
 	private Biomine3000Mimetype(String typeString) {
 		this.typeString = typeString;
 		this.implementationClass = BusinessObject.class;		
 	}
 	
     private Biomine3000Mimetype(String typeString, 
                                 Class<? extends BusinessObject> implementationClass) {
         this.typeString = typeString;
         this.implementationClass = implementationClass;
     }
     
     private Biomine3000Mimetype(String typeString, 
                                 Class<? extends BusinessObject> implementationClass,
                                 String fileExtension) {
         this.typeString = typeString;
         this.implementationClass = implementationClass;
         this.fileExtension = fileExtension;
     }
         
     
     public BusinessObject makeBusinessObject() throws InstantiationException, IllegalAccessException {
         return implementationClass.newInstance();
     }	
 	
    /** 
     * Note that the actual names are accessed via {@link #toString()}, not via {@link #name()}, which     
     * is final in java's enum class, and returns the name of the actual java language enum constant object,
     * which naturally cannot be same as the actual mime type string.
     * 
     * We remind the reader that using toString for such a business-critical purpose is against normal leronen policies, but.
     */  
 	public String toString() {
 	    return this.typeString;
 	}
 };
 
 
