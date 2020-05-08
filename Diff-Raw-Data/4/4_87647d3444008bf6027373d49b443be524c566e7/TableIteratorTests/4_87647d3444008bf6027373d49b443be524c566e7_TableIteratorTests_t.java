 package memoryManager;
 
 import dbCommands.CreateTableCommand;
 import dbCommands.InsertRowsCommand;
 import dbCommands.TableRow;
 import dbEnvironment.DbContext;
 import index.TableEntryPtr;
 import org.junit.Assert;
 import org.junit.Test;
 import tableTypes.Column;
 import tableTypes.ColumnTuple;
 import tableTypes.Table;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nikita_kartashov
  * Date: 22/01/2014
  * Time: 17:08
  * To change this template use File | Settings | File Templates.
  */
 public class TableIteratorTests
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
 	public void SelectAllTest()
 	{
 		String tableName = "imported";
 		DbContext context = initBasicContext(tableName);
 
 		List<String> args = new ArrayList<String>();
 		args.add("4");
 		args.add("gjghjf");
 		args.add("k");
 		TableRow tableRow = new TableRow(args);
 
 		List<TableRow> rows = new ArrayList<TableRow>();
 
 		int numberOfRows = 5000;
 		for (int i = 0; i < numberOfRows; ++i)
 			rows.add(tableRow);
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 
 		TableIterator tableIterator = new TableIterator(context, tableName);
 
 		Table table = context.getTableByName(tableName);
 		List<Column> rowSignature = table.rowSignature();
 
 		List<Object> expected = new ArrayList<Object>();
 
 		for (int i = 0; i < args.size(); ++i)
 			expected.add(rowSignature.get(i).type().getAsObject(args.get(i)));
 
 		int extractedRows = 0;
 		while (!tableIterator.isFinished())
 		{
 			extractedRows++;
 			List<Object> actual = tableIterator.nextRow();
 			for (int j = 0; j < actual.size(); ++j)
 				Assert.assertEquals(expected.get(j), actual.get(j));
 		}
 
 		Assert.assertEquals(numberOfRows, extractedRows);
 		context.close();
 	}
 
 	@Test
 	public void TablePtrTest()
 	{
 		String tableName = "importedreported";
 		DbContext context = initBasicContext(tableName);
 
 		List<TableRow> rows = new ArrayList<TableRow>();
 
 		Random random = new Random();
 
 		int numberOfRows = 5000;
 		for (int i = 0; i < numberOfRows; ++i)
 		{
 			List<String> args = new ArrayList<String>();
 			args.add(Integer.toString(random.nextInt(numberOfRows)));
 			args.add("gjghjf");
 			args.add("k");
 			TableRow tableRow = new TableRow(args);
 			rows.add(tableRow);
 		}
 
 		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
 		insertRowsCommand.executeCommand(context);
 
 		TableIterator tableIterator = new TableIterator(context, tableName);
 
 		List<Object> expected = tableIterator.nextRow();
 		TableEntryPtr tableEntryPtr = tableIterator.tableEntryPtr();
 
 		PageManager manager = PageManager.getInstance();
 
 		Table table = context.getTableByName(tableName);
 
 		HeapFile heapFile = new HeapFile(context.getLocation() + table.getRelativeDataPath(),
 			table.rowSignature());
 
 		PageId pageId = heapFile.localPageId(tableEntryPtr.pagePointer());
 		RowPage page = new RowPage(manager.getPage(pageId), DiskPage.NOT_BLANK_PAGE, heapFile.rowSize());
		manager.releasePage(pageId);
 		List<Object> actual = heapFile.selectRowFromPage(page, tableEntryPtr.rowPointer());
 
 		for (int i = 0; i < expected.size(); ++i)
 		{
 			Assert.assertEquals(expected.get(i), actual.get(i));
 		}
 
 		context.close();
 	}
 
 	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";
 }
