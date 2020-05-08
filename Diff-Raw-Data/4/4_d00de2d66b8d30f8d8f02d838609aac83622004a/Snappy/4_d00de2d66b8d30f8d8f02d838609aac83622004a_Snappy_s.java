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
 // utgb-core Project
 //
 // Snappy.java
 // Since: 2011/04/01
 //
 //--------------------------------------
 package org.utgenome.core.cui;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.utgenome.shell.UTGBShellException;
 import org.xerial.snappy.SnappyInputStream;
 import org.xerial.snappy.SnappyOutputStream;
 import org.xerial.util.FileType;
 import org.xerial.util.io.StandardInputStream;
 import org.xerial.util.io.StandardOutputStream;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 
 /**
  * Snappy compresser/decompressor
  * 
  * @author leo
  * 
  */
 public class Snappy extends UTGBCommandBase {
 
 	private static Logger _logger = Logger.getLogger(Snappy.class);
 
 	@Option(symbol = "c", longName = "stdout", description = "write on standard output")
 	private boolean useStdout = false;
 
 	@Option(symbol = "x", description = "decompress")
 	private boolean decompression = false;
 
 	@Argument(index = 0)
 	private String input;
 
 	@Option(symbol = "o", description = "output file name")
 	private String outputFileName;
 
 	@Override
 	public void execute(String[] args) throws Exception {
 
		String outputFileName = null;
 		InputStream in = null;
 		if (input == null || "-".equals(input)) {
 			_logger.info("use STDIN");
 			in = new StandardInputStream();
 			useStdout = true;
 		}
 		else {
 			in = new BufferedInputStream(new FileInputStream(input));
 			if (outputFileName == null) {
 				if (decompression && !input.endsWith(".snappy"))
 					throw new UTGBShellException("input file name does not end with .snappy. Use -o option to specify the output file name");
 				outputFileName = decompression ? FileType.removeFileExt(input) : input + ".snappy";
 			}
 		}
 
 		OutputStream out = null;
 		if (useStdout) {
 			_logger.info("output to STDOUT");
 			out = new StandardOutputStream();
 		}
 		else {
 			_logger.info("output to " + outputFileName);
 			out = new BufferedOutputStream(new FileOutputStream(outputFileName));
 		}
 
 		if (!decompression) {
 			// compression
 			readFully(in, new SnappyOutputStream(out));
 		}
 		else {
 			// decompression
 			readFully(new SnappyInputStream(in), out);
 		}
 
 	}
 
 	void readFully(InputStream in, OutputStream out) throws IOException {
 		try {
			byte[] buf = new byte[8192];
 			for (int readBytes = 0; (readBytes = in.read(buf)) != -1;) {
 				out.write(buf, 0, readBytes);
 			}
 		}
 		finally {
 			in.close();
 			out.flush();
 			out.close();
 		}
 	}
 
 	@Override
 	public String getOneLineDescription() {
 		return "snappy compressor/decompressor";
 	}
 
 	@Override
 	public String name() {
 		return "snappy";
 	}
 
 }
