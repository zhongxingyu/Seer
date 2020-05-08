 // ***************************************************************************
 // Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // *  Redistributions of source code must retain the above copyright notice,
 //    this list of conditions and the following disclaimer.
 // *  Redistributions in binary form must reproduce the above copyright
 //    notice, this list of conditions and the following disclaimer in the
 //    documentation and/or other materials provided with the distribution.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 // TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 // PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 // CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 // EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 // PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 // OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 // OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 // ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 // **************************************************************************
 
 //ifdef JAVA
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 //endif VAJA
 
 //ifdef JAVA
 interface GtConst {
 //endif VAJA
 	// Version
 	public final static String  ProgName  = "GreenTeaScript";
 	public final static String  CodeName  = "Reference Implementation of D-Script";
 	public final static int     MajorVersion = 0;
 	public final static int     MinerVersion = 1;
 	public final static int     PatchLevel   = 0;
 	public final static String  Version = "0.1";
 	public final static String  Copyright = "Copyright (c) 2013, JST/CREST DEOS project authors";
 	public final static String  License = "BSD-Style Open Source";
 
 	// ClassFlag
 	public final static int		NativeClass	     				= 1 << 0;
 	public final static int		InterfaceClass				   	= 1 << 1;
 	public final static int		DynamicClass				    = 1 << 2;
 	public final static int     EnumClass                       = 1 << 3;
 	public final static int     OpenClass                       = 1 << 4;
 
 	// FuncFlag
 	public final static int     PublicFunc          = 1 << 0;
 	public final static int		ExportFunc		    = 1 << 1;
 	public final static int		VirtualFunc		    = 1 << 2;
 	public final static int		NativeFunc		    = 1 << 3;
 	public final static int		NativeStaticFunc	= 1 << 4;
 	public final static int		NativeMacroFunc	    = 1 << 5;
 	public final static int		NativeVariadicFunc	= 1 << 6;
 	public final static int		DynamicFunc		    = 1 << 7;
 	public final static int		ConstFunc			= 1 << 8;
 	public final static int     ConstructorFunc     = 1 << 9;
 	public final static int     GetterFunc          = 1 << 10;
 	public final static int     SetterFunc          = 1 << 11;
 
 	// VarFlag
 	public final static int  ReadOnlyVar = 1;              // @ReadOnly x = 1; disallow x = 2
 	//public final static int  MutableFieldVar  = (1 << 1);  // @Mutable x; x.y = 1 is allowed
 
 	public final static int		NoMatch							= -1;
 	public final static boolean Optional = true;
 	public final static boolean Required = false;
 
 	public final static int		ErrorLevel						= 0;
 	public final static int     TypeErrorLevel                  = 1;
 	public final static int		WarningLevel					= 2;
 	public final static int		InfoLevel					    = 3;
 
 	public final static int	NullChar				= 0;
 	public final static int	UndefinedChar			= 1;
 	public final static int	DigitChar				= 2;
 	public final static int	UpperAlphaChar			= 3;
 	public final static int	LowerAlphaChar			= 4;
 	public final static int	UnderBarChar			= 5;
 	public final static int	NewLineChar				= 6;
 	public final static int	TabChar					= 7;
 	public final static int	SpaceChar				= 8;
 	public final static int	OpenParChar				= 9;
 	public final static int	CloseParChar			= 10;
 	public final static int	OpenBracketChar			= 11;
 	public final static int	CloseBracketChar		= 12;
 	public final static int	OpenBraceChar			= 13;
 	public final static int	CloseBraceChar			= 14;
 	public final static int	LessThanChar			= 15;
 	public final static int	GreaterThanChar			= 16;
 	public final static int	QuoteChar				= 17;
 	public final static int	DoubleQuoteChar			= 18;
 	public final static int	BackQuoteChar			= 19;
 	public final static int	SurprisedChar			= 20;
 	public final static int	SharpChar				= 21;
 	public final static int	DollarChar				= 22;
 	public final static int	PercentChar				= 23;
 	public final static int	AndChar					= 24;
 	public final static int	StarChar				= 25;
 	public final static int	PlusChar				= 26;
 	public final static int	CommaChar				= 27;
 	public final static int	MinusChar				= 28;
 	public final static int	DotChar					= 29;
 	public final static int	SlashChar				= 30;
 	public final static int	ColonChar				= 31;
 	public final static int	SemiColonChar			= 32;
 	public final static int	EqualChar				= 33;
 	public final static int	QuestionChar			= 34;
 	public final static int	AtmarkChar				= 35;
 	public final static int	VarChar					= 36;
 	public final static int	ChilderChar				= 37;
 	public final static int	BackSlashChar			= 38;
 	public final static int	HatChar					= 39;
 	public final static int	UnicodeChar				= 40;
 	public final static int MaxSizeOfChars          = 41;
 
 	public final static int[]	CharMatrix = /*BeginArray*/{
 		/*nul soh stx etx eot enq ack bel*/
 		0, 1, 1, 1, 1, 1, 1, 1,
 		/*bs ht nl vt np cr so si  */
 		1, TabChar, NewLineChar, 1, 1, NewLineChar, 1, 1,
 		/*020 dle  021 dc1  022 dc2  023 dc3  024 dc4  025 nak  026 syn  027 etb */
 		1, 1, 1, 1, 1, 1, 1, 1,
 		/*030 can  031 em   032 sub  033 esc  034 fs   035 gs   036 rs   037 us */
 		1, 1, 1, 1, 1, 1, 1, 1,
 		/*040 sp   041  !   042  "   043  #   044  $   045  %   046  &   047  ' */
 		SpaceChar, SurprisedChar, DoubleQuoteChar, SharpChar, DollarChar, PercentChar, AndChar, QuoteChar,
 		/*050  (   051  )   052  *   053  +   054  ,   055  -   056  .   057  / */
 		OpenParChar, CloseParChar, StarChar, PlusChar, CommaChar, MinusChar, DotChar, SlashChar,
 		/*060  0   061  1   062  2   063  3   064  4   065  5   066  6   067  7 */
 		DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar, DigitChar,
 		/*070  8   071  9   072  :   073  ;   074  <   075  =   076  >   077  ? */
 		DigitChar, DigitChar, ColonChar, SemiColonChar, LessThanChar, EqualChar, GreaterThanChar, QuestionChar,
 		/*100  @   101  A   102  B   103  C   104  D   105  E   106  F   107  G */
 		AtmarkChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*110  H   111  I   112  J   113  K   114  L   115  M   116  N   117  O */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*120  P   121  Q   122  R   123  S   124  T   125  U   126  V   127  W */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, UpperAlphaChar,
 		/*130  X   131  Y   132  Z   133  [   134  \   135  ]   136  ^   137  _ */
 		UpperAlphaChar, UpperAlphaChar, UpperAlphaChar, OpenBracketChar, BackSlashChar, CloseBracketChar, HatChar, UnderBarChar,
 		/*140  `   141  a   142  b   143  c   144  d   145  e   146  f   147  g */
 		BackQuoteChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*150  h   151  i   152  j   153  k   154  l   155  m   156  n   157  o */
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*160  p   161  q   162  r   163  s   164  t   165  u   166  v   167  w */
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, LowerAlphaChar,
 		/*170  x   171  y   172  z   173  {   174  |   175  }   176  ~   177 del*/
 		LowerAlphaChar, LowerAlphaChar, LowerAlphaChar, OpenBraceChar, VarChar, CloseBraceChar, ChilderChar, 1,
 		/*EndArray*/};
 
 	// TokenFlag
 	public final static int	SourceTokenFlag	    = 1;
 	public final static int	ErrorTokenFlag	    = (1 << 1);
 	public final static int IndentTokenFlag	    = (1 << 2);
 	public final static int	WhiteSpaceTokenFlag	= (1 << 3);
 	public final static int DelimTokenFlag	    = (1 << 4);
 	public final static int QuotedTokenFlag	    = (1 << 5);
 	public final static int NameSymbolTokenFlag	    = (1 << 6);
 
 	// ParseFlag
 	public final static int	BackTrackParseFlag	= 1;
 	public final static int	SkipIndentParseFlag	= (1 << 1);
 
 	// SyntaxTree
 	public final static int NoWhere         = -1;
 	// UnaryTree, SuffixTree
 	public final static int UnaryTerm      = 0;
 	// BinaryTree
 	public final static int	LeftHandTerm	= 0;
 	public final static int	RightHandTerm	= 1;
 
 	// IfStmt
 	public final static int	IfCond	= 0;
 	public final static int	IfThen	= 1;
 	public final static int	IfElse	= 2;
 
 	// while(cond) {...}
 	static final int WhileCond = 0;
 	static final int WhileBody = 1;
 
 	// ReturnStmt
 	public final static int	ReturnExpr	= 0;
 
 	// var N = 1;
 	public final static int	VarDeclType		= 0;
 	public final static int	VarDeclName		= 1;
 	public final static int	VarDeclValue	= 2;
 	public final static int	VarDeclScope	= 3;
 
 	//Func Call;
 	public static final int	CallExpressionOffset	= 0;
 	public static final int	CallParameterOffset		= 1;
 
 	// Const Decl;
 	public final static int	ConstDeclClassIndex	= 0;
 	public final static int	ConstDeclNameIndex	= 1;
 	public final static int	ConstDeclValueIndex	= 2;
 
 	// Func Decl;
 	public final static int	FuncDeclReturnType	= 0;
 	public final static int	FuncDeclClass		= 1;
 	public final static int	FuncDeclName		= 2;
 	public final static int	FuncDeclBlock		= 3;
 	public final static int	FuncDeclParam		= 4;
 
 	// Class Decl;
 	public final static int	ClassDeclSuperType	= 0;
 	public final static int	ClassDeclName			= 1;
 	public final static int	ClassDeclFieldStartIndex    = 2;
 
 	// try-catch
 	public final static int TryBody         = 0;
 	public final static int CatchVariable   = 1;
 	public final static int CatchBody       = 2;
 	public final static int FinallyBody     = 3;
 
 	// switch-case
 	static final int SwitchCaseCondExpr = 0;
 	static final int SwitchCaseDefaultBlock = 1;
 	static final int SwitchCaseCaseIndex = 2;
 
 	// Enum
 	public final static int EnumNameTreeIndex = 0;
 
 	public final static int BinaryOperator					= 1;
 	public final static int LeftJoin						= 1 << 1;
 	public final static int PrecedenceShift					= 3;
 	//	Precedence_CppMember      = 300;  /* .x ->x */
 	public final static int Precedence_CStyleMUL			= (400 << PrecedenceShift);				/* x * x; x / x; x % x*/
 	public final static int Precedence_CStyleADD			= (500 << PrecedenceShift);				/* x + x; x - x */
 	public final static int Precedence_CStyleSHIFT			= (600 << PrecedenceShift);				/* x << x; x >> x */
 	public final static int Precedence_CStyleCOMPARE		= (700 << PrecedenceShift);
 	public final static int Precedence_CStyleEquals			= (800 << PrecedenceShift);
 	public final static int Precedence_CStyleBITAND			= (900 << PrecedenceShift);
 	public final static int Precedence_CStyleBITXOR			= (1000 << PrecedenceShift);
 	public final static int Precedence_CStyleBITOR			= (1100 << PrecedenceShift);
 	public final static int Precedence_CStyleAND			= (1200 << PrecedenceShift);
 	public final static int Precedence_CStyleOR				= (1300 << PrecedenceShift);
 	public final static int Precedence_CStyleTRINARY		= (1400 << PrecedenceShift);				/* ? : */
 	public final static int Precedence_CStyleAssign			= (1500 << PrecedenceShift);
 	public final static int Precedence_CStyleCOMMA			= (1600 << PrecedenceShift);
 
 	public final static int DefaultTypeCheckPolicy			= 0;
 	public final static int NoCheckPolicy                   = 1;
 	public final static int CastPolicy                      = (1 << 1);
 	public final static int IgnoreEmptyPolicy               = (1 << 2);
 	public final static int AllowEmptyPolicy                = (1 << 3);
 	public final static int AllowVoidPolicy                 = (1 << 4);
 	public final static int AllowCoercionPolicy             = (1 << 5);
 	public final static int OnlyConstPolicy                 = (1 << 6);
 
 	public final static Object UndefinedSymbol = new Object();   // any class 
 	public final static String NativeNameSuffix = "__";
 	public final static String[] ShellGrammarReservedKeywords = {"true", "false", "as", "if"};
 
 	public final static boolean UseLangStat = true;
 	
 	public final static int VerboseSymbol    = 1;
 	public final static int VerboseType      = (1 << 1);
 	public final static int VerboseFunc      = (1 << 2);
 	public final static int VerboseEval      = (1 << 3);
 	public final static int VerboseToken     = (1 << 4);
 	public final static int VerboseUndefined   = (1 << 5);
 	public final static int VerboseNative    = (1 << 6);
 	public final static int VerboseFile      = (1 << 7);
 	
 //ifdef JAVA
 }
 
 class GtStatic implements GtConst {
 //endif VAJA
 
 	public final static String JoinStrings(String Unit, int Times) {
 		/*local*/String s = "";
 		/*local*/int i = 0;
 		while(i < Times) {
 			s = s + Unit;
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static int ListSize(ArrayList<?> a) {
 		if(a == null) {
 			return 0;
 		}
 		return a.size();
 	}
 
 	public final static boolean IsFlag(int flag, int flag2) {
 		return ((flag & flag2) == flag2);
 	}
 
 	public final static int AsciiToTokenMatrixIndex(char c) {
 		if(c < 128) {
 			return CharMatrix[c];
 		}
 		return UnicodeChar;
 	}
 
 	private final static String n2s(int n) {
 		if(n < (27)) {
 			return LibGreenTea.CharToString((/*cast*/char)(65 + (n - 0)));
 		}
 		else if(n < (27 + 10)) {
 			return LibGreenTea.CharToString((/*cast*/char)(48 + (n - 27)));
 		}
 		else {
 			return LibGreenTea.CharToString((/*cast*/char)(97 + (n - 37)));
 		}
 	}
 
 	public final static String NumberToAscii(int number) {
 		if(number >= 3600) {
 			return n2s(number / 3600) + NumberToAscii(number % 3600);
 		}
 		return n2s((number / 60)) + n2s((number % 60));
 	}
 
 	public final static String NativeVariableName(String Name, int Index) {
 		return Name + NativeNameSuffix + NumberToAscii(Index);
 	}
 
 	public final static String ClassSymbol(GtType ClassType, String Symbol) {
 		return ClassType.GetUniqueName() + "." + Symbol;
 	}
 
 	public final static String MangleGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = BaseType.ShortClassName + NativeNameSuffix;
 		/*local*/int i = BaseIdx;
 		while(i < ListSize(TypeList)) {
 			s = s + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		return s;
 	}
 
 	public final static String MangleFuncName(GtType BaseType, String FuncName, int BaseIdx, ArrayList<GtType> TypeList) {
 		/*local*/String s = FuncName + NativeNameSuffix + NumberToAscii(BaseType.ClassId);
 		/*local*/int i = BaseIdx;
 		while(i < ListSize(TypeList)) {
 			s = s + NumberToAscii(TypeList.get(i).ClassId);
 			i = i + 1;
 		}
 		return s;
 	}
 
 	private final static GtPolyFunc JoinPolyFuncFunc(GtType ClassType, GtPolyFunc PolyFunc, GtFunc Func) {
 		if(ClassType == null || Func.GetRecvType() == ClassType) {
 			if(PolyFunc == null) {
 				PolyFunc = new GtPolyFunc(null, Func);
 			}
 			else {
 				PolyFunc.FuncList.add(Func);
 			}
 		}
 		return PolyFunc;
 	}
 
 	public final static GtPolyFunc JoinPolyFunc(GtType ClassType, GtPolyFunc PolyFunc, Object FuncValue) {
 		if(FuncValue instanceof GtFunc) {
 			return JoinPolyFuncFunc(ClassType, PolyFunc, (/*cast*/GtFunc)FuncValue);
 		}
 		if(FuncValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc Poly = (/*cast*/GtPolyFunc)FuncValue;
 			/*local*/int i = 0;
 			while(i < Poly.FuncList.size()) {
 				PolyFunc = JoinPolyFuncFunc(ClassType, PolyFunc, Poly.FuncList.get(i));
 				i += 1;
 			}
 		}
 		return PolyFunc;
 	}
 
 //ifdef JAVA
 	public final static GtDelegateToken FunctionA(Object Callee, String FuncName) {
 		return new GtDelegateToken(Callee, LibGreenTea.LookupNativeMethod(Callee, FuncName));
 	}
 
 	public final static GtDelegateMatch FunctionB(Object Callee, String FuncName) {
 		return new GtDelegateMatch(Callee, LibGreenTea.LookupNativeMethod(Callee, FuncName));
 	}
 
 	public final static GtDelegateType FunctionC(Object Callee, String FuncName) {
 		return new GtDelegateType(Callee, LibGreenTea.LookupNativeMethod(Callee, FuncName));
 	}
 //endif VAJA
 
 	public final static int ApplyTokenFunc(TokenFunc TokenFunc, GtTokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/GtDelegateToken delegate = TokenFunc.Func;
 			/*local*/int NextIdx = LibGreenTea.ApplyTokenFunc(delegate, TokenContext, ScriptSource, Pos);
 			if(NextIdx > Pos) return NextIdx;
 			TokenFunc = TokenFunc.ParentFunc;
 		}
 		return NoMatch;
 	}
 
 	public final static GtSyntaxPattern MergeSyntaxPattern(GtSyntaxPattern Pattern, GtSyntaxPattern Parent) {
 		if(Parent == null) return Pattern;
 		/*local*/GtSyntaxPattern MergedPattern = new GtSyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc, Pattern.TypeFunc);
 		MergedPattern.ParentPattern = Parent;
 		return MergedPattern;
 	}
 
 	public final static boolean IsEmptyOrError(GtSyntaxTree Tree) {
 		return Tree == null || Tree.IsEmptyOrError();
 	}
 
 	public final static GtSyntaxTree LinkTree(GtSyntaxTree LastNode, GtSyntaxTree Node) {
 		Node.PrevTree = LastNode;
 		if(LastNode != null) {
 			LastNode.NextTree = Node;
 		}
 		return Node;
 	}
 
 	public final static GtSyntaxTree TreeHead(GtSyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.PrevTree != null) {
 				Tree = Tree.PrevTree;
 			}
 		}
 		return Tree;
 	}
 
 	public final static GtSyntaxTree ApplySyntaxPattern(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int Pos = TokenContext.CurrentPosition;
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		/*local*/GtSyntaxPattern CurrentPattern = Pattern;
 		while(CurrentPattern != null) {
 			/*local*/GtDelegateMatch delegate = CurrentPattern.MatchFunc;
 			TokenContext.CurrentPosition = Pos;
 			if(CurrentPattern.ParentPattern != null) {   // This means it has next patterns
 				TokenContext.ParseFlag = ParseFlag | BackTrackParseFlag;
 			}
 			//LibGreenTea.DebugP("B :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + ", next=" + CurrentPattern.ParentPattern);
 			TokenContext.IndentLevel += 1;
 			/*local*/GtSyntaxTree ParsedTree = (/*cast*/GtSyntaxTree)LibGreenTea.ApplyMatchFunc(delegate, NameSpace, TokenContext, LeftTree, CurrentPattern);
 			TokenContext.IndentLevel -= 1;
 			if(ParsedTree != null && ParsedTree.IsEmpty()) {
 				ParsedTree = null;
 			}
 			//LibGreenTea.DebugP("E :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + " => " + ParsedTree);
 			TokenContext.ParseFlag = ParseFlag;
 			if(ParsedTree != null) {
 				return ParsedTree;
 			}
 			CurrentPattern = CurrentPattern.ParentPattern;
 		}
 		if(TokenContext.IsAllowedBackTrack()) {
 			TokenContext.CurrentPosition = Pos;
 		}
 		if(Pattern == null) {
 			LibGreenTea.VerboseLog(VerboseUndefined, "undefined syntax pattern: " + Pattern);
 		}
 		return TokenContext.ReportExpectedPattern(Pattern);
 	}
 
 	public final static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext) {
 		/*local*/GtSyntaxPattern Pattern = TokenContext.GetFirstPattern();
 		/*local*/GtSyntaxTree LeftTree = GtStatic.ApplySyntaxPattern(NameSpace, TokenContext, null, Pattern);
 		while(!GtStatic.IsEmptyOrError(LeftTree) && !TokenContext.MatchToken(";")) {
 			/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern();
 			if(ExtendedPattern == null) {
 				//LibGreenTea.DebugP("In $Expression$ ending: " + TokenContext.GetToken());
 				break;
 			}
 			LeftTree = GtStatic.ApplySyntaxPattern(NameSpace, TokenContext, LeftTree, ExtendedPattern);
 		}
 		return LeftTree;
 	}
 
 	// typing
 	public final static GtNode ApplyTypeFunc(GtDelegateType delegate, GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		LibGreenTea.Assert(delegate != null);
 		Gamma.NameSpace = ParsedTree.NameSpace;
 		return (/*cast*/GtNode)LibGreenTea.ApplyTypeFunc(delegate, Gamma, ParsedTree, Type);
 	}
 
 	public final static GtNode LinkNode(GtNode LastNode, GtNode Node) {
 		Node.PrevNode = LastNode;
 		if(LastNode != null) {
 			LastNode.NextNode = Node;
 		}
 		return Node;
 	}
 //ifdef JAVA
 }
 
 final class GtMap {
 	final HashMap<String, Object>	Map;
 
 	public GtMap() {
 		this.Map = new HashMap<String, Object>();
 	}
 
 	public final void put(String Key, Object Value) {
 		this.Map.put(Key, Value);
 	}
 
 	public final Object get(String Key) {
 		return this.Map.get(Key);
 	}
 
 	public final String[] keys() {
 		return LibGreenTea.MapGetKeys(this);
 	}
 }
 
 class GtDelegateCommon {
 	/*field*/public Object	Self;
 	/*field*/public Method	Func;
 	GtDelegateCommon(Object Self, Method method) {
 		this.Self = Self;
 		this.Func = method;
 	}
 	@Override public final String toString() {
 		if(this.Func == null) {
 			return "*undefined*";
 		}
 		return this.Func.getName();
 	}
 }
 
 final class GtDelegateToken extends GtDelegateCommon {
 	GtDelegateToken/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 final class GtDelegateMatch extends GtDelegateCommon {
 	GtDelegateMatch/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 final class GtDelegateType extends GtDelegateCommon {
 	GtDelegateType/*constructor*/(Object Self, Method method) {
 		super(Self, method);
 	}
 }
 
 //endif VAJA
 
 // tokenizer
 
 final class GtToken extends GtStatic {
 	/*field*/public int		        TokenFlag;
 	/*field*/public String	        ParsedText;
 	/*field*/public long		    FileLine;
 	/*field*/public GtSyntaxPattern	PresetPattern;
 
 	GtToken/*constructor*/(String text, long FileLine) {
 		this.TokenFlag = 0;
 		this.ParsedText = text;
 		this.FileLine = FileLine;
 		this.PresetPattern = null;
 	}
 
 	public boolean IsSource() {
 		return IsFlag(this.TokenFlag, SourceTokenFlag);
 	}
 
 	public boolean IsError() {
 		return IsFlag(this.TokenFlag, ErrorTokenFlag);
 	}
 
 	public boolean IsIndent() {
 		return IsFlag(this.TokenFlag, IndentTokenFlag);
 	}
 
 	public boolean IsDelim() {
 		return IsFlag(this.TokenFlag, DelimTokenFlag);
 	}
 
 	public final boolean IsNextWhiteSpace() {
 		return IsFlag(this.TokenFlag, WhiteSpaceTokenFlag);
 	}
 	
 	public boolean IsQuoted() {
 		return IsFlag(this.TokenFlag, QuotedTokenFlag);
 	}
 
 	public boolean IsNameSymbol() {
 		return IsFlag(this.TokenFlag, NameSymbolTokenFlag);
 	}
 
 	public boolean EqualsText(String text) {
 		return this.ParsedText.equals(text);
 	}
 
 	@Override public String toString() {
 		/*local*/String TokenText = "";
 		if(this.PresetPattern != null) {
 			TokenText = "(" + this.PresetPattern.PatternName + ") ";
 		}
 		return TokenText + this.ParsedText;
 	}
 
 	public String ToErrorToken(String Message) {
 		this.TokenFlag = ErrorTokenFlag;
 		this.ParsedText = Message;
 		return Message;
 	}
 
 	public String GetErrorMessage() {
 		LibGreenTea.Assert(this.IsError());
 		return this.ParsedText;
 	}
 }
 
 final class TokenFunc {
 	/*field*/public GtDelegateToken       Func;
 	/*field*/public TokenFunc	ParentFunc;
 
 	TokenFunc/*constructor*/(GtDelegateToken Func, TokenFunc Parent) {
 		this.Func = Func;
 		this.ParentFunc = Parent;
 	}
 
 	@Override public String toString() {
 		return this.Func.Func.toString();
 	}
 }
 
 final class GtTokenContext extends GtStatic {
 	/*field*/public final static GtToken NullToken = new GtToken("", 0);
 
 	/*field*/public GtNameSpace TopLevelNameSpace;
 	/*field*/public ArrayList<GtToken> SourceList;
 	/*field*/public int CurrentPosition;
 	/*field*/public long ParsingLine;
 	/*field*/public int ParseFlag;
 	/*field*/public int IndentLevel = 0;
 
 	GtTokenContext/*constructor*/(GtNameSpace NameSpace, String Text, long FileLine) {
 		this.TopLevelNameSpace = NameSpace;
 		this.SourceList = new ArrayList<GtToken>();
 		this.CurrentPosition = 0;
 		this.ParsingLine = FileLine;
 		this.ParseFlag = 0;
 		this.AddNewToken(Text, SourceTokenFlag, null);
 		this.IndentLevel = 0;
 	}
 
 	public GtToken AddNewToken(String Text, int TokenFlag, String PatternName) {
 		/*local*/GtToken Token = new GtToken(Text, this.ParsingLine);
 		Token.TokenFlag |= TokenFlag;
 		if(PatternName != null) {
 			Token.PresetPattern = this.TopLevelNameSpace.GetPattern(PatternName);
 			LibGreenTea.Assert(Token.PresetPattern != null);
 		}
 		this.SourceList.add(Token);
 		return Token;
 	}
 
 	public void FoundWhiteSpace() {
 		/*local*/GtToken Token = this.GetToken();
 		Token.TokenFlag |= WhiteSpaceTokenFlag;
 	}
 
 	public void FoundLineFeed(long line) {
 		this.ParsingLine += line;
 	}
 
 	public void ReportTokenError(int Level, String Message, String TokenText) {
 		/*local*/GtToken Token = this.AddNewToken(TokenText, 0, "$Error$");
 		this.TopLevelNameSpace.Context.ReportError(Level, Token, Message);
 	}
 
 	public GtSyntaxTree NewErrorSyntaxTree(GtToken Token, String Message) {
 		if(!this.IsAllowedBackTrack()) {
 			this.TopLevelNameSpace.Context.ReportError(ErrorLevel, Token, Message);
 			/*local*/GtSyntaxTree ErrorTree = new GtSyntaxTree(Token.PresetPattern, this.TopLevelNameSpace, Token, null);
 			return ErrorTree;
 		}
 		return null;
 	}
 
 	public GtToken GetBeforeToken() {
 		/*local*/int pos = this.CurrentPosition - 1;
 		while(pos >= 0 && pos < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(pos);
 			if(IsFlag(Token.TokenFlag, IndentTokenFlag)) {
 				pos -= 1;
 				continue;
 			}
 			return Token;
 		}
 		return null;
 	}
 
 	public GtSyntaxTree ReportExpectedToken(String TokenText) {
 		if(!this.IsAllowedBackTrack()) {
 			/*local*/GtToken Token = this.GetBeforeToken();
 			if(Token != null) {
 				return this.NewErrorSyntaxTree(Token, TokenText + " is expected after " + Token.ParsedText);
 			}
 			Token = this.GetToken();
 			LibGreenTea.Assert(Token != GtTokenContext.NullToken);
 			return this.NewErrorSyntaxTree(Token, TokenText + " is expected at " + Token.ParsedText);
 		}
 		return null;
 	}
 
 	public GtSyntaxTree ReportExpectedPattern(GtSyntaxPattern Pattern) {
 		if(Pattern == null) {
 			return this.ReportExpectedToken("null");
 		}
 		return this.ReportExpectedToken(Pattern.PatternName);
 	}
 
 	public void Vacume() {
 		if(this.CurrentPosition > 0) {
 			/*local*/ArrayList<GtToken> NewList = new ArrayList<GtToken>();
 			/*local*/int i = this.CurrentPosition;
 			while(i < ListSize(this.SourceList)) {
 				NewList.add(this.SourceList.get(i));
 				i = i + 1;
 			}
 			this.SourceList = NewList;
 			this.CurrentPosition = 0;
 		}
 	}
 
 	private int DispatchFunc(String ScriptSource, int GtChar, int pos) {
 		/*local*/TokenFunc TokenFunc = this.TopLevelNameSpace.GetTokenFunc(GtChar);
 		/*local*/int NextIdx = GtStatic.ApplyTokenFunc(TokenFunc, this, ScriptSource, pos);
 		if(NextIdx == NoMatch) {
 			LibGreenTea.VerboseLog(VerboseUndefined, "undefined tokenizer: " + LibGreenTea.CharAt(ScriptSource, pos));
 			this.AddNewToken(ScriptSource.substring(pos, pos + 1), 0, null);
 			return pos + 1;
 		}
 		return NextIdx;
 	}
 
 	private void Tokenize(String ScriptSource, long CurrentLine) {
 		/*local*/int currentPos = 0;
 		/*local*/int len = ScriptSource.length();
 		this.ParsingLine = CurrentLine;
 		while(currentPos < len) {
 			/*local*/int gtCode = AsciiToTokenMatrixIndex(LibGreenTea.CharAt(ScriptSource, currentPos));
 			/*local*/int nextPos = this.DispatchFunc(ScriptSource, gtCode, currentPos);
 			if(currentPos >= nextPos) {
 				break;
 			}
 			currentPos = nextPos;
 		}
 		this.Dump();
 	}
 
 	public GtToken GetToken() {
 		while(this.CurrentPosition < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(this.CurrentPosition);
 			if(Token.IsSource()) {
 				this.SourceList.remove(this.SourceList.size()-1);
 				this.Tokenize(Token.ParsedText, Token.FileLine);
 				if(this.CurrentPosition < this.SourceList.size()) {
 					Token = this.SourceList.get(this.CurrentPosition);
 				}else{
 					break;
 				}
 			}
 			if(IsFlag(this.ParseFlag, SkipIndentParseFlag) && Token.IsIndent()) {
 				this.CurrentPosition = this.CurrentPosition + 1;
 				continue;
 			}
 			return Token;
 		}
 		return GtTokenContext.NullToken;
 	}
 
 	public boolean HasNext() {
 		return (this.GetToken() != GtTokenContext.NullToken);
 	}
 
 	public GtToken Next() {
 		/*local*/GtToken Token = this.GetToken();
 		this.CurrentPosition += 1;
 		return Token;
 	}
 
 	public void SkipIndent() {
 		/*local*/GtToken Token = this.GetToken();
 		while(Token.IsIndent()) {
 			this.CurrentPosition = this.CurrentPosition + 1;
 			Token = this.GetToken();
 		}
 	}
 
 	public GtSyntaxPattern GetPattern(String PatternName) {
 		return this.TopLevelNameSpace.GetPattern(PatternName);
 	}
 
 	public GtSyntaxPattern GetFirstPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.PresetPattern != null) {
 			return Token.PresetPattern;
 		}
 		/*local*/GtSyntaxPattern Pattern = this.TopLevelNameSpace.GetPattern(Token.ParsedText);
 		if(Pattern == null) {
 			return this.TopLevelNameSpace.GetPattern("$Symbol$");
 		}
 		return Pattern;
 	}
 
 	public GtSyntaxPattern GetExtendedPattern() {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token != GtTokenContext.NullToken) {
 			/*local*/GtSyntaxPattern Pattern = this.TopLevelNameSpace.GetExtendedPattern(Token.ParsedText);
 			return Pattern;
 		}
 		return null;
 	}
 
 	public boolean MatchToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		if(Token.EqualsText(TokenText)) {
 			this.CurrentPosition += 1;
 			return true;
 		}
 		return false;
 	}
 
 	public GtToken GetMatchedToken(String TokenText) {
 		/*local*/GtToken Token = this.GetToken();
 		while(Token != GtTokenContext.NullToken) {
 			this.CurrentPosition += 1;
 			if(Token.EqualsText(TokenText)) {
 				break;
 			}
 			Token = this.GetToken();
 		}
 		return Token;
 	}
 
 	public final boolean IsAllowedBackTrack() {
 		return IsFlag(this.ParseFlag, BackTrackParseFlag);
 	}
 
 	public final int SetBackTrack(boolean Allowed) {
 		/*local*/int ParseFlag = this.ParseFlag;
 		if(Allowed) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		else {
 			this.ParseFlag = (~(BackTrackParseFlag) & this.ParseFlag);
 		}
 		return ParseFlag;
 	}
 
 	public final GtSyntaxTree ParsePatternAfter(GtNameSpace NameSpace, GtSyntaxTree LeftTree, String PatternName, boolean IsOptional) {
 		/*local*/int Pos = this.CurrentPosition;
 		/*local*/int ParseFlag = this.ParseFlag;
 		/*local*/GtSyntaxPattern Pattern = this.GetPattern(PatternName);
 		if(IsOptional) {
 			this.ParseFlag = this.ParseFlag | BackTrackParseFlag;
 		}
 		/*local*/GtSyntaxTree SyntaxTree = GtStatic.ApplySyntaxPattern(NameSpace, this, LeftTree, Pattern);
 		this.ParseFlag = ParseFlag;
 		if(SyntaxTree != null) {
 			return SyntaxTree;
 		}
 		this.CurrentPosition = Pos;
 		return null;
 	}
 
 	public final GtSyntaxTree ParsePattern(GtNameSpace NameSpace, String PatternName, boolean IsOptional) {
 		return this.ParsePatternAfter(NameSpace, null, PatternName, IsOptional);
 	}
 
 	public final GtMap SkipAndGetAnnotation(boolean IsAllowedDelim) {
 		// this is tentative implementation. In the future, you have to
 		// use this pattern.
 		/*local*/GtMap Annotation = null;
 		this.SkipIndent();
 		while(this.MatchToken("@")) {
 			/*local*/GtToken Token = this.Next();
 			if(Annotation == null) {
 				Annotation = new GtMap();
 			}
 			Annotation.put(Token.ParsedText, true);
 			this.SkipIndent();
 //			if(this.MatchToken(";")) {
 //				if(IsAllowedDelim) {
 //					Annotation = null; // empty statement
 //					this.SkipIndent();
 //				}
 //				else {
 //					return null;
 //				}
 //			}
 		}
 		return Annotation;
 	}
 
 	public final boolean SkipEmptyStatement() {
 		/*local*/GtToken Token = null;
 		while((Token = this.GetToken()) != GtTokenContext.NullToken) {
 			if(Token.IsIndent() || Token.IsDelim()) {
 				this.CurrentPosition += 1;
 				continue;
 			}
 			break;
 		}
 		return (Token != GtTokenContext.NullToken);
 	}
 
 	public final String Stringfy(String PreText, int BeginIdx, int EndIdx) {
 		/*local*/String Buffer = PreText;
 		/*local*/int Position = BeginIdx;
 		while(Position < EndIdx) {
 			/*local*/GtToken Token = this.SourceList.get(Position);
 			if(Token.IsIndent()) {
 				Buffer += "\n";
 			}
 			Buffer += Token.ParsedText;
 			if(Token.IsNextWhiteSpace()) {
 				Buffer += " ";
 			}
 			Position += 1;
 		}
 		return Buffer;
 	}
 	
 	public final void Dump() {
 		/*local*/int Position = this.CurrentPosition;
 		while(Position < this.SourceList.size()) {
 			/*local*/GtToken Token = this.SourceList.get(Position);
 			/*local*/String DumpedToken = "["+Position+"] " + Token;
 			if(Token.PresetPattern != null) {
 				DumpedToken = DumpedToken + " : " + Token.PresetPattern;
 			}
 			LibGreenTea.VerboseLog(VerboseToken,  DumpedToken);
 			Position += 1;
 		}
 	}
 
 	public void StopParsing(boolean b) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
 final class GtSyntaxPattern extends GtStatic {
 	/*field*/public GtNameSpace	          PackageNameSpace;
 	/*field*/public String		          PatternName;
 	/*field*/int				          SyntaxFlag;
 	/*field*/public GtDelegateMatch       MatchFunc;
 	/*field*/public GtDelegateType        TypeFunc;
 	/*field*/public GtSyntaxPattern	      ParentPattern;
 
 	GtSyntaxPattern/*constructor*/(GtNameSpace NameSpace, String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		this.PackageNameSpace = NameSpace;
 		this.PatternName = PatternName;
 		this.SyntaxFlag = 0;
 		this.MatchFunc = MatchFunc;
 		this.TypeFunc  = TypeFunc;
 		this.ParentPattern = null;
 	}
 
 	@Override public String toString() {
 		return this.PatternName + "<" + this.MatchFunc + ">";
 	}
 
 	public boolean IsBinaryOperator() {
 		return IsFlag(this.SyntaxFlag, BinaryOperator);
 	}
 
 	public boolean IsLeftJoin(GtSyntaxPattern Right) {
 		/*local*/int left = this.SyntaxFlag >> PrecedenceShift;
 		/*local*/int right = Right.SyntaxFlag >> PrecedenceShift;
 		return (left < right || (left == right && IsFlag(this.SyntaxFlag, LeftJoin) && IsFlag(Right.SyntaxFlag, LeftJoin)));
 	}
 
 	public final boolean EqualsName(String Name) {
 		return LibGreenTea.EqualsString(this.PatternName, Name);
 	}
 
 }
 
 class GtSyntaxTree extends GtStatic {
 	/*field*/public GtSyntaxTree		ParentTree;
 	/*field*/public GtSyntaxTree		PrevTree;
 	/*field*/public GtSyntaxTree		NextTree;
 
 	/*field*/public GtNameSpace	    NameSpace;
 	/*field*/public GtSyntaxPattern	Pattern;
 	/*field*/public GtToken		    KeyToken;
 	/*field*/public ArrayList<GtSyntaxTree> TreeList;
 	/*field*/public Object          ConstValue;
 	/*field*/public GtMap           Annotation;
 
 	GtSyntaxTree/*constructor*/(GtSyntaxPattern Pattern, GtNameSpace NameSpace, GtToken KeyToken, Object ConstValue) {
 		this.NameSpace = NameSpace;
 		this.KeyToken = KeyToken;
 		this.Pattern = Pattern;
 		this.ParentTree = null;
 		this.PrevTree = null;
 		this.NextTree = null;
 		this.TreeList = null;
 		this.ConstValue = ConstValue;
 		this.Annotation = null;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = "(" + this.KeyToken.ParsedText;
 		/*local*/int i = 0;
 		while(i < ListSize(this.TreeList)) {
 			/*local*/GtSyntaxTree SubTree = this.TreeList.get(i);
 			while(SubTree != null) {
 				/*local*/String Entry = SubTree.toString();
 				if(ListSize(SubTree.TreeList) == 0) {
 					Entry = SubTree.KeyToken.ParsedText;
 				}
 				s = s + " " + Entry;
 				SubTree = SubTree.NextTree;
 			}
 			i += 1;
 		}
 		return s + ")";
 	}
 
 	public final void AppendNext(GtSyntaxTree Tree) {
 		/*local*/GtSyntaxTree TailTree = this;
 		while(TailTree.NextTree != null) {
 			TailTree = TailTree.NextTree;
 		}
 		TailTree.NextTree = Tree;
 	}
 
 	public void SetAnnotation(GtMap Annotation) {
 		this.Annotation = Annotation;
 	}
 
 	public boolean IsError() {
 		return this.KeyToken.IsError();
 	}
 
 	public void ToError(GtToken Token) {
 		LibGreenTea.Assert(Token.IsError());
 		this.KeyToken = Token;
 		this.TreeList = null;
 	}
 
 	public boolean IsEmpty() {
 		return (this.KeyToken == GtTokenContext.NullToken && this.Pattern == null);
 	}
 
 	public void ToEmpty() {
 		this.KeyToken = GtTokenContext.NullToken;
 		this.TreeList = null;
 		this.Pattern = null; // Empty tree must backtrack
 	}
 
 	public boolean IsEmptyOrError() {
 		return this.KeyToken == GtTokenContext.NullToken || this.KeyToken.IsError();
 	}
 
 	public void ToEmptyOrError(GtSyntaxTree ErrorTree) {
 		if(ErrorTree == null) {
 			this.ToEmpty();
 		}
 		else {
 			this.ToError(ErrorTree.KeyToken);
 		}
 	}
 
 	public GtSyntaxTree GetSyntaxTreeAt(int Index) {
 		if(this.TreeList != null && Index >= this.TreeList.size()) {
 			return null;
 		}
 		return this.TreeList.get(Index);
 	}
 
 	public void SetSyntaxTreeAt(int Index, GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(Index >= 0) {
 					if(this.TreeList == null) {
 						this.TreeList = new ArrayList<GtSyntaxTree>();
 					}
 					if(Index < this.TreeList.size()) {
 						this.TreeList.set(Index, Tree);
 						return;
 					}
 					while(this.TreeList.size() < Index) {
 						this.TreeList.add(null);
 					}
 					this.TreeList.add(Tree);
 					Tree.ParentTree = this;
 				}
 			}
 		}
 	}
 
 	public void SetMatchedPatternAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String PatternName,  boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/GtSyntaxTree ParsedTree = TokenContext.ParsePattern(NameSpace, PatternName, IsOptional);
 			if(ParsedTree != null) {
 				this.SetSyntaxTreeAt(Index, ParsedTree);
 			}
 			else if(ParsedTree == null && !IsOptional) {
 				this.ToEmpty();
 			}
 		}
 	}
 
 	public void SetMatchedTokenAt(int Index, GtNameSpace NameSpace, GtTokenContext TokenContext, String TokenText, boolean IsOptional) {
 		if(!this.IsEmptyOrError()) {
 			/*local*/int Pos = TokenContext.CurrentPosition;
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.ParsedText.equals(TokenText)) {
 				this.SetSyntaxTreeAt(Index, new GtSyntaxTree(null, NameSpace, Token, null));
 			}
 			else {
 				TokenContext.CurrentPosition = Pos;
 				if(!IsOptional) {
 					this.ToEmptyOrError(TokenContext.ReportExpectedToken(TokenText));
 				}
 			}
 		}
 	}
 
 	public void AppendParsedTree(GtSyntaxTree Tree) {
 		if(!this.IsError()) {
 			LibGreenTea.Assert(Tree != null);
 			if(Tree.IsError()) {
 				this.ToError(Tree.KeyToken);
 			}
 			else {
 				if(this.TreeList == null) {
 					this.TreeList = new ArrayList<GtSyntaxTree>();
 				}
 				this.TreeList.add(Tree);
 			}
 		}
 	}
 
 	public final GtType GetParsedType() {
 		return (this.ConstValue instanceof GtType) ? (/*cast*/GtType)this.ConstValue : null;
 	}
 
 	public final boolean HasNodeAt(int Index) {
 		if(this.TreeList != null && Index < this.TreeList.size()) {
 			return this.TreeList.get(Index) != null;
 		}
 		return false;
 	}
 
 	public final GtNode TypeCheckNodeAt(int Index, GtTypeEnv Gamma, GtType Type, int TypeCheckPolicy) {
 		if(this.TreeList != null && Index < this.TreeList.size()) {
 			/*local*/GtSyntaxTree ParsedTree = this.TreeList.get(Index);
 			if(ParsedTree != null) {
 				/*local*/GtNode Node = GtStatic.ApplyTypeFunc(ParsedTree.Pattern.TypeFunc, Gamma, ParsedTree, Type);
 				/*local*/GtNode TypedNode = Gamma.TypeCheckSingleNode(ParsedTree, Node, Type, TypeCheckPolicy);
 				return TypedNode;
 			}
 		}
 		if(IsFlag(TypeCheckPolicy, AllowEmptyPolicy)) {
 			return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 		}
 		return Gamma.CreateSyntaxErrorNode(this, "not empty");
 	}
 }
 
 /* typing */
 class GtFieldInfo extends GtStatic {
 	/*field*/public int     FieldFlag;
 	/*field*/public int     FieldIndex;
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	NativeName;
 	/*field*/public Object  InitValue;
 	/*field*/public GtFunc	GetterFunc;
 	/*field*/public GtFunc	SetterFunc;
 
 	GtFieldInfo/*constructor*/(int FieldFlag, GtType Type, String Name, int FieldIndex) {
 		this.FieldFlag = FieldFlag;
 		this.Type = Type;
 		this.Name = Name;
 		this.NativeName = Name; // use this in a generator
 		this.FieldIndex = FieldIndex;
 		this.InitValue = null;
 		this.GetterFunc = null;
 		this.SetterFunc = null;
 	}
 }
 
 final class GtClassField {
 	/*field*/ public ArrayList<GtFieldInfo> FieldList;
 	/*field*/ public int ThisClassIndex;
 
 	GtClassField/*constructor*/(GtType SuperClass) {
 		this.FieldList = new ArrayList<GtFieldInfo>();
 		if(SuperClass.NativeSpec instanceof GtClassField) {
 			/*local*/GtClassField SuperField = (/*cast*/GtClassField)SuperClass.NativeSpec;
 			/*local*/int i = 0;
 			while(i < SuperField.FieldList.size()) {
 				this.FieldList.add(SuperField.FieldList.get(i));
 				i+=1;
 			}
 		}
 		this.ThisClassIndex = this.FieldList.size();
 	}
 
 	public GtFieldInfo CreateField(int FieldFlag, GtType Type, String Name) {
 		/*local*/int i = 0;
 		while(i < this.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = this.FieldList.get(i);
 			if(FieldInfo.Name.equals(Name)) {
 				return null;  // report error
 			}
 			i = i + 1;
 		}
 		/*local*/GtFieldInfo FieldInfo = new GtFieldInfo(FieldFlag, Type, Name, this.FieldList.size());
 		this.FieldList.add(FieldInfo);
 		return FieldInfo;
 	}
 
 }
 
 class GtVariableInfo extends GtStatic {
 	/*field*/public int     VariableFlag;
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	NativeName;
 	/*field*/public int     DefCount;
 	/*field*/public int     UsedCount;
 
 	GtVariableInfo/*constructor*/(int VarFlag, GtType Type, String Name, int Index) {
 		this.VariableFlag = VarFlag;
 		this.Type = Type;
 		this.Name = Name;
 		this.NativeName = GtStatic.NativeVariableName(Name, Index);
 		this.UsedCount = 0;
 		this.DefCount  = 1;
 	}
 }
 
 final class GtTypeEnv extends GtStatic {
 	/*field*/public final GtClassContext    Context;
 	/*field*/public final GtGenerator       Generator;
 	/*field*/public GtNameSpace	    NameSpace;
 
 	/*field*/public GtFunc	Func;
 	/*field*/public ArrayList<GtVariableInfo> LocalStackList;
 	/*field*/public int StackTopIndex;
 
 	/* for convinient short cut */
 	/*field*/public final GtType	VoidType;
 	/*field*/public final GtType	BooleanType;
 	/*field*/public final GtType	IntType;
 	/*field*/public final GtType	StringType;
 	/*field*/public final GtType	VarType;
 	/*field*/public final GtType	AnyType;
 	/*field*/public final GtType    ArrayType;
 	/*field*/public final GtType    FuncType;
 
 	GtTypeEnv/*constructor*/(GtNameSpace NameSpace) {
 		this.NameSpace = NameSpace;
 		this.Context   = NameSpace.Context;
 		this.Generator = NameSpace.Context.Generator;
 		this.Func = null;
 		this.LocalStackList = new ArrayList<GtVariableInfo>();
 		this.StackTopIndex = 0;
 
 		this.VoidType = NameSpace.Context.VoidType;
 		this.BooleanType = NameSpace.Context.BooleanType;
 		this.IntType = NameSpace.Context.IntType;
 		this.StringType = NameSpace.Context.StringType;
 		this.VarType = NameSpace.Context.VarType;
 		this.AnyType = NameSpace.Context.AnyType;
 		this.ArrayType = NameSpace.Context.ArrayType;
 		this.FuncType = NameSpace.Context.FuncType;
 	}
 
 	public final boolean IsStrictMode() {
 		return this.Generator.IsStrictMode();
 	}
 
 	public final boolean IsTopLevel() {
 		return (this.Func == null);
 	}
 
 	public void AppendRecv(GtType RecvType) {
 		/*local*/String ThisName = this.Generator.GetRecvName();
 		this.AppendDeclaredVariable(0, RecvType, ThisName);
 		this.LocalStackList.get(this.StackTopIndex-1).NativeName = ThisName;
 	}
 
 	public void AppendDeclaredVariable(int VarFlag, GtType Type, String Name) {
 		/*local*/GtVariableInfo VarInfo = new GtVariableInfo(VarFlag, Type, Name, this.StackTopIndex);
 		if(this.StackTopIndex < this.LocalStackList.size()) {
 			this.LocalStackList.set(this.StackTopIndex, VarInfo);
 		}
 		else {
 			this.LocalStackList.add(VarInfo);
 		}
 		this.StackTopIndex += 1;
 	}
 
 	public GtVariableInfo LookupDeclaredVariable(String Symbol) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= 0) {
 			/*local*/GtVariableInfo VarInfo = this.LocalStackList.get(i);
 			if(VarInfo.Name.equals(Symbol)) {
 				return VarInfo;
 			}
 			i -= 1;
 		}
 		return null;
 	}
 
 	public final GtNode CreateCoercionNode(GtType ParamType, GtFunc TypeCoercion, GtNode Node) {
 		/*local*/GtNode ApplyNode = this.Generator.CreateApplyNode(ParamType, null, TypeCoercion);
 		ApplyNode.Append(Node);
 		return ApplyNode;
 	}
 
 	public final GtNode ReportTypeResult(GtSyntaxTree ParsedTree, GtNode Node, int Level, String Message) {
 		if(Level == ErrorLevel || (this.IsStrictMode() && Level == TypeErrorLevel)) {
 			LibGreenTea.Assert(Node.Token == ParsedTree.KeyToken);
 			this.NameSpace.Context.ReportError(ErrorLevel, Node.Token, Message);
 			return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 		}
 		else {
 			this.NameSpace.Context.ReportError(Level, Node.Token, Message);
 		}
 		return Node;
 	}
 
 	public final void ReportTypeInference(GtToken SourceToken, String Name, GtType InfferedType) {
 		this.Context.ReportError(InfoLevel, SourceToken, Name + " has type " + InfferedType);
 	}
 
 	public final GtNode CreateSyntaxErrorNode(GtSyntaxTree ParsedTree, String Message) {
 		this.NameSpace.Context.ReportError(ErrorLevel, ParsedTree.KeyToken, Message);
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	public final GtNode SupportedOnlyTopLevelError(GtSyntaxTree ParsedTree) {
 		return this.CreateSyntaxErrorNode(ParsedTree, "supported only at top level " + ParsedTree.Pattern);
 	}
 
 	public final GtNode UnsupportedTopLevelError(GtSyntaxTree ParsedTree) {
 		return this.CreateSyntaxErrorNode(ParsedTree, "unsupported at top level " + ParsedTree.Pattern);
 	}
 
 	public final GtNode CreateLocalNode(GtSyntaxTree ParsedTree, String Name) {
 		/*local*/GtVariableInfo VariableInfo = this.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return this.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.NativeName);
 		}
 		return this.CreateSyntaxErrorNode(ParsedTree, "unresolved name: " + Name + "; not your fault");
 	}
 
 	public final GtNode CreateDefaultValue(GtSyntaxTree ParsedTree, GtType Type) {
 		return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 	}
 
 //	public GtNode DefaultValueConstNode(GtSyntaxTree ParsedTree, GtType Type) {
 //		if(Type.DefaultNullValue != null) {
 //			return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 //		}
 //		return this.CreateSyntaxErrorNode(ParsedTree, "undefined initial value of " + Type);
 //	}
 
 	/* typing */
 	public GtNode TypeEachNode(GtSyntaxTree Tree, GtType Type) {
 		/*local*/GtNode Node = GtStatic.ApplyTypeFunc(Tree.Pattern.TypeFunc, this, Tree, Type);
 		return Node;
 	}
 
 	public GtNode TypeCheckEachNode(GtSyntaxTree Tree, GtType Type, int TypeCheckPolicy) {
 		/*local*/GtNode Node = this.TypeEachNode(Tree, Type);
 		if(Node.IsError()) {
 			return Node;
 		}
 		return Node;
 	}
 
 	public GtNode TypeBlock(GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/int StackTopIndex = this.StackTopIndex;
 		/*local*/GtNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtType CurrentType = Type;
 			if(ParsedTree.NextTree != null) {
 				CurrentType = this.VoidType;
 			}
 			/*local*/GtNode TypedNode = this.TypeCheckEachNode(ParsedTree, CurrentType, DefaultTypeCheckPolicy);
 			/*local*/LastNode = GtStatic.LinkNode(LastNode, TypedNode);
 			if(TypedNode.IsError()) {
 				break;
 			}
 			ParsedTree = ParsedTree.NextTree;
 		}
 		this.StackTopIndex = StackTopIndex;
 		if(LastNode == null) {
 			return null;
 		}
 		return LastNode.MoveHeadNode();
 	}
 
 	public GtNode TypeCheck(GtSyntaxTree ParsedTree, GtType Type, int TypeCheckPolicy) {
 		return this.TypeBlock(ParsedTree, Type);
 	}
 
 	public final GtNode TypeCheckSingleNode(GtSyntaxTree ParsedTree, GtNode Node, GtType Type, int TypeCheckPolicy) {
 		if(Node.IsError() || IsFlag(TypeCheckPolicy, NoCheckPolicy)) {
 			return Node;
 		}
 		if(IsFlag(TypeCheckPolicy, AllowVoidPolicy) || Type == this.VoidType) {
 			return Node;
 		}
 		if(Node.Type == this.VarType) {
 			return this.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "unspecified type: " + Node.Token.ParsedText);
 		}
 		if(Node.Type == Type || Type == this.VarType || Type.Accept(Node.Type)) {
 			return Node;
 		}
 		/*local*/GtFunc Func = ParsedTree.NameSpace.GetCoercionFunc(Node.Type, Type, true);
 		if(Func != null && IsFlag(TypeCheckPolicy, CastPolicy)) {
 			/*local*/GtNode ApplyNode = this.Generator.CreateApplyNode(Type, ParsedTree, Func);
 			ApplyNode.Append(Node);
 			return ApplyNode;
 		}
 		return this.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "type error: requested = " + Type + ", given = " + Node.Type);
 	}
 
 //	public final void DefineFunc(GtFunc Func) {
 //		this.NameSpace.AppendFunc(Func);
 //		this.Func = Func;
 //	}
 
 }
 
 // NameSpace
 
 final class GtNameSpace extends GtStatic {
 	/*field*/public final GtClassContext		Context;
 	/*field*/public final GtNameSpace		    ParentNameSpace;
 	/*field*/public String                      PackageName;
 
 	/*field*/TokenFunc[] TokenMatrix;
 	/*field*/GtMap	 SymbolPatternTable;
 
 	GtNameSpace/*constructor*/(GtClassContext Context, GtNameSpace ParentNameSpace) {
 		this.Context = Context;
 		this.ParentNameSpace = ParentNameSpace;
 		this.PackageName = (ParentNameSpace != null) ? ParentNameSpace.PackageName : null;
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 	}
 
 	public final TokenFunc GetTokenFunc(int GtChar2) {
 		if(this.TokenMatrix == null) {
 			return this.ParentNameSpace.GetTokenFunc(GtChar2);
 		}
 		return this.TokenMatrix[GtChar2];
 	}
 
 	public final void DefineTokenFunc(String keys, GtDelegateToken f) {
 		/*local*/int i = 0;
 		if(this.TokenMatrix == null) {
 			this.TokenMatrix = new TokenFunc[MaxSizeOfChars];
 			if(this.ParentNameSpace != null) {
 				while(i < MaxSizeOfChars) {
 					this.TokenMatrix[i] = this.ParentNameSpace.GetTokenFunc(i);
 				}
 			}
 		}
 		i = 0;
 		while(i < keys.length()) {
 			/*local*/int kchar = GtStatic.AsciiToTokenMatrixIndex(LibGreenTea.CharAt(keys, i));
 			this.TokenMatrix[kchar] = LibGreenTea.CreateOrReuseTokenFunc(f, this.TokenMatrix[kchar]);
 			i += 1;
 		}
 	}
 
 	public final Object GetSymbol(String Key) {
 		/*local*/GtNameSpace NameSpace = this;
 		while(NameSpace != null) {
 			if(NameSpace.SymbolPatternTable != null) {
 				/*local*/Object Value = NameSpace.SymbolPatternTable.get(Key);
 				if(Value != null) {
 					return Value == UndefinedSymbol ? null : Value;
 				}
 			}
 			NameSpace = NameSpace.ParentNameSpace;
 		}
 		return null;
 	}
 
 	public final boolean HasSymbol(String Key) {
 		return (this.GetSymbol(Key) != null);
 	}
 
 	public final void SetSymbol(String Key, Object Value) {
 		if(this.SymbolPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 		}
 		this.SymbolPatternTable.put(Key, Value);
 		LibGreenTea.VerboseLog(VerboseSymbol, "adding symbol: " + Key + ", " + Value);
 	}
 
 	public final void SetUndefinedSymbol(String Symbol) {
 		this.SetSymbol(Symbol, UndefinedSymbol);
 	}
 
 	public GtSyntaxPattern GetPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		if(Body instanceof GtSyntaxPattern) {
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	public GtSyntaxPattern GetExtendedPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol("\t" + PatternName);
 		if(Body instanceof GtSyntaxPattern) {
 			return (/*cast*/GtSyntaxPattern)Body;
 		}
 		return null;
 	}
 
 	private void AppendPattern(String PatternName, GtSyntaxPattern NewPattern) {
 		LibGreenTea.Assert(NewPattern.ParentPattern == null);
 		/*local*/GtSyntaxPattern ParentPattern = this.GetPattern(PatternName);
 		NewPattern.ParentPattern = ParentPattern;
 		this.SetSymbol(PatternName, NewPattern);
 	}
 
 	public void AppendSyntax(String PatternName, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/int Alias = PatternName.indexOf(" ");
 		/*local*/String Name = (Alias == -1) ? PatternName : PatternName.substring(0, Alias);
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, Name, MatchFunc, TypeFunc);
		this.AppendPattern(Name, Pattern);
 		if(Alias != -1) {
 			this.AppendSyntax(PatternName.substring(Alias+1), MatchFunc, TypeFunc);
 		}
 	}
 
 	public void AppendExtendedSyntax(String PatternName, int SyntaxFlag, GtDelegateMatch MatchFunc, GtDelegateType TypeFunc) {
 		/*local*/int Alias = PatternName.indexOf(" ");
 		/*local*/String Name = (Alias == -1) ? PatternName : PatternName.substring(0, Alias);
 		/*local*/GtSyntaxPattern Pattern = new GtSyntaxPattern(this, Name, MatchFunc, TypeFunc);
 		Pattern.SyntaxFlag = SyntaxFlag;
 		this.AppendPattern("\t" + Name, Pattern);
 		if(Alias != -1) {
 			this.AppendExtendedSyntax(PatternName.substring(Alias+1), SyntaxFlag, MatchFunc, TypeFunc);
 		}
 	}
 
 	public final GtType AppendTypeName(GtType ClassInfo) {
 		if(ClassInfo.PackageNameSpace == null) {
 			ClassInfo.PackageNameSpace = this;
 			if(this.PackageName != null) {
 				this.Context.ClassNameMap.put(this.PackageName + "." + ClassInfo.ShortClassName, ClassInfo);
 			}
 		}
 		if(ClassInfo.BaseType == ClassInfo) {
 			this.SetSymbol(ClassInfo.ShortClassName, ClassInfo);
 		}
 		return ClassInfo;
 	}
 
 	public final Object GetClassSymbol(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		while(ClassType != null) {
 			/*local*/String Key = ClassSymbol(ClassType, Symbol);
 			/*local*/Object Value = this.GetSymbol(Key);
 			if(Value != null) {
 				return Value;
 			}
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.SuperType;
 		}
 		return null;
 	}
 
 	public final GtFunc GetGetterFunc(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(ClassType, Symbol, RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		return null;
 	}
 
 	public final GtFunc GetSetterFunc(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(ClassType, Symbol + "=", RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		return null;
 	}
 
 	public final GtFunc GetCoercionFunc(GtType FromType, GtType ToType, boolean RecursiveSearch) {
 		/*local*/Object Func = this.Context.RootNameSpace.GetClassSymbol(FromType, ToType.GetUniqueName(), RecursiveSearch);
 		if(Func instanceof GtFunc) {
 			return (/*cast*/GtFunc)Func;
 		}
 		return null;
 	}
 
 	public final GtPolyFunc GetMethod(GtType ClassType, String Symbol, boolean RecursiveSearch) {
 		/*local*/GtPolyFunc PolyFunc = null;
 		while(ClassType != null) {
 			/*local*/String Key = GtStatic.ClassSymbol(ClassType, Symbol);
 			PolyFunc = GtStatic.JoinPolyFunc(ClassType, PolyFunc, this.GetSymbol(Key));
 			if(!RecursiveSearch) {
 				break;
 			}
 			ClassType = ClassType.SuperType;
 		}
 		return PolyFunc;
 	}
 
 	public final GtPolyFunc GetConstructorFunc(GtType ClassType) {
 		return this.Context.RootNameSpace.GetMethod(ClassType, "", false);
 	}
 
 	public final GtFunc GetFuncParam(String FuncName, int BaseIndex, GtType[] ParamTypes) {
 		/*local*/Object FuncValue = this.GetSymbol(FuncName);
 		if(FuncValue instanceof GtFunc) {
 			/*local*/GtFunc Func = (/*cast*/GtFunc)FuncValue;
 			if(Func.EqualsParamTypes(BaseIndex, ParamTypes)) {
 				return Func;
 			}
 		}
 		else if(FuncValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)FuncValue;
 			/*local*/int i = PolyFunc.FuncList.size();
 			while(i >= 1) {
 				if(PolyFunc.FuncList.get(i-1).EqualsParamTypes(BaseIndex, ParamTypes)) {
 					return PolyFunc.FuncList.get(i);
 				}
 				i = i - 1;
 			}
 		}
 		return null;
 	}
 
 	private final GtNameSpace PublicNameSpace(boolean IsPublic) {
 		return IsPublic ? this.Context.RootNameSpace : this;
 	}
 
 	public final Object AppendFuncName(String Key, GtFunc Func) {
 		/*local*/Object OldValue = this.GetSymbol(Key);
 		if(OldValue instanceof GtFunc) {
 			/*local*/GtPolyFunc PolyFunc = new GtPolyFunc(this, (/*cast*/GtFunc)OldValue);
 			this.SetSymbol(Key, PolyFunc);
 			return PolyFunc.Append(Func);
 		}
 		else if(OldValue instanceof GtPolyFunc) {
 			/*local*/GtPolyFunc PolyFunc = ((/*cast*/GtPolyFunc)OldValue).Dup(this);
 			this.SetSymbol(Key, PolyFunc);
 			return PolyFunc.Append(Func);
 		}
 		else {
 			this.SetSymbol(Key, Func);
 		}
 		return OldValue;
 	}
 
 	public final Object AppendFunc(GtFunc Func) {
 		return this.PublicNameSpace(Func.Is(PublicFunc)).AppendFuncName(Func.FuncName, Func);
 	}
 
 	public final Object AppendMethod(GtType ClassType, GtFunc Func) {
 		/*local*/String Key = ClassSymbol(ClassType, Func.FuncName);
 		return this.PublicNameSpace(Func.Is(PublicFunc)).AppendFuncName(Key, Func);
 	}
 
 	public final void AppendConstructor(GtType ClassType, GtFunc Func) {
 		/*local*/String Key = ClassSymbol(ClassType, "");
 		Func.FuncFlag |= ConstructorFunc;
 		this.Context.RootNameSpace.AppendFuncName(Key, Func);  // @Public
 	}
 
 	public final void SetGetterFunc(GtType ClassType, String Name, GtFunc Func) {
 		/*local*/String Key = ClassSymbol(ClassType, Name);
 		Func.FuncFlag |= GetterFunc;
 		this.Context.RootNameSpace.SetSymbol(Key, Func);  // @Public
 	}
 
 	public final void SetSetterFunc(GtType ClassType, String Name, GtFunc Func) {
 		/*local*/String Key = ClassSymbol(ClassType, Name + "=");
 		Func.FuncFlag |= SetterFunc;
 		this.Context.RootNameSpace.SetSymbol(Key, Func);  // @Public
 	}
 
 	public final void SetCoercionFunc(GtType ClassType, GtType ToType, GtFunc Func) {
 		/*local*/String Key = ClassSymbol(ClassType, "to" + ToType);
 		this.PublicNameSpace(Func.Is(PublicFunc)).SetSymbol(Key, Func);
 	}
 
 	public final void ReportOverrideName(GtToken Token, GtType ClassType, String Symbol) {
 		/*local*/String Message = "duplicated symbol: ";
 		if(ClassType == null) {
 			Message += Symbol;
 		}
 		else {
 			Message += ClassType + "." + Symbol;
 		}
 		this.Context.ReportError(WarningLevel, Token, Message);
 	}
 	
 	public final Object Eval(String ScriptSource, long FileLine) {
 		/*local*/Object ResultValue = null;
 		LibGreenTea.VerboseLog(VerboseEval, "eval: " + ScriptSource);
 		/*local*/GtTokenContext TokenContext = new GtTokenContext(this, ScriptSource, FileLine);
 		this.Context.Generator.StartCompilationUnit();
 		TokenContext.SkipEmptyStatement();
 		while(TokenContext.HasNext()) {
 			/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 			/*local*/GtSyntaxTree TopLevelTree = GtStatic.ParseExpression(this, TokenContext);
 			TopLevelTree.SetAnnotation(Annotation);
 			/*local*/GtTypeEnv Gamma = new GtTypeEnv(this);
 			/*local*/GtNode node = Gamma.TypeCheckEachNode(TopLevelTree, Gamma.VoidType, DefaultTypeCheckPolicy);
 			ResultValue = this.Context.Generator.Eval(node);
 			TokenContext.SkipEmptyStatement();
 			TokenContext.Vacume();
 		}
 		this.Context.Generator.FinishCompilationUnit();
 		return ResultValue;
 	}
 
 	public final boolean LoadFile(String FileName) {
 		/*local*/String ScriptText = LibGreenTea.LoadFile2(FileName);
 		if(ScriptText != null) {
 			/*local*/long FileLine = this.Context.GetFileLine(FileName, 1);
 			this.Eval(ScriptText, FileLine);
 			return true;
 		}
 		return false;
 	}
 
 	public final boolean LoadRequiredLib(String LibName) {
 		/*local*/String Key = GtStatic.NativeNameSuffix + LibName.toLowerCase();
 		if(!this.HasSymbol(Key)) {
 			/*local*/String Path = LibGreenTea.GetLibPath(this.Context.Generator.TargetCode, LibName);
 			/*local*/String Script = LibGreenTea.LoadFile2(Path);
 			if(Script == null) {
 				return false;
 			}
 			/*local*/long FileLine = this.Context.GetFileLine(Path, 1);
 			this.Eval(Script, FileLine);
 			this.SetSymbol(Key, Script); // Rich
 		}
 		return true;
 	}
 
 }
 
 class GtGrammar extends GtStatic {
 	public void LoadTo(GtNameSpace NameSpace) {
 		/*extension*/
 	}
 }
 
 final class GreenTeaGrammar extends GtGrammar {
 	// Token
 	public static int WhiteSpaceToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		TokenContext.FoundWhiteSpace();
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(!LibGreenTea.IsWhitespace(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		return pos;
 	}
 
 	public static int IndentToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int LineStart = pos + 1;
 		TokenContext.FoundLineFeed(1);
 		pos = pos + 1;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(!LibGreenTea.IsWhitespace(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		/*local*/String Text = "";
 		if(LineStart < pos) {
 			Text = SourceText.substring(LineStart, pos);
 		}
 		TokenContext.AddNewToken(Text, IndentTokenFlag, null);
 		return pos;
 		//TokenContext.AddNewToken(SourceText.substring(pos), SourceTokenFlag, null);
 		//return SourceText.length();
 	}
 
 	public static int SymbolToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(!LibGreenTea.IsLetter(ch) && !LibGreenTea.IsDigit(ch) && ch != '_') {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), NameSymbolTokenFlag, null);
 		return pos;
 	}
 
 	public static int OperatorToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, NextPos);
 			if(LibGreenTea.IsWhitespace(ch) || LibGreenTea.IsLetter(ch) || LibGreenTea.IsDigit(ch)) {
 				break;
 			}
 			NextPos += 1;
 		}
 		/*local*/boolean Matched = false;
 		while(NextPos > pos) {
 			/*local*/String Sub = SourceText.substring(pos, NextPos);
 			/*local*/GtSyntaxPattern Pattern = TokenContext.TopLevelNameSpace.GetExtendedPattern(Sub);
 			if(Pattern != null) {
 				Matched = true;
 				break;
 			}
 			NextPos -= 1;
 		}
 		// FIXME
 		if(Matched == false) {
 			NextPos = pos + 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(pos, NextPos), 0, null);
 		return NextPos;
 	}
 
 	public static int CommentToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		if(NextPos < SourceText.length()) {
 			/*local*/char NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 			if(NextChar != '/' && NextChar != '*') {
 				return NoMatch;
 			}
 			/*local*/int Level = 0;
 			/*local*/char PrevChar = 0;
 			if(NextChar == '*') {
 				Level = 1;
 			}
 			while(NextPos < SourceText.length()) {
 				NextChar = LibGreenTea.CharAt(SourceText, NextPos);
 				if(NextChar == '\n' && Level == 0) {
 					return GreenTeaGrammar.IndentToken(TokenContext, SourceText, NextPos);
 				}
 				if(NextChar == '/' && PrevChar == '*') {
 					if(Level == 1) {
 						return NextPos + 1;
 					}
 					Level = Level - 1;
 				}
 				if(Level > 0) {
 					if(NextChar == '*' && PrevChar == '/') {
 						Level = Level + 1;
 					}
 				}
 				PrevChar = NextChar;
 				NextPos = NextPos + 1;
 			}
 		}
 		return NoMatch;
 	}
 
 	public static int NumberLiteralToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(!LibGreenTea.IsDigit(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, "$IntegerLiteral$");
 		return pos;
 	}
 
 	public static int StringLiteralToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		/*local*/char prev = '"';
 		pos = pos + 1; // eat "\""
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(SourceText.substring(start, pos + 1), QuotedTokenFlag, "$StringLiteral$");
 				return pos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, pos));
 				TokenContext.FoundLineFeed(1);
 				return pos;
 			}
 			pos = pos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, pos));
 		return pos;
 	}
 
 	public static int StringLiteralToken_StringInterpolation(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos + 1;
 		/*local*/int NextPos = start;
 		/*local*/char prev = '"';
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, NextPos);
 			if(ch == '$') {
 				/*local*/int end = NextPos + 1;
 				ch = LibGreenTea.CharAt(SourceText, end);
 				if(ch == '{') {
 					while(end < SourceText.length()) {
 						ch = LibGreenTea.CharAt(SourceText, end);
 						if(ch == '}') {
 							break;
 						}
 						end = end + 1;
 					}
 					/*local*/String Expr = SourceText.substring(NextPos + 2, end);
 					/*local*/GtTokenContext LocalContext = new GtTokenContext(TokenContext.TopLevelNameSpace, Expr, TokenContext.ParsingLine);
 					LocalContext.SkipEmptyStatement();
 
 					TokenContext.AddNewToken("\"" + SourceText.substring(start, NextPos) + "\"", 0, "$StringLiteral$");
 					TokenContext.AddNewToken("+", 0, null);
 					while(LocalContext.HasNext()) {
 						/*local*/GtToken NewToken = LocalContext.Next();
 						TokenContext.AddNewToken(NewToken.ParsedText, 0, null);
 					}
 					TokenContext.AddNewToken("+", 0, null);
 					end = end + 1;
 					start = end;
 					NextPos = end;
 					prev = ch;
 					if(ch == '"') {
 						TokenContext.AddNewToken("\"" + SourceText.substring(start, NextPos) + "\"", 0, "$StringLiteral$");
 						return NextPos + 1;
 					}
 					continue;
 				}
 			}
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken("\"" + SourceText.substring(start, NextPos) + "\"", 0, "$StringLiteral$");
 				return NextPos + 1;
 			}
 			if(ch == '\n') {
 				TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, NextPos));
 				TokenContext.FoundLineFeed(1);
 				return NextPos;
 			}
 			NextPos = NextPos + 1;
 			prev = ch;
 		}
 		TokenContext.ReportTokenError(ErrorLevel, "expected \" to close the string literal", SourceText.substring(start, NextPos));
 		return NextPos;
 	}
 
 	// parser and type checker
 	public static GtSyntaxTree ParseType(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(!(ConstValue instanceof GtType)) {
 			return null;  // Not matched
 		}
 		/*local*/GtType ParsedType = (/*cast*/GtType)ConstValue;
 		/*local*/int BacktrackPosition = TokenContext.CurrentPosition;
 		if(ParsedType.IsGenericType()) {
 			if(TokenContext.MatchToken("<")) {  // Generics
 				/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 				while(!TokenContext.MatchToken(">")) {
 					/*local*/GtSyntaxTree ParamTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 					if(ParamTree == null) {
 						TokenContext.CurrentPosition = BacktrackPosition;
 						return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 					}
 					TypeList.add(ParamTree.GetParsedType());
 					if(TokenContext.MatchToken(",")) {
 						continue;
 					}
 				}
 				ParsedType = NameSpace.Context.GetGenericType(ParsedType, 0, TypeList, true);
 				BacktrackPosition = TokenContext.CurrentPosition;
 			}
 		}
 		while(TokenContext.MatchToken("[")) {  // Array
 			if(!TokenContext.MatchToken("]")) {
 				TokenContext.CurrentPosition = BacktrackPosition;
 				return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 			}
 			ParsedType = NameSpace.Context.GetGenericType1(NameSpace.Context.ArrayType, ParsedType, true);
 			BacktrackPosition = TokenContext.CurrentPosition;
 		}
 		return new GtSyntaxTree(Pattern, NameSpace, Token, ParsedType);
 	}
 
 	public static GtSyntaxTree ParseConst(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue != null) {
 			return new GtSyntaxTree(Pattern, NameSpace, Token, ConstValue);
 		}
 		return null; // Not Matched
 	}
 
 	public static GtNode TypeConst(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		if(ParsedTree.ConstValue instanceof String) {
 			ParsedTree.ConstValue = (/*cast*/String) ParsedTree.ConstValue;
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ParsedTree.ConstValue), ParsedTree, ParsedTree.ConstValue);
 	}
 
 	public static GtSyntaxTree ParseNull(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("null");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		return NewTree;
 	}
 
 	public static GtNode TypeNull(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType ThisType = ContextType;
 		if(ThisType == Gamma.VarType) {
 			ThisType = Gamma.AnyType;
 		}
 		return Gamma.Generator.CreateNullNode(ThisType, ParsedTree);
 	}
 
 	public static GtSyntaxTree ParseSymbol(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TypeTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 		if(TypeTree != null) {
 			/*local*/GtSyntaxTree DeclTree = TokenContext.ParsePatternAfter(NameSpace, TypeTree, "$FuncDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			DeclTree = TokenContext.ParsePatternAfter(NameSpace, TypeTree, "$VarDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			return TypeTree;
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 //		/*local*/Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 //		if(ConstValue != null && !(ConstValue instanceof GtType)) {
 //			return new GtSyntaxTree(NameSpace.GetPattern("$Const$"), NameSpace, Token, ConstValue);
 //		}
 		return new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, Token, null);
 	}
 
 	public static GtSyntaxTree ParseVariable(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/char ch = LibGreenTea.CharAt(Token.ParsedText, 0);
 		if(LibGreenTea.IsLetter(ch) || ch == '_') {
 			return new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		}
 		return null;
 	}
 
 	public static GtNode TypeVariable(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtVariableInfo VariableInfo = Gamma.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return Gamma.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.NativeName);
 		}
 //		Don't add ad hoc code
 //		if(Name.equals("this")) {
 //			VariableInfo = Gamma.LookupDeclaredVariable(Gamma.Generator.GetRecvName());
 //			if(VariableInfo != null) {
 //				return Gamma.Generator.CreateLocalNode(VariableInfo.Type, ParsedTree, VariableInfo.NativeName);
 //			}
 //		}
 		/*local*/Object ConstValue = (/*cast*/Object) ParsedTree.NameSpace.GetSymbol(Name);
 		if(ConstValue != null) {
 			return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateLocalNode(Gamma.AnyType, ParsedTree, Name + Gamma.Generator.BlockComment("undefined"));
 		return Gamma.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "undefined name: " + Name);
 	}
 
 	public static GtSyntaxTree ParseVarDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			Tree.SetMatchedPatternAt(VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 		}
 		else {
 			Tree.SetSyntaxTreeAt(VarDeclType, LeftTree);
 		}
 		Tree.SetMatchedPatternAt(VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 		if(Tree.IsEmptyOrError()) {
 			return null;
 		}
 		if(TokenContext.MatchToken("=")) {
 			Tree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 		}
 		while(TokenContext.MatchToken(",")) {
 			/*local*/GtSyntaxTree NextTree = new GtSyntaxTree(Pattern, NameSpace, Tree.KeyToken, null);
 			NextTree.SetSyntaxTreeAt(VarDeclType, Tree.GetSyntaxTreeAt(VarDeclType));
 			Tree = GtStatic.LinkTree(Tree, NextTree);
 			Tree.SetMatchedPatternAt(VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				Tree.SetMatchedPatternAt(VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 		}
 		return Tree;
 	}
 
 	public static GtNode TypeVarDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int VarFlag = Gamma.Generator.ParseVarFlag(0, ParsedTree.Annotation);
 		/*local*/GtSyntaxTree TypeTree = ParsedTree.GetSyntaxTreeAt(VarDeclType);
 		/*local*/GtSyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(VarDeclName);
 		/*local*/GtSyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(VarDeclValue);
 		/*local*/GtType DeclType = TypeTree.GetParsedType();
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		Gamma.AppendDeclaredVariable(VarFlag, DeclType, VariableName);
 		/*local*/GtNode VariableNode = Gamma.TypeCheck(NameTree, DeclType, DefaultTypeCheckPolicy);
 		if(VariableNode.IsError()) {
 			return VariableNode;
 		}
 		/*local*/GtNode InitValueNode = (ValueTree == null) ? Gamma.CreateDefaultValue(ParsedTree, DeclType) : Gamma.TypeCheck(ValueTree, DeclType, DefaultTypeCheckPolicy);
 		/*local*/GtNode BlockNode = Gamma.TypeBlock(ParsedTree.NextTree, ContextType);
 		ParsedTree.NextTree = null;
 		return Gamma.Generator.CreateVarNode(DeclType, ParsedTree, DeclType, ((/*cast*/LocalNode)VariableNode).NativeName, InitValueNode, BlockNode);
 	}
 
 	// Parse And Type
 	public static GtSyntaxTree ParseIntegerLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.ParseInt(Token.ParsedText));
 	}
 
 	public static GtSyntaxTree ParseStringLiteral(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, LibGreenTea.UnquoteString(Token.ParsedText));
 		return NewTree;
 	}
 
 	public static GtSyntaxTree ParseExpression(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return GtStatic.ParseExpression(NameSpace, TokenContext);
 	}
 
 	public static GtSyntaxTree ParseUnary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		Tree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		return Tree;
 	}
 
 	public static GtNode TypeUnary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ExprNode  = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ExprNode.IsError()) {
 			return ExprNode;
 		}
 		/*local*/GtType BaseType = ExprNode.Type;
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, OperatorSymbol, true);
 		if(PolyFunc != null) {
 			ResolvedFunc = PolyFunc.ResolveUnaryFunc(Gamma, ParsedTree, ExprNode);
 		}
 		if(ResolvedFunc == null) {
 			Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 		}
 		else {
 			ReturnType = ResolvedFunc.GetReturnType();
 		}
 		/*local*/GtNode UnaryNode =  Gamma.Generator.CreateUnaryNode(ReturnType, ParsedTree, ResolvedFunc, ExprNode);
 		if(ResolvedFunc == null && !BaseType.IsDynamic()) {
 			return Gamma.ReportTypeResult(ParsedTree, UnaryNode, TypeErrorLevel, "undefined operator: "+ OperatorSymbol + " of " + BaseType);
 		}
 		return UnaryNode;
 	}
 
 	public static GtSyntaxTree ParseBinary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree RightTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 		if(GtStatic.IsEmptyOrError(RightTree)) {
 			return RightTree;
 		}
 		if(RightTree.Pattern.IsBinaryOperator()) {
 			if(Pattern.IsLeftJoin(RightTree.Pattern)) {
 				/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 				NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 				NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree.GetSyntaxTreeAt(LeftHandTerm));
 				RightTree.SetSyntaxTreeAt(LeftHandTerm, NewTree);
 				return RightTree;
 			}
 		}
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 		NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree);
 		if(RightTree.NextTree != null) {
 			GtStatic.LinkTree(NewTree, RightTree.NextTree);
 			RightTree.NextTree = null;
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeBinary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String OperatorSymbol = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode LeftNode  = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode.IsError()) {
 			return LeftNode;
 		}
 		if(RightNode.IsError()) {
 			return RightNode;
 		}
 		/*local*/GtType BaseType = LeftNode.Type;
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseType, OperatorSymbol, true);
 		if(PolyFunc != null) {
 			/*local*/GtNode[] BinaryNodes = new GtNode[2];
 			BinaryNodes[0] = LeftNode;
 			BinaryNodes[1] = RightNode;
 			ResolvedFunc = PolyFunc.ResolveBinaryFunc(Gamma, BinaryNodes);
 			LeftNode = BinaryNodes[0];
 			RightNode = BinaryNodes[1];
 		}
 		if(ResolvedFunc == null) {
 			Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched operators: " + PolyFunc);
 		}
 		else {
 			ReturnType = ResolvedFunc.GetReturnType();
 		}
 		/*local*/GtNode BinaryNode =  Gamma.Generator.CreateBinaryNode(ReturnType, ParsedTree, ResolvedFunc, LeftNode, RightNode);
 		if(ResolvedFunc == null && !BaseType.IsDynamic()) {
 			return Gamma.ReportTypeResult(ParsedTree, BinaryNode, TypeErrorLevel, "undefined operator: "+ OperatorSymbol + " of " + LeftNode.Type);
 		}
 		return BinaryNode;
 	}
 
 	public static GtSyntaxTree ParseTrinary(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TrinaryTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("?"), null);
 		TrinaryTree.SetSyntaxTreeAt(IfCond, LeftTree);
 		TrinaryTree.SetMatchedPatternAt(IfThen, NameSpace, TokenContext, "$expression$", Required);
 		TrinaryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 		TrinaryTree.SetMatchedPatternAt(IfElse, NameSpace, TokenContext, "$expression$", Required);
 		return TrinaryTree;
 	}
 
 	public static GtNode TypeTrinary(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ThenNode = ParsedTree.TypeCheckNodeAt(IfThen, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ThenNode.IsError()) {
 			return ThenNode;
 		}
 		/*local*/GtNode ElseNode = ParsedTree.TypeCheckNodeAt(IfThen, Gamma, ThenNode.Type, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateTrinaryNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// PatternName: "("
 	public static GtSyntaxTree ParseGroup(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtSyntaxTree GroupTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("("), null);
 		/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 		GroupTree.AppendParsedTree(Tree);
 		if(!TokenContext.MatchToken(")")) {
 			GroupTree = TokenContext.ReportExpectedToken(")");
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return GroupTree;
 	}
 
 	public static GtNode TypeGroup(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, ContextType, DefaultTypeCheckPolicy);
 	}
 
 	// PatternName: "(" "to" $Type$ ")"
 	public static GtSyntaxTree ParseCast(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtToken FirstToken = TokenContext.Next(); // skip the first token
 		/*local*/GtSyntaxTree CastTree = null;
 		if(TokenContext.MatchToken("to")) {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 		}
 		else if(TokenContext.MatchToken("as")) {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 		}
 		else {
 			CastTree = new GtSyntaxTree(Pattern, NameSpace, FirstToken, null);
 		}
 		CastTree.SetMatchedPatternAt(LeftHandTerm, NameSpace, TokenContext, "$Type$", Required);
 		CastTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		TokenContext.ParseFlag = ParseFlag;
 		CastTree.SetMatchedPatternAt(RightHandTerm, NameSpace, TokenContext, "$Expression$", Required);
 		return CastTree;
 	}
 
 	public static GtNode TypeCast(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType CastType = ParsedTree.GetSyntaxTreeAt(LeftHandTerm).GetParsedType();
 		/*local*/int TypeCheckPolicy = CastPolicy;
 		return ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, CastType, TypeCheckPolicy);
 	}
 
 	public static GtSyntaxTree ParseGetter(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.MatchToken(".");
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token.IsNameSymbol()) {
 			/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 			NewTree.AppendParsedTree(LeftTree);
 			return NewTree;
 		}
 		return TokenContext.ReportExpectedToken("field name");
 	}
 
 	public static GtNode TypeGetter(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(ObjectNode.IsError()) {
 			return ObjectNode;
 		}
 		// To start, check class const such as Math.Pi if base is a type value
 		if(ObjectNode instanceof ConstNode && ObjectNode.Type == Gamma.Context.TypeType) {
 			/*local*/GtType ObjectType = (/*cast*/GtType)((/*cast*/ConstNode)ObjectNode).ConstValue;
 			/*local*/Object ConstValue = ParsedTree.NameSpace.GetClassSymbol(ObjectType, Name, true);
 			if(ConstValue != null) {
 				return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(ConstValue), ParsedTree, ConstValue);
 			}
 		}
 		/*local*/GtFunc GetterFunc = ParsedTree.NameSpace.GetGetterFunc(ObjectNode.Type, Name, true);
 		/*local*/GtType ReturnType = (GetterFunc != null) ? GetterFunc.GetReturnType() : Gamma.AnyType;
 		/*local*/GtNode Node = Gamma.Generator.CreateGetterNode(ReturnType, ParsedTree, GetterFunc, ObjectNode);
 		if(GetterFunc == null) {
 			if(!ObjectNode.Type.IsDynamic() && ContextType != Gamma.FuncType) {
 				return Gamma.ReportTypeResult(ParsedTree, Node, TypeErrorLevel, "undefined name " + Name + " of " + ObjectNode.Type);
 			}
 		}
 		return Node;
 	}
 	
 	public static GtSyntaxTree ParseDefined(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		GtSyntaxTree DefinedTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("defined"), null);
 		DefinedTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		DefinedTree.SetMatchedPatternAt(UnaryTerm, NameSpace, TokenContext, "$Expression$", Required);
 		DefinedTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		return DefinedTree;
 	}
 
 	public static GtNode TypeDefined(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		Gamma.Context.SetNoErrorReport(true);
 		/*local*/GtNode ObjectNode = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		Gamma.Context.SetNoErrorReport(false);
 		return Gamma.Generator.CreateConstNode(Gamma.BooleanType, ParsedTree, (ObjectNode instanceof ConstNode));
 	}
 
 
 	public static GtSyntaxTree ParseApply(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtSyntaxTree FuncTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("("), null);
 		FuncTree.AppendParsedTree(LeftTree);
 		if(!TokenContext.MatchToken(")")) {
 			while(!FuncTree.IsEmptyOrError()) {
 //	            Meaningless ?
 //				if(TokenContext.CurrentPosition > 150) {
 //					LibGreenTea.DebugP("");
 //				}
 				/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 				FuncTree.AppendParsedTree(Tree);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				FuncTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return FuncTree;
 	}
 
 	public static GtNode TypeApply(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode FuncNode = ParsedTree.TypeCheckNodeAt(0, Gamma, Gamma.FuncType, NoCheckPolicy);
 		if(FuncNode.IsError()) {
 			return FuncNode;
 		}
 		/*local*/ArrayList<GtNode> NodeList = new ArrayList<GtNode>();
 		NodeList.add(FuncNode);
 		/*local*/GtFunc ResolvedFunc = null;
 		/*local*/int TreeIndex = 1;
 		if(FuncNode instanceof GetterNode) { /* Func style .. o.f x, y, .. */
 			/*local*/GtNode BaseNode = ((/*cast*/GetterNode)FuncNode).Expr;
 			/*local*/String FuncName = FuncNode.Token.ParsedText;
 			NodeList.add(BaseNode);
 			/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(BaseNode.Type, FuncName, true);
 			ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, TreeIndex, NodeList);
 		}
 		else if(FuncNode instanceof ConstNode) { /* Func style .. f x, y .. */
 			/*local*/Object Func = ((/*cast*/ConstNode)FuncNode).ConstValue;
 			if(Func instanceof GtFunc) {
 				ResolvedFunc = (/*cast*/GtFunc)Func;
 			}
 			else if(Func instanceof GtType) {  // constructor;
 				/*local*/GtType ClassType = (/*cast*/GtType)Func;
 				/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetConstructorFunc(/*GtFunc*/ClassType);
 				if(PolyFunc == null) {
 					return Gamma.CreateSyntaxErrorNode(ParsedTree, "no constructor: " + ClassType);
 				}
 				NodeList.set(0, Gamma.Generator.CreateNullNode(ClassType, ParsedTree));
 				ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, NodeList);
 				if(ResolvedFunc == null) {
 					Gamma.Context.ReportError(TypeErrorLevel, ParsedTree.KeyToken, "mismatched : constructor" + PolyFunc);
 				}
 				/*local*/GtNode NewNode = Gamma.Generator.CreateNewNode(ClassType, ParsedTree, ResolvedFunc);
 				NewNode.AppendNodeList(NodeList);
 				return NewNode;
 			}
 			else if(Func instanceof GtPolyFunc) {
 				/*local*/GtPolyFunc PolyFunc = (/*cast*/GtPolyFunc)Func;
 				/*local*/ArrayList<GtNode> ParamList = new ArrayList<GtNode>();
 				ResolvedFunc = PolyFunc.ResolveFunc(Gamma, ParsedTree, 1, ParamList);
 				if(ResolvedFunc != null) {
 					// reset ConstValue as if non-polymorphic function were found
 					((/*cast*/ConstNode)FuncNode).ConstValue = ResolvedFunc;
 					((/*cast*/ConstNode)FuncNode).Type = ResolvedFunc.GetFuncType();
 				}
 			}
 		}
 		/*local*/GtType ReturnType = Gamma.AnyType;
 		if(FuncNode.Type == Gamma.AnyType) {
 			while(TreeIndex < ListSize(ParsedTree.TreeList)) {
 				/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 				if(Node.IsError()) {
 					return Node;
 				}
 				NodeList.add(Node);
 				TreeIndex = TreeIndex + 1;
 			}
 		}
 		else if(FuncNode.Type.BaseType == Gamma.FuncType) {
 			/*local*/GtType FuncType = FuncNode.Type;
 			LibGreenTea.Assert(ListSize(ParsedTree.TreeList) == FuncType.TypeParams.length); // FIXME: add check paramerter size
 			while(TreeIndex < ListSize(ParsedTree.TreeList)) {
 				/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, FuncType.TypeParams[TreeIndex], DefaultTypeCheckPolicy);
 				if(Node.IsError()) {
 					return Node;
 				}
 				NodeList.add(Node);
 				TreeIndex = TreeIndex + 1;
 			}
 			ReturnType = FuncType.TypeParams[0];
 		}
 		else {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, FuncNode.Type + " is not applicapable");
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateApplyNode(ReturnType, ParsedTree, ResolvedFunc);
 		Node.AppendNodeList(NodeList);
 		return Node;
 	}
 
 	public static GtNode TypeAnd(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAndNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeOr(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateOrNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static GtNode TypeInstanceOf(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		GtType GivenType = ParsedTree.GetSyntaxTreeAt(RightHandTerm).GetParsedType();
 		if(GivenType != null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree,  "type is expected in" + ParsedTree.KeyToken);
 		}
 		return Gamma.Generator.CreateInstanceOfNode(Gamma.BooleanType, ParsedTree, LeftNode, GivenType);
 	}
 
 	
 	public static GtNode TypeAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode) {
 			/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateAssignNode(LeftNode.Type, ParsedTree, LeftNode, RightNode);
 		}
 		return Gamma.CreateSyntaxErrorNode(ParsedTree, "not assigned to a left hand value");
 	}
 
 	public static GtNode TypeSelfAssign(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(LeftHandTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode) {
 			/*local*/GtNode RightNode = ParsedTree.TypeCheckNodeAt(RightHandTerm, Gamma, LeftNode.Type, DefaultTypeCheckPolicy);
 			return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, LeftNode, RightNode);
 		}
 		return Gamma.CreateSyntaxErrorNode(ParsedTree, "not assigned to a left hand value");
 	}
 
 	public static GtSyntaxTree ParseIncl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree InclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.Next(), null);
 		if(LeftTree != null) { /* i++ */
 			InclTree.SetSyntaxTreeAt(UnaryTerm, LeftTree);
 		}
 		else { /* ++i */
 			/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 			InclTree.SetSyntaxTreeAt(UnaryTerm, Tree);
 		}
 		return InclTree;
 	}
 
 	public static GtNode TypeIncl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		/*local*/GtNode LeftNode = ParsedTree.TypeCheckNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		if(LeftNode.Type == Gamma.IntType) {
 			if(Type != Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "only available as statement: " + ParsedTree.KeyToken);
 			}
 			if(LeftNode instanceof LocalNode || LeftNode instanceof GetterNode || LeftNode instanceof IndexerNode) {
 				GtNode ConstNode = Gamma.Generator.CreateConstNode(LeftNode.Type, ParsedTree, 1);
 				return Gamma.Generator.CreateSelfAssignNode(LeftNode.Type, ParsedTree, LeftNode, ConstNode);
 			}
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "neither incremental nor decrimental");
 		}
 		return LeftNode.IsError() ? LeftNode : GreenTeaGrammar.TypeUnary(Gamma, ParsedTree, Type);
 	}
 
 	public static GtSyntaxTree ParseEmpty(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		return new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 	}
 
 	public static GtNode TypeEmpty(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	public static GtSyntaxTree ParseRequire(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		TokenContext.Next(); // skipped first token "require";
 		while(TokenContext.HasNext()) {
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.IsIndent() || Token.IsDelim()) {
 				break;
 			}
 			if(Token.IsNameSymbol()) {
 				if(!NameSpace.LoadRequiredLib(Token.ParsedText)) {
 					NameSpace.Context.ReportError(ErrorLevel, Token, "no required library: " + Token.ParsedText);
 					TokenContext.StopParsing(true);
 				}
 			}
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 		}
 		return GreenTeaGrammar.ParseEmpty(NameSpace, TokenContext, LeftTree, Pattern);
 	}
 
 	public static GtSyntaxTree ParseImport(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		GtSyntaxTree ImportTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("import"), null);
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/String PackageName = "";
 		if(Token.IsQuoted()) {
 			PackageName = LibGreenTea.UnquoteString(Token.ParsedText);  // FIXME: unquoted
 		}
 		else {
 			PackageName = Token.ParsedText;
 			while(TokenContext.HasNext()) {
 				Token = TokenContext.Next();
 				if(Token.IsNameSymbol() || TokenContext.MatchToken(".")) {
 					PackageName += Token.ParsedText;
 				}
 				break;
 			}
 		}	
 		ImportTree.ConstValue = PackageName;
 		return ImportTree;
 	}
 
 	public static GtNode TypeImport(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
 		Object Value = Gamma.Generator.ImportNativeObject((String)ParsedTree.ConstValue);
 		if(Value == null) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "cannot import: " + ParsedTree.ConstValue);
 		}
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(Value), ParsedTree, Value);
 	}
 	
 	public static GtSyntaxTree ParseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/GtSyntaxTree PrevTree = null;
 			/*local*/GtNameSpace NameSpace = new GtNameSpace(ParentNameSpace.Context, ParentNameSpace);
 			while(TokenContext.SkipEmptyStatement()) {
 				if(TokenContext.MatchToken("}")) {
 					break;
 				}
 				/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 				/*local*/GtSyntaxTree CurrentTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 				if(GtStatic.IsEmptyOrError(CurrentTree)) {
 					return CurrentTree;
 				}
 				CurrentTree.SetAnnotation(Annotation);
 				PrevTree = GtStatic.LinkTree(PrevTree, CurrentTree);
 			}
 			if(PrevTree == null) {
 				return TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 			}
 			return GtStatic.TreeHead(PrevTree);
 		}
 		return null;
 	}
 
 	public static GtSyntaxTree ParseStatement(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree StmtTree = TokenContext.ParsePattern(NameSpace, "$Block$", Optional);
 		if(StmtTree == null) {
 			StmtTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 		}
 		if(StmtTree == null) {
 			StmtTree = TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 		}
 		return StmtTree;
 	}
 
 	public static GtNode TypeBlock(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.TypeBlock(ParsedTree, ContextType);
 	}
 
 	// If Statement
 	public static GtSyntaxTree ParseIf(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("if");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		NewTree.SetMatchedPatternAt(IfCond, NameSpace, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		NewTree.SetMatchedPatternAt(IfThen, NameSpace, TokenContext, "$Statement$", Required);
 		if(TokenContext.MatchToken("else")) {
 			NewTree.SetMatchedPatternAt(IfElse, NameSpace, TokenContext, "$Statement$", Required);
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return NewTree;
 	}
 
 	public static GtNode TypeIf(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode ThenNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(IfThen), ContextType);
 		/*local*/GtNode ElseNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(IfElse), ThenNode.Type);
 		return Gamma.Generator.CreateIfNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// While Statement
 	public static GtSyntaxTree ParseWhile(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree WhileTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("while"), null);
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		WhileTree.SetMatchedPatternAt(WhileCond, NameSpace, TokenContext, "$Expression$", Required);
 		WhileTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		WhileTree.SetMatchedPatternAt(WhileBody, NameSpace, TokenContext, "$Block$", Required);
 		return WhileTree;
 	}
 
 	public static GtNode TypeWhile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(WhileCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/GtNode BodyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(WhileBody), ContextType);
 		return Gamma.Generator.CreateWhileNode(BodyNode.Type, ParsedTree, CondNode, BodyNode);
 	}
 
 	// Break/Continue Statement
 	public static GtSyntaxTree ParseBreak(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("break");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		return NewTree;
 	}
 
 	public static GtNode TypeBreak(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateBreakNode(Gamma.VoidType, ParsedTree, "");
 	}
 
 	public static GtSyntaxTree ParseContinue(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("continue");
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		return NewTree;
 	}
 
 	public static GtNode TypeContinue(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		return Gamma.Generator.CreateContinueNode(Gamma.VoidType, ParsedTree, "");
 	}
 
 	// Return Statement
 	public static GtSyntaxTree ParseReturn(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("return"), null);
 		ReturnTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Optional);
 		return ReturnTree;
 	}
 
 	public static GtNode TypeReturn(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		ParsedTree.NextTree = null; // stop
 		if(Gamma.IsTopLevel()) {
 			return Gamma.UnsupportedTopLevelError(ParsedTree);
 		}
 		/*local*/GtType ReturnType = Gamma.Func.GetReturnType();
 		if(ParsedTree.HasNodeAt(ReturnExpr)) {
 			/*local*/GtNode Expr = ParsedTree.TypeCheckNodeAt(ReturnExpr, Gamma, ReturnType, DefaultTypeCheckPolicy);
 			if(ReturnType == Gamma.VarType && !Expr.IsError()) {
 				Gamma.Func.Types[0] = Expr.Type;
 				Gamma.ReportTypeInference(ParsedTree.KeyToken, "return value of " + Gamma.Func.FuncName, Expr.Type);
 			}
 			if(ReturnType == Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "ignored return value");
 				return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, null);
 			}
 			return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 		}
 		else {
 			if(ReturnType == Gamma.VarType) {
 				Gamma.Func.Types[0] = Gamma.VoidType;
 				Gamma.ReportTypeInference(ParsedTree.KeyToken, "return value of " + Gamma.Func.FuncName, Gamma.VoidType);
 			}
 			if(Gamma.Func.Is(ConstructorFunc)) {
 				/*local*/GtNode ThisNode = Gamma.CreateLocalNode(ParsedTree, Gamma.Generator.GetRecvName());
 				return Gamma.Generator.CreateReturnNode(ThisNode.Type, ParsedTree, ThisNode);
 			}
 			if(ReturnType != Gamma.VoidType) {
 				Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "returning default value of " + ReturnType);
 				return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, Gamma.CreateDefaultValue(ParsedTree, ReturnType));
 			}
 			return Gamma.Generator.CreateReturnNode(ReturnType, ParsedTree, null);
 		}
 	}
 
 	// try
 	public static GtSyntaxTree ParseTry(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree TryTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("try"), null);
 		TryTree.SetMatchedPatternAt(TryBody, NameSpace, TokenContext, "$Block$", Required);
 		if(TokenContext.MatchToken("catch")) {
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 			TryTree.SetMatchedPatternAt(CatchVariable, NameSpace, TokenContext, "$VarDecl$", Required);
 			TryTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 			TryTree.SetMatchedPatternAt(CatchBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		if(TokenContext.MatchToken("finally")) {
 			TryTree.SetMatchedPatternAt(FinallyBody, NameSpace, TokenContext, "$Block$", Required);
 		}
 		return TryTree;
 	}
 
 	public static GtNode TypeTry(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType FaultType = ContextType; // FIXME Gamma.FaultType;
 		/*local*/GtNode TryNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(TryBody), ContextType);
 		/*local*/GtNode CatchExpr = ParsedTree.TypeCheckNodeAt(CatchVariable, Gamma, FaultType, DefaultTypeCheckPolicy);
 		/*local*/GtNode CatchNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(CatchBody), ContextType);
 		/*local*/GtNode FinallyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(FinallyBody), ContextType);
 		return Gamma.Generator.CreateTryNode(TryNode.Type, ParsedTree, TryNode, CatchExpr, CatchNode, FinallyNode);
 	}
 
 	// throw $Expr$
 	public static GtSyntaxTree ParseThrow(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ThrowTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("throw"), null);
 		ThrowTree.SetMatchedPatternAt(ReturnExpr, NameSpace, TokenContext, "$Expression$", Required);
 		return ThrowTree;
 	}
 
 	public static GtNode TypeThrow(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType FaultType = ContextType; // FIXME Gamma.FaultType;
 		/*local*/GtNode ExprNode = ParsedTree.TypeCheckNodeAt(ReturnExpr, Gamma, FaultType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateThrowNode(ExprNode.Type, ParsedTree, ExprNode);
 	}
 
 	public static GtSyntaxTree ParseSuper(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token =TokenContext.GetMatchedToken("super");
 		/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		Tree.SetSyntaxTreeAt(CallExpressionOffset, new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, Token, null));
 		Tree.SetSyntaxTreeAt(CallParameterOffset,  new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, new GtToken("this", 0), null));
 		TokenContext.MatchToken("(");
 		if(!TokenContext.MatchToken(")")) {
 			while(!Tree.IsEmptyOrError()) {
 				/*local*/GtSyntaxTree ParamTree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 				Tree.AppendParsedTree(ParamTree);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				Tree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		if(!Tree.IsEmptyOrError()) {
 			// translate '$super$(this, $Params$)' => 'super(this, $Params$)'
 			Tree.Pattern = NameSpace.GetExtendedPattern("(");
 			return Tree;
 		}
 		return Tree;
 	}
 	// new $Type ( $Expr$ [, $Expr$] )
 	public static GtSyntaxTree ParseNew(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("new"), null);
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		NewTree.SetMatchedPatternAt(CallExpressionOffset, NameSpace, TokenContext, "$Type$", Required);
 		TokenContext.MatchToken("(");
 		if(!TokenContext.MatchToken(")")) {
 			while(!NewTree.IsEmptyOrError()) {
 				/*local*/GtSyntaxTree Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Required);
 				NewTree.AppendParsedTree(Tree);
 				if(TokenContext.MatchToken(")")) {
 					break;
 				}
 				NewTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		if(NewTree.IsEmptyOrError()) {
 			return null;
 		}
 		return NewTree;
 	}
 
 	// switch
 	public static GtSyntaxTree ParseEnum(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/String EnumTypeName = null;
 		/*local*/GtType NewEnumType = null;
 		/*local*/GtMap VocabMap = new GtMap();
 		/*local*/GtSyntaxTree EnumTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("enum"), null);
 		EnumTree.SetMatchedPatternAt(EnumNameTreeIndex, NameSpace, TokenContext, "$FuncName$", Required);  // $ClassName$ is better
 		if(!EnumTree.IsEmptyOrError()) {
 			EnumTypeName = EnumTree.GetSyntaxTreeAt(EnumNameTreeIndex).KeyToken.ParsedText;
 			if(NameSpace.GetSymbol(EnumTypeName) != null) {
 				NameSpace.Context.ReportError(WarningLevel, EnumTree.KeyToken, "already defined name: " + EnumTypeName);
 			}
 			NewEnumType = new GtType(NameSpace.Context, EnumClass, EnumTypeName, null, VocabMap);
 		}
 		EnumTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 		/*local*/int EnumValue = 0;
 		while(!EnumTree.IsEmptyOrError()) {
 			TokenContext.SkipIndent();
 			if(TokenContext.MatchToken(",")) {
 				continue;
 			}
 			if(TokenContext.MatchToken("}")) {
 				break;
 			}
 			/*local*/GtToken Token = TokenContext.Next();
 			if(LibGreenTea.IsLetter(LibGreenTea.CharAt(Token.ParsedText, 0))) {
 				if(VocabMap.get(Token.ParsedText) != null) {
 					NameSpace.Context.ReportError(WarningLevel, Token, "already defined name: " + Token.ParsedText);
 					continue;
 				}
 				VocabMap.put(Token.ParsedText, new GreenTeaEnum(NewEnumType, EnumValue, Token.ParsedText));
 				EnumValue += 1;
 				continue;
 			}
 		}
 		if(!EnumTree.IsEmptyOrError()) {
 			NameSpace.AppendTypeName(NewEnumType);
 			EnumTree.ConstValue = NewEnumType;
 		}
 		return EnumTree;
 	}
 
 	public static GtNode TypeEnum(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/Object EnumType = ParsedTree.ConstValue;
 		return Gamma.Generator.CreateConstNode(Gamma.Context.GuessType(EnumType), ParsedTree, EnumType);
 	}
 	
 	public static GtSyntaxTree ParseCaseBlock(GtNameSpace ParentNameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree PrevTree = null;
 		/*local*/GtNameSpace NameSpace = new GtNameSpace(ParentNameSpace.Context, ParentNameSpace);
 		/*local*/boolean IsCaseBlock = TokenContext.MatchToken("{"); // case EXPR : {}
 		while(TokenContext.SkipEmptyStatement()) {
 			if(TokenContext.MatchToken("case")) {
 				TokenContext.CurrentPosition -= 1;
 				break;
 			}
 			if(TokenContext.MatchToken("default")) {
 				TokenContext.CurrentPosition -= 1;
 				break;
 			}
 			if(TokenContext.MatchToken("}")) {
 				if(!IsCaseBlock) {
 					TokenContext.CurrentPosition -= 1;
 				}
 				break;
 			}
 			/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 			/*local*/GtSyntaxTree CurrentTree = GtStatic.ParseExpression(NameSpace, TokenContext);
 			if(GtStatic.IsEmptyOrError(CurrentTree)) {
 				return CurrentTree;
 			}
 			CurrentTree.SetAnnotation(Annotation);
 			PrevTree = GtStatic.LinkTree(PrevTree, CurrentTree);
 		}
 		if(PrevTree == null) {
 			return TokenContext.ParsePattern(NameSpace, "$Empty$", Required);
 		}
 		return GtStatic.TreeHead(PrevTree);
 	}
 
 	public static GtSyntaxTree ParseSwitch(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree SwitchTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("switch"), null);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		SwitchTree.SetMatchedPatternAt(SwitchCaseCondExpr, NameSpace, TokenContext, "$Expression$", Required);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ")", Required);
 		SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "{", Required);
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 
 		/*local*/int CaseIndex = SwitchCaseCaseIndex;
 		while(!SwitchTree.IsEmptyOrError() && !TokenContext.MatchToken("}")) {
 			if(TokenContext.MatchToken("case")) {
 				SwitchTree.SetMatchedPatternAt(CaseIndex, NameSpace, TokenContext, "$Expression$", Required);
 				SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 				SwitchTree.SetMatchedPatternAt(CaseIndex + 1, NameSpace, TokenContext, "$CaseBlock$", Required);
 				CaseIndex += 2;
 				continue;
 			}
 			if(TokenContext.MatchToken("default")) {
 				SwitchTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ":", Required);
 				SwitchTree.SetMatchedPatternAt(SwitchCaseDefaultBlock, NameSpace, TokenContext, "$CaseBlock$", Required);
 			}
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return SwitchTree;
 	}
 
 	public static GtNode TypeSwitch(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtNode CondNode = ParsedTree.TypeCheckNodeAt(IfCond, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		/*local*/GtNode DefaultNode = null;
 		if(ParsedTree.HasNodeAt(SwitchCaseDefaultBlock)) {
 			DefaultNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(SwitchCaseDefaultBlock), ContextType);
 		}
 		/*local*/GtNode Node = Gamma.Generator.CreateSwitchNode(Gamma.VoidType/*FIXME*/, ParsedTree, CondNode, DefaultNode);
 		/*local*/int CaseIndex = SwitchCaseCaseIndex;
 		while(CaseIndex < ParsedTree.TreeList.size()) {
 			/*local*/GtNode CaseExpr  = ParsedTree.TypeCheckNodeAt(CaseIndex, Gamma, CondNode.Type, DefaultTypeCheckPolicy);
 			/*local*/GtNode CaseBlock = null;
 			if(ParsedTree.HasNodeAt(CaseIndex+1)) {
 				CaseBlock = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(CaseIndex+1), ContextType);
 			}
 			Node.Append(CaseExpr);
 			Node.Append(CaseBlock);
 			CaseIndex += 2;
 		}
 		return Node;
 	}
 
 	// const decl
 	public static GtSyntaxTree ParseConstDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ConstDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("const"), null);
 		/*local*/GtSyntaxTree ClassNameTree = TokenContext.ParsePattern(NameSpace, "$Type$", Optional);
 		/*local*/GtType ConstClass = null;
 		if(ClassNameTree != null) {
 			ConstDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ".", Required);
 			if(!ConstDeclTree.IsEmptyOrError()) {
 				ConstDeclTree.SetSyntaxTreeAt(ConstDeclClassIndex, ClassNameTree);
 				ConstClass = ConstDeclTree.GetParsedType();
 			}
 		}
 		ConstDeclTree.SetMatchedPatternAt(ConstDeclNameIndex, NameSpace, TokenContext, "$Variable$", Required);
 		ConstDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "=", Required);
 		ConstDeclTree.SetMatchedPatternAt(ConstDeclValueIndex, NameSpace, TokenContext, "$Expression$", Required);
 
 		if(!ConstDeclTree.IsEmptyOrError()) {
 			/*local*/String ConstName = ConstDeclTree.GetSyntaxTreeAt(ConstDeclNameIndex).KeyToken.ParsedText;
 			if(ConstClass != null) {
 				ConstName = ClassSymbol(ConstClass, ConstName);
 			}
 			/*local*/Object ConstValue = null;
 			if(ConstDeclTree.GetSyntaxTreeAt(ConstDeclValueIndex).Pattern.PatternName.equals("$Const$")) {
 				ConstValue = ConstDeclTree.GetSyntaxTreeAt(ConstDeclValueIndex).ConstValue;
 			}
 			if(ConstValue == null) {
 
 			}
 			if(NameSpace.GetSymbol(ConstName) != null) {
 				NameSpace.ReportOverrideName(ConstDeclTree.KeyToken, ConstClass, ConstName);
 			}
 			NameSpace.SetSymbol(ConstName, ConstValue);
 		}
 		return ConstDeclTree;
 	}
 
 	public static GtNode TypeConstDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtSyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(ConstDeclNameIndex);
 		/*local*/GtSyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(ConstDeclValueIndex);
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		/*local*/GtNode ValueNode = Gamma.TypeCheck(ValueTree, Gamma.AnyType, DefaultTypeCheckPolicy);
 		if(!(ValueNode instanceof ConstNode)) {
 			return Gamma.CreateSyntaxErrorNode(ParsedTree, "definition of variable " + VariableName + " is not constant");
 		}
 		/*local*/ConstNode CNode = (/*cast*/ConstNode) ValueNode;
 		Gamma.NameSpace.SetSymbol(VariableName, CNode.ConstValue);
 		return Gamma.Generator.CreateEmptyNode(ContextType);
 	}
 
 	// FuncDecl
 	public static GtSyntaxTree ParseFuncName(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		if(Token != GtTokenContext.NullToken) {
 			/*local*/char ch = LibGreenTea.CharAt(Token.ParsedText, 0);
 			if(ch != '.') {
 				return new GtSyntaxTree(Pattern, NameSpace, Token, Token.ParsedText);
 			}
 		}
 		return null;
 	}
 
 	public static GtSyntaxTree ParseFuncDecl(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree FuncDeclTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetToken(), null);
 		if(LeftTree == null) {
 			FuncDeclTree.SetMatchedPatternAt(FuncDeclReturnType, NameSpace, TokenContext, "$Type$", Required);
 		}
 		else {
 			FuncDeclTree.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		}
 		FuncDeclTree.SetMatchedPatternAt(FuncDeclName, NameSpace, TokenContext, "$FuncName$", Required);
 		FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 		/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 		/*local*/int ParamBase = FuncDeclParam;
 		while(!FuncDeclTree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 			TokenContext.SkipIndent();
 			if(ParamBase != FuncDeclParam) {
 				FuncDeclTree.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 				TokenContext.SkipIndent();
 			}
 			FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 			FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				FuncDeclTree.SetMatchedPatternAt(ParamBase + VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 			ParamBase += 3;
 		}
 		TokenContext.SkipIndent();
 		if(TokenContext.MatchToken("as")) {
 			/*local*/GtToken Token = TokenContext.Next();
 			FuncDeclTree.ConstValue = Token.ParsedText;
 		}
 		else {
 			FuncDeclTree.SetMatchedPatternAt(FuncDeclBlock, NameSpace, TokenContext, "$Block$", Optional);
 		}
 		TokenContext.ParseFlag = ParseFlag;
 		return FuncDeclTree;
 	}
 
 	public static GtNode TypeFuncDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/int FuncFlag = Gamma.Generator.ParseFuncFlag(0, ParsedTree.Annotation);
 		/*local*/String FuncName = (/*cast*/String)ParsedTree.GetSyntaxTreeAt(FuncDeclName).ConstValue;
 		Gamma = new GtTypeEnv(ParsedTree.NameSpace);  // creation of new type environment
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		/*local*/GtType ReturnType = ParsedTree.GetSyntaxTreeAt(FuncDeclReturnType).GetParsedType();
 		TypeList.add(ReturnType);
 		/*local*/ArrayList<String> ParamNameList = new ArrayList<String>();
 		/*local*/GtType RecvType = null;
 		if(ParsedTree.HasNodeAt(FuncDeclClass)) {
 			FuncFlag |= VirtualFunc;
 			RecvType = ParsedTree.GetSyntaxTreeAt(FuncDeclClass).GetParsedType();
 			TypeList.add(RecvType);
 			Gamma.AppendRecv(RecvType);
 			ParamNameList.add(Gamma.Generator.GetRecvName());
 		}
 		/*local*/int TreeIndex = FuncDeclParam;
 		while(TreeIndex < ParsedTree.TreeList.size()) {
 			/*local*/GtType ParamType = ParsedTree.GetSyntaxTreeAt(TreeIndex).GetParsedType();
 			/*local*/String ParamName = ParsedTree.GetSyntaxTreeAt(TreeIndex+1).KeyToken.ParsedText;
 			TypeList.add(ParamType);
 			ParamNameList.add(NativeVariableName(ParamName, ParamNameList.size()));
 			Gamma.AppendDeclaredVariable(0, ParamType, ParamName);
 			TreeIndex += 3;
 		}
 		/*local*/GtFunc DefinedFunc = null;
 		if(FuncName.equals("converter")) {
 			DefinedFunc = GreenTeaGrammar.CreateCoercionFunc(Gamma, ParsedTree, FuncFlag, 0, TypeList);
 		}
 		else {
 			DefinedFunc = GreenTeaGrammar.CreateFunc(Gamma, ParsedTree, FuncFlag, FuncName, 0, TypeList);
 		}
 		if(ParsedTree.ConstValue instanceof String) {
 			DefinedFunc.SetNativeMacro((/*cast*/String)ParsedTree.ConstValue);
 		}
 		else if(ParsedTree.HasNodeAt(FuncDeclBlock)) {
 			Gamma.Func = DefinedFunc;
 			/*local*/GtNode BodyNode = Gamma.TypeBlock(ParsedTree.GetSyntaxTreeAt(FuncDeclBlock), ReturnType);
 			Gamma.Generator.GenerateFunc(DefinedFunc, ParamNameList, BodyNode);
 			if(FuncName.equals("main")) {
 				Gamma.Generator.InvokeMainFunc(DefinedFunc.GetNativeFuncName());
 			}
 
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	private static GtFunc CreateCoercionFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int FuncFlag, int BaseIndex, ArrayList<GtType> TypeList) {
 		/*local*/GtType ToType = TypeList.get(0);
 		/*local*/GtType FromType = TypeList.get(1);
 		/*local*/GtFunc Func = ParsedTree.NameSpace.GetCoercionFunc(FromType, ToType, false);
 		if(Func != null) {
 			Gamma.Context.ReportError(WarningLevel, ParsedTree.KeyToken, "already defined: " + FromType + " to " + ToType);
 		}
 		Func = Gamma.Generator.CreateFunc(FuncFlag, "to" + ToType.ShortClassName, BaseIndex, TypeList);
 		ParsedTree.NameSpace.SetCoercionFunc(FromType, ToType, Func);
 		return Func;
 	}
 
 	private static GtFunc CreateFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int FuncFlag, String FuncName, int BaseIndex, ArrayList<GtType> TypeList) {
 		/*local*/GtType RecvType = (TypeList.size() > 1) ? TypeList.get(1) : Gamma.VoidType;
 		/*local*/GtFunc Func = ParsedTree.NameSpace.GetFuncParam(FuncName, 0, LibGreenTea.CompactTypeList(BaseIndex, TypeList));
 		if(Func != null && Func.IsAbstract()) {
 			return Func;
 		}
 		if(Func == null) {
 			Func = Gamma.Generator.CreateFunc(FuncFlag, FuncName, BaseIndex, TypeList);
 		}
 		if(FuncName.equals("constructor")) {
 			ParsedTree.NameSpace.AppendConstructor(RecvType, Func);
 		}
 		else {
 			if(LibGreenTea.IsLetter(LibGreenTea.CharAt(Func.FuncName, 0))) {
 				ParsedTree.NameSpace.AppendFunc(Func);
 			}
 			if(RecvType != Gamma.VoidType) {
 				ParsedTree.NameSpace.AppendMethod(RecvType, Func);
 			}
 		}
 		return Func;
 	}
 
 	// ClassDecl
 	public static GtSyntaxTree ParseClassDecl(GtNameSpace NameSpace0, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree ClassDeclTree = new GtSyntaxTree(Pattern, NameSpace0, TokenContext.GetMatchedToken("class"), null);
 		ClassDeclTree.SetMatchedPatternAt(ClassDeclName, NameSpace0, TokenContext, "$FuncName$", Required); //$ClassName$ is better
 		if(TokenContext.MatchToken("extends")) {
 			ClassDeclTree.SetMatchedPatternAt(ClassDeclSuperType, NameSpace0, TokenContext, "$Type$", Required);
 		}
 		if(ClassDeclTree.IsEmptyOrError()) {
 			return ClassDeclTree;
 		}
 		// define new class
 		/*local*/GtNameSpace ClassNameSpace = new GtNameSpace(NameSpace0.Context, NameSpace0);
 		/*local*/GtSyntaxTree ClassNameTree = ClassDeclTree.GetSyntaxTreeAt(ClassDeclName);
 		/*local*/String ClassName = ClassNameTree.KeyToken.ParsedText;
 		/*local*/GtType SuperType = NameSpace0.Context.StructType;
 		if(ClassDeclTree.HasNodeAt(ClassDeclSuperType)) {
 			SuperType = ClassDeclTree.GetSyntaxTreeAt(ClassDeclSuperType).GetParsedType();
 		}
 		/*local*/int ClassFlag = 0; //Gamma.Generator.ParseClassFlag(0, ParsedTree);
 		/*local*/GtType NewType = SuperType.CreateSubType(ClassFlag, ClassName, null, null);
 		// FIXME: Obviously strange
 		/*local*/GreenTeaTopObject DefaultObject = new GreenTeaTopObject(NewType);
 		NewType.DefaultNullValue = DefaultObject;
 		ClassNameSpace.AppendTypeName(NewType);  // temporary
 		ClassDeclTree.ConstValue = NewType;
 		ClassNameTree.ConstValue = NewType;
 
 		if(TokenContext.MatchToken("{")) {
 			/*local*/int ParseFlag = TokenContext.SetBackTrack(false);
 			while(!ClassDeclTree.IsEmptyOrError() && !TokenContext.MatchToken("}")) {
 				/*local*/GtMap Annotation = TokenContext.SkipAndGetAnnotation(true);
 				if(TokenContext.MatchToken("}")) {
 					break;
 				}
 				if(TokenContext.MatchToken("constructor")) {
 					/*local*/GtSyntaxTree FuncDecl = TokenContext.ParsePatternAfter(ClassNameSpace, ClassNameTree, "$Constructor$", Required);
 					if(!FuncDecl.IsEmptyOrError()) {
 						FuncDecl.SetAnnotation(Annotation);
 						FuncDecl.SetSyntaxTreeAt(FuncDeclClass, ClassNameTree);
 						ClassDeclTree.AppendParsedTree(FuncDecl);
 						continue;
 					}
 				}
 				/*local*/GtSyntaxTree TypeDecl = TokenContext.ParsePattern(ClassNameSpace, "$Type$", Required);
 				if(TypeDecl != null && !TypeDecl.IsEmptyOrError()) {
 					/*local*/GtSyntaxTree FuncDecl = TokenContext.ParsePatternAfter(ClassNameSpace, TypeDecl, "$FuncDecl$", Optional);
 					if(FuncDecl != null) {
 						FuncDecl.SetAnnotation(Annotation);
 						FuncDecl.SetSyntaxTreeAt(FuncDeclClass, ClassNameTree);
 						ClassDeclTree.AppendParsedTree(FuncDecl);
 						continue;
 					}
 					/*local*/GtSyntaxTree VarDecl = TokenContext.ParsePatternAfter(ClassNameSpace, TypeDecl, "$VarDecl$", Required);
 					if(VarDecl != null) {
 						VarDecl = GtStatic.TreeHead(VarDecl);
 						while(VarDecl != null) {
 							/*local*/GtSyntaxTree NextTree = VarDecl.NextTree;
 							VarDecl.NextTree = null;
 							VarDecl.SetAnnotation(Annotation);
 							ClassDeclTree.AppendParsedTree(VarDecl);
 							VarDecl = NextTree;
 						}
 					}
 				}
 			}
 			TokenContext.ParseFlag = ParseFlag;
 		}
 		/*local*/int TreeIndex = ClassDeclFieldStartIndex;
 		/*local*/ArrayList<GtSyntaxTree> VarDeclList = new ArrayList<GtSyntaxTree>();
 		/*local*/ArrayList<GtSyntaxTree> ConstructorList = new ArrayList<GtSyntaxTree>();
 		/*local*/ArrayList<GtSyntaxTree> MethodList = new ArrayList<GtSyntaxTree>();
 		VarDeclList.add(ClassDeclTree.GetSyntaxTreeAt(ClassDeclSuperType));
 		VarDeclList.add(ClassDeclTree.GetSyntaxTreeAt(ClassDeclName));
 		while(TreeIndex < ClassDeclTree.TreeList.size()) {
 			/*local*/GtSyntaxTree FieldTree = ClassDeclTree.GetSyntaxTreeAt(TreeIndex);
 			if(FieldTree.Pattern.PatternName.equals("$VarDecl$")) {
 				VarDeclList.add(FieldTree);
 			}
 			else if(FieldTree.Pattern.PatternName.equals("$FuncDecl$")) {
 				ConstructorList.add(FieldTree);
 			}
 			else if(FieldTree.Pattern.PatternName.equals("$Constructor$")) {
 				MethodList.add(FieldTree);
 			}
 			TreeIndex = TreeIndex + 1;
 		}
 		ClassDeclTree.TreeList = VarDeclList;
 		TreeIndex = 0;
 		while(TreeIndex < ConstructorList.size()) {
 			ClassDeclTree.AppendParsedTree(ConstructorList.get(TreeIndex));
 			TreeIndex = TreeIndex + 1;
 		}
 		while(TreeIndex < MethodList.size()) {
 			ClassDeclTree.AppendParsedTree(MethodList.get(TreeIndex));
 			TreeIndex = TreeIndex + 1;
 		}
 		NameSpace0.AppendTypeName(NewType);
 		return ClassDeclTree;
 	}
 
 	public static GtNode TypeClassDecl(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/GtType DefinedType = ParsedTree.GetParsedType();
 		/*local*/int TreeIndex = ClassDeclFieldStartIndex;
 		/*local*/GtClassField ClassField = new GtClassField(DefinedType.SuperType);
 		while(TreeIndex < ParsedTree.TreeList.size()) {
 			/*local*/GtSyntaxTree FieldTree = ParsedTree.GetSyntaxTreeAt(TreeIndex);
 			if(!FieldTree.Pattern.PatternName.equals("$VarDecl$")) {
 				break;
 			}
 			/*local*/GtNode FieldNode = ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, Gamma.VarType, OnlyConstPolicy);
 			if(FieldNode.IsError()) {
 				return FieldNode;
 			}
 			/*local*/String FieldName = FieldTree.GetSyntaxTreeAt(VarDeclName).KeyToken.ParsedText;
 			/*local*/int FieldFlag = 0;
 			/*local*/GtFieldInfo FieldInfo = ClassField.CreateField(FieldFlag, FieldNode.Type, FieldName);
 			if(FieldInfo != null) {
 				/*local*/ArrayList<GtType> ParamList = new ArrayList<GtType>();
 				ParamList.add(FieldInfo.Type);
 				ParamList.add(DefinedType);
 				FieldInfo.GetterFunc = new GtFunc(0, FieldInfo.Name, 0, ParamList);
 				Gamma.NameSpace.SetGetterFunc(DefinedType, FieldInfo.Name, FieldInfo.GetterFunc);
 				ParamList.clear();
 				ParamList.add(Gamma.VoidType);
 				ParamList.add(DefinedType);
 				ParamList.add(FieldInfo.Type);
 				FieldInfo.SetterFunc = new GtFunc(0, FieldInfo.Name, 0, ParamList);
 				Gamma.NameSpace.SetGetterFunc(DefinedType, FieldInfo.Name, FieldInfo.SetterFunc);
 				FieldInfo.InitValue = ((/*cast*/ConstNode)((/*cast*/VarNode)FieldNode).InitNode).ConstValue;
 			}
 			TreeIndex += 1;
 		}
 		DefinedType.SetClassField(ClassField);
 		Gamma.Generator.GenerateClassField(DefinedType, ClassField);
 		while(TreeIndex < ParsedTree.TreeList.size()) {
 			/*local*/GtSyntaxTree FieldTree = ParsedTree.GetSyntaxTreeAt(TreeIndex);
 			if(!FieldTree.Pattern.PatternName.equals("$FuncDecl$")) {
 				break;
 			}
 			ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, Gamma.VarType, OnlyConstPolicy);
 			TreeIndex += 1;
 		}
 		while(TreeIndex < ParsedTree.TreeList.size()) {
 			/*local*/GtSyntaxTree FieldTree = ParsedTree.GetSyntaxTreeAt(TreeIndex);
 			if(!FieldTree.Pattern.PatternName.equals("$Constructor$")) {
 				break;
 			}
 			ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, DefinedType, OnlyConstPolicy);
 			TreeIndex += 1;
 		}
 		return Gamma.Generator.CreateEmptyNode(Gamma.VoidType);
 	}
 
 	public static GtNode TypeConstructor(GtTypeEnv Gamma, GtSyntaxTree ConstructorTree, GtType ContextType) {
 		/*local*/GtNameSpace NameSpace = Gamma.NameSpace;
 		/*local*/GtType DefinedType = ConstructorTree.GetSyntaxTreeAt(FuncDeclReturnType).GetParsedType();
 		/*local*/GtSyntaxTree BlockTree = ConstructorTree.GetSyntaxTreeAt(FuncDeclBlock);
 		/*local*/GtSyntaxTree TailTree = BlockTree;
 		while(TailTree.NextTree != null) {
 			TailTree = TailTree.NextTree;
 		}
 		/*local*/GtSyntaxTree ThisTree = new GtSyntaxTree(NameSpace.GetPattern("$Variable$"), NameSpace, new GtToken(Gamma.Generator.GetRecvName(), 0), null);
 		/*local*/GtSyntaxTree ReturnTree = new GtSyntaxTree(NameSpace.GetPattern("return"), NameSpace, new GtToken("return", 0), null);
 		ReturnTree.SetSyntaxTreeAt(ReturnExpr, ThisTree);
 		if(BlockTree.IsEmpty()) {
 			ConstructorTree.SetSyntaxTreeAt(FuncDeclBlock, ReturnTree);
 		}
 		else {
 			GtStatic.LinkTree(TailTree, ReturnTree);
 		}
 		if(ConstructorTree.HasNodeAt(FuncDeclBlock)) {
 			/*local*/GtPolyFunc Func = Gamma.NameSpace.GetConstructorFunc(DefinedType.SuperType);
 			ConstructorTree.GetSyntaxTreeAt(FuncDeclBlock).NameSpace.SetSymbol("super", Func);
 		}
 		return GreenTeaGrammar.TypeFuncDecl(Gamma, ConstructorTree, ContextType);
 	}
 
 	// constructor
 	public static GtSyntaxTree ParseConstructor(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		LibGreenTea.Assert(LeftTree != null);
 		/*local*/GtSyntaxTree ConstructorTreeDecl = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetBeforeToken(), null);
 		ConstructorTreeDecl.SetSyntaxTreeAt(FuncDeclName, new GtSyntaxTree(null, NameSpace, ConstructorTreeDecl.KeyToken, "constructor"));
 		ConstructorTreeDecl.SetSyntaxTreeAt(FuncDeclReturnType, LeftTree);
 		ConstructorTreeDecl.SetSyntaxTreeAt(FuncDeclClass, LeftTree);
 		ConstructorTreeDecl.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, "(", Required);
 
 		/*local*/int ParseFlag = TokenContext.SetBackTrack(false);  // disabled
 		/*local*/int ParamBase = FuncDeclParam;
 		while(!ConstructorTreeDecl.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 			if(ParamBase > FuncDeclParam) {
 				ConstructorTreeDecl.SetMatchedTokenAt(NoWhere, NameSpace, TokenContext, ",", Required);
 			}
 			ConstructorTreeDecl.SetMatchedPatternAt(ParamBase + VarDeclType, NameSpace, TokenContext, "$Type$", Required);
 			ConstructorTreeDecl.SetMatchedPatternAt(ParamBase + VarDeclName, NameSpace, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				ConstructorTreeDecl.SetMatchedPatternAt(ParamBase + VarDeclValue, NameSpace, TokenContext, "$Expression$", Required);
 			}
 			ParamBase += 3;
 		}
 		ConstructorTreeDecl.SetMatchedPatternAt(FuncDeclBlock, NameSpace, TokenContext, "$Block$", Required);
 		TokenContext.ParseFlag = ParseFlag;
 		return ConstructorTreeDecl;
 	}
 
 	// shell grammar
 	private static boolean IsUnixCommand(String cmd) {
 //ifdef  JAVA
 		String[] path = System.getenv("PATH").split(":");
 		int i = 0;
 		while(i < path.length) {
 			if(LibGreenTea.HasFile(path[i] + "/" + cmd)) {
 				return true;
 			}
 			i = i + 1;
 		}
 //endif VAJA
 		return false;
 	}
 
 	public static int SymbolShellToken(GtTokenContext TokenContext, String SourceText, int pos) {
 		/*local*/boolean ShellMode = false;
 		/*local*/int start = pos;
 		if(TokenContext.SourceList.size() > 0) {
 			/*local*/GtToken PrevToken = TokenContext.SourceList.get(TokenContext.SourceList.size() - 1);
 			if(PrevToken != null && PrevToken.PresetPattern != null &&
 				PrevToken.PresetPattern.PatternName.equals("$ShellExpression$")) {
 				ShellMode = true;
 			}
 		}
 
 		while(pos < SourceText.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 			// a-zA-Z0-9_-
 			if(LibGreenTea.IsLetter(ch)) {
 			}
 			else if(LibGreenTea.IsDigit(ch)) {
 			}
 			else if(ch == '_') {
 			}
 			else if(ShellMode && (ch == '-' || ch == '/')) {
 			}
 			else {
 				break;
 			}
 			pos += 1;
 		}
 		if(start == pos) {
 			return NoMatch;
 		}
 		/*local*/String Symbol = SourceText.substring(start, pos);
 
 		/*local*/int i = 0;
 		while(i < ShellGrammarReservedKeywords.length) {
 			/*local*/String Keyword = ShellGrammarReservedKeywords[i];
 			if(Symbol.equals(Keyword)) {
 				return GtStatic.NoMatch;
 			}
 			i = i + 1;
 		}
 		if(Symbol.startsWith("/") || Symbol.startsWith("-")) {
 			if(Symbol.startsWith("//")) { // One-Line Comment
 				return GtStatic.NoMatch;
 			}
 			TokenContext.AddNewToken(Symbol, 0, "$StringLiteral$");
 			return pos;
 		}
 
 		if(GreenTeaGrammar.IsUnixCommand(Symbol)) {
 			TokenContext.AddNewToken(Symbol, 0, "$ShellExpression$");
 			return pos;
 		}
 		return GtStatic.NoMatch;
 	}
 
 	public final static GtSyntaxTree ParseShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 		while(!GtStatic.IsEmptyOrError(NewTree) && !TokenContext.MatchToken(";")) {
 			/*local*/GtSyntaxTree Tree = null;
 			if(TokenContext.GetToken().IsDelim() || TokenContext.GetToken().IsIndent()) {
 				break;
 			}
 			if(TokenContext.MatchToken("$ShellExpression$")) {
 				// FIXME
 			}
 			if(Tree == null) {
 				Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Optional);
 			}
 			NewTree.AppendParsedTree(Tree);
 		}
 		return NewTree;
 	}
 
 	public static GtNode TypeShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/CommandNode Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 		/*local*/GtNode HeadNode = Node;
 		/*local*/int i = 0;
 		/*local*/String Command = ParsedTree.KeyToken.ParsedText;
 		/*local*/GtNode ThisNode = Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Command);
 		Node.Append(ThisNode);
 		while(i < ListSize(ParsedTree.TreeList)) {
 			/*local*/GtNode ExprNode = ParsedTree.TypeCheckNodeAt(i, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 			if(ExprNode instanceof ConstNode) {
 				/*local*/ConstNode CNode = (/*cast*/ConstNode) ExprNode;
 				if(CNode.ConstValue instanceof String) {
 					/*local*/String Val = (/*cast*/String) CNode.ConstValue;
 					if(Val.equals("|")) {
 						LibGreenTea.DebugP("PIPE");
 						/*local*/CommandNode PrevNode = Node;
 						Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 						PrevNode.PipedNextNode = Node;
 					}
 				}
 			}
 			Node.Append(ExprNode);
 			i = i + 1;
 		}
 		return HeadNode;
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		// Define Constants
 		NameSpace.SetSymbol("true", true);
 		NameSpace.SetSymbol("false", false);
 
 		NameSpace.DefineTokenFunc(" \t", FunctionA(this, "WhiteSpaceToken"));
 		NameSpace.DefineTokenFunc("\n",  FunctionA(this, "IndentToken"));
 		NameSpace.DefineTokenFunc("{}()[]<>.,:;+-*/%=&|!@~^", FunctionA(this, "OperatorToken"));
 		NameSpace.DefineTokenFunc("/", FunctionA(this, "CommentToken"));  // overloading
 		NameSpace.DefineTokenFunc("Aa", FunctionA(this, "SymbolToken"));
 		NameSpace.DefineTokenFunc("Aa-/", FunctionA(this, "SymbolShellToken")); // overloading
 
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken"));
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken_StringInterpolation"));
 		NameSpace.DefineTokenFunc("1",  FunctionA(this, "NumberLiteralToken"));
 //#ifdef JAVA
 		GtDelegateMatch ParseUnary     = FunctionB(this, "ParseUnary");
 		GtDelegateType  TypeUnary      = FunctionC(this, "TypeUnary");
 		GtDelegateMatch ParseBinary    = FunctionB(this, "ParseBinary");
 		GtDelegateType  TypeBinary     = FunctionC(this, "TypeBinary");
 		GtDelegateType  TypeConst      = FunctionC(this, "TypeConst");
 		GtDelegateType  TypeBlock      = FunctionC(this, "TypeBlock");
 //endif VAJA
 		NameSpace.AppendSyntax("+", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("-", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("~", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("! not", ParseUnary, TypeUnary);
 		NameSpace.AppendSyntax("++ --", FunctionB(this, "ParseIncl"), FunctionC(this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("*", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("/", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("% mod", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("+", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("-", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("<", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("<=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax(">", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax(">=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("==", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("!=", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("<<", BinaryOperator | Precedence_CStyleSHIFT, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax(">>", BinaryOperator | Precedence_CStyleSHIFT, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("&", BinaryOperator | Precedence_CStyleBITAND, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("|", BinaryOperator | Precedence_CStyleBITOR, ParseBinary, TypeBinary);
 		NameSpace.AppendExtendedSyntax("^", BinaryOperator | Precedence_CStyleBITXOR, ParseBinary, TypeBinary);
 
 		NameSpace.AppendExtendedSyntax("=", BinaryOperator | Precedence_CStyleAssign | LeftJoin, ParseBinary, FunctionC(this, "TypeAssign"));
 		NameSpace.AppendExtendedSyntax("+= -= *= /= %= <<= >>= & | ^=", BinaryOperator | Precedence_CStyleAssign, ParseBinary, FunctionC(this, "TypeSelfAssign"));
 		NameSpace.AppendExtendedSyntax("++ --", 0, FunctionB(this, "ParseIncl"), FunctionC(this, "TypeIncl"));
 
 		NameSpace.AppendExtendedSyntax("&& and", BinaryOperator | Precedence_CStyleAND, ParseBinary, FunctionC(this, "TypeAnd"));
 		NameSpace.AppendExtendedSyntax("|| or", BinaryOperator | Precedence_CStyleOR, ParseBinary, FunctionC(this, "TypeOr"));
 		NameSpace.AppendExtendedSyntax("<: instanceof", BinaryOperator | Precedence_CStyleBITXOR, ParseBinary, FunctionC(this, "TypeInstanceOf"));
 
 		NameSpace.AppendExtendedSyntax("?", 0, FunctionB(this, "ParseTrinary"), FunctionC(this, "TypeTrinary"));
 
 		NameSpace.AppendSyntax("$Empty$", FunctionB(this, "ParseEmpty"), FunctionC(this, "TypeEmpty"));
 		NameSpace.AppendSyntax("$Symbol$", FunctionB(this, "ParseSymbol"), null);
 		NameSpace.AppendSyntax("$Type$", FunctionB(this, "ParseType"), TypeConst);
 		NameSpace.AppendSyntax("$Variable$", FunctionB(this, "ParseVariable"), FunctionC(this, "TypeVariable"));
 		NameSpace.AppendSyntax("$Const$", FunctionB(this, "ParseConst"), TypeConst);
 		NameSpace.AppendSyntax("$StringLiteral$", FunctionB(this, "ParseStringLiteral"), TypeConst);
 		NameSpace.AppendSyntax("$IntegerLiteral$", FunctionB(this, "ParseIntegerLiteral"), TypeConst);
 
 		NameSpace.AppendSyntax("$ShellExpression$", FunctionB(this, "ParseShell"), FunctionC(this, "TypeShell"));
 
 		NameSpace.AppendExtendedSyntax(".", 0, FunctionB(this, "ParseGetter"), FunctionC(this, "TypeGetter"));
 		NameSpace.AppendSyntax("(", FunctionB(this, "ParseGroup"), FunctionC(this, "TypeGroup"));
 		NameSpace.AppendSyntax("(", FunctionB(this, "ParseCast"), FunctionC(this, "TypeCast"));
 		NameSpace.AppendExtendedSyntax("(", 0, FunctionB(this, "ParseApply"), FunctionC(this, "TypeApply"));
 		NameSpace.AppendExtendedSyntax("[", 0, FunctionB(this, "ParseIndexer"), FunctionC(this, "TypeIndexer"));
 		NameSpace.AppendSyntax("|", FunctionB(this, "ParseSizeOf"), FunctionC(this, "TypeSizeOf"));
 
 		NameSpace.AppendSyntax("$Block$", FunctionB(this, "ParseBlock"), TypeBlock);
 		NameSpace.AppendSyntax("$Statement$", FunctionB(this, "ParseStatement"), TypeBlock);
 		NameSpace.AppendSyntax("$Expression$", FunctionB(this, "ParseExpression"), TypeBlock);
 
 		NameSpace.AppendSyntax("$FuncName$", FunctionB(this, "ParseFuncName"), TypeConst);
 		NameSpace.AppendSyntax("$FuncDecl$", FunctionB(this, "ParseFuncDecl"), FunctionC(this, "TypeFuncDecl"));
 		NameSpace.AppendSyntax("$VarDecl$",  FunctionB(this, "ParseVarDecl"), FunctionC(this, "TypeVarDecl"));
 
 		NameSpace.AppendSyntax("null", FunctionB(this, "ParseNull"), FunctionC(this, "TypeNull"));
 		NameSpace.AppendSyntax("defined", FunctionB(this, "ParseDefined"), FunctionC(this, "TypeDefined"));
 		NameSpace.AppendSyntax("require", FunctionB(this, "ParseRequire"), FunctionC(this, "TypeRequire"));
 		NameSpace.AppendSyntax("import", FunctionB(this, "ParseImport"), FunctionC(this, "TypeImport"));
 		
 		NameSpace.AppendSyntax("if", FunctionB(this, "ParseIf"), FunctionC(this, "TypeIf"));
 		NameSpace.AppendSyntax("while", FunctionB(this, "ParseWhile"), FunctionC(this, "TypeWhile"));
 		NameSpace.AppendSyntax("continue", FunctionB(this, "ParseContinue"), FunctionC(this, "TypeContinue"));
 		NameSpace.AppendSyntax("break", FunctionB(this, "ParseBreak"), FunctionC(this, "TypeBreak"));
 		NameSpace.AppendSyntax("return", FunctionB(this, "ParseReturn"), FunctionC(this, "TypeReturn"));
 		NameSpace.AppendSyntax("let const", FunctionB(this, "ParseConstDecl"), FunctionC(this, "TypeConstDecl"));
 
 		NameSpace.AppendSyntax("try", FunctionB(this, "ParseTry"), FunctionC(this, "TypeTry"));
 		NameSpace.AppendSyntax("throw", FunctionB(this, "ParseThrow"), FunctionC(this, "TypeThrow"));
 		
 		NameSpace.AppendSyntax("class", FunctionB(this, "ParseClassDecl"), FunctionC(this, "TypeClassDecl"));
 		NameSpace.AppendSyntax("$Constructor$", FunctionB(this, "ParseConstructor"), FunctionC(this, "TypeConstructor"));
 		NameSpace.AppendSyntax("super", FunctionB(this, "ParseSuper"), null);
 		NameSpace.AppendSyntax("new", FunctionB(this, "ParseNew"), FunctionC(this, "TypeApply"));
 
 		NameSpace.AppendSyntax("enum", FunctionB(this, "ParseEnum"), FunctionC(this, "TypeEnum"));
 		NameSpace.AppendSyntax("switch", FunctionB(this, "ParseSwitch"), FunctionC(this, "TypeSwitch"));
 		NameSpace.AppendSyntax("$CaseBlock$", FunctionB(this, "ParseCaseBlock"), TypeBlock);
 	}
 }
 
 final class GtStat {
 	/*field*/public long MatchCount;
 	/*field*/public long BacktrackCount;  // To count how many times backtracks happen.
 
 	GtStat/*constructor*/() {
 		this.MatchCount     = 0;
 		this.BacktrackCount = 0;
 	}
 }
 
 final class GtClassContext extends GtStatic {
 	/*field*/public final  GtGenerator   Generator;
 	/*field*/public final  GtNameSpace		   RootNameSpace;
 	/*field*/public GtNameSpace		           TopLevelNameSpace;
 
 	// basic class
 	/*field*/public final GtType		VoidType;
 	/*field*/public final GtType		BooleanType;
 	/*field*/public final GtType		IntType;
 	/*field*/public final GtType		StringType;
 	/*field*/public final GtType		AnyType;
 	/*field*/public final GtType		ArrayType;
 	/*field*/public final GtType		FuncType;
 
 	/*field*/public final GtType		TopType;
 	/*field*/public final GtType		EnumType;
 	/*field*/public final GtType		StructType;
 	/*field*/public final GtType		VarType;
 
 	/*field*/public final GtType		TypeType;
 	/*field*/public GtType		        PolyFuncType;
 
 	/*field*/public final  GtMap               SourceMap;
 	/*field*/public final  ArrayList<String>   SourceList;
 	/*field*/public final  GtMap			   ClassNameMap;
 	/*field*/public final  GtMap               UniqueFuncMap;
 	/*field*/public int ClassCount;
 	/*field*/public int FuncCount;
 	/*field*/public final GtStat Stat;
 	/*field*/public ArrayList<String>    ReportedErrorList;
 	/*filed*/private boolean NoErrorReport;
 
 	GtClassContext/*constructor*/(GtGrammar Grammar, GtGenerator Generator) {
 		this.Generator    = Generator;
 		this.Generator.Context = this;
 		this.SourceMap     = new GtMap();
 		this.SourceList    = new ArrayList<String>();
 		this.ClassNameMap  = new GtMap();
 		this.UniqueFuncMap = new GtMap();
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.ClassCount = 0;
 		this.FuncCount = 0;
 		this.Stat = new GtStat();
 		this.NoErrorReport = false;
 		this.ReportedErrorList = new ArrayList<String>();
 
 		this.TopType     = new GtType(this, 0, "top", null, null);               //  unregistered
 		this.StructType  = this.TopType.CreateSubType(0, "record", null, null);  //  unregistered
 		this.EnumType    = this.TopType.CreateSubType(EnumClass, "enum", null, null);    //  unregistered
 
 		this.VoidType    = this.RootNameSpace.AppendTypeName(new GtType(this, NativeClass, "void", null, Void.class));
 		this.BooleanType = this.RootNameSpace.AppendTypeName(new GtType(this, NativeClass, "boolean", false, Boolean.class));
 		this.IntType     = this.RootNameSpace.AppendTypeName(new GtType(this, NativeClass, "int", 0L, Long.class));
 		this.StringType  = this.RootNameSpace.AppendTypeName(new GtType(this, NativeClass, "String", "", String.class));
 		this.VarType     = this.RootNameSpace.AppendTypeName(new GtType(this, 0, "var", null, null));
 		this.AnyType     = this.RootNameSpace.AppendTypeName(new GtType(this, DynamicClass, "any", null, null));
 		this.TypeType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Type", null, null));
 		this.ArrayType   = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Array", null, null));
 		this.FuncType    = this.RootNameSpace.AppendTypeName(this.TopType.CreateSubType(0, "Func", null, null));
 
 		this.ArrayType.TypeParams = new GtType[1];
 		this.ArrayType.TypeParams[0] = this.AnyType;
 		this.FuncType.TypeParams = new GtType[1];
 		this.FuncType.TypeParams[0] = this.AnyType;
 
 //ifdef JAVA
 		this.ClassNameMap.put("java.lang.Void",    this.VoidType);
 		this.ClassNameMap.put("java.lang.Boolean", this.BooleanType);
 		this.ClassNameMap.put("java.lang.Integer", this.IntType);
 		this.ClassNameMap.put("java.lang.Long",    this.IntType);
 		this.ClassNameMap.put("java.lang.Short",   this.IntType);
 		this.ClassNameMap.put("java.lang.String",  this.StringType);
 //endif VAJA
 		Grammar.LoadTo(this.RootNameSpace);
 		this.TopLevelNameSpace = new GtNameSpace(this, this.RootNameSpace);
 		this.Generator.InitContext(this);
 	}
 
 	public void LoadGrammar(GtGrammar Grammar) {
 		Grammar.LoadTo(this.TopLevelNameSpace);
 	}
 
 	public final GtType GuessType (Object Value) {
 		if(Value instanceof GtFunc) {
 			return ((/*cast*/GtFunc)Value).GetFuncType();
 		}
 		else if(Value instanceof GtPolyFunc) {
 			return this.FuncType;
 		}
 		else if(Value instanceof GreenTeaTopObject) {
 			return ((/*cast*/GreenTeaTopObject)Value).GreenType;
 		}
 		else if(Value instanceof GtType) {
 			return this.TypeType;
 		}
 		else {
 			return this.Generator.GetNativeType(Value);
 		}
 	}
 
 	private final String SubtypeKey(GtType FromType, GtType ToType) {
 		return FromType.GetUniqueName() + "<" + ToType.GetUniqueName();
 	}
 
 	public final boolean CheckSubType(GtType SubType, GtType SuperType) {
 		// TODO: Structual Typing database
 		return false;
 	}
 
 	public GtType GetGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList, boolean IsCreation) {
 		LibGreenTea.Assert(BaseType.IsGenericType());
 		/*local*/String MangleName = GtStatic.MangleGenericType(BaseType, BaseIdx, TypeList);
 		/*local*/GtType GenericType = (/*cast*/GtType)this.ClassNameMap.get(MangleName);
 		if(GenericType == null && IsCreation) {
 			/*local*/int i = BaseIdx;
 			/*local*/String s = BaseType.ShortClassName + "<";
 			while(i < ListSize(TypeList)) {
 				s = s + TypeList.get(i).ShortClassName;
 				i += 1;
 				if(i == ListSize(TypeList)) {
 					s = s + ">";
 				}
 				else {
 					s = s + ",";
 				}
 			}
 			GenericType = BaseType.CreateGenericType(BaseIdx, TypeList, s);
 			this.ClassNameMap.put(MangleName, GenericType);
 		}
 		return GenericType;
 	}
 
 	public GtType GetGenericType1(GtType BaseType, GtType ParamType, boolean IsCreation) {
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(ParamType);
 		return this.GetGenericType(BaseType, 0, TypeList, IsCreation);
 	}
 
 	public final boolean CheckExportableName(GtFunc Func) {
 		return true;
 	}
 
 	public final long GetFileLine(String FileName, int Line) {
 		/*local*/Integer Id = (/*cast*/Integer)this.SourceMap.get(FileName);
 		if(Id == null) {
 			this.SourceList.add(FileName);
 			Id = this.SourceList.size();
 			this.SourceMap.put(FileName, Id);
 		}
 		return LibGreenTea.JoinIntId(Id, Line);
 	}
 
 	private final String GetSourcePosition(long FileLine) {
 		/*local*/int FileId = LibGreenTea.UpperId(FileLine);
 		/*local*/int Line = LibGreenTea.LowerId(FileLine);
 		/*local*/String FileName = (FileId == 0) ? "eval" : this.SourceList.get(FileId - 1);
 		return "(" + FileName + ":" + Line + ")";
 	}
 
 	
 	public void SetNoErrorReport(boolean b) {
 		this.NoErrorReport = b;
 	}
 
 	public final void ReportError(int Level, GtToken Token, String Message) {
 		if(!Token.IsError() || !this.NoErrorReport) {
 			if(Level == ErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 				Token.ToErrorToken(Message);
 			}
 			else if(Level == TypeErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			else if(Level == WarningLevel) {
 				Message = "(warning) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			else if(Level == InfoLevel) {
 				Message = "(info) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			this.ReportedErrorList.add(Message);
 			//GtStatic.println(Message);
 		}
 	}
 
 	public final String[] GetReportedErrors() {
 		/*local*/ArrayList<String> List = this.ReportedErrorList;
 		this.ReportedErrorList = new ArrayList<String>();
 		return LibGreenTea.CompactStringList(List);
 	}
 
 	public final void ShowReportedErrors() {
 		/*local*/int i = 0;
 		/*local*/String[] Messages = this.GetReportedErrors();
 		while(i < Messages.length) {
 			LibGreenTea.println(Messages[i]);
 			i = i + 1;
 		}
 	}
 }
 
 public class GreenTeaScript extends GtStatic {
 	public final static void ParseCommandOption(String[] Args) {
 		/*local*/String TargetCode = "exe";  // self executable
 		/*local*/int GeneratorFlag = 0;
 		/*local*/String OneLiner = null;
 		/*local*/String OutputFile = "-";  // stdout
 		/*local*/int    Index = 0;
 		while(Index < Args.length) {
 			/*local*/String Argu = Args[Index];
 			if(!Argu.startsWith("-")) {
 				break;
 			}
 			Index += 1;
 			if((Argu.equals("-e") || Argu.equals("--eval")) && Index < Args.length) {
 				OneLiner = Args[Index];
 				Index += 1;
 				continue;
 			}
 			if((Argu.equals("-o") || Argu.equals("--out")) && Index < Args.length) {
 				if(!Args[Index].endsWith(".green")) {  // for safety
 					OutputFile = Args[Index];
 					Index += 1;
 					continue;
 				}
 			}
 			if((Argu.equals("-l") || Argu.equals("--lang")) && Index < Args.length) {
 				if(!Args[Index].endsWith(".green")) {  // for safety
 					TargetCode = Args[Index];
 					Index += 1;
 					continue;
 				}
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose")) {
 				LibGreenTea.DebugMode = true;
 				LibGreenTea.VerboseMask |= (GtStatic.VerboseFile|GtStatic.VerboseSymbol|GtStatic.VerboseNative);
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:token")) {
 				LibGreenTea.VerboseMask |= GtStatic.VerboseToken;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:type")) {
 				LibGreenTea.VerboseMask |= GtStatic.VerboseType;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:symbol")) {
 				LibGreenTea.VerboseMask |= GtStatic.VerboseSymbol;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:native")) {
 				LibGreenTea.VerboseMask |= GtStatic.VerboseNative;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:func")) {
 				LibGreenTea.VerboseMask |= GtStatic.VerboseFunc;
 				continue;
 			}
 			if(LibGreenTea.EqualsString(Argu, "--verbose:no")) {
 				LibGreenTea.VerboseMask = 0;
 				continue;
 			}
 			LibGreenTea.Usage(Argu + " is unknown");
 		}
 		/*local*/GtGenerator Generator = LibGreenTea.CodeGenerator(TargetCode, OutputFile, GeneratorFlag);
 		if(Generator == null) {
 			LibGreenTea.Usage("no target: " + TargetCode);
 		}
 		/*local*/GtClassContext Context = new GtClassContext(new GreenTeaGrammar(), Generator);
 		/*local*/boolean ShellMode = true;
 		if(OneLiner != null) {
 			Context.TopLevelNameSpace.Eval(OneLiner, 1);
 			ShellMode = false;
 		}
 		while(Index < Args.length) {
 			/*local*/String ScriptText = LibGreenTea.LoadFile2(Args[Index]);
 			if(ScriptText == null) {
 				LibGreenTea.Exit(1, "file not found: " + Args[Index]);
 			}
 			/*local*/long FileLine = Context.GetFileLine(Args[Index], 1);
 			Context.TopLevelNameSpace.Eval(ScriptText, FileLine);
 			ShellMode = false;
 			Index += 1;
 		}
 		if(ShellMode) {
 			LibGreenTea.println(GtStatic.ProgName + GtStatic.Version + " (" + GtStatic.CodeName + ") on " + LibGreenTea.GetPlatform());
 			LibGreenTea.println(GtStatic.Copyright);
 			/*local*/int linenum = 1;
 			/*local*/String Line = null;
 			while((Line = LibGreenTea.ReadLine(">>> ")) != null) {
 				Context.TopLevelNameSpace.Eval(Line, linenum);
 				Context.ShowReportedErrors();
 				linenum += 1;
 			}
			LibGreenTea.println("");
 		}
 		else {
 			Generator.FlushBuffer();
 		}
 	}
 
 	public final static void main(String[] Args) {
 		GreenTeaScript.ParseCommandOption(Args);
 	}
 }
