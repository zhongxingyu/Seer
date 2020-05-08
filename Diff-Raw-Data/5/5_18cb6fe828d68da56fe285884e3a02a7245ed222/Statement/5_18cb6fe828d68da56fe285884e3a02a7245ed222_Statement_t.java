 package org.cwi.waebric.parser.ast.statements;
 
 import org.cwi.waebric.WaebricKeyword;
 import org.cwi.waebric.WaebricSymbol;
 import org.cwi.waebric.parser.ast.CharacterLiteral;
 import org.cwi.waebric.parser.ast.ISyntaxNode;
 import org.cwi.waebric.parser.ast.StringLiteral;
 import org.cwi.waebric.parser.ast.SyntaxNodeList;
 import org.cwi.waebric.parser.ast.basic.StrCon;
 import org.cwi.waebric.parser.ast.embedding.Embedding;
 import org.cwi.waebric.parser.ast.expressions.Expression;
 import org.cwi.waebric.parser.ast.expressions.Var;
 import org.cwi.waebric.parser.ast.predicates.Predicate;
 
 public abstract class Statement implements ISyntaxNode {
 
 	/**
 	 * "if" "(" Predicate ")" Statement NoElseMayFollow -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class IfStatement extends Statement {
 		
 		protected Predicate predicate;
 		protected Statement statement;
 		
 		public Predicate getPredicate() {
 			return predicate;
 		}
 		
 		public void setPredicate(Predicate predicate) {
 			this.predicate = predicate;
 		}
 		
 		public Statement getStatement() {
 			return statement;
 		}
 		
 		public void setStatement(Statement statement) {
 			this.statement = statement;
 		}
 
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.IF)),
 				new CharacterLiteral(WaebricSymbol.LPARANTHESIS),
 				predicate,
 				new CharacterLiteral(WaebricSymbol.RPARANTHESIS),
 				statement
 			};
 		}
 		
 	}
 	
 	/**
 	 * "if" "(" Predicate ")" Statement "else" Statement -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class IfElseStatement extends IfStatement {
 		
 		private Statement secondStatement;
 		
 		public Statement getSecondStatement() {
 			return secondStatement;
 		}
 		
 		public void setSecondStatement(Statement secondStatement) {
 			this.secondStatement = secondStatement;
 		}
 
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.IF)),
 				new CharacterLiteral(WaebricSymbol.LPARANTHESIS),
 				predicate,
 				new CharacterLiteral(WaebricSymbol.RPARANTHESIS),
 				statement,
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.IF)),
 				secondStatement
 			};
 		}
 		
 	}
 
 	/**
 	 * "each" "(" Var ":" Expression ")" Statement -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class EachStatement extends Statement {
 
 		private Var var;
 		private Expression expression;
 		private Statement statement;
 		
 		public Var getVar() {
 			return var;
 		}
 
 		public void setVar(Var var) {
 			this.var = var;
 		}
 
 		public Expression getExpression() {
 			return expression;
 		}
 
 		public void setExpression(Expression expression) {
 			this.expression = expression;
 		}
 
 		public Statement getStatement() {
 			return statement;
 		}
 
 		public void setStatement(Statement statement) {
 			this.statement = statement;
 		}
 
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.EACH)),
 				new CharacterLiteral(WaebricSymbol.LPARANTHESIS),
 				var,
 				new CharacterLiteral(WaebricSymbol.COLON),
 				expression,
 				new CharacterLiteral(WaebricSymbol.RPARANTHESIS),
 				statement
 			};
 		}
 		
 	}
 	
 	/**
 	 * "let" Assignment+ "in" Statement* "end" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class LetStatement extends Statement {
 
 		private SyntaxNodeList<Assignment> assignments;
 		private SyntaxNodeList<Statement> statements;
 		
		public LetStatement() {
			assignments = new SyntaxNodeList<Assignment>();
			statements = new SyntaxNodeList<Statement>();
		}
		
 		public boolean addAssignment(Assignment assignment) {
 			return assignments.add(assignment);
 		}
 		
 		public Assignment getAssignment(int index) {
 			return assignments.get(index);
 		}
 		
 		public int getAssignmentCount() {
 			return assignments.size();
 		}
 		
 		public boolean addStatement(Statement statement) {
 			return statements.add(statement);
 		}
 		
 		public Statement getStatement(int index) {
 			return statements.get(index);
 		}
 		
 		public int getStatementCount() {
 			return statements.size();
 		}
 		
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.LET)),
 				assignments,
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.IN)),
 				statements,
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.END))
 			};
 		}
 		
 	}
 	
 	/**
 	 * "{" Statement* "}"
 	 * @author schagen
 	 *
 	 */
 	public static class StatementCollection extends Statement {
 
 		private SyntaxNodeList<Statement> statements;
 		
 		public boolean addStatement(Statement statement) {
 			return statements.add(statement);
 		}
 		
 		public Statement getStatement(int index) {
 			return statements.get(index);
 		}
 		
 		public int getStatementCount() {
 			return statements.size();
 		}
 		
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new CharacterLiteral(WaebricSymbol.LCBRACKET),
 				statements,
 				new CharacterLiteral(WaebricSymbol.RCBRACKET)
 			};
 		}
 		
 	}
 	
 	/**
 	 * "comment" StrCon ";" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class CommentStatement extends Statement {
 
 		private StrCon comment;
 
 		public StrCon getComment() {
 			return comment;
 		}
 
 		public void setComment(StrCon comment) {
 			this.comment = comment;
 		}
 
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.COMMENT)),
 				comment,
 				new CharacterLiteral(WaebricSymbol.SEMICOLON)
 			};
 		}
 		
 	}
 	
 	/**
 	 * "echo" Expression ";" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class EchoExpressionStatement extends Statement {
 		
 		private Expression expression;
 
 		public Expression getExpression() {
 			return expression;
 		}
 
 		public void setExpression(Expression expression) {
 			this.expression = expression;
 		}
 		
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.ECHO)),
 				expression,
 				new CharacterLiteral(WaebricSymbol.SEMICOLON)
 			};
 		}
 		
 	}
 	
 	/**
 	 * "echo" Embedding ";" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class EchoEmbeddingStatement extends Statement {
 		
 		private Embedding embedding;
 
 		public Embedding getEmbedding() {
 			return embedding;
 		}
 
 		public void setEmbedding(Embedding embedding) {
 			this.embedding = embedding;
 		}
 		
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.ECHO)),
 				embedding,
 				new CharacterLiteral(WaebricSymbol.SEMICOLON)
 			};
 		}
 		
 	}
 	
 	/**
 	 * "cdata" Expression ";" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class CDataStatement extends Statement {
 
 		private Expression expression;
 		
 		public Expression getExpression() {
 			return expression;
 		}
 
 		public void setExpression(Expression expression) {
 			this.expression = expression;
 		}
 
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.CDATA)),
 				expression,
 				new CharacterLiteral(WaebricSymbol.SEMICOLON)
 			};
 		}
 		
 	}
 	
 	/**
 	 * "yield" ";" -> Statement
 	 * @author schagen
 	 *
 	 */
 	public static class YieldStatement extends Statement {
 		
 		public ISyntaxNode[] getChildren() {
 			return new ISyntaxNode[] {
 				new StringLiteral(WaebricKeyword.getLiteral(WaebricKeyword.YIELD)),
 				new CharacterLiteral(WaebricSymbol.SEMICOLON)
 			};
 		}
 		
 	}
 	
 }
