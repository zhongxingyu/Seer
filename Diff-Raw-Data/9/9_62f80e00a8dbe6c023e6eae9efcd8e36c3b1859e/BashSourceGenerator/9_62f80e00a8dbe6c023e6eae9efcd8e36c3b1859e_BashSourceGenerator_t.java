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
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*field*/boolean inFunc = false;
 	/*field*/boolean inMainFunc = false;
 
 	BashSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.TrueLiteral  = "0";
 		this.FalseLiteral = "1";
 		this.NullLiteral = LibGreenTea.QuoteString("__NULL__");
 		this.MemberAccessOperator = "__MEMBER__";
 		this.LineComment = "##";
 		this.ParameterDelimiter = "";
 	}
 
 	@Override public void InitContext(GtParserContext Context) {
 		super.InitContext(Context);
 		this.WriteLineHeader("#!/bin/bash");
 		this.WriteLineCode(this.LineFeed + "source $GREENTEA_HOME/include/bash/GreenTeaPlus.sh" + this.LineFeed);
 	}
 
 	@Override public String VisitBlockWithIndent(GtNode Node, boolean NeedBlock) {
 		return this.VisitBlockWithOption(Node, true, NeedBlock, false);	//actually NeedBlock -> allowDummyBlock
 	}
 
 	private String VisitBlockWithReplaceBreak(GtNode Node, boolean allowDummyBlock) {
 		return this.VisitBlockWithOption(Node, true, allowDummyBlock, true);
 	}
 
 	private String VisitBlockWithoutIndent(GtNode Node, boolean allowDummyBlock) {
 		return this.VisitBlockWithOption(Node, false, allowDummyBlock, false);
 	}
 
 	private String VisitBlockWithOption(GtNode Node, boolean inBlock, boolean allowDummyBlock, boolean replaceBreak) {
 		/*local*/String Code = "";
 		/*local*/boolean isBreakReplaced = false;
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		if(this.IsEmptyBlock(Node) && allowDummyBlock) {
 			Code += this.GetIndentString() + "echo dummy block!! &> /dev/zero" + this.LineFeed;
 		}
 		while(!this.IsEmptyBlock(CurrentNode)) {
 			/*local*/String poppedCode = this.VisitNode(CurrentNode);
 			if(replaceBreak && CurrentNode instanceof BreakNode) {
 				isBreakReplaced = true;
 				poppedCode = ";;";
 			}
 			if(!LibGreenTea.EqualsString(poppedCode, "")) {
 				Code += this.GetIndentString() + poppedCode + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.GetNextNode();
 		}
 		if(replaceBreak && !isBreakReplaced) {
 			Code += this.GetIndentString() + ";&" + this.LineFeed;
 		}
 		if(inBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString();
 		}
 		else {
 			if(Code.length() > 0) {
 				Code = LibGreenTea.SubString(Code, 0, Code.length() - 1);
 			}
 		}
 		return Code;
 	}
 
 	@Override public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		/*
 		 * do { Block } while(Cond)
 		 * => while(True) { Block; if(!Cond) { break; } }
 		 */
 		/*local*/GtNode Break = this.CreateBreakNode(Type, ParsedTree, null);
		/*local*/GtPolyFunc PolyFunc = ParsedTree.NameSpace.GetMethod(Cond.Type, "!", true);
 		/*local*/GtTypeEnv Gamma = new GtTypeEnv(ParsedTree.NameSpace);
 		/*local*/GtFunc Func = null;
 		if(PolyFunc != null) {
 			Func = PolyFunc.ResolveUnaryFunc(Gamma, ParsedTree, Cond);
 		}
 		Cond = this.CreateUnaryNode(Type, ParsedTree, Func, Cond);
 		/*local*/GtNode IfBlock = this.CreateIfNode(Type, ParsedTree, Cond, Break, null);
 		GtStatic.LinkNode(Block.MoveTailNode(), IfBlock);
 		/*local*/GtNode TrueNode = this.CreateConstNode(ParsedTree.NameSpace.Context.BooleanType, ParsedTree, true);
 		return this.CreateWhileNode(Type, ParsedTree, TrueNode, Block);
 	}
 	
 	@Override public GtNode CreateSelfAssignNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Left, GtNode Right) {
 		/*local*/GtNode NewBinary = this.CreateBinaryNode(Type, ParsedTree, Func, Left, Right);
 		return this.CreateAssignNode(Type, ParsedTree, Left, NewBinary);
 	}
 
 	private String ResolveCondition(GtNode Node) {
 		/*local*/String Cond = this.VisitNode(Node);
 		if(LibGreenTea.EqualsString(Cond, this.TrueLiteral)) {
 			Cond = "((1 == 1))";
 		}
 		else if(LibGreenTea.EqualsString(Cond, this.FalseLiteral)) {
 			Cond = "((1 != 1))";
 		}
 		return Cond;
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.ResolveCondition(Node.CondExpr) + " ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.ResolveCondition(Node.CondExpr);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " )) ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		/*local*/String Variable = this.VisitNode(Node.Variable);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for " + Variable + " in " + "${" + Iter + "[@]} ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	private String[] MakeParamCode(ArrayList<GtNode> ParamList, boolean isAssert) {
 		/*local*/int Size = LibGreenTea.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size];
 		/*local*/int i = 0;
 		while(i < Size) {
 			/*local*/GtNode ParamNode = ParamList.get(i);
 			if(isAssert) {
 				ParamCode[i] = this.ResolveCondition(ParamNode);
 			}
 			else {
 				ParamCode[i] = this.ResolveValueType(ParamNode, false);
 			}
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	private boolean FindAssert(GtFunc Func) {
 		/*local*/boolean isAssert = false;
 		if(Func != null && Func.Is(NativeMacroFunc)) {
 			if(LibGreenTea.EqualsString(Func.FuncName, "assert")) {
 				isAssert = true;
 			}
 		}
 		return isAssert;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/int ParamSize = LibGreenTea.ListSize(Node.NodeList);
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		/*local*/boolean isAssert = this.FindAssert(Node.Func);
 		if(isAssert) {
 			Template = "assert " + LibGreenTea.QuoteString("$1");
 		}
 		/*local*/String[] ParamCode = this.MakeParamCode(Node.NodeList, isAssert);
 		this.PushSourceCode(this.ApplyMacro2(Template, ParamCode));
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/GtFunc Func = Node.Func;
 		/*local*/String Expr = this.ResolveValueType(Node.Expr, false);	//TODO: support ++ --
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			Macro = "((" + FuncName + " $1))";
 		}
 		this.PushSourceCode(Macro.replace("$1", Expr));
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/GtFunc Func = Node.Func;
 		/*local*/String Left = this.ResolveValueType(Node.LeftNode, false);
 		/*local*/String Right = this.ResolveValueType(Node.RightNode, false);
 		/*local*/String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
 			}
 		}
 		if(Macro == null) {
 			Macro = "(($1 " + FuncName + " $2))";
 		}
 		this.PushSourceCode(Macro.replace("$1", Left).replace("$2", Right));
 	}
 
 	private String GetMemberIndex(GtType ClassType, String MemberName) {
 		return "$" + ClassType.ShortClassName + this.MemberAccessOperator + MemberName;
 	}
 
 	private boolean IsNativeType(GtType Type) {
 		if(Type != null && Type.IsNative()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.GetMemberIndex(Node.Expr.Type, Node.Func.FuncName) + "]");
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.ResolveValueType(Node.GetAt(0), false) + "]");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " && " + this.ResolveCondition(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " || " + this.ResolveCondition(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.LeftNode) + "=" + this.ResolveValueType(Node.RightNode, true));
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		/*local*/String VarName = Node.NativeName;
 		/*local*/String Declare = "declare ";
 		/*local*/String Option = "";
 		if(this.inFunc) {
 			Declare = "local ";
 		}
 		if(!this.IsNativeType(Node.DeclType)) {
 			Option = "-a ";
 		}
 		
 		/*local*/String Code = Declare + Option + VarName + this.LineFeed;
 		Code += this.GetIndentString() + VarName;
 		if(Node.InitNode != null) {
 			Code += "=" + this.ResolveValueType(Node.InitNode, true);
 		} 
 		Code +=  this.LineFeed;
 		this.PushSourceCode(Code + this.VisitBlockWithoutIndent(Node.BlockNode, false));
 	}
 
 	@Override public void VisitTrinaryNode(TrinaryNode Node) {
 		/*local*/String CondExpr = this.ResolveCondition(Node.CondExpr);
 		/*local*/String Then = this.ResolveValueType(Node.ThenExpr, false);
 		/*local*/String Else = this.ResolveValueType(Node.ElseExpr, false);
 		this.PushSourceCode("((" + CondExpr + " ? " + Then + " : " + Else + "))");
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.ResolveCondition(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true);
 		/*local*/String Code = "if " + CondExpr + " ;then" + this.LineFeed + ThenBlock;
 		if(!this.IsEmptyBlock(Node.ElseNode)) {
 			Code += "else" + this.LineFeed + this.VisitBlockWithIndent(Node.ElseNode, false);
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		/*local*/String Match = this.ResolveValueType(Node.MatchNode, false);
 		/*local*/String Code = "case " + Match + " in" + this.LineFeed + this.GetIndentString();
 		/*local*/int i = 0;
 		while(i < LibGreenTea.ListSize(Node.CaseList)) {
 			/*local*/GtNode Case  = Node.CaseList.get(i);
 			/*local*/GtNode Block = Node.CaseList.get(i+1);
 			Code += this.VisitNode(Case) + ")" + this.LineFeed;
 			Code += this.VisitBlockWithReplaceBreak(Block, true);
 			i = i + 2;
 		}
 		if(Node.DefaultBlock != null) {
 			Code += "*)" + this.LineFeed;
 			Code += this.VisitBlockWithReplaceBreak(Node.DefaultBlock, false);
 		}
 		Code += "esac";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(!this.inFunc) {
 			return;
 		}
 		
 		if(Node.Expr != null) {
 			/*local*/String Ret = this.ResolveValueType(Node.Expr, false);
 			if(Node.Type.equals(Node.Type.Context.BooleanType) || 
 					(Node.Type.equals(Node.Type.Context.IntType) && this.inMainFunc)) {
 				this.PushSourceCode("return " + Ret);
 				return;
 			}
 			this.PushSourceCode("echo " + Ret + this.LineFeed + this.GetIndentString() + "return 0");
 			return;
 		}
 		this.PushSourceCode("return 0");
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/GtNode TrueNode = new ConstNode(Node.Type.Context.BooleanType, null, true);
 		/*local*/String Code = "trap ";
 		/*local*/String Try = this.VisitNode(new IfNode(null, null, TrueNode, Node.TryBlock, null));
 		/*local*/String Catch = this.VisitNode(new IfNode(null, null, TrueNode, Node.CatchBlock, null));
 		Code += LibGreenTea.QuoteString(Catch) + " ERR" + this.LineFeed;
 		Code += this.GetIndentString() + Try + this.LineFeed + this.GetIndentString() + "trap ERR";
 		if(Node.FinallyBlock != null) {
 			/*local*/String Finally = this.VisitNode(new IfNode(null, null, TrueNode, Node.FinallyBlock, null));
 			Code += this.LineFeed + this.GetIndentString() + Finally;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		this.PushSourceCode("kill &> /dev/zero");
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		this.PushSourceCode("echo " + LibGreenTea.QuoteString(Node.Token.ParsedText) + " >&2");
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "";
 		/*local*/GtType Type = Node.Type;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			Code += this.AppendCommand(CurrentNode);
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		
 		if(Type.equals(Type.Context.StringType)) {
 			Code = "execCommadString " + LibGreenTea.QuoteString(Code);
 		}
 		else if(Type.equals(Type.Context.BooleanType)) {
 			Code = "execCommadBool " + LibGreenTea.QuoteString(Code);
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String AppendCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = LibGreenTea.ListSize(CurrentNode.Params);
 		/*local*/int i = 0;
 		while(i < size) {
 			Code += this.ResolveValueType(CurrentNode.Params.get(i), false) + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	private boolean CheckConstFolding(GtNode TargetNode) {
 		if(TargetNode instanceof ConstNode) {
 			return true;
 		}
 		else if(TargetNode instanceof UnaryNode) {
 			/*local*/UnaryNode Unary = (/*cast*/UnaryNode) TargetNode;
 			return this.CheckConstFolding(Unary.Expr);
 		}
 		else if(TargetNode instanceof BinaryNode) {
 			/*local*/BinaryNode Binary = (/*cast*/BinaryNode) TargetNode;
 			if(this.CheckConstFolding(Binary.LeftNode) && this.CheckConstFolding(Binary.RightNode)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private String ResolveValueType(GtNode TargetNode, boolean isAssign) {
 		/*local*/String ResolvedValue;
 		/*local*/String Value = this.VisitNode(TargetNode);
 		/*local*/GtType Type = TargetNode.Type;
 		
 		// resolve constant folding
 		if(this.CheckConstFolding(TargetNode)) {
 			return Value;
 		}
 		
 		// resolve boolean function
 		if(Type != null && Type.equals(Type.Context.BooleanType)) {
 			if(TargetNode instanceof ApplyNode || TargetNode instanceof UnaryNode || 
 					TargetNode instanceof CommandNode || TargetNode instanceof BinaryNode) {
 				return "$(valueOfBool " + LibGreenTea.QuoteString(Value) + ")";
 			}
 		}
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			return Value;
 		}
 		else if(TargetNode instanceof IndexerNode || TargetNode instanceof GetterNode) {
 			ResolvedValue = "${" + Value + "}";
 		}
 		else if(TargetNode instanceof ApplyNode || TargetNode instanceof CommandNode || TargetNode instanceof NewNode) {
 			ResolvedValue = "$(" + Value + ")";
 		}
 		else if(TargetNode instanceof LocalNode && !this.IsNativeType(Type)) {
 			/*local*/LocalNode Local = (/*cast*/LocalNode) TargetNode;
 			/*local*/String Name = Local.NativeName;
 			ResolvedValue = "${" + Value + "[@]}";
 			if(Name.length() == 1 && LibGreenTea.IsDigit(Name, 0)) {
 				ResolvedValue = "$" + Value;
 			}
 		}
 		else {
 			ResolvedValue = "$" + Value;
 		}
 		
 		// resolve assigned object
 		if(isAssign) {
 			if(!this.IsNativeType(Type)) {
 				ResolvedValue = "(" + ResolvedValue + ")";
 				return ResolvedValue;
 			}
 		}
 		
 		// resolve string and object value
 		if(Type != null) {
 			if(Type.equals(Type.Context.StringType) || !this.IsNativeType(Type)) {
 				ResolvedValue = LibGreenTea.QuoteString(ResolvedValue);
 			}
 		}
 		return ResolvedValue;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/String Function = "";
 		/*local*/String FuncName = Func.GetNativeFuncName();
 		this.inFunc = true;
 		if(LibGreenTea.EqualsString(FuncName, "main")) {
 			this.inMainFunc = true;
 		}
 		Function += FuncName + "() {" + this.LineFeed;
 		this.Indent();
 
 		/*local*/int i = 0;
 		while(i < ParamNameList.size()) {
 			/*local*/GtType ParamType = Func.GetFuncParamType(i);
 			// "local -a x"
 			Function += this.GetIndentString() + "local ";
 			if(!this.IsNativeType(ParamType)) {
 				Function += "-a ";
 			}
 			Function += ParamNameList.get(i) + ";" + this.LineFeed + this.GetIndentString();
 			// "x = $1"
 			Function += ParamNameList.get(i) + "=$" + (i + 1) + ";" + this.LineFeed;
 			i = i + 1;
 		}
 		;
 		Function += this.VisitBlockWithoutIndent(Body, true) + this.LineFeed;
 		this.UnIndent();
 		Function += this.GetIndentString() + "}" + this.LineFeed;
 		this.WriteLineCode(Function);
 		this.inFunc = false;
 		this.inMainFunc = false;
 	}
 
 	@Override protected String GetNewOperator(GtType Type) {
 		return LibGreenTea.QuoteString("$(__NEW__" + Type.ShortClassName + ")");
 	}
 
 	@Override public void OpenClassField(GtType Type, GtClassField ClassField) {	//TODO: support super
 		/*local*/String Program = "__NEW__" + Type.ShortClassName + "() {" + this.LineFeed;
 		this.WriteLineCode("#### define class " + Type.ShortClassName + " ####");
 		this.Indent();
 		Program += this.GetIndentString() + "local -a " + this.GetRecvName() + this.LineFeed;
 
 		/*local*/int i = 0;
 		while(i < LibGreenTea.ListSize(ClassField.FieldList)) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = "NULL";
 			}
 			this.WriteLineCode(Type.ShortClassName + this.MemberAccessOperator + FieldInfo.NativeName + "=" + i);
 			
 			Program += this.GetIndentString() + this.GetRecvName();
 			Program += "[" + this.GetMemberIndex(Type, FieldInfo.NativeName) + "]=" + InitValue + this.LineFeed;
 			i = i + 1;
 		}
 		Program += this.GetIndentString() + "echo ";
 		Program += LibGreenTea.QuoteString("${" + this.GetRecvName() + "[@]}") + this.LineFeed;
 		this.UnIndent();
 		Program += "}";
 		
 		this.WriteLineCode(this.LineFeed + Program);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithoutIndent(Node, false);
 		if(!LibGreenTea.EqualsString(Code, "")) {
 			this.WriteLineCode(Code);
 		}
 		return Code;
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName);
 	}
 }
