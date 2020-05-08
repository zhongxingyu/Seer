 package com.novel.stdmsg;
 
 /**     ODISP    .
  *      . 
  *       .
  * @author <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
  * @author (C) 2003,  "-"
 * @version $Id: ODRemoveDepMessage.java,v 1.1 2003/12/03 19:18:38 valeks Exp $
  */
 
 public class ODRemoveDepMessage extends StandartMessage {
   /**       
    * @param origin 
    * @param replyTo      
    */
   public ODRemoveDepMessage(String origin, int replyTo) {
     super("od_remove_dep", origin, "stddispatcher", replyTo);
   }
 
   /**    
    * @return  
    */
   public String getDepName() {
     return (String) getField(0);
   }
 
   /**    
   * @param newExitCode   
    */
  public ODRemoveDepMessage setDepName(Strin newDepName) {
     fields.clear();
     addField(newDepName);
     return this;
   }
   
 }// ODShutdownMessage
