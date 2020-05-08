 package jpaoletti.jpm.hibernate.security;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.*;
 import org.hibernate.annotations.Type;
 
 @Entity
 @Table(name = "sec_users")
 public class SECUser implements Serializable {
 
     private static final long serialVersionUID = -2596321779435316577L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @Column(unique = true, updatable = false, nullable = false, length = 32)
     private String nick;
     @Column(length = 512, nullable = false)
     private String password;
     private String name;
     @ManyToMany(targetEntity = SECUserGroup.class)
     @JoinTable(name = "sec_user_groups", joinColumns =
     @JoinColumn(name = "sec_user"), inverseJoinColumns =
     @JoinColumn(name = "sec_group"))
     private List<SECUserGroup> groups;
     @Type(type = "yes_no")
     private boolean deleted;
     @Type(type = "yes_no")
     private boolean active;
     private String email;
     @Column(name = "change_password")
     @Type(type = "yes_no")
     private boolean changePassword;
     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
     private List<SECUserProp> props;
 
     public SECUser() {
         super();
     }
 
     public boolean hasPermission(String permName) {
         if (permName == null) {
             return true;
         }
         for (SECUserGroup g : groups) {
             if (g.hasPermission(permName)) {
                 return true;
             }
         }
         return false;
     }
 
     public void logRevision(String s, SECUser me) {
     }
 
     public boolean belongsTo(long gid) {
         for (SECUserGroup g : groups) {
             if (g.getId() == gid) {
                 return true;
             }
         }
         return false;
     }
 
     public String getNick() {
         return nick;
     }
 
     public void setNick(String nick) {
         this.nick = nick;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setDeleted(boolean deleted) {
         this.deleted = deleted;
     }
 
     public boolean isDeleted() {
         return deleted;
     }
 
     public void setActive(boolean active) {
         this.active = active;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void set(String prop, String value) {
         SECUserProp p = getProp(prop);
         if (p != null) {
             p.setPropValue(value);
         } else {
             p = new SECUserProp();
             p.setPropName(prop);
             p.setPropValue(value);
             p.setUser(this);
             props.add(p);
         }
     }
 
     public String get(String prop) {
         SECUserProp p = getProp(prop);
         if (p != null) {
             return p.getPropValue();
         } else {
             return null;
         }
     }
 
     public List<SECUserProp> getProps() {
         return props;
     }
 
     public void setProps(List<SECUserProp> props) {
         this.props = props;
     }
 
     private SECUserProp getProp(String name) {
         if (name == null) {
             return null;
         }
         for (SECUserProp prop : props) {
             if (name.equals(prop.getPropName())) {
                 return prop;
             }
         }
         return null;
     }
 
     public String get(String prop, String defValue) {
         String value = get(prop);
         return value == null ? defValue : value;
     }
 
     public boolean hasProperty(String prop) {
         return get(prop) != null ? true : false;
     }
 
     @Override
     public String toString() {
        return getNickAndId();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SECUser other = (SECUser) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 41 * hash + (int) (this.id ^ (this.id >>> 32));
         return hash;
     }
 
     /**
      * @return "nick(id)"
      */
     public String getNickAndId() {
         StringBuilder sb = new StringBuilder(getNick());
         sb.append('(');
         sb.append(Long.toString(getId()));
         sb.append(')');
         return sb.toString();
     }
 
     public List<SECUserGroup> getGroups() {
         if (groups == null) {
             groups = new ArrayList<SECUserGroup>();
         }
         return groups;
     }
 
     public void setGroups(List<SECUserGroup> groups) {
         this.groups = groups;
     }
 
     /**
      * @param email the email to set
      */
     public void setEmail(String email) {
         this.email = email;
     }
 
     /**
      * @return the email
      */
     public String getEmail() {
         return email;
     }
 
     public void setChangePassword(boolean changePassword) {
         this.changePassword = changePassword;
     }
 
     public boolean isChangePassword() {
         return changePassword;
     }
 }
