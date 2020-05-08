 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.profiler.utilities;
 
 import com.sun.tools.javac.code.Kinds;
 import java.io.IOException;
 import java.util.LinkedList;
 import org.netbeans.api.javafx.source.ClasspathInfo;
 import org.netbeans.api.javafx.source.JavaFXSource;
 import org.netbeans.api.javafx.source.JavaFXSource.Phase;
 import org.netbeans.api.project.ProjectUtils;
 import org.netbeans.api.project.SourceGroup;
 import org.netbeans.api.project.Sources;
 import org.netbeans.lib.profiler.common.filters.SimpleFilter;
 import org.netbeans.modules.profiler.projectsupport.utilities.SourceUtils;
 import org.netbeans.spi.project.SubprojectProvider;
 import org.netbeans.modules.javafx.project.JavaFXProject;
 import org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities;
 import org.openide.filesystems.FileUtil;
 import org.netbeans.api.javafx.source.CompilationController;
 import org.netbeans.modules.javafx.project.classpath.ClassPathProviderImpl;
 import org.netbeans.modules.javafx.source.classpath.FileObjects;
 import com.sun.tools.javac.code.Symbol;
 import com.sun.tools.javac.code.TypeTags;
 import com.sun.tools.javac.util.Convert;
 import com.sun.tools.javac.util.Name;
 import org.netbeans.api.javafx.source.CompilationInfo;
 import org.netbeans.api.java.classpath.ClassPath;
 import org.netbeans.api.project.Project;
 import org.netbeans.lib.profiler.ProfilerLogger;
 import org.netbeans.lib.profiler.client.ClientUtils;
 import org.netbeans.lib.profiler.utils.VMUtils;
 import org.openide.filesystems.FileObject;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ExecutableElement;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.element.VariableElement;
 import javax.lang.model.type.DeclaredType;
 import javax.lang.model.type.TypeKind;
 import javax.lang.model.type.TypeMirror;
 import org.netbeans.api.javafx.source.CancellableTask;
 import com.sun.javafx.api.tree.JavaFXTreePath;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.source.tree.ClassTree;
import com.sun.tools.javafx.api.JavafxcTrees;
 import javax.lang.model.element.ElementKind;
