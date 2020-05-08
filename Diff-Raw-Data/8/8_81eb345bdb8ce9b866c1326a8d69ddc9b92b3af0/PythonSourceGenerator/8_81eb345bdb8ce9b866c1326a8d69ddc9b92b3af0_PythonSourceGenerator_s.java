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
 
 public class PythonSourceGenerator extends SourceGenerator {
 	/*field*/private int SwitchCaseCount;
 
 	PythonSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.Tab = "    ";
 		this.LogicalAndOperator = "and";
 		this.LogicalOrOperator  = "or";
 		this.TrueLiteral  = "True";
 		this.FalseLiteral = "False";
 		this.NullLiteral = "None";
 		this.LineComment = "##";
 		this.SwitchCaseCount = 0;
 		this.BlockBegin = "";
 		this.BlockEnd = "";
 		this.SemiColon = "";
 	}
 
 	@Override protected String GetNewOperator(GtType Type) {
 		/*local*/String TypeName = Type.ShortClassName;
 		return TypeName + "()";
 	}
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		/*
 		 * do { Block } while(Cond)
 		 * => for(; True; if(!Cond) { break; } ) { Block;  }
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
 		/*local*/GtNode TrueNode = this.CreateConstNode(ParsedTree.NameSpace.Context.BooleanType, ParsedTree, true);
 		return this.CreateForNode(Type, ParsedTree, TrueNode, IfBlock, Block);
 	}
 
 	// Visitor API
 	@Override public void VisitWhileNode(WhileNode Node) {
 		/*local*/String Program = "while " + this.VisitNode(Node.CondExpr) + ":" + this.LineFeed;
 		if(this.IsEmptyBlock(Node.LoopBody)) {
 			Program += this.GetIndentString() + "pass";
 		}
 		else {
 			Program += this.VisitBlockWithIndent(Node.LoopBody, true);
 		}
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		/* for(; COND; ITER) BLOCK1; continue; BLOCK2;
 		 * => while COND:
 		 * 		BLOCK1;
 		 * 		ITER;continue;
 		 * 		BLOCK2;
 		 * 		ITER
 		 */
 		/*local*/String Program = "while " + this.VisitNode(Node.CondExpr) + ":" + this.LineFeed;
 		if(this.IsEmptyBlock(Node.LoopBody)) {
 			Program += this.GetIndentString() + "pass";
 		}
 		else {
 			Program += this.VisitBlockWithIndent(Node.LoopBody, true);
 		}
		Program += this.VisitNode(Node.IterExpr);
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		/*local*/String Iter = this.VisitNode(Node.IterExpr);
 		/*local*/String Variable = this.VisitNode(Node.Variable);
 		/*local*/String Program = "for " + Variable + " in " + Iter + ":" + this.LineFeed;
 		Program += this.VisitBlockWithIndent(Node.LoopBody, true);
 		this.PushSourceCode(Program);
 	}
 
 	private ForNode FindParentForNode(GtNode Node) {
 		/*local*/GtNode Parent = Node.GetParentNode();
 		while(Parent != null) {
 			if(Parent instanceof ForNode) {
 				return (/*cast*/ForNode)Parent;
 			}
 			if(Parent.GetParentNode() == null) {
 				Parent = Parent.MoveHeadNode();
 			}
 			Parent = Parent.GetParentNode();
 		}
 		return null;
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "";
 		/*local*/ForNode Parent = this.FindParentForNode(Node);
 		if(Parent != null) {
 			/*local*/GtNode IterNode = Parent.IterExpr;
 			if(IterNode != null) {
 				Code += this.VisitNode(IterNode) + this.LineFeed + this.GetIndentString();
 			}
 		}
 		Code += this.ContinueKeyword;
 		if(this.HasLabelSupport) {
 			/*local*/String Label = Node.Label;
 			if(Label != null) {
 				Code += " " + Label;
 			}
 		}
 		this.PushSourceCode(Code);
 		this.StopVisitor(Node);
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String FuncName = Node.Token.ParsedText;
 		/*local*/String Expr = this.VisitNode(Node.Expr);
 		if(LibGreenTea.EqualsString(FuncName, "++")) {
 			FuncName = " += 1";
 		}
 		else if(LibGreenTea.EqualsString(FuncName, "--")) {
 			FuncName = " -= 1";
 		}
 		else {
 			LibGreenTea.DebugP(FuncName + " is not supported suffix operator!!");
 		}
 		this.PushSourceCode("(" + SourceGenerator.GenerateApplyFunc1(null, FuncName, true, Expr) + ")");
 	}
 
 	@Override public void VisitVarNode(VarNode Node) {
 		/*local*/String Code = Node.NativeName;
 		/*local*/String InitValue = this.NullLiteral;
 		if(Node.InitNode != null) {
 			InitValue = this.VisitNode(Node.InitNode);
 		}
 		Code += " = " + InitValue + this.LineFeed;
 		this.PushSourceCode(Code + this.VisitBlockWithIndent(Node.BlockNode, false));
 	}
 
 	@Override public void VisitTrinaryNode(TrinaryNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String Then = this.VisitNode(Node.ThenExpr);
 		/*local*/String Else = this.VisitNode(Node.ElseExpr);
 		this.PushSourceCode(Then + " if " + CondExpr + " else " + Else);
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		/*local*/String CondExpr = this.VisitNode(Node.CondExpr);
 		/*local*/String ThenBlock = this.VisitBlockWithIndent(Node.ThenNode, true);
		/*local*/String Code = "if " + CondExpr + ":" + this.LineFeed + ThenBlock;
 		if(this.IsEmptyBlock(Node.ThenNode)) {
 			Code += this.GetIndentString() + "pass" + this.LineFeed + this.GetIndentString();
 		}
 
 		if(!this.IsEmptyBlock(Node.ElseNode)) {
 			/*local*/String ElseBlock = this.VisitBlockWithIndent(Node.ElseNode, true);
 			Code += "else:" + this.LineFeed + ElseBlock;
 		}
 		this.PushSourceCode(Code);
 	}
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		/*local*/String Code = "Match" + this.SwitchCaseCount + " = " + this.VisitNode(Node.MatchNode) + this.LineFeed;
 		this.SwitchCaseCount += 1;
 		/*local*/int i = 0;
 		while(i < Node.CaseList.size()) {
 			/*local*/GtNode Case  = Node.CaseList.get(i);
 			/*local*/GtNode Block = Node.CaseList.get(i+1);
 			Code += this.GetIndentString();
 			if(i == 0) {
 				Code += "if";
 			}
 			else {
 				Code += "elif";
 			}
 			Code += this.VisitNode(Case) + ":";
 			Code += this.VisitBlockWithIndent(Block, true) + this.LineFeed;
 			i = i + 2;
 		}
 		if(Node.DefaultBlock != null) {
 			Code += this.GetIndentString() + "else: ";
 			Code += this.VisitBlockWithIndent(Node.DefaultBlock, true) + this.LineFeed;
 		}
 		Code += this.GetIndentString() + "}";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try:" + this.LineFeed;
 		Code += this.VisitBlockWithIndent(Node.TryBlock, true);
 		/*local*/VarNode Val = (/*cast*/VarNode) Node.CatchExpr;
 		Code += "except " + Val.Type.toString() + ", " + Val.NativeName + ":" + this.LineFeed;
 		Code += this.VisitBlockWithIndent(Node.CatchBlock, true);
 		if(Node.FinallyBlock != null) {
 			/*local*/String Finally = this.VisitBlockWithIndent(Node.FinallyBlock, true);
 			Code += "finally:" + this.LineFeed + Finally;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String expr = "";
 		if(Node.Expr != null) {
 			expr = this.VisitNode(Node.Expr);
 		}
 		this.PushSourceCode("raise " + expr);
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "raise SoftwareFault(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "";
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			Code += this.AppendCommand(CurrentNode);
 			CurrentNode = (/*cast*/CommandNode) CurrentNode.PipedNextNode;
 		}
 
 		if(Node.Type.equals(Node.Type.Context.StringType)) {
 			Code = "subprocess.check_output(\"" + Code + "\", shell=True)";
 		}
 		else if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 			Code = "(subprocess.call(\"" + Code + "\", shell=True) == 0)";
 		}
 		else {
 			Code = "subprocess.call(\"" + Code + "\", shell=True)";
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String AppendCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		/*local*/int i = 0;
 		while(i < size) {
 			Code += this.VisitNode(CurrentNode.Params.get(i)) + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	@Override public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		this.FlushErrorReport();
 		/*local*/String Function = "def ";
 		Function += Func.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		/*local*/int size = ParamNameList.size();
 		while(i < size) {
 			if(i > 0) {
 				Function += ", ";
 			}
 			Function += ParamNameList.get(i);
 			i = i + 1;
 		}
 		/*local*/String Block = this.VisitBlockWithIndent(Body, true);
 		Function += "):" + this.LineFeed + Block + this.LineFeed;
 		this.WriteLineCode(Function);
 	}
 
 	@Override public void OpenClassField(GtType Type, GtClassField ClassField) {
 		this.FlushErrorReport();
 		/*local*/String Program = this.GetIndentString() + "class " + Type.ShortClassName;
 //		if(Type.SuperType != null) {
 //			Program += "(" + Type.SuperType.ShortClassName + ")";
 //		}
 		Program += ":" + this.LineFeed;
 		this.Indent();
 
 		Program += this.GetIndentString() + "def __init__(" + this.GetRecvName() + ")" + ":" + this.LineFeed;
 		this.Indent();
 		/*local*/int i = 0;
 		while(i < ClassField.FieldList.size()) {
 			/*local*/GtFieldInfo FieldInfo = ClassField.FieldList.get(i);
 			/*local*/String InitValue = this.StringifyConstValue(FieldInfo.InitValue);
 			if(!FieldInfo.Type.IsNative()) {
 				InitValue = "None";
 			}
 			Program += this.GetIndentString() + this.GetRecvName() + "." + FieldInfo.NativeName + " = " + InitValue + this.LineFeed;
 			i = i + 1;
 		}
 		this.UnIndent();
 		this.UnIndent();
 		this.WriteLineCode(Program);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		/*local*/String Code = this.VisitBlockWithIndent(Node, false);
 		if(!LibGreenTea.EqualsString(Code, "")) {
 			this.WriteLineCode(Code);
 		}
 		return null;
 	}
 
 	@Override public String GetRecvName() {
 		return "self";
 	}
 
 	@Override public void InvokeMainFunc(String MainFuncName) {
 		this.WriteLineCode("if __name__ == '__main__':");
 		this.WriteLineCode(this.Tab + MainFuncName + "()");
 	}
 
 }
