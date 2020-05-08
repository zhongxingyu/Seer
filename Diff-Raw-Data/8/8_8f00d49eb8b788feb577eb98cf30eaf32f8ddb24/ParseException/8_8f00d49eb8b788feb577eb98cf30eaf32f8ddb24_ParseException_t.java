 
 package com.quartercode.quarterbukkit.api.exception;
 
 import org.bukkit.plugin.Plugin;
 import com.quartercode.quarterbukkit.api.Updater;
 
 /**
 * The ParseException occurres if something goes wrong while installing a plugin via {@link Updater}.
  */
 public class ParseException extends GameException {
 
     private final Exception cause;
     private final String    string;
 
     /**
     * Creates a ParseException filled with a cause as an {@link Exception} for the error.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param cause The cause for the error.
      * @param string {@link String} which could not be parsed.
      */
     public ParseException(final Plugin plugin, final Exception cause, final String string) {
 
         super(plugin);
         this.cause = cause;
         this.string = string;
     }
 
     /**
     * Creates a ParseException filled with a cause as an {@link Exception} for the error and an informational message.
      * 
      * @param plugin The causing {@link Plugin}.
      * @param cause The cause for the error.
      * @param string The {@link String} which could not be parsed.
      * @param message The informational message.
      */
     public ParseException(final Plugin plugin, final Exception cause, final String string, final String message) {
 
         super(plugin, message);
         this.cause = cause;
         this.string = string;
     }
 
     /**
      * Returns the cause for the error.
      * 
      * @return The cause for the error as an {@link Exception}.
      */
     public Exception getCause() {
 
         return cause;
     }
 
     /**
      * Returns the {@link String} which could not be parsed.
      * 
      * @return The {@link String} which could not be parsed.
      */
     public String getString() {
 
         return string;
     }
 
 }
