 package haw.ci.lib.nodes;
 
 import haw.ci.lib.SymbolTable;
 import haw.ci.lib.descriptor.Descriptor;
 
 public class IfStatementNode extends AbstractNode {
 	private static final long serialVersionUID = -8068718524238360831L;
 	private AbstractNode expression;
 	private StatementSequenceNode statementSeq1;
 	private AbstractNode elsif;
 	private StatementSequenceNode statementSeq2;
 
 	public IfStatementNode(AbstractNode expression,
 			StatementSequenceNode statementSeq1, AbstractNode elsif,
 			StatementSequenceNode statementSeq2) {
 		this.expression = expression;
 		this.statementSeq1 = statementSeq1;
 		this.elsif = elsif;
 		this.statementSeq2 = statementSeq2;
 	}
 	
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((elsif == null) ? 0 : elsif.hashCode());
 		result = prime * result
 				+ ((expression == null) ? 0 : expression.hashCode());
 		result = prime * result
 				+ ((statementSeq1 == null) ? 0 : statementSeq1.hashCode());
 		result = prime * result
 				+ ((statementSeq2 == null) ? 0 : statementSeq2.hashCode());
 		return result;
 	}
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		IfStatementNode other = (IfStatementNode) obj;
 		if (elsif == null) {
 			if (other.elsif != null)
 				return false;
 		} else if (!elsif.equals(other.elsif))
 			return false;
 		if (expression == null) {
 			if (other.expression != null)
 				return false;
 		} else if (!expression.equals(other.expression))
 			return false;
 		if (statementSeq1 == null) {
 			if (other.statementSeq1 != null)
 				return false;
 		} else if (!statementSeq1.equals(other.statementSeq1))
 			return false;
 		if (statementSeq2 == null) {
 			if (other.statementSeq2 != null)
 				return false;
 		} else if (!statementSeq2.equals(other.statementSeq2))
 			return false;
 		return true;
 	}
 
 
 	@Override
 	public String toString(int indentation) {
 		String result = toString(indentation, this.getClass().getName() + "\n");
 		if(expression != null) {
		    result += expression.toString() + "\n";
 		}
 		if(statementSeq1 != null) {
 		    result += statementSeq1.toString(indentation+1);
 		}
 		if(elsif != null) {
 		    result += elsif.toString(indentation+1);
 		}
 		if(statementSeq2 != null) {
 		    result += statementSeq2.toString(indentation+1);
 		}
 	    return result;
 	}
 
 
 	@Override
 	public Descriptor compile(SymbolTable symbolTable) {
 		final int labelElse = getNextLabelNumber();
 		final int labelEnd = getNextLabelNumber();
 		
 		expression.compile(symbolTable);
 		branchFalse(labelElse);
 		statementSeq1.compile(symbolTable);
 		jump(labelEnd);
 		label(labelElse); 
 		if (elsif != null) elsif.compile(symbolTable);
 		if (statementSeq2 != null) statementSeq2.compile(symbolTable);
 		label(labelEnd);
 		return null;
 	}	
 }
