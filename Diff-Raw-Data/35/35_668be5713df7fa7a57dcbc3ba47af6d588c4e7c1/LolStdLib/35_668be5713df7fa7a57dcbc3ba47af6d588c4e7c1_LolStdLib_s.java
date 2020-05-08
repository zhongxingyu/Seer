 package com.lolcode.runtime;
 
import java.util.Scanner;
 
 public class LolStdLib {
    private Scanner scanner = new Scanner(System.in);
 
     protected void putStr(String str) {
         System.out.println(str);
     }
 
     protected String readStr() {
        return scanner.nextLine();
     }
 
     public void print(LolObject lolObject) {
         putStr(lolObject.toLolString().strVal);
     }
 
     public LolObject read() {
         String str = readStr();
         if (str.equals("WIN")) {
             return new LolBool(true);
         } else if (str.equals("FAIL")) {
             return new LolBool(false);
         }
         try {
             return new LolInt(Integer.parseInt(str));
         } catch (Exception e) {
             try {
                 return new LolDouble(Double.parseDouble(str));
             } catch (Exception ex) {
                 return new LolString(str);
             }
         }
     }
 }
