 package gov.nih.nci.cagrid.common;
 
 
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Calendar;
 
 import org.apache.axis.Constants;
 import org.apache.axis.types.URI;
 import org.apache.axis.utils.JavaUtils;
 import org.globus.util.I18n;
 import org.globus.wsrf.utils.AnyHelper;
 import org.globus.wsrf.utils.Resources;
 import org.oasis.wsrf.faults.BaseFaultType;
 import org.oasis.wsrf.faults.BaseFaultTypeDescription;
 import org.oasis.wsrf.faults.BaseFaultTypeErrorCode;
 import org.w3c.dom.Element;
 
 /**
  * This class provides convenience functions around BaseFault API. It also 
  * provides a common way of including stack traces with Faults. A stack trace
  * of a Fault is added as a chained BaseFault with an error code dialect
  * attribute set to {@link #STACK_TRACE STACK_TRACE}. A regular Java 
  * exception is automatically converted into a BaseFault with the description
  * of exception message and with a chained BaseFault with 
  * {@link #STACK_TRACE STACK_TRACE} error code dialect.
  */
 public class FaultHelper {
 
     private static final String LS = 
         System.getProperty("line.separator");
 
     /**
      * Stack trace error code URI
      */
     public static final URI STACK_TRACE;
 
     /**
      * Exception error code URI
      */
     public static final URI EXCEPTION;
 
     private static I18n i18n = I18n.getI18n(Resources.class.getName());
 
     static {
         try {
             STACK_TRACE = new URI("http://www.globus.org/fault/stacktrace");
             EXCEPTION = new URI("http://www.globus.org/fault/exception");
         } catch(Exception e) {
             throw new RuntimeException(e.getMessage());
         }
     }
 
     private BaseFaultType fault;
     
     /**
      * Creates <code>FaultHelper</code> with a fault.
      * If the fault contains a stack trace it will be automatically converted
      * into a chained BaseFault with an error code dialect attribute set to
      * set to {@link #STACK_TRACE STACK_TRACE}.
      *
      * @param fault fault
      */
     public FaultHelper(BaseFaultType fault) {
         this(fault, true);
     }
 
     /**
      * Creates <code>FaultHelper</code> with a fault.
      *
      * @param fault fault
      * @param convertStackTrace if true and if the fault contains a stack trace
      *        it will be automatically converted into a chained BaseFault with
      *        an error code dialect attribute set to  set to 
      *        {@link #STACK_TRACE STACK_TRACE}.
      */
     public FaultHelper(BaseFaultType fault, boolean convertStackTrace) {
         if (fault == null) {
             throw new IllegalArgumentException(i18n.getMessage(
                     "nullArgument", "fault"));
         }
         this.fault = fault;
         if (convertStackTrace) {
             addStackTraceFault();
         }
         // add timestamp automatically if not set
         if (this.fault.getTimestamp() == null) {
             this.fault.setTimestamp(Calendar.getInstance());
         }
     }
 
     /**
      * Gets the fault.
      */
     public BaseFaultType getFault() {
         return this.fault;
     }
     
     /**
      * Returns all the descriptions of the fault as a simple string.
      */
     public String getDescriptionAsString() {
         BaseFaultTypeDescription [] desc = this.fault.getDescription();
         if (desc == null) {
             return null;
         }
         StringBuffer buf = new StringBuffer();
         for (int i=0;i<desc.length;i++) {
             buf.append(desc[i].get_value());
             if (i+1<desc.length) {
                 buf.append(" / ");
             }
         }
         return buf.toString();
     }
 
     /**
      * Returns descriptions of the fault.
      *
      * @return the descriptions. Might be null.
      */
     public String[] getDescription() {
         BaseFaultTypeDescription [] desc = this.fault.getDescription();
         if (desc == null) {
             return null;
         }
         String [] description = new String[desc.length];
         for (int i=0;i<description.length;i++) {
             description[i] = desc[i].get_value();
         }
         return description;
     }
 
     /**
      * Sets the description of the fault. 
      *
      * @param description the new description of the fault.
      */
     public void setDescription(String description) {
         setDescription((description == null) ? 
                        null : new String [] {description});
     }
 
     /**
      * Sets the description of the fault. 
      *
      * @param description the new descriptions of the fault.
      */
     public void setDescription(String[] description) {
         BaseFaultTypeDescription [] desc = null;
         if (description != null) {
             desc = new BaseFaultTypeDescription[description.length];
             for (int i=0;i<description.length;i++) {
                 desc[i] = new BaseFaultTypeDescription(description[i]);
             }
         }
         this.fault.setDescription(desc);
     }
 
     /**
      * Adds a description to the description list of the fault.
      *
      * @param description the description to add.
      */
     public void addDescription(String description) {
         if (description == null) {
            // not throwing an exception when this can be properly handled
            // throw new IllegalArgumentException(i18n.getMessage(
            //    "nullArgument", "description"));
            description = "null";
         }
         BaseFaultTypeDescription [] desc = this.fault.getDescription();
         BaseFaultTypeDescription [] newDesc = null;
         if (desc == null) {
             newDesc = new BaseFaultTypeDescription[1];
         } else {
             newDesc = new BaseFaultTypeDescription[desc.length + 1];
             System.arraycopy(desc, 0, newDesc, 0, desc.length);
         }
         newDesc[newDesc.length - 1] = 
             new BaseFaultTypeDescription(description);
         this.fault.setDescription(newDesc);
     }
 
     /**
      * Adds a fault cause to the fault.
      *
      * @param exception the exception to add as a cause of this fault.
      *                  If the exception is of BaseFault type then it is
      *                  just added as is as a fault cause. Otherwise, the 
      *                  exception is converted into a new BaseFault and then
      *                  added as a fault cause. 
      */
     public void addFaultCause(Throwable exception) {
         addFaultCause( toBaseFault(exception) );
     }
 
     private void addFaultCause(BaseFaultType faultCause) {
         BaseFaultType[] cause = this.fault.getFaultCause();
         BaseFaultType[] newCause = null;
         if (cause == null) {
             newCause = new BaseFaultType[1];
         } else {
             newCause = new BaseFaultType [cause.length + 1];
             System.arraycopy(cause, 0, newCause, 0, cause.length);
         }
         newCause[newCause.length - 1] = faultCause;
         this.fault.setFaultCause(newCause);
     }
 
     private void addStackTraceFault() {
         // check if stack trace fault is already added
         Element stackElement = this.fault.lookupFaultDetail(
                                  Constants.QNAME_FAULTDETAIL_STACKTRACE);
         if (stackElement == null) {
             return;
         }
         // remove SOAP details stack entry
         this.fault.removeFaultDetail(Constants.QNAME_FAULTDETAIL_STACKTRACE);
 
         String message = this.fault.getClass().getName();
         String stackTrace = stackElement.getFirstChild().getNodeValue();
 
         // add stack trace fault
         addFaultCause(createStackFault(message, stackTrace));
     }
 
     private void addStackTraceFault(Throwable exception) {
         String message = exception.getClass().getName();
         String stackTrace = JavaUtils.stackToString(exception);
 
         // add stack trace fault
         addFaultCause(createStackFault(message, stackTrace));
     }
     
     private static BaseFaultType createStackFault(String message,
                                                   String stackTrace) {
         BaseFaultType stackFault = new BaseFaultType();
         BaseFaultTypeErrorCode errorCode = new BaseFaultTypeErrorCode();
         errorCode.setDialect(STACK_TRACE);
         errorCode.set_any(AnyHelper.toText(stackTrace));
         stackFault.setErrorCode(errorCode);
         
         if (message != null && message.length() > 0) {
             BaseFaultTypeDescription [] desc = new BaseFaultTypeDescription[1];
             desc[0] = new BaseFaultTypeDescription(message);
             stackFault.setDescription(desc);
         }
         
         stackFault.setTimestamp(Calendar.getInstance());
         
         return stackFault;
     }
 
     /**
      * Converts exception to a BaseFault.
      *
      * @param exception the exception to convert.
      * @return If the exception is of BaseFault type then it is returned
      *         as is. Otherwise, the exception is converted into a BaseFault
      *         with the description of the exception message and with a
      *         chained BaseFault with {@link #STACK_TRACE STACK_TRACE} 
      *         error code dialect and error code value that contains the
      *         exception stack trace.
      */
     public static BaseFaultType toBaseFault(Throwable exception) {
         BaseFaultType fault = null;
         if (exception instanceof BaseFaultType) {
             fault = (BaseFaultType)exception;
             // will add the FaultCause with stack trace
             FaultHelper helper = new FaultHelper(fault, false);
             helper.addDescription(fault.getFaultString());
             helper.addStackTraceFault();
         } else {
             fault = new BaseFaultType();
             FaultHelper helper = new FaultHelper(fault, false);
             helper.setDescription(exception.getMessage());
             helper.addStackTraceFault(exception);
         }
         return fault;
     }
 
     /**
      * Gets the error message of the exception.
      *
      * @param exception if exception is of type <code>BaseFaultType</code>
      *                  {@link #getMessage() getMessage()} is
      *                  called to get the error message. Otherwise, 
      *                  <code>getMessage</code> operation is called on the
      *                  exception.
      */
     public static String getMessage(Throwable exception) {
         if (exception instanceof BaseFaultType) {
             FaultHelper faultHelper = 
                 new FaultHelper((BaseFaultType)exception, false);
             return faultHelper.getMessage();
         } else {
             return exception.getMessage();
         }
     }
 
     /**
      * Gets the stack trace of the exception.
      *
      * @param exception if exception is of type <code>BaseFaultType</code>
      *                  {@link #printStackTrace() printStackTrace()} is
      *                  called to get the error message. Otherwise, 
      *                  <code>printStackTrace</code> operation is called on the
      *                  exception.
      */
     public static void printStackTrace(Throwable exception) {
         if (exception instanceof BaseFaultType) {
             exception.printStackTrace();
             // Disabled becuase it omits client stack info
             /*
             FaultHelper faultHelper = 
                 new FaultHelper((BaseFaultType)exception, false);
             faultHelper.printStackTrace();
             */
         } else {
             exception.printStackTrace();
         }
     }
 
     /**
      * Prints stack trace of the fault to <code>System.err</code>.
      * See {@link #getStackTrace() getStackTrace()} for more information.
      */
     public void printStackTrace() {
         printStackTrace(System.err);
     }
     
     /**
      * Writes stack trace of the fault to stream.
      * See {@link #getStackTrace() getStackTrace()} for more information.
      */
     public void printStackTrace(PrintStream s) {
         s.println(getStackTrace());
     }
     
     /**
      * Writes stack trace of the fault to writer.
      * See {@link #getStackTrace() getStackTrace()} for more information.   
      */
     public void printStackTrace(PrintWriter s) {
         s.println(getStackTrace());
     }
     
     /**
      * Gets error message of the fault.
      *
      * @return If the fault has error code dialect of {@link #STACK_TRACE
      *         STACK_TRACE} null is returned. Otherwise, the error message 
      *         is composed of all descriptions of the fault and descriptions
      *         of the chained faults.
      */
     public String getMessage() {
         BaseFaultTypeErrorCode errorCode =
             this.fault.getErrorCode();
         if (errorCode != null) {
             if (STACK_TRACE.equals(errorCode.getDialect())) {
                 return null;
             }
         }
         StringBuffer buf = new StringBuffer();
         buf.append(this.fault.getClass().getName());
         String desc = getDescriptionAsString();
         if (desc != null) {
             buf.append(": ").append(desc);
         }
         BaseFaultType [] cause = this.fault.getFaultCause();
         if (cause != null) {
             boolean wroteCauseBy = false;
             int j = 0;
             for (int i=0;i<cause.length;i++) {
                 desc = getMessage(cause[i]);
                 if (desc == null) {
                     continue;
                 }
                 if (!wroteCauseBy) {
                     buf.append(i18n.getMessage("causedBy") + "[");
                     wroteCauseBy = true;
                 }
                 buf.append(String.valueOf(j++)).append(": ").append(desc);
             }
             if (wroteCauseBy) {
                 buf.append("]");
             }
         }
         return buf.toString();
     }
 
     /**
      * Gets stack trace of the fault. Note, this stack trace only contains
      * information sent from server. It does not contain client stack 
      * trace information.
      *
      * @return stack trace of the fault. It includes any chained faults.
      */
     public String getStackTrace() {
         StringBuffer buf = new StringBuffer();
         buf.append(this.fault.getClass().getName());
         String desc = getDescriptionAsString();
         if (desc != null) {
             buf.append(": ").append(desc);
         }
         if (this.fault.getTimestamp() != null) {
             buf.append(LS).append(i18n.getMessage("timestamp"));
             buf.append(this.fault.getTimestamp().getTime().toString());
         }
         if (this.fault.getOriginator() != null) {
             buf.append(LS).append(i18n.getMessage("originator"));
             buf.append(this.fault.getOriginator().toString());
         }
         BaseFaultType [] cause = this.fault.getFaultCause();
         BaseFaultTypeErrorCode errorCode = null;
         if (cause != null) {
             FaultHelper helper = null;
             for (int i=0;i<cause.length;i++) {
                 helper = new FaultHelper(cause[i], false);
                 errorCode = cause[i].getErrorCode();
                 if (errorCode != null && 
                     STACK_TRACE.equals(errorCode.getDialect())) {
                     buf.append(LS);
                     try {
                         buf.append(AnyHelper.toSingleString(
                                               errorCode.get_any()));
                     } catch (Exception e) {
                         // ?
                     }
                     continue;
                 }
                 buf.append(LS).append(i18n.getMessage("causedBy01"));
                 buf.append(helper.getStackTrace());
             }
         }
         return buf.toString();
     }
 }
