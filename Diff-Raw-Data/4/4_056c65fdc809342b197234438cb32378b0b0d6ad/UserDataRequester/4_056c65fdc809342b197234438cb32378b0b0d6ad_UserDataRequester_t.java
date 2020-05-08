 package br.ufmg.dcc.vod.spiderpig.jobs.youtube.users.subs;
 
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResult;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.PayloadBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.YTConstants;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.api.client.googleapis.json.GoogleJsonError;
 import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
 import com.google.api.client.googleapis.json.GoogleJsonResponseException;
 import com.google.api.services.youtube.YouTube;
 import com.google.api.services.youtube.model.Channel;
 import com.google.api.services.youtube.model.ChannelListResponse;
 import com.google.api.services.youtube.model.Subscription;
 import com.google.api.services.youtube.model.SubscriptionListResponse;
 import com.google.common.collect.Sets;
 
 public class UserDataRequester extends ConfigurableRequester {
 
 	private static final String SUB_DETAILS = "id,snippet,contentDetails," +
 			"subscriberSnippet";
 	private static final String USER_DETAILS = "id,snippet,brandingSettings," +
 			"contentDetails,invideoPromotion,statistics,topicDetails";
 	private final Set<String> SUB_ORDERS = 
 			Sets.newHashSet("relevance", "unread", "alphabetical");
 	
 	private static final int _403_QUOTA_ERR = 403;
 	
 	private YouTube youtube;
 	private String apiKey;
 	
 	@Override
 	public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
 		CrawlResultBuilder crawlResultBuilder = new CrawlResultBuilder(crawlID);
 		try {
 			String userId = crawlID.getId();
 			Channel userChannel = getUserChannel(userId);
 			
 			PayloadBuilder payloadBuilder = new PayloadBuilder();
 			payloadBuilder.addPayload("userId-data-" + userId, 
 					userChannel.toPrettyString().getBytes());
 			
 			List<CrawlID> links = new ArrayList<>();
 			
 			for (String order : SUB_ORDERS) {
 				List<Subscription> subs = getLinks(userChannel.getId(), order);
 				for (Subscription sub : subs) {
 					String subId = sub.getSubscriberSnippet().getChannelId();
 					CrawlID toFollow = CrawlID.newBuilder().
 							setId(subId).
 							build();
 					payloadBuilder.addPayload(
 							"userId-sub-" +userId + "-" + subId, 
 							sub.toPrettyString().getBytes());
 					links.add(toFollow);
 				}
 			}
 			
 			Map<String, byte[]> filesToSave = payloadBuilder.build();
 			return crawlResultBuilder.buildOK(filesToSave, links);
 		} catch (QuotaException e) {
 			throw e;
 		} catch (IOException e) {
 			return crawlResultBuilder.buildNonQuotaError(
 					new UnableToCrawlException(e));
 		}
 	}
 	
 	private List<Subscription> getLinks(String userID, String order) 
 			throws IOException {
 		YouTube.Subscriptions.List subsList = 
 				youtube.subscriptions().list(SUB_DETAILS);
 		
 		subsList.setKey(this.apiKey);
 		subsList.setMaxResults(50l);
 		subsList.setChannelId(userID);
 		subsList.setOrder(order);
 		
 		List<Subscription> returnValue = new ArrayList<>();
 		String nextPageToken;
 		try {
 			SubscriptionListResponse response = subsList.execute();
 			do {
 				List<Subscription> items = response.getItems();
 				returnValue.addAll(items);
 				
 				nextPageToken = response.getNextPageToken();
 				if (nextPageToken != null) {
 					subsList.setPageToken(nextPageToken);
 					response = subsList.execute();
 				}
 			} while (nextPageToken != null);
 			
 		} catch (GoogleJsonResponseException e) {
 			List<ErrorInfo> errors = e.getDetails().getErrors();
 			if (_403_QUOTA_ERR == e.getStatusCode()) {
 				for (ErrorInfo ei : errors) {
 					if (ei.getReason().equals("dailyLimitExceeded")) {
 						throw new QuotaException(e);
					} else if (ei.getReason().equals("subscriptionForbidden")) {
						return Collections.emptyList();
 					}
 				}
 			}
 			throw e;
 		}
 		
 		return returnValue;
 	}
 
 	private Channel getUserChannel(String userID)
 			throws IOException, QuotaException, GoogleJsonResponseException {
 		
 		YouTube.Channels.List userList = youtube.channels().list(USER_DETAILS);
 		userList.setKey(this.apiKey);
 		userList.setMaxResults(50l);
 		userList.setId(userID);
 		
 		try {
 			ChannelListResponse response = userList.execute();
 			List<Channel> items = response.getItems();
 			if (items.isEmpty()) {
 				throw new IOException("User not found " + userID);
 			} else {
 				return items.get(0);
 			}
 		} catch (GoogleJsonResponseException e) {
 			GoogleJsonError details = e.getDetails();
 			
 			if (details != null) {
 				Object statusCode = details.get("code");
 				
 				if (statusCode.equals(_403_QUOTA_ERR)) {
 					throw new QuotaException(e);
 				} else {
 					throw e;
 				}
 			} else {
 				throw e;
 			}
 		}
 	}
 
 
 	@Override
 	public Set<String> getRequiredParameters() {
 		return Sets.newHashSet(YTConstants.API_KEY);
 	}
 
 
 	@Override
 	public Requester realConfigurate(Configuration configuration)
 			throws Exception {
 		this.apiKey = configuration.getString(YTConstants.API_KEY);
 		this.youtube = YTConstants.buildYoutubeService();
 		return this;
 	}
 }
