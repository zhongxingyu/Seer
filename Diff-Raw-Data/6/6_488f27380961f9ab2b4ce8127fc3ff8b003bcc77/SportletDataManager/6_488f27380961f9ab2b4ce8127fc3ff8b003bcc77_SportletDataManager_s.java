 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:oliver@wehrens.de">Oliver Wehrens</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.impl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.portlet.GuestUser;
 import org.gridlab.gridsphere.portlet.PortletData;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletData;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portletcontainer.PortletDataManager;
 import org.gridlab.gridsphere.portletcontainer.PortletRegistry;
 
 /**
  * The <code>SportletDataManager</code> provides a a singleton implementation of the <code>PortletDataManager</code>
  * used for loading and storing <code>PortletData</code>.
  */
 public class SportletDataManager implements PortletDataManager {
 
     private static PersistenceManagerRdbms pm = PersistenceManagerFactory.createGridSphereRdbms();
     private static PortletDataManager instance = new SportletDataManager();
 
     /**
      * Default instantiation is disallowed
      */
     private SportletDataManager() {
     }
 
     /**
      * Returns an instance of a <code>PortletDataManager</code>
      *
      * @return an instance of a <code>PortletDataManager</code>
      */
     public static PortletDataManager getInstance() {
         return instance;
     }
 
     /**
      * Returns the users portlet data for the specified portlet
      *
      * @param user the <code>User</code>
      * @param portletID the concrete portlet id
      * @return the PortletData for this portlet or null if none exists.
      */
     public PortletData getPortletData(User user, String portletID) throws PersistenceManagerException {
 
         if (user instanceof GuestUser) return null;
 
         String command =
                 "select u from " + SportletData.class.getName() + " u where u.UserID='" + user.getID() + "' and u.PortletID='" + portletID + "'";
 
         // get sportlet data if it exists
         SportletData data = (SportletData) pm.restore(command);

         // or create one
         if (data == null) {
            data = new SportletData(pm);
             data.setPortletID(portletID);
             data.setUserID(user.getID());
             pm.create(data);
         }
         return data;
     }
 
 }
