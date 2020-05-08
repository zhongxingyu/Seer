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
 package org.GreenTeaScript;
 import java.util.ArrayList;
 //endif VAJA
 
 public class DShellGrammar extends GtGrammar {
 	
 //	public final static String[] ShellGrammarReservedKeywords = {"true", "false", "as", "if", "/"};
 //
 //	// shell grammar
 //	public static int SymbolShellToken(GtTokenContext TokenContext, String SourceText, int pos) {
 //		/*local*/int start = pos;
 //		/*local*/boolean isHeadOfToken = true;
 //		while(pos < SourceText.length()) {
 //			/*local*/char ch = LibGreenTea.CharAt(SourceText, pos);
 //			// a-zA-Z0-9_-
 //			if(ch == ' ' || ch == '\t' || ch == '\n' || ch == ';') {
 //				break;
 //			}
 //			else if(ch == '|' || ch == '>' || ch == '<') {
 //				if(isHeadOfToken) {
 //					pos += 1;
 //				}
 //				break;
 //			}
 //			isHeadOfToken = false;
 //			pos += 1;
 //		}
 //		if(start == pos) {
 //			return MismatchedPosition;
 //		}
 //		/*local*/String Symbol = SourceText.substring(start, pos);
 //
 //		/*local*/int i = 0;
 //		while(i < ShellGrammarReservedKeywords.length) {
 //			/*local*/String Keyword = ShellGrammarReservedKeywords[i];
 //			if(Symbol.equals(Keyword)) {
 //				return GtStatic.MismatchedPosition;
 //			}
 //			i = i + 1;
 //		}
 //
 //		if(Symbol.startsWith("/")) {
 //			if(Symbol.startsWith("//")) { // One-Line Comment
 //				return GtStatic.MismatchedPosition;
 //			}
 //			if(Symbol.startsWith("/*")) {
 //				return GtStatic.MismatchedPosition;
 //			}
 //		}
 //
 //		if(LibGreenTea.IsUnixCommand(Symbol)) {
 //			TokenContext.AddNewToken(Symbol, WhiteSpaceTokenFlag, "$ShellExpression$");
 //			return pos;
 //		}
 //
 //		/*local*/int SrcListSize = TokenContext.SourceList.size();
 //		if(SrcListSize > 0) {
 //			/*local*/int index = SrcListSize - 1;
 //			while(index >= 0) {
 //				/*local*/GtToken PrevToken = TokenContext.SourceList.get(index);
 //				if(PrevToken.PresetPattern != null &&
 //					PrevToken.PresetPattern.EqualsName("$ShellExpression$")) {
 //					TokenContext.AddNewToken(Symbol, WhiteSpaceTokenFlag, "$StringLiteral$");
 //					return pos;
 //				}
 //				if(PrevToken.IsIndent() || PrevToken.EqualsText(";")) {
 //					break;
 //				}
 //				index = index - 1;
 //			}
 //		}
 //		return GtStatic.MismatchedPosition;
 //	}
 //
 //	public final static GtSyntaxTree ParseIdeShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 //		/*local*/GtToken Token = TokenContext.Next();
 //		/*local*/GtSyntaxTree NewTree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 //		/*local*/int ParseFlag = TokenContext.ParseFlag;
 //		TokenContext.ParseFlag = 0;
 //		while(!GtStatic.IsMismatchedOrError(NewTree) && !TokenContext.MatchToken(";")) {
 //			/*local*/GtSyntaxTree Tree = null;
 //			if(TokenContext.GetToken().IsDelim() || TokenContext.GetToken().IsIndent()) {
 //				break;
 //			}
 //
 //			if(TokenContext.GetToken().IsNextWhiteSpace()) {
 //				Tree = TokenContext.ParsePattern(NameSpace, "$StringLiteral$", Optional);
 //				if(Tree == null) {
 //					Tree = TokenContext.ParsePattern(NameSpace, "$ShellExpression$", Optional);
 //				}
 //			}
 //			if(Tree == null) {
 //				Tree = TokenContext.ParsePattern(NameSpace, "$Expression$", Optional);
 //			}
 //			NewTree.AppendParsedTree2(Tree);
 //		}
 //		TokenContext.ParseFlag = ParseFlag;
 //		return NewTree;
 //	}
 //
 //	public static GtNode TypeIdeShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 //		/*local*/CommandNode Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 //		/*local*/GtNode HeadNode = Node;
 //		/*local*/int i = 0;
 //		/*local*/String Command = ParsedTree.KeyToken.ParsedText;
 //		/*local*/GtNode ThisNode = Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Command);
 //		Node.Append(ThisNode);
 //		while(i < LibGreenTea.ListSize(ParsedTree.SubTreeList)) {
 //			/*local*/GtNode ExprNode = ParsedTree.TypeCheckAt(i, Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 //			if(ExprNode instanceof ConstNode) {
 //				/*local*/ConstNode CNode = (/*cast*/ConstNode) ExprNode;
 //				if(CNode.ConstValue instanceof String) {
 //					/*local*/String Val = (/*cast*/String) CNode.ConstValue;
 //					if(Val.equals("|")) {
 //						LibGreenTea.DebugP("PIPE");
 //						/*local*/CommandNode PrevNode = Node;
 //						Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(ContextType, ParsedTree, null);
 //						PrevNode.PipedNextNode = Node;
 //					}
 //				}
 //			}
 //			Node.Append(ExprNode);
 //			i = i + 1;
 //		}
 //		return HeadNode;
 //	}
 
 	private static String CommandSymbol(String Symbol) {
 		return "__$" + Symbol;
 	}
 	
 	private static void AppendCommand(GtNameSpace NameSpace, String CommandPath, GtToken SourceToken) {
 		if(CommandPath.length() > 0) {
 			int loc = CommandPath.lastIndexOf('/');
 			String Command = CommandPath;
 			if(loc != -1) {
 				Command = CommandPath.substring(loc+1);
 			}
 			if(LibGreenTea.EqualsString(Command, "*")) {
 				//TODO
 			}
 			else {
 				NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell$"), SourceToken);
 				NameSpace.SetSymbol(CommandSymbol(Command), CommandPath, null);
 				if(!LibGreenTea.IsUnixCommand(CommandPath)) {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "unknown command: " + CommandPath);
 				}
 			}
 		}
 	}
 	
 	public static GtSyntaxTree ParseCommand(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("command"), null);
 		/*local*/String Command = "";
 		/*local*/GtToken SourceToken = null;
 		while(TokenContext.HasNext()) {
 			/*local*/GtToken Token = TokenContext.Next();
 			if(Token.EqualsText(",")) {
 				Token.ParsedText = "";
 			}
 			if(Token.IsDelim() || Token.IsIndent()) {
 				break;
 			}
 			SourceToken = Token;
 			Command += Token.ParsedText;
 			if(Token.IsNextWhiteSpace()) {
 				AppendCommand(NameSpace, Command, SourceToken);
 				Command = "";
 				if(SourceToken.IsError()) {
 					CommandTree.ToError(SourceToken);
 				}
 			}
 		}
 		AppendCommand(NameSpace, Command, SourceToken);
 		if(SourceToken.IsError()) {
 			CommandTree.ToError(SourceToken);
 		}
 		return CommandTree;
 	}
 
 	public static GtSyntaxTree ParseEnv(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = new GtSyntaxTree(Pattern, NameSpace, TokenContext.GetMatchedToken("letenv"), null);
 		GtToken Token = TokenContext.Next();
 		if(!LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 			return TokenContext.ReportExpectedMessage(Token, "name", true);
 		}
 		String Name = Token.ParsedText;
 		String Env = System.getenv(Name);
 		if(TokenContext.MatchToken("=")) {
 			GtSyntaxTree ConstTree = GtStatic.ParseExpression(NameSpace, TokenContext, false);
 			if(GtStatic.IsMismatchedOrError(ConstTree)) {
 				return ConstTree;
 			}
 			if(Env == null) {
 				GtTypeEnv Gamma = new GtTypeEnv(NameSpace);
 				GtNode ConstNode = ConstTree.TypeCheck(Gamma, Gamma.StringType, DefaultTypeCheckPolicy);
 				Env = (/*cast*/String)ConstNode.ToConstValue(true);
 			}
 		}
 		if(Env == null) {
 			NameSpace.Context.ReportError(ErrorLevel, Token, "undefined environment variable: " + Name);
 			CommandTree.ToError(Token);
 		}
 		else {
 			NameSpace.SetSymbol(Name, Env, Token);
 			CommandTree.ToConstTree(Env);
 		}
 		return CommandTree;
 	}
 
 	public static GtSyntaxTree ParseDShell(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken CommandToken = TokenContext.Next();
 		/*local*/GtSyntaxTree CommandTree = new GtSyntaxTree(Pattern, NameSpace, CommandToken, null);
 		/*local*/String Command = (/*cast*/String)NameSpace.GetSymbol(CommandSymbol(CommandToken.ParsedText));
 		if(Command == null) {
 			//TODO()
 		}
 		/*local*/ArrayList<String> CommandLine = new ArrayList<String>();
 		/*local*/String Argument = "";
 		/*local*/boolean FoundOpen = false;
 		while(TokenContext.HasNext()) {
 			GtToken Token = TokenContext.GetToken();
 			if(Token.EqualsText("||") || Token.EqualsText("&&")) {
				/*local*/GtSyntaxPattern ExtendedPattern = TokenContext.GetExtendedPattern(NameSpace);
 				return GtStatic.ApplySyntaxPattern(NameSpace, TokenContext, CommandTree, ExtendedPattern);
 			}
 			if(Token.IsDelim() || Token.IsIndent()) {
 				break;
 			}
 			Token = TokenContext.Next();
 			if(Token.EqualsText("{")) {
 				FoundOpen = true;
 			}
 			if(Token.EqualsText("}")) {
 				FoundOpen = false;
 			}
 			if(Token.EqualsText("$")) {   // $HOME/hoge
 				GtToken Token2 = TokenContext.GetToken();
 				if(LibGreenTea.IsVariableName(Token2.ParsedText, 0)) {
 					Object Env = NameSpace.GetSymbol(Token2.ParsedText);
 					if(Env instanceof String) {
 						Argument += Env.toString();	
 					}
 					else {
 						Argument += "${" + Token2.ParsedText + "}";
 					}
 					TokenContext.Next();
 					continue;
 				}
 			}
 			if(!FoundOpen && Token.IsNextWhiteSpace() ) {
 				if(Argument.length() > 0) {
 					CommandLine.add((Argument + Token.ParsedText));
 				}
 				else {
 					CommandLine.add(Token.ParsedText);
 				}
 				Argument = "";
 			}
 			else {
 				Argument += Token.ParsedText;
 			}
 		}
 		if(Argument.length() > 0) {
 			CommandLine.add(Argument);
 		}
 		CommandTree.ConstValue = CommandLine.toArray(new String[CommandLine.size()]);
 		return CommandTree;
 	}
 
 	public static GtNode TypeDShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String[] CommandLine = (/*cast*/String[])ParsedTree.ConstValue;
 		return DShellGrammar.GenerateCommandNode(Gamma, ParsedTree, ContextType, ParsedTree.KeyToken.ParsedText, 0, CommandLine);
 	}
 
 	private static GtNode GenerateCommandNode(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType, String Command, int StartIndex, String[] CommandLine) {
 		/*local*/CommandNode Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(Gamma.VoidType, ParsedTree, null);
 		Node.Append(Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Command));
 		int i = StartIndex;
 		while(i < CommandLine.length) {
 			String Argument = CommandLine[i];
 			if(Argument.indexOf("${") != -1) {
 				// TODO string interpolation
 			}
 			else if(Argument.indexOf("*") != -1) {
 				String[] ExpandedArguments = DShellGrammar.ExpandPath(Argument);
 				int j = 0;
 				while(j < ExpandedArguments.length) {
 					Node.Append(Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, ExpandedArguments[j]));					
 					j = j + 1;
 				}
 				if(ExpandedArguments.length == 0) {
 					Gamma.Context.ReportError(InfoLevel, ParsedTree.KeyToken, "no file: " + Argument);
 				}
 			}
 			else if(LibGreenTea.EqualsString(Argument, "|")) {
 				Node.PipedNextNode = DShellGrammar.GenerateCommandNode(Gamma, ParsedTree, ContextType, CommandLine[i+1], i+2, CommandLine);
 				return Node;
 			}
 			else {
 				Node.Append(Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, CommandLine[i]));
 			}
 			i += 1;
 		}
 		if(ContextType.IsStringType() || ContextType.IsBooleanType()) {
 			Node.Type = ContextType;
 		}
 		return Node;
 	}
 	
 	private final static String FileOperators = "-f -x -d";
 	private final static String StopTokens = ";,)]}&&||";
 	
 	private static String ParseFilePath(GtNameSpace NameSpace, GtTokenContext TokenContext) {
 		String Path = "";
 		boolean FoundOpen = false;
 		while(TokenContext.HasNext()) {
 			GtToken Token = TokenContext.GetToken();
 			if(Token.IsIndent() || (!FoundOpen && StopTokens.indexOf(Token.ParsedText) != -1)) {
 				break;
 			}
 			TokenContext.Next();
 			if(Token.EqualsText("$")) {   // $HOME/hoge
 				GtToken Token2 = TokenContext.GetToken();
 				if(LibGreenTea.IsVariableName(Token2.ParsedText, 0)) {
 					Object Env = NameSpace.GetSymbol(Token2.ParsedText);
 					if(Env instanceof String) {
 						Path += Env.toString();	
 					}
 					else {
 						Path += "${" + Token2.ParsedText + "}";
 					}
 					TokenContext.Next();
 					continue;
 				}
 			}
 			if(Token.EqualsText("{")) {
 				FoundOpen = true;
 			}
 			if(Token.EqualsText("}")) {
 				FoundOpen = false;
 			}
 			Path += Token.ParsedText;
 			if(!FoundOpen && Token.IsNextWhiteSpace()) {
 				break;
 			}
 		}
 		return Path;
 	}
 	
 	public static GtSyntaxTree ParseOpFile(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtToken Token = TokenContext.Next();
 		/*local*/GtToken Token2 = TokenContext.Next();
 		if(!Token.IsNextWhiteSpace()) {
 			if(FileOperators.indexOf(Token2.ParsedText) != -1) {
 				String Path = ParseFilePath(NameSpace, TokenContext);
 				if(Path.length() > 0) {
 					Token.ParsedText += Token2.ParsedText;
 					/*local*/GtSyntaxTree Tree = new GtSyntaxTree(Pattern, NameSpace, Token, null);
 					Tree.ConstValue = Path;
 //					/*local*/GtSyntaxTree SubTree = new GtSyntaxTree(Pattern, NameSpace, Token2, null);
 //					Tree.SetSyntaxTreeAt(UnaryTerm, SubTree);
 					return Tree;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static GtNode TypeOpFile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String FileOperator = ParsedTree.KeyToken.ParsedText;
 		/*local*/String FilePath = (/*cast*/String)ParsedTree.ConstValue;
 		/*local*/GtNode OpNode  = Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, FileOperator + " " + FilePath);
 		return OpNode;
 	}
 
 	
 	public static String[] ExpandPath(String Path) {
 		return new String[0]; //if not found
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		/*local*/GtParserContext ParserContext = NameSpace.Context;
 		NameSpace.AppendSyntax("-", LoadParseFunc(ParserContext, this, "ParseOpFile"), LoadTypeFunc(ParserContext, this, "TypeOpFile"));
 		NameSpace.AppendSyntax("letenv", LoadParseFunc(ParserContext, this, "ParseEnv"), null);
 		NameSpace.AppendSyntax("command", LoadParseFunc(ParserContext, this, "ParseCommand"), null);
 		NameSpace.AppendSyntax("$DShell$", LoadParseFunc(ParserContext, this, "ParseDShell"), LoadTypeFunc(ParserContext, this, "TypeDShell"));
 	}
 
 }
