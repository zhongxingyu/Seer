 package euler;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 public class Solver011 implements Solver
 {
   public String solve()
   {
     int row = 20;
     int col = 20;
     int num = 4;
     int max = 0;
     int i, j;
     
     // read data
     int[][] data = readData(row, col);
 
     // right
     for(i=0; i<row; i++){
       for(j=0; j<=col-num; j++){
         int v = getProductRight(data, i, j, num);
         if(v > max) max = v;
       }
     }
     
     // down
     for(i=0; i<=row-num; i++){
       for(j=0; j<col; j++){
         int v = getProductDown(data, i, j, num);
         if(v > max) max = v;
       }
     }
     
     // diag(right down)
     for(i=0; i<=row-num; i++){
       for(j=0; j<=col-num; j++){
         int v = getProductDiagRD(data, i, j, num);
         if(v > max) max = v;
       }
     }
     
     // diag(right up)
     for(i=num-1; i<row; i++){
       for(j=0; j<=col-num; j++){
         int v = getProductDiagRU(data, i, j, num);
         if(v > max) max = v;
       }
     }
     
     return Integer.toString(max);
   }
   
   public int[][] readData(int row, int col)
   {
     int[][] data = new int[row][col];
     int i, j;
     for(i=0; i<row; i++){
       for(j=0; j<col; j++){
         data[i][j] = 0;
       }
     }
 
     BufferedReader br = null;
     try{
       br = new BufferedReader(new FileReader("src/euler/data011.txt"));
       String line;
       i = 0;
       while((line=br.readLine())!=null){
         String[] arr = line.trim().split(" ");
         for(j=0; j<arr.length; j++){
           data[i][j] = Integer.parseInt(arr[j]);
         }
         i++;
       }
     }catch(IOException e){
       e.printStackTrace();
       if(br!=null){
         try{
           br.close();
         }catch(IOException e2){}
       }
     }
 
     return data;
   }
   
   public int getProductRight(int[][] data, int i, int j, int n)
   {
     int v = 1;
     for(int m=0; m<n; m++){
       v *= data[i][j+m];
     }
     return v;
   }
   
   public int getProductDown(int[][] data, int i, int j, int n)
   {
     int v = 1;
     for(int m=0; m<n; m++){
       v *= data[i+m][j];
     }
     return v;
   }
 
   public int getProductDiagRD(int[][] data, int i, int j, int n)
   {
     int v = 1;
     for(int m=0; m<n; m++){
       v *= data[i+m][j+m];
     }
     return v;
   }
 
   public int getProductDiagRU(int[][] data, int i, int j, int n)
   {
     int v = 1;
     for(int m=0; m<n; m++){
       v *= data[i-m][j+m];
     }
     return v;
   }
 
 }
