 import java.util.ArrayList;
 
 // GreenTea Generator should be written in each language.
 
 public class JavaScriptSampleGenerator extends GreenTeaGenerator {
 	/*field*/IndentGenerator Indent;
 
 	JavaScriptSampleGenerator() {
 		super("JavaScript");
 		this.Indent = new IndentGenerator(2);
 	}
 
 	private boolean UseLetKeyword;
 
 	@Override
 	public void DefineFunction(GtMethod Method, ArrayList<String> NameList, TypedNode Body) {
 		/*local*/int ArgCount = Method.Types.length - 1;
 		/*local*/String Code = "var " + Method.MethodName + "= (function(";
 		/*local*/int i = 0;
 		while(i < ArgCount){
 			if(i > 0){
 				Code = Code + ", ";
 			}
 			Code = Code + NameList.get(i) + "0";
 			i = i + 1;
 		}
 		Code += ") ";
 		this.VisitBlockJS(Body);
 		Code += this.PopCode() + ")";
 		this.PushCode(Code);
 	};
 
 	public  void VisitBlockJS(TypedNode Node) {
 		/*local*/String highLevelIndent = this.Indent.AddIndentAndGet(1);
 		super.VisitBlock(Node);
 		/*local*/String currentLevelIndent = this.Indent.AddIndentAndGet(-1);
 		/*local*/int Size = Node.CountForrowingNode();
 		/*local*/String Block = this.PopManyCodeWithModifier(Size, true, "\n" + highLevelIndent, ";", null);
 		this.PushCode("{" + Block + "\n" + currentLevelIndent + "}");
 	}
 
 	@Override
 	public void VisitConstNode(ConstNode Node) {
 		this.PushCode(Node.ConstValue == null ? "null" : Node.ConstValue.toString());
 		return;
 	}
 
 	@Override
 	public void VisitUnaryNode(UnaryNode UnaryNode) {
 		UnaryNode.Expr.Evaluate(this);
 		this.PushCode(UnaryNode.Token.ParsedText + this.PopCode());
 	};
 
 	@Override
 	public void VisitBinaryNode(BinaryNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		/*local*/String operator = Node.Token.ParsedText;
 		if(operator.equals("/") /*&& Node.Type == Context.IntType*/ ){
 			this.PushCode("((" + this.PopCode() + " " + operator + " " + this.PopCode() + ") | 0)");
 		}else{
 			this.PushCode("(" + this.PopCode() + " " + operator + " " + this.PopCode() + ")");
 		}
 	}
 
 	@Override
 	public void VisitNewNode(NewNode Node) {
 		/*local*/int i = 0;
 		while(i < Node.Params.size()) {
 			/*local*/TypedNode Param = Node.Params.get(i);
 			Param.Evaluate(this);
 			i = i + 1;
 		}
 		this.PushCode("new " + Node.Type.ShortClassName + "()");
 		return;
 	}
 
 	@Override
 	public void VisitNullNode(NullNode Node) {
 		this.PushCode("null");
 		return;
 	}
 
 	@Override
 	public void VisitLocalNode(LocalNode Node) {
 		//this.AddLocalVarIfNotDefined(Node.Type, Node.Token.ParsedText);
 		this.PushCode(Node.LocalName);
 	}
 
 	@Override
 	public void VisitGetterNode(GetterNode Node) {
 		Node.Expr.Evaluate(this);
 		this.PushCode(this.PopCode() + "." + Node.Token.ParsedText);
 	}
 
 	@Override
 	public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushCode(this.PopCode() + "." + Node.Token.ParsedText + "[" +this.PopCode() + "]");
 	}
 
 	@Override
 	public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String methodName = Node.Method.MethodName;
 		/*local*/int ParamCount = Node.Params.size();
 		/*local*/int i = 0;
 		while(i < ParamCount) {
 			Node.Params.get(i).Evaluate(this);
 			i = i + 1;
 		}
		/*local*/String params = "(" + this.PopManyCodeWithModifier(ParamCount, true, null, null, ", ") + ")";
		this.PushCode(methodName + params);
 	}
 
 	@Override
 	public void VisitAndNode(AndNode Node) {
 		Node.LeftNode.Evaluate(this);
 		Node.RightNode.Evaluate(this);
 		/*local*/String Right = this.PopCode();
 		/*local*/String Left = this.PopCode();
 		this.PushCode(Left + " && " + Right);
 	}
 
 	@Override
 	public void VisitOrNode(OrNode Node) {
 		Node.LeftNode.Evaluate(this);
 		Node.RightNode.Evaluate(this);
 		/*local*/String Right = this.PopCode();
 		/*local*/String Left = this.PopCode();
 		this.PushCode(Left + " || " + Right);
 	}
 
 	@Override
 	public void VisitAssignNode(AssignNode Node) {
 		Node.LeftNode.Evaluate(this);
 		Node.RightNode.Evaluate(this);
 		/*local*/String Right = this.PopCode();
 		/*local*/String Left = this.PopCode();
 		this.PushCode((this.UseLetKeyword ? "let " : "var ") + Left + " = " + Right);
 	}
 
 	@Override
 	public void VisitLetNode(LetNode Node) {
 		//		Node.ValueNode.Evaluate(this);
 		this.VisitBlockJS(Node.BlockNode);
 		//this.AddLocalVarIfNotDefined(Node.Type, Node.VarToken.ParsedText);
 		//		String Block = this.pop();
 		//		String Right = this.pop();
 		//		this.push(Node.VarToken.ParsedText + " = " + Right + Block);
 	}
 
 	@Override
 	public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockJS(Node.ThenNode);
 		this.VisitBlockJS(Node.ElseNode);
 
 		/*local*/String ElseBlock = this.PopCode();
 		/*local*/String ThenBlock = this.PopCode();
 		/*local*/String CondExpr = this.PopCode();
 		/*local*/String source = "if(" + CondExpr + ") " + ThenBlock;
 		if(Node.ElseNode != null) {
 			source = source + " else " + ElseBlock;
 		}
 		this.PushCode(source);
 	}
 
 	@Override
 	public void VisitSwitchNode(SwitchNode Node) {
 		//		Node..CondExpr.Evaluate(this);
 		//		/* FIXME: Do not use for statement */for(int i = 0; i < Node.Blocks.size(); i++) {
 		//			TypedNode Block = (TypedNode) Node.Blocks.get(i);
 		//			this.VisitListNode(Block);
 		//		}
 		//
 		//		int Size = Node.Labels.size();
 		//		String Exprs = "";
 		//		/* FIXME: Do not use for statement */for(int i = 0; i < Size; i = i + 1) {
 		//			String Label = (String) Node.Labels.get(Size - i);
 		//			String Block = this.pop();
 		//			Exprs = "case " + Label + ":" + Block + Exprs;
 		//		}
 		//		String CondExpr = this.pop();
 		//		this.push("switch (" + CondExpr + ") {" + Exprs + "}");
 	}
 
 	@Override
 	public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockJS(Node.LoopBody);
 		/*local*/String LoopBody = this.PopCode();
 		/*local*/String CondExpr = this.PopCode();
 		this.PushCode("while(" + CondExpr + ") {" + LoopBody + "}");
 	}
 
 	@Override
 	public void VisitForNode(ForNode Node) {
 		Node.CondExpr.Evaluate(this);
 		Node.IterExpr.Evaluate(this);
 		this.VisitBlockJS(Node.LoopBody);
 		/*local*/String LoopBody = this.PopCode();
 		/*local*/String IterExpr = this.PopCode();
 		/*local*/String CondExpr = this.PopCode();
 		this.PushCode("for(;" + CondExpr + "; " + IterExpr + ") {" + LoopBody + "}");
 	}
 
 	@Override
 	public void VisitDoWhileNode(DoWhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitBlockJS(Node.LoopBody);
 		/*local*/String LoopBody = this.PopCode();
 		/*local*/String CondExpr = this.PopCode();
 		this.PushCode("do {" + LoopBody + "}while(" + CondExpr + ");");
 	}
 
 	@Override
 	public void VisitEmptyNode(TypedNode Node) {
 		this.PushCode("");
 	}
 
 	@Override
 	public void VisitReturnNode(ReturnNode Node) {
 		if(Node.Expr != null){
 			Node.Expr.Evaluate(this);
 			this.PushCode("return " + this.PopCode());
 		}else{
 			this.PushCode("return");
 		}
 	}
 
 	@Override
 	public void VisitLabelNode(LabelNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushCode(Label + ":");
 		return;
 	}
 
 	@Override
 	public void VisitBreakNode(BreakNode Node) {
 		this.PushCode("break");
 	}
 
 	@Override
 	public void VisitContinueNode(ContinueNode Node) {
 		this.PushCode("continue");
 	}
 
 	@Override
 	public void VisitJumpNode(JumpNode Node) {
 		/*local*/String Label = Node.Label;
 		this.PushCode("goto " + Label);
 		return;
 	}
 
 	@Override
 	public void VisitTryNode(TryNode Node) {
 		this.VisitBlockJS(Node.TryBlock);
 		//		/* FIXME: Do not use for statement */for(int i = 0; i < Node.CatchBlock.size(); i++) {
 		//			TypedNode Block = (TypedNode) Node.CatchBlock.get(i);
 		//			TypedNode Exception = (TypedNode) Node.TargetException.get(i);
 		//			this.VisitBlockJS(Block);
 		//		}
 		this.VisitBlockJS(Node.FinallyBlock);
 
 		/*local*/String FinallyBlock = this.PopCode();
 		/*local*/String CatchBlocks = this.PopManyCodeWithModifier(Node.CatchBlock.CountForrowingNode(), true, "catch() ", null, null);
 		/*local*/String TryBlock = this.PopCode();
 		this.PushCode("try " + TryBlock + "" + CatchBlocks + "finally " + FinallyBlock);
 		return;
 	}
 
 	@Override
 	public void VisitThrowNode(ThrowNode Node) {
 		Node.Expr.Evaluate(this);
 		/*local*/String Expr = this.PopCode();
 		this.PushCode("throw " + Expr);
 		return;
 	}
 
 	@Override
 	public void VisitFunctionNode(FunctionNode Node) {
 		// TODO Auto-generated method stub
 		return;
 	}
 
 	@Override
 	public void VisitErrorNode(ErrorNode Node) {
 		/*local*/String Expr = Node.toString();
 		this.PushCode("throw new Error(\"" + Expr + "\")");
 		return;
 	}
 
 	// This must be extended in each language
 	@Override 
 	public Object Eval(TypedNode Node) {
 		VisitBlock(Node);
 		/*local*/String ret = this.PopCode() + ";\n";
 		while(this.GeneratedCodeStack.size() > 0){
 			ret = this.PopCode() + ";\n" + ret;
 		}
 		System.out.println(ret);
 		return ret;
 	}
 
 }
 
