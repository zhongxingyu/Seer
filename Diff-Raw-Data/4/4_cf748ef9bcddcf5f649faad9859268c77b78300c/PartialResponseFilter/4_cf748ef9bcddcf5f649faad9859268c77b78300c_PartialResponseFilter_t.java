 package org.robbins.flashcards.webservices.filters;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.inject.Inject;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.cxf.jaxrs.ext.ResponseHandler;
 import org.apache.cxf.jaxrs.model.OperationResourceInfo;
 import org.apache.cxf.jaxrs.model.Parameter;
 import org.apache.cxf.message.Message;
 import org.apache.log4j.Logger;
 import org.robbins.flashcards.jackson.CustomObjectMapper;
 import org.robbins.flashcards.service.util.FieldInitializerUtil;
 import org.robbins.flashcards.webservices.exceptions.GenericWebServiceException;
 import org.springframework.stereotype.Component;
 
 import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
 import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
 
 @Component("partialResponseFilter")
 public class PartialResponseFilter implements ResponseHandler {
 
 	private static Logger logger = Logger.getLogger(PartialResponseFilter.class);
 
 	@Inject
 	private FieldInitializerUtil fieldInitializer;
 
 	@Inject
 	private CustomObjectMapper objectMapper;
 
 	@Context
 	private UriInfo uriInfo;
 
 	@Override
 	public Response handleResponse(Message m, OperationResourceInfo ori,
 			Response response) {
 
		if (ori == null) {
			return null;
		}
		
 		// exit now if not an http GET method
 		if (!StringUtils.equals(ori.getHttpMethod(), "GET")) {
 			return null;			
 		}
 
 		// exit now if not a 200 response, no sense in apply filtering if not a
 		// '200 OK'
 		if (response.getStatus() != Response.Status.OK.getStatusCode()) {
 			return null;
 		}
 
 		// exit now if we are not returning json
 		if (!ori.getProduceTypes().get(0).toString()
 				.equals(MediaType.APPLICATION_JSON)) {
 			return null;			
 		}
 
 		// get a reference to the response entity. the entity holds the payload
 		// of our response
 		Object entity = response.getEntity();
 
 		// try to get the 'fields' parameter from the QueryString
 		String fields = uriInfo.getQueryParameters().getFirst("fields");
 
 		// if the 'fields' QueryString is blank, then check to see if we have
 		// @DefaultValue for 'fields'
 		if (StringUtils.isBlank(fields)) {
 			// get the Parameters for this Resource
 			List<Parameter> parameters = ori.getParameters();
 			for (Parameter parm : parameters) {
 				// is the current Parameter named 'fields'?
 				if (StringUtils.equals(parm.getName(), "fields")) {
 					// get the default value for 'fields'
 					fields = parm.getDefaultValue();
 
 					// now that we found 'fields', there's no need to keep
 					// looping
 					break;
 				}
 			}
 			// If 'fields' is still blank then we don't have a value from either
 			// the QueryString or @DefaultValue
 			if (StringUtils.isBlank(fields)) {
 				logger.debug("Did not find 'fields' pararm for Resource '"
 						+ uriInfo.getPath() + "'");
 				return null;
 			}
 		}
 
 		Set<String> filterProperties = getFieldsAsSet(fields);
 
 		// is the entity a collection?
 		if (entity instanceof Collection<?>) {
 			initializeEntity((Collection<?>) entity, filterProperties);
 		}
 		// is the entity an array?
 		else if (entity instanceof Object[]) {
 			initializeEntity((Object[]) entity, filterProperties);
 		} else {
 			initializeEntity(entity, filterProperties);
 		}
 
 		// apply the Jackson filter and return the Response
 		return applyFieldsFilter(filterProperties, entity, response);
 	}
 
 	// configure the Jackson filter for the speicified 'fields'
 	private Response applyFieldsFilter(Set<String> filterProperties,
 			Object object, Response response) {
 		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
 		filterProvider.addFilter("apiFilter",
 				SimpleBeanPropertyFilter.filterOutAllExcept(filterProperties));
 		filterProvider.setFailOnUnknownId(false);
 
 		return applyWriter(object, filterProvider, response);
 	}
 
 	// Get a JSON string from Jackson using the provided filter.
 	// Note we are using the ObjectWriter rather than the ObjectMapper directly.
 	// According to the Jackson docs,
 	// "ObjectWriter instances are immutable and thread-safe: they are created by ObjectMapper"
 	// You should not change config settings directly on the ObjectMapper while
 	// using it (changing config of ObjectMapper is not thread-safe
 	private Response applyWriter(Object object,
 			SimpleFilterProvider filterProvider, Response response) {
 
 		try {
 			String jsonString = objectMapper.writer(filterProvider)
 					.writeValueAsString(object);
 
 			// replace the Response entity with our filtered JSON string
 			return Response.fromResponse(response).entity(jsonString).build();
 		} catch (Exception e) {
 			logger.error(e.getMessage(), e);
 			throw new GenericWebServiceException(
 					Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
 		}
 	}
 
 	// Convert the vectorized 'fields' parameter to a Set<String>
 	private Set<String> getFieldsAsSet(String fields) {
 		Set<String> filterProperties = new HashSet<String>();
 		StringTokenizer st = new StringTokenizer(fields, ",");
 		while (st.hasMoreTokens()) {
 			String field = st.nextToken();
 
 			// never allow 'userpassword' to be passed even if it was
 			// specifically requested
 			if (field.equals("userpassword")) {
 				continue;
 			}
 
 			// add the field to the Set<String>
 			filterProperties.add(field);
 		}
 
 		return filterProperties;
 	}
 
 	private void initializeEntity(Collection<?> entities, Set<String> fields) {
 		for (Object entity : entities) {
 			initializeEntity(entity, fields);
 		}
 	}
 
 	private void initializeEntity(Object[] entities, Set<String> fields) {
 		for (Object entity : entities) {
 			initializeEntity(entity, fields);
 		}
 	}
 
 	// make sure each of the requested 'fields' are initialized by Hibernate
 	private void initializeEntity(Object entity, Set<String> fields) {
 
 		if (entity == null) {
 			return;
 		}
 
 		// loop through the values of the 'fields'
 		for (String field : fields) {
 			try {
 				// Initialize the current 'field'
 				// This is needed since Hibernate will not auto-initialize most
 				// collections
 				// Therefore, if we want to return the field in the response, we
 				// need to make sure it is loaded
 				fieldInitializer.initializeField(entity, field);
 			} catch (Exception e) {
 				logger.error(e.getMessage(), e);
 				throw new GenericWebServiceException(
 						Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
 			}
 		}
 		return;
 	}
 }
