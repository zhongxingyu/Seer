 package org.ita.testrefactoring.astparser;
 
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.ita.testrefactoring.metacode.Expression;
 import org.ita.testrefactoring.metacode.Field;
 import org.ita.testrefactoring.metacode.NonAccessFieldModifier;
 import org.ita.testrefactoring.metacode.Type;
 import org.ita.testrefactoring.metacode.TypeListener;
 
 public class ASTField implements Field, ASTWrapper<FieldDeclaration> {
 
 	private String name;
 	private FieldTypeListener fieldTypeListener = new FieldTypeListener(); 
 	private Type fieldType;
 	private ParentTypeListener parentTypeListener = new ParentTypeListener();
 	private ASTType parent;
 	private NonAccessFieldModifier nonAccessModifier = new NonAccessFieldModifier();
 	private Expression initialization;
	private ASTInnerElementAccessModifier accessModifier = new ASTInnerElementAccessModifier();
 	private FieldDeclaration fieldDeclaration;
 	
 	
 	private class FieldTypeListener implements TypeListener {
 		@Override
 		public void typePromoted(Type oldType, Type newType) {
 			fieldType = newType;
 		}
 	}
 	
 	private class ParentTypeListener implements TypeListener {
 		@Override
 		public void typePromoted(Type oldType, Type newType) {
 			parent = (ASTType) newType;
 		}
 	}
 	
 	@Override
	public ASTInnerElementAccessModifier getAccessModifier() {
 		return accessModifier;
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public ASTType getParentType() {
 		return parent;
 	}
 	
 	void setParentType(ASTType parent) {
 		if (this.parent != null) {
 			this.parent.removeListener(parentTypeListener);
 		}
 		
 		this.parent = parent;
 		
 		if (this.parent != null) {
 			this.parent.addListener(parentTypeListener);
 		}
 	}
 
 	@Override
 	public Type getFieldType() {
 		return fieldType;
 	}
 	
 	void setFieldType(Type type) {
 		if (fieldType != null) {
 			fieldType.removeListener(fieldTypeListener);
 		}
 		
 		fieldType = type;
 		
 		if (fieldType != null) {
 			fieldType.addListener(fieldTypeListener);
 		}
 	}
 
 	@Override
 	public NonAccessFieldModifier getNonAccessModifier() {
 		return nonAccessModifier;
 	}
 
 	@Override
 	public Expression getInitialization() {
 		return initialization;
 	}
 	
 	public void setInitialization(Expression initialization) {
 		this.initialization = initialization;
 	}
 
 	@Override
 	public void setASTObject(FieldDeclaration astObject) {
 		this.fieldDeclaration = astObject;
 	}
 
 	@Override
 	public FieldDeclaration getASTObject() {
 		return fieldDeclaration;
 	}
 
 }
