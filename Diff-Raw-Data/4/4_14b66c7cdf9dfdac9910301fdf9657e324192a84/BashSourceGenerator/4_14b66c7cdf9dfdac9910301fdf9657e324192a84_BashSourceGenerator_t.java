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
 		this.TrueLiteral  = "0";
 		this.FalseLiteral = "1";
 		this.NullLiteral = "NULL";
 	}
 
 	@Override public void InitContext(GtClassContext Context) {
 		super.InitContext(Context);
 		this.WriteLineHeader("#!/bin/bash");
		this.WriteLineCode(this.LineFeed + "source $GREENTEA_HOME/include/bash/GreenTeaPlus.sh" + this.LineFeed);
		//this.WriteLineCode(this.LineFeed + "source ./GreenTeaPlus.sh" + this.LineFeed);
 	}
 
 	private boolean IsEmptyNode(GtNode Node) {
 		return (Node == null || Node instanceof EmptyNode);
 	}
 	
 	public String VisitBlockWithIndent(GtNode Node, boolean inBlock, boolean allowDummyBlock) {
 		/*local*/String Code = "";
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		if(IsEmptyNode(Node) && allowDummyBlock) {
 			Code += this.GetIndentString() + "echo \"dummy block!!\" &> /dev/zero" + this.LineFeed;
 		}
 		while(!IsEmptyNode(CurrentNode)) {
 			/*local*/String poppedCode = this.VisitNode(CurrentNode);
 			if(!LibGreenTea.EqualsString(poppedCode, "")) {
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
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		/*
 		 * do { Block } while(Cond)
 		 * => while(True) { Block; if(Cond) { break; } }
 		 */
 		/*local*/GtNode Break = this.CreateBreakNode(Type, ParsedTree, null);
 		/*local*/GtNode IfBlock = this.CreateIfNode(Type, ParsedTree, Cond, Break, null);
 		GtStatic.LinkNode(IfBlock, Block);
 		/*local*/GtNode TrueNode = this.CreateConstNode(ParsedTree.NameSpace.Context.BooleanType, ParsedTree, true);
 		return this.CreateWhileNode(Type, ParsedTree, TrueNode, Block);
 	}
 	
 	private String ResolveCondition(GtNode Node) {
 		if(!Node.Type.equals(Node.Type.Context.BooleanType)) {
 			return null;
 		}
 		
 		if(Node instanceof ConstNode) {
 			ConstNode Const = (/*cast*/ConstNode) Node;
 			if(Const.ConstValue.equals(true)) {
 				return "true";
 			}
 			return "false";
 		}
 		else if(Node instanceof BinaryNode || Node instanceof ApplyNode || Node instanceof CommandNode) {
 			return this.VisitNode(Node) + " &> /dev/zero";
 		}
 		return this.VisitNode(Node);
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.ResolveCondition(Node.CondExpr) + " ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/*local*/String Cond = this.ResolveCondition(Node.CondExpr);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " )) ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		/*local*/String Variable = this.VisitNode(Node.Variable);
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Program = "for " + Variable + " in " + "${" + Iter + "[@]} ;do" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true, true) + "done";
 		this.PushSourceCode(Program);
 	}
 
 	private String[] MakeParamCode(ArrayList<GtNode> ParamList) {
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			/*local*/GtNode Node = ParamList.get(i);
 			ParamCode[i - 1] = this.ResolveValueType(Node);
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	private String CreateAssertFunc(ApplyNode Node) {
 		/*local*/ArrayList<GtNode> ParamList = Node.Params;
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			/*local*/String Param = this.VisitNode(ParamList.get(i));
 			if(ParamList.get(i) instanceof ConstNode) {
 				/*local*/ConstNode Const = (/*local*/ConstNode) ParamList.get(i);
 				if(Const.Type.equals(Const.Type.Context.BooleanType)) {
 					if(Const.ConstValue.equals(true)) {
 						Param = "true";
 					}
 					else {
 						Param = "false";
 					}
 				}
 			}
 			ParamCode[i - 1] = "\"" + Param + "\"";
 			i = i + 1;
 		}
 		return this.JoinCode("assert ", 0, ParamCode, "", " ");
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String[] ParamCode = this.MakeParamCode(Node.Params);
 		if(Node.Func == null) {
 			this.PushSourceCode(this.JoinCode(ParamCode[0] + " ", 0, ParamCode, "", " "));
 		}
 //		else if(Node.Func.Is(NativeFunc)) {
 //			this.PushSourceCode(this.JoinCode(ParamCode[0] + "." + Node.Func.FuncName + " ", 0, ParamCode, ""));
 //		}
 		else if(Node.Func.Is(NativeMacroFunc)) {
 			/*local*/String NativeMacro = Node.Func.GetNativeMacro();
 			if(LibGreenTea.EqualsString(NativeMacro, "assert $1")) {
 				this.PushSourceCode(this.CreateAssertFunc(Node));
 				return;
 			}
 			this.PushSourceCode(Node.Func.ApplyNativeMacro(0, ParamCode));
 		}
 		else {
 			this.PushSourceCode(this.JoinCode(Node.Func.GetNativeFuncName() + " ", 0, ParamCode, "", " "));
 		}
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Left = this.ResolveValueType(Node.LeftNode);
 		/*local*/String Right = this.ResolveValueType(Node.RightNode);
 		this.PushSourceCode(SourceGenerator.GenerateApplyFunc2(Node.Func, FuncName, Left, Right));
 		
 //		if(Node.Type.equals(Node.Type.Context.Float)) {	// support float value
 //			this.PushSourceCode("(echo \"scale=10; " + Left + " " + FuncName + " " + Right + "\" | bc)");
 //			return;
 //		}
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " && " + this.ResolveCondition(Node.RightNode) + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		this.PushSourceCode("(" + this.ResolveCondition(Node.LeftNode) + " || " + this.ResolveCondition(Node.RightNode) + ")");
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
 		this.PushSourceCode(Code + this.VisitBlockWithIndent(Node.BlockNode, false, false));
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.ResolveCondition(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true, true);
 		/*local*/String ElseBlock = this.VisitBlockWithIndent(Node.ElseNode, true, false);
 		/*local*/String Code = "if " + CondExpr + " ;then" + this.LineFeed + ThenBlock;
 		if(!IsEmptyNode(Node.ElseNode)) {
 			Code += "else" + this.LineFeed + ElseBlock;
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(!this.inFunc) {
 			return;
 		}
 		
 		if(Node.Expr != null) {
 			/*local*/String Ret = this.ResolveValueType(Node.Expr);
 			if(Node.Expr instanceof ApplyNode || Node.Expr instanceof CommandNode || 
 					Node.Expr instanceof BinaryNode || Node.Expr instanceof UnaryNode) {
 				if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 					/*local*/String Code = "local value=" + Ret + this.LineFeed;
 					Code += this.GetIndentString() + "echo $value" + this.LineFeed;
 					Code += this.GetIndentString() + "return $value";
 					this.PushSourceCode(Code);
 					return;
 				}
 			}
 			this.PushSourceCode("echo " + Ret + this.LineFeed + this.GetIndentString() + "return 0");
 		} else {
 			this.PushSourceCode("return 0");
 		}
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/GtNode TrueNode = new ConstNode(Node.Type.Context.BooleanType, null, true);
 		/*local*/String Code = "trap ";
 		/*local*/String Try = this.VisitNode(new IfNode(null, null, TrueNode, Node.TryBlock, null));
 		/*local*/String Catch = this.VisitNode(new IfNode(null, null, TrueNode, Node.CatchBlock, null));
 		Code += "\"" + Catch + "\" ERR" + this.LineFeed;
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
 			RunnableCmd = FuncName + this.cmdCounter + "() {" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "echo $(" + cmd + ")" + this.LineFeed;
 			RunnableCmd += this.GetIndentString() + "return 0" + this.LineFeed + "}" + this.LineFeed;
 			this.WriteLineCode(RunnableCmd);
 			RunnableCmd = FuncName + this.cmdCounter;
 			this.cmdCounter++;
 		}
 		else if(Type.equals(Type.Context.IntType) || Type.equals(Type.Context.BooleanType)) {
 			RunnableCmd = FuncName + this.cmdCounter + "() {" + this.LineFeed;
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
 		if(ParamNameList == null || index == ParamNameList.size()) {
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
 		/*local*/String Head = "";
 		/*local*/String Tail = "";
 		
 		if(TargetNode.Type != null && TargetNode.Type.equals(TargetNode.Type.Context.BooleanType)) {
 			if(TargetNode instanceof ApplyNode || 
 					TargetNode instanceof CommandNode || TargetNode instanceof BinaryNode) {
 				return "$(retBool \"" + Value + "\")";
 			}
 		}
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			return Value;
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
 		if(TargetNode.Type != null) {
 			if(TargetNode.Type.equals(TargetNode.Type.Context.StringType)) {
 				Head = "\"";
 				Tail = "\"";
 			}
 		}
 		return Head + ResolvedValue + Tail;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "";
 		this.inFunc = true;
 		Function += Func.GetNativeFuncName() + "() {" + this.LineFeed;
 		/*local*/String Block = this.VisitBlockWithIndent(this.ResolveParamName(ParamNameList, Body), true, true);
 		Function += Block + "}" + this.LineFeed;
 		this.WriteLineCode(Function);
 		this.inFunc = false;
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithIndent(Node, false, false);
 		if(!LibGreenTea.EqualsString(Code, "")) {
 			this.WriteLineCode(Code);
 		}
 		return Code;
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode(MainFuncName);
 	}
 }
