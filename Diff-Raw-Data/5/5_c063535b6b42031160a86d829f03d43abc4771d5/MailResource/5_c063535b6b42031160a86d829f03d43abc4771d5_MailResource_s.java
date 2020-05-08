 /*  
  * MailResource.java  
  *   
  * Copyright (C) 2009 LWsystems GmbH & Co. KG  
  * This program is free software; you can redistribute it and/or 
  * modify it under the terms of the GNU General Public License as 
  * published by the Free Software Foundation; either version 2 of 
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,  
  * but WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the  
  * GNU General Public License for more details.  
  *   
  * You should have received a copy of the GNU General Public License  
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 package de.lwsystems.mailarchive.web.service;
 
 import de.lwsystems.mailarchive.repository.MessageID;
 import de.lwsystems.mailarchive.web.SearchResultModel;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.Produces;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.PathParam;
 import org.apache.lucene.index.CorruptIndexException;
 
 /**
  * REST Web Service
  *
  * @author rene
  */
 @Path("mail/{id}")
 public class MailResource {
 
     @Context
     private UriInfo context;
     @Context
     private ServletContext servletContext;
 
     /** Creates a new instance of MailResource */
     public MailResource() {
     }
 
     /**
      * Retrieves representation of an instance of de.lwsystems.mailarchive.web.service.MailResource
      * @return an instance of java.lang.String
      */
     @GET
     @Produces("message/rfc822")
     public String getXml(@PathParam("id") String id) {
         SearchResultModel srm = null;
 
         if (servletContext == null) {
             return "No servlet context!";
         }
         try {
             srm = SearchResultModel.getDefaultInstance(servletContext);
 
 
             if (srm == null) {
                 return "ERROR No SearchResultModel created! Servlet Context Path: " + servletContext.getServletContextName() + "\n";
             }
 
             StringBuilder sb = new StringBuilder();
             BufferedInputStream bin = new BufferedInputStream(srm.getRepository().getDocument(new MessageID(id)));
             int ch = 0;
             try {
                 while ((ch = bin.read()) > -1) {
                     sb.append((char) ch);
                 }
             } catch (IOException ex) {
                 Logger.getLogger(MailResource.class.getName()).log(Level.SEVERE, null, ex);
             }
            
             return sb.toString();
         } catch (CorruptIndexException ex) {
            Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
 
         } catch (IOException ex) {
            Logger.getLogger(BennoSearchResource.class.getName()).log(Level.SEVERE, null, ex);
 
         } finally {
         }
 
         return "";
     }
 }
