 /**
  * Copyright (C) Dmitri Pisarenko
  * http://altruix.wordpress.com/
  */
 package ru.altruix.androidprototyping.server.services;
 
 import java.io.IOException;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 import ru.altruix.androidprototyping.server.persistence.IPersistence;
 
 
 /**
  *
  * @author Dmitri Pisarenko
  *
  */
 public abstract class AbstractService<PersistenceClass extends IPersistence,
 	RequestClass, ResponseClass extends AbstractResponse> {
 	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractService.class);
 
 	private PersistenceClass persistence;
 	
 	private Class<?> requestClass;
 	private Class<?> responseClass;
 	
 	public AbstractService()
 	{
 		final Map<String,Type> genericTypes = getGenericTypes();
 		
 		requestClass = genericTypes.get("RequestClass").getClass();
 		responseClass = genericTypes.get("ResponseClass").getClass();
 	}
 	
 	public PersistenceClass getPersistence() {
 		return persistence;
 	}
 
 	public void setPersistence(final PersistenceClass aPersistence) {
 		this.persistence = aPersistence;
 	}
 	
 	public String processRequest(final String aRequestAsText)
 	{		
 		final ObjectMapper mapper = new ObjectMapper();
 
 		ResponseClass response = null;
 		try
 		{
 			response = (ResponseClass) responseClass.newInstance();
 			response.setRequestProcessedSuccessfully(false);
 		}
		catch (final IllegalAccessException exception)
		{
			LOGGER.error("", exception);
			return "";
		}
 		catch (final InstantiationException exception)
 		{
 			LOGGER.error("", exception);
 			return "";
 		}
 		
 		try {						
 			final RequestClass request = (RequestClass) mapper.readValue(aRequestAsText, 
 					requestClass);
 			
 			fillResponseWithData(request, response);
 			response.setRequestProcessedSuccessfully(true);			
 		} catch (final JsonParseException exception) {
 			LOGGER.error("", exception);
 		} catch (final JsonMappingException exception) {
 			LOGGER.error("", exception);
 		} catch (final IOException exception) {
 			LOGGER.error("", exception);
 		} 
 		finally
 		{
 			return mapper.writeValueAsString(response);
 		}
 
 		
 		
 	}
 
 	private Map<String,Type> getGenericTypes()
 	{
 		final Map<String,Type> result = new HashMap<String,Type>();
 		
 		@SuppressWarnings("rawtypes")
 		final Class clazz = getClass();
 		
 		clazz.getTypeParameters();
 		
 		final ParameterizedType gen = (ParameterizedType) clazz.getGenericSuperclass();
 		final TypeVariable<?> typeVars[] = clazz.getTypeParameters();
 		final Type [] types = gen.getActualTypeArguments();
 	    for (int i = 0; i < typeVars.length; i++) {
 	    	result.put(typeVars[i].getName(), types[i]);
 	    }
 		
 		return result;
 	}
 	
 	protected abstract void fillResponseWithData(final RequestClass aRequest,
 			final ResponseClass aResponse);
 }
 
