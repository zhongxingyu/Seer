 package node;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 import java.util.UUID;
 
 import message.BcastMsg;
 import message.PaxosMsg;
 import message.SiteCrashMsg;
 
 import org.apache.commons.collections.map.LinkedMap;
 import org.apache.commons.collections.map.MultiKeyMap;
 
 import common.Common;
 import common.Common.AcceptorState;
 import common.Common.BcastMsgType;
 import common.Common.PaxosMsgType;
 import common.Common.SiteCrashMsgType;
 import common.Common.State;
 import common.MessageWrapper;
 import common.Triplet;
 import common.Tuple;
 
 public class Acceptor extends Node {
 
 	final class TransactionStatus {
 		Integer gsn;
 		String data;
 		Common.AcceptorState state;
 		Set<String> Acceptors;	
 		Timestamp timeout;
 	}
 
 	 Comparator<Tuple<Integer, UUID>> comparator = new StringLengthComparator();
      PriorityQueue<Tuple<Integer, UUID>> queue = 
          new PriorityQueue<Tuple<Integer, UUID>>(10, comparator);
      
      class StringLengthComparator implements Comparator<Tuple<Integer, UUID>>
      {
          @Override
          public int compare(Tuple<Integer, UUID> x, Tuple<Integer, UUID> y)
          {
              if  (x.x< y.x)
              {
                  return -1;
              }
              if (x.x > y.x)
              {
                  return 1;
              }
              return 0;
          }
      }
      
 	private String paxosLeaderId;
 	private String paxosLeaderExchange;
 	private Map<UUID, TransactionStatus> uidTransactionStatusMap;
 	private static int lastCommitGSN = 0;
 	private Iterator<UUID> itr;
 	
 	@SuppressWarnings("unchecked")
 	public Acceptor(String nodeId, String fileName, String paxosLeaderId) throws IOException
 	{
 		super(nodeId, fileName);
 		this.paxosLeaderId = paxosLeaderId;
 		this.paxosLeaderExchange = Common.PaxosLeaderExchange + this.paxosLeaderId;
 
 		ArrayList<Triplet<String, String, Boolean>> exchanges = new ArrayList<Triplet<String, String, Boolean>>();
 		exchanges.add(new Triplet(Common.DirectMessageExchange, Common.directExchangeType, true));
 		exchanges.add(new Triplet(this.paxosLeaderExchange, Common.bcastExchangeType, true));
 		this.DeclareExchanges(exchanges);
 		this.InitializeConsumer();
 
 		this.uidTransactionStatusMap = new HashMap<UUID, TransactionStatus>();		
 	}
 
 
 
 	public void run() throws IOException, InterruptedException, ClassNotFoundException{
 
 		while (true) {
 
 			MessageWrapper msgwrap= messageController.ReceiveMessage();   
 			if (msgwrap != null)
 			{
 				if (msgwrap.getmessageclass() == BcastMsg.class  && this.NodeState == State.ACTIVE)
 				{
 					
 					BcastMsg msg = (BcastMsg) msgwrap.getDeSerializedInnerMessage();
 					
 					//Print msg
 					System.out.println("Received " + msg);
 					
 					if(msg.getType() == BcastMsgType.COMMIT_ACK)
 						ProcessCommitAckMessage(msg.getUID(), msg.getGsn(), msg.getNodeid(),msg.getData());
 
 					else if (msg.getType() == BcastMsgType.ABORT_ACK)
 						ProcessAbortAckMessage(msg.getUID(), msg.getNodeid());
 				}
 
 				else if (msgwrap.getmessageclass() == PaxosMsg.class  && this.NodeState == State.ACTIVE)
 				{
 					PaxosMsg msg = (PaxosMsg) msgwrap.getDeSerializedInnerMessage();
 
 					//Print msg
 					System.out.println("Received " + msg);
 					
 					if(msg.getType() == PaxosMsgType.ACCEPT)
 						ProcessAcceptMessage(msg.getUID(), msg.getData());
 
 					else if (msg.getType() == PaxosMsgType.COMMIT && msg.getNodeid().equalsIgnoreCase(this.paxosLeaderId))
 						ProcessCommitMessage(msg.getUID(), msg.getGsn());
 
 					else if (msg.getType() == PaxosMsgType.ABORT)
 						ProcessAbortMessage(msg.getUID());
 				}
 				else if (msgwrap.getmessageclass() == SiteCrashMsg.class)
 				{
 					SiteCrashMsg msg = (SiteCrashMsg) msgwrap.getDeSerializedInnerMessage();
 					
 					//Print msg
 					System.out.println("Received " + msg);
 					
 					if(msg.getType() == SiteCrashMsgType.CRASH && this.NodeState == State.ACTIVE)
 					{
 						this.NodeState = State.PAUSED;
 					}
 					else if(msg.getType() == SiteCrashMsgType.RECOVER && this.NodeState == State.PAUSED)
 					{
 						this.NodeState = State.ACTIVE;
 					}					
 				}
 				else
 				{
 					//Message Discarded.
 				}
 			}
 			
 			//send bcast msgs for timeout transactions
 			itr=uidTransactionStatusMap.keySet().iterator();
 			while(itr.hasNext())
 			{
 				UUID uid=itr.next();
 				TransactionStatus temp= uidTransactionStatusMap.get(uid);
 				Timestamp curtime;
 				
 				if(temp.state == AcceptorState.COMMIT)
 				{
 					curtime=new Timestamp(new Date().getTime());
 					if(curtime.after(Common.getUpdatedTimestamp(temp.timeout, Common.commitabort_timeout)))
 					{
 						temp.timeout=new Timestamp(new Date().getTime());
 						BcastCommitMessage(uid);
 						this.uidTransactionStatusMap.put(uid,temp);
 										
 					}
 				}
 			}
 			
 		}		
 	}
 
 	//method used to process the accept msg for the first time when the Paxos leader sends the data
 	public void ProcessAcceptMessage(UUID uid, String data) throws IOException
 	{
 		TransactionStatus temp = new TransactionStatus();
 		
 		temp.data = data;
 		temp.state = AcceptorState.ACCEPT;
 		temp.timeout=new Timestamp(new Date().getTime());
 		temp.Acceptors=new HashSet<String>();
 		this.uidTransactionStatusMap.put(uid, temp);
 
 		PaxosMsg msg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.ACK, uid);
 		this.SendMessageToPaxosLeader(msg);
 	}
 
 	public void SendMessageToPaxosLeader(PaxosMsg msg) throws IOException
 	{	
 		System.out.println("Sent " + msg);
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		messageController.SendMessage(msgwrap, Common.DirectMessageExchange, this.paxosLeaderId);
 	}
 
 	//method used to process commit msg from paxos leader
 	public void ProcessCommitMessage(UUID uid, int gsn) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.gsn=gsn;
 		temp.state=AcceptorState.COMMIT;		
 		temp.timeout=new Timestamp(new Date().getTime());
 		this.uidTransactionStatusMap.put(uid, temp);
 		ProcessCommitToFile(uid, gsn); //Write to file.
 
 		//shouldn't this be to bcast queue
 		PaxosMsg msg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.ACK, uid);
 		this.SendMessageToPaxosLeader(msg);		
 	}
 	
 	
 
 	//method used to process abort msg from paxos leader
 	public void ProcessAbortMessage(UUID uid)
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);
 		temp.state=AcceptorState.ABORT;
 		this.uidTransactionStatusMap.put(uid, temp);
 	}
 
 	public void ProcessCommitToFile(UUID uid, int gsn)
 	{
 		if (gsn == this.lastCommitGSN + 1)
 		{			
 			ProcessAppendToFile(uid);
 			
 			while(true)
			{							
 				Tuple<Integer,UUID> temptuple=queue.peek();
 				if(temptuple.x==this.lastCommitGSN+1)
 				{
 					ProcessAppendToFile(temptuple.y);
 					queue.remove();
 				}
 				else
 				{
 					break;
 				}
 			}
 		}
 		else
 		{
 			//TODO : ADD TO BUFFER.	
 			this.queue.add(new Tuple(gsn,uid));	
 		}
 	}
 
 	public void ProcessAppendToFile(UUID uid)
 	{
 		this.localResource.AppendtoResource(this.uidTransactionStatusMap.get(uid).data);
 		this.lastCommitGSN += 1;
 	}
 	
     //to broadcast to other acceptors about the commit
 	public void BcastCommitMessage(UUID uid) throws IOException
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);	
 		//PaxosMsg commitmsg = new PaxosMsg(this.nodeId, Common.PaxosMsgType.COMMIT, uid, temp.gsn);
 		BcastMsg bcastmsg=new BcastMsg(this.nodeId, BcastMsgType.COMMIT_ACK, temp.gsn,uid,temp.data);
 		SendBcastMessage(bcastmsg);
 	}
 
 	public void SendBcastMessage(BcastMsg msg) throws IOException
 	{
 		//Print msg
 		System.out.println("Sent " + msg);
 
 		MessageWrapper msgwrap = new MessageWrapper(Common.Serialize(msg), msg.getClass());
 		this.messageController.SendMessage(msgwrap, this.paxosLeaderExchange, "");	
 	}
 	
 	//method used to process commit acknowledgement from other acceptors
 	public void ProcessCommitAckMessage(UUID uid, int gsn, String nodeid,String data)
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);		
 		if(temp==null){
 			temp = new TransactionStatus();
 			
 			temp.data = data;
 			temp.state = AcceptorState.COMMIT;
 			temp.timeout=new Timestamp(new Date().getTime());
 			temp.Acceptors=new HashSet<String>();
 			temp.Acceptors.add(nodeid);
 			temp.gsn=gsn;
 			this.uidTransactionStatusMap.put(uid, temp);
 			ProcessCommitToFile(uid, gsn);
 		}
 		else{
 		temp.Acceptors.add(nodeid);
 		System.out.println("Acceptor List (commit ack) " + temp.Acceptors.toString());
 		if(temp.Acceptors.size()== Common.NoAcceptors)
 		{			
 			System.out.println(" total no of acceptors reached ");
 			temp.state=AcceptorState.COMMIT_ACK;
 			this.uidTransactionStatusMap.put(uid, temp);
 		}
 		else if (temp.state == AcceptorState.ACCEPT)
 		{	
 			temp.state = AcceptorState.COMMIT;
 			temp.gsn = gsn;
 			this.uidTransactionStatusMap.put(uid, temp);
 			ProcessCommitToFile(uid, gsn);
 		}
 		}
 
 	}
 
 	//method used to process abort acknowledgement from other acceptors
 	public void ProcessAbortAckMessage(UUID uid, String nodeid)
 	{
 		TransactionStatus temp = this.uidTransactionStatusMap.get(uid);		
 		temp.Acceptors.add(nodeid);
 
 		if(temp.Acceptors.size()== Common.NoAcceptors)
 		{			
 			temp.state=AcceptorState.ABORT_ACK;				 
 		}
 		else if (temp.state == AcceptorState.ACCEPT)
 		{	
 			temp.state = AcceptorState.ABORT;			
 		}
 
 		this.uidTransactionStatusMap.put(uid, temp);
 
 	}
 
 }
