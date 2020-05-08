 package org.freenetproject.plugin.dvcs_webui.ui.web;
 
 import freenet.client.HighLevelSimpleClient;
 import freenet.clients.http.LinkEnabledCallback;
 import freenet.clients.http.PageNode;
 import freenet.clients.http.SessionManager;
 import freenet.clients.http.Toadlet;
 import freenet.clients.http.ToadletContext;
 import freenet.clients.http.ToadletContextClosedException;
 import freenet.support.api.HTTPRequest;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.freenetproject.plugin.dvcs_webui.main.L10n;
 import org.freenetproject.plugin.dvcs_webui.main.Plugin;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Properties;
 
 /**
  * Uses Velocity templates to render a Toadlet.
  */
 public abstract class VelocityToadlet extends Toadlet implements LinkEnabledCallback {
 
 	private final SessionManager sessionManager;
 
 	/**
 	 * URI of the WoT login page. Redirected to when no identity has a session.
 	 */
 	private final URI wotLogin;
 
 	private final String path;
 	private final String templateName;
 	private final String title;
 	private final L10n l10n;
 
 	/**
 	 * @param client       required by Toadlet.
 	 * @param l10n         used to localize page and title.
 	 * @param sessionManager used to check for WoT login.
 	 * @param templateName template filename to use.
 	 * @param path         path to register to.
 	 * @param title        page title localization key.
 	 */
 	VelocityToadlet(HighLevelSimpleClient client, L10n l10n, SessionManager sessionManager, String templateName,
 	                String path, String title) {
 		super(client);
 
 		this.sessionManager = sessionManager;
 
 		try {
 			wotLogin = new URI(null, null, "/WebOfTrust/LogIn/", "redirect-target=" + path, null);
 		} catch (URISyntaxException e) {
 			throw new RuntimeException("Programming error: invalid syntax in wotLogin URI.", e);
 		}
 
 		this.l10n = l10n;
 		this.path = path;
 		this.templateName = templateName;
 		this.title = l10n.get(title);
 
 		// Templates are stored in jars on the classpath.
 		Properties properties = new Properties();
 		properties.setProperty("resource.loader", "class");
 		properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader" +
 				".ClasspathResourceLoader");
 
 		Velocity.init(properties);
 	}
 
 	@Override
 	public String path() {
 		return path;
 	}
 
 	/**
 	 * By default pages are only enabled if a WoT identity is logged in.
 	 */
 	@Override
 	public boolean isEnabled(ToadletContext ctx) {
 		return sessionManager.sessionExists(ctx);
 	}
 
 	/**
 	 * Adds default things to the context, and redirects to the WoT login page if this page is not enabled.<br/>
 	 * Things added to the context.
 	 * <ul>
 	 *     <li>Query parameters</li>
 	 *     <li>t can be queried for localization with .get()</li>
 	 *     <li>local-id has the identity ID of the logged-in identity.</li>
 	 * </ul>
 	 */
 	public final void handleMethodGET(URI uri, HTTPRequest request, ToadletContext ctx) throws
 			ToadletContextClosedException, IOException {
 
 		// Some pages may want to always be enabled so that they show in the menu, but they still need login.
 		// TODO: Configurable?
 		if (!sessionManager.sessionExists(ctx)) {
 			writeTemporaryRedirect(ctx, "Not logged in", wotLogin.toString());
 			return;
 		}
 
 		VelocityContext context = new VelocityContext();
 		for (String key : request.getParameterNames()) {
 			context.put(key, request.getParam(key));
 		}
 
 		context.put("t", l10n);
 		context.put("local-id", sessionManager.useSession(ctx).getUserID());
 
 		onGet(context);
 
 		StringWriter writer = new StringWriter();
 		Velocity.mergeTemplate(getTemplate(), Plugin.ENCODING, context, writer);
 
 		PageNode pageNode = ctx.getPageMaker().getPageNode(title, ctx);
 		pageNode.content.addChild("%", writer.toString());
 
 		writeReply(ctx, 200, "text/html", "OK", pageNode.outer.generate());
 	}
 
 	// TODO: Is it appropriate to react to POSTs in this way?
 	public final void handleMethodPOST(URI uri, HTTPRequest request, ToadletContext ctx) throws
 			ToadletContextClosedException, IOException {
 		onPost(request);
 		handleMethodGET(uri, request, ctx);
 	}
 
 	/**
 	 * @return templateName along with the qualifiers for the classpath loader to find it.
 	 */
 	String getTemplate() {
 		return "/templates/" + templateName;
 	}
 
 	/**
 	 * Put additional variables, properties, and methods in the context as required. Called before rendering the
 	 * template with the context.
 	 *
 	 * @param context context to modify
 	 */
 	abstract void onGet(VelocityContext context);
 
 	/**
 	 * Modify state as desired in response to a POST request. Called before rendering the page as in response to a GET.
 	 * The VelocityToadlet implementation does nothing.
 	 * @param request describes POST parameters.
 	 */
 	// TODO: Would it make sense to include the URI as well?
 	void onPost(HTTPRequest request) {
 	}
 }
