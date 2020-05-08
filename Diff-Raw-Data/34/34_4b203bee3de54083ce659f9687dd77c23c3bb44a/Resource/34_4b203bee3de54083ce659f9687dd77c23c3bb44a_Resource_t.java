 package com.novel.odisp.common;
 
 import java.util.Map;
 
 /**    ODISP .
 * @author  . 
 * @author (C) 2003,  "-"
* @version $Id: Resource.java,v 1.4 2004/07/12 12:31:00 valeks Exp $
 */
 public interface Resource {
   /**      .
    * @return  - 
    */
   int getMaxReferenceCount();
 
   /**     .
    * @param type  
    * @return  
    */
   int cleanUp(int type);
   /**  .
   * @param configuration 
    */
   void setConfiguration(Map configuration);
 }
