 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // genome-weaver Project
 //
 // ConsensusBuilder.java
 // Since: 2011/09/07
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.assembly;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.List;
 
 import org.utgenome.weaver.GenomeWeaverCommand;
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Option;
 
 public class ConsensusBuilder extends GenomeWeaverCommand
 {
     private static Logger _logger = Logger.getLogger(ConsensusBuilder.class);
 
     @Override
     public String getOneLineDescription() {
         return "Build a consensus from a variation frequency data";
     }
 
     @Option(symbol = "r", description = "reference fasata file")
     private String reference;
 
     @Option(symbol = "v", description = "variation data")
     private String freqFile;
 
     @Option(symbol = "o", description = "output fasta file")
     private String outFASTA = "out.fa";
 
     @Override
     public void execute(String[] args) throws Exception {
 
         _logger.info("loading %s", freqFile);
         List<VariationData> var = VariationData.parse(new File(freqFile));
 
         _logger.info("loading %s", reference);
         FASTA ref = FASTA.load(new File(reference));
 
         _logger.info("Building consensus");
         for (VariationData v : var) {
             ACGTSequence seq = ref.getACGTSequence(v.chr);
             if (seq == null)
                 continue;
 
             if (v.isSinglePointMutation()) {
                 // use 0-origin
                seq.set(v.start - 1, ACGT.encode(v.nonRefAllele.charAt(0)));
             }
         }
 
         _logger.info("Output a new FASTA file: %s", outFASTA);
         BufferedWriter out = new BufferedWriter(new FileWriter(outFASTA), 4 * 1024 * 1024);
         ref.toFASTA(out);
         out.close();
     }
 
 }
