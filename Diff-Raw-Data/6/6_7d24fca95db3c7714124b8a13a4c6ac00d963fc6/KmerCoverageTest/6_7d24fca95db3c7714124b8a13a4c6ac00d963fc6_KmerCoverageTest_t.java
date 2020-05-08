 /*
  * Copyright (C) 2014 wangqion
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.msu.cme.rdp.kmer.cli;
 
 import edu.msu.cme.rdp.kmer.cli.KmerCoverage.Contig;
 import edu.msu.cme.rdp.readseq.readers.SequenceReader;
 import edu.msu.cme.rdp.readseq.readers.core.SeqReaderCore;
 import edu.msu.cme.rdp.readseq.utils.SeqUtils;
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.util.concurrent.ConcurrentHashMap;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author wangqion
  */
 public class KmerCoverageTest {
     
     public KmerCoverageTest() {
     }
 
 
     /**
      * Test of printCovereage method, of class KmerCoverage.
      */
     @Test
     public void testGetContigMap() throws Exception {
         System.out.println("getContigMap");
         int kmer_size = 30;
         String contigs = ">contig1\n" +
         "ccccacgagcaggcgaccaagcaaggcccgaagatcatggaattccggctcacggttga\n" +
         ">contig2\n" +
         "caggcgaccaagcaaggcccgaagatcatggaattccggctcacggttgaggagaagcgaattgtcatcgacgacatgg\n" +
         ">contig3\n" +
         "caggcgaccaagcaaggcccgaagatcatggaattccggctcacggttgagN\n";
         
         String reads = ">r1\n" +
         "ccccacgagcaggcgaccaagcaaggcccgaagatcatggaattccggctcacggttgaggagaagcgaattgtcatcgacgacatgg\n" +
         ">r2 reverse of r1 \n" +
         "CCATGTCGTCGATGACAATTCGCTTCTCCTCAACCGTGAGCCGGAATTCCATGATCTTCGGGCCTTGCTTGGTCGCCTGCTCGTGGGG\n" +
         ">r3 one mismatch\n" +
         "ccccacgagcaggcgaccaagcaaggcccgNagatcatggaattccggctcacggttgaggC";
                 
         SeqReaderCore readsReader = SeqUtils.getSeqReaderCore(new BufferedInputStream(new ByteArrayInputStream(reads.getBytes())));
         SequenceReader contigReader = new SequenceReader(new BufferedInputStream(new ByteArrayInputStream(contigs.getBytes())));
         
         ByteArrayOutputStream coverage_out = new ByteArrayOutputStream();
         ByteArrayOutputStream abundance_out = new ByteArrayOutputStream();
         KmerCoverage instance = new KmerCoverage(kmer_size, contigReader, readsReader, null);
         ConcurrentHashMap<Integer, Contig> contigMap = instance.getContigMap();
         // contig 1 should have full coverage
         
         Contig contig1 = contigMap.get(0);
         assertEquals(contig1.name, "contig1");
         // offset from 1,  
         assertEquals(contig1.coverage[0], 3, 0.005);
         assertEquals(contig1.coverage[8], 2, 0.005);
         assertEquals(contig1.coverage[9], 0.6666, 0.005);
         assertEquals(contig1.coverage[29], 0.6666, 0.005);
         
         
         //contig2 is the reverse complement of contig1, should have the same coverage
         Contig contig2 = contigMap.get(1);
         assertEquals(contig2.name, "contig2");
         // offset from 1, 
         assertEquals(contig2.coverage[0], 0.6666, 0.005);
         assertEquals(contig2.coverage[20], 0.6666, 0.005);
         assertEquals(contig2.coverage[21], 1, 0.005);
         assertEquals(contig2.coverage[22], 3, 0.005);
         assertEquals(contig2.coverage[23], 2, 0.005);
         assertEquals(contig2.coverage[49], 2, 0.005);
        
         
         //contig3 has a few mismatches
         
         Contig contig3 = contigMap.get(2);
         assertEquals(contig3.name, "contig3");
         //offset from 1,
         assertEquals(contig3.coverage[0], 0.6666, 0.005);
         assertEquals(contig3.coverage[21], 1, 0.005);
         assertEquals(contig3.coverage[22], 0, 0.005);
 
         
         // check summary
         instance.printCovereage(coverage_out, abundance_out);
         String[] summary = coverage_out.toString().split("\n");
        assertEquals("contig1\t1.100\t0.667\t30\t30\t1.000", summary[3]);
        assertEquals("contig3\t0.652\t0.667\t23\t22\t0.957", summary[4]);
        assertEquals("contig2\t1.440\t2.000\t50\t50\t1.000", summary[5]);
         
     }
 
    
 }
