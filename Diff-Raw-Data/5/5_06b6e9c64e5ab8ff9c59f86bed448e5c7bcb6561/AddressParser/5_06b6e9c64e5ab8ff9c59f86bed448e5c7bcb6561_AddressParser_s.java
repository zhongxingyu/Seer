 package com.alanjeon.dutypharm.util;
 
 /**
  * Created by skyisle on 12/10/13.
  */
 public class AddressParser {
     public String[] parse(String addressLine) {
         String splitAddress[] = addressLine.split(" ");
         if (splitAddress.length > 4) {
            return new String[] { splitAddress[0], splitAddress[1], splitAddress[2] };
         } else if (splitAddress.length > 3) {
            return new String[] { splitAddress[0], splitAddress[1], "" };
         } else {
             return null;
         }
     }
 }
