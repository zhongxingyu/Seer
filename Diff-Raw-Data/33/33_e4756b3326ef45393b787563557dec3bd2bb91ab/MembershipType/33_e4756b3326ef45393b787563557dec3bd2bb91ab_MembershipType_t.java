 package BE;
 
 public class MembershipType
 {
 
     private int id;
     private String membershipName;
     private int price;
 
    /*
     * Constructor for membership type with everything
     */
     public MembershipType(int id, String membershipName, int price)
     {
         this.id = id;
         this.membershipName = membershipName;
         this.price = price;
     }
    
    /*
     * Constructor for membership type without ID
     */
     public MembershipType(String membershipName, int price)
     {
         this(-1, membershipName, price);
     }
 
    /*
     * Constructor for membership type with the ID and a membership type object
     */
     public MembershipType(int id, MembershipType mt)
     {
         this(mt.membershipName, mt.price);
     }
 
     /**
     * Returns the id of a membership type
      * @return the id
      */
     public int getId()
     {
         return id;
     }
 
     /**
     * Returns the name of a membership type
      * @return the membershipName
      */
     public String getMembershipName()
     {
         return membershipName;
     }
 
     /**
     * Sets the name of a membership type
      * @param membershipName the membershipName to set
      */
     public void setMembershipName(String membershipName)
     {
         this.membershipName = membershipName;
     }
 
     /**
     * Returns the price of a membership type
      * @return the price
      */
     public int getPrice()
     {
         return price;
     }
 
     /**
     * Sets the price of a membership type
      * @param price the price to set
      */
     public void setPrice(int price)
     {
         this.price = price;
     }
 }
