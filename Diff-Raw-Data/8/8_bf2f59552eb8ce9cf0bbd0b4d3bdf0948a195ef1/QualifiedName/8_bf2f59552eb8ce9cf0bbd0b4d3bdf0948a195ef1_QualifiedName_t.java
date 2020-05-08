 package ca.uwaterloo.joos.ast.name;
 
import java.util.ArrayList;
 import java.util.List;
 
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.descriptor.SimpleListDescriptor;
 import ca.uwaterloo.joos.parser.ParseTree.LeafNode;
 import ca.uwaterloo.joos.parser.ParseTree.Node;
 import ch.lambdaj.Lambda;
 
 public class QualifiedName extends Name {
 
 	public static final SimpleListDescriptor COMPONENTS = new SimpleListDescriptor(String.class);
 
 	public QualifiedName(Node qualifiedName, ASTNode parent) throws Exception {
 		super(qualifiedName, parent);
 	}
 
 	public String getQualifiedName() throws ChildTypeUnmatchException {
 		return Lambda.join(this.getComponents(), ".");
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> getComponents() throws ChildTypeUnmatchException {
 		return (List<String>) this.getChildByDescriptor(COMPONENTS);
 	}
 
 	@Override
 	public String getName() throws Exception {
 		return this.getQualifiedName();
 	}
 
 	@Override
 	public void processLeafNode(LeafNode leafNode) throws Exception {
 		if (leafNode.token.getKind().equals("ID")) {
 			List<String> components = this.getComponents();
 			if (components == null) {
				components = new ArrayList<String>();
 				this.addChild(QualifiedName.COMPONENTS, components);
 			}
 
			components.add(leafNode.token.getLexeme());
 		}
 	}
 
 }
