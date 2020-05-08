 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.manageable;
 
 import org.xins.common.text.FastStringBuffer;
 
 import org.xins.logdoc.ExceptionUtils;
 
 /**
  * Exception thrown when the initialization of a <code>Manageable</code>
  * object failed.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class InitializationException
 extends Exception {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Creates a message based on the specified constructor argument.
     *
     * @param detail
     *    the detail message passed to the constructor, or <code>null</code>.
     *
     * @param cause
     *    the cause exception, or <code>null</code>.
     *
     * @return
     *    the message, never <code>null</code>.
     */
   private static String createMessage(String detail, Throwable cause) {
 
       FastStringBuffer buffer = new FastStringBuffer(159);
       buffer.append("Initialization failed");
 
      if (detail != null) {
          buffer.append(": \"");
         buffer.append(detail);
          buffer.append('"');
       }
 
       if (cause != null) {
          String causeMessage = cause.getMessage();
          if (causeMessage != null && causeMessage.trim().length() < 1) {
             causeMessage = null;
          }
 
          buffer.append(". Caught ");
          buffer.append(cause.getClass().getName());
          buffer.append(" with message \"");
          buffer.append(causeMessage);
          buffer.append('"');
       }
       buffer.append('.');
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>InitializationException</code> with the specified
     * detail message.
     *
     * @param detail
     *    the detail message, or <code>null</code>.
     */
    public InitializationException(String detail) {
       this(detail, null);
    }
 
    /**
     * Constructs a new <code>InitializationException</code> with the specified
     * cause exception.
     *
     * @param cause
     *    the cause exception, or <code>null</code>.
     */
    public InitializationException(Throwable cause) {
       this(null, cause);
    }
 
    /**
     * Constructs a new <code>InitializationException</code> with the specified
     * detail message and cause exception.
     *
     * @param detail
     *    the detail message, or <code>null</code>.
     *
     * @param cause
     *    the cause exception, or <code>null</code>.
     */
    public InitializationException(String detail, Throwable cause) {
       super(createMessage(detail, cause));
       ExceptionUtils.setCause(this, cause);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
