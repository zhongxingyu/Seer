 package org.eclipse.dltk.python.parser.ast.statements;
 
 import org.eclipse.dltk.ast.ASTVisitor;
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.ast.expressions.Expression;
 
 public class ExecStatement extends SimpleStatement {
 	Expression fIn;
 	Expression fIn2;
 	public ExecStatement(DLTKToken token, Expression expression) {
 		super(token, expression);
 	}
 	
 	public void acceptIn(Expression e ) {
 		fIn = e;
		if( e != null || e.sourceEnd() > this.sourceEnd() ) {
 			this.setEnd(e.sourceEnd());
 		}
 	}
 	public void acceptIn2(Expression e ) {
 		fIn2 = e;
		if( e != null || e.sourceEnd() > this.sourceEnd() ) {
 			this.setEnd(e.sourceEnd());
 		}
 	}
 
 	public int getKind() {
 		return 0;
 	}
 	public void traverse(ASTVisitor pVisitor) throws Exception {
 		if (pVisitor.visit(this)) {
 			if (fExpression != null) {
 				fExpression.traverse(pVisitor);
 			}
 			if( this.fIn != null ) {
 				this.fIn.traverse(pVisitor);
 			}
 			if( this.fIn2 != null ) {
 				this.fIn2.traverse(pVisitor);
 			}
 			pVisitor.endvisit(this);
 		}
 	}
 }
