 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver;
 
 /**
  * This defines an exception that can be turned into a valid xml service
  * exception that wms clients will expect.  All errors should be wrapped in
  * this before returning to clients.
  *
  * @author Gabriel Roldn
 * @version $Id: WmsException.java,v 1.2 2003/12/16 18:46:07 cholmesny Exp $
  */
 public class WmsException extends ServiceException {
     /**
      * Empty constructor.
      */
     public WmsException() {
         super();
     }
 
     /**
     *  constructor with exception message
      *
      * @param message The message for the exception
      */
     public WmsException(String message) {
         super(message);
     }
 
     /**
      * Empty constructor.
      *
      * @param e The message for the .
      */
     public WmsException(Throwable e) {
         super(e);
     }
 
     /**
      * Empty constructor.
      *
      * @param message The message for the .
      * @param locator The message for the .
      */
     public WmsException(String message, String locator) {
         super(message);
 
         this.locator = locator;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param e The message for the .
      * @param preMessage The message to tack on the front.
      * @param locator The message for the .
      */
     public WmsException(Throwable e, String preMessage, String locator) {
         super(e, preMessage, locator);
     }
 }
