 /**
  * Elastic Grid
  * Copyright (C) 2008-2009 Elastic Grid, LLC.
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
 package com.elasticgrid.platforms.ec2.discovery;
 
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.ec2.EC2Node;
 import net.jini.config.Configuration;
 import net.jini.config.ConfigurationException;
 import net.jini.core.discovery.LookupLocator;
 import net.jini.discovery.DiscoveryListener;
 import org.rioproject.resources.client.DiscoveryManagementPool;
 
 import java.io.IOException;
import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Adapts Apache River discovery semantics to EC2.
  */
 public class EC2LookupDiscoveryManager extends DiscoveryManagementPool.SharedDiscoveryManager {
     private static Logger logger = Logger.getLogger(EC2LookupDiscoveryManager.class.getName());
     private ScheduledExecutorService scheduler;
 
     public EC2LookupDiscoveryManager(DiscoveryManagementPool.DiscoveryControl discoveryControl,
                                      String[] groups,
                                      LookupLocator[] lookupLocators,
                                      DiscoveryListener discoveryListener) throws
                                                                           IOException {
         super(discoveryControl, groups, lookupLocators, discoveryListener);
         init(groups);
     }
 
     public EC2LookupDiscoveryManager(DiscoveryManagementPool.DiscoveryControl discoveryControl,
                                      String[] groups,
                                      LookupLocator[] lookupLocators,
                                      DiscoveryListener discoveryListener,
                                      Configuration configuration) throws
                                                                   IOException,
                                                                   ConfigurationException {
         super(discoveryControl,
               groups,
               lookupLocators,
               discoveryListener,
               configuration);
         init(groups);
         logger.info("Created EC2LookupDiscoveryManager");
     }
 
     private void init(String[] groups) {
         if(groups==null)
             throw new IllegalArgumentException("Dont know how to deal with "+
                                                "a null cluster name");
         if(groups.length==0)
             throw new IllegalArgumentException("Dont know how to deal with "+
                                                "a cluster configuration that " +
                                                "has no name");
         if(groups.length>1)
             throw new IllegalArgumentException("Dont know how to deal with "+
                                                "a cluster configuration that has " +
                                                "more than one name");
 
         String clusterName = groups[0];
         scheduler = Executors.newSingleThreadScheduledExecutor();
         scheduler.scheduleAtFixedRate(new ClusterLocatorTask(clusterName),
                                       0,
                                       60,
                                       TimeUnit.SECONDS);
 
     }
 
     @Override
     public void terminate() {
         if(scheduler!=null)
             scheduler.shutdownNow();
         super.terminate();
     }
 
 
     /**
      * Look for Monitor nodes
      */
     class ClusterLocatorTask implements Runnable {
          String clusterName;
          EC2SecurityGroupsClusterLocator clusterLocator;
 
          ClusterLocatorTask(String clusterName) {
              this.clusterName = clusterName;
              clusterLocator = new EC2SecurityGroupsClusterLocator();
          }
 
          public void run() {
              List<LookupLocator> locators = new ArrayList<LookupLocator>();
              try {
                 Set<EC2Node> nodes = clusterLocator.findNodes(clusterName);
                 for(EC2Node node : nodes) {
                      if(node.getProfile().isMonitor()) {
                          locators.add(
                              new LookupLocator("jini://"+node.getAddress().getHostAddress()));
                      }
                  }
                  if(locators.size()>0)
                      addLocators(locators.toArray(new LookupLocator[locators.size()]));
              } catch (ClusterException e) {
                  logger.log(Level.WARNING,
                             "Looking for nodes in the cluster failed, " +
                             "will retry in a minute",
                             e);
              } catch (MalformedURLException e) {
                  logger.log(Level.WARNING,
                             "Trying to create a LookupLocator from a " +
                             "discovered monitor in cluster ["+clusterName+"]",
                             e);
              }
          }
      }
 }
