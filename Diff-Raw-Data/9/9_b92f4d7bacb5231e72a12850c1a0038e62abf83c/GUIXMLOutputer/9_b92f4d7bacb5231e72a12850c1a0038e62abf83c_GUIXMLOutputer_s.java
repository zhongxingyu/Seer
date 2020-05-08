 package uk.ac.ebi.fgpt.sampletab.guixml;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.SaxonApiException;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.Serializer.Property;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.graph.Node;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.GroupNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.AbstractNodeAttributeOntology;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.DatabaseAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.SCDNodeAttribute;
 import uk.ac.ebi.fgpt.sampletab.utils.FileUtils;
 
 public class GUIXMLOutputer {
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
     private final File outputFile;
     private final File outputFileTemp;
     private XMLStreamWriter xmlWriter;
     private boolean started = false;
     
     public GUIXMLOutputer(File outputFile){
         this.outputFile = outputFile;
         this.outputFileTemp = new File(outputFile.getAbsolutePath()+".tmp");
     }
     
     public synchronized void start() throws SaxonApiException, XMLStreamException {
         if (started){
             throw new RuntimeException("Cannot restart a GUIXMLOutputer instance");
         }
         started = true;
         
         Processor processor = new Processor(false);
         Serializer serializer = processor.newSerializer(outputFileTemp);
         serializer.setOutputProperty(Property.INDENT, "yes"); 
         xmlWriter = serializer.getXMLStreamWriter();
         xmlWriter.writeStartDocument();
         xmlWriter.writeStartElement("Biosamples");
     }
     
     public synchronized void process(SampleData sd) throws XMLStreamException{
 
         
         //if release date is in the future, dont output
         if (sd.msi.submissionReleaseDate == null){
             log.info("No release date, skipping");
             return;
         } else if (sd.msi.submissionReleaseDate.after(new Date())){
             log.info("Future release, skipping");
             return;
         }
         
         //NB. this assumes all samples are in a group. This is a condition of the .toload.txt files
         //but not of sampletab.txt files.
         
         for (GroupNode g : sd.scd.getNodes(GroupNode.class)){
             
             if (g.getGroupAccession() == null){
                 log.warn("Group has null accession "+g.getNodeName()+" ("+sd.msi.submissionIdentifier+")");
                 continue;
             }
             
             log.debug("Group "+g.getNodeName());
             
             xmlWriter.writeStartElement("SampleGroup");
             xmlWriter.writeAttribute("id", g.getGroupAccession());
 
             writeAttributeValue(xmlWriter, "Submission Title", "true", "STRING", sd.msi.submissionTitle);
             writeAttributeValue(xmlWriter, "Submission Identifier", "true", "STRING", sd.msi.submissionIdentifier);
             writeAttributeValue(xmlWriter, "Submission Description", "true", "STRING", sd.msi.submissionDescription);
            writeAttributeValue(xmlWriter, "Submission Version", "true", "STRING", sd.msi.submissionVersion);
             writeAttributeValue(xmlWriter, "Submission Reference Layer", "true", "BOOLEAN", sd.msi.submissionReferenceLayer.toString());
             writeAttributeValue(xmlWriter, "Submission Release Date", "true", "STRING", sd.msi.getSubmissionReleaseDateAsString());
             writeAttributeValue(xmlWriter, "Submission Update Date", "true", "STRING", sd.msi.getSubmissionUpdateDateAsString());
            writeAttributeValue(xmlWriter, "Submission Modification Date", "true", "STRING", sd.msi.getSubmissionUpdateDateAsString());
             writeAttributeValue(xmlWriter, "Name", "true", "BOOLEAN", g.getNodeName());
             writeAttributeValue(xmlWriter, "Group Accession", "true", "BOOLEAN", g.getGroupAccession());
             //group description?
             
             writeOrganizations(xmlWriter, sd);
             writePersons(xmlWriter, sd);
             writePublications(xmlWriter, sd);
             writeTermSources(xmlWriter, sd);
             writeDatabases(xmlWriter, sd);
             
             //write out the samples
             writeSamples(xmlWriter, g, sd);
             
             xmlWriter.writeEndElement(); //SampleGroup
         }
     }
     
     public void end() throws XMLStreamException, IOException {
         if (! started){
             throw new RuntimeException("Cannot end a GUIXMLOutputer instance that has not started");
         }
         xmlWriter.writeEndDocument();
         xmlWriter.close();
         //move temp file over target
         FileUtils.move(outputFileTemp, outputFile);
     }
     
     public static String stripNonValidXMLCharacters(String in) {
         //from http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
 
         if (in == null){ 
             return null;
         }
         
         StringBuffer out = new StringBuffer(); // Used to hold the output.
         char current; // Used to reference the current character.
         
         for (int i = 0; i < in.length(); i++) {
             current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
             if ((current == 0x9) ||
                 (current == 0xA) ||
                 (current == 0xD) ||
                 ((current >= 0x20) && (current <= 0xD7FF)) ||
                 ((current >= 0xE000) && (current <= 0xFFFD)) ||
                 ((current >= 0x10000) && (current <= 0x10FFFF))){
                 out.append(current);
             }
         }
         return out.toString();
     } 
 
     private void writeAttribute(XMLStreamWriter xmlWriter, String cls, String classDefined, String dataType) throws XMLStreamException{
         List<String> attrs = new ArrayList<String>();
         writeAttributeValue(xmlWriter, cls, classDefined, dataType, attrs);
     }
     
     private void writeAttributeValue(XMLStreamWriter xmlWriter, String cls, String classDefined, String dataType, String value) throws XMLStreamException{
         if (value == null) return;
         if (value.length() == 0) return;
         List<String> values = new ArrayList<String>();
         values.add(value);
         writeAttributeValue(xmlWriter, cls, classDefined, dataType, values);
     }
     
     private void writeAttributeValue(XMLStreamWriter xmlWriter, String cls, String classDefined, String dataType, List<String> values) throws XMLStreamException{
        if (values.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", cls);
         xmlWriter.writeAttribute("classDefined", classDefined);
         xmlWriter.writeAttribute("dataType", dataType);
         for (String value : values){
             xmlWriter.writeStartElement("value");
             value = stripNonValidXMLCharacters(value);
             xmlWriter.writeCharacters(value);
             xmlWriter.writeEndElement(); //value
         }
         xmlWriter.writeEndElement(); //attribute
     }
     
     private void writeAttribute(XMLStreamWriter xmlWriter, String cls, String classDefined, String dataType, List<AbstractNodeAttributeOntology> attrs, SampleData st) throws XMLStreamException{
         if (attrs.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", cls);
         xmlWriter.writeAttribute("classDefined", classDefined);
         xmlWriter.writeAttribute("dataType", dataType);
         for (AbstractNodeAttributeOntology attr : attrs){
             xmlWriter.writeStartElement("value");
             String value = attr.getAttributeValue();
             value = stripNonValidXMLCharacters(value);
             //handle term source, if present
             TermSource ts = st.msi.getTermSource(attr.getTermSourceREF());
             if (ts != null ){
                 xmlWriter.writeStartElement("attribute");
                 xmlWriter.writeAttribute("class", "Term Source REF");
                 xmlWriter.writeAttribute("classDefined", "true");
                 xmlWriter.writeAttribute("dataType", "OBJECT");
                 xmlWriter.writeStartElement("value");
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", attr.getTermSourceREF());
                 xmlWriter.writeAttribute("class", "Term Source");
                 xmlWriter.writeAttribute("classDefined", "true");
                 writeAttributeValue(xmlWriter, "Term Source Name", "true", "STRING", ts.getName());
                 writeAttributeValue(xmlWriter, "Term Source URI", "true", "STRING", ts.getURI());
                 xmlWriter.writeEndElement(); //object
                 xmlWriter.writeEndElement(); //value of TermSourceREF
                 xmlWriter.writeEndElement(); //attribute of TermSourceREF
                 xmlWriter.writeStartElement("attribute");
                 xmlWriter.writeAttribute("class", "Term Source ID");
                 xmlWriter.writeAttribute("classDefined", "true");
                 xmlWriter.writeAttribute("dataType", "OBJECT");
                 xmlWriter.writeStartElement("value");
                 xmlWriter.writeCharacters(attr.getTermSourceID());
                 xmlWriter.writeEndElement(); //value of TermSourceID
                 xmlWriter.writeEndElement(); //attribute of TermSourceID
             }
             
             xmlWriter.writeCharacters(value);
             xmlWriter.writeEndElement(); //value
         }
         xmlWriter.writeEndElement(); //attribute
     }
     
     private void writeOrganizations(XMLStreamWriter xmlWriter, SampleData sd) throws XMLStreamException {
         if (sd.msi.organizations.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", "Organizations");
         xmlWriter.writeAttribute("classDefined", "true");
         xmlWriter.writeAttribute("dataType", "OBJECT");
         xmlWriter.writeStartElement("value");                     
         for (Integer i = 0; i < sd.msi.organizations.size(); i++){
             Organization o = sd.msi.organizations.get(i);
             if (o != null){
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", "Org"+i.toString());
                 xmlWriter.writeAttribute("class", "Organization");
                 xmlWriter.writeAttribute("classDefined", "true");
 
                 writeAttributeValue(xmlWriter, "Organization Name", "true", "STRING", o.getName());
                 writeAttributeValue(xmlWriter, "Organization Address", "true", "STRING", o.getAddress());
                 writeAttributeValue(xmlWriter, "Organization URI", "true", "STRING", o.getURI());
                 writeAttributeValue(xmlWriter, "Organization Email", "true", "STRING", o.getEmail());
                 writeAttributeValue(xmlWriter, "Organization Role", "true", "STRING", o.getRole());
                 
                 xmlWriter.writeEndElement(); //object
             }
         }
         xmlWriter.writeEndElement(); //value
         xmlWriter.writeEndElement(); //organizations
     }
     
     private void writePersons(XMLStreamWriter xmlWriter, SampleData sd) throws XMLStreamException {
         if (sd.msi.persons.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", "Persons");
         xmlWriter.writeAttribute("classDefined", "true");
         xmlWriter.writeAttribute("dataType", "OBJECT");
         xmlWriter.writeStartElement("value");                     
         for (Integer i = 0; i < sd.msi.persons.size(); i++){
             Person p = sd.msi.persons.get(i);
             if (p != null){
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", "Per"+i.toString());
                 xmlWriter.writeAttribute("class", "Person");
                 xmlWriter.writeAttribute("classDefined", "true");
 
                 writeAttributeValue(xmlWriter, "Person Last Name", "true", "STRING", p.getLastName());
                 writeAttributeValue(xmlWriter, "Person Initials", "true", "STRING", p.getInitials());
                 writeAttributeValue(xmlWriter, "Person First Name", "true", "STRING", p.getFirstName());
                 writeAttributeValue(xmlWriter, "Person Email", "true", "STRING", p.getEmail());
                 writeAttributeValue(xmlWriter, "Person Role", "true", "STRING", p.getRole());
                 
                 xmlWriter.writeEndElement(); //object
             }
         }
         xmlWriter.writeEndElement(); //value
         xmlWriter.writeEndElement(); //persons
     }
     
     private void writePublications(XMLStreamWriter xmlWriter, SampleData sd) throws XMLStreamException {
         if (sd.msi.publications.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", "Publications");
         xmlWriter.writeAttribute("classDefined", "true");
         xmlWriter.writeAttribute("dataType", "OBJECT");
         xmlWriter.writeStartElement("value");                     
         for (Integer i = 0; i < sd.msi.publications.size(); i++){
             Publication p = sd.msi.publications.get(i);
             if (p != null){
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", "Pub"+i.toString());
                 xmlWriter.writeAttribute("class", "Publication");
                 xmlWriter.writeAttribute("classDefined", "true");
 
                 writeAttributeValue(xmlWriter, "Publication PubMed ID", "true", "STRING", p.getPubMedID());
                 writeAttributeValue(xmlWriter, "Publication DOI", "true", "STRING", p.getDOI());
                 
                 xmlWriter.writeEndElement(); //object
             }
         }
         xmlWriter.writeEndElement(); //value
         xmlWriter.writeEndElement(); //publications
     
     }
     
     private void writeTermSources(XMLStreamWriter xmlWriter, SampleData sd) throws XMLStreamException {
         if (sd.msi.termSources.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", "Term Sources");
         xmlWriter.writeAttribute("classDefined", "true");
         xmlWriter.writeAttribute("dataType", "OBJECT");
         xmlWriter.writeStartElement("value");                     
         for (Integer i = 0; i < sd.msi.termSources.size(); i++){
             TermSource t = sd.msi.termSources.get(i);
             if (t != null && t.getName() != null){
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", t.getName());
                 xmlWriter.writeAttribute("class", "Term Source");
                 xmlWriter.writeAttribute("classDefined", "true");
 
                 writeAttributeValue(xmlWriter, "Term Source Name", "true", "STRING", t.getName());
                 writeAttributeValue(xmlWriter, "Term Source URI", "true", "STRING", t.getURI());
                 writeAttributeValue(xmlWriter, "Term Source Version", "true", "STRING", t.getVersion());
                 
                 xmlWriter.writeEndElement(); //object
             }
         }
         xmlWriter.writeEndElement(); //value
         xmlWriter.writeEndElement(); //term sources
     }
     
     private void writeDatabases(XMLStreamWriter xmlWriter, SampleData sd) throws XMLStreamException {
         if (sd.msi.databases.size() == 0) return;
         xmlWriter.writeStartElement("attribute");
         xmlWriter.writeAttribute("class", "Databases");
         xmlWriter.writeAttribute("classDefined", "true");
         xmlWriter.writeAttribute("dataType", "OBJECT");
         xmlWriter.writeStartElement("value");                     
         for (Integer i = 0; i < sd.msi.databases.size(); i++){
             Database d = sd.msi.databases.get(i);
             if (d != null){
                 xmlWriter.writeStartElement("object");
                 xmlWriter.writeAttribute("id", "Dat"+i.toString());
                 xmlWriter.writeAttribute("class", "Database");
                 xmlWriter.writeAttribute("classDefined", "true");
 
                 writeAttributeValue(xmlWriter, "Database Name", "true", "STRING", d.getName());
                 writeAttributeValue(xmlWriter, "Database ID", "true", "STRING", d.getID());
                 writeAttributeValue(xmlWriter, "Database URI", "true", "STRING", d.getURI());
                 
                 xmlWriter.writeEndElement(); //object
             }
         }
         xmlWriter.writeEndElement(); //value
         xmlWriter.writeEndElement(); //databases
     }
     
     
     private void writeSamples(XMLStreamWriter xmlWriter, GroupNode g, SampleData st) throws XMLStreamException {
 
         
         List<String> attributeHeaders =  new ArrayList<String>();
         
         for (Node s : g.getParentNodes()) {
             log.debug("Node "+s.getNodeName());
             //these should all be samples, but have to check anyway...
             if (SampleNode.class.isInstance(s)) {
                 SampleNode sample = (SampleNode) s;
                 xmlWriter.writeStartElement("Sample");
                 xmlWriter.writeAttribute("groupId", g.getGroupAccession());
                 xmlWriter.writeAttribute("id", sample.getSampleAccession());
 
                 //this should not be null and not be zero-length
                 writeAttributeValue(xmlWriter, "Sample Name", "false", "STRING", sample.getNodeName());
                 if (!attributeHeaders.contains("Sample Name")) attributeHeaders.add("Sample Name");
                 
                 //this should not be null and not be zero-length
                 writeAttributeValue(xmlWriter, "Sample Accession", "false", "STRING", sample.getSampleAccession());
                 if (!attributeHeaders.contains("Sample Accession")) attributeHeaders.add("Sample Accession");
                 
                 if (sample.getSampleDescription() != null && sample.getSampleDescription().trim().length() > 0){
                     writeAttributeValue(xmlWriter, "Sample Description", "false", "STRING", sample.getSampleDescription());
                     if (!attributeHeaders.contains("Sample Description")) attributeHeaders.add("Sample Description");
                 }
                 
                 //in order to make sure that attributes with multiple values are collected together, we need to pre-scan them.
                 Map<String, List<SCDNodeAttribute>> orderedAttributes = new HashMap<String, List<SCDNodeAttribute>>();
 
                 //TODO note that this does not maintain consistent ordering with that in the SampleTab
                 //TODO note this combines comments and characteristics of the same type on the same sample
                 for (SCDNodeAttribute a : sample.getAttributes()) {
                     boolean isCommentAttribute = false;
                     synchronized(CommentAttribute.class){
                         isCommentAttribute = CommentAttribute.class.isInstance(a);
                     }
                     boolean isCharacteristicAttribute = false;
                     synchronized(CharacteristicAttribute.class){
                         isCharacteristicAttribute = CharacteristicAttribute.class.isInstance(a);
                     }
 
                     if (a.getAttributeValue() != null && a.getAttributeValue().length() > 0) {
                         String header = null;
                         if (isCommentAttribute) {
                             CommentAttribute ca = (CommentAttribute) a;
                             header = ca.type;
                         } else if (isCharacteristicAttribute) {
                             CharacteristicAttribute ca = (CharacteristicAttribute) a;
                             header = ca.type;
                         } else {
                             header = a.getAttributeType();
                         }
                         if (!orderedAttributes.containsKey(header)){
                             orderedAttributes.put(header, new ArrayList<SCDNodeAttribute>());
                         }
                         orderedAttributes.get(header).add(a);
                     }
                 }
                 for(String attrType : orderedAttributes.keySet()) {
                     List<SCDNodeAttribute> attrlist = orderedAttributes.get(attrType);
                     List<AbstractNodeAttributeOntology> abstractattrlist = new ArrayList<AbstractNodeAttributeOntology>();
                     synchronized(AbstractNodeAttributeOntology.class){
                         for (SCDNodeAttribute a : attrlist){
                             if (AbstractNodeAttributeOntology.class.isInstance(a)) {
                                 abstractattrlist.add((AbstractNodeAttributeOntology) a);
                             }
                         }
                     }
                     
                     if (abstractattrlist.size() == attrlist.size()) {
                         writeAttribute(xmlWriter, attrType, "false", "STRING", abstractattrlist, st);
                         if (!attributeHeaders.contains(attrType)) attributeHeaders.add(attrType);
                     } else {
                         //handle non-ontology attributes e.g. database, same as, etc
                         boolean isDatabase = false;
                         synchronized(DatabaseAttribute.class){
                             isDatabase = DatabaseAttribute.class.isInstance(attrlist.get(0));
                         }
                         if (isDatabase){
                             //TODO full handle composite attribute eg. database
                             List<String> databaseNames = new ArrayList<String>(attrlist.size());
                             List<String> databaseIDs = new ArrayList<String>(attrlist.size());
                             List<String> databaseURIs = new ArrayList<String>(attrlist.size());
                             for (SCDNodeAttribute a : attrlist) {
                                 DatabaseAttribute dba = (DatabaseAttribute) a;
                                 databaseNames.add(dba.getAttributeValue());
                                 databaseIDs.add(dba.databaseID);
                                 databaseURIs.add(dba.databaseURI);
                             }
                             writeAttributeValue(xmlWriter, "Database Name", "false", "STRING", databaseNames);
                             writeAttributeValue(xmlWriter, "Database ID", "false", "STRING", databaseIDs);
                             writeAttributeValue(xmlWriter, "Database URI", "false", "STRING", databaseURIs);
                             if (!attributeHeaders.contains("Database Name")) attributeHeaders.add("Database Name");
                             if (!attributeHeaders.contains("Database ID")) attributeHeaders.add("Database ID");
                             if (!attributeHeaders.contains("Database URI")) attributeHeaders.add("Database URI");
                         } else {
                             List<String> attrvaluelist = new ArrayList<String>();
                             for (SCDNodeAttribute a : attrlist) {
                                 attrvaluelist.add(a.getAttributeValue());
                             }
                             writeAttributeValue(xmlWriter, attrType, "false", "STRING", attrvaluelist);
                             if (!attributeHeaders.contains(attrType)) attributeHeaders.add(attrType);
                         }
                     }
                 }
                 
                 //implicit derived from
                 for (Node p : s.getParentNodes()) { 
                     //these should all be samples, but have to check anyway...
                     if (SampleNode.class.isInstance(p)) {
                         SampleNode parent = (SampleNode) p;
                         writeAttributeValue(xmlWriter, "derived from", "false", "STRING", parent.getSampleAccession());
                     }
                 }
                 
                 xmlWriter.writeEndElement(); //Sample
             }
         }
 
         //write out precomputed attribute summary
         writeSampleAttributes(xmlWriter, attributeHeaders);
     }
 
     private void writeSampleAttributes(XMLStreamWriter xmlWriter, Collection<String> attributes) throws XMLStreamException {
         xmlWriter.writeStartElement("SampleAttributes");
         for (String header : attributes) {
             writeAttribute(xmlWriter, header, "false", "STRING");
         }
         xmlWriter.writeEndElement(); //SampleAttributes
     }
 }
