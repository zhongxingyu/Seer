 package org.otherobjects.cms.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Transient;
 import javax.persistence.Version;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.annotation.Property;
 import org.otherobjects.cms.types.annotation.PropertyType;
 import org.otherobjects.cms.types.annotation.Type;
 import org.springframework.security.GrantedAuthority;
import org.springframework.security.providers.encoding.ShaPasswordEncoder;
 import org.springframework.security.userdetails.UserDetails;
 
 /**
  * This class represents the basic "user" object in AppFuse that allows for authentication
  * and user management.  It implements Acegi Security's UserDetails interface.
  *
  * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
  *         Updated by Dan Kibler (dan@getrolling.com)
  *  Extended to implement Acegi UserDetails interface
  *      by David Carter david@carter.net
  */
 @Entity
 //@Table(name = "app_user")
 @SequenceGenerator(name = "UserSeq", sequenceName = "app_user_seq")
 @Type(label = "User", description = "A User", labelProperty = "email")
 public class User implements Serializable, UserDetails, Editable
 {
 
     private static final long serialVersionUID = -4036033332338732151L;
 
    private static final ShaPasswordEncoder SHA_PASSWORD_ENCODER = new ShaPasswordEncoder();
 
     protected Long id;
     protected Integer version;
     protected String email; // required; unique
     protected String username; // required
     protected String password; // required
     //    protected String plainTextPassword; // required
     //    protected String plainTextConfirmPassword;
     protected String passwordHint;
     protected String firstName; // required
     protected String lastName; // required
     protected List<Role> roles = new ArrayList<Role>();
     protected boolean enabled;
     protected boolean accountExpired;
     protected boolean accountLocked;
     protected boolean credentialsExpired;
 
     protected TypeDef typeDef;
 
     public User()
     {
     }
 
     public User(String username)
     {
         this.username = username;
     }
 
     @Transient
     public String getLabel()
     {
         // FIXME Move this to an superclass? Fetch via annotation?
         return getEmail();
     }
 
     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "UserSeq")
     public Long getId()
     {
         return this.id;
     }
 
     @Column(nullable = false, length = 50, unique = true)
     @Property(type = PropertyType.STRING, required = true, label = "Username", order = 1)
     public String getUsername()
     {
         return this.username;
     }
 
     /**
      * Returns the encoded password.
      */
     @Column(nullable = false)
     public String getPassword()
     {
         return this.password;
         // Return the current hashed password unless the a new plain text one 
         // is available
         //        if (StringUtils.isNotEmpty(getPlainTextPassword()))
         //        {
         //            // TODO Fetch password encoder from Spring. Move this to DAO?
         //            return SHA_PASSWORD_ENCODER.encodePassword(getPlainTextPassword(), null);
         //        }
         //        else
         //        {
         //            return this.password;
         //        }
     }
 
     //    /**
     //     * Transient field that sets the password in plain text. 
     //     * 
     //     * @return
     //     */
     //    @Transient
     //    @Property(required = true, order = 10)
     //    public String getPlainTextPassword()
     //    {
     //        return this.plainTextPassword;
     //    }
     //
     //    @Transient
     //    @Property(order = 11)
     //    public String getPlainTextConfirmPassword()
     //    {
     //        return this.plainTextConfirmPassword;
     //    }
 
     @Column(name = "password_hint")
     @Property(type = PropertyType.TEXT, label = "Password hint", order = 4)
     public String getPasswordHint()
     {
         return this.passwordHint;
     }
 
     @Column(name = "first_name", nullable = false, length = 50)
     @Property(type = PropertyType.STRING, label = "First name", order = 2)
     public String getFirstName()
     {
         return this.firstName;
     }
 
     @Column(name = "last_name", nullable = false, length = 50)
     @Property(type = PropertyType.STRING, label = "Last name", order = 3)
     public String getLastName()
     {
         return this.lastName;
     }
 
     @Column(nullable = false, unique = true)
     @Property(required = true, type = PropertyType.STRING, label = "Email", order = 0)
     public String getEmail()
     {
         return this.email;
     }
 
     /**
      * Returns the full name.
      * @return firstName + ' ' + lastName
      */
     @Transient
     public String getFullName()
     {
         return this.firstName + ' ' + this.lastName;
     }
 
     @ManyToMany(fetch = FetchType.EAGER)
     @org.hibernate.annotations.IndexColumn(name = "index")
     @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "user_id")}, inverseJoinColumns = @JoinColumn(name = "role_id"))
     @Property(type = PropertyType.LIST, label = "Roles", collectionElementType = PropertyType.REFERENCE, relatedType = "org.otherobjects.cms.model.Role", order = 6)
     public List<Role> getRoles()
     {
         return this.roles;
     }
 
     /**
      * Adds a role for the user
      * @param role the fully instantiated role
      */
     public void addRole(Role role)
     {
         getRoles().add(role);
     }
 
     /**
      * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
      */
     @Transient
     public GrantedAuthority[] getAuthorities()
     {
         return this.roles.toArray(new GrantedAuthority[0]);
     }
 
     @Version
     public Integer getVersion()
     {
         return this.version;
     }
 
     @Column(name = "account_enabled")
     @Property(type = PropertyType.BOOLEAN, label = "Enabled", order = 5)
     public boolean isEnabled()
     {
         return this.enabled;
     }
 
     @Column(name = "account_expired", nullable = false)
     public boolean isAccountExpired()
     {
         return this.accountExpired;
     }
 
     /**
      * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
      */
     @Transient
     public boolean isAccountNonExpired()
     {
         return !isAccountExpired();
     }
 
     @Column(name = "account_locked", nullable = false)
     public boolean isAccountLocked()
     {
         return this.accountLocked;
     }
 
     /**
      * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
      */
     @Transient
     public boolean isAccountNonLocked()
     {
         return !isAccountLocked();
     }
 
     @Column(name = "credentials_expired", nullable = false)
     public boolean isCredentialsExpired()
     {
         return this.credentialsExpired;
     }
 
     /**
      * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
      */
     @Transient
     public boolean isCredentialsNonExpired()
     {
         return !this.credentialsExpired;
     }
 
     public void setId(Long id)
     {
         this.id = id;
     }
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     public void setPasswordHint(String passwordHint)
     {
         this.passwordHint = passwordHint;
     }
 
     public void setFirstName(String firstName)
     {
         this.firstName = firstName;
     }
 
     public void setLastName(String lastName)
     {
         this.lastName = lastName;
     }
 
     public void setEmail(String email)
     {
         this.email = email;
     }
 
     public void setRoles(List<Role> roles)
     {
         this.roles = roles;
     }
 
     public void setVersion(Integer version)
     {
         this.version = version;
     }
 
     public void setEnabled(boolean enabled)
     {
         this.enabled = enabled;
     }
 
     public void setAccountExpired(boolean accountExpired)
     {
         this.accountExpired = accountExpired;
     }
 
     public void setAccountLocked(boolean accountLocked)
     {
         this.accountLocked = accountLocked;
     }
 
     public void setCredentialsExpired(boolean credentialsExpired)
     {
         this.credentialsExpired = credentialsExpired;
     }
 
     @Override
     public boolean equals(Object o)
     {
         if (this == o)
             return true;
         if (!(o instanceof User))
             return false;
 
         final User user = (User) o;
 
         return !(this.username != null ? !this.username.equals(user.getUsername()) : user.getUsername() != null);
 
     }
 
     @Override
     public int hashCode()
     {
         return (this.username != null ? this.username.hashCode() : 0);
     }
 
     @Override
     public String toString()
     {
         ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).append("username", this.username).append("enabled", this.enabled).append("accountExpired", this.accountExpired)
                 .append("credentialsExpired", this.credentialsExpired).append("accountLocked", this.accountLocked);
 
         GrantedAuthority[] auths = getAuthorities();
         if (auths != null)
         {
             sb.append("Granted Authorities: ");
 
             for (int i = 0; i < auths.length; i++)
             {
                 if (i > 0)
                 {
                     sb.append(", ");
                 }
                 sb.append(auths[i].toString());
             }
         }
         else
         {
             sb.append("No Granted Authorities");
         }
         return sb.toString();
     }
 
     @Transient
     public String getEditableId()
     {
         return getClass().getName() + "-" + getId();
     }
 
     @Transient
     public TypeDef getTypeDef()
     {
         return this.typeDef;
     }
 
     public void setTypeDef(TypeDef typeDef)
     {
         this.typeDef = typeDef;
     }
 
     //    public void setPlainTextPassword(String plainTextPassword)
     //    {
     //        this.plainTextPassword = plainTextPassword;
     //    }
     //
     //    public void setPlainTextConfirmPassword(String plainTextConfirmPassword)
     //    {
     //        this.plainTextConfirmPassword = plainTextConfirmPassword;
     //    }
 
     // the following is not needed anymore as it is done with annotations now
     //    @Transient
     //    public TypeDef getTypeDef()
     //    {
     //      TypeDef typeDef = new TypeDef();
     //      typeDef.setSuperClassName("java.lang.Object");
     //      typeDef.setClassName(getClass().getName());
     //      typeDef.setDescription("A user");
     //      typeDef.setId(getEditableId());
     //      typeDef.setJcrPath("/Site/users");
     //      typeDef.setLabelProperty("fullName");
     //      //typeDef.addProperty(new PropertyDef("id", "number", null, null, true));
     //      typeDef.addProperty(new PropertyDef("email", "string", null, null, true));
     //      typeDef.addProperty(new PropertyDef("username", "string", null, null));
     //      typeDef.addProperty(new PropertyDef("firstName", "string", null, null));
     //      typeDef.addProperty(new PropertyDef("lastName", "string", null, null));
     //      typeDef.addProperty(new PropertyDef("enabled", "boolean", null, null));
     //      typeDef.addProperty(new PropertyDef("passwordHint", "string", null, null));
     //      
     //      return typeDef;
     //    }
 
 }
