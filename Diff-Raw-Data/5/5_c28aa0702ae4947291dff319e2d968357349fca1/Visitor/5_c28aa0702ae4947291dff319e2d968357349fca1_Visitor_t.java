 package ast.tools.internal.visitor;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.BodyDeclaration;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.Javadoc;
 import org.eclipse.jdt.core.dom.MarkerAnnotation;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.ParameterizedType;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
 import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
 import org.eclipse.jdt.core.dom.TagElement;
 import org.eclipse.jdt.core.dom.TextElement;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 
 import ast.tools.internal.model.impl.TClassImpl;
 import ast.tools.internal.model.impl.TParameterImpl;
 import ast.tools.internal.model.impl.TTagImpl;
 import ast.tools.internal.transformer.AnnotationTransformer;
 import ast.tools.model.TAnnotation;
 import ast.tools.model.TClass;
 import ast.tools.model.TField;
 import ast.tools.model.TImport;
 import ast.tools.model.TMethod;
 import ast.tools.model.TModifier;
 import ast.tools.model.TParameter;
 import ast.tools.model.TTag;
 
 import com.google.common.collect.Lists;
 
 public class Visitor extends ASTVisitor {
 
 	protected TClass klass;
 
 	protected boolean isInterface = true;
 	protected String className;
 	protected String superClassName;
 	protected String packageName;
 	protected Set<TAnnotation> annotations;
 	protected Set<TField> fields;
 	protected Set<TMethod> methods;
 	protected Set<TImport> imports;
 	protected Set<String> interfaces;
 	protected List<String> genericTypeArguments;
 	protected List<String> superClassGenericTypeArguments;
 	protected List<TTag> tags;
 
 	public Visitor() {
 		super();
 		this.annotations = new HashSet<TAnnotation>();
 		this.fields = new HashSet<TField>();
 		this.methods = new HashSet<TMethod>();
 		this.imports = new HashSet<TImport>();
 		this.interfaces = new HashSet<String>();
 		this.genericTypeArguments = new ArrayList<String>();
 		this.superClassGenericTypeArguments = new ArrayList<String>();
 		this.tags = new ArrayList<TTag>();
 
 	}
 
 	protected Set<String> getInterfaces(TypeDeclaration declaration) {
 		if (declaration.isInterface()) {
 
 		}
 		return null;
 	}
 
 	public TClass getTClass() {
 		return new TClassImpl(className, packageName, imports, annotations, interfaces, fields, methods,
 				genericTypeArguments, superClassName, superClassGenericTypeArguments, tags);
 	}
 
 	@SuppressWarnings("unchecked")
 	protected String getSuperClassName(TypeDeclaration declaration) {
 		if (declaration.getSuperclassType() != null) {
 			if (declaration.getSuperclassType().isSimpleType()) {
 				SimpleType simpleType = (SimpleType) declaration.getSuperclassType();
 				this.superClassName = simpleType.getName().toString();
 			} else if (declaration.getSuperclassType().isParameterizedType()) {
 				ParameterizedType parameterizedType = (ParameterizedType) declaration.getSuperclassType();
 				this.superClassName = parameterizedType.getType().toString();
 				this.superClassGenericTypeArguments.addAll(parameterizedType.typeArguments());
 			}
 		}
 		return this.superClassName;
 	}
 
 	/**
 	 * Return attribute name
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	protected String getName(FieldDeclaration declaration) {
 		return ((VariableDeclarationFragment) declaration.fragments().get(0)).getName().toString();
 	}
 
 	/**
 	 * Return attribute name
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	protected String getName(MethodDeclaration declaration) {
 		return declaration.getName().toString();
 	}
 
 	/**
 	 * Return attribute type
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected List<String> getTypes(FieldDeclaration declaration) {
 		return declaration.getType().getClass() == ParameterizedType.class ? ((ParameterizedType) declaration.getType())
 				.typeArguments() : Lists.newArrayList(declaration.getType().toString());
 	}
 
 	/**
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected List<String> getReturnTypes(MethodDeclaration declaration) {
 		Type type = declaration.getReturnType2();
 		if (type != null) {
 			if (type.getClass() == ParameterizedType.class) {
 				ParameterizedType parameterizedType = (ParameterizedType) type;
 				return parameterizedType.typeArguments();
 			} else {
 				return Lists.newArrayList(declaration.getReturnType2().toString());
 			}
 		}
 		return new ArrayList<String>();
 	}
 
 	/**
 	 * Return attribute generic type
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	protected String getGenericType(FieldDeclaration declaration) {
 		Type type = declaration.getType();
 		if (type.getClass() == ParameterizedType.class) {
 			ParameterizedType parameterizedType = (ParameterizedType) type;
 			if (parameterizedType.getType().getClass() == SimpleType.class) {
 				SimpleType simpleType = (SimpleType) parameterizedType.getType();
 				return simpleType.getName().toString();
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * 
 	 * @param declaration
 	 * @return
 	 */
 	protected String getReturnGenericType(MethodDeclaration declaration) {
 		Type type = declaration.getReturnType2();
 		// caso o método seja construtor nao processa
 		if (type != null) {
 			if (type.getClass() == ParameterizedType.class) {
 				ParameterizedType parameterizedType = (ParameterizedType) type;
 				if (parameterizedType.getType().getClass() == SimpleType.class) {
 					SimpleType simpleType = (SimpleType) parameterizedType.getType();
 					return simpleType.getName().toString();
 				}
 			}
 		}
 		return "";
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Set<TParameter> getParameters(MethodDeclaration declaration) {
 		Set<TParameter> parameters = new HashSet<TParameter>();
 
 		List<SingleVariableDeclaration> parameterList = declaration.parameters();
 		for (SingleVariableDeclaration singleVariableDeclaration : parameterList) {
 			String parameterName = singleVariableDeclaration.getName().toString();
 			String genericType = null;
 			List<String> types = new ArrayList<String>();
 
 			if (singleVariableDeclaration.getType().getClass() == ParameterizedType.class) {
 				ParameterizedType parameterizedType = (ParameterizedType) singleVariableDeclaration.getType();
 				types.addAll(parameterizedType.typeArguments());
 				genericType = parameterizedType.getType().toString();
 			} else {
 				types.add(singleVariableDeclaration.getType().toString());
 			}
 			parameters.add(new TParameterImpl(parameterName, types, genericType));
 		}
 
 		return parameters;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Set<TModifier> getModifiers(BodyDeclaration declaration) {
 		Set<TModifier> modifiers = new HashSet<TModifier>();
 		modifiers.addAll(declaration.modifiers());
 		return modifiers;
 	}
 
 	protected Set<TAnnotation> processAnnotations(BodyDeclaration declaration) {
 		Set<TAnnotation> annotationsSet = new HashSet<TAnnotation>();
 
 		annotationsSet.addAll(processSingleMemberAnnotation(declaration));
 		annotationsSet.addAll(processNormalAnnotation(declaration));
 		annotationsSet.addAll(processMarkerAnnotation(declaration));
 
 		return annotationsSet;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<TAnnotation> processMarkerAnnotation(BodyDeclaration node) {
 
 		Collection<MarkerAnnotation> markerAnnotationList = CollectionUtils.select(node.modifiers(), new Predicate() {
 
 			@Override
 			public boolean evaluate(Object annotation) {
 				return annotation.getClass() == MarkerAnnotation.class;
 			}
 		});
 
 		return CollectionUtils.collect(markerAnnotationList, new AnnotationTransformer().new MarkerAnnotationTransformer());
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<TAnnotation> processSingleMemberAnnotation(BodyDeclaration node) {
 		Collection<SingleMemberAnnotation> singleMemberAnnotationList = CollectionUtils.select(node.modifiers(),
 				new Predicate() {
 
 			@Override
 			public boolean evaluate(Object annotation) {
 				return annotation.getClass() == SingleMemberAnnotation.class;
 			}
 		});
 
 		return CollectionUtils.collect(singleMemberAnnotationList,
 				new AnnotationTransformer().new SingleMemberAnnotationTransformer());
 	}
 
 	@SuppressWarnings("unchecked")
 	protected Collection<TAnnotation> processNormalAnnotation(BodyDeclaration node) {
 		Collection<NormalAnnotation> normalAnnotationList = CollectionUtils.select(node.modifiers(), new Predicate() {
 
 			@Override
 			public boolean evaluate(Object annotation) {
 				return annotation.getClass() == NormalAnnotation.class;
 			}
 		});
 		return CollectionUtils.collect(normalAnnotationList, new AnnotationTransformer().new NormalAnnotationTransformer());
 	}
 
 	@SuppressWarnings("unchecked")
 	protected List<TTag> getTags(Javadoc javadoc) {
 		List<TTag> tags = new ArrayList<TTag>();
 		if (javadoc != null) {
 			List<TagElement> elements = javadoc.tags();
 			for (TagElement tagElement : elements) {
 				if (!StringUtils.isEmpty(tagElement.getTagName())) {
 					List<TextElement> fragments = tagElement.fragments();
 					if (!fragments.isEmpty()) {
						TextElement element = fragments.get(0);
						if(element != null) {
							tags.add(new TTagImpl(tagElement.getTagName().substring(1), element.getText()));
						}
 					}
 				}
 			}
 		}
 		return tags;
 	}
 }
