 package edu.berkeley.gamesman.hadoop.util;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 
 import java.net.URI;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.database.SolidDatabase;
 import edu.berkeley.gamesman.database.SplitSolidDatabase;
 import edu.berkeley.gamesman.hadoop.TierMap;
 /**
  * Utilities for the Hadoop master
  * @author Patrick Reiter Horn
  */
 public class HadoopUtil {
 	/**
 	 * MapReduceDatabase abstract parent for children 
 	 *
 	 */
 	public static abstract class MapReduceDatabase extends SplitSolidDatabase {
 		/**
 		 * Default constructor, so this can be instantiated from a class name.
 		 */
 		public MapReduceDatabase() {
 		}
 		
 		protected void startedWrite(int tier, SolidDatabase db, long startRecord, long endRecord) {
 			if (delegate != null) {
 				String file = db.getUri();
 				int lastSlash = file.lastIndexOf('/');
 				if (lastSlash >= 0) {
 					file = file.substring(lastSlash+1);
 				}
 				delegate.started(tier, file, startRecord, endRecord);
 			}
 		}
 		
 		protected void finishedWrite(int tier, SolidDatabase db, long startRecord, long endRecord) {
 			if (delegate != null) {
 				String file = db.getUri();
 				int lastSlash = file.lastIndexOf('/');
 				if (lastSlash >= 0) {
 					file = file.substring(lastSlash+1);
 				}
 				delegate.finished(tier, file, startRecord, endRecord);
 			}
 		}
 
 		/**
 		 * Convenience constructor. Equivalent to calling setFilesystem
 		 * @param fs Reference to hadoop FileSystem.
 		 */
 		public MapReduceDatabase(FileSystem fs) {
 			this.fs = fs;
 		}
 	
 		/**
 		 * All hadoop classes that need to access the disk need a FileSystem instance.
 		 * Must be set before the database is used.
 		 * @param fs The hadoop filesystem.
 		 */
 		public void setFilesystem(FileSystem fs) {
 			this.fs = fs;
 		}
 	
 		/**
 		 * Called by the mapper to allow the database to communicate via
 		 * TierMap.started() and TierMap.finished().
 		 * @param tmr TierMap instance.
 		 */
 		public void setDelegate(TierMap<?> tmr) {
 			this.delegate = tmr;
 		}
 	
 		/**
 		 * Called by the mapper to tell the database where to dump output files.
 		 * @param dir FileOutputFormat.getWorkOutputPath(jobconf));
 		 */
 		public void setOutputDirectory(Path dir) {
 			setOutputDirectory(dir.toString());
 		}
 	
 		protected TierMap<?> delegate;
 	
 		protected FileSystem fs;
 	
 		protected Path outputFilenameBase;
 	}
 
 	/**
 	 * @param gmConf Gamesman Configuration
 	 * @return The toplevel hadoop solve directory.
 	 */
 	public static Path getParentPath(org.apache.hadoop.conf.Configuration hadoopConfig, Configuration gmConf) {
 		URI uri = FileSystem.getDefaultUri(hadoopConfig);
		//System.out.println("URI Is "+uri+", scheme: "+uri.getScheme()+
		//	", host: "+uri.getAuthority()+", path: "+uri.getPath());
		return new Path(uri.getScheme(), uri.getAuthority(), gmConf.getProperty("gamesman.db.uri"));
 	}
 	/**
 	 * @param tier The tier number in the solve process
 	 * @return The directory name for that tier (currently assumes 2 digits).
 	 */
 	public static String getTierDirectoryName(int tier) {
 		return String.format("tier%02d", tier);
 	}
 	/**
 	 * @param gmConf Gamesman Configuration
 	 * @param tier The tier number in the solve process
 	 * @return The full Path to the directory for that tier.
 	 */
 	public static Path getTierPath(org.apache.hadoop.conf.Configuration hadoopConfig, Configuration gmConf, int tier) {
 		return new Path(getParentPath(hadoopConfig, gmConf), getTierDirectoryName(tier));
 	}
 
 	/**
 	 * @param tier The tier number in the solve process
 	 * @return The name of the file that contains the index in the database.
 	 */
 	public static String getTierIndexFilename(int tier) {
 		return "part-00000";//tier+".hdb";
 	}
 
 	/**
 	 * @param gmConf Gamesman Configuration
 	 * @param tier The tier number in the solve process
 	 * @return The full path to the file that contains the index in the database.
 	 */
 	public static Path getTierIndexPath(org.apache.hadoop.conf.Configuration hadoopConfig, Configuration gmConf, int tier) {
 		return new Path(getTierPath(hadoopConfig, gmConf, tier), getTierIndexFilename(tier));
 	}
 }
