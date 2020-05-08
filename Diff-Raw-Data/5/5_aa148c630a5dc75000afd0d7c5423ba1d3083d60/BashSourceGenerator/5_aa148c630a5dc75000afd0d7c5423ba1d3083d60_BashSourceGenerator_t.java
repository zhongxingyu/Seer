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
 import java.util.ArrayList;
 //endif VAJA
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*field*/boolean inFunc = false;
 	/*field*/int cmdCounter = 0;
 
 	BashSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 	}
 
 	@Override public void InitContext(GtClassContext Context) {
 		super.InitContext(Context);
 		this.WriteLineHeader("#!/bin/bash");
 		this.WriteLineCode(this.LineFeed + "source ./efunc.sh" + this.LineFeed);
 	}
 	
 	public String VisitBlockWithIndent(GtNode Node, boolean inBlock) {
 		/*local*/String Code = "";
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			/*local*/String poppedCode = this.VisitNode(CurrentNode);
 			if(!poppedCode.equals("")) {
 				Code += this.GetIndentString() + poppedCode + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(inBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString();
 		}
 		else {
 			if(Code.length() > 0) {
 				Code = Code.substring(0, Code.length() - 1);
 			}
 		}
 		return Code;
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + this.VisitNode(Node.IndexAt) + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// not support
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.VisitNode(Node.CondExpr) + " ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		/*local*/String LoopBody = this.VisitBlockWithIndent(Node.LoopBody, true);
 		/*local*/String Program = "if true ;then" + this.LineFeed + LoopBody + "fi" + this.LineFeed;
 		Program += "while " + this.VisitNode(Node.CondExpr) + " ;do" + this.LineFeed;
 		Program += LoopBody + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.VisitNode(Node.CondExpr);
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
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String value = this.StringfyConstValue(Node.ConstValue);
 		if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 			if(value.equals("true")) {
 				value = "0";
 			}
 			else if(value.equals("false")) {
 				value = "1";
 			}
 		}
 		this.PushSourceCode(value);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 //		/*local*/String Type = Node.Type.ShortClassName;
 //		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.NativeName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		this.PushSourceCode(this.VisitNode(Node.Expr) + "[" + Node.Func.FuncName + "]");
 	}
 	
 	private String[] MakeParamCode1(GtNode Node) {
 		/*local*/String[] ParamCode = new String[1];
 		ParamCode[0] = this.VisitNode(Node);
 		return ParamCode;
 	}
 	
 	private String[] MakeParamCode2(GtNode Node, GtNode Node2) {
 		/*local*/String[] ParamCode = new String[2];
 		ParamCode[0] = this.ResolveValueType(Node);
 		ParamCode[1] = this.ResolveValueType(Node2);
 		return ParamCode;
 	}
 	
 	private String[] MakeParamCode(ArrayList<GtNode> ParamList) {
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			/*local*/GtNode Node = ParamList.get(i);
 			ParamCode[Size - i - 1] = this.ResolveValueType(Node);
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	private String JoinCode(String BeginCode, int BeginIdx, String[] ParamCode, String EndCode) {
 		/*local*/String JoinedCode = BeginCode;
 		/*local*/int i = BeginIdx;
 		while(i < ParamCode.length) {
 			/*local*/String P = ParamCode[i];
 			if(i != BeginIdx) {
 				JoinedCode += " ";
 			}
 			JoinedCode += P;
 			i = i + 1;
 		}
 		return JoinedCode + EndCode;
 	}
 	
 	private String CreateAssertFunc(ApplyNode Node) {
 		/*local*/ArrayList<GtNode> ParamList = Node.Params;
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			ParamCode[Size - i - 1] = "\"" + this.VisitNode(ParamList.get(i)) + "\"";
 			i = i + 1;
 		}
 		return this.JoinCode("assert ", 0, ParamCode, "");
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String[] ParamCode = this.MakeParamCode(Node.Params);
 		if(Node.Func == null) {
 			this.PushSourceCode(this.JoinCode(ParamCode[0] + " ", 0, ParamCode, ""));
 		}
 //		else if(Node.Func.Is(NativeFunc)) {
 //			this.PushSourceCode(this.JoinCode(ParamCode[0] + "." + Node.Func.FuncName + " ", 0, ParamCode, ""));
 //		}
 		else if(Node.Func.Is(NativeMacroFunc)) {
 			/*local*/String NativeMacro = (/*cast*/String) Node.Func.NativeRef;
 			if(NativeMacro.startsWith("assert")) {
 				this.PushSourceCode(this.CreateAssertFunc(Node));
 				return;
 			}
 			this.PushSourceCode(Node.Func.ApplyNativeMacro(0, ParamCode));
 		}
 		else {
 			this.PushSourceCode(this.JoinCode(Node.Func.GetNativeFuncName() + " ", 0, ParamCode, ""));
 		}
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		if(FuncName.equals("++")) {
 		}
 		else if(FuncName.equals("--")) {
 		}
 		else {
 			LibGreenTea.DebugP(FuncName + " is not supported suffix operator!!");
 		}
 		this.PushSourceCode("((" + this.VisitNode(Node.Expr) + FuncName + "))");
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		if(Node.Func == null) {
 			this.PushSourceCode("((" + Node.Token.ParsedText + this.VisitNode(Node.Expr) + "))");
 		}
 		else {
 			/*local*/String[] ParamCode = this.MakeParamCode1(Node.Expr);
 			this.PushSourceCode(Node.Func.ApplyNativeMacro(0, ParamCode));
 		}
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		if(Node.Func == null) {
 			/*local*/String Left = this.ResolveValueType(Node.LeftNode);
 			/*local*/String Right = this.ResolveValueType(Node.RightNode);
 			this.PushSourceCode("((" + Left + " " +  Node.Token.ParsedText + " " + Right + "))");
 		}
 		else {
 			/*local*/String[] ParamCode = this.MakeParamCode2(Node.LeftNode, Node.RightNode);
 			this.PushSourceCode(Node.Func.ApplyNativeMacro(0, ParamCode));
 		}
 		
 //		if(Node.Type.equals(Node.Type.Context.Float)) {	// support float value
 //			this.PushSourceCode("(echo \"scale=10; " + Left + " " + FuncName + " " + Right + "\" | bc)");
 //			return;
 //		}
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
		this.PushSourceCode("(" + this.ResolveValueType(Node.LeftNode) + " && " + this.ResolveValueType(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
		this.PushSourceCode("(" + this.ResolveValueType(Node.LeftNode) + " || " + this.ResolveValueType(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		/*local*/String Left = this.VisitNode(Node.LeftNode);
 		/*local*/String Right = this.ResolveValueType(Node.RightNode);
 		/*local*/String Head = "";
 		if(Node.LeftNode instanceof GetterNode) {
 			Head = "eval ";
 		}
 		this.PushSourceCode(Head + Left + "=" + Right);
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String VarName = Node.VariableName;
 		/*local*/String Code = "";
 		/*local*/String Head = "";
 		if(this.inFunc) {
 			Code += "local " + VarName + this.LineFeed + this.GetIndentString();
 		}
 		
 		if(Node.InitNode != null && Node.InitNode instanceof GetterNode) {
 			Head = "eval ";
 		}
 		Code += Head + VarName;
 		if(Node.InitNode != null) {
 			Code += "=" + this.ResolveValueType(Node.InitNode);
 		} 
 		Code +=  this.LineFeed;
 		this.PushSourceCode(Code + this.VisitBlockWithIndent(Node.BlockNode, false));
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true);
 		/*local*/String ElseBlock = this.VisitBlockWithIndent(Node.ElseNode, true);
 		/*local*/String Code = "if " + CondExpr + " ;then" + this.LineFeed + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "else" + this.LineFeed + ElseBlock;
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(this.inFunc && Node.Expr != null) {
 			/*local*/String Ret = this.ResolveValueType(Node.Expr);
 			if(Node.Expr instanceof ApplyNode || Node.Expr instanceof CommandNode) {
 				if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 					/*local*/String Code = "local value=" + Ret + this.LineFeed;
 					Code += this.GetIndentString() + "echo $value" + this.LineFeed;
 					Code += this.GetIndentString() + "return $value";
 					this.PushSourceCode(Code);
 					return;
 				}
 			}
 			this.PushSourceCode("echo " + Ret + this.LineFeed + this.GetIndentString() + "return 0");
 		}
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";	// not support label
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";	// not support label
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "trap ";
 		/*local*/String Try = this.VisitBlockWithIndent(Node.TryBlock, true);
 		/*local*/String Catch = this.VisitBlockWithIndent(Node.CatchBlock, true);
 		/*local*/String Finally = "";
 		if(Node.FinallyBlock != null) {
 			Finally = this.VisitBlockWithIndent(Node.FinallyBlock, true);
 			Finally = this.LineFeed + 
 					this.GetIndentString() + "if true ;then" + this.LineFeed + Finally + "fi";
 		}
 		Code += "\"if true ;then" + this.LineFeed + Catch + "fi\" ERR" + this.LineFeed;
 		Code += this.GetIndentString() + "if true ;then" + this.LineFeed + Try + "fi" + this.LineFeed;
 		Code += this.GetIndentString() + "trap ERR";
 		Code += Finally;
 		
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		this.PushSourceCode("kill &> /dev/zero");
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 //		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "";
 		/*local*/int count = 0;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(count > 0) {
 				Code += " | ";
 			}
 			Code += this.AppendCommand(CurrentNode);
 			count += 1;
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 		this.PushSourceCode(this.CreateCommandFunc(Code, Node.Type));
 	}
 
 	private String AppendCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		/*local*/int i = 0;
 		while(i < size) {
 			Code += this.ResolveValueType(CurrentNode.Params.get(i)) + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 	
 	private String CreateCommandFunc(String cmd, GtType Type) {
 		/*local*/String FuncName = "execCmd";
 		/*local*/String RunnableCmd = cmd;
 		if(Type.equals(Type.Context.StringType)) {
 			RunnableCmd = "function " + FuncName + this.cmdCounter + "() {" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "echo $(" + cmd + ")" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "return 0" + this.LineFeed + "}" + this.LineFeed;
 			this.WriteLineCode(RunnableCmd);
 			RunnableCmd = FuncName + this.cmdCounter;
 			this.cmdCounter++;
 		}
 		else if(Type.equals(Type.Context.IntType) || Type.equals(Type.Context.BooleanType)) {
 			RunnableCmd = "function " + FuncName + this.cmdCounter + "() {" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + cmd + " >&2" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "local ret=$?" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "echo $ret" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "return $ret" + this.LineFeed + "}" + this.LineFeed;
 			this.WriteLineCode(RunnableCmd);
 			RunnableCmd = FuncName + this.cmdCounter;
 			this.cmdCounter++;
 		}
 		return RunnableCmd;
 	}
 
 	private GtNode ResolveParamName(ArrayList<String> ParamNameList, GtNode Body) {
 		return this.ConvertParamName(ParamNameList, Body, 0);
 	}
 
 	private GtNode ConvertParamName(ArrayList<String> ParamNameList, GtNode Body, int index) {
 		if(ParamNameList == null || index  == ParamNameList.size()) {
 			return Body;
 		}
 
 		/*local*/GtNode oldVarNode = new LocalNode(null, null, "" + (index + 1));
 		/*local*/GtNode Let = new LetNode(null, null, null, ParamNameList.get(index), oldVarNode, null);
 		Let.NextNode = this.ConvertParamName(ParamNameList, Body, index + 1);
 		return Let;
 	}
 
 	private String ResolveValueType(GtNode TargetNode) {
 		/*local*/String ResolvedValue;
 		/*local*/String Value = this.VisitNode(TargetNode);
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			ResolvedValue = Value;
 		}
 		else if(TargetNode instanceof IndexerNode || TargetNode instanceof GetterNode) {
 			ResolvedValue = "${" + Value + "}";
 		}
 		else if(TargetNode instanceof ApplyNode || TargetNode instanceof CommandNode) {
 			ResolvedValue = "$(" + Value + ")";
 		}
 		else {
 			ResolvedValue = "$" + Value;
 		}
 		return ResolvedValue;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "function ";
 		this.inFunc = true;
 		Function += Func.GetNativeFuncName() + "() {" + this.LineFeed;
 		/*local*/String Block = this.VisitBlockWithIndent(this.ResolveParamName(ParamNameList, Body), true);
 		Function += Block + "}" + this.LineFeed;
 		this.WriteLineCode(Function);
 		this.inFunc = false;
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithIndent(Node, false);
 		if(Code.equals("")) {
 			return "";
 		}
 		this.WriteLineCode(Code);
 		return Code;
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName);
 	}
 }
