 package ro.isdc.services;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.client.params.CookiePolicy;
 import org.apache.http.concurrent.FutureCallback;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.nio.client.HttpAsyncClient;
 import org.apache.http.nio.reactor.IOReactorException;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.params.CoreProtocolPNames;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 import org.atmosphere.cpr.AtmosphereResource;
 import org.atmosphere.cpr.Broadcaster;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.springframework.stereotype.Component;
 
 import ro.isdc.model.HtmlNodePathMapper;
 import ro.isdc.model.InfoSourceModel;
 import ro.isdc.model.MovieInfo;
 import ro.isdc.model.SimpleMovieInfo;
 import ro.isdc.parser.impl.SourceParserImpl;
 import ro.isdc.utils.Utils;
 import ro.isdc.utils.EncodingUtil;
 
 @Component("movieRetriever")
 public class MovieRetriever {
 
 	private HttpHost proxy = null;
 
 	public MovieRetriever() {
 
 		String proxyHost = System.getProperty("http.proxyHost");
 		String proxyPort = System.getProperty("http.proxyPort");
 		if (proxyHost != null && proxyPort != null) {
 			proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
 		}
 	}
 
 	public void retrieveMovieData(AtmosphereResource atmosphereResource, String searchTerm, InfoSourceModel infoSourceModel, final HtmlNodePathMapper htmlNodePathMapper,
 			boolean detailedMovieData) throws IOReactorException, InterruptedException {
 		HttpUriRequest uri = null;
 		String searchMethod = null;
 				
		/*try {
 			atmosphereResource.getResponse().flushBuffer();
 		} catch (IOException e) {
 			e.printStackTrace();
		}*/
 		
 		// check if the request was for detailed movie data, to obtain the correct uri
 		if (detailedMovieData) {
 			uri = Utils.getRequestForDetailedMovieData(infoSourceModel, searchTerm);
 			searchMethod = infoSourceModel.getSearchMethods().get("fullSearchMethod");
 		} else {
 			uri = Utils.getRequestForBriefMovieData(infoSourceModel, searchTerm);
 			searchMethod = infoSourceModel.getSearchMethods().get("briefSearchMethod");
 		}
 		if (searchMethod.equalsIgnoreCase("post")) {
 			// convert the uri to HttpPost in order to set the post Data via setEntity()
 			HttpPost httpPost = (HttpPost) uri;			
 			List<BasicNameValuePair> nameValuePairs = new ArrayList<BasicNameValuePair>(1);
 
 			if (infoSourceModel.getUsesCookies().equals("true")) {
 				Map<String, String> postDataMap = infoSourceModel.getPost().get("briefPostData").getPostDataMap();
 				Iterator<Entry<String, String>> it = postDataMap.entrySet().iterator();
 				while (it.hasNext()) {
 					Map.Entry<String, String> m = it.next();
 					if (m.getValue().equalsIgnoreCase("{title}")) {
 						// replace the title with the searchTerm typed by the user
 						nameValuePairs.add(new BasicNameValuePair(m.getKey(), searchTerm));
 					} else {
 						nameValuePairs.add(new BasicNameValuePair(m.getKey(), m.getValue()));
 					}
 				}
 				try {
 					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 				httpPost.setHeader("User-Agent", infoSourceModel.getPresetHeaders().get("User-Agent"));
 				HttpContext httpContext = retrieveContext(infoSourceModel);
 				final Broadcaster bc = atmosphereResource.getBroadcaster();
 				executePostRequest(infoSourceModel, bc, httpPost, htmlNodePathMapper, detailedMovieData, httpContext, searchTerm);		
 				
 			}
 		} else {// the search method is "get", so we don't need a HttpContext
 			final Broadcaster bc = atmosphereResource.getBroadcaster();
 			executeGetRequest(infoSourceModel, bc, uri, htmlNodePathMapper, detailedMovieData, searchTerm);			
 			
 		}
 
 	};
 
 	private void executeGetRequest(final InfoSourceModel infoSourceModel, final Broadcaster bc, final HttpUriRequest uri,
 			final HtmlNodePathMapper htmlNodePathMapper, final boolean detailedMovieData, final String searchTerm) throws InterruptedException, IOReactorException {
 		final HttpAsyncClient httpClient = new DefaultHttpAsyncClient();
 		initParams(httpClient);
 		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
 		httpClient.start();
 		httpClient.execute(uri, new FutureCallback<HttpResponse>() {
 			@Override
 			public void failed(Exception ex) {
 				System.out.println(uri.getRequestLine() + "->" + ex);
 			}
 
 			@Override
 			public void completed(HttpResponse result) {
 
 				if (detailedMovieData) {
 					try {
 						String responseAsString = EntityUtils.toString(result.getEntity());
 						SourceParserImpl parser = new SourceParserImpl();
 						String uriRequested = uri.getURI().getHost();
 						uriRequested = uriRequested.subSequence(uriRequested.indexOf('.') + 1, uriRequested.lastIndexOf('.')).toString();
 
 						MovieInfo movieInfo = (MovieInfo) parser.getMovieDetails(responseAsString, uriRequested, htmlNodePathMapper);
 						String movieAsJson = "";
 						if (movieInfo != null) {
 							final ObjectMapper mapper = new ObjectMapper();
 							movieAsJson = mapper.writeValueAsString(movieInfo);
 						} else {
 							System.out.println("The parser didn't retrieve any detailed movie information");
 						}
 
 						if (bc != null) {									
 							bc.broadcast(movieAsJson+"&~$");
 							System.out.println("Message sent; "+movieAsJson);												
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally {
 						try {							
 							httpClient.shutdown();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						} 
 					}
 				} else {// the request is for brief movie data
 					try {
 						String responseAsString = EntityUtils.toString(result.getEntity());
 						SourceParserImpl parser = new SourceParserImpl();
 						String uriRequested = uri.getURI().getHost();
 						uriRequested = uriRequested.subSequence(uriRequested.indexOf('.') + 1, uriRequested.lastIndexOf('.')).toString();
 
 						ArrayList<SimpleMovieInfo> movies = (ArrayList<SimpleMovieInfo>) parser.getMoviesByTitle(responseAsString, uriRequested, htmlNodePathMapper, searchTerm);
 
 						String moviesAsJson = "";
 						if (movies.size() > 0) {							
 							final ObjectMapper mapper = new ObjectMapper();
 							moviesAsJson = mapper.writeValueAsString(movies);
 						} else {
 							System.out.println("The parser didn't retrieve any brief movie information via GET");
 							JSONObject jsonObject = new JSONObject();
 							JSONArray jsonArray = new JSONArray();
 							jsonObject.put("site", uriRequested);
 							jsonArray.put(jsonObject);
 							moviesAsJson = jsonArray.toString();
 							System.out.println(moviesAsJson);
 						}
 
 						if (bc != null) {
 							bc.broadcast(moviesAsJson+"&~$");
 							System.out.println("Message sent: " + moviesAsJson);								
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 					} finally {
 						try {
 							httpClient.shutdown();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 
 			}
 
 			@Override
 			public void cancelled() {
 				System.out.println(uri.getRequestLine() + " cancelled");
 
 			}
 		});
 	}
 
 	private void executePostRequest(final InfoSourceModel infoSourceModel, final Broadcaster bc, final HttpUriRequest uri,
 			final HtmlNodePathMapper htmlNodePathMapper, final boolean detailedMovieData, HttpContext httpContext, final String searchTerm) throws InterruptedException,
 			IOReactorException {
 		final HttpAsyncClient httpClient = new DefaultHttpAsyncClient();
 		initParams(httpClient);
 		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
 		httpClient.start();
 		if (httpContext != null) {		
 			httpClient.execute(uri, httpContext, new FutureCallback<HttpResponse>() {
 				@Override
 				public void failed(Exception ex) {
 					System.out.println(uri.getRequestLine() + "->" + ex);
 				}
 
 				@Override
 				public void completed(HttpResponse result) {
 
 					/*
 					 * if there was only one result and the website redirected
 					 * our request to the page of that particular result, we
 					 * need to create another request for the detailed data from
 					 * that page
 					 */
 					if (result.getStatusLine().getStatusCode() == 302) {
 						String redirectURL = result.getFirstHeader("Location").getValue();
 						System.out.println("Redirect URL:"+redirectURL);
 						HttpUriRequest uri = new HttpGet(infoSourceModel.getSearchURLs().get("fetchCookieURL") + redirectURL);
 						try {
 							executeGetRequest(infoSourceModel, bc, uri, htmlNodePathMapper, true, searchTerm);
 						} catch (IOReactorException e) {
 							e.printStackTrace();
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						} finally {
 							try {
 								httpClient.shutdown();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							} 
 						}
 						return;
 					}
 
 					if (detailedMovieData) {
 						try {
 							String responseAsString = EntityUtils.toString(result.getEntity());
 							SourceParserImpl parser = new SourceParserImpl();
 							String uriRequested = uri.getURI().getHost();
 							uriRequested = uriRequested.subSequence(uriRequested.indexOf('.') + 1, uriRequested.lastIndexOf('.')).toString();
 
 							MovieInfo movieInfo = (MovieInfo) parser.getMovieDetails(responseAsString, uriRequested, htmlNodePathMapper);
 							String movieAsJson = "";
 							if (movieInfo != null) {
 								final ObjectMapper mapper = new ObjectMapper();
 								movieAsJson = mapper.writeValueAsString(movieInfo);
 							} else {
 								System.out.println("The parser didn't retrieve any detailed movie information");
 							}
 
 							if (bc != null) {
 								bc.broadcast(movieAsJson+"&~$");								
 								System.out.println("Message Sent:"+movieAsJson);							
 							}
 
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							try {
 								httpClient.shutdown();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							} 
 						}
 					} else {// the request is for brief movie data
 						try {
 							String responseAsString = EntityUtils.toString(result.getEntity());
 							SourceParserImpl parser = new SourceParserImpl();
 							String uriRequested = uri.getURI().getHost();
 							uriRequested = uriRequested.subSequence(uriRequested.indexOf('.') + 1, uriRequested.lastIndexOf('.')).toString();
 
 							ArrayList<SimpleMovieInfo> movies = (ArrayList<SimpleMovieInfo>) parser
 									.getMoviesByTitle(responseAsString, uriRequested, htmlNodePathMapper, searchTerm);
 							String moviesAsJson = "";
 							if (movies.size() > 0) {
 								final ObjectMapper mapper = new ObjectMapper();
 								moviesAsJson = mapper.writeValueAsString(movies);
 							} else {
 								System.out.println("The parser didn't retrieve any brief movie information via POST");
 								JSONObject jsonObject = new JSONObject();
 								JSONArray jsonArray = new JSONArray();
 								jsonObject.put("site", uriRequested);
 								jsonArray.put(jsonObject);
 								moviesAsJson = jsonArray.toString();
 								System.out.println(moviesAsJson);
 							}
 
 							if (bc != null) {
 								bc.broadcast(moviesAsJson+"&~$");
 								System.out.println("Message Sent:"+moviesAsJson);								
 							}
 						} catch (Exception e) {
 							e.printStackTrace();
 						} finally {
 							try {								
 								httpClient.shutdown();
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 
 				}
 
 				@Override
 				public void cancelled() {
 					System.out.println(uri.getRequestLine() + " cancelled");
 				}
 			});
 		} else {
 			System.out.println("Cannot execute POST request, the HttpContext is missing...");
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private HttpContext retrieveContext(InfoSourceModel infoSourceModel) {
 
 		HttpUriRequest syncRequest = new HttpGet(infoSourceModel.getSearchURLs().get("fetchCookieURL"));
 		syncRequest.setHeader("User-Agent", infoSourceModel.getPresetHeaders().get("User-Agent"));
 		// Create an instance of HttpClient with the params.
 		DefaultHttpClient syncClient = new DefaultHttpClient();
 		initParams(syncClient);
 		syncClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
 
 		HttpContext httpContext = new BasicHttpContext();
 		httpContext.setAttribute(CoreProtocolPNames.USER_AGENT, infoSourceModel.getPresetHeaders().get("User-Agent"));
 		try {
 			HttpResponse response = syncClient.execute(syncRequest, httpContext);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return httpContext;
 
 	}
 
 	/**
 	 * Initialization of parameters for http asynchronous requests
 	 * 
 	 * @param httpclient
 	 */
 	private void initParams(HttpAsyncClient httpclient) {
 		httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000).setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000)
 				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
 				.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		if (proxy != null) {
 			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		}
 		
 	}
 
 	/**
 	 * Initialization of parameters for http synchronous requests
 	 * 
 	 * @param httpclient
 	 */
 	private void initParams(HttpClient httpclient) {
 		httpclient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000).setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000)
 				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024).setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
 				.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		if (proxy != null) {
 			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
 		}
 	}
 
 }
