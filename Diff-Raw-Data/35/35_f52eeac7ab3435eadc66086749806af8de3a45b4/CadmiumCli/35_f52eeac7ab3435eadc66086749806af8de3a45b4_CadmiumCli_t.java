 package com.meltmedia.cadmium.cli;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameters;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.github.ApiClient;
 
 public class CadmiumCli {
 
 	public static JCommander jCommander = null;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 		  setupSsh();
 		  
 		  jCommander = new JCommander();
 		  
 		  jCommander.setProgramName("cadmium");
 		  
 		  HelpCommand helpCommand = new HelpCommand();
 		  jCommander.addCommand("help", helpCommand);
       
       Map<String, CliCommand> commands = wireCommands(jCommander);
 		  
 		  jCommander.parse(args);
 		  
 		  String commandName = jCommander.getParsedCommand();
 		  if( commandName == null ) {
 		    System.out.println("Please use one of the following commands:");
 		    for(String command : jCommander.getCommands().keySet() ) {
 		      String desc = jCommander.getCommands().get(command).getObjects().get(0).getClass().getAnnotation(Parameters.class).commandDescription();
 		      System.console().format("   %16s    -%s\n", command, desc);
 		    }
 		  } 
 		  else if( commandName.equals("help") ) {
 			  if( helpCommand.subCommand == null || helpCommand.subCommand.size()==0 ) {
 				  jCommander.usage();
 				  return;
 			  }
 			  else {
 				 JCommander subCommander = jCommander.getCommands().get(helpCommand.subCommand.get(0));
 				 if( subCommander == null ) {
 					 System.out.println("Unknown sub command "+helpCommand.subCommand);
 					 return;
 				 }
 				 subCommander.usage();
 				 return;
 			  }
 		  } 
 		  else if(commands.containsKey(commandName)){
 		    CliCommand command = commands.get(commandName);
 		    if(command instanceof AuthorizedOnly) {
 		      setupAuth((AuthorizedOnly) command);
 		    }
 		    command.execute();
 		  }
 		
 		}
 		catch( Exception e ) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 	private static void setupSsh() {
 		File sshDir = new File(System.getProperty("user.home"), ".ssh");
 		if(sshDir.exists()) {
 			GitService.setupLocalSsh(sshDir.getAbsolutePath());
 		}
 	}
 	
 	private static void setupAuth(AuthorizedOnly authCmd) throws Exception {
 	  String token = ApiClient.getToken();
	  if(token != null) {
	    try {
	      new ApiClient(token);
	    } catch(Exception e) {
	      token = null;
	    }
	  }
 	  if(token == null) {
 	    String username = System.console().readLine("Username [github]: ");
 	    String password = new String(System.console().readPassword("Password: "));
 	    List<String> scopes = new ArrayList<String>();
	    scopes.add("repo");
 	    ApiClient.authorizeAndCreateTokenFile(username, password, scopes);
 	    
 	    token = ApiClient.getToken();
 	  }
 	  
 	  if(token != null) {
 	    authCmd.setToken(token);
 	  } else {
 	    System.err.println("Github auth failed");
 	    System.exit(1);
 	  }
 	}
 	
 	private static Map<String, CliCommand> wireCommands(JCommander jCommander) throws Exception {
 	  Map<String, CliCommand> commands = new LinkedHashMap<String, CliCommand>();
 	  String packageName = CadmiumCli.class.getPackage().getName();
 	  String dirName = packageName.replace(".", "/");
 	  Enumeration<URL> resources = CadmiumCli.class.getClassLoader().getResources(dirName);
 	  while(resources.hasMoreElements()) {
 	    URL resource = resources.nextElement();
 	    if(resource.getFile().contains("!")) {
 	      ZipFile resFile = new ZipFile(resource.getFile().split("\\!")[0].substring(5));
 	      Enumeration<? extends ZipEntry> entries = resFile.entries();
 	      while(entries.hasMoreElements()) {
 	        ZipEntry entry = entries.nextElement();
     	    if(entry.getName().startsWith(dirName) && entry.getName().endsWith(".class")) {
     	      String classFileName = entry.getName().substring(0, entry.getName().length() - 6);
     	      try {
       	      String fileName = classFileName.replace("/", ".");
       	      Class<?> classInPackage = Class.forName(fileName);
       	      if(CliCommand.class.isAssignableFrom(classInPackage) && !classInPackage.isInterface()) {
       	        CliCommand command = (CliCommand) classInPackage.newInstance();
       	        
       	        commands.put(command.getCommandName(), command);
       	        jCommander.addCommand(command.getCommandName(), command);
       	      }
     	      } catch(Throwable e) {
     	        // I can't autowire this class.
     	      }
     	    }
 	      }
 	    }
 	  }
 	  return commands;
 	}
 
 }
