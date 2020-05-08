 /*
  * Copyright (C) 2011 University of Edinburgh.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.org.ukfederation.mda;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import net.shibboleth.metadata.ErrorStatus;
 import net.shibboleth.metadata.ItemId;
 import net.shibboleth.metadata.ItemMetadata;
 import net.shibboleth.metadata.WarningStatus;
 import net.shibboleth.metadata.dom.DomElementItem;
 import net.shibboleth.metadata.pipeline.BaseStage;
 import net.shibboleth.metadata.util.ClassToInstanceMultiMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ErrorAnnouncingFilteringStage extends BaseStage<DomElementItem> {
 	
     /** Class logger. */
     private final Logger log = LoggerFactory.getLogger(ErrorAnnouncingFilteringStage.class);
 
 	public void doExecute(Collection<DomElementItem> metadataCollection) {
 		Iterator<DomElementItem> metadataIterator = metadataCollection.iterator();
 		while (metadataIterator.hasNext()) {
 			DomElementItem metadata = metadataIterator.next();
 			ClassToInstanceMultiMap<ItemMetadata> info = metadata.getItemMetadata();
 			List<ErrorStatus> errors = info.get(ErrorStatus.class);
 			if (errors.size() > 0) {
 				// Establish a name for this element
 				List<ItemId> entityId = info.get(ItemId.class);
 				String name;
 				if (entityId.size() > 0) {
 					name = entityId.get(0).getId();
 				} else {
 					name = "element";
 				}
 				log.error("removing " + name + "; reasons follow");
 				
 				// Mention any status items that were previously warnings on this element
 				if (log.isWarnEnabled()) {
 					for (WarningStatus warning: info.get(WarningStatus.class)) {
						log.warn("{}: {}", new Object[]{warning.getComponentId(), warning.getStatusMessage()});
 					}
 				}
 				
 				// Mention the errors on this element, which are the reason we are removing it
 				for (ErrorStatus error: errors) {
					log.error("{}: {}", new Object[]{error.getComponentId(), error.getStatusMessage()});
 				}
 				
 				// remove the element
 				metadataIterator.remove();
 			}
 		}
 		
 		// **TODO** should throw an exception under certain parameterised circumstances
 	}
 	
 	public ErrorAnnouncingFilteringStage() {
 	}
 }
