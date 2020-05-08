 /*
  * Jabox Open Source Version
  * Copyright (C) 2009-2010 Dimitris Kapanidis                                                                                                                          
  * 
  * This file is part of Jabox
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.gnu.org/licenses/.
  */
 package org.jabox.scm.git;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.model.IModel;
 import org.jabox.apis.scm.SCMConnector;
 import org.jabox.apis.scm.SCMException;
 import org.jabox.environment.Environment;
 import org.jabox.model.DeployerConfig;
 import org.jabox.model.Project;
 import org.jabox.model.Server;
 import org.jabox.utils.TemporalDirectory;
 import org.springframework.stereotype.Service;
 
 @Service
 public class GITConnector implements SCMConnector<IGITConnectorConfig>,
 		Serializable {
 	public static final String ID = "plugin.scm.git";
 
 	private static final long serialVersionUID = -3875844507330633672L;
 
 	private File _tmpDir;
 
 	public String getName() {
 		return "Git";
 	}
 
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	public File createProjectDirectories(final Project project,
 			final IGITConnectorConfig config) throws SCMException {
 		IGITConnectorConfig svnc = config;

 		GITFacade git = new GITFacade();
		_tmpDir = GITRepository.getGitBaseDir();
 
 		git.checkoutBaseDir(_tmpDir, svnc);
 		// Create Project directory and its trunk/branches/tags
 		// subdirectories
 		File trunkDir = createProjectDirectories(project, _tmpDir);
 		return trunkDir;
 
 	}
 
 	/**
 	 * 
 	 * @param project
 	 * @param tmpDir
 	 * @return the trunk directory.
 	 */
 	private static File createProjectDirectories(final Project project,
 			final File tmpDir) {
 		assert tmpDir.exists();
 		File projectDir = new File(tmpDir, project.getName());
 		projectDir.mkdirs();
 		return projectDir;
 	}
 
 	public void commitProject(final Project project,
 			final IGITConnectorConfig svnc) throws SCMException {
 		assert _tmpDir != null && _tmpDir.exists();
 
 		GITFacade git = new GITFacade();
		git.commitProject(project, new File(_tmpDir, project.getName()), svnc);
 	}
 
 	public DeployerConfig newConfig() {
 		return new GITConnectorConfig();
 	}
 
 	public Component newEditor(final String id, final IModel<Server> model) {
 		return new GITConnectorEditor(id, model);
 	}
 }
