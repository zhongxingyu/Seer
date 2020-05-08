 package cash_access;
 
 import mware_lib.Communicator;
 import mware_lib.Utilities;
 
 final class WithdrawCaller extends Thread {
 	private final Communicator comm;
 	private final long msgId;
 	private final Account account;
 	private final String param;
 
 	WithdrawCaller(Communicator comm, long msgId, Account account,
 			String param) {
 		this.comm = comm;
 		this.msgId = msgId;
 		this.account = account;
 		this.param = param;
 	}
 
 	@Override
 	public void run() {
 		try {
 			double amount=Double.valueOf(param);
 			account.withdraw(amount);
 			comm.send(Utilities.join(",", "result", String.valueOf(msgId),
 					"void"));
 		} catch (Exception e) {
			comm.send(Utilities.join(",", "exception", e.getClass().toString(),
 					e.getMessage()));
 		}
 	}
 }
