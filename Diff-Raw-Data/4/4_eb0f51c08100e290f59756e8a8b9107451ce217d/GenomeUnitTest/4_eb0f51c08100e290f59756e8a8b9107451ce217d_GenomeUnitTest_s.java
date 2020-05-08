 package au.org.intersect.samifier;
 
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.Test;
 import org.junit.After;
 import org.junit.Before;
 
 import java.io.File;
 
 /**
  * * Tests {@link Genome}
  * */
 public final class GenomeUnitTest
 {
     private File genomeFile = null;
 
     @Before
     public void oneTimeSetup()
     {
       genomeFile = new File(getClass().getResource("/test_genome.gff").getFile());
     }
 
     @Test
     public void testParsing() 
         throws Exception
     {
       Genome genome = Genome.parse(genomeFile);
      assertEquals("Genome has 2 genes", 2, genome.getGenes().size());
       assertTrue("Genome has gene YAL038W", genome.hasGene("YAL038W"));
       assertTrue("Genome has gene YAL038W", genome.hasGene("YDL075W"));
     }
 
     @Test
     public void testGetGene()
         throws Exception
     {
       //GeneInfo gene = new GeneInfo()
       Genome genome = Genome.parse(genomeFile);
       assertThat("Genome returns GeneInfo object for known genes", genome.getGene("YAL038W"), instanceOf(GeneInfo.class));
       assertNull("Genome returns null for unknown genes", genome.getGene("UNKNOWN"));
     }
 }
