 package xdi2.connector.personal.contributor;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import xdi2.connector.personal.api.PersonalApi;
 import xdi2.connector.personal.mapping.PersonalMapping;
 import xdi2.connector.personal.util.GraphUtil;
 import xdi2.core.ContextNode;
 import xdi2.core.Graph;
 import xdi2.core.xri3.impl.XRI3Segment;
 import xdi2.messaging.GetOperation;
 import xdi2.messaging.MessageEnvelope;
 import xdi2.messaging.MessageResult;
 import xdi2.messaging.exceptions.Xdi2MessagingException;
 import xdi2.messaging.target.ExecutionContext;
 import xdi2.messaging.target.Prototype;
 import xdi2.messaging.target.contributor.AbstractContributor;
 import xdi2.messaging.target.contributor.ContributorXri;
 import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
 import xdi2.messaging.target.interceptor.MessageEnvelopeInterceptor;
 
 @ContributorXri(addresses={"(https://personal.com)"})
public class PersonalContributor extends AbstractContributor implements MessageEnvelopeInterceptor, Prototype<PersonalContributor> {
 
 	private static final Logger log = LoggerFactory.getLogger(PersonalContributor.class);
 
 	private Graph tokenGraph;
 	private PersonalApi personalApi;
 	private PersonalMapping personalMapping;
 
 	public PersonalContributor() {
 
 		super();
 
 		this.getContributors().addContributor(new PersonalUserContributor());
 	}
 
 	/*
 	 * Prototype
 	 */
 
 	@Override
 	public PersonalContributor instanceFor(PrototypingContext prototypingContext) throws Xdi2MessagingException {
 
 		// create new contributor
 
 		PersonalContributor contributor = new PersonalContributor();
 
		// set the token graph

		if (this.tokenGraph == null && prototypingContext.getMessagingTarget() instanceof GraphMessagingTarget) {

			contributor.setTokenGraph(((GraphMessagingTarget) prototypingContext.getMessagingTarget()).getGraph());
		}

 		// set api and mapping
 
 		contributor.setPersonalApi(this.getPersonalApi());
 		contributor.setPersonalMapping(this.getPersonalMapping());
 
 		// done
 
 		return contributor;
 	}
 
 	/*
 	 * MessageEnvelopeInterceptor
 	 */
 
 	@Override
 	public boolean before(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {
 
 		PersonalContributorExecutionContext.resetUsers(executionContext);
 
 		return false;
 	}
 
 	@Override
 	public boolean after(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {
 
 		return false;
 	}
 
 	@Override
 	public void exception(MessageEnvelope messageEnvelope, MessageResult messageResult, ExecutionContext executionContext, Exception ex) {
 
 	}
 
 	/*
 	 * Sub-Contributors
 	 */
 
 	@ContributorXri(addresses={"($)"})
 	private class PersonalUserContributor extends AbstractContributor {
 
 		private PersonalUserContributor() {
 
 			super();
 
 			this.getContributors().addContributor(new PersonalGemFieldContributor());
 		}
 	}
 
 	@ContributorXri(addresses={"($)($)"})
 	private class PersonalGemFieldContributor extends AbstractContributor {
 
 		private PersonalGemFieldContributor() {
 
 			super();
 		}
 
 		@Override
 		public boolean getContext(XRI3Segment[] contributorXris, XRI3Segment relativeContextNodeXri, XRI3Segment contextNodeXri, GetOperation operation, MessageResult messageResult, ExecutionContext executionContext) throws Xdi2MessagingException {
 
 			XRI3Segment personalContextXri = contributorXris[contributorXris.length - 3];
 			XRI3Segment userXri = contributorXris[contributorXris.length - 2];
 			XRI3Segment personalDataXri = contributorXris[contributorXris.length - 1];
 
 			log.debug("personalContextXri: " + personalContextXri + ", userXri: " + userXri + ", personalDataXri: " + personalDataXri);
 
 			// retrieve the Personal value
 
 			String personalValue = null;
 
 			try {
 
 				String personalGemIdentifier = PersonalContributor.this.personalMapping.personalDataXriToPersonalGemIdentifier(personalDataXri);
 				String personalFieldIdentifier = PersonalContributor.this.personalMapping.personalDataXriToPersonalFieldIdentifier(personalDataXri);
 				if (personalGemIdentifier == null) return false;
 				if (personalFieldIdentifier == null) return false;
 
 				String accessToken = GraphUtil.retrieveAccessToken(PersonalContributor.this.getTokenGraph(), userXri);
 				if (accessToken == null) throw new Exception("No access token.");
 
 				JSONObject user = PersonalContributor.this.retrieveUser(executionContext, accessToken);
 				if (user == null) throw new Exception("No user.");
 				if (! user.has(personalGemIdentifier)) return false;
 
 				JSONObject gem = user.getJSONObject(personalGemIdentifier);
 				if (! gem.has(personalFieldIdentifier)) return false;
 
 				personalValue = gem.getString(personalFieldIdentifier);
 			} catch (Exception ex) {
 
 				throw new Xdi2MessagingException("Cannot load user data: " + ex.getMessage(), ex, null);
 			}
 
 			// add the Personal value to the response
 
 			if (personalValue != null) {
 
 				ContextNode contextNode = messageResult.getGraph().findContextNode(contextNodeXri, true);
 				contextNode.createLiteral(personalValue);
 			}
 
 			// done
 
 			return true;
 		}
 	}
 
 	/*
 	 * Helper methods
 	 */
 
 	private JSONObject retrieveUser(ExecutionContext executionContext, String accessToken) throws IOException, JSONException {
 
 		JSONObject user = PersonalContributorExecutionContext.getUser(executionContext, accessToken);
 
 		if (user == null) {
 
 			user = this.personalApi.getUser(accessToken);
 			PersonalContributorExecutionContext.putUser(executionContext, accessToken, user);
 		}
 
 		return user;
 	}
 
 	/*
 	 * Getters and setters
 	 */
 
 	public Graph getTokenGraph() {
 
 		return this.tokenGraph;
 	}
 
 	public void setTokenGraph(Graph tokenGraph) {
 
 		this.tokenGraph = tokenGraph;
 	}
 
 	public PersonalApi getPersonalApi() {
 
 		return this.personalApi;
 	}
 
 	public void setPersonalApi(PersonalApi personalApi) {
 
 		this.personalApi = personalApi;
 	}
 
 	public PersonalMapping getPersonalMapping() {
 
 		return this.personalMapping;
 	}
 
 	public void setPersonalMapping(PersonalMapping personalMapping) {
 
 		this.personalMapping = personalMapping;
 	}
 }
