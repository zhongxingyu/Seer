 package cash_access;
 
 import mware_lib.Communicator;
 import mware_lib.Utilities;
 
 public class GetBalanceCaller extends Thread {
 	private final Communicator comm;
 	private final long msgId;
 	private final Account account;
 	
 	GetBalanceCaller(Communicator comm, long msgId, Account account) {
 		this.comm = comm;
 		this.msgId = msgId;
 		this.account = account;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			double result = account.getBalance();
 			comm.send(Utilities.join(",", "result", String.valueOf(msgId),
 					String.valueOf(result)));
 		} catch (Exception e) {
			comm.send(Utilities.join(",", "exception",String.valueOf(msgId), e.getClass().toString(),
 					e.getMessage()));
 		}
 	}
 }
