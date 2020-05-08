 package edu.cst438.fourup;
  
 import java.util.ArrayList;
 import java.util.List;
  
 public class AccountObject
 {
 	private int id = 0;
 	private String email = "";
 	private String hPassword = "";
 	private List<String> searchHistory = new ArrayList<String>();
 	
 	//getter and setter methods
 	public int getId()
 	{
 		return this.id;
 	}
 	
 	public void setId(int id)
 	{
 		this.id = id;
 	}
 	
 	public String getEmail()
 	{
 		return this.email;
 	}
 	
 	public void setEmail(String email)
 	{
 		this.email = email;
 	}
 	
 	public String getHPassword()
 	{
 		return this.hPassword;
 	}
 	
 	public void setHPassword(String hPassword)
 	{
 		this.hPassword = hPassword;
 	}
 	
	public ArrayList getSearchHistory()
 	{
 		return this.searchHistory;
 	}
 	
	public void setSearchHistory(ArrayList searchHistory)
 	{
 		this.searchHistory = searchHistory;
 	}
 }
