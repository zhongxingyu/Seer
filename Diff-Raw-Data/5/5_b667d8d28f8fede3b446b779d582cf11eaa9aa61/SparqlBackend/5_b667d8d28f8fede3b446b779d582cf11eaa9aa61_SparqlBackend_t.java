 
 package net.ontopia.topicmaps.utils.sdshare.client;
 
 import java.util.Map;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Date;
 import java.io.Writer;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.InputStreamReader;
 import java.net.URLEncoder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.hp.hpl.jena.rdf.arp.AResource;
 import com.hp.hpl.jena.rdf.arp.ALiteral;
 import com.hp.hpl.jena.rdf.arp.StatementHandler;
 
 import net.ontopia.utils.SDShareRuntimeException;
 import net.ontopia.utils.StreamUtils;
 import net.ontopia.topicmaps.utils.rdf.RDFUtils;
 
 /**
  * INTERNAL: Backend which uses SPARQL to update an RDF triple store.
  * This class implements the 2012-01-05 SPARQL working drafts from the
  * W3C.  A separate subclass implements the dialect supported by
  * Virtuoso.  There's another for Joseki.
  */
 public class SparqlBackend extends AbstractBackend implements ClientBackendIF {
   static Logger log = LoggerFactory.getLogger(SparqlBackend.class.getName());
 
   public void loadSnapshot(SyncEndpoint endpoint, Snapshot snapshot) {
     String graph = snapshot.getFeed().getPrefix();
     String uri = findPreferredLink(snapshot.getLinks()).getUri();
 
     // first, clear the graph
     doUpdate(endpoint.getHandle(),
              "clear graph <" + graph + ">");
 
     // second, import the snapshot
     insertDataFrom(uri, endpoint.getHandle(), graph);
 
     // third, update the last modified date of the graph
     doUpdate(endpoint.getHandle(),
              makeUpdateLastModifiedStatement(graph, graph));
   }
 
   public void applyFragments(SyncEndpoint endpoint, List<Fragment> fragments) {
     for (Fragment fragment : fragments)
       applyFragment(endpoint, fragment);
   }
 
   private void applyFragment(SyncEndpoint endpoint, Fragment fragment) {
     String graph = fragment.getFeed().getPrefix();
     String uri = findPreferredLink(fragment.getLinks()).getUri();
 
     // FIXME: we don't support more than one SI at a time yet.
     if (fragment.getTopicSIs().size() != 1)
       throw new SDShareRuntimeException("Fragment had " +
                                         fragment.getTopicSIs().size() +
                                         " SIs, which we cannot handle");
     String subject = fragment.getTopicSIs().iterator().next();
 
     // first, remove all statements about the current topic
     doUpdate(endpoint.getHandle(),
              makeDeleteStatement(graph, subject));
 
     // second, load new fragment into graph
     insertDataFrom(uri, endpoint.getHandle(), graph);
 
     // third, update the last modified date of the SI
     doUpdate(endpoint.getHandle(),
              makeUpdateLastModifiedStatement(graph, subject));
   }
 
   protected String makeDeleteStatement(String graph, String subject) {
     return "WITH <" + graph + "> " +
            "DELETE { <" + subject +"> ?p ?o } " +
            "WHERE { <" + subject +"> ?p ?o } ";
   }
 
   protected String makeUpdateLastModifiedStatement(String graph, String subject) {
     String now = String.format("\"%tFT%<tRZ\"^^xsd:dateTime", new Date());
    String stmts = "<" + subject + "> <http://www.sdshare.org/2012/extension/lastmodified> " + now + " .";
    return makeInsertStatement(graph, stmts);
   }
 
   // ===== Implementation code
 
   public int getLinkScore(AtomLink link) {
     MIMEType mimetype = link.getMIMEType();
     // FIXME: this is too simplistic. we could probably support more
     // syntaxes than just this one, but for now this will have to do.
     if (mimetype != null &&
         mimetype.getType() != null &&
         mimetype.getType().equals("application/rdf+xml"))
       return 100;
     return 0;
   }
 
   public void insertDataFrom(String sourceuri, String targeturi,
                                     String graphuri) {
     InsertHandler handler = new InsertHandler(targeturi, graphuri);
     try {
       RDFUtils.parseRDFXML(sourceuri, handler);
       handler.close();
     } catch (IOException e) {
       throw new SDShareRuntimeException(e);
     }
   }
 
   public void doUpdate(String endpoint, String statement) {
     try {
       doUpdate_(endpoint, statement);
     } catch (IOException e) {
       throw new SDShareRuntimeException(e);
     }
   }
 
   public void doUpdate_(String endpoint, String statement) throws IOException {
     if (log.isDebugEnabled())
       log.debug("doUpdate: " + statement);
 
     // FIXME: in time we may have to use Keep-Alive so that we don't
     // need to open new TCP connections all the time.
 
     // (1) putting together the request
     statement = URLEncoder.encode(statement, "utf-8");
     byte rawdata[] = (getParameterName() + "=" + statement).getBytes("utf-8");
 
     HttpClient httpclient = new DefaultHttpClient();
     HttpPost httppost = new HttpPost(endpoint);
     ByteArrayEntity reqbody = new ByteArrayEntity(rawdata);
     reqbody.setContentType("application/x-www-form-urlencoded; charset=utf-8");
     httppost.setEntity(reqbody);
 
     // (2) retrieving the response
     HttpResponse response = httpclient.execute(httppost);
     HttpEntity resEntity = response.getEntity();
 
     if (log.isDebugEnabled())
       log.debug("Server response: " + response.getStatusLine());
 
     String msg = StreamUtils.read(new InputStreamReader(resEntity.getContent()));
     if (log.isDebugEnabled())
       log.debug("Body: " + msg);
 
     if (response.getStatusLine().getStatusCode() != 200)
       throw new SDShareRuntimeException("Error sending SPARQL query: " +
                                         response.getStatusLine() + " " +
                                         msg);
   }
 
   protected String getParameterName() {
     return "update";
   }
 
   // ===== Writing INSERT-format triples
 
   public class InsertHandler implements StatementHandler {
     private String targeturi;
     private String graphuri;
     private Writer out;
     private Map<String, String> nodelabels;
     private int counter;
     private int stmts;
 
     public InsertHandler(String targeturi, String graphuri) {
       this.targeturi = targeturi;
       this.graphuri = graphuri;
       this.out = new StringWriter();
       this.nodelabels = new HashMap();
     }
 
     public void statement(AResource sub, AResource pred, ALiteral lit) {
       try {
         writeResource(sub);
         writeResource(pred);
         writeLiteral(lit);
         terminate();
       } catch (IOException e) {
         throw new SDShareRuntimeException(e);
       }
     }
 
     public void statement(AResource sub, AResource pred, AResource obj) {
       try {
         writeResource(sub);
         writeResource(pred);
         writeResource(obj);
         terminate();
       } catch (IOException e) {
         throw new SDShareRuntimeException(e);
       }
     }
 
     public void close() throws IOException {
       insertBatch();
       log.debug("Closed handler, finished inserting");
     }
 
     private void terminate() throws IOException {
       out.write(".");
       stmts++;
       if (stmts % 1000 == 0)
         insertBatch();
     }
 
     private void writeLiteral(ALiteral lit) throws IOException {
       String litstr = lit.toString();
       char[] tmp = new char[litstr.length() * 4];
       int pos = 0;
       for (int ix = 0; ix < litstr.length(); ix++) {
         char ch = litstr.charAt(ix);
         if (ch == '\\') {
           tmp[pos++] = '\\';
           tmp[pos++] = '\\';
         } else if (ch == '"') {
           tmp[pos++] = '\\';
           tmp[pos++] = '"';
         } else if (ch == 0xD) {
           tmp[pos++] = '\\';
           tmp[pos++] = 'r';
         } else if (ch == 0xA) {
           tmp[pos++] = '\\';
           tmp[pos++] = 'n';
         } else if (ch == 0x9) {
           tmp[pos++] = '\\';
           tmp[pos++] = 't';
         } else
           tmp[pos++] = ch;
       }
 
       out.write('\"');
       out.write(tmp, 0, pos);
       out.write("\" ");
     }
 
     private void writeResource(AResource res) throws IOException {
       if (res.isAnonymous()) {
         String id = res.getAnonymousID();
         String label = nodelabels.get(id);
         if (label == null) {
           label = "_:b" + counter++;
           nodelabels.put(id, label);
         }
         out.write(nodelabels.get(id) + " ");
       } else
         out.write("<" + res.getURI() + "> ");
     }
 
     private void insertBatch() throws IOException {
       log.debug("Posting batch after " + stmts + " statements");
       doUpdate(targeturi, makeInsertStatement(graphuri, out.toString()));
       out = new StringWriter();
     }
   }
 
   protected String makeInsertStatement(String graph, String stmts) {
     return "prefix xsd: <http://www.w3.org/2001/XMLSchema#> " +
            "insert data { graph <" + graph + "> { " + stmts + " } }";
   }
 }
