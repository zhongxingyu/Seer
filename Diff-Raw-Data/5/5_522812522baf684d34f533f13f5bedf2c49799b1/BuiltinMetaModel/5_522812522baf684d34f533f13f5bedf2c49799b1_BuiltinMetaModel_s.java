 /*******************************************************************************
  * Copyright (c) 2006, 2007 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.gmf.internal.xpand;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.emf.common.util.Enumerator;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.ETypedElement;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.gmf.internal.xpand.expression.PolymorphicResolver;
 import org.eclipse.gmf.internal.xpand.model.XpandDefinitionWrap;
 import org.eclipse.gmf.internal.xpand.model.XpandIterator;
 
 /**
  * XXX Guess, will need special support to recognize the fact
  * EJavaObject.isSupertypeOf(EObject)
  * 
  * @author artem
  */
 @SuppressWarnings("unchecked")
 public class BuiltinMetaModel {
     public final static String SET = "Set";
     public final static String LIST = "List";
 
 	
 	private static EPackage XECORE = EcoreFactory.eINSTANCE.createEPackage();
 	
 	static {
 		XECORE.setName("xecore");
 		XECORE.setNsPrefix("xecore");
 		XECORE.setNsURI("uri:org.eclipse.modeling/m2t/xpand/xecore/1.0");
 	}
 
 	private static EClass PARAMETERIZED_TYPE = EcoreFactory.eINSTANCE.createEClass();
 	private static EReference PT_INNER_TYPE_REF = EcoreFactory.eINSTANCE.createEReference();
 	private static EAttribute PT_INNER_TYPE_ATTR = EcoreFactory.eINSTANCE.createEAttribute();
 
 	static {
 		PARAMETERIZED_TYPE.setName("ParameterizedType");
 		PARAMETERIZED_TYPE.getESuperTypes().add(EcorePackage.eINSTANCE.getEClass());
 		PARAMETERIZED_TYPE.setAbstract(true);
 		PT_INNER_TYPE_REF.setName("innerType");
 		PT_INNER_TYPE_REF.setContainment(false);
 		PT_INNER_TYPE_REF.setEType(EcorePackage.eINSTANCE.getEClass());
 
 		PARAMETERIZED_TYPE.getEStructuralFeatures().add(PT_INNER_TYPE_REF);
 
 		PT_INNER_TYPE_ATTR.setName("innerDataType");
 		PT_INNER_TYPE_ATTR.setEType(EcorePackage.eINSTANCE.getEDataType());
 		PARAMETERIZED_TYPE.getEStructuralFeatures().add(PT_INNER_TYPE_ATTR);
 		XECORE.getEClassifiers().add(PARAMETERIZED_TYPE);
 	}
 
 	/**
 	 * Checks whether classifier is one of user's model extension classes
 	 * (conforming to ParameterizedType from our extended ECore meta-model).
 	 * EClassifier instances available in analyze() methods are param
 	 * candidates.
 	 * 
 	 * @param parameterizedTypeM1 -
 	 *            e.g. EClass "Order", or XEClass "OrderList"
 	 */
 	public static boolean isParameterizedType(EClassifier parameterizedTypeM1) {
 		return PARAMETERIZED_TYPE.isSuperTypeOf(parameterizedTypeM1.eClass());
 	}
 
 	// XXX revisit invocations, this check is doubled with isParameterizedType, perhaps, can refactor it 
 	public static boolean isCollectionType(EClassifier parameterizedTypeM1) {
 		// XXX this implementation is not really 'isCollectionType', it's just a copy of what was in the original code
 		return isAssignableFrom(CollectionTypesSupport.COLLECTION_OF_OBJECT, parameterizedTypeM1);
 	}
 
 	public static EClassifier getInnerType(EClassifier parameterizedTypeM1) {
 		assert isParameterizedType(parameterizedTypeM1);
 		if (parameterizedTypeM1.eIsSet(PT_INNER_TYPE_REF)) {
 			return (EClass) parameterizedTypeM1.eGet(PT_INNER_TYPE_REF);
 		} else {
 			return (EDataType) parameterizedTypeM1.eGet(PT_INNER_TYPE_ATTR);
 		}
 	}
 
 	/**
 	 * NOTE, parameterizedTypeM1 is M1 instance, you can't pass {@link BuiltinMetaModel#COLLECTION_TYPE} (or {@link BuiltinMetaModel#LIST_TYPE}) here,
 	 * because COLLECTION_TYPE just extends PARAMETERIZED_TYPE, but still instance of EClass. We could, however, have COLLECTION_TYPE to be an
 	 * instance of PARAMETERIZED_TYPE, and then we could use this method. The reasons not to do so (at least, now) are
 	 * (a) didn't think it over yet (b) looks like extending M2 (sic!) dynamically, though I don't like even M1 polluting with type
 	 * that happens.
 	 * @param parameterizedTypeM1
 	 * @param innerTypeM1
 	 * @return
 	 */
 	public static EClass cloneParametrizedType(EClassifier parameterizedTypeM1, EClassifier innerTypeM1) {
 		assert isParameterizedType(parameterizedTypeM1);
 		return collectionTypes.getCollectionType(parameterizedTypeM1.eClass(), innerTypeM1);
 	}
 
 	/*package*/ static EClass internalNewParameterizedType(EClass parameterizedTypeM2, EClassifier inner) {
 		assert PARAMETERIZED_TYPE.isSuperTypeOf(parameterizedTypeM2);
 		EObject anInstance = XECORE.getEFactoryInstance().create(parameterizedTypeM2);
 		assert anInstance instanceof EClass : "EClass is first supertype with instanceClass set";
 		// e.g. "OrderCollection" or "IntegerList"
 		((EClass) anInstance).setName(inner.getName() + parameterizedTypeM2.getName());
 		anInstance.eSet(inner instanceof EClass ? PT_INNER_TYPE_REF : PT_INNER_TYPE_ATTR, inner);
 		return (EClass) anInstance;
 	}
 
 	public static final EClass VOID = EcoreFactory.eINSTANCE.createEClass();
 
 	static {
 		VOID.setName("void");
 		XECORE.getEClassifiers().add(VOID);
 	}
 
 	private static CollectionTypesSupport collectionTypes = new CollectionTypesSupport();
 
 	static {
 		collectionTypes.init(XECORE, PARAMETERIZED_TYPE);
 	}
 	/**
 	 * @param name
 	 * @return true if name is one of M2 collection meta-types (either Collection, List, Set)
 	 */
 	public static boolean isCollectionMetaType(String name) {
 		return collectionTypes.isCollectionMetaType(name);
 	}
 
 	public static EClass getCollectionType(String metaTypeName, EClassifier innerType) {
 		return collectionTypes.getCollectionType(metaTypeName, innerType);
 	}
 
 	// XXX actually, it's odd to use abstract and vague 'collection' 
 	public static EClass getCollectionType(EClassifier innerType) {
 		return collectionTypes.getCollectionType(innerType);
 	}
 	public static EClass getListType(EClassifier innerType) {
 		return collectionTypes.getListType(innerType);
 	}
 	public static EClass getSetType(EClassifier innerType) {
 		return collectionTypes.getSetType(innerType);
 	}
 
 	public static final EClass DEFINITION_TYPE = EcoreFactory.eINSTANCE.createEClass();
 
 	static {
 		DEFINITION_TYPE.setName("xpand2::Definition");
 		DEFINITION_TYPE.getESuperTypes().add(EcorePackage.eINSTANCE.getEClass());
 		XECORE.getEClassifiers().add(DEFINITION_TYPE);
 	}
 
 	public static final EClass ITERATOR_TYPE = EcoreFactory.eINSTANCE.createEClass();
 
 	static {
 		ITERATOR_TYPE.setName("xpand2::Iterator");
 		ITERATOR_TYPE.getESuperTypes().add(EcorePackage.eINSTANCE.getEClass());
 		XECORE.getEClassifiers().add(ITERATOR_TYPE);
 	}
 
 	/**
 	 * ECore doesn't support 'return types' for Enums, to my best knowledge,
 	 * they are all integers. original EEnumType returns itself as static
 	 * property's return type BTW, what if we'd like to get string value, name,
 	 * instead
 	 */
 	public static EClassifier getReturnType(EEnumLiteral sp) {
 		return sp.getEEnum();
 	}
 
 	// TODO obj.getClass lookup tree?
 	public static EClassifier getType(Object obj) {
 		if (obj == null) {
 			return VOID;
 		}
 		if (obj instanceof Enumerator) {
 			// unlike original impl, we don't return Enum as type
 			// mostly because it's just too hard to look for actual enum
 			// XXX perhaps, EEnumLiteral.getEEnum could help?
 			return EcorePackage.eINSTANCE.getEEnumerator();
 		}
 		if (obj instanceof EObject) {
 			return ((EObject) obj).eClass();
 		}
 		if (obj instanceof Collection) {
 			EClassifier type = null;
 			if (!((Collection) obj).isEmpty()) {
 				// FIXME respect all! elements in the collection, not only the first one
 				type = getType(((Collection) obj).iterator().next());
 			}
 			if (obj instanceof Set) {
 				return collectionTypes.getSetType(type);
 			}
 			if (obj instanceof List) {
 				return collectionTypes.getListType(type);
 			}
 			return collectionTypes.getCollectionType(type);
 		}
 		if (obj instanceof Boolean) {
 			return EcorePackage.eINSTANCE.getEBoolean();
 		}
 		if ((obj instanceof Byte) || (obj instanceof Integer) || (obj instanceof Long) || obj instanceof Short) {
 			return EcorePackage.eINSTANCE.getEInt();
 		}
 		if ((obj instanceof Float) || (obj instanceof Double)) {
 			return EcorePackage.eINSTANCE.getEDouble();
 		}
 		if (obj instanceof String) {
 			return EcorePackage.eINSTANCE.getEString();
 		}
 		if (obj instanceof XpandDefinitionWrap) {
 			return DEFINITION_TYPE;
 		}
 		if (obj instanceof XpandIterator) {
 			return ITERATOR_TYPE;
 		}
 		return EcorePackage.eINSTANCE.getEJavaObject();
 	}
 
 	/**
 	 * FIXME HACK!!!
 	 */
 	public static Object newInstance(EClassifier t) {
 		if (isCollectionType(t)) {
 			return collectionTypes.newInstance(t);
 		}
 		if (t.getInstanceClass() != null) {
 			try {
 				return t.getInstanceClass().newInstance();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * FIXME what if args has null or getType() returns null - we can't find an
 	 * op then, and it's caller who knows how to handle this.
 	 * 
 	 * @param name
 	 * @param args
 	 * @param instance
 	 * @return
 	 */
 	public static Operation executableOperation(String name, Object[] args, Object instance) {
 		EClassifier[] argTypes = new EClassifier[args.length];
 		for (int i = 0; i < args.length; i++) {
 			argTypes[i] = getType(args[i]);
 		}
 		EOperation metaOp = findOperation(getType(instance), name, argTypes);
 		if (metaOp == null) {
 			return null;
 		}
 		if (InternalOperation.isInternalOp(metaOp)) {
 			for (List<InternalOperation> ops : internalOperationsMap.values()) {
 				for (InternalOperation internalOp : ops) {
 					if (internalOp.metaOp == metaOp) {
 						return new OperationEx(instance, args, internalOp);
 					}
 				}
 			}
 			throw new IllegalStateException("Can't find implementation of built-in operation" + metaOp);
 		}
 		return new Operation(instance, args, metaOp);
 	}
 
 	public static EOperation findOperation(EClassifier targetType, String name, EClassifier[] args) {
 		List<EOperation> allOp;
 		if (hasBuiltinSupport(targetType)) {
 			// this one is to cover m2 types that are *propagated* to user's model, m1. Those like
 			// boolean, integer, string, etc.
 			allOp = findInternalOp(targetType);
 		} else if (hasBuiltinSupport(targetType.eClass())){
 			// this one is to cover collection types, because we register their operations
 			// against meta-model (m2) instance, rather than against M1 
 			allOp = findInternalOp(targetType.eClass());
 		} else {
 			if (false == (targetType instanceof EClass)) {
 				return null;
 			} else {
 				allOp = new LinkedList<EOperation>(((EClass) targetType).getEAllOperations());
 				allOp.addAll(findInternalOp(EcorePackage.eINSTANCE.getEJavaObject()));
 			}
 		}
 		return PolymorphicResolver.filterOperation(allOp, name, targetType, Arrays.asList(args));
 	}
 
 	private static Map<String, String> attrNameSubsts = new TreeMap<String, String>();
 	static {
 		attrNameSubsts.put("default_", "default");
 	}
 	public static EStructuralFeature getAttribute(EClassifier type, String name) {
 		if (hasBuiltinSupport(type)) {
 			return findInternalAttr(type, name);
 		}
 		if (type instanceof EClass) {
 			return ((EClass) type).getEStructuralFeature(attrNameSubsts.containsKey(name) ? attrNameSubsts.get(name): name);
 		}
 		if (type instanceof EEnum || type == EcorePackage.eINSTANCE.getEEnumLiteral() || type == EcorePackage.eINSTANCE.getEEnumerator()) {
 			return EcorePackage.eINSTANCE.getEEnumLiteral().getEStructuralFeature(name);
 		}
 		return null;
 	}
 	public static Object getValue(EStructuralFeature prop, Object instance) {
 		if (instance instanceof Enumerator) {
 			if (prop == EcorePackage.eINSTANCE.getEEnumLiteral_Literal()) {
 				return ((Enumerator) instance).getLiteral();
 			}
 			if (prop == EcorePackage.eINSTANCE.getENamedElement_Name()) {
 				return ((Enumerator) instance).getName();
 			}
 			if (prop == EcorePackage.eINSTANCE.getEEnumLiteral_Value()) {
 				return ((Enumerator) instance).getValue();
 			}
 		}
 		if (instance instanceof EObject) {
 			return ((EObject) instance).eGet(prop);
 		}
 		// handle collection/set/list properties?
 		return "HeyHo!";
 	}
 
 	private static boolean hasBuiltinSupport(EClassifier type) {
 		return internalOperationsMap.containsKey(type);
 	}
 
 	private static EStructuralFeature findInternalAttr(EClassifier type, String name) {
 		if (type instanceof EClass) {
 			return ((EClass) type).getEStructuralFeature(name);
 		}
 		if (type instanceof EDataType && type == EcorePackage.eINSTANCE.getEEnumerator()) {
 			// I do not know where EMF uses EEnumLiteralImpl and where Enumerator for EEnum value. 
 			return EcorePackage.eINSTANCE.getEEnumLiteral().getEStructuralFeature(name);
 		}
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	private static final Map<EClassifier,List<InternalOperation>>  internalOperationsMap = new HashMap<EClassifier, List<InternalOperation>>();
 
 	static {
 		final EcorePackage ecorePkg = EcorePackage.eINSTANCE;
 		final OperationFactory opf = new OperationFactory();
 
 		final List<InternalOperation> objectOps = new LinkedList<InternalOperation>();
 		objectOps.add(new InternalOperation<Object>(opf.create("compareTo", ecorePkg.getEBoolean(), ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(Object target, Object[] params) {
 				if (target == null) {
 					return params[0] == null ? 0 : -1;
 				}
 				if (params[0] == null) {
 					return 1;
 				}
 				if (target instanceof Comparable) {
 					return ((Comparable) target).compareTo(params[0]);
 				}
 				// note, unlike ObjectTypeImpl we don't invoke toString registered against metatype here
 				return String.valueOf(target).compareTo(String.valueOf(params[0]));
 			}
 		});
 		objectOps.add(new InternalOperation<Object>(opf.create("toString", ecorePkg.getEString())) {
 			@Override
 			public Object evaluate(Object target, Object[] params) {
 				return String.valueOf(target);
 			}
 		});
 		objectOps.add(new InternalOperation<Object>(opf.create("==", boolean.class, Object.class)) {
 			@Override
 			public Object evaluate(Object target, Object[] params) {
 				return target == null ? params[0] == null : target.equals(params[0]);
 			}
 		});
 		objectOps.add(new InternalOperation<Object>(opf.create("!=", boolean.class, Object.class)) {
 			@Override
 			public Object evaluate(Object target, Object[] params) {
 				return target == null ? params[0] != null : !target.equals(params[0]);
 			}
 		});
 		List<InternalOperation> unmodifiableObjectOps = Collections.unmodifiableList(objectOps);
 		internalOperationsMap.put(ecorePkg.getEJavaObject(), unmodifiableObjectOps);
 		internalOperationsMap.put(ecorePkg.getEEnumerator(), unmodifiableObjectOps);
 
 		final List<InternalOperation> stringOps = new LinkedList<InternalOperation>();
 
 		stringOps.add(new InternalOperation<String>(opf.create("+",ecorePkg.getEString(),ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(String target, Object[] params) {
 				return target + String.valueOf(params[0]);
 			}
 		});
 		stringOps.add(new InternalOperation<String>(opf.create("toFirstUpper",ecorePkg.getEString())) {
 			@Override
 			public Object evaluate(String target, Object[] params) {
 				return StringHelper.firstUpper(target);
 			}
 		});
 		stringOps.add(new InternalOperation<String>(opf.create("toFirstLower",ecorePkg.getEString())) {
 			@Override
 			public Object evaluate(String target, Object[] params) {
 				return StringHelper.firstLower(target);
 			}
 		});
 		stringOps.add(new InternalOperation<String>(opf.create("toCharList",collectionTypes.getListType(ecorePkg.getEString()))) {
 			@Override
 			public Object evaluate(String target, Object[] params) {
 				ArrayList<String> rv = new ArrayList<String>(target.length());
 				for (int i = 0; i < target.length(); i++) {
 					rv.add(target.substring(i, i+1));
 				}
 				return rv;
 			}
 		});
 		stringOps.add(opf.createReflective(String.class, "startsWith", String.class));
 		stringOps.add(opf.createReflective(String.class, "endsWith", String.class));
 		InternalOperation subStringOp = opf.createReflective(String.class, "substring", int.class, int.class);
 		subStringOp.metaOp.setName("subString");
 		stringOps.add(subStringOp);
 		subStringOp = opf.createReflective(String.class, "substring", int.class);
 		subStringOp.metaOp.setName("subString");
 		stringOps.add(subStringOp);
 		stringOps.add(opf.createReflective(String.class, "toUpperCase"));
 		stringOps.add(opf.createReflective(String.class, "toLowerCase"));
 		stringOps.add(opf.createReflective(String.class, "replaceAll", String.class, String.class));
 		stringOps.add(opf.createReflective(String.class, "replaceFirst", String.class, String.class));
 		stringOps.add(opf.createReflective(String.class, "split", String.class));
 		stringOps.add(opf.createReflective(String.class, "matches", String.class));
 		stringOps.add(opf.createReflective(String.class, "trim"));
 		stringOps.add(opf.createReflective(String.class, "length"));
 		stringOps.addAll(unmodifiableObjectOps);
 		internalOperationsMap.put(ecorePkg.getEString(), Collections.unmodifiableList(stringOps));
 
 		final List<InternalOperation> booleanOps = new LinkedList<InternalOperation>();
 		booleanOps.add(new InternalOperation<Boolean>(opf.create("!", boolean.class)) {
 			@Override
 			public Object evaluate(Boolean target, Object[] params) {
 				return Boolean.valueOf(!target.booleanValue());
 			}
 		});
 		booleanOps.addAll(unmodifiableObjectOps);
 		internalOperationsMap.put(ecorePkg.getEBoolean(), Collections.unmodifiableList(booleanOps));
 		final List<InternalOperation> voidOps = new LinkedList<InternalOperation>();
 		voidOps.addAll(unmodifiableObjectOps);
 		internalOperationsMap.put(VOID, Collections.unmodifiableList(voidOps));
 
 		//---------------------------------------------------------------------------------
 		class InternalSumOp extends InternalOperation<Number> {
 			InternalSumOp(EOperation metaOp) {
 				super(metaOp);
 			}
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				if (target instanceof Double || params[0] instanceof Double) {
 					return new Double(target.doubleValue() + ((Number) params[0]).doubleValue());
 				} else {
 					return new Integer(target.intValue() + ((Number) params[0]).intValue());
 				}
 			}
 			
 		}; 
 		class InternalSubOp extends InternalOperation<Number> {
 			InternalSubOp(EOperation metaOp) {
 				super(metaOp);
 			}
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				if (target instanceof Double || params[0] instanceof Double) {
 					return new Double(target.doubleValue() - ((Number) params[0]).doubleValue());
 				} else {
 					return new Integer(target.intValue() - ((Number) params[0]).intValue());
 				}
 			}
 			
 		}; 
 		class InternalMulOp extends InternalOperation<Number> {
 			InternalMulOp(EOperation metaOp) {
 				super(metaOp);
 			}
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				if (target instanceof Double || params[0] instanceof Double) {
 					return new Double(target.doubleValue() * ((Number) params[0]).doubleValue());
 				} else {
 					return new Integer(target.intValue() * ((Number) params[0]).intValue());
 				}
 			}
 			
 		}; 
 		class InternalDivOp extends InternalOperation<Number> {
 			InternalDivOp(EOperation metaOp) {
 				super(metaOp);
 			}
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				if (target instanceof Double || params[0] instanceof Double) {
 					return new Double(target.doubleValue() / ((Number) params[0]).doubleValue());
 				} else {
 					return new Integer(target.intValue() / ((Number) params[0]).intValue());
 				}
 			}
 			
 		};
 		class InternalNegateOp extends InternalOperation<Number> {
 			InternalNegateOp(EOperation metaOp) {
 				super(metaOp);
 			}
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				if (target instanceof Double) {
 					return -target.doubleValue();
 				}
 				return -target.intValue();
 			}
 		}
 		//---------------------------------------------------------------------------------
 
 		final List<InternalOperation> intOps = new LinkedList<InternalOperation>();
 		intOps.add(new InternalSumOp(opf.create("+", int.class, int.class)));
 		intOps.add(new InternalSumOp(opf.create("+", int.class, double.class)));
 		intOps.add(new InternalSubOp(opf.create("-", int.class, int.class)));
 		intOps.add(new InternalSubOp(opf.create("-", int.class, double.class)));
 		intOps.add(new InternalMulOp(opf.create("*", int.class, int.class)));
 		intOps.add(new InternalMulOp(opf.create("*", int.class, double.class)));
 		intOps.add(new InternalDivOp(opf.create("/", int.class, int.class)));
 		intOps.add(new InternalDivOp(opf.create("/", int.class, double.class)));
 		intOps.add(new InternalNegateOp(opf.create("-", int.class)));
 //		intOps.add(new InternalOperation<Number>(opf.create("==", boolean.class, int.class)) {
 //			@Override
 //			public Object evaluate(Number target, Object[] params) {
 //				//we may need this to handle cases like {Long(5), Long(4)}.exists(a | a == 5)
 //				return Boolean.valueOf(target.intValue() == ((Integer) params[0]).intValue());
 //			}
 //			
 //		});
 		intOps.add(new InternalOperation<Number>(opf.create(">=", boolean.class, int.class)) {
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				return Boolean.valueOf(target.intValue() >= ((Number) params[0]).intValue());
 			}
 		});
 		intOps.add(new InternalOperation<Number>(opf.create("<=", boolean.class, int.class)) {
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				return Boolean.valueOf(target.intValue() <= ((Number) params[0]).intValue());
 			}
 		});
 		intOps.add(new InternalOperation<Number>(opf.create("<", boolean.class, int.class)) {
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				return Boolean.valueOf(target.intValue() < ((Number) params[0]).intValue());
 			}
 		});
 		intOps.add(new InternalOperation<Number>(opf.create(">", boolean.class, int.class)) {
 			@Override
 			public Object evaluate(Number target, Object[] params) {
 				return Boolean.valueOf(target.intValue() > ((Number) params[0]).intValue());
 			}
 		});
 		intOps.add(new InternalOperation<Number>(opf.create("upTo", collectionTypes.getListType(ecorePkg.getEInt()), ecorePkg.getEInt())) {
 			@Override
 			public Object evaluate(Number target, Object[] params) {
                 final ArrayList<Integer> result = new ArrayList<Integer>();
                 for (int l1 = target.intValue(), l2 = ((Number) params[0]).intValue(); l1 <= l2; l1++) {
                     result.add(new Integer(l1));
                 }
                 return result;
 			}
 			
 		});
 		intOps.addAll(unmodifiableObjectOps);
 		List<InternalOperation> unmodifiableListIntOps = Collections.unmodifiableList(intOps);
 		internalOperationsMap.put(ecorePkg.getEIntegerObject(), unmodifiableListIntOps);
 		internalOperationsMap.put(ecorePkg.getEInt(), unmodifiableListIntOps);
 
 		final List<InternalOperation> doubleOps = new LinkedList<InternalOperation>();
 		doubleOps.add(new InternalSumOp(opf.create("+", double.class, double.class)));
 		doubleOps.add(new InternalSumOp(opf.create("+", double.class, int.class)));
 		doubleOps.add(new InternalSubOp(opf.create("-", double.class, double.class)));
 		doubleOps.add(new InternalSubOp(opf.create("-", double.class, int.class)));
 		doubleOps.add(new InternalMulOp(opf.create("*", double.class, double.class)));
 		doubleOps.add(new InternalMulOp(opf.create("*", double.class, int.class)));
 		doubleOps.add(new InternalDivOp(opf.create("/", double.class, double.class)));
 		doubleOps.add(new InternalDivOp(opf.create("/", double.class, int.class)));
 		doubleOps.add(new InternalNegateOp(opf.create("-", int.class)));
 		doubleOps.addAll(unmodifiableObjectOps);
 		internalOperationsMap.put(ecorePkg.getEDouble(), doubleOps);
 
 		final List<InternalOperation> collectionOps = new LinkedList<InternalOperation>();
		collectionOps.add(new InternalOperation<List>(opf.create("isEmpty", ecorePkg.getEBoolean())) {
 			@Override
			public Object evaluate(List target, Object[] params) {
 				// TODO isEmpty is rather attribute
 				return target.isEmpty();
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("add", CollectionTypesSupport.COLLECTION_OF_OBJECT, ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				target.add(params[0]);
 				return target;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("addAll", CollectionTypesSupport.COLLECTION_OF_OBJECT, CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				target.addAll((Collection) params[0]);
 				return target;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("clear", CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				target.clear();
 				return target;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("flatten", CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				LinkedList rv = new LinkedList();
 				for (Object o : target) {
 					if (o instanceof Collection) {
 						// XXX unlike original xpand, we do not flatten recursively
 						rv.addAll((Collection) o);
 					} else {
 						rv.add(o);
 					}
 				}
 				return rv;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("size", ecorePkg.getEInt())) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				return target.size();
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("union", CollectionTypesSupport.COLLECTION_OF_OBJECT, CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				LinkedHashSet<Object> rv = new LinkedHashSet<Object>(target);
 				rv.addAll((Collection) params[0]);
 				return rv;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("intersect", CollectionTypesSupport.COLLECTION_OF_OBJECT, CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				LinkedHashSet<Object> rv = new LinkedHashSet<Object>(target);
 				rv.retainAll((Collection) params[0]);
 				return rv;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("without", CollectionTypesSupport.COLLECTION_OF_OBJECT, CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				LinkedHashSet<Object> rv = new LinkedHashSet<Object>(target);
 				rv.removeAll((Collection) params[0]);
 				return rv;
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("toSet", CollectionTypesSupport.SET_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				return new LinkedHashSet<Object>(target);
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("toList", CollectionTypesSupport.LIST_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				return new LinkedList<Object>(target);
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("contains", ecorePkg.getEBoolean(), ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				return target.contains(params[0]);
 			}
 		});
 		collectionOps.add(new InternalOperation<Collection>(opf.create("containsAll", ecorePkg.getEBoolean(), CollectionTypesSupport.COLLECTION_OF_OBJECT)) {
 			@Override
 			public Object evaluate(Collection target, Object[] params) {
 				return target.containsAll((Collection) params[0]);
 			}
 		});
 		List<InternalOperation> unmodifiableListCollectionOps = Collections.unmodifiableList(collectionOps);
 		internalOperationsMap.put(CollectionTypesSupport.COLLECTION_TYPE, unmodifiableListCollectionOps);
 		internalOperationsMap.put(CollectionTypesSupport.SET_TYPE, unmodifiableListCollectionOps);
 		final List<InternalOperation> listOps = new LinkedList<InternalOperation>(unmodifiableListCollectionOps);
 		listOps.add(new InternalOperation<List>(opf.create("get", ecorePkg.getEJavaObject(), ecorePkg.getEInt())) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				int index = ((Number) params[0]).intValue();
 				return index < target.size() ? target.get(index) : null;
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("first", ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				return target.isEmpty() ? null : target.get(0);
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("last", ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				return target.isEmpty() ? null : target.get(target.size() - 1);
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("withoutFirst", CollectionTypesSupport.LIST_OF_OBJECT)) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				if (!target.isEmpty()) {
 					LinkedList rv = new LinkedList(target);
 					rv.removeFirst();
 					return rv;
 				}
 				return target;
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("withoutLast", CollectionTypesSupport.LIST_OF_OBJECT)) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				if (!target.isEmpty()) {
 					LinkedList rv = new LinkedList(target);
 					rv.removeLast();
 					return rv;
 				}
 				return target;
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("purgeDups", CollectionTypesSupport.LIST_OF_OBJECT)) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				if (target.isEmpty()) {
 					return target;
 				}
 				return new LinkedList<Object>(new LinkedHashSet<Object>(target));
 			}
 		});
 		listOps.add(new InternalOperation<List>(opf.create("indexOf", ecorePkg.getEInt(), ecorePkg.getEJavaObject())) {
 			@Override
 			public Object evaluate(List target, Object[] params) {
 				return target.indexOf(params[0]);
 			}
 		});
 		internalOperationsMap.put(CollectionTypesSupport.LIST_TYPE, Collections.unmodifiableList(listOps));
 
 		final List<InternalOperation> definitionOps = new LinkedList<InternalOperation>();
 		definitionOps.add(new InternalOperation<XpandDefinitionWrap>(opf.create("proceed", VOID)) {
 			@Override
 			public Object evaluate(XpandDefinitionWrap target, Object[] params) {
 				target.proceed();
 				return null;
 			}
 		});
 		internalOperationsMap.put(DEFINITION_TYPE, Collections.unmodifiableList(definitionOps));
 		
 		final List<InternalOperation> iteratorOps = new LinkedList<InternalOperation>();
 		iteratorOps.add(new InternalOperation<XpandIterator>(opf.create("isFirstIteration", ecorePkg.getEBoolean())) {
 			@Override
 			public Object evaluate(XpandIterator target, Object[] params) {
 				return target.isFirstIteration();
 			}
 		});
 		internalOperationsMap.put(ITERATOR_TYPE, Collections.unmodifiableList(iteratorOps));
 	}
 
 	private static List<EOperation> findInternalOp(EClassifier targetType) {
 		List<InternalOperation> ops = internalOperationsMap.get(targetType);
 		if (ops != null) {
 			List<EOperation> rv = new ArrayList<EOperation>(ops.size());
 			for (InternalOperation iop : ops) {
 				rv.add(iop.metaOp);
 			}
 			return rv;
 		}
 		if (targetType.eClass() == EcorePackage.eINSTANCE.getEClass()) {
 			// not instanceof because parametric type and all other XECore
 			// classes have different operations, at least
 			// meanwhile, when they are dynamic
 			return((EClass) targetType).getEAllOperations();
 		}
 		// TODO Auto-generated method stub
 		// String length, +, startsWith, endsWith, subString, toUpperCase, toFirstUpper, toFirstLower, toCharList, replaceAll, replaceFirst, split, matches, trim
 		// Int/Real +-*/ > >= < <= != ==
 		// Collection toList, toSet, toString, add, addAll, contains, containsAll, remove, removeAll, without, intersect, flatten ;
 		// properties - size, isEmpty
 		// List get, indexOf, last, first, withoutFirst, withoutLast
 		// Set - none
 		return null;
 	}
 
 	public static EClassifier getTypedElementType(ETypedElement p) {
 		if (p.isMany()) {
 			return p.isOrdered() ? getListType(p.getEType()) : p.isUnique() ? getSetType(p.getEType()) : getCollectionType(p.getEType());
 		}
 		return p.getEType();
 	}
 
 	private static class OperationFactory {
 		EOperation create(String name, Class returnType, Class ... params) {
 			EClassifier[] paramsNew = new EClassifier[params.length];
 			for (int i = 0; i < paramsNew.length; i++) {
 				paramsNew[i] = toEClassifier(params[i]);
 				assert params[i] != null;
 			}
 			EClassifier rt = toEClassifier(returnType);
 			assert rt != null : "Unrecognized return type:" + returnType;
 			return create(name, rt, paramsNew);
 		}
 
 		/**
 		 * NOTE, targetType is NOT operation's return type, but method owning class 
 		 */
 		InternalOperation createReflective(Class targetType, String methodName, Class ... params) {
 			try {
 				final Method m = targetType.getMethod(methodName, params);
 				assert m != null;
 				return new InternalOperation<Object>(create(methodName, m.getReturnType(), params)) {
 					public Object evaluate(Object target, Object[] params) {
 						try {
 							Object rv = m.invoke(target, params);
 							if (rv != null && rv.getClass().isArray()) {
 								return new LinkedList(Arrays.asList((Object[]) rv));
 							}
 							return rv;
 						} catch (Exception e) {
 							Activator.logError(e);
 							return null;
 						}
 					}
 				};
 			} catch (NoSuchMethodException ex) {
 				assert false : ex.getMessage();
 			}
 			return null;
 		}
 
 		private EClassifier toEClassifier(Class targetType) {
 			if (targetType == void.class) {
 				return VOID;
 			}
 			for (EClassifier c : EcorePackage.eINSTANCE.getEClassifiers()) {
 				if (c.getInstanceClass() == targetType) {
 					return c;
 				}
 			}
 			if (targetType.isArray()) {
 				EClassifier t = toEClassifier(targetType.getComponentType());
 				assert t != null : "Unrecognized array component type:" + targetType;
 				return getListType(t);
 			}
 			// TODO other packages
 			return null;
 		}
 
 		EOperation create(String name, EClassifier returnType, EClassifier ... params) {
 			EOperation op = EcoreFactory.eINSTANCE.createEOperation();
 			op.setName(name);
 			op.setEType(returnType);
 			for (EClassifier c : params) {
 				EParameter p1 = EcoreFactory.eINSTANCE.createEParameter();
 				p1.setName("arg" + c.getName());
 				p1.setEType(c);
 				op.getEParameters().add(p1);
 			}
 			return op;
 		}
 	}
 
 	private static abstract class InternalOperation<T> {
 		private final EOperation metaOp;
 		private static String INTERNAL_OP_ANNOTATION = "::internalop::";
 
 		private InternalOperation(EOperation metaOp) {
 			assert metaOp != null;
 			this.metaOp = metaOp;
 			EAnnotation internalOpAnn = EcoreFactory.eINSTANCE.createEAnnotation();
 			internalOpAnn.setSource(INTERNAL_OP_ANNOTATION);
 			metaOp.getEAnnotations().add(internalOpAnn);
 		}
 
 		public abstract Object evaluate(T target, Object[] params);
 
 		static boolean isInternalOp(EOperation op) {
 			return op.getEAnnotation(INTERNAL_OP_ANNOTATION) != null;
 		}
 	}
 
 	public static class OperationEx extends Operation {
 		private final InternalOperation<Object> internalOp;
 		private OperationEx(Object targetObject, Object[] args, InternalOperation<Object> internalOp) {
 			super(targetObject, args, internalOp.metaOp);
 			this.internalOp = internalOp;
 		}
 		@Override
 		public Object evaluate() {
 			return internalOp.evaluate(targetObject, args);
 		}
 	}
 
 	private static final Map<EOperation, Method> externalOpImplementations = new HashMap<EOperation, Method>(); 
 
 	public static void registerOperationImpl(EOperation metaOp, Method implementation) {
 		assert metaOp != null;
 		assert implementation != null;
 		assert Modifier.isStatic(implementation.getModifiers());
 		externalOpImplementations.put(metaOp, implementation);
 	}
 
 	public static class Operation {
 		protected final EOperation metaOp;
 		protected final Object[] args;
 		protected final Object targetObject;
 
 		private Operation(Object targetObject, Object[] args, EOperation metaOp) {
 			this.targetObject = targetObject;
 			this.args = args;
 			this.metaOp = metaOp;
 			
 		}
 		public Object evaluate() {
             try {
                 final Method m;
                 if (externalOpImplementations.containsKey(metaOp)) {
                 	m = externalOpImplementations.get(metaOp);
                 } else {
                 	m = targetObject.getClass().getMethod(metaOp.getName(), getParameterClasses());
                 }
                 return m.invoke(targetObject, args);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
 		}
 
 		private Class[] getParameterClasses() {
 			List<EParameter> emfParams = metaOp.getEParameters();
 			final Class[] paramClasses = new Class[emfParams.size()];
             for (int i = 0, x = emfParams.size(); i < x; i++) {
                 final EParameter param = emfParams.get(i);
         		// XXX works only for generated classes, or those with instance
 				// class set,
                 // and doesn't work with dynamic models, right?
                 paramClasses[i] = param.getEType().getInstanceClass();
             }
             return paramClasses;
 		}
 	}
 
 	/**
 	 * @return true if first argument is more general and second is more
 	 *         specific, think Object and String
 	 * @see AbstractTypeImpl.isAssignableFrom(this, t)
 	 */
 	public static boolean isAssignableFrom(EClassifier c1, EClassifier t) {
 		if ((t == null) || (c1 == null)) {
 			return false;
 		}
 		if (BuiltinMetaModel.primEquals(c1, t)) {
 			return true;
 		}
 		if (t.equals(VOID)) {
 			return true;
 		}
 		if (false == (t instanceof EClass)) {
 			if (c1 instanceof EEnum && t == EcorePackage.eINSTANCE.getEEnumerator()) {
 				return true; // HACK - any enumerator instance can be assigned to any enum attribute. 
 			}
 			if (c1 instanceof EDataType && t instanceof EDataType) {
 				return isCompatibleDataTypes((EDataType) c1, (EDataType) t);
 			}
 			return false;
 		}
 		if (c1 instanceof EDataType) {
 			Class c1Class = ((EDataType) c1).getInstanceClass();
 			return c1Class.isAssignableFrom(t.getInstanceClass() == null ? Object.class : t.getInstanceClass()); 
 		}
 		if (isParameterizedType(c1) && isParameterizedType(t)) {
 			return c1.eClass().isSuperTypeOf(t.eClass()) && isAssignableFrom(getInnerType(c1), getInnerType(t));
 		}
 		// == c1.isSuperTypeOf(t);
 		for (EClass superType : ((EClass) t).getEAllSuperTypes()) {
 			if (BuiltinMetaModel.primEquals(superType, c1)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static boolean primEquals(EClassifier c1, EClassifier obj) {
 	    if (obj == null) {
 			return false;
 		}
 	    if (c1 == obj) {
 	        return true;
 	    }
 	    final boolean namesEqual = c1.getName().equals(obj.getName());
 	    if (!namesEqual) {
 	    	return false;
 	    }
 	    if (c1.getEPackage() == null) {
 	    	return obj.getEPackage() == null;
 	    }
     	if (obj.getEPackage() == null) {
     		return false;
     	}
     	if (c1.getEPackage().getNsURI() == null) {
     		return obj.getEPackage().getNsURI() == null;
     	}
    		return c1.getEPackage().getNsURI().equals(obj.getEPackage().getNsURI());  
 	}
 
 	private static boolean isCompatibleDataTypes(EDataType dt1, EDataType dt2) {
 		try {
 			final Class dt1Class = dt1.getInstanceClass();
 			final Class dt2Class = dt2.getInstanceClass();
 			if (dt1Class != null && dt2Class != null) {
 				if (dt1Class == Object.class) {
 					// anything (with or without wrapping) can be assigned to object
 					return true;
 				}
 				if (dt1Class.isPrimitive() && !dt2Class.isPrimitive()) {
 					Field f = dt2Class.getField("TYPE");
 					return dt1Class.equals(f.get(null));
 				} else if (!dt1Class.isPrimitive() && dt2Class.isPrimitive()) {
 					Field f = dt1Class.getField("TYPE");
 					return dt2Class.equals(f.get(null));
 				}
 				return dt1Class.isAssignableFrom(dt2Class);
 			}
 		} catch (NoSuchFieldException ex) {
 			// IGNORE
 		} catch (IllegalAccessException ex) {
 			// IGNORE
 		}
 		return false;
 	}
 	public static List<EStructuralFeature> getAllFeatures(EClassifier targetType) {
 		// FIXME @see getAllOperations
 		if (targetType instanceof EClass) {
 			return ((EClass) targetType).getEAllStructuralFeatures();
 		}
 		return Collections.emptyList();
 	}
 
 	public static List<EOperation> getAllOperation(EClassifier targetType) {
 		// FIXME - either have datatypes like int/real as 'honest' EClasses, or
 		// provide their operations here
 		if (hasBuiltinSupport(targetType)) {
 			return findInternalOp(targetType);
 		}
 		if (targetType instanceof EClass) {
 			return ((EClass) targetType).getEAllOperations();
 		}
 		/*
 		 * XXX might be not bad idea to use java reflection to provide
 		 * datatype's possible operations if (targetType instanceof EDataType &&
 		 * false == targetType instanceof EEnum) { EDataType dt = (EDataType)
 		 * targetType; dt.getInstanceClass().getMethods() }
 		 */
 		return Collections.emptyList();
 	}
 }
