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
 
 // LangBase is a language-dependent code used in GreenTea.java
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Writer;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public abstract class LibGreenTea {
 
 	public final static String GetPlatform() {
 		return "Java JVM-" + System.getProperty("java.version");
 	}
 
 	public final static void println(String msg) {
 		System.out.println(msg);
 	}
 
 	public static boolean DebugMode = false;
 
 	private final static String GetStackInfo(int depth) {
 		String LineNumber = " ";
 		Exception e =  new Exception();
 		StackTraceElement[] Elements = e.getStackTrace();
 		if(depth < Elements.length) {
 			StackTraceElement elem = Elements[depth];
 			LineNumber += elem;
 		}
 		return LineNumber;
 	}
 
 	public final static void TODO(String msg) {
 		LibGreenTea.println("TODO" + LibGreenTea.GetStackInfo(2) + ": " + msg);
 	}
 
 	public final static void DebugP(String msg) {
 		if(LibGreenTea.DebugMode) {
 			LibGreenTea.println("DEBUG" + LibGreenTea.GetStackInfo(2) + ": " + msg);
 		}
 	}
 
 	public static int VerboseMask = GtStatic.VerboseUndefined;
 	
 	public final static void VerboseLog(int VerboseFlag, String Message) {
 		if((LibGreenTea.VerboseMask & VerboseFlag) == VerboseFlag) {
 			LibGreenTea.println("GreenTea: " + Message);
 		}
 	}
 
 	public final static void VerboseException(Throwable e) {
 		if(e instanceof InvocationTargetException) {
 			Throwable cause = e.getCause();
 			if(cause instanceof RuntimeException) {
 				throw (RuntimeException)cause;
 			}
 			if(cause instanceof Error) {
 				throw (Error)cause;
 			}
 		}
 		LibGreenTea.VerboseLog(GtStatic.VerboseException, e.toString());
 	}
 
 	public final static void Exit(int status, String Message) {
 		System.err.println(Message);
 		System.exit(1);
 	}
 
 	public final static void Assert(boolean TestResult) {
 		if(!TestResult) {
 			Exit(1, "Assertion Failed");
 		}
 	}
 
 	public final static boolean IsWhitespace(char ch) {
 		return Character.isWhitespace(ch);
 	}
 
 	public final static boolean IsLetter(char ch) {
 		return Character.isLetter(ch);
 	}
 
 	public final static boolean IsDigit(char ch) {
 		return Character.isDigit(ch);
 	}
 
 	public final static char CharAt(String Text, int Pos) {
 		return Text.charAt(Pos);
 	}
 
 	public final static String CharToString(char code) {
 		return Character.toString(code);
 	}
 
 	public static final String UnquoteString(String Text) {
 		StringBuilder sb = new StringBuilder();
 		/*local*/char quote = LibGreenTea.CharAt(Text, 0);
 		/*local*/int i = 0;
 		/*local*/int Length = Text.length();
 		if(quote == '"' || quote == '\'') {
 			i = 1;
 			Length -= 1;
 		}
 		else {
 			quote = '\0';
 		}
 		while(i < Length) {
 			/*local*/char ch = LibGreenTea.CharAt(Text, i);
 			if(ch == '\\') {
 				i = i + 1;
 				char next = LibGreenTea.CharAt(Text, i);
 				switch (next) {
 				case 't':
 					ch = '\t';
 					break;
 				case 'n':
 					ch = '\n';
 					break;
 				case '"':
 					ch = '"';
 					break;
 				case '\'':
 					ch = '\'';
 					break;
 				case '\\':
 					ch = '\\';
 					break;
 				default:
 					ch = next;
 					break;
 				}
 				i = i + 1;
 			}
 			sb.append(ch);
 			i = i + 1;
 		}
 		return sb.toString();
 	}
 
 	public static final String QuoteString(String Text) {
 		StringBuilder sb = new StringBuilder();
 		sb.append('"');
 		/*local*/int i = 0;
 		while(i < Text.length()) {
 			/*local*/char ch = LibGreenTea.CharAt(Text, i);
 			if(ch == '\n') {
 				sb.append("\\n");
 			}
 			else if(ch == '\t') {
 				sb.append("\\t");
 			}
 			else if(ch == '"') {
 				sb.append("\\\"");
 			}
 			else if(ch == '\\') {
 				sb.append("\\\\");
 			}
 			else {
 				sb.append(ch);
 			}
 			i = i + 1;
 		}
 		sb.append('"');
 		return sb.toString();
 	}
 
 	public final static boolean EqualsString(String s, String s2) {
 		return s.equals(s2);
 	}
 
 	public final static long ParseInt(String Text) {
 		return Long.parseLong(Text);
 	}
 	
 	public final static boolean IsUnixCommand(String cmd) {
 		String[] path = System.getenv("PATH").split(":");
 		int i = 0;
 		while(i < path.length) {
 			if(LibGreenTea.HasFile(path[i] + "/" + cmd)) {
 				return true;
 			}
 			i = i + 1;
 		}
 		return false;
 	}
 
 	public final static GtType GetNativeType(GtContext Context, Object Value) {
 		GtType NativeType = null;
 		Class<?> NativeClassInfo = Value instanceof Class<?> ? (Class<?>)Value : Value.getClass();
 		NativeType = (GtType) Context.ClassNameMap.get(NativeClassInfo.getName());
 		if(NativeType == null) {
 			NativeType = new GtType(Context, GtStatic.NativeClass, NativeClassInfo.getSimpleName(), null, NativeClassInfo);
 			Context.SetGlobalTypeName(NativeClassInfo.getName(), NativeType);
 			LibGreenTea.VerboseLog(GtStatic.VerboseNative, "binding native class: " + NativeClassInfo.getName());
 		}
 		return NativeType;
 	}
 	
 	public final static String GetClassName(Object Value){
 		return Value.getClass().getName();
 	}
 
 	public final static GtFunc ConvertNativeMethodToFunc(GtContext Context, Method JavaMethod) {
 		/*local*/int FuncFlag = GtStatic.NativeFunc;
 		if(Modifier.isStatic(JavaMethod.getModifiers())) {
 			FuncFlag |= GtStatic.NativeStaticFunc;
 		}
 		/*local*/ArrayList<GtType> TypeList = new ArrayList<GtType>();
 		TypeList.add(LibGreenTea.GetNativeType(Context, JavaMethod.getReturnType()));
 		if(!Modifier.isStatic(JavaMethod.getModifiers())) {
 			TypeList.add(LibGreenTea.GetNativeType(Context, JavaMethod.getDeclaringClass()));
 		}
 		/*local*/Class<?>[] ParamTypes = JavaMethod.getParameterTypes();
 		if(ParamTypes != null) {
 			for(int j = 0; j < ParamTypes.length; j++) {
 				TypeList.add(LibGreenTea.GetNativeType(Context, ParamTypes[j]));
 			}
 		}
 		/*local*/GtFunc NativeFunc = new GtFunc(FuncFlag, JavaMethod.getName(), 0, TypeList);
 		NativeFunc.SetNativeMethod(FuncFlag, JavaMethod);
 		return NativeFunc;
 	}
 
 	public final static Method ImportNativeMethod(String FullName, boolean StaticMethodOnly) {
 		/*local*/Method FoundMethod = null;
 		int Index = FullName.lastIndexOf(".");
 		if(Index != -1) {
 			/*local*/String FuncName = FullName.substring(Index+1);
 			try {
 				/*local*/Class<?> NativeClass = Class.forName(FullName.substring(0, Index));
 				Method[] Methods = NativeClass.getDeclaredMethods();
 				if(Methods != null) {
 					for(int i = 0; i < Methods.length; i++) {
 						if(LibGreenTea.EqualsString(FuncName, Methods[i].getName())) {
 							if(!Modifier.isPublic(Methods[i].getModifiers())) {
 								continue;
 							}
 							if(StaticMethodOnly && !Modifier.isStatic(Methods[i].getModifiers())) {
 								continue;
 							}
 							if(FoundMethod != null) {
 								LibGreenTea.VerboseLog(GtStatic.VerboseUndefined, "overloaded method: " + FullName);
 								return FoundMethod; // return the first one
 							}
 							FoundMethod = Methods[i];
 						}
 					}
 				}
 			} catch (ClassNotFoundException e) {
 				LibGreenTea.VerboseLog(GtStatic.VerboseException, e.toString());			
 			}
 		}
 		if(FoundMethod == null) {
 			LibGreenTea.VerboseLog(GtStatic.VerboseUndefined, "undefined method: " + FullName);
 		}
 		return FoundMethod;	
 	}
 
 	public final static Method LookupNativeMethod(Object Callee, String FuncName) {
 		if(FuncName != null) {
 			// LibGreenTea.DebugP("looking up method : " + Callee.getClass().getSimpleName() + "." + FuncName);
 			Method[] methods = Callee.getClass().getMethods();
 			for(int i = 0; i < methods.length; i++) {
 				if(FuncName.equals(methods[i].getName())) {
 					return methods[i];
 				}
 			}
 			LibGreenTea.VerboseLog(GtStatic.VerboseUndefined, "undefined method: " + Callee.getClass().getSimpleName() + "." + FuncName);
 		}
 		return null;
 	}
 
 	public final static boolean EqualsFunc(Method m1, Method m2) {
 		if(m1 == null) {
 			return (m2 == null) ? true : false;
 		}
 		else {
 			return (m2 == null) ? false : m1.equals(m2);
 		}
 	}
 
 	public final static TokenFunc CreateOrReuseTokenFunc(GtDelegateToken f, TokenFunc prev) {
 		if(prev != null && EqualsFunc(prev.Func.Func, f.Func)) {
 			return prev;
 		}
 		return new TokenFunc(f, prev);
 	}
 
 	public final static int ApplyTokenFunc(GtDelegateToken Delegate, Object TokenContext, String Text, int pos) {
 		try {
 			Integer n = (Integer)Delegate.Func.invoke(Delegate.Self, TokenContext, Text, pos);
 			return n.intValue();
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyTokenFunc");
 		return -1;
 	}
 
 	public final static GtSyntaxTree ApplyMatchFunc(GtDelegateMatch Delegate, Object NameSpace, Object TokenContext, Object LeftTree, Object Pattern) {
 		try {
 			return (GtSyntaxTree)Delegate.Func.invoke(Delegate.Self, NameSpace, TokenContext, LeftTree, Pattern);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyMatchFunc");
 		return null;
 	}
 
 	public final static GtNode ApplyTypeFunc(GtDelegateType Delegate, Object Gamma, Object ParsedTree, Object TypeInfo) {
 		try {
 			return (GtNode)Delegate.Func.invoke(Delegate.Self, Gamma, ParsedTree, TypeInfo);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		}
 		catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		Exit(1, "Failed ApplyTypeFunc");
 		return null;
 	}
 
 	public final static GtType[] CompactTypeList(int BaseIndex, ArrayList<GtType> List) {
 		GtType[] Tuple = new GtType[List.size() - BaseIndex];
 		for(int i = BaseIndex; i < List.size(); i++) {
 			Tuple[i] = List.get(i);
 		}
 		return Tuple;
 	}
 
 	public final static String[] CompactStringList(ArrayList<String> List) {
 		if(List == null) {
 			return null;
 		}
 		String[] Tuple = new String[List.size()];
 		for(int i = 0; i < List.size(); i++) {
 			Tuple[i] = List.get(i);
 		}
 		return Tuple;
 	}
 
 	public static String[] MapGetKeys(GtMap Map) {
 		/*local*/Iterator<String> itr = Map.Map.keySet().iterator();
 		/*local*/ArrayList<String> List = new ArrayList<String>(Map.Map.size());
 		/*local*/int i = 0;
 		while(itr.hasNext()) {
 			List.add(itr.next());
 			i = i + 1;
 		}
 		return List.toArray(new String[List.size()]);
 	}
 
 	public final static void Usage(String Message) {
 		System.out.println("greentea usage :");
 		System.out.println("  --lang|-l LANG        Set Target Language");
 		System.out.println("      bash                Bash");
 		System.out.println("      C C99               C99");
 		System.out.println("      CSharp              CSharp");
 		System.out.println("      java java7 java8    Java");
 		System.out.println("      javascript js       JavaScript");
 		System.out.println("      lua                 Lua");
 		System.out.println("      haxe                Haxe");
 		System.out.println("      ocaml               OCaml");
 		System.out.println("      perl                Perl");
 		System.out.println("      python              Python");
 		System.out.println("      R                   R");
 		System.out.println("      ruby                Ruby");
 		System.out.println("      typescript ts       TypeScript");
 		System.out.println("");
 		System.out.println("  --out|-o  FILE        Output filename");
 		System.out.println("  --eval|-e EXPR        Program passed in as string");
 		System.out.println("  --verbose             Printing Debug infomation");
 		System.out.println("     --verbose:symbol     adding symbol info");
 		System.out.println("     --verbose:token      adding token info");
 		System.out.println("     --verbose:no         no log");
 		LibGreenTea.Exit(0, Message);
 	}
 	
 	public final static String DetectTargetCode(String Extension, String TargetCode) {
 		if(Extension.endsWith(".js")) {
 			return "js";
 		}
 		else if(Extension.endsWith(".pl")) {
 			return "perl";
 		}
 		else if(Extension.endsWith(".py")) {
 			return "python";
 		}
 		else if(Extension.endsWith(".sh")) {
 			return "bash";
 		}
 		else if(Extension.endsWith(".java")) {
 			return "java";
 		}
 		else if(Extension.endsWith(".c")) {
 			return "c";
 		}
 		else if(TargetCode.startsWith("X")) {
 			return "exe";
 		}
 		return TargetCode;
 	}
 
 	public final static GtGenerator CodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
 		String Extension = (OutputFile == null) ? "-" : OutputFile;
 		TargetCode = DetectTargetCode(Extension, TargetCode);
 		TargetCode = TargetCode.toLowerCase();
 		if(TargetCode.startsWith("js") || TargetCode.startsWith("javascript")) {
 			return new JavaScriptSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("pl") || TargetCode.startsWith("perl")) {
 			return new PerlSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("python")) {
 			return new PythonSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("bash")) {
 			return new BashSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("java")) {
 			return new JavaSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("c")) {
 			return new CSourceGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		else if(TargetCode.startsWith("exe")) {
 			return new JavaByteCodeGenerator(TargetCode, OutputFile, GeneratorFlag);
 		}
 		return null;
 	}
 
 	public final static void WriteCode(String OutputFile, String SourceCode) {
 		if(OutputFile == null) {
 			LibGreenTea.Eval(SourceCode);
 		}
 		if(OutputFile.equals("-")) {
 			System.out.println(SourceCode);
 			System.out.flush();
 		}
 		else {
 			Writer out = null;
 			try {
 				out = new FileWriter(OutputFile);
 				out.write(SourceCode);
 				out.flush();
 				out.close();
 			} catch (IOException e) {
 				System.err.println("Cannot write: " + OutputFile);
 				System.exit(1);
 			}
 		}
 	}
 
 	private static java.io.Console Console = null;
 
 	public final static String ReadLine(String Prompt) {
 		if(Console == null) {
 			Console = System.console();
 		}
 		String Line = Console.readLine(Prompt);
 		if(Line == null) {
 			System.exit(0);
 		}
 		return Line;
 	}
 
 	public final static boolean HasFile(String Path) {
 		if(LibGreenTea.class.getResource(Path) != null) {
 			return true;
 		}
 		return new File(Path).exists();
 	}
 
 	public final static boolean IsSupportedTarget(String TargetCode) {
 		return HasFile("lib/" + TargetCode + "/common.green");
 	}
 
 	public final static String LoadFile2(String FileName) {
 		LibGreenTea.VerboseLog(GtStatic.VerboseFile, "loading " + FileName);
 		InputStream Stream = LibGreenTea.class.getResourceAsStream(FileName);
 		if(Stream == null) {
 			File f = new File(FileName);
 			try {
 				Stream = new FileInputStream(f);
 			} catch (FileNotFoundException e) {
 				return null;
 			}
 		}
 		BufferedReader reader = new BufferedReader(new InputStreamReader(Stream));
 		String line = "";
 		String buffer = "";
 		try {
 			while((line = reader.readLine()) != null) {
 				buffer += line + "\n";
 			}
 		} catch (IOException e) {
 			return null;
 		}
 		return buffer;
 	}
 
 	public final static String GetLibPath(String TargetCode, String LibName) {
 		/*local*/String Path = "lib/" + TargetCode + "/" + LibName + ".green";
 		return Path;
 	}
 
 	public static long JoinIntId(int UpperId, int LowerId) {
 		long id = UpperId;
 		id = (id << 32) + LowerId;
 		return id;
 	}
 
 	public static int UpperId(long FileLine) {
 		return (int)(FileLine >> 32);
 	}
 
 	public static int LowerId(long FileLine) {
 		return (int)FileLine;
 	}
 
 	public final static Object Eval(String SourceCode) {
 		LibGreenTea.VerboseLog(GtStatic.VerboseEval, "eval as native code: " + SourceCode);
 		//eval(SourceCode);
 		//System.out.println("Eval: " + SourceCode);  // In Java, no eval
 		return null;
 	}
 
 	public static Object EvalCast(GtType CastType, Object Value) {
 		if(Value != null) {
 			GtType ValueType = CastType.Context.GuessType(Value);
 			if(ValueType == CastType || CastType.Accept(ValueType)) {
 				return Value;
 			}
 			TODO("Add Invoke Coercion.. from " + ValueType + " to " + CastType);
 			if(CastType == CastType.Context.StringType) {
 				return Value.toString();
 			}
 		}
 		return null;
 	}
 
 	public static Object EvalInstanceOf(Object Value, GtType Type) {
 		if(Value != null) {
 			GtType ValueType = Type.Context.GuessType(Value);
 			if(ValueType == Type || Type.Accept(ValueType)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static Object EvalUnary(GtType Type, String Operator, Object Value) {
 		if(Value instanceof Boolean) {
 			if(Operator.equals("!") || Operator.equals("not")) {
 				return EvalCast(Type, !((Boolean)Value).booleanValue());
 			}
 			return null;
 		}
 		if(Value instanceof Long || Value instanceof Integer  || Value instanceof Short) {
 			if(Operator.equals("-")) {
 				return EvalCast(Type, -((Number)Value).longValue());
 			}
 			if(Operator.equals("+")) {
 				return EvalCast(Type, +((Number)Value).longValue());
 			}
 			return null;
 		}
 		return null;
 	}
 
 	public static Object EvalSuffix(GtType Type, Object Value, String Operator) {
 		return null;
 	}
 
 	public static Object EvalBinary(GtType Type, Object LeftValue, String Operator, Object RightValue) {
 		if(LeftValue == null || RightValue == null) {
 			return null;
 		}
 		if(LeftValue instanceof String || RightValue instanceof String) {
 			String left = EvalCast(Type.Context.StringType, LeftValue).toString();
 			String right = EvalCast(Type.Context.StringType, RightValue).toString();
 			if(Operator.equals("+")) {
 				return  EvalCast(Type, left + right);
 			}
		}
		if(LeftValue instanceof String && RightValue instanceof String) {
			String left = EvalCast(Type.Context.StringType, LeftValue).toString();
			String right = EvalCast(Type.Context.StringType, RightValue).toString();
 			if(Operator.equals("==")) {
 				return  EvalCast(Type, left.equals(right));
 			}
 			if(Operator.equals("!=")) {
 				return EvalCast(Type, !left.equals(right));
 			}
 			if(Operator.equals("<")) {
 				return EvalCast(Type, left.compareTo(right) < 0);
 			}
 			if(Operator.equals("<=")) {
 				return EvalCast(Type, left.compareTo(right) <= 0);
 			}
 			if(Operator.equals(">")) {
 				return EvalCast(Type, left.compareTo(right) > 0);
 			}
 			if(Operator.equals(">=")) {
 				return EvalCast(Type, left.compareTo(right) >= 0);
 			}
 			return null;
 		}
 		if(LeftValue instanceof Double || LeftValue instanceof Float || RightValue instanceof Double || RightValue instanceof Float) {
 			try {
 				double left = ((Number)LeftValue).doubleValue();
 				double right = ((Number)RightValue).doubleValue();
 				if(Operator.equals("+")) {
 					return EvalCast(Type, left + right);
 				}
 				if(Operator.equals("-")) {
 					return EvalCast(Type, left - right);
 				}
 				if(Operator.equals("*")) {
 					return EvalCast(Type, left * right);
 				}
 				if(Operator.equals("/")) {
 					return EvalCast(Type, left / right);
 				}
 				if(Operator.equals("%") || Operator.equals("mod")) {
 					return EvalCast(Type, left % right);
 				}
 				if(Operator.equals("==")) {
 					return EvalCast(Type, left == right);
 				}
 				if(Operator.equals("!=")) {
 					return EvalCast(Type, left != right);
 				}
 				if(Operator.equals("<")) {
 					return EvalCast(Type, left < right);
 				}
 				if(Operator.equals("<=")) {
 					return EvalCast(Type, left <= right);
 				}
 				if(Operator.equals(">")) {
 					return EvalCast(Type, left > right);
 				}
 				if(Operator.equals(">=")) {
 					return EvalCast(Type, left >= right);
 				}
 			}
 			catch(ClassCastException e) {
 			}
 			return null;
 		}
 		if(LeftValue instanceof Boolean && RightValue instanceof Boolean) {
 			boolean left = (Boolean)LeftValue;
 			boolean right = (Boolean)RightValue;
 			if(Operator.equals("==")) {
 				return EvalCast(Type, left == right);
 			}
 			if(Operator.equals("!=")) {
 				return EvalCast(Type, left != right);
 			}
 			return null;
 		}
 		try {
 			long left = ((Number)LeftValue).longValue();
 			long right = ((Number)RightValue).longValue();
 			if(Operator.equals("+")) {
 				return EvalCast(Type, left + right);
 			}
 			if(Operator.equals("-")) {
 				return EvalCast(Type, left - right);
 			}
 			if(Operator.equals("*")) {
 				return EvalCast(Type, left * right);
 			}
 			if(Operator.equals("/")) {
 				return EvalCast(Type, left / right);
 			}
 			if(Operator.equals("%") || Operator.equals("mod")) {
 				return EvalCast(Type, left % right);
 			}
 			if(Operator.equals("==")) {
 				return EvalCast(Type, left == right);
 			}
 			if(Operator.equals("!=")) {
 				return EvalCast(Type, left != right);
 			}
 			if(Operator.equals("<")) {
 				return EvalCast(Type, left < right);
 			}
 			if(Operator.equals("<=")) {
 				return EvalCast(Type, left <= right);
 			}
 			if(Operator.equals(">")) {
 				return EvalCast(Type, left > right);
 			}
 			if(Operator.equals(">=")) {
 				return EvalCast(Type, left >= right);
 			}
 			if(Operator.equals("|")) {
 				return EvalCast(Type, left | right);
 			}
 			if(Operator.equals("&")) {
 				return EvalCast(Type, left & right);
 			}
 			if(Operator.equals("<<")) {
 				return EvalCast(Type, left << right);
 			}
 			if(Operator.equals(">>")) {
 				return EvalCast(Type, left >> right);
 			}
 			if(Operator.equals("^")) {
 				return EvalCast(Type, left ^ right);
 			}
 		}
 		catch(ClassCastException e) {
 		}
 		return null;
 	}
 
 	public static Object EvalGetter(GtType Type, Object Value, String FieldName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 }
