 package org.wyona.security.core.api;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class Identity implements java.io.Serializable {
 
     private static Category log = Category.getInstance(Identity.class);
     
     protected String username;
     protected String[] groupnames;
 
     private boolean isWorld = false;
    private boolean parentGroupsAlreadyResolved = false;
 
     /**
      * Identity is WORLD
      */
     public Identity() {
         username = null;
         groupnames = null;
         isWorld = true;
     }
 
     /**
      *
      */
     public Identity(String username, String[] groupnames) {
         this.username = username;
        // TODO: What about parents groups?! This method seems to be used a lot, e.g. during policy instantiation ...!
        //log.error("DEBUG: Set groupnames via String array for user: " + username);
         this.groupnames = groupnames;
     }
     
     /**
      *
      */
     public Identity(User user) {
         try {
             this.username = user.getID();

            log.warn("Set groupnames via user object for user '" + user.getID() + "' such that also parent groups are loaded!");
            // NOTE: Setting true means that also parent groups shall be loaded (groups in groups)!
            Group[] groups = user.getGroups(true);
             groupnames = new String[groups.length];
            for (int i = 0; i < groups.length; i++) {
                 groupnames[i] = groups[i].getID();
             }
            parentGroupsAlreadyResolved = true;
         } catch (AccessManagementException e) {
             log.error(e, e);
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     /**
      * 
      */
     public String getUsername() {
         return username;
     }
 
 /* WARNING: This method leads to problems re serialization within a clustered environment!
     public User getUser() {
         return user;
     }
 */
     
     /**
      * 
      */
     public String[] getGroupnames() {
         // For security reasons a copy instead the reference is being returned
         if (groupnames != null) {
             String[] copy = new String[groupnames.length];
             for (int i = 0; i < groupnames.length; i++) {
                 copy[i] = groupnames[i];
             }
             return copy;
         } else {
             return null;
         }
     }
     
     /**
      * @param parents If true, then all parents (and their parents (and ...)) shall also be returned
      */
     public String[] getGroupnames(boolean parents) {
         if (parents) {
            if (parentGroupsAlreadyResolved) {
                return getGroupnames();
            } else {
                String uname = getUsername();
                if (uname == null) uname = "WORLD";
                log.warn("TODO: Implementation of finding parent groups for user '" + uname + "' not finished yet!");
                return getGroupnames();
            }
/*
            //log.debug("Groups which user " + getUsername() + " belongs to:");
             String[] gns = getGroupnames();
             if (gns != null) {
                 for (int i = 0; i < gns.length; i++) {
                    log.debug("Group: " + gns[i]);
                 }
             }
             return gns;
*/
         } else {
             return getGroupnames();
         }
     }
     
     /**
      *
      */
     private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
         out.defaultWriteObject();
         // TODO: Does this actually make sense?!
         //out.defaultObject(username);
         //out.defaultObject(groupnames);
     }
 
     /**
      *
      */
     private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
         in.defaultReadObject();
         // TODO: Does this actually make sense?!
         //username = (String) in.readObject();
         //groupnames = (String[]) in.readObject();
     }
     
     public String toString() {
         if (getUsername() == null) return "WORLD";
         return getUsername();
     }
 
     /**
      *
      */
     public boolean isWorld() {
         return isWorld;
     }
 }
