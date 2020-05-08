 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.domain;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import java.security.Principal;
 import java.util.Collection;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.lafayette.server.domain.Role.Names;
 import org.msgpack.annotation.Message;
 
 /**
  * Represents an user.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 @XmlRootElement @Message
 public class User extends BaseDomainObject implements Principal {
 
     /**
      * Default anonymous user for all.
      */
     public static final User ANONYMOUS = new User(0, "", "");
     static {
         ANONYMOUS.addRole(new Role(0, ANONYMOUS.getId(), Names.ANONYMOUS));
     }
 
     /**
      * Unique login name of the user.
      */
     private String loginName;
     /**
      * Hashed user data.
      *
      * hashedUserData = md5 ( username : realm : password )
      */
     private String hashedUserData;
     /**
      * Roles a user has.
      */
     private final Map<Names, Role> roles = Maps.newTreeMap();
 
     /**
      * No argument constructor for necessary for Jackson XML generation.
      */
     public User() {
         this("", "");
     }
 
     /**
      * Initializes the {@link BaseDomainObject#id} with {@link BaseDomainObject#UNINITIALIZED_ID}.
      *
      * This constructor should be used for user objects which were not yet saved in the database.
      *
      * @param loginName the login name
      * @param hashedUserData the hashed password
      */
     public User(final String loginName, final String hashedUserData) {
         this(UNINITIALIZED_ID, loginName, hashedUserData);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param id the primary key
      * @param loginName the login name
      * @param hashedUserData the hashed password
      */
     public User(final int id, final String loginName, final String hashedUserData) {
         super(id);
         this.loginName = loginName;
         this.hashedUserData = hashedUserData;
     }
 
     public void setLoginName(final String loginName) {
         this.loginName = loginName;
     }
 
     public void setHashedUserData(final String hashedUserData) {
         this.hashedUserData = hashedUserData;
     }
 
     public String getLoginName() {
         return loginName;
     }
 
     public String getHashedUserData() {
         return hashedUserData;
     }
 
     public Collection<Role> getRoles() {
         return Sets.newHashSet(roles.values());
     }
 
    public boolean hasRole(final String name) {
        return hasRole(Names.forName(name));
    }

    public boolean hasRole(final Names name) {
        return roles.containsKey(name);
     }
 
     public boolean hasRole(final Role r) {
         return roles.containsValue(r);
     }
 
     public void removeRole(final Role r) {
         roles.remove(r.getName());
     }
 
     public void addRole(final Role r) {
         roles.put(r.getName(), r);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(super.hashCode(), loginName, hashedUserData, roles);
     }
 
     @Override
     public boolean equals(final Object obj) {
         if (!(obj instanceof User)) {
             return false;
         }
 
         final User other = (User) obj;
 
         return super.equals(other)
                 && Objects.equal(loginName, other.loginName)
                 && Objects.equal(hashedUserData, other.hashedUserData)
                 && Objects.equal(roles, other.roles);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(this)
                 .addValue(super.toString())
                 .add("loginName", loginName)
                 .add("hashedUserData", hashedUserData)
                 .add("roles", roles)
                 .toString();
     }
 
     @Override
     public String getName() {
         return loginName;
     }
 }
