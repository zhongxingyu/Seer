 /**
  * Copyright 2013 Tommi S.E. Laukkanen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.agocontrol.agent;
 
 import org.agocontrol.client.CommandListener;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The shell command listener.
  *
  * @author Tommi S.E. Laukkanen
  */
 public class ShellCommandListener implements CommandListener {
     /** The logger. */
     private static final Logger LOGGER = Logger.getLogger(ShellCommandListener.class);
     /** List of allowed characters in shell command parameters. */
     private static final String ALLOWD_CHARACTERS_STRING =
             "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!., ";
     /** Lookup set for allowed characters. */
     private static final Set<Character> ALLOWED_CHARACTERS = new HashSet<Character>();
     static {
         for (final Character character : ALLOWD_CHARACTERS_STRING.toCharArray()) {
             ALLOWED_CHARACTERS.add(character);
         }
     }
 
     @Override
     public final Map<String, Object> commandReceived(final Map<String, Object> parameters) {
         LOGGER.debug("Received command: " + parameters.toString());
 
         final String command = (String) parameters.get("command");
 
         if ("screenon".equals(command)) {
            executeShellCommand("xset -display :0 dpms force on");
         }
 
         if ("screenoff".equals(command)) {
            executeShellCommand("xset -display :0 dpms force off");
         }
 
         if ("say".equals(command)) {
             final String message = sanitize((String) parameters.get("message"));
             if (message.length() > 0) {
                executeShellCommand("aoss flite -t '" + message + "' -voice slt");
             }
         }
 
         return null;
     }
 
     /**
      * Sanitizes text to remove any characters which could be harmful when used as shell command parameters.
      *
      * @param text the text to sanitize
      * @return sanitized text
      */
     private String sanitize(final String text) {
         if (text == null) {
             return "";
         }
         final StringBuilder stringBuilder = new StringBuilder();
         for (final Character character : text.toCharArray()) {
             if (ALLOWED_CHARACTERS.contains(character)) {
                 stringBuilder.append(character);
             }
         }
         return stringBuilder.toString().trim();
     }
 
 
     /**
      * Executes requested shell command.
      *
      * @param cmd the shell command to execute
      */
     private void executeShellCommand(final String cmd) {
         LOGGER.debug("Executing: " + cmd);
         try {
             Runtime runtime = Runtime.getRuntime();
             Process process = runtime.exec(new String[] {"/bin/sh", "-c", cmd});
             process.waitFor();
 
             String line;
 
             BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
             while ((line = error.readLine()) != null) {
                 LOGGER.error(line);
             }
             error.close();
 
             BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
             while ((line = input.readLine()) != null) {
                 LOGGER.debug(line);
             }
 
             input.close();
 
             LOGGER.debug("Completed: " + cmd);
         } catch (final Throwable t) {
             LOGGER.error("Error executing shell command: " + cmd, t);
         }
     }
 }
