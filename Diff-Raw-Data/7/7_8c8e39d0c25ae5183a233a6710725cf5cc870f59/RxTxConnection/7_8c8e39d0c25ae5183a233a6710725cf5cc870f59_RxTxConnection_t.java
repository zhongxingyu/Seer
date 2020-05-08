 package jay.gsm.driver.rxtx;
 
 import gnu.io.SerialPort;
 import gnu.io.SerialPortEvent;
 import gnu.io.SerialPortEventListener;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Queue;
 import java.util.TooManyListenersException;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import jay.gsm.CommandResult;
 import jay.gsm.Connection;
 import jay.gsm.ConnectionException;
 import jay.gsm.ConnectionListener;
 import jay.gsm.ConnectionState;
 import jay.gsm.UnsolicitedResult;
 
 public class RxTxConnection implements Connection, SerialPortEventListener {
 	private static final Logger logger = Logger.getLogger(RxTxConnection.class.getName());
 	
 	private SerialPort serialPort;
     private InputStream in;
     private OutputStream out;
     
     private byte[] buffer = new byte[1024];
     
     private boolean open;
     
     private List<ConnectionListener> listeners;
     
     private Queue<Command> commandQueue;
     
     private ConnectionState connectionState;
     
     static class Command {
         boolean issued;
         String string;
         public Command(String string) {
             super();
             this.string = string;
         }
         public boolean isIssued() {
             return issued;
         }
         public void setIssued(boolean issued) {
             this.issued = issued;
         }
         public String getString() {
             return string;
         }
         public void setString(String string) {
             this.string = string;
         }
         public String toString() {
             return this.string;
         }
     }
 	
 	public RxTxConnection(SerialPort serialPort) throws IOException, TooManyListenersException {
 		super();
 
 		this.connectionState = ConnectionState.ACTIVE;
 		this.serialPort = serialPort;
 		
         this.in = serialPort.getInputStream();
         this.out = serialPort.getOutputStream();
         
         this.serialPort.addEventListener(this);
         this.serialPort.notifyOnDataAvailable(true);	
         
         this.listeners = new ArrayList<ConnectionListener>();
         
         this.commandQueue = new ArrayBlockingQueue<Command>(100);
 	}
 	
 	private void issueNextCommand() {
         if (this.connectionState!=ConnectionState.ACTIVE) {
             return;
         }
 	    
 	    Command command = this.commandQueue.element();
	    logger.fine(String.format("Current command: [{%s}]",command.getString()));
 	    if (!command.isIssued()) {
 	        logger.info(String.format("Posting command: [%s]",command));
 	        try {
 	            out.write(command.getString().getBytes());
 	            out.write('\r');
 	            out.flush();
 	            
 	            command.setIssued(true);
 	        } catch (IOException e) {
 	            // FIXME This mainly means the Mobile Equipment is not reachable. 
 	            // Hence it better to clear the command queue.
 	            logger.log(Level.WARNING, String.format("Failed to submit command %s. Probably the Stub is turned-off. Clearing the command-queue: %s", command, this.commandQueue));
 	            this.commandQueue.clear();    
 	            // TODO Auto-generated catch block
 //	            throw new ConnectionException(e);
 	            e.printStackTrace();
 	        }
 	    }
 	}
 	
 	@Override
     public synchronized long sendBatchCommands(String... commands) throws ConnectionException {
        boolean commandBeingExecuted = !this.commandQueue.isEmpty();
         
         for(String command:commands) {
            this.commandQueue.add(new Command(command));
         }
         
         if (commandBeingExecuted) {
        	logger.log(Level.INFO, "Command being executed. Command Queue: {0}",commands);
             return 0;
         }
         
         
         logger.info("No command being executed. Issue next command");
         issueNextCommand();
         
         return 0;
     }
 
     // Implementing Connection methods
 	@Override
 	public long sendCommand(String command) throws ConnectionException {
 	    return sendBatchCommands(new String[]{command});
 	}
 
 	@Override
 	public String executeCommand(String command) throws ConnectionException {
 		return null;
 	}
 
 	@Override
 	public void addConnectionListener(ConnectionListener l)
 			throws ConnectionException {
 		this.listeners.add(l);
 	}
 
 	@Override
 	public void removeConnectionListener(ConnectionListener l)
 			throws ConnectionException {
 		this.listeners.remove(l);
 	}
 	
 	@Override
 	public boolean isClosed() throws ConnectionException {
 		return !open;
 	}
 
 	@Override
 	public void close() throws ConnectionException {
         logger.info("Closing I/O");
         
         try {
         	this.in.close();
         	this.out.close();
         } catch(IOException e) {
         	// FIXME improve exception handling
         	throw new ConnectionException(e);
         }
         
         if (this.serialPort!=null) {
             this.serialPort.removeEventListener();
             logger.info("Closing comm port");
             this.serialPort.close();
             logger.info("Closed");
         }	
         
         this.open = false;
 	}
 
 	// Implementing SerialPortEventListener
 	@Override
 	public void serialEvent(SerialPortEvent event) {
 		int data;
         
         try {
             int len = 0;
             while ( ( data = in.read()) > -1 ) {
 //                if (data=='\0') {
 //                    if (this.connectionState==ConnectionState.SUSPENDED) {
 //                        onStateChanged(ConnectionState.RESET);
 //                    } else {
 //                        onStateChanged(ConnectionState.SUSPENDED);
 //                    }
 //                    String message = String.format("type: %s,  newValue: %s; oldValue: %s",event.getEventType(),
 //                    event.getNewValue(),
 //                    event.getOldValue());
 //                    System.err.println("-----NULL-----" + message);
 //                    return;
 //                }
                 if ( data == '\n' ) {
                     break;
                 }
                 buffer[len++] = (byte) data;
             }
             
             String response = new String(buffer,0,len).trim(); 
             
             logger.info(String.format("RESPONSE: %s\n",response));
             
             handleResponseLine(response);
         } catch ( IOException e ) {
             
             e.printStackTrace();
             
             onStateChanged(ConnectionState.DISCONNECTED);
         }    		
 	}
 	
 	private void onStateChanged(ConnectionState connectionState) {
 	    this.connectionState = connectionState;
 	    
 	    for(ConnectionListener l:this.listeners) {
             l.onStateChanged(connectionState);
         }
 	}
 	
 	private void onCommandExecuted(CommandResult commandResult) {
 		for(ConnectionListener l:this.listeners) {
 			l.onCommandExecuted(commandResult);
 		}
 		
 		logger.info(String.format("Last command [%s] got executed. Issue next command",commandResult.getCommandString()));
 		this.issueNextCommand();
 	}
 	
 	private void onUnsolicitedResult(UnsolicitedResult unsolicitedResult) {
 		for(ConnectionListener l:this.listeners) {
 			l.onUnsolicitedResult(unsolicitedResult);
 		}
 	}
 	
 	private StringBuffer responseTextBuffer = new StringBuffer();
 	private void handleResponseLine(String message) {
 		if (message.startsWith("+CMTI:")) {
 			UnsolicitedResult unsolicitedResult = new UnsolicitedResult();
 			unsolicitedResult.setResultCode("+CMTI");
 			unsolicitedResult.setText(message);
 			
 			onUnsolicitedResult(unsolicitedResult);
 		} else {
 			responseTextBuffer.append(message);
 			responseTextBuffer.append('\n');
 			
 			if ("OK".equals(message)
 			     || "ERROR".equals(message)) {
 				CommandResult commandResult = new CommandResult();
 				Command command = commandQueue.remove();
 				commandResult.setCommandString(command.getString());
 				commandResult.setResponseText(responseTextBuffer.toString());
 				
 				responseTextBuffer.setLength(0);
 			
 				onCommandExecuted(commandResult);
 			}
 		}
 	}
 }
