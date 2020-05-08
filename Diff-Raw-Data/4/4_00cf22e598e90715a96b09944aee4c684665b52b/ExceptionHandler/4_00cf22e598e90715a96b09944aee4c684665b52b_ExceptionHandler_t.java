 /*
  * This file is part of QuarterBukkit-Plugin.
  * Copyright (c) 2012 QuarterCode <http://www.quartercode.com/>
  *
  * QuarterBukkit-Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * QuarterBukkit-Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with QuarterBukkit-Plugin. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.quarterbukkit.api.exception;
 
 import java.util.logging.Level;
 import org.bukkit.Bukkit;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.AuthorNagException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredListener;
 
 /**
  * A utility class for handling event exceptions ({@link GameException}s).
  */
 public class ExceptionHandler {
 
     /**
      * Handles a {@link GameException} and returns if the process should continue (if the exception is an instance of {@link Cancellable}, else true).
      * 
      * @param exception The {@link GameException} to handle.
      * @return If the process should continue (if the exception is an instance of {@link Cancellable}, else true).
      */
     public static boolean exception(GameException exception) {
 
         call(exception);
 
         if (exception instanceof Cancellable) {
             return ! ((Cancellable) exception).isCancelled();
         } else {
             return true;
         }
     }
 
    // Modified bukkit code
     private static void call(GameException exception) {
 
         HandlerList handlers = exception.getHandlers();
         RegisteredListener[] listeners = handlers.getRegisteredListeners();
 
         for (RegisteredListener registration : listeners) {
             if (!registration.getPlugin().isEnabled()) {
                 continue;
             }
             // Check if the exception belongs to the plugin which registered the listener
             else if (!registration.getPlugin().equals(exception.getPlugin())) {
                 continue;
             }
 
             try {
                 registration.callEvent(exception);
             }
             catch (AuthorNagException e) {
                 Plugin plugin = registration.getPlugin();
 
                 if (plugin.isNaggable()) {
                     plugin.setNaggable(false);
                     Bukkit.getLogger().log(Level.SEVERE, String.format("Nag author(s): '%s' of '%s' about the following: %s", new Object[] { plugin.getDescription().getAuthors(), plugin.getDescription().getFullName(), e.getMessage() }));
                 }
             }
            catch (Exception e) {
                 Bukkit.getLogger().log(Level.SEVERE, "Could not pass exception " + exception.getEventName() + " to " + registration.getPlugin().getDescription().getFullName(), e);
             }
         }
     }
 
     private ExceptionHandler() {
 
     }
 
 }
