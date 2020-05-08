 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
package no.ntnu.osnap.com.testing;
 
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import no.ntnu.osnap.com.deprecated.ComLayer;
 
 /**
  *
  * @author anders
  */
 public class ComLayerTest {
     public static void main(String[] args) {
         ComLayer com = new ComLayer();
         //com.sendMsg(com.text);
         Scanner in = new Scanner(System.in);
         
         while (true){
             while (in.hasNextLine()){
                 try {
                     com.sendBytes(in.nextLine().getBytes());
                 } catch (IOException ex) {
                     System.out.println("Send error");
                 }
             }
         }
     }
 }
