 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.cassandra.locator;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.apache.cassandra.config.ConfigurationException;
 import org.apache.cassandra.gms.ApplicationState;
 import org.apache.cassandra.gms.EndpointState;
 import org.apache.cassandra.gms.Gossiper;
 import org.apache.cassandra.hadoop.trackers.TrackerInitializer;
 import org.apache.cassandra.service.StorageService;
 
 /**
  * A snitch that detects if Hadoop trackers are active and put this machine in a separate analytics DC
  */
 public class BriskSimpleSnitch extends AbstractEndpointSnitch
 {
     protected static Logger logger = LoggerFactory.getLogger(BriskSimpleSnitch.class);
     public static final String BRISK_DC = "BriskHadoopAnalyticsReplicaGroup";
     public static final String DEFAULT_DC = "CassandraLiveReplicaGroup";
     
     protected String myDC;
 
     public BriskSimpleSnitch() throws IOException, ConfigurationException
     {
         if(TrackerInitializer.isTrackerNode)
         {
             myDC = BRISK_DC;
             logger.info("Detected Hadoop trackers are enabled, setting my DC to " + myDC);
         }
         else
         {
             myDC = DEFAULT_DC;
             logger.info("Hadoop trackers not running, setting my DC to " + myDC);
         }
     }
     
     public String getDatacenter(InetAddress endpoint)
     {
        //needed for unit tests
         EndpointState es = Gossiper.instance.getEndpointStateForEndpoint(endpoint);
         
        assert es != null : "Endpoint "+es+" has no gossip state";
 
         String DC = es.getApplicationState(ApplicationState.DC).value;
       
         if(logger.isDebugEnabled())
             logger.debug("DC for endpoint "+endpoint+" is "+DC);
         
         return DC;
     }
 
     @Override
     public void gossiperStarting()
     {
         // Share DC info via gossip.
         Gossiper.instance.addLocalApplicationState(ApplicationState.DC, StorageService.instance.valueFactory.datacenter(myDC));
     }
 
     public List<InetAddress> getSortedListByProximity(InetAddress address, Collection<InetAddress> addresses)
     {
         return new ArrayList<InetAddress>(addresses);
     }
 
     public void sortByProximity(InetAddress arg0, List<InetAddress> arg1)
     {      
         
     }
 
     public String getRack(InetAddress arg0)
     {
         return "rack1";
     }
 }
