 package com.devportal.portlet.media.controller;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.GenericPortlet;
 import javax.portlet.PortletException;
 import javax.portlet.PortletPreferences;
 import javax.portlet.PortletRequestDispatcher;
 import javax.portlet.PortletURL;
 import javax.portlet.ProcessAction;
 import javax.portlet.ReadOnlyException;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ValidatorException;
 
 import com.devportal.portlet.media.util.YouTubeUtil;
 import com.google.gdata.client.Query;
 import com.google.gdata.client.youtube.YouTubeQuery;
 import com.google.gdata.client.youtube.YouTubeService;
 import com.google.gdata.data.Category;
 import com.google.gdata.data.youtube.PlaylistLinkEntry;
 import com.google.gdata.data.youtube.PlaylistLinkFeed;
 import com.google.gdata.data.youtube.YouTubeNamespace;
 import com.google.gdata.util.ServiceException;
 
 public class YouTubePlayListPortlet extends GenericPortlet {
 
 	private static final String PLAYLIST_TYPE_PLAYLIST = "playlist";
 	private static final String PLAYLIST_TYPE_SEARCH = "search"; 
 	
 	/**
 	 * Helper method to serve up the mandatory view mode.
 	 */
 	protected void doHelp(RenderRequest request, RenderResponse response)
 			throws PortletException, IOException {
 		response.setContentType("text/html");
 		PortletRequestDispatcher dispatcher = getPortletContext()
 				.getRequestDispatcher(
 						"/WEB-INF/jsp/YouTubePlayListPortlet_help.jsp");
 		dispatcher.include(request, response);
 	}
 
 	/**
 	 * Helper method to serve up the mandatory view mode.
 	 */
 	protected void doEdit(RenderRequest request, RenderResponse response)
 			throws PortletException, IOException {
 		// Cargamos la configuracion del playlist del portlet
 		PortletPreferences prefs = request.getPreferences();
 		String playlistType = prefs.getValue("playlistType", null);
 		String playlist = prefs.getValue("playlist", "");
 		String user = prefs.getValue("user", "");
 		String tags = prefs.getValue("tags", "");
 		request.setAttribute("playlistType", playlistType);
 		request.setAttribute("playlist", playlist);
 		request.setAttribute("user", user);
 		request.setAttribute("tags", tags);
 		
 		response.setContentType("text/html");
 		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/YouTubePlayListPortlet_edit.jsp");
 		
 		PortletURL editActionUrl = response.createActionURL();
 		editActionUrl.setParameter(ActionRequest.ACTION_NAME, "savePlaylist");
 		request.setAttribute("savePlaylistURL", editActionUrl);			
 
 		dispatcher.include(request, response);
 	}
 
 	/**
 	 * Helper method to serve up the mandatory view mode.
 	 */
 	protected void doView(RenderRequest request, RenderResponse response)
 			throws PortletException, IOException {
 		// Cargamos el codigo del playlist de las preferencias del portlet
 		PortletPreferences prefs = request.getPreferences();
 		String playlist = prefs.getValue("playlist", null);
 		String playlistType = prefs.getValue("playlistType", null);
 		String user = prefs.getValue("user", null);
 		String tags = prefs.getValue("tags", null);
 		
 		YouTubeService service = YouTubeUtil.getYouTubeServiceInstance();
 		int maxResults = 10; 
 		int start = 1; // Indice de comienzo del feed por defecto
 		Integer paginaActual = 1; // Indice de pagina por defecto
 		int totalResults = 0;
 		PlaylistLinkFeed videoFeed = null;
 		int totalPaginas = 0;
 		String wsUrl = "http://gdata.youtube.com/feeds/api/playlists";
 		Query.CategoryFilter categoryFilter;
 		YouTubeQuery query = null;
 			
 		if(playlistType != null && !playlistType.trim().isEmpty()) {
 			
 			if(playlistType.equals(YouTubePlayListPortlet.PLAYLIST_TYPE_SEARCH)) {
 				query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
 				categoryFilter = new Query.CategoryFilter();
 				if(tags != null && !tags.trim().isEmpty()) {
 					String [] arrTags = tags.trim().split(" ");
 					for(String tag: arrTags) {	
 						categoryFilter.addCategory(new Category(YouTubeNamespace.KEYWORD_SCHEME, tag.trim()));
 					}
 					query.addCategoryFilter(categoryFilter);
 				}
 				
 				//wsUrl = "http://gdata.youtube.com/feeds/api/videos";
 		        
 		        if(user != null && !user.trim().isEmpty()) {
 		        	query.setAuthor(user);
 		        }
 			}
 			
 			// Invocamos un primer feed para obtener el tamano de la lista de videos
 			String feedUrl = wsUrl + "/" + playlist + "?orderby=published&start-index=" + start + "&max-results=" + maxResults;
 			
 			try {
 				if(playlistType.equals(YouTubePlayListPortlet.PLAYLIST_TYPE_PLAYLIST)) {
 					videoFeed = service.getFeed(new URL(feedUrl), PlaylistLinkFeed.class);
 				}
 				else if(playlistType.equals(YouTubePlayListPortlet.PLAYLIST_TYPE_SEARCH)) {
 					 videoFeed = service.query(query, PlaylistLinkFeed.class);
 				}
 				totalResults = videoFeed.getTotalResults();
 				// Calculo del total de paginas
 				if(totalResults % maxResults == 0) {
 					// Si el modulo es igual a 0 es porque la pagina tiene una cantidad de registro a penas para la pagina
 					totalPaginas = totalResults / maxResults;
 				}
 				else {
 					totalPaginas = (totalResults / maxResults) + 1;
 				}
 
 			} catch (ServiceException se) {
 				// TODO Auto-generated catch block
 				se.printStackTrace();
 			}
 			
 			
 			// Asignacion de la pagina actual y del corrimiento (start -> offset) para el servicio web
 			if(request.getParameter("page") != null && !request.getParameter("page").trim().isEmpty()) {
 				try {
 					Integer page = Integer.parseInt(request.getParameter("page"));
 					if(page >= 1 && page <= totalPaginas) {
 						paginaActual = page;
 					}
 					else if (page > totalPaginas) {
 						paginaActual = totalPaginas;
 					}
 				} catch(Exception e) {
 					// No se hace nada porque se tiene cargados los valores de los indices por defecto
 				}
 			}
 			start = ((paginaActual -1) * maxResults) + 1;
 			// Fin asignacion pagina actual
 		
 			
 			/* Este codigo permite ver todas las listas de un usuario y enviarle peticiones a cada lista	
 			try {
 				String feedUrlLists = "http://gdata.youtube.com/feeds/api/users/campusparty/playlists";
 				PlaylistLinkFeed feedLists = service.getFeed(new URL(feedUrlLists), PlaylistLinkFeed.class);
 				for(PlaylistLinkEntry entry : feedLists.getEntries()) {
 					String playlistUrl = entry.getFeedUrl();
 					System.out.println("URL: " + playlistUrl);
 					PlaylistLinkFeed list = service.getFeed(new URL(playlistUrl), PlaylistLinkFeed.class);
 					System.out.println("SIZE: " + list.getTotalResults());
 					//System.out.println("PP : " + list.getItemsPerPage());
 					
 
 				}
 			} catch (ServiceException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			*/
 						
 		
 			feedUrl = wsUrl + "/" + playlist + "?orderby=published&start-index=" + start + "&max-results=" + maxResults;
 			System.out.println("FEED LIST: " + feedUrl);
 			List<Map<String,String>> feeds = new ArrayList<Map<String,String>>();
 			try {
 				//VideoFeed videoFeed = service.getFeed(new URL(feedUrl), VideoFeed.class);
 				if(playlistType.equals(YouTubePlayListPortlet.PLAYLIST_TYPE_PLAYLIST)) {
 					videoFeed = service.getFeed(new URL(feedUrl), PlaylistLinkFeed.class);
 				}
 				else if(playlistType.equals(YouTubePlayListPortlet.PLAYLIST_TYPE_SEARCH)) {
 					query.setMaxResults(maxResults);
 					query.setStartIndex(start);
 					videoFeed = service.query(query, PlaylistLinkFeed.class);
 					System.out.println("RES: " + totalResults);
 				}
 				for(PlaylistLinkEntry entry : videoFeed.getEntries() ) {
 					Map<String, String> feed = new HashMap<String,String>();
 					feed.put("title", entry.getTitle().getPlainText());
 					// Almacenamos la URL del video convertida a parametro de portlet
 					URL url = new URL(entry.getHtmlLink().getHref());
 					Map<String, String> res = YouTubeUtil.getQueryMap(url.getQuery());
 					PortletURL renderURL = response.createRenderURL();
 					renderURL.setParameter("video", res.get("v"));
 					feed.put("url", renderURL.toString());
 					feed.put("v", res.get("v"));
 					feeds.add(feed);
 				}
 				
 				System.out.println("SIZE: " + videoFeed.getTotalResults());
 				// Asignamos a al vista los videos tomados del feed
 				request.setAttribute("videos", feeds);
 				
 				// Paginadores
 				if(paginaActual < totalPaginas) { 
 					PortletURL siguienteUrl = response.createRenderURL();
 					siguienteUrl.setParameter("page",  Integer.valueOf(paginaActual + 1).toString());
 					request.setAttribute("siguienteUrl", siguienteUrl);
 				}
 				if(paginaActual > 1) {
 					PortletURL anteriorUrl = response.createRenderURL();
 					anteriorUrl.setParameter("page",  Integer.valueOf(paginaActual -1).toString());
 					request.setAttribute("anteriorUrl", anteriorUrl);
 				}
 				request.setAttribute("paginaActual", paginaActual);
 				
 			} catch (ServiceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}		
 			response.setContentType("text/html");
 			PortletRequestDispatcher dispatcher = getPortletContext()
 			.getRequestDispatcher(
 					"/WEB-INF/jsp/YouTubePlayListPortlet_view.jsp");
 			dispatcher.include(request, response);
 		}
 		
 	}
 	
 	@ProcessAction(name="savePlaylist") 
 	public void editAction(ActionRequest actionRequest, ActionResponse actionResponse) {
 		String playlistType = actionRequest.getParameter("playlist_type");
 		String playlist = actionRequest.getParameter("playlist");
 		String user = actionRequest.getParameter("user");
 		String tags = actionRequest.getParameter("tags");
 		PortletPreferences prefs = actionRequest.getPreferences();
 		if(playlistType != null & !playlistType.trim().isEmpty()) {
 			try {
 				prefs.setValue("playlistType", playlistType);
 				if(playlist != null & !playlist.trim().isEmpty()) {
 					prefs.setValue("playlist", playlist.trim());
 				}
				if(user != null) {
 					prefs.setValue("user", user.trim());
 				}
				if(tags != null) {
 					prefs.setValue("tags", tags.trim());
 				}
 				prefs.store();
 			} catch (ReadOnlyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ValidatorException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 }
