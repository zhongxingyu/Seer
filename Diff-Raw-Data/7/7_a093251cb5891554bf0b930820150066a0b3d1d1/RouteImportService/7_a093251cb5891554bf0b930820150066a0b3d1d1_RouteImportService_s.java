 package uk.co.eelpieconsulting.busroutes.parsing;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import uk.co.eelpieconsulting.busroutes.daos.RouteStopDAO;
 import uk.co.eelpieconsulting.busroutes.daos.RoutesFileChecksumDAO;
 import uk.co.eelpieconsulting.busroutes.daos.StopDAO;
 import uk.co.eelpieconsulting.busroutes.model.PersistedStop;
 import uk.co.eelpieconsulting.busroutes.model.Route;
 import uk.co.eelpieconsulting.busroutes.model.RouteStop;
 import uk.co.eelpieconsulting.busroutes.model.Stop;
 import uk.co.eelpieconsulting.busroutes.services.StopsService;
 import uk.co.eelpieconsulting.busroutes.services.elasticsearch.ElasticSearchUpdateService;
 import uk.co.eelpieconsulting.common.files.FileInformationService;
 import uk.co.eelpieconsulting.countdown.api.CountdownApi;
 
 @Component
 public class RouteImportService {
 
 	private static Logger log = Logger.getLogger(RouteImportService.class);
 	
 	private final static int API_STOPS_FETCH_SIZE = 50;
 	private final static int RETRY_BATCH_SIZE = 25;
 	private final static int REQUEST_WAIT = 1000;
 	
 	private RoutesParser routesParser;
 	private RouteStopDAO routeStopDAO;
 	private StopsService stopsService;
 	private StopDAO stopDAO;
 	private CountdownApi countdownApi;
 	private ElasticSearchUpdateService elasticSearchUpdateService;
 	private RoutesFileChecksumDAO routesFileChecksumDAO;
 	private FileInformationService fileInformationService;
 	
 	public RouteImportService() {
 	}
 		
 	@Autowired	
 	public RouteImportService(RoutesParser routesParser, RouteStopDAO routeStopDAO, StopDAO stopDAO, StopsService stopsService, 
 			ElasticSearchUpdateService solrUpdateService, RoutesFileChecksumDAO routesFileChecksumDAO) {
 		this.routesParser = routesParser;
 		this.routeStopDAO = routeStopDAO;
 		this.stopDAO = stopDAO;
 		this.stopsService = stopsService;
 		this.elasticSearchUpdateService = solrUpdateService;
 		this.routesFileChecksumDAO = routesFileChecksumDAO;
 		this.fileInformationService = new FileInformationService();
 		
 		countdownApi = new CountdownApi("http://countdown.api.tfl.gov.uk");
 	}
 
 	public void importRoutes(File routesFile) throws IOException {
 		log.info("Importing route data from file: " + routesFile.getAbsolutePath());
 		
 		log.info("Purging existing stop data");
 		removeExisting();
 		
 		log.info("Importing new route/stop rows");
 		final List<Integer> stopIds = importRouteStops(routesParser.parseRoutesFile(routesFile));
 		Collections.sort(stopIds);
 		log.info("Created stop rows");
 		
 		log.info("Making stops from route stops");
 		makeStopsFromRouteStops(stopIds);
 		
 		log.info("Infilling stop details from arrivals api");
 		infillStopDetailsFromArrivalsAPI(stopIds);
 		
 		log.info("Decorating stops with routes");
 		decorateStopsWithRoutes();		
 		
		log.info("Rebuilding solr index");
		elasticSearchUpdateService.updateSolr();
		
 		log.info("Recording routes file checksum");
 		routesFileChecksumDAO.setChecksum(fileInformationService.getFileInformation(routesFile).getMd5());
 		
 		log.info("Done");
 	}
 
 	private void infillStopDetailsFromArrivalsAPI(List<Integer> stopIdsList) {
 		List<Integer> failed = fetchStopDetails(stopIdsList, API_STOPS_FETCH_SIZE);
 		if (failed.isEmpty()) {
 			log.info("Done");
 			return;
 		}
 		
 		log.info("Retrying failed batches with small batch sizes: " + RETRY_BATCH_SIZE);
 		failed = fetchStopDetails(failed, RETRY_BATCH_SIZE);
 		
 		log.info("Retrying failed ids individually");
 		failed = fetchStopDetails(failed, 1);
 		
 		log.info("Done");
 		if (failed.isEmpty()) {
 			log.info("All stops successfully fetched");
 			return;
 		}
 		
 		log.info("Number of unrecoverable failed ids: " + failed.size());
 		log.info("Unrecoverable failed ids: " + failed);
 	}
 	
 	private void removeExisting() {
 		routeStopDAO.removeAll();
 		stopDAO.removeAllStops();
 	}
 	
 	private List<Integer> importRouteStops(final List<RouteStop> routeStops) {
 		log.info("Importing RouteStop rows: " + routeStops.size());
 		final Set<Integer> stopIds = new HashSet<Integer>();
 		for (RouteStop routeStop : routeStops) {
 			if (!routeStop.getVirtual_Bus_Stop()) {
 				routeStopDAO.addRouteStop(routeStop);
 				stopIds.add(routeStop.getBus_Stop_Code());
 				
 			} else {
 				log.warn("Omitting virtual stop: " + routeStop);
 			}
 		}		
 		return new ArrayList<Integer>(stopIds);		
 	}
 	
 	private void makeStopsFromRouteStops(List<Integer> stopIds) {
 		final int size = stopIds.size();
 		int count = 1;
 		for (Integer stopId : stopIds) {
 			final RouteStop routeStop = routeStopDAO.getFirstForStopId(stopId);
 			final Stop stop = stopsService.makeStopFromRouteStop(routeStop);
 			log.debug(count + "/" + size + " - Creating stop: " + stop.getName());
 			stopDAO.saveStop(new PersistedStop(stop));
 			count++;
 		}
 	}
 	
 	private List<Integer> fetchStopDetails(List<Integer> stopIdsList, int batchSize) {
 		log.info("Fetching in batches of " + batchSize);
 
 		final List<Integer> failedIds = new ArrayList<Integer>();
 		for (int i = 0; i < stopIdsList.size(); i = i + batchSize) {
 			List<Integer> subList = stopIdsList.subList(i, i + batchSize < stopIdsList.size() ? i + batchSize : stopIdsList.size() -1);			
 			int j = 1;
 			final String progress = (i + j) + "/" + stopIdsList.size();
 			try {
 				final List<Stop> stopDetails = countdownApi.getStopDetails(subList);
 				for (Stop apiStop : stopDetails) {
 					final Stop stop = stopDAO.getStop(apiStop.getId());
 					stop.setName(apiStop.getName());
 					stop.setIndicator(apiStop.getIndicator());
 					stop.setTowards(apiStop.getTowards());			
 					
 					log.info(progress + " - Fetched details of stop: " + stop.getName());
 					stopDAO.saveStop(new PersistedStop(stop));
 				}
 				
 			} catch (Exception e) {
 				log.warn(progress + " - Recording failed batch (one of these stops is considered invalid by the arrivals api): " + subList);
 				failedIds.addAll(subList);
 			}
 			j++;
 			sleep(REQUEST_WAIT);
 		}
 		return failedIds;
 	}
 	
 	private void decorateStopsWithRoutes() {
 		List<PersistedStop> all = stopDAO.getAll();
 		final int numberToDecorate = all.size();
 		log.info("Decorating stops with routes: " + numberToDecorate);
 		int i = 1;
 		for (PersistedStop stop : all) {
 			decorateStopWithRoutes(stop);
 			stopDAO.saveStop(stop);
 			log.info(i + "/" + numberToDecorate + " - Decorated stop: " + stop.getName() + " (" + stop.getRoutes().size() + " route/s" + ")");
 			i++;
 		}
 	}
 	
 	private void decorateStopWithRoutes(final Stop stop) {
 		for (RouteStop stopRouteStop : routeStopDAO.findByStopId(stop.getId())) {
 			stop.addRoute(new Route(stopRouteStop.getRoute(), stopRouteStop.getRun(), getDestinationFor(stopRouteStop.getRoute(), stopRouteStop.getRun())));			
 		}
 	}
 	
 	private String getDestinationFor(String route, int run) {
 		RouteStop findLastForRoute = routeStopDAO.findLastForRoute(route, run);
 		final int lastStopId = findLastForRoute.getBus_Stop_Code();
 		Stop lastStopForRoute = stopDAO.getStop(lastStopId);
 		if (lastStopForRoute != null) {
 			return lastStopForRoute.getName();
 		}
 		
 		log.warn("Route " + route + "/" + run + " terminates at unknown stop: " + lastStopId);
 		return findLastForRoute.getStop_Name();
 	}
 	
 	private void sleep(int requestWait) {
 		try {
 			Thread.sleep(requestWait);
 		} catch (InterruptedException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
