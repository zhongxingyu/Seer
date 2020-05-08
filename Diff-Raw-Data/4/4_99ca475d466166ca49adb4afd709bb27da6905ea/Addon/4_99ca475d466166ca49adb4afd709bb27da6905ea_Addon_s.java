 package com.suse.addons.model;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import com.suse.addons.registry.AddonRegistry;
 
 /**
  * Class representing a single add-on bean.
  */
 public class Addon {
 
     // JNDI name used to lookup the registry
     private static final String ADDONS = "suse/addons";
 
     private String name;
     private String group;
     private String entry;
 
     /**
      * Default constructor.
      */
     public Addon() {
     }
 
     /**
      * Constructor.
      * @param name
      * @param entry
      */
     public Addon(String name, String group, String entry) {
         this.setName(name);
         this.setGroup(group);
         this.setEntry(entry);
     }
 
     /**
      * @return the name
      */
     public String getName() {
         return name;
     }
 
     /**
      * @param name the name to set
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * @return the group
      */
     public String getGroup() {
         return group;
     }
 
     /**
      * @param group the group to set
      */
     public void setGroup(String group) {
         this.group = group;
     }
 
     /**
      * @return the entry
      */
     public String getEntry() {
         return entry;
     }
 
     /**
      * @param entry the entry to set
      */
     public void setEntry(String entry) {
         this.entry = entry;
     }
 
     /**
      * Register this add-on.
      */
    public void register() throws Exception {
         try {
             Context context = (Context) new InitialContext().lookup("java:comp/env");
             AddonRegistry registry = (AddonRegistry) context.lookup(ADDONS);
             registry.register(this);
         } catch (NamingException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Unregister this add-on.
      */
     public void unregister() {
         try {
             Context context = (Context) new InitialContext().lookup("java:comp/env");
             AddonRegistry registry = (AddonRegistry) context.lookup(ADDONS);
             registry.unregister(this);
         } catch (NamingException e) {
             e.printStackTrace();
         }
     }
 }
