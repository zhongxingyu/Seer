 /**
  * <copyright>
  *
  * Copyright (c) 2013 CEA LIST and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink (CEA LIST) - initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.codegen.tables;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.util.CodeGenUtil;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.codegen.ecore.OCLGenModelGeneratorAdapter;
 import org.eclipse.ocl.examples.domain.elements.DomainParameterTypes;
 import org.eclipse.ocl.examples.domain.elements.DomainTypeParameters;
 import org.eclipse.ocl.examples.domain.elements.Nameable;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorEnumeration;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorEnumerationLiteral;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorInvalidType;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorPackage;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorProperty;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorType;
 import org.eclipse.ocl.examples.library.ecore.EcoreExecutorVoidType;
 import org.eclipse.ocl.examples.library.ecore.EcoreLibraryOppositeProperty;
 import org.eclipse.ocl.examples.library.executor.ExecutorFragment;
 import org.eclipse.ocl.examples.library.executor.ExecutorOperation;
 import org.eclipse.ocl.examples.library.executor.ExecutorProperty;
 import org.eclipse.ocl.examples.library.executor.ExecutorPropertyWithImplementation;
 import org.eclipse.ocl.examples.library.executor.ExecutorStandardLibrary;
 import org.eclipse.ocl.examples.library.executor.ExecutorType;
 import org.eclipse.ocl.examples.library.executor.ExecutorTypeParameter;
 import org.eclipse.ocl.examples.pivot.Enumeration;
 import org.eclipse.ocl.examples.pivot.EnumerationLiteral;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.OrderedSetType;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.SequenceType;
 import org.eclipse.ocl.examples.pivot.SetType;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeTemplateParameter;
 import org.eclipse.ocl.examples.pivot.VoidType;
import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 public class GenerateTables extends GenerateTablesUtils
 {
 	protected final boolean useNullAnnotations;
 	
 	public GenerateTables(@NonNull GenModel genModel) {
 		super(genModel);
 		this.useNullAnnotations = OCLGenModelGeneratorAdapter.useNullAnnotations(genModel);
 	}
 
 	protected void appendConstants(@NonNull String constants) {
 		s.append("	/**\n");
 		s.append("	 *	Constants used by auto=generated code.\n");
 		s.append("	 */\n");
 		int i = 0;
 		while (i < constants.length()) {
 			int j = constants.indexOf("<%", i);
 			if (j >= 0) {
 				int k = constants.indexOf("%>", j+2);
 				if (k >= 0) {
 					s.append(constants.substring(i, j));
 					@SuppressWarnings("null")@NonNull String referencedClass = constants.substring(j+2, k);
 					s.appendClassReference(referencedClass);
 					i = k+2;
 				}
 				else {
 					break;
 				}
 			}
 			else {
 				break;
 			}
 		}
 		s.append(constants.substring(i));
 	}
 
 	protected void appendTypeFlags(@NonNull Type type) {
 		if (type instanceof OrderedSetType) {
 			s.appendClassReference(ExecutorType.class);
 			s.append(".ORDERED | ");
 			s.appendClassReference(ExecutorType.class);
 			s.append(".UNIQUE");
 		}
 		else if (type instanceof SetType) {
 			s.appendClassReference(ExecutorType.class);
 			s.append(".UNIQUE");
 		}
 		else if (type instanceof SequenceType) {
 			s.appendClassReference(ExecutorType.class);
 			s.append(".ORDERED");
 		}
 		else {
 			s.append("0");
 		}
 	}
 
 	protected void appendUpperName(@NonNull NamedElement namedElement) {
 		s.append(DomainUtil.nonNullModel(CodeGenUtil.upperName(namedElement.getName())));
 	}
 	
 	protected @NonNull String atNonNull() {
 		if (useNullAnnotations) {
 			s.addClassReference("NonNull", "org.eclipse.jdt.annotation.NonNull");
 			return "@NonNull";
 		}
 		else {
 			return "/*@NonNull*/";
 		}
 	}
 
 	protected void declareEnumerationLiterals() {
 		s.append("	/**\n");
 		s.append("	 *	The lists of enumeration literals for each enumeration.\n");
 		s.append("	 */\n");
 		s.append("	public static class EnumerationLiterals {");
 		for (org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			if (pClass instanceof Enumeration) {
 				s.append("\n");
 				List<EnumerationLiteral> enumerationLiterals = ((Enumeration)pClass).getOwnedLiteral();
 				for (int i = 0; i < enumerationLiterals.size(); i++) {
 					EnumerationLiteral enumerationLiteral = DomainUtil.nonNullModel(enumerationLiterals.get(i));
 					s.append("		public static final " + atNonNull() + " ");
 					s.appendClassReference(EcoreExecutorEnumerationLiteral.class);
 					s.append(" _");
 					s.appendName(pClass);
 					s.append("__");
 					s.appendName(enumerationLiteral);
 					s.append(" = new ");
 					s.appendClassReference(EcoreExecutorEnumerationLiteral.class);
 					s.append("(");
 					s.append(genPackage.getPrefix() + "Package.Literals.");
 					appendUpperName(pClass);
 					s.append(".getEEnumLiteral(");
 					s.appendString(DomainUtil.nonNullModel(enumerationLiteral.getName()));
 					s.append("), Types._");
 					s.appendName(pClass);
 					s.append(", " + i + ");\n");
 				}
 				s.append("		private static final " + atNonNull() + " ");
 				s.appendClassReference(EcoreExecutorEnumerationLiteral.class);
 				s.append("[] _");
 				s.appendName(pClass);
 				s.append(" = {");
 				for (int i = 0; i < enumerationLiterals.size(); i++) {
 					EnumerationLiteral enumerationLiteral = DomainUtil.nonNullModel(enumerationLiterals.get(i));
 					if (i > 0) {
 						s.append(",");
 					}
 					s.append("\n");
 					s.append("			_");
 					s.appendName(pClass);
 					s.append("__");
 					s.appendName(enumerationLiteral);
 				}
 				s.append("\n");
 				s.append("		};\n");
 			}
 		}
 		s.append("\n");
 		s.append("		/**\n");
 		s.append("		 *	Install the enumeration literals in the enumerations.\n");
 		s.append("		 */\n");
 		s.append("		static {\n");
 		for (org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			if (pClass instanceof Enumeration) {
 				s.append("			Types._");
 				s.appendName(pClass);
 				s.append(".initLiterals(_");
 				s.appendName(pClass);
 				s.append(");\n");
 			}
 		}
 		s.append("		}\n");
 		s.append("\n");
 		s.append("		public static void init() {}\n");
 		s.append("	}\n");
 	}
 
 	protected void declareFragments() {
 		s.append("	/**\n");
 		s.append("	 *	The fragment descriptors for the local elements of each type and its supertypes.\n");
 		s.append("	 */\n");
 		s.append("	public static class Fragments {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			s.append("\n");
 			for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pSuperClass : getAllSupertypesSortedByName(pClass)) {
 				assert pSuperClass != null;
 				s.append("		public static final " + atNonNull() + " ");
 				s.appendClassReference(ExecutorFragment.class);
 				s.append(" _");
 				s.appendName(pClass);
 				s.append("__");
 				s.appendName(pSuperClass);
 				s.append(" = new ");
 				s.appendClassReference(ExecutorFragment.class);
 				s.append("(");
 				pClass.accept(emitLiteralVisitor);
 				s.append(", ");
 				pSuperClass.accept(emitQualifiedLiteralVisitor);
 				s.append(");\n");
 			}
 		}
 		s.append("	}\n");
 	}
 
 	protected void declareFragmentOperations() {
 		s.append("	/**\n");
 		s.append("	 *	The lists of local operations or local operation overrides for each fragment of each type.\n");
 		s.append("	 */\n");
 		s.append("	public static class FragmentOperations {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			List<Operation> sortedOperations = new ArrayList<Operation>(getOperations(pClass));
 			Collections.sort(sortedOperations, signatureComparator);
 			s.append("\n");
 			s.append("		private static final " + atNonNull() + " ");
 			s.appendClassReference(ExecutorOperation.class);
 			s.append("[] _");
 			s.appendName(pClass);
 			s.append("__");
 			s.appendName(pClass);
 			s.append(" = ");
 			if (sortedOperations.size() <= 0) {
 				s.append("{};\n");
 			}
 			else {
 				s.append("{");
 				for (int i = 0; i < sortedOperations.size(); i++) {
 					Operation op = sortedOperations.get(i);
 					if (i > 0) {
 						s.append(",");
 					}
 					s.append("\n");
 					s.append("			");
 					op.accept(emitQualifiedLiteralVisitor);
 					s.append(" /* ");
 					s.append(getSignature(op));
 					s.append(" */");
 				}
 				s.append("\n");
 				s.append("		};\n");
 			}
 			for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pSuperClass : getAllProperSupertypesSortedByName(pClass)) {
 				assert pSuperClass != null;
 				List<Operation> sortedSuperOperations = new ArrayList<Operation>(getOperations(pSuperClass));
 				Collections.sort(sortedSuperOperations, signatureComparator);
 				s.append("		private static final " + atNonNull() + " ");
 				s.appendClassReference(ExecutorOperation.class);
 				s.append("[] _");
 				s.appendName(pClass);
 				s.append("__");
 				s.appendName(pSuperClass);
 				s.append(" = ");
 				if (sortedSuperOperations.size() <= 0) {
 					s.append("{};\n");
 				}
 				else {
 					s.append("{");
 					for (int i = 0; i < sortedSuperOperations.size(); i++) {
 						Operation op = DomainUtil.nonNullModel(sortedSuperOperations.get(i));
 						Operation overloadOp = getOverloadOp(pClass, op);
 						if (i > 0) {
 							s.append(",");
 						}
 						s.append("\n");
 						s.append("			");
 						overloadOp.accept(emitQualifiedLiteralVisitor);
 						s.append(" /* ");
 						s.append(getSignature(overloadOp));
 						s.append(" */");
 					}
 					s.append("\n");
 					s.append("		};\n");
 				}
 			}
 		}
 		s.append("\n");
 		s.append("		/*\n");
 		s.append("		 *	Install the operation descriptors in the fragment descriptors.\n");
 		s.append("		 */\n");
 		s.append("		static {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			s.append("\n");
 			for (org.eclipse.ocl.examples.pivot.Class pSuperClass : getAllSupertypesSortedByName(pClass)) {
 				assert pSuperClass != null;
 				s.append("			Fragments._");
 				s.appendName(pClass);
 				s.append("__");
 				s.appendName(pSuperClass);
 				s.append(".initOperations(_");
 				s.appendName(pClass);
 				s.append("__");
 				s.appendName(pSuperClass);
 				s.append(");\n");
 			}
 		}
 		s.append("		}\n");
 		s.append("\n");
 		s.append("		public static void init() {}\n");
 		s.append("	}\n");
 	}
 	
 	public void declareFragmentProperties() {
 		s.append("	/**\n");
 		s.append("	 *	The lists of local properties for the local fragment of each type.\n");
 		s.append("	 */\n");
 		s.append("	public static class FragmentProperties {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			Set<Property> allProperties = new HashSet<Property>();
 			for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pSuperClass : getAllSupertypesSortedByName(pClass)) {
 				assert pSuperClass != null;
 				for (/*@NonNull*/ Property prop : getLocalPropertiesSortedByName(pSuperClass)) {
 					assert prop != null;
 					if (isProperty(prop)) {
 						allProperties.add(prop);
 					}
 				}
 			}
 			List<Property> sortedProperties = new ArrayList<Property>(allProperties);
 			Collections.sort(sortedProperties, nameComparator);
 			s.append("\n");
 			s.append("		private static final " + atNonNull() + " ");
 			s.appendClassReference(ExecutorProperty.class);
 			s.append("[] _");
 			s.appendName(pClass);
 			s.append(" = ");
 			if (sortedProperties.size() <= 0) {
 				s.append("{};\n");
 			}
 			else {
 				s.append("{");
 				for (int i = 0; i < sortedProperties.size(); i++) {
 					Property prop = sortedProperties.get(i);
 					if (i > 0) {
 						s.append(",");
 					}
 					s.append("\n");
 					s.append("			");
 					prop.accept(emitQualifiedLiteralVisitor);
 				}
 				s.append("\n");
 				s.append("		};\n");
 			}
 		}
 		s.append("\n");
 		s.append("		/**\n");
 		s.append("		 *	Install the property descriptors in the fragment descriptors.\n");
 		s.append("		 */\n");
 		s.append("		static {\n");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			s.append("			Fragments._");
 			s.appendName(pClass);
 			s.append("__");
 			s.appendName(pClass);
 			s.append(".initProperties(_");
 			s.appendName(pClass);
 			s.append(");\n");
 		}
 		s.append("		}\n");
 		s.append("\n");
 		s.append("		public static void init() {}\n");
 		s.append("	}\n");
 	}
 
 	protected void declareOperations() {
 		s.append("	/**\n");
 		s.append("	 *	The operation descriptors for each operation of each type.\n");
 		s.append("	 */\n");
 		s.append("	public static class Operations {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			List<Operation> sortedOperations = new ArrayList<Operation>(getOperations(pClass));
 			Collections.sort(sortedOperations, signatureComparator);
 			for (int i = 0; i < sortedOperations.size(); i++) {
 				if (i == 0) {
 					s.append("\n");
 				}
 				Operation op = sortedOperations.get(i);
 				TemplateSignature ownedTemplateSignature = op.getOwnedTemplateSignature();
 				s.append("		public static final " + atNonNull() + " ");
 				s.appendClassReference(ExecutorOperation.class);
 				s.append(" ");
 				op.accept(emitLiteralVisitor);
 				s.append(" = new ");
 				s.appendClassReference(ExecutorOperation.class);
 				s.append("(");
 				s.appendString(DomainUtil.nonNullModel(op.getName()));
 				s.append(", Parameters.");
 				s.append(getTemplateBindingsName(op.getParameterTypes()));
 				s.append(", ");
 				op.getOwningType().accept(emitLiteralVisitor);
 				s.append(",\n			" + i + ", ");
 				if (ownedTemplateSignature == null) {
 					s.appendClassReference(DomainTypeParameters.class);
 					s.append(".EMPTY_LIST");
 				}
 				else {
 					s.append("new ");
 					s.appendClassReference(DomainTypeParameters.class);
 					s.append("(");
 					for (TemplateParameter parameter : ownedTemplateSignature.getParameter()) {
 						ParameterableElement parameteredElement = parameter.getParameteredElement();
 						if (parameteredElement instanceof Nameable) {
 							s.append("TypeParameters._");
 							op.accept(emitLiteralVisitor);
 							s.append("_");
 							s.appendName((NamedElement)parameteredElement);
 						}
 					}
 					s.append(")");
 				}
 				s.append(", ");
 				s.append(getImplementationName(op));
 				s.append(");\n");
 			}
 		}
 		s.append("\n");
 		s.append("	}\n");
 	}
 
 	protected void declareParameterLists() {
 		Set<DomainParameterTypes> allLists = new HashSet<DomainParameterTypes>();
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			for (Operation operation : getOperations(pClass)) {
 				allLists.add(operation.getParameterTypes());
 			}
 		}
 		s.append("	/**\n");
 		s.append("	 *	The parameter lists shared by operations.\n");
 		s.append("	 */\n");
 		s.append("	public static class Parameters {\n");
 		List<DomainParameterTypes> sortedLists = new ArrayList<DomainParameterTypes>(allLists);
 		Collections.sort(sortedLists, templateBindingNameComparator);
 		for (/*@NonNull*/ DomainParameterTypes types : sortedLists) {
 			assert types != null;
 			s.append("		public static final " + atNonNull() + " ");
 			s.appendClassReference(DomainParameterTypes.class);
 			s.append(" ");
 			s.append(getTemplateBindingsName(types));
 			s.append(" = new ");
 			s.appendClassReference(DomainParameterTypes.class);
 			s.append("(");
 			for (int i = 0; i < types.size(); i++) {
 				if (i > 0) {
 					s.append(", ");
 				}
				Type type = PivotUtil.getType((Type)types.get(i));
				type.accept(declareParameterTypeVisitor);				
 			}
 			s.append(");\n");
 		}
 		s.append("	}\n");
 	}
 
 	protected void declareProperties() {
 		s.append("	/**\n");
 		s.append("	 *	The property descriptors for each property of each type.\n");
 		s.append("	 */\n");
 		s.append("	public static class Properties {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			boolean isFirst = true;
 			List<Property> sortedProperties = getLocalPropertiesSortedByName(pClass);
 			assert pClass != null;
 			for (int i = 0; i < sortedProperties.size(); i++) {
 				Property prop = DomainUtil.nonNullModel(sortedProperties.get(i));
 				if (isProperty(prop)) {
 					s.append("\n");
 					if (isFirst) {
 						s.append("\n");
 					}
 					isFirst = false;
 					s.append("		public static final " + atNonNull() + " ");
 					s.appendClassReference(ExecutorProperty.class);
 					s.append(" ");
 					prop.accept(emitLiteralVisitor);
 					s.append(" = new ");
 					String name = DomainUtil.nonNullModel(prop.getName());
 					if (prop.getImplementationClass() != null) {
 						s.appendClassReference(ExecutorPropertyWithImplementation.class);
 						s.append("(");
 						s.appendString(name);
 						s.append(", " );
 						pClass.accept(emitLiteralVisitor);
 						s.append(", " + i + ", ");
 						s.append(prop.getImplementationClass());
 						s.append(".INSTANCE)");
 					}
 					else if (hasEcore(prop)) {
 //					    List<Constraint> constraints = prop.getOwnedRule();
 						Type owningType = DomainUtil.nonNullModel(prop.getOwningType());
 /*						if (constraints.size() > 0) {
 							s.appendClassReference(ExecutorPropertyWithImplementation.class);
 							s.append("(");
 							s.appendString(name);
 							s.append(", " );
 							pClass.accept(emitLiteralVisitor);
 							s.append(", " + i + ", ");
 							s.append(getQualifiedBodiesClassName(owningType));
 							s.append("._");
 							s.appendName(prop);
 							s.append("_");
 							s.append(constraints.get(0).getStereotype());
 							s.append("_.INSTANCE)");
 						}
 						else { */
 							s.appendClassReference(EcoreExecutorProperty.class);
 							s.append("(");
 							s.append(genPackage.getPrefix());
 							s.append("Package.Literals." );
 							appendUpperName(owningType);
 							s.append("__" );
 							appendUpperName(prop);
 							s.append(", " );
 							pClass.accept(emitLiteralVisitor);
 							s.append(", " + i + ")");
 //						}
 					} else {
 						Property opposite = prop.getOpposite();
 						if ((opposite != null) && hasEcore(opposite)) {
 							s.appendClassReference(ExecutorPropertyWithImplementation.class);
 							s.append("(");
 							s.appendString(name);
 							s.append(", " );
 							pClass.accept(emitLiteralVisitor);
 							s.append(", " + i + ", new ");
 							s.appendClassReference(EcoreLibraryOppositeProperty.class);
 							s.append("(");
 							s.append(genPackage.getPrefix());
 							s.append("Package.Literals." );
 							appendUpperName(DomainUtil.nonNullModel(opposite.getOwningType()));
 							s.append("__" );
 							appendUpperName(opposite);
 							s.append("))");
 						}
 						else {
 							s.appendClassReference(ExecutorPropertyWithImplementation.class);
 							s.append("(");
 							s.appendString(name);
 							s.append(", " );
 							pClass.accept(emitLiteralVisitor);
 							s.append(", " + i + ", null)");
 						}
 					}
 					s.append(";");
 				}
 			}
 		}
 		s.append("\n");
 		s.append("	}\n");
 	}
 
 	protected void declareType(@NonNull org.eclipse.ocl.examples.pivot.Class pClass) {
 		Class<?> typeClass =
 				pClass instanceof Enumeration ? EcoreExecutorEnumeration.class :
 				pClass instanceof InvalidType ? EcoreExecutorInvalidType.class :
 				pClass instanceof VoidType ? EcoreExecutorVoidType.class :
 				EcoreExecutorType.class;
 		s.append("		public static final " + atNonNull() + " ");
 		s.appendClassReference(typeClass);
 		s.append(" _");
 		s.appendName(pClass);
 		s.append(" = ");
 		if (!hasEcore(pClass)) {
 			s.append("new ");
 			s.appendClassReference(typeClass);
 			s.append("(");
 			if (isBuiltInType(pClass)) {
 				s.appendClassReference(TypeId.class);
 				s.append(".");
 				appendUpperName(pClass);
 			}
 			else {
 				s.appendString(DomainUtil.nonNullModel(pClass.getName()));
 			}
 		}
 		else {
 			s.append("new ");
 			s.appendClassReference(typeClass);
 			s.append("(" + genPackage.getPrefix() + "Package.Literals.");
 			appendUpperName(pClass);
 		}
 		s.append(", PACKAGE, ");
 		appendTypeFlags(pClass);
 		if (pClass.getOwnedTemplateSignature() != null) {
 			for (TemplateParameter parameter : pClass.getOwnedTemplateSignature().getParameter()) {
 				if (parameter instanceof TypeTemplateParameter) {
 					Type parameteredElement = DomainUtil.nonNullModel((Type) parameter.getParameteredElement());
 					s.append(", TypeParameters._");
 					s.appendName(pClass);
 					s.append("_");
 					s.appendName(parameteredElement);
 				}
 			}
 		}
 		s.append(");\n");
 	}
 
 	protected void declareTypes() {
 		s.append("	/**\n");
 		s.append("	 *	The type descriptors for each type.\n");
 		s.append("	 */\n");
 		s.append("	public static class Types {\n");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			declareType(pClass);
 		}
 		s.append("\n");
 		s.append("		private static final " + atNonNull() + " ");
 		s.appendClassReference(EcoreExecutorType.class);
 		s.append("[] types = {");
 		boolean isFirst = true;
 		boolean hasEnumeration = false;
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			if (pClass instanceof Enumeration) {
 				hasEnumeration = true;
 			}
 			if (!isFirst) {
 				s.append(",");
 			}
 			isFirst = false;
 			s.append("\n");
 			s.append("			_");
 			s.appendName(pClass);
 		}
 		s.append("\n");
 		s.append("		};\n");
 		s.append("\n");
 		s.append("		/*\n");
 		s.append("		 *	Install the type descriptors in the package descriptor.\n");
 		s.append("		 */\n");
 		s.append("		static {\n");
 		s.append("			PACKAGE.init(LIBRARY, types);\n");
 		org.eclipse.ocl.examples.pivot.Package extendedPackage = getExtendedPackage(pPackage);
 		if (extendedPackage != null) {
 			s.append("			LIBRARY.addExtension(");
 			s.appendClassReference(getQualifiedTablesClassName(extendedPackage));
 			s.append(".PACKAGE, PACKAGE);\n");
 		}
 		s.append("			TypeFragments.init();\n");
 		s.append("			FragmentOperations.init();\n");
 		s.append("			FragmentProperties.init();\n");
 		if (hasEnumeration) {
 			s.append("			EnumerationLiterals.init();\n");
 		}
 		s.append("		}\n");
 		s.append("	}\n");
 	}
 	
 	protected void declareTypeFragments() {
 		s.append("	/**\n");
 		s.append("	 *	The fragments for all base types in depth order: OclAny first, OclSelf last.\n");
 		s.append("	 */\n");
 		s.append("	public static class TypeFragments {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			final Map<org.eclipse.ocl.examples.pivot.Class, Integer> allSuperTypes = new HashMap<org.eclipse.ocl.examples.pivot.Class, Integer>();
 			int myDepth = getAllSuperClasses(allSuperTypes, pClass);
 			int[] typesPerDepth = new int[myDepth+1];
 			for (int i = 0; i <= myDepth; i++) {
 				typesPerDepth[i] = 0;
 			}
 			for (Integer aDepth : allSuperTypes.values()) {
 				typesPerDepth[aDepth]++;
 			}
 			List<Type> superTypes = new ArrayList<Type>(allSuperTypes.keySet());
 			Collections.sort(superTypes, new Comparator<Type>()
 			{
 				public int compare(Type o1, Type o2) {
 					Integer d1 = allSuperTypes.get(o1);
 					Integer d2 = allSuperTypes.get(o2);
 					if (d1 != d2) {
 						return d1.compareTo(d2);
 					}
 					String n1 = o1.getName();
 					String n2 = o2.getName();
 					return n1.compareTo(n2);
 				}
 			});
 			s.append("\n");
 			s.append("		private static final " + atNonNull() + " ");
 			s.appendClassReference(ExecutorFragment.class);
 			s.append("[] _");
 			s.appendName(pClass);
 			s.append(" =\n");
 			s.append("		{");
 			boolean isFirst = true;
 			for (/*@NonNull*/ Type superClass : superTypes) {
 				assert superClass != null;
 				if (!isFirst) {
 					s.append(",");
 				}
 				s.append("\n");
 				s.append("			Fragments._");
 				s.appendName(pClass);
 				s.append("__");
 				s.appendName(superClass);
 				s.append(" /* " + allSuperTypes.get(superClass) + " */");
 				isFirst = false;
 			}
 			s.append("\n");
 			s.append("		};\n");
 			s.append("		private static final " + atNonNull() + " int[] __");
 			s.appendName(pClass);
 			s.append(" = { ");
 			for (int i = 0; i <= myDepth; i++) {
 				if (i > 0) {
 					s.append(",");
 				}
 				s.append(Integer.toString(typesPerDepth[i]));
 			}
 			s.append(" };\n");
 		}
 		s.append("\n");
 		s.append("		/**\n");
 		s.append("		 *	Install the fragment descriptors in the class descriptors.\n");
 		s.append("		 */\n");
 		s.append("		static {\n");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			assert pClass != null;
 			s.append("			");
 			pClass.accept(emitLiteralVisitor);
 			s.append(".initFragments(_");
 			s.appendName(pClass);
 			s.append(", __");
 			s.appendName(pClass);
 //			if (hasAnotherType(pClass)) {
 //				s.append(", " + genPackage.getPrefix() + "Package.Literals.");
 //				appendUpperName(pClass);
 //			}
 			s.append(");\n");
 		}
 		s.append("		}\n");
 		s.append("\n");
 		s.append("		public static void init() {}\n");
 		s.append("	}\n");
 	}
 		
 	protected void declareTypeParameters() {
 		s.append("	/**\n");
 		s.append("	 *	The type parameters for templated types and operations.\n");
 		s.append("	 */\n");
 		s.append("	public static class TypeParameters {");
 		for (/*@NonNull*/ org.eclipse.ocl.examples.pivot.Class pClass : activeClassesSortedByName) {
 			TemplateSignature templateSignature = pClass.getOwnedTemplateSignature();
 			if (templateSignature != null) {
 				s.append("\n");
 				for (TemplateParameter parameter : templateSignature.getParameter()) {
 					if (parameter instanceof TypeTemplateParameter) {
 						Type parameteredElement = DomainUtil.nonNullModel((Type) parameter.getParameteredElement());
 						s.append("		public static final " + atNonNull() + " ");
 						s.appendClassReference(ExecutorTypeParameter.class);
 						s.append(" _");
 						s.appendName(pClass);
 						s.append("_");
 						s.appendName(parameteredElement);
 						s.append(" = new ");
 						s.appendClassReference(ExecutorTypeParameter.class);
 						s.append("(");
 						if (isBuiltInType(pClass)) {
 							s.appendClassReference(TypeId.class);
 							s.append(".");
 							appendUpperName(pClass);
 							s.append("_T, ");
 						}
 						s.append("LIBRARY, ");
 						s.appendString(DomainUtil.nonNullModel(parameteredElement.getName()));
 						s.append(");\n");
 					}
 				}
 			}
 			for (/*@NonNull*/ Operation operation : getLocalOperationsSortedBySignature(pClass)) {
 				assert operation != null;
 				templateSignature = operation.getOwnedTemplateSignature();
 				if (templateSignature != null) {
 					for (/*@NonNull*/ TemplateParameter parameter : templateSignature.getParameter()) {
 						if (parameter instanceof TypeTemplateParameter) {
 							Type parameteredElement = DomainUtil.nonNullModel((Type) parameter.getParameteredElement());
 							s.append("		public static final " + atNonNull() + " ");
 							s.appendClassReference(ExecutorTypeParameter.class);
 							s.append(" _");
 							operation.accept(emitLiteralVisitor);
 							s.append("_");
 							s.appendName(parameteredElement);
 							s.append(" = new ");
 							s.appendClassReference(ExecutorTypeParameter.class);
 							s.append("(LIBRARY, ");
 							s.appendString(DomainUtil.nonNullModel(parameteredElement.getName()));
 							s.append(");\n");
 						}
 					}
 				}
 			}
 		}
 		s.append("	}\n");
 	}
 
 	public @NonNull String generateTablesClass(@Nullable String constants) {
 		String tablesClassName = getTablesClassName(genPackage);
 		s.append("/**\n");
 		s.append(" * " + tablesClassName + " provides the dispatch tables for the " + pPackage.getName() + " for use by the OCL dispatcher.\n");
 		s.append(" *\n");
 		s.append(" * In order to ensure correct static initialization, a top level class element must be accessed\n");
 		s.append(" * before any nested class element. Therefore an access to PACKAGE.getClass() is recommended.\n");
 		s.append(" */\n");
 		s.append("@SuppressWarnings(\"nls\")\n");
 		s.append("public class " + tablesClassName + "\n");
 		s.append("{\n");
 		s.append("	/**\n");
 		s.append("	 *	The package descriptor for the package.\n");
 		s.append("	 */\n");
 		
 		s.append("	public static final ");
 		s.append(atNonNull());
 		s.append(" ");
 		s.appendClassReference(EcoreExecutorPackage.class);
 		s.append(" PACKAGE = new ");
 		s.appendClassReference(EcoreExecutorPackage.class);
 		s.append("(" + genPackage.getPrefix() + "Package.eINSTANCE);\n");
 		
 		s.append("\n");
 		s.append("	/**\n");
 		s.append("	 *	The library of all packages and types.\n");
 		s.append("	 */\n");
 		
 		s.append("	public static final " + atNonNull() + " ");
 		s.appendClassReference(ExecutorStandardLibrary.class);
 		s.append(" LIBRARY = ");
 		if (hasSharedLibrary()) {
 			s.appendClassReference(getSharedLibrary());
 			s.append(".LIBRARY");
 		}
 		else {
 			s.append("new ");
 			s.appendClassReference(ExecutorStandardLibrary.class);
 			s.append("()");
 		}
 		s.append(";\n");
 		
 		if (constants != null) {
 			s.append("\n");
 			appendConstants(constants);
 		}
 		
 		s.append("\n");
 		declareTypeParameters();
 		s.append("\n");
 		declareTypes();
 		s.append("\n");
 		declareFragments();
 		s.append("\n");
 		declareParameterLists();
 		s.append("\n");
 		declareOperations();
 		s.append("\n");
 		declareProperties();
 		s.append("\n");
 		declareTypeFragments();
 		s.append("\n");
 		declareFragmentOperations();
 		s.append("\n");
 		declareFragmentProperties();
 		s.append("\n");
 		declareEnumerationLiterals();
 		s.append("\n");
 		s.append("	static {\n");
 		s.append("		Types.types[0].getClass();\n");
 		s.append("	}\n");
 		s.append("}\n");
 		return s.toString();
 	}
 	
 	@Override
 	public @NonNull String toString() {
 		String copyright = genPackage.getCopyright(" * ");
 		StringBuilder s1 = new StringBuilder();
 		s1.append("/**\n");
 		if (copyright != null) {
 			s1.append(" * ");
 			s1.append(copyright.replace("\r", ""));
 			s1.append("\n");
 		}
 		s1.append(" *************************************************************************\n");
 		s1.append(" * This code is 100% auto-generated\n");
 		s1.append(" * from: " + pPackage.getName() + "\n");
 		s1.append(" * using: " + getClass().getName() + "\n");
 		s1.append(" *\n");
 		s1.append(" * Do not edit it.\n");
 		s1.append(" */\n");
 		
 		s1.append("package ");
 		s1.append(genPackage.getQualifiedPackageName());
 		s1.append(";\n");
 		
 		s1.append("\n");
 		for (String classReference : s.getClassReferences()) {
 			s1.append("import ");
 			s1.append(classReference);
 			s1.append(";\n");
 		}
 		s1.append("\n");
 		s1.append(s.toString());
 		@SuppressWarnings("null")@NonNull String string = s1.toString();
 		return string;
 	}
 }
