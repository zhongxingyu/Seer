 package org.easysoa.registry;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 
 /**
  * 
  * @author mkalam-alami
  *
  */
 public interface SoaMetamodelService {
 
     public static final String EXTENSIONPOINT_TYPES = "types";
 
     public static final String EXTENSIONPOINT_INHERITEDFACETS = "inheritedFacets";
     
     Collection<String> getChildren(String type);
     
     /**
      * Computes the minimal path needed to link two document types.
      * The returned list is empty if <i>toType</i> can be directly stored under <i>fromType</i>.
      * 
      * @param fromType The parent type
      * @param toType The child type
      * @return The path (without <i>fromType</i>, or null if it's not possible
      * to store <i>toType</i> under <i>fromType</i>)
      */
     List<String> getPath(String fromType, String toType);
     
 	Set<String> getInheritedFacets(Set<String> filter);
 
 	void applyFacetInheritance(CoreSession documentManager, DocumentModel model,
 			boolean isFacetSource) throws Exception;
 
 	void resetInheritedFacets(DocumentModel model) throws Exception;
 	
 	/**
 	 * Ensures that an SoaNode id valid, i.e. that:
 	 * - it's soan:name is not null
 	 * - it's soan:name is consistent with its other properties
 	 * @return null if it was valid, otherwise the expected soa name (only if returnExpectedNameIfNull is enabled)
 	 * @throws ModelIntegrityException
 	 * @throws ClientException
 	 */
 	String validateIntegrity(DocumentModel model, boolean returnExpectedNameIfNull)
 			throws ModelIntegrityException, ClientException;
 
	void validateWriteRightsOnProperties(DocumentModel documentModel,
			Map<String, Object> properties) throws ModelIntegrityException, ClientException;
 
 	
 }
