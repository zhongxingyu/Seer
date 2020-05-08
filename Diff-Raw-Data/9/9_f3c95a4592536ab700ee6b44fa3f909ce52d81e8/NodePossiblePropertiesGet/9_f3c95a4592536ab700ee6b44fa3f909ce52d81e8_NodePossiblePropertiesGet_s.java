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
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import org.alfresco.service.cmr.dictionary.ClassDefinition;
 import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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
 
 public class NodePossiblePropertiesGet extends AbstractWebScript {
 
 
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

 		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
 		String nodeId = templateArgs.get("nodeId");
 		if (nodeId == null) 
 			throw new WebScriptException("No nodeId provided");
 		
 		NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
 		if (! this.nodeService.exists(nodeRef)) 
 			throw new WebScriptException("No such node: " + nodeId);
 		
 		QName nodeType = this.nodeService.getType(nodeRef);
 		Set<QName> aspects = this.nodeService.getAspects(nodeRef);
 		Collection<PropertyDefinition> props = new ArrayList<PropertyDefinition>();
 		ClassDefinition nodeTypeDefiniton = this.dictionaryServiceExtras.getClassDefinition(nodeType);
 		props.addAll(this.dictionaryServiceExtras.getPropertyDefinitions(nodeTypeDefiniton));
 		
 		for (QName aspect: aspects) {
 			nodeTypeDefiniton = this.dictionaryServiceExtras.getClassDefinition(aspect);
 			props.addAll(this.dictionaryServiceExtras.getPropertyDefinitions(nodeTypeDefiniton));
 		}
 	
 		JSONObject jsonResult = new JSONObject();
 		
 		try {
 			JSONArray jsonAspects = new JSONArray();
 			for (QName aspect: aspects) {
 				jsonAspects.put(this.dictionaryServiceExtras.getSimpleQName(aspect).toString());
 			}
 			JSONArray jsonProps = new JSONArray();
 			for (PropertyDefinition prop: props) {
 				JSONObject jsonProperty = new JSONObject();
 				jsonProperty.put("name", this.dictionaryServiceExtras.getSimpleQName(prop.getName()).toString());
 				jsonProperty.put("value", prop.getDataType());
 				jsonProperty.put("defaultValue", prop.getDefaultValue());
 				jsonProperty.put("isMandatory", prop.isMandatory());
 				jsonProps.put(jsonProperty);
 			}
 			jsonResult.put("nodeId", nodeId);
 			jsonResult.put("type", this.dictionaryServiceExtras.getSimpleQName(nodeType).toString());
 			jsonResult.put("aspects", jsonAspects);
 			jsonResult.put("properties", jsonProps);
 			String jsonString = jsonResult.toString(4);
 	    	res.getWriter().write(jsonString);
 		}
 
 		catch (JSONException e) {
 			throw new WebScriptException("Unable to serialize JSON");
 		}	
 	}
 
 }
