 package net.anzix.rahakott;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class CurrencyConverter {
 
 	private Map<String, Map<Date, Double>> rates = new HashMap<String, Map<Date, Double>>();
 
 	public CurrencyConverter() {
 		super();
 	}
 
 	public static CurrencyConverter INSTANCE = new CurrencyConverter();
 
 	public double convert(Date date, String from, String to, double amount) {
 		if (from.equals(to)) {
 			return amount;
 		}
 		if (!rates.containsKey(from + to) || rates.get(from + to).size() == 0) {
 			throw new IllegalArgumentException("No currency rate for " + from
 					+ to);
 		} else {
			return rates.get(from + to).values().iterator().next() * amount;
 		}
 	}
 }
