 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.support.atom.AtomClient;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.http.HttpStatusCodeException;
 import com.metabroadcast.common.media.MimeType;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 
 public class C4BrandExtractor implements ContentExtractor<Feed, Brand> {
 
    private static final Pattern BAD_EPISODE_REDIRECT = Pattern.compile("(\\/episode-guide\\/series-\\d+)");
 	private final C4BrandBasicDetailsExtractor basicDetailsExtractor = new C4BrandBasicDetailsExtractor();
 	private final C4SeriesExtractor seriesExtractor = new C4SeriesExtractor();
 	private final C4EpisodesExtractor itemExtrator = new C4EpisodesExtractor().includeOnDemands().includeBroadcasts();
 	private final RemoteSiteClient<Feed> feedClient;
 
 	public C4BrandExtractor() {
 		this(new AtomClient());
 	}
 
 	public C4BrandExtractor(RemoteSiteClient<Feed> atomClient) {
 		feedClient = atomClient;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Brand extract(Feed source) {
 		Brand brand = basicDetailsExtractor.extract(source);
 		
 		List<Episode> items = Lists.newArrayList();
 		for (Series sery : fetchSeries(brand)) {
 			items.addAll((List) sery.getItems());
 		}
 
 		Map<String, Episode> onDemandEpisodes = onDemandEpisodes(brand);
 		
 		for (Episode episode : items) {
 			Episode odEpisode = onDemandEpisodes.get(episode.getCanonicalUri());
 			if (odEpisode != null) {
 				episode.setVersions(odEpisode.getVersions());
 			}
 			if (episode.getTitle().equals(brand.getTitle())) {
 				if (episode.getSeriesNumber() != null && episode.getEpisodeNumber() != null) {
 					episode.setTitle("Series " + episode.getSeriesNumber() + " Episode " + episode.getEpisodeNumber());
 				}
 			}
 		}
 		brand.setItems(items);
 		return brand;
 	}
 
 	private List<Series> fetchSeries(Brand brand) {
 		Feed episodeGuide = readEpisodeGuide(brand);
 		
 		// The episode guide points directly to a series
 		if (episodeGuide.getId().contains("series")) {
 			return ImmutableList.of(seriesExtractor.extract(episodeGuide));
 		} else {
 			return  loadSeriesFromFeeds(extractSeriesAtomFeedsFrom(episodeGuide));
 		
 		}
 	}
 
 	private List<Series> loadSeriesFromFeeds(List<String> seriesFeeds) {
 		List<Series> series = Lists.newArrayList();
 		for (String uri : seriesFeeds) {
 			try {
 				series.add(seriesExtractor.extract(feedClient.get(uri)));
 			} catch (Exception e) {
 				throw new FetchException("Could not fetch series: "  + uri, e);
 			}
 		}
 		return series;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<String> extractSeriesAtomFeedsFrom(Feed episodeGuide) {
 		List<String> seriesUris = Lists.newArrayList();
 		for (Entry entry : (List<Entry>) episodeGuide.getEntries()) {
 			List<Link> alternateLinks = entry.getOtherLinks();
 			for (Link link : alternateLinks) {
 				if (MimeType.APPLICATION_ATOM_XML.toString().equals(link.getType())) {
 					seriesUris.add(link.getHref());
 				}
 			}
 		}
 		return seriesUris;
 	}
 
 	/**
 	 * @return The feed if it exists or null if the page is probably a special case
 	 */
 	private Feed readEpisodeGuide(Brand brand) {
 		try {
 			return fetch(brand, "/episode-guide.atom");
 		} catch (HttpStatusCodeException e) {
 		    if (e.getStatusCode() == 403 && e.getResponse() != null) {
 		        Matcher matcher = BAD_EPISODE_REDIRECT.matcher(e.getResponse().finalUrl());
		        if (matcher.find()) {
 		            try {
                        return fetch(brand, matcher.group(1)+".atom");
 		            } catch (HttpStatusCodeException e1) {
 		                try {
 		                    return fetch(brand, "/episode-guide/series-1.atom");
 		                } catch (Exception e2) {}
                     } catch (Exception e1) {}
 		        }
 		    }
 		    
 		    throw new FetchException("could not fetch series guide for " + brand.getCanonicalUri(), e);
 		} catch (Exception e) {
 			throw new FetchException("could not read episode guide for ", e);
 		}
 	}
 	
 	private Map<String, Episode> onDemandEpisodes(Brand brand) {
 		try {
 			return toMap(itemExtrator.extract(fetch(brand, "/4od.atom")));
 		} catch (HttpStatusCodeException e) {
 			if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
 				return ImmutableMap.of();
 			}
 			throw new FetchException("could not read on demand info for " + brand.getCanonicalUri(), e);
 		} catch (Exception e) {
 			throw new FetchException("could not read on demand info for " + brand.getCanonicalUri(), e);
 		} 
 	}
 
 	private static <T extends Content> Map<String, T> toMap(Iterable<T> contents) {
 		Map<String, T> lookup = Maps.newHashMap();
 		for (T content : contents) {
 			lookup.put(content.getCanonicalUri(), content);
 		}
 		return lookup;
 	}
 
 	private Feed fetch(Brand brand, String extension) throws Exception {
 		String url = C4AtomApi.requestForBrand(brand.getCanonicalUri(), extension);
 		return feedClient.get(url);
 	}
 }
