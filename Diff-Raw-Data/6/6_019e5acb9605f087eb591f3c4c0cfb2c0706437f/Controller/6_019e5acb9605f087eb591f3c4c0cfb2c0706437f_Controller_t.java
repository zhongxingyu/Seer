 package controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import model.GCContentInfo;
 import model.GFFParser;
 import model.GFFParser.ParseException;
 import model.Gene;
 import model.GeneIsoform;
 import model.GeneUtils;
 import model.Sequence;
 import suffixtree.SuffixTree;
 import suffixtree.SuffixTree.RepeatEntry;
 import suffixtree.SuffixTree.StartEntry;
 import suffixtree.SuffixTreeUtils;
 
 /**
  * The Controller
  * 
  * @author MitchellRosen
  * @version 21-Apr-2013
  */
 public class Controller {
    protected String     mSequenceFile;
    protected String     mGffFile;
 
    protected Sequence   mSequence;
    protected List<Gene> mGenes;
 
    public Controller() {
       mSequenceFile = new String();
       mGffFile = new String();
    }
 
    public void useSequenceFile(String filename) throws IOException,
          IllegalArgumentException {
       if (!mSequenceFile.equals(filename)) {
          mSequenceFile = filename;
          mSequence = new Sequence(mSequenceFile);
          if (mGenes != null) {
             for (Gene gene : mGenes) {
                gene.setSequence(mSequence);
             }
          }
       }
    }
 
    public void useGffFile(String filename) throws IOException, ParseException {
       if (!mGffFile.equals(filename)) {
          mGffFile = filename;
 
          GFFParser parser = new GFFParser(filename);
          mGenes = parser.parse();
          if (mSequence != null) {
             for (Gene gene : mGenes)
                gene.setSequence(mSequence);
          }
       }
 
    }
 
    /**
     * Called when "run" is pressed in the GC content tab. Assumes the file has
     * already been set.
     * 
     * @param startPos
     *           The start position in the file to process. Indexed inclusively
     *           from 1. If the string is empty, we index from the start of file.
     * @param endPos
     *           The end position in the file to process. Indexed inclusively
     *           from 1. (Or exclusively from 0). If empty, we set to end of
     *           file.
     * @param useSlidingWindow
     *           True if we are using sliding window logic.
     * @param winSize
     *           A string containing the size of individual windows to use.
     * @param shiftIncr
     *           The amount to shift the window in our sliding window protocol.
     * @return A string representing information about a nucleotide sequence, or
     *         an appropriate error message detailing why one could not be
     *         provided.
     */
    public String getGcContent(String startPos, String endPos,
          boolean useSlidingWindow, String winSize, String shiftIncr) {
       StringBuilder sb = new StringBuilder();
       sb.append("Start, stop, min %, max %\n");
 
       if (startPos.isEmpty())
          startPos = "1";
 
       if (endPos.isEmpty())
          endPos = Integer.toString(mSequence.size());
 
       mSequence = mSequence.slice(Integer.parseInt(startPos) - 1,
             Integer.parseInt(endPos));
 
       if (useSlidingWindow) {
          GCContentInfo[] gcs = mSequence.gcContentHistogram(
                Integer.parseInt(winSize), Integer.parseInt(shiftIncr));
 
          for (GCContentInfo gc : gcs)
             sb.append(gc + "\n");
       } else {
          sb.append(String.format("%5d, %5d, %5.2f%%, %5.2f%%",
                Integer.parseInt(startPos), Integer.parseInt(endPos),
                mSequence.gcContentMin() * 100, mSequence.gcContentMax() * 100));
       }
 
       return sb.toString();
    }
 
    public String getNucleotides() {
       return String.format("%d", mSequence.size());
    }
 
    public String getGenes() {
       return String.format("%d", mGenes.size());
    }
 
    public String getIsoforms() {
       return String.format("%d", GeneUtils.numIsoforms(mGenes));
    }
 
    public String avgGeneSize() {
       return String.format("%.2f", GeneUtils.avgGeneSize(mGenes));
    }
 
    public String avgCdsSize() {
       return String.format("%.2f", GeneUtils.avgCdsSize(mGenes));
    }
 
    public String avgExonSize() {
       return String.format("%.2f", GeneUtils.avgExonSize(mGenes));
    }
 
    public String avgIntronSize() {
       return String.format("%.2f", GeneUtils.avgIntronSize(mGenes));
    }
 
    public String avgIntergenicRegionSize() {
       return String.format("%.2f", GeneUtils.avgIntergenicRegionSize(mGenes));
    }
 
    public String geneDensity() {
       return String.format("%.2f", GeneUtils.geneDensity(mGenes));
    }
 
    public String cdsDensity() {
       return String.format("%.2f", GeneUtils.cdsDensity(mGenes));
    }
 
    public String genesPerKilobase() {
       return String.format("%.2f", GeneUtils.genesPerKilobase(mGenes));
    }
 
    public String kilobasesPerGene() {
       return String.format("%.2f", GeneUtils.kilobasesPerGene(mGenes));
    }
 
    public String getProteins() {
       StringBuilder sb = new StringBuilder("gene name, isoform name, protein\n");
 
       for (Gene g : mGenes) {
          String geneName = g.getId();
          for (GeneIsoform iso : g.getIsoforms()) {
             sb.append(geneName + ", ");
             sb.append(iso.getTranscriptId() + ", ");
             if (iso.isReverse()) {
                sb.append(iso.getSequence().reverseComplement()
                      .toProteinString()
                      + "\n");
             } else {
                sb.append(iso.getSequence().toProteinString() + "\n");
             }
          }
       }
 
       return sb.toString();
    }
 
    /**
    * Find all repeats.
     * 
     * @param minimumRepeatLength
     *           The minimum length of a repeat.
     * @param maxDistanceFromMRNAStart
     *           The maximum distance from the next mRNA start. If negative, all
     *           occurrences will be included regardless of distance from mRNA
     *           start.
     * @return Returns formatted output representing all repeated strings within
     *         the sequence.
     */
    public String getRepeats(int minimumRepeatLength,
          int maxDistanceFromMRNAStart) {
       StringBuilder matchInfo = new StringBuilder();
       SuffixTreeUtils treeUtil = new SuffixTreeUtils(mSequence, mGenes);
 
       SuffixTree tree = SuffixTree.create(mSequence.toString());
       List<RepeatEntry> repeats = tree.findRepeats(minimumRepeatLength);
       matchInfo.append("Repeated sequence,Frequency,Fold Expression,Average"
             + " Distance From (+) mRNA Start,Average Distance From (-)"
             + " mRNA Start,Coordinates\n");
       for (RepeatEntry repeat : repeats) {
          List<StartEntry> occurences = repeat.getStarts();
          if (maxDistanceFromMRNAStart >= 0) {
             treeUtil.stripStartsOutsideRange(maxDistanceFromMRNAStart,
                   occurences, repeat.toString().length());
          }
 
          matchInfo.append(repeat + ",");
          matchInfo.append(repeat.getStarts().size() + ",");
          matchInfo.append(repeat.getStarts().size()
                / treeUtil.findExpectedFoldExpression(repeat.toString()) + ",");
          matchInfo.append(treeUtil.averageDistanceToNextPositiveMRNA(repeat
                .getStarts()) + ",");
          matchInfo.append(treeUtil.averageDistanceToNextNegativeMRNA(repeat
                .getStarts()));
 
          for (StartEntry occurence : occurences) {
             // Represent as 1-indexed for ease of bio students.
             matchInfo.append("," + (occurence.start + 1));
          }
          matchInfo.append("\n");
       }
       return matchInfo.toString();
    }
 
    /**
     * Find all occurrences of the given search string.
     * 
     * @param searchString
     *           The string to be searched for.
     * @param maxDistanceFromMRNAStart
     *           The maximum distance from the next mRNA start. If negative, all
     *           occurrences will be included regardless of distance from mRNA
     *           start. <Currently excluded from calculations!>
     * @return Returns formatted output representing all repeated strings within
     *         the sequence.
     */
    public String matchString(String searchString, int maxDistanceFromMRNAStart) {
       SuffixTreeUtils treeUtil = new SuffixTreeUtils(mSequence, mGenes);
 
       String reverseSearchString = SuffixTreeUtils
             .reverseComplement(searchString);
 
       SuffixTree tree = SuffixTree.create(mSequence.toString());
 
       List<StartEntry> occurences = tree.getOccurrences(searchString);
       List<StartEntry> revOccurences = tree.getOccurrences(reverseSearchString);
 
       if (maxDistanceFromMRNAStart >= 0) {
          treeUtil.stripStartsOutsideRange(maxDistanceFromMRNAStart, occurences,
                searchString.length());
          treeUtil.stripStartsOutsideRange(maxDistanceFromMRNAStart,
                revOccurences, searchString.length());
       }
 
       StringBuilder matchInfo = new StringBuilder();
 
       int absoluteFrequency = occurences.size();
       int reverseAbsoluteFrequency = revOccurences.size();
 
       double averageDistance = treeUtil
             .averageDistanceToNextPositiveMRNA(occurences);
       double averageDistanceNegative = treeUtil
             .averageDistanceToNextNegativeMRNA(occurences);
 
       double reverseAverageDistance = treeUtil
             .averageDistanceToNextPositiveMRNA(revOccurences);
       double reverseAverageDistanceNegative = treeUtil
             .averageDistanceToNextNegativeMRNA(revOccurences);
 
       double expectedFoldExpression = treeUtil
             .findExpectedFoldExpression(searchString);
       double reverseExpectedFoldExpression = treeUtil
             .findExpectedFoldExpression(reverseSearchString);
 
       double relativeFoldExpression = absoluteFrequency
             / expectedFoldExpression;
       double reverseRelativeFoldExpression = reverseAbsoluteFrequency
             / reverseExpectedFoldExpression;
       matchInfo.append("Repeated sequence,Frequency,Fold Expression,Average"
             + " Distance From (+) mRNA Start,Average Distance From (-)"
             + " mRNA Start,Coordinates\n");
 
       matchInfo.append(searchString + "," + absoluteFrequency + ","
             + relativeFoldExpression + "," + averageDistance + ","
             + averageDistanceNegative);
       for (StartEntry occurance : occurences) {
         // Represent as 1-indexed for ease of bio students.
         matchInfo.append("," + (occurance.start + 1));
       }
 
       matchInfo.append("\n" + reverseSearchString + ","
             + reverseAbsoluteFrequency + "," + reverseRelativeFoldExpression
             + "," + reverseAverageDistance + ","
             + reverseAverageDistanceNegative);
       for (StartEntry occurance : revOccurences) {
          // Represent as 1-indexed for ease of bio students.
          matchInfo.append("," + (occurance.start + 1));
       }
       return matchInfo.toString();
    }
 }
