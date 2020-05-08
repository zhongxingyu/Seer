 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
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
 // utgb-core Project
 //
 // FastqToBAM.java
 // Since: Jul 5, 2010
 //
 //--------------------------------------
 package org.utgenome.format.fastq;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.zip.GZIPInputStream;
 
 import net.sf.samtools.SAMFileHeader;
 import net.sf.samtools.SAMFileWriter;
 import net.sf.samtools.SAMFileWriterFactory;
 import net.sf.samtools.SAMReadGroupRecord;
 import net.sf.samtools.SAMRecord;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 import org.xerial.util.opt.OptionParser;
 import org.xerial.util.opt.OptionParserException;
 
 /**
  * Converting Illumina's FASTQ read data (single or paired-end) into BAM format, which can be used BLOAD's GATK
  * pipeline.
  * 
  * This code is migrated from
  * 
  * @author leo
  * 
  */
 public class FastqToBAM {
 
 	private static Logger _logger = Logger.getLogger(FastqToBAM.class);
 
 	@Option(longName = "readGroup", description = "read group name")
 	private String readGroupName;
 
 	@Option(longName = "sample", description = "sample name")
 	private String sampleName;
 
 	@Option(longName = "prefix", description = "prefix of the read")
 	private String readPrefix;
 
 	@Argument(index = 0, name = "input fastq (.fastq, .fastq.gz)", required = true)
 	private File input1;
 	@Argument(index = 1, name = "input fastq (.fastq, .fastq.gz, when paried-end read)")
 	private File input2;
 
 	@Option(symbol = "o", longName = "output", description = "output file name (.sam or .bam)")
 	private File outputFile;
 
 	public static int execute(String[] args) throws Exception {
 
 		FastqToBAM main = new FastqToBAM();
 		OptionParser parser = new OptionParser(main);
 		try {
 			parser.parse(args);
 
 			main.convert();
 		}
 		catch (OptionParserException e) {
 			_logger.error(e);
 			return 1;
 		}
 
 		return 0;
 
 	}
 
 	public int convert() throws UTGBException, IOException {
 		if (input1 == null) {
 			throw new UTGBException(UTGBErrorCode.MISSING_OPTION, "missing fastq file");
 		}
 
 		Reader in1;
 		if (input1.getName().endsWith(".gz")) {
 			in1 = new InputStreamReader(new GZIPInputStream(new FileInputStream(input1)));
 		}
 		else
 			in1 = new FileReader(input1);
 
 		Reader in2 = null;
 		if (input2 != null) {
 			if (input2.getName().endsWith(".gz"))
 				in2 = new InputStreamReader(new GZIPInputStream(new FileInputStream(input2)));
 			else
 				in2 = new FileReader(input2);
 		}
 
 		return convert(new BufferedReader(in1), in2 == null ? null : new BufferedReader(in2));
 	}
 
 	public int convert(Reader input1, Reader input2) throws UTGBException, IOException {
 
 		FastqReader end1 = new FastqReader(input1);
 		FastqReader end2 = (input2 == null) ? null : new FastqReader(input2);
 
 		SAMReadGroupRecord readGroupRecord = new SAMReadGroupRecord(readGroupName);
 		SAMFileHeader samHeader = new SAMFileHeader();
 		if (readGroupName != null) {
 			samHeader.addReadGroup(readGroupRecord);
 		}
 		readGroupRecord.setSample(sampleName);
 		samHeader.setSortOrder(SAMFileHeader.SortOrder.queryname);
 
 		if (outputFile == null)
 			throw new UTGBException(UTGBErrorCode.MISSING_OPTION, "no output file is specified by -o option");
 
 		SAMFileWriter sfw = (new SAMFileWriterFactory()).makeSAMOrBAMWriter(samHeader, false, outputFile);
 		int readsSeen = 0;
 
 		try {
 			for (FastqRead fqr1, fqr2 = null; (fqr1 = end1.next()) != null && (end2 == null || (fqr2 = end2.next()) != null);) {
 
 				String fqr1Name = fqr1.seqname;
 
 				SAMRecord sr1 = new SAMRecord(samHeader);
 				sr1.setReadName(readPrefix != null ? (readPrefix + ":" + fqr1Name) : fqr1Name);
 				sr1.setReadString(fqr1.seq);
 				sr1.setBaseQualityString(fqr1.qual);
 				sr1.setReadUnmappedFlag(true);
 				sr1.setReadPairedFlag(false);
 				sr1.setAttribute("RG", readGroupName);
 
 				SAMRecord sr2 = null;
 
 				// paired-end read
 				if (fqr2 != null) {
 					sr1.setReadPairedFlag(true);
 					sr1.setFirstOfPairFlag(true);
 					sr1.setSecondOfPairFlag(false);
 					sr1.setMateUnmappedFlag(true);
 
 					String fqr2Name = fqr2.seqname;
 					sr2 = new SAMRecord(samHeader);
 					sr2.setReadName(readPrefix != null ? (readPrefix + ":" + fqr2Name) : fqr2Name);
 					sr2.setReadString(fqr2.seq);
 					sr2.setBaseQualityString(fqr2.qual);
 					sr2.setReadUnmappedFlag(true);
 					sr2.setReadPairedFlag(true);
 					sr2.setAttribute("RG", readGroupName);
 					sr2.setFirstOfPairFlag(false);
 					sr2.setSecondOfPairFlag(true);
 					sr2.setMateUnmappedFlag(true);
 				}
 
 				sfw.addAlignment(sr1);
 				if (fqr2 != null) {
 					sfw.addAlignment(sr2);
 				}
 				readsSeen++;
 
 				if (readsSeen > 0 && (readsSeen % 100000) == 0) {
 					_logger.info(String.format("%d (paired) reads has been processed.", readsSeen));
 				}
 			}
 		}
		catch (IllegalArgumentException e) {
			_logger.error(String.format("error found in read (%d) : %s", readsSeen, e.getMessage()));
			throw e;
 		}
 		finally {
 			if (end1 != null)
 				end1.close();
 			if (end2 != null)
 				end2.close();
 
 			if (sfw != null)
 				sfw.close();
 		}
 
 		return readsSeen;
 	}
 }
