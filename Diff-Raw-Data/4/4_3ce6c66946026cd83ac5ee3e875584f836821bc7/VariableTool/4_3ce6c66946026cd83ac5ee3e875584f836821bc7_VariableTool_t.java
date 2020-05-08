 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 package org.webmacro.servlet;
 
 import org.webmacro.Context;
 import org.webmacro.ContextTool;
 import org.webmacro.PropertyException;
 
 /**
  * A ContextTool which allows one to snoop information about an object
  * in the active Context.
  *
  * @author Zeljko Trogrlic
  * @author Eric B. Ridge (mailto: ebr@tcdi.com)
  */
 
 public class VariableTool implements ContextTool {
 
    Context context;
 
    public VariableTool() {
    }
 
    public VariableTool(Context newContext) {
       context = newContext;
    }
 
    public Object init(Context c) throws PropertyException {
       return new VariableTool(c);
    }
 
    public void destroy(Object o) {
    }
 
    /**
     * Is the specified object <code>name</code> defined in the active
     * Context?
     */
    public boolean isDefined(Object name) {
       return context.containsKey(name);
    }
    
    /**
     * Is the specified object, <code>obj</code>, an instance of the
     * specified <code>className</code>?<p>
     *
     * If either parameter is <code>null</code> this method returns false.<br>
     * If <code>className</code> cannot be found, this method returns false.<br>
     *
     * @param obj an Object from your template Context
     * @param className the <b>fully-qualified</b> class name to check 
     */
    public boolean isInstanceOf(Object obj, String className) {
       try {
          return (obj != null && className != null) 
            && (context.getBroker().classForName(className).isAssignableFrom(
               obj.getClass()));
       } catch (ClassNotFoundException cnfe) {
          context.getBroker().getLog("VariableTool")
                             .error ("VariableTool could not locate the class: /" 
                                     + className + "/");
       } catch (Exception e) {
          context.getBroker().getLog("VariableTool")
                             .error ("An unexpected exception occured", e);
       }
       return false; 
    }
 }
