 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Models;
 
 import java.util.Date;
 import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;
 
 /**
  *
  * @author Shehan Tis
  */
 public class UniqueKeyGenerator {

    public long generateNewKey() {
         Date date;
         date = new Date();
         SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddhhmmssSSS");
         String formattedDate = sdf.format(date);
         return Long.parseLong(formattedDate);
     }

    public String generateNewKey2() {
        UUID GenrateUUID = UUID.fromString("38400000");
        GenrateUUID = UUID.randomUUID();
        return GenrateUUID.toString();
    }

    public static long generateNewKey3() {

        long randomInt = 0;
        Random randomGenerator = new Random();
        for (int idx = 1; idx <= 10; ++idx) {
            randomInt = randomGenerator.nextInt(100000000);
        }
        return randomInt;
    }
 }
