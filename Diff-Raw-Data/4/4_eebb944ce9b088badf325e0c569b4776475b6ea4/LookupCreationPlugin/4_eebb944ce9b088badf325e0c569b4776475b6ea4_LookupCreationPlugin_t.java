 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.enrichment;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.util.ClientUtils;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 
 import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
 import eu.europeana.corelib.definitions.jibx.ProxyType;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.solr.beans.FullBean;
 import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
 import eu.europeana.corelib.solr.entity.AggregationImpl;
 import eu.europeana.corelib.solr.entity.ProxyImpl;
 import eu.europeana.corelib.solr.exceptions.EdmFieldNotFoundException;
 import eu.europeana.corelib.solr.exceptions.EdmValueNotFoundException;
 import eu.europeana.corelib.solr.utils.EseEdmMap;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
 import eu.europeana.corelib.tools.utils.HashUtils;
 import eu.europeana.corelib.tools.utils.PreSipCreatorUtils;
 import eu.europeana.corelib.tools.utils.SipCreatorUtils;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.enrichment.service.EnrichmentService;
 import eu.europeana.uim.enrichment.utils.PropertyReader;
 import eu.europeana.uim.enrichment.utils.UimConfigurationProperty;
 import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
 import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
 import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
 import eu.europeana.uim.store.MetaDataRecord;
 
 /**
  * Redirect creation plugin
  * 
  * @author Yorgos.Mamakis@ kb.nl
  * 
  * @param <I>
  */
 public class LookupCreationPlugin<I> extends
 		AbstractIngestionPlugin<MetaDataRecord<I>, I> {
 	private static EnrichmentService enrichmentService;
 	private static final Logger log = Logger
 			.getLogger(LookupCreationPlugin.class.getName());
 
 	public LookupCreationPlugin(String name, String description) {
 		super(name, description);
 
 		// TODO Auto-generated constructor stub
 	}
 
 	public LookupCreationPlugin() {
 		super("", "");
 
 		// TODO Auto-generated constructor stub
 	}
 
 	private static String repository = PropertyReader
 			.getProperty(UimConfigurationProperty.UIM_REPOSITORY);
 
 	private static IBindingFactory bfact;
 	static {
 		try {
 			// Should be placed in a static block for performance reasons
 			bfact = BindingDirectory.getFactory(RDF.class);
 
 		} catch (JiBXException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public TKey<?, ?>[] getInputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public TKey<?, ?>[] getOptionalFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public TKey<?, ?>[] getOutputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public List<String> getParameters() {
 		// TODO Auto-generated method stub
 		return new ArrayList<String>();
 	}
 
 	@Override
 	public int getPreferredThreadCount() {
 		// TODO Auto-generated method stub
 		return 12;
 	}
 
 	@Override
 	public int getMaximumThreadCount() {
 		// TODO Auto-generated method stub
 		return 15;
 	}
 
 	@Override
 	public boolean process(MetaDataRecord<I> mdr,
 			ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException, CorruptedDatasetException {
 		IUnmarshallingContext uctx;
 
 		try {
 
 			String value = null;
 			if (mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD) != null
 					&& mdr.getValues(
 							EuropeanaModelRegistry.EDMDEREFERENCEDRECORD)
 							.size() > 0) {
 				value = mdr.getValues(
 						EuropeanaModelRegistry.EDMDEREFERENCEDRECORD).get(0);
 			} else {
 				value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD).get(0);
 			}
 			uctx = bfact.createUnmarshallingContext();
 
 			String collectionId = (String) mdr.getCollection().getMnemonic();
			System.out.println(collectionId);
 			String fileName;
 			String oldCollectionId = enrichmentService
 					.getCollectionMongoServer().findOldCollectionId(
 							collectionId);
 			if (oldCollectionId != null) {
 				collectionId = oldCollectionId;
 				fileName = oldCollectionId;
 			} else {
 				fileName = (String) mdr.getCollection().getName();
 			}
 			RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
 			FullBeanImpl fullBean = constructFullBeanMock(rdf, collectionId);
 			String hash = null;
 			try{
				hash = hashExists(collectionId, fileName, fullBean);
 			}
 			catch(Exception e){
 				log.log(Level.SEVERE, e.getMessage());
 				return false;
 			}
 			
 			if (StringUtils.isNotEmpty(hash)) {
 
 				createLookupEntry(fullBean, collectionId, hash);
 				return true;
 			}
 		} catch (JiBXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	// Generate a minimum Fullbean
 	private FullBeanImpl constructFullBeanMock(RDF rdf, String collectionId) {
 		FullBeanImpl fBean = new FullBeanImpl();
 		AggregationImpl aggr = new AggregationImpl();
 		List<AggregationImpl> aggrs = new ArrayList<AggregationImpl>();
 		aggr.setAbout(rdf.getAggregationList().get(0).getAbout());
 		aggr.setEdmIsShownAt(rdf.getAggregationList().get(0).getIsShownAt() != null ? rdf
 				.getAggregationList().get(0).getIsShownAt().getResource()
 				: null);
 		aggr.setEdmIsShownBy(rdf.getAggregationList().get(0).getIsShownBy() != null ? rdf
 				.getAggregationList().get(0).getIsShownBy().getResource()
 				: null);
 		aggr.setEdmObject(rdf.getAggregationList().get(0).getObject() != null ? rdf
 				.getAggregationList().get(0).getObject().getResource()
 				: null);
 		aggrs.add(aggr);
 		List<ProxyImpl> proxies = new ArrayList<ProxyImpl>();
 		ProxyImpl proxy = new ProxyImpl();
 		Map<String, List<String>> dcIdentifiers = new HashMap<String, List<String>>();
 		ProxyType proxyRDF = findProxy(rdf);
 		if (proxyRDF != null) {
 			List<Choice> choices = proxyRDF.getChoiceList();
 			List<String> val = new ArrayList<String>();
 			for (Choice choice : choices) {
 				if (choice.ifIdentifier()) {
 					val.add(choice.getIdentifier().getString());
 				}
 			}
 			dcIdentifiers.put("def", val);
 			proxy.setDcIdentifier(dcIdentifiers);
 			proxies.add(proxy);
 			fBean.setProxies(proxies);
 		}
 		fBean.setAggregations(aggrs);
 		fBean.setAbout(rdf.getProvidedCHOList().get(0).getAbout());
 		return fBean;
 	}
 
 	private ProxyType findProxy(RDF rdf) {
 		for (ProxyType proxy : rdf.getProxyList()) {
 			if (proxy.getEuropeanaProxy() == null
 					|| !proxy.getEuropeanaProxy().isEuropeanaProxy()) {
 				return proxy;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void initialize(ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException {
 
 	}
 
 	@Override
 	public void completed(ExecutionContext<MetaDataRecord<I>, I> context)
 			throws IngestionPluginFailedException {
 
 	}
 
 	@Override
 	public void initialize() {
 
 	}
 
 	private String hashExists(String collectionId, String fileName,
 			FullBean fullBean) throws EdmFieldNotFoundException,EdmValueNotFoundException,NullPointerException{
 		SipCreatorUtils sipCreatorUtils = new SipCreatorUtils();
 		sipCreatorUtils.setRepository(repository);
 		String hashField = sipCreatorUtils.getHashField(fileName, fileName);
 		if (hashField != null) {
 			String val = null;
 
 			try {
 				val = EseEdmMap
 						.getEseEdmMap(
 								StringUtils.contains(hashField, "[") ? StringUtils.substringBefore(
 										hashField, "[")
 										: hashField, fullBean.getAbout())
 						.getEdmValue(fullBean);
 				if (val != null) {
 					return HashUtils.createHash(val);
 				}
 			} catch (EdmFieldNotFoundException e) {
 				throw e;
 			}
 			catch (EdmValueNotFoundException e) {
 				throw e;
 			}
 			catch (NullPointerException e) {
 				throw e;
 			}
 		}
 		PreSipCreatorUtils preSipCreatorUtils = new PreSipCreatorUtils();
 		preSipCreatorUtils.setRepository(repository);
 
 		if (preSipCreatorUtils.getHashField(fileName, fileName) != null) {
 			String val = EseEdmMap.getEseEdmMap(
 					preSipCreatorUtils.getHashField(collectionId, fileName),
 					fullBean.getAbout()).getEdmValue(fullBean);
 			if (val != null) {
 				return HashUtils.createHash(val);
 			}
 		}
 		return null;
 	}
 
 	private void saveEuropeanaId(EuropeanaId europeanaId) {
 		enrichmentService.getEuropeanaIdMongoServer().saveEuropeanaId(
 				europeanaId);
 
 	}
 
 	private void createLookupEntry(FullBean fullBean, String collectionId,
 			String hash) {
 
 		ModifiableSolrParams params = new ModifiableSolrParams();
 		params.add(
 				"q",
 				"europeana_id:"
 						+ ClientUtils.escapeQueryChars("/" + collectionId + "/"
 								+ hash));
 		try {
 			SolrDocumentList solrList = enrichmentService
 					.getProductionSolrServer().query(params).getResults();
 			if (solrList.size() > 0) {
 				EuropeanaId id = new EuropeanaId();
 				id.setOldId("/" + collectionId + "/" + hash);
 				id.setLastAccess(0);
 				id.setTimestamp(new Date().getTime());
 				id.setNewId(fullBean.getAbout());
 				saveEuropeanaId(id);
 
 			}
 
 		} catch (SolrServerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setEnrichmentService(EnrichmentService service) {
 		LookupCreationPlugin.enrichmentService = service;
 	}
 }
