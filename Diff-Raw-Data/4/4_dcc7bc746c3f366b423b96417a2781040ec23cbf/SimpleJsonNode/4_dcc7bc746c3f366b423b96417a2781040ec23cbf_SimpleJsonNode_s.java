 package com.dc2f.datastore.impl.filejson;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.dc2f.datastore.AttributesDefinition;
 import com.dc2f.datastore.ContentRepository;
 import com.dc2f.datastore.DefaultNodeType;
 import com.dc2f.datastore.Node;
 import com.dc2f.datastore.NodeType;
 import com.dc2f.datastore.exception.UnknownPropertyException;
 
 public class SimpleJsonNode implements Node {
 	private static final Logger logger = Logger.getLogger(SimpleJsonNode.class.getName());
 
 	private ContentRepository repository;
 	private String path;
 	private JSONObject jsonObject;
 	private NodeType nodeType;
 
 	public SimpleJsonNode(ContentRepository repository, String path, JSONObject jsonObject, NodeType nodeType) {
 		this.repository = repository;
 		this.path = path;
 		this.jsonObject = jsonObject;
 		this.nodeType = nodeType;
 	}
 	
 	public JSONObject getJsonObject() {
 		return jsonObject;
 	}
 
 	@Override
 	public String getName() {
 		return new File(path).getName();
 	}
 
 	@Override
 	public NodeType getNodeType() {
 		return nodeType;
 	}
 
 	@Override
 	public String getPath() {
 		return path;
 	}
 
 	/*
 	@Override
 	public Object getProperty(String propertyName) {
 		Object obj;
 		try {
 			obj = jsonObject.get(propertyName);
 		} catch (JSONException e) {
 			logger.log(Level.SEVERE, "Error while getting property {" + propertyName
 					+ "} of node {" + path + "}", e);
 			return null;
 		}
 		if (obj instanceof String) {
 			return obj;
 		}
 		logger.severe("FIXME: Not Implemented: Unable to convert property {" + path + "} of node type {" + getName() + "}: {" + obj.getClass().getName() + "}");
 		return null;
 	}
 	*/
 	
 	protected Object internalGetProperty(String propertyName) {
 		try {
 			return jsonObject.get(propertyName);
 		} catch (JSONException e) {
 			if (jsonObject.has(propertyName)) {
 				logger.log(Level.SEVERE, "Error while getting property {" + propertyName
 						+ "} of node type {" + path + "}", e);
 			}
 			return null;
 		}
 	}
 	
 	
 	
 	@Override
 	public Object getProperty(String propertyName) {
 		Object obj = internalGetProperty(propertyName);
 		
 		// FIXME do this the "clean way"
 		if (obj instanceof String && ("class".equals(propertyName) || "type".equals(propertyName))) {
 		//if (obj instanceof String && ("class".equals(propertyName) )) {
 			return obj;
 		}
 		
 		AttributesDefinition attrDefinitions = getNodeType().getAttributeDefinitions();
 		logger.finest(this.getName() + ": Getting property {" + propertyName + "}: " + obj + " - attrDefinitions: " + attrDefinitions);
 		Node attrDefinition = (Node) attrDefinitions.getAttributeDefinition(propertyName);
 		if (attrDefinition == null) {
 			throw new UnknownPropertyException("Unknown property {" + propertyName + "} for {" + getNodeType() + "}", null);
 		}
 		String attributeType = (String) attrDefinition.getProperty("type");
 		
 		if ("NodeReference".equals(attributeType) && obj instanceof String) {
 			String ref = (String) obj;
 			logger.fine("Trying to resolve object {" + ref + "}");
 			if (!ref.startsWith("/")) {
 				try {
 					ref = new URI(getPath()).resolve(ref).toString();
 				} catch (URISyntaxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			return repository.getNode(ref);
 		} if ("clob".equals(attributeType) && obj == null) {
 			obj = ((SimpleFileContentRepository)repository).loadRepositoryFile(new File(path, propertyName + ".clob.property"));
 		}
 
 		if (obj instanceof String) {
 			return obj;
 		}
 		if (obj == null) {
 			// FIXME check if property is required?!
 			return null;
 		}
 
 
 		if ("Node".equals(attributeType)) {
 
 			String subNodeTypeName = ((JSONObject)obj).optString("nodetype", null);
 			NodeType currentSubNodeType = null;
 			if (subNodeTypeName != null) {
 				currentSubNodeType = repository.getNodeType(subNodeTypeName);
 			}
 			
 			if (currentSubNodeType == null) {
 				String typeofnode = (String) attrDefinition.getProperty("typeofnode");
 				if (typeofnode != null) {
 					currentSubNodeType = repository.getNodeType(typeofnode);
 				}
 			}
 			if (currentSubNodeType == null) {
 				currentSubNodeType = new DefaultNodeType();
 			}
 			return new SimpleJsonNode(repository, path, (JSONObject) obj, currentSubNodeType);
 		} else if ("ListOfNodes".equals(attributeType)) {
 			JSONArray array = (JSONArray) obj;
 			String typeofsubnodes = (String) attrDefinition.getProperty("typeofsubnodes");
 			NodeType subNodeType = null;
 			if (typeofsubnodes != null) {
 				subNodeType = repository.getNodeType(typeofsubnodes);
 			}
 			List<Node> ret = new ArrayList<Node>();
 			for (int i = 0 ; i < array.length() ; i++) {
 				try {
 					JSONObject arrayobj = (JSONObject) array.get(i);
 					String subNodeTypeName = arrayobj.optString("nodetype", null);
 					NodeType currentSubNodeType = subNodeType;
 					if (subNodeTypeName != null) {
 						currentSubNodeType = repository.getNodeType(subNodeTypeName);
 					}
 					ret.add(new SimpleJsonNode(repository, path, arrayobj, currentSubNodeType));
 				} catch (JSONException e) {
 					logger.log(Level.SEVERE, "Error while converting property into ListOfNodes", e);
 				}
 				
 			}
 			return ret;
 		} else if ("Boolean".equals(attributeType)) {
 			if (obj instanceof Boolean) {
 				return (Boolean) obj;
 			}
 			return null;
 		}
 		logger.info("getting property " + propertyName + " --- attrDefinitions: " + attrDefinitions + " attrDefinition: " + attrDefinition);
 		logger.severe("FIXME: Not Implemented: Unable to convert property {" + propertyName + "} of node type {" + getName() + "}: {" + obj.getClass().getName() + "}");
 		return null;
 	}
 	
 	@Override
 	public String toString() {
 		return "{SimpleJsonNode:" + getPath() + "}";
 		//return "{SimpleJsonNode:" + jsonObject.toString() + "}";
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof SimpleJsonNode) {
 			return getPath().equals(((SimpleJsonNode)obj).getPath());
 		}
 		return super.equals(obj);
 	}
 
 }
