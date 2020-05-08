 package kz.enu.epam.azimkhan.tour.command.admin;
 
 import kz.enu.epam.azimkhan.tour.command.AdminCommand;
 import kz.enu.epam.azimkhan.tour.dao.TourDAO;
 import kz.enu.epam.azimkhan.tour.exception.CommandException;
 import kz.enu.epam.azimkhan.tour.exception.DAOLogicalException;
 import kz.enu.epam.azimkhan.tour.exception.DAOTechnicalException;
 import kz.enu.epam.azimkhan.tour.notification.creator.NotificationCreator;
 import kz.enu.epam.azimkhan.tour.notification.entity.Notification;
 import kz.enu.epam.azimkhan.tour.notification.service.NotificationService;
 import kz.enu.epam.azimkhan.tour.resource.LocaleManager;
 import kz.enu.epam.azimkhan.tour.resource.PathManager;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Locale;
 
 /**
  * Delete tour command
  */
 public class DeleteTourCommand extends AdminCommand{
 
     @Override
     public String execute(HttpServletRequest request, HttpServletResponse response) throws CommandException {
         String param = request.getParameter("id");
         if (param != null){
             Notification notification = null;
             Locale locale = LocaleManager.INSTANCE.resolveLocale(request);
             try{
                 int id = Integer.parseInt(param);
                 TourDAO dao = TourDAO.getInstance();
                 if (dao.delete(id)){
                     notification = NotificationCreator.createFromProperty("info.db.delete_success", locale);
                 }
             } catch (NumberFormatException e){
                 notification = NotificationCreator.createFromProperty("error.invalid_parameter", Notification.Type.ERROR, locale);
             } catch (DAOTechnicalException e) {
                 throw new CommandException(e);
             } catch (DAOLogicalException e) {
                 notification = NotificationCreator.createFromProperty("error.db.no_such_record", Notification.Type.ERROR, locale);
             } finally {
                 if (notification != null){
                     NotificationService.push(request.getSession(), notification);
                 }
             }
         }
 
         return pathManager.getString("path.page.admin.manager");
     }
 }
