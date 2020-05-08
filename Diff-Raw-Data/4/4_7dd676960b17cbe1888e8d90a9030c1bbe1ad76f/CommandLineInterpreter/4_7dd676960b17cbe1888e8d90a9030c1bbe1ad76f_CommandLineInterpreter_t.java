 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Scanner;
   
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
 			
 		String commandName = "upload divergence";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = uploadCommand(commandLineArguments, "updiv");
 		}
 			
 		commandName = "updiv";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = uploadCommand(commandLineArguments, "updiv");
 		}
 		
 		commandName = "upload annotation";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = uploadCommand(commandLineArguments, "upano");
 		}
 		
 		commandName = "upano";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = uploadCommand(commandLineArguments, "upano");
 		}
 		
 		commandName = "afs";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = vcfCommand(commandLineArguments);
 		}
 		
 		commandName = "allele frequency spectra";
 		if (isTheCommandName(commandLineArguments, commandName)){
 			result = vcfCommand(commandLineArguments);
 		}
 		
 		commandName = "delete";
 		if (isTheCommandName(commandLineArguments, commandName)){						
 			return deleteCommand(commandLineArguments);
 		}
 		
 		commandName = "view";
 		if(isTheCommandName(commandLineArguments, commandName)){
 			return viewCommand(commandLineArguments);
 		}
 		
 		commandName = "create filter";
 		if(isTheCommandName(commandLineArguments, commandName)){
 			createFilterLoop(commandLineArguments);
 		}
 		
 		commandName = "crefil";
 		if(isTheCommandName(commandLineArguments, commandName)){
 			createFilterLoop(commandLineArguments);
 		}
 		
 		commandName = "filter";
 		if(isTheCommandName(commandLineArguments, commandName)){
 			result = filterCommand(commandLineArguments);
 		}
 		
 		return result;
 	}
 
 	//Fix this, ask Daniel about it
 	private static String viewCommand(String[] commandLineArguments) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//Fix this, ask Daniel about it
 	public static String deleteCommand(final String[] commandLineArguments) {
 		Command makeView = new DeleteCommand(commandLineArguments[1],commandLineArguments[2],commandLineArguments[3]);
 		return makeView.execute();
 	}
 
 	public static void createFilterLoop(final String[] commandLineArguments)
 			throws ClassNotFoundException, SQLException {
 		FilterCreator filter = null;
 		String name = "";
 		
 		for(int i = 0; i < commandLineArguments.length; i++){
 			if(commandLineArguments[i].equals("name") && i != commandLineArguments.length - 1){name = commandLineArguments[i+1];}
 		}
 		
 		if(name.equals("")){
 			System.out.println("Please input a name");
 			return;
 		}
 		
		if(commandLineArguments.length == 4 && commandLineArguments[0].equals("create") && commandLineArguments[1].equals("filter")
				|| commandLineArguments.length == 3 && commandLineArguments[0].equals("crefil")){
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
 			
 			if(additionalArguments.size() == 0){
 				System.out.println("Error: Please input arguments");
 			}else{
 			
 				String[] arguments = new String[additionalArguments.size()];
 				arguments = additionalArguments.toArray(arguments);
 				filter = new FilterCreator(commandLineArguments[0],arguments);
 				filter.uploadEntries();
 			}
 			
 		}else{
 			String[] additionalArguments = new String[commandLineArguments.length-2];
 			
 			for(int i = 0; i < additionalArguments.length; i++){
 				additionalArguments[i] = commandLineArguments[i+2];
 			}
 			
 			filter = new FilterCreator(commandLineArguments[0],additionalArguments);
 			filter.uploadEntries();
 		}
 		
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
 		
 		//Ask about this		
 		return result;
 	}
 
 	private static String vcfCommand(String[] args) throws ClassNotFoundException, SQLException {
 		Command command = null;
 		
 		String result = "";
 		String fileLocation = "";
 		String fileName = "";
 		String filterName = "";
 		
 		for(int i = 0; i < args.length; i++){
 			if(args[i].equals("vcf") && i != args.length - 1){fileName = args[i+1];}
 			if(args[i].equals("write") && i != args.length - 1){fileLocation = args[i+1];}
 			if(args[i].equals("filterby") && i != args.length - 1){filterName = args[i+1];}
 		}
 		
 		if(fileLocation.equals("") && fileName.equals("") && filterName.equals("")){return "Please input proper arguments";}
 		//if(fileLocation.equals("")){return "Please include a file location";}
 		
 		command = new AFSCommand(fileName, fileLocation, filterName);
 		result = command.execute();
 		
 		return result;
 	}
 	
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
 	
 	public static boolean isTheCommandName(String[] command, String commandName){
 		String[] commandNameArray = commandName.split("\\s+");
 		for(int i = 0; i < commandNameArray.length; i++){
 			if(!commandNameArray[i].equals(command[i])){
 				return false;
 			}
 		}
 		return true;
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
