 /*
  * Copyright (c) 2005, 2008 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *     Artem Tikhomirov (Borland) - Migration to OCL expressions
  */
 package org.eclipse.gmf.internal.xpand.ast;
 
 import java.util.Set;
 
 import org.eclipse.gmf.internal.xpand.expression.ast.SyntaxElement;
 import org.eclipse.gmf.internal.xpand.model.AnalysationIssue;
 import org.eclipse.gmf.internal.xpand.model.XpandAdvice;
 import org.eclipse.gmf.internal.xpand.model.XpandDefinition;
 import org.eclipse.gmf.internal.xpand.model.ExecutionContext;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 
 /**
  * XXX why it's SyntaxElement? What does 'getLine()' means?
  * 
  * @author Sven Efftinge
  */
 public class Template extends SyntaxElement implements XpandResource {
 	private final NamespaceImport[] imports;
 
 	private final Definition[] definitions;
 
 	private final ImportDeclaration[] extensions;
 
 	private final Advice[] advices;
 
 	private String qualifiedName;
 
 	private String[] importStrings = null;
 
 	private String[] importedExtensions = null;
 
 	public Template(final int start, final int end, final int line, final NamespaceImport[] imports, final ImportDeclaration[] extensions, final Definition[] definitions, final Advice[] advices) {
 		super(start, end, line);
 		this.imports = imports;
 		this.extensions = extensions;
 		for (Definition element : definitions) {
 			element.setOwner(this);
 		}
 		this.definitions = definitions;
 		for (Advice element : advices) {
 			element.setOwner(this);
 		}
 		this.advices = advices;
 	}
 
 	public String getFullyQualifiedName() {
 		// XXX what's the reason to have both file name and qualified name?
 		return qualifiedName == null ? getFileName() : qualifiedName;
 	}
 
 	public void setFullyQualifiedName(String name) {
 		qualifiedName = name;
 	}
 
 	public XpandDefinition[] getDefinitions() {
 		return definitions;
 	}
 
 	public void analyze(ExecutionContext ctx, final Set<AnalysationIssue> issues) {
 		ctx = (ExecutionContext) ctx.cloneWithResource(this);
 		for (Definition element : definitions) {
 			element.analyze(ctx, issues);
 		}
 		for (Advice element : advices) {
 			element.analyze(ctx, issues);
 		}
 	}
 
 	// XXX is it really worth it to kepp imports as ast nodes?
 	// Is it performance gain to duplicate them here with string[]?
 	public String[] getImportedNamespaces() {
 		if (importStrings == null) {
 			importStrings = new String[imports.length];
 			for (int i = 0; i < importStrings.length; i++) {
 				importStrings[i] = imports[i].getImportString();
 			}
 		}
 		return importStrings;
 	}
 
 	public String[] getImportedExtensions() {
 		if (importedExtensions == null) {
 			importedExtensions = new String[extensions.length];
 			for (int i = 0; i < extensions.length; i++) {
 				importedExtensions[i] = extensions[i].getImportString();
 			}
 		}
 		return importedExtensions;
 	}
 
 	public XpandAdvice[] getAdvices() {
 		return advices;
 	}
 }
