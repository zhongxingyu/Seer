 package org.easysoa.registry;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.easysoa.registry.inheritance.ChildrenModelSelector;
 import org.easysoa.registry.inheritance.InheritedFacetDescriptor;
 import org.easysoa.registry.inheritance.InheritedFacetDescriptor.TransferLogic;
 import org.easysoa.registry.inheritance.InheritedFacetModelSelector;
 import org.easysoa.registry.inheritance.UuidInSourceSelector;
 import org.easysoa.registry.inheritance.UuidInTargetSelector;
 import org.easysoa.registry.types.SoaNode;
 import org.easysoa.registry.utils.SimpleELEvaluator;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.core.schema.types.CompositeType;
 import org.nuxeo.ecm.core.schema.types.Field;
 import org.nuxeo.ecm.core.schema.types.Schema;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentInstance;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 import edu.uci.ics.jung.algorithms.shortestpath.ShortestPath;
 import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 
 /**
  * 
  * @author mkalam-alami
  *
  */
 public class SoaMetamodelServiceImpl extends DefaultComponent implements SoaMetamodelService {
 
     private static Logger logger = Logger.getLogger(SoaMetamodelServiceImpl.class);
     
     private Graph<String, String> graph = new DirectedSparseGraph<String, String>();
    
     private UnweightedShortestPath<String, String> shortestPath
     	= new UnweightedShortestPath<String, String>(graph);
     
     private Map<String, InheritedFacetDescriptor> inheritedFacets
     	= new HashMap<String, InheritedFacetDescriptor>();
 
 	private Map<String, InheritedFacetModelSelector> facetTransferSelectors
 		= new HashMap<String, InheritedFacetModelSelector>();
 
 	private Map<String, SoaNodeTypeDescriptor> soaNodeTypes
 		= new HashMap<String, SoaNodeTypeDescriptor>();
     
 	public SoaMetamodelServiceImpl() {
 		facetTransferSelectors.put(ChildrenModelSelector.NAME, new ChildrenModelSelector());
 		facetTransferSelectors.put(UuidInSourceSelector.NAME, new UuidInSourceSelector());
 		facetTransferSelectors.put(UuidInTargetSelector.NAME, new UuidInTargetSelector());
 	}
 	
     @Override
     public void registerContribution(Object contribution, String extensionPoint,
             ComponentInstance contributor) throws Exception {
         if (EXTENSIONPOINT_TYPES.equals(extensionPoint)) {
             SoaNodeTypeDescriptor descriptor = (SoaNodeTypeDescriptor) contribution;
             soaNodeTypes.put(descriptor.name, descriptor);
             graph.addVertex(descriptor.name);
             for (String subtype : descriptor.subtypes) {
                 graph.addVertex(subtype);
                 graph.addEdge(descriptor.name + " contains " + subtype, descriptor.name, subtype);
             }
         }
         else if (EXTENSIONPOINT_INHERITEDFACETS.equals(extensionPoint)) {
         	InheritedFacetDescriptor descriptor = (InheritedFacetDescriptor) contribution;
         	if (descriptor.facetName != null && !descriptor.transferLogicList.isEmpty()) {
         		inheritedFacets.put(descriptor.facetName, descriptor);
         	}
         	else {
         		logger.error("Invalid " + EXTENSIONPOINT_INHERITEDFACETS + " contribution");
         	}
         }
     }
     
     public Collection<String> getChildren(String type) {
         return graph.getSuccessors(type);
     }
 
     public List<String> getPath(String fromType, String toType) {
         return getPath(graph, shortestPath, fromType, toType);
     }
 
     // Modified version of ShortestPathUtils.getPath()
     private static <V, E> List<V> getPath(Graph<V, E> graph, ShortestPath<V, E> sp, V source, V target) {
         LinkedList<V> path = new LinkedList<V>();
         Map<V, E> incomingEdges = sp.getIncomingEdgeMap(source);
 
         if (incomingEdges.isEmpty() || incomingEdges.get(target) == null) {
             return null;
         }
         V current = target;
         while (!current.equals(source)) {
             E incoming = incomingEdges.get(current);
             path.addFirst(current);
             Pair<V> endpoints = graph.getEndpoints(incoming);
             if (endpoints.getFirst().equals(current)) {
                 current = endpoints.getSecond();
             } else {
                 current = endpoints.getFirst();
             }
         }
 
         return path;
     }
     
     public Set<String> getInheritedFacets(Set<String> facetsToTest) {
     	Set<String> facetsSubset = new HashSet<String>(inheritedFacets.keySet());
     	facetsSubset.retainAll(facetsToTest);
     	return facetsSubset;
     }
 
 	@Override
 	public void applyFacetInheritance(CoreSession documentManager,
 			DocumentModel model, boolean isFacetSource) throws Exception {
 		SchemaManager schemaManager = Framework.getService(SchemaManager.class);
 		for (String facet : getInheritedFacets(model.getFacets())) {
 			InheritedFacetDescriptor inheritance = inheritedFacets.get(facet);
 			for (TransferLogic transferLogic : inheritance.transferLogicList) {
 				if (isFacetSource && model.getType().equals(transferLogic.from)) {
 					DocumentModelList modelsToWriteOn = facetTransferSelectors 
 							.get(transferLogic.selectorType)
 							.findTargets(documentManager, model, transferLogic.to, transferLogic.parameters);
 					
 					if (modelsToWriteOn != null) {
 						for (DocumentModel modelToWriteOn : modelsToWriteOn) {
 							CompositeType facetToCopy = schemaManager.getFacet(facet);
 							for (String schemaToCopy : facetToCopy.getSchemaNames()) {
 								modelToWriteOn.setProperties(schemaToCopy,
 										model.getProperties(schemaToCopy));
 								documentManager.saveDocument(modelToWriteOn);
 							}
 						}
 					}
 				}
 				if (!isFacetSource && model.getType().equals(transferLogic.to)) {
 					DocumentModel modelToReadFrom = facetTransferSelectors 
 							.get(transferLogic.selectorType)
 							.findSource(documentManager, model, transferLogic.from, transferLogic.parameters);
 
 					if (modelToReadFrom != null) {
 						CompositeType facetToCopy = schemaManager.getFacet(facet);
 						for (String schemaToCopy : facetToCopy.getSchemaNames()) {
 							model.setProperties(schemaToCopy,
 									modelToReadFrom.getProperties(schemaToCopy));
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void resetInheritedFacets(DocumentModel model) throws Exception {
 		SchemaManager schemaManager = Framework.getService(SchemaManager.class);
 		for (String inheritedFacet : getInheritedFacets(model.getFacets())) {
 			CompositeType facetToReset = schemaManager.getFacet(inheritedFacet);
 			InheritedFacetDescriptor inheritedFacetDesc = inheritedFacets.get(inheritedFacet);
 			
 			boolean isFacetInherited = false;
 			for (TransferLogic transferLogic : inheritedFacetDesc.transferLogicList) {
 				if (model.getType().equals(transferLogic.to)) {
 					isFacetInherited = true;
 					break;
 				}
 			}
 			
 			if (isFacetInherited) {
 				for (Schema schemaToReset : facetToReset.getSchemas()) {
 					for (Field fieldToReset : schemaToReset.getFields()) {
 						model.setPropertyValue(fieldToReset.getName().toString(), null);
 					}
 				}
 			}
 		}
 	}
 	
 	public String validateIntegrity(DocumentModel model, boolean returnExpectedNameIfNull) throws ModelIntegrityException, ClientException {
 		SoaNodeTypeDescriptor soaNodeTypeDescriptor = soaNodeTypes.get(model.getType());
 		if (soaNodeTypeDescriptor != null) {
 		   SimpleELEvaluator elEvaluator = new SimpleELEvaluator();
            String expectedSoaName = soaNodeTypeDescriptor.evaluateSoaName(elEvaluator, model);
            Serializable actualSoaName = model.getPropertyValue(SoaNode.XPATH_SOANAME);
 
            if (actualSoaName == null) {
         	   if (returnExpectedNameIfNull) {
         		   if (expectedSoaName != null) {
         			   return expectedSoaName;
         		   }
         		   else {
         			   return (String) model.getPropertyValue(SoaNode.XPATH_TITLE);
         		   }
         	   }
         	   else {
 	           		throw new ModelIntegrityException("Null soaname for " + model.getPathAsString()
 		           			+ " - did happen when : doc was created when it already existed "
 		           			+ "because it was queried for using erroneously safeName(), "
 		           			+ "doc was created before setting soaname");
         	   }
            }
            
            if (!actualSoaName.equals(expectedSoaName)) {
                throw new ModelIntegrityException("Invalid SoaName, found '" 
                         + actualSoaName + "' instead of '" + expectedSoaName + "'");
            }
 		}
 		
 		return null;
 	}
 	
	public void validateWriteRightsOnProperties(DocumentModel model, Map<String, Object> properties) throws ModelIntegrityException, ClientException {
		SoaNodeTypeDescriptor soaNodeTypeDescriptor = soaNodeTypes.get(model.getType());
 		if (properties != null && soaNodeTypeDescriptor != null) {
 			List<String> immutableProperties = new ArrayList<String>();
 			immutableProperties.add(SoaNode.XPATH_SOANAME);
 			List<String> doctypeImmutableProperties = soaNodeTypeDescriptor.idProperties;
 			if (doctypeImmutableProperties != null) {
 				immutableProperties.addAll(doctypeImmutableProperties);
 			}
 			for (String immutableProperty : immutableProperties) {
				if (properties.containsKey(immutableProperty)
						&& !properties.get(immutableProperty).equals(model.getPropertyValue(immutableProperty))) {
 					throw new ModelIntegrityException("Property '" + immutableProperty + "' cannot be changed");
 				}
 			}
 		}
 	}
 
 }
