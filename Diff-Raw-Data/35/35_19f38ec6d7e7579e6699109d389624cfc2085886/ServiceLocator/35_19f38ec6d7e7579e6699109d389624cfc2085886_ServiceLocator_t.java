 /**
 * Elastic Grid
 * Copyright (C) 2007-2008 Elastic Grid, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.examples.video.util;
 
 import com.elasticgrid.platforms.ec2.discovery.EC2SecurityGroupsClusterLocator;
import com.elasticgrid.platforms.ec2.config.EC2Configuration;
 import com.elasticgrid.model.ClusterMonitorNotFoundException;
 import com.elasticgrid.model.ec2.EC2Node;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import com.elasticgrid.cluster.discovery.ClusterLocator;
 import com.xerox.amazonws.ec2.EC2Exception;
 import com.xerox.amazonws.ec2.Jec2;
 import net.jini.config.ConfigurationException;
 import net.jini.core.discovery.LookupLocator;
 import net.jini.core.lookup.ServiceID;
 import net.jini.core.lookup.ServiceItem;
 import net.jini.core.lookup.ServiceTemplate;
 import net.jini.discovery.DiscoveryManagement;
 import net.jini.lookup.LookupCache;
 import net.jini.lookup.ServiceItemFilter;
 import org.rioproject.opstring.OpStringManagerProxy;
 import org.rioproject.resources.client.DiscoveryManagementPool;
 import org.rioproject.resources.client.LookupCachePool;
 import org.rioproject.resources.servicecore.Service;
 import org.rioproject.watch.WatchDataSource;
 import org.rioproject.watch.Watchable;
 import java.io.IOException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.security.Permission;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class ServiceLocator {
     private static LookupCache lookupCache;
     private static final Logger logger = Logger.getLogger(ServiceLocator.class.getName());
 
     static {
         System.setSecurityManager(new RMISecurityManager() {
             public void checkPermission(Permission perm) {
                 // do nothing -- hence approve everything
             }
         });
         try {
             ServiceTemplate template = new ServiceTemplate(null, null, null);
             DiscoveryManagement discoMgr = DiscoveryManagementPool.getInstance().getDiscoveryManager(
                     null,
                     new String[]{"rio", "javaone"},
                     lookupMonitors()
             );
             lookupCache = LookupCachePool.getInstance().getLookupCache(discoMgr, template);
             while (lookupCache.lookup(null) == null) {
                 System.out.printf("Waiting for service(s)...\n");
                 Thread.sleep(1000);
             }
             OpStringManagerProxy.setDiscoveryManagement(discoMgr);
         } catch (ConfigurationException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (InterruptedException e) {
             e.printStackTrace();
         } catch (EC2Exception e) {
             e.printStackTrace();
         }
     }
 
     private static LookupLocator[] lookupMonitors() throws IOException, EC2Exception {
         System.out.printf("Searching for Elastic Cluster monitor host...\n");
         Properties egProps = AWSUtils.loadEC2Configuration();
         ClusterLocator locator = new EC2SecurityGroupsClusterLocator();
        String awsAccessId = egProps.getProperty(EC2Configuration.AWS_ACCESS_ID);
        String awsSecretKey = egProps.getProperty(EC2Configuration.AWS_SECRET_KEY);
         ((EC2SecurityGroupsClusterLocator) locator).setEc2(new Jec2(awsAccessId, awsSecretKey));
         try {
             EC2Node node = (EC2Node) locator.findMonitor("test");
             return new LookupLocator[] { new LookupLocator("jini://" + node.getAddress().getHostName() )};
         } catch (ClusterMonitorNotFoundException e) {
             logger.info("Could not find monitor host. Using localhost instead hoping we find the service there!");
             return new LookupLocator[] { new LookupLocator("jini://localhost" )};
         }
     }
 
     public static ServiceItem getServiceByServiceID(final ServiceID serviceID) throws InterruptedException {
         return lookupCache.lookup(new ServiceItemFilter() {
             public boolean check(ServiceItem serviceItem) {
                 return serviceItem.serviceID.equals(serviceID);
             }
         });
     }
 
     public static ServiceItem[] getServicesByClass(final Class serviceClazz) throws InterruptedException {
         return lookupCache.lookup(new ServiceItemFilter() {
             public boolean check(ServiceItem serviceItem) {
                 boolean match = serviceClazz.isAssignableFrom(serviceItem.service.getClass());
                 if (match) {
                     logger.log(Level.INFO, "Found service {0} with ID {1}",
                             new Object[] { serviceClazz, serviceItem.serviceID });
                 }
                 return match;
             }
         }, Integer.MAX_VALUE);
     }
 
     public static List<WatchDataSource> getWatchDataSourcesByServiceID(final ServiceID serviceID) throws RemoteException, InterruptedException {
         Watchable w = (Service) getServiceByServiceID(serviceID).service;
         return Arrays.asList(w.fetch());
     }
 
     public static Map<ServiceID, List<WatchDataSource>> getWatchDataSources(Class serviceClazz) throws InterruptedException, RemoteException {
         ServiceItem[] items = getServicesByClass(serviceClazz);
         Map<ServiceID, List<WatchDataSource>> watches = new HashMap<ServiceID, List<WatchDataSource>>();
         for (ServiceItem item : items) {
             watches.put(item.serviceID, Arrays.asList(((Service) item.service).fetch()));
         }
         return watches;
     }
 
 }
