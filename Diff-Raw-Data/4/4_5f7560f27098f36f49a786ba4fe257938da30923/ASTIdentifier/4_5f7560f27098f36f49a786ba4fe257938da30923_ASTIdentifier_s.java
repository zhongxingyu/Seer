 /* ****************************************************************************
  * ASTIdentifier.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2007 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.sc.parser;
 
 public class ASTIdentifier extends SimpleNode {
     private String name = null;
     private Type type = null;
     private int hash = 0;
 
     public static class Type {
         public String typeName = null;
         public boolean nullable = false; // has "?"
         public boolean notnullable = false; // has "!"
     }
 
     public ASTIdentifier(int id) {
         super(id);
     }
 
     public ASTIdentifier(Parser p, int id) {
         super(p, id);
     }
 
     public static Node jjtCreate(int id) {
         return new ASTIdentifier(id);
     }
 
     public static Node jjtCreate(Parser p, int id) {
         return new ASTIdentifier(p, id);
     }
 
     // Added
     public ASTIdentifier(String s) {
         setName(s);
     }
 
     public void setName(String name) {
         this.name = name.intern(); // to lower number of strings
         this.hash = name.hashCode();
     }
   
     public int hashCode() {
         return hash;
     }
   
     public String getName() {
         return name;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public String toString() {
         String typesuffix = "";
         if (type != null) {
             typesuffix = ": ";
             if (type.nullable)
                 typesuffix += "?";
             typesuffix += type.typeName;
             if (type.notnullable)
                 typesuffix += "!";
         }
         return "ASTIdentifier(" + name + typesuffix + ")";
     }
 }
