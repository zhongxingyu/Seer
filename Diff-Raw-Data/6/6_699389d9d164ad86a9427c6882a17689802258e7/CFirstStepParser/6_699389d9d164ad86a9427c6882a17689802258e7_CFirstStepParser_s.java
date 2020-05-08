 /*
  * Copyright 2011 napile
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
 
 package org.napile.cpp4idea.lang.parser;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.cpp4idea.lang.lexer.CTokenType;
import org.napile.cpp4idea.lang.parser.firstparsing.sharpkeyword.SharpDefineKeyword;
import org.napile.cpp4idea.lang.parser.firstparsing.sharpkeyword.SharpIfdefKeyword;
import org.napile.cpp4idea.lang.parser.firstparsing.sharpkeyword.SharpIncludeKeyword;
 import com.intellij.lang.ASTNode;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.lang.PsiParser;
 import com.intellij.psi.tree.IElementType;
 
 /**
  * @author VISTALL
  * @date 2:19/10.12.2011
  *
  * 1 Step parsing - parsing all symbols to stacks, #ifdef/#ifndef - is parsed to it as CPsi*
  */
 public class CFirstStepParser implements PsiParser, CTokenType
 {
 	@NotNull
 	@Override
 	public ASTNode parse(IElementType root, PsiBuilder builder)
 	{
 		builder.setDebugMode(true);
 
 		PsiBuilder.Marker rootMarker = builder.mark();
 
 		while(!builder.eof())
 		{
 			if(builder.getTokenType() == S_INCLUDE_KEYWORD)
 				SharpIncludeKeyword.parse(builder);
 			else if(builder.getTokenType() == S_DEFINE_KEYWORD)
 				SharpDefineKeyword.parse(builder);
 			else if(builder.getTokenType() == S_IFNDEF_KEYWORD || builder.getTokenType() == S_IFDEF_KEYWORD)
 				SharpIfdefKeyword.parseIf(builder);
 			else 
 				builder.advanceLexer();
 		}
 
 		rootMarker.done(root);
 
 		return builder.getTreeBuilt();
 	}
 }
