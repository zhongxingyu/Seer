 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.enums.OssType;
 import org.amanzi.neo.core.enums.SectorIdentificationType;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.eclipse.swt.widgets.Display;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 /**
  * <p>
  * </p>
  * 
  * @author Saelenchits_N
  * @since 1.0.0
  */
 public class PerformanceCountersLoader extends AbstractLoader {
     private static boolean needParceHeader;
     
     private LuceneIndexService luceneService;
     private String luceneIndexName;
     private final ArrayList<String> possibleCellNames = new ArrayList<String>();
 
 
     private Node ossRoot;
     private Node fileNode;
     private Node lastChild;
     
     private final OssType ossType; 
     
     public PerformanceCountersLoader(OssType ossType, String directory, String datasetName, Display display) {
         initialize("Performance Counter", null, directory, display);
         basename = datasetName;
         this.ossType = ossType; 
         needParceHeader = true;
         getHeaderMap(1);
         init();
     }
     
 
     private void init() {
         possibleCellNames.add("cell name");
         possibleCellNames.add("cell");
     }
 
 
     @Override
     protected boolean needParceHeaders() {
         if (needParceHeader) {
             needParceHeader = false;
             return true;
         }
         return false;
     }
 
     @Override
     protected void parseLine(String line) {
         if (fileNode == null) {
             ossRoot = LoaderUtils.findOrCreateOSSNode(ossType, basename, neo);// TODO target
                                                                                    // type
             Pair<Boolean, Node> fileNodePair = NeoUtils.findOrCreateFileNode(neo, ossRoot, new File(basename).getName(), new File(basename).getName());
             fileNode = fileNodePair.getRight();
             lastChild = null;
 
             initializeLucene(ossRoot);
         }
         List<String> fields = splitLine(line);
         if (fields.size() < 2)
             return;
         if (this.isOverLimit())
             return;
         Map<String, Object> lineData = makeDataMap(fields);
         saveStatistic(lineData);
     }
 
     private void saveStatistic(Map<String, Object> lineData) {
         Transaction transaction = neo.beginTx();
         try {
             Node node = neo.createNode();
             NodeTypes.M.setNodeType(node, neo);
             for (Map.Entry<String, Object> entry : lineData.entrySet()) {
                 node.setProperty(entry.getKey(), entry.getValue());
 
                 indexData(entry, node);
             }
             node.setProperty(INeoConstants.PROPERTY_NAME_NAME, "APD counter");
             index(node);
 
             NeoUtils.addChild(fileNode, node, lastChild, neo);
             lastChild = node;
             storingProperties.values().iterator().next().incSaved();
         } finally {
             transaction.finish();
         }
     }
 
     private void indexData(Map.Entry<String, Object> entry, Node curentNode) {
         if (possibleCellNames.contains(entry.getKey())) {
             luceneService.index(curentNode, luceneIndexName, entry.getValue());
         }
     }
 
     private void initializeLucene(Node baseNode) {
         luceneService = NeoServiceProvider.getProvider().getIndexService();
         luceneIndexName = NeoUtils.getLuceneIndexKeyByProperty(baseNode, INeoConstants.SECTOR_ID_PROPERTIES, NodeTypes.M);
     }
 
     @Override
     protected String getPrymaryType(Integer key) {
         return NodeTypes.M.getId();
     }
 
     @Override
     protected Node getStoringNode(Integer key) {
         return ossRoot;
     }
 
     @Override
     public Node[] getRootNodes() {
         return new Node[] {ossRoot};
     }
     
     @Override
     public void finishUp() {
         getStoringNode(1).setProperty(INeoConstants.SECTOR_ID_TYPE, SectorIdentificationType.NAME.toString());
         super.finishUp();
     }
 
 }
