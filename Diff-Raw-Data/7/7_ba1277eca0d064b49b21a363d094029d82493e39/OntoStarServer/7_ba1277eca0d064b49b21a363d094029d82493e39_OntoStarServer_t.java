 package application;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.rmi.NoSuchObjectException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 import databases.KeywordMySQLInterface;
 import databases.UrlMySQLInterface;
 
 public class OntoStarServer {
 
 	private Command currentCommand = null;
 	private final Object lock;
 
 	public OntoStarServer() {
 		super();
 		this.currentCommand = Command.PAUSE;
 		this.lock = new Object();
 	}
 
 	public Command getCurrentCommand() {
 		return currentCommand;
 	}
 
 	public void setCommand(Command c) {
 		if (c != null) {
 			currentCommand = c;
 		}
 	}
 
 	public Object getLock() {
 		return lock;
 	}
 
 	private void execute() {
 		boolean errorOccurred = false;
 		try {
 
 			if (Configuration.runFile.exists()) {
 				BufferedReader reader = new BufferedReader(new FileReader(
 						Configuration.runFile));
 				currentCommand = Command.valueOf(reader.readLine().trim()
 						.toUpperCase());
 
 				reader.close();
 
 			} else {
 				currentCommand = Command.PAUSE;
 			}
 
 			boolean run = true;
 			while (run == true && !Configuration.stopFile.exists()) {
 
 				if (currentCommand.isBlocking()) {
 					run = currentCommand.processRequest();
 					synchronized (lock) {
 						try {
 							lock.wait();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 
 					}
 
 				}
 
 				run = currentCommand.processRequest();
 
 			}
 
 			if (Configuration.stopFile.exists()) {
 				System.out.println("Received stop command.");
 			} else {
 				System.out.println("Job done.");
 			}
			
			KeywordMySQLInterface.getinstance().cleanKeywords();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			errorOccurred = true;
 		} finally {
 
 			if (KeywordExtractionJob.getInstance().numberOfExecutions() > 0) {
 				System.out.println("Mean time to process: "
 						+ KeywordExtractionJob.getInstance()
 								.meanTimeToProcessInMillis() / 1000
 						+ " seconds");
 			}
						
 			try {
 				Configuration.stoppedFile.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 				errorOccurred = true;
 			}
 
 		}
 
 		if (errorOccurred) {
 			System.out
 					.println("An error occurred. Read log file for more details.");
 			OntoStarMonitorClient.getAnInstance().log("Stopped with error.");
 		} else {
 			if (!currentCommand.equals(Command.SHUTDOWN)) {
 				OntoStarMonitorClient.getAnInstance().log("Stopped.");
 			} else {
 				OntoStarMonitorClient.getAnInstance().log("Shut down.");
 			}
 		}
 
 	}
 
 	public static String getTimestamp() {
 		long time = Calendar.getInstance().getTimeInMillis();
 
 		String timestamp = String.format("%tA %tF h%tR", time, time, time);
 
 		return timestamp;
 	}
 
 	public static void main(String[] args) {
 		System.out.println("OntoStarServer started.");
 
 		OntoStarServer server = new OntoStarServer();
 
 		String bindName = "RemoteControl";
 		System.setProperty("java.rmi.server.hostname", Configuration.myIp);
 		System.setProperty("java.security.policy", "file:"
 				+ Configuration.configPath + "rmi.policy");
 
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new SecurityManager());
 		}
 
 		RemoteControl control = null;
 		Registry reg = null;
 		try {
 			reg = LocateRegistry
 					.createRegistry(Configuration.myRmiRegistryPort);
 
 			control = new ConcreteRemoteControl(server);
 
 			System.out.print("Binding . . .");
 			RemoteControl stub = (RemoteControl) UnicastRemoteObject
 					.exportObject(control, 0);
 			reg.rebind(bindName, stub);
 			System.out.println(" Done.");
 
 			server.execute();
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		boolean exit = false;
 
 		try {
 			KeywordMySQLInterface.getinstance().closeConnection();
 			UrlMySQLInterface.getInstance().closeConnection();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 
 		while (exit == false && reg != null && control != null) {
 			try {
 				exit = UnicastRemoteObject.unexportObject(control, false);
 				if (exit == false) {
 					Thread.sleep(1000);
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (NoSuchObjectException e) {
 				e.printStackTrace();
 				exit = true;
 			}
 		}
 
 		System.out.println("OntoStarServer stopped.");
 
 		System.err
 				.println("\n============================================\nEnd: "
 						+ getTimestamp()
 						+ "\n============================================\n");
 	}
 }
