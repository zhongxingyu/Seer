 /*
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.jstestdriver.coverage;
 
 import com.google.inject.Inject;
 import com.google.jstestdriver.coverage.es3.ES3InstrumentLexer;
 import com.google.jstestdriver.coverage.es3.ES3InstrumentParser;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenRewriteStream;
 import org.antlr.stringtemplate.StringTemplateGroup;
 
 import java.io.CharArrayReader;
import java.util.Collections;
import java.util.List;
 
 /**
  * Decorates the source code with coverage instrumentation.
  * @author corysmith@google.com (Cory Smith)
  * @author misko@google.com (Misko Hevery)
  */
 public class CodeInstrumentor implements Instrumentor {
   private static final char[] TEMPLATE =
     ("group TestRewrite;\n" +
      "init_instrument(stmt, hash, name, lines) ::= \"LCOV_<hash>=" +
         "LCOV.initNoop(<name>,0,<lines>);<stmt>\"" +
      "instrument(stmt, hash, ln) ::= \"LCOV_<hash>[<ln>]++; <stmt>\"" +
      "pass(stmt) ::= \"<stmt>\"").toCharArray();
   private final CoverageNameMapper mapper;
 
   @Inject
   public CodeInstrumentor(CoverageNameMapper mapper) {
     this.mapper = mapper;
   }
   
 
   public InstrumentedCode instrument(Code code) {
     StringTemplateGroup templates = new StringTemplateGroup(new CharArrayReader(TEMPLATE));
     ANTLRStringStream stream = new ANTLRStringStream(code.getSourceCode());
     Integer fileId = mapper.map(code.getFilePath());
     String mappedName = String.valueOf(fileId);
     stream.name = mappedName;
     ES3InstrumentLexer lexer = new ES3InstrumentLexer(stream);
     TokenRewriteStream tokens = new TokenRewriteStream(lexer);
     ES3InstrumentParser parser = new ES3InstrumentParser(tokens);
     parser.setTemplateLib(templates);
     try {
       parser.program();
     } catch (RecognitionException e) {
       throw new RuntimeException(e);
     }
    List<Integer> executableLines = parser.linesMap.get(mappedName);
     return new InstrumentedCode(fileId,
                                 code.getFilePath(),
                                executableLines == null ?
                                    Collections.<Integer>emptyList() : executableLines,
                                 tokens.toString());
   }
 }
