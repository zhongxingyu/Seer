 
 //ifdef JAVA
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 interface GtConst {
 //endif VAJA
 
 	// ClassFlag
 	public final static int		PrivateClass					= 1 << 0;
 	public final static int		SingletonClass					= 1 << 1;
 	public final static int		FinalClass						= 1 << 2;
 	public final static int		GreenClass		    			= 1 << 3;
 	public final static int		StaticClass						= 1 << 4;
 	public final static int		ImmutableClass					= 1 << 5;
 	public final static int		InterfaceClass					= 1 << 6;
 
 	// MethodFlag
 	public final static int		PrivateMethod					= 1 << 0;
 	public final static int		VirtualMethod					= 1 << 1;
 	public final static int		FinalMethod						= 1 << 2;
 	public final static int		ConstMethod						= 1 << 3;
 	public final static int		StaticMethod					= 1 << 4;
 	public final static int		ImmutableMethod					= 1 << 5;
 	public final static int		TopLevelMethod					= 1 << 6;
 
 	// call rule
 	public final static int		CoercionMethod					= 1 << 7;
 	public final static int		RestrictedMethod				= 1 << 8;
 	public final static int		UncheckedMethod					= 1 << 9;
 	public final static int		SmartReturnMethod				= 1 << 10;
 	public final static int		VariadicMethod					= 1 << 11;
 	public final static int		IterativeMethod					= 1 << 12;
 
 	// compatible
 	public final static int		UniversalMethod					= 1 << 13;
 
 	// internal
 	public final static int		HiddenMethod					= 1 << 17;
 	public final static int		AbstractMethod					= 1 << 18;
 	public final static int		OverloadedMethod				= 1 << 19;
 	public final static int		Override						= 1 << 20;
 	public final static int		DynamicCall						= 1 << 22;
 	
 	public final static int		SymbolMaskSize					= 3;
 	public final static int		LowerSymbolMask					= 1;
 	public final static int		GetterSymbolMask				= (1 << 1);
 	public final static int		SetterSymbolMask				= (1 << 2);
 	public final static int		MetaSymbolMask					= (GetterSymbolMask | SetterSymbolMask);
 	public final static String	GetterPrefix					= "Get";
 	public final static String	SetterPrefix					= "Set";
 	public final static String	MetaPrefix						= "\\";
 
 	public final static int		AllowNewId						= -1;
 	public final static int		NoMatch							= -1;
 	public final static int		BreakPreProcess					= -1;
 
 	public final static boolean Optional = true;
 	public final static boolean Required = false;
 
 	public final static int		ErrorLevel						= 0;
 	public final static int		WarningLevel					= 1;
 	public final static int		InfoLevel					     = 2;
 
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
 
 	public static final int	CharMatrix[] = /*BeginArray*/{ 
 			0/*nul*/, 1/*soh*/, 1/*stx*/, 1/*etx*/, 1/*eot*/, 1/*enq*/,
 			1/*ack*/, 1/*bel*/, 1/*bs*/, TabChar/*ht*/, NewLineChar/*nl*/, 1/*vt*/, 1/*np*/, 1/*cr*/, 1/*so*/, 1/*si*/,
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
 	
 	public final static GtToken NullToken = new GtToken("", 0);
 
 	// TokenFlag
 	public final static int	SourceTokenFlag	= 1;
 	public final static int	ErrorTokenFlag	= (1 << 1);
 	public final static int IndentTokenFlag	= (1 << 2);
 	public final static int	WhiteSpaceTokenFlag	= (1 << 3);
 	public final static int DelimTokenFlag	= (1 << 4);
 	
 	// ParseFlag
 	public final static int	TrackbackParseFlag	= 1;
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
 
 	// ReturnStmt
 	public final static int	ReturnExpr	= 0;
 
 	// var N = 1;
 	public final static int	VarDeclType		= 0;
 	public final static int	VarDeclName		= 1;
 	public final static int	VarDeclValue	= 2;
 	public final static int	VarDeclScope	= 3;
 
 	// Method Decl;
 	public final static int	MethodDeclReturnType	= 0;
 	public final static int	MethodDeclClass		= 1;
 	public final static int	MethodDeclName		= 2;
 	public final static int	MethodDeclBlock		= 3;
 	public final static int	MethodDeclParam		= 4;
 
 	// spec 
 	public final static int TokenFuncSpec     = 0;
 	public final static int SymbolPatternSpec = 1;
 	public final static int ExtendedPatternSpec = 2;
 
 	public final static int		BinaryOperator					= 1;
 	public final static int		LeftJoin						= 1 << 1;
 	public final static int		PrecedenceShift					= 2;
 	public final static int		Precedence_CStyleValue			= (1 << PrecedenceShift);
 	public final static int		Precedence_CPPStyleScope		= (50 << PrecedenceShift);
 	public final static int		Precedence_CStyleSuffixCall		= (100 << PrecedenceShift);				/*x(); x[]; x.x x->x x++ */
 	public final static int		Precedence_CStylePrefixOperator	= (200 << PrecedenceShift);				/*++x; --x; sizeof x &x +x -x !x (T)x  */
 	//	Precedence_CppMember      = 300;  /* .x ->x */
 	public final static int		Precedence_CStyleMUL			= (400 << PrecedenceShift);				/* x * x; x / x; x % x*/
 	public final static int		Precedence_CStyleADD			= (500 << PrecedenceShift);				/* x + x; x - x */
 	public final static int		Precedence_CStyleSHIFT			= (600 << PrecedenceShift);				/* x << x; x >> x */
 	public final static int		Precedence_CStyleCOMPARE		= (700 << PrecedenceShift);
 	public final static int		Precedence_CStyleEquals			= (800 << PrecedenceShift);
 	public final static int		Precedence_CStyleBITAND			= (900 << PrecedenceShift);
 	public final static int		Precedence_CStyleBITXOR			= (1000 << PrecedenceShift);
 	public final static int		Precedence_CStyleBITOR			= (1100 << PrecedenceShift);
 	public final static int		Precedence_CStyleAND			= (1200 << PrecedenceShift);
 	public final static int		Precedence_CStyleOR				= (1300 << PrecedenceShift);
 	public final static int		Precedence_CStyleTRINARY		= (1400 << PrecedenceShift);				/* ? : */
 	public final static int		Precedence_CStyleAssign			= (1500 << PrecedenceShift);
 	public final static int		Precedence_CStyleCOMMA			= (1600 << PrecedenceShift);
 	public final static int		Precedence_Error				= (1700 << PrecedenceShift);
 	public final static int		Precedence_Statement			= (1900 << PrecedenceShift);
 	public final static int		Precedence_CStyleDelim			= (2000 << PrecedenceShift);
 
 	
 	public final static int		DefaultTypeCheckPolicy			= 0;
 	public final static int     IgnoreEmptyPolicy               = 1;
 	public final static int     AllowEmptyPolicy                = (1 << 1);
 
 	//typedef enum {
 	//	TypeCheckPolicy_NoPolicy       = 0,
 	//	TypeCheckPolicy_NoCheck        = (1 << 0),
 	//	TypeCheckPolicy_AllowVoid      = (1 << 1),
 	//	TypeCheckPolicy_Coercion       = (1 << 2),
 	//	TypeCheckPolicy_AllowEmpty     = (1 << 3),
 	//	TypeCheckPolicy_CONST          = (1 << 4),  /* Reserved */
 	//	TypeCheckPolicy_Creation       = (1 << 6)   /* TypeCheckNodeByName */
 	//} TypeCheckPolicy;
 
 	public final static String	GlobalConstName					= "global";
 
 	public final static ArrayList<String> SymbolList = new ArrayList<String>();
 	public final static GtMap   SymbolMap  = new GtMap();
 
 	public final static GtMethod AnyGetter = null;
 	// debug flags
 	static final public boolean	UseBuiltInTest	= true;
 	static final public boolean	DebugPrint		= true;
 
 //ifdef JAVA
 }
 
 class GtStatic implements GtConst {
 //endif VAJA
 	
 	public static void println(String msg) {
 		LangDeps.println(msg);		
 	}
 	
 	public static void DebugP(String msg) {
 		if(DebugPrint) {
 			LangDeps.println("DEBUG" + LangDeps.GetStackInfo(2) + ": " + msg);
 		}
 	}
 
 	public static void TODO(String msg) {
 		LangDeps.println("TODO" + LangDeps.GetStackInfo(2) + ": " + msg);
 	}
 
 	public static int ListSize(ArrayList<?> a) {
 		return (a == null) ? 0 : a.size();
 	}
 	
 	public final static boolean IsFlag(int flag, int flag2) {
 		return ((flag & flag2) == flag2);
 	}
 		
 	public final static int FromJavaChar(char c) {
 		if(c < 128) {
 			return CharMatrix[c];
 		}
 		return UnicodeChar;
 	}
 
 	// Symbol
 	public static boolean IsGetterSymbol(int SymbolId) {
 		return IsFlag(SymbolId, GetterSymbolMask);
 	}
 
 	public static boolean IsSetterSymbol(int SymbolId) {
 		return IsFlag(SymbolId, SetterSymbolMask);
 	}
 
 	public static int ToSetterSymbol(int SymbolId) {
 		assert(IsGetterSymbol(SymbolId));
 		return (SymbolId & (~GetterSymbolMask)) | SetterSymbolMask;
 	}
 
 	public final static int MaskSymbol(int SymbolId, int Mask) {
 		return (SymbolId << SymbolMaskSize) | Mask;
 	}
 
 	public final static int UnmaskSymbol(int SymbolId) {
 		return SymbolId >> SymbolMaskSize;
 	}
 
 	public static String StringfySymbol(int SymbolId) {
 		/*local*/String Key = (String)SymbolList.get(UnmaskSymbol(SymbolId));
 		if(IsFlag(SymbolId, GetterSymbolMask)) {
 			return GetterPrefix + Key;
 		}
 		if(IsFlag(SymbolId, SetterSymbolMask)) {
 			return SetterPrefix + Key;
 		}
 		if(IsFlag(SymbolId, MetaSymbolMask)) {
 			return MetaPrefix + Key;
 		}
 		return Key;
 	}
 
 	public static int GetSymbolId(String Symbol, int DefaultSymbolId) {
 		/*local*/String Key = Symbol;
 		/*local*/int Mask = 0;
 		if(Symbol.length() >= 3 && Symbol.charAt(1) == 'e' && Symbol.charAt(2) == 't') {
 			if(Symbol.charAt(0) == 'g' && Symbol.charAt(0) == 'G') {
 				Key = Symbol.substring(3);
 				Mask = GetterSymbolMask;
 			}
 			if(Symbol.charAt(0) == 's' && Symbol.charAt(0) == 'S') {
 				Key = Symbol.substring(3);
 				Mask = SetterSymbolMask;
 			}
 		}
 		if(Symbol.startsWith("\\")) {
 			Mask = MetaSymbolMask;
 		}
 		Integer SymbolObject = (Integer)SymbolMap.get(Key);
 		if(SymbolObject == null) {
 			if(DefaultSymbolId == AllowNewId) {
 				int SymbolId = SymbolList.size();
 				SymbolList.add(Key);
 				SymbolMap.put(Key, new Integer(SymbolId));
 				return MaskSymbol(SymbolId, Mask);
 			}
 			return DefaultSymbolId;
 		}
 		return MaskSymbol(SymbolObject.intValue(), Mask);
 	}
 
 	public static int GetSymbolId(String Symbol) {
 		return GetSymbolId(Symbol, AllowNewId);
 	}
 
 	public static String CanonicalSymbol(String Symbol) {
 		return Symbol.toLowerCase().replaceAll("_", "");
 	}
 
 	public static int GetCanonicalSymbolId(String Symbol) {
 		return GetSymbolId(CanonicalSymbol(Symbol), AllowNewId);
 	}
 
 	public final static GtFuncToken FunctionA(Object Callee, String MethodName) {
 		return new GtFuncToken(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 
 	public final static GtFuncMatch FunctionB(Object Callee, String MethodName) {
 		return new GtFuncMatch(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 	
 	public final static GtFuncTypeCheck FunctionC(Object Callee, String MethodName) {
 		return new GtFuncTypeCheck(Callee, LangDeps.LookupMethod(Callee, MethodName));
 	}
 
 	public final static boolean EqualsMethod(Method m1, Method m2) {
 		if(m1 == null) {
 			return (m2 == null) ? true : false;
 		} else {
 			return (m2 == null) ? false : m1.equals(m2);
 		}
 	}
 	
 	public final static TokenFunc CreateOrReuseTokenFunc(GtFuncToken f, TokenFunc prev) {
 		if(prev != null && EqualsMethod(prev.Func.Method, f.Method)) {
 			return prev;
 		}
 		return new TokenFunc(f, prev);
 	}
 
 	public final static int ApplyTokenFunc(TokenFunc TokenFunc, TokenContext TokenContext, String ScriptSource, int Pos) {
 		while(TokenFunc != null) {
 			/*local*/GtFuncToken f = TokenFunc.Func;
 			int NextIdx = LangDeps.ApplyTokenFunc(f.Self, f.Method, TokenContext, ScriptSource, Pos);
 			if(NextIdx > Pos) return NextIdx;
 			TokenFunc = TokenFunc.ParentFunc;
 		}
 		return NoMatch;
 	}
 
 	public final static SyntaxPattern MergeSyntaxPattern(SyntaxPattern Pattern, SyntaxPattern Parent) {
 		if(Parent == null) return Pattern;
 		SyntaxPattern MergedPattern = new SyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc, Pattern.TypeFunc);
 		MergedPattern.ParentPattern = Parent;
 		return MergedPattern;
 	}
 
 	public final static boolean IsEmptyOrError(SyntaxTree Tree) {
 		return Tree == null || Tree.IsEmptyOrError();
 	}
 
 	public final static SyntaxTree TreeHead(SyntaxTree Tree) {
 		if(Tree != null) {
 			while(Tree.PrevTree != null) {
 				Tree = Tree.PrevTree;
 			}
 		}
 		return Tree;
 	}
 	
 	public final static SyntaxTree ApplySyntaxPattern(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int Pos = TokenContext.Pos;
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		/*local*/SyntaxPattern CurrentPattern = Pattern;
 		while(CurrentPattern != null) {
 			/*local*/GtFuncMatch f = CurrentPattern.MatchFunc;
 			TokenContext.Pos = Pos;
 			if(CurrentPattern.ParentPattern != null) {
 				TokenContext.ParseFlag = ParseFlag | TrackbackParseFlag;
 			}
 			DebugP("B ApplySyntaxPattern: " + CurrentPattern + " > " + CurrentPattern.ParentPattern);
 			SyntaxTree ParsedTree = (SyntaxTree)LangDeps.ApplyMatchFunc(f.Self, f.Method, CurrentPattern, LeftTree, TokenContext);
 			if(ParsedTree != null && ParsedTree.IsEmpty()) ParsedTree = null;
 			DebugP("E ApplySyntaxPattern: " + CurrentPattern + " => " + ParsedTree);
 			TokenContext.ParseFlag = ParseFlag;
 			if(ParsedTree != null) {
 				return ParsedTree;
 			}
 			CurrentPattern = CurrentPattern.ParentPattern;
 		}
 		if(TokenContext.IsAllowedTrackback()) {
 			TokenContext.Pos = Pos;
 		}
 		if(Pattern == null) {
 			DebugP("undefined syntax pattern: " + Pattern);
 		}
 		return TokenContext.ReportExpectedPattern(Pattern);
 	}
 
 	public final static SyntaxTree ParseSyntaxTree(SyntaxTree PrevTree, TokenContext TokenContext) {
 		/*local*/SyntaxPattern Pattern = TokenContext.GetFirstPattern();
 		/*local*/SyntaxTree LeftTree = GtStatic.ApplySyntaxPattern(Pattern, PrevTree, TokenContext);
 		while (!GtStatic.IsEmptyOrError(LeftTree)) {
 			SyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern();
 			if(ExtendedPattern == null) {
 				DebugP("In $Expression$ ending: " + TokenContext.GetToken());
 				break;
 			}
 			LeftTree = GtStatic.ApplySyntaxPattern(ExtendedPattern, LeftTree, TokenContext);			
 		}
 		return LeftTree;
 	}
 
 	public final static void TestSyntaxPattern(GtContext Context, String Text) {
 		/*local*/TokenContext TokenContext = new TokenContext(Context.DefaultNameSpace, Text, 1);
 		SyntaxTree ParsedTree = GtStatic.ParseSyntaxTree(null, TokenContext);
 		assert(ParsedTree != null);
 	}
 	
 	// typing 
 	public final static TypedNode ApplyTypeFunc(GtFuncTypeCheck TypeFunc, TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		if(TypeFunc == null || TypeFunc.Method == null){
 			DebugP("try to invoke null TypeFunc");
 			return null;
 		}
 		return (TypedNode)LangDeps.ApplyTypeFunc(TypeFunc.Self, TypeFunc.Method, Gamma, ParsedTree, Type);
 	}
 	
 	public final static TypedNode LinkNode(TypedNode LastNode, TypedNode Node) {
 		Node.PrevNode = LastNode;
 		if(LastNode != null) {
 			LastNode.NextNode = Node;
 		}
 		return Node;
 	}
 
 //ifdef JAVA
 }
 
 final class GtMap {
 	private final HashMap<String, Object>	Map;
 
 	public GtMap() {
 		this.Map = new HashMap<String, Object>();
 	}
 
 	public int size() {
 		return this.Map.size();
 	}
 
 	public void put(String Key, Object Value) {
 		this.Map.put(Key, Value);
 	}
 
 	public Object get(String Key) {
 		return this.Map.get(Key);
 	}
 
 }
 
 final class GtFuncToken {
 	/*field*/public Object	Self;
 	/*field*/public Method	Method;
 	GtFuncToken/*constructor*/(Object Self, Method method) {
 		this.Self = Self;
 		this.Method = method;
 	}
 	@Override public String toString() {
 		if(this.Method == null) return "null";
 		return this.Method.toString();
 	}
 }
 
 final class GtFuncMatch {
 	/*field*/public Object	Self;
 	/*field*/public Method	Method;
 	GtFuncMatch/*constructor*/(Object Self, Method method) {
 		this.Self = Self;
 		this.Method = method;
 	}
 	@Override public String toString() {
 		if(this.Method == null) return "null";
 		return this.Method.toString();
 	}
 }
 
 final class GtFuncTypeCheck {
 	/*field*/public Object	Self;
 	/*field*/public Method	Method;
 	GtFuncTypeCheck/*constructor*/(Object Self, Method method) {
 		this.Self = Self;
 		this.Method = method;
 	}
 	@Override public String toString() {
 		if(this.Method == null) return "null";
 		return this.Method.toString();
 	}
 }
 
 //endif VAJA
 
 // tokenizer
 
 final class GtToken extends GtStatic {
 	/*field*/public int		        TokenFlag;
 	/*field*/public String	        ParsedText;
 	/*field*/public long		    FileLine;
 	/*field*/public SyntaxPattern	PresetPattern;
 
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
 		assert(this.IsError());
 		return this.ParsedText;
 	}
 	
 }
 
 final class TokenFunc {
 	/*field*/public GtFuncToken       Func;
 	/*field*/public TokenFunc	ParentFunc;
 
 	TokenFunc/*constructor*/(GtFuncToken Func, TokenFunc Parent) {
 		this.Func = Func;
 		this.ParentFunc = Parent;
 	}
 
 	@Override public String toString() {
 		return this.Func.Method.toString();
 	}
 }
 
 final class TokenContext extends GtStatic {
 	/*field*/public GtNameSpace NameSpace;
 	/*field*/public ArrayList<GtToken> SourceList;
 	/*field*/public int Pos;
 	/*field*/public long ParsingLine;
 	/*field*/public int ParseFlag;
 
 	TokenContext/*constructor*/(GtNameSpace NameSpace, String Text, long FileLine) {
 		this.NameSpace = NameSpace;
 		this.SourceList = new ArrayList<GtToken>();
 		this.Pos = 0;
 		this.ParsingLine = FileLine;
 		this.ParseFlag = 0;
 		AddNewToken(Text, SourceTokenFlag, null);
 	}
 
 	public GtToken AddNewToken(String Text, int TokenFlag, String PatternName) {
 		/*local*/GtToken Token = new GtToken(Text, this.ParsingLine);
 		Token.TokenFlag |= TokenFlag;
 		if(PatternName != null) {
 			Token.PresetPattern = this.NameSpace.GetPattern(PatternName);
 			assert(Token.PresetPattern != null);
 		}
 		//DebugP("<< " + Text + " : " + PatternName);
 		this.SourceList.add(Token);
 		return Token;
 	}
 
 	public void FoundWhiteSpace() {
 		/*local*/GtToken Token = GetToken();
 		Token.TokenFlag |= WhiteSpaceTokenFlag;
 	}
 	
 	public void FoundLineFeed(long line) {
 		this.ParsingLine += line;
 	}
 
 	public void ReportTokenError(int Level, String Message, String TokenText) {
 		/*local*/GtToken Token = this.AddNewToken(TokenText, 0, "$Error$");
 		this.NameSpace.ReportError(Level, Token, Message);
 	}
 	
 	public SyntaxTree NewErrorSyntaxTree(GtToken Token, String Message) {
 		if(!IsAllowedTrackback()) {
 			this.NameSpace.ReportError(ErrorLevel, Token, Message);
 			/*local*/SyntaxTree ErrorTree = new SyntaxTree(Token.PresetPattern, this.NameSpace, Token, null);
 			return ErrorTree;
 		}
 		return null;
 	}
 	
 	public GtToken GetBeforeToken() {
 		/*local*/int pos = this.Pos - 1;
 		while(pos >= 0) {
 			GtToken Token = this.SourceList.get(pos);
 			if(IsFlag(Token.TokenFlag, IndentTokenFlag)) {
 				pos -= 1;
 				continue;
 			}
 			return Token;
 		}
 		return null;
 	}
 
 	public SyntaxTree ReportExpectedToken(String TokenText) {
 		if(!IsAllowedTrackback()) {
 			/*local*/GtToken Token = GetBeforeToken();
 			if(Token != null) {
 				return NewErrorSyntaxTree(Token, TokenText + " is expected after " + Token.ParsedText);
 			}
 			Token = GetToken();
 			assert(Token != NullToken);
 			return NewErrorSyntaxTree(Token, TokenText + " is expected at " + Token.ParsedText);
 		}
 		return null;
 	}
 
 	public SyntaxTree ReportExpectedPattern(SyntaxPattern Pattern) {
 		return ReportExpectedToken(Pattern.PatternName);
 	}
 	
 	private int DispatchFunc(String ScriptSource, int GtChar, int pos) {
 		/*local*/TokenFunc TokenFunc = this.NameSpace.GetTokenFunc(GtChar);
 		/*local*/int NextIdx = GtStatic.ApplyTokenFunc(TokenFunc, this, ScriptSource, pos);
 		if(NextIdx == NoMatch) {
 			DebugP("undefined tokenizer: " + ScriptSource.charAt(pos));
 			this.AddNewToken(ScriptSource.substring(pos), 0, null);
 			return ScriptSource.length();
 		}
 		return NextIdx;
 	}
 
 	private void Tokenize(String ScriptSource, long CurrentLine) {
 		/*local*/int currentPos = 0;
 		/*local*/int len = ScriptSource.length();
 		this.ParsingLine = CurrentLine;
 		while(currentPos < len) {
 			/*local*/int gtCode = GtStatic.FromJavaChar(ScriptSource.charAt(currentPos));
 			/*local*/int nextPos = this.DispatchFunc(ScriptSource, gtCode, currentPos);
 			if(currentPos >= nextPos) {
 				break;
 			}
 			currentPos = nextPos;
 		}
 		this.Dump();
 	}
 
 	public GtToken GetToken() {
 		while((this.Pos < this.SourceList.size())) {
 			/*local*/GtToken Token = (GtToken)this.SourceList.get(this.Pos);
 			if(Token.IsSource()) {
 				this.SourceList.remove(this.SourceList.size()-1);
 				this.Tokenize(Token.ParsedText, Token.FileLine);
 				Token = (GtToken)this.SourceList.get(this.Pos);
 			}
 			if(IsFlag(this.ParseFlag, SkipIndentParseFlag) && Token.IsIndent()) {
 				this.Pos += 1;
 				continue;
 			}
 			return Token;
 		}
 		return NullToken;
 	}
 
 	public boolean HasNext() {
 		return (this.GetToken() != NullToken);
 	}
 
 	public GtToken Next() {
 		/*local*/GtToken Token = this.GetToken();
 		this.Pos += 1;
 		return Token;
 	}
 
 	public SyntaxPattern GetPattern(String PatternName) {
 		return this.NameSpace.GetPattern(PatternName);
 	}
 
 	public SyntaxPattern GetFirstPattern() {
 		/*local*/GtToken Token = GetToken();
 		if(Token.PresetPattern != null) {
 			return Token.PresetPattern;
 		}
 		/*local*/SyntaxPattern Pattern = this.NameSpace.GetPattern(Token.ParsedText);
 		if(Pattern == null) {
 			return this.NameSpace.GetPattern("$Symbol$");
 		}
 		return Pattern;
 	}
 
 	public SyntaxPattern GetExtendedPattern() {
 		/*local*/GtToken Token = GetToken();
 		/*local*/SyntaxPattern Pattern = this.NameSpace.GetExtendedPattern(Token.ParsedText);
 		return Pattern;		
 	}
 	
 	public boolean MatchToken(String TokenText) {
 		/*local*/GtToken Token = GetToken();
 		if(Token.EqualsText(TokenText)) {
 			this.Pos += 1;
 			return true;
 		}
 		return false;
 	}
 
 	public GtToken GetMatchedToken(String TokenText) {
 		/*local*/GtToken Token = GetToken();
 		while(Token != NullToken) {
 			this.Pos += 1;
 			if(Token.EqualsText(TokenText)) {
 				break;
 			}
 			Token = GetToken();
 		}
 		return Token;
 	}
 
 	public boolean IsAllowedTrackback() {
 		return IsFlag(this.ParseFlag, TrackbackParseFlag);
 	}
 
 	public final SyntaxTree ParsePatternAfter(SyntaxTree LeftTree, String PatternName, boolean IsOptional) {
 		/*local*/int Pos = this.Pos;
 		/*local*/int ParseFlag = this.ParseFlag;
 		/*local*/SyntaxPattern Pattern = this.GetPattern(PatternName);
 		if(IsOptional) {
 			this.ParseFlag |= TrackbackParseFlag;
 		}
 		/*local*/SyntaxTree SyntaxTree = GtStatic.ApplySyntaxPattern(Pattern, LeftTree, this);
 		this.ParseFlag = ParseFlag;
 		if(SyntaxTree != null) {
 			return SyntaxTree;
 		}
 		this.Pos = Pos;
 		return null;
 	}
 
 	public final SyntaxTree ParsePattern(String PatternName, boolean IsOptional) {
 		return this.ParsePatternAfter(null, PatternName, IsOptional);
 	}
 
 	public final boolean SkipEmptyStatement() {
 		/*local*/GtToken Token = null;
 		while((Token = GetToken()) != NullToken) {
 			if(Token.IsIndent() || Token.IsDelim()) {
 				this.Pos += 1;
 				continue;
 			}
 			break;
 		}
 		return (Token != NullToken);
 	}
 	
 	public void Dump() {
 		/*local*/int pos = this.Pos;
 		while(pos < this.SourceList.size()) {
 			GtToken token = (GtToken)this.SourceList.get(pos);
 			DebugP("["+pos+"]\t" + token + " : " + token.PresetPattern);
 			pos += 1;
 		}
 	}
 }
 
 final class SyntaxPattern extends GtStatic {
 	/*field*/public GtNameSpace	PackageNameSpace;
 	/*field*/public String		PatternName;
 	/*field*/int				SyntaxFlag;
 	/*field*/public GtFuncMatch       MatchFunc;
 	/*field*/public GtFuncTypeCheck       TypeFunc;
 	/*field*/public SyntaxPattern	ParentPattern;
 	
 	SyntaxPattern/*constructor*/(GtNameSpace NameSpace, String PatternName, GtFuncMatch MatchFunc, GtFuncTypeCheck TypeFunc) {
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
 
 	public boolean IsLeftJoin(SyntaxPattern Right) {
 		/*local*/int left = this.SyntaxFlag >> PrecedenceShift;
 		/*local*/int right = Right.SyntaxFlag >> PrecedenceShift;
 		return (left < right || (left == right && IsFlag(this.SyntaxFlag, LeftJoin) && IsFlag(Right.SyntaxFlag, LeftJoin)));
 	}
 }
 
 class SyntaxTree extends GtStatic {
 	/*field*/public SyntaxTree		ParentTree;
 	/*field*/public SyntaxTree		PrevTree;
 	/*field*/public SyntaxTree		NextTree;
 
 	/*field*/public GtNameSpace	    NameSpace;
 	/*field*/public SyntaxPattern	Pattern;
 	/*field*/public GtToken		    KeyToken;
 	/*field*/public ArrayList<SyntaxTree> TreeList;
 	/*field*/public Object      ConstValue;
 
 	SyntaxTree/*constructor*/(SyntaxPattern Pattern, GtNameSpace NameSpace, GtToken KeyToken, Object ConstValue) {
 		this.NameSpace = NameSpace;
 		this.KeyToken = KeyToken;
 		this.Pattern = Pattern;
 		this.ParentTree = null;
 		this.PrevTree = null;
 		this.NextTree = null;
 		this.TreeList = null;
 		this.ConstValue = ConstValue;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = "(" + this.KeyToken.ParsedText;
 		/*local*/int i = 0;
 		while(i < ListSize(this.TreeList)) {
 			/*local*/Object o = this.TreeList.get(i);
 			String Entry = o.toString();
 			if(o instanceof SyntaxTree) {
 				SyntaxTree SubTree = (SyntaxTree)o;
 				if(ListSize(SubTree.TreeList) == 0) {
 					Entry = SubTree.KeyToken.ParsedText;
 				}
 			}
 			s = s + " " + Entry;
 			i += 1;
 		}
 		return s + ")";
 	}
 
 	public void LinkNode(SyntaxTree Tree) {
 		Tree.PrevTree = this;
 		this.NextTree = Tree;
 	}
 	
 	public boolean IsError() {
 		return this.KeyToken.IsError();
 	}
 
 	public void ToError(GtToken Token) {
 		assert(Token.IsError());
 		this.KeyToken = Token;
 		this.TreeList = null;
 	}
 
 	public boolean IsEmpty() {
 		return this.KeyToken == NullToken;
 	}
 
 	public void ToEmpty() {
 		this.KeyToken = NullToken;
 		this.TreeList = null;
 		this.Pattern = this.NameSpace.GetPattern("$Empty$");
 	}
 	
 	public boolean IsEmptyOrError() {
 		return this.KeyToken == NullToken || this.KeyToken.IsError();
 	}
 	
 	public void ToEmptyOrError(SyntaxTree ErrorTree) {
 		if(ErrorTree == null) {
 			ToEmpty();
 		}
 		else {
 			ToError(ErrorTree.KeyToken);
 		}
 	}
 		
 	public SyntaxTree GetSyntaxTreeAt(int Index) {
 		return this.TreeList.get(Index);
 	}
 
 	public void SetSyntaxTreeAt(int Index, SyntaxTree Tree) {
 		if(!IsError()) {
 			if(Tree.IsError()) {
 				ToError(Tree.KeyToken);
 			}
 			else {
 				if(Index >= 0) {
 					if(this.TreeList == null) {
 						this.TreeList = new ArrayList<SyntaxTree>();
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
 	
 	public void SetMatchedPatternAt(int Index, TokenContext TokenContext, String PatternName,  boolean IsOptional) {
 		if(!IsEmptyOrError()) {
 			SyntaxTree ParsedTree = TokenContext.ParsePattern(PatternName, IsOptional);
 			if(ParsedTree == null && !IsOptional) {
 				ToEmpty();
 			}
 		}
 	}
 
 	public void SetMatchedTokenAt(int Index, TokenContext TokenContext, String TokenText, boolean IsOptional) {
 		if(!IsEmptyOrError()) {
 			/*local*/int Pos = TokenContext.Pos;
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.ParsedText.equals(TokenText)) {
 				SetSyntaxTreeAt(Index, new SyntaxTree(null, TokenContext.NameSpace, Token, null));
 			}
 			else {
 				TokenContext.Pos = Pos;
 				if (!IsOptional) {
 					ToEmptyOrError(TokenContext.ReportExpectedToken(TokenText));
 				}
 			}
 		}
 	}
 	
 	public void AppendParsedTree(SyntaxTree Tree) {
 		if(!IsError()) {
 			if(Tree.IsError()) {
 				ToError(Tree.KeyToken);
 			}
 			else {
 				if(this.TreeList == null) {
 					this.TreeList = new ArrayList<SyntaxTree>();
 				}
 				this.TreeList.add(Tree);
 			}
 		}
 	}
 
 	public final TypedNode TypeNodeAt(int Index, TypeEnv Gamma, GtType Type, int TypeCheckPolicy) {
 		if(this.TreeList != null && Index < this.TreeList.size()) {
 			/*local*/SyntaxTree NodeObject = this.TreeList.get(Index);
 			/*local*/TypedNode TypedNode = Gamma.TypeCheck(NodeObject, Type, TypeCheckPolicy);
 			return TypedNode;
 		}
 		if(!IsFlag(TypeCheckPolicy, AllowEmptyPolicy) && !IsFlag(TypeCheckPolicy, IgnoreEmptyPolicy)) {
 			Gamma.GammaNameSpace.ReportError(ErrorLevel, this.KeyToken, this.KeyToken.ParsedText + " needs more expression at " + Index);
 			return Gamma.Generator.CreateErrorNode(Type, this); // TODO, "syntax tree error: " + Index);
 		}
 		return null;
 	}
 }
 
 /* typing */
 
 /* builder */
 class GtObject extends GtStatic {
 	/*field*/public GtType	Type;
 	GtObject/*constructor*/(GtType Type) {
 		this.Type = Type;
 	}
 }
 
 class GtType extends GtStatic {
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/int					ClassFlag;
 	/*field*/public GtContext		GtContext;
 	/*field*/public String			ShortClassName;
 	/*field*/GtType					BaseClass;
 	/*field*/GtType					SuperClass;
 	/*field*/GtParam				ClassParam;
 	/*field*/GtType					SearchSimilarClass;
 	/*field*/ArrayList<GtMethod>	ClassMethodList;
 	/*field*/public GtType			SearchSuperMethodClass;
 	/*field*/public Object			DefaultNullValue;
 	/*field*/public Object          LocalSpec;
 
 	GtType/*constructor*/(GtContext GtContext, int ClassFlag, String ClassName, Object DefaultNullValue) {
 		this.GtContext = GtContext;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperClass = null;
 		this.BaseClass = this;
 		this.ClassMethodList = new ArrayList<GtMethod>();
 		this.DefaultNullValue = DefaultNullValue;
 		this.LocalSpec = null;
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 	public boolean Accept(GtType Type) {
 		if(this == Type) {
 			return true;
 		}
 		return false;
 	}
 
 	public void AddMethod(GtMethod Method) {
 		this.ClassMethodList.add(Method);
 	}
 
 	public GtMethod GetGetter(String Name) {
 		return AnyGetter;
 	}
 	
 //	public GtMethod FindMethod(String MethodName, int ParamSize) {
 //		/*local*/int i = 0;
 //		while(i < this.ClassMethodList.size()) {
 //			GtMethod Method = this.ClassMethodList.get(i);
 //			if(Method.Match(MethodName, ParamSize)) {
 //				return Method;
 //			}
 //			i += 1;
 //		}
 //		return null;
 //	}
 //
 //	public GtMethod LookupMethod(String MethodName, int ParamSize) {
 //		GtMethod Method = this.FindMethod(MethodName, ParamSize);
 //		if(Method != null) {
 //			return Method;
 //		}
 //		if(this.SearchSuperMethodClass != null) {
 //			Method = this.SearchSuperMethodClass.LookupMethod(MethodName, ParamSize);
 //			if(Method != null) {
 //				return Method;
 //			}
 //		}
 ////		if(GtContext.Generator.CreateMethods(this.LocalSpec, MethodName)) {
 ////			return this.LookupMethod(MethodName, ParamSize);
 ////		}
 ////ifdef JAVA
 //		if(this.LocalSpec instanceof Class) {
 //			if(this.CreateMethods(MethodName) > 0) {
 //				return this.FindMethod(MethodName, ParamSize);
 //			}
 //		}
 ////endif JAVA
 //		return null;
 //	}
 //
 //	public boolean DefineNewMethod(GtMethod NewMethod) {
 //		/*local*/int i = 0;
 //		while(i < this.ClassMethodList.size()) {
 //			/*local*/GtMethod DefinedMethod = (GtMethod) this.ClassMethodList.get(i);
 //			if(NewMethod.Match(DefinedMethod)) {
 //				return false;
 //			}
 //			i += 1;
 //		}
 //		this.AddMethod(NewMethod);
 //		return true;
 //	}
 //
 ////ifdef JAVA
 //	
 //	public void DefineMethod(int MethodFlag, String MethodName, GtParam Param, Object Callee, String LocalName) {
 //		GtMethod Method = new GtMethod(MethodFlag, this, MethodName, Param, LangDeps.LookupMethod(Callee, LocalName));
 //		this.AddMethod(Method);
 //	}
 //
 //	public GtType(GtContext GtContext, Class<?> ClassInfo) {
 //		this(GtContext, 0, ClassInfo.getSimpleName(), null);
 //		this.LocalSpec = ClassInfo;
 //		// this.ClassFlag = ClassFlag;
 //		Class<?> SuperClass = ClassInfo.getSuperclass();
 //		if(ClassInfo != Object.class && SuperClass != null) {
 //			this.SuperClass = GtContext.LookupHostLangType(ClassInfo.getSuperclass());
 //		}
 //	}
 //
 //	static GtMethod ConvertMethod(GtContext GtContext, Method Method) {
 //		GtType ThisType = GtContext.LookupHostLangType(Method.getClass());
 //		Class<?>[] ParamTypes = Method.getParameterTypes();
 //		GtType[] ParamData = new GtType[ParamTypes.length + 1];
 //		String[] ArgNames = new String[ParamTypes.length + 1];
 //		ParamData[0] = GtContext.LookupHostLangType(Method.getReturnType());
 //		for(int i = 0; i < ParamTypes.length; i++) {
 //			ParamData[i + 1] = GtContext.LookupHostLangType(ParamTypes[i]);
 //			ArgNames[i] = "arg" + i;
 //		}
 //		GtParam Param = new GtParam(ParamData.length, ParamData, ArgNames);
 //		GtMethod Mtd = new GtMethod(0, ThisType, Method.getName(), Param, Method);
 //		ThisType.AddMethod(Mtd);
 //		return Mtd;
 //	}
 //
 //	int CreateMethods(String MethodName) {
 //		int Count = 0;
 //		Method[] Methods = ((Class<?>)this.LocalSpec).getMethods();
 //		for(int i = 0; i < Methods.length; i++) {
 //			if(MethodName.equals(Methods[i].getName())) {
 //				GtType.ConvertMethod(this.GtContext, Methods[i]);
 //				Count = Count + 1;
 //			}
 //		}
 //		return Count;
 //	}
 //
 //	public boolean RegisterCompiledMethod(GtMethod NewMethod) {
 //		for(int i = 0; i < this.ClassMethodList.size(); i++) {
 //			GtMethod DefinedMethod = (GtMethod) this.ClassMethodList.get(i);
 //			if(NewMethod.Match(DefinedMethod)) {
 //				this.ClassMethodList.set(i, NewMethod);
 //				return true;
 //			}
 //		}
 //		return false;
 //	}
 ////endif VAJA
 }
 
 class GtParam extends GtStatic {
 	/*field*/public int                 ParamSize;
 	/*field*/public ArrayList<GtType>	TypeList;
 	/*field*/public ArrayList<String>   NameList;
 
 	GtParam/*constructor*/(ArrayList<GtType> TypeList, ArrayList<String> NameList) {
 		this.TypeList = TypeList;
 		this.NameList = NameList;
 		this.ParamSize = TypeList.size() - 1;
 	}
 
 	public final boolean Equals(GtParam Other) {
 		int ParamSize = Other.ParamSize;
 		if(ParamSize == this.ParamSize) {
 			/*local*/int i = 0;
 			while(i < ParamSize) {
 				if(this.TypeList.get(i) != Other.TypeList.get(i)) {
 					return false;
 				}
 				i += 1;
 			}
 			return true;
 		}
 		return false;
 	}
 
 //	public final boolean Match(int ParamSize, ArrayList<GtType> ParamList) {
 //		while(i + 1 < ParamSize) {
 //			/*local*/GtType ParamType = this.TypeList.get(i+1);
 //			GtType GivenType = ParamList.get(i);
 //			if(!ParamType.Match(GivenType)) {
 //				return false;
 //			}
 //			i += 1;
 //		}
 //		return true;
 //	}
 	
 }
 
 class GtDef extends GtStatic {
 
 	public void MakeDefinition(GtNameSpace NameSpace) {
 		
 	}
 
 }
 
 class GtMethod extends GtDef {
 	/*field*/public int				MethodFlag;
 	/*field*/public GtType			ClassInfo;
 	/*field*/public String			MethodName;
 	/*field*/int					MethodSymbolId;
 	/*field*/int					CanonicalSymbolId;
 	/*field*/public GtParam			Param;
 
 	GtMethod/*constructor*/(int MethodFlag, GtType ClassInfo, String MethodName, GtParam Param) {
 		this.MethodFlag = MethodFlag;
 		this.ClassInfo = ClassInfo;
 		this.MethodName = MethodName;
 		this.MethodSymbolId = GtStatic.GetSymbolId(MethodName);
 		this.CanonicalSymbolId = GtStatic.GetCanonicalSymbolId(MethodName);
 		this.Param = Param;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = this.ClassInfo + "." + this.MethodName + "(";
 		/*local*/int i = 0;
 		while(i < this.Param.ParamSize) {
 			/*local*/GtType ParamType = this.GetParamType(i);
 			if(i > 0) {
 				s += ", ";
 			}
 			s += ParamType;
 			i += 1;
 		}
 		return s + ": " + this.GetReturnType();
 	};
 
 	public boolean Is(int Flag) {
 		return IsFlag(this.MethodFlag, Flag);
 	}
 
 	public final GtType GetReturnType() {
 		GtType ReturnType = this.Param.TypeList.get(0);
 		return ReturnType;
 	}
 
 	public final GtType GetParamType(int ParamIdx) {
 		GtType ParamType = this.Param.TypeList.get(0);
 		return ParamType;
 	}
 
 //	public final boolean Match(GtMethod Other) {
 //		return (this.MethodName.equals(Other.MethodName) && this.Param.Equals(Other.Param));
 //	}
 //
 //	public boolean Match(String MethodName, int ParamSize) {
 //		if(MethodName.equals(this.MethodName)) {
 //			if(ParamSize == -1) {
 //				return true;
 //			}
 //			if(this.Param.GetParamSize() == ParamSize) {
 //				return true;
 //			}
 //		}
 //		return false;
 //	}
 //
 //	public boolean Match(String MethodName, int ParamSize, GtType[] RequestTypes) {
 //		if(!this.Match(MethodName, ParamSize)) {
 //			return false;
 //		}
 //		for(int i = 0; i < RequestTypes.length; i++) {
 //			if(RequestTypes.equals(this.GetParamType(this.ClassInfo, i)) == false) {
 //				return false;
 //			}
 //		}
 //		return true;
 //	}
 
 //	public Object Eval(Object[] ParamData) {
 //		//int ParamSize = this.Param.GetParamSize();
 //		//GtDebug.P("ParamSize: " + ParamSize);
 //		return this.MethodInvoker.Invoke(ParamData);
 //	}
 //
 //	public GtMethod(int MethodFlag, GtType ClassInfo, String MethodName, GtParam Param, GtNameSpace LazyNameSpace, Tokens SourceList) {
 //		this(MethodFlag, ClassInfo, MethodName, Param, null);
 //		this.LazyNameSpace = LazyNameSpace;
 //		this.SourceList = SourceList;
 //	}
 
 	public void DoCompilation() {
 //		if(this.MethodInvoker != null) {
 //			return;
 //		}
 //		SyntaxTree Tree = this.ParsedTree;
 //		GtNameSpace NS = this.LazyNameSpace;
 //		if(Tree == null) {
 //			Tokens BufferList = new Tokens();
 //			NS.PreProcess(this.SourceList, 0, this.SourceList.size(), BufferList);
 //			Tree = SyntaxTree.ParseNewNode(NS, null, BufferList, 0, BufferList.size(), AllowEmpty);
 //			GtStatic.println("untyped tree: " + Tree);
 //		}
 //		TypeEnv Gamma = new TypeEnv(this.LazyNameSpace, this);
 //		TypedNode TNode = TypeEnv.TypeCheck(Gamma, Tree, Gamma.VoidType, DefaultTypeCheckPolicy);
 //		GtBuilder Builder = this.LazyNameSpace.GetBuilder();
 //		this.MethodInvoker = Builder.Build(NS, TNode, this);
 	}
 }
 
 
 final class VariableInfo {
 	/*field*/public GtType	Type;
 	/*field*/public String	Name;
 	/*field*/public String	LocalName;
 
 	VariableInfo/*constructor*/(GtType Type, String Name, int Index) {
 		this.Type = Type;
 		this.Name = Name;
 		this.LocalName = Name + Index;
 	}
 }
 
 final class GtDelegate {
 	/*field*/public GtMethod Method;
 	/*field*/public Object   Callee;
 	/*field*/public GtType   Type;
 }
 
 final class TypeEnv extends GtStatic {
 	/*field*/public GtNameSpace	GammaNameSpace;
 	/*field*/public GreenTeaGenerator Generator;
 
 	/*field*/public GtMethod	Method;
 	/*field*/public GtType	ReturnType;
 	/*field*/public GtType	ThisType;
 //	/*field*/ArrayList<?>	LocalStackList;
 	/*field*/public int StackTopIndex;
 	/*field*/public ArrayList<VariableInfo> LocalStackList;
 	
 	/* for convinient short cut */
 	/*field*/public final GtType	VoidType;
 	/*field*/public final GtType	BooleanType;
 	/*field*/public final GtType	IntType;
 	/*field*/public final GtType	StringType;
 	/*field*/public final GtType	VarType;
 	/*field*/public final GtType	AnyType;
 	
 	TypeEnv/*constructor*/(GtNameSpace GammaNameSpace, GtMethod Method) {
 		this.GammaNameSpace = GammaNameSpace;
 		this.Generator      = GammaNameSpace.GtContext.Generator;
 		
 		this.VoidType = GammaNameSpace.GtContext.VoidType;
 		this.BooleanType = GammaNameSpace.GtContext.BooleanType;
 		this.IntType = GammaNameSpace.GtContext.IntType;
 		this.StringType = GammaNameSpace.GtContext.StringType;
 		this.VarType = GammaNameSpace.GtContext.VarType;
 		this.AnyType = GammaNameSpace.GtContext.AnyType;
 		this.Method = Method;
 		this.LocalStackList = new ArrayList<VariableInfo>();
 		this.StackTopIndex = 0;
 		if(Method != null) {
 			this.InitMethod(Method);
 		} else {
 			// global
 			this.ThisType = GammaNameSpace.GetGlobalObject().Type;
 			this.AppendDeclaredVariable(this.ThisType, "this");
 		}
 	}
 
 	private void InitMethod(GtMethod Method) {
 		this.ReturnType = Method.GetReturnType();
 		this.ThisType = Method.ClassInfo;
 		if(!Method.Is(StaticMethod)) {
 			this.AppendDeclaredVariable(Method.ClassInfo, "this");
 			/*local*/GtParam Param = Method.Param;
 			/*local*/int i = 0;
 			while(i < Param.ParamSize) {
 				this.AppendDeclaredVariable(Param.TypeList.get(i), Param.NameList.get(i));
 				i += 1;
 			}
 		}
 	}
 
 	public boolean AppendDeclaredVariable(GtType Type, String Name) {
 		/*local*/VariableInfo VarInfo = new VariableInfo(Type, Name, this.StackTopIndex);
 		if(this.StackTopIndex < this.LocalStackList.size()) {
 			this.LocalStackList.add(VarInfo);			
 		}
 		else {
 			this.LocalStackList.add(VarInfo);
 		}
 		this.StackTopIndex += 1;
 		return true;
 	}
 
 	public VariableInfo LookupDeclaredVariable(String Symbol) {
 		/*local*/int i = this.StackTopIndex - 1;
 		while(i >= 0) {
 			VariableInfo VarInfo = this.LocalStackList.get(i);
 			if(VarInfo.Name.equals(Symbol)) {
 				return VarInfo;
 			}
 			i -= 1;
 		}
 		return null;
 	}
 
 	public GtType GuessType(Object Value) {
 		TODO("GuessType");
 		return this.AnyType;
 	}
 	
 	public GtDelegate LookupDelegate(String Name) {
 		TODO("finding delegate");
 		return null;
 	}
 	
 	public TypedNode DefaultValueConstNode(SyntaxTree ParsedTree, GtType Type) {
 		if(Type.DefaultNullValue != null) {
 			return this.Generator.CreateConstNode(Type, ParsedTree, Type.DefaultNullValue);
 		}
 		return CreateErrorNode(ParsedTree, "undefined initial value of " + Type);
 	}
 	
 	public TypedNode CreateErrorNode(SyntaxTree ParsedTree, String Message) {
 		this.GammaNameSpace.ReportError(ErrorLevel, ParsedTree.KeyToken, Message);
 		return this.Generator.CreateErrorNode(this.VoidType, ParsedTree);
 	}
 
 	/* typing */
 	public TypedNode TypeEachNode(SyntaxTree Tree, GtType Type) {
 		TypedNode Node = GtStatic.ApplyTypeFunc(Tree.Pattern.TypeFunc, this, Tree, Type);
 		if(Node == null) {
 			Node = this.CreateErrorNode(Tree, "undefined type checker: " + Tree.Pattern);
 		}
 		return Node;
 	}
 
 	public TypedNode TypeCheckEachNode(SyntaxTree Tree, GtType Type, int TypeCheckPolicy) {
 		TypedNode Node = this.TypeEachNode(Tree, Type);
 		if(Node.IsError()) {
 			return Node;
 		}
 		return Node;
 	}
 	
 	public TypedNode TypeCheck(SyntaxTree ParsedTree, GtType Type, int TypeCheckPolicy) {
 		return this.TypeBlock(ParsedTree, Type);
 	}
 
 	public TypedNode TypeBlock(SyntaxTree ParsedTree, GtType Type) {
 		/*local*/int StackTopIndex = this.StackTopIndex;
 		/*local*/TypedNode LastNode = null;
 		while(ParsedTree != null) {
 			/*local*/GtType CurrentType = (ParsedTree.NextTree != null) ? this.VoidType : Type;
 			/*local*/TypedNode TypedNode = this.TypeCheckEachNode(ParsedTree, CurrentType, DefaultTypeCheckPolicy);
 			LastNode = GtStatic.LinkNode(LastNode, TypedNode);
 			if(TypedNode.IsError()) {
 				break;
 			}
 			ParsedTree = ParsedTree.NextTree;
 		}
 		this.StackTopIndex = StackTopIndex;
 		return (LastNode == null) ? null : LastNode.MoveHeadNode();
 	}
 }
 
 // NameSpace 
 
 final class GtSpec extends GtStatic {
 	/*field*/public int SpecType;
 	/*field*/public String SpecKey;
 	/*field*/public Object SpecBody;
 	
 	GtSpec/*constructor*/(int SpecType, String SpecKey, Object SpecBody) {
 		this.SpecType = SpecType;
 		this.SpecKey  = SpecKey;
 		this.SpecBody = SpecBody;
 	}
 }
 
 final class GtNameSpace extends GtStatic {
 	/*field*/public GtContext		GtContext;
 	/*field*/public String          PackageName;
 	/*field*/public GtNameSpace		ParentNameSpace;
 	/*field*/public ArrayList<GtNameSpace>	  ImportedNameSpaceList;
 	/*field*/public ArrayList<GtSpec>         PublicSpecList;
 	/*field*/public ArrayList<GtSpec>         PrivateSpecList;
 	
 	/*field*/TokenFunc[]	TokenMatrix;
 	/*field*/GtMap	 SymbolPatternTable;
 	/*field*/GtMap   ExtendedPatternTable;
 	
 	GtNameSpace/*constructor*/(GtContext GtContext, GtNameSpace ParentNameSpace) {
 		this.GtContext = GtContext;
 		this.ParentNameSpace = ParentNameSpace;
 		if(ParentNameSpace != null) {
 			this.PackageName = ParentNameSpace.PackageName;
 		}
 		this.ImportedNameSpaceList = null;
 		this.PublicSpecList = new ArrayList<GtSpec>();
 		this.PrivateSpecList = null;
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 		this.ExtendedPatternTable = null;
 	}
 		
 	private void RemakeTokenMatrixEach(GtNameSpace NameSpace) {
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(NameSpace.PublicSpecList)) {
 			/*local*/GtSpec Spec = NameSpace.PublicSpecList.get(i);
 			if(Spec.SpecType == TokenFuncSpec) {
 			/*local*/int j = 0;
 				while(j < Spec.SpecKey.length()) {
 					int kchar = GtStatic.FromJavaChar(Spec.SpecKey.charAt(j));
 					GtFuncToken Func = (GtFuncToken)Spec.SpecBody;
 					this.TokenMatrix[kchar] = GtStatic.CreateOrReuseTokenFunc(Func, this.TokenMatrix[kchar]);
 					j += 1;
 				}
 			}
 			i += 1;
 		}
 	}
 	
 	private void RemakeTokenMatrix(GtNameSpace NameSpace) {
 		if(NameSpace.ParentNameSpace != null) {
 			RemakeTokenMatrix(NameSpace.ParentNameSpace);
 		}
 		RemakeTokenMatrixEach(NameSpace);
 		/*local*/int i = 0;
 		while(i < ListSize(NameSpace.ImportedNameSpaceList)) {
 			GtNameSpace Imported = NameSpace.ImportedNameSpaceList.get(i);
 			RemakeTokenMatrixEach(Imported);
 			i += 1;
 		}
 	}
 	
 	public TokenFunc GetTokenFunc(int GtChar2) {
 		if(this.TokenMatrix == null) {
 			this.TokenMatrix = new TokenFunc[MaxSizeOfChars];
 			RemakeTokenMatrix(this);
 		}
 		return this.TokenMatrix[GtChar2];
 	}
 
 	public void DefineTokenFunc(String keys, GtFuncToken f) {
 		this.PublicSpecList.add(new GtSpec(TokenFuncSpec, keys, f));
 		this.TokenMatrix = null;
 	}
 		
 	private void TableAddSpec(GtMap Table, GtSpec Spec) {
 		Object Body = Spec.SpecBody;
 		if(Body instanceof SyntaxPattern) {
 			Object Parent = Table.get(Spec.SpecKey);
 			if(Parent instanceof SyntaxPattern) {
 				Body = GtStatic.MergeSyntaxPattern((SyntaxPattern)Body, (SyntaxPattern)Parent);
 			}
 		}
 		Table.put(Spec.SpecKey, Body);
 	}
 	
 	private void RemakeSymbolTableEach(GtNameSpace NameSpace, ArrayList<?> SpecList) {
 		/*local*/int i = 0;
 		while(i < ListSize(SpecList)) {
 			/*local*/GtSpec Spec = (GtSpec)SpecList.get(i);
 			if(Spec.SpecType == SymbolPatternSpec) {
 				this.TableAddSpec(this.SymbolPatternTable, Spec);
 			}
 			else if(Spec.SpecType == ExtendedPatternSpec) {
 				this.TableAddSpec(this.ExtendedPatternTable, Spec);
 			}
 			i += 1;
 		}
 	}
 	
 	private void RemakeSymbolTable(GtNameSpace NameSpace) {
 		if(NameSpace.ParentNameSpace != null) {
 			this.RemakeSymbolTable(NameSpace.ParentNameSpace);
 		}
 		this.RemakeSymbolTableEach(NameSpace, NameSpace.PublicSpecList);
 		this.RemakeSymbolTableEach(NameSpace, NameSpace.PrivateSpecList);
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(NameSpace.ImportedNameSpaceList)) {
 			GtNameSpace Imported = (GtNameSpace)NameSpace.ImportedNameSpaceList.get(i);
 			this.RemakeSymbolTableEach(Imported, Imported.PublicSpecList);
 			i += 1;
 		}
 	}
 	
 	public Object GetSymbol(String Key) {
 		if(this.SymbolPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 			this.ExtendedPatternTable = new GtMap();
 			this.RemakeSymbolTable(this);
 		}
 		return this.SymbolPatternTable.get(Key);
 	}
 		
 	public SyntaxPattern GetPattern(String PatternName) {
 		/*local*/Object Body = this.GetSymbol(PatternName);
 		return (Body instanceof SyntaxPattern) ? (SyntaxPattern)Body : null;
 	}
 
 	public SyntaxPattern GetExtendedPattern(String PatternName) {
 		if(this.ExtendedPatternTable == null) {
 			this.SymbolPatternTable = new GtMap();
 			this.ExtendedPatternTable = new GtMap();
 			this.RemakeSymbolTable(this);
 		}
 		/*local*/Object Body = this.ExtendedPatternTable.get(PatternName);
 		return (Body instanceof SyntaxPattern) ? (SyntaxPattern)Body : null;
 	}
 
 	public void DefineSymbol(String Key, Object Value) {
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, Key, Value);
 		this.PublicSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefinePrivateSymbol(String Key, Object Value) {
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, Key, Value);
 		if(this.PrivateSpecList == null) {
 			this.PrivateSpecList = new ArrayList<GtSpec>();
 		}
 		this.PrivateSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefineSyntaxPattern(String PatternName, GtFuncMatch MatchFunc, GtFuncTypeCheck TypeFunc) {
 		/*local*/SyntaxPattern Pattern = new SyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		/*local*/GtSpec Spec = new GtSpec(SymbolPatternSpec, PatternName, Pattern);
 		this.PublicSpecList.add(Spec);
 		if(this.SymbolPatternTable != null) {
 			this.TableAddSpec(this.SymbolPatternTable, Spec);
 		}
 	}
 
 	public void DefineExtendedPattern(String PatternName, int SyntaxFlag, GtFuncMatch MatchFunc, GtFuncTypeCheck TypeFunc) {
 		/*local*/SyntaxPattern Pattern = new SyntaxPattern(this, PatternName, MatchFunc, TypeFunc);
 		Pattern.SyntaxFlag = SyntaxFlag;
 		/*local*/GtSpec Spec = new GtSpec(ExtendedPatternSpec, PatternName, Pattern);
 		this.PublicSpecList.add(Spec);
 		if(this.ExtendedPatternTable != null) {
 			this.TableAddSpec(this.ExtendedPatternTable, Spec);
 		}
 	}
 	
 	public GtType DefineClass(GtType ClassInfo) {
 		if(ClassInfo.PackageNameSpace == null) {
 			ClassInfo.PackageNameSpace = this;
 			if(this.PackageName != null) {
 				this.GtContext.ClassNameMap.put(this.PackageName + "." + ClassInfo.ShortClassName, ClassInfo);
 			}
 			this.GtContext.Generator.AddClass(ClassInfo);
 		}
 		this.DefineSymbol(ClassInfo.ShortClassName, ClassInfo);
 		return ClassInfo;
 	}
 	
 	// Global Object
 	public GtObject CreateGlobalObject(int ClassFlag, String ShortName) {
 		/*local*/GtType NewClass = new GtType(this.GtContext, ClassFlag, ShortName, null);
 		/*local*/GtObject GlobalObject = new GtObject(NewClass);
 		NewClass.DefaultNullValue = GlobalObject;
 		return GlobalObject;
 	}
 
 	public GtObject GetGlobalObject() {
 		/*local*/Object GlobalObject = this.GetSymbol(GlobalConstName);
 		if(GlobalObject == null || !(GlobalObject instanceof GtObject)) {
 			GlobalObject = this.CreateGlobalObject(SingletonClass, "global");
 			this.DefinePrivateSymbol(GlobalConstName, GlobalObject);
 		}
 		return (GtObject) GlobalObject;
 	}
 
 	public void ImportNameSpace(GtNameSpace ImportedNameSpace) {
 		if(this.ImportedNameSpaceList == null) {
 			this.ImportedNameSpaceList = new ArrayList<GtNameSpace>();
 			this.ImportedNameSpaceList.add(ImportedNameSpace);
 		}
 		this.TokenMatrix = null;
 		this.SymbolPatternTable = null;
 		this.ExtendedPatternTable = null;
 	}
 
 	public Object Eval(String ScriptSource, long FileLine, GreenTeaGenerator Generator) {
 		/*local*/Object ResultValue = null;
 		DebugP("Eval: " + ScriptSource);
 		/*local*/TokenContext TokenContext = new TokenContext(this, ScriptSource, FileLine);
 		while(TokenContext.HasNext()) {
 			SyntaxTree Tree = GtStatic.ParseSyntaxTree(null, TokenContext);
 			DebugP("untyped tree: " + Tree);
 			TypeEnv Gamma = new TypeEnv(this, null);
 			TypedNode Node = Gamma.TypeCheckEachNode(Tree, Gamma.VoidType, DefaultTypeCheckPolicy);
 			ResultValue = Generator.Eval(Node);
 		}
 		return ResultValue;
 	}
 
 	public GtMethod LookupMethod(String MethodName, int ParamSize) {
 		//FIXME
 		//MethodName = "ClassName.MethodName";
 		//1. (ClassName, MethodName) = MethodName.split(".")
 		//2. find MethodName(arg0, arg1, ... , arg_ParamSize)
 		return null;
 	}
 
 	private String GetSourcePosition(long FileLine) {
 		return "(eval:" + (int) FileLine + ")";
 	}
 
 	public String ReportError(int Level, GtToken Token, String Message) {
 		if(!Token.IsError()) {
 			if(Level == ErrorLevel) {
 				Message = "(error) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 				Token.ToErrorToken(Message);
 			} else if(Level == WarningLevel) {
 				Message = "(warning) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			} else if(Level == InfoLevel) {
 				Message = "(info) " + this.GetSourcePosition(Token.FileLine) + " " + Message;
 			}
 			GtStatic.println(Message);
 			return Message;
 		}
 		return Token.GetErrorMessage();
 	}
 
 }
 
 class GtGrammar extends GtStatic {
 	public void LoadTo(GtNameSpace NameSpace) {
 		/*extension*/
 	}
 }
 
 final class KonohaGrammar extends GtGrammar {
 
 	// Token
 	public static int WhiteSpaceToken(TokenContext TokenContext, String SourceText, int pos) {
 		TokenContext.FoundWhiteSpace();
 		while(pos < SourceText.length()) {
 			char ch = SourceText.charAt(pos);
 			if(!LangDeps.IsWhitespace(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		return pos;
 	}
 
 	public static int IndentToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int LineStart = pos + 1;
 		TokenContext.FoundLineFeed(1);
 		pos = pos + 1;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = SourceText.charAt(pos);
 			if(!LangDeps.IsWhitespace(ch)) {
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
 	}
 
 	public static int SingleSymbolToken(TokenContext TokenContext, String SourceText, int pos) {
 		TokenContext.AddNewToken(SourceText.substring(pos, pos + 1), 0, null);
 		return pos + 1;
 	}
 
 	public static int SymbolToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = SourceText.charAt(pos);
 			if(!LangDeps.IsLetter(ch) && !LangDeps.IsDigit(ch) && ch != '_') {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, null);
 		return pos;
 	}
 
 	public static int OperatorToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int NextPos = pos + 1;
 		while(NextPos < SourceText.length()) {
 			/*local*/char ch = SourceText.charAt(NextPos);
 			if(LangDeps.IsWhitespace(ch) || LangDeps.IsLetter(ch) || LangDeps.IsDigit(ch)) {
 				break;
 			}
 			/*local*/String Sub = SourceText.substring(pos, pos + 1);
 			if(TokenContext.NameSpace.GetExtendedPattern(Sub) == null) {
 				NextPos += 1;
 				continue;
 			}
 			break;
 		}
 		TokenContext.AddNewToken(SourceText.substring(pos, NextPos), 0, null);
 		return NextPos;
 	}
 	
 //	public static int MemberToken(TokenContext TokenContext, String SourceText, int pos) {
 //		int start = pos + 1;
 //		while(pos < SourceText.length()) {
 //			/*local*/char ch = SourceText.charAt(pos);
 //			if(!LangDeps.IsLetter(ch) && !LangDeps.IsDigit(ch) && ch != '_') {
 //				break;
 //			}
 //			pos += 1;
 //		}
 //		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, "$Member$");
 //		return pos;
 //	}
 
 	public static int NumberLiteralToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		while(pos < SourceText.length()) {
 			/*local*/char ch = SourceText.charAt(pos);
 			if(!LangDeps.IsDigit(ch)) {
 				break;
 			}
 			pos += 1;
 		}
 		TokenContext.AddNewToken(SourceText.substring(start, pos), 0, "$IntegerLiteral$");
 		return pos;
 	}
 
 	public static int StringLiteralToken(TokenContext TokenContext, String SourceText, int pos) {
 		/*local*/int start = pos;
 		/*local*/char prev = '"';
 		while(pos < SourceText.length()) {
 			/*local*/char ch = SourceText.charAt(pos);
 			if(ch == '"' && prev != '\\') {
 				TokenContext.AddNewToken(SourceText.substring(start, pos+1), 0, "$StringLiteral$");
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
 
 	public static SyntaxTree ParseType(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/Object ConstValue = TokenContext.NameSpace.GetSymbol(Token.ParsedText);
 		if(ConstValue instanceof GtType) {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, ConstValue);
 		}
 		return null; // Not Matched
 	}
 	
 	public static TypedNode TypeConst(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateConstNode(Gamma.GuessType(ParsedTree.ConstValue), ParsedTree, ParsedTree.ConstValue);
 	}
 	
 	public static SyntaxTree ParseSymbol(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree TypeTree = TokenContext.ParsePattern("$Type$", Optional);
 		if(TypeTree != null) {
			/*local*/SyntaxTree DeclTree = TokenContext.ParsePatternAfter(TypeTree, "$VarDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
			DeclTree = TokenContext.ParsePatternAfter(TypeTree, "$MethodDecl$", Optional);
 			if(DeclTree != null) {
 				return DeclTree;
 			}
 			return TypeTree;
 		}
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtNameSpace NameSpace = TokenContext.NameSpace;
 		Object ConstValue = NameSpace.GetSymbol(Token.ParsedText);
 		if(!(ConstValue instanceof GtType)) {
 			return new SyntaxTree(NameSpace.GetPattern("$Const"), NameSpace, Token, ConstValue);
 		}
 		return new SyntaxTree(NameSpace.GetPattern("$Variable"), NameSpace, Token, null);
 	}
 
 	public static SyntaxTree ParseVariable(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/char ch = Token.ParsedText.charAt(0);
 		if(LangDeps.IsLetter(ch) || ch == '_') {
 			return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		}
 		return null;
 	}
 	
 	public static TypedNode TypeVariable(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/String Name = ParsedTree.KeyToken.ParsedText;
 		/*local*/VariableInfo VariableInfo = Gamma.LookupDeclaredVariable(Name);
 		if(VariableInfo != null) {
 			return Gamma.Generator.CreateLocalNode(Type, ParsedTree, VariableInfo.LocalName);
 		}
 		GtDelegate Delegate = Gamma.LookupDelegate(Name);
 		if(Delegate != null) {
 			return Gamma.Generator.CreateConstNode(Delegate.Type, ParsedTree, Delegate);
 		}
 		return Gamma.CreateErrorNode(ParsedTree, "undefined name: " + Name);
 	}
 	
 	public static SyntaxTree ParseVarDecl(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetToken(), null);
 		Tree.SetMatchedPatternAt(VarDeclType, TokenContext, "$Type$", Required);
 		Tree.SetMatchedPatternAt(VarDeclName, TokenContext, "$Variable$", Required);
 		if(TokenContext.MatchToken("=")) {
 			Tree.SetMatchedPatternAt(VarDeclValue, TokenContext, "$Expression$", Required);
 		}
 		while(TokenContext.MatchToken(",")) {
 			/*local*/SyntaxTree NextTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Tree.KeyToken, null);
 			NextTree.SetSyntaxTreeAt(VarDeclType, Tree.GetSyntaxTreeAt(VarDeclType));
 			Tree.LinkNode(NextTree);
 			Tree = NextTree;
 			Tree.SetMatchedPatternAt(VarDeclName, TokenContext, "$Variable$", Required);
 			if(TokenContext.MatchToken("=")) {
 				Tree.SetMatchedPatternAt(VarDeclValue, TokenContext, "$Expression$", Required);
 			}
 		}
 		return Tree;
 	}
 
 	public static TypedNode TypeVarDecl(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/SyntaxTree TypeTree = ParsedTree.GetSyntaxTreeAt(VarDeclType);
 		/*local*/SyntaxTree NameTree = ParsedTree.GetSyntaxTreeAt(VarDeclName);
 		/*local*/SyntaxTree ValueTree = ParsedTree.GetSyntaxTreeAt(VarDeclValue);
 		/*local*/GtType DeclType = (GtType)TypeTree.ConstValue;
 		/*local*/String VariableName = NameTree.KeyToken.ParsedText;
 		if(!Gamma.AppendDeclaredVariable(DeclType, VariableName)) {
 			Gamma.CreateErrorNode(TypeTree, "already defined variable " + VariableName);
 		}
 		TypedNode VariableNode = Gamma.TypeCheck(NameTree, DeclType, DefaultTypeCheckPolicy);
 		TypedNode InitValueNode = (ValueTree == null) ? Gamma.DefaultValueConstNode(ParsedTree, DeclType) : Gamma.TypeCheck(ValueTree, DeclType, DefaultTypeCheckPolicy);
 		TypedNode AssignNode = Gamma.Generator.CreateAssignNode(DeclType, ParsedTree, VariableNode, InitValueNode);
 		TypedNode BlockNode = Gamma.TypeBlock(ParsedTree.NextTree, Type);
 		ParsedTree.NextTree = null;
 		GtStatic.LinkNode(AssignNode, BlockNode);
 		return Gamma.Generator.CreateLetNode(BlockNode.Type, ParsedTree, DeclType, VariableNode, AssignNode/*connected block*/);
 	}
 
 	// Parse And Type
 	public static SyntaxTree ParseIntegerLiteral(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, LangDeps.ParseInt(Token.ParsedText));
 	}
 
 	public static SyntaxTree ParseStringLiteral(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		TODO("handling string literal");
 		return new SyntaxTree(Pattern, TokenContext.NameSpace, Token, Token.ParsedText);
 	}
 
 	public static SyntaxTree ParseExpression(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		return GtStatic.ParseSyntaxTree(null, TokenContext);
 	}
 	
 	public static SyntaxTree ParseUnary(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		Tree.SetMatchedPatternAt(0, TokenContext, "$Expression$", Required);
 		return Tree;
 	}
 
 	public static SyntaxTree ParseBinary(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree RightTree = GtStatic.ParseSyntaxTree(null, TokenContext);
 		if(GtStatic.IsEmptyOrError(RightTree)) return RightTree;
 		/* 1 + 2 * 3 */
 		/* 1 * 2 + 3 */
 		if(RightTree.Pattern.IsBinaryOperator()) {
 			if(Pattern.IsLeftJoin(RightTree.Pattern)) {
 				SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 				NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 				NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree.GetSyntaxTreeAt(LeftHandTerm));
 				RightTree.SetSyntaxTreeAt(LeftHandTerm, NewTree);
 				return RightTree;
 			}
 		}
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetSyntaxTreeAt(LeftHandTerm, LeftTree);
 		NewTree.SetSyntaxTreeAt(RightHandTerm, RightTree);
 		if(RightTree.NextTree != null) {
 			NewTree.LinkNode(RightTree.NextTree);
 			RightTree.NextTree = null;
 		}
 		return NewTree;
 	}
 
 	public static TypedNode TypeBinary(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 //		public final static int	LeftHandTerm	= 0;
 //		public final static int	RightHandTerm	= 1;
 //		TypedNode ExprNode = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 //		TypedNode ExprNode = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 //		return Gamma.Generator.CreateOperatorNode()
 		return null;
 	}
 	
 	public static SyntaxTree ParseField(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		TokenContext.MatchToken(".");
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.AppendParsedTree(LeftTree);
 		return NewTree;
 	}
 
 	public static TypedNode TypeField(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		TypedNode ExprNode = ParsedTree.TypeNodeAt(UnaryTerm, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 		GtMethod Method = ExprNode.Type.GetGetter(ParsedTree.KeyToken.ParsedText);
 		return Gamma.Generator.CreateGetterNode(Method.GetReturnType(), ParsedTree, Method, ExprNode);
 	}
 	
 	// PatternName: "("
 	public static SyntaxTree ParseParenthesis(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.MatchToken("(");
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/SyntaxTree Tree = TokenContext.ParsePattern("$Expression$", Required);
 		if(!TokenContext.MatchToken(")")) {
 			Tree = TokenContext.ReportExpectedToken(")");
 		}
 		TokenContext.ParseFlag = ParseFlag;		
 		return Tree;
 	}
 	
 	public static SyntaxTree ParseApply(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/int ParseFlag = TokenContext.ParseFlag;
 		TokenContext.ParseFlag |= SkipIndentParseFlag;
 		/*local*/SyntaxTree FuncTree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetMatchedToken("("), null);
 		FuncTree.AppendParsedTree(LeftTree);
 		while(!FuncTree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 			/*local*/SyntaxTree Tree = TokenContext.ParsePattern("$Expression$", Required);
 			FuncTree.AppendParsedTree(Tree);
 			if(TokenContext.MatchToken(",")) continue;
 		}
 		TokenContext.ParseFlag = ParseFlag;		
 		return FuncTree;
 	}
 
 	public static TypedNode TypeApply(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		TypedNode ApplyNode = Gamma.Generator.CreateApplyNode(Gamma.AnyType, ParsedTree, null);
 		TODO("ApplyNode");
 		return null;
 	}
 
 	public static SyntaxTree ParseEmpty(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		return new SyntaxTree(Pattern, TokenContext.NameSpace, NullToken, null);
 	}
 
 	public static TypedNode TypeEmpty(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.Generator.CreateNullNode(Gamma.VoidType, ParsedTree);
 	}
 	
 	public static SyntaxTree ParseBlock(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		if(TokenContext.MatchToken("{")) {
 			/*local*/SyntaxTree PrevTree = null;
 			while(TokenContext.SkipEmptyStatement()) {
 				if(TokenContext.MatchToken("}")) break;
 				PrevTree = GtStatic.ParseSyntaxTree(PrevTree, TokenContext);
 				if(GtStatic.IsEmptyOrError(PrevTree)) return PrevTree;
 			}
 			if(PrevTree == null) {
 				return TokenContext.ParsePattern("$Empty", Required);
 			}
 			return GtStatic.TreeHead(PrevTree);
 		}
 		return null;
 	}
 
 	public static TypedNode TypeBlock(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		return Gamma.TypeBlock(ParsedTree, Type);
 	}
 
 //	public static TypedNode TypeApply(TypeEnv Gamma, SyntaxTree Tree, GtType Type) {
 //		TODO("This is really necessary");
 //		return null;
 //	}
 
 	public static TypedNode TypeAnd(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateAndNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static TypedNode TypeOr(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode LeftNode = ParsedTree.TypeNodeAt(LeftHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode RightNode = ParsedTree.TypeNodeAt(RightHandTerm, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateOrNode(Gamma.BooleanType, ParsedTree, LeftNode, RightNode);
 	}
 
 	public static SyntaxTree ParseMember(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetToken();
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetSyntaxTreeAt(0, LeftTree);
 		return NewTree;		
 	}
 
 	// If Statement
 
 	public static SyntaxTree ParseIf(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("if");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, "(", Required);
 		NewTree.SetMatchedPatternAt(IfCond, TokenContext, "$Expression$", Required);
 		NewTree.SetMatchedTokenAt(NoWhere, TokenContext, ")", Required);
 		NewTree.SetMatchedPatternAt(IfThen, TokenContext, "$Statement$", Required);
 		if(TokenContext.MatchToken("else")) {
 			NewTree.SetMatchedPatternAt(IfElse, TokenContext, "$Statement$", Required);
 		}
 		return NewTree;
 	}
 
 	public static TypedNode TypeIf(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode CondNode = ParsedTree.TypeNodeAt(IfCond, Gamma, Gamma.BooleanType, DefaultTypeCheckPolicy);
 		/*local*/TypedNode ThenNode = ParsedTree.TypeNodeAt(IfThen, Gamma, Type, DefaultTypeCheckPolicy);
 		/*local*/TypedNode ElseNode = ParsedTree.TypeNodeAt(IfElse, Gamma, ThenNode.Type, AllowEmptyPolicy);
 		return Gamma.Generator.CreateIfNode(ThenNode.Type, ParsedTree, CondNode, ThenNode, ElseNode);
 	}
 
 	// Return Statement
 	public static SyntaxTree ParseReturn(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/GtToken Token = TokenContext.GetMatchedToken("return");
 		/*local*/SyntaxTree NewTree = new SyntaxTree(Pattern, TokenContext.NameSpace, Token, null);
 		NewTree.SetMatchedPatternAt(ReturnExpr, TokenContext, "$Expression$", Optional);
 		return NewTree;
 	}
 
 	public static TypedNode TypeReturn(TypeEnv Gamma, SyntaxTree ParsedTree, GtType Type) {
 		/*local*/TypedNode Expr = ParsedTree.TypeNodeAt(ReturnExpr, Gamma, Gamma.ReturnType, DefaultTypeCheckPolicy);
 		return Gamma.Generator.CreateReturnNode(Expr.Type, ParsedTree, Expr);
 	}
 	
 	public static SyntaxTree ParseMethodDecl(SyntaxPattern Pattern, SyntaxTree LeftTree, TokenContext TokenContext) {
 		/*local*/SyntaxTree Tree = new SyntaxTree(Pattern, TokenContext.NameSpace, TokenContext.GetToken(), null);
 		Tree.SetMatchedPatternAt(MethodDeclReturnType, TokenContext, "$Type$", Required);
 		Tree.SetMatchedPatternAt(MethodDeclClass, TokenContext, "$MethodClass$", Optional);
 		Tree.SetMatchedPatternAt(MethodDeclName, TokenContext, "$MethodName$", Required);
 		Tree.SetMatchedTokenAt(NoWhere, TokenContext, "(", Required);
 		/*local*/int ParamBase = MethodDeclParam;
 		while(!Tree.IsEmptyOrError() && !TokenContext.MatchToken(")")) {
 			Tree.SetMatchedPatternAt(ParamBase + VarDeclType, TokenContext, "$Type$", Required);
 			Tree.SetMatchedPatternAt(ParamBase + VarDeclName, TokenContext, "$Symbol$", Required);
 			if(TokenContext.MatchToken("=")) {
 				Tree.SetMatchedPatternAt(ParamBase + VarDeclValue, TokenContext, "$Expression$", Required);
 			}
 			ParamBase += 3;
 		}
 		Tree.SetMatchedPatternAt(MethodDeclBlock, TokenContext, "$Block$", Required);
 		return Tree;
 	}
 
 	public static TypedNode TypeMethodDecl(TypeEnv Gamma, SyntaxTree Tree, GtType Type) {
 		TODO("TypeMethodDecl");
 //		GtType AnyType = Tree.GetTokenType(VarDeclTypeOffset, null);
 //		GtToken VarToken = Tree.GetAtToken(VarDeclNameOffset);
 //		String VarName = Tree.GetTokenString(VarDeclNameOffset, null);
 //		if(AnyType.equals(Gamma.AnyType)) {
 //			return new ErrorNode(Type, VarToken, "cannot infer variable type");
 //		}
 //		assert (VarName != null);
 //		Gamma.AppendLocalType(AnyType, VarName);
 //		TypedNode Value = Tree.TypeNodeAt(2, Gamma, AnyType, 0);
 //		return new LetNode(AnyType, VarToken, Value, null);
 		return null;
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		// Define Types
 		/*local*/GtContext GtContext = NameSpace.GtContext;
 		NameSpace.DefineSymbol("void",    GtContext.VoidType); // FIXME
 		NameSpace.DefineSymbol("boolean", GtContext.BooleanType);
 		NameSpace.DefineSymbol("Object",  GtContext.ObjectType);
 		NameSpace.DefineSymbol("int",     GtContext.IntType);
 		NameSpace.DefineSymbol("String",  GtContext.StringType);
 
 		// Define Constants
 		NameSpace.DefineSymbol("true", new Boolean(true));
 		NameSpace.DefineSymbol("false", new Boolean(false));
 
 		NameSpace.DefineTokenFunc(" \t", FunctionA(this, "WhiteSpaceToken"));
 		NameSpace.DefineTokenFunc("\n",  FunctionA(this, "IndentToken"));
 		NameSpace.DefineTokenFunc("(){}[]<>.,:;+-*/%=&|!", FunctionA(this, "OperatorToken"));
 		NameSpace.DefineTokenFunc("Aa", FunctionA(this, "SymbolToken"));
 		NameSpace.DefineTokenFunc("\"", FunctionA(this, "StringLiteralToken"));
 		NameSpace.DefineTokenFunc("1",  FunctionA(this, "NumberLiteralToken"));
 //#ifdef JAVA
 		GtFuncMatch ParseUnary    = FunctionB(this, "ParseUnary");
 		GtFuncMatch ParseBinary   = FunctionB(this, "ParseBinary");
 		GtFuncTypeCheck TypeOperator = FunctionC(this, "TypeOperator");
 		GtFuncTypeCheck TypeConst = FunctionC(this, "TypeConst");
 		GtFuncTypeCheck TypeBlock = FunctionC(this, "TypeBlock");
 //endif VAJA
 		NameSpace.DefineSyntaxPattern("+", ParseUnary, TypeOperator);
 		NameSpace.DefineSyntaxPattern("-", ParseUnary, TypeOperator);
 		NameSpace.DefineSyntaxPattern("!", ParseUnary, TypeOperator);
 		
 		NameSpace.DefineExtendedPattern("*", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("/", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("%", BinaryOperator | Precedence_CStyleMUL, ParseBinary, TypeOperator);
 
 		NameSpace.DefineExtendedPattern("+", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("-", BinaryOperator | Precedence_CStyleADD, ParseBinary, TypeOperator);
 
 		NameSpace.DefineExtendedPattern("<", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("<=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern(">", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern(">=", BinaryOperator | Precedence_CStyleCOMPARE, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("==", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeOperator);
 		NameSpace.DefineExtendedPattern("!=", BinaryOperator | Precedence_CStyleEquals, ParseBinary, TypeOperator);
 
 		NameSpace.DefineExtendedPattern("=", BinaryOperator | Precedence_CStyleAssign | LeftJoin, ParseBinary, FunctionC(this, "TypeAssign"));
 
 		NameSpace.DefineExtendedPattern("&&", BinaryOperator | Precedence_CStyleAND, ParseBinary, FunctionC(this, "TypeAnd"));
 		NameSpace.DefineExtendedPattern("||", BinaryOperator | Precedence_CStyleOR, ParseBinary, FunctionC(this, "TypeOr"));
 		
 		
 		NameSpace.DefineSyntaxPattern("$Symbol$", FunctionB(this, "ParseSymbol"), null);
 		NameSpace.DefineSyntaxPattern("$Type$", FunctionB(this, "ParseType"), FunctionC(this, "ParseVariable"));
 		NameSpace.DefineSyntaxPattern("$Variable$", FunctionB(this, "ParseVariable"), FunctionC(this, "ParseVariable"));
 		NameSpace.DefineSyntaxPattern("$Const$", FunctionB(this, "ParseConst"), TypeConst);
 
 		NameSpace.DefineSyntaxPattern("$StringLiteral$", FunctionB(this, "ParseStringLiteral"), TypeConst);
 		NameSpace.DefineSyntaxPattern("$IntegerLiteral$", FunctionB(this, "ParseIntegerLiteral"), TypeConst);
 
 		NameSpace.DefineSyntaxPattern("(", FunctionB(this, "ParseParenthesis"), null);
 		NameSpace.DefineExtendedPattern("(", 0, FunctionB(this, "ParseApply"), FunctionC(this, "TypeField"));
 		NameSpace.DefineExtendedPattern(".", 0, FunctionB(this, "ParseField"), FunctionC(this, "TypeField"));
 		
 		NameSpace.DefineSyntaxPattern("$Block$", FunctionB(this, "ParseBlock"), TypeBlock);
 		NameSpace.DefineSyntaxPattern("$Statement$", FunctionB(this, "ParseStatement"), TypeBlock);
 		
 		NameSpace.DefineSyntaxPattern("$MethodDecl$", FunctionB(this, "ParseMethodDecl"), FunctionC(this, "TypeMethodDecl"));
 		NameSpace.DefineSyntaxPattern("$VarDecl$", FunctionB(this, "ParseVarDecl"), FunctionC(this, "TypeVarDecl"));
 		NameSpace.DefineSyntaxPattern("if", FunctionB(this, "ParseIf"), FunctionC(this, "TypeIf"));
 		NameSpace.DefineSyntaxPattern("return", FunctionB(this, "ParseReturn"), FunctionC(this, "ParseReturn"));
 	}
 }
 
 
 class GtContext extends GtStatic {
 
 	/*field*/public GtNameSpace		RootNameSpace;
 	/*field*/public GtNameSpace		DefaultNameSpace;
 
 	/*field*/public final GtType		VoidType;
 	/*field*/public final GtType		ObjectType;
 	/*field*/public final GtType		BooleanType;
 	/*field*/public final GtType		IntType;
 	/*field*/public final GtType		StringType;
 	/*field*/public final GtType		VarType;
 	/*field*/public final GtType		AnyType;
 
 	/*field*/final GtMap				ClassNameMap;
 	/*field*/public GreenTeaGenerator   Generator;
 	
 	GtContext/*constructor*/(GtGrammar Grammar, GreenTeaGenerator Generator) {
 		this.ClassNameMap = new GtMap();
 		this.Generator    = Generator;
 		this.RootNameSpace = new GtNameSpace(this, null);
 		this.VoidType    = this.RootNameSpace.DefineClass(new GtType(this, 0, "void", null));
 		this.ObjectType  = this.RootNameSpace.DefineClass(new GtType(this, 0, "Object", new Object()));
 		this.BooleanType = this.RootNameSpace.DefineClass(new GtType(this, 0, "boolean", false));
 		this.IntType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "int", 0));
 		this.StringType  = this.RootNameSpace.DefineClass(new GtType(this, 0, "String", ""));
 		this.VarType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "var", null));		
 		this.AnyType     = this.RootNameSpace.DefineClass(new GtType(this, 0, "any", null));
 		Grammar.LoadTo(this.RootNameSpace);
 		this.DefaultNameSpace = new GtNameSpace(this, this.RootNameSpace);
 	}
 
 	public void LoadGrammar(GtGrammar Grammar) {
 		Grammar.LoadTo(this.DefaultNameSpace);
 	}
 	
 	public void Define(String Symbol, Object Value) {
 		this.RootNameSpace.DefineSymbol(Symbol, Value);
 	}
 
 	public Object Eval(String text, long FileLine) {
 		return this.DefaultNameSpace.Eval(text, FileLine, this.Generator);
 	}
 }
 
 public class GreenTeaScript {
 	
 	private static void TestAll(GtContext Context) {
 		GtStatic.TestSyntaxPattern(Context, "int");
 		GtStatic.TestSyntaxPattern(Context, "123");
 		GtStatic.TestSyntaxPattern(Context, "1 + 2 * 3");
 	}
 	
 	public static void main(String[] argc) {
 		GtContext GtContext = new GtContext(new KonohaGrammar(), new GreenTeaGenerator());
 		//GtContext.Eval("int f(int a, int b) { return a + b; }", 0);
 		//GtContext.Eval("1 + 2 * 3", 0);
 		TestAll(GtContext);
 	}
 
 }
