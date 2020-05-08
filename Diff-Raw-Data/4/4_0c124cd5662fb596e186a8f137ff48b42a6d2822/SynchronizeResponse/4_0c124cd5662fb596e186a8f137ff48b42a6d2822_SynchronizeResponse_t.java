 package com.twoclams.hww.server.model;
 
 import java.io.Serializable;
 
 public class SynchronizeResponse implements Serializable {
     private static final long serialVersionUID = -4782772020061180342L;
     private Housewife wife;
     private Husband husband;
     private House house;
     private Wallet wallet;
     private Passport passport;
     private Realstate realstate;
 
     public SynchronizeResponse(Housewife wife, Husband husband, House house, Wallet wallet, Passport passport,
             Realstate realstate) {
         this.wife = wife;
         this.husband = husband;
         this.house = house;
         this.wallet = wallet;
         this.passport = passport;
         this.realstate = realstate;
        if (husband != null) {
            husband.update();
        }
     }
 
     public Housewife getWife() {
         return wife;
     }
 
     public Husband getHusband() {
         return husband;
     }
 
     public House getHouse() {
         return house;
     }
 
     public Wallet getWallet() {
         return wallet;
     }
 
     public Passport getPassport() {
         return passport;
     }
 
     public static long getSerialversionuid() {
         return serialVersionUID;
     }
 
     public Realstate getRealstate() {
         return realstate;
     }
 }
