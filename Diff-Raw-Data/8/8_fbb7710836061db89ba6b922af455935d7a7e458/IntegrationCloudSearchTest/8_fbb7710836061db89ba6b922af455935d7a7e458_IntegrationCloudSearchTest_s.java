 package com.amazonaws.services.cloudsearch.service;
 
 import com.amazonaws.services.cloudsearch.AmazonCloudSearchClient;
 import com.amazonaws.services.cloudsearch.common.json.JsonUtils;
 import com.amazonaws.services.cloudsearch.model.DescribeDomainsResult;
 import com.amazonaws.services.cloudsearch.model.DomainStatus;
 import com.amazonaws.services.cloudsearch.model.sdf.Field;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAdd;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentFormat;
 import com.amazonaws.services.cloudsearch.model.search.SearchResponse;
 import com.amazonaws.services.cloudsearch.model.upload.UploadResponse;
 import com.google.common.collect.ImmutableList;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.expression.ExpressionParser;
 import org.springframework.expression.spel.standard.SpelExpressionParser;
 import org.springframework.expression.spel.support.StandardEvaluationContext;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.*;
 
 /**
  * A suite of tests for calling the TIA REST endpoint.
  * 
  * @author Phillip Read
  */
 @ActiveProfiles("development")
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {
 		"classpath*:META-INF/spring/root-context.xml",
 		"classpath*:META-INF/spring/applicationContext*.xml" })
 public class IntegrationCloudSearchTest {
 
     /** The log. */
     private static Log LOG = LogFactory.getLog(IntegrationCloudSearchTest.class);
 
     @Autowired(required=true)
     @Qualifier("cloudSearchClient")
     private AmazonCloudSearchClient cloudSearch;
 
     @Autowired(required=true)
     @Qualifier("cloudSearchService")
     private CloudSearchService cloudSearchService;
 
     private static ExpressionParser parser;
     private static final String DOMAIN_MOVIES = "imdb-movies";
 
     @Before
     public void initObjects() {
         parser = new SpelExpressionParser();
     }
 
 	/**
 	 * Find movies matching query.
 	 */
 	@Test
     public void cloudSearch() {
 
         DescribeDomainsResult domains = cloudSearch.describeDomains();
         StandardEvaluationContext context = new StandardEvaluationContext(domains);
         List<DomainStatus> matches = (List<DomainStatus>) parser.parseExpression("DomainStatusList.?[DomainName == '" + DOMAIN_MOVIES + "']").getValue(context);
 
         if (!matches.isEmpty()) {
 
             String query = "Star Wars";
             String returnFields = "actor%2Cdirector%2Ctitle%2Cyear%2Ctext_relevance";
             String facet = "genre";
 
             LOG.info("Number of searchable documents for " + matches.get(0).getDomainName() + " is " + matches.get(0).getNumSearchableDocs());
             for(int start = 0; start < 10000; start=start+10) {
 
                 SearchResponse searchResponse = cloudSearchService.cloudSearchRead(query, returnFields, start, facet);
 
                 int count = searchResponse.getFound().getCount();
                 int hits = searchResponse.getFound().getHits().size();
                System.out.println("Start position " + start + ". Found " + hits + " of " + count + " matches for query [" + query + "]");
                 if ((start + 10) > count || hits == 0) {
                     break;
                 }
 
             }
 
         }
 
     }
 
     @Test
     public void cloudSearchActor() {
 
         DescribeDomainsResult domains = cloudSearch.describeDomains();
         StandardEvaluationContext context = new StandardEvaluationContext(domains);
         List<DomainStatus> matches = (List<DomainStatus>) parser.parseExpression("DomainStatusList.?[DomainName == '" + DOMAIN_MOVIES + "']").getValue(context);
 
         if (!matches.isEmpty()) {
 
             LOG.info("Number of searchable documents for " + matches.get(0).getDomainName() + " is " + matches.get(0).getNumSearchableDocs());
             int start = 0;
             String query = "(and actor:'Carrie Fisher' genre:'Horror')";
 
             String returnFields = "actor%2Cdirector%2Ctitle%2Cyear%2Ctext_relevance";
             String facet = "genre";
             SearchResponse searchResponse = cloudSearchService.cloudSearchBooleanQuery(query, returnFields, start, facet);
 
             int count = searchResponse.getFound().getCount();
             int hits = searchResponse.getFound().getHits().size();
            System.out.println("Start position " + start + ". Found " + hits + " of " + count + " matches for query [" + query + "]");
         }
 
     }
 
     @Test
     public void cloudSearchBooleanQuery() {
 
         DescribeDomainsResult domains = cloudSearch.describeDomains();
         StandardEvaluationContext context = new StandardEvaluationContext(domains);
         List<DomainStatus> matches = (List<DomainStatus>) parser.parseExpression("DomainStatusList.?[DomainName == '" + DOMAIN_MOVIES + "']").getValue(context);
 
         if (!matches.isEmpty()) {
 
             String query = "(and director:'Lucas|Spielberg' (not actor:'\"Ford, Harrison\"'))";
             String returnFields = "actor%2Cdirector%2Ctitle%2Cyear%2Ctext_relevance";
             String facet = "genre";
 
             LOG.info("Number of searchable documents for " + matches.get(0).getDomainName() + " is " + matches.get(0).getNumSearchableDocs());
             for(int start = 0; start < 10000; start=start+10) {
                 SearchResponse searchResponse = cloudSearchService.cloudSearchBooleanQuery(query, returnFields, start, facet);
                 int count = searchResponse.getFound().getCount();
                 int hits = searchResponse.getFound().getHits().size();
                System.out.println("Start position " + start + ". Found " + hits + " of " + count + " matches for query [" + query + "]");
 
                 if ((start + 10) > count || hits == 0) {
                     break;
                 }
             }
         }
 
     }
 
     @Test
     public void cloudSearchPostSdf() {
 
         DescribeDomainsResult domains = cloudSearch.describeDomains();
         StandardEvaluationContext context = new StandardEvaluationContext(domains);
         List<DomainStatus> matches = (List<DomainStatus>) parser.parseExpression("DomainStatusList.?[DomainName == '" + DOMAIN_MOVIES + "']").getValue(context);
 
         if (!matches.isEmpty()) {
 
             SearchDocumentFormat item = testSdfDocument();
 
             LOG.info("SDF Batch Upload Request : " + JsonUtils.marshal(item.getSearchDocumentAdds()));
             UploadResponse result = cloudSearchService.batch(item);
             LOG.info("SDF Batch Upload Response : " + JsonUtils.marshal(result));
 
             assertNotNull(result);
             assertEquals("success", result.getStatus());
             assertTrue(result.getAdds() == 2);
             assertTrue(result.getDeletes() == 0);
             assertTrue(result.getErrors().size() == 0);
             assertTrue(result.getWarnings().size() == 0);
         }
     }
 
     private SearchDocumentFormat testSdfDocument() {
 
         SearchDocumentFormat item = new SearchDocumentFormat();
         List<SearchDocumentAdd> documents = new ArrayList<SearchDocumentAdd>();
 
         Field fields = new Field();
         fields.setTitle("The Seeker: The Dark Is Rising");
         fields.setDirector("Cunningham, David L.");
         fields.setYear(2007);
         fields.setGenre(ImmutableList.of("Adventure","Drama","Fantasy","Thriller"));
         fields.setActor(ImmutableList.of("McShane, Ian","Eccleston, Christopher","Conroy, Frances","Ludwig, Alexander","Crewson, Wendy","Warner, Amelia","Cosmo, James","Hickey, John Benjamin","Piddock, Jim","Lockhart, Emma"));
 
         Field fields2 = new Field();
         fields2.setTitle("Emma");
         fields2.setDirector("McGrath, Douglas");
         fields2.setYear(1996);
         fields2.setGenre(ImmutableList.of("Comedy","Romance"));
         fields2.setActor(ImmutableList.of("Paltrow, Gwyneth","Cumming, Alan","Collette, Toni","Northam, Jeremy","Scacchi, Greta","Cosmo, James","Thompson, Sophie","Law, Phyllida","Boardman, Lee","Byron, Kathleen","Hawthorne, Denys"));
 
         SearchDocumentAdd z = new SearchDocumentAdd();
         z.setId("tt0484562");
         z.setVersion("2");
         z.setLang("en");
         z.setFields(fields);
 
         SearchDocumentAdd z2 = new SearchDocumentAdd();
         z2.setId("tt0116191");
         z2.setVersion("2");
         z2.setLang("en");
         z2.setFields(fields2);
 
         documents.add(z);
         documents.add(z2);
 
         item.setSearchDocumentAdds(documents);
         return item;
     }
 }
