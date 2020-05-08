 package cc.warlock.core.configuration;
 
 public class Account {
 
 	private String accountName, password, accountId;
 
 	public String getAccountName() {
 		return accountName;
 	}
 
 	public void setAccountName(String accountName) {
 		this.accountName = accountName;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getAccountId() {
 		return accountId;
 	}
 
 	public void setAccountId(String accountId) {
 		this.accountId = accountId;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof String)
 		{
 			return accountName.equals(obj);
 		}
 		
 		return super.equals(obj);
 	}
 }
