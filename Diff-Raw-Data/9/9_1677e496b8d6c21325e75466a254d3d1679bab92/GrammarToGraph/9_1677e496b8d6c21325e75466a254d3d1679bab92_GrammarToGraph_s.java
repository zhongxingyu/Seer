 package de.hszg.atocc.kfgedit.transform.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 import de.hszg.atocc.core.util.internal.ConverterException;
 
 import java.io.StringWriter;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.transform.TransformerConfigurationException;
 
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.ext.GraphMLExporter;
 import org.jgrapht.ext.StringEdgeNameProvider;
 import org.jgrapht.ext.StringNameProvider;
 import org.jgrapht.graph.DefaultDirectedGraph;
 import org.jgrapht.graph.DefaultEdge;
 import org.xml.sax.SAXException;
 
 public class GrammarToGraph extends AbstractXmlTransformationService {
 
     private final static String SPACE = " ";
 
     private GrammarService grammarService;
     private XmlUtilService xmlService;
 
     private Grammar grammar;
     private DirectedGraph<String, DefaultEdge> graph;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             validateInput("GRAMMAR");
 
             grammar = grammarService.grammarFrom(getInput());
 
             createGraph();
             createOutput();
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("GrammarToGraph|INVALID_INPUT", e);
         } catch (SerializationException | SAXException | TransformerConfigurationException
                 | ConverterException e) {
             throw new XmlTransformationException("GrammarToGraph|SERVICE_ERROR", e);
         }
 
         try {
             validateOutput("GRAMMAR");
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("GrammarToGraph|INVALID_OUTPUT", e);
         }
     }
 
     private void createOutput() throws SAXException, TransformerConfigurationException,
             ConverterException {
         final StringNameProvider<String> nameProvider = new StringNameProvider<>();
         final StringEdgeNameProvider<DefaultEdge> edgeNameProvider = new StringEdgeNameProvider<>();
 
         final GraphMLExporter<String, DefaultEdge> exporter = new GraphMLExporter<>(nameProvider,
                null, edgeNameProvider, null);
         
         final StringWriter writer = new StringWriter();
         exporter.export(writer, graph);
 
         setRawOutput(xmlService.stringToXml(writer.toString()));
     }
 
     private void createGraph() {
         graph = new DefaultDirectedGraph<>(DefaultEdge.class);
 
         createVertices();
         createEdges();
     }
 
     private void createVertices() {
         for (String lhs : grammar.getLeftHandSides()) {
             graph.addVertex(lhs);
         }
 
         for (String rhs : grammar.getAllRightHandSides()) {
             graph.addVertex(rhs);
         }
     }
 
     private void createEdges() {
         for (String lhs : grammar.getLeftHandSides()) {
             for (String rhs : grammar.getRightHandSidesFor(lhs)) {
                 graph.addEdge(lhs, rhs);
             }
         }
     }
 
     private void tryToGetRequiredServices() {
         grammarService = getService(GrammarService.class);
         xmlService = getService(XmlUtilService.class);
     }
 
 }
