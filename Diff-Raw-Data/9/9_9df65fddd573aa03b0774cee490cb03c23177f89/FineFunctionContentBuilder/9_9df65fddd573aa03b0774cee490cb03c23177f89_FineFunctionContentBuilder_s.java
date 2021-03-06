 package main.codeitems.functionContent.builders;
 
 import java.util.Iterator;
 
 import antlr.FineFunctionGrammarParser.Assign_exprContext;
 import antlr.FineFunctionGrammarParser.Block_starterContext;
 import antlr.FineFunctionGrammarParser.Closing_curlyContext;
 import antlr.FineFunctionGrammarParser.DeclByTypeContext;
 import antlr.FineFunctionGrammarParser.Else_statementContext;
 import antlr.FineFunctionGrammarParser.If_statementContext;
 import antlr.FineFunctionGrammarParser.Opening_curlyContext;
 import antlr.FineFunctionGrammarParser.StatementContext;
 import antlr.FineFunctionGrammarParser.StatementsContext;
 
 import main.codeitems.CodeItem;
 import main.codeitems.expressions.AssignmentExpr;
 import main.codeitems.functionContent.BlockStarterItem;
 import main.codeitems.functionContent.CloseBlockItem;
 import main.codeitems.functionContent.CompoundItem;
 import main.codeitems.functionContent.ElseItem;
 import main.codeitems.functionContent.ExprStatementItem;
 import main.codeitems.functionContent.IfItem;
 import main.codeitems.functionContent.StatementItem;
 
 public class FineFunctionContentBuilder extends FunctionContentBuilder
 {
 	
 	public void exitStatements(StatementsContext ctx)
 	{
 		if(itemStack.size() != 1)
 			throw new RuntimeException("Broken stack while parsing");
 	
 		consolidateIfElse(rootItem);
 	}
 	
 	public void enterStatement(StatementContext ctx)
 	{
 		StatementItem statementItem = new StatementItem();
 		statementItem.initializeFromContext(ctx);
 		itemStack.push(statementItem);
 	}
 	
 	public void enterBlockStarter(Block_starterContext ctx)
 	{
 		replaceTopOfStack(new BlockStarterItem());
 	}
 
 	public void enterAssignment(Assign_exprContext ctx)
 	{
 		ExprStatementItem exprStmt = new ExprStatementItem();
 		exprStmt.expr = new AssignmentExpr();
 		exprStmt.expr.initializeFromContext(ctx);
 		replaceTopOfStack(exprStmt);
 	}
 	
 	public void enterIf(If_statementContext ctx)
 	{
 		replaceTopOfStack(new IfItem());
 	}
 	
 	public void enterElse(Else_statementContext ctx)
 	{
 		replaceTopOfStack(new ElseItem());
 	}
 
 	public void enterOpeningCurly(Opening_curlyContext ctx)
 	{
 		replaceTopOfStack(new CompoundItem());
 	}
 	
 	public void enterClosingCurly(Closing_curlyContext ctx)
 	{
 		replaceTopOfStack(new CloseBlockItem());
 	}
 	
 	public void exitStatement(StatementContext ctx)
 	{
 		if(itemStack.size() == 0)
 			throw new RuntimeException();
 	
 		CodeItem itemToRemove = itemStack.peek();
 		
 		if(itemToRemove instanceof CloseBlockItem){
 			closeCompoundStatement();
 			return;
 		}
 		
 		// We keep Block-starters and compound items
 		// on the stack. They are removed by following
 		// statements.
 		if(itemToRemove instanceof BlockStarterItem ||
 		   itemToRemove instanceof CompoundItem)
 			return;
 		
 		consolidate();	
 	}
 
 	private void closeCompoundStatement()
 	{
 		itemStack.pop(); // remove 'CloseBlock'
 		
 		CompoundItem compoundItem = (CompoundItem) itemStack.pop();
 		consolidateIfElse(compoundItem);
 		consolidateBlockStarters(compoundItem);		
 	}
 	
 	private void consolidateIfElse(CompoundItem compoundItem)
 	{
 		Iterator<StatementItem> it = compoundItem.statements.iterator();
 		StatementItem prev = null;
 		while(it.hasNext()){
 			
 			StatementItem cur = it.next();
 			if(prev != null && cur instanceof ElseItem){
 				if(prev instanceof IfItem){
 					IfItem ifItem = (IfItem) prev;
 					ifItem.elseItem = (ElseItem) cur;
 					it.remove();
 				}
 			}
 			prev = cur;
 		}
 	}
 
 	private void consolidateBlockStarters(StatementItem topOfStack)
 	{
 		while(true){
 			try{
 				BlockStarterItem bItem = (BlockStarterItem) itemStack.peek();
 				bItem = (BlockStarterItem) itemStack.pop();
 				bItem.setStatement(topOfStack);
 				topOfStack = bItem;
 			}catch(ClassCastException ex){
 				break;
 			}
 		}
 		CompoundItem root = (CompoundItem) itemStack.peek();
 		root.addStatement(topOfStack);
 	}
 	
 	private void consolidate()
 	{
 		
 		StatementItem stmt = (StatementItem) itemStack.pop();
 		StatementItem topOfStack = null;
 		
 		if(itemStack.size() > 0)
 			topOfStack = (StatementItem) itemStack.peek();
 		
 		if(topOfStack instanceof CompoundItem){
 			CompoundItem compound = (CompoundItem)topOfStack;
 			compound.addStatement(stmt);
 		}else{
 			consolidateBlockStarters(topOfStack);
 		}
 		
 	}
 	
 }
