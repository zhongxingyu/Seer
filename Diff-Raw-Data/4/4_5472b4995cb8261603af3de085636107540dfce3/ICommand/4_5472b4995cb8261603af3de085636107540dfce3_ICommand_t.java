 package com.cloudbees.sdk;
 
import com.cloudbees.sdk.extensibility.ExtensionPoint;

 import java.util.List;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 // TODO: rename it back to Command once we renamed the other Command.
@ExtensionPoint
 public abstract class ICommand {
     /**
      * Executes this command.
      *
      * @param args
      *        Never empty, never null. The first argument is the command name itself,
      *        followed by arguments
      * @return
      *        The exit code of the command. 99 is apparently used for something special that I haven't figured out.
      */
     public abstract int run(List<String> args) throws Exception;
 
     /**
      * Print out the detailed help of this command.
      *
      * @param args
      *      For backward compatibility, this method receives the full argument list
      *      (where the first token is the command name for which the help is requested.)
      */
     public abstract void printHelp(List<String> args);
 }
