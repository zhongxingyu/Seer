 package se.spacejens.gagror.view.spring;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import se.spacejens.gagror.LogAware;
 import se.spacejens.gagror.controller.LoginFailedException;
 import se.spacejens.gagror.controller.MayNotBeLoggedInException;
 import se.spacejens.gagror.controller.NotLoggedInException;
 import se.spacejens.gagror.controller.RequestContext;
 import se.spacejens.gagror.controller.ServiceCommunicationException;
 import se.spacejens.gagror.view.ViewSupport;
 
 /**
  * Superclass for all Spring controllers, providing shared functionality.
  * 
  * @author spacejens
  */
 public abstract class SpringViewSupport extends ViewSupport {
 
 	/**
 	 * Get an error view, without redirecting, assuming the user is logged in.
 	 * 
 	 * @param headline
 	 *            The headline displayed.
 	 * @param message
 	 *            The message below the headline.
 	 * @return A view to the error page, with a link to the dashboard.
 	 */
 	protected ModelAndView getErrorView(final String headline, final String message) {
 		final ModelAndView mav = new ModelAndView("error");
 		mav.getModel().put("headline", headline);
 		mav.getModel().put("message", message);
 		return mav;
 	}
 
 	/**
 	 * A unit of work that doesn't care if the user is logged in or not.
 	 * 
 	 * @author spacejens
 	 */
 	protected abstract class Work implements LogAware {
 
 		@Override
 		public Logger getLog() {
 			return SpringViewSupport.this.getLog();
 		}
 
 		/**
 		 * Process this work, handling failed logins appropriately.
 		 * 
 		 * @param request
 		 *            HTTP request.
 		 * @return A view as controlled by implementation, or redirect to login
 		 *         failed page if login failed.
 		 */
 		public final ModelAndView process(final HttpServletRequest request) {
 			final RequestContext rc = SpringViewSupport.this.getContext(request);
 			try {
 				return this.doWork(rc);
 			} catch (final LoginFailedException e) {
 				// Remove session login credentials, go to login failed page
 				this.getLog().debug("Login failed, removing login credentials from session for user {}", rc.getUsername());
 				SpringViewSupport.this.setLoggedInUser(null, request.getSession());
 				final ModelAndView mav = new ModelAndView("login");
 				mav.getModel().put("headline", "Login Failed");
 				mav.getModel().put("message", "Try again");
 				// Form and bindings added automatically by framework
 				return mav;
 			} catch (final ServiceCommunicationException e) {
 				// Go to internal error page
 				return SpringViewSupport.this.getErrorView("Internal Service Error", "No changes were made. This error has been logged.");
			} catch (final RuntimeException e) {
				this.getLog().error("Runtime exception caught", e);
				// Go to internal error page
				return SpringViewSupport.this.getErrorView("Internal Error", "No changes were made. This error has been logged.");
 			}
 		}
 
 		/**
 		 * Perform work without caring if the user is logged in or not.
 		 * 
 		 * @param rc
 		 *            The request context information.
 		 * @return A view as controlled by implementation.
 		 * @throws LoginFailedException
 		 *             If a performed login failed.
 		 * @throws ServiceCommunicationException
 		 *             If communication with a service failed.
 		 */
 		protected abstract ModelAndView doWork(final RequestContext rc) throws LoginFailedException, ServiceCommunicationException;
 	}
 
 	/**
 	 * A unit of work that requires that a user is logged in.
 	 * 
 	 * @author spacejens
 	 */
 	protected abstract class WorkLoggedIn extends Work {
 
 		@Override
 		protected final ModelAndView doWork(final RequestContext rc) throws LoginFailedException, ServiceCommunicationException {
 			try {
 				if (!rc.isContainingLoginInformation()) {
 					throw new NotLoggedInException();
 				}
 				return this.doWorkLoggedIn(rc);
 			} catch (final NotLoggedInException e) {
 				// Go to not logged in error page
 				return new ModelAndView(new RedirectView(rc.getContextPath() + SpringRequestMappings.PUBLIC
 						+ SpringRequestMappings.PUBLIC_NOTLOGGEDIN));
 			}
 		}
 
 		/**
 		 * Perform work that assumes that a user is logged in.
 		 * 
 		 * @param rc
 		 *            The request context information with login information,
 		 *            see {@link RequestContext#isContainingLoginInformation()}.
 		 * @return A view as controlled by implementation.
 		 * @throws NotLoggedInException
 		 *             If the user was not logged in.
 		 * @throws LoginFailedException
 		 *             If a performed login failed.
 		 * @throws ServiceCommunicationException
 		 *             If communication with a service failed.
 		 */
 		protected abstract ModelAndView doWorkLoggedIn(final RequestContext rc) throws NotLoggedInException, LoginFailedException,
 				ServiceCommunicationException;
 	}
 
 	/**
 	 * A unit of work that requires that no user is logged in.
 	 * 
 	 * @author spacejens
 	 */
 	protected abstract class WorkNotLoggedIn extends Work {
 
 		@Override
 		protected final ModelAndView doWork(final RequestContext rc) throws LoginFailedException, ServiceCommunicationException {
 			try {
 				if (rc.isContainingLoginInformation()) {
 					throw new MayNotBeLoggedInException();
 				}
 				return this.doWorkNotLoggedIn(rc);
 			} catch (final MayNotBeLoggedInException e) {
 				// Go to may not be logged in error page
 				return SpringViewSupport.this.getErrorView("May Not Be Logged In", "This action cannot be performed by logged in users.");
 			}
 		}
 
 		/**
 		 * Perform work that assumes that no user is logged in.
 		 * 
 		 * @param rc
 		 *            The request context information without login information,
 		 *            see {@link RequestContext#isContainingLoginInformation()}.
 		 * @return A view as controlled by implementation.
 		 * @throws LoginFailedException
 		 *             If a performed login failed.
 		 * @throws MayNotBeLoggedInException
 		 *             If a user was somehow logged in.
 		 * @throws ServiceCommunicationException
 		 *             If communication with a service failed.
 		 */
 		protected abstract ModelAndView doWorkNotLoggedIn(final RequestContext rc) throws LoginFailedException, MayNotBeLoggedInException,
 				ServiceCommunicationException;
 	}
 }
