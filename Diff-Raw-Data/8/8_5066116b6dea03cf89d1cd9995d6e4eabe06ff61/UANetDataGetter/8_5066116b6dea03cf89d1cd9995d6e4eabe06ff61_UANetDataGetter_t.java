 package com.tools.tvguide.components;
 
 import java.net.MalformedURLException;
 
 import com.tools.tvguide.data.GlobalData;
 import com.tools.tvguide.utils.NetDataGetter;
 
 public class UANetDataGetter extends NetDataGetter
 {
     public UANetDataGetter()
     {
         super();
         setCommonHeaders();
     }
     
     public UANetDataGetter(String url) throws MalformedURLException
     {
         super(url);
         setCommonHeaders();
     }
     
     private void setCommonHeaders()
     {
        setHeader("User-Agent", GlobalData.ChromeUserAgent);
         setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         setHeader("Accept-Language", "zh-CN, en-US");
     }
 }
