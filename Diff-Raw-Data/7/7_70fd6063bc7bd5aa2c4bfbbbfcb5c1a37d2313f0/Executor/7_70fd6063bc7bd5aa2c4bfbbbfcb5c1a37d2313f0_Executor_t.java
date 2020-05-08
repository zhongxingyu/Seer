 
 
 import java.io.OutputStream;
 
 /**
  * @author E. Javier Figueroa 
  * COP5615 Spring 2011 
  * University of Florida
  * 
  */
 public class Executor implements Runnable {
 	private String host;
 	private String server;
 	private int port;
 	private int id;
 	private Action type;
 	private long sleep;
 
 	public int getId() {
 		return id;
 	}
 
 	public Executor(String host, String server, int port, int id, Action type, long sleep) {
 		this.host = host;
 		this.server = server;
 		this.port = port;
 		this.id = id;
 		this.type = type;
 		this.sleep = sleep;
 	}
 
 	public void run() {
 		String path = System.getProperty("user.dir");
 		Log.write("Starting Remote Process: Client ID: " + this.id + " on "
 				+ this.host);
 		int times = Integer.parseInt(PropertyManager.getProperties().get("RW.numberOfAccesses"));
 		String[] command = {
 				"ssh",
				"-o",
				"UserKnownHostsFile=/dev/null",
				"-o", 
				"StrictHostKeyChecking=no",
 				"-i",
 				"id_rsa",
 				"figueroa@" + this.host,
				"id_rsa",
 //				"javier.figueroa@" + this.host,
 				"cd " + path + " ; java start "
 //				"cd " + path + " ; java -jar start.jar "
 				+ this.server + " "	+ this.port + " " + this.id + " " + type.name() + " " + times + " " + sleep };
 
 		try {
 			Runtime rt = Runtime.getRuntime();
 			Process pr = rt.exec(command);
 
 			Log.write("Status: SUCCESS! Parameters Passed: " + this.server	+ " " + this.port + " " + this.id + " " + type.name() + " " + times + " " + sleep) ;
 
 			OutputStream stdin = pr.getOutputStream();
 			String carriageReturn = "\n";
 			stdin.write(carriageReturn.getBytes());
 			stdin.flush();
 			stdin.close();
 
 			pr.waitFor();
 			int exitValue = pr.exitValue();
 			if (exitValue != 0) {
 				Log.write("Warning: Remote Process could not start for " + this.type + " Client ID: "	+ this.id + ", host: " + this.host);
 //				Server.workers--;
 			}
 		} catch (Exception e) {
 			Log.write("Warning: Remote Process could not start for " + this.type + " Client ID: "	+ this.id + ", host: " + this.host);
 //			Server.workers--;
 			e.printStackTrace();
 		} 
 	}
 }
 
