 package beans;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.validator.ValidatorException;
 
 /**
  *
  * @author alex
  */
 @ManagedBean
 @SessionScoped
 public class Register {
 
     @ManagedProperty(value = "#{player}")
     private Player player;
     @ManagedProperty(value = "#{playerList}")
     private PlayerList playerList;
     private boolean showTerms = false;
 
     public void validateUsername(FacesContext ctx, UIComponent component,
             Object value) throws ValidatorException {
         if (this.playerList.getPlayerList().containsKey((String) value)) {
             FacesMessage msg = new FacesMessage("Username already exists");
             throw new ValidatorException(msg);
         }
     }
 
     public void validateBirthday(FacesContext ctx, UIComponent component,
             Object value) throws ValidatorException {
         SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        //String birthday = format.format((Date) value);
        String birthday = (String) value;
         System.out.println("BIRTHDAY TEST" + birthday);
         if (birthday.length() != 10) {
             FacesMessage msg = new FacesMessage(
                     "Wrong Date! Pattern: dd.MM.YYYY");
             throw new ValidatorException(msg);
         }
 
         if (birthday.indexOf('.') != 2 || birthday.lastIndexOf('.') != 5) {
             FacesMessage msg = new FacesMessage(
                     "Wrong Date2! Pattern: dd.MM.YYYY");
             throw new ValidatorException(msg);
         }
 
         try {
             int day = Integer.parseInt(birthday.substring(0, 2));
             int month = Integer.parseInt(birthday.substring(3, 5));
             int year = Integer.parseInt(birthday.substring(6));
 
             if (day < 1 || day > 31 || month < 1 || month > 12 || year < 1890
                     || year > 2012) {
                 FacesMessage msg = new FacesMessage(
                         "Wrong Date3! Pattern: dd.MM.YYYY");
                 throw new ValidatorException(msg);
             }
         } catch (NumberFormatException e) {
             FacesMessage msg = new FacesMessage(
                     "Wrong Date4! Pattern: dd.MM.YYYY");
             throw new ValidatorException(msg);
         }
     }
 
     public String register() {
         System.out.println("name: " + this.player.getUsername() + " pw: "
                 + this.player.getPassword() + " first: " + this.player.getFirstname());
 
 
         this.playerList.getPlayerList()
                 .put(this.player.getUsername(), this.player);
         this.player = new Player();
         this.showTerms = false;
         return "/index.xhtml";
     }
 
     public void showChanged(ValueChangeEvent e) {
         Boolean show = (Boolean) e.getNewValue();
         if (show != null) {
             this.showTerms = show;
         }
 
     }
 
     /**
      * @return the player
      */
     public Player getPlayer() {
         return player;
     }
 
     /**
      * @param player the player to set
      */
     public void setPlayer(Player player) {
         this.player = player;
     }
 
     /**
      * @return the playerList
      */
     public PlayerList getPlayerList() {
         return playerList;
     }
 
     /**
      * @param playerList the playerList to set
      */
     public void setPlayerList(PlayerList playerList) {
         this.playerList = playerList;
     }
 
     /**
      * @return the showTerms
      */
     public boolean isShowTerms() {
         return showTerms;
     }
 
     /**
      * @param showTerms the showTerms to set
      */
     public void setShowTerms(boolean showTerms) {
         this.showTerms = showTerms;
     }
 }
