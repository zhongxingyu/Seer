 package org.bahmni.feed.openerp.server;
 
 import org.bahmni.feed.openerp.repository.DbEventRecordCreator;
 import org.bahmni.feed.openerp.utils.MVCTestUtils;
 import org.ict4h.atomfeed.Configuration;
 import org.ict4h.atomfeed.IntegrationTest;
 import org.ict4h.atomfeed.client.service.AtomFeedClient;
 import org.ict4h.atomfeed.jdbc.JdbcConnectionProvider;
 import org.ict4h.atomfeed.jdbc.JdbcUtils;
 import org.ict4h.atomfeed.server.domain.EventRecord;
 import org.ict4h.atomfeed.server.exceptions.AtomFeedRuntimeException;
 import org.ict4h.atomfeed.server.repository.jdbc.AllEventRecordsJdbcImpl;
 import org.ict4h.atomfeed.spring.resource.EventResource;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.web.server.ResultMatcher;
 
 import java.net.URISyntaxException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import static org.junit.matchers.JUnitMatchers.containsString;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath*:applicationContext-openerpTest.xml"})
 public class AtomFeedServerIT extends IntegrationTest {
 
     private DbEventRecordCreator recordCreator;
    private Connection connection;
     private OpenERPAllEventRecordsJdbcImpl eventRecords;
     private AtomFeedClient atomFeedClient;
     String contents ="  <entry>\n" +
             "    <title>Hello, DiscWorld6</title>\n" +
             "    <category term=\"product\" />\n" +
             "    <id>tag:atomfeed.ict4h.org:062df21f-62e7-405b-9c80-93d7e7a7b4f6</id>\n" +
             "    <updated>2013-07-02T03:16:30Z</updated>\n" +
             "    <content type=\"application/vnd.atomfeed+xml\"><![CDATA[{\"category\": \"All products\", \"list_price\": 2, \"id\": 215, \"manufacturer\": \"cipla3\"}]]></content>\n" +
             "  </entry>\n" +
             "  <entry>\n" +
             "    <title>Hello, DiscWorld7</title>\n" +
             "    <category term=\"product\" />\n" +
             "    <id>tag:atomfeed.ict4h.org:4db7d280-8f64-4470-9d2b-180316034753</id>\n" +
             "    <updated>2013-07-02T03:16:30Z</updated>\n" +
             "    <content type=\"application/vnd.atomfeed+xml\"><![CDATA[{\"category\": \"All products\", \"list_price\": 2, \"id\": 215, \"manufacturer\": \"cipla3\"}]]></content>\n" +
             "  </entry>\n";
 
     @Autowired
     private EventResource eventResource;
     private JdbcConnectionProvider jdbcConnectionProvider;
 
     @Before
     public void before() throws SQLException {
        connection = getConnection();
        eventRecords = new OpenERPAllEventRecordsJdbcImpl(getProvider(connection));
         recordCreator = new DbEventRecordCreator(eventRecords);
     }
 
     @After
     public void after() throws SQLException {
         Statement statement = connection.createStatement();
         statement.execute("TRUNCATE public.event_records  RESTART IDENTITY;");
         statement.close();
         connection.close();
     }
 
 //    @Test
 //    public void shouldReadEventsCreatedEvents() throws URISyntaxException, SQLException {
 //        List<EventRecord> events = createEvents(7, "Hello, DiscWorld");
 //        final int[] counter = {0};
 //        URI uri = new URI("http://localhost:8080/feed/recent");
 //        AllMarkersInMemoryImpl allMarkersInMemoryImpl = new AllMarkersInMemoryImpl();
 //        allMarkersInMemoryImpl.put(uri, events.get(4).getTagUri().toString(), new URI("http://localhost:8080/feed/1"));
 //        atomFeedClient = new AtomClientFactory().create(allMarkersInMemoryImpl, new AllFailedEventsInMemoryImpl());
 //        atomFeedClient.processEvents(uri, new EventWorker() {
 //            @Override
 //            public void process(Event event) {
 //                counter[0] += 1;
 //            }
 //        });
 //        assertEquals(2, counter[0]);
 //    }
    @Test
     public void shouldPublishFeedOfEvents() throws Exception, URISyntaxException {
         List<EventRecord> events = createEvents(7, "Hello, DiscWorld");
        String responseAsString = null;
         MVCTestUtils.mockMvc(eventResource)
                 .perform(get("/feed/product/recent"))
                 .andExpect(compareContent());
     }
 
     private ResultMatcher compareContent() {
         return content().string(containsString("<title>Hello, DiscWorld7</title>"));
     }
 
     private List<EventRecord> createEvents(int numberOfEventsToCreate, String titleTemplate) throws URISyntaxException, SQLException {
         List<EventRecord> records = new ArrayList<EventRecord>();
         int index = 1;
         do {
             records.add(createOneEvent(String.format("%s%s", titleTemplate, index), String.format("%s%s", "http://google.com?q=", index)));
             index++;
         } while (index <= numberOfEventsToCreate);
         return records;
     }
 
     private EventRecord createOneEvent(String title, String url) throws URISyntaxException, SQLException {
         EventRecord eventRecord = recordCreator.create(UUID.randomUUID().toString(), title, url, "{\"category\": \"All products\", \"list_price\": 2, \"id\": 215, \"manufacturer\": \"cipla3\"}");
         return eventRecord;
     }
 
 
     class OpenERPAllEventRecordsJdbcImpl  extends  AllEventRecordsJdbcImpl{
 
         public OpenERPAllEventRecordsJdbcImpl(JdbcConnectionProvider provider) {
             super(provider);
             jdbcConnectionProvider = provider;
         }
 
         @Override
         public void add(EventRecord eventRecord) {
             Connection connection;
             PreparedStatement stmt = null;
             try {
                 connection = jdbcConnectionProvider.getConnection();
                 String insertSql = String.format("insert into %s (uuid, title, uri, object,category,timestamp) values (?, ?, ?, ?,?,?)",
                         JdbcUtils.getTableName(Configuration.getInstance().getSchema(), "event_records"));
                 stmt = connection.prepareStatement(insertSql);
                 stmt.setString(1, eventRecord.getUuid());
                 stmt.setString(2, eventRecord.getTitle());
                 stmt.setString(3, eventRecord.getUri());
                 stmt.setString(4, eventRecord.getContents());
                 stmt.setString(5, eventRecord.getCategory());
                 stmt.setTimestamp(6, new Timestamp(eventRecord.getTimeStamp().getTime()));
                 stmt.executeUpdate();
             } catch (SQLException e) {
                 throw new AtomFeedRuntimeException(e);
             } finally {
                 closeAll(stmt, null);
             }
         }
         private void closeAll(PreparedStatement stmt, ResultSet rs) {
             try {
                 if (rs != null) {
                     rs.close();
                 }
                 if (stmt != null) {
                     stmt.close();
                 }
             } catch (SQLException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
 
 
     }
 }
