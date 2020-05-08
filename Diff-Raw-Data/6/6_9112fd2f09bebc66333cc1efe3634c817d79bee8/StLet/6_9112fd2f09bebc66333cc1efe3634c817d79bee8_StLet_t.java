 // Copyright (c) 2011, Christopher Pavlina. All rights reserved.
 
 package me.pavlina.alco.ast;
 import me.pavlina.alco.compiler.Env;
 import me.pavlina.alco.compiler.errors.*;
 import me.pavlina.alco.lex.TokenStream;
 import me.pavlina.alco.lex.Token;
 import me.pavlina.alco.language.Keywords;
 import me.pavlina.alco.language.Type;
 import me.pavlina.alco.language.HasType;
 import me.pavlina.alco.language.Resolver;
 import me.pavlina.alco.llvm.*;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Variable assignment. Syntax:
  *  - LET := let {VARIABLE} : {expression} [ , {VARIABLE} : {expression} ]* ;
  *  - VARIABLE := {name} [ {type} ]?
  */
 public class StLet extends Statement
 {
     private Token token;
     private List<String> names;
     private List<String> realNames;
     private List<Type> types;
     private List<Expression> expressions;
 
     public StLet (Env env, TokenStream stream, Method method) throws CError {
         Token token;
 
         this.token = stream.next ();
         if (!this.token.is (Token.WORD, "let")) {
             throw new RuntimeException ("StLet instantiated without let kwd");
         }
 
         names = new ArrayList<String> ();
         realNames = new ArrayList<String> ();
         types = new ArrayList<Type> ();
         expressions = new ArrayList<Expression> ();
 
         while (true) {
             String name;
             Type type;
             Expression value;
 
             // Name
             token = stream.next ();
             if (token.is (Token.NO_MORE))
                 throw UnexpectedEOF.after ("name", stream.last ());
             else if (!token.is (Token.WORD) ||
                 Keywords.isKeyword (token.value, true))
                 throw Unexpected.at ("name", token);
             name = token.value;
 
             // Type and colon
             token = stream.next ();
             if (token.is (Token.NO_MORE))
                 throw UnexpectedEOF.after (": or type", stream.last ());
             else if (token.is (Token.OPER, ":"))
                 type = null;
             else {
                 stream.putback (token);
                 type = TypeParser.parse (stream, env);
                 token = stream.next ();
                 if (token.is (Token.NO_MORE))
                     throw UnexpectedEOF.after (":", stream.last ());
                 else if (!token.is (Token.OPER, ":"))
                     throw Unexpected.after (":", stream.last ());
             }
 
             // Value
             value = Expression.parse (env, stream, ";,");
             if (value == null)
                 throw Unexpected.after ("expression", token);
 
             names.add (name);
             realNames.add (null);
             types.add (type);
             expressions.add (value);
 
             token = stream.next ();
             if (token.is (Token.OPER, ";"))
                 break;
             else if (!token.is (Token.OPER, ","))
                 throw Unexpected.after (", or ;", stream.last ());
 
         }
     }
 
     public Token getToken () {
         return token;
     }
 
     @SuppressWarnings("unchecked") // :-( I'm sorry
     public List<AST> getChildren () {
         return (List) expressions;
     }
 
     public void checkTypes (Env env, Resolver resolver) throws CError {
         for (Expression i: expressions) {
             i.checkTypes (env, resolver);
         }
 
         Type.CastCreator castcreator = new Type.CastCreator () {
                 public HasType cast (HasType value, Type type,
                                           Env env) {
                     return new OpCast (value, type, env);
                 }
             };
         for (int i = 0; i < names.size (); ++i) {
             if (types.get (i) == null) {
                 types.set (i, expressions.get (i).getType ());
             } else {
                expressions.set (i, new OpCast
                                 (expressions.get (i), types.get (i), env));
             }
             realNames.set
                 (i, resolver.addVariable
                  (names.get (i), types.get (i)).getName ());
         }
             
     }
 
     public void genLLVM (Env env, LLVMEmitter emitter, Function function) {
         for (int i = 0; i < names.size (); ++i) {
             new alloca (emitter, function)
                 .type (LLVMType.getLLVMName (types.get (i)))
                 .result (realNames.get (i))
                 .build ();
             
             expressions.get (i).genLLVM (env, emitter, function);
             Type.Encoding enc = types.get (i).getEncoding ();
 
             // Simple assign
             if (enc == Type.Encoding.UINT ||
                 enc == Type.Encoding.SINT ||
                 enc == Type.Encoding.FLOAT ||
                 enc == Type.Encoding.POINTER) {
                 
                 String val = expressions.get (i).getValueString ();
                 new store (emitter, function)
                     .pointer (realNames.get (i))
                     .value (LLVMType.getLLVMName (types.get (i)), val)
                     .build ();
             }
 
             // Null assign
             else if (NullValue.class.isInstance (expressions.get (i))) {
                 String elem1 = new getelementptr (emitter, function)
                     .type ("%.nonprim")
                     .pointer (realNames.get (i))
                     .inbounds (true)
                     .addIndex (0).addIndex (0)
                     .build ();
 
                 String elem2 = new getelementptr (emitter, function)
                     .type ("%.nonprim")
                     .pointer (realNames.get (i))
                     .inbounds (true)
                     .addIndex (0).addIndex (1)
                     .build ();
 
                 new store (emitter, function)
                     .pointer (elem1)
                     .value ("i64", "0")
                     .build ();
 
                 new store (emitter, function)
                     .pointer (elem2)
                     .value ("i64", "0")
                     .build ();
             }
 
             // Obj/arr assign
             else if (enc == Type.Encoding.ARRAY ||
                      enc == Type.Encoding.OBJECT) {
                 String valueString = expressions.get (i).getValueString ();
                 String pdestElem1 = new getelementptr (emitter, function)
                     .type ("%.nonprim").pointer (realNames.get (i))
                     .inbounds (true).addIndex (0).addIndex (0).build ();
                 String pdestElem2 = new getelementptr (emitter, function)
                     .type ("%.nonprim").pointer (realNames.get (i))
                     .inbounds (true).addIndex (0).addIndex (1).build ();
                 String psrcElem1 = new getelementptr (emitter, function)
                     .type ("%.nonprim").pointer (valueString)
                     .inbounds (true).addIndex (0).addIndex (0).build ();
                 String psrcElem2 = new getelementptr (emitter, function)
                     .type ("%.nonprim").pointer (valueString)
                     .inbounds (true).addIndex (0).addIndex (1).build ();
 
                 String srcElem1 = new load (emitter, function)
                     .pointer ("%.nonprim", psrcElem1).build ();
                 String srcElem2 = new load (emitter, function)
                     .pointer ("%.nonprim", psrcElem2).build ();
 
                 new store (emitter, function)
                     .pointer (pdestElem1).value ("i64", srcElem1).build ();
                 new store (emitter, function)
                     .pointer (pdestElem2).value ("i64", srcElem2).build ();
             }
         }
     }
 
     public void print (java.io.PrintStream out) {
         out.println ("Let");
         for (int i = 0; i < names.size (); ++i) {
             out.println ("  " + names.get (i));
             expressions.get (i).print (out, 4);
         }
     }
 
     public static Statement.StatementCreator CREATOR;
     static {
         CREATOR = new Statement.StatementCreator () {
                 public Statement create (Env env, TokenStream stream,
                                          Method method)
                     throws CError
                 {
                     return new StLet (env, stream, method);
                 }
             };
     }
 }
