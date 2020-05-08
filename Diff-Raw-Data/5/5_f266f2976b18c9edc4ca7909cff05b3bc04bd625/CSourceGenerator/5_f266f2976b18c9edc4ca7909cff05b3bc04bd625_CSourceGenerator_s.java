 import java.util.ArrayList;
 
 //GreenTea Generator should be written in each language.
 
 public class CSourceGenerator extends GreenTeaGenerator {
 
 	CSourceGenerator() {
 		super("CSource");
 	}
 
 	public void VisitBlockEachStatementWithIndent(TypedNode Node) {
 		/*local*/String Code = "{\n";
 		this.Indent();
 		/*local*/TypedNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + ";\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.UnIndent();
 		Code += this.GetIndentString() + "}";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(TypedNode Node) {
 		this.PushSourceCode("/*empty*/");
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("++")) {
 		//}
 		//else if(MethodName.equals("--")) {
 		//}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + MethodName);
 	}
 
 	@Override
 	public void VisitUnaryNode(UnaryNode Node) {
 		String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("+")) {
 		//}
 		//else if(MethodName.equals("-")) {
 		//}
 		//else if(MethodName.equals("~")) {
 		//}
 		//else if(MethodName.equals("!")) {
 		//}
 		//else if(MethodName.equals("++")) {
 		//}
 		//else if(MethodName.equals("--")) {
 		//}
 		//else {
 		//	throw new RuntimeException("NotSupportOperator");
 		//}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(MethodName + this.PopSourceCode());
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "[" + this.PopSourceCode() + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		String Program = "while(" + this.PopSourceCode() + ")";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		String Program = "do";
 		this.VisitBlockEachStatementWithIndent(Node.LoopBody);
 		Node.CondExpr.Evaluate(this);
 		Program += " while(" + this.PopSourceCode() + ")";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		String Cond = this.PopSourceCode();
 		String Iter = this.PopSourceCode();
 		String Program = "for(; " + Cond  + "; " + Iter + ")";
 		Node.LoopBody.Evaluate(this);
 		Program += this.PopSourceCode();
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String Code = "NULL";
 		if(Node.ConstValue != null) {
 			Code = this.StringfyConstValue(Node.ConstValue);
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitNewNode(NewNode Node) {
 		/*local*/String Type = Node.Type.ShortClassName;
 		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "->" + Node.Method.MethodName);
 	}
 
 	private String[] EvaluateParam(ArrayList<TypedNode> Params) {
 		/*local*/int Size = GtStatic.ListSize(Params);
 		/*local*/String[] Programs = new String[Size];
 		for(int i = 0; i < Size; i++) {
 			TypedNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = this.PopSourceCode();
 		}
 		return Programs;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = Node.Method.LocalFuncName + "(";
 		/*local*/String[] Params = EvaluateParam(Node.Params);
 		/*local*/int i = 0;
 		while(i < Params.length) {
 			String P = Params[i];
 			if(i != 0) {
 				Program += ",";
 			}
 			Program += P;
 			i = i + 1;
 		}
 		Program += ")";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		//if(MethodName.equals("+")) {
 		//}
 		//else if(MethodName.equals("-")) {
 		//}
 		//else if(MethodName.equals("*")) {
 		//}
 		//else if(MethodName.equals("/")) {
 		//}
 		//else if(MethodName.equals("%")) {
 		//}
 		//else if(MethodName.equals("<<")) {
 		//}
 		//else if(MethodName.equals(">>")) {
 		//}
 		//else if(MethodName.equals("&")) {
 		//}
 		//else if(MethodName.equals("|")) {
 		//}
 		//else if(MethodName.equals("^")) {
 		//}
 		//else if(MethodName.equals("<=")) {
 		//}
 		//else if(MethodName.equals("<")) {
 		//}
 		//else if(MethodName.equals(">=")) {
 		//}
 		//else if(MethodName.equals(">")) {
 		//}
 		//else if(MethodName.equals("!=")) {
 		//}
 		//else if(MethodName.equals("==")) {
 		//}
 		//else {
 		//	throw new RuntimeException("NotSupportOperator");
 		//}
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " " + MethodName + " " + this.PopSourceCode());
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " && " + this.PopSourceCode());
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " || " + this.PopSourceCode());
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + " = " + this.PopSourceCode());
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		/*local*/String Type = Node.DeclType.ShortClassName;
 		Node.VarNode.Evaluate(this);
 		/*local*/String Code = Type + " " + this.PopSourceCode() + ";";
 		this.VisitBlockEachStatementWithIndent(Node.BlockNode);
 		this.PushSourceCode(Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockEachStatementWithIndent(Node.ThenNode);
 		this.VisitBlockEachStatementWithIndent(Node.ElseNode);
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += " else " + ElseBlock;
 		}
 		this.PushSourceCode(Code);
 
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		/*local*/String Code = "return";
 		if(Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			Code += " " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode(Label + ":");
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushSourceCode("goto " + Label);
 	}
 
 	@Override public void VisitBreakNode(BreakNode Node) {
 		/*local*/String Code = "break";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitContinueNode(ContinueNode Node) {
 		/*local*/String Code = "continue";
 		/*local*/String Label = Node.Label;
 		if(Label != null) {
 			Code += " " + Label;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitTryNode(TryNode Node) {
 		/*local*/String Code = "try";
 		//this.VisitEach(Node.CatchBlock);
 		this.VisitBlockEachStatementWithIndent(Node.TryBlock);
 		Code += this.PopSourceCode();
 		if(Node.FinallyBlock != null) {
 			this.VisitBlockEachStatementWithIndent(Node.FinallyBlock);
 			Code += " finally " + this.PopSourceCode();
 		}
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 		Node.Expr.Evaluate(this);
 		/*local*/String Code = "throw " + this.PopSourceCode();
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {
 		/*local*/String Code = "system(\"";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(Node.Params)) {
 			TypedNode Param = Node.Params.get(i);
 			if(i != 0) {
 				Code += " ";
 			}
 			Param.Evaluate(this);
 			Code += this.PopSourceCode();
 			i = i + 1;
 		}
 		Code += "\")";
 		this.PushSourceCode(Code);
 	}
 
 	public String LocalTypeName(GtType Type) {
 		return Type.ShortClassName;
 	}
 	
 	@Override
 	public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, TypedNode Body) {
 		/*local*/String Code = "";
 		/*local*/String RetTy = this.LocalTypeName(Method.GetReturnType());
 		Code += RetTy + " " + Method.LocalFuncName + "(";
 		for(int i = 0; i < ParamNameList.size(); i++) {
 			String ParamTy = this.LocalTypeName(Method.GetParamType(i));
			Code += ParamTy + " " + ParamNameList.get(i);
 			if(i > 0) {
 				Code += ", ";
 			}
 		}
 		Code += ")";
 		this.VisitBlockEachStatementWithIndent(Body);
 		Code += this.PopSourceCode();
 		DebugP("\n\n\n" + Code);
 	}
 
 	@Override public Object Eval(TypedNode Node) {
 		this.VisitBlockEachStatementWithIndent(Node);
 		return this.PopSourceCode();
 	}
 
 	@Override public void AddClass(GtType Type) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override public void LoadContext(GtContext Context) {
		new JavaLayerDef().MakeDefinition(Context.DefaultNameSpace);
 	}
 }
