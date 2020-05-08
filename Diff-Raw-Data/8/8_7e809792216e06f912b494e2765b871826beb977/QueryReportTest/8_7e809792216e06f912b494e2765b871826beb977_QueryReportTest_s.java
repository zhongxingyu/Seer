 package edu.umich.eecs.tac.props;
 
 import org.junit.Test;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.text.ParseException;
 
 import se.sics.isl.transport.BinaryTransportWriter;
 import se.sics.isl.transport.BinaryTransportReader;
 import static edu.umich.eecs.tac.props.TransportableTestUtils.getBytesForTransportable;
 import static edu.umich.eecs.tac.props.TransportableTestUtils.readFromBytes;
 
 public class QueryReportTest {
     @Test
     public void testEmptyQueryReportEntry() {
         QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
         assertNotNull(entry);
     }
 
     @Test
     public void testEmptyQueryReport() {
         QueryReport report = new QueryReport();
         assertNotNull(report);
         assertFalse(report.containsQuery(new Query()));
     }
 
     @Test
     public void testBasicQueryReportEntry() {
         QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
         assertNotNull(entry);
 
         assertNull(entry.getQuery());
         entry.setQuery(new Query());
         assertNotNull(entry.getQuery());
 
         assertEquals(entry.getImpressions(), 0);
         entry.setImpressions(1);
         assertEquals(entry.getImpressions(), 1);
 
         assertEquals(entry.getClicks(), 0);
         entry.setClicks(2);
         assertEquals(entry.getClicks(), 2);
 
         assertEquals(entry.getCPC(), 0.0);
         entry.setCost(3.0);
         assertEquals(entry.getCost(), 3.0);
         assertEquals(entry.getCPC(), 1.5);
 
         assertEquals(entry.getPosition(), 0.0);
         entry.addPosition(4.0);
         assertEquals(entry.getPosition(), 4.0);
 
         assertEquals(entry.getTransportName(), "QueryReportEntry");
        assertEquals(entry.toString(), "((Query (null,null)) impr: 1 clicks: 2 pos: 4.000000 cpc: 1.500000)");
 
     }
 
     @Test
     public void testValidTransportOfQueryReportEntry() throws ParseException {
         BinaryTransportWriter writer = new BinaryTransportWriter();
         BinaryTransportReader reader = new BinaryTransportReader();
         reader.setContext(new AAInfo().createContext());
 
 
         QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
         entry.setQuery(new Query());
         entry.setImpressions(1);
         entry.setClicks(2);
         entry.setCost(3.0);
         entry.addPosition(4.0);
 
         byte[] buffer = getBytesForTransportable(writer, entry);
         QueryReport.QueryReportEntry received = readFromBytes(reader, buffer, "QueryReportEntry");
 
 
         assertNotNull(entry);
         assertNotNull(received);
 
         assertEquals(received.getQuery(), new Query());
         assertEquals(received.getImpressions(), 1);
         assertEquals(received.getClicks(), 2);
         assertEquals(received.getCost(), 3.0);
         assertEquals(received.getCPC(), 1.5);
         assertEquals(received.getPosition(), 4.0);
         assertEquals(entry.getTransportName(), "QueryReportEntry");
 
 
     }
 
     @Test
     public void testValidTransportOfNullQueryReportEntry() throws ParseException {
         BinaryTransportWriter writer = new BinaryTransportWriter();
         BinaryTransportReader reader = new BinaryTransportReader();
         reader.setContext(new AAInfo().createContext());
 
 
         QueryReport.QueryReportEntry entry = new QueryReport.QueryReportEntry();
         entry.setImpressions(1);
         entry.setClicks(2);
         entry.setCost(3.0);
         entry.addPosition(4.0);
 
         byte[] buffer = getBytesForTransportable(writer, entry);
         QueryReport.QueryReportEntry received = readFromBytes(reader, buffer, "QueryReportEntry");
 
 
         assertNotNull(entry);
         assertNotNull(received);
 
         assertNull(received.getQuery());
         assertEquals(received.getImpressions(), 1);
         assertEquals(received.getClicks(), 2);
         assertEquals(received.getCost(), 3.0);
         assertEquals(received.getCPC(), 1.5);
         assertEquals(received.getPosition(), 4.0);
 
         assertEquals(received.getTransportName(), "QueryReportEntry");
        assertEquals(received.toString(), "(null impr: 1 clicks: 2 pos: 4.000000 cpc: 1.500000)");
     }
 
     @Test
     public void testValidTransportOfQueryReport() throws ParseException {
         BinaryTransportWriter writer = new BinaryTransportWriter();
         BinaryTransportReader reader = new BinaryTransportReader();
         reader.setContext(new AAInfo().createContext());
 
 
         QueryReport report = new QueryReport();
         report.setClicks(new Query(), 2);
         report.lock();
 
         byte[] buffer = getBytesForTransportable(writer, report);
         QueryReport received = readFromBytes(reader, buffer, "QueryReport");
 
 
         assertNotNull(report);
         assertNotNull(received);
         assertEquals(report.size(), 1);
         assertEquals(received.size(), 1);
         assertEquals(received.getClicks(new Query()), 2);
 
         assertEquals(report.getTransportName(), "QueryReport");
 
         buffer = getBytesForTransportable(writer, new QueryReport());
         received = readFromBytes(reader, buffer, "QueryReport");
         assertFalse(received.isLocked());
 
     }
 
     @Test(expected = NullPointerException.class)
     public void testAddQueryToQueryReport() {
         QueryReport report = new QueryReport();
         assertEquals(report.size(), 0);
 
         report.addQuery(new Query());
         assertEquals(report.size(), 1);
         assertTrue(report.containsQuery(new Query()));
 
         Query q = new Query();
         q.setComponent("c1");
         report.addQuery(q, 10, 100, 40.0, 4.0);
         assertEquals(report.size(), 2);
         assertTrue(report.containsQuery(q));
 
         report.addQuery(null);
     }
 
     @Test(expected = NullPointerException.class)
     public void testSetConversions() {
         QueryReport report = new QueryReport();
         assertEquals(report.size(), 0);
 
         report.setImpressions(new Query(), 1);
         assertEquals(report.size(), 1);
         assertTrue(report.containsQuery(new Query()));
         assertEquals(report.getImpressions(new Query()), 1);
 
         report.setImpressions(new Query(), 3);
         assertEquals(report.size(), 1);
         assertEquals(report.getImpressions(new Query()), 3);
 
         report.setImpressions(null, 2);
     }
 
     @Test(expected = NullPointerException.class)
     public void testSetRevenue() {
         QueryReport report = new QueryReport();
         assertEquals(report.size(), 0);
 
         report.setCost(new Query(), 1.0);
         assertEquals(report.size(), 1);
         assertTrue(report.containsQuery(new Query()));
         assertEquals(report.getCPC(new Query()), Double.POSITIVE_INFINITY);
 
 
         report.setCost(new Query(), 3.0);
         assertEquals(report.size(), 1);
         assertEquals(report.getCPC(new Query()), Double.POSITIVE_INFINITY);
 
         report.setCost(null, 2);
     }
 
     @Test
     public void testSalesReportGettersForEmptyQueries() {
         QueryReport report = new QueryReport();
         assertEquals(report.size(), 0);
 
         assertEquals(report.getImpressions(null), 0);
         assertEquals(report.getClicks(null), 0);
         assertEquals(report.getCost(null), 0.0);
         assertEquals(report.getCPC(null), Double.NaN);
         assertEquals(report.getPosition(null), Double.NaN);
     }
 
     @Test
     public void testQueryReportToString() {
         QueryReport report = new QueryReport();
         report.addQuery(new Query());
        assertEquals(report.toString(), "(QueryReport ((Query (null,null)) impr: 0 clicks: 0 pos: NaN cpc: NaN))");
     }
 
     @Test
     public void testQueryReportAdders() {
         QueryReport report = new QueryReport();
 
         Query query = new Query();
 
         assertEquals(report.getImpressions(query), 0);
         assertEquals(report.getCPC(query), Double.NaN);
         report.addImpressions(query, 2);
         report.addClicks(query, 3);
         assertEquals(report.getImpressions(query), 2);
         assertEquals(report.getClicks(query), 3);
         report.addImpressions(query, 2);
         report.addClicks(query, 3);
         assertEquals(report.getImpressions(query), 4);
         assertEquals(report.getClicks(query), 6);
 
         report = new QueryReport();
         report.addClicks(query, 3);
         report.addImpressions(query, 10);
         assertEquals(report.getClicks(query), 3);
         assertEquals(report.getImpressions(query), 10);
     }
 
     @Test
     public void testPosition() {
         QueryReport instance = new QueryReport();
         instance.setPositionSum(new Query(), 0);
         instance.setPositionSum(0, 3.2);
         assertEquals(instance.getPosition(0), Double.POSITIVE_INFINITY);
 
         Query q = new Query();
         q.setComponent("c1");
         instance.setPositionSum(q, 4.0);
         instance.setImpressions(q, 1);
         assertEquals(instance.getPosition(q), 4.0);
 
         instance.setPositionSum(q, 2.0);
         assertEquals(instance.getPosition(q), 2.0);
     }
 
     @Test
     public void testClicks() {
         QueryReport instance = new QueryReport();
         Query query = new Query();
         query.setComponent("c1");
         instance.setClicks(query, 3);
         assertEquals(instance.getClicks(query), 3);
         instance.setClicks(query, 4);
         assertEquals(instance.getClicks(query), 4);
     }
 }
