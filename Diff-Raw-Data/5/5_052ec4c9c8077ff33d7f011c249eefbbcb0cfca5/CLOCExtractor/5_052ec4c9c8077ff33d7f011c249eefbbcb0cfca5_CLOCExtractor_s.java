 package org.pescuma.buildhealth.extractor.loc;
 
 import static java.lang.Integer.*;
 import static java.lang.Math.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.pescuma.buildhealth.core.BuildData;
 import org.pescuma.buildhealth.extractor.BaseBuildDataExtractor;
 import org.pescuma.buildhealth.extractor.PseudoFiles;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 // http://cloc.sourceforge.net/
 public class CLOCExtractor extends BaseBuildDataExtractor {
 	
 	public CLOCExtractor(PseudoFiles files) {
 		super(files, "csv");
 	}
 	
 	@Override
 	protected void extract(String csvFilename, InputStream input, BuildData data) throws IOException {
 		@SuppressWarnings("resource")
		CSVReader reader = new CSVReader(new InputStreamReader(input));
 		
 		String[] headers = null;
 		int languageCol = -1;
 		int filenameCol = -1;
 		int filesCol = -1;
 		
 		String[] line;
 		while ((line = reader.readNext()) != null) {
 			if (headers == null) {
 				headers = line;
 				languageCol = findHeader(headers, "language");
 				filenameCol = findHeader(headers, "filename");
 				filesCol = findHeader(headers, "files");
 				if (languageCol < 0)
 					throw new IllegalStateException("Invalid cloc file: missing language column");
 				if ((filesCol < 0) == (filenameCol < 0))
 					throw new IllegalStateException(
 							"Invalid cloc file: one column (and only one) of filename / files should exist");
 				continue;
 			}
 			
 			if (headers.length <= languageCol)
 				// ?
 				continue;
 			
 			String language = line[languageCol];
 			String filename = (filenameCol < 0 ? "" : line[filenameCol]);
 			
 			if (filenameCol >= 0) {
 				data.add(1, "LOC", language, "files", filename);
 			} else {
 				int files = parseInt(line[filesCol]);
 				if (files > 0)
 					data.add(files, "LOC", language, "files");
 			}
 			
 			int length = min(line.length, headers.length);
 			for (int i = 0; i < length; i++) {
 				if (i == languageCol || i == filenameCol || i == filesCol)
 					continue;
 				
 				int val = parseInt(line[i]);
 				if (val > 0)
 					data.add(val, "LOC", language, headers[i], filename);
 			}
 		}
 	}
 	
 	private int findHeader(String[] headers, String string) {
 		for (int i = 0; i < headers.length; i++)
 			if (string.equalsIgnoreCase(headers[i]))
 				return i;
 		return -1;
 	}
 }
