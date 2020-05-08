 package com.cqlybest.common.web;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.cqlybest.common.Constant;
 import com.cqlybest.common.mongo.bean.RequestProcessTime;
 import com.cqlybest.common.mongo.dao.MongoDb;
 import com.cqlybest.common.mongo.service.SettingsService;
 
 /**
  * 纪录请求处理时间，缓存页面、图片等
  */
 public class CqlybestFilter implements Filter {
 
   private static final Logger LOGGER = LoggerFactory.getLogger(CqlybestFilter.class);
 
   private ServletContext servletContext;
   private WebApplicationContext applicationContext;
 
   private SettingsService settingsService;
   private MongoDb mongdoDb;
   private File cacheDir;
 
   @Override
   public void init(FilterConfig filterConfig) throws ServletException {
     servletContext = filterConfig.getServletContext();
     applicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
     settingsService = applicationContext.getBean(SettingsService.class);
     mongdoDb = applicationContext.getBean(MongoDb.class);
     String cacheDirPath =
         System.getProperty(Constant.CONFIG_CACHE_DIR, System.getenv(Constant.CONFIG_CACHE_DIR));
     if (cacheDirPath == null) {
       throw new ServletException("请配置缓存文件夹");
     }
 
     cacheDir = new File(cacheDirPath);
     if (!cacheDir.exists() && !cacheDir.mkdirs()) {
       throw new ServletException("创建缓存文件夹失败");
     }
 
     if (!cacheDir.isDirectory() || !cacheDir.canWrite()) {
       throw new ServletException("缓存文件夹权限不足");
     }
   }
 
   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
       throws IOException, ServletException {
     long start = System.currentTimeMillis();
     HttpServletRequest httpRequest = (HttpServletRequest) request;
     HttpServletResponse httpResponse = (HttpServletResponse) response;
 
     final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
     final GenericResponseWrapper wrapper = new GenericResponseWrapper(httpResponse, outstr);
     chain.doFilter(request, wrapper);
     wrapper.flushBuffer();
 
     final byte[] data = outstr.toByteArray();
     String requestURI = httpRequest.getRequestURI();
     if (requestURI.matches(".*\\.(html|js|css|jpg|png|gif)$")
        && httpResponse.getStatus() == HttpServletResponse.SC_OK) {
       File cacheFile = new File(cacheDir, "/" + httpRequest.getServerName() + requestURI);
       new CacheThread(cacheFile, data).start();
     }
     response.getOutputStream().write(data);
 
     long end = System.currentTimeMillis();
     RequestProcessTime rpt = new RequestProcessTime();
     rpt.setStart(start);
     rpt.setEnd(end);
     rpt.setTime(end - start);
     rpt.setMethod(httpRequest.getMethod());
     rpt.setRequest(httpRequest.getRequestURL().toString());
     rpt.setQuery(httpRequest.getQueryString());
     mongdoDb.createObject("RequestProcessTime", rpt);
   }
 
   @Override
   public void destroy() {
     applicationContext = null;
     servletContext = null;
   }
 
   private class CacheThread extends Thread {
     private File file;
     private byte[] data;
 
     private CacheThread(File file, byte[] data) {
       this.file = file;
       this.data = data;
     }
 
     @Override
     public void run() {
       try {
         Map<?, ?> cacheConfig = (Map<?, ?>) settingsService.getSettings().get("cache");
         if (Boolean.TRUE.equals(cacheConfig.get("enabled"))) {
           FileUtils.forceMkdir(file.getParentFile());
           FileUtils.writeByteArrayToFile(file, data);
         }
       } catch (Exception e) {
         LOGGER.error("缓存文件失败");
         FileUtils.deleteQuietly(file);
         e.printStackTrace();
       }
     }
 
   }
 
 }
