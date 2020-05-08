 package fastboard.lineconverter;
 
 /**
  * Created by IntelliJ IDEA.
  * User: knhjp
  * Date: 10-Nov-2009
  * Time: 4:51:39 PM
  * This converts a line into character string, and the other way around
  */
 public class LineConverter {
     public static String convertLineToString(int line) {
         char[] charArr = new char[8];
        for (int i=0 ; i<charArr.length ; i++) {
             int value = line % 3;
             switch (value) {
                 case 0:
                     charArr[i] = '_';
                     break;
                 case 1:
                     charArr[i] = 'x';
                     break;
                 case 2:
                     charArr[i] = 'o';
             }
//            line /=3;
         }
         return new String(charArr);
     }
 }
