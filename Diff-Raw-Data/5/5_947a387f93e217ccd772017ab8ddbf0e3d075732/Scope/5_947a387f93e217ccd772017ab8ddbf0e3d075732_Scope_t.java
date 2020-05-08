 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.compiler.env.lookup;
 
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 import org.eclipse.dltk.internal.compiler.lookup.MethodScope;
 import org.eclipse.dltk.internal.compiler.lookup.SourceModuleScope;
 import org.eclipse.dltk.internal.compiler.lookup.TypeScope;
 
 public abstract class Scope {
 
 	/* Scope kinds */
 	public final static int BLOCK_SCOPE = 1;
	public final static int METHOD_SCOPE = 2;
 	public final static int CLASS_SCOPE = 3;
 	public final static int COMPILATION_UNIT_SCOPE = 4;
	
 	/* Argument Compatibilities */
 	public final static int NOT_COMPATIBLE = -1;
 	public final static int COMPATIBLE = 0;
 	public final static int AUTOBOX_COMPATIBLE = 1;
 	public final static int VARARGS_COMPATIBLE = 2;
 
 	/* Type Compatibilities */
 	public static final int EQUAL_OR_MORE_SPECIFIC = -1;
 	public static final int NOT_RELATED = 0;
 	public static final int MORE_GENERIC = 1;
 
 	public int kind;
 	public Scope parent;
 
 	protected Scope(int kind, Scope parent) {
 		this.kind = kind;
 		this.parent = parent;
 	}
 
 	public final TypeScope classScope() {
 		Scope scope = this;
 		do {
 			if (scope instanceof TypeScope)
 				return (TypeScope) scope;
 			scope = scope.parent;
 		} while (scope != null);
 		return null;
 	}
 
 	public final SourceModuleScope compilationUnitScope() {
 		Scope lastScope = null;
 		Scope scope = this;
 		do {
 			lastScope = scope;
 			scope = scope.parent;
 		} while (scope != null);
 		return (SourceModuleScope) lastScope;
 	}
 
 	public final TypeScope enclosingClassScope() {
 		Scope scope = this;
 		while ((scope = scope.parent) != null) {
 			if (scope instanceof TypeScope)
 				return (TypeScope) scope;
 		}
 		return null; // may answer null if no type around
 	}
 
 	public final MethodScope enclosingMethodScope() {
 		Scope scope = this;
 		while ((scope = scope.parent) != null) {
 			if (scope instanceof MethodScope)
 				return (MethodScope) scope;
 		}
 		return null; // may answer null if no method around
 	}
 
 	public final LookupEnvironment environment() {
 		Scope scope, unitScope = this;
 		while ((scope = unitScope.parent) != null)
 			unitScope = scope;
 		return ((SourceModuleScope) unitScope).environment;
 	}
 
 	// start position in this scope - for ordering scopes vs. variables
 	public int startIndex() {
 		return 0;
 	}
 }
