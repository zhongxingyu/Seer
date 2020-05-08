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
 
 import com.redhat.rhevm.api.model.Storage;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.model.StorageDomainStatus;
 import com.redhat.rhevm.api.model.StorageDomainType;
 import com.redhat.rhevm.api.model.StorageType;
 import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellStorageDomain extends StorageDomain {
 
     private StorageDomainStatus sharedStatus;
 
     public StorageDomainStatus getSharedStatus() {
         return sharedStatus;
     }
     public void setSharedStatus(StorageDomainStatus sharedStatus) {
         this.sharedStatus = sharedStatus;
     }
 
     private static StorageDomainStatus parseStatus(HashMap<String,String> props, String key) {
         try {
            return props.get(key) != null
                   ? StorageDomainStatus.fromValue(props.get(key).toUpperCase())
                   : null;
         } catch (IllegalArgumentException iae) {
             // ignore - assume status is 'Unknown'
             return null;
         }
     }
 
     public static ArrayList<PowerShellStorageDomain> parse(String output) {
         ArrayList<HashMap<String,String>> storageDomainsProps = PowerShellUtils.parseProps(output);
         ArrayList<PowerShellStorageDomain> ret = new ArrayList<PowerShellStorageDomain>();
 
         for (HashMap<String,String> props : storageDomainsProps) {
             PowerShellStorageDomain storageDomain = new PowerShellStorageDomain();
 
             storageDomain.setId(props.get("storagedomainid"));
             storageDomain.setName(props.get("name"));
 
             String domainType = props.get("domaintype").toUpperCase();
             if (domainType.endsWith(" (MASTER)")) {
                 domainType = domainType.split(" ")[0];
                 storageDomain.setMaster(true);
             }
             storageDomain.setType(StorageDomainType.fromValue(domainType));
 
             storageDomain.setStatus(parseStatus(props, "status"));
             storageDomain.setSharedStatus(parseStatus(props, "sharedstatus"));
 
             Storage storage = new Storage();
 
             storage.setType(StorageType.fromValue(props.get("type").toUpperCase()));
 
             switch (storage.getType()) {
             case NFS:
                 String[] parts = props.get("nfspath").split(":");
                 storage.setAddress(parts[0]);
                 storage.setPath(parts[1]);
                 break;
             case ISCSI:
             case FCP:
             default:
                 assert false : storage.getType();
                 break;
             }
 
             storageDomain.setStorage(storage);
 
             ret.add(storageDomain);
         }
 
         return ret;
     }
 }
