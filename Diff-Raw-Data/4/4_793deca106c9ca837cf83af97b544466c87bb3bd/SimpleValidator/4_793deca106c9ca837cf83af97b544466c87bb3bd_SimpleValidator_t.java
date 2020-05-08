 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.validators.core.tests;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.validators.core.AbstractValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class SimpleValidator extends AbstractValidator {
 	private String value = "";
 	boolean valid = true;
 	protected SimpleValidator(String id, IValidatorType type) {
 		super(id, "", type);
 	}
 	protected SimpleValidator(String id, String name, IValidatorType type) {
 		super(id, name, type);
 	}
 	protected SimpleValidator(String id, Element element, IValidatorType type) throws IOException {
 		super(id, "", type);
 		this.loadFrom(element);
 	}
 	protected void loadFrom(Element element ) {
 		super.loadFrom(element);
 		this.value = element.getAttribute("simple_value");
 		this.valid =  (new Boolean(element.getAttribute("simple_valid"))).booleanValue();
 	}
 	public void storeTo(Document doc, Element element) {
 		super.storeTo(doc, element);
 		element.setAttribute("simple_value", this.value);
 		element.setAttribute("simple_valid", Boolean.toString(this.valid));
 	}
 
 	public IStatus validate(ISourceModule[] module, OutputStream console, IProgressMonitor monitor) {
 		return Status.OK_STATUS;
 	}
 
 	public IStatus validate(IResource[] resource, OutputStream console, IProgressMonitor monitor) {
 		return Status.OK_STATUS;
 	}
 	public void setValid(boolean b) {
 		this.valid = b;
 	}
	public boolean isValidatorValid(IEnvironment environment) {
 		return this.valid;
 	}
 	public void clean(ISourceModule[] module) {
 		
 	}
 	public void clean(IResource[] resource) {
 	}
 	public void setProgressMonitor(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 }
