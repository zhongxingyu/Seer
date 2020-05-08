 /**
  * 
  */
 package pl.psnc.dl.wf4ever.webapp.pages;
 
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.request.http.handler.RedirectRequestHandler;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 
 import pl.psnc.dl.wf4ever.webapp.model.AuthCodeData;
 import pl.psnc.dl.wf4ever.webapp.model.DlibraUser;
 import pl.psnc.dl.wf4ever.webapp.model.OAuthClient;
 import pl.psnc.dl.wf4ever.webapp.services.DlibraService;
 import pl.psnc.dl.wf4ever.webapp.services.HibernateService;
 
 /**
  * @author Piotr Hołubowicz
  * 
  * This is the OAuth 2.0 authorization endpoint.
  */
 public class OAuthAuthorizationEndpointPage
 	extends TemplatePage
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3793214124123802219L;
 
 	private static final Logger log = Logger
 			.getLogger(OAuthAuthorizationEndpointPage.class);
 
 	private OAuthClient client;
 
 	private String state;
 
 	private String providedRedirectURI;
 
 
 	public OAuthAuthorizationEndpointPage(PageParameters pageParameters)
 	{
 		super(pageParameters);
 		if (willBeRedirected)
 			return;
 		if (pageParameters.get("response_type").isNull()) {
 			error("Missing response type.");
 		}
 		else {
 			String responseType = pageParameters.get("response_type")
 					.toString();
 			if (responseType.equals("token") || responseType.equals("code")) {
 				this.client = processImplicitGrantOrAuthCodeFlow(pageParameters);
				if (client != null) {
					content.add(new AuthorizeFragment("entry", "validRequest",
							content, client, responseType));
				}
				else {
					content.add(new Fragment("entry", "invalidRequest", content));
				}
 			}
 			else {
 				error(String.format("Unknown response type: %s.", responseType));
 				content.add(new Fragment("entry", "invalidRequest", content));
 			}
 		}
 	}
 
 
 	/**
 	 * @param pageParameters
 	 * @return 
 	 */
 	private OAuthClient processImplicitGrantOrAuthCodeFlow(
 			PageParameters pageParameters)
 	{
 		if (pageParameters.get("client_id").isNull()) {
 			error("Missing client id.");
 		}
 		else {
 			String clientId = pageParameters.get("client_id").toString();
 			try {
 				OAuthClient client = DlibraService.getClient(clientId);
 				if (pageParameters.get("redirect_uri").isNull()) {
 					log.warn("Missing redirect URI.");
 				}
 				else {
 					providedRedirectURI = pageParameters.get("redirect_uri")
 							.toString();
 					if (!client.getRedirectionURI().equals(providedRedirectURI)) {
 						error("Redirect URI does not match client redirect URI.");
 						return null;
 					}
 				}
 				if (!pageParameters.get("state").isNull()) {
 					state = pageParameters.get("state").toString();
 				}
 				return client;
 			}
 			catch (Exception e) {
 				error("Invalid client id: " + e.getMessage() + ".");
 			}
 		}
 		return null;
 	}
 
 	private class AuthorizeFragment
 		extends Fragment
 	{
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = -3040124186474465047L;
 
 
 		@SuppressWarnings("serial")
 		public AuthorizeFragment(String id, String markupId,
 				MarkupContainer markupProvider, final OAuthClient client,
 				final String responseType)
 		{
 			super(id, markupId, markupProvider);
 			Form< ? > form = new Form<Void>("form");
 			add(form);
 			form.add(new Label("name", client.getName()));
 			form.add(new Button("authorize") {
 
 				@Override
 				public void onSubmit()
 				{
 					super.onSubmit();
 					try {
 						String url;
 						if (responseType.equals("token")) {
 							url = prepareTokenResponse(client);
 						}
 						else {
 							url = prepareAuthCodeResponse(client);
 						}
 						getRequestCycle().scheduleRequestHandlerAfterCurrent(
 							new RedirectRequestHandler(url));
 					}
 					catch (Exception e) {
 						error(e);
 						log.error(e);
 					}
 				}
 
 			});
 			form.add(new Button("reject") {
 
 				@Override
 				public void onSubmit()
 				{
 					super.onSubmit();
 					String url = prepareDeniedResponse(client);
 					getRequestCycle().scheduleRequestHandlerAfterCurrent(
 						new RedirectRequestHandler(url));
 				}
 			});
 
 		}
 
 
 		private String prepareTokenResponse(OAuthClient client)
 			throws Exception
 		{
 			DlibraUser user = getDlibraUserModel();
 			String token = DlibraService.getAccessToken(user.getUsername(),
 				client.getClientId());
 			String url = client.getRedirectionURI() + "#";
 			url += ("access_token=" + token);
 			url += "&token_type=bearer";
 			if (state != null) {
 				url += ("&state=" + state);
 			}
 			return url;
 		}
 
 
 		private String prepareAuthCodeResponse(OAuthClient client)
 			throws Exception
 		{
 			DlibraUser user = getDlibraUserModel();
 			String code = UUID.randomUUID().toString().replaceAll("-", "")
 					.substring(0, 20);
 
 			AuthCodeData data = new AuthCodeData(code, providedRedirectURI,
 					user.getUsername(), client.getClientId());
 			HibernateService.storeCode(data);
 			String url = client.getRedirectionURI() + "?";
 			url += ("code=" + code);
 			if (state != null) {
 				url += ("&state=" + state);
 			}
 			return url;
 		}
 
 
 		private String prepareDeniedResponse(OAuthClient client)
 		{
 			String url = client.getRedirectionURI() + "#";
 			url += "error=access_denied";
 			if (state != null) {
 				url += ("&state=" + state);
 			}
 			return url;
 		}
 
 	}
 
 }
