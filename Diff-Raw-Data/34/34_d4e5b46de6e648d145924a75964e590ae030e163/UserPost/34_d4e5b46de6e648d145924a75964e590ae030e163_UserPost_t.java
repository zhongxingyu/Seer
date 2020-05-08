 package com.cs1530.group4.addendum.client;
 
 import java.sql.Date;
 
 import com.cs1530.group4.addendum.shared.Comment;
 import com.cs1530.group4.addendum.shared.Post;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.i18n.shared.DateTimeFormat;
 import com.google.gwt.i18n.shared.DefaultDateTimeFormatInfo;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
public class UserPost extends Composite implements MouseOverHandler, MouseOutHandler, ClickHandler
 {
 	UserServiceAsync userService = UserService.Util.getInstance();
 	int upDownVotes;
 	VerticalPanel commentPanel;
 	Image menu;
 	MenuPopup popup = null;
 	String loggedInUser = Cookies.getCookie("loggedIn");
 	Stream profile;
 
 	public UserPost(final MainView main, final Stream profile, final Post post)
 	{
 		this.profile = profile;
 		upDownVotes = post.getUpvotes() - post.getDownvotes();
 		HorizontalPanel border = new HorizontalPanel();
 		border.setBorderWidth(1);
 		border.setWidth("100%");
 		initWidget(border);
 
 		VerticalPanel scorePanel = new VerticalPanel();
 		scorePanel.getElement().getStyle().setProperty("marginLeft", "5px");
 		scorePanel.getElement().getStyle().setProperty("marginRight", "5px");
 		border.add(scorePanel);
 		border.setCellHorizontalAlignment(scorePanel, HasHorizontalAlignment.ALIGN_CENTER);
 		border.setCellWidth(scorePanel, "34px");
 
 		final Image upArrow = new Image("images/default_up.png");
 		final Image downArrow = new Image("images/default_down.png");
 		if(post.isUpvoted())
 			upArrow.setUrl("images/voted_up.png");
 		if(post.isDownvoted())
 			downArrow.setUrl("images/voted_down.png");
 		final Label scoreLabel = new Label(String.valueOf(upDownVotes));
 		upArrow.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 				{
 					@Override
 					public void onFailure(Throwable caught)
 					{
 					}
 
 					@Override
 					public void onSuccess(Boolean success)
 					{
 						if(success) //user has not upvoted before or they have downvoted before
 						{
 							if(post.isDownvoted())
 								upDownVotes++;
 							post.setDownvoted(false);
 							downArrow.setUrl("images/default_down.png");
 							if(post.isUpvoted())
 							{
 								scoreLabel.setText(String.valueOf(--upDownVotes));
 								post.setUpvoted(false);
 								upArrow.setUrl("images/default_up.png");
 							}
 							else
 							{
 								scoreLabel.setText(String.valueOf(++upDownVotes));
 								post.setUpvoted(true);
 								upArrow.setUrl("images/voted_up.png");
 							}
 						}
 						else //user has upvoted before - undo it
 						{
 							scoreLabel.setText(String.valueOf(--upDownVotes));
 							post.setUpvoted(false);
 							upArrow.setUrl("images/default_up.png");
 						}
 					}
 				};
 				userService.upvotePost(post.getPostKey(), Cookies.getCookie("loggedIn"), callback);
 			}
 		});
 		downArrow.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>()
 				{
 					@Override
 					public void onFailure(Throwable caught)
 					{
 					}
 
 					@Override
 					public void onSuccess(Boolean success)
 					{
 						if(success)
 						{
 							if(post.isUpvoted())
 								upDownVotes--;
 							post.setUpvoted(false);
 							upArrow.setUrl("images/default_up.png");
 							if(post.isDownvoted())
 							{
 								scoreLabel.setText(String.valueOf(++upDownVotes));
 								post.setDownvoted(false);
 								downArrow.setUrl("images/default_down.png");
 							}
 							else
 							{
 								scoreLabel.setText(String.valueOf(--upDownVotes));
 								post.setDownvoted(true);
 								downArrow.setUrl("images/voted_down.png");
 							}
 						}
 						else
 						{
 							post.setDownvoted(false);
 							scoreLabel.setText(String.valueOf(++upDownVotes));
 							downArrow.setUrl("images/default_down.png");
 						}
 					}
 				};
 				userService.downvotePost(post.getPostKey(), Cookies.getCookie("loggedIn"), callback);
 			}
 		});
 		scorePanel.add(upArrow);
 		upArrow.setSize("24px", "24px");
 
 		scoreLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		scorePanel.add(scoreLabel);
 
 		scorePanel.add(downArrow);
 		downArrow.setSize("24px", "24px");
 
 		VerticalPanel postPanel = new VerticalPanel();
 		postPanel.setWidth("100%");
 		border.add(postPanel);
 		
 		String label = post.getStreamLevel();
 		if(label.equals("all"))
 			label = "Everyone";
 		Label lblNewLabel = new Label(label);
 		lblNewLabel.setStyleName("postSpaceTitle");
 		lblNewLabel.getElement().getStyle().setProperty("marginLeft", "10px");
 		lblNewLabel.getElement().getStyle().setProperty("marginTop", "5px");
 		postPanel.add(lblNewLabel);
 		
 		HTML html = new HTML("<hr  style=\"width:100%;\" />");
 		postPanel.add(html);
 
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		postPanel.add(horizontalPanel);
 		horizontalPanel.setWidth("100%");
 
 		HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
 		horizontalPanel_2.setStyleName("PostComment");
 		horizontalPanel.add(horizontalPanel_2);
 
 		Image image = new Image("/addendum/getImage?username=" + post.getUsername());
 		horizontalPanel_2.add(image);
 		image.getElement().getStyle().setProperty("marginRight", "10px");
 		image.setSize("46px", "46px");
 
 		VerticalPanel verticalPanel = new VerticalPanel();
 		horizontalPanel_2.add(verticalPanel);
 
 		Anchor usernameLabel = new Anchor(post.getUsername());
 		if(profile != null)
 		{
 			usernameLabel.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					profile.postSearch("username:"+post.getUsername());
 				}
 			});
 		}
 		usernameLabel.setStyleName("gwt-Label-bold");
 		verticalPanel.add(usernameLabel);
 
 		String timeFormatString = "h:mm a";
 		String editFormatString = "h:mm a";
 		Date now = new Date(System.currentTimeMillis());
 		if(post.getPostTime().getDate() != now.getDate())
 			timeFormatString = "MMM d, yyyy";
 		if(post.getLastEdit() != null && post.getLastEdit().getDate() != now.getDate())
 			editFormatString = "MMM d, yyyy";
 		DateTimeFormat dtf = new DateTimeFormat(timeFormatString, new DefaultDateTimeFormatInfo())
 		{
 		};
 		DateTimeFormat editDtf = new DateTimeFormat(editFormatString, new DefaultDateTimeFormatInfo())
 		{
 		};
 		String timeLabel = dtf.format(post.getPostTime());
 		if(post.getLastEdit() != null)
 			timeLabel += " (last edit - " + editDtf.format(post.getLastEdit()) + ")";
 		Label lblPosttime = new Label(timeLabel);
 		lblPosttime.setStyleName("gwt-Label-grey");
 		verticalPanel.add(lblPosttime);
 
 		HorizontalPanel menuPanel = new HorizontalPanel();
 		menuPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
 		horizontalPanel.add(menuPanel);
 		menuPanel.setWidth("100%");
 
 		if(main != null)
 		{
 			menu = new Image("images/menu.png");
 			menu.setSize("24px", "24px");
 			menu.setVisible(false);
 			menu.getElement().getStyle().setProperty("marginRight", "5px");
 			popup = new MenuPopup(main,menu,post,post.getUsername().equals(loggedInUser));
 			menu.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
					
 				}
 			});
 			menuPanel.add(menu);
 			
 			addDomHandler(this, MouseOverEvent.getType());
 			addDomHandler(this, MouseOutEvent.getType());
 		}
 
 		HTML postContent = new HTML(post.getPostContent());
 		postContent.setStyleName("userpostcomment");
 		postPanel.add(postContent);
 
 		HTML separator = new HTML("<hr  style=\"width:100%;\" />");
 		postPanel.add(separator);
 
 		commentPanel = new VerticalPanel();
 		commentPanel.setSpacing(5);
 		commentPanel.setWidth("100%");
 		if(post.getComments().size() > 0)
 			commentPanel.setStyleName("CommentPanelbackcolor");
 		postPanel.add(commentPanel);
 		if(post.getComments() != null)
 		{
 			for(Comment comment : post.getComments())
 				commentPanel.add(new PostComment(comment,profile));
 		}
 
 		VerticalPanel addCommentPanel = new VerticalPanel();
 		postPanel.add(addCommentPanel);
 		addCommentPanel.setWidth("100%");
 
 		final PromptedTextBox addAComment = new PromptedTextBox("Add a comment...", "promptText");
 		addAComment.getElement().getStyle().setProperty("margin", "10px");
 		final CommentBox commentBox = new CommentBox(addAComment, post, this);
 		commentBox.setVisible(false);
 		addCommentPanel.add(commentBox);
 		commentBox.setWidth("100%");
 		addAComment.addClickHandler(new ClickHandler()
 		{
 			@Override
 			public void onClick(ClickEvent event)
 			{
 				addAComment.setVisible(false);
 				commentBox.setVisible(true);
 				commentBox.textArea.setFocus(true);
 				commentBox.textArea.setText("");
 			}
 		});
 		addCommentPanel.add(addAComment);
		
		addDomHandler(this,ClickEvent.getType());
 	}
 
 	public void addSubmittedComment(Comment comment)
 	{
 		commentPanel.add(new PostComment(comment,profile));
 		commentPanel.setStyleName("gwt-DecoratorPanel-newComment");
 	}
 
 	@Override
 	public void onMouseOut(MouseOutEvent event)
 	{
 		menu.setVisible(false);
 	}
 
 	@Override
 	public void onMouseOver(MouseOverEvent event)
 	{
 		menu.setVisible(true);
 	}
	
	@Override
	public void onClick(ClickEvent event) //hack to make post menu work
	{
		int x = event.getClientX(), y = event.getClientY();
		if(x >= menu.getAbsoluteLeft() && x <= menu.getAbsoluteLeft()+menu.getWidth() &&
				event.getClientY() >= menu.getAbsoluteTop() && y <= menu.getAbsoluteTop()+menu.getHeight())
		{
			if(popup.isShowing())
				popup.hide();
			else
				popup.showRelativeTo(menu);
		}
		else
			popup.hide();
	}
 }
