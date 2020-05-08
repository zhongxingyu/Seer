 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.llvm.LLVMEmitter;
 import me.pavlina.alco.llvm.LLVMType;
 import me.pavlina.alco.llvm.Function;
 import me.pavlina.alco.llvm.Conversion;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.lex.TokenStream;
 import java.util.List;
 import java.util.Arrays;
 import java.math.BigInteger;
 
 /**
  * Cast operator. */
 public class OpCast extends Expression.Operator {
     private Token token;
     private Expression[] children;
     private Type type;
     private String valueString;
 
     public static Expression.OperatorCreator CREATOR;
 
     /**
      * Generate a cast from an expression at typecheck time. This is used
      * for coercion. */
     public OpCast (HasType value, Type type, Env env) {
         children = new Expression[] { (Expression) value };
         token = ((Expression) value).getToken ();
         this.type = type;
     }
 
     public OpCast (Env env, TokenStream stream) throws CError {
         children = new Expression[1];
         token = stream.next ();
         type = TypeParser.parse (stream, env);
     }
 
     public int getPrecedence () {
         return me.pavlina.alco.language.Precedence.CAST;
     }
 
     public Expression.Associativity getAssociativity () {
         return Expression.Associativity.LEFT;
     }
 
     public Expression.Arity getArity () {
         return Expression.Arity.UNARY;
     }
 
     public Type getType () {
         return type;
     }
 
     public void setOperands (Expression op, Expression ignore) {
         children[0] = op;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         children[0].checkTypes (env, resolver);
         Type srcT = children[0].getType ();
         Type dstT = type;
         Type.Encoding srcE = srcT.getEncoding ();
         Type.Encoding dstE = dstT.getEncoding ();
 
         // See Standard:Types:Casting:AllowedCasts
         if (srcT.equalsNoConst (dstT)) {
             // T to T
             // OK
    
         } else if (((srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT)||
                     (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT))
                    && (srcT.getSize () != dstT.getSize ())) {
             throw CError.at ("cannot cast integer in both sign and width;\n" +
                              "sign and width casts are not commutative",
                              token);
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
             // SI to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
             // UI to SI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () < dstT.getSize ()) {
             // SI to SI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () > dstT.getSize ()) {
             // SI to SI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () < dstT.getSize ()) {
             // UI to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () > dstT.getSize ()) {
             // UI to UI
             // OK
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
             // SI to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
             // FP to SI
             // OK
         
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
             // UI to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
             // FP to UI
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () < dstT.getSize ()) {
             // FP to FP
             // OK
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () > dstT.getSize ()) {
             // FP to FP
             // OK
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.POINTER) {
             // T* to U*
             // OK
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.UINT &&
                    dstT.getSize () >= (env.getBits () / 8)) {
             // T* to UI
             // OK
 
         } else if (srcE == Type.Encoding.UINT &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSize () >= (env.getBits () / 8)) {
             // UI to T*
             // OK
 
         } else if (srcE == Type.Encoding.ARRAY &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSubtype ().equals (dstT.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("NOT IMPLEMENTED YET");
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.SINT) {
             // null to SI
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.UINT) {
             // null to UI
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.OBJECT) {
             // null to class
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.ARRAY) {
             // null to T[]
             // OK
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.POINTER) {
             // null to T*
             // OK
 
         } else {
             throw CError.at ("invalid cast", token);
         }
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         children[0].genLLVM (env, emitter, function);
         String val = children[0].getValueString ();
         String sty = LLVMType.getLLVMName (children[0].getType ());
         String dty = LLVMType.getLLVMName (type);
         Type srcT = children[0].getType ();
         Type dstT = type;
         Type.Encoding srcE = srcT.getEncoding ();
         Type.Encoding dstE = dstT.getEncoding ();
 
         // See Standard:Types:Casting:AllowedCasts
         if (srcT.equalsNoConst (dstT)) {
             // T to T
             valueString = val;
    
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.UINT) {
             // SI to UI
             valueString = val;
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.SINT) {
             // UI to SI
             valueString = val;
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () < dstT.getSize ()) {
             // SI to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.SEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.SINT
                    && srcT.getSize () > dstT.getSize ()) {
             // SI to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.TRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () < dstT.getSize ()) {
             // UI to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.ZEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.UINT
                    && srcT.getSize () > dstT.getSize ()) {
             // UI to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.TRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.SINT && dstE == Type.Encoding.FLOAT) {
             // SI to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.SITOFP)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.SINT) {
             // FP to SI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTOSI)
                 .source (sty, val).dest (dty).build ();
         
         } else if (srcE == Type.Encoding.UINT && dstE == Type.Encoding.FLOAT) {
             // UI to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.UITOFP)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.UINT) {
             // FP to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTOUI)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () < dstT.getSize ()) {
             // FP to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPEXT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.FLOAT && dstE == Type.Encoding.FLOAT
                    && srcT.getSize () > dstT.getSize ()) {
             // FP to FP
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.FPTRUNC)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.POINTER) {
             // T* to U*
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.BITCAST)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.POINTER &&
                    dstE == Type.Encoding.UINT &&
                    dstT.getSize () >= (env.getBits () / 8)) {
             // T* to UI
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.PTRTOINT)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.UINT &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSize () >= (env.getBits () / 8)) {
             // UI to T*
             valueString = new Conversion (emitter, function)
                 .operation (Conversion.ConvOp.INTTOPTR)
                 .source (sty, val).dest (dty).build ();
 
         } else if (srcE == Type.Encoding.ARRAY &&
                    dstE == Type.Encoding.POINTER &&
                    srcT.getSubtype ().equals (dstT.getSubtype ())) {
             // T[] to T*
             throw new RuntimeException ("NOT IMPLEMENTED YET");
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.SINT) {
             // null to SI
             valueString = "0";
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.UINT) {
             // null to UI
             valueString = "0";
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.OBJECT) {
             // null to class
             valueString = val;
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.ARRAY) {
             // null to T[]
             valueString = val;
 
         } else if (srcE == Type.Encoding.NULL &&
                    dstE == Type.Encoding.POINTER) {
             // null to T*
             valueString = "null";
 
         } else {
             throw new RuntimeException ("Invalid cast in genLLVM");
         }
     }
 
     public String getValueString () {
         return valueString;
     }
 
     public void checkPointer (boolean write, Token token) throws CError {
         // If we are casting off const, lie to checkPointer about intent to
         // read
         if (!type.isConst () && children[0].getType ().isConst ()) {
             children[0].checkPointer (false, token);
         } else {
             children[0].checkPointer (write, token);
         }
     }
 
     public String getPointer (Env env, LLVMEmitter emitter, Function function) {
         // Casting a pointer is just a bit-cast.
         String chPtr = children[0].getPointer (env, emitter, function);
         String ptr = new Conversion (emitter, function)
             .operation (Conversion.ConvOp.BITCAST)
            .source (LLVMType.getLLVMName (children[0].getType ()), chPtr)
            .dest (LLVMType.getLLVMName (type))
             .build ();
         return ptr;
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("Cast");
         out.println ("  " + type.toString ());
         children[0].print (out, 2);
     }
 
     @SuppressWarnings("unchecked")
     public List<AST> getChildren () {
         return (List) Arrays.asList (children);
     }
 
     public Token getToken () {
         return token;
     }
 
     static {
         CREATOR = new Expression.OperatorCreator () {
                 public Operator create (Env env, TokenStream stream)
                     throws CError {
                     return new OpCast (env, stream);
                 }
             };
     }
 }
