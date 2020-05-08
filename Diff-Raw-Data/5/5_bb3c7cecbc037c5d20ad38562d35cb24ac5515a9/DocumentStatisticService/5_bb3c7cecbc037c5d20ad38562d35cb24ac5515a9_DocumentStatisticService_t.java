 package com.coderskitchen.macdi.webservice;
 
 import com.coderskitchen.macdi.entity.SingleCount;
 import com.coderskitchen.macdi.entity.WordStatistics;
 import com.coderskitchen.macdi.process.ProcessBoundary;
 import com.coderskitchen.macdi.statistic.DocumentStatisticBoundary;
 import net.anotheria.moskito.integration.cdi.Count;
 import net.anotheria.moskito.integration.cdi.Monitor;
 import net.anotheria.moskito.integration.cdi.MonitoringCategorySelector;
 import net.anotheria.moskito.integration.cdi.ProducerDefinition;
 
 import javax.annotation.ManagedBean;
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 import java.net.URI;
 
 /**
  * This class is the entry point for the restful webservice
  */
 @Path("/document")
 @ManagedBean
 public class DocumentStatisticService {
 
 	@Context
 	UriInfo uriInfo;
 	@Inject
 	DocumentStatisticBoundary dsb;
 	@Inject
 	ProcessBoundary pb;
 
 	@PUT
 	@Consumes({MediaType.APPLICATION_JSON})
 	@Monitor(MonitoringCategorySelector.WEB)
 	public Response uploadAndQueueDocument(String jsonDocument) {
 		String document = jsonDocument;
 		String newProcessId = pb.createNewProcessId();
 		dsb.createAndPersistStatistic(newProcessId, document);
 		UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
		String path = uriInfo.getPath();
		UriBuilder uriBuilder = baseUriBuilder.path(path).path(newProcessId);
 		URI build = uriBuilder.build();
 		Response r = Response.created(build).build();
 		return r;
 	}
 
 	@GET
 	@Path("{processId}/{word}")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Count()
 	public Response getCountingForWord(@PathParam("processId") String processId, @PathParam("word") String word) {
 		Integer countingForWord = dsb.getCountingForWord(processId, word);
 		SingleCount c = new SingleCount();
 		c.setCount(countingForWord);
 		c.setWord(word);
 		Response r;
 		r = Response.ok(c).build();
 		return r;
 	}
 
 	@GET
 	@Path("{processId}")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Count
 	public Response getWordStatistics(@PathParam("processId") String processId) {
 		WordStatistics statistics = dsb.getWordStatistics(processId);
 		Response r;
 		r = Response.ok(statistics).build();
 		return r;
 	}
 }
