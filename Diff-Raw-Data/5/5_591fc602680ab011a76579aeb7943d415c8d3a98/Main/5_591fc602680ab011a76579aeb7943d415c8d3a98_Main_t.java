 package cz.edu.x3m;
 
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import cz.edu.x3m.net.objects.Subject;
 import cz.edu.x3m.steps.LoggedInStep;
 import cz.edu.x3m.steps.LoginStep;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Jan
  */
 public class Main {
 
     public static final Logger LOGGER = Logger.getLogger("cz.edu.x3m");
     public static final File DATA = new File(".terms");
     public static final long SLEEP_TIME = 2 * 60 * 1000;
 
     public Main() throws InterruptedException {
         while (true) {
             doWork();
             System.out.println("Sleep");
             synchronized (this) {
                 wait(SLEEP_TIME);
             }
         }
     }
 
     public static void main(String[] args) throws IOException, InterruptedException {
         Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
         Logger.getLogger("cz.edu.x3m").setLevel(Level.OFF);
         new Main();
     }
 
     private boolean doWork() {
 
         System.out.println("Login page");
         Client client = new Client();
         HtmlPage LoginPage = client.getLoginPage();
 
         System.out.println("Logged in page");
         LoginStep loginStep = new LoginStep(LoginPage);
         HtmlPage loggedInPage = loginStep.login();
 
         LoggedInStep loggedInStep = new LoggedInStep(loggedInPage);
         if (loggedInStep.hasLoginForm()) {
             System.out.println("FULL");
             return false;
         }
         List<cz.edu.x3m.net.objects.Subject> currTerms = loggedInStep.getTerms();
 
         List<Subject> prevTerms = loadPrevTerms();
         StageChangeResult result = TermComparator.compareSubjects(prevTerms, currTerms);
 
         System.out.println(result);
        if (result.changes.isEmpty()) {
             return false;
         } else {
             System.out.println(currTerms);
             saveCurrTerms(currTerms);
             System.out.println("Sending mail");
            if (!prevTerms.isEmpty()) sendEMail(result);
             return true;
         }
 
 
     }
 
     private List<Subject> loadPrevTerms() {
         BufferedReader reader = null;
         List<Subject> result = new ArrayList<>();
         try {
             reader = new BufferedReader(new FileReader(DATA));
             String line;
             while ((line = reader.readLine()) != null) {
                 if (line.isEmpty()) {
                     continue;
                 }
                 result.add(new Subject(line));
             }
             return result;
         } catch (IOException ex) {
             return result;
         }
     }
 
     private void saveCurrTerms(List<Subject> items) {
         BufferedWriter writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(DATA));
             for (int i = 0; i < items.size(); i++) {
                 Subject subject = items.get(i);
                 writer.write(subject.asOutput());
                 writer.newLine();
             }
             writer.close();
         } catch (IOException ex) {
             return;
         }
     }
 
     private void sendEMail(StageChangeResult result) {
         Client c = new Client();
         c.getSendEmailPage(result);
     }
 }
