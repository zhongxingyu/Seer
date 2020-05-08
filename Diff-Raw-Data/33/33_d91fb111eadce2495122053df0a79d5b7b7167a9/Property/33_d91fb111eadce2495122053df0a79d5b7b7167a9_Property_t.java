 package com.sparkedia.valrix.BackToBody;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public final class Property {
 	private LinkedHashMap<String, Object> props = new LinkedHashMap<String, Object>();
 	private String file;
 	private final String pname = BackToBody.name;
 	private final Logger log = BackToBody.log;
 
 	public Property(final String file) {
 		this.file = file;
 		if (new File(file).exists()) {
 		    load();
 		}
 	}
 	
 	// Load data from file into ordered HashMap
 	public Property load() {
         BufferedReader br = null;
         try {
             br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
             String line;
             byte cc = 0; // # of comments
             short lc = 0; // # of lines
             int delim;
             
             // While there are lines to read (auto-breaks)
             while (null != (line = br.readLine())) {
                 // Is a comment, store it
                 if ('#' == line.charAt(0) && 0 != lc) {
                     props.put("#"+cc, line.substring(line.indexOf(' ')+1).trim());
                     cc++;
                     lc++;
                     continue;
                 }
                 // Isn't a comment, store the key and value
                 while (-1 != (delim = line.indexOf('='))) {
                     props.put(line.substring(0, delim).trim(), line.substring(delim+1).trim());
                     break;
                 }
             }
         } catch (final FileNotFoundException e) {
             log.log(Level.SEVERE, '['+pname+"]: Couldn't find file "+file, e);
         } catch (final IOException e) {
             log.log(Level.SEVERE, '['+pname+"]: Unable to save "+file, e);
         } finally {
             // Close the reader
             try {
                 if (null != br) br.close();
             } catch (final IOException e) {
                 log.log(Level.SEVERE, '['+pname+"]: Unable to save "+file, e);
             }
         }
         return this;
     }
     
     // Save data from LinkedHashMap to file
     public Property save() {
         BufferedWriter bw = null;
         try {
             // Construct the BufferedWriter object
             bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
             
             // Save all the props one at a time, only if there's data to write
             if (props.size() > 0) {
                 // Grab all the entries and create an iterator to run through them all
                 final Set<?> set = props.entrySet();
                 final Iterator<?> i = set.iterator();
                 
                 // While there's data to iterate through..
                 while (i.hasNext()) {
                     // Map the entry and save the key and value as variables
                     final Map.Entry<?, ?> me = (Map.Entry<?, ?>)i.next();
                     final String key = (String)me.getKey();
                     final String val = (String)me.getValue();
                     
                     // If it starts with "#", it's a comment so write it as such
                     if ('#' == key.charAt(0)) {
                         // Writing a comment to the file
                         bw.write("# "+val);
                         bw.newLine();
                     } else {
                         // Otherwise write the key and value pair as key=value
                         bw.write(key+'='+val);
                         bw.newLine();
                     }
                 }
             }
         } catch (final FileNotFoundException e) {
             log.log(Level.SEVERE, '['+pname+"]: Couldn't find file "+file, e);
         } catch (final IOException e) {
             log.log(Level.SEVERE, '['+pname+"]: Unable to save "+file, e);
         } finally {
             // Close the BufferedWriter
             try {
                 if (null != bw) {
                     bw.close();
                 }
             } catch (final IOException e) {
                 log.log(Level.SEVERE, '['+pname+"]: Unable to close buffer for "+file, e);
             }
         }
         return this;
     }
    
    // Return current map
    public Map<String, Object> copy() {
        return props;
    }
    
    // Convert current map to specified map
    public void paste(final Map<String, ?> map) {
        props.putAll(map);
    }
 	
 	// Extend ability to clear all mappings
     public Property clear() {
         props.clear();
         return this;
     }
     
     public Property remove(final String key) {
         props.remove(key);
         return this;
     }
 	
 	// Check if the key exists or not
 	public boolean keyExists(final String key) {
 		return props.containsKey(key) ? true : false;
 	}
 	
 	// Check if the key no value
 	public boolean isEmpty(final String key) {
 		return ((String)props.get(key)).isEmpty() ? true : false;
 	}
 	
 	// Set property value as a String
 	public Property setString(final String key, final String value) {
 	    props.put(key, value);
 	    return this;
 	}
 	
 	// Set property value as a Number
     public Property setNumber(final String key, final Number value) {
         props.put(key, String.valueOf(value));
         return this;
     }
     
     // Set property value as a Boolean
     public Property setBool(final String key, final Boolean value) {
         props.put(key, value);
         return this;
     }
     
     // Get property value as a String
     public String getString(final String key) {
         return props.containsKey(key) ? (String)props.get(key) : "";
     }
     
     // Get property value as a boolean
     public boolean getBool(final String key) {
         return props.containsKey(key) ? Boolean.parseBoolean((String)props.get(key)) : false;
     }
 	
 	// Get property value as an int
 	public int getInt(final String key) {
 		return props.containsKey(key) ? Integer.parseInt((String)props.get(key)) : 0;
 	}
 	
 	// Get property value as a double
 	public double getDouble(final String key) {
 		return props.containsKey(key) ? Double.parseDouble((String)props.get(key)) : 0.0D;
 	}
 }
