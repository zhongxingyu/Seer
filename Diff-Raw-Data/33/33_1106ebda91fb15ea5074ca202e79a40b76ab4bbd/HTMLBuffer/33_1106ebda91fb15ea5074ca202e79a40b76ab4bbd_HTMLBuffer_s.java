 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se362project1;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 /**
  *
  * @author Randy
  */
 public class HTMLBuffer {
     String buffer;
     String fileName;
     
     public HTMLBuffer(){
         buffer = "";
         fileName = "";
     }
     
     public HTMLBuffer(String name){
         buffer = "";
         fileName = name;
     }
     
     public HTMLBuffer(String name,String text){
         buffer = text;
         fileName = name;
     }
     
     public void setFileName(String name){
         fileName = "";
     }
     
     public String getFileName(){
         return fileName;
     }
     
     public boolean save(){
        File saveFile = new File(fileName);
         try {
            BufferedWriter outFile = new BufferedWriter(new FileWriter(saveFile));
             outFile.write(buffer);
         } catch (IOException ex) {
             return false;
         }
         return true;
     }
     
     public void update(String text){
         buffer = text;
     }
     
     public void appendLine(String text){
         buffer += ("\n"+text);
     }
     
     public String getText(){
         return buffer;
     }
 }
