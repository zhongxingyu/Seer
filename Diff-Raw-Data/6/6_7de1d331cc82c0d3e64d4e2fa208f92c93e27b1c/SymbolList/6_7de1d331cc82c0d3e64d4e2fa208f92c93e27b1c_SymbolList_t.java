 package com.miravtech.annotator;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 import java.io.Reader;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SymbolList extends HashMap<String, Double> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8767598956815897691L;
 	
 	
 	static private Pattern lineFormat = Pattern.compile("(.*)[ |\\t|,|;](-?[0-9]*\\.?[0-9]*)");
 	
 	
 	public SymbolList(InputStream is) throws Exception {
 		Reader isr = new InputStreamReader(is);
 		LineNumberReader lnr = new LineNumberReader(isr);
 		while (true) {
 			String line = lnr.readLine();
 			if (line == null)
 				break;
 			Matcher m = lineFormat.matcher(line);
			if (!m.matches()) {
				System.err.println("Warning, ignored line number: "+lnr.getLineNumber()+" content is:"+line);
				continue;
			}
 			String symbol = m.group(1);
 			Double value = Double.parseDouble(m.group(2));
 			put(symbol, value);
 		}
 		lnr.close();
 		isr.close();
 		is.close();
 	}
 	
 	public Collection<Double> getList(Collection<String> s) {
 		Set<Double> ret = new HashSet<Double>();
 		for (String st: s) 
 			if (containsKey(st))
 				ret.add(get(st));
 		return ret;
 	}
 }
