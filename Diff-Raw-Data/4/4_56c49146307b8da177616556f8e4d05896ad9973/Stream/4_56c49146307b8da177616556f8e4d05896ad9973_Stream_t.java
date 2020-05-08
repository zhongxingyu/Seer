 package com.cs1530.group4.addendum.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import com.cs1530.group4.addendum.shared.Post;
 import com.cs1530.group4.addendum.shared.User;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TabPanel;
 import com.google.gwt.user.client.ui.ValueBoxBase.TextAlignment;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 public class Stream extends Composite
 {
 	MainView main;
 	String username, streamLevel = "all", sortMethod = "Popular";
 	User user;
 	UserServiceAsync userService = UserService.Util.getInstance();
 	VerticalPanel vPanel, currentTab;
 	int startIndex = 0;
 	Anchor nextPage, prevPage;
 	ArrayList<String> userCourses;
 	TabPanel tabPanel;
 	VerticalPanel searchPanel;
 	Stream profile = this;
 
 	public Stream(MainView m, User u)
 	{
 		main = m;
 		username = u.getUsername();
 		user = u;
 		userCourses = u.getCourseList();
 		vPanel = new VerticalPanel();
 		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		vPanel.getElement().getStyle().setProperty("marginBottom", "10px");
 
 		initWidget(vPanel);
 		vPanel.setSize("406px", "162px");
 
 		Grid grid = new Grid(1, 2);
 		vPanel.add(grid);
 		vPanel.setCellHorizontalAlignment(grid, HasHorizontalAlignment.ALIGN_RIGHT);
 
 		Button createPost = new Button("Create a new post");
 		grid.setWidget(0, 0, createPost);
 		createPost.setHeight("30px");
 		createPost.setStyleName("ADCButton");
 		createPost.addClickHandler(new ClickHandler()
 		{
 			public void onClick(ClickEvent event)
 			{
 				NewPost editor = new NewPost(main, userCourses, null);
 				editor.show();
 			}
 		});
 
 		final PromptedTextBox searchBox = new PromptedTextBox("Search for a post...", "promptText");
 		grid.setWidget(0, 1, searchBox);
 		searchBox.setHeight("25px");
 		searchBox.setAlignment(TextAlignment.CENTER);
 		searchBox.setStyleName("profileSearchbox");
 		searchBox.addKeyPressHandler(new KeyPressHandler()
 		{
 			@Override
 			public void onKeyPress(KeyPressEvent event)
 			{
 				if(event.getCharCode() == KeyCodes.KEY_ENTER)
 				{
 					postSearch(searchBox.getText());
 				}
 			}
 		});
 
 		HorizontalPanel hPanel = new HorizontalPanel();
 		hPanel.setStyleName("profileMainPanel");
 		vPanel.add(hPanel);
 
 		VerticalPanel userPanel = new VerticalPanel();
 		userPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		userPanel.getElement().getStyle().setProperty("marginRight", "30px");
 		hPanel.add(userPanel);
 
 		AbsolutePanel absolutePanel = new AbsolutePanel();
 		absolutePanel.setStyleName("profilePic");
 		absolutePanel.setSize("128px", "128px");
 		userPanel.add(absolutePanel);
 
 		Image image = new Image("/addendum/getImage?username=" + username);
 		final Label changeImageLabel = new Label("Change Image");
 		changeImageLabel.setStyleName("gwt-DecoratorPanel-white");
 		changeImageLabel.setSize("128px", "28px");
 		MouseOverHandler mouseOver = new MouseOverHandler()
 		{
 			@Override
 			public void onMouseOver(MouseOverEvent event)
 			{
 				changeImageLabel.setVisible(true);
 			}
 		};
 		MouseOutHandler mouseOut = new MouseOutHandler()
 		{
 			@Override
 			public void onMouseOut(MouseOutEvent event)
 			{
 				changeImageLabel.setVisible(false);
 			}
 		};
 		ClickHandler changePictureHandler = new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				ProfilePictureUpload profilePic = new ProfilePictureUpload(username);
 				profilePic.show();
 			}
 		};
 		image.addMouseOverHandler(mouseOver);
 		changeImageLabel.addMouseOverHandler(mouseOver);
 		image.addMouseOutHandler(mouseOut);
 		changeImageLabel.addMouseOutHandler(mouseOut);
 		image.addClickHandler(changePictureHandler);
 		changeImageLabel.addClickHandler(changePictureHandler);
 
 		absolutePanel.add(image);
 		image.setSize("128px", "128px");
 
 		changeImageLabel.setVisible(false);
 		absolutePanel.add(changeImageLabel, -5, 105);
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		userPanel.add(horizontalPanel);
 
 		Label usernameLabel = new Label(username);
 		usernameLabel.setStyleName("USername");
 		horizontalPanel.add(usernameLabel);
 
 		Anchor logoutAnchor = new Anchor("Logout");
 		logoutAnchor.setStyleName("logout");
 		horizontalPanel.add(logoutAnchor);
 		logoutAnchor.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				Cookies.removeCookie("loggedIn");
 				Storage localStorage = Storage.getLocalStorageIfSupported();
 				localStorage.removeItem("loggedIn");
 				main.setContent(new Login(main), "login");
 			}
 		});
 
 		VerticalPanel classPanel = new VerticalPanel();
 		classPanel.setStyleName("Anchor");
 		classPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		classPanel.setSpacing(3);
 		userPanel.add(classPanel);
 
 		Button addRemove = new Button("Add/Remove Classes");
 		addRemove.setStyleName("ADCButton-addRemoveClasses");
 		addRemove.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				new ClassSearch(main);
 			}
 		});
 		Anchor allAnchor = new Anchor("All Classes");
 		allAnchor.setText("ALL CLASSES");
 		allAnchor.setStyleName("courseAnchor");
 		allAnchor.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		allAnchor.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				streamLevel = "all";
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		});
 
 		tabPanel = new TabPanel();
 		hPanel.add(tabPanel);
 		tabPanel.setSize("800px", "89");
 
 		VerticalPanel popularUpdatesPanel = new VerticalPanel();
 		popularUpdatesPanel.setWidth("100%");
 		VerticalPanel newUpdatesPanel = new VerticalPanel();
 		newUpdatesPanel.setWidth("100%");
 		tabPanel.add(popularUpdatesPanel, "Popular", false);
		tabPanel.selectTab(0);
 		tabPanel.add(newUpdatesPanel, "New", false);
 		tabPanel.addSelectionHandler(new SelectionHandler<Integer>()
 		{
 			@Override
 			public void onSelection(SelectionEvent<Integer> event)
 			{
 				if(tabPanel.getTabBar().getTabHTML(event.getSelectedItem()).equals("Search Results"))
 					return;
 
 				sortMethod = tabPanel.getTabBar().getTabHTML(event.getSelectedItem());
 				currentTab = (VerticalPanel) tabPanel.getWidget(event.getSelectedItem());
 				if(userCourses != null)
 				{
 					ArrayList<String> streamLevels = new ArrayList<String>();
 					streamLevels.add(streamLevel);
 					if(streamLevel.equals("all"))
 						streamLevels.addAll(userCourses);
 					getPosts(currentTab, streamLevels, sortMethod);
 				}
 			}
 		});
 		popularUpdatesPanel.setSpacing(15);
 		newUpdatesPanel.setSpacing(15);
 
 		getClasses(classPanel, addRemove, allAnchor);
 		tabPanel.selectTab(0);
 		
 		HorizontalPanel nextPrevPanel = new HorizontalPanel();
 		nextPage = new Anchor("Next 10 Posts");
 		nextPage.setStyleName("courseAnchor");
 		nextPage.setVisible(false);
 		nextPage.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				prevPage.setVisible(true);
 				startIndex += 10;
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				if(streamLevel.equals("all"))
 					streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		});
 		prevPage = new Anchor("Prev 10 Posts");
 		prevPage.setStyleName("courseAnchor");
 		prevPage.setVisible(false);
 		prevPage.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				nextPage.setVisible(true);
 				startIndex -= 10;
 				if(startIndex < 10)
 					prevPage.setVisible(false);
 				ArrayList<String> streamLevels = new ArrayList<String>();
 				streamLevels.add(streamLevel);
 				if(streamLevel.equals("all"))
 					streamLevels.addAll(userCourses);
 				getPosts(currentTab, streamLevels, sortMethod);
 			}
 		});
 		nextPrevPanel.add(prevPage);
 		nextPrevPanel.add(nextPage);
 		vPanel.add(nextPrevPanel);
 		setStyleName("profilePanel");
 	}
 
 	private void getPosts(final VerticalPanel updatesPanel, ArrayList<String> streamLevels, final String sortMethod)
	{		
 		updatesPanel.clear();
 		AsyncCallback<ArrayList<Post>> callback = new AsyncCallback<ArrayList<Post>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 			}
 
 			@Override
 			public void onSuccess(ArrayList<Post> posts)
 			{
 				if(sortMethod.equals("Popular"))
 					Collections.sort(posts, Post.PostScoreComparator);
 				if(sortMethod.equals("New"))
 					Collections.sort(posts, Post.PostTimeComparator);
 
 				for(Post post : posts)
 				{
 					updatesPanel.add(new UserPost(main, profile, post));
 				}
 				if(posts.size() == 10)
 					nextPage.setVisible(true);
 			}
 		};
 		userService.getPosts(startIndex, streamLevels, username, callback);
 	}
 
 	private void getClasses(final VerticalPanel classPanel, final Button addRemove, final Anchor allAnchor)
 	{
 		userCourses = user.getCourseList();
 		classPanel.clear();
 		classPanel.add(allAnchor);
 		for(final String course : userCourses)
 		{
 			Anchor courseAnchor = new Anchor(course);
 			courseAnchor.setStyleName("courseAnchor");
 			courseAnchor.addClickHandler(new ClickHandler()
 			{
 				@Override
 				public void onClick(ClickEvent event)
 				{
 					streamLevel = course.trim();
 					ArrayList<String> streamLevels = new ArrayList<String>();
 					streamLevels.add(streamLevel);
 					getPosts(currentTab, streamLevels, sortMethod);
 				}
 			});
 			classPanel.add(courseAnchor);
 		}
 		classPanel.add(addRemove);
 	}
 
 	public void postSearch(final String searchString)
 	{
 		AsyncCallback<ArrayList<Post>> callback = new AsyncCallback<ArrayList<Post>>()
 		{
 			@Override
 			public void onFailure(Throwable caught)
 			{
 			}
 
 			@Override
 			public void onSuccess(ArrayList<Post> posts)
 			{
 				if(searchPanel == null)
 				{
 					searchPanel = new VerticalPanel();
 					searchPanel.setWidth("600px");
 					searchPanel.setSpacing(15);
 					tabPanel.add(searchPanel, "Search Results");
 				}
 				if(posts.size() == 0)
 				{
 					searchPanel.clear();
 					searchPanel.add(new Label("No results found for '" + searchString + "'"));
 					return;
 				}
 				tabPanel.selectTab(2);
 				searchPanel.clear();
 
 				Collections.sort(posts, Post.PostScoreComparator);
 				for(Post post : posts)
 				{
 					searchPanel.add(new UserPost(main, profile, post));
 				}
 				if(posts.size() == 10)
 					nextPage.setVisible(true);
 			}
 		};
 		userService.postSearch(searchString, username, callback);
 	}
 }
