 package Escada.tpc.common.clients;
 
 import com.renesys.raceway.DML.*;
 
 import java.util.*;
 import java.lang.reflect.*;
 
 import Escada.virtuahost.core.*;
 import Escada.xest.*;
 import Escada.interfaces.*;
 import Escada.xest.framework.*;
 import Escada.tpc.common.*;
 import Escada.tpc.common.database.*;
 import Escada.tpc.tpcc.database.*;
 
 
 /**
  * This is a test class. It presents a client sample implementation.
  * @author los
  */
 public class ClientSimulation extends Resource implements Notifiable {
     	
     private ClientEmulation[] generators = null;
 
    /**
     * A mapping between the transaction sent and the stream number from wich it was read.
     * <p>Default is null.
     */
     private HashMap trans2stream = null;
 
     public volatile static int tx_counter = 0;
 	
     /**
     * The transaction counter.
     * <p>Default is 0.
     */
     private int ttid = 0;
     
     /**
     * The client id.
     * <p>Default is null.
     */
     private String hid = null;
 
     /**
     * Creates an instance of the simulation client.
     * 
     * @param clino the client id number.
     * @param ncli the number of clients.
     * @param dmlFile the dml dmlConfig.
     */
     public ClientSimulation(Resource parent, Configuration dml)throws configException  {
 	    super(parent,dml);
     }
 
     public void consume(Object obj) {
     }
                                                                                 
     public void cancel(Object obj) {
     }
 
         
     public void failure(Object obj, int failure) {
 	aborted((Transaction)obj);
     }
                                                                                 
     public void success(Object obj) {
 	committed((Transaction)obj);
     }
                                                                                 
     public void notify(int action, Object data) {
 	hid = (String) data;
 	int ncli = Integer.parseInt(attributeValue("-CLI"));
 
 	System.out.println(getClass().getName() + " : " + hid + " : start : client initiates the activities!"); 
 	System.out.println("Starting " + ncli + " clients for host " + hid);
 
 	createClients();
 
         for(int i=0; i < ncli; i++) { System.out.println("Building client " + i + " for host " + hid); sendTransaction(i); }
     }
 
 
     /**
      *  This method is used to tell the client that a transaction has commited.
      * @param t The commited transaction.
      */
     public void committed(Transaction t) {
     	int slot = -1;
     	
         if(config.nsamples() != -1) {
     	    ClientSimulation.tx_counter++;
 	    if(ClientSimulation.tx_counter > config.nsamples()) System.exit(0); 
         }
 
   	slot = ((Integer) trans2stream.remove(t)).intValue();    		
 	sendTransaction(slot);
     }
     
     /**
      * This method is called by the event delivered when a
      * @param t The transaction that aborted. 
      */
     public void aborted(Transaction t) {
     	int slot = -1;
 
         if(config.nsamples() != -1) {
 	    ClientSimulation.tx_counter ++;
 	    if(ClientSimulation.tx_counter > config.nsamples()) System.exit(0); 
         }
   	slot = ((Integer) trans2stream.remove(t)).intValue();    		
 	sendTransaction(slot);       
     }
 
     /**
      * Sends the next transaction in the designated stream.
      * @param slot the stream number from wich the transaction is to be read.
      */
     private void sendTransaction(int slot) {
     
     	Transaction t = (Transaction)generators[slot].processIncrement();
 				
 	if (t != null) {
 
 	    t.header().hid(hid);
 	  
 	    ttid++;
 		
 	    GenericCommand exec = new GenericCommand(t, this.now()) {
 		    public void apply(Object o) {
 			 Resource dbms = (Resource) o;
 			 dbms.consume((Transaction)data());
 		    }
 	     };
 
 	     long tt = t.userThinkTime() * 1000000;
 
	     trans2stream.put(t,new Integer(slot));
 
	     CommandController.write(exec, rightOutc(),tt);
 	}
     }
 
 
    private void createClients() {    	  
 
        trans2stream = new HashMap();
 
        Emulation.setFinished(false);
        Emulation.setTraceInformation(attributeValue("-TRACEflag"));
        Emulation.setNumberConcurrentEmulators(Integer.parseInt(attributeValue("-TOTCLI")));
   	
        generators = new ClientEmulation[Integer.parseInt(attributeValue("-CLI"))];
        for(int i=0;i < generators.length;i++) {
 	   generators[i] = new ClientEmulation(attributeValue("-EBclass"),attributeValue("-STclass"),attributeValue("-DBclass"),Integer.parseInt(attributeValue("-CLI")),i,attributeValue("-TRACEflag"),hid,Integer.parseInt(attributeValue("-WAREHOUSE")));
 	   generators[i].setName(attributeValue("-TRACEflag") + "-" + i);
        }
    }
 
 
     /**
     * Gets the client description.
     */
     public String description() {
     	
     	String res = new String();
     	res += ">>> Client: " + this.hid + "\n";
     	res += "\t>>>ttid: " + this.ttid + "\n";
     	
     	return res; 
     }
 
     private Config config = new Config();
 
     private class Config
     {
 	public int nsamples() { return 5000; }
     };
 
     
 }
 
 // arch-tag: 3f404089-0727-44c4-a4f0-b37ce95af5ea
