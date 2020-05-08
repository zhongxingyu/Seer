 //ifdef JAVA
 import java.util.ArrayList;
//endif VAJA
 import java.util.Arrays;
 
 /* language */
 
 // GreenTea Generator should be written in each language.
 
 class TypedNode extends GtStatic {
 	/*field*/public TypedNode	ParentNode;
 	/*field*/public TypedNode	PrevNode;
 	/*field*/public TypedNode	NextNode;
 
 	/*field*/public GtType	Type;
 	/*field*/public GtToken	Token;
 
 	TypedNode/*constructor*/(GtType Type, GtToken Token) {
 		this.Type = Type;
 		this.Token = Token;
 		this.ParentNode = null;
 		this.PrevNode = null;
 		this.NextNode = null;
 	}
 
 	public final TypedNode MoveHeadNode() {
 		/*local*/TypedNode Node = this;
 		while(Node.PrevNode != null) {
 			Node = Node.PrevNode;
 		}
 		return Node;
 	}
 
 	public final TypedNode MoveTailNode() {
 		/*local*/TypedNode Node = this;
 		while(Node.NextNode != null) {
 			Node = Node.NextNode;
 		}
 		return Node;
 	}
 
 	public void Append(TypedNode Node) {
 		/*extension*/
 	}
 
 	public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitEmptyNode(this);  /* must override */
 	}
 
 	public final boolean IsError() {
 		return (this instanceof ErrorNode);
 	}
 
 	@Override public String toString() {
 		return "(TypedNode)";
 	}
 
 	public static String Stringify(TypedNode Block) {
 		/*local*/String Text = Block.toString();
 		while(Block != null) {
 			Text += Block.toString() + " ";
 			Block = Block.NextNode;
 		}
 		return Text;
 	}
 
 	public int CountForrowingNode() {
 		/*local*/int n = 0;
 		/*local*/TypedNode node = this;
 		while(node != null){
 			n++;
 			node = node.NextNode;
 		}
 		return n;
 	}
 }
 
 // E.g., "~" $Expr
 class UnaryNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode	Expr;
 	UnaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitUnaryNode(this);
 	}
 }
 
 // E.g.,  $Expr "++"
 class SuffixNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode	Expr;
 	SuffixNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitSuffixNode(this);
 	}
 }
 
 // E.g., $LeftNode "+" $RightNode
 class BinaryNode extends TypedNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	BinaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.Method = Method;
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitBinaryNode(this);
 	}
 }
 
 //E.g., $LeftNode && $RightNode
 class AndNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	AndNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitAndNode(this);
 	}
 	@Override public String toString() {
 		return "(And:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + ", " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $LeftNode || $RightNode
 class OrNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	OrNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitOrNode(this);
 	}
 	@Override public String toString() {
 		return "(Or:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + ", " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $LeftNode || $RightNode
 class GetterNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	/*field*/public GtMethod  Method;
 	GetterNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitGetterNode(this);
 	}
 	@Override public String toString() {
 		return "(Getter:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ", " + this.Method.MethodName + ")";
 	}
 }
 
 //E.g., $Expr "[" $Indexer "]"
 class IndexerNode extends TypedNode {
 	/*field*/public GtMethod  Method;
 	/*field*/public TypedNode Expr;
 	/*field*/public TypedNode Indexer;
 	IndexerNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, TypedNode Expr, TypedNode Indexer) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 		this.Indexer = Indexer;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitIndexerNode(this);
 	}
 	@Override public String toString() {
 		return "(Index:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ", " + TypedNode.Stringify(this.Indexer) + ")";
 	}
 }
 
 //E.g., $LeftNode = $RightNode
 class AssignNode extends TypedNode {
 	/*field*/public TypedNode   LeftNode;
 	/*field*/public TypedNode	RightNode;
 	AssignNode/*constructor*/(GtType Type, GtToken Token, TypedNode Left, TypedNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitAssignNode(this);
 	}
 	@Override public String toString() {
 		return "(Assign:" + this.Type + " " + TypedNode.Stringify(this.LeftNode) + " = " + TypedNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 class ConstNode extends TypedNode {
 	/*field*/public Object	ConstValue;
 	ConstNode/*constructor*/(GtType Type, GtToken Token, Object ConstValue) {
 		super(Type, Token);
 		this.ConstValue = ConstValue;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitConstNode(this);
 	}
 	@Override public String toString() {
 		return "(Const:" + this.Type + " "+ this.ConstValue.toString() + ")";
 	}
 }
 
 class LocalNode extends TypedNode {
 	/*field*/public String LocalName;
 	LocalNode/*constructor*/(GtType Type, GtToken Token, String LocalName) {
 		super(Type, Token);
 		this.LocalName = LocalName;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitLocalNode(this);
 	}
 	@Override public String toString() {
 		return "(Local:" + this.Type + " " + this.LocalName + ")";
 	}
 }
 
 class NullNode extends TypedNode {
 	NullNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitNullNode(this);
 	}
 	@Override public String toString() {
 		return "(Null:" + this.Type + " " + ")";
 	}
 }
 
 class LetNode extends TypedNode {
 	/*field*/public GtType	    DeclType;
 	/*field*/public TypedNode	VarNode;
 	/*field*/public TypedNode	BlockNode;
 	/* let VarNode in Block end */
 	LetNode/*constructor*/(GtType Type, GtToken Token, GtType DeclType, TypedNode VarNode, TypedNode Block) {
 		super(Type, Token);
 		this.DeclType = DeclType;
 		this.VarNode  = VarNode;
 		this.BlockNode = Block;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitLetNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Block = TypedNode.Stringify(this.BlockNode);
 		return "(Let:" + this.Type + " " + TypedNode.Stringify(this.VarNode) + " in {" + Block + "})";
 	}
 }
 
 // E.g., $Param[0] "(" $Param[1], $Param[2], ... ")"
 class ApplyNode extends TypedNode {
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<TypedNode>  Params; /* [arg1, arg2, ...] */
 	ApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method) {
 		super(Type, KeyToken);
 		this.Method = Method;
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitApplyNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/TypedNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Apply:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., $Recv.Method "(" $Param[0], $Param[1], ... ")"
 class MessageNode extends TypedNode {
 	/*field*/public GtMethod	Method;
 	/*field*/public TypedNode   RecvNode;
 	/*field*/public ArrayList<TypedNode>  Params;
 	MessageNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method, TypedNode RecvNode) {
 		super(Type, KeyToken);
 		this.Method = Method;
 		this.RecvNode = RecvNode;
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitMessageNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/TypedNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Message:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., "new" $Type "(" $Param[0], $Param[1], ... ")"
 class NewNode extends TypedNode {
 	/*field*/public ArrayList<TypedNode>	Params;
 	NewNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitNewNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/TypedNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(New:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., "if" "(" $Cond ")" $ThenNode "else" $ElseNode
 class IfNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	ThenNode;
 	/*field*/public TypedNode	ElseNode;
 	/* If CondExpr then ThenBlock else ElseBlock */
 	IfNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode ThenBlock, TypedNode ElseNode) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenNode = ThenBlock;
 		this.ElseNode = ElseNode;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitIfNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Then = TypedNode.Stringify(this.ThenNode);
 		/*local*/String Else = TypedNode.Stringify(this.ElseNode);
 		return "(If:" + this.Type + " Cond:" + Cond + " Then:"+ Then + " Else:" + Else + ")";
 	}
 }
 
 //E.g., "while" "(" $CondExpr ")" $LoopBody
 class WhileNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	LoopBody;
 	WhileNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		return "(While:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 class DoWhileNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	LoopBody;
 	DoWhileNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitDoWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		return "(DoWhile:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 //E.g., "for" "(" ";" $CondExpr ";" $IterExpr ")" $LoopNode
 class ForNode extends TypedNode {
 	/*field*/public TypedNode	CondExpr;
 	/*field*/public TypedNode	IterExpr;
 	/*field*/public TypedNode	LoopBody;
 	ForNode/*constructor*/(GtType Type, GtToken Token, TypedNode CondExpr, TypedNode IterExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.IterExpr = IterExpr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitForNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = TypedNode.Stringify(this.CondExpr);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		/*local*/String Iter = TypedNode.Stringify(this.IterExpr);
 		return "(For:" + this.Type + " Cond:" + Cond + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 //E.g., "for" "(" $Variable ":" $IterExpr ")" $LoopNode
 class ForEachNode extends TypedNode {
 	/*field*/public TypedNode	Variable;
 	/*field*/public TypedNode	IterExpr;
 	/*field*/public TypedNode	LoopBody;
 	ForEachNode/*constructor*/(GtType Type, GtToken Token, TypedNode Variable, TypedNode IterExpr, TypedNode LoopBody) {
 		super(Type, Token);
 		this.Variable = Variable;
 		this.IterExpr = IterExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitForEachNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Var = TypedNode.Stringify(this.Variable);
 		/*local*/String Body = TypedNode.Stringify(this.LoopBody);
 		/*local*/String Iter = TypedNode.Stringify(this.IterExpr);
 		return "(Foreach:" + this.Type + " Var:" + Var + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 @Deprecated class LabelNode extends TypedNode {
 	/*field*/public String Label;
 	LabelNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitLabelNode(this);
 	}
 	@Override public String toString() {
 		return "(Label:" + this.Type + " " + this.Label + ")";
 	}
 }
 
 @Deprecated class JumpNode extends TypedNode {
 	/*field*/public String Label;
 	JumpNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitJumpNode(this);
 	}
 	@Override public String toString() {
 		return "(Jump:" + this.Type + " " + this.Label + ")";
 	}
 }
 
 class ContinueNode extends TypedNode {
 	/*field*/public String Label;
 	ContinueNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitContinueNode(this);
 	}
 	@Override public String toString() {
 		return "(Continue:" + this.Type + ")";
 	}
 }
 
 class BreakNode extends TypedNode {
 	/*field*/public String Label;
 	BreakNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitBreakNode(this);
 	}
 	@Override public String toString() {
 		return "(Break:" + this.Type + ")";
 	}
 }
 
 class ReturnNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	ReturnNode/*constructor*/(GtType Type, GtToken Token, TypedNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitReturnNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Text = "";
 		if(Text != null) {
 			Text = TypedNode.Stringify(this.Expr);
 		}
 		return "(Return:" + this.Type + " " + Text + ")";
 	}
 }
 
 class ThrowNode extends TypedNode {
 	/*field*/public TypedNode Expr;
 	ThrowNode/*constructor*/(GtType Type, GtToken Token, TypedNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitThrowNode(this);
 	}
 	@Override public String toString() {
 		return "(Throw:" + this.Type + " " + TypedNode.Stringify(this.Expr) + ")";
 	}
 }
 
 class TryNode extends TypedNode {
 	public TypedNode	TryBlock;
 	public TypedNode	CatchBlock;
 	public TypedNode	FinallyBlock;
 	TryNode/*constructor*/(GtType Type, GtToken Token, TypedNode TryBlock, TypedNode FinallyBlock) {
 		super(Type, Token);
 		this.TryBlock = TryBlock;
 		this.FinallyBlock = FinallyBlock;
 		this.CatchBlock = null;
 	}
 	//public void addCatchBlock(TypedNode TargetException, TypedNode CatchBlock) { //FIXME
 	//	this.TargetException.add(TargetException);
 	//	this.CatchBlock.add(CatchBlock);
 	//}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitTryNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String TryBlock = TypedNode.Stringify(this.TryBlock);
 		return "(Try:" + this.Type + " " + TryBlock + ")";
 	}
 }
 
 class SwitchNode extends TypedNode {
 	SwitchNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	//public TypedNode	CondExpr;
 	//public ArrayList<TypedNode>	Labels;
 	//public ArrayList<TypedNode>	Blocks;
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitSwitchNode(this);
 	}
 	@Override public String toString() {
 		//FIXME
 		return "(Switch:" + this.Type + ")";
 	}
 }
 
 class FunctionNode extends TypedNode {
 	FunctionNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token); // TODO
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitFunctionNode(this);
 	}
 	@Override public String toString() {
 		return "(Function:" + this.Type + ")";
 	}
 }
 
 class ErrorNode extends TypedNode {
 	ErrorNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitErrorNode(this);
 	}
 	@Override public String toString() {
 		return "(Error:" + this.Type + " " + this.Token.toString() + ")";
 	}
 }
 
 // E.g., "ls" "-a"..
 class CommandNode extends TypedNode {
 	/*field*/public ArrayList<TypedNode>  Params; /* ["ls", "-la", "/", ...] */
 	/*field*/public TypedNode PipedNextNode;
 	CommandNode/*constructor*/(GtType Type, GtToken KeyToken, TypedNode PipedNextNode) {
 		super(Type, KeyToken);
 		this.PipedNextNode = PipedNextNode;
 		this.Params = new ArrayList<TypedNode>();
 	}
 	@Override public void Append(TypedNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(CodeGenerator Visitor) {
 		Visitor.VisitCommandNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/TypedNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += TypedNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Command:" + this.Type + " " + Param + ")";
 	}
 }
 
 class GtObject extends GtStatic {
 	/*field*/public GtType	Type;
 	GtObject/*constructor*/(GtType Type) {
 		this.Type = Type;
 	}
 }
 
 class GtType extends GtStatic {
 	/*field*/public final GtContext	Context;
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/int					ClassFlag;
 	/*field*/int                    ClassId;
 	/*field*/public String			ShortClassName;
 	/*field*/GtType					SuperClass;
 	/*field*/public GtType			SearchSuperMethodClass;
 	/*field*/public Object			DefaultNullValue;
 	/*field*/GtType					BaseClass;
 	/*field*/GtType[]				Types;
 	/*field*/public Object          LocalSpec;
 
 	GtType/*constructor*/(GtContext Context, int ClassFlag, String ClassName, Object DefaultNullValue) {
 		this.Context = Context;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperClass = null;
 		this.BaseClass = this;
 		this.SearchSuperMethodClass = null;
 		this.DefaultNullValue = DefaultNullValue;
 		this.LocalSpec = null;
 		this.ClassId = Context.ClassCount;
 		Context.ClassCount += 1;
 		this.Types = null;
 	}
 
 	public final boolean IsGenericType() {
 		return (this.Types != null);
 	}
 
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		GtType GenericType = new GtType(this.Context, this.ClassFlag, ShortName, null);
 		GenericType.BaseClass = this.BaseClass;
 		GenericType.SearchSuperMethodClass = this.BaseClass;
 		GenericType.SuperClass = this.SuperClass;
 		this.Types = LangDeps.CompactTypeList(BaseIndex, TypeList);
 		DebugP("new class: " + GenericType.ShortClassName + ", ClassId=" + GenericType.ClassId);
 		return GenericType;
 	}
 
 	public void SetParamType(GtType ParamType) {
 		this.Types = new GtType[1];
 		this.Types[0] = ParamType;
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 	public final String GetMethodId(String MethodName) {
 		return "" + this.ClassId + "@" + MethodName;
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type || this == this.Context.AnyType) {
 			return true;
 		}
 		return false;
 	}
 }
 
 class GtMethod extends GtStatic {
 	/*field*/public GtLayer         Layer;
 	/*field*/public int				MethodFlag;
 	/*field*/int					MethodSymbolId;
 	/*field*/public String			MethodName;
 	/*field*/public String          LocalFuncName;
 	/*field*/public GtType[]		Types;
 	/*field*/private GtType         FuncType;
 	/*field*/public GtMethod        ElderMethod;
 	/*field*/public String          SourceMacro;
 
 	GtMethod/*constructor*/(int MethodFlag, String MethodName, int BaseIndex, ArrayList<GtType> ParamList, String SourceMacro) {
 		this.MethodFlag = MethodFlag;
 		this.MethodName = MethodName;
 		this.MethodSymbolId = GtStatic.GetCanonicalSymbolId(MethodName);
 		this.Types = LangDeps.CompactTypeList(BaseIndex, ParamList);
 		LangDeps.Assert(this.Types.length > 0);
 		this.Layer = null;
 		this.ElderMethod = null;
 		this.FuncType = null;
 		this.SourceMacro = SourceMacro;
 		
 		String Name = this.MethodName;
 		if(!LangDeps.IsLetter(LangDeps.CharAt(Name, 0))) {
 			Name = "_operator" + this.MethodSymbolId;
 		}
 		if(!this.Is(ExportMethod)) {
 			Name = Name + "__" + GtStatic.Mangle(this.GetRecvType(), BaseIndex + 1, ParamList);
 		}
 		this.LocalFuncName = Name;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = this.MethodName + "(";
 		/*local*/int i = 0;
 		while(i < this.GetParamSize()) {
 			/*local*/GtType ParamType = this.GetParamType(i);
 			if(i > 0) {
 				s += ", ";
 			}
 			s += ParamType.ShortClassName;
 			i += 1;
 		}
 		return s + ": " + this.GetReturnType();
 	}
 
 	public boolean Is(int Flag) {
 		return IsFlag(this.MethodFlag, Flag);
 	}
 
 	public final GtType GetReturnType() {
 		return this.Types[0];
 	}
 
 	public final GtType GetRecvType() {
 		if(this.Types.length == 1){
 			return this.Types[0].Context.VoidType;
 		}
 		return this.Types[1];
 	}
 
 	public final int GetParamSize() {
 		return this.Types.length - 1;
 	}
 
 	public final GtType GetParamType(int ParamIdx) {
 		return this.Types[ParamIdx+1];
 	}
 
 	public final GtType GetFuncType() {
 		if(this.FuncType != null) {
 			GtContext Context = this.GetRecvType().Context;
 			this.FuncType = Context.GetGenericType(Context.FuncType, 0, new ArrayList<GtType>(Arrays.asList(this.Types)), true);
 		}
 		return this.FuncType;
 	}
 
 	public final String ExpandMacro1(String Arg0) {
 		if(this.SourceMacro == null) {
 			return this.MethodName + " " + Arg0;
 		}
 		else {
 			return this.SourceMacro.replaceAll("$0", Arg0);
 		}
 	}
 
 	public final String ExpandMacro2(String Arg0, String Arg1) {
 		if(this.SourceMacro == null) {
 			return Arg0 + " " + this.MethodName + " " + Arg1;
 		}
 		else {
 			return this.SourceMacro.replaceAll("$0", Arg0).replaceAll("$1", Arg1);
 		}
 	}
 
 }
 
 class CodeGenerator extends GtStatic {
 	/*field*/public String     LangName;
 	/*field*/public GtContext  Context;
 	/*field*/public ArrayList<Object> GeneratedCodeStack;
 
 	CodeGenerator/*constructor*/(String LangName) {
 		this.LangName = LangName;
 		this.Context = null;
 		this.GeneratedCodeStack = new ArrayList<Object>();
 	}
 
 	public void SetLanguageContext(GtContext Context) {
 		this.Context = Context;
 	}
 
 	public final TypedNode UnsupportedNode(GtType Type, SyntaxTree ParsedTree) {
 		/*local*/GtToken Token = ParsedTree.KeyToken;
 		ParsedTree.NameSpace.ReportError(ErrorLevel, Token, this.LangName + " has no language support for " + Token.ParsedText);
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateConstNode(GtType Type, SyntaxTree ParsedTree, Object Value) {
 		return new ConstNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public TypedNode CreateNullNode(GtType Type, SyntaxTree ParsedTree) {
 		return new NullNode(Type, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateLocalNode(GtType Type, SyntaxTree ParsedTree, String LocalName) {
 		return new LocalNode(Type, ParsedTree.KeyToken, LocalName);
 	}
 
 	public TypedNode CreateGetterNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateIndexerNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr, TypedNode Index) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Method, Expr, Index);
 	}
 
 	public TypedNode CreateApplyNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method) {
 		return new ApplyNode(Type, ParsedTree.KeyToken, Method);
 	}
 
 	public TypedNode CreateMessageNode(GtType Type, SyntaxTree ParsedTree, TypedNode RecvNode, GtMethod Method) {
 		return new MessageNode(Type, ParsedTree.KeyToken, Method, RecvNode);
 	}
 
 	public TypedNode CreateNewNode(GtType Type, SyntaxTree ParsedTree) {
 		return new NewNode(Type, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateUnaryNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateSuffixNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public TypedNode CreateBinaryNode(GtType Type, SyntaxTree ParsedTree, GtMethod Method, TypedNode Left, TypedNode Right) {
 		return new BinaryNode(Type, ParsedTree.KeyToken, Method, Left, Right);
 	}
 
 	public TypedNode CreateAndNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new AndNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateOrNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new OrNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateAssignNode(GtType Type, SyntaxTree ParsedTree, TypedNode Left, TypedNode Right) {
 		return new AssignNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public TypedNode CreateLetNode(GtType Type, SyntaxTree ParsedTree, GtType DeclType, TypedNode VarNode, TypedNode Block) {
 		return new LetNode(Type, ParsedTree.KeyToken, DeclType, VarNode, Block);
 	}
 
 	public TypedNode CreateIfNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Then, TypedNode Else) {
 		return new IfNode(Type, ParsedTree.KeyToken, Cond, Then, Else);
 	}
 
 	public TypedNode CreateSwitchNode(GtType Type, SyntaxTree ParsedTree, TypedNode Match) {
 		return null;
 	}
 
 	public TypedNode CreateWhileNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Block) {
 		return new WhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public TypedNode CreateDoWhileNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode Block) {
 		return new DoWhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public TypedNode CreateForNode(GtType Type, SyntaxTree ParsedTree, TypedNode Cond, TypedNode IterNode, TypedNode Block) {
 		return new ForNode(Type, ParsedTree.KeyToken, Cond, Block, IterNode);
 	}
 
 	public TypedNode CreateForEachNode(GtType Type, SyntaxTree ParsedTree, TypedNode VarNode, TypedNode IterNode, TypedNode Block) {
 		return new ForEachNode(Type, ParsedTree.KeyToken, VarNode, IterNode, Block);
 	}
 
 	public TypedNode CreateReturnNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return new ReturnNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public TypedNode CreateLabelNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return null;
 	}
 
 	public TypedNode CreateJumpNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new JumpNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateBreakNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new BreakNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateContinueNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node, String Label) {
 		return new ContinueNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public TypedNode CreateTryNode(GtType Type, SyntaxTree ParsedTree, TypedNode TryBlock, TypedNode FinallyBlock) {
 		return new TryNode(Type, ParsedTree.KeyToken, TryBlock, FinallyBlock);
 	}
 
 	public TypedNode CreateThrowNode(GtType Type, SyntaxTree ParsedTree, TypedNode Node) {
 		return new ThrowNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public TypedNode CreateFunctionNode(GtType Type, SyntaxTree ParsedTree, TypedNode Block) {
 		return null;
 	}
 
 	public TypedNode CreateDefineNode(GtType Type, SyntaxTree ParsedTree, Object Module) {
 		return null;
 	}
 
 	public TypedNode CreateEmptyNode(GtType Type, SyntaxTree ParsedTree) {
 		return new TypedNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateErrorNode(GtType Type, SyntaxTree ParsedTree) {
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public TypedNode CreateCommandNode(GtType Type, SyntaxTree ParsedTree, TypedNode PipedNextNode) {
 		return new CommandNode(Type, ParsedTree.KeyToken, PipedNextNode);
 	}
 
 	public int ParseMethodFlag(int MethodFlag, SyntaxTree MethodDeclTree) {
 		if(MethodDeclTree.HasAnnotation("Export")) {
 			MethodFlag = MethodFlag | ExportMethod;
 		}
 		if(MethodDeclTree.HasAnnotation("Operator")) {
 			MethodFlag = MethodFlag | OperatorMethod;
 		}
 		return MethodFlag;
 	}
 
 	public GtMethod CreateMethod(int MethodFlag, String MethodName, int BaseIndex, ArrayList<GtType> TypeList, String RawMacro) {
 		return new GtMethod(MethodFlag, MethodName, BaseIndex, TypeList, RawMacro);
 	}
 
 	//------------------------------------------------------------------------
 
 	public void VisitEmptyNode(TypedNode EmptyNode) {
 		GtStatic.DebugP("empty node: " + EmptyNode.Token.ParsedText);
 	}
 
 	public void VisitSuffixNode(SuffixNode SuffixNode) {
 		/*extension*/
 	}
 
 	public void VisitUnaryNode(UnaryNode UnaryNode) {
 		/*extension*/
 	}
 
 	public void VisitIndexerNode(IndexerNode IndexerNode) {
 		/*extension*/
 	}
 
 	public void VisitMessageNode(MessageNode MessageNode) {
 		/*extension*/
 	}
 
 	public void VisitWhileNode(WhileNode WhileNode) {
 		/*extension*/
 	}
 
 	public void VisitDoWhileNode(DoWhileNode DoWhileNode) {
 		/*extension*/
 	}
 
 	public void VisitForNode(ForNode ForNode) {
 		/*extension*/
 	}
 
 	public void VisitForEachNode(ForEachNode ForEachNode) {
 		/*extension*/
 	}
 
 	public void VisitConstNode(ConstNode Node) {
 		/*extension*/
 	}
 
 	public void VisitNewNode(NewNode Node) {
 		/*extension*/
 	}
 
 	public void VisitNullNode(NullNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLocalNode(LocalNode Node) {
 		/*extension*/
 	}
 
 	public void VisitGetterNode(GetterNode Node) {
 		/*extension*/
 	}
 
 	public void VisitApplyNode(ApplyNode Node) {
 		/*extension*/
 	}
 
 	public void VisitBinaryNode(BinaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitAndNode(AndNode Node) {
 		/*extension*/
 	}
 
 	public void VisitOrNode(OrNode Node) {
 		/*extension*/
 	}
 
 	public void VisitAssignNode(AssignNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLetNode(LetNode Node) {
 		/*extension*/
 	}
 
 	public void VisitIfNode(IfNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSwitchNode(SwitchNode Node) {
 		/*extension*/
 	}
 
 	public void VisitReturnNode(ReturnNode Node) {
 		/*extension*/
 	}
 
 	public void VisitLabelNode(LabelNode Node) {
 		/*extension*/
 	}
 
 	public void VisitJumpNode(JumpNode Node) {
 		/*extension*/
 	}
 
 	public void VisitBreakNode(BreakNode Node) {
 		/*extension*/
 	}
 
 	public void VisitContinueNode(ContinueNode Node) {
 		/*extension*/
 	}
 
 	public void VisitTryNode(TryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitThrowNode(ThrowNode Node) {
 		/*extension*/
 	}
 
 	public void VisitFunctionNode(FunctionNode Node) {
 		/*extension*/
 	}
 
 	public void VisitErrorNode(ErrorNode Node) {
 		/*extension*/
 	}
 
 	public void VisitCommandNode(CommandNode Node) {
 		/*extension*/
 	}
 
 	public final void VisitBlock(TypedNode Node) {
 		/*local*/TypedNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			CurrentNode = CurrentNode.NextNode;
 		}
 	}
 
 	// This must be extended in each language
 	public void DefineFunction(GtMethod Method, ArrayList<String> ParamNameList, TypedNode Body) {
 		/*extenstion*/
 	}
 
 	public Object Eval(TypedNode Node) {
 		this.VisitBlock(Node);
 		return null;
 	}
 
 	public void AddClass(GtType Type) {
 		/*extension*/
 	}
 
 	protected void PushCode(Object Code){
 		this.GeneratedCodeStack.add(Code);
 	}
 
 	protected final Object PopCode(){
 		/*local*/int Size = this.GeneratedCodeStack.size();
 		if(Size > 0){
 			return this.GeneratedCodeStack.remove(Size - 1);
 		}
 		return "";
 	}
 }
 
 class SourceGenerator extends CodeGenerator {
 	/*field*/public int       IndentLevel;
 	/*field*/public String    CurrentLevelIndentString;
 
 	SourceGenerator/*constructor*/(String LangName) {
 		super(LangName);
 		this.IndentLevel = 0;
 		this.CurrentLevelIndentString = null;
 	}
 
 	/* GeneratorUtils */
 
 	public final void Indent() {
 		this.IndentLevel += 1;
 		this.CurrentLevelIndentString = null;
 	}
 
 	public final void UnIndent() {
 		this.IndentLevel -= 1;
 		this.CurrentLevelIndentString = null;
 		LangDeps.Assert(this.IndentLevel >= 0);
 	}
 
 	public final String GetIndentString() {
 		if(this.CurrentLevelIndentString == null) {
 			this.CurrentLevelIndentString = JoinStrings("   ", this.IndentLevel);
 		}
 		return this.CurrentLevelIndentString;
 	}
 
 	protected String StringfyConstValue(Object ConstValue) {
 		if(ConstValue instanceof String) {
 			return "\"" + ConstValue + "\"";  // FIXME \n
 		}
 		return ConstValue.toString();
 	}
 
 	protected final void PushSourceCode(String Code){
 		this.GeneratedCodeStack.add(Code);
 	}
 
 	protected final String PopSourceCode(){
 		return (/*cast*/String)this.PopCode();
 	}
 
 	protected final String[] PopManyCode(int n) {
 		/*local*/String[] array = new String[n];
 		/*local*/int i = 0;
 		while(i < n) {
 			array[i] = this.PopSourceCode();
 			i = i + 1;
 		}
 		return array;
 	}
 
 	protected final String[] PopManyCodeReverse(int n) {
 		/*local*/String[] array = new String[n];
 		/*local*/int i = 0;
 		while(i < n) {
 			array[n - i - 1] = this.PopSourceCode();
 			i = i  + 1;
 		}
 		return array;
 	}
 
 	protected final String PopManyCodeAndJoin(int n, boolean reverse, String prefix, String suffix, String delim) {
 		if(prefix == null) {
 			prefix = "";
 		}
 		if(suffix == null) {
 			suffix = "";
 		}
 		if(delim == null) {
 			delim = "";
 		}
 		/*local*/String[] array = null;
 		if(reverse) {
 			array = this.PopManyCodeReverse(n);
 		}else{
 			array = this.PopManyCode(n);
 		}
 		/*local*/String Code = "";
 		/*local*/int i = 0;
 		while(i < n) {
 			if(i > 0) {
 				Code += delim;
 			}
 			Code = Code + prefix + array[i] + suffix;
 			i = i + 1;
 		}
 		return Code;
 	}
 
 	public final void WriteTranslatedCode(String Text) {
 		LangDeps.println(Text);
 	}
 
 }
