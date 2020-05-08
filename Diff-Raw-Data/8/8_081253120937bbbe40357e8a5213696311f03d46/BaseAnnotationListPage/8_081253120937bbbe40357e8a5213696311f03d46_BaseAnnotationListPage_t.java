 package at.ait.dme.yuma.server.gui;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 
 import at.ait.dme.yuma.server.URIBuilder;
 import at.ait.dme.yuma.server.gui.feeds.User;
 import at.ait.dme.yuma.server.gui.search.Search;
 import at.ait.dme.yuma.server.model.Annotation;
 import at.ait.dme.yuma.server.model.SemanticTag;
 
 /**
  * Base class for all pages that lists of annotations (i.e. feed and
  * search result pages).
  * 
  * TODO Memo to myself: currently throws a RuntimeException if not all
  * template components are set (title, headline, annotations, feed URL).
  * Probably better to fill member fields in the setters and then override
  * the onRender method, replacing nulls where necessary.
  * 
  * @author Rainer Simon
  */
 public abstract class BaseAnnotationListPage extends WebPage {
 
 	private static final String SPAN = "<span class=\"grad\"></span>";
	private static final String ELLIPSIS = "...";
 	
 	public BaseAnnotationListPage() {
 		add(new BookmarkablePageLink<String>("home", Search.class));
 	}
 	
 	public void setTitle(String title) {
 		add(new Label("title", title));				
 	}
 		
 	public void setHeadline(String headline) {
 		add(new Label("heading", SPAN + headline).setEscapeModelStrings(false));		
 	}
 	
 	public void setAnnotations(List<Annotation> annotations) {
 		add(new AnnotationListView("annotations", annotations));				
 	}
 	
 	public void setFeedURL(String feedUrl) {
 		if (feedUrl == null) {
 			add(new ExternalLink("list-feed-url", "#").add(new SimpleAttributeModifier("style", "visibility:hidden")));
 		} else {
 			add(LinkHeaderContributor.forRss(feedUrl));
 			add(new ExternalLink("list-feed-url", feedUrl));
 		}
 	}
 	
 	private class AnnotationListView extends ListView<Annotation> {
 
 		private static final long serialVersionUID = 6677934776500475422L;
 		
 		public AnnotationListView(String id, List<Annotation> list) {
 			super(id, list);
 		}
 
 		@Override
 		protected void populateItem(ListItem<Annotation> item) {
 			Annotation a = (Annotation) item.getModelObject();
 			item.add(new Label("title", a.getTitle()));
 			
 			HashMap<String, String> params = new HashMap<String, String>();
 			params.put(User.PARAM_USERNAME, a.getCreatedBy());
 			item.add(
 				new BookmarkablePageLink<String>("author-href", User.class, new PageParameters(params))
 					.add(new Label("author-label", a.getCreatedBy())));
 			
			String screenUri = a.getObjectUri();
			if (screenUri.length() > 40)
				screenUri = screenUri.substring(0, 55) + ELLIPSIS;
			item.add(new ExternalLink("objectUri", a.getObjectUri(), screenUri));
			
 			item.add(new Label("lastModified", a.getLastModified().toString()));
 			item.add(new Label("text", a.getText()));
 			String uri = URIBuilder.toURI(a.getAnnotationID()).toString();
 
 			StringBuffer tags = new StringBuffer();
 			if (a.getTags() != null) {
 				for (SemanticTag t : a.getTags()) {
 					tags.append(t.getPrimaryLabel() + ", ");
 				}
 				tags.delete(tags.length() - 2, tags.length());
 			}
 			item.add(new Label("tags", tags.toString()));
 			
 			item.add(new ExternalLink("uri", uri, uri));
 		}
 		
 	}
 
 }
