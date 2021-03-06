 package org.esa.beam.framework.gpf.main;
 
 import com.bc.ceres.binding.ConversionException;
 import com.bc.ceres.binding.ValidationException;
 import com.bc.ceres.binding.ValueContainer;
 import com.thoughtworks.xstream.io.xml.xppdom.Xpp3Dom;
 import org.esa.beam.framework.dataio.ProductIO;
 import org.esa.beam.framework.datamodel.Product;
 import org.esa.beam.framework.gpf.GPF;
 import org.esa.beam.framework.gpf.OperatorException;
 import org.esa.beam.framework.gpf.OperatorSpi;
 import org.esa.beam.framework.gpf.annotations.ParameterDescriptorFactory;
 import org.esa.beam.framework.gpf.graph.Graph;
 import org.esa.beam.framework.gpf.graph.GraphException;
 import org.esa.beam.framework.gpf.graph.Node;
 import org.esa.beam.framework.gpf.graph.NodeSource;
 import org.esa.beam.framework.gpf.operators.common.ReadOp;
 import org.esa.beam.framework.gpf.operators.common.WriteOp;
 
 import javax.media.jai.JAI;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 /**
  * The common command-line tool for the GPF.
  * For usage, see {@link org/esa/beam/framework/gpf/main/CommandLineUsage.txt}.
  */
 class CommandLineTool {
 
     private final CommandLineContext commandLineContext;
     static final String TOOL_NAME = "gpt";
     static final String DEFAULT_TARGET_FILEPATH = "./target.dim";
     static final String DEFAULT_FORMAT_NAME = ProductIO.DEFAULT_FORMAT_NAME;
 
     static {
         GPF.getDefaultInstance().getOperatorSpiRegistry().loadOperatorSpis();
     }
 
     /**
      * Constructs a new tool.
      */
     CommandLineTool() {
         this(new DefaultCommandLineContext());
     }
 
     /**
      * Constructs a new tool with the given context.
      *
      * @param commandLineContext The context used to run the tool.
      */
     CommandLineTool(CommandLineContext commandLineContext) {
         this.commandLineContext = commandLineContext;
     }
 
     void run(String[] args) throws Exception {
 
         CommandLineArgs lineArgs = new CommandLineArgs(args);
 
         if (lineArgs.isHelpRequested()) {
             if (lineArgs.getOperatorName() != null) {
                 commandLineContext.print(CommandLineUsage.getUsageText(lineArgs.getOperatorName()));
             } else {
                 commandLineContext.print(CommandLineUsage.getUsageText());
             }
             return;
         }
 
         try {
             run(lineArgs);
         } catch (Exception e) {
             if (lineArgs.isStackTraceDump()) {
                 e.printStackTrace(System.err);
             }
             throw e;
         }
     }
 
     private void run(CommandLineArgs lineArgs) throws ValidationException, ConversionException, IOException, GraphException {
         JAI.getDefaultInstance().getTileCache().setMemoryCapacity(lineArgs.getTileCacheCapacity());
 
         if (lineArgs.getOperatorName() != null) {
             Map<String, Object> parameters = getParameterMap(lineArgs);
             Map<String, Product> sourceProducts = getSourceProductMap(lineArgs);
             String opName = lineArgs.getOperatorName();
             Product targetProduct = createOpProduct(opName, parameters, sourceProducts);
             String filePath = lineArgs.getTargetFilepath();
             String formatName = lineArgs.getTargetFormatName();
             writeProduct(targetProduct, filePath, formatName);
         } else if (lineArgs.getGraphFilepath() != null) {
             Map<String, String> sourceNodeIdMap = getSourceNodeIdMap(lineArgs);
             Map<String, String> templateMap = new TreeMap<String, String>(sourceNodeIdMap);
             if (lineArgs.getParameterFilepath() != null) {
                 templateMap.putAll(readParameterFile(lineArgs.getParameterFilepath()));
             }
             templateMap.putAll(lineArgs.getParameterMap());
             Graph graph = readGraph(lineArgs.getGraphFilepath(), templateMap);
             Node lastNode = graph.getNode(graph.getNodeCount() - 1);
             SortedMap<String, String> sourceFilepathsMap = lineArgs.getSourceFilepathMap();
             String readOperatorAlias = OperatorSpi.getOperatorAlias(ReadOp.class);
             for (Entry<String, String> entry : sourceFilepathsMap.entrySet()) {
                 String sourceId = entry.getKey();
                 String sourceFilepath = entry.getValue();
                 String sourceNodeId = sourceNodeIdMap.get(sourceId);
                 if (graph.getNode(sourceNodeId) == null) {
                     Node sourceNode = new Node(sourceNodeId, readOperatorAlias);
                     Xpp3Dom parameters = new Xpp3Dom("parameters");
                    Xpp3Dom filePath = new Xpp3Dom("filePath");
                     filePath.setValue(sourceFilepath);
                     parameters.addChild(filePath);
                     sourceNode.setConfiguration(parameters);
                     graph.addNode(sourceNode);
                 }
             }
 
             String writeOperatorAlias = OperatorSpi.getOperatorAlias(WriteOp.class);
             Node targetNode = new Node("WriteProduct$" + lastNode.getId(), writeOperatorAlias);
             targetNode.addSource(new NodeSource("input", lastNode.getId()));
             Xpp3Dom configDom = new Xpp3Dom("parameters");
            Xpp3Dom dom1 = new Xpp3Dom("filePath");
             dom1.setValue(lineArgs.getTargetFilepath());
             configDom.addChild(dom1);
             Xpp3Dom dom2 = new Xpp3Dom("formatName");
             dom2.setValue(lineArgs.getTargetFormatName());
             configDom.addChild(dom2);
             targetNode.setConfiguration(configDom);
             graph.addNode(targetNode);
 
             executeGraph(graph);
         }
     }
 
     private Map<String, Object> getParameterMap(CommandLineArgs lineArgs) throws ValidationException, ConversionException {
         HashMap<String, Object> parameters = new HashMap<String, Object>();
         ValueContainer container = ParameterDescriptorFactory.createMapBackedOperatorValueContainer(lineArgs.getOperatorName(), parameters);
         Map<String, String> parameterMap = lineArgs.getParameterMap();
         for (Entry<String, String> entry : parameterMap.entrySet()) {
             String paramName = entry.getKey();
             String paramValue = entry.getValue();
             container.setFromText(paramName, paramValue);
         }
         return parameters;
     }
 
     private Map<String, Product> getSourceProductMap(CommandLineArgs lineArgs) throws IOException {
         SortedMap<File, Product> fileToProductMap = new TreeMap<File, Product>();
         SortedMap<String, Product> productMap = new TreeMap<String, Product>();
         SortedMap<String, String> sourceFilepathsMap = lineArgs.getSourceFilepathMap();
         for (Entry<String, String> entry : sourceFilepathsMap.entrySet()) {
             String sourceId = entry.getKey();
             String sourceFilepath = entry.getValue();
             Product product = addProduct(sourceFilepath, fileToProductMap);
             productMap.put(sourceId, product);
         }
         return productMap;
     }
 
     private Product addProduct(String sourceFilepath, Map<File, Product> fileToProductMap) throws IOException {
         File sourceFile = new File(sourceFilepath).getCanonicalFile();
         Product product = fileToProductMap.get(sourceFile);
         if (product == null) {
             String s = sourceFile.getPath();
             product = readProduct(s);
             if (product == null) {
                 throw new IOException("No approriate product reader found for " + sourceFile);
             }
             fileToProductMap.put(sourceFile, product);
         }
         return product;
     }
 
     private Map<String, String> getSourceNodeIdMap(CommandLineArgs lineArgs) throws IOException {
         SortedMap<File, String> fileToNodeIdMap = new TreeMap<File, String>();
         SortedMap<String, String> nodeIdMap = new TreeMap<String, String>();
         SortedMap<String, String> sourceFilepathsMap = lineArgs.getSourceFilepathMap();
         for (Entry<String, String> entry : sourceFilepathsMap.entrySet()) {
             String sourceId = entry.getKey();
             String sourceFilepath = entry.getValue();
             String nodeId = addNodeId(sourceFilepath, fileToNodeIdMap);
             nodeIdMap.put(sourceId, nodeId);
         }
         return nodeIdMap;
     }
 
     private String addNodeId(String sourceFilepath, Map<File, String> fileToNodeId) throws IOException {
         File sourceFile = new File(sourceFilepath).getCanonicalFile();
         String nodeId = fileToNodeId.get(sourceFile);
         if (nodeId == null) {
             nodeId = "ReadProduct$" + fileToNodeId.size();
             fileToNodeId.put(sourceFile, nodeId);
         }
         return nodeId;
     }
 
     public Product readProduct(String productFilepath) throws IOException {
         return commandLineContext.readProduct(productFilepath);
     }
 
     public void writeProduct(Product targetProduct, String filePath, String formatName) throws IOException {
         commandLineContext.writeProduct(targetProduct, filePath, formatName);
     }
 
     public Graph readGraph(String filepath, Map<String, String> parameterMap) throws IOException {
         return commandLineContext.readGraph(filepath, parameterMap);
     }
 
     public void executeGraph(Graph graph) throws GraphException {
         commandLineContext.executeGraph(graph);
     }
 
     public Map<String, String> readParameterFile(String propertiesFilepath) throws IOException {
         return commandLineContext.readParameterFile(propertiesFilepath);
     }
 
     private Product createOpProduct(String opName, Map<String, Object> parameters, Map<String, Product> sourceProducts) throws OperatorException {
         return commandLineContext.createOpProduct(opName, parameters, sourceProducts);
     }
 }
