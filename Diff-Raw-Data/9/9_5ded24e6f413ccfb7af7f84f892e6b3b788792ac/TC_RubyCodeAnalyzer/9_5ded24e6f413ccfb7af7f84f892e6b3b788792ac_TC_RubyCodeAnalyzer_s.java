 /*
  * Author: David Corbin
  *
  * Copyright (c) 2005 RubyPeople.
  *
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
  * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
  * RDT except in compliance with the License. For further information see 
  * org.rubypeople.rdt/rdt.license.
  */
 package org.rubypeople.rdt.internal.core.builder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.jruby.ast.Node;
 import org.jruby.ast.visitor.NodeVisitor;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.eclipse.shams.resources.ShamFile;
 
 public class TC_RubyCodeAnalyzer extends TestCase {
 
     private static final String FILE_CONTENTS = "file Contents";
     private static final String FILENAME = "testFile.rb";
     private ShamFile file;
     private ShamMarkerManager markerManager;
     private ShamRubyParser parser;
     private RubyCodeAnalyzer compiler;
     private ShamIndexUpdater indexUpdater;
     private Node rootNode;
 
     public void setUp() {
         file = new ShamFile(FILENAME);
         file.setContents(FILE_CONTENTS);
         
        rootNode = new Node(null) {
             public Instruction accept(NodeVisitor visitor) {
                 return null;
             }
 
             public List childNodes() {
                 return new ArrayList();
             }
         };
         markerManager = new ShamMarkerManager();
         parser = new ShamRubyParser();
         parser.addParseResult(file, rootNode);
         indexUpdater = new ShamIndexUpdater();
         compiler = new RubyCodeAnalyzer(markerManager, parser, indexUpdater);
     }
     
     public void testParserInvocation() throws Exception {
         compiler.compileFile(file);
         
         parser.assertParsed(file, FILE_CONTENTS);
         file.assertContentStreamClosed();
         indexUpdater.assertUpdated(file, rootNode, true);
     }
     
 
     public void testSyntaxException() throws Exception {
         SyntaxException syntaxException = new SyntaxException(null, "");
         parser.setExceptionToThrow(syntaxException);
         
         compiler.compileFile(file);
         
         file.assertContentStreamClosed();
         markerManager.assertErrorCreated(file, syntaxException);
     }
 }
