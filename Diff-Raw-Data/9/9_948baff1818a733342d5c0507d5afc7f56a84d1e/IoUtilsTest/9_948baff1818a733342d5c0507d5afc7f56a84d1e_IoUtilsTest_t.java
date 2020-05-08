 package gov.cida.sedmap.io;
 
 import static org.junit.Assert.*;
 
 import gov.cida.sedmap.io.FileDownloadHandler;
 import gov.cida.sedmap.io.IoUtils;
 import gov.cida.sedmap.io.MultiPartHandler;
 import gov.cida.sedmap.io.util.StrUtils;
 import gov.cida.sedmap.mock.MockConnection;
 import gov.cida.sedmap.mock.MockResponse;
 import gov.cida.sedmap.mock.MockResultSet;
 import gov.cida.sedmap.mock.MockStatement;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import javax.servlet.http.HttpServletResponse;
 
 import org.junit.Test;
 
 public class IoUtilsTest {
 
 
 	@Test
 	public void readTextResource() throws Exception {
 		String actual = IoUtils.readTextResource("/test.txt");
 		String expect = "test";
 		assertEquals("Expect file data", expect, actual);
 	}
 
 
 
 	@Test
 	public void writeFile_1files() throws Exception {
 		String data = "Test data for writeFile";
 		InputStream fileData = new ByteArrayInputStream(data.getBytes());
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		HttpServletResponse   res = new MockResponse(out);
 
 		FileDownloadHandler handler = new MultiPartHandler(res, out);
 
		handler.writeFile("text/csv", "foo.csv", fileData);
 		res.getWriter().flush();
 		IoUtils.quiteClose(out);
 		String actual = out.toString();
 
 		assertEquals("Expect contentType", 1, StrUtils.occurrences("Content-type: text/csv", actual));
 		assertTrue  ("filename was expected", actual.contains("filename=foo.csv") );
 		assertEquals("Expect one boundary ", 1, StrUtils.occurrences(MultiPartHandler.BOUNDARY_TAG, actual));
 		assertEquals("Expect file data", 1, StrUtils.occurrences(data, actual));
 
 		assertEquals("The last boundary terminator should not be present.",
 				-1, actual.lastIndexOf(MultiPartHandler.BOUNDARY_TAG+"--"));
 	}
 
 	@Test
 	public void writeFile_3files() throws Exception {
 		String data = "Test data for writeFile";
 		String data1 = data+" 1";
 		String data2 = data+" 2";
 		String data3 = data+" 3";
 		InputStream fileData1 = new ByteArrayInputStream(data1.getBytes());
 		InputStream fileData2 = new ByteArrayInputStream(data2.getBytes());
 		InputStream fileData3 = new ByteArrayInputStream(data3.getBytes());
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		HttpServletResponse   res = new MockResponse(out);
 
 		FileDownloadHandler handler = new MultiPartHandler(res, out);
 
		handler.writeFile("text/csv", "foo1.csv", fileData1);
		handler.writeFile("text/csv", "foo2.csv", fileData2);
		handler.writeFile("text/csv", "foo3.csv", fileData3);
 		res.getWriter().flush();
 		IoUtils.quiteClose(out);
 		String actual = out.toString();
 		System.out.println(actual);
 
 		assertEquals("Expect contentType", 3, StrUtils.occurrences("Content-type: text/csv", actual));
 		assertTrue  ("filename was expected", actual.contains("filename=foo1.csv") );
 		assertTrue  ("filename was expected", actual.contains("filename=foo2.csv") );
 		assertTrue  ("filename was expected", actual.contains("filename=foo3.csv") );
 		assertEquals("Expect one boundary ", 3, StrUtils.occurrences(MultiPartHandler.BOUNDARY_TAG, actual));
 		assertEquals("Expect file data", 3, StrUtils.occurrences(data, actual));
 		assertEquals("Expect file data 1", 1, StrUtils.occurrences(data1, actual));
 		assertEquals("Expect file data 2", 1, StrUtils.occurrences(data2, actual));
 		assertEquals("Expect file data 3", 1, StrUtils.occurrences(data3, actual));
 
 		assertEquals("The last boundary terminator should not be present.",
 				-1, actual.lastIndexOf(MultiPartHandler.BOUNDARY_TAG+"--"));
 	}
 
 
 
 
 	@Test
 	public void readStream_success() throws Exception {
 		String expect = "foo";
 		ByteArrayInputStream in = new ByteArrayInputStream(expect.getBytes());
 		String actual = IoUtils.readStream(in);
 		assertEquals("input and output streams should be identical", expect, actual);
 	}
 
 
 
 
 
 
 	@Test(expected=UnsupportedOperationException.class)
 	public void quiteClose_noCloseMethod() throws Exception {
 		IoUtils.quiteClose(this);
 	}
 	@Test
 	public void quiteClose_sqlConnection() throws Exception {
 		Connection test = new MockConnection();
 		assertFalse("Expect open", test.isClosed());
 		IoUtils.quiteClose(test);
 		assertTrue("Expect closed", test.isClosed());
 	}
 	@Test
 	public void quiteClose_sqlStatement() throws Exception {
 		Statement test = new MockStatement();
 		assertFalse("Expect open", test.isClosed());
 		IoUtils.quiteClose(test);
 		assertTrue("Expect closed", test.isClosed());
 	}
 	@Test
 	public void quiteClose_sqlResultSet() throws Exception {
 		ResultSet test = new MockResultSet();
 		assertFalse("Expect open", test.isClosed());
 		IoUtils.quiteClose(test);
 		assertTrue("Expect closed", test.isClosed());
 	}
 	@Test
 	public void quiteClose_sqlResultSet_notOpen() throws Exception {
 		ResultSet test = new MockResultSet();
 		test.close();
 		IoUtils.quiteClose(test);
 		// If throws exception then it is not working
 	}
 	@Test
 	public void quiteClose_closeable() throws Exception {
 		final boolean[] closed = {false};
 		Closeable test = new Closeable() {
 			@Override
 			public void close() throws IOException {
 				closed[0] = true;
 			}
 		};
 		// tisk tisk - closeable does not have isClosed
 		assertFalse("Expect open", closed[0]);
 		IoUtils.quiteClose(test);
 		assertTrue("Expect closed", closed[0]);
 	}
 	@Test
 	public void quiteClose_many() throws Exception {
 		Connection cn = new MockConnection();
 		Statement  st = new MockStatement();
 		ResultSet  rs = new MockResultSet();
 
 		final boolean[] closed = {false};
 		Closeable closeable = new Closeable() {
 			@Override
 			public void close() throws IOException {
 				closed[0] = true;
 			}
 		};
 		// tisk tisk - closeable does not have isClosed
 		assertFalse("Expect open", closed[0]);
 		assertFalse("Expect open", cn.isClosed());
 		assertFalse("Expect open", st.isClosed());
 		assertFalse("Expect open", rs.isClosed());
 
 		IoUtils.quiteClose(closeable, rs, st, cn);
 
 		assertTrue("Expect closed", closed[0]);
 		assertTrue("Expect closed", cn.isClosed());
 		assertTrue("Expect closed", st.isClosed());
 		assertTrue("Expect closed", rs.isClosed());
 	}
 
 
 }
