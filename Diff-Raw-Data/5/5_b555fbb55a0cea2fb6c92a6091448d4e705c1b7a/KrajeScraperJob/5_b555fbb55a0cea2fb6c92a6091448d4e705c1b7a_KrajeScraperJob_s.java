 package jobs;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 
 import models.Organization;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import play.Logger;
import play.jobs.Every;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import cz.rhok.prague.osf.governmentcontacts.helper.RepeatOnTimeoutTask;
 import cz.rhok.prague.osf.governmentcontacts.scraper.PaginableRecordsListPageRetriever;
 import cz.rhok.prague.osf.governmentcontacts.scraper.PaginableRecordsListPageRetriever.PaginableRecord;
 import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekDetailPageScaper;
 import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekKrajeListPageScraper;
 import cz.rhok.prague.osf.governmentcontacts.scraper.SeznamDatovychSchranekMunicipalityListPageScraper;
 
@Every("0 0 23 * * ?") /* each day in 23:00 */
 public class KrajeScraperJob extends AbstractScraperJob {
 
 	private static final String KRAJS_LISTING_PAGE = "http://seznam.gov.cz/ovm/regionList.do";
 
 	@Override
 	public void doJob() throws Exception {
 
 		final SeznamDatovychSchranekKrajeListPageScraper krajsListPageScraper = 
 				new SeznamDatovychSchranekKrajeListPageScraper();
 
 		RepeatOnTimeoutTask<List<URL>> repeatingExtractDetailPageForKrajsPage = new RepeatOnTimeoutTask<List<URL>>() {
 
 			@Override
 			public List<URL> doTask() {
 				return krajsListPageScraper.extractDetailPageUrlsFrom(KRAJS_LISTING_PAGE);
 			}
 
 		};
 		List<URL> krajDetailPageUrls = repeatingExtractDetailPageForKrajsPage.call();
 
 		for (URL krajDetailPageUrl : krajDetailPageUrls) {
 			// save kraj to db
 			// kraj is not saved to db because it's itself part of municipality list
 		}
 
 		List<URL> krajDetailPageWithMunicipalitiesListUrls = Lists.newArrayList();
 
 		for (final URL krajDetailPageUrl : krajDetailPageUrls) {
 
 			RepeatOnTimeoutTask<Document> documentRetrieveTask = new RepeatOnTimeoutTask<Document>() {
 
 				@Override
 				public Document doTask() {
 					try {
 						return Jsoup.connect(krajDetailPageUrl.toExternalForm()).get();
 					} catch (IOException e) {
 						throw new RuntimeException(e);
 					}
 				}
 
 			};
 
 			Document document = documentRetrieveTask.call();
 
 			Element linkToDetailWithAllMunicipalities = document.select("a[href*=allMunicipality]").first();
 
 			if (linkToDetailWithAllMunicipalities != null) {
 				String relativeStringUrl = linkToDetailWithAllMunicipalities.attr("href");
 				String dataBoxBaseUrl = "http://seznam.gov.cz/ovm/regionDetail.do";
 				String urlAsString = dataBoxBaseUrl + relativeStringUrl;
 				try {
 					URL url = new URL(urlAsString);
 					krajDetailPageWithMunicipalitiesListUrls.add(url);
 				} catch (MalformedURLException e) {
 					Logger.error(
 							"Seems that url is malformed. Malformed url: %s When parsed document on: %s",
 							urlAsString, krajDetailPageUrl);
 				}
 			}
 
 		}
 
 
 		for (URL krajDetailUrl : krajDetailPageWithMunicipalitiesListUrls) {
 
 			/* get all urls for list pages of municipalities */
 			Map<Long, URL> allPages = Maps.newHashMap();
 
 			URL nextPaginable = krajDetailUrl;
 			while(nextPaginable != null) {
 				final String url = nextPaginable.toExternalForm();
 
 				RepeatOnTimeoutTask<PaginableRecord> listPagesRetrieverTask = new RepeatOnTimeoutTask<PaginableRecord>() {
 
 					@Override
 					public PaginableRecord doTask() {
 						PaginableRecordsListPageRetriever listPagesRetriever = new PaginableRecordsListPageRetriever();
 						return listPagesRetriever.getListPageLinks(url);
 					}
 
 				};
 
 				PaginableRecord paginable = listPagesRetrieverTask.call();
 				allPages.putAll(paginable.getPages());
 				nextPaginable = paginable.getNextPaginable();
 			}
 
 
 			for(final URL municipalityListPage : allPages.values()) {
 
 				RepeatOnTimeoutTask<List<URL>> municipalityListPageScraperTask = new RepeatOnTimeoutTask<List<URL>>() {
 
 					@Override
 					public List<URL> doTask() {
 						SeznamDatovychSchranekMunicipalityListPageScraper municipalityListPageScraper = 
 								new SeznamDatovychSchranekMunicipalityListPageScraper();
 
 						return municipalityListPageScraper.extractDetailPageUrlsFrom(municipalityListPage.toExternalForm());
 					}
 
 				};
 
 				List<URL> detailPageLinks = municipalityListPageScraperTask.call();
 
 				for (final URL municipalityDetailPageUrl : detailPageLinks) {
 					RepeatOnTimeoutTask<Organization> detailPageScrapeTask = new RepeatOnTimeoutTask<Organization>() {
 
 						@Override
 						public Organization doTask() {
 							SeznamDatovychSchranekDetailPageScaper detailPageScaper = new SeznamDatovychSchranekDetailPageScaper();
 							return detailPageScaper.scrape(municipalityDetailPageUrl.toExternalForm());
 						}
 
 					};
 
 					Organization organization = detailPageScrapeTask.call();
 
 					saveOrganization(organization);
 
 				}
 
 			}
 
 
 		}
 
 
 
 	}
 
 
 }
