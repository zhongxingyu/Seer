 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
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
 // utgb-shell Project
 //
 // Keyword.java
 // Since: May 20, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.Reader;
 
import org.utgenome.UTGBException;
 import org.utgenome.format.keyword.KeywordDB;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult;
 import org.utgenome.shell.Import.FileType;
 import org.xerial.lens.Lens;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 
 /**
  * keyword DB management tool
  * 
  * @author leo
  * 
  */
 public class Keyword extends UTGBShellCommand {
 
 	private static Logger _logger = Logger.getLogger(Keyword.class);
 
 	@Option(symbol = "f", longName = "file", description = "keyword database file name. default=db/keyword.sqlite")
 	private String dbFile = "db/keyword.sqlite";
 
 	public static enum SubCommand {
 		IMPORT, ALIAS, SEARCH
 	};
 
 	/**
 	 * import (from BED, FASTA.index), alias
 	 */
 	@Argument(index = 0, name = "command")
 	private SubCommand subCommand = null;
 
 	@Argument(index = 1, name = "input file")
 	private String input = "-";
 
 	@Option(symbol = "r", longName = "ref", description = "reference sequence name for the keywords, e.g., hg19, ce6, etc.")
 	private String ref;
 
 	@Option(symbol = "p", description = "keyword page (default = 1)")
 	private int page = 1;
 	@Option(symbol = "s", description = "page size (default = 10)")
 	private int pageSize = 10;
 
 	@Option(symbol = "t", description = "input file type (AUTO, BED) for importing")
 	private FileType inputFileType = FileType.AUTO;
 
 	@Override
 	public void execute(String[] args) throws Exception {
 
 		if (subCommand == null)
			throw new UTGBException("specify on of the command: utgb keyword (import|alias)");
 
 		_logger.info("keyword database: " + dbFile);
 		File dbPath = new File(getProjectRoot(), dbFile);
 		if (!dbPath.getParentFile().exists())
 			dbPath.getParentFile().mkdirs();
 
 		KeywordDB db = new KeywordDB(dbPath);
 
 		StopWatch timer = new StopWatch();
 		try {
 			switch (subCommand) {
 			case IMPORT: {
 				if (ref == null)
 					throw new UTGBShellException("specify a reference sequence name with -r option");
 
 				Reader r = getInputFileReader();
 
 				if (inputFileType == FileType.AUTO)
 					inputFileType = Import.detectFileType(input);
 				switch (inputFileType) {
 				case BED:
 					db.importFromBED(ref, r);
 					_logger.info(String.format("done. %s sec.", timer.getElapsedTime()));
 					break;
 				default:
 					throw new UTGBShellException(String.format("Unsupported (or unknown) file type. Use -t option to explicitely specify the file type."));
 				}
 				break;
 			}
 			case ALIAS:
 				Reader r = getInputFileReader();
 
 				db.importKeywordAliasFile(r);
 				_logger.info(String.format("done. %s sec.", timer.getElapsedTime()));
 				break;
 			case SEARCH:
 				KeywordSearchResult query = db.query(ref, input, page, pageSize);
 				System.out.println(Lens.toSilk(query));
 				break;
 			}
 
 		}
 		finally {
 			db.close();
 		}
 
 	}
 
 	private BufferedReader getInputFileReader() throws FileNotFoundException {
 		if (input != null && !input.equals("-")) {
 			_logger.info("input file: " + input);
 			return new BufferedReader(new FileReader(input));
 		}
 		else {
 			_logger.info("use STDIN for input");
 			return new BufferedReader(new InputStreamReader(System.in));
 		}
 	}
 
 	@Override
 	public String getOneLinerDescription() {
 		return "create a keyword database";
 	}
 
 	@Override
 	public String name() {
 		return "keyword";
 	}
 
 }
