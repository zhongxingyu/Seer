 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.parboiled;
 
 import net.colar.netbeans.fan.FanParserTask;
 import org.parboiled.BaseParser;
 import org.parboiled.Context;
 import org.parboiled.Rule;
 import org.parboiled.support.Leaf;
 import net.colar.netbeans.fan.parboiled.FantomLexerTokens.TokenName;
 
 /**
  * Parboiled parser for the Fantom Language
  *
  * Current for Fantom grammar 1.0.51
  *
  * Grammar spec:
  * http://fantom.org/doc/docLang/Grammar.html
  *
  * Test Suite: net.colar.netbeans.fan.test.FantomParserTest
  *
  * Note: ast.newNode() calls create ast nodes, using the last node matched (LAST_NODE())
  *		 ast.newScopeNode(), does the same for items that should introduce a scope.
  *
  * @author thibautc
  */
 @SuppressWarnings(
 {
 	"InfiniteRecursion"
 })
 public class FantomParser extends BaseParser<AstNode>
 {
 	//Note: Fields in PB parse can NOT be private
 	final FantomParserAstActions ast = new FantomParserAstActions();
 
 	boolean inFieldInit = false; // to help with differentiation of field accesor & itBlock
 	boolean inEnum = false; // so we know whether to allow enumDefs
 	// TODO: See if I can do away with this one and simplify  the map /simpelMap stuff
 	boolean noSimpleMap = false; // to disallow ambigous simpleMaps in certain situations (within another map, ternaryExpr)
 	/**Parse task that kicked in this parser*/
 	final FanParserTask parserTask;
 	public boolean cancel;
 
 	public FantomParser(FanParserTask parserTask)
 	{
 		this.parserTask=parserTask;
 	}
 
 	// ------------ Comp Unit --------------------------------------------------
 	public Rule compilationUnit()
 	{
 		return sequence(
 			OPT_LF(),
 			sequence(
 			// Missing from grammar: Optional unix env line
 			optional(unixLine()),
 			zeroOrMore(firstOf(using(), incUsing())),
 			zeroOrMore(typeDef()),
 			OPT_LF(),
 			zeroOrMore(doc()) // allow for extra docs at end of file (if last type commented out)
 			// Create comp. unit AST node (root node)
 			), ast.newRootNode(AstKind.AST_COMP_UNIT, parserTask),
 			OPT_LF(),
 			eoi());
 	}
 
 	public Rule unixLine()
 	{
 		return sequence("#!", zeroOrMore(sequence(testNot("\n"), any())), "\n").label(TokenName.UNIXLINE.name());
 	}
 
 	public Rule using()
 	{
 		return sequence(OPT_LF(), sequence(
 			KW_USING,
 			optional(ffi()),
 			sequence(id(),
 			zeroOrMore(sequence(DOT, id())),
 			optional(sequence(SP_COLCOL, id()))), ast.newNode(AstKind.AST_ID),
 			optional(usingAs()),
 			eos()), ast.newNode(AstKind.AST_USING), OPT_LF());
 	}
 
 	// Incomplete using - to allow for completion
 	public Rule incUsing()
 	{
 		return sequence(OPT_LF(), sequence(
 			KW_USING,
 			optional(ffi()),
 			sequence(optional(id()), // Not optional, but we want a valid ast for completion if missing
 			zeroOrMore(sequence(DOT, optional(id()))),// not enforced to allow completion
 			optional(sequence(SP_COLCOL, optional(id())))), ast.newNode(AstKind.AST_ID),// not enforced to allow completion
 			optional(sequence(sequence(KW_AS, optional(id())), ast.newNode(AstKind.AST_USING_AS))),
 			eos()), ast.newNode(AstKind.AST_INC_USING), OPT_LF());
 	}
 
 	public Rule ffi()
 	{
 		return enforcedSequence(SQ_BRACKET_L, id(), ast.newNode(AstKind.AST_USING_FFI), SQ_BRACKET_R);
 	}
 
 	public Rule usingAs()
 	{
 		return sequence(KW_AS, id(), ast.newNode(AstKind.AST_USING_AS));
 	}
 
 	public Rule staticBlock()
 	{
 		return sequence(KW_STATIC, OPT_LF(), enforcedSequence(BRACKET_L, zeroOrMore(stmt()), OPT_LF(), BRACKET_R), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF());
 	}
 
 	// ------------- Type def --------------------------------------------------
 	public Rule typeDef()
 	{
 		// grouped together classdef, enumDef, mixinDef & facetDef as they are grammatically very similar (optimized)
 		return sequence(
 			setInEnum(false),
 			OPT_LF(),
 			sequence(
 			optional(doc()),
 			zeroOrMore(facet()),
 			optional(protection()),
 			enforcedSequence(
 			firstOf(
 			// Some fantom code has protection after modifiers, so allowing that
 			sequence(sequence(zeroOrMore(sequence(firstOf(KW_ABSTRACT, KW_FINAL, KW_CONST), ast.newNode(AstKind.AST_MODIFIER))), optional(protection()), KW_CLASS), ast.newNode(AstKind.AST_CLASS)), // standard class
 			enforcedSequence(ENUM, KW_CLASS, setInEnum(true), ast.newNode(AstKind.AST_ENUM)), // enum class
 			enforcedSequence(FACET, KW_CLASS, ast.newNode(AstKind.AST_FACET)), // facet class
 			sequence(sequence(optional(sequence(KW_CONST, ast.newNode(AstKind.AST_MODIFIER))), KW_MIXIN), ast.newNode(AstKind.AST_MIXIN)) // mixin
 			),
 			id(), ast.newNode(AstKind.AST_ID), 
 			optional(inheritance()),
 			OPT_LF(),
 			sequence(
 			BRACKET_L,
 			OPT_LF(),
 			optional(sequence(test(inEnum),optional(enumValDefs()))), // only valid for enums, but simplifying
 			// Static block missing from Fan grammar
 			zeroOrMore(firstOf(staticBlock(), slotDef())),
 			BRACKET_R), ast.newNode(AstKind.AST_BLOCK))),
 			ast.newScopeNode(AstKind.AST_TYPE_DEF), OPT_LF());
 	}
 
 	public Rule protection()
 	{
 		return sequence(firstOf(KW_PUBLIC, KW_PROTECTED, KW_INTERNAL, KW_PRIVATE), ast.newNode(AstKind.AST_MODIFIER));
 	}
 
 	public Rule inheritance()
 	{
 		return enforcedSequence(SP_COL, typeList(), ast.newNode(AstKind.AST_INHERITANCE));
 	}
 
 	// ------------- Facets ----------------------------------------------------
 	public Rule facet()
 	{
 		return enforcedSequence(AT, simpleType(), optional(facetVals()), OPT_LF());
 	}
 
 	public Rule facetVals()
 	{
 		return enforcedSequence(
 			BRACKET_L,
 			facetVal(),
 			zeroOrMore(sequence(eos(), facetVal())),
 			BRACKET_R);
 	}
 
 	public Rule facetVal()
 	{
 		return enforcedSequence(id(), AS_EQUAL, expr());
 	}
 
 	//------------------- Slot Def ---------------------------------------------
 	public Rule enumValDefs()
 	{
 		return sequence(enumValDef(), zeroOrMore(sequence(SP_COMMA, enumValDef())), eos());
 	}
 
 	public Rule enumValDef()
 	{
 		// Fantom grammar is missing "doc"
 		return sequence(OPT_LF(), optional(doc()), id(), optional(enforcedSequence(PAR_L, optional(args()), PAR_R)));
 	}
 
 	public Rule slotDef()
 	{
 		// Rewrote this to "unify" slots common parts (for better performance)
 		return sequence(
 			OPT_LF(),
 			sequence(
 			optional(doc()),// common to all slots
 			zeroOrMore(facet()),// common to all slots
 			optional(protection()),// common to all slots
 			firstOf(
 				ctorDef(), // look for 'new'
 				methodDef(), // look for params : '('
 				fieldDef())), // others
 			ast.newNode(AstKind.AST_SLOT_DEF),
 			OPT_LF());
 	}
 
 	public Rule fieldDef()
 	{
 		return sequence(sequence(
 			zeroOrMore(sequence(firstOf(KW_ABSTRACT, KW_CONST, KW_FINAL, KW_STATIC,
 			KW_NATIVE, KW_OVERRIDE, KW_READONLY, KW_VIRTUAL), ast.newNode(AstKind.AST_MODIFIER))),
 			// Some fantom code has protection after modifiers, so allowing that
 			optional(protection()),
 			/*typeAndOrId(), // Type required for fields(no infered) (Grammar does not say so)*/
 			type(),  ast.newNode(AstKind.AST_TYPE),
 			id(), ast.newNode(AstKind.AST_ID),
 			setFieldInit(true),
 			optional(enforcedSequence(AS_INIT, OPT_LF(), expr())),
 			optional(fieldAccessor()),
 			setFieldInit(false)), ast.newNode(AstKind.AST_FIELD_DEF),
 			eos());
 	}
 
 	public Rule methodDef()
 	{
 		return sequence(enforcedSequence(
 				sequence(
 				// Fan grammar misses 'final'
 				zeroOrMore(sequence(firstOf(KW_ABSTRACT, KW_NATIVE, KW_ONCE, KW_STATIC,
 				KW_OVERRIDE, KW_VIRTUAL, KW_FINAL), ast.newNode(AstKind.AST_MODIFIER))),
 				// Some fantom code has protection after modifiers, so allowing that
 				optional(protection()),
 				type(), ast.newNode(AstKind.AST_TYPE),
 				id(), ast.newNode(AstKind.AST_ID),
 				PAR_L),
 			optional(params()),
 			PAR_R,
 			methodBody()), ast.newNode(AstKind.AST_METHOD_DEF)); // nees own scope because of params
 	}
 
 	public Rule ctorDef()
 	{
 		return sequence(enforcedSequence(KW_NEW,
 			id(), ast.newNode(AstKind.AST_ID),
 			PAR_L, 
 			optional(params()),
 			PAR_R,
 			optional( // ctorChain
 			// Fantom  Grammar page is missing the ':'
 			enforcedSequence(sequence(OPT_LF(), SP_COL),
 			firstOf(
 			enforcedSequence(KW_THIS, DOT, id(), enforcedSequence(PAR_L, optional(args()), PAR_R)),
 			enforcedSequence(KW_SUPER, optional(enforcedSequence(DOT, id())), enforcedSequence(PAR_L, optional(args()), PAR_R))))),
 			methodBody()), ast.newNode(AstKind.AST_CTOR_DEF));
 	}
 
 	public Rule methodBody()
 	{
 		return sequence(firstOf(
 			enforcedSequence(sequence(OPT_LF(), BRACKET_L), zeroOrMore(stmt()), OPT_LF(), BRACKET_R),
 			eos()), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF()); // method with no body
 	}
 
 	public Rule params()
 	{
 		return sequence(param(), zeroOrMore(enforcedSequence(SP_COMMA, params())));
 	}
 
 	public Rule param()
 	{
 		return sequence(OPT_LF(), 
 			sequence(type(), ast.newNode(AstKind.AST_TYPE), id(), ast.newNode(AstKind.AST_ID), optional(enforcedSequence(AS_INIT, expr()))),
 			ast.newNode(AstKind.AST_PARAM),
 			OPT_LF());
 	}
 
 	public Rule fieldAccessor()
 	{
 		return sequence(OPT_LF(),
 			enforcedSequence(
 			BRACKET_L,
 			optional(sequence(OPT_LF(), enforcedSequence(GET, firstOf(block(), eos())))),
 			optional(sequence(OPT_LF(), optional(protection()), enforcedSequence(SET, firstOf(block(), eos())))),
 			BRACKET_R), ast.newNode(AstKind.AST_FIELD_ACCESSOR)); // do not consume trailing LF, since fieldDef looks for EOS
 	}
 
 	public Rule args()
 	{
 		return sequence(expr(), ast.newNode(AstKind.AST_ARG),
 			zeroOrMore(
 				sequence(OPT_LF(),
 				enforcedSequence(SP_COMMA, OPT_LF(), expr(), ast.newNode(AstKind.AST_ARG)))),
 			OPT_LF());
 	}
 
 	// ------------ Statements -------------------------------------------------
 	public Rule block()
 	{
 		return sequence(OPT_LF(), firstOf(
 			enforcedSequence(BRACKET_L, zeroOrMore(stmt()), OPT_LF(), BRACKET_R),
 			stmt() // single statement
 			), ast.newScopeNode(AstKind.AST_BLOCK), OPT_LF());
 	}
 
 	public Rule stmt()
 	{
 		return sequence(testNot(BRACKET_R), OPT_LF(),
 			firstOf(
 			sequence(KW_BREAK, eos()),
 			sequence(KW_CONTINUE, eos()),
 			sequence(KW_RETURN, sequence(optional(expr()), eos())),
 			sequence(KW_THROW, expr(), eos()),
 			if_(),
 			for_(),
 			switch_(),
 			while_(),
 			try_(),
 			// check local var definition as it's faster to parse ':='
 			localDef(),
 			// otherwise expression (optional Comma for itAdd expression)
 			// using firstOf, because "," in this case can be considered an end of statement
 			sequence(expr(), firstOf(sequence(SP_COMMA, optional(eos())), eos()))),
 			OPT_LF());
 	}
 
 	public Rule for_()
 	{
 		return sequence(enforcedSequence(KW_FOR, PAR_L,
 			// LocalDef consumes the SEMI as part of loking for EOS, so rewrote to deal with this
 			firstOf(SP_SEMI, firstOf(localDef(), sequence(expr(), SP_SEMI))),
 			firstOf(SP_SEMI, sequence(expr(), SP_SEMI)),
 			optional(expr()),
 			PAR_R,
 			block()), ast.newScopeNode(AstKind.AST_FOR_LOOP)); // introducing a scopr for the loop var
 	}
 
 	public Rule if_()
 	{
 		// using condExpr rather than expr
 		return enforcedSequence(KW_IF, PAR_L, condOrExpr(), PAR_R, block(),
 			optional(enforcedSequence(KW_ELSE, block())));
 	}
 
 	public Rule while_()
 	{
 		// using condExpr rather than expr
 		return enforcedSequence(KW_WHILE, PAR_L, condOrExpr(), PAR_R, block());
 	}
 
 	public Rule localDef()
 	{
 		// slight change from the grammar to match either:
 		// 'Int j', 'j:=27', 'Int j:=27'
 		return sequence(
 			firstOf(
 			// fan parser says if it's start with "id :=" or "Type, id", then it gotta be a localDef (enforce)
 			enforcedSequence(typeAndId(), AS_INIT, OPT_LF(), expr()),
 			// same if it starts with "id :="
 			enforcedSequence(sequence(id(), ast.newNode(AstKind.AST_ID), AS_INIT), OPT_LF(), expr()),
 			// var def with no value
 			typeAndId()),
 			ast.newNode(AstKind.AST_LOCAL_DEF),
 			eos());
 	}
 
 	public Rule typeAndId()
 	{
 		return sequence(sequence(type(), ast.newNode(AstKind.AST_TYPE), id(), ast.newNode(AstKind.AST_ID)), ast.newNode(AstKind.AST_TYPE_AND_ID));
 	}
 
 	public Rule try_()
 	{
 		return enforcedSequence(KW_TRY, block(), zeroOrMore(catch_()),
 			optional(sequence(KW_FINALLY, block())));
 	}
 
 	public Rule catch_()
 	{
 		return sequence(enforcedSequence(KW_CATCH,
 				optional(enforcedSequence(PAR_L, type(),
 				ast.newNode(AstKind.AST_TYPE), id(),
 				ast.newNode(AstKind.AST_ID), PAR_R)), block()),
 			ast.newNode(AstKind.AST_CATCH_BLOCK));
 	}
 
 	public Rule switch_()
 	{
 		return enforcedSequence(KW_SWITCH, PAR_L, expr(), PAR_R,
 			OPT_LF(), BRACKET_L, OPT_LF(),
 			zeroOrMore(enforcedSequence(KW_CASE, expr(), SP_COL, zeroOrMore(firstOf(stmt(), LF())))),
 			optional(enforcedSequence(KW_DEFAULT, SP_COL, zeroOrMore(firstOf(stmt(), LF())))),
 			OPT_LF(), BRACKET_R);
 	}
 
 	// ----------- Expressions -------------------------------------------------
 	public Rule expr()
 	{
 		return sequence(assignExpr(), ast.newNode(AstKind.AST_EXPR));
 	}
 
 	public Rule assignExpr()
 	{
 		// check '=' first as is most common
 		// moved localDef to statement since it can't be on the right hand side
 		return sequence(ifExpr(), optional(enforcedSequence(firstOf(AS_EQUAL, AS_OPS), OPT_LF(), assignExpr(), ast.newNode(AstKind.AST_EXPR_ASSIGN))));
 	}
 
 	public Rule ifExpr()
 	{
 		// rewritten (together with ternaryTail, elvisTail) such as we check condOrExpr only once
 		// this makes a gigantic difference in parser speed form original grammar
 		return sequence(condOrExpr(),
 			optional(firstOf(elvisTail(), ternaryTail())));
 	}
 
 	//TODO: ternary expression absolutely ille the parser, doing some strange crazy backtracking
 	// needs to be investigated / fixed
 	public Rule ternaryTail()
 	{
 		return enforcedSequence(sequence(OPT_LF(), SP_QMARK), echo("ternary"), OPT_LF(), setNoSimpleMap(true), ifExprBody(), setNoSimpleMap(false), OPT_LF(), SP_COL, OPT_LF(), ifExprBody());
 	}
 
 	public Rule elvisTail()
 	{
 		return enforcedSequence(sequence(OPT_LF(), OP_ELVIS), OPT_LF(), ifExprBody());
 	}
 
 	public Rule ifExprBody()
 	{
 		return firstOf(enforcedSequence(KW_THROW, expr()), condOrExpr());
 	}
 
 	public Rule condOrExpr()
 	{
 		return sequence(condAndExpr(), zeroOrMore(enforcedSequence(OP_OR, OPT_LF(), condAndExpr())));
 	}
 
 	public Rule condAndExpr()
 	{
 		return sequence(equalityExpr(), zeroOrMore(enforcedSequence(OP_AND, OPT_LF(), equalityExpr())));
 	}
 
 	public Rule equalityExpr()
 	{
 		return sequence(relationalExpr(), zeroOrMore(enforcedSequence(CP_EQUALITY, OPT_LF(), relationalExpr())));
 	}
 
 	public Rule relationalExpr()
 	{
 		// Changed (with typeCheckTail, compareTail) to check for rangeExpr only once (way faster)
 		return sequence(rangeExpr(), optional(firstOf(typeCheckTail(), compareTail())));
 	}
 
 	public Rule typeCheckTail()
 	{
 		// changed to required, otherwise consumes all rangeExpr and compare never gets evaled
 		return sequence(enforcedSequence(
 				firstOf(KW_IS, KW_ISNOT, KW_AS),
 				type(),
 				ast.newNode(AstKind.AST_TYPE)),
 			ast.newNode(AstKind.AST_EXPR_TYPE_CHECK));
 	}
 
 	public Rule compareTail()
 	{
 		// changed to not be zeroOrMore as there can be only one comparaison check in an expression (no 3< x <5)
 		return /*zeroOrMore(*/enforcedSequence(CP_COMPARATORS, OPT_LF(), rangeExpr())/*)*/;
 	}
 
 	public Rule rangeExpr()
 	{
 		// changed to not be zeroOrMore(opt instead) as there can be only one range in an expression (no [1..3..5])
 		return sequence(sequence(addExpr(), ast.newNode(AstKind.AST_EXPR),
 			optional(enforcedSequence(firstOf(OP_RANGE_EXCL, OP_RANGE), OPT_LF(), addExpr(), ast.newNode(AstKind.AST_EXPR)))),
 				ast.newNode(AstKind.AST_EXPR_RANGE));
 	}
 
 	public Rule addExpr()
 	{
 		return sequence(multExpr(),
 			// checking it's not '+=' or '-=', so we can let assignExpr happen
 			zeroOrMore(enforcedSequence(sequence(firstOf(OP_PLUS, OP_MINUS), testNot(AS_EQUAL)), OPT_LF(), multExpr(), ast.newNode(AstKind.AST_EXPR_ADD))));
 	}
 
 	public Rule multExpr()
 	{
 		return sequence(parExpr(),
 			// checking it's not '*=', '/=' or '%=', so we can let assignExpr happen
 			zeroOrMore(enforcedSequence(sequence(firstOf(OP_MULT, OP_DIV, OP_MODULO), testNot(AS_EQUAL)), OPT_LF(), parExpr(), ast.newNode(AstKind.AST_EXPR_MULT))));
 	}
 
 	public Rule parExpr()
 	{
 		return firstOf(castExpr(), groupedExpr(), unaryExpr());
 	}
 
 	public Rule castExpr()
 	{
 		return sequence(sequence(PAR_L, type(), ast.newNode(AstKind.AST_TYPE), PAR_R, parExpr(), ast.newNode(AstKind.AST_EXPR)), ast.newNode(AstKind.AST_EXR_CAST));
 	}
 
 	public Rule groupedExpr()
 	{
 		return sequence(PAR_L, OPT_LF(), expr(), OPT_LF(), PAR_R, zeroOrMore(termChain()));
 	}
 
 	public Rule unaryExpr()
 	{
 		// grouped with postfixEpr to avoid looking for termExpr twice (very slow parsing !)
 		return firstOf(prefixExpr(), sequence(termExpr(), optional(firstOf(OP_2MINUS, OP_2PLUS))));
 	}
 
 	public Rule prefixExpr()
 	{
 		return sequence(
 			firstOf(OP_CURRY, OP_BANG, OP_2PLUS, OP_2MINUS, OP_PLUS, OP_MINUS),
 			parExpr());
 	}
 
 	public Rule termExpr()
 	{
 		return sequence(termBase(), zeroOrMore(termChain()));
 	}
 
 	public Rule termBase()
 	{
 		// check for ID alone last (and not as part of idExpr) otherwise it would never check literal & typebase !
 		return firstOf(idExprReq(), litteral(), typeBase(), sequence(id(), ast.newNode(AstKind.AST_ID)));
 	}
 
 	public Rule typeBase()
 	{
 		return firstOf(
 				enforcedSequence(sequence(OP_POUND, id()), ast.newNode(AstKind.AST_TYPE_LITTERAL)), // slot litteral (without type)
 				closure(),
 				dsl(), // DSL
 				// Optimized by grouping all the items that start with "type" (since looking for type if resource intensive)
 				sequence(type(), ast.newNode(AstKind.AST_ID),
 				firstOf(
 					sequence(sequence(OP_POUND, optional(id())), ast.newNode(AstKind.AST_TYPE_LITTERAL)), // type/slot litteral
 					sequence(DOT, KW_SUPER, ast.newNode(AstKind.AST_CALL)), // named super
 					sequence(DOT, idExpr()), // static call
 					sequence(PAR_L, expr(), PAR_R), // simple ?? (ctor call)
 					itBlock() // ctor block
 				)));
 	}
 
 	public Rule dsl()
 	{
 		//TODO: unclosed DSL ?
 		return sequence(simpleType(),
 			enforcedSequence(DSL_OPEN, OPT_LF(), zeroOrMore(sequence(testNot(DSL_CLOSE), any())), OPT_LF(), DSL_CLOSE)).label(TokenName.DSL.name());
 	}
 
 	public Rule closure()
 	{
 		return sequence(sequence(OPT_LF(), funcType(), OPT_LF(),
 			sequence(enforcedSequence(BRACKET_L, zeroOrMore(stmt()), BRACKET_R), ast.newScopeNode(AstKind.AST_BLOCK))
 			), ast.newNode(AstKind.AST_CLOSURE));
 	}
 
 	public Rule itBlock()
 	{
 		return sequence(
 			OPT_LF(),
 			enforcedSequence(sequence(BRACKET_L,
 			// Note, don't allow field accesors to be parsed as itBlock
 			testNot(sequence(inFieldInit, OPT_LF(), firstOf(protection(), KW_STATIC, KW_READONLY, GET, SET, GET, SET)/*, echo("Skipping itBlock")*/))),
 			zeroOrMore(stmt()), BRACKET_R), ast.newScopeNode(AstKind.AST_IT_BLOCK));
 	}
 
 	public Rule termChain()
 	{
 		return sequence(OPT_LF(),
 				firstOf(safeDotCall(), safeDynCall(), dotCall(), dynCall(), indexExpr(),
 			callOp(), itBlock(), incCall()));
 	}
 
 	public Rule dotCall()
 	{
 		// test not "..", as this would be a range
 		return sequence(enforcedSequence(sequence(DOT, testNot(DOT)), ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule dynCall()
 	{
 		return sequence(enforcedSequence(OP_ARROW, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule safeDotCall()
 	{
 		return sequence(enforcedSequence(OP_SAFE_CALL, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	public Rule safeDynCall()
 	{
 		return sequence(enforcedSequence(OP_SAFE_DYN_CALL, ast.newNode(AstKind.LBL_OP), idExpr()), ast.newNode(AstKind.AST_CALL_EXPR));
 	}
 
 	// incomplete dot call, make valid to allow for completion
 	//TODO: this is not shown as an error
 	public Rule incCall()
 	{
 		return sequence(testNot(sequence(DOT, DOT)), // DOT DOT would be a range.
 			firstOf(DOT, OP_SAFE_DYN_CALL), ast.newNode(AstKind.AST_INC_CALL));
 	}
 
 	public Rule idExpr()
 	{
 		// this can be either a local def(toto.value) or a call(toto.getValue or toto.getValue(<params>)) + opt. closure
 		return firstOf(idExprReq(), 
 				sequence(
 					sequence(id(), ast.newNode(AstKind.AST_ID))
 					,ast.newNode(AstKind.AST_CALL)));
 	}
 
 	public Rule idExprReq()
 	{
 		// Same but without matching ID by itself (this would prevent termbase from checking literals).
 		return firstOf(field(), call());
 	}
 
 	// require '*' otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule field()
 	{
 		return sequence(OP_MULT, id());
 	}
 
 	// require params or/and closure, otherwise it's just and ID (this would prevent termbase from checking literals)
 	public Rule call()
 	{
 		return sequence(sequence(id(), ast.newNode(AstKind.AST_ID),
 			firstOf(
 			sequence(noSpace(), enforcedSequence(PAR_L, OPT_LF(), optional(args()), PAR_R), optional(closure())), //params & opt. closure
 			closure())), ast.newNode(AstKind.AST_CALL)); // closure only
 	}
 
 	public Rule indexExpr()
 	{
 		return sequence(noSpace(), SQ_BRACKET_L, expr(), ast.newNode(AstKind.AST_EXPR_INDEX), SQ_BRACKET_R);
 	}
 
 	public Rule callOp()
 	{
 		return enforcedSequence(noSpace(), PAR_L, optional(args()), PAR_R, optional(closure()));
 	}
 
 	public Rule litteral()
 	{
 		return firstOf(litteralBase(), list(), map());
 	}
 
 	public Rule litteralBase()
 	{
 		return sequence(firstOf(KW_NULL, KW_THIS, KW_SUPER, KW_IT, KW_TRUE, KW_FALSE,
 			strs(), uri(), number(), char_()), ast.newNode(AstKind.AST_EXPR_LIT_BASE));
 	}
 
 	public Rule list()
 	{
 		return sequence(sequence(
 			optional(sequence(type(), ast.newNode(AstKind.AST_TYPE))), OPT_LF(),
 			SQ_BRACKET_L, OPT_LF(), listItems(), OPT_LF(), SQ_BRACKET_R), ast.newNode(AstKind.AST_LIST));
 	}
 
 	public Rule listItems()
 	{
 		return firstOf(
 			SP_COMMA,
 			// allow extra comma
 			sequence(expr(), zeroOrMore(sequence(SP_COMMA, OPT_LF(), expr())), optional(SP_COMMA)));
 	}
 
 	public Rule map()
 	{
 		return sequence(sequence(
 			optional(sequence(firstOf(mapType(), simpleMapType()), ast.newNode(AstKind.AST_TYPE))),
 			// Not enforced to allow resolving list of typed maps like [Str:Int][]
 			sequence(SQ_BRACKET_L, OPT_LF(), mapItems(), OPT_LF(), SQ_BRACKET_R)),ast.newNode(AstKind.AST_MAP));
 	}
 
 	public Rule mapItems()
 	{
 		return firstOf(SP_COL,//empty map
 			// allow extra comma
 			sequence(mapPair(), zeroOrMore(sequence(SP_COMMA, OPT_LF(), mapPair())), optional(SP_COMMA)));
 	}
 
 	public Rule mapPair()
 	{
 		// allowing all expressions is probably more than really needed
 		return sequence(sequence(expr(), enforcedSequence(SP_COL, expr())), ast.newNode(AstKind.AST_MAP_PAIR));
 	}
 
 	public Rule mapItem()
 	{
 		return expr();
 	}
 
 	// ------------ Litteral items ---------------------------------------------
 	@Leaf
 	public Rule strs()
 	{
 		return firstOf(
 			enforcedSequence("\"\"\"", // triple quoted string, // (not using 3QUOTE terminal, since it could consume empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			escapedChar(),
 			sequence(testNot(QUOTES3), any()))), QUOTES3),
 			enforcedSequence("\"", // simple string, (not using QUOTE terminal, since it could consume empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			escapedChar(),
 			sequence(testNot(QUOTE), any()))), QUOTE)).label(TokenName.STRS.name());
 	}
 
 	@Leaf
 	public Rule uri()
 	{
 		return enforcedSequence("`",// (not using TICK terminal, since it could consume empty space inside the string)
 			zeroOrMore(firstOf(
 			unicodeChar(),
 			// missing from Fantom litteral page, special URI escape sequences
 			sequence('\\', firstOf(':','/','#','[',']','@','&','=',';','?')),
 			escapedChar(),
 			sequence(testNot(TICK), any()))),
 			TICK).label(TokenName.URI.name());
 	}
 
 	@Leaf
 	public Rule char_()
 	{
 		return enforcedSequence('\'',// (not using SINGLE_Q terminal, since it could consume empty space inside the char)
 				firstOf(
 				unicodeChar(),
 				escapedChar(), // standard esapes
 				any()), //all else
 				SINGLE_Q).label(TokenName.CHAR_.name());
 	}
 
 	@Leaf
 	public Rule escapedChar()
 	{
 		return enforcedSequence('\\', firstOf('b', 'f', 'n', 'r', 't', '"', '\'', '`', '$', '\\'));
 	}
 
 	@Leaf
 	public Rule unicodeChar()
 	{
 		return enforcedSequence("\\u", hexDigit(), hexDigit(), hexDigit(), hexDigit());
 	}
 
 	@Leaf
 	public Rule hexDigit()
 	{
 		return firstOf(digit(),
 			charRange('a', 'f'),
 			charRange('A', 'F'));
 	}
 
 	@Leaf
 	public Rule digit()
 	{
 		return charRange('0', '9');
 	}
 
 	@Leaf
 	public Rule number()
 	{
 		return sequence(
 			optional(OP_MINUS),
 			firstOf(
 			// hex number
 			enforcedSequence(firstOf("0x", "0X"), oneOrMore(firstOf("_", hexDigit()))),
 			// decimal
 			// fractional
 			enforcedSequence(fraction(), optional(exponent())),
 			enforcedSequence(digit(),
 			zeroOrMore(sequence(zeroOrMore("_"), digit())),
 			optional(fraction()),
 			optional(exponent()))),
 			optional(nbType())).label(TokenName.NUMBER.name());
 	}
 
 	@Leaf
 	public Rule fraction()
 	{
 		// not enfored to allow: "3.times ||" constructs as well as ranges 3..5
 		return sequence(DOT, digit(), zeroOrMore(sequence(zeroOrMore("_"), digit())));
 	}
 
 	@Leaf
 	public Rule exponent()
 	{
 		return enforcedSequence(charSet("eE"),
 			optional(firstOf(OP_PLUS, OP_MINUS)),
 			digit(),
 			zeroOrMore(sequence(zeroOrMore("_"), digit())));
 	}
 
 	@Leaf
 	public Rule nbType()
 	{
 		return firstOf(
 			"day", "hr", "min", "sec", "ms", "ns", //durations
 			"f", "F", "D", "d" // float / decimal
 			);
 	}
 
 	/** Rewrote more like Fantom Parser (simpler & faster) - look for type base then listOfs, mapOfs notations
 	 * Added check so that the "?" (nullable type indicator) cannot be separated from it's type (noSpace)
 	 * @return
 	 */
 	public Rule type()
 	{
 		return sequence(
 			firstOf(
 				mapType(),
 				funcType(),
 				sequence(id(), optional(sequence(noSpace(), SP_COLCOL, noSpace(), id())))
 				),
 			// Look for optional map/list/nullable items
 			optional(sequence(noSpace(), SP_QMARK)), //nullable
 			zeroOrMore(sequence(noSpace(),SQ_BRACKET_L, SQ_BRACKET_R)),//list(s)
 			// Do not allow simple maps within left side of expressions ... this causes issues with ":"
 			optional(sequence(testNot(noSimpleMap), SP_COL, type())),//simple map Int:Str
 			optional(sequence(noSpace(), SP_QMARK)) // nullable list/map
 			);
 	}
 
 	public Rule simpleMapType()
 	{
 		// It has to be other nonSimpleMapTypes, otherwise it's left recursive (loops forever)
 		return sequence(nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(),
 			optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			sequence(optional(sequence(noSpace(),SP_QMARK)), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			optional(sequence(noSpace(),SP_QMARK))); // nullable
 	}
  
 	// all types except simple map
 	public Rule nonSimpleMapTypes()
 	{
 		return sequence(
 			firstOf(funcType(), mapType(), simpleType()),
 			optional(
 			// Not enforcing [] because of problems with maps like this Str:int["":5]
 			sequence(optional(sequence(noSpace(),SP_QMARK)), SQ_BRACKET_L, SQ_BRACKET_R)), // list of '?[]'
 			optional(sequence(noSpace(),SP_QMARK))); // nullable
 	}
  
 	public Rule simpleType()
 	{
 		return sequence(
 			id(),
 			optional(enforcedSequence(SP_COLCOL, id())));
 	}
 
 	public Rule mapType()
 	{
 		// We use nonSimpleMapTypes here as well, because [Str:Int:Str] would be confusing
 		// not enforced to allow map rule to work ([Int:Str][5,""])
 		return sequence(SQ_BRACKET_L, nonSimpleMapTypes(), SP_COL, nonSimpleMapTypes(), SQ_BRACKET_R);
 	}
 
 	public Rule funcType()
 	{
 		return sequence(
 			testNot(OP_OR), // '||' that's a logical OR not a function type
 			// '|' could be the closing pipe so we can't enforce
 			sequence(SP_PIPE,
 			firstOf(
 			// First we check for one with no formals |->| or |->Str|
 			enforcedSequence(OP_ARROW, optional(sequence(type(), ast.newNode(AstKind.AST_TYPE)))),
 			// Then we check for one with formals |Int i| or maybe full: |Int i -> Str|
 			sequence(formals(), optional(enforcedSequence(OP_ARROW, optional(sequence(type(), ast.newNode(AstKind.AST_TYPE))))))),
 			SP_PIPE));
 	}
 
 	public Rule formals()
 	{
 		// Allowing funcType within formals | |Int-Str a| -> Str|
 		return sequence(
 			formal(),
 			zeroOrMore(enforcedSequence(SP_COMMA, formal())));
 	}
 
 	public Rule typeList()
 	{
 		return sequence(type(), ast.newNode(AstKind.AST_TYPE), zeroOrMore(enforcedSequence(SP_COMMA, type(), ast.newNode(AstKind.AST_TYPE))));
 	}
 
 	public Rule formal()
 	{
 		// Note it can be "type id", "type" or "id"
 		// but parser can't know if it's "type" or "id" so always recognize as type
 		// so would never actually hit id()
 		return sequence(firstOf(
 			typeAndId(),
 			sequence(type(), ast.newNode(AstKind.AST_ID)))
 			/*, id()*/
 			, ast.newNode(AstKind.AST_FORMAL));
 	}
 	// ------------ Misc -------------------------------------------------------
 
 	@Leaf
 	public Rule id()
 	{
 		return sequence(testNot(keyword()),
 			sequence(firstOf(charRange('A', 'Z'), charRange('a', 'z'), "_"),
 			zeroOrMore(firstOf(charRange('A', 'Z'), charRange('a', 'z'), '_', charRange('0', '9')))),
 			OPT_SP).label(TokenName.ID.name());
 	}
 
 	@Leaf
 	public Rule doc()
 	{
 		// In theory there are no empty lines betwen doc and type ... but that does happen so alowing it
 		return oneOrMore(sequence(OPT_SP, "**", zeroOrMore(sequence(testNot("\n"), any())), OPT_LF())).label(TokenName.DOC.name());
 	}
 
 	@Leaf
 	public Rule spacing()
 	{
 		return oneOrMore(firstOf(
 			// whitespace (Do NOT eat \n since it can be meaningful)
 			whiteSpace(), comment())).label(TokenName.SPACING.name());
 	}
 
 	@Leaf
 	public Rule whiteSpace()
 	{
 		return oneOrMore(charSet(" \t\u000c")).label(TokenName.WHITESPACE.name());
 	}
 
 	@Leaf
 	public Rule comment()
 	{
 		return firstOf(
 			// multiline comment
 			sequence("/*", zeroOrMore(sequence(testNot("*/"), any())), "*/"),
 			// if incomplete multiline comment, then end at end of line
 			sequence("/*", zeroOrMore(sequence(testNot(charSet("\r\n")), any()))),
 			// single line comment
 			sequence("//", zeroOrMore(sequence(testNot(charSet("\r\n")), any())))).label(TokenName.COMMENT.name());
 	}
 
 	@Leaf
 	public Rule LF()
 	{
 		return sequence(oneOrMore(sequence(OPT_SP, charSet("\n\r"))), OPT_SP).label(TokenName.LF.name());
 	}
 
 	@Leaf
 	public Rule OPT_LF()
 	{
 		return optional(LF());
 	}
 
 	@Leaf
 	public Rule keyword(String string)
 	{
 		// Makes sure not to match things that START with a keyword like "thisisatest"
 		return sequence(string,
 			testNot(firstOf(digit(), charRange('A', 'Z'), charRange('a', 'z'), "_")),
 			optional(spacing())).label(string);
 	}
 
 	@Leaf
 	public Rule terminal(String string)
 	{
 		return sequence(string, optional(spacing())).label(string);
 	}
 
 	@Leaf
 	public Rule terminal(String string, Rule mustNotFollow)
 	{
 		return sequence(string, testNot(mustNotFollow), optional(spacing())).label(string);
 	}
 
 	@Leaf
 	public Rule keyword()
 	{
 		return sequence(
 			// don't bother unless it starts with 'a'-'z'
 			test(charRange('a', 'z')),
 			firstOf(KW_ABSTRACT, KW_AS, KW_ASSERT, KW_BREAK, KW_CATCH, KW_CASE, KW_CLASS,
 			KW_CONST, KW_CONTINUE, KW_DEFAULT, KW_DO, KW_ELSE, KW_FALSE, KW_FINAL,
 			KW_FINALLY, KW_FOR, KW_FOREACH, KW_IF, KW_INTERNAL, KW_IS, KW_ISNOT, KW_IT,
 			KW_MIXIN, KW_NATIVE, KW_NEW, KW_NULL, KW_ONCE, KW_OVERRIDE, KW_PRIVATE,
 			KW_PROTECTED, KW_PUBLIC, KW_READONLY, KW_RETURN, KW_STATIC, KW_SUPER, KW_SWITCH,
 			KW_THIS, KW_THROW, KW_TRUE, KW_TRY, KW_USING, KW_VIRTUAL, KW_VOID, KW_VOLATILE,
 			KW_WHILE)).label(TokenName.KEYWORD.name());
 	}
 	// -------------- Terminal items -------------------------------------------
 	// -- Keywords --
 	public final Rule KW_ABSTRACT = keyword("abstract");
 	public final Rule KW_AS = keyword("as");
 	public final Rule KW_ASSERT = keyword("assert"); // not a grammar kw
 	public final Rule KW_BREAK = keyword("break");
 	public final Rule KW_CATCH = keyword("catch");
 	public final Rule KW_CASE = keyword("case");
 	public final Rule KW_CLASS = keyword("class");
 	public final Rule KW_CONST = keyword("const");
 	public final Rule KW_CONTINUE = keyword("continue");
 	public final Rule KW_DEFAULT = keyword("default");
 	public final Rule KW_DO = keyword("do"); // unused, reserved
 	public final Rule KW_ELSE = keyword("else");
 	public final Rule KW_FALSE = keyword("false");
 	public final Rule KW_FINAL = keyword("final");
 	public final Rule KW_FINALLY = keyword("finally");
 	public final Rule KW_FOR = keyword("for");
 	public final Rule KW_FOREACH = keyword("foreach"); // unused, reserved
 	public final Rule KW_IF = keyword("if");
 	public final Rule KW_INTERNAL = keyword("internal");
 	public final Rule KW_IS = keyword("is");
 	public final Rule KW_IT = keyword("it");
 	public final Rule KW_ISNOT = keyword("isnot");
 	public final Rule KW_MIXIN = keyword("mixin");
 	public final Rule KW_NATIVE = keyword("native");
 	public final Rule KW_NEW = keyword("new");
 	public final Rule KW_NULL = keyword("null");
 	public final Rule KW_ONCE = keyword("once");
 	public final Rule KW_OVERRIDE = keyword("override");
 	public final Rule KW_PRIVATE = keyword("private");
 	public final Rule KW_PUBLIC = keyword("public");
 	public final Rule KW_PROTECTED = keyword("protected");
 	public final Rule KW_READONLY = keyword("readonly");
 	public final Rule KW_RETURN = keyword("return");
 	public final Rule KW_STATIC = keyword("static");
 	public final Rule KW_SUPER = keyword("super");
 	public final Rule KW_SWITCH = keyword("switch");
 	public final Rule KW_THIS = keyword("this");
 	public final Rule KW_THROW = keyword("throw");
 	public final Rule KW_TRUE = keyword("true");
 	public final Rule KW_TRY = keyword("try");
 	public final Rule KW_USING = keyword("using");
 	public final Rule KW_VIRTUAL = keyword("virtual");
 	public final Rule KW_VOID = keyword("void"); // unused, reserved
 	public final Rule KW_VOLATILE = keyword("volatile"); // unused, reserved
 	public final Rule KW_WHILE = keyword("while");
 	// Non keyword meningful items
 	public final Rule ENUM = keyword("enum");
 	public final Rule FACET = keyword("facet");
 	public final Rule GET = keyword("get");
 	public final Rule SET = keyword("set");
 	// operators
 	public final Rule OP_SAFE_CALL = terminal("?.");
 	public final Rule OP_SAFE_DYN_CALL = terminal("?->");
 	public final Rule OP_ARROW = terminal("->");
 	public final Rule OP_ELVIS = terminal("?:");
 	public final Rule OP_OR = terminal("||");
 	public final Rule OP_AND = terminal("&&");
 	public final Rule OP_RANGE = terminal("..");
 	public final Rule OP_RANGE_EXCL = terminal("..<");
 	public final Rule OP_CURRY = terminal("&");
 	public final Rule OP_BANG = terminal("!");
 	public final Rule OP_2PLUS = terminal("++");
 	public final Rule OP_2MINUS = terminal("--");
 	public final Rule OP_PLUS = terminal("+");
 	public final Rule OP_MINUS = terminal("-");
 	public final Rule OP_MULT = terminal("*");
 	public final Rule OP_DIV = terminal("/");
 	public final Rule OP_MODULO = terminal("%");
 	public final Rule OP_POUND = terminal("#");
 	// comparators
 	public final Rule CP_EQUALITY = firstOf(terminal("==="), terminal("!=="),
 		terminal("=="), terminal("!="));
 	public final Rule CP_COMPARATORS = firstOf(terminal("<=>"), terminal("<="),
 		terminal(">="), terminal("<"), terminal(">"));
 	// separators
 	public final Rule SP_PIPE = terminal("|");
 	public final Rule SP_QMARK = terminal("?");
 	public final Rule SP_COLCOL = terminal("::");
 	public final Rule SP_COL = terminal(":");
 	public final Rule SP_COMMA = terminal(",");
 	public final Rule SP_SEMI = terminal(";");
 	// assignment
 	public final Rule AS_INIT = terminal(":=");
 	public final Rule AS_EQUAL = terminal("=");
 	public final Rule AS_OPS = firstOf(terminal("*="), terminal("/="),
 		terminal("%="), terminal("+="), terminal("-="));
 	// others
 	public final Rule QUOTES3 = terminal("\"\"\"");
 	public final Rule QUOTE = terminal("\"");
 	public final Rule TICK = terminal("`");
 	public final Rule SINGLE_Q = terminal("'");
 	public final Rule DOT = terminal(".").label(TokenName.DOT.name());
 	public final Rule AT = terminal("@").label(TokenName.AT.name());
 	public final Rule DSL_OPEN = terminal("<|").label(TokenName.DSL_OPEN.name());
 	public final Rule DSL_CLOSE = terminal("|>").label(TokenName.DSL_CLOSE.name());
 	public final Rule SQ_BRACKET_L = terminal("[").label(TokenName.SQ_BRACKET_L.name());
 	public final Rule SQ_BRACKET_R = terminal("]").label(TokenName.SQ_BRACKET_R.name());
 	public final Rule BRACKET_L = terminal("{").label(TokenName.BRACKET_L.name());
 	public final Rule BRACKET_R = terminal("}").label(TokenName.BRACKET_R.name());
 	public final Rule PAR_L = terminal("(").label(TokenName.PAR_L.name());
 	public final Rule PAR_R = terminal(")").label(TokenName.PAR_R.name());
 	// shortcut for optional spacing
 	public final Rule OPT_SP = optional(spacing());
 
 	// ----------- Custom action rules -----------------------------------------
 	/**
 	 * Custom action to find an end of statement
 	 * @return
 	 */
 	@Leaf
 	public Rule eos()
 	{
 		return sequence(OPT_SP, firstOf(
 			SP_SEMI, // ;
 			LF(), // \n
 			test(BRACKET_R), // '}' is end of statement too, but do NOT consume it !
 			test(eoi()))); // EOI is end of statement too, but do NOT consume it !
 	}
 
 	/**Pass if not following some whitespace*/
 	public Rule noSpace()
 	{
 		return testNot(toRule(afterSpace()));
 	}
 
 	public boolean afterSpace()
 	{
 		//char c = getContext().getCurrentLocation().lookAhead(getContext().getInputBuffer(), -1);
 		int index = getContext().getCurrentLocation().getIndex();
 		char c = getContext().getInputBuffer().charAt(index-1);
 		return Character.isWhitespace(c);
 	}
 
 	public boolean setFieldInit(boolean val)
 	{
 		inFieldInit = val;
 		return true;
 	}
 	public boolean setNoSimpleMap(boolean val)
 	{
 		noSimpleMap = val;
 		return true;
 	}
 	public boolean setInEnum(boolean val)
 	{
 		inEnum = val;
 		return true;
 	}
 	/**
 	 * In Parboiled 0.9.5.0, enforcedSequence was removed, just use sequence now.
 	 * Maybe do a replaceAll later.
 	 * @param rules
 	 * @return
 	 */
 	public Rule enforcedSequence(Object... rules)
 	{
 		return sequence(rules);
 	}
 
 	// Debugging utils
 	/** System.out - eval to true*/
 	public boolean echo(String str)
 	{
 		System.out.println(str);
 		return true;
 	}
 	/** System.out - eval to false*/
 	public boolean echof(String str)
 	{
 		System.out.println(str);
 		return false;
 	}
 
 	/** System.out current node path - eval to true*/
 	public boolean printPath()
 	{
 		System.out.println(getContext().getPath());
 		return true;
 	}
 
 	public void cancel()
 	{
 		System.out.println("cancel called!");
 		cancel=true;
 	}
 
 		// ============ Simulate a lexer ===========================================
 	// This should just create tokens for the items we want to highlight(color) in the IDE
 	// It should be able to deal with "anything" and not ever fail if possible.
 	public Rule lexer()
 	{
 		// If any changes made here, keep in sync with lexerTokens list in FantomParserTokens.java
 		return sequence(
 				zeroOrMore(lexerItem()).label("lexerItems"),
 				optional(eoi())); // until end of file
 	}
 
 	public Rule lexerItem()
 	{
 		return firstOf(
 				comment(), unixLine(), doc(),
 				strs(), uri(), char_(), dsl(),
 				lexerInit(), lexerComps(), lexerAssign(), lexerOps(), lexerSeps(), // operators/separators
 				BRACKET_L, BRACKET_R, SQ_BRACKET_L, SQ_BRACKET_R, PAR_L, PAR_R, DOT, AT, DSL_CLOSE, DSL_OPEN, // other known items
 				keyword(), lexerId(), number(),
 				whiteSpace(),
 				// "Any" includes "everything else" - items withough highlighting.
 				// "Any" is also is a catchall for other unexpected items (should not happen)
 				any().label(TokenName.UNEXPECTED.name()));
 	}
 
 	public Rule lexerId()
 	{
 		// same as ID but don't allow space/commennts
 		return sequence(testNot(keyword()),
 				sequence(firstOf(charRange('A', 'Z'), charRange('a', 'z'), "_"),
 				zeroOrMore(firstOf(charRange('A', 'Z'), charRange('a', 'z'), '_', charRange('0', '9'))))).label(TokenName.LEXERID.name());
 	}
 
 	public Rule lexerOps()
 	{
 		return firstOf(OP_2MINUS, OP_2PLUS, OP_AND, OP_ARROW, AS_INIT, OP_BANG, OP_CURRY, OP_DIV, OP_ELVIS,
 				OP_MINUS, OP_MODULO, OP_MULT, OP_OR, OP_PLUS, OP_POUND, OP_RANGE, OP_RANGE_EXCL, OP_SAFE_CALL,
 				OP_SAFE_DYN_CALL).label(TokenName.LEXEROPS.name());
 	}
 
 	public Rule lexerSeps()
 	{
 		return firstOf(SP_COL, SP_COLCOL, SP_COMMA, SP_PIPE, SP_QMARK, SP_SEMI).label(TokenName.LEXERSEPS.name());
 	}
 
 	public Rule lexerComps()
 	{
 		return firstOf(CP_COMPARATORS, CP_EQUALITY).label(TokenName.LEXERCOMPS.name());
 	}
 
 	public Rule lexerAssign()
 	{
 		return firstOf(AS_EQUAL, AS_OPS).label(TokenName.LEXERASSIGN.name());
 	}
 
 	public Rule lexerInit()
 	{
 		return AS_INIT.label(TokenName.LEXERINIT.name());
 	}
 
	/**
	 * Helps with debugging parser, not for normal use
	 * @return
	 */
 	@Override
 	public Context<AstNode> getContext()
 	{
 		Context<AstNode> ctx = super.getContext();
 		int now=ctx.getCurrentLocation().getIndex();
 		int before=ctx.getCurrentLocation().getIndex()-10;
 		if(before<0)
 			before=0;
 		String t = ctx.getInputBuffer().extract(before, now);
 		System.out.println("get ctx: @"+now+" : "+t);
 		return ctx;
 	}
 }
