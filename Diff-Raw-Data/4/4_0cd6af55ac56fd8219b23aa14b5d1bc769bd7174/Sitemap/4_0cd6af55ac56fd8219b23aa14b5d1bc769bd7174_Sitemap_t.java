 package org.codehaus.xsite.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Holds the structure of a website.
  *
  * @author Joe Walnes
 * @author Mauro Talevi
 * @author J&ouml;rg Schaible
  */
 public class Sitemap {
 
 	private List<Parameter> params = new ArrayList<Parameter>();
     private List<Section> sections = new ArrayList<Section>();
     private transient Map<String, String> parameters;
 
     public void addSection(Section section) {
         sections.add(section);
     }
 
     public List<Section> getSections() {
         return Collections.unmodifiableList(sections);
     }
 
     public List<Entry> getAllEntries() {
         List<Entry> list = new ArrayList<Entry>();
         for (Section section : sections ){
             for ( Entry entry : section.getEntries() ){
                 list.add(entry);
             }
         }
         return Collections.unmodifiableList(list);
     }
 
     public List<Page> getAllPages() {
         List<Page> list = new ArrayList<Page>();
         for (Section section : sections ){
             for ( Page page : section.getPages() ){
                 list.add(page);
             }
         }
         return Collections.unmodifiableList(list);
     }
     
     public Map<String, String> getParameter() {
     	if (parameters == null) {
 	    	parameters = new HashMap<String, String>();
 	    	if (params != null) {
 		    	for(Parameter p : params) {
 		    		parameters.put(p.getName(), p.getValue());
 		    	}
 	    	}
     	}
     	return Collections.unmodifiableMap(parameters);
     }
 }
