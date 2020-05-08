 package calico;
 
 import java.awt.*;
 import java.net.ServerSocket;
 import java.nio.ByteOrder;
 import org.apache.commons.vfs.*;
 import org.apache.commons.vfs.impl.*;
 
 public class COptions
 {
 	public static class listen
 	{
 		public static String host = "0.0.0.0";
 		public static int port = 27000;
 	}
 	
 	public static class debug
 	{
 		public static boolean enabled = false;
 		public static boolean consistency_debug_enabled = true;
 		
 		public static class unittests
 		{
 			public static int bound_width = 800;
 			public static int bound_height = 600;
 			public static Color[] stroke_colors = {Color.RED, Color.BLUE, Color.BLACK, Color.GREEN, Color.PINK};
 		}
 	}
 
 
 	public static class client
 	{
 		public static class threadopts
 		{
 			public static long sleeptime = 5L;
 		}
 		public static class network
 		{
 			public static long timeout = 15000L;
 			public static int cluster_size = 400;// this is the number of coordinates to group together
 		}
 	}
 
 
 	public static class admin
 	{
 		public static String server_signature = "Calico Server API/1.1";
 		public static ServerSocket serversocket;
 		
 		public static class listen
 		{
 			public static String host = "0.0.0.0";
 			public static int port = 27001;
 			public static int timeout = 15000;
 			public static int buffer = 8192;
 			public static boolean tcp_nodelay = true;
 			public static boolean stale_conn_check = false;
 		}
 	}
 
 
 	public static class canvas
 	{
 		public static int max_snapshots = 50;
 		public static long sleeptime = 10L; // Must be less than 5000
 		public static int max_sleep_count = (int) (5000 / sleeptime);
 		public static int width = 1600;
 		public static int height = 1200;
 	}
 	
 	public static class uuid
 	{
 		public static int block_size = 300;
 		public static int allocation_increment = 500;
 		public static int min_size = 500;
 	}
 	
 	public static class server
 	{
 		public static int tickrate = 66;
 		public static class backup
 		{
 			public static boolean enable_autobackup = true;
 			public static int write_on_tick = 50; // tickrate * <thisnum>
 			public static String backup_file = "backup_auto.csb";
			public static String backup_file_alt = "backup_auto";
 		}
 		public static class images
 		{
 			public static String download_folder = "uploads/images/";
 		}
 		
 		public static String plugins = "";
 	}
 	
 	public static class group
 	{
 		public static int padding = 10;
 		public static int text_padding = 0;
 		public static Font font = new Font("Helvetica", Font.PLAIN, 14);
 	}
 	
 	public static class stroke
 	{
 		public static Color default_color = Color.BLACK;
 		public static float default_thickness = 1.0f;
 	}
 
 	////////////////////////////////////////////////////////////
 
 	// Year-month-day hour:min:sec.mil
 	//public static final String LOG_FORMAT_STD = "[%p] %d{ISO8601} %m";
 	public static final String LOG_FORMAT_STD = "[%p] %d{yyyy-MM-dd HH:mm:ss.SSS} %m%n";
 	public static final String LOG_FORMAT_DATEMSG = "%d{yyyy-MM-dd HH:mm:ss.SSS} %m%n";
 	public static final String DATEFILE_FMT = "'.'yyyy-MM-dd";
 
 	
 	public static String log_path = "../logs/"; 
 	
 	public static int GridRows = 7;
 	public static int GridCols = 7;
 	
 	public static String APIDefaultContentType = "text/plain";
 	
 	
 	public static int DuplicateGroupShiftDelta = 20;
 	
 	
 	public static DefaultFileSystemManager fs;
 	
 	
 	public static long ServerStartTime = 0L;
 	
 	public String testing = "testing";
 	
 }//
 
