 package visualize;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.jfree.data.DefaultKeyedValues;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.general.DatasetUtilities;
 
 public class DateRange {
 	
 
 	
 	public enum Grouping {
 		SECONDS,
 		MINUTES,
 		HOURS,
 		DAYS,
 		DAYS_OF_WEEK,
 		MONTHS,
 		YEARS
 	}
 	
 	public boolean[] seconds = new boolean[60];
 	public boolean[] minutes = new boolean[60];
 	public boolean[] hours = new boolean[24];
 	public boolean[] daysOfWeek = new boolean[7];
 	public boolean[] months = new boolean[12];
 	public boolean[] years;
 	
 	public int minYear = 2012;
 	public int maxYear = 2012;
 	
 	public Grouping g;
 	
 	public int zooms = 0;
 	//years?
 
 	
 	/*
 	 *  default assumes no restrictions, that you want all data
 	 */
 	public DateRange()
 	{
 		for(int i = 0; i < seconds.length; i++) seconds[i] = true;
 		
 		for(int i = 0; i < minutes.length; i++) minutes[i] = true;
 		
 		//set all hours to true
 		for(int i = 0; i < hours.length; i++) hours[i] = true;
 		
 		//set all days of week to true
 		for(int i=0; i < daysOfWeek.length; i++) daysOfWeek[i] = true;
 		
 		//set all months to true
 		for(int i=0; i < months.length; i++) months[i] = true;
 	
 		// default group to years
 		g = Grouping.YEARS;
 	}
 	
 
 	//used by data
 	public ArrayList<FileObject> filterFiles(ArrayList<FileObject> files)
 	{
 		
 		
 		ArrayList<FileObject> filtered = new ArrayList<FileObject>();
 		
 		for(FileObject file : files)
 		{
 			if(!seconds[file.getSecond()]) continue;
 			
 			if(!minutes[file.getMinute()]) continue;
 			
 			//check if hour is valid
 			if(!hours[file.getHour()]) continue;
 						
 			//check if day of week is valid
 			if(!daysOfWeek[file.getDayOfWeek()-1]) continue;
 						
 			//check if month is valid
 			if(!months[file.getMonth()-1]) continue;
 						
 			if(years != null && !years[file.getYear()-minYear]) continue;
 						
 			//if everything is valid, then add to filtered
 			filtered.add(file);
 		}
 		
 		return filtered;
 	}
 
 	//used by view
 	public CategoryDataset getDataSet(ArrayList<FileObject> filtered)
 	{
 		HashMap<String, Integer> map = new HashMap<String,Integer>();
 		int t;
 		String key;
 		switch(g)
 		{
 			case SECONDS:
 				for(FileObject file : filtered)
 				{
 					t = file.getSecond();
 					key = "" + t;
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);					
 				}
 				break;
 			case MINUTES:
 				for(FileObject file : filtered)
 				{
 					t = file.getMinute();
 					key = "" + t;
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);					
 				}
 				break;
 		
 			case HOURS:				
 				for(FileObject file : filtered)
 				{
 					t = file.getHour();
 					key = "" + t;
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);					
 				}
 				break;
 			case DAYS_OF_WEEK:
 				for(FileObject file : filtered)
 				{
 					t = file.getDayOfWeek()-1;
 					key = DateRange.getDay(t);
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);
 				}
 				break;
 			case MONTHS:
 				for(FileObject file : filtered)
 				{
 					t = file.getMonth()-1;
 					key = DateRange.getMonth(t);
 					
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);					
 				}
 				break;
 			case YEARS:
 				for(FileObject file : filtered)
 				{
 					t = file.getYear();
 					key = ""+t;
 					
 					if(t < minYear) minYear=t;
 					if(t > maxYear) maxYear=t;
 					
 					if(map.containsKey(key)) 
 					{
 						map.put(key, map.get(key) + 1);
 					}
 					else map.put(key, 1);					
 				}
				years = new boolean[maxYear-minYear+1];
				for(int i = 0; i < years.length; i++)
					years[i] = true;
 				break;
 				
 				
 		}
 		
 		
 		DefaultKeyedValues objs = new DefaultKeyedValues();
 		
 		
 		switch(g)
 		{
 			case SECONDS:
 			for(int i = 0; i < seconds.length; i++)
 			{
 				String title = ""+i;
 				if(seconds[i])
 				{
 					if(map.containsKey(title)) objs.addValue(title, map.get(title));
 					else objs.addValue(title, 0);
 				}
 			}
 			break;
 			case MINUTES:
 				for(int i = 0; i < minutes.length; i++)
 				{
 					String title = ""+i;
 					if(minutes[i])
 					{
 						if(map.containsKey(title)) objs.addValue(title, map.get(title));
 						else objs.addValue(title, 0);
 					}
 				}
 				break;
 	
 
 			case HOURS:
 				for(int i = 0; i < hours.length; i++)
 				{
 					String title = ""+i;
 					if(hours[i])
 					{
 						if(map.containsKey(title)) objs.addValue(title, map.get(title));
 						else objs.addValue(title, 0);
 					}
 				}
 				break;
 	
 		
 			case DAYS_OF_WEEK:
 				for(int i = 0; i < daysOfWeek.length; i++)
 				{
 					String title = DateRange.getDay(i);
 					if(daysOfWeek[i])
 					{
 						if(map.containsKey(title)) objs.addValue(title, map.get(title));
 						else objs.addValue(title, 0);
 					}
 				}
 				break;
 				
 			case MONTHS:
 				for(int i = 0; i < months.length; i++)
 				{
 					String title = DateRange.getMonth(i);
 					if(months[i])
 					{
 						if(map.containsKey(title)) objs.addValue(title, map.get(title));
 						else objs.addValue(DateRange.getMonth(i), 0);
 					}
 				}
 				break;
 			case YEARS:
 				for(int i = minYear; i < maxYear+1; i++)
 				{
 					String title = ""+i;
 					if(map.containsKey(title)) objs.addValue(title, map.get(title));
 					else objs.addValue(title, 0);
 				}
 				break;
 				
 		
 		}
 		
 		
 		
 		/*Set<String> s = map.keySet();		
 
 		
 		for(String title : s)
 		{
 			objs.addValue(title, map.get(title));
 		}*/
 		
 		return DatasetUtilities.createCategoryDataset(Controller.inputFile, objs);
 	}
 
 	public static String getDay(int i)
 	{
 		switch(i)
 		{
 			case 0: return "Sunday";
 			case 1: return "Monday";
 			case 2: return "Tuesday";
 			case 3: return "Wednesday";
 			case 4: return "Thursday";
 			case 5: return "Friday";
 			case 6: return "Saturday";
 
 		}
 		
 		return "";
 	}
 
 	public static String getMonth(int i)
 	{
 		switch(i)
 		{
 			case 0: return "January";
 			case 1: return "February";
 			case 2: return "March";
 			case 3: return "April";
 			case 4: return "May";
 			case 5: return "June";
 			case 6: return "July";
 			case 7: return "August";
 			case 8: return "September";
 			case 9: return "October";
 			case 10: return "November";
 			case 11: return "December";				
 		}
 		
 		return "";
 	}
 	
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Grouped by: ");
 		switch(g) {
 			case SECONDS:
 				sb.append("Seconds\n");
 				break;
 			case MINUTES:
 				sb.append("Minutes\n");
 				break;
 			case HOURS:
 				sb.append("Hours\n");
 				break;
 			case DAYS:
 				sb.append("Days\n");
 				break;
 			case DAYS_OF_WEEK:
 				sb.append("Days of the Week\n");
 				break;
 			case MONTHS:
 				sb.append("Months\n");
 				break;
 			case YEARS:
 				sb.append("Years\n");
 				break;
 		}
 		
 		sb.append("Allowed Years: ");
 		for(int i=0; i<years.length; i++) {
 			if(years[i]) {
 				sb.append(minYear+i);
 				sb.append(", ");
 			}
 		}
 		
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 		
 		sb.append("Allowed Months: ");
 		for(int i=0; i<months.length; i++) {
 			if(months[i]) {
 				sb.append(getMonth(i));
 				sb.append(", ");
 			}
 		}
 		
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 		
 		sb.append("Allowed Days of the Week: ");
 		for(int i=0; i<daysOfWeek.length; i++) {
 			if(daysOfWeek[i]) {
 				sb.append(getDay(i));
 				sb.append(", ");
 			}
 		}
 		
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 		
 		sb.append("Allowed Hours: ");
 		for(int i=0; i<hours.length; i++) {
 			if(hours[i]) {
 				sb.append(i);
 				sb.append(", ");
 			}
 		}
 		
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 		
 		sb.append("Allowed Minutes: ");
 		for(int i=0; i<minutes.length; i++) {
 			if(minutes[i]) {
 				sb.append(i);
 				sb.append(", ");
 			}
 		}
 	
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 		
 		sb.append("Allowed Seconds: ");
 		for(int i=0; i<seconds.length; i++) {
 			if(seconds[i]) {
 				sb.append(i);
 				sb.append(", ");
 			}
 		}
 		
 		// Remove last character (comma)
 		sb.setLength(sb.length()-2);
 		sb.append("\n");
 	
 		
 		return sb.toString();
 	}
 }
