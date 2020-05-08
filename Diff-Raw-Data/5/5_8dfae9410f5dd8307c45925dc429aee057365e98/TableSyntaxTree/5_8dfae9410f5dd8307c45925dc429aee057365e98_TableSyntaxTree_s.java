 package parser;
 
 import grammar.NonterminalSymbol;
 import grammar.Production;
 import grammar.Symbol;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.activity.InvalidActivityException;
 
 public class TableSyntaxTree implements SyntaxTree {
 	class STCell {
 		private Integer parent;
 		private Integer leftSibling;
 		Symbol element;
 		
 		public STCell(Symbol s,Integer parent,Integer leftSibling) {
 			this.element = s;
 			this.parent = parent;
 			this.leftSibling = leftSibling;
 		}
 		
 		public Integer getParent() {
 			return parent;
 		}
 		public void setParent(int parent) {
 			this.parent = parent;
 		}
 		public Integer getLeftSibling() {
 			return leftSibling;
 		}
 		public void setLeftSibling(int leftSibling) {
 			this.leftSibling = leftSibling;
 		}
 		public Symbol getElement() {
 			return element;
 		}
 		public void setElement(Symbol element) {
 			this.element = element;
 		}
 	}
 	
 	List<STCell> table = new ArrayList<STCell>();
 		
 	List<Integer> leaves = new ArrayList<Integer>();
 	
 	public TableSyntaxTree() {
 		
 	}
 	
 	@Override
 	public void add(Production p) throws Exception {
 		if(table.isEmpty()) {
 			//We make the left hand side of the production the root
 			//and add the right hand side symbols as leaves.
 			table.add(new STCell(p.getNonterminal(),null,null));
 			Integer left = null;
 			for(Symbol s:p.getProduction()) {
 				table.add(new STCell(s,0,left));
 				if(left==null) {
 					left = 1;
 				}
 				else {
 					left++;
 				}
 			}
 		}
 		else {
 			int leftMostNonTerminalIndex = getLeftMostNonTerminalLeaf();
 			STCell cell = table.get(leftMostNonTerminalIndex);
			if(cell.getElement() != p.getNonterminal()) {
 				throw new Exception("Leftmost leaf has diferent symbol then lhs of the production");
 			}
 			
 			Integer left = null;
 			for(Symbol s:p.getProduction()) {
 				table.add(new STCell(s,leftMostNonTerminalIndex,left));
 				if(left==null) {
 					left = table.size();
 				}
 				else {
 					left++;
 				}
 			}
 		}
 	}	
 	
	private void printTable() {
 		for(int i=0;i<table.size();i++) {
 			STCell cell = table.get(i);
 			Symbol s = cell.getElement();
 			String parent = (cell.getParent()==null) ? "-" : cell.getParent().toString();
 			String left = (cell.getLeftSibling()==null) ? "-" : cell.getLeftSibling().toString();
 			System.out.println(i+" "+s+" "+ parent + " " + left);
 		}
  	}
 	
 	private List<Integer> getChildren(int index) {
 		List<Integer> children = new ArrayList<Integer>();
 		for(int i=0;i<table.size();i++) {
 			STCell cell = table.get(i);
 			if((cell.parent!=null) &&(cell.parent==index)) {
 				children.add(i);
 			}
 		}
 		return children;
 	}
 	
 	private boolean isLeaf(int index) {
 		return getChildren(index).isEmpty();
 	}
 	
 	
 	private List<Integer> getLeaves() {
 		traverse(0);
 		return leaves;
 	}
 	
 	
 	@Override
 	public List<Symbol> getSymbolLeaves() {
 		List<Symbol> leaves = new ArrayList<Symbol>();
 		
 		for(int i:getLeaves()) {
 			STCell cell = table.get(i);
 			leaves.add(cell.getElement());
 		}
 		
 		return leaves;
 	}
 	
 	private Integer getLeftMostNonTerminalLeaf() throws InvalidActivityException {
 		Integer first = null;
 		for(int index:getLeaves()) {
 			if(table.get(index).getElement() instanceof NonterminalSymbol ) {
 				return index;
 			}
 		}
 		
 		throw new InvalidActivityException("No more nonterminal leaves");
 	}
 	
 	public void traverse(int index) {
 		
 		List<Integer> children = getChildren(index);
 		Integer leftMost = null;
 		for(int i : children) {
 			if(table.get(i).leftSibling == null) {
 				leftMost = i;
 				break;
 			}
 		}
 		
 		children.remove(leftMost);
 		Collections.sort(children);
 		children.add(0, leftMost);
 		
 		for(int j:children) {
 			if(isLeaf(j)) {
 				leaves.add(j);
 			}
 			traverse(j);
 		}
 	}
 	
 	public static void main(String[] args) throws Exception {
 		TableSyntaxTree ts = new TableSyntaxTree();
 		
 		List<Symbol> p = new ArrayList<Symbol>();
 		p.add(new Symbol("A"));
 		p.add(new Symbol("B"));
 		p.add(new Symbol("C"));
 		p.add(new Symbol("D"));
 		Production prod = new Production(new NonterminalSymbol("S"), p);
 		
 		ts.add(prod);
 		
 		ts.printTable();
 			
 		
 	}
 }
