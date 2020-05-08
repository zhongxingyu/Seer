 
 public class AddBinary {
     
     private int stringToInt(String in) {
         int out = 0;
         int idx = 0;
         int len = in.length();
         while (idx < len) {
             char i = in.charAt(idx);
             int v;
             if (i == '1')
                 v = 1;
             else
                 v = 0;
 
             out = 2*out + v;
         }
         return out;
     }
     
     private String intToString(int in) {
         int v = in;
         String t = "";
         while (v > 0) {
             int i = v % 2;
             char p;
             if (i == 1)
                 p = '1';
             else
                 p = '0';
             
             t = p + t;
         }
         return t;
     }
     public String addBinary(String a, String b) {
 
         String out = null;
         int va = stringToInt(a);
         int vb = stringToInt(b);
         
         out = intToString(va+vb);
         
         return out;
     }
 
 }
