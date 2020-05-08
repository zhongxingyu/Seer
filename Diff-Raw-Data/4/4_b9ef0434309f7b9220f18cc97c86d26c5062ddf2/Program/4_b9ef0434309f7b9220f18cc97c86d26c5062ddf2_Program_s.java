 package AP2DX.reflex;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.DelayQueue;
 
 import AP2DX.AP2DXBase;
 import AP2DX.AP2DXMessage;
 import AP2DX.ConnectionHandler;
 import AP2DX.Message;
 import AP2DX.Module;
 
 public class Program extends AP2DXBase 
 {	
 	
     /**
 	 * Entrypoint of reflex
 	 */
 	public static void main (String[] args)
     {
 		new Program();
 	}
 
 	
 	/**
 	 * constructor
 	 */
 	public Program() 
     {
         super(Module.REFLEX); // explicitly calls base constructor
 		System.out.println(" Running Reflex... ");
 
 	}
 	
 	
 	/**
 	 * Does the logic for the Reflex module. That is: <br/>
 	 * - check if there is something in front of the vehicle and stop the motor.<br/>
 	 * - forward messages from the Planner to the motor if there is nothing.
 	 * @param message The AP2DX Message that is received and read from the queue.
 	 */
 	@Override
 	public ArrayList<AP2DXMessage> componentLogic(Message message) 
     {
         ArrayList<AP2DXMessage> messageList = new ArrayList<AP2DXMessage> ();
 
  
         switch(message.getMsgType())
         {
             case AP2DX_MOTOR_ACTION:
                 //TODO: check command and override if nessecary
             	// for now, we will just forward stuff to the motor, okay!?
             	
                 message.setDestinationModuleId(Module.MOTOR);
                 messageList.add((AP2DXMessage)message);            
                 break;
             case AP2DX_SENSOR_SONAR:
            	
             	break;
             default:
                 System.out.println("Error in AP2DX.reflex.Program.componentLogic(Message message) Couldn't deal with message: " + message.getMsgType());
         }
         return messageList;
 	}
 	
 	private class MotorPassthrough implements Runnable {
 		AP2DXBase base;
 		
 		public MotorPassthrough(AP2DXBase base) {
 			this.base = base;
 		}
 		
 		@Override
 		public void run() {
 			ConnectionHandler conn = null;
 			try {
 				conn = this.base.getSendConnection(Module.MOTOR);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 
 		}
 	}
 
 	public boolean isPlannerBlocked() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
