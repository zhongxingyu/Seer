 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lut;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 /**
  *
  * @author renee
  */
 public class LookUpTable {
     
     static float lookUpTableSin[] = new float[360] ;
     static float lookUpTableCos[] = new float[360];
     
     public static void main(String[] args) {
         fillSin();
         fillCos();
         WriteSin();
         WriteCos();
     }
     
     static void fillSin(){
         for(int x = 0; x < 360; x++){
            float y = (float)(2 * Math.PI)/360;
            float z = y * x;
            lookUpTableSin[x] = (float) Math.sin(z);
         }
     }
     
     static void fillCos(){
         for(int x = 0; x < 360; x++){
            float y = (float)(2 * Math.PI)/360;
            float z = y * x;
            lookUpTableCos[x] = (float) Math.cos(z);
         }
     }
     
     static void printLookUpSin(){
         System.out.print("(");
         for(int x = 0; x < 360; x++){
             System.out.print(lookUpTableSin[x] + ", ");
         }
         System.out.println(")");
     }
     
     public static void WriteSin(){  
        try{  
            FileWriter fr = new FileWriter("LookUpSin.txt");  
            BufferedWriter br = new BufferedWriter(fr);  
            PrintWriter out = new PrintWriter(br);  
            for(int i=0; i<360; i++){ 
                out.flush();
                float inhoud = lookUpTableSin[i];
                String schrijven = inhoud +", ";
                 out.write(schrijven);  
                 //out.write("\n"); 
                //out.write("lol");
            }   
        }  
        catch(IOException e){  
         System.out.println(e);     
        }  
    }  
     
     public static void WriteCos(){  
        try{  
            FileWriter fr = new FileWriter("LookUpCos.txt");  
            BufferedWriter br = new BufferedWriter(fr);  
            PrintWriter out = new PrintWriter(br);  
            for(int i=0; i<360; i++){ 
                out.flush();
                float inhoud = lookUpTableCos[i];
                String schrijven = inhoud +", ";
                 out.write(schrijven);  
                 //out.write("\n"); 
                //out.write("lol");
            }   
        }  
        catch(IOException e){  
         System.out.println(e);     
        }  
    }  
 }
