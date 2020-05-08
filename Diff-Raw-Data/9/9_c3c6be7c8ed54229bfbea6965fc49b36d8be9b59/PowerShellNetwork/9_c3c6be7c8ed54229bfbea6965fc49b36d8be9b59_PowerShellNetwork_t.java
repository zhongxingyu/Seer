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
 import java.util.List;
 
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.IP;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.model.VLAN;
 import com.redhat.rhevm.api.powershell.enums.PowerShellNetworkStatus;
 import com.redhat.rhevm.api.powershell.model.PowerShellNetwork;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 
 public class PowerShellNetwork {
 
     public static List<Network> parse(PowerShellParser parser, String output) {
         ArrayList<Network> ret = new ArrayList<Network>();
 
         for (PowerShellParser.Entity entity : parser.parse(output)) {
             Network network = new Network();
 
             network.setId(entity.get("networkid"));
             network.setName(entity.get("name"));
             network.setDescription(entity.get("description"));
             network.setStatus(entity.get("status", PowerShellNetworkStatus.class).map());
 
             DataCenter dataCenter = new DataCenter();
             dataCenter.setId(entity.get("datacenterid"));
             network.setDataCenter(dataCenter);
 
             if (entity.get("address") != null ||
                 entity.get("subnet") != null ||
                 entity.get("gateway") != null) {
                 IP ip = new IP();
                 ip.setAddress(entity.get("address"));
                 ip.setNetmask(entity.get("subnet"));
                 ip.setGateway(entity.get("gateway"));
                 network.setIp(ip);
             }
 
             if (entity.isSet("vlanid")) {
                 VLAN vlan = new VLAN();
                vlan.setId(entity.get("vlanid", Integer.class).toString());
                 network.setVlan(vlan);
             }
 
             if (entity.get("stp", Boolean.class)) {
                 network.setStp(true);
             }
 
             ret.add(network);
         }
 
         return ret;
     }
 }
