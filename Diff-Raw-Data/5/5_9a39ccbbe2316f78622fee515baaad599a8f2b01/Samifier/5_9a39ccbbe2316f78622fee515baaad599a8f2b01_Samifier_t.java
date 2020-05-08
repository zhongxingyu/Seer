 package au.org.intersect.samifier;
 
 import org.apache.commons.cli.*;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 import java.io.*;
 import java.math.BigDecimal;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Appender;
 import org.apache.log4j.Logger;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.PatternLayout;
 
 public class Samifier {
 
     private static Logger LOG = Logger.getLogger(Samifier.class);
 
     public static final int SAM_REVERSE_FLAG = 0x10;
     public static final int BASES_PER_CODON = 3;
 
     private Samifier(){}
 
     public static Map<String,String> parseProteinToOLNMappingFile(File f)
         throws IOException, FileNotFoundException, ProteinToOLNMappingFileParsingException
     {
         Map<String,String> proteinOLN = new HashMap<String,String>();
 
         BufferedReader reader = null;
         try{
             reader = new BufferedReader(new FileReader(f));
  
             // Skip header line
             String line = reader.readLine();
             int lineNumber = 1;
             while ((line = reader.readLine()) != null)
             {
                 lineNumber++;
                 if (line.matches("^#.*$"))
                 {
                     continue;
                 }
                 // ordered_locus_name accession_id protein_name id
                 // Tab delimited
                 String[] parts = line.split("\\s+");
                 if (parts.length < 3)
                 {
                     throw new ProteinToOLNMappingFileParsingException("Line "+lineNumber+" not in expected format, should be: ordered_locus_name accession_id protein_name id");
                 }
                 proteinOLN.put(parts[2], parts[0]);
             }
         }
         finally
         {
             if (reader != null)
             {
                 reader.close();
             }
         }
         return proteinOLN;
     }
 
     public static List<PeptideSearchResult> parseMascotPeptideSearchResults(File resultsFile, Map<String,String> proteinOLN)
             throws MascotParsingException {
 
         try
         {
             BufferedReader headerReader = new BufferedReader(new FileReader(resultsFile));
             String firstLine = headerReader.readLine();
             headerReader.close();
             // Detect mzidentML format or text format
             if (firstLine.startsWith("<?xml "))
             {
                 return parseMascotPeptideSearchResultsMzidentMLFormat(resultsFile, proteinOLN);
             }
             else
             {
                 return parseMascotPeptideSearchResultsDATFormat(resultsFile, proteinOLN);
             }
         }
         catch (IOException e)
         {
             throw new MascotParsingException(e);
         }
     }
 
     public static List<PeptideSearchResult> parseMascotPeptideSearchResultsMzidentMLFormat(File resultsFile, Map<String,String> proteinOLN)
         throws MascotParsingException
     {
         List<PeptideSearchResult> results = new ArrayList<PeptideSearchResult>();
 
         try
         {
             XPathFactory factory = XPathFactory.newInstance();
             XPath xPath = factory.newXPath();
 
             xPath.setNamespaceContext(new NamespaceContext() {
                 public String getNamespaceURI(String prefix) {
                     if (prefix == null) throw new NullPointerException("Null prefix");
                     else if ("mzidentml".equals(prefix)) return "http://psidev.info/psi/pi/mzIdentML/1.0";
                     return XMLConstants.NULL_NS_URI;
                 }
                 public String getPrefix(String uri) {
                     throw new UnsupportedOperationException();
                 }
 
                 public Iterator getPrefixes(String uri) {
                     throw new UnsupportedOperationException();
                 }
             });
 
 
             String xPathStr = "//mzidentml:Peptide";
             InputSource iS = new InputSource(new FileReader(resultsFile));
             Node root = (Node) xPath.evaluate("/", iS, XPathConstants.NODE);
 
             QName nodesetType = XPathConstants.NODESET;
             QName nodeType = XPathConstants.NODE;
 
             NodeList peptideList = (NodeList)xPath.evaluate(xPathStr, root, nodesetType);
             for (int peptideIndex = 0 ; peptideIndex < peptideList.getLength(); peptideIndex++)
             {
                 Node peptideNode = peptideList.item(peptideIndex);
                 String peptideId = peptideNode.getAttributes().getNamedItem("id").getNodeValue();
                 String peptideSequenceXpath = "./mzidentml:peptideSequence";
                 Node peptideSequenceNode = (Node)xPath.evaluate(peptideSequenceXpath, peptideNode, nodeType);
                 String peptideSequence = peptideSequenceNode.getTextContent();
 
                 String peptideInfoXpath = "//mzidentml:SpectrumIdentificationItem[@Peptide_ref='" + peptideId + "']";
                 Node peptideInfo = (Node)xPath.evaluate(peptideInfoXpath, root, nodeType);
 
                 String peptideEvidenceXpath = "./mzidentml:PeptideEvidence";
                 String confidenceScoreXpath = "//mzidentml:cvParam[@name='mascot:score']";
 
                 NodeList peptideEvidenceList = (NodeList)xPath.evaluate(peptideEvidenceXpath, peptideInfo, nodesetType);
                 Node confidenceScoreNode = (Node)xPath.evaluate(confidenceScoreXpath, peptideInfo, nodeType);
                 String confidenceScoreStr = confidenceScoreNode.getAttributes().getNamedItem("value").getNodeValue();
                 BigDecimal confidenceScore = new BigDecimal(confidenceScoreStr);
 
                 for (int peptideEvidenceIndex = 0; peptideEvidenceIndex < peptideEvidenceList.getLength(); peptideEvidenceIndex++)
                 {
                     Node peptideEvidence = peptideEvidenceList.item(peptideEvidenceIndex);
                     String id = peptideEvidence.getAttributes().getNamedItem("id").getNodeValue();
                     String start = peptideEvidence.getAttributes().getNamedItem("start").getNodeValue();
                     String stop = peptideEvidence.getAttributes().getNamedItem("end").getNodeValue();
                     String dbSequenceRef = peptideEvidence.getAttributes().getNamedItem("DBSequence_Ref").getNodeValue();
 
                     String dbSequenceXpath = "//mzidentml:DBSequence[@id='" + dbSequenceRef + "']";
                     Node dbSequence = (Node)xPath.evaluate(dbSequenceXpath, root, nodeType);
                     String protein = dbSequence.getAttributes().getNamedItem("accession").getNodeValue();
                     if (!proteinOLN.containsKey(protein))
                     {
                         LOG.info(protein + " not found in given accession mapping file");
                         continue;
                     }
                     results.add(new PeptideSearchResult(id, peptideSequence, protein, Integer.parseInt(start), Integer.parseInt(stop), confidenceScore));
                 }
             }
         }
         catch (Exception e)
         {
             throw new MascotParsingException(e);
         }
 
 
         return results;
     }
 
     public static List<PeptideSearchResult> parseMascotPeptideSearchResultsDATFormat(File resultsFile, Map<String,String> proteinOLN)
         throws MascotParsingException
     {
         BufferedReader reader = null;
         boolean peptidesSectionStarted = false;
         List<PeptideSearchResult> results = new ArrayList<PeptideSearchResult>();
         try {
             reader = new BufferedReader(new FileReader(resultsFile));
             String line;
             int lineNumber = 0;
             while ((line = reader.readLine()) != null)
             {
                 lineNumber++;
                 if (peptidesSectionStarted)
                 {
                     if (line.startsWith("--"))
                     {
                         break;
                     }
                     results.addAll(getProteinsFromQueryLine(line, proteinOLN));
                 }
                 else if (line.startsWith("Content-Type: application/x-Mascot; name=\"peptides\""))
                 {
                     peptidesSectionStarted = true;
                 }
             }
         }
         catch (Exception e)
         {
             throw new MascotParsingException(e);
         }
         finally
         {
             try
             {
                 if (reader != null)
                 {
                     reader.close();
                 }
             }
             catch (IOException e)
             {
                 throw new MascotParsingException(e);
             }
         }
         return results;
     }
 
     public static PeptideSequence getPeptideSequence(PeptideSearchResult peptide, File chromosomeFile, GeneInfo gene)
         throws FileNotFoundException, IOException
     {
         List<NucleotideSequence> sequenceParts = extractSequenceParts(chromosomeFile, gene);
         StringBuilder nucleotideSequence = new StringBuilder();
         StringBuilder cigar = new StringBuilder();
 
         // Coordinates for the peptide are 1-based, so substract 1 so it
         // can be used with a 0-based string slice.
         int relativeStart = (peptide.getPeptideStart() - 1) * BASES_PER_CODON;
         int relativeStop  = peptide.getPeptideStop() * BASES_PER_CODON - 1;
 
         int absoluteStartIndex = 0;
         int absoluteStopIndex = 0;
         int readCursor = 0;
         String direction = gene.getDirection();
 
         /*
          * The chromosome nucleotide sequence contains everything- exons and
          * introns. The peptide positions we are given exclude the introns.
          *
          * Hence, we walk through each sequence part (describe in the genome
          * file), skipping past introns and just counting through exons.
          */
         for (NucleotideSequence part : sequenceParts)
         {
             // Skip introns, but mark them in the cigar string
             if (GeneSequence.INTRON.equals(part.getType()))
             {
                 /*
                    We don't start cigar strings with introns.
                    A non-empty cigar string means we've already got part
                    of the peptide's sequence, and this intron is in the middle.
                 */
                 int size = part.getStopIndex()-part.getStartIndex()+1;
                 if (cigar.length() > 0)
                 {
                     updateCigar(cigar, size, GeneSequence.INTRON, direction);
                     absoluteStopIndex += size;
                 }
                 else
                 {
                     // This intron is before our absolute start position
                     absoluteStartIndex += size;
                     absoluteStopIndex += size;
                 }
                 continue;
             }
 
             int sequenceSize = part.getSequence().length();
             int substringStart = 0;
             int substringEnd   = sequenceSize;
 
             // If the desired start position is not in this coding sequence,
             // move our cursor past it
             if (relativeStart > (readCursor + sequenceSize - 1))
             {
                 readCursor += sequenceSize;
                 absoluteStartIndex += sequenceSize;
                 absoluteStopIndex += sequenceSize;
                 continue;
             }
 
             // After skipping through, the next part should have the starting
             // position within it. Update the absoluteStartIndex for the last
             // time.
             if (readCursor < relativeStart)
             {
                 substringStart = relativeStart - readCursor;
                 absoluteStartIndex += substringStart;
                 absoluteStopIndex += substringStart;
             }
 
             // If this part contains the stop position, then this is the last
             // part to process.
             if ((readCursor + sequenceSize) > relativeStop)
             {
                 substringEnd = relativeStop - readCursor + 1;
                 nucleotideSequence.append(part.getSequence().substring(substringStart, substringEnd));
                 int partSize = substringEnd - substringStart;
                 absoluteStopIndex += partSize;
                 updateCigar(cigar, partSize, GeneSequence.CODING_SEQUENCE, direction);
                 break;
             }
 
             nucleotideSequence.append(part.getSequence().substring(substringStart, substringEnd));
             int partSize = substringEnd - substringStart;
             absoluteStopIndex += partSize;
             updateCigar(cigar, partSize, GeneSequence.CODING_SEQUENCE, direction);
             readCursor = substringEnd;
         }
 
         String peptideSequence = GeneInfo.REVERSE.equals(direction) ? nucleotideSequence.reverse().toString() : nucleotideSequence.toString();
         // When direction is reverse, 
         //  5           17
         // |####----#####|
         //   ^        S
         //   6        15
         //   absoluteStopIndex = 11, from 17 to 6
         //   absoluteStartIndex = 2, from 17 to 15
         //   startIndex  = (17-5 = 12) - 11 + 1 = 2 (because it is 1 based)
         //   stopIndex  = (17-5 = 12) - 2 + 1 = 11 (because it is 1 based)
         int startIndex = GeneInfo.REVERSE.equals(direction) ? (gene.getStop() - gene.getStart() - absoluteStopIndex + 1) : absoluteStartIndex;
         int stopIndex = GeneInfo.REVERSE.equals(direction) ? (gene.getStop() - gene.getStart() - absoluteStartIndex + 1) : absoluteStopIndex;
         int bedStartIndex = gene.getStart() + startIndex - 1; // BED files are zero based (6 in the example)
         int bedStopIndex  = gene.getStart() + stopIndex - 1; // BED files are zero based (15 in the example)
         return new PeptideSequence(peptideSequence, cigar.toString(), startIndex, bedStartIndex, bedStopIndex);
     }
 
     private static void updateCigar(StringBuilder cigar, int size, String type, String direction)
     {
         String marker = GeneSequence.INTRON.equals(type) ? "N" : "M";
         if (GeneInfo.REVERSE.equals(direction))
         {
             cigar.insert(0, marker);
             cigar.insert(0, size);
         }
         else
         {
             cigar.append(size);
             cigar.append(marker);
         }
     }
 
     public static String createBEDLine(PeptideSequence peptide, String chromosome, String name)
     {
         StringBuilder output = new StringBuilder();
         output.append(chromosome).append("\t");
         output.append(peptide.getBedStartIndex()).append("\t");
         output.append(peptide.getBedStopIndex()).append("\t");
         output.append(name);
         output.append(System.getProperty("line.separator"));
 
         return output.toString();
     }
 
     public static void createSAM(Genome genome, Map<String, String> proteinOLNMap, List<PeptideSearchResult> peptideSearchResults, File chromosomeDirectory, Writer output, Writer bedWriter)
         throws FileNotFoundException, IOException
     {
         LOG.debug("creating sam file");
         List<SAMEntry> samEntries = new ArrayList<SAMEntry>();
         for (PeptideSearchResult result : peptideSearchResults)
         {
             String proteinName = result.getProteinName();
             String oln = proteinOLNMap.get(proteinName);
 
             GeneInfo gene = genome.getGene(oln);
             if (gene == null)
             {
                 LOG.info("Protein ID found in accession file, but locus not found in genome file");
                 LOG.info("ERR_GFF: " + proteinName + " " + oln);
                 continue;
             }
 
             File chromosomeFile = getChromosomeFile(chromosomeDirectory, gene);
 
             PeptideSequence peptide = getPeptideSequence(result, chromosomeFile, gene);
             String resultName = proteinName+"."+result.getId();
 
             int peptideStart = peptide.getStartIndex() + gene.getStart();
 
             if (bedWriter != null)
             {
                 bedWriter.write(createBEDLine(peptide, gene.getChromosome(), resultName));
             }
 
             samEntries.add(new SAMEntry(resultName, gene, peptideStart, peptide.getCigarString(), peptide.getNucleotideSequence()));
         }
 
         String prevChromosome = null;
         Collections.sort(samEntries, new SAMEntryComparator());
         for (SAMEntry samEntry : samEntries)
         {
             String chromosome = samEntry.getRname();
             if (! chromosome.equals(prevChromosome))
             {
                 samEntry.setRnext("=");
                 prevChromosome = chromosome;
             }
             output.write(samEntry.toString());
         }
 
         if (bedWriter != null)
         {
             bedWriter.close();
         }
         output.close();
     }
 
     private static File getChromosomeFile(File chromosomeDirectory, GeneInfo gene) {
         // TODO: find the different chrormosome file extensions
         return new File(chromosomeDirectory, gene.getChromosome() + ".fa");
     }
 
     private static List<PeptideSearchResult> getProteinsFromQueryLine(String line, Map<String,String> proteinOLN)
         throws IOException
     {
         List<PeptideSearchResult> results = new ArrayList<PeptideSearchResult>();
         // Expected format:
         // q21_p1=0,705.406113,-0.000065,4,EFGILK,18,00000000,25.95,0000000001000002010,0,0;"KPYK1_YEAST":0:469:474:1,"RL31B_YEAST":0:78:86:1
         Pattern linePattern = Pattern.compile("^(q\\d+_p\\d+)=([^;]+);(.+)$");
         Pattern proteinPartPattern = Pattern.compile("^\"([^\"]+)\":\\d\\:(\\d+)\\:(\\d+)\\:\\d$");
         Matcher lineMatcher = linePattern.matcher(line);
 
         if (lineMatcher.matches())
         {
             String id = lineMatcher.group(1);
             String peptidePart = lineMatcher.group(2);
             String proteinsPart = lineMatcher.group(3);
 
             // Expected format:
             // 0,705.406113,-0.000065,4,EFGILK,18,00000000,25.95,0000000001000002010,0,0
             String[] peptideParts = peptidePart.split(",");
             String peptideSequence = peptideParts[4];
             BigDecimal confidenceScore = new BigDecimal(peptideParts[7]);
 
             // Expected format:
             // "KPYK1_YEAST":0:469:474:1,"RL31B_YEAST":0:78:86:1, ...
             String[] proteins = proteinsPart.split(",");
             for (String proteinPart : proteins)
             {
                 // Expected format:
                 // "KPYK1_YEAST":0:469:474:1
                 Matcher proteinPartMatcher = proteinPartPattern.matcher(proteinPart);
                 if (proteinPartMatcher.matches())
                 {
                     String protein = proteinPartMatcher.group(1);
                     if (!proteinOLN.containsKey(protein))
                     {
                         LOG.info("Protein ID not found in accession file");
                         LOG.info("ERR_ACC: " + protein);
                         continue;
                     }
                     int start = Integer.parseInt(proteinPartMatcher.group(2));
                     int stop  = Integer.parseInt(proteinPartMatcher.group(3));
                     results.add(new PeptideSearchResult(id, peptideSequence, protein, start, stop, confidenceScore));
                 }
             }
         }
         return results;
     }
 
     public static List<NucleotideSequence> extractSequenceParts(File chromosomeFile, GeneInfo gene)
         throws FileNotFoundException, IOException
     {
         if (!chromosomeFile.exists())
         {
             throw new FileNotFoundException(chromosomeFile.getAbsolutePath() + " not found");
         }
 
         String direction = gene.getDirection();
 
         List<NucleotideSequence> parts = new ArrayList<NucleotideSequence>();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new FileReader(chromosomeFile));
             // Skip header of FASTA file
             String line = reader.readLine();
  
             int readCursor = 0;
             List<GeneSequence> locations = gene.getLocations();
             for (GeneSequence location : locations)
             {
                 int startIndex = location.getStart();
                 int stopIndex  = location.getStop();
 
                 if (GeneSequence.INTRON.equals(location.getSequenceType()))
                 {
                     parts.add(new NucleotideSequence(null, GeneSequence.INTRON, startIndex, stopIndex));
                     continue;
                 }
 
                 StringBuilder sequence = new StringBuilder();
 
                 // Read forward to startIndex
                 while (readCursor < startIndex)
                 {
                     line = reader.readLine();
                     line = line.replace("\r", "").replace("\n", "");
                     readCursor += line.length();
                 }
 
                 // GFF (Genome) files use 1-based indices
                 int readStart = (startIndex - 1) % line.length();
                 int readStop  = line.length(); 
 
                 // Read in the nucleotide sequence
                 while (readCursor < stopIndex)
                 {
                     sequence.append(line.substring(readStart, readStop));
                     readStart = 0;
                     line = reader.readLine();
                     line = line.replace("\r", "").replace("\n", "");
                     readCursor += line.length();
                 }
 
                 // Get the last piece
                 readStop = (stopIndex - 1) % line.length();
                 sequence.append(line.substring(readStart, readStop + 1));
 
                 String sequenceString = GeneInfo.REVERSE.equals(direction) ? sequence.reverse().toString() : sequence.toString();
                 parts.add(new NucleotideSequence(sequenceString, GeneSequence.CODING_SEQUENCE, startIndex, stopIndex));
             }
 
         }
         finally
         {
             if (reader != null)
             {
                 reader.close();
             }
         }
 
         if (GeneInfo.REVERSE.equals(direction))
         {
             Collections.reverse(parts);
         }
 
         return parts;
     }
 
     public static void main(String[] args)
     {
         Option resultsFile = OptionBuilder.withArgName("searchResultsFile")
                                           .hasArgs()
                                           .withDescription("Mascot search results file in txt format")
                                           .isRequired()
                                           .create("r");
         Option mappingFile = OptionBuilder.withArgName("mappingFile")
                                           .hasArg()
                                           .withDescription("File mapping protein identifier to ordered locus name")
                                           .isRequired()
                                           .create("m");
         Option genomeFileOpt = OptionBuilder.withArgName("genomeFile")
                                           .hasArg()
                                           .withDescription("Genome file in gff format")
                                           .isRequired()
                                           .create("g");
         Option chrDirOpt  = OptionBuilder.withArgName("chromosomeDir")
                                           .hasArg()
                                           .withDescription("Directory containing the chromosome files in FASTA format for the given genome")
                                           .isRequired()
                                           .create("c");
         Option logFile = OptionBuilder.withArgName("logFile")
                                           .hasArg()
                                           .withDescription("Filename to write the log into")
                                          .create("l");
         Option outputFile = OptionBuilder.withArgName("outputFile")
                                           .hasArg()
                                           .withDescription("Filename to write the SAM format file to")
                                           .isRequired()
                                           .create("o");
         Option bedFile = OptionBuilder.withArgName("bedFile")
                                           .hasArg()
                                           .withDescription("Filename to write IGV regions of interest (BED) file to")
                                           .isRequired()
                                           .create("b");
         Options options = new Options();
         options.addOption(resultsFile);
         options.addOption(mappingFile);
         options.addOption(genomeFileOpt);
         options.addOption(chrDirOpt);
         options.addOption(logFile);
         options.addOption(outputFile);
         options.addOption(bedFile);
 
         CommandLineParser parser = new GnuParser();
         try {
             CommandLine line = parser.parse( options, args );
             String[] searchResultsPaths = line.getOptionValues("r");
             File genomeFile = new File(line.getOptionValue("g"));
             File mapFile = new File(line.getOptionValue("m"));
             File chromosomeDir = new File(line.getOptionValue("c"));
             File outfile = new File(line.getOptionValue("o"));
            String logFileName = line.getOptionValue("l");
             String bedfilePath = line.getOptionValue("b");
 
             if (logFileName != null)
             {
                 Logger.getRootLogger().removeAppender("stdout");
                 FileAppender fa = new FileAppender();
                 fa.setName("FileLogger");
                 fa.setFile(logFileName);
                 fa.setLayout(new PatternLayout("%d %-5p %c - %m%n"));
                 fa.setThreshold(Level.DEBUG);
                 fa.setAppend(true);
                 fa.activateOptions();
                 Logger.getRootLogger().addAppender(fa);
 
             }
 
             Genome genome = Genome.parse(genomeFile);
 
             Map<String,String> map = parseProteinToOLNMappingFile(mapFile);
             List<PeptideSearchResult> peptideSearchResults = new ArrayList<PeptideSearchResult>();
             List<File> searchResultFiles = new ArrayList<File>();
             for (String searchResultsPath : searchResultsPaths)
             {
                 File searchResultFile = new File(searchResultsPath);
                 if (!searchResultFile.exists())
                 {
                     System.err.println(searchResultFile + " does not exist");
                     System.exit(1);
                 }
                 searchResultFiles.add(searchResultFile);
             }
             for (File searchResultFile : searchResultFiles)
             {
                 LOG.debug("Processing: " + searchResultFile.getAbsolutePath());
                 peptideSearchResults.addAll(Samifier.parseMascotPeptideSearchResults(searchResultFile, map));
             }
 
             FileWriter bedWriter = null;
             if (bedfilePath != null)
             {
                 bedWriter = new FileWriter(bedfilePath);
             }
             FileWriter sam = new FileWriter(outfile);
             Samifier.createSAM(genome, map, peptideSearchResults, chromosomeDir, sam, bedWriter);
         }
         catch (ParseException pe)
         {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp("samifier", options, true);
         }
         catch (Exception e)
         {
             System.err.println(e);
             e.printStackTrace();
         }
     }
 }
 
