 import java.util.*;
 public class CommercialAccount extends Account
 {
 	private ArrayList<Service_Location> locations;
 
	public CommercialAccount(String clientFirstName, String clientLastName, int accountID, double balance, boolean flag, Date deadline, Address billingAddress)
 	{
		this.clientFirstName = clientFirstName;
		this.clientLastName = clientLastName;
 		this.accountID = accountID;
 		this.balance = balance;
 		this.flag = flag;
 		this.deadline = deadline;
 		this.billingAddress = billingAddress;
 	}
 
 	public ArrayList<Service_Location> getLocations() {
 		return locations;
 	}
 
 	public void setLocations(ArrayList<Service_Location> locations) {
 		this.locations = locations;
 	}
 
 }
