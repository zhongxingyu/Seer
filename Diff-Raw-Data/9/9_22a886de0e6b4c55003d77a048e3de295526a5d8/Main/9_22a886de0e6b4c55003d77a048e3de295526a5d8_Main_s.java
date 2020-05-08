 import model.Event;
 import java.util.List;
 import output.*;
 import filtering.*;
import sorting.*;
 import parsing.*;
 
 public class Main {
 	public static void main(String[] args){
 		//AbstractXMLParser parser = new DukeXMLParser();
 		//parser.loadFile("http://www.cs.duke.edu/courses/spring12/cps108/assign/02_tivoo/data/dukecal.xml");
 		
 		AbstractXMLParser parser = new TVXMLParser();
 		parser.loadFile("http://duke.edu/~jjh38/tv.xml");
 		
 		List<Event> listOfEvents = parser.processEvents();
		KeywordFilter keywordFilter = new KeywordFilter();
		List<Event> newList = keywordFilter.filter(listOfEvents, "");               
    	AbstractHtmlOutputter ho = new WeekListOutputter();
     	ho.writeEvents(newList);
     	
     	for (Event event : newList) {
     		System.err.println(event.getTitle());
     	}
     	System.err.println("Done!");
 	}
 }
 
