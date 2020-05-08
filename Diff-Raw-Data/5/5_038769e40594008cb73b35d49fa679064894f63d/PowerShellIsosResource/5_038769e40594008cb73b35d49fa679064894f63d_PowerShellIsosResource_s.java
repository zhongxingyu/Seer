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
 
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.model.DataCenter;
 import com.redhat.rhevm.api.model.Iso;
 import com.redhat.rhevm.api.model.Isos;
 import com.redhat.rhevm.api.resource.IsoResource;
 import com.redhat.rhevm.api.resource.IsosResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellIso;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 
 public class PowerShellIsosResource extends UriProviderWrapper implements IsosResource {
 
     private String dataCenterId;
 
     public PowerShellIsosResource(String dataCenterId,
                                   PowerShellPoolMap shellPools,
                                   PowerShellParser parser,
                                   UriInfoProvider uriProvider) {
         super(null, shellPools, parser, uriProvider);
         this.dataCenterId = dataCenterId;
     }
 
     @Override
     public Isos list() {
         StringBuilder buf = new StringBuilder();
         buf.append("get-isoimages");
         buf.append(" -datacenterid " + PowerShellUtils.escape(dataCenterId));
         Isos ret = new Isos();
         for (Iso iso : PowerShellIso.parse(parser, PowerShellCmd.runCommand(getPool(), buf.toString()))) {
             ret.getIsos().add(addLinks(iso, dataCenterId));
         }
         return ret;
     }
 
     @Override
     public IsoResource getIsoSubResource(String id) {
         return new PowerShellIsoResource(id, dataCenterId, this);
     }
 
     public Iso addLinks(Iso iso, String dataCenterId) {
         iso.setDataCenter(new DataCenter());
         iso.getDataCenter().setId(dataCenterId);
 
         return LinkHelper.addLinks(getUriInfo(), iso);
     }
 
 }
