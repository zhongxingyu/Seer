 import java.io.EOFException;
 import java.io.ObjectInputStream;
 
 /*
  * Child thread created by the Server
  * Only keep track of the input stream of a given socket, and deal with the buffer issue
  * It also maintain some connection state information, like the counter of input message number
  */
 class ReceiveThread implements Runnable {
 	String remote_name;
 	MessagePasser mmp;
 	
 	/*
 	 * Constructor
 	 */
 	public ReceiveThread(String remote_name) {
 
 		this.remote_name = remote_name;
 		this.mmp = MessagePasser.getInstance();
 		
 	}
 	
 	/*
 	 * This method do the real work when this thread is created
 	 * It listens to the input stream of the socket
 	 * The connection will never close until the application terminates
 	 */
 	public void run() {
 		
 		// get the connection state of this socket at first
 		ConnState conn_state = this.mmp.connections.get(this.remote_name);
 		ObjectInputStream ois = conn_state.getObjectInputStream();
 		boolean stored = true; //must be true by default to catch ACK of non-new messages
 		// Infinite loop: listen for input
 		try {
 			try {
 				while(true) {
 		
 					TimeStampedMessage message = (TimeStampedMessage)ois.readObject(); 
 						
 					/* get a multicast message */
 					if(message.type.equals("multicast") && !message.kind.equals("ack")) {
 						System.out.println("Got a multicast message: "+message.toString()+" MCID: "+message.mc_id);
 						this.mmp.globalLock.lock();
 						if(!this.mmp.isUsefulMessage(message)) {
 							this.mmp.globalLock.unlock();
 							continue;
 						}
 						else {
 							if(!this.mmp.isInHBQ(message))
 							{
 								stored = this.mmp.insertToHBQ(new HBItem(message));
 							}
 							else {
 								HBItem this_item = this.mmp.getHBItem(message.src, message.mc_id);
 								if(this_item.message == null)
 									this_item.message = message;
 							}
 							if(stored) //because otherwise we'd be acking a message that we won't receive
 							{
 								this.mmp.tryAckAll(message);
 								System.out.println("After getting reg multicast message, trying to mc_ACK message "+message.toString());
 								this.mmp.multicastAck(message);
 							}
 							this.mmp.globalLock.unlock();
 						}
 						this.mmp.printHBQ();
 					}
 						
 					/* get a ACK message */
 					else if(message.type.equals("multicast") && message.kind.equals("ack")) {
 						System.out.println("Got an ACK message: "+message.toString());
 						System.out.println("Payload VTS: "+message.payload.toString());
 						this.mmp.globalLock.lock();
 						if(!this.mmp.isUsefulMessage(message)) {
 							this.mmp.globalLock.unlock();
 							System.out.println("Not useful message "+message.toString());
 							continue;
 						}
 						else {
 							System.out.println("Message is useful: "+message.toString());
 							if(!this.mmp.isInHBQ(message))
 							{
 								System.out.println("Before insertToHBQ");
 								stored = this.mmp.insertToHBQ(new HBItem(message));
 								//System.out.println("After insertToHBQ");
 							}
 							if(stored)
 							{
 								//System.out.println("Before tryAcceptAck");
 								this.mmp.tryAckAll(message);
 								//System.out.println("After tryAcceptAck");
 							}
 							this.mmp.globalLock.unlock();
 						}
 						//this.mmp.printHBQ(); can't print HBQ here, because we're dealing with incomplete messages as the ACK payload
 					}
 						
 					/* get a retransmit kind message */
 					else if(message.kind.equals("retransmit")) {
 						System.out.println("Got a retransmit message: "+message.toString());
 						System.out.println("Retransmit payload: "+message.payload.toString());
 						TimeStampedMessage msg = (TimeStampedMessage)message.payload;
 						this.mmp.globalLock.lock();
 						if(!this.mmp.isUsefulMessage(msg)) {
 							this.mmp.multicastAck(message); //we still need to ack it, b/c somebody didn't receive our ack
 							this.mmp.globalLock.unlock();
 							continue;
 						}
 						else {
 							if(!this.mmp.isInHBQ(msg))
 								stored = this.mmp.insertToHBQ(new HBItem(msg));
 							else {
 								HBItem this_item = this.mmp.getHBItem(msg.src, msg.mc_id);
 								if(this_item.message == null)
 									this_item.message = msg;
 							}
 							if(stored)
 							{
 								this.mmp.tryAckAll(message);
 								this.mmp.tryAckAll(msg);
 								System.out.println("After getting retransmit message, trying to mc_ACK message "+message.toString());
 								this.mmp.multicastAck(message);
 							}
 							this.mmp.globalLock.unlock();
 						}
 						this.mmp.printHBQ();
 					}
 						
 					/* get a regular message */
 					// put it into the MessagePasser's rcv_buf
 					// drop the message if the buffer is full
					if(!this.mmp.rcv_buf.nonblockingOffer(message)) {
						continue;
					}
					//this.mmp.print();
 				}
 			} finally {
 				
 				conn_state.getObjectInputStream().close();
 				conn_state.getObjectOutputStream().close();
 				conn_state.local_socket.close();
 				this.mmp.connections.remove(remote_name);
 				this.mmp.globalLock.unlock();
 			}
 		} catch (Exception e){
 			this.mmp.globalLock.unlock();
 			if(e instanceof EOFException) {
 				System.out.println("Connection to " + remote_name + " is disconnected");
 			}
 			System.out.println("Exception is "+e.toString());
 			e.printStackTrace();
 			return;
 		}
 	}
 }
