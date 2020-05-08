 package haw.ci.lib.nodes;
 
 import haw.ci.lib.SymbolTable;
 import haw.ci.lib.descriptor.Descriptor;
 
 public class AssignmentNode extends AbstractNode {
 	private static final long serialVersionUID = 1L;
 	private final SelectorNode selector;
 	private final AbstractNode expression;
 	private IdentNode ident;
 
 	public AssignmentNode(IdentNode ident, SelectorNode selector, AbstractNode expression) {
 		this.ident = ident;
 		this.selector = selector;
 		this.expression = expression;
 	}
 	
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((expression == null) ? 0 : expression.hashCode());
 		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
 		result = prime * result
 				+ ((selector == null) ? 0 : selector.hashCode());
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
 		AssignmentNode other = (AssignmentNode) obj;
 		if (expression == null) {
 			if (other.expression != null)
 				return false;
 		} else if (!expression.equals(other.expression))
 			return false;
 		if (ident == null) {
 			if (other.ident != null)
 				return false;
 		} else if (!ident.equals(other.ident))
 			return false;
 		if (selector == null) {
 			if (other.selector != null)
 				return false;
 		} else if (!selector.equals(other.selector))
 			return false;
 		return true;
 	}
 
 
 
 	@Override
 	public String toString(int indentation) {
 		String result = toString(indentation, this.getClass().getName() + "\n");
 		if(ident != null) {
 		    result += ident.toString(indentation+1) + "\n";
 		}
 		if(expression != null) {
 		    result += expression.toString(indentation+1);
 		}
 	    return result;
 	}
 	
 	@Override
 	public Descriptor compile(SymbolTable symbolTable) {
		expression.compile(symbolTable);
 	    ident.compile(symbolTable);
	    if (selector != null) { // FIXME: is this the right place to do?
 	    	selector.compile(symbolTable);
 	    }
 	    write("ASSIGN, 1");
 	    return null;
 	}
 
 }
