 package com.tools.tvguide.managers;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.tools.tvguide.data.ChannelDate;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.utils.HtmlUtils;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ChannelHtmlManager 
 {
     private Context mContext;
     
     public ChannelHtmlManager(Context context)
     {
         assert (context != null);
         mContext = context;
     }
     
     public interface ChannelDetailCallback
     {
         void onProgramsLoaded(int requestId, List<Program> programList);
         void onDateLoaded(int requestId, List<ChannelDate> channelDateList);
     }
     
     public void getChannelDetailAsync(final int requestId, final String channelUrl, final ChannelDetailCallback callback)
     {
         assert (callback != null);
         new Thread(new Runnable() 
         {
             @Override
             public void run() 
             {
                 try 
                 {
                     Document doc = HtmlUtils.getDocument(channelUrl);
                     String protocol = new URL(channelUrl).getProtocol();
                     String host = new URL(channelUrl).getHost();
                     String prefix = protocol + "://" + host;
                  
                     // -------------- 获取节目信息 --------------
                     // 返回结果
                     List<Program> retProgramList = new ArrayList<Program>();
                     Elements programs = doc.select("ul[id=pgrow] li");
                     for (int i=0; i<programs.size(); ++i)
                     {
                         // 过滤结果
                         String time = "";
                         String title = "";
                         String trailer = "";
                         String link = null;
                         
                         Element program = programs.get(i);
                         time = program.select("span").text().trim();
                         if (time == null || time.length() == 0)
                             continue;
                         
                         title = program.text().trim().replaceFirst(time, "").trim();
                         Element dramaElement = program.select("> a[class=drama]").first();      // 直属的孩子
                         if (dramaElement != null)
                         {
                             String dramaString = dramaElement.ownText();
                             title = title.replaceFirst(dramaString + ".+", "").trim();
                         }
                         
                         Element trailer1 = program.select("div[class=tvgd]").first();
                         if (trailer1 != null)        // 存在节目预告
                         {
                             title = title.replace(trailer1.text(), "").trim();
                             
                             Elements pargfs = trailer1.select("p");
                             for (int j=0; j<pargfs.size(); ++j)
                             {
                                 trailer += pargfs.get(j).ownText();
                                 if (j < pargfs.size() - 1)
                                     trailer += "\n";
                             }
                         }
                         Element trailer2 = program.select("div[class=tvcgd]").first();
                         if (trailer2 != null)        // 存在节目预告
                         {
                             title = title.replace(trailer2.text(), "").trim();
                             
                             trailer += trailer2.ownText();
                             Elements pargfs = trailer2.select("p");
                             for (int j=0; j<pargfs.size(); ++j)
                             {
                                 trailer += pargfs.get(j).ownText();
                                 if (j < pargfs.size() - 1)
                                     trailer += "\n";
                             }
                         }
                         
                         Element linkElement = program.select("> a[href]").first();
                         if (linkElement != null)
                             link = prefix + linkElement.attr("href");
                         
                         Program addProgram = new Program();
                         addProgram.time = time;
                         addProgram.title = title;
                         addProgram.trailer = trailer;
                         if (link != null)
                             addProgram.link = link;
                         retProgramList.add(addProgram);
                     }
                     
                     callback.onProgramsLoaded(requestId, retProgramList);
                     
                     // -------------- 获取日期信息 --------------
                     // 返回结果
                     List<ChannelDate> retDateList = new ArrayList<ChannelDate>();
                     Element thisWeek = doc.select("nav.theweek").first();
                     if (thisWeek != null)
                     {
                         Elements dates = thisWeek.select("> a");
                         for (int i=0; i<dates.size(); ++i)
                         {
                             String name = dates.get(i).text();
                             if (name.startsWith("上周"))
                                 continue;
                             
                             if (!name.startsWith("周"))
                                 name = "周" + name;
                             
                             String href = prefix + dates.get(i).attr("href");
                             if (href == null || href.equals(""))    // 当前的页面
                             {
                                 href = channelUrl;
                             }
                             
                             ChannelDate channelDate = new ChannelDate();
                             channelDate.name = name;
                             channelDate.link = href;
                             channelDate.setDay(i + 1);
                             retDateList.add(channelDate);
                         }
                     }
                     
                     Element nextWeek = doc.select("nav.nextweek").first();
                     if (nextWeek != null)
                     {
                         Elements dates = nextWeek.select("> a");
                         for (int i=0; i<dates.size(); ++i)
                         {
                             String name = dates.get(i).text();
                             if (!name.startsWith("周"))
                                 name = "周" + name;
                             if (!name.startsWith("下"))
                                 name = "下" + name;
                             
                             Element test = dates.get(i);
                             String testurl = test.attr("abs:href");
                             Log.d("", testurl);
                             
                             String href = prefix + dates.get(i).attr("href");
                             if (href == null || href.equals(""))    // 当前的页面
                             {
                                 href = channelUrl;
                             }
                             
                             ChannelDate channelDate = new ChannelDate();
                             channelDate.name = name;
                             channelDate.link = href;
                             channelDate.setDay(i + 7 + 1);
                             retDateList.add(channelDate);
                         }
                     }
                     
                     callback.onDateLoaded(requestId, retDateList);
                     
                 }
                 catch (MalformedURLException e) 
                 {
                     e.printStackTrace();
                 }
                 catch (IOException e) 
                 {
                     e.printStackTrace();
                 }
             }
         }).start();
     }
 }
