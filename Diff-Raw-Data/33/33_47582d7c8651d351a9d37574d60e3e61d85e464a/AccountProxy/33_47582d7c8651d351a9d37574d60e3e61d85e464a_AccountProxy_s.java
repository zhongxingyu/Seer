 package cash_access;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.Semaphore;
 
 import mware_lib.CommunicatorBindings;
 import mware_lib.ExceptionMessage;
 import mware_lib.MessageId;
 import mware_lib.MessageSemaphores;
 import mware_lib.ReplyMessage;
 import mware_lib.ReplyMessageQueue;
 import mware_lib.RequestMessage;
 import mware_lib.ResultMessage;
 
 public final class AccountProxy extends Account {
 
 	private String name;
 	private InetSocketAddress address;
 
 	public AccountProxy(String name, InetSocketAddress address) {
 		this.name = name;
 		this.address = address;
 	}
 
 	@Override
 	public void deposit(double amount) {
 		long id = MessageId.getNewId();
 		Semaphore sem = MessageSemaphores.create(id);
 		RequestMessage req = new RequestMessage(id, name, "deposit", amount
 				+ "");
 		CommunicatorBindings.getCommunicator(address).send(req.toString());
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		ReplyMessage rep = ReplyMessageQueue.pop(id);
 		if (rep.exception()) {
 			throw new RuntimeException(((ExceptionMessage) rep).message());
 		}
 	}
 
 	@Override
 	public void withdraw(double amount) throws OverdraftException {
 		long id = MessageId.getNewId();
 		Semaphore sem = MessageSemaphores.create(id);
 		RequestMessage req = new RequestMessage(id, name, "withdraw", amount
 				+ "");
 		CommunicatorBindings.getCommunicator(address).send(req.toString());
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		ReplyMessage rep = ReplyMessageQueue.pop(id);
 		if (rep.exception()) {
 			ExceptionMessage exc = (ExceptionMessage) rep;
			if (exc.type().equals("cash_access.OverdraftException"))
 				throw new OverdraftException(exc.message());
 			else
 				throw new RuntimeException(((ExceptionMessage) rep).message());
 		}
 	}
 
 	@Override
 	public double getBalance() {
 		long id = MessageId.getNewId();
 		Semaphore sem = MessageSemaphores.create(id);
 		RequestMessage req = new RequestMessage(id, name, "getBalance",
				"");
 		CommunicatorBindings.getCommunicator(address).send(req.toString());
 		try {
 			sem.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		ReplyMessage rep = ReplyMessageQueue.pop(id);
 		if (rep.exception()) {
 			throw new RuntimeException(((ExceptionMessage) rep).message());
 		}
 		return Double.valueOf(((ResultMessage)rep).value());
 	}
 
 }
