 package com.amm.stores.rest;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicLong;
 import org.apache.log4j.Logger;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import static com.codahale.metrics.MetricRegistry.name;
 import com.codahale.metrics.MetricRegistry;
 import com.codahale.metrics.Counter;
 import com.codahale.metrics.Timer;
 import com.codahale.metrics.JmxReporter;
 import com.amm.stores.rest.metrics.MetricState;
 
 import com.amm.common.jaxrs.util.ResponseUtils;
 import com.amm.common.jaxrs.util.ApplicationError;
 import com.amm.stores.api.dto.stores.Store;
 import com.amm.stores.service.StoreService;
 import com.amm.common.util.ValidatorUtils;
 import com.amm.common.jaxrs.exceptions.InvalidFormatException;
 
 // TODO: get metrics annotations working :)
 
 @Path("/stores")
 public class StoreResource {
 	private static final Logger logger = Logger.getLogger(StoreResource.class);
 	private StoreService service;
 	private final AtomicLong getCount = new AtomicLong();
 	private MetricRegistry metricRegistry = MetricState.REGISTRY;
 	private Timer timerGet;
 	private Timer timerUpsert;
 	private Timer timerDelete;
 	private Counter counterGet;
 	
 	public StoreResource(StoreService service) {
 		this.service = service;
 		logger.debug("service=" + service);
 		counterGet = metricRegistry.counter(name(StoreResource.class, "counterGet"));
 		timerGet = metricRegistry.timer(name(StoreResource.class, "timerGet"));
 		timerUpsert = metricRegistry.timer(name(StoreResource.class, "timerUpsert"));
 		timerDelete = metricRegistry.timer(name(StoreResource.class, "timerDelete"));
 		JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
 		reporter.start();
 	}
 
 	@GET
 	@Path("/{storeId}")
 	@Produces({ MediaType.APPLICATION_JSON })
 	public Response getStore(@PathParam("storeId") String storeId) throws Exception {
 		getCount.getAndIncrement();
 		Timer.Context context = timerGet.time();
 		counterGet.inc();
 		try {
 			logger.debug("storeId=" + storeId);
 			Store store = service.get(storeId);
 			if (store == null) {
 				return ResponseUtils.createClientError(ApplicationError.NotFound.toString(), "Cannot find store ID: "+storeId , Response.Status.NOT_FOUND);
 			}
 			
 			logger.debug("storeId=" + storeId+" store=" + store);
 			return ResponseUtils.createGet(store);
 		} finally {
 			context.stop();
 		}
 	}
 	
 	@PUT
 	@Path("/{storeId}")
 	@Consumes({ MediaType.APPLICATION_JSON })
 	@Produces({ MediaType.APPLICATION_JSON })
	public Response upsert(Store store, @PathParam("storeId") String storeId)
 			throws Exception {
 		logger.debug("storeId=" + storeId+" store="+store);
 		Timer.Context context = timerUpsert.time();
 		try {
 			store.setStoreId(storeId);
			Store store2 = service.upsert(store);
			return ResponseUtils.createPut(store2);
 		} finally {
 			context.stop();
 		}
 	}
 
 	@DELETE
 	@Path("/{storeId}")
 	public void delete(@PathParam("storeId") String storeId) throws Exception {
 		logger.debug("storeId=" + storeId);
 		Timer.Context context = timerDelete.time();
 		try {
 			service.delete(storeId);
 		} finally {
 			context.stop();
 		}
 	}
 
 	public Response info() throws Exception {
 		Map<String,String> map = new LinkedHashMap<String,String>();
 		map.put("service",service.toString());
 		map.put("Upsert","count="+timerUpsert.getCount()+" mean="+timerUpsert.getMeanRate());
 		map.put("Delete","count="+timerDelete.getCount()+" mean="+timerDelete.getMeanRate());
 		map.put("Get","count="+timerGet.getCount()+" mean="+timerGet.getMeanRate());
 		return ResponseUtils.createGet(map);
 	}
 }
