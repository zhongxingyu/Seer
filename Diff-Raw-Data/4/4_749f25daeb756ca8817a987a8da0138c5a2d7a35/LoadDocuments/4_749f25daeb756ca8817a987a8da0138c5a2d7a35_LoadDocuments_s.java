 package org.idiginfo.docsvc.controller.harvest;
 
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.idiginfo.docsvc.jpa.citagora.CitagoraFactoryImpl;
 import org.idiginfo.docsvc.model.apisvc.Annotation;
 import org.idiginfo.docsvc.model.apisvc.Document;
 import org.idiginfo.docsvc.model.apisvc.Documents;
 import org.idiginfo.docsvc.model.citagora.CitagoraFactory;
 import org.idiginfo.docsvc.model.citagora.CitagoraObject;
 import org.idiginfo.docsvc.model.citagora.Container;
 import org.idiginfo.docsvc.model.citagora.Reference;
 import org.idiginfo.docsvc.model.citagora.UriObject;
 import org.idiginfo.docsvc.model.mapping.MapSvcapiToCitagora;
 import org.idiginfo.docsvc.view.rdf.citagora.MapCitagoraObject;
 
 import com.hp.hpl.jena.rdf.model.Model;
 
 public class LoadDocuments {
 
     CitagoraFactoryImpl factory = new CitagoraFactoryImpl();
     MapSvcapiToCitagora documentMapper = new MapSvcapiToCitagora();
 
     List<Container> load(Container containerFields, Documents documents) {
 	List<Container> containers = new Vector<Container>();
 	if (documents == null)
 	    return null;
 	for (Document document : documents) {
 	    Container container = load(containerFields, document);
 	    containers.add(container);
 	}
 	return containers;
 
     }
 
     Container load(Container containerFields, Document document) {
 	boolean localTransaction = false;
 	String doi = document.getDoi();
 	if (doi != null) {
 	    Reference ref = factory.findReferenceByDoi(doi);
 	    if (ref != null) {
 		System.out.println(" doi: " + doi + " already present");
 		List<Container> containers = ref.getContainers();
 		if (containers != null && containers.size() > 0)
 		    return containers.get(0); // there is already a document
 		return null; // no container
 	    }
 	}
 	String uri = document.getUri();
 	if (uri != null) {
 	    CitagoraObject obj = factory.findCitagoraObjectByURI(uri);
 	    if (obj != null) {
 		System.out.println(" uri: " + uri + " already present");
 		return null;
 	    }
 	}
 	if (!factory.isTransactionActive()) {
 	    factory.openTransaction();
 	    localTransaction = true;
 	}
 	Container container = documentMapper.createContainer(containerFields,
 		document);
 	System.out.println(" uri: " + (doi!=null?doi:uri) + " created");
 
 	if (localTransaction) {
 	    factory.commitTransaction();
 	}
 	return container;
     }
 
     public String writeCitagoraRdf(UriObject document, String version, int level) {
 	MapCitagoraObject mapper = new MapCitagoraObject();
 	mapper.add(document, level);
 	Model model = mapper.getModel();
 	StringWriter out = new StringWriter();
 	model.write(out, version);
 	return out.toString();
     }
 
     public CitagoraFactory getFactory() {
 	return factory;
     }
 
 }
