 package requests;
 
 public class LogInRequest extends XMLParsable {
 
 	private String username, password;
 	private boolean accepted;
 	
 	public LogInRequest(String username, String password) {
 		this.username = username;
 		this.password = password;
 		this.accepted = false;
 		addVariable("username", this.username);
 		addVariable("password", this.password);
 		addVariable("accepted", accepted);
 	}
 	
 	public LogInRequest(String username, String password, boolean isAccepted) {
 		this(username, password);
 		this.accepted = isAccepted;
 	}
 	
 	public boolean isAccepted() {
 		return accepted;
 	}
 	
 	public void setAccepted(boolean isAccepted) {
 		this.accepted = isAccepted;
		for (int i = 0; i < getNumberOfVariables(); i++) {
			if(getVariableName(i).equals("accepted")) {
				removeVariable(i);
				addVariable("accepted", isAccepted ? "true" : "false");
			}
		}
 	}
 	
 	@Override
 	public String getClassName() {
 		return "LogInRequest";
 	}
 	
 	public String getUserName() {
 		return username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 	
//	public static void main(String[] args) {
//		LogInRequest a = new LogInRequest("Arne", "qazqaz");
//		String xml = a.toXml();
//		System.out.println(xml);
//		LogInRequest lir = (LogInRequest)XMLParsable.toObject(xml);
//		System.out.println("Username: " + lir.getUserName());
//		System.out.println("Password: " + lir.getPassword());
//		System.out.println("Accepted: " + lir.isAccepted());
//	}
 }
