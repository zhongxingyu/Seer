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
 package br.octahedron.figgo.modules.user.controller;
 
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceManager;
 import br.octahedron.cotopaxi.inject.Inject;
import br.octahedron.cotopaxi.interceptor.TemplateInterceptor;
import br.octahedron.cotopaxi.view.response.RenderableResponse;
 import br.octahedron.figgo.modules.user.manager.UserManager;
 
 /**
  * A {@link ResponseInterceptor} that adds the current user to the response
  * 
  * @author Danilo Penna Queiroz
  */
public class UserResponseInterceptor extends TemplateInterceptor {
 	
 	@Inject
 	private UserManager userManager;
 	
 	@Inject
 	private NamespaceManager namespaceManager;
 	
 	/**
 	 * @param userManager the userManager to set
 	 */
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 	
 	/**
 	 * @param namespaceManager the namespaceManager to set
 	 */
 	public void setNamespaceManager(NamespaceManager namespaceManager) {
 		this.namespaceManager = namespaceManager;
 	}
 	
 	/* (non-Javadoc)
	 * @see br.octahedron.cotopaxi.interceptor.TemplateInterceptor#preRender(br.octahedron.cotopaxi.view.response.RenderableResponse)
 	 */
 	@Override
	public void preRender(RenderableResponse response) {
 		try {
 			this.namespaceManager.changeToGlobalNamespace();
 			String currentUserId =  this.currentUser();
 			if ( currentUserId != null ) {
 				response.addOutput("user", this.userManager.getUser(currentUserId));
 			}
 		} finally {
 			this.namespaceManager.changeToPreviousNamespace();
 		}
 	}
 
 }
