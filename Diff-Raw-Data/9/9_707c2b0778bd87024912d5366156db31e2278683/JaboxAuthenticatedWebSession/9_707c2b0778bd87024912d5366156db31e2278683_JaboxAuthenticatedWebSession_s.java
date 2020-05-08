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
 package org.jabox.webapp.pages;
 
 import org.apache.wicket.Request;
 import org.apache.wicket.authentication.AuthenticatedWebSession;
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.injection.web.InjectorHolder;
 import org.apache.wicket.persistence.provider.GeneralDao;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.jabox.model.User;
 
 /**
  * Authenticated session subclass
  */
 public class JaboxAuthenticatedWebSession extends AuthenticatedWebSession {
 	private static final long serialVersionUID = 1L;
 
	@SpringBean(name = "GeneralDao")
 	protected GeneralDao _generalDao;
 
 	private Long _userId;
 
 	/**
 	 * Construct.
 	 * 
 	 * @param request
 	 *            The current request object
 	 */
 	public JaboxAuthenticatedWebSession(final Request request) {
 		super(request);
 		InjectorHolder.getInjector().inject(this);
 	}
 
 	/**
 	 * @see org.apache.wicket.authentication.AuthenticatedWebSession#authenticate(java.lang.String,
 	 *      java.lang.String)
 	 */
 	@Override
 	public boolean authenticate(final String username, final String password) {
 		if (username == null || password == null) {
 			return false;
 		}
 
 		User user = _generalDao.findEntityByQuery("_login", username,
 				User.class);
 
 		if (user == null) {
 			return false;
 		}
 
 		if (username.equals(user.getLogin())
 				&& password.equals(user.getPassword())) {
 			_userId = user.getId();
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * @see org.apache.wicket.authentication.AuthenticatedWebSession#getRoles()
 	 */
 	@Override
 	public Roles getRoles() {
 		if (isSignedIn()) {
 			// If the user is signed in, they have these roles
 			return new Roles(Roles.ADMIN);
 		}
 		return null;
 	}
 
 	/**
 	 * @return the id of the authenticated user.
 	 */
 	public Long getUserId() {
 		return _userId;
 	}
 }
