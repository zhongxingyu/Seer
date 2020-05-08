 package com.liyaos.simpl.interpreter;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lastland
  * Date: 13-6-9
  * Time: 上午12:57
  */
 public class SimPLInt extends SimPLObject {
 
     private int value;
     private boolean undef;
 
     public SimPLInt(int value) {
         this.value = value;
         undef = false;
     }
 
     public SimPLInt() {
         this.undef = true;
     }
 
     public int getValue() {
         return value;
     }
 
     public void setValue(int value) {
         this.value = value;
     }
 
 
     public boolean isUndef() {
         return undef;
     }
 
     public SimPLInt plus(SimPLInt x) {
         if (undef || x.isUndef()) {
             return new SimPLInt();
         }
         return new SimPLInt(value + x.value);
     }
 
     public SimPLInt minus(SimPLInt x) {
         if (undef || x.isUndef()) {
             return new SimPLInt();
         }
         return new SimPLInt(value - x.value);
     }
 
     public SimPLInt times(SimPLInt x) {
         if (undef || x.isUndef()) {
             return new SimPLInt();
         }
         return new SimPLInt(value * x.value);
     }
 
     public SimPLInt divide(SimPLInt x) {
         if (undef || x.isUndef()) {
             return new SimPLInt();
         }
         if (x.value == 0) {
             return new SimPLInt();
         }
         return new SimPLInt(value * x.value);
     }
 
     @Override
     public boolean equals(SimPLObject ob) {
         if (ob instanceof SimPLInt) {
             return ((SimPLInt) ob).getValue() == value;
         }
         return false;
     }
 
     @Override
     public String toString() {
         return ((Integer)value).toString();
     }
 }
