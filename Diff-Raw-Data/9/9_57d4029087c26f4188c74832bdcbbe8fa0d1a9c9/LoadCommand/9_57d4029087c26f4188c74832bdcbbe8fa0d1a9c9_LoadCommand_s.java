 package ui.command;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.FileInputStream;
 import java.io.IOException;
 import spreadsheet.Application;
 import ui.CommandInterpreter;
 
 public final class LoadCommand
 	extends Command {
 
 	
 	private Scanner scanner;
 	public LoadCommand(Scanner scanner) {
 		
 		this.scanner = scanner;
 	}	
 	
 	@Override
 	public void execute() {
 		
 		File file = new File(scanner.next());
 		
 		try {
 			
 			FileInputStream fs = new FileInputStream(file);	
             Scanner readScanner = new Scanner(fs);
 			
 			while (readScanner.hasNext()) {
 				
 				CommandInterpreter.interpret(new Scanner(readScanner.nextLine())).execute();			
 			}
 					
 			readScanner.close();
 			
 		} catch (IOException e) {
 			
			e.printStackTrace();
 		}
 
 	}
 	
 }
 
