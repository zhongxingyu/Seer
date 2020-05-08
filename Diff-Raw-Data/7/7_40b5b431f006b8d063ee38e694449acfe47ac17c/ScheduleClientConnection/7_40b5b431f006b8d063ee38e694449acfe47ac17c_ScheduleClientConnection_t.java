 package version2;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 public class ScheduleClientConnection implements Runnable {
 
 	private Socket clientSocket;
 	private String line;
 	BufferedReader in;
 	PrintStream out;
 
 	private HashMap<CurrentDate, Day> calStorage;
 
 	ScheduleClientConnection(Socket server, HashMap<CurrentDate, Day> calStorage) {
 		clientSocket = server;
 		this.calStorage = calStorage;
 	}
 
 	/*
 	 * The server takes 4 ints separated by spaces this is somewhat sanitized,
 	 * but don't test it
 	 * 
 	 * the format for input is month day year time
 	 * 
 	 * year and time must be four digits, the time must be in 24 hr format
 	 * 
 	 * it returns two lines, the first line is the current period given the time
 	 * 
 	 * the second line is the next period
 	 * 
 	 * these lines each give the period number and then the start and end times
 	 * separated by spaces
 	 * 
 	 * ex 10 11 2012 1051 <-- input A 4 1050 1205 <-output 6 1305 1405
 	 * 
 	 * key to output anything >0 is the period number -1 is break -2 assembly -3
 	 * class meeting -4 advisory -5 clubs -7 lunch
 	 */
 
 	@Override
 	public void run() {
 
 		try {
 			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 			out = new PrintStream(clientSocket.getOutputStream());
 			while ((line = in.readLine()) != null && !line.equals(".")) {
 				String[] temp = line.split(" ");
 				if (temp.length != 4) {
 					out.println("Please enter the time and date in the following format: month day year time");
 					// out.println(".");
 				} else {
 					CurrentDate today = new CurrentDate(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]),
 							Integer.parseInt(temp[2]));
 					Day thing = null;
 					// System.out.println(today);
 					for (Entry<CurrentDate, Day> c : calStorage.entrySet()) {
 						if (c.getKey().equals(today)) {
 							thing = c.getValue();
 						}
 
 					}
 					if (thing != null) {
 						int currentTime = Integer.parseInt(temp[3]);
 						currentTime = adjustTime(currentTime, today, 1);
 						String stuff = "";
 						if (thing != null) {
 							Period tp;
 							if (currentTime < thing.getD().peek().getStartTime()) {
 								stuff += thing.getDayType() + "--";
 								stuff += "-9--";
 								tp = thing.getD().peek();
 								stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 										+ adjustTime(tp.getEndTime(), today, -1);
 							} else {
 								if ((tp = thing.currentPeriod(currentTime)) != null) {
 									stuff += thing.getDayType() + "--";
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1) + "--";
 								} else {
 									stuff += "-8--";
 								}
 								if ((tp = thing.nextPeriod(currentTime)) != null) {
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1);
 								} else if (((tp = thing.nextPeriod(currentTime - 10)) != null && (thing.getD().peek()
 										.getStartTime() == adjustTime(800, today, 1)
 										&& currentTime < adjustTime(1410, today, 1) || (thing.getD().peek()
 										.getStartTime() == adjustTime(900, today, 1) && currentTime < adjustTime(1425,
 										today, 1))))) {
 									stuff += tp.getNumber() + "--" + adjustTime(tp.getStartTime(), today, -1) + "--"
 											+ adjustTime(tp.getEndTime(), today, -1);
 
 								} else
 									stuff += "-8";
 							}
 							out.println(stuff);
 							// out.println(".");
 						}
 
 					} else {
 						out.println('X');
 					}
 				}
 
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 	public int adjustTime(int time, CurrentDate today, int direction) {
 		int currentTime = time;
		currentTime += 700 * direction;
		System.out.println(currentTime);
 		return currentTime;
 
 	}
 
 }
