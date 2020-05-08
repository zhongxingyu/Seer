 package fatworm.query;
 
 import java.util.Collection;
 
 import fatworm.dataentity.DataEntity;
 import fatworm.record.RecordFile;
 import fatworm.util.Util;
 
 public class DistinctScan implements Scan {
 
 	Scan scan;
 	DistinctContainer container;
 	public DistinctScan(Scan scan) {
 		this.scan = scan;
 		container = Util.getDistinctContainer();
 		scan.beforeFirst();
 		while(scan.next())
 			container.update(scan);
 	}
 	
 	@Override
 	public void beforeFirst() {
 		container.beforeFirst();
 	}
 
 	@Override
 	public boolean next() {
 		return container.next();
 	}
 
 	@Override
 	public DataEntity getField(String fldname) {
 		return container.getColumnByIndex(indexOfField(fldname));
 	}
 
 	@Override
 	public boolean hasField(String fldname) {
 		return scan.hasField(fldname);
 	}
 
 	@Override
 	public DataEntity getColumn(String colname) {
 		return container.getColumnByIndex(indexOfColumn(colname));
 	}
 
 	@Override
 	public boolean hasColumn(String colname) {
 		return scan.hasColumn(colname);
 	}
 
 	@Override
 	public Collection<String> fields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Collection<String> columns() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public DataEntity getColumnByIndex(int index) {
 		return container.getColumnByIndex(index);
 	}
 
 	@Override
 	public int getNumberOfColumns() {
 		return scan.getNumberOfColumns();
 	}
 
 	@Override
 	public int indexOfField(String field) {
 		return scan.indexOfColumn(field);
 	}
 
 	@Override
 	public int type(String colname) {
 		return scan.type(colname);
 	}
 
 	@Override
 	public int type(int index) {
 		return scan.type(index);
 	}
 
 	@Override
 	public String fieldName(int index) {
 		return scan.fieldName(index);
 	}
 
 	@Override
 	public String columnName(int index) {
 		return scan.columnName(index);
 	}
 
 	@Override
 	public RecordFile getRecordFile() {
 		return null;
 	}
 
 	@Override
 	public DataEntity getFunctionValue(String func) {
 		return scan.getFunctionValue(func);
 	}
 
 	@Override
 	public boolean hasFunctionValue(String func) {
 		return scan.hasFunctionValue(func);
 	}
 	@Override
 	public int indexOfColumn(String column) {
		return scan.indexOfColumn(column);
 	}
 	@Override
 	public DataEntity getOrderKey(String key) {
 		return scan.getOrderKey(key);
 	}
 }
