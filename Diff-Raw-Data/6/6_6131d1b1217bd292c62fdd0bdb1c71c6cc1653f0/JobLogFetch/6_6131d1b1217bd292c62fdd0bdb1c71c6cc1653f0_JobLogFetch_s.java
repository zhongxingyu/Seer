 package dataload;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.mapred.Counters;
 import org.apache.hadoop.mapred.JobClient;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.JobID;
 import org.apache.hadoop.mapred.JobStatus;
 import org.apache.hadoop.mapred.RunningJob;
 import org.apache.hadoop.mapred.TaskReport;
 
 /**
  * This is the class that queries the JobTracker for the job counters
  */
 public class JobLogFetch extends TimerTask {
 
 	private HashSet<String> jobsSeen;
 	private Connection connection;
 	private JobClient client;
 	private HashMap<String, Integer> mapJob;
 	private HashMap<String, Integer> mapCounter;
 	private long totalTime;
 
 	public JobLogFetch(String url, String user, String pwd, String coreSiteXml, String hdfsSiteXml, String mapredSiteXml) {
 		jobsSeen = new HashSet<String>();
 		mapJob = new HashMap<String, Integer>();
 		mapCounter = new HashMap<String, Integer>();
 		setupClientAndDatabase(url, user, pwd, coreSiteXml, hdfsSiteXml, mapredSiteXml);
 		makeJobParamMap();
 		makeJobCounterMap();
 	}
 	
 	/**
 	 * This method makes a HashMap which maps all the counter names to a unique number - it is user for insertions into the database
 	 */
 	public void makeJobParamMap() {
 		mapJob.put("BYTES_READ", 1);
 		mapJob.put("BYTES_WRITTEN", 2);
 		mapJob.put("COMBINE_INPUT_RECORDS", 3);
 		mapJob.put("COMBINE_OUTPUT_RECORDS", 4);
 		mapJob.put("COMMITTED_HEAP_BYTES", 5);
 		mapJob.put("CPU_MILLISECONDS", 6);
 		mapJob.put("DATA_LOCAL_MAPS", 7);
 		mapJob.put("FALLOW_SLOTS_MILLIS_MAPS", 8);
 		mapJob.put("FALLOW_SLOTS_MILLIS_REDUCES", 9);
 		mapJob.put("FILE_BYTES_READ", 10);
 		mapJob.put("FILE_BYTES_WRITTEN", 11);
 		mapJob.put("HDFS_BYTES_READ", 12);
 		mapJob.put("HDFS_BYTES_WRITTEN", 13);
 		mapJob.put("MAP_INPUT_RECORDS", 14);
 		mapJob.put("MAP_OUTPUT_BYTES", 15);
 		mapJob.put("MAP_OUTPUT_MATERIALIZED_BYTES", 16);
 		mapJob.put("MAP_OUTPUT_RECORDS", 17);
 		mapJob.put("PHYSICAL_MEMORY_BYTES", 18);
 		mapJob.put("RACK_LOCAL_MAPS", 19);
 		mapJob.put("REDUCE_INPUT_GROUPS", 20);
 		mapJob.put("REDUCE_INPUT_RECORDS", 21);
 		mapJob.put("REDUCE_OUTPUT_RECORDS", 22);
 		mapJob.put("REDUCE_SHUFFLE_BYTES", 23);
 		mapJob.put("SLOTS_MILLIS_MAPS", 24);
 		mapJob.put("SLOTS_MILLIS_REDUCES", 25);
 		mapJob.put("SPLIT_RAW_BYTES", 26);
 		mapJob.put("SPILLED_RECORDS", 27);
 		mapJob.put("TOTAL_LAUNCHED_MAPS", 28);
 		mapJob.put("TOTAL_LAUNCHED_REDUCES", 29);
 		mapJob.put("VIRTUAL_MEMORY_BYTES", 30);
 	}
 
 	/**
 	 * Performs same function as previous method but is for task level, rather than job level parameters
 	 */
 	public void makeJobCounterMap() {
 		mapCounter.put("BYTES_READ", 1);
 		mapCounter.put("BYTES_WRITTEN", 2);
 		mapCounter.put("COMBINE_INPUT_RECORDS", 3);
 		mapCounter.put("COMBINE_OUTPUT_RECORDS", 4);
 		mapCounter.put("COMMITTED_HEAP_BYTES", 5);
 		mapCounter.put("CPU_MILLISECONDS", 6);
 		mapCounter.put("FILE_BYTES_READ", 7);
 		mapCounter.put("FILE_BYTES_WRITTEN", 8);
 		mapCounter.put("HDFS_BYTES_READ", 9);
 		mapCounter.put("HDFS_BYTES_WRITTEN", 10);
 		mapCounter.put("MAP_INPUT_BYTES", 11);
 		mapCounter.put("MAP_INPUT_RECORDS", 12);
 		mapCounter.put("MAP_OUTPUT_BYTES", 13);
 		mapCounter.put("MAP_OUTPUT_MATERIALIZED_BYTES", 14);
 		mapCounter.put("MAP_OUTPUT_RECORDS", 15);
 		mapCounter.put("PHYSICAL_MEMORY_BYTES", 16);
 		mapCounter.put("REDUCE_INPUT_GROUPS", 17);
 		mapCounter.put("REDUCE_INPUT_RECORDS", 18);
 		mapCounter.put("REDUCE_OUTPUT_RECORDS", 19);
 		mapCounter.put("REDUCE_SHUFFLE_BYTES", 20);
 		mapCounter.put("SPLIT_RAW_BYTES", 21);
 		mapCounter.put("SPILLED_RECORDS", 22);
 		mapCounter.put("VIRTUAL_MEMORY_BYTES", 23);
 	}
 	
 	/**
 	 * Method to setup a link between the class and the JobTracker through the XML files and to create the link to the database
 	 * @param url
 	 * @param user
 	 * @param pwd
 	 * @param coreSiteXml
 	 * @param hdfsSiteXml
 	 * @param mapredSiteXml
 	 */
 	public void setupClientAndDatabase(String url, String user, String pwd, String coreSiteXml, String hdfsSiteXml, String mapredSiteXml) {
 		try {
 			JobConf conf = new JobConf();
 
 			conf.addResource(new Path(coreSiteXml));
 			conf.addResource(new Path(hdfsSiteXml));
 			conf.addResource(new Path(mapredSiteXml));
 
 			client = new JobClient(conf);
 			connection = DriverManager.getConnection(url, user, pwd);
 
 		} catch ( SQLException e ) {
 			e.printStackTrace();
 			System.err.println("Error: " + e.getMessage());
 		} catch (IOException e ) {
 			e.printStackTrace();
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 	
 	/**
 	 * This method is only run once, and it creates the jobParameters table which keeps track of all the job level parameters
 	 */
 	public void makeJobTable() {
 		try {
 			Statement statement = connection.createStatement();
 			statement.executeUpdate("CREATE TABLE jobParameters (Bytes_Read BIGINT(20) UNSIGNED, Bytes_Written BIGINT(20) UNSIGNED, Combine_Input_Records BIGINT(20) UNSIGNED, Combine_Output_Records BIGINT(20) UNSIGNED," +
 					" Committed_Heap_Bytes BIGINT(20) UNSIGNED, CPU_Milliseconds BIGINT(20) UNSIGNED, Data_Local_Maps BIGINT(20) UNSIGNED, Fallow_Slots_Millis_Maps BIGINT(20) UNSIGNED, Fallow_Slots_Millis_Reduces BIGINT(20) UNSIGNED," +
 					" File_Bytes_Read BIGINT(20) UNSIGNED, File_Bytes_Written BIGINT(20) UNSIGNED, HDFS_Bytes_Read BIGINT(20) UNSIGNED, HDFS_Bytes_Written BIGINT(20) UNSIGNED, Map_Input_Records BIGINT(20) UNSIGNED," +
 					" Map_Output_Bytes BIGINT(20) UNSIGNED, Map_Output_Materialized_Bytes BIGINT(20) UNSIGNED, Map_Output_Records BIGINT(20) UNSIGNED, Physical_Memory_Bytes BIGINT(20) UNSIGNED, Rack_Local_Maps BIGINT(20) UNSIGNED," +
 					" Reduce_Input_Groups BIGINT(20) UNSIGNED, Reduce_Input_Records BIGINT(20) UNSIGNED, Reduce_Output_Records BIGINT(20) UNSIGNED, Reduce_Shuffle_Bytes BIGINT(20) UNSIGNED, Slots_Millis_Maps BIGINT(20) UNSIGNED," +
 					" Slots_Millis_Reduces BIGINT(20) UNSIGNED, Split_Raw_Bytes BIGINT(20) UNSIGNED, Spilled_Records BIGINT(20) UNSIGNED, Total_Launched_Maps BIGINT(20) UNSIGNED, Total_Launched_Reduces BIGINT(20) UNSIGNED, Virtual_Memory_Bytes BIGINT(20) UNSIGNED," +
 					" JobID VARCHAR(100), Job_Name VARCHAR(100), Total_Time BIGINT(20))");
 		} catch ( SQLException e ) {
 			e.printStackTrace();
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * This method is the method run by the timer
 	 */
 	public void run() {
 		try {
 			for ( JobStatus stat : client.getAllJobs() ) {				
				if ( stat.getUsername().equals("data_svc") && !jobsSeen.contains(stat.getJobID().toString()) ) {
 					totalTime = 0;
 					makeTable(stat.getJobID());
 					writeMapAndReduceCounters(stat.getJobID());
 					getJobParameters(stat.getJobID());
 					jobsSeen.add(stat.getJobID().toString());
 				}
 			}
 		} catch ( SQLException e )
 		{
 			e.printStackTrace();
 			System.err.println("Error: " + e.getMessage());
 		} catch ( IOException e ) {
 			e.printStackTrace();
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * This is the method to get all of the jobParameters for a given job
 	 * @param id
 	 * @throws IOException
 	 * @throws SQLException
 	 */
 	public void getJobParameters(JobID id) throws IOException, SQLException {
 		RunningJob job = client.getJob(id);
 		Counters c = job.getCounters();
 		Iterator<Counters.Group> itrG = c.iterator();
 
 		PreparedStatement prepStatement = connection.prepareStatement("INSERT INTO jobParameters VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 		for ( int i = 1; i < 31; i++ ) { prepStatement.setLong(i, 0); } 
 		
 		prepStatement.setString(31, job.getID().toString());
 		prepStatement.setString(32, job.getJobName());
 		prepStatement.setLong(33, totalTime);
 		
 		while ( itrG.hasNext() ) {
 			Iterator<Counters.Counter> itrC = itrG.next().iterator();
 
 			while ( itrC.hasNext() ) {
 				Counters.Counter counter = itrC.next();
 				if ( mapJob.get(counter.getName()) != null ) { prepStatement.setLong(mapJob.get( counter.getName()), counter.getCounter()); }
 			}
 		}
 		prepStatement.executeUpdate();
 	}
 
 	/**
 	 * This method creates a new table for each job
 	 * @param id
 	 */
 	public void makeTable(JobID id) {		
 		try {
 			Statement statement = connection.createStatement();
 			statement.executeUpdate("CREATE TABLE " + id + " (Bytes_Read BIGINT(20) UNSIGNED, Bytes_Written BIGINT(20) UNSIGNED, Combine_Input_Records BIGINT(20) UNSIGNED, Combine_Output_Records BIGINT(20) UNSIGNED," +
 					" Committed_Heap_Bytes BIGINT(20) UNSIGNED, CPU_Milliseconds BIGINT(20) UNSIGNED, File_Bytes_Read BIGINT(20) UNSIGNED, File_Bytes_Written BIGINT(20) UNSIGNED, HDFS_Bytes_Read BIGINT(20) UNSIGNED," +
 					" HDFS_Bytes_Written BIGINT(20) UNSIGNED, Map_Input_Bytes BIGINT(20) UNSIGNED, Map_Input_Records BIGINT(20) UNSIGNED, Map_Output_Bytes BIGINT(20) UNSIGNED, Map_Output_Records BIGINT(20) UNSIGNED," +
 					" Map_Output_Materialized_Bytes BIGINT(20) UNSIGNED, Physical_Memory_Bytes BIGINT(20) UNSIGNED, Reduce_Input_Groups BIGINT(20) UNSIGNED, Reduce_Input_Records BIGINT(20) UNSIGNED," +
 					" Reduce_Shuffle_Bytes BIGINT(20) UNSIGNED, Reduce_Output_Records BIGINT(20) UNSIGNED, Split_Raw_Bytes BIGINT(20) UNSIGNED, Spilled_Records BIGINT(20) UNSIGNED, Virtual_Memory_Bytes BIGINT(20) UNSIGNED," +
 					" TaskID VARCHAR(100), Task_Time BIGINT(20))");
 		} catch ( SQLException e ) {
 			e.printStackTrace();
 			System.err.println(e.getMessage());
 		}
 	}
 
 	/**
 	 * This is a helper method to regulate how the counters are written into the database
 	 * @param id
 	 * @throws IOException
 	 * @throws SQLException
 	 */
 	public void writeMapAndReduceCounters(JobID id) throws IOException, SQLException {
 		TaskReport[] mapReports = client.getMapTaskReports(id);
 		TaskReport[] reduceReports = client.getReduceTaskReports(id);
 		PreparedStatement prepStatement = null;
 		insertTaskIntoTable(prepStatement, mapReports, id);
 		insertTaskIntoTable(prepStatement, reduceReports, id);		
 	}
 
 	/**
 	 * This does the insertion of a given Task Report into the table
 	 * @param prepStatement
 	 * @param reports
 	 * @param id
 	 * @throws SQLException
 	 */
 	public void insertTaskIntoTable(PreparedStatement prepStatement, TaskReport[] reports, JobID id) throws SQLException {
 		for ( TaskReport rep : reports ) {
 			Counters c = rep.getCounters();
 			Iterator<Counters.Group> itrG = c.iterator();
 
 			prepStatement = connection.prepareStatement("INSERT INTO " + id + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
 			for ( int i = 1; i < 24; i++ ) { prepStatement.setLong(i, 0); }
 			
 			prepStatement.setString(24, rep.getTaskID().toString());
 			prepStatement.setLong(25, 0);
 			
 			if ( !(rep.getFinishTime() == 0) && !(rep.getStartTime() == 0) ) {
 				prepStatement.setLong(25, (rep.getFinishTime() - rep.getStartTime()) / 1000);
 				totalTime += (rep.getFinishTime() - rep.getStartTime());
 			} else {
 				prepStatement.setLong(25, 0);
 			}
 			
 			while ( itrG.hasNext() ) {
 				Iterator<Counters.Counter> itrC = itrG.next().iterator();
 
 				while ( itrC.hasNext() ) {
 					Counters.Counter counter = itrC.next();
 					if ( mapCounter.get(counter.getName()) != null ) { prepStatement.setLong(mapCounter.get(counter.getName()), counter.getCounter()); }
 				}
 			}
 			prepStatement.executeUpdate();
 		}
 	}
 	
 	/**
 	 * This class should be run continuously on a server, it is currently setup to query the JobTracker every 15 minutes
 	 * @param args[0] is the link to the SQL database
 	 * @param args[1] is the user for the SQL database
 	 * @param args[2] is the password for the SQL user
 	 */
 	public static void main(String args[]) {
 		JobLogFetch listener = new JobLogFetch(args[0], args[1], args[2], "core-site.xml", "hdfs-site.xml", "mapred-site.xml");
 		listener.makeJobTable();
 		
 		Timer t = new Timer();
 		t.scheduleAtFixedRate(listener, 0, 1000*60*15);
 	}
}
