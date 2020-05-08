 package descent.internal.compiler.parser;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import descent.core.compiler.CharOperation;
 import descent.core.compiler.IProblem;
 import descent.internal.compiler.lookup.SemanticRest;
 import static descent.internal.compiler.parser.LINK.LINKc;
 import static descent.internal.compiler.parser.LINK.LINKcpp;
 import static descent.internal.compiler.parser.LINK.LINKd;
 import static descent.internal.compiler.parser.LINK.LINKdefault;
 import static descent.internal.compiler.parser.LINK.LINKpascal;
 import static descent.internal.compiler.parser.LINK.LINKsystem;
 import static descent.internal.compiler.parser.LINK.LINKwindows;
 
 import static descent.internal.compiler.parser.STC.STCconst;
 import static descent.internal.compiler.parser.STC.STCextern;
 import static descent.internal.compiler.parser.STC.STCfinal;
 import static descent.internal.compiler.parser.STC.STCin;
 import static descent.internal.compiler.parser.STC.STCinvariant;
 import static descent.internal.compiler.parser.STC.STClazy;
 import static descent.internal.compiler.parser.STC.STCout;
 import static descent.internal.compiler.parser.STC.STCref;
 import static descent.internal.compiler.parser.STC.STCscope;
 import static descent.internal.compiler.parser.STC.STCstatic;
 import static descent.internal.compiler.parser.STC.STCundefined;
 
 import static descent.internal.compiler.parser.TOK.*;
 
 import static descent.internal.compiler.parser.TY.Taarray;
 import static descent.internal.compiler.parser.TY.Tfunction;
 import static descent.internal.compiler.parser.TY.Tsarray;
 
 // DMD 1.020 and DMD 2.003
 public class Parser extends Lexer {
 	
 	// Tokens expected in several parse locations
 	protected final static char[][] moduleExpectations =  { TOKmodule.charArrayValue };
 	protected final static char[][] declDefsExpectations = toCharArray(new TOK[] { 
 		TOKenum, TOKstruct, TOKunion, TOKclass, TOKinterface, TOKimport,
 		TOKtemplate, TOKmixin, TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid, TOKalias,
 		TOKtypedef, TOKtypeof, TOKthis, TOKinvariant, TOKunittest,
 		TOKnew, TOKdelete, TOKstatic, TOKconst, TOKfinal, TOKauto,
 		TOKscope, TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated,
 		TOKextern, TOKprivate, TOKpackage, TOKprotected, TOKpublic,
 		TOKexport, TOKalign, TOKpragma, TOKdebug, TOKversion
 		/*, TOKiftype */ 
 	});
 	protected final static char[][] declDefs_Lstc2Expectations = toCharArray(new TOK[] {
 		TOKconst, TOKinvariant, TOKfinal, TOKauto, TOKscope,
 		TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated
 	});
 	protected final static char[][] afterStaticExpectations = { 
 		TOKthis.charArrayValue, TOKassert.charArrayValue, TOKimport.charArrayValue
 	};
 	protected final static char[][] elseExpectations = { TOKelse.charArrayValue };
 	protected final static char[][] thisExpectations = { TOKthis.charArrayValue };
 	protected final static char[][] aliasExpectations = { TOKalias.charArrayValue };
 	protected final static char[][] typedefAliasExpectations = { TOKtypedef.charArrayValue, TOKalias.charArrayValue };
 	protected final static char[][] typeofExpectations = { TOKtypeof.charArrayValue };
 	protected final static char[][] classExpectations = { TOKclass.charArrayValue };
 	protected final static char[][] voidExpectations = { TOKvoid.charArrayValue };
 	protected final static char[][] traitsExpectations = { TOKtraits.charArrayValue };
 	protected final static char[][] whileExpectations = { TOKwhile.charArrayValue };
 	protected final static char[][] autoExpectations = { TOKauto.charArrayValue };
 	protected final static char[][] catchExpectations = { TOKcatch.charArrayValue };
 	protected final static char[][] finallyExpectations = { TOKfinally.charArrayValue };
 	protected final static char[][] contractsExpectations = { TOKin.charArrayValue, TOKout.charArrayValue, TOKbody.charArrayValue };
 	protected final static char[][] parameters1Expectations = toCharArray(new TOK[] { 
 		TOKin, TOKout, TOKinout, TOKref, TOKlazy
 	});
 	protected final static char[][] parameters2Expectations = toCharArray(new TOK[] { 
 		TOKconst, TOKinvariant, TOKin, TOKout, TOKinout, TOKref,
 		TOKlazy, TOKscope, TOKfinal, TOKstatic
 	});
 	protected final static char[][] basicTypeExpectations = toCharArray(new TOK[] { 
 		TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid,
 		TOKtypeof, TOKconst, TOKinvariant
 	});
 	protected final static char[][] delegateFunctionExpectations = toCharArray(new TOK[] { 
 		TOKdelegate, TOKfunction
 	});
 	protected final static char[][] modifierExpectations = toCharArray(new TOK[] { 
 		TOKconst, TOKinvariant, TOKstatic, TOKfinal, TOKauto, TOKscope,
 		TOKoverride, TOKabstract, TOKsynchronized, TOKdeprecated,
 		TOKextern
 	});
 	protected final static char[][] statementExpectations = toCharArray(new TOK[] { 
 		TOKtypeof, TOKassert, TOKthis, TOKsuper, TOKnull, TOKtrue,
 		TOKfalse, TOKcast, TOKnew, TOKdelete, TOKdelegate, TOKfunction,
 		TOKtypeid, TOKis, TOKstatic, TOKwchar, TOKdchar, /* TOKbit, */ TOKbool,
 		TOKchar, TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32,
 		TOKuns32, TOKint64, TOKuns64, TOKfloat32, TOKfloat64,
 		TOKfloat80, TOKimaginary32, TOKimaginary64, TOKimaginary80, 
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, TOKvoid,
 		TOKtypeof, TOKconst, TOKinvariant, TOKalias, TOKtypedef, TOKauto,
 		TOKextern, TOKstruct, TOKunion, TOKclass, TOKinterface,
 		TOKenum, TOKmixin, TOKwhile, TOKdo, TOKfor, TOKforeach,
 		TOKforeach_reverse, TOKif, TOKscope, TOKdebug, TOKversion,
 		TOKpragma, TOKswitch, TOKcase, TOKreturn, TOKdefault,
 		TOKbreak, TOKcontinue, TOKgoto, TOKsynchronized, TOKwith,
 		TOKtry, TOKthrow, TOKvolatile, TOKasm
 	});
 	protected final static char[][] primaryExpExpectations = toCharArray(new TOK[] { 
 		TOKthis, TOKsuper, TOKnull, TOKtrue, TOKfalse, TOKvoid,
 		TOKint8, TOKuns8, TOKint16, TOKuns16, TOKint32, TOKuns32,
 		TOKint64, TOKuns64, TOKfloat32, TOKfloat64, TOKfloat80,
 		TOKimaginary32, TOKimaginary64, TOKimaginary80,
 		TOKcomplex32, TOKcomplex64, TOKcomplex80, /* TOKbit, */
 		TOKbool, TOKchar, TOKwchar, TOKdchar, TOKtypeof, 
 		TOKtypeid, TOKis, TOKassert, TOKmixin, TOKimport,
 		TOKfunction, TOKdelegate
 	});
 	protected final static char[][] unaryExpExpectations = { TOKdelete.charArrayValue, TOKnew.charArrayValue, TOKcast.charArrayValue };
 	protected final static char[][] traitsArgsExpectations = { 
 		Id.isArithmetic, Id.isFloating, Id.isIntegral, Id.isScalar, 
 		Id.isUnsigned, Id.isAssociativeArray, Id.isStaticArray,
 		Id.isAbstractClass, Id.isFinalClass, Id.isAbstractFunction, 
 		Id.isVirtualFunction, Id.isFinalFunction, 
 		Id.hasMember, Id.getMember, Id.getVirtualFunctions,
 		Id.classInstanceSize, Id.allMembers, Id.derivedMembers,
 		Id.isSame, Id.compiles
 	};
 	protected final static char[][] scopeArgsExpectations = { 
 		Id.exit, Id.failure, Id.success
 	};
 	protected final static char[][] externArgsExpectations = { 
 		Id.C, Id.Cpp, Id.D, Id.Pascal, Id.System, Id.Windows
 	};
 	protected final static char[][] pragmaArgsExpectations = { 
 		Id.msg, Id.lib
 	};
 	
 	private static char[][] toCharArray(TOK[] toks) {
 		char[][] expect = new char[toks.length][];
 		for (int i = 0; i < toks.length; i++) {
 			expect[i] = toks[i].charArrayValue;
 		}
 		return expect;
 	}
 	
 	private final static boolean LTORARRAYDECL = true;
 	
 	public final static int PSsemi = 1;		// empty ';' statements are allowed
 	public final static int PSscope = 2;	// start a new scope
 	public final static int PScurly = 4;	// { } statement is required
 	public final static int PScurlyscope = 8;	// { } starts a new scope
 	
 	/**
 	 * Wheter to do skip parsing of function bodies if they do not
 	 * contain inner declarations.
 	 */
 	public boolean diet;
 	private boolean inDiet;
 
 	private Module module;
 	private ModuleDeclaration md;
 	private int inBrackets;	
 	
 	private List<Comment> comments;
 	private List<Pragma> pragmas;
 	private int lastCommentRead = 0;
 	private boolean appendLeadingComments = true;
 	
 	private LINK linkage = LINKd;
 
 	public Parser(int apiLevel, String source) {
 		this(apiLevel, source.toCharArray(), 0, source.length(), null);
 	}
 
 	public Parser(int apiLevel, char[] source) {
 		this(apiLevel, source, 0, source.length);
 	}
 	
 	public Parser(int apiLevel, char[] source, char[] filename) {
 		this(apiLevel, source, 0, source.length, filename);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length) {
 		this(apiLevel, source, offset, length, null, null, false, null);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[] filename) {
 		this(apiLevel, source, offset, length, null, null, false, filename);
 	}
 	
 	public Parser(int apiLevel, char[] source, int offset, 
 			int length, char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive, char[] filename) {
 		this(source, offset, length, 
 				true /* tokenize comments */, 
 				true /* tokenize pragmas */,
 				false /* don't tokenize whitespace */, 
 				true /* record line separators */,
 				apiLevel,
 				taskTags, taskPriorities, isTaskCaseSensitive,
 				filename);
 	}
 	
 	public Parser(char[] source, int offset, int length,
 			boolean tokenizeComments, boolean tokenizePragmas,
 			boolean tokenizeWhiteSpace, boolean recordLineSeparator,
 			int apiLevel,
 			char[][] taskTags, char[][] taskPriorities, boolean isTaskCaseSensitive,
 			char[] filename) {
 		super(source, offset, length, tokenizeComments, tokenizePragmas,
 				tokenizeWhiteSpace, recordLineSeparator,
 				apiLevel, filename);
 		if (tokenizeComments) {
 			this.comments = new ArrayList<Comment>(0);
 		}
 		if (tokenizePragmas) {
 			this.pragmas = new ArrayList<Pragma>();
 		}
 		this.taskTags = taskTags;
 		this.taskPriorities = taskPriorities;
 		this.isTaskCaseSensitive = isTaskCaseSensitive;
 		
 		// nextToken();
 	}
 	
 	public Module parseModuleObj() {
 		char[] theFilename = filename == null ? null : getName(filename);
 		module = new Module(theFilename == null ? null : new String(theFilename), theFilename == null ? null : new IdentifierExp(theFilename));
 		parseModuleObj(module);
 		return module;
 	}
 	
 	private char[] getName(char[] filename) {
 		int indexOfSlash = CharOperation.lastIndexOf('/', filename);
 		if (indexOfSlash < 0) {
 			indexOfSlash = CharOperation.lastIndexOf('\\', filename);
 		}
 		if (indexOfSlash < 0) {
 			indexOfSlash = -1;
 		}
 		int indexOfDot = CharOperation.lastIndexOf('.', filename);
 		if (indexOfDot < 0) {
 			indexOfDot = filename.length;
 		}		
 		return CharOperation.subarray(filename, indexOfSlash + 1, indexOfDot);
 	}
 
 	public void parseModuleObj(Module module) {
 		this.module = module;
 		module.members = parseModule();
 		module.sourceMembers = new Dsymbols(module.members);
 		module.md = md;
 		if (comments != null) {
 			module.comments = comments.toArray(new Comment[comments.size()]);
 		}
 		if (pragmas != null) {
 			module.pragmas = pragmas.toArray(new Pragma[pragmas.size()]);
 		}
 		module.lineEnds = getLineEnds();
 		
 		if (taskTags != null) {
 			addTaskTagsToProblems();
 		}
 		
 		module.apiLevel = apiLevel;
 		module.problems = problems;
 		module.start = 0;
 		module.length = this.end;
 	}
 	
 	private void addTaskTagsToProblems() {
 		for(int i = 0; i < foundTaskCount; i++) {
 			IProblem problem = Problem.newTask(
 					new String(CharOperation.concat(foundTaskTags[i], foundTaskMessages[i], ' ')), 
 					getLineNumber(foundTaskPositions[i][0]), 
 					foundTaskPositions[i][0], 
 					foundTaskPositions[i][1] - foundTaskPositions[i][0]);
 			reportProblem(problem);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Dsymbols parseModule() {
 		if (token.value == null) nextToken();
 		
 	    Dsymbols decldefs = new Dsymbols();
 	    
 	    // Special treatment if the file starts with Ddoc
 	    if (token.value == TOKidentifier && CharOperation.equals(token.sourceString, Id.Ddoc)) {
 	    	return decldefs;
 	    }
 
 		// ModuleDeclation leads off
 	    expect(moduleExpectations);
 		if (token.value == TOKmodule) {
 			int start = token.ptr;
 			
 			List<Comment> moduleDocComments = getLastComments();
 			
 			nextToken();
 			if (token.value != TOKidentifier) {
 				
 				// Issue a creation of an empty module declaration
 				newModuleDeclaration(null, null);
 				
 				parsingErrorDeleteToken(prevToken);
 				
 				decldefs = parseDeclDefs(false);
 				if (token.value != TOKeof) {
 					parsingErrorDeleteToken(token);
 				}
 				return decldefs;
 			} else {
 				Identifiers a = null;
 				IdentifierExp id = null;
 				id = newIdentifierExp();
 				while (nextToken() == TOKdot) {
 					if (a == null) {
 						a = new Identifiers();
 					}
 					a.add(id);
 					nextToken();
 					if (token.value != TOKidentifier) {
 						
 						// Issue a creation of an empty module declaration
 						newModuleDeclaration(a, null);
 						
 						parsingErrorInsertTokenAfter(prevToken, ";");
 						
 						decldefs = parseDeclDefs(false);
 						if (token.value != TOKeof) {
 							parsingErrorDeleteToken(token);
 						}
 						return decldefs;
 					}
 					id = newIdentifierExp();
 				}
 
 				md = newModuleDeclaration(a, id);
 				md.setSourceRange(start, token.ptr + token.sourceLen - start);
 				md.preComments = moduleDocComments;
 
 				if (token.value != TOKsemicolon) {
 					setMalformed(md);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 				} else {
 					nextToken();
 				}
 				
 				attachLeadingComments(md);
 			}
 		}
 
 		decldefs = parseDeclDefs(false);
 		if (token.value != TOKeof) {
 			parsingErrorDeleteToken(token);
 			// goto Lerr;
 			return parseModule_LErr(decldefs);
 		}
 		return decldefs;
 	}	
 
 	private Dsymbols parseModule_LErr(Dsymbols decldefs) {
 		while (token.value != TOKsemicolon && token.value != TOKeof)
 	    	nextToken();
 	    nextToken();
 	    return decldefs;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Dsymbols parseDeclDefs(boolean once) {
 		if (token.value == null) nextToken();
 		
 		Dsymbol s = null;
 		Dsymbols decldefs;
 		Dsymbols a = new Dsymbols();
 		Dsymbols aelse;
 		PROT prot;
 		int stc;
 		boolean[] isSingle = new boolean[1];
 		boolean attachLeadingComments = true;
 		
 		decldefs = new Dsymbols();
 		do {
 			List<Comment> lastComments = getLastComments();
 			isSingle[0] = false;
 			attachLeadingComments = true;
 			
 			expect(declDefsExpectations);
 			
 			int start = token.ptr;
 			switch (token.value) {
 			case TOKenum:
 				s = parseEnum();
 				break;
 
 			case TOKstruct:
 			case TOKunion:
 			case TOKclass:
 			case TOKinterface:
 				s = parseAggregate();
 				break;
 
 			case TOKimport:
 				s = parseImport(decldefs, false, token.ptr, lastComments);
 				break;
 
 			case TOKtemplate:
 				s = parseTemplateDeclaration();
 				break;
 
 			case TOKmixin:
 				if (peek(token).value == TOKlparen)
 				{   // mixin(string)
 				    nextToken();
 				    check(TOKlparen);
 				    Expression e = parseAssignExp();
 				    check(TOKrparen);
 				    check(TOKsemicolon);
 				    s = new CompileDeclaration(loc(), e);
 				    break;
 				}
 
 				s = parseMixin();
 				break;
 
 			// begin CASE_BASIC_TYPES
 			case TOKwchar:
 			case TOKdchar:
 			case TOKbit:
 			case TOKbool:
 			case TOKchar:
 			case TOKint8:
 			case TOKuns8:
 			case TOKint16:
 			case TOKuns16:
 			case TOKint32:
 			case TOKuns32:
 			case TOKint64:
 			case TOKuns64:
 			case TOKfloat32:
 			case TOKfloat64:
 			case TOKfloat80:
 			case TOKimaginary32:
 			case TOKimaginary64:
 			case TOKimaginary80:
 			case TOKcomplex32:
 			case TOKcomplex64:
 			case TOKcomplex80:
 			case TOKvoid:
 				// end CASE_BASIC_TYPES
 			case TOKalias:
 			case TOKtypedef:
 			case TOKidentifier:
 			case TOKtypeof:
 			case TOKdot:
 				// Ldeclaration:
 				a = parseDeclarations(lastComments);
 				decldefs.addAll(a);
 				continue;
 
 			case TOKthis:
 				s = parseCtor();
 				break;
 
 			case TOKtilde:
 				s = parseDtor();
 				break;
 
 			case TOKinvariant:
 				if (apiLevel == D2) {
 				    Token t;
 					t = peek(token);
 					if (t.value == TOKlparen) {
 						if (peek(t).value == TOKrparen) {
 							// invariant() forms start of class invariant
 							s = parseInvariant();
 						} else {
 							// invariant(type)
 							// goto Ldeclaration;
 							a = parseDeclarations(lastComments);
 							decldefs.addAll(a);
 							continue;
 						}
 					} else {
 						stc = STCinvariant;
 						
 						Modifier modifier = newModifier();
 						
 						// goto Lstc;
 						nextToken();
 						
 						s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					}
 				} else {
 					s = parseInvariant();
 				}
 				break;
 
 			case TOKunittest:
 				s = parseUnitTest();
 				break;
 
 			case TOKnew:
 				s = parseNew();
 				break;
 
 			case TOKdelete:
 				s = parseDelete();
 				break;
 
 			case TOKeof:
 			case TOKrcurly:
 				return decldefs;
 
 			case TOKstatic:
 				nextToken();
 				
 				expect(afterStaticExpectations);
 				if (token.value == TOKthis) {
 					s = parseStaticCtor();
 				} else if (token.value == TOKtilde) {
 					s = parseStaticDtor();
 				} else if (token.value == TOKassert) {
 					s = parseStaticAssert();
 				} else if (token.value == TOKif) {
 					StaticIfCondition condition = parseStaticIfCondition();
 					a = parseBlock();
 					aelse = null;
 					
 					expect(elseExpectations);
 					if (token.value == TOKelse) {
 						nextToken();
 						aelse = parseBlock();
 					}					
 					s = new StaticIfDeclaration(condition, a, aelse);
 					attachLeadingComments = prevToken.value == TOKrcurly;
 					break;
 				} else if (token.value == TOKimport) {
 					s = parseImport(decldefs, true, prevToken.ptr, lastComments);
 				} else {
 					stc = STCstatic;
 					
 					// goto Lstc2;
 					
 					Modifier modifier = newModifier(prevToken);
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 				}
 				break;
 
 			case TOKconst:
 				if (apiLevel == D2 && peek(token).value == TOKlparen) {
 					// goto Ldeclaration
 					a = parseDeclarations(lastComments);
 					decldefs.addAll(a);
 					continue;
 				} else {
 					stc = STCconst;
 					
 					Modifier modifier = newModifier();
 					
 					// goto Lstc;
 					nextToken();
 					
 					s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					break;
 				}
 			case TOKfinal:
 			case TOKauto:
 			case TOKscope:
 			case TOKoverride:
 			case TOKabstract:
 			case TOKsynchronized:
 			case TOKdeprecated:
 				stc = STC.fromTOK(token.value);
 				
 				Modifier modifier = newModifier();
 				
 				// goto Lstc;
 				nextToken();
 				
 				s= parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 				break;
 
 			case TOKextern:
 				if (peek(token).value != TOKlparen) {
 					stc = STCextern;
 					
 					modifier = newModifier();
 					
 					// goto Lstc;
 					nextToken();
 					
 					s = parseDeclDefs_Lstc2(isSingle, modifier, stc, decldefs);
 					break;
 				}
 				{
 					LINK linksave = linkage;
 					LINK linkage = parseLinkage();
 					a = parseBlock();
 					s = new LinkDeclaration(linkage, a);
 					attachLeadingComments = prevToken.value == TOKrcurly;
 					linkage = linksave;
 					break;
 				}
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 			case TOKexport:
 				
 				prot = PROT.fromTOK(token.value);
 				
 				modifier = newModifier();
 				
 				// goto Lprot;
 				nextToken();
 				
 				boolean isColon = token.value == TOKcolon;
 				a = parseBlock(isSingle);
 				s = new ProtDeclaration(prot, a, modifier, isSingle[0], isColon);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 				
 			case TOKalign: {
 				long n;
 
 				s = null;
 				nextToken();
 				if (token.value == TOKlparen) {
 					nextToken();
 					if (token.value == TOKint32v)
 						n = token.intValue.longValue();
 					else {
 						parsingErrorInsertTokenAfter(prevToken, "Integer");
 						n = 1;
 					}
 					nextToken();
 					check(TOKrparen);
 				} else {
 					n = new Global().structalign; // default
 				}
 				a = parseBlock();
 				s = new AlignDeclaration((int) n, a);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 			}
 
 			case TOKpragma: {
 				IdentifierExp ident;
 				Expressions args = null;
 
 				nextToken();
 				check(TOKlparen);
 				
 				expect(pragmaArgsExpectations);
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					s = parseDeclDefs_Lerror();
 					continue;
 				}
 				ident = newIdentifierExp();
 				nextToken();
 				if (token.value == TOKcomma) {
 					args = parseArguments(); // pragma(identifier, args...)
 				} else {
 					check(TOKrparen); // pragma(identifier)
 				}
 
 				if (token.value == TOKsemicolon) {
 					a = null;
 				} else {
 					a = parseBlock();
 				}
 				
 				s = new PragmaDeclaration(loc(), ident, args, a);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 			}
 
 			case TOKdebug:
 				nextToken();
 				if (token.value == TOKassign) {
 					nextToken();
 					if (token.value == TOKidentifier || token.value == TOKint32v) {
 						s = newDebugAssignmentForCurrentToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 						s = null;
 					}
 					nextToken();
 					if (token.value != TOKsemicolon) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					nextToken();
 					break;
 				}
 
 				DebugCondition debugCondition = parseDebugCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}
 				
 				s = new ConditionalDeclaration(debugCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 
 			case TOKversion:
 				nextToken();
 				if (token.value == TOKassign) {
 					nextToken();
 					if (token.value == TOKidentifier || token.value == TOKint32v) {
 						s = newVersionAssignmentForCurrentToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 						s = null;
 					}
 					nextToken();
 					if (token.value != TOKsemicolon) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					nextToken();
 					break;
 				}
 				
 				VersionCondition versionCondition = parseVersionCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}
 				
 				s = new ConditionalDeclaration(versionCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 
 			case TOKiftype:		
 				IftypeCondition iftypeCondition = parseIftypeCondition();
 				
 				a = parseBlock();
 				aelse = null;
 				if (token.value == TOKelse) {
 					nextToken();
 					aelse = parseBlock();
 				}				
 
 				s = new ConditionalDeclaration(iftypeCondition, a, aelse);
 				attachLeadingComments = prevToken.value == TOKrcurly;
 				break;
 
 			case TOKsemicolon: // empty declaration
 				nextToken();
 				continue;
 
 			default:
 				parsingErrorDeleteToken(token);
 				nextToken();
 				continue;
 			}
 			if (s != null) {
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				if (s instanceof TemplateDeclaration 
 						&& ((TemplateDeclaration) s).wrapper) {
 					((TemplateDeclaration) s).members.get(0).setSourceRange(s.start, s.length);
 				}
 				
 				s.preComments = lastComments;
 				if (attachLeadingComments) {
 					attachLeadingComments(s);
 				}
 				decldefs.add(s);
 			}
 		} while (!once);
 		return decldefs;
 	}
 	
 	private Modifier newModifier() {
 		return new Modifier(token, linnum);
 	}
 	
 	private Modifier newModifier(Token token) {
 		return new Modifier(token, linnum);
 	}
 
 	private Dsymbol parseDeclDefs_Lerror() {
 		while (token.value != TOKsemicolon && token.value != TOKeof)
 		    nextToken();
 		nextToken();
 		return null;
 	}
 	
 	private Dsymbol parseDeclDefs_Lstc2(boolean[] isSingle, Modifier modifier, int stc, Dsymbols decldefs) {
 		Token firstToken = new Token(prevToken);
 		int start = firstToken.ptr;
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		modifiers.add(modifier);
 		
 		Dsymbol s = null;
 		
 		boolean repeat = true;
 		while(repeat) {
 			
 			expect(declDefs_Lstc2Expectations);
 			switch (token.value)
 			{
 			    case TOKconst:
 			    case TOKinvariant:
 			    	if (apiLevel == D2) {
 						// If followed by a (, it is not a storage class
 						if (peek(token).value == TOKlparen) {
 							repeat = false;
 						    break;
 						}
 						if (token.value == TOKconst) {
 						    stc |= STCconst;
 						} else {
 						    stc |= STCinvariant;
 						}
 						modifier = newModifier();
 				    	modifiers.add(modifier);
 				    	nextToken();
 				    	break;
 			    	} else {
 			    		if (token.value == TOKinvariant) {
 			    			repeat = false;
 			    			break;
 			    		}
 			    		// fall to next case
 			    	}
 			    case TOKfinal:
 			    case TOKauto:
 			    case TOKscope:
 			    case TOKoverride:
 			    case TOKabstract:
 			    case TOKsynchronized:
 			    case TOKdeprecated:
 			    	stc |= STC.fromTOK(token.value);
 			    	
 			    	modifier = newModifier();
 			    	modifiers.add(modifier);
 			    	nextToken();
 			    	break;
 			    default:
 			    	repeat = false;
 					break;
 			}
 		}
 		
 		VarDeclaration previous = null;
 
 		/* Look for auto initializers:
 		 *	storage_class identifier = initializer;
 		 */
 		if (token.value == TOKidentifier &&
 		    peek(token).value == TOKassign)
 		{
 			boolean first = true;
 			while(true) {
 			    IdentifierExp ident = newIdentifierExp();
 			    nextToken();
 			    nextToken();
 			    Initializer init = parseInitializer();
 			    VarDeclaration v = newVarDeclaration(loc(), null, ident, init);
 			    v.first = first;
 			    first = false;
 			    
 			    v.storage_class = stc;
 			    v.addModifiers(modifiers);
 			    
 			    attachLeadingComments(v);
 			    
 			    s = v;
 			    if (previous != null) {
 			    	previous.next = v;
 			    }
 			    previous = v;
 			    
 			    if (token.value == TOKsemicolon) {
 			    	v.setSourceRange(start, token.ptr + token.sourceLen - start);
 					nextToken();
 				} else if (token.value == TOKcomma) {
 					v.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					nextToken();
 					start = token.ptr;
 					if (token.value == TOKidentifier
 							&& peek(token).value == TOKassign) {
 						decldefs.add(s);
 						continue;
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "identifier");
 					}
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, TOKsemicolon
 							.toString());
 					v.setSourceRange(firstToken.ptr, prevToken.ptr + prevToken.sourceLen - firstToken.ptr);
 				}
 				break;
 			}
 		}
 		else
 		{  
 			boolean isColon = token.value == TOKcolon;
 			Dsymbols a = parseBlock(isSingle);
 			
 			if (isSingle[0] && a.size() == 0) {
 				parsingErrorDeleteToken(firstToken);
 				return null;
 			}
 			
 			s = new StorageClassDeclaration(stc, a, modifier, isSingle[0], isColon);
 			modifiers.remove(modifier);
 			s.modifiers = modifiers;
 		}
 		
 		return s;
 	}
 
 	private Dsymbols parseBlock() {
 		return parseBlock(null);
 	}
 	
 	private Dsymbols parseBlock(boolean[] isSingle) {
 		Dsymbols a = null;
 
 		switch (token.value) {
 		case TOKsemicolon:
 			parsingErrorInsertToComplete(prevToken, "Declaration", "Declaration");
 			nextToken();
 			break;
 
 		case TOKlcurly:
 			nextToken();
 			a = parseDeclDefs(false);
 			if (token.value != TOKrcurly) { /* { */
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			} else
 				nextToken();
 			break;
 
 		case TOKcolon:
 			nextToken();
 			// #if 1
 			// a = null;
 			// #else
 			a = parseDeclDefs(false);   // grab declarations up to closing curly
 										// bracket
 			// #endif
 			break;
 
 		default:
 			a = parseDeclDefs(true);
 			if (isSingle != null) {
 				isSingle[0] = true;
 			}
 			break;
 		}
 		return a;
 	}
 	
 	private StaticAssert parseStaticAssert() {
 		Expression exp;
 		Expression msg = null;
 
 		nextToken();
 		check(TOKlparen);
 		exp = parseAssignExp();
 		if (token.value == TOKcomma) {
 			nextToken();
 			msg = parseAssignExp();
 		}
 		check(TOKrparen);
 		check(TOKsemicolon);
 
 		return new StaticAssert(loc(), exp, msg);
 	}
 	
 	private LINK parseLinkage() {
 		LINK link = LINKdefault;
 		nextToken();
 		
 		if(token.value != TOKlparen) {
 			throw new IllegalStateException();
 		}
 		
 		nextToken();
 		
 		expect(externArgsExpectations);
 		if (token.value == TOKidentifier) {
 			char[] id = token.sourceString;
 			int start = token.ptr;
 			int length = token.sourceLen;
 			int lineNumber = token.lineNumber;
 			nextToken();
 			
 			// We can compare by equality because Lexer reuses the 
 			// char[] instances for this special tokens
 			if (id == Id.Windows) {
 				link = LINKwindows;
 			} else if (id == Id.Pascal) {
 				link = LINKpascal;
 			} else if (id == Id.D) {
 				link = LINKd;
 			} else if (id == Id.C) {
 				link = LINKc;
 				if (token.value == TOKplusplus) {
 					link = LINKcpp;
 					nextToken();
 				}
 			} else if (id == Id.System) {
 				link = LINKsystem;
 			} else {
 				error(IProblem.InvalidLinkageIdentifier, lineNumber, start, length);
 				link = LINKd;
 			}
 		} else {
 			link = LINKdefault; // default
 		}
 		check(TOKrparen);
 		return link;
 	}
 	
 	private DebugCondition parseDebugCondition() {
 		DebugCondition c;
 		long level = 1;
 		char[] id = null;
 		
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = token.sourceString;
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();			
 				c = newDebugCondition(module, loc(), level, id);			
 				check(TOKrparen);
 			} else if (token.value == TOKint32v) {
 				id = token.sourceString;
 				level = token.intValue.longValue();
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();
 				c = newDebugCondition(module, loc(), level, id);
 				check(TOKrparen);
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 				
 				c = newDebugCondition(module, loc(), level, id);
 				
 				// For improved syntax error recovery
 				if (token.value != TOKrparen) {
 					nextToken();
 				}
 				nextToken();
 			}
 		} else {
 			c = newDebugCondition(module, loc(), level, id);
 		}
 		
 		// Don't bring the "c = ..." statement here: it needs to be
 		// created after the identifier for completion parser
 		
 		c.startPosition = idTokenStart;
 		c.length = idTokenLength;
 		return c;
 	}
 	
 	private VersionCondition parseVersionCondition() {
 		VersionCondition c;
 		long level = 1;
 		char[] id = null;
 
 		int idTokenStart = -1;
 		int idTokenLength = -1;
 
 		if (token.value == TOKlparen) {
 			nextToken();
 			if (token.value == TOKidentifier) {
 				id = token.sourceString;
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();			
 				c = newVersionCondition(module, loc(), level, id);			
 				check(TOKrparen);
 			} else if (token.value == TOKint32v) {
 				id = token.sourceString;
 				level = token.intValue.longValue();
 				idTokenStart = token.ptr;
 				idTokenLength = token.sourceLen;
 				
 				nextToken();
 				c = newVersionCondition(module, loc(), level, id);
 				check(TOKrparen);		
 			} else {
 				/* Allow:
 				 *    version (unittest)
 				 * even though unittest is a keyword
 				 */
 				if (apiLevel == D2 &&
 						token.value == TOKunittest) {
 					id = token.value.charArrayValue;
 					idTokenStart = token.ptr;
 					idTokenLength = token.sourceLen;
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier or Integer");
 				}
 				
 				c = newVersionCondition(module, loc(), level, id);
 					
 				// For improved syntax error recovery
 				if (token.value != TOKrparen) {
 					nextToken();
 				}
 				nextToken();
 			}			
 		} else {
 			c = newVersionCondition(module, loc(), level, id);
 			
 			parsingErrorInsertToComplete(prevToken, "(condition)", "VersionDeclaration");
 		}
 		
 		// Don't bring the "c = ..." statement here: it needs to be
 		// created after the identifier for completion parser
 		
 		c.startPosition = idTokenStart;
 		c.length = idTokenLength;
 		return c;
 	}
 	
 	private StaticIfCondition parseStaticIfCondition() {
 		Expression exp;
 		nextToken();
 		if (token.value == TOKlparen) {
 			nextToken();
 			exp = parseAssignExp();
 			check(TOKrparen);
 		} else {
 			parsingErrorInsertToComplete(prevToken, "(expression)", "StaticIfDeclaration");
 			exp = null;
 		}
 		return new StaticIfCondition(loc(), exp);
 	}
 	
 	private IftypeCondition parseIftypeCondition() {
 		Type targ;
 		IdentifierExp[] ident = new IdentifierExp[1];
 		Type tspec = null;
 		TOK tok = TOKreserved;
 
 		int firstTokenStart = token.ptr;
 		int firstTokenLength = token.sourceLen;
 		int firstTokenLine = token.lineNumber;
 
 		nextToken();
 		if (token.value == TOKlparen) {
 			nextToken();
 			targ = parseBasicType();
 			targ = parseDeclarator(targ, ident);
 			if (token.value == TOKcolon || token.value == TOKequal) {
 				tok = token.value;
 				nextToken();
 				tspec = parseBasicType();
 				tspec = parseDeclarator(tspec, null);
 			}
 			check(TOKrparen);
 		} else {
 			parsingErrorInsertToComplete(prevToken, "(type identifier : specialization)", "IftypeDeclaration");
 			return new IftypeCondition(loc(), null, null, null, null);
 		}
 
 		error(
 				IProblem.IftypeDeprecated, firstTokenLine,
 				firstTokenStart, firstTokenLength);
 
 		return new IftypeCondition(loc(), targ, ident[0], tok, tspec);
 	}
 	
 	private CtorDeclaration parseCtor() {
 		int start = token.ptr;
 		
 		int[] varargs = new int[1];
 	    nextToken();
 	    Arguments arguments = parseParameters(varargs);
 	    CtorDeclaration f = new CtorDeclaration(loc(), arguments, varargs[0]);
 	    f.thisStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 	
 	private DtorDeclaration parseDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		int thisStart = token.ptr;
 		
 		expect(thisExpectations);
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		DtorDeclaration f = new DtorDeclaration(loc());
 		f.thisStart = thisStart;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 	
 	private StaticCtorDeclaration parseStaticCtor() {
 		int start = token.ptr;
 		
 		nextToken();
 	    check(TOKlparen);
 	    check(TOKrparen);
 
 	    StaticCtorDeclaration f = new StaticCtorDeclaration(loc());
 	    f.thisStart = start;
 	    
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 	
 	private StaticDtorDeclaration parseStaticDtor() {
 		int start = token.ptr;
 		nextToken();
 		
 		int thisStart = token.ptr;
 		
 		expect(thisExpectations);
 		check(TOKthis);
 	    check(TOKlparen);
 	    check(TOKrparen);
 		
 		StaticDtorDeclaration f = new StaticDtorDeclaration(loc());
 		f.thisStart = thisStart;
 		f.setSourceRange(start, 0);
 	    parseContracts(f);
 	    return f;
 	}
 	
 	private InvariantDeclaration parseInvariant() {
 		int start = token.ptr;
 	    nextToken();
 	    
 	    if (token.value == TOKlparen)	// optional ()
 	    {
 			nextToken();
 			check(TOKrparen);
 	    }
 
 	    InvariantDeclaration invariant = new InvariantDeclaration(loc());
 	    invariant.invariantStart = start;
 	    invariant.setFbody(dietParseStatement(invariant));
 	    invariant.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return invariant;
 	}
 	
 	private UnitTestDeclaration parseUnitTest() {
 		int start = token.ptr;
 		nextToken();
 		
 		UnitTestDeclaration unitTest = new UnitTestDeclaration(loc());
 		unitTest.setFbody(dietParseStatement(unitTest));
 	    unitTest.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return unitTest;
 	}
 	
 	private NewDeclaration parseNew() {
 		int start = token.ptr;
 		
 		nextToken();
 		int[] varargs = new int[1];
 		Arguments arguments = parseParameters(varargs);
 		
 		NewDeclaration f = new NewDeclaration(loc(), arguments, varargs[0]);
 		f.newStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 	
 	private FuncDeclaration parseDelete() {
 		int start = token.ptr;
 		int startLine = token.lineNumber;
 		
 		IdentifierExp name = newIdentifierExp();
 		
 		nextToken();
 		int[] varargs = new int[1];
 		Arguments arguments = parseParameters(varargs);
 		
 		if (varargs[0] != 0) {
 	    	error(
 	    			IProblem.VariadicNotAllowedInDelete, 
 	    			startLine, name);
 	    }
 		
 		DeleteDeclaration f = new DeleteDeclaration(loc(), arguments);
 		f.deleteStart = start;
 	    parseContracts(f);
 	    f.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 	    return f;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Arguments parseParameters(int[] pvarargs) {
 		if (apiLevel < D2) {
 			return parseParametersD1(pvarargs);
 		} else {
 			return parseParametersD2(pvarargs);
 		}
 	}
 	
 	private Arguments parseParametersD1(int[] pvarargs) {
 		Arguments arguments = new Arguments();
 		int varargs = 0;
 		boolean hasdefault = false;
 
 		check(TOKlparen);
 		while (true) {
 			Type tb;
 			IdentifierExp ai;
 			Type at;
 			Argument a;
 			int storageClass;
 			Expression ae;
 			List<Modifier> modifiers = new ArrayList<Modifier>(1);
 			
 			int firstTokenStart = token.ptr;
 			
 			ai = null;
 			storageClass = STCin;
 			
 			if (token.value == TOKrparen) {
 				break;
 			} else if (token.value == TOKdotdotdot) {
 				varargs = 1;
 				nextToken();
 				break;
 			} else {
 				int inoutTokenStart = token.ptr;
 				int inoutTokenLength = token.sourceLen;
 				int inoutTokenLine = token.lineNumber;
 				
 				expect(parameters1Expectations);
 				switch(token.value) {
 					case TOKin:
 						storageClass = STCin;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKout:
 						storageClass = STCout;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKinout:
 						storageClass = STCref;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKref:
 						storageClass = STCref;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 					case TOKlazy:
 						storageClass = STClazy;
 						modifiers.add(newModifier());
 						nextToken();
 						break;
 				}
 				
 				tb = parseBasicType();
 
 				IdentifierExp[] pointer2_ai = { ai };
 				at = parseDeclarator(tb, pointer2_ai);
 				ai = pointer2_ai[0];
 
 				ae = null;
 				if (token.value == TOKassign) // = defaultArg
 				{
 					nextToken();
 					ae = parseAssignExp();
 					hasdefault = true;
 				} else {
 					if (hasdefault) {
 						parsingErrorInsertTokenAfter(prevToken, "default argument");
 					}
 				}
 				if (token.value == TOKdotdotdot) { 
 					/*
 					 * This is: at ai ...
 					 */
 					if ((storageClass & (STCout | STCref)) != 0) {
 						error(IProblem.VariadicArgumentCannotBeOutOrRef, inoutTokenLine, inoutTokenStart, inoutTokenLength);
 					}
 					varargs = 2;
 					
 					a = newArgument(storageClass, at, ai, ae);
 					a.modifiers = modifiers;
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 					nextToken();
 					break;
 				}
 				
 				if (at != null || ai != null || ae != null) {
 					a = newArgument(storageClass, at, ai, ae);
 					a.modifiers = modifiers;
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 				} else {
 					break;
 				}
 			}
 		}
 		check(TOKrparen);
 		pvarargs[0] = varargs;
 		return arguments;
 	}
 	
 	private Arguments parseParametersD2(int[] pvarargs) {
 		Arguments arguments = new Arguments();
 		int varargs = 0;
 		boolean hasdefault = false;
 
 		check(TOKlparen);
 	loopWhile:
 		while (true) {
 			IdentifierExp ai;
 			Type at;
 			Argument a;
 			int storageClass;
 			int stc;
 			Expression ae;
 			
 			int firstTokenStart = token.ptr;
 			
 			ai = null;
 			storageClass = 0;
 			stc = 0;
 			
 			List<Modifier> modifiers = new ArrayList<Modifier>(2);
 			
 		loopFor:
 			for(; true; nextToken()) {
 				int inoutTokenStart = token.ptr;
 				int inoutTokenLength = token.sourceLen;
 				int inoutTokenLine = token.lineNumber;
 				
 				expect(parameters2Expectations);
 				switch(token.value) {
 				case TOKrparen:
 					break loopWhile;
 				case TOKdotdotdot:
 					varargs = 1;
 					nextToken();
 					break loopWhile;
 				case TOKconst:
 				    if (peek(token).value == TOKlparen) {
 						// goto Ldefault;
 				    	break;
 				    } else {
 					    stc = STCconst;
 					    modifiers.add(newModifier());
 					    // goto L2;
 					    storageClass = parseParametersD2_L2(storageClass, stc);
 					    continue loopFor;
 				    }
 				case TOKinvariant:
 				    if (peek(token).value == TOKlparen) {
 				    	// goto Ldefault;
 				    	break;
 				    } else {
 					    stc = STCinvariant;
 					    modifiers.add(newModifier());
 					    // goto L2;
 					    storageClass = parseParametersD2_L2(storageClass, stc);
 					    continue loopFor;
 				    }
 				case TOKin:
 					stc = STCin;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKout:
 					stc = STCout;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKinout:
 					stc = STCref;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKref:
 					stc = STCref;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKlazy:
 					stc = STClazy;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKscope:
 					stc = STCscope;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKfinal:
 					stc = STCfinal;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 				case TOKstatic:
 					stc = STCstatic;
 					modifiers.add(newModifier());
 					// goto L2;
 					storageClass = parseParametersD2_L2(storageClass, stc);
 					continue loopFor;
 					
 				default:
 					// Ldefault:
 					break;
 				}
 				
 				stc = storageClass
 						& (STCin | STCout | STCref | STClazy);
 				if ((stc & (stc - 1)) != 0) { // if stc is not a power of 2
 					error(IProblem.IncompatibleParameterStorageClass, token);
 				}
 				if ((storageClass & (STCfinal | STCout)) == (STCfinal | STCout)) {
 					error(IProblem.OutCannotBeFinal, token);
 				}
 				if ((storageClass & STCscope) != 0
 						&& (storageClass & (STCref | STCout)) != 0) {
 					error(IProblem.ScopeCannotBeRefOrOut, token);
 				}
 				
 				IdentifierExp[] pointer2_ai = { ai };
 				at = parseType(pointer2_ai);
 				ai = pointer2_ai[0];
 				
 				ae = null;
 				if (token.value == TOKassign) // = defaultArg
 				{
 					nextToken();
 					ae = parseAssignExp();
 					hasdefault = true;
 				} else {
 					if (hasdefault) {
 						parsingErrorInsertTokenAfter(prevToken, "default argument");
 					}
 				}
 				if (token.value == TOKdotdotdot) { 
 					/*
 					 * This is: at ai ...
 					 */
 					if ((storageClass & (STCout | STCref)) != 0) {
 						error(IProblem.VariadicArgumentCannotBeOutOrRef, inoutTokenLine, inoutTokenStart, inoutTokenLength);
 					}
 					varargs = 2;
 					
 					a = newArgument(storageClass, at, ai, ae);
 					a.modifiers = modifiers;
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 					nextToken();
 					break;
 				}
 				
 				if (at != null || ai != null || ae != null) {
 					a = newArgument(storageClass, at, ai, ae);
 					a.modifiers = modifiers;
 					a.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 					// goto L1;
 					continue loopWhile;
 				}
 				break;
 			}
 			break;
 			
 			// L1: ;
 		}
 		check(TOKrparen);
 		pvarargs[0] = varargs;
 		return arguments;
 	}	
 
 	private int parseParametersD2_L2(int storageClass, int stc) {
 		if ((storageClass & stc) != 0
 				|| ((storageClass & STCin) != 0 && (stc & (STCfinal
 						| STCconst | STCscope)) != 0)
 				|| ((stc & STCin) != 0 && (storageClass & (STCfinal
 						| STCconst | STCscope)) != 0)) {			
 			error(IProblem.RedundantStorageClass, token);
 		}
 		storageClass |= stc;
 		{
 			int u = storageClass & (STCconst | STCinvariant);
 			if ((u & (u - 1)) != 0) {
 				error(IProblem.ConflictingStorageClass, token);
 			}
 		}
 		return storageClass;
 	}
 
 	private EnumDeclaration parseEnum() {
 		int enumTokenStart = token.ptr;
 		int enumTokenLength = token.sourceLen;
 		int enumTokenLineNumber = token.lineNumber;
 
 		IdentifierExp id;
 		Type t;
 		
 		nextToken();
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 
 		if (token.value == TOKcolon) {
 			nextToken();
 			t = parseBasicType();
 		} else {
 			t = null;
 		}
 		
 		EnumDeclaration e = new EnumDeclaration(loc(), id, t);
 		
 		if (token.value == TOKsemicolon && id != null) {
 			e.setSourceRange(enumTokenStart, token.ptr + token.sourceLen - enumTokenStart);
 			nextToken();			  
 		} else if (token.value == TOKlcurly) {
 			e.members = new Dsymbols();
 			nextToken();
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					error(IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 					break;
 				}
 				
 				if (token.value == TOKidentifier) {
 					Expression value;
 					IdentifierExp ident = newIdentifierExp();
 					value = null;
 					nextToken();
 					if (token.value == TOKassign) {
 						nextToken();
 						value = parseAssignExp();
 					}
 					
 					List<Comment> lastComments = getLastComments();
 					
 					EnumMember em = new EnumMember(loc(), ident, value);
 					e.addMember(em);
 					
 					em.preComments = lastComments;
 					
 					if (token.value == TOKrcurly) {
 						;
 					} else {
 						check(TOKcomma);
 					}
 					
 					attachLeadingComments(em);					
 				} else {
 					parsingErrorInsertToComplete(prevToken, "EnumMember", "EnumDeclaration");
 					nextToken();
 				}
 			}
 			e.setSourceRange(enumTokenStart, token.ptr + token.sourceLen - enumTokenStart);
 			
 			// Discard any previous comments
 			if (comments != null) {
 				lastCommentRead = comments.size();
 			}
 			
 			nextToken();
 		} else {
 			error(IProblem.EnumDeclarationIsInvalid, enumTokenLineNumber, enumTokenStart, enumTokenLength);
 			return null;
 		}
 		
 		return e;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Dsymbol parseAggregate() {
 		AggregateDeclaration a = null;
 		IdentifierExp id;
 		TemplateParameters tpl = null;
 		BaseClasses baseClasses = null;
 		int anon = 0;
 		boolean[] malformed = { false };
 		
 		Token firstToken = new Token(token);
 		
 		nextToken();
 		if (token.value != TOKidentifier) {
 			// Change from DMD
 			id = null;
 		} else {
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKlparen) {				
 				// Gather template parameter list
 				tpl = parseTemplateParameterList(malformed);
 			}
 		}
 
 		// Don't need to expect(...), it was called previously
 		switch (firstToken.value) {
 		case TOKclass:
 		case TOKinterface:
 			if (id == null) {
 				parsingErrorInsertTokenAfter(firstToken, "Identifier");
 			}
 
 			// Collect base class(es)
 			if (token.value == TOKcolon) {
 				nextToken();				
 				baseClasses = parseBaseClasses();
 				
 				if (token.value != TOKlcurly) {
 					parsingErrorInsertToComplete(prevToken, "AggregateBody", "AggregateDeclaration");
 				}
 			}
 			
 			if (firstToken.value == TOKclass) {
 				a = newClassDeclaration(loc(), id, baseClasses);
 			} else {
 				a = newInterfaceDeclaration(loc(), id, baseClasses);
 			}
 			break;			
 		case TOKstruct:
 			if (id != null) {
 				a = newStructDeclaration(loc(), id);
 			} else {
 				anon = 2;
 			}
 			break;
 		case TOKunion:
 			if (id != null) {
 				a = newUnionDeclaration(loc(), id);
 			} else {
 				anon = 1;
 			}
 			break;
 
 		default:
 			throw new IllegalStateException("Can't happen");
 		}
 		
 		if (token.value == TOKsemicolon) {
 			nextToken();
 		} else if (token.value == TOKlcurly) {
 			nextToken();
 			Dsymbols decl = parseDeclDefs(false);
 			if (token.value != TOKrcurly) {
 				parsingErrorInsertTokenAfter(prevToken, "}");
 			}
 			if (anon != 0) {
 				nextToken();
 			    return new AnonDeclaration(loc(), anon == 1, decl);
 			}
 			a.members = decl;
 			a.sourceMembers = new Dsymbols(decl);
 			
 			nextToken();
 		} else {
 			if (id == null) {
 				// A single "class" makes no declaration
 				if (firstToken.value == TOKstruct || firstToken.value == TOKunion) {
 					// Signal the creation of the struct or union, anyway
 					if (firstToken.value == TOKstruct) {
 						newStructDeclaration(loc, null);
 					} else {
 						newUnionDeclaration(loc, null);
 					}
 					
 					String word = toWord(firstToken.value.toString());
 					parsingErrorInsertToComplete(firstToken,  word + "Body", word + "Declaration");
 				}
 				a = null;
 			} else {
 				// We've got at least "class Identifier", make a declaration out of it
 				String word = toWord(firstToken.value.toString());
 				parsingErrorInsertToComplete(prevToken,  word + "Body", word + "Declaration");
 				switch(firstToken.value) {
 				case TOKclass:
 					a = newClassDeclaration(loc(), id, baseClasses);
 					break;
 				case TOKinterface:
 					a = newInterfaceDeclaration(loc(), id, baseClasses);
 					break;
 				case TOKstruct:
 					a = newStructDeclaration(loc(), id);
 					break;
 				case TOKunion:
 					a = newUnionDeclaration(loc(), id);
 					break;
 				}
 			}		
 		}
 		
 		if (malformed[0]) {
 			setMalformed(a);
 		}
 		
 		if (tpl != null)
 		{	
 			Dsymbols decldefs;
 			TemplateDeclaration tempdecl;
 	
 			// Wrap a template around the aggregate declaration
 			decldefs = new Dsymbols();
 			decldefs.add(a);
 			tempdecl = new TemplateDeclaration(loc(), id, tpl, decldefs);
 			tempdecl.setSourceRange(a.start, a.length);
 			tempdecl.wrapper = true;
 			a.templated = true;
 			return tempdecl;
 	    }
 
 		return a;
 	}
 
 	private BaseClasses parseBaseClasses() {
 		PROT protection = PROT.PROTpublic;
 		BaseClasses baseclasses = new BaseClasses();
 		Modifier modifier = null;
 		
 		int start = token.ptr;
 
 		// These modifiers currently have no effect, so we don't expect them
 		// expect(TOKprivate, TOKpackage, TOKprotected, TOKpublic);
 		for (; true; nextToken()) {			
 			switch (token.value) {
 			case TOKidentifier:
 				break;
 			case TOKprivate:
 			case TOKpackage:
 			case TOKprotected:
 			case TOKpublic:
 				protection = PROT.fromTOK(token.value);
 				
 				modifier = newModifier();
 				continue;
 			default:
 				parsingErrorInsertTokenAfter(prevToken, "Type");
 				return null;
 			}
 			BaseClass b = new BaseClass(parseBasicType(), modifier, protection);
 			baseclasses.add(b);
 			
 			b.start = start;
 			b.length = prevToken.ptr + prevToken.sourceLen - b.start;
 			
 			if (token.value != TOKcomma) {
 				break;
 			} else {
 				start = peek(token).ptr;
 			}
 		}
 		return baseclasses;
 	}
 	
 	private TemplateDeclaration parseTemplateDeclaration() {
 		IdentifierExp id;
 		TemplateParameters tpl;
 		Dsymbols decldefs;
 		boolean[] malformed = { false };
 
 		int start = token.ptr;
 		nextToken();
 		if (token.value != TOKidentifier) {
 			parsingErrorInsertTokenAfter(prevToken, "Identifier");
 			return null;
 			// goto Lerr;
 		}
 		id = newIdentifierExp();
 		nextToken();
 		tpl = parseTemplateParameterList(malformed);
 		if (tpl == null)
 			// goto Lerr;
 			return null;
 
 		if (token.value != TOKlcurly) {
 			parsingErrorInsertToComplete(prevToken, "TemplateBodyDeclaration",
 					"TemplateDeclaration");
 			// goto Lerr;
 			return null;
 		} else {
 			nextToken();
 			decldefs = parseDeclDefs(false);
 			if (token.value != TOKrcurly) {
 				parsingErrorInsertToComplete(prevToken,
 						"TemplateBodyDeclaration", "TemplateDeclaration");
 				// goto Lerr;
 				return null;
 			}
 			nextToken();
 		}
 
 		TemplateDeclaration tempdecl = new TemplateDeclaration(loc(), id, tpl, decldefs);
 		tempdecl.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		
 		if (malformed[0]) {
 			setMalformed(tempdecl);
 		}
 		
 		return tempdecl;
 
 		// Lerr:
 		// return NULL;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private TemplateParameters parseTemplateParameterList(boolean[] malformed) {
 		// Change from DMD: empty template parameter list is returned in case
 		// of a syntax error
 		
 		TemplateParameters tpl = new TemplateParameters();
 
 		if (token.value != TOKlparen) {
 			parsingErrorInsertToComplete(prevToken, "Parenthesized TemplateParameterList", "TemplateDeclaration");
 			
 			// goto Lerr;
 			malformed[0] = true;
 			return tpl;
 		}
 		
 		nextToken();
 		
 		expect(aliasExpectations);
 
 		// Get array of TemplateParameters
 		if (token.value != TOKrparen) {
 			
 			boolean isvariadic = false;
 			TemplateParameter tp = null;
 			
 			while (true) {
 				IdentifierExp tp_ident = null;
 				Type tp_spectype = null;
 				Type tp_valtype = null;
 				Type tp_defaulttype = null;
 				Expression tp_specvalue = null;
 				Expression tp_defaultvalue = null;
 				Token t;
 
 				int firstTokenStart = token.ptr;
 
 				// Get TemplateParameter
 
 				// First, look ahead to see if it is a TypeParameter or a
 				// ValueParameter
 				
 				t = peek(token);
 				if (token.value == TOKalias) { // AliasParameter
 					nextToken();
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_spectype = parseBasicType();
 							tp_spectype = parseDeclarator(tp_spectype, null);	
 						} else {
 							tp_spectype = parseType();
 						}
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_defaulttype = parseBasicType();
 							tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 						} else {
 							tp_defaulttype = parseType();
 						}
 					}
 					
 					tp = new TemplateAliasParameter(loc(), tp_ident, tp_spectype, tp_defaulttype);
 				} else if (t.value == TOKcolon || t.value == TOKassign
 						|| t.value == TOKcomma || t.value == TOKrparen) { // TypeParameter
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					tp_ident = newIdentifierExp();
 					nextToken();
 					if (token.value == TOKcolon) // : Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_spectype = parseBasicType();
 							tp_spectype = parseDeclarator(tp_spectype, null);
 						} else {
 							tp_spectype = parseType();
 						}
 					}
 					if (token.value == TOKassign) // = Type
 					{
 						nextToken();
 						if (apiLevel < D2) {
 							tp_defaulttype = parseBasicType();
 							tp_defaulttype = parseDeclarator(tp_defaulttype, null);
 						} else {
 							tp_defaulttype = parseType();
 						}
 					}
 					
 					tp = new TemplateTypeParameter(loc(), tp_ident, tp_spectype, tp_defaulttype);
 				}
 			    else if (token.value == TOKidentifier && t.value == TOKdotdotdot)
 			    {	// ident...
 			    	if (isvariadic) {
 			    		error(IProblem.VariadicTemplateParameterMustBeTheLastOne, 
 			    				t.lineNumber, token.ptr, 
 				    			t.ptr + t.sourceLen - token.ptr);
 			    	}
 					isvariadic = true;
 					tp_ident = newIdentifierExp();
 					nextToken();
 					nextToken();
 					
 					tp = new TemplateTupleParameter(loc(), tp_ident);
 				} else { 
 					// ValueParameter
 					if (apiLevel < D2) {
 						tp_valtype = parseBasicType();
 						IdentifierExp[] pointer2_tp_ident = new IdentifierExp[] { tp_ident };
 						tp_valtype = parseDeclarator(tp_valtype, pointer2_tp_ident);
 						tp_ident = pointer2_tp_ident[0];
 					} else {
 						IdentifierExp[] pointer2_tp_ident = new IdentifierExp[] { tp_ident };
 						tp_valtype = parseType(pointer2_tp_ident);
 						tp_ident = pointer2_tp_ident[0];
 					}
 					
 					if (tp_ident == null) {
 						error(IProblem.NoIdentifierForTemplateValueParameter, t.lineNumber, t.ptr, t.sourceLen);
 						// TODO update to DMD 1.028 behaviour? What's the point?
 						// goto Lerr;
 						malformed[0] = true;
 						return tpl;
 					}
 					if (token.value == TOKcolon) // : CondExpression
 					{
 						nextToken();
 						tp_specvalue = parseCondExp();
 					}
 					if (token.value == TOKassign) // = CondExpression
 					{
 						nextToken();
 						tp_defaultvalue = parseCondExp();
 					}
 					
 					tp = new TemplateValueParameter(loc(), tp_ident, tp_valtype, tp_specvalue, tp_defaultvalue);
 				}
 				tp.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 				
 				tpl.add(tp);
 				if (token.value != TOKcomma)
 					break;
 				nextToken();
 			}
 		}
 		check(TOKrparen);
 		return tpl;
 
 	// Lerr:
 	// return NULL;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public TemplateMixin parseMixin() {
 		TemplateMixin tm;
 		IdentifierExp id = null;
 		Type tqual;
 		Objects tiargs;
 		Identifiers idents;
 
 		nextToken();
 		
 		expect(typeofExpectations);
 		
 		int typeStart = token.ptr;
 		tqual = null;
 		if (token.value == TOKdot) {
 			id = new IdentifierExp(loc(), Id.empty); // Id::empty
 		} else {
 			if (token.value == TOKtypeof) {
 				Expression exp;
 
 				nextToken();
 				check(TOKlparen);
 				exp = parseExpression();
 				check(TOKrparen);
 				tqual = new TypeTypeof(loc(), exp);
 				tqual.setSourceRange(typeStart, prevToken.ptr + prevToken.sourceLen - typeStart);
 				
 				check(TOKdot);				
 			}
 			if (token.value != TOKidentifier) {
 				parsingErrorDeleteToken(prevToken);
 				// goto Lerr;
 				return null;
 			}
 			id = newIdentifierExp();
 			nextToken();
 		}
 		
 		idents = new Identifiers();
 		while (true) {
 			int thisStart = prevToken.ptr;
 			
 			tiargs = null;
 			if (token.value == TOKnot) {
 				nextToken();
 				tiargs = parseTemplateArgumentList();
 			}
 			
 			if (token.value != TOKdot)
 				break;
 			
 			if (tiargs != null) {
 				TemplateInstance tempinst = new TemplateInstance(loc(), id);
 			    tempinst.tiargs(tiargs);
 			    tempinst.start = thisStart;
 			    tempinst.length = prevToken.ptr + prevToken.sourceLen - thisStart;
 			    id = new TemplateInstanceWrapper(loc(), tempinst); // "(PIdentifier *)tempinst;" cant work in Java 
 			    tiargs = null;
 			}
 			idents.add(id);
 
 			nextToken();
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				break;
 			}
 			id = newIdentifierExp();
 			nextToken();
 		}
 		idents.add(id);
 		
 		int typeLength = prevToken.ptr + prevToken.sourceLen - typeStart;
 
 		if (token.value == TOKidentifier) {
 			id = newIdentifierExp();
 			nextToken();
 		} else {
 			id = null;
 		}
 		
 		tm = new TemplateMixin(loc(), id, tqual, idents, tiargs);
 		tm.setTypeSourceRange(typeStart, typeLength);
 
 		//tm = new MixinDeclaration(ast, id, tqual, idents, tiargs);
 		if (token.value != TOKsemicolon) {
 			parsingErrorInsertTokenAfter(prevToken, ";");
 		} else {
 			nextToken();
 		}
 
 		return tm;
 	    
 	    // Lerr:
 	    // return NULL;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Objects parseTemplateArgumentList() {
 	    if (token.value != TOKlparen)
 	    {   
 	    	parsingErrorInsertToComplete(prevToken, "!(TemplateArgumentList)", "TemplateType");
 	    	return new Objects();
 	    }
 	    return parseTemplateArgumentList2();
 	}
 	
 	private Objects parseTemplateArgumentList2() {
 		Objects tiargs = new Objects();
 		nextToken();
 
 		// Get TemplateArgumentList
 		if (token.value != TOKrparen) {
 			while (true) {
 				// See if it is an Expression or a Type
 				if (isDeclaration(token, 0, TOKreserved, null)) { // Type
 					Type ta;
 
 					// Get TemplateArgument
 					if (apiLevel < D2) {
 						ta = parseBasicType();
 						ta = parseDeclarator(ta, null);
 					} else {
 						ta = parseType();
 					}
 					tiargs.add(ta);
 				} else { // Expression
 					Expression ea;
 
 					ea = parseAssignExp();
 					tiargs.add(ea);
 				}
 				if (token.value != TOKcomma)
 					break;
 				nextToken();
 			}
 		}
 		check(TOKrparen);
 		return tiargs;
 	}
 	
 	private Import parseImport(Dsymbols decldefs, boolean isstatic, int start, List<Comment> lastComments) {
 		Import s = null;
 		IdentifierExp id = null;
 		IdentifierExp aliasid = null;
 		Identifiers a = null;
 		int sStart = 0;
 		
 		boolean repeat = true;
 		while (repeat) {
 			repeat = false;
 		
 		// ---	
 		the_do:
 		// ---
 			do {
 				// L1:
 				nextToken();
 				
 				if (aliasid == null) {
 					sStart = token.ptr;
 				}
 
 				if (token.value != TOKidentifier) {
 					
 					// Issue a creation of an empty import declaration
 					newImport(loc(), null, null, aliasid, isstatic);
 					
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					break;
 				}
 				
 				a = null;
 				id = newIdentifierExp();
 
 				nextToken();
 
 				if (aliasid == null && token.value == TOKassign) {
 					aliasid = id;
 					// goto L1;
 					repeat = true;
 					break the_do;
 				}
 
 				while (token.value == TOKdot) {
 					if (a == null) {
 						a = new Identifiers();
 					}
 					a.add(id);
 					nextToken();
 					if (token.value != TOKidentifier) {
 						// Issue a creation of an empty import declaration
 						newImport(loc(), a, null, aliasid, isstatic);
 						
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 						break;
 					}
 					id = newIdentifierExp();
 					nextToken();
 				}
 
 				Import prev = s;
 				s = newImport(loc(), a, id, aliasid, isstatic);
 				s.preComments = lastComments;
 				s.first = prev == null;
 				decldefs.add(s);
 				if (prev == null) {
 					s.firstStart = start;
 				} else {
 					prev.next = s;
 				}
 
 				/*
 				 * Look for : alias=name, alias=name; syntax.
 				 */
 				if (token.value == TOKcolon) {
 					do {
 						IdentifierExp name = null;
 						IdentifierExp alias = null;
 						
 						nextToken();
 
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							break;
 						}
 						
 						alias = newIdentifierExp();
 						nextToken();						
 						if (token.value == TOKassign) {
 							nextToken();
 							if (token.value != TOKidentifier) {
 								parsingErrorInsertTokenAfter(prevToken, "Identifier");
 								break;
 							}
 							name = newIdentifierExp();
 							nextToken();
 						} else {
 							name = alias;
 							alias = null;
 						}
 						s.addAlias(name, alias);
 						s.setSourceRange(sStart, prevToken.ptr + prevToken.sourceLen - sStart);
 					} while (token.value == TOKcomma);
 					break; // no comma-separated imports of this form
 				} else {
 					s.setSourceRange(sStart, prevToken.ptr + prevToken.sourceLen - sStart);
 				}
 
 				aliasid = null;
 			} while (token.value == TOKcomma);
 		}
 		
 		if (s != null) {
 			s.lastLength = token.ptr + token.sourceLen - s.start;
 		}
 
 		check(TOKsemicolon);
 
 		return null;
 	}
 	
 	public Type parseType() {
 		return parseType(null, null);
 	}
 	
 	private Type parseType(IdentifierExp[] pident) {
 		return parseType(pident, null);
 	}
 	
 	private Type parseType(IdentifierExp[] pident, TemplateParameters[] tpl) {   
 		Type t;
 
 		if (token.value == TOKconst && peek(token).value != TOKlparen) {
 			int start = token.ptr;
 			nextToken();
 			/* const type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeConst(start, prevToken.ptr + prevToken.sourceLen - start);
 			return t;
 		} else if (token.value == TOKinvariant
 				&& peek(token).value != TOKlparen) {
 			int start = token.ptr;
 			nextToken();
 			/* invariant type
 			 */
 			t = parseType(pident, tpl);
 			t = t.makeInvariant(start, prevToken.ptr + prevToken.sourceLen - start);
 			return t;
 		} else {
 			t = parseBasicType();
 		}
 		t = parseDeclarator(t, pident, tpl, new int[1]);
 		return t;
 	}
 	
 	private Type parseBasicType() {
 		Type t = null;
 		IdentifierExp id = null;
 		TypeQualified tid = null;
 		TemplateInstance tempinst = null;
 		
 		expect(basicTypeExpectations);
 		
 		switch (token.value) {
 		// CASE_BASIC_TYPES_X(t):
 		case TOKvoid: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64: case TOKfloat80:
 		case TOKimaginary32: case TOKimaginary64: case TOKimaginary80:
 		case TOKcomplex32: case TOKcomplex64: case TOKcomplex80:
 		case TOKbit: case TOKbool: 
 		case TOKchar: case TOKwchar: case TOKdchar: 
 			t = newTypeBasicForCurrentToken(); 
 			t.setSourceRange(token.ptr, token.sourceLen); 
 			nextToken(); 
 			break;
 
 		case TOKidentifier:
 			id = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKnot) {
 				nextToken();
 				tempinst = new TemplateInstance(loc(), id);
 				tempinst.tiargs(parseTemplateArgumentList());
 				tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 				tid = new TypeInstance(loc(), tempinst);
 				tid.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 				// goto Lident2;
 				{
 				Type[] p_t = { t };
 				IdentifierExp[] p_id = { id };
 				TypeQualified[] p_tid = { tid };
 				TemplateInstance[] p_tempinst = { tempinst };
 				
 				parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, id.start);
 				t = p_t[0];
 				id = p_id[0];
 				tid = p_tid[0];
 				tempinst = p_tempinst[0];
 				}
 				break;
 
 			}
 			// Lident:
 			tid = newTypeIdentifier(loc(), id);
 			// Lident2:
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, id.start);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 
 		case TOKdot:
 			id = new IdentifierExp(loc(), Id.empty);
 			// goto Lident;
 			tid = newTypeIdentifier(loc(), id);
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, token.ptr);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 
 		case TOKtypeof: {
 			int start = token.ptr;
 			Expression exp;
 
 			nextToken();
 			check(TOKlparen);
 			exp = parseExpression();
 			check(TOKrparen);
 			
 			tid = new TypeTypeof(loc(), exp);
 			tid.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			((TypeTypeof) tid).setTypeofSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			// goto Lident2;
 			{
 			Type[] p_t = { t };
 			IdentifierExp[] p_id = { id };
 			TypeQualified[] p_tid = { tid };
 			TemplateInstance[] p_tempinst = { tempinst };
 			
 			parseBasicType_Lident2(p_t, p_id, p_tid, p_tempinst, start);
 			t = p_t[0];
 			id = p_id[0];
 			tid = p_tid[0];
 			tempinst = p_tempinst[0];
 			}
 			break;
 		}
 			
 		case TOKconst: {
 		    // const(type)
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    t = parseType();
 		    check(TOKrparen);
 		    t = t.makeConst(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 		case TOKinvariant: {
 		    // invariant(type)
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    t = parseType();
 		    check(TOKrparen);
 		    t = t.makeInvariant(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Type");
 			break;
 		}
 		return t;
 	}
 
 	private void parseBasicType_Lident2(Type[] t, IdentifierExp[] id, TypeQualified[] tid, TemplateInstance[] tempinst, int start) {
 		while (token.value == TOKdot) {
 			nextToken();
 			int tempinstStart = token.ptr;
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				break;
 			}
 			id[0] = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKnot) {
 				nextToken();
 				tempinst[0] = new TemplateInstance(loc(), id[0]);
 				tempinst[0].tiargs(parseTemplateArgumentList());
 				tempinst[0].setSourceRange(tempinstStart, prevToken.ptr + prevToken.sourceLen - tempinstStart);
 				tid[0].addIdent(new TemplateInstanceWrapper(loc(), tempinst[0]));
 			} else {
 				tid[0].addIdent(id[0]);
 			}
 		}
 		tid[0].setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		t[0] = tid[0];
 	}
 
 	private Type parseBasicType2(Type t) {
 		Type ts;
 		Type ta;
 		Type subType;
 
 		expect(delegateFunctionExpectations);
 		while (true) {
 			switch (token.value) {
 			case TOKmul:
 				subType = t;
 				
 				t = new TypePointer(subType);
 				t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 				
 				nextToken();
 				continue;
 
 			case TOKlbracket:
 				if (LTORARRAYDECL) {
 					// Handle []. Make sure things like
 					// int[3][1] a;
 					// is (array[1] of array[3] of int)
 					nextToken();
 					if (token.value == TOKrbracket) {
 						subType = t;
 						
 						t = new TypeDArray(subType);
 						t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 						
 						nextToken();
 					} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's
 																				// an
 																				// associative
 																				// array
 																				// declaration
 						subType = t;
 						Type index;
 
 						if (apiLevel < D2) {
 							index = parseBasicType();
 							index = parseDeclarator(index, null); // [ type ]
 						} else {
 							index = parseType();                  // [ type ]
 						}
 						
 						t = newTypeAArray(t, index);
 						check(TOKrbracket);
 					} else {
 						subType = t;
 
 						inBrackets++;
 						Expression e = parseExpression(); // [ expression ]
 					    if (token.value == TOKslice) {
 							Expression e2;
 
 							nextToken();
 							e2 = parseExpression(); // [ exp .. exp ]
 							
 							t = new TypeSlice(t, e, e2);
 						} else {
 							t = new TypeSArray(t, e);
 						}
 					    t.setSourceRange(subType.start, token.ptr + token.sourceLen - subType.start);
 						
 						inBrackets--;
 						
 						check(TOKrbracket);
 					}
 					continue;
 				} else {
 					// Handle []. Make sure things like
 					// int[3][1] a;
 					// is (array[3] of array[1] of int)
 					ts = t;
 					while (token.value == TOKlbracket) {
 						nextToken();
 						if (token.value == TOKrbracket) {// []
 							ta = new TypeDArray(t);
 							nextToken();
 						} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's
 																					// an
 																					// associative
 																					// array
 																					// declaration
 							Type index;
 
 							index = parseBasicType();
 							index = parseDeclarator(index, null); // [ type ]
 							
 							ta = newTypeAArray(t, index);
 							
 							check(TOKrbracket);
 						} else {
 							Expression e = parseExpression(); // [ expression
 																// ]
 							ta = new TypeSArray(t, e);
 							check(TOKrbracket);
 						}
 
 						if (ts != t) {
 							Type pt = ts;
 							while(pt.next != t) {
 								pt = pt.next;
 							}
 							pt.next = ta;
 							pt.sourceNext = ta;
 						} else {
 							ts = ta;
 						}
 					}
 					continue;
 				}
 
 			case TOKdelegate:
 			case TOKfunction: { // Handle delegate declaration:
 				// t delegate(parameter list)
 				// t function(parameter list)
 				Arguments arguments;
 				int varargs = 0;
 				TOK save = token.value;
 
 				nextToken();
 
 				int[] pointer2_varargs = { varargs };
 				arguments = parseParameters(pointer2_varargs);
 				varargs = pointer2_varargs[0];
 
 				int saveStart = t.start;
 
 				t = new TypeFunction(arguments, t, varargs, linkage);
 				
 				// Assign parent of arguments
 				if (arguments != null) {
 					for(int i = 0; i < arguments.size(); i++) {
 						arguments.get(i).parentType = (TypeFunction) t;
 					}
 				}
 				
 				if (save == TOKdelegate) {
 					t = new TypeDelegate(t);
 				} else {
 					t = new TypePointer(t);
 				}
 				t.setSourceRange(saveStart, prevToken.ptr + prevToken.sourceLen - saveStart);
 				continue;
 			}
 
 			default:
 				ts = t;
 				break;
 			}
 			break;
 		}
 		return ts;
 	}
 	
 	private Type parseDeclarator(Type targ, IdentifierExp[] ident) {
 		return parseDeclarator(targ, ident, null, null);
 	}
 
 	private Type parseDeclarator(Type t, IdentifierExp[] pident,
 			TemplateParameters[] tpl, int[] identStart) {
 		// This may happen if a basic type was not find
 		if (t == null) {
 			return null;
 		}
 
 		Type ts;
 		Type ta;
 
 		t = parseBasicType2(t);
 
 		switch (token.value) {
 
 		case TOKidentifier:
 			if (pident != null) {
 				pident[0] = newIdentifierExp();
 				if (identStart != null)
 					identStart[0] = token.ptr;
 			} else {
 				error(IProblem.UnexpectedIdentifierInDeclarator,
 						token.lineNumber, token.ptr, token.sourceLen);
 			}
 			ts = t;
 			nextToken();
 			break;
 
 		case TOKlparen:
 			int oldStart = t.start;
 			nextToken();
 			ts = parseDeclarator(t, pident, null, identStart);
 			ts.setSourceRange(oldStart, token.ptr + token.sourceLen - oldStart);
 			check(TOKrparen);
 			break;
 
 		default:
 			ts = t;
 			break;
 		}
 
 		while (true) {
 			switch (token.value) {
 			//#if CARRAYDECL
 			case TOKlbracket: { // This is the old C-style post [] syntax.
 				nextToken();
 				if (token.value == TOKrbracket) // []
 				{
 					ta = new TypeDArray(t);
 					ta.setSourceRange(t.start, token.ptr + token.sourceLen
 							- t.start);
 
 					nextToken();
 				} else if (isDeclaration(token, 0, TOKrbracket, null)) { // It's an associative array declaration
 					Type index;
 
 					if (apiLevel < D2) {
 						index = parseBasicType();
 						index = parseDeclarator(index, null, null, identStart); // [ type ]
 					} else {
 						index = parseType(); // [ type ]
 					}
 
 					ta = newTypeAArray(t, index);
 
 					check(TOKrbracket);
 				} else {
 					Expression e = parseExpression(); // [ expression ]
 
 					ta = new TypeSArray(t, e);
 					ta.setSourceRange(t.start, token.ptr + token.sourceLen
 							- t.start);
 					check(TOKrbracket);
 				}
 
 				if (ts != t) {
 					Type pt = ts;
 					while (pt.next != t) {
 						pt = pt.next;
 					}
 					pt.next = ta;
 					pt.sourceNext = ta;
 				} else {
 					ts = ta;
 				}
 				continue;
 			}
 				//#endif
 			case TOKlparen: {
 				Arguments arguments;
 				int varargs = 0;
 
 				if (tpl != null) {
 					/* Look ahead to see if this is (...)(...),
 					 * i.e. a function template declaration
 					 */
 					if (peekPastParen(token).value == TOKlparen) { // It's a function template declaration
 
 						// Gather template parameter list
 						boolean[] malformed = { false };
 						tpl[0] = parseTemplateParameterList(malformed);
 					}
 				}
 
 				int[] pointer2_varargs = { varargs };
 				arguments = parseParameters(pointer2_varargs);
 				varargs = pointer2_varargs[0];
 
 				ta = new TypeFunction(arguments, t, varargs, linkage);
 
 				// Assign parent of arguments
 				if (arguments != null) {
 					for (int i = 0; i < arguments.size(); i++) {
 						arguments.get(i).parentType = (TypeFunction) ta;
 					}
 				}
 
 				ta.setSourceRange(t.start, t.length);
 
 				if (ts != t) {
 					Type pt = ts;
 					while (pt.next != t) {
 						pt = pt.next;
 					}
 					pt.next = ta;
 					pt.sourceNext = ta;
 				} else {
 					ts = ta;
 				}
 				break;
 			}
 			}
 			break;
 		}
 
 		return ts;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Dsymbols parseDeclarations(List<Comment> lastComments) {
 		int storage_class;
 		int stc;
 		Type ts;
 		Type t;
 		Type tfirst;
 		IdentifierExp ident;
 		int lineNumber = 0;
 		Dsymbols a;
 		TOK tok;
 		LINK link = linkage;
 		
 		List<Modifier> modifiers = new ArrayList<Modifier>();
 		
 		int start = token.ptr;
 
 		expect(typedefAliasExpectations);
 		switch (token.value) {
 		case TOKtypedef:
 		case TOKalias:
 			tok = token.value;
 			nextToken();
 			break;
 
 		default:
 			tok = TOKreserved;
 			break;
 		}
 
 		expect(modifierExpectations);
 		storage_class = STCundefined;
 		while (true) {
 			switch (token.value) {
 			case TOKconst:
 			case TOKinvariant:
 				if (apiLevel == D2 && peek(token).value == TOKlparen) {
 					break;
 				}
 				// fall
 			case TOKstatic:
 			case TOKfinal:
 			case TOKauto:
 			case TOKscope:
 			case TOKoverride:
 			case TOKabstract:
 			case TOKsynchronized:
 			case TOKdeprecated:
 				stc = STC.fromTOK(token.value);
 				
 				Modifier currentModifier = newModifier();
 				if ((storage_class & stc) != 0) {
 					error(IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
 				}
 				storage_class = storage_class | stc;
 				modifiers.add(currentModifier);
 				nextToken();
 				continue;
 
 			case TOKextern:
 				if (peek(token).value != TOKlparen) {
 					stc = STC.fromTOK(token.value);
 					
 					currentModifier = newModifier(token);
 					if ((storage_class & stc) != 0) {
 						error(IProblem.RedundantStorageClass, token.lineNumber, currentModifier);
 					}
 					storage_class = storage_class | stc;
 					modifiers.add(currentModifier);
 					nextToken();
 					continue;
 				}
 
 				link = parseLinkage();
 				continue;
 
 			default:
 				break;
 			}
 			break;
 		}
 
 		a = new Dsymbols();
 		
 		boolean first = true;
 		VarDeclaration previousVar = null;
 
 		/*
 		 * Look for auto initializers: storage_class identifier = initializer;
 		 */
 		while (storage_class != 0 && token.value == TOKidentifier
 				&& peek(token).value == TOKassign) {
 			ident = newIdentifierExp();
 			lineNumber = token.lineNumber;
 			
 			nextToken();
 			nextToken();
 			Initializer init = parseInitializer();
 
 			VarDeclaration v = newVarDeclaration(loc(), null, ident, init);
 			v.first = first;
 			first = false;
 			
 			v.storage_class = storage_class;
 			v.addModifiers(modifiers);
 			a.add(v);
 			
 			if (previousVar != null) {
 				previousVar.next = v;
 			}
 			previousVar = v;
 			
 			if (token.value == TOKsemicolon) {
 				v.setSourceRange(start, token.ptr + token.sourceLen - start);
 				
 				// Discard any previous comments
 				if (comments != null) {
 					lastCommentRead = comments.size();
 				}
 				
 				nextToken();
 				v.preComments = lastComments;
 				attachLeadingComments(v);
 			} else if (token.value == TOKcomma) {
 				v.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				
 				// Discard any previous comments
 				if (comments != null) {
 					lastCommentRead = comments.size();
 				}
 				
 				nextToken();
 				if (!(token.value == TOKidentifier && peek(token).value == TOKassign)) {
 					parsingErrorInsertTokenAfter(prevToken, "identifier");
 				} else {
 					start = token.ptr;
 					continue;
 				}
 			} else {
 				parsingErrorInsertTokenAfter(prevToken, ";");
 			}
 			
 			return a;
 		}
 
 		expect(classExpectations);
 		if (token.value == TOKclass) {
 			AggregateDeclaration s;
 
 			s = (AggregateDeclaration) parseAggregate();
 			s.storage_class = storage_class;
 			s.addModifiers(modifiers);
 			a.add(s);
 			s.preComments = lastComments;
 			return a;
 		}
 		
 		int nextVarStart = token.ptr;
 		int nextTypdefOrAliasStart = start;
 
 		ts = parseBasicType();
 		if (ts == null) {
 			return a;
 		}
 		ts = parseBasicType2(ts);
 		tfirst = null;
 		
 		int[] identStart = new int[1];
 		
 		AliasDeclaration previousAlias = null;
 		TypedefDeclaration previousTypedef = null;
 		first = true;
 		while (true) {
 			TemplateParameters tpl = null;
 
 			ident = null;
 			IdentifierExp[] pointer2_ident = { ident };
 			TemplateParameters[] pointer2_tpl = { tpl };
 			t = parseDeclarator(ts, pointer2_ident, pointer2_tpl, identStart);
 			ident = pointer2_ident[0];
 			tpl = pointer2_tpl[0];
 			
 			if(t == null) {
 				throw new IllegalStateException();
 			}
 			if (tfirst == null)
 				tfirst = t;
 			else if (t.singleton != tfirst.singleton) {
 				// TODO check this, should be doing this
 				if (ident != null) {
 					error(IProblem.MultipleDeclarationsMustHaveTheSameType,
 							 lineNumber, ident.start, ident.length);
 				}
 			}
 			if (ident == null) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				//return a;
 			}
 
 			if (tok == TOKtypedef || tok == TOKalias) {
 				Declaration v;
 				Initializer init;
 				
 				init = null;
 				
 				int assignTokenStart = token.ptr;
 				int assignTokenLine = token.lineNumber;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				TypedefDeclaration td = null;
 				AliasDeclaration ad = null;
 				if (tok == TOKtypedef) {
 					td = new TypedefDeclaration(loc(), ident, t, init);
 					td.first = first;
 					v = td;
 					if (previousTypedef != null) {
 						previousTypedef.next = td;
 					}
 					previousTypedef = td;
 				} else {
 					if (init != null) {
 						error(IProblem.AliasCannotHaveInitializer, assignTokenLine, assignTokenStart,  init.start + init.length - assignTokenStart);
 					}
 					
 					ad = new AliasDeclaration(loc(), ident, t);
 					ad.first = first;
 					v = ad;
 					if (previousAlias != null) {
 						previousAlias.next = ad;
 					}
 					previousAlias = ad;
 				}
 				first = false;
 				
 				v.addModifiers(modifiers);
 				v.storage_class = storage_class;
 				
 			    if (link == linkage) {
 					a.add(v);
 			    } else {
 			    	// TODO: this is never reached by tests
 			    	Dsymbols ax = new Dsymbols();
 			    	ax.add(v);
 			    	Dsymbol s = new LinkDeclaration(link, ax);
 			    	a.add(s);
 			    }
 				
 				switch (token.value) {
 				case TOKsemicolon:			
 					v.setSourceRange(nextTypdefOrAliasStart, token.ptr + token.sourceLen - nextTypdefOrAliasStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					v.preComments = lastComments;
 					attachLeadingComments(v);
 					break;
 
 				case TOKcomma:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.sourceLen - nextTypdefOrAliasStart);
 					nextToken();
 					continue;
 
 				default:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextTypdefOrAliasStart, prevToken.ptr + prevToken.sourceLen - nextTypdefOrAliasStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			} else if (t.ty == Tfunction) {
 				Dsymbol s;
 				
 				TypeFunction typeFunction = (TypeFunction) t;
 				
 				FuncDeclaration f = new FuncDeclaration(loc(), ident, storage_class, typeFunction);
 				parseContracts(f);
 				f.setSourceRange(t.start, prevToken.ptr + prevToken.sourceLen - t.start);
 				
 				// it's a function template
 				if (tpl != null) {
 					f.templated = true;
 				}
 				
 				if (link == linkage) {
 					s = f;
 				} else {
 					Dsymbols ax = new Dsymbols();
 					ax.add(f);
 					s = new LinkDeclaration(link, ax);
 				}
 					
 				if (tpl != null) { // it's a function template
 					Dsymbols decldefs;
 					TemplateDeclaration tempdecl;
 
 					// Wrap a template around the aggregate declaration
 					decldefs = new Dsymbols();
 					decldefs.add(s);
 					tempdecl = new TemplateDeclaration(loc(), s.ident, tpl, decldefs);
 					tempdecl.setSourceRange(s.start, s.length);
 					tempdecl.wrapper = true;
 					s = tempdecl;
 				}
 				s.modifiers = modifiers;
 				s.preComments = lastComments;
 				attachLeadingComments(s);
 				a.add(s);
 			} else {
 				VarDeclaration v;
 				Initializer init;
 
 				init = null;
 				if (token.value == TOKassign) {
 					nextToken();
 					init = parseInitializer();
 				}
 				
 				v = newVarDeclaration(loc(), t, ident, init);
 				v.first = first;
 				first = false;
 				
 				v.storage_class = storage_class;
 				v.modifiers = modifiers;
 				a.add(v);
 				
 				if (previousVar != null) {
 					previousVar.next = v;
 				}
 				previousVar = v;
 				
 				switch (token.value) {
 				case TOKsemicolon:
 					v.setSourceRange(nextVarStart, token.ptr + token.sourceLen - nextVarStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					v.preComments = lastComments;
 					attachLeadingComments(v);
 					break;
 
 				case TOKcomma:
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.sourceLen - nextVarStart);
 					
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					nextToken();
 					continue;
 
 				default:
 					// Discard any previous comments
 					if (comments != null) {
 						lastCommentRead = comments.size();
 					}
 					
 					v.setSourceRange(nextVarStart, prevToken.ptr + prevToken.sourceLen - nextVarStart);
 					parsingErrorInsertTokenAfter(prevToken, ";");
 					break;
 				}
 			}
 			break;
 		}
 		
 		return a;
 	}
 	
 	private void parseContracts(final FuncDeclaration f) {
 	    LINK linksave = linkage;
 
 	    // The following is irrelevant, as it is overridden by sc->linkage in
 	    // TypeFunction::semantic
 	    linkage = LINKd;		// nested functions have D linkage
 		
 		boolean repeat = true;
 		while (repeat) {
 			repeat = false;
 			// L1:
 			
 			expect(contractsExpectations);
 			
 			switch (token.value) {
 			case TOKlcurly:
 				if (f.frequire != null || f.fensure != null) {
 					parsingErrorInsertToComplete(prevToken, "body { ... }", "FunctionDeclaration");
 				}
 				
 				/*
 				 * Diet parsing: skip the function body if possible.
 				 * But... the function's body might be needed later if we need to interpret it.
 				 * So... assign the "rest" of it by parsing the actual body when needed.
 				 */
 				final int startSkip = p;
 				
 				Statement body = dietParseStatement(f);
 				f.setFbody(body);
 				
 				// Diet successful
 				if (body == null) {
 					final int endSkip = p;
 					final Module moduleSkip = module;
 					f.rest = new SemanticRest(new Runnable() {
 						public void run() {
 							Parser parser = new Parser(apiLevel, CharOperation.subarray(input, startSkip, endSkip));
 							parser.module = moduleSkip;
 							
 							Statement body = parser.dietParseStatement(f); 
 							f.fbody = body;
 						}
 					});
 				}
 				
 				break;
 
 			case TOKbody:
 				nextToken();
 				f.setFbody(parseStatement(PScurly));
 				break;
 
 			case TOKsemicolon:
 				if (f.frequire != null || f.fensure != null) {
 					parsingErrorInsertToComplete(prevToken, "body { ... }", "FunctionDeclaration");
 				}
 				//f.length = token.ptr + token.len - f.startPosition;
 				nextToken();
 				break;
 
 			/*
 			 * #if 0 // Do we want this for function declarations, so we can do: //
 			 * int x, y, foo(), z; case TOKcomma: nextToken(); continue; #endif
 			 */
 
 			/*
 			 * #if 0 // Dumped feature case TOKthrow: if (!f->fthrows)
 			 * f->fthrows = new Array(); nextToken(); check(TOKlparen); while
 			 * (1) { tb = parseBasicType(); f->fthrows->push(tb); if
 			 * (token.value == TOKcomma) { nextToken(); continue; } break; }
 			 * check(TOKrparen); goto L1; #endif
 			 */
 
 			case TOKin:
 				if (f.frequire != null) {
 					error(IProblem.RedundantInStatement,
 							token);
 				}
 				nextToken();
 				
 				f.setFrequire(parseStatement(PScurly | PSscope));
 				repeat = true;
 				break;
 
 			case TOKout:
 				// parse: out (identifier) { statement }
 				
 				if (f.fensure != null) {
 					error(IProblem.RedundantOutStatement,
 							token.lineNumber, token.ptr, token.sourceLen);
 				}
 				
 				nextToken();
 				if (token.value != TOKlcurly) {
 					check(TOKlparen);
 					if (token.value != TOKidentifier) {
 						parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					} else {
 						f.outId = newIdentifierExp();
 						nextToken();
 					}
 					
 					check(TOKrparen);
 				}
 				
 				f.setFensure(parseStatement(PScurly | PSscope));
 				repeat = true;
 				break;
 
 			default:
 				parsingErrorInsertTokenAfter(prevToken, ";");
 				break;
 			}
 		}
 		
 		linkage = linksave;
 	}
 
 	public Initializer parseInitializer() {
 		StructInitializer is;
 		ArrayInitializer ia;
 		ExpInitializer ie;
 		Expression e;
 		IdentifierExp id;
 		Initializer value;
 		int comma;
 		Token t;
 		int braces = 0;
 		
 		int start = token.ptr;
 
 		expect(voidExpectations);
 		switch (token.value) {
 		case TOKlcurly:
 		    /* Scan ahead to see if it is a struct initializer or
 		     * a function literal.
 		     * If it contains a ';', it is a function literal.
 		     * Treat { } as a struct initializer.
 		     */
 		    braces = 1;
 		    loop:
 			for (t = peek(token); true; t = peek(t)) {
 				switch (t.value) {
 				case TOKsemicolon:
 				case TOKreturn:
 					// goto Lexpression;
 					e = parseAssignExp();
 					ie = new ExpInitializer(loc(), e);
 					return ie;
 
 				case TOKlcurly:
 					braces++;
 					continue;
 
 				case TOKrcurly:
 					if (--braces == 0) {
 						break;
 					}
 					continue;
 				case TOKeof:
 					break loop;
 				default:
 					continue;
 				}
 				break;
 			}
 			
 			is = new StructInitializer(loc());
 			nextToken();
 			comma = 0;
 			while (true) {
 				switch (token.value) {
 				case TOKidentifier:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 					}
 					t = peek(token);
 					if (t.value == TOKcolon) {
 						id = newIdentifierExp();
 						nextToken();
 						nextToken(); // skip over ':'
 					} else {
 						id = null;
 					}
 					value = parseInitializer();
 					
 					is.addInit(id, value);
 					
 					comma = 1;
 					continue;
 
 				case TOKcomma:
 					nextToken();
 					comma = 2;
 					continue;
 
 				case TOKrcurly: // allow trailing comma's
 					is.setSourceRange(start, token.ptr + token.sourceLen - start);
 					nextToken();
 					break;
 				
 				case TOKeof:
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					return null;
 					//break;
 
 				default:
 					value = parseInitializer();
 				
 					is.addInit(null, value);
 					
 					comma = 1;
 					continue;
 				}
 				break;
 			}
 			
 			return is;
 
 		case TOKlbracket:
 			ia = new ArrayInitializer(loc());
 			nextToken();
 			comma = 0;
 			while (true) {
 				switch (token.value) {
 				default:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 						nextToken();
 						ia.setSourceRange(start, token.ptr + token.sourceLen - start);
 						break;
 					}
 					e = parseAssignExp();
 					if (e == null)
 						break;
 					if (token.value == TOKcolon) {
 						nextToken();
 						value = parseInitializer();
 					} else {
 						value = new ExpInitializer(loc(), e);
 						e = null;
 					}
 					ia.addInit(e, value);
 					comma = 1;
 					continue;
 
 				case TOKlcurly:
 				case TOKlbracket:
 					if (comma == 1) {
 						parsingErrorInsertTokenAfter(prevToken, ",");
 					}
 					
 					value = parseInitializer();
 					
 					ia.addInit(null, value);
 					comma = 1;
 					continue;
 
 				case TOKcomma:
 					nextToken();
 					comma = 2;
 					continue;
 
 				case TOKrbracket: // allow trailing comma's
 					ia.setSourceRange(start, token.ptr + token.sourceLen - start);
 					nextToken();					
 					break;
 
 				case TOKeof:
 					parsingErrorInsertTokenAfter(prevToken, "ArryaInitializer");
 					break;
 				}
 				break;
 			}
 			return ia;
 
 		case TOKvoid:
 			t = peek(token);
 			if (t.value == TOKsemicolon || t.value == TOKcomma) {
 				nextToken();
 				return newVoidInitializerForToken(prevToken);
 			}
 			// goto Lexpression;
 
 		default:
 			// Lexpression:
 			e = parseAssignExp();
 			e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			ie = new ExpInitializer(loc(), e);
 			return ie;
 		}
 	}
 	
 	@SuppressWarnings("unchecked") 
 	public Statement parseStatement(int flags) {
 		if (token.value == null) nextToken();
 		
 		Statement s = null;
 		Token t;
 		Statement ifbody;
 	    Statement elsebody;
 
 		if ((flags & PScurly) != 0 && token.value != TOKlcurly) {
 			error(
 					IProblem.StatementExpectedToBeCurlies, token);
 		}
 		
 		int start = token.ptr;
 
 		if (token.value != TOKlcurly) {
 			expect(statementExpectations);
 			if (apiLevel == D2) {
 				expect(traitsExpectations);
 			}
 		}
 		
 		switch (token.value) {
 		case TOKidentifier:
 			// Need to look ahead to see if it is a declaration, label, or
 			// expression
 			t = peek(token);
 			if (t.value == TOKcolon) { // It's a label
 				IdentifierExp ident = newIdentifierExp();
 				nextToken();
 				nextToken();
 				Statement body = parseStatement(PSsemi);
 				
 				s = new LabelStatement(loc(), ident, body);
 				s.start = ident.start;
 				s.length = prevToken.ptr + prevToken.sourceLen - ident.start;
 				break;
 			}
 			// fallthrough to TOKdot
 		case TOKdot:
 		case TOKtypeof:
 			if (isDeclaration(token, 2, TOKreserved, null)) {
 				// goto Ldeclaration;
 				Statement[] ps = { s };
 				parseStatement_Ldeclaration(ps, flags);
 				s = ps[0];
 				break;
 			} else {
 				// goto Lexp;
 				Expression exp = parseExpression();
 				
 				// Signal a new variable declaration if it's an identifier... this is nice
 				// to autocomplete Object | --> object
 				if (exp != null && exp.getNodeType() == ASTDmdNode.IDENTIFIER_EXP) {
 					newVarDeclaration(loc, new TypeIdentifier(loc, ((IdentifierExp) exp).ident), null, null);
 				}
 				
 				check(TOKsemicolon);
 				s = newExpStatement(loc(), exp);
 				break;
 			}
 			// break;
 
 		case TOKassert:
 		case TOKthis:
 		case TOKsuper:
 		case TOKint32v:
 		case TOKuns32v:
 		case TOKint64v:
 		case TOKuns64v:
 		case TOKfloat32v:
 		case TOKfloat64v:
 		case TOKfloat80v:
 		case TOKimaginary32v:
 		case TOKimaginary64v:
 		case TOKimaginary80v:
 		case TOKcharv:
 		case TOKwcharv:
 		case TOKdcharv:
 		case TOKnull:
 		case TOKtrue:
 		case TOKfalse:
 		case TOKstring:
 		case TOKlparen:
 		case TOKcast:
 		case TOKmul:
 		case TOKmin:
 		case TOKadd:
 		case TOKplusplus:
 		case TOKminusminus:
 		case TOKnew:
 		case TOKdelete:
 		case TOKdelegate:
 		case TOKfunction:
 		case TOKtypeid:
 		case TOKis:
 		case TOKlbracket:
 		case TOKtraits: // it's ok not to restrict according to version D2, since
 			            // the traits token is only present in D2
 			// Lexp:
 		{
 			Expression exp = parseExpression();
 			check(TOKsemicolon);
 			s = newExpStatement(loc(), exp);
 			s.setSourceRange(exp.start, prevToken.ptr + prevToken.sourceLen - exp.start);
 			break;
 		}
 
 		case TOKstatic: { // Look ahead to see if it's static assert() or
 							// static if()
 			Token t2;
 			
 			t2 = peek(token);
 			if (t2.value == TOKassert) {
 				nextToken();
 				
 				s = new StaticAssertStatement(parseStaticAssert());
 				break;
 			}
 			if (t2.value == TOKif) {
 				nextToken();
 				
 				StaticIfCondition staticIfCondition = parseStaticIfCondition();
 				
 				// goto Lcondition
 				ifbody = parseStatement(0 /*PSsemi*/);
 				elsebody = null;			
 				if (token.value == TOKelse) {
 					nextToken();
 					elsebody = parseStatement(0 /*PSsemi*/);
 				}
 				
 				s = new ConditionalStatement(loc(), staticIfCondition, ifbody, elsebody);
 				break;
 			}
 			// goto Ldeclaration;
 			Statement[] ps = { s };
 			parseStatement_Ldeclaration(ps, flags);
 			s = ps[0];
 			break;
 		}
 
 			// CASE_BASIC_TYPES:
 		case TOKwchar:
 		case TOKdchar:
 		case TOKbit:
 		case TOKbool:
 		case TOKchar:
 		case TOKint8:
 		case TOKuns8:
 		case TOKint16:
 		case TOKuns16:
 		case TOKint32:
 		case TOKuns32:
 		case TOKint64:
 		case TOKuns64:
 		case TOKfloat32:
 		case TOKfloat64:
 		case TOKfloat80:
 		case TOKimaginary32:
 		case TOKimaginary64:
 		case TOKimaginary80:
 		case TOKcomplex32:
 		case TOKcomplex64:
 		case TOKcomplex80:
 		case TOKvoid:
 		case TOKtypedef:
 		case TOKalias:
 		case TOKconst:
 		case TOKauto:
 		case TOKextern:
 		case TOKfinal:
 		case TOKinvariant:
 			
 		// case TOKtypeof:
 			// Ldeclaration:
 		{
 			Statement[] ps = { s };
 			parseStatement_Ldeclaration(ps, flags);
 			s = ps[0];
 			break;
 		}
 
 		case TOKstruct:
 		case TOKunion:
 		case TOKclass:
 		case TOKinterface: {
 			Dsymbol d;
 
 			d = parseAggregate();
 			if (d != null) {
 				d.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				s = new DeclarationStatement(loc(), d);
 			}
 			break;
 		}
 
 		case TOKenum: {
 			Dsymbol d;
 
 			d = parseEnum();
 			if (d != null) {
 				d.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				s = new DeclarationStatement(loc(), d);
 			}
 			break;
 		}
 
 		case TOKmixin: {
 			Dsymbol d;
 			t = peek(token);
 		    if (t.value == TOKlparen)
 		    {	// mixin(string)
 				nextToken();
 				check(TOKlparen);
 				Expression e = parseAssignExp();
 				check(TOKrparen);
 				check(TOKsemicolon);
 				s = new CompileStatement(loc(), e);
 				break;
 		    } else {			
 		    	d = parseMixin();
 		    	s = new DeclarationStatement(loc(), d);
 		    	break;
 		    }
 		}
 
 		case TOKlcurly: {
 			Statements statements;
 			
 			expect(statementExpectations);
 			if (apiLevel == D2) {
 				expect(traitsExpectations);
 			}
 
 			nextToken();
 			statements = new Statements();
 			
 			while (token.value != TOKrcurly) {
 				if (token.value == TOKeof) {
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					break;
 				}
 				
 				Statement subStatement = parseStatement(PSsemi | PScurlyscope);
 				if (subStatement != null) {
 					statements.add(subStatement);
 				}
 			}
 			
 			s = newBlock(statements, start, token.ptr + token.sourceLen - start);
 			
 			if ((flags & (PSscope | PScurlyscope)) != 0) {
 				s = new ScopeStatement(loc(), s);
 				s.setSourceRange(start, token.ptr + token.sourceLen - start);
 			}
 			
 			discardLastComments();
 			nextToken();
 			break;
 		}
 
 		case TOKwhile: {
 			Expression condition2;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			
 			body = parseStatement(PSscope);
 			
 			s = new WhileStatement(loc(), condition2, body);
 			break;
 		}
 
 		case TOKsemicolon:
 			if ((flags & PSsemi) == 0) {
 				error(IProblem.UseBracesForAnEmptyStatement, token);
 			}
 			nextToken();
 			
 			s = newExpStatement(loc(), null);
 			break;
 
 		case TOKdo: {
 			Statement body;
 			Expression condition2;
 			
 			nextToken();
 			
 			body = parseStatement(PSscope);
 			
 			expect(whileExpectations);
 			
 			check(TOKwhile);
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			s = new DoStatement(loc(), body, condition2);
 			break;
 		}
 
 		case TOKfor: {
 			Statement init;
 			Expression condition2;
 			Expression increment;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			if (token.value == TOKsemicolon) {
 				init = null;
 				nextToken();
 			} else {
 				init = parseStatement(0);
 			}
 			if (token.value == TOKsemicolon) {
 				condition2 = null;
 				nextToken();
 			} else {
 				condition2 = parseExpression();
 				check(TOKsemicolon);
 			}
 			if (token.value == TOKrparen) {
 				increment = null;
 				nextToken();
 			} else {
 				increment = parseExpression();
 				check(TOKrparen);
 			}
 			body = parseStatement(PSscope);
 			s = new ForStatement(loc(), init, condition2, increment, body);
 			if (init != null) {
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				
 				s = new ScopeStatement(loc(), s);
 				s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			}
 			break;
 		}
 
 		case TOKforeach:
 		case TOKforeach_reverse:
 		{
 			TOK op = token.value;
 			Arguments arguments;
 
 			// Statement d; // <-- not used
 			Statement body;
 			Expression aggr;
 			
 			nextToken();
 			check(TOKlparen);
 
 			arguments = new Arguments();
 
 			while (true) {
 				Type tb;
 				IdentifierExp ai = null;
 				Type at;
 				@SuppressWarnings("unused")
 				int storageClass;
 				Argument a;
 				List<Modifier> modifiers = new ArrayList<Modifier>(1);
 				
 				int argumentStart = token.ptr;
 
 				storageClass = STCin;
 				if (token.value == TOKinout || token.value == TOKref) {
 					storageClass = STCref;
 					modifiers.add(newModifier());
 					nextToken();
 				}
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						ai = newIdentifierExp();
 						at = null; // infer argument type
 						nextToken();
 						// goto Larg;
 						a = new Argument(storageClass, at, ai, null);
 						a.modifiers = modifiers;
 						a.setSourceRange(argumentStart, prevToken.ptr + prevToken.sourceLen - argumentStart);
 						arguments.add(a);
 						if (token.value == TOKcomma) {
 							nextToken();
 							continue;
 						}
 						break;
 					}
 				}
 				
 				int lineNumber = token.lineNumber;
 				
 				IdentifierExp[] pointer2_ai = { ai };
 				if (apiLevel < D2) {
 					tb = parseBasicType();
 					at = parseDeclarator(tb, pointer2_ai);
 				} else {
 					at = parseType(pointer2_ai);
 				}
 				ai = pointer2_ai[0];
 				
 				if (ai == null) {
 					if (at == null) {
 						error(IProblem.NoIdentifierForDeclarator, lineNumber, prevToken.ptr, prevToken.sourceLen);
 					} else {
 						error(IProblem.NoIdentifierForDeclarator, lineNumber, at);
 					}
 				}
 				// Larg:
 				if (at != null && ai != null) {
 					a = new Argument(storageClass, at, ai, null);
 					a.setSourceRange(argumentStart, prevToken.ptr + prevToken.sourceLen - argumentStart);
 					arguments.add(a);
 				}
 				if (token.value == TOKcomma) {
 					nextToken();
 					continue;
 				}
 				break;
 			}
 			check(TOKsemicolon);
 
 			aggr = parseExpression();
 			
 			if (apiLevel < D2) {
 				check(TOKrparen);
 				body = parseStatement(0);
 				
 				s = new ForeachStatement(loc(), op, arguments, aggr, body);
 			} else {
 			    if (token.value == TOKslice && arguments.size() == 1) {
 					Argument a = arguments.get(0);
 					nextToken();
 					Expression upr = parseExpression();
 					check(TOKrparen);
 					body = parseStatement(0);
 					s = new ForeachRangeStatement(loc(), op, a, aggr, upr, body);
 				} else {
 					check(TOKrparen);
 					body = parseStatement(0);
 					s = new ForeachStatement(loc(), op, arguments, aggr, body);
 				}
 			}
 			break;
 		}
 
 		case TOKif: {
 			Argument arg = null;
 			Expression condition2;
 			Statement ifbody2;
 			Statement elsebody2;
 			
 			nextToken();
 			check(TOKlparen);
 			
 			expect(autoExpectations);
 			if (token.value == TOKauto) {
 				int autoTokenStart = token.ptr;
 				List<Modifier> modifiers = new ArrayList<Modifier>(1);
 				modifiers.add(newModifier());
 				
 				nextToken();
 				if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKassign) {
 						arg = new Argument(STCin, null, newIdentifierExp(), null);
 						arg.modifiers = modifiers;
 						arg.setSourceRange(autoTokenStart, token.ptr + token.sourceLen - autoTokenStart);
 						
 						nextToken();
 						nextToken();
 					} else {
 						parsingErrorInsertTokenAfter(prevToken, "=");
 						// goto Lerror;
 						while (token.value != TOKrcurly
 								&& token.value != TOKsemicolon
 								&& token.value != TOKeof)
 							nextToken();
 						if (token.value == TOKsemicolon)
 							nextToken();
 						s = null;
 						break;
 					}
 				} else {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerror;
 					while (token.value != TOKrcurly
 							&& token.value != TOKsemicolon
 							&& token.value != TOKeof)
 						nextToken();
 					if (token.value == TOKsemicolon)
 						nextToken();
 					s = null;
 					break;
 				}
 			} else {
 				int argTokenStart = token.ptr;
 				int argTokenLine = token.lineNumber;
 				if (isDeclaration(token, 2, TOKassign, null)) {
 					Type tb;
 					Type at;
 					IdentifierExp ai = null;
 
 					
 					IdentifierExp[] pointer2_ai = { ai };
 					if (apiLevel < D2) {
 						tb = parseBasicType();
 						at = parseDeclarator(tb, pointer2_ai);
 					} else {
 						at = parseType(pointer2_ai);
 					}
 					ai = pointer2_ai[0];
 
 					arg = new Argument(STCin, at, ai, null);
 					arg.setSourceRange(argTokenStart, prevToken.ptr + prevToken.sourceLen - argTokenStart);
 					
 					check(TOKassign);					
 				}
 
 				// Check for " ident;"
 				else if (token.value == TOKidentifier) {
 					Token t2 = peek(token);
 					if (t2.value == TOKcomma || t2.value == TOKsemicolon) {
 						arg = new Argument(STCin, null, newIdentifierExp(), null);
 						arg.setSourceRange(argTokenStart, token.ptr + token.sourceLen - argTokenStart);
 						
 						nextToken();
 						nextToken();
 						
 						// if (!global.params.useDeprecated)
 						error(IProblem.IfAutoDeprecated, argTokenLine, argTokenStart, token.ptr + token.sourceLen - argTokenStart);
 					}
 				}
 			}
 
 			condition2 = parseExpression();
 			check(TOKrparen);
 			ifbody2 = parseStatement(PSscope);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody2 = parseStatement(PSscope);
 			} else
 				elsebody2 = null;
 			s = new IfStatement(loc(), arg, condition2, ifbody2, elsebody2);
 			break;
 		}
 
 		case TOKscope:
 		    if (peek(token).value != TOKlparen) {
 		    	// goto Ldeclaration
 		    	// scope used as storage class
 				Statement[] ps = { s };
 				parseStatement_Ldeclaration(ps, flags);
 				s = ps[0];
 				break;
 		    }
 			
 			nextToken();
 			check(TOKlparen);
 			
 			expect(scopeArgsExpectations);
 			
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof)
 					nextToken();
 				if (token.value == TOKsemicolon)
 					nextToken();
 				s = null;
 				break;
 			} else {
 				TOK t2 = TOKon_scope_exit;
 
 				// This char[] instances are reused by the Lexer
 				char[] id = token.sourceString;			
 				if (id == Id.exit)
 					t2 = TOKon_scope_exit;
 				else if (id == Id.failure)
 					t2 = TOKon_scope_failure;
 				else if (id == Id.success)
 					t2 = TOKon_scope_success;
 				else {
 					error(IProblem.InvalidScopeIdentifier, token);
 				}
 				nextToken();
 				check(TOKrparen);
 				Statement st = parseStatement(PScurlyscope);
 				
 				s = new OnScopeStatement(loc(), t2, st);
 				break;
 			}
 
 		case TOKon_scope_exit:
 		case TOKon_scope_failure:
 		case TOKon_scope_success: {
 			
 			TOK t2 = token.value;
 			
 			// if (!global.params.useDeprecated)
 			error(IProblem.OnScopeDeprecated, token, new String[] { token.toString() });
 			nextToken();
 			Statement st = parseStatement(PScurlyscope);
 			
 			s = new OnScopeStatement(loc(), t2, st);
 			break;
 		}
 
 		case TOKdebug:
 			nextToken();
 			
 			DebugCondition condition = parseDebugCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);			
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc(), condition, ifbody, elsebody);
 			break;
 
 		case TOKversion:
 			nextToken();
 			
 			VersionCondition versionCondition = parseVersionCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc(), versionCondition, ifbody, elsebody);
 			break;
 
 		case TOKiftype:
 			IftypeCondition iftypeCondition = parseIftypeCondition();
 			
 			// goto Lcondition
 			ifbody = parseStatement(0 /*PSsemi*/);
 			elsebody = null;
 			
 			expect(elseExpectations);
 			if (token.value == TOKelse) {
 				nextToken();
 				elsebody = parseStatement(0 /*PSsemi*/);
 			}
 			
 			s = new ConditionalStatement(loc(), iftypeCondition, ifbody, elsebody);
 			break;
 
 		case TOKpragma: {
 			IdentifierExp ident;
 			Expressions args = null;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			
 			expect(pragmaArgsExpectations);
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				// goto Lerror;
 				while (token.value != TOKrcurly && token.value != TOKsemicolon
 						&& token.value != TOKeof)
 					nextToken();
 				if (token.value == TOKsemicolon)
 					nextToken();
 				s = null;
 				break;
 			}
 			ident = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKcomma)
 				args = parseArguments(); // pragma(identifier, args...);
 			else
 				check(TOKrparen); // pragma(identifier);
 			if (token.value == TOKsemicolon) {
 				nextToken();
 				body = null;
 			} else {
 				body = parseStatement(PSsemi);
 			}
 			
 			s = new PragmaStatement(loc(), ident, args, body);
 			break;
 		}
 
 		case TOKswitch: {
 			Expression condition2;
 			Statement body;
 
 			nextToken();
 			check(TOKlparen);
 			condition2 = parseExpression();
 			check(TOKrparen);
 			body = parseStatement(PSscope);
 			
 			s = new SwitchStatement(loc(), condition2, body);
 			break;
 		}
 
 		case TOKcase: {
 			Expression exp;
 			Statements statements;
 			// List cases = new ArrayList(); // array of Expression's
 			
 			// Note: this code was changed a little from DMD to support
 			// better code completion
 			List<CaseStatement> caseStatements = new ArrayList<CaseStatement>();
 
 			while (true) {
 				int caseEnd = token.ptr + token.sourceLen;
 				
 				nextToken();
 				
 				int expStart = token.ptr;
 				int expLength = token.sourceLen;
 				
 				exp = parseAssignExp();
 				caseStatements.add(newCaseStatement(loc(), exp, null, caseEnd, expStart, expLength));			
 				// cases.add(exp);
 				
 				if (token.value != TOKcomma) {
 					break;
 				}
 			}
 			check(TOKcolon);
 
 			statements = new Statements();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly) {
 				
 				if (token.value == TOKeof) {
 					break;
 				}
 				
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements, start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = new ScopeStatement(loc(), s);
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 
 			// Keep cases in order by building the case statements backwards
 			for(int i = caseStatements.size(); i != 0; i--) {
 				CaseStatement cs = caseStatements.get(i - 1);
 				cs.setStatement(s);
 				s = cs;
 			}
 			
 			break;
 		}
 
 		case TOKdefault: {
 			Statements statements;
 			
 			nextToken();
 			check(TOKcolon);
 
 			statements = new Statements();
 			while (token.value != TOKcase && token.value != TOKdefault
 					&& token.value != TOKrcurly) {
 				statements.add(parseStatement(PSsemi | PScurlyscope));
 			}
 			
 			s = newBlock(statements, start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = new ScopeStatement(loc(), s);
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			
 			s = new DefaultStatement(loc(), s);
 			break;
 		}
 
 		case TOKreturn: {
 			Expression exp;
 
 			nextToken();
 			if (token.value == TOKsemicolon) {
 				exp = null;
 			} else {
 				exp = parseExpression();
 			}
 			s = newReturnStatement(loc(), exp);
 			
 			check(TOKsemicolon);
 			break;
 		}
 
 		case TOKbreak: {
 			IdentifierExp ident;
 
 			nextToken();
 			if (token.value == TOKidentifier) {
 				ident = newIdentifierExp();
 				nextToken();
 			} else
 				ident = null;
 			check(TOKsemicolon);
 			
 			s = newBreakStatement(loc(), ident);
 			break;
 		}
 
 		case TOKcontinue: {
 			IdentifierExp ident;
 
 			nextToken();
 			if (token.value == TOKidentifier) {
 				ident = newIdentifierExp();
 				nextToken();
 			} else {
 				ident = null;
 			}
 			
 			check(TOKsemicolon);
 			
 			s = newContinueStatement(loc(), ident);
 			break;
 		}
 
 		case TOKgoto: {
 			IdentifierExp ident;
 			
 			nextToken();
 			if (token.value == TOKdefault) {
 				nextToken();
 				s = new GotoDefaultStatement(loc());
 			} else if (token.value == TOKcase) {
 				Expression exp = null;
 
 				nextToken();
 				if (token.value != TOKsemicolon) {
 					exp = parseExpression();
 				}
 				
 				s = new GotoCaseStatement(loc(), exp);
 			} else {
 				if (token.value != TOKidentifier) {
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					ident = null;
 				} else {
 					ident = newIdentifierExp();
 					nextToken();
 				}
 				
 				s = newGotoStatement(loc(), ident);
 			}
 			check(TOKsemicolon);
 			break;
 		}
 
 		case TOKsynchronized: {
 			Expression exp;
 			Statement body;
 			
 			nextToken();
 			if (token.value == TOKlparen) {
 				nextToken();
 				exp = parseExpression();
 				check(TOKrparen);
 			} else
 				exp = null;
 			body = parseStatement(PSscope);
 			
 			s = new SynchronizedStatement(loc(), exp, body);
 			break;
 		}
 
 		case TOKwith: {
 			Expression exp;
 			Statement body;
 			
 			nextToken();
 			check(TOKlparen);
 			exp = parseExpression();
 			check(TOKrparen);
 			
 			body = parseStatement(PSscope);
 			
 			s = new WithStatement(loc(), exp, body);
 			break;
 		}
 
 		case TOKtry: {
 			Statement body;
 			Array catches = null;
 			Statement finalbody = null;
 			
 			nextToken();
 			body = parseStatement(PSscope);
 			
 			expect(catchExpectations);
 			while (token.value == TOKcatch) {
 				Statement handler;
 				Catch c;
 				Type t2;
 				IdentifierExp id;
 				
 				int firstTokenStart = token.ptr;
 
 				nextToken();
 				if (token.value == TOKlcurly) {
 					t2 = null;
 					id = null;
 				} else {
 					check(TOKlparen);
 					id = null;
 					IdentifierExp[] pointer2_id = { id };
 					if (apiLevel < D2) {
 						t2 = parseBasicType();
 						t2 = parseDeclarator(t2, pointer2_id);
 					} else {
 						t2 = parseType(pointer2_id);
 					}
 					id = pointer2_id[0];
 					check(TOKrparen);
 				}
 				handler = parseStatement(0);
 				
 				c = new Catch(loc(), t2, id, handler);
 				c.setSourceRange(firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 				
 				if (catches == null) {
 					catches = new Array();
 				}
 				
 				catches.add(c);
 			}
 
 			expect(finallyExpectations);
 			if (token.value == TOKfinally) {
 				nextToken();
 				finalbody = parseStatement(0);
 			}
 
 			s = body;
 			if (catches == null && finalbody == null) {
 				parsingErrorInsertToComplete(prevToken, "Catch or finally", "TryStatement");
 			} else {
 				if (catches != null) {
 					s = new TryCatchStatement(loc(), body, catches);
 				}
 				if (finalbody != null) {
 					if (catches != null) {
 						s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 					}
 					s = new TryFinallyStatement(loc(), s, finalbody, catches != null);
 				}
 			}
 			break;
 		}
 
 		case TOKthrow: {
 			Expression exp;
 			
 			nextToken();
 			exp = parseExpression();
 			check(TOKsemicolon);
 			
 			s = new ThrowStatement(loc(), exp);
 			break;
 		}
 
 		case TOKvolatile:
 			nextToken();
 			s = parseStatement(PSsemi | PScurlyscope);
 			s = new VolatileStatement(loc(), s);
 			break;
 
 		case TOKasm: {
 			Statements statements;
 			IdentifierExp label;
 			List<Token> toklist = new ArrayList<Token>(6);
 			
 			// Parse the asm block into a sequence of AsmStatements,
 			// each AsmStatement is one instruction.
 			// Separate out labels.
 			// Defer parsing of AsmStatements until semantic processing.
 
 			nextToken();
 			check(TOKlcurly);
 			label = null;
 			statements = new Statements();
 			while (true) {
 				switch (token.value) {
 				case TOKidentifier:
 					if (toklist.isEmpty()) {
 						// Look ahead to see if it is a label
 						t = peek(token);
 						if (t.value == TOKcolon) { // It's a label
 							label = newIdentifierExp();
 							nextToken();
 							nextToken();
 							continue;
 						}
 					}
 					// goto Ldefault;
 					toklist.add(new Token(token));
 					nextToken();
 					continue;
 
 				case TOKrcurly:
 					if (!toklist.isEmpty() || label != null) {
 						parsingErrorInsertTokenAfter(prevToken, ";");
 					}
 					break;
 
 				case TOKsemicolon:
 					// Create AsmStatement from list of tokens we've saved
 					s = new AsmStatement(loc(), toklist);
 					if (toklist.isEmpty()) {
 						s.setSourceRange(token.ptr, token.sourceLen);
 					} else {
 						s.setSourceRange(toklist.get(0).ptr, token.ptr + token.sourceLen - toklist.get(0).ptr);
 					}
 					
 					toklist = new ArrayList<Token>(6);
 					
 					if (label != null) {
 						s = new LabelStatement(loc(), label, s);
 						s.start = label.start;
 						s.length = token.ptr + token.sourceLen - label.start;
 						label = null;
 					}
 					statements.add(s);
 					nextToken();
 					continue;
 
 				case TOKeof:
 					/* { */
 					parsingErrorInsertTokenAfter(prevToken, "}");
 					break;
 
 				default:
 					toklist.add(new Token(token));
 					nextToken();
 					continue;
 				}
 				break;
 			}
 			
 			s = new AsmBlock(loc(), statements);;
 			
 			nextToken();
 			break;
 		}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Statement");
 			// goto Lerror;
 
 			// Lerror:
 			while (token.value != TOKrcurly && token.value != TOKsemicolon
 					&& token.value != TOKeof)
 				nextToken();
 			if (token.value == TOKsemicolon)
 				nextToken();
 			s = null;
 			break;
 		}
 		
 		if (s != null) {
 			s.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		}
 		
 		return s;
 	}	
 
 	private void parseStatement_Ldeclaration(Statement[] s, int flags) {
 		List a;
 
 		a = parseDeclarations(new ArrayList<Comment>());
 		if (a.size() > 1) {
 			Statements as = new Statements(a.size());
 			for (int i = 0; i < a.size(); i++) {
 				Dsymbol d = (Dsymbol) a.get(i);
 				s[0] = new DeclarationStatement(loc(), d);
 				as.add(s[0]);
 			}
 			
 			s[0] = newManyVarsBlock(as);
 		} else if (a.size() == 1) {
 			Dsymbol d = (Dsymbol) a.get(0);
 			s[0] = new DeclarationStatement(loc(), d);
 		} else {
 			parsingErrorDeleteToken(token);
 			nextToken();
 			s[0] = null;
 		}
 		if ((flags & PSscope) != 0) {
 			s[0] = new ScopeStatement(loc(), s[0]);
 		}
 	}
 	
 	private void check(TOK value) {
 		if (token.value != value) {
 			parsingErrorInsertTokenAfter(prevToken, value.toString());
 		} else {
 			nextToken();
 		}
 	}
 	
 	private boolean isDeclaration(Token t, int needId, TOK endtok, Token[] pt) {
 		int haveId = 0;
 		
 		if (apiLevel == D2) {
 		    if ((t.value == TOKconst || t.value == TOKinvariant)
 					&& peek(t).value != TOKlparen) {
 		    /* const type
 			 * invariant type
 			 */
 				t = peek(t);
 			}
 		}
 
 
 		Token[] pointer2_t = { t };
 
 		if (!isBasicType(pointer2_t)) {
 			return false;
 		}
 
 		t = pointer2_t[0];
 
 		int[] pointer2_haveId = { haveId };
 		if (!isDeclarator(pointer2_t, pointer2_haveId, endtok)) {
 			return false;
 		}
 
 		t = pointer2_t[0];
 		haveId = pointer2_haveId[0];
 
 		if (needId == 1 || (needId == 0 && haveId == 0)
 				|| (needId == 2 && haveId != 0)) {
 			if (pt != null) {
 				pt[0] = t;
 			}
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean isBasicType(Token[] pt) {
 		// This code parallels parseBasicType()
 		Token t = pt[0];
 		// Token t2; // <-- not used
 		// int parens = 0; // <-- not used
 
 		switch (t.value) {
 		case TOKwchar: case TOKdchar: case TOKbit: case TOKbool:
 		case TOKchar: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64: case TOKfloat80:
 		case TOKimaginary32: case TOKimaginary64: case TOKimaginary80:
 		case TOKcomplex32: case TOKcomplex64: case TOKcomplex80:
 		case TOKvoid:
 			t = peek(t);
 			break;
 
 		case TOKidentifier: {
 			t = peek(t);
 			if (t.value == TOKnot) {
 				// goto L4
 				Token[] pointer2_t2 = { t };
 				boolean semiResult = isBasicType_L4(pointer2_t2);
 				t = pointer2_t2[0];
 				if (semiResult) {
 					pt[0] = t;
 				}
 				return semiResult;
 			}
 			
 			// goto L3
 			Token[] pointer2_t2 = { t };
 			boolean semiResult = isBasicType_L3(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 
 		case TOKdot: {
 			// goto Ldot;
 			Token[] pointer2_t2 = new Token[] { t };
 			boolean semiResult = isBasicType_Ldot(pointer2_t2);
 			t = pointer2_t2[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 
 		case TOKtypeof: {
 			/*
 			 * typeof(exp).identifier...
 			 */
 			t = peek(t);
 			if (t.value != TOKlparen) {
 				return false;
 			}
 
 			Token[] pointer2_t = { t };
 			if (!skipParens(t, pointer2_t)) {
 				return false;
 			}
 
 			t = pointer2_t[0];
 
 			// goto L2;
 			boolean semiResult = isBasicType_L2(pointer2_t);
 			t = pointer2_t[0];
 			if (semiResult) {
 				pt[0] = t;
 			}
 			return semiResult;
 		}
 			
 		case TOKconst:
 		case TOKinvariant: {
 			if (apiLevel < D2) {
 				return false;
 			}
 			
 		    // const(type)  or  invariant(type)
 		    t = peek(t);
 		    if (t.value != TOKlparen) {
 		    	return false;
 		    }
 		    t = peek(t);
 		    
 		    Token[] pointer2_t = { t };
 		    if (!isDeclaration(t, 0, TOKrparen, pointer2_t)) {
 		    	return false;
 		    }
 		    t = pointer2_t[0];
 		    
 		    t = peek(t);
 		    break;
 		}
 
 		default:
 			return false;
 		}
 		pt[0] = t;
 		return true;
 	}
 	
 	private boolean isBasicType_L2(Token[] pt) {
 		while (true) {
 			/* L2: */
 			pt[0] = peek(pt[0]);
 			/* L3: */
 			if (pt[0].value == TOKdot) {
 				/* Ldot: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_L3(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			firstTime = false;
 			
 			/* L3: */
 			if (pt[0].value == TOKdot) {
 				/* Ldot: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3;
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_Ldot(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			/* L3: */
 			if (pt[0].value == TOKdot || firstTime) {
 				/* Ldot: */
 				firstTime = false;
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKidentifier) {
 					return false;
 				}
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKnot) {
 					// goto L3;
 					return isBasicType_L3(pt);
 				}
 				/* L4: */
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isBasicType_L4(Token[] pt) {
 		
 		boolean firstTime = true;
 		while (true) {
 			/* L2: */
 			if (!firstTime) {
 				pt[0] = peek(pt[0]);
 			}
 			/* L3: */
 			if (pt[0].value == TOKdot || firstTime) {
 				/* Ldot: */
 				if (!firstTime) {
 					pt[0] = peek(pt[0]);
 					if (pt[0].value != TOKidentifier) {
 						return false;
 					}
 					pt[0] = peek(pt[0]);
 					if (pt[0].value != TOKnot) {
 						// goto L3;
 						return isBasicType_L3(pt);
 					}
 				}
 				/* L4: */
 				firstTime = false;
 				pt[0] = peek(pt[0]);
 				if (pt[0].value != TOKlparen) {
 					return false;
 				}
 
 				if (!skipParens(pt[0], pt)) {
 					return false;
 				}
 			} else {
 				break;
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean isDeclarator(Token[] pt, int[] haveId, TOK endtok) {
 		// This code parallels parseDeclarator()
 	    Token t = pt[0];
 	    int parens;
 
 	    if (t.value == TOKassign)
 		return false;
 
 	    while (true)
 	    {
 		parens = 0;
 		switch (t.value)
 		{
 		    case TOKmul:
 		    case TOKand:
 			t = peek(t);
 			continue;
 
 		    case TOKlbracket:
 			t = peek(t);
 			if (t.value == TOKrbracket)
 			{
 			    t = peek(t);
 			}
 			else { 
 				Token[] pointer2_t = { t };
 				if (isDeclaration(t, 0, TOKrbracket, pointer2_t))
 				{   // It's an associative array declaration
 					t = pointer2_t[0];
 				    t = peek(t);
 				}
 				else
 				{
 					t = pointer2_t[0];
 				    // [ expression ]
 				    // [ expression .. expression ]
 				    if (!isExpression(pointer2_t)) {
 				    	t = pointer2_t[0];
 				    	return false;
 				    }
 				    t = pointer2_t[0];
 				    
 				    if (t.value == TOKslice)
 				    {	
 				    	t = peek(t);
 				    	pointer2_t[0] = t;
 						if (!isExpression(pointer2_t)) {
 							t = pointer2_t[0];
 						    return false;
 					    }
 						t = pointer2_t[0];
 					}
 				    
 				    if (t.value != TOKrbracket) {
 				    	return false;
 				    }
 				    
 				    t = peek(t);
 				}
 				
 			}
 			continue;
 
 		    case TOKidentifier:
 			if (haveId[0] != 0)
 			    return false;
 			haveId[0] = 1;
 			t = peek(t);
 			break;
 
 		    case TOKlparen:
 			t = peek(t);
 
 			if (t.value == TOKrparen)
 			    return false;		// () is not a declarator
 
 			/* Regard ( identifier ) as not a declarator
 			 * BUG: what about ( *identifier ) in
 			 *	f(*p)(x);
 			 * where f is a class instance with overloaded () ?
 			 * Should we just disallow C-style function pointer declarations?
 			 */
 			if (t.value == TOKidentifier)
 			{   Token t2 = peek(t);
 			    if (t2.value == TOKrparen)
 				return false;
 			}
 
 
 			Token[] pointer2_t = { t };
 			if (!isDeclarator(pointer2_t, haveId, TOKrparen))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			t = peek(t);
 			parens = 1;
 			break;
 
 		    case TOKdelegate:
 		    case TOKfunction:
 			t = peek(t);
 			
 			pointer2_t = new Token[] { t };
 			
 			if (!isParameters(pointer2_t))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			continue;
 		}
 		break;
 	    }
 
 	    while (true)
 	    {
 		switch (t.value)
 		{
 	//#if CARRAYDECL
 		    case TOKlbracket:
 			parens = 0;
 			t = peek(t);
 			if (t.value == TOKrbracket)
 			{
 			    t = peek(t);
 			}
 			else {
 				Token[] pointer2_t = { t };
 				if (isDeclaration(t, 0, TOKrbracket, pointer2_t))
 				{   // It's an associative array declaration
 					t = pointer2_t[0];
 				    t = peek(t);
 				}
 				else
 				{
 					t = pointer2_t[0];
 					
 				    // [ expression ]
 				    if (!isExpression(pointer2_t))
 				    	return false;
 				    
 				    t = pointer2_t[0];
 				    
 				    
 				    if (t.value != TOKrbracket)
 					return false;
 				    t = peek(t);
 				}
 			}
 			continue;
 	//#endif
 
 		    case TOKlparen:
 			parens = 0;
 			
 			
 			Token[] pointer2_t = { t };
 			if (!isParameters(pointer2_t))
 			    return false;
 			
 			t = pointer2_t[0];
 			
 			continue;
 
 		    // Valid tokens that follow a declaration
 		    case TOKrparen:
 		    case TOKrbracket:
 		    case TOKassign:
 		    case TOKcomma:
 		    case TOKsemicolon:
 		    case TOKlcurly:
 		    case TOKin:
 			// The !parens is to disallow unnecessary parentheses
 			if (parens == 0 && (endtok == TOKreserved || endtok == t.value))
 			{   pt[0] = t;
 			    return true;
 			}
 			return false;
 
 		    default:
 			return false;
 		}
 	    }
 	}
 	
 	private boolean isParameters(Token[] pt) {
 		if (apiLevel < D2) {
 			return isParametersD1(pt);
 		} else {
 			return isParametersD2(pt);
 		}
 	}
 	
 	private boolean isParametersD1(Token[] pt) {
 		// This code parallels parseParameters()
 		Token t = pt[0];
 		int tmp;
 
 		if (t.value != TOKlparen)
 			return false;
 
 		t = peek(t);
 		while (true) {
 			switch (t.value) {
 			case TOKrparen:
 				break;
 
 			case TOKdotdotdot:
 				t = peek(t);
 				break;
 
 			case TOKin:
 			case TOKout:
 			case TOKinout:
 			case TOKref:
 			case TOKlazy:
 				t = peek(t);
 			default:
 
 				Token[] pointer2_t = { t };
 				if (!isBasicType(pointer2_t))
 					return false;
 
 				t = pointer2_t[0];
 
 				tmp = 0;
 
 				int[] pointer2_tmp = { tmp };
 
 				if (t.value != TOKdotdotdot
 						&& !isDeclarator(pointer2_t, pointer2_tmp, TOKreserved))
 					return false;
 
 				t = pointer2_t[0];
 				tmp = pointer2_tmp[0];
 
 				if (t.value == TOKassign) {
 					t = peek(t);
 					pointer2_t[0] = t;
 					if (!isExpression(pointer2_t))
 						return false;
 
 					t = pointer2_t[0];
 				}
 				if (t.value == TOKdotdotdot) {
 					t = peek(t);
 					break;
 				}
 				if (t.value == TOKcomma) {
 					t = peek(t);
 					continue;
 				}
 				break;
 			}
 			break;
 		}
 		if (t.value != TOKrparen)
 			return false;
 		t = peek(t);
 		pt[0] = t;
 		return true;
 	}
 	
 	private boolean isParametersD2(Token[] pt) {
 		// This code parallels parseParameters()
 		Token t = pt[0];
 		int tmp;
 
 		if (t.value != TOKlparen)
 			return false;
 
 		t = peek(t);
 		for(; true; t = peek(t)) {
 			switch (t.value) {
 			case TOKrparen:
 				break;
 
 			case TOKdotdotdot:
 				t = peek(t);
 				break;
 
 			case TOKin:
 			case TOKout:
 			case TOKinout:
 			case TOKref:
 			case TOKlazy:
 			case TOKconst:
 			case TOKinvariant:
 			case TOKfinal:
 			case TOKstatic:
 				continue;
 				
 			default:
 
 				Token[] pointer2_t = { t };
 				if (!isBasicType(pointer2_t))
 					return false;
 
 				t = pointer2_t[0];
 
 				tmp = 0;
 
 				int[] pointer2_tmp = { tmp };
 
 				if (t.value != TOKdotdotdot
 						&& !isDeclarator(pointer2_t, pointer2_tmp, TOKreserved))
 					return false;
 
 				t = pointer2_t[0];
 				tmp = pointer2_tmp[0];
 
 				if (t.value == TOKassign) {
 					t = peek(t);
 					pointer2_t[0] = t;
 					if (!isExpression(pointer2_t))
 						return false;
 
 					t = pointer2_t[0];
 				}
 				if (t.value == TOKdotdotdot) {
 					t = peek(t);
 					break;
 				}
 				if (t.value == TOKcomma) {
 					continue;
 				}
 				break;
 			}
 			break;
 		}
 		if (t.value != TOKrparen)
 			return false;
 		t = peek(t);
 		pt[0] = t;
 		return true;
 	}
 	
 	private boolean isExpression(Token[] pt)
 	{
 	    // This is supposed to determine if something is an expression.
 	    // What it actually does is scan until a closing right bracket
 	    // is found.
 
 	    Token t = pt[0];
 	    int brnest = 0;
 	    int panest = 0;
 	    int curlynest = 0;
 
 	    for (;; t = peek(t))
 	    {
 		switch (t.value)
 		{
 		    case TOKlbracket:
 			brnest++;
 			continue;
 
 		    case TOKrbracket:
 			if (--brnest >= 0)
 			    continue;
 			break;
 
 		    case TOKlparen:
 			panest++;
 			continue;
 
 		    case TOKcomma:
 			if (brnest != 0 || panest != 0)
 			    continue;
 			break;
 
 		    case TOKrparen:
 			if (--panest >= 0)
 			    continue;
 			break;
 			
 		    case TOKlcurly:
 			curlynest++;
 			continue;
 
 		    case TOKrcurly:
 			if (--curlynest >= 0)
 			    continue;
 			return false;
 
 
 		    case TOKslice:
 			if (brnest != 0)
 			    continue;
 			break;
 			
 		    case TOKsemicolon:
 				if (curlynest != 0)
 				    continue;
 				return false;
 
 
 		    case TOKeof:
 			return false;
 
 		    default:
 			continue;
 		}
 		break;
 	    }
 
 	    pt[0] = t;
 	    return true;
 	}
 	
 	/**********************************************
 	 * Skip over
 	 *	instance foo.bar(parameters...)
 	 * Output:
 	 *	if (pt), *pt is set to the token following the closing )
 	 * Returns:
 	 *	1	it's valid instance syntax
 	 *	0	invalid instance syntax
 	 */
 
 	/* <-- Not used
 	private boolean isTemplateInstance(Token t, Token[] pt)
 	{
 	    t = peek(t);
 	    if (t.value != TOKdot)
 	    {
 		if (t.value != TOKidentifier)
 		    //goto Lfalse;
 			return false;
 		
 		t = peek(t);
 	    }
 	    while (t.value == TOKdot)
 	    {
 		t = peek(t);
 		if (t.value != TOKidentifier)
 		    //goto Lfalse;
 			return false;
 		
 		t = peek(t);
 	    }
 	    if (t.value != TOKlparen)
 		//	goto Lfalse;
 	    	return false;
 
 	    // Skip over the template arguments
 	    while (true)
 	    {
 		while (true)
 		{
 		    t = peek(t);
 		    switch (t.value)
 		    {
 			case TOKlparen:
 				
 				Token[] pointer2_t = { t };
 				
 			    if (!skipParens(t, pointer2_t))
 			    	//goto Lfalse;
 			    	return false;
 			    
 			    t = pointer2_t[0];
 			    
 			    continue;
 			case TOKrparen:
 			    break;
 			case TOKcomma:
 			    break;
 			case TOKeof:
 			case TOKsemicolon:
 			    //goto Lfalse;
 				return false;
 			default:
 			    continue;
 		    }
 		    break;
 		}
 
 		if (t.value != TOKcomma)
 		    break;
 	    }
 	    if (t.value != TOKrparen)
 	    	//goto Lfalse;
 	    	return false;
 	    
 	    t = peek(t);
 	    if (pt != null)
 		pt[0] = t;
 	    return true;
 
 	//Lfalse:
 	//    return 0;
 	}
 	*/
 	
 	/*******************************************
 	 * Skip parens, brackets.
 	 * Input:
 	 *	t is on opening (
 	 * Output:
 	 *	*pt is set to closing token, which is ')' on success
 	 * Returns:
 	 *	!=0	successful
 	 *	0	some parsing error
 	 */
 
 	private boolean skipParens(Token t, Token[] pt)
 	{
 	    int parens = 0;
 
 	    while (true)
 	    {
 		switch (t.value)
 		{
 		    case TOKlparen:
 			parens++;
 			break;
 
 		    case TOKrparen:
 			parens--;
 			if (parens < 0)
 			    //goto Lfalse;
 				return false;
 			if (parens == 0) {
 			    //goto Ldone;
 				if (pt[0] != null)
 				pt[0] = t;
 				return true;
 			}
 			break;
 
 		    case TOKeof:
 		    case TOKsemicolon:
 		    	//goto Lfalse;
 		    	return false;
 
 		     default:
 			break;
 		}
 		t = peek(t);
 	    }
 
 	  //Ldone:
 	    //if (pt[0] != null)
 		//pt[0] = t;
 	    //return true;
 
 	  //Lfalse:
 	  //  return 0;
 	}
 	
 	/********************************* Expression Parser ***************************/
 	
 	private Expression parsePrimaryExp()
 	{   Expression e = null;
 	    Type t;
 	    IdentifierExp id;
 	    TOK save;
 
 	    expect(primaryExpExpectations);	    
 	    if (apiLevel == D2) {
 	    	expect(traitsExpectations);
 	    }
 	    
 	    switch (token.value)
 	    {
 		case TOKidentifier:
 		    id = newIdentifierExp();
 		    nextToken();
 		    if (token.value == TOKnot && peek(token).value == TOKlparen)
 		    {	// identifier!(template-argument-list)
 		    	TemplateInstance tempinst;
 		    	
 		    	tempinst = new TemplateInstance(loc(), id);		    	
 		    	nextToken();
 		    	tempinst.tiargs(parseTemplateArgumentList());
 		    	tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 				e = new ScopeExp(loc(), tempinst);
 				e.setSourceRange(tempinst.start, tempinst.length);
 		    }
 		    else {
 		    	e = id;
 		    }
 		    break;
 
 		case TOKdollar:
 		    if (inBrackets == 0) {
 		    	error(IProblem.DollarInvalidOutsideBrackets, token);
 		    }
 		    e = new DollarExp(loc());
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKdot:
 		    // Signal global scope '.' operator with "" identifier
 			e = new IdentifierExp(loc(), Id.empty);
 			e.start = token.ptr;
 			e.length = 0;
 		    break;
 
 		case TOKthis:
 		    e = newThisExp(loc());
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKsuper:
 			e = newSuperExp(loc());
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKint32v: e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tint32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKuns32v: e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tuns32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKint64v: e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tint64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKuns64v: e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tuns64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat32v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.tfloat32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat64v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.tfloat64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKfloat80v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.tfloat80); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary32v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.timaginary32); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary64v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.timaginary64); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKimaginary80v: e = new RealExp(loc(), token.sourceString, token.floatValue, Type.timaginary80); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 
 		case TOKnull:
 		    e = new NullExp(loc());
 		    e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 
 		case TOKtrue:
 			e = new IntegerExp(loc(), token.sourceString, 1, Type.tbool);
 		    nextToken();
 		    break;
 		case TOKfalse:
 			e = new IntegerExp(loc(), token.sourceString, 0, Type.tbool);
 		    nextToken();
 		    break;
 
 		case TOKcharv: e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKwcharv:  e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.twchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 		case TOKdcharv:  e = new IntegerExp(loc(), token.sourceString, token.intValue, Type.tdchar); e.setSourceRange(token.ptr, token.sourceLen); nextToken(); break;
 
 		case TOKstring: {
 			int startLine = token.lineNumber;
 			
 			List<StringExp> nextStringExps = null;
 			StringExp stringExp = newStringExpForCurrentToken();
 			StringExp lastStringExp = stringExp;
 			
 			char[] s = token.ustring;
 			int len = token.len;
 			int postfix;
 
 			// cat adjacent strings
 			postfix = token.postfix;
 			while (true) {
 				nextToken();
 				if (token.value == TOKstring) {
 					int len1;
 				    int len2;
 					
 					if (token.postfix != 0) {
 						if (token.postfix != postfix) {
 							error(
 									IProblem.MismatchedStringLiteralPostfixes,
 									startLine, lastStringExp.start, token.ptr + token.sourceLen - lastStringExp.start,
 									new String[] { String.valueOf((char) postfix) , String.valueOf((char) token.postfix) });
 						}							
 						postfix = token.postfix;
 					}
 
 				    len1 = len;
 				    len2 = token.len;
 				    len = len1 + len2;
 				    s = CharOperation.concat(s, token.ustring);
 
 					if (token.ustring != null) {
 						if (nextStringExps == null) {
 							nextStringExps = new ArrayList<StringExp>();
 							nextStringExps.add(newStringExpForPreviousToken());
 						}
 						lastStringExp = newStringExpForCurrentToken();
 						nextStringExps.add(lastStringExp);
 					}
 				} else
 					break;
 			}
 			
 			stringExp.allStringExps = nextStringExps;
 			stringExp.string = s;
 			stringExp.len = len;
 			e = stringExp;
 			break;
 		}
 		
 		// CASE_BASIC_TYPES_X(t):
 		case TOKvoid: case TOKint8: case TOKuns8: case TOKint16:
 		case TOKuns16: case TOKint32: case TOKuns32: case TOKint64:
 		case TOKuns64: case TOKfloat32: case TOKfloat64:
 		case TOKfloat80: case TOKimaginary32: case TOKimaginary64:
 		case TOKimaginary80: case TOKcomplex32: case TOKcomplex64:
 		case TOKcomplex80: case TOKbit: case TOKbool:
 		case TOKchar: case TOKwchar: case TOKdchar:
 			t = newTypeBasicForCurrentToken();
 			nextToken();
 			// L1:
 			    check(TOKdot);
 			    if (token.value != TOKidentifier)
 			    {
 			    	parsingErrorInsertTokenAfter(prevToken, "Identifier");
 			    	// goto Lerr;
 		    		// Anything for e, as long as it's not NULL
 			    	// Change from DMD
 					e = newTypeDotIdExp(loc(), t, new IdentifierExp(Id.empty));
 			    	//e = new IntegerExp(loc, Id.ZERO, 0, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.sourceLen);
 		    		nextToken();
 		    		break;
 			    }
 			    e = newTypeDotIdExp(loc(), t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.sourceLen - t.start);
 			    nextToken();
 			    break;
 
 		case TOKtypeof:
 		{   
 			Expression exp;
 			int start = token.ptr;
 
 		    nextToken();
 		    check(TOKlparen);
 		    exp = parseExpression();
 		    check(TOKrparen);
 		    
 			t = new TypeTypeof(loc(), exp);
 			t.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			((TypeTypeof) t).setTypeofSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			
 		    if (token.value == TOKdot) {
 		    	// goto L1;
 		    	check(TOKdot);
 			    if (token.value != TOKidentifier)
 			    {   
 			    	parsingErrorInsertTokenAfter(prevToken, "Identifier");
 					// goto Lerr;
 			    	// Anything for e, as long as it's not NULL
 			    	// Change from DMD
 					e = newTypeDotIdExp(loc(), t, new IdentifierExp(Id.empty));
 			    	//e = new IntegerExp(loc, Id.ZERO, 0, Type.tint32);
 			    	e.setSourceRange(token.ptr, token.sourceLen);
 			    	nextToken();
 			    	break;
 			    }
 			    e = newTypeDotIdExp(loc(), t, newIdentifierExp());
 			    e.setSourceRange(t.start, token.ptr + token.sourceLen - t.start);
 			    nextToken();
 			    break;
 		    }
 		    	
 		    e = new TypeExp(loc(), t);
 		    break;
 		}
 
 		case TOKtypeid:
 		{   Type t2;
 			int start = token.ptr;
 
 		    nextToken();
 		    check(TOKlparen);
 		    if (apiLevel < D2) {
 			    t2 = parseBasicType();
 			    t2 = parseDeclarator(t2, null);	// ( type )
 		    } else {
 		    	t2 = parseType();				// ( type 
 		    }
 		    check(TOKrparen);
 		    e = new TypeidExp(loc(), t2);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 		
 		case TOKtraits:
 		{   
 			/* __traits(identifier, args...)
 			 */
 			IdentifierExp ident;
 			Objects args = null;
 
 			nextToken();
 			check(TOKlparen);
 			
 			expect(traitsArgsExpectations);
 			
 			if (token.value != TOKidentifier) {
 				parsingErrorInsertToComplete(prevToken, "__traits(identifier, args...) expected", "traits expression");
 				//goto Lerr;
 				// Anything for e, as long as it's not NULL
 				e = new IntegerExp(loc(), Id.ZERO, 0, Type.tint32);
 		    	e.setSourceRange(token.ptr, token.sourceLen);
 				nextToken();
 				break;
 			}
 			ident = newIdentifierExp();
 			nextToken();
 			if (token.value == TOKcomma) {
 				args = parseTemplateArgumentList2(); // __traits(identifier, args...)
 			} else {
 				check(TOKrparen); // __traits(identifier)
 			}
 
 			e = new TraitsExp(loc(), ident, args);
 			break;
 		}
 
 		case TOKis:
 		{
 			Type targ = null;
 			IdentifierExp ident = null;
 			Type tspec = null;
 			TOK tok = TOKreserved;
 			TOK tok2 = TOKreserved;
 
 			nextToken();
 			if (token.value == TOKlparen) {
 				nextToken();
 
 				IdentifierExp[] pointer2_ident = { ident };
 				if (apiLevel < D2) {
 					targ = parseBasicType();
 					targ = parseDeclarator(targ, pointer2_ident);
 				} else {
 					targ = parseType(pointer2_ident);
 				}
 				ident = pointer2_ident[0];
 
 				if (token.value == TOKcolon || token.value == TOKequal) {
 					tok = token.value;
 					nextToken();
 					if (tok == TOKequal
 							&& (token.value == TOKtypedef
 									|| token.value == TOKstruct
 									|| token.value == TOKunion
 									|| token.value == TOKclass
 									|| token.value == TOKsuper
 									|| token.value == TOKenum
 									|| token.value == TOKinterface
 									|| token.value == TOKfunction
 									|| token.value == TOKdelegate || token.value == TOKreturn)) {
 						tok2 = token.value;
 						nextToken();
 					} else {
 						if (apiLevel < D2) {
 							tspec = parseBasicType();
 							tspec = parseDeclarator(tspec, null);
 						} else {
 							tspec = parseType();
 						}
 					}
 				}
 				check(TOKrparen);
 			} else {
 				parsingErrorInsertToComplete(prevToken, "(type identifier : specialization)", "IftypeDeclaration");
 				// goto Lerr;
 				// Anything for e, as long as it's not NULL
 				e = new IntegerExp(loc(), Id.ZERO, 0, Type.tint32);
 		    	e.setSourceRange(token.ptr, token.sourceLen);
 				nextToken();
 				break;
 			}
 			e = new IsExp(loc(), targ, ident, tok, tspec, tok2);
 			break;
 		}
 
 		case TOKassert: {
 			Expression msg = null;
 
 			int start = token.ptr;
 			nextToken();
 			check(TOKlparen);
 			e = parseAssignExp();
 			if (token.value == TOKcomma) {
 				nextToken();
 				msg = parseAssignExp();
 			}
 			check(TOKrparen);
 			
 			e = new AssertExp(loc(), e, msg);
 			e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 			break;
 		}
 		
 		case TOKmixin:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new CompileExp(loc(), e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 		case TOKimport:
 		{
 			int start = token.ptr;
 		    nextToken();
 		    check(TOKlparen);
 		    e = parseAssignExp();
 		    check(TOKrparen);
 		    e = new FileExp(loc(), e);
 		    e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		    break;
 		}
 
 
 		case TOKlparen:
 		    if (peekPastParen(token).value == TOKlcurly) { // (arguments) {
 															// statements... }
 				save = TOKdelegate;
 				// goto case_delegate;
 				{
 				Expression[] pe = { e };
 				parsePrimaryExp_case_delegate(pe, save, true /* empty syntax */);
 				e = pe[0];
 				}
 				break;
 			}
 			// ( expression )
 			int start = token.ptr;
 			nextToken();
 			e = parseExpression();
 
 			int end = token.ptr + token.sourceLen;
 			check(TOKrparen);
 
 			e.addParenthesis(start, end - start);
 			break;
 
 		case TOKlbracket:
 		{   
 			/* Parse array literals and associative array literals:
 		     *	[ value, value, value ... ]
 		     *	[ key:value, key:value, key:value ... ]
 		     */
 			Expressions values = new Expressions();
 			Expressions keys = null;
 
 			nextToken();
 			if (token.value != TOKrbracket) {
 				while (true) {
 					Expression e2 = parseAssignExp();
 					if (e2 instanceof ErrorExp) {
 						break;
 					}
 					
 					if (token.value == TOKcolon
 							&& (keys != null || values.size() == 0)) {
 						nextToken();
 						if (keys == null) {
 							keys = new Expressions();
 						}
 						keys.add(e2);
 						e2 = parseAssignExp();
 					} else if (keys != null) {
 						parsingErrorInsertToComplete(token, "key:value", "associative array literal");
 						keys = null;
 					}
 					values.add(e2);
 					if (token.value == TOKrbracket)
 						break;
 					check(TOKcomma);
 				}
 			}
 			check(TOKrbracket);
 
 			if (keys != null) {
 				e = new AssocArrayLiteralExp(loc(), keys, values);
 			} else {
 				e = new ArrayLiteralExp(loc(), values);
 			}
 		    break;
 		}
 		
 		case TOKlcurly:
 		    // { statements... }
 			save = TOKdelegate;
 			// goto case_delegate;
 			{
 			Expression[] pe = { e };
 			parsePrimaryExp_case_delegate(pe, save, true /* empty syntax */);
 			e = pe[0];
 			}
 			break;
 
 		case TOKfunction:
 		case TOKdelegate:
 		    save = token.value;
 			nextToken();
 			// case_delegate:
 			{
 				Expression[] pe = { e };
 				parsePrimaryExp_case_delegate(pe, save, false /* not empty syntax */);
 				e = pe[0];
 				break;
 			}
 
 		default:
 			parsingErrorInsertTokenAfter(prevToken, "Expression");
 		// Lerr:
 		    // Anything for e, as long as it's not NULL
 			e = new ErrorExp();
 	    	e.setSourceRange(token.ptr, token.sourceLen);
 		    nextToken();
 		    break;
 	    }
 	    return parsePostExp(e);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Expression parsePostExp(Expression e) {
 		int start = e == null ? prevToken.ptr : e.start;
 		
 		while (true) {
 			switch (token.value) {
 			case TOKdot:
 				nextToken();
 				if (token.value == TOKidentifier) {
 					IdentifierExp id = newIdentifierExp();
 
 					nextToken();
 					if (token.value == TOKnot && peek(token).value == TOKlparen) { 
 						// identifier!(template-argument-list)
 						TemplateInstance tempinst;
 						
 						tempinst = new TemplateInstance(loc(), id);						
 						nextToken();
 						
 						tempinst.tiargs(parseTemplateArgumentList());
 						tempinst.setSourceRange(id.start, prevToken.ptr + prevToken.sourceLen - id.start);
 						e = new DotTemplateInstanceExp(loc(), e, tempinst);
 					} else {
 						e = newDotIdExp(loc(), e, id);
 						e.start = start;
 						e.length = id.start + id.length - start;
 					}
 					continue;
 				} else if (token.value == TOKnew) {
 					e = parseNewExp(e);
 					continue;
 				} else {
 					// signal a new DotIdExp anyway with the token's string representation
 					IdentifierExp fakeId = new IdentifierExp(token.value.charArrayValue);
 					fakeId.start = token.ptr;
 					fakeId.length = token.sourceLen;
 					
 					e = newDotIdExp(loc(), e, fakeId);
 					
 					parsingErrorInsertTokenAfter(prevToken, "Identifier");
 				}
 				break;
 
 			case TOKplusplus:
 			case TOKminusminus:
 				e = new PostExp(loc(), token.value, e);
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				break;
 
 			case TOKlparen:
 				e = newCallExp(loc(), e, parseArguments());
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				continue;
 
 			case TOKlbracket: { // array dereferences:
 				// array[index]
 				// array[]
 				// array[lwr .. upr]
 				Expression index;
 				Expression upr;
 
 				inBrackets++;
 				nextToken();
 				if (token.value == TOKrbracket) { // array[]
 					e = new SliceExp(loc(), e, null, null);
 					nextToken();
 				} else {
 					index = parseAssignExp();
 					if (token.value == TOKslice) { // array[lwr .. upr]
 						nextToken();
 						upr = parseAssignExp();
 						e = new SliceExp(loc(), e, index, upr);
 					} else { // array[index, i2, i3, i4, ...]
 						Expressions arguments = new Expressions();
 						arguments.add(index);
 						if (token.value == TOKcomma) {
 							nextToken();
 							while (true) {
 								Expression arg;
 
 								arg = parseAssignExp();
 								arguments.add(arg);
 								if (token.value == TOKrbracket)
 									break;
 								check(TOKcomma);
 							}
 						}
 						
 						e = new ArrayExp(loc(), e, arguments);
 					}
 					check(TOKrbracket);
 					inBrackets--;
 				}
 				
 				e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 				continue;
 			}
 
 			default:
 				return e;
 			}
			nextToken();
 	    }
 	}
 
 	private Expression parseUnaryExp() {
 		Expression e;
 
 		int start = token.ptr;
 
 		expect(unaryExpExpectations);
 		switch (token.value) {
 		case TOKand:
 			nextToken();
 			e = parseUnaryExp();
 			e = new AddrExp(loc(), e);
 			break;
 
 		case TOKplusplus:
 			nextToken();
 			e = parseUnaryExp();
 			AddAssignExp aae = new AddAssignExp(loc(), e, new IntegerExp(loc(), 1, Type.tint32));
 			aae.isPreIncrement = true;
 			e = aae;
 			break;
 
 		case TOKminusminus:
 			nextToken();
 			e = parseUnaryExp();
 			MinAssignExp mae = new MinAssignExp(loc(), e, new IntegerExp(loc(), 1, Type.tint32));
 			mae.isPreDecrement = true;
 			e = mae;
 			break;
 
 		case TOKmul:
 			nextToken();
 			e = parseUnaryExp();
 			e = new PtrExp(loc(), e);
 			break;
 
 		case TOKmin:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NegExp(loc(), e);
 			break;
 
 		case TOKadd:
 			nextToken();
 			e = parseUnaryExp();
 			e = new UAddExp(loc(), e);
 			break;
 
 		case TOKnot:
 			nextToken();
 			e = parseUnaryExp();
 			e = new NotExp(loc(), e);
 			break;
 
 		case TOKtilde:
 			nextToken();
 			e = parseUnaryExp();
 			e = new ComExp(loc(), e);
 			break;
 
 		case TOKdelete:
 			nextToken();
 			e = parseUnaryExp();
 			e = new DeleteExp(loc(), e);
 			break;
 
 		case TOKnew:
 			e = parseNewExp(null);
 			break;
 
 		case TOKcast: // cast(type) expression
 		{
 			Type t;
 
 			nextToken();
 			check(TOKlparen);
 			
 			if (apiLevel < D2) {
 				t = parseBasicType();
 				t = parseDeclarator(t, null); // ( type )
 				check(TOKrparen);
 	
 				e = parseUnaryExp();
 				e = new CastExp(loc(), e, t);
 			} else {
 			    if ((token.value == TOKconst || token.value == TOKinvariant)
 						&& peek(token).value == TOKrparen) {
 			    	int modifierStart = token.ptr;
 					TOK tok = token.value;
 					nextToken();
 					nextToken();
 					e = parseUnaryExp();
 					e = new CastExp(loc(), e, tok, modifierStart);
 				} else {
 					t = parseType(); // ( type )
 					check(TOKrparen);
 					e = parseUnaryExp();
 					e = new CastExp(loc(), e, t);
 				}
 			}
 			break;
 		}
 
 		case TOKlparen: {
 			Token tk;
 			
 			int firstTokenStart = token.ptr;
 			int firstTokenLine = token.lineNumber;
 
 			tk = peek(token);
 			// #if CCASTSYNTAX
 			// If cast
 			Token[] pointer2_tk = { tk };
 			if (isDeclaration(tk, 0, TOKrparen, pointer2_tk)) {
 				tk = pointer2_tk[0];
 
 				tk = peek(tk); // skip over right parenthesis
 				switch (tk.value) {
 				case TOKdot:
 				case TOKplusplus:
 				case TOKminusminus:
 				case TOKnot:
 				case TOKdelete:
 				case TOKnew:
 				case TOKlparen:
 				case TOKidentifier:
 				case TOKthis:
 				case TOKsuper:
 				case TOKint32v:
 				case TOKuns32v:
 				case TOKint64v:
 				case TOKuns64v:
 				case TOKfloat32v:
 				case TOKfloat64v:
 				case TOKfloat80v:
 				case TOKimaginary32v:
 				case TOKimaginary64v:
 				case TOKimaginary80v:
 				case TOKnull:
 				case TOKtrue:
 				case TOKfalse:
 				case TOKcharv:
 				case TOKwcharv:
 				case TOKdcharv:
 				case TOKstring:
 					/*
 					 * #if 0 case TOKtilde: case TOKand: case TOKmul: case
 					 * TOKmin: case TOKadd: #endif
 					 */
 				case TOKfunction:
 				case TOKdelegate:
 				case TOKtypeof:
 					// CASE_BASIC_TYPES: // (type)int.size
 				case TOKwchar:
 				case TOKdchar:
 				case TOKbit:
 				case TOKbool:
 				case TOKchar:
 				case TOKint8:
 				case TOKuns8:
 				case TOKint16:
 				case TOKuns16:
 				case TOKint32:
 				case TOKuns32:
 				case TOKint64:
 				case TOKuns64:
 				case TOKfloat32:
 				case TOKfloat64:
 				case TOKfloat80:
 				case TOKimaginary32:
 				case TOKimaginary64:
 				case TOKimaginary80:
 				case TOKcomplex32:
 				case TOKcomplex64:
 				case TOKcomplex80:
 				case TOKvoid: { // (type) una_exp
 					Type t;
 
 					nextToken();
 					
 					if (apiLevel < D2) {
 						t = parseBasicType();
 						t = parseDeclarator(t, null);
 					} else {
 						t = parseType();
 					}
 					check(TOKrparen);
 
 					// if .identifier
 					if (token.value == TOKdot) {
 						nextToken();
 						if (token.value != TOKidentifier) {
 							parsingErrorInsertTokenAfter(prevToken, "Identifier");
 							// Change from DMD
 							e = newTypeDotIdExp(loc(), t, null);
 							e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 							return e;
 						}
 						e = newTypeDotIdExp(loc(), t, newIdentifierExp());
 						e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						nextToken();
 						e = parsePostExp(e);
 					} else {
 						e = parseUnaryExp();
 						e = new CastExp(loc(), e, t);
 						e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 						error(IProblem.CStyleCastIllegal, firstTokenLine, firstTokenStart, prevToken.ptr + prevToken.sourceLen - firstTokenStart);
 					}
 					return e;
 				}
 				}
 			}
 			// #endif
 			e = parsePrimaryExp();
 			break;
 		}
 		default:
 			e = parsePrimaryExp();
 			break;
 		}
 		
 		if (e == null) {
 			throw new IllegalStateException();
 		}
 
 		e.setSourceRange(start, prevToken.ptr + prevToken.sourceLen - start);
 		if (e instanceof FuncExp) {
 			((FuncExp) e).fd.setSourceRange(e.start, e.length);
 		}
 
 		return e;
 	}
 
 	private void parsePrimaryExp_case_delegate(Expression[] e, TOK save, boolean isEmptySyntax) {
 		Arguments arguments;
 		int varargs = 0;
 		FuncLiteralDeclaration fd;
 		Type t;
 
 		if (token.value == TOKlcurly) {
 			t = null;
 			varargs = 0;
 			arguments = new Arguments();
 		} else {
 			if (token.value == TOKlparen)
 				t = null;
 			else {
 				t = parseBasicType();
 				t = parseBasicType2(t); // function return type
 			}
 			
 			int[] pointer2_varargs = { varargs };
 			arguments = parseParameters(pointer2_varargs);
 			varargs = pointer2_varargs[0];
 		}
 		
 		t = new TypeFunction(arguments, t, varargs, linkage);
 		fd = new FuncLiteralDeclaration(loc(), t, save, null);
 		
 		
 		parseContracts(fd);
 		e[0] = new FuncExp(loc(), fd);
 		((FuncExp) e[0]).isEmptySyntax = isEmptySyntax;
 	}
 	
 	private Expression parseMulExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseUnaryExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKmul: nextToken(); e2 = parseUnaryExp(); e = newMulExp(loc(), e, e2); continue;
 		    case TOKdiv:   nextToken(); e2 = parseUnaryExp(); e = newDivExp(loc(), e, e2); continue;
 		    case TOKmod:  nextToken(); e2 = parseUnaryExp(); e = newModExp(loc(), e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 
 	private Expression parseAddExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseMulExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKadd:    nextToken(); e2 = parseMulExp(); e = newAddExp(loc(), e, e2); continue;
 		    case TOKmin:    nextToken(); e2 = parseMulExp(); e = newMinExp(loc(), e, e2); continue;
 		    case TOKtilde:  nextToken(); e2 = parseMulExp(); e = newCatExp(loc(), e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 
 	private Expression parseShiftExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseAddExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKshl:  nextToken(); e2 = parseAddExp(); e = newShlExp(loc(), e, e2);  continue;
 		    case TOKshr:  nextToken(); e2 = parseAddExp(); e = newShrExp(loc(), e, e2);  continue;
 		    case TOKushr: nextToken(); e2 = parseAddExp(); e = newUshrExp(loc(), e, e2); continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 
 	private Expression parseRelExp()
 	{   Expression e;
 	    Expression e2;
 	    TOK op;
 
 	    e = parseShiftExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		    case TOKlt:
 		    case TOKle:
 		    case TOKgt:
 		    case TOKge:
 		    case TOKunord:
 		    case TOKlg:
 		    case TOKleg:
 		    case TOKule:
 		    case TOKul:
 		    case TOKuge:
 		    case TOKug:
 		    case TOKue: 
 		    	op = token.value;
 		    	nextToken(); 
 		    	e2 = parseShiftExp();
 		    	e = newCmpExp(loc(), op, e, e2); continue;
 		    case TOKin: 
 		    	nextToken(); 
 		    	e2 = parseShiftExp(); 
 		    	e = newInExp(loc(), e, e2); 
 		    	continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseEqualExp()
 	{   Expression e;
 	    Expression e2;
 	    Token t;
 
 	    e = parseRelExp();
 	    while (true)
 	    {	TOK value = token.value;
 
 		switch (value)
 		{
 		    case TOKequal:
 		    case TOKnotequal:
 		    	nextToken();
 				e2 = parseRelExp();
 				e = newEqualExp(loc(), value, e, e2);
 				continue;
 
 		    case TOKidentity:
 		    	error(
 		    			IProblem.ThreeEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.sourceLen);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(loc(), value, e, e2);
 			continue;
 
 		    case TOKnotidentity:
 		    	error(
 		    			IProblem.NotTwoEqualsIsNoLongerLegal, token.lineNumber, token.ptr, token.sourceLen);
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(loc(), value, e, e2);
 			continue;
 
 		    case TOKis:
 			value = TOKidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(loc(), value, e, e2);
 			continue;
 
 		    case TOKnot:
 			// Attempt to identify '!is'
 			t = peek(token);
 			if (t.value != TOKis)
 			    break;
 			nextToken();
 			value = TOKnotidentity;
 			//goto L1;
 			nextToken();
 			e2 = parseRelExp();
 			e = newIdentityExp(loc(), value, e, e2);
 			continue;
 
 		    // L1:
 			//nextToken();
 			//e2 = parseRelExp();
 			//e = new IdentityExp(value, loc, e, e2);
 			//continue;
 
 		    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 	
 	private Expression parseCmpExp()
 	{   Expression e;
 	    Expression e2;
 	    Token t;
 
 	    e = parseShiftExp();
 	    TOK op = token.value;
 
 	    switch (op)
 	    {
 		case TOKequal:
 		case TOKnotequal:
 			 nextToken();
 			    e2 = parseShiftExp();
 			    e = newEqualExp(loc(), op, e, e2);
 			    break;
 
 		case TOKis:
 		    //op = TOKidentity;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newIdentityExp(loc(), op, e, e2);
 		    break;
 
 		case TOKnot:
 		    // Attempt to identify '!is'
 		    t = peek(token);
 		    if (t.value != TOKis)
 		    	break;
 		    nextToken();
 		    op = TOKnotis;
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newIdentityExp(loc(), op, e, e2);
 		    break;
 		    
 		case TOKlt:
 	    case TOKle:
 	    case TOKgt:
 	    case TOKge:
 	    case TOKunord:
 	    case TOKlg:
 	    case TOKleg:
 	    case TOKule:
 	    case TOKul:
 	    case TOKuge:
 	    case TOKug:
 	    case TOKue:
 	    	nextToken(); 
 	    	e2 = parseShiftExp(); 
 	    	e = newCmpExp(loc(), op, e, e2); 
 	    	break;
 
 		case TOKin:
 		    nextToken();
 		    e2 = parseShiftExp();
 		    e = newInExp(loc(), e, e2);
 		    break;
 
 		default:
 		    break;
 	    }
 	    return e;
 	}
 
 	private Expression parseAndExp() {
 		Expression e;
 		Expression e2;
 
 		if (apiLevel == D0) {
 			e = parseEqualExp();
 			while (token.value == TOKand) {
 				nextToken();
 				e2 = parseEqualExp();
 				e = newAndExp(loc(), e, e2);
 			}
 		} else {
 			e = parseCmpExp();
 			while (token.value == TOKand)
 			{
 			    nextToken();
 			    e2 = parseCmpExp();
 			    e = newAndExp(loc(), e, e2);
 			}
 
 		}
 		return e;
 	}
 
 	private Expression parseXorExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseAndExp();
 		while (token.value == TOKxor) {
 			nextToken();
 			e2 = parseAndExp();
 			e = newXorExp(loc(), e, e2);
 		}
 		return e;
 	}
 
 	private Expression parseOrExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseXorExp();
 		while (token.value == TOKor) {
 			nextToken();
 			e2 = parseXorExp();
 			e = newOrExp(loc(), e, e2);
 		}
 		return e;
 	}
 
 	private Expression parseAndAndExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseOrExp();
 		while (token.value == TOKandand) {
 			nextToken();
 			e2 = parseOrExp();
 			e = newAndAndExp(loc(), e, e2);
 		}
 		return e;
 	}
 
 	private Expression parseOrOrExp() {
 		Expression e;
 		Expression e2;
 
 		e = parseAndAndExp();
 		while (token.value == TOKoror) {
 			nextToken();
 			e2 = parseAndAndExp();
 			e = newOrOrExp(loc(), e, e2);
 		}
 		return e;
 	}
 
 	private Expression parseCondExp() {
 		Expression e;
 		Expression e1;
 		Expression e2;
 
 		e = parseOrOrExp();
 		if (token.value == TOKquestion) {
 			nextToken();
 			e1 = parseExpression();
 			check(TOKcolon);
 			e2 = parseCondExp();
 			e = newCondExp(loc(), e, e1, e2);
 		}
 		return e;
 	}
 
 	private Expression parseAssignExp()
 	{   Expression e;
 	    Expression e2;
 
 	    e = parseCondExp();
 	    while (true)
 	    {
 		switch (token.value)
 		{
 		case TOKassign:  nextToken(); e2 = parseAssignExp(); e = newAssignExp(loc(), e, e2); continue;
 		case TOKaddass:  nextToken(); e2 = parseAssignExp(); e = newAddAssignExp(loc(), e, e2); continue;
 		case TOKminass:  nextToken(); e2 = parseAssignExp(); e = newMinAssignExp(loc(), e, e2); continue;
 		case TOKmulass:  nextToken(); e2 = parseAssignExp(); e = newMulAssignExp(loc(), e, e2); continue;
 		case TOKdivass:  nextToken(); e2 = parseAssignExp(); e = newDivAssignExp(loc(), e, e2); continue;
 		case TOKmodass:  nextToken(); e2 = parseAssignExp(); e = newModAssignExp(loc(), e, e2); continue;
 		case TOKandass:  nextToken(); e2 = parseAssignExp(); e = newAndAssignExp(loc(), e, e2); continue;
 		case TOKorass:  nextToken(); e2 = parseAssignExp(); e = newOrAssignExp(loc(), e, e2); continue;
 		case TOKxorass:  nextToken(); e2 = parseAssignExp(); e = newXorAssignExp(loc(), e, e2); continue;
 		case TOKshlass:  nextToken(); e2 = parseAssignExp(); e = newShlAssignExp(loc(), e, e2); continue;
 		case TOKshrass:  nextToken(); e2 = parseAssignExp(); e = newShrAssignExp(loc(), e, e2); continue;
 		case TOKushrass:  nextToken(); e2 = parseAssignExp(); e = newUshrAssignExp(loc(), e, e2); continue;
 		case TOKcatass:  nextToken(); e2 = parseAssignExp(); e = newCatAssignExp(loc(), e, e2); continue;
 	    default:
 			break;
 		}
 		break;
 	    }
 	    return e;
 	}
 
 	public Expression parseExpression() {
 		Expression e;
 		Expression e2;
 
 		e = parseAssignExp();
 		while (token.value == TOKcomma) {
 			nextToken();
 			e2 = parseAssignExp();
 			e = new CommaExp(loc(), e, e2);
 		}
 		return e;
 	}
 	
 	/***************************************************************************
 	 * Collect argument list. Assume current token is '('.
 	 */
 	
 	@SuppressWarnings("unchecked")
 	private Expressions parseArguments() {
 		// function call
 		Expressions arguments;
 		Expression arg;
 		TOK endtok;
 
 		arguments = new Expressions();
 		if (token.value == TOKlbracket) {
 			endtok = TOKrbracket;
 		} else {
 			endtok = TOKrparen;
 		}
 
 		{
 			nextToken();
 			if (token.value != endtok) {
 				while (true) {
 					arg = parseAssignExp();
 					arguments.add(arg);
 					if (token.value == endtok)
 						break;
 					if (token.value == TOKeof) 
 						return arguments;
 					check(TOKcomma);
 				}
 			}
 			check(endtok);
 		}
 		return arguments;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private Expression parseNewExp(Expression thisexp) {
 		int start = token.ptr;
 		
 		Type t;
 		Expressions newargs = null;
 		Expressions arguments = null;
 		Expression e;
 
 		nextToken();
 		if (token.value == TOKlparen) {
 			newargs = parseArguments();
 		}
 		
 		expect(classExpectations);
 
 		// An anonymous nested class starts with "class"
 		if (token.value == TOKclass) {
 			nextToken();
 			if (token.value == TOKlparen)
 				arguments = parseArguments();
 
 			BaseClasses baseClasses = null;
 			if (token.value != TOKlcurly)
 				baseClasses = parseBaseClasses();
 
 			IdentifierExp id = null;
 			ClassDeclaration cd = new ClassDeclaration(loc(), id, baseClasses);
 
 			if (token.value != TOKlcurly) {
 				parsingErrorInsertToComplete(prevToken, "{ members }", "AnnonymousClassDeclaration");
 				cd.members = null;
 			} else {
 				nextToken();
 				Dsymbols decl = parseDeclDefs(false);
 				if (token.value != TOKrcurly) {
 					parsingErrorInsertToComplete(prevToken, "ClassBody", "AnnonymousClassDeclaration");
 				}
 				nextToken();
 				cd.members = decl;
 				cd.sourceMembers = new Dsymbols(decl);
 			}
 			
 			e = new NewAnonClassExp(loc(), thisexp, newargs, cd, arguments);
 
 			return e;
 		}
 
 		// #if LTORARRAYDECL
 		int lineNumber = token.lineNumber;
 		t = parseBasicType();
 		t = parseBasicType2(t);
 		if (t != null) {
 			if (t.ty == Taarray) {
 				Type index = ((TypeAArray) t).index;
 				
 				Expression e2 = index.toExpression();
 				if (e2 != null) {
 					arguments = new Expressions();
 					arguments.add(e2);				
 					t = new TypeDArray(t.next);
 				} else {
 					error(IProblem.NeedSizeOfRightmostArray, lineNumber, index);
 					NullExp nullExp = new NullExp(loc());
 					nullExp.setSourceRange(token.ptr, token.sourceLen);
 					return nullExp;
 				}
 			} else if (t.ty == Tsarray) {
 				TypeSArray tsa = (TypeSArray) t;
 				Expression e2 = tsa.dim;
 	
 				arguments = new Expressions();
 				arguments.add(e2);
 				
 				t = new TypeDArray(t.next);
 			} else if (token.value == TOKlparen) {
 				arguments = parseArguments();
 			}
 		}
 		/*
 		 * #else t = parseBasicType(); while (token.value == TOKmul) { t = new
 		 * TypePointer(t); nextToken(); } if (token.value == TOKlbracket) {
 		 * Expression *e;
 		 * 
 		 * nextToken(); e = parseAssignExp(); arguments = new Array();
 		 * arguments->push(e); check(TOKrbracket); t = parseDeclarator(t, NULL);
 		 * t = new TypeDArray(t); } else if (token.value == TOKlparen) arguments =
 		 * parseArguments(); #endif
 		 */
 		e = newNewExp(loc(), thisexp, newargs, t, arguments, start);
 		return e;
 	}
 
 	private StringExp newStringExpForCurrentToken() {
 		StringExp string = new StringExp(loc(), token.ustring, token.len, (char) token.postfix);
 		string.sourceString = token.sourceString;
 		string.setSourceRange(token.ptr, token.sourceLen);
 		return string;
 	}
 	
 	private StringExp newStringExpForPreviousToken() {
 		StringExp string = new StringExp(loc(), prevToken.ustring, prevToken.len, (char) prevToken.postfix);
 		string.sourceString = prevToken.sourceString;
 		string.setSourceRange(prevToken.ptr, prevToken.sourceLen);
 		return string;
 	}
 	
 	private TypeAArray newTypeAArray(Type componentType, Type keyType) {
 		TypeAArray associativeArray = new TypeAArray(componentType, keyType);
 	    associativeArray.setSourceRange(componentType.start, token.ptr + token.sourceLen - componentType.start);
 	    return associativeArray;
 	}
 	
 	protected CompoundStatement newBlock(Statements statements, int start, int length) {
 		CompoundStatement cs = new CompoundStatement(loc(), statements);
 		cs.setSourceRange(start, length);
 		return cs;
 	}
 	
 	private CompoundStatement newManyVarsBlock(Statements statements) {
 		CompoundStatement statement = new CompoundStatement(loc(), statements);
 		statement.manyVars = true;
 		return statement;
 	}
 	
 	private DebugSymbol newDebugAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return new DebugSymbol(loc(), token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return newDebugSymbol(loc(), new IdentifierExp(loc(), token.sourceString), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}
 	
 	private Version newVersionForCurrentToken() {
 		Version version;
 		
 		if (token.value == TOKint32v) {
 			version = new Version(loc(), token.sourceString);
 			version.setSourceRange(token.ptr, token.sourceLen);
 		} else if (token.value == TOKidentifier) {
 			version = new Version(loc(), token.sourceString);
 			version.setSourceRange(token.ptr, token.sourceLen);
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 		
 		return version;
 	}
 	
 	private VersionSymbol newVersionAssignmentForCurrentToken() {
 		if (token.value == TOKint32v) {
 			return new VersionSymbol(loc(), token.intValue.longValue(), newVersionForCurrentToken());
 		} else if (token.value == TOKidentifier) {
 			return newVersionSymbol(loc(), newIdentifierExp(), newVersionForCurrentToken());
 		} else {
 			throw new RuntimeException("Can't happen");
 		}
 	}
 
 	private VoidInitializer newVoidInitializerForToken(Token token) {
 		VoidInitializer voidInitializer = new VoidInitializer(loc());
 		voidInitializer.setSourceRange(token.ptr, token.sourceLen);
 		return voidInitializer;
 	}
 	
 	protected Type newTypeBasicForCurrentToken() {
 		TypeBasic t = null;
 		switch(token.value) {
 		case TOKvoid: t = new TypeBasic(Type.tvoid); break;
 		case TOKint8: t = new TypeBasic(Type.tint8); break;
 		case TOKuns8: t = new TypeBasic(Type.tuns8); break;
 		case TOKint16: t = new TypeBasic(Type.tint16); break;
 		case TOKuns16: t = new TypeBasic(Type.tuns16); break;
 		case TOKint32: t = new TypeBasic(Type.tint32); break;
 		case TOKuns32: t = new TypeBasic(Type.tuns32); break;
 		case TOKint64: t = new TypeBasic(Type.tint64); break;
 		case TOKuns64: t = new TypeBasic(Type.tuns64); break;
 		case TOKfloat32: t = new TypeBasic(Type.tfloat32); break;
 		case TOKfloat64: t = new TypeBasic(Type.tfloat64); break;
 		case TOKfloat80: t = new TypeBasic(Type.tfloat80); break;
 		case TOKimaginary32: t = new TypeBasic(Type.timaginary32); break;
 		case TOKimaginary64: t = new TypeBasic(Type.timaginary64); break;
 		case TOKimaginary80: t = new TypeBasic(Type.timaginary80); break;
 		case TOKcomplex32: t = new TypeBasic(Type.tcomplex32); break;
 		case TOKcomplex64: t = new TypeBasic(Type.tcomplex64); break;
 		case TOKcomplex80: t = new TypeBasic(Type.tcomplex80); break;
 		case TOKbit: t = new TypeBasic(Type.tbit); break;
 		case TOKbool: t = new TypeBasic(Type.tbool); break;
 		case TOKchar: t = new TypeBasic(Type.tchar); break;
 		case TOKwchar: t = new TypeBasic(Type.twchar); break;
 		case TOKdchar: t = new TypeBasic(Type.tdchar); break;
 		}
 		t.setSourceRange(token.ptr, token.sourceLen);
 		return t;
 	}
 	
 	private List<Comment> getLastComments() {
 		if (comments == null) {
 			return Collections.emptyList();
 		}
 		
 		LinkedList<Comment> toReturn = new LinkedList<Comment>();
 		for(int i = comments.size() - 1; i >= lastCommentRead; i--) {
 			Comment comment = comments.get(i);
 			toReturn.addFirst(comment);
 		}
 		
 		lastCommentRead = comments.size();		
 		return toReturn;
 	}
 	
 	private void attachLeadingComments(ASTDmdNode declaration) {
 		if (prevToken.leadingComment != null) {
 			declaration.postComment = prevToken.leadingComment;
 		}
 	}
 	
 	private void parsingErrorInsertTokenAfter(Token targetToken, String expected) {
 		error(IProblem.ParsingErrorInsertTokenAfter, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { new String(targetToken.toString()), expected });
 	}
 	
 	private void parsingErrorDeleteToken(Token targetToken) {
 		error(IProblem.ParsingErrorDeleteToken, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { targetToken.toString() });
 	}
 	
 	private void parsingErrorInsertToComplete(Token targetToken, String insert, String toComplete) {
 		error(IProblem.ParsingErrorInsertToComplete, targetToken.lineNumber, targetToken.ptr, targetToken.sourceLen, new String[] { insert, toComplete });
 	}
 	
 	private String toWord(String s) {
 		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
 	}
 	
 	@Override
 	public TOK nextToken() {
 		// In diet mode we don't care about comments and pragmas
 		if (inDiet) {
 			TOK tok = super.nextToken();
 			while(tok == TOKlinecomment || tok == TOKdoclinecomment ||
 				  tok == TOKblockcomment || tok == TOKdocblockcomment ||
 				  tok == TOKpluscomment || tok == TOKdocpluscomment ||
 				  tok == TOKPRAGMA) {
 				tok = super.nextToken();
 			}
 			return tok;
 		}
 		
 		appendLeadingComments = true;
 		
 		// If many comments come in a rush, only the first
 		// one is to be attached to the previous token as a
 		// leading comment
 		boolean first = true;
 		TOK tok = super.nextToken();
 		while(tok == TOKlinecomment || tok == TOKdoclinecomment ||
 			  tok == TOKblockcomment || tok == TOKdocblockcomment ||
 			  tok == TOKpluscomment || tok == TOKdocpluscomment) {
 			
 			inComment();
 			
 			Comment comment;
 			switch(tok) {
 			case TOKlinecomment:
 				comment = new Comment(Comment.LINE_COMMENT, token.sourceString);
 				break;
 			case TOKblockcomment:
 				comment = new Comment(Comment.BLOCK_COMMENT, token.sourceString);
 				break;
 			case TOKpluscomment:
 				comment = new Comment(Comment.PLUS_COMMENT, token.sourceString);
 				break;
 			case TOKdoclinecomment:
 				comment = new Comment(Comment.DOC_LINE_COMMENT, token.sourceString);
 				break;
 			case TOKdocblockcomment:
 				comment = new Comment(Comment.DOC_BLOCK_COMMENT, token.sourceString);
 				break;
 			case TOKdocpluscomment:
 				comment = new Comment(Comment.DOC_PLUS_COMMENT, token.sourceString);
 				break;
 			default:
 				throw new IllegalStateException("Can't happen");
 			}
 			
 			comment.setSourceRange(token.ptr, token.sourceLen);
 			
 			comments.add(comment);
 			if (first) {
 				attachCommentToCurrentToken(comment);
 			}
 			
 			tok = super.nextToken();
 			first = false;
 		}
 		
 		while(tok == TOKPRAGMA) {
 			if (token.ptr == 0 && token.sourceString.length > 1 && token.sourceString[1] == '!') {
 				// Script line
 				Pragma pragma = new Pragma();
 				pragma.setSourceRange(0, token.sourceLen);
 				pragmas.add(pragma);
 			} else {
 				Pragma pragma = new Pragma();
 				pragma.setSourceRange(token.ptr, token.sourceLen);
 				pragmas.add(pragma);
 				
 				// Let's see if it's correct
 				// TODO improve performance
 				StringTokenizer st = new StringTokenizer(new String(token.sourceString).substring(1));
 				int count = st.countTokens();
 				if (count <= 1 || count >= 4) {
 					error(IProblem.InvalidPragmaSyntax, token);
 					setMalformed(pragma);
 				} else {
 					String value = st.nextToken();
 					if (!"line".equals(value)) {
 						error(IProblem.InvalidPragmaSyntax, token);
 						setMalformed(pragma);
 					} else {
 						value = st.nextToken();
 						try {
 							if (!"__LINE__".equals(value)) {
 								int num = Integer.parseInt(value);
 								if (num < 0) throw new NumberFormatException();
 							}
 							
 							if (st.hasMoreTokens()) {
 								value = st.nextToken();
 								if (!"__FILE__".equals(value)) {
 									if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
 										error(IProblem.InvalidPragmaSyntax, token);
 										setMalformed(pragma);
 									}
 								}
 							}
 						} catch (NumberFormatException e) {
 							error(IProblem.InvalidPragmaSyntax, token);
 							setMalformed(pragma);
 						}
 					}
 				}
 			}
 			
 			tok = nextToken();
 		}
 		
 		return tok;
 	}
 
 	@Override
 	protected void newline(boolean inComment) {
 		super.newline(inComment);
 		if (!inComment) {
 			appendLeadingComments = false;
 		}
 	}
 	
 	protected IdentifierExp newIdentifierExp() {
 		return new IdentifierExp(loc(), token);
 	}
 	
 	private void attachCommentToCurrentToken(Comment comment) {
 		if ((!appendLeadingComments || 
 					//!comment.isDDocComment() || 
 					(prevToken.value != TOKsemicolon && 
 						prevToken.value != TOKrcurly &&
 						prevToken.value != TOKcomma))) return;
 		
 		if (prevToken.leadingComment == null) {
 			lastCommentRead = comments.size();	
 		}
 		prevToken.leadingComment = comment;		
 	}
 	
 	private void discardLastComments() {
 		if (this.comments != null) {
 			this.lastCommentRead = this.comments.size();
 		}
 	}
 	
 	private Loc loc() {
 		return new Loc(filename, linnum);
 	}
 	
 	/**
 	 * Creates a ModuleDeclaration with the given packages and module name. The
 	 * current token is the token following the last token of the module declaration.
 	 * packages and module can be null at the same time, meaning "module " is written
 	 * in the source code.
 	 */
 	protected ModuleDeclaration newModuleDeclaration(Identifiers packages, IdentifierExp module) {
 		return new ModuleDeclaration(packages, module);
 	}
 	
 	protected Import newImport(Loc loc, Identifiers packages, IdentifierExp module, IdentifierExp aliasid, boolean isstatic) {
 		return new Import(loc, packages, module, aliasid, isstatic);
 	}
 	
 	protected Argument newArgument(int storageClass, Type at, IdentifierExp ai, Expression ae) {
 		return new Argument(storageClass, at, ai, ae);
 	}
 	
 	protected GotoStatement newGotoStatement(Loc loc, IdentifierExp ident) {
 		return new GotoStatement(loc, ident);
 	}
 	
 	protected ContinueStatement newContinueStatement(Loc loc, IdentifierExp ident) {
 		return new ContinueStatement(loc, ident);
 	}
 
 	protected BreakStatement newBreakStatement(Loc loc, IdentifierExp ident) {
 		return new BreakStatement(loc, ident);
 	}
 	
 	protected VersionCondition newVersionCondition(Module module, Loc loc, long level, char[] id) {
 		return new VersionCondition(module, loc, level, id);
 	}
 	
 	protected VersionSymbol newVersionSymbol(Loc loc, IdentifierExp id, Version version) {
 		return new VersionSymbol(loc, id, version);
 	}
 	
 	protected DebugCondition newDebugCondition(Module module, Loc loc, long level, char[] id) {
 		return new DebugCondition(module, loc, level, id);
 	}
 	
 	protected DebugSymbol newDebugSymbol(Loc loc, IdentifierExp id, Version version) {
 		return new DebugSymbol(loc, id, version);
 	}
 	
 	protected CaseStatement newCaseStatement(Loc loc, Expression exp, Statement statement, int caseEnd, int expStart, int expLength) {
 		return new CaseStatement(loc, exp, statement);
 	}
 	
 	protected TypeDotIdExp newTypeDotIdExp(Loc loc, Type t, IdentifierExp exp) {
 		return new TypeDotIdExp(loc, t, exp);
 	}
 	
 	protected DotIdExp newDotIdExp(Loc loc, Expression e, IdentifierExp id) {
 		return new DotIdExp(loc, e, id);
 	}
 	
 	protected TypeQualified newTypeIdentifier(Loc loc, IdentifierExp id) {
 		return new TypeIdentifier(loc, id);
 	}
 	
 	protected ExpStatement newExpStatement(Loc loc, Expression exp) {
 		return new ExpStatement(loc, exp);
 	}
 	
 	protected SuperExp newSuperExp(Loc loc) {
 		return new SuperExp(loc);
 	}
 
 	protected ThisExp newThisExp(Loc loc) {
 		return new ThisExp(loc);
 	}
 	
 	protected Expression newAssignExp(Loc loc, Expression e, Expression e2) {
 		return new AssignExp(loc, e, e2);
 	}
 
 	protected Expression newAddAssignExp(Loc loc, Expression e, Expression e2) {
 		return new AddAssignExp(loc, e, e2);
 	}
 
 	protected Expression newMinAssignExp(Loc loc, Expression e, Expression e2) {
 		return new MinAssignExp(loc, e, e2);
 	}
 
 	protected Expression newMulAssignExp(Loc loc, Expression e, Expression e2) {
 		return new MulAssignExp(loc, e, e2);
 	}
 
 	protected Expression newDivAssignExp(Loc loc, Expression e, Expression e2) {
 		return new DivAssignExp(loc, e, e2);
 	}
 
 	protected Expression newModAssignExp(Loc loc, Expression e, Expression e2) {
 		return new ModAssignExp(loc, e, e2);
 	}
 
 	protected Expression newAndAssignExp(Loc loc, Expression e, Expression e2) {
 		return new AndAssignExp(loc, e, e2);
 	}
 
 	protected Expression newOrAssignExp(Loc loc, Expression e, Expression e2) {
 		return new OrAssignExp(loc, e, e2);
 	}
 
 	protected Expression newXorAssignExp(Loc loc, Expression e, Expression e2) {
 		return new XorAssignExp(loc, e, e2);
 	}
 
 	protected Expression newShlAssignExp(Loc loc, Expression e, Expression e2) {
 		return new ShlAssignExp(loc, e, e2);
 	}
 
 	protected Expression newShrAssignExp(Loc loc, Expression e, Expression e2) {
 		return new ShrAssignExp(loc, e, e2);
 	}
 
 	protected Expression newUshrAssignExp(Loc loc, Expression e, Expression e2) {
 		return new UshrAssignExp(loc, e, e2);
 	}
 
 	protected Expression newCatAssignExp(Loc loc, Expression e, Expression e2) {
 		return new CatAssignExp(loc, e, e2);
 	}
 	
 	protected Expression newCondExp(Loc loc, Expression e, Expression e1, Expression e2) {
 		return new CondExp(loc, e, e1, e2);
 	}
 	
 	protected Expression newOrOrExp(Loc loc, Expression e, Expression e2) {
 		return new OrOrExp(loc, e, e2);
 	}
 	
 	protected Expression newAndExp(Loc loc, Expression e, Expression e2) {
 		return new AndExp(loc(), e, e2);
 	}
 	
 	protected Expression newOrExp(Loc loc, Expression e, Expression e2) {
 		return new OrExp(loc(), e, e2);
 	}
 	
 	protected Expression newAndAndExp(Loc loc, Expression e, Expression e2) {
 		return new AndAndExp(loc(), e, e2);
 	}
 	
 	protected Expression newXorExp(Loc loc, Expression e, Expression e2) {
 		return new XorExp(loc(), e, e2);
 	}
 	
 	protected Expression newEqualExp(Loc loc, TOK op, Expression e, Expression e2) {
 		return new EqualExp(loc(), op, e, e2);
 	}
 	
 	protected Expression newIdentityExp(Loc loc, TOK op, Expression e, Expression e2) {
 		return new IdentityExp(loc(), op, e, e2);
 	}
 	
 	protected Expression newCmpExp(Loc loc, TOK op, Expression e, Expression e2) {
 		return new CmpExp(loc(), op, e, e2);
 	}
 	
 	protected Expression newInExp(Loc loc, Expression e, Expression e2) {
 		return new InExp(loc(), e, e2);
 	}
 	
 	protected Expression newShlExp(Loc loc, Expression e, Expression e2) {
 		return new ShlExp(loc, e, e2);
 	}
 
 	protected Expression newShrExp(Loc loc, Expression e, Expression e2) {
 		return new ShrExp(loc, e, e2);
 	}
 
 	protected Expression newUshrExp(Loc loc, Expression e, Expression e2) {
 		return new UshrExp(loc, e, e2);
 	}
 	
 	protected Expression newAddExp(Loc loc, Expression e, Expression e2) {
 		return new AddExp(loc, e, e2);
 	}
 
 	protected Expression newMinExp(Loc loc, Expression e, Expression e2) {
 		return new MinExp(loc, e, e2);
 	}
 
 	protected Expression newCatExp(Loc loc, Expression e, Expression e2) {
 		return new CatExp(loc, e, e2);
 	}
 	
 	protected Expression newModExp(Loc loc, Expression e, Expression e2) {
 		return new ModExp(loc, e, e2);
 	}
 
 	protected Expression newDivExp(Loc loc, Expression e, Expression e2) {
 		return new DivExp(loc, e, e2);
 	}
 
 	protected Expression newMulExp(Loc loc, Expression e, Expression e2) {
 		return new MulExp(loc, e, e2);
 	}
 	
 	protected VarDeclaration newVarDeclaration(Loc loc, Type type, IdentifierExp ident, Initializer init) {
 		return new VarDeclaration(loc, type, ident, init);
 	}
 	
 	protected Expression newCallExp(Loc loc, Expression e, Expressions expressions) {
 		return new CallExp(loc, e, expressions);
 	}
 	
 	protected Expression newNewExp(Loc loc, Expression thisexp, Expressions newargs, Type t, Expressions arguments, int start) {
 		return new NewExp(loc, thisexp, newargs, t, arguments);
 	}
 	
 	protected Statement newReturnStatement(Loc loc, Expression exp) {
 		return new ReturnStatement(loc, exp);
 	}
 	
 	protected AggregateDeclaration newUnionDeclaration(Loc loc, IdentifierExp id) {
 		return new UnionDeclaration(loc, id);
 	}
 
 	protected AggregateDeclaration newStructDeclaration(Loc loc, IdentifierExp id) {
 		return new StructDeclaration(loc, id);
 	}
 
 	protected AggregateDeclaration newInterfaceDeclaration(Loc loc, IdentifierExp id, BaseClasses baseClasses) {
 		return new InterfaceDeclaration(loc, id, baseClasses);
 	}
 
 	protected AggregateDeclaration newClassDeclaration(Loc loc, IdentifierExp id, BaseClasses baseClasses) {
 		return new ClassDeclaration(loc, id, baseClasses);
 	}
 	
 	private Statement dietParseStatement(FuncDeclaration f) {
 		if (diet) {
 			int saveP = p;
 			Token saveToken = new Token(token);
 			
 			inDiet = true;
 			boolean success = dietParse(f);					
 			inDiet = false;
 			
 			if (success) {
 				return null;
 			}
 			
 			p = saveP;
 			token = saveToken;
 		}
 		
 		return parseStatement(PSsemi);
 	}
 	
 	/**
 	 * Tries to skip a function body. The current token is '{'.
 	 * @param f the target function whoes body is being skipped
 	 * @return <code>true</code> if the body could be skipped,
 	 * or <code>false</code> if not, and thus function body
 	 * will be parsed (by the method that invoked this method).
 	 * 
 	 * TODO try doing this at the character level to improve performance further
 	 */
 	protected boolean dietParse(FuncDeclaration f) {
 		int curlyCount = 1;
 		int parenCount = 0;
 		TOK save = null;
 		TOK prev;
 		TOK tok = token.value;
 		do {
 			prev = tok;
 			tok = nextToken();
 			switch(tok) {
 			// Any of this implies an inner-declaration
 			case TOKclass:
 			case TOKinterface:
 			case TOKstruct:
 			case TOKunion:
 				return false;
 				
 			// Parenthesis counting:
 			// If we found a parenthesis for the first time, remember
 			// which token was before it. If after the closing parenthesis
 			// comes '{', we check what that token was. If it is an
 			// identifier, then this is probably an inner-function declaration.
 			case TOKlparen:
 				if (parenCount == 0) {
 					save = prev;
 				}
 				parenCount++;
 				break;
 			case TOKrparen:
 				parenCount--;
 				if (parenCount == 0) {
 					if (save == TOKidentifier &&
 						peek(token).value == TOK.TOKlcurly) {
 						return false;
 					}
 				}
 				break;
 			
 			// Curly counting
 			case TOKlcurly:
 				curlyCount++;
 				break;
 			case TOKrcurly:
 				curlyCount--;
 				break;
 			}
 		} while(curlyCount != 0 && tok != TOK.TOKeof);
 		
 		inDiet = false;
 		
 		nextToken();
 		
 		return true;
 	}
 	
 	/**
 	 * The parser expects the current token to be
 	 * any of given tokens. Does nothing. Subclases may override.
 	 */
 	protected void expect(char[][] tok) {
 		
 	}
 	
 	/**
 	 * The current token is a comment.
 	 *
 	 */
 	protected void inComment() {
 	}
 
 	
 }
