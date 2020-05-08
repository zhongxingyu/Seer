 package com.novel.odisp.common;
 
 import java.util.Map;
 
 /**    ODISP .
 * @author  . 
 * @author (C) 2003,  "-"
* @version $Id: Resource.java,v 1.3 2004/03/26 21:53:37 valeks Exp $
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
   * @param config 
    */
   void setConfiguration(Map configuration);
 }
