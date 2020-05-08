 package org.dspace.resourcesync;
 
 import org.dspace.authorize.AuthorizeException;
 import org.dspace.content.DSpaceObject;
 import org.dspace.content.crosswalk.CrosswalkException;
 import org.dspace.content.crosswalk.DisseminationCrosswalk;
 import org.dspace.core.ConfigurationManager;
 import org.dspace.core.Constants;
 import org.dspace.core.Context;
 import org.dspace.core.PluginManager;
 import org.dspace.handle.HandleManager;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.SQLException;
 
 public class ResourceSyncServlet extends HttpServlet
 {
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException
     {
         resp.setCharacterEncoding("UTF-8");
 
         // determine which kind of request this is
         String path = req.getPathInfo();
         if (path.startsWith("/resource/"))
         {
             this.serveMetadata(req, resp);
         }
         else
         {
             this.serveStatic(req, resp);
         }
     }
 
     private void serveMetadata(HttpServletRequest req, HttpServletResponse resp)
             throws IOException, ServletException
     {
         String path = req.getPathInfo();
 
         // the path starts with "/resource/", so we want to strip that out
         path = path.substring("/resource/".length());
 
         // get the metadata format off the end
         String[] bits = path.split("/");
         if (bits.length < 3)
         {
             resp.sendError(404);
             return;
         }
         String formatPrefix = bits[bits.length - 1];
 
         // now get the handle out of the middle (substring off the format prefix and the "/" before it)
         String handle = path.substring(0, path.length() - formatPrefix.length() - 1);
 
         Context context = null;
         try
         {
             context = new Context();
 
             // get the DSpace object that we want to expose
             DSpaceObject dso = HandleManager.resolveToObject(context, handle);
 
             // if we're not given an item, we can't crosswalk it
             if (dso.getType() != Constants.ITEM)
             {
                 resp.sendError(404);
                 return;
             }
 
             // get the dissemination crosswalk for this prefix and get the element for the object
             DisseminationCrosswalk dc = (DisseminationCrosswalk) PluginManager.getNamedPlugin(DisseminationCrosswalk.class, formatPrefix);
             Element element = dc.disseminateElement(dso);
 
             DSpaceResourceDocument drd = new DSpaceResourceDocument();
             MetadataFormat mdf = drd.getMetadataFormat(formatPrefix);
             resp.setContentType(mdf.getMimetype());
 
             // serialise the element out to a string
             Document doc = new Document(element);
             XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
             OutputStream os = resp.getOutputStream();
             out.output(doc, os);
         }
         catch(SQLException e)
         {
             throw new ServletException(e);
         }
         catch (CrosswalkException e)
         {
             throw new ServletException(e);
         }
         catch (AuthorizeException e)
         {
             // if we can't access the resource, then pretend that it doesn't exist
             resp.sendError(404);
         }
         finally
         {
             if (context != null)
             {
                 context.abort();
             }
         }
     }
 
     private void serveStatic(HttpServletRequest req, HttpServletResponse resp)
             throws IOException
     {
         // this is a standard resourcesync document serve
         String dir = ConfigurationManager.getProperty("resourcesync", "resourcesync.dir");
         if (dir == null)
         {
             resp.sendError(404);
             return;
         }
         String document = req.getPathInfo();
         if (document.startsWith("/"))
         {
             document = document.substring(1);
         }
         String filepath = dir + File.separator + document;
 
         File f = new File(filepath);
         if (!f.exists() || !f.isFile())
         {
             resp.sendError(404);
             return;
         }
 
        resp.setContentType("application/xml");
 
         InputStream is = new FileInputStream(f);
         OutputStream os = resp.getOutputStream();
 
         byte[] buffer = new byte[102400]; // 100k chunks
         int len = is.read(buffer);
         while (len != -1)
         {
             os.write(buffer, 0, len);
             len = is.read(buffer);
         }
 
         return;
     }
 
 }
