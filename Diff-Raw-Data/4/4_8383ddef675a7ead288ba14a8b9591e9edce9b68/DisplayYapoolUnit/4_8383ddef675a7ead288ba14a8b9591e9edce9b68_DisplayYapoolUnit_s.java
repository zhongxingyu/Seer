 package com.jsar.client.unit;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.jsar.client.YapoolGWT;
 import com.jsar.client.http.AbstractRequestCallback;
 import com.jsar.client.http.HttpInterface;
 import com.jsar.client.json.ProfileJson;
 import com.jsar.client.json.RestaurantJson;
 import com.jsar.client.json.ViewJson;
 import com.jsar.client.json.PostJson;
 import com.jsar.client.json.YapoolJson;
 
 /**
  * 
  * @author jangho, rem
  * 
  */
 public class DisplayYapoolUnit extends AbstractUnit {
 
   public static DisplayYapoolUnit displayYapoolUnit;
   private Label yapoolNameLabel;
   private FlexTable postListTable;
   private PostJson tempMessage = null;
   private String currentYapoolId;
   private Button joinButton;
   private Button leaveButton;
   private Button closeButton;
   private Button doneButton;
   private ScrollPanel postScrollPanel;
   
   private Label restaurantNameLabel;
   private Label pickUpPlaceLabel;
   private Label orderTimeLabel;
 
   private TextBox messageInput;
   private FlexTable memberListTable;
   private YapoolJson currentYapool;
   private Label stateLabel;
 
   /**
    * constructor class
    */
   public DisplayYapoolUnit() {
     DisplayYapoolUnit.displayYapoolUnit = this;
     yapoolNameLabel = new Label();
     yapoolNameLabel.getElement().setClassName("displayYapoolName");
     postListTable = new FlexTable();
     postListTable.getElement().setClassName("displayYapoolPostTable");
     joinButton = new Button("Join this YaPooL!");
     joinButton.getElement().setClassName("displayYapoolButton");
     leaveButton = new Button("Leave this YaPooL!");
     leaveButton.getElement().setClassName("displayYapoolButton");
     stateLabel = new Label();
 
     closeButton = new Button("Close");
     doneButton = new Button("Done");
 
     messageInput = new TextBox();
     ScrollPanel memberScrollPanel = new ScrollPanel();
 
     memberListTable = new FlexTable();
     currentYapool = new YapoolJson();
     
     restaurantNameLabel = new Label();
     pickUpPlaceLabel = new Label();
     orderTimeLabel = new Label();
     Label spaceLabel = new Label("");
     
     joinButton.setVisible(false);
     leaveButton.setVisible(false);
     messageInput.setVisible(false);
     closeButton.setVisible(false);
     doneButton.setVisible(false);
 
     RootPanel.get("displayYapoolName").add(yapoolNameLabel);
 
     memberScrollPanel.add(memberListTable);
     memberScrollPanel.setHeight("80px");
     memberScrollPanel.setWidth("80px");
     memberScrollPanel.getElement().setClassName(
 	memberScrollPanel.getElement().getClassName() + "displayYapoolMemberTable");
     RootPanel.get("displayYapoolContent").add(memberScrollPanel);
     RootPanel.get("displayYapoolContent").add(stateLabel);
     RootPanel.get("displayYapoolContent").add(joinButton);
     RootPanel.get("displayYapoolContent").add(leaveButton);
     RootPanel.get("displayYapoolContent").add(closeButton);
     RootPanel.get("displayYapoolContent").add(doneButton);
     //RootPanel.get("displayYapoolContent").add(spaceLabel);
     
 
     //VerticalPanel verticalPanel = new VerticalPanel();
 
     postScrollPanel = new ScrollPanel();
     postScrollPanel.add(postListTable);
     postScrollPanel.setVisible(true);
     postScrollPanel.setHeight("250px");
     postScrollPanel.setWidth("500px");
     Label chatLabel = new Label("Chat With the Members");
     chatLabel.getElement().setClassName("displayYapoolChatLabel");
     //verticalPanel.add(postScrollPanel);
     //verticalPanel.add(messageInput);
     RootPanel.get("displayYapoolWall").add(restaurantNameLabel);
     RootPanel.get("displayYapoolWall").add(pickUpPlaceLabel);
     RootPanel.get("displayYapoolWall").add(orderTimeLabel);
     RootPanel.get("displayYapoolWall").add(chatLabel);
     RootPanel.get("displayYapoolWall").add(postScrollPanel);
     // RootPanel.get("displayYapoolWall")
 
     messageInput.addKeyDownHandler(new PostMessageKeydowndHandler());
     joinButton.addClickHandler(new JoinYapoolClickHandler());
     leaveButton.addClickHandler(new LeaveButtonClickHandler());
     closeButton.addClickHandler(new CloseButtonClickHandler());
     doneButton.addClickHandler(new DoneButtonClickHandler());
 
     this.SetVisible(false);
   }
 
   public void displayYapool(String yapoolId) {
     lastPostCount=-1;
     NavigationUnit.navigationUnit.hideAll();
     this.SetVisible(true);
     currentYapoolId = yapoolId;
     postListTable.removeAllRows();
 
     HttpInterface.doGet("/yapooldb/" + currentYapoolId, new LoadYapoolRequestCallback());
     HttpInterface.doGet("/yapooldb/_design/post/_view/by_yapoolId?key=\"" + yapoolId + "\"",
 	new DisplayYapoolPostRequestCallback());
   }
 
   public class PostMessageKeydowndHandler implements KeyDownHandler {
     @Override
     public void onKeyDown(KeyDownEvent event) {
       if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
 	if (messageInput.getText() == "")
 	  return;
 	PostJson newPost = new PostJson();
 	newPost.setMessage(messageInput.getText());
 	newPost.setYapoolId(currentYapoolId);
 	if (YapoolGWT.currentSession.getName() == null) {
 	  Window.alert("You need to log in first!");
 	  return;
 	}
 	newPost.setUser(YapoolGWT.currentSession.getName());
 	messageInput.setText("");
 	
 	tempMessage = newPost;
 	HttpInterface.doPostJson("/yapooldb/", newPost, new WritePostRequestCallback());
       }
     }
   }
 
   public class JoinYapoolClickHandler implements ClickHandler {
     @Override
     public void onClick(ClickEvent event) {
       HttpInterface.doGet("/yapooldb/" + YapoolGWT.currentSession.getName() + "/", new AbstractRequestCallback() {
 	@Override
 	public void onResponseReceived(Request request, Response response) {
 	  ProfileJson profile = new ProfileJson(response.getText());
 	  if (profile.getCurrentYapool().equals("")) {
 	    joinButton.setVisible(false);
 
 	    // Update Profile - currentYapool to
 	    // CurrentYapool
 	    profile.setCurrentYapool(currentYapoolId);
 	    HttpInterface.doPostJson("/yapooldb/", profile, new AbstractRequestCallback() {
 	      @Override
 	      public void onResponseReceived(Request request, Response response) {
 
 		System.out.println(response.toString());
 		System.out.println("Current Yapool is set Successfully");
 	      }
 	    }); // http doPostJson Ends
 	    YapoolGWT.currentProfile = profile;
 
 	    // Update Yapool - Add this user to the Yapool
 	    HttpInterface.doGet("/yapooldb/" + currentYapoolId, new AbstractRequestCallback() {
 	      @Override
 	      public void onResponseReceived(Request request, Response response) {
 		YapoolJson yapool = new YapoolJson(response.getText());
 		yapool.addMember(YapoolGWT.currentProfile.getId());
 
 		HttpInterface.doPostJson("/yapooldb/", yapool, new AbstractRequestCallback() {
 		  @Override
 		  public void onResponseReceived(Request request, Response response) {
 		    System.out.println(response.toString());
 		    System.out.println("Joined Successfully");
 		    YapoolGWT.currentProfile.setCurrentYapool(currentYapoolId);
 		    YapoolSignUnit.yapoolSignUnit.displayCurrentYapoolButton();
 		  }
 		});
 
 	      }
 	    });
 
 	    messageInput.setVisible(true);
 	    leaveButton.setVisible(true);
 	  }
 	}
       });
     }
   }
 
   public class LeaveButtonClickHandler implements ClickHandler {
     @Override
     public void onClick(ClickEvent event) {
 
       HttpInterface.doGet("/yapooldb/" + YapoolGWT.currentSession.getName() + "/", new AbstractRequestCallback() {
 	@Override
 	public void onResponseReceived(Request request, Response response) {
 	  ProfileJson profile = new ProfileJson(response.getText());
 	  if (profile.getCurrentYapool().equals(currentYapoolId)) {
 	    leaveButton.setVisible(false);
 
 	    // Update Profile - currentYapool to Empty
 	    profile.setCurrentYapool("");
 	    HttpInterface.doPostJson("/yapooldb/", profile, new AbstractRequestCallback() {
 	      @Override
 	      public void onResponseReceived(Request request, Response response) {
 		System.out.println(response.toString());
 		System.out.println("Current Yapool is set to empty.");
 	      }
 	    });
 	    YapoolGWT.currentProfile = profile;
 	    // Update Yapool - Add this user to the Yapool
 	    HttpInterface.doGet("/yapooldb/" + currentYapoolId, new AbstractRequestCallback() {
 	      @Override
 	      public void onResponseReceived(Request request, Response response) {
 		YapoolJson yapool = new YapoolJson(response.getText());
 
 		JSONArray tempArray = yapool.getMembers();
 		JSONArray newArray = new JSONArray();
 		int j = 0;
 		for (int i = 0; i < tempArray.size(); i++) {
 		  if (!tempArray.get(i).isString().stringValue().equals(YapoolGWT.currentSession.getName())) {
 		    newArray.set(j, new JSONString(tempArray.get(i).isString().stringValue()));
 		    j++;
 		  }
 		}
 		yapool.setMembers(newArray);
 		// yapool.getMembers(YapoolGWT.currentProfile.getId());
 
 		HttpInterface.doPostJson("/yapooldb/", yapool, new AbstractRequestCallback() {
 		  @Override
 		  public void onResponseReceived(Request request, Response response) {
 		    System.out.println(response.toString());
 		    System.out.println("Left Successfully");
 		    YapoolGWT.currentProfile.setCurrentYapool("");
 		    YapoolSignUnit.yapoolSignUnit.displayCurrentYapoolButton();
 		  }
 		});
 
 	      }
 	    });
 
 	    messageInput.setVisible(false);
 	    joinButton.setVisible(true);
 	  }
 	}
       });
     }
   }
 
   public class CloseButtonClickHandler implements ClickHandler {
     @Override
     public void onClick(ClickEvent event) {
       closeButton.setVisible(false);
       currentYapool.setState("closed");
       memberListTable.setText(0, 1, currentYapool.getState());
       HttpInterface.doPostJson("/yapooldb/", currentYapool, new AbstractRequestCallback() {
 	@Override
 	public void onResponseReceived(Request request, Response response) {
 
 	  System.out.println(response.toString());
 	  System.out.println("YaPooL is CLOSED");
 	}
       }); // http doPostJson Ends
     }
   }
 
   public class DoneButtonClickHandler implements ClickHandler {
     @Override
     public void onClick(ClickEvent event) {
       doneButton.setVisible(false);
       currentYapool.setState("done");
       memberListTable.setText(0, 1, currentYapool.getState());
       HttpInterface.doPostJson("/yapooldb/", currentYapool, new AbstractRequestCallback() {
 	@Override
 	public void onResponseReceived(Request request, Response response) {
 
 	  System.out.println(response.toString());
 	  System.out.println("YaPooL is DONE");
 	}
       }); // http doPostJson Ends
     }
   }
 
   public static int lastPostCount = -1;
   static int lastMemberCount = -1;
   static String lastState = "";
 
   public class LoadYapoolRequestCallback extends AbstractRequestCallback {
     @Override
     public void onResponseReceived(Request request, Response response) {
     	messageInput.setFocus(true);
       currentYapool = new YapoolJson(response.getText());
       yapoolNameLabel.setText(currentYapool.getName());
       JSONArray members = currentYapool.getMembers();
       int size = members.size();
       String state = currentYapool.getState();
       if (state.equals("open")) {
 	stateLabel.setText("Open");
 	stateLabel.getElement().setClassName("displayYapoolState open");
      } else if (state.equals("close")) {
 	stateLabel.setText("Close");
 	stateLabel.getElement().setClassName("displayYapoolState close");
       } else if (state.equals("done")) {
 	stateLabel.setText("Done");
 	stateLabel.getElement().setClassName("displayYapoolState done");
       }
       
       pickUpPlaceLabel.setText("Pick Up Place: " + currentYapool.getPickUpPlace());
       orderTimeLabel.setText("Shooting Order Time: " + currentYapool.getExpectedOrderDate());
 	  HttpInterface.doGet("/yapooldb/" + currentYapool.getRestaurant(), new AbstractRequestCallback() {
 		    @Override
 		    public void onResponseReceived(Request request, Response response) {
 		      RestaurantJson restaurant = new RestaurantJson(response.getText());
 		      //System.out.println("here: " + restaurant);
 		      //System.out.println("name: " + restaurant.getName());
 		      restaurantNameLabel.setText("Restaurant Name: " + restaurant.getName());
 		      restaurantNameLabel.addClickHandler(new ClickHandler() {
 		            public void onClick(ClickEvent event) {
 		            	NavigationUnit.navigationUnit.hideAll();
 		            	DisplayRestaurantUnit.displayRestaurantUnit.displayRestaurant(currentYapool.getRestaurant());
 		              }
 		      });
 		      restaurantNameLabel.getElement().setClassName("listTags");
 		    }
 		  });
 
       if (size != lastMemberCount || !state.equals(lastState)) {
 	lastMemberCount = size;
 	lastState = state;
 	memberListTable.removeAllRows();
 	memberListTable.setText(0, 0, "*" + currentYapool.getOwner());
 
 	for (int i = 0; i < size; i++) {
 	  int rowCounts = memberListTable.getRowCount();
 	  memberListTable.setText(rowCounts, 0, members.get(i).isString().stringValue());
 	}
 
 	if (state.equals("done")) {
 	  // Update Profile - Add this YaPooL to the passedYapool
 	  HttpInterface.doGet("/yapooldb/" + YapoolGWT.currentSession.getName(), new AbstractRequestCallback() {
 	    @Override
 	    public void onResponseReceived(Request request, Response response) {
 	      YapoolGWT.currentProfile = new ProfileJson(response.getText());
 	      YapoolGWT.currentProfile.archieveYapool();
 
 	      HttpInterface.doPostJson("/yapooldb/", YapoolGWT.currentProfile, new AbstractRequestCallback() {
 		@Override
 		public void onResponseReceived(Request request, Response response) {
 		  System.out.println(response.toString());
 		  System.out.println("Archieved YaPooL Successfully");
 		  YapoolSignUnit.yapoolSignUnit.displayCurrentYapoolButton();
 		}
 	      });
 
 	    }
 	  });
 	}
       }
 
       if (YapoolGWT.currentProfile != null) {
 	if (YapoolGWT.currentProfile.getCurrentYapool().equals("")) {
 	  // NO YAPOOL
 	  if (currentYapool.getState().equals("open")) {
 	    joinButton.setVisible(true);
 	    leaveButton.setVisible(false);
 	    messageInput.setVisible(false);
 	    closeButton.setVisible(false);
 	    doneButton.setVisible(false);
 	  } else if (currentYapool.getState().equals("closed")) {
 	    joinButton.setVisible(false);
 	    leaveButton.setVisible(false);
 	    messageInput.setVisible(false);
 	    closeButton.setVisible(false);
 	    doneButton.setVisible(false);
 	  }
 	} else if (YapoolGWT.currentProfile.getCurrentYapool().equals(currentYapoolId)) {
 	  // CURRENTYAPOOL = THIS YAPOOL
 	  if (currentYapool.getOwner().equals(YapoolGWT.currentProfile.getId())) {
 	    // OWNER
 	    if (currentYapool.getState().equals("open")) {
 	      joinButton.setVisible(false);
 	      leaveButton.setVisible(false);
 	      messageInput.setVisible(true);
 	      closeButton.setVisible(true);
 	      doneButton.setVisible(false);
 	    } else if (currentYapool.getState().equals("closed")) {
 	      joinButton.setVisible(false);
 	      leaveButton.setVisible(false);
 	      messageInput.setVisible(true);
 	      closeButton.setVisible(false);
 	      doneButton.setVisible(true);
 	    }
 	  } else {
 	    // NOT OWNER
 	    if (currentYapool.getState().equals("open")) {
 	      joinButton.setVisible(false);
 	      leaveButton.setVisible(true);
 	      messageInput.setVisible(true);
 	      closeButton.setVisible(false);
 	      doneButton.setVisible(false);
 	    } else if (currentYapool.getState().equals("closed")) {
 	      joinButton.setVisible(false);
 	      leaveButton.setVisible(false);
 	      messageInput.setVisible(true);
 	      closeButton.setVisible(false);
 	      doneButton.setVisible(false);
 	    }
 	  }
 	} else {
 	  // IN OTHER YAPOOL
 	  joinButton.setVisible(false);
 	  leaveButton.setVisible(false);
 	  messageInput.setVisible(false);
 	  closeButton.setVisible(false);
 	  doneButton.setVisible(false);
 	}
       }
 
     }
 
   }
 
   public class DisplayYapoolPostRequestCallback extends AbstractRequestCallback {
     @Override
     public void onResponseReceived(Request request, Response response) {
       JSONArray yapools = new ViewJson(response.getText()).getRows();
       int size = yapools.size();
       if (size > lastPostCount) {
 	lastPostCount = size;
 	postListTable.removeAllRows();
 	postListTable.setText(0, 0, "User Name");
 	postListTable.setText(0, 1, "Message");
 	for (int i = 0; i < size; i++) {
 	  JSONObject temp = yapools.get(i).isObject().get("value").isObject();
 	  PostJson post = new PostJson(temp);
 	  int rowCounts = postListTable.getRowCount();
 	  postListTable.setText(rowCounts, 0, post.getUser());
 	  postListTable.setText(rowCounts, 1, post.getMessage());
 	}
 	int rowCounts = postListTable.getRowCount();
 	postListTable.setText(rowCounts, 0, "message:");
 	postListTable.setWidget(rowCounts, 1, messageInput);
 	postScrollPanel.scrollToBottom();
       }
       Timer t = new UpdatePostTimer();
       t.schedule(1000);
     }
   }
 
   /**
    * update the post of a YaPooL! every second
    * 
    * @author rem
    * 
    */
   public class UpdatePostTimer extends Timer {
 
     @Override
     public void run() {
       if (RootPanel.get(getContainerId()).isVisible() && currentYapool != null) {
 
 	HttpInterface.doGet("/yapooldb/_design/post/_view/by_yapoolId?key=\"" + currentYapoolId + "\"",
 	    new DisplayYapoolPostRequestCallback());
 	HttpInterface.doGet("/yapooldb/" + currentYapoolId, new LoadYapoolRequestCallback());
       }
     }
   }
 
   public class WritePostRequestCallback extends AbstractRequestCallback {
     @Override
     public void onResponseReceived(Request request, Response response) {
       int rowCounts = postListTable.getRowCount();
       postListTable.remove(messageInput);
       postListTable.setText(rowCounts-1, 0, tempMessage.getUser());
       postListTable.setText(rowCounts-1, 1, tempMessage.getMessage());
       rowCounts++;
       
       postListTable.setText(rowCounts, 0, "message: ");
       postListTable.setWidget(rowCounts, 1, messageInput);
       postScrollPanel.scrollToBottom();
     }
   }
 
   @Override
   public String getContainerId() {
     return "displayYapoolContainer";
   }
 }
