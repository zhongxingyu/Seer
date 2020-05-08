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
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 //endif VAJA
 
 /* language */
 // GreenTea Generator should be written in each language.
 
 class GtNode extends GtStatic {
 	/*field*/public GtNode	ParentNode;
 	/*field*/public GtNode	PrevNode;
 	/*field*/public GtNode	NextNode;
 
 	/*field*/public GtType	Type;
 	/*field*/public GtToken	Token;
 
 	GtNode/*constructor*/(GtType Type, GtToken Token) {
 		this.Type = Type;
 		this.Token = Token;
 		this.ParentNode = null;
 		this.PrevNode = null;
 		this.NextNode = null;
 	}
 
 	public final GtNode MoveHeadNode() {
 		/*local*/GtNode Node = this;
 		while(Node.PrevNode != null) {
 			Node = Node.PrevNode;
 		}
 		return Node;
 	}
 
 	public final GtNode MoveTailNode() {
 		/*local*/GtNode Node = this;
 		while(Node.NextNode != null) {
 			Node = Node.NextNode;
 		}
 		return Node;
 	}
 
 	public void Append(GtNode Node) {
 		/*extension*/
 	}
 
 	public final void AppendNodeList(ArrayList<GtNode> NodeList) {
 		/*local*/int i = 0;
 		while(i < ListSize(NodeList)) {
 			this.Append(NodeList.get(i));
 			i = i + 1;
 		}
 	}
 	
 	public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitEmptyNode(this);  /* must override */
 	}
 
 	public final boolean IsError() {
 		return (this instanceof ErrorNode);
 	}
 
 	@Override public String toString() {
 		return "(TypedNode)";
 	}
 
 	public static String Stringify(GtNode Block) {
 		/*local*/String Text = Block.toString();
 		while(Block != null) {
 			Text += Block.toString() + " ";
 			Block = Block.NextNode;
 		}
 		return Text;
 	}
 
 	public int CountForrowingNode() {
 		/*local*/int n = 0;
 		/*local*/GtNode node = this;
 		while(node != null){
 			n++;
 			node = node.NextNode;
 		}
 		return n;
 	}
 }
 
 class ConstNode extends GtNode {
 	/*field*/public Object	ConstValue;
 	ConstNode/*constructor*/(GtType Type, GtToken Token, Object ConstValue) {
 		super(Type, Token);
 		this.ConstValue = ConstValue;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitConstNode(this);
 	}
 	@Override public String toString() {
 		return "(Const:" + this.Type + " "+ this.ConstValue.toString() + ")";
 	}
 }
 
 class LocalNode extends GtNode {
 	/*field*/public String NativeName;
 	LocalNode/*constructor*/(GtType Type, GtToken Token, String NativeName) {
 		super(Type, Token);
 		this.NativeName = NativeName;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLocalNode(this);
 	}
 	@Override public String toString() {
 		return "(Local:" + this.Type + " " + this.NativeName + ")";
 	}
 }
 
 class NullNode extends GtNode {
 	NullNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitNullNode(this);
 	}
 	@Override public String toString() {
 		return "(Null:" + this.Type + " " + ")";
 	}
 }
 
 //E.g., "~" $Expr
 class CastNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtType	CastType;
 	/*field*/public GtNode	Expr;
 	CastNode/*constructor*/(GtType Type, GtToken Token, GtType CastType, GtNode Expr) {
 		super(Type, Token);
 		this.CastType = CastType;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitCastNode(this);
 	}
 }
 
 // E.g., "~" $Expr
 class UnaryNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	Expr;
 	UnaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitUnaryNode(this);
 	}
 }
 
 // E.g.,  $Expr "++"
 class SuffixNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	Expr;
 	SuffixNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSuffixNode(this);
 	}
 }
 
 //E.g., "exists" $Expr
 class ExistsNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	Expr;
 	ExistsNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitExistsNode(this);
 	}
 }
 
 //E.g., $LeftNode = $RightNode
 class AssignNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	 RightNode;
 	AssignNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitAssignNode(this);
 	}
 	@Override public String toString() {
 		return "(Assign:" + this.Type + " " + GtNode.Stringify(this.LeftNode) + " = " + GtNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $LeftNode += $RightNode
 class SelfAssignNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	 RightNode;
 	SelfAssignNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSelfAssignNode(this);
 	}
 	@Override public String toString() {
 		return "(Assign:" + this.Type + " " + GtNode.Stringify(this.LeftNode) + " = " + GtNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $LeftNode || $RightNode
 class InstanceOfNode extends GtNode {
 	/*field*/public GtNode   ExprNode;
 	/*field*/public GtType	 TypeInfo;
 	InstanceOfNode/*constructor*/(GtType Type, GtToken Token, GtNode ExprNode, GtType TypeInfo) {
 		super(Type, Token);
 		this.ExprNode = ExprNode;
 		this.TypeInfo = TypeInfo;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitInstanceOfNode(this);
 	}
 }
 
 // E.g., $LeftNode "+" $RightNode
 class BinaryNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	BinaryNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.Func = Func;
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitBinaryNode(this);
 	}
 }
 
 //E.g., $LeftNode && $RightNode
 class AndNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	AndNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitAndNode(this);
 	}
 	@Override public String toString() {
 		return "(And:" + this.Type + " " + GtNode.Stringify(this.LeftNode) + ", " + GtNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $LeftNode || $RightNode
 class OrNode extends GtNode {
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	OrNode/*constructor*/(GtType Type, GtToken Token, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.LeftNode  = Left;
 		this.RightNode = Right;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitOrNode(this);
 	}
 	@Override public String toString() {
 		return "(Or:" + this.Type + " " + GtNode.Stringify(this.LeftNode) + ", " + GtNode.Stringify(this.RightNode) + ")";
 	}
 }
 
 //E.g., $CondExpr "?" $ThenExpr ":" $ElseExpr
 class TrinaryNode extends GtNode {
 	/*field*/public GtFunc    Func;
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	ThenExpr;
 	/*field*/public GtNode	ElseExpr;
 	TrinaryNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenExpr, GtNode ElseExpr) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenExpr = ThenExpr;
 		this.ElseExpr = ElseExpr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitTrinaryNode(this);
 	}
 }
 
 //E.g., $Expr . Token.ParsedText
 class GetterNode extends GtNode {
 	/*field*/public GtNode Expr;
 	/*field*/public GtFunc  Func;
 	GetterNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitGetterNode(this);
 	}
 	@Override public String toString() {
 		return "(Getter:" + this.Type + " " + GtNode.Stringify(this.Expr) + ", " + this.Func.FuncName + ")";
 	}
 }
 
 //E.g., $Expr "[" $Indexer "]"
 class IndexerNode extends GtNode {
 	/*field*/public GtFunc  Func;
 	/*field*/public GtNode Expr;
 	/*field*/public GtNode IndexAt;
 	IndexerNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr, GtNode IndexAt) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.IndexAt = IndexAt;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitIndexerNode(this);
 	}
 	@Override public String toString() {
 		return "(Index:" + this.Type + " " + GtNode.Stringify(this.Expr) + ", " + GtNode.Stringify(this.IndexAt) + ")";
 	}
 }
 
 //E.g., $Expr "[" $Index ":" $Index2 "]"
 class SliceNode extends GtNode {
 	/*field*/public GtFunc  Func;
 	/*field*/public GtNode Expr;
 	/*field*/public GtNode Index1;
 	/*field*/public GtNode Index2;
 	SliceNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func, GtNode Expr, GtNode Index1, GtNode Index2) {
 		super(Type, Token);
 		this.Func = Func;
 		this.Expr = Expr;
 		this.Index1 = Index1;
 		this.Index2 = Index2;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSliceNode(this);
 	}
 	@Override public String toString() {
 		return "(Index:" + this.Type + " " + GtNode.Stringify(this.Expr) + ", " + GtNode.Stringify(this.Index1) + ")";
 	}
 }
 
 class LetNode extends GtNode {
 	/*field*/public GtType	DeclType;
 	/*field*/public String	VariableName;
 	/*field*/public GtNode	InitNode;
 	/*field*/public GtNode	BlockNode;
 	/* let VarNode in Block end */
 	LetNode/*constructor*/(GtType Type, GtToken Token, GtType DeclType, String VarName, GtNode InitNode, GtNode Block) {
 		super(Type, Token);
 		this.VariableName = VarName;
 		this.DeclType = DeclType;
 		this.InitNode  = InitNode;
 		this.BlockNode = Block;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLetNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Block = GtNode.Stringify(this.BlockNode);
 		/*local*/String Init  = "null";
 		if(this.InitNode != null) {
 			Init = GtNode.Stringify(this.InitNode);
 		}
 		return "(Let:" + this.Type + " " + this.VariableName + " = " +  Init  +" in {" + Block + "})";
 	}
 }
 
 // E.g., $Param[0] "(" $Param[1], $Param[2], ... ")"
 class ApplyNode extends GtNode {
 	/*field*/public GtFunc	Func;
 	/*field*/public ArrayList<GtNode>  Params; /* [arg1, arg2, ...] */
 	ApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtFunc Func) {
 		super(Type, KeyToken);
 		this.Func = Func;
 		this.Params = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitApplyNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/GtNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += GtNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Apply:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., $Recv.Func "(" $Param[0], $Param[1], ... ")"
 class MessageNode extends GtNode {
 	/*field*/public GtFunc	Func;
 	/*field*/public GtNode   RecvNode;
 	/*field*/public ArrayList<GtNode>  Params;
 	MessageNode/*constructor*/(GtType Type, GtToken KeyToken, GtFunc Func, GtNode RecvNode) {
 		super(Type, KeyToken);
 		this.Func = Func;
 		this.RecvNode = RecvNode;
 		this.Params = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitMessageNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/GtNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += GtNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Message:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., "new" $Type "(" $Param[0], $Param[1], ... ")"
 class NewNode extends GtNode {
 	/*field*/public ArrayList<GtNode>	Params;
 	/*field*/GtFunc Func;
 	NewNode/*constructor*/(GtType Type, GtToken Token, GtFunc Func) {
 		super(Type, Token);
 		this.Params = new ArrayList<GtNode>();
 		this.Func = Func;
 		this.Params.add(new ConstNode(Func.GetFuncType(), Token, Func));
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitNewNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/GtNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += GtNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(New:" + this.Type + " " + Param + ")";
 	}
 }
 
 //E.g., "if" "(" $Cond ")" $ThenNode "else" $ElseNode
 class IfNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	ThenNode;
 	/*field*/public GtNode	ElseNode;
 	/* If CondExpr then ThenBlock else ElseBlock */
 	IfNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode ThenBlock, GtNode ElseNode) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.ThenNode = ThenBlock;
 		this.ElseNode = ElseNode;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitIfNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = GtNode.Stringify(this.CondExpr);
 		/*local*/String Then = GtNode.Stringify(this.ThenNode);
 		/*local*/String Else = GtNode.Stringify(this.ElseNode);
 		return "(If:" + this.Type + " Cond:" + Cond + " Then:"+ Then + " Else:" + Else + ")";
 	}
 }
 
 //E.g., "while" "(" $CondExpr ")" $LoopBody
 class WhileNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	LoopBody;
 	WhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = GtNode.Stringify(this.CondExpr);
 		/*local*/String Body = GtNode.Stringify(this.LoopBody);
 		return "(While:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 class DoWhileNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	LoopBody;
 	DoWhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitDoWhileNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = GtNode.Stringify(this.CondExpr);
 		/*local*/String Body = GtNode.Stringify(this.LoopBody);
 		return "(DoWhile:" + this.Type + " Cond:" + Cond + " Body:"+ Body + ")";
 	}
 }
 
 //E.g., "for" "(" ";" $CondExpr ";" $IterExpr ")" $LoopNode
 class ForNode extends GtNode {
 	/*field*/public GtNode	CondExpr;
 	/*field*/public GtNode	IterExpr;
 	/*field*/public GtNode	LoopBody;
 	ForNode/*constructor*/(GtType Type, GtToken Token, GtNode CondExpr, GtNode IterExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.CondExpr = CondExpr;
 		this.LoopBody = LoopBody;
 		this.IterExpr = IterExpr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitForNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Cond = GtNode.Stringify(this.CondExpr);
 		/*local*/String Body = GtNode.Stringify(this.LoopBody);
 		/*local*/String Iter = GtNode.Stringify(this.IterExpr);
 		return "(For:" + this.Type + " Cond:" + Cond + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 //E.g., "for" "(" $Variable ":" $IterExpr ")" $LoopNode
 class ForEachNode extends GtNode {
 	/*field*/public GtNode	Variable;
 	/*field*/public GtNode	IterExpr;
 	/*field*/public GtNode	LoopBody;
 	ForEachNode/*constructor*/(GtType Type, GtToken Token, GtNode Variable, GtNode IterExpr, GtNode LoopBody) {
 		super(Type, Token);
 		this.Variable = Variable;
 		this.IterExpr = IterExpr;
 		this.LoopBody = LoopBody;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitForEachNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Var = GtNode.Stringify(this.Variable);
 		/*local*/String Body = GtNode.Stringify(this.LoopBody);
 		/*local*/String Iter = GtNode.Stringify(this.IterExpr);
 		return "(Foreach:" + this.Type + " Var:" + Var + " Body:"+ Body + " Iter:" + Iter + ")";
 	}
 }
 
 @Deprecated class LabelNode extends GtNode {
 	/*field*/public String Label;
 	LabelNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLabelNode(this);
 	}
 	@Override public String toString() {
 		return "(Label:" + this.Type + " " + this.Label + ")";
 	}
 }
 
 @Deprecated class JumpNode extends GtNode {
 	/*field*/public String Label;
 	JumpNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitJumpNode(this);
 	}
 	@Override public String toString() {
 		return "(Jump:" + this.Type + " " + this.Label + ")";
 	}
 }
 
 class ContinueNode extends GtNode {
 	/*field*/public String Label;
 	ContinueNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitContinueNode(this);
 	}
 	@Override public String toString() {
 		return "(Continue:" + this.Type + ")";
 	}
 }
 
 class BreakNode extends GtNode {
 	/*field*/public String Label;
 	BreakNode/*constructor*/(GtType Type, GtToken Token, String Label) {
 		super(Type, Token);
 		this.Label = Label;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitBreakNode(this);
 	}
 	@Override public String toString() {
 		return "(Break:" + this.Type + ")";
 	}
 }
 
 class ReturnNode extends GtNode {
 	/*field*/public GtNode Expr;
 	ReturnNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitReturnNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Text = "";
 		if(Text != null) {
 			Text = GtNode.Stringify(this.Expr);
 		}
 		return "(Return:" + this.Type + " " + Text + ")";
 	}
 }
 
 class ThrowNode extends GtNode {
 	/*field*/public GtNode Expr;
 	ThrowNode/*constructor*/(GtType Type, GtToken Token, GtNode Expr) {
 		super(Type, Token);
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitThrowNode(this);
 	}
 	@Override public String toString() {
 		return "(Throw:" + this.Type + " " + GtNode.Stringify(this.Expr) + ")";
 	}
 }
 
 class TryNode extends GtNode {
 	/*field*/public GtNode	TryBlock;
 	/*field*/public GtNode	CatchExpr;
 	/*field*/public GtNode	CatchBlock;
 	/*field*/public GtNode	FinallyBlock;
 	TryNode/*constructor*/(GtType Type, GtToken Token, GtNode TryBlock, GtNode CatchExpr, GtNode CatchBlock, GtNode FinallyBlock) {
 		super(Type, Token);
 		this.TryBlock = TryBlock;
 		this.CatchExpr = CatchExpr;
 		this.CatchBlock = CatchBlock;
 		this.FinallyBlock = FinallyBlock;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitTryNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String TryBlock = GtNode.Stringify(this.TryBlock);
 		return "(Try:" + this.Type + " " + TryBlock + ")";
 	}
 }
 
 class SwitchNode extends GtNode {
 	SwitchNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	//public TypedNode	CondExpr;
 	//public ArrayList<TypedNode>	Labels;
 	//public ArrayList<TypedNode>	Blocks;
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSwitchNode(this);
 	}
 	@Override public String toString() {
 		//FIXME
 		return "(Switch:" + this.Type + ")";
 	}
 }
 
 class FunctionNode extends GtNode {
 	FunctionNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token); // TODO
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitFunctionNode(this);
 	}
 	@Override public String toString() {
 		return "(Function:" + this.Type + ")";
 	}
 }
 
 class ErrorNode extends GtNode {
 	ErrorNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitErrorNode(this);
 	}
 	@Override public String toString() {
 		return "(Error:" + this.Type + " " + this.Token.toString() + ")";
 	}
 }
 
 // E.g., "ls" "-a"..
 class CommandNode extends GtNode {
 	/*field*/public ArrayList<GtNode>  Params; /* ["ls", "-la", "/", ...] */
 	/*field*/public GtNode PipedNextNode;
 	CommandNode/*constructor*/(GtType Type, GtToken KeyToken, GtNode PipedNextNode) {
 		super(Type, KeyToken);
 		this.PipedNextNode = PipedNextNode;
 		this.Params = new ArrayList<GtNode>();
 	}
 	@Override public void Append(GtNode Expr) {
 		this.Params.add(Expr);
 	}
 
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitCommandNode(this);
 	}
 	@Override public String toString() {
 		/*local*/String Param = "";
 		/*local*/int i = 0;
 		while(i < GtStatic.ListSize(this.Params)) {
 			/*local*/GtNode Node = this.Params.get(i);
 			if(i != 0) {
 				Param += ", ";
 			}
 			Param += GtNode.Stringify(Node);
 			i = i + 1;
 		}
 		return "(Command:" + this.Type + " " + Param + ")";
 	}
 }
 
 class GtType extends GtStatic {
 	/*field*/public final GtClassContext	Context;
 	/*field*/public GtNameSpace     PackageNameSpace;
 	/*field*/int					ClassFlag;
 	/*field*/int                    ClassId;
 	/*field*/public String			ShortClassName;
 	/*field*/GtType					SuperType;
 	/*field*/public GtType			SearchSuperFuncClass;
 	/*field*/public Object			DefaultNullValue;
 //	/*field*/public GtMap           ClassSymbolTable;
 	/*field*/GtType					BaseType;
 	/*field*/GtType[]				TypeParams;
 	/*field*/public Object          NativeSpec;
 
 	GtType/*constructor*/(GtClassContext Context, int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		this.Context = Context;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperType = null;
 		this.BaseType = this;
 		this.SearchSuperFuncClass = null;
 		this.DefaultNullValue = DefaultNullValue;
 		this.NativeSpec = NativeSpec;
 //		this.ClassSymbolTable = IsFlag(ClassFlag, EnumClass) ? (/*cast*/GtMap)NativeSpec : null;
 		this.ClassId = Context.ClassCount;
 		Context.ClassCount += 1;
 		this.TypeParams = null;
 	}
 
 	public GtType CreateSubType(int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		GtType SubType = new GtType(this.Context, ClassFlag, ClassName, DefaultNullValue, NativeSpec);
 		SubType.SuperType = this;
 		SubType.SearchSuperFuncClass = this;
 		return SubType;
 	}
 	
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		GtType GenericType = new GtType(this.Context, this.ClassFlag, ShortName, null, null);
 		GenericType.BaseType = this.BaseType;
 		GenericType.SearchSuperFuncClass = this.BaseType;
 		GenericType.SuperType = this.SuperType;
 		GenericType.TypeParams = LibGreenTea.CompactTypeList(BaseIndex, TypeList);
 		LibGreenTea.DebugP("new class: " + GenericType.ShortClassName + ", ClassId=" + GenericType.ClassId);
 		return GenericType;
 	}
 
 	public final boolean IsNative() {
 		return IsFlag(this.ClassFlag, NativeClass);
 	}
 
 	public final boolean IsDynamic() {
 		return IsFlag(this.ClassFlag, DynamicClass);
 	}
 
 	public final boolean IsGenericType() {
 		return (this.TypeParams != null);
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 //	public final Object GetClassSymbol(String Key, boolean RecursiveSearch) {
 //		GtType Type = this;
 //		while(Type != null) {
 //			if(Type.ClassSymbolTable != null) {
 //				return Type.ClassSymbolTable.get(Key);
 //			}
 //			Type = (RecursiveSearch) ? Type.SuperType : null;
 //		}
 //		return null;
 //	}
 //
 //	public final void SetClassSymbol(String Key, Object Value) {
 //		if(this.ClassSymbolTable == null) {
 //			this.ClassSymbolTable = new GtMap();
 //		}
 //		this.ClassSymbolTable.put(Key, Value);
 //	}
 	
 	public final String GetUniqueName() {
 		if(LibGreenTea.DebugMode) {
 			return this.BaseType.ShortClassName + NativeNameSuffix + GtStatic.NumberToAscii(this.ClassId);
 		}
 		else {
 			return NativeNameSuffix + GtStatic.NumberToAscii(this.ClassId);
 		}
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type || this == this.Context.AnyType) {
 			return true;
 		}
 		GtType SuperClass = this.SuperType;
 		while(SuperClass != null) {
 			if(SuperClass == Type) {
 				return true;
 			}
 			SuperClass = SuperClass.SuperType;
 		}
 		return this.Context.CheckSubType(Type, this);
 	}
 
 	public void SetClassField(GtClassField ClassField) {
 		this.NativeSpec = ClassField;
 	}
 
 }
 
 class GtFunc extends GtStatic {
 	/*field*/public int				FuncFlag;
 //	/*field*/int					FuncSymbolId;
 	/*field*/public String			FuncName;
 	/*field*/public String          MangledName;
 	/*field*/public GtType[]		Types;
 	/*field*/private GtType         FuncType;
 	/*field*/public GtFunc        ListedFuncs;
 	/*field*/public Object          NativeRef;  // Abstract function if null 
 
 	GtFunc/*constructor*/(int FuncFlag, String FuncName, int BaseIndex, ArrayList<GtType> ParamList) {
 		this.FuncFlag = FuncFlag;
 		this.FuncName = FuncName;
 //		this.FuncSymbolId = GtStatic.GetSymbolId(FuncName, CreateNewSymbolId);
 		this.Types = LibGreenTea.CompactTypeList(BaseIndex, ParamList);
 		LibGreenTea.Assert(this.Types.length > 0);
 		this.ListedFuncs = null;
 		this.FuncType = null;
 		this.NativeRef = null;
 		this.MangledName = GtStatic.MangleFuncName(this.GetRecvType(), this.FuncName, BaseIndex+2, ParamList);
 	}
 
 	public final String GetNativeFuncName() {
 		if(this.Is(ExportFunc)) {
 			return this.FuncName;
 		}
 		else {
 			return this.MangledName;
 		}
 	}
 
 	public final GtType GetFuncType() {
 		if(this.FuncType == null) {
 			GtClassContext Context = this.GetRecvType().Context;
 			this.FuncType = Context.GetGenericType(Context.FuncType, 0, new ArrayList<GtType>(Arrays.asList(this.Types)), true);
 		}
 		return this.FuncType;
 	}
 
 	@Override public String toString() {
 		/*local*/String s = this.FuncName + "(";
 		/*local*/int i = 0;
 		while(i < this.GetFuncParamSize()) {
 			/*local*/GtType ParamType = this.GetFuncParamType(i);
 			if(i > 0) {
 				s += ", ";
 			}
 			s += ParamType.ShortClassName;
 			i += 1;
 		}
 		return s + ": " + this.GetReturnType();
 	}
 
 	public boolean Is(int Flag) {
 		return IsFlag(this.FuncFlag, Flag);
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
 
 	public final int GetFuncParamSize() {
 		return this.Types.length - 1;
 	}
 
 	public final GtType GetFuncParamType(int ParamIdx) {
 		return this.Types[ParamIdx+1];
 	}
 
 	public final int GetMethodParamSize() {
 		return this.Types.length - 2;
 	}
 		
 	public final boolean EqualsParamTypes(int BaseIndex, GtType[] ParamTypes) {
 		if(this.Types.length == ParamTypes.length) {
 			/*local*/int i = BaseIndex;
 			while(i < this.Types.length) {
 				if(this.Types[i] != ParamTypes[i]) {
 					return false;
 				}
 				i = i + 1;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public final boolean EqualsType(GtFunc AFunc) {
 		return this.EqualsParamTypes(0, this.Types);
 	}
 
 	public final boolean IsAbstract() {
 		return this.NativeRef == null;
 	}
 	
 	public final void SetNativeMacro(String NativeMacro) {
 		LibGreenTea.Assert(this.NativeRef == null);
 		this.FuncFlag |= NativeMacroFunc;
 		this.NativeRef = NativeMacro;
 	}
 	
 	public final String GetNativeMacro() {
 		return (/*cast*/String)this.NativeRef;
 	}
 
 	public final String ApplyNativeMacro(int BaseIndex, String[] ParamCode) {
 		/*local*/String NativeMacro = "$1 " + this.FuncName + " $2";
 		if(IsFlag(this.FuncFlag, NativeMacroFunc)) {
 			NativeMacro = this.GetNativeMacro();
 		}
 		/*local*/String Code = NativeMacro.replace("$1", ParamCode[BaseIndex]);
 		if(ParamCode.length == BaseIndex + 1) {
 			Code = Code.replace("$2", "");
 		}
 		else {
 			Code = Code.replace("$2", ParamCode[BaseIndex + 1]);
 		}
 		return Code;
 	}
 
 	public final void SetNativeMethod(int OptionalFuncFlag, Object Method) {
 		LibGreenTea.Assert(this.NativeRef == null);
 		this.FuncFlag |= NativeFunc | OptionalFuncFlag;
 		this.NativeRef = Method;
 	}
 		
 }
 
 class GtPolyFunc extends GtStatic {
 	/*field*/public GtNameSpace NameSpace;
 	/*field*/public ArrayList<GtFunc> FuncList;
 
 	GtPolyFunc/*constructor*/(GtNameSpace NameSpace, GtFunc Func1) {
 		this.NameSpace = NameSpace;
 		this.FuncList = new ArrayList<GtFunc>();
 		this.FuncList.add(Func1);
 	}
 	
 	@Override public String toString() { // this is used in an error message
 		/*local*/String s = "";
 		/*local*/int i = 0;
 		while(i < this.FuncList.size()) {
 			if(i > 0) {
 				s = s + " ";
 			}
 			s = s + this.FuncList.get(i);
 			i = i + 1;
 		}
 		return s;
 	}
 	
 	public final GtPolyFunc Dup(GtNameSpace NameSpace) {
 		if(this.NameSpace != NameSpace) {
 			/*local*/GtPolyFunc PolyFunc = new GtPolyFunc(NameSpace, this.FuncList.get(0));
 			/*local*/int i = 1;
 			while(i < this.FuncList.size()) {
 				PolyFunc.FuncList.add(this.FuncList.get(i));
 				i = i + 1;
 			}
 			return PolyFunc;
 		}
 		return this;
 	}
 
 	public final GtFunc Append(GtFunc Func) {
 		/*local*/int i = 0;
 		while(i < this.FuncList.size()) {
 			/*local*/GtFunc ListedFunc = this.FuncList.get(i); 
 			if(ListedFunc == Func) {
 				return null; /* same function */
 			}
 			if(Func.EqualsType(ListedFunc)) {
 				this.FuncList.add(Func);
 				return ListedFunc;
 			}
 			i = i + 1;
 		}
 		this.FuncList.add(Func);
 		return null;
 	}
 
 	public GtFunc ResolveUnaryFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtNode ExprNode) {
 		/*local*/int i = this.FuncList.size() - 1;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == 1 && Func.Types[1].Accept(ExprNode.Type)) {
 				return Func;
 			}
 			i = i - 1;
 		}
 		return null;
 	}
 
 	public final GtFunc ResolveBinaryFunc(GtTypeEnv Gamma, GtNode[] BinaryNodes) {
 		/*local*/int i = this.FuncList.size() - 1;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == 2 && Func.Types[1].Accept(BinaryNodes[0].Type) && Func.Types[2].Accept(BinaryNodes[1].Type)) {
 				return Func;
 			}
 			i = i - 1;
 		}
 		i = this.FuncList.size() - 1;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == 2 && Func.Types[1].Accept(BinaryNodes[0].Type)) {
 				GtFunc TypeCoercion = Gamma.NameSpace.GetCoercionFunc(BinaryNodes[1].Type, Func.Types[2], true);
 				if(TypeCoercion != null) {
 					BinaryNodes[1] = Gamma.CreateCoercionNode(Func.Types[2], TypeCoercion, BinaryNodes[1]);
 					return Func;
 				}
 			}
 			i = i - 1;
 		}
 		return null;
 	}
 
 	public GtFunc IncrementalMatch(int FuncParamSize, ArrayList<GtNode> NodeList) {
 		/*local*/int i = this.FuncList.size() - 1;
 		/*local*/GtFunc ResolvedFunc = null;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == FuncParamSize) {
 				/*local*/int p = 0;
 				while(p < NodeList.size()) {
 					GtNode Node = NodeList.get(p);
 					if(!Func.Types[p + 1].Accept(Node.Type)) {
 						Func = null;
 						break;
 					}
 					p = p + 1;
 				}
 				if(Func != null) {
 					if(ResolvedFunc != null) {
 						return null; // two more func
 					}
 					ResolvedFunc = Func;
 				}
 			}
 			i = i - 1;
 		}
 		return ResolvedFunc;
 	}
 
 	
 	public GtFunc MatchAcceptableFunc(GtTypeEnv Gamma, int FuncParamSize, ArrayList<GtNode> NodeList) {
 		/*local*/int i = this.FuncList.size() - 1;
 		while(i >= 0) {
 			/*local*/GtFunc Func = this.FuncList.get(i);
 			if(Func.GetFuncParamSize() == FuncParamSize) {
 				/*local*/int p = 0;
 				/*local*/GtNode Coercions[] = null;
 				while(p < NodeList.size()) {
 					GtNode Node = NodeList.get(p);
 					GtType ParamType = Func.Types[p + 1];
 					if(ParamType.Accept(Node.Type)) {
 						p = p + 1;
 						continue;
 					}
 					GtFunc TypeCoercion = Gamma.NameSpace.GetCoercionFunc(Node.Type, ParamType, true);
 					if(TypeCoercion != null) {
 						if(Coercions == null) {
 							Coercions = new GtNode[NodeList.size()];
 						}
 						Coercions[p] = Gamma.CreateCoercionNode(ParamType, TypeCoercion, Node);
 						p = p + 1;
 						continue;
 					}
 					Func = null;
 					Coercions = null;
 					break;
 				}
 				if(Func != null) {
 					if(Coercions != null) {
 						i = 1;
 						while(i < Coercions.length) {
 							if(Coercions[i] != null) {
 								NodeList.set(i, Coercions[i]);
 							}
 							i = i + 1;
 						}
 						Coercions = null;
 					}
 					return Func;
 				}
 			}
 			i = i - 1;
 		}
 		return null;
 	}
 
 	public GtFunc ResolveFunc(GtTypeEnv Gamma, GtSyntaxTree ParsedTree, int TreeIndex, ArrayList<GtNode> NodeList) {
 		/*local*/int FuncParamSize = ListSize(ParsedTree.TreeList) - TreeIndex + NodeList.size();
 		/*local*/GtFunc ResolvedFunc = this.IncrementalMatch(FuncParamSize, NodeList);
 		while(ResolvedFunc == null && TreeIndex < ListSize(ParsedTree.TreeList)) {
 			/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, Gamma.VarType, DefaultTypeCheckPolicy);
 			NodeList.add(Node);
 			if(Node.IsError()) {
 				return null;
 			}
 			TreeIndex = TreeIndex + 1;
 			ResolvedFunc = this.IncrementalMatch(FuncParamSize, NodeList);
 		}
 		if(ResolvedFunc == null) {
 			return this.MatchAcceptableFunc(Gamma, FuncParamSize, NodeList);
 		}
 		while(TreeIndex < ListSize(ParsedTree.TreeList)) {
 			/*local*/GtType ContextType = ResolvedFunc.Types[NodeList.size()];
 			/*local*/GtNode Node = ParsedTree.TypeCheckNodeAt(TreeIndex, Gamma, ContextType, DefaultTypeCheckPolicy);
 			NodeList.add(Node);
 			if(Node.IsError()) {
 				return null;
 			}
 			TreeIndex = TreeIndex + 1;
 		}
 		return ResolvedFunc;
 	}
 
 	
 }
 
 
 class GtGenerator extends GtStatic {
 	/*field*/public final String      TargetCode;
 	/*field*/public GtClassContext    Context;
 	/*field*/public ArrayList<Object> GeneratedCodeStack;
 	/*field*/public String OutputFile;
 	/*field*/public int GeneratorFlag;
 	
 	GtGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		this.TargetCode = TargetCode;
 		this.OutputFile = OutputFile;
 		this.GeneratorFlag = GeneratorFlag;
 		this.Context = null;
 		this.GeneratedCodeStack = null;
 	}
 
 	public void InitContext(GtClassContext Context) {
 		this.Context = Context;
 		this.GeneratedCodeStack = new ArrayList<Object>();
 		Context.LoadFile(LibGreenTea.GetLibPath(this.TargetCode, "common"));
 	}
 
 	public final GtNode UnsupportedNode(GtType Type, GtSyntaxTree ParsedTree) {
 		/*local*/GtToken Token = ParsedTree.KeyToken;
 		Type.Context.ReportError(ErrorLevel, Token, this.TargetCode + " has no language support for " + Token.ParsedText);
 		return new ErrorNode(Type.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateConstNode(GtType Type, GtSyntaxTree ParsedTree, Object Value) {
 		return new ConstNode(Type, ParsedTree.KeyToken, Value);
 	}
 
 	public GtNode CreateNullNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new NullNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateLocalNode(GtType Type, GtSyntaxTree ParsedTree, String LocalName) {
 		return new LocalNode(Type, ParsedTree.KeyToken, LocalName);
 	}
 
 	public GtNode CreateGetterNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateIndexerNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr, GtNode Index) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Func, Expr, Index);
 	}
 
 	public GtNode CreateApplyNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func) {
 		return new ApplyNode(Type, ParsedTree == null ? GtTokenContext.NullToken : ParsedTree.KeyToken, Func);
 	}
 
 	public GtNode CreateMessageNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, GtFunc Func) {
 		return new MessageNode(Type, ParsedTree.KeyToken, Func, RecvNode);
 	}
 
 	public GtNode CreateNewNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func) {
 		return new NewNode(Type, ParsedTree.KeyToken, Func);
 	}
 
 	public GtNode CreateUnaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateSuffixNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Func, Expr);
 	}
 
 	public GtNode CreateBinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Left, GtNode Right) {
 		return new BinaryNode(Type, ParsedTree.KeyToken, Func, Left, Right);
 	}
 
 	public GtNode CreateAndNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new AndNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateOrNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new OrNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateAssignNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		return new AssignNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	public GtNode CreateLetNode(GtType Type, GtSyntaxTree ParsedTree, GtType DeclType, String VarName, GtNode InitNode, GtNode Block) {
 		return new LetNode(Type, ParsedTree.KeyToken, DeclType, VarName, InitNode, Block);
 	}
 
 	public GtNode CreateIfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Then, GtNode Else) {
 		return new IfNode(Type, ParsedTree.KeyToken, Cond, Then, Else);
 	}
 
 	public GtNode CreateSwitchNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Match) {
 		return null;
 	}
 
 	public GtNode CreateWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		return new WhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		return new DoWhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 
 	public GtNode CreateForNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode IterNode, GtNode Block) {
 		return new ForNode(Type, ParsedTree.KeyToken, Cond, Block, IterNode);
 	}
 
 	public GtNode CreateForEachNode(GtType Type, GtSyntaxTree ParsedTree, GtNode VarNode, GtNode IterNode, GtNode Block) {
 		return new ForEachNode(Type, ParsedTree.KeyToken, VarNode, IterNode, Block);
 	}
 
 	public GtNode CreateReturnNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return new ReturnNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public GtNode CreateLabelNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return null;
 	}
 
 	public GtNode CreateJumpNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node, String Label) {
 		return new JumpNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateBreakNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node, String Label) {
 		return new BreakNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateContinueNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node, String Label) {
 		return new ContinueNode(Type, ParsedTree.KeyToken, Label);
 	}
 
 	public GtNode CreateTryNode(GtType Type, GtSyntaxTree ParsedTree, GtNode TryBlock, GtNode CatchExpr, GtNode CatchNode, GtNode FinallyBlock) {
 		return new TryNode(Type, ParsedTree.KeyToken, TryBlock, CatchExpr, CatchNode, FinallyBlock);
 	}
 
 	public GtNode CreateThrowNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Node) {
 		return new ThrowNode(Type, ParsedTree.KeyToken, Node);
 	}
 
 	public GtNode CreateFunctionNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Block) {
 		return null;
 	}
 
 	public GtNode CreateEmptyNode(GtType Type) {
 		return new GtNode(Type, GtTokenContext.NullToken);
 	}
 
 	public GtNode CreateErrorNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateCommandNode(GtType Type, GtSyntaxTree ParsedTree, GtNode PipedNextNode) {
 		return new CommandNode(Type, ParsedTree.KeyToken, PipedNextNode);
 	}
 
 	/* language constructor */
 
 	public GtType GetNativeType(Object Value) {
 		GtType NativeType = this.Context.AnyType;  // if unknown 
 //ifdef JAVA
 		Class<?> NativeClassInfo = Value instanceof Class<?> ? (Class<?>)Value : Value.getClass();
 		NativeType = (GtType)this.Context.ClassNameMap.get(NativeClassInfo.getName());
 		if(NativeType == null) {
 			NativeType = new GtType(this.Context, NativeClass, NativeClassInfo.getSimpleName(), null, NativeClassInfo);
 			this.Context.ClassNameMap.put(NativeClassInfo.getName(), NativeType);
 		}
 //endif VAJA
 		return NativeType;
 	}
 
 	public boolean TransformNativeFuncs(GtType NativeBaseType, String FuncName) {
 		boolean TransformedResult = false;
 //ifdef JAVA
 		Class<?> NativeClassInfo = (Class<?>)NativeBaseType.NativeSpec;
 		Method[] Methods = NativeClassInfo.getMethods();
 		if(Methods != null) {
 			for(int i = 0; i < Methods.length; i++) {
 				if(FuncName.equals(Methods[i].getName())) {
 					int FuncFlag = NativeFunc;
 					if(Modifier.isStatic(Methods[i].getModifiers())) {
 						FuncFlag |= NativeStaticFunc;
 					}
 					ArrayList<GtType> TypeList = new ArrayList<GtType>();
 					TypeList.add(this.GetNativeType(Methods[i].getReturnType()));
 					TypeList.add(NativeBaseType);
 					Class<?>[] ParamTypes = Methods[i].getParameterTypes();
 					if(ParamTypes != null) {
 						for(int j = 0; j < Methods.length; j++) {
 							TypeList.add(this.GetNativeType(ParamTypes[j]));
 						}
 					}
 					GtFunc NativeFunc = new GtFunc(FuncFlag, FuncName, 0, TypeList);
 					NativeFunc.SetNativeMethod(FuncFlag, Methods[i]);
 					NativeBaseType.Context.RootNameSpace.AppendMethod(NativeBaseType, NativeFunc);
 					TransformedResult = false;
 				}
 			}
 		}
 //endif VAJA
 		return TransformedResult;
 	}
 
 	public void GenerateClassField(GtType Type, GtClassField ClassField) {
 		/*extension*/
 	}
 
 	public final boolean HasAnnotation(GtMap Annotation, String Key) {
 		if(Annotation != null) {
 			/*local*/Object Value = Annotation.get(Key);
 			if(Value instanceof Boolean) {
 				Annotation.put(Key, false);  // consumed;
 			}
 			return (Value != null);
 		}
 		return false;
 	}
 	
 	public int ParseClassFlag(int ClassFlag, GtMap Annotation) {
 		return ClassFlag;
 	}
 
 	public int ParseFuncFlag(int FuncFlag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(this.HasAnnotation(Annotation, "Export")) {
 				FuncFlag = FuncFlag | ExportFunc;
 			}
 			if(this.HasAnnotation(Annotation, "Public")) {
 				FuncFlag = FuncFlag | PublicFunc;
 			}
 		}
 		return FuncFlag;
 	}
 
 	public int ParseVarFlag(int VarFlag, GtMap Annotation) {
 		if(Annotation != null) {
 			if(this.HasAnnotation(Annotation, "ReadOnly")) {
 				VarFlag = VarFlag | ReadOnlyVar;
 			}
 		}
 		return VarFlag;
 	}
 
 	public GtFunc CreateFunc(int FuncFlag, String FuncName, int BaseIndex, ArrayList<GtType> TypeList) {
 		return new GtFunc(FuncFlag, FuncName, BaseIndex, TypeList);
 	}
 
 	public void GenerateFunc(GtFunc Func, ArrayList<String> ParamNameList, GtNode Body) {
 		/*extenstion*/
 	}
 
 	public void SyncCodeGeneration() {
 		/*extension*/
 	}
 	
 	
 	public final void StopVisitor(GtNode Node) {
 		Node.NextNode = null;
 	}
 	
 	//------------------------------------------------------------------------
 
 	public void VisitEmptyNode(GtNode EmptyNode) {
 		LibGreenTea.DebugP("empty node: " + EmptyNode.Token.ParsedText);
 	}
 
 	public void VisitInstanceOfNode(InstanceOfNode Node) {
 		/*extention*/
 	}
 
 	public void VisitSelfAssignNode(SelfAssignNode Node) {
 		/*extention*/
 	}
 
 	public void VisitTrinaryNode(TrinaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitExistsNode(ExistsNode Node) {
 		/*extension*/
 	}
 
 	public void VisitCastNode(CastNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSliceNode(SliceNode Node) {
 		/*extension*/
 	}
 
 	public void VisitSuffixNode(SuffixNode Node) {
 		/*extension*/
 	}
 
 	public void VisitUnaryNode(UnaryNode Node) {
 		/*extension*/
 	}
 
 	public void VisitIndexerNode(IndexerNode Node) {
 		/*extension*/
 	}
 
 	public void VisitMessageNode(MessageNode Node) {
 		/*extension*/
 	}
 
 	public void VisitWhileNode(WhileNode Node) {
 		/*extension*/
 	}
 
 	public void VisitDoWhileNode(DoWhileNode Node) {
 		/*extension*/
 	}
 
 	public void VisitForNode(ForNode Node) {
 		/*extension*/
 	}
 
 	public void VisitForEachNode(ForEachNode Node) {
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
 
 	public final void VisitBlock(GtNode Node) {
 		/*local*/GtNode CurrentNode = Node;
 		while(CurrentNode != null) {
 			CurrentNode.Evaluate(this);
 			CurrentNode = CurrentNode.NextNode;
 		}
 	}
 
 	// This must be extended in each language
 
 	public boolean IsStrictMode() {
 		return false; /* override this */
 	}
 
 	public Object Eval(GtNode Node) {
 		this.VisitBlock(Node);
 		return null;
 	}
 
 	public void FlushBuffer() {
 		/*extension*/
 	}
 
 	public String BlockComment(String Comment) {
 		return "/*" + Comment + "*/";
 	}
 
 	public void StartCompilationUnit() {
 		/*extension*/
 	}
 
 	public void FinishCompilationUnit() {
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
 
 	public String GetRecvName() {
 		return "this";  // default 
 	}
 
 	public void InvokeMainFunc(String MainFuncName) {
 		/*extension*/
 	}
 }
 
 class SourceGenerator extends GtGenerator {
 	/*field*/public String    HeaderSource;
 	/*field*/public String    BodySource;
 
 	/*field*/public String    Tab;
 	/*field*/public String    LineFeed;
 	/*field*/public int       IndentLevel;
 	/*field*/public String    CurrentLevelIndentString;
 
 	SourceGenerator/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 		this.LineFeed = "\n";
 		this.IndentLevel = 0;
 		this.Tab = "   ";
 		this.CurrentLevelIndentString = null;
 		this.HeaderSource = "";
 		this.BodySource = "";
 		
 	}
 
 	@Override public void InitContext(GtClassContext Context) {
 		super.InitContext(Context);
 		this.HeaderSource = "";
 		this.BodySource = "";
 	}
 
 	public final void WriteHeader(String Text) {
 		this.HeaderSource += Text;
 	}
 
 	public final void WriteLineHeader(String Text) {
 		this.HeaderSource += Text + this.LineFeed;
 	}
 
 	public final void WriteCode(String Text) {
 		this.BodySource += Text;
 	}
 
 	public final void WriteLineCode(String Text) {
 		this.BodySource += Text + this.LineFeed;
 	}
 
 	@Override public void FlushBuffer() {
 		LibGreenTea.WriteCode(this.OutputFile, this.HeaderSource + this.BodySource);			
 		this.HeaderSource = "";
 		this.BodySource = "";
 	}
 	
 	/* GeneratorUtils */
 
 	public final void Indent() {
 		this.IndentLevel += 1;
 		this.CurrentLevelIndentString = null;
 	}
 
 	public final void UnIndent() {
 		this.IndentLevel -= 1;
 		this.CurrentLevelIndentString = null;
 		LibGreenTea.Assert(this.IndentLevel >= 0);
 	}
 
 	public final String GetIndentString() {
 		if(this.CurrentLevelIndentString == null) {
 			this.CurrentLevelIndentString = JoinStrings(this.Tab, this.IndentLevel);
 		}
 		return this.CurrentLevelIndentString;
 	}
 
 	protected String StringfyConstValue(Object ConstValue) {
 		if(ConstValue instanceof String) {
 			/*local*/int i = 0;
 			/*local*/String Value = ConstValue.toString();
 			/*local*/String[] List = Value.split("\n");
 			Value = "";
 			while(i < List.length) {
 				Value += List[i];
 				if(i > 0) {
 					 Value += "\n";
 				}
 				i = i + 1;
 			}
 			return Value;
 		}
 		return ConstValue.toString();
 	}
 
 	protected final void PushSourceCode(String Code){
 		this.PushCode(Code);
 	}
 
 	protected final String PopSourceCode(){
 		return (/*cast*/String) this.PopCode();
 	}
 
 	public final String VisitNode(GtNode Node) {
 		Node.Evaluate(this);
 		return this.PopSourceCode();
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
 
 	public final static String GenerateApplyFunc1(GtFunc Func, String FuncName, boolean IsSuffixOp, String Arg1) {
 		String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
				Macro = Macro.substring(1, Macro.length() - 1); // remove ""
 			}
 		}
 		if(Macro == null) {
 			if(IsSuffixOp) {
 				Macro = FuncName + " $1";
 			}
 			else {
 				Macro = "$1 " + FuncName;
 			}
 		}
 		return Macro.replace("$1", Arg1);
 	}
 
 	public final static String GenerateApplyFunc2(GtFunc Func, String FuncName, String Arg1, String Arg2) {
 		String Macro = null;
 		if(Func != null) {
 			FuncName = Func.GetNativeFuncName();
 			if(IsFlag(Func.FuncFlag, NativeMacroFunc)) {
 				Macro = Func.GetNativeMacro();
				Macro = Macro.substring(1, Macro.length() - 1); // remove ""
 			}
 		}
 		if(Macro == null) {
 			Macro = "$1 " + FuncName + " $2";
 		}
 		return Macro.replace("$1", Arg1).replace("$2", Arg2);
 	}
 	
 	public String GenerateFuncTemplate(int ParamSize, GtFunc Func) {
 		/*local*/int BeginIdx = 1;
 		/*local*/int i = BeginIdx;
 		/*local*/String Template = "";
 		/*local*/boolean IsNative = false;
 		if(Func == null) {
 			Template = "$1";
 			BeginIdx = 2;
 		}
 		else if(Func.Is(NativeFunc)) {
 			Template = "$1." + Func.FuncName;
 			BeginIdx = 2;
 		}
 		else if(Func.Is(NativeMacroFunc)) {
 			Template = Func.GetNativeMacro();
			Template = Template.substring(1, Template.length() - 1); // remove ""
 			IsNative = true;
 		}
 		else {
 			Template = Func.GetNativeFuncName();
 		}
 		if(IsNative == false) {
 			Template += "(";
 			while(i < ParamSize) {
 				if(i != BeginIdx) {
 					Template += ", ";
 				}
 				Template += "$" + i;
 				i = i + 1;
 			}
 			Template += ")";
 		}
 		return Template;
 	}
 
 	public String ApplyMacro(String Template, ArrayList<GtNode> NodeList) {
 		/*local*/int i = 1;
 		/*local*/int ParamSize = GtStatic.ListSize(NodeList);
 		while(i < ParamSize) {
 			/*local*/String Param = this.VisitNode(NodeList.get(i));
 			Template = Template.replace("$" + i, Param);
 			i = i + 1;
 		}
 		return Template;
 	}
 
 	public String GenerateApplyFunc(ApplyNode Node) {
 		/*local*/int ParamSize = GtStatic.ListSize(Node.Params);
 		/*local*/String Template = this.GenerateFuncTemplate(ParamSize, Node.Func);
 		return this.ApplyMacro(Template, Node.Params);
 	}
 }
