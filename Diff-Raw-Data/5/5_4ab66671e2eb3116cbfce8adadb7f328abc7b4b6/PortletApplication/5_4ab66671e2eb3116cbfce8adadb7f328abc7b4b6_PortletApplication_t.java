 /*
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 public class PortletApplication {
 
     private String Uid = new String();                 // Uid of the Portletapplication
     private String Name = new String();                // Name of the
     private PortletInfo portletInfo = new PortletInfo();            // PortletInfo
 
     /**
      * gets the Uid of a PortletApplication
      *
      * @returns Uid of the PortletApplication
      */
     public String getUid() {
         return Uid;
     }
 
     /**
      * sets the Uid of a PortletApplication
      *
      * @param uid uid of the PortletApplication
      */
     public void setUid(String uid) {
         this.Uid = uid;
     }
 
     /**
      * gets the name of a PortletApplication
      *
      * @returns name of the PortletApplication
      */
     public String getName() {
         return Name;
     }
 
     /**
      * sets the name of a PortletApplication
      *
      * @param name name of a PortletApplication
      */
     public void setName(String name) {
         this.Name = name;
     }
 
     /**
      * gets the portletInfo of a PortletApplication
      *
      * @see portlet
      * @returns portletInfo of a PortletApplication
      */
     public PortletInfo getPortletInfo() {
         return portletInfo;
     }
 
 
     /**
      * sets the portlet of a PortletApplication
      *
      * @see portlet
     * @param portletInfo the portlet of a PortletApplication
      */
    public void setPortletInfo(PortletInfo portletInfo) {
         this.portletInfo = portletInfo;
     }
 }
 
