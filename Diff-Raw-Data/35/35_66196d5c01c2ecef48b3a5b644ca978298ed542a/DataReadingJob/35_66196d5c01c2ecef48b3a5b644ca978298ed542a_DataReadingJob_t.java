 package org.app;
 
 import org.htmlparser.parserapplications.StringExtractor;
 import org.htmlparser.util.ParserException;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class DataReadingJob implements Job {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DataReadingJob.class);
 
     public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
         try {
             LOGGER.info("Site reading started { http://www.autolanka.com/buy.asp }");
             StringExtractor se = new StringExtractor("http://www.autolanka.com/buy.asp");
             String content = se.extractStrings(false);
             extract(content);
         } catch (ParserException e) {
            LOGGER.error("Error occurs while reading the site {http://www.autolanka.com/buy.asp}", e);
         }
     }
 
     private void extract(String content) {
         LOGGER.info("Messages preparation started");
         SimpleDateFormat sdfDate = new SimpleDateFormat("M/dd/yyyy");
         Date now = new Date();
         String strDate = sdfDate.format(now);
         content = content.replaceAll("\\[Date Added: "+strDate+"\\]", "");
         String[] contentArray = content.split("\\[More Details\\]");
         int count = 0;
         for(String oneContent : contentArray) {
             String[] onUnit = oneContent.split("Price:");
             if(onUnit.length == 2) {
                 String message = onUnit[1]
                         .replaceFirst(" ", "")
                         .replaceAll("\n", ",")
                         .replace("/=","")
                         .replace("Additional Info:", "")
                         .replace("Rs.Highest Offer, ", "");
                 if (message.length() <= 160) {
                     count++;
                     String addsId = Integer.toString(count);
                     System.out.println("Trying to add the data added to the tables: {"+ message +"}");
                     dbConnection(addsId, message);
                 }
             }
             if (count == 10) {
                 break;
             }
         }
         LOGGER.info("Data manipulation done");
     }
 
     public void dbConnection(String count, String mesage) {
 
         Connection con = null;
         Statement st = null;
         ResultSet rs = null;
 
         String url = "jdbc:mysql://localhost:3306/automart";
        String user = "malithn";
        String password = "Md&#h&*";
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             con = DriverManager.getConnection(url, user, password);
             st = con.createStatement();
            st.executeUpdate("INSERT INTO adds VALUE('" + count + "', '" + mesage + "','"+"PENDING"+"');");
 
         } catch (SQLException ex) {
             ex.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } finally {
             try {
                 if (st != null) {
                     st.close();
                 }
                 if (con != null) {
                     con.close();
                 }
 
             } catch (SQLException ex) {
                 ex.printStackTrace();
             }
         }
     }
 }
