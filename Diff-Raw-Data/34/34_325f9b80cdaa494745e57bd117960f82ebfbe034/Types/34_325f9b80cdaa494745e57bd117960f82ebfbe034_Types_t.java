 // Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
 // for details. All rights reserved. Use of this source code is governed by a
 // BSD-style license that can be found in the LICENSE file.
 
 package com.google.dart.compiler.type;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Lists;
 import com.google.common.collect.MapMaker;
 import com.google.dart.compiler.ast.DartNewExpression;
 import com.google.dart.compiler.ast.DartNode;
 import com.google.dart.compiler.ast.DartPropertyAccess;
 import com.google.dart.compiler.ast.DartTypeNode;
 import com.google.dart.compiler.resolver.ClassElement;
 import com.google.dart.compiler.resolver.CoreTypeProvider;
 import com.google.dart.compiler.resolver.Element;
 import com.google.dart.compiler.resolver.FunctionAliasElement;
 import com.google.dart.compiler.resolver.ResolutionErrorListener;
 import com.google.dart.compiler.resolver.TypeVariableElement;
 import com.google.dart.compiler.resolver.VariableElement;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * Utility class for types.
  */
 public class Types {
   private static Map<Type, Type> inferredTypes = new MapMaker().weakKeys().weakValues().makeMap();
   private final CoreTypeProvider typeProvider;
 
   private Types(CoreTypeProvider typeProvider) { // Prevent subclassing.
     this.typeProvider = typeProvider;
   }
 
   public Type leastUpperBound(Type t, Type s) {
     if (isSubtype(t, s)) {
       return s;
     } else if (isSubtype(s, t)) {
       return t;
     } else {
       List<InterfaceType> tTypes = getSuperTypes(t);
       List<InterfaceType> sTypes = getSuperTypes(s);
       for (InterfaceType tType : tTypes) {
         if (sTypes.contains(tType)) {
           return tType;
         }
       }
       return typeProvider.getObjectType();
     }
   }
 
   /**
    * @return list of the super-types (if class type given) or super-interfaces (if interface type
    *         given) from most specific to least specific.
    */
   private static List<InterfaceType> getSuperTypes(Type type) {
     List<InterfaceType> types = Lists.newArrayList();
     if (type instanceof InterfaceType) {
       InterfaceType interfaceType = (InterfaceType) type;
       if (interfaceType.getElement().isInterface()) {
         types.add(interfaceType);
         for (InterfaceType intf : interfaceType.getElement().getInterfaces()) {
           types.addAll(getSuperTypes(intf));
         }
       } else {
         for (InterfaceType st = interfaceType; st != null; st = st.getElement().getSupertype()) {
           types.add(st);
         }
       }
     }
     return types;
   }
 
   /**
    * Return an interface type representing the given interface, function or
    * variable type.
    * @return An interface type or null if the argument is neither an interface
    *         function or variable type.
    */
   public InterfaceType getInterfaceType(Type type) {
     switch (TypeKind.of(type)) {
       case VARIABLE: {
         TypeVariableElement element = ((TypeVariable) type).getTypeVariableElement();
         if (element.getBound() == null) {
           return typeProvider.getObjectType();
         } else {
           return getInterfaceType(element.getBound());
         }
       }
       case FUNCTION:
       case FUNCTION_ALIAS:
         return typeProvider.getFunctionType();
       case INTERFACE:
         return (InterfaceType) type;
       case DYNAMIC:
       case NONE:
       case VOID:
       default:
         return null;
     }
   }
 
   /**
    * Returns true if t is a subtype of s.
    */
   public boolean isSubtype(Type t, Type s) {
     if (t.getKind().equals(TypeKind.DYNAMIC)) {
       return true;
     }
     switch (s.getKind()) {
       case DYNAMIC:
         return true;
 
       case INTERFACE:
         return isSubtypeOfInterface(t, (InterfaceType) s);
 
       case FUNCTION_ALIAS:
         return isSubtypeOfAlias(t, (FunctionAliasType) s);
 
       case FUNCTION:
         switch (t.getKind()) {
           case FUNCTION_ALIAS:
             return isSubtypeOfFunction(asFunctionType((FunctionAliasType) t), (FunctionType) s);
 
           case FUNCTION:
             return isSubtypeOfFunction((FunctionType) t, (FunctionType) s);
 
           default:
             return false;
         }
 
       case VARIABLE:
         return isSubtypeOfTypeVariable(t, (TypeVariable) s);
 
       case VOID:
         return t.equals(s);
 
       default:
         throw new AssertionError(s.getKind());
     }
   }
 
   FunctionType asFunctionType(FunctionAliasType alias) {
     FunctionAliasElement element = alias.getElement();
     FunctionType type =
         (FunctionType) element.getFunctionType().subst(alias.getArguments(),
                                                        element.getTypeParameters());
     return type;
   }
 
   private boolean isSubtypeOfAlias(Type t, FunctionAliasType s) {
     if (isSubtypeOfInterface(t, s)) {
       return true;
     }
    if (t.getKind() == TypeKind.VARIABLE) {
      Type bound = ((TypeVariable) t).getTypeVariableElement().getBound();
      if (bound != null) {
        return isSubtype(bound, s);
      }
      return true;
    }
     if (t.getKind().equals(TypeKind.FUNCTION_ALIAS)) {
       return isSubtypeOfFunction(asFunctionType((FunctionAliasType) t), asFunctionType(s));
     }
     return false;
   }
 
   private boolean isSubtypeOfTypeVariable(Type t, TypeVariable sv) {
     return sv.equals(t);
   }
 
   private boolean isSubtypeOfInterface(Type t, InterfaceType s) {
     final Type sup = asInstanceOf(t, s.getElement());
 
     if (TypeKind.of(sup).equals(TypeKind.INTERFACE)) {
       InterfaceType ti = (InterfaceType) sup;
       assert ti.getElement().equals(s.getElement());
       if (ti.isRaw() || s.isRaw()) {
         return true;
       }
       // Type arguments are covariant.
       return areSubtypes(ti.getArguments().iterator(), s.getArguments().iterator());
     }
     return false;
   }
 
   /**
    * Implement the Dart function subtype rule. Unlike the classic arrow rule (return type is
    * covariant, and paramter types are contravariant), in Dart they must just be assignable.
    */
   private boolean isSubtypeOfFunction(FunctionType t, FunctionType s) {
     if (s.getKind() == TypeKind.DYNAMIC || t.getKind() == TypeKind.DYNAMIC) {
       return true;
     }
     // Classic: return type is covariant; Dart: assignable.
     if (!isAssignable(t.getReturnType(), s.getReturnType())) {
       // A function that returns a value can be used as a function where you ignore the value.
       if (!s.getReturnType().equals(typeProvider.getVoidType())) {
         return false;
       }
     }
     Type tRest = t.getRest();
     Type sRest = s.getRest();
     if ((tRest == null) != (sRest == null)) {
       return false;
     }
     if (tRest != null) {
       // Classic: parameter types are contravariant; Dart: assignable.
       if (!isAssignable(sRest, tRest)) {
         return false;
       }
     }
     Map<String, Type> tNamed = t.getNamedParameterTypes();
     Map<String, Type> sNamed = s.getNamedParameterTypes();
     if (tNamed.isEmpty() && !sNamed.isEmpty()) {
       return false;
     }
 
     // T's named parameters must be in the same order and assignable to S's but
     // maybe a superset.
     if (!sNamed.isEmpty()) {
       LinkedHashMap<String,Type> tMap = (LinkedHashMap<String, Type>)(tNamed);
       LinkedHashMap<String,Type> sMap = (LinkedHashMap<String, Type>)(sNamed);
       Iterator<Entry<String, Type>> tList = tMap.entrySet().iterator();
       Iterator<Entry<String, Type>> sList = sMap.entrySet().iterator();
       // t named parameters must start with the named parameters of s
       while (sList.hasNext()) {
         if (!tList.hasNext()) {
           return false;
         }
         Entry<String, Type> sEntry = sList.next();
         Entry<String, Type> tEntry = tList.next();
         if (!sEntry.getKey().equals(tEntry.getKey())) {
           return false;
         }
         // Classic: parameter types are contravariant; Dart: assignable.
         if (!isAssignable(tEntry.getValue(), sEntry.getValue())) {
           return false;
         }
       }
     }
 
     // Classic: parameter types are contravariant; Dart: assignable.
     return areAssignable(s.getParameterTypes().iterator(), t.getParameterTypes().iterator());
   }
 
   private boolean areSubtypes(Iterator<? extends Type> t, Iterator<? extends Type> s) {
     while (t.hasNext() && s.hasNext()) {
       if (!isSubtype(t.next(), s.next())) {
         return false;
       }
     }
 
     // O(1) check to assert t and s are of same size.
     return t.hasNext() == s.hasNext();
   }
 
   private boolean areAssignable(Iterator<? extends Type> t, Iterator<? extends Type> s) {
     while (t.hasNext() && s.hasNext()) {
       if (!isAssignable(t.next(), s.next())) {
         return false;
       }
     }
 
     // O(1) check to assert t and s are of same size.
     return t.hasNext() == s.hasNext();
   }
 
   /**
    * Returns true if s is assignable to t.
    */
   public boolean isAssignable(Type t, Type s) {
     t.getClass(); // Quick null check.
     s.getClass(); // Quick null check.
     return isSubtype(t, s) || isSubtype(s, t);
   }
 
   /**
    * Translates the given type into an instantiation of the given
    * element. This is done by walking the supertype hierarchy and
    * substituting in the appropriate type arguments.
    *
    * <p>For example, if {@code GrowableArray<T>} is a subtype of
    * {@code Array<T>}, then
    * {@code asInstanceOf("GrowableArray<String>", "Array")} would
    * return {@code Array<String>}
    *
    * @return null if t is not a subtype of element
    */
   @VisibleForTesting
   public InterfaceType asInstanceOf(Type t, ClassElement element) {
     return checkedAsInstanceOf(t, element, new HashSet<TypeVariable>(), new HashSet<Type>());
   }
 
   private InterfaceType checkedAsInstanceOf(Type t, ClassElement element,
       Set<TypeVariable> variablesReferenced, Set<Type> checkedTypes) {
     // check for recursion
     if (checkedTypes.contains(t)) {
       return null;
     }
     checkedTypes.add(t);
     // check current Type
     switch (TypeKind.of(t)) {
       case FUNCTION_ALIAS:
       case INTERFACE: {
         if (t.getElement().equals(element)) {
           return (InterfaceType) t;
         }
         InterfaceType ti = (InterfaceType) t;
         ClassElement tElement = ti.getElement();
         InterfaceType supertype = tElement.getSupertype();
         if (supertype != null) {
           InterfaceType result = checkedAsInstanceOf(asSupertype(ti, supertype), element,
                                                      variablesReferenced, checkedTypes);
           if (result != null) {
             return result;
           }
         }
         for (InterfaceType intrface : tElement.getInterfaces()) {
           InterfaceType result = checkedAsInstanceOf(asSupertype(ti, intrface), element,
                                                      variablesReferenced, checkedTypes);
           if (result != null) {
             return result;
           }
         }
         return null;
       }
       case FUNCTION: {
         Element e = t.getElement();
         switch (e.getKind()) {
           case CLASS:
             // e should be the interface Function in the core library. See the
             // documentation comment on FunctionType.
             InterfaceType ti = (InterfaceType) e.getType();
             return checkedAsInstanceOf(ti, element, variablesReferenced, checkedTypes);
           default:
             return null;
         }
       }
       case VARIABLE: {
         TypeVariable v = (TypeVariable) t;
         Type bound = v.getTypeVariableElement().getBound();
         // Check for previously encountered variables to avoid getting stuck in an infinite loop.
         if (variablesReferenced.contains(v)) {
           if (bound instanceof InterfaceType) {
             return (InterfaceType) bound;
           }
           return typeProvider.getObjectType();
         }
         variablesReferenced.add(v);
         return checkedAsInstanceOf(bound, element, variablesReferenced, checkedTypes);
       }
       default:
         return null;
     }
   }
 
   private InterfaceType asSupertype(InterfaceType type, InterfaceType supertype) {
     if (supertype == null) {
       return null;
     }
     if (type.isRaw()) {
       return supertype.asRawType();
     }
     List<Type> arguments = type.getArguments();
     List<Type> parameters = type.getElement().getTypeParameters();
     return supertype.subst(arguments, parameters);
   }
 
   static void printTypesOn(StringBuilder sb, List<Type> types,
                            String start, String end) {
     sb.append(start);
     boolean first = true;
     for (Type argument : types) {
       if (!first) {
         sb.append(", ");
       }
       sb.append(argument);
       first = false;
     }
     sb.append(end);
   }
 
   public static List<Type> subst(List<Type> types,
                                  List<Type> arguments, List<Type> parameters) {
     ArrayList<Type> result = new ArrayList<Type>(types.size());
     for (Type type : types) {
       result.add(type.subst(arguments, parameters));
     }
     return result;
   }
 
   public static FunctionType makeFunctionType(ResolutionErrorListener listener,
                                               ClassElement element,
                                               List<VariableElement> parameters,
                                               Type returnType) {
     List<Type> parameterTypes = new ArrayList<Type>(parameters.size());
     Map<String, Type> namedParameterTypes = null;
     Type restParameter = null;
     for (VariableElement parameter : parameters) {
       Type type = parameter.getType();
       if (parameter.isNamed()) {
         if (namedParameterTypes == null) {
           namedParameterTypes = new LinkedHashMap<String, Type>();
         }
         namedParameterTypes.put(parameter.getName(), type);
       } else {
         parameterTypes.add(type);
       }
     }
     return FunctionTypeImplementation.of(element, parameterTypes, namedParameterTypes,
                                          restParameter, returnType);
   }
 
   public static Types getInstance(CoreTypeProvider typeProvider) {
     return new Types(typeProvider);
   }
 
   public static InterfaceType interfaceType(ClassElement element, List<Type> arguments) {
     return new InterfaceTypeImplementation(element, arguments);
   }
 
   public static FunctionAliasType functionAliasType(FunctionAliasElement element,
       List<TypeVariable> typeVariables) {
     return new FunctionAliasTypeImplementation(element,
         Collections.<Type>unmodifiableList(typeVariables));
   }
 
   public static TypeVariable typeVariable(TypeVariableElement element) {
     return new TypeVariableImplementation(element);
   }
 
   public static DynamicType newDynamicType() {
     return new DynamicTypeImplementation();
   }
 
   public static InterfaceType ensureInterface(Type type) {
     TypeKind kind = TypeKind.of(type);
     switch (kind) {
       case INTERFACE:
         return (InterfaceType) type;
       case NONE:
       case DYNAMIC:
         return null;
       default:
         throw new AssertionError("unexpected kind " + kind);
     }
   }
 
   public static Type newVoidType() {
     return new VoidType();
   }
 
   /**
    * Returns the type node corresponding to the instantiated class or interface.
    */
   public static DartTypeNode constructorTypeNode(DartNewExpression node) {
     DartNode constructor = node.getConstructor();
     if (constructor instanceof DartPropertyAccess) {
       return (DartTypeNode) ((DartPropertyAccess) constructor).getQualifier();
     } else {
       return (DartTypeNode) constructor;
     }
   }
 
   /**
    * Returns the interface type being instantiated by the given node.
    */
   public static InterfaceType constructorType(DartNewExpression node) {
     DartTypeNode typeNode = constructorTypeNode(node);
     return (InterfaceType) typeNode.getType();
   }
 
   /**
    * @return the wrapper of the given {@link Type} which returns <code>true</code> from
    *         {@link Type#isInferred()}.
    */
   public static Type makeInferred(Type type) {
     if (type instanceof DynamicType) {
       return makeInferred(type, DynamicType.class);
     }
     if (type instanceof InterfaceType) {
       return makeInferred(type, InterfaceType.class);
     }
     return type;
   }
 
   private static Type makeInferred(final Type type, Class<?> typeInterface) {
     Type inferred = inferredTypes.get(type);
     if (inferred == null) {
       inferred = (Type) Proxy.newProxyInstance(type.getClass().getClassLoader(),
           new Class<?>[] {typeInterface}, new InvocationHandler() {
             @Override
             public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
               if (args == null && method.getName().equals("isInferred")) {
                 return true;
               }
               return method.invoke(type, args);
             }
           });
       inferredTypes.put(type, inferred);
     }
     return inferred;
   }
 }
