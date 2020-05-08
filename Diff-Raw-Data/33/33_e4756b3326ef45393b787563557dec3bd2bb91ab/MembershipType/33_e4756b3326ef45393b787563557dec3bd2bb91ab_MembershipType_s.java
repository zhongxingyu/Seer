/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package BE;
 
/**
 *
 * @author ZavezX
 */
 public class MembershipType
 {
 
     private int id;
     private String membershipName;
     private int price;
 
     public MembershipType(int id, String membershipName, int price)
     {
         this.id = id;
         this.membershipName = membershipName;
         this.price = price;
     }

     public MembershipType(String membershipName, int price)
     {
         this(-1, membershipName, price);
     }
 
     public MembershipType(int id, MembershipType mt)
     {
         this(mt.membershipName, mt.price);
     }
 
     /**
      * @return the id
      */
     public int getId()
     {
         return id;
     }
 
     /**
      * @return the membershipName
      */
     public String getMembershipName()
     {
         return membershipName;
     }
 
     /**
      * @param membershipName the membershipName to set
      */
     public void setMembershipName(String membershipName)
     {
         this.membershipName = membershipName;
     }
 
     /**
      * @return the price
      */
     public int getPrice()
     {
         return price;
     }
 
     /**
      * @param price the price to set
      */
     public void setPrice(int price)
     {
         this.price = price;
     }
 }
