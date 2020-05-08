 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.AnonymousClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.InnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetAnonymousClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterizable;
 
 
 /**
  * ^邽߂̃[eBeBNX
  * 
  * @author higo
  * 
  */
 public final class NameResolver {
 
     /**
      * ŗ^ꂽNX̐eNXłCONX(ExternalClassInfo)ł̂ԂD NXKwIɍłʂɈʒuONXԂD
      * YNX݂Ȃꍇ́C null ԂD
      * 
      * @param classInfo ΏۃNX
      * @return ŗ^ꂽNX̐eNXłCNXKwIɍłʂɈʒuONX
      */
     public static ExternalClassInfo getExternalSuperClass(final ClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new IllegalArgumentException();
         }
 
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
 
             if (superClassInfo instanceof ExternalClassInfo) {
                 return (ExternalClassInfo) superClassInfo;
             }
 
             final ExternalClassInfo superSuperClassInfo = NameResolver
                     .getExternalSuperClass(superClassInfo);
             if (null != superSuperClassInfo) {
                 return superSuperClassInfo;
             }
         }
 
         return null;
     }
 
     /**
      * ŗ^ꂽNXNXƂĎCłÓiCi[NXłȂjNXԂ
      * 
      * @param innerClass Ci[NX
      * @return łÕNX
      */
     public static ClassInfo getOuterstClass(final InnerClassInfo innerClass) {
 
         if (null == innerClass) {
             throw new IllegalArgumentException();
         }
 
         final ClassInfo outerClass = innerClass.getOuterClass();
         return outerClass instanceof InnerClassInfo ? NameResolver
                 .getOuterstClass((InnerClassInfo) outerClass) : outerClass;
     }
 
     /**
      * ŗ^ꂽNX̗p\ȓNX SortedSet Ԃ
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̗p\ȓNX SortedSet
      */
     public static SortedSet<InnerClassInfo> getAvailableInnerClasses(final ClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         final SortedSet<InnerClassInfo> innerClasses = new TreeSet<InnerClassInfo>();
         for (final InnerClassInfo innerClass : classInfo.getInnerClasses()) {
 
             innerClasses.add(innerClass);
             final SortedSet<InnerClassInfo> innerClassesInInnerClass = NameResolver
                     .getAvailableInnerClasses((ClassInfo) innerClass);
             innerClasses.addAll(innerClassesInInnerClass);
         }
 
         return Collections.unmodifiableSortedSet(innerClasses);
     }
 
     public static void getAvailableSuperClasses(final ClassInfo subClass,
             final ClassInfo superClass, final List<ClassInfo> availableClasses) {
 
         if ((null == subClass) || (null == superClass) || (null == availableClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (availableClasses.contains(superClass)) {
             return;
         }
 
         // NXǉ
         // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
         if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
             if (superClass.isInheritanceVisible() || superClass.isNamespaceVisible()) {
                 availableClasses.add(superClass);
                 for (final InnerClassInfo innerClass : superClass.getInnerClasses()) {
                     NameResolver.getAvailableInnerClasses((ClassInfo) innerClass, availableClasses);
                 }
             }
 
             //qNXƐeNX̖OԂႤꍇ́Cp΂悢
         } else {
 
             if (superClass.isInheritanceVisible()) {
                 availableClasses.add(superClass);
                 for (final InnerClassInfo innerClass : superClass.getInnerClasses()) {
                     NameResolver.getAvailableInnerClasses((ClassInfo) innerClass, availableClasses);
                 }
             }
         }
 
         // eNXǉ
         for (final ClassInfo superSuperClass : ClassTypeInfo.convert(superClass.getSuperClasses())) {
             NameResolver.getAvailableSuperClasses(subClass, superSuperClass, availableClasses);
         }
     }
 
     public static void getAvailableInnerClasses(final ClassInfo classInfo,
             final List<ClassInfo> availableClasses) {
 
         if ((null == classInfo) || (null == availableClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (availableClasses.contains(classInfo)) {
             return;
         }
 
         // Ci[NX̏ꍇ͒ǉɏI
         if (classInfo instanceof AnonymousClassInfo) {
             return;
         }
 
         availableClasses.add(classInfo);
 
         // NXǉ
         for (final InnerClassInfo innerClass : classInfo.getInnerClasses()) {
             NameResolver.getAvailableInnerClasses((ClassInfo) innerClass, availableClasses);
         }
 
         return;
     }
 
     /**
      * ŗ^ꂽNX^ŌĂяo\ȃRXgN^ListԂ
      * 
      * @param classType
      * @return
      */
     public static final List<ConstructorInfo> getAvailableConstructors(final ClassTypeInfo classType) {
 
         final List<ConstructorInfo> constructors = new LinkedList<ConstructorInfo>();
         final ClassInfo classInfo = classType.getReferencedClass();
 
         constructors.addAll(classInfo.getDefinedConstructors());
 
         for (final ClassTypeInfo superClassType : classInfo.getSuperClasses()) {
             final List<ConstructorInfo> superConstructors = NameResolver
                     .getAvailableConstructors(superClassType);
             constructors.addAll(superConstructors);
         }
 
         return constructors;
     }
 
     /**
      * ŗ^ꂽNX̒ڂ̃Ci[NXԂDeNXŒ`ꂽCi[NX܂܂D
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̒ڂ̃Ci[NXCeNXŒ`ꂽCi[NX܂܂D
      */
     public static final SortedSet<InnerClassInfo> getAvailableDirectInnerClasses(
             final ClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new IllegalArgumentException();
         }
 
         final SortedSet<InnerClassInfo> availableDirectInnerClasses = new TreeSet<InnerClassInfo>();
 
         // ŗ^ꂽNX̒ڂ̃Ci[NXǉ
         availableDirectInnerClasses.addAll(classInfo.getInnerClasses());
 
         // eNXɑ΂čċAIɏ
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
 
             final SortedSet<InnerClassInfo> availableDirectInnerClassesInSuperClass = NameResolver
                     .getAvailableDirectInnerClasses((ClassInfo) superClassInfo);
             availableDirectInnerClasses.addAll(availableDirectInnerClassesInSuperClass);
         }
 
         return Collections.unmodifiableSortedSet(availableDirectInnerClasses);
     }
 
     public static final List<TypeParameterInfo> getAvailableTypeParameters(
             final TypeParameterizable unit) {
 
         if (null == unit) {
             throw new IllegalArgumentException();
         }
 
         final List<TypeParameterInfo> typeParameters = new LinkedList<TypeParameterInfo>();
 
         typeParameters.addAll(unit.getTypeParameters());
         final TypeParameterizable outerUnit = unit.getOuterTypeParameterizableUnit();
         if (null != outerUnit) {
             typeParameters.addAll(getAvailableTypeParameters(outerUnit));
         }
 
         return Collections.unmodifiableList(typeParameters);
     }
 
     /**
      * Ŏw肳ꂽNXŗp\ȁCNXListԂ
      * 
      * @param usingClass
      * @return
      */
     public static synchronized List<ClassInfo> getAvailableClasses(final ClassInfo usingClass) {
 
         if (CLASS_CACHE.containsKey(usingClass)) {
             return CLASS_CACHE.get(usingClass);
         } else {
             final List<ClassInfo> _SAME_CLASS = new NonDuplicationLinkedList<ClassInfo>();
             final List<ClassInfo> _INHERITANCE = new NonDuplicationLinkedList<ClassInfo>();
             final List<ClassInfo> _SAME_NAMESPACE = new NonDuplicationLinkedList<ClassInfo>();
 
             getAvailableClasses(usingClass, usingClass, new HashSet<ClassInfo>(), _SAME_CLASS,
                     _INHERITANCE, _SAME_NAMESPACE);
 
             final List<ClassInfo> availableClasses = new NonDuplicationLinkedList<ClassInfo>();
             availableClasses.addAll(_SAME_CLASS);
             availableClasses.addAll(_INHERITANCE);
             availableClasses.addAll(_SAME_NAMESPACE);
 
             final ClassInfo outestClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             availableClasses.addAll(DataManager.getInstance().getClassInfoManager().getClassInfos(
                     outestClass.getNamespace()));
             CLASS_CACHE.put(usingClass, availableClasses);
             return Collections.unmodifiableList(availableClasses);
         }
     }
 
     private static void getAvailableClasses(final ClassInfo usedClass, final ClassInfo usingClass,
             Set<ClassInfo> checkedClasses, final List<ClassInfo> _SAME_CLASS,
             final List<ClassInfo> _INHERITANCE, final List<ClassInfo> _SAME_NAMESPACE) {
 
         if (checkedClasses.contains(usedClass)) {
             return;
         }
 
         if (usedClass instanceof TargetAnonymousClassInfo) {
             return;
         }
 
         checkedClasses.add(usedClass);
 
         // usedp\ǂ𒲍C\ł΃Xgɒǉ
         if (!addAvailableClass(usedClass, usingClass, _SAME_CLASS, _INHERITANCE, _SAME_NAMESPACE)) {
             return;
         }
 
         for (final InnerClassInfo innerClass : usedClass.getInnerClasses()) {
             getAvailableClasses((ClassInfo) innerClass, usingClass, checkedClasses, _SAME_CLASS,
                     _INHERITANCE, _SAME_NAMESPACE);
         }
 
         if (usedClass instanceof InnerClassInfo) {
             final ClassInfo outerUsedClass = ((InnerClassInfo) usedClass).getOuterClass();
             getAvailableClasses(outerUsedClass, usingClass, checkedClasses, _SAME_CLASS,
                     _INHERITANCE, _SAME_NAMESPACE);
         }
 
         for (final ClassTypeInfo superUsedType : usedClass.getSuperClasses()) {
             final ClassInfo superUsedClass = superUsedType.getReferencedClass();
             getAvailableClasses(superUsedClass, usingClass, checkedClasses, _SAME_CLASS,
                     _INHERITANCE, _SAME_NAMESPACE);
         }
     }
 
     /**
      * usedClassusingClassɂăANZX\ԂD
      * ȂCusedClasspublicłꍇ͍lĂȂD
      * publicŃANZX\ǂ́CC|[gׂȂ΂킩Ȃ
      * 
      * @param usedClass
      * @param usingClass
      * @return
      */
     private static boolean addAvailableClass(final ClassInfo usedClass, final ClassInfo usingClass,
             final List<ClassInfo> _SAME_CLASS, final List<ClassInfo> _INHERITANCE,
             final List<ClassInfo> _SAME_NAMESPACE) {
 
         // usingusedł΁Cp\
         if (usingClass.equals(usedClass)) {
             _SAME_CLASS.add(usedClass);
             return true;
         }
 
         // usedCi[NX̂Ƃ
         if (usedClass instanceof InnerClassInfo) {
 
             final ClassInfo outerUsedClass = ((InnerClassInfo) usedClass).getOuterClass();
 
             //outerNX̓ANZX
             if (outerUsedClass.equals(usingClass)) {
                 _SAME_CLASS.add(usedClass);
                 return true;
             }
 
             // usingCi[NX̏ꍇ́CusedƓNX̃Ci[NXǂ𒲂ׂ
             if (usingClass instanceof InnerClassInfo) {
                 final ClassInfo outerUsingClass = ((InnerClassInfo) usingClass).getOuterClass();
                 if (outerUsedClass.equals(outerUsingClass)) {
                     _SAME_CLASS.add(usedClass);
                     return true;
                 }
             }
 
             // outerNXusing̖OԂꍇ
             if (outerUsedClass.getNamespace().equals(usingClass.getNamespace())) {
                 if (outerUsedClass instanceof InnerClassInfo) {
                     _SAME_CLASS.add(usedClass);
                     return true;
                 } else {
                     _SAME_NAMESPACE.add(usedClass);
                     return true;
                 }
             }
 
             // outerNXCi[NXłȂꍇ
             if (!(outerUsedClass instanceof InnerClassInfo)) {
                 final ClassInfo outestUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                         .getOutestClass((InnerClassInfo) usingClass)
                         : usingClass;
 
                 // OԂ
                 if (outerUsedClass.getNamespace().equals(outestUsingClass.getNamespace())) {
 
                     ClassInfo outerUsingClass = usingClass;
                     while (true) {
                         if (outerUsingClass.isSubClass(outerUsedClass)) {
                             _INHERITANCE.add(usedClass);
                             return true;
                         }
 
                         if (!(outerUsingClass instanceof InnerClassInfo)) {
                             break;
                         }
 
                         outerUsingClass = ((InnerClassInfo) outerUsingClass).getOuterClass();
                     }
                 }
 
                 // OԂႤ
                 else {
                     if (usedClass.isInheritanceVisible()) {
 
                         ClassInfo outerUsingClass = usingClass;
                         while (true) {
                             if (outerUsingClass.isSubClass(outerUsedClass)) {
                                 _INHERITANCE.add(usedClass);
                                 return true;
                             }
 
                             if (!(outerUsingClass instanceof InnerClassInfo)) {
                                 break;
                             }
 
                             outerUsingClass = ((InnerClassInfo) outerUsingClass).getOuterClass();
                         }
                     }
                 }
             }
         }
 
         // usedCi[NXłȂƂ
         else {
 
             //usedusingNXiĵƂ
             final ClassInfo outestUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             if (outestUsingClass.equals(usedClass)) {
                 _SAME_CLASS.add(usedClass);
                 return true;
             }
 
             if (outestUsingClass.getNamespace().equals(usedClass.getNamespace())) {
                 _SAME_NAMESPACE.add(usedClass);
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * gpNXƎgpNX^邱ƂɂCp\ȃ\bhListԂ
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return
      */
     public static synchronized List<MethodInfo> getAvailableMethods(final ClassInfo usedClass,
             final ClassInfo usingClass) {
 
         final boolean hasCache = METHOD_CACHE.hasCash(usedClass, usingClass);
         if (hasCache) {
             return METHOD_CACHE.getCache(usedClass, usingClass);
         } else {
             final List<MethodInfo> methods = getAvailableMethods(usedClass, usingClass,
                     new HashSet<ClassInfo>());
             METHOD_CACHE.putCache(usedClass, usingClass, methods);
             return methods;
         }
     }
 
     /**
      * gpNXƎgpNX^邱ƂɂCp\ȃtB[hListԂ
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return
      */
     public static synchronized List<FieldInfo> getAvailableFields(final ClassInfo usedClass,
             final ClassInfo usingClass) {
 
         final boolean hasCache = FIELD_CACHE.hasCash(usedClass, usingClass);
         if (hasCache) {
             return FIELD_CACHE.getCache(usedClass, usingClass);
         } else {
             final List<FieldInfo> fields = getAvailableFields(usedClass, usingClass,
                     new HashSet<ClassInfo>());
             FIELD_CACHE.putCache(usedClass, usingClass, fields);
             return fields;
         }
     }
 
     private static List<MethodInfo> getAvailableMethods(final ClassInfo usedClass,
             final ClassInfo usingClass, final Set<ClassInfo> checkedClasses) {
 
         // łɃ`FbNĂNXłΉɔ
         if (checkedClasses.contains(usedClass)) {
             return Collections.<MethodInfo> emptyList();
         }
 
         // `FbNς݃NXɒǉ
         checkedClasses.add(usedClass);
 
         // usedɒ`Ă郁\bĥCp\Ȃ̂ǉ
         final List<MethodInfo> availableMethods = new NonDuplicationLinkedList<MethodInfo>();
         availableMethods.addAll(extractAvailableMethods(usedClass, usingClass));
 
         // used̊ONX`FbN
         if (usedClass instanceof InnerClassInfo) {
             final ClassInfo outerClass = ((InnerClassInfo) usedClass).getOuterClass();
             availableMethods.addAll(getAvailableMethods(outerClass, usingClass, checkedClasses));
         }
 
         // eNX`FbN
         for (final ClassTypeInfo superClassType : usedClass.getSuperClasses()) {
             final ClassInfo superClass = superClassType.getReferencedClass();
             availableMethods.addAll(getAvailableMethods(superClass, usingClass, checkedClasses));
         }
 
        // I[o[ChɂĂяosƂȂ\bh͍폜
        final List<MethodInfo> deletedMethods = new NonDuplicationLinkedList<MethodInfo>();
        for (final MethodInfo method : availableMethods) {
            for (final MethodInfo overridee : method.getOverridees()) {
                deletedMethods.add(overridee);
            }
        }
        availableMethods.removeAll(deletedMethods);

         return availableMethods;
     }
 
     private static List<FieldInfo> getAvailableFields(final ClassInfo usedClass,
             final ClassInfo usingClass, final Set<ClassInfo> checkedClasses) {
 
         // łɃ`FbNĂNXłΉɔ
         if (checkedClasses.contains(usedClass)) {
             return Collections.<FieldInfo> emptyList();
         }
 
         // `FbNς݃NXɒǉ
         checkedClasses.add(usedClass);
 
         // usedɒ`Ă郁\bĥCp\Ȃ̂ǉ
         final List<FieldInfo> availableFields = new NonDuplicationLinkedList<FieldInfo>();
         availableFields.addAll(extractAvailableFields(usedClass, usingClass));
 
         // used̊ONX`FbN
         if (usedClass instanceof InnerClassInfo) {
             final ClassInfo outerClass = ((InnerClassInfo) usedClass).getOuterClass();
             availableFields.addAll(getAvailableFields(outerClass, usingClass, checkedClasses));
         }
 
         // eNX`FbN
         for (final ClassTypeInfo superClassType : usedClass.getSuperClasses()) {
             final ClassInfo superClass = superClassType.getReferencedClass();
             availableFields.addAll(getAvailableFields(superClass, usingClass, checkedClasses));
         }
 
         return availableFields;
     }
 
     private static List<MethodInfo> extractAvailableMethods(final ClassInfo usedClass,
             final ClassInfo usingClass) {
 
         final List<MethodInfo> availableMethods = new NonDuplicationLinkedList<MethodInfo>();
 
         // usingusedꍇ́CׂẴ\bhgp\
         {
             final ClassInfo tmpUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             final ClassInfo tmpUsedClass = usedClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usedClass)
                     : usedClass;
             if (tmpUsingClass.getNamespace().equals(tmpUsedClass.getNamespace())) {
                 availableMethods.addAll(usedClass.getDefinedMethods());
             }
         }
 
         // usingusedƓpbP[Wł΁Cprivate ȊÕ\bhgp\
         {
             final ClassInfo tmpUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             final ClassInfo tmpUsedClass = usedClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usedClass)
                     : usedClass;
             if (tmpUsingClass.getNamespace().equals(tmpUsedClass.getNamespace())) {
                 for (final MethodInfo method : usedClass.getDefinedMethods()) {
                     if (method.isNamespaceVisible()) {
                         availableMethods.add(method);
                     }
                 }
             }
         }
 
         // usingused̃TuNXł,protectedȊÕ\bhgp\
         if (usingClass.isSubClass(usedClass)) {
             for (final MethodInfo method : usedClass.getDefinedMethods()) {
                 if (method.isInheritanceVisible()) {
                     availableMethods.add(method);
                 }
             }
         }
 
         // usingusedƊ֌ŴȂNXł΁Cpublic̃\bhp\
         for (final MethodInfo method : usedClass.getDefinedMethods()) {
             if (method.isPublicVisible()) {
                 availableMethods.add(method);
             }
         }
 
         return availableMethods;
     }
 
     private static List<FieldInfo> extractAvailableFields(final ClassInfo usedClass,
             final ClassInfo usingClass) {
 
         final List<FieldInfo> availableFields = new NonDuplicationLinkedList<FieldInfo>();
 
         // usingusedꍇ́CׂẴtB[hgp\
         {
             final ClassInfo tmpUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             final ClassInfo tmpUsedClass = usedClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usedClass)
                     : usedClass;
             if (tmpUsingClass.getNamespace().equals(tmpUsedClass.getNamespace())) {
                 availableFields.addAll(usedClass.getDefinedFields());
             }
         }
 
         // usingusedƓpbP[Wł΁Cprivate ȊÕtB[hgp\
         {
             final ClassInfo tmpUsingClass = usingClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usingClass)
                     : usingClass;
             final ClassInfo tmpUsedClass = usedClass instanceof InnerClassInfo ? TargetInnerClassInfo
                     .getOutestClass((InnerClassInfo) usedClass)
                     : usedClass;
             if (tmpUsingClass.getNamespace().equals(tmpUsedClass.getNamespace())) {
                 for (final FieldInfo field : usedClass.getDefinedFields()) {
                     if (field.isNamespaceVisible()) {
                         availableFields.add(field);
                     }
                 }
             }
         }
 
         // usingused̃TuNXł,protectedȊÕtB[hgp\
         if (usingClass.isSubClass(usedClass)) {
             for (final FieldInfo field : usedClass.getDefinedFields()) {
                 if (field.isInheritanceVisible()) {
                     availableFields.add(field);
                 }
             }
         }
 
         // usingusedƊ֌ŴȂNXł΁Cpublic̃tB[hp\
         for (final FieldInfo field : usedClass.getDefinedFields()) {
             if (field.isPublicVisible()) {
                 availableFields.add(field);
             }
         }
 
         return availableFields;
     }
 
     private static final Map<ClassInfo, List<ClassInfo>> CLASS_CACHE = new HashMap<ClassInfo, List<ClassInfo>>();
 
     private static final Cache<MethodInfo> METHOD_CACHE = new Cache<MethodInfo>();
 
     private static final Cache<FieldInfo> FIELD_CACHE = new Cache<FieldInfo>();
 
     /**
      * gpNXƎgpNX̊֌W痘p\ȃo[̃LbV~Ă߂̃NX
      * 
      * @author higo
      *
      * @param <T>
      */
     static class Cache<T> {
 
         private final ConcurrentMap<ClassInfo, ConcurrentMap<ClassInfo, List<T>>> firstCache;
 
         Cache() {
             this.firstCache = new ConcurrentHashMap<ClassInfo, ConcurrentMap<ClassInfo, List<T>>>();
         }
 
         boolean hasCash(final ClassInfo usedClass, final ClassInfo usingClass) {
 
             final boolean hasSecondCache = this.firstCache.containsKey(usedClass);
             if (!hasSecondCache) {
                 return false;
             }
 
             final ConcurrentMap<ClassInfo, List<T>> secondCache = this.firstCache.get(usedClass);
             final boolean hasThirdCache = secondCache.containsKey(usingClass);
             return hasThirdCache;
         }
 
         List<T> getCache(final ClassInfo usedClass, final ClassInfo usingClass) {
 
             final ConcurrentMap<ClassInfo, List<T>> secondCache = this.firstCache.get(usedClass);
             if (null == secondCache) {
                 return null;
             }
 
             return secondCache.get(usingClass);
         }
 
         void putCache(final ClassInfo usedClass, final ClassInfo usingClass, final List<T> cache) {
 
             ConcurrentMap<ClassInfo, List<T>> secondCache = this.firstCache.get(usedClass);
             if (null == secondCache) {
                 secondCache = new ConcurrentHashMap<ClassInfo, List<T>>();
                 this.firstCache.put(usedClass, secondCache);
             }
 
             secondCache.put(usingClass, cache);
         }
     }
 
     @SuppressWarnings("serial")
     private static class NonDuplicationLinkedList<T> extends LinkedList<T> {
 
         @Override
         public boolean add(final T element) {
             if (super.contains(element)) {
                 return false;
             } else {
                 return super.add(element);
             }
         }
 
         @Override
         public boolean addAll(Collection<? extends T> elements) {
 
             boolean added = false;
             for (final T element : elements) {
                 if (this.add(element)) {
                     added = true;
                 }
             }
 
             return added;
         }
     }
 }
