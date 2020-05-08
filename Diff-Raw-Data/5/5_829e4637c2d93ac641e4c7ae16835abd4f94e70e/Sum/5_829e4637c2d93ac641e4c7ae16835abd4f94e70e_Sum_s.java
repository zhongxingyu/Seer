 package Online;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.sql.*;
 
 public class Sum {
 
	public static int processSum(ResultSet result, int sample_size, int db_size) throws SQLException {
 
 		HashMap<Integer, Integer> frequencies = new HashMap<Integer, Integer>();
 		Integer k;
 		Integer v;
 
		int sum = 0;
 		int s = 0;
 		while (result.next()) {
 			k = new Integer(result.getInt("value"));// sum first column
 			if (frequencies.containsKey(k)) {
 				v = frequencies.get(k);
 				frequencies.put(k, new Integer(++v));
 			} else
 				frequencies.put(k, new Integer(1));
 			s++;
 		}
 
 		System.out.println(s);
 		Iterator<Map.Entry<Integer, Integer>> it = frequencies.entrySet().iterator();
 		while (it.hasNext()) {
 			Entry<Integer, Integer> entry = (Entry<Integer, Integer>) it.next();
 			sum += entry.getKey() * (entry.getValue() / (double)sample_size) * db_size;
 		}
 
 		return sum;
 	}
 
 }
