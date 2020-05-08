 package controllers.main;
 
 import com.jamonapi.MonitorFactory;
 import com.jamonapi.utils.Misc;
 import controllers.conge.HolidayRequests;
 import controllers.ins.Attendances;
 import controllers.sa.SAController;
 import controllers.security.Check;
 import controllers.security.Secure;
 import models.admin.Monitoring;
 import models.conge.HolidayBook;
 import models.conge.HolidayRequest;
 import models.ins.Contract;
 import models.ma.*;
 import models.main.Person;
 import models.main.PersonStatus;
 import models.sa.LSPStat;
 import models.sa.Shop;
 import models.security.Role;
 import models.security.User;
 import notifiers.Notifier;
 import play.data.validation.Required;
 import play.libs.Codec;
 import play.mvc.Controller;
 import utils.Dates;
 import utils.ModuleDispatch;
 
 import java.util.*;
 
 public class Application extends Controller {
 
     public static void index() {
         try {
             User user = User.loadFromSession();
 
             if (user == null) {
                 Secure.login();
             }
 
             session.put("title", "PR - WEB");
             if(user.hasRole("allUsers")){
                 session.put("isAdmin", true);
             }
             ModuleDispatch.valueOf(user.module).homePage(session);
         } catch (Throwable ex) {
             ex.printStackTrace();
         }
     }
 
     @Check("admin")
     public static void adminIndex() throws Throwable {
         Secure.checkAccess();
         List<Monitoring> stats = new ArrayList<Monitoring>();
         Object[][] data = Misc.sort(MonitorFactory.getRootMonitor().getBasicData(), 3, "desc");
         int lm = 10;
         for (Object[] row : data) {
             if (row[0].toString().length() > lm) {
                 lm = row[0].toString().length();
             }
         }
         for (Object[] row : data) {
             if (((Double) row[1]) > 0 && ((Double) row[2]) > 1000) {
                 Monitoring m = new Monitoring();
                 m.element = (String) row[0];
                 Double hits = (Double) row[1];
                 m.hits = hits.intValue();
                 m.avg = (Double) row[2];
                 m.min = (Double) row[6];
                 m.max = (Double) row[7];
                 stats.add(m);
 
             }
         }
 
         Collections.sort(stats);
 
         render(stats);
 
     }
 
     @Check({"adminINS", "userINS"})
     public static void insIndex() throws Throwable {
         Secure.checkAccess();
 
         long nbVolunteer = Person.nbPersons(PersonStatus.volunteer.value);
         long nbTIG = Person.nbPersons(PersonStatus.tig.value);
         long nbArt60 = Person.nbPersons(PersonStatus.art60.value);
         long nbResident = Person.nbPersons(PersonStatus.resident.value);
 
         List<Contract> endContracts = Contract.byEndAtCurrentWeek(PersonStatus.getINSStatus());
         List<Person> birthDayPersons = Person.birthDaysByStatus(PersonStatus.getINSStatus());
 
 
         render(nbArt60, nbResident, nbTIG, nbVolunteer, endContracts, birthDayPersons);
     }
 
     @Check({"adminChief", "userChief", "divisionChief"})
     public static void chiefIndex() throws Throwable {
         Secure.checkAccess();
         Attendances.byWeekAndDepartment(new Date(), 0, 0);
     }
 
     @Check({"adminMA", "userMA", "corMA"})
     public static void maIndex() throws Throwable {
         Secure.checkAccess();
         User user = User.find("username = ?", Secure.Security.connected()).first();
         if (user.person.floor == null) {
             maIndexNoFloor();
         } else {
             maIndexFloor(user.person.floor);
         }
     }
 
     private static void maIndexFloor(Floor floor) {
         long nbResident = Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true ", "000");
 
         List<ResidentRequest> rRequests = ResidentRequest.find("rdvDay = ? "
                 + "and status = 1 "
                 + "and (personHome is null "
                 + "or personHome.floor = ?) ", new Date(), floor).fetch();
 
         List<RDVResident> rdvResidents = RDVResident.find("rdvDate = ? "
                 + "and (resident.room is null "
                 + "or resident.room.floor = ?) "
                 + "order by rdvTime", new Date(), floor).fetch();
 
 
         Calendar d = new GregorianCalendar();
         List<Person> birthDays = Person.birthDaysResidentByFloor(floor);
 
         long nbRoom = Room.count();
         Hostel h = Hostel.find("byName", "Petits Riens").first();
 
         long freeRoom = nbRoom - h.bookedUp - nbResident;
 
         GregorianCalendar dSeven = new GregorianCalendar();
         dSeven.add(Calendar.DAY_OF_MONTH, 7);
 
         List<Evaluation> evaluations = Evaluation.find("evaluationDate >= ? "
                 + "and evaluationDate <= ? "
                 + "and actif = true "
                 + "and (resident.room is null "
                 + "or resident.room.floor = ?) "
                 + "order by evaluationDate", d.getTime(), dSeven.getTime(), floor).fetch();
 
         List debts = Debt.em().createQuery("select d.resident.id, d.resident.name,"
                 + "d.resident.firstname, SUM(d.monthlyPayement) "
                 + "from Debt d where d.actif = true and d.payementMode = ? "
                 + "and (d.resident.room is null "
                 + "or d.resident.room.floor = ?) "
                 + "group by d.resident.id, d.resident.name,"
                 + "d.resident.firstname").
                 setParameter(1, d.get(Calendar.DAY_OF_MONTH)).
                 setParameter(2, floor).
                 getResultList();
 
         List<Person> redList = Person.find("redList = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         List<Person> noReadmission = Person.find("noReadmission = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         List<Person> noForMA = Person.find("noForMA = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         render(nbResident, rRequests, birthDays, d, freeRoom, h, rdvResidents, evaluations,
                 debts, redList, noReadmission, noForMA);
     }
 
     private static void maIndexNoFloor() {
         long nbResident = Person.count("from Person p,in(p.personStatus) ps "
                 + "where ps = ? "
                 + "and p.enabled = true ", "000");
         List<ResidentRequest> rRequests = ResidentRequest.find("rdvDay = ? and status = 1", new Date()).fetch();
 
         List<RDVResident> rdvResidents = RDVResident.find("rdvDate = ? "
                 + "and resident.enabled = true "
                 + "order by rdvTime", new Date()).fetch();
 
 
         GregorianCalendar d = new GregorianCalendar();
         List<Person> birthDays = Person.birthDaysResident();
 
         long nbRoom = Room.count();
         Hostel h = Hostel.find("byName", "Petits Riens").first();
 
         long freeRoom = nbRoom - h.bookedUp - nbResident;
 
         GregorianCalendar dSeven = new GregorianCalendar();
         dSeven.add(Calendar.DAY_OF_MONTH, 7);
 
         List<Evaluation> evaluations = Evaluation.find("evaluationDate >= ? "
                 + "and evaluationDate <= ? "
                 + "and actif = true "
                 + "order by evaluationDate", d.getTime(), dSeven.getTime()).fetch();
 
         List debts = Debt.em().createQuery("select d.resident.id, d.resident.name,"
                 + "d.resident.firstname, SUM(d.monthlyPayement) "
                 + "from Debt d where d.actif = true and d.payementMode = ? "
                 + "group by d.resident.id, d.resident.name,"
                 + "d.resident.firstname").
                 setParameter(1, d.get(Calendar.DAY_OF_MONTH)).
                 getResultList();
 
         List<Person> redList = Person.find("redList = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         List<Person> noReadmission = Person.find("noReadmission = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         List<Person> noForMA = Person.find("noForMA = true "
                 + "and enabled = true "
                 + "order by name, firstname").fetch();
 
         render(nbResident, rRequests, birthDays, d, freeRoom, h, rdvResidents, evaluations,
                 debts, redList, noReadmission, noForMA);
     }
 
     @Check({"adminSA", "userSA"})
     public static void saIndex() throws Throwable {
         Secure.checkAccess();
         User user = User.find("byUsername", Secure.Security.connected()).<User>first();
 
         if (user.roles.contains(new Role("adminSA"))) {
             SAController.adminSAIndex();
         } else {
             SAController.userSAIndex();
         }
     }
 
     @Check({"adminLSP"})
     public static void lspIndex() throws Throwable {
         Secure.checkAccess();
 
         int currentYear = new GregorianCalendar().get(Calendar.YEAR);
         List<Calendar> dates = new ArrayList<Calendar>();
         dates.add(Dates.getFirstDayOfYear(currentYear-2));
         dates.add(Dates.getFirstDayOfYear(currentYear-1));
         dates.add(Dates.getFirstDayOfYear(currentYear));
 
         List<String> monthNames = Dates.getMonthNames();
         Map<Integer, Double> totalListCurrentYear = LSPStat.totalByMonth(dates.get(2));
         Map<Integer, Double> totalListYear1 = LSPStat.totalByMonth(dates.get(1));
         Map<Integer, Double> totalListYear2 = LSPStat.totalByMonth(dates.get(0));
 
         render(monthNames, totalListCurrentYear, totalListYear1,totalListYear2, dates);
     }
 
     @Check({"adminRH", "userRH"})
     public static void rhIndex() throws Throwable {
         Secure.checkAccess();
 
         long nbEmploye = Person.nbPersons(PersonStatus.employe.value);
         long nbWorker = Person.nbPersons(PersonStatus.worker.value);
 
         List<Contract> endContracts = Contract.byEndAtCurrentWeek(PersonStatus.getRHStatus());
         List<Person> birthDayPersons = Person.birthDaysByStatus(PersonStatus.getRHStatus());
 
 
         render(nbWorker, nbEmploye, endContracts, birthDayPersons);
     }
 
     public static void confirmUser(String confirm) {
         User user = User.find("byConfirm", confirm).first();
         render(user);
     }
 
     public static void saveNewPassword(String username, String confirm, @Required String newpassword, @Required String confirmpassword) {
         User user = User.find("byConfirm", confirm).first();
 
         if (validation.hasError("newpassword")) {
             flash.error("error");
             flash.keep("url");
             flash.put("errorNewpassword", "error.emptyPassword");
             params.flash();
         }
         if (validation.hasError("confirmpassword")) {
             flash.error("error");
             flash.keep("url");
             flash.put("errorNewpassword", "error.badConfirmPassword");
             params.flash();
         }
 
         if (!newpassword.isEmpty() && !confirmpassword.isEmpty()) {
             if (!newpassword.equals(confirmpassword)) {
                 flash.keep("url");
                 flash.error("error");
                 flash.put("errorNewpassword", "error.badConfirmPassword");
                 params.flash();
             }
 
         }
 
         if (flash.get("error") == null) {
             flash.put("success", "user.confirmed");
 
             user.password = Codec.hexMD5(newpassword);
             user.enabled = true;
             user.confirm = Codec.UUID();
 
             user.save();
         }
 
         render("main/Application/confirmUser.html", user);
     }
 
     public static void lostPassword() {
         render();
     }
 
     public static void sendMailLostPassword(@Required String username) {
         User user = User.find("byUsername", username).first();
 
         if (validation.hasError("username")) {
             flash.error("error");
             flash.keep("url");
             flash.put("emptyUsername", "error.emptyUsername");
             params.flash();
         }
 
         if (user == null) {
             flash.error("error");
             flash.keep("url");
             flash.put("badUsername", "error.badUsername");
             params.flash();
         }
 
         if (flash.get("error") == null) {
             flash.put("success", "info.lostPasswordSuccess");
             try {
                 Notifier.sendPasswordReset(user);
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }
 
         render("main/Application/lostPassword.html");
     }
 
     public static void cptIndex() {
         List<Shop> shops = Shop.getShops();
 
         render(shops);
     }
 
     public static void dirIndex() {
         long nbResident = Person.nbResidents();
         long nbEmploye = Person.nbPersons(PersonStatus.employe.value);
         long nbWorker = Person.nbPersons(PersonStatus.worker.value);
         long nbVolunteer = Person.nbPersons(PersonStatus.volunteer.value);
         long nbTIG = Person.nbPersons(PersonStatus.tig.value);
         long nbArt60 = Person.nbPersons(PersonStatus.art60.value);
 
         List<Person> birthDayPersons = Person.birthDaysByStatus(PersonStatus.getRHStatus());
 
         Calendar date = new GregorianCalendar();
         Calendar lastYear = new GregorianCalendar();
         lastYear.add(Calendar.YEAR, -1);
 
         List<String> monthNames = Dates.getMonthNames();
         Map<Integer, Double> totalList = LSPStat.totalByMonth(date);
         Map<Integer, Double> totalList2 = LSPStat.totalByMonth(lastYear);
 
         int year = date.get(Calendar.YEAR);
         int year2 = lastYear.get(Calendar.YEAR);
 
         render(nbArt60, nbEmploye, nbResident, nbTIG, nbVolunteer, nbWorker,
                 birthDayPersons, monthNames, totalList, totalList2, year, year2);
     }
 
     public static void validateHolidaysRequest(String validToken, String username) {
         HolidayRequest holidayRequest = HolidayRequest.byValidToken(validToken);
 
         if (holidayRequest == null) {
             session.remove("username");
            render("Application/badHolidayRequest.html");
         }
 
         List<HolidayBook> holidayBooks = HolidayBook.byRequest(holidayRequest);
         User user = User.loadByUsername(username);
 
         if (user == null) {
             session.remove("username");
            render("Application/badHolidayRequest.html");
         }
 
         session.put("username", user.username);
 
         if(holidayRequest.dateClosed != null){
             HolidayRequests.editByAdmin(holidayRequest.id);
         }
 
         if (user.hasAtLeastOneRole(Arrays.asList("adminRH", "userRH"))) {
             holidayRequest.validateByRH();
         } else {
             holidayRequest.validateByChief(holidayBooks);
         }
         try {
             Notifier.sendHolidayResponse(holidayRequest);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         HolidayRequests.editByAdmin(holidayRequest.id);
     }
 
     public static void routes(){
         response.cacheFor("30d");
         render("main/Application/routes.js");
     }
 
     public static void messages(){
         response.cacheFor("30d");
         render("main/Application/messages.js");
     }
 }
