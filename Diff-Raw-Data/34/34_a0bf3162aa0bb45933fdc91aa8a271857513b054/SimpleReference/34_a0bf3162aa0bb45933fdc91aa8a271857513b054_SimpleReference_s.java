 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 /*
  * (c) 2002, 2005 xored software and others all rights reserved. http://www.xored.com
  */
 
 package org.eclipse.dltk.ast.references;
 
 import org.eclipse.dltk.ast.DLTKToken;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class SimpleReference extends Reference {
 
 	protected String fName;
 
 	public SimpleReference(int start, int end, String name) {
 		super(start, end);
 		this.fName = name;
 	}
 
 	public SimpleReference(DLTKToken token) {
 		this.fName = token.getText();
 		this.setStart(token.getColumn());
 		if (fName != null) {
 			this.setEnd(this.sourceStart() + this.fName.length());
 		}
 	}
 
 	public String getName() {
 		return fName;
 	}
 
 	public void setName(String name) {
 		this.fName = name;
 	}
 
 	public String getStringRepresentation() {
 		return fName;
 	}
 
 	public void printNode(CorePrinter output) {
		output.formatPrintLn("SimpleReference" + this.getSourceRange().toString() + ":(" + this.fName + ")");
 	}
 
 	public String toString() {
 		return this.fName;
 	}
 
 	public boolean equals(Object obj) {
 		if (!(obj instanceof SimpleReference)) {
 			return false;
 		}
 		SimpleReference d = (SimpleReference) obj;
 		return sourceStart() == d.sourceStart() && sourceEnd() == d.sourceEnd()
 				&& (fName != null && fName.equals(d.fName));
 	}
 
 	public int hashCode() {
 		return fName.hashCode();
 	}
 }
