 package bixi.hbase.query;
 /**
  * A class containing constants for client side Bixi.
  * @author hv
  */
 public class BixiConstant {
   /**
    * 
    */
   public final static double MONTREAL_LAT = 45.508183d;
   
   /**
    * 
    */
   public final static double MONTREAL_LON = -73.554094d;
       
   public final static byte[] FAMILY = "Data".getBytes();
   
	public static String SCHEMA1_TABLE_NAEM = "BixiData";
 	public static String SCHEMA1_FAMILY_NAME = "Data";  
 	
 	public static String SCHEMA2_CLUSTER_TABLE_NAME = "schema2_cluster";
 	public static String SCHEMA2_CLUSTER_FAMILY_NAME = "s";
 	public static String SCHEMA2_BIKE_TABLE_NAME = "schema2_bike";
 	public static String SCHEMA2_BIKE_FAMILY_NAME = "t";
 	
 	public static String ID_DELIMITER = "#";
   
   
 }
