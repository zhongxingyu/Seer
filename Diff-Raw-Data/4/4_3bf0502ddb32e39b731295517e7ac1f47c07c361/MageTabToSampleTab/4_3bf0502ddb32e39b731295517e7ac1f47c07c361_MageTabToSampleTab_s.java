 package uk.ac.ebi.fgpt.sampletab.arrayexpress;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Map;
 
 import org.mged.magetab.error.ErrorItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.IDF;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.MAGETABInvestigation;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.ExtractNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.LabeledExtractNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SDRFNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SourceNode;
 import uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.attribute.CharacteristicsAttribute;
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.magetab.listener.ErrorItemListener;
 import uk.ac.ebi.arrayexpress2.magetab.parser.IDFParser;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.arrayexpress2.magetab.validator.Validator;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Organization;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Person;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 import uk.ac.ebi.arrayexpress2.sampletab.validator.SampleTabValidator;
 import uk.ac.ebi.fgpt.sampletab.CorrectorTermSource;
 
 public class MageTabToSampleTab {
 	private final MAGETABParser<MAGETABInvestigation> parser;
     private final List<ErrorItem> errorItems;
 
 	private SimpleDateFormat magetabdateformat = new SimpleDateFormat(
 			"yyyy-MM-dd");
 
 	// logging
 	public Logger log = LoggerFactory.getLogger(getClass());
 
 	public MageTabToSampleTab() {
 	    parser = new MAGETABParser<MAGETABInvestigation>();
         errorItems = new ArrayList<ErrorItem>();
 
         parser.addErrorItemListener(new ErrorItemListener() {
 
             public void errorOccurred(ErrorItem item) {
                 errorItems.add(item);
             }
         });
 	    
 	}
 
 	public SampleData convert(String idfFilename) throws IOException, ParseException {
 		return convert(new File(idfFilename));
 	}
 
 	public SampleData convert(File idfFile) throws IOException, ParseException {
         //A few IDF files specify multiple SDRF files which may not all have been downloaded.
         //Due to a bug in Limpopo, this can cause Limpopo to hang indefinitely.
         //therefore, first parse the IDF only to see if this is something to avoid.
         
         IDFParser idfparser = new IDFParser();
         IDF idf = null;
         idf = idfparser.parse(idfFile);
         if (idf.sdrfFile.size() != 1){
             throw new ParseException("Multiple sdrf file references");
         }
 
         errorItems.clear();
         MAGETABInvestigation mt = parser.parse(idfFile);
         if (!errorItems.isEmpty()) {
             // there are error items, print them and fail
             for (ErrorItem error : errorItems){
                 log.error(error.toString());
             }
             throw new ParseException();
         }
 		return convert(mt);
 	}
 	
 	private void convertPublications(MAGETABInvestigation mt, SampleData st){
         for (int i = 0; i < mt.IDF.publicationDOI.size() || i < mt.IDF.pubMedId.size(); i++){
             String doi = null;
             if (i < mt.IDF.publicationDOI.size()){
                 doi = mt.IDF.publicationDOI.get(i);
             }
             String pubmedid = null;
             if (i < mt.IDF.pubMedId.size()){
                 pubmedid = mt.IDF.pubMedId.get(i);
             }
             Publication pub = new Publication(pubmedid, doi);
             if (!st.msi.publications.contains(pub)){
                 st.msi.publications.add(pub);
             }
         }
 	}
 	
     private void convertPeople(MAGETABInvestigation mt, SampleData st){
         for (int i = 0; i < mt.IDF.personLastName.size() || 
             i < mt.IDF.personMidInitials.size() || 
             i < mt.IDF.personFirstName.size() || 
             i < mt.IDF.personEmail.size() || 
             i < mt.IDF.personRoles.size(); i++){
             String lastname = null;
             if (i < mt.IDF.personLastName.size()){
                 lastname = mt.IDF.personLastName.get(i);
             }
             String initials = null;
             if (i < mt.IDF.personMidInitials.size()){
                 initials = mt.IDF.personMidInitials.get(i);
             }
             String firstname = null;
             if (i < mt.IDF.personFirstName.size()){
                 firstname = mt.IDF.personFirstName.get(i);
             }
             String email = null;
             if (i < mt.IDF.personEmail.size()){
                 email = mt.IDF.personEmail.get(i);
             }
             // TODO fix minor spec mismatch when there are multiple roles for the
             // same person
             String role = null;
             if (i < mt.IDF.personRoles.size()){
                 role = mt.IDF.personRoles.get(i);
             }
             Person org = new Person(lastname, initials, firstname, email, role);
             if (!st.msi.persons.contains(org)){
                 st.msi.persons.add(org);
             }
         }   
     }
 
     private void convertOrganizations(MAGETABInvestigation mt, SampleData st){
 
         // AE doesn't really have organisations, but does have affiliations
         // st.msi.organizationURI/Email can't be mapped from ArrayExpress
         for (int i = 0; i < mt.IDF.personAffiliation.size() || 
                             i < mt.IDF.personAddress.size() || 
                             i < mt.IDF.personRoles.size(); i++){
             String name = null;
             if (i < mt.IDF.personAffiliation.size()){
                 name = mt.IDF.personAffiliation.get(i);
             }
             String uri = null;
             if (i < mt.IDF.personAddress.size()){
                 uri = mt.IDF.personAddress.get(i);
             }
             String role = null;
             if (i < mt.IDF.personRoles.size()){
                 role = mt.IDF.personRoles.get(i);
             }
             Organization org = new Organization(name, null, uri, null, role);
             if (!st.msi.organizations.contains(org)){
                 st.msi.organizations.add(org);
             }
         }
     }
     
     private String convertTermSource(String name, MAGETABInvestigation mt, SampleData st){
         for (int i = 0; i < mt.IDF.termSourceName.size(); i++){
             String tsname = null;
             if (i < mt.IDF.termSourceName.size()){
                 tsname = mt.IDF.termSourceName.get(i);
             }
             if (tsname.equals(name)){
                 String uri = null;
                 if (i < mt.IDF.termSourceFile.size()){
                     uri = mt.IDF.termSourceFile.get(i);
                 }
                 String version = null;
                 if (i < mt.IDF.termSourceVersion.size()){
                     version = mt.IDF.termSourceVersion.get(i);
                 }
                 TermSource ts = new TermSource(name, uri, version);
                 return st.msi.getOrAddTermSource(ts);
             }
         }
         throw new IllegalArgumentException("Unable to find term source "+name);
     }
     
 	public SampleData convert(MAGETABInvestigation mt)
 			throws ParseException {
 
 		SampleData st = new SampleData();
 		st.msi.submissionTitle = mt.IDF.investigationTitle;
 		st.msi.submissionDescription = mt.IDF.experimentDescription;
 		if (mt.IDF.publicReleaseDate == null){
 		    //null release date
 		    //ArrayExpress defaults to private
 		    //so set a date in the far future
 		    Calendar cal = new GregorianCalendar();
 		    cal.set(3000, 1, 1);
 		    st.msi.submissionReleaseDate = cal.getTime();
 		} else if (mt.IDF.publicReleaseDate != null && !mt.IDF.publicReleaseDate.trim().equals("")) {
 			try{
 			    st.msi.submissionReleaseDate = magetabdateformat
 					.parse(mt.IDF.publicReleaseDate.trim());
 			} catch (java.text.ParseException e){
 				log.error("Unable to parse release date "+mt.IDF.publicReleaseDate);
 			}
 		}
 		//reuse the release date as update date
 		st.msi.submissionUpdateDate = st.msi.submissionReleaseDate;
 		st.msi.submissionIdentifier = "GA" + mt.IDF.accession;
 		st.msi.submissionReferenceLayer = false;
 		
 		convertPublications(mt, st);
 		convertPeople(mt, st);
 		convertOrganizations(mt,st);
 
 		st.msi.databases.add(new Database("ArrayExpress", 
 		        "http://www.ebi.ac.uk/arrayexpress/experiments/"+ mt.IDF.accession,
 		        mt.IDF.accession));
 		
 		// TODO add samples...
 		// get the nodes that have relevant sample information
 		// e.g. characteristics
 		Collection<SDRFNode> samplenodes = new ArrayList<SDRFNode>();
 		for (SDRFNode node : mt.SDRF.getNodes("sourcename")) {
 			samplenodes.add(node);
 		}
 		for (SDRFNode node : mt.SDRF.getNodes("samplename")) {
 			samplenodes.add(node);
 		}
 		for (SDRFNode node : mt.SDRF.getNodes("extractname")) {
 			samplenodes.add(node);
 		}
 		for (SDRFNode node : mt.SDRF.getNodes("labeledextractname")) {
 			samplenodes.add(node);
 		}
 
 		// now get nodes that are the topmost nodes
 		ArrayList<SDRFNode> topnodes = new ArrayList<SDRFNode>();
 		for (SDRFNode node : samplenodes) {
 			if (node.getParentNodes().size() == 0) {
 				topnodes.add(node);
 			}
 		}
 
 		log.debug("Creating node names");
 		// create a sample from each topmost node
 		for (SDRFNode sdrfnode : topnodes) {
 
 		    
 			SampleNode scdnode = new SampleNode();
 			String name = sdrfnode.getNodeName();
 			log.debug("processing " + name);
 			scdnode.setNodeName(name);
 			
 			//TODO cascade down related nodes of the same top-level node?
 			//e.g. extracts taken from samples
 			
 			// since some attributes only exist for some sub-classes, need to
 			// test
 			// for instanceof for each of those sub-classes, cast accordingly
 			// and then access the attributes
 			List<CharacteristicsAttribute> characteristics = null;
 			Map<String, String> comments = null;
 			if (uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode.class.isInstance(sdrfnode)) {
 				// horribly long class references due to namespace collision
 				uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode sdrfsamplenode = (uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode) sdrfnode;
 				scdnode.setSampleDescription(sdrfsamplenode.description);
 				characteristics = sdrfsamplenode.characteristics;
 				comments = sdrfsamplenode.comments;
 			} else if (SourceNode.class.isInstance(sdrfnode)) {
 				SourceNode sdrfsourcenode = (SourceNode) sdrfnode;
 				scdnode.setSampleDescription(sdrfsourcenode.description);
 				characteristics = sdrfsourcenode.characteristics;
 				comments = sdrfsourcenode.comments;
 			} else if (ExtractNode.class.isInstance(sdrfnode)) {
 				ExtractNode sdrfextractnode = (ExtractNode) sdrfnode;
 				scdnode.setSampleDescription(sdrfextractnode.description);
 				characteristics = sdrfextractnode.characteristics;
 				comments = sdrfextractnode.comments;
 			} else if (LabeledExtractNode.class.isInstance(sdrfnode)) {
 				LabeledExtractNode sdrflabeledextractnode = (LabeledExtractNode) sdrfnode;
 				scdnode.setSampleDescription(sdrflabeledextractnode.description);
 				characteristics = sdrflabeledextractnode.characteristics;
 				comments = sdrflabeledextractnode.comments;
 			}
 
 			log.debug("got characteristics");
 			if (characteristics != null) {
 				for (CharacteristicsAttribute sdrfcharacteristic : characteristics) {
 					
 					if (sdrfcharacteristic.getAttributeValue() != null && sdrfcharacteristic.getAttributeValue().trim().length() > 0){
 	                    CharacteristicAttribute scdcharacteristic = new CharacteristicAttribute();
 					
     					scdcharacteristic.type = sdrfcharacteristic.type;
     					scdcharacteristic.setAttributeValue(sdrfcharacteristic
     							.getAttributeValue());
     					if (sdrfcharacteristic.unit != null) {
     						scdcharacteristic.unit = new UnitAttribute();
     						if (sdrfcharacteristic.unit.termSourceREF != null && sdrfcharacteristic.unit.termSourceREF.length() > 0){
         						scdcharacteristic.unit.setTermSourceREF(convertTermSource(sdrfcharacteristic.unit.termSourceREF, mt, st));
         						scdcharacteristic.unit.setTermSourceID(sdrfcharacteristic.unit.termAccessionNumber);
     						}
     					} else {
     					    if (sdrfcharacteristic.termSourceREF != null && sdrfcharacteristic.termSourceREF.length() > 0){
             					scdcharacteristic.setTermSourceREF(convertTermSource(sdrfcharacteristic.termSourceREF, mt, st));
             					scdcharacteristic.setTermSourceID(sdrfcharacteristic.termAccessionNumber);
         					}
     					}
     					scdnode.addAttribute(scdcharacteristic);
 					}
 				}
 			}
 			
 			log.debug("got comments");
 			if (comments != null) {
 				for (String key : comments.keySet()) {
 					CommentAttribute comment = new CommentAttribute();
 					comment.type = key;
 					comment.setAttributeValue(comments.get(key));
 					scdnode.addAttribute(comment);
 				}
 			}
 
 			st.scd.addNode(scdnode);
 		}
 		
 		//correct any term source issues here first
         CorrectorTermSource cts = new CorrectorTermSource();
         cts.correct(st);
 		
 		log.info("Finished convert()");
 		return st;
 	}
 
 	public void convert(MAGETABInvestigation mt, Writer writer)
 			throws IOException, ParseException {
 	    log.debug("recieved magetab, preparing to convert");
 		SampleData st = convert(mt);
 		log.debug("sampletab converted, preparing to output");
 		SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
 		log.debug("created SampleTabWriter");
 		sampletabwriter.write(st);
 		sampletabwriter.close();
 
 	}
 
 	public void convert(File idfFile, Writer writer) throws IOException,
 			ParseException {
         //a few idf files specify multiple sdrf files which may not all have been downloaded
         //due to a bug in limpopo, this can cause limpopo to hang indefinately.
         //therefore, first parse the idf only to see if this is something to avoid.
 
         log.info("Parsing IDF");
         IDFParser idfparser = new IDFParser();
         IDF idf = null;
         idf = idfparser.parse(idfFile);
         log.info("Checking IDF");
         if (idf.sdrfFile.size() != 1){
             log.error("Non-standard sdrf file references");
             throw new ParseException();
         }
         
         errorItems.clear();
 		MAGETABInvestigation mt = parser.parse(idfFile);
         if (!errorItems.isEmpty()) {
             // there are error items, print them and fail
             for (ErrorItem error : errorItems){
                 log.error(error.toString());
             }
             throw new ParseException();
         }
 		
 		convert(mt, writer);
 	}
 
 	public void convert(File idffile, String stfilename) throws IOException,
 			ParseException {
 		convert(idffile, new File(stfilename));
 	}
 
 	public void convert(File idffile, File stfile) throws IOException,
 			ParseException {
 		convert(idffile, new FileWriter(stfile));
 	}
 
 	public void convert(String idffilename, Writer writer) throws IOException,
 			ParseException {
 		convert(new File(idffilename), writer);
 	}
 
 	public void convert(String idffilename, File stfile) throws IOException,
 			ParseException, java.text.ParseException {
 		convert(idffilename, new FileWriter(stfile));
 	}
 
 	public void convert(String idffilename, String stfilename)
 			throws IOException, ParseException, java.text.ParseException {
 		convert(idffilename, new File(stfilename));
 	}
 
 	public static void main(String[] args) {
         new MageTabToSampleTab().doMain(args);
     }
 
     public void doMain(String[] args) {
 		if (args.length < 2) {
 			System.out
 					.println("Must provide an MAGETAB IDF filename and a SampleTab output filename.");
 			System.exit(1);
 			return;
 		}
 		String idfFilename = args[0];
 		String sampleTabFilename = args[1];
 		
         //a few idf files specify multiple sdrf files which may not all have been downloaded
         //due to a bug in limpopo, this can cause limpopo to hang indefinitely.
         //therefore, first parse the idf only to see if this is something to avoid.
 
         IDFParser idfparser = new IDFParser();
         IDF idf = null;
         try {
             idf = idfparser.parse(new File(idfFilename));
         } catch (ParseException e) {
             log.error("Error parsing " + idfFilename);
             e.printStackTrace();
             System.exit(1);
             return;
         }
         
         if (idf.sdrfFile.size() != 1){
             log.error("Non-standard sdrf file references");
             log.error(""+idf.sdrfFile);
             System.exit(1);
             return;
         }
 
 		try {
             convert(idfFilename, sampleTabFilename);
         } catch (IOException e) {
             log.error("Error converting "+idfFilename);
             e.printStackTrace();
             return;
         } catch (ParseException e) {
             log.error("Error converting "+idfFilename);
             e.printStackTrace();
             return;
         } catch (java.text.ParseException e) {
             log.error("Error converting "+idfFilename);
             e.printStackTrace();
             return;
         }
 	}
 }
