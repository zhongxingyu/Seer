 package org.broadinstitute.sting.gatk.walkers.genotyper;
 
 import org.broadinstitute.sting.utils.BaseUtils;
 
 import static java.lang.Math.log10;
 import java.util.TreeMap;
 import java.util.EnumMap;
 
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMReadGroupRecord;
 
 public class EmpiricalSubstitutionProbabilities extends FourBaseProbabilities {
     // --------------------------------------------------------------------------------------------------------------
     //
     // Static methods to manipulate machine platforms
     //
     // --------------------------------------------------------------------------------------------------------------
     public enum SequencerPlatform {
         SOLEXA,         // Solexa / Illumina
         ROCHE454,       // 454
         SOLID,          // SOLiD
         UNKNOWN         // No idea -- defaulting to 1/3
     }
 
     private static TreeMap<String, SequencerPlatform> PLFieldToSequencerPlatform = new TreeMap<String, SequencerPlatform>();
     private static void bind(String s, SequencerPlatform x) {
         PLFieldToSequencerPlatform.put(s, x);
         PLFieldToSequencerPlatform.put(s.toUpperCase(), x);
         PLFieldToSequencerPlatform.put(s.toLowerCase(), x);
     }
 
     //
     // Static list of platforms supported by this system.
     //
     static {
         bind("LS454", SequencerPlatform.ROCHE454);
         bind("454", SequencerPlatform.ROCHE454);
         bind("ILLUMINA", SequencerPlatform.SOLEXA);
         bind("solid", SequencerPlatform.SOLID);
     }
 
     public static SequencerPlatform standardizeSequencerPlatform( final String sequencerString ) {
         String lcSequencerString = sequencerString == null ? null : sequencerString.toLowerCase();
         if ( sequencerString != null && PLFieldToSequencerPlatform.containsKey(lcSequencerString) ) {
             return PLFieldToSequencerPlatform.get(lcSequencerString);
         } else {
             return SequencerPlatform.UNKNOWN;
         }
     }
 
     private static ThreadLocal<SAMRecord> lastReadForPL = new ThreadLocal<SAMRecord>();
     private static ThreadLocal<SequencerPlatform> plOfLastRead = new ThreadLocal<SequencerPlatform>();
     public static SequencerPlatform getReadSequencerPlatform( SAMRecord read ) {
         if ( lastReadForPL.get() != read ) {
             lastReadForPL.set(read);
             SAMReadGroupRecord readGroup = read.getReadGroup();
             final String platformName = readGroup == null ? null : readGroup.getPlatform();
             plOfLastRead.set(standardizeSequencerPlatform(platformName));
         }
 
         return plOfLastRead.get();
     }
 
     public int getReadSequencerPlatformIndex( SAMRecord read ) {
         return getReadSequencerPlatform(read).ordinal();
     }
 
     // --------------------------------------------------------------------------------------------------------------
     //
     // Static methods to get at the transition tables themselves
     //
     // --------------------------------------------------------------------------------------------------------------
 
     /**
      * A matrix of value i x j -> log10(p) where
      *
      *  i      - char of the miscalled base (i.e., A)
      *  j      - char of the presumed true base (i.e., C)
      *  log10p - empirical probability p that A is actually C
      *
      * The table is available for each technology
      */
     private final static EnumMap<SequencerPlatform, double[][]> log10pTrueGivenMiscall = new EnumMap<SequencerPlatform, double[][]>(SequencerPlatform.class);
 
     private static void addMisCall(final SequencerPlatform pl, char miscalledBase, char trueBase, double p) {
         if ( ! log10pTrueGivenMiscall.containsKey(pl) )
             log10pTrueGivenMiscall.put(pl, new double[4][4]);
 
         double[][] misCallProbs = log10pTrueGivenMiscall.get(pl);
         int i = BaseUtils.simpleBaseToBaseIndex(miscalledBase);
         int j = BaseUtils.simpleBaseToBaseIndex(trueBase);
         misCallProbs[i][j] = log10(p);
     }
 
     private static double getProbMiscallIsBase(SequencerPlatform pl, char miscalledBase, char trueBase) {
         int i = BaseUtils.simpleBaseToBaseIndex(miscalledBase);
         int j = BaseUtils.simpleBaseToBaseIndex(trueBase);
 
         double logP = log10pTrueGivenMiscall.get(pl)[i][j];
         if ( logP == 0.0 )
             throw new RuntimeException(String.format("Bad miscall base request miscalled=%c true=%b", miscalledBase, trueBase));
         else
             return logP;
     }
 
     private static void addSolexa() {
         SequencerPlatform pl = SequencerPlatform.SOLEXA;
         addMisCall(pl, 'A', 'C', 57.7/100.0);
         addMisCall(pl, 'A', 'G', 17.1/100.0);
         addMisCall(pl, 'A', 'T', 25.2/100.0);
 
         addMisCall(pl, 'C', 'A', 34.9/100.0);
         addMisCall(pl, 'C', 'G', 11.3/100.0);
         addMisCall(pl, 'C', 'T', 53.9/100.0);
 
         addMisCall(pl, 'G', 'A', 31.9/100.0);
         addMisCall(pl, 'G', 'C',  5.1/100.0);
         addMisCall(pl, 'G', 'T', 63.0/100.0);
 
         addMisCall(pl, 'T', 'A', 45.8/100.0);
         addMisCall(pl, 'T', 'C', 22.1/100.0);
         addMisCall(pl, 'T', 'G', 32.0/100.0);
     }
 
     private static void addSOLiD() {
         SequencerPlatform pl = SequencerPlatform.SOLID;
         addMisCall(pl, 'A', 'C', 18.7/100.0);
         addMisCall(pl, 'A', 'G', 42.5/100.0);
         addMisCall(pl, 'A', 'T', 38.7/100.0);
 
         addMisCall(pl, 'C', 'A', 27.0/100.0);
         addMisCall(pl, 'C', 'G', 18.9/100.0);
         addMisCall(pl, 'C', 'T', 54.1/100.0);
 
         addMisCall(pl, 'G', 'A', 61.0/100.0);
         addMisCall(pl, 'G', 'C', 15.7/100.0);
         addMisCall(pl, 'G', 'T', 23.2/100.0);
 
         addMisCall(pl, 'T', 'A', 40.5/100.0);
         addMisCall(pl, 'T', 'C', 34.3/100.0);
         addMisCall(pl, 'T', 'G', 25.2/100.0);
     }
 
     private static void add454() {
         SequencerPlatform pl = SequencerPlatform.ROCHE454;
         addMisCall(pl, 'A', 'C', 23.2/100.0);
         addMisCall(pl, 'A', 'G', 42.6/100.0);
         addMisCall(pl, 'A', 'T', 34.3/100.0);
 
         addMisCall(pl, 'C', 'A', 19.7/100.0);
         addMisCall(pl, 'C', 'G',  8.4/100.0);
         addMisCall(pl, 'C', 'T', 71.9/100.0);
 
         addMisCall(pl, 'G', 'A', 71.5/100.0);
         addMisCall(pl, 'G', 'C',  6.6/100.0);
         addMisCall(pl, 'G', 'T', 21.9/100.0);
 
         addMisCall(pl, 'T', 'A', 43.8/100.0);
         addMisCall(pl, 'T', 'C', 37.8/100.0);
         addMisCall(pl, 'T', 'G', 18.5/100.0);
     }
 
     private static void addUnknown() {
         SequencerPlatform pl = SequencerPlatform.UNKNOWN;
         for ( char b1 : BaseUtils.BASES ) {
             for ( char b2 : BaseUtils.BASES ) {
                 if ( b1 != b2 )
                     addMisCall(pl, b1, b2, 1.0/3.0);
             }
 
         }
     }
 
     static {
         addSolexa();
         add454();
         addSOLiD();
         addUnknown();
     }
 
 
     // --------------------------------------------------------------------------------------------------------------
     //
     // The actual objects themselves
     //
     // --------------------------------------------------------------------------------------------------------------
     private boolean raiseErrorOnUnknownPlatform = true;
     private SequencerPlatform defaultPlatform = SequencerPlatform.UNKNOWN;
 
     //
     // forwarding constructors -- don't do anything at all
     //
     public EmpiricalSubstitutionProbabilities() { super(); }
 
     public EmpiricalSubstitutionProbabilities(boolean raiseErrorOnUnknownPlatform) {
         super();
         this.raiseErrorOnUnknownPlatform = raiseErrorOnUnknownPlatform;
     }
 
     public EmpiricalSubstitutionProbabilities(SequencerPlatform assumeUnknownPlatformsAreThis) {
         super();
 
         if ( assumeUnknownPlatformsAreThis != null ) {
             raiseErrorOnUnknownPlatform = false;
             defaultPlatform = assumeUnknownPlatformsAreThis;
         }
     }
 
     /**
      * Cloning of the object
      * @return clone
      * @throws CloneNotSupportedException
      */
     protected Object clone() throws CloneNotSupportedException {
         return super.clone();
     }
 
     // -----------------------------------------------------------------------------------------------------------------
     //
     //
     // calculation of p(B|GT)
     //
     //
     // -----------------------------------------------------------------------------------------------------------------
 
     protected double log10PofTrueBaseGivenMiscall(char observedBase, char chromBase, SAMRecord read, int offset) {
         boolean fwdStrand = ! read.getReadNegativeStrandFlag();
         SequencerPlatform pl = getReadSequencerPlatform(read);
 
         if ( pl == SequencerPlatform.UNKNOWN ) {
             if ( raiseErrorOnUnknownPlatform )
                throw new RuntimeException("Unknown Sequencer platform for read " + read.format());
             else {
                 pl = defaultPlatform;
                 //System.out.printf("Using default platform %s", pl);
             }
         }
 
         //System.out.printf("%s for %s%n", pl, read);
 
         double log10p;
         if ( fwdStrand ) {
             log10p = getProbMiscallIsBase(pl, observedBase, chromBase);
         } else {
             log10p = getProbMiscallIsBase(pl, BaseUtils.simpleComplement(observedBase), BaseUtils.simpleComplement(chromBase));
         }
 
         //System.out.printf("p = %f for %s %c %c fwd=%b %d at %s%n", pow(10,log10p), pl, observedBase, chromBase, fwdStrand, offset, read.getReadName() );
 
         return log10p;
     }
}
