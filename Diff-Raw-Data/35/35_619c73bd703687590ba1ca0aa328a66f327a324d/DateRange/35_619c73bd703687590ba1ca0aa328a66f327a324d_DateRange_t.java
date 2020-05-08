 package visualize;
 
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.regex.PatternSyntaxException;
 
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 
 public class DateRange {
 	
 	int graph; // Graph number that uses this DateRange object
 	
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
 	
 	public int minYear = Integer.MAX_VALUE;
 	public int maxYear = Integer.MIN_VALUE;
 	
 	public Grouping g;
 	
 	public int zooms = 0;
 	//years?
 
 	public String regex = null;
 	
 	
 	/*
 	 *  default assumes no restrictions, that you want all data
 	 */
 	public DateRange(int graphNum)
 	{
 		graph = graphNum;
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
 	public ArrayList<ArrayList<FileObject>> filterFiles(ArrayList<ArrayList<FileObject>> files)
 	{
 		ArrayList<ArrayList<FileObject>> filtered = new ArrayList<ArrayList<FileObject>>();
 		
 		for(int i = 0; i < files.size(); i++)
 		{
 			filtered.add(new ArrayList<FileObject>());
 			for(FileObject file : files.get(i))
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
 						
				try {
					if(regex != null && !file.getName().matches(regex)) continue;
				}
				catch(PatternSyntaxException p) {
					
				}
 				
 				//if everything is valid, then add to filtered
 				filtered.get(i).add(file);
 			}
 		}
 		
 		return filtered;
 	}
 
 
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public CategoryDataset getDataSet(ArrayList<ArrayList<FileObject>> filtered, 
 										ArrayList<String> titles)
 	{
 		DefaultCategoryDataset d = new DefaultCategoryDataset();
 		for(int i = 0; i < filtered.size(); i++)
 			getDataSetHelp(filtered.get(i), d, titles.get(i));
 		return d;
 	}
 	
 	private void getDataSetHelp(ArrayList<FileObject> filtered, 
 			DefaultCategoryDataset d, 
 			String setTitle)
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
 				if(years==null)
 				{
 					years = new boolean[maxYear-minYear+1];
 					for(int i = 0; i < years.length; i++)
 						years[i] = true;
 				}
 				break;
 				
 				
 		}
 		
 
 		
 		switch(g)
 		{
 			case SECONDS:
 			for(int i = 0; i < seconds.length; i++)
 			{
 				String title = ""+i;
 				if(seconds[i])
 				{
 					if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 					else d.addValue(0,setTitle, title);
 				}
 			}
 			break;
 			case MINUTES:
 				for(int i = 0; i < minutes.length; i++)
 				{
 					String title = ""+i;
 					if(minutes[i])
 					{
 						if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 						else d.addValue(0,setTitle, title);
 					}
 				}
 				break;
 	
 
 			case HOURS:
 				for(int i = 0; i < hours.length; i++)
 				{
 					String title = ""+i;
 					if(hours[i])
 					{
 						if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 						else d.addValue(0,setTitle, title);
 					}
 				}
 				break;
 	
 		
 			case DAYS_OF_WEEK:
 				for(int i = 0; i < daysOfWeek.length; i++)
 				{
 					String title = DateRange.getDay(i);
 					if(daysOfWeek[i])
 					{
 						if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 						else d.addValue(0,setTitle, title);
 					}
 				}
 				break;
 				
 			case MONTHS:
 				for(int i = 0; i < months.length; i++)
 				{
 					String title = DateRange.getMonth(i);
 					if(months[i])
 					{
 						if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 						else d.addValue(0,setTitle, title);
 					}
 				}
 				break;
 			case YEARS:
 				for(int i = minYear; i < maxYear+1; i++)
 				{
 					String title = ""+i;
 					if(map.containsKey(title)) d.addValue(map.get(title), setTitle, title);
 					else d.addValue(0,setTitle, title);
 				}
 				break;
 				
 		
 		}
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
 		if(years != null)
 		{
 			for(int i=0; i<years.length; i++) 
 			{
 				if(years[i]) 
 				{
 					sb.append(minYear+i);
 					sb.append(", ");
 				}
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
 	
 		if(regex != null && !regex.equals("")) {
 			sb.append("Filename Filtering Regex: ");
 			sb.append(regex);
 			sb.append("\n");
 		}
 		
 		
 		return sb.toString();
 	}
 }
