 package com.cs1530.group4.addendum.client;
 
 import java.util.ArrayList;
 
 import com.cs1530.group4.addendum.shared.Post;
 import com.cs1530.group4.addendum.shared.User;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RadioButton;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MenuPopup extends PopupPanel implements MouseOverHandler, MouseOutHandler
 {
 	UserServiceAsync userService = UserService.Util.getInstance();
 	MenuPopup popup = this;
 	Widget relativeWidget;
 
 	public MenuPopup(final MainView main, Widget w, final Post post, boolean isUser)
 	{
		super(true);
 		setStyleName("MenuPopUp");
 		relativeWidget = w;
 		VerticalPanel vPanel = new VerticalPanel();
 		vPanel.setSpacing(5);
 		if(isUser)
 		{
 			Label editPost = new Label("Edit Post");
 			editPost.setStyleName("menuitem");
 			editPost.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					popup.hide();
 					ArrayList<String> originalStream = new ArrayList<String>();
 					originalStream.add(post.getStreamLevel());
 					NewPost editor = new NewPost(main, originalStream, post);
 					editor.show();
 				}
 			});
 			Label deletePost = new Label("Delete Post");
 			deletePost.setStyleName("menuitem");
 			deletePost.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					popup.hide();
 					if(Window.confirm("Are you sure you want to delete this post?"))
 					{
 						AsyncCallback<Void> callback = new AsyncCallback<Void>()
 						{
 							@Override
 							public void onFailure(Throwable caught)
 							{
 							}
 
 							@Override
 							public void onSuccess(Void v)
 							{
 								Storage localStorage = Storage.getLocalStorageIfSupported();
 								User user = new User(Cookies.getCookie("loggedIn"));
 								if(localStorage.getItem("loggedIn") != null)
 									user = User.deserialize(localStorage.getItem("loggedIn"));
 								main.setContent(new Stream(main, user), "profile-" + user);
 							}
 						};
 						userService.deletePost(post.getPostKey(), callback);
 					}
 				}
 			});
 
 			vPanel.add(editPost);
 			vPanel.add(deletePost);
 		}
 		else
 		{
 			Label flagPost = new Label("Report Post");
 			flagPost.setStyleName("menuitem");
 			flagPost.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					popup.hide();
 					new FlagPostForm(post.getPostKey());
 				}
 			});
 			vPanel.add(flagPost);
 		}
 		addDomHandler(this, MouseOutEvent.getType());
 		addDomHandler(this, MouseOverEvent.getType());
 
 		add(vPanel);
 	}
 
 	@Override
 	public void onMouseOut(MouseOutEvent event)
 	{
 		relativeWidget.setVisible(false);
 	}
 
 	@Override
 	public void onMouseOver(MouseOverEvent event)
 	{
 		relativeWidget.setVisible(true);
 	}
 
 	private class FlagPostForm extends DialogBox
 	{
 		FlagPostForm form = this;
 		String[] options = {"Unwanted commercial content or spam","Pornography or sexually explicit material","Hate speech or graphic violence","Harassment or bullying","This account might be compromised or hacked","Other"};
 		
 		public FlagPostForm(final String postKey)
 		{
 			final ArrayList<RadioButton> radios = new ArrayList<RadioButton>();
 			VerticalPanel vPanel = new VerticalPanel();
 			vPanel.add(new Label("What kind of abuse are you reporting?"));
 			for(String option : options)
 			{
 				RadioButton button = new RadioButton("options", option);
 				radios.add(button);
 				vPanel.add(button);
 			}
 			vPanel.add(new Label("Any additional remarks?"));
 			vPanel.add(new TextBox());
 			HorizontalPanel buttonPanel = new HorizontalPanel();
 			Button cancelButton = new Button("Cancel");
 			buttonPanel.add(cancelButton);
 			cancelButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					form.hide();
 				}
 			});
 			Button okButton = new Button("OK");
 			buttonPanel.add(okButton);
 			okButton.addClickHandler(new ClickHandler()
 			{
 				public void onClick(ClickEvent event)
 				{
 					AsyncCallback<Void> callback = new AsyncCallback<Void>()
 					{
 						@Override
 						public void onFailure(Throwable caught){}
 						@Override
 						public void onSuccess(Void v){}
 					};
 					String reason = "";
 					for(RadioButton button : radios)
 					{
 						if(button.getValue())
 							reason = button.getText();
 					}
 					userService.flagPost(postKey, reason, true, callback);
 				}
 			});
 			vPanel.add(buttonPanel);
 			add(vPanel);
 			form.setGlassEnabled(true);
 			form.center();
 		}
 	}
 }
