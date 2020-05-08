 package org.araqne.logstorage.dump;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 public class ImportRequest {
 	private String driverType;
	private String guid = UUID.randomUUID().toString();
 	private Map<String, String> params = new HashMap<String, String>();
 	private List<DumpTabletEntry> entries = new ArrayList<DumpTabletEntry>();
 
 	public ImportRequest() {
 	}
 
 	public ImportRequest(String driverType, Map<String, String> params, List<DumpTabletEntry> entries) {
 		this.driverType = driverType;
 		this.params = params;
 		this.entries = entries;
 	}
 
 	public ImportRequest clone() {
 		List<DumpTabletEntry> l = new ArrayList<DumpTabletEntry>();
 		for (DumpTabletEntry e : entries)
 			l.add(e.clone());
 
		return new ImportRequest(driverType, new HashMap<String, String>(params), l);
 	}
 
 	public String getDriverType() {
 		return driverType;
 	}
 
 	public void setDriverType(String driverType) {
 		this.driverType = driverType;
 	}
 
 	public String getGuid() {
 		return guid;
 	}
 
 	public void setGuid(String guid) {
 		this.guid = guid;
 	}
 
 	public Map<String, String> getParams() {
 		return params;
 	}
 
 	public void setParams(Map<String, String> params) {
 		this.params = params;
 	}
 
 	public List<DumpTabletEntry> getEntries() {
 		return entries;
 	}
 
 	public void setEntries(List<DumpTabletEntry> entries) {
 		this.entries = entries;
 	}
 }
