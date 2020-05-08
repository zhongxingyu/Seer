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
    public void Print(int id, String name, double value, int roundPoint){/*Print statement for Double, optional round point*/
        if(checkToPrint(id)){
            if(roundPoint!=0&&roundPoint!=null){
                double valueRounded = ;
                if(name!=""&&name!=null){
                    System.out.print(name+": "+valueRounded);
                }
                else {
                    System.out.print(valueRounded);
                }
            }
        }
    }
    public void Print(int id, String name, double value){/*Print statement for Double sans roundpoint*/
         if(checkToPrint(id)){
             if(name!=""&&name!=null){
                 System.out.print(name+": "+value);
             }
             else {
                 System.out.print(value);
             }
             
         }
    }/*Ythak rage comment:
             HELLO SILLY HUMANS
             I HAVE COME TO EAT EVERYTHING YOU LOVE
             INCLUDING YOU
             AND YOUR MOM'
             AND YOUR DAD
             AND YOUR MOM'S MOM
             AND YOUR DAD'S DAD
             WEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE
             OKEY
             hello
             i am a destroyer
             named may
             hehehehehehhehehehehehhehehehehehehehe
             weeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
             okay bye :)*/
 }
