 package euler;
 
 
 public class Solver019 implements Solver
 {
   private static final int[] D_OF_M   = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
   private static final int[] D_OF_M_L = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
 
   public String solve()
   {
     int cnt = 0;
     int y, m;
     for(y=1901; y<=2000; y++){
       for(m=1; m<=12; m++){
         if(getDays(y, m, 1) % 7 == 0) cnt++;
       }
     }
     return Integer.toString(cnt);
   }
 
   public int getDays(int y, int m, int d)
   {
     int days = 0;
     int i;
     for(i=1900; i<y; i++){
       if(isLeap(i)){
         days += 366;
       }else{
         days += 365;
       }
     }
     for(i=1; i<m; i++){
       if(isLeap(y)){
         days += D_OF_M_L[i - 1];
       }else{
         days += D_OF_M[i - 1];
       }
     }
    days += d - 1;
     return days;
   }
   
   public boolean isLeap(int y)
   {
     if(y % 4 == 0){
       if(y % 400 == 0) return false;
       else return true;
     }else{
       return false;
     }
   }
 
 }
