 /**
  * Copyright (c) 2008-2010, Peter Major All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Peter Major nor the names of
  * its contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission. * All advertising
  * materials mentioning features or use of this software must display the
  * following acknowledgement: This product includes software developed by the
  * Kir-Dev Team, Hungary and its contributors.
  *
  * THIS SOFTWARE IS PROVIDED BY Peter Major ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
  * EVENT SHALL Peter Major BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package hu.sch.kurzuscsere.authz;
 
 import hu.sch.kurzuscsere.domain.User;
 import java.util.Set;
 import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
 import org.apache.wicket.Application;
 import org.apache.wicket.Request;
 import org.apache.wicket.protocol.http.WebRequest;
 
 /**
  * Autorizációs feladatok ellátásáért felelős osztály, mely feldolgozza az
  * agenttől kapott adatokat, melyeket az egyes oldalak később le tudnak
  * kérdezni.
  *
  * @author hege
  * @author balo
  */
 @SuppressWarnings("unchecked")
 public final class AgentBasedAuthorization implements UserAuthorization {
 
     /**
      * Logoláshoz szükséges objektum
      */
    private static final Logger log =
            Logger.getLogger(AgentBasedAuthorization.class);
     /**
      * A vezetéknévhez tartozó HTTP header kulcs
      */
     private static final String LASTNAME_ATTRNAME = "sn";
     /**
      * A keresztnévhez tartozó HTTP header kulcs
      */
     private static final String FIRSTNAME_ATTRNAME = "givenName";
     /**
      * A becenévhez tartozó HTTP header kulcs
      */
     private static final String NICNKAME_ATTRNAME = "displayName";
     /**
      * Az e-mailhez tartozó HTTP header kulcs
      */
     private static final String EMAIL_ATTRNAME = "mail";
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void init(Application wicketApplication) {
         log.warn("Agent based authorization mode successfully initiated.");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public User getUserAttributes(Request wicketRequest) {
         HttpServletRequest servletRequest =
                 ((WebRequest) wicketRequest).getHttpServletRequest();
         User user = new User();
 
         user.setEmail(getSingleValuedStringAttribute(servletRequest, EMAIL_ATTRNAME));
         user.setNick(getSingleValuedStringAttribute(servletRequest, NICNKAME_ATTRNAME));
 
         StringBuilder nameSb =
                 new StringBuilder(getSingleValuedStringAttribute(servletRequest, LASTNAME_ATTRNAME));
         nameSb.append(" ").append(getSingleValuedStringAttribute(servletRequest, FIRSTNAME_ATTRNAME));
         user.setName(nameSb.toString());
 
         return user;
     }
 
     /**
      * {@inheritDoc}
      */
     private String getSingleValuedStringAttribute(HttpServletRequest request, String attrName) {
         Set<String> attrSet = (Set<String>) request.getAttribute(attrName);
 
         if (attrSet != null) {
             for (String string : attrSet) {
                 return string;
             }
         }
 
         return null;
     }
 
     /*
      * {@inheritDoc}
      */
     @Override
     public String getRemoteUser(Request wicketRequest) {
         return ((WebRequest) wicketRequest).getHttpServletRequest().getRemoteUser();
     }
 }
