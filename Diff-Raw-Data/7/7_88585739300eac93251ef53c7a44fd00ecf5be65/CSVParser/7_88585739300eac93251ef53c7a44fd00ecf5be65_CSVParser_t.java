 /*
  * Copyright (c) 2011-2013 Nexmo Inc
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package net.pricing.common.parser.helper;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.pricing.common.parser.helper.CSVParser;
 import net.pricing.common.utils.FileUtils;
 import net.pricing.common.utils.PricingColumn;
 import net.pricing.common.utils.PricingConstants;
 import net.pricing.common.utils.PricingRow;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author Yuliia Petrushenko
  * @version
  */
 public class CSVParser {
 
 	private static final Logger logger = Logger.getLogger(CSVParser.class);
 	private String[] columns;
 	private List<PricingColumn> columnsIndexing;
 	private String delimiter;
 
 	public List<PricingRow> parseFile(File file, String[] columns,
 			String delimiter) {
 
 		List<PricingRow> resultList = new ArrayList<PricingRow>();
 
 		this.columns = new String[columns.length];
 		this.columns = columns;
 		columnsIndexing = new ArrayList<PricingColumn>();
 		this.delimiter = delimiter;
 
 		try {
 			logger.debug("[parserFile] ---------> Start CSV file parsing");
 			FileInputStream fileIn = new FileInputStream(file);
 			DataInputStream in = new DataInputStream(fileIn);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strHeader = br.readLine();
 			findHeaderColumns(strHeader);
 			String strLine = new String();
 			resultList = new ArrayList<PricingRow>();
 			while ((strLine = br.readLine()) != null) {
 				PricingRow pricingRow = new PricingRow();
 				String[] array = strLine.split(delimiter);
 				for (int i = 0; i < array.length; i++) {
 					for (PricingColumn currentColumn : columnsIndexing) {
 						if (currentColumn.getIndex() == i) {
 							PricingColumn pColumn = new PricingColumn();
 							pColumn.setIndex(currentColumn.getIndex());
 							pColumn.setName(currentColumn.getName());
 							pColumn.setValue(array[i]);
 							pricingRow.addColumn(pColumn);
 
 						}
 
 					}
 				}
 				if (pricingRow.getColumns() != null) {
 
 					if (pricingRow.getColumns().size() != columnsIndexing
 							.size()) {
 						pricingRow.setErrorRow(true);
 					}
 					resultList.add(pricingRow);
 				}
 
 			}
 			in.close();
 			logger.debug("[parseFile] ---------> Parsing completed");
 		} catch (IOException e) {
 			logger.error("[parseFile] --> IOException" + e.getMessage());
 		}
 
 		return resultList;
 	}
 
 	public File parseFromCsv(File file) {
 		if (!FileUtils.isFileExtMatchesTheParser(file.getName(),
 				PricingConstants.CSV_FILE_EXTENSION)) {
 			return null;
 		}
 		return file;
 	}
 
 	private void findHeaderColumns(String headerRow) {
 		String[] strHeaderArr = headerRow.toLowerCase().split(this.delimiter);
 		for (int i = 0; i < strHeaderArr.length; i++) {
			String currentName = strHeaderArr[i].toLowerCase()
					.replace("\"", "").trim();
 			for (int j = 0; j < this.columns.length; j++) {
				if (currentName.equals(columns[j].toLowerCase().trim())) {
 					PricingColumn currentColumn = new PricingColumn();
 					currentColumn.setIndex(i);
 					currentColumn.setName(columns[j]);
 					columnsIndexing.add(currentColumn);
 					break;
 				}
 			}
 		}
 	}
 
 }
