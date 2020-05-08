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
 package br.octahedron.straight.controller;
 
 import static br.octahedron.straight.controller.ControllerFilter.APPLICATION_DOMAIN;
 import static br.octahedron.straight.controller.ControllerFilter.BARRA;
 
 import java.util.logging.Logger;
 
 import br.octahedron.commons.database.NamespaceCommons;
 import br.octahedron.straight.modules.ManagerBuilder;
 import br.octahedron.straight.modules.Module;
 import br.octahedron.straight.modules.ModuleSpec;
 import br.octahedron.straight.modules.authorization.manager.AuthorizationManager;
 import br.octahedron.straight.modules.users.manager.UsersManager;
 
 /**
  * @author danilo
  * 
  */
 public class ControllerChecker {
 
 	private static final Logger logger = Logger.getLogger(ControllerChecker.class.getName());
 	private final UsersManager usersManager = (UsersManager) ManagerBuilder.getUserManager();
 	private final AuthorizationManager authorizationManager = (AuthorizationManager) ManagerBuilder.getAuthorizationManager();
 
 	/**
 	 * @throws NotAuthorizedException
 	 * 
 	 */
 	public void check(String domain, String email, String moduleName, String action) throws NotFoundException, NotLoggedException,
 			InexistentAccountException, NotAuthorizedException {
 
 		logger.info(">>>" + domain + " : " + email + " : " + moduleName + " : " + action);
 		try {
 			if (APPLICATION_DOMAIN.equals(domain) && BARRA.equals(moduleName)) { 
 				moduleName = Module.USER.name();
 			} else if (BARRA.equals(moduleName)) {
				moduleName = Module.DOMAIN.name();
 			}
 			Module module = Module.valueOf(moduleName);
 			ModuleSpec spec = module.getModuleSpec();
 
 			// checks authentication needs
 			if (spec.needsAuthentication(action)) {
 				if (email == null && !action.isEmpty()) {
 					throw new NotLoggedException();
				} else if (email != null && !this.usersManager.existsUser(email) && 
						   !"USER_NEW".equals(action) && !"USER_CREATE".equals(action)) {
 					throw new InexistentAccountException();
 				}
 			}
 			// checks authorization
 			// TODO: "USER_NEW".equals(action) deve lançar exceção
 			if (spec.needsAuthorization(action)) {
 				if (!this.authorizationManager.isAuthorized(domain, email, action)) {
 					throw new NotAuthorizedException();
 				}
 			}
 
 			if (spec.usesDomainNamespace()) {
 				NamespaceCommons.changeToNamespace(domain);
 			}
 		} catch (IllegalArgumentException e) {
 			throw new NotFoundException(moduleName);
 		}
 
 	}
 }
