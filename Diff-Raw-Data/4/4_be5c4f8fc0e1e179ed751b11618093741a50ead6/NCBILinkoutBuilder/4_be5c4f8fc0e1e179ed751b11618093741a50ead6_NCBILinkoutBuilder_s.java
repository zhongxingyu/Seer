 /*
  * NCBI Linkout generator for Dryad
  *
  * Created on Feb 17, 2012
  * Last updated on July 20, 2012
  * 
  */
 package org.datadryad.interop;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import nu.xom.Builder;
 import nu.xom.Comment;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Node;
 import nu.xom.ParsingException;
 import nu.xom.Text;
 import nu.xom.ValidityException;
 
 import org.apache.log4j.Logger;
 
 public class NCBILinkoutBuilder {
 
     DBConnection dbc;
     
     static final String PACKAGECOLLECTIONNAME = "Dryad Data Packages";
     
     static final String DEFAULTPUBLINKFILE = "pubmedlinkout";
     static final String DEFAULTSEQLINKFILE = "sequencelinkout";
     
     final Set<DryadPackage> dryadPackages = new HashSet<DryadPackage>();
     //final Set<Publication> publications = new HashSet<Publication>();
     
     static final String NCBIEntrezPrefix = "";
     
     static final String NCBIDatabasePrefix = "http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?dbfrom=pubmed&db=";
 
     static final Map<String,String> doiToPMID = new HashMap<String,String>();
     
     static final Map<String,String> NCBIDatabaseNames = new HashMap<String,String>(); //name (suffix) -> Abbreviation
     static {
         NCBIDatabaseNames.put("gene","Gene");
         NCBIDatabaseNames.put("nucleotide","Nucleotide");
         NCBIDatabaseNames.put("est","NucEST");
         NCBIDatabaseNames.put("gss","NucGSS");
         NCBIDatabaseNames.put("protein","Protein");
         NCBIDatabaseNames.put("taxonomy","Taxonomy");
         NCBIDatabaseNames.put("bioproject","BioProject");
     }
     
         
     static final Logger logger = Logger.getLogger(NCBILinkoutBuilder.class);
     
     /**
      * @param args
      * @throws SQLException 
      */
     public static void main(String[] args) throws Exception {
         final NCBILinkoutBuilder builder = new NCBILinkoutBuilder();
        if (args.length > 2){
            builder.process(args[1],args[2]);
         }
         else
             builder.process(DEFAULTPUBLINKFILE,DEFAULTSEQLINKFILE);
     }
 
     
     /**
      * 1. Gather all dryadpackages from the database
      * 2. Locate the associated publication in NCBI
      *    a. if there is a doi, lookup the publication using the doi
      *    b. if no doi specified, lookup the publication using the citation matcher (to be implemented?) - Maybe not (10/12/2012)
      *    c. if both of these fail the publication is assumed not to be known to NCBI
      * 3. 
      * @param publinkFile
      * @param seqlinkFile
      * @throws Exception
      */
     private void process(String publinkFile, String seqlinkFile) throws Exception{
         dbc = getConnection();
         int doiCount = 0;
         int pmidCount = 0;
         DryadPackage.getPackages(dryadPackages,PACKAGECOLLECTIONNAME,dbc);
         for(DryadPackage dpackage : dryadPackages){
             if (!dpackage.hasPubDOI()){
                 doiCount++;
                 dpackage.directLookup();  //TODO implement this
             }
             else{
                 dpackage.lookupPMID(dbc);
                 if (dpackage.getPMIDs().size() != 0)
                     pmidCount++;
             }
         }
         logger.info("Found " + dryadPackages.size() + " packages");
         logger.info("Found " + doiCount + " packages with no DOI");
         logger.info("Found " + pmidCount + " publications with a DOI that resolved to no PMIDs");
         dbc.disconnect();
         generatePubLinkout(publinkFile);
         queryELinkRefs();
         generateSeqLinkout(seqlinkFile);
     }
 
     /**
      * @throws ValidityException
      * @throws ParsingException
      * @throws IOException
      */
     private void queryELinkRefs() throws ValidityException, ParsingException, IOException {
         int queryCount = 0;
         int hitCount = 0;
         for(DryadPackage pkg : dryadPackages){
             if (pkg.hasPMID()){
                 final String pmid = pkg.getPubPMID();
                 for (String dbName : NCBIDatabaseNames.keySet()){
                     final String query = NCBIDatabasePrefix + NCBIDatabaseNames.get(dbName) + "&id=" + pmid;
                     final Document d = queryNCBI(query);
                     if (d != null){
                         boolean processResult = processQueryReturn(d, query, pkg);
                         if (processResult && pkg.hasPMIDLinks(pmid) || pkg.hasSeqLinks()){
                             hitCount++;
                         }
                     }
                     queryCount++;
                     if (queryCount % 100 == 0){
                         logger.info("Processed " + queryCount + " queries, with " + hitCount + " returning linklist results");
                     }
                 }
             }
         }
     }
 
 
     /**
      * @throws ParserConfigurationException
      * @throws IOException 
      */
     private void generatePubLinkout(String targetFile) throws ParserConfigurationException, IOException {
         //captured everything in a dryad article, now generate the xml linkout file
         final LinkoutTarget target = new PubMedTarget();
         for (DryadPackage pkg : dryadPackages){
             target.addPackage(pkg);
         }
         target.save(targetFile);
     }
     
     /**
      * @throws ParserConfigurationException
      * @throws IOException 
      */
     private void generateSeqLinkout(String targetFile) throws ParserConfigurationException, IOException {
         //captured everything in a dryad article, now generate a series of xml linkout files
         final LinkoutTarget target = new OtherTarget();
         for (DryadPackage pkg : dryadPackages){
             target.addPackage(pkg);
         }
         target.save(targetFile);
     }
     
     
     private Document queryNCBI(String query) throws  ValidityException, ParsingException, IOException{
         final Builder builder = new Builder();
         return builder.build(query);
     }
 
     private boolean processQueryReturn(Document d, String query, DryadPackage pkg){
         final Element root = d.getRootElement();
         if (root.getChildCount()>0 && "eLinkResult".equals(root.getLocalName())){
             return processELinkResult(root,query,pkg);
         }
         else
             return false;
     }
     
     
     
     private boolean processELinkResult(Node eLinkElement,String query, DryadPackage pkg){
         boolean valid = true;
         for(int i = 0; i<eLinkElement.getChildCount() && valid; i++){
             final Node nChild = eLinkElement.getChild(i);
             if (nChild instanceof Element){
                 final Element child = (Element)nChild;
                 if ("LinkSet".equals(child.getLocalName()) && child.getChildCount()>0){
                     valid = processLinkSetElement(child,query,pkg);
                 }
             }  //otherwise ignore
         }
         return valid;
     }
     
     private boolean processLinkSetElement(Node linkSetElement, String query, DryadPackage pkg){
         boolean valid = true;
         for(int i = 0; i<linkSetElement.getChildCount() && valid; i++){
             final Node nChild = linkSetElement.getChild(i);
             if (nChild instanceof Element){
                 final Element child = (Element)nChild;
                 if ("DbFrom".equals(child.getLocalName())){
                     final String sourceDB = checkDbFrom(child);
                     if (!"pubmed".equals(sourceDB)){
                         logger.warn("Source DB is not pubmed..." + query);
                     }
                 valid = (sourceDB != null);
                 }
                 else if ("IdList".equals(child.getLocalName())){
                     valid = checkIdList(child);
                 }
                 else if ("LinkSetDb".equals(child.getLocalName())){
                     valid = processLinkSetDb(child,query,pkg);
                 }
                 else 
                     logger.error("LinkSet child name = " + child.getLocalName() + " Child count = " + child.getChildCount()); 
             }
         }
         return valid;
     }
     
     
     private String checkDbFrom(Node dbFromElement){
         if (dbFromElement.getChildCount()>=1){
             if (dbFromElement.getChild(0) instanceof Text){
                 final Text child = (Text)dbFromElement.getChild(0);
                 return child.getValue();
             }
             logger.error("Bad DbFrom element: " + dbFromElement);
             return null;
         }
         logger.error("Bad DbFrom element child count: " + dbFromElement.getChildCount());
         return null;
     }
     
     //TODO: This should either be implemented or removed
     private boolean checkIdList(Node idListElement){
         return true;
     }
     
     
     private boolean processLinkSetDb(Node linkSetDbElement, String query, DryadPackage pkg){
         boolean valid = true;
         String targetDB = null;
         for (int i = 0; i<linkSetDbElement.getChildCount() && valid; i++){
             final Node nChild = linkSetDbElement.getChild(i);
             if (nChild instanceof Element){
                 final Element child = (Element)nChild;
                 if ("DbTo".equals(child.getLocalName())){
                     targetDB = checkDbTo(child);
                     if (!"pubmed".equals(targetDB))
                         logger.info("targetDB: " + targetDB);
                     valid = (targetDB != null);
                 }
                 else if ("LinkName".equals(child.getLocalName())){
                     final String linkName = checkLinkName(child);
                     logger.info("Link Name: " + linkName);
                     valid = (linkName != null);
                 }
                 else if ("Link".equals(child.getLocalName())){
                     final String idStr = processLinkId(child);
                     if (targetDB != null)
                         pkg.addSequenceLink(targetDB, idStr);
                     else
                         logger.error("No targetDB specified in linkset: " + query);
                 }
                 else 
                     logger.warn("LinkSetDb child name = " + child.getLocalName() + " Child count = " + child.getChildCount());            
             }  //else ignore
         }
         return valid;
     }
 
     private String checkDbTo(Node dbToElement){
         if (dbToElement.getChildCount()>= 1){
             if (dbToElement.getChild(0) instanceof Text){
                 return ((Text)dbToElement.getChild(0)).getValue();
             }
             logger.error("Bad DbTo element: " + dbToElement);
             return null;
         }
         logger.error("Bad DbTo element child count: " + dbToElement.getChildCount());
         return null;
     }
     
     private String checkLinkName(Node linkNameElement){
         if (linkNameElement.getChildCount()>=1){
             if (linkNameElement.getChild(0) instanceof Text){
                 return ((Text)linkNameElement.getChild(0)).getValue();
             }
             logger.error("Bad LinkName element: " + linkNameElement);
             return null;
         }
         logger.error("Bad LinkName element child count: " + linkNameElement.getChildCount());
         return null;
     }
     
     private String processLinkId(Node linkElement){
         if (linkElement.getChildCount()>=2){
             if (linkElement.getChild(1) instanceof Element){
                 final Element child = (Element)linkElement.getChild(1);
                 if (child.getChild(0) instanceof Text){
                     return ((Text)child.getChild(0)).getValue();
                 }
                 logger.error("Bad Id element: " + linkElement);
                 return null;
             }
             logger.error("Bad Id element: " + linkElement);
             return null;
         }
         logger.error("Bad LinkName element child count: " + linkElement.getChildCount());
         return null;        
     }
     
     
     private DBConnection getConnection(){
         DBConnection result = new DBConnectionImpl();
         result.connect();
         return result;
     }
     
     
 }
