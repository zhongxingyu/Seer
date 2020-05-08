 /******************************************************************************
  * Configuration.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.server;
 
 import java.io.*;
 import java.util.*;
 
 import org.openlaszlo.utils.ChainedException;
 
 import org.apache.log4j.Logger;
 import org.apache.regexp.RE;
 import org.apache.regexp.RESyntaxException;
 import org.jdom.*;
 import org.jdom.input.SAXBuilder;
 import org.jdom.filter.ElementFilter;
 
 /**
  * Configuration contains global server configuration information.
  * It reads an array of options from an xml file.
  * 
  * @author Eric Bloch
  * @version 1.0 
  */
 public class Configuration {
 
     Map mOptions      = new Hashtable();
     Map mAppPaths     = new Hashtable();
     Map mAppPatterns  = new Hashtable();
     ArrayList mAppRegexps   = new ArrayList();
     Map mAppOptions   = new Hashtable(); // options set in application LZX
    Map mDebugRegexps = new Hashtable();
    
     private static Logger mLogger  = Logger.getLogger(Configuration.class);
 
     /**
      * Constructs a new configuration
      */
     public Configuration() 
     {
         try {
             mLogger.debug("building configuration");
             SAXBuilder builder = new SAXBuilder();
     
             String fileName = LPS.getConfigDirectory() + File.separator + "lps.xml";
             Document doc = builder.build(new File(fileName));
             Element root = doc.getRootElement();
             List elts = root.getContent(new ElementFilter()); 
             ListIterator iter = elts.listIterator();
 
             while(iter.hasNext()) {
                 Element elt = (Element)iter.next();
 
                 // Check for global default options. 
                 if (elt.getName().equals("option")) {
                     String name = elt.getAttributeValue("name");
                     if (name != null && !name.equals("")) {
                         Option option = new Option(elt);
                         mOptions.put(name, option);
                     }
                 }
 
                 // Check for application specific options
                 if (elt.getName().equals("application")) {
                     String path    = elt.getAttributeValue("path");
                     String pattern = elt.getAttributeValue("pattern");
 
                     if (path    != null  && ! path.equals("") &&
                         pattern != null  && ! pattern.equals("")) {
                         String msg = 
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Can't have attributes path (" + p[0] + ")" + " and pattern (" + p[1] + ") " + " defined in an application element together."
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 Configuration.class.getName(),"051019-80", new Object[] {path, pattern});
                             
                         mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Exception reading configuration: " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 Configuration.class.getName(),"051019-88", new Object[] {msg})
                             );
                         throw new ChainedException(msg);
                     }
 
                     if (path != null && !path.equals("")) {
                         Hashtable appOpts = getOptions(elt, "option", "name");
                         if (appOpts.size() != 0)
                             mAppPaths.put(path, appOpts);
                     } 
 
                     if (pattern != null && !pattern.equals("")) {
                         Hashtable appOpts = getOptions(elt, "option", "name");
                         if (appOpts.size() != 0) {
                             RE re = new RE(pattern);
                            mDebugRegexps.put(re, pattern);
                             mAppRegexps.add(re);
                             mAppPatterns.put(re, appOpts);
                         }
                     }
                 }
             }
         } catch (RESyntaxException e) {
 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="RE exception reading configuration " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 Configuration.class.getName(),"051019-116", new Object[] {e.getMessage()})
             );
             throw new ChainedException(e.getMessage());
         } catch (JDOMException e) {
 mLogger.error(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="jdom exception reading configuration " + p[0]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 Configuration.class.getName(),"051019-126", new Object[] {e.getMessage()})
             );
             throw new ChainedException(e.getMessage());
         } catch (IOException e) {
             mLogger.error("io exception reading configuration " + e.getMessage());
             throw new ChainedException(e.getMessage());
         }
     }
 
 
     /**
      * @param app element
      * @param tagname tag name
      * @param optname option name
      * @return hashtable of options
      */
     public static Hashtable getOptions(Element app, String tagname, String optname)
     {
         Hashtable options = new Hashtable();
         List elts = app.getContent(new ElementFilter());
         ListIterator iter = elts.listIterator();
         while (iter.hasNext()) {
             Element elt = (Element)iter.next();
             if (elt.getName().equals(tagname)) {
                 String name = elt.getAttributeValue(optname);
                 if (name != null && !name.equals("")) {
                    addOptionNamed(options, elt, name);
                 }
             }
         }
         return options;
     }
 
    public String getPattern(RE re) {
        return (String) mDebugRegexps.get(re);
    }

     public static void addOption(Map options, Element elt) {
         String name = elt.getName();
         Option option = (Option)options.get(name);
         if (option == null) {
             option = new Option(elt);
             options.put(name, option);
         } else {
             option.addElement(elt);
         }
     }
     
    public static void addOptionNamed(Map options, Element elt, String name) {
        Option option = (Option)options.get(name);
        if (option == null) {
            option = new Option(elt);
            options.put(name, option);
        } else {
            option.addElement(elt);
        }
    }


     /**
      * @param path application path - .lzo extensions treated like .lzx
      * @return table of application options
      */
     public Map getApplicationOptions(String path) {
         if (path.endsWith(".lzo")) {
             path = path.substring(0, path.length()-1) + 'x';
         }
         return (Map)mAppOptions.get(path);
     }
 
     /**
      * @param path application path - .lzo extensions treated like .lzx
      * @param opts application option hashtable from LZX
      */
     public void setApplicationOptions(String path, Map opts) {
         if (path.endsWith(".lzo")) {
             path = path.substring(0, path.length()-1) + 'x';
         }
         mAppOptions.put(path, opts);
     }
 
 
     /**
      * @return true if the option is allowed for given value of the 
      * given key
      */
     public boolean optionAllows(String key, String value) {
         return optionAllows(key, value, true);
     }
 
     /**
      * @param allow if true, an undefined option means that it is
      * allowed, else it is denied.
      * @return true if the option is allowed for given value of the 
      * given key
      */
     public boolean optionAllows(String key, String value, boolean allow) {
         Option opt = (Option)mOptions.get(key);
         if (opt != null) {
             return opt.allows(value, allow);
         } else {
 mLogger.debug(
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="No option for " + p[0] + "; is allowed? " + p[1]
  */
             org.openlaszlo.i18n.LaszloMessages.getMessage(
                 Configuration.class.getName(),"051019-215", new Object[] {key, new Boolean(allow)})
             );
             return allow;
         }
     }
 
 
     /**
      * @param path application path relative to webapp.
      * @return true if the option is allowed for given value of the 
      * given key
      */
     public boolean optionAllows(String path, String key, String value) {
         return optionAllows(path, key, value, true);
     }
 
     /**
      * @param path application path relative to webapp.
      * @param allow if true, an undefined option means that it is
      * allowed, else it is denied.
      * @return true if the option is allowed for given value of the 
      * given key
      */
     public boolean optionAllows(String path, String key, String value,
                                 boolean allow) {
         Option opt;
         Map appOptions;
 
         // Check LZX configured application options.
         appOptions = (Map)mAppOptions.get(path);
         if (appOptions != null) {
             opt = (Option)appOptions.get(key);
             if (opt != null) {
                 return opt.allows(value, allow);
             }
         }
 
         // Check for server configured application option.
         appOptions = (Map)mAppPaths.get(path);
         if (appOptions != null) {
             opt = (Option)appOptions.get(key);
            if (opt != null) {
                 return opt.allows(value, allow);
            }
         }
 
         // Check regexp patterns
         if (! mAppRegexps.isEmpty()) {
             ListIterator iter = mAppRegexps.listIterator();
             while (iter.hasNext()) {
                 RE re = (RE)iter.next();
                 if (re.match(path)) {
                     appOptions = (Map)mAppPatterns.get(re);
                     if (appOptions != null) {
                         opt = (Option)appOptions.get(key);
                         if (opt != null) {
                             return opt.allows(value, allow);
                         }
                     }
                 }
             }
         }
 
         // Check for global option.
         return optionAllows(key, value, allow);
     }
 }
