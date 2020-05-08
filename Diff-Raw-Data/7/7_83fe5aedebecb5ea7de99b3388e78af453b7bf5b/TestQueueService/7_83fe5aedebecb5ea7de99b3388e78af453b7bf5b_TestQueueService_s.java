 
 import java.util.List;
 
 import com.xerox.amazonws.sqs.QueueService;
 import com.xerox.amazonws.sqs.Message;
 import com.xerox.amazonws.sqs.MessageQueue;
 
 public class TestQueueService {
 	public static void main(String [] args) throws Exception {
 		QueueService qs = new QueueService("[AWS Access Id]", "[AWS Secret Key]");
 		List<MessageQueue> queues = qs.listMessageQueues(null);
 		for (MessageQueue queue : queues) {
 			log.debug("Queue : "+queue.getUrl().toString());
 			// delete queues that contain a certain phrase
 //			if (queue.getUrl().toString().indexOf("test")>-1) {
 //				queue.deleteQueue();
 //			}
 		}
 		for (int i=0; i<args.length; i++) {
 			MessageQueue mq = qs.getOrCreateMessageQueue(args[i]);
 			for (int j=0; j<50; j++) {
 				mq.sendMessage("Testing 1, 2, 3");
 			}
 			for (int j=0; j<50; j++) {
 				Message msg = mq.receiveMessage();
 				log.debug("Message "+(j+1)+" = "+msg.getMessageBody());
 				mq.deleteMessage(msg.getMessageId());
 			}
 //			msg = mq.peekMessage(msg.getMessageId());
 		}
 	}
 }
