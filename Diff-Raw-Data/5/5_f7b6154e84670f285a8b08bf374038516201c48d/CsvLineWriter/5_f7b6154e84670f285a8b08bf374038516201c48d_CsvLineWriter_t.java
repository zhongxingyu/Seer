 /**
  * Copyright 2014 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.writer;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.query.command.IoHelper;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 /**
  * @author darkluster
  */
 public class CsvLineWriter implements LineWriter {
 	private FileOutputStream os;
 	private CSVWriter writer;
 	private List<String> fields;
 	private String[] csvLine;
 	private SimpleDateFormat sdf;
 
 	public CsvLineWriter(String path, List<String> fields, String encoding, char separator, boolean useBom,
 			Map<String, List<Integer>> boms) throws IOException {
 		this.fields = fields;
 		this.csvLine = new String[fields.size()];
 		this.os = new FileOutputStream(new File(path));
 		this.writer = new CSVWriter(new OutputStreamWriter(os, Charset.forName(encoding)), separator);
 
 		if (!boms.containsKey(encoding))
 			throw new QueryParseException("unsuported-encoding: " + encoding, -1);
 
		if (useBom)
			for (Integer bom : boms.get(encoding))
				os.write(bom);
 
 		this.writer.writeNext(fields.toArray(new String[0]));
 		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 	}
 
 	@Override
 	public void write(Row m) throws IOException {
 		int i = 0;
 		for (String field : fields) {
 			Object o = m.get(field);
 			String s = null;
 			if (o == null)
 				s = "";
 			else if (o instanceof Date)
 				s = sdf.format(o);
 			else
 				s = o.toString();
 			csvLine[i++] = s;
 		}
 		writer.writeNext(csvLine);
 	}
 
 	@Override
 	public void close() throws IOException {
 		IoHelper.close(writer);
 		IoHelper.close(os);
 	}
 }
