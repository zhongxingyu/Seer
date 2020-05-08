 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.configuration.controller;
 
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceManager;
 import br.octahedron.cotopaxi.inject.Inject;
import br.octahedron.cotopaxi.interceptor.ResponseInterceptor;
import br.octahedron.cotopaxi.view.response.InterceptableResponse;
 import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.configuration.manager.ConfigurationManager;
 
 /**
  * A response interceptor that adds the current DomainConfiguration to output
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
public class DomainResponseInterceptor extends ResponseInterceptor {
 	
 	@Inject
 	private ConfigurationManager configurationManager;
 	
 	@Inject
 	private NamespaceManager namespaceManager;
 	
 	/**
 	 * @param namespaceManager the namespaceManager to set
 	 */
 	public void setNamespaceManager(NamespaceManager namespaceManager) {
 		this.namespaceManager = namespaceManager;
 	}
 	
 	/**
 	 * @param configurationManager the configurationManager to set
 	 */
 	public void setConfigurationManager(ConfigurationManager configurationManager) {
 		this.configurationManager = configurationManager;
 	}
 	
 	/* (non-Javadoc)
	 * @see br.octahedron.cotopaxi.interceptor.ResponseInterceptor#preRender(br.octahedron.cotopaxi.view.response.InterceptableResponse)
 	 */
 	@Override
	public void preRender(InterceptableResponse response) {
 		try {
 			// TODO review
 			String subdomain = this.subDomain();
 			if (!subdomain.equals("www")) {
 				this.namespaceManager.changeToNamespace(subdomain);
 				response.addOutput("domain", this.configurationManager.getDomainConfiguration());
 				response.addOutput("modules", this.configurationManager.getModulesInfoService());
 			}
 		} catch (DataDoesNotExistsException e) {
 			// nothing to do here.
 		} finally {
 			namespaceManager.changeToPreviousNamespace();
 		}
 	}
 }
