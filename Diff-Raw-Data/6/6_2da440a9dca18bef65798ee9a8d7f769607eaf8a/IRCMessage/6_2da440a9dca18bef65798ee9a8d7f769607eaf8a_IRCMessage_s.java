 package com.dwarfcrank.kemubotti.irc;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  *
  * @author dwarfcrank
  */
 public class IRCMessage {
 
     /**
      * The source of this message. Empty if sending the message.
      */
     protected String prefix;
     /**
      * The command in this message. Must not be empty.
      */
     protected String command;
     /**
      * The command parameters. May be null.
      */
     protected String[] parameters;
 
     public String getCommand() {
         return command;
     }
 
     public String[] getParameters() {
         return parameters;
     }
 
     public String getPrefix() {
         return prefix;
     }
 
     private IRCMessage(String prefix, String command, String... parameters) {
         this.prefix = prefix;
         this.command = command;
         this.parameters = parameters;
     }
 
     /**
      * Creates a new message to be sent to the server.
      *
      * @param command The command to be sent
      * @param parameters The parameters associated with the command
      */
     public IRCMessage(String command, String... parameters) {
         // Messages originating from the client are not allowed to specify
         // the prefix, so create the message with an empty prefix.
         this("", command, parameters);
     }
 
     private static String parsePrefix(String text) {
         // Check for prefix
         // The presence of a prefix is denoted by a colon at the beginning of
         // the line.
         if (text.startsWith(":")) {
             String prefix = text.substring(1);
 
             return prefix;
         }
 
         return "";
     }
 
     // TODO: These two methods are somewhat nasty. Get rid of them.
     private static String combineParameters(String[] parameters, int startIndex) {
         StringBuilder builder = new StringBuilder();
 
         // The element at the start index begins with a colon, strip it away
         String start = parameters[startIndex].substring(1);
         builder.append(start);
 
         for (int i = startIndex + 1; i < parameters.length; i++) {
             builder.append(' ');
             builder.append(parameters[i]);
         }
 
         return builder.toString();
     }
 
     private static String[] processParameters(String[] parameters) {
         // First check if parameters need to be concatenated at all
         boolean needsProcessing = false;
 
         for (String s : parameters) {
             if (s.startsWith(":")) {
                 needsProcessing = true;
                 break;
             }
         }
 
         if (!needsProcessing) {
             // No need to do anything, return the original parameters
             return parameters;
         }
 
         ArrayList<String> newParameters = new ArrayList<String>();
 
         for (int i = 0; i < parameters.length; i++) {
             if (parameters[i].startsWith(":")) {
                 // Found the starting point of a whole string, combine it
                 String combined = combineParameters(parameters, i);
 
                 newParameters.add(combined);
                 break;
             }
 
             newParameters.add(parameters[i]);
         }
 
         String[] array = newParameters.toArray(parameters);
         return Arrays.copyOfRange(array, 0, newParameters.size());
     }
 
     /**
      * Parses a line into an IRCMessage object.
      *
      * @param line The line read from the IRC stream.
      * @return The parsed message
      */
     public static IRCMessage parseMessage(String line) {
         String[] parts = line.split(" ");
         String[] parameters;
 
         String prefix = parsePrefix(parts[0]);
         String command;
 
         // Assuming the prefix is present, the first element of the parameter
         // array is the third element of the whole split message.
         int paramsStartIndex = 2;
 
         // If the prefix is empty, the first part is the command
         if (prefix.isEmpty()) {
             command = parts[0];
 
             // Prefix is not present so adjust paramsStartIndex accordingly
             paramsStartIndex = 1;
         } else {
             command = parts[1];
         }
 
         // Copy the rest of the parameters into a new array.
         parameters = Arrays.copyOfRange(parts, paramsStartIndex, parts.length);
 
         // Combine the parameter strings according to the protocol rules
        parameters = processParameters(parameters);
 
         return new IRCMessage(prefix, command, parameters);
     }
 
     /**
      * Returns the string representation of this message, ready to be written to
      * the server stream.
      * @return
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.ensureCapacity(512);
 
         if (!prefix.isEmpty()) {
             sb.append(prefix).append(' ');
         }
         
         sb.append(command);
 
         for (String parameter : parameters) {
             sb.append(' ').append(parameter);
         }
 
         return sb.toString();
     }
 }
