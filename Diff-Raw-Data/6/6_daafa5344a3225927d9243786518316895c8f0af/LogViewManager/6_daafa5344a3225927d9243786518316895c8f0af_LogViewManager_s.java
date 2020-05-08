 package org.makumba.parade.view.managers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Log;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.tools.HtmlUtils;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class LogViewManager {
     
     private static final String LAST_HOUR = "hour";
     private static final String LAST_RESTART = "restart";
     private static final String LAST_DAY = "day";
     private static final String LAST_WEEK = "week";
     
         
 
     public String getLogView(Session s, String context, String filter, Integer years, Integer months, Integer days) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("logs.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Creating the data model
         SimpleHash root = new SimpleHash();
         root.put("context", context);
         root.put("year", years.toString());
         root.put("month", ""+(months.intValue()+1));
         root.put("day", days.toString());
         
         Calendar cal = GregorianCalendar.getInstance();
         cal.clear();
         
         // here we have several filtering possibilities
         // either by date, with day, month, year
         // then depending on the context, either a specific one, or the root one (parade), or all
         // then also by quick filter
         
         String contextQuery = "l.context = :context";
         
         if(context.equals("all"))
             contextQuery = "";
         if(context.equals("(root)"))
            contextQuery = "l.context is null or l.context = 'parade2'";
         
         String dateQuery = "l.date > :myDate";
         
        String query = "from Log l where ("+contextQuery+(contextQuery.length()==0?"":") and ")+dateQuery;
         
         Query q = s.createQuery(query);
         q.setCacheable(false);
         
         if(!context.equals("all") && !context.equals("(root)"))
           q.setString("context", context);
         
         if(!filter.equals("none")) {
             cal.setTime(new Date());
             
             if(filter.equals(LAST_HOUR)) {
                 cal.add(Calendar.HOUR_OF_DAY, -1);
             }
             if(filter.equals(LAST_DAY)) {
                 cal.add(Calendar.DAY_OF_MONTH, -1);
             }
             if(filter.equals(LAST_WEEK)) {
                 cal.add(Calendar.WEEK_OF_MONTH, -1);
             }
             if(filter.equals(LAST_RESTART)) {
                 //FIXME there's probably more performant way to do this
                 //FIXME like, using a report query
                 Query q1 = s.createQuery("from Log l where l.message = 'Server restart' order by l.date DESC");
                 Date d = null;
                 if(q1.list().size() > 0) {
                     d = ((Log) q1.list().get(0)).getDate();
                 }
                 if(d != null)
                     cal.setTime(d);
             }
         } else {
             cal.set(years.intValue(), months.intValue(), days.intValue());
         }
         
         System.out.println("cal time we set: "+cal.getTime());
         q.setTimestamp("myDate", cal.getTime());
         
         
         List<Log> entries = q.list();
         List viewEntries = new LinkedList();
         for(int i=0; i<entries.size(); i++) {
             SimpleHash entry = new SimpleHash();
             if(entries.get(i).getMessage().trim().length() == 0) //skip blank lines
                 continue;
             if(entries.get(i).getMessage().equals("Server restart"))
                 entry.put("serverRestart", true);
             else
                 entry.put("serverRestart", false);
             entry.put("message", HtmlUtils.string2html(entries.get(i).getMessage()));
             entry.put("date", entries.get(i).getDate().toString());
             entry.put("level", entries.get(i).getLevel());
             entry.put("user", (entries.get(i).getUser() == null)?"system":entries.get(i).getUser());
             entry.put("context", (entries.get(i).getContext() == null)?"parade2":entries.get(i).getContext());
             
             viewEntries.add(entry);
         }
         
         root.put("entries", viewEntries);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return result.toString();
         
     }
     
     public String getLogMenuView(Session s, String context, String filter, Integer years, Integer months, Integer days) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("logsHeader.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Creating the data model
         SimpleHash root = new SimpleHash();
         root.put("context", context);
         root.put("year", years.toString());
         root.put("month", ""+(months.intValue()+1));
         root.put("day", days.toString());
         root.put("filter", filter);
         
         List rows = new LinkedList();
         
         Parade p = (Parade) s.get(Parade.class, new Long(1));
         
         for (Iterator i = p.getRows().keySet().iterator(); i.hasNext();) {
             Row currentRow = (Row) p.getRows().get(i.next());
             String displayName = currentRow.getRowname();
             if (currentRow.getRowname() == "")
                 displayName = "(root)";
             
             rows.add(displayName);
         }
         
         root.put("rows", rows);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return result.toString();
         
     }
 }
