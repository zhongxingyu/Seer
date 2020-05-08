 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 /**
 * This is kind of a wrapper class for XmlRpcFault and XmlRpcException
  * 
  * @author jrusnack
  */
 public class TcmsException extends Exception {
 
     public TcmsException(String message) {        
         super(message);
     }
 }
