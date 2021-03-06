 /*
   Jar Jar Links - A utility to repackage and embed Java libraries
   Copyright (C) 2004  Tonic Systems, Inc.
 
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; see the file COPYING.  if not, write to
   the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
   Boston, MA 02111-1307 USA
 */
 
 package com.tonicsystems.jarjar;
 
 import net.sf.cglib.core.Constants;
 import net.sf.cglib.core.Signature;
 import net.sf.cglib.transform.AbstractClassTransformer;
 import org.objectweb.asm.Attribute;
 import org.objectweb.asm.CodeAdapter;
 import org.objectweb.asm.CodeVisitor;
 import org.objectweb.asm.Label;
 import org.objectweb.asm.Type;
 import java.util.*;
 
 class DepKillTransformer
 extends AbstractClassTransformer
 {
     private String[] packageNames;
 
     public DepKillTransformer(String[] packageNames)
     {
         this.packageNames = new String[packageNames.length];
         for (int i = 0; i < packageNames.length; i++) {
             // TODO: check that package is valid
             StringBuffer sb = new StringBuffer();
             sb.append('L');
             sb.append(packageNames[i].replace('.', '/'));
             if (sb.charAt(sb.length() - 1) != '/')
                 sb.append('/');
             this.packageNames[i] = sb.toString();
         }
     }
 
     private boolean checkDesc(String desc)
     {
         for (int i = 0; i < packageNames.length; i++) {
             if (desc.startsWith(packageNames[i])) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean checkMethodDesc(String methodDesc)
     {
         for (int i = 0; i < packageNames.length; i++) {
             if (methodDesc.indexOf(packageNames[i]) >= 0) {
                 return true;
             }
         }
         return false;
     }
 
     private String fixMethodDesc(String methodDesc)
     {
         Signature sig = new Signature("foo", methodDesc);
         Type[] args = sig.getArgumentTypes();
         for (int i = 0; i < args.length; i++)
             args[i] = eraseType(args[i]);
         return new Signature("foo", eraseType(sig.getReturnType()), args).getDescriptor();
     }
 
     private Type eraseType(Type type)
     {
         return checkDesc(type.getDescriptor()) ? Constants.TYPE_OBJECT : type;
     }
 
     private boolean checkName(String name)
     {
         return checkDesc("L" + name + ";");
     }
 
     private static void replace(CodeVisitor cv, String desc)
     {
         switch (desc.charAt(0)) {
         case 'V':
             break;
         case 'D':
             cv.visitInsn(Constants.DCONST_0);
             break;
         case 'F':
             cv.visitInsn(Constants.FCONST_0);
             break;
         case 'J':
             cv.visitInsn(Constants.LCONST_0);
             break;
         case 'C':
         case 'S':
         case 'B':
         case 'I':
         case 'Z':
             cv.visitInsn(Constants.ICONST_0);
             break;
         case 'L':
         case '[':
             cv.visitInsn(Constants.ACONST_NULL);
             break;
         }
     }
 
     public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs)
     {
         // TODO: attrs?
         if (exceptions != null) {
             List exceptionList = new ArrayList(exceptions.length);
             for (int i = 0; i < exceptions.length; i++) {
                 if (!checkName(exceptions[i]))
                     exceptionList.add(exceptions[i]);
             }
             exceptions = (String[])exceptionList.toArray(new String[exceptionList.size()]);
         }
         return new DepKillCodeVisitor(cv.visitMethod(access, name, fixMethodDesc(desc), exceptions, attrs));
     }
 
     private class DepKillCodeVisitor
     extends CodeAdapter
     {
         public DepKillCodeVisitor(CodeVisitor cv)
         {
             super(cv);
         }
 
         public void visitTypeInsn(int opcode, String desc)
         {
             if ((desc.charAt(0) == '[') ? checkDesc(desc) : checkName(desc)) {
                 // System.err.println("visitTypeInsn " + desc);
                 switch (opcode) {
                 case Constants.NEW:
                 case Constants.ANEWARRAY:
                     cv.visitInsn(Constants.ACONST_NULL);
                     break;
                 case Constants.CHECKCAST:
                 case Constants.INSTANCEOF:
                     cv.visitInsn(Constants.ICONST_0);
                     break;
                 }
             } else {
                 cv.visitTypeInsn(opcode, desc);
             }
         }
                
         public void visitFieldInsn(int opcode, String owner, String name, String desc)
         {
             if (checkName(owner) || checkDesc(desc)) {
                 // System.err.println("visitFieldInsn " + owner + ", " + desc);
                if (opcode == Constants.GETFIELD || opcode == Constants.PUTFIELD)
                     cv.visitInsn(Constants.POP);
                replace(cv, desc);
             } else {
                 cv.visitFieldInsn(opcode, owner, name, desc);
             }
         }
 
         public void visitMethodInsn(int opcode, String owner, String name, String desc)
         {
             if (checkName(owner)) {
                 // System.err.println("visitMethodInsn " + owner + ", " + desc + " (" + name + ")");
                 switch (opcode) {
                 case Constants.INVOKEINTERFACE:
                 case Constants.INVOKEVIRTUAL:
                     cv.visitInsn(Constants.POP);
                     break;
                 case Constants.INVOKESPECIAL:
                     throw new DepKillException("Cannot remove invocation of " + owner + "." + desc);
                 case Constants.INVOKESTATIC:
                 }
 
                 Signature sig = new Signature(name, desc);
                 Type[] args = sig.getArgumentTypes();
                 for (int i = 0; i < args.length; i++)
                     cv.visitInsn((args[i].getSize() == 2) ? Constants.POP2 : Constants.POP);
                 replace(cv, sig.getReturnType().getDescriptor());
 
             } else if (checkMethodDesc(desc)) {
                 // System.err.println("visitMethodInsn " + owner + ", " + desc + " (" + name + ")");
                 desc = fixMethodDesc(desc);
 
             } else {
                 cv.visitMethodInsn(opcode, owner, name, desc);
             }
         }
 
         public void visitMultiANewArrayInsn(String desc, int dims)
         {
             if (checkDesc(desc)) {
                 // System.err.println("visitMultiANewArrayInsn " + desc);
                 cv.visitInsn(Constants.ACONST_NULL);
             } else {
                 cv.visitMultiANewArrayInsn(desc, dims);
             }
         }
 
         public void visitTryCatchBlock(Label start, Label end, Label handler, String type)
         {
             if (checkName(type)) {
                 // System.err.println("visitTryCatchBlock " + type);
             } else {
                 cv.visitTryCatchBlock(start, end, handler, type);
             }
         }
 
         public void visitLocalVariable(String name, String desc, Label start, Label end, int index)
         {
             if (checkDesc(desc)) {
                 // System.err.println("visitLocalVariable " + desc);
                 desc = Constants.TYPE_OBJECT.getDescriptor();
             }
             cv.visitLocalVariable(name, desc, start, end, index);
         }
 
         public void visitAttribute(Attribute attr)
         {
             // TODO?
             cv.visitAttribute(attr);
         }
     }
 }
