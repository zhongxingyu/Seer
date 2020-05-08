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
 // ImportBED.java
 // Since: 2011/07/19
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.db;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.Reader;
 
 import org.utgenome.UTGBException;
 import org.utgenome.format.bed.BED2SilkReader;
 import org.utgenome.weaver.GenomeWeaverCommand;
 import org.utgenome.weaver.db.BlockArray.Block;
 import org.xerial.lens.SilkLens;
 import org.xerial.util.ObjectHandler;
 import org.xerial.util.ObjectHandlerBase;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 
 public class ImportBED extends GenomeWeaverCommand
 {
     private static Logger _logger = Logger.getLogger(ImportBED.class);
 
     @Override
     public String getOneLineDescription() {
         return "Import chromatin annotation written in BED format";
     }
 
     @Argument(index = 0)
     private String bedFile;
 
     @Option(symbol = "b", description = "bin size. default = 10000")
     private int    binSize = 10000;
 
     @Override
     public void execute(String[] args) throws Exception {
         if (bedFile == null) {
             throw new UTGBException("no input file is given");
         }
 
         final BlockArrayTable chromatinAnnotation = new BlockArrayTable();
 
         // Prepare binning code: BEDAnnotation object stream -> BinInGenome<BEDAnnotaion>
         final BinSplitter<BEDAnnotation> splitter = new BinSplitter<BEDAnnotation>(binSize,
                 new ObjectHandlerBase<BinInGenome<BEDAnnotation>>() {
                     @Override
                     public void handle(BinInGenome<BEDAnnotation> input) throws Exception {
                         // Handle bins
                         _logger.info("Processing bin %s", input);
 
                        // Block data for scores
                         Block ba = new Block(input.range);
                         for (BEDAnnotation each : input.data()) {
                             for (int x = each.getStart(); x < each.getEnd(); ++x)
                                 ba.set(x - 1, each.score); // Use 0-origin
                         }
                         // Put the block to the table
                         chromatinAnnotation.getBlockArray(input.chr).add(ba);
                     }
                 });
 
        // Load BED file and create priority search trees for each chromosome
         //  BED (0-origin) -> Silk (1-origin) -> BEDAnnotation object stream 
         Reader bedIn = new BED2SilkReader(new BufferedReader(new FileReader(bedFile)));
         try {
             SilkLens.findFromSilk(bedIn, "gene", BEDAnnotation.class, new ObjectHandler<BEDAnnotation>() {
                 int count = 0;
 
                 @Override
                 public void init() throws Exception {
                     _logger.info("Loading BED file: " + bedFile);
                 }
 
                 @Override
                 public void handle(BEDAnnotation input) throws Exception {
                     // Send the input entry to the bin splitter
                     splitter.handle(input);
                     count++;
                     if (count % 10000 == 0) {
                         _logger.info("Loaded %,d entries", count);
                     }
                 }
 
                 @Override
                 public void finish() throws Exception {
                     splitter.finish(); // dump the remaining entries
                     _logger.info("Loaded %,d entries", count);
                 }
             });
         }
         finally {
             bedIn.close();
         }
 
         // Save the block array table to a file
         File out = new File(bedFile + ".bin");
         _logger.info("Save to %s", out);
         chromatinAnnotation.saveTo(out);
 
         // You can load the binary data as follows: 
         // BlockArrayTable loadedData = BlockArrayTable.loadFrom(new File(".bed.bin"));
 
         // Query chromatin state data
         for (String chr : chromatinAnnotation.keySet()) {
             _logger.info(chr);
             BlockArray ba = chromatinAnnotation.getBlockArray(chr);
             int max = ba.getMaxLength();
             for (int x = 0; x < max; ++x) {
                 _logger.info(ba.get(x));
             }
         }
 
     }
 }
