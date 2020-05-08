 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
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
 // SAMReaderTest.java
 // Since: Dec 25, 2009
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.format.sam;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.xerial.util.FileResource;
 import org.xerial.util.FileUtil;
 import org.xerial.util.log.Logger;
 
 public class BAMReaderTest {
 	private static Logger _logger = Logger.getLogger(BAMReaderTest.class);
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testRead() throws Exception {
 		_logger.info("read test");
 		SAMFileReader reader = new SAMFileReader(FileResource.find(BAMReaderTest.class, "bss-align-sorted.bam").openStream());
 		for (SAMRecord each : reader)
 			_logger.info(each.format());
 	}
 
 	@Test
 	public void queryTest() throws Exception {
 		_logger.info("query test");
 		ArrayList<SAMRead> readDataList = new ArrayList<SAMRead>();
 
 		File temp_bam = FileUtil.createTempFile(new File("target"), "sample", ".bam");
 		FileUtil.copy(FileResource.find(BAMReaderTest.class, "bss-align-sorted.bam").openStream(), temp_bam);
 		File temp_bam_bai = FileUtil.createTempFile(new File("target"), "sample", ".bai");
 		FileUtil.copy(FileResource.find(BAMReaderTest.class, "bss-align-sorted.bam.bai").openStream(), temp_bam_bai);
 
 		temp_bam.deleteOnExit();
 		temp_bam_bai.deleteOnExit();
 
 		Iterator<SAMRecord> iterator = new SAMFileReader(temp_bam, temp_bam_bai).query("chr13", 0, 0, true);
 		while (iterator.hasNext()) {
 			SAMRecord each = iterator.next();
 			_logger.info(each.format());
 
			SAMRead read = SAM2SilkReader.convertToSAMRead(each);
 			_logger.info(read);
 
 			readDataList.add(read);
 
 		}
 	}
 }
