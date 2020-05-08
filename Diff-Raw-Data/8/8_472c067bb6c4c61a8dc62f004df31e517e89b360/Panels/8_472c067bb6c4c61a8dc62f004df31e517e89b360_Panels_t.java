 package com.madalla.webapp.panel;
 
 import static com.madalla.webapp.PageParams.RETURN_PAGE;
 import static com.madalla.webapp.blog.BlogParameters.BLOG_ENTRY_ID;
 import static com.madalla.webapp.blog.BlogParameters.BLOG_NAME;
 import static com.madalla.webapp.images.admin.AlbumParams.ALBUM;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.wicket.Page;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.WicketRuntimeException;
 import org.apache.wicket.markup.html.panel.Panel;
 
 import com.madalla.webapp.CmsApplication;
 import com.madalla.webapp.blog.BlogHomePanel;
 import com.madalla.webapp.blog.admin.BlogEntryPanel;
 import com.madalla.webapp.email.EmailFormPanel;
 import com.madalla.webapp.google.YoutubePlayerPanel;
 import com.madalla.webapp.images.AlbumPanel;
 import com.madalla.webapp.images.admin.AlbumAdminPanel;
 import com.madalla.webapp.images.exhibit.ExhibitPanel;
 
 /**
  * Utility static instantiation methods to create Wicket Panels
  * for your Pages. Provided to simplify the API by providing one
  * place for getting Panels.
  * 
  * NOTE: Your Wicket Application needs to extend {@link CmsApplication} before you can use these Panels
  * 
 * TODO restyle class to a Page Utils
  * @author Eugene Malan
  *
  */
 public class Panels {
 	
 	private Panels(){}
 	
 	/**
 	 * @param id wicket id
 	 * @param node Content Parent Node, normally Page Name
 	 * @return Instantiated Panel of type YoutubePlayerPanel
 	 */
 	public static Panel videoPlayerPanel(String id, String node){
 		validate("YoutubePlayerPanel", id, node);
 		return new YoutubePlayerPanel(id, node);
 	}
 	
 	
 	/**
 	 * @param id - wicket id
 	 * @param blog - Name of this Blog
 	 * @param returnPage - used to return from blog admin Page
 	 * @param parameters - page parameters
 	 * @return Instantiated Panel of Type {@link com.madalla.webapp.blog.BlogHomePanel}
 	 */
 	public static Panel blogPanel(String id, String blog, Class<? extends Page> returnPage, PageParameters parameters) {
 		validate("BlogHomePanel", id, blog, returnPage, parameters);
 		String blogEntryId = parameters.getString(BLOG_ENTRY_ID);
    		return new BlogHomePanel(id, blog, blogEntryId, returnPage);
 	}
 	
 	/**
 	 * @param id - wicket id
 	 * @param parameters - page parameters including Blog name and return page
 	 * @return Instantiated Panel of Type {@link com.madalla.webapp.blog.admin.BlogEntryPanel}
 	 */
 	public static Panel blogEntryPanel(String id, PageParameters parameters){
 		validate("BlogEntryPanel", id, parameters);
 		String blogName = getPageParameter(BLOG_NAME, parameters, "BlogEntryPanel");
 		String blogEntryId = parameters.getString(BLOG_ENTRY_ID);
 		if (StringUtils.isEmpty(blogEntryId)){
 			return new BlogEntryPanel(id, blogName, getReturnPage(parameters, "BlogEntryPanel"));
 		} else {
 			return new BlogEntryPanel(id, blogName, blogEntryId, getReturnPage(parameters, "BlogEntryPanel"));
 		}
 	}
 	
 	/**
 	 * @param id - wicket id
 	 * @param subject - this will end up as subject in the email
 	 * @return Instantiated Panel of Type {@link com.madalla.webapp.email.EmailFormPanel}
 	 */
 	public static Panel emailPanel(String id, String subject){
 		validate("EmailFormPanel", id, subject);
 		return new EmailFormPanel(id, subject);
 	}
 	
 	/**
 	 * @param id - wicket id
 	 * @param parameters - pageParameters - passed on to Panel
 	 * @return Instantiated Panel of Type {@link com.madalla.webapp.images.admin.AlbumAdminPanel}
 	 */
 	public static Panel albumAdminPanel(String id, PageParameters parameters){
 		String album = getPageParameter(ALBUM, parameters,"AlbumAdminPanel");
 		return new AlbumAdminPanel(id, album);
 	}
 	
 	/**
 	 * @param id - wicket id
 	 * @param album
 	 * @param returnPage - used to create return link
 	 * @return Instantiated Panel of Type {@link com.madalla.webapp.images.AlbumPanel}
 	 */
 	public static Panel albumPanel(String id, String album, Class<? extends Page> returnPage){
 		validate("AlbumPanel", id, album, returnPage);
 		return new AlbumPanel(id, album, returnPage);
 	}
 
 	/**
      * @param id - wicket id & name
      * @param returnPage - used to create return link
      * @return Instantiated Panel of Type {@link com.madalla.webapp.images.AlbumPanel}
      */
     public static Panel exhibitPanel(String id, Class<? extends Page> returnPage){
         validate("ExhibitPanel", id, returnPage);
         return new ExhibitPanel(id, returnPage);
     }
 	
 	/*  Utility methods */
 	private static void validate(String panelName, String param1, String param2){
 		if (StringUtils.isEmpty(param1) || StringUtils.isEmpty(param2)){
 			error(panelName + " - All parameters need to be supplied.");
 		}
 	}
 	private static void validate(String panelName, String s1, String s2, Object o){
 		if (StringUtils.isEmpty(s1) || StringUtils.isEmpty(s2) || o == null){
 			error(panelName + " - All parameters need to be supplied.");
 		}
 	}
 	private static void validate(String panelName, String s1, String s2, Object o, Object o2){
 		if (StringUtils.isEmpty(s1) || StringUtils.isEmpty(s2) || o == null || o2 == null){
 			error(panelName + " - All parameters need to be supplied.");
 		}
 	}
 	private static void validate(String panelName, String s1, Object o){
 		if (StringUtils.isEmpty(s1) || o == null){
 			error(panelName + " - All parameters need to be supplied.");
 		}
 	}
 	private static void error(String message){
 		throw new WicketRuntimeException(message);
 	}
 	private static void error(String message, Exception e){
 		throw new WicketRuntimeException(message, e);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static Class<? extends Page> getReturnPage(PageParameters parameters, String panel){
 		String pageString = getPageParameter(RETURN_PAGE, parameters, panel);
 		Class returnPage = null;
 		try {
             returnPage = Class.forName(pageString);
             returnPage.asSubclass(Page.class);
         } catch (ClassNotFoundException e) {
         	error(panel + " - ClassNotFoundException while getting return Class from PageParameters.", e);
         } catch (ClassCastException e){
         	error(panel + " - The Class Type of the PageParameter '"+RETURN_PAGE+"' needs to extend the Page class.");
         }
         return returnPage;
 	}
 	
 	/** throws Exception if param not found */
 	public static String getPageParameter(String paramName, PageParameters parameters, String panel){
 		String paramValue = parameters.getString(paramName);
 		if (StringUtils.isEmpty(paramValue)){
 			error(panel + " - A pageParameter with value '"+ paramName+"' needs to be supplied.");
 		}
 		return paramValue;
 	}
 	
 	/** supply default value if param not found */
 	public static String getPageParameter(String paramName, PageParameters parameters, String panel, String defaultValue){
 		String paramValue = parameters.getString(paramName);
 		if (StringUtils.isEmpty(paramValue)){
 			return defaultValue;
 		} else {
 			return paramValue;
 		}
 	}
 
 }
