 package org.mule.galaxy.example.atompub;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Element;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.ExtensibleElement;
 import org.apache.abdera.protocol.client.AbderaClient;
 import org.apache.abdera.protocol.client.ClientResponse;
 import org.apache.abdera.protocol.client.RequestOptions;
 import org.apache.axiom.om.util.Base64;
 import org.mule.galaxy.atom.AbstractItemCollection;
 import org.mule.galaxy.test.AbstractAtomTest;
 
 public class AtomPubExampleTest extends AbstractAtomTest {
     public void testExamples() throws Exception {
         // START SNIPPET: setup
         AbderaClient client = new AbderaClient();
 
         RequestOptions defaultOpts = client.getDefaultRequestOptions();
         defaultOpts.setAuthorization("Basic " + Base64.encode("admin:admin".getBytes()));
         defaultOpts.setContentType("application/atom+xml;type=entry");
         // END SNIPPET: setup
         
         // START SNIPPET: createworkspace
         // Create a "Services" workspace
         Entry entry = factory.newEntry();
         entry.setTitle("Services");
         entry.setUpdated(new Date());
         entry.addAuthor("Ignored");
         entry.setId(factory.newUuidUri());
         entry.setContent("");
         
         // Create a <workspace-info> element
         Element wInfo = factory.newElement(new QName(AbstractItemCollection.NAMESPACE, "workspace-info"));
         wInfo.setAttributeValue("name", "Services");
         entry.addExtension(wInfo);
         
         ClientResponse result = client.post("http://localhost:9002/api/registry", entry, defaultOpts);
         // END SNIPPET: createworkspace
         assertEquals(201, result.getStatus());
        result.release();
         
         // START SNIPPET: addwsdl
         // Store a WSDL inside the Services workspace
         RequestOptions opts = new RequestOptions();
         opts.setContentType("application/xml; charset=utf-8");
         opts.setSlug("hello.wsdl");
         opts.setHeader("X-Artifact-Version", "1.0");
         opts.setAuthorization(defaultOpts.getAuthorization());
         
         result = client.post("http://localhost:9002/api/registry/Services", 
                              getClass().getResourceAsStream("/hello.wsdl"), 
                              opts);
         // END SNIPPET: addwsdl
        result.release();
         
         // START SNIPPET: getwsdl
         // Get the metadata for the WSDL
         result = client.get("http://localhost:9002/api/registry/Services/hello.wsdl;atom", defaultOpts);
         // END SNIPPET: addwsdl
         
         prettyPrint(result.getDocument());
        result.release();
         
         // START SNIPPET: lifecycle
         // Get the metadata for the WSDL
         Document<Entry> entryDoc = result.getDocument();
         entry = entryDoc.getRoot();
         
         // Find the versioned metadata
         String namespace = "http://galaxy.mule.org/1.0";
         List<ExtensibleElement> extensions = entry.getExtensions(new QName(namespace, "metadata"));
         ExtensibleElement metadata = null;
         for (ExtensibleElement e : extensions) {
             if ("versioned".equals(e.getAttributeValue("id"))) {
                 metadata = e;
             }
         }
         
         // Transition to the next phase
         ExtensibleElement lifecycle = metadata.getExtension(new QName(namespace, "lifecycle"));
         lifecycle.setAttributeValue("phase", "Developed");
         
         // Update the metadata
         result = client.put("http://localhost:9002/api/registry/Services/hello.wsdl;atom", entry, defaultOpts);
         // END SNIPPET: lifecycle
         assertEquals(204, result.getStatus());
        result.release();
     }
 }
