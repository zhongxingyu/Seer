 /*
  * Copyright 2010-2012 JetBrains s.r.o.
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
 
 /*
  * @author max
  */
 package org.napile.compiler.lang.parsing;
 
 import static org.napile.compiler.NapileNodeTypes.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jetbrains.annotations.Nullable;
 import org.napile.compiler.NapileNodeType;
 import org.napile.compiler.lexer.NapileKeywordToken;
 import org.napile.compiler.lexer.JetTokens;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author max
  * @author abreslav
  */
 public class JetParsing extends AbstractJetParsing
 {
 	// TODO: token sets to constants, including derived methods
 	public static final Map<String, IElementType> MODIFIER_KEYWORD_MAP = new HashMap<String, IElementType>();
 
 	static
 	{
 		for(IElementType softKeyword : JetTokens.MODIFIER_KEYWORDS.getTypes())
 		{
 			MODIFIER_KEYWORD_MAP.put(((NapileKeywordToken) softKeyword).getValue(), softKeyword);
 		}
 	}
 
	private static final TokenSet CLASS_KEYWORDS = TokenSet.create(JetTokens.CLASS_KEYWORD, JetTokens.ENUM_KEYWORD, JetTokens.RETELL_KEYWORD);
 	private static final TokenSet ENUM_MEMBER_FIRST = TokenSet.create(JetTokens.CLASS_KEYWORD, JetTokens.METH_KEYWORD, JetTokens.VAL_KEYWORD, JetTokens.IDENTIFIER);
 
 	private static final TokenSet CLASS_NAME_RECOVERY_SET = TokenSet.orSet(TokenSet.create(JetTokens.LT, JetTokens.LPAR, JetTokens.COLON, JetTokens.LBRACE), CLASS_KEYWORDS);
 	private static final TokenSet TYPE_PARAMETER_GT_RECOVERY_SET = TokenSet.create(JetTokens.WHERE_KEYWORD, JetTokens.LPAR, JetTokens.COLON, JetTokens.LBRACE, JetTokens.GT);
 	private static final TokenSet PARAMETER_NAME_RECOVERY_SET = TokenSet.create(JetTokens.COLON, JetTokens.EQ, JetTokens.COMMA, JetTokens.RPAR);
 	private static final TokenSet NAMESPACE_NAME_RECOVERY_SET = TokenSet.create(JetTokens.DOT, JetTokens.EOL_OR_SEMICOLON);
 	/*package*/ static final TokenSet TYPE_REF_FIRST = TokenSet.create(JetTokens.LBRACKET, JetTokens.IDENTIFIER, JetTokens.METH_KEYWORD, JetTokens.LPAR, JetTokens.THIS_KEYWORD, JetTokens.HASH);
 	private static final TokenSet RECEIVER_TYPE_TERMINATORS = TokenSet.create(JetTokens.DOT, JetTokens.SAFE_ACCESS);
 
 	static JetParsing createForTopLevel(SemanticWhitespaceAwarePsiBuilder builder)
 	{
 		JetParsing jetParsing = new JetParsing(builder);
 		jetParsing.myExpressionParsing = new JetExpressionParsing(builder, jetParsing);
 		return jetParsing;
 	}
 
 	private static JetParsing createForByClause(final SemanticWhitespaceAwarePsiBuilder builder)
 	{
 		final SemanticWhitespaceAwarePsiBuilderForByClause builderForByClause = new SemanticWhitespaceAwarePsiBuilderForByClause(builder);
 		JetParsing jetParsing = new JetParsing(builderForByClause);
 		jetParsing.myExpressionParsing = new JetExpressionParsing(builderForByClause, jetParsing)
 		{
 			@Override
 			protected boolean parseCallWithClosure()
 			{
 				if(builderForByClause.getStackSize() > 0)
 				{
 					return super.parseCallWithClosure();
 				}
 				return false;
 			}
 
 			@Override
 			protected JetParsing create(SemanticWhitespaceAwarePsiBuilder builder)
 			{
 				return createForByClause(builder);
 			}
 		};
 		return jetParsing;
 	}
 
 	private JetExpressionParsing myExpressionParsing;
 
 	private JetParsing(SemanticWhitespaceAwarePsiBuilder builder)
 	{
 		super(builder);
 	}
 
 	/*
 		 * [start] jetlFile
 		 *   : preamble toplevelObject[| import]* [eof]
 		 *   ;
 		 */
 	void parseFile()
 	{
 		PsiBuilder.Marker fileMarker = mark();
 
 		parsePreamble();
 
 		parseToplevelDeclarations(false);
 
 		fileMarker.done(JET_FILE);
 	}
 
 
 	/*
 		 * toplevelObject[| import]*
 		 */
 	private void parseToplevelDeclarations(boolean insideBlock)
 	{
 		while(!eof() && (!insideBlock || !at(JetTokens.RBRACE)))
 		{
 			if(at(JetTokens.IMPORT_KEYWORD))
 			{
 				parseImportDirective();
 			}
 			else
 			{
 				parseClassLike();
 			}
 		}
 	}
 
 	/*
 		 *preamble
 		 *  : namespaceHeader? import*
 		 *  ;
 		 */
 	private void parsePreamble()
 	{		/*
 		 * namespaceHeader
          *   : modifiers "namespace" SimpleName{"."} SEMI?
          *   ;
          */
 		PsiBuilder.Marker namespaceHeader = mark();
 		PsiBuilder.Marker firstEntry = mark();
 		parseModifierList(MODIFIER_LIST, true);
 
 		if(at(JetTokens.PACKAGE_KEYWORD))
 		{
 			advance(); // PACKAGE_KEYWORD
 
 
 			parseNamespaceName();
 
 			if(at(JetTokens.LBRACE))
 			{
 				// Because it's blocked namespace and it will be parsed as one of top level objects
 				firstEntry.rollbackTo();
 				namespaceHeader.done(NAMESPACE_HEADER);
 				return;
 			}
 
 			firstEntry.drop();
 
 			consumeIf(JetTokens.SEMICOLON);
 		}
 		else
 		{
 			firstEntry.rollbackTo();
 		}
 		namespaceHeader.done(NAMESPACE_HEADER);
 
 		parseImportDirectives();
 	}
 
 	/* SimpleName{"."} */
 	private void parseNamespaceName()
 	{
 		while(true)
 		{
 			if(myBuilder.newlineBeforeCurrentToken())
 			{
 				errorWithRecovery("Package name must be a '.'-separated identifier list placed on a single line", NAMESPACE_NAME_RECOVERY_SET);
 				break;
 			}
 
 			PsiBuilder.Marker nsName = mark();
 			if(expect(JetTokens.IDENTIFIER, "Package name must be a '.'-separated identifier list", NAMESPACE_NAME_RECOVERY_SET))
 			{
 				nsName.done(REFERENCE_EXPRESSION);
 			}
 			else
 			{
 				nsName.drop();
 			}
 
 			if(at(JetTokens.DOT))
 			{
 				advance(); // DOT
 			}
 			else
 			{
 				break;
 			}
 		}
 	}
 
 	/*
 		 * import
 		 *   : "import" ("namespace" ".")? SimpleName{"."} ("." "*" | "as" SimpleName)? SEMI?
 		 *   ;
 		 */
 	private void parseImportDirective()
 	{
 		assert _at(JetTokens.IMPORT_KEYWORD);
 		PsiBuilder.Marker importDirective = mark();
 		advance(); // IMPORT_KEYWORD
 
 		PsiBuilder.Marker qualifiedName = mark();
 		if(at(JetTokens.PACKAGE_KEYWORD))
 		{
 			advance(); // PACKAGE_KEYWORD
 			expect(JetTokens.DOT, "Expecting '.'", TokenSet.create(JetTokens.IDENTIFIER, JetTokens.MUL, JetTokens.SEMICOLON));
 		}
 
 		PsiBuilder.Marker reference = mark();
 		expect(JetTokens.IDENTIFIER, "Expecting qualified name");
 		reference.done(REFERENCE_EXPRESSION);
 		while(at(JetTokens.DOT) && lookahead(1) != JetTokens.MUL)
 		{
 			advance(); // DOT
 
 			reference = mark();
 			if(expect(JetTokens.IDENTIFIER, "Qualified name must be a '.'-separated identifier list", TokenSet.create(JetTokens.AS_KEYWORD, JetTokens.DOT, JetTokens.SEMICOLON)))
 			{
 				reference.done(REFERENCE_EXPRESSION);
 			}
 			else
 			{
 				reference.drop();
 			}
 
 			PsiBuilder.Marker precede = qualifiedName.precede();
 			qualifiedName.done(DOT_QUALIFIED_EXPRESSION);
 			qualifiedName = precede;
 		}
 		qualifiedName.drop();
 
 		if(at(JetTokens.DOT))
 		{
 			advance(); // DOT
 			assert _at(JetTokens.MUL);
 			advance(); // MUL
 			handleUselessRename();
 		}
 		if(at(JetTokens.AS_KEYWORD))
 		{
 			advance(); // AS_KEYWORD
 			expect(JetTokens.IDENTIFIER, "Expecting identifier", TokenSet.create(JetTokens.SEMICOLON));
 		}
 		consumeIf(JetTokens.SEMICOLON);
 		importDirective.done(IMPORT_DIRECTIVE);
 	}
 
 	private void parseImportDirectives()
 	{
 		// TODO: Duplicate with parsing imports in parseToplevelDeclarations
 		while(at(JetTokens.IMPORT_KEYWORD))
 		{
 			parseImportDirective();
 		}
 	}
 
 	private void handleUselessRename()
 	{
 		if(at(JetTokens.AS_KEYWORD))
 		{
 			PsiBuilder.Marker as = mark();
 			advance(); // AS_KEYWORD
 			consumeIf(JetTokens.IDENTIFIER);
 			as.error("Cannot rename a all imported items to one identifier");
 		}
 	}
 
 	/*
 		 * toplevelObject
 		 *   : namespace
 		 *   : class
 		 *   : extension
 		 *   : function
 		 *   : property
 		 *   : typedef
 		 *   : object
 		 *   ;
 		 */
 	private void parseClassLike()
 	{
 		PsiBuilder.Marker decl = mark();
 
 		parseModifierList(MODIFIER_LIST);
 
 		IElementType keywordToken = tt();
 		IElementType declType = null;
 
 		if(CLASS_KEYWORDS.contains(keywordToken))
 			declType = parseClass();
 
 		if(declType == null)
 		{
 			errorAndAdvance("Expecting package directive or top level declaration");
 			decl.drop();
 		}
 		else
 		{
 			decl.done(declType);
 		}
 	}
 
 	/*
 		 * (modifier | attribute)*
 		 */
 	boolean parseModifierList(NapileNodeType nodeType, boolean allowShortAnnotations)
 	{
 		return parseModifierList(nodeType);
 	}
 
 	/**
 	 * (modifier | attribute)*
 	 * <p/>
 	 * Feeds modifiers (not attributes) into the passed consumer, if it is not null
 	 */
 	boolean parseModifierList(NapileNodeType nodeType)
 	{
 		PsiBuilder.Marker list = mark();
 		boolean empty = true;
 		while(!eof())
 		{
 			if(atSet(JetTokens.MODIFIER_KEYWORDS))
 			{
 				advance(); // MODIFIER
 			}
 			else if(at(JetTokens.LBRACKET))
 			{
 				parseAnnotation();
 			}
 			else
 			{
 				break;
 			}
 			empty = false;
 		}
 		if(empty)
 		{
 			list.drop();
 		}
 		else
 		{
 			list.done(nodeType);
 		}
 		return !empty;
 	}
 
 	/*
 		 * annotations
 		 *   : annotation*
 		 *   ;
 		 */
 	void parseAnnotations()
 	{
 		while(true)
 		{
 			if(!(parseAnnotation()))
 				break;
 		}
 	}
 
 	/*
 		 * annotation
 		 *   : "[" annotationEntry+ "]"
 		 *   : annotationEntry
 		 *   ;
 		 */
 	private boolean parseAnnotation()
 	{
 		if(at(JetTokens.LBRACKET))
 		{
 			PsiBuilder.Marker annotation = mark();
 
 			myBuilder.disableNewlines();
 			advance(); // LBRACKET
 
 			if(!at(JetTokens.IDENTIFIER))
 			{
 				error("Expecting a list of attributes");
 			}
 			else
 			{
 				parseAnnotationEntry();
 				while(at(JetTokens.COMMA))
 				{
 					errorAndAdvance("No commas needed to separate attributes");
 				}
 
 				while(at(JetTokens.IDENTIFIER))
 				{
 					parseAnnotationEntry();
 					while(at(JetTokens.COMMA))
 					{
 						errorAndAdvance("No commas needed to separate attributes");
 					}
 				}
 			}
 
 			expect(JetTokens.RBRACKET, "Expecting ']' to close an attribute annotation");
 			myBuilder.restoreNewlinesState();
 
 			annotation.done(ANNOTATION_LIST);
 			return true;
 		}
 		return false;
 	}
 
 	/*
 		 * annotationEntry
 		 *   : SimpleName{"."} typeArguments? valueArguments?
 		 *   ;
 		 */
 	private void parseAnnotationEntry()
 	{
 		assert _at(JetTokens.IDENTIFIER);
 
 		PsiBuilder.Marker attribute = mark();
 
 		PsiBuilder.Marker reference = mark();
 		PsiBuilder.Marker typeReference = mark();
 		parseUserType();
 		typeReference.done(TYPE_REFERENCE);
 		reference.done(CONSTRUCTOR_CALLEE);
 
 		parseTypeArgumentList();
 
 		if(at(JetTokens.LPAR))
 		{
 			myExpressionParsing.parseValueArgumentList();
 		}
 		attribute.done(ANNOTATION_ENTRY);
 	}
 
 	/*
 		 * class
 		 *   : modifiers "class" SimpleName
 		 *       typeParameters?
 		 *         modifiers ("(" primaryConstructorParameter{","} ")")?
 		 *       (":" attributes delegationSpecifier{","})?
 		 *       typeConstraints
 		 *       (classBody? | enumClassBody)
 		 *   ;
 		 */
 	IElementType parseClass()
 	{
 		IElementType lastKeyword = tt();
 
 		advance(); // CLASS_KEYWORD
 
 		if(!parseIdeTemplate())
 		{
 			expect(JetTokens.IDENTIFIER, "Class name expected", CLASS_NAME_RECOVERY_SET);
 		}
 		boolean typeParametersDeclared = parseTypeParameterList(TYPE_PARAMETER_GT_RECOVERY_SET);
 
 		PsiBuilder.Marker beforeConstructorModifiers = mark();
 
 		// We are still inside a class declaration
 		beforeConstructorModifiers.drop();
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			parseDelegationSpecifierList();
 		}
 
 		parseTypeConstraintsGuarded(typeParametersDeclared);
 
 		if(at(JetTokens.LBRACE))
 		{
 			if(lastKeyword == JetTokens.ENUM_KEYWORD)
 				parseEnumBody();
 			else if(lastKeyword == JetTokens.RETELL_KEYWORD)
 				parseRetellClassBody();
 			else
 				parseClassBody();
 		}
 
 		return CLASS;
 	}
 
 	/*
 		 * enumClassBody
 		 *   : "{" enumEntry* "}"
 		 *   ;
 		 */
 	private void parseEnumBody()
 	{
 		if(!at(JetTokens.LBRACE))
 			return;
 
 		PsiBuilder.Marker classBody = mark();
 
 		myBuilder.enableNewlines();
 		advance(); // LBRACE
 
 		if(!parseIdeTemplate())
 		{
 			while(!eof() && !at(JetTokens.RBRACE))
 			{
 				PsiBuilder.Marker entryOrMember = mark();
 
 				TokenSet constructorNameFollow = TokenSet.create(JetTokens.SEMICOLON, JetTokens.COLON, JetTokens.LPAR, JetTokens.LT, JetTokens.LBRACE);
 				int lastId = findLastBefore(ENUM_MEMBER_FIRST, constructorNameFollow, false);
 
 				createTruncatedBuilder(lastId).parseModifierList(MODIFIER_LIST);
 
 				IElementType type;
 				if(at(JetTokens.IDENTIFIER))
 				{
 					parseEnumEntry();
 					type = ENUM_ENTRY;
 				}
 				else
 				{
 					type = parseMemberDeclarationRest();
 				}
 
 				if(type == null)
 				{
 					errorAndAdvance("Expecting an enum entry or member declaration");
 					entryOrMember.drop();
 				}
 				else
 				{
 					entryOrMember.done(type);
 				}
 			}
 		}
 
 		expect(JetTokens.RBRACE, "Expecting '}' to close enum body");
 		myBuilder.restoreNewlinesState();
 
 		classBody.done(CLASS_BODY);
 	}
 
 	/*
 		 * enumEntry
 		 *   : modifiers typeParameters? valueArguments? typeConstraints classBody?
 		 *   ;
 		 */
 	private void parseEnumEntry()
 	{
 		assert _at(JetTokens.IDENTIFIER);
 
 		advance();
 
 		parseTypeArgumentList();
 		if(at(JetTokens.LPAR))
 		{
 			PsiBuilder.Marker callExpression = mark();
 
 			myExpressionParsing.parseValueArgumentList();
 
 			callExpression.done(CONSTRUCTOR_CALLEE);
 		}
 
 		if(at(JetTokens.LBRACE))
 			parseClassBody();
 
 		consumeIf(JetTokens.SEMICOLON);
 	}
 
 	private void parseRetellClassBody()
 	{
 		if(!at(JetTokens.LBRACE))
 			return;
 
 		PsiBuilder.Marker classBody = mark();
 
 		myBuilder.enableNewlines();
 		advance(); // LBRACE
 
 		if(!parseIdeTemplate())
 		{
 			while(!eof() && !at(JetTokens.RBRACE))
 			{
 				PsiBuilder.Marker entryMarker = mark();
 
 				if(at(JetTokens.IDENTIFIER))
 				{
 					parseRetellEntry();
 					entryMarker.done(RETELL_ENTRY);
 				}
 				else
 				{
 					errorAndAdvance("Expecting identifier");
 					entryMarker.drop();
 				}
 			}
 		}
 
 		expect(JetTokens.RBRACE, "Expecting '}' to close retell body");
 		myBuilder.restoreNewlinesState();
 
 		classBody.done(CLASS_BODY);
 	}
 
 	private void parseRetellEntry()
 	{
 		assert _at(JetTokens.IDENTIFIER);
 
 		advance();
 
 		if(at(JetTokens.EQ))
 		{
 			advance();
 
 			myExpressionParsing.parseExpression();
 		}
 		else
 			error("'=' Expected");
 
 		consumeIf(JetTokens.SEMICOLON);
 	}
 
 	/*
 		 * classBody
 		 *   : ("{" memberDeclaration "}")?
 		 *   ;
 		 */	/*package*/ void parseClassBody()
 	{
 		PsiBuilder.Marker body = mark();
 
 		myBuilder.enableNewlines();
 		expect(JetTokens.LBRACE, "Expecting a class body", TokenSet.create(JetTokens.LBRACE));
 
 		if(!parseIdeTemplate())
 		{
 			while(!eof())
 			{
 				if(at(JetTokens.RBRACE))
 				{
 					break;
 				}
 				parseMemberDeclaration();
 			}
 		}
 		expect(JetTokens.RBRACE, "Missing '}");
 		myBuilder.restoreNewlinesState();
 
 		body.done(CLASS_BODY);
 	}
 
 	/*
 		 * memberDeclaration
 		 *   : modifiers memberDeclaration'
 		 *   ;
 		 *
 		 * memberDeclaration'
 		 *   : classObject
 		 *   : constructor
 		 *   : function
 		 *   : property
 		 *   : class
 		 *   : extension
 		 *   : typedef
 		 *   : anonymousInitializer
 		 *   : object
 		 *   ;
 		 */
 	private void parseMemberDeclaration()
 	{
 		PsiBuilder.Marker decl = mark();
 
 		parseModifierList(MODIFIER_LIST);
 
 		IElementType declType = parseMemberDeclarationRest();
 
 		if(declType == null)
 		{
 			errorWithRecovery("Expecting member declaration", TokenSet.create(JetTokens.RBRACE));
 			decl.drop();
 		}
 		else
 		{
 			decl.done(declType);
 		}
 	}
 
 	private IElementType parseMemberDeclarationRest()
 	{
 		IElementType keywordToken = tt();
 		IElementType declType = null;
 		if(CLASS_KEYWORDS.contains(keywordToken))
 			declType = parseClass();
 		else if(keywordToken == JetTokens.METH_KEYWORD)
 			declType = parseMethod();
 		else if(keywordToken == JetTokens.THIS_KEYWORD)
 			declType = parseConstructor();
 		else if(keywordToken == JetTokens.VAL_KEYWORD || keywordToken == JetTokens.VAR_KEYWORD)
 			declType = parseProperty();
 
 		return declType;
 	}
 
 	/*
 		 * object
 		 *   : "object" SimpleName? ":" delegationSpecifier{","}? classBody?
 		 *   ;
 		 */
 	void parseObject()
 	{
 		assert _at(JetTokens.ANONYM_KEYWORD);
 
 		advance(); // OBJECT_KEYWORD
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			parseDelegationSpecifierList();
 		}
 
 		if(at(JetTokens.LBRACE))
 			parseClassBody();
 	}
 
 	/*
 		 * initializer{","}
 		 */
 	private void parseInitializerList()
 	{
 		PsiBuilder.Marker list = mark();
 		while(true)
 		{
 			if(at(JetTokens.COMMA))
 				errorAndAdvance("Expecting a this or super constructor call");
 			parseInitializer();
 			if(!at(JetTokens.COMMA))
 				break;
 			advance(); // COMMA
 		}
 		list.done(INITIALIZER_LIST);
 	}
 
 	/*
 		 * initializer
 		 *   : attributes "this" valueArguments
 		 *   : attributes constructorInvocation // type parameters may (must?) be omitted
 		 *   ;
 		 */
 	private void parseInitializer()
 	{
 		PsiBuilder.Marker initializer = mark();
 		parseAnnotations();
 
 		IElementType type;
 		if(at(JetTokens.THIS_KEYWORD))
 		{
 			PsiBuilder.Marker mark = mark();
 			advance(); // THIS_KEYWORD
 			mark.done(THIS_CONSTRUCTOR_REFERENCE);
 			type = THIS_CALL;
 		}
 		else if(atSet(TYPE_REF_FIRST))
 		{
 			PsiBuilder.Marker reference = mark();
 			parseTypeRef();
 			reference.done(CONSTRUCTOR_CALLEE);
 			type = DELEGATOR_SUPER_CALL;
 		}
 		else
 		{
 			errorWithRecovery("Expecting constructor call (this(...)) or supertype initializer", TokenSet.create(JetTokens.LBRACE, JetTokens.COMMA));
 			initializer.drop();
 			return;
 		}
 		myExpressionParsing.parseValueArgumentList();
 
 		initializer.done(type);
 	}
 
 	/*
 		 * property
 		 *   : modifiers ("val" | "var")
 		 *       typeParameters? (type "." | attributes)?
 		 *       SimpleName (":" type)?
 		 *       typeConstraints
 		 *       ("=" element SEMI?)?
 		 *       (getter? setter? | setter? getter?) SEMI?
 		 *   ;
 		 */
 	private IElementType parseProperty()
 	{
 		return parseProperty(false);
 	}
 
 	IElementType parseProperty(boolean local)
 	{
 		if(at(JetTokens.VAL_KEYWORD) || at(JetTokens.VAR_KEYWORD))
 		{
 			advance(); // VAL_KEYWORD or VAR_KEYWORD
 		}
 		else
 		{
 			errorAndAdvance("Expecting 'val' or 'var'");
 		}
 
 		boolean typeParametersDeclared = at(JetTokens.LT) && parseTypeParameterList(TokenSet.create(JetTokens.IDENTIFIER, JetTokens.EQ, JetTokens.COLON, JetTokens.SEMICOLON));
 
 		TokenSet propertyNameFollow = TokenSet.create(JetTokens.COLON, JetTokens.EQ, JetTokens.LBRACE, JetTokens.RBRACE, JetTokens.SEMICOLON, JetTokens.VAL_KEYWORD, JetTokens.VAR_KEYWORD, JetTokens.METH_KEYWORD, JetTokens.CLASS_KEYWORD);
 
 		myBuilder.disableJoiningComplexTokens();
 
 		// TODO: extract constant
 		int lastDot = matchTokenStreamPredicate(new LastBefore(new AtSet(JetTokens.DOT, JetTokens.SAFE_ACCESS), new AbstractTokenStreamPredicate()
 		{
 			@Override
 			public boolean matching(boolean topLevel)
 			{
 				if(topLevel && (at(JetTokens.EQ) || at(JetTokens.COLON)))
 					return true;
 				if(topLevel && at(JetTokens.IDENTIFIER))
 				{
 					IElementType lookahead = lookahead(1);
 					return lookahead != JetTokens.LT && lookahead != JetTokens.DOT && lookahead != JetTokens.SAFE_ACCESS && lookahead != JetTokens.QUEST;
 				}
 				return false;
 			}
 		}));
 
 		parseReceiverType("property", propertyNameFollow, lastDot);
 
 		myBuilder.restoreJoiningComplexTokensState();
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			if(!parseIdeTemplate())
 			{
 				parseTypeRef();
 			}
 		}
 
 		parseTypeConstraintsGuarded(typeParametersDeclared);
 
 		if(local)
 		{
 			if(at(JetTokens.EQ))
 			{
 				advance(); // EQ
 				myExpressionParsing.parseExpression();
 				// "val a = 1; b" must not be an infix call of b on "val ...;"
 			}
 		}
 		else
 		{
 			if(at(JetTokens.EQ))
 			{
 				advance(); // EQ
 				myExpressionParsing.parseExpression();
 				consumeIf(JetTokens.SEMICOLON);
 			}
 
 			if(parsePropertyGetterOrSetter())
 			{
 				parsePropertyGetterOrSetter();
 			}
 			if(!atSet(JetTokens.EOL_OR_SEMICOLON, JetTokens.RBRACE))
 			{
 				if(getLastToken() != JetTokens.SEMICOLON)
 				{
 					errorUntil("Property getter or setter expected", TokenSet.create(JetTokens.EOL_OR_SEMICOLON));
 				}
 			}
 			else
 			{
 				consumeIf(JetTokens.SEMICOLON);
 			}
 		}
 
 
 		return PROPERTY;
 	}
 
 	/*
 		 * getterOrSetter
 		 *   : modifiers ("get" | "set")
 		 *   :
 		 *        (     "get" "(" ")"
 		 *           |
 		 *              "set" "(" modifiers parameter ")"
 		 *        ) functionBody
 		 *   ;
 		 */
 	private boolean parsePropertyGetterOrSetter()
 	{
 		PsiBuilder.Marker getterOrSetter = mark();
 
 		parseModifierList(MODIFIER_LIST, false);
 
 		if(!at(JetTokens.GET_KEYWORD) && !at(JetTokens.SET_KEYWORD))
 		{
 			getterOrSetter.rollbackTo();
 			return false;
 		}
 
 		boolean setter = at(JetTokens.SET_KEYWORD);
 		advance(); // GET_KEYWORD or SET_KEYWORD
 
 		if(!at(JetTokens.LPAR))
 		{
 			// Account for Jet-114 (val a : int get {...})
 			TokenSet ACCESSOR_FIRST_OR_PROPERTY_END = TokenSet.orSet(JetTokens.MODIFIER_KEYWORDS, TokenSet.create(JetTokens.LBRACKET, JetTokens.GET_KEYWORD, JetTokens.SET_KEYWORD, JetTokens.EOL_OR_SEMICOLON, JetTokens.RBRACE));
 			if(!atSet(ACCESSOR_FIRST_OR_PROPERTY_END))
 			{
 				errorUntil("Accessor body expected", TokenSet.orSet(ACCESSOR_FIRST_OR_PROPERTY_END, TokenSet.create(JetTokens.LBRACE, JetTokens.LPAR, JetTokens.EQ)));
 			}
 			else
 			{
 				getterOrSetter.done(PROPERTY_ACCESSOR);
 				return true;
 			}
 		}
 
 		myBuilder.disableNewlines();
 		expect(JetTokens.LPAR, "Expecting '('", TokenSet.create(JetTokens.RPAR, JetTokens.IDENTIFIER, JetTokens.COLON, JetTokens.LBRACE, JetTokens.EQ));
 		if(setter)
 		{
 			PsiBuilder.Marker parameterList = mark();
 			PsiBuilder.Marker setterParameter = mark();
 			parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(JetTokens.IDENTIFIER), TokenSet.create(JetTokens.RPAR, JetTokens.COMMA, JetTokens.COLON));
 			expect(JetTokens.IDENTIFIER, "Expecting parameter name", TokenSet.create(JetTokens.RPAR, JetTokens.COLON, JetTokens.LBRACE, JetTokens.EQ));
 
 			if(at(JetTokens.COLON))
 			{
 				advance();
 
 				parseTypeRef();
 			}
 			setterParameter.done(VALUE_PARAMETER);
 			parameterList.done(VALUE_PARAMETER_LIST);
 		}
 		if(!at(JetTokens.RPAR))
 			errorUntil("Expecting ')'", TokenSet.create(JetTokens.RPAR, JetTokens.COLON, JetTokens.LBRACE, JetTokens.EQ, JetTokens.EOL_OR_SEMICOLON));
 		expect(JetTokens.RPAR, "Expecting ')'", TokenSet.create(JetTokens.RPAR, JetTokens.COLON, JetTokens.LBRACE, JetTokens.EQ));
 		myBuilder.restoreNewlinesState();
 
 		if(at(JetTokens.COLON))
 		{
 			advance();
 
 			parseTypeRef();
 		}
 
 		parseFunctionBody();
 
 		getterOrSetter.done(PROPERTY_ACCESSOR);
 
 		return true;
 	}
 
 	/*
 		 * function
 		 *   : modifiers "meth" typeParameters?
 		 *       (type "." | attributes)?
 		 *       SimpleName
 		 *       typeParameters? functionParameters (":" type)?
 		 *       typeConstraints
 		 *       functionBody?
 		 *   ;
 		 */
 	IElementType parseMethod()
 	{
 		assert _at(JetTokens.METH_KEYWORD);
 
 		advance(); // FUN_METH_KEYWORD
 
 		// Recovery for the case of class A { fun| }
 		if(at(JetTokens.RBRACE))
 		{
 			error("Function body expected");
 			return METHOD;
 		}
 
 		boolean typeParameterListOccurred = false;
 		if(at(JetTokens.LT))
 		{
 			parseTypeParameterList(TokenSet.create(JetTokens.LBRACKET, JetTokens.LBRACE, JetTokens.LPAR));
 			typeParameterListOccurred = true;
 		}
 
 		myBuilder.disableJoiningComplexTokens();
 		int lastDot = findLastBefore(RECEIVER_TYPE_TERMINATORS, TokenSet.create(JetTokens.LPAR), true);
 		parseReceiverType("function", TokenSet.create(JetTokens.LT, JetTokens.LPAR, JetTokens.COLON, JetTokens.EQ), lastDot);
 		myBuilder.restoreJoiningComplexTokensState();
 
 		TokenSet valueParametersFollow = TokenSet.create(JetTokens.COLON, JetTokens.EQ, JetTokens.LBRACE, JetTokens.SEMICOLON, JetTokens.RPAR);
 
 		if(at(JetTokens.LT))
 		{
 			PsiBuilder.Marker error = mark();
 			parseTypeParameterList(TokenSet.orSet(TokenSet.create(JetTokens.LPAR), valueParametersFollow));
 			if(typeParameterListOccurred)
 			{
 				error.error("Only one type parameter list is allowed for a function"); // TODO : discuss
 			}
 			else
 			{
 				error.drop();
 			}
 			typeParameterListOccurred = true;
 		}
 
 		parseValueParameterList(false, valueParametersFollow);
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 
 			if(!parseIdeTemplate())
 			{
 				parseTypeRef();
 			}
 		}
 
 		parseTypeConstraintsGuarded(typeParameterListOccurred);
 
 		if(at(JetTokens.SEMICOLON))
 		{
 			advance(); // SEMICOLON
 		}
 		else if(at(JetTokens.EQ) || at(JetTokens.LBRACE))
 		{
 			parseFunctionBody();
 		}
 
 		return METHOD;
 	}
 
 	/*
 		 * constructor
 		 *   : modifiers "this" functionParameters (":" initializer{","}) block?
 		 *   ;
 		 */
 	private NapileNodeType parseConstructor()
 	{
 		assert _at(JetTokens.THIS_KEYWORD);
 
 		advance(); // THIS_KEYWORD
 
 		parseValueParameterList(false, TokenSet.create(JetTokens.COLON, JetTokens.LBRACE, JetTokens.SEMICOLON));
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 
 			parseDelegationSpecifierList();
 			//parseInitializerList();
 		}
 
 		if(at(JetTokens.LBRACE))
 		{
 			parseBlock();
 		}
 		else
 		{
 			consumeIf(JetTokens.SEMICOLON);
 		}
 
 		return CONSTRUCTOR;
 	}
 
 
 	/*
 		 * :
 		 *   (type "." | attributes)?
 		 */
 	private void parseReceiverType(String title, TokenSet nameFollow, int lastDot)
 	{
 		if(lastDot == -1)
 		{ // There's no explicit receiver type specified
 			parseAnnotations();
 
 			if(!parseIdeTemplate())
 			{
 				expect(JetTokens.IDENTIFIER, "Expecting " + title + " name or receiver type", nameFollow);
 			}
 		}
 		else
 		{
 			if(parseIdeTemplate())
 			{
 				expect(JetTokens.DOT, "Expecting '.' after receiver template");
 			}
 			else
 			{
 				createTruncatedBuilder(lastDot).parseTypeRef();
 
 				if(atSet(RECEIVER_TYPE_TERMINATORS))
 				{
 					advance(); // expectation
 				}
 				else
 				{
 					errorWithRecovery("Expecting '.' before a " + title + " name", nameFollow);
 				}
 			}
 
 			if(!parseIdeTemplate())
 			{
 				expect(JetTokens.IDENTIFIER, "Expecting " + title + " name", nameFollow);
 			}
 		}
 	}
 
 	/*
 		 * functionBody
 		 *   : block
 		 *   : "=" element
 		 *   ;
 		 */
 	private void parseFunctionBody()
 	{
 		if(at(JetTokens.LBRACE))
 		{
 			parseBlock();
 		}
 		else if(at(JetTokens.EQ))
 		{
 			advance(); // EQ
 			myExpressionParsing.parseExpression();
 			consumeIf(JetTokens.SEMICOLON);
 		}
 		else
 		{
 			errorAndAdvance("Expecting function body");
 		}
 	}
 
 	/*
 		 * block
 		 *   : "{" (expressions)* "}"
 		 *   ;
 		 */
 	void parseBlock()
 	{
 		PsiBuilder.Marker block = mark();
 
 		myBuilder.enableNewlines();
 		expect(JetTokens.LBRACE, "Expecting '{' to open a block");
 
 		myExpressionParsing.parseStatements();
 
 		expect(JetTokens.RBRACE, "Expecting '}");
 		myBuilder.restoreNewlinesState();
 
 		block.done(BLOCK);
 	}
 
 	/*
 		 * delegationSpecifier{","}
 		 */
 	void parseDelegationSpecifierList()
 	{
 		PsiBuilder.Marker list = mark();
 
 		while(true)
 		{
 			if(at(JetTokens.COMMA))
 			{
 				errorAndAdvance("Expecting a delegation specifier");
 				continue;
 			}
 			parseDelegationSpecifier();
 			if(!at(JetTokens.COMMA))
 				break;
 			advance(); // COMMA
 		}
 
 		list.done(DELEGATION_SPECIFIER_LIST);
 	}
 
 	/*
 		 * attributes delegationSpecifier
 		 *
 		 * delegationSpecifier
 		 *   : constructorInvocation // type and constructor arguments
 		 *   : userType
 		 *   : explicitDelegation
 		 *   ;
 		 *
 		 * explicitDelegation
 		 *   : userType "by" element
 		 *   ;
 		 */
 	private void parseDelegationSpecifier()
 	{
 		PsiBuilder.Marker delegator = mark();
 		parseAnnotations();
 
 		PsiBuilder.Marker reference = mark();
 		parseTypeRef();
 
 		if(at(JetTokens.LPAR))
 		{
 			reference.done(CONSTRUCTOR_CALLEE);
 			myExpressionParsing.parseValueArgumentList();
 			delegator.done(DELEGATOR_SUPER_CALL);
 		}
 		else
 		{
 			reference.drop();
 			delegator.done(DELEGATOR_SUPER_CLASS);
 		}
 	}
 
 	/*
 		 * typeParameters
 		 *   : ("<" typeParameter{","} ">"
 		 *   ;
 		 */
 	private boolean parseTypeParameterList(TokenSet recoverySet)
 	{
 		PsiBuilder.Marker list = mark();
 		boolean result = false;
 		if(at(JetTokens.LT))
 		{
 
 			myBuilder.disableNewlines();
 			advance(); // LT
 
 			while(true)
 			{
 				if(at(JetTokens.COMMA))
 					errorAndAdvance("Expecting type parameter declaration");
 				parseTypeParameter();
 
 				if(!at(JetTokens.COMMA))
 					break;
 				advance(); // COMMA
 			}
 
 			expect(JetTokens.GT, "Missing '>'", recoverySet);
 			myBuilder.restoreNewlinesState();
 			result = true;
 		}
 		list.done(TYPE_PARAMETER_LIST);
 		return result;
 	}
 
 	/*
 		 * typeConstraints
 		 *   : ("where" typeConstraint{","})?
 		 *   ;
 		 */
 	private void parseTypeConstraintsGuarded(boolean typeParameterListOccurred)
 	{
 		PsiBuilder.Marker error = mark();
 		boolean constraints = parseTypeConstraints();
 		if(constraints && !typeParameterListOccurred)
 		{
 			error.error("Type constraints are not allowed when no type parameters declared");
 		}
 		else
 		{
 			error.drop();
 		}
 	}
 
 	private boolean parseTypeConstraints()
 	{
 		if(at(JetTokens.WHERE_KEYWORD))
 		{
 			parseTypeConstraintList();
 			return true;
 		}
 		return false;
 	}
 
 	/*
 		 * typeConstraint{","}
 		 */
 	private void parseTypeConstraintList()
 	{
 		assert _at(JetTokens.WHERE_KEYWORD);
 
 		advance(); // WHERE_KEYWORD
 
 		PsiBuilder.Marker list = mark();
 
 		while(true)
 		{
 			if(at(JetTokens.COMMA))
 				errorAndAdvance("Type constraint expected");
 			parseTypeConstraint();
 			if(!at(JetTokens.COMMA))
 				break;
 			advance(); // COMMA
 		}
 
 		list.done(TYPE_CONSTRAINT_LIST);
 	}
 
 	/*
 		 * typeConstraint
 		 *   : attributes SimpleName ":" type
 		 *   : attributes "class" "object" SimpleName ":" type
 		 *   ;
 		 */
 	private void parseTypeConstraint()
 	{
 		PsiBuilder.Marker constraint = mark();
 
 		parseAnnotations();
 
 		if(at(JetTokens.CLASS_KEYWORD))
 		{
 			advance(); // CLASS_KEYWORD
 
 			expect(JetTokens.ANONYM_KEYWORD, "Expecting 'object'", TYPE_REF_FIRST);
 		}
 
 		PsiBuilder.Marker reference = mark();
 		if(expect(JetTokens.IDENTIFIER, "Expecting type parameter name", TokenSet.orSet(TokenSet.create(JetTokens.COLON, JetTokens.COMMA), TYPE_REF_FIRST)))
 		{
 			reference.done(REFERENCE_EXPRESSION);
 		}
 		else
 		{
 			reference.drop();
 		}
 
 		expect(JetTokens.COLON, "Expecting ':' before the upper bound", TYPE_REF_FIRST);
 
 		parseTypeRef();
 
 		constraint.done(TYPE_CONSTRAINT);
 	}
 
 	/*
 		 * typeParameter
 		 *   : modifiers SimpleName (":" userType)?
 		 *   ;
 		 */
 	private void parseTypeParameter()
 	{
 		if(atSet(TYPE_PARAMETER_GT_RECOVERY_SET))
 		{
 			error("Type parameter declaration expected");
 			return;
 		}
 
 		PsiBuilder.Marker mark = mark();
 
 		parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(JetTokens.IDENTIFIER), TokenSet.create(JetTokens.COMMA, JetTokens.GT, JetTokens.COLON));
 
 		expect(JetTokens.IDENTIFIER, "Type parameter name expected", TokenSet.EMPTY);
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			parseTypeRef();
 		}
 
 		mark.done(TYPE_PARAMETER);
 	}
 
 	/*
 		 * type
 		 *   : attributes typeDescriptor
 		 *
 		 * typeDescriptor
 		 *   : selfType
 		 *   : functionType
 		 *   : userType
 		 *   : tupleType
 		 *   : nullableType
 		 *   ;
 		 *
 		 * nullableType
 		 *   : typeDescriptor "?"
 		 */
 	void parseTypeRef()
 	{
 		parseTypeRef(TokenSet.EMPTY);
 	}
 
 	void parseTypeRef(TokenSet extraRecoverySet)
 	{
 		PsiBuilder.Marker typeRefMarker = parseTypeRefContents(extraRecoverySet);
 		typeRefMarker.done(TYPE_REFERENCE);
 	}
 
 	// The extraRecoverySet is needed for the foo(bar<x, 1, y>(z)) case, to tell whether we should stop
 	// on expression-indicating symbols or not
 	private PsiBuilder.Marker parseTypeRefContents(TokenSet extraRecoverySet)
 	{
 		// Disabling token merge is required for cases like
 		//    Int?.(Foo) -> Bar
 		// we don't support this case now
 		//        myBuilder.disableJoiningComplexTokens();
 		PsiBuilder.Marker typeRefMarker = mark();
 		parseAnnotations();
 
 		if(at(JetTokens.IDENTIFIER) || at(JetTokens.PACKAGE_KEYWORD))
 		{
 			parseUserType();
 		}
 		else if(at(JetTokens.LPAR))
 		{
 			PsiBuilder.Marker functionOrParenthesizedType = mark();
 
 			// This may be a function parameter list or just a prenthesized type
 			advance(); // LPAR
 			parseTypeRefContents(TokenSet.EMPTY).drop(); // parenthesized types, no reference element around it is needed
 
 			if(at(JetTokens.RPAR))
 			{
 				advance(); // RPAR
 				if(at(JetTokens.ARROW))
 				{
 					// It's a function type with one parameter specified
 					//    (A) -> B
 					functionOrParenthesizedType.rollbackTo();
 					parseFunctionType();
 				}
 				else
 				{
 					// It's a parenthesized type
 					//    (A)
 					functionOrParenthesizedType.drop();
 				}
 			}
 			else
 			{
 				// This must be a function type
 				//   (A, B) -> C
 				// or
 				//   (a : A) -> C
 				functionOrParenthesizedType.rollbackTo();
 				parseFunctionType();
 			}
 		}
 		else if(at(JetTokens.THIS_KEYWORD))
 		{
 			parseSelfType();
 		}
 		else
 		{
 			errorWithRecovery("Type expected", TokenSet.orSet(CLASS_KEYWORDS, TokenSet.create(JetTokens.EQ, JetTokens.COMMA, JetTokens.GT, JetTokens.RBRACKET, JetTokens.DOT, JetTokens.RPAR, JetTokens.RBRACE, JetTokens.LBRACE, JetTokens.SEMICOLON), extraRecoverySet));
 		}
 
 		while(at(JetTokens.QUEST))
 		{
 			PsiBuilder.Marker precede = typeRefMarker.precede();
 
 			advance(); // QUEST
 			typeRefMarker.done(NULLABLE_TYPE);
 
 			typeRefMarker = precede;
 		}
 
 		if(at(JetTokens.DOT))
 		{
 			// This is a receiver for a function type
 			//  A.(B) -> C
 			//   ^
 
 			PsiBuilder.Marker precede = typeRefMarker.precede();
 			typeRefMarker.done(TYPE_REFERENCE);
 
 			advance(); // DOT
 
 			if(at(JetTokens.LPAR))
 			{
 				parseFunctionTypeContents().drop();
 			}
 			else
 			{
 				error("Expecting function type");
 			}
 			typeRefMarker = precede.precede();
 
 			precede.done(FUNCTION_TYPE);
 		}
 		//        myBuilder.restoreJoiningComplexTokensState();
 		return typeRefMarker;
 	}
 
 	/*
 		 * userType
 		 *   : ("namespace" ".")? simpleUserType{"."}
 		 *   ;
 		 */
 	private void parseUserType()
 	{
 		PsiBuilder.Marker userType = mark();
 
 		if(at(JetTokens.PACKAGE_KEYWORD))
 		{
 			advance(); // PACKAGE_KEYWORD
 			expect(JetTokens.DOT, "Expecting '.'", TokenSet.create(JetTokens.IDENTIFIER));
 		}
 
 		PsiBuilder.Marker reference = mark();
 		while(true)
 		{
 			if(expect(JetTokens.IDENTIFIER, "Expecting type name", TokenSet.orSet(JetExpressionParsing.EXPRESSION_FIRST, JetExpressionParsing.EXPRESSION_FOLLOW)))
 			{
 				reference.done(REFERENCE_EXPRESSION);
 			}
 			else
 			{
 				reference.drop();
 				break;
 			}
 
 			parseTypeArgumentList();
 			if(!at(JetTokens.DOT))
 			{
 				break;
 			}
 			if(lookahead(1) == JetTokens.LPAR)
 			{
 				// This may be a receiver for a function type
 				//   Int.(Int) -> Int
 				break;
 			}
 
 			PsiBuilder.Marker precede = userType.precede();
 			userType.done(USER_TYPE);
 			userType = precede;
 
 			advance(); // DOT
 			reference = mark();
 		}
 
 		userType.done(USER_TYPE);
 	}
 
 	/*
 		 * selfType
 		 *   : "This"
 		 *   ;
 		 */
 	private void parseSelfType()
 	{
 		assert _at(JetTokens.THIS_KEYWORD);
 
 		PsiBuilder.Marker type = mark();
 		advance(); // THIS_KEYWORD
 		type.done(SELF_TYPE);
 	}
 
 	/*
 		 *  (optionalProjection type){","}
 		 */
 	private PsiBuilder.Marker parseTypeArgumentList()
 	{
 		if(!at(JetTokens.LT))
 			return null;
 
 		PsiBuilder.Marker list = mark();
 
 		tryParseTypeArgumentList(TokenSet.EMPTY);
 
 		list.done(TYPE_ARGUMENT_LIST);
 		return list;
 	}
 
 	boolean tryParseTypeArgumentList(TokenSet extraRecoverySet)
 	{
 		myBuilder.disableNewlines();
 		advance(); // LT
 
 		while(true)
 		{
 			parseTypeRef(extraRecoverySet);
 
 			if(!at(JetTokens.COMMA))
 				break;
 			advance(); // COMMA
 		}
 
 		boolean atGT = at(JetTokens.GT);
 		if(!atGT)
 		{
 			error("Expecting a '>'");
 		}
 		else
 		{
 			advance(); // GT
 		}
 		myBuilder.restoreNewlinesState();
 		return atGT;
 	}
 
 	private void parseModifierListWithShortAnnotations(NapileNodeType modifierList, TokenSet lookFor, TokenSet stopAt)
 	{
 		int lastId = findLastBefore(lookFor, stopAt, false);
 		createTruncatedBuilder(lastId).parseModifierList(modifierList, true);
 	}
 
 	/*
 		 * functionType
 		 *   : (type ".")? "(" (parameter | modifiers type){","}? ")" "->" type?
 		 *   ;
 		 */
 	private void parseFunctionType()
 	{
 		parseFunctionTypeContents().done(FUNCTION_TYPE);
 	}
 
 	private PsiBuilder.Marker parseFunctionTypeContents()
 	{
 		assert _at(JetTokens.LPAR) : tt();
 		PsiBuilder.Marker functionType = mark();
 
 		//        advance(); // LPAR
 		//
 		//        int lastLPar = findLastBefore(TokenSet.create(LPAR), TokenSet.create(COLON), false);
 		//        if (lastLPar >= 0 && lastLPar > myBuilder.getCurrentOffset()) {
 		//            TODO : -1 is a hack?
 		//            createTruncatedBuilder(lastLPar - 1).parseTypeRef();
 		//            advance(); // DOT
 		//        }
 
 		parseValueParameterList(true, TokenSet.EMPTY);
 
 		//        if (at(COLON)) {
 		//            advance(); // COLON // expect(COLON, "Expecting ':' followed by a return type", TYPE_REF_FIRST);
 
 		expect(JetTokens.ARROW, "Expecting '->' to specify return type of a function type", TYPE_REF_FIRST);
 		parseTypeRef();
 		//        }
 
 		return functionType;//.done(FUNCTION_TYPE);
 	}
 
 	/*
 		 * functionParameters
 		 *   : "(" functionParameter{","}? ")" // default values
 		 *   ;
 		 *
 		 * functionParameter
 		 *   : modifiers functionParameterRest
 		 *   ;
 		 *
 		 * functionParameterRest
 		 *   : parameter ("=" element)?
 		 *   ;
 		 */
 	void parseValueParameterList(boolean isFunctionTypeContents, TokenSet recoverySet)
 	{
 		PsiBuilder.Marker parameters = mark();
 
 		myBuilder.disableNewlines();
 		expect(JetTokens.LPAR, "Expecting '(", recoverySet);
 
 		if(!parseIdeTemplate())
 		{
 			if(!at(JetTokens.RPAR) && !atSet(recoverySet))
 			{
 				while(true)
 				{
 					if(at(JetTokens.COMMA))
 					{
 						errorAndAdvance("Expecting a parameter declaration");
 					}
 					else if(at(JetTokens.RPAR))
 					{
 						error("Expecting a parameter declaration");
 						break;
 					}
 					if(isFunctionTypeContents)
 					{
 						if(!tryParseValueParameter())
 						{
 							PsiBuilder.Marker valueParameter = mark();
 							parseModifierList(MODIFIER_LIST, false); // lazy, out, ref
 							parseTypeRef();
 							valueParameter.done(VALUE_PARAMETER);
 						}
 					}
 					else
 					{
 						parseValueParameter();
 					}
 					if(!at(JetTokens.COMMA))
 						break;
 					advance(); // COMMA
 				}
 			}
 		}
 
 		expect(JetTokens.RPAR, "Expecting ')'", recoverySet);
 		myBuilder.restoreNewlinesState();
 
 		parameters.done(VALUE_PARAMETER_LIST);
 	}
 
 	/*
 		 * functionParameter
 		 *   : modifiers ("val" | "var")? parameter ("=" element)?
 		 *   ;
 		 */
 	private boolean tryParseValueParameter()
 	{
 		return parseValueParameter(true);
 	}
 
 	private void parseValueParameter()
 	{
 		parseValueParameter(false);
 	}
 
 	private boolean parseValueParameter(boolean rollbackOnFailure)
 	{
 		PsiBuilder.Marker parameter = mark();
 
 		parseModifierListWithShortAnnotations(MODIFIER_LIST, TokenSet.create(JetTokens.IDENTIFIER), TokenSet.create(JetTokens.COMMA, JetTokens.RPAR, JetTokens.COLON));
 
 		if(at(JetTokens.VAR_KEYWORD) || at(JetTokens.VAL_KEYWORD))
 		{
 			advance(); // VAR_KEYWORD | VAL_KEYWORD
 		}
 
 		if(!parseFunctionParameterRest() && rollbackOnFailure)
 		{
 			parameter.rollbackTo();
 			return false;
 		}
 
 		parameter.done(VALUE_PARAMETER);
 		return true;
 	}
 
 	/*
 		 * functionParameterRest
 		 *   : parameter ("=" element)?
 		 *   ;
 		 */
 	private boolean parseFunctionParameterRest()
 	{
 		expect(JetTokens.IDENTIFIER, "Parameter name expected", PARAMETER_NAME_RECOVERY_SET);
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			parseTypeRef();
 		}
 		else
 		{
 			error("Parameters must have type annotation");
 			return false;
 		}
 
 		if(at(JetTokens.EQ))
 		{
 			advance(); // EQ
 			myExpressionParsing.parseExpression();
 		}
 		return true;
 	}
 
 	/*
 		* "<#<" expression ">#>"
 		*/
 	boolean parseIdeTemplate()
 	{
 		@Nullable NapileNodeType nodeType = IDE_TEMPLATE_EXPRESSION;
 		if(at(JetTokens.IDE_TEMPLATE_START))
 		{
 			PsiBuilder.Marker mark = null;
 			if(nodeType != null)
 			{
 				mark = mark();
 			}
 			advance();
 			expect(JetTokens.IDENTIFIER, "Expecting identifier inside template");
 			expect(JetTokens.IDE_TEMPLATE_END, "Expecting IDE template end after identifier");
 			if(nodeType != null)
 			{
 				mark.done(nodeType);
 			}
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 	@Override
 	protected JetParsing create(SemanticWhitespaceAwarePsiBuilder builder)
 	{
 		return createForTopLevel(builder);
 	}
 }
