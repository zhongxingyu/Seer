 package rental;
 
 import java.rmi.AlreadyBoundException;
 import java.rmi.NotBoundException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 public class Main {
 
 	public static void main(String[] args) throws Exception {
 		// TODO Use an RMISecurityManager for the next session
 		System.setSecurityManager(null);
 
 		// Registry host
 		String host = (args.length < 1) ? null : args[0];
 		Registry registry = LocateRegistry.getRegistry(host);
 
 		// Lookup company registry
 		CompanyRegistry companyRegistry = null;
 		try {
 			companyRegistry = (CompanyRegistry) registry
 					.lookup(CompanyRegistry.class.getSimpleName());
 		} catch (NotBoundException e1) {
 			System.err.println("Company registry not bound");
 			System.exit(-1);
 		}
 
 		// Create agency
 		AgencyImpl ageny = new AgencyImpl(companyRegistry);
 
 		// Bind stub to registry
		Ageny stub = (Ageny) UnicastRemoteObject.exportObject(ageny, 0);
 		try {
			registry.bind(Ageny.class.getSimpleName(), stub);
 		} catch (AlreadyBoundException e) {
 			System.err.println("Agency already bound");
 			System.exit(-1);
 		}
 
 		System.out.println("Agency ready");
 	}
 
 }
