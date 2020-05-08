 package de.autoit4you.bankaccount.api;
 
 import de.autoit4you.bankaccount.BankAccount;
 import de.autoit4you.bankaccount.Database;
 import de.autoit4you.bankaccount.Permissions;
 import de.autoit4you.bankaccount.exceptions.BankAccountException;
 import org.junit.Test;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import static org.mockito.Mockito.*;
 
 public class APIFactory {
 
     public API getDumpedInstance(BankAccount plugin) {
         API api = new API(plugin, true);
         try {
             Field f = API.class.getDeclaredField("accounts");
             HashSet<Account> set = new HashSet<Account>();
            set.add(new Account(api, "test"));
             f.setAccessible(true);
             f.set(api, set);
         } catch (Exception e) {
         }
 
         return api;
     }
 }
