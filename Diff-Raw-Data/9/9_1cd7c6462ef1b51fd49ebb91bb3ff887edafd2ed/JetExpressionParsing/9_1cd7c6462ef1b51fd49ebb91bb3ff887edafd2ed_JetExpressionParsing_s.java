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
 
 package org.napile.compiler.lang.parsing;
 
 import static org.napile.compiler.NapileNodeTypes.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.napile.compiler.NapileNodeType;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.compiler.lexer.NapileToken;
 import com.google.common.collect.ImmutableMap;
 import com.intellij.lang.PsiBuilder;
 import com.intellij.psi.tree.IElementType;
 import com.intellij.psi.tree.TokenSet;
 
 /**
  * @author abreslav
  */
 public class JetExpressionParsing extends AbstractJetParsing
 {
 	private static final TokenSet WHEN_CONDITION_RECOVERY_SET = TokenSet.create(JetTokens.RBRACE, JetTokens.IN_KEYWORD, JetTokens.NOT_IN, JetTokens.IS_KEYWORD, JetTokens.NOT_IS, JetTokens.ELSE_KEYWORD);
 	private static final TokenSet WHEN_CONDITION_RECOVERY_SET_WITH_ARROW = TokenSet.create(JetTokens.RBRACE, JetTokens.IN_KEYWORD, JetTokens.NOT_IN, JetTokens.IS_KEYWORD, JetTokens.NOT_IS, JetTokens.ELSE_KEYWORD, JetTokens.ARROW, JetTokens.DOT);
 
 
 	private static final ImmutableMap<String, NapileToken> KEYWORD_TEXTS = tokenSetToMap(JetTokens.KEYWORDS);
 
 	private static ImmutableMap<String, NapileToken> tokenSetToMap(TokenSet tokens)
 	{
 		ImmutableMap.Builder<String, NapileToken> builder = ImmutableMap.builder();
 		for(IElementType token : tokens.getTypes())
 		{
 			builder.put(token.toString(), (NapileToken) token);
 		}
 		return builder.build();
 	}
 
 	private static final TokenSet TYPE_ARGUMENT_LIST_STOPPERS = TokenSet.create(JetTokens.INTEGER_LITERAL, JetTokens.FLOAT_LITERAL, JetTokens.CHARACTER_LITERAL, JetTokens.OPEN_QUOTE, JetTokens.PACKAGE_KEYWORD, JetTokens.AS_KEYWORD, JetTokens.CLASS_KEYWORD, JetTokens.THIS_KEYWORD, JetTokens.VAL_KEYWORD, JetTokens.VAR_KEYWORD, JetTokens.METH_KEYWORD, JetTokens.FOR_KEYWORD, JetTokens.NULL_KEYWORD, JetTokens.TRUE_KEYWORD, JetTokens.FALSE_KEYWORD, JetTokens.IS_KEYWORD, JetTokens.THROW_KEYWORD, JetTokens.RETURN_KEYWORD, JetTokens.BREAK_KEYWORD, JetTokens.CONTINUE_KEYWORD, JetTokens.ANONYM_KEYWORD, JetTokens.IF_KEYWORD, JetTokens.TRY_KEYWORD, JetTokens.ELSE_KEYWORD, JetTokens.WHILE_KEYWORD, JetTokens.DO_KEYWORD, JetTokens.WHEN_KEYWORD, JetTokens.RBRACKET, JetTokens.RBRACE, JetTokens.RPAR, JetTokens.PLUSPLUS, JetTokens.MINUSMINUS, JetTokens.EXCLEXCL,
 			//            MUL,
 			JetTokens.PLUS, JetTokens.MINUS, JetTokens.EXCL, JetTokens.DIV, JetTokens.PERC, JetTokens.LTEQ,
 			// TODO GTEQ,   foo<bar, baz>=x
 			JetTokens.EQEQEQ, JetTokens.EXCLEQEQEQ, JetTokens.EQEQ, JetTokens.EXCLEQ, JetTokens.ANDAND, JetTokens.OROR, JetTokens.SAFE_ACCESS, JetTokens.ELVIS, JetTokens.SEMICOLON, JetTokens.RANGE, JetTokens.EQ, JetTokens.MULTEQ, JetTokens.DIVEQ, JetTokens.PERCEQ, JetTokens.PLUSEQ, JetTokens.MINUSEQ, JetTokens.NOT_IN, JetTokens.NOT_IS, //HASH,
 			JetTokens.COLON);
 
 	/*package*/ static final TokenSet EXPRESSION_FIRST = TokenSet.create(
 			// Prefix
 			JetTokens.MINUS, JetTokens.PLUS, JetTokens.MINUSMINUS, JetTokens.PLUSPLUS, JetTokens.EXCL, JetTokens.EXCLEXCL, // Joining complex tokens makes it necessary to put EXCLEXCL here
 			JetTokens.LBRACKET, JetTokens.LABEL_IDENTIFIER, JetTokens.AT, JetTokens.ATAT,
 			// Atomic
 
 			JetTokens.LPAR, // parenthesized
 			JetTokens.HASH, // Tuple
 
 			// literal constant
 			JetTokens.TRUE_KEYWORD, JetTokens.FALSE_KEYWORD, JetTokens.OPEN_QUOTE, JetTokens.INTEGER_LITERAL, JetTokens.CHARACTER_LITERAL, JetTokens.FLOAT_LITERAL, JetTokens.NULL_KEYWORD,
 
 			JetTokens.LBRACE, // functionLiteral
 
 			JetTokens.LPAR, // tuple
 
 			JetTokens.THIS_KEYWORD, // this
 			JetTokens.SUPER_KEYWORD, // super
 
 			JetTokens.IF_KEYWORD, // if
 			JetTokens.WHEN_KEYWORD, // when
 			JetTokens.TRY_KEYWORD, // try
 			JetTokens.ANONYM_KEYWORD, // object
 
 			// jump
 			JetTokens.THROW_KEYWORD, JetTokens.RETURN_KEYWORD, JetTokens.CONTINUE_KEYWORD, JetTokens.BREAK_KEYWORD,
 
 			// loop
 			JetTokens.FOR_KEYWORD, JetTokens.WHILE_KEYWORD, JetTokens.DO_KEYWORD,
 
 			JetTokens.IDENTIFIER, // SimpleName
 			JetTokens.FIELD_IDENTIFIER, // Field reference
 
 			JetTokens.PACKAGE_KEYWORD, // for absolute qualified names
 			JetTokens.IDE_TEMPLATE_START);
 
 	private static final TokenSet STATEMENT_FIRST = TokenSet.orSet(EXPRESSION_FIRST, TokenSet.create(
 			// declaration
 			JetTokens.LBRACKET, // attribute
 			JetTokens.METH_KEYWORD, JetTokens.VAL_KEYWORD, JetTokens.VAR_KEYWORD, JetTokens.CLASS_KEYWORD), JetTokens.MODIFIER_KEYWORDS);
 
 	/*package*/ static final TokenSet EXPRESSION_FOLLOW = TokenSet.create(JetTokens.SEMICOLON, JetTokens.ARROW, JetTokens.COMMA, JetTokens.RBRACE, JetTokens.RPAR, JetTokens.RBRACKET, JetTokens.IDE_TEMPLATE_END);
 
 	@SuppressWarnings({"UnusedDeclaration"})
 	private enum Precedence
 	{
 		POSTFIX(JetTokens.PLUSPLUS, JetTokens.MINUSMINUS, JetTokens.EXCLEXCL,
 				//                HASH,
 				JetTokens.DOT, JetTokens.SAFE_ACCESS), // typeArguments? valueArguments : typeArguments : arrayAccess
 
 		PREFIX(JetTokens.MINUS, JetTokens.PLUS, JetTokens.MINUSMINUS, JetTokens.PLUSPLUS, JetTokens.EXCL, JetTokens.LABEL_IDENTIFIER, JetTokens.AT, JetTokens.ATAT)
 				{ // attributes
 
 					@Override
 					public void parseHigherPrecedence(JetExpressionParsing parser)
 					{
 						throw new IllegalStateException("Don't call this method");
 					}
 				},
 
 		COLON_AS(JetTokens.COLON, JetTokens.AS_KEYWORD, JetTokens.AS_SAFE)
 				{
 					@Override
 					public NapileNodeType parseRightHandSide(IElementType operation, JetExpressionParsing parser)
 					{
 						parser.myJetParsing.parseTypeRef();
 						return BINARY_WITH_TYPE;
 					}
 
 					@Override
 					public void parseHigherPrecedence(JetExpressionParsing parser)
 					{
 						parser.parsePrefixExpression();
 					}
 				},
 
 		MULTIPLICATIVE(JetTokens.MUL, JetTokens.DIV, JetTokens.PERC),
 		ADDITIVE(JetTokens.PLUS, JetTokens.MINUS),
 		RANGE(JetTokens.RANGE),
 		SIMPLE_NAME(JetTokens.IDENTIFIER),
 		ELVIS(JetTokens.ELVIS),
 		IN_OR_IS(JetTokens.IN_KEYWORD, JetTokens.NOT_IN, JetTokens.IS_KEYWORD, JetTokens.NOT_IS)
 				{
 					@Override
 					public NapileNodeType parseRightHandSide(IElementType operation, JetExpressionParsing parser)
 					{
 						if(operation == JetTokens.IS_KEYWORD || operation == JetTokens.NOT_IS)
 						{
 							parser.parsePattern();
 
 							return BINARY_WITH_PATTERN;
 						}
 
 						return super.parseRightHandSide(operation, parser);
 					}
 				},
 		COMPARISON(JetTokens.LT, JetTokens.GT, JetTokens.LTEQ, JetTokens.GTEQ),
 		EQUALITY(JetTokens.EQEQ, JetTokens.EXCLEQ, JetTokens.EQEQEQ, JetTokens.EXCLEQEQEQ),
 		CONJUNCTION(JetTokens.ANDAND),
 		DISJUNCTION(JetTokens.OROR),
 		//        ARROW(JetTokens.ARROW),
 		ASSIGNMENT(JetTokens.EQ, JetTokens.PLUSEQ, JetTokens.MINUSEQ, JetTokens.MULTEQ, JetTokens.DIVEQ, JetTokens.PERCEQ),;
 
 		static
 		{
 			Precedence[] values = Precedence.values();
 			for(Precedence precedence : values)
 			{
 				int ordinal = precedence.ordinal();
 				precedence.higher = ordinal > 0 ? values[ordinal - 1] : null;
 			}
 		}
 
 		private Precedence higher;
 		private final TokenSet operations;
 
 		Precedence(IElementType... operations)
 		{
 			this.operations = TokenSet.create(operations);
 		}
 
 		public void parseHigherPrecedence(JetExpressionParsing parser)
 		{
 			assert higher != null;
 			parser.parseBinaryExpression(higher);
 		}
 
 		/**
 		 * @param operation the operation sign (e.g. PLUS or IS)
 		 * @param parser    the parser object
 		 * @return node type of the result
 		 */
 		public NapileNodeType parseRightHandSide(IElementType operation, JetExpressionParsing parser)
 		{
 			parseHigherPrecedence(parser);
 			return BINARY_EXPRESSION;
 		}
 
 		public final TokenSet getOperations()
 		{
 			return operations;
 		}
 	}
 
 	public static final TokenSet ALLOW_NEWLINE_OPERATIONS = TokenSet.create(JetTokens.DOT, JetTokens.SAFE_ACCESS);
 
 	public static final TokenSet ALL_OPERATIONS;
 
 	static
 	{
 		Set<IElementType> operations = new HashSet<IElementType>();
 		Precedence[] values = Precedence.values();
 		for(Precedence precedence : values)
 		{
 			operations.addAll(Arrays.asList(precedence.getOperations().getTypes()));
 		}
 		ALL_OPERATIONS = TokenSet.create(operations.toArray(new IElementType[operations.size()]));
 	}
 
 	static
 	{
 		IElementType[] operations = JetTokens.OPERATIONS.getTypes();
 		Set<IElementType> opSet = new HashSet<IElementType>(Arrays.asList(operations));
 		IElementType[] usedOperations = ALL_OPERATIONS.getTypes();
 		Set<IElementType> usedSet = new HashSet<IElementType>(Arrays.asList(usedOperations));
 
 		if(opSet.size() > usedSet.size())
 		{
 			opSet.removeAll(usedSet);
 			assert false : opSet;
 		}
 		assert usedSet.size() == opSet.size() : "Either some ops are unused, or something a non-op is used";
 
 		usedSet.removeAll(opSet);
 
 		assert usedSet.isEmpty() : usedSet.toString();
 	}
 
 
 	private final JetParsing myJetParsing;
 	private TokenSet decomposerExpressionFollow = null;
 
 	public JetExpressionParsing(SemanticWhitespaceAwarePsiBuilder builder, JetParsing jetParsing)
 	{
 		super(builder);
 		myJetParsing = jetParsing;
 	}
 
 	private TokenSet getDecomposerExpressionFollow()
 	{
 		// TODO : memoize
 		if(decomposerExpressionFollow == null)
 		{
 			List<IElementType> elvisFollow = new ArrayList<IElementType>();
 			Precedence precedence = Precedence.ELVIS;
 			while(precedence != null)
 			{
 				IElementType[] types = precedence.getOperations().getTypes();
 				Collections.addAll(elvisFollow, types);
 				precedence = precedence.higher;
 			}
 			decomposerExpressionFollow = TokenSet.orSet(EXPRESSION_FOLLOW, TokenSet.create(elvisFollow.toArray(new IElementType[elvisFollow.size()])));
 		}
 		return decomposerExpressionFollow;
 	}
 
 	/*
 		 * element
 		 *   : attributes element
 		 *   : "(" element ")" // see tupleLiteral
 		 *   : literalConstant
 		 *   : functionLiteral
 		 *   : tupleLiteral
 		 *   : "null"
 		 *   : "this" ("<" type ">")?
 		 *   : expressionWithPrecedences
 		 *   : if
 		 *   : try
 		 *   : "typeof" "(" element ")"
 		 *   : "new" constructorInvocation
 		 *   : objectLiteral
 		 *   : declaration
 		 *   : jump
 		 *   : loop
 		 *   // block is syntactically equivalent to a functionLiteral with no parameters
 		 *   ;
 		 */
 	public void parseExpression()
 	{
 		if(!atSet(EXPRESSION_FIRST))
 		{
 			error("Expecting an expression");
 			return;
 		}
 		parseBinaryExpression(Precedence.ASSIGNMENT);
 	}
 
 	/*
 		 * element (operation element)*
 		 *
 		 * see the precedence table
 		 */
 	private void parseBinaryExpression(Precedence precedence)
 	{
 		//        System.out.println(precedence.name() + " at " + myBuilder.getTokenText());
 
 		PsiBuilder.Marker expression = mark();
 
 		precedence.parseHigherPrecedence(this);
 
 		while(!interruptedWithNewLine() && atSet(precedence.getOperations()))
 		{
 			IElementType operation = tt();
 
 			parseOperationReference();
 
 			NapileNodeType resultType = precedence.parseRightHandSide(operation, this);
 			expression.done(resultType);
 			expression = expression.precede();
 		}
 
 		expression.drop();
 	}
 
 	/*
 		 * operation? prefixExpression
 		 */
 	private void parsePrefixExpression()
 	{
 		//        System.out.println("pre at "  + myBuilder.getTokenText());
 
 		if(at(JetTokens.LBRACKET))
 		{
 			if(!parseLocalDeclaration())
 			{
 				PsiBuilder.Marker expression = mark();
 				myJetParsing.parseAnnotations();
 				parsePrefixExpression();
 				expression.done(ANNOTATED_EXPRESSION);
 			}
 			else
 			{
 				return;
 			}
 		}
 		else
 		{
 			myBuilder.disableJoiningComplexTokens();
 			if(atSet(Precedence.PREFIX.getOperations()))
 			{
 				PsiBuilder.Marker expression = mark();
 
 				parseOperationReference();
 
 				myBuilder.restoreJoiningComplexTokensState();
 
 				parsePrefixExpression();
 				expression.done(PREFIX_EXPRESSION);
 			}
 			else
 			{
 				myBuilder.restoreJoiningComplexTokensState();
 				parsePostfixExpression();
 			}
 		}
 	}
 
 	/*
 		 * atomicExpression postfixUnaryOperation?
 		 *
 		 * postfixUnaryOperation
 		 *   : "++" : "--" : "!!"
 		 *   : typeArguments? valueArguments (getEntryPoint? functionLiteral)
 		 *   : typeArguments (getEntryPoint? functionLiteral)
 		 *   : arrayAccess
 		 *   : memberAccessOperation postfixUnaryExpression // TODO: Review
 		 *   ;
 		 */
 	private void parsePostfixExpression()
 	{
 		//        System.out.println("post at "  + myBuilder.getTokenText());
 
 		PsiBuilder.Marker expression = mark();
 		parseAtomicExpression();
 		while(true)
 		{
 			if(interruptedWithNewLine())
 			{
 				break;
 			}
 			else if(at(JetTokens.LBRACKET))
 			{
 				parseArrayAccess();
 				expression.done(ARRAY_ACCESS_EXPRESSION);
 			}
 			else if(parseCallSuffix())
 			{
 				expression.done(CALL_EXPRESSION);
 			}
 			else if(at(JetTokens.DOT))
 			{
 				advance(); // DOT
 
 				parseCallExpression();
 
 				expression.done(DOT_QUALIFIED_EXPRESSION);
 			}
 			else if(at(JetTokens.SAFE_ACCESS))
 			{
 				advance(); // SAFE_ACCESS
 
 				parseCallExpression();
 
 				expression.done(SAFE_ACCESS_EXPRESSION);
 			}
 			//            else if (at(HASH)) {
 			//                advance(); // HASH
 			//
 			//                expect(IDENTIFIER, "Expecting property or function name");
 			//
 			//                expression.done(HASH_QUALIFIED_EXPRESSION);
 			//            }
 			else if(atSet(Precedence.POSTFIX.getOperations()))
 			{
 				parseOperationReference();
 				expression.done(POSTFIX_EXPRESSION);
 			}
 			else
 			{
 				break;
 			}
 			expression = expression.precede();
 		}
 		expression.drop();
 	}
 
 	/*
 		 * callSuffix
 		 *   : typeArguments? valueArguments (getEntryPoint? functionLiteral*)
 		 *   : typeArguments (getEntryPoint? functionLiteral*)
 		 *   ;
 		 */
 	private boolean parseCallSuffix()
 	{
 		if(parseCallWithClosure())
 		{
 			parseCallWithClosure();
 		}
 		else if(at(JetTokens.LPAR))
 		{
 			parseValueArgumentList();
 			parseCallWithClosure();
 		}
 		else if(at(JetTokens.LT))
 		{
 			PsiBuilder.Marker typeArgumentList = mark();
 			if(myJetParsing.tryParseTypeArgumentList(TYPE_ARGUMENT_LIST_STOPPERS))
 			{
 				typeArgumentList.done(TYPE_ARGUMENT_LIST);
 				if(!myBuilder.newlineBeforeCurrentToken() && at(JetTokens.LPAR))
 					parseValueArgumentList();
 				parseCallWithClosure();
 			}
 			else
 			{
 				typeArgumentList.rollbackTo();
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 
 		return true;
 	}
 
 	/*
 		 * atomicExpression typeParameters? valueParameters? functionLiteral*
 		 */
 	private void parseCallExpression()
 	{
 		PsiBuilder.Marker mark = mark();
 		parseAtomicExpression();
 		if(!myBuilder.newlineBeforeCurrentToken() && parseCallSuffix())
 		{
 			mark.done(CALL_EXPRESSION);
 		}
 		else
 		{
 			mark.drop();
 		}
 	}
 
 	private void parseOperationReference()
 	{
 		PsiBuilder.Marker operationReference = mark();
 		advance(); // operation
 		operationReference.done(OPERATION_REFERENCE);
 	}
 
 	/*
 		 * element (getEntryPoint? functionLiteral)?
 		 */
 	protected boolean parseCallWithClosure()
 	{
 		boolean success = false;
 		//        while (!myBuilder.newlineBeforeCurrentToken()
 		//                && (at(LBRACE)
 		while((at(JetTokens.LBRACE) || atSet(JetTokens.LABELS) && lookahead(1) == JetTokens.LBRACE))
 		{
 			if(!at(JetTokens.LBRACE))
 			{
 				assert _atSet(JetTokens.LABELS);
 				parsePrefixExpression();
 			}
 			else
 			{
 				parseFunctionLiteral();
 			}
 			success = true;
 		}
 		return success;
 	}
 
 	/*
 		 * atomicExpression
 		 *   : tupleLiteral // or parenthesized element
 		 *   : "this" label?
 		 *   : "super" ("<" type ">")? label?
 		 *   : objectLiteral
 		 *   : jump
 		 *   : if
 		 *   : when
 		 *   : try
 		 *   : loop
 		 *   : literalConstant
 		 *   : functionLiteral
 		 *   : declaration
 		 *   : SimpleName
 		 *   : "package" // foo the root namespace
 		 *   ;
 		 */
 	private void parseAtomicExpression()
 	{
 		//        System.out.println("atom at "  + myBuilder.getTokenText());
 
 		if(at(JetTokens.LPAR))
 		{
 			parseParenthesizedExpression();
 		}
 		else if(at(JetTokens.IDE_TEMPLATE_START))
 		{
 			myJetParsing.parseIdeTemplate();
 		}
 		else if(at(JetTokens.PACKAGE_KEYWORD))
 		{
 			parseOneTokenExpression(ROOT_NAMESPACE);
 		}
 		else if(at(JetTokens.THIS_KEYWORD))
 		{
 			parseThisExpression();
 		}
 		else if(at(JetTokens.SUPER_KEYWORD))
 		{
 			parseSuperExpression();
 		}
 		else if(at(JetTokens.ANONYM_KEYWORD))
 		{
 			parseObjectLiteral();
 		}
 		else if(at(JetTokens.THROW_KEYWORD))
 		{
 			parseThrow();
 		}
 		else if(at(JetTokens.RETURN_KEYWORD))
 		{
 			parseReturn();
 		}
 		else if(at(JetTokens.CONTINUE_KEYWORD))
 		{
 			parseJump(CONTINUE);
 		}
 		else if(at(JetTokens.BREAK_KEYWORD))
 		{
 			parseJump(BREAK);
 		}
 		else if(at(JetTokens.IF_KEYWORD))
 		{
 			parseIf();
 		}
 		else if(at(JetTokens.WHEN_KEYWORD))
 		{
 			parseWhen();
 		}
 		else if(at(JetTokens.TRY_KEYWORD))
 		{
 			parseTry();
 		}
 		else if(at(JetTokens.FOR_KEYWORD))
 		{
 			parseFor();
 		}
 		else if(at(JetTokens.WHILE_KEYWORD))
 		{
 			parseWhile();
 		}
 		else if(at(JetTokens.DO_KEYWORD))
 		{
 			parseDoWhile();
 		}
 		else if(atSet(JetTokens.CLASS_KEYWORD, JetTokens.METH_KEYWORD, JetTokens.VAL_KEYWORD, JetTokens.VAR_KEYWORD))
 		{
 			parseLocalDeclaration();
 		}
 		else if(at(JetTokens.FIELD_IDENTIFIER))
 		{
 			parseSimpleNameExpression();
 		}
 		else if(at(JetTokens.IDENTIFIER))
 		{
 			parseSimpleNameExpression();
 		}
 		else if(at(JetTokens.LBRACE))
 		{
 			parseFunctionLiteral();
 		}
 		else if(at(JetTokens.OPEN_QUOTE))
 		{
 			parseStringTemplate();
 		}
 		else if(!parseLiteralConstant())
 		{
 			// TODO: better recovery if FIRST(element) did not match
 			errorWithRecovery("Expecting an element", EXPRESSION_FOLLOW);
 		}
 	}
 
 	/*
 		 * stringTemplate
 		 *   : OPEN_QUOTE stringTemplateElement* CLOSING_QUOTE
 		 *   ;
 		 */
 	private void parseStringTemplate()
 	{
 		assert _at(JetTokens.OPEN_QUOTE);
 
 		PsiBuilder.Marker template = mark();
 
 		advance(); // OPEN_QUOTE
 
 		while(!eof())
 		{
 			if(at(JetTokens.CLOSING_QUOTE) || at(JetTokens.DANGLING_NEWLINE))
 			{
 				break;
 			}
 			parseStringTemplateElement();
 		}
 
 		if(at(JetTokens.DANGLING_NEWLINE))
 		{
 			errorAndAdvance("Expecting '\"'");
 		}
 		else
 		{
 			expect(JetTokens.CLOSING_QUOTE, "Expecting '\"'");
 		}
 		template.done(STRING_TEMPLATE);
 	}
 
 	/*
 		 * stringTemplateElement
 		 *   : RegularStringPart
 		 *   : ShortTemplateEntrySTART (SimpleName | "this")
 		 *   : EscapeSequence
 		 *   : longTemplate
 		 *   ;
 		 *
 		 * longTemplate
 		 *   : "${" expression "}"
 		 *   ;
 		 */
 	private void parseStringTemplateElement()
 	{
 		if(at(JetTokens.REGULAR_STRING_PART))
 		{
 			PsiBuilder.Marker mark = mark();
 			advance(); // REGULAR_STRING_PART
 			mark.done(LITERAL_STRING_TEMPLATE_ENTRY);
 		}
 		else if(at(JetTokens.ESCAPE_SEQUENCE))
 		{
 			PsiBuilder.Marker mark = mark();
 			advance(); // ESCAPE_SEQUENCE
 			mark.done(ESCAPE_STRING_TEMPLATE_ENTRY);
 		}
 		else if(at(JetTokens.SHORT_TEMPLATE_ENTRY_START))
 		{
 			PsiBuilder.Marker entry = mark();
 			advance(); // SHORT_TEMPLATE_ENTRY_START
 
 			if(at(JetTokens.THIS_KEYWORD))
 			{
 				PsiBuilder.Marker thisExpression = mark();
 				PsiBuilder.Marker reference = mark();
 				advance(); // THIS_KEYWORD
 				reference.done(REFERENCE_EXPRESSION);
 				thisExpression.done(THIS_EXPRESSION);
 			}
 			else
 			{
 				NapileToken keyword = KEYWORD_TEXTS.get(myBuilder.getTokenText());
 				if(keyword != null)
 				{
 					myBuilder.remapCurrentToken(keyword);
 					errorAndAdvance("Keyword cannot be used as a reference");
 				}
 				else
 				{
 					PsiBuilder.Marker reference = mark();
 					expect(JetTokens.IDENTIFIER, "Expecting a name");
 					reference.done(REFERENCE_EXPRESSION);
 				}
 			}
 
 			entry.done(SHORT_STRING_TEMPLATE_ENTRY);
 		}
 		else if(at(JetTokens.LONG_TEMPLATE_ENTRY_START))
 		{
 			PsiBuilder.Marker longTemplateEntry = mark();
 
 			advance(); // LONG_TEMPLATE_ENTRY_START
 
 			parseExpression();
 
 			expect(JetTokens.LONG_TEMPLATE_ENTRY_END, "Expecting '}'", TokenSet.create(JetTokens.CLOSING_QUOTE, JetTokens.DANGLING_NEWLINE, JetTokens.REGULAR_STRING_PART, JetTokens.ESCAPE_SEQUENCE, JetTokens.SHORT_TEMPLATE_ENTRY_START));
 			longTemplateEntry.done(LONG_STRING_TEMPLATE_ENTRY);
 		}
 		else
 		{
 			errorAndAdvance("Unexpected token in a string template");
 		}
 	}
 
 	/*
 		 * literalConstant
 		 *   : "true" | "false"
 		 *   : StringWithTemplates
 		 *   : NoEscapeString
 		 *   : IntegerLiteral
 		 *   : LongLiteral
 		 *   : CharacterLiteral
 		 *   : FloatLiteral
 		 *   : "null"
 		 *   ;
 		 */
 	private boolean parseLiteralConstant()
 	{
 		if(at(JetTokens.TRUE_KEYWORD) || at(JetTokens.FALSE_KEYWORD))
 		{
 			parseOneTokenExpression(BOOLEAN_CONSTANT);
 		}
 		else if(at(JetTokens.INTEGER_LITERAL))
 		{
 			parseOneTokenExpression(INTEGER_CONSTANT);
 		}
 		else if(at(JetTokens.CHARACTER_LITERAL))
 		{
 			parseOneTokenExpression(CHARACTER_CONSTANT);
 		}
 		else if(at(JetTokens.FLOAT_LITERAL))
 		{
 			parseOneTokenExpression(FLOAT_CONSTANT);
 		}
 		else if(at(JetTokens.NULL_KEYWORD))
 		{
 			parseOneTokenExpression(NULL);
 		}
 		else
 		{
 			return false;
 		}
 		return true;
 	}
 
 	/*
 		 * when
 		 *   : "when" ("(" (modifiers "val" SimpleName "=")? element ")")? "{"
 		 *         whenEntry*
 		 *     "}"
 		 *   ;
 		 */
 	private void parseWhen()
 	{
 		assert _at(JetTokens.WHEN_KEYWORD);
 
 		PsiBuilder.Marker when = mark();
 
 		advance(); // WHEN_KEYWORD
 
 		// Parse condition
 		myBuilder.disableNewlines();
 		if(at(JetTokens.LPAR))
 		{
 			advanceAt(JetTokens.LPAR);
 
 			int valPos = matchTokenStreamPredicate(new FirstBefore(new At(JetTokens.VAL_KEYWORD), new AtSet(JetTokens.RPAR, JetTokens.LBRACE, JetTokens.RBRACE, JetTokens.SEMICOLON, JetTokens.EQ)));
 			if(valPos >= 0)
 			{
 				PsiBuilder.Marker property = mark();
 				myJetParsing.parseModifierList(MODIFIER_LIST, true);
 				myJetParsing.parseProperty(true);
 				property.done(PROPERTY);
 			}
 			else
 			{
 				parseExpression();
 			}
 
 			expect(JetTokens.RPAR, "Expecting ')'");
 		}
 		myBuilder.restoreNewlinesState();
 
 		// Parse when block
 		myBuilder.enableNewlines();
 		expect(JetTokens.LBRACE, "Expecting '{'");
 
 		while(!eof() && !at(JetTokens.RBRACE))
 		{
 			parseWhenEntry();
 		}
 
 		expect(JetTokens.RBRACE, "Expecting '}'");
 		myBuilder.restoreNewlinesState();
 
 		when.done(WHEN);
 	}
 
 	/*
 		 * whenEntry
 		 *   // TODO : consider empty after ->
 		 *   : whenCondition{","} "->" element SEMI
 		 *   : "else" "->" element SEMI
 		 *   ;
 		 */
 	private void parseWhenEntry()
 	{
 		PsiBuilder.Marker entry = mark();
 
 		if(at(JetTokens.ELSE_KEYWORD))
 		{
 			advance(); // ELSE_KEYWORD
 
 			if(!at(JetTokens.ARROW))
 			{
 				errorUntil("Expecting '->'", TokenSet.create(JetTokens.ARROW, JetTokens.RBRACE, JetTokens.EOL_OR_SEMICOLON));
 			}
 
 			if(at(JetTokens.ARROW))
 			{
 				advance(); // ARROW
 
 				if(atSet(WHEN_CONDITION_RECOVERY_SET))
 				{
 					error("Expecting an element");
 				}
 				else
 				{
 					parseExpressionPreferringBlocks();
 				}
 			}
 			else if(!atSet(WHEN_CONDITION_RECOVERY_SET))
 			{
 				errorAndAdvance("Expecting '->'");
 			}
 		}
 		else
 		{
 			parseWhenEntryNotElse();
 		}
 
 		entry.done(WHEN_ENTRY);
 		consumeIf(JetTokens.SEMICOLON);
 	}
 
 	/*
 		 * : whenCondition{","} "->" element SEMI
 		 */
 	private void parseWhenEntryNotElse()
 	{
 		if(!myJetParsing.parseIdeTemplate())
 		{
 			while(true)
 			{
 				while(at(JetTokens.COMMA))
 					errorAndAdvance("Expecting a when-condition");
 				parseWhenCondition();
 				if(!at(JetTokens.COMMA))
 					break;
 				advance(); // COMMA
 			}
 		}
 		expect(JetTokens.ARROW, "Expecting '->' or 'when'", WHEN_CONDITION_RECOVERY_SET);
 		if(atSet(WHEN_CONDITION_RECOVERY_SET))
 		{
 			error("Expecting an element");
 		}
 		else
 		{
 			parseExpressionPreferringBlocks();
 		}
 		// SEMI is consumed in parseWhenEntry
 	}
 
 	/*
 		 * whenCondition
 		 *   : expression
 		 *   : ("in" | "!in") expression
 		 *   : ("is" | "!is") isRHS
 		 *   ;
 		 */
 	private void parseWhenCondition()
 	{
 		PsiBuilder.Marker condition = mark();
 		myBuilder.disableNewlines();
 		if(at(JetTokens.IN_KEYWORD) || at(JetTokens.NOT_IN))
 		{
 			PsiBuilder.Marker mark = mark();
 			advance(); // IN_KEYWORD or NOT_IN
 			mark.done(OPERATION_REFERENCE);
 
 
 			if(atSet(WHEN_CONDITION_RECOVERY_SET_WITH_ARROW))
 			{
 				error("Expecting an element");
 			}
 			else
 			{
 				parseExpression();
 			}
 			condition.done(WHEN_CONDITION_IN_RANGE);
 		}
 		else if(at(JetTokens.IS_KEYWORD) || at(JetTokens.NOT_IS))
 		{
 			advance(); // IS_KEYWORD or NOT_IS
 
 			if(atSet(WHEN_CONDITION_RECOVERY_SET_WITH_ARROW))
 			{
 				error("Expecting a type or a decomposer pattern");
 			}
 			else
 			{
 				parsePattern();
 			}
 			condition.done(WHEN_CONDITION_IS_PATTERN);
 		}
 		else
 		{
 			PsiBuilder.Marker expressionPattern = mark();
 			if(atSet(WHEN_CONDITION_RECOVERY_SET_WITH_ARROW))
 			{
 				error("Expecting an expression, is-condition or in-condition");
 			}
 			else
 			{
 				parseExpression();
 			}
 			expressionPattern.done(EXPRESSION_PATTERN);
 			condition.done(WHEN_CONDITION_EXPRESSION);
 		}
 		myBuilder.restoreNewlinesState();
 	}
 
 	/*
 		 * pattern
 		 *   : attributes pattern
 		 *   : type // '[a] T' is a type-pattern 'T' with an attribute '[a]', not a type-pattern '[a] T'
 		 *          // this makes sense because is-check may be different for a type with attributes
 		 *   : constantPattern
 		 *   : bindingPattern
 		 *   : "*" // wildcard pattern
 		 *   ;
 		 */
 	private void parsePattern()
 	{
 		PsiBuilder.Marker pattern = mark();
 
 		myJetParsing.parseAnnotations();
 
 		if(at(JetTokens.PACKAGE_KEYWORD) || at(JetTokens.IDENTIFIER) || at(JetTokens.METH_KEYWORD) || at(JetTokens.THIS_KEYWORD))
 		{
 			PsiBuilder.Marker rollbackMarker = mark();
 			parseBinaryExpression(Precedence.ELVIS);
 
 			int expressionEndOffset = myBuilder.getCurrentOffset();
 			rollbackMarker.rollbackTo();
 			rollbackMarker = mark();
 
 			myJetParsing.parseTypeRef();
 
 			rollbackMarker.drop();
 			pattern.done(TYPE_PATTERN);
 		}
 		else if(at(JetTokens.VAL_KEYWORD))
 		{
 			parseBindingPattern();
 			pattern.done(BINDING_PATTERN);
 		}
 		else if(at(JetTokens.OPEN_QUOTE))
 		{
 			parseStringTemplate();
 			pattern.done(EXPRESSION_PATTERN);
 		}
 		else if(parseLiteralConstant())
 		{
 			pattern.done(EXPRESSION_PATTERN);
 		}
 		else
 		{
 			errorUntil("Pattern expected", TokenSet.create(JetTokens.RBRACE, JetTokens.ARROW));
 			pattern.drop();
 		}
 	}
 
 	/*
 		 * bindingPattern
 		 *   : "val" SimpleName binding?
 		 *   ;
 		 *
 		 * binding
 		 *   : "is" pattern
 		 *   : "!is" pattern
 		 *   : "in" element
 		 *   : "!in" element
 		 *   : ":" type
 		 *   ;
 		 */
 	private void parseBindingPattern()
 	{
 		assert _at(JetTokens.VAL_KEYWORD);
 
 		PsiBuilder.Marker declaration = mark();
 
 		advance(); // VAL_KEYWORD
 
 		expect(JetTokens.IDENTIFIER, "Expecting an identifier");
 
 		if(at(JetTokens.COLON))
 		{
 			advance(); // EQ
 
 			myJetParsing.parseTypeRef();
 			declaration.done(PROPERTY);
 		}
 		else
 		{
 			declaration.done(PROPERTY);
 			PsiBuilder.Marker subCondition = mark();
 			if(at(JetTokens.IS_KEYWORD) || at(JetTokens.NOT_IS))
 			{
 
 				advance(); // IS_KEYWORD or NOT_IS
 
 				parsePattern();
 				subCondition.done(WHEN_CONDITION_IS_PATTERN);
 			}
 			else if(at(JetTokens.IN_KEYWORD) || at(JetTokens.NOT_IN))
 			{
 				PsiBuilder.Marker mark = mark();
 				advance(); // IN_KEYWORD ot NOT_IN
 				mark.done(OPERATION_REFERENCE);
 
 				parseExpression();
 				subCondition.done(WHEN_CONDITION_IN_RANGE);
 			}
 			else
 			{
 				subCondition.drop();
 			}
 		}
 	}
 
 	/*
 		 * arrayAccess
 		 *   : "[" element{","} "]"
 		 *   ;
 		 */
 	private void parseArrayAccess()
 	{
 		assert _at(JetTokens.LBRACKET);
 
 		PsiBuilder.Marker indices = mark();
 
 		myBuilder.disableNewlines();
 		advance(); // LBRACKET
 
 		while(true)
 		{
 			if(at(JetTokens.COMMA))
 				errorAndAdvance("Expecting an index element");
 			if(at(JetTokens.RBRACKET))
 			{
 				error("Expecting an index element");
 				break;
 			}
 			parseExpression();
 			if(!at(JetTokens.COMMA))
 				break;
 			advance(); // COMMA
 		}
 
 		expect(JetTokens.RBRACKET, "Expecting ']'");
 		myBuilder.restoreNewlinesState();
 
 		indices.done(INDICES);
 	}
 
 	/*
 		 * SimpleName
 		 */
 	public void parseSimpleNameExpression()
 	{
 		PsiBuilder.Marker simpleName = mark();
 		if(at(JetTokens.FIELD_IDENTIFIER))
 		{
 			advance(); //
 		}
 		else
 		{
 			expect(JetTokens.IDENTIFIER, "Expecting an identifier");
 		}
 		simpleName.done(REFERENCE_EXPRESSION);
 	}
 
 	/*
 		 * modifiers declarationRest
 		 */
 	private boolean parseLocalDeclaration()
 	{
 		PsiBuilder.Marker decl = mark();
 
 		myJetParsing.parseModifierList(MODIFIER_LIST);
 
 		IElementType declType = parseLocalDeclarationRest();
 
 		if(declType != null)
 		{
 			decl.done(declType);
 			return true;
 		}
 		else
 		{
 			decl.rollbackTo();
 			return false;
 		}
 	}
 
 	/*
 		 * functionLiteral  // one can use "it" as a parameter name
 		 *   : "{" expressions "}"
 		 *   : "{" (modifiers SimpleName){","} "->" statements "}"
 		 *   : "{" (type ".")? "(" (modifiers SimpleName (":" type)?){","} ")" (":" type)? "->" expressions "}"
 		 *   ;
 		 */
 	private void parseFunctionLiteral()
 	{
 		parseFunctionLiteral(false);
 	}
 
 	private void parseFunctionLiteral(boolean preferBlock)
 	{
 		assert _at(JetTokens.LBRACE);
 
 		PsiBuilder.Marker literalExpression = mark();
 
 		PsiBuilder.Marker literal = mark();
 
 		myBuilder.enableNewlines();
 		advance(); // LBRACE
 
 		boolean paramsFound = false;
 
 		if(at(JetTokens.ARROW))
 		{
 			//   { -> ...}
 			advance(); // ARROW
 			mark().done(VALUE_PARAMETER_LIST);
 			paramsFound = true;
 		}
 		else if(at(JetTokens.LPAR))
 		{
 			// Look for ARROW after matching RPAR
 			//   {(a, b) -> ...}
 
 			{
 				boolean preferParamsToExpressions = isConfirmedParametersByComma();
 
 				PsiBuilder.Marker rollbackMarker = mark();
 				parseFunctionLiteralParametersAndType();
 
 				paramsFound = preferParamsToExpressions ? rollbackOrDrop(rollbackMarker, JetTokens.ARROW, "An -> is expected", JetTokens.RBRACE) : rollbackOrDropAt(rollbackMarker, JetTokens.ARROW);
 			}
 
 			if(!paramsFound)
 			{
 				// If not found, try a typeRef DOT and then LPAR .. RPAR ARROW
 				//   {((A) -> B).(x) -> ... }
 				paramsFound = parseFunctionTypeDotParametersAndType();
 			}
 		}
 		else
 		{
 			if(at(JetTokens.IDENTIFIER))
 			{
 				// Try to parse a simple name list followed by an ARROW
 				//   {a -> ...}
 				//   {a, b -> ...}
 				PsiBuilder.Marker rollbackMarker = mark();
 				boolean preferParamsToExpressions = (lookahead(1) == JetTokens.COMMA);
 				parseFunctionLiteralShorthandParameterList();
 				parseOptionalFunctionLiteralType();
 
 				paramsFound = preferParamsToExpressions ? rollbackOrDrop(rollbackMarker, JetTokens.ARROW, "An -> is expected", JetTokens.RBRACE) : rollbackOrDropAt(rollbackMarker, JetTokens.ARROW);
 			}
 			if(!paramsFound && atSet(JetParsing.TYPE_REF_FIRST))
 			{
 				// Try to parse a type DOT valueParameterList ARROW
 				//   {A.(b) -> ...}
 				paramsFound = parseFunctionTypeDotParametersAndType();
 			}
 		}
 
 		if(!paramsFound)
 		{
 			if(preferBlock)
 			{
 				literal.drop();
 				parseStatements();
 				expect(JetTokens.RBRACE, "Expecting '}'");
 				literalExpression.done(BLOCK);
 				myBuilder.restoreNewlinesState();
 
 				return;
 			}
 		}
 
 		PsiBuilder.Marker body = mark();
 		parseStatements();
 		body.done(BLOCK);
 
 		expect(JetTokens.RBRACE, "Expecting '}'");
 		myBuilder.restoreNewlinesState();
 
 		literal.done(FUNCTION_LITERAL);
 		literalExpression.done(FUNCTION_LITERAL_EXPRESSION);
 	}
 
 	private boolean rollbackOrDropAt(PsiBuilder.Marker rollbackMarker, IElementType dropAt)
 	{
 		if(at(dropAt))
 		{
 			advance(); // dropAt
 			rollbackMarker.drop();
 			return true;
 		}
 		rollbackMarker.rollbackTo();
 		return false;
 	}
 
 	private boolean rollbackOrDrop(PsiBuilder.Marker rollbackMarker, NapileToken expected, String expectMessage, IElementType validForDrop)
 	{
 		if(at(expected))
 		{
 			advance(); // dropAt
 			rollbackMarker.drop();
 			return true;
 		}
 		else if(at(validForDrop))
 		{
 			rollbackMarker.drop();
 			expect(expected, expectMessage);
 			return true;
 		}
 
 		rollbackMarker.rollbackTo();
 		return false;
 	}
 
 
 	/*
 		 * SimpleName{,}
 		 */
 	private void parseFunctionLiteralShorthandParameterList()
 	{
 		PsiBuilder.Marker parameterList = mark();
 
 		while(!eof())
 		{
 			PsiBuilder.Marker parameter = mark();
 
 			//            int parameterNamePos = matchTokenStreamPredicate(new LastBefore(new At(IDENTIFIER), new AtOffset(doubleArrowPos)));
 			//            createTruncatedBuilder(parameterNamePos).parseModifierList(MODIFIER_LIST, false);
 
 			expect(JetTokens.IDENTIFIER, "Expecting parameter name", TokenSet.create(JetTokens.ARROW));
 
 			parameter.done(VALUE_PARAMETER);
 
 			if(at(JetTokens.COLON))
 			{
 				PsiBuilder.Marker errorMarker = mark();
 				advance(); // COLON
 				myJetParsing.parseTypeRef();
 				errorMarker.error("To specify a type of a parameter or a return type, use the full notation: {(parameter : Type) : ReturnType -> ...}");
 			}
 			else if(at(JetTokens.ARROW))
 			{
 				break;
 			}
 			else if(at(JetTokens.COMMA))
 			{
 				advance(); // COMMA
 			}
 			else
 			{
 				error("Expecting '->' or ','");
 				break;
 			}
 		}
 
 		parameterList.done(VALUE_PARAMETER_LIST);
 	}
 
 	// Check that position is followed by top level comma. It can't be expression and we want it be
 	// parsed as parameters in function literal
 	private boolean isConfirmedParametersByComma()
 	{
 		assert _at(JetTokens.LPAR);
 		PsiBuilder.Marker lparMarker = mark();
 		advance(); // LPAR
 		int comma = matchTokenStreamPredicate(new FirstBefore(new At(JetTokens.COMMA), new AtSet(JetTokens.ARROW, JetTokens.RPAR)));
 		lparMarker.rollbackTo();
 		return comma > 0;
 	}
 
 	private boolean parseFunctionTypeDotParametersAndType()
 	{
 		PsiBuilder.Marker rollbackMarker = mark();
 
 		// True when it's confirmed that body of literal can't be simple expressions and we prefer to parse
 		// it to function params if possible.
 		boolean preferParamsToExpressions = false;
 
 		int lastDot = matchTokenStreamPredicate(new LastBefore(new At(JetTokens.DOT), new AtSet(JetTokens.ARROW, JetTokens.RPAR)));
 		if(lastDot >= 0)
 		{
 			createTruncatedBuilder(lastDot).parseTypeRef();
 			if(at(JetTokens.DOT))
 			{
 				advance(); // DOT
 
 				if(at(JetTokens.LPAR))
 				{
 					preferParamsToExpressions = isConfirmedParametersByComma();
 				}
 
 				parseFunctionLiteralParametersAndType();
 			}
 		}
 
 		return preferParamsToExpressions ? rollbackOrDrop(rollbackMarker, JetTokens.ARROW, "An -> is expected", JetTokens.RBRACE) : rollbackOrDropAt(rollbackMarker, JetTokens.ARROW);
 	}
 
 	private void parseFunctionLiteralParametersAndType()
 	{
 		parseFunctionLiteralParameterList();
 		parseOptionalFunctionLiteralType();
 	}
 
 	/*
 		 * (":" type)?
 		 */
 	private void parseOptionalFunctionLiteralType()
 	{
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			if(at(JetTokens.ARROW))
 			{
 				error("Expecting a type");
 			}
 			else
 			{
 				myJetParsing.parseTypeRef();
 			}
 		}
 	}
 
 	/*
 		 * "(" (modifiers SimpleName (":" type)?){","} ")"
 		 */
 	private void parseFunctionLiteralParameterList()
 	{
 		PsiBuilder.Marker list = mark();
 		expect(JetTokens.LPAR, "Expecting a parameter list in parentheses (...)", TokenSet.create(JetTokens.ARROW, JetTokens.COLON));
 
 		myBuilder.disableNewlines();
 
 		if(!at(JetTokens.RPAR))
 		{
 			while(true)
 			{
 				if(at(JetTokens.COMMA))
 					errorAndAdvance("Expecting a parameter declaration");
 
 				PsiBuilder.Marker parameter = mark();
 				int parameterNamePos = matchTokenStreamPredicate(new LastBefore(new At(JetTokens.IDENTIFIER), new AtSet(JetTokens.COMMA, JetTokens.RPAR, JetTokens.COLON, JetTokens.ARROW)));
 				createTruncatedBuilder(parameterNamePos).parseModifierList(MODIFIER_LIST, false);
 
 				expect(JetTokens.IDENTIFIER, "Expecting parameter declaration");
 
 				if(at(JetTokens.COLON))
 				{
 					advance(); // COLON
 					myJetParsing.parseTypeRef();
 				}
 				parameter.done(VALUE_PARAMETER);
 				if(!at(JetTokens.COMMA))
 					break;
 				advance(); // COMMA
 
 				if(at(JetTokens.RPAR))
 				{
 					error("Expecting a parameter declaration");
 					break;
 				}
 			}
 		}
 
 		myBuilder.restoreNewlinesState();
 
 		expect(JetTokens.RPAR, "Expecting ')", TokenSet.create(JetTokens.ARROW, JetTokens.COLON));
 		list.done(VALUE_PARAMETER_LIST);
 	}
 
 	/*
 		 * expressions
 		 *   : SEMI* statement{SEMI+} SEMI*
 		 */
 	public void parseStatements()
 	{
 		while(at(JetTokens.SEMICOLON))
 			advance(); // SEMICOLON
 		while(!eof() && !at(JetTokens.RBRACE))
 		{
 			if(!atSet(STATEMENT_FIRST))
 			{
 				errorAndAdvance("Expecting an element");
 			}
 			if(atSet(STATEMENT_FIRST))
 			{
 				parseStatement();
 			}
 			if(at(JetTokens.SEMICOLON))
 			{
 				while(at(JetTokens.SEMICOLON))
 					advance(); // SEMICOLON
 			}
 			else if(at(JetTokens.RBRACE))
 			{
 				break;
 			}
 			else if(!myBuilder.newlineBeforeCurrentToken())
 			{
 				errorUntil("Unexpected tokens (use ';' to separate expressions on the same line)", TokenSet.create(JetTokens.EOL_OR_SEMICOLON));
 			}
 		}
 	}
 
 	/*
 		 * statement
 		 *  : expression
 		 *  : declaration
 		 *  ;
 		 */
 	private void parseStatement()
 	{
 		if(!parseLocalDeclaration())
 		{
 			if(!atSet(EXPRESSION_FIRST))
 			{
 				errorAndAdvance("Expecting a statement");
 			}
 			else
 			{
 				parseExpression();
 			}
 		}
 	}
 
 	/*
 		 * declaration
 		 *   : function
 		 *   : property
 		 *   : extension
 		 *   : class
 		 *   : typedef
 		 *   : object
 		 *   ;
 		 */
 	private IElementType parseLocalDeclarationRest()
 	{
 		IElementType keywordToken = tt();
 		IElementType declType = null;
		if(keywordToken == JetTokens.CLASS_KEYWORD || keywordToken == JetTokens.ENUM_KEYWORD)
 		{
 			declType = myJetParsing.parseClass();
 		}
 		else if(keywordToken == JetTokens.METH_KEYWORD)
 		{
 			declType = myJetParsing.parseMethod();
 		}
 		else if(keywordToken == JetTokens.VAL_KEYWORD || keywordToken == JetTokens.VAR_KEYWORD)
 		{
 			declType = myJetParsing.parseProperty(true);
 		}
 		return declType;
 	}
 
 	/*
 		 * doWhile
 		 *   : "do" element "while" "(" element ")"
 		 *   ;
 		 */
 	private void parseDoWhile()
 	{
 		assert _at(JetTokens.DO_KEYWORD);
 
 		PsiBuilder.Marker loop = mark();
 
 		advance(); // DO_KEYWORD
 
 		if(!at(JetTokens.WHILE_KEYWORD))
 		{
 			parseControlStructureBody();
 		}
 
 		if(expect(JetTokens.WHILE_KEYWORD, "Expecting 'while' followed by a post-condition"))
 		{
 			parseCondition();
 		}
 
 		loop.done(DO_WHILE);
 	}
 
 	/*
 		 * while
 		 *   : "while" "(" element ")" element
 		 *   ;
 		 */
 	private void parseWhile()
 	{
 		assert _at(JetTokens.WHILE_KEYWORD);
 
 		PsiBuilder.Marker loop = mark();
 
 		advance(); // WHILE_KEYWORD
 
 		parseCondition();
 
 		parseControlStructureBody();
 
 		loop.done(WHILE);
 	}
 
 	/*
 		 * for
 		 *   : "for" "(" attributes valOrVar? SimpleName (":" type)? "in" element ")" element
 		 *   ;
 		 *
 		 *   TODO: empty loop body (at the end of the block)?
 		 */
 	private void parseFor()
 	{
 		assert _at(JetTokens.FOR_KEYWORD);
 
 		PsiBuilder.Marker loop = mark();
 
 		advance(); // FOR_KEYWORD
 
 		myBuilder.disableNewlines();
 		expect(JetTokens.LPAR, "Expecting '(' to open a loop range", TokenSet.create(JetTokens.RPAR, JetTokens.VAL_KEYWORD, JetTokens.VAR_KEYWORD, JetTokens.IDENTIFIER));
 
 		PsiBuilder.Marker parameter = mark();
 		if(at(JetTokens.VAL_KEYWORD) || at(JetTokens.VAR_KEYWORD))
 			advance(); // VAL_KEYWORD or VAR_KEYWORD
 		if(!myJetParsing.parseIdeTemplate())
 		{
 			expect(JetTokens.IDENTIFIER, "Expecting a variable name", TokenSet.create(JetTokens.COLON));
 		}
 		if(at(JetTokens.COLON))
 		{
 			advance(); // COLON
 			myJetParsing.parseTypeRef();
 		}
 		parameter.done(LOOP_PARAMETER);
 
 		expect(JetTokens.IN_KEYWORD, "Expecting 'in'");
 
 		PsiBuilder.Marker range = mark();
 		parseExpression();
 		range.done(LOOP_RANGE);
 
 		expectNoAdvance(JetTokens.RPAR, "Expecting ')'");
 		myBuilder.restoreNewlinesState();
 
 		parseControlStructureBody();
 
 		loop.done(FOR);
 	}
 
 	/**
 	 * If it has no ->, it's a block, otherwise a function literal
 	 */
 	private void parseExpressionPreferringBlocks()
 	{
 		if(at(JetTokens.LBRACE))
 		{
 			parseFunctionLiteral(true);
 		}
 		else if(atSet(JetTokens.LABELS) && lookahead(1) == JetTokens.LBRACE)
 		{
 			PsiBuilder.Marker mark = mark();
 
 			parseOperationReference();
 
 			parseFunctionLiteral(true);
 
 			mark.done(PREFIX_EXPRESSION);
 		}
 		else
 		{
 			parseExpression();
 		}
 	}
 
 	/*
 		 * element
 		 */
 	private void parseControlStructureBody()
 	{
 		PsiBuilder.Marker body = mark();
 		if(!at(JetTokens.SEMICOLON))
 		{
 			parseExpressionPreferringBlocks();
 		}
 		body.done(BODY);
 	}
 
 	/*
 		 * try
 		 *   : "try" block catchBlock* finallyBlock?
 		 *   ;
 		 * catchBlock
 		 *   : "catch" "(" attributes SimpleName ":" userType ")" block
 		 *   ;
 		 *
 		 * finallyBlock
 		 *   : "finally" block
 		 *   ;
 		 */
 	private void parseTry()
 	{
 		assert _at(JetTokens.TRY_KEYWORD);
 
 		PsiBuilder.Marker tryExpression = mark();
 
 		advance(); // TRY_KEYWORD
 
 		myJetParsing.parseBlock();
 
 		boolean catchOrFinally = false;
 		while(at(JetTokens.CATCH_KEYWORD))
 		{
 			catchOrFinally = true;
 			PsiBuilder.Marker catchBlock = mark();
 			advance(); // CATCH_KEYWORD
 
 			myJetParsing.parseValueParameterList(false, TokenSet.create(JetTokens.LBRACE, JetTokens.FINALLY_KEYWORD, JetTokens.CATCH_KEYWORD));
 
 			myJetParsing.parseBlock();
 			catchBlock.done(CATCH);
 		}
 
 		if(at(JetTokens.FINALLY_KEYWORD))
 		{
 			catchOrFinally = true;
 			PsiBuilder.Marker finallyBlock = mark();
 
 			advance(); // FINALLY_KEYWORD
 
 			myJetParsing.parseBlock();
 
 			finallyBlock.done(FINALLY);
 		}
 
 		if(!catchOrFinally)
 		{
 			error("Expecting 'catch' or 'finally'");
 		}
 
 		tryExpression.done(TRY);
 	}
 
 	/*
 		 * if
 		 *   : "if" "(" element ")" element SEMI? ("else" element)?
 		 *   ;
 		 */
 	private void parseIf()
 	{
 		assert _at(JetTokens.IF_KEYWORD);
 
 		PsiBuilder.Marker marker = mark();
 
 		advance(); //IF_KEYWORD
 
 		parseCondition();
 
 		PsiBuilder.Marker thenBranch = mark();
 		if(!at(JetTokens.ELSE_KEYWORD) && !at(JetTokens.SEMICOLON))
 		{
 			parseExpressionPreferringBlocks();
 		}
 		if(at(JetTokens.SEMICOLON) && lookahead(1) == JetTokens.ELSE_KEYWORD)
 		{
 			advance(); // SEMICOLON
 		}
 		thenBranch.done(THEN);
 
 		if(at(JetTokens.ELSE_KEYWORD))
 		{
 			advance(); // ELSE_KEYWORD
 
 			PsiBuilder.Marker elseBranch = mark();
 			if(!at(JetTokens.SEMICOLON))
 			{
 				parseExpressionPreferringBlocks();
 			}
 			elseBranch.done(ELSE);
 		}
 
 		marker.done(IF);
 	}
 
 	/*
 		 * "(" element ")"
 		 */
 	private void parseCondition()
 	{
 		myBuilder.disableNewlines();
 		expect(JetTokens.LPAR, "Expecting a condition in parentheses '(...)'");
 
 		PsiBuilder.Marker condition = mark();
 		parseExpression();
 		condition.done(CONDITION);
 
 		expect(JetTokens.RPAR, "Expecting ')");
 		myBuilder.restoreNewlinesState();
 	}
 
 	/*
 		 * : "continue" getEntryPoint?
 		 * : "break" getEntryPoint?
 		 */
 	private void parseJump(NapileNodeType type)
 	{
 		assert _at(JetTokens.BREAK_KEYWORD) || _at(JetTokens.CONTINUE_KEYWORD);
 
 		PsiBuilder.Marker marker = mark();
 
 		advance(); // BREAK_KEYWORD or CONTINUE_KEYWORD
 
 		parseLabel();
 
 		marker.done(type);
 	}
 
 	/*
 		 * "return" getEntryPoint? element?
 		 */
 	private void parseReturn()
 	{
 		assert _at(JetTokens.RETURN_KEYWORD);
 
 		PsiBuilder.Marker returnExpression = mark();
 
 		advance(); // RETURN_KEYWORD
 
 		parseLabel();
 
 		if(atSet(EXPRESSION_FIRST) && !at(JetTokens.EOL_OR_SEMICOLON))
 			parseExpression();
 
 		returnExpression.done(RETURN);
 	}
 
 	/*
 		 * labels
 		 */
 	private void parseLabel()
 	{
 		if(!eol() && atSet(JetTokens.LABELS))
 		{
 			PsiBuilder.Marker labelWrap = mark();
 
 			PsiBuilder.Marker mark = mark();
 			advance(); // LABELS
 			mark.done(LABEL_REFERENCE);
 
 			labelWrap.done(LABEL_QUALIFIER);
 		}
 	}
 
 	/*
 		 * : "throw" element
 		 */
 	private void parseThrow()
 	{
 		assert _at(JetTokens.THROW_KEYWORD);
 
 		PsiBuilder.Marker marker = mark();
 
 		advance(); // THROW_KEYWORD
 
 		parseExpression();
 
 		marker.done(THROW);
 	}
 
 	/*
 		 * "(" expression ")"
 		 */
 	private void parseParenthesizedExpression()
 	{
 		assert _at(JetTokens.LPAR);
 
 		PsiBuilder.Marker mark = mark();
 
 		myBuilder.disableNewlines();
 		advance(); // LPAR
 		if(at(JetTokens.RPAR))
 		{
 			error("Expecting an expression");
 		}
 		else
 		{
 			parseExpression();
 		}
 
 		expect(JetTokens.RPAR, "Expecting ')'");
 		myBuilder.restoreNewlinesState();
 
 		mark.done(PARENTHESIZED);
 	}
 
 
 	/*
 		 * "this" label?
 		 */
 	private void parseThisExpression()
 	{
 		assert _at(JetTokens.THIS_KEYWORD);
 		PsiBuilder.Marker mark = mark();
 
 		PsiBuilder.Marker thisReference = mark();
 		advance(); // THIS_KEYWORD
 		thisReference.done(REFERENCE_EXPRESSION);
 
 		parseLabel();
 
 		mark.done(THIS_EXPRESSION);
 	}
 
 	/*
 		 * "this" ("<" type ">")? label?
 		 */
 	private void parseSuperExpression()
 	{
 		assert _at(JetTokens.SUPER_KEYWORD);
 		PsiBuilder.Marker mark = mark();
 
 		PsiBuilder.Marker superReference = mark();
 		advance(); // SUPER_KEYWORD
 		superReference.done(REFERENCE_EXPRESSION);
 
 		if(at(JetTokens.LT))
 		{
 			// This may be "super < foo" or "super<foo>", thus the backtracking
 			PsiBuilder.Marker supertype = mark();
 
 			myBuilder.disableNewlines();
 			advance(); // LT
 
 			myJetParsing.parseTypeRef();
 
 			if(at(JetTokens.GT))
 			{
 				advance(); // GT
 				supertype.drop();
 			}
 			else
 			{
 				supertype.rollbackTo();
 			}
 			myBuilder.restoreNewlinesState();
 		}
 		parseLabel();
 
 		mark.done(SUPER_EXPRESSION);
 	}
 
 	/*
 		 * valueArguments
 		 *   : "(" (SimpleName "=")? "*"? element{","} ")"
 		 *   ;
 		 */
 	public void parseValueArgumentList()
 	{
 		PsiBuilder.Marker list = mark();
 
 		myBuilder.disableNewlines();
 		expect(JetTokens.LPAR, "Expecting an argument list", EXPRESSION_FOLLOW);
 
 		if(!at(JetTokens.RPAR))
 		{
 			while(true)
 			{
 				while(at(JetTokens.COMMA))
 					errorAndAdvance("Expecting an argument");
 				parseValueArgument();
 				if(!at(JetTokens.COMMA))
 					break;
 				advance(); // COMMA
 				if(at(JetTokens.RPAR))
 				{
 					error("Expecting an argument");
 					break;
 				}
 			}
 		}
 
 		expect(JetTokens.RPAR, "Expecting ')'", EXPRESSION_FOLLOW);
 		myBuilder.restoreNewlinesState();
 
 		list.done(VALUE_ARGUMENT_LIST);
 	}
 
 	/*
 		 * (SimpleName "=")? "*"? element
 		 */
 	private void parseValueArgument()
 	{
 		PsiBuilder.Marker argument = mark();
 		if(at(JetTokens.IDENTIFIER) && lookahead(1) == JetTokens.EQ)
 		{
 			PsiBuilder.Marker argName = mark();
 			PsiBuilder.Marker reference = mark();
 			advance(); // IDENTIFIER
 			reference.done(REFERENCE_EXPRESSION);
 			argName.done(VALUE_ARGUMENT_NAME);
 			advance(); // EQ
 		}
 		if(at(JetTokens.MUL))
 		{
 			advance(); // MUL
 		}
 		parseExpression();
 		argument.done(VALUE_ARGUMENT);
 	}
 
 	/*
 		 * "object" (":" delegationSpecifier{","})? classBody // Cannot make class body optional: foo(object : F, A)
 		 */
 	public void parseObjectLiteral()
 	{
 		PsiBuilder.Marker literal = mark();
 		PsiBuilder.Marker declaration = mark();
 		myJetParsing.parseObject(); // Body is not optional because of foo(object : A, B)
 		declaration.done(ANONYM_CLASS);
 		literal.done(OBJECT_LITERAL);
 	}
 
 	private void parseOneTokenExpression(NapileNodeType type)
 	{
 		PsiBuilder.Marker mark = mark();
 		advance();
 		mark.done(type);
 	}
 
 	@Override
 	protected JetParsing create(SemanticWhitespaceAwarePsiBuilder builder)
 	{
 		return myJetParsing.create(builder);
 	}
 
 	private boolean interruptedWithNewLine()
 	{
 		return !ALLOW_NEWLINE_OPERATIONS.contains(tt()) && myBuilder.newlineBeforeCurrentToken();
 	}
 }