import org.netbeans.api.javafx.source.TreeUtilities;
 import org.netbeans.modules.profiler.utils.OutputParameter;
 
 /* 
  * @author cms
  */
 public class JavaFXProjectUtilities extends ProjectUtilities {
     
     public static final String SOURCES_TYPE_JAVAFX = "fx";         // NOI18N
     public static final String SOURCES_TYPE_JAVA   = "java";       // NOI18N
     public static final String JAVAFX_MIME_TYPE    = "text/x-fx";  // NOI18N
     public static final String MAGIC_METHOD_NAME    = "javafx$run$";  // NOI18N
     public static final String MAGIC_METHOD_SIGNATURE    = "(Lcom/sun/javafx/runtime/sequence/Sequence;)Ljava/lang/Object;";  // NOI18N
     public static final String INTERFACE_NAME_SUFFIX = "$Intf";  // NOI18N
     public static final String JAVAFX_PREFIX = "javafx.";  // NOI18N
 
     public static String getEnclosingClassName(FileObject profiledClassFile, final int position) {
         final OutputParameter<String> result = new OutputParameter<String>(null);
 
         if (isJavaFXFile(profiledClassFile)) {
             JavaFXSource js = JavaFXSource.forFileObject(profiledClassFile);
 
             if (js == null) {
                 return null; // not java source
             }
 
             try {
                 js.runUserActionTask(new CancellableTask<CompilationController>() {
                         public void cancel() {
                         }
 
                         public void run(final CompilationController controller)
                                  throws Exception {
                             if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                                return;
                             }
                             TypeElement parentClass = controller.getTreeUtilities().scopeFor(position).getEnclosingClass();
 
                             if (parentClass != null) {
                                 // no enclosing class found (i.e. cursor at import)
                                 result.setValue(getBinaryName(parentClass, parentClass.getEnclosingElement()));
                             }
                         }
                     }, true);
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
 
         return result.getValue();
     }
 
     public static ClientUtils.SourceCodeSelection[] getProjectDefaultRoots(Project project, String[][] projectPackagesDescr) {
         computeProjectPackages(project, true, projectPackagesDescr);
 
         ClientUtils.SourceCodeSelection[] ret = new ClientUtils.SourceCodeSelection[projectPackagesDescr[1].length];
 
         for (int i = 0; i < projectPackagesDescr[1].length; i++) {
             if ("".equals(projectPackagesDescr[1][i])) { //NOI18N
                 ret[i] = new ClientUtils.SourceCodeSelection("", "", ""); //NOI18N
             } else {
                 ret[i] = new ClientUtils.SourceCodeSelection(projectPackagesDescr[1][i] + ".", "", ""); //NOI18N
             }
         }
 
         return ret;
     }
 
     public static void computeProjectPackages(final Project project, boolean subprojects, String[][] storage) {
         if ((storage == null) || (storage.length != 2)) {
             throw new IllegalArgumentException("Storage must be a non-null String[2][] array"); // NOI18N
         }
 
         if (storage[0] == null) {
             Collection<String> packages1 = new ArrayList<String>();
 
             for (FileObject root : getSourceRoots(project, false)) {
                 addSubpackages(packages1, "", root); //NOI18N
             }
 
             storage[0] = packages1.toArray(new String[0]);
         }
 
         if (subprojects && (storage[1] == null)) {
             FileObject[] srcRoots2 = getSourceRoots(project, true); // TODO: should be computed based on already known srcRoots1
             ArrayList<String> packages2 = new ArrayList<String>();
 
             for (FileObject root : srcRoots2) {
                 addSubpackages(packages2, "", root); //NOI18N
             }
 
             storage[1] = packages2.toArray(new String[0]);
         }
     }
 
     public static String cutIntfSuffix(String signature) {
         if (signature == null)
             return null;
 
         return signature.indexOf(INTERFACE_NAME_SUFFIX) != -1 ?
                 signature.substring(0, signature.indexOf(INTERFACE_NAME_SUFFIX)).
         concat(signature.substring(signature.indexOf(INTERFACE_NAME_SUFFIX) +
         INTERFACE_NAME_SUFFIX.length(), signature.length())) : signature;
     }
 
     public static String getToplevelClassName(FileObject profiledClassFile) {
         final String[] result = new String[1];
 
         if (isJavaFXFile(profiledClassFile)) {
             JavaFXSource js = JavaFXSource.forFileObject(profiledClassFile);
 
             if (js == null) {
                 return null; // not java source
             }
 
             try {
                 js.runUserActionTask(new CancellableTask<CompilationController>() {
                         public void cancel() {
                         }
 
                         public void run(final CompilationController controller)
                                  throws Exception {
                             // Controller has to be in some advanced phase, otherwise controller.getCompilationUnit() == null
                             if (controller.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                                return;
                             }
 
                             JavaFXTreePathScanner<String, Void> scanner = new JavaFXTreePathScanner<String, Void>() {
                                 public String visitClassDeclaration(ClassTree node, Void p) {
                                     try {
                                         TypeElement classElement = (TypeElement) controller.getTrees().getElement(getCurrentPath());
                                         return getBinaryName(classElement, classElement.getEnclosingElement());
                                     } catch (NullPointerException e) {
                                         ProfilerLogger.log(e);
                                         return "";
                                     }
                                 }
                             };
 
                             result[0] = scanner.scan(controller.getCompilationUnit(), null);
                         }
                     }, true);
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
 
         return result[0];
     }
 
     private static void addSubpackages(Collection<String> packages, String prefix, FileObject packageFO) {
         if (!packageFO.isFolder()) { // not a folder
             return;
         }
 
         FileObject[] children = packageFO.getChildren();
 
         // 1. check if there are java sources in this folder and if so, add to the list of packages
         if (!packages.contains(prefix)) { // already in there, skip this
             for (int i = 0; i < children.length; i++) {
                 FileObject child = children[i];
 
                 if (child.getExt().equals(SOURCES_TYPE_JAVAFX) || //NOI18N
                     child.getExt().equals(SOURCES_TYPE_JAVA)) {   //NOI18N
                     packages.add(prefix);
                     
                     break;
                 }
             }
         }
 
         // 2. recurse into subfolders
         for (int i = 0; i < children.length; i++) {
             FileObject child = children[i];
 
             if (child.isFolder()) {
                 if ("".equals(prefix)) { //NOI18N
                     addSubpackages(packages, child.getName(), child);
                 } else {
                     addSubpackages(packages, prefix + "." + child.getName(), child); //NOI18N
                 }
             }
         }
     }
 
     public static FileObject[] getSourceRoots(final Project project, final boolean traverse) {
         Set<FileObject> set = new HashSet<FileObject>();
         Set<Project> projects = new HashSet<Project>();
 
         projects.add(project);
         getSourceRoots(project, traverse, projects, set);
 
         return set.toArray(new FileObject[set.size()]);
     }
 
     private static void getSourceRoots(final Project project, final boolean traverse, Set<Project> projects, Set<FileObject> roots) {
         final Sources sources = ProjectUtils.getSources(project);
 
         for (SourceGroup sg : sources.getSourceGroups(SOURCES_TYPE_JAVA)) {
             roots.add(sg.getRootFolder());
         }
         
         if (traverse) {
             // process possible subprojects recursively
             SubprojectProvider spp = project.getLookup().lookup(SubprojectProvider.class);
 
             if (spp != null) {
                 for (Project p : spp.getSubprojects()) {
                     if (projects.add(p)) {
                         getSourceRoots(p, traverse, projects, roots);
                     }
                 }
             }
         }
     }
     
     public static String getToplevelClassName(final Project project, FileObject profiledClassFile) {
         if (SourceUtils.isJavaFile(profiledClassFile)) {
             return SourceUtils.getToplevelClassName(profiledClassFile);
         } else if (isJavaFXFile(profiledClassFile)) {
             JavaFXProject projectJFX = (JavaFXProject)project;
             String clazz = FileUtil.getRelativePath(getRoot(projectJFX.getFOSourceRoots(),profiledClassFile), profiledClassFile);
             return (clazz.substring(0, clazz.length() - 3)).replace('/','.');           
         }
         return ""; //NOI18N // won't be here: other file types are not supported
     }   
     
     public static boolean isJavaFXFile(FileObject f) {
         return JAVAFX_MIME_TYPE.equals(f.getMIMEType()); //NOI18N
     }        
     
     public static FileObject getRoot(FileObject[] roots, FileObject file) {
         FileObject srcDir = null;
         for (int i=0; i< roots.length; i++) {
             if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                 srcDir = roots[i];
                 break;
             }
         }
         return srcDir;
     }    
     
     public static SimpleFilter computeProjectOnlyInstrumentationFilter(Project project, SimpleFilter predefinedInstrFilter,
             String[][] projectPackagesDescr) {
         // TODO: projectPackagesDescr[1] should only contain packages from subprojects, currently contains also toplevel project packages
         if (FILTER_PROJECT_ONLY.equals(predefinedInstrFilter)) {
             computeProjectPackages(project, false, projectPackagesDescr);
 
             StringBuffer projectPackages = new StringBuffer();
 
             for (int i = 0; i < projectPackagesDescr[0].length; i++) {
                 projectPackages.append("".equals(projectPackagesDescr[0][i]) ? getDefaultPackageClassNames(project)
                         : (projectPackagesDescr[0][i] + ". ")); //NOI18N
             }
             
             // TBD: correct filter name!!!
             return new SimpleFilter("", SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                     projectPackages.toString().trim());
         } else if (FILTER_PROJECT_SUBPROJECTS_ONLY.equals(predefinedInstrFilter)) {
             computeProjectPackages(project, true, projectPackagesDescr);
 
             StringBuffer projectPackages = new StringBuffer();
 
             for (int i = 0; i < projectPackagesDescr[1].length; i++) {
                 projectPackages.append("".equals(projectPackagesDescr[1][i]) ? getDefaultPackageClassNames(project)
                         : (projectPackagesDescr[1][i] + ". ")); //NOI18N // TODO: default packages need to be processed also for subprojects!!!
             }
             // TBD: correct filter name!!!
             return new SimpleFilter("", SimpleFilter.SIMPLE_FILTER_INCLUSIVE,
                     projectPackages.toString().trim());
         }
 
         return null;
     }    
     
     
     private static ClassPathProviderImpl getCPProvider(JavaFXProject project) {
         return project.getClassPathProvider();
     }
     
     public static ClasspathInfo createClassPathInfo(JavaFXProject project) {
         ClassPath srcPath = null;
         ClassPath bootPath = null;
         ClassPath compilePath = null;
         
         FileObject[] roots = project.getFOSourceRoots();
 
         if (roots == null) {
             ClassPathProviderImpl cpProvider = getCPProvider(project);
             if (cpProvider != null) {
                 bootPath = cpProvider.getProjectSourcesClassPath(ClassPath.BOOT);
                 compilePath = cpProvider.getProjectSourcesClassPath(ClassPath.EXECUTE);
                 srcPath = cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE);   //Empty ClassPath
             }
         } else {
             bootPath = ClassPath.getClassPath (roots[0], ClassPath.BOOT);        //Single compilation unit
             compilePath = ClassPath.getClassPath (roots[0], ClassPath.EXECUTE);
             srcPath = ClassPath.getClassPath(roots[0], ClassPath.SOURCE);            
         }
 
         // create ClassPathInfo for JavaSources only -> (bootPath, classPath, sourcePath)
         return ClasspathInfo.create(bootPath, compilePath, srcPath);
     }        
     
     
     public static JavaFXSource getSources(JavaFXProject project) {
         final ClasspathInfo cpInfo = createClassPathInfo(project);
         return JavaFXSource.create(cpInfo, getSourceFiles(project));
     }
     
     /**
      * Resolves a class by its name
      * @param className The name of the class to be resolved
      * @param c ontroller The compilation controller to be used to resolve the class
      * @return Returns a TypeElement representing the resolved class or NULL
      */
     public static TypeElement resolveClassByName(String className, final CompilationController controller) {
         if ((className == null) || (controller == null)) {
             return null;
         }
         
         if (className.indexOf('$') != -1) {
             try {
                 new Integer(className.substring(className.indexOf('$') + 1, className.indexOf('$') + 2));
                 className = className.substring(0, className.indexOf('$')); // digit. Assume hidden anonymous inner class                
             } catch (NumberFormatException e) {
             }
         }
     
         TypeElement mainClass = controller.getElements().getTypeElement(className.replace('$', '.')); // NOI18N
 
         if (mainClass != null) {
             ProfilerLogger.debug("Resolved: " + mainClass); // NOI18N
         } else {
             ProfilerLogger.debug("Could not resolve: " + className); // NOI18N
         }
         return mainClass;
     }
 
     public static List<FileObject> getSourceFiles(JavaFXProject project) {
         FileObject[] roots = project.getFOSourceRoots();
 
         List<FileObject> result = new ArrayList<FileObject>();
         // call recursive method
         return getSourceFiles(roots, result);
     }    
     
     private static List<FileObject> getSourceFiles(FileObject[] roots, List<FileObject> result) {        
         for(FileObject fo: roots) {
             FileObject[] children = fo.getChildren();
             for(FileObject child : children) {
                 if(child.isFolder()) {
                     // call recursively
                     getSourceFiles(new FileObject[] {child}, result);
                 }
                 if (child.getMIMEType().equals(JAVAFX_MIME_TYPE)) {
                     result.add(child);
                 }
             }
         }
         return result;
     }
     
     public static FileObject getFile(Element handle, ClasspathInfo cpInfo) {
         assert handle != null;
         assert handle instanceof TypeElement;
         TypeElement te = (TypeElement) handle;
         StringBuilder sb = new StringBuilder ();
         Name name = ((Symbol.ClassSymbol)te).flatname;
         assert name != null;
         int nameLength = name.len;
         char[] nameChars = new char[512]; //Initial storage
         
         if (nameChars.length < nameLength) {
             nameChars = new char[nameLength];
         }
 
         int charLength = Convert.utf2chars(name.table.names, name.index, nameChars, 0, nameLength);
         sb.append(nameChars,0,charLength);
             
         String[] signature = new String[] { sb.toString() };
 
         assert signature.length >= 1;
         String pkgName, className = null;
         int index = signature[0].lastIndexOf('.');                          //NOI18N
         pkgName = FileObjects.convertPackage2Folder(signature[0].substring(0,index));
         className = signature[0].substring(index+1);
 
         ClassPath bCP = cpInfo.getClassPath(ClasspathInfo.PathKind.BOOT);
         ClassPath cCP = cpInfo.getClassPath(ClasspathInfo.PathKind.COMPILE);
         ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);            
 
         List<FileObject> fos = bCP.findAllResources(pkgName);
         fos.addAll(cCP.findAllResources(pkgName));
         fos.addAll(sourcePath.findAllResources(pkgName));
 
         for (FileObject fo : fos) {
             LinkedList<FileObject> folders = new LinkedList<FileObject>(sourcePath.findAllResources(pkgName));
             // TBD make sure if this is case sensitive really
             boolean caseSensitive = true;
             int ind = className.indexOf('$'); //NOI18N
             String sourceFileName = ind == -1 ? className : className.substring(0, ind);
             folders.addFirst(fo);
             for (FileObject folder : folders) {
                 FileObject[] children = folder.getChildren();
                 for (FileObject child : children) {
                     if (((caseSensitive && child.getName().equals (sourceFileName)) ||
                         (!caseSensitive && child.getName().equalsIgnoreCase (sourceFileName))) &&
                         (child.isData() && isJavaFXFile(child))) {
                         return child;
                     }
                 }
             }
         }
         return null;
     }
 
     public static String getBinaryName(Element element, Element parent) {
         Symbol owner = (Symbol)parent;
         Name name = ((Symbol)element).getQualifiedName();
 
         if (owner == null || (owner.kind & (Kinds.VAR | Kinds.MTH)) != 0 || 
                 (owner.kind == Kinds.TYP && owner.type.tag == TypeTags.TYPEVAR)) 
             return name.toString();
 
         char sep = owner.kind == Kinds.TYP ? '$' : '.';
         name = ((Symbol)element).getSimpleName();
         
         Name prefix = owner.flatName();
 
         if (prefix == null || prefix == prefix.table.empty) return name.toString(); 
         else  return prefix.append(sep, name).toString();
     }
     
     public static String getVMMethodSignature(ExecutableElement method, CompilationInfo ci) {
         try {
             switch (method.getKind()) {
                 case METHOD:
                 case CONSTRUCTOR:
                 case STATIC_INIT:
                     String paramsVMSignature = getParamsSignature(method.getParameters(), ci);
                     String retTypeVMSignature = VMUtils.typeToVMSignature(getRealTypeName(method.getReturnType(), ci));
                     return "(" + paramsVMSignature + ")" + retTypeVMSignature; //NOI18N
                 default:
                     return null;
             }
         } catch (IllegalArgumentException e) {
             // LOGGER.warning(e.getMessage());
         }
         return null;
     }
     
     private static String getParamsSignature(List<?extends VariableElement> params, CompilationInfo ci) {
         StringBuffer ret = new StringBuffer();
         Iterator<?extends VariableElement> it = params.iterator();
 
         while (it.hasNext()) {
             TypeMirror type = it.next().asType();
             String realTypeName = getRealTypeName(type, ci);
             String typeVMSignature = VMUtils.typeToVMSignature(realTypeName);
             ret.append(typeVMSignature);
         }
 
         return ret.toString();
     }
         
     private static String getRealTypeName(TypeMirror type, CompilationInfo ci) {
         TypeKind typeKind = type.getKind();
 
         if (typeKind.isPrimitive()) {
             return type.toString(); // primitive type, return its name
         }
 
         switch (typeKind) {
             case VOID:
 
                 // VOID type, return "void" - will be converted later by VMUtils.typeToVMSignature
                 return type.toString();
             case DECLARED:
                 // Java class (also parametrized - "ArrayList<String>" or "ArrayList<T>"), need to generate correct innerclass signature using "$"
                 if (getDeclaredType(type) instanceof Symbol.TypeSymbol) {
                     String flatName = ((Symbol.TypeSymbol)getDeclaredType(type)).flatName().toString();                    
                     return flatName.startsWith(JAVAFX_PREFIX) ? flatName.concat(INTERFACE_NAME_SUFFIX) : flatName;
                 }
             case TYPEVAR:
                 // TYPEVAR means "T" or "<T extends String>" or "<T extends List&Runnable>"
                 List<?extends TypeMirror> subTypes = ci.getTypes().directSupertypes(type);
 
                 if (subTypes.size() == 0) {
                     return "java.lang.Object"; // NOI18N // Shouldn't happen
                 }
 
                 if ((subTypes.size() > 1) && subTypes.get(0).toString().equals("java.lang.Object")
                         && getDeclaredType(subTypes.get(1)).getKind().isInterface()) {
                     // NOI18N
                     // Master type is interface
                     return getRealTypeName(subTypes.get(1), ci);
                 } else {
                     // Master type is class
                     return getRealTypeName(subTypes.get(0), ci);
                 }
             case WILDCARD:
                 // WILDCARD means "<?>" or "<? extends Number>" or "<? super T>", shouldn't occur here
                 throw new IllegalArgumentException("Unexpected WILDCARD parameter: " + type); // NOI18N
             default:
                 // Unexpected parameter type
                 throw new IllegalArgumentException("Unexpected type parameter: " + type + " of kind " + typeKind); // NOI18N
         }
     }    
     
     private static TypeElement getDeclaredType(TypeMirror type) {
         return (TypeElement) ((DeclaredType)type).asElement();
     }
     
     public static ResolvedMethod resolveMethodAtPosition(final FileObject fo, final int position) {
         // Get JavaSource for given FileObject
         JavaFXSource js = JavaFXSource.forFileObject(fo);
 
         if (js == null) {
             return null; // not java source
         }
 
         // Final holder of resolved method
         final OutputParameter<ResolvedMethod> resolvedMethod = new OutputParameter(null);
 
         // Resolve the method
         try {
             js.runUserActionTask(new CancellableTask<CompilationController>() {
 
                 public void cancel() {
                 }
 
                 public void run(CompilationController ci) throws Exception {
                     if (ci.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                        return;
                     }
                     
                     JavaFXTreePath path = ci.getTreeUtilities().pathFor(position);
                     if (path == null) {
                         return;
                     }
                     
                     Element element = ci.getTrees().getElement(path);
                     if ((element != null) && ((element.getKind() == ElementKind.METHOD) || (element.getKind() == ElementKind.CONSTRUCTOR) || (element.getKind() == ElementKind.STATIC_INIT))) {
                         ExecutableElement method = (ExecutableElement) element;
                         TypeElement classElement = (TypeElement)method.getEnclosingElement();
 
                         String vmClassName = getBinaryName(classElement, classElement.getEnclosingElement());
                         String vmMethodName = method.getSimpleName().toString();
                         String vmMethodSignature = getVMMethodSignature(method, ci);
                         resolvedMethod.setValue(new ResolvedMethod(method, vmClassName, vmMethodName, vmMethodSignature));
                     }
                 }
             }, true);
         } catch (IOException ioex) {
             ProfilerLogger.log(ioex);
             ioex.printStackTrace();
 
             return null;
         }
 
         return resolvedMethod.getValue();
     }
     
     public static ResolvedClass resolveClassAtPosition(final FileObject fo, final int position, final boolean resolveField) {
         // Get JavaFXSource for given FileObject
         JavaFXSource js = JavaFXSource.forFileObject(fo);
 
         if (js == null) {
             return null; // not javafx source
         }
 
         // Final holder of resolved method
         final OutputParameter<ResolvedClass> resolvedClass = new OutputParameter(null);
 
         // Resolve the method
         try {
             js.runUserActionTask(new CancellableTask<CompilationController>() {
                     public void cancel() {
                     }
 
                     public void run(CompilationController ci)
                              throws Exception {
                         if (ci.toPhase(Phase.ANALYZED).lessThan(Phase.ANALYZED)) {
                            return;
                         }
                         JavaFXTreePath path = ci.getTreeUtilities().pathFor(position);
 
                         if (path == null) {
                             return;
                         }
                         Element element = ci.getTrees().getElement(path);
 
                         if (element == null) {
                             return;
                         }
 
                         // resolve class/enum at cursor
                         if ((element.getKind() == ElementKind.CLASS) || (element.getKind() == ElementKind.ENUM)) {
                             TypeElement jclass = (TypeElement) element;
                             String vmClassName = getBinaryName(jclass, jclass.getEnclosingElement());
                             resolvedClass.setValue(new ResolvedClass(jclass, vmClassName));
 
                             return;
                         }
 
                         // resolve field at cursor
                         if (resolveField
                                 && ((element.getKind() == ElementKind.FIELD) || (element.getKind() == ElementKind.LOCAL_VARIABLE))
                                 && (element.asType().getKind() == TypeKind.DECLARED)) {
                             TypeElement jclass = getDeclaredType(element.asType());
                             String vmClassName = getBinaryName(jclass, jclass.getEnclosingElement());
                             resolvedClass.setValue(new ResolvedClass(jclass, vmClassName));
 
                             return;
                         }

                        // function
                        if (element.getKind() == ElementKind.METHOD) {
                            TypeElement jclass = (TypeElement) element.getEnclosingElement();
                            String vmClassName = getBinaryName(jclass, jclass.getEnclosingElement());
                            resolvedClass.setValue(new ResolvedClass(jclass, vmClassName));

                            return;
                        }


                        // package
                        if (element.getKind() == ElementKind.PACKAGE) {
                            Iterator<? extends Element> elements = element.getEnclosedElements().iterator();
                            Element elmt;
                            while (elements.hasNext()) {
                                if ((elmt = elements.next()).getKind() == ElementKind.CLASS) {
                                    TypeElement jclass = (TypeElement) elmt;
                                    String vmClassName = getBinaryName(jclass, jclass.getEnclosingElement());
                                    resolvedClass.setValue(new ResolvedClass(jclass, vmClassName));
                                }
                            }

                            return;
                        }
                     }
                 }, true);
         } catch (IOException ioex) {
             ProfilerLogger.log(ioex);
             ioex.printStackTrace();
 
             return null;
         }
 
         return resolvedClass.getValue();
     }
         
     public static class ResolvedClass {
         //~ Instance fields ------------------------------------------------------------------------------------------------------
 
         private String vmClassName;
         private TypeElement jclass;
 
         //~ Constructors ---------------------------------------------------------------------------------------------------------
 
         ResolvedClass(TypeElement jclass, String className) {
             this.jclass = jclass;
             this.vmClassName = className;
         }
 
         //~ Methods --------------------------------------------------------------------------------------------------------------
 
         public TypeElement getJClass() {
             return jclass;
         }
 
         public String getVMClassName() {
             return vmClassName;
         }
     }
 
     public static class ResolvedMethod {
         //~ Instance fields ------------------------------------------------------------------------------------------------------
 
         private ExecutableElement method;
         private String vmClassName;
         private String vmMethodName;
         private String vmMethodSignature;
 
         //~ Constructors ---------------------------------------------------------------------------------------------------------
 
         ResolvedMethod(ExecutableElement method, String className, String methodName, String methodSignature) {
             this.method = method;
             this.vmClassName = className;
             this.vmMethodName = methodName;
             this.vmMethodSignature = methodSignature;
         }
 
         //~ Methods --------------------------------------------------------------------------------------------------------------
 
         public ExecutableElement getMethod() {
             return method;
         }
 
         public String getVMClassName() {
             return vmClassName;
         }
 
         public String getVMMethodName() {
             return vmMethodName;
         }
 
         public String getVMMethodSignature() {
             return vmMethodSignature;
         }
     }
 }
