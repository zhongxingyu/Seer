 import java.io.*;
 import java.lang.*;
 import java.util.*;
 
 /* Wrapper to support re-entrancy for Qss - needs to take a pipe as input */
 public class Parser {
     private Qss qss = new Qss(new StringReader(""));
 
 	public Parser() {
 	}
 
 	public boolean booleanVal(Evidence ev, String exp) {
 		return parse(ev, exp).booleanVal();
 	}
 
 	public String stringVal(Evidence ev, String exp) {
		return parse(ev, exp).stringVal(false);
	}
	
	public String stringVal(Evidence ev, String exp, boolean showReserved) {
		return parse(ev,exp).stringVal(showReserved);
 	}
 
 	public double doubleVal(Evidence ev, String exp) {
 		return parse(ev, exp).doubleVal();
 	}
 
 	public long longVal(Evidence ev, String exp) {
 		return parse(ev, exp).longVal();
 	}
 
 	public Datum parse(Evidence ev, String exp) {
 		qss.ReInit(new StringReader(exp));
 		return qss.parse(ev);
 	}
 
 	public boolean hasErrors() {
 		return qss.hasErrors();
 	}
 
 	public Vector getErrors() {
 		return qss.getErrors();
 	}
 
 	public String parseJSP(Evidence ev, String msg) {
 		java.util.StringTokenizer st = new java.util.StringTokenizer(msg,"`",true);
 		StringBuffer sb = new StringBuffer();
 		String s;
 		boolean inside = false;
 
 		while(st.hasMoreTokens()) {
 			s = st.nextToken();
 			if ("`".equals(s)) {
 				inside = (inside) ? false : true;
 				continue;
 			}
 			else {
 				if (inside) {
					sb.append(stringVal(ev,s,true));	// so that see the *REFUSED*, etc as part of questions
 				}
 				else {
 					sb.append(s);
 				}
 			}
 		}
 		return sb.toString();
 	}
 }
