 package de.uni_koblenz.jgralab.java_extractor.utilities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import de.uni_koblenz.edl.parser.Position;
 import de.uni_koblenz.edl.parser.symboltable.SymbolTableStack;
 import de.uni_koblenz.edl.preprocessor.schema.semantic_actions.ConstructorCall;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.EdgeDirection;
 import de.uni_koblenz.jgralab.TemporaryEdge;
 import de.uni_koblenz.jgralab.TemporaryVertex;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.graphmarker.GraphMarker;
 import de.uni_koblenz.jgralab.java_extractor.builder.Java5Builder;
 import de.uni_koblenz.jgralab.java_extractor.builder.Java5Builder.Mode;
 import de.uni_koblenz.jgralab.java_extractor.schema.annotation.ElementValuePair;
 import de.uni_koblenz.jgralab.java_extractor.schema.annotation.SetsField;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.AttributedEdge;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.ContainsStatement;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.Identifier;
 import de.uni_koblenz.jgralab.java_extractor.schema.common.JavaVertex;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.DeclaresInvokedMethod;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.Expression;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.FieldAccess;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.HasInvokedMethodName;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.HasVariableName;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.InvokesConstructor;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.MethodInvocation;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.OwnsField;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.SuperFieldAccess;
 import de.uni_koblenz.jgralab.java_extractor.schema.expression.VariableAccess;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.ConstructorDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Field;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Member;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.MethodDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Modifier;
 import de.uni_koblenz.jgralab.java_extractor.schema.member.Modifiers;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.DeclaresExternalDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.DeclaresImportedStaticMember;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.DeclaresImportedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.DefinesImport;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.ExternalDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.ImportDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.JavaPackage;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.SingleStaticImportDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.SingleTypeImportDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.StaticImportOnDemandDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.TranslationUnit;
 import de.uni_koblenz.jgralab.java_extractor.schema.program.TypeImportOnDemandDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.statement.Statement;
 import de.uni_koblenz.jgralab.java_extractor.schema.statement.TypeDefinitionStatement;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.AnnotationDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.ClassDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.Extends;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.ExtendsClass;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.ImplementedInterfacesFromClass;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.Implements;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.InterfaceDefinition;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.SpecificType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.Type;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.definition.TypeParameterDeclaration;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.EnclosedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.IsDefinedByType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.QualifiedName;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.QualifiedType;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.TypeParameterUsage;
 import de.uni_koblenz.jgralab.java_extractor.schema.type.specification.TypeSpecification;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 public class Linker {
 
 	private final Java5Builder graphBuilder;
 
 	/**
 	 * All currently used {@link Identifier}.
 	 */
 	private SymbolTableStack name2Identifier;
 
 	/**
 	 * All packages defined by package declarations including their
 	 * superpackages. At least the default package (unnamed package) is
 	 * contained.
 	 */
 	private SymbolTableStack packageNames;
 
 	/**
 	 * Contains the elements in nesting order, i.e., super type definitions of
 	 * outer class stand before the super type definitions of the inner classes.
 	 * 
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>extends &lt;TypeName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link QualifiedType} or {@link EnclosedType}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>implements &lt;TypeName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link QualifiedType} or {@link EnclosedType}</td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final List<Vertex> superTypeNames = new LinkedList<Vertex>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>import &lt;TypeName&gt;;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link SingleTypeImportDefinition} --{@link DefinesImport}-&gt;
 	 * <code>&lt;TypeName&gt;</code></td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<SingleTypeImportDefinition> singleTypeImports = new HashSet<SingleTypeImportDefinition>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <td><code>import static &lt;TypeName&gt;.aMemberName;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link SingleStaticImportDefinition} -- {@link DefinesImport}-&gt;
 	 * <code>&lt;TypeName&gt;</code><br>
 	 * {@link SingleStaticImportDefinition} --
 	 * {@link DeclaresImportedStaticMember}-&gt; {@link TemporaryVertex}{name =
 	 * aMemberName}</td>
 	 * </tr> </tbody>
 	 * </table>
 	 */
 	private final List<SingleStaticImportDefinition> singleStaticImports = new LinkedList<SingleStaticImportDefinition>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>import &lt;PackageOrTypeName&gt;.*;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link TypeImportOnDemandDefinition} --{@link DefinesImport}-&gt;
 	 * <code>&lt;PackageOrTypeName&gt;</code></td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<TypeImportOnDemandDefinition> onDemandTypeImports = new HashSet<TypeImportOnDemandDefinition>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>import static &lt;TypeName&gt;.*;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link StaticImportOnDemandDefinition} --{@link DefinesImport}-&gt;
 	 * <code>&lt;TypeName&gt;</code></td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<StaticImportOnDemandDefinition> onDemandStaticImports = new HashSet<StaticImportOnDemandDefinition>();
 
 	private final Set<TypeParameterDeclaration> typeParameters = new HashSet<TypeParameterDeclaration>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td>all <code>ClassifierType</code>s except if they occur behind extends
 	 * or implements</td>
 	 * <td>=&gt;</td>
 	 * <td>{@link QualifiedType} or {@link EnclosedType}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>@&lt;TypeName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td>{@link QualifiedType}</td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 * {@link QualifiedType}s have to be replaced by {@link TypeParameterUsage}
 	 * if they refer to a {@link TypeParameterDeclaration}.
 	 */
 	private final Set<Vertex> typeNames = new HashSet<Vertex>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>super.&lt;ExpressionName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link SuperFieldAccess} --{@link HasVariableName}-&gt;
 	 * <code>&lt;ExpressionName&gt;</code></td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>AType.super.&lt;ExpressionName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link SuperFieldAccess} --{@link HasVariableName}-&gt;
 	 * <code>&lt;ExpressionName&gt;</code></td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;ExpressionName&gt;.new AType()...</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{preliminaryType={@link VariableAccess}
 	 * ,lexem=&lt;ExpressionName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;ExpressionName&gt;</code> (= a single variable)</td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{preliminaryType={@link VariableAccess}
 	 * ,lexem=&lt;ExpressionName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName&gt;.&lt;ExpressionName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{preliminaryType={@link VariableAccess}
 	 * ,lexem=&lt;ExpressionName&gt;} -- {@link TemporaryEdge}{preliminaryType=
 	 * {@link OwnsField} -&gt; {@link TemporaryVertex}
 	 * {lexem=&lt;AmbiguousName&gt;} (only the first vertex is contained)</td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<Vertex> expressionNames = new HashSet<Vertex>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>@AnAnnotation(&lt;MethodName&gt;=...)</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link ElementValuePair} --{@link SetsField}-&gt; {@link FieldAccess}
 	 * --{@link HasVariableName}-&gt; <code>&lt;MethodName&gt;</code></td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;MethodName&gt;(...)</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link MethodInvocation} --{@link HasInvokedMethodName}-&gt;
 	 * <code>&lt;MethodName&gt;</code></td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<Vertex> methodNames = new HashSet<Vertex>();
 
 	/**
 	 * The following {@link MethodInvocation}s have to be linked by an edge of
 	 * type {@link DeclaresInvokedMethod} with the corresponding
 	 * {@link ConstructorDefinition}.<br>
 	 * <code>{@link ConstructorCall} --{@link InvokesConstructor}-&gt; <strong>{@link MethodInvocation}</strong>
 	 */
 	private final Set<Vertex> constructorNames = new HashSet<Vertex>();
 
 	/**
 	 * <table>
 	 * <tbody>
 	 * <tr>
 	 * <td><code>@AnAnnotation(aMethodName=&lt;AmbiguousName&gt;)</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>public AType aMethod() default &lt;AmbiguousName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName&gt;.super()</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName&gt;.this()</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName&gt;.aSimpleMethodName(...)</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName&gt;.anExpressionName</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;}</td>
 	 * </tr>
 	 * <tr>
 	 * <td><code>&lt;AmbiguousName2&gt;.&lt;AmbiguousName&gt;</code></td>
 	 * <td>=&gt;</td>
 	 * <td> {@link TemporaryVertex}{lexem=&lt;AmbiguousName&gt;} --
 	 * {@link TemporaryEdge}{preliminaryType={@link OwnsField} -&gt;
 	 * {@link TemporaryVertex}{lexem=&lt;AmbiguousName2&gt;} (both vertices are
 	 * contained)</td>
 	 * </tr>
 	 * </tbody>
 	 * </table>
 	 */
 	private final Set<Vertex> ambiguousNames = new HashSet<Vertex>();
 
 	/**
 	 * Enum constants are constructor invocations.
 	 */
 	private final Set<Vertex> enumConstants = new HashSet<Vertex>();
 
 	/**
 	 * Contains the anonymous classes (not enum classes):<br>
 	 * <code><strong>{@link ClassDefinition}</strong> --{@link ExtendsClass}-&gt; {@link QualifiedType}</code>
 	 * <br>
 	 * or<br>
 	 * <code><strong>{@link ClassDefinition}</strong> --{@link ExtendsClass}-&gt; {@link EnclosedType}</code>
 	 * <br>
 	 * The edge {@link ExtendsClass} might be replaced by
 	 * {@link ImplementedInterfacesFromClass}.
 	 */
 	private final Set<Vertex> anonymousClasses = new HashSet<Vertex>();
 
 	public Linker(Java5Builder graphBuilder) {
 		this.graphBuilder = graphBuilder;
 	}
 
 	public void setPackageNames(Object packageNames) {
 		this.packageNames = (SymbolTableStack) packageNames;
 	}
 
 	public void setName2Identifier(Object name2Identifier) {
 		this.name2Identifier = (SymbolTableStack) name2Identifier;
 	}
 
 	public void addTypeName(Vertex typeName) {
 		typeNames.add(typeName);
 	}
 
 	public void addTypeParameterDeclaration(Vertex typeParameterDeclaration) {
 		typeParameters.add((TypeParameterDeclaration) typeParameterDeclaration);
 	}
 
 	public void addSuperTypeName(Vertex superTypeName) {
 		assert !superTypeNames.contains(superTypeName);
 		superTypeNames.add(superTypeName);
 	}
 
 	public void addExpressionName(Vertex expressionName) {
 		expressionNames.add(expressionName);
 	}
 
 	public void addMethodName(Vertex methodName) {
 		methodNames.add(methodName);
 	}
 
 	public void addConstructorName(Vertex constructorName) {
 		constructorNames.add(constructorName);
 	}
 
 	public void addImport(Vertex anImport) {
 		if (anImport.isInstanceOf(SingleTypeImportDefinition.VC)) {
 			singleTypeImports.add((SingleTypeImportDefinition) anImport);
 		} else if (anImport.isInstanceOf(SingleStaticImportDefinition.VC)) {
 			assert !singleStaticImports.contains(anImport);
 			singleStaticImports.add((SingleStaticImportDefinition) anImport);
 		} else if (anImport.isInstanceOf(TypeImportOnDemandDefinition.VC)) {
 			onDemandTypeImports.add((TypeImportOnDemandDefinition) anImport);
 		} else {
 			assert anImport.isInstanceOf(StaticImportOnDemandDefinition.VC);
 			onDemandStaticImports
 					.add((StaticImportOnDemandDefinition) anImport);
 		}
 	}
 
 	public void addAmbiguousName(Vertex ambiguousName) {
 		ambiguousNames.add(ambiguousName);
 	}
 
 	public void addEnumConstant(Vertex enumConstant) {
 		enumConstants.add(enumConstant);
 	}
 
 	public void addAnonymousClass(Vertex anonymousClass) {
 		anonymousClasses.add(anonymousClass);
 	}
 
 	// TODO fill FieldAccess.qualifiedAccess and
 	// MethodInvocation.qualifiedInvocation
 
 	public void link(Mode mode) {
 		determineScopesOfParsedTypes();
 		publishTypeParameters();
 		resolveSingleTypeAndOnDemandTypeImports(mode);
 		resolveStaticOnDemandImports(mode);
 		resolveExtendsAndImplements(mode);
 		resolveSingleStaticImports(mode);
 		resolveTypeNames(mode);
 		// TODO publish members of Object to types with no supertype??
 		// or implement a lazy lookup strategy for members (depending on mode)
 
 		// if java and java.lang has no imported or defined type, delete them
 		JavaPackage java = getPackageWithSimpleName(mode, "java", null);
 		JavaPackage javaLang = getPackageWithSimpleName(mode, "lang", java);
 		if (javaLang != null && javaLang.getDegree(EdgeDirection.OUT) == 0) {
 			javaLang.delete();
 		}
 		if (java != null && java.getDegree(EdgeDirection.OUT) == 0) {
 			java.delete();
 		}
 
 		// below all temporary vertices are blessed
 		// TODO delete this code when Linker is finished
 		for (Vertex v : ambiguousNames) {
 			TemporaryVertex tv = (TemporaryVertex) v;
 			String name = v.getAttribute("lexem");
 			FieldAccess fa = (FieldAccess) blessVertex(tv, FieldAccess.VC);
 			fa.set_qualifiedAccess(name);
 		}
 		for (Vertex v : expressionNames) {
 			if (v.isTemporary()) {
 				TemporaryVertex tv = (TemporaryVertex) v;
 				String name = v.getAttribute("lexem");
 				FieldAccess fa = (FieldAccess) blessVertex(tv, FieldAccess.VC);
 				fa.set_qualifiedAccess(name);
 			}
 		}
 		for (Vertex v : graphBuilder.getGraph().vertices()) {
 			if (v.isTemporary()) {
 				System.out.println(v);
 			}
 		}
 		for (Edge e : graphBuilder.getGraph().edges()) {
 			if (e.isTemporary()) {
 				System.out.println(e.getAlpha() + " --" + e + "-> "
 						+ e.getOmega());
 			}
 		}
 	}
 
 	/*
 	 * resolve type names
 	 */
 
 	private void resolveTypeNames(Mode mode) {
 		for (Vertex typeName : typeNames) {
 			resolveTypeName(mode, (TypeSpecification) typeName);
 		}
 	}
 
 	private void resolveTypeName(Mode mode, TypeSpecification typeName) {
 		String name = getQualifiedName(typeName);
 		Type referedType = getTypeOrPackageWithName(mode, name, typeName,
 				false, Type.class);
 		if (referedType == null) {
 			return;
 		}
 		if (referedType.isInstanceOf(SpecificType.VC)) {
 			graphBuilder.createEdge(IsDefinedByType.EC, typeName, referedType);
 		} else {
 			assert referedType.isInstanceOf(TypeParameterDeclaration.VC);
 			// this is the use of a type parameter
 			// it is modeled as a qualified type wrongly
 			TypeParameterUsage typeParameterUsage = (TypeParameterUsage) graphBuilder
 					.createVertex(TypeParameterUsage.VC, graphBuilder
 							.getPositionsMap().get(typeName));
 			Edge edge2Parent = ((JavaVertex) typeName)
 					.getFirstAttributedEdgeIncidence(EdgeDirection.IN)
 					.getReversedEdge();
 			Edge newEdge2Parent = graphBuilder.createEdge(
 					edge2Parent.getAttributedElementClass(),
 					edge2Parent.getAlpha(), typeParameterUsage);
 			newEdge2Parent.putIncidenceBefore(edge2Parent);
 			graphBuilder.createEdge(IsDefinedByType.EC, typeParameterUsage,
 					referedType);
 			typeName.delete();
 		}
 	}
 
 	private void publishTypeParameters() {
 		for (TypeParameterDeclaration typeParameterDeclaration : typeParameters) {
 			Vertex genericMember = typeParameterDeclaration
 					.get_genericElement();
 			assert genericMember != null;
 			addType(genericMember, typeParameterDeclaration);
 		}
 	}
 
 	/*
 	 * resolve super type hierarchy consisting of extends and implements
 	 */
 
 	private void resolveExtendsAndImplements(Mode mode) {
 		while (!superTypeNames.isEmpty()) {
 			// if C' is nested in C, then C is resolved first
 			Vertex v = superTypeNames.remove(0);
 			resolveSuperTypeHierarchy(mode, (TypeSpecification) v);
 		}
 		for (Vertex v : anonymousClasses) {
 			ClassDefinition anonymousClass = (ClassDefinition) v;
 			ExtendsClass extendsClass = anonymousClass
 					.getFirstExtendsClassIncidence(EdgeDirection.OUT);
 			TypeSpecification superTypeSpecification = extendsClass.getOmega();
 			if (superTypeSpecification.getDegree(IsDefinedByType.EC,
 					EdgeDirection.OUT) == 0) {
 				// the current type is yet unknown
 				JavaVertex context = anonymousClass
 						.getFirstIsDefinedByTypeIncidence(EdgeDirection.IN)
 						.getAlpha();
 				String nameOfSuperClass = getQualifiedName(superTypeSpecification);
 				SpecificType superType = getTypeOrPackageWithName(mode,
 						nameOfSuperClass, context, false, SpecificType.class);
				if (superType == null) {
					System.err.println("Couldn't resolve supertype "
							+ nameOfSuperClass + " of anonymous class "
							+ anonymousClass.get_canonicalName());
					continue;
				}
 				if (superType.isInstanceOf(InterfaceDefinition.VC)
 						|| superType.isInstanceOf(AnnotationDefinition.VC)) {
 					extendsClass.delete();
 					graphBuilder.createEdge(ImplementedInterfacesFromClass.EC,
 							anonymousClass, superTypeSpecification);
 				}
 				graphBuilder.createEdge(IsDefinedByType.EC,
 						superTypeSpecification, superType);
 				typeNames.remove(superTypeSpecification);
 			}
 		}
 	}
 
 	private void resolveSuperTypeHierarchy(Mode mode,
 			TypeSpecification superType) {
 		for (@SuppressWarnings("unused")
 		Type type : superType.get_definingType()) {
 			// the superType has already been linked
 			return;
 		}
 		superTypeNames.remove(superType);
 		String name = getQualifiedName(superType);
 		SpecificType currentType = getCurrentSpecificType(superType);
 		// the super type may have been statically imported
 		for (SingleStaticImportDefinition anImport : getUnresolvedSingleStaticImportsWithName(
 				superType, currentType)) {
 			resolveSingleStaticImport(mode, anImport);
 		}
 
 		SpecificType specificSuperType = getTypeOrPackageWithName(mode, name,
 				currentType, false, SpecificType.class);
 		if (specificSuperType == null) {
 			// the super type is unknown
 		} else {
 			graphBuilder.createEdge(IsDefinedByType.EC, superType,
 					specificSuperType);
 			for (TypeSpecification superSuperType : getDefinedSuperTypes(specificSuperType)) {
 				resolveSuperTypeHierarchy(mode, superSuperType);
 			}
 			publishInheritedMembers(mode, currentType, specificSuperType);
 		}
 	}
 
 	/**
 	 * Finds all unresolved {@link SingleStaticImportDefinition}s of the
 	 * {@link TranslationUnit} in which <code>contextOfImports</code> is
 	 * contained. The simple name of the imported member has to occur in the
 	 * qualified name of <code>superType</code>.
 	 * 
 	 * @param superType
 	 * @param contextOfImports
 	 * @return
 	 */
 	private Set<SingleStaticImportDefinition> getUnresolvedSingleStaticImportsWithName(
 			TypeSpecification superType, JavaVertex contextOfImports) {
 		Set<SingleStaticImportDefinition> result = new HashSet<SingleStaticImportDefinition>();
 
 		List<JavaVertex> scopes = determineScopes(contextOfImports);
 		TranslationUnit translationUnit = (TranslationUnit) scopes.get(scopes
 				.size() - 2);
 		for (ExternalDeclaration exDecl : translationUnit
 				.get_declaredExternalDeclaration()) {
 			if (exDecl.isInstanceOf(SingleStaticImportDefinition.VC)) {
 				SingleStaticImportDefinition anImport = (SingleStaticImportDefinition) exDecl;
 				TemporaryVertex importedMemberName = null;
 				for (Edge edge : anImport.incidences(EdgeDirection.OUT)) {
 					if (edge.isTemporary()
 							&& ((TemporaryEdge) edge).getPreliminaryType() == DeclaresImportedStaticMember.EC) {
 						importedMemberName = (TemporaryVertex) edge.getThat();
 					}
 				}
 				String nameOfMember = getQualifiedName(superType);
 				if (importedMemberName == null
 						|| !nameOfMember.matches("(^|\\.)"
 								+ Pattern.quote((String) importedMemberName
 										.getAttribute("name")) + "($|\\.)")) {
 					// the single static import has already been resolved
 					continue;
 				} else {
 					result.add(anImport);
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * This method may only be called if super type hierarchy is finished and
 	 * the direct super types already know all there (inherited) member types
 	 * 
 	 * @param mode
 	 * @param currentType
 	 * @param superType
 	 */
 	private void publishInheritedMembers(Mode mode, SpecificType currentType,
 			SpecificType superType) {
 		// work according to mode
 		if (superType == null) {
 			// handle java.lang.Object
 		} else {
 			Map<String, Type> visibleTypesOfSuperType = visibleTypes
 					.getMark(superType);
 			assert visibleTypesOfSuperType != null;
 			for (Entry<String, Type> entry : visibleTypesOfSuperType.entrySet()) {
 				if (isVisibleMember(currentType, entry.getValue())
 						&& !isHidden(currentType, entry.getValue())) {
 					// realizes implicit shadowing
 					addType(currentType, entry.getValue());
 				}
 			}
 			// TODO publish fields and methods
 		}
 	}
 
 	/**
 	 * if a type defines a member <code>m</code> and inherits a member
 	 * <code>m&apos;</code> with the same name, than <code>m&apos;</code> is
 	 * hidden
 	 * 
 	 * @param currentType
 	 * @param member
 	 * @return
 	 */
 	private boolean isHidden(SpecificType currentType, Member member) {
 		if (member.isInstanceOf(ConstructorDefinition.VC)) {
 			// a constructor can never be hidden
 			return false;
 		} else if (member.isInstanceOf(SpecificType.VC)) {
 			if (currentType.get_simpleName().equals(member.get_simpleName())) {
 				return true;
 			}
 		}
 		for (Member definedMember : currentType.get_members()) {
 			if (definedMember.isInstanceOf(SpecificType.VC)) {
 				if (member.isInstanceOf(SpecificType.VC)
 						&& definedMember.get_simpleName().equals(
 								member.get_simpleName())) {
 					return true;
 				}
 			} else if (member.isInstanceOf(Field.VC)
 					&& definedMember.isInstanceOf(Field.VC)) {
 				if (definedMember.get_simpleName().equals(
 						member.get_simpleName())) {
 					return true;
 				}
 			} else if (definedMember.isInstanceOf(MethodDeclaration.VC)) {
 				// TODO check if a method is hidden
 			}
 		}
 		return false;
 	}
 
 	private boolean isVisibleMember(JavaVertex context, Member member) {
 		SpecificType currentType = getCurrentSpecificType(context);
 
 		// http://docs.oracle.com/javase/specs/jls/se5.0/html/names.html#6.6.1
 		SpecificType containingType = member.get_containingType();
 		if (containingType != null) {
 			if (currentType != null && containingType == currentType) {
 				// each member is visible in the type, in which it is defined
 				return true;
 			}
 			// the member is defined in another type
 			for (Modifier modifier : member.get_modifiers()) {
 				if (modifier.get_type() == Modifiers.PUBLIC) {
 					return true;
 				} else if (modifier.get_type() == Modifiers.PRIVATE) {
 					return false;
 				} else if (modifier.get_type() == Modifiers.PROTECTED) {
 					if (getContainingPackage(context) == getContainingPackage(containingType)) {
 						// package visibility
 						return true;
 					}
 					if (currentType != null
 							&& member.isInstanceOf(SpecificType.VC)) {
 						return isSubtypeOf(currentType, containingType);
 					} else {
 						// TODO see
 						// http://docs.oracle.com/javase/specs/jls/se5.0/html/names.html#62587
 					}
 					return false;
 				}
 			}
 			// default visibility
 			if (currentType != null
 					&& (currentType.isInstanceOf(InterfaceDefinition.VC) || currentType
 							.isInstanceOf(AnnotationDefinition.VC))) {
 				// all members of interfaces and annotations are implicitly
 				// public
 				return true;
 			} else {
 				// package visibility
 				return getContainingPackage(context) == getContainingPackage(containingType);
 			}
 		} else {
 			assert member.isInstanceOf(SpecificType.VC);
 			// anonymous or local class or top level class
 			if (member.getDegree(DeclaresExternalDeclaration.EC,
 					EdgeDirection.OUT) > 0) {
 				// this is a top level class
 				for (Modifier modifier : member.get_modifiers()) {
 					if (modifier.get_type() == Modifiers.PUBLIC) {
 						return true;
 					}
 				}
 				// default visibility
 				if (getContainingPackage(context) == getContainingPackage(member)) {
 					// package visibility
 					return true;
 				}
 			} else {
 				// anonymous and local classes are never visible
 				return false;
 			}
 		}
 		return false;
 	}
 
 	private boolean isSubtypeOf(SpecificType currentType, SpecificType superType) {
 		if (currentType == superType) {
 			return true;
 		} else {
 			for (TypeSpecification directSuperTypeSpecification : getDefinedSuperTypes(currentType)) {
 				SpecificType directSuperType = null;
 				for (Type type : directSuperTypeSpecification
 						.get_definingType()) {
 					assert type.isInstanceOf(SpecificType.VC);
 					assert directSuperType == null;
 					directSuperType = (SpecificType) type;
 				}
 				if (directSuperType != null
						&& isSubtypeOf(directSuperType, superType)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private SpecificType getCurrentSpecificType(JavaVertex context) {
 		SpecificType currentType = null;
 		if (context.isInstanceOf(SpecificType.VC)) {
 			currentType = (SpecificType) context;
 		} else {
 			for (JavaVertex scope : determineScopes(context)) {
 				if (scope.isInstanceOf(SpecificType.VC)) {
 					currentType = (SpecificType) scope;
 					break;
 				}
 			}
 		}
 		return currentType;
 	}
 
 	private JavaPackage getContainingPackage(JavaVertex aVertex) {
 		List<JavaVertex> scopes = determineScopes(aVertex);
 		return (JavaPackage) scopes.get(scopes.size() - 1);
 	}
 
 	private Set<TypeSpecification> getDefinedSuperTypes(SpecificType currentType) {
 		Set<TypeSpecification> superTypes = new HashSet<TypeSpecification>();
 		for (TypeSpecification superType : currentType.get_superTypes()) {
 			superTypes.add(superType);
 		}
 		for (TypeSpecification superType : currentType
 				.get_implementedInterfaces()) {
 			superTypes.add(superType);
 		}
 		return superTypes;
 	}
 
 	private SpecificType getCurrentSpecificType(TypeSpecification superType) {
 		SpecificType subType;
 		Extends e = superType.getFirstExtendsIncidence();
 		if (e != null) {
 			subType = e.getAlpha();
 		} else {
 			Implements i = superType.getFirstImplementsIncidence();
 			assert i != null;
 			subType = i.getAlpha();
 		}
 		return subType;
 	}
 
 	private String getQualifiedName(TypeSpecification superType) {
 		String name = "";
 		if (superType.isInstanceOf(QualifiedName.VC)) {
 			name = ((QualifiedName) superType).get_fullyQualifiedName();
 		} else {
 			assert superType.isInstanceOf(EnclosedType.VC);
 			EnclosedType enclosedType = (EnclosedType) superType;
 			name = getQualifiedName(enclosedType.get_enclosingType()) + "."
 					+ getQualifiedName(enclosedType.get_enclosedType());
 		}
 		return name;
 	}
 
 	private Vertex blessVertex(TemporaryVertex tempVertex, VertexClass target) {
 		Position p = graphBuilder.getPositionsMap().get(tempVertex);
 		graphBuilder.getPositionsMap().remove(tempVertex);
 		Vertex newVertex = tempVertex.bless(target);
 		graphBuilder.getPositionsMap().put(newVertex, p);
 		return newVertex;
 	}
 
 	/*
 	 * visibility of parsed types
 	 */
 
 	private final Set<SpecificType> parsedSpecificTypes = new HashSet<SpecificType>();
 
 	private GraphMarker<Map<String, Type>> visibleTypes;
 
 	public void registerSpecificType(Vertex specificType) {
 		assert specificType.isInstanceOf(SpecificType.VC);
 		SpecificType specType = (SpecificType) specificType;
 		parsedSpecificTypes.add(specType);
 	}
 
 	private void addType(Vertex context, Type type) {
 		Map<String, Type> visibleSpecificTypes = visibleTypes.getMark(context);
 		if (visibleSpecificTypes == null) {
 			visibleSpecificTypes = new HashMap<String, Type>();
 			visibleTypes.mark(context, visibleSpecificTypes);
 		}
 		String simpleName = type.get_name();
 		visibleSpecificTypes.put(simpleName, type);
 	}
 
 	private void determineScopesOfParsedTypes() {
 		visibleTypes = new GraphMarker<Map<String, Type>>(
 				graphBuilder.getGraph());
 		for (SpecificType specificType : parsedSpecificTypes) {
 			// each type is visible in its own body
 			addType(specificType, specificType);
 			TranslationUnit translationUnit = specificType
 					.get_declaringTranslationUnit();
 			if (translationUnit != null) {
 				// specificType is a top level type
 				// a top level type is visible in the translation unit in which
 				// it is defined
 				addType(translationUnit, specificType);
 				// a top level type is visible in the package in which the
 				// translation unit is located in which it is defined
 				JavaPackage containingPackage = translationUnit
 						.get_containingPackage();
 				addType(containingPackage, specificType);
 			} else {
 				SpecificType containingType = specificType.get_containingType();
 				if (containingType != null) {
 					// specificType is a member type or a static nested type
 					// a member type is visible in the body of the type in which
 					// it is contained
 					addType(containingType, specificType);
 				} else {
 					TypeDefinitionStatement tdStatement = specificType
 							.get_definingStatement();
 					if (tdStatement != null) {
 						// specificType is a local class
 						// a local class is visible in all following statements
 						// of the same block or case or default block of a
 						// switch
 						ContainsStatement cs = (ContainsStatement) tdStatement
 								.getFirstContainsStatementIncidence(
 										EdgeDirection.IN).getReversedEdge();
 						cs = cs.getNextContainsStatementIncidence(EdgeDirection.OUT);
 						while (cs != null) {
 							Statement statement = cs.getOmega();
 							addType(statement, specificType);
 							cs = cs.getNextContainsStatementIncidence(EdgeDirection.OUT);
 						}
 					} else {
 						// specific type is an anonymous class
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * resolve imports
 	 */
 
 	/**
 	 * Each {@link TranslationUnit} which has at least one on demand type
 	 * import, is mapped on a {@link Map} <code>M</code> that maps the
 	 * corresponding {@link TypeImportOnDemandDefinition} on the imported
 	 * {@link JavaPackage} or {@link SpecificType}. If the latter could not be
 	 * identified, the corresponding {@link TypeImportOnDemandDefinition} is not
 	 * inserted in <code>M</code><br>
 	 * For each {@link TranslationUnit} the {@link Entry}
 	 * <code>null -&gt; {@link JavaPackage}{fullyQualifiedName = &quot;java.lang&quot;}</code>
 	 * is inserted into <code>M</code><br>
 	 * TODO the {@link TypeImportOnDemandDefinition}s have to be connected via
 	 * {@link DeclaresImportedType} with the {@link SpecificType} that has been
 	 * actually used in the current {@link TranslationUnit}.
 	 */
 	private GraphMarker<Map<TypeImportOnDemandDefinition, JavaVertex>> visibleOnDemandTypeImports;
 
 	/**
 	 * Each {@link TranslationUnit} which has at least one static on demand
 	 * import, is mapped on a {@link Map} <code>M</code> that maps the
 	 * corresponding {@link StaticImportOnDemandDefinition} on the imported
 	 * {@link SpecificType}. If the latter could not be identified, the
 	 * corresponding {@link StaticImportOnDemandDefinition} is not inserted in
 	 * <code>M</code><br>
 	 * TODO the {@link StaticImportOnDemandDefinition}s have to be connected via
 	 * {@link DeclaresImportedStaticMember} with the {@link SpecificType} that
 	 * has been actually used in the current {@link TranslationUnit}.
 	 */
 	private GraphMarker<Map<StaticImportOnDemandDefinition, SpecificType>> visibleStaticOnDemandImports;
 
 	private <K extends ImportDefinition, V extends JavaVertex> void addVisibleOnDemandImport(
 			GraphMarker<Map<K, V>> graphMarker,
 			TranslationUnit translationUnit, K importOnDemandDefinition,
 			V importedPackageOrType) {
 		Map<K, V> map = graphMarker.getMark(translationUnit);
 		if (map == null) {
 			map = new HashMap<K, V>();
 			graphMarker.mark(translationUnit, map);
 		}
 		if (!map.containsKey(importOnDemandDefinition)) {
 			map.put(importOnDemandDefinition, importedPackageOrType);
 		}
 	}
 
 	private void addVisibleOnDemandTypeImport(TranslationUnit translationUnit,
 			TypeImportOnDemandDefinition typeImportOnDemandDefinition,
 			JavaVertex importedPackageOrType) {
 		if (visibleOnDemandTypeImports == null) {
 			visibleOnDemandTypeImports = new GraphMarker<Map<TypeImportOnDemandDefinition, JavaVertex>>(
 					graphBuilder.getGraph());
 		}
 		addVisibleOnDemandImport(visibleOnDemandTypeImports, translationUnit,
 				typeImportOnDemandDefinition, importedPackageOrType);
 	}
 
 	private void resolveSingleTypeAndOnDemandTypeImports(Mode mode) {
 		// single type imports shadow on demand imports
 		for (SingleTypeImportDefinition anImport : singleTypeImports) {
 			QualifiedName nameOfType = anImport.get_definedImportName();
 			String simpleName = nameOfType.get_fullyQualifiedName().substring(
 					nameOfType.get_fullyQualifiedName().lastIndexOf(".") + 1);
 			SpecificType importedType = getTypeOrPackageWithName(mode,
 					nameOfType.get_fullyQualifiedName(), anImport, true,
 					SpecificType.class);
 			if (importedType == null) {
 				continue;
 			}
 			graphBuilder.createEdge(DeclaresImportedType.EC, anImport,
 					importedType);
 			// if there exists a type with the same simple name as the
 			// imported type in the current TranslationUnit, than this type
 			// is not imported
 			TranslationUnit translationUnit = anImport
 					.get_declaringTranslationUnit();
 			if (visibleTypes.getMark(translationUnit).containsKey(simpleName)) {
 				continue;
 			}
 			addType(translationUnit, importedType);
 		}
 
 		// imports on demand may not shadow anything
 		for (TypeImportOnDemandDefinition anImport : onDemandTypeImports) {
 			QualifiedName nameOfPackageOrType = anImport
 					.get_definedImportName();
 			if (nameOfPackageOrType.get_fullyQualifiedName()
 					.equals("java.lang")
 					|| nameOfPackageOrType.get_fullyQualifiedName().equals(
 							anImport.get_declaringTranslationUnit()
 									.get_containingPackage()
 									.get_fullyQualifiedName())) {
 				// import java.lang.*; is ignored
 				// on demand imports of the current package are ignored
 				continue;
 			}
 			JavaVertex importedPackageOrType = getTypeOrPackageWithName(mode,
 					nameOfPackageOrType.get_fullyQualifiedName(), anImport,
 					true, JavaVertex.class);
 			if (importedPackageOrType == null) {
 				continue;
 			}
 			TranslationUnit translationUnit = anImport
 					.get_declaringTranslationUnit();
 			addVisibleOnDemandTypeImport(translationUnit, anImport,
 					importedPackageOrType);
 		}
 
 		// each translation unit has an implicit "import java.lang.*;"
 		if (mode != Mode.LAZY) {
 			JavaPackage java = getPackageWithSimpleName(mode, "java", null);
 			JavaPackage javaLang = getPackageWithSimpleName(mode, "lang", java);
 			for (Vertex translationUnit : graphBuilder.getGraph().vertices(
 					TranslationUnit.VC)) {
 				addVisibleOnDemandTypeImport((TranslationUnit) translationUnit,
 						null, javaLang);
 			}
 		}
 	}
 
 	private void addVisibleStaticOnDemandImport(
 			TranslationUnit translationUnit,
 			StaticImportOnDemandDefinition staticImportOnDemandDefinition,
 			SpecificType importedType) {
 		if (visibleStaticOnDemandImports == null) {
 			visibleStaticOnDemandImports = new GraphMarker<Map<StaticImportOnDemandDefinition, SpecificType>>(
 					graphBuilder.getGraph());
 		}
 		addVisibleOnDemandImport(visibleStaticOnDemandImports, translationUnit,
 				staticImportOnDemandDefinition, importedType);
 	}
 
 	private void resolveStaticOnDemandImports(Mode mode) {
 		for (StaticImportOnDemandDefinition anImport : onDemandStaticImports) {
 			QualifiedName nameOfPackageOrType = anImport
 					.get_definedImportName();
 			SpecificType importedType = getTypeOrPackageWithName(mode,
 					nameOfPackageOrType.get_fullyQualifiedName(), anImport,
 					true, SpecificType.class);
 			if (importedType == null) {
 				continue;
 			}
 			TranslationUnit translationUnit = anImport
 					.get_declaringTranslationUnit();
 			addVisibleStaticOnDemandImport(translationUnit, anImport,
 					importedType);
 		}
 	}
 
 	private void resolveSingleStaticImports(Mode mode) {
 		while (!singleStaticImports.isEmpty()) {
 			SingleStaticImportDefinition anImport = singleStaticImports.get(0);
 			resolveSingleStaticImport(mode, anImport);
 		}
 	}
 
 	private void resolveSingleStaticImport(Mode mode,
 			SingleStaticImportDefinition anImport) {
 		TemporaryVertex importedMemberName = null;
 		for (Edge edge : anImport.incidences(EdgeDirection.OUT)) {
 			if (edge.isTemporary()
 					&& ((TemporaryEdge) edge).getPreliminaryType() == DeclaresImportedStaticMember.EC) {
 				importedMemberName = (TemporaryVertex) edge.getThat();
 			}
 		}
 		if (importedMemberName == null) {
 			// the current single static import has already been resolved
 			return;
 		}
 
 		QualifiedName typeName = anImport.get_definedImportName();
 		SpecificType importedType = getTypeOrPackageWithName(mode,
 				typeName.get_fullyQualifiedName(), anImport, true,
 				SpecificType.class);
 
 		Map<String, Type> memberTypes = visibleTypes.getMark(importedType);
 		Type importedMemberType = memberTypes == null ? null : memberTypes
 				.get(importedMemberName.getAttribute("name"));
 		boolean isInheritanceResolved = false;
 		if (importedMemberType == null
 				|| !(isStatic(importedMemberType) && isVisibleMember(anImport,
 						importedMemberType))) {
 			// no member type could be imported
 			// check if a member type could be imported which is inherited
 			for (TypeSpecification definedSuperType : getDefinedSuperTypes(importedType)) {
 				resolveSuperTypeHierarchy(mode, definedSuperType);
 			}
 			importedMemberType = memberTypes == null ? null : memberTypes
 					.get(importedMemberName.getAttribute("name"));
 			isInheritanceResolved = true;
 		}
 
 		TranslationUnit translationUnit = anImport
 				.get_declaringTranslationUnit();
 		if (importedMemberType != null && isStatic(importedMemberType)
 				&& isVisibleMember(anImport, importedMemberType)) {
 			if (!existsEdge(DeclaresImportedStaticMember.EC, anImport,
 					importedMemberType)) {
 				graphBuilder.createEdge(DeclaresImportedStaticMember.EC,
 						anImport, importedMemberType);
 			}
 			addType(translationUnit, importedMemberType);
 		}
 		if ((isInheritanceResolved || isInheritanceCompletlyResolved(importedType))
 				&& importedMemberName.isValid()) {
 			singleStaticImports.remove(anImport);
 			importedMemberName.delete();
 			if (importedMemberType != null) {
 				// TODO import static fields and methods
 			}
 		}
 	}
 
 	private boolean existsEdge(EdgeClass ec, JavaVertex alpha, JavaVertex omega) {
 		for (Edge edge : alpha.incidences(ec, EdgeDirection.OUT)) {
 			if (edge.getOmega() == omega) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isInheritanceCompletlyResolved(SpecificType specificType) {
 		for (TypeSpecification superType : getDefinedSuperTypes(specificType)) {
 			if (superType.getDegree(IsDefinedByType.EC, EdgeDirection.OUT) == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean isStatic(Member aMember) {
 		for (Modifier modifier : aMember.get_modifiers()) {
 			if (modifier.get_type() == Modifiers.STATIC) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * resolve names
 	 */
 
 	/**
 	 * @param mode
 	 * @param name
 	 * @param context
 	 * @param isCanonical
 	 *            if <code>false</code> then if a member type M is defined in
 	 *            type A and a type B inherits it from A, then M can have the
 	 *            qualifiedNames A.M and B.M. If set to <code>true</code> then
 	 *            the name always states its declaration A.M
 	 * @param expectedResultType
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	private <V extends JavaVertex> V getTypeOrPackageWithName(Mode mode,
 			String name, JavaVertex context, boolean isCanonical,
 			Class<V> expectedResultType) {
 		boolean isSimpleName = !name.contains(".");
 		boolean ensureType = Type.class.isAssignableFrom(expectedResultType);
 		JavaVertex result = null;
 		if (isSimpleName) {
 			if (ensureType) {
 				// this is a simple name reflection is already done
 				result = getTypeWithSimpleName(mode, name, context,
 						!isCanonical);
 			} else {
 				result = getTypeOrPackageWithSimpleName(mode, name, context,
 						!isCanonical);
 			}
 		} else {
 			// this is a qualified name
 			String[] nameParts = name.split("\\.");
 			assert nameParts.length > 1;
 			JavaVertex currentPackageOrType = null;
 			for (int i = 0; i < nameParts.length; i++) {
 				String namePart = nameParts[i];
 				boolean useOnDemandImports = !isCanonical && i == 0;
 				if ((ensureType && i == nameParts.length - 1)
 						|| (currentPackageOrType != null && currentPackageOrType
 								.isInstanceOf(SpecificType.VC))) {
 					// in the type name a.B.C.D, D has to be a type and
 					// everything following the first type B has to be a type
 					currentPackageOrType = getTypeWithSimpleName(mode,
 							namePart, currentPackageOrType, useOnDemandImports);
 				} else {
 					// a qualified name starts with ambiguous names (type or
 					// package)
 					currentPackageOrType = getTypeOrPackageWithSimpleName(mode,
 							namePart, i == 0 ? context : currentPackageOrType,
 							useOnDemandImports);
 				}
 				if (currentPackageOrType == null) {
 					// at least one part of the qualified name could not be
 					// resolved
 					break;
 				}
 			}
 			result = currentPackageOrType;
 		}
 		return (V) result;
 	}
 
 	private JavaVertex getTypeOrPackageWithSimpleName(Mode mode,
 			String simpleName, JavaVertex context, boolean useOnDemandImports) {
 		// http://docs.oracle.com/javase/specs/jls/se5.0/html/names.html#6.5.4
 		// 1. check if simpleName is a type
 		JavaVertex result = getTypeWithSimpleName(mode, simpleName, context,
 				useOnDemandImports);
 		if (result != null) {
 			return result;
 		}
 		// 2. check if simpleName is a package
 		result = getPackageWithSimpleName(mode, simpleName, context);
 		return result;
 	}
 
 	private JavaPackage getPackageWithSimpleName(Mode mode, String simpleName,
 			JavaVertex parentPackage) {
 		// this method is executed, when the parsing is finished
 		JavaPackage resultPackage = null;
 		if (parentPackage == null
 				|| !parentPackage.isInstanceOf(JavaPackage.VC)) {
 			// this is a top level package
 			resultPackage = (JavaPackage) packageNames.use(simpleName);
 		} else {
 			resultPackage = (JavaPackage) packageNames
 					.use(((JavaPackage) parentPackage).get_fullyQualifiedName()
 							+ "." + simpleName);
 		}
 		if (resultPackage == null) {
 			resultPackage = createJavaPackageViaReflection(mode, simpleName,
 					parentPackage);
 		}
 		return resultPackage;
 	}
 
 	private JavaPackage createJavaPackageViaReflection(Mode mode,
 			String simpleName, JavaVertex parentPackage) {
 		// TODO Auto-generated method stub
 		// returns null if no package is found
 		// parentPackage may be null in case of top level packages
 		// mark package with all visible types
 		// decide if in EAGER or COMPLETE
 		return null;
 	}
 
 	private Type getTypeWithSimpleName(Mode mode, String simpleName,
 			JavaVertex context, boolean useOnDemandImports) {
 		Type resultType = null;
 		if (context.isInstanceOf(Type.VC)) {
 			// determineScopes only resolves the enclosing Types
 			// the check if the simpleName is contained in the scope of the
 			// current context
 			Map<String, Type> visibleTypesInScope = visibleTypes
 					.getMark(context);
 			if (visibleTypesInScope != null) {
 				resultType = visibleTypesInScope.get(simpleName);
 				if (resultType != null) {
 					return resultType;
 				}
 			}
 		}
 		TranslationUnit translationUnit = null;
 		JavaPackage jPackage = null;
 		for (JavaVertex scope : determineScopes(context)) {
 			Map<String, Type> visibleTypesInScope = visibleTypes.getMark(scope);
 			if (visibleTypesInScope != null) {
 				resultType = visibleTypesInScope.get(simpleName);
 				if (resultType != null) {
 					// this enables shadowing
 					return resultType;
 				}
 			}
 			if (scope.isInstanceOf(TranslationUnit.VC)) {
 				translationUnit = (TranslationUnit) scope;
 			} else if (scope.isInstanceOf(JavaPackage.VC)) {
 				jPackage = (JavaPackage) scope;
 			}
 		}
 
 		if (resultType == null && jPackage != null && mode != Mode.LAZY) {
 			// TODO use reflection to find other classes in the same package
 			// e.g. if someone defines an own package java.lang then it is
 			// merged with the corresponding package from the JDK
 			// defined classes hide classes in the JDK
 			// existing files in visibleTypes may not be overwritten!
 		}
 
 		if (useOnDemandImports && translationUnit != null) {
 			resultType = findOnDemandTypeImport(mode, simpleName,
 					translationUnit);
 			if (resultType == null) {
 				Set<Member> importedMembers = findStaticOnDemandImport(mode,
 						simpleName, translationUnit);
 				for (Member member : importedMembers) {
 					if (member.isInstanceOf(SpecificType.VC)) {
 						assert resultType == null;
 						resultType = (SpecificType) member;
 						break;
 					}
 				}
 			}
 		}
 		return resultType;
 	}
 
 	private Set<Member> findStaticOnDemandImport(Mode mode, String simpleName,
 			TranslationUnit translationUnitWithImports) {
 		Set<Member> result = new HashSet<Member>();
 		if (visibleStaticOnDemandImports == null) {
 			return result;
 		}
 
 		Map<StaticImportOnDemandDefinition, SpecificType> registeredStaticOnDemandImports = visibleStaticOnDemandImports
 				.getMark(translationUnitWithImports);
 		if (registeredStaticOnDemandImports != null) {
 			for (Entry<StaticImportOnDemandDefinition, SpecificType> entry : registeredStaticOnDemandImports
 					.entrySet()) {
 				Map<String, Type> visibleTypesOfCurrentOnDemandImport = visibleTypes
 						.getMark(entry.getValue());
 				if (visibleTypesOfCurrentOnDemandImport != null) {
 					Member member = visibleTypesOfCurrentOnDemandImport
 							.get(simpleName);
 					if (isVisibleMember(translationUnitWithImports, member)
 							&& isStatic(member)) {
 						if (member != null && entry.getKey() != null) {
 							graphBuilder.createEdge(
 									DeclaresImportedStaticMember.EC,
 									entry.getKey(), member);
 						}
 						result.add(member);
 					}
 				}
 				// TODO add fields and methods
 				// TODO use reflection!!
 			}
 		}
 		return result;
 	}
 
 	private SpecificType findOnDemandTypeImport(Mode mode, String simpleName,
 			TranslationUnit translationUnitWithImports) {
 		if (visibleOnDemandTypeImports == null) {
 			return null;
 		}
 		SpecificType result = null;
 
 		Map<TypeImportOnDemandDefinition, JavaVertex> registeredOnDemandTypeImports = visibleOnDemandTypeImports
 				.getMark(translationUnitWithImports);
 		if (registeredOnDemandTypeImports != null) {
 			for (Entry<TypeImportOnDemandDefinition, JavaVertex> entry : registeredOnDemandTypeImports
 					.entrySet()) {
 				Map<String, Type> visibleTypesOfCurrentOnDemandImport = visibleTypes
 						.getMark(entry.getValue());
 				if (visibleTypesOfCurrentOnDemandImport != null) {
 					result = (SpecificType) visibleTypesOfCurrentOnDemandImport
 							.get(simpleName);
 					if (!isVisibleMember(translationUnitWithImports, result)) {
 						result = null;
 					}
 					if (result == null) {
 						// TODO use reflection!!
 					}
 					if (result != null && entry.getKey() != null) {
 						graphBuilder.createEdge(DeclaresImportedType.EC,
 								entry.getKey(), result);
 						break;
 					}
 				}
 
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Determines all the scopes of <code>context</code>:
 	 * <ul>
 	 * <li>The first containing Statement</li>
 	 * <li>The first enclosing MethodDeclaration or ConstructorDefinition (for
 	 * type parameter declarations)</li>
 	 * <li>The first enclosing(!) SpecificType</li>
 	 * <li>The TranslationUnit</li>
 	 * <li>The JavaPackage</li>
 	 * </ul>
 	 * 
 	 * @param context
 	 * @return all enclosing scopes, from inner most to outer most
 	 */
 	private List<JavaVertex> determineScopes(JavaVertex context) {
 		List<JavaVertex> result = new ArrayList<JavaVertex>();
 		AttributedEdge edgeToParent = context
 				.getFirstAttributedEdgeIncidence(EdgeDirection.IN);
 		JavaVertex currentVertex = context;
 		boolean skipFirstScope = context.isInstanceOf(SpecificType.VC);
 		while (edgeToParent != null) {
 			assert edgeToParent
 					.getNextAttributedEdgeIncidence(EdgeDirection.IN) == null;
 			if (!skipFirstScope && isAScope(currentVertex)) {
 				result.add(currentVertex);
 			}
 			skipFirstScope = false;
 			currentVertex = (JavaVertex) edgeToParent.getThat();
 			edgeToParent = currentVertex
 					.getFirstAttributedEdgeIncidence(EdgeDirection.IN);
 		}
 		if (currentVertex.isInstanceOf(ImportDefinition.VC)) {
 			currentVertex = ((ExternalDeclaration) currentVertex)
 					.get_declaringTranslationUnit();
 		} else if (currentVertex.isInstanceOf(SpecificType.VC)) {
 			if (!skipFirstScope) {
 				result.add(currentVertex);
 			}
 			currentVertex = ((ExternalDeclaration) currentVertex)
 					.get_declaringTranslationUnit();
 		}
 		if (currentVertex.isInstanceOf(TranslationUnit.VC)) {
 			result.add(currentVertex);
 			currentVertex = ((TranslationUnit) currentVertex)
 					.get_containingPackage();
 		}
 		assert currentVertex.isInstanceOf(JavaPackage.VC);
 		result.add(currentVertex);
 		return result;
 	}
 
 	private boolean isAScope(JavaVertex currentVertex) {
 		return (currentVertex.isInstanceOf(Statement.VC) && !currentVertex
 				.isInstanceOf(Expression.VC))
 				|| currentVertex.isInstanceOf(SpecificType.VC)
 				|| currentVertex.isInstanceOf(TranslationUnit.VC)
 				|| currentVertex.isInstanceOf(JavaPackage.VC)
 				|| currentVertex.isInstanceOf(MethodDeclaration.VC)
 				|| currentVertex.isInstanceOf(ConstructorDefinition.VC);
 	}
 }
