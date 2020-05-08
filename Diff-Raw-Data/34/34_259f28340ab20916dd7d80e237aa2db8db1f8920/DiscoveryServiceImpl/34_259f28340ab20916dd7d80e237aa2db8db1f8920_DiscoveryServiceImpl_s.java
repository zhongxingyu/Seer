 package org.easysoa.registry;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.types.ids.SoaNodeId;
 import org.nuxeo.common.utils.IdUtils;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.runtime.api.Framework;
 
 /**
  * Computes...
  * 
  * still TODO (see FIXMEs) :
  * if needed soaMetamodel performances
  * special cases (see FIXMEs)
  * 
  * @author mkalam-alami, mdutoo
  *
  */
 public class DiscoveryServiceImpl implements DiscoveryService {
 
     public DocumentModel runDiscovery(CoreSession documentManager, SoaNodeId identifier,
             Map<String, Object> properties, List<SoaNodeId> parentDocuments) throws Exception {
 
     	// FIXME
 //        if (Endpoint.DOCTYPE.equals(identifier.getType())) { // TODO MDU hack
 //        	return discoverEndpoint(documentManager, identifier, properties, parentDocuments);
 //        }
     	
     	SoaMetamodelService metamodelService = Framework.getService(SoaMetamodelService.class);
         DocumentService documentService = Framework.getService(DocumentService.class);
 
         if (!documentService.isSoaNode(documentManager, identifier.getType())) {
         	throw new Exception("Can only discover a SoaNode but is " + identifier
         			+ " - " + properties + " - " + parentDocuments);
         }
         
         // Fetch or create document
         boolean shouldCreate = false;
         DocumentModel documentModel = documentService.find(documentManager, identifier);
         if (documentModel == null) {
             shouldCreate = true;
             documentModel = documentService.newSoaNodeDocument(documentManager, identifier);
         }
         else {
            metamodelService.validateWriteRightsOnProperties(identifier.getType(), properties);
         }
         
         // Set properties
         if (properties != null) {
             for (Entry<String, Object> property : properties.entrySet()) {
                 // FIXME Non-serializable error handling
             	Object propertyValue = property.getValue();
             	if (propertyValue instanceof Boolean) {
             		propertyValue = propertyValue.toString();
             	}
                 documentModel.setPropertyValue(property.getKey(), (Serializable) propertyValue);
             }
             //documentManager.saveDocument(documentModel); // TODO mdu ?!
         }
         
         // only now that props are OK, create or save document
         // (required below for handling parents by creating proxies to it)
         if (shouldCreate) {
         	documentModel = documentManager.createDocument(documentModel);
         } else if (properties != null && !properties.isEmpty()) {
         	documentModel = documentManager.saveDocument(documentModel);
         }
         
         // Link to parent documents
         // FIXME cache / build model of soaMetamodelService responses to speed it up
         if (parentDocuments != null && !parentDocuments.isEmpty()) {
             String type = documentModel.getType();
             SoaMetamodelService soaMetamodelService = Framework.getService(SoaMetamodelService.class);
             for (SoaNodeId parentDocumentId : parentDocuments) {
                 List<String> path = soaMetamodelService.getPath(parentDocumentId.getType(), type);
                 String parentPathAsString = null;
                 
                 if (parentDocumentId.getType() != null) {
 	                // Make sure parent is valid
 	                if (path == null) {
 	                    documentManager.cancel();
 	                    throw new Exception("No possible valid path from "
 	                            + parentDocumentId.getType() + " (" + parentDocumentId.toString()
 	                            + ") to " + type + " (" + identifier.getName() + ")");
 	                }
 	                else {
 		                DocumentModel parentDocument = documentService.find(documentManager, parentDocumentId);
 		                
 	                    // Create parent if necessary
 	                    if (parentDocument == null) {
 	                        parentDocument = documentService.create(documentManager, parentDocumentId);
 	                    }
 		                parentPathAsString = parentDocument.getPathAsString();
 	                    
 	                    // Link the intermediate documents 
 	                    // If we have unknown documents between the two, create placeholders
 	                    for (String pathStepType : path.subList(0, path.size() - 1)) {
 	                        // Before creating a placeholder, check if the intermediate type
 	                        // is not already listed in the parent documents
 	                        boolean placeholderNeeded = true;
 	                        for (SoaNodeId placeholderReplacementCandidate : parentDocuments) {
 	                            if (pathStepType.equals(placeholderReplacementCandidate.getType())) {
 	                                parentDocument = documentService.create(documentManager,
 	                                        placeholderReplacementCandidate, parentPathAsString);
 	        		                parentPathAsString = parentDocument.getPathAsString();
 	                                placeholderNeeded = false;
 	                                break;
 	                            }
 	                        }
 	                        
 	                        if (placeholderNeeded) {
 	                            parentDocument = documentService.create(documentManager,
 	                                    new SoaNodeId(pathStepType, IdUtils.generateStringId()), parentPathAsString);
 	                            parentDocument.setPropertyValue(SoaNode.XPATH_TITLE, "(Placeholder)");
 	                            parentDocument.setPropertyValue(SoaNode.XPATH_ISPLACEHOLDER, "1");
 	                        }
 	                    }
 	                }
                 
                 }
                 else {
                 	parentPathAsString = parentDocumentId.getName();
                 }
                 
                 // Create target document if necessary
                 if (documentService.findProxy(documentManager, identifier, parentPathAsString) == null) {
                     documentService.create(documentManager, identifier, parentPathAsString);
                 }
             }
         }
         
         return documentModel;
     }
 
 	@SuppressWarnings("unused")
 	private DocumentModel discoverEndpoint(CoreSession documentManager,
 			SoaNodeId identifier, Map<String, Object> properties,
 			List<SoaNodeId> parentDocuments) throws Exception {
         DocumentService documentService = Framework.getService(DocumentService.class);
 
 		// TODO differ between :
         List<SoaNodeId> suggestedParentIds = new ArrayList<SoaNodeId>(); // better : those that don't exist
         List<SoaNodeId> knownComponentIds = parentDocuments; // better : those that exist
         // TODO check that all component ids exist
         
         // Fetch or create document
         DocumentModel documentModel = documentService.findEndpoint(documentManager, identifier,
         		properties, suggestedParentIds, knownComponentIds);
 
         if (documentModel == null) {
             documentModel = documentService.create(documentManager, identifier);
             // TODO create suggestedParentIds if don't exist
             // TODO link with suggestedParentIds
             
         } else {
         	// TODO create assumedParentIds if don't exist
         }
         
         return documentModel;
 	}
 
 }
