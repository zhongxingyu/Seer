 package dungeon.ui.messages;
 
 import dungeon.messages.Message;
 
 import java.io.Serializable;
 
 public class ChatMessage implements Message, Serializable {
   private final int authorId;
 
   private final String author;
 
   private final String text;
 
   public ChatMessage (int authorId, String author, String text) {
     this.authorId = authorId;
     this.author = author;
     this.text = text;
   }
 
   public String getAuthor () {
    return author;
   }
 
   /**
    * Returns the player ID of the author.
    *
    * This is used to prevent endless bouncing of messages between client and server.
    */
   public int getAuthorId () {
     return this.authorId;
   }
 
   public String getText () {
    return text;
   }
 }
