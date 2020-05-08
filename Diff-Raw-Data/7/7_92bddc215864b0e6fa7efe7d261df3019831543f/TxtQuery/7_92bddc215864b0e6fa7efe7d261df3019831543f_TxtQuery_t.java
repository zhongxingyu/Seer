 package com.yahoo.dtf.query;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 import com.yahoo.dtf.query.QueryIntf;
 import com.yahoo.dtf.actions.Action;
 import com.yahoo.dtf.actions.conditionals.And;
 import com.yahoo.dtf.actions.conditionals.Condition;
 import com.yahoo.dtf.actions.conditionals.Eq;
 import com.yahoo.dtf.actions.conditionals.False;
 import com.yahoo.dtf.actions.conditionals.Gt;
 import com.yahoo.dtf.actions.conditionals.Lt;
 import com.yahoo.dtf.actions.conditionals.Neq;
 import com.yahoo.dtf.actions.conditionals.Or;
 import com.yahoo.dtf.actions.conditionals.True;
 import com.yahoo.dtf.actions.event.Field;
 import com.yahoo.dtf.actions.properties.Match;
 import com.yahoo.dtf.exception.DTFException;
 import com.yahoo.dtf.exception.ParseException;
 import com.yahoo.dtf.exception.QueryException;
 import com.yahoo.dtf.exception.StorageException;
 
 public class TxtQuery extends QueryIntf {
     
     private BufferedReader _br = null;
     
     private URI _uri = null;
     private String _event = null;
     private String _property = null;
     private String _encoding = null;
    
     private Condition _constraints = null;
     private ArrayList _fields = null;
     private ArrayList _fieldNames = null;
     
     private long _resetcount = 0;
     
     public synchronized void close() throws QueryException {
         try {
             _br.close();
         } catch (IOException e) {
             throw new QueryException("Error closing filehandle.",e);
         }
     }
 
     public HashMap next(boolean recycle) throws QueryException {
         return readNext(recycle);
     }
     
     private Condition process(Condition cond, 
                               LinkedHashMap<String, String> props,
                               String eventName)
             throws QueryException, ParseException { 
         Condition conditional = null;
        
         if (cond instanceof And) { 
             conditional = new And();
             ArrayList<Action> children = cond.children();
             for (int i = 0; i < cond.children().size(); i++) {
                 conditional.addAction(process((Condition)children.get(i),props,eventName));
             }
         } else if (cond instanceof Or) { 
             conditional = new Or();
             ArrayList children = cond.children();
             for (int i = 0; i < cond.children().size(); i++) {
                 conditional.addAction(process((Condition)children.get(i),props,eventName));
             }
         } else {  
             String field = null; 
             String value = null;
             
             if (props.containsKey(eventName + "." + cond.getOp1())) { 
                 field = cond.getOp1();
                 value = cond.getOp2();
             } else if (props.containsKey(eventName + "." + cond.getOp2())) { 
                 field = cond.getOp2();
                 value = cond.getOp1();
             } else  { 
                 if (cond.getNullable()) {
                     /*
                      * None of the operators exist therefore if this is a nullable
                      * element then we can just have always true condition because
                      * the field is null logically :)
                      */
                     return new True();
                 } else
                     return new False();
             }
             
             String fValue = props.get(eventName + "." + field);
             
             if (fValue == null) 
                 throw new QueryException("Constraint field [" + field + 
                                          "] does not exist in results.");
 
             if (cond instanceof Eq) {
                 conditional = new Eq();
             } else if (cond instanceof Neq) {
                 conditional = new Neq();
             } else if (cond instanceof Lt)  {
                 conditional = new Lt();
             } else if (cond instanceof Gt)  {
                 conditional = new Gt();
             } else if (cond instanceof Match)  {
                 conditional = new Match();
             }
 
             if (conditional == null)
                 throw new QueryException("Uknown conditional: " + 
                         cond.getClass());
            
             conditional.setOp1(fValue);
             conditional.setOp2(value);
         }
         
         return conditional;
     }
     
     private synchronized HashMap readNext(boolean recycle) throws QueryException { 
         try { 
             String line = _br.readLine();
             long linecount = 0;
             
             while (line != null) { 
                 String eventName = null;
                 /*
                  * Read a block of properties since we know that TXTRecorder
                  * separates events by an empty line with a new line.
                  */
                 LinkedHashMap<String, String> props = 
                                             new LinkedHashMap<String, String>();
                 
                 while ((line != null) && (!line.trim().equals(""))) {
                     linecount++;
                     // comment skip this line
                     if (line.charAt(0) != '#') {
                         // using split is more expensive because it uses Pattern
                         // class to do the matching.
                         int index = line.indexOf('=');
                         
                         if (index == -1) {
                             throw new QueryException("Event information has an " 
                                                       + "invalid encoding for "
                                                       + "contents [" + line 
                                                       + "] at line " + linecount);
                         }
 
                         /*
                          * XXX: may not be the best way of doing this but for 
                          * now if you want to create your own txt recorder in 
                          * another language and then process this with DTF 
                          * you'll have to make sure start is the first attribute
                          * for each event in the event files.
                          */
                         String key = line.substring(0,index);
                         if ( eventName == null ) { 
                             if (key.endsWith(".start")) {
                                 eventName = key.substring(0, key.lastIndexOf("."));
                             } else { 
                                 throw new QueryException("All events in txt query file shoud begin with the xxx.start attribute [" + line + "]");
                             }
                         }
                         
                         String keyString = key.substring(eventName.length() + 1);
                         if (_fieldNames == null || _fieldNames.contains(keyString)) {
 	                        String value = line.substring(index+1);
 	                        value = URLDecoder.decode(value,_encoding);
 	                        props.put(key, value);
                         }
                     }
                     
                     line = _br.readLine();
                 }
                 
                 /*
                  * Skip events that are not of the type specified. The easiest
                  * way to do this is to look for the default start property that
                  * would exist for every event recorded with the DTF event 
                  * recording framework.
                  */
                 if (_event != null) { 
                     if (props.get(_event + ".stop") == null)  {
                         line = _br.readLine();
                         linecount++;
                         continue;
                     }
                 }
                
                 if (_constraints != null) { 
                     Condition cond = process(_constraints, props, eventName);
                     if ( !cond.evaluate() ) {
                         line = _br.readLine();
                         linecount++;
                         continue;
                     } 
                 } 
                 
                 /*
                  * If we get here without having failed to meet the contraints
                  * then the current event is the one we should return.
                  */
                 HashMap<String, String> result = new LinkedHashMap<String, String>();
                 Iterator<Entry<String,String>> entries =  
                                                     props.entrySet().iterator();
                
                 String prop = _property + ".";
                 while ( entries.hasNext() )  { 
                     Entry<String,String> entry = entries.next();
                     String key = entry.getKey();
                     String value = entry.getValue();
                     
                     /*
                      * All results fields are store in the result attribute
                      */
                     String keyString = key.substring(eventName.length() + 1);
                     if (_property != null) {
                         result.put(prop + keyString, value);
                     } else {
                         result.put(key, value);
                     }
                 }
               
                if ( _property != null ) 
                    result.put(_property + ".event.name", eventName);
                else 
                    result.put("event.name", eventName);
                 
                 return result;
             }
             
             if (recycle) { 
             	reset();
                 return next(false);
             }
             
             /*
              * If we got this far and have nothing to return then we failed to 
              * find an event that matches the requested query.
              */
             return null;
         } catch (IOException e) { 
             throw new QueryException("Error reading file.",e);
         } catch (ParseException e) {
             throw new QueryException("Parsing exception.",e);
         } catch (DTFException e) {
             throw new QueryException("Parsing exception.",e);
         }
     }
     
     @Override
     public synchronized void reset() throws QueryException { 
     	try { 
             _br.close();
         	open(_uri, _fields, _constraints, _event, _property, _encoding);
         	_resetcount++;
     	} catch (IOException e) { 
             throw new QueryException("Error reading file.",e);
     	}
     }
     
     @Override
     public synchronized long getResetCount() { return _resetcount; }
 
     public String getProperty() { return _property; }
     
     public synchronized void open(URI uri,
 			                      ArrayList fields, 
 			                      Condition constraints,
 			                      String event, 
 			                      String property,
 			                      String encoding) throws QueryException {
         InputStream is = null;
         
         try {
             is = Action.getStorageFactory().getInputStream(uri,true);
         } catch (StorageException e) {
             throw new QueryException("Problem accessing storage.",e);
         }
         
         _uri = uri;
         _constraints = constraints;
         _fields = fields;
         _event = event;
         _property = property;
         _encoding = encoding;
         
         try {
             InputStreamReader isr = new InputStreamReader(is,_encoding); 
             _br = new BufferedReader(isr);
         } catch (UnsupportedEncodingException e) {
             throw new QueryException("Bad encoding specified.", e);
         }
      
         if (_fields != null) {
             _fieldNames = new ArrayList();
             
             for(int i = 0 ; i < _fields.size(); i++) { 
                 try {
                     _fieldNames.add(((Field)_fields.get(i)).getName());
                 } catch (ParseException e) {
                     throw new QueryException("Unable to get field name.",e);
                 }
             }
 
             if (!_fieldNames.contains("start")) 
                 _fieldNames.add("start");
             
             if (!_fieldNames.contains("stop")) 
                 _fieldNames.add("stop");
 
             if (!_fieldNames.contains("event.name")) 
                 _fieldNames.add("event.name");
         }
     }
 }
