 /*
  *
  * Copyright (C) 2013 Altrusoft AB
  *
  * This file is part of Altrusoft Alfresco Play, ASAP
  *
  * ASAP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * ASAP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with ASAP. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package se.altrusoft.alfplay.node;
   
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.alfresco.service.cmr.repository.ChildAssociationRef;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.cmr.repository.StoreRef;
 import org.alfresco.service.namespace.QName;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.extensions.webscripts.AbstractWebScript;
 import org.springframework.extensions.webscripts.WebScriptException;
 import org.springframework.extensions.webscripts.WebScriptRequest;
 import org.springframework.extensions.webscripts.WebScriptResponse;
 
 import se.altrusoft.alfplay.repo.dictionary.DictionaryServiceExtras;
 import se.altrusoft.alfplay.repo.dictionary.SimpleQName;
 
 public class NodeMetadataGet extends AbstractWebScript {
 
 
 	protected NodeService nodeService;
 
 	protected DictionaryServiceExtras dictionaryServiceExtras;
 	
 	public void setNodeService(NodeService nodeService) {
 		this.nodeService = nodeService;
 	}
 	
 	public void setDictionaryServiceExtras(DictionaryServiceExtras dictionaryServiceExtras) {
 		this.dictionaryServiceExtras = dictionaryServiceExtras;
 	}
 	
 
 	@Override
 	public void execute(WebScriptRequest req, WebScriptResponse res)
 			throws IOException {		
 		// String contentType = req.getContentType(); 
		res.setContentEncoding("UTF-8");
 
 		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
 		String nodeId = templateArgs.get("nodeId");
 		if (nodeId == null) 
 			throw new WebScriptException("No nodeId provided");
 		
 		NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
 		if (! this.nodeService.exists(nodeRef)) 
 			throw new WebScriptException("No such node: " + nodeId);
 		
 		QName nodeType = this.nodeService.getType(nodeRef);
 		Set<QName> aspects = this.nodeService.getAspects(nodeRef);
 		Map<QName, Serializable> props = this.nodeService.getProperties(nodeRef);
 		List<ChildAssociationRef> children = this.nodeService.getChildAssocs(nodeRef);
 
 		// The API for downloading content is: /api/node/content{property}/{store_type}/{store_id}/{id}
 		// example:
 		// http://localhost:8080/alfresco/service/api/node/content/workspace/SpacesStore/6ce33a7b-6c99-4dbb-978f-4ae3c6af6421
 		JSONObject jsonResult = new JSONObject();
 		String store_type = "";
 		String store_id = "";
 		try {
 			JSONArray jsonAspects = new JSONArray();
 			for (QName aspect: aspects) {
 				jsonAspects.put(this.dictionaryServiceExtras.getSimpleQName(aspect).toString());
 			}
 			JSONArray jsonProps = new JSONArray();
 			for (Map.Entry<QName, Serializable> prop: props.entrySet()) {
 				JSONObject jsonProperty = new JSONObject();
 				SimpleQName simpleQname = this.dictionaryServiceExtras.getSimpleQName(prop.getKey());
 				if (simpleQname.getPrefix().equalsIgnoreCase("sys")) {
 					if (simpleQname.getShortName().equalsIgnoreCase("store-identifier")) {
 						store_id=prop.getValue().toString();
 					}
 					else if (simpleQname.getShortName().equalsIgnoreCase("store-protocol")) {
 						store_type=prop.getValue().toString();
 					}
 				}
 				jsonProperty.put(simpleQname.toString(), prop.getValue());
 				jsonProps.put(jsonProperty);
 			}
 			JSONArray jsonChildren = new JSONArray();
 			for (ChildAssociationRef child: children) {
 				JSONObject jsonProperty = new JSONObject();
 				jsonProperty.put(this.dictionaryServiceExtras.getSimpleQName(child.getTypeQName()).toString(), child.getChildRef().getId());	
 				jsonChildren.put(jsonProperty);
 			}
 			
 			jsonResult.put("nodeId", nodeId);
 			jsonResult.put("type", this.dictionaryServiceExtras.getSimpleQName(nodeType));
 			jsonResult.put("aspects", jsonAspects);
 			jsonResult.put("properties", jsonProps);
 			jsonResult.put("children", jsonChildren);
 			jsonResult.put("content_url", req.getServiceContextPath()+"/api/node/content/"+store_type+"/"+store_id+"/"+nodeId);
 			String jsonString = jsonResult.toString(4);
 	    	res.getWriter().write(jsonString);
 		}
 
 		catch (JSONException e) {
 			throw new WebScriptException("Unable to serialize JSON");
 		}	
 	}
 
 }
