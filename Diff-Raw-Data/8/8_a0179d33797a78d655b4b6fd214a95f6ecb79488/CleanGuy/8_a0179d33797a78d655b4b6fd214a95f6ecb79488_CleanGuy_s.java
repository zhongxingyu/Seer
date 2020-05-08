 package com.rolflekang.kube95;
 
 import java.util.Calendar;
 
 
 public class CleanGuy {
 	
 	public String getCleaner() {
 		Calendar c = Calendar.getInstance();
 		getCleaner(c.get(Calendar.WEEK_OF_MONTH));
 		return "Random";
 	}
 	public String getCleaner(int weeknr) {
 		if (weeknr == 9 || (weeknr - 9) % 8 == 0) return "Katrine";
 		else if(weeknr == 10 || (weeknr - 10) % 8 == 0) return "Rolf";
 		else if(weeknr == 11 || (weeknr - 11) % 8 == 0) return "Julie";
 		else if(weeknr == 12 || (weeknr - 12) % 8 == 0)  return "Rolf Erik";
 		else if(weeknr == 13 || (weeknr - 13) % 8 == 0)  return "Anders";
 		else if(weeknr == 14 || (weeknr - 14) % 8 == 0)  return "Andreas";
 		else if(weeknr == 15 || (weeknr - 15) % 8 == 0)  return "Ole";
		else if(weeknr == 16 || (weeknr - 16) % 8 == 0)  return "Hvard";
 		return "Random";
 	}
 	public int getNextCleanWeek(String name){
 		//TODO: Lage denne funskjonen
 		return 0;
 	}
 
 }
