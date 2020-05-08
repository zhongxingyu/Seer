 /***** BEGIN LICENSE BLOCK *****
  * Version: CPL 1.0/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Common Public
  * License Version 1.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.eclipse.org/legal/cpl-v10.html
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * Copyright (C) 2006 Mirko Stocker <me@misto.ch>
  * 
  * Alternatively, the contents of this file may be used under the terms of
  * either of the GNU General Public License Version 2 or later (the "GPL"),
  * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the CPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the CPL, the GPL or the LGPL.
  ***** END LICENSE BLOCK *****/
 
 package org.rubypeople.rdt.refactoring.core.formatsource;
 
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.jruby.ast.visitor.rewriter.FormatHelper;
 import org.jruby.ast.visitor.rewriter.ReWriteVisitor;
 import org.jruby.ast.visitor.rewriter.ReWriterFactory;
 import org.jruby.ast.visitor.rewriter.utils.ReWriterContext;
 import org.jruby.common.NullWarnings;
 import org.jruby.lexer.yacc.LexerSource;
 import org.jruby.parser.DefaultRubyParser;
 import org.jruby.parser.RubyParserConfiguration;
 import org.jruby.parser.RubyParserPool;
//import org.jruby.parser.postprocessor.DefaultCommentPlacer;
 
 public class PreviewGeneratorImpl implements PreviewGenerator {
 
 	private String source;
 
 	public PreviewGeneratorImpl(String source) {
 		this.source = source;
 	}
 
 	public String getPreview(FormatHelper formatHelper) {
 		StringWriter writer = new StringWriter();
 		ReWriterFactory factory = new ReWriterFactory(new ReWriterContext(new PrintWriter(writer), source, formatHelper));
 
 		DefaultRubyParser parser = RubyParserPool.getInstance().borrowParser();
 		parser.setWarnings(new NullWarnings());
 
 		LexerSource lexerSource = new LexerSource("", new StringReader(source)); //$NON-NLS-1$
 		ReWriteVisitor visitor = factory.createReWriteVisitor();
 		RubyParserConfiguration parserConfig = new RubyParserConfiguration();
//		parserConfig.addPostProcessor(new DefaultCommentPlacer());
 		parser.parse(parserConfig, lexerSource).getAST().accept(visitor);
 		visitor.flushStream();
 		RubyParserPool.getInstance().returnParser(parser);
 		return writer.getBuffer().toString();
 	}
 }
