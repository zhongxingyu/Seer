 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.ChildOfAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CountAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.MaterialAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SexAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.fgpt.sampletab.utils.ENAUtils;
 import uk.ac.ebi.fgpt.sampletab.utils.XMLUtils;
 
 public class DGVaXMLToSampleTab {
 
     // logging
     private Logger log = LoggerFactory.getLogger(getClass());
     
     public SampleData convert(String filename) throws IOException, ParseException {
         return convert(new File(filename));
     }
 
     public SampleData convert(File infile) throws ParseException, IOException {
         
         infile = infile.getAbsoluteFile();
         
         if (!infile.exists()) {
             throw new IOException(infile + " does not exist");
         }
 
         Document studydoc;
         try {
             studydoc = XMLUtils.getDocument(infile);
         } catch (DocumentException e) {
             // rethrow as a ParseException
             throw new ParseException("Unable to parse XML document of study");
         }
         Element root = studydoc.getRootElement();
         Element submission = root;
         Element study = XMLUtils.getChildByName(submission, "STUDY");
         
         SampleData st = new SampleData();
         
         st.msi.submissionTitle = study.attributeValue("display_name");
         st.msi.submissionDescription = XMLUtils.getChildByName(study, "DESCRIPTION").getTextTrim();
         st.msi.submissionIdentifier = "GVA-"+study.attributeValue("study_accession");
         st.msi.submissionReferenceLayer = false;
         
         for (Element publication : XMLUtils.getChildrenByName(study, "PUBLICATION")){ 
             st.msi.publications.add(new Publication(publication.attributeValue("NCBI_pmid"), null));
         }
         
 
         st.msi.persons.add(new Person(submission.attributeValue("last_name"), 
                 null, submission.attributeValue("first_name"), 
                 submission.attributeValue("email"), "submitter"));
         st.msi.organizations.add(new Organization(submission.attributeValue("affiliation"), 
                 null, null, null, "submitter"));
         st.msi.databases.add(new Database("DGVa", 
                 null, study.attributeValue("study_accession")));
         
         
         //now actually add the samples
         //start with the "subjects", then add their "samples", then put them in "groups"
         for (Element dgvasample : XMLUtils.getChildrenByName(submission, "SUBJECT")){
             SampleNode sampleNode = new SampleNode();
            sampleNode.setNodeName("Subject "+dgvasample.attributeValue("subject_id"));
             log.debug("Adding Subject "+dgvasample.attributeValue("subject_id"));
             
             if (dgvasample.attributeValue("sample_type") != null) {
                 sampleNode.addAttribute(new MaterialAttribute(dgvasample.attributeValue("sample_type")));
             }
             if (dgvasample.attributeValue("sex") != null) {
                 sampleNode.addAttribute(new SexAttribute(dgvasample.attributeValue("sex")));
             }
             if (dgvasample.attributeValue("ethnicity") != null) { 
                 sampleNode.addAttribute(new CharacteristicAttribute("Ethnicity", dgvasample.attributeValue("ethnicity")));
             }
             if (dgvasample.attributeValue("NCBI_tax_id") != null) {
                 sampleNode.addAttribute(new OrganismAttribute(dgvasample.attributeValue("NCBI_tax_id")));
                 //TODO go from tax id to organism name
             }
             if (XMLUtils.getChildByName(dgvasample, "SOURCE") != null){
                 sampleNode.addAttribute(new CommentAttribute("Source", XMLUtils.getChildByName(dgvasample, "SOURCE").getTextTrim()));
             }
             //add any other mappings here
             //now the sample node can be added to scd
             st.scd.addNode(sampleNode);
         }
 
         //now add samples, deriving from subjects as appropriate
         for (Element dgvasample : XMLUtils.getChildrenByName(submission, "SAMPLE")){
             SampleNode sampleNode = new SampleNode();
             sampleNode.setNodeName(dgvasample.attributeValue("sample_id"));
             log.debug("Adding "+dgvasample.attributeValue("sample_id"));
             
             //derive this sample from its parent
             String parentName = "subject "+dgvasample.attributeValue("subject_id");
             SampleNode parentNode = st.scd.getNode(parentName, SampleNode.class);
            assert parentNode != null: parentName;
             //make sure to do both of these!
             sampleNode.addParentNode(parentNode);
             parentNode.addChildNode(sampleNode);
             
             if (dgvasample.attributeValue("sample_type") != null) {
                 sampleNode.addAttribute(new MaterialAttribute(dgvasample.attributeValue("sample_type")));
             }
             if (dgvasample.attributeValue("sex") != null) {
                 sampleNode.addAttribute(new SexAttribute(dgvasample.attributeValue("sex")));
             }
             if (dgvasample.attributeValue("ethnicity") != null) { 
                 sampleNode.addAttribute(new CharacteristicAttribute("Ethnicity", dgvasample.attributeValue("ethnicity")));
             }
             if (dgvasample.attributeValue("strain") != null) { 
                 sampleNode.addAttribute(new CharacteristicAttribute("StrainOrLine", dgvasample.attributeValue("strain")));
             }
             if (dgvasample.attributeValue("paternal_subject_id") != null) { 
                 sampleNode.addAttribute(new ChildOfAttribute(dgvasample.attributeValue("paternal_subject_id")));
                 //TODO imply that paternal subject has sex male
             }
             if (dgvasample.attributeValue("maternal_subject_id") != null) { 
                 sampleNode.addAttribute(new ChildOfAttribute(dgvasample.attributeValue("maternal_subject_id")));
                 //TODO imply that maternal subject has sex female
             }
             if (dgvasample.attributeValue("NCBI_tax_id") != null) {
                 sampleNode.addAttribute(new OrganismAttribute(dgvasample.attributeValue("NCBI_tax_id")));
                 //TODO go from tax id to organism name
             }
             if (XMLUtils.getChildByName(dgvasample, "SOURCE") != null){
                 sampleNode.addAttribute(new CommentAttribute("Source", XMLUtils.getChildByName(dgvasample, "SOURCE").getTextTrim()));
             }
             //add any other mappings here
             //now the sample node can be added to scd
             st.scd.addNode(sampleNode);
         }
         
         int groupCounter = 1;
         for (Element sampleset : XMLUtils.getChildrenByName(submission, "SAMPLESET")){
 
             GroupNode group = new GroupNode();
             String groupName = null;
             groupName = sampleset.elementTextTrim("sampleset_name");
             if (groupName == null){
                 groupName = "G"+groupCounter;
                 groupCounter += 1;
             }
             group.setNodeName(groupName);
             
             if (XMLUtils.getChildByName(sampleset, "DESCRIPTION") != null){
                 group.setGroupDescription(XMLUtils.getChildByName(sampleset, "DESCRIPTION").getTextTrim());
             }
             
             for (Element dgvasample : XMLUtils.getChildrenByName(sampleset, "SAMPLE")){
                 SampleNode sampleNode = st.scd.getNode(dgvasample.attributeValue("sample_id"), SampleNode.class);
                 assert sampleNode != null: dgvasample.attributeValue("sample_id");
                 group.addSample(sampleNode);  
             }
             
             if (group.getParentNodes().size() != new Integer(sampleset.attribute("size").getText())){
                 //misc samples are in this group
                 group.addAttribute(new CountAttribute(new Integer(sampleset.attribute("size").getText())));
             }
             
             if (sampleset.attributeValue("sex") != null) {
                 group.addAttribute(new SexAttribute(sampleset.attributeValue("sex")));
             }
             if (sampleset.attributeValue("ethnicity") != null) { 
                 group.addAttribute(new CharacteristicAttribute("Ethnicity", sampleset.attributeValue("ethnicity")));
             }
             if (sampleset.attributeValue("NCBI_tax_id") != null) {
                 group.addAttribute(new OrganismAttribute(sampleset.attributeValue("NCBI_tax_id")));
                 //TODO go from tax id to organism name
             }
             if (XMLUtils.getChildByName(sampleset, "SOURCE") != null){
                 group.addAttribute(new CommentAttribute("Source", XMLUtils.getChildByName(sampleset, "SOURCE").getTextTrim()));
             }
 
             st.scd.addNode(group);
             
         }
         
         log.info("Finished convert()");
         return st;
     }
 
     public void convert(File file, Writer writer) throws IOException, ParseException {
         log.debug("recieved xml, preparing to convert");
         SampleData st = convert(file);
 
         log.info("SampleTab converted, preparing to write");
         SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
         sampletabwriter.write(st);
         log.info("SampleTab written");
         sampletabwriter.close();
 
     }
 
     public void convert(File studyFile, String outfilename) throws IOException, ParseException {
 
         convert(studyFile, new File(outfilename));
     }
 
     public void convert(File studyFile, File sampletabFile) throws IOException, ParseException {
 
         // create parent directories, if they dont exist
         sampletabFile = sampletabFile.getAbsoluteFile();
         if (sampletabFile.isDirectory()) {
             sampletabFile = new File(sampletabFile, "sampletab.pre.txt");
         }
         if (!sampletabFile.getParentFile().exists()) {
             sampletabFile.getParentFile().mkdirs();
         }
 
         convert(studyFile, new FileWriter(sampletabFile));
     }
 
     public void convert(String studyFilename, Writer writer) throws IOException, ParseException {
         convert(new File(studyFilename), writer);
     }
 
     public void convert(String studyFilename, File sampletabFile) throws IOException, ParseException {
         convert(studyFilename, new FileWriter(sampletabFile));
     }
 
     public void convert(String studyFilename, String sampletabFilename) throws IOException, ParseException {
         convert(studyFilename, new File(sampletabFilename));
     }
 
     public static void main(String[] args) {
         new DGVaXMLToSampleTab().doMain(args);
     }
 
     public void doMain(String[] args) {
         if (args.length < 2) {
             System.out.println("Must provide an DGVa XML filename and a SampleTab output filename.");
             return;
         }
         
         String dgvafilename = args[0];
         String sampleTabFilename = args[1];
 
         DGVaXMLToSampleTab converter = new DGVaXMLToSampleTab();
 
         try {
             converter.convert(dgvafilename, sampleTabFilename);
         } catch (ParseException e) {
             System.out.println("Error converting " + dgvafilename + " to " + sampleTabFilename);
             e.printStackTrace();
         } catch (IOException e) {
             System.out.println("Error converting " + dgvafilename + " to " + sampleTabFilename);
             e.printStackTrace();
         }
     }
 }
