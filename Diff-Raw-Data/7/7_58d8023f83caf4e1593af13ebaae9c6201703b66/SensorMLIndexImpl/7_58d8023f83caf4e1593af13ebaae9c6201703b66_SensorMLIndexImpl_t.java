 package net.prep.solr.sensorml.impl;
 
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.common.SolrInputDocument;
 
 import net.opengis.sensorML.x101.AbstractProcessType;
 import net.opengis.sensorML.x101.KeywordsDocument.Keywords;
 import net.opengis.sensorML.x101.KeywordsDocument.Keywords.KeywordList;
 import net.opengis.sensorML.x101.SensorMLDocument.SensorML;
 import net.prep.solr.sensorml.SensorMLIndex;
 
 public class SensorMLIndexImpl implements SensorMLIndex {
 	private String url = "http://localhost:8983/solr";
 
 	public void index(SensorML sensor) throws Exception {
 		// TODO Auto-generated method stub
 		SolrServer server = new HttpSolrServer(url);
 		SolrInputDocument solrdoc = new SolrInputDocument();
 		server.deleteByQuery( "*:*" );
 		/*
 		 * Simple fields added
 		 */
 
 		// Get the keywords list first
 		AbstractProcessType process = sensor.getMemberArray()[0].getProcess();
 
 		Keywords[] keywords = process.getKeywordsArray();
 		KeywordList keywordList = (keywords[0].getKeywordList());
 		String [] str_keywords = keywordList.getKeywordArray();
 		for(int i=0;i<str_keywords.length;i++)
 			solrdoc.addField("keyword",str_keywords[i]);
 		String id = (process.getIdentificationArray()[0].getIdentifierList().getIdentifierArray()[0].getTerm().getValue());
		String beginPosition  = (process.getValidTime().getTimePeriod().getBeginPosition().getStringValue());
		String endPosition = process.getValidTime().getTimePeriod().getEndPosition().getStringValue();
 		solrdoc.addField("id",id);
		solrdoc.addField("beginPosition", beginPosition);
		solrdoc.addField("endPosition", endPosition);
 		server.add(solrdoc);
 		server.commit();
 	}
 }
