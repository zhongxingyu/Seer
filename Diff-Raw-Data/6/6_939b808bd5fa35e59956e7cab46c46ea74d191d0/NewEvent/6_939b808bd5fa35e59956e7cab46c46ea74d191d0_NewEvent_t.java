 package fi.helsinki.cs.scheduler3000.cli;
 
 import fi.helsinki.cs.scheduler3000.model.Event;
 import fi.helsinki.cs.scheduler3000.model.Schedule;
 import fi.helsinki.cs.scheduler3000.model.Weekday.Day;
 
 public class NewEvent extends CliCommand {
 	
 	NewEvent(Schedule schedule) {
 		this.schedule = schedule;
 	}
 	
 	void run() {
 		newEventDialog();
 	}
 	
 	private void newEventDialog() {
 		int startTime = -1;
 		int endTime = -1;
 		String location, title, eventDayTemp;
 		Day eventDay = null;
 
 		// validate user input in place
 		while( eventDay == null ) {
 			
 			System.out.println("Which day is the event?");
 			Helpers.printDates();
 			printPrompt();
 			eventDayTemp =  input.nextLine();
 
 			if (eventDayTemp.equals(endCommand)){
 				return;
 			}
 			
			try {
				eventDay = Helpers.getDay(eventDayTemp);
			} catch (Exception e) {
				System.out.println("Invalid date. Try again, please");
			}
 		}
 
 		while( !Event.isValidStartTime( startTime ) ) {
 			System.out.println("What is the start time?");
 			printPrompt();
 			startTime = Integer.parseInt( input.nextLine() );
 		}
 		
 		while( !Event.isValidEndTime( startTime, endTime ) ) {
 
 			System.out.println("What is the end time?");
 			printPrompt();
 			endTime = Integer.parseInt( input.nextLine() );
 		}
 
 		System.out.println("What this event should be named as?");
 		System.out.println("(just press enter to skip this)");
 		printPrompt();
 		title = input.nextLine();
 
 		System.out.println("Where this event is held?");
 		System.out.println("(just press enter to skip this)");
 		printPrompt();
 		location = input.nextLine();
 
 
 		System.out.print("Adding event to schedule...");
 
 		this.execute(eventDay, title, location, startTime, endTime);
 		System.out.println("ok!");
 
 	}
 	
 	private void execute(Day day, String title, String location, int startTime, int endTime) {
 		Event event = new Event(day, title, location, startTime, endTime);
 		schedule.addEvent(event);
 	}
 
 }
