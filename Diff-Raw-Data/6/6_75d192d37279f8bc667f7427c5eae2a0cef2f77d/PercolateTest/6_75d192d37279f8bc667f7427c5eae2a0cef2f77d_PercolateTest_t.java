 package com.za.elasticsearch.progfun;
 
 import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
 import static org.elasticsearch.index.query.FilterBuilders.boolFilter;
 import static org.elasticsearch.index.query.FilterBuilders.geoDistanceFilter;
 import static org.elasticsearch.index.query.FilterBuilders.rangeFilter;
 import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
 import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 
 import java.io.IOException;
 import java.util.Date;
 
 import org.elasticsearch.action.percolate.PercolateResponse;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.common.unit.DistanceUnit;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.index.query.FilterBuilder;
 import org.elasticsearch.node.Node;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class PercolateTest {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(PercolateTest.class);
 
     private Node node;
     private Client client;
 
     @Before
     public void setUp() throws IOException {
         node = nodeBuilder().local(true).node();
         client = node.client();
         addGeoTypeMapping();
     }
 
     @Test
     public void indexAndSearch() throws IOException {
 
         Household household = createHouseholdToIndex();
         client.prepareIndex("households", "household", household.getId())
                 .setSource(toJson(household))
                 .setRefresh(true)  // --> refresh the index after the operation so that the document appears in search results immediately
                 .execute().actionGet();
 
         SearchResponse response = client.prepareSearch("households")
                 .setTypes("household")
                 .setFilter(filterBuilder())
                 .setExplain(true)
                 .execute().actionGet();
 
         assertThat(response.getHits().getTotalHits(), is(1L));
 
     }
 
     @Test
     public void percolate() throws IOException {
         // register the query
         client.prepareIndex("households", "_percolator", "myQuery")
                 .setSource(jsonBuilder()
                         .startObject()
                             .field("query", constantScoreQuery(filterBuilder()))
                         .endObject())
                 .setRefresh(true)  // // Needed when the query shall be available immediately
                 .execute().actionGet();
 
         // the document to percolate
         XContentBuilder docBuilder = jsonBuilder()
                 .startObject()
                     .field("doc")
                         .startObject()
                             .startObject("location")
                                 .field("lat", 52.10)
                                 .field("lon", 5.12)
                             .endObject()
                             .field("price", 290000)
                         .endObject()
                 .endObject();
 
         LOGGER.info("Percolate request source:" + docBuilder.string());
 
         // check a document against a registered query
         PercolateResponse response = client.preparePercolate()
                 .setIndices("households")
                 .setDocumentType("household")
                 .setSource(docBuilder)
                 .execute().actionGet();
 
         assertThat(response.getCount(), is(1L));
         assertThat(response.getMatches()[0].getIndex().toString(), is("households"));
         assertThat(response.getMatches()[0].getId().toString(), is("myQuery"));
 
     }
 
     private FilterBuilder filterBuilder() {
         return boolFilter()
                 .must(rangeFilter("price").from("250000").to("300000"))
                 .must(geoDistanceFilter("location")
                         .point(52.09, 5.11)
                         .distance(5, DistanceUnit.KILOMETERS)
                 );
     }
 
 
     private Household createHouseholdToIndex() {
         Household household = new Household();
         household.setId("1");
         household.setLat(52.09179);
         household.setLon(5.11457);
         household.setPostDate(new Date());
         household.setPrice(280000);
         household.setDescription("Great house");
         return household;
     }
 
     private String toJson(Household household) throws IOException {
         return jsonBuilder()
                 .startObject()
                     .startObject("location")
                         .field("lat", household.getLat())
                         .field("lon", household.getLon())
                     .endObject()
                     .field("postDate", household.getPostDate())
                     .field("price", household.getPrice())
                     .field("description", household.getDescription())
                 .endObject()
                 .string();
     }
 
     private void addGeoTypeMapping() throws IOException {
         XContentBuilder mapping = jsonBuilder()
                 .startObject()
                     .startObject("household")
                         .startObject("properties")
                             .startObject("location")
                                 .field("type", "geo_point")
                             .endObject()
                            .startObject("price")
                                 .field("type", "integer")
                            .endObject()
                        .endObject()
                     .endObject()
                 .endObject();
 
         client.admin().indices().prepareCreate("households").addMapping("household", mapping).execute().actionGet();
     }
 
 
     @After
     public void tearDown() {
         client.admin().indices().prepareDelete("households").execute().actionGet();
 
         node.close();
     }
 
 }
