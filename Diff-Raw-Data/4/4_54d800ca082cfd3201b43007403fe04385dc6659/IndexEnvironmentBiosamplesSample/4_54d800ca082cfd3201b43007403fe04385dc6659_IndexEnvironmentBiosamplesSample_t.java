 /**
  * 
  */
 package uk.ac.ebi.arrayexpress.utils.saxon.search;
 
 import java.io.File;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 
 import net.sf.saxon.Configuration;
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.om.NodeInfo;
 import net.sf.saxon.xpath.XPathEvaluator;
 
 import org.apache.commons.configuration.HierarchicalConfiguration;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
 import org.xmldb.api.modules.XPathQueryService;
 
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 import uk.ac.ebi.arrayexpress.components.SearchEngine;
 import uk.ac.ebi.arrayexpress.components.XmlDbConnectionPool;
 import uk.ac.ebi.arrayexpress.utils.HttpServletRequestParameterMap;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.saxon.PrintUtils;
 
 /**
  * @author rpslpereira
  * 
  */
 public class IndexEnvironmentBiosamplesSample extends AbstractIndexEnvironment {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	private XmlDbConnectionPool xmlDBConnectionPool;
 
 	// private String driverXml;
 	// private String connectionString;
 	// private String dbname;
 	// private Database db;
 	// private Collection coll;
 
 	// private long numberOfExperiments;
 	//
 	// private long numberOfAssays;
 
 	/**
 	 * @param indexConfig
 	 */
 	public IndexEnvironmentBiosamplesSample(
 			HierarchicalConfiguration indexConfig) {
 		super(indexConfig);
 	}
 
 	// I need to initialize the database connection stuff nad to calculate the
 	// number of assys ans the number os experiments (matter of performance)
 	@Override
 	public void setup() {
 
 		defaultSortField = "";
 		defaultSortDescending = false;
 		defaultPageSize = 25;
 
 		// I'm calling this to clean the reference to the IndexReader->
 		// closeIndexReader();getIndexReader();
 		super.setup();
 		this.xmlDBConnectionPool = (XmlDbConnectionPool) Application
 				.getAppComponent("XmlDbConnectionPool");
 
 	}
 
 	public String queryDB(ScoreDoc[] hits, IndexSearcher isearcher,
 			int initialExp, int finalExp, HttpServletRequestParameterMap map)
 			throws Exception {
 
 		// Collection instance
 		String ret = "";
 		StringBuilder totalRes = new StringBuilder();
 		if (map.get("accession") == null && map.get("groupaccession") == null) {
 
 			// StringBuilder totalResXml = new StringBuilder();
 			totalRes.append("<biosamples><all>");
 			for (int i = initialExp; i < finalExp; i++) {
 
 				int docId = hits[i].doc;
 				Document doc = isearcher.doc(docId);
 				StringBuilder org = new StringBuilder();
 				for (String x : doc.getValues("organism")) {
 						org.append("<organism>" + x +  "</organism>");
 				}
 				StringBuilder groups =new StringBuilder();
 				for (String x : doc.getValues("groupaccession")) {
 					groups.append("<id>" + x +  "</id>");
 				}
 				StringBuilder dbs =new StringBuilder();
 
 				for (String x : doc.getValues("databaseinfo")) {
 					String[] arr =x.split("###");
 					//System.out.println("Valor->" + x);
 					dbs.append("<database>");
 					dbs.append("<name>" + arr[0].trim() +  "</name>");
 					dbs.append("<url>" + StringEscapeUtils.escapeXml(arr[1].trim()) +  "</url>");
 					dbs.append("<id>" +  StringEscapeUtils.escapeXml(arr[2].trim()) +  "</id>");
 					dbs.append("</database>");
 				}
 
 				totalRes.append("<Sample>");
 				totalRes.append("<id>" + doc.get("accession") + "</id>");
				String aux=doc.get("description")!=null?doc.get("description"):"";
 				totalRes.append("<description>"
						+ StringTools.limit(StringEscapeUtils.escapeXml(aux),150)
 						// + doc.get("title")
 						+ "</description>");
 				totalRes.append("<name>"
 						+ StringEscapeUtils.escapeXml(doc.get("name"))
 						// + doc.get("title")
 						+ "</name>");
 				totalRes.append("<organisms>" + org /*
 													 * doc.get("org")!=null &&
 													 * doc
 													 * .get("org").length()>0?
 													 * doc.get("org"):""
 													 */
 						+ "</organisms>");
 				totalRes.append("<groupaccession>" + groups
 						+ "</groupaccession>");
 				
 				totalRes.append("<databases>" + dbs
 						+ "</databases>");
 				totalRes.append("</Sample>");
 
 			}
 			totalRes.append("</all></biosamples>");
 			// System.out.println("totalRes->" + totalRes.toString());
 			ret = totalRes.toString();
 
 		} else {
 			try {
 
 				// coll = DatabaseManager.getCollection(connectionString);
 				Collection coll = xmlDBConnectionPool.getCollection();
 				XPathQueryService service = (XPathQueryService) coll
 						.getService("XPathQueryService", "1.0");
 				totalRes.append("(");
 
 				for (int i = initialExp; i < finalExp; i++) {
 
 					int docId = hits[i].doc;
 					Document doc = isearcher.doc(docId);
 					totalRes.append("'" + doc.get("accession") + "'");
 					// totalRes.append("'" + doc.get("id") + "'");
 					if (i != (finalExp - 1)) {
 						totalRes.append(",");
 					}
 				}
 
 				totalRes.append(")");
 				if (logger.isDebugEnabled()) {
 					logger.debug("QueryString->" + totalRes);
 				}
 				long time = System.nanoTime();
 
 				ResourceSet set = null;
 				// search in all samples
 				// I'm showing the datail of a sample inside a sample Group
 				// browsing
 				if (map.get("accession") != null) {
 					set = service
 							.query("<biosamples><all>{for $x in  distinct-values("
 									+ totalRes.toString()
 									+ ")  let $y:=//Sample[@id=($x)]"
 									+ " return ($y) " + " }</all></biosamples>");
 				} else {
 					// I'm browsing a samplegroup
 					// if(map.get("groupaccession")!=null){
 					String queryStr = "<biosamples><all><Samples>{for $x in "
 							+ totalRes.toString()
 							+ "  let $y:=//Sample[@id=($x)]"
 							+ "  return $y } "
 							+ " { let $att:= //SampleGroup[@id='"
 							+ map.get("groupaccession")[0]
 							+ "']"
 							+ " return <SampleAttributes>{$att/SampleAttributes/*} </SampleAttributes>}"
 							+ " { let $att:= //SampleGroup[@id='"
 							+ map.get("groupaccession")[0]
 							+ "']"
 							// +
 							// " return <DatabaseGroup name=\"{$att/attribute/value[../@class='Databases']//attribute/value[../@class='Database Name']}\"  uri=\"{$att/attribute/value[../@class='Databases']//attribute/value[../@class='Database URI']}\"/>}"
 							+ " return <DatabaseGroup>{$att/attribute[@class='Databases']} </DatabaseGroup>}"
 							+ "</Samples></all></biosamples>";
 					// logger.debug(queryStr);
 					set = service.query(queryStr);
 				}
 
 				double ms = (System.nanoTime() - time) / 1000000d;
 
 				if (logger.isDebugEnabled()) {
 					logger.debug("Xml db query took: " + ms + " ms");
 				}
 
 				time = System.nanoTime();
 				ResourceIterator iter = set.getIterator();
 
 				// Loop through all result items
 				while (iter.hasMoreResources()) {
 
 					ret += iter.nextResource().getContent();
 				}
 				ms = (System.nanoTime() - time) / 1000000d;
 				if (logger.isDebugEnabled()) {
 					logger.debug("Retrieve data from Xml db took: " + ms
 							+ " ms");
 				}
 
 			} catch (final XMLDBException ex) {
 				// Handle exceptions
 				logger.error("Exception:->[{}]", ex.getMessage());
 				ex.printStackTrace();
 			} finally {
 
 			}
 
 		}
 
 		return ret;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * uk.ac.ebi.arrayexpress.utils.saxon.search.AbstractIndexEnvironment#queryDB
 	 * (org.apache.lucene.search.TopDocs,
 	 * org.apache.lucene.search.IndexSearcher, int, int)
 	 */
 	@Deprecated
 	public String queryDB(TopDocs hits, IndexSearcher isearcher,
 			int initialExp, int finalExp, HttpServletRequestParameterMap map)
 			throws Exception {
 
 		// Collection instance
 		String ret = "";
 		StringBuilder totalRes = new StringBuilder();
 
 		// Collection coll=null;
 		try {
 
 			// coll = DatabaseManager.getCollection(connectionString);
 			Collection coll = xmlDBConnectionPool.getCollection();
 			XPathQueryService service = (XPathQueryService) coll.getService(
 					"XPathQueryService", "1.0");
 			totalRes.append("(");
 			for (int i = initialExp; i < finalExp; i++) {
 				Document doc = isearcher.doc(hits.scoreDocs[i].doc);
 				totalRes.append("'" + doc.get("accession") + "'");
 				// totalRes.append("'" + doc.get("id") + "'");
 				if (i != (finalExp - 1)) {
 					totalRes.append(",");
 				}
 			}
 			totalRes.append(")");
 			if (logger.isDebugEnabled()) {
 				logger.debug("QueryString->" + totalRes);
 			}
 			long time = System.nanoTime();
 
 			ResourceSet set = null;
 			// search
 			set = service.query("<biosamples><all>{for $x in  "
 					+ totalRes.toString() + "  let $y:=//Sample[@id=($x)]"
 					+ "  return <Samples>{$y[../@id='"
 					+ map.get("groupaccession")[0] + "']}</Samples>} "
 					+ " </all></biosamples>");
 
 			double ms = (System.nanoTime() - time) / 1000000d;
 
 			if (logger.isDebugEnabled()) {
 				logger.debug("Xml db query took: " + ms + " ms");
 			}
 
 			time = System.nanoTime();
 			ResourceIterator iter = set.getIterator();
 
 			// Loop through all result items
 			while (iter.hasMoreResources()) {
 
 				ret += iter.nextResource().getContent();
 			}
 			ms = (System.nanoTime() - time) / 1000000d;
 			if (logger.isDebugEnabled()) {
 				logger.debug("Retrieve data from Xml db took: " + ms + " ms");
 			}
 
 		} catch (final XMLDBException ex) {
 			// Handle exceptions
 			logger.error("Exception:->[{}]", ex.getMessage());
 			ex.printStackTrace();
 		} finally {
 			// if (coll!=null){
 			// try {
 			// coll.close();
 			// } catch (XMLDBException e) {
 			// // TODO Auto-generated catch block
 			// e.printStackTrace();
 			// }
 			// }
 
 		}
 		// logger.debug("Xml->" + ret);
 		// TODO rpe> remove this
 		// ret=ret.replace("&", "ZZZZZ");
 		return ret;
 
 		// return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +ret;
 	}
 
 	// I need to implement different logic for Biosamples Samples ... If a
 	// SampleGroup is removed I will not receive that all the samples were
 	// removed
 	public void indexIncrementalFromXmlDB(String indexLocationDirectory,
 			String dbHost, int dbPort, String dbPassword, String dbName)
 			throws Exception {
 		// I'm upgrading so the baseline is the current nodes number
 		int countNodes = getCountDocuments();
 		String driverXml = "";
 		String connectionString = "";
 		Collection coll;
 		IndexWriter w = null;
 		Map<String, XPathExpression> fieldXpe = new HashMap<String, XPathExpression>();
 		logger.info(
 				"indexIncrementalFromXmlDB(BioSamplesSample) is starting for [{}], and initially I have[{}] ... ",
 				new Object[] { indexId, countNodes });
 		try {
 
 			Directory indexTempDirectory = FSDirectory.open(new File(
 					indexLocationDirectory, indexId));
 			w = openIndex(indexTempDirectory, indexAnalyzer);
 			HierarchicalConfiguration connsConf = (HierarchicalConfiguration) Application
 					.getInstance().getPreferences()
 					.getConfSubset("bs.xmldatabase");
 			if (null != connsConf) {
 				driverXml = connsConf.getString("driver");
 				connectionString = connsConf.getString("base") + "://" + dbHost
 						+ ":" + dbPort + "/" + dbName;
 			} else {
 				logger.error("bs.xmldatabase Configuration is missing!!");
 			}
 			logger.debug("connectionString->" + connectionString);
 			coll = DatabaseManager.getCollection(connectionString);
 			XPathQueryService service = (XPathQueryService) coll.getService(
 					"XPathQueryService", "1.0");
 
 			DocumentInfo source = null;
 			Configuration config = ((SaxonEngine) Application
 					.getAppComponent("SaxonEngine")).trFactory
 					.getConfiguration();
 			XPath xp = new XPathEvaluator(config);
 			for (FieldInfo field : fields.values()) {
 				fieldXpe.put(field.name, xp.compile(field.path));
 				logger.debug("Field Path->[{}]", field.path);
 			}
 
 			// I will do one of them manually (I need to go to all deleted
 			// SampleGroups and remove all the Samples - to do that I will query
 			// the databse)
 			long numberResults = 0;
 			SearchEngine se = (SearchEngine) Application
 					.getAppComponent("SearchEngine");
 
 			ResourceSet set = service
 					.query("count("
 							+ se.getController().getEnvironment(
 									"biosamplesgroup").indexDocumentPath
 							+ "[@delete='true'])");
 			logger.debug("count("
 					+ se.getController().getEnvironment("biosamplesgroup").indexDocumentPath
 					+ "[@delete='true'])");
 			if (set.getIterator().hasMoreResources()) {
 				numberResults = Integer.parseInt((String) set.getIterator()
 						.nextResource().getContent());
 			}
 			logger.debug("Number of results(SampleGroups) to delete->"
 					+ numberResults);
 			long pageSizeDefault = 50000;
 			if (numberResults > 1000000) {
 				pageSizeDefault = 1000000;
 			}
 
 			long pageNumber = 1;
 			int count = 0;
 			while ((pageNumber * pageSizeDefault) <= (numberResults
 					+ pageSizeDefault - 1)) {
 
 				// calculate the last hit
 				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 						: (numberResults - pageInit + 1);
 
 				service = (XPathQueryService) coll.getService(
 						"XPathQueryService", "1.0");
 
 				// xquery paging using subsequence function
 				long time = System.nanoTime();
 				// TODO: I'm assuming that there is always an attribute @id in
 				// each element
 				set = service
 						.query("for $x in(subsequence("
 								+ se.getController().getEnvironment(
 										"biosamplesgroup").indexDocumentPath
 								+ "[@delete='true']/@id," + pageInit + ","
 								+ pageSize + ")) return string($x)");
 				double ms = (System.nanoTime() - time) / 1000000d;
 				logger.info("Query XMLDB to delete took ->[{}]", ms);
 
 				ResourceIterator iter = set.getIterator();
 				logger.debug("for $x in(subsequence("
 						+ se.getController().getEnvironment("biosamplesgroup").indexDocumentPath
 						+ "[@delete='true']/@id," + pageInit + "," + pageSize
 						+ ")) return string($x)");
 				XPath xp2;
 				XPathExpression xpe2;
 				List documentNodes;
 				StringReader reader;
 				// cache of distinct attributes fora each sample group
 
 				while (iter.hasMoreResources()) {
 					count++;
 					logger.debug("its beeing processed(to delete) the number ->"
 							+ count);
 					String idToProcess = (String) iter.nextResource()
 							.getContent();
 
 					logger.debug("@id that is being processed(to delete->"
 							+ idToProcess);
 
 					Term idTerm = new Term("groupaccession",
 							idToProcess.toLowerCase());
 					int countToDelete = getIndexReader().docFreq(idTerm);
 					if (countToDelete > 0) {
 						logger.debug(
 								"The document with SampleGroup [{}] is being deleted from sample Lucene Index. It had [{}] samples",
 								new Object[] { idToProcess, countToDelete });
 						w.deleteDocuments(idTerm);
 						// need to remove one from the number of
 						// documents count
 						countNodes -= countToDelete;
 					} else {
 						Application
 								.getInstance()
 								.sendEmail(
 										null,
 										null,
 										"BIOSAMPLES WARNING - Incremental Update - SampleGroup Id marked for deletion but the id doesn't exist samples on the GUI! id-> "
 												+ idToProcess, "");
 					}
 				}
 
 				logger.debug("until now it were processed->[{}]", pageNumber
 						* pageSizeDefault);
 				pageNumber++;
 
 			}
 
 			// the xmldatabase is not very correct and have memory problem for
 			// queires with huge results, so its necessary to implement our own
 			// iteration mechanism
 			//
 			// // I will collect all the results
 			// ResourceSet set = service.query(this.env.indexDocumentPath);
 			numberResults = 0;
 
 			set = service.query("count(" + indexDocumentPath + ")");
 			if (set.getIterator().hasMoreResources()) {
 				numberResults = Integer.parseInt((String) set.getIterator()
 						.nextResource().getContent());
 			}
 			// TODO:######################################Change this after -
 			// this is just a performance test
 			// float percentage=0.1F;
 			// numberResults=Math.round(numberResults * percentage);
 			logger.debug("Number of results->" + numberResults);
 			pageSizeDefault = 50000;
 			if (numberResults > 1000000) {
 				pageSizeDefault = 1000000;
 			}
 
 			pageNumber = 1;
 			count = 0;
 			Map<String, AttsInfo[]> cacheAtt = new HashMap<String, AttsInfo[]>();
 			Map<String, XPathExpression> cacheXpathAtt = new HashMap<String, XPathExpression>();
 			Map<String, XPathExpression> cacheXpathAttValue = new HashMap<String, XPathExpression>();
 			while ((pageNumber * pageSizeDefault) <= (numberResults
 					+ pageSizeDefault - 1)) {
 				// calculate the last hit
 				long pageInit = (pageNumber - 1) * pageSizeDefault + 1;
 				long pageSize = (pageNumber * pageSizeDefault < numberResults) ? pageSizeDefault
 						: (numberResults - pageInit + 1);
 
 				service = (XPathQueryService) coll.getService(
 						"XPathQueryService", "1.0");
 
 				// xquery paging using subsequence function
 				long time = System.nanoTime();
 				// TODO: I'm assuming that there is always an attribute @id in
 				// each element
 				set = service.query("for $x in(subsequence("
 						+ indexDocumentPath + "/@id," + pageInit + ","
 						+ pageSize + ")) return string($x)");
 
 				double ms = (System.nanoTime() - time) / 1000000d;
 				logger.info("Query XMLDB took ->[{}]", ms);
 
 				ResourceIterator iter = set.getIterator();
 				XPath xp2;
 				XPathExpression xpe2;
 				List documentNodes;
 				StringReader reader;
 				// cache of distinct attributes fora each sample group
 
 				while (iter.hasMoreResources()) {
 					count++;
 					logger.debug("its beeing processed the number ->" + count);
 					String idToProcess = (String) iter.nextResource()
 							.getContent();
 
 					logger.debug("@id that is being processed->" + idToProcess);
 					// I need to get the sample
 					ResourceSet setid = service.query(indexDocumentPath
 							+ "[@id='" + idToProcess + "']");
 					ResourceIterator iterid = setid.getIterator();
 					while (iterid.hasMoreResources()) {
 						StringBuilder xml = new StringBuilder();
 						xml.append((String) iterid.nextResource().getContent());
 						// logger.debug(xml.toString());
 						reader = new StringReader(xml.toString());
 						source = config.buildDocument(new StreamSource(reader));
 
 						// logger.debug("XML DB->[{}]",
 						// PrintUtils.printNodeInfo((NodeInfo) source, config));
 						Document d = new Document();
 
 						xp2 = new XPathEvaluator(source.getConfiguration());
 
 						int position = indexDocumentPath.lastIndexOf("/");
 						// TODO: I also need to change this
 						String pathRoot = "";
 						if (position != -1) {
 							pathRoot = indexDocumentPath.substring(position);
 						} else {
 							pathRoot = indexDocumentPath;
 						}
 						// logger.debug("PathRoot->[{}]",pathRoot);
 						xpe2 = xp2.compile(pathRoot);
 						documentNodes = (List) xpe2.evaluate(source,
 								XPathConstants.NODESET);
 
 						for (Object node : documentNodes) {
 							// logger.debug("XML DB->[{}]",PrintUtils.printNodeInfo((NodeInfo)node,config));
 							// TODO: the name should be configurable
 							String elementIdentifier = "accession";
 							String idElement = (String) fieldXpe.get(
 									elementIdentifier).evaluate(node,
 									XPathConstants.STRING);
 							// I need to see if it already exists
 							// I will also add this document if it is nor marked
 							// as "todelete"
 							Boolean toDelete = (Boolean) fieldXpe.get("delete")
 									.evaluate(node, XPathConstants.BOOLEAN);
 
 							logger.debug(
 									"Incremental Update - The document [{}] is being processed and is marked to delete?[{}]",
 									new Object[] { idElement, toDelete });
 							// I will always try to delete the document (i don't
 							// know if it is new or if it was changed)
 							Term idTerm = new Term(elementIdentifier,
 									idElement.toLowerCase());
 							int countToDelete = getIndexReader()
 									.docFreq(idTerm);
 							if (countToDelete > 0) {
 								// if has more than one, I have to send an email
 								// to warn
 								if (countToDelete > 1) {
 									Application
 											.getInstance()
 											.sendEmail(
 													null,
 													null,
 													"BIOSAMPLES ERROR - Incremental Update - Removing more than one document! id-> "
 															+ idElement,
 													" documents found:"
 															+ countToDelete);
 									// I will launch an exception
 									throw new Exception(
 											"BIOSAMPLES ERROR - Incremental Update -  Removing more than one document in incremental update id-> "
 													+ idElement
 													+ " documents found:"
 													+ countToDelete);
 								}
 								logger.debug(
 										"The document with id [{}] is being deleted from Lucene",
 										idElement);
 								w.deleteDocuments(idTerm);
 								// need to remove one from the number of
 								// documents count
 								countNodes--;
 
 							}
 							// the element doesn't exist on GUI
 							else {
 								// if it is marked to delete I will just an
 								// warning email - it's possible that it was
 								// inserted and deleted on the Backend but it
 								// had never been sent to the GUI before
 								if (toDelete) {
 									Application
 											.getInstance()
 											.sendEmail(
 													null,
 													null,
 													"BIOSAMPLES WARNING - Incremental Update - Id marked for deletion but the id doesn't exist on the GUI! id-> "
 															+ idElement, "");
 
 								}
 							}
 
 							try {
 								d = processEntryIndex(node, config, service,
 										fieldXpe);
 
 							} catch (Exception x) {
 								String xmlError = PrintUtils.printNodeInfo(
 										(NodeInfo) node, config);
 								logger.error(
 										"XML that was being processed when the error occurred DB->[{}]",
 										xmlError);
 								// to avoid the next running to stop
 								// because its not able to delete the
 								// newSetup directory
 								w.close();
 								throw new Exception(
 										"Xml that is being processed:"
 												+ xmlError, x);
 							}
 							countNodes++;
 							addIndexDocument(w, d);
 
 						}
 
 						documentNodes = null;
 						source = null;
 						reader = null;
 						xml = null;
 						// logger.debug("count->[{}]", countNodes);
 
 					}
 				}
 				logger.debug("until now it were processed->[{}]", pageNumber
 						* pageSizeDefault);
 				pageNumber++;
 				if (coll != null) {
 					try {
 						// coll.close();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				set = null;
 
 			}
 
 			setCountDocuments(countNodes);
 			// add metadata to the lucene index
 			Map<String, String> map = new HashMap<String, String>();
 			map.put("numberDocs", Integer.toString(countNodes));
 			map.put("date", Long.toString(System.nanoTime()));
 			// logger.debug(Application.getInstance().getComponent("XmlDbConnectionPool").getMetaDataInformation());
 			// I cannot call directly
 			// getComponent("XmlDbConnectionPool").getMetaDataInformation(),
 			// because I can be working in a did
 			String dbInfo = ((XmlDbConnectionPool) Application.getInstance()
 					.getComponent("XmlDbConnectionPool")).getDBInfo(dbHost,
 					dbPort, dbPassword, dbName);
 
 			// TODO: I need to put here what I have before - to track all the
 			// changes (old numberDocs + old date + oldDBInfo)
 			map.put("DBInfo",
 					dbInfo
 							+ "<BR>##################################################<BR>"
 							+ getMetadataInformation());
 			commitIndex(w, map);
 
 		} catch (Exception x) {
 			logger.error("Caught an exception:", x);
 			w.close();
 			throw x;
 		}
 	}
 
 }
