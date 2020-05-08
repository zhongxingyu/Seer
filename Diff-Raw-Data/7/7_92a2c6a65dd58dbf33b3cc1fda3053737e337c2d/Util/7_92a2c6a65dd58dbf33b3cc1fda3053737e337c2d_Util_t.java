 package com.infomancers.collections.yield.asm;
 
 import com.infomancers.collections.yield.Yielder;
 import org.objectweb.asm.Opcodes;
 
 /**
  * Copyright (c) 2007, Aviad Ben Dov
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list
  * of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice, this
  * list of conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 3. Neither the name of Infomancers, Ltd. nor the names of its contributors may be
  * used to endorse or promote products derived from this software without specific
  * prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 /**
  * Utility class for doing redundant checks on fields, variables and methods while
  * visiting different classes.
  */
 final class Util {
     public enum TypeDescriptor {
         Integer("I", "int", "Integer"),
         Long("L", "long", "Long"),
         Float("F", "float", "Float"),
         Double("D", "double", "Double"),
        Object("Ljava/lang/Object;", null, "Object");
 
         private final String desc;
         private final String primitive;
         private final String wrapper;
 
         public String getDesc() {
             return desc;
         }
 
         public String getClassName() {
             return wrapper;
         }
 
         public String getClassNameAsOwner() {
             return "java/lang/" + wrapper;
         }
 
         public String getClassNameAsDesc() {
             return "L" + getClassNameAsOwner() + ";";
         }
 
         public String getPrimitiveName() {
             return primitive;
         }
 
         public String getPrimitiveNameCapitalised() {
            if (getPrimitiveName() == null) {
                return "";
            }

             char[] c = getPrimitiveName().toCharArray();
             c[0] = Character.toUpperCase(c[0]);
             return new String(c);
         }
 
         public String getBoxMethodDesc() {
             return "(" + getDesc() + ")" + getClassNameAsDesc();
         }
 
         public String getBoxMethodName() {
             return "valueOf";
         }
 
         public String getUnboxMethodDesc() {
             return "()" + getDesc();
         }
 
         public String getUnboxMethodName() {
             return getPrimitiveName() + "Value";
         }
 
         public String getArraySetterMethodDesc() {
             return "(Ljava/lang/Object;I" + getDesc() + ")V";
         }
 
         public String getArraySetterMethodName() {
             return "set" + getPrimitiveNameCapitalised();
         }
 
         public String getArrayGetterMethodDesc() {
             return "(Ljava/lang/Object;I)" + getDesc();
         }
 
         public String getArrayGetterMethodName() {
             return "get" + getPrimitiveNameCapitalised();
         }
 
         TypeDescriptor(String desc, String primitive, String wrapper) {
             this.desc = desc;
             this.primitive = primitive;
             this.wrapper = wrapper;
         }
     }
 
 //    private final static String[] descs = {"I", "L", "F", "D", "Ljava/lang/Object;"};
 //    private final static String[] wrappers = {"Ljava/lang/Integer;", "Ljava/lang/Long;",
 //            "Ljava/lang/Float;", "Ljava/lang/Double;"
 //    };
 //    private final static String[] unboxMethods = {"intValue", "longValue", "floatValue", "doubleValue"};
 
     public static boolean isYieldNextCoreMethod(String name, String desc) {
         return "yieldNextCore".equals(name) && "()V".equals(desc);
     }
 
     static boolean isInvokeYieldReturn(int opcode, String name, String desc) {
         return opcode == Opcodes.INVOKEVIRTUAL && "yieldReturn".equals(name) && "(Ljava/lang/Object;)V".equals(desc);
     }
 
     public static boolean isInvokeYieldBreak(int opcode, String name, String desc) {
         return opcode == Opcodes.INVOKEVIRTUAL && "yieldBreak".equals(name) && "()V".equals(desc);
     }
 
     public static boolean isYielderClassName(String name) {
         return "com/infomancers/collections/yield/Yielder".equals(name);
     }
 
     public static int offsetForDesc(String desc) {
         for (int i = 0; i < TypeDescriptor.values().length; i++) {
             String cur = TypeDescriptor.values()[i].desc;
             if (cur.equals(desc)) {
                 return i;
             }
         }
 
         return -1;
     }
 
     public static TypeDescriptor typeForOffset(int offset) {
         return TypeDescriptor.values()[offset];
     }
 
     public static boolean isYielderInHierarchyTree(String className) {
         try {
             return Class.forName(className).isAssignableFrom(Yielder.class);
         } catch (ClassNotFoundException e) {
             return false;
         }
     }
 }
