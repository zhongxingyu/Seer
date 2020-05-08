 package com.pjaol.ESB.formatters;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.solr.common.util.NamedList;
 
 public class XMLFormatter extends Formatter {
 
 	String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
 	String docStart = "<doc>";
 	String docEnd = "</doc>";
 	
 	
 	@Override
 	public String toOutput(NamedList output) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(xmlHeader);
 		sb.append(docStart);
 		
 		sb.append(recurseNamedList(output));
 	
 		
 		sb.append(docEnd);
 		return sb.toString();
 	}
 
 	
 	private String recurseNamedList(NamedList output){
 		
 		StringBuilder sb = new StringBuilder();
 		int sz = output.size();
 		
 		for(int i =0; i < sz; i++){
 			String k = output.getName(i);
			Object v = output.getVal(i);
 			sb.append("<f name=\""+k+"\">");
 			if (v instanceof Map){
 				sb.append(recurseMap((Map)v));
 			} else if (v instanceof List){
 				sb.append(recurseList((List)v));
 			} else if (v instanceof NamedList){
 				sb.append(recurseNamedList((NamedList)v));
 			} else {
 				sb.append(v.toString());
 			}
 			
 			sb.append("</f>");
 		}
 		return sb.toString();
 	}
 	
 	
 	// Basic Map iterator
 	private String recurseMap(Map items){
 		StringBuilder sb = new StringBuilder();
 		
 		// Some sort of map 
 		Set keys = items.keySet();
 		Iterator i = keys.iterator();
 		while(i.hasNext()){
 			String k = (String)i.next();
 			Object v = items.get(k);
 			sb.append("<f name=\""+k+"\">");
 			
 			if (v instanceof Map){
 			
 				sb.append(recurseMap((Map)v));
 			} else if (v instanceof List) {
 				
 				sb.append(recurseList((List) v));
 			} else if (v instanceof NamedList){
 				
 				sb.append(recurseNamedList((NamedList)v));
 			} else {
 				
				sb.append(new String(v.toString()));
 			}
 			sb.append("</f>");
 		}
 		
 		return sb.toString();
 	}
 	
 	// Basic List iterator / recurse
 	private String recurseList (List items){
 		StringBuilder sb = new StringBuilder();
 		
 		for(Object i: items){
 			
 			if (i instanceof List){
 				sb.append(recurseList((List)i));
 			} else if (i instanceof Map){
 				sb.append(recurseMap((Map)i));
 			} else {
 				sb.append("<f>"+i.toString()+"</f>");
 			}
 			
 		}
 		
 		return sb.toString();
 	}
 	
 }
