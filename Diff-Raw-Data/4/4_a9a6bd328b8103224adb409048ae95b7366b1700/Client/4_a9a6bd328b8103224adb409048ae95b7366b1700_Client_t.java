 package no.ntnu.fp;
 import java.awt.Container;
 import java.security.PublicKey;
 import java.util.Date;
 import java.util.Stack;
 
 import no.ntnu.fp.gui.CalendarView;
 import no.ntnu.fp.gui.LoginView;
 import no.ntnu.fp.gui.View;
 import no.ntnu.fp.model.*;
 
 import javax.swing.*;
 
 public class Client extends JFrame {
     
     /** Test data **/
     public static Appointment app1;
     public static Appointment app2;
     public static Invitation inv1;
     public static Invitation inv2;
     public static User sigve;
     public static Room f1;
     
     
     private static User user;
     public static Client calendar;
     /**A stack of views*/
     static Stack<View> viewStack;
     static{
     	viewStack = new Stack<View>();
     }
     /**
      * Pushes view on {@link #viewStack}, displays and returns view afterwards.
      * @param view
      * @return topmost view
      * @see no.ntnu.fp.Client#viewStack
      */
     static View pushView(View view) {
     	view.initialize();
     	calendar.setContentPane((Container) view);
     	if(viewStack.size()>0){
 	    	viewStack.peek().pause();
     	}
     	viewStack.push(view);
     	calendar.pack();
     	calendar.centerOnScreen();
 		return view;
 	}
     /**
      * Pops view, resuming and returning the next view in stack
      * @return next view
      */
     public static View popView() {
     	View popped = viewStack.pop();
     	popped.deinitialize();
     	View view = viewStack.peek();
     	view.resume();
     	calendar.setContentPane((Container) view);
     	calendar.pack();
     	calendar.centerOnScreen();
 		return view;
 	}
 
     public Client() {
         setTitle("Calendar");
 
         sigve = new User("sigveseb", "Sigve Sebastian", "Farstad", "sigve@arkt.is");
         inv1 = new Invitation(sigve, app1);
         inv2 = new Invitation(sigve, app2);
        app1 = new Appointment("Testappointment", "Utendørs", "Dette er en test for å teste testen", new Date(2012, 3, 15, 9, 0), new Date(2012, 3, 15, 10, 30));
        app2 = new Appointment("Testappointment nummer 2", f1, "Dette er en test for å teste testen", new Date(2012, 3, 14, 14, 0), new Date(2012, 3, 15, 17, 0));
         f1 = new Room("F1", 300);
     }
     /**
      * Logs in user, eventually
      * @param username
      * @param password
      * 
      */
     public static void login(String username, String password){
     	//For now we just pretend we have logged in a user
     	System.out.println("Logging in with username: \""+username+"\" and password: \""+password+"\"");
         setUser(sigve);
     	pushView(new CalendarView());
     }
     /**
      * Sets this calendar user
      * @param user
      */
     public static void setUser(User user) {
         calendar.user = user;
     }
     /**
      * Returns this Calendar's user
      * @return user
      * 
      */
     public static User getUser() {
         return calendar.user;
     }
     /**
      * Centers the window
      */
     public void centerOnScreen(){
     	calendar.setLocationRelativeTo(null);
     }
     /**
      * makes a new Calendar, pushes to stack, and makes it visible
      * @see#viewStack 
      */
     public static void main (String[] args) {
         calendar = new Client();
         pushView(new LoginView());
         calendar.setVisible(true);
     }
     
 }
