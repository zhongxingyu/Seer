 package agh.msc.xbowbase.link;
 
 import agh.msc.xbowbase.exception.LinkException;
 import agh.msc.xbowbase.lib.NicHelper;
 import agh.msc.xbowbase.publisher.Publisher;
 import agh.msc.xbowbase.publisher.exception.NotPublishedException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.management.Notification;
 import javax.management.NotificationListener;
 
 import org.apache.log4j.Logger;
 
 /**
  * Implementation of VNicManagerMBean, @see VNicManagerMBean
  *
  * @author robert boczek
  */
 public class VNicManager implements VNicManagerMBean, NotificationListener {
 
     /** Logger */
     private static final Logger logger = Logger.getLogger(Nic.class);
     private Publisher publisher;
     private NicHelper linkHelper;
 
     /**
      * Executes discover() in response to notification.
      *
      * @see  NotificationListener#handleNotification( javax.management.Notification, java.lang.Object )
      */
     @Override
     public void handleNotification(Notification notification, Object handback) {
 
         logger.debug("VNicManager received notification... running discovery method");
 
         try {
             discover();
         } catch (LinkException ex) {
             logger.error("Error while running discovery method", ex);
         }
     }
 
     /**
      * @see  VNicManagerMBean#create(agh.msc.xbowbase.link.VNicMBean)
      */
     @Override
     public void create(VNicMBean vNicMBean) throws LinkException {
 
         logger.debug("VNicManager creating new vnic with name: " + vNicMBean.getName() + ", temporary: " + vNicMBean.isTemporary() + ", under: " + vNicMBean.getParent());
 
         try {
             this.linkHelper.createVNic(vNicMBean.getName(), vNicMBean.isTemporary(), vNicMBean.getParent());
             registerNewVNicMBean(vNicMBean);
             discover();
         } catch (LinkException e) {
             logger.error("VNic " + vNicMBean + " couldn't be created", e);
             throw e;
         }
         
     }
 
     /**
      * @see  VNicManagerMBean#delete(java.lang.String, boolean)
      */
     @Override
     public void delete(String name, boolean temporary) throws LinkException {
 
         logger.debug("VNicManager removing vnic with name: " + name + ", temporary: " + temporary);
 
         try {
             VNicMBean vnicMBean = new VNic(name, temporary);
             this.linkHelper.deleteVNic(name, temporary);
             removeNoMoreExistingVNicMBeans(Arrays.asList(new VNicMBean[]{ vnicMBean }));
             discover();
 
         } catch (LinkException e) {
             logger.error("Link " + name + " couldn't be deleted", e);
             throw e;
         }
     }
 
     /**
      * @see  VNicManagerMBean#getVNicsNames()
      */
     @Override
     public List<String> getVNicsNames() throws LinkException {
 
         String[] linkNames = this.linkHelper.getLinkNames(true);
         
         if (linkNames == null) {
             return new LinkedList<String>();
         } else {
             return Arrays.asList(this.linkHelper.getLinkNames(true));
         }
     }
 
     /**
      * @see  VNicManagerMBean#discover()
      */
     @Override
     public void discover() throws LinkException {
         logger.info("VNicManager.discover()... searching for new vnic's and ones that don't exist any more");
 
        //@todo use jna library to get list of exisitng vnic's names
        Set<VNicMBean> currentMBeans = convertToSet(null);
 
         if(publisher != null){
             Set<Object> vnicSet = new HashSet<Object>(publisher.getPublished());
 
             //check for new Etherstubs
             for (VNicMBean vNicMBean : currentMBeans) {
                 if (vnicSet.contains(vNicMBean) == false) {
                 //create and register new VnicMBean
                 registerNewVNicMBean(vNicMBean);
             }
         }
 
         List<VNicMBean> vNicMBeansToRemove = new LinkedList<VNicMBean>();
         //remove etherstubs that don't exist anymore
         for (Object object : vnicSet) {
             if (object instanceof VNicMBean && currentMBeans.contains((VNicMBean)object) == false) {
                 //save this etherstub as one to be removed
                 vNicMBeansToRemove.add(((VNicMBean)object));
             }
         }
         removeNoMoreExistingVNicMBeans(vNicMBeansToRemove);
         }       
     }
 
     /**
      * Sets publisher instance
      * @param publisher Instance of publisher to be used for publishing MBeans
      */
     public void setPublisher(Publisher publisher) {
         this.publisher = publisher;
     }
 
     /**
      * Registers  new VNicMBean to MBeanServer
      * @param vNicMBean New VNicMBean to be registered to MBeanServer
      */
     private void registerNewVNicMBean(VNicMBean vNicMBean) {
 
         if (publisher != null) {
            logger.info("Registering new VNicMBean to MBeanServer: " + vNicMBean);
 
             publisher.publish(vNicMBean);
         }
     }
 
     /**
      * Removes VNicMBeans that don't exist in the system from the MBeanServer
      * @param vNicMBeansList List of VNicMBean to unregister
      */
     private void removeNoMoreExistingVNicMBeans(List<VNicMBean> vNicMBeansList) {
 
         for (VNicMBean vNicMBean : vNicMBeansList) {
             try {
                 if (publisher != null) {
                     logger.info("Unregistering VNicMBean from the MBeanServer: " + vNicMBean);
                     this.publisher.unpublish(vNicMBean);
                 }
             } catch (NotPublishedException ex) {
                 logger.error("VNicMBean object : " + vNicMBean + " has not been registered in the mbean server");
             }
         }
     }
 
     /**
      * Converts array of names to set of VNicMBean objets (we assume that
      * created VNic's are persitent not temporary and parent is null )
      * @param vNicNames Array of existing VNicMBean names
      * @return Set of EtherstubMBean objects
      */
     private Set<VNicMBean> convertToSet(String[] vNicNames) {
         Set<VNicMBean> set = new HashSet<VNicMBean>();
         if (vNicNames != null) {
             for (String vnicMBeanName : vNicNames) {
                 set.add(new VNic(vnicMBeanName, false));
             }
         }
         return set;
     }
 
     public void setLinkHelper(NicHelper linkHelper){
 
         this.linkHelper = linkHelper;
     }
 }
