 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.resources;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Delete;
 import org.restlet.resource.Get;
 import eu.stratuslab.storage.disk.main.PersistentDiskApplication;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 
 public class DiskResource extends BaseResource {
 
 	@Get
 	public Representation listDiskProperties() {
 		Map<String, Object> infos = createInfoStructure("Disk info");
 
 		if (!diskExists(getDiskId())) {
 			return respondError(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Disk does not exists");
 		}
 
 		Properties diskProperties = getDiskProperties();
 
 		if (!hasSuficientRightsToView(diskProperties)) {
 			return respondError(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Not enought rights to display disk properties");
 		}
 
 		infos.put("properties", diskProperties);
 		infos.put("url", getCurrentUrl());
 		infos.put("can_delete", hasSuficientRightsToDelete(diskProperties));
 
 		addDiskUserHeader();
 
 		return directTemplateRepresentation("disk.ftl", infos);
 	}
 
 	private Properties getDiskProperties() {
 		return zk.getDiskProperties(getDiskZkPath());
 	}
 
 	private void addDiskUserHeader() {
 		Form diskUserHeaders = (Form) getResponse().getAttributes().get(
 				"org.restlet.http.headers");
 
 		if (diskUserHeaders == null) {
 			diskUserHeaders = new Form();
 			getResponse().getAttributes().put("org.restlet.http.headers",
 					diskUserHeaders);
 		}
 
 		diskUserHeaders.add("X-DiskUser-Limit",
 				String.valueOf(PersistentDiskApplication.USERS_PER_DISK));
 		diskUserHeaders.add("X-DiskUser-Remaining",
 				String.valueOf(zk.remainingFreeUser(getDiskZkPath())));
 	}
 
 	@Delete
 	public Representation deleteDiskRequest() {
 		Representation response = null;
 		Properties diskProperties = getDiskProperties();
 
 		if (!hasSuficientRightsToDelete(diskProperties)) {
 			return respondError(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Not enought rights to delete disk");
 		}
 
 		if (zk.getDiskUsersNo(getDiskZkPath()) > 0) {
 			return respondError(Status.CLIENT_ERROR_BAD_REQUEST,
 					"Can't remove the disk as it is in use");
 		}
 
 		deleteDisk();
 
 		if (useAPI()) {
 			Map<String, Object> info = new HashMap<String, Object>();
 			info.put("key", "uuid");
 			info.put("value", getDiskId());
 			response = directTemplateRepresentation("keyvalue.ftl", info);
 		} else {
 			MESSAGES.push("Your disk have been deleted successfully");
 			redirectSeeOther(getBaseUrl() + "/disks/");
 		}
 
 		return response;
 	}
 
 	private void deleteDisk() {
 		zk.deleteDiskProperties(getDiskZkPath());
 		DiskUtils.removeDisk(getDiskId());
 	}
 
 	private String getDiskZkPath() {
 		return getDiskZkPath(getDiskId());
 	}
 
 	private String getDiskId() {
 		Map<String, Object> attributes = getRequest().getAttributes();
 
 		return attributes.get("uuid").toString();
 	}
 
 }
