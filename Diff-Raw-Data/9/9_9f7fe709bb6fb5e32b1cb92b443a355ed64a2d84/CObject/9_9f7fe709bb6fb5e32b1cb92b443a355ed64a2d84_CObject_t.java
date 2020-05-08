 package edu.berkeley.cs.builtin.objects;
 
 import edu.berkeley.cs.builtin.functions.GetField;
 import edu.berkeley.cs.builtin.functions.Invokable;
 import edu.berkeley.cs.builtin.functions.PutField;
 import edu.berkeley.cs.builtin.objects.preprocessor.*;
 import edu.berkeley.cs.lexer.BasicScanner;
 import edu.berkeley.cs.lexer.Lexer;
 import edu.berkeley.cs.lexer.Scanner;
 import edu.berkeley.cs.parser.*;
 
 import java.io.*;
 import java.util.IdentityHashMap;
 import java.util.TreeMap;
 
 /**
  * Copyright (c) 2006-2011,
  * Koushik Sen    <ksen@cs.berkeley.edu>
  * All rights reserved.
  * <p/>
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  * <p/>
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * <p/>
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * <p/>
  * 3. The names of the contributors may not be used to endorse or promote
  * products derived from this software without specific prior written
  * permission.
  * <p/>
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /* @keywords
     @block @argument @LS @literal @symbol @nl { } true false null number-literals string-literals def assert print ( ) ,
     |
 
     */
 
 public class CObject {
     private CObject SS;
     private Reference prototype;
 
 //    private CObject superC;  // for efficiency only
     private RuleNode rules;
 
     public CObject() {
         rules = new RuleNode(null);
     }
 
     public void setRule(CObject methods) {
         this.rules = methods.rules;
     }
 
     public CObject getParent(boolean isPrototype) {
         if (isPrototype)
             return prototype==null?null:prototype.value;
         else
             return SS;
     }
 
     public void setParent(CObject obj,boolean isPrototype) {
         if (isPrototype) {
             this.prototype = new Reference(obj);
             if (obj !=null) {
                 this.addNewRule();
                 this.addSymbol(SymbolTable.getInstance().getId("prototype"));
                 this.addAction(new GetField(prototype));
 
                 this.addNewRule();
                 this.addSymbol(SymbolTable.getInstance().getId("prototype"));
                 this.addSymbol(SymbolTable.getInstance().getId("="));
                this.addMeta(SymbolTable.getInstance().expr);
                 this.addAction(new PutField(prototype));
             }
         } else {
             this.SS = obj;
             if (obj !=null) {
                 this.addNewRule();
                 this.addSymbol(SymbolTable.getInstance().getId("SS"));
                 this.addAction(new GetField(new Reference(obj)));
             }
         }
     }
 
 //    public void setSuperClass(CObject superClass) {
 //        this.superC = superClass;
 //    }
 
     public RuleNode getRuleNode() {
         return rules;
     }
 
 //    public CObject getSuperClass() {
 //        return superC;
 //    }
 
     private static CompoundToken parseIt(Reader in,String fname) throws IOException {
         Lexer lexer = new Lexer(in);
         Scanner scnr = new BasicScanner(lexer);
 
         CObject LS = new TokenEater(null,fname);
         CallFrame cf = new CallFrame(LS,LS,scnr);
         CompoundToken pgm = (CompoundToken)((TokenEater)cf.interpret()).returnNewCompoundToken();
         return pgm;
     }
 
     public CObject eval(String s) {
         try {
             CompoundToken pgm = parseIt(new StringReader(s),null);
             return pgm.execute(this,true); //comeback
         } catch (IOException e) {
             e.printStackTrace();
             throw new RuntimeException(e.getMessage());
         }
     }
 
     private static TreeMap<String,CompoundToken> codeCache = new TreeMap<String, CompoundToken>();
     public static String currentFile = null;
     public static IdentityHashMap<CompoundToken,CObject> staticObjects = new IdentityHashMap<CompoundToken, CObject>();
 
     public static CObject load(String file, boolean reload) {
         Reader in = null;
         CompoundToken pgm;
         try {
             if (reload || !codeCache.containsKey(file)) {
                 in = new BufferedReader(new FileReader(file));
                 pgm = parseIt(in,file);
                 codeCache.put(file,pgm);
             } else {
                 pgm = codeCache.get(file);
             }
             return pgm;
         } catch (IOException e) {
             e.printStackTrace();
             throw new RuntimeException(e.getMessage());
         } finally {
             if (in!=null)
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
         }
     }
 
     public CObject equality(CObject ret) {
         return this==ret?BooleanToken.TRUE():BooleanToken.FALSE();
     }
 
     public CObject disequality(CObject ret) {
         return this==ret?BooleanToken.FALSE():BooleanToken.TRUE();
     }
 
 
 
     public CObject newDefinitionEater() {
         return new CDefinitionEater(this);
     }
 
 
     public CObject loadFile(CObject file) {
         File f = new File(currentFile);
         f = new File(f.getParent(),((StringToken)file).value);
         return load(f.getAbsolutePath(),false);
     }
 
     public CObject reloadFile(CObject file) {
         File f = new File(currentFile);
         f = new File(f.getParent(),((StringToken)file).value);
         return load(f.getAbsolutePath(),true);
     }
 
     public CObject assignment(CObject sym, CObject value) {
         CObject self = this;
         SymbolToken symbol = (SymbolToken)sym;
 
         Reference common = new Reference(value);
 
         self.addNewRule();
         self.addSymbol(symbol.symbol);
         self.addAction(new GetField(common));
 
         self.addNewRule();
         self.addSymbol(symbol.symbol);
         self.addSymbol(SymbolTable.getInstance().getId("="));
         self.addMeta(SymbolTable.getInstance().expr);
         self.addAction(new PutField(common));
 
         return value;
 
     }
 
     public CObject print(CObject ret) {
         System.out.println(ret);
         return ret;
     }
 
 
     public CObject assertEquality(CObject first) {
         if (!((BooleanToken)first).value)
             throw new RuntimeException("assert failed");
         return first;
     }
 
     public CObject returnArgument(CObject arg) {
         return arg;
     }
 
     public CObject whileAction(CObject S1, CObject S2) {
         CompoundToken s1 = (CompoundToken)S1;
         CompoundToken s2 = (CompoundToken)S2;
         while(((BooleanToken)s1.execute(this,false)).value) {
             s2.execute(this,false);
         }
         return this;
     }
 
 
 
     public CObject onceAction(CObject S) {
         CompoundToken s = (CompoundToken)S;
         CObject val;
         if ((val = CObject.staticObjects.get(s))==null) {
             val = s.execute(this,false);
             CObject.staticObjects.put(s,val);
         }
         return val;
     }
 
     public CObject ifAction(CObject c1, CObject S1, CObject S2) {
         BooleanToken cond = (BooleanToken) c1;
         CompoundToken s1 = (CompoundToken)S1;
         CompoundToken s2 = (CompoundToken)S2;
         if (cond.value) {
             s1.execute(this,false);
             return this;
         } else {
             s2.execute(this,false);
             return this;
         }
     }
 
 
     public CObject newObject() {
         CNonPrimitiveObject ret = new CNonPrimitiveObject();
         ret.setParent(this,false);
         return ret;
     }
 
     @Override
     public boolean equals(Object o) {
         if (!(o instanceof CObject)) return false;
         return this == o;
     }
 
 
     RuleNode curr;
     int argCount;
 
     public void addNewRule() {
         if(rules == null) {
             rules = new RuleNode(null);
         }
         curr = rules;
         argCount=0;
     }
 
     public void addMeta(int argument) {
         curr = curr.addMeta(rules,argument);
         argCount++;
     }
 
     public void addMeta(int argument,boolean override) {
         curr = curr.addMeta(rules,argument,override);
         argCount++;
     }
 
     public void addSymbol(int symbol) {
         curr = curr.addSymbol(symbol);
     }
 
     public void addNewLine() {
         curr = curr.addNewLine();
     }
 
     public void addAction(Invokable func) {
         curr = curr.addAction(func,argCount);
     }
 
 //    public RuleNode getSuperRuleNode() {
 //        if (superC == null) return null;
 //        return superC.getRuleNode();
 //    }
 }
