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
 
 package eu.europeana.uim.europeanaspecific.workflowstarts.httpzip;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 import org.theeuropeanlibrary.model.common.qualifier.LinkStatus;
 import org.theeuropeanlibrary.model.common.qualifier.Status;
 
 import eu.europeana.uim.storage.StorageEngine;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.common.progress.ProgressMonitor;
 import eu.europeana.uim.europeanaspecific.workflowstarts.util.SaxBasedIDExtractor;
 import eu.europeana.corelib.definitions.jibx.Aggregation;
 import eu.europeana.corelib.definitions.jibx.HasView;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.jibx.WebResourceType;
 import eu.europeana.corelib.tools.lookuptable.LookupState;
 import eu.europeana.dedup.osgi.service.DeduplicationResult;
 import eu.europeana.dedup.osgi.service.DeduplicationService;
 import eu.europeana.dedup.osgi.service.exceptions.DeduplicationException;
 import eu.europeana.uim.model.europeana.EuropeanaLink;
 import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.Request;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * This class uses the private zipiterator in order to batch load records into
  * the storage engine
  * 
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @param <I>
  * @since 5 Mar 2012
  */
 public class ZipLoader<I> {
 
 	@SuppressWarnings("rawtypes")
 	private StorageEngine storage;
 
 	@SuppressWarnings("rawtypes")
 	private Request request;
 
 	@SuppressWarnings("unused")
 	private ProgressMonitor monitor;
 	
 	private Iterator<String> zipiterator;
 
 	private ExecutionContext<MetaDataRecord<I>, I> context;
 
 	private int totalProgress = 0;
 	
 	private int expectedRecords = 0;
 
 	private int created = 0;
 	
 	private int updated = 0;
 	
 	private int omitted = 0;
 	
 	private int discarded = 0;
 	
 	private int generated = 0;
 	
 	private boolean forceUpdate;
 
 	/**
 	 * Reference to Deduplication service
 	 */
 	private DeduplicationService dedup;
 
 	/**
 	 * Static unmarshalling context used for all EDM unmarshalling operations
 	 */
	private static IUnmarshallingContext mctx;
 
 	/**
 	 * Static Logger reference
 	 */
 	private static final Logger LOGGER = Logger.getLogger(ZipLoader.class.getName());
 	
 	
 	// Unmarshalling context initialization
 	static {
 
 		IBindingFactory bfact;
 		try {
 			bfact = BindingDirectory.getFactory(RDF.class);
			mctx = bfact.createUnmarshallingContext();
 		} catch (JiBXException e) {
 			LOGGER.log(Level.WARNING,"ZipLoader:Error intializing static unmarshalling context",e);
 
 		}
 
 	}
 
 	/**
 	 * Default constructor for this class
 	 * 
 	 * @param expectedRecords
 	 *            The number of expected records
 	 * @param zipiterator
 	 *            An iterator over the parsed files
 	 * @param storage
 	 *            A reference to the storage engine
 	 * @param request
 	 *            A reference to the current request
 	 * @param monitor
 	 *            A reference to the current monitor
 	 * @param loggingEngine
 	 *            A reference to the logging engine
 	 */
 	public ZipLoader(int expectedRecords, Iterator<String> zipiterator,
 			ExecutionContext<MetaDataRecord<I>, I> context, Request<I> request,
 			DeduplicationService dedup, String forceupdate) {
 		super();
 		this.expectedRecords = expectedRecords;
 		this.zipiterator = zipiterator;
 		this.storage = context.getStorageEngine();
 		this.request = request;
 		this.monitor = context.getMonitor();
 		this.dedup = dedup;
 		this.context = context;
 
 		if (forceupdate != null && forceupdate.toLowerCase().equals("true")) {
 			forceUpdate = true;
 		}
 	}
 
 	/**
 	 * Returns the next batch of metadata records to the orchestrator
 	 * 
 	 * @param <I>
 	 *            the type of the Metadata Record ID
 	 * @param batchSize
 	 *            number of loaded records
 	 * @param save
 	 *            Should they be saved to the index?
 	 * @return list of loaded Metadata records
 	 */
 	@SuppressWarnings({ "unchecked" })
 	public synchronized List<MetaDataRecord<I>> doNext(int batchSize,
 			boolean save) {
 		List<MetaDataRecord<I>> result = new ArrayList<MetaDataRecord<I>>();
 		int progress = 0;
 
 
 		
 		
 		HttpZipWorkflowStart.Data value = context
 				.getValue(HttpZipWorkflowStart.DATA_KEY);
 
 		while (zipiterator.hasNext()) {
 
 			if (progress >= batchSize) {
 				break;
 			}
 
 			String rdfstring = zipiterator.next();
 
 			try {
 
 				// First Check the record for duplicates.
 				List<DeduplicationResult> reslist = dedup.deduplicateRecord(
 						(String) request.getCollection().getMnemonic(),
 						(String) request.getId(), rdfstring);
 
 				
 				//If the resultlist contains more than one records then we assume that the incoming records
 				//have been split. We append the number of the extra generated records in the "generated"
 				//variable.
 				
 				if(reslist.size() > 1){
 					generated += reslist.size() -1;
 				}
 				
 				for (DeduplicationResult dedupres : reslist) {
 
 					LookupState state = dedupres.getLookupresult().getState();
 
 					MetaDataRecord<I> mdr = null;
 					
 					switch (state) {
 					case ID_REGISTERED:						
 						mdr = processrecord(mdr, dedupres, Status.CREATED);
 						LOGGER.log(Level.INFO,"Unique Identifier in ID_REGISTERED state for record with ID " + dedupres
 								.getDerivedRecordID());
 						
 						value.deletioncandidates.remove(dedupres
 								.getDerivedRecordID());
 						
 						dedup.deleteFailedRecord(dedupres.getOriginalRecordID(),(String) request.getCollection().getMnemonic());
 						
 						created ++;
 						
 						break;
 					case COLLECTION_CHANGED:
 						LOGGER.log(Level.INFO,"Unique Identifier in COLLECTION_CHANGED state for record with ID " + dedupres
 								.getDerivedRecordID());
 						discarded ++;
 						break;
 					case DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS:
 						LOGGER.log(Level.INFO,"Unique Identifier in DUPLICATE_IDENTIFIER_ACROSS_COLLECTIONS state for record with ID " + dedupres
 								.getDerivedRecordID());
 						discarded ++;
 						break;
 					case DUPLICATE_INCOLLECTION:						
 						LOGGER.log(Level.INFO,"Unique Identifier in DUPLICATE_INCOLLECTION state for record with ID " + dedupres
 							.getDerivedRecordID());
 						discarded ++;
 						break;
 					case DUPLICATE_RECORD_ACROSS_COLLECTIONS:
 						LOGGER.log(Level.INFO,"Unique Identifier in DUPLICATE_RECORD_ACROSS_COLLECTIONS state for record with ID " + dedupres
 								.getDerivedRecordID());
 						discarded ++;
 						break;
 					case IDENTICAL:
 
 						if (forceUpdate) {
 							LOGGER.log(Level.INFO,"Unique Identifier in IDENTICAL (forceupdate) state for record with ID " + dedupres
 									.getDerivedRecordID());
 							try {
 								mdr = storage.getMetaDataRecord(dedupres
 										.getDerivedRecordID());
 								processrecord(mdr,dedupres,Status.UPDATED);
 								value.deletioncandidates.remove(dedupres
 										.getDerivedRecordID());
 								
 							} catch (StorageEngineException e) {
 								e.printStackTrace();
 								mdr = processrecord(mdr,dedupres,Status.UPDATED);
 								value.deletioncandidates.remove(dedupres
 										.getDerivedRecordID());
 							}
 							
 							updated ++;
 
 						} else {
 							LOGGER.log(Level.INFO,"Unique Identifier in IDENTICAL (ignore identical) state for record with ID " + dedupres
 									.getDerivedRecordID());
 							value.deletioncandidates.remove(dedupres
 									.getDerivedRecordID());
 							omitted ++;
 						}
  
 						break;
 					case UPDATE:
 						try {
 							LOGGER.log(Level.INFO,"Unique Identifier in UPDATE state for record with ID " + dedupres
 									.getDerivedRecordID());
 							mdr = storage.getMetaDataRecord(dedupres
 									.getDerivedRecordID());
 							processrecord(mdr, dedupres,Status.UPDATED);
 							value.deletioncandidates.remove(dedupres
 									.getDerivedRecordID());
 							dedup.deleteFailedRecord(dedupres.getOriginalRecordID(),(String) request.getCollection().getMnemonic());
 						} catch (StorageEngineException e) {
 							e.printStackTrace();
 							mdr = processrecord(mdr,dedupres,Status.UPDATED);
 							value.deletioncandidates.remove(dedupres
 									.getDerivedRecordID());
 							dedup.deleteFailedRecord(dedupres.getOriginalRecordID(),(String) request.getCollection().getMnemonic());
 
 						}
 						updated ++;
 						break;
 					default:
 						break;
 					}
 
 					if (mdr != null) {
 						result.add(mdr);
 					}
 
 				}
 
 				progress++;
 				totalProgress++;
 
 			} catch (JiBXException e) {
 				LOGGER.log(Level.SEVERE,"ZipLoader:Error unmarshalling xml for object",e);
 				
 				SaxBasedIDExtractor extractor = new SaxBasedIDExtractor();
 				List<String> ids = extractor.extractIDs(rdfstring);
 				
 				for(String id:ids){
 					List<String> newids = dedup.retrieveEuropeanaIDFromOld(id, request.getCollection().getMnemonic());
 					
 					for(String newid : newids){
 						value.deletioncandidates.remove(newid);
 						
 						dedup.createUpdateIdStatus(id,newid,request.getCollection().getMnemonic(),rdfstring,LookupState.INCOMPATIBLE_XML_CONTENT);
 					}	
 				}
 				discarded ++;
 				
 			} catch (StorageEngineException e) {
 				e.printStackTrace();
 				
 				SaxBasedIDExtractor extractor = new SaxBasedIDExtractor();
 				List<String> ids = extractor.extractIDs(rdfstring);
 				
 				for(String id:ids){
 					List<String> newids = dedup.retrieveEuropeanaIDFromOld(id, request.getCollection().getMnemonic());
 					
 					for(String newid : newids){
 						value.deletioncandidates.remove(newid);
 						dedup.createUpdateIdStatus(id,newid,request.getCollection().getMnemonic(),rdfstring,LookupState.SYSTEM_ERROR);
 					}
 				}
 				
 				LOGGER.log(Level.SEVERE,"ZipLoader:Storage engine error",e);
 				discarded ++;
 			} catch (DeduplicationException e) {
 				LOGGER.log(Level.SEVERE,"ZipLoader:Deduplication Exception",e);
 				
 				SaxBasedIDExtractor extractor = new SaxBasedIDExtractor();
 				List<String> ids = extractor.extractIDs(rdfstring);
 				
 				for(String id:ids){
 					List<String> newids = dedup.retrieveEuropeanaIDFromOld(id, request.getCollection().getMnemonic());
 					
 					for(String newid : newids){
 						value.deletioncandidates.remove(newid);
 						dedup.createUpdateIdStatus(id,newid,request.getCollection().getMnemonic(),rdfstring,LookupState.INCOMPATIBLE_XML_CONTENT);
 					}
 				}
 				
 				discarded ++;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * @param mdr
 	 * @param dedupres
 	 * @return
 	 * @throws StorageEngineException
 	 * @throws JiBXException
 	 */
 	@SuppressWarnings("unchecked")
 	private MetaDataRecord<I> processrecord(MetaDataRecord<I> mdr,
 			DeduplicationResult dedupres,Status status) throws JiBXException,
 			StorageEngineException {
 
 		if (mdr == null) {
 			mdr = storage.createMetaDataRecord(request.getCollection(),
 					dedupres.getDerivedRecordID());
 		} else {
 			// Remove the previous ingestion date
 			mdr.deleteValues(EuropeanaModelRegistry.UIMINGESTIONDATE);
 			// Remove the previous instances of EDM Records
 			mdr.deleteValues(EuropeanaModelRegistry.EDMRECORD);
 			// Remove the previous registered links
 			mdr.deleteValues(EuropeanaModelRegistry.EUROPEANALINK);
             // Remove all previous STATUS information
 			mdr.deleteValues(EuropeanaModelRegistry.STATUS);
 		}
 
 		mdr.addValue(EuropeanaModelRegistry.UIMINGESTIONDATE,
 				new Date().toString());
 
 		mdr.addValue(EuropeanaModelRegistry.EDMRECORD, dedupres.getEdm());
 
 		// Add Links to be checked values here
 		addLinkcheckingValues(unmarshall(dedupres.getEdm()), mdr);
 		
 		mdr.addValue(EuropeanaModelRegistry.STATUS, status);
 		
 		storage.updateMetaDataRecord(mdr);
 
 		return mdr;
 	}
 
 	/**
 	 * @param edm
 	 * @return
 	 * @throws JiBXException
 	 */
 	private RDF unmarshall(String edm) throws JiBXException {
 		StringReader reader = new StringReader(edm);
 		RDF rdf = (RDF) mctx.unmarshalDocument(reader, "UTF-8");
 		return rdf;
 	}
 
 	/**
 	 * Extracts the locations of links from the imported JIBX representation of
 	 * EDM and registers the appropriate values
 	 * 
 	 * @param validedmrecord
 	 */
 	private void addLinkcheckingValues(RDF validedmrecord, MetaDataRecord<I> mdr) {
 
 		List<Aggregation> aggregations = validedmrecord.getAggregationList();
 		List<WebResourceType> webresources = validedmrecord
 				.getWebResourceList();
 
 		Set<String> existingLinks = Collections
 				.synchronizedSet(new HashSet<String>());
 
 		if (aggregations != null) {
 			for (Aggregation aggregation : aggregations) {
 
 				List<HasView> edm_has_view = aggregation.getHasViewList();
 
 				if (edm_has_view != null) {
 					for (HasView view : edm_has_view) {
 						String hasView = view.getResource();
 						addLink(hasView, mdr, existingLinks, true);
 					}
 				}
 
 				if (aggregation.getIsShownAt() != null) {
 					String isShownAt = aggregation.getIsShownAt().getResource();
 					addLink(isShownAt, mdr, existingLinks, false);
 				}
 
 				if (aggregation.getIsShownBy() != null) {
 					String isShownBy = aggregation.getIsShownBy().getResource();
 					addLink(isShownBy, mdr, existingLinks, true);
 				}
 
 				if (aggregation.getObject() != null) {
 					String theObject = aggregation.getObject().getResource();
 					addLink(theObject, mdr, existingLinks, true);
 				}
 			}
 		}
 
 		if (webresources != null) {
 			for (WebResourceType wrtype : webresources) {
 				String about = wrtype.getAbout();
 				addLink(about, mdr, existingLinks, true);
 			}
 		}
 
 	}
 
 	/**
 	 * Attach a EuropeanaLink object to the specific MDR (if it is not declared
 	 * twice in the EDM record context)
 	 * 
 	 */
 	private void addLink(String link, MetaDataRecord<I> mdr,
 			Set<String> existingLinks, boolean isCacheable) {
 
 		if (!existingLinks.contains(link)) {
 			EuropeanaLink eulink = new EuropeanaLink();
 			eulink.setCacheable(isCacheable);
 			eulink.setLinkStatus(LinkStatus.NOT_CHECKED);
 			eulink.setUrl(link);
 			mdr.addValue(EuropeanaModelRegistry.EUROPEANALINK, eulink);
 			existingLinks.add(link);
 		}
 
 	}
 
 	/**
 	 * Returns the loading state
 	 * 
 	 * @return the loading state
 	 */
 	public boolean isFinished() {
 		return !zipiterator.hasNext();
 	}
 
 	/**
 	 * Returns the expected records
 	 * 
 	 * @return the expected records
 	 */
 	public int getExpectedRecordCount() {
 
 		return this.expectedRecords;
 	}
 
 	/**
 	 * @return the created
 	 */
 	public int getCreated() {
 		return created;
 	}
 
 	/**
 	 * @return the updated
 	 */
 	public int getUpdated() {
 		return updated;
 	}
 	
 	
 	/**
 	 * @return the generated
 	 */
 	public int getGenerated() {
 		return generated;
 	}
 
 	/**
 	 * @return the discarded
 	 */
 	public int getDiscarded() {
 		return discarded;
 	}
 	
 	/**
 	 * @return the omitted
 	 */
 	public int getOmitted() {
 		return omitted;
 	}
 	
 	
 	/**
 	 * Finalizes the current object and make its fields eligible for garbage
 	 * collection
 	 */
 	public void close() {
 		this.expectedRecords = 0;
 		this.zipiterator = null;
 		this.storage = null;
 		this.request = null;
 		this.monitor = null;
 	}
 
 }
