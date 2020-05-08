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
 
 public class PythonSourceGenerator extends SourceGenerator {
 
 	PythonSourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 	}
 
 	public void VisitBlockWithIndent(GtNode Node, boolean inBlock) {
 		/*local*/String Code = "";
 		if(inBlock) {
 			this.Indent();
 		}
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			/*local*/String poppedCode = this.PopSourceCode();
 			if(!poppedCode.equals("")) {
 				Code += this.GetIndentString() + poppedCode + this.LineFeed;
 			}
 			CurrentNode = CurrentNode.NextNode;
 		}
 		if(inBlock) {
 			this.UnIndent();
 			Code += this.GetIndentString();
 		}		else {
 			if(Code.length() > 0) {
 				Code = Code.substring(0, Code.length() - 1);
 			}
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.IndexAt.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "[" + this.PopSourceCode() + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while " + this.PopSourceCode() + ":" + this.LineFeed;
 		this.VisitBlockWithIndent(Node.LoopBody, true);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		this.VisitBlockWithIndent(Node.LoopBody, true);
 		/*local*/String LoopBody = this.PopSourceCode();
 		/*local*/String Program = "if True:" + this.LineFeed + LoopBody;
 		Node.CondExpr.Evaluate(this);
 		Program += "while " + this.PopSourceCode() + ":" + this.LineFeed;
 		this.PushSourceCode(Program + LoopBody);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.LoopBody.MoveTailNode().NextNode = Node.IterExpr;
 		/*local*/GtNode NewLoopBody = Node.LoopBody;
 		/*local*/WhileNode NewNode = new WhileNode(Node.Type, Node.Token, Node.CondExpr, NewLoopBody);
 		this.VisitWhileNode(NewNode);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		Node.Variable.Evaluate(this);
 		Node.IterExpr.Evaluate(this);
 		/*local*/String Iter = this.PopSourceCode();
 		/*local*/String Variable = this.PopSourceCode();
 		
 		/*local*/String Program = "for " + Variable + " in " + Iter + ":" + this.LineFeed;
 		this.VisitBlockWithIndent(Node.LoopBody, true);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String StringfiedValue = this.StringfyConstValue(Node.ConstValue);
 		if(Node.ConstValue instanceof GtMethod) {
 			StringfiedValue = ((/*cast*/GtMethod)Node.ConstValue).GetNativeFuncName();
 		}
 		else if(Node.Type.equals(Node.Type.Context.BooleanType) || StringfiedValue.equals("true") || StringfiedValue.equals("false")) {
 			if(StringfiedValue.equals("true")) {
 				StringfiedValue = "True";
 			}
 			else if(StringfiedValue.equals("false")) {
 				StringfiedValue = "False";
 			}
 		}
 		this.PushSourceCode(StringfiedValue);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("None");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "." + Node.Token.ParsedText);
 	}
 
 	protected String[] MakeParamCode1(GtNode Node) {
 		/*local*/String[] ParamCode = new String[1];
 		Node.Evaluate(this);
 		ParamCode[0] = this.PopSourceCode();
 		return ParamCode;
 	}
 	
 	protected String[] MakeParamCode2(GtNode Node, GtNode Node2) {
 		/*local*/String[] ParamCode = new String[2];
 		Node.Evaluate(this);
 		Node2.Evaluate(this);
 		ParamCode[1] = this.PopSourceCode();
 		ParamCode[0] = this.PopSourceCode();
 		return ParamCode;
 	}
 
 	protected String[] MakeParamCode(ArrayList<GtNode> ParamList) {
 		/*local*/int Size = GtStatic.ListSize(ParamList);
 		/*local*/String[] ParamCode = new String[Size - 1];
 		/*local*/int i = 1;
 		while(i < Size) {
 			/*local*/GtNode Node = ParamList.get(i);
 			Node.Evaluate(this);
 			ParamCode[Size - i - 1] = this.PopSourceCode();
 			i = i + 1;
 		}
 		return ParamCode;
 	}
 
 	protected String JoinCode(String BeginCode, int BeginIdx, String[] ParamCode, String EndCode) {
 		/*local*/String JoinedCode = BeginCode;
 		/*local*/int i = BeginIdx;
 		while(i < ParamCode.length) {
 			/*local*/String P = ParamCode[i];
 			if(i != BeginIdx) {
 				JoinedCode += ", ";
 			}
 			JoinedCode += P;
 			i = i + 1;
 		}
 		return JoinedCode + EndCode;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String[] ParamCode = this.MakeParamCode(Node.Params);
 		if(Node.Method == null) {
 			this.PushSourceCode(this.JoinCode(ParamCode[0] + "(", 0, ParamCode, ")"));
 		}
 		else if(Node.Method.Is(NativeMethod)) {
 			this.PushSourceCode(this.JoinCode(ParamCode[0] + "." + Node.Method.MethodName + "(", 0, ParamCode, ")"));			
 		}
 		else if(Node.Method.Is(NativeMacroMethod)) {
 			this.PushSourceCode(Node.Method.ApplyNativeMacro(0, ParamCode));						
 		}else {
 			this.PushSourceCode(this.JoinCode(Node.Method.GetNativeFuncName() + "(", 0, ParamCode, ")"));						
 		}
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {	//FIXME
 		/*local*/String MethodName = Node.Token.ParsedText;
 		if(MethodName.equals("++")) {
 			MethodName = " += 1";
 		}
 		else if(MethodName.equals("--")) {
 			MethodName = " -= 1";
 		}
 		else {
 			LangDeps.DebugP(MethodName + " is not supported suffix operator!!");
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + MethodName);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		/*local*/String Type = Node.Type.ShortClassName;
 		this.PushSourceCode( this.JoinCode(Type + "(", 0, this.MakeParamCode(Node.Params), ")"));
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		if(Node.Method == null) {
 			Node.Expr.Evaluate(this);
 			this.PushSourceCode("(" + Node.Token.ParsedText + this.PopSourceCode() + ")");
 		}
 		else {
			/*local*/String[] ParamCode = this.MakeParamCode1(Node.Expr);
 			this.PushSourceCode("(" + Node.Method.ApplyNativeMacro(0, ParamCode) + ")");
 		}
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		if(Node.Method == null) {
 			Node.RightNode.Evaluate(this);
 			Node.LeftNode.Evaluate(this);
 			this.PushSourceCode("(" + this.PopSourceCode() + " " +  Node.Token.ParsedText + " " + this.PopSourceCode() + ")");
 		}
 		else {
			/*local*/String[] ParamCode = this.MakeParamCode2(Node.LeftNode, Node.RightNode);
 			this.PushSourceCode("(" + Node.Method.ApplyNativeMacro(0, ParamCode) + ")");
 		}
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " and " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " or " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " = " + this.PopSourceCode());
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Code = Node.VariableName;
 		/*local*/String InitValue = "None";
 		if(Node.InitNode != null) {
 			Node.InitNode.Evaluate(this);
 			InitValue = this.PopSourceCode();
 		}
 		Code += " = " + InitValue + this.LineFeed;
 		this.VisitBlockWithIndent(Node.BlockNode, false);
 		this.PushSourceCode(Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockWithIndent(Node.ThenNode, true);
 		this.VisitBlockWithIndent(Node.ElseNode, true);
 		
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if " + CondExpr + ":" + this.LineFeed + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "else:" + this.LineFeed + ElseBlock;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		/*local*/String retValue = "";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			retValue = this.PopSourceCode();
 		}
 		this.PushSourceCode("return " + retValue);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try:" + this.LineFeed;
 		this.VisitBlockWithIndent(Node.TryBlock, true);
 		Code += this.PopSourceCode();
 		/*local*/LetNode Val = (/*cast*/LetNode) Node.CatchExpr;
 		Code += "except " + Val.Type.toString() + ", " + Val.VariableName + ":" + this.LineFeed;
 		this.VisitBlockWithIndent(Node.CatchBlock, true);
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockWithIndent(Node.FinallyBlock, true);
 			Code += " finally:" + this.LineFeed + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		/*local*/String expr = "";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			expr = this.PopSourceCode();
 		}
 		this.PushSourceCode("raise " + expr);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "raise SoftwareFault(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
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
 		
 		if(Node.Type.equals(Node.Type.Context.StringType)) {
 			Code = "subprocess.check_output(\"" + Code + "\", shell=True)";
 		}
 		else if(Node.Type.equals(Node.Type.Context.BooleanType)) {
 			Code = "True if subprocess.call(\"" + Code + "\", shell=True) == 0 else False";
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
 			CurrentNode.Params.get(i).Evaluate(this);
 			Code += this.PopSourceCode() + " ";
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	@Override public void GenerateMethod(GtMethod Method, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "def ";
 		Function += Method.GetNativeFuncName() + "(";
 		/*local*/int i = 0;
 		/*local*/int size = ParamNameList.size();
 		while(i < size) {
 			if(i > 0) {
 				Function += ", ";
 			}
 			Function += ParamNameList.get(i);
 			i = i + 1;
 		}
 		this.VisitBlockWithIndent(Body, true);
 		Function += "):" + this.LineFeed + this.PopSourceCode() + this.LineFeed;
 		this.WriteLineCode(Function);
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		this.VisitBlockWithIndent(Node, false);
 		/*local*/String Code = this.PopSourceCode();
 		if(Code.equals("")) {
 			return "";
 		}
 		this.WriteLineCode(Code);
 		return Code;
 	}
 
 	@Override public void AddClass(GtType Type) {
 	}
 
 }
