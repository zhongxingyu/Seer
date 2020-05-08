 /*******************************************************************************
  * Copyright (c) 2001, 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.common.internal.types;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.IClassFile;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.context.symbol.IBeanMethodSymbol;
 import org.eclipse.jst.jsf.context.symbol.IBeanPropertySymbol;
 
 /**Provides a cache for java IType properties. It can cache bean property symbols, method symbols,
  * supertypes and implemented interfaces per IType. The cache listens to changes in the java model
  * and invalidates affected properties, but does not update them.
  * 
  * @author Matthias
  */
 public class TypeInfoCache implements IElementChangedListener {
     
     private static TypeInfoCache instance = null;
     
     /**Returns the TypeInfoCache instance.  This instance is considered
      * protected and must not be disposded with disposeInstance.
      * 
      * @return the TypeInfoCache instance
      */
     public static synchronized TypeInfoCache getInstance() {
         if (instance == null) {
             instance = createNewInstance();
         }
         return instance;
     }
     
     /**
      * Create a new instance of the type cache.
      * 
      * @return a new instance of the type info cache.
      */
     public static TypeInfoCache createNewInstance()
     {
         final TypeInfoCache newCache = new TypeInfoCache();
         JavaCore.addElementChangedListener(newCache, ElementChangedEvent.POST_CHANGE);
         return newCache;
     }
     
     /**
      * If cache is not the singleton instance acquired with {@link #getInstance()}
      * then the cache will be disposed and should not be used.  If cache is
      * protected instance, then nothing will happen (the singleton instance
      * cannot be disposed).
      * 
      * @param cache
      */
     public static void disposeInstance(final TypeInfoCache cache)
     {
         if (cache != null 
                 && cache != instance)        
         {
             JavaCore.removeElementChangedListener(cache);
             
             synchronized(cache)
             {
                 if (cache.cachedInfo != null)
                 {
                     cache.cachedInfo.clear();
                 }
                 
                 if (cache.cachedTypesByAffectingTypeRoot != null)
                 {
                     cache.cachedTypesByAffectingTypeRoot.clear();
                 }
                 
                 if (cache.cachedTypesByMissingSupertypename != null)
                 {
                     cache.cachedTypesByMissingSupertypename.clear();
                 }
             }
         }
     }
     
     private final Map<IType, TypeInfo> cachedInfo;
     private final Map<ITypeRoot, Set<IType>> cachedTypesByAffectingTypeRoot;
     private final Map<String, Set<IType>> cachedTypesByMissingSupertypename;
     
     private TypeInfoCache() {
         cachedInfo = new HashMap<IType, TypeInfo>();
         cachedTypesByAffectingTypeRoot = new HashMap();
         cachedTypesByMissingSupertypename = new HashMap(10);
     }
 
     public void elementChanged(ElementChangedEvent event) {
         updateChangedJavaElement(event.getDelta());
     }
     
     /**Returns the cached info({@link TypeInfo}) for a given type. Will
      * return <code>null</code> if no info has been cached or the the type/something it depends on
      * has changed since then.
      * 
      * @param type - the type in question
      * @return a TypeInfo instance that contains all cached info for the given type. May be null.  
      */
     protected TypeInfo getTypeInfo(IType type) {
         TypeInfo info = null;
         if (type != null)
         {
             info = cachedInfo.get(type);
         }
         return info;
     }
 
     /**Returns the cached bean property symbols for a given type. Will return null if no
      * bean property symbols have been cached or the type/something it depends on has changed since
      * then.
      * @param beanType - the bean type in question
      * @return the bean property symbols for the given type. May be null.
      * @see TypeInfoCache#cachePropertySymbols(IType, IBeanPropertySymbol[])
      */
     public synchronized IBeanPropertySymbol[] getCachedPropertySymbols(IType beanType) {
         IBeanPropertySymbol[] props = null;
         
         if (beanType != null)
         {
             TypeInfo typeInfo = getTypeInfo(beanType);
             if (typeInfo != null)
             {
                 props =  typeInfo.getPropertySymbols();
             }
         }
         return props;
     }
 
     /**Returns the cached method symbols for a given type. Will return null if no
      * method symbols have been cached or the type/something it depends on has changed since
      * then.
      * @param beanType - the bean type in question
      * @return the method symbols for the given type. May be null.
      * @see TypeInfoCache#cacheMethodSymbols(IType, IBeanMethodSymbol[])
      */
     public synchronized IBeanMethodSymbol[] getCachedMethodSymbols(IType beanType) {
         IBeanMethodSymbol[]  methods = null;
         
         if (beanType != null)
         {
             TypeInfo typeInfo = getTypeInfo(beanType);
             if (typeInfo != null)
             {
                 methods =  typeInfo.getMethodSymbols();
             }
         }
             
         return methods;
     }
     
     /**Returns the cached supertypes for a given type. Will return null if no supertypes
      * have been cached for this type or if the type/something it depends on has changed since
      * then.
      * @param type - the bean type in question
      * @return the supertypes for the given type. May be null.
      * @see TypeInfoCache#cacheSupertypesFor(IType)
      */
     public synchronized IType[] getCachedSupertypes(IType type) {
         IType[] types = null;
         
         if (type != null)
         {
             TypeInfo typeInfo = getTypeInfo(type);
             if (typeInfo != null)
             {
                 types = typeInfo.getSupertypes();
             }
         }
         
         return types;
     }
     
     /**Returns the cached implemented interfaces for a given type. Will return null if no interfaces
      * have been cached for this type or if the type/something it depends on has changed since
      * then.
      * @param type - the bean type in question
      * @return the interface types implemented by the given type. May be null.
      * @see TypeInfoCache#cacheInterfaceTypesFor(IType)
      */
     public synchronized IType[] getCachedInterfaceTypes(IType type) 
     {
         IType[] types = null;
         
         if (type != null)
         {
             TypeInfo typeInfo = getTypeInfo(type);
             if (typeInfo != null)
             {
                 types = typeInfo.getInterfaceTypes(); 
             }
         }
         
         return types;
     }
     
     /**Caches the given method symbols for the given type. 
      * @param beanType - the type
      * @param methods - the method symbols to cache
      */
     public synchronized void cacheMethodSymbols(IType beanType, IBeanMethodSymbol[] methods) {
         if (beanType != null)
         {
             TypeInfo typeInfo = getOrCreateTypeInfo(beanType);
             if (typeInfo != null) {
                 typeInfo.setMethodSymbols(methods);
             }
         }
     }
 
     /**Caches the given property symbols for the given type. 
      * @param beanType - the type
      * @param properties - the property symbols to cache
      */
     public synchronized void cachePropertySymbols(IType beanType, IBeanPropertySymbol[] properties) {
         if (beanType != null)
         {
             TypeInfo typeInfo = getOrCreateTypeInfo(beanType);
             if (typeInfo != null) {
                 typeInfo.setPropertySymbols(properties);
             }
         }
     }
     
     /**Caches the supertypes for the given type. The supertypes will be calculated (and also returned)
      * by this method.
      * @param type - the type to cache supertypes for
      * @return the supertypes of the given type.
      */
     public synchronized IType[] cacheSupertypesFor(IType type) 
     {
         IType[] types = null;
         
         if (type != null)
         {
             TypeInfo typeInfo = getOrCreateTypeInfo(type);
             
             if (typeInfo != null)
             {
                 types = typeInfo.getSupertypes();
             }
         }
         return types;
     }
 
     /**Caches the interface types for the given type. The interface types will be calculated (and also
      * returned) by this method.
      * @param type - the type to cache interface types for
      * @return the interface types implemented by the given type.
      */
     public synchronized IType[] cacheInterfaceTypesFor(IType type) 
     {
         IType[] types = null;
         
         if (type != null)
         {
             TypeInfo typeInfo = getOrCreateTypeInfo(type);
             if (typeInfo != null)
             {
                 types = typeInfo.getInterfaceTypes();
             }
         }
         return types;
     }
 
     /**Returns the TypeInfo for the given type. If no TypeInfo exists for this type, an empty TypeInfo
      * will be created and cached.
      * @param type - the type in question
      * @return the (modifyable) TypeInfo for the given type
      */
     protected TypeInfo getOrCreateTypeInfo(IType type) {
         TypeInfo typeInfo = getTypeInfo(type);
         if (typeInfo == null) {
             try {
                 final ITypeHierarchy  hierarchy = 
                     type.newSupertypeHierarchy(new NullProgressMonitor());
                 final IType[] supertypes = hierarchy.getAllSuperclasses(type);
                 final IType[] interfaceTypes = hierarchy.getAllInterfaces();
                 final IType[] rootClasses = hierarchy.getRootClasses();
                 List missingSupertypesList = null;
                 for (int i = 0; i < rootClasses.length; i++) {
                     String superclassName = rootClasses[i].getSuperclassName();
                     if (superclassName != null) {
                         if (missingSupertypesList == null) {
                             missingSupertypesList = new ArrayList(1);
                         }
                         superclassName = shortTypename(superclassName);
                         missingSupertypesList.add(superclassName);
                     }
                 }
                 String[] missingSupertypes = null;
                 if (missingSupertypesList != null) {
                     missingSupertypes = (String[]) missingSupertypesList.toArray(new String[missingSupertypesList.size()]);
                 } else {
                     missingSupertypes = TypeInfo.NO_NAMES;
                 }
                 typeInfo = new TypeInfo();
                 typeInfo.setSupertypes(supertypes);
                 typeInfo.setInterfaceTypes(interfaceTypes);
                 typeInfo.setMissingSupertypeNames(missingSupertypes);
                 cachedInfo.put(type, typeInfo);
                 registerCachedType(type, typeInfo);
             } catch (JavaModelException e) {
                 JSFCommonPlugin.log(e);
             }
         }
         return typeInfo;
     }
 
     /**Returns the typename fragment after the last "." (which in most cases is identical to the
      * unqualified typename).
      * Used only to make sure that if n1 and n2 are names of the same type
      * shortname(n1) equals shortname(2) even if one name is qualified and one not.
      * @param typename
      * @return the typename fragment after the last "."
      */
     private String shortTypename(String typename) {
         int pos = typename.lastIndexOf('.');
         if (pos >= 0) {
             typename = typename.substring(pos + 1);
         }
         return typename;
     }
     
     /**
      * Registers the given type for all ITypeRoot's it depends on, so that it can be uncached if
      * one of this ITypeRoot's has changed. The type must be unregistered when it should not be watched
      * anymore.
      * @param type - the type
      * @param typeInfo - TypeInfo of the given type
      * @see TypeInfoCache#unregisterCachedType(IType, TypeInfo)
      */
     protected void registerCachedType(IType type, TypeInfo typeInfo) {
         registerTypeForTypeRoot(type, type.getTypeRoot());
         IType[] supertypes = typeInfo.getSupertypes();
         for (int i = 0; i < supertypes.length; i++) {
             registerTypeForTypeRoot(type, supertypes[i].getTypeRoot());
         }
         String[] missingSupertypeNames = typeInfo.getMissingSupertypeNames();
         if (missingSupertypeNames != null) {
             for (int i = 0; i < missingSupertypeNames.length; i++) {
                 registerTypeForMissingSupertype(type, missingSupertypeNames[i]);
             }
         }
     }
 
     private void registerTypeForTypeRoot(IType type, ITypeRoot typeRoot) {
         Set dependentTypes = cachedTypesByAffectingTypeRoot.get(typeRoot);
         if (dependentTypes == null) {
             dependentTypes = new HashSet(5);
             cachedTypesByAffectingTypeRoot.put(typeRoot, dependentTypes);
         }
         dependentTypes.add(type);
     }
 
     private void registerTypeForMissingSupertype(IType type, String supertype) {
         Set dependentTypes = cachedTypesByMissingSupertypename.get(supertype);
         if (dependentTypes == null) {
             dependentTypes = new HashSet(5);
             cachedTypesByMissingSupertypename.put(supertype, dependentTypes);
         }
         dependentTypes.add(type);
     }
 
     /**Unregisters the given type for all ITypeRoot's it depended on.
      * @param type - the type
      * @param typeInfo - TypeInfo of the given type
      */
     protected void unregisterCachedType(IType type, TypeInfo typeInfo) {
         unregisterTypeForTypeRoot(type, type.getTypeRoot());
         IType[] supertypes = typeInfo.getSupertypes();
         for (int i = 0; i < supertypes.length; i++) {
             unregisterTypeForTypeRoot(type, supertypes[i].getTypeRoot());
         }
         String[] missingSupertypeNames = typeInfo.getMissingSupertypeNames();
         if (missingSupertypeNames != null) {
             for (int i = 0; i < missingSupertypeNames.length; i++) {
                 unregisterTypeForMissingSupertype(type, missingSupertypeNames[i]);
             }
         }
     }
 
     private void unregisterTypeForTypeRoot(IType type, ITypeRoot typeRoot) {
         Set dependentTypes = cachedTypesByAffectingTypeRoot.get(typeRoot);
         if (dependentTypes != null) {
             dependentTypes.remove(type);
             if (dependentTypes.isEmpty()) {
                 cachedTypesByAffectingTypeRoot.remove(typeRoot);
             }
         }
     }
     
     private void unregisterTypeForMissingSupertype(IType type, String supertype) {
         Set dependentTypes = cachedTypesByMissingSupertypename.get(supertype);
         if (dependentTypes != null) {
             dependentTypes.remove(type);
             if (dependentTypes.isEmpty()) {
                 cachedTypesByMissingSupertypename.remove(supertype);
             }
         }
     }
     
     /**This will remove all cached info for all types.
      */
     protected synchronized void uncacheAllTypes() {
         cachedInfo.clear();
         cachedTypesByAffectingTypeRoot.clear();
         cachedTypesByMissingSupertypename.clear();
     }
     
     /**Removes all cached info for all types that are subtypes of a type of the given ITypeRoot. 
      * @param typeRoot
      */
     protected synchronized void uncacheAffectedTypes(ITypeRoot typeRoot) {
         Collection affectedTypes = cachedTypesByAffectingTypeRoot.get(typeRoot);
         if (affectedTypes != null && !affectedTypes.isEmpty()) {
             List affectedTypesCopy = new ArrayList(affectedTypes);
             for (Iterator it = affectedTypesCopy.iterator(); it.hasNext(); ) {
                 IType cachedType = (IType) it.next();
                 TypeInfo typeInfo = cachedInfo.remove(cachedType);
                 unregisterCachedType(cachedType, typeInfo);
             }
         }
     }
     
     /**Removes all cached info for all types (or subtypes of types) that specify a supertype
      * that has a name similar to the given name. 
      * @param supertypename - the missing supertype name. May be qualified or not
      */
     protected synchronized void uncacheTypesWithMissingSupertype(String supertypename) {
         Collection affectedTypes = cachedTypesByMissingSupertypename.get(shortTypename(supertypename));
         if (affectedTypes != null && !affectedTypes.isEmpty()) {
             List affectedTypesCopy = new ArrayList(affectedTypes);
             for (Iterator it = affectedTypesCopy.iterator(); it.hasNext(); ) {
                 IType cachedType = (IType) it.next();
                 TypeInfo typeInfo = cachedInfo.remove(cachedType);
                 unregisterCachedType(cachedType, typeInfo);
             }
         }
     }
     
     /**Removes all cached info that may be affected by the given change.
      * @param delta - the change in the java model
      */
     protected void updateChangedJavaElement(IJavaElementDelta delta) {
         IJavaElement element= delta.getElement();
         switch (element.getElementType()) {
             case IJavaElement.JAVA_MODEL:
                 updateChangedJavaModel(delta, element);
                 break;
             case IJavaElement.JAVA_PROJECT:
                 updateChangedJavaProject(delta, element);
                 break;
             case IJavaElement.PACKAGE_FRAGMENT_ROOT:
                 updateChangedPackageFragmentRoot(delta, element);
                 break;
             case IJavaElement.PACKAGE_FRAGMENT:
                 updateChangedPackageFragment(delta, (IPackageFragment) element);
                 break;
             case IJavaElement.CLASS_FILE:
             case IJavaElement.COMPILATION_UNIT:
                 updateChangedOpenable(delta, element);
                 break;
         }
     }
     
     private void updateChangedChildren(IJavaElementDelta delta) {
         if ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) > 0) {
             IJavaElementDelta[] children= delta.getAffectedChildren();
             for (int i= 0; i < children.length; i++) {
                 updateChangedJavaElement(children[i]);
             }
         }
     }
 
     private void updateChangedJavaModel(IJavaElementDelta delta, IJavaElement element) {
         switch (delta.getKind()) {
             case IJavaElementDelta.ADDED :
             case IJavaElementDelta.REMOVED :
                 uncacheAllTypes();
                 break;
             case IJavaElementDelta.CHANGED :
                 updateChangedChildren(delta);
                 break;
         }
     }
 
     private void updateChangedJavaProject(IJavaElementDelta delta, IJavaElement element) {
         int kind = delta.getKind();
         int flags = delta.getFlags();
         if ((flags & IJavaElementDelta.F_OPENED) != 0) {
             kind = IJavaElementDelta.ADDED; // affected in the same way
         }
         if ((flags & IJavaElementDelta.F_CLOSED) != 0) {
             kind = IJavaElementDelta.REMOVED; // affected in the same way
         }
         switch (kind) {
             case IJavaElementDelta.ADDED :
             case IJavaElementDelta.REMOVED :
                 uncacheAllTypes();
                 break;
             case IJavaElementDelta.CHANGED :
                 updateChangedChildren(delta);
                 break;
         }
     }
 
    private void updateChangedPackageFragment(IJavaElementDelta delta, IPackageFragment element) {
         switch (delta.getKind()) {
             case IJavaElementDelta.ADDED :
                 // if the package fragment is in the projects being considered, this could
                 // introduce new types, changing the hierarchy
             case IJavaElementDelta.REMOVED :
                 // is a change if the package fragment contains supertypes?
                 uncacheAllTypes();
                 break;
             case IJavaElementDelta.CHANGED :
                 // look at the files in the package fragment
                 updateChangedChildren(delta);
         }
     }
 
     private void updateChangedPackageFragmentRoot(IJavaElementDelta delta, IJavaElement element) {
         switch (delta.getKind()) {
             case IJavaElementDelta.ADDED :
             case IJavaElementDelta.REMOVED :
                 uncacheAllTypes();
                 break;
             case IJavaElementDelta.CHANGED :
                 int flags = delta.getFlags();
                 if (((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) > 0)||(flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) > 0) {
                     uncacheAllTypes();
                 } else {
                     updateChangedChildren(delta);
                 }
                 break;
         }
     }
 
     /**Removes all cached info that may be affected by the change in this IOpenable
      * @param delta - the change in the java model
      * @param element - the (changed) IOpenable considered
      */
     protected void updateChangedOpenable(IJavaElementDelta delta, IJavaElement element) {
         if (element instanceof ITypeRoot) {
             ITypeRoot typeRoot = (ITypeRoot) element;
             uncacheAffectedTypes(typeRoot);
             // Creates missing superclass for any cached type?
             if (delta.getKind() == IJavaElementDelta.ADDED) {
                 if (typeRoot instanceof ICompilationUnit) {
                     ICompilationUnit cu = (ICompilationUnit) typeRoot;
                     try {
                         IType[] types = cu.getAllTypes();
                         for (int i = 0; i < types.length; i++) {
                             uncacheTypesWithMissingSupertype(types[i].getElementName());
                         }
                     } catch (JavaModelException e) {
                        JSFCommonPlugin.log(IStatus.INFO, "Unable to get types for compilation unit " + cu, e); //$NON-NLS-1$
                         uncacheAllTypes();
                     }
                 } else if (typeRoot instanceof IClassFile) {
                     IClassFile cf = (IClassFile) typeRoot;
                     IType type = cf.getType();
                     uncacheTypesWithMissingSupertype(type.getElementName());
                 }
             }
         }
     }
 
 }
