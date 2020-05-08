 package com.marbl.administration.backend.utils;
 
 //<editor-fold defaultstate="collapsed" desc="Imports">
 import com.marbl.administration.domain.Driver;
 import com.marbl.administration.domain.DriverGroup;
 import com.marbl.administration.domain.utils.Hasher;
import java.util.ArrayList;
 import java.util.Date;
 //</editor-fold>
 
 public final class DriverGenerator {
 
     //<editor-fold defaultstate="collapsed" desc="Fields">
     private int index;
     private final int startBSN = 900000000;
     private String emailProvider;
     private Hasher hasher;
     private String[] firstNames;
     private String[] lastNames;
     //</editor-fold>
 
     public DriverGenerator(String emailProvider, Hasher hasher, String[] firstNames, String[] lastNames) {
         this.emailProvider = emailProvider;
         this.hasher = hasher;
         this.firstNames = firstNames;
         this.lastNames = lastNames;
         
         reset();
     }
     
     public void reset() {
         index = -1;
     }
 
     public Driver[] generate() {
         return generate(firstNames.length);
     }
 
     public Driver[] generate(int length) {
         Driver[] drivers = new Driver[length];
 
         for (int i = 0; i < length; i++) {
             drivers[i] = next();
         }
 
         return drivers;
     }
 
     public Driver next() {
         index++;
 
         Integer bsn = startBSN + index;
         String firstName = firstNames[index % firstNames.length];
         String lastName = lastNames[index % lastNames.length];
         String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@" + emailProvider;
         String password = hasher.hash(firstName.toLowerCase() + "123");
         String residence = "Eindhoven";
         String address = "Hoofdstraat 1";
         String zipCode = "1234AB";
         Date dateOfBirth = new Date();
        ArrayList<DriverGroup> groups = new ArrayList<>();
        groups.add(DriverGroup.DRIVER);
         Boolean activated = true;
 
         return new Driver(bsn, firstName, lastName, email, password,
                 residence, address, zipCode, dateOfBirth, groups, activated);
     }
 }
