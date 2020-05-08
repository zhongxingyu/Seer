 package ch.cern.atlas.apvs.ptu.server;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public interface PtuConstants {
 	public static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 	public static final DateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final String oracleFormat = "'YYYY-MM-DD HH24:MI:SS'";
 }
