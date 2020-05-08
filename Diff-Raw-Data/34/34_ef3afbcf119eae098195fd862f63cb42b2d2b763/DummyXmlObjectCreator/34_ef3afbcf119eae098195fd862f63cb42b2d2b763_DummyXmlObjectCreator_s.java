 /* DummyDataSourceCreator.java - created on Jan 25, 2012, Copyright (c) 2011 The European Library, all rights reserved */
 package eu.europeana.uim.repox.rest.utils;
 
 import java.math.BigInteger;
 
 import eu.europeana.uim.repox.rest.client.xml.Aggregator;
 import eu.europeana.uim.repox.rest.client.xml.Namespace;
 import eu.europeana.uim.repox.rest.client.xml.Namespaces;
 import eu.europeana.uim.repox.rest.client.xml.Provider;
 import eu.europeana.uim.repox.rest.client.xml.RecordIdPolicy;
 import eu.europeana.uim.repox.rest.client.xml.RetrieveStrategy;
 import eu.europeana.uim.repox.rest.client.xml.Source;
 import eu.europeana.uim.repox.rest.client.xml.SplitRecords;
 import eu.europeana.uim.repox.rest.client.xml.Target;
 
 /**
  * Creates dummy data sources!
  * 
  * @author Markus Muhr (markus.muhr@kb.nl)
  * @since Jan 25, 2012
  */
 public class DummyXmlObjectCreator {
     /**
      * @param name
      * @return dummy filled aggregator
      */
     public static Aggregator createAggregator(String name) {
         Aggregator aggr = new Aggregator();
         aggr.setName(name);
         aggr.setNameCode("7777");
         aggr.setUrl("http://www.in.gr");
         return aggr;
     }
 
     /**
     * @param name
      * @return dummy filled provider
      */
    public static Provider createProvider(String name) {
         Provider provider = new Provider();
        provider.setName(name);
         provider.setNameCode("7777");
         provider.setUrl("http://www.in.gr");
         return provider;
     }
 
     /**
      * @param id
      * @return dummy filled oai data source
      */
     public static Source createOAIDataSource(String id) {
         Source oaids = new Source();
         oaids.setId(id);
         oaids.setDescription("description");
         oaids.setNameCode("00000");
         oaids.setName("name");
         oaids.setExportPath("/export/");
         oaids.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
         oaids.setNamespace("http://www.europeana.eu/schemas/ese/");
         oaids.setMetadataFormat("ese");
         oaids.setOaiSet("bda");
         oaids.setOaiSource("http://sip-manager.isti.cnr.it:8080/repoxUI_Europeana//OAIHandler");
         return oaids;
     }
 
     /**
      * @param id
      * @return dummy filled z3950 source
      */
     public static Source createZ3950TimestampDataSource(String id) {
         Source z3950TSds = new Source();
         z3950TSds.setId(id);
         z3950TSds.setDescription("description");
         z3950TSds.setNameCode("00130");
         z3950TSds.setName("Z3950-TimeStamp");
         z3950TSds.setExportPath("D:/Projectos/repoxdata_new");
         z3950TSds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
         z3950TSds.setNamespace("info:lc/xmlns/marcxchange-v1");
 
         Target target = new Target();
         target.setAddress("193.6.201.205");
         target.setPort(new BigInteger("1616"));
         target.setRecordSyntax("usmarc");
         target.setCharset("UTF-8");
         target.setDatabase("db");
         target.setUser("user");
         target.setPassword("pwd");
         z3950TSds.setTarget(target);
 
         z3950TSds.setEarliestTimestamp(BigInteger.valueOf(20110301));
 
         Namespace space = new Namespace();
         space.setNamespacePrefix("nsprefix");
         space.setNamespaceUri("uri");
         Namespaces spaces = new Namespaces();
         spaces.setNamespace(space);
         RecordIdPolicy policy = new RecordIdPolicy();
         policy.setType("IdGenerated");
         policy.setIdXpath("idxpath");
         policy.setNamespaces(spaces);
         z3950TSds.setRecordIdPolicy(policy);
 
         return z3950TSds;
     }
 
     /**
      * @param id
      * @return dummy filled z3950 source
      */
     public static Source createZ3950IdFileDataSource(String id) {
         Source z3950IDFileds = new Source();
         z3950IDFileds.setId(id);
         z3950IDFileds.setDescription("test");
         z3950IDFileds.setNameCode("00124");
         z3950IDFileds.setName("Z3950-IdFile");
         z3950IDFileds.setExportPath("D:/Projectos/repoxdata_new");
         z3950IDFileds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
         z3950IDFileds.setNamespace("info:lc/xmlns/marcxchange-v1");
         Target target = new Target();
         target.setAddress("aleph.lbfl.li");
         target.setPort(BigInteger.valueOf(9909));
         target.setDatabase("LLB_IDS");
         target.setUser("test");
         target.setPassword("test");
         target.setRecordSyntax("usmarc");
         target.setCharset("UTF-8");
         z3950IDFileds.setTarget(target);
         z3950IDFileds.setFilePath("C:\folderZ3950\1900028192z3960idList.txt");
         RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
         recordIdPolicy.setType("IdGenerated");
         recordIdPolicy.setIdXpath("test");
 
         Namespaces namespaces = new Namespaces();
         Namespace namespace = new Namespace();
         namespace.setNamespacePrefix("test");
         namespace.setNamespaceUri("test");
         namespaces.setNamespace(namespace);
         recordIdPolicy.setNamespaces(namespaces);
         z3950IDFileds.setRecordIdPolicy(recordIdPolicy);
 
         return z3950IDFileds;
     }
 
     /**
      * @param id
      * @return dummy filled z3950 source
      */
     public static Source createZ3950IdSequenceDataSource(String id) {
         Source ds = new Source();
         ds.setId(id);
         ds.setDescription("test Z39.50 with id sequence");
         ds.setNameCode("00129");
         ds.setName("Z3950-Idseq");
         ds.setExportPath("D:/Projectos/repoxdata_new");
         ds.setSchema("info:lc/xmlns/marcxchange-v1.xsd");
         ds.setNamespace("info:lc/xmlns/marcxchange-v1");
         Target target = new Target();
         target.setAddress("aleph.lbfl.li");
         target.setPort(BigInteger.valueOf(9909));
         target.setDatabase("LLB_IDS");
         target.setUser("test");
         target.setPassword("test");
         target.setRecordSyntax("usmarc");
         target.setCharset("UTF-8");
         ds.setTarget(target);
         ds.setMaximumId(BigInteger.valueOf(6000));
         RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
         recordIdPolicy.setType("IdGenerated");
         recordIdPolicy.setIdXpath("test");
         Namespaces namespaces = new Namespaces();
         Namespace namespace = new Namespace();
         namespace.setNamespacePrefix("test");
         namespace.setNamespaceUri("test");
         namespaces.setNamespace(namespace);
         recordIdPolicy.setNamespaces(namespaces);
         ds.setRecordIdPolicy(recordIdPolicy);
         return ds;
     }
 
     /**
      * @param id
      * @return dummy filled ftp source
      */
     public static Source createFtpDataSource(String id) {
         Source ds = new Source();
         ds.setId(id);
         ds.setDescription("test ftp Data source");
         ds.setNameCode("00124");
         ds.setName("FTP");
         ds.setExportPath("D:/Projectos/repoxdata_new");
         ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
         ds.setNamespace("http://www.europeana.eu/schemas/ese/");
         Target target = new Target();
         target.setCharset("UTF-8");
         ds.setTarget(target);
         ds.setMetadataFormat("ese");
         ds.setIsoFormat("test");
         ds.setFtpPath("/Lizbeth");
 
         SplitRecords splitRecords = new SplitRecords();
         splitRecords.setRecordXPath("record");
         ds.setSplitRecords(splitRecords);
         RetrieveStrategy retrieveStrategy = new RetrieveStrategy();
         retrieveStrategy.setUser("ftp");
 
         retrieveStrategy.setPassword("password");
         retrieveStrategy.setServer("bd1.inesc-id.pt");
         ds.setRetrieveStrategy(retrieveStrategy);
 
         RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
         recordIdPolicy.setType("IdGenerated");
         recordIdPolicy.setIdXpath("test");
         Namespaces namespaces = new Namespaces();
         Namespace namespace = new Namespace();
         namespace.setNamespacePrefix("test");
         namespace.setNamespaceUri("test");
         namespaces.setNamespace(namespace);
         recordIdPolicy.setNamespaces(namespaces);
         ds.setRecordIdPolicy(recordIdPolicy);
 
         return ds;
     }
 
     /**
      * @param id
      * @return dummy filled http source
      */
     public static Source createHttpDataSource(String id) {
         Source ds = new Source();
         ds.setId(id);
         ds.setDescription("test http Data source");
         ds.setNameCode("00124");
         ds.setName("HTTP");
         ds.setExportPath("D:/Projectos/repoxdata_new");
         ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
         ds.setNamespace("http://www.europeana.eu/schemas/ese/");
 
         Target target = new Target();
         target.setCharset("UTF-8");
         ds.setTarget(target);
         ds.setMetadataFormat("ese");
         ds.setIsoFormat("test");
 
         SplitRecords splitRecords = new SplitRecords();
         splitRecords.setRecordXPath("record");
         ds.setSplitRecords(splitRecords);
 
         RetrieveStrategy retrieveStrategy = new RetrieveStrategy();
         retrieveStrategy.setUrl("http://digmap2.ist.utl.pt:8080/index_digital/contente/09428_Ag_DE_ELocal.zip");
         ds.setRetrieveStrategy(retrieveStrategy);
 
         RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
         recordIdPolicy.setType("IdGenerated");
         recordIdPolicy.setIdXpath("test");
         Namespaces namespaces = new Namespaces();
         Namespace namespace = new Namespace();
         namespace.setNamespacePrefix("test");
         namespace.setNamespaceUri("test");
         namespaces.setNamespace(namespace);
         recordIdPolicy.setNamespaces(namespaces);
         ds.setRecordIdPolicy(recordIdPolicy);
 
         return ds;
     }
 
     /**
      * @param id
      * @return dummy filled folder source
      */
     public static Source createFolderDataSource(String id) {
         Source ds = new Source();
         ds.setId(id);
         ds.setDescription("test Folder source");
         ds.setNameCode("00124");
         ds.setName("Folder");
         ds.setExportPath("D:/Projectos/repoxdata_new");
         ds.setSchema("http://www.europeana.eu/schemas/ese/ESE-V3.3.xsd");
         ds.setNamespace("http://www.europeana.eu/schemas/ese/");
 
         Target target = new Target();
         target.setCharset("UTF-8");
         ds.setTarget(target);
         ds.setMetadataFormat("ese");
 
         ds.setIsoFormat("");
         ds.setFolder("C:\folder");
         SplitRecords splitRecords = new SplitRecords();
         splitRecords.setRecordXPath("record");
         ds.setSplitRecords(splitRecords);
 
         RecordIdPolicy recordIdPolicy = new RecordIdPolicy();
         recordIdPolicy.setType("IdGenerated");
         recordIdPolicy.setIdXpath("test");
         Namespaces namespaces = new Namespaces();
         Namespace namespace = new Namespace();
         namespace.setNamespacePrefix("test");
         namespace.setNamespaceUri("test");
         namespaces.setNamespace(namespace);
         recordIdPolicy.setNamespaces(namespaces);
         ds.setRecordIdPolicy(recordIdPolicy);
 
         return ds;
     }
 }
