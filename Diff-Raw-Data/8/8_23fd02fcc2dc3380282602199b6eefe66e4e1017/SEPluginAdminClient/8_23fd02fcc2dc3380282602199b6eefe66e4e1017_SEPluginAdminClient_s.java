 /*
  * SCI-Flex: Flexible Integration of SOA and CEP
  * Copyright (C) 2008, 2009  http://sci-flex.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 
 package org.sciflex.plugins.synapse.esper.client;
 
 import java.rmi.RemoteException;
 import java.util.Comparator;
 import java.util.Locale;
 import java.util.ResourceBundle;
 import java.util.TreeSet;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.client.Options;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.context.ConfigurationContext;
 import org.apache.axis2.transport.http.HTTPConstants;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sciflex.plugins.synapse.esper.core.util.EPLQueryObject;
 import org.sciflex.plugins.synapse.esper.core.util.AdminStatisticsObject;
 
 /**
  * Admin Web Client for SCI-Flex Synapse Esper Plugin.
  */
 public class SEPluginAdminClient extends AdminClientConstants {
 
     /**
      * Log associated with the Synapse Esper Plugin Admin Client.
      */
     private static final Log log = LogFactory.getLog(SEPluginAdminClient.class);
 
     /**
      * Internationalization Resource Bundle
      */
     private ResourceBundle bundle = null;
 
     /**
      * Client Side Stub instance.
      */
     private SEPluginAdminStub stub = null;
 
     /**
      * Unique Identifier of currently handled mediator.
      */
     private String mediatorUID = null;
 
     /**
      * Stores a list of mediator references.
      */
     private String[] listOfMediators = null;
 
     /**
      * Stores a list of query report objects.
      */
     private EPLQueryObject[] queryReports = null;
 
     /**
      * Constructor accepting cookie, backend server's URL, Configuration
      * and Environment variables. This constructor also accepts a UID of
      * of a mediator to use in place of the default.
      *
      * @param cookie           HTTP Cookie String
      * @param backendServerURL URL of backend server
      * @param configCtx        the configuration context of the server
      * @param locale           the locale for which a resource bundle is desired
      * @param mediatorUID      UID of the Mediator
      * @throws AxisFault       an Axis Fault is thrown on an error
      */
     public SEPluginAdminClient(String cookie, String backendServerURL,
         ConfigurationContext configCtx, Locale locale, String mediatorUID) throws AxisFault {
         String serviceURL = backendServerURL + SERVICE_NAME;
         bundle = ResourceBundle.getBundle(BUNDLE, locale);
 
         stub = new SEPluginAdminStub(configCtx, serviceURL);
         ServiceClient client = stub._getServiceClient();
         Options option = client.getOptions();
         option.setManageSession(true);
         option.setProperty(HTTPConstants.COOKIE_STRING, cookie);
 
         // We need not deal with the RemoteException's caught below.
         // The exceptions will already be logged.
         if (mediatorUID != null) {
             try {
                 String[] mediatorList = getListOfMediators();
                 for (int i = 0; i < mediatorList.length; i++) {
                     if (mediatorUID.equals(mediatorList[i])) {
                         this.mediatorUID = mediatorUID;
                         break;
                     }
                 }
             } catch (RemoteException e) { }
         }
         // If mediatorUID was not set
         if (this.mediatorUID == null) {
             try {
                 this.mediatorUID = getListOfMediators()[0];
             } catch (RemoteException e) { }
         }
     }
 
     /**
      * Retrieves the UID of the current mediator.
      *
      * @return the current mediator's UID.
      */
     public String getCurrentMediatorUID() {
         return mediatorUID;
     }
 
     /**
      * Gets CEP Instance URI.
      *
      * @return CEP Instance URI.
      * @throws RemoteException
      */
     public String getCEPInstanceURI() throws RemoteException {
         try {
             return stub.getCEPInstanceURI(stub.getCEPInstanceEditorUID(mediatorUID));
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.cep.instance.uri"), e);
         }
         return null;
     }
 
     /**
      * Sets CEP Instance URI.
      *
      * @param instanceURI CEP Instance URI.
      * @return            true if operation was successful, false otherwise.
      * @throws RemoteException
      */
     public boolean setCEPInstanceURI(String instanceURI) 
         throws RemoteException {
         try {
             return stub.setCEPInstanceURI(stub.getCEPInstanceEditorUID(mediatorUID), instanceURI);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.set.cep.instance.uri"), e);
         }
         return false;
     }
 
     /**
      * Gets set of mediator UIDs.
      *
      * @return set of mediator UIDs.
      * @throws RemoteException
      */
     public String[] getListOfMediators() throws RemoteException {
         try {
             if (listOfMediators == null) {
                 listOfMediators = stub.getMediatorUIDs();
             }
             return listOfMediators;
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.list.of.mediators"), e);
         }
         return null;
     }
 
     /**
      * populates list of mediators
      */
     public void populateMediatorList()
         throws RemoteException {
         String[] newListOfMediators = getListOfMediators();
         listOfMediators = newListOfMediators;
     }
 
     /**
      * Gets number of mediator pages.
      *
      * @param pageSize   size of page
      * @return           number of Mediator pages.
      */
     public int getMediatorPageCount(int pageSize)
         throws RemoteException {
         String[] objects = getListOfMediators();
         if (objects == null || objects.length == 0) {
             return 0;
         }
         return ((int)(objects.length / pageSize)) +
             ((objects.length % pageSize) != 0 ? 1 : 0);
     }
 
     /**
      * Gets a page of mediators.
      *
      * @param pageSize   size of page
      * @param pageOffset starting offset of page.
      * @return           array of mediators
      * @throws RemoteException
      */
     public String[] getMediatorPage(int pageSize, int pageOffset)
         throws RemoteException {
         String[] objects = getListOfMediators();
         if (objects == null || (objects.length - (pageSize * pageOffset)) < 1) {
             return null;
         }
         try {
             int responseLength = objects.length - (pageSize * pageOffset);
             if (responseLength > DEFAULT_MEDIATOR_PAGE_SIZE) {
                 responseLength = DEFAULT_MEDIATOR_PAGE_SIZE;
             }
             int responseOffset = pageSize * pageOffset;
             String[] response = new String[responseLength];
             if (objects.length > responseOffset) {
                 for (int i = responseOffset; i < responseOffset + responseLength; i++) {
                     response[i - responseOffset] = objects[i];
                 }
                 return response;
             }
         } catch (java.lang.Exception e) {
             handleException(bundle.getString("cannot.fetch.mediator.page"), e);
         }
         return null;
     }
 
 
     /**
      * Gets Registry Key.
      *
      * @return Registry Key.
      * @throws RemoteException
      */
     public String getRegistryKey() throws RemoteException {
         try {
             return stub.getRegistryKey(stub.getRegistryKeyEditorUID(mediatorUID));
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.registry.key"), e);
         }
         return null;
     }
 
     /**
      * Sets Registry Key.
      *
      * @param registryKey Registry Key.
      * @return            true if operation was successful, false otherwise.
      * @throws RemoteException
      */
     public boolean setRegistryKey(String registryKey) 
         throws RemoteException {
         try {
             return stub.setRegistryKey(stub.getRegistryKeyEditorUID(mediatorUID), registryKey);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.set.registry.key"), e);
         }
         return false;
     }
 
     /**
      * Gets Static EPL.
      *
      * @return Static EPL.
      * @throws RemoteException
      */
     public String getStaticEPL() throws RemoteException {
         try {
             return stub.getStaticEPL(stub.getStaticEPLEditorUID(mediatorUID));
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.static.epl"), e);
         }
         return null;
     }
 
     /**
      * Sets Static EPL.
      *
      * @param staticEPL Static EPL.
      * @return          true if operation was successful, false otherwise.
      * @throws RemoteException
      */
     public boolean setStaticEPL(String staticEPL) 
         throws RemoteException {
         try {
             return stub.setStaticEPL(stub.getStaticEPLEditorUID(mediatorUID), staticEPL);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.set.static.epl"), e);
         }
         return false;
     }
 
     /**
      * Gets Dynamic EPL.
      *
      * @return Dynamic EPL.
      * @throws RemoteException
      */
     public String getDynamicEPL() throws RemoteException {
         try {
             return stub.getDynamicEPL(stub.getDynamicEPLEditorUID(mediatorUID));
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.dynamic.epl"), e);
         }
         return null;
     }
 
     /**
      * Sets Dynamic EPL.
      *
      * @param dynamicEPL  Dynamic EPL.
      * @return            true if operation was successful, false otherwise.
      * @throws RemoteException
      */
     public boolean setDynamicEPL(String dynamicEPL) 
         throws RemoteException {
         try {
             return stub.setDynamicEPL(stub.getDynamicEPLEditorUID(mediatorUID), dynamicEPL);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.set.dynamic.epl"), e);
         }
         return false;
     }
 
     /**
      * Stops Mediator
      *
      * @throws RemoteException
      */
     public void stopMediator() 
         throws RemoteException {
         try {
             stub.stopMediator(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.stop.mediator"), e);
         }
     }
 
     /**
      * Starts Mediator
      *
      * @throws RemoteException
      */
     public void startMediator() 
         throws RemoteException {
         try {
             stub.startMediator(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.start.mediator"), e);
         }
     }
 
     /**
      * Refreshes Mediator
      *
      * @throws RemoteException
      */
     public void refreshMediator() 
         throws RemoteException {
         try {
             stub.refreshMediator(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.refresh.mediator"), e);
         }
     }
 
     /**
      * Gets how long the Mediator has been active.
      *
      * @return active time
      * @throws RemoteException
      */
     public long getMediatorActiveTime() 
         throws RemoteException {
         try {
             return stub.getMediatorActiveTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.mediator.active.time"), e);
         }
         return 0L;
     }
 
     /**
      * Indicates whether the mediator is active.
      *
      * @return true if active, false if not
      * @throws RemoteException
      */
     public boolean getIsMediatorActive() 
         throws RemoteException {
         try {
             return stub.getIsMediatorActive(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.determine.whether.mediator.is.active"), e);
         }
         return false;
     }
 
     /**
      * Resets Mediator Active Time.
      *
      * @throws RemoteException
      */
     public void resetMediatorActiveTime() 
         throws RemoteException {
         try {
             stub.resetMediatorActivityTimer(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.reset.mediator.active.time"), e);
         }
     }
 
     /**
      * Resets Mediator Statistics.
      *
      * @throws RemoteException
      */
     public void resetMediatorStatistics() 
         throws RemoteException {
         try {
             stub.resetMediatorStatisticsMonitor(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.reset.statistics"), e);
         }
     }
 
     /**
      * Gets Maximum Response Time.
      *
      * @return Maximum Response Time
      * @throws RemoteException
      */
     public long getMaximumResponseTime() 
         throws RemoteException {
         try {
             return stub.getMaximumResponseTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.maximum.response.time"), e);
         }
         return 0L;
     }
 
     /**
      * Gets Maximum Request Time.
      *
      * @return Maximum Request Time
      * @throws RemoteException
      */
     public long getMaximumRequestTime() 
         throws RemoteException {
         try {
             return stub.getMaximumRequestTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.maximum.request.time"), e);
         }
         return 0L;
     }
 
     /**
      * Gets Minimum Response Time.
      *
      * @return Minimum Response Time
      * @throws RemoteException
      */
     public long getMinimumResponseTime() 
         throws RemoteException {
         try {
             return stub.getMinimumResponseTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.minimum.response.time"), e);
         }
         return 0L;
     }
 
     /**
      * Gets Minimum Request Time.
      *
      * @return Minimum Request Time
      * @throws RemoteException
      */
     public long getMinimumRequestTime() 
         throws RemoteException {
         try {
             return stub.getMinimumRequestTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.minimum.request.time"), e);
         }
         return 0L;
     }
 
     /**
      * Gets Average Response Time.
      *
      * @return Average Response Time
      * @throws RemoteException
      */
     public double getAverageResponseTime() 
         throws RemoteException {
         try {
             return stub.getAverageResponseTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.average.response.time"), e);
         }
         return 0.0;
     }
 
     /**
      * Gets Average Request Time.
      *
      * @return Average Request Time
      * @throws RemoteException
      */
     public double getAverageRequestTime() 
         throws RemoteException {
         try {
             return stub.getAverageRequestTime(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.average.request.time"), e);
         }
         return 0.0;
     }
 
     /**
      * Gets load on mediator as a percentage of total load.
      *
      * @return load on mediator
      * @throws RemoteException
      */
     public double getPercentageLoadOnMediator() 
         throws RemoteException {
         try {
             return stub.getLoadOnMediator(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.percentage.load.on.mediator"), e);
         }
         return 0.0;
     }
 
     /**
      * Resets Query Reports.
      *
      * @throws RemoteException
      */
     public void resetQueryReports() 
         throws RemoteException {
         try {
             stub.resetQueryActivityMonitor(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.reset.query.reports"), e);
         }
     }
 
     /**
      * Gets Query Reports.
      *
      * @return array of query objects
      * @throws RemoteException
      */
     public EPLQueryObject[] getQueryReports() 
         throws RemoteException {
         try {
             if (queryReports == null) {
                 queryReports = stub.getQueryActivities(mediatorUID);
             }
             return queryReports;
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.query.reports"), e);
         }
         return null;
     }
 
     /**
      * Clears cached query report objects
      */
     public void clearQueryReportCache() {
         queryReports = null;
     }
 
     /**
      * Gets number of query object pages.
      *
      * @param pageSize   size of page
      * @return           number of query object pages.
      */
     public int getQueryObjectPageCount(int pageSize)
         throws RemoteException {
         EPLQueryObject[] objects = getQueryReports();
         if (objects == null || objects.length == 0) {
             return 0;
         }
         return ((int)(objects.length / pageSize)) +
             ((objects.length % pageSize) != 0 ? 1 : 0);
     }
 
     /**
      * Gets a page of query objects.
      *
      * @param pageSize   size of page
      * @param pageOffset starting offset of page.
      * @return           array of query objects
      * @throws RemoteException
      */
     public EPLQueryObject[] getQueryObjectPage(int pageSize, int pageOffset)
         throws RemoteException {
         EPLQueryObject[] objects = getQueryReports();
         if (objects == null || (objects.length - (pageSize * pageOffset)) < 1) {
             return null;
         }
         try {
             TreeSet<EPLQueryObject> sortedObjectSet =
                 new TreeSet<EPLQueryObject>(new EPLQueryObjectGreaterThanComparator());
             for (int i = 0; i < objects.length; i++) {
                 if (objects[i] != null) {
                     sortedObjectSet.add(objects[i]);
                 }
             }
             int responseLength = objects.length - (pageSize * pageOffset);
             if (responseLength > DEFAULT_QUERY_PAGE_SIZE) {
                 responseLength = DEFAULT_QUERY_PAGE_SIZE;
             }
             int responseOffset = pageSize * pageOffset;
             EPLQueryObject[] response = new EPLQueryObject[responseLength];
             EPLQueryObject[] sortedSet = sortedObjectSet.toArray(new EPLQueryObject[0]);
             if (sortedSet.length > responseOffset) {
                 for (int i = responseOffset; i < responseOffset + responseLength; i++) {
                     response[i - responseOffset] = sortedSet[i];
                 }
                 return response;
             }
         } catch (java.lang.Exception e) {
             handleException(bundle.getString("cannot.sort.query.reports"), e);
         }
         return null;
     }
 
     /**
      * Implementation of {@link Comparator} that helps sort {@link EPLQueryObject}
      * objects in descending order.
      *
      * The use of this class remains internal as the behavior is specific and if
      * not might lead into exceptions.
      */
     private final class EPLQueryObjectGreaterThanComparator implements Comparator {
 
         /**
          * Compares to {@link EPLQueryObject} objects and says which one
          * is greater than the other.
          *
          * @param arg0 first object
          * @param arg1 second object
          * @return     -1 if first object is greater than the second
          *             which is the inverse of the less than behavior.
          *             Returns 1, if the condition is not satisfied.
          */
         public int compare(Object arg0, Object arg1) {
             EPLQueryObject x = (EPLQueryObject) arg0;
             EPLQueryObject y = (EPLQueryObject) arg1;
             if (x.getCount() > y.getCount()) {
                 return -1;
             }
             // We never return 0, as we don't want to say that two objects
             // are equal.
             return 1;
         }
 
         /**
          * Determines whether the given object is similar in type to
          * this object.
          *
          * @param obj given object
          * @return    true if they are similar, and false if not.
          */
         public boolean equals(Object obj) {
             // being null is a special case, and we not bothered
             // as we never will pass in null objects here in reality.
             if (obj != null && obj instanceof EPLQueryObjectGreaterThanComparator) {
                 return true;
             }
             return false;
         }
     }
 
     /**
      * Resets all Mediator Timers and Counters.
      *
     * @param mediatorUID mediator's UID
      * @throws RemoteException
      */
     public void resetAll()
         throws RemoteException {
         try {
             stub.resetAll(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.reset.mediator.admin.objects"), e);
         }
     }
 
     /**
      * Gets all Mediator statistics.
      *
      * @return object containing Mediator statistics
      * @throws RemoteException
      */
     public AdminStatisticsObject getStatistics() 
         throws RemoteException {
         try {
             return stub.getAllStatistics(mediatorUID);
         } catch (SEPluginAdminServiceException e) {
             handleException(bundle.getString("cannot.get.mediator.statistics"), e);
         }
         return null;
     }
 
     /**
      * Method to gracefully handle exceptions
      *
      * @param msg exception message to be displayed
      * @param e   exception that occured
      * @throws RemoteException after the exception is handled a new remote exception
      *                         is created and thrown with the parent exception and the
      *                         message passed.
      */
     private void handleException(String msg, java.lang.Exception e) throws RemoteException {
         log.error(msg, e);
         throw new RemoteException(msg, e);
     }
 }
 
