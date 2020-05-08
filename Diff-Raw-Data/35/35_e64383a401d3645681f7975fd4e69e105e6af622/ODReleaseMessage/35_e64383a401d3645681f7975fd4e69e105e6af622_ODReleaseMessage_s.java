 package com.novel.stdmsg;
 
 /**     .
  * <p>    1  -   .
  *    (  - ) 
  *             .</p>
  * <p><b></b>:        
  *     ,      .
  *  ,     ,    
  *         .</p>
  * @author <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
  * @author (C) 2003,  "-"
 * @version $Id: ODReleaseMessage.java,v 1.1 2003/12/03 19:18:38 valeks Exp $
  */
 
 public class ODReleaseMessage extends StandartMessage {
   /**         
    * @param origin 
    * @param replyTo     No.
    */
   public ODReleaseMessage(String origin, int replyTo) {
     super("od_release", "stddispatcher", origin, replyTo);
   }
 
   /**     
    * @return  
    */
   public String getResourceName() {
     return (String) getField(0);
   }
 
   /**   
    * @param newName  
    * @return    
    */
  public ODAcquireMessage setResourceName(String newName) {
     fields.add(0, newName);
     return this;
   }
 
   /**    
    * @return   
    */
   public Resource getResource() {
     return (Resource) getField(1);
   }
 
   /**      
    * @param newResource   
    * @return    
    */
  public ODAcquireMessage setResource(Resource newResource) {
     fields.add(1, newResource);
     return this;
   }
   
 }// ODReleaseMessage
