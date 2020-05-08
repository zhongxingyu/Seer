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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import com.redhat.rhevm.api.common.resource.StorageDomainActionValidator;
 import com.redhat.rhevm.api.common.util.JAXBHelper;
 import com.redhat.rhevm.api.common.util.LinkHelper;
 import com.redhat.rhevm.api.model.Action;
 import com.redhat.rhevm.api.model.ActionsBuilder;
 import com.redhat.rhevm.api.model.ActionValidator;
 import com.redhat.rhevm.api.model.Link;
 import com.redhat.rhevm.api.model.StorageDomain;
 import com.redhat.rhevm.api.model.StorageDomainStatus;
 import com.redhat.rhevm.api.resource.AttachmentsResource;
 import com.redhat.rhevm.api.resource.StorageDomainResource;
 import com.redhat.rhevm.api.powershell.model.PowerShellStorageDomain;
 import com.redhat.rhevm.api.powershell.util.PowerShellCmd;
 import com.redhat.rhevm.api.powershell.util.PowerShellParser;
 import com.redhat.rhevm.api.powershell.util.PowerShellPool;
 import com.redhat.rhevm.api.powershell.util.PowerShellPoolMap;
 import com.redhat.rhevm.api.powershell.util.PowerShellUtils;
 
 import static com.redhat.rhevm.api.common.util.CompletenessAssertor.validateParameters;
 
 
 public class PowerShellStorageDomainResource extends AbstractPowerShellActionableResource<StorageDomain> implements StorageDomainResource {
 
     private PowerShellStorageDomainsResource parent;
     private StorageDomain tornDown;
 
     public PowerShellStorageDomainResource(String id,
                                            PowerShellStorageDomainsResource parent,
                                            PowerShellPoolMap shellPools,
                                            PowerShellParser parser) {
         super(id, parent.getExecutor(), shellPools, parser);
         this.parent = parent;
     }
 
     public StorageDomain getTornDown() {
         return tornDown;
     }
 
     /**
      * Run a powershell command and parse the output as a list of storage
      * domains.
      * <p>
      * If the resulting storage domains are being viewed in the context
      * of a specific data center, then the caller wants the value of the
      * 'status' property. In this case, @sharedStatus should be #false.
      * <p>
      * If the storage domain is being viewed outside of the context of any
      * data center, then the 'sharedStatus' property contains the required
      * status and the caller should supply #true for @sharedStatus.
      *
      * @param command the powershell command to execute
      * @param sharedStatus whether the 'sharedStatus' property is needed
      * @return a list of storage domains
      */
     public static List<StorageDomain> runAndParse(PowerShellPool pool,
                                                   PowerShellParser parser,
                                                   String command,
                                                   boolean sharedStatus) {
         List<PowerShellStorageDomain> storageDomains =
             PowerShellStorageDomain.parse(parser, PowerShellCmd.runCommand(pool, command));
         List<StorageDomain> ret = new ArrayList<StorageDomain>();
 
         for (PowerShellStorageDomain storageDomain : storageDomains) {
             if (sharedStatus) {
                 storageDomain.setStatus(storageDomain.getSharedStatus());
             }
             ret.add(storageDomain);
         }
 
         return ret;
     }
 
     /**
      * Run a powershell command and parse the output as a list of storage
      * domains. The 'sharedStatus' property in the output from the command
      * is ignored.
      *
      * @param command the powershell command to execute
      * @return a list of storage domains
      */
     public static List<StorageDomain> runAndParse(PowerShellPool pool, PowerShellParser parser, String command) {
         return runAndParse(pool, parser, command, false);
     }
 
     /**
      * Run a powershell command and parse the output as a single storage
      * domain.
      *
      * @param command the powershell command to execute
      * @param whether the 'sharedStatus' property is needed
      * @return a single storage domain, or null
      */
     public static StorageDomain runAndParseSingle(PowerShellPool pool,
                                                   PowerShellParser parser,
                                                   String command,
                                                   boolean sharedStatus) {
         List<StorageDomain> storageDomains = runAndParse(pool, parser, command, sharedStatus);
 
         return !storageDomains.isEmpty() ? storageDomains.get(0) : null;
     }
 
     /**
      * Run a powershell command and parse the output as a single storage
      * domain. The 'sharedStatus' property in the output from the command
      * is ignored.
      *
      * @param command the powershell command to execute
      * @return a single storage domain, or null
      */
     public static StorageDomain runAndParseSingle(PowerShellPool pool, PowerShellParser parser, String command) {
         return runAndParseSingle(pool, parser, command, false);
     }
 
     public StorageDomain runAndParseSingle(String command, boolean sharedStatus) {
         return runAndParseSingle(getPool(), getParser(), command, sharedStatus);
     }
 
     public static StorageDomain addLinks(StorageDomain storageDomain) {
         storageDomain = JAXBHelper.clone("storage_domain", StorageDomain.class, storageDomain);
 
         storageDomain = LinkHelper.addLinks(storageDomain);
 
         ActionValidator actionValidator = new StorageDomainActionValidator(storageDomain);
         ActionsBuilder actionsBuilder = new ActionsBuilder(LinkHelper.getUriBuilder(storageDomain),
                                                            StorageDomainResource.class,
                                                            actionValidator);
         storageDomain.setActions(actionsBuilder.build());
 
         Link link = new Link();
         link.setRel("attachments");
         link.setHref(LinkHelper.getUriBuilder(storageDomain).path("attachments").build().toString());
         storageDomain.getLinks().clear();
         storageDomain.getLinks().add(link);
 
         return storageDomain;
     }
 
     @Override
     public StorageDomain get(UriInfo uriInfo) {
         StorageDomain storageDomain;
         if (tornDown != null) {
             storageDomain = tornDown;
         } else {
             storageDomain = runAndParseSingle("get-storagedomain " + PowerShellUtils.escape(getId()), true);
         }
         return addLinks(storageDomain);
     }
 
     @Override
     public StorageDomain update(final UriInfo uriInfo, StorageDomain storageDomain) {
         validateUpdate(storageDomain);
 
         StringBuilder buf = new StringBuilder();
         if (tornDown != null) {
             // update writable fields only
             if (storageDomain.isSetName()) {
                 tornDown.setName(storageDomain.getName());
             }
             storageDomain = tornDown;
         } else {
            buf.append("$d = get-storagedomain " + PowerShellUtils.escape(getId()) + ";");
 
             if (storageDomain.isSetName()) {
                buf.append("$d.name = " + PowerShellUtils.escape(storageDomain.getName()) + ";");
             }
 
            buf.append("update-storagedomain -storagedomainobject $d");
 
             storageDomain = runAndParseSingle(buf.toString(), true);
         }
         return addLinks(storageDomain);
     }
 
     @Override
     public Response teardown(UriInfo uriInfo, Action action) {
         validateParameters(action, "host.id|name");
         return doAction(uriInfo, new StorageDomainTeardowner(action));
     }
 
     @Override
     public AttachmentsResource getAttachmentsResource() {
         return new PowerShellAttachmentsResource(getId(), shellPools, getParser());
     }
 
     private class StorageDomainTeardowner extends AbstractPowerShellActionTask {
 
         public StorageDomainTeardowner(Action action) {
             super(action, "remove-storagedomain -force");
         }
 
         public void execute() {
             String id = PowerShellStorageDomainResource.this.getId();
 
             StorageDomain storageDomain =
                 runAndParseSingle("get-storagedomain " + PowerShellUtils.escape(id), true);
 
             StringBuilder buf = new StringBuilder();
 
             String hostArg = null;
             if (action.getHost().isSetId()) {
                 hostArg = PowerShellUtils.escape(action.getHost().getId());
             } else {
                 buf.append("$h = select-host -searchtext ");
                 buf.append(PowerShellUtils.escape("name=" +  action.getHost().getName()));
                 buf.append(";");
                 hostArg = "$h.hostid";
             }
 
             buf.append(command);
 
             buf.append(" -storagedomainid " + PowerShellUtils.escape(id));
 
             buf.append(" -hostid " + hostArg);
 
             PowerShellCmd.runCommand(getPool(), buf.toString());
 
             storageDomain.setStatus(StorageDomainStatus.TORNDOWN);
             PowerShellStorageDomainResource.this.tornDown = storageDomain;
             PowerShellStorageDomainResource.this.parent.addToTornDownDomains(id, PowerShellStorageDomainResource.this);
         }
     }
 }
