 package br.ufmg.dcc.vod.spiderpig.jobs.topsy;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 
import twitter4j.internal.org.json.JSONArray;
import twitter4j.internal.org.json.JSONException;
import twitter4j.internal.org.json.JSONObject;
 import br.ufmg.dcc.vod.spiderpig.common.URLGetter;
 import br.ufmg.dcc.vod.spiderpig.common.config.BuildException;
 import br.ufmg.dcc.vod.spiderpig.common.config.ConfigurableBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.PayloadsFactory;
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.UnableToCrawlException;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Worker.CrawlResult;
 
 public class Requester implements ConfigurableRequester {
 
 	private DefaultHttpClient httpClient;
     private URLGetter urlGetter;
 	
 	@Override
 	public void configurate(Configuration configuration,
 			ConfigurableBuilder builder) throws BuildException {
 		this.httpClient = new DefaultHttpClient();
         this.httpClient.getParams().setParameter(
                 ClientPNames.COOKIE_POLICY, 
                 CookiePolicy.BROWSER_COMPATIBILITY);
         this.urlGetter = new URLGetter("Research-Crawler-contact-" + 
                 "flaviov at dcc.ufmg.br");
 	}
 
 	@Override
 	public Set<String> getRequiredParameters() {
 		return null;
 	}
 
 	@Override
 	public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
 		String query = crawlID.getId();
 		CrawlResultFactory crawlResult = new CrawlResultFactory(crawlID);
 		List<CrawlID> toQueue = new ArrayList<>();
 		
 		try {
 			PayloadsFactory payloadsFactory = new PayloadsFactory();
 			int numResults = 0;
 			int offset = 0;
 			do {
 				HttpGet getMethod = new HttpGet(createUrl(query, offset));
 				byte[] jsonResult = 
 						this.urlGetter.getHtml(this.httpClient, getMethod, 
 								"", "");
 				payloadsFactory.addPayload(crawlID, jsonResult, offset + "");
 				
 				String jsonString = new String(jsonResult);
 				JSONObject json = new JSONObject(jsonString);
 				JSONArray results = 
 						json.getJSONObject("response").getJSONArray("list");
 				
 				for (int i = 0; i < results.length(); i ++) {
 					JSONObject aResult = results.getJSONObject(i);
 					long tstamp = aResult.getLong("firstpost_date");
 					String tstampStr = tstamp + "";
 					toQueue.add(
 							CrawlID.newBuilder().
 							setId(tstampStr).build());
 				}
 				
 				numResults = results.length();
 				offset += 1;
 			} while (numResults > 0);
 			return crawlResult.buildOK(payloadsFactory.build(), toQueue);
 		} catch (IOException | URISyntaxException | JSONException e) {
 			UnableToCrawlException cause = new UnableToCrawlException(e);
             return crawlResult.buildNonQuotaError(cause);
 		}
 	}
 
 	private URI createUrl(String query, int offset) throws URISyntaxException {
 		URIBuilder builder = new URIBuilder();
         builder.setScheme("http").
                 setHost("otter.topsy.com").
                 setPath("/search.js").
                 setParameter("window", "a").
                 setParameter("apikey", "09C43A9B270A470B8EB8F2946A9369F3").
                 setParameter("perpage", "100").
                 setParameter("offset", "" + (100 * offset));
         
 		String[] split = query.split("\t");
 		if (split.length == 3) {
 			builder.setParameter("q", split[0]);
 			builder.setParameter("mintime", split[1]);
 			builder.setParameter("maxtime", split[2]);
 		} else {
 			builder.setParameter("q", query);
 		}
 		
 		return builder.build();
 	}	
 }
