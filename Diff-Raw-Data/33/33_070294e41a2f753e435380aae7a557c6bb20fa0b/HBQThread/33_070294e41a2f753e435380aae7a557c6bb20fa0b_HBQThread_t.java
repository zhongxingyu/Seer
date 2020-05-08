 import java.util.ArrayList;
 
 class HBQThread implements Runnable {
 	
 	MessagePasser mp;
 	
 	public HBQThread() {
 		this.mp = MessagePasser.getInstance();
 	}
 	
 	public void run() {
 		
 		try{
 			while(true) {
 				this.mp.globalLock.lock();
 				/* push ready message to rcv_buf */
 				if(!this.mp.hbq.isEmpty())
 					if(this.mp.hbq.get(0).isReady()) {
						System.out.println("In HBQThread() $$ Before getReadyMessage, the HBQ size =  " + this.mp.hbq.size());
						TimeStampedMessage msg = this.mp.getReadyMessage();
						System.out.println("In HBQThread() $$ We got the ready message: " + msg.toString());
						System.out.println("In HBQThread() $$ After getReadyMessage, the HBQ size =  " + this.mp.hbq.size());
						System.out.println("In HBQThread() $$ Before offer rcv_buf, the rcv_buf size =  " + this.mp.rcv_buf.size());
						this.mp.rcv_buf.nonblockingOffer(msg);
						System.out.println("In HBQThread() $$ After offer rcv_buf, the rcv_buf size =  " + this.mp.rcv_buf.size());

 					}
 				
 				/* if timeout, resend to non-acked nodes of this message*/
 				for(int i = 0; i < this.mp.hbq.size(); i++) {
 					if(this.mp.hbq.get(i).isTimeOut()) {
 						System.out.println("Out of Time!!!");
 						ArrayList<String> needed_nodes = this.mp.hbq.get(i).nodesNeedResend();
 						for(int j = 0; j < needed_nodes.size(); j++) {
 							this.mp.resend(this.mp.hbq.get(i).message, needed_nodes.get(i));
 						}
 					}
 				}
 				this.mp.globalLock.unlock();
 				Thread.sleep(50);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		finally{
 			this.mp.globalLock.unlock();
 		}
 				
 	}
 	
 }
