 package org.mat.nounou.services;
 
 import org.mat.nounou.model.Appointment;
 import org.mat.nounou.model.Child;
 import org.mat.nounou.model.User;
 import org.mat.nounou.servlets.EntityManagerLoaderListener;
 import org.mat.nounou.util.Check;
 import org.mat.nounou.util.Constants;
 import org.mat.nounou.vo.AppointmentVO;
 import org.mat.nounou.vo.ReportVO;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Value Object for Appointment
  * AppointmentVO: mlecoutre
  * Date: 28/10/12
  * Time: 11:13
  */
 @Path("/appointments")
 @Produces(MediaType.APPLICATION_JSON)
 public class AppointmentService {
 
     @GET
     public List<AppointmentVO> get() {
         System.out.println("Appointment service");
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         TypedQuery<Appointment> query = em.createQuery("FROM Appointment", Appointment.class);
         List<AppointmentVO> apps = new ArrayList<AppointmentVO>();
         try {
             query.setMaxResults(Constants.MAX_RESULT);
             List<Appointment> appointments = query.getResultList();
             for (Appointment a : appointments) {
                 AppointmentVO appVo = new AppointmentVO();
                 appVo.setAppointmentId(a.getAppointmentId());
                 if (a.getArrivalDate() != null)
                     appVo.setArrivalDate(Constants.sdf.format(a.getArrivalDate()));
                 if (a.getArrivalUser() != null)
                     appVo.setArrivalUserName(a.getArrivalUser().getFirstName().concat(" ").concat(a.getArrivalUser().getLastName()));
                 if (a.getDepartureUser() != null)
                     appVo.setDepartureDate(Constants.sdf.format(a.getDepartureDate()));
                 if (a.getDepartureUser() != null)
                     appVo.setDepartureUserName(a.getDepartureUser().getFirstName().concat(" ").concat(a.getDepartureUser().getLastName()));
                 appVo.setKidId(a.getChild().getChildId());
                 appVo.setKidName(a.getChild().getFirstName());
                 apps.add(appVo);
                 //TODO map Planned date when it will be available
             }
         } catch (NoResultException nre) {
             System.out.println("No appointment at this time");
         } finally {
             em.close();
         }
         return apps;
     }
 
 
     @GET
     @Path("/report/account/{accountId}/searchType/{searchType}")
     public ReportVO getLastAppointments(@PathParam("accountId") Integer accountId, @PathParam("searchType") String searchType) {
         System.out.println("getLastAppointments service");
         //Check input parameters
        if (Check.checkIsEmptyOrNull(accountId) || Check.checkIsNotEmptyOrNull(searchType)) {
             System.out.printf("WARNING: Incorrect parameters accountId:%d, searchType:%s\n", accountId, searchType);
             return null;
         }
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         List<AppointmentVO> vos = new ArrayList<AppointmentVO>();
         long totalDuration = 0;
         try {
             StringBuffer buff = new StringBuffer("FROM Appointment WHERE accountId=:accountId");
 
             if ("currentMonth".equals(searchType)) {
                 buff.append(" AND MONTH(arrivalDate) = MONTH(CURRENT_DATE)");
             }
             buff.append(" ORDER BY arrivalDate DESC");
             TypedQuery<Appointment> query = em.createQuery(buff.toString(), Appointment.class);
             query.setParameter("accountId", accountId);
             if ("last".equals(searchType)) {
                 query.setMaxResults(5);
             } else {
                 query.setMaxResults(31);
             }
             List<Appointment> appointments = query.getResultList();
             for (Appointment app : appointments) {
                 AppointmentVO vo = new AppointmentVO();
                 vo.setAppointmentId(app.getAppointmentId());
                 vo.setDepartureUserName(app.getDepartureUser().getFirstName().concat(" ").concat(app.getDepartureUser().getLastName()));
 
                 if (app.getArrivalDate() != null) {
                     vo.setArrivalDate(Constants.sdfTime.format(app.getArrivalDate()));
                     vo.setDate(Constants.sdfDate.format(app.getArrivalDate()));
                 }
                 if (app.getDepartureDate() != null) {
                     vo.setDepartureDate(Constants.sdfTime.format(app.getDepartureDate()));
                     vo.setDate(Constants.sdfDate.format(app.getArrivalDate()));
                 }
                 vo.setArrivalUserName(app.getArrivalUser().getFirstName().concat(" ").concat(app.getArrivalUser().getLastName()));
 
                 TypedQuery<Child> qChild = em.createQuery("FROM Child WHERE accountId=:accountId", Child.class);
                 qChild.setParameter("accountId", accountId);
                 List<Child> children = qChild.getResultList();
                 if (children.size() > 0) {
                     vo.setKidId(children.get(0).getChildId());
                     vo.setKidName(children.get(0).getFirstName());
                     vos.add(vo);
                 }
 
                 //calculate duration
                 if (app.getDepartureDate() != null && app.getArrivalDate() != null) {
                     long timeMilli = app.getDepartureDate().getTime() - app.getArrivalDate().getTime();
                     Date duration = new Date(timeMilli);
                     totalDuration = totalDuration + timeMilli;
                     vo.setDuration(Constants.sdfTime.format(duration));
                 } else {
                     vo.setDuration("n/a");
                 }
 
             }
         } catch (NoResultException nre) {
             System.out.printf("ERROR: No result found with accountId:%d, searchType: %s\n", accountId, searchType);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         ReportVO reportVO = new ReportVO();
         reportVO.setAppointments(vos);
         reportVO.setReportTitle("Total duration");
         reportVO.setTotalDuration(TimeService.getDurationBreakdown(totalDuration));
 
 
         return reportVO;
     }
 
     /**
      * Used for initialization of run  part
      *
      * @param userId userId
      * @return AppointmentVO
      */
     @GET
     @Path("/current/account/{accountId}/userId/{userId}")
     public AppointmentVO getCurrentAppointment(@PathParam("accountId") Integer accountId, @PathParam("userId") Integer userId) {
         //Check input parameters
        if (Check.checkIsEmptyOrNull(accountId) || Check.checkIsNotEmptyOrNull(userId)) {
             System.out.printf("WARNING: Incorrect parameters accountId:%d, userId:%d\n", accountId, userId);
             return null;
         }
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         User u = null;
         List<Child> children = null;
         try {
             //get Possible Children
             TypedQuery<Child> qChild = em.createQuery("FROM Child c WHERE c.account.accountId=:accountId", Child.class);
             qChild.setParameter("accountId", accountId);
             children = qChild.getResultList();
             //get User
             TypedQuery<User> qUser = em.createQuery("FROM User c WHERE userId=:userId", User.class);
             qUser.setParameter("userId", userId);
             u = qUser.getSingleResult();
         } catch (NoResultException nre) {
             System.out.printf("ERROR: No result found with parameters accountId:%d, userId:%d\n", accountId, userId);
             return null;
         } catch (Exception e) {
             System.out.printf("ERROR: Exception with parameters accountId:%d, userId:%d\n", accountId, userId);
             e.printStackTrace();
             return null;
         } finally {
             em.close();
         }
 
         AppointmentVO vo = new AppointmentVO();
         vo.setAccountId(accountId);
         vo.setCurrentUserId(userId);
 
         if (children.size() > 0) {
             //set the first of the list by default
             Child child = children.get(0);
             vo.setKidId(child.getChildId());
             vo.setKidName(child.getFirstName());
         }
 
         vo.setCurrentUserName(u.getFirstName().concat(" ").concat(u.getLastName()));
         //TODO check if we have an existing appointment today in db
 
         //TODO populate AppointmentVo wit app
 
         //else create a new appointment from scratch
         Date currentDate = new Date();
 
         String dateStr = Constants.sdf.format(currentDate);
         if (Calendar.HOUR_OF_DAY > 12) {
             //we consider that is the arrival
             vo.setArrivalDate(dateStr);
             vo.setDeclarationType("arrival");
         } else {
             //we consider that is the departure
             vo.setDepartureDate(dateStr);
             vo.setDeclarationType("departure");
         }
 
         return vo;
     }
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Produces(MediaType.APPLICATION_JSON)
     public AppointmentVO registerAppointment(AppointmentVO appointment) {
         System.out.println("register appointment " + appointment);
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         Appointment entity = null;
         try {
 
             if (appointment.getAppointmentId() != null) {
                 //TODO check
                 TypedQuery<Appointment> qApp = em.createQuery("FROM Appointment a WHERE a.account.accountId=:accountId AND a.arrivalDate EQUALS CURRENT_DATE", Appointment.class);
                 qApp.setParameter("accountId", appointment.getAccountId());
                 Appointment app = qApp.getSingleResult();
 
             } else {
                 entity = new Appointment();
             }
 
             TypedQuery<Child> qChild = em.createQuery("FROM Child c WHERE childId=:childId", Child.class);
             qChild.setParameter("childId", appointment.getKidId());
             Child child = qChild.getSingleResult();
 
             TypedQuery<User> qUser = em.createQuery("FROM User c WHERE userId=:userId", User.class);
             qUser.setParameter("userId", appointment.getCurrentUserId());
             User u = qUser.getSingleResult();
             Date d = null;
             if ("departure".equals(appointment.getDeclarationType())) {
                 entity.setDepartureUser(u);
                 d = Constants.sdf.parse(appointment.getDepartureDate());
                 entity.setDepartureDate(d);
             } else if ("arrival".equals(appointment.getDeclarationType())) {
                 entity.setArrivalUser(u);
                 d = Constants.sdf.parse(appointment.getArrivalDate());
                 entity.setArrivalDate(d);
             } else if ("both".equals(appointment.getDeclarationType())) {
                 entity.setDepartureUser(u);
                 entity.setArrivalUser(u);
                 d = Constants.sdf.parse(appointment.getArrivalDate());
                 entity.setArrivalDate(d);
                 d = Constants.sdf.parse(appointment.getDepartureDate());
                 entity.setDepartureDate(d);
             } else {
                 System.out.println("ERROR: Unknown declaration type: " + appointment.getDeclarationType());
                 return null;
             }
             entity.setAccount(u.getAccount());
             entity.setChild(child);
             em.getTransaction().begin();
             em.persist(entity);
             em.getTransaction().commit();
             em.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
 
 
         return appointment;
     }
 
     @GET
     @Path("/delete/{appointmentId}")
     public Response deleteById(@PathParam("appointmentId") Integer appointmentId) {
         List<Child> c = null;
         EntityManager em = EntityManagerLoaderListener.createEntityManager();
         try {
             em.getTransaction().begin();
             Query query = em.createQuery("DELETE FROM Appointment WHERE appointmentId=:appointmentId");
 
             query.setParameter("appointmentId", appointmentId);
             query.executeUpdate();
             em.getTransaction().commit();
             em.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             em.close();
         }
         return Response.ok().build();
     }
 }
