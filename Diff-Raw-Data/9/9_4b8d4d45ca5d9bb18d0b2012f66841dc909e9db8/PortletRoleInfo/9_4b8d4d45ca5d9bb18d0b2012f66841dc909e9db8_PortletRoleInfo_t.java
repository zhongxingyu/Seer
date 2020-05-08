 package org.gridlab.gridsphere.portlet;
 
 /*
  * @author <a href="mailto:oliver.wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  * The <code>PortletRoleInfo</code> saves the role associated to a concrete portlet.
  */
 
 public class PortletRoleInfo {
 
     private transient PortletRole portletRole;
     private String portletClass = new String();
     private String role = new String();
    private String oid = new String();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
 
     public PortletRole getPortletRole() {
         return portletRole;
     }
 
     public void setPortletRole(PortletRole portletRole) {
         this.portletRole = portletRole;
     }
 
     public String getRole() {
         return portletRole.getName();
     }
 
     public void setRole(String role) {
         portletRole = PortletRole.toPortletRole(role);
         this.role = role;
     }
 
     /**
      * Returns the concrete PortletId.
      *
      * @return concrete PortletId
      */
     public String getPortletClass() {
         return portletClass;
     }
 
     /**
      * Sets the concrete PortletClass.
      *
      * @param concreteID id od the portletclass
      */
     public void setPortletClass(String concreteID) {
         this.portletClass = portletClass;
     }
 
 }
