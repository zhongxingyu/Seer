 /*
  * Copyright Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the authors tag. All rights reserved.
  *
  * This copyrighted material is made available to anyone wishing to use,
  * modify, copy, or redistribute it subject to the terms and conditions
  * of the GNU General Public License version 2.
  * 
  * This particular file is subject to the "Classpath" exception as provided in the 
  * LICENSE file that accompanied this code.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT A
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License,
  * along with this distribution; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 
 package com.redhat.ceylon.compiler.java.codegen;
 
 import static com.redhat.ceylon.compiler.java.codegen.Naming.DeclNameFlag.QUALIFIED;
 import static com.sun.tools.javac.code.Flags.ABSTRACT;
 import static com.sun.tools.javac.code.Flags.FINAL;
 import static com.sun.tools.javac.code.Flags.INTERFACE;
 import static com.sun.tools.javac.code.Flags.PRIVATE;
 import static com.sun.tools.javac.code.Flags.PROTECTED;
 import static com.sun.tools.javac.code.Flags.PUBLIC;
 import static com.sun.tools.javac.code.Flags.STATIC;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.RetentionPolicy;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import com.redhat.ceylon.compiler.java.codegen.Naming.DeclNameFlag;
 import com.redhat.ceylon.compiler.java.codegen.Naming.Substitution;
 import com.redhat.ceylon.compiler.java.codegen.Naming.SyntheticName;
 import com.redhat.ceylon.compiler.loader.model.LazyInterface;
 import com.redhat.ceylon.compiler.typechecker.model.AnnotationArgument;
 import com.redhat.ceylon.compiler.typechecker.model.AnnotationInstantiation;
 import com.redhat.ceylon.compiler.typechecker.model.Class;
 import com.redhat.ceylon.compiler.typechecker.model.ClassOrInterface;
 import com.redhat.ceylon.compiler.typechecker.model.Declaration;
 import com.redhat.ceylon.compiler.typechecker.model.Functional;
 import com.redhat.ceylon.compiler.typechecker.model.FunctionalParameter;
 import com.redhat.ceylon.compiler.typechecker.model.Generic;
 import com.redhat.ceylon.compiler.typechecker.model.Interface;
 import com.redhat.ceylon.compiler.typechecker.model.Method;
 import com.redhat.ceylon.compiler.typechecker.model.MethodOrValue;
 import com.redhat.ceylon.compiler.typechecker.model.Package;
 import com.redhat.ceylon.compiler.typechecker.model.Parameter;
 import com.redhat.ceylon.compiler.typechecker.model.ParameterList;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedReference;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
 import com.redhat.ceylon.compiler.typechecker.model.ProducedTypedReference;
 import com.redhat.ceylon.compiler.typechecker.model.Scope;
 import com.redhat.ceylon.compiler.typechecker.model.Setter;
 import com.redhat.ceylon.compiler.typechecker.model.TypeAlias;
 import com.redhat.ceylon.compiler.typechecker.model.TypeDeclaration;
 import com.redhat.ceylon.compiler.typechecker.model.TypeParameter;
 import com.redhat.ceylon.compiler.typechecker.model.TypedDeclaration;
 import com.redhat.ceylon.compiler.typechecker.model.Value;
 import com.redhat.ceylon.compiler.typechecker.model.ValueParameter;
 import com.redhat.ceylon.compiler.typechecker.tree.NaturalVisitor;
 import com.redhat.ceylon.compiler.typechecker.tree.Node;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.AttributeDeclaration;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.AttributeGetterDefinition;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.AttributeSetterDefinition;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.BaseMemberExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.InvocationExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.LazySpecifierExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.MethodDeclaration;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.SequencedArgument;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.SpecifierExpression;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.SpecifierStatement;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.TypeParameterDeclaration;
 import com.redhat.ceylon.compiler.typechecker.tree.Tree.TypeParameterList;
 import com.redhat.ceylon.compiler.typechecker.tree.Visitor;
 import com.sun.tools.javac.code.Flags;
 import com.sun.tools.javac.tree.JCTree;
 import com.sun.tools.javac.tree.JCTree.JCAnnotation;
 import com.sun.tools.javac.tree.JCTree.JCBinary;
 import com.sun.tools.javac.tree.JCTree.JCBlock;
 import com.sun.tools.javac.tree.JCTree.JCExpression;
 import com.sun.tools.javac.tree.JCTree.JCIdent;
 import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
 import com.sun.tools.javac.tree.JCTree.JCNewClass;
 import com.sun.tools.javac.tree.JCTree.JCReturn;
 import com.sun.tools.javac.tree.JCTree.JCStatement;
 import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
 import com.sun.tools.javac.util.Context;
 import com.sun.tools.javac.util.List;
 import com.sun.tools.javac.util.ListBuffer;
 
 /**
  * This transformer deals with class/interface declarations
  */
 public class ClassTransformer extends AbstractTransformer {
 
     public static ClassTransformer getInstance(Context context) {
         ClassTransformer trans = context.get(ClassTransformer.class);
         if (trans == null) {
             trans = new ClassTransformer(context);
             context.put(ClassTransformer.class, trans);
         }
         return trans;
     }
 
     private ClassTransformer(Context context) {
         super(context);
     }
 
     public List<JCTree> transform(final Tree.ClassOrInterface def) {
         final ClassOrInterface model = def.getDeclarationModel();
         
         // we only create types for aliases so they can be imported with the model loader
         // and since we can't import local declarations let's just not create those types
         // in that case
         if(model.isAlias()
                 && Decl.isAncestorLocal(def))
             return List.nil();
         
         naming.noteDecl(model);
         final String javaClassName;
         String ceylonClassName = def.getIdentifier().getText();
         if (def instanceof Tree.AnyInterface) {
             javaClassName = naming.declName(model, QUALIFIED).replaceFirst(".*\\.", "");
         } else {
             javaClassName = Naming.quoteClassName(def.getIdentifier().getText());
         }
         ClassDefinitionBuilder instantiatorImplCb;
         ClassDefinitionBuilder instantiatorDeclCb;
         if (Decl.withinInterface(model)) {
             instantiatorImplCb = gen().current().getCompanionBuilder((Interface)model.getContainer());
             instantiatorDeclCb = gen().current();
         } else {
             instantiatorImplCb = gen().current();
             instantiatorDeclCb = null;
         }
         ClassDefinitionBuilder classBuilder = ClassDefinitionBuilder
                 .klass(this, javaClassName, ceylonClassName)
                 .forDefinition(def);
         TypeParameterList typeParameterList = def.getTypeParameterList();
 
         // Very special case for Anything
         if ("ceylon.language::Anything".equals(model.getQualifiedNameString())) {
             classBuilder.extending(null);
         }
         
         if (def instanceof Tree.AnyClass) {
             Tree.ParameterList paramList = ((Tree.AnyClass)def).getParameterList();
             Class cls = ((Tree.AnyClass)def).getDeclarationModel();
             // Member classes need a instantiator method
             boolean generateInstantiator = Strategy.generateInstantiator(cls);
             if(generateInstantiator){
                 generateInstantiators(cls, classBuilder, paramList, cls, instantiatorDeclCb, instantiatorImplCb, typeParameterList);
             }
             classBuilder.annotations(expressionGen().transform(def.getAnnotationList()));
             if(def instanceof Tree.ClassDefinition){
                 transformClass(def, (Class)model, classBuilder, paramList, generateInstantiator, cls, instantiatorDeclCb, instantiatorImplCb, typeParameterList);
             }else{
                 // class alias
                 classBuilder.constructorModifiers(PRIVATE);
                 classBuilder.annotations(makeAtAlias(model.getExtendedType()));
                 classBuilder.isAlias(true);
             }
         }
         
         if (def instanceof Tree.AnyInterface) {
             classBuilder.annotations(expressionGen().transform(def.getAnnotationList()));
             if(def instanceof Tree.InterfaceDefinition){
                 transformInterface(def, model, classBuilder, typeParameterList);
             }else{
                 // interface alias
                 classBuilder.annotations(makeAtAlias(model.getExtendedType()));
                 classBuilder.isAlias(true);
             }
         }
 
         // make sure we set the container in case we move it out
         addAtContainer(classBuilder, model);
         
         // Transform the class/interface members
         List<JCStatement> childDefs = visitClassOrInterfaceDefinition(def, classBuilder);
 
         // If it's a Class without initializer parameters...
         if (Strategy.generateMain(def)) {
             // ... then add a main() method
             classBuilder.method(makeMainForClass(model));
         }
         
         classBuilder
             .modelAnnotations(model.getAnnotations())
             .modifiers(transformClassDeclFlags(def))
             .satisfies(model.getSatisfiedTypes())
             .caseTypes(model.getCaseTypes(), model.getSelfType())
             .init(childDefs);
         
         // aliases don't need a $getType method
         if(!model.isAlias()){
             // only classes get a $getType method
             if(model instanceof Class)
                 classBuilder.addGetTypeMethod(model.getType());
             if(supportsReifiedAlias(model))
                 classBuilder.reifiedAlias(model.getType());
         }
         
         List<JCTree> result;
         if (Decl.isAnnotationClass(def)) {
             ListBuffer<JCTree> trees = ListBuffer.lb();
             trees.addAll(transformAnnotationClass((Tree.AnyClass)def));
             transformAnnotationClassConstructor((Tree.AnyClass)def, classBuilder);
             trees.addAll(classBuilder.build());
             result = trees.toList();
         } else {
             result = classBuilder.build();
         }
         
         return result;
     }
 
     /**
      * Generates a constructor for an annotation class which takes the 
      * annotation type as parameter.
      * @param classBuilder
      */
     private void transformAnnotationClassConstructor(
             Tree.AnyClass def,
             ClassDefinitionBuilder classBuilder) {
         Class klass = def.getDeclarationModel();
         MethodDefinitionBuilder annoCtor = classBuilder.addConstructor();
         annoCtor.ignoreModelAnnotations();
         annoCtor.modifiers(transformClassDeclFlags(klass));
         ParameterDefinitionBuilder pdb = ParameterDefinitionBuilder.instance(this, "anno");
         pdb.type(makeJavaType(klass.getType(), JT_ANNOTATION), null);
         annoCtor.parameter(pdb);
         
         // It's up to the caller to invoke value() on the Java annotation for a sequenced
         // annotation
         
         ListBuffer<JCExpression> args = ListBuffer.lb();
         for (Tree.Parameter parameter : def.getParameterList().getParameters()) {
             at(parameter);
             Parameter parameterModel = parameter.getDeclarationModel();
             JCExpression annoAttr = make().Apply(null, naming.makeQuotedQualIdent(naming.makeUnquotedIdent("anno"),
                     parameter.getDeclarationModel().getName()),
                     List.<JCExpression>nil());
             ProducedType parameterType = parameterModel.getType();
             JCExpression argExpr;
             if (typeFact().isIterableType(parameterType)
                     && !isCeylonString(parameterType)) {
                 // Convert from array to Sequential
                 ProducedType iteratedType = typeFact().getIteratedType(parameterType);
                 if (isCeylonBasicType(iteratedType)) {
                     argExpr = makeUtilInvocation("sequentialInstanceBoxed", 
                             List.<JCExpression>of(annoAttr), 
                             null);
                 } else if (Decl.isAnnotationClass(iteratedType.getDeclaration())) {
                     // Can't use Util.sequentialAnnotation becase we need to 'box'
                     // the Java annotations in their Ceylon annotation class
                     String wrapperMethod = "$annotationSequence";
                     argExpr = make().Apply(null, naming.makeUnquotedIdent(wrapperMethod), List.of(annoAttr));
                     ListBuffer<JCStatement> stmts = ListBuffer.lb();
                     SyntheticName array = naming.synthetic("array$");
                     SyntheticName sb = naming.synthetic("sb$");
                     SyntheticName element = naming.synthetic("element$");
                     stmts.append(makeVar(FINAL, sb, 
                             makeJavaType(typeFact().getSequenceBuilderType(iteratedType)),
                             make().NewClass(null, null, makeJavaType(typeFact().getSequenceBuilderType(iteratedType), JT_CLASS_NEW), List.<JCExpression>of(makeReifiedTypeArgument(iteratedType)), null)));
                     stmts.append(make().ForeachLoop(
                             makeVar(element, makeJavaType(iteratedType, JT_ANNOTATION), null), 
                             array.makeIdent(), 
                             make().Exec(make().Apply(null, naming.makeQualIdent(sb.makeIdent(), "append"), 
                                     List.<JCExpression>of(instantiateAnnotationClass(iteratedType, element.makeIdent()))))));
                     stmts.append(make().Return(make().Apply(null, naming.makeQualIdent(sb.makeIdent(), "getSequence"), List.<JCExpression>nil())));
                     classBuilder.method(
                             MethodDefinitionBuilder.systemMethod(this, wrapperMethod)
                                 .ignoreModelAnnotations()
                                 .modifiers(PRIVATE | STATIC)
                                 .resultType(null, makeJavaType(typeFact().getSequentialType(iteratedType)))
                                 .parameter(ParameterDefinitionBuilder.instance(this, array.getName())
                                         .type(make().TypeArray(makeJavaType(iteratedType, JT_ANNOTATION)), null))
                                 .body(stmts.toList()));
                 } else {
                     argExpr = makeUtilInvocation("sequentialInstance", 
                             List.<JCExpression>of(makeReifiedTypeArgument(iteratedType), annoAttr), 
                             List.<JCExpression>of(makeJavaType(iteratedType, JT_TYPE_ARGUMENT)));
                 }                
             } else if (Decl.isAnnotationClass(parameterType.getDeclaration())) {
                 argExpr = instantiateAnnotationClass(parameterType, annoAttr);
             } else {
                 argExpr = annoAttr;
             }
             args.add(argExpr);
         }
         annoCtor.body(at(def).Exec(
                 make().Apply(null,  naming.makeThis(), args.toList())));
     }
 
     private JCNewClass instantiateAnnotationClass(
             ProducedType annotationClass,
             JCExpression javaAnnotationInstance) {
         return make().NewClass(null, null, makeJavaType(annotationClass), 
                 List.<JCExpression>of(javaAnnotationInstance), null);
     }
 
     /**
      * Transforms an annotation class into a Java annotation type.
      * <pre>
      * annotation class Foo(String s, Integer i=1) {}
      * </pre>
      * is transformed into
      * <pre>
      * @Retention(RetentionPolicy.RUNTIME)
      * @interface Foo$annotation {
      *     String s();
      *     long i() default 1;
      * }
      * </pre>
      * If the annotation class is a subtype of SequencedAnnotation a wrapper
      * annotation is also generated:
      * <pre>
      * @Retention(RetentionPolicy.RUNTIME)
      * @interface Foo$annotations{
      *     Foo$annotation[] value();
      * }
      * </pre>
      */
     private List<JCTree> transformAnnotationClass(Tree.AnyClass def) {
         Class klass = (Class)def.getDeclarationModel();
         String annotationName = klass.getName()+"$annotation";
         ClassDefinitionBuilder annoBuilder = ClassDefinitionBuilder.klass(this, annotationName, null);
         annoBuilder.modifiers(Flags.ANNOTATION | Flags.INTERFACE | transformClassDeclFlags(def));
         annoBuilder.annotations(makeAtRetention(RetentionPolicy.RUNTIME));
         
         
         for (Tree.Parameter p : def.getParameterList().getParameters()) {
             Parameter parameterModel = p.getDeclarationModel();
             annoBuilder.method(makeAnnotationMethod(p));
         }
         List<JCTree> result;
         if (isSequencedAnnotation(klass)) {
             result = annoBuilder.annotations(makeAtAnnotationTarget()).build();
             String wrapperName = klass.getName()+"$annotations";
             ClassDefinitionBuilder sequencedBuilder = ClassDefinitionBuilder.klass(this, wrapperName, null);
             sequencedBuilder.modifiers(Flags.ANNOTATION | Flags.INTERFACE | transformClassDeclFlags(def));
             sequencedBuilder.annotations(makeAtRetention(RetentionPolicy.RUNTIME));
             MethodDefinitionBuilder mdb = MethodDefinitionBuilder.method2(this, "value");
             mdb.modifiers(PUBLIC | ABSTRACT);
             mdb.resultType(null, make().TypeArray(makeJavaType(klass.getType(), JT_ANNOTATION)));
             mdb.noBody();
             ClassDefinitionBuilder sequencedAnnotation = sequencedBuilder.method(mdb);
             sequencedAnnotation.annotations(transformAnnotationConstraints(klass));
             result = result.appendList(sequencedAnnotation.build());
             
         } else {
             result = annoBuilder.annotations(transformAnnotationConstraints(klass)).build();
         }
         
         return result;
     }
     
     private List<JCAnnotation> makeAtRetention(RetentionPolicy retentionPolicy) {
         return List.of(
                 make().Annotation(
                         make().Type(syms().retentionType), 
                         List.of(naming.makeQuotedQualIdent(make().Type(syms().retentionPolicyType), retentionPolicy.name()))));
     }
     
     /** 
      * Makes {@code @java.lang.annotation.Target(types)} 
      * where types are the given element types.
      */
     private List<JCAnnotation> makeAtAnnotationTarget(ElementType... types) {
         List<JCExpression> typeExprs = List.<JCExpression>nil();
         for (ElementType type : types) {
             typeExprs = typeExprs.prepend(naming.makeQuotedQualIdent(make().Type(syms().elementTypeType), type.name()));
         }
         return List.of(
                 make().Annotation(
                         make().Type(syms().targetType), 
                         List.<JCExpression>of(make().NewArray(null, null, typeExprs))));
     }
     
     private List<JCAnnotation> transformAnnotationConstraints(Class klass) {
         TypeDeclaration meta = (TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclaration("ConstrainedAnnotation");
         ProducedType constrainedType = klass.getType().getSupertype(meta);
         if (constrainedType != null) {
             ProducedType programElement = constrainedType.getTypeArgumentList().get(2);
             if (programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("ClassOrInterfaceDeclaration")).getType())
                     || programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("Package")).getType())
                     || programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("Module")).getType())) {
                 return makeAtAnnotationTarget(ElementType.TYPE);
             } else if (programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("AttributeDeclaration")).getType())
                     || programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("FunctionDeclaration")).getType())) {
                 return makeAtAnnotationTarget(ElementType.METHOD);
             } else if (programElement.isSubtypeOf(((TypeDeclaration)typeFact().getLanguageModuleMetamodelDeclarationDeclaration("Import")).getType())) {
                 return makeAtAnnotationTarget(ElementType.FIELD);
             }
         }
         return List.<JCAnnotation>nil();
     }
 
     private JCExpression transformAnnotationParameterDefault(Tree.Parameter p) {
         Tree.Expression defaultExpression = p.getDefaultArgument().getSpecifierExpression().getExpression();
         Tree.Term term = defaultExpression.getTerm();
         JCExpression defaultLiteral = null;
         if (term instanceof Tree.Literal
                 && !(term instanceof Tree.QuotedLiteral)) {
             defaultLiteral = expressionGen().transform((Tree.Literal)term);
         } else if (term instanceof Tree.BaseMemberExpression) {
             Tree.BaseMemberExpression bme = (Tree.BaseMemberExpression)term;
             Declaration decl = bme.getDeclaration();
             if (isBooleanTrue(decl)) {
                 defaultLiteral = makeBoolean(true);
             } else if (isBooleanFalse(decl)) {
                 defaultLiteral = makeBoolean(false);
             } else {
                 defaultLiteral = make().Literal(bme.getDeclaration().getQualifiedNameString());
             }
         } else if (term instanceof Tree.MemberOrTypeExpression) {
             Tree.MemberOrTypeExpression mte = (Tree.MemberOrTypeExpression)term;
             defaultLiteral = make().Literal(mte.getDeclaration().getQualifiedNameString());
         } else if (term instanceof Tree.SequenceEnumeration) {
             Tree.SequenceEnumeration seq = (Tree.SequenceEnumeration)term;
             SequencedArgument sequencedArgument = seq.getSequencedArgument();
             defaultLiteral = makeArrayInitializer(sequencedArgument);
         } else if (term instanceof Tree.Tuple) {
             Tree.Tuple seq = (Tree.Tuple)term;
             SequencedArgument sequencedArgument = seq.getSequencedArgument();
             defaultLiteral = makeArrayInitializer(sequencedArgument);
         } else if (term instanceof Tree.InvocationExpression) {
             // Allow invocations of annotation constructors, so long as they're
             // themselves being invoked with permitted arguments
             Tree.InvocationExpression invocation = (Tree.InvocationExpression)term;
             AnnotationInvocationVisitor visitor = new AnnotationInvocationVisitor(expressionGen(), invocation);
             defaultLiteral = visitor.transform(invocation);
         }
         if (defaultLiteral == null) {
             defaultLiteral = makeErroneous(p, "Unsupported defaulted parameter expression");
         }
         return defaultLiteral;
     }
 
     private JCExpression transformAnnotationMethodType(Tree.Parameter parameter) {
         ProducedType parameterType = parameter.getDeclarationModel().getType();
         JCExpression type = null;
         if (isScalarAnnotationParameter(parameterType)) {
             type = makeJavaType(parameterType, JT_ANNOTATION);
         } else if (isMetamodelReference(parameterType)) {
             type = make().Type(syms().stringType);
         } else if (typeFact().isIterableType(parameterType)) {
             ProducedType iteratedType = typeFact().getIteratedType(parameterType);
             if (isScalarAnnotationParameter(iteratedType)) {
                 JCExpression scalarType = makeJavaType(iteratedType, JT_ANNOTATION);
                 type = make().TypeArray(scalarType);
             } else if (isMetamodelReference(iteratedType)) {
                 JCExpression scalarType = make().Type(syms().stringType);
                 type = make().TypeArray(scalarType);
             }
         } else {
             type = makeErroneous(parameter, "Unsupported annotation parameter type");
         }
         return type;
     }
 
     private boolean isMetamodelReference(ProducedType parameterType) {
         // TODO Handle metamodel references properly
         return typeFact().getAnythingDeclaration().getType().isExactly(parameterType);
     }
 
 
     /**
      * Makes a new Array expression suitable for use in initializing a Java array
      * using as elements the positional arguments 
      * of the given {@link Tree.SequencedArgument} (which must 
      * be {@link Tree.ListedArgument}s).
      * 
      * <pre>
      *     Whatever[] w = <strong>{ listedArg1, listedArg2, ... }</strong>
      *     //             ^---------- this bit ---------^
      * </pre>
      * 
      * @param sequencedArgument
      * @return The array initializer expression
      */
     JCExpression makeArrayInitializer(
             Tree.SequencedArgument sequencedArgument) {
         JCExpression defaultLiteral;
         ListBuffer<JCExpression> elements = ListBuffer.<JCTree.JCExpression>lb();
         if (sequencedArgument != null) {
             for (Tree.PositionalArgument arg : sequencedArgument.getPositionalArguments()) {
                 if (arg instanceof Tree.ListedArgument) {
                     Tree.ListedArgument la = (Tree.ListedArgument)arg;
                     elements.append(expressionGen().transformExpression(la.getExpression().getTerm(), BoxingStrategy.UNBOXED, la.getExpression().getTypeModel()));
                 } else {
                     elements = null;
                     break;
                 }
             }
         }
         defaultLiteral = elements == null ? null : 
             make().NewArray(null, List.<JCExpression>nil(), elements.toList());
         return defaultLiteral;
     }
 
     private boolean isScalarAnnotationParameter(ProducedType parameterType) {
         return isCeylonBasicType(parameterType)
                 || Decl.isAnnotationClass(parameterType.getDeclaration());
     }
 
     private List<JCStatement> visitClassOrInterfaceDefinition(Node def, ClassDefinitionBuilder classBuilder) {
         // Transform the class/interface members
         CeylonVisitor visitor = gen().visitor;
         
         // don't visit if we have errors in the initialiser
         if(def instanceof Tree.ClassOrInterface && visitor.hasClassInitialiserErrors((Tree.ClassOrInterface)def))
             return List.nil();
         
         final ListBuffer<JCTree> prevDefs = visitor.defs;
         final boolean prevInInitializer = visitor.inInitializer;
         final ClassDefinitionBuilder prevClassBuilder = visitor.classBuilder;
         try {
             visitor.defs = new ListBuffer<JCTree>();
             visitor.inInitializer = true;
             visitor.classBuilder = classBuilder;
             
             def.visitChildren(visitor);
             return (List<JCStatement>)visitor.getResult().toList();
         } finally {
             visitor.classBuilder = prevClassBuilder;
             visitor.inInitializer = prevInInitializer;
             visitor.defs = prevDefs;
         }
     }
 
     private void generateInstantiators(Class model, ClassDefinitionBuilder classBuilder, Tree.ParameterList paramList,
             Class cls, ClassDefinitionBuilder instantiatorDeclCb, ClassDefinitionBuilder instantiatorImplCb, TypeParameterList typeParameterList) {
         // TODO Instantiators on companion classes
         classBuilder.constructorModifiers(PROTECTED);
         
         if (Decl.withinInterface(cls)) {
             MethodDefinitionBuilder instBuilder = MethodDefinitionBuilder.systemMethod(this, naming.getInstantiatorMethodName(cls));
             DefaultedArgumentOverload overloaded = new DefaultedArgumentInstantiator(daoAbstract, model);
             overloaded.makeOverload(
                     instBuilder,
                     paramList, null, typeParameterList);
             instantiatorDeclCb.method(instBuilder);
         }
         if (!Decl.withinInterface(cls)
                 || !model.isFormal()) {
             MethodDefinitionBuilder instBuilder = MethodDefinitionBuilder.systemMethod(this, naming.getInstantiatorMethodName(cls));
             DefaultedArgumentOverload overloaded = new DefaultedArgumentInstantiator(!cls.isFormal() ? daoThis : daoAbstract, model);
             overloaded.makeOverload(
                     instBuilder,
                     paramList, null, typeParameterList);
             instantiatorImplCb.method(instBuilder);
         }
     }
 
     private void transformClass(com.redhat.ceylon.compiler.typechecker.tree.Tree.ClassOrInterface def, Class model, ClassDefinitionBuilder classBuilder, 
             com.redhat.ceylon.compiler.typechecker.tree.Tree.ParameterList paramList, boolean generateInstantiator, 
             Class cls, ClassDefinitionBuilder instantiatorDeclCb, ClassDefinitionBuilder instantiatorImplCb, TypeParameterList typeParameterList) {
         // do reified type params first
         if(typeParameterList != null)
             classBuilder.reifiedTypeParameters(typeParameterList);
         
         for (Tree.Parameter param : paramList.getParameters()) {
             // Overloaded instantiators
             expressionGen().transform(param.getAnnotationList());
             Parameter paramModel = param.getDeclarationModel();
             Parameter refinedParam = CodegenUtil.findParamForDecl(
                     (TypedDeclaration)CodegenUtil.getTopmostRefinedDeclaration(param.getDeclarationModel()));
             at(param);
             classBuilder.parameter(param);
             if (paramModel.isDefaulted()
                     || paramModel.isSequenced()
                     || (generateInstantiator
                             && refinedParam != null
                             && (refinedParam.isDefaulted()
                                     || refinedParam.isSequenced()))) {
                 ClassDefinitionBuilder cbForDevaultValues;
                 ClassDefinitionBuilder cbForDevaultValuesDecls = null;
                 switch (Strategy.defaultParameterMethodOwner(model)) {
                 case STATIC:
                     cbForDevaultValues = classBuilder;
                     break;
                 case OUTER:
                     cbForDevaultValues = classBuilder.getContainingClassBuilder();
                     break;
                 case OUTER_COMPANION:
                     cbForDevaultValues = classBuilder.getContainingClassBuilder().getCompanionBuilder(Decl.getClassOrInterfaceContainer(model, true));
                     cbForDevaultValuesDecls = classBuilder.getContainingClassBuilder();
                     break;
                 default:
                     cbForDevaultValues = classBuilder.getCompanionBuilder(model);
                 }
                 if (generateInstantiator && refinedParam != paramModel) {}
                 else {
                     cbForDevaultValues.method(makeParamDefaultValueMethod(false, def.getDeclarationModel(), paramList, param, typeParameterList));
                     if (cbForDevaultValuesDecls != null) {
                         cbForDevaultValuesDecls.method(makeParamDefaultValueMethod(true, def.getDeclarationModel(), paramList, param, typeParameterList));
                     }
                 }
                 if (generateInstantiator) {
                     if (Decl.withinInterface(cls)) {
                         MethodDefinitionBuilder instBuilder = MethodDefinitionBuilder.systemMethod(this, naming.getInstantiatorMethodName(cls));
                         new DefaultedArgumentInstantiator(daoAbstract, model).makeOverload(instBuilder,
                                 paramList, param, typeParameterList);
                         instantiatorDeclCb.method(instBuilder);
                     }
                     MethodDefinitionBuilder instBuilder = MethodDefinitionBuilder.systemMethod(this, naming.getInstantiatorMethodName(cls));
                     new DefaultedArgumentInstantiator(daoThis, model).makeOverload(instBuilder,
                             paramList, param, typeParameterList);
                     instantiatorImplCb.method(instBuilder);
                 } else {
                     // Add overloaded constructors for defaulted parameter
                     MethodDefinitionBuilder overloadBuilder = classBuilder.addConstructor();
                     new DefaultedArgumentConstructor(daoThis, model).makeOverload(
                             overloadBuilder,
                             paramList, param, typeParameterList);
                 }
             }
         }
         satisfaction((Class)model, classBuilder);
         at(def);
         // Generate the inner members list for model loading
         addAtMembers(classBuilder, model);
         // Make sure top types satisfy reified type
         addReifiedTypeInterface(classBuilder, model);
     }
     
     private void addReifiedTypeInterface(ClassDefinitionBuilder classBuilder, ClassOrInterface model) {
         if(model.getExtendedType() == null || willEraseToObject(model.getExtendedType()) || !Decl.isCeylon(model.getExtendedTypeDeclaration()))
             classBuilder.reifiedType();
     }
 
     /**
      * Transforms the type of the given class parameter
      * @param decl
      * @return
      */
     JCExpression transformClassParameterType(Parameter decl) {
         Assert.that(decl.getContainer() instanceof Class);
         JCExpression type;
         MethodOrValue attr = CodegenUtil.findMethodOrValueForParam(decl);
         if (Decl.isValue(attr)) {
             ProducedTypedReference typedRef = getTypedReference(attr);
             ProducedTypedReference nonWideningTypedRef = nonWideningTypeDecl(typedRef);
             ProducedType paramType = nonWideningType(typedRef, nonWideningTypedRef);
             type = makeJavaType(nonWideningTypedRef.getDeclaration(), paramType, 0);
         } else {
             ProducedType paramType = decl.getType();
             type = makeJavaType(decl, paramType, 0);
         }
         return type;
     }
 
     private void transformInterface(com.redhat.ceylon.compiler.typechecker.tree.Tree.ClassOrInterface def, ClassOrInterface model, ClassDefinitionBuilder classBuilder, TypeParameterList typeParameterList) {
         //  Copy all the qualifying type's type parameters into the interface
         ProducedType type = model.getType().getQualifyingType();
         while (type != null) {
             java.util.List<TypeParameter> typeArguments = type.getDeclaration().getTypeParameters();
             if (typeArguments == null) {
                 continue;
             }
             for (TypeParameter typeArgument : typeArguments) {
                 classBuilder.typeParameter(typeArgument);
             }
             type = type.getQualifyingType();
         }
         
         classBuilder.method(makeCompanionAccessor((Interface)model, model.getType(), false));
         // Build the companion class
         buildCompanion(def, (Interface)model, classBuilder, typeParameterList);
         
         // Generate the inner members list for model loading
         addAtMembers(classBuilder, model);
     }
 
     private void addAtMembers(ClassDefinitionBuilder classBuilder, ClassOrInterface model) {
         List<JCExpression> members = List.nil();
         Package pkg = Decl.getPackageContainer(model);
         for(Declaration member : model.getMembers()){
             if(member instanceof ClassOrInterface == false
                     && member instanceof TypeAlias == false){
                 continue;
             }
             TypeDeclaration innerType = (TypeDeclaration) member;
             // figure out its java name (strip the leading dot)
             String javaClass = naming.declName(innerType, DeclNameFlag.QUALIFIED).substring(1);
             String ceylonName = member.getName();
             JCAnnotation atMember = makeAtMember(ceylonName, javaClass, pkg.getQualifiedNameString());
             members = members.prepend(atMember);
         }
         classBuilder.annotations(makeAtMembers(members));
     }
 
     private void addAtContainer(ClassDefinitionBuilder classBuilder, TypeDeclaration model) {
         Package pkg = Decl.getPackageContainer(model);
         Scope scope = model.getContainer();
         if(scope == null || scope instanceof ClassOrInterface == false)
             return;
         ClassOrInterface container = (ClassOrInterface) scope; 
         // figure out its java name (strip the leading dot)
         String javaClass = naming.declName(container, DeclNameFlag.QUALIFIED).substring(1);
         String ceylonName = container.getName();
         List<JCAnnotation> atContainer = makeAtContainer(ceylonName, javaClass, pkg.getQualifiedNameString());
         classBuilder.annotations(atContainer);
     }
 
     private void satisfaction(final Class model, ClassDefinitionBuilder classBuilder) {
         final java.util.List<ProducedType> satisfiedTypes = model.getSatisfiedTypes();
         Set<Interface> satisfiedInterfaces = new HashSet<Interface>();
         // start by saying that we already satisfied each interface from superclasses
         Class superClass = model.getExtendedTypeDeclaration();
         while(superClass != null){
             for(TypeDeclaration interfaceDecl : superClass.getSatisfiedTypeDeclarations()){
                 collectInterfaces((Interface) interfaceDecl, satisfiedInterfaces);
             }
             superClass = superClass.getExtendedTypeDeclaration();
         }
         // now satisfy each new interface
         for (ProducedType satisfiedType : satisfiedTypes) {
             TypeDeclaration decl = satisfiedType.getDeclaration();
             if (!(decl instanceof Interface)) {
                 continue;
             }
             concreteMembersFromSuperinterfaces((Class)model, classBuilder, satisfiedType, satisfiedInterfaces);
         }
         // now find the set of interfaces we implemented twice with more refined type parameters
         if(model.getExtendedTypeDeclaration() != null){
             // reuse that Set
             satisfiedInterfaces.clear();
             for(TypeDeclaration interfaceDecl : model.getSatisfiedTypeDeclarations()){
                 collectInterfaces((Interface) interfaceDecl, satisfiedInterfaces);
             }
             // now see if we refined them
             for(Interface iface : satisfiedInterfaces){
                 // skip those we can't do anything about
                 if(!supportsReified(iface))
                     continue;
                 ProducedType thisType = model.getType().getSupertype(iface);
                 ProducedType superClassType = model.getExtendedType().getSupertype(iface);
                 if(thisType != null
                         && superClassType != null
                         && !thisType.isExactly(superClassType)
                         && thisType.isSubtypeOf(superClassType)){
                     // we're refining it
                     classBuilder.refineReifiedType(thisType);
                 }
             }
         }
     }
 
     private void collectInterfaces(Interface interfaceDecl, Set<Interface> satisfiedInterfaces) {
         if(satisfiedInterfaces.add(interfaceDecl)){
             for(TypeDeclaration newInterfaceDecl : interfaceDecl.getSatisfiedTypeDeclarations()){
                 collectInterfaces((Interface) newInterfaceDecl, satisfiedInterfaces);
             }
         }
     }
 
     /**
      * Generates companion fields ($Foo$impl) and methods
      */
     private void concreteMembersFromSuperinterfaces(final Class model,
             ClassDefinitionBuilder classBuilder, 
             ProducedType satisfiedType, Set<Interface> satisfiedInterfaces) {
         satisfiedType = satisfiedType.resolveAliases();
         Interface iface = (Interface)satisfiedType.getDeclaration();
         if (satisfiedInterfaces.contains(iface)
                 || iface.getType().isExactly(typeFact().getIdentifiableDeclaration().getType())) {
             return;
         }
      
         // If there is no $impl (e.g. implementing a Java interface) 
         // then don't instantiate it...
         if (hasImpl(iface)) {
             // ... otherwise for each satisfied interface, 
             // instantiate an instance of the 
             // companion class in the constructor and assign it to a
             // $Interface$impl field
             transformInstantiateCompanions(classBuilder,
                     model, iface, satisfiedType);
         }
         
         if(!Decl.isCeylon(iface)){
             // let's not try to implement CMI for Java interfaces
             return;
         }
         
         // For each super interface
         for (Declaration member : iface.getMembers()) {
             
             if (member instanceof Class
                     && Strategy.generateInstantiator(member)
                     && model.getDirectMember(member.getName(), null, false) == null) {
                 // instantiator method implementation
                 Class klass = (Class)member;
                 generateInstantiatorDelegate(classBuilder, satisfiedType,
                         iface, klass);
             } 
             
             if (Strategy.onlyOnCompanion(member)) {
                 // non-shared interface methods don't need implementing
                 // (they're just private methods on the $impl)
                 continue;
             }
             if (member instanceof Method) {
                 Method method = (Method)member;
                 final ProducedTypedReference typedMember = satisfiedType.getTypedMember(method, Collections.<ProducedType>emptyList());
                 final ProducedTypedReference refinedTypedMember = model.getType().getTypedMember((TypedDeclaration)model.getMember(method.getName(), null, false), Collections.<ProducedType>emptyList());
                 Method subMethod = (Method)model.getMember(method.getName(), null, false);
                 final ProducedType refinedType = subMethod.getType();
                 final java.util.List<TypeParameter> typeParameters = subMethod.getTypeParameters();
                 final java.util.List<Parameter> parameters = subMethod.getParameterLists().get(0).getParameters();
                 if (!satisfiedInterfaces.contains((Interface)method.getContainer())) {
                     
                     for (Parameter param : parameters) {
                         if (param.isDefaulted()
                                 || param.isSequenced()) {
                             final ProducedTypedReference typedParameter = refinedTypedMember.getTypedParameter(param);
                             // If that method has a defaulted parameter, 
                             // we need to generate a default value method
                             // which also delegates to the $impl
                             final MethodDefinitionBuilder defaultValueDelegate = makeDelegateToCompanion(iface,
                                     typedParameter,
                                     PUBLIC | FINAL, 
                                     typeParameters, 
                                     typedParameter.getType(), 
                                     Naming.getDefaultedParamMethodName(method, param), 
                                     parameters.subList(0, parameters.indexOf(param)),
                                     param.getTypeErased());
                             classBuilder.method(defaultValueDelegate);
                             
                             final MethodDefinitionBuilder overload = makeDelegateToCompanion(iface,
                                     refinedTypedMember,
                                     PUBLIC | (method.isDefault() ? 0 : FINAL), 
                                     typeParameters,  
                                     typedMember.getType(), 
                                     naming.selector(method), 
                                     parameters.subList(0, parameters.indexOf(param)),
                                     ((Method) member).getTypeErased());
                             classBuilder.method(overload);
                         }
                     }
                 }
                 // if it has the *most refined* default concrete member, 
                 // then generate a method on the class
                 // delegating to the $impl instance
                 if (needsCompanionDelegate(model, member)) {
                     
                     final MethodDefinitionBuilder concreteMemberDelegate = makeDelegateToCompanion(iface,
                             typedMember,
                             PUBLIC | (method.isDefault() ? 0 : FINAL),
                             method.getTypeParameters(), 
                             method.getType(), 
                             naming.selector(method), 
                             method.getParameterLists().get(0).getParameters(),
                             ((Method) member).getTypeErased());
                     classBuilder.method(concreteMemberDelegate);
                      
                 }
             } else if (member instanceof Value
                     || member instanceof Setter) {
                 TypedDeclaration attr = (TypedDeclaration)member;
                 final ProducedTypedReference typedMember = satisfiedType.getTypedMember(attr, null);
                 if (needsCompanionDelegate(model, member)) {
                     if (member instanceof Value) {
                         final MethodDefinitionBuilder getterDelegate = makeDelegateToCompanion(iface, 
                                 typedMember,
                                 PUBLIC | (attr.isDefault() ? 0 : FINAL), 
                                 Collections.<TypeParameter>emptyList(), 
                                 typedMember.getType(), 
                                 Naming.getGetterName(attr), 
                                 Collections.<Parameter>emptyList(),
                                 attr.getTypeErased());
                         classBuilder.method(getterDelegate);
                     }
                     if (member instanceof Setter) { 
                         final MethodDefinitionBuilder setterDelegate = makeDelegateToCompanion(iface, 
                                 typedMember,
                                 PUBLIC | (((Setter)member).getGetter().isDefault() ? 0 : FINAL), 
                                 Collections.<TypeParameter>emptyList(), 
                                 typeFact().getAnythingDeclaration().getType(), 
                                 Naming.getSetterName(attr), 
                                 Collections.<Parameter>singletonList(((Setter)member).getParameter()),
                                 ((Setter) member).getTypeErased());
                         classBuilder.method(setterDelegate);
                     }
                     if (Decl.isValue(member) 
                             && ((Value)attr).isVariable()) {
                         // I don't *think* this can happen because although a 
                         // variable Value can be declared on an interface it 
                         // will need to we refined as a Getter+Setter on a 
                         // subinterface in order for there to be a method in a 
                         // $impl to delegate to
                         throw new RuntimeException();
                     }
                 }
             } else if (needsCompanionDelegate(model, member)) {
                 log.error("ceylon", "Unhandled concrete interface member " + member.getQualifiedNameString() + " " + member.getClass());
             }
         }
         
         // Add $impl instances for the whole interface hierarchy
         satisfiedInterfaces.add(iface);
         for (ProducedType sat : iface.getSatisfiedTypes()) {
             sat = satisfiedType.getSupertype(sat.getDeclaration());
             concreteMembersFromSuperinterfaces(model, classBuilder, sat, satisfiedInterfaces);
         }
         
     }
 
     private void generateInstantiatorDelegate(
             ClassDefinitionBuilder classBuilder, ProducedType satisfiedType,
             Interface iface, Class klass) {
         ProducedType typeMember = satisfiedType.getTypeMember(klass, Collections.<ProducedType>emptyList());
         java.util.List<TypeParameter> typeParameters = klass.getTypeParameters();
         java.util.List<Parameter> parameters = klass.getParameterLists().get(0).getParameters();
         
         String instantiatorMethodName = naming.getInstantiatorMethodName(klass);
         for (Parameter param : parameters) {
             if (param.isDefaulted()
                     || param.isSequenced()) {
                 final ProducedTypedReference typedParameter = typeMember.getTypedParameter(param);
                 // If that method has a defaulted parameter, 
                 // we need to generate a default value method
                 // which also delegates to the $impl
                 final MethodDefinitionBuilder defaultValueDelegate = makeDelegateToCompanion(iface,
                         typedParameter,
                         PUBLIC | FINAL, 
                         typeParameters, 
                         typedParameter.getType(),
                         Naming.getDefaultedParamMethodName(klass, param), 
                         parameters.subList(0, parameters.indexOf(param)),
                         param.getTypeErased());
                 classBuilder.method(defaultValueDelegate);
                 
                 final MethodDefinitionBuilder overload = makeDelegateToCompanion(iface,
                         typeMember,
                         PUBLIC | FINAL, 
                         typeParameters,  
                         typeMember.getType(), 
                         instantiatorMethodName, 
                         parameters.subList(0, parameters.indexOf(param)),
                         false);
                 classBuilder.method(overload);
             }
         }
         final MethodDefinitionBuilder overload = makeDelegateToCompanion(iface,
                 typeMember,
                 PUBLIC | FINAL, 
                 typeParameters,  
                 typeMember.getType(), 
                 instantiatorMethodName, 
                 parameters,
                 false);
         classBuilder.method(overload);
     }
 
     private boolean needsRawification(ProducedType type) {
         final Iterator<TypeParameter> refinedTypeParameters = type.getDeclaration().getTypeParameters().iterator();
         boolean rawifyParametersAndResults = false;
         for (ProducedType typeArg : type.getTypeArgumentList()) {
             final TypeParameter typeParam = refinedTypeParameters.next();
             if (typeParam.getSatisfiedTypes().isEmpty()
                     && (typeFact().isIntersection(typeArg) || typeFact().isUnion(typeArg))) {
                 // Use the same hack that makeJavaType() does when handling iterables
                 if (!willEraseToSequential(typeArg)) {
                     rawifyParametersAndResults = true;
                     break;
                 }
             }
         }
         return rawifyParametersAndResults;
     }
 
     private boolean needsCompanionDelegate(final Class model, Declaration member) {
         final boolean mostRefined;
         Declaration m = model.getMember(member.getName(), null, false);
         if (member instanceof Setter && Decl.isGetter(m)) {
             mostRefined = member.equals(((Value)m).getSetter());
         } else {
             mostRefined = member.equals(m);
         }
         return mostRefined
                 && (member.isDefault() || !member.isFormal());
     }
 
     /**
      * Generates a method which delegates to the companion instance $Foo$impl
      */
     private MethodDefinitionBuilder makeDelegateToCompanion(Interface iface,
             ProducedReference typedMember, final long mods,
             final java.util.List<TypeParameter> typeParameters,
             final ProducedType methodType,
             final String methodName, final java.util.List<Parameter> parameters, 
             boolean typeErased) {
         final MethodDefinitionBuilder concreteWrapper = MethodDefinitionBuilder.systemMethod(gen(), methodName);
         concreteWrapper.modifiers(mods);
         concreteWrapper.ignoreModelAnnotations();
         concreteWrapper.isOverride(true);
         if(typeParameters != null)
             concreteWrapper.reifiedTypeParametersFromModel(typeParameters);
         for (TypeParameter tp : typeParameters) {
             concreteWrapper.typeParameter(tp);
         }
         boolean explicitReturn = false;
         Declaration member = typedMember.getDeclaration();
         ProducedType returnType = null;
         if (!isAnything(methodType) 
                 || ((member instanceof Method || member instanceof Value) && !Decl.isUnboxedVoid(member)) 
                 || (member instanceof Method && Strategy.useBoxedVoid((Method)member))) {
             explicitReturn = true;
             if (typedMember instanceof ProducedTypedReference) {
                 ProducedTypedReference typedRef = (ProducedTypedReference) typedMember;
                 concreteWrapper.resultTypeNonWidening(typedRef, typedMember.getType(), 0);
                 // FIXME: this is redundant with what we computed in the previous line in concreteWrapper.resultTypeNonWidening
                 ProducedTypedReference nonWideningTypedRef = gen().nonWideningTypeDecl(typedRef);
                 returnType = gen().nonWideningType(typedRef, nonWideningTypedRef);
             } else {
                 concreteWrapper.resultType(null, makeJavaType((ProducedType)typedMember));
                 returnType = (ProducedType) typedMember;
             }
         }
         
         ListBuffer<JCExpression> arguments = ListBuffer.<JCExpression>lb();
         if(typeParameters != null){
             for(TypeParameter tp : typeParameters){
                 arguments.add(naming.makeUnquotedIdent(naming.getTypeArgumentDescriptorName(tp.getName())));
             }
         }
         for (Parameter param : parameters) {
             final ProducedTypedReference typedParameter = typedMember.getTypedParameter(param);
             ProducedType type;
             // if the supertype method itself got erased to Object, we can't do better than this
             if(gen().willEraseToObject(param.getType()) && !gen().willEraseToBestBounds(param))
                 type = typeFact().getObjectDeclaration().getType();
             else
                 type = typedParameter.getType();
             concreteWrapper.parameter(param, type, FINAL, 0, true);
             arguments.add(naming.makeName(param, Naming.NA_MEMBER));
         }
         JCExpression expr = make().Apply(
                 null,  // TODO Type args
                 makeSelect(getCompanionFieldName(iface), methodName),
                 arguments.toList());
         
         if (!explicitReturn) {
             concreteWrapper.body(gen().make().Exec(expr));
         } else {
             // deal with erasure and stuff
             BoxingStrategy boxingStrategy;
             boolean exprBoxed;
             if(member instanceof TypedDeclaration){
                 TypedDeclaration typedDecl = (TypedDeclaration) member;
                 exprBoxed = !CodegenUtil.isUnBoxed(typedDecl);
                 boxingStrategy = CodegenUtil.getBoxingStrategy(typedDecl);
             }else{
                 // must be a class or interface
                 exprBoxed = true;
                 boxingStrategy = BoxingStrategy.UNBOXED;
             }
             expr = gen().expressionGen().applyErasureAndBoxing(expr, methodType, typeErased, 
                                                                exprBoxed, boxingStrategy,
                                                                returnType, 0);
             concreteWrapper.body(gen().make().Return(expr));
         }
         return concreteWrapper;
     }
 
     private Boolean hasImpl(Interface iface) {
         if (gen().willEraseToObject(iface.getType())) {
             return false;
         }
         if (iface instanceof LazyInterface) {
             return ((LazyInterface)iface).isCeylon();
         }
         return true;
     }
 
     private void transformInstantiateCompanions(
             ClassDefinitionBuilder classBuilder, 
             Class model, Interface iface, ProducedType satisfiedType) {
         at(null);
         List<JCExpression> state = List.nil();
         
         // pass all reified type info to the constructor
         for(JCExpression t : makeReifiedTypeArguments(satisfiedType)){
             state = state.append(t);
         }
 
         // pass the instance of this
         state = state.append( expressionGen().applyErasureAndBoxing(naming.makeThis(), 
                 model.getType(), false, true, BoxingStrategy.BOXED, 
                 satisfiedType, ExpressionTransformer.EXPR_FOR_COMPANION));
 
         JCExpression containerInstance = null;
         JCExpression ifaceImplType = null;
         if(!Decl.isToplevel(iface)){
             // if it's a member type we need to qualify the new instance with its $impl container
             ClassOrInterface interfaceContainer = Decl.getClassOrInterfaceContainer(iface, false);
             if(interfaceContainer instanceof Interface){
                 ClassOrInterface modelContainer = model;
                 while((modelContainer = Decl.getClassOrInterfaceContainer(modelContainer, false)) != null
                         && modelContainer.getType().getSupertype(interfaceContainer) == null){
                     // keep searching
                 }
                 Assert.that(modelContainer != null, "Could not find container that satisfies interface "
                         + iface.getQualifiedNameString() + " to find qualifying instance for companion instance for "
                         + model.getQualifiedNameString());
                 // if it's an interface we just qualify it properly
                 if(modelContainer instanceof Interface){
                     JCExpression containerType = makeJavaType(modelContainer.getType(), JT_COMPANION | JT_SATISFIES);
                     containerInstance = makeSelect(containerType, "this");
                     ifaceImplType = makeJavaType(satisfiedType, JT_COMPANION | JT_SATISFIES | JT_NON_QUALIFIED);
                 }else{
                     // it's a class: find the right field used for the interface container impl
                     String containerFieldName = getCompanionFieldName((Interface)interfaceContainer);
                     JCExpression containerType = makeJavaType(modelContainer.getType(), JT_SATISFIES);
                     containerInstance = makeSelect(makeSelect(containerType, "this"), containerFieldName);
                     ifaceImplType = makeJavaType(satisfiedType, JT_COMPANION | JT_SATISFIES | JT_NON_QUALIFIED);
                 }
             }
         }
         if(ifaceImplType == null){
             ifaceImplType = makeJavaType(satisfiedType, JT_COMPANION | JT_SATISFIES);
         }
         JCExpression newInstance = make().NewClass(containerInstance, 
                 null,
                 ifaceImplType,
                 state,
                 null);
         
         final String fieldName = getCompanionFieldName(iface);
         classBuilder.init(make().Exec(make().Assign(
                 makeSelect("this", fieldName),// TODO Use qualified name for quoting? 
                 newInstance)));
         
         classBuilder.field(PROTECTED | FINAL, fieldName, 
                 makeJavaType(satisfiedType, AbstractTransformer.JT_COMPANION | JT_SATISFIES), null, false,
                 makeAtIgnore());
 
         classBuilder.method(makeCompanionAccessor(iface, satisfiedType, true));
     }
     
     private MethodDefinitionBuilder makeCompanionAccessor(Interface iface, ProducedType satisfiedType, boolean forImplementor) {
         MethodDefinitionBuilder thisMethod = MethodDefinitionBuilder.systemMethod(
                 this, getCompanionAccessorName(iface));
         thisMethod.noModelAnnotations();
         if (!forImplementor && Decl.isAncestorLocal(iface)) {
             // For a local interface the return type cannot be a local
             // companion class, because that won't be visible at the 
             // top level, so use Object instead
             thisMethod.resultType(null, make().Type(syms().objectType));
         } else {
             thisMethod.resultType(null, makeJavaType(satisfiedType, JT_COMPANION | JT_SATISFIES));
         }
         if (forImplementor) {
             thisMethod.isOverride(true);
         } else {
             thisMethod.ignoreModelAnnotations();
         }
         thisMethod.modifiers(PUBLIC);
         if (forImplementor) {
             thisMethod.body(make().Return(naming.makeCompanionFieldName(iface)));
         } else {
             thisMethod.noBody();
         }
         return thisMethod;
     }
 
     private void buildCompanion(final Tree.ClassOrInterface def,
             final Interface model, ClassDefinitionBuilder classBuilder,
             TypeParameterList typeParameterList) {
         at(def);
         // Give the $impl companion a $this field...
         ClassDefinitionBuilder companionBuilder = classBuilder.getCompanionBuilder(model);
 
         // make sure we get fields and init code for reified params
         if(typeParameterList != null)
             companionBuilder.reifiedTypeParameters(typeParameterList);
         ProducedType thisType = model.getType();
         companionBuilder.field(PRIVATE | FINAL, 
                 "$this", 
                 makeJavaType(thisType), 
                 null, false);
         MethodDefinitionBuilder ctor = companionBuilder.addConstructorWithInitCode();
         ctor.noModelAnnotations();
         if(typeParameterList != null)
             ctor.reifiedTypeParameters(typeParameterList.getTypeParameterDeclarations());
         ctor.modifiers(model.isShared() ? PUBLIC : 0);
         ParameterDefinitionBuilder pdb = ParameterDefinitionBuilder.instance(this, "$this");
         pdb.type(makeJavaType(thisType), null);
         // ...initialize the $this field from a ctor parameter...
         ctor.parameter(pdb);
         ListBuffer<JCStatement> bodyStatements = ListBuffer.<JCStatement>of(
                 make().Exec(
                         make().Assign(
                                 makeSelect(naming.makeThis(), "$this"), 
                                 naming.makeQuotedThis())));
         ctor.body(bodyStatements.toList());
         
         if(typeParameterList != null)
             companionBuilder.addRefineReifiedTypeParametersMethod(typeParameterList);
     }
 
     public List<JCStatement> transformRefinementSpecifierStatement(SpecifierStatement op, ClassDefinitionBuilder classBuilder) {
         List<JCStatement> result = List.<JCStatement>nil();
         // Check if this is a shortcut form of formal attribute refinement
         if (op.getRefinement()) {
             Tree.Term baseMemberTerm = op.getBaseMemberExpression();
             if(baseMemberTerm instanceof Tree.ParameterizedExpression)
                 baseMemberTerm = ((Tree.ParameterizedExpression)baseMemberTerm).getPrimary();
             
             Tree.BaseMemberExpression expr = (BaseMemberExpression) baseMemberTerm;
             Declaration decl = expr.getDeclaration();
             
             if (Decl.isValue(decl)) {
                 // Now build a "fake" declaration for the attribute
                 Tree.AttributeDeclaration attrDecl = new Tree.AttributeDeclaration(null);
                 attrDecl.setDeclarationModel((Value)decl);
                 attrDecl.setIdentifier(expr.getIdentifier());
                 attrDecl.setScope(op.getScope());
                 attrDecl.setSpecifierOrInitializerExpression(op.getSpecifierExpression());
                 
                 // Make sure the boxing information is set correctly
                 BoxingDeclarationVisitor v = new CompilerBoxingDeclarationVisitor(this);
                 v.visit(attrDecl);
                 
                 // Generate the attribute
                 transform(attrDecl, classBuilder);
             } else if (decl instanceof Method) {
                 // Now build a "fake" declaration for the method
                 Tree.MethodDeclaration methDecl = new Tree.MethodDeclaration(null);
                 Method m = (Method)decl;
                 methDecl.setDeclarationModel(m);
                 methDecl.setIdentifier(expr.getIdentifier());
                 methDecl.setScope(op.getScope());
                 
                 Tree.SpecifierExpression specifierExpression = op.getSpecifierExpression();
                 methDecl.setSpecifierExpression(specifierExpression);
                 
                 if(specifierExpression instanceof Tree.LazySpecifierExpression == false){
                     Tree.Expression expression = specifierExpression.getExpression();
                     Tree.Term expressionTerm = Decl.unwrapExpressionsUntilTerm(expression);
                     // we can optimise lambdas and static method calls
                     if(!CodegenUtil.canOptimiseMethodSpecifier(expressionTerm, m)){
                         // we need a field to save the callable value
                         String name = naming.getMethodSpecifierAttributeName(m);
                         JCExpression specifierType = makeJavaType(expression.getTypeModel());
                         JCExpression specifier = expressionGen().transformExpression(expression);
                         classBuilder.field(PRIVATE | FINAL, name, specifierType, specifier, false);
                     }
                 }
 
                 // copy from formal declaration
                 for (ParameterList pl : m.getParameterLists()) {
                     Tree.ParameterList tpl = new Tree.ParameterList(null);
                     for (Parameter p : pl.getParameters()) {
                         Tree.Parameter tp = null;
                         if (p instanceof ValueParameter) {
                             Tree.ValueParameterDeclaration tvpd = new Tree.ValueParameterDeclaration(null);
                             tvpd.setDeclarationModel((ValueParameter)p);
                             tp = tvpd;
                         } else if (p instanceof FunctionalParameter) {
                             Tree.FunctionalParameterDeclaration tfpd = new Tree.FunctionalParameterDeclaration(null);
                             tfpd.setDeclarationModel((FunctionalParameter)p);
                             tp = tfpd;
                         }
                         tp.setScope(p.getContainer());
                         tp.setIdentifier(makeIdentifier(p.getName()));
                         tpl.addParameter(tp);
                     }
                     methDecl.addParameterList(tpl);
                 }
                 
                 // Make sure the boxing information is set correctly
                 BoxingDeclarationVisitor v = new CompilerBoxingDeclarationVisitor(this);
                 v.visit(methDecl);
                 
                 // Generate the method
                 classBuilder.method(methDecl);
             }
         } else {
             // Normal case, just generate the specifier statement
             result = result.append(expressionGen().transform(op));
         }
         return result;
     }
 
     private Tree.Identifier makeIdentifier(String name) {
         Tree.Identifier id = new Tree.Identifier(null);
         id.setText(name);
         return id;
     }
     
     public void transform(AttributeDeclaration decl, ClassDefinitionBuilder classBuilder) {
         final Value model = decl.getDeclarationModel();
         boolean lazy = decl.getSpecifierOrInitializerExpression() instanceof LazySpecifierExpression;
         boolean useField = Strategy.useField(model) && !lazy;
         String attrName = decl.getIdentifier().getText();
 
         // Only a non-formal or a concrete-non-lazy attribute has a corresponding field
         // and if a captured class parameter exists with the same name we skip this part as well
         Parameter parameter = CodegenUtil.findParamForDecl(decl);
         boolean createField = Strategy.createField(parameter, model) && !lazy;
         boolean concrete = Decl.withinInterface(decl)
                 && decl.getSpecifierOrInitializerExpression() != null;
         if (!lazy && (concrete || (!Decl.isFormal(decl) && createField))) {
             ProducedTypedReference typedRef = getTypedReference(model);
             ProducedTypedReference nonWideningTypedRef = nonWideningTypeDecl(typedRef);
             ProducedType nonWideningType = nonWideningType(typedRef, nonWideningTypedRef);
             
             if (Decl.isIndirect(decl)) {
                 attrName = Naming.getAttrClassName(model, 0);
                 nonWideningType = getGetterInterfaceType(model);
             }
             
             JCExpression initialValue = null;
             if (decl.getSpecifierOrInitializerExpression() != null) {
                 Value declarationModel = model;
                 initialValue = expressionGen().transformExpression(decl.getSpecifierOrInitializerExpression().getExpression(), 
                         CodegenUtil.getBoxingStrategy(declarationModel), 
                         nonWideningType);
             }
 
             int flags = 0;
             
             if (!CodegenUtil.isUnBoxed(nonWideningTypedRef.getDeclaration())) {
                 flags |= JT_NO_PRIMITIVES;
             }
             JCExpression type = makeJavaType(nonWideningType, flags);
             if (Decl.isLate(decl)) {
                 type = make().TypeArray(type);
             }
 
             int modifiers = (useField) ? transformAttributeFieldDeclFlags(decl) : transformLocalDeclFlags(decl);
             
             // If the attribute is really from a parameter then don't generate a field
             // (The ClassDefinitionBuilder does it in that case)
             if (parameter == null
                     || ((parameter instanceof ValueParameter) 
                             && ((ValueParameter)parameter).isHidden())) {
                 if (concrete) {
                     classBuilder.getCompanionBuilder((TypeDeclaration)model.getContainer()).field(modifiers, attrName, type, initialValue, !useField);
                 } else {
                     classBuilder.field(modifiers, attrName, type, initialValue, !useField);
                 }        
             }
         }
 
         boolean withinInterface = Decl.withinInterface(decl);
         if (useField || withinInterface || lazy) {
             if (!withinInterface || model.isShared()) {
                 // Generate getter in main class or interface (when shared)
                 classBuilder.attribute(makeGetter(decl, false, lazy));
             }
             if (withinInterface && lazy) {
                 // Generate getter in companion class
                 classBuilder.getCompanionBuilder((Interface)decl.getDeclarationModel().getContainer()).attribute(makeGetter(decl, true, lazy));
             }
             if (Decl.isVariable(decl) || Decl.isLate(decl)) {
                 if (!withinInterface || model.isShared()) {
                     // Generate setter in main class or interface (when shared)
                     classBuilder.attribute(makeSetter(decl, false, lazy));
                 }
                 if (withinInterface) {
                     // Generate setter in companion class
                     classBuilder.getCompanionBuilder((Interface)decl.getDeclarationModel().getContainer()).attribute(makeSetter(decl, true, lazy));
                 }
             }
         }
     }
 
 	public AttributeDefinitionBuilder transform(AttributeSetterDefinition decl, boolean forCompanion) {
 	    if (Strategy.onlyOnCompanion(decl.getDeclarationModel()) && !forCompanion) {
 	        return null;
 	    }
         String name = decl.getIdentifier().getText();
         final AttributeDefinitionBuilder builder = AttributeDefinitionBuilder
                 /* 
                  * We use the getter as TypedDeclaration here because this is the same type but has a refined
                  * declaration we can use to make sure we're not widening the attribute type.
                  */
             .setter(this, name, decl.getDeclarationModel().getGetter())
             .modifiers(transformAttributeGetSetDeclFlags(decl.getDeclarationModel(), forCompanion));
         
         // companion class members are never actual no matter what the Declaration says
         if(forCompanion)
             builder.notActual();
         
         if (Decl.withinClass(decl) || forCompanion) {
             JCBlock setterBlock = makeSetterBlock(decl.getDeclarationModel(), decl.getBlock(), decl.getSpecifierExpression());
             builder.setterBlock(setterBlock);
         } else {
             builder.isFormal(true);
         }
         builder.userAnnotationsSetter(expressionGen().transform(decl.getAnnotationList()));
         return builder;
     }
 
     public AttributeDefinitionBuilder transform(AttributeGetterDefinition decl, boolean forCompanion) {
         if (Strategy.onlyOnCompanion(decl.getDeclarationModel()) && !forCompanion) {
             return null;
         }
         String name = decl.getIdentifier().getText();
         //expressionGen().transform(decl.getAnnotationList());
         final AttributeDefinitionBuilder builder = AttributeDefinitionBuilder
             .getter(this, name, decl.getDeclarationModel())
             .modifiers(transformAttributeGetSetDeclFlags(decl.getDeclarationModel(), forCompanion));
         
         // companion class members are never actual no matter what the Declaration says
         if(forCompanion)
             builder.notActual();
         
         if (Decl.withinClass(decl) || forCompanion) {
             JCBlock body = statementGen().transform(decl.getBlock());
             builder.getterBlock(body);
         } else {
             builder.isFormal(true);
         }
         builder.userAnnotations(expressionGen().transform(decl.getAnnotationList()));
         return builder;    
     }
 
     private int transformDeclarationSharedFlags(Declaration decl){
         return Decl.isShared(decl) && !Decl.isAncestorLocal(decl) ? PUBLIC : 0;
     }
     
     private int transformClassDeclFlags(ClassOrInterface cdecl) {
         int result = 0;
 
         result |= transformDeclarationSharedFlags(cdecl);
         // aliases cannot be abstract, especially since they're just placeholders
         result |= (cdecl instanceof Class) && (cdecl.isAbstract() || cdecl.isFormal()) && !cdecl.isAlias() ? ABSTRACT : 0;
         result |= (cdecl instanceof Interface) ? INTERFACE : 0;
         // aliases are always final placeholders, final classes are also final
         result |= (cdecl instanceof Class) && (cdecl.isAlias() || cdecl.isFinal())  ? FINAL : 0;
 
         return result;
     }
 
     private int transformTypeAliasDeclFlags(TypeAlias decl) {
         int result = 0;
 
         result |= transformDeclarationSharedFlags(decl);
         result |= FINAL;
 
         return result;
     }
 
     private int transformClassDeclFlags(Tree.ClassOrInterface cdecl) {
         return transformClassDeclFlags(cdecl.getDeclarationModel());
     }
     
     private int transformMethodDeclFlags(Method def) {
         int result = 0;
 
         if (def.isToplevel()) {
             result |= def.isShared() ? PUBLIC : 0;
             result |= STATIC;
         } else if (Decl.isLocal(def)) {
             result |= def.isShared() ? PUBLIC : 0;
         } else {
             result |= def.isShared() ? PUBLIC : PRIVATE;
             result |= def.isFormal() && !def.isDefault() ? ABSTRACT : 0;
             result |= !(def.isFormal() || def.isDefault() || def.getContainer() instanceof Interface) ? FINAL : 0;
         }
 
         return result;
     }
     
     private int transformAttributeFieldDeclFlags(Tree.AttributeDeclaration cdecl) {
         int result = 0;
 
         result |= Decl.isVariable(cdecl) || Decl.isLate(cdecl) ? 0 : FINAL;
         result |= PRIVATE;
 
         return result;
     }
 
     private int transformLocalDeclFlags(Tree.AttributeDeclaration cdecl) {
         int result = 0;
 
         result |= Decl.isVariable(cdecl) ? 0 : FINAL;
 
         return result;
     }
 
     /**
      * Returns the modifier flags to be used for the getter & setter for the 
      * given attribute-like declaration.  
      * @param tdecl attribute-like declaration (Value, Getter, Parameter etc)
      * @param forCompanion Whether the getter/setter is on a companion type
      * @return The modifier flags.
      */
     int transformAttributeGetSetDeclFlags(TypedDeclaration tdecl, boolean forCompanion) {
         if (tdecl instanceof Setter) {
             // Spec says: A setter may not be annotated shared, default or 
             // actual. The visibility and refinement modifiers of an attribute 
             // with a setter are specified by annotating the matching getter.
             tdecl = ((Setter)tdecl).getGetter();
         }
         
         int result = 0;
 
         result |= tdecl.isShared() ? PUBLIC : PRIVATE;
         result |= ((tdecl.isFormal() && !tdecl.isDefault()) && !forCompanion) ? ABSTRACT : 0;
         result |= !(tdecl.isFormal() || tdecl.isDefault() || Decl.withinInterface(tdecl)) || forCompanion ? FINAL : 0;
 
         return result;
     }
 
     private int transformObjectDeclFlags(Value cdecl) {
         int result = 0;
 
         result |= FINAL;
         result |= !Decl.isAncestorLocal(cdecl) && Decl.isShared(cdecl) ? PUBLIC : 0;
 
         return result;
     }
 
     private AttributeDefinitionBuilder makeGetterOrSetter(Tree.AttributeDeclaration decl, boolean forCompanion, boolean lazy, 
                                                           AttributeDefinitionBuilder builder, boolean isGetter) {
         at(decl);
         if (forCompanion || lazy) {
             if (decl.getSpecifierOrInitializerExpression() != null) {
                 Value declarationModel = decl.getDeclarationModel();
                 ProducedTypedReference typedRef = getTypedReference(declarationModel);
                 ProducedTypedReference nonWideningTypedRef = nonWideningTypeDecl(typedRef);
                 ProducedType nonWideningType = nonWideningType(typedRef, nonWideningTypedRef);
                 
                 JCExpression expr = expressionGen().transformExpression(decl.getSpecifierOrInitializerExpression().getExpression(), 
                         CodegenUtil.getBoxingStrategy(declarationModel), 
                         nonWideningType);
                 builder.getterBlock(make().Block(0, List.<JCStatement>of(make().Return(expr))));
             } else {
                 JCExpression accessor = naming.makeQualifiedName(
                         naming.makeQuotedThis(), 
                         decl.getDeclarationModel(), 
                         Naming.NA_MEMBER | (isGetter ? Naming.NA_GETTER : Naming.NA_SETTER));
                 
                 if (isGetter) {
                     builder.getterBlock(make().Block(0, List.<JCStatement>of(make().Return(
                             make().Apply(
                                     null, 
                                     accessor, 
                                     List.<JCExpression>nil())))));
                 } else {
                     List<JCExpression> args = List.<JCExpression>of(naming.makeName(decl.getDeclarationModel(), Naming.NA_MEMBER | Naming.NA_IDENT));
                     builder.setterBlock(make().Block(0, List.<JCStatement>of(make().Exec(
                             make().Apply(
                                     null, 
                                     accessor, 
                                     args)))));
                 }
                 
             }
         }
         if(forCompanion)
             builder.notActual();
         return builder
             .modifiers(transformAttributeGetSetDeclFlags(decl.getDeclarationModel(), forCompanion))
             .isFormal((Decl.isFormal(decl) || Decl.withinInterface(decl)) && !forCompanion);
     }
     
     private AttributeDefinitionBuilder makeGetter(Tree.AttributeDeclaration decl, boolean forCompanion, boolean lazy) {
         at(decl);
         String attrName = decl.getIdentifier().getText();
         AttributeDefinitionBuilder getter = AttributeDefinitionBuilder
             .getter(this, attrName, decl.getDeclarationModel())
             .userAnnotations(expressionGen()
             .transform(decl.getAnnotationList()));
         
         if (Decl.isIndirect(decl)) {
             getter.getterBlock(generateIndirectGetterBlock(decl.getDeclarationModel()));
         }
         
         return makeGetterOrSetter(decl, forCompanion, lazy, getter, true);
     }
 
     private JCTree.JCBlock generateIndirectGetterBlock(Value v) {
         JCTree.JCExpression returnExpr;
         returnExpr = naming.makeQualIdent(naming.makeName(v, Naming.NA_WRAPPER), "$get");
         returnExpr = make().Apply(null, returnExpr, List.<JCExpression>nil());
         JCReturn returnValue = make().Return(returnExpr);
         List<JCStatement> stmts = List.<JCTree.JCStatement>of(returnValue);   
         JCTree.JCBlock block = make().Block(0L, stmts);
         return block;
     }
 
     private AttributeDefinitionBuilder makeSetter(Tree.AttributeDeclaration decl, boolean forCompanion, boolean lazy) {
         at(decl);
         String attrName = decl.getIdentifier().getText();
         AttributeDefinitionBuilder setter = AttributeDefinitionBuilder.setter(this, attrName, decl.getDeclarationModel());
         return makeGetterOrSetter(decl, forCompanion, lazy, setter, false);
     }
 
     public List<JCTree> transformWrappedMethod(Tree.AnyMethod def) {
         final Method model = def.getDeclarationModel();
         naming.noteDecl(model);
         // Generate a wrapper class for the method
         String name = def.getIdentifier().getText();
         ClassDefinitionBuilder builder = ClassDefinitionBuilder.methodWrapper(this, name, Decl.isShared(def));
         
         if (Decl.isAnnotationConstructor(def)) {
             AnnotationInstantiation inlineInfo = def.getDeclarationModel().getAnnotationInstantiation();
             builder.defs(makeLiteralArguments(def));
             builder.annotations(List.of(makeAtAnnotationInstantiation(inlineInfo)));
         }
         
         builder.methods(classGen().transform(def, builder));
         
         // Toplevel method
         if (Strategy.generateMain(def)) {
             // Add a main() method
             builder.method(makeMainForFunction(model));
         }
         
         List<JCTree> result = builder.build();
         
         if (Decl.isLocal(def)) {
             // Inner method
             JCVariableDecl call = at(def).VarDef(
                     make().Modifiers(FINAL),
                     naming.getSyntheticInstanceName(model),
                     naming.makeSyntheticClassname(model),
                     makeSyntheticInstance(model));
             result = result.append(call);
         }
         
         //if (Decl.isAnnotationConstructor(def)) {
             //result = result.prependList(transformAnnotationConstructorType(def));
         //}
         return result;
     }
     
     public List<JCTree> makeLiteralArguments(final Tree.AnyMethod method) {
         class AnnotationConstructorVisitor extends Visitor implements NaturalVisitor {
             
             private ListBuffer<JCStatement> staticArgs = ListBuffer.<JCStatement>lb();
             private boolean checkingArguments;
             private String fieldName;
             
             @Override
             public void handleException(Exception e, Node node) {
                 if (e instanceof RuntimeException) {
                     throw (RuntimeException)e;
                 } else {
                     throw new RuntimeException(e);
                 }
             }
             
             public void visit(Tree.Parameter p) {
                 if (p.getDefaultArgument() != null) {
                     checkingArguments = true;
                     fieldName = p.getDeclarationModel().getName();
                     super.visit(p);
                     checkingArguments = false;
                 }
             }
             
             public void visit(Tree.InvocationExpression invocation) {
                 checkingArguments = true;
                 if (invocation.getPositionalArgumentList() != null) {
                     invocation.getPositionalArgumentList().visit(this);
                 }
                 if (invocation.getNamedArgumentList() != null) { 
                     invocation.getNamedArgumentList().visit(this);
                 }
                 checkingArguments = false;
             }
             
             public void visit(Tree.Literal literal) {
                 if (checkingArguments){
                     appendStaticArgument(literal, expressionGen().transform(literal));
                 }
             }
             
             public void visit(Tree.BaseMemberExpression bme) {
                 if (checkingArguments){
                     Declaration declaration = bme.getDeclaration();
                     if (declaration instanceof Value
                             && (isBooleanFalse(declaration)
                             || isBooleanTrue(declaration))) {
                         appendStaticArgument(bme, expressionGen().transform(bme));
                     }
                 }
             }
             
             public void visit(Tree.PositionalArgument arg) {
                 fieldName = arg.getParameter().getName();
                 super.visit(arg);
             }
             
             public void visit(Tree.NamedArgument arg) {
                 fieldName = arg.getParameter().getName();
                 super.visit(arg);
             }
             
             private void appendStaticArgument(Tree.Primary bme, JCExpression init) {
                 staticArgs.append(makeVar(STATIC | FINAL | (method.getDeclarationModel().isShared() ? PUBLIC : 0), 
                         fieldName,
                         makeJavaType(bme.getTypeModel()), 
                         init));
             }
             
             public List<JCStatement> getStaticArguments() {
                 return staticArgs.toList();
             }
 
         }
         AnnotationConstructorVisitor v = new AnnotationConstructorVisitor();
         v.visit(method);
         return (List)v.staticArgs.toList();
     }
     
     public JCAnnotation makeAtAnnotationInstantiation(AnnotationInstantiation inlineInfo) {
         ListBuffer<JCExpression> arguments = ListBuffer.lb();
         for (AnnotationArgument inlineArgument : inlineInfo.getArguments()) {
             arguments.append(make().Literal(Decl.encodeAnnotationConstructor(inlineArgument)));
         }
         return make().Annotation(
                 make().Type(syms().ceylonAtAnnotationInstantiationType),
                 List.<JCExpression>of(
                         make().Assign(
                                 naming.makeUnquotedIdent("arguments"),
                                 make().NewArray(null, null, arguments.toList())),
                         make().Assign(
                                 naming.makeUnquotedIdent("annotationClass"),
                                 naming.makeQualIdent(makeJavaType(((Class)inlineInfo.getPrimary()).getType()), "class"))
                 ));
     }
 
     private MethodDefinitionBuilder makeAnnotationMethod(Tree.Parameter parameter) {
         Parameter parameterModel = parameter.getDeclarationModel();
         String name = naming.selector(parameterModel, Naming.NA_ANNOTATION_MEMBER);
         JCExpression type = transformAnnotationMethodType(parameter);
         JCExpression defaultValue = parameterModel.isDefaulted() ? transformAnnotationParameterDefault(parameter) : null;
         MethodDefinitionBuilder mdb = MethodDefinitionBuilder.method2(this, name);
         if (isMetamodelReference(parameterModel.getType())
                 || 
                 (typeFact().isIterableType(parameterModel.getType())
                 && isMetamodelReference(typeFact().getIteratedType(parameterModel.getType())))) {
             mdb.modelAnnotations(List.of(make().Annotation(make().Type(syms().ceylonAtMetamodelReferenceType), 
                     List.<JCExpression>nil())));
         }
         mdb.modifiers(PUBLIC | ABSTRACT);
         mdb.resultType(null, type);
         mdb.defaultValue(defaultValue);
         mdb.noBody();
         return mdb;
     }
 
     public List<MethodDefinitionBuilder> transform(Tree.AnyMethod def, ClassDefinitionBuilder classBuilder) {
         // Transform the method body of the 'inner-most method'
         List<JCStatement> body = transformMethodBody(def);        
         return transform(def, classBuilder, body);
     }
 
     List<MethodDefinitionBuilder> transform(Tree.AnyMethod def,
             ClassDefinitionBuilder classBuilder, List<JCStatement> body) {
         final Method model = def.getDeclarationModel();
         
         List<MethodDefinitionBuilder> result = List.<MethodDefinitionBuilder>nil();
         if (!Decl.withinInterface(model)) {
             // Transform to the class
             boolean refinedResultType = !model.getType().isExactly(
                     ((TypedDeclaration)model.getRefinedDeclaration()).getType());
             result = transformMethod(def, 
                     true,
                     true,
                     true,
                     transformMplBody(def.getParameterLists(), model, body),
                     refinedResultType 
                     && !Decl.withinInterface(model.getRefinedDeclaration())? daoSuper : daoThis,
                     !Strategy.defaultParameterMethodOnSelf(model));
         } else {// Is within interface
             // Transform the definition to the companion class, how depends
             // on what kind of method it is
             List<MethodDefinitionBuilder> companionDefs;
             if (def instanceof Tree.MethodDeclaration) {
                 final SpecifierExpression specifier = ((Tree.MethodDeclaration) def).getSpecifierExpression();
                 if (specifier == null) {
                     // formal or abstract 
                     // (still need overloads and DPMs on the companion)
                     companionDefs = transformMethod(def,  
                             false,
                             true,
                             true,
                             null,
                            model.isShared() ? daoCompanionDollarThis : daoCompanion,//daoCompanionDollarThis,
                             false);   
                 } else {
                     companionDefs = transformMethod(def,
                             true,
                             false,
                             !model.isShared(),
                             transformMplBody(def.getParameterLists(), model, body),
                            model.isShared() ? daoCompanionDollarThis : daoCompanion,//daoCompanionDollarThis,
                             false);
                 }
             } else if (def instanceof Tree.MethodDefinition) {
                 companionDefs = transformMethod(def,  
                         true,
                         false,
                         !model.isShared(),
                         transformMethodBlock((Tree.MethodDefinition)def),
                        model.isShared() ? daoCompanionDollarThis : daoCompanion,
                         false);
             } else {
                 throw new RuntimeException();
             }
             classBuilder.getCompanionBuilder((TypeDeclaration)model.getContainer())
                 .methods(companionDefs);
             
             // Transform the declaration to the target interface
             // but only if it's shared
             if (Decl.isShared(model)) {
                 result = transformMethod(def, 
                         true,
                         true,
                         true,
                         null,
                         daoAbstract,
                         !Strategy.defaultParameterMethodOnSelf(model));
             }
         }
         return result;
     }
 
     
     /**
      * Transforms a method, generating default argument overloads and 
      * default value methods
      * @param def The method
      * @param model The method model
      * @param methodName The method name
      * @param transformMethod Whether the method itself should be transformed.
      * @param actualAndAnnotations Whether the method itself is actual and has 
      * model annotations
      * @param body The body of the method (or null for an abstract method)
      * @param daoTransformation The default argument overload transformation
      * @param transformDefaultValues Whether to generate default value methods
      * @param defaultValuesBody Whether the default value methods should have a body
      */
     private List<MethodDefinitionBuilder> transformMethod(Tree.AnyMethod def,
             boolean transformMethod, boolean actual, boolean includeAnnotations, List<JCStatement> body, 
             DaoBody daoTransformation, 
             boolean defaultValuesBody) {
         
         final Method methodModel = def.getDeclarationModel();
         ListBuffer<MethodDefinitionBuilder> lb = ListBuffer.<MethodDefinitionBuilder>lb();
         boolean needsRaw = false;
         Declaration refinedDeclaration = methodModel.getRefinedDeclaration();
         if (Decl.withinClassOrInterface(methodModel)) {
             final Scope refinedFrom = refinedDeclaration.getContainer();
             final ProducedType inheritedFrom = ((ClassOrInterface)methodModel.getContainer()).getType().getSupertype((TypeDeclaration)refinedFrom);
             needsRaw = needsRawification(inheritedFrom);
         }
         
         final MethodDefinitionBuilder methodBuilder = MethodDefinitionBuilder.method(
                 this, methodModel);
         
         // do the reified type param arguments
         if(def.getTypeParameterList() != null && gen().supportsReified(methodModel))
             methodBuilder.reifiedTypeParameters(def.getTypeParameterList().getTypeParameterDeclarations());
         
         Tree.ParameterList parameterList = def.getParameterLists().get(0);
         for (final Tree.Parameter parameter : parameterList.getParameters()) {
             Parameter parameterModel = parameter.getDeclarationModel();
             List<JCAnnotation> annotations = null;
             if (includeAnnotations) {
                 annotations = expressionGen().transform(parameter.getAnnotationList());
             }
             
             methodBuilder.parameter(parameterModel, annotations, needsRaw ? JT_RAW_TP_BOUND : 0, true);
 
             if (parameterModel.isDefaulted()
                     || parameterModel.isSequenced()) {
                 if (refinedDeclaration == methodModel
                         || (!methodModel.getType().isExactly(
                                 ((TypedDeclaration)refinedDeclaration).getType())
                                 && !Decl.withinInterface(refinedDeclaration))) {
                     
                     if (daoTransformation != null) {
                         MethodDefinitionBuilder overloadBuilder = MethodDefinitionBuilder.method(this, methodModel);
                         MethodDefinitionBuilder overloadedMethod = new DefaultedArgumentMethod(daoTransformation, methodModel).makeOverload(
                                 overloadBuilder, 
                                 parameterList, parameter, def.getTypeParameterList());
                         lb.append(overloadedMethod);
                     }
                     
                     if (refinedDeclaration == methodModel) {
                         lb.append(makeParamDefaultValueMethod(defaultValuesBody, methodModel, parameterList, parameter, def.getTypeParameterList()));    
                     }
                 }
             }
         }
         
         if (transformMethod) {
             methodBuilder
                 .modifiers(transformMethodDeclFlags(methodModel));
             if (actual) {
                 methodBuilder.isOverride(methodModel.isActual());
             }
             if (includeAnnotations) {
                 methodBuilder.userAnnotations(expressionGen().transform(def.getAnnotationList()));
                 methodBuilder.modelAnnotations(methodModel.getAnnotations());
             } else {
                 methodBuilder.noAnnotations();
             }
             if (CodegenUtil.hasCompilerAnnotation(def, "test")){
                 methodBuilder.userAnnotations(List.of(make().Annotation(naming.makeFQIdent("org", "junit", "Test"), List.<JCTree.JCExpression>nil())));
             }
             methodBuilder.resultType(methodModel, needsRaw ? JT_RAW_TP_BOUND : 0);
             if (!needsRaw) {
                 copyTypeParameters(methodModel, methodBuilder);
             }
             if (body != null) {
                 // Construct the outermost method using the body we've built so far
                 methodBuilder.body(body);
             } else {
                 methodBuilder.noBody();
             }
             
             lb.append(methodBuilder);
         }
         return lb.toList();
     }
 
     /**
      * Constructs all but the outer-most method of a {@code Method} with 
      * multiple parameter lists 
      * @param model The {@code Method} model
      * @param body The inner-most body
      */
     List<JCStatement> transformMplBody(java.util.List<Tree.ParameterList> parameterListsTree,
             Method model,
             List<JCStatement> body) {
         ProducedType resultType = model.getType();
         for (int index = model.getParameterLists().size() - 1; index >  0; index--) {
             resultType = gen().typeFact().getCallableType(resultType);
             CallableBuilder cb = CallableBuilder.mpl(gen(), resultType, model.getParameterLists().get(index), parameterListsTree.get(index), body);
             body = List.<JCStatement>of(make().Return(cb.build()));
         }
         return body;
     }
 
     private List<JCStatement> transformMethodBody(Tree.AnyMethod def) {
         List<JCStatement> body = null;
         final Method model = def.getDeclarationModel();
         
         if (Decl.isDeferredOrParamInitialized(def)) {
             // Uninitialized or deferred initialized method => Make a Callable field
             final Parameter initializingParameter = CodegenUtil.findParamForDecl(def);
             int mods = PRIVATE;
             JCExpression initialValue;
             if (initializingParameter != null) {
                 mods |= FINAL;
                 int namingOptions = Naming.NA_MEMBER;
                 if (initializingParameter.getContainer() instanceof Method) {
                     // We're initializing a local method, which will have a 
                     // class wrapper of the same name as the param, so 
                     // the param gets renamed
                     namingOptions |= Naming.NA_ALIASED;
                 }
                 initialValue = naming.makeName(initializingParameter, namingOptions);
             } else {
                 // The field isn't initialized by a parameter, but later in the block
                 initialValue = makeNull();
             }
             current().field(mods, model.getName(), makeJavaType(typeFact().getCallableType(model.getType())), initialValue, false);
             Invocation invocation = new CallableSpecifierInvocation(
                     this,
                     model,
                     initializingParameter != null ? 
                                             naming.makeName(initializingParameter, Naming.NA_IDENT) : 
                                             naming.makeName(model, Naming.NA_IDENT),
                     def);
             invocation.handleBoxing(true);
             JCExpression call = expressionGen().transformInvocation(invocation);
             JCStatement stmt;
             if (isVoid(def)) {
                 stmt = make().Exec(call);
             } else {
                 stmt = make().Return(call);
             }
             
             JCStatement result;
             if (initializingParameter == null) {
                 // If the field isn't initialized by a parameter we have to 
                 // cope with the possibility that it's never initialized
                 final JCBinary cond = make().Binary(JCTree.EQ, naming.makeName(model, Naming.NA_IDENT), makeNull());
                 final JCStatement throw_ = make().Throw(make().NewClass(null, null, 
                         makeIdent(syms().ceylonUninitializedMethodErrorType), 
                         List.<JCExpression>nil(), 
                         null));
                 result = make().If(cond, throw_, stmt);
             } else {
                 result = stmt;
             }
             return List.<JCStatement>of(result);
         } else if (def instanceof Tree.MethodDefinition) {
             Scope container = model.getContainer();
             boolean isInterface = container instanceof com.redhat.ceylon.compiler.typechecker.model.Interface;
             if(!isInterface){
                 body = transformMethodBlock((Tree.MethodDefinition)def);
             } 
         } else if (def instanceof MethodDeclaration
                 && ((MethodDeclaration) def).getSpecifierExpression() != null) {
             body = transformSpecifiedMethodBody((MethodDeclaration)def, ((MethodDeclaration) def).getSpecifierExpression());
         }
         return body;
     }
 
     private List<JCStatement> transformMethodBlock(
             final Tree.MethodDefinition def) {
         final Method model = def.getDeclarationModel();
         final Tree.Block block = def.getBlock();
         List<JCStatement> body;
         boolean prevNoExpressionlessReturn = statementGen().noExpressionlessReturn;
         try {
             statementGen().noExpressionlessReturn = Decl.isMpl(model) || Strategy.useBoxedVoid(model);
             body = statementGen().transform(block).getStatements();    
         } finally {
             statementGen().noExpressionlessReturn = prevNoExpressionlessReturn;
         }
         // We void methods need to have their Callables return null
         // so adjust here.
         if ((Decl.isMpl(model) || Strategy.useBoxedVoid(model))
                 && !block.getDefinitelyReturns()) {
             if (Decl.isUnboxedVoid(model)) {
                 body = body.append(make().Return(makeNull()));
             } else {
                 body = body.append(make().Return(makeErroneous(block, "non-void method doesn't definitely return")));
             }
         }
         return body;
     }
 
     List<JCStatement> transformSpecifiedMethodBody(Tree.MethodDeclaration  def, SpecifierExpression specifierExpression) {
         final Method model = def.getDeclarationModel();
         List<JCStatement> body;
         MethodDeclaration methodDecl = (MethodDeclaration)def;
         boolean isLazy = specifierExpression instanceof Tree.LazySpecifierExpression;
         boolean returnNull = false;
         JCExpression bodyExpr;
         Tree.Term term = null;
         if (specifierExpression != null
                 && specifierExpression.getExpression() != null) {
             term = specifierExpression.getExpression().getTerm();
         }
         if (!isLazy && term instanceof Tree.FunctionArgument) {
             // Method specified with lambda: Don't bother generating a 
             // Callable, just transform the expr to use as the method body.
             Tree.FunctionArgument fa = (Tree.FunctionArgument)term;
             ProducedType resultType = model.getType();
             returnNull = isAnything(resultType) && fa.getExpression().getUnboxed();
             final java.util.List<com.redhat.ceylon.compiler.typechecker.tree.Tree.Parameter> lambdaParams = fa.getParameterLists().get(0).getParameters();
             final java.util.List<com.redhat.ceylon.compiler.typechecker.tree.Tree.Parameter> defParams = def.getParameterLists().get(0).getParameters();
             List<Substitution> substitutions = List.nil();
             for (int ii = 0; ii < lambdaParams.size(); ii++) {
                 substitutions = substitutions.append(naming.addVariableSubst(lambdaParams.get(ii).getDeclarationModel(), 
                         defParams.get(ii).getIdentifier().getText()));
             }
             bodyExpr = gen().expressionGen().transformExpression(fa.getExpression(), BoxingStrategy.UNBOXED, null);
             bodyExpr = gen().expressionGen().applyErasureAndBoxing(bodyExpr, resultType, 
                     true, 
                     model.getUnboxed() ? BoxingStrategy.UNBOXED : BoxingStrategy.BOXED, 
                             resultType);
             for (Substitution subs : substitutions) {
                 subs.close();
             }
         } else if (!isLazy && typeFact().isCallableType(term.getTypeModel())) {
             returnNull = isAnything(term.getTypeModel()) && term.getUnboxed();
             Method method = methodDecl.getDeclarationModel();
             Tree.Term primary = Decl.unwrapExpressionsUntilTerm(specifierExpression.getExpression());
             boolean lazy = specifierExpression instanceof Tree.LazySpecifierExpression;
             boolean inlined = CodegenUtil.canOptimiseMethodSpecifier(primary, method);
             Invocation invocation;
             if (lazy && primary instanceof InvocationExpression) {
                 primary = ((InvocationExpression)primary).getPrimary();
             }
             if ((lazy || inlined)
                     && primary instanceof Tree.MemberOrTypeExpression
                     && ((Tree.MemberOrTypeExpression)primary).getDeclaration() instanceof Functional) {
                 Declaration primaryDeclaration = ((Tree.MemberOrTypeExpression)primary).getDeclaration();
                 ProducedReference producedReference = ((Tree.MemberOrTypeExpression)primary).getTarget();
                 invocation = new MethodReferenceSpecifierInvocation(
                         this, 
                         (Tree.MemberOrTypeExpression)primary, 
                         primaryDeclaration,
                         producedReference,
                         method,
                         specifierExpression);
             } else if (!lazy && !inlined) {
                 // must be a callable we stored
                 String name = naming.getMethodSpecifierAttributeName(method);
                 invocation = new CallableSpecifierInvocation(
                         this, 
                         method, 
                         naming.makeUnquotedIdent(name),
                         primary);
             } else if (isCeylonCallableSubtype(primary.getTypeModel())) {
                 invocation = new CallableSpecifierInvocation(
                         this, 
                         method, 
                         expressionGen().transformExpression(primary),
                         primary);
             } else {
                 throw Assert.fail("Unhandled primary " + primary);
             }
             invocation.handleBoxing(true);
             bodyExpr = expressionGen().transformInvocation(invocation);
         } else {
             bodyExpr = expressionGen().transformExpression(model, term);
         }
         if (!Decl.isUnboxedVoid(model) || Strategy.useBoxedVoid(model)) {
             if (returnNull) {
                 body = List.<JCStatement>of(make().Exec(bodyExpr), make().Return(makeNull()));
             } else {
                 body = List.<JCStatement>of(make().Return(bodyExpr));
             }
         } else {
             body = List.<JCStatement>of(make().Exec(bodyExpr));
         }
         return body;
     }
 
     private boolean isVoid(Tree.Declaration def) {
         if (def instanceof Tree.AnyMethod) {
             return gen().isAnything(((Tree.AnyMethod)def).getType().getTypeModel());
         } else if (def instanceof Tree.AnyClass) {
             // Consider classes void since ctors don't require a return statement
             return true;
         }
         throw new RuntimeException();
     }
     
     private boolean isVoid(Declaration def) {
         if (def instanceof Method) {
             return gen().isAnything(((Method)def).getType());
         } else if (def instanceof Class) {
             // Consider classes void since ctors don't require a return statement
             return true;
         }
         throw new RuntimeException();
     }
 
     /** 
      * Generate a body for the overload method which delegates to the 
      * canonical method using a let to spubstitute defaulted parameters
      */
     private static int OL_BODY_DELEGATE_CANONICAL = 1<<0;
     /** 
      * Modifies OL_BODY_DELEGATE_CANONICAL so that the method is suitable 
      * for a companion class.
      */
     private static int OL_COMPANION = 1<<1;
     /** 
      * Modifies OL_BODY_DELEGATE_CANONICAL so that the canonical method on the 
      * interface instantce, {@code $this}, is called, rather than the 
      * canonical method on {@code this}.
      */ 
     private static int OL_DELEGATE_INTERFACE_INSTANCE = 1<<2;
     
     /** 
      * Abstraction over possible transformations for the body of an overloaded
      * declaration which supplies a defaulted argument.
      * @see DefaultedArgumentOverload 
      */
     abstract class DaoBody {
         
         protected List<JCExpression> makeTypeArguments(DefaultedArgumentOverload ol) {
             if (ol.defaultParameterMethodOnSelf() 
                     || ol.defaultParameterMethodOnOuter()) {
                 return List.<JCExpression>nil();
             } else if (ol.defaultParameterMethodStatic()){
                 return typeArguments((Functional)ol.getModel());
             } else {
                 return List.<JCExpression>nil();
             }
         }
 
         abstract void makeBody(DefaultedArgumentOverload overloaded,
                 MethodDefinitionBuilder overloadBuilder,
                 Tree.ParameterList parameterList,
                 Tree.Parameter currentParameter,
                 Tree.TypeParameterList typeParameterList);
 
         JCExpression makeMethodNameQualifier() {
             return null;
         }
         
     }
     
     /** 
      * a body-less (i.e. abstract) transformation.
      */
     private class DaoAbstract extends DaoBody {
         @Override
         public void makeBody(DefaultedArgumentOverload overloaded,
                 MethodDefinitionBuilder overloadBuilder,
                 Tree.ParameterList parameterList,
                 Tree.Parameter currentParameter,
                 Tree.TypeParameterList typeParameterList) {
             overloadBuilder.noBody();
         }
     };
     final DaoAbstract daoAbstract = new DaoAbstract();
     /**
      * a transformation for an overloaded 
      * default parameter body which delegates to the "canonical"
      * method/constructor using a {@code let} expresssion
      * to substitute defaulted arguments
      */
     private class DaoThis extends DaoBody {
         public void makeBody(DefaultedArgumentOverload overloaded,
                 MethodDefinitionBuilder overloadBuilder,
                 Tree.ParameterList parameterList,
                 Tree.Parameter currentParameter,
                 Tree.TypeParameterList typeParameterList) {
             ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
             overloaded.appendImplicitArguments(typeParameterList, overloadBuilder, args);
             
             ListBuffer<JCStatement> vars = ListBuffer.<JCStatement>lb();
             overloaded.initVars(currentParameter, vars);
             
             boolean useDefault = false;
             for (Tree.Parameter parameter : parameterList.getParameters()) {
                 final Parameter parameterModel = parameter.getDeclarationModel();
                 if (parameter == currentParameter) {
                     useDefault = true;
                 }
                 if (useDefault) {
                     JCExpression defaultValueMethodName = naming.makeDefaultedParamMethod(overloaded.makeDefaultArgumentValueMethodQualifier(), parameterModel);
                     Naming.SyntheticName varName = naming.temp("$"+parameterModel.getName()+"$");
                     final ProducedType paramType;
                     if (parameterModel instanceof FunctionalParameter) {
                         paramType = typeFact().getCallableType(parameterModel.getType());
                     } else {
                         paramType = parameterModel.getType();
                     }
                     vars.append(makeVar(varName, 
                             makeJavaType(paramType), 
                             make().Apply(makeTypeArguments(overloaded), 
                                     defaultValueMethodName, 
                                     ListBuffer.<JCExpression>lb().appendList(args).toList())));
                     args.add(varName.makeIdent());
                 } else {
                     args.add(naming.makeName(parameterModel, Naming.NA_MEMBER | Naming.NA_ALIASED));
                 }
             }
             makeBody(overloaded, overloadBuilder, args, vars);
         }
         
         protected final void makeBody(DefaultedArgumentOverload overloaded, MethodDefinitionBuilder overloadBuilder, ListBuffer<JCExpression> args, ListBuffer<JCStatement> vars) {
             JCExpression invocation = overloaded.makeInvocation(args);
             Declaration model = overloaded.getModel();// TODO Yuk
             if (!isVoid(model)
                     || model instanceof Method && !(Decl.isUnboxedVoid(model))
                     || (model instanceof Method && Strategy.useBoxedVoid((Method)model)) 
                     || Strategy.generateInstantiator(model)) {
                 if (!vars.isEmpty()) {
                     invocation = make().LetExpr(vars.toList(), invocation);
                 }
                 overloadBuilder.body(make().Return(invocation));
             } else {
                 vars.append(make().Exec(invocation));
                 invocation = make().LetExpr(vars.toList(), makeNull());
                 overloadBuilder.body(make().Exec(invocation));
             }
         }
     }
     final DaoThis daoThis = new DaoThis();
     
     /**
      * specialises {@link DaoThis} for transforming declarations for companion classes
      */
     private class DaoCompanion extends DaoThis {
         @Override
         protected final List<JCExpression> makeTypeArguments(DefaultedArgumentOverload ol) {
             return List.<JCExpression>nil();
         }
     }
     final DaoCompanion daoCompanion = new DaoCompanion();
     
     /**
      * specialises {@link DaoCompanion} for transforming dpms 
      * on companion classes where the we delegate to the $this instances 
      * canonical method.
      */
     private class DaoCompanionDollarThis extends DaoCompanion {
         @Override
         JCExpression makeMethodNameQualifier() {
             return naming.makeQuotedThis();
         }
     }
     final DaoCompanionDollarThis daoCompanionDollarThis = new DaoCompanionDollarThis();
     
     /**
      * a transformation for an overloaded 
      * default parameter method body which delegates
      * to the super class. This is used when we need to refine the return 
      * type of a DPM. 
      */
     private class DaoSuper extends DaoBody {
 
         JCExpression makeMethodNameQualifier() {
             return naming.makeSuper();
         }
         
         @Override
         void makeBody(
                 DefaultedArgumentOverload overloaded,
                 MethodDefinitionBuilder overloadBuilder,
                 com.redhat.ceylon.compiler.typechecker.tree.Tree.ParameterList parameterList,
                 com.redhat.ceylon.compiler.typechecker.tree.Tree.Parameter currentParameter,
                 TypeParameterList typeParameterList) {
             
             ListBuffer<JCExpression> args = ListBuffer.<JCExpression>lb();
             for (Tree.Parameter parameter : parameterList.getParameters()) {
                 if (parameter == currentParameter) {
                     break;
                 }
                 args.add(naming.makeUnquotedIdent(parameter.getIdentifier().getText()));
             }
             JCExpression superCall = overloaded.makeInvocation(args);
             /*JCMethodInvocation superCall = make().Apply(null,
                     naming.makeQualIdent(naming.makeSuper(), ((Method)overloaded.getModel()).getName()),
                     args.toList());*/
             JCExpression refinedType = makeJavaType(((Method)overloaded.getModel()).getType(), JT_NO_PRIMITIVES);
             overloadBuilder.body(make().Return(make().TypeCast(refinedType, superCall)));
         }
     }
     final DaoSuper daoSuper = new DaoSuper();
     
     /**
      * A base class for transformations used for Ceylon declarations
      * which have defaulted parameters. We generate an overloaded 
      * method/constructor whose implementation which supplies the default
      * argument and delegates to the "canonical" method.
      * 
      * Subclasses specialise for the different kinds of declaration, and 
      * a separate set of classes handle the various transformations for the 
      * body of an overloaded declaration (see {@link DaoBody})
      */
     abstract class DefaultedArgumentOverload {
         protected final DaoBody daoBody;
         
         protected DefaultedArgumentOverload(DaoBody daoBody){
             this.daoBody = daoBody;
         }
         
         protected abstract long getModifiers();
 
         protected abstract JCExpression makeMethodName();
 
         protected abstract void resultType(MethodDefinitionBuilder overloadBuilder);
 
         protected abstract void typeParameters(MethodDefinitionBuilder overloadBuilder);
 
         protected final void appendImplicitParameters(TypeParameterList typeParameterList,
                 MethodDefinitionBuilder overloadBuilder) {
             if(typeParameterList != null){
                 java.util.List<TypeParameterDeclaration> typeParameterDeclarations = typeParameterList.getTypeParameterDeclarations();
                 overloadBuilder.reifiedTypeParameters(typeParameterDeclarations);
             }
         }
         
         protected abstract void appendImplicitArguments(TypeParameterList typeParameterList,
                 MethodDefinitionBuilder overloadBuilder, ListBuffer<JCExpression> args);
         
         protected abstract void initVars(Tree.Parameter currentParameter, ListBuffer<JCStatement> vars);
 
         protected final boolean defaultParameterMethodOnSelf() {
             return Strategy.defaultParameterMethodOnSelf(getModel());
         }
 
         protected final boolean defaultParameterMethodOnOuter() {
             return Strategy.defaultParameterMethodOnOuter(getModel());
         }
 
         protected final boolean defaultParameterMethodStatic() {
             return Strategy.defaultParameterMethodStatic(getModel());
         }
 
         protected abstract Declaration getModel();
 
         protected JCExpression makeInvocation(ListBuffer<JCExpression> args) {
             final JCExpression methName = makeMethodName();
             return make().Apply(List.<JCExpression>nil(),
                     methName, args.toList());            
         }
 
         /** Returns the qualiifier to use when invoking the default parameter value method */
         protected abstract JCIdent makeDefaultArgumentValueMethodQualifier();
         
         
         /**
          * Generates an overloaded method or constructor.
          */
         public MethodDefinitionBuilder makeOverload (
                 MethodDefinitionBuilder overloadBuilder,
                 Tree.ParameterList parameterList,
                 Tree.Parameter currentParameter,
                 Tree.TypeParameterList typeParameterList) {
             at(currentParameter);
 
             // Make the declaration
             // need annotations for BC, but the method isn't really there
             overloadBuilder.ignoreModelAnnotations();
             overloadBuilder.modifiers(getModifiers());
             resultType(overloadBuilder);
             typeParameters(overloadBuilder);
 
             appendImplicitParameters(typeParameterList, overloadBuilder);
             for (Tree.Parameter parameter : parameterList.getParameters()) {
                 if (parameter == currentParameter) {
                     break;
                 }
                 overloadBuilder.parameter(parameter.getDeclarationModel(), null, 0, false);
             }
             
             // Make the body
             // TODO MPL
             // TODO Type args on method call
             
             daoBody.makeBody(this, overloadBuilder,
                     parameterList,
                     currentParameter,
                     typeParameterList);
             
             return overloadBuilder;
         }
     }
     
     /**
      * A transformation for generating overloaded <em>methods</em> for 
      * defaulted arguments. 
      */
     class DefaultedArgumentMethod extends DefaultedArgumentOverload {
         private final Method method;
 
         DefaultedArgumentMethod(DaoBody daoBody, Method method) {
             super(daoBody);
             this.method = method;
         }
 
         @Override
         protected Method getModel() {
             return method;
         }
         @Override
         protected long getModifiers() {
             long mods = transformMethodDeclFlags(method);
             if (daoBody instanceof DaoAbstract == false) {
                 mods &= ~ABSTRACT;
             }
             if (daoBody instanceof DaoCompanion) {
                 mods |= FINAL;
             }
             return mods;
         }
 
         @Override
         protected final JCExpression makeMethodName() {
             return naming.makeQualifiedName(daoBody.makeMethodNameQualifier(), method, Naming.NA_MEMBER);
         }
 
         @Override
         protected void resultType(MethodDefinitionBuilder overloadBuilder) {
             overloadBuilder.resultType(method, 0);            
         }
 
         @Override
         protected void typeParameters(MethodDefinitionBuilder overloadBuilder) {
             copyTypeParameters(method, overloadBuilder);
         }
         
         @Override
         protected void appendImplicitArguments(TypeParameterList typeParameterList,
                 MethodDefinitionBuilder overloadBuilder, ListBuffer<JCExpression> args) {
             if(typeParameterList != null){
                 // we pass the reified type parameters along
                 java.util.List<TypeParameterDeclaration> typeParameterDeclarations = typeParameterList.getTypeParameterDeclarations();
                 for(TypeParameterDeclaration tp : typeParameterDeclarations){
                     args.append(makeUnquotedIdent(naming.getTypeArgumentDescriptorName(tp.getIdentifier().getText())));
                 }
             }
         }
 
         @Override
         protected void initVars(Tree.Parameter currentParameter, ListBuffer<JCStatement> vars) {
         }
 
         @Override
         protected JCIdent makeDefaultArgumentValueMethodQualifier() {
             return null;
         }
     }
     
     /**
      * A base class for transformations that generate overloaded declarations for 
      * defaulted arguments. 
      */
     abstract class DefaultedArgumentClass extends DefaultedArgumentOverload {
         
         protected final Class klass;
         
         protected Naming.SyntheticName companionInstanceName = null;
 
         DefaultedArgumentClass(DaoBody daoBody, Class klass) {
             super(daoBody);
             this.klass = klass;
         }
         
         @Override
         protected final Class getModel() {
             return klass;
         }
         
         @Override
         protected void initVars(Tree.Parameter currentParameter, ListBuffer<JCStatement> vars) {
             if (!Strategy.defaultParameterMethodStatic(klass)
                     && !Strategy.defaultParameterMethodOnOuter(klass)
                     && currentParameter != null) {
                 companionInstanceName = naming.temp("$impl$");
                 vars.append(makeVar(companionInstanceName, 
                         makeJavaType(klass.getType(), AbstractTransformer.JT_COMPANION),
                         make().NewClass(null, 
                                 null,
                                 makeJavaType(klass.getType(), AbstractTransformer.JT_CLASS_NEW | AbstractTransformer.JT_COMPANION),
                                 List.<JCExpression>nil(), null)));
             }
         }
         
         @Override
         protected JCIdent makeDefaultArgumentValueMethodQualifier() {
             if (defaultParameterMethodOnSelf() 
                     || defaultParameterMethodOnOuter()
                     || daoBody instanceof DaoCompanion) {
                 return null;
             } else if (defaultParameterMethodStatic()){
                 return null;
             } else {
                 return companionInstanceName.makeIdent();
             }
         }
     }
     
     /**
      * A transformation for generating overloaded <em>constructors</em> for 
      * defaulted arguments. 
      */
     class DefaultedArgumentConstructor extends DefaultedArgumentClass {
 
         DefaultedArgumentConstructor(DaoBody daoBody, Class klass) {
             super(daoBody, klass);
         }
 
         @Override
         protected long getModifiers() {
             return transformClassDeclFlags(klass) & (PUBLIC | PRIVATE | PROTECTED);
         }
 
         @Override
         protected JCExpression makeMethodName() {
             return naming.makeQualifiedThis(daoBody.makeMethodNameQualifier());
         }
 
         @Override
         protected void resultType(MethodDefinitionBuilder overloadBuilder) {
             // Constructor has no result type
         }
 
         @Override
         protected void typeParameters(MethodDefinitionBuilder overloadBuilder) {
             // Constructor has type parameters
         }
         
         @Override
         protected void appendImplicitArguments(TypeParameterList typeParameterList,
                 MethodDefinitionBuilder overloadBuilder, ListBuffer<JCExpression> args) {
             if(typeParameterList != null){
                 // we pass the reified type parameters along
                 java.util.List<TypeParameterDeclaration> typeParameterDeclarations = typeParameterList.getTypeParameterDeclarations();
                 for(TypeParameterDeclaration tp : typeParameterDeclarations){
                     args.append(makeUnquotedIdent(naming.getTypeArgumentDescriptorName(tp.getIdentifier().getText())));
                 }
             }
         }
     }
     
     /**
      * A transformation for generating overloaded <em>instantiator methods</em> for 
      * defaulted arguments. 
      */
     class DefaultedArgumentInstantiator extends DefaultedArgumentClass {
 
         DefaultedArgumentInstantiator(DaoBody daoBody, Class klass) {
             super(daoBody, klass);
         }
 
         @Override
         protected long getModifiers() {
             // remove the FINAL bit in case it gets set, because that is valid for a class decl, but
             // not for a method if in an interface
             return transformClassDeclFlags(klass) & ~FINAL;
         }
 
         @Override
         protected JCExpression makeMethodName() {
             return naming.makeInstantiatorMethodName(daoBody.makeMethodNameQualifier(), klass);
         }
 
         @Override
         protected void resultType(MethodDefinitionBuilder overloadBuilder) {
             /* Not actually part of the return type */
             overloadBuilder.ignoreModelAnnotations();
             if (!klass.isAlias() 
                     && Strategy.generateInstantiator(klass.getExtendedTypeDeclaration())
                     && klass.isActual()){
                     //&& ((Class)model).getExtendedTypeDeclaration().getContainer() instanceof Class) {
                 overloadBuilder.isOverride(true);
             }
             /**/
             
             JCExpression resultType;
             ProducedType type = klass.isAlias() ? klass.getExtendedType() : klass.getType();
             if (Decl.isAncestorLocal(klass)) {
                 // We can't expose a local type name to a place it's not visible
                 resultType = make().Type(syms().objectType);
             } else {
                 resultType = makeJavaType(type);
             }
             overloadBuilder.resultType(null, resultType);
         }
 
         @Override
         protected void typeParameters(MethodDefinitionBuilder overloadBuilder) {
             for (TypeParameter tp : typeParametersForInstantiator(klass)) {
                 overloadBuilder.typeParameter(tp);
             }
         }
         
         @Override
         protected void appendImplicitArguments(TypeParameterList typeParameterList,
                 MethodDefinitionBuilder overloadBuilder, ListBuffer<JCExpression> args) {
             ProducedType type = klass.isAlias() ? klass.getExtendedType() : klass.getType();
             type = type.resolveAliases();
             // fetch the type parameters from the klass we're instantiating itself if any
             for(ProducedType pt : type.getTypeArgumentList()){
                 args.append(makeReifiedTypeArgument(pt));
             }
         }
 
         @Override
         protected JCExpression makeInvocation(ListBuffer<JCExpression> args) {
             ProducedType type = klass.isAlias() ? klass.getExtendedType() : klass.getType();
             return make().NewClass(null, 
                     null, 
                     makeJavaType(type, JT_CLASS_NEW | JT_NON_QUALIFIED),
                     args.toList(),
                     null);
         }
     }
 
     /**
      * When generating an instantiator method if the inner class has a type 
      * parameter with the same name as a type parameter of an outer type, then the 
      * instantiator method shouldn't declare its own type parameter of that 
      * name -- it should use the captured one. This method filters out the
      * type parameters of the inner class which are the same as type parameters 
      * of the outer class so that they can be captured.
      */
     private java.util.List<TypeParameter> typeParametersForInstantiator(final Class model) {
         Assert.that(Strategy.generateInstantiator(model));
         java.util.List<TypeParameter> filtered = new ArrayList<TypeParameter>();
         java.util.List<TypeParameter> tps = model.getTypeParameters();
         if (tps != null) {
             for (TypeParameter tp : tps) {
                 boolean omit = false;
                 Scope s = model.getContainer();
                 while (!(s instanceof Package)) {
                     if (s instanceof Generic) {
                         for (TypeParameter outerTp : ((Generic)s).getTypeParameters()) {
                             if (tp.getName().equals(outerTp.getName())) {
                                 omit = true;
                             }
                         }
                     }
                     s = s.getContainer();
                 }
                 if (!omit) {
                     filtered.add(tp);
                 }
             }
         }
         return filtered;
     }
 
     /**
      * Creates a (possibly abstract) method for retrieving the value for a 
      * defaulted parameter
      * @param typeParameterList 
      */
     MethodDefinitionBuilder makeParamDefaultValueMethod(boolean noBody, Declaration container, 
             Tree.ParameterList params, Tree.Parameter currentParam, TypeParameterList typeParameterList) {
         at(currentParam);
         Parameter parameter = currentParam.getDeclarationModel();
         String name = Naming.getDefaultedParamMethodName(container, parameter );
         MethodDefinitionBuilder methodBuilder = MethodDefinitionBuilder.systemMethod(this, name);
         methodBuilder.ignoreModelAnnotations();
         int modifiers = 0;
         if (noBody) {
             modifiers |= PUBLIC | ABSTRACT;
         } else if (container == null
                 || !(container instanceof Class 
                         && Strategy.defaultParameterMethodStatic(container))) {
             // initializers can override parameter defaults
             modifiers |= FINAL;
         }
         if (container != null && container.isShared()) {
             modifiers |= PUBLIC;
         } else if (container == null || (!container.isToplevel()
                 && !noBody)){
             modifiers |= PRIVATE;
         }
         if (Strategy.defaultParameterMethodStatic(container)) {
             modifiers |= STATIC;
         }
         methodBuilder.modifiers(modifiers);
         
         if (container instanceof Method) {
             copyTypeParameters((Method)container, methodBuilder);
         } else if (container != null
                 && Decl.isToplevel(container)
                 && container instanceof Class) {
             copyTypeParameters((Class)container, methodBuilder);
         }
         
         // make sure reified type parameters are accepted
         if(typeParameterList != null)
             methodBuilder.reifiedTypeParameters(typeParameterList.getTypeParameterDeclarations());
         
         // Add any of the preceding parameters as parameters to the method
         for (Tree.Parameter p : params.getParameters()) {
             if (p == currentParam) {
                 break;
             }
             at(p);
             methodBuilder.parameter(p.getDeclarationModel(), null, 0, container instanceof Class);
         }
 
         // The method's return type is the same as the parameter's type
         methodBuilder.resultType(parameter, parameter.getType(), 0);
 
         // The implementation of the method
         if (noBody) {
             methodBuilder.noBody();
         } else {
             JCExpression expr = expressionGen().transform(currentParam);
             JCBlock body = at(currentParam).Block(0, List.<JCStatement> of(at(currentParam).Return(expr)));
             methodBuilder.block(body);
         }
 
         return methodBuilder;
     }
 
     public List<JCTree> transformObjectDefinition(Tree.ObjectDefinition def, ClassDefinitionBuilder containingClassBuilder) {
         return transformObject(def, def.getDeclarationModel(), 
                 def.getAnonymousClass(), containingClassBuilder, Decl.isLocal(def));
     }
     
     public List<JCTree> transformObjectArgument(Tree.ObjectArgument def) {
         return transformObject(def, def.getDeclarationModel(), 
                 def.getAnonymousClass(), null, false);
     }
     
     private List<JCTree> transformObject(Tree.StatementOrArgument def, Value model, 
             Class klass,
             ClassDefinitionBuilder containingClassBuilder,
             boolean makeLocalInstance) {
         naming.noteDecl(model);
         
         String name = model.getName();
         ClassDefinitionBuilder objectClassBuilder = ClassDefinitionBuilder.object(
                 this, name);
         
         CeylonVisitor visitor = gen().visitor;
         final ListBuffer<JCTree> prevDefs = visitor.defs;
         final boolean prevInInitializer = visitor.inInitializer;
         final ClassDefinitionBuilder prevClassBuilder = visitor.classBuilder;
         List<JCStatement> childDefs;
         try {
             visitor.defs = new ListBuffer<JCTree>();
             visitor.inInitializer = true;
             visitor.classBuilder = objectClassBuilder;
             
             def.visitChildren(visitor);
             childDefs = (List<JCStatement>)visitor.getResult().toList();
         } finally {
             visitor.classBuilder = prevClassBuilder;
             visitor.inInitializer = prevInInitializer;
             visitor.defs = prevDefs;
         }
 
         satisfaction(klass, objectClassBuilder);
         
         TypeDeclaration decl = model.getType().getDeclaration();
 
         if (Decl.isToplevel(model)
                 && def instanceof Tree.ObjectDefinition) {
             // generate a field and getter
             AttributeDefinitionBuilder builder = AttributeDefinitionBuilder
                     // TODO attr build take a JCExpression className
                     .wrapped(this, null, model.getName(), model, true)
                     .immutable()
                     .initialValue(makeNewClass(naming.makeName(model, Naming.NA_FQ | Naming.NA_WRAPPER)))
                     .is(PUBLIC, Decl.isShared(decl))
                     .is(STATIC, true);
             if (def instanceof Tree.ObjectDefinition) {
                 builder.userAnnotations(expressionGen().transform(((Tree.ObjectDefinition) def).getAnnotationList()));
             }            
             objectClassBuilder.defs(builder.build());
         }
 
         // Make sure top types satisfy reified type
         addReifiedTypeInterface(objectClassBuilder, klass);
 
         List<JCTree> result = objectClassBuilder
             .annotations(makeAtObject())
             .modelAnnotations(model.getAnnotations())
             .modifiers(transformObjectDeclFlags(model))
             .constructorModifiers(PRIVATE)
             .satisfies(decl.getSatisfiedTypes())
             .init(childDefs)
             .addGetTypeMethod(model.getType())
             .build();
         
         if (makeLocalInstance) {
             result = result.append(makeLocalIdentityInstance(name, objectClassBuilder.getClassName(), false));
         } else if (Decl.withinClassOrInterface(model)) {
             boolean visible = Decl.isCaptured(model);
             int modifiers = FINAL | ((visible) ? PRIVATE : 0);
             JCExpression type = makeJavaType(klass.getType());
             JCExpression initialValue = makeNewClass(makeJavaType(klass.getType()), null);
             containingClassBuilder.field(modifiers, name, type, initialValue, !visible);
             
             if (visible) {
                 AttributeDefinitionBuilder getter = AttributeDefinitionBuilder
                 .getter(this, name, model)
                 .modifiers(transformAttributeGetSetDeclFlags(model, false));
                 if (def instanceof Tree.ObjectDefinition) {
                     getter.userAnnotations(expressionGen().transform(((Tree.ObjectDefinition)def).getAnnotationList()));
                 }
                 
                 result = result.appendList(getter.build());
             }
         }
         
         return result;
     }
     
     /**
      * Makes a {@code main()} method which calls the given top-level method
      * @param def
      */
     private MethodDefinitionBuilder makeMainForClass(ClassOrInterface model) {
         at(null);
         if(model.isAlias())
             model = model.getExtendedTypeDeclaration();
         JCExpression nameId = makeJavaType(model.getType(), JT_RAW);
         List<JCExpression> arguments = makeBottomReifiedTypeParameters(model.getTypeParameters());
         JCNewClass expr = make().NewClass(null, null, nameId, arguments, null);
         return makeMainMethod(model, expr);
     }
     
     /**
      * Makes a {@code main()} method which calls the given top-level method
      * @param method
      */
     private MethodDefinitionBuilder makeMainForFunction(Method method) {
         at(null);
         JCExpression qualifiedName = naming.makeName(method, Naming.NA_FQ | Naming.NA_WRAPPER | Naming.NA_MEMBER);
         List<JCExpression> arguments = makeBottomReifiedTypeParameters(method.getTypeParameters());
         MethodDefinitionBuilder mainMethod = makeMainMethod(method, make().Apply(null, qualifiedName, arguments));
         return mainMethod;
     }
     
     private List<JCExpression> makeBottomReifiedTypeParameters(
             java.util.List<TypeParameter> typeParameters) {
         List<JCExpression> arguments = List.nil();
         for(int i=typeParameters.size()-1;i>=0;i--){
             arguments = arguments.prepend(gen().makeNothingTypeDescriptor());
         }
         return arguments;
     }
 
     /** 
      * Makes a {@code main()} method which calls the given callee 
      * (a no-args method or class)
      * @param decl
      * @param callee
      */
     private MethodDefinitionBuilder makeMainMethod(Declaration decl, JCExpression callee) {
         // Add a main() method
         MethodDefinitionBuilder methbuilder = MethodDefinitionBuilder
                 .main(this)
                 .ignoreModelAnnotations();
         // Add call to process.setupArguments
         JCExpression argsId = makeUnquotedIdent("args");
         JCMethodInvocation processExpr = make().Apply(null, naming.makeLanguageValue("process"), List.<JCTree.JCExpression>nil());
         methbuilder.body(make().Exec(make().Apply(null, makeSelect(processExpr, "setupArguments"), List.<JCTree.JCExpression>of(argsId))));
         // Add call to toplevel method
         methbuilder.body(make().Exec(callee));
         return methbuilder;
     }
     
     void copyTypeParameters(Functional def, MethodDefinitionBuilder methodBuilder) {
         if (def.getTypeParameters() != null) {
             for (TypeParameter t : def.getTypeParameters()) {
                 methodBuilder.typeParameter(t);
             }
         }
     }
 
     public List<JCTree> transform(final Tree.TypeAliasDeclaration def) {
         final TypeAlias model = def.getDeclarationModel();
         
         // we only create types for aliases so they can be imported with the model loader
         // and since we can't import local declarations let's just not create those types
         // in that case
         if(Decl.isAncestorLocal(def))
             return List.nil();
         
         naming.noteDecl(model);
         String ceylonClassName = def.getIdentifier().getText();
         final String javaClassName = Naming.quoteClassName(def.getIdentifier().getText());
 
         ClassDefinitionBuilder classBuilder = ClassDefinitionBuilder
                 .klass(this, javaClassName, ceylonClassName);
 
         // class alias
         classBuilder.constructorModifiers(PRIVATE);
         classBuilder.annotations(makeAtTypeAlias(model.getExtendedType()));
         classBuilder.annotations(expressionGen().transform(def.getAnnotationList()));
         classBuilder.isAlias(true);
 
         // make sure we set the container in case we move it out
         addAtContainer(classBuilder, model);
 
         visitClassOrInterfaceDefinition(def, classBuilder);
 
         return classBuilder
             .modelAnnotations(model.getAnnotations())
             .modifiers(transformTypeAliasDeclFlags(model))
             .satisfies(model.getSatisfiedTypes())
             .build();
     }
 }
