 package net.imagini.cassandra.DumpSSTables;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class SSTableCassandraRow implements Serializable {
 
     /**
      * Serialization Version
      */
     private static final long serialVersionUID = 1L;
     private final Map<String, Map<String, String>> columns;
     private final HashMap<String, SSTableCassandraColumnTombstone> columnTombstones;
     private final ArrayList<SSTableCassandraTombstone> tombstones;
     private final ArrayList<SSTableCassandraRangeTombstone> rangeTombstones;
     private final String rowKey;
 
     public SSTableCassandraRow(String rowKey) {
 	this.columns = new HashMap<String, Map<String, String>>();
 	this.columnTombstones = new HashMap<String, SSTableCassandraColumnTombstone>();
 	this.tombstones = new ArrayList<SSTableCassandraTombstone>();
 	this.rangeTombstones = new ArrayList<SSTableCassandraRangeTombstone>();
 	this.rowKey = rowKey;
     }
 
     private long getLastTimeDeleted() {
 	long lastDel = Long.MIN_VALUE;
 	for (SSTableCassandraTombstone ts : tombstones) {
 	    if (ts instanceof SSTableCassandraRangeTombstone) {
 		// TODO: Take account of range tombstones, currently ignored.
 		// Moved rangeTombstones to separate variable
 	    } else {
 		if (ts.getMarkedForDeleteAt() > lastDel) {
 		    lastDel = ts.getMarkedForDeleteAt();
 		}
 	    }
 	}
 	return lastDel;
     }
 
     @Override
     public String toString() {
 	StringBuilder s = new StringBuilder();
 	Long lastDel = getLastTimeDeleted();
 	for (Entry<String, Map<String, String>> entry : columns.entrySet()) {
 	    if (Long.parseLong(entry.getValue().get("timestamp")) > lastDel) {
 		if (columnTombstones.containsKey(entry.getKey())) {
 		    SSTableCassandraColumnTombstone colTs = columnTombstones.get(entry.getKey());
 		    if (colTs instanceof SSTableCassandraDeletedColumnTombstone) {
			if (Long.parseLong(entry.getValue().get("timestamp")) < colTs.getTimestamp()) {
 			    continue;
 			}
 		    } else if (colTs instanceof SSTableCassandraCounterColumnTombstone) {
 			System.out.println("[Column Skipped :" + entry.getKey() + "] - This program does not currently support counter columns RowKey(" + rowKey + ")");
 			continue;
 		    } else if (colTs instanceof SSTableCassandraExpiringColumnTombstone) {
			if (Long.parseLong(entry.getValue().get("timestamp")) < ((SSTableCassandraExpiringColumnTombstone) colTs).getTtl() + colTs.getTimestamp()) {
 			    continue;
 			}
 		    }
 		}
 		if (!(s.length() == 0)) {
 		    s.append(", ");
 		}
 		// s.append(SSTableExportMapper.getJSONKey(entry.getKey()));
 		// s.append(SSTableExportMapper.getJSON(entry.getValue().get("value")));
 		s.append("\"" + entry.getKey() + "\": ");
 		s.append(SSTableExportMapper.getJSON(entry.getValue().get("value")));
 	    }
 	}
 
 	if (s.length() > 0) {
 	    return rowKey + " {" + s.toString() + "}";
 	} else {
 	    return null;
 	}
     }
 
     public Map<String, Map<String, String>> getColumns() {
 	return columns;
     }
 
     public ArrayList<SSTableCassandraTombstone> getTombstones() {
 	return tombstones;
     }
 
     public String getRowKey() {
 	return rowKey;
     }
 
     public HashMap<String, SSTableCassandraColumnTombstone> getColumnTombstones() {
 	return columnTombstones;
     }
 
     public ArrayList<SSTableCassandraRangeTombstone> getRangeTombstones() {
 	return rangeTombstones;
     }
 }
