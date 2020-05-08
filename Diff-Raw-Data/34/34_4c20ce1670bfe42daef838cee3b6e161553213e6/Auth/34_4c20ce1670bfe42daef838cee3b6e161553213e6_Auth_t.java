 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package formel0api;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 
 /**
  *
  * @author Dieter
  */
 @ManagedBean(name="auth")
 @SessionScoped
 public class Auth {
     @ManagedProperty(value="#{player}")
     Player player;
     
     private boolean newPlayer = false;
     private boolean wrongPwd = false;
     
     private ResourceBundle bundle;
     private HashMap<String, Player> players = new HashMap<String, Player>();
 
     public Auth() {
         player = new Player("Hans", "Moser");
         players.put(player.getName(), player); 
     }
 
     //Getter and Setter
     public void setPlayer(Player player) {
         this.player = player;
     }
     
     public Player getPlayer() {
         return player;
     }
     
     public void setNewPlayer(boolean newPlayer) {
         this.newPlayer = newPlayer;
     }
     
     public boolean isNewPlayer() {
         return newPlayer;
     }
     
     public void setWrongPwd(boolean wrongPwd) {
         this.wrongPwd = wrongPwd;
     }
     
     public boolean isWrongPwd() {
         return wrongPwd;
     }
     
     private void reset() {
         newPlayer = wrongPwd = false;
     }
 
     //Login
     public String login() {
         reset();
         if(players.containsKey(player.getName())) {
             String pwd = players.get(player.getName()).getPwd();
             if(pwd.equals(player.getPwd())) {
                 return "/table.xhtml?faces-redirect=true";
             } else {
                 wrongPwd = true;
                 return "/login.xhtml?faces-redirect=true";
             }
         } else {
             newPlayer = true;
             return "/login.xhtml?faces-redirect=true";
         }
     }
     
     //Register
     public String register() {
         reset();
         if(!players.containsKey(player.getName())) {
             Player p = new Player(player);
             players.put(p.getName(), p);
             return "/table.xhtml?faces-redirect=true";
         }
         return "/register.xhtml?faces-redirect=true";
     }
 }
