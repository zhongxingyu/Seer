 package aic.service;
 
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.regex.Pattern;
 import aic.service.analyzer.IAnalyzer;
 import aic.service.analyzer.MongoAnalyzer;
 
 public class ServiceImpl implements IService {
 	private IAnalyzer analyser;
 	
 	public ServiceImpl(IAnalyzer analyser) {
 		super();
 		this.analyser = analyser;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		IAnalyzer analyser = new MongoAnalyzer("localhost", "tweets");
 		
         try {
             String name = "TSA";
             ServiceImpl service=new ServiceImpl(analyser);
             IService stub =(IService) UnicastRemoteObject.exportObject(service, 0);
             Registry registry = null;
             try{
             	registry = LocateRegistry.getRegistry();
            	registry.rebind(name, stub);
             }catch(RemoteException e){
             	registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            	registry.rebind(name, stub);
             }
            
             System.out.println("TSA Service ready on port: " + Registry.REGISTRY_PORT);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 	}
 	@Override
 	public double analyseSentiment(String company) throws RemoteException {
 		Pattern p=Pattern.compile(".*"+company+".*", Pattern.CASE_INSENSITIVE);
 		double r=analyser.analyze(p);
 		System.out.println("Rating for " + company + ": " + r);
 		return r;
 	}
 }
