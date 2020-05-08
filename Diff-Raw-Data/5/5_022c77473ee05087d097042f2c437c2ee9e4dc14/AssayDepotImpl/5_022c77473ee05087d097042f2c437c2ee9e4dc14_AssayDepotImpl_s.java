 package com.assaydepot;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.JsonToken;
 
 import com.assaydepot.conf.Configuration;
 import com.assaydepot.result.BaseResult;
 import com.assaydepot.result.Provider;
 import com.assaydepot.result.ProviderRef;
 import com.assaydepot.result.ProviderResult;
 import com.assaydepot.result.Results;
 import com.assaydepot.result.WareRef;
 
 public class AssayDepotImpl implements AssayDepot {
 
 	private static final String BASE_URL_STRING = "https://www.assaydepot.com/api/providers.json";
 	private Configuration conf;
 	
 	AssayDepotImpl( Configuration conf ) {
 		this.conf = conf;
 	}
 	
 	public Results getProviders( String query ) throws JsonParseException, IOException {
 		StringBuilder urlBuilder = new StringBuilder( BASE_URL_STRING );
 		if( query != null ) {
 			urlBuilder.append( "?q=" ).append( query );
 		}
 		if( conf.getApiToken() != null ) {
 			urlBuilder.append( "&auth_token=" ).append( conf.getApiToken() );
 		}
 
 		URL url = new URL( urlBuilder.toString() );
 		JsonFactory f = new JsonFactory();
 		JsonParser jp = f.createJsonParser( url.openStream() );
 		
 		Results results = new Results();
 		results.setProviderRefs( new ArrayList<ProviderRef>() );
 		results.setProviders( new ArrayList<Provider>() );
 		results.setFacets( new HashMap<String,Map<String,String>>() );
 		
 		
 		jp.nextToken(); // will return JsonToken.START_OBJECT (verify?)
 		//
 		// Get result metadata
 		//
 		while (jp.nextToken() != JsonToken.END_OBJECT) {
 			String fieldName = jp.getCurrentName();
 			jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
 			//
 			// Build the Results object
 			//
 			if( "total".equals( fieldName )) {
 				results.setTotal( jp.getIntValue() );
 			} else if( "page".equals( fieldName )) {
 				results.setPage( jp.getIntValue() );
 			} else if( "per_page".equals( fieldName )) {
 				results.setPerPage( jp.getIntValue() );
 			} else if( "query_time".equals( fieldName )) {
 				results.setQueryTime( jp.getDoubleValue() );
 			} else if ("facets".equals( fieldName )) { // contains an object
 				Map<String,String> facetMap = null;
 				while ( jp.nextToken() != JsonToken.END_OBJECT ) {
 					fieldName = jp.getCurrentName();
 					facetMap = new HashMap<String,String>();
 					results.getFacets().put( fieldName, facetMap );
 					JsonToken token = jp.nextToken();
 					while ( token != JsonToken.END_OBJECT ) {
 						if( token == JsonToken.START_OBJECT ) { // skip the "{" for each facet
 							token = jp.nextToken();
 							if( token == JsonToken.END_OBJECT ) {
 								token = jp.nextToken();
 							}
 						}
 					}
 					System.out.println( "token1 = ["+token+"]");
 					token = jp.nextToken();
					results.getFacets().get( fieldName ).put( value1, value2 ));
 					System.out.println( "token2 = ["+token+"]");
 					System.out.println( "token3 = ["+jp.getText()+"]");
 				}
 			} else if ("providerRefs".equals( fieldName )) {
 				getProviderRefs( jp, results );
 			} else {
 //				throw new IllegalStateException("Unrecognized field '"+fieldName+"'!");
 				System.out.println("Unrecognized field '"+fieldName+"'!");
 			}
 		}
 		
 		//
 		// Get result providers
 		//
 		Provider provider = new Provider();
 		
 		while (jp.nextToken() != JsonToken.END_OBJECT) {
 			String fieldName = jp.getCurrentName();
 			jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
 			//
 			// Build the Results object
 			//
 			if( "total".equals( fieldName )) {
 				results.setTotal( jp.getIntValue() );
 			} else if( "page".equals( fieldName )) {
 				results.setPage( jp.getIntValue() );
 		jp.close(); // ensure resources get cleaned up timely and properly
 		
 			}
 
 		}
 		return results;
 	}
 	
 	private void getLocations( JsonParser jp, ProviderResult pResult ) throws JsonParseException, IOException {
 		Map<String,String> locationMap = null;
 		while( jp.nextToken() != JsonToken.END_ARRAY ) {
 			locationMap = new HashMap<String,String>();					
 			while( jp.nextToken() != JsonToken.END_OBJECT ) {
 				String fieldName = jp.getCurrentName();
 				jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
 				locationMap.put( fieldName
 						, jp.getText() );
 				pResult.getLocations().add( locationMap );
 			}
 		}			
 	}
 	private void getUrls( JsonParser jp, BaseResult baseResult ) throws JsonParseException, IOException {
 		baseResult.setUrls( new HashMap<String, String>() );
 		while( jp.nextToken() != JsonToken.END_ARRAY ) {
 			String fieldName = jp.getCurrentName();
 			jp.nextToken(); // move to value, or START_OBJECT/START_ARRAY
 			baseResult.getUrls().put( fieldName, jp.getText() );
 		}
 	}
 	/**
 	 * Provider refs are actually opened and closed with the JSON Array chars '[' and ']'
 	 * @param jp
 	 * @param results
 	 * @throws JsonParseException
 	 * @throws IOException
 	 */
 	private void getProviderRefs( JsonParser jp, Results results ) throws JsonParseException, IOException  {
 		ProviderRef pRef = null;	
 		String fieldName = null;
 		while (jp.nextToken() != JsonToken.END_ARRAY ) {
 			pRef = new ProviderRef();
 			pRef.setLocations( new ArrayList<Map<String,String>>() );
 			
 			while (jp.nextToken() != JsonToken.END_OBJECT ) {
 				fieldName = jp.getCurrentName();
 				if( "id".equals( fieldName )) {
 					pRef.setId( jp.getText() );
 				} else if( "slug".equals( fieldName )) {
 					pRef.setSlug( jp.getText() ); 
 				} else if( "name".equals( fieldName )) {
 					pRef.setName( jp.getText() ); 
 				} else if( "snippet".equals( fieldName )) {
 					pRef.setSnippet( jp.getText() ); 
 				} else if( "permission".equals( fieldName )) {
 					pRef.setPermission( jp.getText() ); 
 				} else if( "score".equals( fieldName )) {
					pRef.setScore( jp.getFloatValue() ); 
 				} else if( "locations".equals( fieldName )) {
 					getLocations( jp, pRef );
 				} else if( "urls".equals( fieldName )) {
 					getUrls( jp, pRef );
 				}
 			}
 			results.getProviderRefs().add( pRef );
 		}
 
 	}
 
 
 	public Results getWares( String query ) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
