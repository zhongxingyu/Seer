 package bean.managed.sessionScoped;
 
 import bean.statefull.LocalBeanSessionBasket;
 import bean.stateless.LocalBeanSessionBook;
 import bean.stateless.LocalBeanSessionUser;
 import entity.EntityBook;
 import entity.EntityCopy;
 import entity.EntityUser;
 import exception.ExceptionUserAlreadyExists;
 import exception.ExceptionUserDoesNotExist;
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.security.RolesAllowed;
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Tomáš Čerevka
  * @author Adam Činčura
  */
 @ManagedBean(name = "user")
 @SessionScoped
 public class BeanManagedUser implements Serializable {
 
     public static final Logger logger = Logger.getLogger(BeanManagedUser.class.getName());
 
     @EJB
     private LocalBeanSessionUser beanSessionUser;
 
     @EJB
     private LocalBeanSessionBasket beanSessionBasket;
 
     @EJB
     private LocalBeanSessionBook beanSessionBook;
 
     /**
      * Pouze pro ucely prihlasovani.
      */
     private String email;
 
     /**
      * Pouze pro ucely prihlasovani a registrace.
      */
     private String password;
 
     /**
      * Aktualne prihlaseny uzivatel.
      */
     private EntityUser user = new EntityUser();
 
     public String getEmail() {
         return "";
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return "";
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public EntityUser getUser() {
         return user;
     }
 
     public void setUser(EntityUser user) {
         this.user = user;
     }
 
     /**
      * Prihlasi uzivatele.
      * @return Vysledek akce.
      */
     public String doLogin() {
         FacesContext ctx = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) ctx.getExternalContext().getRequest();
         try {
             request.login(email, password);
             user = beanSessionUser.getUserByEmail(email);
             email = password = null;
             return "/index";
         } catch (ServletException ex) {
             email = password = null;
             ResourceBundle bundle = ctx.getApplication().getResourceBundle(ctx, "bundle");
             ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, bundle.getString("message.error.login"), ""));
             return null;
         }
 
     }
 
     /**
      * Odhlasi uzivatele.
      * @return Vysledek akce.
      */
     public String doLogout() {
         HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
         session.invalidate();
         user = new EntityUser();
         return "/logout";
     }
 
     /**
      * Registruje noveho uzivatele.
      * @return Vysledek akce.
      */
     public String doRegister() {
         FacesContext context = FacesContext.getCurrentInstance();
         ResourceBundle bundle = context.getApplication().getResourceBundle(context, "bundle");
 
         // Overeni, ze se shoduji hesla.
         if (!user.getPassword().equals(password)) {
             FacesMessage message = new FacesMessage(bundle.getString("message.error.passwordsDoNotMatch"));
             context.addMessage("registrationForm:controlPassword", message);
             password = "";
             user.setPassword("");
             logger.log(Level.WARNING, "E-mails do not match.");
             return null;
         }
 
         // Vytvareni uzivatele.
         try {
             beanSessionUser.registerNewUser(user);
         } catch (ExceptionUserAlreadyExists ex) {
             FacesMessage message = new FacesMessage(bundle.getString("message.error.userAlreadyExists"));
             context.addMessage("registrationForm:email", message);
             password = "";
             user.setPassword("");
             logger.log(Level.SEVERE, "User with this e-mail already exists.", ex);
         }
 
         // Odesle se e-mail.        
         String registrationBodyPattern = bundle.getString("email.registration.body");
         String registrationBody = MessageFormat.format(registrationBodyPattern, user.getName(), user.getEmail());
 
         beanSessionUser.sendMail(user.getEmail(), bundle.getString("email.registration.subject"), registrationBody);
 
         // Registrace je dokoncena.
         user = new EntityUser();
         FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("message.success.registrationComplete"), "");
         context.addMessage(null, message);
         return "/login";
     }
 
     /**
      * Resetuje heslo uzivatele s danym e-mailem a zašle mu ho.
      * @return Vysledek akce.
      */
     public String doResetPassword() {
         FacesContext context = FacesContext.getCurrentInstance();
         ResourceBundle bundle = context.getApplication().getResourceBundle(context, "bundle");
         try {
             // Zmena hesla.
             String newPassword = beanSessionUser.resetPassword(email);
             user = beanSessionUser.getUserByEmail(email);
 
             // Nove heslo se odesle na e-mail.
             String newPasswordBodyPattern = bundle.getString("email.newPassword.body");
             String newPasswordBody = MessageFormat.format(newPasswordBodyPattern, user.getName(), user.getEmail(), newPassword);
             beanSessionUser.sendMail(user.getEmail(), bundle.getString("email.newPassword.subject"), newPasswordBody);
 
             // Zobrazi se hlaska o odeslani hesla.
             String messagePatter = bundle.getString("message.success.newPasswordWasSent");
             String message = MessageFormat.format(messagePatter, user.getEmail());
             FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, message, "");
             context.addMessage(null, facesMessage);
 
             // Vycisti se uzivatel.
             user = new EntityUser();
             return "/login";
         } catch (ExceptionUserDoesNotExist exception) {
             // Uzivatel s danym e-mailem neexistuje.
             String pattern = bundle.getString("message.error.userDoesNotExist");
             String message = MessageFormat.format(pattern, email);
             FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, "");
             context.addMessage(null, facesMessage);
             user = new EntityUser();
             logger.log(Level.SEVERE, "User with this e-mail does not exist.", exception);
             return null;
         }
     }
 
     /**
      * Rozhodne o prihlaseni uzivatele.
      * @return TRUE uzivatel je prihlaseny, FALSE uzivatel neni prihlaseny.
      */
     public Boolean isLogged() {
         return getRole() != null;
     }
 
     public Boolean isUser() {
         return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("user");
     }
 
     public Boolean isAdmin() {
         return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("admin");
     }
 
     /**
      * Vrati v jake roli je uzivatel prihlasen.
      * @return Role uzivatele.
      */
     public String getRole() {
         Collection<String> roles = new ArrayList<String>();
         if (isUser() == true) {
             roles.add("user");
         }
         if (isAdmin() == true) {
             roles.add("admin");
         }
         if (roles.isEmpty() == true) {
             return null;
         }
         return roles.toString();
     }
 
     @RolesAllowed({"user", "admin"})
     public void addToOwnership(EntityBook book) {
 //        beanSessionBasket.addCopy(copy);
         beanSessionBook.setBookCopyToUserOwnership(book, getUser());
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "bundle");
         String bookAddedPattern = bundle.getString("message.success.ownershipSet");
         String bookAddedMessage = MessageFormat.format(bookAddedPattern, book.getTitle());
         FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, bookAddedMessage, "");
         facesContext.addMessage(null, facesMessage);
     }
 
     @RolesAllowed({"user", "admin"})
     public void addToBasket(EntityCopy copy) {
         beanSessionBasket.addCopy(copy);
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "bundle");
         String bookAddedPattern = bundle.getString("message.success.bookAddedToBasket");
         String bookAddedMessage = MessageFormat.format(bookAddedPattern, copy.getBookId().getTitle());
         FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, bookAddedMessage, "");
         facesContext.addMessage(null, facesMessage);
     }
 
     @RolesAllowed({"user", "admin"})
     public Collection<EntityCopy> getCopiesInBasket() {
         return beanSessionBasket.getContent();
     }
 
     @RolesAllowed({"user", "admin"})
     public void removeFromBasket(EntityCopy copy) {
         beanSessionBasket.removeCopy(copy);
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "bundle");
         String bookRemovedPatter = bundle.getString("message.success.bookRemovedFromBasket");
         String bookRemovedMessage = MessageFormat.format(bookRemovedPatter, copy.getBookId().getTitle());
         FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, bookRemovedMessage, "");
         facesContext.addMessage(null, facesMessage);
     }
 
     @RolesAllowed({"user", "admin"})
     public void doBorrowBasket() {
         beanSessionBasket.borrow();
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ResourceBundle bundle = facesContext.getApplication().getResourceBundle(facesContext, "bundle");
         FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, bundle.getString("message.success.borrowed"), "");
         facesContext.addMessage(null, facesMessage);
     }
 }
