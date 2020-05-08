 package se.liu.tdp024.logic.bean;
 
 import com.google.gson.*;
 import java.util.*;
 import se.liu.tdp024.entity.Account;
 import se.liu.tdp024.facade.AccountFacade;
 import se.liu.tdp024.logic.util.HTTPHelper;
 
 public abstract class AccountBean {
     private static String PersonAPI_URL = "http://enterprise-systems.appspot.com/person/";
     private static String BankAPI_URL =   "http://enterprise-systems.appspot.com/bank/";
 
     private static boolean personExists(String personKey) {
        String resp = HTTPHelper.get(PersonAPI_URL + "find.key", "key", personKey);
         JsonParser jp = new JsonParser();
         return jp.parse(resp).isJsonObject();
     }
 
     private static boolean bankExists(String bankKey) {
        String resp = HTTPHelper.get(BankAPI_URL + "find.key", "key", bankKey);
         JsonParser jp = new JsonParser();
         return jp.parse(resp).isJsonObject();
     }
 
     public static long create(int accountType,
                               String personKey,
                               String bankKey) {
         if (!personExists(personKey)) {
             //log
             return 0;
         }
         if (!bankExists(bankKey)) {
             //log
             return 0;
         }
         return AccountFacade.create(accountType, personKey, bankKey);
     }
 
     public static Account getAccount(long accountNumber) {
         return AccountFacade.find(accountNumber);
     }
 
     public static List<Account> findByPersonKey(String personKey) {
         return AccountFacade.findByPersonKey(personKey);
     }
 
     public static List<Account> findByBankKey(String bankKey) {
         return AccountFacade.findByBankKey(bankKey);
     }
 
     public static boolean deposit(long accountNumber, long amount) {
         return AccountFacade.deposit(accountNumber, amount);
     }
 
     public static boolean withdraw(long accountNumber, long amount) {
         return AccountFacade.withdraw(accountNumber, amount);
     }
 }
