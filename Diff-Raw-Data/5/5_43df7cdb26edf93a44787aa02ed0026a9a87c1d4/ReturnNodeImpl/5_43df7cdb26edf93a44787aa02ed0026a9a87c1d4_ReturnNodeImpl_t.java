 package swp_compiler_ss13.fuc.ast;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import swp_compiler_ss13.common.ast.ASTNode;
 import swp_compiler_ss13.common.ast.nodes.IdentifierNode;
 import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
 
 /**
  * ReturnNode implementation
  * 
  * @author "Frank Zechert, Danny Maasch"
  * @version 1
  */
 public class ReturnNodeImpl extends ASTNodeImpl implements ReturnNode {
 
 	/**
 	 * The optional right hand value of the return statement
 	 */
 	private IdentifierNode rightNode;
 
 	@Override
 	public ASTNodeType getNodeType() {
 		return ASTNodeType.ReturnNode;
 	}
 
 	@Override
 	public Integer getNumberOfNodes() {
 		if (this.rightNode == null) {
 			return 1;
 		}
 
 		return 1 + this.rightNode.getNumberOfNodes();
 	}
 
 	@Override
 	public List<ASTNode> getChildren() {
 		if (this.rightNode == null) {
 			return new LinkedList<>();
 		}
 
 		List<ASTNode> children = new LinkedList<>();
 		children.add(this.rightNode);
 		return children;
 	}
 
 	@Override
 	public void setRightValue(IdentifierNode identifier) {
 		this.rightNode = identifier;
		
		if (identifier != null) {
			identifier.setParentNode(this);
		}
 	}
 
 	@Override
 	public IdentifierNode getRightValue() {
 		return this.rightNode;
 	}
 }
