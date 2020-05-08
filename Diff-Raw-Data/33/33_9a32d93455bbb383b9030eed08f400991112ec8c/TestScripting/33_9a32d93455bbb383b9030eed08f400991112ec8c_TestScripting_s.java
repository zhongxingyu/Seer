 /*
  * Created on 24. jan.. 2007
  *
  * Copyright (c) 2005-2011, Karl Trygve Kalleberg <karltk near strategoxt dot org>
  * 
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.adapter.ecj;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.junit.Before;
 import org.junit.Test;
 import org.spoofax.interpreter.core.Interpreter;
 import org.spoofax.interpreter.core.InterpreterException;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.terms.TermFactory;
 
 public class TestScripting {
 
     private ECJFactory wef;
     private Interpreter interp;
 
     @Before
     public void setUp() {
         wef = new ECJFactory();
         interp = new Interpreter(wef, new TermFactory());
     }
 
     private CompilationUnit parseCompilationUnit() {
         ASTParser parser = ASTParser.newParser(AST.JLS3);
         parser.setSource("class Foo { /** foo */ void dataInvariant() {} int f() { return 0; } }".toCharArray());
         return (CompilationUnit) parser.createAST(null);
     }
     
     private void setupData() {
         CompilationUnit cu = parseCompilationUnit();
         wef.setAST(cu.getAST());
         interp.setCurrent(wef.parseFromTree(cu));
     }
 
     @Test
    public void test_traversal_allid() throws IOException, InterpreterException {
        interp.load("bin/allid.ctree");
         setupData();
         assertTrue(interp.invoke("main_0_0"));
     }
 
     @Test
    public void test_traversal_all_ctor_debug() throws IOException, InterpreterException {
        interp.load("bin/all-ctor-debug.ctree");
         setupData();
         assertTrue(interp.invoke("main_0_0"));
         IStrategoList t = (IStrategoList) interp.current();
        System.out.println(t);
        assertTrue(t.getSubtermCount() > 10);
     }
 
 }
