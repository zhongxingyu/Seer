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
 
 import java.util.List;
 import java.util.concurrent.Executor;
 
 import com.redhat.rhevm.api.model.IP;
 import com.redhat.rhevm.api.model.Network;
 import com.redhat.rhevm.api.resource.NetworkResource;
 import com.redhat.rhevm.api.common.resource.UriInfoProvider;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.powershell.model.PowerShellNetwork;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 public class PowerShellNetworkResource extends AbstractPowerShellActionableResource<Network> implements NetworkResource {
 
     public PowerShellNetworkResource(String id,
                                      Executor executor,
                                      UriInfoProvider uriProvider,
                                      PowerShellPoolMap shellPools,
                                      PowerShellParser parser) {
         super(id, executor, uriProvider, shellPools, parser);
     }
 
     public static List<Network> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
         return PowerShellNetwork.parse(parser, PowerShellCmd.runCommand(pool, command));
     }
 
     public static Network runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
         List<Network> networks = runAndParse(pool, parser, command);
 
         return !networks.isEmpty() ? networks.get(0) : null;
     }
 
     public List<Network> runAndParse(String command) {
         return runAndParse(getPool(), getParser(), command);
     }
 
     public Network runAndParseSingle(String command) {
         return runAndParseSingle(getPool(), getParser(), command);
     }
 
     @Override
     public Network get() {
         StringBuilder buf = new StringBuilder();
 
         buf.append("$n = get-networks;");
         buf.append("foreach ($i in $n) {");
         buf.append("  if ($i.networkid -eq " + PowerShellUtils.escape(getId()) + ") {");
         buf.append("    $i");
         buf.append("  }");
         buf.append("}");
 
         return LinkHelper.addLinks(getUriInfo(), runAndParseSingle(buf.toString()));
     }
 
     @Override
     public Network update(Network network) {
         validateUpdate(network);
 
         StringBuilder buf = new StringBuilder();
 
         buf.append("foreach ($i in $n) { ");
         buf.append("if ($i.networkid -eq " + PowerShellUtils.escape(getId()) + ") { ");
 
         if (network.getName() != null) {
             buf.append("$i.name = " + PowerShellUtils.escape(network.getName()) + "; ");
         }
         if (network.getDescription() != null) {
             buf.append("$i.description = " + PowerShellUtils.escape(network.getDescription()) + "; ");
         }
 
         if (network.getIp() != null) {
             IP ip = network.getIp();
 
             if (ip.getAddress() != null) {
                 buf.append("$i.address = " + PowerShellUtils.escape(ip.getAddress()) + "; ");
             }
             if (ip.getNetmask() != null) {
                 buf.append("$i.subnet = " + PowerShellUtils.escape(ip.getNetmask()) + "; ");
             }
             if (ip.getGateway() != null) {
                 buf.append("$i.gateway = " + PowerShellUtils.escape(ip.getGateway()) + "; ");
             }
         }
 
         if (network.getVlan() != null) {
             buf.append("$i.vlanid " + PowerShellUtils.escape(network.getVlan().getId()) + "; ");
         }
         if (network.isStp() != null) {
             if (network.isStp()) {
                 buf.append("$i.stp = $true; ");
             } else {
                 buf.append("$i.stp = $false; ");
             }
         }
 
         buf.append("update-network");
         buf.append(" -networkobject $i");
         buf.append(" -datacenterid $i.datacenterid");
 
         buf.append(" } ");
         buf.append("}");
 
         return LinkHelper.addLinks(getUriInfo(), runAndParseSingle(buf.toString()));
     }
 }
