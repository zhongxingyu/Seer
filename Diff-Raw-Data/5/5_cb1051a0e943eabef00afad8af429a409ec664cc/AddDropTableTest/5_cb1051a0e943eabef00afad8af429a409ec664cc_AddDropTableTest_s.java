 package dbmigrate.executor;
 
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import junit.framework.TestCase;
 import dbmigrate.model.db.Column;
 import dbmigrate.model.db.DbConnector;
 import dbmigrate.model.db.IColumn;
 import dbmigrate.model.db.Table;
 import dbmigrate.model.db.TypeEnum;
 import dbmigrate.model.operation.AddColumnOperationDescriptor;
 import dbmigrate.model.operation.CreateTableOperationDescriptor;
 import dbmigrate.model.operation.DropTableOperationDescriptor;
 
 public class AddDropTableTest extends TestCase {
 	private Connection dbcon = null;
 	private String sampleTable = null;
 	
 	@Override
 	public void setUp() {
 		dbcon = new DbConnector().getConnection("postgresql", "149.156.205.250:13833", "dbmigrate", "dbmigrate", "dbmigrate");
 	}
 	
 	private String getTblName() {
 		Random rnd = new Random();
 		int randNum = rnd.nextInt(9999);
 		String colname = "sampleTable" + randNum;
 		return colname;
 	}
 	
 	private String getColName() {
 		Random rnd = new Random();
 		int randNum = rnd.nextInt(9999);
 		String colname = "sampleCol" + randNum;
 		return colname;
 	}
 	
 	
 	public void testAddTable() {
 		String colname = getColName();
 		Column col = new Column();
 		col.setName(colname);
 		col.setType(TypeEnum.INT);
 	
 		CreateTableOperationDescriptor cto = new CreateTableOperationDescriptor();
 		
 		Table tbl = new Table();
 		tbl.setName(getTblName());
 		List<IColumn> columns = new ArrayList<IColumn>();
 		columns.add(col);
 		tbl.setColumns(columns);
 		cto.setTable(tbl);
 		
		CreateTableExecutor cte = new CreateTableExecutor();
 		cte.setConnection(dbcon);
 		
 		
 		System.out.println(cte.createSql(cto));
 		try {
 			cte.execute(cto);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	public void testDropTable() {
 		String colname = getColName();
 		Column col = new Column();
 		col.setName(colname);
 		col.setType(TypeEnum.INT);
 	
 		CreateTableOperationDescriptor cto = new CreateTableOperationDescriptor();
 		
 		Table tbl = new Table();
 		tbl.setName(getTblName());
 		List<IColumn> columns = new ArrayList<IColumn>();
 		columns.add(col);
 		tbl.setColumns(columns);
 		cto.setTable(tbl);
 		
		CreateTableExecutor cte = new CreateTableExecutor();
 		cte.setConnection(dbcon);
 		
 		
 		System.out.println(cte.createSql(cto));
 		try {
 			cte.execute(cto);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 		
 		
 		DropTableOperationDescriptor dto = new DropTableOperationDescriptor(tbl);
 		DropTableExecutor dte = new DropTableExecutor(dbcon);
 		
 		System.out.println(dte.createSql(dto));
 		try {
 			dte.execute(dto);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	
 	
 }
