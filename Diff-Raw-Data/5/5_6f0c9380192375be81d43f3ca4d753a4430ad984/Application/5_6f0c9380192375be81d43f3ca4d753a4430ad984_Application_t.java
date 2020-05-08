 package controllers;
 
 import static controllers.Secured.SESSION_KEY_EMAIL;
 import static controllers.Secured.SESSION_KEY_USERNAME;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Locale;
 
 import models.Education;
 import models.Employment;
 import models.Project;
 import models.Skill;
 import models.SkillGroup;
 import models.User;
 import models.forms.CredentialsFormData;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.joda.time.DateTime;
 
 import play.Logger;
 import play.api.templates.Html;
 import play.data.Form;
 import play.i18n.Lang;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.contact;
 import views.html.index;
 
 public class Application extends Controller {
     private static final Form<CredentialsFormData> credentialsForm = Form.form(CredentialsFormData.class);
     public final static String SESSION_LANGKEY = "langkey";
     
     public static Result index(String langKey) {
         setSessionLang(langKey);
         List<Project> spProjects = Project.findForStartpage();
         Collections.sort(spProjects, new NewestProjectsFirstComparator());
         return ok(index.render(Project.findCurrent(), spProjects));
     }
 
     public static Result contact(String langKey) {
         setSessionLang(langKey);
 
         return ok(contact.render());
     }
 
     public static Result profile(String langKey) {
         setSessionLang(langKey);
 
         List<Employment> empl = new ArrayList<Employment>();
 
         if (langKey.equals("de")) {
             empl.add(new Employment(new DateTime(2012, 4, 1, 1, 1), null, "Freiberufler", "", "http://www.julius-seltenheim.com", new String[] { "Webentwicklung", "Softwareentwicklung",
                     "Datenbankentwicklung", "Beratung" }));
             empl.add(new Employment(
                     new DateTime(2012,10, 1, 1, 1), 
                     null,
                     "Teamleiter & Javaentwickler", 
                     "Docear", 
                     "http://docear.org", 
                     new String[] { "Uniprojekt, Entwicklung einer Real-Time Collaborative Mindmapping Tool","Kommunikation mit Auftraggeber", "Backendentwicklung (Java, OSGI, Play Framework, Akka,...)"}));
             empl.add(new Employment(
                     new DateTime(2013, 2, 1, 1, 1), 
                     new DateTime(2013, 4, 1, 1, 1),
                     "Freiberufler", 
                     "first fox in space", 
                     "http://firstfox.com", 
                     new String[] { "Planung einer Applikation", "Implementation","IT-Infrastruktur (Jenkins CI, Linux-Server)"}));
             empl.add(new Employment(
                     new DateTime(2012, 4, 1, 1, 1), 
                     new DateTime(2012,11, 1, 1, 1),
                     "Freiberufler", 
                     "Best Ants", 
                     "", 
                     new String[] { "Planung einer Applikation", "Entwicklung eines Prototyps"}));
             empl.add(new Employment(new DateTime(2011, 5, 1, 1, 1), new DateTime(2011, 12, 1, 1, 1), "HTW Berlin", "Studentische Hilfskraft", "http://www.htw-berlin.de", new String[] {
                     "Entwicklung von Prototypen", "Unterstützende Arbeiten" }));
             empl.add(new Employment(new DateTime(2010, 10, 1, 1, 1), new DateTime(2011, 2, 1, 1, 1), "Canary Data Solutions Ltd", "Praktikant im Bereich \".Net Softwareentwicklung\"",
                     "http://canary.co.nz", new String[] { "Softwareentwicklung", "Kundensupport", "Administration", "Refactoring" }));
             empl.add(new Employment(new DateTime(2010, 3, 1, 1, 1), new DateTime(2010, 9, 1, 1, 1), "HTW Berlin", "Tutor für Mathematik", "http://www.htw-berlin.de", new String[] {
                     "Vorbereitung und Durchführung von 2 Tutorien pro Woche", "Beantwortung von Fragen per E-Mail" }));
            empl.add(new Employment(new DateTime(2008, 7, 1, 1, 1), new DateTime(2009, 3, 1, 1, 1), "Caritas", "Zivildienst", "http://www.invia-center-berlin.de", new String[] { "Kochen", "Catering",
                     "Für Gäste sorgen", "Vorbereitung", "Servieren", "Praktikanten einweisen", "Lieferungen" }));
         } else {
             empl.add(new Employment(new DateTime(2012, 4, 1, 1, 1), null, "Freelancer", "", "http://www.julius-seltenheim.com", new String[] { "Webdevelopment", "Softwaredevelopment",
                     "Databasedevelopment", "Consulting" }));
             empl.add(new Employment(
                     new DateTime(2012,10, 1, 1, 1), 
                     null,
                     "Team Leader & Java Developer", 
                     "Docear", 
                     "http://docear.org", 
                     new String[] { "University project about developping a real time mind map collaboration tool","Communication with product owner", "Backend development (Java, OSGI, Play Framework, Akka,...)"}));
             empl.add(new Employment(
                     new DateTime(2013, 2, 1, 1, 1), 
                     new DateTime(2013, 4, 1, 1, 1),
                     "Freelancer", 
                     "first fox in space", 
                     "http://firstfox.com", 
                     new String[] { "Design an application based on Play Framework (Java) and MySQL", "Implementation","Set up Jenkins Continuous Integration and Linux servers"}));
             empl.add(new Employment(
                     new DateTime(2012, 4, 1, 1, 1), 
                     new DateTime(2012,11, 1, 1, 1),
                     "Freelancer", 
                     "Best Ants", 
                     "", 
                     new String[] { "Design an application based on PHP, the Zend Framework and MySQL", "Start development of a prototype"}));
             empl.add(new Employment(new DateTime(2011, 5, 1, 1, 1), new DateTime(2011, 12, 1, 1, 1), "HTW Berlin", "Student Research Assistant", "http://www-en.htw-berlin.de", new String[] {
                     "Prototyping", "General Tasks" }));
             empl.add(new Employment(new DateTime(2010, 10, 1, 1, 1), new DateTime(2011, 2, 1, 1, 1), "Canary Data Solutions Ltd", ".Net Developer Intern", "http://canary.co.nz", new String[] {
                     "Software Development", "Customer Support", "Maintenance", "Refactoring" }));
             empl.add(new Employment(new DateTime(2010, 3, 1, 1, 1), new DateTime(2010, 9, 1, 1, 1), "HTW Berlin", "Tutor for Math", "http://www-en.htw-berlin.de", new String[] {
                     "Prepare and perform 2 tutorials per week", "E-mail support" }));
            empl.add(new Employment(new DateTime(2008, 7, 1, 1, 1), new DateTime(2009, 3, 1, 1, 1), "Caritas", "Compulsory Community Service", "http://www.invia-center-berlin.de", new String[] {
                     "Cooking", "Catering", "Care for guests", "Preparing", "Serving", "Briefing Interns", "Delivery" }));
         }
 
         List<Education> edus = new ArrayList<Education>();
         if (langKey.equals("de")) {
             edus.add(new Education("2012", "(2014)", "HTW Berlin", "http://www.htw-berlin.de/", "---", "", "M.Sc. in Internationaler Medieninformatik", "Spezialisierung: Visual Computing"));
             edus.add(new Education("2009", "2012", "HTW Berlin", "http://www.htw-berlin.de/", "1.3, sehr gut (A)", "", "B.Sc. in Internationaler Medieninformatik", ""));
             edus.add(new Education("2001", "2008", "Hans und Hilde Coppi Gymnasium, Berlin", "http://www.coppi-gym.de/", "2.1", "", "Abitur", ""));
         } else {
             edus.add(new Education("2012", "(2014)", "HTW Berlin, University of Applied Science", "http://www-en.htw-berlin.de/", "---", "", "M.Sc. in International Media and Computing",
                     "Specialisation: Visual Computing"));
             edus.add(new Education("2009", "2012", "HTW Berlin, University of Applied Science", "http://www-en.htw-berlin.de/", "1.3, very good (A)", "", "B.Sc. in International Media and Computing",
                     ""));
             edus.add(new Education("2001", "2008", "Hans und Hilde Coppi Gymnasium, Berlin", "http://www.coppi-gym.de/", "2.1", "", "Abitur", ""));
         }
         
         Collections.sort(empl, new Comparator<Employment>() {
             @Override
             public int compare(Employment o1, Employment o2) {
                 final DateTime end1 = o1.getTo();
                 final DateTime end2 = o2.getTo();
                 if(end1 == null && end2 == null) {
                     return 0;
                 } else if(end1 == null) {
                     return -1;
                 } else if(end2 == null) {
                     return 1;
                 } else {
                     return -end1.compareTo(end2);
                 }
             }
         });
 
         return ok(views.html.profile.render(empl, edus));
     }
 
     public static Result skills(String langKey) {
         setSessionLang(langKey);
 
         final List<SkillGroup> skillGroups = new ArrayList<SkillGroup>();
         final boolean isDe = langKey.equals("de");
 
         final String description = isDe ? "<p>" + "Hier finden sie einen Auszug aus der Liste meiner technischen Qualifikationen.<br>" + "Auf Soft Skills gehe ich an dieser Stelle nicht ein.<br>"
                 + "Jedoch stehe ich gerne für ein Gespräch bereit, in dem sie sich ein Bild von meiner Persönlichkeit machen können." + "</p>" : "<p>"
                 + "This page contains an excerpt of my technical qualifications.<br>" + "Feel free to contact me to get an image of my personality." + "</p>";
 
         List<Skill> skills;
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("C#", 0.75));
         skills.add(new Skill("PHP", 0.75));
         skills.add(new Skill("Java", 0.8));
         skills.add(new Skill("C/C++", 0.15));
         skills.add(new Skill("Javascript", 0.65));
         skills.add(new Skill("CoffeScript", 0.30));
         skillGroups.add(new SkillGroup(isDe ? "Programmiersprachen" : "Programming Languages", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("Visual Studio", 0.65));
         skills.add(new Skill("Eclipse", 0.7));
         skills.add(new Skill("NetBeans", 0.6));
         skillGroups.add(new SkillGroup("IDEs", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("Windows", 1));
         skills.add(new Skill("Mac OS X 10.x", 0.3));
         skills.add(new Skill("Linux", 0.4));
         skillGroups.add(new SkillGroup(isDe ? "Betriebssysteme" : "Operation Systems", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill(isDe ? "Relationelle Datenbanken entwerfen" : "Database Engineering", 0.75));
         skills.add(new Skill("MySQL", 0.7));
         skills.add(new Skill("MSSQL", 0.4));
         skillGroups.add(new SkillGroup(isDe ? "Netzwerke & Datenbanken" : "Networking & Databases", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("Play! Framework 2", 0.75));
         skills.add(new Skill("Akka", 0.75));
         skills.add(new Skill("Zend Framework 1", 0.3));
         skills.add(new Skill("Zend Framework 2", 0.4));
         skills.add(new Skill("Twitter Bootstrap", 0.6));
         skills.add(new Skill("jQuery", 0.6));
         skills.add(new Skill("XNA 4", 0.55));
         skills.add(new Skill("Processing", 0.90));
         skillGroups.add(new SkillGroup(isDe ? "Frameworks & Bibliotheken" : "Frameworks & Libraries", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("SVN", 0.85));
         skills.add(new Skill("Git", 0.70));
         skillGroups.add(new SkillGroup(isDe ? "Versionierungstools" : "Source Code Management", skills));
 
         skills = new ArrayList<Skill>();
         skills.add(new Skill("Maven", -1));
         skills.add(new Skill("Ivy", -1));
         skills.add(new Skill("Sbt", -1));
         skillGroups.add(new SkillGroup("Dependency Management & Build tools", skills));
 
         return ok(views.html.skills.render(Html.apply(description), skillGroups));
     }
 
     public static Result autoSelectLanguage() {
 
         if (request().host().endsWith(".de"))
             return redirect(routes.Application.index("de"));
         else
             return redirect(routes.Application.index("en"));
     }
 
     public static void setSessionLang(String langKey) {
         session(SESSION_LANGKEY, langKey);
     }
 
     public static String getSessionLang() {
         return session(SESSION_LANGKEY);
     }
 
     public static Locale getCurrentLocale() {
         return Lang.forCode(getSessionLang()).toLocale();
     }
 
     public static String getCurrentRouteWithOtherLang(String langKey) {
         return "/" + langKey + request().uri().substring(3);
     }
 
     public static Html messages(String key) {
         return Html.apply(Messages.get(Lang.forCode(getSessionLang()), key));
     }
 
     public static String toLower(String string) {
         return string.toLowerCase();
     }
 
     public static Result register() {
         return TODO;
     }
 
     public static Result login(String langKey) throws JsonGenerationException, JsonMappingException, IOException {
         final Form<CredentialsFormData> filledForm = credentialsForm.bindFromRequest();
 
         // Logger.debug(filledForm.toString());
 
         Result result;
         if (UsersController.getLoggedInUser() != null) {
             result = redirect(routes.Application.index(langKey));
 
         } else if (filledForm.hasErrors()) {
             result = badRequest(views.html.login.render());
 
         } else {
             final CredentialsFormData credentials = filledForm.get();
             User user = authenticate(credentials.getUsername(), credentials.getPassword());
             final boolean authenticationSuccessful = user != null;
             if (authenticationSuccessful) {
                 session(SESSION_KEY_EMAIL, user.getEmail());
                 session(SESSION_KEY_USERNAME, user.getUsername());
 
                 result = redirect(routes.Application.index(langKey));
             } else {
                 filledForm.reject("The credentials doesn't match any user.");
                 Logger.debug(credentials.getUsername() + " is unauthorized");
                 result = unauthorized(views.html.login.render());
             }
         }
         return result;
     }
 
     public static User authenticate(String username, String password) {
         User user = User.findByNameAndPassword(username, password);
         return user;
     }
     
     private static final class NewestProjectsFirstComparator implements Comparator<Project> {
         @Override
         public int compare(Project o1, Project o2) {
             final DateTime end1 = o1.getDevelopmentEnd();
             final DateTime end2 = o2.getDevelopmentEnd();
             if(end1 == null && end2 == null) {
                 return 0;
             } else if(end1 == null) {
                 return 1;
             } else if(end2 == null) {
                 return -1;
             } else {
                 return -end1.compareTo(end2);
             }
         }
     }
 }
