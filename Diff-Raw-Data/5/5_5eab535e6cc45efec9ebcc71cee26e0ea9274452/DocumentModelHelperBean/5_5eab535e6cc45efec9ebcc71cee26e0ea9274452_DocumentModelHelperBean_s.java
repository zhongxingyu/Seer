 package org.easysoa.registry.beans;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.easysoa.registry.DocumentService;
 import org.easysoa.registry.types.IntelligentSystem;
 import org.easysoa.registry.types.Repository;
 import org.easysoa.registry.types.TaggingFolder;
 import org.easysoa.registry.utils.DocumentModelHelper;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Install;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
 
 @Name("documentModelHelperBean")
 @Scope(ScopeType.CONVERSATION)
 @Install(precedence = Install.FRAMEWORK)
 public class DocumentModelHelperBean {
 
 	public static final String PARENT_TYPE_CLASSIFICATION = "Classification";
 	
 	public static final String PARENT_TYPE_DOCTYPE = "Document types";
 	
 	public static final String PARENT_TYPE_MODEL = "Related documents";
 	
     @In
     private CoreSession documentManager;
 
     @In(create = true)
     private DocumentService documentService;
 
     public String getDocumentTypeLabel(DocumentModel model) throws Exception {
         return DocumentModelHelper.getDocumentTypeLabel(model.getType());
     }
     
     public DocumentModelList findAllParents(DocumentModel documentModel) throws Exception {
         return documentService.findAllParents(documentManager, documentModel);
     }
     
     public Map<String, DocumentModelList> findAllParentsByType(DocumentModel documentModel) throws Exception {
     	Map<String, DocumentModelList> parentsByType = new HashMap<String, DocumentModelList>();
     	DocumentModelList parentModels = findAllParents(documentModel);
         for (DocumentModel parentModel : parentModels) {
         	if (TaggingFolder.DOCTYPE.equals(parentModel.getType())) {
         		addParent(parentsByType, PARENT_TYPE_CLASSIFICATION, parentModel);
         	}
        	else if (IntelligentSystem.DOCTYPE.equals(parentModel.getType())) {
         		if (parentModel.getPathAsString().startsWith(Repository.REPOSITORY_PATH)) {
             		addParent(parentsByType, PARENT_TYPE_DOCTYPE, parentModel);
         		}
         		else {
             		addParent(parentsByType, PARENT_TYPE_CLASSIFICATION, parentModel);
         		}
         	}
         	else {
         		addParent(parentsByType, PARENT_TYPE_MODEL, parentModel);
         	}
         }
         return parentsByType;
     }
 
 	private void addParent(Map<String, DocumentModelList> parentsByType,
 			String parentTypeClassification, DocumentModel parentModel) {
 		if (!parentsByType.containsKey(parentTypeClassification)) {
 			parentsByType.put(parentTypeClassification, new DocumentModelListImpl());
 		}
 		parentsByType.get(parentTypeClassification).add(parentModel);
 	}
     
 }
