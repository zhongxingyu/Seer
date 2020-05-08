 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.common.ExceptionUtils;
 
 /**
  * Exception thrown to indicate that the result from a XINS API call was
  * invalid according to the XINS standard.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  */
 public final class InvalidCallResultException
 extends CallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>InvalidCallResultException</code> with the
     * specified detail message.
     *
     * @param message
     *    the detail message, can be <code>null</code>.
    *
    * @deprecated
    *    Deprecated since XINS 0.201. Use
    *    {@link InvalidCallResultException(CallRequest,TargetDescriptor,String)}
    *    instead.
     */
    InvalidCallResultException(String message) {
      this(message, null);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
