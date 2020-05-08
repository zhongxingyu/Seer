 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.webapp;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.tiles.TilesRequestProcessor;
 import org.apache.struts.action.ActionMapping;
 
 /**
  * Extention of the TilesRequestProcessor to assure that roles are
  * proccessed correctly.
  */
 public class JammTilesRequestProcessor extends TilesRequestProcessor
 {
     /**
      * Processes the roles in accordance to the Jamm security model.
      *
      * @param request the httpservet request
      * @param response the http servlet response
      * @param mapping the action mapping
      * @return a boolean
      * @exception IOException if an error occurs
      * @exception ServletException if an error occurs
      */
    protected boolean processRoles(HttpServletRequest request,
                                   HttpServletResponse response,
                                   ActionMapping mapping)
         throws IOException, ServletException
     {
         // Get roles.  If roles aren't defined, we assume its good for
         // all.
         String roles[] = mapping.getRoleNames();
         if ((roles == null) || (roles.length < 1))
         {
             return true;
         }
 
         User user = JammAction.getUser(request);
         for (int i = 0; i < roles.length; i++)
         {
             if (user.isUserInRole(roles[i]))
             {
                 return true;
             }
         }
 
         response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            getInternal().getMessage("notAuthorized",
                                                     mapping.getPath()));
 
         return false;
     }
 }
