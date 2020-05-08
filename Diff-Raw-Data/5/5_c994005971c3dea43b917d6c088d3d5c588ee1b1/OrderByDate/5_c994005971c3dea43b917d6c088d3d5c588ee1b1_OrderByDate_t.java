 package me.arno.blocklog;
 
 import java.util.Comparator;
 
 import me.arno.blocklog.logs.DataEntry;
 
 public class OrderByDate implements Comparator<DataEntry> {
 
 	@Override
 	public int compare(DataEntry arg0, DataEntry arg1) {
 		if(arg0.getDate() > arg1.getDate())
 			return -1;
		else if(arg0.getDate() < arg1.getDate())
			return 1;
 		return 0;
 	}
 
 }
