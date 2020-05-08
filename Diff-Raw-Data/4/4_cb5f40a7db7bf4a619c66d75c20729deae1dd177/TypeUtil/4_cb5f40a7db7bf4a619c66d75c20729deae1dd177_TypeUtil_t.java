 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman/Oracle - initial API and implementation
  *    
  ********************************************************************************/
 
 package org.eclipse.jst.jsf.common.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.ITypeParameter;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.Signature;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.internal.types.TypeConstants;
 
 /**
  * Utility for handling IType's and type signatures
  * 
  * Class is static and cannot be extended or instantiated.
  * 
  * @author cbateman
  *
  */
 public final class TypeUtil 
 {
     static IType resolveType(final IType owningType, final String typeSignature)
     {
         // if type signature is already resolved then simply look it up
         if (typeSignature.charAt(0) == Signature.C_RESOLVED
         		|| (Signature.getTypeSignatureKind(typeSignature) == Signature.ARRAY_TYPE_SIGNATURE
         			&& Signature.getElementType(typeSignature).charAt(0) == Signature.C_RESOLVED))
         {
             IType type = null;
             
             try
             {
                 type = owningType.getJavaProject().
                            findType(getFullyQualifiedName(typeSignature));
             }
             catch (JavaModelException jme)
             {
                 // do nothing; return type == null;
             }
             
             return type;
         }
         
         
         return resolveTypeRelative(owningType, typeSignature);
     }
 
     /**
      * Fully equivalent to:
      * 
      * #resolveTypeSignature(owningType, typeSignature, true)
      * 
      * If resolved, type signature has generic type parameters erased (absent).
      * 
      * @param owningType
      * @param typeSignature
      * @return the resolved type signature for typeSignature in owningType or
      * typeSignature unchanged if cannot resolve.
      */
     public static String resolveTypeSignature(final IType owningType, final String typeSignature)
     {
         return resolveTypeSignature(owningType, typeSignature, true);
     }
     
     /**
      * Resolve typeSignature in the context of owningType.  This method will return 
      * a type erased signture if eraseTypeParameters == true and will attempt to
      * resolve and include parameters if eraseTypeParamters == false
      * 
      * NOTE: special rules apply to the way unresolved type parameters and wildcards
      * are resolved:
      * 
      * 1) If a fully unresolved type parameter is found, then it will be replaced with Ljava.lang.Object;
      * 
      * i.e.  List<T>  -> Ljava.util.List<Ljava.lang.Object;>;  for any unresolved T.
      * 
      * 2) Any bounded wildcard will be replaced by the bound:
      * 
      * i.e. List<? extends String> -> Ljava.util.List<Ljava.lang.String;>;
      * i.e. List<? super String> -> Ljava.util.List<Ljava.lang.String;>;
      * 
      * Note limitation here: bounds that use 'super' will take the "best case" scenario that the list
      * type is of that type.
      * 
      * 3) The unbounded wildcard will be replaced by Ljava.lang.Object;
      * 
      * i.e. List<?> -> Ljava.util.List<Ljava.lang.Object;>;
      * 
      * 
      * The reason for this substition is to return the most accurate reasonable approximation
      * of the type within what is known by owningType
      * 
      * @param owningType
      * @param typeSignature
      * @param eraseTypeParameters if set to false, type parameters are resolved included
      * in the signature
      * @return the resolved type signature for typeSignature in owningType or
      * typeSignature unchanged if cannot resolve.
      */
     public static String resolveTypeSignature(final IType owningType, final String typeSignature, boolean eraseTypeParameters)
     {
         final int sigKind = Signature.getTypeSignatureKind(typeSignature);
     
         switch (sigKind)
         {
             case Signature.BASE_TYPE_SIGNATURE:
                 return typeSignature;
                 
             case Signature.ARRAY_TYPE_SIGNATURE:
             {
                 final String elementType = Signature.getElementType(typeSignature);
                 
                 if (Signature.getTypeSignatureKind(elementType) == Signature.BASE_TYPE_SIGNATURE)
                 {
                     return typeSignature;
                 }
 
                 final String resolvedElementType = resolveSignatureRelative(owningType, elementType, eraseTypeParameters);
                 String resultType = ""; //$NON-NLS-1$
                 for (int i = 0; i < Signature.getArrayCount(typeSignature);i++)
                 {
                     resultType+=Signature.C_ARRAY;
                 }
                 
                 return resultType+resolvedElementType;
             }
 
             case Signature.TYPE_VARIABLE_SIGNATURE:
             	return resolveSignatureRelative(owningType, typeSignature, eraseTypeParameters);
             
             case Signature.CLASS_TYPE_SIGNATURE:
                 return resolveSignatureRelative(owningType, typeSignature, eraseTypeParameters);
 
             case Signature.WILDCARD_TYPE_SIGNATURE:
                 // strip the wildcard and try again.  Too bad Signature doesn't seem to have a method
                 // for this
                if (typeSignature.charAt(0) == Signature.C_STAR)
                {
                    return TypeConstants.TYPE_JAVAOBJECT;
                }
                 return resolveTypeSignature(owningType, typeSignature.substring(1), eraseTypeParameters);
             
             case Signature.CAPTURE_TYPE_SIGNATURE:
                 // strip the capture and try again
                 return resolveTypeSignature(owningType, Signature.removeCapture(typeSignature), eraseTypeParameters);
 //            case Signature.TYPE_VARIABLE_SIGNATURE:
 //                resolveSignatureRelative(owningType, typeSignature, eraseTypeParameters);
 
             default:
                 return typeSignature;
         }
     }
     
     /**
      * @param owningType -- type relative to which typeSignature will be resolved
      * @param typeSignature -- non-array type signature
      * @return the resolved type signature if possible or typeSignature if not
      */
     private static String resolveSignatureRelative(final IType owningType, final String typeSignature, final boolean eraseTypeParameters)
     {
         // if already fully resolved, return the input
         if (typeSignature.charAt(0) == Signature.C_RESOLVED)
         {
             return typeSignature;
         }
 
         List<String> typeParameters = new ArrayList<String>();
 
         IType resolvedType = resolveTypeRelative(owningType, typeSignature);
 
         if (resolvedType != null)
         {
             if (!eraseTypeParameters)
             {
                 // ensure that type parameters are resolved recursively
                 for (String typeParam : Signature.getTypeArguments(typeSignature))
                 {
                     typeParam = Signature.removeCapture(typeParam);
                     // check and remove bound wildcarding (extends/super/?)
                     if (Signature.getTypeSignatureKind(typeParam) == Signature.WILDCARD_TYPE_SIGNATURE)
                     {
                         // convert ? to Object, strip extends/super
                         if (typeParam.charAt(0) == Signature.C_STAR)
                         {
                             typeParam = TypeConstants.TYPE_JAVAOBJECT;
                         }
                         else
                         {
                             typeParam = typeParam.substring(1);
                         }
                     }
                     final String resolvedParameter = 
                     	resolveSignatureRelative(
                     			// use the enclosing type, 
                     			// *not* the resolved type because 
                     			// we need to resolve in that context
                     			owningType, 
                     				typeParam, eraseTypeParameters);
                     typeParameters.add(resolvedParameter);
                 }
             }
 
             final String  resolvedTypeSignature = 
                 Signature.createTypeSignature
                     (resolvedType.getFullyQualifiedName(), true);
            
 
             if (typeParameters.size() > 0 && !eraseTypeParameters)
             {
                 StringBuffer sb = new StringBuffer(resolvedTypeSignature);
 
                 if (sb.charAt(sb.length()-1) == ';')
                 {
                     sb = sb.delete(sb.length()-1, sb.length());
                 }
                 
                 sb.append("<"); //$NON-NLS-1$
                 for(String param : typeParameters)
                 {
                     //System.out.println("type param: "+resolvedType.getTypeParameter(param));
                     sb.append(param);
                 }
                 
                 // replace the dangling ',' with the closing ">"
                 sb.append(">;"); //$NON-NLS-1$
                 return sb.toString();
             }
             
             return resolvedTypeSignature;
         }
 
         if (Signature.getTypeSignatureKind(typeSignature) == 
                 Signature.CLASS_TYPE_SIGNATURE
             || Signature.getTypeSignatureKind(typeSignature)
                 == Signature.TYPE_VARIABLE_SIGNATURE)
         {
             // if we are unable to resolve, check to see if the owning type has
             // a parameter by this name
             ITypeParameter typeParam = owningType.getTypeParameter(Signature.getSignatureSimpleName(typeSignature));
             
             // if we have a type parameter and it hasn't been resolved to a type,
             // then assume it is a method template placeholder (i.e. T in ArrayList).
             // at runtime these unresolved parameter variables are effectively 
             // turned into Object's.  For example, think List.add(E o).  At runtime,
             // E will behave exactly like java.lang.Object in that signature
             if (typeParam.exists())
             {
                 return TypeConstants.TYPE_JAVAOBJECT;
             }
             
             // TODO: is there a better way to handle a failure to resolve
             // than just garbage out?
             //JSFCommonPlugin.log(new Exception("Failed to resolve type: "+typeSignature), "Failed to resolve type: "+typeSignature); //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         return typeSignature;
     }
 
     private static IType resolveTypeRelative(final IType owningType, final String typeSignature)
     {
         final String fullName = getFullyQualifiedName(typeSignature);
         
         IType resolvedType = null;
         
         try
         {
             // TODO: this call is only supported on sourceTypes!
             String[][] resolved = owningType.resolveType(fullName);
     
             if (resolved != null && resolved.length > 0)
             {
                 resolvedType = owningType.getJavaProject().findType(resolved[0][0], resolved[0][1]);
             }
             else
             {
                 resolvedType = resolveInParents(owningType, fullName);
             }
         }
         catch (JavaModelException jme)
         {
             //  do nothing; newType == null
         }
 
         return resolvedType;
     }
 
     /**
      * @param type
      * @return a type signature for a type
      */
     public static String getSignature(IType type)
     {
         final String fullyQualifiedName = type.getFullyQualifiedName();
         return Signature.createTypeSignature(fullyQualifiedName, true);
     }
 
     
     /**
      * @param owner
      * @param unresolvedSignature
      * @return the resolved method signature for unresolvedSignature in owner
      */
     public static String resolveMethodSignature(final IType  owner, 
                                          final String unresolvedSignature)
     {
         // get the list of parameters
         final String[] parameters = 
             Signature.getParameterTypes(unresolvedSignature);
         
         for (int i = 0; i < parameters.length; i++)
         {
             // try to full resolve the type
             parameters[i] = resolveTypeSignature(owner, parameters[i]);
         }
         
         // resolve return type
         final String resolvedReturn = 
             resolveTypeSignature(owner, 
                                   Signature.getReturnType(unresolvedSignature));
         
         return Signature.createMethodSignature(parameters, resolvedReturn);
     }
     
     /**
      * @param typeSignature     
      * @return a fully qualified Java class name from a type signature
      * i.e. Ljava.lang.String; -> java.lang.String
      */
     public static String getFullyQualifiedName(final String typeSignature)
     {
         final String packageName = Signature.getSignatureQualifier(typeSignature);
         final String typeName = Signature.getSignatureSimpleName(typeSignature);
         return "".equals(packageName) ? typeName : packageName + "." + typeName;  //$NON-NLS-1$//$NON-NLS-2$
     }
     
     private static IType resolveInParents(IType  childType, String fullyQualifiedName)
                                 throws JavaModelException
     {
         IType resolvedType = null;
         
         // not resolved? try the supertypes
         final ITypeHierarchy typeHierarchy =
             childType.newSupertypeHierarchy(new NullProgressMonitor());
         IType[] superTypes = typeHierarchy.getAllSupertypes(childType);
         String[][]   resolved;
         
         LOOP_UNTIL_FIRST_MATCH:
             for (int i = 0; i < superTypes.length; i++)
         {
             IType type = superTypes[i];
             resolved = type.resolveType(fullyQualifiedName);
             
             if (resolved != null && resolved.length > 0)
             {
                 resolvedType = childType.getJavaProject().findType(resolved[0][0], resolved[0][1]);
                 break LOOP_UNTIL_FIRST_MATCH;
             }
         }
 
         return resolvedType;
     }
     
     /**
      * Attempts to get a Java IType for a fully qualified signature.  Note that
      * generic type arguments are generally ignored by JDT when doing such 
      * look ups.
      * 
      * @param javaProject the project context inside which to resolve the type
      * @param fullyResolvedTypeSignature a fully resolved type signature
      * @return the IType if resolved, null otherwise
      */
     public static IType resolveType(final IJavaProject javaProject, final String fullyResolvedTypeSignature)
     {
         final String fullyQualifiedName =
             getFullyQualifiedName(fullyResolvedTypeSignature);
         
         try {
             return javaProject.findType(fullyQualifiedName);
         } catch (JavaModelException e) {
             // accessible problem
             JSFCommonPlugin.log(e);
             return null;
         }
     }
     
     /**
      * @param type
      * @param typeParamSignature -- must be a Type Variable Signature
      * @param typeArguments
      * @return the signature for the type argument in typeArguments that matches the
      * named typeParamSignature in type.
      * @throws IllegalArgumentException if typeParamSignature is not valid
      * 
      * For example, given type for java.util.Map, typeParamSignature == "V" and
      * typeArguments = {Ljava.util.String;, Lcom.test.Blah;}, the result would be
      * the typeArgument that matches "V", which is "Lcom.test.Blah;}
      * 
      * returns null if the match cannot be found.
      */
     public static String matchTypeParameterToArgument(final IType type, final String typeParamSignature, final List<String> typeArguments)
     {
     	if (Signature.getTypeSignatureKind(typeParamSignature) != Signature.TYPE_VARIABLE_SIGNATURE)
     	{
     		throw new IllegalArgumentException();
     	}
     	
         try
         {
             ITypeParameter[] typeParams = type.getTypeParameters();
 
             for (int pos = 0; pos < typeParams.length; pos++)
             {
                 if (typeParams[pos].getElementName().equals(Signature.getSignatureSimpleName(typeParamSignature)))
                 {
                     if (pos < typeArguments.size())
                     {
                         // TODO: should typeArguments.size ever != typeParams.length?
                         return typeArguments.get(pos);
                     }
                 }
             }
         } 
         catch (JavaModelException e) 
         {
             JSFCommonPlugin.log(e);
         }
         
         return null;
     }
     
     /**
      * @param type
      * @param fieldName
      * @return true if fieldName is a member of type.  Note that if type is java.lang.Enum
      * then this will always return true since we cannot know what fields the instance has (it could be any enum)
      */
     public static boolean isEnumMember(final IType type, final String fieldName)
     {
         try
         {
             if (type == null || !isEnumType(type))
             {
                 throw new IllegalArgumentException("type must be non-null and isEnum()==true"); //$NON-NLS-1$
             }
             
             if (fieldName == null)
             {
                 throw new IllegalArgumentException("fieldName must be non-null"); //$NON-NLS-1$
             }
 
             // if type is the java.lang.Enum, always true
             if (TypeConstants.TYPE_ENUM_BASE.equals(Signature.createTypeSignature(type.getFullyQualifiedName(), true)))
             {
                 return true;
             }
             
             final IField field = type.getField(fieldName);
 
             if (field.exists() && field.isEnumConstant())
             {
                 return true;
             }
         }
         catch (JavaModelException jme)
         {
             // fall through and return false
         }
         
         return false;
     }
     
     /**
      * @param typeSig1 the type signature of the first enum. Must be non-null, fully resolved enum type.
      * @param typeSig2 the type signature of the second enum.  Must be non-null, fully resolved enum type.
      * 
      * @return true if typeSig1.compareTo(typeSig2) is a legal operation (won't throw a CCE)
      */
     public static boolean isEnumsCompareCompatible(final String typeSig1, final String typeSig2)
     {
         if (typeSig1 == null || typeSig2 == null)
         {
             throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
         }
         
         if (Signature.getTypeSignatureKind(typeSig1) != Signature.CLASS_TYPE_SIGNATURE
              || Signature.getTypeSignatureKind(typeSig2) != Signature.CLASS_TYPE_SIGNATURE)
         {
             throw new IllegalArgumentException("args must be resolved class types"); //$NON-NLS-1$
         }
         
         // if one or the other is the raw enum type, then they *may* be comparable; we don't know
         if (TypeConstants.TYPE_ENUM_BASE.equals(typeSig1) 
                 || TypeConstants.TYPE_ENUM_BASE.equals(typeSig2))
         {
             return true;
         }
         
         // TODO: support the case of enum base type with generic type argument
         
         // only comparable if is the same class
         return typeSig1.equals(typeSig2);
     }
     
     /**
      * @param typeSig1 the type signature of the first enum. Must be non-null, fully resolved enum type.
      * @param typeSig2 the type signature of the second enum. Must be non-null, fully resolved enum type.
      * @return true if instances typeSig1 and typeSig2 can never be equal due
      * their being definitively different enum types
      */
     public static boolean canNeverBeEqual(final String typeSig1, final String typeSig2)
     {
         if (typeSig1 == null || typeSig2 == null)
         {
             throw new IllegalArgumentException("args must not be null"); //$NON-NLS-1$
         }
         
         if (Signature.getTypeSignatureKind(typeSig1) != Signature.CLASS_TYPE_SIGNATURE
              || Signature.getTypeSignatureKind(typeSig2) != Signature.CLASS_TYPE_SIGNATURE)
         {
             throw new IllegalArgumentException("args must be resolved class types"); //$NON-NLS-1$
         }
 
         // if either one is the base enum type, then we can't be sure
         if (TypeConstants.TYPE_ENUM_BASE.equals(typeSig1) 
                 || TypeConstants.TYPE_ENUM_BASE.equals(typeSig2))
         {
             return false;
         }
 
         // if they are definitely not the same enum types, then their values
         // can never be equal
         return !typeSig1.equals(typeSig2);
     }
     
 
     /**
      * NOTE: we diverge from IType.isEnum() because we also return true if the base type
      * is a java.lang.Enum since we consider this to be "any enumeration type" whereas JDT considers
      * it merely a class since it doesn't use an "enum" keyword declaration.
      * @param type
      * @return true if type is an enum type or is java.lang.Enum
      */
     static boolean isEnumType(IType type)
     {
         if (type == null)
         {
             return false;
         }
         
         // check if it's the enumeration base type
         if (TypeConstants.TYPE_ENUM_BASE.equals(Signature.createTypeSignature(type.getFullyQualifiedName(), true)))
         {
             return true;
         }
     
         try
         {
             return type.isEnum();
         }
         catch (JavaModelException jme)
         {
             // log and fallthrough to return false
             JSFCommonPlugin.log(jme, "Problem resolving isEnum"); //$NON-NLS-1$
         }
         
         // if unresolved assume false
         return false;
     }
     
     private TypeUtil()
     {
         // no external instantiation
     }
 }
