 package model;
 
 import java.util.HashMap;
 import cmd.*;
 
 /**
  * The Client is the main entry point into the htmledit program. It has the main
  * function which starts up the components and wires them together
  * 
  * @author Steven Kroh
  */
 public class Client
 {
     private static HashMap<Class<?>, Command> commands;
 
     public static void main(String[] args)
     {
         Session session = new Session();

         Mediator mediator = new Mediator(session);
         
         // add each command into the HashMap, which will be passed into the GUI.
         // the GUI is able to access commands by their Class @formatter:off
         commands = new HashMap<Class<?>, Command>();
         commands.put(CmdAutoIndent.class, new CmdAutoIndent (mediator));
         commands.put(CmdAutoWrap.class,   new CmdAutoWrap   (mediator));
         commands.put(CmdClose.class,      new CmdClose      (mediator));
         commands.put(CmdCloseAll.class,   new CmdCloseAll   (mediator));
         commands.put(CmdIndent.class,     new CmdIndent     (mediator));
         commands.put(CmdInsert.class,     new CmdInsert     (mediator));
         commands.put(CmdNew.class,        new CmdNew        (mediator));
         commands.put(CmdOpen.class,       new CmdOpen       (mediator));
         commands.put(CmdSave.class,       new CmdSave       (mediator));
         commands.put(CmdSaveAll.class,    new CmdSaveAll    (mediator));
         commands.put(CmdSaveAs.class,     new CmdSaveAs     (mediator));
         commands.put(CmdWellFormed.class, new CmdWellFormed (mediator));
         //@formatter:off
         
         commands.put(CmdSetIndentSpace.class, new CmdSetIndentSpace(mediator));
     }
 }
