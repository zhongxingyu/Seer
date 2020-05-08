 package org.valabs.odisp.standart;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.valeks.xlang.parser.Parser;
 import org.valeks.xlang.parser.Tag;
 import org.valeks.xlang.parser.XLangException;
 
 
 /**   .
  * @author (C) 2004 <a href="valeks@valabs.spb.ru"> . </a>
 * @version $Id: ConfigurationManager.java,v 1.3 2004/11/30 07:42:09 boris Exp $
  */
 class ConfigurationManager implements org.valabs.odisp.common.ConfigurationManager {
   List objects = new ArrayList();
   List resources = new ArrayList();
   MultiMap params = new MultiMap();
   Logger log = Logger.getLogger(ConfigurationManager.class.getName());
   
   public ConfigurationManager() {
     log.setLevel(Level.ALL);
   }
   
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#supportComponentListing()
    */
   public boolean supportComponentListing() {
     return true;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#getResourceList()
    */
   public List getResourceList() {
     return resources;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#getObjectList()
    */
   public List getObjectList() {
     return objects;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#supportParameterFetching()
    */
   public boolean supportParameterFetching() {
     return true;
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#getParameter(java.lang.String, java.lang.String)
    */
   public String getParameter(String domain, String paramName) {
     return params.get(domain, paramName);
   }
 
   /* (non-Javadoc)
    * @see org.valabs.odisp.common.ConfigurationManager#setCommandLineArguments(java.lang.String[])
    */
   public void setCommandLineArguments(String[] args) {
     try {
 	    InputStream inp = new FileInputStream(args[0]);
         Parser p = new Parser(inp);
 	    loadConfiguration(p.getRootTag());
       } catch (FileNotFoundException e) {
 	    log.severe("configuration file " + args[0] + " not found.");
       } catch (XLangException e) {
         log.severe("configuration file " + args[0] + " contains unrecoverable errors: " + e);
       }
   }
   
   private void loadConfiguration(Tag docTag) {
     Iterator it = docTag.getChild().iterator();
     params.putAll("root", getParamsForTag(docTag));
     while (it.hasNext()) {
       Tag curt = (Tag) it.next();
       if (curt.getName().equalsIgnoreCase("object")) {
         String className = (String) curt.getAttributes().get("name");
         if (className == null) {
           log.warning("object tag has no name attribute. ignoring.");
           continue;
         }
         Map lparams = getParamsForTag(curt);
         objects.add(new org.valabs.odisp.common.ConfigurationManager.ComponentConfiguration(className, lparams));
         params.putAllPrefixed("boot", className, lparams);
       } else if (curt.getName().equalsIgnoreCase("resource")) {
         String className = (String) curt.getAttributes().get("name");
         if (className == null) {
           log.warning("resource tag has no name attribute. ignoring.");
           continue;
         }
         Map lparams = getParamsForTag(curt);
         resources.add(new org.valabs.odisp.common.ConfigurationManager.ComponentConfiguration(className, lparams));
         params.putAllPrefixed("boot", className, lparams);
       }
     }
     log.fine("Found " + resources.size() + " resources and " + objects.size() + " objects to load.");
   }
 
 
   /**       .
    * @param childTag 
    */
   private Map getParamsForTag(final Tag childTag) {
     Map params = null;
     if (childTag.getChild().size() != 0) {
       params = new HashMap();
       //   --       
       Iterator cit = childTag.getChild().iterator();
       while (cit.hasNext()) {
         Tag ctag = (Tag) cit.next();
         if (ctag.getName().equalsIgnoreCase("param")) {
           String paramName = (String) ctag.getAttributes().get("name");
           String paramValue = (String) ctag.getAttributes().get("value");
           if (paramName != null && paramValue != null) {
             params.put(paramName, paramValue);
           }
         }
       }
     }
     return params;
   }
 
   class MultiMap {
     private Map domains = new HashMap();
     public void put(final String domainName, final String param, final String value) {
       getDomain(domainName).put(param, value);
     }
     
     public String get(final String domainName, final String paramName) {
       return (String) getDomain(domainName).get(paramName);
     }
     
     public void putAll(final String domainName, final Map params) {
       getDomain(domainName).putAll(params);
     }
     
     public void putAllPrefixed(final String domainName, final String prefix, final Map params) {
      //         ;)
      if(params == null)return;

       Map domain = getDomain(domainName);
       Iterator keyIt = params.keySet().iterator();
       while (keyIt.hasNext()) {
         String key = (String) keyIt.next();
         domain.put(prefix + key, params.get(key));
       }
     }
     
     public Map getDomain(final String domain) {
       Map result;
       if (domains.containsKey(domain)) {
         result = (Map) domains.get(domain);
       } else {
         result = new HashMap();
         domains.put(domain, result);
       }
       return result;
     }
   }
 }
