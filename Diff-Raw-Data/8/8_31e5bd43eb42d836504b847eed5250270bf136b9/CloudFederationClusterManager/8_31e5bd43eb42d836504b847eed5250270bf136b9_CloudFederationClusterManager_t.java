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
 
 package com.elasticgrid.cluster;
 
 import com.elasticgrid.cluster.spi.CloudPlatformManager;
 import com.elasticgrid.config.GenericConfiguration;
 import com.elasticgrid.model.Cluster;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.ClusterNotFoundException;
 import com.elasticgrid.model.NodeProfileInfo;
 import com.elasticgrid.utils.amazon.AWSUtils;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Logger;
 
 /**
  * Cloud Federation Cluster Manager.
  * This {@link ClusterManager} enables Cloud Federations by exposing all resources from all
  * {@link ClusterManager}s in charge of specific Cloud Platforms.
  * Note: actually this only works by mixing EC2 and LAN platforms.
  * @author Jerome Bernard
  */
 public class CloudFederationClusterManager<C extends Cluster> implements ClusterManager<C> {
     private boolean ec2Mode = true;
     private List<CloudPlatformManager<C>> clouds;
     private static final Logger logger = Logger.getLogger(CloudFederationClusterManager.class.getName());
 
     public void startCluster(String clusterName, List<NodeProfileInfo> clusterTopology)
             throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         clouds.get(ec2Mode?1:0).startCluster(clusterName, clusterTopology);
     }
 
     public void stopCluster(String clusterName) throws ClusterException, RemoteException {
         clouds.get(ec2Mode?1:0).stopCluster(clusterName);
     }
 
     public Set<C> findClusters() throws ClusterException, RemoteException {
         Set<C> clusters = new HashSet<C>();
         for (CloudPlatformManager<C> cloud : clouds)
             clusters.addAll(cloud.findClusters());
         return clusters;
     }
 
     public C cluster(String name) throws ClusterException, RemoteException {
         /*
         C cluster = null;
         int i = 0;
         logger.log(Level.INFO, "Searching through {0} cloud platforms", clouds.size());
         while ((cluster == null || cluster.getNodes().size() == 0) && i < clouds.size()) {
             CloudPlatformManager<C> cloud = clouds.get(i++);
             logger.log(Level.INFO, "Searching for cluster {0} in cloud {1}", new Object[] { name, cloud.getName() });
             cluster = cloud.cluster(name);
         }
         return cluster;
         */
        if (clouds.size() == 1)
            return clouds.get(0).cluster(name);
        else
            return clouds.get(ec2Mode?1:0).cluster(name);
     }
 
     public void resizeCluster(String clusterName, List<NodeProfileInfo> clusterTopology)
             throws ClusterNotFoundException, ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         clouds.get(ec2Mode?1:0).resizeCluster(clusterName, clusterTopology);
     }
 
     public void setClouds(List<CloudPlatformManager<C>> clouds) {
        if (clouds == null)
            throw new IllegalArgumentException("The list of cloud platforms can't be null");
         this.clouds = clouds;
         try {
             Properties awsConfig = AWSUtils.loadEC2Configuration();
             String egMode = awsConfig.getProperty(GenericConfiguration.EG_MODE);
             if (egMode != null && egMode.equalsIgnoreCase("lan")) {
                 ec2Mode = false;
             }
         } catch (IOException e) {
         }
     }
 }
