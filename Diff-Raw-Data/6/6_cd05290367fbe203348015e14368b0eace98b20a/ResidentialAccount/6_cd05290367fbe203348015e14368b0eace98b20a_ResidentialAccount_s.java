 import java.util.*;
 public class ResidentialAccount extends Account
 {
 	private Service_Location location;
 	
	public ResidentialAccount(String clientName, int accountID, double balance, boolean flag, Date deadline, Address billingAddress)
 	{
		this.clientName = clientName;
 		this.accountID = accountID;
 		this.balance = balance;
 		this.flag = flag;
 		this.deadline = deadline;
 		this.billingAddress = billingAddress;
 	}
 	 
 	public Service_Location getLocation() {
 		return location;
 	}
 
 
 	public void setLocation(Service_Location location) {
 		this.location = location;
 	}
 		
 }
