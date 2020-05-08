 package primary;
 
 import java.io.BufferedReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.ResourceBundle;
 
 public class Update {
 	
 	//bundle gets the root name of our repository address from the config.properties file
 	private ResourceBundle bundle = ResourceBundle.getBundle("config");
 
 	public void updateOccurrences(int key) throws Exception {
 		
 		String p = bundle.getString("repo" + key);
 		String edit = p.replace('/', '0');
 		String path = "dataRead/occurrences/" + edit.replace(':', '0') + "occurrences" + ".txt"; 
 		String s;
 		String[] ss;
 		String current = "";
 		int count = 0;
 		
 		FileWriter outFile = new FileWriter(path);
         PrintWriter storing = new PrintWriter(outFile);
 		Process exec = Runtime.getRuntime().exec("svn log " + p + " -q -v");
 		BufferedReader  stdInput =  new  BufferedReader(new
               InputStreamReader(exec.getInputStream()));
 		CounterList<String> counter = new CounterList<String>(true);
 	
 		while  ((s =  stdInput.readLine())  !=  null)  {
 			if (s.startsWith("   M") || s.startsWith("   A") || s.startsWith("   D") || s.startsWith("   R")){
 				ss = s.split(" ");
 				current = ss[4];
 				counter.newInput(current);
 				count++;
 			}
 		}
 		storing.println("Top " + bundle.getString("occurrenceDisplayLimit") + " Occurring File Names:");
 		storing.println(counter.toString());
 		storing.close();
 	}
 
 	public void updateFullLog(int key) throws Exception {
 	
 		String p = bundle.getString("repo" + key);
 		String edit = p.replace('/', '0');
 		String path = "dataRead/fullLogs/" + edit.replace(':', '0') + "time" + ".txt"; 
 		String s;//current input line
 		String[] ss; //breaking down the line to get the date information
 		String previous = ""; //the last line's date stamp
 		String current = ""; //the current lines date stamp
 		long totalTime = 0; //the sum of all interval times
 		int rev = 0; //how many revisions there were in total
 		Process exec = Runtime.getRuntime().exec("svn log " + p + " -q");
 		BufferedReader  stdInput;
 		String toStore = "";
 		
 			stdInput =  new  BufferedReader(new
 	              InputStreamReader(exec.getInputStream()));
 			
 			while  ((s =  stdInput.readLine())  !=  null)  {
 				
 				toStore += s + "\n";
 
 				if (s.startsWith("r")) {  //a line starting with a lower case r implies that we are at a new revision
 					
 					ss = s.split(" "); //split the string along white spaces
 					if (ss[4].equals("|")){
 						current = (ss[5] + " " + ss[6]); 
 					}
 					else {
 						current = (ss[4] + " " + ss[5]);  // gets both the date and time of the revision
 					}
 					Calendar thisTime = new GregorianCalendar(Integer.parseInt(current.split(" ")[0].split("-")[0]), Integer.parseInt(current.split(" ")[0].split("-")[1])-1, Integer.parseInt(current.split(" ")[0].split("-")[2]), Integer.parseInt(current.split(" ")[1].split(":")[0]), Integer.parseInt(current.split(" ")[1].split(":")[1]), Integer.parseInt(current.split(" ")[1].split(":")[2]));
 					if (!previous.equals("")){ //implies this is not the first iteration
 						Calendar lastTime = new GregorianCalendar(Integer.parseInt(previous.split(" ")[0].split("-")[0]), Integer.parseInt(previous.split(" ")[0].split("-")[1])-1, Integer.parseInt(previous.split(" ")[0].split("-")[2]), Integer.parseInt(previous.split(" ")[1].split(":")[0]), Integer.parseInt(previous.split(" ")[1].split(":")[1]), Integer.parseInt(previous.split(" ")[1].split(":")[2]));
 						long timeDiff = lastTime.getTimeInMillis() - thisTime.getTimeInMillis();
 						rev++;
 						totalTime += timeDiff;
 					}
 					previous = current;
 				}
 			}
 			
 			FileWriter outFile = new FileWriter(path);
 			
 			PrintWriter storing = new PrintWriter(outFile);
 	       	storing.println(toStore);
 	       	storing.close();
 			
 	}
 	
 	public void runFullUpdate() {
 		
		int i = 1;
 		
 		while (1 != 0) {
 			try {
 				System.out.println("repo" + i + "a....");
 				updateFullLog(i);
 				System.out.println("repo" + i + "b....");
 				updateOccurrences(i);
 				i++;
 			}
 			
 			catch (Exception e) {
 				System.out.println("DONE");
 				break;
 			}
 		}
 	}
 }
