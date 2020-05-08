 /*
  * Copyright (c) 2002-2012, the original author or authors.
  * This software is distributable under the BSD license. See the terms of the
  * BSD license in the documentation provided with this software.
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package org.bonitasoft.engine;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 
 import jline.console.ConsoleReader;
 
 import org.apache.commons.io.FileUtils;
 import org.bonitasoft.engine.command.HelpCommand;
 import org.bonitasoft.engine.command.ShellCommand;
 import org.bonitasoft.engine.completer.CommandArgumentsCompleter;
 
 /**
  * A basic shell
  * Implement abstract methods
  * to run it just execute (e.g. in a main)
  * shell.run();
  * 
  * @author Baptiste Mesta
  */
 public abstract class BaseShell<T extends ShellContext> {
 
     private static final String PROMPT = "bonita> ";
 
     static final String HELP = "help";
 
     private HashMap<String, ShellCommand<T>> commands;
 
     private HelpCommand<T> helpCommand;
 
     private File homeFoler;
 
     public void init() throws Exception {
         final List<ShellCommand<T>> commandList = initShellCommands();
         commands = new HashMap<String, ShellCommand<T>>();
         for (final ShellCommand<T> shellCommand : commandList) {
             commands.put(shellCommand.getName(), shellCommand);
         }
         helpCommand = getHelpCommand();
         if (helpCommand != null) {
             commands.put(helpCommand.getName(), helpCommand);
         }
         initHome();
 
     }
 
     /**
      * return the help command used
      * Can be overridden
      */
     protected HelpCommand<T> getHelpCommand() {
         return new HelpCommand<T>(commands);
     }
 
     /**
      * @return
      *         list of commands contributed to the shell
      * @throws Exception
      */
     protected abstract List<ShellCommand<T>> initShellCommands() throws Exception;
 
     /**
      * called by {@link BaseShell} when the shell is exited
      * 
      * @throws Exception
      */
     public void destroy() throws Exception {
         cleanHome();
     }
 
     public void run(final InputStream in, final OutputStream out) throws Exception {
         init();
         printWelcomeMessage();
         final ConsoleReader reader = new ConsoleReader(in, out);
         reader.setBellEnabled(false);
         final CommandArgumentsCompleter<T> commandArgumentsCompleter = new CommandArgumentsCompleter<T>(commands);
 
         reader.addCompleter(commandArgumentsCompleter);
 
         String line;
         while ((line = reader.readLine("\n" + getPrompt())) != null) {
             final List<String> args = parse(line);
             final String command = args.remove(0);
             if (commands.containsKey(command)) {
                 final ShellCommand<T> clientCommand = commands.get(command);
                 if (clientCommand.validate(args)) {
                     try {
                         clientCommand.execute(args, getContext());
                     } catch (final Exception e) {
                         e.printStackTrace();
                     }
                 } else {
                     clientCommand.printHelp();
                 }
             } else if ("exit".equals(line)) {
                 System.out.println("Exiting application");
                 destroy();
                 return;
             } else {
                 System.out.println("Wrong argument");
                 helpCommand.printHelp();
             }
         }
         destroy();
     }
 
     /**
      * @return
      */
     protected abstract T getContext();
 
     /**
      * used to parse arguments of the line
      * 
      * @param line
      * @return
      */
     protected List<String> parse(final String line) {
        return new ArrayList<String>(Arrays.asList(line.trim().split("(\\s)+")));
     }
 
     protected void initHome() throws IOException {
         homeFoler = null;
         if (System.getProperty("bonita.home") == null) {
             homeFoler = new File("home");
             FileUtils.deleteDirectory(homeFoler);
             homeFoler.mkdir();
             File file = new File(homeFoler, "client");
             file.mkdir();
             file = new File(file, "conf");
             file.mkdir();
             file = new File(file, "bonita-client.properties");
             file.createNewFile();
             final Properties properties = new Properties();
             properties.load(this.getClass().getResourceAsStream("/bonita-client.properties"));
             final String application = System.getProperty("shell.application");
             if (application != null) {
                 properties.put("application.name", application);
             }
             final String host = System.getProperty("shell.host");
             final String port = System.getProperty("shell.port");
             properties.put("server.url", "http://" + (host != null ? host : "localhost") + ":" + (port != null ? port : "8080"));
 
             final FileWriter writer = new FileWriter(file);
             try {
                 properties.store(writer, "Server configuration");
             } finally {
                 writer.close();
             }
             System.out.println("Using server configuration " + properties);
             System.setProperty("bonita.home", homeFoler.getAbsolutePath());
         }
     }
 
     protected void cleanHome() throws IOException {
         if (homeFoler != null) {
             FileUtils.deleteDirectory(homeFoler);
         }
     }
 
     protected String getPrompt() {
         return PROMPT;
     }
 
     protected void printWelcomeMessage() {
         System.out.println("Welcome to Bonita Shell.\n For assistance press TAB or type \"help\" then hit ENTER.");
         System.out.println("______             _ _        _____ _          _ _ ");
         System.out.println("| ___ \\           (_) |      /  ___| |        | | |");
         System.out.println("| |_/ / ___  _ __  _| |_ __ _\\ `--.| |__   ___| | |");
         System.out.println("| ___ \\/ _ \\| '_ \\| | __/ _` |`--. \\ '_ \\ / _ \\ | |");
         System.out.println("| |_/ / (_) | | | | | || (_| /\\__/ / | | |  __/ | |");
         System.out.println("\\____/ \\___/|_| |_|_|\\__\\__,_\\____/|_| |_|\\___|_|_|");
         System.out.println("");
         System.out.println("");
         System.out.println("");
         System.out.println("");
     }
 
 }
