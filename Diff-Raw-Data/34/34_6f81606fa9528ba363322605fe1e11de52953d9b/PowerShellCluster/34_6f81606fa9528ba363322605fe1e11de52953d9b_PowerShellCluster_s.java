 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.redhat.rhevm.api.model.Cluster;
 import com.redhat.rhevm.api.model.CPU;
 import com.redhat.rhevm.api.model.DataCenter;
import com.redhat.rhevm.api.model.StorageType;
 import com.redhat.rhevm.api.powershell.model.PowerShellCluster;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellCluster {
 
     public static ArrayList<Cluster> parse(String output) {
         ArrayList<HashMap<String,String>> clustersProps = PowerShellUtils.parseProps(output);
         ArrayList<Cluster> ret = new ArrayList<Cluster>();
 
         for (HashMap<String,String> props : clustersProps) {
             Cluster cluster = new Cluster();
 
             cluster.setId(props.get("clusterid"));
             cluster.setName(props.get("name"));
 
             CPU cpu = new CPU();
             cpu.setId(props.get("cpuname"));
             cluster.setCpu(cpu);
 
             DataCenter dataCenter = new DataCenter();
             dataCenter.setId(props.get("datacenterid"));
             cluster.setDataCenter(dataCenter);
 
             ret.add(cluster);
         }
 
         return ret;
     }
 }
