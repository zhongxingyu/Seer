 package haw.ci.lib.nodes;
 
 import haw.ci.lib.SymbolTable;
 import haw.ci.lib.descriptor.Descriptor;
 
 public class PrintNode extends AbstractNode {
 	private static final long serialVersionUID = 1664634323229382354L;
 	private AbstractNode expression;
 
 	public PrintNode(AbstractNode expression) {
 		this.expression = expression;
 	}
 	
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((expression == null) ? 0 : expression.hashCode());
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
 		PrintNode other = (PrintNode) obj;
 		if (expression == null) {
 			if (other.expression != null)
 				return false;
 		} else if (!expression.equals(other.expression))
 			return false;
 		return true;
 	}
 
 
 	@Override
 	public String toString(int indentation) {
 		String result = toString(indentation, this.getClass().getName() + "\n");
 		if(expression != null) {
		    result += expression.toString(indentation+1) + "\n";
 		}
 
 	    return result;
 	}
 
 	@Override 
 	public Descriptor compile(SymbolTable symbolTable) {
 		expression.compile(symbolTable);
 		if (expression instanceof IdentNode) write("CONT, 1");
 		write("PRINT");
 		return null;
 	}
 }
