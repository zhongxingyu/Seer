 package no.feide.moria.authorization;
 
 import java.util.logging.Logger;
 import java.util.HashMap;
 import java.util.Iterator;
 
 
 /**
  * This class represents a LDAP attribute and is used for
  * authorization of a web service. Both Profile and WebService have
  * lists of attributes.
  */
 public class Attribute {
 
     /** Used for logging. */
     private static Logger log = Logger.getLogger(Attribute.class.toString());
 
     /** Name of attribute */
     private String name = null;
 
     /** Is this attribute allowd in use with SSO */
     private Boolean sso = null;
 
     /** Security level */
     private int secLevel = 1;
     
     /** Security level register */
     private static HashMap secLevels = initSecLevels();
     
 
     /**
      * Constructor
      * @param name Attribute name
      * @param sso Allow use of SSO with this attribute
      */
     protected Attribute(String name, String ssoStr, String secLevelStr) {
         log.finer("Attribute(String, boolean, String)");
 
         /* Set security level */
        if (secLevelStr == null || secLevelStr.equals("")) {
            log.warning("Attribute secLevel not set. Defaults to HIGH.");
             secLevelStr = "HIGH";
        }
 
         if (!secLevels.containsKey(secLevelStr)) {
            log.warning("Invalid attribute secLevel: \""+secLevelStr+"\" Set to default (HIGH).");
             secLevelStr = "HIGH";
         }
 
 
         if (ssoStr == null)
             ssoStr = "false";
 
         secLevel = ((Integer) secLevels.get(secLevelStr)).intValue();
         sso = new Boolean(ssoStr.equals("true"));
         this.name = name;
     }
 
 
     protected Attribute(String name, boolean sso, int secLevel) {
         this.name = name;
         this.sso  = new Boolean(sso);
         this.secLevel = secLevel;
     }
     
     /** Return security level */
     public int getSecLevel() {
         return secLevel;
     }
 
 
     /** Initialize security level register. */
     private static HashMap initSecLevels() {
         HashMap secLevels = new HashMap();
         secLevels.put("HIGH", new Integer(3));
         secLevels.put("MEDIUM", new Integer(2));
         secLevels.put("LOW", new Integer(1));
         return  secLevels;
     }
 
 
     /**
      * Find the name for a given security level.
      */
     public static String secLevelName(int level) {
 
         for (Iterator it = secLevels.keySet().iterator(); it.hasNext(); ) {
             String key = (String) it.next();
             if (((Integer)secLevels.get(key)).intValue() == level)
                 return key;
         }
         
         log.warning("Unknown security level: "+level);
         return "UNKNOWN";
     }
 
     /**
      * Constructor
      * @param name Attribute name
      */
     public Attribute(String name) {
         log.finer("Attribute(String)");
 
         this.name = name;
         this.sso = null;
     }
 
 
     
     /**
      * Get name of attribute.
      */
     public String getName() {
         log.finer("getName()");
 
         return name;
     }
 
 
 
     /**
      * Is the attribute allowed in use with SSO?
      */
     public boolean allowSso() {
         log.finer("allowSso()");
 
         return sso.booleanValue();
     }
 
 
 }
