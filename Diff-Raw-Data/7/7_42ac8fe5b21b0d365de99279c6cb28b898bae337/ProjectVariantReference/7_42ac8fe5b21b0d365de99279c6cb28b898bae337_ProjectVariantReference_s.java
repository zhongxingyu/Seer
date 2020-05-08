 /*******************************************************************************
  * Copyright (c) 2010 Broadcom Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Broadcom Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.resources.*;
 
 /**
  * Represents a reference to a specific project variant. The project is
  * stored as an IProject handle, and the variant as the variants unique name.
  * This allows references to project variants that do not exist in the workspace,
  * or variants that do not exist in the project.
  * @since 3.2
  * @noextend This class is not intended to be subclassed by clients.
  * @noinstantiate This class is not intended to be instantiated by clients.
  */
 public class ProjectVariantReference implements IProjectVariantReference {
 	private final IProject project;
 	private String variantName;
 
 	/**
 	 * Create a reference to a project's active variant.
 	 */
 	public ProjectVariantReference(IProject project) {
 		Assert.isLegal(project != null);
 		this.project = project;
 		this.variantName = null;
 	}
 
 	/**
 	 * Create a reference to a specific variant of a project.
 	 */
 	public ProjectVariantReference(IProject project, String variantName) {
 		Assert.isLegal(project != null);
 		this.project = project;
 		this.variantName = variantName;
 	}
 
 	/**
 	 * Create a reference to a specific variant of a project.
 	 */
 	public ProjectVariantReference(IProjectVariant projectVariant) {
 		Assert.isLegal(projectVariant != null);
 		this.project = projectVariant.getProject();
 		this.variantName = projectVariant.getVariantName();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectVariantReference#getProject()
 	 */
 	public IProject getProject() {
 		return project;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectVariantReference#getVariantName()
 	 */
 	public String getVariantName() {
 		return variantName;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see IProjectVariantReference#setVariantName(String)
 	 */
 	public void setVariantName(String name) {
 		Assert.isLegal(name != null);
 		Assert.isTrue(variantName == null);
 		variantName = name;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectVariantReference#getVariant()
 	 */
 	public IProjectVariant getVariant() throws CoreException {
		ProjectDescription desc = ((Project) project).internalGetDescription();
 		IProjectVariant variant = null;
 		if (variantName == null)
			variant = desc.internalGetActiveVariant();
 		else {
 			IProjectVariant[] variants = project.getVariants();
 			for (int i = 0; i < variants.length; i++)
 				if (variants[i].getVariantName().equals(variantName)) {
 					variant = variants[i];
 					break;
 				}
 		}
 		if (variant == null)
 			throw new ResourceException(IResourceStatus.PROJECT_VARIANT_NOT_FOUND, project.getFullPath(), null, null);
		if (((ProjectVariant) variant).internalGetProject() == null)
			((ProjectVariant) variant).setProject(project);
 		return variant;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Object#hashCode()
 	 */
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((project == null) ? 0 : project.hashCode());
 		result = prime * result + ((variantName == null) ? 0 : variantName.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Object#equals(Object)
 	 */
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ProjectVariantReference other = (ProjectVariantReference) obj;
 		if (project == null) {
 			if (other.project != null)
 				return false;
 		} else if (!project.equals(other.project))
 			return false;
 		if (variantName == null) {
 			if (other.variantName != null)
 				return false;
 		} else if (!variantName.equals(other.variantName))
 			return false;
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see Object#toString()
 	 */
 	/** For debugging purposes only. */
 	public String toString()
 	{
 		StringBuffer result = new StringBuffer();
 		result.append(project.getName());
 		result.append(";"); //$NON-NLS-1$
 		if (variantName != null)
 			result.append(variantName);
 		else
 			result.append("<active>"); //$NON-NLS-1$
 		return result.toString();
 	}
  }
