 package com.github.kzn.jaot.morph;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Splitter;
 
 /**
  * Class for AOT grammatical features table.
  * The table is represented by a key that is used in the respective morphological
  * dictionaries and the value represents the POS tag and grammatical features
  * @author Anton Kazennikov
  *
  */
 public class GramTable {
 	public static class Record {
 		String pos;
 		String type;
 		List<String> feats;
 		
 		public Record() {
 			
 		}
 		
 		public Record(String pos, String type, List<String> feats) {
 			this.pos = pos;
 			this.type = type;
 			this.feats = feats;
 		}
 		
 		public List<String> feats() {
 			List<String> res = new ArrayList<String>(feats.size() + 1);
 			if(pos != null && !pos.isEmpty())
 				res.add(pos);
 			res.addAll(feats);
 			
 			return res;
 		}
 		
 		@Override
 		public String toString() {
 			return Objects.toStringHelper(this)
 					.add("POS", pos)
 					.add("feats", feats)
 					.toString();
 		}
 	}
 	
 	Map<String, Record> content = new HashMap<String, GramTable.Record>();
 	
 	public Record get(String key) {
 		return content.get(key);
 	}
 	
 	
 	public void read(BufferedReader br ) throws IOException {
 		while(true) {
 			String s = br.readLine();
 			if(s == null)
 				break;
 
 			s = s.trim();
 			int commentStart = s.indexOf("//");
 			if(commentStart != -1) {
 				s = s.substring(0, commentStart);
 			}
 
 			if(s.isEmpty())
 				continue;
 
 			String[] parts = s.split("\\s+");
 
 
 			String key = parts[0];
 			String type = parts[1];
 			String pos = parts[2];
 			List<String> feats = new ArrayList<String>();
 			if(parts.length > 3) {
 				for(String feat : Splitter.on(',').omitEmptyStrings().split(parts[3])) {
 					feats.add(feat.intern());
 				}
 			}
 			
			if(pos.equals("*"))
 				pos = null;
 
			content.put(key, new Record(pos != null? pos.intern() : null, type.intern(), feats));
 		}
 	}
 	
 	public void read(File f, Charset charset) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
 		try {
 			read(br);
 		} finally {
 			br.close();
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		File path = new File("seman/trunk/Dicts/Morph/ggramtab.tab");
 		GramTable gramTable = new GramTable();
 		gramTable.read(path, Charset.forName("CP1251"));
 	}
 }
