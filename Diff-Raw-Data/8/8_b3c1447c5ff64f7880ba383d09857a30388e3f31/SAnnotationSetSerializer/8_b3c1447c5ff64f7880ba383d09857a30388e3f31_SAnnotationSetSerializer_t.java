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
 package org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.serializers;
 
 import java.util.List;
 
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 import org.mindinformatics.gwt.domeo.model.MAnnotationSet;
 import org.mindinformatics.gwt.domeo.model.persistence.ontologies.IDomeoOntology;
 import org.mindinformatics.gwt.domeo.model.persistence.ontologies.IPavOntology;
 import org.mindinformatics.gwt.domeo.model.persistence.ontologies.IRdfsOntology;
 import org.mindinformatics.gwt.domeo.plugins.persistence.annotopia.model.IAnnotopia;
 
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 
 
 /**
  * This class serializes the Annotation Set to Annotopia JSON format.
  * 
  * @author Paolo Ciccarese <paolo.ciccarese@gmail.com>
  */
 public class SAnnotationSetSerializer extends AAnnotopiaSerializer implements IAnnotopiaSerializer {
 
 	@Override
 	public JSONObject serialize(AnnotopiaSerializerManager manager, Object obj) {
 		MAnnotationSet annotationSet = (MAnnotationSet) obj;
 		JSONObject annotationSetJson = new JSONObject();
 		annotationSetJson.put(IRdfsOntology.type, new JSONString(IAnnotopia.ANNOTATION_SET));
 		
 		// These have to exist and defined
 		// TODO HIGH track exception when any of these is null or blank
		annotationSetJson.put("@context",new JSONString("https://raw2.github.com/Annotopia/AtSmartStorage/master/web-app/data/AnnotopiaContext.json"));
 		annotationSetJson.put(IRdfsOntology.id, new JSONString(annotationSet.getIndividualUri()));
 		annotationSetJson.put(IDomeoOntology.transientLocalId, new JSONString(Long.toString(annotationSet.getLocalId())));
 		
 		//annotationSetJson.put(IDomeoOntology.annotates, new JSONString(annotationSet.getTargetResource().getUrl()));
 		//manager.addResourceToSerialize(annotationSet.getTargetResource());
 		
 		//  Creation
 		// --------------------------------------------------------------------
 		if(annotationSet.getCreatedBy()!=null) {
 			//manager.addAgentToSerialize(annotationSet.getCreatedBy());
 			annotationSetJson.put("createdBy", manager.serialize(annotationSet.getCreatedBy()));
 			//annotationSetJson.put(IPavOntology.createdBy, serializeAgent(manager, annotationSet.getCreatedBy()));
 		} else {
 			// Warning/Exception?
 		}
 
 		if(annotationSet.getCreatedOn()!=null) {
 			annotationSetJson.put("createdAt", new JSONString(dateFormatter.format(annotationSet.getCreatedOn())));
 		} else {
 			// Warning/Exception?
 		}
 		if(annotationSet.getCreatedWith()!=null) {
 			//manager.addAgentToSerialize(annotationSet.getCreatedWith());
 			annotationSetJson.put("createdWith", manager.serialize(annotationSet.getCreatedWith()));
 			//annotationSetJson.put(IPavOntology.createdWith, serializeAgent(manager, annotationSet.getCreatedWith()));
 		} else {
 			// Warning/Exception?
 		}
 
 		if(annotationSet.getLastSavedOn()!=null) {
 			annotationSetJson.put("lastSavedOn", new JSONString(dateFormatter.format(annotationSet.getLastSavedOn())));
 		}
 		
 		//  Imports
 		// --------------------------------------------------------------------
 		if(annotationSet.getImportedFrom()!=null) {
 			//manager.addAgentToSerialize(annotationSet.getImportedFrom());
 			annotationSetJson.put(IPavOntology.importedFrom, new JSONString(annotationSet.getImportedFrom().getUri()));
 			//annotationSetJson.put(IPavOntology.importedFrom, serializeAgent(manager, annotationSet.getImportedFrom()));
 		}
 		if(annotationSet.getImportedBy()!=null) {
 			//manager.addAgentToSerialize(annotationSet.getImportedBy());
 			annotationSetJson.put(IPavOntology.importedBy, new JSONString(annotationSet.getImportedBy().getUri()));
 			//annotationSetJson.put(IPavOntology.importedBy, serializeAgent(manager, annotationSet.getImportedBy()));
 		}
 		if(annotationSet.getImportedOn()!=null) {
 			annotationSetJson.put(IPavOntology.importedOn, new JSONString(dateFormatter.format(annotationSet.getImportedOn())));
 		}
 		// These translate null values into blank strings
 		annotationSetJson.put("label", nullable(annotationSet.getLabel()));
 		annotationSetJson.put("description", nullable(annotationSet.getDescription()));
 		annotationSetJson.put("lineageUri", nullable(annotationSet.getLineageUri()));
 		annotationSetJson.put("versionNumber", nullable(annotationSet.getVersionNumber()));
 		annotationSetJson.put("previousVersion", nullable(annotationSet.getPreviousVersion()));
 		annotationSetJson.put("deleted", nullableBoolean(annotationSet.getIsDeleted()));
 		
 		// Serialization of the annotation items that have changed
 		JSONArray annotations = new JSONArray();
 		List<MAnnotation> annotationsList = annotationSet.getAnnotations();
 		for(int i=0; i<annotationsList.size(); i++) {
 			if(annotationsList.get(i).getHasChanged())
 				annotations.set(i, manager.serialize(annotationsList.get(i)));
 		}
 		annotationSetJson.put("annotations", annotations);
 		
 		return annotationSetJson;
 	}
 }
