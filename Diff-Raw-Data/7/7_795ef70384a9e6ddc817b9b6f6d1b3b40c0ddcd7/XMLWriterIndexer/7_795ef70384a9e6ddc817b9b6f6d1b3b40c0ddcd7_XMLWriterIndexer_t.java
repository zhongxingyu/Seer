 package edu.iit.swyne.crawler;
 
 import java.io.File;
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStreamWriter;
 import java.util.Properties;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 public class XMLWriterIndexer extends Indexer {
 	
 	private static final String DEFAULT_XML_OUTPUTFILE = "data/newsDocs.xml";
 	
 	protected static Document xmlDoc;
 
 	public XMLWriterIndexer(Properties props) {
 		super(props);
 		
 		xmlDoc = new Document(new Element("news_documents"));
 	}
 
 	public XMLWriterIndexer() {
 		super();
 	}
 
 	@Override
 	public void sendDocument(NewsDocument doc) {
 		File outputFile = new File(this.props.getProperty("crawler.indexer.xml.outputFile", DEFAULT_XML_OUTPUTFILE));
 		
 		Element newsDoc = new Element("document");
 		newsDoc .addContent(new Element("collection").addContent(doc.getCollection()));
 		newsDoc.addContent(new Element("title").addContent(doc.getTitle()));
 		newsDoc.addContent(new Element("article").addContent(doc.getArticle()));
 		newsDoc.addContent(new Element("date").addContent(doc.getPublishedDate().toString()));
 		newsDoc.addContent(new Element("source").addContent(doc.getSource()));
 		
 		xmlDoc.getRootElement().addContent(newsDoc);
 		
 		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
 		try {
 			outputFile.getParentFile().mkdirs();
 			outputFile.createNewFile();
			out.output(xmlDoc, new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
