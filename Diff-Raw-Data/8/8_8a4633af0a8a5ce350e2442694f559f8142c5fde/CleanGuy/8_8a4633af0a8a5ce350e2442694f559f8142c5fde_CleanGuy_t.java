 package com.rolflekang.kube95;
 
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Locale;
 
 
 public class CleanGuy {
 	private final int KATRINE = 26;
 	private final int ROLFOLE = 19;
 	private final int JULIE = 20;
 	private final int ROLFERIK = 21;
 	private final int ANDERS = 22;
 	private final int ANDREAS = 23;
 	private final int OLE = 24;
 	private final int HAVARD = 25;
 	private HttpConnector hc;	
 	public CleanGuy() {
 		hc = new HttpConnector("rolflekang.com");
 	}	
 	public String getCleaner() {
		Calendar c = Calendar.getInstance(Locale.UK);
 		return getCleaner(c.get(Calendar.WEEK_OF_YEAR));
 	}
 	public String getNextCleaner() {
		Calendar c = Calendar.getInstance(Locale.UK);
 		return getCleaner(c.get(Calendar.WEEK_OF_YEAR) + 1);
 	}
 	public String getCleaner(int weeknr) {
 		weeknr = checkIfSwapped(weeknr);
 		if (weeknr == KATRINE || (weeknr - KATRINE) % 8 == 0) return "Katrine";
 		else if(weeknr == ROLFOLE || (weeknr - ROLFOLE) % 8 == 0) return "Rolf Ole";
 		else if(weeknr == JULIE || (weeknr - JULIE) % 8 == 0) return "Julie";
 		else if(weeknr == ROLFERIK || (weeknr - ROLFERIK) % 8 == 0)  return "Rolf Erik";
 		else if(weeknr == ANDERS || (weeknr - ANDERS) % 8 == 0)  return "Anders";
 		else if(weeknr == ANDREAS || (weeknr - ANDREAS) % 8 == 0)  return "Andreas";
 		else if(weeknr == OLE || (weeknr - OLE) % 8 == 0)  return "Ole";
 		else if(weeknr == HAVARD || (weeknr - HAVARD) % 8 == 0)  return "Havard";
 		return "Random";
 	}
 	public int getNextCleanWeek(String name){
 		Calendar cal = Calendar.getInstance();
 		int week = cal.get(Calendar.WEEK_OF_YEAR);
 		int weekOrigin = 0;
 		if(name.equalsIgnoreCase("katrine")) weekOrigin = KATRINE;
 		else if(name.equalsIgnoreCase("rolfole")) weekOrigin = ROLFOLE;
 		else if(name.equalsIgnoreCase("julie")) weekOrigin = JULIE;
 		else if(name.equalsIgnoreCase("rolferik")) weekOrigin = ROLFERIK;
 		else if(name.equalsIgnoreCase("anders")) weekOrigin = ANDERS;
 		else if(name.equalsIgnoreCase("andreas")) weekOrigin = ANDREAS;
 		else if(name.equalsIgnoreCase("ole")) weekOrigin = OLE;
 		else if(name.equalsIgnoreCase("havard"))  weekOrigin = HAVARD;
 		for (int i = 0; i < 8; i++) {
 			if((week + i) == weekOrigin || ((week + i) - weekOrigin) % 8 == 0) return (week + i);
 		}
 		return 0;
 	}
 	public ArrayList<String> getNextCleanersList(int weeknr) {
 		ArrayList<String> list = new ArrayList<String>();
 		for (int i = 1; i < 8; i++) {
 			list.add((weeknr + i) +": "+ getCleaner(weeknr + i));			
 		}
 		return list;
 	}
 	public void swap(int oldWeekNr, int newWeekNr) {
 		hc.swap(oldWeekNr, newWeekNr);
 	}
 	private int checkIfSwapped(int week){
 		int[][] swaps = hc.getSwaps();
 		for (int[] nr : swaps) {
 			if(nr[0] == week) return nr[1];
 			else if(nr[1] == week) return nr[0];
 		}
 		return week;
 	}
 
 
 }
