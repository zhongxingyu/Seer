 /*
  * Copyright (C) 2009-2010 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class Sarariman implements ServletContextListener {
 
     private final String revision = "$Revision$"; // Do not edit this.  It is set by Subversion.
     private final Collection<Employee> administrators = new EmployeeTable(this, "administrators");
     private final Collection<Employee> approvers = new EmployeeTable(this, "approvers");
     private final Collection<Employee> invoiceManagers = new EmployeeTable(this, "invoice_managers");
     private final Collection<LaborCategoryAssignment> projectBillRates = new LaborCategoryAssignmentTable(this);
     private final Collection<LaborCategory> laborCategories = new LaborCategoryTable(this);
     private LDAPDirectory directory;
     private EmailDispatcher emailDispatcher;
     private CronJobs cronJobs;
     private String logoURL;
 
     private String getRevision() {
         StringBuilder buf = new StringBuilder();
         for (int i = 0; i < revision.length(); i++) {
             char c = revision.charAt(i);
             if (Character.isDigit(c)) {
                 buf.append(c);
             }
         }
 
         return buf.toString();
     }
 
     public String getVersion() {
         return "1.0.32r" + getRevision();
     }
 
     private static Properties lookupDirectoryProperties(Context envContext) throws NamingException {
         Properties props = new Properties();
         String[] propNames = new String[]{Context.INITIAL_CONTEXT_FACTORY, Context.PROVIDER_URL, Context.SECURITY_AUTHENTICATION,
             Context.SECURITY_PRINCIPAL, Context.SECURITY_CREDENTIALS};
 
         for (String s : propNames) {
             props.put(s, envContext.lookup(s));
         }
 
         return props;
     }
 
     private static Properties lookupMailProperties(Context envContext) throws NamingException {
         Properties props = new Properties();
         String[] propNames = new String[]{"mail.from", "mail.smtp.host", "mail.smtp.port"};
 
         for (String s : propNames) {
             props.put(s, envContext.lookup(s));
         }
 
         return props;
     }
 
     public Connection openConnection() {
         try {
             DataSource source = (DataSource)new InitialContext().lookup("java:comp/env/jdbc/sarariman");
             return source.getConnection();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     public Directory getDirectory() {
         return directory;
     }
 
     public EmailDispatcher getEmailDispatcher() {
         return emailDispatcher;
     }
 
     public Collection<Employee> getApprovers() {
         return approvers;
     }
 
     public Collection<Employee> getInvoiceManagers() {
         return invoiceManagers;
     }
 
     public Map<Long, Customer> getCustomers() throws SQLException {
         return Customer.getCustomers(this);
     }
 
     public Map<Long, Project> getProjects() throws SQLException {
         return Project.getProjects(this);
     }
 
     public Collection<Task> getTasks() throws SQLException {
         return Task.getTasks(this);
     }
 
     public Collection<Employee> getAdministrators() {
         return administrators;
     }
 
     public String getLogoURL() {
         return logoURL;
     }
 
     public Collection<LaborCategoryAssignment> getProjectBillRates() {
         return projectBillRates;
     }
 
     public Map<Long, LaborCategory> getLaborCategories() {
         Map<Long, LaborCategory> result = new HashMap<Long, LaborCategory>();
         for (LaborCategory lc : laborCategories) {
             result.put(lc.getId(), lc);
         }
 
         return result;
     }
 
     public void contextInitialized(ServletContextEvent sce) {
         try {
             Context initContext = new InitialContext();
             Context envContext = (Context)initContext.lookup("java:comp/env");
             Properties directoryProperties = lookupDirectoryProperties(envContext);
             directory = new LDAPDirectory(new InitialDirContext(directoryProperties), this);
             boolean inhibitEmail = (Boolean)envContext.lookup("inhibitEmail");
             emailDispatcher = new EmailDispatcher(lookupMailProperties(envContext), inhibitEmail);
             logoURL = (String)envContext.lookup("logoURL");
         } catch (NamingException ne) {
             throw new RuntimeException(ne);  // FIXME: Is this the best thing to throw here?
         }
 
         cronJobs = new CronJobs(this, directory, emailDispatcher);
 
         ServletContext servletContext = sce.getServletContext();
         servletContext.setAttribute("sarariman", this);
         servletContext.setAttribute("directory", directory);
 
         cronJobs.start();
         String hostname;
         try {
             hostname = InetAddress.getLocalHost().getHostName();
         } catch (UnknownHostException uhe) {
            hostname = "unknown host";
         }
 
         for (Employee employee : directory.getByUserName().values()) {
             if (employee.isAdministrator()) {
                emailDispatcher.send(employee.getEmail(), null, "sarariman started",
                        String.format("Sarariman has been started on %s.", hostname));
             }
         }
     }
 
     public void contextDestroyed(ServletContextEvent sce) {
         // FIXME: Should we worry about email that has been queued but not yet sent?
         cronJobs.stop();
     }
 
 }
