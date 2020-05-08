 package br.ufmg.dcc.vod.spiderpig.jobs.youtube.video;
 
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.configuration.ConfigurationException;
 
 import br.ufmg.dcc.vod.spiderpig.jobs.ConfigurableRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResult;
 import br.ufmg.dcc.vod.spiderpig.jobs.CrawlResultBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.PayloadBuilder;
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.video.api.VideoAPIRequester;
 import br.ufmg.dcc.vod.spiderpig.jobs.youtube.video.html.VideoHTMLRequester;
 import br.ufmg.dcc.vod.spiderpig.protocol_buffers.Ids.CrawlID;
 
 import com.google.common.collect.Sets;
 
 public class VideoRequester extends ConfigurableRequester {
 
 	private static final String CRAWL_HTML = "worker.job.youtube.video.html";
 	private static final String CRAWL_API = "worker.job.youtube.video.api";
 	
 	private boolean crawlHtml;
 	private boolean crawlApi;
 	
 	private VideoAPIRequester apiRequester;
 	private VideoHTMLRequester htmlRequester;
 	
 	public VideoRequester() {
 		this.apiRequester = new VideoAPIRequester();
 		this.htmlRequester = new VideoHTMLRequester();
 	}
 	
 	@Override
 	public Requester realConfigurate(Configuration configuration) 
 			throws Exception {
 		
 		this.apiRequester.configurate(configuration);
 		this.htmlRequester.configurate(configuration);
 		
 		boolean hasOne = crawlHtml || crawlApi;
 		if (!hasOne)
 			throw new ConfigurationException("Please set at least one option"
 					+ " to crawl");
 		
		this.crawlHtml = configuration.getBoolean(CRAWL_HTML);
		this.crawlApi = configuration.getBoolean(CRAWL_API);
		
 		return this;
 	}
 
 	@Override
 	public Set<String> getRequiredParameters() {
 		Set<String> req = Sets.newHashSet(CRAWL_HTML, CRAWL_API);
 		return req;
 	}
 
 	@Override
 	public CrawlResult performRequest(CrawlID crawlID) throws QuotaException {
 		CrawlResult apiResult = this.apiRequester.performRequest(crawlID);
 		if (apiResult.hasAnyError()) {
 			return apiResult;
 		}
 		
 		CrawlResult htmlResult = this.htmlRequester.performRequest(crawlID);
 		if (htmlResult.hasAnyError()) {
 			return htmlResult;
 		}
 		
 		CrawlResultBuilder resultBuilder = new CrawlResultBuilder(crawlID);
 		PayloadBuilder payloadBuilder = new PayloadBuilder();
 
 		for (Entry<String, byte[]> e : apiResult.getFilesToSave().entrySet())
 			payloadBuilder.addPayload(e.getKey(), e.getValue());
 
 		for (Entry<String, byte[]> e : htmlResult.getFilesToSave().entrySet())
 			payloadBuilder.addPayload(e.getKey(), e.getValue());
 			
 		return resultBuilder.buildOK(payloadBuilder.build());
 	}
 }
