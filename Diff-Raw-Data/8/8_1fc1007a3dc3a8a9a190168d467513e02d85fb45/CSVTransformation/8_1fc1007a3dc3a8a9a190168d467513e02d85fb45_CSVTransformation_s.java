 package br.ufrj.ppgi.greco.dataTransformation.services;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class CSVTransformation {
 
 	private String separatorChar;
 	private String withHeader;
 
 	public CSVTransformation() {
 		super();
 	}
 
 	public ArrayList<HashMap<String, ArrayList<String>>> getValue(String data, String group, HashMap<String, String> transformationsIndexedByLabel) {
 		ArrayList<HashMap<String, ArrayList<String>>> result = new ArrayList<HashMap<String, ArrayList<String>>>();
 		try {
 			Character separator = ',';
 			if (this.separatorChar != null && !"".equals(this.separatorChar) && this.separatorChar.length() == 1) {
 				separator = this.separatorChar.charAt(0);
 			}
 			CSVReader reader = new CSVReader(new StringReader(data), separator);
 			
 			Map<String, List<ColunaValor<String, String>>> map = new LinkedHashMap<String, List<ColunaValor<String, String>>>();
 
 			if (this.withHeader!= null && "true".equals(this.withHeader)) {
 				String[] header = reader.readNext();
 
 			}
 			List<String> groupList = Arrays.asList(group.split(separator.toString()));
 			ArrayList<Integer> groupIndexList = groupAsIndexList(groupList);
 			
 			Collection<String> usedColumns = transformationsIndexedByLabel.keySet();
 			ArrayList<Integer> usedColumnsIndex= usedColumnsAsIndexList(usedColumns);
 			
 			
 			List<String> lineList = null;
 			String[] line = reader.readNext();
 			while (line != null) {
 				lineList = Arrays.asList(line);
 				
 				StringBuilder mapKey = new StringBuilder();
 				for (Integer groupIndexColumn : groupIndexList) {
 					mapKey.append(this.separatorChar).append(lineList.get(groupIndexColumn - 1));
 				}
 				
 				ArrayList<ColunaValor<String, String>> values = new ArrayList<ColunaValor<String, String>>();
 				for (Integer usedColumnIndex : usedColumnsIndex) {
 					values.add(new ColunaValor<String, String>(usedColumnIndex.toString(), lineList.get(usedColumnIndex - 1)));
 				}
 				
				if(map.containsKey(mapKey)) {
					map.get(mapKey).addAll(values);
 				} else {
					map.put(mapKey.substring(1), values);
 				}
 				
 				line = reader.readNext();
 			}
 			
 			for (Entry<String, List<ColunaValor<String, String>>> groupEntry : map.entrySet()) {
 				HashMap<String, ArrayList<String>> mapping = new HashMap<String, ArrayList<String>>();
 				for (Entry<String, String> transEntry : transformationsIndexedByLabel.entrySet()) {
 					ArrayList<String> values = new ArrayList<String>();
 					for (ColunaValor<String, String> groupValue : groupEntry.getValue()) {
 						if (groupValue.column.equals(transEntry.getKey())) {
 							values.add(groupValue.value);
 						}
 					}
 					mapping.put(transEntry.getValue(), values);
 				}
 				result.add(mapping);
 			}
 
 		} catch (Exception e) {
 			// FIXME Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	private ArrayList<Integer> usedColumnsAsIndexList(Collection<String> usedColumns) {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 		for (String usedColumn : usedColumns) {
 			result.add(Integer.parseInt(usedColumn));
 		}
 		return result;
 	}
 
 	private ArrayList<Integer> groupAsIndexList(List<String> groupList) {
 		ArrayList<Integer> result = new ArrayList<Integer>();
 		for (String groupItem : groupList) {
 			result.add(Integer.parseInt(groupItem));
 		}
 		return result;
 	}
 
 	public String getSeparatorChar() {
 		return separatorChar;
 	}
 
 	public void setSeparatorChar(String separatorChar) {
 		this.separatorChar = separatorChar;
 	}
 
 	public String getWithHeader() {
 		return withHeader;
 	}
 
 	public void setWithHeader(String withHeader) {
 		this.withHeader = withHeader;
 	}
 	
 	private class ColunaValor<C, V>{
 		private final C column;
 		private final V value;
 		public ColunaValor(final C column, final V value) {
 			this.column = column;
 			this.value = value;
 		}
 		@Override
 		public String toString() {
 			return "<"+column+","+value+">";
 		}
 	}
 }
