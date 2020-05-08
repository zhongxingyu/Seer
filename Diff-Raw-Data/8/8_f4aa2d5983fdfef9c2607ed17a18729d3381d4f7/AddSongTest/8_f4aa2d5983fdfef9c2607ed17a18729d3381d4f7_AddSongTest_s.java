 /****************************************************************************
  * Copyright (c) 2010 Giorgio Sironi. All rights reserved.
  * This program and the accompanying materials are made available under 
  * the terms of the Eclipse Public License v1.0 which accompanies this 
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  ****************************************************************************/
 package it.polimi.chansonnier.test;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.client.solrj.impl.XMLResponseParser;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.PostMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 import com.meterware.httpunit.WebForm;
 
 public class AddSongTest extends AcceptanceTest {
 	public void testTheAddPageIsLoaded() throws Exception {
 		WebConversation wc = new WebConversation();
 		WebRequest     req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/add" );
 		WebResponse   resp = wc.getResponse( req );
         WebForm add = resp.getForms()[0];
         assertEquals("add", add.getAction());
         assertEquals("post", add.getMethod());
         String[] parameters = add.getParameterNames();
         assertEquals(1, parameters.length);
         assertEquals("link", parameters[0]);
         assertEquals(1, add.getSubmitButtons().length);
 	}
 	
 	public void testGivenAYouTubeLinkAddsItToTheLastIndexedListAndTheRelatedSongIsSearchableThroughSolr() throws Exception {
         String link = "http://www.youtube.com/watch?v=GMDd4on20Yg";
         WebResponse resp = addVideoLink(link);
 		// TODO insert redirect
		assertTrue(resp.getText().contains("Success"));
 
 		WebRequest req = new GetMethodWebRequest( "http://localhost:8080/chansonnier/last" );
 		assertWebPageContains(req, link, 300000);
         Thread.sleep(10000);
 		
 		SolrDocumentList docList = searchForSongs("*:*");
 	    assertEquals(1, docList.size());
 	    SolrDocument song = docList.get(0);
 	    assertEquals("Green Day", song.get("artist"));
 	    assertEquals("Boulevard of Broken Dreams", song.get("title"));
 	    assertTrue(((String) song.get("lyrics")).contains("I walk a lonely road"));
 	}
 	
 	public void testFixturesCanBeAddedWithThePushOfAButton() throws Exception {
 		WebRequest     req = new GetMethodWebRequest("http://localhost:8080/chansonnier/fixtures");
 		WebConversation wc = new WebConversation();
 		WebResponse   resp = wc.getResponse( req );
 		assertEquals(1, resp.getForms()[0].getButtons().length);
 		req = new PostMethodWebRequest( "http://localhost:8080/chansonnier/fixtures" );
 		resp = wc.getResponse( req );
 		Thread.sleep(15000);
 		SolrDocumentList result = searchForSongs("title:Hero");
 		assertEquals(1, result.size());
 		String id = (String) result.get(0).get("link");
 		WebRequest image = new GetMethodWebRequest("http://localhost:8080/chansonnier/attachment?id=" + id + "&name=image1");
 		resp = wc.getResponse(image);
 		assertEquals(200, resp.getResponseCode());
 	}
 	
 	private SolrDocumentList searchForSongs(String queryString) throws Exception {
 		String url = "http://localhost:8983/solr";
 		CommonsHttpSolrServer server = new CommonsHttpSolrServer( url );
 		server.setParser(new XMLResponseParser());
 	    SolrQuery query = new SolrQuery();
 	    query.setQuery(queryString);
 	    QueryResponse rsp = server.query( query );
 	    return rsp.getResults();
 	}
 }
