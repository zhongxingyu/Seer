 /**
  * Copyright (C) 2007-2008 Elastic Grid, LLC.
  * 
  * This file is part of Elastic Grid.
  * 
  * Elastic Grid is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or any later version.
  * 
  * Elastic Grid is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with Elastic Grid.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.elasticgrid.cluster;
 
 import org.springframework.stereotype.Service;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import com.elasticgrid.model.ClusterException;
 import com.elasticgrid.model.Cluster;
 import com.elasticgrid.model.ClusterNotFoundException;
 import com.elasticgrid.cluster.spi.CloudPlatformManager;
 import java.rmi.RemoteException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 import java.util.List;
 import java.util.LinkedList;
 
 /**
  * Cloud Federation Cluster Manager.
  * This {@link ClusterManager} enables Cloud Federations by exposing all resources from all
  * {@link ClusterManager}s in charge of specific Cloud Platforms.
  * Note: actually this only works by mixing EC2 and LAN platforms.
  * @author Jerome Bernard
  */
 @Service("clusterManager")
 public class CloudFederationClusterManager<C extends Cluster> extends AbstractClusterManager<C> implements ClusterManager<C> {
     private List<CloudPlatformManager<C>> clouds;
 
     public void startCluster(String clusterName, int numberOfMonitors, int numberOfAgents) throws ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         clouds.get(0).startCluster(clusterName, numberOfMonitors, numberOfAgents);
     }
 
     public void stopCluster(String clusterName) throws ClusterException, RemoteException {
         clouds.get(0).stopCluster(clusterName);
     }
 
     public List<C> findClusters() throws ClusterException, RemoteException {
         List<C> clusters = new LinkedList<C>();
         for (CloudPlatformManager<C> cloud : clouds)
             clusters.addAll(cloud.findClusters());
         return clusters;
     }
 
     public C cluster(String name) throws ClusterException, RemoteException {
        C cluster = null;
        int i = 0;
        while (cluster == null || cluster.getNodes().size() == 0)
            cluster = clouds.get(i++).cluster(name);
        return cluster;
     }
 
     public void resizeCluster(String clusterName, int newSize) throws ClusterNotFoundException, ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         clouds.get(0).resizeCluster(clusterName, newSize);
     }
 
     public void resizeCluster(String clusterName, int numberOfMonitors, int numberOfAgents) throws ClusterNotFoundException, ClusterException, ExecutionException, TimeoutException, InterruptedException, RemoteException {
         clouds.get(0).resizeCluster(clusterName, numberOfMonitors, numberOfAgents);
     }
 
     @Required
     public void setClouds(List<CloudPlatformManager<C>> clouds) {
         this.clouds = clouds;
     }
 }
