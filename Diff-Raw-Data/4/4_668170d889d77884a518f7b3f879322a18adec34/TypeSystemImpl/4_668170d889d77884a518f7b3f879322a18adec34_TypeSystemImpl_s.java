 /*******************************************************************************
  * Copyright (c) 2012 NumberFour AG
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     NumberFour AG - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.internal.javascript.ti;
 
 import static org.eclipse.core.runtime.Platform.getDebugOption;
 import static org.eclipse.dltk.javascript.typeinfo.RTypes.none;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.dltk.annotations.NonNull;
 import org.eclipse.dltk.javascript.internal.core.RConstructor;
 import org.eclipse.dltk.javascript.internal.core.RMethod;
 import org.eclipse.dltk.javascript.internal.core.RParameter;
 import org.eclipse.dltk.javascript.internal.core.RParameterizedTypeDeclaration;
 import org.eclipse.dltk.javascript.internal.core.RProperty;
 import org.eclipse.dltk.javascript.internal.core.RTypeDeclaration;
 import org.eclipse.dltk.javascript.typeinfo.AttributeKey;
 import org.eclipse.dltk.javascript.typeinfo.IRArrayType;
 import org.eclipse.dltk.javascript.typeinfo.IRConstructor;
 import org.eclipse.dltk.javascript.typeinfo.IRContextualizableType;
 import org.eclipse.dltk.javascript.typeinfo.IRMember;
 import org.eclipse.dltk.javascript.typeinfo.IRMethod;
 import org.eclipse.dltk.javascript.typeinfo.IRParameter;
 import org.eclipse.dltk.javascript.typeinfo.IRProperty;
 import org.eclipse.dltk.javascript.typeinfo.IRSimpleType;
 import org.eclipse.dltk.javascript.typeinfo.IRType;
 import org.eclipse.dltk.javascript.typeinfo.IRTypeDeclaration;
 import org.eclipse.dltk.javascript.typeinfo.IRTypeTransformer;
 import org.eclipse.dltk.javascript.typeinfo.ITypeSystem;
 import org.eclipse.dltk.javascript.typeinfo.RTypes;
 import org.eclipse.dltk.javascript.typeinfo.model.Constructor;
 import org.eclipse.dltk.javascript.typeinfo.model.GenericType;
 import org.eclipse.dltk.javascript.typeinfo.model.JSType;
 import org.eclipse.dltk.javascript.typeinfo.model.Member;
 import org.eclipse.dltk.javascript.typeinfo.model.Method;
 import org.eclipse.dltk.javascript.typeinfo.model.Parameter;
 import org.eclipse.dltk.javascript.typeinfo.model.ParameterizedType;
 import org.eclipse.dltk.javascript.typeinfo.model.Property;
 import org.eclipse.dltk.javascript.typeinfo.model.SimpleType;
 import org.eclipse.dltk.javascript.typeinfo.model.Type;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeInfoModelLoader;
 import org.eclipse.dltk.javascript.typeinfo.model.TypeVariable;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.InternalEObject;
 
 public class TypeSystemImpl implements ITypeSystem {
 
 	/*
 	 * @see ITypeSystem#resolveType(Type)
 	 */
 	public Type resolveType(Type type) {
 		if (type != null && type.isProxy()) {
 			return doResolveType(type);
 		} else {
 			return type;
 		}
 	}
 
 	public Type doResolveType(Type type) {
 		final String typeName = URI.decode(((InternalEObject) type).eProxyURI()
 				.fragment());
 		final Type resolved = TypeInfoModelLoader.getInstance().getType(
 				typeName);
 		if (resolved != null) {
 			return resolved;
 		}
 		return type;
 	}
 
 	/*
 	 * @see ITypeSystem#valueOf(Member)
 	 */
 	public IValue valueOf(IRMember member) {
 		return null;
 	}
 
 	private final Object lock = new Object();
 
 	private final Map<Type, RTypeDeclaration> declarations = new HashMap<Type, RTypeDeclaration>();
 
 	public IRTypeDeclaration convert(Type type) {
 		return convert0(resolveType(type));
 	}
 
 	final IRTypeDeclaration convert0(Type type) {
 		final ITypeSystem preferred = type.getMetaType()
 				.getPreferredTypeSystem(type);
 		if (preferred != null && preferred != this) {
 			if (preferred instanceof TypeSystemImpl) {
 				return ((TypeSystemImpl) preferred).convert1(type);
 			} else {
 				return preferred.convert(type);
 			}
 		}
 		return convert1(type);
 	}
 
 	final IRTypeDeclaration convert1(Type type) {
 		synchronized (lock) {
 			return convertType(type, null);
 		}
 	}
 
 	/**
 	 * Clears the cached data of this type system.
 	 */
 	public void reset() {
 		synchronized (lock) {
 			declarations.clear();
 			parameterized.clear();
 			contextualized.clear();
			values.clear();
 		}
 	}
 
 	private RTypeDeclaration convertType(Type type, Set<Type> processedTypes) {
 		{
 			final RTypeDeclaration declaration = declarations.get(type);
 			if (declaration != null) {
 				return declaration;
 			}
 		}
 		if (processedTypes == null) {
 			processedTypes = new HashSet<Type>();
 		}
 		if (!processedTypes.add(type)) {
 			return null;
 		}
 		final RTypeDeclaration declaration = new RTypeDeclaration(type);
 		declarations.put(type, declaration);
 		if (TRACE) {
 			log("Creating", declaration, "declarations.size=",
 					declarations.size());
 		}
 		buildType(declaration, type, processedTypes);
 		return declaration;
 	}
 
 	private void buildType(final RTypeDeclaration declaration, Type type,
 			Set<Type> processedTypes) {
 		final SimpleType superType = type.getSuperTypeExpr();
 		if (superType != null && superType.getTarget() != null) {
 			if (superType.getTarget() instanceof GenericType) {
 				final GenericType generic = (GenericType) superType.getTarget();
 				if (superType instanceof ParameterizedType) {
 					declaration.setSuperType(parameterizeType(generic, RTypes
 							.convert(this, ((ParameterizedType) superType)
 									.getActualTypeArguments())));
 				} else if (declaration.isParameterized()) {
 					declaration.setSuperType(parameterizeType(generic,
 							declaration.getActualTypeArguments()));
 				} else {
 					declaration.setSuperType(convertType(generic,
 							processedTypes));
 				}
 			} else {
 				declaration.setSuperType(convertType(superType.getTarget(),
 						processedTypes));
 			}
 		}
 		final List<RTypeDeclaration> traits = new ArrayList<RTypeDeclaration>(
 				type.getTraits().size());
 		for (Type trait : type.getTraits()) {
 			final RTypeDeclaration t = convertType(trait, processedTypes);
 			if (t != null) {
 				traits.add(t);
 			}
 		}
 		declaration.setTraits(traits);
 		final Member[] additionalMembers = type.getAdditionalMembers();
 		final List<IRMember> members = new ArrayList<IRMember>(type
 				.getMembers().size()
 				+ (additionalMembers != null ? additionalMembers.length : 0));
 		for (Member member : type.getMembers()) {
 			members.add(convertMember(member, declaration));
 		}
 		if (additionalMembers != null && additionalMembers.length != 0) {
 			for (Member member : additionalMembers) {
 				members.add(convertMember(member, declaration));
 			}
 		}
 		declaration.setMembers(members);
 		final List<IRConstructor> constructors = new ArrayList<IRConstructor>(
 				type.getConstructors().size());
 		for (Constructor constructor : type.getConstructors()) {
 			constructors.add(convertConstructor(constructor, declaration));
 		}
 		declaration.setConstructors(constructors);
 		final Constructor staticConstructor = type.getStaticConstructor();
 		if (staticConstructor != null) {
 			declaration.setStaticConstructor(convertConstructor(
 					staticConstructor, declaration));
 		}
 	}
 
 	private void log(Object... args) {
 		if (!TRACE)
 			return;
 		final StringBuilder sb = new StringBuilder(128);
 		sb.append('[').append(Thread.currentThread().getName()).append("] ");
 		sb.append(this.toString());
 		sb.append(":");
 		boolean space = true;
 		for (int i = 0; i < args.length; ++i) {
 			if (space) {
 				sb.append(' ');
 			}
 			final Object arg = args[i];
 			sb.append(arg);
 			space = !(arg instanceof String && ((String) arg).endsWith("="));
 		}
 		System.out.println(sb);
 	}
 
 	private IRType convert(final JSType type) {
 		return RTypes.create(this, type);
 	}
 
 	public IRType getTypeVariable(TypeVariable variable) {
 		return typeVariables.get(variable);
 	}
 
 	protected IRMember convertMember(Member member,
 			IRTypeDeclaration declaration) {
 		if (member instanceof Method) {
 			return convertMethod((Method) member, declaration);
 		} else {
 			assert member instanceof Property;
 			return convertProperty((Property) member, declaration);
 		}
 	}
 
 	private IRMember convertProperty(Property property,
 			IRTypeDeclaration declaration) {
 		return new RProperty(property, convert(property.getType()), declaration);
 	}
 
 	private IRMember convertMethod(Method method, IRTypeDeclaration declaration) {
 		return new RMethod(method, convert(method.getType()),
 				convertParameters(method.getParameters()), declaration);
 	}
 
 	private IRConstructor convertConstructor(Constructor constructor,
 			RTypeDeclaration declaration) {
 		return new RConstructor(constructor, convert(constructor.getType()),
 				convertParameters(constructor.getParameters()), declaration);
 	}
 
 	private List<IRParameter> convertParameters(List<Parameter> parameters) {
 		if (parameters.isEmpty()) {
 			return Collections.emptyList();
 		}
 		final List<IRParameter> result = new ArrayList<IRParameter>(
 				parameters.size());
 		for (Parameter param : parameters) {
 			final IRType type = param.getType() != null ? convert(param
 					.getType()) : RTypes.any();
 			result.add(new RParameter(param.getName(), type, param.getKind()));
 		}
 		return result;
 	}
 
 	private static class ParameterizedTypeKey {
 		final GenericType type;
 		final IRType[] parameters;
 
 		public ParameterizedTypeKey(GenericType type, List<IRType> params) {
 			this.type = type;
 			final int expectedParamCount = type.getTypeParameters().size();
 			this.parameters = new IRType[expectedParamCount];
 			for (int i = 0; i < expectedParamCount; ++i) {
 				IRType t;
 				if (i < params.size()) {
 					t = params.get(i);
 					if (t == null) {
 						t = none();
 					}
 				} else {
 					t = none();
 				}
 				this.parameters[i] = t;
 			}
 		}
 
 		@Override
 		public int hashCode() {
 			return type.hashCode() ^ Arrays.hashCode(parameters);
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof ParameterizedTypeKey) {
 				final ParameterizedTypeKey other = (ParameterizedTypeKey) obj;
 				return type == other.type
 						&& Arrays.equals(parameters, other.parameters);
 			}
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			final StringBuilder parameterizedName = new StringBuilder(type
 					.getName().length() + parameters.length * 16);
 			parameterizedName.append(type.getName());
 			parameterizedName.append("<");
 			for (int i = 0; i < parameters.length; ++i) {
 				if (i > 0) {
 					parameterizedName.append(",");
 				}
 				parameterizedName.append(parameters[i].getName());
 			}
 			parameterizedName.append(">");
 			return parameterizedName.toString();
 		}
 
 	}
 
 	private final Map<ParameterizedTypeKey, RTypeDeclaration> parameterized = new HashMap<ParameterizedTypeKey, RTypeDeclaration>();
 
 	/*
 	 * @see ITypeSystem#parameterize(Type, java.util.List)
 	 */
 	public IRTypeDeclaration parameterize(Type target, List<IRType> parameters) {
 		target = resolveType(target);
 		if (!(target instanceof GenericType)) {
 			return convert0(target);
 		}
 		synchronized (lock) {
 			return parameterizeType((GenericType) target, parameters);
 		}
 	}
 
 	private final Map<TypeVariable, IRType> typeVariables = new HashMap<TypeVariable, IRType>();
 
 	private RTypeDeclaration parameterizeType(GenericType genericType,
 			List<IRType> parameters) {
 		final ParameterizedTypeKey key = new ParameterizedTypeKey(genericType,
 				parameters);
 		{
 			final RTypeDeclaration declaration = parameterized.get(key);
 			if (declaration != null) {
 				return declaration;
 			}
 		}
 		if (TRACE) {
 			log("Creating", key, "parameterized.size=", parameterized.size());
 		}
 		final RParameterizedTypeDeclaration declaration = new RParameterizedTypeDeclaration(
 				genericType, Arrays.asList(key.parameters));
 		parameterized.put(key, declaration);
 		final IRType[] oldTypes = new IRType[key.parameters.length];
 		final TypeVariable[] variables = genericType.getTypeParameters()
 				.toArray(new TypeVariable[key.parameters.length]);
 		for (int i = 0; i < key.parameters.length; ++i) {
 			oldTypes[i] = typeVariables.put(variables[i], key.parameters[i]);
 		}
 		try {
 			buildType(declaration, genericType, new HashSet<Type>());
 		} finally {
 			for (int i = 0; i < key.parameters.length; ++i) {
 				final TypeVariable variable = variables[i];
 				if (oldTypes[i] != null) {
 					typeVariables.put(variable, oldTypes[i]);
 				} else {
 					typeVariables.remove(variable);
 				}
 			}
 		}
 		return declaration;
 	}
 
 	private static class ContextualizeKey {
 		@NonNull
 		final IRMember member;
 
 		@NonNull
 		final IRTypeDeclaration declaration;
 
 		public ContextualizeKey(@NonNull IRMember member,
 				@NonNull IRTypeDeclaration declaration) {
 			this.member = member;
 			this.declaration = declaration;
 		}
 
 		@Override
 		public int hashCode() {
 			return member.hashCode() ^ declaration.hashCode();
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj instanceof ContextualizeKey) {
 				final ContextualizeKey other = (ContextualizeKey) obj;
 				return member.equals(other.member)
 						&& declaration.equals(other.declaration);
 			}
 			return false;
 		}
 	}
 
 	private final Map<ContextualizeKey, IRMember> contextualized = new HashMap<ContextualizeKey, IRMember>();
 
 	@SuppressWarnings("unchecked")
 	public <E extends IRMember> E contextualize(E member,
 			IRTypeDeclaration declaration) {
 		if (!isContextualizable(member)) {
 			return member;
 		}
 		final ContextualizeKey key = new ContextualizeKey(member, declaration);
 		synchronized (contextualized) {
 			final E cached = (E) contextualized.get(key);
 			if (cached != null) {
 				return cached;
 			}
 			final E result = contextualizeMember(member, declaration);
 			contextualized.put(key, result);
 			if (TRACE) {
 				log("Contextualized", result, "with", declaration,
 						"contextualized.size=", contextualized.size());
 			}
 			return result;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private <E extends IRMember> E contextualizeMember(E member,
 			IRTypeDeclaration declaration) {
 		final IRTypeTransformer transformer = newTypeContextualizer(declaration);
 		if (member instanceof IRMethod) {
 			final IRMethod method = (IRMethod) member;
 			final List<IRParameter> parameters = transformParameters(method,
 					transformer);
 			if (member instanceof IRConstructor) {
 				return (E) new RConstructor((Method) member.getSource(),
 						transformer.transform(member.getType()), parameters,
 						member.getDeclaringType());
 			} else {
 				return (E) new RMethod((Method) member.getSource(),
 						transformer.transform(member.getType()), parameters,
 						member.getDeclaringType());
 			}
 		} else {
 			assert member instanceof IRProperty;
 			return (E) new RProperty((Property) member.getSource(),
 					transformer.transform(member.getType()),
 					member.getDeclaringType());
 		}
 	}
 
 	public IRTypeTransformer newTypeContextualizer(IRTypeDeclaration declaration) {
 		return new TypeContextualizer(declaration);
 	}
 
 	public static List<IRParameter> transformParameters(final IRMethod method,
 			final IRTypeTransformer transformer) {
 		if (method.getParameterCount() == 0) {
 			return Collections.emptyList();
 		} else {
 			final List<IRParameter> parameters = new ArrayList<IRParameter>(
 					method.getParameterCount());
 			for (IRParameter parameter : method.getParameters()) {
 				final IRType newType = transformer.transform(parameter
 						.getType());
 				if (newType != parameter.getType()) {
 					parameters.add(new RParameter(parameter.getName(), newType,
 							parameter.getKind()));
 				} else {
 					parameters.add(parameter);
 				}
 			}
 			return parameters;
 		}
 	}
 
 	private class TypeContextualizer implements IRTypeTransformer {
 		private final IRTypeDeclaration contextType;
 
 		public TypeContextualizer(IRTypeDeclaration declaration) {
 			this.contextType = declaration;
 		}
 
 		public IRType transform(IRType type) {
 			if (type instanceof IRContextualizableType) {
 				final IRContextualizableType c = (IRContextualizableType) type;
 				if (c.isContextualizable()) {
 					return c.contextualize(contextType);
 				}
 			}
 			return type != null ? type.transform(this) : null;
 		}
 
 		public IRTypeDeclaration transform(IRTypeDeclaration declaration) {
 			if (declaration.isParameterized()) {
 				boolean createNew = false;
 				final List<IRType> typeParams = new ArrayList<IRType>(
 						declaration.getActualTypeArguments());
 				for (ListIterator<IRType> i = typeParams.listIterator(); i
 						.hasNext();) {
 					final IRType type = i.next();
 					if (type instanceof IRContextualizableType) {
 						final IRContextualizableType c = (IRContextualizableType) type;
 						if (c.isContextualizable()) {
 							final IRType newType = c
 									.contextualize(this.contextType);
 							if (newType != type) {
 								i.set(newType);
 								createNew = true;
 							}
 						}
 					}
 				}
 				if (createNew) {
 					return parameterize(declaration.getSource(), typeParams);
 				}
 			}
 			return declaration;
 		}
 	}
 
 	/*
 	 * @see ITypeSystem#getAttribute(AttributeKey)
 	 */
 	public <T> T getAttribute(AttributeKey<T> key) {
 		return null;
 	}
 
 	private Map<Object, Object> values;
 
 	/*
 	 * @see ITypeSystem#getValue(java.lang.Object )
 	 */
 	public Object getValue(Object key) {
 		assert key != null;
 		return values != null ? values.get(key) : null;
 	}
 
 	/*
 	 * @see ITypeSystem#setValue(java.lang.Object , java.lang.Object)
 	 */
 	public void setValue(Object key, Object value) {
 		assert key != null;
 		if (values == null) {
 			values = new HashMap<Object, Object>();
 		}
 		values.put(key, value);
 	}
 
 	public static boolean isContextualizable(IRMember member) {
 		if (isContextualizable(member.getType())) {
 			return true;
 		}
 		if (member instanceof IRMethod) {
 			for (IRParameter parameter : ((IRMethod) member).getParameters()) {
 				if (isContextualizable(parameter.getType())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	static boolean isContextualizable(IRType type) {
 		if (type == null) {
 			return false;
 		} else if (type instanceof IRContextualizableType) {
 			return ((IRContextualizableType) type).isContextualizable();
 		} else if (type instanceof IRArrayType) {
 			return isContextualizable(((IRArrayType) type).getItemType());
 		} else if (type instanceof IRSimpleType) {
 			final IRTypeDeclaration declaration = ((IRSimpleType) type)
 					.getDeclaration();
 			if (declaration.isParameterized()) {
 				for (IRType typeArgument : declaration.getActualTypeArguments()) {
 					if (isContextualizable(typeArgument)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public IRType contextualize(IRType type, IRTypeDeclaration declaration) {
 		if (isContextualizable(type)) {
 			final IRTypeTransformer transformer = newTypeContextualizer(declaration);
 			return transformer.transform(type);
 		} else {
 			return type;
 		}
 	}
 
 	private static final boolean TRACE = Boolean.valueOf(
 			getDebugOption("org.eclipse.dltk.javascript.core/traceTypeSystem"))
 			.booleanValue();
 
 }
