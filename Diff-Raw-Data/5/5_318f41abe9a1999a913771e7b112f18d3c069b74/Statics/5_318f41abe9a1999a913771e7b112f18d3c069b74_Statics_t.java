 package de.reneruck.tcd.ipp.datamodel;
 
 public class Statics {
 	public static final String DB_FILE = "Database.db";
 	
 	public static final int DB_SERVER_PORT = 4446;
 	public static final int DISCOVERY_PORT = 5899;
 
	public static final int INTER_SERVER_COM_PORT = 5447;
 	public static final int CLIENT_PORT = 4447;
 	public static final int CLIENT_DISCOVERY_PORT = 6899;
 	
 	public static final String ACK = "ACK";
 	public static final String SYN = "SYN";
 	public static final String SYNACK = "SYNACK";
 	public static final String FINACK = "FINACK";
 	public static final String FIN = "FIN";
 	public static final String RX_SERVER_ACK = "RX_SERVER_ACK";
 	public static final String RX_HELI_ACK = "RX_HELI_ACK";
 	public static final String RX_SERVER =  "RX_SERVER";
 	public static final String RX_HELI =  "RX_HELI";
 	public static final String SHUTDOWN = "SHUTDOWN";
 	public static final String FINISH_RX_HELI = "FINISH_RX_HELI";
 	public static final String FINISH_RX_SERVER = "FINISH_RX_SERVER";
 	public static final String DATA = "DATA";
	public static final String ERR = "ERROR";
 	
 	public static final String TRAMSITION_ID = "transitionId";
 	public static final int MAX_PASSENGERS = 6;
 
 	public static final String DB_SCHMEA_FILE = "initDBStructure.sql";
 	public static final String DB_INIT_DATA_FILE = "InitData.sql";

 	public static String CONTENT_TRANSITION = "transition";
 }
