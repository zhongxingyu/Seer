 /*
  * Will eventually contain data associated with a single Patron for
  * project library
  */
 package project_library;
 
 /**
  *
  * @author jacob
  */
 public class Patron 
 {
     private boolean isSet;
     private String firstName;
     private String lastName;
     private String street;
     private String city;
     private String state;
     private String zipCode;
     private String phone;
     private String email;
     private String restrictedTo;
     private String membershipStatus;
     private String birthday;
     private double fine;
     private double specialFine;
     private static int patronID;
     private int[] checkedBooks;
    
    Patron()
    {
        isSet = false;
    }
     public String getFirstName()
     {
         return firstName;
     }
    
     public String getLastName()
     {
         return lastName;
     }
    
     public String getStreet()
     {
         return street;
     }
    
     public String getCity()
     {
         return city;
     }
    
     public String getState()
     {
         return state;
     }
    
     public String getZipCode()
     {
         return zipCode;
     }
    
     public String getPhone()
     {
         return phone;
     }
    
     public String getEmail()
     {
         return email;
     }
    
     public String getRestrictedTo()
     {
         return restrictedTo;
     }
    
     public String getMembershipStatus()
     {
         return membershipStatus;
     }
    
     public String getBirthday()
     {
         return birthday;
     }
    
     public double getFine()
     {
         return fine;
     }
    
     public double getSpecialFine()
     {
         return specialFine;
     }
    
     public int[] getCheckedBooks()
     {
         return checkedBooks;
     }
    
     public static int getPatronID()
     {
         return patronID;
     }
    
     public void setFirstName(String newValue)
     {
         firstName = newValue;
     }
    
     public void setLastName(String newValue)
     {
         lastName = newValue;
     }
    
     public void setStreet(String newValue)
     {
         street = newValue;
     }
    
     public void setCity(String newValue)
     {
         city = newValue;
     }
    
     public void setState(String newValue)
     {
         state = newValue;
     }
    
     public void setZipCode(String newValue)
     {
         zipCode = newValue;
     }
    
     public void setPhone(String newValue)
     {
         phone = newValue;
     }
    
     public void setEmail(String newValue)
     {
         email = newValue;
     }
    
     public void setRestrictedTo(String newValue)
     {
         restrictedTo = newValue;
     }
    
     public void setBirthday(String newValue)
     {
         birthday = newValue;
     }
    
     public void setFine(double newValue)
     {
         fine = newValue;
     }
    
     public void setSpecialFine(double newValue)
     {
         specialFine = newValue;
     }
    
     public void setCheckedBooks(int[] newValue)
     {
         checkedBooks = newValue;
     }
     
     public void setCheckedBooks(int newValue)
     {
         int[] temp = new int[1];
         temp[0] = newValue;
         checkedBooks = temp;
     }
    
     public void setPatronID(int newValue)
     {
         patronID = newValue;
     }
     
     public boolean isSet()
     {
         return isSet;
     }
     
     public void setIsSet()
     {
         isSet = true;
     }
     
     public void setMembershipStatus(String status)
     {
         membershipStatus=status;
     }
 
 }
