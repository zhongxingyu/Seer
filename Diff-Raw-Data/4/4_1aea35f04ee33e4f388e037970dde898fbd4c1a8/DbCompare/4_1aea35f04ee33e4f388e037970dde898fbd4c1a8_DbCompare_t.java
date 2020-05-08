 package com.andre.dbcompare;
 
 import java.util.*;
 import java.io.*;
 import java.sql.*;
 import javax.sql.*;
 import org.apache.log4j.Logger;
 //import com.beust.jcommander.JCommander;
 //import com.beust.jcommander.Parameter;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.ApplicationContext;
 
 public class DbCompare {
 	private static Logger logger = Logger.getLogger(DbCompare.class);
	//private Options options = new Options();
	//private JCommander jcommander;
 
 	private String [] configFiles = { "appContext.xml" };
 	private List<String> queries ;
 	private String username;
 	private String password;
 	private DataSource dataSource1;
 	private DataSource dataSource2;
 
 
 	public static void main(String [] args) throws Exception {
 		(new DbCompare()).process(args);
 	}
 
 	void process(String [] args) throws Exception {
 		initSpring();
 		for (String query : queries)
 			process(query);
 	}
 
 	void process(String query) throws Exception {
 		Connection conn1 = dataSource1.getConnection();
 		Connection conn2 = dataSource2.getConnection();
 
 		MyRunnable task1 = new MyRunnable(conn1,query);
 		Thread thread1 = new Thread(task1);
 		MyRunnable task2 = new MyRunnable(conn2,query);
 		Thread thread2 = new Thread(task2);
 
 		thread1.start();
 		thread2.start();
 		thread2.join();
 
 		ResultSetMetaData meta = task1.rs.getMetaData();
 		int numCols = meta.getColumnCount();
 		int numRows;
 		boolean hasError = false;
 		for (numRows=0 ; task1.rs.next() && task2.rs.next() && !hasError ; numRows++) {
 			for (int col=1 ; col <= numCols && !hasError; col++) {
 				Object o1 = task1.rs.getObject(col) ;
 				Object o2 = task2.rs.getObject(col) ;
 				if (!o1.equals(o2)) {
 					error("Row="+numRows+" Column "+col+" is not the same: col1="+o1+" col2="+o2+" Query="+query);
 					hasError = true ;
 				}
 			}
 		}
 		if (! hasError) {
 			if (task1.rs.next() || task1.rs.next()) {
 				error("Result sets are not the same size. Query="+query);
 				return ;
 			}
 			info("OK: #rows="+numRows+" Query="+query);
 		}
 
 		task1.rs.close();
 		task2.rs.close();
 	}
 
 	class MyRunnable implements Runnable {
 		public String query;
 		public Connection conn;
 		public ResultSet rs ;
 
 		public MyRunnable(Connection conn, String query) {
 			this.query = query;
 			this.conn = conn;
 		}
 
 		public void run() {
 			try {
 				process(query);
 			} catch (Exception e) {
 				error("query="+query+" ex="+e);
 			}
 		}
 
 		void process(String query) throws Exception {
 			long t0=System.currentTimeMillis();
 			try {
 				long t1=System.currentTimeMillis();
 				Statement stmt = conn.createStatement() ;
 				rs = stmt.executeQuery(query);
 				long timeConn=t1-t0;
 				long timeExecute=System.currentTimeMillis()-t1;
 				long timeTotal=System.currentTimeMillis()-t0;
 				//logger.debug("timeTotal="+timeTotal+" timeConn="+timeConn+" timeExecute="+timeExecute);
 			} finally {
 				//if (conn != null) conn.close();
 			}
 		}
 
 	}
 
 	private	void initSpring() throws Exception {
 		//logger.debug("configFiles="+Arrays.toString(configFiles));
 		ApplicationContext context = new ClassPathXmlApplicationContext(configFiles);
 		dataSource1 = context.getBean("dataSource1",DataSource.class);
 		dataSource2 = context.getBean("dataSource2",DataSource.class);
 		queries = (List<String>)context.getBean("queries");
 	}
 
 /*
 	class Options {
 		@Parameter(names = { "-u", "--url" }, description = "Portal URL", required = true )
 		public String url ;
 
 		@Parameter(names = { "-i", "--iterations" }, description = "Iterations")
 		public int iterations = 1;
 
 		@Parameter(names = { "-h", "--help" }, description = "Help")
 		public boolean help = false;
 	}
 
 	void usage() {
 		jcommander.usage();
 	}
 */
 
 	void info(Object o) { System.out.println(o);}
 	void debug(Object o) { System.out.println(""+o);}
 	void error(Object o) { System.out.println("ERROR: "+o);}
 }
