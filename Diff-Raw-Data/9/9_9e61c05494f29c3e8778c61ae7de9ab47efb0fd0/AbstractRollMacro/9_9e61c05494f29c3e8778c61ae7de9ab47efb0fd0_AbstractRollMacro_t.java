 /* The MIT License
  * 
  * Copyright (c) 2005 David Rice, Trevor Croft
  * 
  * Permission is hereby granted, free of charge, to any person 
  * obtaining a copy of this software and associated documentation files 
  * (the "Software"), to deal in the Software without restriction, 
  * including without limitation the rights to use, copy, modify, merge, 
  * publish, distribute, sublicense, and/or sell copies of the Software, 
  * and to permit persons to whom the Software is furnished to do so, 
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be 
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE 
  * SOFTWARE.
  */
 package net.rptools.maptool.client.macro.impl;
 
 import java.math.BigDecimal;
 import java.net.URL;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.rptools.common.expression.ExpressionParser;
 import net.rptools.common.expression.Result;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.macro.Macro;
 import net.rptools.parser.ParserException;
 
 public abstract class AbstractRollMacro extends AbstractMacro {
 	
     protected String roll(String roll) {
         
         try {
         	String text = roll + " => "+ rollInternal(roll);
             
             return text;
         } catch (Exception e) {
             MapTool.addLocalMessage("<b>Unknown roll '" + roll + "', use #d#+#</b>");
             return null;
         }
     }
 
     private static final Pattern INLINE_ROLL = Pattern.compile("\\[([^\\]]+)\\]");
     public static String inlineRoll(String line) {
         Matcher m = INLINE_ROLL.matcher(line);
         StringBuffer buf = new StringBuffer();
    		while( m.find()) {
   			String roll = m.group(1);
   			
   			// Preprocessed roll already ?
   			if (roll.startsWith("roll")) {
   				continue;
   			}
   			
   			m.appendReplacement(buf, "[roll "+ roll + " &#8658; " + rollInternal(roll)+"]" );
        	}
    		m.appendTail(buf);
 
    		return buf.toString();
     }
 
     protected static String rollInternal(String roll) {
     	
       	try {
 			Result result = new ExpressionParser().evaluate(roll);
 
 	    	StringBuilder sb = new StringBuilder();
 	    	
 	    	if (result.getDetailExpression().equals(result.getValue().toString())) {
 	    		sb.append(result.getDetailExpression());
 	    	} else {
 	    		sb.append(result.getDetailExpression()).append(" = ").append((BigDecimal) result.getValue());
 	    	}
 	
 	        return sb.toString();
 		} catch (ParserException e) {
 			return "Invalid expression: " + roll;
 		}
     	
     }
 }
