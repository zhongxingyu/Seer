 package edu.ucsb.cs56.S13.utilities.commafeed;
 
 /**
  * A wrapper for the CommaFeed API in the form of a Java Object.
  * @author Mark Nguyen
  * @author Daniel Vicory
  */
 public class CommaFeed {
   /** the raw unencoded username of the client */
   private final String username;
 
   /** the raw unencoded password of the client */
   private final String password;
 
   /**
    * Creates a new CommaFeed API wrapper that only has access to
    * the demo content.  To access a user's content, pass the user's
    * username and password as parameters for the constructor.
    * This is essentially calling <code>CommaFeed("demo", "demo")
    * </code>.
   * @see #Constructor(String username, String password)
    */
   public CommaFeed() {
     this.username = null;
     this.password = null;
   }
 
   /**
    * Creates a new CommaFeed API wrapper that has access to the user's
    * content.
    * @param username the unencoded raw username of the client
    * @param password the unencoded raw password of the client
   * @see #Constructor()
    */
   public CommaFeed(String username, String password) {
     this.username = null;
     this.password = null;
   }
 
   /**
    * Retrieves the username given during construction, or null if the
    * no args constructor was called.
    * @return the raw unencoded username of the client, or null if not
    * specified during construction.
    */
   public String getUsername() {
     return null;
   }
 
   /**
    * Retrieves the password given during construction, or null if the
    * no args constructor was called.
    * @return the raw unencoded password of the client, or null if not
    * specified during construction.
    */
   public String getPassword() {
     return null;
   }
 
 }
