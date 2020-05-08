 package com.htmlthor;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import org.json.simple.JSONObject;
 
 
 public class StructureBreakdown {
 
 	private Map<String, StructureBreakdown> subfiles;
 	private String type;
	private String name;
 
 
 	/* Constructor */
 	public StructureBreakdown() {
 		subfiles = new HashMap<String, StructureBreakdown>();
 	}
 	
 	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		
 	}
 	
 	/* adds a file to the structure */
 	public void addSubfile(String filename, StructureBreakdown file) {
 		subfiles.put(filename, file);
 	}
 	
 	/* gets a subfile/subfolder */
 	public StructureBreakdown getSubfile(String filename) {
 		return subfiles.get(filename);
 	}
 	
 	/* set type - should be either "file" or "folder" */
 	public void setType(string t) {
 		type = t;
 	}
 
 
 
 }
