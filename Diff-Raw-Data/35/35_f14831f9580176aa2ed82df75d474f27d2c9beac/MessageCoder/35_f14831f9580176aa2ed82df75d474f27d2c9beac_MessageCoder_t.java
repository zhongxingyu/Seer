 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package team039.common;
 
 import battlecode.common.*;
 
 /**
  *
  * @author sashko
  */
 public abstract class MessageCoder {
 
     //not in Quantum Constants because they are only used here
     public static final int HEADER_LENGTH = 2;
     public static final int FOOTER_LENGTH = 1;
     public static final int MAX_MESSAGES = 15;
     public static final String SECURITY_CODE = "Q5";
     public static final String START_MSG = "%S";
     public static final String END_MSG = "%E";
 
 
     //message types
     public static final String RECYCLER_PING = "rp";
     public static final String RECYCLER_DESIGNATION = "rd";
 
     /*
      * Takes a series of arguments, any of which can be null, and generates a
      * valid single message.
      */
     public static Message encodeMessage(
             String msgType,
             int broadcasterID,
             MapLocation broadcasterLocation,
             int roundNum,
             boolean reBroadcast,
             String[] bodyStrings,
             int[] bodyInts,
             MapLocation[] bodyLocations) {
 
         int bodyLength = Math.max(bodyStrings!=null?bodyStrings.length:0, Math.max(bodyInts!=null?bodyInts.length:0, bodyLocations!=null?bodyLocations.length:0));
         int outputLength = HEADER_LENGTH + bodyLength + FOOTER_LENGTH;
 
         String[] outputStrings = new String[outputLength];
         int[] outputInts = new int[outputLength];
         MapLocation[] outputLocations = new MapLocation[outputLength];
 
         outputStrings[0] = START_MSG;
         outputInts[0] = broadcasterID;
         outputLocations[0] = broadcasterLocation;
 
         outputStrings[1] = msgType;
         outputInts[1] = roundNum;
 
        for (int i = 0; i < bodyLength; i++) {
             int offsetI = i + HEADER_LENGTH;
             outputStrings[offsetI] = bodyStrings[i];
             outputInts[offsetI] = bodyInts[i];
             outputLocations[offsetI] = bodyLocations[i];
         }
 
         int footerLocation = outputLength - 1;
         outputStrings[footerLocation] = END_MSG;
         outputInts[footerLocation] = reBroadcast ? 1 : 0;
 
         Message output = new Message();
 
         output.strings = outputStrings;
         output.ints = outputInts;
         output.locations = outputLocations;
 
         return output;
     }
 
     public static String getMessageType(Message input) {
         return input.strings[1];
     }
 
     public static int getBroadcasterID(Message input) {
         return input.ints[0];
     }
 
     public static MapLocation getBroadcasterLocation(Message input) {
         return input.locations[0];
     }
 
     public static int getRoundSent(Message input) {
         return input.ints[1];
     }
 
     public static boolean getReBroadcast(Message input) {
         return input.ints[input.ints.length - 1] > 0;
     }
     
     public static String getStringFromBody( Message input, int bodyIndex )
     {
         return input.strings[HEADER_LENGTH + bodyIndex];
     }
 
     public static int getIntFromBody( Message input, int bodyIndex )
     {
         return input.ints[HEADER_LENGTH + bodyIndex];
     }
 
     public static MapLocation getLocationFromBody( Message input, int bodyIndex )
     {
         return input.locations[HEADER_LENGTH + bodyIndex];
     }
 
     public static Message stripHeaderAndFooter(Message input) {
         int bodyLength = input.strings.length - HEADER_LENGTH - FOOTER_LENGTH;
 
         if (bodyLength > 0) {
             Message outputMessage = new Message();
 
             outputMessage.strings = new String[bodyLength];
             outputMessage.ints = new int[bodyLength];
             outputMessage.locations = new MapLocation[bodyLength];
 
             System.arraycopy(input.strings, HEADER_LENGTH, outputMessage.strings, 0, bodyLength);
             System.arraycopy(input.ints, HEADER_LENGTH, outputMessage.ints, 0, bodyLength);
             System.arraycopy(input.locations, HEADER_LENGTH, outputMessage.locations, 0, bodyLength);
 
             return outputMessage;
         } else {
             return null;
         }
     }
 
     public static Message mergeMessages( Message inputs[] )
     {
         int totalLength = 0;
 
         for(Message input : inputs)
         {
             if(input != null)
             {
                 totalLength += input.strings.length;
             }
         }
 
         if(totalLength == 0)
         {
             return null;
         }
 
         int currentLocation = 0;
         Message outputMessage = new Message();
 
         outputMessage.strings = new String[totalLength];
         outputMessage.ints = new int[totalLength];
         outputMessage.locations = new MapLocation[totalLength];
 
         for(Message input : inputs)
         {
             if(input != null)
             {
                 int currentLength = input.strings.length;
                 
                 System.arraycopy(input.strings, 0, outputMessage.strings, currentLocation, currentLength);
                 System.arraycopy(input.ints, 0, outputMessage.ints, currentLocation, currentLength);
                 System.arraycopy(input.locations, 0, outputMessage.locations, currentLocation, currentLength);
                 currentLocation += currentLength;
             }
         }
 
         return outputMessage;
     }
 
     public static Message[] splitMessages( Message input )
     {
         int totalLength = input.strings.length;
 
         int messageCounter = 0;
         Message[] messages = new Message[MAX_MESSAGES];
 
         int startOfMessage = 0;
 
         for( int currentLocation = 0; currentLocation < totalLength; currentLocation++ )
         {
            if( input.strings[currentLocation].equals(START_MSG) )
            {
                startOfMessage = currentLocation;
            } else if ( input.strings[currentLocation].equals(END_MSG) )
            {
                int currentLength = currentLocation - startOfMessage;
 
                messages[messageCounter] = new Message();
                messages[messageCounter].strings = new String[currentLength];
                messages[messageCounter].ints = new int[currentLength];
                messages[messageCounter].locations = new MapLocation[currentLength];
 
                System.arraycopy(input.strings, startOfMessage, messages[messageCounter].strings, 0, currentLength);
                System.arraycopy(input.ints, startOfMessage, messages[messageCounter].ints, 0, currentLength);
                System.arraycopy(input.locations, startOfMessage, messages[messageCounter].locations, 0, currentLength);
                messageCounter++;
            }
         }
 
         Message[] outputMessages = new Message[messageCounter];
 
         System.arraycopy(messages, 0, outputMessages, 0, messageCounter);
 
         return outputMessages;
     }
 }
