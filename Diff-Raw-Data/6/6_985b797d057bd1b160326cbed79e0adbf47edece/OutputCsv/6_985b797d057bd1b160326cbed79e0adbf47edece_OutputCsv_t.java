 /*
  * Copyright 2012 Future Systems
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
 package org.araqne.logdb.query.command;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.nio.charset.Charset;
 import java.util.List;
 
 import org.araqne.logdb.LogMap;
 import org.araqne.logdb.LogQueryCommand;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class OutputCsv extends LogQueryCommand {
 	private Charset utf8;
 	private List<String> fields;
 	private File f;
 	private FileOutputStream os;
 	private CSVWriter writer;
 	private String[] csvLine;
 
 	public OutputCsv(File f, List<String> fields) throws IOException {
 		this.f = f;
 		this.os = new FileOutputStream(f);
 		this.fields = fields;
 		this.utf8 = Charset.forName("utf-8");
 		this.csvLine = new String[fields.size()];
 		this.writer = new CSVWriter(new OutputStreamWriter(os, utf8));
 		writer.writeNext(fields.toArray(new String[0]));
 	}
 
 	public File getCsvFile() {
 		return f;
 	}
 
 	public List<String> getFields() {
 		return fields;
 	}
 
 	@Override
 	public void push(LogMap m) {
 		int i = 0;
 		for (String field : fields) {
 			Object o = m.get(field);
			String s = o == null ? "" : o.toString();
			int p = s.indexOf('\n');
			if (p >= 0)
				s = s.substring(0, p);
			csvLine[i++] = s;
 		}
 
 		writer.writeNext(csvLine);
 		write(m);
 	}
 
 	@Override
 	public boolean isReducer() {
 		return false;
 	}
 
 	@Override
 	public void eof(boolean canceled) {
 
 		this.status = Status.Finalizing;
 		try {
 			writer.flush();
 		} catch (IOException e1) {
 		}
 
 		try {
 			os.close();
 		} catch (IOException e) {
 		}
 		super.eof(canceled);
 	}
 }
