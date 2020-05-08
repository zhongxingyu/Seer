 package uk.ac.ebi.fgpt.sampletab.tools.myeq;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.dom4j.DocumentException;
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SDRFNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
 import uk.ac.ebi.fgpt.sampletab.utils.ENAUtils;
 
 public class MageTabLoaderTask implements Runnable {
     
     private final File inFile;
     private final EntityMappingManager emMgr;
     
     private final MAGETABParser<MAGETABInvestigation> parser;
     private final List<ErrorItem> errorItems;
     
     private static final String ENA_SAMPLE = "ENA_SAMPLE";
     
     private static final String SAMPLEPATTERN = "[SED]RS[0-9]+";
     private static final String RUNPATTERN = "[SED]RR[0-9]+";
     private static final String EXPERIMENTPATTERN = "[SED]RX[0-9]+";
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     public MageTabLoaderTask(File inFile, EntityMappingManager emMgr){
         this.inFile = inFile;
         this.emMgr = emMgr;
         
         parser = new MAGETABParser<MAGETABInvestigation>();
         errorItems = new ArrayList<ErrorItem>();
 
         parser.addErrorItemListener(new ErrorItemListener() {
 
             public void errorOccurred(ErrorItem item) {
                 errorItems.add(item);
             }
         });
     }
     
     private String getSRASample(SourceNode node) {
         if (node.comments.containsKey(ENA_SAMPLE)) {
             if (node.comments.get(ENA_SAMPLE).size() != 1) {
                 throw new RuntimeException("Not one ENA_SAMPLE for "+node.getNodeName());
             }
             String sra = node.comments.get(ENA_SAMPLE).get(0);
             if (!sra.matches("[SED]RS[0-9]+")) {
                 throw new RuntimeException("Unrecognized ENA accession "+sra);
             }
             return sra;
         } else {
             return null;
         }
     }
     
     private String getSRASample(SampleNode node) {
         if (node.comments.containsKey(ENA_SAMPLE)) {
             if (node.comments.get(ENA_SAMPLE).size() != 1) {
                 throw new RuntimeException("Not one ENA_SAMPLE for "+node.getNodeName());
             }
             String sra = node.comments.get(ENA_SAMPLE).get(0);
             if (!sra.matches("[SED]RS[0-9]+")) {
                 throw new RuntimeException("Unrecognized ENA accession "+sra);
             }
             return sra;
         } else {
             return null;
         }
     }
     
     
     private Set<String> handleNode(Node node) throws DocumentException, IOException {
         boolean isSampleNode;
         synchronized (SampleNode.class) {
             isSampleNode = SampleNode.class.isInstance(node);
         }
         boolean isSourceNode;
         synchronized (SourceNode.class) {
             isSourceNode = SourceNode.class.isInstance(node);
         }
         
         Set<String> matches = new HashSet<String>();
         
         if (isSampleNode) {
             String sra = getSRASample((SampleNode) node);
             if (sra != null) {
                 matches.add(sra);
             }
             //some samples are not directly specified, but can be calculated by looking at 
             //run information
             for (Node child : node.getChildNodes()) {
                 matches.addAll(handleNode(child));
             }
             
         } else if (isSourceNode) {
            String sra = getSRASample((SourceNode) node);
             if (sra != null) {
                 matches.add(sra);
             }
             for (Node child : node.getChildNodes()) {
                 matches.addAll(handleNode(child));
             }
 
         } else {
             //some other type of node
             LinkedList<Node> stack = new LinkedList<Node>();
             stack.addAll(node.getChildNodes());
             while (stack.size() > 0) {
                 Node testNode = stack.pop();
                 boolean isSDRFNode;
                 synchronized (SDRFNode.class) {
                     isSDRFNode = SDRFNode.class.isInstance(testNode);
                 }
                 if (isSDRFNode) {
                     SDRFNode testSDRFNode = (SDRFNode) testNode;
                     for (String value : testSDRFNode.values()) {
                         if (value.matches(SAMPLEPATTERN)) {
                             matches.add(value);
                         } else if (value.matches(RUNPATTERN)) {
                             matches.addAll(ENAUtils.getSamplesForRun(value));
                         } else if (value.matches(EXPERIMENTPATTERN)) {
                             matches.addAll(ENAUtils.getSamplesForExperiment(value));
                         } 
                     }
                 }
             }
         }
         
         return matches;
     }
     
     @Override
     public void run() {
 
         MAGETABInvestigation mt = null;
         try {
             mt = parser.parse(inFile);
         } catch (ParseException e) {
             log.error("Problem parsing "+inFile, e);
             return;
         }
         try {
             for (SDRFNode node : mt.SDRF.getAllNodes()) {
     
                 Set<String> matches = new HashSet<String>();
                 
                 boolean isSampleNode;
                 synchronized (SampleNode.class) {
                     isSampleNode = SampleNode.class.isInstance(node);
                 }
                 boolean isSourceNode;
                 synchronized (SourceNode.class) {
                     isSourceNode = SourceNode.class.isInstance(node);
                 }
                 
                 if (isSampleNode) {
                     String sra = getSRASample((SampleNode) node);
                     if (sra != null) {
                         matches.add(sra);
                     } else {
                         matches.addAll(handleNode(node));
                     }
                 } else if (isSourceNode) {
                    String sra = getSRASample((SourceNode) node);
                     if (sra != null) {
                         matches.add(sra);
                     } else {
                         matches.addAll(handleNode(node));
                     }
                 }
                 
                 for (String match : matches) {
                     log.info("Match "+match+" "+mt.getAccession()+":"+node.getNodeName());
                     //TODO STORE
                 }
             }
         } catch (IOException e) {
             log.error("Problem handling "+inFile, e);
             return;
         } catch (DocumentException e) {
             log.error("Problem handling "+inFile, e);
             return;
         }
         
     }
 }
