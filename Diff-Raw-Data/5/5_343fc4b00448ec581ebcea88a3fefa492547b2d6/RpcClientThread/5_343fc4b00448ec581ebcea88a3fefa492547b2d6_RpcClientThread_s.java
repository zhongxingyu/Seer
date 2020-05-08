 package rpc.client;
 
 import java.net.*;
 import java.util.*;
 import org.apache.xmlrpc.*;
 import org.apache.xmlrpc.client.*;
 
 public class RpcClientThread extends Thread{
 	private int clientNum;
 	private URL serverUrl = null; // url is in the form http://<hostname>:<port>
 	private int scenario; 
 
 	public RpcClientThread(int cn, String url, int scenario){
 		this.clientNum = cn;
 		try {
 			this.serverUrl = new URL(url);
 		} catch (Exception e) {
 			System.out.println("Error in RPC Client: [" + cn +"]\n");
 			e.printStackTrace();
 		}
 		this.scenario = scenario;
 	}
 		
 	public void run(){
 		int size;
 		if(scenario == 2){
 			size = 10;
 		}else 
 			size = 1;
 				
 		try{
 			// create a new configuration object that allows us to set the required characteristics of our client
 			XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
 			config.setServerURL(serverUrl); // the server to connect to
 			config.setEnabledForExtensions(true); // allow serializable objects to be used
 			config.setConnectionTimeout(60000); // timeout of 1 min
 			config.setReplyTimeout(60000); // 1 min timeout on replies too
 			
 			// create the new client
 			XmlRpcClient client = new XmlRpcClient();
 			client.setConfig(config);
 			
 			for (int i = 0; i < size; i++){
 				try{ 
 					new RpcClientSender(client,genPayload(),i,clientNum).start();
 				}catch(Exception e){ e.printStackTrace();}
 			}
 		}catch (Exception e){e.printStackTrace();}
 	}
 	
	public static ArrayList<double[]> genPayload(){
 		int size=5;
 	
 		ArrayList<double[]> payload = new ArrayList<double[]>(size);
 			for (int i=0; i<size; i++){
 				payload.add(genArray());
 			}
 			return payload;
 		
 	}
 	
	public static double[] genArray(){
 		double [] arr = new double[1000];
 		for (int i = 0; i < arr.length; i++){
 			arr[i]=Math.random()*10;
 		}
 		return arr;
 	}
 	
 	
 	
 }
