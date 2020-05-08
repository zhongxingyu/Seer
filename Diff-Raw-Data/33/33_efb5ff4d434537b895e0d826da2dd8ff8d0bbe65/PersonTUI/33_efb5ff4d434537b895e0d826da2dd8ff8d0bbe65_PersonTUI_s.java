 package de.htwg.seapal.person.views.tui;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Scanner;
 
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.util.observer.Event;
 import de.htwg.seapal.person.util.observer.IObserver;
 
 
 public class PersonTUI implements IObserver{
 	
 	private IPersonController controller;
 
 	public PersonTUI(IPersonController controller) {
 		this.controller = controller;
 		controller.addObserver(this);
 	}
 
 	public void update(Event e) {
 		printTUI();
 	}
 
 	public boolean processInputLine(String line) {
 		
 		boolean continu = true;
 		
 		if (line.equalsIgnoreCase("q")) {
 			continu = false;
 		}
 		
 		if (line.equalsIgnoreCase("f")) {
 			System.out.print("Firstname: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonFirstname(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("l")) {
 			System.out.print("Lastname: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonLastname(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("b")) {
 			System.out.print("Birth (dd.mm.yyyy): ");
 			
 			Scanner scanner = new Scanner(System.in);
 			String input = scanner.next();
 			
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 			
 			Date date = null;
 			
 			try {
 				date = dateFormat.parse(input);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			controller.setPersonBirth(date);
 		}
 		
 		if (line.equalsIgnoreCase("r")) {
 			System.out.print("Registration (dd.mm.yyyy): ");
 			
 			Scanner scanner = new Scanner(System.in);
 			String input = scanner.next();
 			
 			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
 			
 			Date date = null;
 			
 			try {
 				date = dateFormat.parse(input);
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 			
 			controller.setPersonRegistration(date);
 		}
 		
 		if (line.equalsIgnoreCase("a")) {
 			System.out.print("Age: ");
 			Scanner scanner = new Scanner(System.in);
 			Integer age = new Integer(scanner.next());
 			controller.setPersonAge(age.intValue());
 		}
 		
 		if (line.equalsIgnoreCase("n")) {
 			System.out.print("Nationality: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonNationality(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("e")) {
 			System.out.print("Email: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonEmail(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("t")) {
 			System.out.print("Telephone: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonTelephone(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("m")) {
 			System.out.print("Mobile: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonMobile(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("s")) {
 			System.out.print("Street: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonStreet(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("p")) {
 			System.out.print("Postcode: ");
 			Scanner scanner = new Scanner(System.in);
 			Integer postcode = new Integer(scanner.next());
 			controller.setPersonPostcode(postcode.intValue());
 		}
 		
 		if (line.equalsIgnoreCase("c")) {
 			System.out.print("City: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonCity(scanner.next());
 		}
 		
 		if (line.equalsIgnoreCase("o")) {
 			System.out.print("Country: ");
 			Scanner scanner = new Scanner(System.in);
 			controller.setPersonCountry(scanner.next());
 		}
 
 		return continu;
 	}
 	
 	public void printTUI() {
 		System.out.println("PersonDemo: ");
 		System.out.println("f - set Firstname");
 		System.out.println("l - set Lastname");
 		System.out.println("b - set Birth");
 		System.out.println("r - set Registration");
 		System.out.println("a - set Age");
 		System.out.println("n - set Nationality");
 		System.out.println("e - set Email");
 		System.out.println("t - set Telephone");
 		System.out.println("m - set Mobile");
 		System.out.println("s - set Street");
 		System.out.println("p - set Postcode");
 		System.out.println("c - set City");
 		System.out.println("o - set Country");
 		System.out.println();
 		System.out.println(controller.getPersonString());
 		System.out.print("Command: ");
 	}
 
 }
