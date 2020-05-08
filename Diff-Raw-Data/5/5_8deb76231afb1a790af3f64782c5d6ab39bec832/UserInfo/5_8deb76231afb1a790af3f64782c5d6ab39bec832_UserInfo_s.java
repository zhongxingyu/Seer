 // Persistence tier
 
 package edu.uml.project90308.persistence;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.uml.project90308.businesslogic.*;
 
 /**
  * @author Alan Derrick
  *
  * Modifications made by Peter G. Martin
  *
  * This class represents a Userinfo.  The kind of Userinfo it represents is an investor for our stock
  * program.  The Userinfo has a userName, an password, and it tracks certain stock symbols.   This Userinfo
  * will check to make sure the userName is valid (contains only letters and spaces).  This Userinfo will also
  * check to make sure the password passed to it is not a blank line.
  *
  */
 public class UserInfo {
     private String userName;
     private String password;
     private List<Stock> stocks;
 
     public UserInfo() {
         //
     }
     /**
      *
      * This constructor will accept various parameters and fill the new Userinfo object
      *
      * @param name Name of the user
      * @param password password of the user
      * @param stocks A List of stock items the user is tracking
      */
     public UserInfo(String name, String password, List<Stock> stocks) {
         this.userName = name;
         this.password = password;
         this.stocks = stocks;
     } // end constructor
 
     /**
      * This constructor will auto-populate a new Userinfo object.  So if no arguments are given, then this
      * constructor will create the Userinfo object with the "default" values below
      */
 
     public UserInfo(String uname, String passwd) {
         this.userName = uname;
         this.password = passwd;
     }
 
     /**
      *
      * @return  returns userName
      */
     public String getUserName(){
         return userName;
     }
 
     /**
      *
      * @return password
      */
     public String getPassword(){
         return password;
     }
 
     /**
      *
      * @return stock symbols from the favorites list
      */
    public List<Stock> getStocks(){
         return stocks;
     }
 
     /**
      * verifies the userName only contains letters and white space, then sets the userName
      *
      * @param userName userName of Person object
      *
      */
     public void setUserName(String userName) {
         this.userName = userName;
     } // end setUserName
 
 
     public void setPassword(String passwd) {
         this.password = passwd;
     }// end setPassword
 
     /**
      * sets the Person object's ArrayList to the passed ArrayList
      *
      * @param stocks List stockSymbols
      */
    public void setStocks(List stocks) {
         this.stocks = stocks;
     }
 
     /**
      * Add a stock symbol to the favorites list
      *
      * @param stock Stock object to add
      *
      */
     public void addStock(Stock stock) {
         stocks.add(stock);
     }
 
     /**
      * Remove a stock symbol to the favorites list
      *
      * @param stock Stock object to remove
      *
      * return status of removal
      */
     public boolean removeStock(Stock stock) {
         return stocks.remove(stock);
     }
 
 } // end UserInfo
