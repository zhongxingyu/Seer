 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.common.tools.ltk;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.atos.optimus.common.tools.Activator;
 
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.IPackageBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.MarkerAnnotation;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.Name;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
 import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 
 /**
  * Imports Generation Visitor tool. The role of this tool is to parse all the
  * names located in a class, in order to extract imports and trim package names
  * from fully qualified names
  * 
  * @author Maxence Vanbésien (mvaawl@gmail.com)
  * @since 1.0
  * 
  */
 public class ImportsGenerationVisitor extends ASTVisitor {
 
 	/**
 	 * True if the source has been modified, false otherwise
 	 */
 	private boolean hasModifications = false;
 
 	/**
 	 * Contains all the resolved imports to add to the source code
 	 */
 	private Map<String, ImportDeclaration> resolvedImports = new LinkedHashMap<String, ImportDeclaration>();
 
 	/**
 	 * Contains the Imports to be associated with Simple Named Type.
 	 * 
 	 * e.g. List -> java.util.List or Generated -> javax.annotation.Generated.
 	 * 
 	 * Used to avoid conflicts if two classes with same simple name and
 	 * different packages are used.
 	 * 
 	 */
 	private Map<String, String> typesToPackageBinding = new LinkedHashMap<String, String>();
 
 	/**
 	 * Name of the Package containing the currently processed element
 	 */
 	private String currentPackageName;
 
 	/**
 	 * Name of the currently processed element
 	 */
 	private String currentTypeName;
 
 	/*
 	 * Private Constructor
 	 */
 	private ImportsGenerationVisitor() {
 		super(true);
 	}
 
 	/**
 	 * Applies the Import Generation Visitor on the Compilation Unit passed as
 	 * parameter. Returns whether modifications were done, to tell the caller to
 	 * persist the modifications.
 	 * 
 	 * @param unit
 	 *            : Compilation Unit
 	 * @return true if need to write modifications, false if nothing has
 	 *         changed.
 	 */
 	public static boolean apply(CompilationUnit unit) {
 		try {
 			ImportsGenerationVisitor visitor = new ImportsGenerationVisitor();
 			unit.accept(visitor);
 			visitor.addMissingImports(unit);
 			return visitor.hasModifications;
		} catch (Throwable t) {
			Activator.getDefault().logError("Exception encountered while generating imports", t);
 		}
 		return false;
 	}
 
 	/**
 	 * Adds all the missing imports detected by this visitor, to the compilation
 	 * unit passed as parameter.
 	 * 
 	 * @param unit
 	 *            : CompilationUnit
 	 */
 	@SuppressWarnings("unchecked")
 	private void addMissingImports(CompilationUnit unit) {
 		Collection<ImportDeclaration> values = resolvedImports.values();
 		for (ImportDeclaration value : values) {
 			String fullyQualifiedName = value.getName().getFullyQualifiedName();
 			boolean found = false;
 			Iterator<?> iterator = unit.imports().iterator();
 			while (iterator.hasNext() && !found) {
 				Object next = iterator.next();
 				if (next instanceof ImportDeclaration) {
 					String existingImport = ((ImportDeclaration) next).getName()
 							.getFullyQualifiedName();
 					if (fullyQualifiedName.equals(existingImport))
 						found = true;
 				}
 			}
 			if (!found)
 				unit.imports().add(value);
 		}
 	}
 
 	@Override
 	public boolean visit(CompilationUnit node) {
 		this.currentPackageName = node.getPackage().getName().getFullyQualifiedName();
 		
 		// Prefills the list with the imports that already exist in the list. (XADII-320)
 		for (Object o : node.imports()) {
 			ImportDeclaration declaration = (ImportDeclaration) o;
 			IBinding resolvedBinding = declaration.resolveBinding();
 			if (resolvedBinding instanceof ITypeBinding) {
 				ITypeBinding resolvedTypeBinding = (ITypeBinding) resolvedBinding;
 				if (resolvedTypeBinding != null && !resolvedTypeBinding.isRecovered()) {
 					resolvedTypeBinding = resolvedTypeBinding.getErasure();
 					String typeName = resolvedTypeBinding.getName();
 					IPackageBinding packageBinding = resolvedTypeBinding.getPackage();
 					if (packageBinding != null) {
 						String packageName = packageBinding.getName();
 						this.typesToPackageBinding.put(typeName, packageName);
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public boolean visit(TypeDeclaration node) {
 		if (this.currentTypeName == null)
 			this.currentTypeName = node.getName().getFullyQualifiedName();
 
 		if (this.currentTypeName != null && this.currentPackageName != null)
 			this.typesToPackageBinding.put(this.currentTypeName, this.currentPackageName);
 
 		return true;
 	}
 
 	@Override
 	public boolean visit(EnumDeclaration node) {
 		if (this.currentTypeName == null)
 			this.currentTypeName = node.getName().getFullyQualifiedName();
 
 		if (this.currentTypeName != null && this.currentPackageName != null)
 			this.typesToPackageBinding.put(this.currentTypeName, this.currentPackageName);
 		return true;
 	}
 
 	/**
 	 * Checks Import Generation for Marker Annotation
 	 */
 	@Override
 	public boolean visit(MarkerAnnotation node) {
 		if (node == null)
 			return super.visit(node);
 		
 		ITypeBinding typeBinding = node.resolveTypeBinding();
 		if (typeBinding == null)
 			return super.visit(node);
 		
 		Name typeName = node.getTypeName();
 		this.checkType(node, typeName, typeBinding);
 		return super.visit(node);
 	}
 
 	/**
 	 * Checks Import Generation for Normal Annotation
 	 */
 	@Override
 	public boolean visit(NormalAnnotation node) {
 		if (node == null)
 			return super.visit(node);
 		
 		ITypeBinding typeBinding = node.resolveTypeBinding();
 		if (typeBinding == null)
 			return super.visit(node);
 		
 		Name typeName = node.getTypeName();
 		this.checkType(node, typeName, typeBinding);
 		return super.visit(node);
 	}
 
 	/**
 	 * Checks Import Generation for Single Member Annotation
 	 */
 	@Override
 	public boolean visit(SingleMemberAnnotation node) {
 		if (node == null)
 			return super.visit(node);
 		
 		ITypeBinding typeBinding = node.resolveTypeBinding();
 		if (typeBinding == null)
 			return super.visit(node);
 		
 		Name typeName = node.getTypeName();
 		this.checkType(node, typeName, typeBinding);
 		return super.visit(node);
 	}
 
 	/**
 	 * Checks Import Generation for Simple Type (most common case)
 	 */
 	@Override
 	public boolean visit(SimpleType node) {
 		if (node == null)
 			return super.visit(node);
 		
 		ITypeBinding typeBinding = node.resolveBinding();
 		if (typeBinding == null)
 			return super.visit(node);
 		
 		Name typeName = node.getName();
 		this.checkType(node, typeName, typeBinding);
 		return super.visit(node);
 	}
 
 	/**
 	 * Checks Import Generation for Qualified Name.
 	 * 
 	 * This is a specific case, in order to deal with Enumerations & static
 	 * method invocations.
 	 */
 	@Override
 	public boolean visit(QualifiedName node) {
 
 		// We are is an import. No need to trim anything !!!
 		if (node.getParent() instanceof ImportDeclaration)
 			return super.visit(node);
 
 		// We get the current binding
 		IBinding binding = node.resolveBinding();
 
 		if (binding != null) {
 			// We check if we are dealing with a type
 			// binding (Case when Enumerations)
 			if (binding.getKind() == IBinding.TYPE && !((ITypeBinding) binding).isRecovered()) {
 				checkQualifiedType(node.getParent(), node, (ITypeBinding) binding);
 			} else {
 				// We check if we are dealing with a Method Binding referencing
 				// a Type Binding (Case when static methods invocations)
 				if (node.getQualifier().isQualifiedName()
 						&& Modifier.isStatic(binding.getModifiers())) {
 					QualifiedName qualifier = (QualifiedName) node.getQualifier();
 					IBinding binding2 = qualifier.resolveBinding();
 					if (binding2 != null && binding2.getKind() == IBinding.TYPE
 							&& !((ITypeBinding) binding2).isRecovered()) {
 						checkQualifiedType(node, qualifier, (ITypeBinding) binding2);
 					}
 				}
 			}
 		}
 		return super.visit(node);
 	}
 
 	/**
 	 * Checks Type, represented by its use in parent node, its name, and its
 	 * type binding.
 	 * 
 	 * @param node
 	 *            : Parent Node
 	 * @param typeName
 	 *            : Type Name
 	 * @param typeBinding
 	 *            : Type Binding
 	 */
 	private void checkType(ASTNode node, Name typeName, ITypeBinding typeBinding) {
 		if (typeName.isSimpleName())
 			this.checkSimpleType(node, (SimpleName) typeName, typeBinding);
 		else
 			this.checkQualifiedType(node, (QualifiedName) typeName, typeBinding);
 	}
 
 	/**
 	 * Checks Qualified Type, represented by its use in parent node, its name,
 	 * and its type binding.
 	 * 
 	 * @param node
 	 *            : Parent Node
 	 * @param qualifiedName
 	 *            : Qualified Name
 	 * @param typeBinding
 	 *            : Type Binding
 	 */
 	@SuppressWarnings("unchecked")
 	private void checkQualifiedType(ASTNode node, QualifiedName qualifiedName,
 			ITypeBinding typeBinding) {
 
 		// At first we extract package name & type for Type, by :
 		// - Splitting the String if Type Binding has been deduced (recovered)
 		// - Taking it from Binding otherwise
 		String fullyQualifiedName = qualifiedName.getFullyQualifiedName();
 		String typeName = null;
 		String packageName = null;
 
 		if (typeBinding == null || typeBinding.isRecovered()) {
 			typeName = qualifiedName.getFullyQualifiedName().substring(
 					qualifiedName.getFullyQualifiedName().lastIndexOf(".") + 1);
 			packageName = qualifiedName.getFullyQualifiedName().substring(0,
 					qualifiedName.getFullyQualifiedName().lastIndexOf("."));
 		} else {
 			typeBinding = typeBinding.getErasure();
 			typeName = typeBinding.getName();
 			IPackageBinding packageBinding = typeBinding.getPackage();
 			if (packageBinding == null)
 				return;
 			packageName = packageBinding.getName();
 		}
 
 		// Checks if name should be trimmed (if class with same name but
 		// different package has already been registered), and trims it if
 		// needed
 		if (shouldTrimName(packageName, typeName)) {
 			StructuralPropertyDescriptor locationInParent = qualifiedName.getLocationInParent();
 			if (locationInParent == null)
 				return;
 			if (locationInParent.isChildListProperty()) {
 				ChildListPropertyDescriptor clpd = (ChildListPropertyDescriptor) locationInParent;
 				List<ASTNode> astNodes = (List<ASTNode>) node.getStructuralProperty(clpd);
 				astNodes.remove(qualifiedName);
 				astNodes.add(node.getAST().newName(typeName));
 			} else {
 				node.setStructuralProperty(locationInParent, node.getAST().newName(typeName));
 			}
 			hasModifications = true;
 		}
 
 		// Checks if import should be added (e.g. package is not java.lang) and
 		// does it if needed
 		if (shouldAddImport(node, typeName, packageName, fullyQualifiedName)) {
 			this.tryAddImport(node.getAST(), fullyQualifiedName);
 			if (!typesToPackageBinding.containsKey(typeName))
 				typesToPackageBinding.put(typeName, packageName);
 		} else if (this.currentPackageName.equals(packageName))
 			if (!typesToPackageBinding.containsKey(typeName))
 				typesToPackageBinding.put(typeName, packageName);
 	}
 
 	/**
 	 * Checks Simple Type, represented by its use in parent node, its name, and
 	 * its type binding.
 	 * 
 	 * @param node
 	 *            : Parent Node
 	 * @param simpleName
 	 *            : Simple Name
 	 * @param typeBinding
 	 *            : Type Binding
 	 */
 	private void checkSimpleType(ASTNode node, SimpleName simpleName, ITypeBinding typeBinding) {
 
 		// If type binding is null, recovered or type corresponds to primitive:
 		// returns.
 		if (typeBinding == null || typeBinding.isRecovered() || typeBinding.getPackage() == null)
 			return;
 
 		// Extracts information from Type Binding.
 		String packageName = typeBinding.getErasure().getPackage().getName();
 		String typeName = typeBinding.getErasure().getName();
 		String fullyQualifiedName = typeBinding.getErasure().getQualifiedName();
 
 		// Checks if need to add associated import, and adds it if necessary
 		if (shouldAddImport(node, typeName, packageName, fullyQualifiedName)) {
 			this.tryAddImport(node.getAST(), fullyQualifiedName);
 
 			if (!typesToPackageBinding.containsKey(typeName))
 				typesToPackageBinding.put(typeName, packageName);
 		}
 	}
 
 	/**
 	 * Creates and stores import declaration for FQN passed as parameter, is it
 	 * has not been registered yet.
 	 * 
 	 * @param ast
 	 *            : AST, needed to create the ImportDeclaration object itself
 	 * @param fullyQualifiedName
 	 *            : Import FQN
 	 */
 	private void tryAddImport(AST ast, String fullyQualifiedName) {
 		if (!this.resolvedImports.containsKey(fullyQualifiedName)) {
 			ImportDeclaration importDeclaration = ast.newImportDeclaration();
 			importDeclaration.setName(ast.newName(fullyQualifiedName));
 			this.resolvedImports.put(fullyQualifiedName, importDeclaration);
 		}
 		hasModifications = true;
 	}
 
 	/**
 	 * Determines whether an import should be added for the Type, which
 	 * information are passed as parameter.
 	 * 
 	 * @param node
 	 *            : Current Node
 	 * @param typeName
 	 *            : Type Simple Name
 	 * @param packageName
 	 *            : Type's Package Name
 	 * @param fullyQualifiedName
 	 *            Types FQN
 	 * @return true if need to add import, false otherwise
 	 */
 	private boolean shouldAddImport(ASTNode node, String typeName, String packageName,
 			String fullyQualifiedName) {
 
 		// Determines the package name of the current CU containing this node.
 		// This way, no import is added if I am in the same package.
 		String currentPackageName = getPackageDeclarationForNode(node) != null ? getPackageDeclarationForNode(
 				node).getName().getFullyQualifiedName()
 				: "";
 
 		// If there is a conflict with the current class name, we cannot add an
 		// import...
 		if (this.currentTypeName != null && this.currentPackageName != null)
 			if (this.currentTypeName.equals(typeName)
 					&& !this.currentPackageName.equals(packageName))
 				return false;
 
 		// Conditions to add a new import:
 		// - Package Name is not null
 		// - Package Name not yet registered
 		// - Package is not "java.lang"
 		// - Package is not the current CU's package
 		return packageName != null && !this.typesToPackageBinding.containsKey(typeName)
 				&& !this.resolvedImports.containsKey(fullyQualifiedName)
 				&& !packageName.equals("java.lang") && !packageName.equals(currentPackageName);
 	}
 
 	/**
 	 * Determines if we should trim name for the current Type described by its
 	 * simple name and package name.
 	 * 
 	 * @param packageName
 	 * @param typeName
 	 * @return true if name can be trimmed, false otherwise
 	 */
 	private boolean shouldTrimName(String packageName, String typeName) {
 		// If there is a conflict with the current class name, we cannot trim
 		// name
 		if (this.currentTypeName != null && this.currentPackageName != null)
 			if (this.currentTypeName.equals(typeName)
 					&& !this.currentPackageName.equals(packageName))
 				return false;
 
 		// If the typeName is not yet registered with another package, we can
 		// trim it !
 		return this.typesToPackageBinding.get(typeName) == null
 				|| packageName.equals(this.typesToPackageBinding.get(typeName));
 	}
 
 	/**
 	 * Returns Package declaration of the CU containing this node.
 	 * 
 	 * @param node
 	 *            : Node
 	 * @return Package Declaration.
 	 */
 	private PackageDeclaration getPackageDeclarationForNode(ASTNode node) {
 		ASTNode root = node.getRoot();
 		if (root instanceof CompilationUnit)
 			return ((CompilationUnit) root).getPackage();
 		return null;
 	}
 
 }
