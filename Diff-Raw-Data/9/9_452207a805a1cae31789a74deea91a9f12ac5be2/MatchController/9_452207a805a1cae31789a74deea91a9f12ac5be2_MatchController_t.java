 /*******************************************************************************
  * Copyright 2012-2013 University of Trento - Department of Information
  * Engineering and Computer Science (DISI)
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  *
  * http://www.gnu.org/licenses/lgpl-2.1.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  ******************************************************************************/ 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.unitn.disi.smatch.webapi.server.controllers;
 
 import it.unitn.disi.common.components.ConfigurableException;
 import it.unitn.disi.smatch.MatchManager;
 import it.unitn.disi.smatch.data.trees.Context;
 import it.unitn.disi.smatch.data.trees.IContext;
 import it.unitn.disi.smatch.data.trees.INode;
 import it.unitn.disi.smatch.data.mappings.IContextMapping;
 import it.unitn.disi.smatch.data.mappings.IMappingElement;
import it.unitn.disi.smatch.webapi.server.utils.Configuration;
 import it.unitn.disi.smatch.webapi.server.utils.RequestTimer;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONObject;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  * A controller to execute the match operation. It is a wrapper around S-Match 
  * that reads input contexts and writes output correspondence in JSON format.
  * 
  * @author Moaz Reyad <reyad@disi.unitn.it>
  * @date Jul 11, 2013
  */
 @Controller
 @RequestMapping()
 public class MatchController extends AbstractController {
 
     public MatchController() {
         addMethodName("/match", "post");
     }
 
     @ResponseBody
     @RequestMapping(value = "/match", method = RequestMethod.POST)
     public 
     JSONObject match(@RequestBody JSONObject requestObject)
             throws JSONException, IOException, ConfigurableException, URISyntaxException {
        
         JSONObject jRequest = getJSONFromRequest(requestObject);
         JSONObject jParams = getJSONParameters(jRequest);
 
         JSONArray contextArray = jParams.getJSONArray("Contexts");
 
         JSONObject jSourceContext = (JSONObject) contextArray.get(0);
         jSourceContext = (JSONObject) jSourceContext.get("SourceContext");
         JSONObject jTargetContext = (JSONObject) contextArray.get(1);
         jTargetContext = (JSONObject) jTargetContext.get("TargetContext");
 
         IContext ctxSource = createContext(jSourceContext);
         IContext ctxTarget = createContext(jTargetContext);
 
        String propertiesFile = Configuration.getString("smatch.webapi.matching.properties-file");

        IContextMapping<INode> mapping = MatchManager.simpleMatch(ctxSource, ctxTarget, propertiesFile);
 
         JSONObject jMapping = renderMappingJson(mapping);
 
         return getJSONHelper().createResponse(jMapping, RequestTimer.getTime());
     }
 
     private IContext createContext(JSONObject json) {
         IContext ctx = new Context();
 
         JSONArray array = json.names();
         String root = null;
         try {
             root = array.getString(0);
         } catch (JSONException ex) {
             Logger.getLogger(MatchController.class.getName()).log(Level.SEVERE, null, ex);
         }
         INode rootNode = ctx.createRoot(root);
         try {
             JSONArray children = json.getJSONArray(root);
             for (int i = 0; i < children.length(); i++) {
                 String child = children.getString(i);
                 rootNode.createChild(child);
             }
 
         } catch (JSONException ex) {
             Logger.getLogger(MatchController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return ctx;
     }
 
     private JSONObject renderMappingJson(IContextMapping<INode> mapping) {
 
         JSONArray jMappingList = new JSONArray();
 
         for (IMappingElement<INode> mappingElement : mapping) {
 
             JSONObject jMappingItem = new JSONObject();
             String sourceConceptName = getPathToRoot(mappingElement.getSource());
             String targetConceptName = getPathToRoot(mappingElement.getTarget());
             char relation = mappingElement.getRelation();
 
             try {
                 jMappingItem.put("Source", sourceConceptName);
                 jMappingItem.put("Target", targetConceptName);
                 jMappingItem.put("Relation", String.valueOf(relation));
 
                 jMappingList.put(jMappingItem);
             } catch (JSONException ex) {
                 Logger.getLogger(MatchController.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         }
 
         JSONObject jCorrespondace = new JSONObject();
 
         try {
             jCorrespondace.put("Correspondence", jMappingList);
         } catch (JSONException ex) {
             Logger.getLogger(MatchController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return jCorrespondace;
     }
 
     private String getPathToRoot(INode node) {
         StringBuilder result = new StringBuilder();
         ArrayList<String> path = new ArrayList<String>();
         INode curNode = node;
         while (null != curNode) {
             path.add(0, curNode.getNodeData().getName());
             curNode = curNode.getParent();
         }
         for (int i = 0; i < path.size(); i++) {
             if (0 == i) {
                 result.append(path.get(i));
             } else {
                 result.append("\\").append(path.get(i));
             }
         }
         return result.toString();
     }
 }
