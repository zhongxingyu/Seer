 /*******************************************************************************
  * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.codegen.ui.generators.common;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.eef.EEFGen.EEFGenModel;
 import org.eclipse.emf.eef.codegen.EEFCodegenPlugin;
 import org.eclipse.emf.eef.codegen.core.launcher.AbstractPropertiesGeneratorLauncher;
 import org.eclipse.emf.eef.codegen.core.services.PropertiesGeneratorLaunchersServices;
 
 /**
  * Main entry point of the 'Codegen' generation module.
  * 
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class GenerateAll {
 
 	/**
 	 * The output folder.
 	 */
 	private File targetFolder;
 
 	/**
 	 * The Generation PSM
 	 */
 	private EEFGenModel eefGenModel;
 
 	/**
 	 * A set containing the target folder for generation
 	 */
 	private Set<IContainer> generationTargets;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param modelURI
 	 *            is the URI of the model.
 	 * @param targetFolder
 	 *            is the output folder
 	 * @throws IOException
 	 *             Thrown when the output cannot be saved.
 	 */
 	public GenerateAll(IContainer targetFolder, EEFGenModel eefGenModel) {
 		if (targetFolder.getLocation() != null) {
			this.targetFolder = targetFolder.getLocation().toFile();
		} else {
 			EEFCodegenPlugin.getDefault().logWarning(
 					new IllegalArgumentException("TargetFolder must specify a correct location"));
 		}
 		this.eefGenModel = eefGenModel;
 		this.generationTargets = new HashSet<IContainer>();
 		this.generationTargets.add(targetFolder);
 	}
 
 	/**
 	 * @return the generationTargets
 	 */
 	public Set<IContainer> getGenerationTargets() {
 		return generationTargets;
 	}
 
 	/**
 	 * Launches the generation.
 	 * 
 	 * @throws IOException
 	 *             Thrown when the output cannot be saved.
 	 */
 	public void doGenerate(IProgressMonitor monitor) throws IOException {
 		if (targetFolder == null) {
 			return;
 		}
 		if (!targetFolder.exists()) {
 			monitor.subTask("Creating target folder");
 			targetFolder.mkdirs();
 			monitor.worked(1);
 		}
 
 		List<Object> arguments = new ArrayList<Object>();
 		monitor.subTask("Loading...");
 		org.eclipse.emf.eef.codegen.launcher.EEFLauncher launcher = new org.eclipse.emf.eef.codegen.launcher.EEFLauncher(
 				eefGenModel, targetFolder, arguments);
 		monitor.worked(1);
 		monitor.subTask("Generating EEF code using " + eefGenModel.eResource().getURI().lastSegment() + "...");
 		launcher.doGenerate(BasicMonitor.toMonitor(new SubProgressMonitor(monitor, IProgressMonitor.UNKNOWN)));
 		monitor.worked(1);
 		for (AbstractPropertiesGeneratorLauncher abstractPropertiesGeneratorLauncher : PropertiesGeneratorLaunchersServices
 				.getInstance().getlaunchers()) {
 			abstractPropertiesGeneratorLauncher.doGenerate(eefGenModel, targetFolder, monitor);
 			if (!abstractPropertiesGeneratorLauncher.getTargetContainer().isEmpty())
 				generationTargets.addAll(abstractPropertiesGeneratorLauncher.getTargetContainer());
 		}
 	}
 
 }
