 package net.es.topology.common.converter.nml;
 
 import net.es.lookup.client.RegistrationClient;
 import net.es.lookup.client.SimpleLS;
 import net.es.lookup.common.exception.LSClientException;
 import net.es.lookup.common.exception.ParserException;
 import net.es.lookup.records.Record;
 import net.es.topology.common.config.JAXBConfig;
 import net.es.topology.common.config.sls.JsonClientProvider;
 import net.es.topology.common.records.ts.*;
 import net.es.topology.common.records.ts.utils.*;
 import net.es.topology.common.visitors.nml.DepthFirstTraverserImpl;
 import net.es.topology.common.visitors.nml.TraversingVisitor;
 import net.es.topology.common.visitors.sls.SLSTraverserImpl;
 import net.es.topology.common.visitors.sls.SLSTraversingVisitor;
 import net.es.topology.common.visitors.sls.TraversingVisitorProgressMonitorLoggingImpl;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.ogf.schemas.nml._2013._05.base.*;
 import org.ogf.schemas.nsi._2013._09.messaging.Message;
 import org.ogf.schemas.nsi._2013._09.topology.NSAType;
 import org.ogf.schemas.nsi._2013._09.topology.NsiServiceType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 /**
  * @author <a href="mailto:a.hassany@gmail.com">Ahmed El-Hassany</a>
  */
 public class SLSVisitorTest {
     private final String sLSConfigFile = getClass().getClassLoader().getResource("config/sls.json").getFile();
     private final Logger logger = LoggerFactory.getLogger(SLSVisitorTest.class);
     private String logGUID = null;
     private JsonClientProvider sLSConfig;
 
     /**
      * get the UUID of the current test case
      *
      * @return
      */
     public String getLogGUID() {
         return this.logGUID;
     }
 
     @Before
     public void setup() {
         // Make sure that each test case has it's own ID to make it easier to trace.
         this.logGUID = UUID.randomUUID().toString();
         this.sLSConfig = JsonClientProvider.getInstance();
         sLSConfig.setFilename(this.sLSConfigFile);
         sLSConfig.setLogGUID(this.getLogGUID());
     }
 
     @Test
     @Ignore
     public void testReceive() {
         String urn = "urn:ogf:network:example.org:2013:nsa";
         try {
             SimpleLS client = new SimpleLS("localhost", 8090);
             client.setRelativeUrl("lookup/records?ts-id=" + urn);
             client.connect();
             client.send();
             String resp = client.getResponse();
             List<Record> records = TSRecordFactory.toRecords(resp, getLogGUID());
             System.out.println(records.get(0).getRecordType());
             for (Record record : records) {
                 System.out.println(((NSA) record).getTopologies());
             }
         } catch (LSClientException e) {
             e.printStackTrace();
         } catch (ParserException e) {
             e.printStackTrace();
         }
     }
 
     @Test
     public void testTraverser() throws Exception {
         // Arrange
         String urn = "urn:ogf:network:example.org:2013:nsa1";
         String topoURN = "urn:ogf:network:example.org:2013:topo";
         SimpleLS client = sLSConfig.getClient();
         RecordsCache cache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
 
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
 
         // Register the first NSA
         NSA nsa1 = new NSA();
         nsa1.setId(urn);
         nsa1.setVersion("2013-05-30T09:30:10Z");
         nsa1.setName("nsa1");
         nsa1.setTopologies(new ArrayList<String>());
         nsa1.getTopologies().add(topoURN);
         registrationClient.setRecord(nsa1);
         registrationClient.register();
 
         Topology topology = new Topology();
         topology.setId(topoURN);
         topology.setName(topoURN);
         topology.setVersion("2013-05-30T09:30:10Z");
         topology.setIsAlias(new ArrayList<String>());
         topology.getIsAlias().add(topoURN);
         registrationClient.setRecord(topology);
         registrationClient.register();
 
         // Prepare the visitor
         SLSVisitor visitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), visitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
 
         // Act
         NSA receivedNSA = recordsCache.getNSA(urn);
         Assert.assertNotNull(receivedNSA);
         receivedNSA.accept(tv);
 
         // Assert
         Assert.assertTrue(visitor.getNsaTypeMap().containsKey(urn));
         NSAType topo = visitor.getNsaTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new org.ogf.schemas.nsi._2013._09.topology.ObjectFactory().createNSA(topo), System.out);
     }
 
     /**
      * Helper method to read NML/NSI XML then convert it and register it with sLS instance
      *
      * @param filename
      * @throws Exception
      */
     protected void registerSLS(String filename) throws Exception {
         logger.debug("event=SLSVisitorTest.registerSLS.start guid=" + getLogGUID());
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor visitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), visitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         Message msg = (Message) um.unmarshal(ss);
         msg.getBody().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
 
         logger.debug("event=SLSVisitorTest.registerSLS.end guid=" + getLogGUID());
 
     }
 
     @Test
     public void testVisitPort() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitPort.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-port.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<PortType> msg = (JAXBElement<PortType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:portTest";
 
         // Act
         Port received = recordsCache.getPort(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getPortTypeMap().containsKey(urn));
         PortType nmlObj = slsVisitor.getPortTypeMap().get(urn);
         //JAXBConfig.getMarshaller().marshal(new ObjectFactory().createPort(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitPort.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitPortGroup() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitPortGroup.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-port-group.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<PortGroupType> msg = (JAXBElement<PortGroupType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:portGroup";
 
         // Act
         PortGroup received = recordsCache.getPortGroup(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getPortGroupTypeMap().containsKey(urn));
         PortGroupType nmlObj = slsVisitor.getPortGroupTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new ObjectFactory().createPortGroup(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
 
         logger.debug("event=SLSVisitorTest.testVisitPortGroup.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitTopology() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitTopology.start guid=" + getLogGUID());
         // Load data to sLS
         registerSLS("xml-examples/example-message-topology.xml");
 
         // Prepare the sLS client
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor visitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), visitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:org";
         // Act
         Topology received = recordsCache.getTopology(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(visitor.getTopologyTypeMap().containsKey(urn));
         TopologyType topo = visitor.getTopologyTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new ObjectFactory().createTopology(topo), System.out);
     }
 
     @Test
     public void testVisitNSA() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitNSA.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-nsa.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<NSAType> msg = (JAXBElement<NSAType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.org:2013:nsatest";
 
         // Act
         NSA received = recordsCache.getNSA(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getNsaTypeMap().containsKey(urn));
         NSAType nmlObj = slsVisitor.getNsaTypeMap().get(urn);
         //JAXBConfig.getMarshaller().marshal(new org.ogf.schemas.nsi._2013._09.topology.ObjectFactory().createNSA(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
 
         logger.debug("event=SLSVisitorTest.testVisitNSA.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitNSIService() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitNSIService.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-nsi-service.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<NsiServiceType> msg = (JAXBElement<NsiServiceType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.com:2013:nsi-service-test";
 
         // Act
         NSIService received = recordsCache.getNSIService(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getNsiServiceTypeMap().containsKey(urn));
         NsiServiceType nmlObj = slsVisitor.getNsiServiceTypeMap().get(urn);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
 
         logger.debug("event=SLSVisitorTest.testVisitNSIService.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitNode() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitNode.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-node.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<NodeType> msg = (JAXBElement<NodeType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
        String urn = "urn:ogf:network:example.net:2013:nodeA";
 
         // Act
         Node received = recordsCache.getNode(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getNodeTypeMap().containsKey(urn));
         NodeType nmlObj = slsVisitor.getNodeTypeMap().get(urn);
        //JAXBConfig.getMarshaller().marshal(new ObjectFactory().createNode(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitNode.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitLink() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitLink.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-link.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<LinkType> msg = (JAXBElement<LinkType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:linkTest:XY";
 
         // Act
         Link received = recordsCache.getLink(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getLinkTypeMap().containsKey(urn));
         LinkType nmlObj = slsVisitor.getLinkTypeMap().get(urn);
         //JAXBConfig.getMarshaller().marshal(new ObjectFactory().createLink(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitLink.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitBidirectionalLink() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalLink.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-bidirectional-link.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<BidirectionalLinkType> msg = (JAXBElement<BidirectionalLinkType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:BidirLinkA";
 
         // Act
         BidirectionalLink received = recordsCache.getBidirectionalLink(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getBidirectionalLinkTypeMap().containsKey(urn));
         BidirectionalLinkType nmlObj = slsVisitor.getBidirectionalLinkTypeMap().get(urn);
         //JAXBConfig.getMarshaller().marshal(new ObjectFactory().createBidirectionalLink(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalLink.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitBidirectionalLinkWithLinkGroups() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalLinkWithLinkGroups.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-bidirectional-link-with-linkgroup.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<BidirectionalLinkType> msg = (JAXBElement<BidirectionalLinkType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:BidirLinkB";
 
         // Act
         BidirectionalLink received = recordsCache.getBidirectionalLink(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getBidirectionalLinkTypeMap().containsKey(urn));
         BidirectionalLinkType nmlObj = slsVisitor.getBidirectionalLinkTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new ObjectFactory().createBidirectionalLink(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalLinkWithLinkGroups.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitBidirectionalPort() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalPort.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-bidirectional-port.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<BidirectionalPortType> msg = (JAXBElement<BidirectionalPortType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:BidirPortA";
 
         // Act
         BidirectionalPort received = recordsCache.getBidirectionalPort(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getBidirectionalPortTypeMap().containsKey(urn));
         BidirectionalPortType nmlObj = slsVisitor.getBidirectionalPortTypeMap().get(urn);
         //JAXBConfig.getMarshaller().marshal(new ObjectFactory().createBidirectionalPort(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitBidirectionalPort.end status=0 guid=" + getLogGUID());
     }
 
     @Test
     public void testVisitLinkGroup() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitLinkGroup.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/example-link-group.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<LinkGroupType> msg = (JAXBElement<LinkGroupType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:example.net:2013:linkGroupA";
 
         // Act
         LinkGroup received = recordsCache.getLinkGroup(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getLinkGroupTypeMap().containsKey(urn));
         LinkGroupType nmlObj = slsVisitor.getLinkGroupTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new ObjectFactory().createLinkGroup(nmlObj), System.out);
         //Assert.assertTrue(nmlObj.equals(msg.getValue()));
         logger.debug("event=SLSVisitorTest.testVisitLinkGroup.end status=0 guid=" + getLogGUID());
     }
 
     /**
      * Testing with complete NSA
      *
      * @throws Exception
      */
     @Test
     public void testVisitESnet() throws Exception {
         // Prepare
         logger.debug("event=SLSVisitorTest.testVisitESnet.start guid=" + getLogGUID());
         // Load data to sLS
         String filename = "xml-examples/es.net.xml";
 
         // Read the example and send it to sLS
         RecordsCollection collection = new RecordsCollection(getLogGUID());
         NMLVisitor nmlVisitor = new NMLVisitor(collection, getLogGUID());
         TraversingVisitor nmlTraversingVisitor = new TraversingVisitor(new DepthFirstTraverserImpl(), nmlVisitor);
 
         // Prepare for by reading the example message
         InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
 
         StreamSource ss = new StreamSource(in);
         Unmarshaller um = JAXBConfig.getUnMarshaller();
         JAXBElement<NSAType> msg = (JAXBElement<NSAType>) um.unmarshal(ss);
         msg.getValue().accept(nmlTraversingVisitor);
 
         /**
          * register with sLS
          */
         RegistrationClient registrationClient = new RegistrationClient(sLSConfig.getClient());
         collection.sendTosLS(new SLSRegistrationClientDispatcherImpl(registrationClient), new URNMaskGetAllImpl());
         SimpleLS client = sLSConfig.getClient();
 
         // Prepare the visitor
         SLSVisitor slsVisitor = new SLSVisitor();
         RecordsCache recordsCache = new RecordsCache(new SLSClientDispatcherImpl(client), new URNMaskGetAllImpl(), getLogGUID());
         SLSTraversingVisitor tv = new SLSTraversingVisitor(new SLSTraverserImpl(recordsCache, getLogGUID()), slsVisitor, getLogGUID());
         TraversingVisitorProgressMonitorLoggingImpl monitorLogging = new TraversingVisitorProgressMonitorLoggingImpl(getLogGUID());
         tv.setProgressMonitor(monitorLogging);
         tv.setTraverseFirst(true);
 
         String urn = "urn:ogf:network:es.net:2013:nsa";
 
         // Act
         NSA received = recordsCache.getNSA(urn);
         Assert.assertNotNull(received);
         received.accept(tv);
 
         // Assert
         Assert.assertTrue(slsVisitor.getNsaTypeMap().containsKey(urn));
         NSAType nmlObj = slsVisitor.getNsaTypeMap().get(urn);
         JAXBConfig.getMarshaller().marshal(new org.ogf.schemas.nsi._2013._09.topology.ObjectFactory().createNSA(nmlObj), System.out);
         Assert.assertTrue(nmlObj.equals(msg.getValue()));
 
         logger.debug("event=SLSVisitorTest.testVisitESnet.end status=0 guid=" + getLogGUID());
     }
 
 }
