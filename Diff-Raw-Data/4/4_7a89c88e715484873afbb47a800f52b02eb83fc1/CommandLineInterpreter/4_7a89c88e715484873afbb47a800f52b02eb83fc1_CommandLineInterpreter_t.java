 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import org.apache.commons.cli.CommandLine;  
 import org.apache.commons.cli.CommandLineParser;  
 import org.apache.commons.cli.GnuParser;  
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;  
 import org.apache.commons.cli.ParseException;
   
 public class CommandLineInterpreter
 {  
 	private static Scanner input;
 
 	/**
      * Use GNU Parser
 	 * Interprets commands given and carries out the proper functions.
 	 * If you want to add a command, add it here as an if statement and
 	 * in the constructGnuOptions function for the program to recognize it.
 	 * @param commandLineArguments
 	 * @throws SQLException 
 	 * @throws ClassNotFoundException 
 	 */
 
 	public static String parseCommand(final String[] commandLineArguments) throws ClassNotFoundException, SQLException{  
 		
 		String result = "";
 			
 		if (commandLineArguments[0].equals("upload") 
 				&& commandLineArguments[1].equals("divergence")){
 			String[] args = new String[commandLineArguments.length - 2];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+2];
 			}
 			result = uploadCommand(commandLineArguments, "updiv");
 		}
 			
 		if (commandLineArguments[0].equals("updiv")){
 			String[] args = new String[commandLineArguments.length - 1];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+1];
 			}
 			result = uploadCommand(commandLineArguments, "updiv");
 		}
 		
 		if (commandLineArguments[0].equals("upload") 
 				&& commandLineArguments[1].equals("annotation")){
 			String[] args = new String[commandLineArguments.length - 2];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+2];
 			}
 			result = uploadCommand(commandLineArguments, "upano");
 		}
 			
 		if (commandLineArguments[0].equals("upano")){
 			String[] args = new String[commandLineArguments.length - 1];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+1];
 			}
 			result = uploadCommand(commandLineArguments, "upano");
 		}
 		
 		if (commandLineArguments[0].equals("afs")){
 			String[] args = new String[commandLineArguments.length - 1];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+1];
 			}
 			result = vcfCommand(commandLineArguments);
 		}
 		
 		if (commandLineArguments[0].equals("allele") 
 				&& commandLineArguments[1].equals("frequency") 
 				&& commandLineArguments[2].equals("spectra")){
 			String[] args = new String[commandLineArguments.length - 3];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+3];
 			}
 			result = vcfCommand(commandLineArguments);
 		}
 		
 		if (commandLineArguments[0].equals("delete")){						
 			Command makeView = new DeleteCommand(commandLineArguments[1],commandLineArguments[2],commandLineArguments[3]);
 			return makeView.execute();
 		}
 		
 		if(commandLineArguments[0].equals("create")
 				&& commandLineArguments[1].equals("filter")){
 			FilterCreator filter = null;
 			if(commandLineArguments.length == 2){
 				input = new Scanner(System.in);
 				ArrayList<String> additionalArguments = new ArrayList<String>();
 				System.out.println("Please input additional arguments for creating a filter. Enter 'done' or hit enter twice when finished.");
 				while(true){
 					System.out.print(">> ");
 					String line = input.nextLine().trim();
 					if(line.equals("done") || line.equals("")){
 						break;
 					}
 					System.out.println(line);
 					additionalArguments.add(line);
 				}
 				String[] arguments = new String[additionalArguments.size()];
 				arguments = additionalArguments.toArray(arguments);
 				filter = new FilterCreator(commandLineArguments[0],arguments);
 			}else{
 				String[] additionalArguments = new String[commandLineArguments.length-2];
 				
 				for(int i = 0; i < additionalArguments.length; i++){
 					additionalArguments[i] = commandLineArguments[i+2];
 				}
 				
 				filter = new FilterCreator(commandLineArguments[0],additionalArguments);
 			}
 			filter.uploadEntries();
 		}
 		
 		if(commandLineArguments[0].equals("crefil")){
 			FilterCreator filter = null;
 			if(commandLineArguments.length == 1){
 				input = new Scanner(System.in);
 				ArrayList<String> additionalArguments = new ArrayList<String>();
 				System.out.println("Please input additional arguments for creating a filter. Enter 'done' or hit enter twice when finished.");
 				while(true){
 					System.out.print(">> ");
 					String line = input.nextLine().trim();
 					if(line.equals("done") || line.equals("")){
 						break;
 					}
 					System.out.println(line);
 					additionalArguments.add(line);
 				}
 				String[] arguments = new String[additionalArguments.size()];
 				arguments = additionalArguments.toArray(arguments);
 				filter = new FilterCreator(commandLineArguments[0],arguments);
 			}else{
 				String[] additionalArguments = new String[commandLineArguments.length-1];
 				
 				for(int i = 0; i < additionalArguments.length; i++){
 					additionalArguments[i] = commandLineArguments[i+1];
 				}
 				
 				filter = new FilterCreator(commandLineArguments[0],additionalArguments);
 			}
 			filter.uploadEntries();
 		}
 		
 		if(commandLineArguments[0].equals("filter")){
 			String[] args = new String[commandLineArguments.length - 1];
 			for(int i = 0; i < args.length; i++){
 				args[i] = commandLineArguments[i+1];
 			}
 			result = filterCommand(commandLineArguments);
 		}
 			/*if (commandLine.hasOption("upano")){
 				result = uploadCommand(commandLineArguments, commandLine, "upano");
 			}
 			
 			//allows for three arguments, afs(vcfname, filename, filtername)
 			if (commandLine.hasOption("asf")){
 				String[] args = commandLine.getOptionValues("asf");
 				Command command = null;
 				if(args.length == 2) command = new AFSCommand(args[0], args[1], "");
 				if(args.length == 3) command = new AFSCommand(args[0], args[1], args[2]);
 				result = command.execute();
 			}
 			
 			//Allow for two optional arguments--
 			if (commandLine.hasOption("filterWrite")){
 				String[] args = commandLine.getOptionValues("filterWrite");
 				Command command = null;
 				if(args.length == 3) command = new FilterWriteApplier(args[0], args[1], args[2]);
 				result = command.execute();
 			}
 			
 			if (commandLine.hasOption("filterStore")){
 				String[] args = commandLine.getOptionValues("filterStore");
 				Command command = null;
 				if(args.length == 2) command = new FilterStoreApplier(args[0], args[1]);
 				result = command.execute();
 			}
 			
 			if(commandLine.hasOption("createfilter")){
 				String[] args = commandLine.getOptionValues("createfilter");
 				FilterCreator filter = null;
 				if(args.length == 1){
 					input = new Scanner(System.in);
 					ArrayList<String> additionalArguments = new ArrayList<String>();
 					System.out.println("Please input additional arguments for creating a filter. Enter 'done' or hit enter twice when finished.");
 					while(true){
 						System.out.print(">> ");
 						String line = input.nextLine().trim();
 						if(line.equals("done") || line.equals("")){
 							break;
 						}
 						System.out.println(line);
 						additionalArguments.add(line);
 					}
 					String[] arguments = new String[additionalArguments.size()];
 					arguments = additionalArguments.toArray(arguments);
 					filter = new FilterCreator(args[0],arguments);
 				}else{
 					String[] additionalArguments = new String[args.length-1];
 					
 					for(int i = 0; i < additionalArguments.length; i++){
 						additionalArguments[i] = args[i+1];
 					}
 					
 					filter = new FilterCreator(args[0],additionalArguments);
 				}
 				filter.uploadEntries();
 			}
 			
 			if (commandLine.hasOption("sum")){
 				
 				String[] stringNumbers = commandLine.getOptionValues("sum");
 				int sum = 0;
 
 				for(int i = 0; i < stringNumbers.length; i++){
 					sum += Integer.parseInt(stringNumbers[i]);
 				}
         	 
 				System.out.println(sum);
 			}
 			
 			if (commandLine.hasOption("view")){
 				String[] args = commandLine.getOptionValues("view");
 						
 				Command makeView = new View(args[0],args[1]);
 				return makeView.execute();
 			}
 			
 			if (commandLine.hasOption("delete")){
 				String[] args = commandLine.getOptionValues("delete");
 						
 				Command makeView = new DeleteCommand(args[0],args[1],args[2]);
 				return makeView.execute();
 			}
 			
 			if (commandLine.hasOption("help")){
 				
 				/*
 				 * Expand to a more general help function
 				 */
 				
 			/*	System.out.println("hello\nn <arg>\nsum <arg0> <arg1> <arg2> ...");
 			}*/
 		//}
       
 		/*catch (ParseException parsingException){  
 			System.err.println("Could not find argument: " + parsingException.getMessage());  
 		}*/
 		
 		return result;
 	}  
 	
 	private static String filterCommand(String[] args) {
 		FilterApplier applier  = null;
 		
 		String result = "";
 		String store = "";
 		String write = "";
 		String by = "";
 		String vcf = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("store") && i != args.length - 1){store = args[i+1];}
 			if(args[i].equals("write") && i != args.length - 1){write = args[i+1];}
 			if(args[i].equals("by") && i != args.length - 1){by = args[i+1];}
 			if(args[i].equals("vcf") && i != args.length - 1){vcf = args[i+1];}
 		}
 		
 		if(vcf.equals("")){return "Please include the vcf that is being used.";}
 		if(store.equals("") && write.equals("")){return "Please include the file name output.";}
 		if(by.equals("")){return "Please include what this is being filtered by";}
 		
 		if(!write.equals("")){applier = new FilterWriteApplier(vcf, by, write);}
 		if(!store.equals("")){applier = new FilterWriteApplier(vcf, by, store);}
 		
 		return result;
 	}
 
 	private static String vcfCommand(String[] args) throws ClassNotFoundException, SQLException {
 		Command command = null;
 		
 		String result = "";
 		String fileLocation = "";
 		String fileName = "";
 		String filterName = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("name") && i != args.length - 1){fileName = args[i+1];}
 			if(args[i].equals("file") && i != args.length - 1){fileLocation = args[i+1];}
 			if(args[i].equals("filter") && i != args.length - 1){filterName = args[i+1];}
 		}
 		
 		if(fileLocation.equals("") && fileName.equals("") && filterName.equals("")){return "Please input proper arguments";}
 		if(fileLocation.equals("")){return "Please include a file location";}
 		
 		command = new AFSCommand(fileName, fileLocation, filterName);
 		result = command.execute();
 		
 		return result;
 	}
 
 	/**
 	 * Uploads either divergence file or annotation file
 	 * @param commandLineArguments
 	 * @param commandLine
 	 * @param type
 	 * @return the name of the upload
 	 */
 	
 	public static String uploadCommand(final String[] args, String type){
 		Command command = null;
 
 		String result = "";
 		String fileLocation = "";
 		String fileName = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("name") && i != args.length - 1){fileName = args[i+1];}
 			if(args[i].equals("file") && i != args.length - 1){fileLocation = args[i+1];}
 		}
 		
 		if(fileLocation.equals("") && fileName.equals("")){return "Please input proper arguments";}
 		if(fileLocation.equals("")){return "Please include a file location.";}
 		
 		if(type=="updiv"){command = new UploadDivergenceCommand(fileLocation, null,fileName);}
 		if(type=="upano"){command = new UploadAnnotationCommand(fileLocation, null,fileName);}
 			
 		result = command.execute();
 		
 		return result;
 	}
   
 
    /**
     * Prints out the commands the user input.
     */
    
    public static void displayInput(final String[] commandLineArguments){  
 	   
 	   int length = commandLineArguments.length;
 	   String output = "";
 	   
 	   for(int i = 0; i < length; i++){
 		   output += commandLineArguments[i];
 		   output += " ";
 	   }
 	   
 	   System.out.println(output);
    }
    
    /**
     * This is the method that should be called by outside methods and classes
     * to run all commands.
  * @throws SQLException 
  * @throws ClassNotFoundException 
     */
    
    public static String interpreter(String[] commandLineArguments) throws ClassNotFoundException, SQLException{
 	    if (commandLineArguments.length < 1)  
 	      {  
 	         System.out.println("Please input help"); 
 	      }  
 	      //displayInput(commandLineArguments);
 	      //System.out.println("");
 	      return parseCommand(commandLineArguments);  
    }
    
 	public static void main(String[] args) throws ClassNotFoundException, SQLException{

		System.out.println(interpreter(args));
 	
 	}
 } 
