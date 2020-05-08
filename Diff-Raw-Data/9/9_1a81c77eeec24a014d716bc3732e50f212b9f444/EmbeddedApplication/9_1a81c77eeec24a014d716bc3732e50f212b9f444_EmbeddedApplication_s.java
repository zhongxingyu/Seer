 /*
  * Copyright 2010 Instituto Superior Tecnico
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the vaadin-framework.
  *
 *   The vaadin-framework Infrastructure is free software: you can 
 *   redistribute it and/or modify it under the terms of the GNU Lesser General 
 *   Public License as published by the Free Software Foundation, either version 
  *   3 of the License, or (at your option) any later version.*
  *
  *   vaadin-framework is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with vaadin-framework. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package pt.ist.vaadinframework;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import pt.ist.vaadinframework.fragment.FragmentQuery;
 import pt.ist.vaadinframework.ui.EmbeddedComponentContainer;
 
 import com.vaadin.Application;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Window;
 
 /**
  * <p>
  * Application to manage components embedded in JSPs sharing the same session.
  * This application needs to be configured at startup with the mappings between
  * textual patterns and the {@link EmbeddedComponentContainer}'s that will be
  * attached to the embedded {@link Window}. On embedding a component the
  * hostItem application must set the {@link EmbeddedApplication#VAADIN_PARAM } to
  * a textual parameter that will be matched to the patterns specified, the first
  * pattern to match will define the {@link EmbeddedComponentContainer} that will
  * be instantiated. The parameter will also be sent to the newly instantiated
  * container.
  * </p>
  * 
  * <p>
  * For example: Lets say we have a component that displays information about a
  * Product given its ID. First add the pattern to the resolver by doing:
  * 
  * <pre>
  * {@code EmbeddedApplication.addResolutionPattern(Pattern.compile("product-(.*)"), ProductContainer.class);}
  * </pre>
  * 
  * In the hostItem application set the {@link EmbeddedApplication#VAADIN_PARAM }
  * like:
  * 
  * <pre>
  * {@code request.getSession().setAttribute(EmbeddedApplication.VAADIN_PARAM, "product-10042786");}
  * </pre>
  * 
  * As you defined a group in the pattern the group will be sent as an argument
  * to {@link EmbeddedComponentContainer#setArguments(String...)}.
  * </p>
  * 
  * To use this feature client applications have to do to following:
  * 
  * <li>
  * <ul>
  * Add EmbeddedApplication servlet to web.xml:
  * 
  * <pre>
  * {@code
  * <servlet>
  *   <servlet-name>VaadinEmbedded</servlet-name>
  *   <servlet-class>com.vaadin.terminal.gwt.server.ApplicationServlet</servlet-class>
  *   <init-param>
  *     <description>Vaadin Embedded Application class</description>
  *     <param-name>application</param-name>
  *     <param-value>pt.ist.vaadinframework.EmbeddedApplication</param-value>
  *   </init-param>
  * </servlet>
  * 
  * <servlet-mapping>
  *   <servlet-name>VaadinEmbedded</servlet-name>
  *     <url-pattern>/vaadin/*</url-pattern>
  * </servlet-mapping>
  * }
  * </pre>
  * 
  * </ul>
  * <ul>
  * 
  * Then redirect to a jsp with the following code:
  * 
  * <pre>
  * {@code
  * <script type="text/javascript">
  *   var vaadin = { vaadinConfigurations: { 'vaadin': {appUri:'<%= request.getContextPath() + "/vaadin" %>', pathInfo: '/', themeUri:'<%= request.getContextPath() + "/VAADIN/themes/default" %>'}}};
  * </script>
  * <script language='javascript' src='<%= request.getContextPath() + "/VAADIN/widgetsets/com.vaadin.terminal.gwt.DefaultWidgetSet/com.vaadin.terminal.gwt.DefaultWidgetSet.nocache.js" %>'></script>
  * <div id="vaadin"/>
  * }
  * </pre>
  * 
  * </ul>
  * </li>
  * 
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * @version 1.0
  */
 @SuppressWarnings("serial")
 public class EmbeddedApplication extends Application implements VaadinResourceConstants {
     private static final Map<Pattern, Class<? extends EmbeddedComponentContainer>> resolver = new HashMap<Pattern, Class<? extends EmbeddedComponentContainer>>();
     private static final Set<Class<? extends EmbeddedComponentContainer>> pages = new HashSet<Class<? extends EmbeddedComponentContainer>>();
 
     private static ApplicationErrorListener errorListener = null;
 
     @Override
     public void init() {
 	setTheme("reindeer");
 	setMainWindow(new EmbeddedWindow(pages));
     }
 
     public static void open(Application application, Class<? extends EmbeddedComponentContainer> clazz, String... args) {
 	((EmbeddedApplication) application).open(clazz, args);
     }
 
     public static void open(Application application, String fragment) {
 	((EmbeddedApplication) application).open(fragment);
     }
 
     public void open(Class<? extends EmbeddedComponentContainer> clazz, String... args) {
 	final FragmentQuery fragmentQuery = new FragmentQuery(clazz, args);
 	open(fragmentQuery.getQueryString());
     }
 
     public void open(String fragment) {
 	((EmbeddedWindow) getMainWindow()).open(fragment);
     }
 
     public void back() {
 	((EmbeddedWindow) getMainWindow()).back();
     }
 
     public static void back(Application application) {
 	((EmbeddedApplication) application).back();
     }
 
     /**
      * @see com.vaadin.Application#getWindow(java.lang.String)
      */
     @Override
     public Window getWindow(String name) {
 	// If the window is identified by name, we are good to go
 	Window window = super.getWindow(name);
 
 	// If not, we must create a new window for this new browser window/tab
 	if (window == null) {
 	    window = new EmbeddedWindow(pages);
 	    // Use the random name given by the framework to identify this
 	    // window in future
 	    window.setName(name);
	    // addWindow(window);
 
 	    // Move to the url to remember the name in the future
 	    // window.open(new ExternalResource(window.getURL()));
 	}
 	return window;
     }
 
     /**
      * Reloads current page with current arguments.
      */
     public void refresh() {
 	((EmbeddedWindow) getMainWindow()).refreshContent();
     }
 
     /**
      * Adds a new pattern to the resolver, the pattern can have groups that when
      * captured will be supplied to the corresponding
      * {@link EmbeddedComponentContainer} using
      * {@link EmbeddedComponentContainer#setArguments(String...)}.
      * 
      * @param pattern
      *            The compiled {@link Pattern} instance.
      * @param type
      *            The container that will be instantiated if the pattern matches
      *            the {@link EmbeddedApplication#VAADIN_PARAM}.
      */
     public static void addResolutionPattern(Pattern pattern, Class<? extends EmbeddedComponentContainer> type) {
 	resolver.put(pattern, type);
     }
 
     public static void addPage(Class<? extends EmbeddedComponentContainer> type) {
 	pages.add(type);
     }
 
     public static SystemMessages getSystemMessages() {
 	return new CustomizedSystemMessages() {
 	    @Override
 	    public String getSessionExpiredCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_SESSION_EXPIRED);
 	    }
 
 	    @Override
 	    public String getSessionExpiredMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_SESSION_EXPIRED);
 	    }
 
 	    @Override
 	    public String getCommunicationErrorCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_COMMUNICATION_ERROR);
 	    }
 
 	    @Override
 	    public String getCommunicationErrorMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_COMMUNICATION_ERROR);
 	    }
 
 	    @Override
 	    public String getAuthenticationErrorCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_AUTHENTICATION_ERROR);
 	    }
 
 	    @Override
 	    public String getAuthenticationErrorMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_AUTHENTICATION_ERROR);
 	    }
 
 	    @Override
 	    public String getInternalErrorCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_INTERNAL_ERROR);
 	    }
 
 	    @Override
 	    public String getInternalErrorMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_INTERNAL_ERROR);
 	    }
 
 	    @Override
 	    public String getOutOfSyncCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_OUTOFSYNC_ERROR);
 	    }
 
 	    @Override
 	    public String getOutOfSyncMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_OUTOFSYNC_ERROR);
 	    }
 
 	    @Override
 	    public String getCookiesDisabledCaption() {
 		return VaadinResources.getString(SYSTEM_TITLE_COOKIES_DISABLED_ERROR);
 	    }
 
 	    @Override
 	    public String getCookiesDisabledMessage() {
 		return VaadinResources.getString(SYSTEM_MESSAGE_COOKIES_DISABLED_ERROR);
 	    }
 	};
     }
 
     /**
      * @see com.vaadin.Application#terminalError(com.vaadin.terminal.Terminal.ErrorEvent)
      */
     @Override
     public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
 	if (errorListener != null) {
 	    errorListener.terminalError(event, this);
 	} else {
 	    super.terminalError(event);
 	}
     }
 
     public static class TerminalErrorWindow extends Window {
 	public TerminalErrorWindow(Throwable throwable) {
 	    setCaption(VaadinResources.getString(SYSTEM_TITLE_TERMINAL_ERROR));
 	    addComponent(new Label(VaadinResources.getString(SYSTEM_MESSAGE_TERMINAL_ERROR), Label.CONTENT_XHTML));
 	    setModal(true);
 	    getContent().setSizeUndefined();
 	    center();
 	}
     }
 
     public static void registerErrorListener(final ApplicationErrorListener errorListenerArg) {
 	errorListener = errorListenerArg;
     }
 
 }
