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
 	/*field*/public String LocalName;
 	LocalNode/*constructor*/(GtType Type, GtToken Token, String LocalName) {
 		super(Type, Token);
 		this.LocalName = LocalName;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitLocalNode(this);
 	}
 	@Override public String toString() {
 		return "(Local:" + this.Type + " " + this.LocalName + ")";
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
 	/*field*/public GtMethod    Method;
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
 	/*field*/public GtMethod    Method;
 	/*field*/public GtNode	Expr;
 	UnaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitUnaryNode(this);
 	}
 }
 
 // E.g.,  $Expr "++"
 class SuffixNode extends GtNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public GtNode	Expr;
 	SuffixNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitSuffixNode(this);
 	}
 }
 
 //E.g., "exists" $Expr
 class ExistsNode extends GtNode {
 	/*field*/public GtMethod    Method;
 	/*field*/public GtNode	Expr;
 	ExistsNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
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
 	/*field*/public GtMethod    Method;
 	/*field*/public GtNode   LeftNode;
 	/*field*/public GtNode	RightNode;
 	BinaryNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Left, GtNode Right) {
 		super(Type, Token);
 		this.Method = Method;
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
 	/*field*/public GtMethod    Method;
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
 	/*field*/public GtMethod  Method;
 	GetterNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr) {
 		super(Type, Token);
 		this.Method = Method;
 		this.Expr = Expr;
 	}
 	@Override public void Evaluate(GtGenerator Visitor) {
 		Visitor.VisitGetterNode(this);
 	}
 	@Override public String toString() {
 		return "(Getter:" + this.Type + " " + GtNode.Stringify(this.Expr) + ", " + this.Method.MethodName + ")";
 	}
 }
 
 //E.g., $Expr "[" $Indexer "]"
 class IndexerNode extends GtNode {
 	/*field*/public GtMethod  Method;
 	/*field*/public GtNode Expr;
 	/*field*/public GtNode IndexAt;
 	IndexerNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr, GtNode IndexAt) {
 		super(Type, Token);
 		this.Method = Method;
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
 	/*field*/public GtMethod  Method;
 	/*field*/public GtNode Expr;
 	/*field*/public GtNode Index1;
 	/*field*/public GtNode Index2;
 	SliceNode/*constructor*/(GtType Type, GtToken Token, GtMethod Method, GtNode Expr, GtNode Index1, GtNode Index2) {
 		super(Type, Token);
 		this.Method = Method;
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
 	/*field*/public GtMethod	Method;
 	/*field*/public ArrayList<GtNode>  Params; /* [arg1, arg2, ...] */
 	ApplyNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method) {
 		super(Type, KeyToken);
 		this.Method = Method;
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
 
 //E.g., $Recv.Method "(" $Param[0], $Param[1], ... ")"
 class MessageNode extends GtNode {
 	/*field*/public GtMethod	Method;
 	/*field*/public GtNode   RecvNode;
 	/*field*/public ArrayList<GtNode>  Params;
 	MessageNode/*constructor*/(GtType Type, GtToken KeyToken, GtMethod Method, GtNode RecvNode) {
 		super(Type, KeyToken);
 		this.Method = Method;
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
 	NewNode/*constructor*/(GtType Type, GtToken Token) {
 		super(Type, Token);
 		this.Params = new ArrayList<GtNode>();
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
 	/*field*/GtType					SuperClass;
 	/*field*/public GtType			SearchSuperMethodClass;
 	/*field*/public Object			DefaultNullValue;
 	/*field*/public GtMap           ClassSymbolTable;
 	/*field*/GtType					BaseClass;
 	/*field*/GtType[]				Types;
 	/*field*/public Object          NativeSpec;
 
 	GtType/*constructor*/(GtClassContext Context, int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		this.Context = Context;
 		this.ClassFlag = ClassFlag;
 		this.ShortClassName = ClassName;
 		this.SuperClass = null;
 		this.BaseClass = this;
 		this.SearchSuperMethodClass = null;
 		this.DefaultNullValue = DefaultNullValue;
 		this.NativeSpec = NativeSpec;
 		this.ClassSymbolTable = IsFlag(ClassFlag, EnumClass) ? (/*cast*/GtMap)NativeSpec : null;
 		this.ClassId = Context.ClassCount;
 		Context.ClassCount += 1;
 		this.Types = null;
 	}
 
 	public GtType CreateSubType(int ClassFlag, String ClassName, Object DefaultNullValue, Object NativeSpec) {
 		GtType SubType = new GtType(this.Context, ClassFlag, ClassName, DefaultNullValue, NativeSpec);
 		SubType.SuperClass = this;
 		SubType.SearchSuperMethodClass = this;
 		return SubType;
 	}
 	
 	// Note Don't call this directly. Use Context.GetGenericType instead.
 	public GtType CreateGenericType(int BaseIndex, ArrayList<GtType> TypeList, String ShortName) {
 		GtType GenericType = new GtType(this.Context, this.ClassFlag, ShortName, null, null);
 		GenericType.BaseClass = this.BaseClass;
 		GenericType.SearchSuperMethodClass = this.BaseClass;
 		GenericType.SuperClass = this.SuperClass;
 		this.Types = LangDeps.CompactTypeList(BaseIndex, TypeList);
 		DebugP("new class: " + GenericType.ShortClassName + ", ClassId=" + GenericType.ClassId);
 		return GenericType;
 	}
 
 	public final boolean IsNative() {
 		return IsFlag(this.ClassFlag, NativeClass);
 	}
 
 	public final boolean IsDynamic() {
 		return IsFlag(this.ClassFlag, DynamicClass);
 	}
 
 	public final boolean IsGenericType() {
 		return (this.Types != null);
 	}
 
 	@Override public String toString() {
 		return this.ShortClassName;
 	}
 
 	public final Object GetClassSymbol(String Key, boolean RecursiveSearch) {
 		GtType Type = this;
 		while(Type != null) {
 			if(Type.ClassSymbolTable != null) {
 				return Type.ClassSymbolTable.get(Key);
 			}
 			Type = (RecursiveSearch) ? Type.SuperClass : null;
 		}
 		return null;
 	}
 
 	public final void SetClassSymbol(String Key, Object Value) {
 		if(this.ClassSymbolTable == null) {
 			this.ClassSymbolTable = new GtMap();
 		}
 		this.ClassSymbolTable.put(Key, Value);
 	}
 
 	
 	public final String GetSignature() {
 		return GtStatic.NumberToAscii(this.ClassId);
 	}
 
 	public final boolean Accept(GtType Type) {
 		if(this == Type || this == this.Context.AnyType) {
 			return true;
 		}
 		GtType SuperClass = this.SuperClass;
 		while(SuperClass != null) {
 			if(SuperClass == Type) {
 				return true;
 			}
 			SuperClass = SuperClass.SuperClass;
 		}
 		return this.Context.CheckSubType(Type, this);
 	}
 
 }
 
 class GtMethod extends GtStatic {
 	/*field*/public int				MethodFlag;
 //	/*field*/int					MethodSymbolId;
 	/*field*/public String			MethodName;
 	/*field*/public String          MangledName;
 	/*field*/public GtType[]		Types;
 	/*field*/private GtType         FuncType;
 	/*field*/public GtMethod        ListedMethods;
 	/*field*/public String          SourceMacro;
 	/*field*/public Object          NativeRef;
 
 	GtMethod/*constructor*/(int MethodFlag, String MethodName, int BaseIndex, ArrayList<GtType> ParamList, Object NativeRef) {
 		this.MethodFlag = MethodFlag;
 		this.MethodName = MethodName;
 //		this.MethodSymbolId = GtStatic.GetSymbolId(MethodName, CreateNewSymbolId);
 		this.Types = LangDeps.CompactTypeList(BaseIndex, ParamList);
 		LangDeps.Assert(this.Types.length > 0);
 		this.ListedMethods = null;
 		this.FuncType = null;
		this.NativeRef = NativeRef;
 		this.MangledName = GtStatic.MangleMethodName(this.GetRecvType(), this.MethodName, BaseIndex+2, ParamList);
 	}
 
 	public final String GetNativeFuncName() {
 		if(this.Is(ExportMethod)) {
 			return this.MethodName;
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
 		/*local*/String s = this.MethodName + "(";
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
 
 	public final int GetFuncParamSize() {
 		return this.Types.length - 1;
 	}
 
 	public final GtType GetFuncParamType(int ParamIdx) {
 		return this.Types[ParamIdx+1];
 	}
 
 	public final int GetMethodParamSize() {
 		return this.Types.length - 2;
 	}
 
 	public final String GetNativeMacro() {
 		return (/*cast*/String)this.NativeRef;
 	}
 
 	public final String ExpandMacro1(String Arg0) {
 		String NativeMacro = IsFlag(this.MethodFlag, NativeMacroMethod) ? (/*cast*/String)this.NativeRef : this.MethodName + " $1";
 		return NativeMacro.replaceAll("$0", Arg0);
 	}
 
 	public final String ExpandMacro2(String Arg0, String Arg1) {
 		String NativeMacro = IsFlag(this.MethodFlag, NativeMacroMethod) ? (/*cast*/String)this.NativeRef : "$1 " + this.MethodName + " $2";
 		return NativeMacro.replaceAll("$0", Arg0);
 	}
 }
 
 class GtPolyFunc {
 	/*field*/public ArrayList<GtMethod> FuncList;
 	GtPolyFunc/*constructor*/(GtMethod Func1, GtMethod Func2) {
 		this.FuncList = new ArrayList<GtMethod>();
 		this.FuncList.add(Func1);
 		this.FuncList.add(Func2);
 	}
 }
 
 
 class GtGenerator extends GtStatic {
 	/*field*/public String     LangName;
 	/*field*/public GtClassContext  Context;
 	/*field*/public ArrayList<Object> GeneratedCodeStack;
 
 	GtGenerator/*constructor*/(String LangName) {
 		this.LangName = LangName;
 		this.Context = null;
 		this.GeneratedCodeStack = new ArrayList<Object>();
 	}
 
 	public void SetLanguageContext(GtClassContext Context) {
 		this.Context = Context;
 	}
 
 	public final GtNode UnsupportedNode(GtType Type, GtSyntaxTree ParsedTree) {
 		/*local*/GtToken Token = ParsedTree.KeyToken;
 		ParsedTree.NameSpace.Context.ReportError(ErrorLevel, Token, this.LangName + " has no language support for " + Token.ParsedText);
 		return new ErrorNode(ParsedTree.NameSpace.Context.VoidType, ParsedTree.KeyToken);
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
 
 	public GtNode CreateGetterNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method, GtNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public GtNode CreateIndexerNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method, GtNode Expr, GtNode Index) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Method, Expr, Index);
 	}
 
 	public GtNode CreateApplyNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method) {
 		return new ApplyNode(Type, ParsedTree.KeyToken, Method);
 	}
 
 	public GtNode CreateMessageNode(GtType Type, GtSyntaxTree ParsedTree, GtNode RecvNode, GtMethod Method) {
 		return new MessageNode(Type, ParsedTree.KeyToken, Method, RecvNode);
 	}
 
 	public GtNode CreateNewNode(GtType Type, GtSyntaxTree ParsedTree) {
 		return new NewNode(Type, ParsedTree.KeyToken);
 	}
 
 	public GtNode CreateUnaryNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method, GtNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public GtNode CreateSuffixNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method, GtNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Method, Expr);
 	}
 
 	public GtNode CreateBinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtMethod Method, GtNode Left, GtNode Right) {
 		return new BinaryNode(Type, ParsedTree.KeyToken, Method, Left, Right);
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
 
 	public boolean TransformNativeMethods(GtType NativeBaseType, String MethodName) {
 		boolean TransformedResult = false;
 //ifdef JAVA
 		Class<?> NativeClassInfo = (Class<?>)NativeBaseType.NativeSpec;
 		Method[] List = NativeClassInfo.getMethods();
 		if(List != null) {
 			for(int i = 0; i < List.length; i++) {
 				if(MethodName.equals(List[i].getName())) {
 					int MethodFlag = NativeMethod;
 					if(Modifier.isStatic(List[i].getModifiers())) {
 						MethodFlag |= NativeStaticMethod;
 					}
 					ArrayList<GtType> TypeList = new ArrayList<GtType>();
 					TypeList.add(this.GetNativeType(List[i].getReturnType()));
 					TypeList.add(NativeBaseType);
 					Class<?>[] ParamTypes = List[i].getParameterTypes();
 					if(ParamTypes != null) {
 						for(int j = 0; j < List.length; j++) {
 							TypeList.add(this.GetNativeType(ParamTypes[j]));
 						}
 					}
 					GtMethod NativeMethod = new GtMethod(MethodFlag, MethodName, 0, TypeList, List[i]);
 					this.Context.DefineMethod(NativeMethod);
 					TransformedResult = false;
 				}
 			}
 		}
 //endif VAJA
 		return TransformedResult;
 	}
 
 	public int ParseClassFlag(int ClassFlag, GtSyntaxTree ClassDeclTree) {
 //		if(ClassDeclTree.HasAnnotation("Final")) {
 //			ClassFlag = ClassFlag | FinalClass;
 //		}
 //		if(ClassDeclTree.HasAnnotation("Private")) {
 //			ClassFlag = ClassFlag | PrivateClass;
 //		}
 		return ClassFlag;
 	}
 
 	public void AddClass(GtType Type) {
 		/*extension*/
 	}
 
 	public void GenerateClassField(GtNameSpace NameSpace, GtType Type, ArrayList<GtVariableInfo> FieldList) {
 		/*extension*/
 	}
 
 	public int ParseMethodFlag(int MethodFlag, GtSyntaxTree MethodDeclTree) {
 		if(MethodDeclTree.HasAnnotation("Export")) {
 			MethodFlag = MethodFlag | ExportMethod;
 		}
 		return MethodFlag;
 	}
 
 	public GtMethod CreateMethod(int MethodFlag, String MethodName, int BaseIndex, ArrayList<GtType> TypeList, Object NativeRef) {
 		return new GtMethod(MethodFlag, MethodName, BaseIndex, TypeList, NativeRef);
 	}
 
 	public void GenerateMethod(GtMethod Method, ArrayList<String> ParamNameList, GtNode Body) {
 		/*extenstion*/
 
 	}
 
 	//------------------------------------------------------------------------
 
 	public void VisitEmptyNode(GtNode EmptyNode) {
 		GtStatic.DebugP("empty node: " + EmptyNode.Token.ParsedText);
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
 		return true; /* override this */
 	}
 
 	public Object Eval(GtNode Node) {
 		this.VisitBlock(Node);
 		return null;
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
 }
 
 class SourceGenerator extends GtGenerator {
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
