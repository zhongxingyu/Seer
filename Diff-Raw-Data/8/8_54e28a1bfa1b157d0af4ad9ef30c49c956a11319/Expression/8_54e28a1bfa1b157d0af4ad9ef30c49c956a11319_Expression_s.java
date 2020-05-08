 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.  
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or 
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)  
  *
  * This software is provided "as is", with NO WARRANTY, not even the 
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.  
  */
 
 package org.webmacro.engine;
 
 import java.io.*;
 import org.webmacro.*;
 
 public abstract class Expression { 
 
   public abstract static class ExpressionBase implements Macro, Visitable {
     protected ExpressionBase() {}
 
     // Macro methods 
     final public void write(FastWriter out, Context context) 
       throws PropertyException, IOException {
       out.write(evaluate(context).toString());
     }
   }
 
   final private static Boolean TRUE  = Boolean.TRUE;
   final private static Boolean FALSE = Boolean.FALSE;
 
   public static boolean isTrue(Object o) {
     if (o == null) 
       return false;
     else if (o instanceof Boolean)
       return ((Boolean) o).booleanValue();
     else 
       return (o != null);
   }
 
   public static boolean isNumber(Object o) {
    return ((o instanceof Integer) 
            || o instanceof Long
            || o instanceof Short
            || o instanceof Byte);
   }
 
   public static Object numberObject(long result, Object op1, Object op2) {
      if (op1.getClass() == Long.class || op2.getClass() == Long.class)
         return new Long(result);
      else 
         return new Integer((int) result);
   }
 
   public static long numberValue(Object o) {
     return ((Number) o).longValue();
   }
 
   public abstract static class BinaryOperation extends ExpressionBase {
 
     private Object _l, _r;
     
     BinaryOperation(Object l, Object r) { _l = l; _r = r; }
 
     public abstract Object operate(Object l, Object r) throws PropertyException;
 
     public Object evaluate(Context context) 
       throws PropertyException  {
       Object l, r;
 
       try { 
         l = (_l instanceof Macro) ? ((Macro) _l).evaluate(context) : _l;
       } catch (Exception e) { l = null; }
       try { 
         r = (_r instanceof Macro) ? ((Macro) _r).evaluate(context) : _r;
       } catch (Exception e) { r = null; }
 
       return operate(l, r);
     }
 
     public abstract String getName();
 
     public void accept(TemplateVisitor v) { 
       v.visitBinaryOperation(getName(), _l, _r);
     }
   }
 
   public abstract static class UnaryOperation extends ExpressionBase {
 
     private Object _o;
     
     UnaryOperation(Object o) { _o = o; }
 
     public abstract Object operate(Object o);
 
     public Object evaluate(Context context) 
       throws PropertyException {
       Object o = (_o instanceof Macro) ? ((Macro) _o).evaluate(context) : _o;
 
       return operate(o);
     }
 
     public abstract String getName();
 
     public void accept(TemplateVisitor v) { 
       v.visitUnaryOperation(getName(), _o);
     }
   }
 
   public static class AndOperation extends ExpressionBase {
     private Object _l, _r;
     
     public AndOperation(Object l, Object r) { _l = l; _r = r; }
 
     public Object evaluate(Context context) 
       throws PropertyException  {
       Object l, r;
 
       try { 
         l = (_l instanceof Macro) ? ((Macro) _l).evaluate(context) : _l;
       } catch (Exception e) { l = null; }
       if (!isTrue(l)) 
         return FALSE;
       try { 
         r = (_r instanceof Macro) ? ((Macro) _r).evaluate(context) : _r;
       } catch (Exception e) { r = null; }
       if (!isTrue(r))
         return FALSE;
 
       return TRUE;
     }
 
     public void accept(TemplateVisitor v) { 
       v.visitBinaryOperation("And", _l, _r);
     }
   }
 
   public static class OrOperation extends ExpressionBase {
     private Object _l, _r;
     
     public OrOperation(Object l, Object r) { _l = l; _r = r; }
 
     public Object evaluate(Context context) 
       throws PropertyException  {
       Object l, r;
 
       try { 
         l = (_l instanceof Macro) ? ((Macro) _l).evaluate(context) : _l;
       } catch (Exception e) { l = null; }
       if (isTrue(l)) 
         return TRUE;
       try { 
         r = (_r instanceof Macro) ? ((Macro) _r).evaluate(context) : _r;
       } catch (Exception e) { r = null; }
       if (isTrue(r))
         return TRUE;
 
       return FALSE;
     }
 
     public void accept(TemplateVisitor v) { 
       v.visitBinaryOperation("Or", _l, _r);
     }
   }
 
   public static class NotOperation extends UnaryOperation {
     public NotOperation(Object o) { super(o); }
     public String getName() { return "Not"; }
 
     public Object operate(Object o) {
       return (!Expression.isTrue(o)) ? TRUE : FALSE;
     }
   }
 
 
   public static class AddOperation extends BinaryOperation {
     public AddOperation(Object l, Object r) { super(l, r); }
     public String getName() { return "Add"; }
 
     public Object operate(Object l, Object r) throws PropertyException {
       if (!isNumber(l) || !isNumber(r))
         throw new PropertyException("Add requires numeric operands");
       else
         return numberObject(numberValue(l) + numberValue(r), l, r);
     }
   }
 
   public static class SubtractOperation extends BinaryOperation {
     public SubtractOperation(Object l, Object r) { super(l, r); }
     public String getName() { return "Subtract"; }
 
     public Object operate(Object l, Object r) throws PropertyException {
       if (!isNumber(l) || !isNumber(r))
         throw new PropertyException("Subtract requires numeric operands");
       else
         return numberObject(numberValue(l) - numberValue(r), l, r);
     }
   }
 
   public static class MultiplyOperation extends BinaryOperation {
     public MultiplyOperation(Object l, Object r) { super(l, r); }
     public String getName() { return "Multiply"; }
 
     public Object operate(Object l, Object r) throws PropertyException {
       if (!isNumber(l) || !isNumber(r))
         throw new PropertyException("Multiply requires numeric operands");
       else
         return numberObject(numberValue(l) * numberValue(r), l, r);
     }
   }
 
   public static class DivideOperation extends BinaryOperation {
     public DivideOperation(Object l, Object r) { super(l, r); }
     public String getName() { return "Divide"; }
 
     public Object operate(Object l, Object r) throws PropertyException {
       if (!isNumber(l) || !isNumber(r))
         throw new PropertyException("Divide requires numeric operands");
       else {
         long denom = numberValue(r);
         if (denom == 0)
           throw new PropertyException("Divide by zero");
         else 
           return numberObject(numberValue(l) / denom, l, r);
       }
     }
   }
 
 
   public abstract static class Compare extends BinaryOperation {
     public Compare(Object l, Object r) { super(l, r); }
     
     public abstract Boolean compare(String l, String r);
     public abstract Boolean compare(long l, long r);
     public Boolean compare(Object l, Object r) {
       return null;
     }
     public Boolean compareNull(Object o) {
       return null;
     }
 
     public Object operate(Object l, Object r) throws PropertyException {
       Boolean b=null;
       boolean lIsNumber = isNumber(l), rIsNumber=isNumber(r);
 
       if (lIsNumber && rIsNumber)
         b = compare(numberValue(l), numberValue(r));
       else {
         boolean lIsString = (l instanceof String), 
           rIsString = (r instanceof String);
 
         if (lIsString && rIsString) 
           b = compare((String) l, (String) r);
         else if (lIsString && rIsNumber) 
           b = compare((String) l, Long.toString(numberValue(r)));
         else if (lIsNumber && rIsString) 
           b = compare(Long.toString(numberValue(l)), (String) r);
         else if (l == null) 
           b = compareNull(r);
         else if (r == null) 
           b = compareNull(l);
         else 
           b = compare(l, r);
       }
 
       if (b == null)
         throw new PropertyException("Objects not comparable");
       else 
         return b;
     }
   }
 
   public static class CompareEq extends Compare {
     public CompareEq(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareEq"; }
 
     public Boolean compare(Object l, Object r) {
       return (l.equals(r)) ? TRUE : FALSE;
     }
 
     public Boolean compare(String l, String r) {
       return (l.equals(r)) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l == r) ? TRUE : FALSE;
     }
 
     public Boolean compareNull(Object o) {
       return (o == null) ? TRUE : FALSE;
     }
   }
 
   public static class CompareNe extends Compare {
     public CompareNe(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareNe"; }
 
     public Boolean compare(Object l, Object r) {
      return (l.equals(r)) ? TRUE : FALSE;
     }
 
     public Boolean compare(String l, String r) {
       return (!l.equals(r)) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l != r) ? TRUE : FALSE;
     }
 
     public Boolean compareNull(Object o) {
       return (o != null) ? TRUE : FALSE;
     }
   }
 
   public static class CompareLe extends Compare {
     public CompareLe(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareLe"; }
 
     public Boolean compare(String l, String r) {
       return (l.compareTo(r) <= 0) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l <= r) ? TRUE : FALSE;
     }
   }
 
   public static class CompareLt extends Compare {
     public CompareLt(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareLt"; }
 
     public Boolean compare(String l, String r) {
       return (l.compareTo(r) < 0) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l < r) ? TRUE : FALSE;
     }
   }
 
   public static class CompareGe extends Compare {
     public CompareGe(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareGe"; }
 
     public Boolean compare(String l, String r) {
       return (l.compareTo(r) >= 0) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l >= r) ? TRUE : FALSE;
     }
   }
 
   public static class CompareGt extends Compare {
     public CompareGt(Object l, Object r) { super(l, r); }
     public String getName() { return "CompareGt"; }
 
     public Boolean compare(String l, String r) {
       return (l.compareTo(r) > 0) ? TRUE : FALSE;
     }
 
     public Boolean compare(long l, long r) {
       return (l > r) ? TRUE : FALSE;
     }
   }
 
 
 
   public abstract static class BinaryOperationBuilder implements Builder {
 
     private Object _l, _r;
 
     public BinaryOperationBuilder(Object l, Object r) { _l = l; _r = r; }
 
     public abstract Object build(Object l, Object r) throws BuildException;
 
     public Object build(BuildContext pc) 
       throws BuildException {
       Object l, r;
       l = (_l instanceof Builder) ? ((Builder) _l).build(pc) : _l;
       r = (_r instanceof Builder) ? ((Builder) _r).build(pc) : _r;
       
       return build(l, r);
     }
   } 
 
   public abstract static class UnaryOperationBuilder implements Builder {
 
     private Object _o;
 
     public UnaryOperationBuilder(Object o) { _o = o; }
 
     public abstract Object build(Object o) throws BuildException;
 
     public Object build(BuildContext pc) 
       throws BuildException {
       Object o;
       o = (_o instanceof Builder) ? ((Builder) _o).build(pc) : _o;
       
       return build(o);
     }
   } 
 
 
   public static class AndBuilder extends BinaryOperationBuilder {
     public AndBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) {
       if (l instanceof Macro) {
         if (r instanceof Macro) 
           return new AndOperation(l, r);
         else 
           return isTrue(r) ? l : FALSE;
       }
       else {
         if (r instanceof Macro)
           return isTrue(l) ? r : FALSE;
         else 
           return (isTrue(l) && isTrue(r)) ? TRUE : FALSE;
       }
     }
   }
 
   public static class OrBuilder extends BinaryOperationBuilder {
     public OrBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) {
       if (l instanceof Macro) {
         if (r instanceof Macro) 
           return new OrOperation(l, r);
         else 
           return isTrue(r) ? TRUE : l;
       }
       else {
         if (r instanceof Macro)
           return isTrue(l) ? TRUE : r;
         else 
           return (isTrue(l) || isTrue(r)) ? TRUE : FALSE;
       }
     }
   }
 
   public static class NotBuilder extends UnaryOperationBuilder {
     public NotBuilder(Object o) { super(o); }
 
     public Object build(Object o) {
       if (o instanceof Macro) 
         return new NotOperation(o);
       else 
         return !isTrue(o) ? TRUE : FALSE;
     }
   }
 
 
 
   public static class AddBuilder extends BinaryOperationBuilder {
     public AddBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       if (!(l instanceof Macro) && !(r instanceof Macro)) {
         if (isNumber(l) && isNumber(r))
           return numberObject(numberValue(l) + numberValue(r), l, r);
         else
           throw new BuildException("Add requires numeric operands");
       }
       else
         return new AddOperation(l, r);
     }
   }
 
   public static class SubtractBuilder extends BinaryOperationBuilder {
     public SubtractBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       if (!(l instanceof Macro) && !(r instanceof Macro)) {
         if (isNumber(l) && isNumber(r))
           return numberObject(numberValue(l) - numberValue(r), l, r);
         else
           throw new BuildException("Subtract requires numeric operands");
       }
       else
         return new SubtractOperation(l, r);
     }
   }
 
   public static class MultiplyBuilder extends BinaryOperationBuilder {
     public MultiplyBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       if (!(l instanceof Macro) && !(r instanceof Macro)) {
         if (isNumber(l) && isNumber(r))
           return numberObject(numberValue(l) * numberValue(r), l, r);
         else
           throw new BuildException("Multiply requires numeric operands");
       }
       else
         return new MultiplyOperation(l, r);
     }
   }
 
   public static class DivideBuilder extends BinaryOperationBuilder {
     public DivideBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       if (!(l instanceof Macro) && !(r instanceof Macro)) {
         if (isNumber(l) && isNumber(r)) {
           long denom = numberValue(r);
           if (denom == 0)
             throw new BuildException("Divide by zero");
           else 
             return numberObject(numberValue(l) / denom, l, r);
         }
         else
           throw new BuildException("Divide requires numeric operands");
       }
       else
         return new DivideOperation(l, r);
     }
   }
 
   public static class CompareEqBuilder extends BinaryOperationBuilder {
     public CompareEqBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareEq c = new CompareEq(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 
   public static class CompareNeBuilder extends BinaryOperationBuilder {
     public CompareNeBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareNe c = new CompareNe(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 
   public static class CompareLeBuilder extends BinaryOperationBuilder {
     public CompareLeBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareLe c = new CompareLe(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 
   public static class CompareLtBuilder extends BinaryOperationBuilder {
     public CompareLtBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareLt c = new CompareLt(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 
   public static class CompareGeBuilder extends BinaryOperationBuilder {
     public CompareGeBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareGe c = new CompareGe(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 
   public static class CompareGtBuilder extends BinaryOperationBuilder {
     public CompareGtBuilder(Object l, Object r) { super(l,r); }
 
     public Object build(Object l, Object r) throws BuildException {
       CompareGt c = new CompareGt(l, r);
 
       if ((l instanceof Macro) || (r instanceof Macro))
         return c;
       else 
         try {
           return c.operate(l, r);
         }
         catch (PropertyException e) { throw new BuildException(e.toString()); }
     }
   }
 }
