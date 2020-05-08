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
 
 import java.io.Writer;
 import java.util.*;
 import java.net.URLEncoder;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.topicquests.common.ResultPojo;
 import org.topicquests.common.api.IMergeRuleMethod;
 import org.topicquests.common.api.IResult;
 import org.topicquests.common.api.ITopicQuestsOntology;
 import org.topicquests.solr.api.ISolrClient;
 import org.topicquests.solr.api.ISolrDataProvider;
 import org.topicquests.util.LoggingPlatform;
 import org.topicquests.model.api.INodeModel;
 import org.topicquests.model.Node;
 import org.topicquests.model.api.IMergeImplementation;
 import org.topicquests.model.api.INode;
 import org.topicquests.model.api.ITuple;
 import org.topicquests.model.api.ITupleQuery;
 import org.topicquests.model.api.IXMLFields;
 
 import org.nex.util.LRUCache;
 
 import org.apache.commons.collections.CollectionUtils;
 //use parseDate to get a Date from a Solr date string
 import org.apache.solr.common.util.DateUtil;
 
 /**
  * @author park
  *
  */
 public class SolrDataProvider implements ISolrDataProvider {
 	private LoggingPlatform log = LoggingPlatform.getInstance();
 	private ISolrClient client;
 	private INodeModel _model;
 	private ITupleQuery tupleQuery;
 	private SolrExporter exporter;
 	/** We only save public nodes in this cache */
 	private LRUCache nodeCache;
 	
 	/**
 	 * @param cacheSize
 	 * 
 	 */
 	public SolrDataProvider(SolrEnvironment e, int cacheSize) throws Exception {
 		client = e.getSolrClient();
 
 		exporter = new SolrExporter(this);
 		nodeCache = new LRUCache(cacheSize);
 		tupleQuery = new SolrTupleQuery(this);
 	}
 	
 	@Override
 	public void removeFromCache(String nodeLocator) {
 		nodeCache.remove(nodeLocator);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#getUUID()
 	 */
 	public String getUUID() {
 		UUID x = UUID.randomUUID();
 		return x.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#getUUID_Pre(java.lang.String)
 	 */
 	public String getUUID_Pre(String prefix) {
 		UUID x = UUID.randomUUID();
 		return prefix+x.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#getUUID_Post(java.lang.String)
 	 */
 	public String getUUID_Post(String suffix) {
 		UUID x = UUID.randomUUID();
 		return x.toString()+suffix;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#getNode(java.lang.String)
 	 */
 	public IResult getNode(String locator, Set<String> credentials) {
 		IResult result = null;
 		INode n = (INode)nodeCache.get(locator);
 		if (n != null) {
 			result = new ResultPojo();
 			result.setResultObject(n);
 		} else {
 			result = runQuery(ITopicQuestsOntology.LOCATOR_PROPERTY+":"+locator,0,-1, credentials);
 			List<INode> l = (List<INode>)result.getResultObject();
 			System.out.println("SolrDataProvider.getNode "+locator+" "+l);
 			if (l != null && l.size() > 0) {
 				n = (INode)l.get(0);
 				if (!n.getIsPrivate()) {
 					//if it's public, return it and add to cache
 					result.setResultObject(n);
 					nodeCache.add(locator, n);
 				} else {
 					result.setResultObject(testNodeForCredentials(n,credentials)); //That's the result
 					if (result.getResultObject() == null)
 						result.addErrorString("Insufficient credentials for a private node");
 				}
 			} else
 				result.setResultObject(null);
 		}
 		return result;
 	}
 	
 	@Override
 	public IResult getVirtualNodeIfExists(String locator,
 			Set<String> credentials) {
 		String query = ITopicQuestsOntology.INSTANCE_OF_PROPERTY_TYPE+":"+ITopicQuestsOntology.MERGE_ASSERTION_TYPE+
 				" AND "+ITopicQuestsOntology.TUPLE_OBJECT_PROPERTY+":"+locator;
 		IResult r = client.runQuery(query, 0, -1);
 log.logDebug("SolrDataProvider.getVirtualNodeIfExists "+query+" | "+r.getResultObject());
 		String lox = locator;
 		if (r.getResultObject() != null) {
 			SolrDocumentList ol = (SolrDocumentList)r.getResultObject();
 			if (ol.size() > 0) {
 				Map<String,Object> m = (Map<String,Object>)ol.get(0);
 				lox = (String)m.get(ITopicQuestsOntology.TUPLE_SUBJECT_PROPERTY);
 log.logDebug("SolrDataProvider.getVirtualNodeIfExists-1 "+locator+" "+lox);			}
 		}
 		return getNode(lox,credentials);
 	}
 
 	/**
 	 * Utility method to accept or reject a node based on privacy and credentials
 	 * @param n
 	 * @param credentials
 	 * @return
 	 */
 	INode testNodeForCredentials(INode n, Set<String>credentials) {
 		INode result = n;
 		if (n.getIsPrivate()) {
 //			System.out.println("XXXX-1");
 			List<String>creds = n.listRestrictionCredentials();
 //			System.out.println("testnodeforcredentials-1 "+n.getLocator()+" "+creds+" "+credentials);
 			Collection<String> x = CollectionUtils.intersection(credentials, creds);
 			if (x.isEmpty())
 				result = null;
 		}
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#removeNode(java.lang.String)
 	 */
 	public IResult removeNode(String locator) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#putNode(org.topicquests.model.api.INode)
 	 */
 	public IResult putNode(INode node) {
 		return client.addData(node.getProperties());
 	}
 
 
 	/**
 	 * Utility method to hande a list of nodes
 	 * @param result
 	 * @param credentials
 	 */
 	void listNodes(IResult result, Set<String>credentials) {
 		List<INode> temp = (List<INode>)result.getResultObject();
 //		System.out.println("SolrDataProvider.listNodes "+temp.size());
 		if (temp.size() > 0) {
 //			System.out.println("AAAA");
 			List<INode> l = new ArrayList<INode>();
 			int len = temp.size();
 //			System.out.println("AAAB "+len);
 			INode n;
 			for (int i=0;i<len;i++) {
 //				System.out.println("AAAC "+i);
 				n = (INode)temp.get(i);
 //				System.out.println("XXXX "+n.getIsPrivate());
 				n = testNodeForCredentials(n,credentials);
 				if (n != null)
 					l.add(n);
 			}
 			result.setResultObject(l);
 		}
 //		System.out.println("BBBB");
 	
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#listNodesByNameString(java.lang.String)
 	 * /
 	public IResult listNodesByNameString(String nameString, Set<String> credentials) {
 		IResult result = runQuery(ITopicQuestsOntology.NAME_STRING_PROPERTY,0,-1);
 		listNodes(result,credentials);
 		return result;
 	}
 
 	public IResult listNodesByNameStringLike(String nameFragment, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.NAME_STRING_PROPERTY+":*"+nameFragment+"*";
 		IResult result = runQuery(query, start, count);
 		listNodes(result,credentials);
 		return result;
 	}
 */
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#listNodesByLabelAndType(java.lang.String, java.lang.String, int, int)
 	 */
 	public IResult listNodesByLabelAndType(String label, String typeLocator,
 			int start, int count, Set<String> credentials) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#listNodesByLabel(java.lang.String, int, int)
 	 */
 	public IResult listNodesByLabel(String label, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.LABEL_PROPERTY+":"+label;
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 	
 	public IResult listNodesByLabelLike(String labelFragment, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.LABEL_PROPERTY+":*"+labelFragment+"*";
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 	
 	public IResult listNodesByDetailsLike(String detailsFragment, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.DETAILS_PROPERTY+":*"+detailsFragment+"*";
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#listNodesByQuery(java.lang.String, int, int)
 	 */
 	public IResult listNodesByQuery(String queryString, int start, int count, Set<String> credentials) {
 		IResult result = runQuery(queryString, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 	public IResult listNodesByCreatorId(String creatorId, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.CREATOR_ID_PROPERTY+":"+creatorId;
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#listNodesByType(java.lang.String, int, int)
 	 */
 	public IResult listNodesByType(String typeLocator, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.INSTANCE_OF_PROPERTY_TYPE+":"+typeLocator;
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 
 	public IResult listInstanceNodes(String typeLocator, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.INSTANCE_OF_PROPERTY_TYPE+":"+typeLocator;
 		System.out.println("LISTINSTANCES- "+query);
 		IResult result = runQuery(query, start, count, credentials);
 		System.out.println("LISTINSTANCES-1 "+result.hasError()+" "+result.getResultObject());
 		
 		listNodes(result,credentials);
 		System.out.println("LISTINSTANCES+ "+result.hasError()+" "+result.getResultObject());
 		return result;
 	}
 
 	public IResult listSubclassNodes(String superclassLocator, int start,
 			int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.SUBCLASS_OF_PROPERTY_TYPE+":"+superclassLocator;
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.INodeModel#getNodeByPSI(java.lang.String)
 	 */
 	public IResult listNodesByPSI(String psi, int start, int count, Set<String> credentials) {
 		String query = ITopicQuestsOntology.PSI_PROPERTY_TYPE+":"+psi;
 		IResult result = runQuery(query, start, count, credentials);
 		listNodes(result,credentials);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#putTuple(org.topicquests.model.api.ITuple)
 	 */
 	public IResult putTuple(ITuple tuple) {
 		return client.addData(tuple.getProperties());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#getTuple(java.lang.String)
 	 */
 	public IResult getTuple(String tupleLocator, Set<String> credentials) {
 		IResult result = runQuery(ITopicQuestsOntology.LOCATOR_PROPERTY+":"+tupleLocator, 0,-1, credentials);
 		List<INode> l = (List<INode>)result.getResultObject();
 		if (l != null && l.size() > 0) {
 			ITuple n = (ITuple)l.get(0);	
 			result.setResultObject(n);
 		} else
 			result.setResultObject(null);
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#listTuplesByPredTypeAndSubjectId(java.lang.String, java.lang.String)
 	 * /
 	public IResult listTuplesByPredTypeAndSubjectId(String predType,
 			String subjectLocator, int start, int count) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#listTuplesBySubjectId(java.lang.String)
 	 * /
 	public IResult listTuplesBySubjectId(String subjectLocator, int start, int count) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#listTuplesByPredTypeAndObject(java.lang.String, java.lang.String)
 	 * /
 	public IResult listTuplesByPredTypeAndObject(String predType, String obj, int start, int count) {
 		IResult result = new ResultPojo();
 		String query = ITopicQuestsOntology.INSTANCE_OF_PROPERTY_TYPE+":"+predType; //TODO
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#updateTuple(org.topicquests.model.api.ITuple)
 	 * /
 	public IResult updateTuple(ITuple tuple) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.topicquests.model.api.IDataProvider#removeTuple(org.topicquests.model.api.ITuple)
 	 * /
 	public IResult removeTuple(ITuple tuple) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 */
 	public ISolrClient getSolrClient() {
 		return client;
 	}
 
 	public IResult runQuery(String queryString, int start, int count, Set<String> credentials) {
 		System.out.println("SolrDataProvider.runQuery "+queryString);
 		IResult result = client.runQuery(queryString, start, count);
 		result = convertResultsWithFilter(result,credentials);
 		return result;
 	}
 
 	/**
 	 * Convert a list of {@link SolrDocument} objects to either {@link INode} or {@link ITuple} objects
 	 * @param x
 	 * @return
 	 */
 	IResult convertResults(IResult x) {
 		IResult result = new ResultPojo();
 		List<INode> l = new ArrayList<INode>();
 		result.setResultObject(l);
 		SolrDocumentList ol = (SolrDocumentList)x.getResultObject();
 		if (ol != null) {
 			INode obj;
 			Map<String,Object> dx;
 			int len = ol.size();
 			for (int i=0;i<len;i++) {
 				dx = (Map<String,Object>)ol.get(i);
 				unescapeQueryCulprits(dx);
 				obj = new Node(dx);
 				//System.out.println("QQQQ "+obj.toXML());
 				l.add(obj);
 			}
 		}
 		return result;
 
 	}
 	
 	void unescapeQueryCulprits(Map<String,Object>map) {
 		String key;
 		List<String> vals;
 		Object o;
 		Iterator<String>itr = map.keySet().iterator();
 		while (itr.hasNext()) {
 			key = itr.next();
 			if (key.startsWith(ITopicQuestsOntology.LABEL_PROPERTY) ||
 				key.startsWith(ITopicQuestsOntology.DETAILS_PROPERTY)) {
 				o = map.get(key);
 				if (o != null) {
 					if (o instanceof List) {
 						vals = (List<String>)o;
 						map.put(key, cleanListOfStrings(vals));
 					} else {
 						//REQUIRE that labels and details always be List<String>
 						vals = new ArrayList<String>();
 						vals.add((String)o);
 						map.put(key, cleanListOfStrings(vals));
 					}
 				}
 			}
 		}
 	}
 	
 	List<String> cleanListOfStrings(List<String>vals) {
 		List<String>result = new ArrayList<String>(vals.size());
 		String x;
 		Iterator<String>itr = vals.iterator();
 		while (itr.hasNext()) {
 			x = itr.next();
 			x = QueryUtil.unEscapeQueryCulprits(x);
 			result.add(x);
 		}
 		
 		return result;
 	}
 	/**
 	 * Convert a list of {@link SolrDocument} objects to either {@link INode} or {@link ITuple} object
 	 * after filtering on <code>credentials</code>
 	 * @param x
 	 * @param credentials
 	 * @return
 	 */
 	IResult convertResultsWithFilter(IResult x, Set<String>credentials) {
 //		System.out.println("CONVERTRESULT- "+x.getResultObject());
 		IResult result = new ResultPojo();
 		List<INode> l = new ArrayList<INode>();
 		result.setResultObject(l);
 		SolrDocumentList ol = (SolrDocumentList)x.getResultObject();
 		if (ol != null) {
 			INode obj;
 			Map<String,Object> dx;
 			int len = ol.size();
 			for (int i=0;i<len;i++) {
 				dx = (Map<String,Object>)ol.get(i);
 				if (isSafe(dx,credentials)) {
 					obj = new Node(dx);
 					//System.out.println("QQQQ "+obj.toXML());
 					l.add(obj);
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Will always return false if private node and <code>credentials = null</code>
 	 * @param node
 	 * @param credentials
 	 * @return
 	 */
 	boolean isSafe(Map<String,Object>node, Set<String>credentials) {
 		System.out.println("ISSAFE- "+node.get(ITopicQuestsOntology.LOCATOR_PROPERTY)+" "+node.get(ITopicQuestsOntology.IS_PRIVATE_PROPERTY));
 		Boolean isPrivate = (Boolean)node.get(ITopicQuestsOntology.IS_PRIVATE_PROPERTY);
 		if (isPrivate == null) // rare event; need to understand what's going on
 			log.logError("SolrDataProvider.isSave bad boolean "+node, null);
 		else if (isPrivate) {
 			if (credentials == null)
 				return false;
 			Object o = node.get(ITopicQuestsOntology.RESTRICTION_PROPERTY_TYPE);
 			if (o == null) {
 				//only option is to see if credentials include userId
 				String creatorId = (String)node.get(ITopicQuestsOntology.CREATOR_ID_PROPERTY);
 				return credentials.contains(creatorId);
 			} else {
 				List<String>acls = (List<String>)o;
 				Collection<String> x = CollectionUtils.intersection(credentials, acls);
 				return x.isEmpty();
 			}
 		}
 		return true; // default
 	}
 	/**
 	 * Do it by hand now, but later create a custom Solr RequestHandler
 	 */
 	public IResult nodeIsA(String nodeLocator, String targetTypeLocator, Set<String> credentials) {
 		IResult result = walkUpTransitiveClosure(nodeLocator,targetTypeLocator,credentials);
 		if (result.getResultObject() != null)
 			result.setResultObject(new Boolean(true));
 		else
 			result.setResultObject(new Boolean(false));
 		return result;
 	}
 
 	/**
 	 * A recursive call to walk up the isA hierarchy, if any, above the
 	 * @param locator
 	 * @param typeTargetLocator
 	 * @param credentials
 	 * @return IResult.returnObject = <code>null</code> if not found. Any non-null means found
 	 */
 	IResult walkUpTransitiveClosure(String locator, String typeTargetLocator, Set<String>credentials) {
 		IResult result = new ResultPojo();
 		result.setResultObject(null);
 		IResult temp = getNode(locator,credentials);
 		INode child = (INode)temp.getResultObject();
 		temp.setResultObject(null);
 		if (temp.hasError())
 			result.addErrorString(temp.getErrorString());
 		if (child != null) {
 			//The cases where that could be null are:
 			//  database error (node's there, system failed)
 			//  lack of appropriate credentials (private node)
 			List<String> supers = child.listSuperclassIds();
 			String type = child.getNodeType();
 			if (type.equals(typeTargetLocator)) {
 				result.setResultObject(child);
 				return result;
 			} else {
 				temp = walkUpTransitiveClosure(type,typeTargetLocator,credentials);
 				if (temp.hasError())
 					result.addErrorString(temp.getErrorString());
 				if (temp.getResultObject() != null) {
 					result.setResultObject(temp.getResultObject());
 					return result;
 				}
 			}
 			if (supers != null && supers.size() > 0) {
 				Iterator<String>itr = supers.iterator();
 				while (itr.hasNext()) {
 					temp = walkUpTransitiveClosure(itr.next(),typeTargetLocator,credentials);
 					if (temp.hasError())
 						result.addErrorString(temp.getErrorString());
 					if (temp.getResultObject() != null) {
 						result.setResultObject(temp.getResultObject());
 						return result;
 					}
 				}
 			}
 		}
 		return result;
 	}
 	/**
 	 * Do it by hand now, but later create a custom Solr RequestHandler
 	 */
 	public IResult getNodeView(String locator, Set<String> credentials) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 
 	@Override
 	public INodeModel getNodeModel() {
 		return _model;
 	}
 
 
 
 
 	@Override
 	public IResult exportXmlFile(Writer out, Set<String> credentials) {
 		return exporter.exportXmlFile(out, credentials);
 	}
 
 
 	@Override
 	public IResult exportXmlTreeFile(String treeRootLocator, Writer out, Set<String> credentials) {
 		IResult result = null;
 		try {
 			out.write("<"+IXMLFields.DATABASE+">\n");
 			result =  exporter.exportXmlTreeFile(treeRootLocator, out, credentials, true);
 			out.write("</"+IXMLFields.DATABASE+">\n");
 			out.flush();
 			out.close();
 		} catch (Exception e) {
 			result.addErrorString(e.getMessage());
 			log.logError(e.getMessage(),e);
 		}
 		return result;
 	}
 
 
 	@Override
 	public IResult updateNode(INode node) {
 		this.removeFromCache(node.getLocator());
 		return client.updateData(node.getProperties());
 	}
 
 
 	@Override
 	public IResult partialUpdateData(Map<String, Object> fields) {
 		String lox = (String)fields.get(ITopicQuestsOntology.LOCATOR_PROPERTY);
 		if (lox == null) {
 			IResult result = new ResultPojo();
 			result.addErrorString("SolrDataProvider.partialUpdateData missing locator property");
 			return result;
 		} else {
 			this.removeFromCache(lox);
 			return client.partialUpdateData(fields);
 		}
 		
 	}
 
 	@Override
 	public ITupleQuery getTupleQuery() {
 		return tupleQuery;
 	}
 
 	@Override
 	public IResult createMergeRule(IMergeRuleMethod theMethod) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	@Override
 	public IResult existsTupleBySubjectOrObjectAndRelation(String theLocator,
 			String relationLocator) {
 		IResult result = new ResultPojo();
 		// TODO Auto-generated method stub
 		return result;
 	}
 
 	@Override
 	public void setMergeBean(IMergeImplementation merger) {
 		_model = new SolrNodeModel(this,merger);
 	}
 
 }
