 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mmo.Money;
 
 import java.util.Date;
 
 /**
  *
  * @author Xaymar
  */
 public class Account {
 
 	private MoneyDB account;
 	private Date lastModified;
 	private boolean changed;
 	private Money api;
 
 	public Account() {
 		api = new Money();
 	}
 
 	protected MoneyDB getAccount() {
 		return account;
 	}
 
 	protected void setAccount(MoneyDB account) {
 		this.account = account;
 		this.setChanged(true);
 	}
 
 	public String getOwner() {
 		return account.getOwner();
 	}
 
 	protected void setOwner(String owner) {
 		account.setOwner(owner);
 	}
 
 	protected long getMoneyAmount() {
 		return account.getAmount();
 	}
 
 	protected void setMoneyAmount(long moneyAmount) {
 		account.setAmount(moneyAmount);
 	}
 
 	protected boolean isChanged() {
 		return changed;
 	}
 
 	protected void setChanged(boolean changed) {
 		this.changed = changed;
 		if (changed == true) {
 			this.lastModified.setTime(System.currentTimeMillis());
 		}
 	}
 
 	//Helpful functions
 	public long getMoney() {
 		return account.getAmount();
 	}
 
 	public TransactionDB setMoney(long money) {
 		TransactionDB tdb;
 		long total = money - account.getAmount();
 		if (total < 0) {
 			tdb = takeMoney(-total);
 		} else {
 			tdb = giveMoney(total);
 		}
 		tdb.setReason("setMoney");
 		return tdb;
 	}
 
 	public TransactionDB giveMoney(long money) {
 		TransactionDB tdb = api.getAccount("SERVER").transferMoney(this, money);
 		tdb.setReason("giveMoney");
 		return tdb;
 	}
 
 	public TransactionDB takeMoney(long money) {
 		TransactionDB tdb = transferMoney(api.getAccount("SERVER"), money);
 		tdb.setReason("takeMoney");
 		return tdb;
 	}
 
 	public TransactionDB transferMoney(Account to, long amount) {
 		TransactionDB tdb = createTransaction(this, to, amount, "transferMoney");
 		if (account.getAmount() >= amount) {
 			account.setAmount(account.getAmount() - amount);
 			to.getAccount().setAmount(to.getAccount().getAmount() + amount);
 			setChanged(true);
 			to.setChanged(true);
 		} else {
 			tdb.setFailed(true);
 		}
 		return tdb;
 	}
 
 	public TransactionDB createTransaction(Account from, Account to, long amount, String reason) {
 		TransactionDB tdb = new TransactionDB();
 		tdb.setFromAccount(from.getAccount());
 		tdb.setToAccount(to.getAccount());
 		tdb.setAmount(amount);
 		tdb.setReason(reason);
 		tdb.setOnDate(new Date());
 		tdb.setFailed(false);
 		return tdb;
 	}
 }
