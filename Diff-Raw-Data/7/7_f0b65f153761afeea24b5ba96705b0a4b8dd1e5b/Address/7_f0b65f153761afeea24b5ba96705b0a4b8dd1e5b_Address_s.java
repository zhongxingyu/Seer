 package BackEnd.UserSystem;
 
 /**
  * This class represents an Address.
  * @author Anderson Santana
  */
 public class Address {
     private String street;
     private String city;
     private String state;
     private String zipCode;
     private String country;
     private final int ZIP_CODE_LENGTH = 5;
     
     /**
      * Default Constructor.
      */
     public Address(){
     }
     
     /**
      * Constructor.
      * @param street The street of the address
      * @param city  The city of the address
      * @param state The state of the address
      * @param zipCode The zip code of the address
      * @param country The country of the address
      */
     public Address(String street, String city, String state, 
             String zipCode, String country){
         this.street = street;
         this.city = city;
         this.state = state;
         this.zipCode = zipCode;
         this.country = country;
     }
     
     /**
      * It sets the street of the address.
      * @param street The street of the address
      */
     public void setStreet(String street){
         this.street = street;
     }
     
     /**
      * It returns the street of the address.
      * @return The street of the address
      */
     public String getStreet(){
         return street;
     }
     
     /**
      * It sets the city of the address.
      * @param city The city of the address
      */
     public void setCity(String city){
         this.city = city;
     }
     
     /**
      * It returns the city of the address.
      * @return The city of the address
      */
     public String getCity(){
         return city;
     }
     
     /**
      * It sets the state of the address.
      * @param state The state of the address
      */
     public void setState(String state){
         this.state = state;
     }
     
     /**
      * It returns the state of the address.
      * @return The state of the address
      */
     public String getState(){
         return state;
     }
     
     /**
      * It sets the zip code of the address.
      * @param zipCode The zip code of the address
      */
     public void setZipCode(String zipCode){
         //THROWS Exception
         //try
         {
         }
         //catch ZipCodeInvalidFormatException
                 
     }
     
     /**
      * It returns the zip code of the address.
      * @return The zip code of the address
      */
     public String getZipCode(){
         return zipCode;
     }
     
     /**
      * 
      * @param zipCode
      * @return true if 
      */
     private boolean verifyZipCodeFormat(String zipCode){
         return true;
     }
     
     public void setCountry(String country){
     }
     
     public String getCountry(){
         return country;
     }
     
    /** Determines whether the current object matches its argument.
     * @param obj The object to be compared to the current object
     * @return true if the object has the same street, city, state,
     *      zip code and country; otherwise, return false     
     */    
   @Override    
   public boolean equals(Object obj){
     if(obj == this) return true;
     if(obj == null) return false;
     if(this.getClass() == obj.getClass()){
       Address other = (Address) obj;
       return street.equals(other.street) && city.equals(other.city) && 
                  state.equals(other.state) && zipCode.equals(other.zipCode);
         } else {
           return false;
         }
   }
   
   /**
    * Creates a string representation of the address.
    * @return A string representation of the address.
    */
   @Override
   public String toString(){
     String info = "Street: " + street +
                   "\nCity: " + city + 
                   "\nState: " + state + 
                   "\nZip Code: " + zipCode + 
                   "\nCountry: " + country;    
     return info;
   }
 }
