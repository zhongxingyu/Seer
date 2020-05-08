 package back_end;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import util.ast.AbstractSyntaxTree;
 import util.ast.node.*;
 import util.ast.node.PostfixExpressionNode.PostfixType;
 import util.error.FunctionNotDefinedError;
 import util.symbol_table.FunctionSymbol;
 import util.symbol_table.Symbol;
 import util.symbol_table.SymbolTable;
 import util.symbol_table.VariableSymbol;
 import util.type.Types;
 import util.type.VariableRedefinedException;
 import util.type.VariableUndeclaredException;
 
 
 /**
  * 
  * @author ben, jason
  *
  */
 
 public class SymbolTableVisitor implements Visitor {
 	
 	protected AbstractSyntaxTree tree;
 	
 	public SymbolTableVisitor(AbstractSyntaxTree tree) {
 		this.tree = tree;
 	}
 	
 	protected final static Logger LOGGER = Logger
 			.getLogger(SymbolTableVisitor.class.getName());
 	
 	public void walk() {
 		
 		ProgramNode treeRoot = (ProgramNode) this.tree.getRoot();
 		treeRoot.accept(this);
 	}
 	
 	private void openScope(Node node) {
 		
 //		LOGGER.warning("Calling openScope for " + node.getName());
 		// push new scope
 		if(node.isNewScope()) {
 	//		LOGGER.warning("Scope opening for " + node.getName());
 			SymbolTable.push();
 			
 			// map this as representative node
 			try {
 				SymbolTable.mapNode(node);
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 		}
 
 	}
 	
 	private void closeScope(Node node) {
 
 		
 		// pop if this was a new scope
 		if(node.isNewScope()) {
 			SymbolTable.pop();
 		}
 	
 	}
 	
 	private void visitAllChildrenStandard(Node node) {
 		// visit all children
 		List<Node> children = node.getChildren();
 		for (Node n : children) {
 			n.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(ArgumentsNode node) {
 		LOGGER.finer("visit(ArgumentsNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(BiOpNode node) {
 		LOGGER.finer("visit(BiOpNode node) called on " + node.getName());
 		openScope(node);
 		LOGGER.finer("The children of this BiNoNode are: " + node.getChildrenString());
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(CatchesNode node) {
 		LOGGER.finer("visit(CatchesNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ConstantNode node) {
 		LOGGER.finer("visit(Constant node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(DerivedTypeNode node) {
 		LOGGER.finer("visit(DerivedType node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(DictTypeNode node) {
 		LOGGER.finer("visit(DictTypeNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(ElseIfStatementNode node) {
 		LOGGER.finer("visit(ElseIfStatementNodt node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ElseStatementNode node) {
 		LOGGER.finer("visit(ElseStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(ExceptionTypeNode node) {
 		LOGGER.finer("visit(ExceptionTypeNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ExpressionNode node) {
 		LOGGER.finer("visit(ExpressionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(FunctionNode node) {
 		LOGGER.finer("visit(FunctionNode node) called on " + node.getName());
 		
 		// add function to symbol table - these need to be visible to entire program
 		FunctionSymbol funSym = new FunctionSymbol(node.getType(), node.getParametersNode());
 		SymbolTable.putToRootSymbolTable(node.getIdentifier(), funSym);
 		
 		// open new scope - these are specific to within the function
 		openScope(node);
 		
 		// add variables and types in the params list to the symbol table
 		ParametersNode currParamNode = node.getParametersNode();
 		if( currParamNode != null) {
 			TypeNode paramType = currParamNode.getType();
 			String paramName = currParamNode.getIdentifier();
 			try {
 				SymbolTable.put(paramName, new VariableSymbol(paramType));
 			} catch (VariableRedefinedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			// recurse through children, adding variables to the symbol table
 			while(currParamNode.hasChildren()) {
 				currParamNode = (ParametersNode) currParamNode.getChildren().get(0);
 				paramType = currParamNode.getType();
 				paramName = currParamNode.getIdentifier();
 				try {
 					SymbolTable.put(paramName, new VariableSymbol(paramType));
 				} catch (VariableRedefinedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 		
 		visitAllChildrenStandard(node);
 		
 		// close scope
 		closeScope(node);
 
 	}
 
 	@Override
 	public void visit(GuardingStatementNode node) {
 		LOGGER.finer("visit(GuardingStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(IdNode node) {
 		LOGGER.finer("visit(IdNode node) called on " + node.getName());
 		openScope(node);
 		// if it has a type, it is a declaration. Put it in the symbol table
 		if(node.getType() != null) {
 			
 			LOGGER.finer("IdNode has a non null type which is: " + node.getTypeName());
 			
 			try {
 				SymbolTable.put(node.getIdentifier(), new VariableSymbol(node.getType()));
 			} catch (VariableRedefinedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				System.exit(1);
 			}
 		} 
 		
 		//else, it does not have a type, so we ensure it is already declared
 		else {
 			Symbol nodeSymbol = SymbolTable.getSymbolForIdNode(node);
 			// if there is no nodeSymbol, this is being used before delcartion so throw an error
 			if( nodeSymbol == null ) {
 				LOGGER.finer("IdNode was used before it was declared. Throw an error.");
 				throw new VariableUndeclaredException("Use of " + node.getIdentifier() + " undefined.");
 			}
 			
 			LOGGER.finer("IdNode has a null type. Symbol is  " + nodeSymbol.toString());
 			// it has been declared. Now we decorate it with its type
 			node.setType(nodeSymbol.getType());
 			LOGGER.finer("We have set the IdNode type to" + nodeSymbol.getType().getName());
 		}
 		
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(IfElseStatementNode node) {
 		LOGGER.finer("visit(IfElseStatement node) called on " + node.getName());
 
 		// first visit the condition
 		if(node.getCondition() != null)
 			node.getCondition().accept(this);
		
		// open scope
		openScope(node);
 		
 		if(node.getIfCondTrue() != null) {
 			LOGGER.finer("we are in the getIfCondTrue");
 			node.getIfCondTrue().accept(this);
 		}
 		
 		// then close scope, then visit any remaining children
 		closeScope(node);
 		
 		if(node.getCheckNext() != null) {
 			
 			node.getCheckNext().accept(this);
 		}
 		
 		if(node.getIfCondFalse() != null) {
 			node.getIfCondFalse().accept(this);
 		}
 	}
 
 	@Override
 	public void visit(IterationStatementNode node) {
 		LOGGER.finer("visit(IterationStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(JumpStatementNode node) {
 		LOGGER.finer("visit(JumpStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(MockExpressionNode node) {
 		LOGGER.finer("visit(MockExpressionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(MockNode node) {
 		LOGGER.finer("visit(MockNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(Node node) {
 		LOGGER.finer("visit(Node node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ParametersNode node) {
 		LOGGER.finer("visit(ParametersNode node) called on " + node.getName());
 		openScope(node);
 		// we have already recursed through each paramater when visiting the function node
 		//visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(PostfixExpressionNode node) {
 		LOGGER.finer("visit(PostfixExpressionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(PrimaryExpressionNode node) {
 		LOGGER.finer("visit(PrimaryExpressionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(PrimitiveTypeNode node) {
 		LOGGER.finer("visit(PrimitiveTypeNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ProgramNode node) {
 		LOGGER.finer("visit(ProgramNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(RelationalExpressionNode node) {
 		LOGGER.finer("visit(RelationalExpressionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(ReservedWordTypeNode node) {
 		LOGGER.finer("visit(ReservedWordTypeNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(SectionNode node) {
 		LOGGER.finer("visit(SectionNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		//if(node.getSectionName() != SectionNode.SectionName.FUNCTIONS){
 			closeScope(node);
 		//}
 	}
 
 	@Override
 	public void visit(SectionTypeNode node) {
 		LOGGER.finer("visit(SectionTypeNode node) called on " + node.getName());
 		
 		openScope(node);
 		
 		// add emit() with the parameters as specified by the SectionTypeNode
 		List<TypeNode> emitParams = new ArrayList<TypeNode>();
 		emitParams.add(node.getReturnKey());
 		emitParams.add(node.getReturnValue());
 		FunctionSymbol funSym = new FunctionSymbol(new PrimitiveTypeNode(Types.Primitive.VOID), emitParams);
 		
 		// add input key and value
 		VariableSymbol inputKeySym = new VariableSymbol(node.getInputKeyIdNode().getType());
 		String inputKeyStr = node.getInputKeyIdNode().getIdentifier();
 		
 		VariableSymbol inputValueSym = new VariableSymbol(node.getInputValueIdNode().getType());
 		String inputValStr = node.getInputValueIdNode().getIdentifier();
 		
 		
 		try {
 			SymbolTable.put(inputKeyStr, inputKeySym);
 			SymbolTable.put(inputValStr, inputValueSym);
 			SymbolTable.put("emit", funSym);
 		} catch (VariableRedefinedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.exit(1);
 		}
 		
 
 		
 		
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(SelectionStatementNode node) {
 		LOGGER.finer("visit(SeletionStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(StatementListNode node) {
 		LOGGER.finer("visit(StatementListNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(StatementNode node) {
 		LOGGER.finer("visit(StatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(SwitchStatementNode node) {
 		LOGGER.finer("visit(SwitchStatementNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(TypeNode node) {
 		LOGGER.finer("visit(TypeNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 
 	@Override
 	public void visit(UnOpNode node) {
 		LOGGER.finer("visit(UnOpNode node) called on " + node.getName());
 		openScope(node);
 		visitAllChildrenStandard(node);
 		closeScope(node);
 	}
 }
