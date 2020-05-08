 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package org.lafayette.server.resources;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import org.apache.commons.lang3.StringUtils;
 import org.lafayette.server.log.Log;
 import org.lafayette.server.log.Logger;
 import org.lafayette.server.Registry;
 import org.lafayette.server.ServerContextListener;
 import org.lafayette.server.domain.Finders;
 import org.lafayette.server.domain.User;
 import org.lafayette.server.domain.UserFinder;
 import org.lafayette.server.http.MediaType;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @Path("/")
 public class IndexResource {
     private static final int PAD_ID = 5;
     private static final int PAD_LOGIN_NAME = 16;
     private static final int PAD_HASHED_PASSWORD = 34;
 
     @Context
     private ServletContext context;
     private static final String NL = "\r\n";
     private final Logger log = Log.getLogger(this);
 
     @Produces(MediaType.TEXT_PLAIN)
     @GET
     public String indexAsText() {
         final StringBuilder buffer = new StringBuilder("Lafayette Server");
         buffer.append(NL).append(NL).append("Hello World!");
         return buffer.toString();
     }
 
    @Produces(MediaType.TEXT_URI_LIST)
     @GET
     public String indexAsUriList() {
         final StringBuilder buffer = new StringBuilder();
         buffer.append("# Available URIs:").append(NL)
                 .append("/users").append(NL);
         return buffer.toString();
     }
 
     @Produces(MediaType.TEXT_PLAIN)
     @GET @Path("users")
     public String usersAsText() {
         final StringBuilder buffer = new StringBuilder();
         final Registry registry = (Registry) context.getAttribute(ServerContextListener.REGISRTY);
         final UserFinder finder = Finders.forUsers(registry.getMappers());
         buffer.append(StringUtils.rightPad("id", PAD_ID))
               .append(StringUtils.rightPad("loginName", PAD_LOGIN_NAME))
               .append(StringUtils.rightPad("hashedPassword", PAD_HASHED_PASSWORD))
               .append("salt")
               .append(NL);
 
         for (final User user : finder.findAll(25, 0)) {
             buffer.append(StringUtils.rightPad(String.valueOf(user.getId()), PAD_ID))
                   .append(StringUtils.rightPad(user.getLoginName(), PAD_LOGIN_NAME))
                   .append(StringUtils.rightPad(user.getHashedPassword(), PAD_HASHED_PASSWORD))
                   .append(user.getSalt()).append(NL);
         }
 
         return buffer.toString();
     }
 
 }
