 package descent.internal.codeassist;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import descent.core.CompletionContext;
 import descent.core.CompletionProposal;
 import descent.core.CompletionRequestor;
 import descent.core.Flags;
 import descent.core.IJavaProject;
 import descent.core.JavaModelException;
 import descent.core.Signature;
 import descent.core.compiler.CharOperation;
 import descent.core.ddoc.Ddoc;
 import descent.core.ddoc.DdocMacros;
 import descent.core.ddoc.DdocParser;
 import descent.core.ddoc.DdocSection;
 import descent.core.ddoc.DdocSection.Parameter;
 import descent.core.dom.CompilationUnitResolver;
 import descent.internal.codeassist.complete.CompletionOnArgumentName;
 import descent.internal.codeassist.complete.CompletionOnBreakStatement;
 import descent.internal.codeassist.complete.CompletionOnCallExp;
 import descent.internal.codeassist.complete.CompletionOnCaseStatement;
 import descent.internal.codeassist.complete.CompletionOnClassDeclaration;
 import descent.internal.codeassist.complete.CompletionOnCompoundStatement;
 import descent.internal.codeassist.complete.CompletionOnContinueStatement;
 import descent.internal.codeassist.complete.CompletionOnDebugCondition;
 import descent.internal.codeassist.complete.CompletionOnDotIdExp;
 import descent.internal.codeassist.complete.CompletionOnExpStatement;
 import descent.internal.codeassist.complete.CompletionOnGotoStatement;
 import descent.internal.codeassist.complete.CompletionOnIdentifierExp;
 import descent.internal.codeassist.complete.CompletionOnImport;
 import descent.internal.codeassist.complete.CompletionOnInterfaceDeclaration;
 import descent.internal.codeassist.complete.CompletionOnJavadocImpl;
 import descent.internal.codeassist.complete.CompletionOnModuleDeclaration;
 import descent.internal.codeassist.complete.CompletionOnNewExp;
 import descent.internal.codeassist.complete.CompletionOnReturnStatement;
 import descent.internal.codeassist.complete.CompletionOnSuperDotExp;
 import descent.internal.codeassist.complete.CompletionOnTemplateMixin;
 import descent.internal.codeassist.complete.CompletionOnThisDotExp;
 import descent.internal.codeassist.complete.CompletionOnTypeDotIdExp;
 import descent.internal.codeassist.complete.CompletionOnTypeIdentifier;
 import descent.internal.codeassist.complete.CompletionOnVersionCondition;
 import descent.internal.codeassist.complete.CompletionParser;
 import descent.internal.codeassist.complete.ICompletionOnKeyword;
 import descent.internal.codeassist.impl.Engine;
 import descent.internal.compiler.env.AccessRestriction;
 import descent.internal.compiler.env.ICompilationUnit;
 import descent.internal.compiler.parser.ASTDmdNode;
 import descent.internal.compiler.parser.ASTNodeEncoder;
 import descent.internal.compiler.parser.AggregateDeclaration;
 import descent.internal.compiler.parser.AliasDeclaration;
 import descent.internal.compiler.parser.Argument;
 import descent.internal.compiler.parser.Arguments;
 import descent.internal.compiler.parser.AttribDeclaration;
 import descent.internal.compiler.parser.BaseClass;
 import descent.internal.compiler.parser.BaseClasses;
 import descent.internal.compiler.parser.CallExp;
 import descent.internal.compiler.parser.CaseStatement;
 import descent.internal.compiler.parser.CastExp;
 import descent.internal.compiler.parser.Chars;
 import descent.internal.compiler.parser.ClassDeclaration;
 import descent.internal.compiler.parser.CommaExp;
 import descent.internal.compiler.parser.CompoundStatement;
 import descent.internal.compiler.parser.ConditionalDeclaration;
 import descent.internal.compiler.parser.CtorDeclaration;
 import descent.internal.compiler.parser.DVCondition;
 import descent.internal.compiler.parser.Declaration;
 import descent.internal.compiler.parser.DotExp;
 import descent.internal.compiler.parser.DotVarExp;
 import descent.internal.compiler.parser.Dsymbol;
 import descent.internal.compiler.parser.DsymbolExp;
 import descent.internal.compiler.parser.DsymbolTable;
 import descent.internal.compiler.parser.Dsymbols;
 import descent.internal.compiler.parser.EnumDeclaration;
 import descent.internal.compiler.parser.EnumMember;
 import descent.internal.compiler.parser.ErrorExp;
 import descent.internal.compiler.parser.Expression;
 import descent.internal.compiler.parser.FuncAliasDeclaration;
 import descent.internal.compiler.parser.FuncDeclaration;
 import descent.internal.compiler.parser.FuncLiteralDeclaration;
 import descent.internal.compiler.parser.HashtableOfCharArrayAndObject;
 import descent.internal.compiler.parser.Id;
 import descent.internal.compiler.parser.IdentifierExp;
 import descent.internal.compiler.parser.Import;
 import descent.internal.compiler.parser.IndexExp;
 import descent.internal.compiler.parser.IntegerExp;
 import descent.internal.compiler.parser.InterfaceDeclaration;
 import descent.internal.compiler.parser.InvariantDeclaration;
 import descent.internal.compiler.parser.LINK;
 import descent.internal.compiler.parser.LabelStatement;
 import descent.internal.compiler.parser.Loc;
 import descent.internal.compiler.parser.MATCH;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.NewExp;
 import descent.internal.compiler.parser.Package;
 import descent.internal.compiler.parser.PtrExp;
 import descent.internal.compiler.parser.STC;
 import descent.internal.compiler.parser.Scope;
 import descent.internal.compiler.parser.ScopeDsymbol;
 import descent.internal.compiler.parser.ScopeExp;
 import descent.internal.compiler.parser.SemanticContext;
 import descent.internal.compiler.parser.Statement;
 import descent.internal.compiler.parser.StaticDtorDeclaration;
 import descent.internal.compiler.parser.StringExp;
 import descent.internal.compiler.parser.StructDeclaration;
 import descent.internal.compiler.parser.SuperExp;
 import descent.internal.compiler.parser.SwitchStatement;
 import descent.internal.compiler.parser.SymOffExp;
 import descent.internal.compiler.parser.TOK;
 import descent.internal.compiler.parser.TY;
 import descent.internal.compiler.parser.TemplateDeclaration;
 import descent.internal.compiler.parser.TemplateExp;
 import descent.internal.compiler.parser.TemplateInstance;
 import descent.internal.compiler.parser.TemplateMixin;
 import descent.internal.compiler.parser.ThisExp;
 import descent.internal.compiler.parser.Type;
 import descent.internal.compiler.parser.TypeAArray;
 import descent.internal.compiler.parser.TypeArray;
 import descent.internal.compiler.parser.TypeBasic;
 import descent.internal.compiler.parser.TypeClass;
 import descent.internal.compiler.parser.TypeDArray;
 import descent.internal.compiler.parser.TypeDelegate;
 import descent.internal.compiler.parser.TypeEnum;
 import descent.internal.compiler.parser.TypeExp;
 import descent.internal.compiler.parser.TypeFunction;
 import descent.internal.compiler.parser.TypeIdentifier;
 import descent.internal.compiler.parser.TypePointer;
 import descent.internal.compiler.parser.TypeSArray;
 import descent.internal.compiler.parser.TypeStruct;
 import descent.internal.compiler.parser.TypeTypedef;
 import descent.internal.compiler.parser.TypedefDeclaration;
 import descent.internal.compiler.parser.UnionDeclaration;
 import descent.internal.compiler.parser.UnitTestDeclaration;
 import descent.internal.compiler.parser.VarDeclaration;
 import descent.internal.compiler.parser.VarExp;
 import descent.internal.compiler.parser.WithScopeSymbol;
 import descent.internal.compiler.parser.ast.AstVisitorAdapter;
 import descent.internal.compiler.util.HashtableOfObject;
 import descent.internal.core.CompilerConfiguration;
 import descent.internal.core.INamingRequestor;
 import descent.internal.core.InternalNamingConventions;
 import descent.internal.core.InternalSignature;
 import descent.internal.core.SearchableEnvironment;
 import descent.internal.core.util.Util;
 
 /**
  * This class is the entry point for source completions.
  * It contains two public APIs used to call CodeAssist on a given source with
  * a given environment, assisting position and storage (and possibly options).
  */
 public class CompletionEngine extends Engine 
 	implements RelevanceConstants, ISearchRequestor {
 	
 	public final static Type typeInt = TypeBasic.tint32;
 	public final static Type typeCharArray = new TypeDArray(TypeBasic.tchar);
 	
 	public static boolean DEBUG = false;
 	public static boolean PERF = false;
 	private static final char[][] ddocSections = { "Authors".toCharArray(), "Bugs".toCharArray(), "Date".toCharArray(), "Deprecated".toCharArray(), "Examples".toCharArray(), "History".toCharArray(), "License".toCharArray(), "Returns".toCharArray(), "See_Also".toCharArray(), "Standards".toCharArray(), "Throws".toCharArray(), "Version".toCharArray(), "Copyright".toCharArray(), "Params".toCharArray(), "Macros".toCharArray() };
 	private static final char[][] specialTokens = { Id.FILE, Id.LINE, Id.DATE, Id.TIME, Id.TIMESTAMP, Id.VERSION, Id.VENDOR };
 	private final static char[] parenthesis = { '(', ')' };
 	
 	private static final HashtableOfCharArrayAndObject specialFunctions = new HashtableOfCharArrayAndObject();
 	{
 		Object dummy = new Object();
 		specialFunctions.put(Id.ctor, dummy);
 		specialFunctions.put(Id.dtor, dummy);
 		specialFunctions.put(Id.classNew, dummy);
 		specialFunctions.put(Id.classDelete, dummy);
 	}
 	
 	private static final char[] op = { 'o', 'p' };
 	private static final HashtableOfCharArrayAndObject ops = new HashtableOfCharArrayAndObject();
 	{
 		Object dummy = new Object();
 		ops.put(Id.uadd, dummy);
 		ops.put(Id.neg, dummy);
 		ops.put(Id.com, dummy);
 		ops.put(Id.add, dummy);
 		ops.put(Id.add_r, dummy);
 		ops.put(Id.sub, dummy);
 		ops.put(Id.sub_r, dummy);
 		ops.put(Id.mul, dummy);
 		ops.put(Id.mul_r, dummy);
 		ops.put(Id.div, dummy);
 		ops.put(Id.div_r, dummy);
 		ops.put(Id.mod, dummy);
 		ops.put(Id.mod_r, dummy);
 		ops.put(Id.eq, dummy);
 		ops.put(Id.cmp, dummy);
 		ops.put(Id.iand, dummy);
 		ops.put(Id.iand_r, dummy);
 		ops.put(Id.ior, dummy);
 		ops.put(Id.ior_r, dummy);
 		ops.put(Id.ixor, dummy);
 		ops.put(Id.ixor_r, dummy);
 		ops.put(Id.shl, dummy);
 		ops.put(Id.shl_r, dummy);
 		ops.put(Id.shr, dummy);
 		ops.put(Id.shr_r, dummy);
 		ops.put(Id.ushr, dummy);
 		ops.put(Id.ushr_r, dummy);
 		ops.put(Id.cat, dummy);
 		ops.put(Id.cat_r, dummy);
 		ops.put(Id.assign, dummy);
 		ops.put(Id.addass, dummy);
 		ops.put(Id.subass, dummy);
 		ops.put(Id.mulass, dummy);
 		ops.put(Id.divass, dummy);
 		ops.put(Id.modass, dummy);
 		ops.put(Id.andass, dummy);
 		ops.put(Id.orass, dummy);
 		ops.put(Id.xorass, dummy);
 		ops.put(Id.shlass, dummy);
 		ops.put(Id.shrass, dummy);
 		ops.put(Id.ushrass, dummy);
 		ops.put(Id.catass, dummy);
 		ops.put(Id.postinc, dummy);
 		ops.put(Id.postdec, dummy);
 		ops.put(Id.index, dummy);
 		ops.put(Id.indexass, dummy);
 		ops.put(Id.slice, dummy);
 		ops.put(Id.sliceass, dummy);
 		ops.put(Id.call, dummy);
 		ops.put(Id._cast, dummy);
 		ops.put(Id.opIn, dummy);
 		ops.put(Id.opIn_r, dummy);
 		ops.put(Id.apply, dummy);
 		ops.put(Id.applyReverse, dummy);
 	}
 	
 	IJavaProject javaProject;
 	CompletionParser parser;
 	CompletionRequestor requestor;
 	CompilerConfiguration compilerConfig;
 	public InternalSignature internalSignature;
 	
 	Module module;
 	SemanticContext semanticContext;
 	ASTNodeEncoder encoder;
 	
 	char[] fileName = null;
 	char[] sourceUnitFqn = null;
 	int actualCompletionPosition;
 	int startPosition;
 	int endPosition;
 	int offset;
 	
 	char[] source;
 	char[] currentName;
 	char[] expectedTypeSignature;
 	Type expectedType;
 	IdentifierExp targetIdent;
 	
 	HashtableOfObject typeCache;
 	
 	HashtableOfObject knownCompilationUnits = new HashtableOfObject(10);
 	HashtableOfObject knownKeywords = new HashtableOfObject(10);
 	HashtableOfObject knownDeclarations = new HashtableOfObject(10);
 	HashtableOfObject suggestedModules = new HashtableOfObject(10);
 	
 	boolean semanticRun = false;
 	
 	boolean isCompletingPackage;
 	boolean isCompletingTypeIdentifier;
 	boolean isWithScopeSymbol;
 	boolean wantConstructorsAndOpCall = true;
 	boolean wantProperties = true;
 	boolean wantMethodContextInfo = false;
 	boolean isCompletingThisCall = false;
 	boolean isCompletingSuperCall = false;
 	boolean isBetweenMethodName = false;
 	boolean wantArguments = true;
 	
 	Scope rootScope;
 	int includesFilter;
 	
 	private final static int INCLUDE_TYPES = 1;
 	private final static int INCLUDE_VARIABLES = 2;
 	private final static int INCLUDE_FUNCTIONS = 4;
 	private final static int INCLUDE_CONSTRUCTORS = 8;
 	private final static int INCLUDE_OPCALL = 16;
 	private final static int INCLUDE_IMPORTS = 32;
 	private final static int INCLUDE_ALL = INCLUDE_TYPES | INCLUDE_VARIABLES | INCLUDE_FUNCTIONS | INCLUDE_IMPORTS;
 	
 	/**
 	 * The CompletionEngine is responsible for computing source completions.
 	 *
 	 * It requires a searchable name environment, which supports some
 	 * specific search APIs, and a requestor to feed back the results to a UI.
 	 *
 	 *  @param nameEnvironment descent.internal.codeassist.ISearchableNameEnvironment
 	 *      used to resolve type/package references and search for types/packages
 	 *      based on partial names.
 	 *
 	 *  @param requestor descent.internal.codeassist.ICompletionRequestor
 	 *      since the engine might produce answers of various forms, the engine 
 	 *      is associated with a requestor able to accept all possible completions.
 	 *
 	 *  @param settings java.util.Map
 	 *		set of options used to configure the code assist engine.
 	 */
 	public CompletionEngine(
 			SearchableEnvironment nameEnvironment,
 			CompletionRequestor requestor,
 			Map settings,
 			IJavaProject javaProject) {
 		super(settings);
 		this.javaProject = javaProject;
 		this.requestor = requestor;
 		this.nameEnvironment = nameEnvironment;
 		this.typeCache = new HashtableOfObject(5);
 		this.compilerConfig = new CompilerConfiguration();
 		this.internalSignature = new InternalSignature(javaProject);
 	}
 
 	/**
 	 * Ask the engine to compute a completion at the specified position
 	 * of the given compilation unit.
 	 *
 	 *  No return
 	 *      completion results are answered through a requestor.
 	 *
 	 *  @param sourceUnit descent.internal.compiler.env.ICompilationUnit
 	 *      the source of the current compilation unit.
 	 *
 	 *  @param completionPosition int
 	 *      a position in the source where the completion is taking place. 
 	 *      This position is relative to the source provided.
 	 */
 	public void complete(ICompilationUnit sourceUnit, int completionPosition, int pos) {
 		if(DEBUG) {
 			System.out.print("COMPLETION IN "); //$NON-NLS-1$
 			System.out.print(sourceUnit.getFileName());
 			System.out.print(" AT POSITION "); //$NON-NLS-1$
 			System.out.println(completionPosition);
 			System.out.println("COMPLETION - Source :"); //$NON-NLS-1$
 			System.out.println(sourceUnit.getContents());
 		}
 		
 //		long time = System.currentTimeMillis();
 		
 		this.requestor.beginReporting();
 		try {
 			this.source = sourceUnit.getContents();	
 			this.fileName = sourceUnit.getFileName();
 			this.actualCompletionPosition = completionPosition; // - 1;
 			this.offset = pos;
 			
 			// Get the sourceUnit's fqn
 			sourceUnitFqn = fileName;
 			int index = CharOperation.lastIndexOf('.', fileName);
 			if (index != -1) {
 				sourceUnitFqn = CharOperation.subarray(this.fileName, 0, index);
 			} else {
 				sourceUnitFqn = new char[sourceUnitFqn.length];
 				System.arraycopy(sourceUnitFqn, 0, sourceUnitFqn, 0, sourceUnitFqn.length);
 			}
 			CharOperation.replace(sourceUnitFqn, '/', '.');
 			
 			suggestedModules.put(sourceUnitFqn, this);
 			
 			parser = new CompletionParser(Util.getApiLevel(this.compilerOptions.getMap()), source, this.fileName);
 			encoder = parser.encoder;
 			
 			parser.cursorLocation = completionPosition;
 			parser.nextToken();
 			
 			this.module = parser.parseModuleObj();
 			this.module.moduleName = sourceUnit.getFullyQualifiedName();
 			this.requestor.acceptContext(buildContext(parser));
 			
 			if (!parser.wantAssist()) {
 				return;
 			}
 			
 			ASTDmdNode assistNode = parser.getAssistNode();
 			
 			// Determine to which id we are assigning
 			if (parser.getExpectedTypeNode() instanceof VarDeclaration) {
 				targetIdent = ((VarDeclaration) parser.getExpectedTypeNode()).ident;
 			} else if (parser.getExpectedTypeNode() instanceof IdentifierExp) {
 				targetIdent = (IdentifierExp) parser.getExpectedTypeNode();
 			}
 			
 			if (parser.wantNames()) {
 				if (assistNode instanceof VarDeclaration) {
 					VarDeclaration var = (VarDeclaration) assistNode;
 					char[] name = var.ident == null ? CharOperation.NO_CHAR : var.ident.ident;
 					
 					this.startPosition = this.actualCompletionPosition - name.length;
 					this.endPosition = this.actualCompletionPosition;
 					
 					findVariableNames(name, var.type, CharOperation.NO_CHAR_CHAR);
 				}
 				return;
 			}
 			
 			// If there's an expected type, doSemantic to find it out
 			if (parser.wantAssitNode()) {
 				if (parser.expectedTypeNode != null) {
 					doSemantic();
 					computeExpectedType();
 				}
 				
 				// First the assist node
 				completeNode(assistNode);
 				
 				// For new |, don't suggest keywords or ddoc
 				if (parser.inNewExp) {
 					// In:
 					// Foo f = new |
 					// suggest Foo constructors
 					if (currentName != null && currentName.length == 0) {
 						if (expectedType != null) {
 							if (expectedType.ty == TY.Tclass) {
 								TypeClass tc = (TypeClass) expectedType;
 								ClassDeclaration cd = tc.sym;
 								
 								if (!(cd instanceof InterfaceDeclaration) && cd.ident != null && cd.ident.ident != null) {
 									wantConstructorsAndOpCall = true;
 									currentName = cd.ident.ident;
 									
 									suggestConstructors(cd);
 								}
 							}
 						}
 					}
 					return;
 				}
 				
 				// If it's expecting a struct type, and it has an opCall,
 				// and there's no identifier typed, suggest opCalls
 				if (currentName != null && currentName.length == 0 && expectedType != null
 						&& expectedType.ty == TY.Tstruct) {
 					TypeStruct typeStruct = (TypeStruct) expectedType;
 					if (typeStruct.sym != null && typeStruct.sym.members != null) {
 						currentName = typeStruct.sym.ident.ident;
 						wantConstructorsAndOpCall = true;
 						suggestMembers(typeStruct.sym.members, true, new HashtableOfCharArrayAndObject(), INCLUDE_OPCALL);
 					}
 				}
 			}
 			
 			// Then the keywords
 			if (!wantMethodContextInfo &&
 					parser.getKeywordCompletions() != null && 
 					!requestor.isIgnored(CompletionProposal.KEYWORD)) {
 				
 				char[] prefix = findTextBeforeCursor();
 				this.startPosition = this.actualCompletionPosition - prefix.length;
 				this.endPosition = this.actualCompletionPosition;
 				
 				for(ICompletionOnKeyword node : parser.getKeywordCompletions()) {
 					findKeywords(prefix, node.getPossibleKeywords(), node.canCompleteEmptyToken());
 				}
 				
 				// Also suggest the special tokens, if at least
 				// two characters were typed (otherwise, it's annoying)
 				if (prefix.length > 1) {
 					findKeywords(prefix, specialTokens, true);
 				}
 			}
 			
 			// Then ddoc
 			if (parser.getJavadocCompletion() != null) {
 				CompletionOnJavadocImpl node = parser.getJavadocCompletion();
 				completeJavadoc(node);
 			}
 		} catch (Exception e) {
 			Util.log(e);
 		} finally {
 			this.requestor.endReporting();
 			
 //			time = System.currentTimeMillis() - time;
 //			System.out.println("Completion took " + time + " milliseconds to complete.");
 		}
 	}
 
 	private void completeNode(ASTDmdNode assistNode) throws JavaModelException {
 		if (assistNode != null) {
 			if (assistNode instanceof CompletionOnModuleDeclaration && 
 					!requestor.isIgnored(CompletionProposal.COMPILATION_UNIT_REF)) {
 				CompletionOnModuleDeclaration node = (CompletionOnModuleDeclaration) assistNode;
 				completeModuleDeclaration(node);
 			} else if (assistNode instanceof CompletionOnImport &&
 					!requestor.isIgnored(CompletionProposal.COMPILATION_UNIT_REF)) {
 				CompletionOnImport node = (CompletionOnImport) assistNode;
 				if (node.isSelective) {
 					completeSelectiveImport(node);
 				} else {
 					completeImport(node);
 				}
 			} else if (assistNode instanceof CompletionOnArgumentName) {
 				CompletionOnArgumentName node = (CompletionOnArgumentName) assistNode;
 				completeArgumentName(node);
 			} else if (assistNode instanceof CompletionOnGotoStatement) {
 				CompletionOnGotoStatement node = (CompletionOnGotoStatement) assistNode;
 				completeGotoBreakOrContinueStatement(node.ident, false /* don't check in loop */);
 			} else if (assistNode instanceof CompletionOnBreakStatement) {
 				CompletionOnBreakStatement node = (CompletionOnBreakStatement) assistNode;
 				completeGotoBreakOrContinueStatement(node.ident, true /* check in loop */);
 			} else if (assistNode instanceof CompletionOnContinueStatement) {
 				CompletionOnContinueStatement node = (CompletionOnContinueStatement) assistNode;
 				completeGotoBreakOrContinueStatement(node.ident, true /* check in loop */);
 			} else if (assistNode instanceof CompletionOnVersionCondition) {
 				CompletionOnVersionCondition node = (CompletionOnVersionCondition) assistNode;
 				completeVersionCondition(node);
 			} else if (assistNode instanceof CompletionOnDebugCondition) {
 				CompletionOnDebugCondition node = (CompletionOnDebugCondition) assistNode;
 				completeDebugCondition(node);
 			} else if (assistNode instanceof CompletionOnCaseStatement) {
 				CompletionOnCaseStatement node = (CompletionOnCaseStatement) assistNode;
 				completeCaseStatement(node);
 			} else if (assistNode instanceof CompletionOnTypeDotIdExp) {
 				CompletionOnTypeDotIdExp node = (CompletionOnTypeDotIdExp) assistNode;
 				completeTypeDotIdExp(node);
 			} else if (assistNode instanceof CompletionOnDotIdExp) {
 				CompletionOnDotIdExp node = (CompletionOnDotIdExp) assistNode;
 				completeDotIdExp(node);
 			} else if (assistNode instanceof CompletionOnTypeIdentifier) {
 				CompletionOnTypeIdentifier node = (CompletionOnTypeIdentifier) assistNode;
 				completeTypeIdentifier(node);
 			} else if (assistNode instanceof CompletionOnExpStatement) {
 				CompletionOnExpStatement node = (CompletionOnExpStatement) assistNode;
 				completeExpStatement(node);
 			} else if (assistNode instanceof CompletionOnIdentifierExp) {
 				CompletionOnIdentifierExp node = (CompletionOnIdentifierExp) assistNode;
 				completeIdentifierExp(node);
 			} else if (assistNode instanceof CompletionOnThisDotExp) {
 				CompletionOnThisDotExp node = (CompletionOnThisDotExp) assistNode;
 				completeThisDotExp(node);
 			} else if (assistNode instanceof CompletionOnSuperDotExp) {
 				CompletionOnSuperDotExp node = (CompletionOnSuperDotExp) assistNode;
 				completeSuperDotExp(node);
 			} else if (assistNode instanceof CompletionOnCompoundStatement) {
 				CompletionOnCompoundStatement node = (CompletionOnCompoundStatement) assistNode;
 				completeCompoundStatement(node);
 			} else if (assistNode instanceof CompletionOnCallExp) {
 				CompletionOnCallExp node = (CompletionOnCallExp) assistNode;
 				completeCallExp(node);
 			} else if (assistNode instanceof CompletionOnNewExp) {
 				CompletionOnNewExp node = (CompletionOnNewExp) assistNode;
 				completeNewExp(node);
 			} else if (assistNode instanceof CompletionOnTemplateMixin) {
 				CompletionOnTemplateMixin node = (CompletionOnTemplateMixin) assistNode;
 				completeTemplateMixin(node);
 			} else if (assistNode instanceof CompletionOnClassDeclaration) {
 				CompletionOnClassDeclaration node = (CompletionOnClassDeclaration) assistNode;
 				completeClassDeclaration(node);
 			} else if (assistNode instanceof CompletionOnInterfaceDeclaration) {
 				CompletionOnInterfaceDeclaration node = (CompletionOnInterfaceDeclaration) assistNode;
 				completeInterfaceDeclaration(node);
 			}
 		}
 	}
 	
 	private void computeExpectedType() {
 		computeExpectedTypeBasic();
 		
 		// If it's ~ or ~= on a dynamic array, the expectedType is the next
 		if (expectedType != null && parser.inCatExp && expectedType.ty == TY.Tarray) {
 			TypeArray ta = (TypeArray) expectedType;
 			expectedType = ta.next;
 			expectedTypeSignature = expectedType.getSignature().toCharArray();
 		}
 	}
 	
 	private void computeExpectedTypeBasic() {
 		if (parser.expectedTypeNode instanceof CallExp) {
 			CallExp callExp = (CallExp) parser.expectedTypeNode;
 			if (callExp.e1 == null) {
 				return;
 			}
 				
 			Type type = callExp.e1.type;
 			if (type == null) {
 				return;
 			}
 			
 			if (type instanceof TypeFunction) {
 				computeExpectedTypeForTypeFunction((TypeFunction) type, 0);
 			}
 			return;
 		}
 		
 		if (parser.expectedTypeNode instanceof NewExp) {
 			NewExp newExp = (NewExp) parser.expectedTypeNode;
 			
 			boolean hasAllocator = newExp.allocator != null && (newExp.sourceNewargs != null && newExp.sourceNewargs.size() > 0);
 			if (hasAllocator) {
 				Type type = newExp.allocator.type();
 				if (type == null) {
 					return;
 				}
 				
 				if (type instanceof TypeFunction) {
 					computeExpectedTypeForTypeFunction((TypeFunction) type, -1);
 					if (expectedTypeSignature != null) {
 						return;
 					}
 				}
 			}
 			
 			
 			if (newExp.member == null) {
 				return;
 			}
 				
 			Type type = newExp.member.type();
 			if (type == null) {
 				return;
 			}
 			
 			if (type instanceof TypeFunction) {
 				if (hasAllocator) {
 					computeExpectedTypeForTypeFunction((TypeFunction) type, newExp.newargs == null ? -1 : newExp.newargs.size() - 1);
 				} else {
 					computeExpectedTypeForTypeFunction((TypeFunction) type, 0);
 				}
 			}
 			return;
 		}
 		
 		if (parser.expectedTypeNode instanceof Expression) {
 			Expression exp = (Expression) parser.expectedTypeNode;
 			Type type = exp.type;
 			if (type == null) {
 				if (exp.resolvedExpression != null) {
 					type = exp.resolvedExpression.type;
 				}
 			}
 			if (type != null) {
 				// For setters
 				if (type instanceof TypeFunction) {
 					if (isSetter((TypeFunction) type)) {
 						type = ((TypeFunction) type).parameters.get(0).type;
 						if (type == null) {
 							return;
 						}
 					} else if (isGetter((TypeFunction) type)) {
 						type = ((TypeFunction) type).next;
 						if (type == null) {
 							return;
 						}
 					}
 				}
 				expectedType = type;
 				expectedTypeSignature = expectedType.getSignature().toCharArray();				
 			}
 			return;
 		}
 		
 		if (parser.expectedTypeNode instanceof Type) {
 			expectedType = (Type) parser.expectedTypeNode;
 			expectedTypeSignature = expectedType.getSignature().toCharArray();
 			return;
 		}
 		
 		if (parser.expectedTypeNode instanceof VarDeclaration) {
 			expectedType = ((VarDeclaration) parser.expectedTypeNode).type;
 			if (expectedType != null) {
 				expectedTypeSignature = expectedType.getSignature().toCharArray();
 			}
 			return;
 		}
 		
 		if (parser.expectedTypeNode instanceof CompletionOnReturnStatement) {
 			CompletionOnReturnStatement stm = (CompletionOnReturnStatement) parser.expectedTypeNode;
 			Scope scope = stm.scope;
 			if (scope != null && scope.func != null && scope.func.type != null && scope.func.type.next != null) {
 				expectedType = scope.func.type.next;
 				expectedTypeSignature = expectedType.getSignature().toCharArray();
 			}
 		}
 			
 	}
 	
 	private boolean isSetter(TypeFunction type) {
 		return type != null && type.next == Type.tvoid
 			&& type.parameters != null && type.parameters.size() == 1;
 	}
 	
 	private boolean isGetter(TypeFunction type) {
 		return type != null && type.next != Type.tvoid
 			&& (type.parameters == null || type.parameters.isEmpty());
 	}
 
 	private void computeExpectedTypeForTypeFunction(TypeFunction typeFunc, int decrease) {
 		if (typeFunc.parameters == null || parser.expectedArgumentIndex - decrease >= typeFunc.parameters.size()) {
 			return;
 		}
 		
 		Argument arg = typeFunc.parameters.get(parser.expectedArgumentIndex - decrease);
 		Type type = arg.type;
 		if (type != null) {
 			expectedType = type;
 			expectedTypeSignature = expectedType.getSignature().toCharArray();
 		}
 	}
 
 	private void completeCompoundStatement(CompletionOnCompoundStatement node) throws JavaModelException {
 		doSemantic();
 		
 		if (rootScope == null) {
 			rootScope = node.scope;
 		}
 		
 		if (parser.completionToken != null) {
 			currentName = parser.completionToken;
 			startPosition = parser.completionTokenStart;
 			endPosition = parser.completionTokenEnd;
 		} else {
 			currentName = CharOperation.NO_CHAR;
 			startPosition = actualCompletionPosition;
 			endPosition = actualCompletionPosition;
 		}
 		
 		wantConstructorsAndOpCall = false;
 		
 		if (parser.wantOnlyType()) {
 			completeScope(rootScope, INCLUDE_TYPES | INCLUDE_IMPORTS);
 		} else {
 			if (currentName.length == 0) {
 				completeScope(rootScope, INCLUDE_ALL & ~INCLUDE_TYPES);
 			} else {
 				completeScope(rootScope, INCLUDE_ALL);
 			}
 		}
 	}
 
 	private CompletionContext buildContext(CompletionParser parser) {
 		// TODO Descent completion context
 		CompletionContext context = new CompletionContext();
 		context.setOffset(this.offset);
 		if (parser.completionToken != null) {
 			context.setTokenKind(CompletionContext.TOKEN_KIND_NAME);
 			context.setToken(parser.completionToken);
 			context.setTokenRange(parser.completionTokenStart, parser.completionTokenEnd);
 		}
 		return context;
 	}
 
 	private void doSemantic() throws JavaModelException {
 		if (semanticRun) {
 			return;
 		}
 		
 		semanticRun = true;
 		semanticContext = CompilationUnitResolver.resolve(module, this.javaProject, null, encoder);
 	}
 
 	private void completeModuleDeclaration(CompletionOnModuleDeclaration node) {
 		char[] fqn = node.getFQN();
 		char[] fqnBeforeCursor = CharOperation.subarray(fqn, 0, this.actualCompletionPosition - node.getFqnStart());
 		
 		this.startPosition = node.getFqnStart();
 		this.endPosition = node.getFqnEnd();
 		
 		// Also include the next dot, if any
 		if (this.endPosition < this.source.length && this.source[this.endPosition] == '.') {
 			this.endPosition++;
 		}
 		
 		if (fqnBeforeCursor.length == 0 || 
 				match(fqnBeforeCursor, sourceUnitFqn)) {
 			CompletionProposal proposal = createProposal(CompletionProposal.COMPILATION_UNIT_REF, this.actualCompletionPosition, node);
 			proposal.setCompletion(sourceUnitFqn);
 			proposal.setReplaceRange(this.startPosition, this.endPosition);
 			this.requestor.accept(proposal);
 		}
 	}
 	
 	private boolean match(char[] prefix, char[] name) {
 		return CharOperation.prefixEquals(prefix, name) || 
 			(options.camelCaseMatch && CharOperation.camelCaseMatch(prefix, name));
 	}
 	
 	private void completeSelectiveImport(CompletionOnImport node) throws JavaModelException {
 		doSemantic();
 		
 //		node.semantic(Scope.createGlobal(node.mod, semanticContext), semanticContext);
 		
 		if (node.mod != null) {
 			if (node.selectiveName == null) {
 				this.startPosition = actualCompletionPosition;
 				this.endPosition = actualCompletionPosition;
 				this.currentName = CharOperation.NO_CHAR;
 			} else {
 				this.currentName = computePrefixAndSourceRange(node.selectiveName);
 			}
 			
 			node.mod = node.mod.unlazy(this.currentName, semanticContext);
 			
 			this.wantArguments = false;
 			suggestMembers(node.mod.members, false, new HashtableOfCharArrayAndObject(), INCLUDE_TYPES | INCLUDE_VARIABLES | INCLUDE_FUNCTIONS);
 		}
 	}
 	
 	private void completeImport(CompletionOnImport node) {
 		char[] fqn = node.getFQN();
 		char[] fqnBeforeCursor = CharOperation.subarray(fqn, 0, this.actualCompletionPosition - node.getFqnStart());
 		
 		if (fqnBeforeCursor.length == 0) {
 			return;
 		}
 		
 		this.startPosition = node.getFqnStart();
 		this.endPosition = node.getFqnEnd();
 		
 		// Also include the next dot, if any
 		if (this.endPosition < this.source.length && this.source[this.endPosition] == '.') {
 			this.endPosition++;
 		}
 		
 		if (fqnBeforeCursor.length > 0) {
 			nameEnvironment.findCompilationUnits(fqnBeforeCursor, this);
 		}
 	}
 	
 	private void completeArgumentName(CompletionOnArgumentName node) throws JavaModelException {
 		if (node.type instanceof CompletionOnTypeIdentifier) {
 			completeTypeIdentifier((CompletionOnTypeIdentifier) node.type);
 		} else {
 			char[] name = computePrefixAndSourceRange(node.ident);
 			findVariableNames(name, node.type, findExcludedNames(node));
 		}
 	}
 	
 	private void completeGotoBreakOrContinueStatement(IdentifierExp ident, final boolean inLoop) {
 		if (parser.completionToken != null) {
 			currentName = parser.completionToken;
 			startPosition = parser.completionTokenStart;
 			endPosition = parser.completionTokenEnd;
 		}
 		
 		final HashtableOfCharArrayAndObject labelsMap = new HashtableOfCharArrayAndObject();		
 		module.accept(new AstVisitorAdapter() {
 			
 			private FuncDeclaration func;
 			
 			@Override
 			public boolean visit(FuncDeclaration node) {
 				return visitFunc(node);
 			}
 			
 			@Override
 			public boolean visit(UnitTestDeclaration node) {
 				return visitFunc(node);
 			}
 			
 			@Override
 			public boolean visit(InvariantDeclaration node) {
 				return visitFunc(node);
 			}
 			
 			@Override
 			public boolean visit(FuncLiteralDeclaration node) {
 				return visitFunc(node);
 			}
 			
 			private boolean visitFunc(FuncDeclaration node) {
 				if (inRange(node)) {
 					func = node;
 					labelsMap.clear();
 					return true;
 				} else {
 					return false;
 				}
 			}
 			
 			@Override
 			public boolean visit(LabelStatement node) {
 				if (func != null && func.start <= node.start && node.start <= func.start + func.length
 						&& (!inLoop || isRelevant(node))) {
 					labelsMap.put(node.ident.ident, this);
 				}
 				return true;
 			}
 			
 			private boolean inRange(ASTDmdNode node) {
 				return node.start <= CompletionEngine.this.actualCompletionPosition &&
 					CompletionEngine.this.actualCompletionPosition <= node.start + node.length;
 			}
 			
 			private boolean isRelevant(LabelStatement node) {
 				Statement stm = node.statement;
 				while(stm != null) {
 					int type = stm.getNodeType();
 					if (type == ASTDmdNode.WHILE_STATEMENT ||
 						type == ASTDmdNode.DO_STATEMENT ||
 						type == ASTDmdNode.FOR_STATEMENT) {
 						return true;
 					} else if (type == ASTDmdNode.COMPOUND_STATEMENT) {
 						CompoundStatement cs = (CompoundStatement) stm;
 						if (cs.sourceStatements != null && cs.sourceStatements.size() > 0) {
 							stm = cs.sourceStatements.get(0);
 						} else {
 							return false;
 						}
 					} else {
 						return false;
 					}
 				}
 				return false;
 			}
 
 			@Override
 			public boolean visit(EnumDeclaration node) {
 				return false;
 			}			
 			@Override
 			public boolean visit(ClassDeclaration node) {
 				return inRange(node);
 			}			
 			@Override
 			public boolean visit(InterfaceDeclaration node) {
 				return inRange(node);
 			}			
 			@Override
 			public boolean visit(StructDeclaration node) {
 				return inRange(node);
 			}			
 			@Override
 			public boolean visit(UnionDeclaration node) {
 				return inRange(node);
 			}			
 			@Override
 			public boolean visit(TemplateDeclaration node) {
 				return inRange(node);
 			}			
 		});
 		
 		char[][] labels = labelsMap.keys();
 		if (labels != null) {
 			char[] prefix = computePrefixAndSourceRange(ident);			
 			for(char[] label : labels) {
 				if (label != null) {
 					if (prefix.length == 0 || match(prefix, label) && !CharOperation.equals(prefix, label)) {
 						int relevance = computeBaseRelevance();
 						relevance += computeRelevanceForInterestingProposal();
 						relevance += computeRelevanceForCaseMatching(prefix, label);
 						relevance += RelevanceConstants.R_LABEL;
 						
 						CompletionProposal proposal = this.createProposal(CompletionProposal.LABEL_REF, this.actualCompletionPosition, null);
 						proposal.setName(label);
 						proposal.setCompletion(label);
 						proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						CompletionEngine.this.requestor.accept(proposal);
 					}
 				}
 			}
 		}
 	}
 	
 	private void completeVersionCondition(CompletionOnVersionCondition node) {
 		completeVersionDebugCondition(node, CompletionProposal.VERSION_REF, compilerConfig.versionIdentifiers, parser.versions);
 	}
 	
 	private void completeDebugCondition(CompletionOnDebugCondition node) {
 		completeVersionDebugCondition(node, CompletionProposal.DEBUG_REF, compilerConfig.debugIdentifiers, parser.debugs);
 	}
 	
 	private void completeVersionDebugCondition(DVCondition node, int kind, HashtableOfCharArrayAndObject predefs, HashtableOfCharArrayAndObject inSource) {
 		char[] name = node.ident == null ? CharOperation.NO_CHAR : node.ident;
 		this.startPosition = this.actualCompletionPosition - name.length;
 		this.endPosition = this.actualCompletionPosition;
 		
 		// Suggest the versions found in the source file
 		if (inSource == null) {
 			inSource = new HashtableOfCharArrayAndObject();
 		}
 		
 		// And also the compiler ones
 		for(char[] predefined : predefs.keys()) {
 			if (predefined != null) {
 				inSource.put(predefined, this);
 			}
 		}
 		
 		for(char[] id : inSource.keys()) {
 			if (id == null || id.length == 0) {
 				continue;
 			}
 			
 			if (name.length == 0 || match(name, id) && !CharOperation.equals(name, id)) {
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(name, id);
 				
 				CompletionProposal proposal = this.createProposal(kind, this.actualCompletionPosition, null);
 				proposal.setName(id);
 				proposal.setCompletion(id);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				proposal.setRelevance(relevance);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}
 	}
 	
 	private void completeCaseStatement(CompletionOnCaseStatement node) throws JavaModelException {
 		doSemantic();
 		
 		// Check to see if the condition is an enum, and suggest
 		// the members of it
 		SwitchStatement sw = node.sw;
 		if (sw == null) {
 			return;
 		}
 		
 		TypeEnum typeEnum;
 		
 		Expression condition = sw.condition;
 		if (condition == null) {
 			return;
 		} else if (condition instanceof VarExp) {
 			VarExp var = (VarExp) condition;
 			Type type = var.type;
 			if (type == null || !(type instanceof TypeEnum)) {
 				return;
 			}
 			
 			typeEnum = (TypeEnum) type;	
 		} else if (condition instanceof CastExp) {
 			CastExp castExp = (CastExp) condition;
 			if (castExp.e1 == null || !(castExp.e1 instanceof VarExp)) {
 				return;
 			}
 			
 			VarExp varExp = (VarExp) castExp.e1;
 			if (varExp.var == null || !(varExp.var instanceof VarDeclaration)) {
 				return;
 			}
 			
 			VarDeclaration varDeclaration = (VarDeclaration) varExp.var;
 			if (varDeclaration.type == null || !(varDeclaration.type instanceof TypeEnum)) {
 				return;
 			}
 			
 			typeEnum = (TypeEnum) varDeclaration.type;
 		} else {
 			return;
 		}
 		
 		EnumDeclaration enumDeclaration = typeEnum.sym;
 		if (enumDeclaration == null) {
 			return;
 		}
 		
 		if (enumDeclaration.members == null) {
 			return;
 		}
 		
 		Expression caseExp = node.originalExpression;
 		if (caseExp instanceof ErrorExp) {
 			currentName = CharOperation.NO_CHAR;
 			this.startPosition = this.actualCompletionPosition;
 			this.endPosition = this.actualCompletionPosition;
 		} else if (caseExp instanceof IdentifierExp) {
 			IdentifierExp idExp = (IdentifierExp) caseExp;
 			currentName = idExp.ident;
 			this.startPosition = idExp.start;
 			this.endPosition = this.actualCompletionPosition;
 		} else {
 			return;
 		}
 		
 		HashtableOfCharArrayAndObject excludedNames = new HashtableOfCharArrayAndObject();
 		if (sw.cases != null) {
 			for(Object caseObject : sw.cases) {
 				CaseStatement caseStatement = (CaseStatement) caseObject;
 				Expression exp = caseStatement.sourceExp;
 				if (exp != null) {
 					excludedNames.put(exp.toCharArray(), this);
 				}
 			}
 		}
 		
 		completeEnumMembers(enumDeclaration, excludedNames, true /* use fqn */);
 	}
 	
 	private void completeTypeDotIdExp(CompletionOnTypeDotIdExp node) throws JavaModelException {
 		Type type = node.type;
 		if (type == null) {
 			return;
 		}
 		
 		// If it's a basic type, no semantic analysis is needed
 		if (type.getNodeType() == ASTDmdNode.TYPE_BASIC) {
 			currentName = computePrefixAndSourceRange(node.ident);
 			completeTypeBasicProperties((TypeBasic) type);
 		} else {
 			// else, do semantic, then see what the type is
 			// it's typeof(exp)
 			doSemantic();
 			
 			completeType(node.resolvedType, node.ident, true /* only statics */);
 		}
 	}
 
 	private void completeDotIdExp(CompletionOnDotIdExp node) throws JavaModelException {
 		if (node.e1 == null) {
 			return;
 		}
 		
 		doSemantic();
 		
 		// Case where there is:
 		//
 		// this.|
 		// this.foo.bar = 2;
 		//
 		// or:
 		//
 		// this.something.|
 		// this.foo.bar = 2;
 		// 
 		// that is, "this" is next
 		
 		if (node.e1 instanceof IntegerExp && node.sourceE1 instanceof CompletionOnDotIdExp) {
 			CompletionOnDotIdExp cip = (CompletionOnDotIdExp) node.sourceE1;
 			completeExpression(cip.e1, node.ident);
 			return;
 		}
 		
 		completeExpression(node.e1, node.ident);
 	}
 	
 	private void completeTypeIdentifier(CompletionOnTypeIdentifier node) throws JavaModelException {
 		if ((node.idents == null || node.idents.isEmpty()) && 
 			(node.ident == null || node.ident.ident == null || node.ident.ident.length == 0)) {
 			return;
 		}
 		
 		isCompletingTypeIdentifier = true;
 		
 		doSemantic();
 		
 		if (node.dot == -1) {
 			currentName = computePrefixAndSourceRange(node.ident);
 			
 			if (rootScope == null) {
 				rootScope = node.scope;
 			}
 			
 			// Inside a function, this may be the case of:
 			// Object something;
 			//
 			// somet --> request autocompletion
 			//
 			// something = ...;
 			//
 			// The parser thinks it's: somet something (a var declaration with type "somet"),
 			// so inside a function we want other things beside types
 			if (rootScope != null && rootScope.func != null) {
 				isCompletingTypeIdentifier = false;
 			}
 			
 			completeScope(rootScope, INCLUDE_ALL);
 			
 			// Also suggest packages
 			isCompletingPackage = true;
 			if (currentName.length > 0) {
 				nameEnvironment.findCompilationUnits(currentName, this);
 			}
 			
 			// And top level declarations
 			if (currentName.length > 0) {
 				nameEnvironment.findPrefixDeclarations(currentName, false, options.camelCaseMatch, this);
 			}
 			
 			// Show constructors and opCalls
 			if (node.ident.resolvedSymbol != null) {
 				suggestConstructorsAndOpCall(node.ident.resolvedSymbol, node.ident.resolvedExpression);
 			}
 		} else if (node.ident != null) {
 			isCompletingTypeIdentifier = false;
 			completeIdentDot(node.ident);
 		}
 	}
 	
 	private void suggestConstructorsAndOpCall(Dsymbol sym, Expression resolvedExpression) {
 		if (!wantConstructorsAndOpCall) {
 			return;
 		}
 		
 		boolean onlyStatics = resolvedExpression instanceof TypeExp;
 		
 		if (sym instanceof ClassDeclaration) {
 			// First constructors, only if in new
 			if (parser.inNewExp) {
 				suggestConstructors((ClassDeclaration) sym);
 			// Then opCalls, only if not in new
 			} else {
 				ClassDeclaration cd = ((ClassDeclaration) sym).unlazy(this.currentName, semanticContext);
 				suggestMembers(cd.members, onlyStatics, 0, new HashtableOfCharArrayAndObject(), INCLUDE_OPCALL);
 			}
 		} else if (sym instanceof StructDeclaration) {
 			StructDeclaration struct = ((StructDeclaration) sym).unlazy(CharOperation.NO_CHAR, semanticContext);
 			suggestMembers(struct.members, onlyStatics, 0, new HashtableOfCharArrayAndObject(), INCLUDE_OPCALL);
 		}
 	}
 	
 	private void suggestConstructors(ClassDeclaration sym) {
 		ClassDeclaration cd = sym.unlazy(this.currentName, semanticContext);
 		
 		Declaration func = cd.ctor(semanticContext);
 		
 		HashtableOfCharArrayAndObject hash = new HashtableOfCharArrayAndObject();
 		
 		if (func == null && !((ClassDeclaration) sym).isAbstract()) {
 			// Suggest default constructor
 			cd.ctor(new CtorDeclaration(Loc.ZERO, new Arguments(), 0));
 			cd.ctor(semanticContext).type = new TypeFunction(new Arguments(), cd.type, 0, LINK.LINKd);
 			cd.ctor(semanticContext).parent = cd;
 			
 			suggestMember(cd.ctor(semanticContext), false, 0, hash, INCLUDE_CONSTRUCTORS);
 		} else {
 			while(func != null) {
 				suggestMember(func, false, 0, hash, INCLUDE_CONSTRUCTORS);
 				
 				if (func instanceof FuncDeclaration) {
 					func = ((FuncDeclaration) func).overnext;
 				} else {
 					break;
 				}
 			}
 		}
 	}
 
 	private void completeExpStatement(CompletionOnExpStatement node) throws JavaModelException {
 		doSemantic();
 		
 		startPosition = node.start;
 		endPosition = node.start + node.length;
 		
 		if (node.exp instanceof ScopeExp) {
 			ScopeExp se = (ScopeExp) node.exp;
 			if (se.sds instanceof Module) {
 				currentName = computePrefixAndSourceRange(((Module) se.sds).ident);
 				// Need to do this again because the ident of the module has other source range
 				startPosition = node.start;
 				endPosition = node.start + node.length;
 			} else if (se.sds instanceof Package) {
 				currentName = computePrefixAndSourceRange(((Package) se.sds).ident);
 				// Need to do this again because the ident of the package has other source range
 				startPosition = node.start;
 				endPosition = node.start + node.length;
 			} else {
 				return;
 			}
 		} else if (node.exp instanceof IdentifierExp) {
 			currentName = computePrefixAndSourceRange((IdentifierExp) node.exp);
 			// Need to do this again because the ident may have other source range
 			startPosition = node.start;
 			endPosition = node.start + node.length;
 		} else if (node.exp instanceof TypeExp) {
 			TypeExp te = (TypeExp) node.exp;
 			Type type = te.type;
 			Dsymbol sym = null;
 			if (type instanceof TypeClass) {
 				sym = ((TypeClass) type).sym;
 			} else if (type instanceof TypeStruct) {
 				sym = ((TypeStruct) type).sym;
 			} else if (type instanceof TypeEnum) {
 				sym = ((TypeEnum) type).sym;
 			} else if (type instanceof TypeTypedef) {
 				sym = ((TypeTypedef) type).sym;
 			} else {
 				return;
 			}
 			
 			if (sym == null) {
 				return;
 			}
 			
 			if (node.sourceExp instanceof IdentifierExp) {
 				currentName = ((IdentifierExp) node.sourceExp).ident;
 			} else {
 				currentName = sym.ident.ident;
 			}
 			
 			trySuggestCall(type, currentName, CharOperation.NO_CHAR, true /* only statics */);
 		} else if (node.exp instanceof CallExp) {
 			CallExp ce = (CallExp) node.exp;
 			if (ce.e1 instanceof IdentifierExp) {
 				currentName = computePrefixAndSourceRange((IdentifierExp) ce.e1);
 			} else if (ce.e1 instanceof VarExp) {
 				Declaration var = ((VarExp) ce.e1).var;
 				currentName = var.ident.ident;
 			} else {
 				return;
 			}
 		} else if (node.exp instanceof VarExp) {
 			Declaration var = ((VarExp) node.exp).var;
 			
 			// Need to set correct location of IdentifierExp
 			IdentifierExp ident = new IdentifierExp(var.ident.ident);
 			ident.copySourceRange(node);
 			
 			currentName = computePrefixAndSourceRange(ident);
 		} else if (node.exp instanceof TemplateExp) {
 			TemplateDeclaration td = ((TemplateExp) node.exp).td;
 			
 			// Need to set correct location of IdentifierExp
 			IdentifierExp ident = new IdentifierExp(td.ident.ident);
 			ident.copySourceRange(node);
 			
 			currentName = computePrefixAndSourceRange(ident);
 		} else {
 			return;
 		}
 		
 		if (rootScope == null) {
 			rootScope = node.scope;
 		}
 		
 		if (rootScope == null) {
 			return;
 		}
 		
 		completeScope(rootScope, INCLUDE_ALL);
 		
 		// Also suggest packages
 		isCompletingPackage = true;
 		if (currentName.length > 0) {
 			nameEnvironment.findCompilationUnits(currentName, this);
 		}
 		
 		// And top level declarations
 		if (currentName.length > 0) {
 			nameEnvironment.findPrefixDeclarations(currentName, false, options.camelCaseMatch, this);
 		}
 	}
 	
 	private void completeIdentifierExp(CompletionOnIdentifierExp node) throws JavaModelException {
 		doSemantic();
 		
 		if (node.dot == -1) {
 			currentName = computePrefixAndSourceRange(node);
 			isBetweenMethodName = node.resolvedSymbol != null;
 			
 			if (rootScope == null) {
 				rootScope = node.scope;
 			}
 			
 			completeScope(rootScope, INCLUDE_ALL);
 			
 			// Also suggest packages
 			isCompletingPackage = true;
 			
 			if (currentName.length > 0) {
 				nameEnvironment.findCompilationUnits(currentName, this);
 			}
 			
 			// And top level declarations
 			if (currentName.length > 0) {
 				nameEnvironment.findPrefixDeclarations(currentName, false, options.camelCaseMatch, this);
 			}
 			
 			// Show constructors and opCalls
 			if (node.resolvedSymbol != null) {
 				suggestConstructorsAndOpCall(node.resolvedSymbol, node.resolvedExpression);
 			}
 		} else {
 			completeIdentDot(node);
 		}
 	}
 	
 	private void completeCallExp(CallExp node) throws JavaModelException {
 		doSemantic();
 		
 		if (node.sourceE1 instanceof SuperExp) {
 			isCompletingSuperCall = true;	
 		} else if (node.sourceE1 instanceof ThisExp) {
 			isCompletingThisCall = true;	
 		}
 		
 		if (node.e1 != null && node.e1.type != null) {
 			if (node.sourceE1 instanceof CompletionOnDotIdExp) {
 				this.wantArguments = false;
 				this.isBetweenMethodName = true;
 				completeNode(node.sourceE1);
 				return;
 			}
 			
 			currentName = CharOperation.NO_CHAR;
 			startPosition = actualCompletionPosition;
 			endPosition = actualCompletionPosition;
 			
 			if (node.e1 instanceof VarExp && node.e1.type instanceof TypeFunction) {
 				VarExp var = (VarExp) node.e1;
 				if (var.var instanceof FuncDeclaration) {
 					suggestContextInfo((FuncDeclaration) var.var);
 				}
 			} else if (node.e1 instanceof DotVarExp && node.e1.type instanceof TypeFunction) {
 				DotVarExp var = (DotVarExp) node.e1;
 				if (var.var instanceof FuncDeclaration) {
 					suggestContextInfo((FuncDeclaration) var.var);
 				}
 			} else if (node.e1.type instanceof TypeFunction && node.e1.type.next != null) {
 				trySuggestCall(node.e1.type.next, currentName, CharOperation.NO_CHAR, false /* dosen't matter here */);
 			} else {
 				// This means that the call is not resolved
 				if (node.e1.type instanceof TypeBasic) {
 					wantArguments = false;
 					completeNode(node.sourceE1);
 				} else {
 					wantMethodContextInfo = true;
 					trySuggestCall(node.e1.type, currentName, CharOperation.NO_CHAR, false /* dosen't matter here */);
 				}
 			}
 		} else if (node.e1 instanceof CallExp && node.e1 != node) {
 			completeCallExp((CallExp) node.e1);
 		} else if (node.e1 instanceof SuperExp) {
 			
 		}
 	}
 	
 	private void suggestContextInfo(FuncDeclaration func) {
 		int includes = func instanceof CtorDeclaration ? INCLUDE_CONSTRUCTORS : INCLUDE_FUNCTIONS;
 		
 		wantMethodContextInfo = true;
 		suggestMember(func, false, 0, null, includes);
 		
 		Dsymbol prev = func.overprevious;
 		while(prev != null) {
 			suggestMember(prev, false, 0, null, includes);
 			prev = prev.overprevious;
 		}
 		
 		Dsymbol next = func.overnext;
 		while(next != null) {
 			suggestMember(next, false, 0, null, includes);
 			if (next instanceof FuncDeclaration) {
 				next = ((FuncDeclaration) next).overnext;
 			} else if (next instanceof AliasDeclaration) {
 				next = ((AliasDeclaration) next).overnext;
 			} else {
 				break;
 			}
 		}
 	}
 	
 	private void completeNewExp(NewExp node) throws JavaModelException {
 		doSemantic();
 		
 		if (node.member != null && node.member instanceof FuncDeclaration) {
 			currentName = CharOperation.NO_CHAR;
 			startPosition = actualCompletionPosition;
 			endPosition = actualCompletionPosition;
 			
 			suggestContextInfo((FuncDeclaration) node.member);
 		} else if (node.sourceNewtype != null) {
 			completeNode(node.sourceNewtype);
 		}
 	}
 	
 	private void completeTemplateMixin(CompletionOnTemplateMixin node) throws JavaModelException {
 		doSemantic();
 		
 		if (node.scope != null) {
 			currentName = computePrefixAndSourceRange(node.name);
 			
 			completeScope(node.scope, INCLUDE_ALL);
 		}
 	}
 	
 	private void completeClassDeclaration(CompletionOnClassDeclaration node) throws JavaModelException {
 		doSemantic();
 		
 		completeAggregateDeclaration(node, node.theScope, node.baseClassIndex);
 	}
 
 	private void completeInterfaceDeclaration(CompletionOnInterfaceDeclaration node) throws JavaModelException {
 		doSemantic();
 		
 		completeAggregateDeclaration(node, node.theScope, node.baseClassIndex);
 	}
 	
 	private void completeAggregateDeclaration(ClassDeclaration node, Scope scope, int baseClassIndex) throws JavaModelException {
 		if (scope != null) {
 			rootScope = scope;
 			
 			boolean wantOverrides = node instanceof CompletionOnClassDeclaration && ((CompletionOnClassDeclaration) node).isCompletingScope;
 			
 			if (baseClassIndex == -1) {
 				currentName = CharOperation.NO_CHAR;
 				startPosition = actualCompletionPosition;
 				endPosition = actualCompletionPosition;
 				
 				includesFilter = INCLUDE_VARIABLES;
 				completeScope(rootScope, INCLUDE_ALL);
 				includesFilter = 0;
 				
 				wantOverrides = false;
 				if (wantOverrides) {
 					List<FuncDeclaration> funcs = new ArrayList<FuncDeclaration>();
 					
 					// Suggest override of the function if its virtual
 					// and it's not from this class
 					for(Object obj : node.vtbl) {
 						if (obj instanceof FuncDeclaration) {
 							FuncDeclaration fd = (FuncDeclaration) obj;
 							if (fd.parent != node) {
 								suggestOverride(fd);
 							}
 							
 							funcs.add(fd);
 						}
 					}
 					
 					// Also suggest interfaces methods, but only if these
 					// are not already present in this class
 					for(BaseClass bc : node.vtblInterfaces) {
 						TypeClass tc = (TypeClass) bc.type;
 					loop:
 						for(Dsymbol member : tc.sym.unlazy(semanticContext).members) {
 							if (member instanceof FuncDeclaration) {
 								FuncDeclaration fd = (FuncDeclaration) member;
 								for(FuncDeclaration old : funcs) {
 									if (ASTDmdNode.equals(fd.ident, old.ident)) {
 										int cov = fd.type.covariant(old.type, semanticContext);
 										if(cov == 1) {
 											continue loop;
 										}
 									}
 								}
 									
 								suggestOverride(fd);
 							}
 						}
 					}
 				}
 			} else {
 				includesFilter = INCLUDE_VARIABLES;
 				completeNode(node.sourceBaseclasses.get(baseClassIndex).sourceType);
 				includesFilter = 0;
 			}
 		}
 	}
 
 	private void completeSuperDotExp(CompletionOnSuperDotExp node) throws JavaModelException {
 		doSemantic();
 		
 		Type type = node.type;
 		completeTypeDot(type);
 	}
 
 	private void completeThisDotExp(CompletionOnThisDotExp node) throws JavaModelException {
 		doSemantic();
 		
 		Type type = node.type;
 		completeTypeDot(type);
 	}
 	
 	private void completeTypeDot(Type type) {
 		currentName = CharOperation.NO_CHAR;
 		startPosition = actualCompletionPosition;
 		endPosition = actualCompletionPosition;
 		
 		completeType(type, false);
 	}
 	
 	private void completeIdentDot(IdentifierExp ident) {
 		currentName = CharOperation.NO_CHAR;
 		startPosition = actualCompletionPosition;
 		endPosition = actualCompletionPosition;
 		
 		Dsymbol sym = ident.resolvedSymbol;
 		if (sym instanceof VarDeclaration) {
 			VarDeclaration var = (VarDeclaration) sym;
 			Type type = var.type();
 			
 			currentName = CharOperation.NO_CHAR;
 			completeType(type, false /* not only statics */);
 		} else if (sym instanceof ScopeDsymbol) {
 			completeScopeDsymbol((ScopeDsymbol) sym, true /* only statics */, INCLUDE_ALL);
 		}
 	}
 	
 	private void completeScope(Scope scope, int includes) {
 		// No current scope properties
 		wantProperties = false;
 		completeScope0(scope, includes);
 		wantProperties = true;
 	}
 
 	private void completeScope0(Scope scope, int includes) {
 		if (scope == null) {
 			return;
 		}
 		
 		if (scope.enclosing != null) {
 			completeScope0(scope.enclosing, includes);
 		}
 		
 		Dsymbol sym = getScopeSymbol(scope);
 		if (sym ==  null) {
 			return;
 		}
 		
 		// If it's a with(...) { }
 		WithScopeSymbol wsc = sym.isWithScopeSymbol();
 		if (wsc != null) {
 			VarDeclaration var = wsc.withstate.wthis;
 			if (var != null) {
 				isWithScopeSymbol = true;
 				completeType(var.type, false /* not only statics */);
 				isWithScopeSymbol = false;
 			}
 		} else {
 			if (sym instanceof ScopeDsymbol) {
 				ScopeDsymbol scopeDsymbol = ((ScopeDsymbol) sym).unlazy(semanticContext);
 				if (scopeDsymbol.members != null) {
 					boolean onlyStatics = false;
 					
 					// If the completion is inside a method, and this is the time
 					// to suggest class members, if the class is the one defining the method,
 					// and the method is static, then only suggest static stuff
 					if (rootScope.func != null && rootScope.func.storage_class == STC.STCstatic
 							&& rootScope.func.parent == scopeDsymbol) {
 						onlyStatics = true;
 					}
 					
 					completeScopeDsymbol(scopeDsymbol, onlyStatics, includes);
 				}
 				if (scopeDsymbol.imports != null) {
 					// TODO optimize this
 					Dsymbols syms = new Dsymbols();
 					for(ScopeDsymbol scopeSym : scopeDsymbol.imports) {
 						if (!(scopeSym instanceof TemplateMixin)) {
 							syms.add(scopeSym);
 						}
 					}
 					
 					suggestMembers(syms, false /* not only statics */, new HashtableOfCharArrayAndObject(), includes);	
 				}
 				
 				if (scopeDsymbol.members != null || scopeDsymbol.imports != null) {
 					return;
 				}
 			}
 			
 			DsymbolTable table = scope.scopesym.symtab;
 			if (table == null) {
 				return;
 			}
 			
 			for(char[] key : table.keys()) {
 				if (key == null) {
 					continue;
 				}
 				
 				Dsymbol dsymbol = table.lookup(key);
 				if (dsymbol != null) {
 					suggestDsymbol(dsymbol, includes);
 				}
 			}
 		}
 	}
 	
 	private void completeScopeDsymbol(ScopeDsymbol sd, boolean onlyStatics, int includes) {
 		sd = sd.unlazy(semanticContext);
 		if (sd instanceof ClassDeclaration) {
 			completeTypeClass((TypeClass) sd.type(), onlyStatics);
 		} else if (sd.members != null && !sd.members.isEmpty()) {
 			// If the members are from a Module, don't restrict to "static" symbols
 			if (sd instanceof Module) {
 				sd = ((Module) sd).unlazy(semanticContext);
 			}
 			
 			suggestMembers(sd.members, sd instanceof Module ? false : onlyStatics, 0, new HashtableOfCharArrayAndObject(), includes);
 		}
 	}
 
 	private Dsymbol getScopeSymbol(Scope scope) {
 		scope.scopesym = scope.scopesym == null ? null : scope.scopesym.unlazy(semanticContext);
 		if (scope.scopesym == null || scope.scopesym.members == null
 				|| scope.scopesym.members.isEmpty()) {
 			return null;
 		} else {
 		 return scope.scopesym.members.get(0);
 		}
 	}
 	private void completeExpression(Expression e1, IdentifierExp ident) throws JavaModelException {
 		isBetweenMethodName = ident != null && ident.resolvedSymbol != null;
 		
 		while(e1 instanceof CommaExp) {
 			e1 = ((CommaExp) e1).e2;
 		}
 		
 		if (e1 instanceof VarExp) {
 			VarExp var = (VarExp) e1;
 			Declaration decl = var.var;
 			if (decl == null) {
 				return;
 			}
 			
 			Type type = decl.type();
 			completeType(type, ident, false /* not only statics */);
 		} else if (e1 instanceof DotExp) {
 			// This is the case of a mixin
 			DotExp dot = (DotExp) e1;
 			completeExpression(dot.e2, ident);
 		} else if (e1 instanceof DotVarExp) {
 			DotVarExp var = (DotVarExp) e1;
 			Declaration decl = var.var;
 			if (decl == null) {
 				return;
 			}
 			
 			Type type = decl.type();
 			completeType(type, ident, false /* not only statics */);
 		} else if (e1 instanceof CallExp) {
 			Type type = ((CallExp) e1).type;
 			completeType(type, ident, false /* not only statics */);
 		} else if (e1 instanceof TypeExp) {
 			Type type = e1.type;
 			completeType(type, ident, true /* only statics */);
 		} else if (e1 instanceof StringExp) {
 			Type type = e1.type;
 			completeType(type, ident, true /* only statics */);
 		} else if (e1 instanceof ScopeExp) {
 			ScopeExp se = (ScopeExp) e1;
 			if (se.sds instanceof Module) {
 				currentName = computePrefixAndSourceRange(ident);
 				suggestMembers((((Module) se.sds)).unlazy(semanticContext).members, 
 						false /* not only statics */, 
 						new HashtableOfCharArrayAndObject(), INCLUDE_ALL);
 			} else if (se.sds instanceof ClassDeclaration) {
 				currentName = computePrefixAndSourceRange(ident);
 				suggestMembers((((ClassDeclaration) se.sds)).unlazy(semanticContext).members, 
 						false /* not only statics */, 
 						new HashtableOfCharArrayAndObject(), INCLUDE_ALL);
 			} else if (se.sds instanceof StructDeclaration) {
 				currentName = computePrefixAndSourceRange(ident);
 				suggestMembers((((StructDeclaration) se.sds)).unlazy(semanticContext).members, 
 						false /* not only statics */, 
 						new HashtableOfCharArrayAndObject(), INCLUDE_ALL);
 			} else if (se.sds instanceof Package) {
 				char[] name = ident.ident;
 				this.startPosition = se.start;
 				this.endPosition = se.start + se.length;
 				completePackage((Package) se.sds, name, ident);
 			} else if (se.sds instanceof TemplateInstance) {
 				currentName = computePrefixAndSourceRange(ident);
 				options.checkVisibility = false;
 				suggestMembers(((TemplateInstance) se.sds).members, false, 0, new HashtableOfCharArrayAndObject(), INCLUDE_ALL);
 				options.checkVisibility = true;
 			}
 		} else if (e1 instanceof DsymbolExp) {
 			DsymbolExp de = (DsymbolExp) e1;
 			if (de.s instanceof TemplateInstance) {
 				currentName = computePrefixAndSourceRange(ident);
 				options.checkVisibility = false;
 				suggestMembers(((TemplateInstance) de.s).members, false, 0, new HashtableOfCharArrayAndObject(), INCLUDE_ALL);
 				options.checkVisibility = true;
 			}
 		} else if (e1 instanceof ThisExp) {
 			ThisExp thisExp = (ThisExp) e1;
 			Type type = thisExp.type;
 			completeType(type, ident, false);
 		} else if (e1 instanceof PtrExp) {
 			PtrExp ptr = (PtrExp) e1;
 			Type type = ptr.type;
 			completeType(type, ident, false);
 		} else if (e1 instanceof SymOffExp) {
 			SymOffExp symoff = (SymOffExp) e1;
 			Type type = symoff.type;
 			completeType(type, ident, false);
 		} else if (e1 instanceof IndexExp) {
 			IndexExp index = (IndexExp) e1;
 			Type type = index.type;
 			completeType(type, ident, false);
 		} else if (ident.resolvedSymbol != null) {
 			// TODO check this
 			trySuggestCall(ident.resolvedSymbol.type(), ident.ident, CharOperation.NO_CHAR, true /* only statics */);
 			
 			currentName = ident.ident;
 			startPosition = ident.start;
 			endPosition = ident.start + ident.length;
 			
 			suggestDsymbol(ident.resolvedSymbol, INCLUDE_ALL);
 			return;
 		}
 	}
 	
 	private void completePackage(Package sds, char[] name, IdentifierExp ident) {
 		Stack<char[]> pieces = new Stack<char[]>();
 		while(true) {
 			pieces.push(sds.ident.ident);
 			if (sds.parent instanceof Package) {
 				sds = (Package) sds.parent;
 			} else {
 				break;
 			}
 		}
 		
 		char[][] fqnPieces = new char[pieces.size()][];
 		int i = 0;
 		while(!pieces.isEmpty()) {
 			fqnPieces[i] = pieces.pop();
 			i++;
 		}
 		
 		char[] fqn = CharOperation.concatWith(fqnPieces, '.');
 		if (ident.ident != null && ident.ident.length != 0) {
 			fqn = CharOperation.concat(fqn, ident.ident, '.');
 		}
 		
 		// Also include the next dot, if any
 		if (this.endPosition < this.source.length && this.source[this.endPosition] == '.') {
 			this.endPosition++;
 		}
 		
 		isCompletingPackage = true;
 		if (fqn.length > 0) {
 			nameEnvironment.findCompilationUnits(fqn, this);
 		}
 	}
 
 	private void completeType(Type type, IdentifierExp ident, boolean onlyStatics) {
 		if (type == null) {
 			return;
 		}
 		
 		currentName = computePrefixAndSourceRange(ident);
 		completeType(type, onlyStatics);
 	}
 	
 	private void completeType(Type type, boolean onlyStatics) {
 		if (type == null) {
 			return;
 		}
 		
 		switch(type.getNodeType()) {
 		case ASTDmdNode.TYPE_BASIC:
 			completeTypeBasicProperties((TypeBasic) type);
 			break;
 		case ASTDmdNode.TYPE_CLASS:
 			completeTypeClass((TypeClass) type, onlyStatics);
 			break;
 		case ASTDmdNode.TYPE_STRUCT:
 			completeTypeStruct((TypeStruct) type, onlyStatics);
 			break;
 		case ASTDmdNode.TYPE_ENUM:
 			completeTypeEnum((TypeEnum) type);
 			break;
 		case ASTDmdNode.TYPE_S_ARRAY:
 			suggestTypeStaticArrayProperties((TypeSArray) type);
 			suggestExtensionMethods((TypeArray) type);
 			break;
 		case ASTDmdNode.TYPE_D_ARRAY:
 			suggestTypeDynamicArrayProperties((TypeDArray) type);
 			suggestExtensionMethods((TypeArray) type);
 			break;
 		case ASTDmdNode.TYPE_A_ARRAY:
 			suggestTypeAssociativeArrayProperties((TypeAArray) type);
 			suggestExtensionMethods((TypeArray) type);
 			break;
 		case ASTDmdNode.TYPE_POINTER:
 			// Allow completion for:
 			// struct Foo { int x; }
 			// Foo* f; f.|
 			TypePointer pointer = (TypePointer) type;
 			if (pointer.next instanceof TypeStruct) {
 				completeTypeStruct((TypeStruct) pointer.next, onlyStatics);
 			}
 			break;
 		}
 	}
 	
 	private void suggestExtensionMethods(TypeArray array) {
 		suggestExtensionMethods(array, module, true);
 	}
 	
 	private void suggestExtensionMethods(TypeArray array, Module module, boolean original) {
 		module.unlazy(semanticContext);
 		
 		suggestExtensionMethods(array, module.members);
 		
 		if (CharOperation.equals(Id.object, module.ident.ident)) {
 			return;
 		}
 		
 		for(Object o : module.members) {
 			if (o instanceof Import) {
 				Import imp = (Import) o;
 				if (original || imp.ispublic) {
 					suggestExtensionMethods(array, imp.mod, false);
 				}
 			}
 		}
 	}
 	
 	private void suggestExtensionMethods(TypeArray array, Dsymbols dsymbols) {
 		for(Dsymbol s : dsymbols) {
 			suggestExtensionMethod(array, s);
 		}
 	}
 
 	private void suggestExtensionMethod(TypeArray array, Dsymbol s) {
 		if (s instanceof AttribDeclaration) {
 			AttribDeclaration attrib = (AttribDeclaration) s;
 			switch(attrib.getNodeType()) {
 			case ASTDmdNode.ALIGN_DECLARATION:
 			case ASTDmdNode.ANON_DECLARATION:
 			case ASTDmdNode.COMPILE_DECLARATION:
 			case ASTDmdNode.LINK_DECLARATION:
 			case ASTDmdNode.PRAGMA_DECLARATION:
 			case ASTDmdNode.PROT_DECLARATION:
 			case ASTDmdNode.STORAGE_CLASS_DECLARATION:
 				suggestExtensionMethods(array, attrib.decl);
 				break;
 			case ASTDmdNode.CONDITIONAL_DECLARATION:
 				ConditionalDeclaration conditional = (ConditionalDeclaration) attrib;
 				if (conditional.condition.inc == 1) {
 					suggestExtensionMethods(array, conditional.decl);
 				} else if (conditional.condition.inc == 2) {
 					suggestExtensionMethods(array, conditional.elsedecl);
 				}
 				break;
 			}
 			return;
 		}
 		
 		if (!isVisible(s)) {
 			return;
 		}
 		
 		if (s instanceof FuncDeclaration) {
 			FuncDeclaration f = (FuncDeclaration) s;
 			suggestExtensionMethod(array, f, false /* is not template */);
 		} else if (s instanceof TemplateDeclaration) {
 			TemplateDeclaration t = (TemplateDeclaration) s;
 			if (t.parameters != null && t.parameters.size() == 1) {
 				for(Dsymbol s2 : t.members) {
 					if (s2 instanceof FuncDeclaration) {
 						FuncDeclaration f = (FuncDeclaration) s2;
 						suggestExtensionMethod(array, f, true /* is template */);
 					}
 				}
 			}
 		}
 	}
 
 	private void suggestExtensionMethod(TypeArray array, FuncDeclaration f, boolean isTemplate) {
 		if (f.ident == null || f.ident.ident == null) {
 			return;
 		}
 		
 		char[] ident = f.ident.ident;
 		if (!match(this.currentName, ident)) {
 			return;
 		}
 		
 		if (!(f.type instanceof TypeFunction)) {
 			return;
 		}
 		
 		TypeFunction tf = (TypeFunction) f.type;
 		if (tf.parameters != null && tf.parameters.size() > 0 &&
 				((!isTemplate && tf.parameters.get(0).type != null &&
 				array.deco.equals(tf.parameters.get(0).type.deco)) ||
				(isTemplate && tf.parameters.get(0).type instanceof TypeIdentifier))) {
 			int relevance = computeBaseRelevance();
 			relevance += computeRelevanceForInterestingProposal();
 			relevance += computeRelevanceForCaseMatching(currentName, ident);
 			relevance += R_METHOD;
 			
 			CompletionProposal proposal = this.createProposal(CompletionProposal.EXTENSION_METHOD, this.actualCompletionPosition, f);
 			proposal.setName(ident);
 			
 			if (wantMethodContextInfo) {
 				handleContextInfo(f, false /* func name is not opCall */, proposal);
 			} else {
 				handleMethodCompletion(proposal, ident);
 			}
 			
 			proposal.setTypeSignature(f.type.getSignature().toCharArray());
 			proposal.setSignature(f.getSignature().toCharArray());
 			proposal.setDeclarationSignature(f.parent.getSignature().toCharArray());
 			proposal.setFlags(f.getFlags());
 			proposal.setRelevance(relevance);
 			proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 			CompletionEngine.this.requestor.accept(proposal);
 		}
 	}
 
 	private void suggestTypeDelegate(TypeDelegate type, char[] name, char[] signature) {
 		Type next = type.next;
 		if (next instanceof TypeFunction) {
 			suggestTypeFunction((TypeFunction) next, name, signature);
 		}
 	}
 
 	private void suggestTypePointer(TypePointer type, char[] name, char[] signature) {
 		Type next = type.next;
 		if (next instanceof TypeFunction) {
 			suggestTypeFunction((TypeFunction) next, name, signature);
 		}
 	}
 	
 	private void suggestTypeFunction(TypeFunction type, char[] name, char[] signature) {
 		if (isCompletingTypeIdentifier) {
 			return;
 		}
 		
 		// Replace variable for function
 		int lastIndexOfVar = CharOperation.lastIndexOf(Signature.C_VARIABLE, signature);
 		if (lastIndexOfVar != -1) {
 			signature[lastIndexOfVar] = Signature.C_FUNCTION;
 		}
 		
 		char[] typeSignature = type.getSignature().toCharArray();
 		char[] declSignature = CharOperation.concat(signature, typeSignature);
 		suggestTypeFunction(declSignature, typeSignature, name);
 	}
 	
 	private void suggestTypeFunction(char[] signature, char[] typeSignature, char[] name) {
 		CompletionProposal proposal = this.createProposal(CompletionProposal.FUNCTION_CALL, this.actualCompletionPosition, null);
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForInterestingProposal();
 		relevance += computeRelevanceForCaseMatching(currentName, name);
 		relevance += R_FUNCTION_POINTER_CALL;
 		
 		// If it's the same name as the variable, prefer the call
 		if (CharOperation.equals(currentName, name)) {
 			relevance += R_FUNCTION_POINTER_CALL_WANT;
 		}
 		
 		proposal.setName(name);
 		
 		if (wantMethodContextInfo) {
 			proposal.setCompletion(CharOperation.NO_CHAR);
 		} else {
 			handleMethodCompletion(proposal, currentName);
 		}
 		
 		proposal.setSignature(signature);
 		proposal.setTypeSignature(typeSignature);
 		proposal.setRelevance(relevance);
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		CompletionEngine.this.requestor.accept(proposal);
 	}
 
 	private void completeTypeBasicProperties(TypeBasic type) {
 		// Nothing for type void
 		if (type.ty == TY.Tvoid) {
 			return;
 		}
 		
 		suggestAllTypesProperties(type);
 		
 		if (type.isintegral()) {
 			suggestIntegralProperties(type.getSignature().toCharArray());
 		}
 		
 		if (type.isfloating()) {
 			suggestFloatingProperties(type);
 		}
 		
 		if (type.iscomplex()) {
 			suggestComplextProperties(type);
 		}
 	}
 	
 	public final static char[][] allTypesProperties = { Id.init, Id.__sizeof, Id.alignof, Id.mangleof, Id.stringof };
 	private void suggestAllTypesProperties(Type type) {
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				allTypesProperties, 
 				new Type[] { type, typeInt, typeInt, typeCharArray, typeCharArray }, 
 				R_BUILTIN_PROPERTY);
 	}
 	
 	public final static char[][] integralTypesProperties = { Id.max, Id.min };
 	private void suggestIntegralProperties(char[] declarationSignature) {
 		suggestProperties(
 				declarationSignature,
 				integralTypesProperties, 
 				new Type[] { typeInt, typeInt },
 				R_INTERESTING_BUILTIN_PROPERTY);
 	}
 	
 	public final static char[][] floatingPointTypesProperties = { Id.infinity, Id.nan, Id.dig, Id.epsilon, Id.mant_dig, Id.max_10_exp, Id.max_exp, Id.min_10_exp, Id.min_exp, Id.max, Id.min };
 	private void suggestFloatingProperties(Type type) {
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				floatingPointTypesProperties, 
 				new Type[] { type, type, typeInt, type, typeInt, typeInt, typeInt, typeInt, typeInt, type, type },
 				R_INTERESTING_BUILTIN_PROPERTY);
 	}
 	
 	public final static char[][] complextTypesProperties = { Id.re, Id.im };
 	private void suggestComplextProperties(Type type) {
 		Type component;
 		switch(type.ty) {
 		case Tcomplex32: component = Type.tfloat32; break;
 		case Tcomplex64: component = Type.tfloat64; break;
 		case Tcomplex80: component = Type.tfloat80; break;
 		default: throw new IllegalStateException("Should not happen");
 		}
 		
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				complextTypesProperties, 
 				new Type[] { component, component },
 				R_VERY_INTERESTING_BUILTIN_PROPERTY);
 	}
 	
 	public final static char[][] staticAndDynamicArrayProperties = { Id.dup, Id.sort, Id.length, Id.ptr, Id.reverse };
 	public final static char[][] staticAndDynamicArrayProperties2 = { Id.idup };
 
 	private void suggestTypeStaticArrayProperties(TypeSArray type) {
 		suggestAllTypesProperties(type);
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				staticAndDynamicArrayProperties, 
 				new Type[] { type, type, typeInt, typeInt, type },
 				R_INTERESTING_BUILTIN_PROPERTY);
 	}
 	
 	private void suggestTypeDynamicArrayProperties(TypeDArray type) {
 		suggestAllTypesProperties(type);
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				staticAndDynamicArrayProperties,
 				new Type[] { type, type, typeInt, typeInt, type },
 				R_INTERESTING_BUILTIN_PROPERTY);
 		if (semanticContext.isD2()) {
 			suggestProperties(
 					type.getSignature().toCharArray(),
 					staticAndDynamicArrayProperties2,
 					new Type[] { type.invariantOf(semanticContext) },
 					R_INTERESTING_BUILTIN_PROPERTY);
 		}
 	}
 	
 	public final static char[][] associativeArrayProperties = { Id.length, Id.keys, Id.values, Id.rehash };
 	private void suggestTypeAssociativeArrayProperties(TypeAArray type) {
 		suggestAllTypesProperties(type);
 		suggestProperties(
 				type.getSignature().toCharArray(),
 				associativeArrayProperties,
 				new Type[] { typeInt, 
 					new TypeDArray(type.index), 
 					new TypeDArray(type.next),
 					type },
 				R_INTERESTING_BUILTIN_PROPERTY);
 	}
 	
 	private void completeTypeClass(TypeClass type, boolean onlyStatics) {
 		// Keep a hashtable of already used signatures, in order to avoid
 		// suggesting overriden functions
 		completeTypeClassRecursively(type, onlyStatics, new HashtableOfCharArrayAndObject());
 		
 		// And also all type's properties
 		suggestAllTypesProperties(type);
 	}
 	
 	private void completeTypeClassRecursively(TypeClass type, boolean onlyStatics, HashtableOfCharArrayAndObject funcSignatures) {
 		ClassDeclaration decl = type.sym == null ? null : type.sym.unlazy(this.currentName, semanticContext);
 		if (decl == null) {
 			return;
 		}
 		
 		// First suggest my members
 		suggestMembers(decl.members, onlyStatics, funcSignatures, INCLUDE_ALL);
 		
 		// Then suggest superclass and superinterface members
 		BaseClasses baseClasses = decl.baseclasses;
 		if (baseClasses.isEmpty()) {
 			// Suggest Object if no base classes are present and this is not object
 			if (semanticContext.ClassDeclaration_object != null && 
 				type != semanticContext.ClassDeclaration_object.type) {
 				completeTypeClassRecursively((TypeClass) semanticContext.ClassDeclaration_object.type, onlyStatics, funcSignatures);
 			}
 		} else {
 			for(BaseClass baseClass : baseClasses) {
 				completeTypeClassRecursively((TypeClass) baseClass.base.type, onlyStatics, funcSignatures);
 			}
 		}
 	}
 	
 	private void completeTypeStruct(TypeStruct type, boolean onlyStatics) {
 		StructDeclaration decl = type.sym == null ? null : type.sym.unlazy(this.currentName, semanticContext);
 		if (decl == null) {
 			return;
 		}
 		
 		// Suggest members
 		suggestMembers(decl.members, onlyStatics, null, INCLUDE_ALL);
 		
 		// And also all type's properties
 		suggestAllTypesProperties(type);
 	}
 	
 	private void completeTypeEnum(TypeEnum type) {
 		EnumDeclaration enumDeclaration = type.sym;
 		if (enumDeclaration == null) {
 			return;
 		}
 		
 		// Suggest enum members
 		completeEnumMembers(enumDeclaration, null, false /* don't use fqn */);
 		
 		// And also all type's properties
 		suggestAllTypesProperties(type);
 		
 		// If the base type of the enum is integral, also suggest integer properties
 		if (enumDeclaration.memtype.isintegral()) {
 			suggestIntegralProperties(enumDeclaration.getSignature().toCharArray());
 		}
 	}
 	
 	private void suggestOverride(FuncDeclaration fd) {
 		CompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION, this.actualCompletionPosition, fd);
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForInterestingProposal();
 		relevance += computeRelevanceForCaseMatching(currentName, fd.ident.ident);
 		relevance += R_METHOD_OVERIDE;
 		
 		proposal.setName(fd.ident.ident);
 		
 		handleMethodCompletion(proposal, fd.ident.ident);
 		
 		proposal.setSignature(fd.getSignature().toCharArray());
 		proposal.setTypeSignature(fd.type.getSignature().toCharArray());
 		proposal.setDeclarationSignature(fd.parent.getSignature().toCharArray());
 		proposal.setRelevance(relevance);
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		CompletionEngine.this.requestor.accept(proposal);
 	}
 	
 	private void suggestMembers(Dsymbols members, boolean onlyStatics, HashtableOfCharArrayAndObject funcSignatures, int includes) {
 		suggestMembers(members, onlyStatics, 0, funcSignatures, includes);
 	}
 	
 	private void suggestMembers(Dsymbols members, boolean onlyStatics, long flags, HashtableOfCharArrayAndObject funcSignatures, int includes) {
 		if (members == null || members.isEmpty()) {
 			return;
 		}
 		
 		int length = members.size();
 		for(int i = 0; i < length; i++) {
 			suggestMember(members.get(i), onlyStatics, flags, funcSignatures, includes);
 		}
 	}
 	
 	private void suggestDsymbol(Dsymbol dsymbol, int includes) {
 		suggestMember(dsymbol, false, 0, null, includes);
 	}
 	
 	/*
 	 * Suggest a member with it's name.
 	 */
 	private void suggestMember(Dsymbol member, boolean onlyStatics, long flags, HashtableOfCharArrayAndObject funcSignatures, int includes) {
 		if (member instanceof AttribDeclaration) {
 			AttribDeclaration attrib = (AttribDeclaration) member;
 			switch(attrib.getNodeType()) {
 			case ASTDmdNode.ALIGN_DECLARATION:
 			case ASTDmdNode.ANON_DECLARATION:
 			case ASTDmdNode.COMPILE_DECLARATION:
 			case ASTDmdNode.LINK_DECLARATION:
 			case ASTDmdNode.PRAGMA_DECLARATION:
 			case ASTDmdNode.PROT_DECLARATION:
 			case ASTDmdNode.STORAGE_CLASS_DECLARATION:
 				suggestMembers(attrib.decl, onlyStatics, flags | attrib.getFlags(), funcSignatures, includes);
 				break;
 			case ASTDmdNode.CONDITIONAL_DECLARATION:
 				ConditionalDeclaration conditional = (ConditionalDeclaration) attrib;
 				if (conditional.condition.inc == 1) {
 					suggestMembers(conditional.decl, onlyStatics, flags | attrib.getFlags(), funcSignatures, includes);
 				} else if (conditional.condition.inc == 2) {
 					suggestMembers(conditional.elsedecl, onlyStatics, flags | attrib.getFlags(), funcSignatures, includes);
 				}
 				break;
 			}
 			return;
 		}
 		
 		if (member.ident == null || member.ident.ident == null) {
 			// Suggest member of anynoymous symbols
 			if (member instanceof StructDeclaration ||
 				member instanceof EnumDeclaration) {
 				suggestMembers(((ScopeDsymbol) member).unlazy(semanticContext).members, onlyStatics, flags, funcSignatures, includes);
 			}
 			return;
 		}
 		
 		// Skip template instances!
 		if (member instanceof TemplateInstance && !(member instanceof TemplateMixin)) {
 			return;
 		}
 		
 		suggestMember(member, member.ident.ident, onlyStatics, flags, funcSignatures, includes);
 	}		
 		
 	/*
 	 * Suggest a member with another name (for aliases).
 	 */
 	private void suggestMember(Dsymbol member, char[] ident, boolean onlyStatics, long flags, HashtableOfCharArrayAndObject funcSignatures, int includes) {
 		suggestMember(member, ident, onlyStatics, flags, funcSignatures, includes, false);
 	}
 	
 	private void suggestMember(Dsymbol member, char[] ident, boolean onlyStatics, long flags, HashtableOfCharArrayAndObject funcSignatures, int includes, boolean isAliased) {
 		if (includesFilter != 0) {
 			includes &= ~includesFilter;
 		}
 		
 		if (member.isStaticCtorDeclaration() != null ||
 			member instanceof StaticDtorDeclaration) {
 			return;
 		}
 		
 		if (member.isImport() != null) {
 			Import imp = ((Import) member);
 			
 			// Don't suggest imports from imported modules
 			if (!imp.ispublic && (includes & INCLUDE_IMPORTS) == 0) {
 				return;
 			}
 			
 			Module mod = imp.mod;
 			if (mod != null) {
 				char[] fqn = mod.getFullyQualifiedName().toCharArray();
 				if (!suggestedModules.containsKey(fqn)) {
 					suggestedModules.put(fqn, this);
 					mod = mod.unlazy(this.currentName, semanticContext);
 					suggestMembers(mod.members, false, funcSignatures, includes & (~INCLUDE_IMPORTS));
 				}
 			}
 			return;
 		}
 		
 		if (member.isTemplateMixin() != null) {
 			TemplateMixin mixin = (TemplateMixin) member;
 			if (mixin.sourceIdent != null && mixin.sourceIdent.ident != null &&
 				match(this.currentName, ident)) {
 				
 				// Suggest as if it were a variable declaration
 				String signature = mixin.parent.getSignature() + Signature.C_VARIABLE + ident.length + new String(ident);
 				
 				char[] sig = signature.toCharArray();
 				char[] typeName = mixin.inst.getSignature().toCharArray();
 				
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(currentName, ident);
 				
 				boolean isLocal = mixin.parent instanceof FuncDeclaration;
 				
 				if (isLocal) {
 					relevance += R_LOCAL_VAR;
 				} else {
 					relevance += R_VAR;	
 				}
 				
 				CompletionProposal proposal = this.createProposal(isLocal ? CompletionProposal.LOCAL_VARIABLE_REF : CompletionProposal.FIELD_REF, this.actualCompletionPosition, mixin);
 				proposal.setName(ident);
 				proposal.setCompletion(ident);
 				proposal.setTypeSignature(typeName);
 				proposal.setSignature(sig);
 				proposal.setDeclarationSignature(mixin.parent.getSignature().toCharArray());
 				proposal.setFlags(flags | mixin.getFlags());
 				proposal.setRelevance(relevance);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 			
 			suggestMembers(mixin.members, onlyStatics, funcSignatures, includes);
 			return;
 		}
 		
 		if (!isVisible(member)) {
 			return;
 		}
 		
 		// Types act like statics
 		boolean isType = member instanceof ClassDeclaration || 
 						member instanceof StructDeclaration ||
 						member instanceof EnumDeclaration;
 		
 		// Filter statics if only statics were requested
 		Declaration decl = member.isDeclaration();
 		if (decl != null) {
 			if (!isType && !decl.isStatic() && !decl.isConst() && onlyStatics 
 					&& !(member instanceof AliasDeclaration) 
 					&& !(member instanceof TypedefDeclaration)
 					) {
 				return;
 			}
 		}
 		
 		// If it's an alias to a symbol, suggest it and return
 		AliasDeclaration alias = member.isAliasDeclaration();
 		if (alias != null) {
 			Dsymbol sym = alias.toAlias(semanticContext);
 			if (sym != null && sym != alias) {
 				suggestMember(sym, ident, onlyStatics, flags, funcSignatures, includes, true);
 				
 				if (sym instanceof FuncAliasDeclaration) {
 					sym = ((FuncAliasDeclaration) sym).funcalias;
 				}
 				
 				// Find overloads! :-)
 				while(sym instanceof FuncDeclaration) {
 					FuncDeclaration func = (FuncDeclaration) sym;
 					
 					semanticContext.allowOvernextBySignature = true;				
 					Declaration funcDecl = func.overnext;
 					if (funcDecl != null && funcDecl != sym) {
 						suggestMember(funcDecl, ident, onlyStatics, flags, funcSignatures, includes);
 					}
 					sym = funcDecl;
 				}
 				return;
 			}
 		}
 		
 		// See if it's a variable
 		if ((includes & INCLUDE_VARIABLES) != 0 && !isCompletingTypeIdentifier && !parser.inNewExp) {
 			VarDeclaration var = member.isVarDeclaration();
 			if (var != null && ident != null) {
 				if (currentName.length == 0 || match(currentName, ident)) {
 					String signature = var.getSignature();
 					
 					char[] sig = signature.toCharArray();
 					char[] typeName = var.type().getSignature().toCharArray();
 					
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(currentName, ident);
 					relevance += computeRelevanceForExpectedType(var.type());
 					
 					// If it's:
 					// type name = ...
 					// give "name" lower relevance
 					// Same for:
 					// name = ...
 					if (ASTDmdNode.equals(targetIdent, ident)) {
 						relevance -= RelevanceConstants.R_INTERESTING;
 					}
 					
 					boolean isLocal = var.parent instanceof FuncDeclaration;
 					
 					if (isLocal) {
 						relevance += R_LOCAL_VAR;
 					} else {
 						relevance += R_VAR;	
 					}
 					
 					CompletionProposal proposal = this.createProposal(isLocal ? CompletionProposal.LOCAL_VARIABLE_REF : CompletionProposal.FIELD_REF, this.actualCompletionPosition, var);
 					proposal.setName(ident);
 					proposal.setCompletion(ident);
 					proposal.setTypeSignature(typeName);
 					proposal.setSignature(sig);
 					proposal.setDeclarationSignature(var.parent.getSignature().toCharArray());
 					proposal.setFlags(flags | var.getFlags());
 					proposal.setRelevance(relevance);
 					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 					proposal.isAlias = isAliased;
 					CompletionEngine.this.requestor.accept(proposal);
 					
 					if (CharOperation.equals(this.currentName, var.ident.ident)) {
 						trySuggestCall(var.type(), ident, sig, false /* dosen't matter */);
 					}
 				}
 				return;
 			}
 		}
 		
 		if (member instanceof EnumMember && !parser.inNewExp) {
 			if (currentName.length == 0 || match(currentName, ident)) {
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(currentName, ident);
 				relevance += R_VAR;
 				
 				CompletionProposal proposal = this.createProposal(CompletionProposal.ENUM_MEMBER, this.actualCompletionPosition, member);
 				proposal.setName(ident);
 				proposal.setCompletion(ident);
 				proposal.setSignature(member.getSignature().toCharArray());
 				proposal.setFlags(flags | member.getFlags());		
 				proposal.setRelevance(relevance);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				proposal.isAlias = isAliased;
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 			return;
 		}
 		
 		if ((includes & INCLUDE_TYPES) != 0) {
 			if (alias != null) {
 				if (currentName.length == 0 || match(currentName, ident)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(currentName, ident);
 					relevance += R_ALIAS;
 					
 					boolean isLocal = alias.parent instanceof FuncDeclaration;
 					
 					CompletionProposal proposal = this.createProposal(isLocal ? CompletionProposal.LOCAL_VARIABLE_REF : CompletionProposal.FIELD_REF, this.actualCompletionPosition, alias);
 					proposal.setName(ident);
 					proposal.setCompletion(ident);
 					String signature = alias.getSignature();
 					if (signature != null) {
 						proposal.setSignature(signature.toCharArray());
 					}
 					Type type = alias.type();
 					if (type == null) {
 						Dsymbol sym = alias.toAlias(semanticContext);
 						if (sym != null) {
 							type = sym.type();
 							if (type instanceof TypeTypedef) {
 								type = ((TypeTypedef) type).toBasetype(semanticContext);
 							}
 							if (type == null) {
 								return;
 							}
 						} else {
 							return;
 						}
 					}
 					
 					if (parser.inNewExp) {
 						if (type instanceof TypeClass) {
 							ClassDeclaration cd = ((TypeClass) type).sym.unlazy(this.currentName, semanticContext);
 							if (cd.isClassDeclaration() != null && cd.isInterfaceDeclaration() == null) {
 								// If it's abstract, skip
 								if ((cd.getFlags() & Flags.AccAbstract) != 0) {
 									return;
 								}
 								relevance += R_NEW;
 							} else {
 								return;
 							}
 						} else {
 							return;
 						}
 					}
 					
 					String sig = type.getSignature();
 					if (sig == null) {
 						return;
 					}
 					
 					char[] sigChars = sig.toCharArray();
 					relevance += computeRelevanceForExpectedType(type);
 					
 					proposal.setTypeSignature(sigChars);
 					proposal.setFlags(flags | alias.getFlags() | Flags.AccAlias);
 					proposal.setRelevance(relevance);
 					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 					proposal.isAlias = isAliased;
 					CompletionEngine.this.requestor.accept(proposal);
 				}
 				return;
 			}
 			
 			// See if it's a typedef
 			TypedefDeclaration typedef = member.isTypedefDeclaration();
 			if (typedef != null) {
 				if (currentName.length == 0 || match(currentName, ident)) {
 					char[] sigChars = typedef.getSignature().toCharArray();
 					
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(currentName, ident);
 					relevance += computeRelevanceForExpectedType(sigChars);
 					relevance += R_TYPEDEF;
 					
 					boolean isLocal = typedef.parent instanceof FuncDeclaration;
 					
 					CompletionProposal proposal = this.createProposal(isLocal ? CompletionProposal.LOCAL_VARIABLE_REF : CompletionProposal.FIELD_REF, this.actualCompletionPosition, typedef);
 					proposal.setName(ident);
 					proposal.setCompletion(ident);
 					proposal.setSignature(sigChars);
 					proposal.setTypeSignature(typedef.basetype.getSignature().toCharArray());
 					proposal.setFlags(flags | typedef.getFlags() | Flags.AccTypedef);
 					proposal.setRelevance(relevance);
 					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 					proposal.isAlias = isAliased;
 					CompletionEngine.this.requestor.accept(proposal);
 				}
 				return;
 			}
 			
 			
 			if (isType) {
 				if (currentName.length == 0 || match(currentName, ident)) {
 					char[] sigChars = member.getSignature().toCharArray();
 					
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(currentName, ident);
 					relevance += computeRelevanceForExpectedType(member.type());
 					if (parser.inNewExp) {
 						if (member.isClassDeclaration() != null && member.isInterfaceDeclaration() == null) {
 							// If it's abstract, skip
 							if ((member.getFlags() & Flags.AccAbstract) != 0) {
 								return;
 							}
 							relevance += R_NEW;
 						} else {
 							return;
 						}
 					}
 					relevance += R_CLASS;
 					
 					CompletionProposal proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition, member);
 					proposal.setName(ident);
 					proposal.setCompletion(ident);
 					proposal.setSignature(sigChars);
 					proposal.setTypeSignature(sigChars);
 					proposal.setDeclarationSignature(member.parent.getSignature().toCharArray());
 					proposal.setFlags(flags | member.getFlags());
 					proposal.setRelevance(relevance);
 					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 					proposal.isAlias = isAliased;
 					
 					CompletionEngine.this.requestor.accept(proposal);
 					
 //					trySuggestCall(member.type(), ident, sigChars, true /* only statics */);
 					
 					// If we are expecting an enum, suggest it's members! :-)
 					if (expectedType != null && expectedType instanceof TypeEnum 
 							&& member instanceof EnumDeclaration &&
 							expectedType.same(member.getType(semanticContext))) {
 						completeEnumMembers((EnumDeclaration) member, null, true);
 					}
 				}
 			}			
 		}
 		
 		boolean constructor = (includes & INCLUDE_CONSTRUCTORS) != 0;
 		boolean opCall = (includes & INCLUDE_OPCALL) != 0;
 		boolean funcNameIsOpCall = CharOperation.equals(Id.call, ident);
 		
 		if (member.isTemplateDeclaration() != null) {
 			if (currentName.length == 0 || match(currentName, ident)
 					|| (opCall && funcNameIsOpCall)) {
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				
 				if (constructor || opCall) {
 					relevance += R_CAMEL_CASE;
 					relevance += R_CONSTRUCTOR;
 					relevance += R_NEW;
 				} else {
 					relevance += computeRelevanceForCaseMatching(currentName, ident);
 					relevance += R_TEMPLATE;
 				}
 				
 				int kind = CompletionProposal.TEMPLATE_REF;
 				TemplateDeclaration temp = (TemplateDeclaration) member;
 				if (temp.wrapper) {
 					if (funcNameIsOpCall) {
 						kind = CompletionProposal.OP_CALL;
 					} else {
 						Dsymbol first = temp.members.get(0);
 						if (first instanceof FuncDeclaration) {
 							kind = CompletionProposal.TEMPLATED_FUNCTION_REF;
 						} else if (first instanceof AggregateDeclaration) {
 							kind = CompletionProposal.TEMPLATED_AGGREGATE_REF;
 						}
 					}
 				}
 				
 				CompletionProposal proposal = this.createProposal(
 						kind, this.actualCompletionPosition, member);
 				if (opCall && funcNameIsOpCall) {
 					proposal.setName(currentName);
 					proposal.setCompletion(currentName);
 				} else {
 					proposal.setName(ident);
 					proposal.setCompletion(ident);
 				}
 				
 				char[] sig = member.getSignature().toCharArray();
 				proposal.setSignature(sig);
 				proposal.setTypeSignature(sig);
 				proposal.setDeclarationSignature(temp.parent.getSignature().toCharArray());
 				
 				proposal.setFlags(flags | member.getFlags());
 				proposal.setRelevance(relevance);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				proposal.isAlias = isAliased;
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}
 		
 		if (!isCompletingTypeIdentifier && !parser.inNewExp && (includes & INCLUDE_FUNCTIONS) != 0 || 
 				((includes & (INCLUDE_CONSTRUCTORS | INCLUDE_OPCALL)) != 0 && wantConstructorsAndOpCall)) {
 			// See if it's a function
 			FuncDeclaration func = member.isFuncDeclaration();
 			if (func != null) {
 				char[] funcName = ident;
 				
 				// Skip op*, unless currentName starts with op
 				if ((includes & INCLUDE_OPCALL) == 0 && ops.containsKey(funcName) &&
 						!CharOperation.prefixEquals(op, currentName)) {
 					return;
 				}
 				
 				// Skip special functions if not completing constructors and opCall
 				if ((includes & INCLUDE_FUNCTIONS) != 0 && specialFunctions.containsKey(funcName)) {
 					return;
 				}
 				
 				// Don't suggest already suggested functions
 				// Use signature . name, because of alias symbols
 				// (we want to suggest methods twice, if they are aliases,
 				// with different names)
 				char[] signature = func.getFunctionSignature().toCharArray();
 				char[] aliasedSignature = CharOperation.concat(ident, signature, '.');
 				if (funcSignatures != null && funcSignatures.containsKey(aliasedSignature)) {
 					return;
 				}
 				
 				if ((!constructor && !opCall && (currentName.length == 0 || match(currentName, ident)))
 						|| (constructor && CharOperation.equals(funcName, Id.ctor)) 
 						|| (opCall && funcNameIsOpCall)) {
 					
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					if (constructor || opCall) {
 						relevance += R_CAMEL_CASE;
 						relevance += R_CONSTRUCTOR;
 						relevance += R_NEW;
 					} else {
 						relevance += computeRelevanceForCaseMatching(currentName, ident);
 					}
 					relevance += R_METHOD;
 					
 					Type type = func.type();
 					if (type instanceof TypeFunction) {
 						relevance += computeRelevanceForExpectedType(type);
 					}
 					
 					CompletionProposal proposal = this.createProposal(
 							(opCall && funcNameIsOpCall) ? 
 									CompletionProposal.OP_CALL : 
 									CompletionProposal.METHOD_REF, 
 								this.actualCompletionPosition, func);
 					
 					if (parser.isInAddrExp) {
 						proposal.wantArguments = false;
 					}
 					
 					if (constructor || opCall) {
 						proposal.setName(currentName);
 						if (wantMethodContextInfo) {
 							handleContextInfo(func, funcNameIsOpCall, proposal);
 						} else {
 							handleMethodCompletion(proposal, currentName);
 						}
 					} else {
 						proposal.setName(funcName);
 						if (wantMethodContextInfo) {
 							handleContextInfo(func, funcNameIsOpCall, proposal);
 						} else {
 							handleMethodCompletion(proposal, ident);							
 						}
 					}
 					
 					String sig = func.getSignature();
 					if (sig != null) {
 						proposal.setSignature(sig.toCharArray());
 					}
 					proposal.setTypeSignature(type.getSignature().toCharArray());
 					proposal.setFlags(flags | func.getFlags());
 					proposal.setRelevance(relevance);
 					proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 					proposal.isAlias = isAliased;
 					if (func.parent != null) {
 						String parentSignature = func.parent.getSignature();
 						if (parentSignature != null) {
 							proposal.setDeclarationSignature(parentSignature.toCharArray());
 						}
 					}
 					
 					CompletionEngine.this.requestor.accept(proposal);
 					
 					if (funcSignatures != null) {
 						funcSignatures.put(aliasedSignature, proposal);
 					}
 				}
 				return;
 			}
 		}
 	}
 	
 	private void handleMethodCompletion(CompletionProposal proposal, char[] ident) {
 		if (isBetweenMethodName) {
 			if (parser.completionToken != null && parser.completionTokenEnd == actualCompletionPosition) {
 				proposal.setCompletion(CharOperation.concat(ident, parenthesis));
 			} else {
 				proposal.setCompletion(ident);
 			}
 		} else {
 			proposal.setCompletion(CharOperation.concat(ident, parenthesis));
 		}
 	}
 
 	private void handleContextInfo(FuncDeclaration func, boolean funcNameIsOpCall, CompletionProposal proposal) {
 		proposal.setCompletion(CharOperation.NO_CHAR);
 		if (funcNameIsOpCall || func instanceof CtorDeclaration) {
 			if (isCompletingThisCall) {
 				proposal.setName(TOK.TOKthis.charArrayValue);
 			} else if (isCompletingSuperCall) {
 				proposal.setName(TOK.TOKsuper.charArrayValue);
 			} else {
 				proposal.setName(func.parent.ident.ident);
 			}
 		}
 	}
 	
 	/*
 	 * Suggest function pointer, delegate, or opCall calls.
 	 */
 	private void trySuggestCall(Type type, char[] ident, char[] signature, boolean onlyStatics) {
 		// We are completing a type, don't suggest a call
 		if (isCompletingTypeIdentifier || !wantConstructorsAndOpCall
 				|| parser.inNewExp) {
 			return;
 		}
 		
 		if (type == null) {
 			return;
 		}
 		
 		if (type instanceof TypeTypedef) {
 			type = ((TypeTypedef) type).toBasetype(semanticContext);
 			if (type == null) {
 				return;
 			}
 		}
 		
 		char[] currentNameSave = currentName;
 		
 		switch(type.getNodeType()) {
 		
 		// function pointer and delegate call
 		case ASTDmdNode.TYPE_POINTER:
 			suggestTypePointer((TypePointer) type, ident, signature);
 			break;
 		case ASTDmdNode.TYPE_DELEGATE:
 			suggestTypeDelegate((TypeDelegate) type, ident, signature);
 			break;
 			
 		// opCall
 		case ASTDmdNode.TYPE_STRUCT: {
 			currentName = ident;
 			StructDeclaration sym = (((TypeStruct) type).sym).unlazy(Id.call, semanticContext);			
 			suggestMembers(sym.members, onlyStatics, new HashtableOfCharArrayAndObject(), INCLUDE_OPCALL);
 			break;
 		}
 		case ASTDmdNode.TYPE_CLASS: {
 			currentName = ident;
 			ClassDeclaration sym = (((TypeClass) type).sym).unlazy(this.currentName, semanticContext);			
 			suggestMembers(sym.members, onlyStatics, new HashtableOfCharArrayAndObject(), INCLUDE_OPCALL);
 			
 			// Then try with superclass and superinterface members
 			BaseClasses baseClasses = sym.baseclasses;
 			if (baseClasses.isEmpty()) {
 				// Suggest Object if no base classes are present and this is not object
 				if (semanticContext.ClassDeclaration_object != null && 
 					type != semanticContext.ClassDeclaration_object.type) {
 					trySuggestCall((TypeClass) semanticContext.ClassDeclaration_object.type, ident, signature, onlyStatics);
 				}
 			} else {
 				for(BaseClass baseClass : baseClasses) {
 					trySuggestCall((TypeClass) baseClass.base.type, ident, signature, onlyStatics);
 				}
 			}
 			break;
 		}
 		}
 		
 		currentName = currentNameSave;
 	}
 
 	private boolean isVisible(Dsymbol member) {
 		if (options.checkVisibility && member.getModule() != this.module) {
 			// private and protected excluded
 			
 			if ((member.getFlags() & (Flags.AccPrivate | Flags.AccProtected)) != 0
 					&& (member.getFlags() & Flags.AccPublic) == 0) {
 				return false;
 			} else if ((member.getFlags() & Flags.AccPackage) != 0) {
 				// package only if in same package
 				Dsymbol myParent = module.parent;
 				Dsymbol hisParent = member.getModule().parent;
 				
 				while(myParent != null && hisParent != null && 
 					myParent instanceof Package && hisParent instanceof Package) {
 					Package myPack = (Package) myParent;
 					Package hisPack = (Package) hisParent;
 					
 					if (!ASTDmdNode.equals(myPack.ident, hisPack.ident)) {
 						return false;
 					}
 					
 					myParent = myParent.parent;
 					hisParent = hisParent.parent;
 				}
 				
 				return myParent == null && hisParent == null;
 			}
 		}
 		
 		if (options.checkDeprecation && (member.getFlags() & Flags.AccDeprecated) != 0) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private boolean isVisible(long modifiers, char[] packageName) {
 		if (options.checkVisibility) { 
 			if ((modifiers & (Flags.AccPrivate | Flags.AccProtected)) != 0) {
 				return false;
 			} else if ((modifiers & Flags.AccPackage) != 0) {
 				int myIndex = CharOperation.indexOf('.', sourceUnitFqn);
 				int hisIndex = CharOperation.indexOf('.', packageName);
 				
 				if (myIndex == -1) {
 					if (hisIndex == -1) {
 						return CharOperation.equals(sourceUnitFqn, packageName);
 					} else {
 						return false;
 					}
 				} else {
 					if (hisIndex != -1) {
 						char[] me = CharOperation.subarray(sourceUnitFqn, 0, myIndex);
 						char[] he = CharOperation.subarray(packageName, 0, hisIndex);
 						return CharOperation.equals(me, he);
 					} else {
 						return false;
 					}
 				}
 			} else {
 				return true;
 			}
 		}
 		
 		if (options.checkDeprecation && (modifiers & Flags.AccDeprecated) != 0) {
 			return false;
 		}
 		
 		return true;
 	}
 
 	private void suggestProperties(
 			char[] declarationSignature,
 			char[][] properties,
 			Type[] types,
 			int relevance) {
 		
 		if (!wantProperties) {
 			return;
 		}
 		
 		for (int i = 0; i < properties.length; i++) {
 			char[] property = properties[i];
 			Type type = types[i];
 			if (currentName.length == 0 || match(currentName, property)) {
 				relevance += computeRelevanceForExpectedType(type);
 				
 				CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition, null);
 				proposal.setRelevance(relevance);
 				proposal.setName(property);
 				proposal.setCompletion(property);
 				proposal.setDeclarationSignature(declarationSignature);
 				proposal.setTypeSignature(type.getSignature().toCharArray());
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}
 	}
 	
 	private void completeEnumMembers(EnumDeclaration enumDeclaration, HashtableOfCharArrayAndObject excludedNames, boolean useQualifiedName) {
 		for(Dsymbol symbol : enumDeclaration.members) {
 			EnumMember member = symbol.isEnumMember();
 			if (member == null) {
 				continue;
 			}
 			
 			char[] proposition;
 			if (useQualifiedName) {
 				proposition = CharOperation.concat(enumDeclaration.ident.ident, member.ident.ident, '.');
 			} else {
 				proposition = member.ident.ident;
 			}
 			if (!(excludedNames != null && excludedNames.containsKey(proposition)) && (
 					currentName.length == 0 || 
 					match(currentName, proposition) || 
 					match(currentName, member.ident.ident)
 					)) {
 				
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(currentName, member.ident.ident);
 				relevance += R_ENUM_CONSTANT;
 				
 				CompletionProposal proposal = this.createProposal(CompletionProposal.ENUM_MEMBER, this.actualCompletionPosition, member);
 				proposal.setName(member.ident.ident);
 				proposal.setCompletion(proposition);
 				proposal.setSignature(member.getSignature().toCharArray());
 				proposal.setFlags(member.getFlags());		
 				proposal.setRelevance(relevance);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}
 	}
 	
 	private void completeJavadoc(CompletionOnJavadocImpl node) {
 		// A very na�ve solution for now...
 		char[] ddocSource = node.getSource();
 		DdocParser parser = new DdocParser(ddocSource);
 		Ddoc ddoc = parser.parse();
 		
 		char[] prefix = findTextBeforeCursor();
 		this.startPosition = this.actualCompletionPosition - prefix.length;
 		this.endPosition = this.actualCompletionPosition;
 		
 		if (isInDdocSectionName(node.getStart(), prefix.length + 1)) {
 			suggestDdocSectionNames(ddoc, prefix);
 		}
 		suggestDdocMacroNames(ddoc, node.getOtherDdocs(), prefix);
 	}
 
 	private char[] findTextBeforeCursor() {
 		StringBuilder sb = new StringBuilder();
 		for(int i = actualCompletionPosition - 1; i >= 0 && 
 			isJavadocLetter(this.source[i]); i--) {
 			sb.insert(0, this.source[i]);
 		}
 		
 		char[] ret = new char[sb.length()];
 		sb.getChars(0, sb.length(), ret, 0);
 		return ret;
 	}
 	
 	private boolean isInDdocSectionName(int start, int backwards) {
 		// Check to see if we are in the beginning of the line
 		for(int i = actualCompletionPosition - backwards; i >= 0; i--) {
 			char c = this.source[i];
 			switch(c) {
 			case ' ':
 			case '\t':
 			case '\r':
 				continue;
 			case '\n':
 				return true;
 			case '*':
 				boolean foundAnotherSpace = false;
 				for(int j = i - 1; j >= 0; j--) {
 					char d = this.source[j];
 					switch(d) {
 					case ' ':
 					case '\t':
 					case '\r':
 						foundAnotherSpace = true;
 						continue;
 					case '\n':
 						return true;
 					case '*':
 						if (foundAnotherSpace) {
 							return false;
 						} else {
 							continue;
 						}
 					case '/':
 						return j == start;
 					default:
 						return false;
 					}
 				}
 				break;
 			default:
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean isJavadocLetter(char c) {
 		return Chars.isalpha(c) || c == '_' || c == '$';
 	}
 
 	private void suggestDdocSectionNames(Ddoc ddoc, char[] prefix) {
 		HashtableOfCharArrayAndObject excludedNames = new HashtableOfCharArrayAndObject();
 		for (int i = 0; i < ddoc.getSections().length; i++) {
 			DdocSection ddocSection = ddoc.getSections()[i];
 			if (ddocSection.getName() != null) {
 				excludedNames.put(ddocSection.getName().toCharArray(), this);
 			}
 		}
 		
 		// Suggest sections that doesn't already exist
 		for(char[] name : ddocSections) {
 			if (!excludedNames.containsKey(name) && 
 					(prefix.length == 0 || match(prefix, name))) {
 				char[] nameColon = new char[name.length + 1];
 				System.arraycopy(name, 0, nameColon, 0, name.length);
 				nameColon[nameColon.length - 1] = ':';
 				
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(prefix, name);
 				
 				CompletionProposal proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition, null);
 				proposal.setName(nameColon);
 				proposal.setCompletion(nameColon);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				proposal.setRelevance(relevance);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}		
 	}
 	
 	private void suggestDdocMacroNames(Ddoc ddoc, List<char[]> otherDdocs, char[] prefix) {
 		Map<String, String> macros = new HashMap<String, String>(DdocMacros.getDefaultMacros());
 		mergeMacros(macros, ddoc);
 		
 		if (otherDdocs != null) {
 			for(char[] otherDdoc : otherDdocs) {
 				DdocParser parser = new DdocParser(otherDdoc);
 				Ddoc other = parser.parse();
 				mergeMacros(macros, other);
 			}
 		}
 		
 		for(Map.Entry<String, String> entry : macros.entrySet()) {
 			String macroName = entry.getKey();
 			String macroValue = entry.getValue();
 			
 			char[] name = new char[macroName.length() + 1];
 			System.arraycopy(macroName.toCharArray(), 0, name, 1, macroName.length());
 			name[0] = '$';
 			if (prefix.length == 0 || match(prefix, name)) {
 				// TODO document this somewhere: the "name" is the
 				// macro replacement, while the completion is the
 				// name of the macro
 				int relevance = computeBaseRelevance();
 				relevance += computeRelevanceForInterestingProposal();
 				relevance += computeRelevanceForCaseMatching(prefix, name);
 				
 				CompletionProposal proposal = this.createProposal(CompletionProposal.DDOC_MACRO, this.actualCompletionPosition, null);
 				proposal.setName(macroValue.toCharArray());
 				proposal.setCompletion(name);
 				proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 				proposal.setRelevance(relevance);
 				CompletionEngine.this.requestor.accept(proposal);
 			}
 		}
 	}
 
 	private void mergeMacros(Map<String, String> macros, Ddoc ddoc) {
 		DdocSection macrosSection = ddoc.getMacrosSection();
 		if (macrosSection != null) {
 			if (macrosSection != null) {
 				Parameter[] params = macrosSection.getParameters();
 				for(Parameter param : params)  {
 					if (param.getName() != null) {
 						macros.put(param.getName(), param.getText());
 					}
 				}
 			}
 		}
 	}
 
 	private char[] computePrefixAndSourceRange(IdentifierExp ident) {
 		char[] prefix;
 		
 		if (parser.completionToken != null) {
 			this.startPosition = parser.completionTokenStart;
 			this.endPosition = parser.completionTokenEnd;
 			prefix = parser.completionToken;
 		} else if (ident == null || ident.ident == null || CharOperation.equals(ident.ident, CharOperation.NO_CHAR)) {
 			this.startPosition = this.actualCompletionPosition;
 			this.endPosition = this.actualCompletionPosition;
 			prefix = CharOperation.NO_CHAR;
 		} else {
 			this.startPosition = ident.start;
 			this.endPosition = ident.start + ident.length;
 			prefix = ident.ident;
 		}
 		
 		return prefix;
 	}
 	
 	private char[][] findExcludedNames(CompletionOnArgumentName node) {
 		TypeFunction parent = node.parentType;
 		if (parent == null || parent.parameters == null) {
 			return CharOperation.NO_CHAR_CHAR;
 		}
 		
 		char[][] excluded = new char[parent.parameters.size() - 1][];
 		for(int i = 0, j = 0; i < parent.parameters.size(); i++) {
 			Argument arg = parent.parameters.get(i);
 			if (arg == node) {
 				continue;
 			}
 			
 			if (arg.ident == null || arg.ident.ident == null) {
 				excluded[j] = CharOperation.NO_CHAR;
 			} else {
 				excluded[j] = arg.ident.ident;
 			}
 			j++;
 		}
 		return excluded;
 	}
 
 	private void findVariableNames(char[] name, Type type, char[][] excludeNames){
 		// compute variable name for non base type
 		final char[] t = name;
 		INamingRequestor namingRequestor = new INamingRequestor() {
 			public void acceptNameWithPrefixAndSuffix(char[] name, boolean isFirstPrefix, boolean isFirstSuffix, int reusedCharacters) {
 				accept(
 						name,
 						(isFirstPrefix ? R_NAME_FIRST_PREFIX : R_NAME_PREFIX) + (isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX),
 						reusedCharacters);
 			}
 
 			public void acceptNameWithPrefix(char[] name, boolean isFirstPrefix, int reusedCharacters) {
 				accept(name, isFirstPrefix ? R_NAME_FIRST_PREFIX :  R_NAME_PREFIX, reusedCharacters);
 			}
 
 			public void acceptNameWithSuffix(char[] name, boolean isFirstSuffix, int reusedCharacters) {
 				accept(name, isFirstSuffix ? R_NAME_FIRST_SUFFIX : R_NAME_SUFFIX, reusedCharacters);
 			}
 
 			public void acceptNameWithoutPrefixAndSuffix(char[] name,int reusedCharacters) {
 				accept(name, 0, reusedCharacters);
 			}
 			void accept(char[] name, int prefixAndSuffixRelevance, int reusedCharacters){
 				if (CharOperation.prefixEquals(t, name, false)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(t, name);
 					relevance += prefixAndSuffixRelevance;
 					if(reusedCharacters > 0) relevance += R_NAME_LESS_NEW_CHARACTERS;
 					//relevance += computeRelevanceForRestrictions(IAccessRule.K_ACCESSIBLE); // no access restriction for variable name
 					
 					// accept result
 					//CompletionEngine.this.noProposal = false;
 					if(!CompletionEngine.this.requestor.isIgnored(CompletionProposal.VARIABLE_DECLARATION)) {
 						CompletionProposal proposal = CompletionEngine.this.createProposal(CompletionProposal.VARIABLE_DECLARATION, CompletionEngine.this.actualCompletionPosition, null);
 						//proposal.setSignature(getSignature(typeBinding));
 						//proposal.setPackageName(q);
 						//proposal.setTypeName(displayName);
 						proposal.setName(name);
 						proposal.setCompletion(name);
 						//proposal.setFlags(Flags.AccDefault);
 						proposal.setReplaceRange(CompletionEngine.this.startPosition - CompletionEngine.this.offset, CompletionEngine.this.endPosition - CompletionEngine.this.offset);
 						proposal.setRelevance(relevance);
 						CompletionEngine.this.requestor.accept(proposal);
 					}
 				}
 			}
 		};
 		
 		InternalNamingConventions.suggestArgumentNames(
 				this.javaProject, 
 				CharOperation.NO_CHAR, 
 				InternalNamingConventions.readableName(type), 
 				0, 
 				name, 
 				excludeNames, 
 				namingRequestor);
 	}
 	
 	private void findKeywords(char[] keyword, char[][] choices, boolean canCompleteEmptyToken) {
 		if(choices == null || choices.length == 0) return;
 		
 		boolean isExpectingBool = expectedTypeSignature != null && expectedTypeSignature.length == 1 &&
 			expectedTypeSignature[0] == TY.Tbool.mangleChar;
 		
 		if (isExpectingBool) {
 			choices = new char[][] { TOK.TOKtrue.charArrayValue, TOK.TOKfalse.charArrayValue };
 			canCompleteEmptyToken = true;
 		}
 			
 		int length = keyword.length;
 		if (canCompleteEmptyToken || length > 0) {
 			for (int i = 0; i < choices.length; i++) {
 				if (length <= choices[i].length
 					&& CharOperation.prefixEquals(keyword, choices[i], false /* ignore case */
 				)) {
 					int relevance = computeBaseRelevance();
 					relevance += computeRelevanceForInterestingProposal();
 					relevance += computeRelevanceForCaseMatching(keyword, choices[i]);
 					
 					if (isExpectingBool && (
 							CharOperation.equals(choices[i], TOK.TOKtrue.charArrayValue) ||
 							CharOperation.equals(choices[i], TOK.TOKfalse.charArrayValue))) {
 						relevance += R_EXACT_EXPECTED_TYPE;
 						relevance += R_TRUE_OR_FALSE;
 					}
 					
 					if(!this.requestor.isIgnored(CompletionProposal.KEYWORD)) {
 						if (knownKeywords.containsKey(choices[i])) {
 							continue;
 						}
 						knownKeywords.put(choices[i], this);
 						
 						CompletionProposal proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition, null);
 						proposal.setName(choices[i]);
 						proposal.setCompletion(choices[i]);
 						proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 						proposal.setRelevance(relevance);
 						this.requestor.accept(proposal);
 					}
 				}
 			}
 		}
 	}
 	
 	int computeBaseRelevance(){
 		int relevance = R_DEFAULT;
 		if (isWithScopeSymbol) {
 			relevance += R_WITH;
 		}
 		return relevance;
 	}
 	
 	int computeRelevanceForInterestingProposal(){
 		return R_INTERESTING;
 	}
 	
 	int computeRelevanceForCaseMatching(char[] token, char[] proposalName){
 		if (this.options.camelCaseMatch) {
 			if(CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
 				return R_CASE + R_EXACT_NAME;
 			} else if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
 				return R_CASE;
 			} else if (CharOperation.camelCaseMatch(token, proposalName)){
 				return R_CAMEL_CASE;
 			} else if(CharOperation.equals(token, proposalName, false /* ignore case */)) {
 				return R_EXACT_NAME;
 			}
 		} else if (CharOperation.prefixEquals(token, proposalName, true /* do not ignore case */)) {
 			if(CharOperation.equals(token, proposalName, true /* do not ignore case */)) {
 				return R_CASE + R_EXACT_NAME;
 			} else {
 				return R_CASE;
 			}
 		} else if(CharOperation.equals(token, proposalName, false /* ignore case */)) {
 			return R_EXACT_NAME;
 		}
 		return 0;
 	}
 
 	protected CompletionProposal createProposal(int kind, int completionOffset, ASTDmdNode node) {
 		CompletionProposal proposal = CompletionProposal.create(kind, completionOffset - this.offset);
 		proposal.node = node;
 		proposal.javaProject = javaProject;
 		proposal.nameLookup = this.nameEnvironment.nameLookup;
 		proposal.completionEngine = this;
 		proposal.wantArguments = this.wantArguments;
 		return proposal;
 	}
 
 	public void acceptCompilationUnit(char[] fullyQualifiedName) {
 		if (knownCompilationUnits.containsKey(fullyQualifiedName)
 				|| CharOperation.equals(fullyQualifiedName, this.sourceUnitFqn) ) {
 			return;
 		}
 		
 		// If we are completing a package, only show those packages for which
 		// there is an import
 		if (isCompletingPackage) {
 			if (!isImported(fullyQualifiedName)) {
 				return;
 			}
 		}
 		
 		knownCompilationUnits.put(fullyQualifiedName, this);
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForInterestingProposal();
 		if (currentName != null) {
 			relevance += computeRelevanceForCaseMatching(currentName, fullyQualifiedName);
 		}
 		relevance += R_QUALIFIED + R_COMPILATION_UNIT;
 		
 		CompletionProposal proposal = createProposal(CompletionProposal.COMPILATION_UNIT_REF, this.actualCompletionPosition, null);
 		proposal.setCompletion(fullyQualifiedName);
 		proposal.setRelevance(relevance);
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		this.requestor.accept(proposal);
 	}
 
 	public void acceptPackage(char[] packageName) {
 		
 	}
 
 	public void acceptType(char[] packageName, char[] typeName, char[] templateParametersSignature, char[][] enclosingTypeNames, long modifiers, int declarationStart, AccessRestriction accessRestriction) {
 		if (packageName == null || packageName.length == 0) {
 			return;
 		}
 		
 		if (isImported(packageName) || isIgnored(packageName)) {
 			return;
 		}
 		
 		if (!isVisible(modifiers, packageName)) {
 			return;
 		}
 		
 		char[] fullName = CharOperation.concat(packageName, typeName, '.');
 		if (knownDeclarations.containsKey(fullName)) {
 			return;
 		}
 		knownDeclarations.put(fullName, this);
 		
 		StringBuilder sig = new StringBuilder();
 		InternalSignature.appendPackageName(packageName, sig);
 		
 		char[] declSignature = sig.toString().toCharArray();
 		
 		if (templateParametersSignature.length > 0) {
 			sig.append(Signature.C_TEMPLATED_CLASS);
 		} else {
 			sig.append(Signature.C_CLASS);
 		}
 		sig.append(typeName.length);
 		sig.append(typeName);
 		if (templateParametersSignature != null) {
 			sig.append(templateParametersSignature);
 			sig.append(Signature.C_TEMPLATE_PARAMETERS_BREAK);
 		}
 		
 		char[] sigChar = sig.toString().toCharArray();
 		
 		CompletionProposal proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition, null);
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForCaseMatching(currentName, typeName);
 		relevance += computeRelevanceForExpectedType(sigChar);
 		if (parser.inNewExp) {
 			if ((modifiers & (Flags.AccClass)) != 0) {
 				// Don't suggest abstract classes
 				if ((modifiers & Flags.AccAbstract) != 0) {
 					return;
 				}
 				
 				relevance += R_NEW;
 			}
 		}
 		relevance += R_CLASS;
 		
 		proposal.setCompletion(fullName);
 		
 		proposal.setName(typeName);
 		
 		proposal.setSignature(sigChar);
 		proposal.setTypeSignature(sigChar);
 		proposal.setDeclarationSignature(declSignature);
 		proposal.setFlags(modifiers);
 		proposal.setRelevance(relevance);
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		
 		proposal.setPackageName(packageName);
 		proposal.declarationStart = declarationStart;
 		
 		CompletionEngine.this.requestor.accept(proposal);
 	}
 	
 	public void acceptField(char[] packageName, char[] name, char[] typeName, char[][] enclosingTypeNames, long modifiers, int declarationStart, AccessRestriction accessRestriction) {
 		if (!options.wantNonImportedVariables && (modifiers & Flags.AccAlias) == 0 && (modifiers & Flags.AccTypedef) == 0) {
 			return;
 		}
 		if (!options.wantNonImportedAliases && (modifiers & Flags.AccAlias) != 0) {
 			return;
 		}
 		if (!options.wantNonImportedTypedefs && (modifiers & Flags.AccTypedef) != 0) {
 			return;
 		}		
 		
 		if (packageName == null || packageName.length == 0) {
 			return;
 		}
 		
 		if (isImported(packageName) || isIgnored(packageName)) {
 			return;
 		}
 		
 		if (parser.inNewExp) {
 			return;
 		}
 		
 		// Skip variables if they are not imported
 //		if ((modifiers & (Flags.AccAlias | Flags.AccTypedef)) == 0 && !isImported(packageName)) {
 //			return;
 //		}
 		
 		// Don't show not imported variables if completing type identifier
 		if (isCompletingTypeIdentifier) {
 			if ((modifiers & Flags.AccTypedef) == 0 &&
 					(modifiers & Flags.AccAlias) == 0) {
 				return;
 			}
 		}
 		
 		if (!isVisible(modifiers, packageName)) {
 			return;
 		}
 		
 		char[] fullName = CharOperation.concat(packageName, name, '.');
 		if (knownDeclarations.containsKey(fullName)) {
 			return;
 		}
 		knownDeclarations.put(fullName, this);
 		
 		CompletionProposal proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition, null);
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForCaseMatching(currentName, name);
 		relevance += computeRelevanceForExpectedType(typeName);
 		relevance += R_VAR;
 		
 		proposal.setCompletion(fullName);
 		proposal.setName(name);
 		
 		StringBuilder sig = new StringBuilder();
 		InternalSignature.appendPackageName(packageName, sig);
 		
 		char[] declSignature = sig.toString().toCharArray();
 		
 		sig.append(Signature.C_VARIABLE);
 		sig.append(name.length);
 		sig.append(name);
 		
 		proposal.setSignature(sig.toString().toCharArray());
 		proposal.setTypeSignature(typeName);
 		proposal.setDeclarationSignature(declSignature);
 		proposal.setFlags(modifiers);
 		proposal.setRelevance(relevance);
 		proposal.setPackageName(packageName);
 		proposal.declarationStart = declarationStart;
 		
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		CompletionEngine.this.requestor.accept(proposal);
 	}
 	
 	public void acceptMethod(char[] packageName, char[] name, char[][] enclosingTypeNames, char[] signature, char[] templateParametersSignature, long modifiers, int declarationStart, AccessRestriction accessRestriction) {
 		if (!options.wantNonImportedFunctions) {
 			return;
 		}
 		
 		if (packageName == null || packageName.length == 0) {
 			return;
 		}
 		
 		if (isImported(packageName) || isIgnored(packageName)) {
 			return;
 		}
 		
 		// Don't show methods for type identifier completions
 		if (isCompletingTypeIdentifier) {
 			return;
 		}
 		
 		if (parser.inNewExp) {
 			return;
 		}
 		
 		if (specialFunctions.containsKey(name)) {
 			return;
 		}
 		
 		if (!isVisible(modifiers, packageName)) {
 			return;
 		}
 		
 		StringBuilder sig = new StringBuilder();
 		InternalSignature.appendPackageName(packageName, sig);
 		
 		char[] declSignature = sig.toString().toCharArray();
 		
 		if (templateParametersSignature.length > 0) {
 			sig.append(Signature.C_TEMPLATED_FUNCTION);
 		} else {
 			sig.append(Signature.C_FUNCTION);
 		}
 		sig.append(name.length);
 		sig.append(name);
 		sig.append(signature);
 		if (templateParametersSignature.length > 0) {
 			sig.append(templateParametersSignature);
 			sig.append(Signature.C_TEMPLATE_PARAMETERS_BREAK);
 		}
 		
 		char[] sigChars = sig.toString().toCharArray();
 		if (knownDeclarations.containsKey(sigChars)) {
 			return;
 		}
 		knownDeclarations.put(sigChars, this);
 		
 		char[] fullName = CharOperation.concat(packageName, name, '.');
 		
 		CompletionProposal proposal = this.createProposal(CompletionProposal.METHOD_REF, this.actualCompletionPosition, null);
 		
 		if (parser.isInAddrExp || parser.isCallFollowing) {
 			proposal.wantArguments = false;
 		}
 		
 		int relevance = computeBaseRelevance();
 		relevance += computeRelevanceForCaseMatching(currentName, name);
 		relevance += computeRelevanceForExpectedType(signature);
 		relevance += R_METHOD;
 		
 		handleMethodCompletion(proposal, fullName);
 		
 		proposal.setName(name);
 		proposal.setSignature(sigChars);
 		proposal.setTypeSignature(signature);
 		proposal.setDeclarationSignature(declSignature);
 		proposal.setFlags(modifiers);
 		proposal.setRelevance(relevance);
 		proposal.setReplaceRange(this.startPosition - this.offset, this.endPosition - this.offset);
 		
 		proposal.setPackageName(packageName);
 		proposal.declarationStart = declarationStart;
 		
 		CompletionEngine.this.requestor.accept(proposal);
 	}
 	
 	private int computeRelevanceForExpectedType(Type type) {
 		if (expectedTypeSignature == null || expectedType == null || type == null) {
 			return 0;
 		}
 		
 		// If it's an in expression, match associative arrays whose
 		// keys are the expected type
 		if (parser.isInExp) {
 			if (type instanceof TypeAArray) {
 				TypeAArray ta = (TypeAArray) type;
 				if (ta.index != null) {
 					// Need to see if the type we have (expectedType) can be converted
 					// to the key, so it's like inverting roles here
 					parser.isInExp = false;					
 					Type expectedTypeSave = expectedType;
 					expectedType = ta.index;
 					expectedTypeSignature = expectedType.getSignature().toCharArray();
 					
 					int relevance = computeRelevanceForExpectedType(expectedTypeSave);
 					
 					expectedType = expectedTypeSave;
 					expectedTypeSignature = expectedType.getSignature().toCharArray();
 					parser.isInExp = true;
 					return relevance;
 				}
 			}
 			return 0;
 		}
 		
 		if (type instanceof TypeFunction && type.next != null) {
 			// If the expected type is a function pointer, and the type to suggest
 			// is a function, suggest it, if it matches
 			if (expectedType instanceof TypePointer && expectedType.next instanceof TypeFunction) {
 				Type expectedTypeSave = expectedType;
 				expectedType = expectedType.next;
 				expectedTypeSignature = expectedType.getSignature().toCharArray();
 				
 				int relevance = computeRelevanceForExpectedType(type);
 				
 				expectedType = expectedTypeSave;
 				expectedTypeSignature = expectedType.getSignature().toCharArray();				
 				return relevance;
 			} else if (expectedType instanceof TypeFunction) {
 				if (type.covariant(expectedType, semanticContext) == 1) {
 					return R_EXPECTED_TYPE;
 				}
 			} else if (expectedType instanceof TypeDelegate) {
 				if (type.covariant(expectedType.next, semanticContext) == 1) {
 					return R_EXPECTED_TYPE;
 				}
 			}
 			
 			return computeRelevanceForExpectedType(type.next);
 		}
 		
 		MATCH match = type.implicitConvTo(expectedType, semanticContext);
 		if (match == MATCH.MATCHexact) {
 			return R_EXACT_EXPECTED_TYPE;
 		}
 		if (match == MATCH.MATCHconvert) {
 			return R_EXPECTED_TYPE;
 		}
 		
 		if (expectedType.isBaseOf(type, null, semanticContext)) {
 			return R_EXPECTED_TYPE;
 		}
 		
 		return computeRelevanceForExpectedType(type.getSignature().toCharArray());
 	}
 	
 	private int computeRelevanceForExpectedType(char[] signature) {
 		if (expectedTypeSignature == null || signature == null) {
 			return 0;
 		}
 		
 		boolean isFunctionSignature = isFunctionSignature(signature);
 		
 		if (CharOperation.equals(signature, expectedTypeSignature) ||
 				(expectedTypeSignature.length > 0 && expectedTypeSignature[0] == Signature.C_DELEGATE
 						&& isFunctionSignature
 						&& CharOperation.equals(signature, expectedTypeSignature, 1, expectedTypeSignature.length))) {
 			return R_EXACT_EXPECTED_TYPE;
 		}
 		
 		if (isFunctionSignature) {
 			return computeRelevanceForExpectedType(Signature.getReturnType(signature));
 		}
 		
 		// It may be that the signature is unresolved, so... use
 		// an heuristic here
 		int i = CharOperation.indexOf('?', signature);
 		if (i != -1) {
 			i++;
 			int start = i;
 			char c = signature[start];
 			
 			// Read number to see what's the type name
 			int n = 0;
 			while(Character.isDigit(c)) {
 				n = 10 * n + (c - '0');
 				i++;
 				if (i >= signature.length) {
 					return 0;
 				}
 				c = signature[i];
 			}
 			i += n;
 			
 			char[] name = CharOperation.subarray(signature, start + 1, i);
 			if (CharOperation.indexOf(name, expectedTypeSignature, true, 0) != -1) {
 				return R_EXPECTED_TYPE;
 			}
 		}
 		
 		return 0;
 	}
 
 	private boolean isFunctionSignature(char[] signature) {
 		return signature.length > 0 && (signature[0] == Signature.C_D_LINKAGE || signature[0] == Signature.C_C_LINKAGE ||
 				signature[0] == Signature.C_CPP_LINKAGE || signature[0] == Signature.C_PASCAL_LINKAGE ||
 				signature[0] == Signature.C_WINDOWS_LINKAGE);
 	}
 
 	private boolean isImported(char[] fullyQualifiedName) {
 		// This module is "imported"
 		if (CharOperation.equals(fullyQualifiedName, this.sourceUnitFqn)) {
 			return true;
 		}
 		
 		if (module.aimports != null) {
 			for(Object sym : module.aimports) {
 				if (sym instanceof Module) {
 					Module mod = (Module) sym;
 					char[] fqn = mod.getFullyQualifiedName().toCharArray();
 					if (CharOperation.equals(fqn, fullyQualifiedName)) {
 						return true;
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private boolean isIgnored(char[] fullyQualifiedName) {
 		if (options.ignoredNonImportedModules.size() == 0) {
 			return false;
 		}
 		
 		for(char[] key : options.ignoredNonImportedModules.keys()) {
 			if (key != null) {
 				if (CharOperation.match(key, fullyQualifiedName, false)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 }
