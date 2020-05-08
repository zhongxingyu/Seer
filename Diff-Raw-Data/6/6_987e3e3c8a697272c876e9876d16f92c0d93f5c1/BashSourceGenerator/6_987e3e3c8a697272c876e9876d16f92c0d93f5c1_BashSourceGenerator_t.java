 import java.util.ArrayList;
 
 //GreenTea Generator should be written in each language.
 
 public class BashSourceGenerator extends SourceGenerator {
 	/*local*/private boolean inFunc = false;
 	/*local*/private final String retVar = "__ret";
 
 	BashSourceGenerator() {
 		super("BashSource");
 	}
 
 	public void VisitEach(GtNode Node) {
 		/*local*/String Code = "\n";
 		this.Indent();
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			Code += this.GetIndentString() + this.PopSourceCode() + "\n";
 			CurrentNode = CurrentNode.NextNode;
 		}
 		this.UnIndent();
 		Code += this.GetIndentString();
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitEmptyNode(GtNode Node) {
 	}
 
 	@Override public void VisitIndexerNode(IndexerNode Node) {
 		Node.Indexer.Evaluate(this);
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode(this.PopSourceCode() + "[" + this.PopSourceCode() + "]");
 	}
 
 	@Override public void VisitMessageNode(MessageNode Node) {
 		// not support
 	}
 
 	@Override public void VisitWhileNode(WhileNode Node) {
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Program = "while " + this.PopSourceCode() + " ;do";
 		this.VisitEach(Node.LoopBody);
 		Program += this.PopSourceCode() + "done\n";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitDoWhileNode(DoWhileNode Node) {
 		// not support
 	}
 
 	@Override public void VisitForNode(ForNode Node) {
 		Node.IterExpr.Evaluate(this);
 		Node.CondExpr.Evaluate(this);
 		/*local*/String Cond = this.PopSourceCode();
 		/*local*/String Iter = this.PopSourceCode();
 
 		/*local*/String Program = "for((; " + Cond  + "; " + Iter + " )) ;do";
 		this.VisitEach(Node.LoopBody);
 		Program += this.PopSourceCode() + "done\n";
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitForEachNode(ForEachNode Node) {
 	}
 
 	@Override public void VisitConstNode(ConstNode Node) {
 		/*local*/String value = Node.ConstValue.toString();
 		
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
 //		String Type = Node.Type.ShortClassName;
 //		this.PushSourceCode("new " + Type);
 	}
 
 	@Override public void VisitNullNode(NullNode Node) {
 		this.PushSourceCode("NULL");
 	}
 
 	@Override public void VisitLocalNode(LocalNode Node) {
 		this.PushSourceCode(Node.LocalName);
 	}
 
 	@Override public void VisitGetterNode(GetterNode Node) {
 		// not support
 	}
 
 	private String[] EvaluateParam(ArrayList<GtNode> Params) {
 		/*local*/int Size = Params.size();
 		/*local*/String[] Programs = new String[Size];
 		for(int i = 0; i < Size; i++) {
 			/*local*/GtNode Node = Params.get(i);
 			Node.Evaluate(this);
 			Programs[Size - i - 1] = ResolveValueType(Node, this.PopSourceCode());
 		}
 		return Programs;
 	}
 
 	@Override public void VisitApplyNode(ApplyNode Node) {
 		/*local*/String Program = Node.Method.MethodName + " ";
 		/*local*/String[] Params = EvaluateParam(Node.Params);
 		for(int i = 0; i < Params.length; i++) {
 			String P = Params[i];
 			if(i != 0) {
 				Program += " ";
 			}
 			Program += P;
 		}
 		this.PushSourceCode(Program);
 	}
 
 	@Override public void VisitSuffixNode(SuffixNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		if(MethodName.equals("++")) {
 		}
 		else if(MethodName.equals("--")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("((" + this.PopSourceCode() + MethodName + "))");
 	}
 
 	@Override public void VisitUnaryNode(UnaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		
 		if(MethodName.equals("+")) {
 		}
 		else if(MethodName.equals("-")) {
 		}
 		else if(MethodName.equals("~")) {
 		}
 		else if(MethodName.equals("!")) {
 		}
 		else if(MethodName.equals("++")) {
 		}
 		else if(MethodName.equals("--")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		Node.Expr.Evaluate(this);
 		this.PushSourceCode("((" + MethodName + this.PopSourceCode() + "))");
 	}
 
 	@Override public void VisitBinaryNode(BinaryNode Node) {
 		/*local*/String MethodName = Node.Token.ParsedText;
 		
 		if(Node.Type.equals(Node.Type.Context.StringType)) {
 			Node.RightNode.Evaluate(this);
 			Node.LeftNode.Evaluate(this);
 			
 			if(MethodName.equals("+")) {
 				this.PushSourceCode(this.PopSourceCode() + this.PopSourceCode());
 				return;
 			}
 			else if(MethodName.equals("!=")) {
 			}
 			else if(MethodName.equals("==")) {
 			}
 			else {
 				throw new RuntimeException("NotSupportOperator: " + MethodName);
 			}
 			
 			Node.RightNode.Evaluate(this);
 			Node.LeftNode.Evaluate(this);
 			/*local*/String left = ResolveValueType(Node.LeftNode, this.PopSourceCode());
 			/*local*/String right = ResolveValueType(Node.RightNode, this.PopSourceCode());
 			this.PushSourceCode("((" + this.PopSourceCode() + " " + left  + " " + right + "))");
 			return;
 		}
 		
 		if(MethodName.equals("+")) {
 		}
 		else if(MethodName.equals("-")) {
 		}
 		else if(MethodName.equals("*")) {
 		}
 		else if(MethodName.equals("/")) {
 		}
 		else if(MethodName.equals("%")) {
 		}
 		else if(MethodName.equals("<<")) {
 		}
 		else if(MethodName.equals(">>")) {
 		}
 		else if(MethodName.equals("&")) {
 		}
 		else if(MethodName.equals("|")) {
 		}
 		else if(MethodName.equals("^")) {
 		}
 		else if(MethodName.equals("<=")) {
 		}
 		else if(MethodName.equals("<")) {
 		}
 		else if(MethodName.equals(">=")) {
 		}
 		else if(MethodName.equals(">")) {
 		}
 		else if(MethodName.equals("!=")) {
 		}
 		else if(MethodName.equals("==")) {
 		}
 		else {
 			throw new RuntimeException("NotSupportOperator: " + MethodName);
 		}
 		
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		/*local*/String left = ResolveValueType(Node.LeftNode, this.PopSourceCode());
 		/*local*/String right = ResolveValueType(Node.RightNode, this.PopSourceCode());
 		this.PushSourceCode("((" + left + " " + MethodName + " " + right + "))");
 	}
 
 	@Override public void VisitAndNode(AndNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " && " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitOrNode(OrNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		this.PushSourceCode("(" + this.PopSourceCode() + " || " + this.PopSourceCode() + ")");
 	}
 
 	@Override public void VisitAssignNode(AssignNode Node) {
 		Node.RightNode.Evaluate(this);
 		Node.LeftNode.Evaluate(this);
 		/*local*/String left = this.PopSourceCode();
 		/*local*/String code = this.PopSourceCode();
 		/*local*/String right = ResolveValueType(Node.RightNode, code);
 		
 		this.PushSourceCode(left + "=" + right);
 	}
 
 	@Override public void VisitLetNode(LetNode Node) {
 		Node.VarNode.Evaluate(this);
 		/*local*/String Code = this.PopSourceCode();
 		this.VisitEach(Node.BlockNode);
 		
 		/*local*/String head = "";
 		if(inFunc) {
 			head = "local ";
 		}
 		this.PushSourceCode(head + Code + this.PopSourceCode());
 	}
 
 	@Override public void VisitIfNode(IfNode Node) {
 		Node.CondExpr.Evaluate(this);
 		this.VisitEach(Node.ThenNode);
 		this.VisitEach(Node.ElseNode);
 
 		/*local*/String ElseBlock = this.PopSourceCode();
 		/*local*/String ThenBlock = this.PopSourceCode();
 		/*local*/String CondExpr = this.PopSourceCode();
 		/*local*/String Code = "if " + CondExpr + " ;then" + ThenBlock;
 		if(Node.ElseNode != null) {
 			Code += "\nelse" + ElseBlock;
 		}
 		Code += "fi";
 		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitSwitchNode(SwitchNode Node) {
 
 	}
 
 	@Override public void VisitReturnNode(ReturnNode Node) {
 		if(inFunc && Node.Expr != null) {
 			Node.Expr.Evaluate(this);
 			String expr = this.PopSourceCode();
 			String ret = ResolveValueType(Node.Expr, expr);
 			
 			if(Node.Expr instanceof CommandNode) {
 				this.PushSourceCode(expr + "\nreturn " + ret);
 				return;
 			}
 			
 			if(Node.Type.equals(Node.Type.Context.BooleanType) || 
 					Node.Type.equals(Node.Type.Context.IntType)) {
 				this.PushSourceCode("return " + ret);
 				return;
 			}
 			
 			ret = retVar + "=" + ret;
 			this.PushSourceCode(ret);
 		}
 	}
 
 	@Override public void VisitLabelNode(LabelNode Node) {
 	}
 
 	@Override public void VisitJumpNode(JumpNode Node) {
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
 //		String Code = "try";
 //		//this.VisitEach(Node.CatchBlock);
 //		this.VisitEach(Node.TryBlock);
 //		Code += this.PopSourceCode();
 //		if(Node.FinallyBlock != null) {
 //			this.VisitEach(Node.FinallyBlock);
 //			Code += " finally " + this.PopSourceCode();
 //		}
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitThrowNode(ThrowNode Node) {
 //		Node.Expr.Evaluate(this);
 //		String Code = "throw " + this.PopSourceCode();
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitFunctionNode(FunctionNode Node) {
 	}
 
 	@Override public void VisitErrorNode(ErrorNode Node) {
 //		String Code = "throw Error(\"" + Node.Token.ParsedText + "\")";
 //		this.PushSourceCode(Code);
 	}
 
 	@Override public void VisitCommandNode(CommandNode Node) {	// currently only support statement
 		/*local*/String Code = "";
 		/*local*/int count = 0;
 		/*local*/CommandNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			if(count > 0) {
 				Code += " | ";
 			}
 			Code += CreateCommand(CurrentNode);
 			count++;
 			CurrentNode = (CommandNode) CurrentNode.PipedNextNode;
 		}
 		this.PushSourceCode(Code);
 	}
 
 	private String CreateCommand(CommandNode CurrentNode) {
 		/*local*/String Code = "";
 		/*local*/int size = CurrentNode.Params.size();
 		for(int i = 0; i < size; i++) {
 			CurrentNode.Params.get(i).Evaluate(this);
 			Code += this.PopSourceCode() + " ";
 		}
 		return Code;
 	}
 
 	private GtNode ResolveParamName(ArrayList<String> ParamNameList, GtNode Body) {
 		return ConvertParamName(ParamNameList, Body, 0);
 	}
 
 	private GtNode ConvertParamName(ArrayList<String> ParamNameList, GtNode Body, int index) {
 		if(index  == ParamNameList.size()) {
 			return Body;
 		}
 		
 		/*local*/GtNode varNode = new LocalNode(null, null, ParamNameList.get(index));
 		/*local*/GtNode oldVarNode = new LocalNode(null, null, "" +(index + 1));
 		/*local*/GtNode assignNode = new AssignNode(null, null, varNode, oldVarNode);
 		assignNode.NextNode = ConvertParamName(ParamNameList, Body, ++index);
 		return new LetNode(null, null, null, varNode, assignNode);
 	}
 	
	private String ResolveValueType(GtNode TargetNode, String value) {
 		String resolvedValue;
 		
 		if(TargetNode instanceof ConstNode || TargetNode instanceof NullNode) {
 			resolvedValue = value;
 		}
 		else if(TargetNode instanceof IndexerNode) {
 			resolvedValue = "${" + value + "}";
 		}
 		else if(TargetNode instanceof CommandNode) {	// TODO: support statement and expression
 			resolvedValue = "$?";
 		}
 //		else if(TargetNode instanceof ApplyNode) {
 //			//TODO
 //		}
 		else {
 			resolvedValue = "$" + value;
 		}
 		return resolvedValue;
 	}
 
 	@Override public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, GtNode Body) {
 		/*local*/String Function = "function ";
 		inFunc = true;
 		Function += Method.MethodName + "() {";
 		this.VisitEach(ResolveParamName(ParamNameList, Body));
 		Function += PopSourceCode() + "\n}";
 		this.WriteTranslatedCode(Function);
 		inFunc = false;
 	}
 
 	@Override public Object Eval(GtNode Node) {
 		this.VisitEach(Node);
 		/*local*/String Code = this.PopSourceCode();
 		this.WriteTranslatedCode(Code);
 		return Code;
 	}
 
 	@Override public void AddClass(GtType Type) {
 	}
 
 	@Override public void SetLanguageContext(GtContext Context) {
 		new JavaLayerDef().MakeDefinition(Context.DefaultNameSpace);
 	}
 }
