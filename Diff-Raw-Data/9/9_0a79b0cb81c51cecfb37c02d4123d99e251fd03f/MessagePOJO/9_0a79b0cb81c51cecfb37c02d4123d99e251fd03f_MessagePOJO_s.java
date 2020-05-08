 package edu.upenn.cis350;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 
 public class MessagePOJO implements Parcelable {
 	private boolean read;
 	private String message;
 	private String author; // should this be authorID?
 	private String timestamp;
 	private List<CommentPOJO> comments;
 	
 	// Do I need this?
 	public static final Parcelable.Creator<MessagePOJO> CREATOR = new Parcelable.Creator<MessagePOJO>() {
         public MessagePOJO createFromParcel(Parcel in) {
             return new MessagePOJO(in);
         }
 
         public MessagePOJO[] newArray(int size) {
             return new MessagePOJO[size];
         }
     };
     
     public MessagePOJO() {
     	comments = new ArrayList<CommentPOJO>();
     }
     
 	public MessagePOJO(Parcel in) {
 		read = false;
 		message = in.readString();
 		author = in.readString();
 		timestamp = in.readString();
    	in.readList(comments, null);
 	}
 	
 	@Override
 	public int describeContents() {
 		// TODO Auto-generated method stub
 		return hashCode();
 	}
 	
 	public boolean isRead() {
 		return read;
 	}
 	
 	public String getText() {
 		return message;
 	}
 	
 	public String getAuthor() {
 		return author;
 	}
 	
 	public String getTimestamp() {
 		return timestamp;
 	}
 	
 	public List<CommentPOJO> getComments() {
 		return comments;
 	}
 	
 	public void addToComments(CommentPOJO comment) {
 		comments.add(comment);
 	}
 	
 	public void setText(String msg) {
 		message = msg;
 	}
 	
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	public void setTimestamp(String time) {
 		timestamp = time;
 	}
 
 	@Override
 	public void writeToParcel(Parcel dest, int flags) {
 		dest.writeString(message);
 		dest.writeString(author);
 		dest.writeString(timestamp);
 		dest.writeList(comments);
 	}
 
 }
