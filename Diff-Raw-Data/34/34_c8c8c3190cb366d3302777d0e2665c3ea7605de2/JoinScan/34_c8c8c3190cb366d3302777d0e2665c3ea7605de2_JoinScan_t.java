 package fatworm.query;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import fatworm.dataentity.DataEntity;
 import fatworm.record.RecordFile;
 
 public class JoinScan implements Scan{
 
     List<Scan> scanList;
     int width;
     public JoinScan(List<Scan> scanList) {
         this.scanList = scanList;
         width = 0;
         for (Scan s: scanList) {
             width += s.getNumberOfColumns();
         }
     }
 	@Override
 	public void beforeFirst() {
 		for (Scan s: scanList)
 			s.beforeFirst();
 		for (int i =0; i <scanList.size() -1; i++)
 			scanList.get(i).next();
 	}
 	
 	@Override
 	public boolean next() {
         for (int i = scanList.size() - 1; i >0; i--) {
             if(scanList.get(i).next())
                 return true;
             else {
                 scanList.get(i).beforeFirst();
                 scanList.get(i).next();
             }
         }
         return scanList.get(0).next();
 	}
 	@Override
 	public DataEntity getField(String fldname) {
         for (Scan s: scanList) {
             if (s.hasField(fldname))
                 return s.getField(fldname);
         }
         return null;
 	}
 	@Override
 	public boolean hasField(String fldname) {
         for (Scan s: scanList) {
             if (s.hasField(fldname))
                 return true;
         }
 		return false;
 	}
 	@Override
 	public boolean hasColumn(String colname) {
         for (Scan s: scanList) {
             if (s.hasColumn(colname))
                 return true;
         }
 		return false;
 	}
 	@Override
 	public Collection<String> fields() {
         Set<String> result = new HashSet<String>();
         for (Scan s: scanList)
             result.addAll(s.fields());
         return result;
 	}
 	@Override
 	public Collection<String> columns() {
         Set<String> result = new HashSet<String>();
         for (Scan s: scanList)
             result.addAll(s.columns());
         return result;
 	}
 	@Override
 	public DataEntity getColumnByIndex(int index) {
         for (Scan s: scanList) {
             int number = s.getNumberOfColumns();
             if (number <= index)
                 index -= number;
             else 
                 return s.getColumnByIndex(index);
         }
         return null;
 	}
 	@Override
 	public int getNumberOfColumns() {
  		return width;
 	}
 	@Override
 	public DataEntity getColumn(String colname) {
         for (Scan s: scanList) {
             if (s.hasColumn(colname))
                 return s.getColumn(colname);
         }
 		return null;
 	}
 	
 	@Override
 	public int indexOfField(String field) {
 		int result = 0;
 		for (Scan scan: scanList) {
 			if (scan.indexOfField(field) >= 0)
 				return result + scan.indexOfField(field);
 			else 
 				result += scan.getNumberOfColumns();
 		}
 		return -1;
 	}
 	
 	@Override
 	public int indexOfColumn(String column) {
 		int result = 0;
 		for (Scan scan: scanList) {
 			if (scan.indexOfColumn(column) >= 0)
 				return result + scan.indexOfColumn(column);
 			else 
 				result += scan.getNumberOfColumns();
 		}
 		return -1;
 	}
 	
 	
 	@Override
 	public int type(String colname) {
		return type(indexOfColumn(colname));
 	}
 	@Override
 	public int type(int index) {
 		if (index < 0)
 			return -1;
 		for (Scan scan: scanList) {
 			if (index >= scan.getNumberOfColumns())
 				index -= scan.getNumberOfColumns();
 			else 
 				return scan.type(index);
 		}
 		return -1;
 	}
 	@Override
 	public String fieldName(int index) {
 		if (index < 0)
 			return null;
 		for (Scan scan: scanList) {
 			if (index >= scan.getNumberOfColumns())
 				index -= scan.getNumberOfColumns();
 			else 
 				return scan.fieldName(index);
 		}
 		return null;
 	}
 	@Override
 	public String columnName(int index) {
 		if (index < 0)
 			return null;
 		for (Scan scan: scanList) {
 			if (index >= scan.getNumberOfColumns())
 				index -= scan.getNumberOfColumns();
 			else 
 				return scan.fieldName(index);
 		}
 		return null;
 	}
 	@Override
 	public RecordFile getRecordFile() {
 		return null;
 	}
 	@Override
 	public DataEntity getFunctionValue(String func) {
 		// no function could be produced by join
 		return null;
 	}
 	public boolean hasFunctionValue(String func) {
 		return false;
 	}	
 	
 	@Override
 	public DataEntity getOrderKey(String key) {
 		return getColumn(key);
 	}			
 }
