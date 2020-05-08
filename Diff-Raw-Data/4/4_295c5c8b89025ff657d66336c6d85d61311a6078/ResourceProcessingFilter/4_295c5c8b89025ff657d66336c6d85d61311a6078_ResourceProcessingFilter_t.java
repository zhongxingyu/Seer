 /*
  * Copyright 2012, Emanuel Rabina (http://www.ultraq.net.nz/)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nz.net.ultraq.postprocessing;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Parent class for any filter that does post-processing on resources found in a
  * Java web application.  Includes a resource cache to prevent unecessary
  * post-processing.
  * 
  * @author Emanuel Rabina
  * @param <R> Specific resource type.
  */
 public abstract class ResourceProcessingFilter<R extends Resource> implements Filter {
 
 	private final HashMap<String,R> resourcecache = new HashMap<>();
 
 	/**
 	 * Given these bits and pieces, build a resource object that can be used for
 	 * processing.
 	 * 
 	 * @param path
 	 * @param resourcecontent
 	 * @return Resource.
 	 * @throws IOException
 	 */
 	protected abstract R buildResource(String path, String resourcecontent) throws IOException;
 
 	/**
 	 * Does nothing.
 	 */
 	@Override
 	public void destroy() {
 	}
 
 	/**
 	 * Allows a resource request to first go through to the application server,
 	 * then checks to see if the resource has been processed or if the processed
 	 * result has changed since the last time that resource was processed.  If
 	 * it hasn't been processed or if it has changed, invokes the implementing
 	 * filter to do its post-processing tasks and caches that result.  If it
 	 * hasn't changed, it just uses the one already in the cache.
 	 * 
 	 * @param req
 	 * @param res
 	 * @param chain
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	@Override
 	public final void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
 		throws IOException, ServletException {
 
 		HttpServletRequest request = (HttpServletRequest)req;
 		HttpServletResponse response = (HttpServletResponse)res;
 
 		// Capture the resource file
 		ResourceResponseWrapper resourceresponsewrapper = new ResourceResponseWrapper(response);
 		chain.doFilter(request, resourceresponsewrapper);
 
		// Stop processing if not all good
		if (resourceresponsewrapper.getStatus() != HttpServletResponse.SC_OK) {
 			return;
 		}
 
 		// Use URL as the cache key
 		StringBuffer urlbuilder = request.getRequestURL();
 		if (request.getQueryString() != null) {
 			urlbuilder.append(request.getQueryString());
 		}
 		String url = urlbuilder.toString();
 
 		// Create a new processing result
 		R resource;
 		if (!resourcecache.containsKey(url) || resourcecache.get(url).isModified()) {
 			resource = buildResource(request.getServletContext().getRealPath(request.getServletPath()),
 					new String(resourceresponsewrapper.getResourceBytes().toByteArray()));
 			doProcessing(resource);
 			resourcecache.put(url, resource);
 		}
 		// Use the existing result in cache
 		else {
 			resource = resourcecache.get(url);
 		}
 
 		// Write processed result to response
 		response.setContentLength(resource.getProcessedContent().getBytes().length);
 		response.getOutputStream().write(resource.getProcessedContent().getBytes());
 	}
 
 	/**
 	 * Perform post-processing on the given resource.
 	 * 
 	 * @param resource
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	protected abstract void doProcessing(R resource) throws IOException, ServletException;
 }
