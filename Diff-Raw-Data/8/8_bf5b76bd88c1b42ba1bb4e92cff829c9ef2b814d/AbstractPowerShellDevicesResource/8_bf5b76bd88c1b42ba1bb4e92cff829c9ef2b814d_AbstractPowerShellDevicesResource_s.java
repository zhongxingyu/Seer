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
 package com.redhat.rhevm.api.powershell.resource;
 
 import com.redhat.rhevm.api.model.BaseDevice;
 import com.redhat.rhevm.api.model.BaseDevices;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.resource.ReadOnlyDevicesResource;
 

 public abstract class AbstractPowerShellDevicesResource<D extends BaseDevice, C extends BaseDevices>
     implements ReadOnlyDevicesResource<D, C> {
 
     protected String parentId;
     protected PowerShellPoolMap shellPools;
 
     public AbstractPowerShellDevicesResource(String parentId, PowerShellPoolMap shellPools) {
         this.parentId = parentId;
         this.shellPools = shellPools;
     }
 
     public PowerShellCmd getShell() {
         return shellPools.get().get();
     }
 
     public abstract D addLinks(D device);
 
     public abstract C getDevices();
 }
