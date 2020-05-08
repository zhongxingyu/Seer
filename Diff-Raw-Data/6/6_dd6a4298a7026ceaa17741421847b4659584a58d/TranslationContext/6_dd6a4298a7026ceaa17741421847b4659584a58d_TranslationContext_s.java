 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /***
  * A linked list of TranslationContexts stores information that is used
  * for break, continue, and return.  The TranslationContext is threaded
  * through calls to the statement visitors, as the context argument.
  *
  * There are two views of the context list: the abstract view, which
  * the abrupt completion code uses, and the concrete view, which is
  * what's constructed and what's threaded through the statement
  * visitors.  In the abstract view, each item in the list represents an
  * iteration context (a context for an iteration construct such as for
  * or do statement), except for the outermost context which either
  * represents a program or a function; and each context is labelled
  * with a list of labels.  In the concrete view, each context has a
  * single, optional, label, and there are contexts representing labeled
  * statements as well.  In order to produce the first view from the
  * second, the accessor methods consider a context to have as labels
  * all the labels of it labeled statement ancestors as well, up to the
  * closest ancestor that's not a labeled statement; and a context's
  * parent is considered to be the closest ancestor that's not a labeled
  * statement.
  */
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.sc;
 import java.util.*;
 import org.openlaszlo.sc.parser.*;
 
 
 public class TranslationContext extends HashMap {
   public static String FLASM = "flasm";
   public static String VARIABLES = "variables";
  public static String LOWERVARIABLES = "lowerVariables";
   public static String REGISTERS = "registers";
  public static String LOWERREGISTERS = "lowerRegisters";
 
   public Object type;                  // Class or String
   public TranslationContext parent;
   public String label;
   public boolean isEnumeration;
   public HashMap targets;
   // properties are stored in this
 
   public TranslationContext(Object type, TranslationContext parent) {
     this(type, parent, null);
   }
 
   public TranslationContext(Object type, TranslationContext parent, String label) {
     super();
     if (ASTFunctionDeclaration.class.equals(type)) {
       type = ASTFunctionExpression.class;
     }
     this.type = type;
     this.parent = parent;
     this.label = label;
     // if isEnumeration is true, this context represents a
     // "for...in", and unused values need to be popped from the
     // stack during an abrupt completion
     this.isEnumeration = false;
     this.targets = new HashMap();
   }
 
   public Object clone() {
     TranslationContext copy = (TranslationContext)super.clone();
     copy.targets = (HashMap)targets.clone();
     return copy;
   }
 
   public Object get(Object key) {
     if (containsKey(key)) {
       return super.get(key);
     } else if (parent != null) {
       return parent.get(key);
     } else {
       return null;
     }
   }
 
   // jython compatibility
   public Object getProperty(Object key) {
     return get(key);
   }
 
   // jython compatibility
   public Object setProperty(Object key, Object value) {
     return put(key, value);
   }
 
   public boolean isFunctionBoundary() {
     return ASTFunctionExpression.class.equals(type);
   }
 
   public boolean inLabelSet(String label) {
     for (TranslationContext context = this;
          context != null && ASTLabeledStatement.class.equals(context.type);
          context = context.parent) {
       if (label == context.label) {
         return true;
       }
     }
     return false;
   }
 
   public TranslationContext getParentStatement() {
     TranslationContext context;
     for (context = parent;
          context != null && ASTLabeledStatement.class.equals(context.type);
          context = context.parent) {
       ;
     }
     return context;
   }
 
   // Returns the innermost context labelled with the specified label,
   // or null.
   public TranslationContext findLabeledContext(String label) {
     if (label == null) {
       return this;
     }
     if (inLabelSet(label)) {
       return this;
     }
     TranslationContext parent = getParentStatement();
     if (parent == null || parent.isFunctionBoundary()) {
       return null;
     }
     return parent.findLabeledContext(label);
   }
 
   // Returns the next enclosing function context
   public TranslationContext findFunctionContext() {
     if (isFunctionBoundary()) {
       return this;
     }
     if (parent == null) {
       return null;
     }
     return parent.findFunctionContext();
   }
 
   public void setTarget(Object type, Object instrs) {
     assert "break".equals(type) || "continue".equals(type);
     targets.put(type, instrs);
   }
 
   public Object getTarget(Object type) {
     assert "break".equals(type) || "continue".equals(type);
     return targets.get(type);
   }
 
   // Emit the code necessary to abruptly leave a context (not
   // including the jump statement itself).
   // TODO: [2005-12-13 ptw] CodeGenerator translator -> Translator translator
   public void emitBreakPreamble(SimpleNode node, CodeGenerator translator) {
     if (isEnumeration) {
       translator.unwindEnumeration(node);
     }
   }
 }
 
