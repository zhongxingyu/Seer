 /*
  * Copyright 2012, TopicQuests
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  */
 package org.topicquests.solr;
 
 import org.apache.log4j.Logger;
 import java.util.*;
 
 import org.topicquests.common.api.IResult;
 import org.topicquests.common.ResultPojo;
 import org.topicquests.model.api.IMergeImplementation;
 import org.topicquests.model.api.INodeModel;
 import org.topicquests.model.api.INode;
 import org.topicquests.model.api.ITuple;
 import org.topicquests.common.api.ITopicQuestsOntology;
 import org.topicquests.solr.api.ISolrDataProvider;
 import org.topicquests.util.LoggingPlatform;
 import org.topicquests.model.Node;
 
 import org.apache.solr.schema.DateField;
 
 /**
  * @author park
  *
  */
 public class SolrNodeModel implements INodeModel {
 	private LoggingPlatform log = LoggingPlatform.getInstance();
 	private ISolrDataProvider database;
 	private DateField dateField;
 	private IMergeImplementation merger;
 
 	/**
 	 * 
 	 */
 	public SolrNodeModel(ISolrDataProvider p, IMergeImplementation m) {
 		database = p;
 		merger = m;
		//not all instances will include merge capabilities
		if (merger != null)
			merger.setNodeModel(this);
 		dateField = new DateField();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newNode(String locator, String label, String description,
 			String lang, String userId, String smallImagePath, String largeImagePath, boolean isPrivate) {
 		INode n = new Node();
 		System.out.println("SolrNodeModel.newNode- "+locator);
 		IResult result = new ResultPojo();
 		result.setResultObject(n);
 		n.setLocator(locator);
 		n.setCreatorId(userId);
 		Date d = new Date();
 		n.setDate(d); 
 		n.setLastEditDate(d);
 		if (label != null)
 			n.addLabel(label, lang, userId, false);
 		
 		if (smallImagePath != null)
 			n.setSmallImage(smallImagePath);
 		if (largeImagePath != null)
 			n.setImage(largeImagePath);
 		if (description != null)
 			n.addDetails(description, lang, userId, false);
 		n.setIsPrivate(isPrivate);
 		//we do not set _version_ here; Solr does that
 		System.out.println("SolrNodeModel.newNode+ "+locator);		
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newNode(String label, String description, String lang,
 			String userId, String smallImagePath, String largeImagePath, boolean isPrivate) {
 		IResult result = newNode(database.getUUID(),label,description,lang,userId,smallImagePath,largeImagePath,isPrivate);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newSubclassNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newSubclassNode(String locator, String superclassLocator,
 			String label, String description, String lang, String userId,
 			String smallImagePath, String largeImagePath, boolean isPrivate) {
 		IResult result = newNode(locator,label,description,lang,userId,smallImagePath,largeImagePath,isPrivate);
 		INode n = (INode)result.getResultObject();
 		n.addSuperclassId(superclassLocator);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newSubclassNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newSubclassNode(String superclassLocator, String label,
 			String description, String lang, String userId, String smallImagePath, String largeImagePath, boolean isPrivate) {
 		IResult result = newNode(label,description,lang,userId,smallImagePath,largeImagePath,isPrivate);
 		INode n = (INode)result.getResultObject();
 		n.addSuperclassId(superclassLocator);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newInstanceNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newInstanceNode(String locator, String typeLocator,
 			String label, String description, String lang, String userId,
 			String smallImagePath, String largeImagePath, boolean isPrivate) {
 		IResult result = newNode(locator,label,description,lang,userId,smallImagePath,largeImagePath,isPrivate);
 		INode n = (INode)result.getResultObject();
 		n.setNodeType(typeLocator);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#newInstanceNode(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
 	 */
 	public IResult newInstanceNode(String typeLocator, String label,
 			String description, String lang, String userId, String smallImagePath, String largeImagePath, boolean isPrivate) {
 		IResult result = newNode(label,description,lang,userId,smallImagePath,largeImagePath,isPrivate);
 		INode n = (INode)result.getResultObject();
 		n.setNodeType(typeLocator);
 		return result;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#removeNode(java.lang.String)
 	 */
 	public IResult removeNode(String locator) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/**
 	 * <p><em>Dealing with Version in updating nodes being linked</em></p>
 	 * <p>It turns out you must use partialUpdate, and the particular Solr field
 	 * being updated needs to be dropped into a <code>Map<String,Object></code>, and
 	 * that map is then dropped into the field (property type). Solr then knows what 
 	 * to do with that.</p>
 	 */
 	public IResult relateNodes(String sourceNodeLocator,
 			String targetNodeLocator, String relationTypeLocator,
 			String userId, String smallImagePath, String largeImagePath, boolean isTransclude, boolean isPrivate) {
 		IResult result = new ResultPojo();
 		Set<String> credentials = getDefaultCredentials(userId);
 		String details = sourceNodeLocator+" "+relationTypeLocator+" "+targetNodeLocator;
 		//fetch the source actor node
 		IResult x = database.getNode(sourceNodeLocator, credentials);
 		INode nxA = (INode)x.getResultObject();
 		if (x.hasError())
 			result.addErrorString(x.getErrorString());
 		if (nxA != null) {
 			IResult y = database.getNode(targetNodeLocator,credentials);
 			INode nxB = (INode)y.getResultObject();
 			if (y.hasError())
 				result.addErrorString(y.getErrorString());
 			if (nxB != null) {
 				y = relateExistingNodes(nxA, nxB, relationTypeLocator,userId,smallImagePath,largeImagePath,isTransclude,isPrivate);
 				if (y.hasError())
 					result.addErrorString(y.getErrorString());
 			}
 				
 		}
 
 		return result;
 	}
 	
 	@Override
 	public IResult relateExistingNodes(INode sourceNode, INode targetNode,
 			String relationTypeLocator, String userId, String smallImagePath,
 			String largeImagePath, boolean isTransclude, boolean isPrivate) {
 		database.removeFromCache(sourceNode.getLocator());
 		database.removeFromCache(targetNode.getLocator());
 		IResult result = new ResultPojo();
 		//NOTE that we make the tuple an instance of the relation type, not of TUPLE_TYPE
 		ITuple t = (ITuple)this.newInstanceNode(relationTypeLocator, relationTypeLocator, 
 				sourceNode.getLocator()+" "+relationTypeLocator+" "+targetNode.getLocator(), "en", userId, smallImagePath, largeImagePath, isPrivate).getResultObject();
 		t.setIsTransclude(isTransclude);
 		t.setObject(targetNode.getLocator());
 		t.setObjectType(ITopicQuestsOntology.NODE_TYPE);
 		t.setSubjectLocator(sourceNode.getLocator());
 		t.setSubjectType(ITopicQuestsOntology.NODE_TYPE);
 		IResult x = database.putNode(t);
 		if (x.hasError())
 			result.addErrorString(x.getErrorString());
 //		Map<String,Object> updateMap = new HashMap<String,Object>();
 //		Map<String,String> newMap = new HashMap<String,String>();
 //		Map<String,Object> myMap = null;
 //		List<String> theList = null;
 		String tLoc = t.getLocator();
 		//save the tuple's locator in the output
 		result.setResultObject(tLoc);
 		boolean isRestricted = isPrivate; // seed restriction test
 		if (!x.hasError()) {
 			//start with source node
 			isRestricted = isRestricted || sourceNode.getIsPrivate();
 			System.out.println("Relate-0: "+sourceNode.toXML());
 			String key = ITopicQuestsOntology.TUPLE_LIST_PROPERTY;
 			if (isRestricted)
 				key = ITopicQuestsOntology.TUPLE_LIST_PROPERTY_RESTRICTED;
 			x = addPropertyValueInList(sourceNode, key, tLoc);
 			
 	/*		myMap = sourceNode.getProperties();
 			isRestricted = isRestricted || sourceNode.getIsPrivate();
 			if (!isRestricted)
 				sourceNode.addTuple(tLoc);
 			else
 				sourceNode.addRestrictedTuple(tLoc);
 			theList = sourceNode.listTuples();
 			myMap = sourceNode.getProperties();
 			updateMap.put(ITopicQuestsOntology.LOCATOR_PROPERTY, myMap.get(ITopicQuestsOntology.LOCATOR_PROPERTY));
 			if (myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE) != null)
 				updateMap.put(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE, myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE));
 			newMap.put(getUpdateKey(theList), t.getLocator());
 			if (!isRestricted)
 				updateMap.put(ITopicQuestsOntology.TUPLE_LIST_PROPERTY, newMap);
 			else
 				updateMap.put(ITopicQuestsOntology.TUPLE_LIST_PROPERTY_RESTRICTED, newMap);
 			
 			x = database.partialUpdateData(updateMap);
 			*/
 			if (!x.hasError()) {	
 				//deal with target node
 //				updateMap.clear();
 //				newMap.clear();
 				System.out.println("RELATO-3 "+x.hasError()+" "+targetNode.getLocator());
 				isRestricted = isRestricted || targetNode.getIsPrivate();
 				System.out.println("Relate-4: "+targetNode.toXML());
 				key = ITopicQuestsOntology.TUPLE_LIST_PROPERTY;
 				if (isRestricted)
 					key = ITopicQuestsOntology.TUPLE_LIST_PROPERTY_RESTRICTED;
 				x = addPropertyValueInList(targetNode, key, tLoc);
 /*
 				myMap = targetNode.getProperties();
 				isRestricted = isRestricted || targetNode.getIsPrivate();
 				if (!isRestricted)
 					targetNode.addTuple(tLoc);
 				else
 					targetNode.addRestrictedTuple(tLoc);
 				theList = targetNode.listTuples(); //was nxA
 				System.out.println("Relate-5: "+isRestricted+" "+targetNode.toXML());
 				updateMap.put(ITopicQuestsOntology.LOCATOR_PROPERTY, myMap.get(ITopicQuestsOntology.LOCATOR_PROPERTY));
 				if (myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE) != null)
 					updateMap.put(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE, myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE));
 				newMap.put(getUpdateKey(theList), t.getLocator());
 				if (!isRestricted)
 					updateMap.put(ITopicQuestsOntology.TUPLE_LIST_PROPERTY, newMap);
 				else
 					updateMap.put(ITopicQuestsOntology.TUPLE_LIST_PROPERTY_RESTRICTED, newMap);
 				x = database.partialUpdateData(updateMap); */
 				System.out.println("RELATO-6 "+x.hasError()+" "+targetNode.getLocator());
 
 			} else
 					result.addErrorString(x.getErrorString());
 		} else
 				result.addErrorString(x.getErrorString());	
 		log.logDebug("SolrNodeModel.relateExistingNodes "+sourceNode.getLocator()+" "+targetNode.getLocator()+" "+t.getLocator()+" | "+result.getErrorString());
 		return result;
 	}
 	
 	@Override
 	public IResult relateNewNodes(INode sourceNode, INode targetNode,
 			String relationTypeLocator, String userId, String smallImagePath,
 			String largeImagePath, boolean isTransclude, boolean isPrivate) {
 		IResult result = new ResultPojo();
 		ITuple t = (ITuple)this.newInstanceNode(relationTypeLocator, relationTypeLocator, 
 				sourceNode.getLocator()+" "+relationTypeLocator+" "+targetNode.getLocator(), "en", userId, smallImagePath, largeImagePath, isPrivate).getResultObject();
 		t.setIsTransclude(isTransclude);
 		t.setObject(targetNode.getLocator());
 		t.setObjectType(ITopicQuestsOntology.NODE_TYPE);
 		t.setSubjectLocator(sourceNode.getLocator());
 		t.setSubjectType(ITopicQuestsOntology.NODE_TYPE);
 		String tLoc = t.getLocator();
 		if (isPrivate) {
 			sourceNode.addRestrictedTuple(tLoc);
 			targetNode.addRestrictedTuple(tLoc);
 		} else {
 			sourceNode.addTuple(tLoc);
 			targetNode.addTuple(tLoc);
 		}
 		IResult x = database.putNode(sourceNode);
 		if (x.hasError())
 			result.addErrorString(x.getErrorString());
 		x = database.putNode(targetNode);
 		if (x.hasError())
 			result.addErrorString(x.getErrorString());
 		database.putNode(t);
 		if (x.hasError())
 			result.addErrorString(x.getErrorString());
 		log.logDebug("SolrNodeModel.relateNewNodes "+sourceNode.getLocator()+" "+targetNode.getLocator()+" "+t.getLocator()+" | "+result.getErrorString());
 		result.setResultObject(tLoc);
 		return result;
 	}
 
 	private String getUpdateKey(List<String>list) {
 		if (list == null)
 			return "set";
 		if (list.size() > 1)
 			return "add";
 		else
 			return "set";
 	}
 	
 	public Set<String> getDefaultCredentials(String userId) {
 		Set<String>result = new HashSet<String>();
 		result.add(userId);
 		return result;
 	}
 
 	public IResult assertMerge(INode sourceNode,
 			String targetNodeLocator, Map<String, Double> mergeData,
 			double mergeConfidence, String userLocator) {
		if (merger != null)
			return merger.assertMerge(sourceNode, targetNodeLocator, mergeData, mergeConfidence, userLocator);
		IResult result = new ResultPojo();
		result.addErrorString("SolrNodeModel.assertMerge called: No Merger Installed");
		log.logError("SolrNodeModel.assertMerge called: No Merger Installed", null);
		return result;
 	}
 
 	@Override
 	public IResult assertPossibleMerge(String sourceNodeLocator,
 			String targetNodeLocator, Map<String, Double> mergeData,
 			double mergeConfidence, String userLocator) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 	
 	@Override
 	public IResult assertUnmerge(String sourceNodeLocator, INode targetNodeLocator,
 			Map<String, Double> mergeData, double mergeConfidence,
 			String userLocator) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	@Override
 	public IResult updateNode(String nodeLocator, String updatedLabel,
 				String updatedDetails, String language, String oldLabel,
 				String oldDetails, String userId, boolean isLanguageAddition, Set<String> credentials) {
 		IResult result = database.getNode(nodeLocator, credentials);
 		if (result.getResultObject() != null) {
 			INode n = (INode)result.getResultObject();
 			if ((updatedLabel != null && !updatedLabel.equals("")) &&
 				(oldLabel == null || oldLabel.equals("")))
 				n.addLabel(updatedLabel, language, userId, isLanguageAddition);
 			if ((updatedDetails != null && !updatedLabel.equals("")) && 
 				(oldDetails == null || oldLabel.equals("")))
 				n.addDetails(updatedDetails, language, userId, isLanguageAddition);
 			List<String>val;
 			String field;
 			if (oldLabel != null) {
 				field = n.makeField(ITopicQuestsOntology.LABEL_PROPERTY, language);
 				val = (List<String>)n.getProperty(field);
 				val.remove(oldLabel);
 				val.add(updatedLabel);
 				n.setProperty(field, val);
 			}
 			if (oldDetails != null) {
 				field = n.makeField(ITopicQuestsOntology.DETAILS_PROPERTY, language);
 				val = (List<String>)n.getProperty(field);
 				val.remove(oldDetails);
 				val.add(updatedDetails);
 				n.setProperty(field, val);
 			}
 			return database.updateNode(n);
 		}
 		return result;
 	}
 	
 
 /*	@Override
 	public IResult updateNode(String nodeLocator, String propertyKey,
 			List<String> propertyValues, Set<String> credentials) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 */
 	@Override
 	public String dateToSolrDate(Date d) {
 		return dateField.formatExternal(d);
 	}
 
 	@Override
 	public IResult changePropertyValue(INode node, String key, String newValue) {
 
 		String sourceNodeLocator = node.getLocator();
 		Map<String,Object> updateMap = new HashMap<String,Object>();
 		Map<String,String> newMap = new HashMap<String,String>();
 		Map<String,Object> myMap = node.getProperties();
 		updateMap.put(ITopicQuestsOntology.LOCATOR_PROPERTY, sourceNodeLocator);
 		if (myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE) != null)
 			updateMap.put(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE, myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE));
 		newMap.put("set",newValue);
 		updateMap.put(key, newMap);
 		IResult result = database.partialUpdateData(updateMap);;
 		database.removeFromCache(sourceNodeLocator);
 		return result;
 	}
 
 	@Override
 	public IResult addPropertyValueInList(INode node, String key,
 			String newValue) {
 		String sourceNodeLocator = node.getLocator();
 		Map<String,Object> updateMap = new HashMap<String,Object>();
 		Map<String,Object> newMap = new HashMap<String,Object>();
 		Map<String,Object> myMap = node.getProperties();
 		List<String>values = makeListIfNeeded( myMap.get(key));
 		String what = getUpdateKey(values);
 		updateMap.put(ITopicQuestsOntology.LOCATOR_PROPERTY, sourceNodeLocator);
 		if (myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE) != null)
 			updateMap.put(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE, myMap.get(ITopicQuestsOntology.SOLR_VERSION_PROPERTY_TYPE));
 		newMap.put(what,newValue);
 		updateMap.put(key, newMap);
 		IResult result = database.partialUpdateData(updateMap);
 		database.removeFromCache(sourceNodeLocator);
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param o
 	 * @return can return <code>null</code>
 	 */
 	List<String> makeListIfNeeded(Object o) {
 		if (o == null)
 			return null;
 		List<String>result = null;
 		if (o instanceof List)
 			result = (List<String>)o;
 		else {
 			result = new ArrayList<String>();
 			result.add((String)o);
 		}
 		return result;
 	}
 
 
 
 }
