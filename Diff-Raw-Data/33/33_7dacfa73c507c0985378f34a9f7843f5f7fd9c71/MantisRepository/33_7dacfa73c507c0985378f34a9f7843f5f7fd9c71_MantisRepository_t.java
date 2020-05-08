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
 
 package org.jabox.its.mantis;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.model.IModel;
 import org.jabox.apis.its.ITSConnector;
 import org.jabox.apis.scm.SCMConnectorConfig;
 import org.jabox.model.DeployerConfig;
 import org.jabox.model.Project;
 import org.jabox.model.Server;
 import org.springframework.stereotype.Service;
 import org.xml.sax.SAXException;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import com.meterware.httpunit.WebConversation;
 import com.meterware.httpunit.WebForm;
 import com.meterware.httpunit.WebRequest;
 import com.meterware.httpunit.WebResponse;
 
 @Service
 public class MantisRepository implements ITSConnector<MantisRepositoryConfig>,
 		Serializable {
 	private static final long serialVersionUID = 8131183843391948936L;
 	public static final String ID = "plugin.its.mantis";
 
	private WebConversation _wc;
 
 	public String getName() {
 		return "Mantis";
 	}
 
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 
 	public MantisRepository() {
 		_wc = new WebConversation();
 	}
 
 	public boolean login(final MantisRepositoryConfig config)
 			throws MalformedURLException, IOException, SAXException {
 		WebRequest req = new GetMethodWebRequest(config.getServer().getUrl());
		_wc = new WebConversation();
 		WebResponse resp = _wc.getResponse(req);
 		WebForm form = resp.getForms()[0]; // select the first form in the page
 		form.setParameter("username", config.getUsername());
 		form.setParameter("password", config.getPassword());
 		resp = form.submit();
 
 		// TODO check if not logged in.
 		return true;
 	}
 
 	public boolean addProject(final Project project,
 			final MantisRepositoryConfig config) throws IOException,
 			SAXException {
 		String url = config.getServer().getUrl();
 		WebRequest req = new GetMethodWebRequest(url
				+ "/manage_proj_create_page.php");
 		WebResponse resp = _wc.getResponse(req);
		WebForm form = resp.getForms()[2];
 
		form.setParameter("name", project.getName());
 		form.setParameter("description", project.getDescription());
 		form.submit();
 		return true;
 	}
 
 	public boolean addModule(final Project project,
 			final MantisRepositoryConfig config, final String module,
 			final String description, final String initialOwner)
 			throws SAXException, IOException {
 		return true;
 	}
 
 	public boolean addVersion(final Project project,
 			final MantisRepositoryConfig config, final String version)
 			throws IOException, SAXException {
 		return true;
 	}
 
 	public DeployerConfig newConfig() {
 		return new MantisRepositoryConfig();
 	}
 
 	public Component newEditor(final String id, final IModel<Server> model) {
 		return new MantisRepositoryEditor(id, model);
 	}
 
 	public void addRepository(Project project, MantisRepositoryConfig config,
 			SCMConnectorConfig scmConfig, String username, String password)
 			throws MalformedURLException, IOException, SAXException {
 	}
 
 }
