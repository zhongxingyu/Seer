 /**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements. See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.googlecode.wicket.jquery.ui.plugins.whiteboard;
 
 import com.googlecode.wicket.jquery.ui.plugins.whiteboard.elements.*;
 import com.googlecode.wicket.jquery.ui.plugins.whiteboard.resource.GoogStyleSheetResourceReference;
 import com.googlecode.wicket.jquery.ui.plugins.whiteboard.resource.WhiteboardJavaScriptResourceReference;
 import com.googlecode.wicket.jquery.ui.plugins.whiteboard.resource.WhiteboardStyleSheetResourceReference;
 import com.googlecode.wicket.jquery.ui.plugins.whiteboard.settings.IWhiteboardLibrarySettings;
 import org.apache.wicket.Application;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.json.JSONArray;
 import org.apache.wicket.ajax.json.JSONException;
 import org.apache.wicket.ajax.json.JSONObject;
 import org.apache.wicket.markup.head.*;
 import org.apache.wicket.protocol.ws.IWebSocketSettings;
 import org.apache.wicket.protocol.ws.api.IWebSocketConnection;
 import org.apache.wicket.protocol.ws.api.IWebSocketConnectionRegistry;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.http.WebRequest;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class WhiteboardBehavior extends AbstractDefaultAjaxBehavior{
 
 	private String whiteboardId;
 	private static HashMap<Integer,Element> elementMap=new HashMap<Integer,Element>();
 
	private ArrayDeque<ArrayList> undoSnapshots=new ArrayDeque<ArrayList>(20);
	private ArrayDeque<ArrayList> undoSnapshotCreationList=new ArrayDeque<ArrayList>(20);
 
 	private ArrayList<Element> snapShot=null;
 	private ArrayList<Boolean> snapShotCreation=null;
 
 	public WhiteboardBehavior(String whiteboardId){
 		super();
 		this.whiteboardId=whiteboardId;
 	}
 
 	protected void respond(final AjaxRequestTarget target){
 
 		RequestCycle cycle = RequestCycle.get();
 		WebRequest webRequest = (WebRequest) cycle.getRequest();
 
 		if(webRequest.getQueryParameters().getParameterValue("editedElement").toString()!=null){
 			String editedElement = webRequest.getQueryParameters().getParameterValue("editedElement").toString();
 
 			try{
 				//Mapping JSON String to Objects and Adding to the Element List
 				JSONObject jsonEditedElement=new JSONObject(editedElement);
 
 				String elementType=(String)jsonEditedElement.get("type");
 
 				Element element=null;
 
 				if(snapShot==null&&snapShotCreation==null){
 					snapShot=new ArrayList<Element>();
 					snapShotCreation=new ArrayList<Boolean>();
 				}
 
 				if("PointFree".equals(elementType)){
 					element=new PointFree(jsonEditedElement);
 				}else if("PencilCurve".equals(elementType)){
 					element=new PencilCurve(jsonEditedElement);
 				}else if("PencilFreeLine".equals(elementType)){
 					element=new PencilFreeLine(jsonEditedElement);
 				}else if("PencilRect".equals(elementType)){
 					element=new PencilRect(jsonEditedElement);
 				}else if("PencilPointAtRect".equals(elementType)){
 					element=new PencilPointAtRect(jsonEditedElement);
 				}else if("PencilCircle".equals(elementType)){
 					element=new PencilCircle(jsonEditedElement);
 				}else if("Text".equals(elementType)){
 					element=new Text(jsonEditedElement);
 				}else if("PointAtLine".equals(elementType)){
 					element=new PointAtLine(jsonEditedElement);
 				}else if("PointAtCircle".equals(elementType)){
 					element=new PointAtCircle(jsonEditedElement);
 				}else if("Point_2l".equals(elementType)){
 					element=new Point_2l(jsonEditedElement);
 				}else if("Point_2c".equals(elementType)){
 					element=new Point_2c(jsonEditedElement);
 				}else if("Point_lc".equals(elementType)){
 					element=new Point_lc(jsonEditedElement);
 				}else if("LineGeneral".equals(elementType)){
 					element=new LineGeneral(jsonEditedElement);
 				}else if("Line_2p".equals(elementType)){
 					element=new Line_2p(jsonEditedElement);
 				}else if("Segment".equals(elementType)){
 					element=new Segment(jsonEditedElement);
 				}else if("CircleGeneral".equals(elementType)){
 					element=new CircleGeneral(jsonEditedElement);
 				}else if("Circle_3p".equals(elementType)){
 					element=new Circle_3p(jsonEditedElement);
 				}
 
 				if(elementMap.containsKey(element.getId())&&!elementMap.isEmpty()){
 					snapShot.add(elementMap.get(element.getId()));
 					snapShotCreation.add(false);
 				}
 				else{
 					snapShot.add(element);
 					snapShotCreation.add(true);
 				}
 
 				if(!"PointFree".equals(element.getType())){
 					if(undoSnapshots.size()==20){
 						undoSnapshots.pollFirst();
 						undoSnapshotCreationList.pollFirst();
 					}
 					undoSnapshots.addLast(snapShot);
 					undoSnapshotCreationList.addLast(snapShotCreation);
 
 					snapShot=null;
 					snapShotCreation=null;
 				}
 
 				// Synchronizing newly added element between whiteboards
 				if(element!=null){
 					elementMap.put(element.getId(),element);
 
 					IWebSocketConnectionRegistry reg = IWebSocketSettings.Holder.get(Application.get()).getConnectionRegistry();
 					for (IWebSocketConnection c : reg.getConnections(Application.get())) {
 						try {
 							JSONObject jsonObject=new JSONObject(editedElement);
 							c.sendMessage(getAddElementMessage(jsonObject).toString());
 						} catch(Exception e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 			}catch(JSONException e){
 				e.printStackTrace();
 			}
 		}
 		else if(webRequest.getQueryParameters().getParameterValue("undo").toString()!=null){
 			if(!undoSnapshots.isEmpty()){
 				ArrayList<Boolean> undoCreationList=undoSnapshotCreationList.pollLast();
 				ArrayList<Element> undoElement=undoSnapshots.pollLast();
 
 				String deleteList="";
 				JSONArray changeList=new JSONArray();
 
 				IWebSocketConnectionRegistry reg = IWebSocketSettings.Holder.get(Application.get()).getConnectionRegistry();
 
 				for(int i=0;i<undoElement.size();i++){
 					if(undoCreationList.get(i)){
 						elementMap.remove(undoElement.get(i).getId());
 						if("".equals(deleteList)){
 							deleteList= ""+undoElement.get(i).getId();
 						}
 						else{
 							deleteList+= ","+undoElement.get(i).getId();
 						}
 					}
 					else{
 						elementMap.put(undoElement.get(i).getId(),undoElement.get(i));
 						changeList.put(undoElement.get(i).getJSON());
 					}
 				}
 
 				for (IWebSocketConnection c : reg.getConnections(Application.get())) {
 					try {
 						c.sendMessage(getUndoMessage(changeList,deleteList).toString());
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 
 		}
 		else if(webRequest.getQueryParameters().getParameterValue("eraseAll").toString()!=null){
 			elementMap.clear();
 			IWebSocketConnectionRegistry reg = IWebSocketSettings.Holder.get(Application.get()).getConnectionRegistry();
 			for (IWebSocketConnection c : reg.getConnections(Application.get())) {
 				try {
 					JSONArray jsonArray=new JSONArray();
 					c.sendMessage(getWhiteboardMessage(jsonArray).toString());
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private JSONObject getAddElementMessage(JSONObject element) throws JSONException {
 		return new JSONObject()
 				.put("type", "addElement")
 				.put("json", element);
 	}
 
 	private JSONObject getDeleteElementMessage(int element) throws JSONException {
 		return new JSONObject()
 				.put("type", "deleteElement")
 				.put("elementID", element);
 	}
 
 	private JSONObject getUndoMessage(JSONArray changeList,String deleteList) throws JSONException {
 		return new JSONObject()
 				.put("type", "undoList")
 				.put("changeList", changeList)
 		        .put("deleteList", deleteList);
 	}
 
 	private JSONObject getWhiteboardMessage(JSONArray array) throws JSONException {
 		return new JSONObject()
 				.put("type", "parseWB")
 				.put("json", array);
 	}
 
 	public void renderHead(Component component, IHeaderResponse response) {
 		super.renderHead(component,response);
 		initReferences(response);
 		String callbackUrl=getCallbackUrl().toString();
 		String whiteboardInitializeScript="" +
 				"callbackUrl='"+callbackUrl+"';" +
 				"whiteboard = bay.whiteboard.Create();\n" +
 				"elementCollection=whiteboard.getMainCollection();"+
 				"whiteboard.getMainCollection().onChange = function(element){\n"+
 				"changedElement=this.getJson(element);\n"+
 				"Wicket.Ajax.get({u:'"+callbackUrl+"',ep:{editedElement:changedElement}});\n};\n"+
 				"whiteboard.render(document.getElementById('"+whiteboardId+"'));";
 
 		//Clearing the whiteboard for first client
 		IWebSocketConnectionRegistry reg = IWebSocketSettings.Holder.get(Application.get()).getConnectionRegistry();
 		if(reg.getConnections(Application.get()).size()==0){
 			elementMap.clear();
 		}
 
 		//Loading existing content for clients join after first one
 		if(!elementMap.isEmpty()){
 			JSONArray jsonArray=new JSONArray();
 			for (Element e : elementMap.values()) {
 				jsonArray.put(e.getJSON());
 			}
 			whiteboardInitializeScript+="elementCollection.parseJson('"+jsonArray.toString()+"');";
 		}
 
 		response.render(OnDomReadyHeaderItem.forScript(whiteboardInitializeScript));
 	}
 
 
 	private void initReferences(IHeaderResponse response){
 		IWhiteboardLibrarySettings settings = getLibrarySettings();
 
 //Whiteboard.css
 		if (settings != null && settings.getWhiteboardStyleSheetReference() != null)
 		{
 			response.render(new PriorityHeaderItem(CssHeaderItem.forReference(settings.getWhiteboardStyleSheetReference())));
 		}
 		else
 		{
 			response.render(new PriorityHeaderItem(CssHeaderItem.forReference(WhiteboardStyleSheetResourceReference.get())));
 		}
 
 //Goog.css
 		if (settings != null && settings.getGoogStyleSheetReference() != null)
 		{
 			response.render(new PriorityHeaderItem(CssHeaderItem.forReference(settings.getGoogStyleSheetReference())));
 		}
 		else
 		{
 			response.render(new PriorityHeaderItem(CssHeaderItem.forReference(GoogStyleSheetResourceReference.get())));
 		}
 
 
 //Whiteboard.js
 		if (settings != null && settings.getWhiteboardJavaScriptReference() != null)
 		{
 			response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(settings.getWhiteboardJavaScriptReference())));
 		}
 		else
 		{
 			response.render(new PriorityHeaderItem(JavaScriptHeaderItem.forReference(WhiteboardJavaScriptResourceReference.get())));
 		}
 
 	}
 
 	private static IWhiteboardLibrarySettings getLibrarySettings()
 	{
 		if (Application.exists() && (Application.get().getJavaScriptLibrarySettings() instanceof IWhiteboardLibrarySettings))
 		{
 			return (IWhiteboardLibrarySettings) Application.get().getJavaScriptLibrarySettings();
 		}
 
 		return null;
 	}
 
 	public HashMap<Integer,Element> getElementMap(){
 		return elementMap;
 	}
 
 	public void setElementMap(HashMap<Integer,Element> elementMap){
 		this.elementMap=elementMap;
 	}
 
 	public void undo(){
 
 	}
 
 }
