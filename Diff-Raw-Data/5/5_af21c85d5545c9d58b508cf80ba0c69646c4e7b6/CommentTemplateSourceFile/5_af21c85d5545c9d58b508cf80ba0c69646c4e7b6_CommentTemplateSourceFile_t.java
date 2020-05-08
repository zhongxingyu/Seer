 /*******************************************************************************
  * Copyright (c) 2006-2013
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package org.buildboost.genext.commenttemplate;
 
 import java.io.File;
 
 import de.devboost.buildboost.artifacts.AbstractArtifact;
 import de.devboost.buildboost.artifacts.Plugin;
 import de.devboost.buildboost.model.UnresolvedDependency;
 import de.devboost.buildboost.util.EclipsePluginHelper;
 
 /**
  * A {@link CommentTemplateSourceFile} is a Java source file the contains
  * CommentTemplate annotations.
  */
 public class CommentTemplateSourceFile extends AbstractArtifact {
 
 	private static final long serialVersionUID = -1197582465990182947L;
 	private File file;
 	private File projectDir;
 
 	public CommentTemplateSourceFile(File file) {
 		this.file = file;
 		this.projectDir = new EclipsePluginHelper().findProjectDir(file);
		
		// add dependency to CommentTemplate build extension to make sure the
		// extension is in the class path of the generated build script
		UnresolvedDependency autobuildDependency = new UnresolvedDependency(Plugin.class, "org.buildboost.buildext.commenttemplate", null, true, null, true, false, false);
 		getUnresolvedDependencies().add(autobuildDependency);
 	}
 
 	public String getIdentifier() {
 		// TODO this is not unique
 		return file.getName();
 	}
 
 	public File getFile() {
 		return file;
 	}
 
 	public File getProjectDir() {
 		return projectDir;
 	}
 }
