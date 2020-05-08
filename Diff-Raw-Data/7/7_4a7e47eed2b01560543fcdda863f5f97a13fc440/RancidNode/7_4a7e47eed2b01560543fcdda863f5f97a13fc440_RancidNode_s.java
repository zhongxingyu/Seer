 /*
  * This file is part of the OpenNMS(R) Application.
  *
  * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
  * OpenNMS(R) is a derivative work, containing both original code, included code and modified
  * code that was published under the GNU General Public License. Copyrights for modified
  * and included code are below.
  *
  * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
  *
  * Modifications:
  * 
  * Created: March 17, 2009
  *
  * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * For more information contact:
  *      OpenNMS Licensing       <license@opennms.org>
  *      http://www.opennms.org/
  *      http://www.opennms.com/
  */
 package org.opennms.rancid;
 
 import java.util.HashMap;
 
 /**
  * An Object Representation of a node in RANCID
  * 
  * @author <a href="mailto:guglielmoincisa@gmail.com">Guglielmo Incisa</a>
  * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
  */
 public class RancidNode {
 
     @Override
     public int hashCode() {
       int hash = 7;
       hash = 31 * hash + (null == this.deviceName ? 0 : this.deviceName.hashCode());
       hash = 31 * hash + (null == this.group ? 0 : this.group.hashCode());
 
       return hash;
     }
     
     /**
      * Override equals so that we equal based on deviceName and group
      */
     @Override
     public boolean equals(Object obj) {
         boolean equal = false;
 
         if (this == obj) {
             return true;
         } else if (obj == null || obj.getClass() != this.getClass()) {
             throw new IllegalArgumentException("The comparison object is either null or of the wrong class");
         } 
         
         //probably don't need this case insensitivity check now that we force the deviceName to
         //lower case
         RancidNode that = (RancidNode)obj;
         
         if (this.deviceName.equalsIgnoreCase(that.deviceName) 
                 && this.group.equalsIgnoreCase(that.group)) {
             equal = true;
         }
         
         return equal;
     };
     
     /**
      * Override to string for better log messages.
      */
     @Override
     public String toString() {
         StringBuilder bldr = new StringBuilder();
         bldr.append("RANICD Node: ");
         bldr.append(deviceName);
         bldr.append(" of Group: ");
         bldr.append(this.group);
         return bldr.toString();
     }
 
     public static String DEVICE_TYPE_ALTEON = "alteon";
     //An Alteon WebOS switches.
     public static String DEVICE_TYPE_BAYNET="baynet";
     //A Bay Networks router.
     public static String DEVICE_TYPE_CISCO_CATOS="cat5";
     //A Cisco catalyst series 5000 and 4000 switches (i.e.: running the catalyst OS, not IOS).
     public static String DEVICE_TYPE_CISCO_IOS="cisco";
     //A Cisco router, PIX, or switch such as the 3500XL or 6000 running IOS (or IOS-like) OS.
     public static String DEVICE_TYPE_CISCO_CSS="css";
     //A Cisco content services switch.
     public static String DEVICE_TYPE_ENTERASYS="enterasys";
     //An enterasys NAS. This is currently an alias for the riverstone device type.
     public static String DEVICE_TYPE_JUNOS="erx";
     //A Juniper E-series edge router.
     public static String DEVICE_TYPE_EXTREME="extreme";
     //An Extreme switch.
     public static String DEVICE_TYPE_EZT3="ezt3";
     //An ADC-Kentrox EZ-T3 mux.
     public static String DEVICE_TYPE_FORCE10="force10";
     //A Force10 router.
     public static String DEVICE_TYPE_FOUNDRY="foundry";
     //A Foundry router, switch, or router-switch. This includes HP Procurve switches that are OEMs of Foundry products, such as the HP9304M.
     public static String DEVICE_TYPE_HITACHI="hitachi";
     //A Hitachi routers.
     public static String DEVICE_TYPE_HPPROCURVE="hp";
     //A HP Procurve switch such as the 2524 or 4108 procurve switches. Also see the foundry type.
     public static String DEVICE_TYPE_JUNIPER="juniper";
     //A Juniper router.
     public static String DEVICE_TYPE_MRTD="mrtd";
     //A host running the (merit) MRTd daemon.
     public static String DEVICE_TYPE_NETSCALAR="netscalar";
     //A Netscalar load balancer.
     public static String DEVICE_TYPE_NETSCREEN="netscreen";
     //A Netscreen firewall.
     public static String DEVICE_TYPE_REDBACK="redback";
     //A Redback router, NAS, etc.
     public static String DEVICE_TYPE_RIVERSTONE="riverstone";
     //A Riverstone NAS or Cabletron (starting with version ~9.0.3) router.
     public static String DEVICE_TYPE_TNT="tnt";
     //A lucent TNT.
     public static String DEVICE_TYPE_ZEBRA="zebra";
     //Zebra routing software.   
     
     // For provisioning only
     private String deviceName;
     private String deviceType;
     private boolean stateUp = true;
     private String comment;
     
     //http://www.rionero.com/rws-current/rws/rancid/groups/laboratorio/7206ped.wind.lab/configs
     private int TotalRevisions;
     private String HeadRevision;
     private String rootConfigurationUrl;
     
     private String group;
     
     private RancidNodeAuthentication m_auth;
     
     // The list of downloaded versions
     private HashMap<String, InventoryNode> nodeVersions;
 
     
     public RancidNode() {
         this(null, null);
     }
     
     public RancidNode(String group, String deviceName) {
         this.group = group;
        this.deviceName = deviceName.toLowerCase();
         this.nodeVersions = new HashMap<String, InventoryNode>();
     }
 
     public String getComment() {
         return comment;
     }
     public void setComment(String comment) {
         this.comment = comment;
     }
     public String getDeviceName() {
         return deviceName;
     }
     
     //force this to lower case
     public void setDeviceName(String deviceName) {
        this.deviceName = deviceName.toLowerCase();
     }
     public String getDeviceType() {
         return deviceType;
     }
     public void setDeviceType(String deviceType) {
         this.deviceType = deviceType;
     }
     public boolean isStateUp() {
         return stateUp;
     }
     public void setStateUp(boolean stateUp) {
         this.stateUp = stateUp;
     }
 
     public String getState() {
         if (isStateUp()) return "up";
         return "down";
     }
     public HashMap<String, InventoryNode> getNodeVersions() {
         return nodeVersions;
     }
     public void setNodeVersions(HashMap<String, InventoryNode> nodeVersions) {
         this.nodeVersions = nodeVersions;
     }
     public String getGroup() {
         return group;
     }
     public void setGroup(String group) {
         this.group = group;
     }
     public void setRootConfigurationUrl(String rootConfigurationUrl) {
         this.rootConfigurationUrl = rootConfigurationUrl;
     }
     public String getRootConfigurationUrl() {
         return rootConfigurationUrl;
     }
     public int getTotalRevisions(){
         return TotalRevisions;
     }
     public void setTotalRevisions(String TotalRevisions){
         this.TotalRevisions = Integer.parseInt(TotalRevisions);
     }
     public String getHeadRevision(){
         return HeadRevision;
     }
     public void setHeadRevision(String HeadRevision){
         this.HeadRevision = HeadRevision;
     }
     public void setAuth(RancidNodeAuthentication auth) {
         m_auth = auth;
     }
 
     public RancidNodeAuthentication getAuth() {
         return m_auth;
     }
 
     public void addInventoryNode(String version, InventoryNode invNode) {
         this.nodeVersions.put(version,invNode);
     }
         
 }
