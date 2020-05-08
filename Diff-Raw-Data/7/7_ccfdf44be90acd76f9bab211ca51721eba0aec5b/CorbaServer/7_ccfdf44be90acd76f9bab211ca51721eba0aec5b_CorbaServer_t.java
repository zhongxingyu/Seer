 package corba.server;
 
 import org.omg.CosNaming.*;
 import org.omg.CosNaming.NamingContextPackage.*;
 import org.omg.CORBA.*;
 import org.omg.PortableServer.*;
 
 
 import java.util.Properties;
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 class CorbaImpl extends CorbaPOA {
 	private ORB orb;
 	ExecutorService es = Executors.newFixedThreadPool(100);
 	
 	public void setORB(ORB orb){
 		this.orb = orb;
 	}
 	// implementation
 	public double[] getAverage(Payload p){
 		double[] result = null;
 		Callable<double[]> worker = new CorbaServerThread(p);
 		Future<double[]> future = es.submit(worker);
 		
 		try{
 			result = future.get();
 		}catch (Exception e){e.printStackTrace();}
 		
 		return result;
 	}
 	
 	public void shutdown(){
 		this.orb.shutdown(false);
 	}
 }
 
 public class CorbaServer {
 	public static void main(String[]args){
 		try{
 			Runtime.getRuntime().exec("cmd /c start src/project/corba/server/orbd.exe"); // start orbd service
 			
 			ORB orb = ORB.init(args, null);
 			POA root = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
 			root.the_POAManager().activate();
 			
 			CorbaImpl corbaImpl = new CorbaImpl();
 			corbaImpl.setORB(orb);
 			
 			org.omg.CORBA.Object ref = root.servant_to_reference(corbaImpl);
 			Corba corbaRef = CorbaHelper.narrow(ref);
 			
 			org.omg.CORBA.Object objectReference = orb.resolve_initial_references("NameService");
 			NamingContextExt namingContextReference = NamingContextExtHelper.narrow(objectReference);
 			
 			String name = "getAverage";
 			NameComponent path[] = namingContextReference.to_name(name);
 			namingContextReference.rebind(path, corbaRef);
 			
 			System.out.println("CorbaServer ready and waiting for requests...");
 			
 			orb.run();
		} catch (Exception e){
			e.printStackTrace();
			try{
				Thread.sleep(10000);
			}catch(Exception ex){ex.printStackTrace();}
		}
 		
 		System.out.println("CorbaServer Exiting");
 		
 			
 		}
 	}
 	
 
 
 	
 	
 	
 	
 	
 	
 
