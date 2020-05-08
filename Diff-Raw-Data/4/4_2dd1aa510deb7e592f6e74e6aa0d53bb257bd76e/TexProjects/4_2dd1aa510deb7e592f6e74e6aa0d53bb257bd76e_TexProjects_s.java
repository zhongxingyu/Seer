 /*=============================================================================#
  # Copyright (c) 2014 Stephan Wahlbrink (WalWare.de) and others.
  # All rights reserved. This program and the accompanying materials
  # are made available under the terms of the Eclipse Public License v1.0
  # which accompanies this distribution, and is available at
  # http://www.eclipse.org/legal/epl-v10.html
  # 
  # Contributors:
  #     Stephan Wahlbrink - initial API and implementation
  #=============================================================================*/
 
 package de.walware.docmlet.tex.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.osgi.util.NLS;
 
 import de.walware.ecommons.resources.ProjectUtil;
 
 import de.walware.docmlet.tex.internal.core.Messages;
 import de.walware.docmlet.tex.internal.core.TexProject;
 
 
 public class TexProjects {
 	
 	
 	public static final String TEX_NATURE_ID= "de.walware.docmlet.tex.natures.Tex"; //$NON-NLS-1$
 	
 	
 	public static ITexProject getTexProject(final IProject project) {
 		return TexProject.getTexProject(project);
 	}
 	
 	/**
 	 * 
 	 * @param project the project to setup
 	 * @param monitor SubMonitor-recommended
 	 * @throws CoreException
 	 */
 	public static void setupTexProject(final IProject project,
 			final IProgressMonitor monitor) throws CoreException {
 		final SubMonitor progress= SubMonitor.convert(monitor,
 				NLS.bind(Messages.TexProject_ConfigureTask_label, project.getName()),
				8 + 2 );
 		
 		final IProjectDescription description= project.getDescription();
 		boolean changed= false;
 		changed|= ProjectUtil.addNature(description, TEX_NATURE_ID);
 		progress.worked(2);
 		
 		if (changed) {
 			project.setDescription(description, progress.newChild(8));
			progress.worked(8);
 		}
 	}
 	
 }
