 package Communications;
 
 import java.io.*;
 import java.net.SocketException;
 import java.util.concurrent.*;
 
 import Messages.Message;
 import Utilities.Parser;
 
 
 public class UDPReceiver  {
 	protected PipedInputStream data=null;
 	protected UDPReceiverThread thread=null;
 	protected Thread t;
 	protected ConcurrentLinkedQueue<Byte> queue=new ConcurrentLinkedQueue<Byte>();
 	protected boolean needsMore=false;
 	int size=132;
     int moreNeeded=Integer.MAX_VALUE;
     byte[] current=null;
     byte[] body=null;
     Parser parser=new Parser();
 	public UDPReceiver() throws SocketException{
 		data=new PipedInputStream();
 		thread=new UDPReceiverThread(queue);
 		t=(new Thread(thread));
 		t.start();
 	}
 	
 //	public byte[] getPacket(){
 //		if(queue.size()<size){
 //			return new byte[0];
 //		}
 //		else{
 //			byte[] packet = new byte[size];
 //			for(int i=0;i<size;i++){
 //				packet[i]=queue.poll();
 //			}
 //		}
 //	}
 	
 	public Message read(){
 		if (!needsMore) {
 			if (queue.size() >= size) {
 				System.out.println("getting first");
 				current = new byte[size];
 				for (int i = 0; i < size; i++) {
 					current[i] = queue.poll();
 				}
 				moreNeeded=parser.parse(current);
 				if(queue.size()>=moreNeeded){
 					System.out.println("Second is there alredy");
 					for (int i = 0; i < moreNeeded; i++) {
 						body[i] = queue.poll();
 					}
 					needsMore=false;
 					moreNeeded=Integer.MAX_VALUE;
 					return parser.addBody(body);
 				}
 				needsMore=true;
 				return null;
 			} else {
 				return null;
 			}
 		}
 		else if(queue.size()>=moreNeeded){
 			System.out.println("getting second");
 			for (int i = 0; i < moreNeeded; i++) {
 				body[i] = queue.poll();
 			}
 			needsMore=false;
 			moreNeeded=Integer.MAX_VALUE;
 			return parser.addBody(body);
 		}
 		return null;
 	}
 	
 	public void stop() throws InterruptedException{
 		thread.setRunning(false);
 		t.join();
 	}
 }
