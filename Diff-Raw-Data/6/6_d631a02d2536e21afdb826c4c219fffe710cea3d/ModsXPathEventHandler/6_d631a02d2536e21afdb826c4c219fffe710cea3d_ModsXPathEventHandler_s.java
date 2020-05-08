 package dk.statsbiblioteket.newspaper.metadatachecker;
 
 import static dk.statsbiblioteket.util.Strings.getStackTrace;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
 import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
 import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
 import dk.statsbiblioteket.util.xml.DOM;
 import dk.statsbiblioteket.util.xml.XPathSelector;
 
 /**
  * This class uses xpath to validate metadata requirements for mods files that do no otherwise fit into the schematron
  * paradigm. This includes checking for consistency of the existence of brik files with the displayName attribute
  * in the mods file.
  */
 public class ModsXPathEventHandler extends DefaultTreeEventHandler {
 
    public static final String EDITION_REGEX = "^[0-9]{4}.*-[0-9]{2}$";
     private ResultCollector resultCollector;
     private List<String> briksInThisEdition;
     private Set<String> displayLabelsInThisEdition;
     private Document batchXmlStructure;
     private static final XPathSelector BATCH_XPATH_SELECTOR = DOM.createXPathSelector();
     private static final XPathSelector MODS_XPATH_SELECTOR = DOM.createXPathSelector("mods", "http://www.loc.gov/mods/v3");
     private BatchContext context;
 
     /**
      * Constructor for this class.
      * @param resultCollector the result collector to collect errors in
      * @param batchXmlStructure the complete structure of this batch as XML.
      * @param context the batch context
      */
     public ModsXPathEventHandler(ResultCollector resultCollector, BatchContext context, Document batchXmlStructure) {
         this.resultCollector = resultCollector;
         this.batchXmlStructure = batchXmlStructure;
         briksInThisEdition = new ArrayList<>();
         this.context = context;
     }
 
     /**
      * Whenever we reach the start of an edition node, this method reinitializes briksInThisEdition
      * with a list of brik-files in this edition node.
      * @param event the node-begins event.
      */
     @Override
     public void handleNodeBegin(NodeBeginsParsingEvent event) {
 
         String shortName = getLastTokenInPath(event.getName());
         if (shortName.matches(EDITION_REGEX)) {
             briksInThisEdition = new ArrayList<String>();
             displayLabelsInThisEdition = new HashSet<>();
             String xpathForBriks = "//node[@name='" + event.getName() + "']/node[ends-with(@shortName, 'brik')]/@shortName";
             NodeList nodeList = BATCH_XPATH_SELECTOR.selectNodeList(batchXmlStructure, xpathForBriks);
             for (int nodeNumber = 0; nodeNumber < nodeList.getLength(); nodeNumber++ ) {
                 Node node = nodeList.item(nodeNumber);
                 briksInThisEdition.add(node.getNodeValue().replace("-brik", ""));
             }
         }
     }
 
     @Override
     public void handleAttribute(AttributeParsingEvent event) {
         if (event.getName().endsWith("mods.xml")) {
             try {
                 doValidate(event);
             } catch (Exception e) {    //Fault Barrier
                 resultCollector.addFailure(
                         event.getName(),
                         "exception",
                         getClass().getSimpleName(),
                         "Error processing page-MODS metadata: " + e.toString(),
                         getStackTrace(e)
                 );
             }
         }
     }
 
     @Override
     public void handleNodeEnd(NodeEndParsingEvent event) {
         String shortName = getLastTokenInPath(event.getName());
         if (shortName.matches(EDITION_REGEX)) {
             validate2C10AllBriksInEdition(event);
         }
     }
 
     private void doValidate(AttributeParsingEvent event) {
         Document modsDocument;
         try {
             modsDocument = DOM.streamToDOM(event.getData());
             if (modsDocument == null) {
                 resultCollector.addFailure(
                         event.getName(),
                         "exception",
                         getClass().getSimpleName(),
                         "Could not parse xml");
                 return;
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         validate2C1(event, modsDocument);
         validate2C4(event, modsDocument);
         validate2C5(event, modsDocument);
         validate2C11(event, modsDocument);
         validate2C10(event, modsDocument);
     }
 
     /**
      * Checks that section titles are absent if option B7 is not chosen, and vice versa.
      * @param event the event corresponding to the mods file being checked.
      * @param modsDocument the xml representation of the file.
      */
     private void validate2C1(AttributeParsingEvent event, Document modsDocument) {
         if (context.getBatchOptions() == null) {
             resultCollector.addFailure(event.getName(),
                     "metadata",
                     getClass().getSimpleName(),
                     "2C-1: Couldn't read batch options from mfpak. Got null value.",
                     context.getBatch().getBatchID()
             );
             return;
         } else if (!context.getBatchOptions().isOptionB7()) {
             String sectionLabelXpath = "mods:mods/mods:part/mods:detail[@type='sectionLabel']";
             NodeList nodes = MODS_XPATH_SELECTOR.selectNodeList(modsDocument, sectionLabelXpath);
             if (nodes == null || nodes.getLength() == 0) {
                 return;
             } else {
                 resultCollector.addFailure(
                         event.getName(),
                         "metadata",
                         getClass().getSimpleName(),
                         "2C-1: Found section entitled " + nodes.item(0).getTextContent() + " for the page "
                                 + event.getName() + " although Option B7 (Section Titles) was not chosen for the batch "
                                 + context.getBatch().getBatchID(),
                         sectionLabelXpath );
             }
         } else {
             String sectionLabelXpath = "mods:mods/mods:part/mods:detail[@type='sectionLabel']";
             NodeList nodes = MODS_XPATH_SELECTOR.selectNodeList(modsDocument, sectionLabelXpath);
             if (nodes == null || nodes.getLength() == 0) {
                 resultCollector.addFailure(
                         event.getName(),
                         "metadata",
                         getClass().getSimpleName(),
                         "2C-1: Did not find section for the page " + event.getName()
                         + " although Option B7 (Section Titles) was chosen for the batch " + context.getBatch().getBatchID(),
                         sectionLabelXpath );
             } else {
                 return;
             }
         }
     }
 
     /**
      * Valid against the requirement that the displayLabel attribute implies the existence of a corresponding briks
      * file. Also display labels are remembered, so it can be checked that all briks were described.
      * @param event
      * @param modsDocument
      */
     private void validate2C10(AttributeParsingEvent event, Document modsDocument) {
         String display = "mods:mods/mods:relatedItem/mods:note[@type='noteAboutReproduction' and @displayLabel]";
         String name = getLastTokenInPath(event.getName());
         name = getBrikName(name);
         boolean brikExists = briksInThisEdition.contains(name);
         NodeList nodes = MODS_XPATH_SELECTOR.selectNodeList(modsDocument, display);
         boolean hasDisplayLabel =  nodes != null && nodes.getLength() > 0 ;
         if (hasDisplayLabel) {
             displayLabelsInThisEdition.add(name);
         }
         if (!brikExists && hasDisplayLabel) {
             resultCollector.addFailure(
                     event.getName(),
                     "metadata",
                     getClass().getSimpleName(),
                     "2C-10: Did not find symbol " + name + " although it is implied by existence of " +
                             "displayname in corresponding page " + event.getName(),
                     display
             );
         }
 
     }
 
     /**
      * Validate that all briks were mentioned in at least one metadatafile.
      * @param event An edition end node event.
      */
     private void validate2C10AllBriksInEdition(NodeEndParsingEvent event) {
        Set<String> undescribedBriks = new HashSet<>(briksInThisEdition); undescribedBriks.removeAll(displayLabelsInThisEdition);
         for (String undescribedBrik : undescribedBriks) {
             resultCollector.addFailure(event.getName(),
                                        "metadata",
                                        getClass().getSimpleName(),
                                        "2C-10: The brik-file '" + undescribedBrik
                                                + "' was not mentioned by any MODS metadata displayLabel attribute.");
         }
     }
 
     /**
      * Transform the name of this mods file to the expected name of the brik object.
      * This means
      * i) remove the .mods.xml ending
      * ii) remove the multi-page suffix so
      * adresseavisen1759-1795-06-15-02-0005B.mods.xml  ->  adresseavisen1759-1795-06-15-02-0005
      * adresseavisen1759-1795-06-15-02-0004.mods.xml   ->  adresseavisen1759-1795-06-15-02-0004
      * @param modsFileName the name of this mods file.
      * @return the name of the matching brik (symbol) file.
      */
     private String getBrikName(String modsFileName) {
         modsFileName = modsFileName.replace(".mods.xml", "");
         char lastChar = modsFileName.charAt(modsFileName.length() -1);
         if (String.valueOf(lastChar).matches("[A-Z]")) {
             modsFileName = modsFileName.substring(0, modsFileName.length() -1);
         }
         return modsFileName;
     }
 
     /**
      * Validate consistency of newspaper title against the database.
      * @param event
      * @param modsDocument
      */
     private void validate2C11(AttributeParsingEvent event, Document modsDocument) {
         //2C-11
         final String xpath2C11 = "mods:mods/mods:relatedItem/mods:titleInfo[@type='uniform' and @authority='Statens Avissamling']/mods:title";
         String modsAvisId = MODS_XPATH_SELECTOR.selectString(modsDocument, xpath2C11);
         if (modsAvisId == null || context.getAvisId() == null || !modsAvisId.equals(context.getAvisId())) {
             resultCollector.addFailure(
                     event.getName(),
                     "metadata",
                     getClass().getSimpleName(),
                     "2C-11: avisId mismatch. Document gives " + modsAvisId + " but mfpak gives " + context.getAvisId(),
                     xpath2C11
             );
         }
     }
 
     /**
      * Validate consistency of sequence number in mods file against the name of the file.
      * @param event
      * @param modsDocument
      */
     private void validate2C5(AttributeParsingEvent event, Document modsDocument) {
         //2C-5
         if (event.getName().matches(".*-X[0-9]{4}\\.mods\\.xml$")) {
             //Missing pages do not have a sequence number
             return;
         }
         final String xpath2C5 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel sequence number']";
         String sequenceNumber = MODS_XPATH_SELECTOR.selectString(modsDocument, xpath2C5);
         String namePattern = ".*-[0]*" + sequenceNumber + ".mods.xml";
         if (sequenceNumber == null || !(event.getName().matches(namePattern))) {
             resultCollector.addFailure(event.getName(),
                                     "metadata",
                                     getClass().getSimpleName(),
                                     "2C-5: " + sequenceNumber + " not found in file name. Should match " + namePattern + ".",
                                     xpath2C5
                                     );
         }
     }
 
     /**
      * Validate that the reel number matches the expected pattern for reels in this batch.
      * @param event
      * @param modsDocument
      */
     private void validate2C4(AttributeParsingEvent event, Document modsDocument) {
         //2C-4
         final String xpath2C4 = "mods:mods/mods:relatedItem[@type='original']/mods:identifier[@type='reel number']";
         String reelNumber = MODS_XPATH_SELECTOR.selectString(modsDocument, xpath2C4);
         String reelNumberPatternString = "^" + context.getBatch().getBatchID() + "-" + "[0-9]+$";
         if (reelNumber == null || !reelNumber.matches(reelNumberPatternString)) {
               resultCollector.addFailure(event.getName(),
                                     "metadata",
                                     getClass().getSimpleName(),
                                     "2C-4: reel number " + reelNumber + " does not match expected pattern '" + reelNumberPatternString + "'",
                                     xpath2C4
                                     );
         }
     }
 
 
     /**
          * We use a constant "/" as file separator in DOMS, not the system-dependent file-separator, so this
          * method finds the last token in a path assuming that "/" is the file separator.
          * @param name
          * @return
          */
         private static String getLastTokenInPath(String name) {
             String [] nameSplit = name.split("/");
             return nameSplit[nameSplit.length -1];
         }
 
 }
