 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.redhat.nitrate;
 
 /**
 * This is kind of a wrapper class for XmlRpcFault, XmlRpcException, IOException 
 * and generally any exception that is caught and thrown again, but with different
 * message (in case we want to say something more useful than default message)
  * 
  * @author jrusnack
  */
 public class TcmsException extends Exception {
 
     public TcmsException(String message) {        
         super(message);
     }
 }
