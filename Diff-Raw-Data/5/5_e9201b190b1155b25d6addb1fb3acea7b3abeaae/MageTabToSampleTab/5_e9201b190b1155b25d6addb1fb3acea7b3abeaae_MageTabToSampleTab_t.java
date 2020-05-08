 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
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
 import uk.ac.ebi.arrayexpress2.magetab.parser.IDFParser;
 import uk.ac.ebi.arrayexpress2.magetab.parser.MAGETABParser;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Database;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.Publication;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.msi.TermSource;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.UnitAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 public class MageTabToSampleTab {
 	private final MAGETABParser<MAGETABInvestigation> parser = new MAGETABParser<MAGETABInvestigation>();
 
 	private SimpleDateFormat magetabdateformat = new SimpleDateFormat(
 			"yyyy-MM-dd");
 
 	// logging
 	public Logger log = LoggerFactory.getLogger(getClass());
 
 	public MageTabToSampleTab() {
 	}
 
 	public SampleData convert(String idfFilename) throws IOException, ParseException {
 		return convert(new File(idfFilename));
 	}
 
 	public SampleData convert(File idfFile) throws IOException, ParseException {
         //A few idf files specify multiple sdrf files which may not all have been downloaded
         //Due to a bug in Limpopo, this can cause Limpopo to hang indefinately.
         //therefore, first parse the idf only to see if this is something to avoid.
         
         IDFParser idfparser = new IDFParser();
         IDF idf = null;
         idf = idfparser.parse(idfFile);
         if (idf.sdrfFile.size() != 1){
             throw new ParseException("Multiple sdrf file references");
         }
         
 		return convert(parser.parse(idfFile));
 	}
 
 	public SampleData convert(MAGETABInvestigation mt)
 			throws ParseException {
 
 		SampleData st = new SampleData();
 		st.msi.submissionTitle = mt.IDF.investigationTitle;
 		st.msi.submissionDescription = mt.IDF.experimentDescription;
 		if (mt.IDF.publicReleaseDate != null && !mt.IDF.publicReleaseDate.trim().equals("")) {
 			try{
			    st.msi.submissionReleaseDate = magetabdateformat
 					.parse(mt.IDF.publicReleaseDate.trim());
 			} catch (java.text.ParseException e){
 				log.error("Unable to parse release date "+mt.IDF.publicReleaseDate);
 			}
 		}
 		//reuse the relase date as update date
 		st.msi.submissionUpdateDate = st.msi.submissionReleaseDate;
 		st.msi.submissionIdentifier = "GA" + mt.IDF.accession;
 		st.msi.submissionReferenceLayer = false;
 		for (int i = 0; i < mt.IDF.publicationDOI.size() || i < mt.IDF.pubMedId.size(); i++){
 		    String doi = null;
 		    if (i < mt.IDF.publicationDOI.size()){
 		        doi = mt.IDF.publicationDOI.get(i);
 		    }
             String pubmedid = null;
             if (i < mt.IDF.pubMedId.size()){
                 pubmedid = mt.IDF.pubMedId.get(i);
             }
             st.msi.publications.add(new Publication(pubmedid, doi));
 		}
 
 		st.msi.personLastName = mt.IDF.personLastName;
 		st.msi.personInitials = mt.IDF.personMidInitials;
 		st.msi.personFirstName = mt.IDF.personFirstName;
 		st.msi.personEmail = mt.IDF.personEmail;
 		// TODO fix minor spec mismatch when there are multiple roles for the
 		// same person
 		st.msi.personRole = mt.IDF.personRoles;
 
 		// AE doesn't really have organisations, but does have affiliations
 		// TODO check and remove duplicates
 		st.msi.organizationName = mt.IDF.personAffiliation;
 		st.msi.organizationAddress = mt.IDF.personAddress;
 		// st.msi.organizationURI/Email/Role can't be mapped from ArrayExpress
 
 		st.msi.databases.add(new Database("ArrayExpress", 
 		        "http://www.ebi.ac.uk/arrayexpress/experiments/"+ mt.IDF.accession,
 		        mt.IDF.accession));
 		
         for (int i = 0; i < mt.IDF.termSourceName.size() || 
                             i < mt.IDF.termSourceFile.size() || 
                             i < mt.IDF.termSourceVersion.size(); i++){
             String name = null;
             if (i < mt.IDF.termSourceName.size()){
                 name = mt.IDF.termSourceName.get(i);
             }
             String uri = null;
             if (i < mt.IDF.termSourceFile.size()){
                 uri = mt.IDF.termSourceFile.get(i);
             }
             String version = null;
             if (i < mt.IDF.termSourceVersion.size()){
                 version = mt.IDF.termSourceVersion.get(i);
             }
            st.msi.termSources.add(new TermSource(name, uri, version));
         }
 
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
 			if (sdrfnode instanceof uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode) {
 				// horribly long class references due to namespace collision
 				uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode sdrfsamplenode = (uk.ac.ebi.arrayexpress2.magetab.datamodel.sdrf.node.SampleNode) sdrfnode;
 				scdnode.sampleDescription = sdrfsamplenode.description;
 				characteristics = sdrfsamplenode.characteristics;
 				comments = sdrfsamplenode.comments;
 			} else if (sdrfnode instanceof SourceNode) {
 				SourceNode sdrfsourcenode = (SourceNode) sdrfnode;
 				scdnode.sampleDescription = sdrfsourcenode.description;
 				characteristics = sdrfsourcenode.characteristics;
 				comments = sdrfsourcenode.comments;
 			} else if (sdrfnode instanceof ExtractNode) {
 				ExtractNode sdrfextractnode = (ExtractNode) sdrfnode;
 				scdnode.sampleDescription = sdrfextractnode.description;
 				characteristics = sdrfextractnode.characteristics;
 				comments = sdrfextractnode.comments;
 			} else if (sdrfnode instanceof LabeledExtractNode) {
 				LabeledExtractNode sdrflabeledextractnode = (LabeledExtractNode) sdrfnode;
 				scdnode.sampleDescription = sdrflabeledextractnode.description;
 				characteristics = sdrflabeledextractnode.characteristics;
 				comments = sdrflabeledextractnode.comments;
 			}
 
 			log.debug("got characteristics");
 			if (characteristics != null) {
 				for (CharacteristicsAttribute sdrfcharacteristic : characteristics) {
 					CharacteristicAttribute scdcharacteristic = new CharacteristicAttribute();
 					scdcharacteristic.type = sdrfcharacteristic.type;
 					scdcharacteristic.setAttributeValue(sdrfcharacteristic
 							.getAttributeValue());
 					if (sdrfcharacteristic.unit != null) {
 						scdcharacteristic.unit = new UnitAttribute();
 						scdcharacteristic.unit.setTermSourceREF(sdrfcharacteristic.unit.termSourceREF);
 						scdcharacteristic.unit.setTermSourceID(sdrfcharacteristic.unit.termAccessionNumber);
 					}
 					scdcharacteristic.setTermSourceREF(sdrfcharacteristic.termSourceREF);
 					scdcharacteristic.setTermSourceID(sdrfcharacteristic.termAccessionNumber);
 					scdnode.addAttribute(scdcharacteristic);
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
 
         log.info("Checking IDF");
         IDFParser idfparser = new IDFParser();
         IDF idf = null;
         idf = idfparser.parse(idfFile);
         log.info("Checking IDF");
         if (idf.sdrfFile.size() != 1){
             log.error("Non-standard sdrf file references");
             throw new ParseException();
         }
         
 		MAGETABInvestigation mt = parser.parse(idfFile);
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
         //due to a bug in limpopo, this can cause limpopo to hang indefinately.
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
