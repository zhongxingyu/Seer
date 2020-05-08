 package com.crowdplatform.util;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.web.multipart.MultipartFile;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class FileReader {
 
 	public List<Map<String, String>> readCSVFile(MultipartFile file) throws IOException {
 		List<Map<String, String>> result = Lists.newArrayList();

		CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()));
 		String[] headerLine = reader.readNext();
 		String[] nextLine;
 		while ((nextLine = reader.readNext()) != null) {
 			if (nextLine != null) {
 				Map<String, String> line = Maps.newHashMap();
 				boolean hasValue = false;
 				for (int i = 0; i < nextLine.length; ++i) {
 					line.put(headerLine[i], nextLine[i]);
 					if (!nextLine[i].isEmpty()) {
 						hasValue = true;
 					}
 				}
 				if (hasValue) {
 					result.add(line);
 				}
 			}
 		}
 		reader.close();
 
 		return result;
 	}
 }
