 package ch9k.chat;
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Wrapper around a String that represents a chatMessage
  * @author Jens Panneel
  */
 public class ChatMessage implements Comparable<ChatMessage>, Serializable{
     private String text;
     private String author;
     private Date time;
     private boolean systemMessage;
 
     /**
      * Filter to strip out HTML.
      */
     private static Pattern htmlStripper = Pattern.compile("<[^<]*>");
 
     /**
      * Constructor.
      * @param author The username of the person who typed this ChatMessage.
      * @param text The actual text.
      * @param systemMessage
      */
     public ChatMessage(String author, String text, boolean systemMessage) {
        this.time = new Date();
         this.text = text;
         this.author = author;
         this.systemMessage = systemMessage;
     }
 
     public ChatMessage(String author, String text) {
         this(author, text, false);
     }
 
     /**
      * Get the actual text from this ChatMessage
      * @return text
      */
     public String getText() {
         return text;
     }
 
     /**
      * Get the raw text.
      * @return HTML-stripped text.
      */
     public String getRawText() {
         Matcher matcher = htmlStripper.matcher(text);
         return matcher.replaceAll("").trim();
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
      * Check if this is a system message
      */
     public boolean isSystemMessage() {
         return systemMessage;
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
         Formatter f = new Formatter();
         f.format("<%1$tH:%1$tM> %2$s: %3$s", time, author, text);
         return f.toString();
     }
 }
