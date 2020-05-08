 package nz.co.searchwellington.controllers.models;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import nz.co.searchwellington.controllers.RelatedTagsService;
 import nz.co.searchwellington.controllers.RssUrlBuilder;
 import nz.co.searchwellington.filters.LocationParameterFilter;
 import nz.co.searchwellington.model.Geocode;
 import nz.co.searchwellington.model.PublisherContentCount;
 import nz.co.searchwellington.model.TagContentCount;
 import nz.co.searchwellington.repositories.ContentRetrievalService;
 import nz.co.searchwellington.urls.UrlBuilder;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.web.servlet.ModelAndView;
 
 @Component
 public class GeotaggedModelBuilder extends AbstractModelBuilder implements ModelBuilder {
 	
 	private static Logger log = Logger.getLogger(GeotaggedModelBuilder.class);
 	
 	private static final int REFINEMENTS_TO_SHOW = 8;
     protected static final double HOW_FAR_IS_CLOSE_IN_KILOMETERS = 1.0;
     
 	private ContentRetrievalService contentRetrievalService;
 	private UrlBuilder urlBuilder;
 	private RssUrlBuilder rssUrlBuilder;
 	private RelatedTagsService relatedTagsService;
 	
 	@Autowired
 	public GeotaggedModelBuilder(ContentRetrievalService contentRetrievalService, UrlBuilder urlBuilder, RssUrlBuilder rssUrlBuilder, RelatedTagsService relatedTagsService) {
 		this.contentRetrievalService = contentRetrievalService;
 		this.urlBuilder = urlBuilder;
 		this.rssUrlBuilder = rssUrlBuilder;
 		this.relatedTagsService = relatedTagsService;
 	}
 
 	@Override
 	public boolean isValid(HttpServletRequest request) {
 		return request.getPathInfo().matches("^/geotagged(/(rss|json))?$");
 	}
 	
 	@Override
 	public ModelAndView populateContentModel(HttpServletRequest request) {
 		if (isValid(request)) {
 			log.info("Building geotagged page model");
 			
 			ModelAndView mv = new ModelAndView();							
 			mv.addObject("heading", "Geotagged newsitems");
 			mv.addObject("description", "Geotagged newsitems");
 			mv.addObject("link", urlBuilder.getGeotaggedUrl());
 			
 			final Geocode userSuppliedLocation = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);						
 			final boolean hasUserSuppliedALocation = userSuppliedLocation != null;
 			if (hasUserSuppliedALocation) {
 				if (userSuppliedLocation.isValid()) {
 					
 					final double latitude = userSuppliedLocation.getLatitude();
 					final double longitude = userSuppliedLocation.getLongitude();
 					log.info("Location is set to: " + latitude + ", " + longitude);
 					
 					final int page = getPage(request);
 					mv.addObject("page", page);
 					final int startIndex = getStartIndex(page);
 					
 					final double radius = getLocationSearchRadius(request);					
 					final int totalNearbyCount = contentRetrievalService.getNewsitemsNearCount(latitude, longitude, radius);
 					if (startIndex > totalNearbyCount) {
 						return null;
 					}
 					populatePagination(mv, startIndex, totalNearbyCount);
 					
 					mv.addObject("location", userSuppliedLocation);
 					mv.addObject("latitude", latitude);	// TODO Are these used in the view?
 					mv.addObject("longitude", longitude);
 					
 					log.info("Populating main content with newsitems near: " + latitude + ", " + longitude + " (radius: " + radius + ")");
 					mv.addObject("main_content", contentRetrievalService.getNewsitemsNear(latitude, longitude, radius, startIndex, MAX_NEWSITEMS));
 				
 					if (userSuppliedLocation.getAddress() != null) {
 						mv.addObject("heading", rssUrlBuilder.getRssTitleForGeotagged(userSuppliedLocation));
 					}			
 					setRssForLocation(mv, userSuppliedLocation);
 				
 				}				
 				return mv;				
 			}
 			
 			final int page = getPage(request);
 			mv.addObject("page", page);	// TODO push to populate pagination.
 			final int startIndex = getStartIndex(page);
 			final int totalGeotaggedCount = contentRetrievalService.getGeotaggedCount();
 			if (startIndex > totalGeotaggedCount) {
 				return null;
 			}
 			
 			mv.addObject("main_content", contentRetrievalService.getGeocoded(startIndex, MAX_NEWSITEMS));			
 			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(), rssUrlBuilder.getRssUrlForGeotagged());
 			
 			populatePagination(mv, startIndex, totalGeotaggedCount);
 			return mv;
 		}
 		return null;
 	}
 
 	private double getLocationSearchRadius(HttpServletRequest request) {
 		double radius = HOW_FAR_IS_CLOSE_IN_KILOMETERS;
 		if (request.getAttribute(LocationParameterFilter.RADIUS) != null) {
 			radius = (Double) request.getAttribute(LocationParameterFilter.RADIUS);
 		}
 		return radius;
 	}
 	
 	@Override
 	public void populateExtraModelConent(HttpServletRequest request, ModelAndView mv) {
 		if (request.getAttribute(LocationParameterFilter.LOCATION) == null) {
 			mv.addObject("geotagged_tags", contentRetrievalService.getGeotaggedTags());			
 		} else {
			final Geocode userSuppliedLocation = (Geocode) request.getAttribute(LocationParameterFilter.LOCATION);
 			if (userSuppliedLocation.isValid()) {
 				List<TagContentCount> relatedTagLinks = relatedTagsService.getRelatedTagsForLocation(userSuppliedLocation, HOW_FAR_IS_CLOSE_IN_KILOMETERS, REFINEMENTS_TO_SHOW);
 				if (relatedTagLinks.size() > 0) {
 					mv.addObject("related_tags", relatedTagLinks);
 				}
 
 				List<PublisherContentCount> relatedPublisherLinks = relatedTagsService.getRelatedPublishersForLocation(userSuppliedLocation, HOW_FAR_IS_CLOSE_IN_KILOMETERS, REFINEMENTS_TO_SHOW);
 				if (relatedPublisherLinks.size() > 0) {
 					mv.addObject("related_publishers", relatedPublisherLinks);
 				}
 			}			
 		}
 		mv.addObject("latest_newsitems", contentRetrievalService.getLatestNewsitems(5));
 	}
 	
 	@Override
 	public String getViewName(ModelAndView mv) {
 		return "geocoded";
 	}
 
 	private void setRssForLocation(ModelAndView mv, Geocode location) {
 		final double latitude = location.getLatitude();
 		final double longitude = location.getLongitude();
 		if (location.getAddress() != null) {		
 			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(location), rssUrlBuilder.getRssUrlForGeotagged(location.getAddress()));					
 		} else {
 			setRss(mv, rssUrlBuilder.getRssTitleForGeotagged(location), rssUrlBuilder.getRssUrlForGeotagged(latitude, longitude));
 		}
 	}
 	
 }
