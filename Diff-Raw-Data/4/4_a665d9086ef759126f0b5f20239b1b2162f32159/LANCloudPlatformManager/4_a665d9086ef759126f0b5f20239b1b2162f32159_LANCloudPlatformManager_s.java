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
 
 package com.elasticgrid.platforms.lan;
 
 import com.elasticgrid.cluster.spi.AbstractCloudPlatformManager;
 import com.elasticgrid.cluster.spi.CloudPlatformManager;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.NodeProfileInfo;
 import com.elasticgrid.model.lan.LANCluster;
 import com.elasticgrid.model.lan.LANNode;
 import com.elasticgrid.model.lan.impl.LANClusterImpl;
 import com.elasticgrid.platforms.lan.discovery.LANClusterLocator;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class LANCloudPlatformManager extends AbstractCloudPlatformManager<LANCluster> implements CloudPlatformManager<LANCluster> {
     private LANClusterLocator clusterLocator;
     private static final Logger logger = Logger.getLogger(LANCloudPlatformManager.class.getName());
 
     public String getName() {
         return "Private LAN";
     }
 
     public void startCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         throw new UnsupportedOperationException("There is no way to start a new cluster on a private LAN.");
     }
 
     public void stopCluster(String clusterName) throws ClusterException, RemoteException {
         throw new UnsupportedOperationException("There is no way to stop a cluster on a private LAN.");
     }
 
     public Collection<LANCluster> findClusters() throws ClusterException, RemoteException {
        logger.log(Level.INFO, "Searching for clusters running on the LAN...");
         Collection<String> clustersNames = clusterLocator.findClusters();
         List<LANCluster> clusters = new ArrayList<LANCluster>(clustersNames.size());
         for (String cluster : clustersNames) {
             clusters.add(cluster(cluster));
         }
         return clusters;
     }
 
     public LANCluster cluster(String name) throws RemoteException, ClusterException {
         LANCluster cluster = new LANClusterImpl();
         Set<LANNode> nodes = clusterLocator.findNodes(name);
         if (nodes == null)
             cluster.name(name);
         else
             cluster.name(name).addNodes(nodes);
         cluster.addApplications(clusterLocator.findApplications(name));
         return cluster;
     }
 
     public void resizeCluster(String clusterName, List<NodeProfileInfo> clusterTopology) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         throw new UnsupportedOperationException("There is no way to resize a cluster on a private LAN.");
     }
 
     public void setClusterLocator(LANClusterLocator clusterLocator) {
         this.clusterLocator = clusterLocator;
     }
 }
