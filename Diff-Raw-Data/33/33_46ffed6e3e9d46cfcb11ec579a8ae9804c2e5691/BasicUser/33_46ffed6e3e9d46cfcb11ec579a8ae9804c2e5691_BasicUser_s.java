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
 package de.elatexam.editor.user;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 
 import net.databinder.auth.data.DataPassword;
 import net.databinder.auth.data.DataUser;
 import net.databinder.auth.data.hib.BasicPassword;
 
 import org.apache.wicket.authorization.strategies.role.Roles;
 
 import de.elatexam.model.ComplexTaskDef;
 import de.elatexam.model.Indexed;
 import de.elatexam.model.SubTaskDef;
 
 /**
  * @author Steffen Dienst
  *
  */
 @Entity
 public class BasicUser implements DataUser, Indexed {
   private BasicPassword password;
 
   @Column(unique = true)
   private String username;
 
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
     private Long hjid;
 
   @ElementCollection
   private final Set<String> roles;
 
   @ElementCollection
   private final List<ComplexTaskDef> taskdefs;
 
   @ElementCollection
   private final List<SubTaskDef> subtaskdefs;
 
   public BasicUser() {
     taskdefs = new ArrayList<ComplexTaskDef>();
     subtaskdefs = new ArrayList<SubTaskDef>();
 
     roles = new Roles(Roles.USER);
     password = new BasicPassword();
   }
 
   /**
    * @return the subtaskdefs
    */
   public List<SubTaskDef> getSubtaskdefs() {
     return subtaskdefs;
   }
 
   /**
    * Filter {@link #getSubtaskdefs()} to instances of a specific {@link SubTaskDef}. CAUTION: You must not add new
    * instances to this list, they won't get persisted. Use {@link #getSubtaskdefs()}.add(...) instead.
    *
    * @param <T>
    * @param clazz
    * @return
    */
   public <T extends SubTaskDef> Collection<T> getSubtaskdefsOf(final Class<T> clazz) {
     final Set<T> res = new HashSet<T>();
     for (final SubTaskDef st : getSubtaskdefs()) {
       if (st.getClass().isAssignableFrom(clazz)) {
         res.add((T) st);
       }
     }
     return res;
   }
 
   public void addRole(final String role) {
     this.roles.add(role);
   }
 
     public Long getHjid() {
     return hjid;
   }
 
   /*
    * (non-Javadoc)
    *
    * @see net.databinder.auth.data.DataUser#getPassword()
    */
   public DataPassword getPassword() {
     return password;
   }
 
   public Set<String> getRoles() {
     return new HashSet<String>(roles);
   }
 
   public List<ComplexTaskDef> getTaskdefs() {
     return taskdefs;
   }
 
   /*
    * (non-Javadoc)
    *
    * @see net.databinder.auth.data.DataUser#getUsername()
    */
   public String getUsername() {
     return username;
   }
 
   /*
    * (non-Javadoc)
    *
    * @see net.databinder.auth.data.DataUser#hasRole(java.lang.String)
    */
   public boolean hasRole(final String role) {
     return roles.contains(role);
   }
 
   public void setPassword(final BasicPassword password) {
     this.password = password;
   }
 
   public void setUsername(final String username) {
     this.username = username;
   }
 
   @Override
   public String toString() {
     return String.format("[user '%s']", getUsername());
   }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.elatexam.model.Indexed#setHjid(java.lang.Long)
      */
     @Override
     public void setHjid(Long id) {
         this.hjid = id;
     }
 }
