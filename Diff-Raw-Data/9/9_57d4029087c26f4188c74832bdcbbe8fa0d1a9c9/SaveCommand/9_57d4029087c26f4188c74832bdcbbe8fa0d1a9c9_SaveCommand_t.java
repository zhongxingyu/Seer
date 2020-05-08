 package ui.command;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Scanner;
 
 import spreadsheet.Application;
 
 public final class SaveCommand
 	extends Command {
 
 private Scanner scanner;
 
 public SaveCommand(Scanner scanner) {
 	
 	this.scanner = scanner;
 }
 	@Override
 	public void execute() {
 	
 		try {		
 			
 			File file = new File(scanner.next());
 					
 			
 			if(!file.exists()) {
 				
 				file.createNewFile();
 			}
 			
 			FileOutputStream fo = new FileOutputStream(file);
 			
 			String saveContent = "";
 			
 			for (String s : Application.saveVariables) {
 				
 				saveContent = saveContent + s + "\n";			
 			}
 			
 			byte[] contentInBytes = saveContent.getBytes();
  
 			fo.write(contentInBytes);
 			fo.flush();
 			fo.close();		
  
 			System.out.println("Done");			
 			
 		} catch (IOException e) {
 			
			System.out.println(e.getMessage());
 		}
 		
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
