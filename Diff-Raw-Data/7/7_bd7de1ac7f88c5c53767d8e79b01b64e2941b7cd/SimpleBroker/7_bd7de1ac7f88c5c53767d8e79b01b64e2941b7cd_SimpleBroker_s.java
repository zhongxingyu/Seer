 package aic.broker;
 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 
 import aic.service.IService;
 
 public class SimpleBroker implements IService{
 	private IService service;
 	
 	public SimpleBroker(IService service) {
 		super();
 		this.service=service;
 	}
 	
 	public static void main(String[] args) throws Exception {
         try {
         	//this simple broker has just one Worker Service on localhost which is fixed
         	IService service=(IService)Naming.lookup("rmi://localhost/TSA");
         	
             String name = "Broker";
             SimpleBroker broker=new SimpleBroker(service);
             IService stub =(IService) UnicastRemoteObject.exportObject(broker, 0);
             Registry registry = null;
             try{
             	//get existing registry
             	registry = LocateRegistry.getRegistry();
             }catch(RemoteException e){
             	//create new registry
             	registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
             }
            registry.rebind(name, stub);
            System.out.println("TSA Service ready on port: " + Registry.REGISTRY_PORT);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 	}
 
 	public double analyseSentiment(String company) throws RemoteException {
 		return service.analyseSentiment(company);
 	}
 	
 }
