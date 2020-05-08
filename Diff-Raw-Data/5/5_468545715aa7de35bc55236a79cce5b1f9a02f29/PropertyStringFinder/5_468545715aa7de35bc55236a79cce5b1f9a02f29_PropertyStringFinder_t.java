 package jp.hishidama.xtext.dmdl_editor.jdt.hyperlink;
 
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelDefinition;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelUiUtil;
 import jp.hishidama.xtext.dmdl_editor.dmdl.ModelUtil;
 import jp.hishidama.xtext.dmdl_editor.dmdl.Property;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.MemberValuePair;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.StringLiteral;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.Region;
 
 public class PropertyStringFinder extends ASTVisitor {
 	private ICompilationUnit unit;
 	private int offset;
 
 	private IRegion region;
 	private SingleVariableDeclaration declaration;
 	private String memberName;
 	private String propertyName;
 
 	public PropertyStringFinder(ICompilationUnit unit, int offset) {
 		this.unit = unit;
 		this.offset = offset;
 	}
 
 	public Property getProperty() {
 		visit();
 		if (propertyName == null) {
 			return null;
 		}
 
 		ModelDefinition model = getModel();
 		Property property = ModelUtil.findProperty(model, propertyName);
 		return property;
 	}
 
 	private ModelDefinition model;
 
 	public ModelDefinition getModel() {
 		if (model != null) {
 			return model;
 		}
 
 		visit();
 		if (declaration == null) {
 			return null;
 		}
 
 		IProject project = unit.getJavaProject().getProject();
 		String modelClassName = declaration.getType().toString();
		int n = modelClassName.indexOf('<');
		int e = modelClassName.lastIndexOf('>');
		if (n >= 0 && e >= 0) {
			modelClassName = modelClassName.substring(n + 1, e);
		}
 		this.model = ModelUiUtil.findModelByClass(project, modelClassName);
 		return model;
 	}
 
 	public IRegion getRegion() {
 		visit();
 		return region;
 	}
 
 	private boolean visited = false;
 
 	private void visit() {
 		if (visited) {
 			return;
 		}
 		visited = true;
 
 		ASTParser parser = ASTParser.newParser(AST.JLS4);
 		parser.setSource(unit);
 		parser.setSourceRange(offset, 1);
 		ASTNode node = parser.createAST(new NullProgressMonitor());
 		node.accept(this);
 	}
 
 	@Override
 	public boolean preVisit2(ASTNode node) {
 		int offset = node.getStartPosition();
 		int length = node.getLength();
 		return offset <= this.offset && this.offset <= offset + length;
 	}
 
 	@Override
 	public void endVisit(SingleVariableDeclaration node) {
 		if (propertyName != null) {
 			if (declaration == null) {
 				this.declaration = node;
 			}
 		}
 	}
 
 	@Override
 	public boolean visit(NormalAnnotation node) {
 		String name = node.getTypeName().getFullyQualifiedName();
 		return "Key".equals(name);
 	}
 
 	@Override
 	public boolean visit(MemberValuePair node) {
 		this.memberName = node.getName().getIdentifier();
 		return "group".equals(memberName) || "order".equals(memberName);
 	}
 
 	@Override
 	public boolean visit(StringLiteral node) {
 		String value = node.getLiteralValue();
 		int s = 0;
 		for (; s < value.length(); s++) {
 			char c = value.charAt(s);
 			switch (c) {
 			case '+':
 			case '-':
 			case ' ':
 			case '\t':
 				continue;
 			}
 			break;
 		}
 
 		int n = value.indexOf(' ', s);
 		if (n < 0) {
 			n = value.length();
 		}
 		this.propertyName = value.substring(s, n);
 		this.region = new Region(node.getStartPosition() + 1 + s, propertyName.length());
 		return false;
 	}
 }
