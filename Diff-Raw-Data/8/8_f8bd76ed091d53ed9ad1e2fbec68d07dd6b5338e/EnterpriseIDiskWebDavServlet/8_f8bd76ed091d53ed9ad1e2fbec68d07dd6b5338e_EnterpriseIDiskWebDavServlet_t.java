 package org.apache.maven.enterprise.web;
 
 import org.codehaus.plexus.webdav.servlet.DavServerRequest;
 import org.codehaus.plexus.webdav.util.WebdavMethodUtil;
 import org.codehaus.plexus.security.authentication.AuthenticationException;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Extends the basic WebDav servlet.
  * Adds authentication tests so each user can only write to their own directory.
  * If the publicIDisk flag is false then a user can only see the root and their own files.
  *
  * @uthor: Andrew Williams
  * @since: 31-Jan-2007
  * @version: $Id$
  */
 public class EnterpriseIDiskWebDavServlet
     extends EnterpriseWebDavServlet
 {
     public File getRootDirectory( ServletConfig config )
         throws ServletException
     {
         File serverRoot = new File( enterpriseDavRoot, "idisk" );
 
         if ( serverRoot.exists() )
         {
             if ( !serverRoot.isDirectory() )
             {
                 throw new ServletException( "Unable to create webdav server, " + serverRoot.getAbsolutePath() +
                     " is not a directory ");
             }
         }
         else
         {
             serverRoot.mkdirs();
         }
 
         return serverRoot;
     }
 
     public boolean isAuthenticated( DavServerRequest davRequest, HttpServletResponse response )
         throws ServletException, IOException
     {
         boolean authenticated = super.isAuthenticated( davRequest, response );
 
         if ( authenticated )
         {
             if ( httpAuth.getSessionUser() != null )
             {
                 String user = httpAuth.getSessionUser().getUsername();
 
                 HttpServletRequest request = davRequest.getRequest();
                 if ( request.getPathInfo().equals( "/" + user ) ||
                     request.getPathInfo().startsWith( "/" + user + "/" ) )
                 {
                     File userHome = new File( new File( enterpriseDavRoot, "idisk" ), user );
 
                     if ( !userHome.exists() )
                     {
                         userHome.mkdir();
                     }
                 }
             }
         }
 
         return authenticated;
     }
 
     public boolean isAuthorized( DavServerRequest davRequest, HttpServletResponse response )
         throws ServletException, IOException
     {
         HttpServletRequest request = davRequest.getRequest();
        boolean isRead = WebdavMethodUtil.isReadMethod( request.getMethod() );
 
         /* we don't always need to authenticate read requests */
         if ( isAnonRequest( davRequest ) )
         {
             return true;
         }
 
        /* All users can read all areas, a bit useless otherwise! */
        if ( isRead )
        {
            return true;
        }

         String resource = davRequest.getLogicalResource();
         String user = httpAuth.getSessionUser().getUsername();
 
         if ( resource.equals( "" ) || resource.equals( "/" ) ) {
             return true;
         }
 
         if ( resource.equals( "/" + user ) || resource.startsWith( "/" + user + "/" ) )
         {
             return true;
         }
 
         httpAuth.challenge( request, response, "Enterprise Repository",
                             new AuthenticationException( "Access denied." ) );
         return false;
     }
 
     /**
      * Tell if the passed request can be treated anonymously (no authentication needed).
      * Only read requests can be anonymous, clearly!
      *
      * @param davRequest the request to check
      * @return true if the request is a read request and the area we are requesting is publicly visible
      */
     protected boolean isAnonRequest( DavServerRequest davRequest )
     {
         HttpServletRequest request = davRequest.getRequest();
         boolean isRead = WebdavMethodUtil.isReadMethod( request.getMethod() );
 
         if ( isRead )
         {
             /* if the idisks are public we do not require authentication for read requests */
             if ( config.getWebdav().isPublicIDisk() )
             {
                 return true;
             }
         }
 
         return false;
     }
 }
