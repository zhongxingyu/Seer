 package memoryManager;
 
 import dbCommands.*;
 import dbEnvironment.DbContext;
 import org.junit.Assert;
 import org.junit.Test;
 import tableTypes.Column;
 import tableTypes.ColumnTuple;
 import tableTypes.Table;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 27/11/2013
  * Time: 14:34
  * To change this template use File | Settings | File Templates.
  */
 public class HeapFileTests
 {
 	DbContext initBasicContext(String tableName)
 	{
 		DbContext context = new DbContext(RESOURCE_PATH);
 
 		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
 		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
 		ColumnTuple columnTuple3 = new ColumnTuple("baz", 1, "char");
 		ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
 		tuples.add(columnTuple1);
 		tuples.add(columnTuple2);
 		tuples.add(columnTuple3);
 		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);
 
 		createTableCommand.executeCommand(context);
 
 		return context;
 	}
 
 	@Test
 	public void SeedFileTest()
 	{
 		String tableName = "testTable";
 		DbContext context = initBasicContext(tableName);
 
 		Table newTable = context.tables().get(tableName);
 
 		context.close();
 
 		File tableFile = new File(context.getLocation() + newTable.getRelativeTablePath());
 		Assert.assertEquals(true, tableFile.exists());
 
 		File tableDataFile = new File(context.getLocation() + newTable.getRelativeDataPath());
 		Assert.assertEquals(true, tableDataFile.exists());
 
 		tableFile.delete();
 		tableDataFile.delete();
 	}
 
 	@Test
 	public void EmptySelectTest()
 	{
 		String tableName = "testTablegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 		SelectCommand selectCommand = new SelectCommand(tableName, null, 100);
 		selectCommand.executeCommand(context);
 
 		Assert.assertEquals(0, selectCommand.getResult().size());
 	}
 
 	@Test
 	public void Insert300RowsTest()
 	{
 		String tableName = "testTablegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 300;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
 		selectAllRowsCommand.executeCommand(context);
 		List<Object> result = selectAllRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(numberOfRows, result.size());
 
 		context.close();
 	}
 
 	@Test
 	public void HugeInsertTest()
 	{
 		String tableName = "testTableghsadfhjsdffsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
		int numberOfRows = 87000;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 
 		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
 		selectAllRowsCommand.executeCommand(context);
 		List<Object> result = selectAllRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(numberOfRows, result.size());
 
 		context.close();
 	}
 
 	@Test
 	public void SimpleInsertAndDecodeTest()
 	{
 		String tableName = "testTabsdfgasdflegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 300;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
 		selectAllRowsCommand.executeCommand(context);
 		List<Object> result = selectAllRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(numberOfRows, result.size());
 
 		Table table = context.getTableByName(tableName);
 		List<Column> rowSignature = table.rowSignature();
 
 		ArrayList<Object> expected = new ArrayList<Object>();
 
 		for (int i = 0; i < args.size(); ++i)
 			expected.add(rowSignature.get(i).type().getAsObject(args.get(i)));
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			ArrayList<Object> actual = (ArrayList<Object>) result.get(i);
 			for (int j = 0; j < actual.size(); ++j)
 				Assert.assertEquals(expected.get(j), actual.get(j));
 		}
 
 		context.close();
 	}
 
 	@Test
 	public void ComplexInsertAndDecodeTest()
 	{
 		String tableName = "testTabsdfgfasdflhsgdfkahsdlkfgasljdgfasdflegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 300;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
 		selectAllRowsCommand.executeCommand(context);
 		List<Object> result = selectAllRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(numberOfRows, result.size());
 
 		Table table = context.getTableByName(tableName);
 		List<Column> rowSignature = table.rowSignature();
 
 		ArrayList<Object> expected = new ArrayList<Object>();
 
 		for (int i = 0; i < args.size(); ++i)
 			expected.add(rowSignature.get(i).type().getAsObject(args.get(i)));
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			ArrayList<Object> actual = (ArrayList<Object>) result.get(i);
 			for (int j = 0; j < actual.size(); ++j)
 				Assert.assertEquals(expected.get(j), actual.get(j));
 		}
 
 		context.close();
 	}
 
 	@Test
 	public void BoundSelectTest()
 	{
 		String tableName = "testTabsdasbflaksdbfklfgasdflegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 1000;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 
 		int expectedRowCount = 200;
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 		SelectCommand selectRowsCommand = new SelectCommand(tableName, null, expectedRowCount);
 		selectRowsCommand.executeCommand(context);
 		List<Object> result = selectRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(expectedRowCount, result.size());
 
 		context.close();
 	}
 
 	@Test
 	public void SimpleUpdateAllTest()
 	{
 		String tableName = "testTabsdfgasdjfkahsdfflegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 300;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 
 		ArrayList<String> newArgs = new ArrayList<String>();
 
 		newArgs.add("5");
 		newArgs.add("kekeke");
 		newArgs.add("k");
 
 		TableRow newRow = new TableRow(newArgs);
 
 		UpdateCommand updateCommand = new UpdateCommand(tableName, null, newRow);
 		updateCommand.executeCommand(context);
 
 		int expectedRowCount = 200;
 
 		SelectCommand selectRowsCommand = new SelectCommand(tableName, null, expectedRowCount);
 		selectRowsCommand.executeCommand(context);
 		List<Object> result = selectRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(expectedRowCount, result.size());
 
 		Table table = context.getTableByName(tableName);
 		List<Column> rowSignature = table.rowSignature();
 
 		List<Object> expected = new ArrayList<Object>();
 
 		for (int i = 0; i < args.size(); ++i)
 			expected.add(rowSignature.get(i).type().getAsObject(newArgs.get(i)));
 
 		for (int i = 0; i < expectedRowCount; ++i)
 		{
 			List<Object> actual = (ArrayList<Object>) result.get(i);
 			for (int j = 0; j < actual.size(); ++j)
 				Assert.assertEquals(expected.get(j), actual.get(j));
 		}
 
 		context.close();
 	}
 
 	@Test
 	public void DeleteTest()
 	{
 		String tableName = "testTabljkglkfagdfasdhfegfsgdfks";
 		DbContext context = initBasicContext(tableName);
 
 		ArrayList<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 		ArrayList<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 300;
 
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			rows.add(tableRow);
 		}
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
 		selectAllRowsCommand.executeCommand(context);
 		List<Object> result = selectAllRowsCommand.getResult();
 
 		Assert.assertNotEquals(null, result);
 
 		Assert.assertEquals(numberOfRows, result.size());
 
 		DeleteCommand deleteCommand = new DeleteCommand(tableName, null);
 		deleteCommand.executeCommand(context);
 
 		SelectCommand selectCommand = new SelectCommand(tableName, null, numberOfRows);
 		selectCommand.executeCommand(context);
 
 		Assert.assertEquals(0, selectCommand.getResult().size());
 
 		context.close();
 	}
 
 	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";
 }
