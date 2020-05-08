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
 package br.octahedron.straight.modules.authorization;
 
 import static br.octahedron.commons.database.NamespaceCommons.backToPreviousNamespace;
 import static br.octahedron.commons.database.NamespaceCommons.changeToGlobalNamespace;
 
 import java.util.Collection;
 
 import br.octahedron.commons.inject.Inject;
 import br.octahedron.straight.modules.authorization.data.Role;
 
 /**
  * Responsible by provide a mechanism to check user authorizations.
  * 
  * As this entity should be called by internal modules, it should take care the current namespace.
  * 
  * @author Danilo Queiroz
  */
 public class AuthorizationDecorator implements AuthorizationIF {
 
 	@Inject
	private AuthorizationIF authorizationManager;
 
 	/**
 	 * @param authorizationManager
 	 *            the authorizationManager to set
 	 */
	public void setAuthorizationManager(AuthorizationIF authorizationManager) {
 		this.authorizationManager = authorizationManager;
 	}
 
 	/**
 	 * Checks if the given user is authorized to perform the given activity at the given domain.
 	 * 
 	 * @return <code>true</code> if the given user is authorized to perform the given activity at
 	 *         the given domain, <code>false</code> otherwise.
 	 */
 	public boolean isAuthorized(String domain, String username, String activity) {
 		try {
 			changeToGlobalNamespace();
 			return this.authorizationManager.isAuthorized(domain, username, activity);
 		} finally {
 			backToPreviousNamespace();
 		}
 	}
 	
 	/**
 	 * Get all domains that the given user is in any {@link Role}.
 	 * The {@link Collection} will be empty if the given user hasn't any role.
 	 * 
 	 * @return All domains that the user has some role.
 	 */
 	public Collection<String> getUserDomains(String username) {
 		try {
 			changeToGlobalNamespace();
 			return this.authorizationManager.getUserDomains(username);
 		} finally {
 			backToPreviousNamespace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#addActivitiesToRole(java.lang.String, java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public void addActivitiesToRole(String domainName, String roleName, String... activities) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#addUsersToRole(java.lang.String, java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public void addUsersToRole(String domainName, String roleName, String... users) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#createRole(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public Role createRole(String domainName, String roleName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#existsRole(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean existsRole(String domainName, String roleName) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#getRole(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public Role getRole(String domainName, String roleName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see br.octahedron.straight.modules.authorization.AuthorizationIF#removeRole(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void removeRole(String domainName, String roleName) {
 		// TODO Auto-generated method stub
 		
 	}
 }
