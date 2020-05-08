 package pojo;
 
 import java.util.Set;
 
 public class SiteUser {
     private Integer uid;
     private String username;
     private String password;
     private String salt;
     private Boolean active;
     private Boolean banned;
     private String email;
     private String instituteId;
     private UserEntity userEntity;
     private Register register;
     private Set<Activity> activities;
 
     public Set<Activity> getActivities() {
         return activities;
     }
 
     public void setActivities(Set<Activity> activities) {
         this.activities = activities;
     }
 
     public Integer getUid() {
         return uid;
     }
 
     public void setUid(Integer uid) {
         this.uid = uid;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getSalt() {
         return salt;
     }
 
     public void setSalt(String salt) {
         this.salt = salt;
     }
 
     public Boolean getActive() {
         return active;
     }
 
     public void setActive(Boolean active) {
         this.active = active;
     }
 
     public Boolean getBanned() {
         return banned;
     }
 
     public void setBanned(Boolean banned) {
         this.banned = banned;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getInstituteId() {
         return instituteId;
     }
 
     public void setInstituteId(String instituteId) {
         this.instituteId = instituteId;
     }
 
     public UserEntity getUserEntity() {
         return userEntity;
     }
 
     public void setUserEntity(UserEntity userEntity) {
         this.userEntity = userEntity;
     }
 
     public Register getRegister() {
         return register;
     }
 
     public void setRegister(Register register) {
         this.register = register;
     }
 
     @Override
     public String toString() {
         return this.username + " " + this.email + " " + this.password + " " + this.register.getSiteUser().getUid() + " " + this.register.getRegisterTime();
     }
 }
