 package ast;
 
 import java.util.List;
 
 public class DataTypeDeclNode extends GlobalDeclNode {
 
 	protected ParameterListNode pl;
 
	public DataTypeDeclNode(ParameterListNode pl) {
		this.pl=pl;
	}  
 
 
 }
