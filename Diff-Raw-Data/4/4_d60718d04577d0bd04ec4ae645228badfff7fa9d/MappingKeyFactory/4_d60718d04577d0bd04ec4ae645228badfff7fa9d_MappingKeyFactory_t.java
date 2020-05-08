 package cytoscape.visual.ui;
 
 import giny.model.GraphObject;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesUtils;
 import cytoscape.visual.mappings.ObjectMapping;
 
 public class MappingKeyFactory {
 
 	public static Set<Object> getKeySet(final String attrName,
 			final CyAttributes attrs, final ObjectMapping mapping,
 			boolean isNode) {
 
 		if (attrName.equals("ID")) {
 			return loadID(isNode);
 		}
 
 		final Map<String, Object> id2AttrValMap = CyAttributesUtils
 				.getAttribute(attrName, attrs);
 
 		if ((id2AttrValMap == null) || (id2AttrValMap.size() == 0))
 			return new TreeSet<Object>();
 
 		final List<Class<?>> acceptedClasses = Arrays.asList(mapping
 				.getAcceptedDataClasses());
 		final Class<?> mapAttrClass = CyAttributesUtils.getClass(attrName,
 				attrs);
 
 		if ((mapAttrClass == null) || !(acceptedClasses.contains(mapAttrClass)))
 			return new TreeSet<Object>(); // Return empty set.
 
 		return loadKeySet(id2AttrValMap);
 	}
 
 	/**
 	 * Create String set of node/edge ID.
 	 * 
 	 * @param isNode
 	 * @return set of ID
 	 */
 	private static Set<Object> loadID(final boolean isNode) {
 		final Set<Object> ids = new TreeSet<Object>();
 
 		final List<GraphObject> obj;
 		if (isNode)
 			obj = Cytoscape.getCurrentNetworkView().getNetwork().nodesList();
 		else
 			obj = Cytoscape.getCurrentNetworkView().getNetwork().edgesList();
 
 		for (GraphObject o : obj)
 			ids.add(o.getIdentifier());
 
 		return ids;
 	}
 
 	/**
 	 * Loads the Key Set.
 	 */
 	private static Set<Object> loadKeySet(final Map<String, Object> id2AttrMap) {
 		final Set<Object> mappedKeys = new TreeSet<Object>();
 
 		for (final Object attrValue : id2AttrMap.values()) {
			if(attrValue == null)
				continue;
				
 			if (attrValue instanceof List<?>) {
 				// This is list, but contents of list is unknown
 				List<?> list = (List<?>) attrValue;
 
 				for (int i = 0; i < list.size(); i++) {
 					final Object vo = list.get(i);
 
 					if (!mappedKeys.contains(vo))
 						mappedKeys.add(vo);
 				}
 			} else {
 				if (!mappedKeys.contains(attrValue))
 					mappedKeys.add(attrValue);
 			}
 		}
 		return mappedKeys;
 	}
 }
