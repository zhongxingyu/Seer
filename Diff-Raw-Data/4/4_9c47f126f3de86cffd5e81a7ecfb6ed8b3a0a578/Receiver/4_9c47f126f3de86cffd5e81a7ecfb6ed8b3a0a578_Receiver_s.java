 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import jade.core.Agent;
 import jade.core.behaviours.Behaviour;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import jade.lang.acl.ACLMessage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.TreeMap;
 
 /**
  *
  * @author hstefan
  */
 public class Receiver extends Agent {
 
     public final String folder = "receive/" + getLocalName() + "/";
 
     public void setup() {
 	System.out.println("Starting Receiver Agent.");
 	System.out.println("Agent name: " + this.getAID().getName());
 	addBehaviour(new WaitingMessage(this));
 
 	DFAgentDescription dfd = new DFAgentDescription();
         dfd.setName(getAID());
         ServiceDescription sd = new ServiceDescription();
         sd.setType("receiver");
         sd.setName("dark-hole-file-transfer");
         dfd.addServices(sd);
         try {
             DFService.register(this, dfd);
         }
         catch(FIPAException fe) {
             System.out.println(getName() + ": Impossible to register this agent in the yellow pages");
         }
     }
 
     public void takeDown() {
 	try {
             DFService.deregister(this);
         }
         catch (FIPAException fe) {
             System.out.println(getName() + ": Couldn't remove this agent from the yellow pages.");
         }
     }
 
     class WaitingMessage extends Behaviour {
 
 	public WaitingMessage(Agent agent) {
 	    super(agent);
 	    don = false;
 	}
 
 	@Override
 	public void action() {
 	    ACLMessage msg = receive();
 	    if (msg != null) {
 		String param = msg.getUserDefinedParameter("start");
 		if (param != null && param.equals("true")) {
 		    new File("receive/").mkdir();
 		    new File("receive/" + getLocalName() + "/").mkdir();
 		    String filepath = msg.getUserDefinedParameter("filepath");
 		    myAgent.addBehaviour(new ReceivingMessage(myAgent, filepath));
 		    myAgent.removeBehaviour(this);
 		    don = true;
		    
 		}
	    }
 	}
 
 	@Override
 	public boolean done() {
 	    return don;
 	}
 
 	private boolean don;
     }
 
     class ReceivingMessage extends Behaviour {
 	public ReceivingMessage(Agent agent, String filepath) {
             files = new TreeMap<String, FileOutputStream>();
             FileOutputStream fos;
             try {
 		fos = new FileOutputStream("receive/" + getLocalName() + "/" + filepath);
 		files.put(filepath, fos);
             } catch (IOException ex) {
                 Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
             }
 	}
 
 	@Override
 	public void action() {
 	   ACLMessage msg = receive();
 	   if (msg != null) {
 	       String op = msg.getUserDefinedParameter("stop");
 	       String filename = msg.getUserDefinedParameter("filepath");
 	       if(op != null && op.equals("true")) {
 		   System.out.println(getName() + " finished writting!");
 		   FileOutputStream fos = files.get(filename);
 		   if(fos != null) {
 			try {
 			    fos.close();
 			} catch (IOException ex) {
 			    Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
 			}
 			files.remove(filename);
 			System.out.println(getName() + " is now waiting for new files.");
 			myAgent.addBehaviour(new WaitingMessage(myAgent));
 			myAgent.removeBehaviour(this);
 			return;
 		   }
 	       }
 	       else if(filename != null) {
 		   OutputStream fos = files.get(filename);
 		   if (fos != null) {
 		       try {
 			   fos.write(msg.getContent().getBytes());
 		       } catch (IOException ex) {
 			   System.err.println(getName() + " is unable to write file.");
 			   Logger.getLogger(Receiver.class.getName()).log(Level.SEVERE, null, ex);
 		       }
 		   }
 	       }
 	   } 
 	}
 
 	@Override
 	public boolean done() {
 	    return files.isEmpty();
 	}
 
 	private TreeMap<String, FileOutputStream> files;
     }
 }
