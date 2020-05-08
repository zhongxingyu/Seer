 package de.htwg.seapal.person.views.tui;
 
 import java.io.PrintStream;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 
 class EditPersonTuiState implements TuiState {
 
 	private IPersonController controller;
 	private String personId;
 	private Map<Character, TuiEditAttributeCommand> editAttributeCommands;
 	private PrintStream consoleUi;
 
 	public EditPersonTuiState(IPersonController controller, String personId) {
 		this.personId = personId;
 		this.controller = controller;
 		this.consoleUi = System.out;
 		
 		addPersonTuiStateCommands();
 	}
 	
 	private void addPersonTuiStateCommands() {
 		editAttributeCommands = new HashMap<Character, TuiEditAttributeCommand>();
 		editAttributeCommands.put('f', new SetPersonFirstNameCommand());
 		editAttributeCommands.put('l', new SetPersonLastNameCommand());
 		editAttributeCommands.put('b', new SetPersonBirthCommand());
 		editAttributeCommands.put('a', new SetPersonAgeCommand());
 		editAttributeCommands.put('r', new SetPersonRegistrationCommand());
 		editAttributeCommands.put('n', new SetPersonNationalityCommand());
 		editAttributeCommands.put('e', new SetPersonEmailCommand());
 		editAttributeCommands.put('t', new SetPersonTelephoneCommand());
 		editAttributeCommands.put('m', new SetPersonMobileCommand());
 		editAttributeCommands.put('s', new SetPersonStreetCommand());
 		editAttributeCommands.put('p', new SetPersonPostcodeCommand());
 		editAttributeCommands.put('c', new SetPersonCityCommand());
 		editAttributeCommands.put('o', new SetPersonCountryCommand());
 		editAttributeCommands.put('d', new PrintPersonCommand());
 		editAttributeCommands.put('h', new PrintCommandsTuiEditAttributeCommand());
 		editAttributeCommands.put('q', new ReturnToMenuCommand());
 		
 	}
 
 	@Override
 	public TuiState processInput(String input) {
 		char command = input.charAt(0);
 		TuiEditAttributeCommand tcmd = editAttributeCommands.get(command);
 		if (tcmd == null) {
 			consoleUi.printf("Unknown Command! Pleas try again ... %n");
 			return this;
 		}
		return tcmd.execute(personId, input.substring(1));
 	}
 	
 	
 	
 	private class SetPersonFirstNameCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonFirstname(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set first name";
 		}
 	}
 	
 	private class SetPersonLastNameCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonLastname(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set last name";
 		}
 	}
 
 	private class SetPersonBirthCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonBirth(personId, parseDate(value));
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set birth date";
 		}
 	}
 
 	private class SetPersonAgeCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) throws NumberFormatException {
 			controller.setPersonAge(personId, Integer.parseInt(value));
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set age";
 		}
 	}
 
 	private class SetPersonRegistrationCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonRegistration(personId, parseDate(value));
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set registration";
 		}
 	}
 
 	private class SetPersonNationalityCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonNationality(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set nationality";
 		}
 	}
 
 	private class SetPersonEmailCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonEmail(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set email";
 		}
 	}
 
 	private class SetPersonTelephoneCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonTelephone(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set telephone";
 		}
 	}
 
 	private class SetPersonMobileCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonMobile(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set mobile";
 		}
 	}
 
 	private class SetPersonStreetCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonStreet(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set street";
 		}
 	}
 
 	private class SetPersonPostcodeCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) throws NumberFormatException {
 			controller.setPersonPostcode(personId, Integer.parseInt(value));
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set postcode";
 		}
 	}
 
 	private class SetPersonCityCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonCity(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set city";
 		}
 	}
 
 	private class SetPersonCountryCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			controller.setPersonCountry(personId, value);
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "set country";
 		}
 	}
 	
 	private class PrintPersonCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			System.out.printf(controller.getPersonString(personId) + "%n");
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "print person";
 		}
 	}
 	
 	private class ReturnToMenuCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			return new MenuTuiState(controller);
 		}
 
 		@Override
 		public String getDescription() {
 			return "return to menu";
 		}
 	}
 	
 	private class PrintCommandsTuiEditAttributeCommand implements TuiEditAttributeCommand {
 		@Override
 		public TuiState execute(String personId, String value) {
 			for(Entry<Character, TuiEditAttributeCommand> i : editAttributeCommands.entrySet()) {
 				consoleUi.printf("%c - %s %n", i.getKey(), i.getValue().getDescription());
 			}
 			return EditPersonTuiState.this;
 		}
 
 		@Override
 		public String getDescription() {
 			return "print commands";
 		}
 	}
 	
 	
 	private Date parseDate(String input) {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 
 		Date date = null;
 
 		try {
 			date = dateFormat.parse(input);
 		} catch (ParseException e) {
 			date = new Date();
 		}
 
 		return date;
 	}
 
 }
