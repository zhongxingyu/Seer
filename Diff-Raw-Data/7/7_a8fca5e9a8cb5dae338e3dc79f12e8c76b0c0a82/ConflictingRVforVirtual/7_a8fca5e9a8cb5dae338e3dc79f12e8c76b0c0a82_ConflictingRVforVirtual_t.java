 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package errorHandling;
 
 import symbolTable.types.Method;
 
 /**
  *
  * @author kostas
  */
 public class ConflictingRVforVirtual extends ErrorMessage{
    String message;
     String final_error;
     
     public ConflictingRVforVirtual(String name, Method der, Method base){
        super("error: conflicting return type specified for 'virtual " + der.toString(ErrorMessage.getFullName(der, name)) + "'");
        this.final_error = "error: overriding '" + base.toString(ErrorMessage.getFullName(base, name)) +"'";
     }
     
     public String getMessage(int der_line, int der_pos){
         return "line " + der_line + ":" + der_pos + " " + this.getMessage();
     }
     
     public String getFinalError(){
         return this.final_error;
     }
     
     public void setLineAndPos(int base_line, int base_pos){
         this.final_error = "line " + base_line + ":" + base_pos + " " + this.final_error;
     }
 
 }
