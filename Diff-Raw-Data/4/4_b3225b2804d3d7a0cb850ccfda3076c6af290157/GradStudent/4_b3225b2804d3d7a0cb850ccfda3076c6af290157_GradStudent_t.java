 package gradstudent;
 
 import java.io.*;
 import java.util.*;
 
 public class GradStudent {
   private String firstName;
   private String middleName;
   private String lastName;
   private String citizenship;
   private String residence;
   private Address address;
   private boolean foreign;
   private ArrayList<Degree> degrees;
   private int currentDegree;
   private String specialization;
 

   public GradStudent() {
     firstName = "";
     middleName = "";
     lastName = "";
   }
 
   /* student constructor with parameters */
   public GradStudent( String firstName, String middleName, String lastName) {
     super();
     this.firstName = firstName;
     this.middleName = middleName;
     this.lastName = lastName;
     degrees = new ArrayList<Degree>();
     currentDegree = 0;
   }
 
   public String getFirstName() {
     return firstName;
   }
 
   public void setFirstName(String firstName) {
     this.firstName = firstName;
   }
 
   public String getMiddleName() {
     return middleName;
   }
 
   public void setMiddleName(String middleName) {
     this.middleName = middleName;
   }
 
   public String getLastName() {
     return lastName;
   }
 
   public void setLastName(String lastName) {
     this.lastName = lastName;
   }
 
   public void setCitizenship(String citizenship) {
     this.citizenship = citizenship;
   }
 
   public String getCitizenship() {
     return this.citizenship;
   }
 
   public void setResidence(String residence) {
     this.residence = residence;
   }
 
   public String getResidence() {
     return this.residence;
   }
 
   public void setAddress(String streetNumber,
    String streetName,
    String city,
    String stateCode,
    String zipcode,
    String areaCode,
    String phoneNumber) {
     address = new Address( streetNumber,
       streetName,
       city, stateCode,
       zipcode,
       areaCode,
       phoneNumber);
   }
 
   public Address getAddress() {
     return this.address;
   }
 
   public void setForeign(boolean bool) {
     this.foreign=bool;
   }
 
   public boolean getForeign() {
     return this.foreign;
   }
 
   public void newDegree(String loc,
    String uni,
    String discipline,
    String month,
    String year,
    String gpa) {
     Degree newDeg = new Degree( loc, uni, discipline, month, year, gpa);
     //currentDegree++;
     degrees.add(newDeg);
   }
 
   public ArrayList<Degree> getDegrees() {
     return this.degrees;
   }
 
   public int getCurrentDegree() {
     return this.currentDegree;
   }
 
   public void setSpecialization(String specialization) {
     this.specialization = specialization;
   }
 
  public String getSpecialization() {
     return this.specialization;
   }
 
 }
