 package gui.Main;
 
 import hypeerweb.HyPeerWeb;
 import hypeerweb.Node.NodeContents;
 
 import gui.Main.HyPeerWebDebugger;
 
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.*;
 
 import javax.swing.*;
 
 import command.GlobalObjectId;
 import command.PeerCommunicator;
 
 /**
  * The central GUI used to display information about the HyPeerWeb and debug information
  * 
  * @author Matthew Smith
  *
  */
 public class GUI extends JFrame implements Observer
 {
 	private static GUI singleton = null;
 	
 	/** Main Debugger Panel**/
 	private HyPeerWebDebugger debugger;
 	
 	private HyPeerWeb hypeerweb;
 	private JScrollPane scrollPane;
 	
 	public GUI()
 	{
 	}
 	
 	/**
 	 * Creates and initializes the GUI as being the root
 	 */
	public GUI(HyPeerWeb hypeerweb){
 		init(hypeerweb);
 	}
 	
	private void init(HyPeerWeb hypeerweb){
 	    this.hypeerweb = hypeerweb;
         this.setTitle("HyPeerWeb DEBUGGER V 1.1");
         
         // Make GUI an observer of hypeerweb
         this.hypeerweb.addNewObserver(this);
         //System.out.println(this.hypeerweb.getCountObservers());
 
         this.addWindowListener(new WindowAdapter() {
               public void windowClosing(WindowEvent we) {
                 shutdown();
                 System.exit(0);
               }
             });
         
         debugger = new HyPeerWebDebugger(this);
         scrollPane = new JScrollPane(debugger);
         scrollPane.setPreferredSize(new Dimension(debugger.WIDTH+20, debugger.HEIGHT));
         
         this.getContentPane().add(scrollPane);
         
         this.pack();
 	}
 	
 	private void shutdown()
 	{
 
 	}
 	
	public static GUI getSingleton(HyPeerWeb hypeerweb){
 		if(singleton == null){
 			try{
 				singleton = new GUI(hypeerweb);
 						
 				singleton.setVisible(true);
 			}
 			catch(Exception e)	{
 				JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR",JOptionPane.ERROR_MESSAGE);
 				e.printStackTrace();
 		        hypeerweb.saveToDatabase();
 		        hypeerweb.clear();
 				System.exit(1);
 			}
 		}
 		return singleton;
 	}
 	
 	/**
 	 * Start Point of the Program
 	 */
	public static void main (String[] args){
 	   // GUI gui;
 	    if (args.length == 0)
 	    {
 	        getSingleton(HyPeerWeb.getSingleton());
 	        System.out.println("ERROR, not getting args.");
 	    }
 	    else
 	    {
 	       //init this id object with the args. These will be the values printed by Server.java
 	       command.PortNumber pn = new command.PortNumber(new Integer(args[1]));
 	       command.LocalObjectId loid = new command.LocalObjectId(new Integer(args[2]));
 	       command.GlobalObjectId id = new command.GlobalObjectId(args[0], pn, loid);
 	       try{
 	            PeerCommunicator.createPeerCommunicator();
 	        }catch(Exception e){
 	            System.err.println("ERROR:" + e.getMessage());
 	            System.err.println(e.getStackTrace());
 	        }
 	       getSingleton(new command.HyPeerWebProxy(id));
 	    }
 	}
 
 	/**
 	 * Retrieves the HyPeerWeb Debugging Panel
 	 * @return HyPeerWebDebugger
 	 */
 	public HyPeerWebDebugger getHyPeerWebDebugger() {
 		return debugger;
 	}
 	
 	public HyPeerWeb getHyPeerWeb(){
 		return hypeerweb;
 	}
 	
 	public void printToTracePanel(Object msg){
 		debugger.getTracePanel().print(msg);
 	}
 	
 	public void finalize()
 	{
         hypeerweb.saveToDatabase();
         hypeerweb.clear();
 	}
 	
 	/**
      * Observer pattern
      */
     public void update(Observable o, Object arg) {
         // TODO update GUI with nodes from HyPeerWeb
         System.out.println("Change observed.");
         
         debugger.getMapper().getNodeListing().updateList();
         if(arg instanceof NodeContents){
             for(String key: ((NodeContents)arg).getKeys()){
                 getSingleton(null).printToTracePanel(((NodeContents)arg).get(key));
             }
         }
         
         /* Check the nodes
          * Update the GUI representation of HyPeerWeb so that it is accurate
          */
         // arg = transient Map<Integer,Node> nodes;
         
     }
     
     public Object writeReplace()
     {
         GlobalObjectId id = new GlobalObjectId();
         command.ObjectDB.getSingleton().store(id.getLocalObjectId(), this);
         command.GUIProxy myProx = new command.GUIProxy(id);
         return myProx;
     }
 
 }
