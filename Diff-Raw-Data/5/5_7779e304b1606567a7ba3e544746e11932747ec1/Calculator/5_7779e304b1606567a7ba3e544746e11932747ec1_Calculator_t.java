 package info.ohgita.android.bincalc;
 
 import info.ohgita.android.bincalc.calculator.BaseConverter;
 import info.ohgita.android.bincalc.calculator.BasicArithOperator;
 import info.ohgita.android.bincalc.calculator.ExpParser;
 import info.ohgita.android.bincalc.calculator.HistoryItem;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import android.util.Log;
 
 /**
  * bin.Calc Internal calculator class
  * @author Masanori Ohgita
  */
 public class Calculator {
 	protected ExpParser expParser;	// Numerical-expression (Numerical formula) parser object
 	protected BasicArithOperator basicArithOperator;	// Basic arithmetic operator object
 	protected BaseConverter baseConverter;	// Base converter object
 	
 	protected String EXP_SYMBOLS[] = {"(",")","*","/","+","-"};
 	
 	protected String memory;
 	
 	public ArrayList<HistoryItem> histories;
 	
 	/**
 	 * Constructor
 	 */
 	public Calculator(){
 		/* Initialize objects */
 		expParser = new ExpParser();
 		basicArithOperator = new BasicArithOperator();
 		baseConverter = new BaseConverter();
 		
 		histories = new ArrayList<HistoryItem>();
 		
 		/* Initialize sort for binarySearch */
 		Arrays.sort(EXP_SYMBOLS);
 		
 	}
 	
 	/**
 	 * MemoryIn method
 	 */
 	public void MemoryIn(){
 		
 	}
 	
 	/**
 	 * MemoryOut (Read) method
 	 */
 	public void MemoryOut(){
 		
 	}
 	
 	/**
 	 * Calculate a numerical formula
 	 * @param exp Numerical formula (Decimal numbers)
 	 * @return Calculated result
 	 */
 	public String calc(String exp){
 		Log.d("binCalc", "calc("+exp+")");
 		
 		/* Parse a numerical formula */
 		LinkedList<String> list = parseToList(exp);
 		
 		/* Calculate formula */
 		String res = basicArithOperator.calculate(list);
 		return res;
 	}
 	
 	/**
 	 * Parse a numerical formula, and convert to a LinkedList.
 	 * @param exp Numerical formula (Decimal numbers)
 	 * @return Parsed LinkedList
 	 */
 	public LinkedList<String> parseToList(String exp){
 		Log.d("binCalc", "parseToList("+exp+")");
 		return expParser.parseToList(exp);
 	}
 	
 	/**
 	 * Base number convert from LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param fromNBase Source base
 	 * @param destNBase Destination base
 	 * @return Converted result
 	 * @throws Exception 
 	 */
 	public String listBaseConv(LinkedList<String> list, int fromNBase, int destNBase ) throws Exception{
 		Log.d("binCalc", "Calculator.listBaseConv(list, "+fromNBase+", "+destNBase+")");
 		Iterator<String> iter = list.iterator();
 		StringBuilder resultExp = new StringBuilder();
 		
 		while(iter.hasNext()){
 			String chunk = iter.next();
 			
 			String conv = null;
 			if(Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){ // if number
 				
 				if(fromNBase == 10){
 					/* Convert DEC(10) -> NADIC( 2 or 16 ) */
 					conv = baseConverter.decToN(Double.parseDouble(chunk), destNBase);
 					
 				}else if(fromNBase == 2){
 					
 					if(destNBase == 10){
 						/* Convert BIN(2) -> DEC(10) */
 						Double d =baseConverter.binToDec(chunk);
 						if(isDecimalFraction(2, chunk) == true){
 							conv = d.toString();
 						}else{
 							conv = d.intValue() + ""; // Remove the decimal fraction point
 						}
 					}else if(destNBase == 16){
 						/* Convert BIN(2) -> DEC(16) */
 						conv = baseConverter.decToN( baseConverter.binToDec(chunk), 16);
 					}
 					
 				}else if(fromNBase == 16){
 					
 					if(destNBase == 2){
 						/* Convert HEX(16) -> BIN(2) */
 						conv = baseConverter.decToN( baseConverter.hexToDec(chunk), 2);
 					}else if(destNBase == 10){
 						/* Convert HEX(16) -> DEC(10) */
 						Double d = baseConverter.hexToDec(chunk);
 						if(isDecimalFraction(16, chunk) == true){
 							conv = d.toString();
 						}else{
 							conv = d.intValue() + ""; // Remove the decimal fraction point
 						}
 					}
 				
 				} else {
 					conv = chunk;
 				}
 				
 				/* Insert the separator to number */
 				conv = insertSeparator(conv, destNBase);
 				
 			} else {// if symbols(ex: operator)
 				conv = chunk;
 			}
 			
 			resultExp.append(conv);
 		}
 		return resultExp.toString();
 	}
 	
 	/**
 	 * Convert LinkedList to String (Numerical formula) 
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param nBase Source base
 	 * @return Numerical formula string
 	 */
 	public String listToString(LinkedList<String> list, int nBase){
 		Log.d("binCalc", "Calculator.listToString(list, "+nBase+")");
 		Iterator<String> iter = list.iterator();
 		StringBuilder resultExp = new StringBuilder();
 		
 		while(iter.hasNext()){
 			String chunk = iter.next();
 			Log.i("binCalc", chunk);
 			if (nBase == 10) { // Decimal
 				if (Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){// if number
 					if (chunk.contentEquals("-0")){
 						
 					} else {
 						/* Remove a decimal point */
 						Double d = Double.parseDouble(chunk);
 						if(isDecimalFraction(nBase, chunk) == false){
 							if(! (d.intValue() >= Integer.MAX_VALUE)){ // If not overflow...
 								chunk = d.intValue() + "";
 							}
 						}
 						
 						/* Insert the separator to number */
 						chunk = insertSeparator(chunk, nBase);
 					}
 				}
 			} else if (nBase == 2) { // Binary
 				if (Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){// if number
 					/* Insert the separator to number */
 					chunk = insertSeparator(chunk, nBase);
 				}
 			}
 			
 			resultExp.append(chunk);
 		}
 		Log.d("binCalc", "  => "+resultExp.toString());
 		return resultExp.toString();
 	}
 	
 	/**
 	 * Remove parenthesis for LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @return Processed LinkedList
 	 */
 	public LinkedList<String> removeParentheses(LinkedList<String> list){
 		Log.d("binCalc", "Calculator.removeParentheses(list)");
 		LinkedList<String> ret_list = new LinkedList<String>(); 
 		for(int i=0;i<list.size();i++){
 			String chunk = list.get(i);
 			if(chunk.contentEquals("(") == false && chunk.contentEquals(")") == false){
 				// if NOT parenthesis...
 				ret_list.add(chunk);
 			}
 		}
 		return ret_list;
 	}
 	
 	/**
 	 * Zero-padding for LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param nBase Source base
 	 * @return Processed LinkedList
 	 */
 	public LinkedList<String> listZeropadding(LinkedList<String> list, int nBase){
 		Log.d("binCalc", "Calculator.listZeropadding(list, "+nBase+")");
 		for(int i=0;i<list.size();i++){
 			String chunk = list.get(i);
 			if(Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){// if number
 				if(nBase == 2){ // binary
 					list.set(i, baseConverter.binZeroPadding(chunk));
 				}
 			}
 		}
 		return list;
 	}
 	
 	/**
 	 * Separate for LinkedList
 	 * @param list LinkedList (Parsed numerical formula)
 	 * @param nBase Source base
 	 * @return Processed LinkedList
 	 */
 	/*public LinkedList<String> listSeparate(LinkedList<String> list, int nBase){
 		Log.d("binCalc", "Calculator.listSeparate(list, "+nBase+")");
 		LinkedList<String> ret_list = new LinkedList<String>(); 
 		for(int i=0;i<list.size();i++){
 			String chunk = list.get(i);
 			if(Arrays.binarySearch(EXP_SYMBOLS, chunk) < 0){// if number
 				if(nBase == 2){ // binary
 					ret_list.set(i, baseConverter.binSeparate(chunk));
 					continue;
 				}
 			}
 			ret_list.set(i, chunk);
 		}
 		return ret_list;
 	}*/
 	
 	/**
 	 * Add a history of calculator
 	 * @param history item
 	 */
 	public int putHistory(HistoryItem history) {
 		Log.d("binCalc","historyAdd(history)");
 		/* Push a history */
 		histories.add(history);
 		return histories.size() - 1;
 	}
 	
 	/**
 	 * Add (insert and overwrite) a history of calculator
 	 * @param id	Insert destination Id (Later items will be removed.)
 	 * @param history item
 	 */
 	public int putHistory(int id, HistoryItem history) {
 		Log.d("binCalc","historyAdd(history)");
 		/* Remove histories of later than it */
 		while (id < histories.size()) {
 			histories.remove(histories.size() - 1);
 		}
 		/* Push a history */
 		histories.add(history);
 		return histories.size() - 1;
 	}
 	
 	/**
 	 * Get a history of calculator
 	 * @param ID of a history
 	 * @return history
 	 */
 	public HistoryItem getHistory(int id) {
 		if (id < histories.size()) {
 			return histories.get(id);
 		}
 		return null;
 	}
 	
 	public int getHistoryNums(){
 		return histories.size();
 	}
 	
 	protected boolean isDecimalFraction(int nBase, String dec){
 		if(dec.charAt(dec.length() -1) == '.'){ // If last char is point... (ex: "1.")
 			return true;
 		}
 		
 		if(nBase == 16){ // HEX
 			if (dec.indexOf('.') == -1)
 				return false;
 		
 		} else{ // BIN or DEC
 			Double d = Double.parseDouble(dec);
 			if(d % 1.0 == 0.0)
 				return false; // Integer number
 		}
 		return true; // Fraction number
 	}
 	
 	/**
 	 * Insert the digit-separator into number
 	 * @param number Source number string
 	 * @param NBase
 	 * @return Result number string
 	 */
 	protected String insertSeparator(String number, int nBase) {
 		Log.d("binCalc", "Calculator.insertSeparator("+number+")");
 		if (nBase == 2) { // Binary
 			String r = "";
 			int c = 0;
 			for (int i = 0; i < number.length(); i++) {
 				if (c % 4 == 0) {
 					r += " ";
 				}
 				r += number.charAt(i);
 				c++;
 			}
 			return r;
 		} else if (nBase == 10) { // Decimal
 			DecimalFormat formatter = new DecimalFormat("#,###");
			if (number.indexOf(".") != -1) {
				int i = number.indexOf(".");
				String n = number.substring(0, i);
				return formatter.format(Integer.parseInt(n)) + number.substring(i, number.length());
			}
 			return formatter.format(Integer.parseInt(number));
 		}
 		return number;
 	}
 }
