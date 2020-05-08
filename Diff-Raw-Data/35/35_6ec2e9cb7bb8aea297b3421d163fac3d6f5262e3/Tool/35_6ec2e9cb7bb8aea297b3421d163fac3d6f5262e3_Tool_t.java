 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package memory;
 import java.io.Serializable;
 /**
  *
  * @author kbronson
  */
 public class Tool implements Serializable{
         //arrowTool=;
     String arrowTool = "arrow";
     public Tool() {
     }
     
     public String getArrowTool () {
             return arrowTool;
     }
    public void setArrowTool (String arrowTool){
         this.arrowTool = arrowTool;
     }
    
    /**
     *
     */
    public void displayTool(){
         System.out.println("Tool arrow:" + this.arrowTool);
 }
}
