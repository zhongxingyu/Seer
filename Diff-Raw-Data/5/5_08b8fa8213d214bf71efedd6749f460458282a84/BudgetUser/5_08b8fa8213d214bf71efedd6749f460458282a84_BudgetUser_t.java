 package sheridan;
 
 public class BudgetUser {
 
 	String mName;
 	String mEmail;
 	double mtotalCash;
 	
 	public BudgetUser(String name, String email, double totalCash) {
 		mName = name;
 		mEmail = email;
 		mtotalCash = totalCash;
 	}
 
	public BudgetUser() {
		mName = "";
		mEmail = "";
		mtotalCash = 0;
	}
 	
 	public String getName() {
 		return mName;
 	}
 	public String getEmail() {
 		return mEmail;
 	}
 	public double getTotalCash() {
 		return mtotalCash;
 	}
 	public void setName(String mName) {
 		this.mName = mName;
 	}
 	public void setEmail(String mEmail) {
 		this.mEmail = mEmail;
 	}
 	public void setTotalCash(double totalCash) {
 		this.mtotalCash = totalCash;
 	}
 	
 	
 }
