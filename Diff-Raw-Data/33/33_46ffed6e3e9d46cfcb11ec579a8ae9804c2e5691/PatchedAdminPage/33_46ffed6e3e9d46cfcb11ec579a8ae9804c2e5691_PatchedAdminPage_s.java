 /*
 
 Copyright (C) 2009 Steffen Dienst
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package de.elatexam.editor.pages;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import net.databinder.auth.components.hib.UserAdminPage;
 
 import org.apache.wicket.authorization.strategies.role.Roles;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 
 import de.elatexam.editor.user.BasicUser;
 
 /**
  * Allow the creation of users only.
  * 
  * @author Steffen Dienst
  * 
  */
 public class PatchedAdminPage extends UserAdminPage<BasicUser> {
   /**
    * ListMultipleChoice needs the same instance of collection for every call to getObject within one request. That
    * means, we can't rely on CompoundPropertyModel....
    */
  private final List<String> roles = new ArrayList<String>(Arrays.asList(Roles.USER));
 
   @Override
   protected IModel<Collection<String>> rolesModel() {
    return new AbstractReadOnlyModel<Collection<String>>() {

      @Override
      public Collection<String> getObject() {
        return roles;
      }
    };
   }
 }
