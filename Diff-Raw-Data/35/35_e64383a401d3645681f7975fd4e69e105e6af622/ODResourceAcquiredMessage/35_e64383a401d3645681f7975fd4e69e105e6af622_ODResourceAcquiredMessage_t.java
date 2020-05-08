 package com.novel.stdmsg;
 
 import com.novel.odisp.common.Resource;

/**      
  * @author <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
  * @author (C) 2003,  "-"
 * @version $Id: ODResourceAcquiredMessage.java,v 1.3 2003/12/03 21:12:44 valeks Exp $
  */
 
 public class ODResourceAcquiredMessage extends StandartMessage {
   public ODResourceAcquiredMessage(String destination, int replyId) {
     super("od_resource_acquired", destination, "stddispatcher", replyId);
   }
 
   public String getClassName() {
     return (String) getField(0);
   }
 
   public ODResourceAcquiredMessage setClassName(String newClassName) {
     fields.add(0, newClassName);
     return this;
   }
 
 
   public Resource getResource() {
     return (Resource) getField(1);
   }
   
   public ODResourceAcquiredMessage setResource(Resource newResource) {
     fields.add(1, newResource);
     return this;
   }
 
 }// ODResourceAcquiredMessage
