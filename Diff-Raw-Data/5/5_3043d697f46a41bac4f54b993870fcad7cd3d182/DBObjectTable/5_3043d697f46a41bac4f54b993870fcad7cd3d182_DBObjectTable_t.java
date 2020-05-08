 package dh.protege41.db2onto.event.dbobject;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 public class DBObjectTable extends DBObject implements Comparable {
 	public static final String TABLE_CAT = "TABLE_CAT"; //string: show name of database
 	public static final String TABLE_SCHEM = "TABLE_SCHEM";//string dbo
 	public static final String TABLE_NAME = "TABLE_NAME"; //string name of table
 	public static final String TABLE_TYPE = "TABLE_TYPE";//string
 	public static final String EXCEPT_TABLE = "sysdiagrams";
 	//header name
 	public static final String CATEGORY = "Category";
 	public static final String SCHEM = "Schem";
 	public static final String TYPE = "Type";
 	public static final String COLUMNS = "Columns";
 	public static final String REF_BY = "Referenced by";
 	public static final String REF_TO = "Reference to";
 	public static final String PRIMARY_KEY = "Primary key";
 	public static final String FOREIGN_KEY = "Foreign keys";
 	
 	private String category;
 	private String schem;
 	private String type;
 	private int tableCase = DBObjectType.CASE_0;
 	
 	private List<DBObjectColumn> columns = new ArrayList<DBObjectColumn>();
 	private List<DBObjectPrimaryKey> primaryKeys = new ArrayList<DBObjectPrimaryKey>();
 	private List<DBObjectForeignKey> foreignKeys = new ArrayList<DBObjectForeignKey>();
 	
 	public DBObjectTable() {
 		super(DBObjectType.DB_TABLE_OBJECT, "Unknown");
 	}
 	public DBObjectTable(String name) {
 		super(DBObjectType.DB_TABLE_OBJECT, name);
 	}
 	public DBObjectTable(String name, String cat, String schem, String type) {
 		super(DBObjectType.DB_TABLE_OBJECT, name);
 		this.category = cat;
 		this.schem = schem;
 		this.type = type;
 	}
 	public DBObjectTable(String name, String cat, String schem, String type, List<DBObjectColumn> cols, List<DBObjectForeignKey> fks, List<DBObjectPrimaryKey> pks) {
 		super(DBObjectType.DB_TABLE_OBJECT, name);
 		this.category = cat;
 		this.schem = schem;
 		this.type = type;
 		this.columns.addAll(cols);
 		this.primaryKeys.addAll(pks);
 		this.foreignKeys.addAll(fks);
 	}
 	
 	public String toString() {
 		return getName() + " (" + tableCase + ")";
 	}
 	
 	public DBObjectForeignKey getFKByColumnName(String colName) {
 		if(colName == null){
 			return null;
 		}
 		for(DBObjectForeignKey fk : this.foreignKeys) {
 			if(colName.equals(fk.getFKColumn())) {
 				return fk;
 			}
 		}
 		return null;
 	}
 	
 	public DBObjectColumn getColumnByName(String colName) {
 		if(colName == null) 
 			return null;
 		for(DBObjectColumn col : this.columns) {
 			if(colName.equals(col.getName()))
 				return col;
 		}
 		return null;
 	}
 	public void addColumn(DBObjectColumn col) {
 		for(DBObjectColumn obj : this.columns) {
 			if(obj.getName().equals(col.getName()))
 				return;
 		}
 		this.columns.add(col);
 	}
 	public void addPrimaryKey(DBObjectPrimaryKey pk) {
 		for(DBObjectPrimaryKey obj : this.primaryKeys) {
			if(obj.getColumn().equals(pk.getColumn()))
 				return;
 		}
 		this.primaryKeys.add(pk);
 	}
 	public void addForeignKey(DBObjectForeignKey fk) {
 		for(DBObjectForeignKey obj : this.foreignKeys) {
			if(obj.getFKColumn().equals(fk.getFKColumn()))
 				return;
 		}
 		this.foreignKeys.add(fk);
 	}
 	
 	public String getCategory() {
 		return category;
 	}
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	public String getSchem() {
 		return schem;
 	}
 	public void setSchem(String schem) {
 		this.schem = schem;
 	}
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public int getTableCase() {
 		return tableCase;
 	}
 	public void setTableCase(int tableCase) {
 		this.tableCase = tableCase;
 	}
 	public List<DBObjectColumn> getColumns() {
 		return columns;
 	}
 	public void setColumns(List<DBObjectColumn> columns) {
 		this.columns.addAll(columns);
 	}
 	public List<DBObjectPrimaryKey> getPrimaryKeys() {
 		return primaryKeys;
 	}
 	public void setPrimaryKeys(List<DBObjectPrimaryKey> primaryKeys) {
 		this.primaryKeys.addAll(primaryKeys);
 	}
 	public List<DBObjectForeignKey> getForeignKeys() {
 		return foreignKeys;
 	}
 	public void setForeignKeys(List<DBObjectForeignKey> foreignKeys) {
 		this.foreignKeys.addAll(foreignKeys);
 	}
 	
 	@Override
 	public int compareTo(Object o) {
 		if(!(o instanceof DBObjectTable)) {
 			throw new ClassCastException();
 		}
 		return DBObjectTable.this.getName().compareTo(((DBObjectTable)o).getName());
 	}
 	
 	static class CaseComparator implements Comparator {
 
 		@Override
 		public int compare(Object o1, Object o2) {
 			if(!(o1 instanceof DBObjectTable) || !(o2 instanceof DBObjectTable)) {
 				throw new ClassCastException();
 			}
 			
 			return -(((DBObjectTable)o1).getTableCase() - ((DBObjectTable)o2).getTableCase());
 		}
 		
 	}
 }
