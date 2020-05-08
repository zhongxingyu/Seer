 package jfmi.control;
 
 
 /** Represents an association between a file and a tag.
   */
 public class FileTagging implements Comparable<FileTagging> {
 
 	// PRIVATE INSTANCE Fields
 	int taggingId;
 	int fileId;
 	String tag;
 	String comment;
 
 	//************************************************************
 	// PUBLIC INSTANCE Methods
 	//************************************************************
 
 	/** Constructs a default FileTagging with negative tagging and file ids,
 	  and empty tag and comment.
 	  */
 	public FileTagging()
 	{
 		this(-1, -1, "", "");
 	}
 
 	/** Constructs a new FileTagging with the specified tagging id, file id,
 	  tag, and comment.
 	  @param tid value for taggingId
 	  @param fid value for fileId
 	  @param tagVal value for tag
 	  @param commentVal value for comment
 	  */
 	public FileTagging(int tid, int fid, String tagVal, String commentVal)
 	{
 		setTaggingId(tid);
 		setFileId(fid);
 		setTag(tagVal);
 		setComment(commentVal);
 	}
 
 	/** Determines whether or not the instance is equal to another FileTagging
 	  instance.
 	  @param other the FileTagging instance to test for equality against
 	  @return true if the instance is equal to the parameter instance
 	  */
 	public boolean equals(FileTagging other)
 	{
 		return taggingId == other.taggingId 
 				&& fileId == other.fileId
				&& tag.equals(tag)
				&& comment.equals(comment);
 	}
 
 	/** Retrieves the tagging's id.
 	  @return value of the taggingId field.
 	  */
 	public int getTaggingId()
 	{
 		return taggingId;
 	}
 
 	/** Retrieves the id of the tagging's associated file.
 	  @return the value of the fileId field
 	  */
 	public int getFileId()
 	{
 		return fileId;
 	}
 
 	/** Retrieves the tag associated with this tagging.
 	  @return the value of the tag field
 	  */
 	public String getTag()
 	{
 		return tag;
 	}
 
 	/** Retrieves the comment associated with this tagging.
 	  @return the value of the comment field
 	  */
 	public String getComment()
 	{
 		return comment;
 	}
 
 	/** Sets the id of this tagging.
 	  @param id the new value for the taggingId field
 	  */
 	public void setTaggingId(int id)
 	{
 		taggingId = id;
 	}
 
 	/** Sets the id of the file associated with this tagging.
 	  @param id the new value for the fileId field
 	  */
 	public void setFileId(int id)
 	{
 		fileId = id;
 	}
 
 	/** Sets the value of the tag associated with this tagging. If the parameter
 	  is null, the tag is set to the empty string, "".
 	  @param newTag the new value for the tag field
 	  */
 	public void setTag(String newTag)
 	{
 		if (newTag == null) {
 			tag = "";
 		} else {
 			tag = newTag;
 		}
 	}
 
 	/** Sets the value of the comment associated with this tagging. If the
 	  parameter is null, the comment is set to the empty string, "".
 	  @param newComment the new value for the comment field
 	  */
 	public void setComment(String newComment)
 	{
 		if (newComment == null) {
 			comment = "";
 		} else {
 			comment = newComment;
 		}
 	}
 
 	/**
 	  @return a String representation of the FileTagging
 	  */
 	public String toString()
 	{
 		StringBuilder string = new StringBuilder("");
 		string.append("[");
 		string.append(tag);
 		string.append("] ");
 		return string.toString();
 	}
 
 	//************************************************************
 	// IMPLEMENTATION of Comparable<FileTagging>
 	//************************************************************
 
 	/** Compares this object with the specified object for order. The natural
 	  ordering implemented by this class is consistent with equals(). Two
 	  FileTagging objects are equal if all their fields are equal. If not
 	  equal, their relationship is determined first by tag value, then by
 	  comment, taggingId, fileId.
 	  @param o the object to compare this instance against
 	  @return -1, 0, 1 as this object is less than, equal to, or greater than
 			the parameter
 	  */
 	public int compareTo(FileTagging o)
 	{
 		if (this.equals(o)) {
 			return 0;
 		} 
 
 		if (tag.compareTo(o.tag) == 0) {
 
 			if (comment.compareTo(o.comment) == 0) {
 
 				if (((Integer)taggingId).compareTo(o.taggingId) == 0) {
 
 					return ((Integer)fileId).compareTo(o.fileId);
 
 				} else {
 					return ((Integer)taggingId).compareTo(o.taggingId);
 				}
 
 			} else {
 				return comment.compareTo(o.comment);
 			}
 
 		} else {
 			return tag.compareTo(o.tag);
 		}
 	}
 
 }
