 /*
  * $Id$
  */
 package org.xins.client;
 
 import org.xins.common.text.FastStringBuffer;
 
 /**
  * Exception that indicates that an API call returned a result that was
  * considered unacceptable by the application layer.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 0.136
  */
 public final class UnacceptableCallResultException
 extends CallException {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a message for the constructor.
     *
     * @param reason
     *    the reason why the call result is unacceptable, or <code>null</code>.
     *
     * @return
    *    the constructed message for the constructor to pass up to the
     *    superconstructor, never <code>null</code>.
     */
    private static final String createMessage(String reason) {
 
       // Create message in buffer
       FastStringBuffer buffer = new FastStringBuffer(80);
       buffer.append("Call result is unacceptable.");
       if (reason != null && reason.length() > 0) {
          buffer.append(" Reason: ");
          buffer.append(reason);
       }
 
       // Return the message string
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>UnacceptableCallResultException</code> with an
     * optional reason.
     *
     * @param reason
     *    the reason why the call result is unacceptable, or <code>null</code>.
     */
    public UnacceptableCallResultException(String reason) {
 
       super(createMessage(reason), null);
       _reason = reason;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The reason. Can be <code>null</code>.
     */
    private final String _reason;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns the reason.
     *
     * @return
     *    the reason, can be <code>null</code>.
     */
    public String getReason() {
       return _reason;
    }
 }
