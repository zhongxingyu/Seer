 /*
   Copyright (c) 2010 Sascha Steinbiss <steinbiss@zbh.uni-hamburg.de>
   Copyright (c) 2010 Center for Bioinformatics, University of Hamburg
 
   Permission to use, copy, modify, and distribute this software for any
   purpose with or without fee is hereby granted, provided that the above
   copyright notice and this permission notice appear in all copies.
 
   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
 
 
 package extended;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import core.GTerrorJava;
 
 public class GFF3InStreamTest {
   static File tmpfile;
   static String gffcontent = "##gff-version 3\n"
       + "##sequence-region 1 1 500\n"
       + "1\tucb\tgene\t100\t200\t.\t-\t.\tID=geneA\n"
       + "1\tucb\tgene\t300\t400\t.\t+\t.\tID=geneB";
 
   @BeforeClass
   public static void setUpBeforeClass() throws IOException {
     tmpfile = File.createTempFile("gff3test_temp", ".gff3");
     tmpfile.deleteOnExit();
     BufferedWriter out = new BufferedWriter(new FileWriter(tmpfile));
     out.write(gffcontent);
     out.close();
   }

   @Test
   public void test_in_stream() throws IOException, GTerrorJava {
     GFF3InStream i = new GFF3InStream(tmpfile.getCanonicalPath());
     GenomeNode n;
     ArrayList<GenomeNode> result_nodes = new ArrayList<GenomeNode>();
     while ((n = i.next_tree()) != null) {
       result_nodes.add(n);
     }
    assertTrue(result_nodes.size() == 3);

     i.dispose();
     for(int j=0; j < result_nodes.size(); j++){
         result_nodes.get(j).dispose();
      }
   }
 
   @Test(expected=IOException.class)
   public void test_in_stream_failure_nonexistant_file() throws IOException {
     new GFF3InStream("");
   }
 }
