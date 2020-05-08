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
 				if(LibGreenTea.IsUnixCommand(CommandPath)) {
 					NameSpace.SetSymbol(Command, NameSpace.GetSyntaxPattern("$DShell$"), SourceToken);
 					NameSpace.SetSymbol(CommandSymbol(Command), CommandPath, null);
 				} 
 				else {
 					NameSpace.Context.ReportError(ErrorLevel, SourceToken, "unknown command: " + CommandPath);
 				}
 			}
 		}
 	}
 	
 	public static GtSyntaxTree ParseCommand(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
 		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "command");
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
 		/*local*/GtSyntaxTree CommandTree = TokenContext.CreateMatchedSyntaxTree(NameSpace, Pattern, "letenv");
 		GtToken Token = TokenContext.Next();
 		if(!LibGreenTea.IsVariableName(Token.ParsedText, 0)) {
 			return TokenContext.ReportExpectedMessage(Token, "name", true);
 		}
 		String Name = Token.ParsedText;
 		String Env = LibGreenTea.GetEnv(Name);
 		if(TokenContext.MatchToken("=")) {
 			GtSyntaxTree ConstTree = GreenTeaUtils.ParseExpression(NameSpace, TokenContext, false);
 			if(GreenTeaUtils.IsMismatchedOrError(ConstTree)) {
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
 				return GreenTeaUtils.ApplySyntaxPattern(NameSpace, TokenContext, CommandTree, ExtendedPattern);
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
 		CommandTree.ParsedValue = CommandLine.toArray(new String[CommandLine.size()]);
 		return CommandTree;
 	}
 
 	public static GtNode TypeDShell(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
 		/*local*/String[] CommandLine = (/*cast*/String[])ParsedTree.ParsedValue;
 		return DShellGrammar.GenerateCommandNode(Gamma, ParsedTree, ContextType, ParsedTree.KeyToken.ParsedText, 0, CommandLine);
 	}
 
 	private static GtNode GenerateCommandNode(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType, String Command, int StartIndex, String[] CommandLine) {
 		/*local*/CommandNode Node = (/*cast*/CommandNode) Gamma.Generator.CreateCommandNode(Gamma.VoidType, ParsedTree, null);
 		if(ContextType.IsStringType() || ContextType.IsBooleanType()) {
 			Node.Type = ContextType;
 		}
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
 		return Node;
 	}
 	
 	private final static String FileOperators = "-d -e -f -r -w -x";
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
 	
 	private static boolean EvalFileOp(String FileOp, String Path) {
 		if(LibGreenTea.EqualsString(FileOp, "-d")) {
 			return LibGreenTea.IsDirectory(Path);
 		}
 		else if(LibGreenTea.EqualsString(FileOp, "-e")) {
 			return LibGreenTea.IsExist(Path);
 		}
 		else if(LibGreenTea.EqualsString(FileOp, "-f")) {
 			return LibGreenTea.IsFile(Path);
 		}
 		else if(LibGreenTea.EqualsString(FileOp, "-r")) {
 			return LibGreenTea.IsReadable(Path);
 		}
 		else if(LibGreenTea.EqualsString(FileOp, "-w")) {
 			return LibGreenTea.IsWritable(Path);
 		}
 		else if(LibGreenTea.EqualsString(FileOp, "-x")) {
 			return LibGreenTea.IsExecutable(Path);
 		}
 		return false;
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
 					Tree.ParsedValue = EvalFileOp(Token.ParsedText, Path);
 //					/*local*/GtSyntaxTree SubTree = new GtSyntaxTree(Pattern, NameSpace, Token2, null);
 //					Tree.SetSyntaxTreeAt(UnaryTerm, SubTree);
 					return Tree;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static GtNode TypeOpFile(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType ContextType) {
		/*local*/boolean Value = (/*cast*/boolean)ParsedTree.ParsedValue;
		/*local*/GtNode OpNode  = Gamma.Generator.CreateConstNode(Gamma.StringType, ParsedTree, Value);
		return OpNode;
 	}
 
 	
 	public static String[] ExpandPath(String Path) {
 		/*local*/String[] ExpanddedPaths = LibGreenTea.GetFileList(Path);
 		if(ExpanddedPaths != null) {
 			return ExpanddedPaths;
 		}
 		return new String[0];
 	}
 
 	@Override public void LoadTo(GtNameSpace NameSpace) {
 		/*local*/GtParserContext ParserContext = NameSpace.Context;
 		NameSpace.AppendSyntax("-", LoadParseFunc(ParserContext, this, "ParseOpFile"), LoadTypeFunc(ParserContext, this, "TypeOpFile"));
 		NameSpace.AppendSyntax("letenv", LoadParseFunc(ParserContext, this, "ParseEnv"), null);
 		NameSpace.AppendSyntax("command", LoadParseFunc(ParserContext, this, "ParseCommand"), null);
 		NameSpace.AppendSyntax("$DShell$", LoadParseFunc(ParserContext, this, "ParseDShell"), LoadTypeFunc(ParserContext, this, "TypeDShell"));
 	}
 
 }
