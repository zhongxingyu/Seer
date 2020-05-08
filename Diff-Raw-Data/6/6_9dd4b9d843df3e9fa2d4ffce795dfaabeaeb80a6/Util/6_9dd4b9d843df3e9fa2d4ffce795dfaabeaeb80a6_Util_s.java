 package ru.anglerhood.lj.client;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: anglerhood
  * Date: 10/4/12
  * Time: 10:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Util {
 
     public static String nullString(Object obj) {
         if (obj == null)
             return "";
         else return obj.toString();
     }
 
     public static String toHTML(String input) {
         if (input == null) return null;
         StringBuilder result = new StringBuilder();
         String body = input.replaceAll("\\n", "\n<br>");
         Pattern pattern = Pattern.compile("<lj user=\"([\\w]+)\">");
         Matcher matcher = pattern.matcher(body);
         int end = 0;
         while(matcher.find()) {
             String ljuser = matcher.group(1);
             result.append(body.substring(end, matcher.start()));
             result.append("<span class=\"ljuser\">");
             result.append(ljuser);
             result.append("</span>");
             end = matcher.end();
         }
         result.append(body.substring(end));
         return result.toString();
     }
 
     public static String replaceJournalLinks(String input, String journal){
         if (input == null) return null;
         StringBuilder result = new StringBuilder();
         Pattern pattern = Pattern.compile("http://[\\w./]+/([\\w]+)/([\\w.?=#]+)");
         Matcher matcher = pattern.matcher(input);
         int start = 0;
         int end = 0;
         while(matcher.find()) {
             String href =  matcher.group(0);
             if(matcher.group(1).equals(journal)) {
                 href = matcher.group(2).replaceAll("\\?thread=\\d+", "").replaceAll("\\?view=\\d+", "");
             }
             result.append(input.substring(end, matcher.start()));
             result.append("<a href=\"");
             result.append(href);
             result.append("\">");
             result.append(href);
             result.append("</a>");
             end = matcher.end();
         }
         result.append(input.substring(end));
         return result.toString();
     }
 
 
 
 }
