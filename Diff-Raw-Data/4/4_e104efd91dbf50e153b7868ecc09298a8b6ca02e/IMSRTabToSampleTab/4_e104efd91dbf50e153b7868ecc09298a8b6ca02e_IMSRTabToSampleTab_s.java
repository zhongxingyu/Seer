 package uk.ac.ebi.fgpt.sampletab;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.ebi.arrayexpress2.magetab.exception.ParseException;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.SampleData;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.SampleNode;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CharacteristicAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.CommentAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.MaterialAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.datamodel.scd.node.attribute.OrganismAttribute;
 import uk.ac.ebi.arrayexpress2.sampletab.renderer.SampleTabWriter;
 
 public class IMSRTabToSampleTab {
 
 	// singlton instance
 	private static final IMSRTabToSampleTab instance = new IMSRTabToSampleTab();
 
 	private static IMSRTabWebSummary summary = null;
 
 	// logging
 	private Logger log = LoggerFactory.getLogger(getClass());
 
 	private IMSRTabToSampleTab() {
 		// private constructor to prevent accidental multiple initialisations
 
 	}
 
 	private IMSRTabWebSummary getSummary() {
 		if (summary == null) {
 			summary = IMSRTabWebSummary.getInstance();
 		}
 		return summary;
 	}
 
 	public static IMSRTabToSampleTab getInstance() {
 		return instance;
 	}
 
 	public Logger getLog() {
 		return log;
 	}
 
 	public SampleData convert(String filename) throws IOException,
 			ParseException {
 		return convert(new File(filename));
 	}
 
 	public SampleData convert(File infile) throws ParseException, IOException {
 
 		SampleData st = new SampleData();
 
 		String stock = null;
 		String site = null;
 		String state = null;
 		String synonym = null;
 		String type = null;
 		String chr = null;
 		String mutation = null;
 		String alleleSymbol = null;
 		String alleleName = null;
 		String geneName = null;
 		BufferedReader input;
 		String line;
 		boolean headers = false;
 
 		// these store the data that we need to track
 		Map<String, Set<String>> synonyms = new HashMap<String, Set<String>>();
 		Map<String, Set<String>> types = new HashMap<String, Set<String>>();
 		Map<String, Set<String>> states = new HashMap<String, Set<String>>();
 		Map<String, Set<List<String>>> mutations = new HashMap<String, Set<List<String>>>();
 
 		log.info("Prepared for reading.");
 		try {
 			input = new BufferedReader(new FileReader(infile));
 			while ((line = input.readLine()) != null) {
 				// line too short? skip
 				if (line.length() == 0) {
 					continue;
 				}
 				if (!headers) {
 					headers = true;
 					continue;
 				}
 				String[] entries = line.split("\t");
 				// Strain/Stock Site State Synonyms Type Chr Mutation Allele
 				// Symbol Allele Name Gene Name
 				stock = entries[0];
 				if (site == null) {
 					site = entries[1];
 					addSite(st, site);
 				}
 				state = entries[2];
 				synonym = entries[3];
 				type = entries[4];
 				chr = entries[5];
 				mutation = entries[6];
 				alleleSymbol = entries[7];
 				alleleName = entries[8];
 				geneName = entries[9];
 
 				// always store stock in synonyms to pull them back out later
 				if (!synonyms.containsKey(stock)) {
 					synonyms.put(stock, new HashSet<String>());
 				}
 				if (synonym.length() > 0
 						&& !synonyms.get(stock).contains(synonym)) {
 					synonyms.get(stock).add(synonym);
 				}
 
 				if (!types.containsKey(stock)) {
 					types.put(stock, new HashSet<String>());
 				}
 				if (type.length() > 0 && !types.get(stock).contains(type)) {
 					types.get(stock).add(type);
 				}
 
 				if (!states.containsKey(stock)) {
 					states.put(stock, new HashSet<String>());
 				}
 				if (state.length() > 0 && !states.get(stock).contains(state)) {
 					states.get(stock).add(state);
 				}
 
 				if (geneName.length() > 0) {
 					List<String> mutantlist = new ArrayList<String>();
 					mutantlist.add(chr);
 					mutantlist.add(mutation);
 					mutantlist.add(alleleSymbol);
 					mutantlist.add(alleleName);
 					mutantlist.add(geneName);
 
 					// check the stock name is in the mutations map
 					if (!mutations.containsKey(stock)) {
 						mutations.put(stock, new HashSet<List<String>>());
 					}
 					// actually add the mutationlist
 					if (!mutations.get(stock).contains(mutantlist)) {
 						mutations.get(stock).add(mutantlist);
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		getLog().info("Finished reading, starting conversion");
 
 		// now all the data has been parsed into memory, but we need to turn
 		// them into sample objects
 
 		for (String name : synonyms.keySet()) {
 			SampleNode newnode = new SampleNode();
 			newnode.setNodeName(name);
 
 			if (synonyms.containsKey(name)) {
 				for (String thissynonym : synonyms.get(name)) {
 					CommentAttribute synonymattrib = new CommentAttribute();
 					synonymattrib.type = "Synonym";
 					synonymattrib.setAttributeValue(thissynonym);
 					// insert all synonyms at position zero so they display next
 					// to name
 					newnode.addAttribute(synonymattrib, 0);
 				}
 			}
 
 			if (states.containsKey(name) && (states.get(name).size() > 1)) {
 				// if there are multiple materials
 				// create a strain sample and derive individual materials
 
 				MaterialAttribute matterialattribute = new MaterialAttribute();
 				matterialattribute.setAttributeValue("strain");
 				newnode.addAttribute(matterialattribute);
 				
 				// add the strain node to the st
 				st.scd.addNode(newnode);
 
 				for (String material: states.get(name)){
 					SampleNode materialnode = new SampleNode();
 					materialnode.setNodeName(name+" "+material);
 					
 					matterialattribute = new MaterialAttribute();
 					matterialattribute.setAttributeValue(material);
 					materialnode.addAttribute(matterialattribute);
 					
 					newnode.addChildNode(materialnode);
 					materialnode.addParentNode(newnode);
 					//dont add the separate material node
 					//If you do, duplication will result.
 					//st.scd.addNode(materialnode);
 					
 				}
 
 			} else if (states.containsKey(name)
 					&& (states.get(name).size() == 1)) {
 				// if there is only one material
 				//this is a for loop that only runs once because no easy way to access a member of a set
 				for (String materal: states.get(name)){
 					MaterialAttribute matterialattribute = new MaterialAttribute();
 					matterialattribute.setAttributeValue(materal);
 					newnode.addAttribute(matterialattribute);
 					break;
 				}
 
 				// TODO add efo mappings
 
 				// add the node to the st
 				st.scd.addNode(newnode);
 			} else {
 				// no material
 				// should never happen?
 				// TODO check
 				log.warn("found a sample without material: " + name);
 				continue;
 			}
 			
 			//all IMSR samples must be mice.
 			OrganismAttribute organismattribute = new OrganismAttribute();
 			organismattribute.setAttributeValue("Mus musculus (Mouse)");
 			organismattribute.setTermSourceREF("NEWT");
 			organismattribute.setTermSourceID(10090);
 			
 
 			if (mutations.containsKey(name)) {
 				for (List<String> thismutation : mutations.get(name)) {
 					chr = thismutation.get(0);
 					mutation = thismutation.get(1);
 					alleleSymbol = thismutation.get(2);
 					alleleName = thismutation.get(3);
 					geneName = thismutation.get(4);
 
 					if (chr.length() > 0) {
 						newnode.addAttribute(CharacteristicAttribute.makeNew(
 								"Mutation Chromosome", chr));
 					}
 					if (mutation.length() > 0) {
 						newnode.addAttribute(CharacteristicAttribute.makeNew(
 								"Mutation Type", mutation));
 						// TODO add EFO mappings
 					}
 					if (alleleSymbol.length() > 0) {
 						newnode.addAttribute(CharacteristicAttribute.makeNew(
 								"Allele Symbol", alleleSymbol));
 					}
 					if (alleleName.length() > 0) {
 						newnode.addAttribute(CharacteristicAttribute.makeNew(
 								"Allele Name", alleleName));
 					}
 					if (geneName.length() > 0) {
 						newnode.addAttribute(CharacteristicAttribute.makeNew(
 								"Gene Name", geneName));
 					}
 				}
 			}
 		}
 		getLog().info("Finished convert()");
 		return st;
 	}
 
 	public void convert(File file, Writer writer) throws IOException,
 			ParseException {
 		getLog().debug("recieved magetab, preparing to convert");
 		SampleData st = convert(file);
 
 		getLog().info("SampleTab converted, preparing to write");
 		SampleTabWriter sampletabwriter = new SampleTabWriter(writer);
 		sampletabwriter.write(st);
 		getLog().info("SampleTab written");
 		sampletabwriter.close();
 
 	}
 
 	public void convert(File infile, String outfilename) throws IOException,
 			ParseException {
 
 		convert(infile, new File(outfilename));
 	}
 
 	public void convert(File infile, File outfile) throws IOException,
 			ParseException {
 
 		// create parent directories, if they dont exist
 		outfile = outfile.getAbsoluteFile();
 		if (outfile.isDirectory()) {
			outfile = new File(outfile, "sampletab.txt");
 		}
 		if (!outfile.getParentFile().exists()) {
 			outfile.getParentFile().mkdirs();
 		}
 
 		convert(infile, new FileWriter(outfile));
 	}
 
 	public void convert(String infilename, Writer writer) throws IOException,
 			ParseException {
 		convert(new File(infilename), writer);
 	}
 
 	public void convert(String infilename, File outfile) throws IOException,
 			ParseException {
 		convert(infilename, new FileWriter(outfile));
 	}
 
 	public void convert(String infilename, String outfilename)
 			throws IOException, ParseException {
 		convert(infilename, new File(outfilename));
 	}
 
 	private void addSite(SampleData st, String site) {
 		log.info("Adding site " + site);
 		assert getSummary().sites.contains(site);
 		int index = getSummary().sites.indexOf(site);
 		assert index >= 0;
 		st.msi.submissionTitle = "International Mouse Strain Resource - "
 				+ getSummary().facilities.get(index);
 		st.msi.submissionDescription = "The IMSR is a searchable online database of mouse strains and stocks available worldwide, including inbred, mutant, and genetically engineered mice. The goal of the IMSR is to assist the international scientific community in locating and obtaining mouse resources for research. These samples are held by "
 				+ getSummary().facilities.get(index);
 		st.msi.submissionReleaseDate = getSummary().updates.get(index);
 		st.msi.submissionIdentifier = "GMS-" + site;
 		st.msi.submissionReferenceLayer = true;
 
 		st.msi.organizationName.add("International Mouse Strain Resource");
 		st.msi.organizationAddress.add("");
 		st.msi.organizationURI.add("http://www.findmice.org/");
 		st.msi.organizationEmail.add("");
 		st.msi.organizationRole.add("Submitter");
 
 		st.msi.organizationName.add(getSummary().facilities.get(index));
 		st.msi.organizationAddress.add("");
 		st.msi.organizationURI.add("");
 		st.msi.organizationEmail.add("");
 		st.msi.organizationRole.add("Biomaterial Provider");
 
 		// TODO need mapping between site name and site number to do this
 		st.msi.databaseName.add("IMSR");
 		st.msi.databaseID.add("");
 		st.msi.databaseURI.add("");
 
 		st.msi.termSourceName.add("NEWT");
 		st.msi.termSourceURI.add("http://www.ebi.ac.uk/newt/");
 		st.msi.termSourceVersion.add("");
 
 		st.msi.termSourceName.add("EFO");
 		st.msi.termSourceURI.add("http://www.ebi.ac.uk/efo/");
 		st.msi.termSourceVersion.add("2.13.1");
 	}
 
 	public static void main(String[] args) {
 		if (args.length < 2) {
 			System.out
 					.println("Must provide an IMSR Tab input filename and a SampleTab output filename.");
 			return;
 		}
 		String imsrTabFilename = args[0];
 		String sampleTabFilename = args[1];
 
 		IMSRTabToSampleTab converter = IMSRTabToSampleTab.getInstance();
 
 		try {
 			converter.convert(imsrTabFilename, sampleTabFilename);
 		} catch (ParseException e) {
 			System.out.println("Error converting " + imsrTabFilename + " to "
 					+ sampleTabFilename);
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Error converting " + imsrTabFilename + " to "
 					+ sampleTabFilename);
 			e.printStackTrace();
 		}
 	}
 }
