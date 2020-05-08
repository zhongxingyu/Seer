 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX Function representation
  * @author steele@osteele.com
  */
 
 package org.openlaszlo.sc;
 
 public class Function {
   public String name;
   private final String args;
   private final String body;
   private final String sourceLocation;
 
   public Function(String body) {
     this("", body);
   }
 
   public Function(String args, String body) {
     this("", args, body);
   }
 
   public Function(String name, String args, String body) {
     this(name, args, body, null);
   }
 
   public Function(String name, String args, String body, String loc) {
     this.name = name;
     this.args = args;
     this.body = body;
     this.sourceLocation = loc;
   }
 
   public void setName(String name) {
     this.name = name;
   }
 
   public String toString() {
    return (sourceLocation != null?(sourceLocation + "\n"):"") +
      "function " + name + "\n(" + args + "\n) {" + body + "\n}";
   }
 }
 
 /**
  * @copyright Copyright 2001-2008 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
