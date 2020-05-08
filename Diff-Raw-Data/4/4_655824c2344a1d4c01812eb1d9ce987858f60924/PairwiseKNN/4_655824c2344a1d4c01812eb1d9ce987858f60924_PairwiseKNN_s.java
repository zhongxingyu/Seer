 /*
  * Copyright (C) 2012 Jordan Fish <fishjord at msu.edu>
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
 package edu.msu.cme.rdp.alignment.pairwise;
 
 import edu.msu.cme.rdp.alignment.AlignmentMode;
 import edu.msu.cme.rdp.alignment.pairwise.rna.DistanceModel;
 import edu.msu.cme.rdp.alignment.pairwise.rna.IdentityDistanceModel;
 import edu.msu.cme.rdp.readseq.SequenceType;
 import edu.msu.cme.rdp.readseq.readers.SeqReader;
 import edu.msu.cme.rdp.readseq.readers.Sequence;
 import edu.msu.cme.rdp.readseq.readers.SequenceReader;
 import edu.msu.cme.rdp.readseq.utils.IUBUtilities;
 import edu.msu.cme.rdp.readseq.utils.SeqUtils;
 import java.io.File;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 /**
  *
  * @author Jordan Fish <fishjord at msu.edu>
  */
 public class PairwiseKNN {
 
     public static class Neighbor {
 
         PairwiseAlignment alignment;
         boolean reverse;
         Sequence dbSeq;
     }
 
     private static <T> void insert(T n, List<T> list, Comparator<T> comp, int k) {
         int i = list.size();
         list.add(n);
 
         while (i > 0 && comp.compare(list.get(i), list.get(i - 1)) > 0) {
             Collections.swap(list, i, i - 1);
             i--;
         }
 
        if (list.size() >= k) {
            list.remove(k - 1);
         }
     }
 
     public static List<Neighbor> getKNN(Sequence query, List<Sequence> dbSeqs, AlignmentMode mode, int k) {
         List<Neighbor> ret = new ArrayList();
         Neighbor n;
         Comparator c = new Comparator<Neighbor>() {
             public int compare(Neighbor t, Neighbor t1) {
                 return t.alignment.getScore() - t1.alignment.getScore();
             }
         };
 
         SequenceType seqType = SeqUtils.guessSequenceType(query);
         ScoringMatrix matrix;
         if (seqType == SequenceType.Nucleotide) {
             matrix = ScoringMatrix.getDefaultNuclMatrix();
         } else {
             matrix = ScoringMatrix.getDefaultProteinMatrix();
         }
             matrix = ScoringMatrix.getDefaultProteinMatrix();
 
         for (Sequence dbSeq : dbSeqs) {
             n = new Neighbor();
             n.dbSeq = dbSeq;
             PairwiseAlignment fwd = PairwiseAligner.align(dbSeq.getSeqString(), query.getSeqString(), matrix, mode);
             if (seqType == SequenceType.Nucleotide) {
                 PairwiseAlignment rc = PairwiseAligner.align(dbSeq.getSeqString(), IUBUtilities.reverseComplement(query.getSeqString()), matrix, mode);
 
                 if (rc.getScore() > fwd.getScore()) {
                     n.alignment = rc;
                     n.reverse = true;
                 } else {
                     n.alignment = fwd;
                     n.reverse = false;
                 }
             } else {
                 n.alignment = fwd;
                 n.reverse = false;
             }
 
             insert(n, ret, c, k);
         }
 
         return ret;
     }
 
     /*public static void main(String[] args) {
      List<Integer> list = new ArrayList();
 
      list.add(10);
      list.add(8);
      list.add(7);
      list.add(6);
 
      Comparator<Integer> comp = new Comparator<Integer>() {
 
      public int compare(Integer t, Integer t1) {
      return t - t1;
      }
 
      };
 
      System.out.println(list);
      insert(11, list, comp, 5);
      System.out.println(list);
      insert(9, list, comp, 5);
      System.out.println(list);
      }*/
     public static void main(String[] args) throws Exception {
         SeqReader queryReader;
         List<Sequence> dbSeqs;
         AlignmentMode mode = AlignmentMode.glocal;
         int k = 1;
         PrintStream out = new PrintStream(System.out);
 
         Options options = new Options();
         options.addOption("m", "mode", true, "Alignment mode {global, glocal, local, overlap, overlap_trimmed} (default= glocal)");
         options.addOption("k", true, "K-nearest neighbors to return");
         options.addOption("o", "out", true, "Redirect output to file instead of stdout");
 
 
         try {
             CommandLine line = new PosixParser().parse(options, args);
 
             if (line.hasOption("mode")) {
                 mode = AlignmentMode.valueOf(line.getOptionValue("mode"));
             }
 
             if (line.hasOption('k')) {
                 k = Integer.valueOf(line.getOptionValue('k'));
             }
 
             if (line.hasOption("out")) {
                 out = new PrintStream(line.getOptionValue("out"));
             }
 
             args = line.getArgs();
 
             if (args.length != 2) {
                 throw new Exception("Unexpected number of command line arguments");
             }
 
             queryReader = new SequenceReader(new File(args[0]));
             dbSeqs = SequenceReader.readFully(new File(args[1]));
 
         } catch (Exception e) {
             new HelpFormatter().printHelp("PairwiseKNN <options> <queryFile> <dbFile>", options);
             System.err.println("ERROR: " + e.getMessage());
             return;
         }
 
         DistanceModel dist = new IdentityDistanceModel();
 
         out.println("#query file: " + args[0] + " db file: " + args[1] + " k: " + k + " mode: " + mode);
         out.println("#seqname\tk\tref seqid\tref desc\torientation\tscore\tident\tquery start\tquery end\tquery length\tref start\tref end");
         Sequence seq;
         List<Neighbor> alignments;
         Neighbor n;
         PairwiseAlignment alignment;
         while ((seq = queryReader.readNextSequence()) != null) {
             alignments = getKNN(seq, dbSeqs, mode, k);
 
             for (int index = 0; index < alignments.size(); index++) {
                 n = alignments.get(index);
                 alignment = n.alignment;
                 double ident = 1 - dist.getDistance(alignment.getAlignedSeqi().getBytes(), alignment.getAlignedSeqj().getBytes(), 0);
 
                 out.println("@" + seq.getSeqName()
                         + "\t" + (index + 1)
                         + "\t" + n.dbSeq.getSeqName()
                         + "\t" + n.dbSeq.getDesc()
                         + "\t" + (n.reverse ? "-" : "+")
                         + "\t" + alignment.getScore()
                         + "\t" + ident
                         + "\t" + alignment.getStartj()
                         + "\t" + alignment.getEndj()
                         + "\t" + seq.getSeqString().length()
                         + "\t" + alignment.getStarti()
                         + "\t" + alignment.getEndi());
 
                 out.println(">" + alignment.getAlignedSeqj());
                 out.println(">" + alignment.getAlignedSeqi());
             }
         }
 
     }
 }
