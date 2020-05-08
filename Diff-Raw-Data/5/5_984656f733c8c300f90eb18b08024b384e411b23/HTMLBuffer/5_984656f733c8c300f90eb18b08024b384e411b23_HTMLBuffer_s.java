 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package se362project1;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import javax.swing.JEditorPane;
 
 /**
  *
  * @author Randy
  */
 public class HTMLBuffer implements KeyListener{
     String buffer;
     String fileName;
     JEditorPane pane;
     
     public HTMLBuffer(JEditorPane p){
         buffer = "";
         fileName = "";
         pane = p;
     }
     
     public HTMLBuffer(JEditorPane p,String name){
         buffer = "";
         fileName = name;
         pane = p;
     }
     
     public HTMLBuffer(String name,String text){
         buffer = text;
         fileName = name;
     }
     
     public void setFileName(String name){
         fileName = name;
     }
     
     public String getFullPath(){
         File pathFile = new File(fileName);
         return pathFile.getAbsolutePath();
     }
     
     public String getFileName(){
         if(fileName.contains("\\")){
            return fileName.substring(fileName.lastIndexOf("\\"));
         }else if(fileName.contains("/")){
            return fileName.substring(fileName.lastIndexOf("/"));
         }else{
             return fileName;
         }
     }
     
     public boolean save(){
         try {
             File saveFile = new File(fileName);
             //System.out.println(saveFile.getAbsolutePath());
             if(!saveFile.exists()){
                 saveFile.createNewFile();
             }
             System.out.println(saveFile.getAbsolutePath());
             BufferedWriter outFile = new BufferedWriter(new FileWriter(saveFile.getAbsoluteFile()));
             outFile.write(buffer);
             outFile.close();
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
 
     @Override
     public void keyTyped(KeyEvent e) {
         //Save a key into the buffer
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         buffer = pane.getText();
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
