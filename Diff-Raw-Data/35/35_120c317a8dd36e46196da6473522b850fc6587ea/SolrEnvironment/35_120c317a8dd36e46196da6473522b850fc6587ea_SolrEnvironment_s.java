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
 import java.util.*;
 
 import org.nex.config.ConfigPullParser;
 import org.topicquests.common.api.IConsoleDisplay;
 import org.topicquests.model.BiblioBootstrap;
 import org.topicquests.model.CoreBootstrap;
 import org.topicquests.model.RelationsBootstrap;
 import org.topicquests.model.api.IMergeImplementation;
 import org.topicquests.solr.api.ISolrClient;
 import org.topicquests.solr.api.ISolrDataProvider;
 import org.topicquests.solr.api.ISolrModel;
 import org.topicquests.solr.api.ISolrQueryIterator;
 import org.topicquests.util.LoggingPlatform;
 import org.topicquests.util.Tracer;
 
 /**
  * 
  * @author park
  *
  */
 public class SolrEnvironment {
 	private LoggingPlatform log = LoggingPlatform.getInstance();
 	private Hashtable<String,Object>props;
 	private ISolrClient solr;
 	private ISolrDataProvider database;
 	private ISolrModel model;
 	private IConsoleDisplay host;
 
 	/**
 	 * @param p
 	 */
 	public SolrEnvironment(Hashtable<String,Object>p) {
 		init(p);
 	}
 	
 	public SolrEnvironment() {
 		ConfigPullParser p = new ConfigPullParser("config-props.xml");
 		init(p.getProperties());
 	}
 	
 	void init(Hashtable<String,Object>p) {
 		props = p;
 		try {
 			solr = new Solr3Client(getStringProperty("SolrURL")); //TODO Solr4Client for testing
 			record("Solr4Client started");
 			System.out.println("AAAA "+getStringProperty("MapCacheSize"));
 			int cachesize = Integer.parseInt(getStringProperty("MapCacheSize"));
 			database = new SolrDataProvider(this,cachesize );
 			IMergeImplementation merger;
 			String cp = (String)props.get("MergeImplementation");
			Class o = Class.forName(cp);
			merger = (IMergeImplementation)o.newInstance();
			merger.init(this);
			database.setMergeBean(merger);
 			model = new SolrModel(this);
 			String bs = (String)props.get("ShouldBootstrap");
 			boolean shouldBootstrap = false; // default value
 			if (bs != null)
 				shouldBootstrap = bs.equalsIgnoreCase("Yes");
 			if (shouldBootstrap)
 				bootstrap();
 		} catch (Exception e) {
 			logError(e.getMessage(),e);
 			e.printStackTrace();
 		}
 		logDebug("Started");
 	}
 	void bootstrap() {
 		CoreBootstrap cbs = new CoreBootstrap(database);
 		cbs.bootstrap();
 		BiblioBootstrap bbs = new BiblioBootstrap(database);
 		bbs.bootstrap();
 		RelationsBootstrap rbs = new RelationsBootstrap(database);
 		rbs.bootstrap();
 	}
 	
 	public ISolrModel getSolrModel() {
 		return model;
 	}
 	
 	public ISolrClient getSolrClient() {
 		return solr;
 	}
 	
 	public ISolrDataProvider getDataProvider() {
 		return database;
 	}
 	public Map<String,Object> getProperties() {
 		return props;
 	}
 	
 	public String getStringProperty(String key) {
 		return (String)props.get(key);
 	}
 		
 	public void setConsoleDisplay(IConsoleDisplay d) {
 		host = d;
 	}
 	/**
 	 * Can return <code>null</code>
 	 * @return
 	 */
 	public IConsoleDisplay getConsoleDisplay() {
 		return host;
 	}
 
 	/**
 	 * Return a new {@link ISolrQueryIterator}
 	 * @return
 	 */
 	public ISolrQueryIterator getQueryIterator() {
 		return new SolrQueryIterator(this);
 	}
 	
 	
 	public void shutDown() {
 		//
 	}
 	/////////////////////////////
 	// Utilities
 	public void logDebug(String msg) {
 		log.logDebug(msg);
 	}
 	
 	public void logError(String msg, Exception e) {
 		log.logError(msg,e);
 	}
 	
 	public void record(String msg) {
 		log.record(msg);
 	}
 
 	public Tracer getTracer(String name) {
 		return log.getTracer(name);
 	}
 }
