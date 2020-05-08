 package com.dreamskiale.zodiac.client;
 
 import java.util.List;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 
 public class ZodiacPresenter {
   
   final ZodiacView view;
   
   public ZodiacPresenter(ZodiacView view) {
     this.view = view;
     bindControls();
   }
 
   public void setUser(User user) {
     view.getName().setText(user.getName());
     view.getBirthday().setText(user.getBirthday());
     view.getPicture().setUrl("http://graph.facebook.com/"+user.getId()+"/picture");
   }
   
   
 
   private void bindControls() {
     view.getLoginButton().addClickHandler(new ClickHandler(){
       @Override
       public void onClick(ClickEvent event) {
         Facebook.login(new AsyncCallback<Boolean>(){
           @Override
           public void onFailure(Throwable caught) {}
           @Override
           public void onSuccess(Boolean result) {}
         });
       }
     });
     
   }
 
   public void setFriends(List<User> friends) {
     for (User friend : friends) {
       String s = friend.getBirthday();
       if (s == null || s.trim().length() == 0) { continue; }
       ZodiacSign z = ZodiacSign.getZodiacSign(s);
       view.getPanel(z).add(new Label("==="));
       view.getPanel(z).add(new Label(friend.getName()));
       view.getPanel(z).add(new Label(friend.getBirthday()));
       view.getPanel(z).add(new Image(friend.getPicSquare()));
     }
     
   }
 
   public void go(FlowPanel panel) {
     view.getContent().setVisible(false);
     panel.add(view.asWidget());
     
     Facebook.addUserLoggedInHandler(new Facebook.UserLoggedInHandler(){
       @Override
       public void onUserLoggedIn() {
         startApp();
       }
     });
     
    checkLoginStatus();
  }
  
  private void checkLoginStatus() {
     Facebook.getLoginStatus(new AsyncCallback<Boolean>() {
       @Override
       public void onFailure(Throwable caught) {
         Window.alert("on failure getlogin status " + caught);
       }
 
       @Override
       public void onSuccess(Boolean isUserLoggedIn) {
         view.getLoginButton().setVisible(!isUserLoggedIn);
         view.getContent().setVisible(isUserLoggedIn);
         if (isUserLoggedIn) {
           startApp();
         }
       }
     });      
   }
   
   private void startApp() {
     view.getLoginButton().setVisible(false);
     view.getContent().setVisible(true);
     getUser();
   }
   
   private void getUser() {
     User.get(new AsyncCallback<User>() {
       @Override
       public void onFailure(Throwable caught) {
         Window.alert("failure getuser " + caught);
       }
 
       @Override
       public void onSuccess(User user) {
         displayUser(user);
         displayFriends(user);
       }
       
     });
   }
   
   private void displayUser(User user) {
     setUser(user);
   }
   
   private void displayFriends(User user) {
     user.getFriends(new AsyncCallback<List<User>> () {
 
       @Override
       public void onFailure(Throwable caught) {
         Window.alert("failure get friends " + caught);
         
       }
 
       @Override
       public void onSuccess(List<User> friends) {
         setFriends(friends);
 //        Window.alert("number of friends: " + users.size());
 //        Window.alert(users.get(1).getName() + " " + users.get(1).getBirthday());
       }
       
     });
   }
 }
