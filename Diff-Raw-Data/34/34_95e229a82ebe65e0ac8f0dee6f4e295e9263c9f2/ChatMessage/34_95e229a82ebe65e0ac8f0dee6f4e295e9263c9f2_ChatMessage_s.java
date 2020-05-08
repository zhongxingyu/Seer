 package ch9k.chat;
 
 import java.io.Serializable;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Wrapper around a String that represents a chatMessage
  * @author Jens Panneel
  */
 public class ChatMessage implements Comparable<ChatMessage>, Serializable{
     private String text;
     private String author;
     private Date time;
 
     /**
      * Constructor.
      * @param author The username of the person who typed this ChatMessage.
      * @param text The actual text.
      */
     public ChatMessage(String author, String text) {
         this.time = new Date();
         this.text = text;
         this.author = author;
     }
 
     /**
      * Get the actual text from this ChatMessage
      * @return text
      */
     public String getText() {
         return text;
     }
 
     /**
      * Get the name of the user that wrote this ChatMessage
      * @return writer
      */
     public String getAuthor() {
         return author;
     }
 
     /**
      * Get the time of this
      * @return time
      */
     public Date getTime() {
         return time;
     }
 
     /**
      * Compares To ChatMessages ordered by time
      * @param chatMessage The ChatMessage to compare with this one.
      */
     @Override
     public int compareTo(ChatMessage chatMessage) {
         if(this.equals(chatMessage)) {
             return 0;
         }
 
         int compareTime = this.getTime().compareTo(chatMessage.getTime());
         if(compareTime == 0) {
             compareTime++;
         }
         return compareTime;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null || getClass() != obj.getClass()) {
             return false;
         }
         ChatMessage other = (ChatMessage) obj;
         if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
             return false;
         }
         if ((this.author == null) ? (other.author != null) : !this.author.equals(other.author)) {
             return false;
         }
         if (this.time != other.time && (this.time == null || !this.time.equals(other.time))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 53 * hash + (this.text != null ? this.text.hashCode() : 0);
         hash = 53 * hash + (this.author != null ? this.author.hashCode() : 0);
         hash = 53 * hash + (this.time != null ? this.time.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);

        return ("<" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ">" + author + ": " + text);
     }
 }
