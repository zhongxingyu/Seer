 package euler;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Solver018 implements Solver
 {
   public String solve()
   {
     long[] sum0 = new long[1];
     long[] sum1 = new long[1];
     int i;
 
     BufferedReader br = null;
     try{
       br = new BufferedReader(new FileReader("src/euler/data018.txt"));
       String line = br.readLine();
       sum0[0] = Integer.parseInt(line);
       while((line=br.readLine())!=null){
         String[] arr = line.split(" ");
         sum1 = new long[arr.length];
         for(i=0; i<arr.length; i++){
           sum1[i] = Integer.parseInt(arr[i]);
           if(i == 0){
             sum1[i] += sum0[i];
           }else if(i == arr.length - 1){
             sum1[i] += sum0[i - 1];
           }else{
             sum1[i] += Math.max(sum0[i - 1], sum0[i]);
           }
         }
         sum0 = sum1;
       }
     }catch(IOException e){
       e.printStackTrace();
       if(br!=null){
         try{
           br.close();
         }catch(IOException e2){}
       }
     }
     
     long max = 0;
     for(i = 0; i<sum1.length; i++){
       if(sum1[i] > max) max = sum1[i];
     }
 
     return Long.toString(max);
   }
   
 }
