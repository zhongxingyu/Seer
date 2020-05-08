 /*
  * Copyright 2007 Wyona
  */
 
 package org.wyona.yanel.impl.resources.foaf;
 
 import org.wyona.yanel.core.Resource;
 import org.wyona.yanel.core.api.attributes.ViewableV2;
 import org.wyona.yanel.core.attributes.viewable.View;
 import org.wyona.yanel.core.attributes.viewable.ViewDescriptor;
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 import org.wyona.yanel.impl.resources.xml.ConfigurableViewDescriptor;
 
 import org.apache.log4j.Category;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringBufferInputStream;
 import java.net.URL;
 
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 /**
  *
  */
 public class FOAFResource extends BasicXMLResource implements ViewableV2 {
 //public class FOAFResource extends Resource implements ViewableV2 {
 
     private static Category log = Category.getInstance(FOAFResource.class);
 
     /**
      *
      */
     public FOAFResource() {
     }
 
     /**
      *
      */
     public boolean exists() {
         log.error("Implementation not finished yet! Needs to check existence of local or remote FOAF.");
         return true;
     }
 
     /**
      *
      */
     public String getMimeType(String viewId) {
         if (viewId !=  null && viewId.equals("source")) {
             return "application/xml";
         } else if (viewId !=  null && viewId.equals("rdf+xml")) {
             return "application/rdf+xml";
         } else if (viewId !=  null && viewId.equals("atom")) {
             return "application/atom+xml";
         } else {
             return "application/xhtml+xml";
         }
     }
 
     /**
      *
      */
     public long getSize() {
         log.error("Implementation not done yet!");
         return -1;
     }
 
     /**
      *
      */
     public View getView(String viewId) {
         View view = new View();
         String path = getPath();
 
 
         try {
 	    if (path.lastIndexOf(".rdf") == path.length() - 4) {
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType("rdf+xml"));
                 return view;
             }
 
             StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
             sb.append("<wyona:foaf xmlns:wyona=\"http://www.wyona.org/foaf/1.0\">");
             if (getRequest().getParameter("href") != null) {
                 sb.append("<wyona:third-party-source href=\"" + getRequest().getParameter("href") + "\"/>");
             } else {
                 sb.append("<wyona:source href=\"" + path.substring(0, path.lastIndexOf(".html")) + ".rdf\"/>");
             }
             // TODO: The following leads to errors if the RDF contains special characters!
             BufferedReader br = new BufferedReader(new InputStreamReader(getRDFAsInputStream()));
             String line;
             while((line = br.readLine()) != null){
                 if (line.indexOf("<?xml") < 0) {
                     sb.append(line);
                 }
             }
             sb.append("</wyona:foaf>");
 
            log.error("DEBUG: XML: " + sb.toString());
 
             if (viewId != null && viewId.equals("source")) {
                 view.setInputStream(new StringBufferInputStream(sb.toString()));
                 view.setMimeType(getMimeType(viewId));
                 return view;
 	    } else if (viewId != null && viewId.equals("rdf+xml")) {
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType(viewId));
                 return view;
 	    } else if (viewId != null && viewId.equals("atom")) {
                 view.setInputStream(new StringBufferInputStream(new StringBuffer("Not implemented yet!").toString()));
                 view.setMimeType("text/plain");
 /*
                 view.setInputStream(getRDFAsInputStream());
                 view.setMimeType(getMimeType(viewId));
 */
                 return view;
             } else {
                 return getXMLView(viewId, new StringBufferInputStream(sb.toString()));
             }
         } catch (java.io.FileNotFoundException e) {
             log.error(e);
             view.setInputStream(new StringBufferInputStream(new StringBuffer("No such file: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             view.setInputStream(new StringBufferInputStream(new StringBuffer("Exception: " + e.getMessage()).toString()));
             view.setMimeType("text/plain");
         }
         return view;
     }
 
     /**
      *
      */
     public ViewDescriptor[] getViewDescriptors() {
         ConfigurableViewDescriptor[] vd = new ConfigurableViewDescriptor[4];
 
         vd[0] = new ConfigurableViewDescriptor("default");
         vd[0].setMimeType(getMimeType(null));
 
         vd[1] = new ConfigurableViewDescriptor("source");
         vd[1].setMimeType(getMimeType("source"));
 
         vd[2] = new ConfigurableViewDescriptor("rdf+xml");
         vd[2].setMimeType(getMimeType("rdf+xml"));
 
         vd[3] = new ConfigurableViewDescriptor("atom");
         vd[3].setMimeType(getMimeType("atom"));
         return vd;
     }
 
     /**
      *
      */
     private InputStream getRDFAsInputStream() throws Exception {
         if (getRequest().getParameter("href") != null) {
             URL url = new URL(getRequest().getParameter("href"));
             return url.openConnection().getInputStream();
         } else {
             String path = getPath();
 	    if (path.endsWith(".html")) {
                 path = path.substring(0, path.lastIndexOf(".html")) + ".rdf";
             }
            log.error("DEBUG: RDF path: " + path);
             return getProfilesRepository().getNode(path).getInputStream();
         }
     }
 
     /**
      *
      */
     private Repository getProfilesRepository() throws Exception {
         Repository repo = getRealm().getRepository();
 	String repoConfig = getResourceConfigProperty("repo-config");
         if (repoConfig != null) {
             repo = new RepositoryFactory().newRepository("profiles", new File(repoConfig));
         }
         return repo;
     }
 }
