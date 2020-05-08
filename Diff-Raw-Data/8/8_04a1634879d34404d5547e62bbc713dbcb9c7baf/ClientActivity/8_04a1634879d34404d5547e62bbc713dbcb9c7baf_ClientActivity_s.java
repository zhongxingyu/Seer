 package Core_System;
 
 import java.sql.Connection;
 import java.sql.Date;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import Data_Access.AccountsDBManager;
 import Data_Access.ActivitysDBManager;
 import Data_Access.ClientsDBManager;
 import Data_Access.ConnectionPoolManager;
 import Data_Access.DepositsDBManager;
 import Data_Access.PropertiesDBManager;
 import Data_Access.Type;
 
 public class ClientActivity {
 	private int id;
 	private Client client;
 	private Account account;
 	protected ConnectionPoolManager conn;
 
 	public ClientActivity(Client client) {
 		this.client = client;
 
 	}
 	
 	/** View client details **/
 	public Client getClientDetails(int client_id) throws MbankException {
		//TODO validate if the client exist if not throw Mbank exception 
 		ConnectionPoolManager connn = new ConnectionPoolManager();
 		 client = ClientsDBManager.getInstance().selectClient(
 				connn.getConnectionFromPool(), client_id);
 		if (client_id == client.getClient_id()){
 			return client;
		}else {			
 		}
		throw new MbankException("No Client found");
 	}
 
 	/**view Account Details**/
 	public Account viewAccountDetails(int account_id)  throws MbankException {	 
 		ConnectionPoolManager connn = new ConnectionPoolManager();
 		client = ClientsDBManager.getInstance().selectClient(
 				connn.getConnectionFromPool(), client.getClient_id());
 		 account = AccountsDBManager.getInstance().getAccount(
 				connn.getConnectionFromPool(), account_id);
 		
 		System.out.println(account);
 		if (account.getClient_id() == client.getClient_id()) {
 System.out.println("\n your account deails is : >>> " + account);
 			return account;
 		} else {
 		}
 		throw new MbankException("  No account Found ");
 	}
 
 	/**update Client Details**/
 	public void updateClientDetails(Client client) throws MbankException {
 		ConnectionPoolManager con = new ConnectionPoolManager();
 		if (ClientsDBManager.getInstance().selectClient(con.getConnectionFromPool(), client.getClient_id()) !=null){
 		ClientsDBManager.getInstance().updateclients(
 				con.getConnectionFromPool(), client);
 		System.out.println("\n your Client Details is updated  :" + client);
 		}else{
 			throw new MbankException("update Failed");
 		}
 	}
 
 	/**withdraw from account**/
 	public boolean withdraw(int account_id, double amount) throws MbankException {
 		boolean withdraw  = false;
 		ConnectionPoolManager con = new ConnectionPoolManager();
 		Account ac = new Account(account_id);
 		account = AccountsDBManager.getInstance().getAccount(con.getConnectionFromPool(), ac.getAccount_id());
 		if (amount <= 0) {
 			
 			throw new MbankException("illegal amount: " + amount);
 		}
 		if (account.getBalance() - amount >= (account.getCredit_limit()*(-1))) {
 			account.setBalance(account.getBalance() - amount);
 			AccountsDBManager.getInstance().updateAccount(
 					con.getConnectionFromPool(), account);
 			withdraw  = true;
 //			Client cl = new Client(ac.getClient_id());
 //			String commission = "0.5"; 
 //			String description = "withdraw";
 			
 //			java.util.Date utilDate = new java.sql.Date(System.currentTimeMillis());
 			// Convert it to java.sql.Date
 //			java.sql.Date sqlDate = (Date) utilDate; 
 			
 //			//long t = today.getTime();
 //			//java.sql.Date dt = new java.sql.Date(t);
 			
 //			List<Properties> properties1 = new ArrayList<Properties>();
 			
 //			properties1 = PropertiesDBManager.getInstance().getAllProperties(con.getConnectionFromPool());
 			// DTOD i need to resolve the date problem
 //			Activity act = new Activity(0, cl.getClient_id(), amount,sqlDate, commission, description);
 //			ActivitysDBManager.getInstance().createNewActivity(con.getConnectionFromPool(), act);
 		} else {
 			
 			throw new MbankException("you don't have sufficient credit");
 		}
 		System.out.println(withdraw);
 		return  withdraw;
 		
 	}
 
 	/**createNewDeposit in to Deposit Table**/
 	/***** @throws MbankException**/
 	public void createNewDeposit(Deposit deposit)
 			throws MbankException {
 		
 		ConnectionPoolManager connn = new ConnectionPoolManager();
 		client = ClientsDBManager.getInstance().selectClient(
 				connn.getConnectionFromPool(), client.getClient_id());
 
 		deposit.setClient_id(client.getClient_id());
 		deposit.setType(client.getType());
 		if (deposit.getBalance() <= 0) {
 			throw new MbankException("illegal amount: " + deposit.getBalance());
 		}
 		if(client.getType() == Data_Access.Type.REGULAR ){
 			deposit.setEstimated_balance((deposit.getBalance() * 0.5)+(deposit.getBalance()));
 		} else if (client.getType() == Data_Access.Type.GOLD ){
 			deposit.setEstimated_balance((deposit.getBalance() * 0.7)+(deposit.getBalance()));
 		}else if (client.getType() == Data_Access.Type.PLATINUM ){
 			deposit.setEstimated_balance((deposit.getBalance() * 0.8)+(deposit.getBalance()));
 		}
 
 		DepositsDBManager.getInstance().createNewDeposit(
 				connn.getConnectionFromPool(), deposit);
 		// TODO close the client Deposits when the closing date is arrive.
 		// TODO Calculate the commission and the Interest FOR the deposits
 	}
 
 	/** deposit depositInToAccount balance **/
 	public void depositInToAccount(int account_id, double amnout)
 			throws MbankException {
 		ConnectionPoolManager con = new ConnectionPoolManager();
 		account = AccountsDBManager.getInstance().getAccount(
 				con.getConnectionFromPool(), account_id);
 		if (amnout <= 0) {
 			throw new MbankException("illegal amount: " + amnout);
 		}
 		account.setBalance(account.getBalance() + amnout);
 		AccountsDBManager.getInstance().updateAccount(
 				con.getConnectionFromPool(), account);
 	}
 	
 
 		
 		/**getAllClientDeposits**/
 		
 		public List<Deposit> viewAllClientDeposits(int client_id){
 			List<Deposit> deposits = new ArrayList<Deposit>();
 			ConnectionPoolManager con = new ConnectionPoolManager();
 			deposits = DepositsDBManager.getInstance().getAllClientDeposits(con.getConnectionFromPool(),client_id);
 			for (Deposit d : deposits){
 			System.out.println(d);	
 			}
 			
 		return deposits;
 	}
 		
 	
 		
 		/**get Account By Id from client action**/
 		public Account getAccountBy_Id(int client_id) {
 			ConnectionPoolManager con = new ConnectionPoolManager();
 			client = ClientsDBManager.getInstance().selectClient(con.getConnectionFromPool(), client_id);
 			Account ac = new Account(client.getClient_id());
 			ac = AccountsDBManager.getInstance().getAccountByClientId(con.getConnectionFromPool(), client.getClient_id());
 			System.out.println("get Account By Id from client action  " + ac);
 			return ac;
 		}
 		
 		public List<Activity> ViewClientActivities(int Client_id) throws MbankException {
 			ConnectionPoolManager con = new ConnectionPoolManager();
 			List<Activity> activitys = new ArrayList<Activity>(Client_id);
 			activitys = ActivitysDBManager.getInstance().getClientActivities(con.getConnectionFromPool(),Client_id);
 				for (Activity c : activitys)
 					System.out.println(c);
 		return activitys;
 		}
 
 		
 	
 	/**close Deposit it should be automatic**/
 	public void PreOpenDeposit(int deposit_id , int client_id, int account_id) throws MbankException {		
 		ConnectionPoolManager con = new ConnectionPoolManager();
 		Client cl = new Client(client_id);
 		Deposit dp = new Deposit(deposit_id);
 		dp = DepositsDBManager.getInstance().getDepositById(con.getConnectionFromPool(), dp.getDeposit_id());
 			
 		if (cl.getClient_id() !=(dp.getClient_id())) {
 			throw new MbankException("No deposit found for this client: check yout deposit ID");
 		} else if (dp.getBalance() >= 1){
 			Account ac = new Account(account_id);
 			ac = AccountsDBManager.getInstance().getAccount(con.getConnectionFromPool(), ac.getAccount_id());
 			ac.setBalance(dp.getBalance() + ac.getBalance() - (dp.getBalance() * 0.1));
 			
 			AccountsDBManager.getInstance().updateAccount(con.getConnectionFromPool(), ac);
 			dp.setBalance(0);		
 			DepositsDBManager.getInstance().updateDeposit(con.getConnectionFromPool(), dp);		
 		}else {
 			throw new MbankException("cant run the method  seccse........");	
 		} 
  }
 	
 
 	/**pre Open Deposit**/
 	public void preOpenDeposit() {
 		// TODO "Money is transferred after charging a commission"
 	}
 
 
 	
 //	public double getClientBalance() {
 //		// TODO Auto-generated method stub
 //		return this.account.getBalance();
 //	}
 }
