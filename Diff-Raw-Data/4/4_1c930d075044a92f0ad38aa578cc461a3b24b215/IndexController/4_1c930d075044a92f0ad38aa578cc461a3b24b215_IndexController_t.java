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
 package br.octahedron.figgo;
 
 import static br.octahedron.cotopaxi.CotopaxiProperty.APPLICATION_BASE_URL;
 import static br.octahedron.cotopaxi.CotopaxiProperty.getProperty;
 import static br.octahedron.cotopaxi.validation.Rule.Builder.required;
 import br.octahedron.cotopaxi.auth.AuthenticationRequired;
 import br.octahedron.cotopaxi.controller.Controller;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceManager;
 import br.octahedron.cotopaxi.datastore.namespace.NamespaceRequired;
 import br.octahedron.cotopaxi.inject.Inject;
 import br.octahedron.cotopaxi.validation.Validator;
 import br.octahedron.figgo.OnlyForNamespaceControllerInterceptor.OnlyForNamespace;
import br.octahedron.figgo.modules.DataDoesNotExistsException;
 import br.octahedron.figgo.modules.authorization.manager.AuthorizationManager;
 import br.octahedron.figgo.modules.domain.manager.ConfigurationManager;
 import br.octahedron.figgo.util.Mailer;
 
 /**
  * 
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class IndexController extends Controller {
 
 	private static final String INDEX_TPL = "index.vm";
 	private static final String CONTACT_TPL = "contact.vm";
 	private static final String ABOUT_TPL = "about.vm";
 	private static final String ERROR_TPL = "error.vm";
 	private static final String DOMAIN_INDEX_TPL = "domain/index.vm";
 	private static final String DOMAIN_PUBLIC_INDEX_TPL = "domain/public_index.vm";
 
 	@Inject
 	private ConfigurationManager configurationManager;
 	@Inject
 	private AuthorizationManager authorizationManager;
 	@Inject
 	private NamespaceManager namespaceManager;
 
 	public void setNamespaceManager(NamespaceManager namespaceManager) {
 		this.namespaceManager = namespaceManager;
 	}
 
 	public void setConfigurationManager(ConfigurationManager configurationManager) {
 		this.configurationManager = configurationManager;
 	}
 
 	public void setAuthorizationManager(AuthorizationManager authorizationManager) {
 		this.authorizationManager = authorizationManager;
 	}
 
 	/**
 	 * Shows initial land page or redirect user to dashboard/domain page
 	 */
 	public void getIndex() {
 		String username = this.currentUser();
 		boolean userLogged = (username != null);
 
 		if (fullRequestedUrl().equalsIgnoreCase(getProperty(APPLICATION_BASE_URL))) {
 			// user accessing the raw url (www), redirects it to dash board
 			if (!userLogged) {
 				// user not logged, show the initial land page
 				this.success(INDEX_TPL);
 			} else {
 				this.redirect("/dashboard");
 			}
 		} else {
 			this.forward("DomainIndex");
 		}
 	}
 
 	@AuthenticationRequired
 	@NamespaceRequired
 	@OnlyForNamespace
 	public void getDomainIndex() {
 		try {
 			out("domain", this.configurationManager.getDomainConfiguration());
 			namespaceManager.changeToGlobalNamespace();
 			boolean hasPermission = this.authorizationManager.getActiveUserDomains(this.currentUser()).contains(this.subDomain());
 			if (hasPermission) {
 				success(DOMAIN_INDEX_TPL);
 			} else {
 				success(DOMAIN_PUBLIC_INDEX_TPL);
 			}
		} catch (DataDoesNotExistsException e) {
			this.notFound();
 		} finally {
 			namespaceManager.changeToPreviousNamespace();
 		}
 
 	}
 
 	/**
 	 * Just to force user to login. If user already logged, redirect to main page
 	 */
 	@AuthenticationRequired
 	public void getLogin() {
 		redirect("/");
 	}
 
 	public void getAbout() {
 		success(ABOUT_TPL);
 	}
 
 	public void getContact() {
 		success(CONTACT_TPL);
 	}
 
 	public void postContact() {
 		Validator validator = ContactValidator.getValidator();
 		if (validator.isValid()) {
 			Mailer.send(in("name"), in("from"), in("subject"), in("message"));
 			this.out("notice", "MESSAGE_SENT");
 			this.success(CONTACT_TPL);
 		} else {
 			this.echo();
 			this.invalid(CONTACT_TPL);
 		}
 	}
 
 	/**
 	 * @author vitoravelino
 	 */
 	private static class ContactValidator {
 
 		private static Validator validator;
 
 		protected static synchronized Validator getValidator() {
 			if (validator == null) {
 				validator = new Validator();
 				validator.add("name", required("REQUIRED_NAME"));
 				validator.add("from", required("REQUIRED_EMAIL"));
 				validator.add("subject", required("REQUIRED_SUBJECT"));
 				validator.add("message", required("REQUIRED_MESSAGE_TYPE"));
 			}
 			return validator;
 		}
 	}
 	
 	public void postError() {
 		success(ERROR_TPL);
 	}
 
 }
