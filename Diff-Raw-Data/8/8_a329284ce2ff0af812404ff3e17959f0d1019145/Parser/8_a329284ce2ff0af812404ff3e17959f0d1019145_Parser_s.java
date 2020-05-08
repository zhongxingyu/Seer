 ////////////////////////////////////////////////////////////////////////////////
 //
 //  ADOBE SYSTEMS INCORPORATED
 //  Copyright 2004-2007 Adobe Systems Incorporated
 //  All Rights Reserved.
 //
 //  NOTICE: Adobe permits you to use, modify, and distribute this file
 //  in accordance with the terms of the license agreement accompanying it.
 //
 ////////////////////////////////////////////////////////////////////////////////
 
 /*
  * Written by Jeff Dyer
  * Copyright (c) 1998-2003 Mountain View Compiler Company
  * All rights reserved.
  */
 
 package macromedia.asc.parser;
 
 import java.io.*;
 import java.util.HashSet;
 import java.util.ListIterator;
 
 import macromedia.asc.util.*;
 import static macromedia.asc.parser.Tokens.*;
 import static macromedia.asc.embedding.avmplus.RuntimeConstants.*;
 import static macromedia.asc.embedding.avmplus.Features.*;
 import static macromedia.asc.embedding.ErrorConstants.*;
 import macromedia.asc.embedding.CompilerHandler;
 import static macromedia.asc.parser.States.*;
 
 /**
  * Parse ECMAScript programs.
  *
  * @author Jeff Dyer
  */
 public final class Parser
 {
     private static final boolean debug = false;
 
 
     private static final int abbrevIfElse_mode       = ELSE_TOKEN;  // lookahead uses this value. don't change.
     private static final int abbrevDoWhile_mode      = WHILE_TOKEN; // ditto.
     private static final int abbrevFunction_mode     = FUNCTION_TOKEN;
     private static final int abbrev_mode             = LAST_TOKEN - 1;
     private static final int full_mode               = abbrev_mode - 1;
 
     private static final int allowIn_mode            = full_mode - 1;
     private static final int noIn_mode               = allowIn_mode - 1;
     private static final int catch_parameter_error   = 1;
     private static final int syntax_xml_error        = 2;
 
     private static final int syntax_error            = 7;
     private static final int expression_syntax_error = 8;
     private static final int syntax_eos_error        = 9;
 
     private static final int xmlid_tokens[] = 
               { // include all keyword tokens and the identifier token
                 // I moved IDENTIFIER to the top because it's the most common
                   IDENTIFIER_TOKEN,
                   ABSTRACT_TOKEN,
                   AS_TOKEN,
                   BREAK_TOKEN,
                   CASE_TOKEN,
                   CATCH_TOKEN,
                   CLASS_TOKEN,
                   CONST_TOKEN,
                   CONTINUE_TOKEN,
                   DEBUGGER_TOKEN,
                   DEFAULT_TOKEN,
                   DELETE_TOKEN,
                   DO_TOKEN,
                   ELSE_TOKEN,
                   ENUM_TOKEN,
                   EXTENDS_TOKEN,
                   FALSE_TOKEN,
                   FINAL_TOKEN,
                   FINALLY_TOKEN,
                   FOR_TOKEN,
                   FUNCTION_TOKEN,
                   GET_TOKEN,
                   GOTO_TOKEN,
                   IF_TOKEN,
                   IMPLEMENTS_TOKEN,
                   IMPORT_TOKEN,
                   IN_TOKEN,
                   INCLUDE_TOKEN,
                   INSTANCEOF_TOKEN,
                   INTERFACE_TOKEN,
                   IS_TOKEN,
                   NAMESPACE_TOKEN,
                   CONFIG_TOKEN,
                   NATIVE_TOKEN,
                   NEW_TOKEN,
                   NULL_TOKEN,
                   PACKAGE_TOKEN,
                   PRIVATE_TOKEN,
                   PROTECTED_TOKEN,
                   PUBLIC_TOKEN,
                   RETURN_TOKEN,
                   SET_TOKEN,
                   STATIC_TOKEN,
                   SUPER_TOKEN,
                   SWITCH_TOKEN,
                   SYNCHRONIZED_TOKEN,
                   THIS_TOKEN,
                   THROW_TOKEN,
                   THROWS_TOKEN,
                   TRANSIENT_TOKEN,
                   TRUE_TOKEN,
                   TRY_TOKEN,
                   TYPEOF_TOKEN,
                   USE_TOKEN,
                   VAR_TOKEN,
                   VOID_TOKEN,
                   VOLATILE_TOKEN,
                   WHILE_TOKEN,
                   WITH_TOKEN,
               };
     static final int xmlid_tokens_count = xmlid_tokens.length;
 
     private int lastToken;
     private int nexttoken;
     private int thisToken;
     private Context ctx;
     private NodeFactory nodeFactory;
     private boolean create_doc_info;
     private boolean save_comment_nodes;
     public Scanner scanner;
 
     private String encoding;
 
     // keeps track of the type of the currently evaluating member expressions base
     public ObjectList<Node>    comments = new ObjectList<Node>(); // all comments encountered while parsing are placed here, rather than in the parse tree
     public IntList block_kind_stack = new IntList();
     public String current_class_name = null;
     
     private boolean within_package;
 
     private boolean parsing_include = false;
 
     private ObjectList< HashSet<String> > config_namespaces = new ObjectList< HashSet<String> >();
     /*
      * Log a syntax error and recover
      */
 
     Node error(int errCode)                          { return error(syntax_error, errCode,"","",-1); }
     private Node error(int kind, int errCode)                { return error(kind,errCode,"","",-1); }
     private Node error(int kind, int errCode, String arg1) { return error(kind,errCode,arg1,"",-1); }
     private Node error(int kind, int errCode, String arg1, String arg2) { return error(kind,errCode,arg1,arg2,-1); }
     private Node error(int kind, int errCode, String arg1, String arg2,int pos)
     {
         String origin = this.scanner.input.origin;
         StringBuffer out = new StringBuffer();
         
         if(debug) out.append("[Parser] ");
         
         // Just the arguments for sanities, no message (since they chang often)
         if(ContextStatics.useSanityStyleErrors)
         {
             out.append("code=" + errCode + "; arg1=" + arg1 + "; arg2=" + arg2);
         }
         else
         {
             String msg = ctx.shellErrorString(errCode);
             int nextLoc = Context.replaceStringArg(out, msg, 0, arg1);
             nextLoc = Context.replaceStringArg(out, msg, nextLoc, arg2);
             if (nextLoc != -1) // append msg remainder after replacement point, if any
                 out.append(msg.substring(nextLoc, msg.length()));
         }
 
         int lineno;
         int column;
         if( pos < 0 )
         {
             lineno = scanner.input.markLn + 1;
             column = scanner.input.markCol;
             pos = scanner.input.positionOfMark();
 
         }
         else
         {
             lineno = scanner.input.getLnNum(pos);
             column = scanner.input.getColPos(pos);
         }
 
         if (kind == syntax_error || kind == syntax_eos_error )
         {
             ctx.localizedError(origin, lineno, column, out.toString(), scanner.input.getLineText(pos), errCode);
         }
         else
         {
             ctx.localizedError(origin, lineno, column, out.toString(), "", errCode);
         }
 
         //skiperror(tokenid);
         return null;
     }
 
     /*
      * skip ahead after an error is detected. this simply goes until the next
      * whitespace or end of input.
      */
 
     private void skiperror()
     {
         skiperror(syntax_error);
     }
 
     private void skiperror(int kind)
     {
         // If kind is < 0 then it is a token class to be advanced to,
         // Otherwise it is a error category.
 
         if (kind < 0)
         {
             while (true)
             {
                 // nexttoken is the same variable used by lookahead
 
                 if (nexttoken == kind)
                 {
                     if( kind == RIGHTBRACE_TOKEN )
                     {
                         int size = block_kind_stack.size();
                         block_kind_stack.clear();
                         for( ; size > 0; --size)
                         {
                            block_kind_stack.add(ERROR_TOKEN);    // ignore attribute errors since blocks are probably messed up
                         }
                     }
                     break;
                 }
                 else
                 if( nexttoken == EOS_TOKEN )
                 {
                     break;
                 }
                 else
                 if( nexttoken == SEMICOLON_TOKEN && kind != RIGHTBRACE_TOKEN ) // don't stop eating until right brace is found
                 {
                     break;
                 }
                 else
                 if( nexttoken == LEFTBRACE_TOKEN && kind != RIGHTBRACE_TOKEN ) // don't stop eating until right brace is found
                 {
                     scanner.retract();
                     break;
                 }
                 else
                 if( nexttoken == RIGHTBRACE_TOKEN )
                 {
                     scanner.retract();
                     break;
                 }
                 nexttoken = getNextToken();
             }
         }
         else
         {
             switch (kind)
             {
                 case catch_parameter_error:
                     while (true)
                     {
                         nexttoken = getNextToken();
                         if (nexttoken == RIGHTPAREN_TOKEN || nexttoken == EOS_TOKEN)
                         {
                             break;
                         }
                     }
                     break;
                 case syntax_xml_error:
                 case syntax_eos_error:
                     while (true)
                     {
                         nexttoken = getNextToken();
                         if (nexttoken == EOS_TOKEN)
                         {
                             break;
                         }
                     }
                     break;
                 case syntax_error:
                 case expression_syntax_error:
                 default:
                     do
                     {
                         if (nexttoken == LEFTPAREN_TOKEN || nexttoken == RIGHTPAREN_TOKEN ||
                             nexttoken == LEFTBRACE_TOKEN || nexttoken == RIGHTBRACE_TOKEN ||
                             nexttoken == LEFTBRACKET_TOKEN || nexttoken == RIGHTBRACKET_TOKEN ||
                             nexttoken == COMMA_TOKEN || nexttoken == SEMICOLON_TOKEN ||
                             nexttoken == EOS_TOKEN)
                         {
                             break;
                         }
                         nexttoken = getNextToken();
                     }
                     while (true);
                     break;
             }
         }
         lastToken = ERROR_TOKEN;
     }
 
 	private void init(Context cx, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
 	{
 		ctx = cx;
 		lastToken = EMPTY_TOKEN;
 		nexttoken = EMPTY_TOKEN;
 		thisToken = EMPTY_TOKEN;
 		create_doc_info = emit_doc_info;
         within_package = false;
 		this.save_comment_nodes = save_comment_nodes;
 		nodeFactory = cx.getNodeFactory();
 		nodeFactory.createDefaultDocComments(emit_doc_info); // for now, always create defualt comments for uncommented nodes in -doc
 		if( block_kind_stack != null )
 		{
 		    this.block_kind_stack.addAll(block_kind_stack);
 		}
 		else
 		{
 		    this.block_kind_stack.add(EMPTY_TOKEN);  // set initial state
 		}
 		cx.parser = this;
 		cx.setOrigin(origin);
 	}
 
     public Parser(Context cx, InputStream in, String origin)
     {
         this(cx, in, origin, null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding)
     {
         init(cx, origin, false, false, null);
         scanner = new Scanner(cx, in, encoding, origin);
         this.encoding = encoding;
     }
 
     public Parser(Context cx, InputStream in, String origin, boolean emit_doc_info, boolean save_comment_nodes)
     {
         this(cx, in, origin, null, emit_doc_info, save_comment_nodes,null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes)
     {
         this(cx, in, origin, encoding, emit_doc_info, save_comment_nodes,null);
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
     {
 	    init(cx, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
         scanner = new Scanner(cx, in, encoding, origin);
         this.encoding = encoding;
     }
 
     public Parser(Context cx, InputStream in, String origin, String encoding, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack, boolean is_include)
     {
         this(cx, in, origin,encoding, emit_doc_info, save_comment_nodes, block_kind_stack);
         this.parsing_include = is_include;
     }
 
 	public Parser(Context cx, String in, String origin)
 	{
 		init(cx, origin, false, false, null);
 	    scanner = new Scanner(cx, in, origin);
 	}
 
 	public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes)
 	{
 	    this(cx, in, origin, emit_doc_info, save_comment_nodes,null);
 	}
 
 	public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack)
 	{
 		init(cx, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
 	    scanner = new Scanner(cx, in, origin);
 	}
 
     public Parser(Context cx, String in, String origin, boolean emit_doc_info, boolean save_comment_nodes, IntList block_kind_stack, boolean is_include)
     {
         this(cx, in, origin, emit_doc_info, save_comment_nodes, block_kind_stack);
         this.parsing_include = is_include;
 
     }
 
     public boolean newline()
     {
         if (nexttoken == EMPTY_TOKEN)
         {
             nexttoken = getNextToken();
         }
         return scanner.followsLineTerminator();
     }
 
     /*
      * Match the current input with an expected token. lookahead is managed by
      * setting the state of this.nextToken to EMPTY_TOKEN after an match is
      * attempted. the next lookahead will initialize it again.
      */
 
     public int match(int expectedTokenClass)
     {
         int result = ERROR_TOKEN;
 
         if (!lookahead(expectedTokenClass))
         {
             if (!lookahead(ERROR_TOKEN))
             {
                 if (expectedTokenClass == EOS_TOKEN)
                 {
                     if (ctx.errorCount() == 0)  // only if this is the first error.
                     {
                         error(syntax_error, kError_Parser_ExtraCharsAfterEndOfProgram);
                     }
                     // otherwise, don't say anything.
                     skiperror(expectedTokenClass);
                     result = nexttoken;
                     nexttoken = EMPTY_TOKEN;
                 }
                 else if (nexttoken == EOS_TOKEN)
                 {
                     error(syntax_eos_error, kError_Parser_ExpectedToken,
                           Token.getTokenClassName(expectedTokenClass),
                           scanner.getTokenText(nexttoken));
                     skiperror(expectedTokenClass);
                     result = nexttoken;
                 }
                 else
                 {
                     error(syntax_eos_error, kError_Parser_ExpectedToken,
                         Token.getTokenClassName(expectedTokenClass),
                         scanner.getTokenText(nexttoken));
                     skiperror(expectedTokenClass);
                     result = nexttoken;
                     nexttoken = EMPTY_TOKEN;
                 }
             }
             else
             {
                 result = nexttoken;
                 nexttoken = EMPTY_TOKEN;
             }
         }
         else if (expectedTokenClass != scanner.getTokenClass(nexttoken))
         {
             result = thisToken;
         }
         else
         {
             result = nexttoken;
             lastToken = nexttoken;
             nexttoken = EMPTY_TOKEN;
         }
 
         return result;
     }
 
     int match( final int expectedTokenClasses[], int count )
     {
         int result = ERROR_TOKEN;
 
         if( !lookahead( expectedTokenClasses, count ) )
         {
             if( !lookahead(ERROR_TOKEN) )
             {
                 error(syntax_error, kError_Parser_ExpectedToken,
                 Token.getTokenClassName(expectedTokenClasses[0]),
                 scanner.getTokenText(nexttoken));
                 skiperror(expectedTokenClasses[0]);
             }
             result    = nexttoken;
             nexttoken = EMPTY_TOKEN;
         }
         else
         {
             result    = nexttoken;
             lastToken = nexttoken;
             nexttoken = EMPTY_TOKEN;
         }
 
         return result;
     }
 
     private boolean seen_it = false;
 
     /*
      * Match the current input with an expected token. lookahead is managed by
      * setting the state of this.nextToken to EMPTY_TOKEN after an match is
      * attempted. the next lookahead will initialize it again.
      */
 
     public boolean lookaheadSemicolon(int mode)
     {
 
         boolean result = false;
 
         if (lookahead(SEMICOLON_TOKEN) ||
             lookahead(EOS_TOKEN) ||
             lookahead(RIGHTBRACE_TOKEN) ||
             lookahead(mode) ||
             scanner.followsLineTerminator())
         {
             result = true;
         }
 
         return result;
     }
 
     public boolean lookaheadNoninsertableSemicolon(int mode)
     {
 
         boolean result = false;
 
         if (lookahead(EOS_TOKEN) ||
             lookahead(RIGHTBRACE_TOKEN) ||
             lookahead(mode))
         {
             result = true;
         }
 
         return result;
     }
 
     public int matchSemicolon(int mode)
     {
 
         int result = ERROR_TOKEN;
 
         if (lookahead(SEMICOLON_TOKEN))
         {
             result = match(SEMICOLON_TOKEN);
         }
         else if (scanner.followsLineTerminator() ||
             lookahead(EOS_TOKEN) ||
             lookahead(RIGHTBRACE_TOKEN))
         {
             result = SEMICOLON_TOKEN;
         }
         else if ((mode == abbrevIfElse_mode || mode == abbrevDoWhile_mode)
             && lookahead(mode))
         {
             result = SEMICOLON_TOKEN;
         }
         else if (!lookahead(ERROR_TOKEN))
         {
             if (mode == abbrevFunction_mode)
             {
                 error(syntax_error, kError_Parser_ExpectedLeftBrace);
                 skiperror(LEFTBRACE_TOKEN);
             }
             else
             {
                 error(syntax_error, kError_Parser_ExpectedSemicolon, scanner.getTokenText(nexttoken));
                 skiperror(SEMICOLON_TOKEN);
             }
         }
 
         return result;
     }
 
     /*
      * Match a non-insertable semi-colon. This function looks for
      * a SEMICOLON_TOKEN or other grammatical markers that indicate
      * that the EMPTY_TOKEN is equivalent to a semicolon.
      */
 
     public int matchNoninsertableSemicolon(int mode)
     {
 
         int result = ERROR_TOKEN;
 
         if (lookahead(SEMICOLON_TOKEN))
         {
             result = match(SEMICOLON_TOKEN);
         }
         else if (lookahead(EOS_TOKEN) || lookahead(RIGHTBRACE_TOKEN))
         {
             result = SEMICOLON_TOKEN;
         }
         else if ((mode == abbrevIfElse_mode || mode == abbrevDoWhile_mode) &&
             lookahead(mode))
         {
             result = SEMICOLON_TOKEN;
         }
         else
         {
             error(syntax_error, kError_Parser_ExpectedSemicolon, scanner.getTokenText(nexttoken));
             skiperror(SEMICOLON_TOKEN);
         }
 
         return result;
     }
 
     /*
      * Lookahead to the next token.
      */
 
     public boolean lookahead(int expectedTokenClass)
     {
         // If the nextToken is EMPTY_TOKEN, then fetch another. This is the first
         // lookahead since the last match.
         if (nexttoken == EMPTY_TOKEN)
         {
             nexttoken = getNextToken();
             //printf("%s ",Token::getTokenClassName(scanner.getTokenClass(nextToken)).c_str());
             if (debug)
             {
                 System.err.println("\tnextToken() returning " + Token.getTokenClassName(scanner.getTokenClass(nexttoken)));
             }
 
             // Check for invalid token.
 
             if (nexttoken == ERROR_TOKEN)
             {
                 //error(syntax_error,kError_Parser_InvalidWord);
                 //nextToken = getNextToken();
             }
         }
 
         if (debug)
         {
             // System.err.println("\t" + Token::getTokenClassName(scanner.getTokenClass(nextToken)) + " lookahead(" + Token::getTokenClassName(expectedTokenClass) + ")");
             System.err.println("\t" + Token.getTokenClassName(scanner.getTokenClass(nexttoken)) + " lookahead(" + Token.getTokenClassName(expectedTokenClass) + ")");
         }
 
         // Compare the expected token class against the token class of
         // the nextToken.
 
         if (expectedTokenClass != scanner.getTokenClass(nexttoken))
         {
             return false;
         }
 
         thisToken = expectedTokenClass;
         return true;
     }
 
     /*
      * This wrapper to scanner->nexttoken filters out all non-doccomment comments from the
      *  scanner output and collects all comments (including doccomments) in the commentTable.
      *  The commentTable can be used by external compiler toolchain tools to detect where
      *  comments are located in the source while walking the parse-tree.
      */
     int getNextToken()
     {
         int tok = scanner.nexttoken(true);
         int tok_type = scanner.getTokenClass(tok);
         while(tok_type == SLASHSLASHCOMMENT_TOKEN || tok_type == BLOCKCOMMENT_TOKEN
             || tok_type == DOCCOMMENT_TOKEN)
         {
             if( save_comment_nodes && (!ctx.scriptAssistParsing || tok_type != DOCCOMMENT_TOKEN))
             {
                 Node newComment = nodeFactory.comment(scanner.getTokenText(tok), tok_type, ctx.input.positionOfMark());
                 comments.push_back(newComment);
             }
 
             if (tok_type == DOCCOMMENT_TOKEN && create_doc_info) // return doccomment tokens if create_doc_info, skip normal comment tokens)
                 break;
 
             tok = scanner.nexttoken(false);
             tok_type = scanner.getTokenClass(tok);
         }
 
         return tok;
     }
 
     boolean lookahead( final int expectedTokenClasses[], int count )
     {
         // If the nexttoken is empty_token, then fetch another. This is the first
         // lookahead since the last match.
 
         if( nexttoken == EMPTY_TOKEN )
         {
             nexttoken = getNextToken();
 
             // Check for invalid token.
 
             if( nexttoken == ERROR_TOKEN )
             {
                 //error(syntax_error,kError_Parser_InvalidWord);
                 //nexttoken = getNextToken();
             }
         }
 
         // Compare the expected token class against the token class of
         // the nexttoken.
 
         int tokenclass = scanner.getTokenClass(nexttoken);
         int i = 0;
         for( ; i < count; ++i )
         {
             if( expectedTokenClasses[i] == tokenclass )
             {
                 thisToken = tokenclass;
                 return true;
             }
         }
 
         return false;
     }
 
 
     /*
      * Start grammar.
      */
 
     /*
      * Identifier
      */
 
     public IdentifierNode parseIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseIdentifier");
         }
 
         IdentifierNode result = null;
 
         if (lookahead(GET_TOKEN))
         {
             match(GET_TOKEN);
             result = nodeFactory.identifier("get",ctx.input.positionOfMark());
         }
         else if (lookahead(SET_TOKEN))
         {
             match(SET_TOKEN);
             result = nodeFactory.identifier("set",ctx.input.positionOfMark());
         }
         else if (lookahead(NAMESPACE_TOKEN))
         {
             match(NAMESPACE_TOKEN);
             result = nodeFactory.identifier("namespace",ctx.input.positionOfMark());
         }
         else
         {
             result = nodeFactory.identifier(scanner.getTokenText(match(IDENTIFIER_TOKEN)),ctx.input.positionOfMark());
         }
 
         if (debug)
         {
             System.err.println("finish parseIdentifier");
         }
 
         return result;
 
     }
 
     /*
      * PropertyIdentifier
      */
 
     public IdentifierNode parsePropertyIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parsePropertyIdentifier");
         }
 
         IdentifierNode result = null;
 
         if (lookahead(DEFAULT_TOKEN))
         {
             match(DEFAULT_TOKEN);
             result = nodeFactory.identifier("default",ctx.input.positionOfMark());
         }
         else if (lookahead(GET_TOKEN))
         {
             match(GET_TOKEN);
             result = nodeFactory.identifier("get",ctx.input.positionOfMark());
         }
         else if (lookahead(SET_TOKEN))
         {
             match(SET_TOKEN);
             result = nodeFactory.identifier("set",ctx.input.positionOfMark());
         }
         else if (lookahead(NAMESPACE_TOKEN))
         {
             match(NAMESPACE_TOKEN);
             result = nodeFactory.identifier("namespace",ctx.input.positionOfMark());
         }
         else if (HAS_WILDCARDSELECTOR && lookahead(MULT_TOKEN))
         {
             match(MULT_TOKEN);
             result = nodeFactory.identifier("*",ctx.input.positionOfMark());
         }
         else
         {
 //            result = nodeFactory.identifier(scanner.getTokenText(match(IDENTIFIER_TOKEN)),ctx.input.positionOfMark());
             int pos = ctx.input.positionOfMark();
             String id = scanner.getTokenText(match(IDENTIFIER_TOKEN));
             result = nodeFactory.identifier(id,pos);
         }
 
         if (debug)
         {
             System.err.println("finish parsePropertyIdentifier");
         }
 
         return result;
 
     }
 
     /*
      * Qualifier
      */
 
     public IdentifierNode parseQualifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseQualifier");
         }
 
         IdentifierNode result;
 
         if (lookahead(PUBLIC_TOKEN))
         {
             match(PUBLIC_TOKEN);
             result = nodeFactory.identifier("public",ctx.input.positionOfMark());
         }
         else if (lookahead(PRIVATE_TOKEN))
         {
             match(PRIVATE_TOKEN);
             result = nodeFactory.identifier("private",ctx.input.positionOfMark());
         }
         else if (lookahead(PROTECTED_TOKEN))
         {
             match(PROTECTED_TOKEN);
             result = nodeFactory.identifier("protected",ctx.input.positionOfMark());
         }
         else
         {
             result = parsePropertyIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseQualifier");
         }
 
         return result;
     }
 
     /*
      * SimpleQualifiedIdentifier
      */
 
     public IdentifierNode parseSimpleQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseSimpleQualifiedIdentifier");
         }
 
         IdentifierNode result = null;
         IdentifierNode first;
 
         boolean is_attr;
         if (HAS_ATTRIBUTEIDENTIFIERS && lookahead(AMPERSAND_TOKEN))
         {
             match(AMPERSAND_TOKEN);
             is_attr = true;
         }
         else
         {
             is_attr = false;
         }
 
         if( is_attr && lookahead(LEFTBRACKET_TOKEN) )   // @[expr]
         {
             MemberExpressionNode men = parseBrackets(null);
             GetExpressionNode gen = men.selector instanceof GetExpressionNode ? (GetExpressionNode) men.selector : null;
             result = nodeFactory.qualifiedExpression(null,gen.expr,gen.expr.pos());
         }
         else
         {
             first = parseQualifier();
             if (HAS_QUALIFIEDIDENTIFIERS && lookahead(DOUBLECOLON_TOKEN))
             {
                 match(DOUBLECOLON_TOKEN);
                 MemberExpressionNode temp;
                 temp = nodeFactory.memberExpression(null,nodeFactory.getExpression(first));
                 if( lookahead(LEFTBRACKET_TOKEN) )  // @ns::[expr]
                 {
                     MemberExpressionNode men = parseBrackets(null);
                     GetExpressionNode gen = men.selector instanceof GetExpressionNode ? (GetExpressionNode) men.selector : null;
                     result = nodeFactory.qualifiedExpression(temp,gen.expr,gen.expr.pos());
                 }
                 else
                 {
                 	QualifiedIdentifierNode qualid = nodeFactory.qualifiedIdentifier(temp,parsePropertyIdentifier()); 
                     if( config_namespaces.last().contains(first.name) )
                     	qualid.is_config_name = true;
                     result = qualid; 
                     result.setOrigTypeToken(DOUBLECOLON_TOKEN);
                 }
             }
             else
             {
                 result = first;
             }
         }
 
         result.setAttr(is_attr);
 
         if (debug)
         {
             System.err.println("finish parseSimpleQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * ExpressionQualifiedIdentifier
      */
 
     public IdentifierNode parseExpressionQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseExpressionQualifiedIdentifier");
         }
 
         IdentifierNode result;
         Node first;
 
         boolean is_attr;
         if (HAS_ATTRIBUTEIDENTIFIERS && lookahead(AMPERSAND_TOKEN))
         {
             match(AMPERSAND_TOKEN);
             is_attr = true;
         }
         else
         {
             is_attr = false;
         }
 
         first = parseParenExpression();
         match(DOUBLECOLON_TOKEN);
         result = nodeFactory.qualifiedIdentifier(first, parsePropertyIdentifier());
         result.setAttr(is_attr);
 
         if (debug)
         {
             System.err.println("finish parseExpressionQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * QualifiedIdentifier
      */
 
     public IdentifierNode parseQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parseQualifiedIdentifier");
         }
 
         IdentifierNode result;
         if (HAS_EXPRESSIONQUALIFIEDIDS && lookahead(LEFTPAREN_TOKEN))
         {
             result = parseExpressionQualifiedIdentifier();
         }
         else
         {
             result = parseSimpleQualifiedIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * PrimaryExpression
      */
 
     public Node parsePrimaryExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parsePrimaryExpression");
         }
 
         Node result;
 
         if (lookahead(NULL_TOKEN))
         {
             match(NULL_TOKEN);
             result = nodeFactory.literalNull(ctx.input.positionOfMark());
         }
         else if (lookahead(TRUE_TOKEN))
         {
             match(TRUE_TOKEN);
             result = nodeFactory.literalBoolean(true,ctx.input.positionOfMark());
         }
         else if (lookahead(FALSE_TOKEN))
         {
             match(FALSE_TOKEN);
             result = nodeFactory.literalBoolean(false,ctx.input.positionOfMark());
         }
         else if (lookahead(PRIVATE_TOKEN))
         {
             match(PRIVATE_TOKEN);
             result = nodeFactory.identifier("private",ctx.input.positionOfMark());
         }
         else if (lookahead(PUBLIC_TOKEN))
         {
             match(PUBLIC_TOKEN);
             result = nodeFactory.identifier("public",ctx.input.positionOfMark());
         }
         else if (lookahead(PROTECTED_TOKEN))
         {
             match(PROTECTED_TOKEN);
             result = nodeFactory.identifier("protected",ctx.input.positionOfMark());
         }
         else if (lookahead(NUMBERLITERAL_TOKEN))
         {
             result = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),ctx.input.positionOfMark());
         }
         else if (lookahead(STRINGLITERAL_TOKEN))
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             result = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
         }
         else if (lookahead(THIS_TOKEN))
         {
             match(THIS_TOKEN);
             result = nodeFactory.thisExpression(ctx.input.positionOfMark());
         }
         else if (HAS_REGULAREXPRESSIONS && lookahead(REGEXPLITERAL_TOKEN))
         {
             result = nodeFactory.literalRegExp(scanner.getTokenText(match(REGEXPLITERAL_TOKEN)),ctx.input.positionOfMark());
         }
         else if (lookahead(LEFTPAREN_TOKEN))
         {
             result = parseParenListExpression();
         }
         else if (lookahead(LEFTBRACKET_TOKEN))
         {
             result = parseArrayLiteral();
         }
         else if (lookahead(LEFTBRACE_TOKEN))
         {
             result = parseObjectLiteral();
         }
         else if (lookahead(FUNCTION_TOKEN))
         {
             match(FUNCTION_TOKEN);
             IdentifierNode first = null;
             if (lookahead(IDENTIFIER_TOKEN))
             {
                 first = parseIdentifier();
             }
             result = parseFunctionCommon(first);
         }
         else if( HAS_XMLLITERALS && (lookahead(XMLMARKUP_TOKEN) || lookahead(LESSTHAN_TOKEN)) )
         {
             result = parseXMLLiteral();
         }
         else if (lookahead(PACKAGE_TOKEN))
         {
             if (within_package)
             {
                 error(syntax_error, kError_NestedPackage);
                 result = nodeFactory.error(ctx.input.positionOfMark(), kError_NestedPackage);
             }
             else
             {
                 error(syntax_error,kError_Parser_ExpectedPrimaryExprBefore,scanner.getTokenText(nexttoken));
                 result = nodeFactory.error(ctx.input.positionOfMark(), kError_Parser_ExpectedPrimaryExprBefore);
             }
             skiperror(LEFTBRACE_TOKEN);
             skiperror(RIGHTBRACE_TOKEN);
         }
         else if (lookahead(CATCH_TOKEN) || lookahead(FINALLY_TOKEN) || lookahead(ELSE_TOKEN))
         {
             error(syntax_error, kError_Parser_ExpectedPrimaryExprBefore,
                       scanner.getTokenText(nexttoken));
             skiperror(LEFTBRACE_TOKEN);
             skiperror(RIGHTBRACE_TOKEN);
             result = nodeFactory.error(ctx.input.positionOfMark(), kError_Parser_ExpectedPrimaryExprBefore);
         }
         else
         {
             result = parseTypeIdentifier();
             //result = nodeFactory.memberExpression(null, nodeFactory.getExpression(first, first.pos()),first.pos());
 /*            //if (nexttoken != EOS_TOKEN)  // filter out stupid error messages
             {
                 error(syntax_error, kError_Parser_ExpectedPrimaryExprBefore,
                       scanner.getTokenText(nexttoken));
             }
             if (nexttoken != SEMICOLON_TOKEN)
             {
                 skiperror(SEMICOLON_TOKEN);
             }
             result = nodeFactory.error(ctx.input.positionOfMark(), kError_ExpectingExpression);
 */
         }
 
         if (debug)
         {
             System.err.println("finish parsePrimaryExpression");
         }
 
         return result;
     }
     boolean is_xmllist = false;
     public LiteralXMLNode parseXMLLiteral()
     {
         is_xmllist = false;
         LiteralXMLNode result = null;
         Node first;
         int pos = ctx.input.positionOfMark();
         if( lookahead(XMLMARKUP_TOKEN) )
         {
             first  = nodeFactory.list(null,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN)),pos));
         }
         else
         {
             scanner.pushState();
             first = parseXMLElement();
             scanner.popState();
         }
         if( first != null )
         {
             ListNode list = nodeFactory.list(null,first,pos);
             result = nodeFactory.literalXML(list,is_xmllist,ctx.input.positionOfMark());
         }
         return result;
     }
 
     Node parseXMLElement()
     {
         Node result;
         boolean is_xmllist = false;
 
         match(LESSTHAN_TOKEN);
         scanner.enterSlashDivContext();  // after the initial < tokenize in slashdiv mode to avoid /> conflict
         if( lookahead(GREATERTHAN_TOKEN) )
         {
             is_xmllist = true;
             result = nodeFactory.list(null,nodeFactory.literalString("",0));
         }
         else
         {
             result = nodeFactory.list(null,nodeFactory.literalString("<",0));
             result = parseXMLName(result);
             result = parseXMLAttributes(result);
         }
 
         if( lookahead(GREATERTHAN_TOKEN) )
         {
             match(GREATERTHAN_TOKEN);
             if( !is_xmllist )
             {
                 result = concatXML(result,nodeFactory.literalString(">",0));
             }
             result = parseXMLElementContent(result);
             if( lookahead(EOS_TOKEN) )
             {
                 scanner.exitSlashDivContext();
                 error(kError_Lexical_NoMatchingTag);
                 return nodeFactory.error(ctx.input.positionOfMark()-1,kError_Lexical_NoMatchingTag);
             }
             match(XMLTAGSTARTEND_TOKEN); // "</"
             if( lookahead(GREATERTHAN_TOKEN) )
             {
                 if( !is_xmllist )
                 {
                     ctx.error(ctx.input.positionOfMark()-1,kError_MissingXMLTagName);
                 }
             }
             else
             {
                 result = concatXML(result,nodeFactory.literalString("</",0));
                 result = parseXMLName(result);
                 result = concatXML(result,nodeFactory.literalString(">",0));
             }
             match(GREATERTHAN_TOKEN);
         }
         else
         {
             match(XMLTAGENDEND_TOKEN);  // "/>"
             result = concatXML(result,nodeFactory.literalString("/>",0));
         }
         scanner.exitSlashDivContext();
         this.is_xmllist = is_xmllist;
         return result;
     }
 
     Node concatXML(Node left, Node right)
     {
     	Node tmpLeft = left;
     	if (left instanceof ListNode && ((ListNode)left).size() == 1)
     	{
     		tmpLeft = ((ListNode)left).items.first();
     	}
 
     	// If both sides are literal strings, return one literal string that is the
     	// concatenation.
     	if (tmpLeft instanceof LiteralStringNode && right instanceof LiteralStringNode)
     	{
     		return nodeFactory.literalString(((LiteralStringNode)tmpLeft).value + ((LiteralStringNode)right).value, left.pos());
     	}
 
     	// If the lhs is a + expression with a string literal rhs,
     	// and the rhs is a string literal, turn (x+y)+z into x+(y+z)
     	if (left instanceof BinaryExpressionNode && right instanceof LiteralStringNode)
     	{
     		BinaryExpressionNode leftBinary = (BinaryExpressionNode)left;
     		if (leftBinary.op == PLUS_TOKEN && leftBinary.rhs instanceof LiteralStringNode)
     		{
         		return nodeFactory.binaryExpression(PLUS_TOKEN, leftBinary.lhs,
         				nodeFactory.literalString(((LiteralStringNode)leftBinary.rhs).value + ((LiteralStringNode)right).value), leftBinary.pos());
     		}
     	}
 
     	// Couldn't optimize, return ordinary binary expression
     	return nodeFactory.binaryExpression(PLUS_TOKEN,left,right);
     }
 
      Node parseXMLName(Node first)
     {
         Node result;
         if( lookahead(LEFTBRACE_TOKEN) )
         {
             match(LEFTBRACE_TOKEN);
             scanner.pushState();     // save the state of the scanner
             result = concatXML(first,parseListExpression(allowIn_mode));
             match(RIGHTBRACE_TOKEN);
             scanner.popState();      // restore the state of the scanner
         }
         else
         {
             if( lookahead(xmlid_tokens,xmlid_tokens_count) )
             {
                 result  = concatXML(first,nodeFactory.literalString(scanner.getTokenText(match(xmlid_tokens,xmlid_tokens_count)),ctx.input.positionOfMark()));
             }
             else
             {
                 error(syntax_error,kError_ErrorNodeError,"invalid xml name");
                 skiperror(syntax_xml_error);
                 result = nodeFactory.error(ctx.input.positionOfMark(),kError_MissingXMLTagName);
             }
 
             String separator_text;
             while( lookahead(DOT_TOKEN) || lookahead(MINUS_TOKEN) || lookahead(COLON_TOKEN) )
             {
                 if( lookahead(DOT_TOKEN) )
                 {   match(DOT_TOKEN);
                     separator_text = ".";
                 }
                 else
                 if( lookahead(MINUS_TOKEN) )
                 {   match(MINUS_TOKEN);
                     separator_text = "-";
                 }
                 else
                 {   match(COLON_TOKEN);
                     separator_text = ":";
                 }
 
                 if( lookahead(xmlid_tokens,xmlid_tokens_count) )
                 {
                     result = concatXML(result,nodeFactory.literalString(separator_text,0));
                     result = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(xmlid_tokens,xmlid_tokens_count)),ctx.input.positionOfMark()));
                 }
                 else
                 {
                     error(syntax_error,kError_MissingXMLTagName);
                     skiperror(syntax_xml_error);
                     result = nodeFactory.error(ctx.input.positionOfMark(),kError_MissingXMLTagName);
                 }
             }
         }
         return result;
     }
 
     Node parseXMLAttributes(Node first)
     {
         Node result = first;
         while( !(lookahead(GREATERTHAN_TOKEN)||lookahead(XMLTAGENDEND_TOKEN)||lookahead(EOS_TOKEN) ) )
         {
             result = concatXML(result,nodeFactory.literalString(" ",0));
             result = parseXMLAttribute(result);
         }
         return result;
     }
 
     Node parseXMLAttribute(Node first)
     {
         Node result = parseXMLName(first);
         if( lookahead(ASSIGN_TOKEN) )
         {
             match(ASSIGN_TOKEN);
 
             Node value = null;
             boolean single_quote = false;
 
             if( lookahead(STRINGLITERAL_TOKEN) )
             {
                 boolean[] is_single_quoted = new boolean[1];
                 String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
                 value = nodeFactory.literalString( enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
                 single_quote = is_single_quoted[0];
             }
             else
             if( lookahead(LEFTBRACE_TOKEN) )
             {
                 match(LEFTBRACE_TOKEN);
                 scanner.pushState();     // save the state of the scanner
                 Node expr = parseListExpression(allowIn_mode);
                 value = nodeFactory.invoke("[[ToXMLAttrString]]",nodeFactory.argumentList(null,expr),0);
                 match(RIGHTBRACE_TOKEN);
                 scanner.popState();      // restore the state of the scanner
             }
             else
             {
                 error(syntax_error,kError_ParserExpectingLeftBraceOrStringLiteral);
             }
 
             result = concatXML(result,nodeFactory.literalString(single_quote ? "='" : "=\"",0));
 
             result = concatXML(result,value);
 
             result = concatXML(result,nodeFactory.literalString(single_quote ? "'" : "\"",0));
         }
 
         return result;
     }
 
     Node parseXMLElementContent(Node first)
     {
         Node result = first;
 
         // begin xmlelementcontent
 
         scanner.pushState();
 
         scanner.state = xmltext_state;
         while( !(lookahead(XMLTAGSTARTEND_TOKEN) || lookahead(EOS_TOKEN)) )  // </
         {
             if( lookahead(LEFTBRACE_TOKEN) )
             {
                 match(LEFTBRACE_TOKEN);
                 scanner.pushState();     // save the state of the scanner
                 Node expr = parseListExpression(allowIn_mode);
                 expr = nodeFactory.invoke("[[ToXMLString]]",nodeFactory.argumentList(null,expr),0);
                 result = concatXML(result,expr);
                 match(RIGHTBRACE_TOKEN);
                 scanner.popState();      // restore the state of the scanner
             }
             else
             if( lookahead(LESSTHAN_TOKEN) )
             {
                 scanner.state = start_state;
                 result = concatXML(result,parseXMLElement());
             }
             else
             if( lookahead(XMLMARKUP_TOKEN) )
             {
                 result  = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN)),ctx.input.positionOfMark()));
             }
             else
             if( lookahead(XMLTEXT_TOKEN) )
             {
                 result  = concatXML(result,nodeFactory.literalString(scanner.getTokenText(match(XMLTEXT_TOKEN)),ctx.input.positionOfMark()));
             }
             else
             {
                 error(syntax_error,kError_MissingXMLTagName);
                 skiperror(RIGHTBRACE_TOKEN);
                 scanner.popState();
                 return result;
             }
             scanner.state = xmltext_state;
         }
         scanner.popState();
         return result;
     }
 
 /*
 XMLElement
     <  XMLTagContent  />
     <  XMLTagContent  >  XMLElementContentopt  <  XMLTagContent  />
 
 XMLTagContent
     XMLTagCharacters XMLTagContentopt
     =  Whitespaceopt  {  Expression  }  XMLTagContentopt
     {  Expression  }  XMLTagContentopt
 
 XMLElementContent
     XMLMarkup  XMLElementContentopt
     XMLText  XMLElementContentopt
     {  Expression  }  XMLElementContentopt
 */
 
     /*
     public LiteralXMLNode parseXMLLiteral()
     {
         LiteralXMLNode result;
         ListNode first;
         if (lookahead(LESSTHAN_TOKEN))
         {
             match(LESSTHAN_TOKEN);
             first = nodeFactory.list(null, nodeFactory.literalString(scanner.getTokenText(match(XMLPART_TOKEN))));
             scanner.pushState();     // save the state of the scanner
             first = nodeFactory.list(first, parseListExpression(allowIn_mode));
             match(RIGHTBRACE_TOKEN);  // eat the expression escape character
             scanner.popState();      // restore the state of the scanner
 
             while (lookahead(XMLPART_TOKEN))
             {
                 first = nodeFactory.list(first, nodeFactory.literalString(scanner.getTokenText(match(XMLPART_TOKEN))));
                 scanner.pushState();
                 first = nodeFactory.list(first, parseListExpression(allowIn_mode));
                 match(RIGHTBRACE_TOKEN);  // eat the expression escape character
                 scanner.popState();
             }
             first = nodeFactory.list(first, nodeFactory.literalString(scanner.getTokenText(match(XMLLITERAL_TOKEN))));
             result = nodeFactory.literalXML(first,ctx.input.positionOfMark());
         }
         else // xml markup
         {
 //            first  = nodeFactory.List(0,nodeFactory.literalString(scanner.getTokenText(match(XMLMARKUP_TOKEN))));
 //            result = nodeFactory.LiteralXML(first,ctx.input.positionOfMark());
             result = null;
         }
         return result;
     }
     */
 
     /*
      * ParenExpression
      */
 
     public Node parseParenExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseParenExpression");
         }
 
         Node result;
 
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
         int mark = scanner.input.positionOfMark();
         result = parseAssignmentExpression(allowIn_mode);
         result.setPosition(mark);
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseParenExpression");
         }
 
         return result;
     }
 
     /*
      * ParenListExpression
      */
 
     public ListNode parseParenListExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseParenListExpression");
         }
 
         ListNode result;
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
         result = parseListExpression(allowIn_mode);
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseParenListExpression");
         }
 
         return result;
     }
 
     /*
      * PrimaryExpressionOrExpressionQualifiedIdentifier
      */
 
     public Node parsePrimaryExpressionOrExpressionQualifiedIdentifier()
     {
 
         if (debug)
         {
             System.err.println("begin parsePrimaryExpressionOrExpressionQualifiedIdentifier");
         }
 
         Node result;
 
         if (lookahead(LEFTPAREN_TOKEN))
         {
             ListNode first = parseParenListExpression();
             if (HAS_EXPRESSIONQUALIFIEDIDS && lookahead(DOUBLECOLON_TOKEN))
             {
                 match(DOUBLECOLON_TOKEN);
                 if (first.size() != 1)
                 {
                     result = error(kError_Parser_ExpectingASingleExpressionWithinParenthesis);
                     skiperror(expression_syntax_error);
                 }
                 else
                 {
                     result = nodeFactory.qualifiedIdentifier(first.items.last(), parseIdentifier());
                     result = nodeFactory.memberExpression(null, nodeFactory.getExpression(result));
                 }
             }
             else
             {
                 result = first;
             }
         }
         else
         {
             result = parsePrimaryExpression();
         }
 
         if (debug)
         {
             System.err.println("finish parsePrimaryExpressionOrExpressionQualifiedIdentifier");
         }
 
         return result;
     }
 
     /*
      * FunctionExpression
      */
 
     public FunctionCommonNode parseFunctionCommon(IdentifierNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionCommon");
         }
 
         FunctionCommonNode result;
         FunctionSignatureNode second;
         StatementListNode third = null;
 
         boolean saved_has_arguments = nodeFactory.has_arguments;  // for nested functions
         boolean saved_has_rest = nodeFactory.has_rest;  // for nested functions
         boolean saved_has_dxns = nodeFactory.has_dxns;  //
 
         nodeFactory.has_arguments = false;  // set by Identifier, captured by FunctionCommon
         nodeFactory.has_rest = false;  // set by RestParameterNode, captured by FunctionCommon
         nodeFactory.has_dxns = false;
 
         boolean is_ctor = false;
         if (ctx.statics.es4_nullability && block_kind_stack.last() == CLASS_TOKEN
                 && first != null && first.name.equals(current_class_name)) {
             is_ctor = true;
         }
 
         block_kind_stack.add(FUNCTION_TOKEN);
 
         if (is_ctor)
             second = parseConstructorSignature();
         else
             second = parseFunctionSignature();
 
         DefaultXMLNamespaceNode saved_dxns = nodeFactory.dxns;
 
         //nodeFactory.StartClassDefs();
 
         if (lookahead(LEFTBRACE_TOKEN))
         {
             third = parseBlock();
 
             if (!AVMPLUS)
             {
                 third = nodeFactory.statementList(third, nodeFactory.returnStatement(null, scanner.input.positionOfMark()));
             }
 
             if ( third == null )
             {
                 // Function had an empty body.  Create an empty list to represent { }
                 third = nodeFactory.statementList(null, null);
             }
         }
         else
         {
             matchSemicolon(abbrevFunction_mode);
         }
 
         // Append a default return statement with the line number at the position of the } (happens in nodeFactory->FunctionCommon)
         result = nodeFactory.functionCommon(ctx,first,second,third,first!=null?first.pos():second.pos());
         result.default_dxns = nodeFactory.has_dxns?null:saved_dxns;
 
         nodeFactory.has_arguments = saved_has_arguments;
         nodeFactory.has_rest = saved_has_rest;
         nodeFactory.has_dxns = saved_has_dxns;
 
         //nodeFactory.FinishClassDefs();
         block_kind_stack.removeLast();
 
 
         if (debug)
         {
             System.err.println("finish parseFunctionCommon");
         }
 
         return result;
     }
 
     /*
      * ObjectLiteral
      */
 
     public Node parseObjectLiteral()
     {
 
         if (debug)
         {
             System.err.println("begin parseObjectLiteral");
         }
 
         Node result;
         ArgumentListNode first;
 
         int pos = ctx.input.positionOfMark();
 
 		scanner.enterSlashRegExpContext();
         match(LEFTBRACE_TOKEN);
 
         if (lookahead(RIGHTBRACE_TOKEN))
         {
             first = null;
         }
         else
         {
             // Inlining parseFieldList:
             //     FieldList: LiteralField FieldListPrime
 
             first = parseFieldListPrime(nodeFactory.argumentList(null, parseLiteralField()));
         }
 
         match(RIGHTBRACE_TOKEN);
 		scanner.exitSlashRegExpContext();			
         result = nodeFactory.literalObject(first,pos);
 
         if (debug)
         {
             System.err.println("finish parseObjectLiteral with result = ");
         }
 
         return result;
     }
 
     /*
      * FieldListPrime
      */
 
     public ArgumentListNode parseFieldListPrime(ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFieldListPrime");
         }
 
         ArgumentListNode result;
 
         if (lookahead(COMMA_TOKEN))
         {
             Node second;
             match(COMMA_TOKEN);
             second = parseLiteralField();
             result = parseFieldListPrime(nodeFactory.argumentList(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFieldListPrime with result = ");
         }
 
         return result;
     }
 
     /*
      * LiteralField
      */
 
     public Node parseLiteralField()
     {
 
         if (debug)
         {
             System.err.println("begin parseLiteralField");
         }
 
         Node result;
         Node first;
         Node second;
         ListNode l = null;
 
         first = parseFieldOrConfigName();
         if( first.isConfigurationName() && !lookahead(COLON_TOKEN) )
         {
         	// If we got back a configuration name, then 
         	// we need to parse the actual name
         	// e.g. {... CONFIG::Debug y:val ... }
         	l = nodeFactory.list(null, first);
         	first = parseFieldOrConfigName();
         }
         match(COLON_TOKEN);
         second = parseAssignmentExpression(allowIn_mode);
 
         result = nodeFactory.literalField(first, second);
 
         if( l != null )
         {
         	result = nodeFactory.list(l, result);
         }
         if (debug)
         {
             System.err.println("finish parseLiteralField");
         }
 
         return result;
     }
 
     /*
      * FieldName
      */
 
     public Node parseFieldOrConfigName()
     {
 
         if (debug)
         {
             System.err.println("begin parseFieldName");
         }
 
         Node result;
 
         if (HAS_NONIDENTFIELDNAMES && lookahead(STRINGLITERAL_TOKEN))
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             result = nodeFactory.literalString( enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
         }
         else if (HAS_NONIDENTFIELDNAMES && lookahead(NUMBERLITERAL_TOKEN))
         {
             result = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),ctx.input.positionOfMark());
         }
         else if (HAS_NONIDENTFIELDNAMES && lookahead(LEFTPAREN_TOKEN))
         {
             result = parseParenExpression();
         }
         else
         {
         	IdentifierNode ident = parseIdentifier();
             if( config_namespaces.last().contains(ident.name) && lookahead(DOUBLECOLON_TOKEN))
             {
             	match(DOUBLECOLON_TOKEN);
             	QualifiedIdentifierNode qualid = nodeFactory.qualifiedIdentifier(ident, parseIdentifier());
             	qualid.is_config_name = true;
             	result = qualid;
             }
             else
             {
             	result = ident;
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseFieldName");
         }
 
         return result;
     }
 
     /*
      * ArrayLiteral
      *     [ ElementList ]
      */
 
     public LiteralArrayNode parseArrayLiteral()
     {
 
         if (debug)
         {
             System.err.println("begin parseArrayLiteral");
         }
 		
         LiteralArrayNode result;
         ArgumentListNode first;
 
         int pos = ctx.input.positionOfMark();
 
         scanner.enterSlashRegExpContext();
         match(LEFTBRACKET_TOKEN);
 
         // ElementList : LiteralElement ElementListPrime (inlined)
 
         if (lookahead(RIGHTBRACKET_TOKEN))
         {
             first = null;
         }
         else
         {
             first = parseElementListPrime(nodeFactory.argumentList(null, parseLiteralElement()));
         }
 
         result = nodeFactory.literalArray(first,pos);
         match(RIGHTBRACKET_TOKEN);
 		scanner.exitSlashRegExpContext();
 
         if (debug)
         {
             System.err.println("finish parseArrayLiteral");
         }
 
         return result;
     }
 
     /*
      * ElementListPrime
      *     , LiteralElement ElementListPrime
      *     empty
      */
 
     public ArgumentListNode parseElementListPrime(ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseElementListPrime");
         }
 
         ArgumentListNode result;
 
         while (lookahead(COMMA_TOKEN))
         {
             match(COMMA_TOKEN);
             Node second;
             second = parseLiteralElement(); // May be empty.
             if( second == null )
             {
                 first = first;
             }
             else
             {
                 first = nodeFactory.argumentList(first, second);
             }
         }
 
         result = first;
 
         if (debug)
         {
             System.err.println("finish parseElementListPrime");
         }
 
         return result;
     }
 
     /*
      * LiteralElement
      *     AssignmentExpression
      *     empty
      */
 
     public Node parseLiteralElement()
     {
 
         if (debug)
         {
             System.err.println("begin parseLiteralElement");
         }
 
         Node result;
         Node first;
 
         if( lookahead(COMMA_TOKEN) )
         {
             result = nodeFactory.emptyElement(ctx.input.positionOfMark());
         }
         else
         if( lookahead(RIGHTBRACKET_TOKEN) )
         {
             result = null;
         }
         else
         {
         	first = parseAssignmentExpression(allowIn_mode);
         	if( first.isConfigurationName() && !lookahead(COMMA_TOKEN) && !lookahead(RIGHTBRACKET_TOKEN) )
         	{
         		// We have an element with a config attr i.e. 
         		// [..., CONFIG::Debug 4, ...]
         		ListNode list = nodeFactory.list(null, first);
         		result = nodeFactory.list(list, parseAssignmentExpression(allowIn_mode));
         	}
         	else
         	{
         		result = first;
         	}
         }
 
         if (debug)
         {
             System.err.println("finish parseLiteralElement");
         }
 
         return result;
     }
 
     /*
      * SuperExpression
      *     super
      *     super ParenExpression
      */
 
     public Node parseSuperExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseSuperExpression");
         }
 
         Node result;
 
         match(SUPER_TOKEN);
         Node first = null;
         if (lookahead(LEFTPAREN_TOKEN))
         {
             first = parseParenExpression();
         }
         result = nodeFactory.superExpression(first,scanner.input.positionOfMark()-5);
 
         if (debug)
         {
             System.err.println("finish parseSuperExpression");
         }
 
         return result;
     }
 
     /*
      * PostfixExpression
      */
 
     public Node parsePostfixExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parsePostfixExpression");
         }
 
         Node first;
         Node result;
 
 
         if (lookahead(PUBLIC_TOKEN) ||
             lookahead(PRIVATE_TOKEN) ||
             lookahead(PROTECTED_TOKEN) ||
             lookahead(DEFAULT_TOKEN) ||
             lookahead(GET_TOKEN) ||
             lookahead(SET_TOKEN) ||
             lookahead(IDENTIFIER_TOKEN) ||
             lookahead(NAMESPACE_TOKEN) ||
             lookahead(MULT_TOKEN) ||
             lookahead(AMPERSAND_TOKEN))
         {
             first = parseAttributeExpression();
             if ( !lookaheadSemicolon(full_mode) && (lookahead(PLUSPLUS_TOKEN) || lookahead(MINUSMINUS_TOKEN)) )
             {
                 first = parsePostfixIncrement(first);
             }
         }
         else if (lookahead(NULL_TOKEN) ||
             lookahead(TRUE_TOKEN) ||
             lookahead(FALSE_TOKEN) ||
             lookahead(NUMBERLITERAL_TOKEN) ||
             lookahead(STRINGLITERAL_TOKEN) ||
             lookahead(THIS_TOKEN) ||
             lookahead(REGEXPLITERAL_TOKEN) ||
             lookahead(PUBLIC_TOKEN) ||
             lookahead(PRIVATE_TOKEN) ||
             lookahead(PROTECTED_TOKEN) ||
             lookahead(LEFTPAREN_TOKEN) ||
             lookahead(LEFTBRACKET_TOKEN) ||
             lookahead(LEFTBRACE_TOKEN) ||
             lookahead(FUNCTION_TOKEN))
         {
             first = parsePrimaryExpressionOrExpressionQualifiedIdentifier();
         }
         else
         {
             if (lookahead(SUPER_TOKEN))
             {
                 first = parseSuperExpression();
             }
             else if (lookahead(NEW_TOKEN))
             {
                 first = parseShortNewExpression();
                 if (lookahead(LEFTPAREN_TOKEN))
                 {
                     first = parseArguments(first);
                 }
                 else
                 {
                     first = nodeFactory.callExpression(first, null);
                 }
 
                 first = nodeFactory.newExpression(first);  // translates call to new
 
                 if ( !lookaheadSemicolon(full_mode) && ((lookahead(PLUSPLUS_TOKEN) ||
                     lookahead(MINUSMINUS_TOKEN))))
                 {
                     first = parsePostfixIncrement(first);
                 }
                 else
                 {
                     // first = first;
                 }
             }
             else
             {
                 first = parseFullNewSubexpression();
                 if (lookahead(LEFTPAREN_TOKEN))
                 {
                     first = parseArguments(first);
                 }
                 else
                 {
                     // first = first;
                 }
             }
         }
 
         result = parseFullPostfixExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parsePostfixExpression");
         }
 
         return result;
     }
 
     /*
      * PostfixIncrement
      */
 
     public Node parsePostfixIncrement(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parsePostfixIncrement");
         }
 
         Node result;
 
         if (lookahead(PLUSPLUS_TOKEN))
         {
             result = nodeFactory.postfixExpression(match(PLUSPLUS_TOKEN), first,ctx.input.positionOfMark());
         }
         else if (lookahead(MINUSMINUS_TOKEN))
         {
             result = nodeFactory.postfixExpression(match(MINUSMINUS_TOKEN), first,ctx.input.positionOfMark());
         }
         else
         {
             result = error(kError_Parser_ExpectingIncrOrDecrOperator);
             skiperror(syntax_error);
         }
 
         if (debug)
         {
             System.err.println("finish parsePostfixIncrement");
         }
 
         return result;
     }
 
     /*
      * FullPostfixExpressionPrime
      *     PropertyOperator FullPostfixExpressionPrime
      *     Arguments FullPostfixExpressionPrime
      *     FullPostfixExpressionIncrementExpressionSuffix
      *     empty
      */
 
     public Node parseFullPostfixExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFullPostfixExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(DOT_TOKEN) || lookahead(LEFTBRACKET_TOKEN) || lookahead(DOTLESSTHAN_TOKEN) ||
             (HAS_DESCENDOPERATORS && lookahead(DOUBLEDOT_TOKEN)))
         {
             first = parsePropertyOperator(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else if (lookahead(LEFTPAREN_TOKEN))
         {
             first = parseArguments(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else if ( !lookaheadSemicolon(full_mode) && ((lookahead(PLUSPLUS_TOKEN) || lookahead(MINUSMINUS_TOKEN))))
         {
             first = parsePostfixIncrement(first);
             result = parseFullPostfixExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFullPostfixExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * AttributeExpression
      *    SimpleQualifiedIdentifier
      *    AttributeExpression PropertyOperator
      *    AttributeExpression Arguments
      */
 
     public Node parseAttributeExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributeExpression");
         }
 
         Node result;
         Node first;
 
         IdentifierNode ident = parseSimpleQualifiedIdentifier();
     /*
     if( ident.name.equals("*") )
         {
             ctx.error(scanner.input.positionOfMark()-2,kError_InvalidWildcardIdentifier);
         }
     */
         first = nodeFactory.memberExpression(null, nodeFactory.getExpression(ident));
         if (lookahead(DOT_TOKEN) ||
             lookahead(LEFTBRACKET_TOKEN) ||
             lookahead(DOUBLEDOT_TOKEN) ||
             lookahead(LEFTPAREN_TOKEN) ||
             lookahead(AMPERSAND_TOKEN))
         {
             result = parseAttributeExpressionPrime(first);
         }
         else
         {
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
 /*
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode id = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( id != null )
                 {
                     ctx.error(memb.selector.expr.pos()-1,kError_IllegalPackageReference, id.name);
                 }
                 memb.selector.is_package = false; // hack, to avoid reporting same error later
             }
 */
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAttributeExpression");
         }
 
         return result;
     }
 
     public Node parseAttributeExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributeExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(DOT_TOKEN) ||
             lookahead(DOUBLEDOT_TOKEN) ||
             lookahead(LEFTBRACKET_TOKEN) ||
             lookahead(DOTLESSTHAN_TOKEN))  // PropertyOperator
         {
             result = parseAttributeExpressionPrime(parsePropertyOperator(first));
         }
         else
         if (lookahead(LEFTPAREN_TOKEN)) // Arguments
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos()-1,kError_IllegalPackageReference, ident.name);
                     memb.selector.is_package = false; // to avoid redundant errors
                 }
             }
 */
             result = parseAttributeExpressionPrime(parseArguments(first));
         }
         else  // empty
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos()-1,kError_IllegalPackageReference, ident.name);
                 }
             }
 */
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAttributeExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * FullOrShortNewExpression
      */
 
     public Node parseFullOrShortNewExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseFullOrShortNewExpression");
         }
 
         Node result;
         Node first;
 
         match(NEW_TOKEN);
 
         if (lookahead(SUPER_TOKEN))
         {
             first = parseSuperExpression();
         }
         else if (lookahead(NEW_TOKEN))
         {
             first = parseShortNewExpression();
         }
         else
         {
             first = parseFullNewSubexpression();
         }
 
         if (lookahead(LEFTPAREN_TOKEN))
         {
             first = parseArguments(first);
         }
         else
         {
             // first = first;
         }
 
         result = nodeFactory.newExpression(first);
 
         if (debug)
         {
             System.err.println("finish parseFullOrShortNewExpression");
         }
 
         return result;
     }
 
     /*
      * FullNewExpression
      *     new FullNewSubexpression Arguments
      */
 
     public Node parseFullNewExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewExpression");
         }
 
         Node result;
         Node first;
 
         match(NEW_TOKEN);
         first = parseFullNewSubexpression();
         result = nodeFactory.newExpression(parseArguments(first));
 
         if (debug)
         {
             System.err.println("finish parseFullNewExpression");
         }
 
         return result;
     }
 
     /*
      * FullNewSubexpression
      *     PrimaryExpression FullNewSubexpressionPrime
      *     QualifiedIdentifier FullNewSubexpressionPrime
      *     FullNewExpression FullNewSubexpressionPrime
      *     SuperExpression PropertyOperator FullNewSubexpressionPrime
      */
 
     public Node parseFullNewSubexpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewSubexpression");
         }
 
         Node result;
 
         if (lookahead(NEW_TOKEN))
         {
             Node first;
             first = parseFullNewExpression();
             result = parseFullNewSubexpressionPrime(first);
         }
         else if (lookahead(SUPER_TOKEN))
         {
             Node first;
             first = parseSuperExpression();
             first = parsePropertyOperator(first);
             result = parseFullNewSubexpressionPrime(first);
         }
         else if (lookahead(LEFTPAREN_TOKEN))
         {
             Node first;
             first = parsePrimaryExpressionOrExpressionQualifiedIdentifier();
             result = parseFullNewSubexpressionPrime(first);
         }
         else if (lookahead(PUBLIC_TOKEN) || lookahead(PRIVATE_TOKEN) ||
             lookahead(PROTECTED_TOKEN) ||
             lookahead(DEFAULT_TOKEN) || lookahead(GET_TOKEN) ||
             lookahead(SET_TOKEN) || lookahead(NAMESPACE_TOKEN) ||
             lookahead(MULT_TOKEN) || lookahead(IDENTIFIER_TOKEN) ||
             lookahead(AMPERSAND_TOKEN))
         {
             Node first;
             first = nodeFactory.memberExpression(null, nodeFactory.getExpression(parseQualifiedIdentifier()));
             result = parseFullNewSubexpressionPrime(first);
         }
         else
         {
             Node first;
             first = parsePrimaryExpression();
             result = parseFullNewSubexpressionPrime(first);
         }
 
         if (debug)
         {
             System.err.println("finish parseFullNewSubexpression");
         }
 
         return result;
     }
 
     /*
      * FullNewSubexpressionPrime
      *     PropertyOperator FullNewSubexpressionPrime
      *     empty
      */
 
     public Node parseFullNewSubexpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFullNewSubexpressionPrime");
         }
 
         Node result;
 
         if (lookahead(DOT_TOKEN) || lookahead(LEFTBRACKET_TOKEN) || lookahead(DOTLESSTHAN_TOKEN) ||
             (HAS_DESCENDOPERATORS && lookahead(DOUBLEDOT_TOKEN)) ||
             lookahead(AMPERSAND_TOKEN) )
         {
             first = parsePropertyOperator(first);
             result = parseFullNewSubexpressionPrime(first);
         }
         else
         {
 /*
             MemberExpressionNode memb = first instanceof MemberExpressionNode ? (MemberExpressionNode) first : null;
             if( memb != null && memb.selector.is_package )
             {
                 IdentifierNode ident = memb.selector.expr instanceof IdentifierNode ? (IdentifierNode) memb.selector.expr : null;
                 if( ident != null )
                 {
                     ctx.error(memb.selector.expr.pos()-1,kError_IllegalPackageReference, ident.name);
                 }
             }
 */
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseFullNewSubexpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ShortNewExpression
      *     new ShortNewSubexpression
      */
 
     public Node parseShortNewExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShortNewExpression");
         }
 
         Node result;
 
         match(NEW_TOKEN);
         result = parseShortNewSubexpression();
 
         if (debug)
         {
             System.err.println("finish parseShortNewExpression");
         }
 
         return result;
     }
 
     /*
      * ShortNewSubexpression
      *     FullNewSubexpression
      *     ShortNewExpression
      */
 
     public Node parseShortNewSubexpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShortNewSubexpression");
         }
 
         Node result;
 
         // Implement branch into ShortNewExpression
         if (lookahead(NEW_TOKEN))
         {
             result = parseShortNewExpression();
             if (lookahead(LEFTPAREN_TOKEN))
             {
                 result = parseArguments(result);
             }
             result = nodeFactory.newExpression(result);
         }
         else
         {
             result = parseFullNewSubexpression();
         }
 
         if (debug)
         {
             System.err.println("finish parseShortNewSubexpression");
         }
 
         return result;
     }
 
     /*
      * PropertyOperator
      *     . QualifiedIdentifier
      *     .. QualifiedIdentifier
      *     . ( ListExpressionAllowIn )
      *     Brackets
      *
      *     TypeApplication
      *         .<  TypeExpressionList  >
      */
 
     public Node parsePropertyOperator(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parsePropertyOperator");
         }
 
         Node result;
 
         if (lookahead(DOT_TOKEN))
         {
             match(DOT_TOKEN);
             if (lookahead(LEFTPAREN_TOKEN))
             {
                 scanner.enterSlashRegExpContext();
                 match(LEFTPAREN_TOKEN);
                 result = nodeFactory.filterOperator(first,parseListExpression(allowIn_mode),first.pos()-1);
                 match(RIGHTPAREN_TOKEN);
                 scanner.exitSlashRegExpContext();
             }
             else
             {
                 result = nodeFactory.memberExpression(first, nodeFactory.getExpression(parseQualifiedIdentifier()));
             }
         }
         else if (lookahead(LEFTBRACKET_TOKEN))
         {
             result = parseBrackets(first);
         }
         else if (lookahead(DOUBLEDOT_TOKEN))
         {
             match(DOUBLEDOT_TOKEN);
             SelectorNode selector = nodeFactory.getExpression(parseQualifiedIdentifier());
             selector.setMode(DOUBLEDOT_TOKEN);
             result = nodeFactory.memberExpression(first, selector);
         }
         else if (lookahead(DOTLESSTHAN_TOKEN))
         {
             match(DOTLESSTHAN_TOKEN);
             result = nodeFactory.applyTypeExpr(first, parseTypeExpressionList(),ctx.input.positionOfMark());
             if( lookahead(UNSIGNEDRIGHTSHIFT_TOKEN) )
             {
                 // Transform >>> to >> and eat one >
                 nexttoken = RIGHTSHIFT_TOKEN;
             }
             else if( lookahead( RIGHTSHIFT_TOKEN ) )
             {
                 // Transform >> to > and eat one >
                 nexttoken = GREATERTHAN_TOKEN;
             }
             else
             {
                 match(GREATERTHAN_TOKEN);
             }
         }
         else
         {
             // C: this else-part not in the C++ code...
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parsePropertyOperator");
         }
 
         return result;
     }
 
     /*
      * Brackets
      *     [ ListExpressionallowIn ]
      *     [ ExpressionsWithRest ]
      */
 
     public MemberExpressionNode parseBrackets(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBrackets");
         }
 
         MemberExpressionNode result;
         ArgumentListNode second;
 
         match(LEFTBRACKET_TOKEN);
 
         if (lookahead(RIGHTBRACKET_TOKEN))
         {
             error(kError_Lexical_SyntaxError);
             second = null;
         }
         else
         {
             second = parseArgumentsWithRest(allowIn_mode);
         }
 
         match(RIGHTBRACKET_TOKEN);
         result = nodeFactory.indexedMemberExpression(first, nodeFactory.getExpression(second));
         
         if (debug)
         {
             System.err.println("finish parseBrackets");
         }
 
         return result;
     }
 
     /*
      * Arguments
      *     ( )
      *     ( ListExpressionallowIn )
      *     ( NamedArgumentList )
      */
 
     public Node parseArguments(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseArguments");
         }
 
         Node result;
         ArgumentListNode second;
 
         scanner.enterSlashRegExpContext();
         match(LEFTPAREN_TOKEN);
 
         if (lookahead(RIGHTPAREN_TOKEN))
         {
             second = null;
         }
         else
         {
             second = parseArgumentsWithRest(allowIn_mode);
         }
 
         match(RIGHTPAREN_TOKEN);
         scanner.exitSlashRegExpContext();
         result = nodeFactory.callExpression(first, second);
 
         if (debug)
         {
             System.err.println("finish parseArguments");
         }
 
         return result;
     }
 
     /*
      *  ArgumentsWithRest
      *      RestExpression
      *      AssignmentExpression ListExpressionWithRestPrime
      */
 
     public ArgumentListNode parseArgumentsWithRest(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseArgumentsWithRest");
         }
 
         ArgumentListNode result;
         Node first;
 
         if (lookahead(TRIPLEDOT_TOKEN))
         {
             first = parseRestExpression();
         }
         else
         {
             first = parseAssignmentExpression(mode);
         }
 
         result = parseArgumentsWithRestPrime(mode, nodeFactory.argumentList(null, first));
 
         if (debug)
         {
             System.err.println("finish parseArgumentsWithRest with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
         *  ArgumentsWithRestPrime
         *      empty
         *      , RestExpression
         *      , AssignmentExpression ListExpressionPrime
         */
 
     public ArgumentListNode parseArgumentsWithRestPrime(int mode, ArgumentListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseArgumentsWithRestPrime");
         }
 
         ArgumentListNode result;
 
         // , RestExpression
         // , AssignmentExpression ListExpressionWithRest
 
         if (lookahead(COMMA_TOKEN))
         {
             match(COMMA_TOKEN);
 
             Node second;
 
             if (lookahead(TRIPLEDOT_TOKEN))
             {
                 result = nodeFactory.argumentList(first, parseRestExpression());
             }
             else
             {
                 second = parseAssignmentExpression(mode);
                 result = parseArgumentsWithRestPrime(mode, nodeFactory.argumentList(first, second));
             }
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseArgumentsWithRestPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * RestExpression
      *     ... AssignmentExpression[allowIn]
      */
 
     public Node parseRestExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseRestExpression");
         }
 
         Node result;
 
         match(TRIPLEDOT_TOKEN);
 
         result = nodeFactory.restExpression(parseAssignmentExpression(allowIn_mode));
 
         if (debug)
         {
             System.err.println("finish parseRestExpression");
         }
 
         return result;
     }
 
     /*
      * UnaryExpression
      *     PostfixExpression
      *     delete PostfixExpression
      *     void UnaryExpression
      *     typeof UnaryExpression
      *     ++ PostfixExpression
      *     -- PostfixExpression
      *     + UnaryExpression
      *     - UnaryExpression
      *     - NegatedMinLong
      *     ~ UnaryExpression
      *     ! UnaryExpression
      */
 
     public Node parseUnaryExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseUnaryExpression");
         }
 
         Node result = null;
 
         if (lookahead(DELETE_TOKEN))
         {
             match(DELETE_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(DELETE_TOKEN, parsePostfixExpression(), pos);
         }
         else if (lookahead(VOID_TOKEN))
         {
             match(VOID_TOKEN);
             if( lookahead(COMMA_TOKEN) ||
                 lookahead(SEMICOLON_TOKEN) ||
                 lookahead(RIGHTPAREN_TOKEN) )
             {
                 int pos = scanner.input.positionOfMark();
                 result = nodeFactory.unaryExpression(VOID_TOKEN, nodeFactory.literalNumber("0",pos),pos);
             }
             else
             {
                 int pos = scanner.input.positionOfMark();
                 result = nodeFactory.unaryExpression(VOID_TOKEN, parseUnaryExpression(), pos);
             }
         }
         else if (lookahead(TYPEOF_TOKEN))
         {
             match(TYPEOF_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(TYPEOF_TOKEN, parseUnaryExpression(), pos);
         }
         else if (lookahead(PLUSPLUS_TOKEN))
         {
             match(PLUSPLUS_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(PLUSPLUS_TOKEN, parseUnaryExpression(), pos);
         }
         else if (lookahead(MINUSMINUS_TOKEN))
         {
             match(MINUSMINUS_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(MINUSMINUS_TOKEN, parseUnaryExpression(), pos);
         }
         else if (lookahead(PLUS_TOKEN))
         {
             match(PLUS_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(PLUS_TOKEN, parseUnaryExpression(), pos);
         }
         else if (lookahead(MINUS_TOKEN))
         {
             match(MINUS_TOKEN);
             int pos = scanner.input.positionOfMark();
             if (lookahead(NEGMINLONGLITERAL_TOKEN))
             {
                 result = nodeFactory.unaryExpression(MINUS_TOKEN,nodeFactory.literalNumber(scanner.getTokenText(match(NEGMINLONGLITERAL_TOKEN)),ctx.input.positionOfMark()),scanner.input.positionOfMark());
             }
             else
             {
                 result = nodeFactory.unaryExpression(MINUS_TOKEN, parseUnaryExpression(), pos);
             }
         }
         else if (lookahead(BITWISENOT_TOKEN))
         {
             match(BITWISENOT_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(BITWISENOT_TOKEN, parseUnaryExpression(), pos);
         }
         else if (lookahead(NOT_TOKEN))
         {
             match(NOT_TOKEN);
             int pos = scanner.input.positionOfMark();
             result = nodeFactory.unaryExpression(NOT_TOKEN, parseUnaryExpression(), pos);
         }
         else
         {
         	result = parsePostfixExpression();
         }
 
         if (debug)
         {
             System.err.println("finish parseUnaryExpression");
         }
 
         return result;
     }
 
     /*
      * MultiplicativeExpression
      */
 
     public Node parseMultiplicativeExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseMultiplicativeExpression");
         }
 
         Node result;
         Node first = null;
 
         lookahead(DIV_TOKEN); // for a lookahead before changing the context since
         // the next token needs to be scanned in the current
         // mode.
         scanner.enterSlashDivContext();
         first = parseUnaryExpression();
         scanner.exitSlashDivContext();
         result = parseMultiplicativeExpressionPrime(first);
 //        scanner.exitSlashDivContext();
 
         if (debug)
         {
             System.err.println("finish parseMultiplicativeExpression");
         }
 
         return result;
     }
 
     public Node parseMultiplicativeExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseMultiplicativeExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(MULT_TOKEN))
         {
             match(MULT_TOKEN);
             lookahead(DIV_TOKEN); // for a lookahead before changing the context since
             // the next token needs to be scanned in the current
             // mode.
             scanner.enterSlashDivContext();
             first = nodeFactory.binaryExpression(MULT_TOKEN, first, parseUnaryExpression());
             scanner.exitSlashDivContext();
             result = parseMultiplicativeExpressionPrime(first);
         }
         else if (lookahead(DIV_TOKEN))
         {
             match(DIV_TOKEN);
             lookahead(DIV_TOKEN); // for a lookahead before changing the context since
             // the next token needs to be scanned in the current
             // mode.
             scanner.enterSlashDivContext();
             first = nodeFactory.binaryExpression(DIV_TOKEN, first, parseUnaryExpression());
             scanner.exitSlashDivContext();
             result = parseMultiplicativeExpressionPrime(first);
         }
         else if (lookahead(MODULUS_TOKEN))
         {
             match(MODULUS_TOKEN);
             lookahead(DIV_TOKEN); // for a lookahead before changing the context since
             // the next token needs to be scanned in the current
             // mode.
             scanner.enterSlashDivContext();
             first = nodeFactory.binaryExpression(MODULUS_TOKEN, first, parseUnaryExpression());
             scanner.exitSlashDivContext();
             result = parseMultiplicativeExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseMultiplicativeExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * AdditiveExpression
      *     MultiplicativeExpression AdditiveExpressionPrime
      *
      * AdditiveExpressionPrime
      *     + MultiplicativeExpression AdditiveExpressionPrime
      *     - MultiplicativeExpression AdditiveExpressionPrime
      *     empty
      */
 
     public Node parseAdditiveExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseAdditiveExpression");
         }
 
         Node result;
         Node first;
 
         first = parseMultiplicativeExpression();
         result = parseAdditiveExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parseAdditiveExpression");
         }
 
         return result;
     }
 
     public Node parseAdditiveExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAdditiveExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(PLUS_TOKEN))
         {
             match(PLUS_TOKEN);
             first = nodeFactory.binaryExpression(PLUS_TOKEN, first, parseMultiplicativeExpression());
             result = parseAdditiveExpressionPrime(first);
         }
         else if (lookahead(MINUS_TOKEN))
         {
             match(MINUS_TOKEN);
             first = nodeFactory.binaryExpression(MINUS_TOKEN, first, parseMultiplicativeExpression());
             result = parseAdditiveExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAdditiveExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ShiftExpression
      *     AdditiveExpression ShiftExpressionPrime
      *
      * ShiftExpressionPrime
      *     << AdditiveExpression ShiftExpressionPrime
      *     >> AdditiveExpression ShiftExpressionPrime
      *     >>> AdditiveExpression ShiftExpressionPrime
      *     empty
      */
 
     public Node parseShiftExpression()
     {
 
         if (debug)
         {
             System.err.println("begin parseShiftExpression");
         }
 
         Node result;
         Node first;
 
         first = parseAdditiveExpression();
         result = parseShiftExpressionPrime(first);
 
         if (debug)
         {
             System.err.println("finish parseShiftExpression");
         }
 
         return result;
     }
 
     public Node parseShiftExpressionPrime(Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseShiftExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(LEFTSHIFT_TOKEN))
         {
             match(LEFTSHIFT_TOKEN);
             first = nodeFactory.binaryExpression(LEFTSHIFT_TOKEN, first, parseAdditiveExpression());
             result = parseShiftExpressionPrime(first);
         }
         else if (lookahead(RIGHTSHIFT_TOKEN))
         {
             match(RIGHTSHIFT_TOKEN);
             first = nodeFactory.binaryExpression(RIGHTSHIFT_TOKEN, first, parseAdditiveExpression());
             result = parseShiftExpressionPrime(first);
         }
         else if (lookahead(UNSIGNEDRIGHTSHIFT_TOKEN))
         {
             match(UNSIGNEDRIGHTSHIFT_TOKEN);
             first = nodeFactory.binaryExpression(UNSIGNEDRIGHTSHIFT_TOKEN, first, parseAdditiveExpression());
             result = parseShiftExpressionPrime(first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseShiftExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * RelationalExpression
      *     ShiftExpression RelationalExpressionPrime
      *
      * RelationalExpressionPrime
      *     < ShiftExpression RelationalExpressionPrime
      *     > ShiftExpression RelationalExpressionPrime
      *     <= ShiftExpression RelationalExpressionPrime
      *     >= ShiftExpression RelationalExpressionPrime
      *     instanceof ShiftExpression RelationalExpressionPrime
      *     as ShiftExpression RelationalExpressionPrime
      *     in ShiftExpression RelationalExpressionPrime
      *     empty
      */
 
     public Node parseRelationalExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseRelationalExpression");
         }
 
         Node result;
         Node first;
 
         first = parseShiftExpression();
         result = parseRelationalExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseRelationalExpression");
         }
 
         return result;
     }
 
     public Node parseRelationalExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseRelationalExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(LESSTHAN_TOKEN))
         {
             match(LESSTHAN_TOKEN);
             first = nodeFactory.binaryExpression(LESSTHAN_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(GREATERTHAN_TOKEN))
         {
             match(GREATERTHAN_TOKEN);
             first = nodeFactory.binaryExpression(GREATERTHAN_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(LESSTHANOREQUALS_TOKEN))
         {
             match(LESSTHANOREQUALS_TOKEN);
             first = nodeFactory.binaryExpression(LESSTHANOREQUALS_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(GREATERTHANOREQUALS_TOKEN))
         {
             match(GREATERTHANOREQUALS_TOKEN);
             first = nodeFactory.binaryExpression(GREATERTHANOREQUALS_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(IS_TOKEN))
         {
             match(IS_TOKEN);
             first = nodeFactory.binaryExpression(IS_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(AS_TOKEN))
         {
             match(AS_TOKEN);
             first = nodeFactory.binaryExpression(AS_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (lookahead(INSTANCEOF_TOKEN))
         {
             match(INSTANCEOF_TOKEN);
             first = nodeFactory.binaryExpression(INSTANCEOF_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else if (mode == allowIn_mode && lookahead(IN_TOKEN))
         {
             match(IN_TOKEN);
             first = nodeFactory.binaryExpression(IN_TOKEN, first, parseShiftExpression());
             result = parseRelationalExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseRelationalExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * EqualityExpression(mode)
      *     RelationalExpression(mode)
      *     EqualityExpression(mode) == RelationalExpression(mode)
      *     EqualityExpression(mode) != RelationalExpression(mode)
      *     EqualityExpression(mode) === RelationalExpression(mode)
      *     EqualityExpression(mode) !== RelationalExpression(mode)
      */
 
     public Node parseEqualityExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseEqualityExpression");
         }
 
         Node result;
         Node first;
 
         first = parseRelationalExpression(mode);
         result = parseEqualityExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseEqualityExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public Node parseEqualityExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseEqualityExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(EQUALS_TOKEN))
         {
             match(EQUALS_TOKEN);
             first = nodeFactory.binaryExpression(EQUALS_TOKEN, first, parseRelationalExpression(mode));
             result = parseEqualityExpressionPrime(mode, first);
         }
         else if (lookahead(NOTEQUALS_TOKEN))
         {
             match(NOTEQUALS_TOKEN);
             first = nodeFactory.binaryExpression(NOTEQUALS_TOKEN, first, parseRelationalExpression(mode));
             result = parseEqualityExpressionPrime(mode, first);
         }
         else if (lookahead(STRICTEQUALS_TOKEN))
         {
             match(STRICTEQUALS_TOKEN);
             first = nodeFactory.binaryExpression(STRICTEQUALS_TOKEN, first, parseRelationalExpression(mode));
             result = parseEqualityExpressionPrime(mode, first);
         }
         else if (HAS_STRICTNOTEQUALS && lookahead(STRICTNOTEQUALS_TOKEN))
         {
             match(STRICTNOTEQUALS_TOKEN);
             first = nodeFactory.binaryExpression(STRICTNOTEQUALS_TOKEN, first, parseRelationalExpression(mode));
             result = parseEqualityExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseEqualityExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseAndExpression(mode)
      *     EqualityExpression(mode)
      *     BitwiseAndExpression(mode) & EqualityExpression(mode)
      */
 
     public Node parseBitwiseAndExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseAndExpression");
         }
 
         Node result;
         Node first;
 
         first = parseEqualityExpression(mode);
         result = parseBitwiseAndExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseAndExpression");
         }
 
         return result;
     }
 
     public Node parseBitwiseAndExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseAndExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(BITWISEAND_TOKEN))
         {
             match(BITWISEAND_TOKEN);
             first = nodeFactory.binaryExpression(BITWISEAND_TOKEN, first, parseEqualityExpression(mode));
             result = parseBitwiseAndExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseAndExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseXorExpression(mode)
      *     BitwiseAndExpression(mode)
      *     BitwiseXorExpression(mode) ^ BitwiseAndExpression(mode)
      */
 
     public Node parseBitwiseXorExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseXorExpression");
         }
 
         Node result;
         Node first;
 
         first = parseBitwiseAndExpression(mode);
         result = parseBitwiseXorExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseXorExpression");
         }
 
         return result;
     }
 
     public Node parseBitwiseXorExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseXorExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(BITWISEXOR_TOKEN))
         {
             match(BITWISEXOR_TOKEN);
             first = nodeFactory.binaryExpression(BITWISEXOR_TOKEN, first, parseBitwiseAndExpression(mode));
             result = parseBitwiseXorExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseXorExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * BitwiseOrExpression(mode)
      *     BitwiseXorExpression(mode)
      *     BitwiseOrExpression(mode) | BitwiseXorExpression(mode)
      */
 
     public Node parseBitwiseOrExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseOrExpression");
         }
 
         Node result;
         Node first;
 
         first = parseBitwiseXorExpression(mode);
         result = parseBitwiseOrExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseBitwiseOrExpression");
         }
 
         return result;
     }
 
     public Node parseBitwiseOrExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBitwiseOrExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(BITWISEOR_TOKEN))
         {
             match(BITWISEOR_TOKEN);
             first = nodeFactory.binaryExpression(BITWISEOR_TOKEN, first, parseBitwiseXorExpression(mode));
             result = parseBitwiseOrExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseBitwiseOrExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalAndExpression
      *     BitwiseOrExpression
      *     LogicalAndExpression && BitwiseOrExpression
      */
 
     public Node parseLogicalAndExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalAndExpression");
         }
 
         Node result;
         Node first;
 
         first = parseBitwiseOrExpression(mode);
         result = parseLogicalAndExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalAndExpression");
         }
 
         return result;
     }
 
     public Node parseLogicalAndExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalAndExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(LOGICALAND_TOKEN))
         {
             match(LOGICALAND_TOKEN);
             first = nodeFactory.binaryExpression(LOGICALAND_TOKEN, first, parseBitwiseOrExpression(mode));
             result = parseLogicalAndExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalAndExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalXorExpression
      *     LogicalAndExpression
      *     LogicalXorExpression ^^ LogicalAndExpression
      */
 
     public Node parseLogicalXorExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalXorExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalAndExpression(mode);
         result = parseLogicalXorExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalXorExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public Node parseLogicalXorExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalXorExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(LOGICALXOR_TOKEN))
         {
             match(LOGICALXOR_TOKEN);
             first = nodeFactory.binaryExpression(LOGICALXOR_TOKEN, first, parseLogicalAndExpression(mode));
             result = parseLogicalXorExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalXorExpressionPrime with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * LogicalOrExpression
      *     LogicalXorExpression
      *     LogicalOrExpression || LogicalXorExpression
      */
 
     public Node parseLogicalOrExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalOrExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalXorExpression(mode);
         result = parseLogicalOrExpressionPrime(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseLogicalOrExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public Node parseLogicalOrExpressionPrime(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseLogicalOrExpressionPrime");
         }
 
         Node result;
 
         if (lookahead(LOGICALOR_TOKEN))
         {
             match(LOGICALOR_TOKEN);
             first = nodeFactory.binaryExpression(LOGICALOR_TOKEN, first, parseLogicalXorExpression(mode));
             result = parseLogicalOrExpressionPrime(mode, first);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseLogicalOrExpressionPrime");
         }
 
         return result;
     }
 
     /*
      * ConditionalExpression
      *     LogicalOrExpression
      *     LogicalOrExpression ? AssignmentExpression : AssignmentExpression
      */
 
     public Node parseConditionalExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseConditionalExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalOrExpression(mode);
 
         if (lookahead(QUESTIONMARK_TOKEN))
         {
             match(QUESTIONMARK_TOKEN);
             Node second;
             Node third;
             second = parseAssignmentExpression(mode);
             match(COLON_TOKEN);
             third = parseAssignmentExpression(mode);
             result = nodeFactory.conditionalExpression(first, second, third);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseConditionalExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * NonAssignmentExpression
      *     LogicalOrExpression
      *     LogicalOrExpression ? NonAssignmentExpression : NonAssignmentExpression
      */
 
     public Node parseNonAssignmentExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseNonAssignmentExpression");
         }
 
         Node result;
         Node first;
 
         first = parseLogicalOrExpression(mode);
 
         if (lookahead(QUESTIONMARK_TOKEN))
         {
             match(QUESTIONMARK_TOKEN);
             Node second;
             Node third;
             second = parseNonAssignmentExpression(mode);
             match(COLON_TOKEN);
             third = parseNonAssignmentExpression(mode);
             result = nodeFactory.conditionalExpression(first, second, third);
         }
         else
         {
             result = first;
         }
 
 
         if (debug)
         {
             System.err.println("finish parseNonAssignmentExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * AssignmentExpression(mode)
      *     ConditionalExpression
      *     SuperExpression CompoundAssignmentExpressionSuffix(mode)
      *     PostfixExpression AssignmentExpressionSuffix(mode)
      *
      * AssignmentExpressionSuffix(mode)
      *       CompoundAssignmentExpressionSuffix(mode)
      *       LogicalAssignment AssignmentExpression(mode)
      *       = AssigmentExpression(mode)
      *
      * CompoundAssignmentExpressionSuffix(mode)
      *     CompoundAssignment SuperExpression
      *     CompoundAssignment AssignmentExpression(mode)
      */
 
     public Node parseAssignmentExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseAssignmentExpression");
         }
 
         Node result;
         Node first;
 
         first = parseConditionalExpression(mode);
         result = parseAssignmentExpressionSuffix(mode, first);
 
         if (debug)
         {
             System.err.println("finish parseAssignmentExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public Node parseAssignmentExpressionSuffix(int mode, Node first)
     {
 
         if (debug)
         {
             System.err.println("begin parseAssignmentExpressionSuffix");
         }
 
         Node result;
         int second = ERROR_TOKEN;
 
         if (lookahead(ASSIGN_TOKEN) ? ((second = match(ASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(MULTASSIGN_TOKEN) ? ((second = match(MULTASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(DIVASSIGN_TOKEN) ? ((second = match(DIVASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(MODULUSASSIGN_TOKEN) ? ((second = match(MODULUSASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(PLUSASSIGN_TOKEN) ? ((second = match(PLUSASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(MINUSASSIGN_TOKEN) ? ((second = match(MINUSASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(LEFTSHIFTASSIGN_TOKEN) ? ((second = match(LEFTSHIFTASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(RIGHTSHIFTASSIGN_TOKEN) ? ((second = match(RIGHTSHIFTASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(UNSIGNEDRIGHTSHIFTASSIGN_TOKEN) ? ((second = match(UNSIGNEDRIGHTSHIFTASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(BITWISEANDASSIGN_TOKEN) ? ((second = match(BITWISEANDASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(BITWISEXORASSIGN_TOKEN) ? ((second = match(BITWISEXORASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(BITWISEORASSIGN_TOKEN) ? ((second = match(BITWISEORASSIGN_TOKEN)) != ERROR_TOKEN) : false)
         {
             // ACTION: verify first.isPostfixExpression();
 
             Node third;
 
             third = parseAssignmentExpression(mode);
             result = nodeFactory.assignmentExpression(first, second, third);
 
         }
         else if (HAS_LOGICALASSIGNMENT &&
             lookahead(LOGICALANDASSIGN_TOKEN) ? ((second = match(LOGICALANDASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(LOGICALXORASSIGN_TOKEN) ? ((second = match(LOGICALXORASSIGN_TOKEN)) != ERROR_TOKEN) :
             lookahead(LOGICALORASSIGN_TOKEN) ? ((second = match(LOGICALORASSIGN_TOKEN)) != ERROR_TOKEN) : false)
         {
 
             // ACTION: verify first.isPostfixExpression();
 
             Node third;
             third = parseAssignmentExpression(mode);
             result = nodeFactory.assignmentExpression(first, second, third);
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseAssignmentExpressionSuffix with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * ListExpression
      *     AssignmentExpression
      *     ListExpression , AssignmentExpression
      */
 
     public ListNode parseListExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseListExpression");
         }
 
         ListNode result;
         Node first;
 
         first = parseAssignmentExpression(mode);
         result = parseListExpressionPrime(mode, nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseListExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public ListNode parseListExpressionPrime(int mode, ListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseListExpressionPrime");
         }
 
         ListNode result;
 
         // , AssignmentExpression ListExpressionPrime
 
         if (lookahead(COMMA_TOKEN))
         {
             match(COMMA_TOKEN);
 
             Node second;
 
             second = parseAssignmentExpression(mode);
             result = parseListExpressionPrime(mode, nodeFactory.list(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseListExpression with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     public IdentifierNode parseNonAttributeQualifiedExpression()
     {
         if (debug)
         {
             System.err.println("begin parseNonAttributeQualifiedExpression");
         }
         IdentifierNode result;
         if(lookahead(LEFTPAREN_TOKEN))
         {
             result = parseExpressionQualifiedIdentifier();
         }
         else
         {
             result = parseSimpleQualifiedIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseNonAttributeQualifiedExpression");
         }
 
         return result;
     }
 
     public Node parseTypeName()
     {
         Node first = parseTypeIdentifier();
         return first;//nodeFactory.memberExpression(null, nodeFactory.getExpression(first, first.pos()),first.pos());
     }
 
     public ListNode parseTypeNameList()
     {
         ListNode type_list = null;
 
         type_list = nodeFactory.list(type_list, parseTypeName());
         while( lookahead(COMMA_TOKEN) )
         {
             match(COMMA_TOKEN);
             type_list = nodeFactory.list(type_list, parseTypeName());
         }
 
         return type_list;
     }
 
     public Node parseSimpleTypeIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parseSimpleTypeIdentifier");
         }
 
         Node result;
         Node first;
         // TODO: handle packageidentifier.identifier
 //        if( lookahead(PACKAGEIDENTIFIER_TOKEN))
         {
 
         }
 //        else
         {
             first = parseNonAttributeQualifiedExpression();
             result = nodeFactory.memberExpression(null, nodeFactory.getExpression(first, first.pos()),first.pos());
 
             while( lookahead(DOT_TOKEN) )
             {
                 // TODO: this should only be for real package identifiers
                 match(DOT_TOKEN);
                 result = nodeFactory.memberExpression(result, nodeFactory.getExpression(parseNonAttributeQualifiedExpression()));
             }
         }
         if (debug)
         {
             System.err.println("finish parseSimpleTypeIdentifier");
         }
 
         return result;
     }
 
     public Node parseTypeIdentifier()
     {
         if (debug)
         {
             System.err.println("begin parseTypeIdentifier");
         }
 
         Node result;
         Node first = parseSimpleTypeIdentifier();
 
         // TODO: parse <...> type expression list
         if( lookahead(DOTLESSTHAN_TOKEN))
         {
             match(DOTLESSTHAN_TOKEN);
             result = nodeFactory.applyTypeExpr(first, parseTypeExpressionList(),ctx.input.positionOfMark());
             if( lookahead(UNSIGNEDRIGHTSHIFT_TOKEN) )
             {
                 // Transform >>> to >> and eat one >
                 nexttoken = RIGHTSHIFT_TOKEN;
             }
             else if( lookahead( RIGHTSHIFT_TOKEN ) )
             {
                 // Transform >> to > and eat one >
                 nexttoken = GREATERTHAN_TOKEN;
             }
             else
             {
                 match(GREATERTHAN_TOKEN);
             }
         }
 
         result = first;
 
         if (debug)
         {
             System.err.println("finish parseTypeIdentifier");
         }
 
         return result;
     }
     /*
      * TypeExpression
      *     NonassignmentExpression
      */
 
     public Node parseTypeExpression(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpression");
         }
 
         Node result;
 
         if( ctx.dialect(7) && thisToken == IDENTIFIER_TOKEN && scanner.getTokenText(nexttoken).equals("Object") )
         {
             match(IDENTIFIER_TOKEN);
             result = null;  // means *
         }
         else
         {
             Node first = parseTypeIdentifier();
             boolean is_nullable = true;
             boolean is_explicit = false;
             if( lookahead(NOT_TOKEN) )
             {
                 match(NOT_TOKEN);
                 is_nullable = false;
                 is_explicit = true;
             }
             else if( lookahead(QUESTIONMARK_TOKEN) )
             {
                 match(QUESTIONMARK_TOKEN);
                 is_nullable = true;
                 is_explicit = true;
             }
 
             result = nodeFactory.typeExpression(first, is_nullable, is_explicit, -1);
         }
 
         if (debug)
         {
             System.err.println("finish parseTypeExpression");
         }
 
         return result;
     }
 
     /*
      * Statementw
      *     SuperStatement Semicolonw
      *     Block
      *     IfStatementw
      *     SwitchStatement
      *     DoStatement Semicolonw
      *     WhileStatementw
      *     ForStatementw
      *     WithStatementw
      *     ContinueStatement Semicolonw
      *     BreakStatement Semicolonw
      *     ReturnStatement Semicolonw
      *     ThrowStatement Semicolonw
      *     TryStatement
      *     LabeledStatementw
      *     ExpressionStatement Semicolonw
      */
 
     public Node parseStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseStatement");
         }
 
         Node result;
 
         if (lookahead(SUPER_TOKEN))
         {
             result = parseSuperStatement(mode);
             // Moved into parseSuperStatement to allow for AS3 super expressions:
             //     matchSemicolon(mode);
         }
         else if (lookahead(LEFTBRACE_TOKEN))
         {
             StatementListNode sln = parseBlock();
             result = sln;
         }
         else if( lookahead(LEFTBRACKET_TOKEN) || lookahead(XMLLITERAL_TOKEN)|| lookahead(DOCCOMMENT_TOKEN) )
         {
             result = parseMetaData();
         }
         else if (lookahead(IF_TOKEN))
         {
             result = parseIfStatement(mode);
         }
         else if (lookahead(SWITCH_TOKEN))
         {
             result = parseSwitchStatement();
         }
         else if (lookahead(DO_TOKEN))
         {
             result = parseDoStatement();
             matchSemicolon(mode);
         }
         else if (lookahead(WHILE_TOKEN))
         {
             result = parseWhileStatement(mode);
         }
         else if (lookahead(FOR_TOKEN))
         {
             result = parseForStatement(mode);
         }
         else if (lookahead(WITH_TOKEN))
         {
             result = parseWithStatement(mode);
         }
         else if (lookahead(CONTINUE_TOKEN))
         {
             result = parseContinueStatement();
             matchSemicolon(mode);
         }
         else if (lookahead(BREAK_TOKEN))
         {
             result = parseBreakStatement();
             matchSemicolon(mode);
         }
         else if (lookahead(RETURN_TOKEN))
         {
             result = parseReturnStatement();
             matchSemicolon(mode);
         }
         else if (lookahead(THROW_TOKEN))
         {
             result = parseThrowStatement();
             matchSemicolon(mode);
         }
         else if (lookahead(TRY_TOKEN))
         {
             result = parseTryStatement();
         }
         else
         {
             result = parseLabeledOrExpressionStatement(mode);
             if (!result.isLabeledStatement())
             {
                 matchSemicolon(mode);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseStatement");
         }
 
         return result;
     }
 
     /*
      * Substatementw
   *     EmptyStatement
   *     Statementw
   *     SimpleVariableDefinition Semicolonw
   *     Attributes [no line break] { Substatements }
      */
 
     public Node parseSubstatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseSubstatement");
         }
 
         Node result;
 
         if (lookahead(SEMICOLON_TOKEN))
         {
             match(SEMICOLON_TOKEN);
             result = nodeFactory.emptyStatement();
         }
         else if (lookahead(VAR_TOKEN))
         {
             result = parseVariableDefinition(null/*attrs*/, mode);
             matchSemicolon(mode);
         }
         else
         {
             result = parseAnnotatedSubstatementsOrStatement(mode);
         }
 
         if (debug)
         {
             System.err.println("finish parseSubstatement");
         }
 
         return result;
     }
 
 
 
     /*
      * SuperStatement
      *     super Arguments
      */
 
     public Node parseSuperStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseSuperStatement");
         }
 
         Node result;
         Node first;
 
         match(SUPER_TOKEN);
         first = nodeFactory.superExpression(null,ctx.input.positionOfMark() );
 
         if (lookahead(LEFTPAREN_TOKEN))  // super statement
         {
             Node n = parseArguments(first);
             CallExpressionNode call = (n instanceof CallExpressionNode) ? (CallExpressionNode) n : null;  // this returns a call expression node
             if (call == null)
             {
                 ctx.internalError("Internal error in parseSuperStatement()");
             }
             if (lookaheadSemicolon(mode))
             {
                 result = nodeFactory.superStatement(call);
                 matchSemicolon(mode);
             }
             else // super expression
             {
                 if (!(call != null && call.args != null && call.args.size() == 1))
                 {
                     error(kError_Parser_WrongNumberOfSuperArgs);
                 }
                 else
                 {
                     SuperExpressionNode se = first instanceof SuperExpressionNode ? (SuperExpressionNode)first : null;
                     if( se != null )
                     {
                         se.expr = call.args.items.get(0);
                     }
                     else
                     {
                         ctx.internalError("internal error in super expression");
                     }
                 }
                 first = parseFullPostfixExpressionPrime(first);
                 result = nodeFactory.expressionStatement(parseListExpressionPrime(mode, nodeFactory.list(null, first)));
                 matchSemicolon(mode);
             }
         }
         else  // super expression
         {
             first = parseFullPostfixExpressionPrime(first);
             if (lookahead(ASSIGN_TOKEN)) // ISSUE: what about compound assignment?
             {
                 first = parseAssignmentExpressionSuffix(mode, first);
             }
             result = nodeFactory.expressionStatement(parseListExpressionPrime(mode, nodeFactory.list(null, first)));
             matchSemicolon(mode);
         }
 
         if (debug)
         {
             System.err.println("finish parseSuperStatement");
         }
 
         return result;
     }
 
     /*
      * LabeledOrExpressionStatement
      *     Identifier : Statement
      *     [lookahead?{function, {, const }] ListExpressionallowIn ;
      */
 
     public Node parseLabeledOrExpressionStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseLabeledOrExpressionStatement");
         }
 
         Node result;
 
         {
             ListNode first;
             first = parseListExpression(allowIn_mode);
             if (lookahead(COLON_TOKEN))
             {
                 if (!first.isLabel())
                 {
                     error(kError_Parser_LabelIsNotIdentifier);
                 }
                 match(COLON_TOKEN);
                 result = nodeFactory.labeledStatement(first, parseSubstatement(mode));
             }
             else
             {
                 result = nodeFactory.expressionStatement(first);
                 // leave matchSemicolon(mode) for caller
             }
         }
 
 
         if (debug)
         {
             System.err.println("finish parseLabeledOrExpressionStatement with " + ((result != null) ? result.toString() : ""));
         }
 
         return result;
     }
 
     /*
      * Block
      *     { Directives }
      */
     public StatementListNode parseBlock()
     {
         return parseBlock(null);
     }
 
     public StatementListNode parseBlock(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseBlock");
         }
 
         StatementListNode result;
 
         match(LEFTBRACE_TOKEN);
         result = parseDirectives(first, null/*stmts*/);
         match(RIGHTBRACE_TOKEN);
 
         if (debug)
         {
             System.err.println("finish parseBlock");
         }
 
         return result;
     }
 
     public MetaDataNode parseMetaData()
     {
         if (debug)
         {
             System.err.println("begin parseMetaData");
         }
 
         int pos = scanner.input.positionOfMark();
         MetaDataNode result;
 
         if( lookahead(DOCCOMMENT_TOKEN) )
         {
             ListNode list = nodeFactory.list(null,nodeFactory.literalString(scanner.getTokenText(match(DOCCOMMENT_TOKEN)),ctx.input.positionOfMark()));
             LiteralXMLNode first = nodeFactory.literalXML(list,false/*is_xmllist*/,ctx.input.positionOfMark());
             Node x = nodeFactory.memberExpression(null,nodeFactory.getExpression(first),pos);
             result = nodeFactory.docComment(nodeFactory.literalArray(nodeFactory.argumentList(null,x),pos),pos);
         }
         else if( lookahead(XMLLITERAL_TOKEN) )
         {
             result = nodeFactory.metaData(nodeFactory.literalArray(
                                             nodeFactory.argumentList(null,parseXMLLiteral())),pos);
         }
         else
         {
             result = nodeFactory.metaData(parseArrayLiteral(),pos);
         }
 
         if (debug)
         {
             System.err.println("end parseMetaData");
         }
 
         return result;
     }
 
     /*
      * IfStatement
      *     if ParenExpressionNode Statement
      *     if ParenExpressionNode Statement else Statement
      */
 
     public Node parseIfStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseIfStatement");
         }
 
         Node result;
         ListNode first;
         Node second;
         Node third = null;
 
         match(IF_TOKEN);
         first = parseParenListExpression();
         second = parseSubstatement(abbrevIfElse_mode);
         if (lookahead(ELSE_TOKEN))
         {
             match(ELSE_TOKEN);
             third = parseSubstatement(mode);
         }
 
         result = nodeFactory.ifStatement(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseIfStatement");
         }
 
         return result;
     }
 
     /*
      * SwitchStatement
      *     switch ParenListExpression { CaseStatements }
      */
 
     public Node parseSwitchStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseSwitchStatement");
         }
 
         Node result;
         Node first;
         StatementListNode second;
 
         match(SWITCH_TOKEN);
         first = parseParenListExpression();
         match(LEFTBRACE_TOKEN);
         second = parseCaseStatements();
         match(RIGHTBRACE_TOKEN);
 
         result = nodeFactory.switchStatement(first, second);
 
         if (debug)
         {
             System.err.println("finish parseSwitchStatement");
         }
 
         return result;
     }
 
     /*
      * CaseStatement
      *     Statement
      *     CaseLabel
      */
 
     public Node parseCaseStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatement");
         }
 
         Node result;
 
         if (lookahead(CASE_TOKEN) || lookahead(DEFAULT_TOKEN))
         {
             result = parseCaseLabel();
         }
         else
         {
             result = parseDirective(null, mode);
         }
 
         if (debug)
         {
             System.err.println("finish parseCaseStatement");
         }
 
         return result;
     }
 
     /*
      * CaseLabel
      *     case ListExpressionallowIn :
      *     default :
      */
 
     public Node parseCaseLabel()
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseLabel");
         }
 
         Node result;
 
         if (lookahead(CASE_TOKEN))
         {
             match(CASE_TOKEN);
             result = nodeFactory.caseLabel(parseListExpression(allowIn_mode),ctx.input.positionOfMark());
         }
         else if (lookahead(DEFAULT_TOKEN))
         {
             match(DEFAULT_TOKEN);
             result = nodeFactory.caseLabel(null,ctx.input.positionOfMark()); // 0 argument means default case.
         }
         else
         {
             error(kError_Parser_ExpectedCaseLabel);
             skiperror(RIGHTBRACE_TOKEN);
             result = null;
         }
         match(COLON_TOKEN);
 
         if (debug)
         {
             System.err.println("finish parseCaseLabel");
         }
 
         return result;
     }
 
     /*
      * CaseStatements
      *     empty
      *     CaseLabel
      *     CaseLabel CaseStatementsPrefix CaseStatementabbrev
      */
 
     public StatementListNode parseCaseStatements()
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatements");
         }
 
         StatementListNode result;
 
         if (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
         {
             Node first;
             first = parseCaseLabel();
             if (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
             {
                 result = parseCaseStatementsPrefix(nodeFactory.statementList(null, first));
             }
             else
             {
                 result = nodeFactory.statementList(null, first);
             }
 
         }
         else
         {
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseCaseStatements");
         }
 
         return result;
     }
 
     /*
      * CaseStatementsPrefix
      *     empty
      *     CaseStatement[full] CaseStatementsPrefix
      */
 
     public StatementListNode parseCaseStatementsPrefix(StatementListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseCaseStatementsPrefix");
         }
 
         StatementListNode result = null;
 
         if (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
         {
             first = nodeFactory.statementList(first, parseCaseStatement(full_mode));
             while (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
             {
                 first = nodeFactory.statementList(first, parseCaseStatement(full_mode));
             }
             result = first;
         }
 
 
         if (debug)
         {
             System.err.println("finish parseCaseStatementsPrefix");
         }
 
         return result;
     }
 
     /*
      * DoStatement
      *     do Statement[abbrev] while ParenListExpression
      */
 
     public Node parseDoStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseDoStatement");
         }
 
         Node result;
         Node first;
 
         match(DO_TOKEN);
         first = parseSubstatement(abbrevDoWhile_mode);
         match(WHILE_TOKEN);
         result = nodeFactory.doStatement(first, parseParenListExpression());
 
         if (debug)
         {
             System.err.println("finish parseDoStatement");
         }
 
         return result;
     }
 
     /*
      * WhileStatement
      *     while ParenListExpression Statement
      */
 
     public Node parseWhileStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseWhileStatement");
         }
 
         Node result;
 
         match(WHILE_TOKEN);
         Node first = parseParenListExpression();
         Node second = parseSubstatement(mode);
         result = nodeFactory.whileStatement(first, second);
 
         if (debug)
         {
             System.err.println("finish parseWhileStatement");
         }
 
         return result;
     }
 
     /*
      * ForStatementw
      *     for ( ForInitializer ; OptionalExpression ; OptionalExpression ) Substatementw
      *     for ( ForInBinding in ListExpressionallowIn ) Substatementw
      *
      * ForInitializer
      *     empty
      *     ListExpressionnoIn
      *     VariableDefinitionnoIn
      *     //Attributes [no line break] VariableDefinitionnoIn
      *
      * ForInBinding
      *     PostfixExpression
      *     VariableDefinitionKind VariableBindingnoIn
      *     //Attributes [no line break] VariableDefinitionKind VariableBindingnoIn
      */
 
     public Node parseForStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseForStatement");
         }
 
         Node result;
         boolean is_each = false;
 
         match(FOR_TOKEN);
 
         if( lookahead(IDENTIFIER_TOKEN) )
         {
             String id = scanner.getTokenText(match(IDENTIFIER_TOKEN));
             if( !id.equals("each") )
             {
                 error(syntax_error,kError_Parser_ExpectedLeftParen);
             }
             is_each = true;
         }
 
         match(LEFTPAREN_TOKEN);
 
         Node first;
 
         if (lookahead(SEMICOLON_TOKEN))
         {
             first = null;
         }
         else if (lookahead(CONST_TOKEN) || lookahead(VAR_TOKEN))
         {
             first = parseVariableDefinition(null/*attrs*/, noIn_mode);
         }
         else
         if( is_each )
         {
             first = nodeFactory.list(null,parsePostfixExpression());
         }
         else
         {
             first = parseListExpression(noIn_mode);
         }
 
         Node second;
         if (lookahead(IN_TOKEN))
         {
             // ISSUE: verify that first is a single expression or variable definition
 
             if( first instanceof VariableDefinitionNode && ((VariableDefinitionNode)first).list.size() > 1)
             {
                 error(syntax_error, kError_ParserInvalidForInInitializer);
             }
             else if( first instanceof ListNode && ((ListNode)first).size() > 1 )
             {
                 error(syntax_error, kError_ParserInvalidForInInitializer);
             }
             match(IN_TOKEN);
             second = parseListExpression(allowIn_mode);
             match(RIGHTPAREN_TOKEN);
             int pos = ctx.input.positionOfMark();
             result = nodeFactory.forInStatement(is_each, first, second, parseSubstatement(mode), pos);
         }
         else
         if( lookahead( COLON_TOKEN ) )
         {
             match(IN_TOKEN); //error(syntax_error,kError_Parser_EachWithoutIn);
             skiperror(LEFTBRACE_TOKEN);
             result = parseSubstatement(mode);
         }
         else
         {
             if( is_each )
             {
                 error(syntax_error,kError_Parser_EachWithoutIn);
             }
             Node third;
             match(SEMICOLON_TOKEN);
             if (lookahead(SEMICOLON_TOKEN))
             {
                 second = null;
             }
             else
             {
                 second = parseListExpression(allowIn_mode);
             }
             match(SEMICOLON_TOKEN);
 
             if (lookahead(RIGHTPAREN_TOKEN))
             {
                 third = null;
             }
             else
             {
                 third = parseListExpression(allowIn_mode);
             }
             match(RIGHTPAREN_TOKEN);
             int pos = ctx.input.positionOfMark();
             result = nodeFactory.forStatement(first, second, third, parseSubstatement(mode),false/*is_forin*/, pos);
         }
         return result;
     }
 
     /*
      * WithStatement
      *     with ParenListExpression Statement
      */
 
     public Node parseWithStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseWithStatement");
         }
 
         Node result;
 
         match(WITH_TOKEN);
         Node first = parseParenListExpression();
         result = nodeFactory.withStatement(first, parseSubstatement(mode));
 
         if (debug)
         {
             System.err.println("finish parseWithStatement");
         }
 
         return result;
     }
 
     /*
      * ContinueStatement
      *     continue
      *     continue [no line break] Identifier
      */
 
     public Node parseContinueStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseContinueStatement");
         }
 
         Node result;
         IdentifierNode first = null;
 
         match(CONTINUE_TOKEN);
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseIdentifier();
         }
 
         result = nodeFactory.continueStatement(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseContinueStatement");
         }
 
         return result;
     }
 
     /*
      * BreakStatement
      *     break
      *     break [no line break] Identifier
      */
 
     public Node parseBreakStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseBreakStatement");
         }
 
         Node result;
         IdentifierNode first = null;
 
         match(BREAK_TOKEN);
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseIdentifier();
         }
 
         result = nodeFactory.breakStatement(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseBreakStatement");
         }
 
         return result;
     }
 
     /*
      * ReturnStatement
      *     return
      *     return [no line break] ExpressionallowIn
      */
 
     public Node parseReturnStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseReturnStatement");
         }
 
         Node result;
         Node first = null;
 
         match(RETURN_TOKEN);
 
         // ACTION: check for VirtualSemicolon
 
         if (!lookaheadSemicolon(full_mode))
         {
             first = parseListExpression(allowIn_mode);
         }
 
         result = nodeFactory.returnStatement(first, ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseReturnStatement");
         }
 
         return result;
     }
 
     /*
      * ThrowStatement
      *     throw [no line break] ListExpression[allowIn]
      */
 
     public Node parseThrowStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseThrowStatement");
         }
 
         Node result;
 
         match(THROW_TOKEN);
         lookahead(EMPTY_TOKEN);  // ACTION: fix newline() so that we don't
         // have to force a lookahead to the next
         // token before it works properly.
         if (newline())
         {
             error(syntax_error, kError_Parser_ThrowWithoutExpression,"","",ctx.input.positionOfMark()-8);   // -8 gets us to the first char of throw
             result = nodeFactory.throwStatement(null,ctx.input.positionOfMark());    // make a dummy node
         }
         else
         {
             result = nodeFactory.throwStatement(parseListExpression(allowIn_mode),ctx.input.positionOfMark());
         }
 
         if (debug)
         {
             System.err.println("finish parseThrowStatement");
         }
 
         return result;
     }
 
     /*
      * TryStatement
      *     try AnnotatedBlock CatchClauses
      *     try AnnotatedBlock FinallyClause
      *     try AnnotatedBlock CatchClauses FinallyClause
      */
 
     public Node parseTryStatement()
     {
 
         if (debug)
         {
             System.err.println("begin parseTryStatement");
         }
 
         Node result;
         StatementListNode first;
 
         match(TRY_TOKEN);
         first = parseBlock();
         if (lookahead(CATCH_TOKEN))
         {
             StatementListNode second = parseCatchClauses();
             if (lookahead(FINALLY_TOKEN))
             {
                 result = nodeFactory.tryStatement(first, second, parseFinallyClause());
             }
             else
             {
                 result = nodeFactory.tryStatement(first, second, null);
             }
         }
         else if (lookahead(FINALLY_TOKEN))
         {
             // finally with no catch clause
             // force feed the list a default catch clause so finally works
             StatementListNode catchClause = nodeFactory.statementList(null,
                           nodeFactory.catchClause(null,
                           nodeFactory.statementList(null,
                           nodeFactory.throwStatement(
                           null, 0 ))));
 
             result = nodeFactory.tryStatement(first, catchClause, parseFinallyClause());
         }
         else
         {
             error(kError_Parser_ExpectingCatchOrFinally);
             skiperror(SEMICOLON_TOKEN);
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseTryStatement");
         }
 
         return result;
     }
 
     /*
      * CatchClauses
      *     CatchClause
      *     CatchClauses CatchClause
      */
 
     public StatementListNode parseCatchClauses()
     {
 
         if (debug)
         {
             System.err.println("begin parseCatchClauses");
         }
 
         StatementListNode result;
 
         result = nodeFactory.statementList(null, parseCatchClause());
         while (lookahead(CATCH_TOKEN))
         {
             result = nodeFactory.statementList(result, parseCatchClause());
         }
 
         if (debug)
         {
             System.err.println("finish parseCatchClauses");
         }
 
         return result;
     }
 
     /*
      * CatchClause
      *     catch ( Parameter ) AnnotatedBlock
      */
 
     public Node parseCatchClause()
     {
 
         if (debug)
         {
             System.err.println("begin parseCatchClause");
         }
 
         Node result;
         Node first;
 
         match(CATCH_TOKEN);
         match(LEFTPAREN_TOKEN);
         first = parseParameter();
         match(RIGHTPAREN_TOKEN);
 
         result = nodeFactory.catchClause(first, parseBlock());
 
         if (debug)
         {
             System.err.println("finish parseCatchClause");
         }
 
         return result;
     }
 
     /*
      * FinallyClause
      *     finally AnnotatedBlock
      */
 
     public FinallyClauseNode parseFinallyClause()
     {
 
         if (debug)
         {
             System.err.println("begin parseFinallyClause");
         }
 
         FinallyClauseNode result;
 
         match(FINALLY_TOKEN);
 
         // No line break.
 
         result = nodeFactory.finallyClause(parseBlock());
 
         if (debug)
         {
             System.err.println("finish parseFinallyClause");
         }
 
         return result;
     }
 
 
     // Directives
 
 
     /*
      * Directivew
      *     DefaultXMLNamespaceDirective
      *     EmptyStatement
      *     AnnotatableDirectivew
      *     Pragma Semicolonw
      *     Attributes [no line break] AnnotatableDirectivew
      *     Attributes [no line break] { Directives }
      *     Statementw
      */
 
     public Node parseDirective(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDirective");
         }
 
         Node result = null;
 
         try
         {
 
             if (lookahead(SEMICOLON_TOKEN))
             {
                 matchNoninsertableSemicolon(mode);
                 result = nodeFactory.emptyStatement();
             }
             else if (lookahead(VAR_TOKEN) || lookahead(CONST_TOKEN) ||
                 lookahead(FUNCTION_TOKEN) || lookahead(CLASS_TOKEN) ||
                 lookahead(NAMESPACE_TOKEN) || lookahead(IMPORT_TOKEN) ||
                 lookahead(INCLUDE_TOKEN) || lookahead(USE_TOKEN) ||
                 (HAS_INTERFACEDEFINITIONS && lookahead(INTERFACE_TOKEN)))
             {
                 result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
             }
             else
             if( lookahead(DEFAULT_TOKEN) )
             {
                 match(DEFAULT_TOKEN);
                 String id = scanner.getTokenText(match(IDENTIFIER_TOKEN));
                 if( !id.equals("xml") && lookahead(NAMESPACE_TOKEN) )
                 {
                     error(kError_Parser_ExpectedXMLBeforeNameSpace);
                 }
                 match(NAMESPACE_TOKEN);
                 match(ASSIGN_TOKEN);
                 result = nodeFactory.defaultXMLNamespace(parseNonAssignmentExpression(allowIn_mode),0);
             }
 /*            else
             if( lookahead(CONFIG_TOKEN) )
             {
                 result = parseConfigNamespaceDefinition(first);
             }
 */            else
             {
                 result = parseAnnotatedDirectiveOrStatement(mode);
             }
 
             if (debug)
             {
                 System.err.println("finish parseDirective with " + ((result != null) ? result.toString() : ""));
             }
 
         }
             /* C: SyntaxError not thrown in Parser...
             catch (SyntaxError x)
             {
                 System.err.println( "\nResyncing after syntax error");
             }
             */
         catch (Exception ex)
         {
 	        result = null;
 	        //assert false : ex.getMessage();
             // ex.printStackTrace();
             // System.err.println("\nInternal error: exception caught in parseDirective()");
         }
 
         return result;
     }
 
     /*
      *  AnnotatedDirectiveOrStatement
      *   [ true, false, private, public, Identifier ] Attributes [no line break] AnnotatableDirectivew
      *   [ true, false, private, public, Identifier ] Attributes [no line break] { Directives }
      *   Statementw
      */
 
     public Node parseAnnotatedDirectiveOrStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
 
         if (lookahead(SUPER_TOKEN) || lookahead(LEFTBRACE_TOKEN) ||
             lookahead(IF_TOKEN) || lookahead(SWITCH_TOKEN) ||
             lookahead(DO_TOKEN) || lookahead(WHILE_TOKEN) ||
             lookahead(FOR_TOKEN) || lookahead(WITH_TOKEN) ||
             lookahead(CONTINUE_TOKEN) || lookahead(BREAK_TOKEN) ||
             lookahead(RETURN_TOKEN) || lookahead(THROW_TOKEN) ||
             lookahead(TRY_TOKEN) || lookahead(LEFTBRACKET_TOKEN) || lookahead(DOCCOMMENT_TOKEN)) // doccomment_token)
         {
             result = parseStatement(mode);
         }
         else
         {
             Node temp;
             if (HAS_SQUAREBRACKETATTRS && lookahead(LEFTBRACKET_TOKEN))
             {
                 match(LEFTBRACKET_TOKEN);
                 temp = parseAssignmentExpression(allowIn_mode);
                 match(RIGHTBRACKET_TOKEN);
             }
             else
             {
                 temp = parseLabeledOrExpressionStatement(mode);
             }
             String attributeString = this.scanner.getTokenText(this.lastToken);
             String directiveString = this.scanner.getTokenText(this.nexttoken);
 
             if (!temp.isLabeledStatement())
             {
                 if (lookaheadSemicolon(mode) && !temp.isConfigurationName())
                 {
                     // If full mode then cannot be an attribute
                     matchSemicolon(mode);
                     result = temp;
 
                     ExpressionStatementNode estmt = (temp instanceof ExpressionStatementNode) ? (ExpressionStatementNode) temp : null;
                     if (estmt != null)
                     {
                         temp = estmt.expr;
                     }
                     boolean is_attribute_keyword = checkAttribute(block_kind_stack.last(),temp);
                     if( is_attribute_keyword )
                     {
                         error(syntax_error, kError_Parser_ExpectingAnnotatableDirectiveAfterAttributes, attributeString, directiveString);
                     }
                 }
                 else
                 if (temp.isAttribute())
                 {
                     // If its an expression statement, then extract the expression
                     ExpressionStatementNode estmt = (temp instanceof ExpressionStatementNode) ? (ExpressionStatementNode) temp : null;
                     if (estmt != null)
                     {
                         temp = estmt.expr;
                     }
 
                     boolean is_attribute_keyword = checkAttribute(block_kind_stack.last(),temp);
 
                     AttributeListNode first;
                     if (lookahead(TRUE_TOKEN)    || lookahead(FALSE_TOKEN) ||
                         lookahead(PRIVATE_TOKEN) || lookahead(PUBLIC_TOKEN) ||
                         lookahead(PROTECTED_TOKEN) || lookahead(IDENTIFIER_TOKEN))
                     {
                         first = nodeFactory.attributeList(temp, parseAttributes());
                     }
                     else
                     {
                         first = nodeFactory.attributeList(temp, null);
                     }
 
                     if (lookahead(VAR_TOKEN)       || lookahead(CONST_TOKEN) ||
                         lookahead(FUNCTION_TOKEN)  || lookahead(CLASS_TOKEN) ||
                         lookahead(NAMESPACE_TOKEN) || lookahead(IMPORT_TOKEN) ||
                         lookahead(USE_TOKEN) || lookahead(INTERFACE_TOKEN) ||
                         lookahead(DOCCOMMENT_TOKEN) )
                     {
                         result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
                     }
                     else if ( temp.isConfigurationName() && lookahead(LEFTBRACE_TOKEN) )
                     {
                         result = parseAnnotatableDirectiveOrPragmaOrInclude(first, mode);
                     }
                     else
                     if( lookahead(PACKAGE_TOKEN ))
                     {
                          error(kError_AttributesOnPackage);
                          result = parsePackageDefinition();
                     }
                     else
                     if( is_attribute_keyword || first.size() > 1 )
                     {
                         error(syntax_error, kError_Parser_ExpectingAnnotatableDirectiveAfterAttributes,attributeString, directiveString);
                         skiperror(SEMICOLON_TOKEN);
                     }
                 }
                 else
                 {
                 //    match(SEMICOLON_TOKEN); // force syntax error
                 }
             }
             else
             {
                 result = temp;
                 // no semi-colon necessary if labeled statement.
             }
 
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
 
         return result;
     }
 
     /*
      *  AnnotatedSubstatementsOrStatement
      */
 
     public Node parseAnnotatedSubstatementsOrStatement(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
 
         if (lookahead(SUPER_TOKEN) || lookahead(LEFTBRACE_TOKEN) ||
             lookahead(IF_TOKEN) || lookahead(SWITCH_TOKEN) ||
             lookahead(DO_TOKEN) || lookahead(WHILE_TOKEN) ||
             lookahead(FOR_TOKEN) || lookahead(WITH_TOKEN) ||
             lookahead(CONTINUE_TOKEN) || lookahead(BREAK_TOKEN) ||
             lookahead(RETURN_TOKEN) || lookahead(THROW_TOKEN) ||
             lookahead(TRY_TOKEN) || lookahead(LEFTBRACKET_TOKEN) ||
             lookahead(DOCCOMMENT_TOKEN))
         {
             result = parseStatement(mode);
         }
         else
         {
             Node temp;
             temp = parseLabeledOrExpressionStatement(mode);
             if (!temp.isLabeledStatement())
             {
                 if (lookaheadSemicolon(mode))
                 {
                     // If full mode then cannot be an attribute
                     matchSemicolon(mode);
                     result = temp;
                 }
                 else if (temp.isAttribute())
                 {
                     error(syntax_error,kError_InvalidAttribute);
 
                     /* cn: this is for the unimplemented named block feature
                      *
                     AttributeListNode first;
                     if (lookahead(TRUE_TOKEN) || lookahead(FALSE_TOKEN) ||
                         lookahead(PRIVATE_TOKEN) || lookahead(PUBLIC_TOKEN) ||
                         lookahead(PROTECTED_TOKEN) ||
                         lookahead(STATIC_TOKEN) || lookahead(IDENTIFIER_TOKEN))
                     {
                         first = nodeFactory.attributeList(temp, parseAttributes());
                     }
                     else
                     {
                         first = nodeFactory.attributeList(temp, null);
                     }
 
                     if (lookahead(LEFTBRACE_TOKEN))
                     {
                         result = parseSubstatements(first, null);
                     }
                     else
                     {
                         error(syntax_error, kError_Parser_ExpectingBlockStatement);
                         skiperror(SEMICOLON_TOKEN);
                     }
 
                     */
                 }
                 else
                 {
                     match(SEMICOLON_TOKEN); // force syntax error
                     skiperror(SEMICOLON_TOKEN);
                 }
             }
             else
             {
                 result = temp;
                 // no semi-colon necessary if labeled statement.
             }
 
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
 
         return result;
     }
 
     /*
      * Substatements
      */
 
     public StatementListNode parseSubstatements(AttributeListNode first, StatementListNode second)
     {
 
         if (debug)
         {
             System.err.println("begin parseSubstatements");
         }
 
         StatementListNode result = null;
         Node third;
 
         // ISSUE: ignoring attributes for now. Don't know what an attribute on a statement would do.
 
         while (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
         {
             third = parseSubstatement(abbrev_mode);
             second = nodeFactory.statementList(second, third);
         }
 
         result = second;
 
         if (debug)
         {
             System.err.println("finish parseSubstatements");
         }
 
         return result;
     }
 
     /*
      *  AnnotatableDirectivew
      *      VariableDefinitionallowIn Semicolonw
      *      FunctionDefinition
      *      ClassDefinition
      *      NamespaceDefinition Semicolonw
      *      ImportDirective Semicolonw
      *      UseDirective Semicolonw
      *      IncludeDirective Semicolonw
      */
 
     public Node parseAnnotatableDirectiveOrPragmaOrInclude(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseDefinition");
         }
 
         Node result = null;
 
         if (lookahead(CONST_TOKEN) || lookahead(VAR_TOKEN))
         {
             result = parseVariableDefinition(first, mode);
             matchSemicolon(mode);
         }
         else if (lookahead(FUNCTION_TOKEN))
         {
             result = parseFunctionDefinition(first);
         }
         else if (lookahead(CLASS_TOKEN))
         {
             result = parseClassDefinition(first, mode);
         }
         else if (lookahead(INTERFACE_TOKEN))
         {
             result = parseInterfaceDefinition(first, mode);
         }
         else if (lookahead(NAMESPACE_TOKEN))
         {
             result = parseNamespaceDefinition(first);
             matchSemicolon(mode);
         }
 /*        else if (lookahead(CONFIG_TOKEN))
         {
             result = parseConfigNamespaceDefinition(first);
             matchSemicolon(mode);
         }
 */        else if (lookahead(IMPORT_TOKEN))
         {
             result = parseImportDirective(first);
             matchSemicolon(mode);
         }
         else if (lookahead(USE_TOKEN))
         {
             result = parseUseDirectiveOrPragma(first);
             matchSemicolon(mode);
         }
         else if (lookahead(INCLUDE_TOKEN))
         {
             result = parseIncludeDirective();
             matchSemicolon(mode);
         }
         else if (lookahead(LEFTBRACE_TOKEN))
         {
         	StatementListNode stmts = parseBlock();
         	stmts.config_attrs = first;
         	result = stmts;
         	matchSemicolon(mode);
         }
         else
         {
             error(kError_Parser_DefinitionOrDirectiveExpected);
             skiperror(SEMICOLON_TOKEN);
         }
 
         if (debug)
         {
             System.err.println("finish parseDefinition");
         }
 
         return result;
     }
 
     public StatementListNode parseDirectives(AttributeListNode first, StatementListNode second)
     {
 
         if (debug)
         {
             System.err.println("begin parseDirectives");
         }
 
         StatementListNode result = null;
         Node third;
 
         while (!(lookahead(RIGHTBRACE_TOKEN) || lookahead(EOS_TOKEN)))
         {
             third = parseDirective(first, abbrev_mode);
 	        if (third == null)
 	        {
 		        break;
 	        }
             else if( third instanceof StatementListNode )
             {
                 StatementListNode sln = (StatementListNode) third;     // remove any gratuitous nestings
                 if( sln.config_attrs == null && !sln.has_pragma )
                 {
                     if( second == null )
                     {
                         second = sln;
                     }
                     else
                     {
                         second.items.addAll(sln.items);
                     }
                 }
                 else
                 {
                 	// Can't collapse into one statementlist node if the inner one might be compiled
                 	// out due to conditional compilation
                 	second = nodeFactory.statementList(second, third);
                 }
             }
             else if (third instanceof PragmaNode) {
                 // make sure it's at the top of the block
             	if (second != null) {
             		for (int i = 0; i < second.items.size(); i++) {
             			Node kid = second.items.at(i);
             			if (!(kid instanceof PragmaNode)) {
             				error(syntax_error,kError_Parser_NumericUseMisplaced);
             				break;
             			}
             		}
             	}
             	second = nodeFactory.statementList(second, third);
             	second.has_pragma = true;
             }
             else
             {
                 second = nodeFactory.statementList(second, third);
             }
         }
 
         result = second;
 
         if (debug)
         {
             System.err.println("finish parseDirectives");
         }
 
         return result;
     }
 
     /*
      * UseDirective
      *  use namespace ParenListExpression
      *
      * Pragma
      *  use PragmaItems
      *
      * PragmaItems
      *  PragmaItem
      *  PragmaItems , PragmaItem
      *
      * PragmaItem
      *  PragmaExpr
      *  PragmaExpr ?
      *
      * PragmaExpr
      *  Identifier
      *  Identifier ( PragmaArgument )
      *
      * PragmaArgument
      *  true
      *  false
      *  Number
      *  - Number
      *  - NegatedMinLong
      * String
      */
 
     public Node parseUseDirectiveOrPragma(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseUseDirectiveOrPragma");
         }
 
         Node result = null;
 
         match(USE_TOKEN);
         if (lookahead(NAMESPACE_TOKEN))
         {
             match(NAMESPACE_TOKEN);
             result = nodeFactory.useDirective(first, parseNonAssignmentExpression(allowIn_mode));
         }
         else if (lookahead(INCLUDE_TOKEN))  // for AS3 #include
         {
             result = parseIncludeDirective();
         }
         else
         {
             if( ctx.statics.es4_numerics )
                 result = nodeFactory.pragma(parsePragmaItems(allowIn_mode),ctx.input.positionOfMark());
             else
                 error(kError_UndefinedNamespace);  // Do what we used to do... better than an unlocalized internal error of feature not implemented
         }
 
         if (debug)
         {
             System.err.println("finish parseUseDirectiveOrPragma");
         }
 
         return result;
     }
 
 	public Node parsePragmaItem(int mode) {
         Node id;
         Node argument;
         if (debug)
         {
             System.err.println("begin parsePragmaItem");
         }
 
         id = parseIdentifier();
         if (lookahead(COMMA_TOKEN) || lookaheadSemicolon(mode)) {
             argument = null;
         } 
         else if (lookahead(TRUE_TOKEN)) {
             match(TRUE_TOKEN);
             argument = nodeFactory.literalBoolean(true,ctx.input.positionOfMark());
         }
         else if (lookahead(FALSE_TOKEN))
         {
             match(FALSE_TOKEN);
             argument = nodeFactory.literalBoolean(false,ctx.input.positionOfMark());
         }
         else if (lookahead(NUMBERLITERAL_TOKEN))
         {
             argument = nodeFactory.literalNumber(scanner.getTokenText(match(NUMBERLITERAL_TOKEN)),
                                                ctx.input.positionOfMark());
         }
         else if (lookahead(STRINGLITERAL_TOKEN))
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             argument = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
         }
         else argument = parseIdentifier();
         Node result = nodeFactory.usePragma(id, argument, ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parsePragmaItem");
         }
         return result;
     }
 
     public ListNode parsePragmaItems(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parsePragmaItems");
         }
 
         ListNode result = null;
 
         Node first = parsePragmaItem(mode);
 
         result = parsePragmaItemsPrime(nodeFactory.list(null, first), mode); 
 
         if (debug)
         {
             System.err.println("finish parsePragmaItems");
         }
         return result;
     }
 
 	public ListNode parsePragmaItemsPrime(ListNode first, int mode) {
         if (debug)
         {
             System.err.println("begin parsePragmaItemsPrime");
         }
 
         ListNode result;
 
         if (lookahead(COMMA_TOKEN)) {
             match(COMMA_TOKEN);
             Node second;
             second = parsePragmaItem(mode);
             result =  parsePragmaItemsPrime(nodeFactory.list(first, second), mode);
         }
         else
             result = first;
 
         if (debug)
         {
             System.err.println("finish parsePragmaItemsPrime");
         }
         return result;
     }
 
     /*
      * IncludeDirective
      *     include [no line break] String
      */
 
     public Node parseIncludeDirective()
     {
 
         if (debug)
         {
             System.err.println("begin parseIncludeDirective");
         }
 
         IncludeDirectiveNode result;
         LiteralStringNode first;
 
         match(INCLUDE_TOKEN);
 
         boolean[] is_single_quoted = new boolean[1];
         String filespec = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted).trim();
 
         CompilerHandler.FileInclude incl = null;
         if (ctx.handler != null)
         {
             incl = ctx.handler.findFileInclude(ctx.path(), filespec);
         }
 
 	    // The input could be an input stream or an in-memory string.
         InputStream in = null;
 	    String text = null;
 
         String fixed_filespec = null, parentPath = null;
         if (incl == null)
         {
             filespec = filespec.replace('/', File.separatorChar);
 
             File inc_file = new File(filespec);
             if (inc_file.isAbsolute())
             {
                 // absolute path
                	fixed_filespec = inc_file.getAbsolutePath();
             }
             else
             {
                 // must be a relative path
                 fixed_filespec = (ctx.path() == null ? "" : ctx.path()) + File.separator + filespec;
             }
 
             try
             {
             	fixed_filespec = new File(fixed_filespec).getCanonicalPath();
             }
             catch (IOException ex)
             {
             	fixed_filespec = new File(fixed_filespec).getAbsolutePath();
             }
             
             parentPath = fixed_filespec.substring(0, fixed_filespec.lastIndexOf(File.separator));
         	if (!ctx.scriptAssistParsing){
 	            try
 	            {
 	                in = new BufferedInputStream(new FileInputStream(fixed_filespec));
 	            }
 	            catch (FileNotFoundException ex)
 	            {
 	                error(syntax_error, kError_Parser_UnableToOpenFile, fixed_filespec);
 	                return null;
 	            }
         	}
         }
         else
         {
             fixed_filespec = incl.fixed_filespec;
             parentPath = incl.parentPath;
             in = incl.in;
 	        text = incl.text;
         }
 
         // make sure that we check the include path trail. This is to stop infinite recursion.
         if (ctx.statics.includePaths.contains(fixed_filespec))
         {
             error(syntax_error, kError_Parser_FileIncludesItself, fixed_filespec);
             try { in.close(); } catch (IOException ex) {}
             return null;
         }
         else
         {
             // add the file name to the include path trail.
             ctx.statics.includePaths.push_back(fixed_filespec);
         }
 
         // To get proper path resolution for included files inside of include directives,
         // temporarily update ctx.pathspec with spec (minus file name) for this file
 
         String oldCtxPathSpec = ctx.path();
         ctx.setPath(parentPath);
 
         // Reset to oldCtxPathSpec once we are finished parsing this file
 
         first  = nodeFactory.literalString(fixed_filespec,ctx.input.positionOfMark(), is_single_quoted[0]);
 
         ProgramNode second = null;
     	if (!ctx.scriptAssistParsing){
 	        Context cx = new Context(ctx.statics);
 	        try
 	        {
 	            // cx.setEmitter(ctx.getEmitter());
 	            // cx.statics.nodeFactory = ctx.statics.nodeFactory;
 	            // cx.statics.global = ctx.statics.global;
             Parser p = null;
 		        if (in != null)
 		        {
                 p = new Parser(cx, in, fixed_filespec, encoding, create_doc_info, save_comment_nodes,block_kind_stack, true);
                 p.config_namespaces = this.config_namespaces;
                 second = p.parseProgram();
 		        }
 		        else
 		        {
                 p = new Parser(cx, text, fixed_filespec, create_doc_info, save_comment_nodes,block_kind_stack, true);
                 p.config_namespaces = this.config_namespaces;
 	            second = p.parseProgram();
 		        }
 	        }
 	        finally
 	        {
 	            ctx.setPath(oldCtxPathSpec);
 	            // now we can remove the filename...
 	            ctx.statics.includePaths.removeLast();
 	            if (in != null)
 		        {
 			        try { in.close(); } catch (IOException ex) {}
 		        }
 	        }
 	        result = nodeFactory.includeDirective(cx, first, second);
 	    } else
 	    	result = nodeFactory.includeDirective(ctx, first, second);
 
         if (debug)
         {
             System.err.println("finish parseIncludeDirective");
         }
 
         return result;
     }
 
     /*
      * Attributes
      *     Attribute
      *     Attribute [no line break] Attributes
      */
 
     private boolean checkAttribute( int block_kind, Node node )
     {
         if( block_kind_stack.last() != ERROR_TOKEN  && block_kind_stack.last() != CLASS_TOKEN  && block_kind_stack.last() != INTERFACE_TOKEN)
         {
             if( node.hasAttribute("private") )
             {
                 ctx.error(node.pos()-1, kError_InvalidPrivate);
             }
             else
             if( node.hasAttribute("protected") )
             {
                 ctx.error(node.pos()-1, kError_InvalidProtected);
             }
             else
             if( node.hasAttribute("static") )
             {
                 ctx.error(node.pos()-1, kError_InvalidStatic);
             }
             else
             if( block_kind_stack.last() != PACKAGE_TOKEN && block_kind_stack.last() != EMPTY_TOKEN && node.hasAttribute("internal") )
             {
                 ctx.error(node.pos()-1, kError_InvalidInternal);
             }
             else
             if( block_kind_stack.last() != PACKAGE_TOKEN && node.hasAttribute("public") )
             {
                 ctx.error(node.pos()-1, kError_InvalidPublic);
             }
         }
 
         if( node.hasAttribute("prototype") )
         {
             ctx.error(node.pos()-1,kError_PrototypeIsAnInvalidAttribute);
         }
 
         return node.hasAttribute("static") ||
                 node.hasAttribute("public") ||
                 node.hasAttribute("private") ||
                 node.hasAttribute("protected") ||
                 node.hasAttribute("internal") ||
                 node.hasAttribute("native") ||
                 node.hasAttribute("final") ||
                 node.hasAttribute("override") ||
                 node.hasAttribute("prototype");
     }
 
     public AttributeListNode parseAttributes()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttributes");
         }
 
         AttributeListNode result;
 
         Node first = parseAttribute();
 
         checkAttribute(block_kind_stack.last(),first);
 
         AttributeListNode second = null;
 
         if (lookahead(IDENTIFIER_TOKEN) ||
             lookahead(PRIVATE_TOKEN) ||
             lookahead(PROTECTED_TOKEN) ||
             lookahead(PUBLIC_TOKEN) ||
             lookahead(FALSE_TOKEN) ||
             lookahead(TRUE_TOKEN))
         {
             second = parseAttributes();
         }
 
         result = nodeFactory.attributeList(first, second);
 
         if (debug)
         {
             System.err.println("finish parseAttributes");
         }
 
         return result;
     }
 
     /*
      * Attribute
      *     AttributeExpression
      *     true
      *     false
      *     private
      *     public
      */
 
     public Node parseAttribute()
     {
 
         if (debug)
         {
             System.err.println("begin parseAttribute");
         }
 
         Node result;
 
         if (lookahead(TRUE_TOKEN))
         {
             match(TRUE_TOKEN);
             result = nodeFactory.literalBoolean(true, ctx.input.positionOfMark());
         }
         else if (lookahead(FALSE_TOKEN))
         {
             match(FALSE_TOKEN);
             result = nodeFactory.literalBoolean(false, ctx.input.positionOfMark());
         }
         else if (lookahead(PRIVATE_TOKEN))
         {
             match(PRIVATE_TOKEN);
             result = nodeFactory.identifier("private", ctx.input.positionOfMark());
             if (lookahead(DOUBLECOLON_TOKEN))
             {
                 match(DOUBLECOLON_TOKEN);
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifier());
             }
         }
         else if (lookahead(PROTECTED_TOKEN))
         {
             match(PROTECTED_TOKEN);
             result = nodeFactory.identifier("protected", ctx.input.positionOfMark());
             if (lookahead(DOUBLECOLON_TOKEN))
             {
                 match(DOUBLECOLON_TOKEN);
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifier());
             }
         }
         else if (lookahead(PUBLIC_TOKEN))
         {
             match(PUBLIC_TOKEN);
             result = nodeFactory.identifier("public", ctx.input.positionOfMark());
             if (lookahead(DOUBLECOLON_TOKEN))
             {
                 match(DOUBLECOLON_TOKEN);
                 result = nodeFactory.qualifiedIdentifier(result, parseIdentifier());
             }
         }
         else if(lookahead(LEFTBRACKET_TOKEN))
         {
             result = parseArrayLiteral();
         }
         else
         {
             result = parseSimpleTypeIdentifier();
         }
 
         if (debug)
         {
             System.err.println("finish parseAttribute");
         }
 
         return result;
     }
 
     /*
      * ImportDirective
      *     import PackageName
      *     import Identifier = PackageName
      */
 
     public Node parseImportDirective(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseImportDirective");
         }
 
         Node result;
         PackageNameNode second;
         PackageNameNode third = null;
 
         int stmtPos = scanner.input.positionOfMark();
         match(IMPORT_TOKEN);
         second = parsePackageName(true);
         if (lookahead(ASSIGN_TOKEN))
         {
             // ISSUE: second should be a simple identifier
             third = parsePackageName(true);
         }
 
         result = nodeFactory.importDirective(first, second, third, stmtPos, ctx);
 
         if (debug)
         {
             System.err.println("finish parseImportDirective");
         }
 
         return result;
     }
 
     // Definitions
 
 
     /*
      * VariableDefinition
      *     VariableDefinitionKind VariableBindingList[allowIn]
      */
 
     public Node parseVariableDefinition(AttributeListNode first, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableDefinition");
         }
 
         int second;
         ListNode third;
         Node result;
 
         // The following logic goes something like this: If it is a
         // CONST_TOKEN, then first is a CONST_TOKEN. If it is a VAR_TOKEN
         // then first is VAR_TOKEN. If it is anything else then first is
         // the default (VAR_TOKEN).
 
         second = lookahead(CONST_TOKEN) ? match(CONST_TOKEN) :
             lookahead(VAR_TOKEN) ? match(VAR_TOKEN) : VAR_TOKEN;
         third = parseVariableBindingList(first, second, mode);
         result = nodeFactory.variableDefinition(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseVariableDefinition");
         }
 
         return result;
     }
 
     /*
      * VariableBindingList
      *     VariableBinding VariableBindingListPrime
      *
      * VariableBindingListPrime
      *     , VariableBinding VariableBindingListPrime
      *     empty
      */
 
     public ListNode parseVariableBindingList(AttributeListNode attrs, int kind, int mode)
     {
         if (debug)
         {
             System.err.println("begin parseVariableBindingList");
         }
 
         ListNode result;
         Node first;
 
         first = parseVariableBinding(attrs, kind, mode);
         result = parseVariableBindingListPrime(attrs, kind, mode, nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseVariableBindingList");
         }
 
         return result;
     }
 
     public ListNode parseVariableBindingListPrime(AttributeListNode attrs, int kind, int mode, ListNode first)
     {
         if (debug)
         {
             System.err.println("begin parseVariableBindingListPrime");
         }
 
         ListNode result;
 
         if (lookahead(COMMA_TOKEN))
         {
             match(COMMA_TOKEN);
             Node second;
             second = parseVariableBinding(attrs, kind, mode);
             result = parseVariableBindingListPrime(attrs, kind, mode, nodeFactory.list(first, second));
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseVariableBindingListPrime'");
         }
 
         return result;
     }
 
     /*
      * VariableBindingb
      *     TypedIdentifierb VariableInitialisationb
      */
 
     public Node parseVariableBinding(AttributeListNode attrs, int kind, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableBinding");
         }
 
         Node result;
         TypedIdentifierNode first;
         Node second;
 
         first = parseTypedIdentifier(mode);
         second = parseVariableInitialization(mode);
         result = nodeFactory.variableBinding(attrs, kind, first, second);
 
         if (debug)
         {
             System.err.println("finish parseVariableBinding");
         }
 
         return result;
     }
 
     /*
      * VariableInitialisationb
      *     empty
      *     = VariableInitialiserb
      *
      * VariableInitialiserb
      *     AssignmentExpressionb
      *     AttributeCombination
      */
 
     public Node parseVariableInitialization(int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseVariableInitialization");
         }
 
         Node result;
         Node first;
 
         if (lookahead(ASSIGN_TOKEN))
         {
             match(ASSIGN_TOKEN);
             first = parseAssignmentExpression(mode);
 
             if (lookahead(COMMA_TOKEN) || lookaheadSemicolon(mode))
             {
                 result = first;
             }
             else if (ctx.scriptAssistParsing &&( lookahead(COMMA_TOKEN) || lookaheadSemicolon(mode) || lookahead(IN_TOKEN)))
             {
                 result = first;
             }
             else
             {
                 result = null;
             }
         }
         else
         {
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseVariableInitialization");
         }
 
         return result;
     }
 
     /*
      * TypedIdentifier
      *         Identifier
      *         Identifier : TypeExpression
      */
 
     public TypedIdentifierNode parseTypedIdentifier(int mode)
     {
         if (debug)
         {
             System.err.println("begin parseTypedIdentifier");
         }
 
         TypedIdentifierNode result;
         Node first;
         Node second;
         boolean no_anno;
 
         first = parseIdentifier();
 
         if (lookahead(COLON_TOKEN))
         {
             match(COLON_TOKEN);
             if( lookahead(MULT_TOKEN) )
             {
                 match(MULT_TOKEN);
                 if (ctx.scriptAssistParsing){
                 	second = nodeFactory.identifier("*");
                 }
                 else
                 	second = null;
             }
             else
             if( lookahead(MULTASSIGN_TOKEN) )
             {
                 nexttoken = ASSIGN_TOKEN;  // morph into ordinary looking intializer
                 second = null;
             }
             else
             {
                 // if it's a keyword, that's invalid, throws an error
                 if( errorIfNextTokenIsKeywordInsteadOfTypeExpression() )
                 {
                     second = null; // skip
                 }
                 else
                 {
                     second = parseTypeExpression(mode);
                 }
             }
             no_anno = false;
         }
         else
         {
             second = null;
             no_anno = true;
         }
 
         result = nodeFactory.typedIdentifier(first, second);
         result.no_anno = no_anno;
 
         if (debug)
         {
             System.err.println("finish parseTypedIdentifier");
         }
 
         return result;
     }
 
     /*
      * SimpleVariableDefinition
      *     var UntypedVariableBindingList
      */
 
     public Node parseSimpleVariableDefinition()
     {
 
         if (debug)
         {
             System.err.println("begin parseSimpleVariableDefinition");
         }
 
         Node result;
 
         match(VAR_TOKEN);
         result = parseUntypedVariableBindingList();
 
         if (debug)
         {
             System.err.println("finish parseSimpleVariableDefinition");
         }
 
         return result;
     }
 
     /*
      * UntypedVariableBindingList
      *     UntypedVariableBinding UntypedVariableBindingListPrime
      *
      * UntypedVariableBindingListPrime
      *     , UntypedVariableBinding UntypedVariableBindingListPrime
      *     empty
      */
 
     public ListNode parseUntypedVariableBindingList()
     {
         if (debug)
         {
             System.err.println("begin parseUntypedVariableBindingList");
         }
 
         ListNode result;
         ListNode first;
 
         first = nodeFactory.list(null, parseUntypedVariableBinding());
 
         while (lookahead(COMMA_TOKEN))
         {
             Node second;
             match(COMMA_TOKEN);
             second = parseUntypedVariableBinding();
             first = nodeFactory.list(first, second);
         }
 
         result = first;
 
         if (debug)
         {
             System.err.println("finish parseUntypedVariableBindingList");
         }
 
         return result;
     }
 
     /*
      * UntypedVariableBinding
      *     Identifier VariableInitialisationallowIn
      */
 
     public Node parseUntypedVariableBinding()
     {
 
         if (debug)
         {
             System.err.println("begin parseUntypedVariableBinding");
         }
 
         Node result;
         IdentifierNode first;
         Node second;
 
         first = parseIdentifier();
         second = parseVariableInitialization(allowIn_mode);
         result = nodeFactory.variableBinding(null,VAR_TOKEN,nodeFactory.typedIdentifier(first,null), second);
 
         if (debug)
         {
             System.err.println("finish parseUntypedVariableBinding");
         }
 
         return result;
     }
 
     /*
      * FunctionDefinition
      *     function FunctionName FunctionCommon
      */
 
     public Node parseFunctionDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionDefinition");
         }
 
         Node result;
         FunctionNameNode second;
         FunctionCommonNode third;
 
         match(FUNCTION_TOKEN);
         second = parseFunctionName();
         third = parseFunctionCommon(second.identifier);
         result = nodeFactory.functionDefinition(ctx, first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseFunctionDefinition");
         }
 
         return result;
     }
 
     /*
      * FunctionName
      *     Identifier
      *     get [no line break] Identifier
      *     set [no line break] Identifier
      */
 
     public FunctionNameNode parseFunctionName()
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionName");
         }
 
         FunctionNameNode result;
 
         if (lookahead(GET_TOKEN))
         {
             match(GET_TOKEN);
             if (lookahead(LEFTPAREN_TOKEN))  // function get(...) {...}
             {
                 result = nodeFactory.functionName(EMPTY_TOKEN, nodeFactory.identifier("get",ctx.input.positionOfMark()));
             }
             else
             {
                 result = nodeFactory.functionName(GET_TOKEN, parseIdentifier());
             }
         }
         else if (lookahead(SET_TOKEN))
         {
             match(SET_TOKEN);
             if (lookahead(LEFTPAREN_TOKEN))  // function set(...) {...}
             {
                 result = nodeFactory.functionName(EMPTY_TOKEN, nodeFactory.identifier("set",ctx.input.positionOfMark()));
             }
             else
             {
                 result = nodeFactory.functionName(SET_TOKEN, parseIdentifier());
             }
         }
         else
         {
             result = nodeFactory.functionName(EMPTY_TOKEN, parseIdentifier());
         }
 
         if (debug)
         {
             System.err.println("finish parseFunctionName");
         }
         return result;
     }
 
     /*
      * FunctionSignature
      *     ParameterSignature ResultSignature
      *
      * ParameterSignature
      *     ( Parameters )
      */
 
     public FunctionSignatureNode parseFunctionSignature()
     {
 
         if (debug)
         {
             System.err.println("begin parseFunctionSignature");
         }
 
         FunctionSignatureNode result;
         ParameterListNode first;
         Node second;
         boolean no_anno[] = new boolean[1];
         boolean void_anno[] = new boolean[1];
 
         // inlined: parseParameterSignature
 
         match(LEFTPAREN_TOKEN);
         first = parseParameters();
         match(RIGHTPAREN_TOKEN);
         second = parseResultSignature(no_anno,void_anno);
 
         result = nodeFactory.functionSignature(first, second,ctx.input.positionOfMark());
         result.no_anno = no_anno[0];
         result.void_anno = void_anno[0];
 
         if (debug)
         {
             System.err.println("finish parseFunctionSignature");
         }
 
         return result;
     }
 
     /**
      * FunctionSignature ParameterSignature ResultSignature
      * 
      * ParameterSignature ( Parameters )
      */
 
     public FunctionSignatureNode parseConstructorSignature() {
 
         if (debug) {
             System.err.println("begin parseConstructorSignature");
         }
 
         FunctionSignatureNode result;
         ParameterListNode first;
         ListNode second;
         boolean no_anno[] = new boolean[1];
         boolean void_anno[] = new boolean[1];
 
         // inlined: parseParameterSignature
 
         match(LEFTPAREN_TOKEN);
         first = parseParameters();
         match(RIGHTPAREN_TOKEN);
         second = parseConstructorInitializer(no_anno, void_anno);
 
         if( void_anno[0] )
         {
             result = nodeFactory.functionSignature(first, null, ctx.input.positionOfMark());
             result.no_anno = false;
             result.void_anno = true;
         }
         else
         {
             result = nodeFactory.constructorSignature(first, second, ctx.input
                 .positionOfMark());
             result.no_anno = true;
             result.void_anno = false;
         }
         
         if (debug) {
             System.err.println("finish parseConstructorSignature");
         }
 
         return result;
     }
 
     public ListNode parseConstructorInitializer(boolean no_anno[], boolean void_anno[]) {
         if (debug) {
             System.err.println("begin parseConstructorInitializer");
         }
 
         ListNode result = null;
 
         no_anno[0] = true;
         void_anno[0] = false;
         
         if (lookahead(COLON_TOKEN)) {
             match(COLON_TOKEN);
 
             if( lookahead(VOID_TOKEN) )
             {
                 // allow :void, since we allowed that in Flex2.0/AS3
                 match(VOID_TOKEN);
                 no_anno[0] = false;
                 void_anno[0] = true;
                 return null;
             }
             result = parseInitializerList();
             if( lookahead(SUPER_TOKEN) ) {
                 result = nodeFactory.list(result, parseSuperInitializer() );
             }
         } else {
             result = null;
         }
 
         if (debug) {
             System.err.println("finish parseConstructorInitializer");
         }
 
         return result;
     }
 
     public Node parseSuperInitializer()
     {
         if (debug) {
             System.err.println("begin parseSuperInitializer");
         }
         
         match(SUPER_TOKEN);
         Node result = null;
         Node first = nodeFactory.superExpression(null, ctx.input.positionOfMark());
 
         Node n = parseArguments(first);
         CallExpressionNode call = (n instanceof CallExpressionNode) ? (CallExpressionNode) n
                 : null; // this returns a call expression node
         if (call == null) {
             ctx.internalError("Internal error in parseSuperInitializer()");
         }
         result = nodeFactory.superStatement(call);
 
         if (debug) {
             System.err.println("end parseSuperInitializer");
         }
                 
         return result;
     }
     
     public ListNode parseInitializerList() {
         if (debug) {
             System.err.println("begin parseInitiliazerList");
         }
 
         ListNode result = null;
 
         Node first;
         if( lookahead(SUPER_TOKEN) )
         {
             result = null;
         }
         else
         {
             first = parseInitializer();
             result = parseInitializerListPrime(nodeFactory.list(null, first));
         }
         
         if (debug) {
             System.err.println("end parseInitializerList");
         }
 
         return result;
     }
 
     public ListNode parseInitializerListPrime(ListNode first) {
         if (debug) {
             System.err.println("begin parseInitializerListPrime");
         }
 
         ListNode result;
 
         if (lookahead(COMMA_TOKEN)) {
             match(COMMA_TOKEN);
 
             if( lookahead(SUPER_TOKEN) )
             {
                 result = first;
             }
             else
             {
                 Node second;
     
                 second = parseInitializer();
                 result = parseInitializerListPrime(nodeFactory.list(first, second));
             }
         } else {
             result = first;
         }
 
         if (debug) {
             System.err.println("end parseInitializerListPrime");
         }
 
         return result;
     }
 
     public Node parseInitializer()
     {
         if (debug)
         {
             System.err.println("begin parseInitializer");
         }
 
         Node result = null;
 
         // Todo: this should be parsePattern();
         Node first = parseIdentifier();
         Node second = null;
         if (lookahead(ASSIGN_TOKEN) )
         {
             second = parseVariableInitialization(allowIn_mode);
         }
         else
         {
             // error
             ctx.error(first.pos()-1, kError_Parser_DefinitionOrDirectiveExpected);
         }
         
         result = nodeFactory.assignmentExpression(first, ASSIGN_TOKEN, second);
         
         if (debug)
         {
             System.err.println("end parseInitializer");
         }
 
         return result;
     }
 
     /*
      * Parameters
      *         empty
      *         NonemptyParameters
      */
 
     public ParameterListNode parseParameters()
     {
 
         if (debug)
         {
             System.err.println("begin parseParameters");
         }
 
         ParameterListNode result;
 
         if (lookahead(RIGHTPAREN_TOKEN))
         {
             result = null;
         }
         else
         {
             result = parseNonemptyParameters(null);
         }
 
         if (debug)
         {
             System.err.println("finish parseParameters");
         }
 
         return result;
     }
 
     /*
      * NonemptyParameters
      *     ParameterInit
      *     ParameterInit , NonemptyParameters
      *     RestParameters
      */
 
     public ParameterListNode parseNonemptyParameters(ParameterListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseNonemptyParameters");
         }
 
         ParameterListNode result;
         ParameterNode second;
 
         if (lookahead(TRIPLEDOT_TOKEN))
         {
             result = nodeFactory.parameterList(first, parseRestParameter());
         }
         else
         {
             second = parseParameter();
             if (lookahead(COMMA_TOKEN))
             {
                 match(COMMA_TOKEN);
                 result = parseNonemptyParameters(nodeFactory.parameterList(first, second));
             }
             else if (lookahead(ASSIGN_TOKEN))
             {
                 match(ASSIGN_TOKEN);
                 second.init = parseNonAssignmentExpression(allowIn_mode);
                 if (lookahead(COMMA_TOKEN))
                 {
                     match(COMMA_TOKEN);
                     result = parseNonemptyParameters(nodeFactory.parameterList(first, second));
                 }
                 else
                 {
                     result = nodeFactory.parameterList(first, second);
                 }
             }
             else
             {
                 result = nodeFactory.parameterList(first, second);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseNonemptyParameters");
         }
 
         return result;
     }
 
     /*
      * RestParameter
      *     ...
      *     ... ParameterAttributes Identifier
      */
 
     public ParameterNode parseRestParameter()
     {
 
         if (debug)
         {
             System.err.println("begin parseRestParameter");
         }
 
         ParameterNode result;
         ParameterNode first;
 
         match(TRIPLEDOT_TOKEN);
 
         if (lookahead(CONST_TOKEN) ||
             lookahead(IDENTIFIER_TOKEN) ||
             lookahead(GET_TOKEN) ||
             lookahead(SET_TOKEN))
         {
             first = parseParameter();
         }
         else
         {
             first = null;
         }
 
         result = nodeFactory.restParameter(first,ctx.input.positionOfMark());
 
         if (debug)
         {
             System.err.println("finish parseRestParameter");
         }
 
         return result;
     }
 
     /*
      * Parameter
      *     Identifier
      *     Identifier : TypeExpression[allowIn]
      */
 
     public ParameterNode parseParameter()
     {
 
         if (debug)
         {
             System.err.println("begin parseParameter");
         }
 
         ParameterNode result;
         int first;
         IdentifierNode second;
         Node third;
         boolean no_anno = false;
 
         if (HAS_CONSTPARAMETERS && lookahead(CONST_TOKEN))
         {
             first = match(CONST_TOKEN);
         }
         else
         {
             first = VAR_TOKEN;
         }
 
         second = parseIdentifier();
         if (lookahead(COLON_TOKEN))
         {
             match(COLON_TOKEN);
             if( lookahead(MULT_TOKEN) )
             {
                 match(MULT_TOKEN);
                 second.setOrigTypeToken(MULT_TOKEN);
                 third = null;
             }
             else
             if( lookahead(MULTASSIGN_TOKEN) )
             {
                 nexttoken = ASSIGN_TOKEN;  // morph into ordinary looking intializer
                 second.setOrigTypeToken(MULTASSIGN_TOKEN);
                 third = null;
             }
             else
             {
                 // if it's a keyword, that's invalid, throws an error
                 if( errorIfNextTokenIsKeywordInsteadOfTypeExpression() )
                 {
                     third = null; // skip
                 }
                 else
                 {
                     third = parseTypeExpression(allowIn_mode);
                 }
             }
         }
         else
         {
             third = null;
             no_anno = true;
         }
 
         result = nodeFactory.parameter(first, second, third);
         result.no_anno = no_anno;
 
         if (debug)
         {
             System.err.println("finish parseParameter");
         }
 
         return result;
     }
 
     /*
      * ResultSignature
      *     empty
      *     : TypeExpression[allowIn]
      */
 
     public Node parseResultSignature(boolean no_anno[],boolean void_anno[])
     {
 
         if (debug)
         {
             System.err.println("begin parseResultSignature");
         }
 
         Node result;
 
         no_anno[0] = false;
         void_anno[0] = false;
 
         if (lookahead(COLON_TOKEN))
         {
             match(COLON_TOKEN);
             if( lookahead(MULT_TOKEN) )
             {
                 match(MULT_TOKEN);
                 if (ctx.scriptAssistParsing)
                 	result = nodeFactory.identifier("*",ctx.input.positionOfMark());
                 else
                 	result = null;
             }
             else
                 if( lookahead(MULTASSIGN_TOKEN) ) // do this here for better (potential) syntax error reporting
             {
                 nexttoken=ASSIGN_TOKEN; // morph into an assign token
                 result = null;  // same as no annotation
             }
             else
             if( lookahead(VOID_TOKEN) )
             {
                 match(VOID_TOKEN);
                 result = null;
                 void_anno[0] = true;
             }
             else
             {
 
                 // if it's a keyword, that's invalid, throws an error
                 errorIfNextTokenIsKeywordInsteadOfTypeExpression();
                 result = parseTypeExpression(allowIn_mode);
             }
         }
         else
         {
             result = null;
             no_anno[0] = true;
         }
 
         if (debug)
         {
             System.err.println("finish parseResultSignature");
         }
 
         return result;
     }
 
     /**
      * This looks to see if the next token is a reserved keyword, if so then an error is thrown
      * for a keyword being used where we expected a type expression/identifier
      */
     private boolean errorIfNextTokenIsKeywordInsteadOfTypeExpression() {
         if ( lookahead(VOID_TOKEN) )
         {
             error(syntax_error, kError_Parser_keywordInsteadOfTypeExpr, scanner.getTokenText(nexttoken));
             match(VOID_TOKEN);
             return true;
         }
         else
         if (lookahead(xmlid_tokens, xmlid_tokens_count) && thisToken != IDENTIFIER_TOKEN)
         {
             error(syntax_error, kError_Parser_keywordInsteadOfTypeExpr, scanner.getTokenText(nexttoken));
             match(xmlid_tokens, xmlid_tokens_count);
             return true;
         }
         else
         {
             if( ctx.dialect(8) && thisToken == IDENTIFIER_TOKEN && scanner.getTokenText(nexttoken).equals("Object") )
             {
                 error(syntax_error, kError_ColonObjectAnnoOutOfService);
                 return true;
             }
             return false;
         }
     }
 
     /*
      * ClassDefinition
      *     class Identifier Inheritance Block
      */
 
     public Node parseClassDefinition(AttributeListNode attrs, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseClassDefinition");
         }
 
         Node result;
         ClassNameNode first;
 
         match(CLASS_TOKEN);
 
         if( block_kind_stack.last() != PACKAGE_TOKEN && block_kind_stack.last() != EMPTY_TOKEN )
         {
             error(syntax_error,kError_InvalidClassNesting);
         }
 
         block_kind_stack.add(CLASS_TOKEN);
 
         first = parseClassName();
 
         String temp_class_name = current_class_name;
         current_class_name = first.ident.name;
 
         {
             InheritanceNode second;
             StatementListNode third;
 
             if( first.pkgname != null )
             {
                 nodeFactory.startPackage(ctx,null,first.pkgname);
             }
 
             nodeFactory.StartClassDefs();
             second = parseInheritance();
             third  = parseBlock();
             result = nodeFactory.classDefinition(ctx, attrs, first.ident, second, third, first.non_nullable);
             block_kind_stack.removeLast();
 
             current_class_name = temp_class_name;
             
             if( first.pkgname != null )
             {
                 nodeFactory.finishPackage(ctx,null);
             }
         }
 
         if (debug)
         {
             System.err.println("finish parseClassDefinition");
         }
 
         return result;
     }
 
     /*
      * ClassName
      *     Identifier
      *     PackageName . Identifier
      */
 
     public ClassNameNode parseClassName()
     {
 
         if (debug)
         {
             System.err.println("begin parseClassName");
         }
 
         ClassNameNode result;
         IdentifierNode first;
         first = parseIdentifier();
         if (HAS_COMPOUNDCLASSNAMES)
         {
             result = parseClassNamePrime(first);
         }
         else
         {
             result = nodeFactory.className(null, first);
         }
 
         if( ctx.statics.es4_nullability )
         {
             if( lookahead(NOT_TOKEN))
             {
                 match(NOT_TOKEN);
                 result.non_nullable = true;
             }
             else if ( lookahead(QUESTIONMARK_TOKEN))
             {
                 match(QUESTIONMARK_TOKEN);
                 // do nothing, classes are by default nullable.
             }
         }
         
         if (debug)
         {
             System.err.println("finish parseClassName");
         }
 
         return result;
     }
 
     public ClassNameNode parseClassNamePrime(IdentifierNode second)
     {
 
         if (debug)
         {
             System.err.println("begin parseClassNamePrime");
         }
 
         ClassNameNode result;
         PackageIdentifiersNode first = null;
 
         while (lookahead(DOT_TOKEN))
         {
             match(DOT_TOKEN);
             first = nodeFactory.packageIdentifiers(first, second, true);
             second = parseIdentifier();
         }
 
         PackageNameNode pkgname = first!=null?nodeFactory.packageName(first):null;
         result = nodeFactory.className(pkgname,second);
 
         if (debug)
         {
             System.err.println("finish parseClassNamePrime");
         }
 
         return result;
     }
 
     /*
      * Inheritance
      *     empty
      *     extends TypeExpressionallowIn
      *     implements TypeExpressionList
      *     extends TypeExpressionallowIn implements TypeExpressionList
      */
 
     public InheritanceNode parseInheritance()
     {
 
         if (debug)
         {
             System.err.println("begin parseInheritance");
         }
 
         InheritanceNode result = null;
         Node first;
         ListNode second;
 
         if (lookahead(EXTENDS_TOKEN))
         {
             match(EXTENDS_TOKEN);
             first = parseTypeName();
         }
         else
         {
             first = null;
         }
 
         if (lookahead(IMPLEMENTS_TOKEN))
         {
             match(IMPLEMENTS_TOKEN);
             second = parseTypeNameList();
         }
         else
         {
             second = null;
         }
 
         if (!(first == null && second == null))
         {
             result = nodeFactory.inheritance(first, second);
         }
 
         if (debug)
         {
             System.err.println("finish parseInheritance");
         }
 
         return result;
     }
 
     /*
      * TypeExpressionList
      *     TypeExpression[allowin] TypeExpressionListPrime
      *
      * TypeExpressionListPrime:
      *     , TypeExpression[allowin] TypeExpressionListPrime
      *     empty
      */
 
     public ListNode parseTypeExpressionList()
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpressionList");
         }
 
         ListNode result;
         Node first;
 
         first = parseTypeExpression(allowIn_mode);
         result = parseTypeExpressionListPrime(nodeFactory.list(null, first));
 
         if (debug)
         {
             System.err.println("finish parseTypeExpressionList");
         }
 
         return result;
     }
 
     public ListNode parseTypeExpressionListPrime(ListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseTypeExpressionListPrime");
         }
 
         ListNode result;
 
         if (lookahead(COMMA_TOKEN))
         {
             match(COMMA_TOKEN);
 
             Node second;
 
             second = parseTypeExpression(allowIn_mode);
             result = parseTypeExpressionListPrime(nodeFactory.list(first, second));
 
         }
         else
         {
             result = first;
         }
 
         if (debug)
         {
             System.err.println("finish parseTypeExpressionListPrime'");
         }
 
         return result;
     }
 
     /*
      * ExtendsList
      *     empty
      *     extends TypeExpressionList
      */
 
     public ListNode parseExtendsList()
     {
 
         if (debug)
         {
             System.err.println("begin parseExtendsList");
         }
 
         ListNode result;
 
         if (lookahead(EXTENDS_TOKEN))
         {
             match(EXTENDS_TOKEN);
             result = parseTypeNameList();
         }
         else
         {
             result = null;
         }
 
         if (debug)
         {
             System.err.println("finish parseExtendsList");
         }
 
         return result;
     }
 
     /*
      * InterfaceDefinition
      *     interface ClassName ExtendsList Block
 
      */
 
     public Node parseInterfaceDefinition(AttributeListNode attrs, int mode)
     {
 
         if (debug)
         {
             System.err.println("begin parseInterfaceDefinition");
         }
 
         Node result;
         ClassNameNode first;
 
         match(INTERFACE_TOKEN);
 
         block_kind_stack.add(INTERFACE_TOKEN);
 
         first = parseClassName();
 
         {
             ListNode second;
             StatementListNode third;
 
             if( first.pkgname != null )
             {
                 nodeFactory.startPackage(ctx,null,first.pkgname);
             }
 
             second = parseExtendsList();
             third  = parseBlock();
             result = nodeFactory.interfaceDefinition(ctx, attrs, first.ident, second, third);
 
             block_kind_stack.removeLast();
 
             if( first.pkgname != null )
             {
                 nodeFactory.finishPackage(ctx,null);
             }
 
         }
 
         if (debug)
         {
             System.err.println("finish parseInterfaceDefinition");
         }
 
         return result;
     }
 
     /*
      * NamespaceDefinition
      *     namespace Identifier
      */
 
     public NamespaceDefinitionNode parseNamespaceDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseNamespaceDefinition");
         }
 
         if( first != null && first.items.size() == 1 && first.hasAttribute("config"))
         {
         	first = null;
         	return parseConfigNamespaceDefinition(first);
         }
 
         NamespaceDefinitionNode result;
         IdentifierNode second = null;
         Node third = null;
 
         match(NAMESPACE_TOKEN);
 //        if (!lookahead(ASSIGN_TOKEN))
         {
             second = parseIdentifier();
         }
 /*        else
         {
             // ISSUE: error if not the default namespace
         }
 */
         if (lookahead(ASSIGN_TOKEN))
         {
             match(ASSIGN_TOKEN);
             if( ctx.statics.es4_nullability )
             {
                 if( lookahead(STRINGLITERAL_TOKEN) )
                 {
                     boolean[] is_single_quoted = new boolean[1];
                     String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
                     third = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0] );
                 }
                 else
                 {
                     third = parseSimpleTypeIdentifier();
                 }
             }
             else
             {
                 third = parseAssignmentExpression(allowIn_mode);
             }
         }
         
         result = nodeFactory.namespaceDefinition(first, second, third);
 
         if (debug)
         {
             System.err.println("finish parseNamespaceDefinition");
         }
 
         return result;
     }
 
     /*
      * NamespaceDefinition
      *     namespace Identifier
      */
 
     public NamespaceDefinitionNode parseConfigNamespaceDefinition(AttributeListNode first)
     {
 
         if (debug)
         {
             System.err.println("begin parseConfigNamespaceDefinition");
         }
 
         NamespaceDefinitionNode result;
 
         match(NAMESPACE_TOKEN);
         IdentifierNode second = parseIdentifier();
         
         result = nodeFactory.configNamespaceDefinition(first, second, -1);
 
         config_namespaces.last().add(result.name.name);
         
         if (debug)
         {
             System.err.println("finish parseConfigNamespaceDefinition");
         }
 
         return result;
     }
     
     /*
      * PackageDefinition
      *     package Block
      *     package PackageName Block
      */
 
     public PackageDefinitionNode parsePackageDefinition()
     {
 
         if (debug)
         {
             System.err.println("begin parsePackageDefinition");
         }
 
         if (within_package)
             error(kError_NestedPackage);
 
         within_package = true;
 
         PackageDefinitionNode result;
 
         // Init the default xml namespace
 
         nodeFactory.dxns = null;
 
         block_kind_stack.add(PACKAGE_TOKEN);
 
         match(PACKAGE_TOKEN);
 
         HashSet<String> conf_ns = new HashSet<String>(config_namespaces.last().size());
         conf_ns.addAll(config_namespaces.last());
         config_namespaces.push_back(conf_ns);
         
         if (lookahead(LEFTBRACE_TOKEN))
         {
             result = nodeFactory.startPackage(ctx, null, null);
             Node udn = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("AS3"))));
             Node idn = null;
             if( ctx.statics.es4_vectors )
             {
                 PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier("__AS3__"), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("vec"), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("Vector"), true);
                 idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             }
             result = nodeFactory.finishPackage(ctx, parseBlock());
             if ((ctx.dialect(10) /*|| ctx.dialect(11)*/) && result != null)
         	{
         		result.statements.items.add(1,udn);  // insert after the first statment, which is the starting package definition
         	}
             if ( !ctx.statics.use_namespaces.isEmpty() && result != null)
             {
                 for (String useName : ctx.statics.use_namespaces )
                 {
                    Node udn2 = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("flash10"))));
                     result.statements.items.add(1,udn2);
                 }
             }
             if( ctx.statics.es4_vectors && result != null)
             {
                 result.statements.items.add(1, idn);
             }
 
         }
         else
         {
             PackageNameNode first = parsePackageName(false);
             result = nodeFactory.startPackage(ctx, null, first);
             Node udn = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("AS3"))));
             Node idn = null;
             if( ctx.statics.es4_vectors )
             {
                 PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier("__AS3__"), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("vec"), true);
                 pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("Vector"), true);
                 idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             }
             result = nodeFactory.finishPackage(ctx, parseBlock());
             if ((ctx.dialect(10) /*|| ctx.dialect(11)*/) && result != null)
             {
         		result.statements.items.add(1,udn);  // insert after the first statment, which is the starting package definition
             }
             if ( !ctx.statics.use_namespaces.isEmpty() && result != null)
             {
                 for (String useName : ctx.statics.use_namespaces )
                 {
                    Node udn2 = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("flash10"))));
                     result.statements.items.add(1,udn2);
                 }
             }
             if( ctx.statics.es4_vectors && result != null)
             {
                 result.statements.items.add(1, idn);
             }
        }
 
         block_kind_stack.removeLast();
         config_namespaces.pop_back();
         
         if (debug)
         {
             System.err.println("finish parsePackageDefinition");
         }
 
         within_package = false;
 
         return result;
     }
 
     private StatementListNode parseConfigValues()
     {
         StatementListNode configs = null;
         String config_code = ctx.getConfigVarCode();
         if( config_code != null )
         {
             Scanner orig = scanner;
             InputBuffer input = ctx.input;
             int orig_lastToken = lastToken;
             int orig_nextToken = nexttoken;
             int orig_thisToken = thisToken;
             
             scanner = new Scanner(ctx, config_code, "");
             scanner.input.report_pos = false;  // Don't give position to generated nodes, since they don't have corresponding source
 
             configs = parseDirectives(null, null);
 
             scanner = orig;
             ctx.input = input;
             lastToken = orig_lastToken;
             nexttoken = orig_nextToken;
             thisToken = orig_thisToken;
         }
         return configs;
     }
     
     /*
      * PackageName
      *     Identifier
      *     PackageName . Identifier
      */
 
     public PackageNameNode parsePackageName(boolean isDefinition)
     {
         if (debug)
         {
             System.err.println("begin parsePackageName");
         }
 
         PackageNameNode result;
 
         if (lookahead(STRINGLITERAL_TOKEN))
         {
             boolean[] is_single_quoted = new boolean[1];
             String enclosedText = scanner.getStringTokenText(match(STRINGLITERAL_TOKEN), is_single_quoted);
             LiteralStringNode first = nodeFactory.literalString(enclosedText, ctx.input.positionOfMark(), is_single_quoted[0]);
             result = nodeFactory.packageName(first);
         }
         else
         {
             PackageIdentifiersNode first;
             first = nodeFactory.packageIdentifiers(null, parseIdentifier(), isDefinition);
             result = parsePackageNamePrime(first, isDefinition);
         }
 
         if (debug)
         {
             System.err.println("finish parsePackageName");
         }
 
         return result;
     }
 
     public PackageNameNode parsePackageNamePrime(PackageIdentifiersNode first, boolean isDefinition)
     {
         if (debug)
         {
             System.err.println("begin parsePackageNamePrime");
         }
 
         while (lookahead(DOT_TOKEN))
         {
             match(DOT_TOKEN);
             first = nodeFactory.packageIdentifiers(first, parsePropertyIdentifier(), isDefinition);
         }
 
         PackageNameNode result = nodeFactory.packageName(first);
 
         if (debug)
         {
             System.err.println("finish parsePackageNamePrime");
         }
 
         return result;
     }
 
     /** <PRE>
      * Program
      * Directives
      * PackageDefinition Program
      * 
      * <B>CLEARS unused buffers before returning</B></PRE>
      */
     public ProgramNode parseProgram()
     {
         if (debug)
         {
             System.err.println("begin parseProgram");
         }
 
         StatementListNode first = null;
         StatementListNode second = null;
 
         StatementListNode configs = null;
         if( !parsing_include )
         {
             config_namespaces.push_back(new HashSet<String>());
             config_namespaces.last().add("CONFIG");
             configs = parseConfigValues();
         }
 
         // Default config namespace
         //config_namespaces.last().add("CONFIG");
         
         while (lookahead(PACKAGE_TOKEN) || lookahead(DOCCOMMENT_TOKEN))
         {
             MetaDataNode meta=null;
             if( lookahead(DOCCOMMENT_TOKEN) || lookahead(LEFTBRACKET_TOKEN) || lookahead(XMLLITERAL_TOKEN) )
             {
                 meta = parseMetaData();
                 second = nodeFactory.statementList(second,meta); 
             }
 
             if (lookahead(PACKAGE_TOKEN))
             {
                 PackageDefinitionNode pkgdef = parsePackageDefinition();
                 first = nodeFactory.statementList(first, pkgdef );
                 if (meta != null)
                 {
                     meta.def = pkgdef;
                     pkgdef.addMetaDataNode(meta);
                 }
                 
                 // Merge the package statements with the existing statements
                 if (pkgdef != null) // package is NULL if there was an erronously nested package.  Error has already been generated
                 {
                     second = nodeFactory.statementList(second,pkgdef.statements);
                 }
             }
         }
 
         second = parseDirectives(null/*attrs*/,second);
 
         if( (ctx.dialect(10) /*|| ctx.dialect(11)*/) && !parsing_include && second != null )  // don't insert a statement for includes because will screw up metadata parsing
         {
             Node udn = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("AS3"))));
             second.items.add(0,udn);
         }
         if ( !ctx.statics.use_namespaces.isEmpty() && !parsing_include && second != null)
         {
             for (String useName : ctx.statics.use_namespaces )
             {
                Node udn2 = nodeFactory.useDirective(null,nodeFactory.memberExpression(null,nodeFactory.getExpression(nodeFactory.identifier("flash10"))));
                 second.items.add(0,udn2);
             }
         }
         if( ctx.statics.es4_vectors && !parsing_include && second != null)
         {
             PackageIdentifiersNode pin = nodeFactory.packageIdentifiers(null, nodeFactory.identifier("__AS3__"), true);
             pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("vec"), true);
             pin = nodeFactory.packageIdentifiers(pin, nodeFactory.identifier("Vector"), true);
             Node idn = nodeFactory.importDirective(null, nodeFactory.packageName(pin), null, ctx);
             second.items.add(0, idn);
         }
         ProgramNode result = nodeFactory.program(ctx,second,ctx.input.positionOfMark());
         match(EOS_TOKEN);
 
         if (ctx.scriptAssistParsing){//the parser comments are needed after the parser is gone
     		for (ListIterator<Node> it = comments.listIterator(); it.hasNext(); )
     		{
     			ctx.comments.add(it.next());
     		}
         }
         
         clearUnusedBuffers();
         
 
         if( !parsing_include )
         {
             if( result != null )
             {
             	// Add tool chain added values
             	if( configs != null )
             		result.statements.items.addAll(0, configs.items);
             	NamespaceDefinitionNode configdef = nodeFactory.configNamespaceDefinition(null, nodeFactory.identifier("CONFIG"), -1);
             	// Add the default CONFIG namespace
             	result.statements.items.add(0, configdef);
             }
             config_namespaces.pop_back();
         }
         
         if (debug)
         {
             System.err.println("finish parseProgram");
         }
         
         return result;
     }
 
     private void clearUnusedBuffers() {
         comments.clear();
         scanner.clearUnusedBuffers();
         scanner = null;
         comments = null;
     }
 }
