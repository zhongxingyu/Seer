 package euler;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class Solver008 implements Solver
 {
   public String solve()
   {
     String[] data = readData();
     int max = 0;
     for(int i=0; i<data.length; i++){
       int v = getMaxProduct(data[i]);
       if(v > max) max = v;
     }
     return Integer.toString(max);
   }
   
   public String[] readData()
   {
     StringBuffer buf = new StringBuffer();
     BufferedReader br = null;
     try{
       br = new BufferedReader(new FileReader("src/euler/data008.txt"));
       String line;
       while((line=br.readLine())!=null){
         buf.append(line.trim());
       }
     }catch(IOException e){
       e.printStackTrace();
       if(br!=null){
         try{
           br.close();
         }catch(IOException e2){}
       }
     }
 
     String[] datas = buf.toString().split("0");
     ArrayList<String> list = new ArrayList<String>();
     for(int i=0; i<datas.length; i++){
       if(datas[i].length()>=5){
         list.add(datas[i]);
       }
     }
     return list.toArray(new String[list.size()]);
   }
   
   public int getMaxProduct(String data)
   {
     int max = 0;
     int[] d = new int[data.length()];
     for(int i=0; i<data.length(); i++){
       d[i] = Integer.parseInt(data.substring(i, i+1));
     }
     
     for(int i=0; i<data.length()-4; i++){
       int p = getProduct(d, i);
       if(p > max) max = p;
     }
     return max;
   }
   
   public int getProduct(int[] d, int pos)
   {
     int v = 1;
     for(int i=0; i<5; i++){
       v *= d[pos+i];
     }
     return v;
   }
 
 }
