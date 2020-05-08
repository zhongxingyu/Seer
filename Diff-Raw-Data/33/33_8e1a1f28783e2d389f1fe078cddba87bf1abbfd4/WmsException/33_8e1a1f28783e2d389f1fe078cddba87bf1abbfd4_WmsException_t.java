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
 * @version $Id: WmsException.java,v 1.3 2003/12/26 17:46:10 cholmesny Exp $
  */
 public class WmsException extends ServiceException {
     /**
      * Empty constructor.
      */
     public WmsException() {
         super();
     }
 
     /**
     * constructor with exception message
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

    /**
     * Return request type.
     *
     * @param printStackTrace whether the stack trace should be included.
     *
     * @return The ServiceExceptionReport of this error.
     *
     * @task REVISIT: adapt it to handle WMS too
     */
    public String getXmlResponse(boolean printStackTrace) {
        StringBuffer returnXml = new StringBuffer("<?xml version=\"1.0\"");
        returnXml.append(" encoding=\"UTF-8\" standalone=\"no\" ?>");
        returnXml.append(
            "<!DOCTYPE ServiceExceptionReport SYSTEM \"http://www.digitalearth.gov/wmt/xml/exception_1_1_0.dtd\"> ");
        returnXml.append("<ServiceExceptionReport version=\"1.1.0\">");

        // Write exception code
        returnXml.append("    <ServiceException"
            + ((code != null) ? (" code=\"" + code + "\"") : "") + ">"
            + getXmlMessage(printStackTrace) + "</ServiceException>");

        // Write footer
        returnXml.append("  </ServiceExceptionReport>");

        return returnXml.toString();
    }
 }
