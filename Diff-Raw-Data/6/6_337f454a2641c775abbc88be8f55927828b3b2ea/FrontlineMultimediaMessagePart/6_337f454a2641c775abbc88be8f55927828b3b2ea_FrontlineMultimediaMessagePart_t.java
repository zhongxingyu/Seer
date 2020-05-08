 package net.frontlinesms.data.domain;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 /**
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 @Entity
 public class FrontlineMultimediaMessagePart {
 	/** Unique id for this entity.  This is for hibernate usage. */
 	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(unique=true,nullable=false,updatable=false) @SuppressWarnings("unused")
 	private long id;
 	/** The content of the part, if text, or the name of the file where the data is stored. */
 	@Column(length=4096) // arbitrary size that is bigger than people are likely to type on a handset
 	private String content;
	/** 
	 * <code>true</code> if {@link #content} links to the binary file name; <code>false</code> if {@link #content} contains the text content of this part 
	 * NB: On MySQL, "binary" is a restricted keyword and can't be used.
	 * */
	@Column(name="binaryData")
 	private boolean binary;
 	
 	FrontlineMultimediaMessagePart() {}
 	
 	private FrontlineMultimediaMessagePart(boolean binary, String content) {
 		this.binary = binary;
 		this.content = content;
 	}
 	
 	public String getFilename() {
 		if(!isBinary()) throw new IllegalStateException("Should not be calling this method on a text part.");
 		return this.content;
 	}
 	public void setFilename(String filename) {
 		if(!isBinary()) throw new IllegalStateException("Should not be calling this method on a text part.");
 		this.content = filename;
 	}
 	
 	public String getTextContent() {
 		if(isBinary()) throw new IllegalStateException("Should not be calling this method on a binary part.");
 		return this.content;
 	}
 	
 	public boolean isBinary() {
 		return this.binary;
 	}
 
 //> FACTORY METHODS
 	public static FrontlineMultimediaMessagePart createTextPart(String textContent) {
 		return new FrontlineMultimediaMessagePart(false, textContent);
 	}
 	public static FrontlineMultimediaMessagePart createBinaryPart(String filename) {
 		return new FrontlineMultimediaMessagePart(true, filename);
 	}
 }
