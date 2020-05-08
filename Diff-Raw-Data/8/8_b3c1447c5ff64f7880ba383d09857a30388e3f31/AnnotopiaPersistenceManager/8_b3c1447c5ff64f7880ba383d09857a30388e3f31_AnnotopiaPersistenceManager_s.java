 /*
  * Copyright 2014 Massachusetts General Hospital
  * 
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.src;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.model.persistence.AnnotationPersistenceManager;
 import org.mindinformatics.gwt.domeo.plugins.annotation.persistence.src.APersistenceManager;
 import org.mindinformatics.gwt.domeo.plugins.annotation.persistence.src.IPersistenceManager;
 import org.mindinformatics.gwt.domeo.plugins.annotation.persistence.src.IRetrieveExistingAnnotationSetHandler;
 import org.mindinformatics.gwt.domeo.plugins.annotation.persistence.src.IRetrieveExistingAnnotationSetListHandler;
 import org.mindinformatics.gwt.domeo.plugins.annotation.persistence.src.IRetrieveExistingBibliographySetHandler;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.model.JsAnnotopiaAnnotationSetGraph;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.model.JsAnnotopiaSetsResultWrapper;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.model.MAnnotopiaAnnotationSet;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.serializers.AnnotopiaSerializerManager;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.ui.existingsets.ExistingAnnotationViewerPanel;
 import org.mindinformatics.gwt.framework.component.ui.glass.EnhancedGlassPanel;
 import org.mindinformatics.gwt.framework.src.ICommandCompleted;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.query.client.Function;
 import com.google.gwt.query.client.GQuery;
 import com.google.gwt.query.client.plugins.ajax.Ajax;
 import com.google.gwt.user.client.Window;
 
 /**
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class AnnotopiaPersistenceManager extends APersistenceManager implements IPersistenceManager {
 
 	public String URL = "http://127.0.0.1:8090/";
 	
 	public AnnotopiaPersistenceManager(IDomeo domeo, String url, ICommandCompleted callback) {
 		super(domeo, callback);
 		if(url!=null) URL = url;
 	}
 	
 	public static native JavaScriptObject parseJson(String jsonStr) /*-{
 		try {
 			var jsonStr = jsonStr      
 	    		.replace(/[\\]/g, '\\\\')
 	    		.replace(/[\/]/g, '\\/')
 	    		.replace(/[\b]/g, '\\b')
 	    		.replace(/[\f]/g, '\\f')
 	    		.replace(/[\n]/g, '\\n')
 	    		.replace(/[\r]/g, '\\r')
 	    		.replace(/[\t]/g, '\\t')
 	    		.replace(/[\\][\"]/g, '\\\\\"')
 	    		.replace(/\\'/g, "\\'");
 	    	//alert(jsonStr);
 		  	return JSON.parse(jsonStr);
 		} catch (e) {
 			alert("Error while parsing the JSON message: " + e);
 		}
 	}-*/;
 
 	@Override
 	public void retrieveExistingAnnotationSetList(IRetrieveExistingAnnotationSetListHandler handler) {
 		_application.getLogger().debug(this, "Retrieving list of existing annotation sets...");
 		_application.getProgressPanelContainer().setProgressMessage("Retrieving list of existing annotation sets from Annotopia...");
 		
 		try {
 			Ajax.ajax(Ajax.createSettings()
 				.setUrl(URL+"s/annotationset")
 		        .setDataType("json") // txt, json, jsonp, xml
 		        .setType("get")      // post, get
 		        .setData(GQuery.$$("apiKey: testkey,outCmd:frame,tgtUrl:"+((IDomeo)_application).getPersistenceManager().getCurrentResource().getUrl())) // parameters for the query-string
 		        .setTimeout(10000)
 		        .setSuccess(new Function(){ // callback to be run if the request success
 		    		public void f() {
 		    			IDomeo _domeo = ((IDomeo)_application);
 		    			JsAnnotopiaSetsResultWrapper wrapper = 
 		    				(JsAnnotopiaSetsResultWrapper) parseJson(getDataProperties().toJsonString());
 		    			AnnotopiaConverter unmarshaller = new AnnotopiaConverter(_domeo);
 		    			List<MAnnotopiaAnnotationSet> sets = unmarshaller.unmarshallAnnotationSetsList(wrapper);	    			
 		    			_application.getLogger().debug(this, "Completed Execution of retrieveExistingAnnotationSetList() in " + (System.currentTimeMillis()-((IDomeo)_application).getDocumentPipelineTimer())+ "ms");
 
 		    			if(sets.size()==0) {
 		    				// TODO message no annotation found
 		    				_application.getLogger().info(this, "No annotation sets found");
 		    				_application.getProgressPanelContainer().setCompletionMessage("No annotation exist for this document");
 		    			} else {
 		    				_application.getProgressPanelContainer().hide();
 		    				try {
 		    					ExistingAnnotationViewerPanel lwp = new ExistingAnnotationViewerPanel((IDomeo)_application, sets);
 		    					new EnhancedGlassPanel((IDomeo)_application, lwp, lwp.getTitle(), false, false, false);
 		    				} catch (Exception e) {
 		    					_application.getLogger().exception(this, "Exeption in visualizing existing annotation");
 		    				}		
 		    			}
 		    		}
 		        })
 		        .setError(new Function(){ // callback to be run if the request fails
 		        	public void f() {
 		        		_application.getLogger().exception(this, 
 		        			"Couldn't complete existing annotation sets list retrieval process");
 		        		_application.getProgressPanelContainer().setErrorMessage(
 							"Couldn't complete existing annotation sets list retrieval process");
 		        	}
 		        })
 		     );
 		} catch (Exception e) {
 			_application.getLogger().exception(this, "Couldn't complete existing annotation sets list retireval");
 		}
 	}
 	
 	@Override
 	public void retrieveExistingAnnotationSets(List<String> urls, IRetrieveExistingAnnotationSetHandler handler) {
 		_application.getLogger().debug(this, "Retrieving requested annotation sets...");
 		_application.getProgressPanelContainer().setProgressMessage("Retrieving requested annotation sets from Annotopia...");
 		
 		for(String url: urls) {
 			try {
 				Ajax.ajax(Ajax.createSettings()
 					.setUrl(url)
 			        .setDataType("json") // txt, json, jsonp, xml
 			        .setType("get")      // post, get
 			        .setData(GQuery.$$("apiKey: testkey,outCmd:frame")) // parameters for the query-string
 			        .setTimeout(10000)
 			        .setSuccess(new Function(){ // callback to be run if the request success
 			    		public void f() {
 			    			IDomeo _domeo = ((IDomeo)_application);
 			    			JsAnnotopiaAnnotationSetGraph wrapper = 
 			    				(JsAnnotopiaAnnotationSetGraph) parseJson(getDataProperties().toJsonString());
 			    			AnnotopiaConverter unmarshaller = new AnnotopiaConverter(_domeo);
 			    			
 			    			MAnnotationSet set = unmarshaller.unmarshallAnnotationSet(wrapper);	    			
 			    			if(set==null) {
 			    				// TODO message no annotation found
 			    				_application.getLogger().info(this, "No annotation set found");
 			    				_application.getProgressPanelContainer().setCompletionMessage("Annotation Set not found");
 			    			} else {	
 			    				((AnnotationPersistenceManager) _domeo.getPersistenceManager()).loadAnnotationSet(set);
 			    				_application.getProgressPanelContainer().hide();
 			    				_application.getLogger().debug(this, "Completed Execution of retrieveExistingAnnotationSets() in " + (System.currentTimeMillis()-((IDomeo)_application).getDocumentPipelineTimer())+ "ms");
 			    				_domeo.refreshAllComponents();
 			    			}
 			    		}
 			        })
 			        .setError(new Function(){ // callback to be run if the request fails
 			        	public void f() {
 			        		_application.getLogger().exception(this, 
 			        			"Couldn't complete existing annotation sets list retrieval process");
 			        		_application.getProgressPanelContainer().setErrorMessage(
 								"Couldn't complete existing annotation sets list retrieval process");
 			        	}
 			        })
 			     );
 			} catch (Exception e) {
 				_application.getLogger().exception(this, "Couldn't complete existing annotation sets list retireval");
 			}
 		}
 	}
 	
 	@Override
 	public void saveAnnotation() {
 		_application.getLogger().debug(this, "Saving modified annotation sets...");
 		_application.getProgressPanelContainer().setProgressMessage("Saving modified annotation sets to Annotopia...");
 		
 		ArrayList<MAnnotationSet> setToSerialize = new ArrayList<MAnnotationSet>();
 		for(MAnnotationSet set: ((IDomeo)_application).getAnnotationPersistenceManager().getAllDiscussionSets()) {
 			if(set.getHasChanged() && set.getAnnotations().size()>0) 
 				setToSerialize.add(set);
 		}
 		for(MAnnotationSet set: ((IDomeo)_application).getAnnotationPersistenceManager().getAllUserSets()) {
 			if(set.getHasChanged() && set.getAnnotations().size()>0) 
 				setToSerialize.add(set);
 		}
 		
 		AnnotopiaSerializerManager manager = AnnotopiaSerializerManager.getInstance((IDomeo)_application);
 		for(MAnnotationSet annotationSet: setToSerialize) {
 			String value = manager.serialize(annotationSet).toString();
 			Window.alert(value);
 			try {
 				Ajax.ajax(Ajax.createSettings()
 					.setUrl(URL+"s/annotationset")
			        .setDataType("jsonp") // txt, json, jsonp, xml */
 			        .setType("post")      // post, get
 			        .setData(GQuery.$$("apiKey: testkey, set: " + value)) // parameters for the query-string
 			        .setTimeout(10000)
 			        .setSuccess(new Function(){ // callback to be run if the request success
 			    		public void f() {
 			    			Window.alert("ended");
 			    			/*
 			    			IDomeo _domeo = ((IDomeo)_application);
 			    			JsAnnotopiaAnnotationSetGraph wrapper = 
 			    				(JsAnnotopiaAnnotationSetGraph) parseJson(getDataProperties().toJsonString());
 			    			AnnotopiaConverter unmarshaller = new AnnotopiaConverter(_domeo);
 			    			
 			    			MAnnotationSet set = unmarshaller.unmarshallAnnotationSet(wrapper);	    			
 			    			if(set==null) {
 			    				// TODO message no annotation found
 			    				_application.getLogger().info(this, "No annotation set found");
 			    				_application.getProgressPanelContainer().setCompletionMessage("Annotation Set not found");
 			    			} else {	
 			    				((AnnotationPersistenceManager) _domeo.getPersistenceManager()).loadAnnotationSet(set);
 			    				_application.getProgressPanelContainer().hide();
 			    				_application.getLogger().debug(this, "Completed Execution of retrieveExistingAnnotationSets() in " + (System.currentTimeMillis()-((IDomeo)_application).getDocumentPipelineTimer())+ "ms");
 			    				_domeo.refreshAllComponents();
 			    			}
 			    			*/
 			    		}
 			        })
 			        .setError(new Function(){ // callback to be run if the request fails
 			        	public void f() {
 			        		 Window.alert("There was an error" + getDataObject());
 
 			        		_application.getLogger().exception(this, 
 			        			"Couldn't complete existing annotation sets list saving");
 			        		_application.getProgressPanelContainer().setErrorMessage(
 								"Couldn't complete existing annotation sets list saving");
 			        	}
 			        })
 			     );
 			} catch (Exception e) {
 				_application.getLogger().exception(this, "Couldn't complete existing annotation sets list saving");
 			}
 		}
 	}
 
 	@Override
 	public void retrieveExistingBibliographySet(IRetrieveExistingBibliographySetHandler handler) {
 		// TODO Auto-generated method stub
 	}
 	
 	@Override
 	public void saveBibliography() {
 		// TODO Auto-generated method stub		
 	}
 }
