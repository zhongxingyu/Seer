 package org.liveSense.api.sql;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
 import org.apache.commons.dbcp.BasicDataSource;
 import org.hsqldb.Server;
 import org.hsqldb.persist.HsqlProperties;
 import org.junit.Test;
 import org.liveSense.api.beanprocessors.TestBean;
 import org.liveSense.misc.queryBuilder.QueryBuilder;
 import org.liveSense.misc.queryBuilder.SimpleSQLQueryBuilder;
 
 public class SQLExecuteTest {
 
 	private BasicDataSource dataSource;
 	private Connection connection;
 	private StringBuffer blob = new StringBuffer();
 	private Date date = new Date();
 	private Date dateWithoutTime = null;
 	private TestBean bean;
 	private static final String BLOBTEXT = "dfsdfsdf";
 
 	public static void dropTable(
 		Connection connection,
 		String tableName) {
 
 		try {
 			executeSql(connection, "drop table " + tableName);
 			connection.commit();
 		}
 		catch (SQLException se) {
 			// if the table doesn't exist, we'll get an exception.
 			// Ignore it & continue.
 			System.out.println(se.getMessage());
 		}
 	}
 
 	public static void executeSql(
 		Connection connection,
 		String sql)
 		throws SQLException {
 
 		Statement statement = connection.createStatement();
 		statement.execute(sql);
 	}
 
 	@Test
 	public void testFirebird()
 		throws Exception {
 
 		dataSource = new BasicDataSource();
 		dataSource.setDriverClassName("org.firebirdsql.jdbc.FBDriver");
 		dataSource.setDefaultAutoCommit(false);
 		String path = new java.io.File(".").getCanonicalPath() + "/target/test-classes/EMPTYDATABASE.FDB";
 		dataSource.setUrl("jdbc:firebirdsql:localhost/3050:" + path + "?charSet=UTF-8");
 		dataSource.setUsername("sysdba");
 		dataSource.setPassword("masterkey");
 		connection = dataSource.getConnection();
 		dropTable(connection, "BeanTest1");
 		
 		// Create table from annotation
 		@SuppressWarnings("unchecked") SQLExecute<TestBean> x =
 			(SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource);
 		x.createTable(connection, TestBean.class);
 		
 		if (!x.existsTable(connection, TestBean.class))
 				throw new SQLException("Table does not exists");
 		
 		x.dropTable(connection, TestBean.class);
 		
 		if (x.existsTable(connection, TestBean.class))
 			throw new SQLException("Table exists after drop");
 		
 		dropTable(connection, "BeanTest1");
 		
 		executeSql(connection, "create table BeanTest1 (" + 
 			"ID integer, " + 
 			"ID_CUSTOMER integer, "+ 
 			"PASSWORD_ANNOTATED varchar(20), " + 
 			"FOUR_PART_COLUMN_NAME integer, "+ 
 			"DATE_FIELD_WITHOUT_ANNOTATION date," + 
 			"BLOB_FIELD blob, "+
 			"FLOAT_FIELD NUMERIC(15,2) )");
 		connection.commit();
 
 		testExecute();
 		dataSource.close();
 	}
 
 	@Test
 	public void testHsqlDb()
 		throws Exception {
 
 		// Start HSQL Server
 		final Server hsqlServer = new Server();
 		hsqlServer.setDatabaseName(0, "testDb");
 		hsqlServer.setAddress("127.0.0.1");
 		hsqlServer.setDatabasePath(0, "file:./target/test-classes/testDb");
 		
 		hsqlServer.start();
 		dataSource = new BasicDataSource();
 		dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
 		dataSource.setUrl("jdbc:hsqldb:hsql://localhost/testDb");
 		dataSource.setUsername("sa");
 		dataSource.setPassword("");
 		dataSource.setDefaultAutoCommit(false);
 		connection = dataSource.getConnection();
 		executeSql(connection, "SET PROPERTY \"sql.enforce_strict_size\" true");
 		
 		dropTable(connection, "BeanTest1");
 		executeSql(connection, "create table BeanTest1 (" + 
 			"ID integer, " + 
 			"ID_CUSTOMER integer, " +
 			"PASSWORD_ANNOTATED varchar(20), " + 
 			"FOUR_PART_COLUMN_NAME integer, " +
 			"DATE_FIELD_WITHOUT_ANNOTATION date," + 
 			"BLOB_FIELD longvarchar, " +
 			"FLOAT_FIELD numeric (15,2) )");
 		connection.commit();
 
 		testExecute();
 		dataSource.close();
 		hsqlServer.stop();
 	}
 
 	public void testExecute()
 		throws Exception {
 
 		for (int i = 0; i < 1000; i++) {
 			// blob.append("ÁRVÍZTŰRŐTÜKÖRFÚRÓGÉPárvíztűrőtükörfúrógépНаཤེལ་སྒོ་ཟ་ནས་ང་ན་གི་མ་རེད།"
 			// האקדמיה ללשון העבריберегу пустынных волнயாமறிந்த மொழிகளிலே
 			// தமிழ்மொழி போல் இனிதாவது எங்கும் காணோம்Я можу їсти шкло, й воно
 			// мені не пошкодить私はガラスを食べられます。それは私を傷つけません");
 			blob.append("ÁRVÍZTŰRŐTÜKÖRFÚRÓGÉPárvíztűrőtükörfúrógépཤེལ་སྒོ་ཟ་ནས་ང་ན་གི་མ་རེདஇனிதாவதுהאקדמיה ללשון העבריь私はガラスを食べられます");
 		}
 		dateWithoutTime = new SimpleDateFormat("yyyy.MM.dd").parse("2011.01.03");
 
 		@SuppressWarnings("unchecked") SQLExecute<TestBean> x =
 			(SQLExecute<TestBean>) SQLExecute.getExecuterByDataSource(dataSource);
 
		//dropTable(connection, "T1");
		x.executeScript(connection, new File("./target/test-classes/test.sql"), "Drop");
 		x.executeScript(connection, new File("./target/test-classes/test.sql"), "Create");
		
		
 		// Insert data with JDBC
 		executeSql(
 			connection,
 			"INSERT INTO BeanTest1(ID, ID_CUSTOMER, PASSWORD_ANNOTATED, FOUR_PART_COLUMN_NAME, DATE_FIELD_WITHOUT_ANNOTATION, BLOB_FIELD, FLOAT_FIELD)" +
 				" values (1, 1, 'password', 1, '2011-01-03', '" + BLOBTEXT + "', 0.3)");
 
 		// Insert data with bean
 		bean = new TestBean();
 		bean.setId(2);
 		bean.setIdCustomer(2);
 		bean.setConfirmationPassword("password");
 		bean.setFourPartColumnName(true);
 		bean.setDateFieldWithoutAnnotation(date);
 		bean.setBlob(blob.toString());
 		bean.setFloatField(new Double(1.0f/3.0f));
 		
 		x.insertEntity(connection, bean);
 
 		// Query all record
 		QueryBuilder builder = new SimpleSQLQueryBuilder("SELECT * FROM BeanTest1");
 		List<TestBean> res = x.queryEntities(connection, TestBean.class, builder);
 
 		assertEquals("Entity name", "BeanTest1", AnnotationHelper.getTableName(res.get(0)));
 		assertEquals("Resultset size", 2, res.size());
 
 		assertEquals("ID", new Integer(1), res.get(0).getId());
 		assertEquals("ID_CUSTOMER", new Integer(1), res.get(0).getId());
 		assertEquals("PASSWORD_ANNOTATED", "password", res.get(0).getConfirmationPassword());
 		assertEquals("FOUR_PART_COLUMN_NAME", new Boolean(true), res.get(0).getFourPartColumnName());
 		assertEquals("BLOB_FIELD", BLOBTEXT, res.get(0).getBlob());
 		assertNotNull("DATE_FIELD_WITHOUT_ANNOTATION", res.get(0).getDateFieldWithoutAnnotation());
 		assertEquals("DATE_FIELD_WITHOUT_ANNOTATION", dateWithoutTime, res.get(0).getDateFieldWithoutAnnotation());
 		assertEquals("FLOAT_FIELD", 0.3, res.get(0).getFloatField().doubleValue(), 0.0f);
 		
 		assertEquals("ID", new Integer(2), res.get(1).getId());
 		assertEquals("ID_CUSTOMER", new Integer(2), res.get(1).getId());
 		assertEquals("PASSWORD_ANNOTATED", "password", res.get(1).getConfirmationPassword());
 		assertEquals("FOUR_PART_COLUMN_NAME", new Boolean(true), res.get(1).getFourPartColumnName());
 		assertEquals("BLOB_FIELD", blob.toString(), res.get(1).getBlob());
 		assertEquals("DATE_FIELD_WITHOUT_ANNOTATION", (Date) null, res.get(1).getDateFieldWithoutAnnotation());
 		assertEquals("FLOAT_FIELD", 0.33, res.get(1).getFloatField().doubleValue(), 0.0f);
 
 		// Updating bean
 		res.get(1).setConfirmationPassword("testupdate");
 		x.updateEntity(connection, res.get(1));
 		// Query all records
 		res = x.queryEntities(connection, TestBean.class, builder);
 		assertEquals("PASSWORD_ANNOTATED", "testupdate", res.get(1).getConfirmationPassword());
 
 		// Delete bean
 		x.deleteEntity(connection, res.get(1));
 		// Query all records
 		res = x.queryEntities(connection, TestBean.class, builder);
 		assertEquals("Resultset size", 1, res.size());
 
 		connection.commit();
 		connection.close();
 	}
 }
