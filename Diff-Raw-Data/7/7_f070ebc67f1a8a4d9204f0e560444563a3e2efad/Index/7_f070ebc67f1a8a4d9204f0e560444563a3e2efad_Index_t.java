 package finki.ukim.mk.pages;
 
 import java.util.List;
 
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.PageRenderLinkSource;
 import org.apache.tapestry5.services.Response;
 import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
 
 import twitter4j.Query;
 import twitter4j.QueryResult;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.User;
 import twitter4j.auth.AccessToken;
 import finki.ukim.mk.pages.base.TwitterBasePage;
 
 /**
  * Start page of application twitter.
  */
 public class Index extends TwitterBasePage {
 	@Property
 	private Status status;
 	@Inject
 	private AjaxResponseRenderer ajaxResponseRenderer;
 
 	@InjectComponent
 	private Zone userDetailsZone, statusesZone;
 
 	@Property
 	private User user;
 
 	@Property
 	private String searchTerm;
 
 	@Inject
 	private Response response;
 
 	@Inject
 	private PageRenderLinkSource pageRenderLinkSource;
 
 	private Twitter twitter = TwitterFactory.getSingleton();
 
 	void setupRender() {
		if (getTwitterUser() != null)
			twitter.setOAuthAccessToken(new AccessToken(getTwitterUser()
					.getToken(), getTwitterUser().getTokenSecret()));
 	}
 
 	public void onUpdateStatus() throws TwitterException {
 		List<Status> statuses = twitter.getHomeTimeline();
 
 		for (Status status : statuses) {
 			System.out.println(status.getUser().getName() + ":"
 					+ status.getText());
 		}
 	}
 
 	public List<Status> getStatuses() throws TwitterException {
 		return TwitterFactory.getSingleton().getHomeTimeline();
 	}
 
 	@OnEvent(value = "viewUserDetails")
 	public void getUserDetails(Long id) throws TwitterException {
 		this.user = twitter.showUser(id);
 		ajaxResponseRenderer.addRender("userDetailsZone", userDetailsZone);
 	}
 
 	@OnEvent(value = EventConstants.SUCCESS, component = "searchForm")
 	void getResults() throws TwitterException {
 
 		Twitter twitter = TwitterFactory.getSingleton();
 		Query query = new Query(searchTerm);
 		QueryResult queryResult = twitter.search(query);
 
 		// TODO carry on here
 		queryResult.getTweets();
 
 		ajaxResponseRenderer.addRender("statusesZone", statusesZone);
 
 	}
 }
