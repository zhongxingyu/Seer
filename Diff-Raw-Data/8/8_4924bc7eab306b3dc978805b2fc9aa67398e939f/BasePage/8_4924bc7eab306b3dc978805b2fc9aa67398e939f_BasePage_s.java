 package ar.edu.itba.paw.grupo1.web.Base;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.ContextRelativeResource;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.edu.itba.paw.grupo1.model.Ad;
 import ar.edu.itba.paw.grupo1.model.Owned;
 import ar.edu.itba.paw.grupo1.model.Picture;
 import ar.edu.itba.paw.grupo1.model.Property;
 import ar.edu.itba.paw.grupo1.model.Room;
 import ar.edu.itba.paw.grupo1.model.User;
 import ar.edu.itba.paw.grupo1.repository.AdRepository;
 import ar.edu.itba.paw.grupo1.repository.PictureRepository;
 import ar.edu.itba.paw.grupo1.repository.UserRepository;
 import ar.edu.itba.paw.grupo1.web.WicketSession;
 import ar.edu.itba.paw.grupo1.web.WicketUtils;
 import ar.edu.itba.paw.grupo1.web.Brokers.BrokersPage;
 import ar.edu.itba.paw.grupo1.web.Home.HomePage;
 import ar.edu.itba.paw.grupo1.web.Login.LoginPage;
 import ar.edu.itba.paw.grupo1.web.PropertyList.PropertyListPage;
 import ar.edu.itba.paw.grupo1.web.Query.QueryPage;
 
 @SuppressWarnings("serial")
 public abstract class BasePage extends WebPage {
 
 	@SpringBean
 	private UserRepository users;
 	
 	@SpringBean
 	private AdRepository adRepository;
 	
 	private List<Link<Void>> sectionsList = new ArrayList<Link<Void>>();
 	
 	public BasePage() {
 		initLinks();
 
 		BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("logo", HomePage.class);
 		link.add(new Image("searchIcon", new ContextRelativeResource("/images/arqvengers.png")));
 		add(link);
 		
 		add(new ListView<Link<Void>>("linkList", new PropertyModel<List<Link<Void>>>(this, "sectionsList")) {
 			@Override
 			protected void populateItem(ListItem<Link<Void>> item) {
 				item.add(item.getModelObject());
 			}
 		});
 		
 		BookmarkablePageLink<Void> myPropertiesLink = new BookmarkablePageLink<Void>("MyProperties", PropertyListPage.class);
 		addLabelToLink(myPropertiesLink, getLocalizer().getString("myProperties", this));
 		add(myPropertiesLink, isSignedIn());
 		
 		BookmarkablePageLink<Void> loginLink = new BookmarkablePageLink<Void>("baseLoginLink", LoginPage.class);
 		addLabelToLink(loginLink,  getLocalizer().getString("login", this));
 		add(loginLink, !isSignedIn());
 		
 		Link<Void> logoutLink = new Link<Void>("baseLogoutLink") {
 
 			@Override
 			public void onClick() {
 				signOut();
 				setResponsePage(HomePage.class);
 			}
 		};
 		addLabelToLink(logoutLink, getLocalizer().getString("logout", this));
 		add(logoutLink, isSignedIn());
 		
 		String username = isSignedIn()?getSignedInUser().getUsername():"";
 		add(new Label("username", username), isSignedIn());
 	}
 	
 	
 	@Override
 	protected void onBeforeRender() {
 		super.onBeforeRender();
 		showAd();
 	}
 	
 	private void showAd() {
 		
 		final Ad ad = adRepository.getRandomAd();
 		
 		if (get("banner") != null) {
 			remove("banner");
 		}
 		
 		Component banner;
 
 		if (ad != null) {
 			banner = new WebMarkupContainer("banner"); 
 			banner.add(new AttributeModifier("src", new Model<String>(ad.getUrl()))); 
 		} else {
 			banner = new Image("banner", (IModel<?>) null);
 			banner.setVisible(false);
 		}
 
 		add(banner);
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initLinks() {
 		
 		Class<?> pages[] = {HomePage.class, QueryPage.class, BrokersPage.class};
 		Map<String, String> labels = new HashMap<String, String>();
 		labels.put("HomePage", getLocalizer().getString("home", this));
 		labels.put("QueryPage", getLocalizer().getString("search", this));
 		labels.put("BrokersPage", getLocalizer().getString("brokers", this));
 
 		
 		for(Class<?> page: pages) {
 			BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("link", (Class<? extends Page>) page);
 			addLabelToLink(link, labels.get(page.getSimpleName()));
 			if (this.getClass() == page) {
 				link.add(new AttributeModifier("class", "active"));
 			}
 			sectionsList.add(link);
 		}
 	}
 
 	protected <T> void addLabelToLink(Link<T> link, String labelValue) {
 		link.add(new Label("label", labelValue));		
 	}
 
 	public void renderHead(IHeaderResponse response) {
 //	    response.renderCSSReference(new PackageResourceReference(BasePage.class,
 //	      "main.css"));
 	    response.renderCSSReference("css/bootstrap.min.css");
 	}
 	
 	protected boolean isSignedIn() {
 		return WicketSession.get().isSignedIn();
 	}
 	
 	protected User getSignedInUser() {
 		
 		if (isSignedIn()) {
 			return users.get(WicketSession.get().getUserId());
 		} else {
 			return null;
 		}
 	}
 	
 	protected void signOut() {
 		WicketSession.get().signOut(getRequest(), getResponse());
 	}
 	
 	protected boolean isMine(Owned obj) {
 
 		if (isSignedIn() && obj.getUser() != null) {
 			return getSignedInUser().getId().equals(obj.getUser().getId());
 		}
 		
 		return false;
 	}
 	
 	protected void add(Component c, boolean visibilityCondition) {
 		WicketUtils.addToContainer(this, c, visibilityCondition);
 	}
 	
 	protected IModel<List<Room>> initRoomsModel(final IModel<Property> model) {
 		
 		IModel<List<Room>> roomsModel = new LoadableDetachableModel<List<Room>>() {
 			@Override
 			protected List<Room> load() {
 				return new ArrayList<Room>(model.getObject().getRooms()); 
 			}
 		};
 		return roomsModel;
 	}
 	
 	protected IModel<List<Picture>> initPicturesModel(final IModel<Property> model, final PictureRepository pictures) {
 		
 		IModel<List<Picture>> picturesModel = new LoadableDetachableModel<List<Picture>>() {
 			@Override
 			protected List<Picture> load() {
 				return new ArrayList<Picture>(pictures.getPictures(model.getObject())); 
 			}
 		};
 		return picturesModel;
 	}
 	
 	protected void addRoomsView(Set<Room> rooms, IModel<List<Room>> roomsModel) {
 		
 		ListView<Room> roomsView = new ListView<Room>("rooms", roomsModel) {
 
 			@Override
 			protected void populateItem(ListItem<Room> item) {
 				Room room = item.getModelObject();
 				item.add(new Label("label", getLocalizer().getString(room.getRoom().toString(), this)));
 				item.add(new Label("length", Double.toString(room.getLength())));
 				item.add(new Label("width", Double.toString(room.getWidth())));
 			}
 		};
 		add(roomsView, rooms != null && !rooms.isEmpty());
 	}
 	
 	protected void addLabel(String id, String label, boolean visibilityCondition) {
 		add(new Label(id, label), visibilityCondition);	
 	}
 
	protected void addLabel(String id, String key, IModel<Property> model, boolean visibilityCondition) {
 		add(new Label(id, getLocalizer().getString(key, this, model)), visibilityCondition);				
 	}
 
 	protected void addLabel(String id, boolean visibilityCondition) {
 		addLabel(id, id, null, visibilityCondition);
 	}
 	
 }
