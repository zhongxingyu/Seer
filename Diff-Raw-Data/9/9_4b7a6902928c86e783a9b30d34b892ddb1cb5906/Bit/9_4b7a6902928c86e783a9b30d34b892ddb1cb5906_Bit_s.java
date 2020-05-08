 package com.mycompany.app;
 
 /**
  * Created by IntelliJ IDEA.
  * User: pesekb
 * Date: aaaa12.10.11gfgfgfgfgfdgcvbvbdfdfsdfsdfsd
  * Time: 15:41dsfsdfsdfs
  * To change this template use File | Settings | File Templates. ddddddd
  */
 public class Bit {
 
     public static Bit newZero() {
                 return new Bit(false);
     }
 
     public static Bit newOne() {
                 return new Bit(true);
     }
 
     private boolean b;
 
     public Bit(boolean bit) {
         b = bit;
     }
 
     public Bit(int bit) {
         b = (bit==0)?false:true;
     }
 
     public boolean value() {
         return b;
     }
 
     public int valueAsInt() {
         return b?1:0;
     }
 
     public char valueAsChar() {
         return b?'1':'0';
     }
 
     public boolean negate() {
 	b = (!b);
 	return b;
     }
 
     public boolean xor(Bit aBit) {
  	b = (b==(aBit.value()))?false:true;
 	return b;
     }
 
 	
 
     public boolean and(Bit aBit) {
 	b = (b&&(aBit.value()));
 	return b;
     }
 
     public boolean or(Bit aBit) {
 	b = (b||(aBit.value()));
 	if (true) {
 		int x = 1;
 	}
 	return b;
     }	
 
 
    /////
 ///////
 }
