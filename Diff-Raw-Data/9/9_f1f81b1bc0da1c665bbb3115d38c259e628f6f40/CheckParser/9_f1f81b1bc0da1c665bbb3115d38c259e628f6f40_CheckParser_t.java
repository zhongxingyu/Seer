 package edu.luc.clearing;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class CheckParser {
 	private static final Map<String, Integer> AMOUNTS = new HashMap<String, Integer>();
 
 	public CheckParser() {
 		AMOUNTS.put("zero", 0);
 		AMOUNTS.put("one", 100);
 		AMOUNTS.put("two", 200);
 		AMOUNTS.put("three", 300);
 		AMOUNTS.put("four", 400);
 		AMOUNTS.put("five", 500);
 		AMOUNTS.put("six", 600);
 		AMOUNTS.put("seven", 700);
 		AMOUNTS.put("eight", 800);
 		AMOUNTS.put("nine", 900);
 		AMOUNTS.put("ten", 1000);
 		AMOUNTS.put("eleven", 1100);
 		AMOUNTS.put("twelve", 1200);
 		AMOUNTS.put("thirteen", 1300);
 		AMOUNTS.put("fourteen", 1400);
 		AMOUNTS.put("fifteen", 1500);
 		AMOUNTS.put("sixteen", 1600);
 		AMOUNTS.put("seventeen", 1700);
 		AMOUNTS.put("eighteen", 1800);
 		AMOUNTS.put("nineteen", 1900);
 		AMOUNTS.put("twenty", 2000);
 		AMOUNTS.put("thirty", 3000);
 		AMOUNTS.put("forty", 4000);
 		AMOUNTS.put("fifty", 5000);
 		AMOUNTS.put("sixty", 6000);
 		AMOUNTS.put("seventy", 7000);
 		AMOUNTS.put("eighty", 8000);
 		AMOUNTS.put("ninety", 9000);
 	}
 
 	public Integer parseExpression(String amount) {
 
 		amount = amount.toLowerCase().trim();
 
 		if (IsMatch("([a-z\\s]+)(\\s*)+and(\\s?)(\\d+/100)", amount)) {
 			return parseAmountWithCents(amount);
 		} else if (IsMatch("([a-z\\s]+)(\\s*)+dollar([s]?)", amount)) {
 			return parseAmountWithDollars(amount);
 		} else if (IsMatch("([a-z\\s]+)(\\s*)([a-z]+)", amount)) {
 			return parseAmountTwoDigits(amount);
 		} else if (IsMatch("\\d+/100(\\s*)", amount)) {
 			return parseCents(amount);
 		}
 		return parseAmount(amount);
 	}
 
 	private Integer parseAmount(String amount) {
 		return AMOUNTS.get(amount);
 	}
 
 	private Integer getInvalidAmount() {
 		return null;
 	}
 
 	private Integer parseFromArrayFirstElement(String[] array) {
 		if (array.length > 0) {
 			return parseAmountTwoDigits(array[0]);
 		} else {
 			return getInvalidAmount();
 		}
 	}
 	
 	private Integer parseAmountTwoDigits(String amount) {
 		Pattern p = getPattern("\\s+");
 		String[] array = p.split(amount);
 
 		if (array.length > 0) {
 			Integer total = null;
 			for (String s : array) {
 				Integer i = parseAmount(s);
 				if (i != null){
 					if (total == null){
 						total = 0;
					} else if (total < 1000 || i > 900 ){
 						return null;
 					}
 					total += i;
 				}
 			}
 			return total;
 		} else {
 			return getInvalidAmount();
 		}
 	}
 
 	private Integer parseAmountWithDollars(String amount) {
 		Pattern p = getPattern("dollar");
 		String[] array = p.split(amount);
 		return parseFromArrayFirstElement(array);
 	}
 
 	private Integer parseCents(String amount) {
 		Pattern p = getPattern("/100");
 		String[] array = p.split(amount);
 		if (array.length > 0) {
 			return Integer.parseInt(array[0].trim());
 		}
 		return getInvalidAmount();
 	}
 
 	private Integer parseAmountWithCents(String amount) {
 		Pattern p = getPattern("and");
 		String[] array = p.split(amount);
 		if (array.length == 2) {
 			return parseAmountTwoDigits(array[0]) + parseCents(array[1]);
 		}
 
 		return getInvalidAmount();
 	}
 
 	private Pattern getPattern(String regex) {
 		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE
 				| Pattern.MULTILINE);
 		return p;
 	}
 
 	private boolean IsMatch(String regex, String amount) {
 		Pattern p = getPattern(regex);
 		Matcher m = p.matcher(amount);
 		return m.matches();
 	}
 }
