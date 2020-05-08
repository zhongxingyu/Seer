 package de.fuberlin.wiwiss.trust;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Node_URI;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.shared.PrefixMapping;
 import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 import de.fuberlin.wiwiss.ng4j.swp.vocabulary.FOAF;
 
 /**
 * @version $Id: ExplanationToHTMLRenderer.java,v 1.3 2005/05/24 13:53:24 maresch Exp $
  * @author Richard Cyganiak (richard@cyganiak.de)
  */
 public class ExplanationToHTMLRenderer {
     private Explanation expl;
     private TrustLayerGraph tlg;
     private PrefixMapping prefixes = new PrefixMappingImpl();
     private List detailsBuffer;
     private long timestamp;
     
     public ExplanationToHTMLRenderer(Explanation expl, TrustLayerGraph tlg) {
         this.expl = expl;
         this.detailsBuffer = new ArrayList();
         this.timestamp = System.currentTimeMillis();
         this.tlg = tlg;
     }
     
     public static String renderExplanationPart(ExplanationPart part, TrustLayerGraph tlg) {
         Explanation dummyExpl = new Explanation(
                 new Triple(Node.ANY, Node.ANY, Node.ANY),
                 TrustPolicy.TRUST_EVERYTHING);
         dummyExpl.addPart(part);
         return new ExplanationToHTMLRenderer(dummyExpl, tlg).getExplanationPartsAsHTML();
     }
     
     public void setPrefixes(PrefixMapping prefixes) {
         this.prefixes = prefixes;
     }
     
     public String getSubjectAsHTML() {
         return getNodeAsHTML(this.expl.getExplainedTriple().getSubject());
     }
     
     public String getPredicateAsHTML() {
         return getNodeAsHTML(this.expl.getExplainedTriple().getPredicate());
     }
     
     public String getObjectAsHTML() {
         return getNodeAsHTML(this.expl.getExplainedTriple().getObject());
     }
 
     public String getPolicyAsHTML() {
         return getNodeAsHTML(this.expl.getPolicyURI());
     }
 
     public String getExplanationPartsAsHTML() {
         if (this.expl.parts().isEmpty()) {
             return "<em>This policy does not generate explanations</em>";
         }
         StringBuffer result = new StringBuffer();
         renderExplanationParts(this.expl.parts(), result);
         return result.toString();
     }
     
     public String getDetailsAsHTML(){
         StringBuffer buffer = new StringBuffer();
         if(detailsBuffer.isEmpty()){
             buffer.append("No details.");
         }else{
             for(int i = 0; i < detailsBuffer.size(); i++){
                 int number = i + 1;
                 ExplanationPart detail = (ExplanationPart) detailsBuffer.get(i);
                 buffer.append("<p><a name='detail" + this.timestamp + "_" + i + "'/><b>Detail number " + number + "</b></p>");
                 buffer.append("<p>");
                 List parts = new ArrayList();
                 parts.add(detail);
                 renderExplanationParts(parts, buffer);
                 buffer.append("</p>");
             }
         }
         return buffer.toString();
     }
     
     public String getExplanationAsHTML() {
 	    return "<dl><dt><a name='expanation" + this.timestamp + "'/>Triple:</dt><dd>"
 	            + getSubjectAsHTML() + " " + getPredicateAsHTML() + " " + getObjectAsHTML() + " .</dd>"
 	            + "<dt>Policy:</dt><dd>" + getPolicyAsHTML() + "</dd>"
 	            + "<dt>Explanation:</dt><dd>" + getExplanationPartsAsHTML() + "</dd>"
                 + "<dt>Details:</dt><dd>" + getDetailsAsHTML() + "</dd></dl>";
     }
     
     private String getNodeAsHTML(Node node) {
         if (node == null) {
             return "<tt>[null]</tt>";
         }
         if (node.isLiteral()) {
             return escape(node.getLiteral().getLexicalForm());
         }
         if (node.isBlank()) {
             return "<tt>_:" + escape(node.getBlankNodeId().toString()) + "</tt>";
         }
         if(node.isURI()) {
             return "<a href=\"" + escape(node.getURI()) + "\">" + escape(findLabel((Node_URI) node)) + "</a>";
 //            return "<a href=\"" + escape(node.getURI()) + "\">" + escape(node.getURI()) + "</a>";
         }
         return "<tt>[null]</tt>";
     }
     
     private void renderExplanationParts(Collection parts, StringBuffer buffer) {
         if (parts.isEmpty()) {
             return;
         }
         buffer.append("<ul>");
         Iterator it = parts.iterator();
         while (it.hasNext()) {
             ExplanationPart part = (ExplanationPart) it.next();
             buffer.append("<li>");
             if (part.explanationNodes().isEmpty() && part.parts().isEmpty()) {
 	            buffer.append("<em>empty ExplanationPart</em>");
             } else {
                 Iterator it2 = part.explanationNodes().iterator();
                 while (it2.hasNext()) {
                     Node node = (Node) it2.next();
                     buffer.append(getNodeAsHTML(node));
                 }
                 
                 // add link to detials 
                 ExplanationPart detail = part.getDetails();
                 if(detail != null){
                     buffer.append("(<a href='#detail" + this.timestamp + "_" + detailsBuffer.size() + "'>detail number " + (detailsBuffer.size() + 1) + "</a>)");
                     detailsBuffer.add(detail);
                 }
                 renderExplanationParts(part.parts(), buffer);
             }
             buffer.append("</li>");
         }
         buffer.append("</ul>");
     }
     
     private String escape(String s) {
         s.replaceAll("&", "&amp;").replaceAll("<", "&lt;");
         s.replaceAll("ä", "&auml;").replaceAll("Ä", "&Auml;");
         s.replaceAll("ö", "&ouml;").replaceAll("Ö", "&Ouml;");
         s.replaceAll("ü", "&uuml;").replaceAll("Ü", "&Uuml;");
         s.replaceAll("ß", "&szlig;");
         return s;
     }
     
     private String findLabel(Node_URI uri){
         Triple triple = new Triple(uri, RDFS.Nodes.label , Node.ANY); 
        tlg.selectTrustPolicy("http://www.wiwiss.fu-berlin.de/suhl/bizer/TPL/TrustEveryting");
         Iterator it = tlg.find(triple);
         
         if(it.hasNext()){
             // if at least one label was found, take the first
             return ((Triple) it.next()).getObject().getLiteral().getLexicalForm();
         } else {
             triple = new Triple(uri, FOAF.name.getNode(), Node.ANY);
             it = tlg.find(triple);
             
             if(it.hasNext()){
                 return ((Triple) it.next()).getObject().getLiteral().getLexicalForm();
             } else {
                 // if no label was found try to prefix the uri
                 String label = this.prefixes.qnameFor(uri.getURI());
                 if (label == null) {
                     // if no prefix is available use the complete URI
                     label = uri.getURI();
                 }
                 return label;
             }
         }
     }
 }
