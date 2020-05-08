 package edu.ibs.core.entity;
 
 import edu.ibs.common.dto.AccountDTO;
 import edu.ibs.common.enums.AccountRole;
 import java.io.Serializable;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  *
  * @date Dec 13, 2012
  *
  * @author Vadim Martos
  */
 @Entity
 @Table(name = "Account")
 @XmlRootElement
 public class Account implements Serializable, AbstractEntity {
 
     private static final long serialVersionUID = 4245141234284L;
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
     @Column(name = "id", updatable = false, nullable = false, unique = true)
     private long id;
     // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
     @Basic(optional = false)
     @Column(name = "email", nullable = false, updatable = false, unique = true)
     private String email;
     @Basic(optional = false)
     @Column(name = "role", nullable = false, updatable = false)
     @Enumerated(EnumType.STRING)
     private AccountRole role;
     @Basic(optional = false)
     @Column(name = "password", nullable = false, updatable = false)
     private String password;
     @Column(name = "securityQuestion")
     private String securityQuestion;
     @Column(name = "securityAnswer")
     private String securityAnswer;
     @Column(name = "avatar")
     private String avatar;
     @JoinColumn(name = "userID", referencedColumnName = "id")
     @OneToOne(optional = true)
     private User user;
 
     public Account() {
     }
 
     public Account(String email, String password, AccountRole role, String securityQuestion, String securityAnswer, String avatar) {
         this.email = email;
         this.role = role;
         this.password = password;
         this.securityQuestion = securityQuestion;
         this.securityAnswer = securityAnswer;
         this.avatar = avatar;
     }
 
     public Account(String email, String password, AccountRole role, String securityQuestion, String securityAnswer) {
         this(email, password, role, securityQuestion, securityAnswer, null);
     }
 
     public Account(String email, String password, AccountRole role) {
         this(email, password, role, null, null, null);
     }
 
     public Account(AccountDTO dto) {
         this(dto.getEmail(), dto.getPassword(), dto.getRole(), dto.getSecurityQuestion(), dto.getSecurityAnswer(), dto.getAvatar());
         this.id = dto.getId();
		if (!AccountRole.ADMIN.equals(dto.getRole())) {
			setUser(new User(dto.getUser()));
		}
     }
 
     public String getAvatar() {
         return avatar;
     }
 
     public String getEmail() {
         return email;
     }
 
     public long getId() {
         return id;
     }
 
     public String getPassword() {
         return password;
     }
 
     public AccountRole getRole() {
         return role;
     }
 
     public String getSecurityAnswer() {
         return securityAnswer;
     }
 
     public String getSecurityQuestion() {
         return securityQuestion;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setAvatar(String avatar) {
         this.avatar = avatar;
     }
 
     public void setSecurityAnswer(String securityAnswer) {
         this.securityAnswer = securityAnswer;
     }
 
     public void setSecurityQuestion(String securityQuestion) {
         this.securityQuestion = securityQuestion;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Account other = (Account) obj;
         if (this.id != other.id) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 89 * hash + (int) (this.id ^ (this.id >>> 32));
         return hash;
     }
 
     @Override
     public String toString() {
         return "Account{" + "id=" + id + ", email=" + email + ", role=" + role + ", password=" + password + ", securityQuestion=" + securityQuestion + ", securityAnswer=" + securityAnswer + ", avatar=" + avatar + ", user=" + user + '}';
     }
 }
