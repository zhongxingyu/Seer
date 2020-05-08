 package com.excelsior;
 
 import com.excelsior.push.EnhancedNotification;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
 
 public class MessageUtils {
 
    private static final Logger log = LogManager.getLogger(MessageUtils.class);

     public static byte maxUnsignedByte() {
         byte b = -1;
         int i = b & 0xFF; // unsignedInt = signedByte & 0xFF;
         b = (byte) i; // signedByte = (byte) unsignedInt;
         return b;
     }
 
 
     public static short getErrorCode(final EnhancedNotification message) {
         if (isCommandError(message)) return 1;
         if (isDeviceTokenMissing(message))  return 2;
         if (isPayloadMissing(message)) return 4;
         if (isInvalidTokenSize(message)) return 5;
         if (isInvalidPayloadSize(message)) return 7;
 
         return 0;
     }
 
     private static boolean isCommandError(final EnhancedNotification message) {
        if (message.getCommand() != 1)  {
            log.error("Invalid command "+message.getCommand());
            return true;
        }
         return false;
     }
 
     private static boolean isDeviceTokenMissing(final EnhancedNotification message) {
        if (message.getTokenLength() <= 0) {
            log.error("Invalid token length "+message.getTokenLength());
            return true;
        }
         return false;
     }
 
     private static boolean isPayloadMissing(final EnhancedNotification message) {
        if (message.getPayloadLength() == 0) {
            log.error("Invalid payload length "+message.getPayloadLength());
            return true;
        }
         return false;
     }
 
     private static boolean isInvalidTokenSize(final EnhancedNotification message) {
        if (message.getTokenLength() != 32) {
            log.error("Invalid token length "+message.getTokenLength());
            return true;
        }
         return false;
     }
 
     private static boolean isInvalidPayloadSize(final EnhancedNotification message) {
        if (message.getPayloadLength() > 256) {
            log.error("Invalid payload length "+message.getPayloadLength());
            return true;
        }
         return false;
     }
 }
