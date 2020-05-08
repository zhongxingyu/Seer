 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.killswitch;
 
 /**
  *
  * @author q8r9e4
  */
 public class Killswitch {
 	private Vector<String> staticHosts = new Vector<String>();
 	/**
 	 * @param args
 	 */
 	
 	public static void main(String[] args) {
		killswitch ks = new killswitch();
 		ks.run();
 
 		}
 	public void run(){
 		this.staticHosts.add("http://google.com");
 		this.staticHosts.add("http://en.wikipedia.org");
 		
 		Vector<String> FoundHosts = new Vector<String>();
 		for (String host:this.staticHosts){
 			//Try to connect (to lazy to write that now)
 			//if found
 				//FoundHosts.add(host);
 			
 			System.out.println(host);
 		}
 		
 		if (FoundHosts.size()==0){
 			//geo IP
 		}
 		
 		if (FoundHosts.size()==0){
 			//found no hosts/other clients
 			//we are fucked
 			System.out.println("QUICK! HIDE YOURSELF! CIA MIGHT BE LOOKING FOR YOU!");
 			System.out.println("(can't connect to nodes or other clients. Are you connected to the internet?)");
 		} else {
 			// anounce client's IP
 			// fetch neighbour IP's
 		}
 	}
 	
 	public Vector<String> GeoIPLookup(){
 		Vector<String> FoundHosts = new Vector<String>();
 
 		
 		
 		
 		return FoundHosts;
 		
 	}
 }
