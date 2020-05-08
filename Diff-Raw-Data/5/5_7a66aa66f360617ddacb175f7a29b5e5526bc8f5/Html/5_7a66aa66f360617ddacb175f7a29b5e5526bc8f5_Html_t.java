 package com.ezra.test;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.util.*;
 
 public class Html {
     private String url_yes;
 
     private String Sources;
 
    Html(String a){url_yes=a;Sources=getHtmlString();}
 
    private String getHtmlString() {
         try {
             URL url = new URL(url_yes);
             URLConnection ucon = url.openConnection();
             InputStream instr = ucon.getInputStream();
             BufferedInputStream bis = new BufferedInputStream(instr);
             ByteArrayBuffer baf = new ByteArrayBuffer(500);
             int current = 0;
             while ((current = bis.read()) != -1) {
                 baf.append((byte) current);
             }
             //对应网页编码
             Sources=EncodingUtils.getString(baf.toByteArray(), "GBK");
             return EncodingUtils.getString(baf.toByteArray(), "GBK");
         } catch (Exception e) {
             return "";
         }
     }
 
     //for home page
     public ArrayList getAllUrl(){
         Pattern new_col = Pattern.compile("<table width=\"100%\" cellpadding=\"3\">(.*?)</table>");
         Matcher matchedCol = new_col.matcher(Sources);
         String title=new String();
         ArrayList url=new ArrayList();
         while (matchedCol.find()){
             title = matchedCol.group(1);
             Pattern news = Pattern.compile("<a.*?href=\"(.*?)\"\\s+target=\"_blank\"\\stitle=.*?>(.*?)</a>");
             Matcher matchedTitle = news.matcher(title);
             //get all of them by while
             while (matchedTitle.find()){
                 url.add(url_yes+matchedTitle.group(1));
             }
         }
         return url;
     }
 
     public String getCol(){
         String Col=new String();
         //xueshengyuandi
         Pattern patterncol_student=Pattern.compile("(.*ColumnNo=NA05.*)");
         //jiaoxuejingwei
         Pattern patterncol_teaching=Pattern.compile("(.*ColumnNo=NA02.*)");
         //xuesuzixun
         Pattern patterncol_learning=Pattern.compile("(.*ColumnNo=NA03.*)");
         //xueyuandongtai
         Pattern patterncol_activity=Pattern.compile("(.*ColumnNo=NA01.*)");
         Matcher matchactivity=patterncol_activity.matcher(url_yes);
         Matcher matchlearning=patterncol_learning.matcher(url_yes);
         Matcher matchstudent=patterncol_student.matcher(url_yes);
         Matcher matchteaching=patterncol_teaching.matcher(url_yes);
         if (matchactivity.find()){
             Col="学院动态";
         }
         if (matchlearning.find()){
             Col="学术资讯";
         }
         if (matchstudent.find()){
             Col="学生园地";
         }
         if (matchteaching.find()){
             Col="教学经纬";
         }
         return Col;
     }
 
     public String getTime(){
         String Time=new String("cant get it");
         //识别4个汉字。。。
         Pattern news = Pattern.compile("<font size='2'>.*?</font>.*?[\\u4E00-\\u9FA5]{4}:</b>(.*?)&nbsp;&nbsp;<b>");
         Matcher matchedNews = news.matcher(Sources);
         if (matchedNews.find()){
             Time=matchedNews.group(1);
         }
         return Time;
     }
 
     public String getTitle(){
         String Title=new String("cant get it");
         Pattern news = Pattern.compile("<font size='2'>(.*?)</font>");
         Matcher matchedNews = news.matcher(Sources);
         if (matchedNews.find()){
             Title=matchedNews.group(1);
         }
         return Title;
     }
 
     public String getContent(){
         String Content=new String("cant get it");
         Pattern news = Pattern.compile("(<table\\s*width=\"100%\"\\s*align=\"center\">[\\s\\S]*?</table>)");
         Matcher matchedNews = news.matcher(Sources);
         if (matchedNews.find()){
             Content=matchedNews.group(1);
         }
         return Content;
     }
 
 }
