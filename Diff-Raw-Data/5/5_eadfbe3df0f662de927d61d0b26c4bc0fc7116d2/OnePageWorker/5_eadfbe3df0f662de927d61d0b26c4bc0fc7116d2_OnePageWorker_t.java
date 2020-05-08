 package ru.xrm.app.threads;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import ru.xrm.app.VacancyListOnePageParser;
 import ru.xrm.app.VacancyParser;
 import ru.xrm.app.config.Config;
 import ru.xrm.app.domain.Vacancy;
 import ru.xrm.app.domain.VacancyLink;
 import ru.xrm.app.httpclient.CachingHttpFetcher;
 import ru.xrm.app.httpclient.UrlHelper;
 
 public class OnePageWorker implements Callable<List<Vacancy>>{
 
 	private String url;
 	private String basename;
 	private String encoding;
 	private Config config;
 	private VacancyListOnePageParser onePageParser;
 	private CachingHttpFetcher fetcher;
 	private UrlHelper urlHelper;
 	
 
 	public OnePageWorker(Config config, String url, String basename, String encoding, CachingHttpFetcher fetcher){
 		this.config = config;
 		this.url = url;
 		this.basename = basename;
 		this.encoding = encoding;
 		this.onePageParser = new VacancyListOnePageParser(config, "");
 		this.fetcher = fetcher;
		this.urlHelper=UrlHelper.getInstance();
 	}
 
 
 	public List<Vacancy> call() throws Exception {
 		// ArrayList default size is 10, so use it instead of LinkedList 
 		List<Vacancy> result=new ArrayList<Vacancy>();
 
 		String content=fetcher.fetch(url, encoding);
 
 		onePageParser.setHtml(content);
 
 		List<VacancyLink> lvl = onePageParser.parse();
 
 		for (VacancyLink vacancyLink: lvl){
 
 			String link=vacancyLink.getHref();
 
 			link=urlHelper.constructAbsoluteUrl(link, basename);
 
 			// get vacancy itself
 
 			content=fetcher.fetch(link, "windows-1251");
 
 			VacancyParser vacancyParser=new VacancyParser(config, content);
 			Vacancy vacancy=vacancyParser.parse();
 
 			result.add(vacancy);
 		} // loop vacancies
 
		return result;
 	}
 
 
 	public String getUrl() {
 		return url;
 	}
 
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	
 }
