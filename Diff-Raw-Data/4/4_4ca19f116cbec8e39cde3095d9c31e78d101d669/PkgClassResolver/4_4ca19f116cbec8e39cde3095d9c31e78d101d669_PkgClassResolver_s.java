 package cs444.types;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import cs444.CompilerException;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.ast.AInterfaceOrClassSymbol;
 import cs444.parser.symbols.ast.AMethodSymbol;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ImplementationLevel;
 import cs444.parser.symbols.ast.AModifiersOptSymbol.ProtectionLevel;
 import cs444.parser.symbols.ast.ConstructorSymbol;
 import cs444.parser.symbols.ast.DclSymbol;
 import cs444.parser.symbols.ast.MethodOrConstructorSymbol;
 import cs444.parser.symbols.ast.NameSymbol;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 import cs444.types.exceptions.CircularDependancyException;
 import cs444.types.exceptions.DuplicateDeclarationException;
 import cs444.types.exceptions.IllegalExtendsException;
 import cs444.types.exceptions.IllegalMethodOverloadException;
 import cs444.types.exceptions.ImplicitStaticConversionException;
 import cs444.types.exceptions.UndeclaredException;
 import cs444.types.exceptions.UnimplementedException;
 
 public class PkgClassResolver {
     private static final String DEFAULT_PKG = "!default";
     private static final String LANG = "java.lang";
     private static final String OBJECT = LANG + ".Object";
 
     private final AInterfaceOrClassSymbol start;
 
     private final Map<String, AMethodSymbol> methodMap = new HashMap<String, AMethodSymbol>();
     private final Map<String, AMethodSymbol> smethodMap = new HashMap<String, AMethodSymbol>();
     private final Map<String, DclSymbol> fieldMap = new HashMap<String, DclSymbol>();
     private final Map<String, DclSymbol> sfieldMap = new HashMap<String, DclSymbol>();
 
     private final Map<String, DclSymbol> hfieldMap = new HashMap<String, DclSymbol>();
     private final Map<String, DclSymbol> hsfieldMap = new HashMap<String, DclSymbol>();
 
     private final Map<String, PkgClassResolver> namedMap = new HashMap<String, PkgClassResolver>();
     private final Map<String, PkgClassResolver> samepkgMap = new HashMap<String, PkgClassResolver>();
     private final Map<String, PkgClassResolver> staredMap = new HashMap<String, PkgClassResolver>();
     private final Set<String> assignableTo = new HashSet<String>();
     private final Set<String> imported = new HashSet<String>();
 
     private final Map<String, ConstructorSymbol> constructors = new HashMap<String, ConstructorSymbol>();
 
     public final String name;
     public final String fullName;
     public final String pkg;
     private final boolean isFinal;
 
     private boolean isBuilt = false;
 
     public static final PkgClassResolver badResolve = new PkgClassResolver("!invalid");
 
     private static final Map<AInterfaceOrClassSymbol, PkgClassResolver> resolverMap =
             new HashMap<AInterfaceOrClassSymbol, PkgClassResolver>();
 
     private static String generateUniqueName(String name, Iterable<String> types){
         StringBuilder sb = new StringBuilder(name + "-");
 
         for (String type : types) sb.append(type + "*");
 
         return sb.toString();
     }
 
     //name in caes it's this and not the dcl name
     private static String generateUniqueName(MethodOrConstructorSymbol methodSymbol, String name) throws UndeclaredException{
         List<String> types = new LinkedList<String>();
         PkgClassResolver resolver = methodSymbol.resolver;
         for(DclSymbol param : methodSymbol.params){
             String type = resolver.getClass(param.type.value, true).fullName;
             types.add(type);
         }
         return generateUniqueName(name, types);
     }
 
     public static PkgClassResolver getResolver(AInterfaceOrClassSymbol start) throws UndeclaredException, DuplicateDeclarationException{
         PkgClassResolver resolver = resolverMap.get(start);
 
         if(resolver == null){
             resolver = new PkgClassResolver(start);
             resolverMap.put(start, resolver);
         }
 
         return resolver;
     }
 
     public static PkgClassResolver getPrimativeResolver(String name){
         return new PkgClassResolver(name);
     }
 
     private PkgClassResolver(String name) {
         this.start = null;
         isBuilt = true;
         fullName = this.name = name;
         pkg = null;
         isFinal = true;
         assignableTo.add(fullName);
     }
 
     private PkgClassResolver(AInterfaceOrClassSymbol start) throws UndeclaredException, DuplicateDeclarationException{
         namedMap.put(start.dclName, this);
         this.start = start;
         name = start.dclName;
         Iterator<NameSymbol> pkg = start.pkgImports.iterator();
         String mypkg = DEFAULT_PKG;
 
         if(pkg.hasNext()){
             NameSymbol first = pkg.next();
             if(first.type == NameSymbol.Type.PACKAGE){
                 mypkg = first.value;
             }
         }
 
         fullName = mypkg + "." + name;
         this.pkg = mypkg;
         isFinal = start.getImplementationLevel() == ImplementationLevel.FINAL;
         assignableTo.add(fullName);
     }
 
     private void addAll(String firstPart, Map<String, PkgClassResolver> entryMap) throws DuplicateDeclarationException{
         if(imported.contains(firstPart)) return;
         for(Entry<String, PkgClassResolver> entry : PkgClassInfo.instance.getNamespaceParts(firstPart)){
             String ename = entry.getKey();
             if(namedMap.containsKey(ename)) continue;
             //According to trying in java, this is fine as long as you don't go to use it, so don't let them use it.
             //if(entryMap.containsKey(ename)) throw new DuplicateDeclarationException(ename, start.dclName);
             if(entryMap.containsKey(ename)) entryMap.put(ename, badResolve);
             else entryMap.put(ename, entry.getValue());
         }
         imported.add(firstPart);
     }
 
     private DclSymbol getDcl(String name, boolean isStatic, PkgClassResolver pkgClass)
             throws UndeclaredException, ImplicitStaticConversionException{
 
         Map<String, DclSymbol> getFrom = isStatic ? sfieldMap : fieldMap;
 
         DclSymbol retVal = getFrom.get(name);
 
         //If it is not assignable to this and it's protected see if there is a hidden one.
         if(retVal.getProtectionLevel() == ProtectionLevel.PROTECTED && !pkgClass.assignableTo.contains(fullName)){
             getFrom = isStatic ? hsfieldMap : hfieldMap;
             retVal = getFrom.get(name);
         }
 
         return retVal;
     }
 
     public List<DclSymbol> findDcl(String name, boolean isStatic, PkgClassResolver pkgClass)
             throws UndeclaredException, ImplicitStaticConversionException {
 
         if(start == null) throw new UndeclaredException(name, "primatives");
 
         String [] nameParts = name.split("\\.");
 
         DclSymbol retVal;
         if(nameParts.length == 1){
             retVal = getDcl(name, isStatic, pkgClass);
             if(retVal == null) throw new UndeclaredException(name, start.dclName);
             return  Arrays.asList(new DclSymbol []{ retVal });
         }
 
         DclSymbol dcl = getDcl(nameParts[0], isStatic, pkgClass);
         List<DclSymbol> dclList = new LinkedList<DclSymbol>();
 
         PkgClassResolver pkgResolver = null;
 
         int i = 1;
 
         if(dcl != null){
             dclList.add(dcl);
             pkgResolver = getClass(dcl.type.value, true);
         }else{
             StringBuilder sb = new StringBuilder(nameParts[0]);
             pkgResolver = getClass(nameParts[0], false);
 
             //At least one must be a field
             for(; pkgResolver == null && i < nameParts.length - 2; i++){
                 sb.append("." + nameParts[i]);
                 pkgResolver = getClass(sb.toString(), false);
             }
         }
 
         if(pkgResolver == null) throw new UndeclaredException(name, start.dclName);
 
         for(; i < nameParts.length; i++){
             dcl = pkgResolver.getDcl(nameParts[i], isStatic, pkgClass);
             dclList.add(dcl);
             pkgResolver = getClass(dcl.type.value, true);
         }
 
         return dclList;
     }
 
     public AMethodSymbol findMethod(String name, boolean isStatic, Iterable<String> paramTypes, PkgClassResolver pkgClass) throws UndeclaredException{
         if(start == null) throw new UndeclaredException(name, "primatives");
         final Map<String, AMethodSymbol> getFrom = isStatic ? smethodMap : methodMap;
         AMethodSymbol retVal = getFrom.get(generateUniqueName(name, paramTypes));
 
         if(retVal == null) throw new UndeclaredException(name, start.dclName);
 
         if(retVal.getProtectionLevel() == ProtectionLevel.PROTECTED && !pkgClass.assignableTo.contains(fullName))
             throw new UndeclaredException(name, start.dclName);
 
         return retVal;
     }
 
     public PkgClassResolver getClass(String name, boolean die) throws UndeclaredException{
         PkgClassResolver retVal = null;
         if(namedMap.containsKey(name)) retVal = namedMap.get(name);
         else if(samepkgMap.containsKey(name)) retVal =  samepkgMap.get(name);
         else if(staredMap.containsKey(name)) retVal =  staredMap.get(name);
         else retVal = PkgClassInfo.instance.getSymbol(name);
 
         if((retVal == null || retVal == badResolve) && die) throw new UndeclaredException(name, start.dclName);
         return retVal;
     }
 
     public PkgClassResolver findClass(String name) throws UndeclaredException {
         String [] nameParts = name.split("\\.");
         StringBuilder sb = new StringBuilder();
 
         for(int i = 0; i < nameParts.length - 1; i++){
             sb.append(nameParts[i]);
             if(namedMap.containsKey(sb.toString())) throw new UndeclaredException(name, fullName);
             if(samepkgMap.containsKey(sb.toString())) throw new UndeclaredException(name, fullName);
             if(staredMap.containsKey(sb.toString())) throw new UndeclaredException(name, fullName);
 
             sb.append('.');
         }
 
         return getClass(name, true);
     }
 
     private void copyInfo(PkgClassResolver building, Set<PkgClassResolver> visited,
             List<Set<PkgClassResolver>> resolvedSets, boolean mustBeInterface, boolean mustBeClass) throws CompilerException{
 
         if(building.isFinal) throw new IllegalExtendsException(start.superName);
         Set<PkgClassResolver> cpySet = new HashSet<PkgClassResolver>(visited);
         building.build(cpySet, mustBeInterface, mustBeClass);
         resolvedSets.add(cpySet);
 
         //copy in reverse order so that when they are added to the start they are in order
         List<ISymbol> copyChildren = new LinkedList<ISymbol>(building.start.children);
         Collections.reverse(copyChildren);
 
         for(ISymbol child : copyChildren){
             if(child instanceof DclSymbol){
                 DclSymbol dcl = (DclSymbol) child;
                 DclSymbol field = fieldMap.get(dcl.dclName);
                 field = field == null ? sfieldMap.get(dcl.dclName) : field;
                 if(field == null){
                     Map<String, DclSymbol> addTo = dcl.isStatic() ? sfieldMap : fieldMap;
                     addTo.put(dcl.dclName, dcl);
                 }else if(field.getProtectionLevel() != ProtectionLevel.PUBLIC && dcl.getProtectionLevel() == ProtectionLevel.PUBLIC){
                     Map<String, DclSymbol> addTo = dcl.isStatic() ? hfieldMap : hsfieldMap;
                     addTo.put(dcl.dclName, dcl);
                 }
                 start.children.add(0, dcl);
             }else if(child instanceof AMethodSymbol){
                 AMethodSymbol methodSymbol = (AMethodSymbol) child;
                 String uniqueName = generateUniqueName(methodSymbol, methodSymbol.dclName);
                 AMethodSymbol has = methodMap.get(uniqueName);
                 has = has == null ? smethodMap.get(uniqueName) : has;
                 AMethodSymbol is = has == null ? methodSymbol : has;
 
                 //If it has it move it to the front so that it's in the correct place for the super's this
                 start.children.remove(is);
                 start.children.add(0, is);
                 if(has != null){
                     if(is.isStatic() != methodSymbol.isStatic())
                         throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is static and not static");
                     if(methodSymbol.getImplementationLevel() == ImplementationLevel.FINAL && has != null)
                         throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is final, but overrided");
                     if(methodSymbol.getProtectionLevel() == ProtectionLevel.PUBLIC && is.getProtectionLevel() != ProtectionLevel.PUBLIC)
                         throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is public, but overrided is not");
                     if(methodSymbol.getProtectionLevel() == ProtectionLevel.PROTECTED
                             && is.getProtectionLevel() != ProtectionLevel.PUBLIC && is.getProtectionLevel() != ProtectionLevel.PROTECTED)
                         throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is protected, but overrided is not protected or public");
                     //covarient return types not allowed in JOOS, it was added in java 5
                     if(methodSymbol.resolver.getClass(is.type.value, true) != building.getClass(methodSymbol.type.value, true))
                         throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "return types don't match");
                 }else{
                     if(methodSymbol.getImplementationLevel() == ImplementationLevel.ABSTRACT && start.getImplementationLevel() != ImplementationLevel.ABSTRACT)
                         throw new UnimplementedException(fullName, methodSymbol.dclName);
                     final Map<String, AMethodSymbol> addTo = methodSymbol.isStatic() ? smethodMap : methodMap;
                     addTo.put(uniqueName, methodSymbol);
                 }
             }
         }
     }
 
     public void verifyObject() throws CompilerException{
         PkgClassResolver obj = getClass(OBJECT, true);
         if(obj == this) return;
         obj.build();
         for(AMethodSymbol methodSymbol : obj.start.getMethods()){
             String uniqueName = generateUniqueName(methodSymbol, methodSymbol.dclName);
             AMethodSymbol has = methodMap.get(uniqueName);
             has = has == null ? smethodMap.get(uniqueName) : has;
             AMethodSymbol is = has == null ? methodSymbol : has;
             //If it has it move it to the front so that it's in the correct place for the super's this
             start.children.remove(is);
             if(has != null){
                 if(is.isStatic() != methodSymbol.isStatic())
                     throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is static and not static");
                 if(methodSymbol.getImplementationLevel() == ImplementationLevel.FINAL && has != null)
                     throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is final, but overrided");
                 if(methodSymbol.getProtectionLevel() == ProtectionLevel.PUBLIC && is.getProtectionLevel() != ProtectionLevel.PUBLIC)
                     throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is public, but overrided is not");
                 if(methodSymbol.getProtectionLevel() == ProtectionLevel.PROTECTED
                         && is.getProtectionLevel() != ProtectionLevel.PUBLIC && is.getProtectionLevel() != ProtectionLevel.PROTECTED)
                     throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "is protected, but overrided is not protected or public");
                 //covarient return types not allowed in JOOS, it was added in java 5
                 if(methodSymbol.resolver.getClass(is.type.value, true) != obj.getClass(methodSymbol.type.value, true))
                     throw new IllegalMethodOverloadException(fullName, methodSymbol.dclName, "return types don't match");
             }
         }
     }
 
     private void build(Set<PkgClassResolver> visited, boolean mustBeInterface, boolean mustBeClass) throws CompilerException{
 
         if(visited.contains(this)) throw new CircularDependancyException(start.dclName);
         if(mustBeInterface && start.isClass()) throw new UnsupportedException("Interface extending a class");
         if(mustBeClass && !start.isClass()) throw new UnsupportedException("Class extending interface");
 
         visited.add(this);
 
         if(!isBuilt){
             for(NameSymbol symbol : start.pkgImports){
                 NameSymbol name = symbol;
 
                 switch(name.type){
                 case IMPORT:
                     PkgClassResolver resolver = PkgClassInfo.instance.getSymbol(name.value);
                     if(resolver == null) throw new UndeclaredException(name.value, start.dclName);
 
                     String typeName = name.value.substring(name.value.lastIndexOf(".") + 1, name.value.length());
 
                     if(namedMap.containsKey(typeName) && namedMap.get(typeName) != resolver)
                         throw new DuplicateDeclarationException(name.value, start.dclName);
 
                     namedMap.put(name.value, resolver);
                     namedMap.put(typeName, resolver);
                     break;
                 case STAR_IMPORT:
                     if(name.value.equals(LANG)) continue;
                     addAll(name.value, staredMap);
                     break;
                 default:
                     break;
                 }
             }
 
             addAll(pkg, samepkgMap);
             addAll("java.lang", staredMap);
 
             for (AMethodSymbol methodSymbol : start.getMethods()){
                 methodSymbol.resolver = this;
                 String uniqueName = generateUniqueName(methodSymbol, methodSymbol.dclName);
                 if(methodMap.containsKey(uniqueName)) throw new DuplicateDeclarationException(uniqueName, start.dclName);
                 if(smethodMap.containsKey(uniqueName)) throw new DuplicateDeclarationException(uniqueName, start.dclName);
 
                 final Map<String, AMethodSymbol> addTo = methodSymbol.isStatic() ? smethodMap : methodMap;
                 addTo.put(uniqueName, methodSymbol);
                 findClass(methodSymbol.type.value);
             }
 
             for(DclSymbol fieldSymbol : start.getFields()){
                 if(fieldMap.containsKey(fieldSymbol.dclName) || sfieldMap.containsKey(fieldSymbol.dclName))
                     throw new UndeclaredException(fieldSymbol.dclName, start.dclName);
 
                 fieldSymbol.resolve = this;
                 final Map<String, DclSymbol> addTo = fieldSymbol.isStatic() ? sfieldMap : fieldMap;
                 addTo.put(fieldSymbol.dclName, fieldSymbol);
                 findClass(fieldSymbol.type.value);
             }
 
             for(ConstructorSymbol constructorSymbol : start.getConstructors()){
                 constructorSymbol.resolver = this;
                 String uniqueName = generateUniqueName(constructorSymbol, "this");
                 if(constructors.containsKey(uniqueName)) throw new DuplicateDeclarationException(uniqueName, start.dclName);
                 constructors.put(uniqueName, constructorSymbol);
             }
 
             mustBeInterface |= !start.isClass();
 
             PkgClassResolver building = null;
 
             List<Set<PkgClassResolver>> resolvedSets = new LinkedList<Set<PkgClassResolver>>();
 
             if(start.superName != null){
                 building = findClass(start.superName);
                 copyInfo(building, visited, resolvedSets, false, true);
                 assignableTo.addAll(building.assignableTo);
             }else{
                 verifyObject();
             }
 
             Set<String> alreadyImps = new HashSet<String>();
 
             for(String impl : start.impls){
                 building = findClass(impl);
                 if(alreadyImps.contains(building.fullName)) throw new DuplicateDeclarationException(impl, fullName);
 
                 //Interfaces must be implemented, unless this is abstract
                 if(start.getImplementationLevel() == ImplementationLevel.ABSTRACT){
                     copyInfo(building, visited, resolvedSets, true, false);
                 }
                 else{
                     Set<PkgClassResolver> cpySet = new HashSet<PkgClassResolver>(visited);
                     building.build(cpySet, true, false);
                     resolvedSets.add(cpySet);
                     //we only have methods in interfaces in JOOS
                     for(AMethodSymbol methodSymbol : building.start.getMethods()){
                         String uniqueName = generateUniqueName(methodSymbol, methodSymbol.dclName);
                         //No method can be static in an interface.
                         if(!methodMap.containsKey(uniqueName)) throw new UndeclaredException(uniqueName, fullName);
                         AMethodSymbol hasMethod = methodMap.get(uniqueName);
                         PkgClassResolver hasResolver = hasMethod.resolver;
                         if(hasResolver.findClass(hasMethod.type.value) != methodSymbol.resolver.findClass(methodSymbol.type.value))
                                 throw new UndeclaredException(uniqueName, fullName);
                     }
                 }
 
                 assignableTo.addAll(building.assignableTo);
                 alreadyImps.add(building.fullName);
             }
 
             for(Set<PkgClassResolver> pkgSet : resolvedSets) visited.addAll(pkgSet);
 
             for(PkgClassResolver resolver : visited) assignableTo.add(resolver.fullName);
             //Java specific
             assignableTo.add(OBJECT);
 
             start.accept(new TypeResolverVisitor(this));
             linkLocalNamesToDcl();
             isBuilt = true;
         }else{
             for(String s : assignableTo) visited.add(PkgClassInfo.instance.getSymbol(s));
         }
     }
 
     private void linkLocalNamesToDcl() throws CompilerException {
         for (ISymbol child : start.children) {
             if (child instanceof MethodOrConstructorSymbol){
                 ((MethodOrConstructorSymbol) child).resolveLocalVars(fullName);
             }
         }
     }
 
     public void build() throws CompilerException{
         build(new HashSet<PkgClassResolver>(), false, false);
     }
 
     public PkgClassResolver getSuper() throws UndeclaredException{
         return findClass(start.superName);
     }
 
     public ConstructorSymbol getConstructor(List<String> types) throws UndeclaredException{
         String name = generateUniqueName("this", types);
         ConstructorSymbol cs = constructors.get(name);
         if(cs == null) throw new UndeclaredException(name, fullName);
         return cs;
     }
 }
