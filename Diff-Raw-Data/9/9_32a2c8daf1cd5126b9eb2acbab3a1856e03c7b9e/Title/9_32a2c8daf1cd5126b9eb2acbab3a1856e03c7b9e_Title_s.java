 package modules;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import panacea.MapFunction;
 import static panacea.Panacea.*;
 
 /**
  * Title
  *
  * @author Michael Mrozek
  *         Created May 8, 2011.
  */
 public class Title extends NoiseModule {
 
 	// http://gaming.stackexchange.com/q/21292/3391
 	private static final String[][] lists = {
 		{"Lead", "Senior", "Direct", "Dynamic", "Future", "National", "Regional", "Central", "Global", "Dynamic", "International", "Legacy", "Forward", "Internal", "Chief", "Principal", "Postdoctoral", "Regulatory"},
 		{"Human", "Environmental", "Aerospace", "Space", "Deep Sea", "Atmospheric", "Cardiovascular", "Electrical", "Computer", "Emergency", "Mining", "Nuclear", "Safety", "Histology", "Forensic"},
 		{"Surgeon", "Scientist", "Engineer", "Technologist", "Neurosurgeon", "Pilot", "Astronaut", "Archeologist", "Aviator", "Specialist", "Psychologist", "Composer", "Fighter", "Professional", "Geographer", "Architect", "Astronomer", "Cytogeneticist", "Dentist", "Interpreter", "Phlebotomist", "Physician", "Meteorologist", "Philosopher", "Garbologist"}
 	};
 	
 	@Command("\\.title (.*)")
 	public void title(Message message, String target) {
 		this.bot.sendMessage(target + ": " + implode(map(lists, new MapFunction<String[], String>() {
 			@Override public String map(String[] list) {
 				return getRandom(list);
 			}
 		}), " "));
 	}
 	
 	@Command("\\.title") public void titleSelf(Message message) {
 		this.title(message, message.getSender());
 	}
 	
 	@Override public String getFriendlyName() {return "Title";}
 	@Override public String getDescription() {return "Chooses a random job title for the given nick";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".title -- Display a title for the sending user",
				".title _nick__ -- Display a title for the specified nick",
 		};
 	}
 }
