 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.rootdev.javardfaweb;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.StreamingOutput;
 import net.rootdev.javardfa.NTripleSink;
 import net.rootdev.javardfa.ParserFactory;
 import net.rootdev.javardfa.ParserFactory.Format;
 import net.rootdev.javardfa.RDFXMLSink;
 import net.rootdev.javardfa.StatementSink;
 import net.rootdev.javardfa.Version;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 /**
  * REST Web Service
  *
  * @author pldms
  */
 
 @Path("translate")
 public class Translator {
     private static final Logger log = LoggerFactory.getLogger(Translator.class);
 
     //@Context
     //private UriInfo context;
     private final String comment;
 
     /** Creates a new instance of Translator */
     public Translator() {
         comment = "Produced by " + Version.get();
     }
     
     @GET
     @Produces("application/rdf+xml")
     public StreamingOutput getRDFXML(
             @QueryParam("uri") final URL uri,
             @QueryParam("parser")
             @DefaultValue("XHTML") final Format format) {
         return new StreamingOutput() {
             public void write(OutputStream output) throws IOException, WebApplicationException {
                 StatementSink sink = 
                         new RDFXMLSink(output, comment, "Origin: <" + uri + ">");
                 parse(uri, format, sink);
             }
         };
     }
     
     @GET
     @Produces("application/xml")
     public StreamingOutput getXML(
             @QueryParam("uri") final URL uri,
             @QueryParam("parser")
             @DefaultValue("XHTML") final Format format) {
         return getRDFXML(uri, format);
     }
 
     @GET
     @Produces("text/plain")
     public StreamingOutput getNTriples(
             @QueryParam("uri") final URL uri,
             @QueryParam("parser")
             @DefaultValue("XHTML") final Format format) {
         return new StreamingOutput() {
             public void write(OutputStream output) throws IOException, WebApplicationException {
                 StatementSink sink =
                         new NTripleSink(output, comment, "Origin: <" + uri + ">");
                 parse(uri, format, sink);
             }
         };
     }
 
     @GET
     @Produces("application/turtle")
     public StreamingOutput getTurtle(
             @QueryParam("uri") final URL uri,
             @QueryParam("parser")
             @DefaultValue("XHTML") final Format format) {
         return getNTriples(uri, format);
     }
 
     protected void parse(URL url, Format format, StatementSink sink) {
         try {
             XMLReader parser = ParserFactory.createReaderForFormat(sink, format);
             parser.parse(url.toString()); // Ought to change this based on format
         } catch (SAXException ex) {
             throw new WebApplicationException(ex);
         } catch (IOException ex) {
             throw new WebApplicationException(ex);
         }
     }
 }
