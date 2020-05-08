 package cps450;
 
 import java.io.File;
 import java.util.Stack;
 import java.io.PrintWriter;
 
 import cps450.oodle.analysis.*;
 import cps450.oodle.node.*;
 
 public class CodeGenerator extends DepthFirstAdapter {
 	
 	PrintWriter writer;
 
 	int ifStatementCount = 0;
 	Stack<Integer> ifStatementCounts;
 	int loopStatementCount;
 	Stack<Integer> loopStatementCounts;
 	
 	public CodeGenerator(PrintWriter _writer) {
 		super();
 		writer = _writer;
 		ifStatementCounts = new Stack<Integer>();
 		loopStatementCounts = new Stack<Integer>();
 	}
 	
 	private void emit(String sourceLine) {
 		System.out.println(sourceLine);
 		writer.println(sourceLine);
 	}
 	
 	private void emitOodleStatement(Token token) {
 		emit("");
 		emit("# " + SourceHolder.instance().getLine(token.getLine()-1));
 	}
 	
 	@Override
 	public void inAClassDef(AClassDef node) {
 		emit(".data");
 		emit(".comm _out, 4, 4");
 		emit(".comm _in, 4, 4");
 	}
 
 	@Override
 	public void outAAddExpression(AAddExpression node) {
 		emit("popl %eax # AddExpression");
 		emit("popl %ebx");
 		
 		if (node.getOperator() instanceof APlusOperator) {
 			emit("addl %eax, %ebx");
 		} else if (node.getOperator() instanceof AMinusOperator) {
 			emit("subl %eax, %ebx");
 		}
 		
 		emit("pushl %ebx # Store AddExpression result");
 	}
 
 	@Override
 	public void outAAndExpression(AAndExpression node) {
 		emit("popl %eax # AndExpression");
 		emit("popl %ebx");
 		emit("andl %ebx, %eax");
 		emit("pushl %eax # Store AndExpression result");
 	}
 	
 	@Override
 	public void inAAssignmentStatement(AAssignmentStatement node) {
 		emitOodleStatement(node.getId());
 	}
 
 	@Override
 	public void outAAssignmentStatement(AAssignmentStatement node) {
 		emit("popl %eax # AssignmentStatement");
 		emit("movl %eax, _" + node.getId().getText());
 	}
 
 	@Override
 	public void outACallExpression(ACallExpression node) {
 		emit("call " + node.getMethod().getText());
 		emit("addl $" + (node.getArguments().size() * 4) + ", %esp # Clean up the argument values");
 		if (node.getObject() != null) {
 			emit("popl %ebx # Clean up the Object Expression");
 		}
 		emit("pushl %eax # Assume that we got a return value");
 	}
 	
 	@Override
 	public void inACallStatement(ACallStatement node) {
 		ACallExpression expr = (ACallExpression)node.getExpression();
 		emitOodleStatement(expr.getMethod());
 	}
 
 	@Override
 	public void outACallStatement(ACallStatement node) {
 		emit("popl %eax # Cleanup unused return value in CallStatement");
 	}
 
 	@Override
 	public void outAComparisonExpression(AComparisonExpression node) {
 		emit("popl %eax # ComparisonExpression");
 		emit("popl %ebx");
 		emit("cmpl %ebx, %eax");
 		
 		if (node.getOperator() instanceof AEqualOperator) {
 			emit("sete %al # Equal");
 		} else if (node.getOperator() instanceof AGreaterOperator) {
 			emit("setg %al # Greater");
 		} else if (node.getOperator() instanceof AGreaterEqualOperator) {
 			emit("setge %al # GreaterOrEqual");
 		}
 		emit("movzbl %al, %eax");
 		
 		emit("pushl %eax # Store ComparisonExpression result");
 	}
 
 	@Override
 	public void outAFalseExpression(AFalseExpression node) {
 		emit("pushl $0 # FalseExpression");
 	}
 
 	@Override
 	public void outAIdentifierExpression(AIdentifierExpression node) {
 		emit("movl _" + node.getId().getText() + ", %eax");
 		emit("pushl %eax");
 	}
 
 	@Override
 	public void inAIfStatement(AIfStatement node) {
 		emitOodleStatement(node.getIf());
 	}
 
 	@Override
 	public void outAIfHelper(AIfHelper node) {
 		this.ifStatementCounts.push(this.ifStatementCount);
 		this.ifStatementCount++;
 		emit("popl %eax # Get comparison value for IfStatement");
		emit("cmpl 0, %eax");
 		emit("jne _true_statements_" + this.ifStatementCounts.peek());
 		emit("jmp _false_statements_" + this.ifStatementCounts.peek());
 		emit("_true_statements_" + this.ifStatementCounts.peek() + ":");
 	}
 
 	@Override
 	public void outAElseHelper(AElseHelper node) {
 		emit("jmp _end_if_statement_" + this.ifStatementCounts.peek());
 		emit("_false_statements_" + this.ifStatementCounts.peek() + ":");
 	}
 
 	@Override
 	public void outAIfStatement(AIfStatement node) {
 		emit("_end_if_statement_" + this.ifStatementCounts.peek() + ":");
 		
 		this.ifStatementCounts.pop();
 	}
 
 	@Override
 	public void outAIntegerExpression(AIntegerExpression node) {
 		emit("pushl $" + node.getIntlit().getText());
 	}
 
 	@Override
 	public void inALoopStatement(ALoopStatement node) {
 		emitOodleStatement(node.getLoop());
 		
 		this.loopStatementCounts.push(this.loopStatementCount);
 		this.loopStatementCount++;
 		
 		emit("_begin_loop_statement_" + this.loopStatementCounts.peek() + ":");
 	}
 
 	@Override
 	public void outALoopHelper(ALoopHelper node) {
 		emit("popl %eax # Get comparison value for LoopStatement");
		emit("cmpl 0, %eax");
 		emit("jne _loop_statements_" + this.loopStatementCounts.peek());
 		emit("jmp _end_loop_statement_" + this.loopStatementCounts.peek());
 		emit("_loop_statements_" + this.loopStatementCounts.peek() + ":");
 	}
 
 	@Override
 	public void outALoopStatement(ALoopStatement node) {
 		emit("jmp _begin_loop_statement_" + this.loopStatementCounts.peek());
 		emit("_end_loop_statement_" + this.loopStatementCounts.peek() + ":");
 		
 		this.loopStatementCounts.pop();
 	}
 
 	@Override
 	public void inAMethodDeclaration(AMethodDeclaration node) {
 		emit(".text");
 		if (node.getBeginName().getText().equals("start")) {
 			emit(".global main");
 			emit("main:");
 		} else {
 			emit("_" + node.getBeginName().getText() + ":");
 		}
 	}
 
 	@Override
 	public void outAMethodDeclaration(AMethodDeclaration node) {
 		if (node.getBeginName().getText().equals("start")) {
 			emit("push $0");
 			emit("call exit");
 		}
 	}
 
 	@Override
 	public void outAMultExpression(AMultExpression node) {
 		emit("popl %ebx # MultExpression");
 		emit("popl %eax");
 		
 		if (node.getOperator() instanceof AMultOperator) {
 			emit("imull %ebx, %eax");
 		} else if (node.getOperator() instanceof ADivOperator) {
 			emit("cdq");
 			emit("idivl %ebx");
 		}
 		
 		emit("pushl %eax # Store MultExpression result");
 	}
 
 	@Override
 	public void outANullExpression(ANullExpression node) {
 		emit("pushl $0");
 	}
 
 	@Override
 	public void outAOrExpression(AOrExpression node) {
 		emit("popl %eax # AndExpression");
 		emit("popl %ebx");
 		emit("orl %ebx, %eax");
 		emit("pushl %eax # Store AndExpression result");
 	}
 
 	@Override
 	public void outATrueExpression(ATrueExpression node) {
 		emit("pushl $1 # TrueExpression");
 	}
 
 	@Override
 	public void outAUnaryExpression(AUnaryExpression node) {
 		emit("popl %eax # Begin UnaryExpression");
 		if (node.getOperator() instanceof AMinusOperator) {
 			emit("negl %eax");
 		} else if (node.getOperator() instanceof ANotOperator) {
 			emit("xorl $1, %eax # Not");
 		}
 		emit("pushl %eax # End UnaryExpression");
 		
 	}
 
 	@Override
 	public void outAVarDeclaration(AVarDeclaration node) {
 		emit(".comm _" + node.getName().getText() + ", 4, 4");
 	}
 	
 	
 	
 }
