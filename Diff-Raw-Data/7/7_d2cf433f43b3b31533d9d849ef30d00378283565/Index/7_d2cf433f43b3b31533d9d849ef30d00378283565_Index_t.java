 package wicket.examples.flickr.standard;
 
 import java.util.List;
 import java.util.Vector;
 
 import wicket.AttributeModifier;
 import wicket.PageParameters;
 import wicket.examples.flickr.FlickrDao;
 import wicket.examples.flickr.HelpInfo;
 import wicket.markup.html.WebPage;
 import wicket.markup.html.form.Form;
 import wicket.markup.html.form.TextField;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.model.PropertyModel;
 
 /**
  * Home page for the Flickr Demo application. This page will load thumbnail
  * image url's from the static flickr servers using the flickr API. The search
  * is conducted using tags provided by the user of this application, which are
  * submitted using the form on this page.
  * 
  * This application is a blatant copy from the Ruby on Rails demo and is a showcase
  * for Wicket application programming. See the rails demo on http://www.rubyonrails.org.
  * 
  * The Ruby on Rails Flickr demo uses AJAX for the form submission and rendering of the
  * list of images. This demo is built using Wicket 1.1, so we don't have the ability to
  * render the list of images yet.
  */
 public class Index extends WebPage {
 	/** Used for serialization. */
 	private static final long serialVersionUID = 1L;
 
 	/** Holds the tags from the input field. */
 	public String tags;
 
 	/** Holds the urls to the image thumbnails. */
 	public List<String> images = new Vector<String>();
 
 	/**
 	 * Constructor that is invoked when page is invoked without a session.
 	 * 
 	 * @param parameters
 	 *            Page parameters
 	 */
 	public Index(final PageParameters parameters) {
 		add(new HelpInfo("helpinfo"));
 		Form form = new Form("form") {
 			/** used for serialization. */
 			private static final long serialVersionUID = 1L;
 
 			protected void onSubmit() {
 				// clear the list of images
 				images.clear();
 
 				// get the new image URL's from flickr
 				FlickrDao flickr = new FlickrDao();
				if(tags == null) tags = "";
 				images.addAll(flickr.getSmallSquareImages(tags.split(" ")));
 			}
 		};
 		add(form);
 		TextField tags = new TextField("tags", new PropertyModel(this, "tags"));
 		ListView photos = new ListView("photos", new PropertyModel(this, "images")) {
 			/** Used for serialization. */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void populateItem(ListItem item) {
 				item.add(new AttributeModifier("src", item.getModel()));
 			}
 		};
 		form.add(tags);
 		form.add(photos);
 	}
 }
