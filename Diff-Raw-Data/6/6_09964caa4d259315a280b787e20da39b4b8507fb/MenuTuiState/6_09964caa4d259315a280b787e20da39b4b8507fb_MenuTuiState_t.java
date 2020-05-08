 package de.htwg.seapal.person.views.tui;
 
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 
 class MenuTuiState implements TuiState {
 
 	private PrintStream consoleUi;
 	private IPersonController controller;
 	private Map<Character, MenuTuiCommand> editAttributeCommands;
 
 	public MenuTuiState(IPersonController controller) {
 		this.controller = controller;
 		consoleUi = System.out;
 		
 		addMenuTuiStateCommands();
 	}
 	
 	
 	private void addMenuTuiStateCommands() {
 		editAttributeCommands = new HashMap<Character, MenuTuiCommand>();
 		editAttributeCommands.put('q', new QuitCommand());
 		editAttributeCommands.put('e', new EditPersonCommand());
 		editAttributeCommands.put('a', new AddPersonCommand());
 		editAttributeCommands.put('h', new PrintCommandsMenuTuiCommand());
 	}
 
 
 	@Override
 	public TuiState processInput(String input) {
 		char command = input.charAt(0);
 		MenuTuiCommand tcmd = editAttributeCommands.get(command);
 		if (tcmd == null) {
 			consoleUi.printf("Unknown Command! Pleas try again ... %n");
 			return this;
		}		
		TuiState tSt = tcmd.execute(new Scanner(input.substring(1)));
 
 		return tSt;
 	}
 	
 	
 	private class QuitCommand implements MenuTuiCommand {
 		@Override
 		public TuiState execute(Scanner arguments) {
 			return null;
 		}
 
 		@Override
 		public String getDescription() {
 			return "quit program";
 		}
 	}
 
 	private class EditPersonCommand implements MenuTuiCommand {
 
 		@Override
 		public TuiState execute(Scanner arguments) {
 			String personId = arguments.next();
 			return new EditPersonTuiState(MenuTuiState.this.controller, personId);
 		}
 
 		@Override
 		public String getDescription() {
 			return "edit person with id";
 		}
 	}
 
 
 	private class AddPersonCommand implements MenuTuiCommand {
 		@Override
 		public TuiState execute(Scanner arguments) {
 			String persId = controller.addPerson();
 			consoleUi.printf("New person id: %s %n", persId);
 			return new EditPersonTuiState(MenuTuiState.this.controller, persId);
 		}
 
 		@Override
 		public String getDescription() {
 			return "add person";
 		}
 	}
 	
 	private class PrintCommandsMenuTuiCommand implements MenuTuiCommand {
 		@Override
 		public TuiState execute(Scanner arguments) {
 			for(Entry<Character, MenuTuiCommand> i : editAttributeCommands.entrySet()) {
 				consoleUi.printf("%c - %s %n", i.getKey(), i.getValue().getDescription());
 			}
 			return MenuTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "print commands";
 		}
 	}
 }
