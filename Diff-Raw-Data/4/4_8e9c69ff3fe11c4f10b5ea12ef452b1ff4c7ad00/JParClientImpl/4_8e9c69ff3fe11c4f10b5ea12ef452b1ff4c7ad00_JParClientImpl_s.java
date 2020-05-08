 package matlab.jpar.client;
 
 import java.rmi.NotBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Hashtable;
 
 import matlab.jpar.client.common.JParClient;
 import matlab.jpar.server.common.JParServer;
 import matlab.jpar.solver.common.JParSolver;
 
 public class JParClientImpl extends UnicastRemoteObject implements
 		JParClient {
 
 	private int task_count;
 	private int task_no;
 	private Hashtable results;
 	
 	private Object taskSynch;
 	private JParServer server;
 
 	private static final long serialVersionUID = 7526472295622776147L;
 	
 	// this flag should be checked by M-scripts
 	private boolean initialized = false;
 	
 	public boolean isInitialized() {
 		return initialized;
 	}	
 
 	public Object getResult(String taskID) {
 		synchronized (taskSynch) {
 			return this.results.get(taskID);
 		}
 	}
 	
 	public JParClientImpl(String hostname, int port) throws RemoteException {
 		if (System.getSecurityManager() == null) {
 			System.setSecurityManager(new RMISecurityManager());
 		}
 
 		String serverName = "JParServer";
 		try {			
 			Registry registry = LocateRegistry.getRegistry(hostname, port);
 			this.server = (JParServer) registry.lookup(serverName);
 		} catch (RemoteException e) {
 			System.err.println("Solver: Java RMI Exception: " + e.getMessage());
 			return;			
 		} catch (NotBoundException e) {
 			System.err.println("Solver: " + serverName + " lookup failed:" + e.getMessage());
 			return;
 		} catch (Exception e) {
 			System.err.println("\n");
 			System.err.println("Client: JParClinet Searching Server Exception: " 
 					+ e.getMessage());
 			e.printStackTrace();
 			return;
 		}
 
 		this.task_count = 0;
 		this.task_no = 0;
 		this.taskSynch = new Object();
 		this.results = new Hashtable();
 		
 		initialized = true;
 	}
 
 	public boolean taskIsDone(String taskID, Object[] retVal)
 			throws RemoteException {
 		
 		System.out.println("Client: taskIsDone(): Old task: " + taskID);
 		
 		synchronized (taskSynch) {
 			this.results.put(taskID, retVal);
 			task_count--;
 			taskSynch.notify();
 		}
 		
 		System.out.println("Client: Task finished: " + taskID);
 
 		return true;
 	}
 
 	public String createTask(int nargout, String argout, String func, Object[] args) {
 		try {
 			JParSolver solver = (JParSolver) this.server.getFreeSolver();
 
 			synchronized (taskSynch) {
 				task_count++;
 				task_no++;
 				String taskID = String.valueOf(task_no);
 				System.out.println("\nClient: createTask(): New task: " + taskID);
 				System.out.println("Client: Sending task (" + func + "): "
 						+ solver.executeTask(this, taskID, nargout, argout, func, args));				
 				return taskID;				
 			}
 		} catch (RemoteException e) {
 			System.err.println("Client: Java Remote Exception: "
 					+ e.getMessage());			
 		} catch (Exception e) {
 			System.err.println("Client: JParClinet Exception: "
 					+ e.getMessage());
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void waitForTask() {
 		while (true) {
 			synchronized (taskSynch) {
 				if (task_count == 0)
 					return;
 				try {
 					taskSynch.wait();
 				} catch (Exception e) {
 					System.err.println("Client: JParClinet Wait For Task: "
 							+ e.getMessage());
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public String [] getSolvers() {
 		try {
 			return server.getSolvers();
 		} catch (RemoteException e) {
 			System.err.println("Client: JParClinet Get Solvers: "
 					+ e.getMessage());
 		}
 		return null;
 	}
 	
 	public void killSolvers() {
 		try {
 			server.killSolvers();
 		} catch (RemoteException e) {
 			System.err.println("Client: JParClinet Kill Solvers: "
 					+ e.getMessage());
 		}
 	}
 }
