 package uk.ac.ebi.fgpt.sampletab.pride;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SCDNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.PRIDEutils;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class PRIDEXMLToSampleTab {
 
     @Option(name = "-h", aliases={"--help"}, usage = "display help")
     private boolean help;
 
     //TODO make required
     //TODO make multiple
     @Option(name = "-i", aliases={"--input"}, usage = "input filename")
     private String inputFilename;
 
     //TODO make required
     @Option(name = "-o", aliases={"--output"}, usage = "output filename")
     private String outputFilename;
 
     @Option(name = "-p", aliases={"--projects"}, usage = "projects filename")
     private String projectsFilename;
 
 
     private Map<String, Set<String>> projects;
     
     
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
         
     public PRIDEXMLToSampleTab() {
         
     }
     
     public PRIDEXMLToSampleTab(String projectsFilename) {
         this.projectsFilename = projectsFilename;
         //read all the projects
         try {
             this.projects = PRIDEutils.loadProjects(new File(this.projectsFilename));
         } catch (IOException e) {
             log.error("Unable to read projects file "+projectsFilename);
             e.printStackTrace();
             this.projects = null;
         }
             
     }
     
 
     public SampleData convert(Set<File> infiles) throws DocumentException, FileNotFoundException {
         
         SampleData st = new SampleData();
         st.msi.submissionReferenceLayer = false;
         //PRIDE does not track dates :(
         st.msi.submissionReleaseDate = new Date();
         st.msi.submissionUpdateDate = new Date();
         
         for (File infile : infiles){
             Element expcollection = XMLUtils.getDocument(infile).getRootElement();
             Element exp = XMLUtils.getChildByName(expcollection, "Experiment");
             Element additional = XMLUtils.getChildByName(exp, "additional");
             Element mzd = XMLUtils.getChildByName(exp, "mzData");
             Element description = XMLUtils.getChildByName(mzd, "description");
             Element admin = XMLUtils.getChildByName(description, "admin");
             Element sampledescription = XMLUtils.getChildByName(admin, "sampleDescription");
             String accession = XMLUtils.getChildByName(exp, "ExperimentAccession").getTextTrim();
             
             if (st.msi.submissionTitle == null || st.msi.submissionTitle.length() == 0)
                 st.msi.submissionTitle = XMLUtils.getChildByName(exp, "Title").getTextTrim();
             //PRIDE dont have submission description
             //actually maybe it does as a CVparam...
             
             
             for (Element contact : XMLUtils.getChildrenByName(admin, "contact")){
                 String name = XMLUtils.getChildByName(contact, "name").getTextTrim();
                 String institution = XMLUtils.getChildByName(contact, "institution").getTextTrim();
                 
                 Organization org = new Organization(institution, null, null, null, null);
                 if (!st.msi.organizations.contains(org)){
                     st.msi.organizations.add(org);
                 }
                 
                 String[] splitnames = PRIDEutils.splitName(name);
                 Person per = new Person(splitnames[2], splitnames[1], splitnames[0], null, "Submitter");
                 if (!st.msi.persons.contains(per)){
                     st.msi.persons.add(per);
                 }
             }
             
             for (Element reference : XMLUtils.getChildrenByName(exp, "Reference")){
                 for (Element referenceadditional : XMLUtils.getChildrenByName(reference, "additional")){
                     for (Element referenceaddpar : XMLUtils.getChildrenByName(referenceadditional, "cvParam")){
                         if (referenceaddpar.attributeValue("cvLabel").trim().equals("PubMed")){
                             //some PubMed IDs have full URLs, strip them
                             String pubmedid = referenceaddpar.attributeValue("accession").trim();
                             pubmedid.replace("http://www.ncbi.nlm.nih.gov/pubmed/", "");
                             //TODO check to avoid duplicates
                             st.msi.publications.add(new Publication(pubmedid, null));
                         }
                     }
                 }
             }
             
             SampleNode sample = new SampleNode();
            sample.setNodeName(accession);
             
             DatabaseAttribute dbattr = new DatabaseAttribute("PRIDE", accession, "http://www.ebi.ac.uk/pride/showExperiment.do?experimentAccessionNumber="+accession);
             sample.addAttribute(dbattr);
 
             for (Element cvparam : XMLUtils.getChildrenByName(sampledescription, "cvParam")){
                 String name = cvparam.attributeValue("name").trim();
                 String value = cvparam.attributeValue("value");
                 String cvLabel = null;
                 if (cvparam.attributeValue("cvLabel") != null){
                     cvLabel = cvparam.attributeValue("cvLabel").trim();
                 }
                 String cvAccession = null;
                 if (cvparam.attributeValue("accession") != null){
                     cvAccession = cvparam.attributeValue("accession").trim();
                 }
                 
                 
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                 } else {
                     value = value.trim();
                 }
                 //TODO  use special attribute classes where appropriate
                 if ("NEWT".equals(cvLabel)) {
                     String termSourceREF = "NCBI Taxonomy";
                     //TODO make sure that this term source is then added to msi section
                     Integer termSourceID = new Integer(cvAccession);
                     OrganismAttribute attr = new OrganismAttribute(name, termSourceREF, termSourceID);
                     sample.addAttribute(attr);
                 } else {
                     CharacteristicAttribute attr = new CharacteristicAttribute(name, value);
                     if (cvLabel != null && cvAccession != null){
                         attr.setTermSourceREF(cvLabel);
                         //TODO make sure that this term source is then added to msi section
                         attr.setTermSourceID(cvAccession);
                     }
                     sample.addAttribute(attr);
                 }
             }
             
             for (Element cvparam : XMLUtils.getChildrenByName(additional, "cvParam")){
                 String name = cvparam.attributeValue("name").trim();
                 String value = cvparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                 } else {
                     value = value.trim();
                 }
                 CharacteristicAttribute attr = new CharacteristicAttribute(name, value);
                 if (cvparam.attributeValue("cvLabel") != null && cvparam.attributeValue("accession") != null){
                     attr.setTermSourceREF(cvparam.attributeValue("cvLabel").trim());
                     //TODO make sure that this term source is then added to msi section
                     attr.setTermSourceID(cvparam.attributeValue("accession").trim());
                 }
                 sample.addAttribute(attr);
             }
             
             for (Element userparam : XMLUtils.getChildrenByName(sampledescription, "userParam")){
                 String name = userparam.attributeValue("name").trim();
                 String value = userparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                 } else {
                     value = value.trim();
                 }
                 CommentAttribute attr = new CommentAttribute(name, value);
                 sample.addAttribute(attr);
             }
             
             for (Element userparam : XMLUtils.getChildrenByName(additional, "userParam")){
                 String name = userparam.attributeValue("name").trim();
                 String value = userparam.attributeValue("value");
                 if (value == null){
                     //some PRIDE attributes are boolean
                     //set their value to be their name
                     value = name;
                 } else {
                     value = value.trim();
                 }
                 CommentAttribute attr = new CommentAttribute(name, value);
                 sample.addAttribute(attr);
             }
             
             try {
                 st.scd.addNode(sample);
             } catch (ParseException e) {
                 log.error("Unable to add node "+sample);
                 e.printStackTrace();
                 continue;
             }
             
         }
         //these can only be calculated after all other steps
         
         //submission id is the minimum sample id
         List<String> submitids = new ArrayList<String>();
         for (SCDNode in : st.scd.getAllNodes()){
             submitids.add(in.getNodeName());
         }
         Collections.sort(submitids);
        st.msi.submissionIdentifier = "GPR-"+submitids.get(0) ;
         
         log.info("Finished convert()");
         return st;
     }
 
     public void convert(Set<File> infiles, Writer writer) throws IOException, DocumentException  {
         log.debug("recieved infiles, preparing to convert");
         SampleData st = convert(infiles);
 
         log.info("SampleTab converted, preparing to write");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         sampletabwriter.write(st);
         log.info("SampleTab written");
         sampletabwriter.close();
 
     }
 
     public void convert(Set<File> infiles, File sampletabFile) throws IOException, DocumentException {
 
         // create parent directories, if they dont exist
         sampletabFile = sampletabFile.getAbsoluteFile();
         if (sampletabFile.isDirectory()) {
             sampletabFile = new File(sampletabFile, "sampletab.pre.txt");
         }
         if (!sampletabFile.getParentFile().exists()) {
             sampletabFile.getParentFile().mkdirs();
         }
         FileWriter writer = null;
         try {
             writer = new FileWriter(sampletabFile); 
             convert(infiles, writer);
         } finally {
             try {
                 if (writer != null ){
                     writer.close();
                 }
             } catch (IOException e) {
                 //do nothing
             }
         }
     }
 
     public void convert(Set<File> infiles, String outfilename) throws IOException, DocumentException  {
 
         convert(infiles, new File(outfilename));
     }
     
     
     public void convert(String inputFilename, String outputFilename) throws IOException, DocumentException {
 
         //read the given input filename to determine accession
         String accession;
         File inputFile = new File(inputFilename);
         try {
             accession = "GPR-"+PRIDEutils.extractAccession(inputFile);
         } catch (FileNotFoundException e1) {
             log.warn("Unable to find accession of "+inputFilename);
             return;
         } catch (DocumentException e1) {
             log.warn("Unable to find accession of "+inputFilename);
             return;
         }
         
         log.debug("accession = "+accession);
 
         //find the project
         String projectname = null;
         for (String name: projects.keySet()){
             //TODO handle where one accession is in multiple projects...
             if (projects.get(name).contains(accession)){
                 if (projectname == null){
                     projectname = name;
                 } else {
                     log.warn("Multiple project names for "+accession+" : "+name);
                 }
             }
         }
         
         if (projectname == null){
             log.warn("Unable to find project for "+accession);
             projectname = accession;
         }
         log.debug("projectname = "+projectname);
 
         HashSet<File> prideFiles = new HashSet<File>();
         
         if (projectsFilename != null){
             //if a project filename was given, then we find the project that the provided input filename is part of
             if (projects == null){
                 log.warn("No files of project "+projectname+" to process");
                 return;
             }
             
             //now add all the files that are similar to the input filename but with the other accessions
             Set<String> accessions = projects.get(projectname);
             for (String subaccession : accessions){
                 prideFiles.add(new File(inputFilename.replace(accession, subaccession)));
             }
             
         } else {
             prideFiles.add(new File(inputFilename));
         }
         
         if (!accession.equals(Collections.min(projects.get(projectname)))){
             log.error("Accession is not minimum in project, aborting");
             return;
         }
         
         this.convert(prideFiles, outputFilename);
         
     }
 
     public static void main(String[] args) {
         new PRIDEXMLToSampleTab().doMain(args);
     }
 
     public void doMain(String[] args) {
         CmdLineParser parser = new CmdLineParser(this);
         try {
             // parse the arguments.
             parser.parseArgument(args);
             // TODO check for extra arguments?
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             help = true;
         }
 
         if (help) {
             // print the list of available options
             parser.printSingleLineUsage(System.err);
             System.err.println();
             parser.printUsage(System.err);
             System.err.println();
             System.exit(1);
             return;
         }
         
         //TODO convert to using globs
         log.info("Converting "+inputFilename+" to "+outputFilename);
         try {
             convert(inputFilename, outputFilename);
         } catch (IOException e) {
             System.out.println("Error converting " + inputFilename + " to " + outputFilename);
             e.printStackTrace();
             System.exit(2);
             return;
         } catch (DocumentException e) {
             System.out.println("Error converting " + inputFilename + " to " + outputFilename);
             e.printStackTrace();
             System.exit(3);
             return;
         }
     }
 }
