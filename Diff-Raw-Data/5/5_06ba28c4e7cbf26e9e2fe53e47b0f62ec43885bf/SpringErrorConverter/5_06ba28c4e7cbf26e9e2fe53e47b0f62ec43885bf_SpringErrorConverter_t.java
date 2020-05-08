 /*
  * Created on 27 Jul 2006
  */
 package uk.org.ponder.springutil.errors;
 
 import org.springframework.validation.Errors;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.ObjectError;
 
 import uk.org.ponder.messageutil.TargettedMessage;
 import uk.org.ponder.messageutil.TargettedMessageList;
 
 /**
  * Provides conversion between PUC "TargettedMessage"s and Spring "Error"
  * objects. This conversion is slightly lossy, since PUC doesn't support the
  * idea of "objectName" or "rejectedValue", and Spring doesn't support message
  * severities.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class SpringErrorConverter {
 
   public static TargettedMessage SpringErrortoTargettedMessage(Object erroro) {
     if (erroro instanceof FieldError) {
       FieldError error = (FieldError) erroro;
       TargettedMessage togo = new TargettedMessage(error.getCodes(), error
           .getArguments(), error.getField());
       return togo;
     }
     else if (erroro instanceof ObjectError) {
       ObjectError error = (ObjectError) erroro;
       TargettedMessage togo = new TargettedMessage(error.getCodes(), error
           .getArguments(), TargettedMessage.TARGET_NONE);
       return togo;
     }
     else
       throw new IllegalArgumentException(
           "Cannot convert Spring Error of unknown " + erroro.getClass());
   }
 
   public static Object targettedMessageToSpringError(TargettedMessage message) {
     if (message.targetid.equals(TargettedMessage.TARGET_NONE)) {
       ObjectError togo = new ObjectError("", message.messagecodes,
           message.args, null);
       return togo;
     }
     else {
       FieldError togo = new FieldError("", message.targetid, "", false,
           message.messagecodes, message.args, null);
       return togo;
     }
   }
 
  public static void appendErrors(TargettedMessageList tml,
       Errors errors) {
    tml.pushNestedPath(errors.getObjectName());
     try {
       for (int i = 0; i < errors.getErrorCount(); ++i) {
         tml.addMessage(SpringErrorConverter
             .SpringErrortoTargettedMessage(errors.getAllErrors().get(i)));
       }
     }
     finally {
       tml.popNestedPath();
     }
 
   }
 }
