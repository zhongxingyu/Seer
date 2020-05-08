 package cz.cuni.mff.odcleanstore.engine.outputws.output;
 
 import cz.cuni.mff.odcleanstore.configuration.OutputWSConfig;
 import cz.cuni.mff.odcleanstore.conflictresolution.CRQuad;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadata;
 import cz.cuni.mff.odcleanstore.conflictresolution.NamedGraphMetadataMap;
 import cz.cuni.mff.odcleanstore.qualityassessment.impl.QualityAssessorImpl.GraphScoreWithTrace;
 import cz.cuni.mff.odcleanstore.qualityassessment.rules.QualityAssessmentRule;
 import cz.cuni.mff.odcleanstore.queryexecution.BasicQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.MetadataQueryResult;
 import cz.cuni.mff.odcleanstore.queryexecution.QueryResultBase;
 import cz.cuni.mff.odcleanstore.queryexecution.impl.PrefixMapping;
 import cz.cuni.mff.odcleanstore.shared.Utils;
 
 import com.hp.hpl.jena.datatypes.RDFDatatype;
 import com.hp.hpl.jena.graph.Node;
 
 import de.fuberlin.wiwiss.ng4j.Quad;
 
 import org.restlet.data.MediaType;
 import org.restlet.data.Reference;
 import org.restlet.representation.Representation;
 import org.restlet.representation.WriterRepresentation;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Returns a representation of a query result in a user-friendly HTML document.
  * 
  * TODO: process labels?
  * 
  * @author Jan Michelfeit
  */
 public class HTMLFormatter extends ResultFormatterBase {
     
     private static final String HTML_HEADER_COLOR = "FFE677";  
     private static final String HTML_EVEN_COLOR = "FFEEA6";  
     private static final String HTML_ODD_COLOR = "FFF5EC";  
     
     /** Configuration of the output webservice from the global configuration file. */
     private OutputWSConfig outputWSConfig;
     
     /** Namespace prefix mappings. */
     private PrefixMapping prefixMapping;
 
     /**
      * Creates a new instance.
      * @param outputWSConfig configuration of the output webservice from the global configuration file
      * @param prefixMapping namespace prefix mappings
      */
     public HTMLFormatter(OutputWSConfig outputWSConfig, PrefixMapping prefixMapping) {
         this.outputWSConfig = outputWSConfig;
         this.prefixMapping = prefixMapping;
     }
 
     @Override
     public Representation format(BasicQueryResult result, Reference requestReference) {
         WriterRepresentation representation = new BasicQueryHTMLRepresentation(result, requestReference);
         representation.setCharacterSet(OUTPUT_CHARSET);
         return representation;
     }
 
     @Override
     public Representation format(MetadataQueryResult metadataResult, GraphScoreWithTrace qaResult,
             long totalTime, Reference requestReference) {
 
         WriterRepresentation representation = new MetadataQueryHTMLRepresentation(
                 metadataResult, qaResult, totalTime, requestReference);
         representation.setCharacterSet(OUTPUT_CHARSET);
         return representation;
     }
 
     /** The actual representation of the result HTML document. */
     private abstract class HTMLRepresentationBase extends WriterRepresentation {
         /** Representation of the requested URI. */
         private Reference requestReference;
         
         /** Query result. */
         private QueryResultBase queryResult;
 
         /**
          * Initialize.
          * @param queryResult query result
          * @param requestReference representation of the requested URI
          */
         public HTMLRepresentationBase(QueryResultBase queryResult, Reference requestReference) {
             super(MediaType.TEXT_HTML);
             this.requestReference = requestReference;
             this.queryResult = queryResult;
         }
 
         @Override
         public abstract void write(Writer writer) throws IOException;
 
         /**
          * Write start of the HTML document.
          * @param writer output writer
          * @param queryResult query result
          * @param executionTime execution time of the query
          * @throws IOException if an I/O error occurs
          */
         protected void writeHeader(Writer writer, QueryResultBase queryResult, Long executionTime) throws IOException {
             writer.write("<!DOCTYPE html>"
                     + "\n<html lang=\"en\">" 
                     + "\n<head>"
                     + "\n <meta charset=\"utf-8\" />"
                     + "\n <title>");
             writer.write(queryResult.getQueryType().toString());
             writer.write(" query</title>" 
                     + "\n</head>"
                     + "\n<body>" 
                     + "\n");
             writer.write(" <p>");
             if (queryResult.getQuery() != null) {
                 switch (queryResult.getQueryType()) {
                 case KEYWORD:
                     writer.write("Keyword query for <code>");
                     writer.write(queryResult.getQuery());
                     writer.write("</code>.");
                     break;
                 case URI:
                     writer.write("URI query for &lt;");
                     writer.write(queryResult.getQuery());
                     writer.write("&gt;.");
                     break;
                 case METADATA:
                     writer.write("Metadata query for named graph &lt;");
                     writer.write(queryResult.getQuery());
                     writer.write("&gt;.");
                     break;
                 case NAMED_GRAPH:
                     writer.write("Named graph query for &lt;");
                     writer.write(queryResult.getQuery());
                     writer.write("&gt;.");
                     break;
                 default:
                     writer.write("Query <code>");
                     writer.write(queryResult.getQuery());
                     writer.write("</code>.");
                 }
             }
             if (executionTime != null) {
                 writer.write(" Query executed in ");
                 writer.write(formatExecutionTime(executionTime));
                 writer.write('.');
             }
             writer.write("</p>\n");
         }
 
         /**
          * Write table with metadata.
          * @param writer output writer
          * @param metadataMap metadata for graphs in the result
          * @throws IOException if an I/O error occurs
          */
         protected void writeMetadata(Writer writer, NamedGraphMetadataMap metadataMap) throws IOException {
             writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
             writer.write("  <tr style=\"background-color: #");
             writer.write(HTML_HEADER_COLOR);
             writer.write(";\"><th>Named graph</th><th>Data source</th><th>Inserted at</th>"
                     + "<th>Graph score</th><th>License</th></tr>");
             int row = 0;
             for (NamedGraphMetadata metadata : metadataMap.listMetadata()) {
                 writeOpeningTr(writer, ++row);
                 writer.write("<td>");
                 writeRelativeLink(
                         writer, 
                         getRequestForMetadata(metadata.getNamedGraphURI()),
                         getPrefixedURI(metadata.getNamedGraphURI()),
                         "Metadata query");
                 writer.write("</td><td>");
                 Collection<String> sourceList = metadata.getSources();
                 if (sourceList != null) {
                     boolean isFirst = true;
                     for (String source : sourceList) {
                         if (!isFirst) {
                             writer.write(", ");
                         }
                         writeAbsoluteLink(writer, source, source);
                         isFirst = false;
                     }
                 }
                 writer.write("</td><td>");
                 if (metadata.getInsertedAt() != null) {
                     writer.write(formatDate(metadata.getInsertedAt()));
                 }
                 writer.write("</td><td>");
                 if (metadata.getScore() != null) {
                     writer.write(metadata.getScore().toString());
                 }
                 writer.write("</td><td>");
                 List<String> licenseList = metadata.getLicences();
                 if (licenseList != null) {
                     boolean isFirst = true;
                     for (String license : licenseList) {
                         if (!isFirst) {
                             writer.write(", ");
                         }
                         writer.write(license);
                         isFirst = false;
                     }
                 }
                 writer.write("</td></tr>\n");
             }
             writer.write(" </table>\n");
         }
 
         /**
          * Write end of the HTML document.
          * @param writer output writer
          * @throws IOException if an I/O error occurs
          */
         protected void writerFooter(Writer writer) throws IOException {
             writer.write("\n</body>\n</html>");
         }
 
         /**
          * Write a single node.
          * @param writer output writer
          * @param node RDF node
          * @throws IOException if an I/O error occurs
          */
         protected void writeNode(Writer writer, Node node) throws IOException {
             if (node.isURI()) {
                 String text = getPrefixedURI(node.getURI());
                 assert queryResult.getQuery() != null;
                 if (queryResult.getQuery().equals(text) || queryResult.getQuery().equals(node.getURI())) {
                     writer.write("<em>");
                     writeRelativeLink(writer, getRequestForURI(node.getURI()), text, "URI query");
                     writer.write("</em>");
                 } else {
                     writeRelativeLink(writer, getRequestForURI(node.getURI()), text, "URI query");
                 }
             } else if (node.isLiteral()) {
                 String text = formatLiteral(node);
                 assert queryResult.getQuery() != null;
                 if (queryResult.getQuery().equals(node.getLiteralLexicalForm())) {
                     writer.write("<em>");
                     writeRelativeLink(writer, getRequestForKeyword(node.getLiteralLexicalForm()), text, "Keyword query");
                     writer.write("</em>");                    
                 } else {
                     writeRelativeLink(writer, getRequestForKeyword(node.getLiteralLexicalForm()), text, "Keyword query");
                 }
             } else if (node.isBlank()) {
                 String uri = "nodeID://" + node.getBlankNodeLabel();
                 assert queryResult.getQuery() != null;
                 if (queryResult.getQuery().equals(uri)) {
                     writer.write("<em>");
                     writeRelativeLink(writer, getRequestForURI(uri), "_:" + node.getBlankNodeLabel(), "URI query");
                     writer.write("</em>");
                 } else {
                     writeRelativeLink(writer, getRequestForURI(uri), "_:" + node.getBlankNodeLabel(), "URI query");
                 }
                 
             } else {
                 writer.write(node.toString());
             }
         }
         
         /**
          * Return uri with namespace shortened to prefix if possible.
          * @param uri uri to format
          * @return uri with namespace shortened to prefix if possible
          */
         protected String getPrefixedURI(String uri) {
             if (Utils.isNullOrEmpty(uri)) {
                 return uri;
             }
             int namespacePartLength = Math.max(uri.lastIndexOf('/'), uri.lastIndexOf('#')) + 1; // use a simple heuristic
             String prefix = 0 < namespacePartLength && namespacePartLength < uri.length()
                     ? prefixMapping.getPrefix(uri.substring(0, namespacePartLength))
                     : null;
             return (prefix == null)
                     ? uri
                     : prefix + ":" + uri.substring(namespacePartLength);
         }
         
         /**
          * Format a literal value for output.
          * @param literalNode a literal node (literalNode.isLiteral() must return true!)
          * @return literal value formatted for output
          */
         protected String formatLiteral(Node literalNode) {
             assert literalNode.isLiteral();
             StringBuilder result = new StringBuilder();
             String lang = literalNode.getLiteralLanguage();
             RDFDatatype dtype = literalNode.getLiteralDatatype();
             
             result.append('"');
             result.append(literalNode.getLiteralLexicalForm());
             result.append('"');
             if (!Utils.isNullOrEmpty(lang)) {
                 result.append("@").append(lang);
             }
             if (dtype != null) {
                 result.append(" ^^").append(getPrefixedURI(dtype.getURI()));
             }
             return result.toString();
         }
         /**
          * Write a relative hyperlink.
          * @param writer output writer
          * @param uri URI of the hyperlink
          * @param text text of the hyperlink
          * @param title title of the hyperlink
          * @throws IOException if an I/O error occurs
          */
         protected void writeRelativeLink(Writer writer, CharSequence uri, String text, String title) throws IOException {
             writer.append("<a title=\"")
                     .append(title)
                     .append("\" href=\"/")
                     .append(escapeHTML(uri))
                     .append("\">")
                     .append(text)
                     .append("</a>");
         }
 
         /**
          * Write an absolute hyperlink.
          * @param writer output writer
          * @param uri URI of the hyperlink
          * @param text text of the hyperlink
          * @throws IOException if an I/O error occurs
          */
         protected void writeAbsoluteLink(Writer writer, CharSequence uri, String text) throws IOException {
             writer.append("<a href=\"")
                     .append(escapeHTML(uri))
                     .append("\">")
                     .append(text)
                     .append("</a>");
         }
 
         /**
          * Returns a URI of a URI query request with other settings same as for the current request.
          * @param uri the requested URI
          * @return URI of the query request
          * @throws UnsupportedEncodingException exception
          */
         protected CharSequence getRequestForURI(String uri) throws UnsupportedEncodingException {
             StringBuilder result = new StringBuilder();
             result.append(outputWSConfig.getUriPath());
             result.append("?uri=");
             result.append(URLEncoder.encode(uri, "UTF-8"));
             result.append("&");
             result.append(requestReference.getQuery());
             return result.toString();
         }
 
         /**
          * Returns a URI of a keyword query request with other settings same as for the current request.
          * @param keyword the searched keyword
          * @return URI of the keyword request
          * @throws UnsupportedEncodingException exception
          */
         protected CharSequence getRequestForKeyword(String keyword) throws UnsupportedEncodingException {
             StringBuilder result = new StringBuilder();
             result.append(outputWSConfig.getKeywordPath());
             result.append("?kw=");
             result.append(URLEncoder.encode(keyword, "UTF-8"));
             result.append("&");
             result.append(requestReference.getQuery());
             return result.toString();
         }
 
         /**
          * Returns a URI of a metadata query request.
          * @param namedGraphURI the requested named graph
          * @return URI of the metadata request
          * @throws UnsupportedEncodingException exception
          */
         protected CharSequence getRequestForMetadata(String namedGraphURI) throws UnsupportedEncodingException {
             StringBuilder result = new StringBuilder();
             result.append(outputWSConfig.getMetadataPath());
             result.append("?uri=");
             result.append(URLEncoder.encode(namedGraphURI, "UTF-8"));
             result.append("&format=HTML");
             return result.toString();
         }
         
         /**
          * Returns a URI of a named graph query request.
          * @param namedGraphURI the requested named graph
          * @return URI of the named graph request
          * @throws UnsupportedEncodingException exception
          */
         protected CharSequence getRequestForNamedGraph(String namedGraphURI) throws UnsupportedEncodingException {
             StringBuilder result = new StringBuilder();
             result.append(outputWSConfig.getNamedGraphPath());
             result.append("?uri=");
             result.append(URLEncoder.encode(namedGraphURI, "UTF-8"));
             result.append("&format=HTML");
             return result.toString();
         }
 
         /**
          * Return a text escaped for use in HTML.
          * @param text text to escape
          * @return escaped text
          */
         protected String escapeHTML(CharSequence text) {
             return text.toString()
                     .replace("&", "&amp;")
                     .replace("<", "&lt;")
                     .replace(">", "&gt;")
                     .replace("\"", "&quot;")
                     .replace("'", "&#x27;")
                     .replace("/", "&#x2F;");
         }
         
         /**
          * Write an opening &lt;tr&gt; tag with the correct background color.
          * @param writer writer
          * @param rowIndex row index
          * @throws IOException exception
          */
         protected void writeOpeningTr(Writer writer, int rowIndex) throws IOException {
             writer.write("  <tr style=\"background-color: #");
             if (rowIndex % 2 == 0) {
                 writer.write(HTML_EVEN_COLOR);
             } else {
                 writer.write(HTML_ODD_COLOR);
             }
             writer.write(";\">");
         }
     }
 
     /**
      * Response representation for basic (URI/KW) query.
      */
     private class BasicQueryHTMLRepresentation extends HTMLRepresentationBase {
         /** Query result. */
         private BasicQueryResult queryResult;
 
         /**
          * Initialize.
          * @param queryResult query result
          * @param requestReference representation of the requested URI
          */
         public BasicQueryHTMLRepresentation(BasicQueryResult queryResult, Reference requestReference) {
             super(queryResult, requestReference);
             this.queryResult = queryResult;
         }
 
         @Override
         public void write(Writer writer) throws IOException {
             writeHeader(writer, queryResult, queryResult.getExecutionTime());
             writeResultQuads(writer);
             writer.write("\n <br />Source graphs:\n");
             writeMetadata(writer, queryResult.getMetadata());
             writerFooter(writer);
         }
 
         /**
          * Write table with result quads.
          * @param writer output writer
          * @throws IOException if an I/O error occurs
          */
         private void writeResultQuads(Writer writer) throws IOException {
             writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
             writer.write("  <tr style=\"background-color: #");
             writer.write(HTML_HEADER_COLOR);
             writer.write(";\"><th>Subject</th><th>Predicate</th><th>Object</th>"
                     + "<th>Quality</th><th>Source named graphs</th></tr>\n");
             int row = 0;
             for (CRQuad crQuad : queryResult.getResultQuads()) {
                 writeOpeningTr(writer, ++row);
                 writer.write("<td>");
                 writeNode(writer, crQuad.getQuad().getSubject());
                 writer.write("</td><td>");
                 writer.write(getPrefixedURI(crQuad.getQuad().getPredicate().toString()));
                 writer.write("</td><td>");
                 writeNode(writer, crQuad.getQuad().getObject());
                 writer.write("</td><td>");
                 writer.write(String.format(Locale.ROOT, "%.5f", crQuad.getQuality()));
                 writer.write("</td><td>");
                 boolean first = true;
                 for (String sourceURI : crQuad.getSourceNamedGraphURIs()) {
                     if (!first) {
                         writer.write(", ");
                     }
                     first = false;
                     writeRelativeLink(writer, getRequestForNamedGraph(sourceURI), getPrefixedURI(sourceURI), "Named graph query");
                 }
                 writer.write("</td></tr>\n");
             }
             writer.write(" </table>\n");
         }
     }
 
     /**
      * Response representation for metadata query.
      */
     private class MetadataQueryHTMLRepresentation extends HTMLRepresentationBase {
         /** Result of metadata query about the requested named graph. */
         private MetadataQueryResult metadataResult;
 
         /** Result of quality assessment over the given named graph. Can be null. */
         private GraphScoreWithTrace qaResult;
 
         /** Execution time of the query. */
         private long totalTime;
 
         /**
          * Initialize.
          * @param metadataResult result of metadata query about the requested named graph
          * @param qaResult result of quality assessment over the given named graph; can be null
          * @param totalTime execution time of the query
          * @param requestReference representation of the requested URI
          */
         public MetadataQueryHTMLRepresentation(MetadataQueryResult metadataResult,
                 GraphScoreWithTrace qaResult, long totalTime, Reference requestReference) {
 
             super(metadataResult, requestReference);
             this.metadataResult = metadataResult;
             this.qaResult = qaResult;
             this.totalTime = totalTime;
         }
 
         @Override
         public void write(Writer writer) throws IOException {
             // Header
             writeHeader(writer, metadataResult, totalTime);
 
             // Basic metadata
             writer.write("\n Basic metadata:\n");
             writeMetadata(writer, metadataResult.getMetadata());
 
             // QA results
             if (qaResult != null) {
                 writer.write("\n <br />Total Quality Assessment score: ");
                writer.write(String.format("%.5f", qaResult.getScore()));
                 writer.write("\n <br />Quality Assessment rule violations:\n");
                 writeQARules(writer, qaResult.getTrace());
             }
 
             // Additional provenance metadata
             if (!metadataResult.getProvenanceMetadata().isEmpty()) {
                 writer.write("\n <br />Additional provenance metadata:\n");
                 writeTriples(writer, metadataResult.getProvenanceMetadata());
             }
 
             // Footer
             writerFooter(writer);
         }
 
         /**
          * Write table with quads converted to triples.
          * @param writer output writer
          * @param quads quads to write
          * @throws IOException if an I/O error occurs
          */
         private void writeTriples(Writer writer, Collection<Quad> quads) throws IOException {
             writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
             writer.write("  <tr style=\"background-color: #");
             writer.write(HTML_HEADER_COLOR);
             writer.write(";\"><th>Subject</th><th>Predicate</th><th>Object</th></tr>\n");
             int row = 0;
             for (Quad quad : quads) {
                 writeOpeningTr(writer, ++row);
                 writer.write("<td>");
                 writeNode(writer, quad.getSubject());
                 writer.write("</td><td>");
                 writer.write(getPrefixedURI(quad.getPredicate().toString()));
                 writer.write("</td><td>");
                 writeNode(writer, quad.getObject());
                 writer.write("</td></tr>\n");
             }
             writer.write(" </table>\n");
         }
 
         /**
          * Write table with violated QA rules.
          * @param writer output writer
          * @param qaRules violated QA rules
          * @throws IOException if an I/O error occurs
          */
         private void writeQARules(Writer writer, Collection<QualityAssessmentRule> qaRules) throws IOException {
             writer.write(" <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">\n");
             writer.write("  <tr style=\"background-color: #");
             writer.write(HTML_HEADER_COLOR);
             writer.write(";\"><th>Rule description</th><th>Score decreased by</th></tr>\n");
             int row = 0;
             for (QualityAssessmentRule rule : qaRules) {
                 writeOpeningTr(writer, ++row);
                 writer.write("<td>");
                 writer.write(rule.getDescription());
                 writer.write("</td><td>");
                writer.write(String.format("%.5f", rule.getCoefficient()));
                 writer.write("</td></tr>\n");
             }
             writer.write(" </table>\n");
         }
     }
 }
