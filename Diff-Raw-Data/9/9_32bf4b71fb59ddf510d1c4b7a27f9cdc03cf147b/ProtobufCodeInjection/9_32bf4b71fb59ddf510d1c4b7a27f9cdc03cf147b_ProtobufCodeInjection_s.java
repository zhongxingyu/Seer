 /*
  * Copyright 2010-2012 napile.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.compiler.injection.protobuf;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.lib.NapileLangPackage;
 import org.napile.compiler.injection.CodeInjection;
 import org.napile.compiler.injection.protobuf.lang.PbLanguage;
 import org.napile.compiler.injection.protobuf.lang.PbTokenTypes;
 import org.napile.compiler.injection.protobuf.lang.lexer.PbMergingLexer;
 import org.napile.compiler.injection.protobuf.lang.parser.PbParser;
 import org.napile.compiler.injection.protobuf.lang.psi.PbPsiCreator;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.TypeUtils;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.Language;
 import com.intellij.lang.PsiParser;
 import com.intellij.lexer.Lexer;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author VISTALL
  * @date 11:04/12.10.12
  */
 public class ProtobufCodeInjection extends CodeInjection
 {
 	@NotNull
 	@Override
 	public String getName()
 	{
		return "org/napile/idea/injection/protobuf";
 	}
 
 	@NotNull
 	@Override
 	public Language getLanguage()
 	{
 		return PbLanguage.INSTANCE;
 	}
 
 	@NotNull
 	@Override
 	public JetType getReturnType(@Nullable JetType expectType, @NotNull BindingTrace bindingTrace, @NotNull JetScope jetScope)
 	{
 		return TypeUtils.getTypeOfClassOrErrorType(jetScope, NapileLangPackage.ANY);
 	}
 
 	@NotNull
 	@Override
 	public Lexer createLexer(Project project)
 	{
 		return new PbMergingLexer();
 	}
 
 	@Override
 	public PsiParser createParser(Project project)
 	{
 		return new PbParser();
 	}
 
 	@NotNull
 	@Override
 	public TokenSet getWhitespaceTokens()
 	{
 		return PbTokenTypes.WHITE_SPACES;
 	}
 
 	@NotNull
 	@Override
 	public TokenSet getCommentTokens()
 	{
 		return PbTokenTypes.COMMENTS;
 	}
 
 	@NotNull
 	@Override
 	public TokenSet getStringLiteralElements()
 	{
 		return PbTokenTypes.STRING_LITERALS;
 	}
 
 	@NotNull
 	@Override
 	public PsiElement createElement(ASTNode astNode)
 	{
 		return PbPsiCreator.createElement(astNode);
 	}
 }
