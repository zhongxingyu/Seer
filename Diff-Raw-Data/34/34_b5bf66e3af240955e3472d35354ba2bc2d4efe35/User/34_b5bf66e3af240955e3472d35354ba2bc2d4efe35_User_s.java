 package de.raulin.rosario.helloCloud.usermanagement;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class User {
 
 	protected String name;
 	protected String email;
 	protected byte[] password;
 	protected final List<Integer> vids;
 	protected AccountStatus status;
 
	// TODO: throw EmptyInputException if name.isEmpty() == true
 	public User(final String email, final String name, final String password)
 			throws EmptyInputException {
		if (email == null || password == null || email.isEmpty()
				|| password.isEmpty()) {
 			throw new EmptyInputException();
 		}
		this.name = name;
		this.email = email;
		this.password = hash(password);
		this.vids = new ArrayList<Integer>();
		this.status = AccountStatus.OK;
 	}
 
 	public User(final String email, final String password)
 			throws EmptyInputException {
		this(email, null, password);
 	}
 
 	protected byte[] hash(final String password) {
 		try {
 			MessageDigest md;
 			md = MessageDigest.getInstance("SHA-1");
 			md.update(password.getBytes());
 			return md.digest();
 		} catch (NoSuchAlgorithmException e) {
 			// This will never happen since SHA-1 is required.
 			return new byte[1];
 		}
 	}
 
 	public AccountStatus getStatus() {
 		return status;
 	}
 
 	public void setStatus(AccountStatus status) {
 		this.status = status;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public byte[] getPassword() {
 		return password;
 	}
 
 	public List<Integer> getVids() {
 		return vids;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public void setPassword(String password) {
 		this.password = hash(password);
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 }
