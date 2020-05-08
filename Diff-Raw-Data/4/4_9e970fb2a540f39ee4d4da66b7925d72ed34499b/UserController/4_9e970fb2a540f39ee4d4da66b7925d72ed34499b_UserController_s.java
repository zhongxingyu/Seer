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
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.APPLICATION_BASE_URL;
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import static br.octahedron.cotopaxi.controller.Converter.Builder.strArray;
 import static br.octahedron.figgo.modules.user.controller.validation.UserValidators.getUserValidator;
 
 import java.util.ArrayList;
 
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.auth.AuthenticationRequired.AuthenticationLevel;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.OnlyForGlobalSubdomainControllerInterceptor.OnlyForGlobal;
 import br.octahedron.figgo.modules.authorization.manager.AuthorizationManager;
 import br.octahedron.figgo.modules.user.data.User;
 import br.octahedron.figgo.modules.user.manager.UserManager;
 
 /**
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class UserController extends Controller {
 
 	static final String BASE_DIR_TPL = "user/";
 	static final String DASHBOARD_TPL = BASE_DIR_TPL + "dashboard.vm";
 	static final String NEW_USER_TPL = BASE_DIR_TPL + "new.vm";
 	static final String EDIT_USER_TPL = BASE_DIR_TPL + "edit.vm";
 	static final String EDIT_USER_URL = "/users/edit";
 
 	@Inject
 	private UserManager userManager;
 	@Inject
 	private AuthorizationManager authorizationManager;
 
 	/**
 	 * @param userManager
 	 *            the userManager to set
 	 */
 	public void setUserManager(UserManager userManager) {
 		this.userManager = userManager;
 	}
 
 	/**
 	 * @param authorizationManager
 	 *            the authorizationManager to set
 	 */
 	public void setAuthorizationManager(AuthorizationManager authorizationManager) {
 		this.authorizationManager = authorizationManager;
 	}
 
 	/**
 	 * Shows new user form
 	 */
 	@OnlyForGlobal
 	@AuthenticationRequired(authenticationLevel = AuthenticationLevel.AUTHENTICATE)
 	public void getNewUser() {
 		String userEmail = this.currentUser();
 		if (!this.userManager.existsUser(userEmail)) {
 			out("email", userEmail);
 			success(NEW_USER_TPL);
 		} else {
 			redirect(EDIT_USER_URL);
 		}
 	}
 
 	/**
 	 * Process new user form
 	 */
 	@OnlyForGlobal
 	@AuthenticationRequired(authenticationLevel = AuthenticationLevel.AUTHENTICATE)
 	public void postCreateUser() {
 		Validator validator = getUserValidator();
 		if (validator.isValid()) {
 			this.userManager.createUser(this.currentUser(), in("name"), in("phoneNumber"), in("description"));
 			redirect(getProperty(APPLICATION_BASE_URL));
 		} else {
 			out("email", this.currentUser());
 			echo();
 			invalid(NEW_USER_TPL);
 		}
 	}
 
 	/**
 	 * Shows user dashboard
 	 */
 	@OnlyForGlobal
 	@AuthenticationRequired
 	public void getDashboardUser() {
 		String userEmail = this.currentUser();
 		out("domains", this.authorizationManager.getActiveUserDomains(userEmail));
 		success(DASHBOARD_TPL);
 	}
 
 	/**
 	 * Shows user edit form
 	 */
 	@OnlyForGlobal
 	@AuthenticationRequired
 	public void getEditUser() {
 		User user = this.userManager.getUser(this.currentUser());
 		out("name", user.getName());
 		out("phoneNumber", user.getPhoneNumber());
 		out("description", user.getDescription());
 		success(EDIT_USER_TPL);
 	}
 
 	@OnlyForGlobal
 	@AuthenticationRequired
 	public void postUpdateUser() {
 		Validator validator = getUserValidator();
 		if (validator.isValid()) {
 			this.userManager.updateUser(this.currentUser(), in("name"), in("phoneNumber"), in("description"));
 			redirect(getProperty(APPLICATION_BASE_URL));
 		} else {
 			echo();
 			invalid(EDIT_USER_TPL);
 		}
 	}
 
 	/**
 	 * AJAX function to search user
 	 */
 	public void getSearchUser() {
 		this.out("result", userManager.getUsersStartingWith(in("term")));
 		jsonSuccess();
 	}
 
 	/**
 	 * AJAX function to search users
 	 */
	public void getSearchUsers() {
 		// TODO validate
 		String[] users = in("users", strArray(","));
 		ArrayList<String> validUsers = new ArrayList<String>();
 		for (String s : users) {
 			if (s != null && !s.isEmpty()) {
 				validUsers.add(s);
 			}
 		}
 		if (!validUsers.isEmpty()) {
 			this.out("result", userManager.getUsersIn(validUsers.toArray(new String[validUsers.size()])));
 			jsonSuccess();
 		} else {
 			jsonInvalid();
 		}
 	}
 
 }
