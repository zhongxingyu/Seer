 package org.kisst.util;
 
 import org.kisst.cfg4j.Props;
 
 public class StringUtil {
 
 	public static String quotedName(String name) {
 		if (name.indexOf(' ')>=0 || name.indexOf('.')>=0)
 			return '"'+name+'"';
 		else
 			return name;
 	}
 
 	public static String substitute(String str, Props vars) {
 		StringBuilder result = new StringBuilder();
 		int prevpos=0;
 		int pos=str.indexOf("${");
 		while (pos>=0) {
 			int pos2=str.indexOf("}", pos);
			if (pos2<0)
				throw new RuntimeException("Unbounded ${ starting with "+str.substring(pos,pos+10));
 			String key=str.substring(pos+2,pos2);
 			result.append(str.substring(prevpos,pos));
 			Object value=vars.get(key,null);
 			if (value==null && key.equals("dollar"))
 				value="$";
 			if (value==null)
 				throw new RuntimeException("Unknown variable ${"+key+"}");
 			result.append(value.toString());
 			prevpos=pos2+1;
 			pos=str.indexOf("${",prevpos);
 		}
 		result.append(str.substring(prevpos));
 		return result.toString();
 	}
 }
