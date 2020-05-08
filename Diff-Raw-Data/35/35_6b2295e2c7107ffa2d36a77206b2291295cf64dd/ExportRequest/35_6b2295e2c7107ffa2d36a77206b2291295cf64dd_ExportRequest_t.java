 package org.araqne.logstorage.dump;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 public class ExportRequest {
 	private String driverType;
	private String guid;
 	private Set<String> tableNames;
 	private Date from;
 	private Date to;
 	private Map<String, String> params;
 
 	public ExportRequest(String driverType, Set<String> tableNames, Date from, Date to, Map<String, String> params) {
		this(UUID.randomUUID().toString(), driverType, tableNames, from, to, params);
	}

	public ExportRequest(String guid, String driverType, Set<String> tableNames, Date from, Date to, Map<String, String> params) {
		this.guid = guid;
 		this.driverType = driverType;
 		this.tableNames = tableNames;
 		this.from = from;
 		this.to = to;
 		this.params = params;
 	}
 
 	public ExportRequest clone() {
 		HashSet<String> clonedTableNames = new HashSet<String>(tableNames);
		HashMap<String, String> clonedParams = new HashMap<String, String>(params);
		return new ExportRequest(guid, driverType, clonedTableNames, cloneDate(from), cloneDate(to), clonedParams);
 	}
 
 	private Date cloneDate(Date d) {
 		return d == null ? null : (Date) d.clone();
 	}
 
 	public String getDriverType() {
 		return driverType;
 	}
 
 	public void setDriverType(String driverName) {
 		this.driverType = driverName;
 	}
 
 	public String getGuid() {
 		return guid;
 	}
 
 	public Set<String> getTableNames() {
 		return tableNames;
 	}
 
 	public Date getFrom() {
 		return from;
 	}
 
 	public Date getTo() {
 		return to;
 	}
 
 	public Map<String, String> getParams() {
 		return params;
 	}
 }
