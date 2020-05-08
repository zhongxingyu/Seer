 package com.mycelium.rolefen.model.entities;
 
 import java.util.Date;
 
 public class UserEntity implements Entity {
 
     private long _id;
     private long _roleId;
    private long _teamId;
     private boolean _active;
     private String _login;
     private String _firstName;
     private String _lastName;
     private String _password;
     private String _patronymic;
     private String _phone;
     private String _email;    
     private String _rating;
     private String _address;
     private Date _registrationDate;
     private Date _birthday;
 
     @Override
     public long getId() {
         return _id;
     }
 
     @Override
     public void setId(long id) {
         _id = id;
     }
 
     public long getRoleId() {
         return _roleId;
     }
 
     public void setRoleId(long roleId) {
         _roleId = roleId;
     }
 
     public String getLogin() {
         return _login;
     }
     
     public boolean isActive() {
         return _active;
     }
 
     public void setActive(boolean enabled) {
         _active = enabled;
     }
 
     public void setLogin(String login) {
         _login = login;
     }
 
     public String getFirstName() {
         return _firstName;
     }
 
     public void setFirstName(String firstName) {
         _firstName = firstName;
     }
 
     public String getLastName() {
         return _lastName;
     }
 
     public void setLastName(String lastName) {
         _lastName = lastName;
     }
 
    public long getTeamId() {
         return _teamId;
     }
 
    public void setTeamId(long teamId) {
         _teamId = teamId;
     }
 
     public String getPassword() {
         return _password;
     }
 
     public void setPassword(String password) {
         _password = password;
     }
 
     public String getPatronymic() {
         return _patronymic;
     }
 
     public void setPatronymic(String patronymic) {
         _patronymic = patronymic;
     }
 
     public String getPhone() {
         return _phone;
     }
 
     public void setPhone(String phone) {
         _phone = phone;
     }
     
     public String getEmail() {
         return _email;
     }
 
     public void setEmail(String email) {
         this._email = email;
     }
 
     public String getRating() {
         return _rating;
     }
 
     public void setRating(String rating) {
         _rating = rating;
     }
 
     public Date getRegistrationDate() {
         return _registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         _registrationDate = registrationDate;
     }
 
     public String getAddress() {
         return _address;
     }
 
     public void setAddress(String address) {
         _address = address;
     }
 
     public Date getBirthday() {
         return _birthday;
     }
 
     public void setBirthday(Date birthday) {
         _birthday = birthday;
     }
 
 }
