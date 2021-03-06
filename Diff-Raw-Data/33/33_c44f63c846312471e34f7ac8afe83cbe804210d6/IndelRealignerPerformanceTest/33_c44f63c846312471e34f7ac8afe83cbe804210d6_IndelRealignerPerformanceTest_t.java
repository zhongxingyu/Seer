 package org.broadinstitute.sting.gatk.walkers.indels;
 
 import org.broadinstitute.sting.WalkerTest;
 import org.junit.Test;
 
 import java.util.ArrayList;
 
 public class IndelRealignerPerformanceTest extends WalkerTest {
     @Test
     public void testRealigner() {
         WalkerTestSpec spec1 = new WalkerTestSpec(
 
                 "-R " + seqLocation + "references/Homo_sapiens_assembly18/v0/Homo_sapiens_assembly18.fasta" +
                         " -T IndelRealigner" +
                         " -LOD 5" +
                         " -maxConsensuses 100" +
                         " -greedy 100" +
                         " -D /humgen/gsa-hpprojects/GATK/data/dbsnp_129_hg18.rod" +
                         " -I " + evaluationDataLocation + "NA12878.GAII.chr1.50MB.bam" +
                         " -L chr1:1-5,650,000" +
                         " -compress 1" +
                         " -sort NO_SORT" +
                         " -targetIntervals " + evaluationDataLocation + "NA12878.GAII.chr1.50MB.realigner.intervals" +
                         " -O /dev/null",
                  0,
                 new ArrayList<String>(0));
         try {
             executeTest("testRealignerTargetCreatorWholeGenome", spec1);
        } catch (RuntimeException e) {
             // using /dev/null as an output source causes samtools to fail when it closes the stream, we shouldn't sweat it         
         }
         WalkerTestSpec spec2 = new WalkerTestSpec(
                 "-R " + seqLocation + "references/Homo_sapiens_assembly18/v0/Homo_sapiens_assembly18.fasta" +
                         " -T IndelRealigner" +
                         " -LOD 5" +
                         " -maxConsensuses 100" +
                         " -greedy 100" +
                         " -D /humgen/gsa-hpprojects/GATK/data/dbsnp_129_hg18.rod" +
                         " -I " + evaluationDataLocation + "NA12878.ESP.WEx.chr1.bam" +
                         " -L chr1:1-150,000,000" +
                         " -compress 1" +
                         " -sort NO_SORT" +
                         " -targetIntervals " + evaluationDataLocation + "NA12878.ESP.WEx.chr1.realigner.intervals" +
                         " -O /dev/null",
                  0,
                 new ArrayList<String>(0));
         try {
             executeTest("testRealignerTargetCreatorWholeExome", spec2);
        } catch (RuntimeException e) {
             // using /dev/null as an output source causes samtools to fail when it closes the stream, we shouldn't sweat it
         }
     }
 }
