 package kz.enu.epam.azimkhan.tour.command;
 
 import kz.enu.epam.azimkhan.tour.dao.OrderDAO;
 import kz.enu.epam.azimkhan.tour.entity.Order;
 import kz.enu.epam.azimkhan.tour.entity.Role;
 import kz.enu.epam.azimkhan.tour.entity.User;
 import kz.enu.epam.azimkhan.tour.exception.*;
 import kz.enu.epam.azimkhan.tour.logic.authentication.AuthenticationLogic;
 import kz.enu.epam.azimkhan.tour.notification.creator.NotificationCreator;
 import kz.enu.epam.azimkhan.tour.notification.entity.Notification;
 import kz.enu.epam.azimkhan.tour.notification.service.NotificationService;
 import kz.enu.epam.azimkhan.tour.resource.LocaleManager;
 import kz.enu.epam.azimkhan.tour.resource.PathManager;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Login command
  */
 public class LoginCommand extends ActionCommand{
 
     public static final String LOGIN_PARAMETER = "login";
     public static final String PASSWORD_PARAMETER = "password";
 
     private Logger logger = Logger.getRootLogger();
 
     /**
      * Everyone allowed to login
      * @param user can be null
      * @return
      */
     @Override
     public boolean checkAccess(User user) {
         return true;
     }
 
 
     /**
      * Execute login command
      * @param request request to read the command from
      * @param response
      * @return
      * @throws CommandException
      */
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws CommandException {
 
         Notification notification = null;
 
         Locale locale = LocaleManager.INSTANCE.resolveLocale(request);
         final String login = request.getParameter(LOGIN_PARAMETER);
         final String password = request.getParameter(PASSWORD_PARAMETER);
 
         if (login != null && password != null){
 
             try {
 				User user = AuthenticationLogic.authenticate(login, password);
 
 					HttpSession session = request.getSession();
 					session.setAttribute(AuthenticationLogic.SESSION_VAR, user);
 
                     logger.info("Successful authentication by login: " + login);
                     notification = NotificationCreator.createFromProperty("info.auth.success", locale);
 
                     if (user.getRole().getRolename().equals(Role.ROLE_ADMIN)){
 						return pathManager.getString("path.page.admin.manager");
 					} else if (user.getRole().getRolename().equals(Role.ROLE_CLIENT)){
                         List<Order> orders = OrderDAO.getInstance().findOrdersForUser(user);
                         request.setAttribute("orders", orders);
 						return pathManager.getString("path.page.client.account");
 					}
 
 
             } catch (AuthenticationTechnicalException e) {
                 throw new CommandException(e);
             } catch (AuthenticationLogicalException e) {
                 logger.info("Authentication fail by login: " + login);
                 notification = NotificationCreator.createFromProperty("error.auth.invalid_login_pass", Notification.Type.ERROR, locale);
 
             } catch (DAOTechnicalException e) {
                 throw new CommandException(e);
             } catch (DAOLogicalException e) {
                 throw new CommandException(e);
             } finally {
                 if (notification != null){
                     NotificationService.push(request.getSession(), notification);
                 }
             }
         }
         return pathManager.getString("path.page.login");
     }
 }
