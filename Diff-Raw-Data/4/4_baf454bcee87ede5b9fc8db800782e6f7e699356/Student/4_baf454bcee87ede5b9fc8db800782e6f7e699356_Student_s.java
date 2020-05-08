 package Student;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import Degree.Degree;
 
 /**
  * Student class to store applicant information
  */
 public class Student{
 
 	//for the first page
 	private String fName;
 	private String midInitial;
 	private String lName;
 	
 	//second page
 	private int cid;
 	
 	//third page
 	private int rid;
 	
 	//fourth page
 	private String stAddress;
 	private String city;
 	private String state;
 	private String zipCode;
 	private String areaCode;
 	private String telephoneCode;
 	private String phoneNumber;
 	
 	private ArrayList<Degree> degrees;
 	
 	//specialization
 	private int sid;
 	
 	public Student(){
 		fName = "";
 		midInitial = "";
 		lName = "";
 		
 		stAddress = "";
 		city = "";
 		state = "";
 		zipCode = "";
 		areaCode = "";
 		telephoneCode = "";
 		phoneNumber = "";
 		
 		cid = -1;
 		rid = -1;
 		sid = -1;
 		
 		degrees = new ArrayList<Degree>();
 	}
 	
 	public void setFName(String newFName){
 		this.fName = newFName;
 	}
 	
 	public String getFName(){
 		return this.fName;
 	}
 	
 	public void setMidInitial(String newMidInitials){
 		this.midInitial = newMidInitials;
 	}
 	
 	public String getMidInitial(){
 		return this.midInitial;
 	}
 	
 	public void setLName(String newLName){
 		this.lName = newLName;
 	}
 	
 	public String getLName(){
 		return this.lName;
 	}
 	
 	public void setStAddress(String newStAddress){
 		this.stAddress = newStAddress;
 	}
 	
 	public String getStAddress(){
 		return this.stAddress;
 	}
 	
 	public void setCity(String newCity){
 		this.city = newCity;
 	}
 	
 	public String getCity(){
 		return this.city;
 	}
 	public void setState(String newState){
 		this.state = newState;
 	}
 	
 	public String getState(){
 		return this.state;
 	}
 	
 	public void setZipCode(String newZipCode){
 		this.zipCode = newZipCode;
 	}
 	
 	public String getZipCode(){
 		return this.zipCode;
 	}
 	
 	public void setAreaCode(String newAreaCode){
 		this.areaCode = newAreaCode;
 	}
 	
 	public String getAreaCode(){
 		return this.areaCode;
 	}
 	
 	public void setTelephoneCode(String newTelephoneCode){
 		this.telephoneCode = newTelephoneCode;
 	}
 	
 	public String getTelephoneCode(){
 		return this.telephoneCode;
 	}
 	
 	public void setPhoneNumber(String newPhoneNumber){
 		this.phoneNumber = newPhoneNumber;
 	}
 	
 	public String getPhoneNumber(){
 		return this.phoneNumber;
 	}
 	
 	public void setCID(int newCID){
 		if(newCID>=0)
 			this.cid = newCID;
 	}
 	
 	public int getCID(){
 		return this.cid;
 	}
 	
 	public void setRID(int newRID){
 		if(newRID>=0)
 			this.rid = newRID;
 	}
 	
 	public int getRID(){
 		return this.rid;
 	}
 	
 	public void setSID(int newSID){
 		if(newSID>=0)
 			this.sid = newSID;
 	}
 	
 	public int getSID(){
 		return this.sid;
 	}
 	
 	public void addDegree(Degree theDegree){
 		this.degrees.add(theDegree);
 	}
 	
 	public Degree getDegree(int i){
 		if(i>=0 && i < degrees.size())
 			return degrees.get(i);
 		else
 			return null;
 	}
 
 	public int numOfDegrees(){
 		return this.degrees.size();
 	}
 }
