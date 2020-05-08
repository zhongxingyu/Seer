 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package util;
 
 /**
  * @author matthew.sundberg
  *
  */
 public class Output {
     int[] toPrint;
     public void Output(int[] toPrintInput){
         toPrint = toPrintInput;
     }
     public boolean checkToPrint(int idNum){
         for(int count=0; count<=toPrint.length; count++){
             if(idNum==toPrint[count]){
                 return true;
             }
         }/*End of the loop to test if is included in output array.*/
         return false;
     }
     public void Print(int id, String name, int value){/*Print statement for int*/
         if(checkToPrint(id)){
             if(name!=""&&name!=null){
                 System.out.print(name+": "+value);
             }
             else {
                 System.out.print(value);
             }
             
         }
     }
     public void Print(int id, String name, boolean value){/*Print statement for Boolean*/
         if(checkToPrint(id)){
             if(name!=""&&name!=null){
                 System.out.print(name+" is "+value);
             }
             else {
                 System.out.print(value);
             }
             
         }
     }
    public void Print(int id, String name, double value){/*Print statement for Double*/
         if(checkToPrint(id)){
             if(name!=""&&name!=null){
                 System.out.print(name+": "+value);
             }
             else {
                 System.out.print(value);
             }
             
         }
    }
 }
