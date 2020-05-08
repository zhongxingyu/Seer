 /*
  * Copyright 2012 Robert Philipp
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.microtitan.diffusive.launcher.config;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.microtitan.diffusive.diffuser.restful.RestfulDiffuser;
 
 /**
  * Simple utility methods that are used by the configuration classes
  * 
  * @author Robert Philipp
  */
 public class ConfigUtils {
 
 	private static final Logger LOGGER = Logger.getLogger( ConfigUtils.class );
 
 	/**
 	 * @return a {@link List} of {@link URI} that hold the location of end-points to which the
 	 * local {@link RestfulDiffuser} can diffuse method calls.
 	 */
 	public static List< URI > createEndpointList( final Collection< String > clientEndpoints )
 	{
 		final List< URI > endpoints = new ArrayList<>();
 		for( String client : clientEndpoints )
 		{
 			endpoints.add( URI.create( client ) );
 		}
 		return endpoints;
 	}
 	
 	/**
 	 * Validates each end-point specified in the list. If the end-point is valid, then it adds it the 
 	 * return list, otherwise it logs a warning with the invalid end-point address.
 	 * @param clientEndpoints The list of end-points of the client
 	 * @return A validated list of end-points.
 	 */
 	public static List< String > validateEndpoints( final Collection< String > clientEndpoints )
 	{
 		final List< String > endpoints = new ArrayList<>();
 		for( String client : clientEndpoints )
 		{
 			URI uri;
 			try
 			{
				if( !client.isEmpty() )
				{
					uri = new URI( client );
					endpoints.add( uri.getScheme() + "://" + uri.getHost() +":" + uri.getPort() + uri.getPath() );
				}
 			}
 			catch( URISyntaxException e )
 			{
 				final String message = "Invalid URI for end-point: " + client;
 				LOGGER.warn( message, e );
 			}
 		}
 		return endpoints;
 	}
 }
