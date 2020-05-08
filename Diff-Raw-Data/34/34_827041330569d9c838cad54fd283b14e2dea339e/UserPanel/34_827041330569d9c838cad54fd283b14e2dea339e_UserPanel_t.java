 /*
  * Created on Aug 12, 2006
  */
 package com.alertscape.browser.ui.swing.panel;
 
 import java.awt.BorderLayout;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.alertscape.browser.common.auth.Authentication;
 import com.alertscape.browser.common.auth.AuthenticationEvent;
 import com.alertscape.browser.common.auth.AuthenticationListener;
 import com.alertscape.common.model.AuthenticatedUser;
 
 /**
  * @author josh
  * @version $Version: $
  */
 public class UserPanel extends JPanel implements AuthenticationListener
 {
   private static final long serialVersionUID = 1L;
 
   private static final ImageIcon userImage;
   private static final String NO_USER = "None";
   private JLabel userLabel;
 
   static
   {
     URL imageLoc = UserPanel.class
         .getResource("/com/alertscape/images/mini/icon_user.gif");
     userImage = new ImageIcon(imageLoc);
   }
 
   public UserPanel( )
   {
     init( );
   }
 
   protected void init( )
   {
     userLabel = new JLabel( );
     userLabel.setIcon(userImage);
     // userLabel.setHorizontalAlignment(JLabel.RIGHT);
     setLayout(new BorderLayout( ));
 
     add(userLabel, BorderLayout.CENTER);
 
     configureLabel( );
 
     Authentication.addAuthenticationListener(this);
   }
 
   public void handleAuthEvent(AuthenticationEvent e)
   {
    AuthenticatedUser u = e.getUser();
    if (u == null)
    {
      userLabel.setText(NO_USER);
    }
    else
    {
      userLabel.setText(u.getUsername( ));
    }
   }
 
   protected void configureLabel( )
   {
     AuthenticatedUser u = Authentication.getUser("CEV");
     if (u == null)
     {
       userLabel.setText(NO_USER);
     }
     else
     {
       userLabel.setText(u.getUsername( ));
     }
   }
 }
