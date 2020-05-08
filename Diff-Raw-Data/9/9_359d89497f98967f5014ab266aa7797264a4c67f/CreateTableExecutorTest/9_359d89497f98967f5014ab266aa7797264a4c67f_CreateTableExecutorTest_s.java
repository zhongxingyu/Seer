 package dbmigrate.executor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 import dbmigrate.model.db.Column;
 import dbmigrate.model.db.IColumn;
 import dbmigrate.model.db.ITable;
 import dbmigrate.model.db.Table;
 import dbmigrate.model.db.TypeEnum;
 import dbmigrate.model.operation.CreateTableOperationDescriptor;
 
 public class CreateTableExecutorTest extends TestCase {
 
 	public void testCreateSql() {
 		ITable table = new Table();
 
 		table.setName("ala");
 
 		List<IColumn> columns = new ArrayList<IColumn>();
 
 		Column column = new Column();
 		column.setLength(255);
 		column.setType(TypeEnum.VARCHAR);
 		column.setName("ola_varchar_255");
 		column.setNullable(false);
 		columns.add(column);
 
 		Column column2 = new Column();
 		column2.setType(TypeEnum.INT);
 		column2.setName("ola_int");
 		column2.setNullable(true);
 		columns.add(column2);
 
 		table.setColumns(columns);
 
 		CreateTableOperationDescriptor operation = new CreateTableOperationDescriptor(table);
 
 		CreateTableExecutor executor = new CreateTableExecutor(null);
 		System.out.println(executor.createSql(operation));
		assertEquals("CREATE TABLE \"ala\" (ola_varchar_255 VARCHAR (255) NOT NULL,ola_int INT NULL);", executor.createSql(operation));
 
 	}
 
 }
