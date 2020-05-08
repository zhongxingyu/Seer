 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
package jp.co.antenna.DfvJavaCtl;
 
 /**
  *
  * @author Test User
  */
public class DfvException extends Exception implements java.io.Serializable {
     private int errLevel;
     private int errCode;
     private String errMsg;
     
    public DfvException (int errLevel, int errCode, String errMsg) {
         this.errLevel = errLevel;
         this.errCode = errCode;
         this.errMsg = errMsg;
     }
     
     public int getErrorLevel () {
         return errLevel;
     }
     
     public int getErrorCode () {
         return errCode;
     }
     
     public String getErrorMessage () {
         return errMsg;
     }
 }
