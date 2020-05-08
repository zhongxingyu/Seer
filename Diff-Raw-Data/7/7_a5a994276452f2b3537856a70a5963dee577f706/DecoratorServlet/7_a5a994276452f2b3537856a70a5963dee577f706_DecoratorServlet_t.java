 package com.atlassian.refapp.decorator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.atlassian.refapp.auth.external.WebSudoSessionManager;
 import com.atlassian.templaterenderer.RenderingException;
 import com.atlassian.templaterenderer.TemplateRenderer;
 
 import com.opensymphony.module.sitemesh.HTMLPage;
 import com.opensymphony.module.sitemesh.Page;
 import com.opensymphony.module.sitemesh.RequestConstants;
 import com.opensymphony.module.sitemesh.util.OutputConverter;
 
 import org.apache.commons.io.IOUtils;
 
 public class DecoratorServlet extends HttpServlet
 {
     private static final String SERVLET_PATH = "/refappdecorator";
     private static final String PROPERTIES_LOCATION = "META-INF/maven/com.atlassian.refapp/atlassian-refapp/pom.properties";
 
     private final TemplateRenderer templateRenderer;
     private final WebSudoSessionManager webSudoSessionManager;
     
     public DecoratorServlet(TemplateRenderer templateRenderer, WebSudoSessionManager webSudoSessionManager)
     {
         this.templateRenderer = templateRenderer;
         this.webSudoSessionManager = webSudoSessionManager;
     }
 
     protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
     {
         Page page = (Page) request.getAttribute(RequestConstants.PAGE);
         if (page != null)
         {
             applyDecoratorUsingVelocity(request, page, response);
         }
         else
         {
             String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");
             if (servletPath == null)
             {
                 servletPath = request.getServletPath();
             }
             throw new ServletException("No sitemesh page to decorate. This servlet should not be invoked directly. " +
                 "The path invoked was " + servletPath);
         }
     }
 
     private void applyDecoratorUsingVelocity(HttpServletRequest request, Page page, HttpServletResponse response) throws
         IOException
     {
         String template;
         String pathInfo = (String) request.getAttribute("javax.servlet.include.path_info");
         if (pathInfo == null)
         {
             pathInfo = request.getPathInfo();
         }
         if (pathInfo != null)
         {
             template = mapPathToTemplate(pathInfo);
         }
         else
         {
             template = "/templates/general.vmd";
         }
 
         Map<String, Object> velocityParams = getVelocityParams(request, page, response);
 
         final PrintWriter writer = response.getWriter();
         try
         {
             response.setContentType("text/html");
             templateRenderer.render(template,  velocityParams, writer);
         }
         catch (RenderingException e)
         {
             writer.write("Exception rendering velocity file " + template);
             writer.write("<br><pre>");
             e.printStackTrace(writer);
             writer.write("</pre>");
         }
     }
 
     private String mapPathToTemplate(String pathInfo)
     {
        if (pathInfo.startsWith(SERVLET_PATH))
        {
            return pathInfo.replace(SERVLET_PATH,"templates");
        }

        return pathInfo;
     }
 
     private Properties getPropertiesFromServletContext(String location) throws IOException
     {
         ServletContext servletContext = getServletConfig().getServletContext(); 
         InputStream in = servletContext.getResourceAsStream(location);
 
         if (in == null)
         {
             throw new IOException("Resource '" + location + "' not found");
         }
 
         try
         {
             Properties properties = new Properties();
             properties.load(in);
             return properties;
         }
         finally
         {
             IOUtils.closeQuietly(in);
         }
     }
 
     private Map<String, Object> getVelocityParams(HttpServletRequest request, Page page, HttpServletResponse response)
         throws IOException
     {
         Map<String, Object> velocityParams = new HashMap<String, Object>();
 
         String version;
         try
         {
             Properties properties = getPropertiesFromServletContext(PROPERTIES_LOCATION);
             version = properties.getProperty("version");
         }
         catch (IOException e)
         {
             version = "(unknown)";
         }
         velocityParams.put("version", version);
 
         velocityParams.put("page", page);
         
         if (webSudoSessionManager.isWebSudoSession(request))
         {
             velocityParams.put("websudosession", Boolean.TRUE.toString());
         }
 
         String titleHtml = page.getTitle();
         if (titleHtml == null || titleHtml.isEmpty())
         {
             titleHtml = "Welcome!";
         }
 
         velocityParams.put("titleHtml", titleHtml);
 
         String htmlClass = page.getProperty("class");
 
         if (htmlClass != null)
         {
             velocityParams.put("headClass", htmlClass);
         }
 
         String bodyClass = page.getProperty("body.class");
         if (bodyClass != null)
         {
             velocityParams.put("bodyClass", bodyClass);
         }
 
         StringWriter bodyBuffer = new StringWriter();
         page.writeBody(OutputConverter.getWriter(bodyBuffer));
         velocityParams.put("bodyHtml", bodyBuffer);
 
         if (page instanceof HTMLPage)
         {
             HTMLPage htmlPage = (HTMLPage) page;
             StringWriter buffer = new StringWriter();
             htmlPage.writeHead(OutputConverter.getWriter(buffer));
             velocityParams.put("headHtml", buffer.toString());
         }
 
         velocityParams.put("request", request);
         velocityParams.put("response", response);   // as of 2.5.0 both req and resp must be available in all apps
         return velocityParams;
     }
 }
