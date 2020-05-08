 import java.io.*;
 
 public class BlindPassenger
 {
   public static void main(String [] args) throws IOException
   {
     BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
     String line = br.readLine();
     int t,n;
     //System.out.println(line);
    t = Integer.parseInt(line.trim());
     for(int i=0;i<t;++i)
     {
       line = br.readLine();
      n = Integer.parseInt(line.trim()); --n;
       if(n == 0)
       {
         System.out.println("poor conductor");
       }
       else
       {
         char direction='l',seat_posn='l';
         int row_no = 0, relative_seat_no = 0;
         row_no = (int) Math.ceil(n/5.0);
         relative_seat_no = n % 5;
         if(row_no % 2 == 0)
         {
           //even row, need to reverse the relative seat no
           relative_seat_no = 6 - relative_seat_no;
         }
 
         if(relative_seat_no < 3)
         {
           direction = 'L';
           if(relative_seat_no == 1) seat_posn = 'W';
           else seat_posn = 'A';
         }
         else
         {
           direction = 'R';
           if(relative_seat_no == 3) seat_posn = 'A';
           else if(relative_seat_no == 4) seat_posn = 'M';
           else seat_posn = 'W';
         }
 
         System.out.println(row_no + " " + seat_posn + " " + direction);
       }
     }
   }
 }
