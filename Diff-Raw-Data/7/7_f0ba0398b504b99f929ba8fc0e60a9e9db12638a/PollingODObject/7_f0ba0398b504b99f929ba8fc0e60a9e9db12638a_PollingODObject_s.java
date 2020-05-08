 package com.novel.odisp.common;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**       
 *   ODISP.
 * @author  . 
 * @author () 2003-2004,  "-"
* @version $Id: PollingODObject.java,v 1.21 2004/05/11 09:55:39 valeks Exp $
 */
 public abstract class PollingODObject extends ODObject {
   /**   ODISP  .
    * @param name  
    */
   public PollingODObject(final String name) {
     super(name);
   }
   /**     .
    * @param msg 
    */
   public final void addMessage(final Message msg) {
     handleMessage(msg);
   }
   /**      .
    * @param type  
    * @return  
    */
   public int cleanUp(final int type) {
     return 0;
   }
 }
