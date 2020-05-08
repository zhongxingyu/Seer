 package org.lareferencia.backend.indexer;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
 import org.apache.solr.client.solrj.request.DirectXmlRequest;
 import org.apache.xpath.XPathAPI;
 import org.lareferencia.backend.domain.Country;
 import org.lareferencia.backend.domain.NationalNetwork;
 import org.lareferencia.backend.domain.NetworkSnapshot;
 import org.lareferencia.backend.domain.OAIRecord;
 import org.lareferencia.backend.domain.RecordStatus;
 import org.lareferencia.backend.harvester.OAIRecordMetadata;
 import org.lareferencia.backend.harvester.OAIRecordMetadata.OAIRecordMetadataParseException;
 import org.lareferencia.backend.repositories.NetworkSnapshotRepository;
 import org.lareferencia.backend.repositories.OAIRecordRepository;
 import org.lareferencia.backend.util.MedatadaDOMHelper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.transaction.annotation.Transactional;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class IndexerImpl implements IIndexer{
 
 	private File stylesheet; 
 	private Transformer trf; 
 	
	private static final int PAGE_SIZE = 1000;
 
 	
 	private DocumentBuilder builder; 
 	
 	@Autowired
 	private OAIRecordRepository recordRepository;
 	
 	@Autowired
 	private NetworkSnapshotRepository networkSnapshotRepository;
 	
 	private HttpSolrServer server;
 
 	
 	public IndexerImpl(String xslFileName, String solrURL) throws IndexerException {
 		
 		stylesheet = new File(xslFileName);
 		server = new HttpSolrServer(solrURL);
 
 		try {
 			trf = MedatadaDOMHelper.buildXSLTTransformer(stylesheet);
 			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		} catch (TransformerConfigurationException e) {
 			throw new IndexerException(e.getMessage(), e.getCause());
 		} catch (ParserConfigurationException e) {
 			throw new IndexerException(e.getMessage(), e.getCause());
 		}
 	}
 	
 	@Transactional(readOnly = true)
 	public boolean index(NetworkSnapshot snapshot) {
 		
 		DirectXmlRequest request;
 
 		 try {
 			
 			// Borrado de los docs del país del snapshot
 			request = new DirectXmlRequest("/update", "<delete><query>country_iso:" + snapshot.getNetwork().getCountry().getIso() +"</query></delete>");
 			server.request(request);
 		
			// Update de los registros de a 1000
 			Page<OAIRecord> page = recordRepository.findBySnapshotAndStatus(snapshot, RecordStatus.VALID, new PageRequest(0, PAGE_SIZE));
 			int totalPages = page.getTotalPages();
 
 			for (int i = 0; i < totalPages; i++) {
 				page = recordRepository.findBySnapshotAndStatus(snapshot, RecordStatus.VALID, new PageRequest(i, PAGE_SIZE));
 				
 				System.out.println( "Indexando Snapshot: " + snapshot.getId() + " de: " + snapshot.getNetwork().getName() + " página: " + i + " de: " + totalPages);
 
 
 				String xmlSolrDocsString = "";
 				
 				for (OAIRecord record : page.getContent()) {
 					xmlSolrDocsString += MedatadaDOMHelper.Node2XMLString(this.transform(record, snapshot.getNetwork()));
 				}
 				
 				request = new DirectXmlRequest("/update", "<add>" + xmlSolrDocsString + "</add>");
 				server.request(request);
 				
 				recordRepository.flush();
 			}
 			
 			// commit de los cambios
 			request = new DirectXmlRequest("/update", "<commit/>");
 			server.request(request);
 			
 			System.gc();
 				 
 		} catch (Exception e) {
 			e.printStackTrace();
 			try {
 				server.rollback();
 			} catch (SolrServerException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return false;
 		}
 
 		return true;
 	}
 	
 	public Document transform(OAIRecord record, NationalNetwork network) throws IndexerException {
 		
 		
 		Document dstDocument = builder.newDocument();
 		
 		if ( record.getPublishedXML() == null )
 			throw new IndexerException("El registro: " + record.getId() + "  no tiene publibledXML definido");
 		
 		
 
 		try {
 			
 			OAIRecordMetadata domRecord = new OAIRecordMetadata(record.getIdentifier(), record.getPublishedXML() );
 			
 			trf.transform( new DOMSource(domRecord.getDOMDocument()), new DOMResult(dstDocument));
 			
 			Country country = network.getCountry();
 		    addSolrField(dstDocument, "country", country.getName());
 		    addSolrField(dstDocument, "country_iso", country.getIso());
 		    addSolrField(dstDocument, "id", country.getIso() + "_" + record.getSnapshot().getId() + "_" + record.getId()  );		
 			
 			
 
 		} catch (TransformerException e) {
 			throw new IndexerException(e.getMessage(), e.getCause());
 		} catch (OAIRecordMetadataParseException e) {
 			e.printStackTrace();
 		}
 		
 		
 		return dstDocument;
 		
 	}
 	
 	private void addSolrField(Document document, String fieldName, String content) throws DOMException, TransformerException {
 		Element elem = document.createElement("field");
 		elem.setAttribute("name", fieldName);
 		elem.setTextContent(content);
 		
 		XPathAPI.selectSingleNode(document, "//doc").appendChild(elem);
 	}
 	
 	
 }
