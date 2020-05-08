 package ch.zhaw.fancysearch;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 import java.io.BufferedWriter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 
 import java.io.File;
 import org.w3c.dom.*;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.DocumentBuilder;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 public class FancySearch {
 
 	public static String[] queryNr = new String[50];
 	public static String[] queryText = new String[50];
 
 	public static void main(String[] args) throws IOException, ParseException {
 		// 0. Specify the analyzer for tokenizing text.
 		// The same analyzer should be used for indexing and searching
 		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
 
 		// 1. create the index
 		Directory index = new RAMDirectory();
 
 		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_42, analyzer);
 
 		IndexWriter w = new IndexWriter(index, config);
 
 		// read irg_collection.xml, add all documents
 		readXMLIntoIndexWriter(w, "data/irg_collection.xml");
 
 		w.close();
 
 		// Index written
 
 		queryNr = new String[50];
 		queryText = new String[50];
 
 		// int queryNr = 1;
		String systemName = "fancySearch-master";
 
 		readXMLIntoQueryArrays("data/irg_queries.xml");
 
 		StringBuilder sb = new StringBuilder();
 
 		for (int queryCounter = 0; queryCounter < 50; queryCounter++) {
 			// 2. query
			String querystr = "transistor phase splitting circuits";
 
 			// the "title" arg specifies the default field to use
 			// when no field is explicitly specified in the query.
 			Query q = new QueryParser(Version.LUCENE_42, "text", analyzer).parse(querystr);
 
 			// 3. search
 			int hitsPerPage = 1000;
 			IndexReader reader = DirectoryReader.open(index);
 			IndexSearcher searcher = new IndexSearcher(reader);
 			TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
 			searcher.search(q, collector);
 			ScoreDoc[] hits = collector.topDocs().scoreDocs;
 
 			// 4. display results
 			System.out.println(".. done with query " + queryNr[queryCounter]);
 
 			for (int i = 0; i < hits.length; ++i) {
 				int docId = hits[i].doc;
 				Document d = searcher.doc(docId);
 				sb.append(queryNr[queryCounter] + " " + "Q0 " + d.get("recordId") + " " + (i + 1) + " " + hits[i].score + " " + systemName + "\n");
 
 			}
 
 			// reader can only be closed when there
 			// is no need to access the documents any more.
 			reader.close();
 		}
 
 		// System.out.println(sb.toString());
 		Writer writer = null;
 
 		try {
 			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("data/output/" + systemName + ".txt"), "utf-8"));
 			writer.write(sb.toString());
 		} catch (IOException ex) {
 			// report
 			ex.printStackTrace();
 		} finally {
 			try {
 				writer.close();
 			} catch (Exception ex) {
 			}
 		}
 
 	}
 
 	private static void readXMLIntoQueryArrays(String path) {
 
 		try {
 
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 			org.w3c.dom.Document doc = docBuilder.parse(new File(path));
 
 			doc.getDocumentElement().normalize();
 
 			NodeList listOfDOCs = doc.getElementsByTagName("DOC");
 			int totalDOCs = listOfDOCs.getLength();
 			System.out.println("Total no of queries : " + totalDOCs);
 
 			for (int s = 0; s < listOfDOCs.getLength(); s++) {
 				// create doc, fill below
 				// Document documentToAdd = new Document();
 
 				Node firstDOCNode = listOfDOCs.item(s);
 				if (firstDOCNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element firstDOCElement = (Element) firstDOCNode;
 
 					// -------
 					NodeList recordIDList = firstDOCElement.getElementsByTagName("recordId");
 					Element recordIDElement = (Element) recordIDList.item(0);
 
 					NodeList textrIDList = recordIDElement.getChildNodes();
 					queryNr[s] = ((Node) textrIDList.item(0)).getNodeValue().trim();
 
 					// -------
 					NodeList textList = firstDOCElement.getElementsByTagName("text");
 					Element textElement = (Element) textList.item(0);
 
 					NodeList textTextList = textElement.getChildNodes();
 					queryText[s] = ((Node) textTextList.item(0)).getNodeValue().trim();
 
 				}
 
 			}
 
 		} catch (SAXParseException err) {
 			System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
 			System.out.println(" " + err.getMessage());
 
 		} catch (SAXException e) {
 			Exception x = e.getException();
 			((x == null) ? e : x).printStackTrace();
 
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 	private static void readXMLIntoIndexWriter(IndexWriter w, String path) {
 		try {
 
 			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
 			org.w3c.dom.Document doc = docBuilder.parse(new File(path));
 
 			doc.getDocumentElement().normalize();
 
 			NodeList listOfDOCs = doc.getElementsByTagName("DOC");
 			int totalDOCs = listOfDOCs.getLength();
 			System.out.println("Total no of documents to index : " + totalDOCs);
 
 			for (int s = 0; s < listOfDOCs.getLength(); s++) {
 				// create doc, fill below
 				Document documentToAdd = new Document();
 
 				Node firstDOCNode = listOfDOCs.item(s);
 				if (firstDOCNode.getNodeType() == Node.ELEMENT_NODE) {
 
 					Element firstDOCElement = (Element) firstDOCNode;
 
 					// -------
 					NodeList recordIDList = firstDOCElement.getElementsByTagName("recordId");
 					Element recordIDElement = (Element) recordIDList.item(0);
 
 					NodeList textrIDList = recordIDElement.getChildNodes();
 					documentToAdd.add(new StringField("recordId", ((Node) textrIDList.item(0)).getNodeValue().trim(), Field.Store.YES));
 
 					// -------
 					NodeList textList = firstDOCElement.getElementsByTagName("text");
 					Element textElement = (Element) textList.item(0);
 
 					NodeList textTextList = textElement.getChildNodes();
 
 					documentToAdd.add(new TextField("text", ((Node) textTextList.item(0)).getNodeValue().trim(), Field.Store.YES));
 
 					w.addDocument(documentToAdd);
 
 				}
 
 			}
 
 		} catch (SAXParseException err) {
 			System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
 			System.out.println(" " + err.getMessage());
 
 		} catch (SAXException e) {
 			Exception x = e.getException();
 			((x == null) ? e : x).printStackTrace();
 
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 }
