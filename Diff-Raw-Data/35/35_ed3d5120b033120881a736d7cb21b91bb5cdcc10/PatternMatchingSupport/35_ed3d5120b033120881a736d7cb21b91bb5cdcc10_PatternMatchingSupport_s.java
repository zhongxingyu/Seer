 /*
  * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.util.formatting;
 
 import org.ijsberg.iglu.util.misc.StringSupport;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Helper class that validates formats of values.
  */
 public abstract class PatternMatchingSupport {
 
 	private static HashMap<String, Pattern> cache = new HashMap<String, Pattern>();
 
 
 	/**
 	 * Returns true only if the value matches the regular expression
 	 * only once and exactly.
 	 *
 	 * @param val	string value that may match the expression
 	 * @param regexp regular expression
 	 * @return true if val matches regular expression regexp
 	 */
 	public static boolean valueMatchesRegularExpression(String val, String regexp) {
 		Pattern p = cache.get(regexp);
 		if(p == null) {
 			p = Pattern.compile(regexp);
 			cache.put(regexp, p);
 		}
 		return valueMatchesRegularExpression(val, p);
 	}
 
 	/**
 	 * Returns true only if the value matches the regular expression
 	 * at least once.
 	 *
 	 * @param val	string value that may match the expression
 	 * @param regexp regular expression
 	 * @return true if val matches regular expression regexp
 	 */
 	public static boolean valueMatchesRegularExpression(String val, Pattern regexp) {
 		Matcher m = regexp.matcher(val);
		return m.matches();
 	}
 
 	public static List<int[]> getRangesMatchingRegularExpression(String val, String regexp) {
 
 		List<int[]> retval = new ArrayList<int[]>();
 		
 		Pattern p = Pattern.compile(regexp);
 		Matcher matcher = p.matcher(val);
 
 		while (matcher.find()) {
 			retval.add(new int[] {matcher.start(), matcher.end()});
 		}
 		  
 		if (matcher.matches()) {
 			retval.add(new int[] {matcher.start(), matcher.end()});
 		}
 		return retval;
 	}
 
 	
 	
 	/**
 	 * Matches DOS-type wildcardexpressions rather than regular expressions.
 	 * The function adds one little but handy feature of regular expressions:
 	 * The '|'-character is regarded as a boolean OR that separates multiple expressions.
 	 *
 	 * @param val string value that may match the expression
 	 * @param exp expression that may contain wild cards
 	 * @return
 	 */
 	public static boolean valueMatchesWildcardExpression(String val, String exp) {
 		//replace [\^$.|?*+() to make regexp do wildcard match
 		String expCopy = StringSupport.replaceAll(
 				exp,
 				new String[]{"[", 	"\\", 	"^", 	"$", ".",	"?", "*", "+", "(", ")"},
 				new String[]{"\\[", "\\\\", "\\^", "\\$", "\\.",".?", ".*", "\\+", "\\(", "\\)"});
 
 		return (valueMatchesRegularExpression(val, expCopy));
 	}
 	
 
 
 }
