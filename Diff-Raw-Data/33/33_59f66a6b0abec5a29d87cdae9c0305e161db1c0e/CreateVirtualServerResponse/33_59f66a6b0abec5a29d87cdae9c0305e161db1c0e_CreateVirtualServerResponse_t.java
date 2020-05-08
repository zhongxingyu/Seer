 /**
  * @author Nigel Cook
  *
  * (C) Copyright 2010-2012. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  */
 package n3phele.service.model.core;
 
 import java.net.URI;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
import n3phele.service.actions.CreateVMAction;

 @XmlRootElement(name="CreateVirtualServerResponse")
 @XmlType(name="CreateVirtualServerResponse", propOrder={"vmList"})
 
 public class CreateVirtualServerResponse {
 	public URI vmList[];
	final private static java.util.logging.Logger log = java.util.logging.Logger.getLogger(CreateVirtualServerResponse.class.getName()); 
 	
 	public CreateVirtualServerResponse() {}
 
 	public CreateVirtualServerResponse(List<URI> vmRefs) {
		log.info("Create VMAction called");
 		vmList = vmRefs.toArray(new URI[vmRefs.size()]);
 	};
	
	public URI[] getVmList(){
		return this.vmList;
	}
 }
