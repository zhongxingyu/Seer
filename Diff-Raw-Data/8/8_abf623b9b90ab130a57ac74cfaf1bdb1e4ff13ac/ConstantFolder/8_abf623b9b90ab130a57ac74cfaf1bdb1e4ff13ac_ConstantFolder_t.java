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
 
 //ifdef  JAVA
 import java.util.ArrayList;
 //endif VAJA
 
 public class ConstantFolder extends GtGenerator {
 	private GtType BooleanType;
 
 	ConstantFolder/*constructor*/(String TargetCode, String OutputFile, int GeneratorFlag) {
 		super(TargetCode, OutputFile, GeneratorFlag);
 	}
 
 	@Override public void InitContext(GtClassContext Context) {
 		this.BooleanType = Context.BooleanType;
 	}
 
 	private void FoldList(ArrayList<GtNode> NodeList) {
 		/*local*/int i = 0;
 		while(i < NodeList.size()) {
 			/*local*/GtNode Param = NodeList.get(i);
 			NodeList.set(i, this.Fold(Param));
 			i = i + 1;
 		}
 	}
 
 	private GtNode Tail(GtNode Node) {
 		while(Node.NextNode != null) {
 			Node = Node.NextNode;
 		}
 		return Node;
 	}
 
 	private GtNode FoldBlock(GtNode Block) {
 		if(Block == null) {
 			return null;
 		}
 		/*local*/GtNode Head = this.FoldOnce(Block);
 		/*local*/GtNode Tmp  = this.Tail(Head);
 		Block = Block.NextNode;
 		while(Block != null) {
 			Tmp.NextNode = this.FoldOnce(Block);
 			Block = Block.NextNode;
 			Tmp = this.Tail(Tmp);
 		}
 		return Head;
 	}
 
 	public final GtNode Fold(GtNode SourceNode) {
 		return this.FoldBlock(SourceNode);
 	}
 
 	private GtNode EvalNode(GtNode Node) {
 		/*local*/Object Value = Node.ToConstValue();
 		if(Value != null) {
 			return this.CreateConstNode2(Node.Type, Node.Token, Value);
 		}
 		return Node;
 	}
 
 	private GtNode FoldCast(CastNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldUnary(UnaryNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return this.EvalNode(Node);
 	}
 
 	private GtNode FoldSuffix(SuffixNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldExists(ExistsNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldAssign(AssignNode Node) {
 		Node.RightNode = this.Fold(Node.RightNode);
 		return Node;
 	}
 	private GtNode FoldSelfAssign(SelfAssignNode Node) {
 		Node.RightNode = this.Fold(Node.RightNode);
 		return Node;
 	}
 	private GtNode FoldInstanceOf(InstanceOfNode Node) {
 		Node.ExprNode = this.Fold(Node.ExprNode);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldGetter(GetterNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldIndexer(IndexerNode Node) {
 		Node.IndexAt = this.Fold(Node.IndexAt);
 		return this.EvalNode(Node);
 	}
 	private GtNode FoldBinary(BinaryNode Node) {
 		Node.LeftNode = this.Fold(Node.LeftNode);
 		Node.RightNode = this.Fold(Node.RightNode);
 		return this.EvalNode(Node);
 	}
 
 	private GtNode FoldTrinary(TrinaryNode Node) {
 		Node.CondExpr = this.Fold(Node.CondExpr);
 		Node.ThenExpr = this.Fold(Node.ThenExpr);
 		Node.ElseExpr = this.Fold(Node.ElseExpr);
 		if(Node.CondExpr instanceof ConstNode && Node.CondExpr.Type.equals(this.BooleanType)) {
 			if(this.ConstValue(Node.CondExpr).equals(true)) {
 				return Node.ThenExpr;
 			}
 			else {
 				return Node.ElseExpr;
 			}
 		}
 		return Node;
 	}
 
 	private GtNode FoldOr(OrNode Node) {
 		/*local*/GtNode Left = Node.LeftNode;
 		/*local*/GtNode Right = Node.RightNode;
 		return this.FoldOrNode(Node, Left, Right);
 	}
 
 	private GtNode FoldOrNode(GtNode Original, GtNode Left, GtNode Right) {
 		Left = this.Fold(Left);
 		Right = this.Fold(Right);
 		if(Left.Type == this.BooleanType && Right.Type == this.BooleanType) {
 			if(Left instanceof ConstNode) {
 				if(this.ConstValue(Left).equals(false)) {
 					return Right;
 				}
 			}
 			if(Right instanceof ConstNode) {
 				if(this.ConstValue(Right).equals(false)) {
 					return Left;
 				}
 			}
 		}
 		return Original;
 	}
 
 	private GtNode FoldAnd(AndNode Node) {
 		/*local*/GtNode Left = Node.LeftNode;
 		/*local*/GtNode Right = Node.RightNode;
 		return this.FoldAndNode(Node, Left, Right);
 	}
 
 	private GtNode FoldAndNode(GtNode Original, GtNode Left, GtNode Right) {
 		Left = this.Fold(Left);
 		Right = this.Fold(Right);
 		if(Left.Type == this.BooleanType && Right.Type == this.BooleanType) {
 			if(Left instanceof ConstNode) {
 				if(this.ConstValue(Left).equals(false)) {
 					return Left;
 				}
 				if(Right instanceof ConstNode && this.ConstValue(Right).equals(true)) {
 					return Left;
 				}
 			}
 			if(Right instanceof ConstNode) {
 				if(this.ConstValue(Right).equals(false)) {
 					return Right;
 				}
 			}
 		}
 		return Original;
 	}
 
 	private GtNode FoldLet(LetNode Node) {
 		Node.InitNode = this.Fold(Node.InitNode);
 		return Node;
 	}
 	private GtNode FoldSlice(SliceNode Node) {
 		Node.Index1 = this.Fold(Node.Index1);
 		Node.Index2 = this.Fold(Node.Index2);
 		return Node;
 	}
 	private GtNode FoldApply(ApplyNode Node) {
		this.FoldList(Node.Params);
 		return Node;
 	}
 
 	private GtNode FoldMessage(MessageNode Node) {
 		Node.RecvNode = this.Fold(Node.RecvNode);
		this.FoldList(Node.Params);
 		return Node;
 	}
 
 	private GtNode FoldIf(IfNode Node) {
 		Node.CondExpr = this.Fold(Node.CondExpr);
 		if(Node.CondExpr.Type == this.BooleanType && Node.CondExpr instanceof ConstNode) {
 			if(this.ConstValue(Node.CondExpr).equals(true)) {
 				Node.ElseNode = this.CreateEmptyNode(Node.Type);
 			}
 			else {
 				if(this.IsEmptyBlock(Node.ElseNode)) {
 					return this.CreateEmptyNode(Node.Type);
 				}
 				Node.ThenNode = this.CreateEmptyNode(Node.Type);
 			}
 		}
 		Node.ThenNode = this.FoldBlock(Node.ThenNode);
 		Node.ElseNode = this.FoldBlock(Node.ElseNode);
 		return Node;
 	}
 	private GtNode FoldWhile(WhileNode Node) {
 		Node.CondExpr = this.Fold(Node.CondExpr);
 		Node.LoopBody = this.FoldBlock(Node.LoopBody);
 		if(Node.CondExpr instanceof ConstNode && this.ConstValue(Node.CondExpr).equals(false)) {
 			return this.CreateEmptyNode(Node.Type);
 		}
 		return Node;
 	}
 	private GtNode FoldDoWhile(DoWhileNode Node) {
 		Node.CondExpr = this.Fold(Node.CondExpr);
 		Node.LoopBody = this.FoldBlock(Node.LoopBody);
 		if(Node.CondExpr instanceof ConstNode && this.ConstValue(Node.CondExpr).equals(false)) {
 			return Node.LoopBody;
 		}
 		return Node;
 	}
 	private GtNode FoldFor(ForNode Node) {
 		Node.IterExpr = this.Fold(Node.IterExpr);
 		Node.CondExpr = this.Fold(Node.CondExpr);
 		Node.LoopBody = this.FoldBlock(Node.LoopBody);
 		return Node;
 	}
 	private GtNode FoldReturn(ReturnNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return Node;
 	}
 	private GtNode FoldThrow(ThrowNode Node) {
 		Node.Expr = this.Fold(Node.Expr);
 		return Node;
 	}
 	private GtNode FoldTry(TryNode Node) {
 		Node.TryBlock = this.FoldBlock(Node.TryBlock);
 		Node.CatchBlock = this.FoldBlock(Node.CatchBlock);
 		Node.FinallyBlock = this.FoldBlock(Node.FinallyBlock);
 		return Node;
 	}
 	private GtNode FoldCommand(CommandNode Node) {
		this.FoldList(Node.Params);
 		return Node;
 	}
 
 	private GtNode FoldOnce(GtNode SourceNode) {
 		if(SourceNode == null) {
 			return null;
 		}
 		else if(SourceNode instanceof EmptyNode) {
 		}
 		else if(SourceNode instanceof ConstNode) {
 		}
 		else if(SourceNode instanceof LocalNode) {
 		}
 		else if(SourceNode instanceof NullNode) {
 		}
 		else if(SourceNode instanceof CastNode) {
 			return this.FoldCast((/*cast*/CastNode) SourceNode);
 		}
 		else if(SourceNode instanceof UnaryNode) {
 			return this.FoldUnary((/*cast*/UnaryNode) SourceNode);
 		}
 		else if(SourceNode instanceof SuffixNode) {
 			return this.FoldSuffix((/*cast*/SuffixNode) SourceNode);
 		}
 		else if(SourceNode instanceof ExistsNode) {
 			return this.FoldExists((/*cast*/ExistsNode) SourceNode);
 		}
 		else if(SourceNode instanceof AssignNode) {
 			return this.FoldAssign((/*cast*/AssignNode) SourceNode);
 		}
 		else if(SourceNode instanceof SelfAssignNode) {
 			return this.FoldSelfAssign((/*cast*/SelfAssignNode) SourceNode);
 		}
 		else if(SourceNode instanceof InstanceOfNode) {
 			return this.FoldInstanceOf((/*cast*/InstanceOfNode) SourceNode);
 		}
 		else if(SourceNode instanceof BinaryNode) {
 			return this.FoldBinary((/*cast*/BinaryNode) SourceNode);
 		}
 		else if(SourceNode instanceof AndNode) {
 			return this.FoldAnd((/*cast*/AndNode) SourceNode);
 		}
 		else if(SourceNode instanceof OrNode) {
 			return this.FoldOr((/*cast*/OrNode) SourceNode);
 		}
 		else if(SourceNode instanceof TrinaryNode) {
 			return this.FoldTrinary((/*cast*/TrinaryNode) SourceNode);
 		}
 		else if(SourceNode instanceof GetterNode) {
 			return this.FoldGetter((/*cast*/GetterNode) SourceNode);
 		}
 		else if(SourceNode instanceof IndexerNode) {
 			return this.FoldIndexer((/*cast*/IndexerNode) SourceNode);
 		}
 		else if(SourceNode instanceof SliceNode) {
 			return this.FoldSlice((/*cast*/SliceNode) SourceNode);
 		}
 		else if(SourceNode instanceof LetNode) {
 			return this.FoldLet((/*cast*/LetNode) SourceNode);
 		}
 		else if(SourceNode instanceof ApplyNode) {
 			return this.FoldApply((/*cast*/ApplyNode) SourceNode);
 		}
 		else if(SourceNode instanceof MessageNode) {
 			return this.FoldMessage((/*cast*/MessageNode) SourceNode);
 		}
 		else if(SourceNode instanceof NewNode) {
 		}
 		else if(SourceNode instanceof IfNode) {
 			return this.FoldIf((/*cast*/IfNode) SourceNode);
 		}
 		else if(SourceNode instanceof WhileNode) {
 			return this.FoldWhile((/*cast*/WhileNode) SourceNode);
 		}
 		else if(SourceNode instanceof DoWhileNode) {
 			return this.FoldDoWhile((/*cast*/DoWhileNode) SourceNode);
 		}
 		else if(SourceNode instanceof ForNode) {
 			return this.FoldFor((/*cast*/ForNode) SourceNode);
 		}
 		else if(SourceNode instanceof ForEachNode) {
 		}
 		else if(SourceNode instanceof ContinueNode) {
 		}
 		else if(SourceNode instanceof BreakNode) {
 		}
 		else if(SourceNode instanceof ReturnNode) {
 			return this.FoldReturn((/*cast*/ReturnNode) SourceNode);
 		}
 		else if(SourceNode instanceof ThrowNode) {
 			return this.FoldThrow((/*cast*/ThrowNode) SourceNode);
 		}
 		else if(SourceNode instanceof TryNode) {
 			return this.FoldTry((/*cast*/TryNode) SourceNode);
 		}
 		else if(SourceNode instanceof SwitchNode) {
 		}
 		else if(SourceNode instanceof FunctionNode) {
 		}
 		else if(SourceNode instanceof ErrorNode) {
 		}
 		else if(SourceNode instanceof CommandNode) {
 			return this.FoldCommand((/*cast*/CommandNode) SourceNode);
 		}
 		return SourceNode;
 	}
 
 	private Object ConstValue(GtNode Node) {
 		if(Node instanceof ConstNode) {
 			return ((/*cast*/ConstNode) Node).ConstValue;
 		}
 		return null;
 	}
 
 	private GtNode CreateConstNode2(GtType Type, GtToken KeyToken, Object Value) {
 		return new ConstNode(Type, KeyToken, Value);
 	}
 
 	@Override public GtNode CreateGetterNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new GetterNode(Type, ParsedTree.KeyToken, Func, this.Fold(Expr));
 	}
 
 	@Override public GtNode CreateIndexerNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr, GtNode Index) {
 		return new IndexerNode(Type, ParsedTree.KeyToken, Func, this.Fold(Expr), this.Fold(Index));
 	}
 
 	@Override public GtNode CreateUnaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new UnaryNode(Type, ParsedTree.KeyToken, Func, this.Fold(Expr));
 	}
 
 	@Override public GtNode CreateSuffixNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Expr) {
 		return new SuffixNode(Type, ParsedTree.KeyToken, Func, this.Fold(Expr));
 	}
 
 	@Override public GtNode CreateBinaryNode(GtType Type, GtSyntaxTree ParsedTree, GtFunc Func, GtNode Left, GtNode Right) {
 		Left = this.Fold(Left);
 		Right = this.Fold(Right);
 		/*local*/Object LeftValue = Left.ToConstValue();
 		/*local*/Object RightValue = Right.ToConstValue();
 		if(Func != null) {
 			/*local*/String Operator = Func.GetNativeFuncName();
 			/*local*/Object ConstValue = LibGreenTea.EvalBinary(Type, LeftValue, Operator, RightValue);
 			if(ConstValue != null) {
 				return this.CreateConstNode(Type, ParsedTree, ConstValue);
 			}
 		}
 		return new BinaryNode(Type, ParsedTree.KeyToken, Func, Left, Right);
 	}
 
 	@Override public GtNode CreateAndNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		Left = this.Fold(Left);
 		Right = this.Fold(Right);
 		/*local*/GtNode Node = this.FoldAndNode(null, Left, Right);
 		if(Node != null) {
 			return Node;
 		}
 		return new AndNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	@Override public GtNode CreateOrNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		Left = this.Fold(Left);
 		Right = this.Fold(Right);
 		/*local*/GtNode Node = this.FoldOrNode(null, Left, Right);
 		if(Node != null) {
 			return Node;
 		}
 		return new OrNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	@Override public GtNode CreateAssignNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Left, GtNode Right) {
 		Right = this.Fold(Right);
 		return new AssignNode(Type, ParsedTree.KeyToken, Left, Right);
 	}
 
 	@Override public GtNode CreateLetNode(GtType Type, GtSyntaxTree ParsedTree, GtType DeclType, String VarName, GtNode InitNode, GtNode Block) {
 		InitNode = this.Fold(InitNode);
 		return new LetNode(Type, ParsedTree.KeyToken, DeclType, VarName, InitNode, Block);
 	}
 
 	@Override public GtNode CreateIfNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode ThenNode, GtNode ElseNode) {
 		CondNode = this.Fold(CondNode);
 		ThenNode = this.Fold(ThenNode);
 		ElseNode = this.Fold(ElseNode);
 		/*local*/GtType BooleanType = ParsedTree.NameSpace.Context.BooleanType;
 		if(CondNode.Type == BooleanType && CondNode instanceof ConstNode) {
 			if(this.ConstValue(CondNode).equals(true)) {
 				ElseNode = this.CreateEmptyNode(Type);
 			}
 			else {
 				if(this.IsEmptyBlock(ElseNode)) {
 					return this.CreateEmptyNode(Type);
 				}
 				ThenNode = this.CreateEmptyNode(Type);
 			}
 		}
 		return new IfNode(Type, ParsedTree.KeyToken, CondNode, ThenNode, ElseNode);
 	}
 
 	@Override public GtNode CreateWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode CondNode, GtNode Block) {
 		CondNode = this.Fold(CondNode);
 		/*local*/GtType BooleanType = ParsedTree.NameSpace.Context.BooleanType;
 		if(CondNode.Type == BooleanType && CondNode instanceof ConstNode) {
 			if(this.ConstValue(CondNode).equals(false)) {
 				return this.CreateEmptyNode(Type);
 			}
 		}
 		return new WhileNode(Type, ParsedTree.KeyToken, CondNode, Block);
 	}
 
 	@Override public GtNode CreateDoWhileNode(GtType Type, GtSyntaxTree ParsedTree, GtNode Cond, GtNode Block) {
 		Cond = this.Fold(Cond);
 		/*local*/GtType BooleanType = ParsedTree.NameSpace.Context.BooleanType;
 		if(Cond.Type == BooleanType && Cond instanceof ConstNode) {
 			if(this.ConstValue(Cond).equals(false)) {
 				return Block;
 			}
 		}
 		return new DoWhileNode(Type, ParsedTree.KeyToken, Cond, Block);
 	}
 }
