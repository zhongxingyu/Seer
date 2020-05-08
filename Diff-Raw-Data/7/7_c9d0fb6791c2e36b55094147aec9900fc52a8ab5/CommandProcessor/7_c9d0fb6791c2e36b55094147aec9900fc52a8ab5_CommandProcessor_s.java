 /**
  * Copyright (c) 2012 Daniele Pantaleone, Mathias Van Malderen
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  * 
  * @author      Mathias Van Malderen, Daniele Pantaleone
 * @version     1.0
  * @copyright   Mathias Van Malderen, 04 February, 2012
  * @package     com.orion.utility
  **/
 
 package com.orion.utility;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.concurrent.BlockingQueue;
 
 import org.apache.commons.logging.Log;
 
 import com.orion.annotation.Usage;
 import com.orion.bot.Orion;
 import com.orion.command.Command;
 import com.orion.command.RegisteredCommand;
 import com.orion.console.Console;
 import com.orion.exception.CommandRuntimeException;
 import com.orion.exception.CommandSyntaxException;
 import com.orion.plugin.Plugin;
 import com.orion.urt.Color;
 
 public class CommandProcessor implements Runnable {
    
     private final Log log;
     private final Console console;
     private BlockingQueue<Command> commandqueue;
     private MultiKeyMap<String, String, RegisteredCommand> regcommands;
     
     
     /**
      * Object constructor
      * 
      * @author Mathias Van Malderen, Daniele Pantaleone
      * @param  orion <tt>Orion</tt> object reference
      **/
     public CommandProcessor(Orion orion) {
         
         this.log = orion.log;
         this.console = orion.console;
         this.commandqueue = orion.commandqueue;
         this.regcommands = orion.regcommands;
         
         // Printing some info in the log.
         this.log.debug("Command processor initialized [ commands : " + this.regcommands.getMap1().size() + " | buffer : " + (this.commandqueue.remainingCapacity() + this.commandqueue.size()) + " ]");
         
     }
     
     
     /**
      * Runnable implementation<br>
      * Will iterate through all the events stored by the parser in the 
      * command queue. It peeks a command from the queue and process it over 
      * all the mapped Methods retrieved from the registered command map
      * 
      * @author Mathias Van Malderen, Daniele Pantaleone
      **/
     @Override
     public void run() {
         
         // Notifying Thread start in the log file
         this.log.debug("Starting command processor [ systime : " + System.currentTimeMillis() + " ]");
         
         while (true) {
             
             try {
                 
                 Command command = this.commandqueue.take();
                 
                 // If the thread was interrupted, then the event won't be processed
                 // We'll notify it with an Exception so we can break the while cycle
                 // and gracefully stop the running thread
                 if (Thread.interrupted()) throw new InterruptedException();
                 
                 // Checking for a match in the HashMap.
                 if (!this.regcommands.containsKey(command.handle)) {
                     // Informing the client that the command is not mapped
                     this.console.tell(command.client, "Unable to find command: " + Color.YELLOW + command.prefix.name + Color.RED + command.handle);
                     continue;
                 }
                 
                 // Retrieving the correct RegisteredCommand from the HashMap
                 RegisteredCommand regcommand = this.regcommands.get(command.handle);
                 
                 // Checking correct client minLevel
                if ((command.client.group.level < regcommand.minGroup.level) && (!command.force)) {
                    // Informing the client that he has not sufficiend access for this command
                     this.console.tell(command.client, "You have no sufficient access: " + Color.YELLOW + command.prefix.name + Color.RED + command.handle);
                     continue;
                 }
                 
                 try {
                     
                     Method method = regcommand.method;
                     Plugin plugin = regcommand.plugin;
                     
                     // Discard if disabled
                     if (!plugin.isEnabled()) {
                         // Informing the client that the plugin is disabled
                         this.console.tell(command.client, "Unable to execute command " + Color.YELLOW + command.prefix.name + command.handle + ": " + Color.RED + "plugin disabled");
                         continue;
                     }
                     
                     
                     try {
                         
                         // Executing the command. If the command syntax is invalid,
                         // a CommandRuntimeException will be raised, but it will be
                         // incapsulated into an InvocationTargetException object.
                         method.invoke(plugin, command);
                         
                     } catch (InvocationTargetException e) {
                         
                     	if (e.getCause().getClass().equals(CommandRuntimeException.class)) {
                     	
                     		// Displaying the error in the game chat
                         	this.console.tell(command.client, e.getCause().getMessage());
                     		
                     	} else if (e.getCause().getClass().equals(CommandSyntaxException.class)) {
                     
                             // Display a little help text so the user can 
                             // try again using the correct command syntax
                             Usage usage = method.getAnnotation(Usage.class);
                         	this.console.tell(command.client, e.getCause().getMessage());
                             this.console.tell(command.client, "Usage: " + Color.YELLOW + usage.syntax());
                             continue;
                            
                         } else {
                             
                             // Informing the client of the Exception and log it. We'll keep processing anyway...
                             this.console.tell(command.client, "There was an error processing your command");
                             this.log.error("[" + regcommand.plugin.getClass().getSimpleName() + "] Unable to process command " + command.prefix.name + command.handle, e);
                             continue;
                             
                         }
                     
                     }
                     
                 } catch (IllegalAccessException | IllegalArgumentException e) {
                     
                     // Informing the client of the Exception and log it. We'll keep processing anyway...
                     this.console.tell(command.client, "There was an error processing your command");
                     this.log.error("[" + regcommand.plugin.getClass().getSimpleName() + "] Unable to process command " + command.prefix.name + command.handle, e);
                     continue;
                 
                 }
             
             } catch (InterruptedException e) {
                 
                 // Thread has received interrupt signal.
                 // Breaking the cycle so it will terminate.
                 break;
             
             }
         
         }
         
         
         // Notify that the thread is shutting down.
         this.log.debug("Stopping command processor [ systime : " + System.currentTimeMillis() + " ]");
     
     }
 
 }
