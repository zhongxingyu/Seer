 package org.openmrs.module.chartsearch.solr;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.handler.dataimport.custom.IndexClearStrategies;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.chartsearch.ChartListItem;
 import org.openmrs.module.chartsearch.server.PatientInfo;
 import org.openmrs.module.chartsearch.server.StatisticsInfo;
 
 public class SolrSearch {
 	
	private ChartSearchIndexer indexer;
	private ChartSearchSearcher searcher;
 	
 	public SolrSearch() {
		indexer  = new ChartSearchIndexer();
		searcher = new ChartSearchSearcher();
 	}
 	
 	public void initiateServer(){
 		SolrSingleton.getInstance().getServer();
 	}
 	
 	public void indexPatientData(Integer personId) {
 		indexer.clearIndex(IndexClearStrategies.IDS.toString(), personId+"", 0, 0);
 		indexer.indexPatientData(personId);
 	}
 	
 	public PatientInfo getPatientInfo(Integer patientId) {
 		return indexer.getPatientInfo(patientId);
 	}
 	
 	public StatisticsInfo getStatistics() {
 		return indexer.getStatistics();
 	}
 	
 	public Integer clearIndex(String strategy, String ids, Integer maxPatients, Integer ago) {
 		return indexer.clearIndex(strategy, ids, maxPatients, ago);
 	}
 	
 	public int changeDaemonsCount(int count) {
 		return indexer.changeDaemonsCount(count);
 	}
 	
 	public Long getDocumentListCount(Integer patientId, String searchText){
 		try {
 			return searcher.getDocumentListCount(patientId, searchText);
 		} catch (Exception e) {
 			return (long) 0;
 		}
 	}
 	
 	public List<ChartListItem> search(Integer patientId,
 			String searchText, Integer start, Integer length){
 		try {
 			return searcher.getDocumentList(patientId, searchText, start, length);
 		} catch (Exception e) {
 			return new ArrayList<ChartListItem>();
 		}
 	}
 	
 	private <T> T getComponent(Class<T> clazz) {
 		List<T> list = Context.getRegisteredComponents(clazz);
 		if (list == null || list.size() == 0)
 			throw new RuntimeException("Cannot find component of " + clazz);
 		return list.get(0);
 	}
 	
 }
